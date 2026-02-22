# Testing Guide

## Quick reference

| What | Command |
|------|---------|
| Unit tests (no server needed) | `go test ./...` or `make unit` |
| Integration tests (server must be running) | `./tests/integration.sh` or `make integration` |
| Both | `make test` |

---

## 1. Unit tests

Unit tests live alongside the package they test (`service/url_service_test.go`).
They use in-memory mocks for both the database and Redis — **no running server
or infrastructure needed**.

```bash
go test ./... -v
```

What is covered:

| Test | What it checks |
|------|---------------|
| `TestEncode` | base62 encoding math for specific known values |
| `TestIsDuplicateKeyError` | correctly identifies Postgres SQLSTATE 23505 |
| `TestShorten_AutoGenerate` | returns a non-empty full URL |
| `TestShorten_SameURLIsIdempotent` | same long URL always gets the same short URL |
| `TestShorten_DifferentURLsAreUnique` | different long URLs get different short URLs |
| `TestShorten_CustomAlias` | custom alias appears in the returned URL |
| `TestShorten_DuplicateAlias` | taking an existing alias returns an error |
| `TestShorten_AliasedURLGetsNewAutoURL` | alias-registered URL still gets a fresh auto short URL |
| `TestResolve_CacheHit` | resolves from cache without touching the DB |
| `TestResolve_CacheMiss_FallsBackToRepo` | falls back to DB on cache miss, then warms the cache |
| `TestResolve_NotFound` | unknown short code returns an error |
| `TestShorten_ThenResolve_RoundTrip` | shorten → resolve returns the original URL |
| `TestShorten_ConcurrentUniqueness` | 50 goroutines get 50 unique short URLs (mutex test) |

---

## 2. Integration tests (against a live server)

Start the server first (pick one):

```bash
# Local dev (Docker Compose)
./start.sh

# Kubernetes / Minikube
./k8s/deploy.sh
```

Then run:

```bash
# Against local server (default)
./tests/integration.sh

# Against Kubernetes / Minikube
./tests/integration.sh http://$(minikube ip):30300
```

Or via Make:

```bash
make integration                               # localhost:3000
make integration BASE=http://192.168.x.x:30300 # custom address
```

---

## 3. Manual testing with curl

### Shorten a URL (auto-generated short code)

```bash
curl -X POST http://localhost:3000/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/very/long/path"}'
```

Expected response:

```json
{"short_url":"http://localhost:3000/4c93"}
```

### Same URL → same short code (idempotency)

```bash
# Run the above command twice — the short_url must be identical both times.
```

### Shorten with a custom alias

```bash
curl -X POST http://localhost:3000/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com", "requested_url": "my-alias"}'
```

Expected response:

```json
{"short_url":"http://localhost:3000/my-alias"}
```

### Duplicate alias → error

```bash
# Run the custom alias command above a second time.
```

Expected response:

```json
{"error":"requested url is not available"}
```

### Resolve (follow the redirect)

```bash
curl -L http://localhost:3000/4c93
# Browser / curl follows the redirect to https://example.com/very/long/path
```

### Resolve without following the redirect (inspect headers)

```bash
curl -i http://localhost:3000/4c93
# Look for: HTTP/1.1 302 Found
#           Location: https://example.com/very/long/path
```

### Unknown short code → 404

```bash
curl -i http://localhost:3000/doesnotexist
# Expected: HTTP/1.1 404 Not Found
#           {"error":"URL not found"}
```

### URL with alias → still gets a separate auto-generated URL

```bash
# 1. Register with alias
curl -X POST http://localhost:3000/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://shared.com", "requested_url": "shared-alias"}'

# 2. Register same URL without alias → must get a NEW short code, not the alias
curl -X POST http://localhost:3000/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://shared.com"}'
```

---

## 4. Load testing (verify autoscaling in Kubernetes)

Install [hey](https://github.com/rakyll/hey) or [wrk](https://github.com/wg/wrk), then hammer the shorten endpoint:

```bash
# Install hey
go install github.com/rakyll/hey@latest

# 200 concurrent users, 10 000 total requests
hey -c 200 -n 10000 \
    -m POST \
    -H "Content-Type: application/json" \
    -d '{"url":"https://load-test.example.com/path"}' \
    http://$(minikube ip):30300/shorten
```

Watch the HPA react in another terminal:

```bash
# Live HPA status (updates every 5 s)
watch -n5 kubectl get hpa -n urlshortener

# Or use the status script
./k8s/status.sh
```

You should see `REPLICAS` climb as CPU crosses 50 % of the requested 100m,
and drop back down once load subsides.
