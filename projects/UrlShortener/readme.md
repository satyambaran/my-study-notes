# UrlShortener

A URL shortening service built with Go, PostgreSQL, and Redis.

## Tech Stack

- **Go** + [Fiber v2](https://github.com/gofiber/fiber) — HTTP server
- **PostgreSQL** — persistent URL storage (via GORM)
- **Redis** — caching layer with LFU eviction and 3-day TTL

## Prerequisites

- Go 1.21+
- Docker and Docker Compose

## Running Locally

### 1. Start the dependencies (PostgreSQL + Redis)

```bash
docker compose up -d db redis
```

### 2. Run the server

```bash
go run main.go
```

The server starts at `http://localhost:3000`.

Or use the provided script (starts deps + server in one step):

```bash
chmod +x ./start.sh
./start.sh
```

### Environment Variables

The app reads from a `.env` file in the project root. The defaults work out of the box with the Docker Compose setup:

| Variable    | Default                                             | Description           |
|-------------|-----------------------------------------------------|-----------------------|
| `DB_URL`    | `postgresql://postgres:password@localhost:5432/db`  | PostgreSQL connection |
| `REDIS_URL` | `localhost:6379`                                    | Redis address         |

## API

### Shorten a URL (auto-generated alias)

```bash
curl -X POST http://localhost:3000/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/very/long/url"}'
```

**Response:**
```json
{ "short_url": "http://localhost:3000/aB3xYz12" }
```

---

### Shorten a URL (custom alias)

```bash
curl -X POST http://localhost:3000/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/very/long/url", "requested_url": "my-alias"}'
```

**Response:**
```json
{ "short_url": "http://localhost:3000/my-alias" }
```

**Error — alias already taken:**
```json
{ "error": "requested url is not available" }
```

---

### Redirect to original URL

```bash
curl -L http://localhost:3000/my-alias
```

`-L` follows the redirect. Without it, curl shows the `301` response headers only:

```bash
curl -v http://localhost:3000/my-alias
```

```
< HTTP/1.1 301 Moved Permanently
< Location: https://example.com/very/long/url
```

**Error — short URL not found:**
```json
{ "error": "URL not found" }
```

---

## How it works

1. **Shortening** — SHA-256 hash of `(url + random nonce)`, base64-URL-encoded. Starts at 6 bytes (8 chars). On collision, retries up to 8 times; after 7 retries it increases the length by 1 byte to reduce collision probability.
2. **Caching** — every resolved URL is cached in Redis with a 3-day TTL and LFU eviction policy.
3. **Fallback** — on a cache miss, the app queries PostgreSQL and re-populates the cache.

## To connect with postgres and redis servers
```bash
brew install libpq redis

psql "postgresql://postgres:password@localhost:5432/db"
redis-cli -h localhost -p 6379 -a password
```

## To Kill:
```bash
lsof -i :3000
kill -9 <PID>

docker ps -a
docker kill redis
docker kill db
```

## Revamp

What changed and why

| File | Role | Notes |
|------|------|-------|
| `config/config.go` | Config struct | A single `Config` struct loaded once at startup from env. All other packages receive what they need via constructor args — no package reads env vars directly except `config`. |
| `logger/logger.go` | Singleton `slog.Logger` | Uses `sync.Once` so the exact same `*slog.Logger` instance is returned no matter how many times `Get()` is called. Outputs structured JSON (good for log aggregation). Uses Go 1.21's built-in `log/slog` — no new dependency. |
| `database/database.go` | Singleton GORM connection | `Connect(dsn)` uses `sync.Once` — GORM's `Open()` runs exactly once regardless of how many times `Connect` is called. `Get()` returns the shared `*gorm.DB`. `AutoMigrate` stays in `main.go` (it's a startup concern, not a DB concern). |
| `cache/cache.go` | Singleton Redis connection | Same `sync.Once` pattern. `setEvictionPolicy` now uses the structured logger instead of `log.Fatal`, so a misconfigured eviction policy is a warning, not a crash. |
| `model/url.go` | Data model | Pure data — just the GORM struct, no logic. |
| `repository/url_repository.go` | Data access | Interface + concrete struct. Only speaks to the DB. No business logic. |
| `service/url_service.go` | Business logic | All business logic lives here. Depends on `URLRepository` interface (not the concrete struct) — easy to test/mock. Logs key events (shorten, cache hit/miss). |
| `controller/url_controller.go` | HTTP handler | Only parses HTTP request → calls service → writes HTTP response. Zero business logic. |
| `main.go` | Thin entry point | Startup order: load env → load config → init logger → init DB singleton → init Redis singleton → wire repo → wire service → wire controller → start server. |