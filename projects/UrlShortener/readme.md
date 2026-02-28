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

chmod +x ./stop.sh
./stop.sh
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

curl -X POST http://localhost:3000/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/very/long/url1"}'

curl -X POST http://localhost:3000/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/very/long/url2"}'

curl -X POST http://localhost:3000/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/very/long/url3"}'
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
curl -X POST http://localhost:3000/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/very/long/url1", "requested_url": "my-alias"}'
curl -X POST http://localhost:3000/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/very/long/url", "requested_url": "my-alias-1"}'
curl -X POST http://localhost:3000/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/very/long/url", "requested_url": "my-alias-2"}'
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

## Fixes
- Change 1 — Idempotent short URLs for alias-free registrations

  `model/url.go`
  - Added IsCustomAlias bool field. 
  - GORM's AutoMigrate (already called in main.go) will add the column automatically.

  `repository/url_repository.go` 
  - Added FindByOriginalURL method that queries WHERE original_url = ? AND is_custom_alias = false. 
  - This intentionally excludes URLs registered with a custom alias, so they're treated as non-existent for auto-generation purposes.
  
  `service/url_service.go` (idempotency part):
  - Alias path sets IsCustomAlias: true on the created record.
  - No-alias path now calls FindByOriginalURL first. 
    - If a match is found, it returns the existing short URL immediately (also refreshing the cache). 
    - Only if no match is found does it proceed to generate a new one.


- Change 2 — Fix inconsistent short URL length/format

  Root cause: base64.URLEncoding.EncodeToString(hash[:length]) slices bytes before encoding, so:
  - 6 bytes → 8 chars (no padding)
  - 7 bytes → 12 chars (== padding) ← ugly jump, = in URLs

  Fix: Encode the full 32-byte hash first, then take the first length characters of the encoded string:
  ```go
    encoded := base64.RawURLEncoding.EncodeToString(hash[:])
    return encoded[:length]
  ```

  RawURLEncoding never emits `=` padding. With initialLength = 8 the output is always exactly 8 clean chars (A-Z a-z 0-9 - _). When the length increments on collision, it grows by 1 char at a time (9, 10, …) instead of jumping from 8 to 12.

## Moving towards making it scalable
How the range allocation works

|  Server A starts         |  Server B starts       |
|--------------------------|------------------------|
|    INCRBY counter 1000   |    INCRBY counter 1000 |
|    ← gets 1000           |    ← gets 2000         |
|    range: [1, 1000]      |    range: [1001, 2000] |

INCRBY is atomic in Redis — two servers can never claim the same range. When a server exhausts its block it calls INCRBY again to get the next one. If a server
  crashes with IDs left in its block, those IDs are simply skipped; there's no ambiguity.

### Changes

`service/url_service.go`:
- Removed crypto/sha256, encoding/base64, math/rand, and the rng field — no longer needed.
- Added sync.Mutex, currentID int64, rangeEnd int64 to the struct.
- claimRange(): calls INCRBY shortener:id_counter 1000 → gets the top of the newly claimed block, sets [currentID, rangeEnd].
- nextID(): mutex-protected; claims a new range from Redis whenever the current block is exhausted.
- encode(n): base62 encoding — digits → lowercase → uppercase. encode(1) = "1", encode(62) = "10", encode(238327) = "zzz". No fixed width, naturally grows as the ID space fills up.
- Shorten no-alias path: look up existing → if none, get a unique ID → encode → insert. No retry loop needed — uniqueness is guaranteed by the range allocation.

`cache/cache.go`
- Changed eviction policy from allkeys-lfu → volatile-lfu. With volatile-lfu, only keys with a TTL (the URL cache entries, set with 3 * 24h) are candidates for eviction. The shortener:id_counter key has no TTL and is therefore immune to eviction.

`docker-compose.yml`
- Added `--appendonly yes` to the Redis command. AOF (Append-Only File) writes every write operation to disk, so the counter value is durable across Redis container restarts. Without this, a Redis restart would reset the counter to 0 and IDs would repeat.
`stop.sh`
- Normal stop — Redis remembers everything (counter, cached URLs)
  ```bash
  ./stop.sh 
  ```                                                                                     
- One-time wipe — Redis starts from scratch on next ./start.sh  
  ```bash                                                                          
  ./stop.sh --fresh-redis  
  ```                                                                                                               
                                                                                                           
## Implementing kubernetes
### How to use it
- First time (or after code changes)
  ```bash
  ./k8s/deploy.sh
  ```
- Check what's running
  ```
  ./k8s/status.sh
  ```
- Watch logs from all app pods at once
  ```
  ./k8s/logs.sh
  ```
- Manually set replica count (HPA will still auto-scale after)
  ```
  ./k8s/scale.sh 5
  ```
- Stop pods but keep data on disk
  ```
  ./k8s/stop.sh
  ```
- Delete everything including database data
  ```
  ./k8s/stop.sh --wipe
  ```
- Stop the Minikube VM entirely (frees all RAM/CPU)
  ```
  ./k8s/stop.sh --minikube
  ```

  ---
### Manifests (applied in numbered order):

- HPA (autoscaler): watches average CPU across all app pods. If it exceeds 50% of the requested 100m (i.e. each pod averages >50m CPU), Kubernetes spawns more pods — up to 10. It also scales back down when load drops. `deploy.sh` enables metrics-server which is the Minikube addon that feeds real CPU metrics to the HPA.

- BASE_URL fix: deploy.sh computes http://<minikube-ip>:30300/ after the service is created and patches it into the deployment with kubectl set env. This triggers a zero-downtime rolling restart so all pods serve correct short URLs.

- `service/url_service.go`: baseURL is now read from config using $BASE_URL env var, falling back to localhost:3000/ for local Docker Compose dev

- Kuberenetes yml files
  |          File          |                       What it creates                        |
  |------------------------|--------------------------------------------------------------|
  | 00-namespace.yaml      | An isolated urlshortener namespace — all resources live here |
  | 01-secret.yaml         | Passwords and the DB connection string (never in app code)   |
  | 02-postgres.yaml       | PVC (1 GiB disk) + Deployment + ClusterIP Service            |
  | 03-redis.yaml          | PVC (256 MiB disk) + Deployment + ClusterIP Service          |
  | 04-redis-init-job.yaml | One-shot Job: SET shortener:id_counter 1000000 NX            |
  | 05-app.yaml            | Deployment (2 → 10 pods) + NodePort Service on :30300 + HPA  |

