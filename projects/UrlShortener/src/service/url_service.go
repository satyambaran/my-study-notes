package service

import (
	"context"
	"errors"
	"log/slog"
	"sync"
	"time"

	"github.com/go-redis/redis/v8"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/satyambaran/UrlShortener/src/config"
	"github.com/satyambaran/UrlShortener/src/model"
	"github.com/satyambaran/UrlShortener/src/repository"
	"gorm.io/gorm"
)

// CacheClient is the subset of *redis.Client that the service uses.
// Defining it as an interface makes the service testable without a real Redis server.
type CacheClient interface {
	Get(ctx context.Context, key string) *redis.StringCmd
	Set(ctx context.Context, key string, value interface{}, expiration time.Duration) *redis.StatusCmd
	IncrBy(ctx context.Context, key string, value int64) *redis.IntCmd
}

// baseURL is read from BASE_URL env var so it can be set correctly in each
// environment (localhost for local dev, minikube IP:port for Kubernetes, etc.).
var baseURL = func() string {
	cfg := config.Load()
	if u := cfg.BASEURL; u != "" {
		return u
	}
	return "http://localhost:3000/"
}()

const (
	ttl        = 3 * 24 * time.Hour
	blockSize  = 1000
	counterKey = "shortener:id_counter"
)

// base62Chars defines the alphabet for encoding IDs into short URL tokens.
// Order: digits → lowercase → uppercase, giving 62 symbols total.
const base62Chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

var ctx = context.Background()

type URLService interface {
	Shorten(url, requestedShortURL string) (string, error)
	Resolve(shortURL string) (string, error)
}

type urlService struct {
	repo      repository.URLRepository
	cache     CacheClient
	log       *slog.Logger
	mu        sync.Mutex
	currentID int64
	rangeEnd  int64
}

func NewURLService(repo repository.URLRepository, cache CacheClient, log *slog.Logger) URLService {
	return &urlService{
		repo:      repo,
		cache:     cache,
		log:       log,
		currentID: 1, // set > rangeEnd(0) so the first call to nextID claims a range
		rangeEnd:  0,
	}
}

// encode converts a positive integer to a base62 string.
// IDs grow in length naturally: 1–61 → 1 char, 62–3843 → 2 chars, etc.
func encode(n int64) string {
	var buf [12]byte // ceil(log62(2^63)) = 11 digits max
	pos := len(buf)
	for n > 0 {
		pos--
		buf[pos] = base62Chars[n%62]
		n /= 62
	}
	return string(buf[pos:])
}

// claimRange atomically claims the next block of IDs from Redis.
// INCRBY returns the new counter value, which is the top (inclusive) of our range.
// Must be called with s.mu held.
func (s *urlService) claimRange() error {
	top, err := s.cache.IncrBy(ctx, counterKey, blockSize).Result()
	if err != nil {
		return err
	}
	s.currentID = top - blockSize + 1
	s.rangeEnd = top
	s.log.Info("claimed ID range", "from", s.currentID, "to", s.rangeEnd)
	return nil
}

// nextID returns the next unique ID, claiming a new range from Redis when needed.
func (s *urlService) nextID() (int64, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	if s.currentID > s.rangeEnd {
		if err := s.claimRange(); err != nil {
			return 0, err
		}
	}
	id := s.currentID
	s.currentID++
	return id, nil
}

// isDuplicateKeyError covers both gorm.ErrDuplicatedKey (not always set by the
// pgx driver) and the raw PostgreSQL SQLSTATE 23505.
func isDuplicateKeyError(err error) bool {
	if errors.Is(err, gorm.ErrDuplicatedKey) {
		return true
	}
	var pgErr *pgconn.PgError
	return errors.As(err, &pgErr) && pgErr.Code == "23505"
}

func (s *urlService) Shorten(url, shortURL string) (string, error) {
	if shortURL != "" {
		err := s.repo.Create(&model.URL{ShortURL: shortURL, OriginalURL: url, IsCustomAlias: true})
		if isDuplicateKeyError(err) {
			return "", errors.New("requested url is not available")
		}
		if err != nil {
			return "", err
		}
		s.cache.Set(ctx, shortURL, url, ttl)
		s.log.Info("URL shortened with custom alias", "short", shortURL, "original", url)
		return baseURL + shortURL, nil
	}

	// Return the existing auto-generated short URL if one already exists for
	// this original URL (registered without a custom alias).
	if existing, err := s.repo.FindByOriginalURL(url); err == nil {
		s.cache.Set(ctx, existing.ShortURL, url, ttl)
		s.log.Info("URL already shortened, returning existing", "short", existing.ShortURL, "original", url)
		return baseURL + existing.ShortURL, nil
	}

	// Claim a globally unique ID and encode it. No collision is possible
	// because each server's range is allocated atomically from Redis.
	id, err := s.nextID()
	if err != nil {
		return "", err
	}
	shortURL = encode(id)

	if err := s.repo.Create(&model.URL{ShortURL: shortURL, OriginalURL: url}); err != nil {
		return "", err
	}
	s.cache.Set(ctx, shortURL, url, ttl)
	s.log.Info("URL shortened", "short", shortURL, "original", url, "id", id)
	return baseURL + shortURL, nil
}

func (s *urlService) Resolve(shortURL string) (string, error) {
	if originalURL, err := s.cache.Get(ctx, shortURL).Result(); err == nil {
		s.log.Info("cache hit", "short", shortURL)
		return originalURL, nil
	}

	url, err := s.repo.FindByShortURL(shortURL)
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return "", errors.New("URL not found")
	}
	if err != nil {
		return "", err
	}

	s.cache.Set(ctx, shortURL, url.OriginalURL, ttl)
	s.log.Info("cache miss, fetched from db", "short", shortURL)
	return url.OriginalURL, nil
}
