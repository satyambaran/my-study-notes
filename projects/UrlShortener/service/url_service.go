package service

import (
	"context"
	"crypto/sha256"
	"encoding/base64"
	"errors"
	"log/slog"
	"math/rand"
	"time"

	"github.com/go-redis/redis/v8"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/satyambaran/UrlShortener/model"
	"github.com/satyambaran/UrlShortener/repository"
	"gorm.io/gorm"
)

const (
	initialLength = 6
	maxRetries    = 8
	baseURL       = "http://localhost:3000/"
	ttl           = 3 * 24 * time.Hour
)

var ctx = context.Background()

type URLService interface {
	Shorten(url, requestedShortURL string) (string, error)
	Resolve(shortURL string) (string, error)
}

type urlService struct {
	repo  repository.URLRepository
	cache *redis.Client
	rng   *rand.Rand
	log   *slog.Logger
}

func NewURLService(repo repository.URLRepository, cache *redis.Client, log *slog.Logger) URLService {
	return &urlService{
		repo:  repo,
		cache: cache,
		rng:   rand.New(rand.NewSource(time.Now().UnixNano())),
		log:   log,
	}
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

func (s *urlService) generateShortURL(url string, length int) string {
	hash := sha256.Sum256([]byte(url + string(rune(s.rng.Int63()))))
	return base64.URLEncoding.EncodeToString(hash[:length])
}

func (s *urlService) Shorten(url, shortURL string) (string, error) {
	if shortURL != "" {
		err := s.repo.Create(&model.URL{ShortURL: shortURL, OriginalURL: url})
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

	length := initialLength
	for i := 0; i <= maxRetries; i++ {
		shortURL = s.generateShortURL(url, length)
		err := s.repo.Create(&model.URL{ShortURL: shortURL, OriginalURL: url})
		if err == nil {
			s.cache.Set(ctx, shortURL, url, ttl)
			s.log.Info("URL shortened", "short", shortURL, "original", url)
			return baseURL + shortURL, nil
		}
		if !isDuplicateKeyError(err) {
			return "", err
		}
		if i == maxRetries-1 {
			length++
		}
	}
	return "", errors.New("failed to generate a unique short URL after multiple attempts")
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
