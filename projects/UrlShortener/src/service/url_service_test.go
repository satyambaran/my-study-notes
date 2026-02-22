package service

// White-box test: same package as the service so unexported helpers
// (encode, isDuplicateKeyError) can be tested directly.
// Zero external test dependencies — stdlib only.

import (
	"context"
	"errors"
	"log/slog"
	"os"
	"strings"
	"sync"
	"testing"
	"time"

	"github.com/go-redis/redis/v8"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/satyambaran/UrlShortener/src/model"
	"github.com/satyambaran/UrlShortener/src/repository"
	"gorm.io/gorm"
)

// ── stdlib assertion helpers ──────────────────────────────────────────────────

func assertEqual(t *testing.T, got, want interface{}) {
	t.Helper()
	if got != want {
		t.Errorf("got %v, want %v", got, want)
	}
}

func assertNotEqual(t *testing.T, a, b interface{}) {
	t.Helper()
	if a == b {
		t.Errorf("expected values to differ, both are %v", a)
	}
}

func assertTrue(t *testing.T, ok bool, msg string) {
	t.Helper()
	if !ok {
		t.Error(msg)
	}
}

func assertFalse(t *testing.T, ok bool, msg string) {
	t.Helper()
	if ok {
		t.Error(msg)
	}
}

func assertNoError(t *testing.T, err error) {
	t.Helper()
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
}

func assertError(t *testing.T, err error) {
	t.Helper()
	if err == nil {
		t.Fatal("expected an error, got nil")
	}
}

func assertContains(t *testing.T, s, sub string) {
	t.Helper()
	if !strings.Contains(s, sub) {
		t.Errorf("%q does not contain %q", s, sub)
	}
}

func assertNotContains(t *testing.T, s, sub string) {
	t.Helper()
	if strings.Contains(s, sub) {
		t.Errorf("%q should not contain %q", s, sub)
	}
}

// ── mock CacheClient ──────────────────────────────────────────────────────────
// Pure in-memory implementation — no real Redis server required.

type mockCache struct {
	mu      sync.Mutex
	strings map[string]string
	ints    map[string]int64
}

func newMockCache() *mockCache {
	return &mockCache{
		strings: make(map[string]string),
		ints:    make(map[string]int64),
	}
}

func (m *mockCache) Get(_ context.Context, key string) *redis.StringCmd {
	m.mu.Lock()
	defer m.mu.Unlock()
	cmd := redis.NewStringCmd(context.Background())
	if v, ok := m.strings[key]; ok {
		cmd.SetVal(v)
	} else {
		cmd.SetErr(redis.Nil)
	}
	return cmd
}

func (m *mockCache) Set(_ context.Context, key string, value interface{}, _ time.Duration) *redis.StatusCmd {
	m.mu.Lock()
	defer m.mu.Unlock()
	cmd := redis.NewStatusCmd(context.Background())
	m.strings[key] = value.(string)
	cmd.SetVal("OK")
	return cmd
}

func (m *mockCache) IncrBy(_ context.Context, key string, delta int64) *redis.IntCmd {
	m.mu.Lock()
	defer m.mu.Unlock()
	cmd := redis.NewIntCmd(context.Background())
	m.ints[key] += delta
	cmd.SetVal(m.ints[key])
	return cmd
}

// Seed pre-populates a key (simulates a cache that was already warmed).
func (m *mockCache) Seed(key, value string) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.strings[key] = value
}

var _ CacheClient = (*mockCache)(nil)

// ── mock URLRepository ────────────────────────────────────────────────────────

type mockRepo struct {
	mu      sync.RWMutex
	byShort map[string]*model.URL
	byOrig  map[string]*model.URL // non-alias entries only
}

func newMockRepo() *mockRepo {
	return &mockRepo{
		byShort: make(map[string]*model.URL),
		byOrig:  make(map[string]*model.URL),
	}
}

func (m *mockRepo) Create(u *model.URL) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	if _, exists := m.byShort[u.ShortURL]; exists {
		return &pgconn.PgError{Code: "23505"}
	}
	cp := *u
	m.byShort[u.ShortURL] = &cp
	if !u.IsCustomAlias {
		m.byOrig[u.OriginalURL] = &cp
	}
	return nil
}

func (m *mockRepo) FindByShortURL(shortURL string) (*model.URL, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()
	if u, ok := m.byShort[shortURL]; ok {
		return u, nil
	}
	return nil, gorm.ErrRecordNotFound
}

func (m *mockRepo) FindByOriginalURL(originalURL string) (*model.URL, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()
	if u, ok := m.byOrig[originalURL]; ok {
		return u, nil
	}
	return nil, gorm.ErrRecordNotFound
}

var _ repository.URLRepository = (*mockRepo)(nil)

// ── test factory ──────────────────────────────────────────────────────────────

func newTestService(t *testing.T, repo repository.URLRepository) (URLService, *mockCache) {
	t.Helper()
	cache := newMockCache()
	log := slog.New(slog.NewTextHandler(os.Stderr, &slog.HandlerOptions{Level: slog.LevelError}))
	return NewURLService(repo, cache, log), cache
}

// ── encode ────────────────────────────────────────────────────────────────────

func TestEncode(t *testing.T) {
	cases := []struct {
		n    int64
		want string
	}{
		{1, "1"},
		{9, "9"},
		{10, "a"},       // first lowercase letter (index 10)
		{35, "z"},       // last lowercase
		{36, "A"},       // first uppercase
		{61, "Z"},       // last symbol (index 61)
		{62, "10"},      // wraps into two digits
		{63, "11"},
		{1_000_000, "4c92"},
		{1_000_001, "4c93"},
	}
	for _, tc := range cases {
		got := encode(tc.n)
		if got != tc.want {
			t.Errorf("encode(%d) = %q, want %q", tc.n, got, tc.want)
		}
	}
}

// ── isDuplicateKeyError ───────────────────────────────────────────────────────

func TestIsDuplicateKeyError(t *testing.T) {
	assertTrue(t, isDuplicateKeyError(gorm.ErrDuplicatedKey), "gorm sentinel must be detected")
	assertTrue(t, isDuplicateKeyError(&pgconn.PgError{Code: "23505"}), "SQLSTATE 23505 must be detected")
	assertFalse(t, isDuplicateKeyError(&pgconn.PgError{Code: "23000"}), "other pg codes must not match")
	assertFalse(t, isDuplicateKeyError(errors.New("random")), "unrelated errors must not match")
	assertFalse(t, isDuplicateKeyError(nil), "nil must not match")
}

// ── Shorten ───────────────────────────────────────────────────────────────────

func TestShorten_AutoGenerate(t *testing.T) {
	svc, _ := newTestService(t, newMockRepo())

	result, err := svc.Shorten("https://example.com/page", "")
	assertNoError(t, err)
	assertTrue(t, result != "", "result must not be empty")
	assertTrue(t, strings.HasPrefix(result, "http"), "result must be a full URL")
}

// Same long URL sent twice without alias must return the identical short URL.
func TestShorten_SameURLIsIdempotent(t *testing.T) {
	svc, _ := newTestService(t, newMockRepo())
	url := "https://example.com/idempotency-check"

	first, err := svc.Shorten(url, "")
	assertNoError(t, err)

	second, err := svc.Shorten(url, "")
	assertNoError(t, err)

	assertEqual(t, first, second)
}

// Different long URLs must produce different short URLs.
func TestShorten_DifferentURLsAreUnique(t *testing.T) {
	svc, _ := newTestService(t, newMockRepo())

	urls := []string{
		"https://example.com/alpha",
		"https://example.com/beta",
		"https://example.com/gamma",
	}

	seen := make(map[string]bool)
	for _, u := range urls {
		result, err := svc.Shorten(u, "")
		assertNoError(t, err)
		assertFalse(t, seen[result], "short URL "+result+" was returned for two different long URLs")
		seen[result] = true
	}
}

func TestShorten_CustomAlias(t *testing.T) {
	svc, _ := newTestService(t, newMockRepo())

	result, err := svc.Shorten("https://example.com", "my-alias")
	assertNoError(t, err)
	assertContains(t, result, "my-alias")
}

// Taking an already-registered alias must return an error.
func TestShorten_DuplicateAlias(t *testing.T) {
	svc, _ := newTestService(t, newMockRepo())

	_, err := svc.Shorten("https://first.com", "taken")
	assertNoError(t, err)

	_, err = svc.Shorten("https://second.com", "taken")
	assertError(t, err)
	assertContains(t, err.Error(), "not available")
}

// A URL previously registered with a custom alias must get a brand-new
// auto-generated short URL when the same URL is submitted without an alias.
func TestShorten_AliasedURLGetsNewAutoURL(t *testing.T) {
	svc, _ := newTestService(t, newMockRepo())
	url := "https://example.com/shared"

	aliased, err := svc.Shorten(url, "cool-alias")
	assertNoError(t, err)

	auto, err := svc.Shorten(url, "")
	assertNoError(t, err)

	assertNotEqual(t, aliased, auto)
	assertNotContains(t, auto, "cool-alias")
}

// ── Resolve ───────────────────────────────────────────────────────────────────

// Short code present in the cache must be resolved without hitting the DB.
func TestResolve_CacheHit(t *testing.T) {
	svc, cache := newTestService(t, newMockRepo())
	cache.Seed("cached-code", "https://cached.example.com")

	result, err := svc.Resolve("cached-code")
	assertNoError(t, err)
	assertEqual(t, result, "https://cached.example.com")
}

// On a cache miss the service must fall back to the DB, then populate the
// cache so subsequent calls are hits.
func TestResolve_CacheMiss_FallsBackToRepo(t *testing.T) {
	repo := newMockRepo()
	repo.byShort["db-code"] = &model.URL{ShortURL: "db-code", OriginalURL: "https://db.example.com"}

	svc, cache := newTestService(t, repo)

	result, err := svc.Resolve("db-code")
	assertNoError(t, err)
	assertEqual(t, result, "https://db.example.com")

	// Cache should have been populated after the DB lookup.
	cached, cacheErr := cache.Get(context.Background(), "db-code").Result()
	assertNoError(t, cacheErr)
	assertEqual(t, cached, "https://db.example.com")
}

func TestResolve_NotFound(t *testing.T) {
	svc, _ := newTestService(t, newMockRepo())

	_, err := svc.Resolve("does-not-exist")
	assertError(t, err)
	assertContains(t, err.Error(), "not found")
}

// Full round-trip: shorten a URL then resolve the short code back.
func TestShorten_ThenResolve_RoundTrip(t *testing.T) {
	svc, _ := newTestService(t, newMockRepo())
	original := "https://roundtrip.example.com/very/long/path"

	short, err := svc.Shorten(original, "")
	assertNoError(t, err)

	code := short[strings.LastIndex(short, "/")+1:]

	resolved, err := svc.Resolve(code)
	assertNoError(t, err)
	assertEqual(t, resolved, original)
}

// ── concurrency ───────────────────────────────────────────────────────────────

// Fifty goroutines shorten fifty different URLs concurrently.
// All resulting short URLs must be unique (exercises the mutex in nextID).
func TestShorten_ConcurrentUniqueness(t *testing.T) {
	svc, _ := newTestService(t, newMockRepo())

	const workers = 50
	results := make([]string, workers)
	var wg sync.WaitGroup

	for i := 0; i < workers; i++ {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			url := "https://concurrent.example.com/" + strings.Repeat("x", i+1)
			r, err := svc.Shorten(url, "")
			if err != nil {
				t.Errorf("goroutine %d: unexpected error: %v", i, err)
				return
			}
			results[i] = r
		}(i)
	}
	wg.Wait()

	seen := make(map[string]bool)
	for _, r := range results {
		assertFalse(t, seen[r], "duplicate short URL detected: "+r)
		seen[r] = true
	}
}
