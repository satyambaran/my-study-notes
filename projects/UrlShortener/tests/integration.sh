#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# integration.sh — End-to-end tests against a running URL Shortener server.
#
# Usage:
#   ./tests/integration.sh                     # default: http://localhost:3000
#   ./tests/integration.sh http://192.168.x.x:30300   # Kubernetes / Minikube
#
# Prerequisites:
#   • The server (and its DB + Redis) must be running before you run this.
#   • curl must be installed.
# ─────────────────────────────────────────────────────────────────────────────

BASE="${1:-http://localhost:3000}"
PASS=0
FAIL=0

# ── helpers ───────────────────────────────────────────────────────────────────

GREEN="\033[0;32m"
RED="\033[0;31m"
RESET="\033[0m"
BOLD="\033[1m"

header() { echo ""; echo -e "${BOLD}▶  $1${RESET}"; }
pass()   { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET}  $1"; }
fail()   { FAIL=$((FAIL+1)); echo -e "  ${RED}✗${RESET}  $1"; }

# post <path> <json_body> → prints response body, exits non-zero on curl error
post() { curl -sf -X POST "${BASE}$1" -H "Content-Type: application/json" -d "$2"; }

# get_status <url> → prints HTTP status code only
get_status() { curl -so /dev/null -w "%{http_code}" "$1"; }

# get_redirect <short_url_path> → prints the Location header value
get_redirect() { curl -si "${BASE}$1" | grep -i "^location:" | tr -d '\r' | awk '{print $2}'; }

# json_field <json> <field> → extracts a quoted string field from JSON without jq
json_field() { echo "$1" | grep -o "\"$2\":\"[^\"]*\"" | cut -d'"' -f4; }

# ── connectivity check ────────────────────────────────────────────────────────

header "Connectivity"
STATUS=$(curl -so /dev/null -w "%{http_code}" "${BASE}/health-check-404-is-fine" 2>/dev/null || echo "000")
if [ "$STATUS" = "000" ]; then
    echo -e "${RED}ERROR: Cannot reach ${BASE}${RESET}"
    echo "       Start the server first: ./start.sh  or  ./k8s/deploy.sh"
    exit 1
fi
pass "Server is reachable at ${BASE}"

# ── 1. Shorten a new URL ──────────────────────────────────────────────────────

header "1. Shorten a new URL"
RESP=$(post /shorten '{"url":"https://example.com/very/long/path"}')
SHORT1=$(json_field "$RESP" "short_url")
if [ -n "$SHORT1" ]; then
    pass "Got short URL: $SHORT1"
else
    fail "No short_url in response: $RESP"
fi

# ── 2. Same URL → same short URL (idempotency) ───────────────────────────────

header "2. Same URL must return the same short URL (idempotency)"
RESP2=$(post /shorten '{"url":"https://example.com/very/long/path"}')
SHORT2=$(json_field "$RESP2" "short_url")
if [ "$SHORT1" = "$SHORT2" ]; then
    pass "Idempotent: $SHORT2"
else
    fail "Expected $SHORT1, got $SHORT2"
fi

# ── 3. Different URL → different short URL ────────────────────────────────────

header "3. Different URL must produce a different short URL"
RESP3=$(post /shorten '{"url":"https://other.com/completely/different"}')
SHORT3=$(json_field "$RESP3" "short_url")
if [ "$SHORT1" != "$SHORT3" ] && [ -n "$SHORT3" ]; then
    pass "Unique: $SHORT3"
else
    fail "Expected a different short URL, got: $SHORT3"
fi

# ── 4. Custom alias ───────────────────────────────────────────────────────────

header "4. Custom alias"
ALIAS="my-test-alias-$$"
RESP4=$(post /shorten "{\"url\":\"https://alias-test.com\",\"requested_url\":\"$ALIAS\"}")
SHORT4=$(json_field "$RESP4" "short_url")
if echo "$SHORT4" | grep -q "$ALIAS"; then
    pass "Alias accepted: $SHORT4"
else
    fail "Alias not in response: $RESP4"
fi

# ── 5. Duplicate alias → error ────────────────────────────────────────────────

header "5. Duplicate alias must be rejected"
RESP5=$(post /shorten "{\"url\":\"https://other-site.com\",\"requested_url\":\"$ALIAS\"}" 2>/dev/null || true)
ERR5=$(json_field "$RESP5" "error")
if [ -n "$ERR5" ]; then
    pass "Rejected with: $ERR5"
else
    fail "Expected error for duplicate alias, got: $RESP5"
fi

# ── 6. Resolve → redirect ─────────────────────────────────────────────────────

header "6. Resolving a short code must redirect to the original URL"
CODE1="${SHORT1##*/}"   # strip everything up to the last /
LOCATION=$(get_redirect "/$CODE1")
if [ "$LOCATION" = "https://example.com/very/long/path" ]; then
    pass "Redirects to: $LOCATION"
else
    fail "Expected redirect to https://example.com/very/long/path, got: '$LOCATION'"
fi

# ── 7. Resolve unknown code → 404 ────────────────────────────────────────────

header "7. Unknown short code must return 404"
STATUS7=$(get_status "${BASE}/definitely-does-not-exist-xyz123")
if [ "$STATUS7" = "404" ]; then
    pass "404 returned"
else
    fail "Expected 404, got $STATUS7"
fi

# ── 8. URL with alias → auto-generated is separate ───────────────────────────

header "8. URL registered with alias must still get a separate auto-generated short URL"
URL8="https://alias-and-auto.example.com"
ALIAS8="fixed-alias-$$"
# Register with alias first
post /shorten "{\"url\":\"$URL8\",\"requested_url\":\"$ALIAS8\"}" > /dev/null

# Now register same URL without alias
RESP8=$(post /shorten "{\"url\":\"$URL8\"}")
SHORT8=$(json_field "$RESP8" "short_url")
if [ -n "$SHORT8" ] && ! echo "$SHORT8" | grep -q "$ALIAS8"; then
    pass "Auto-generated URL ($SHORT8) is distinct from alias"
else
    fail "Expected a distinct auto-generated URL, got: $SHORT8"
fi

# ── summary ───────────────────────────────────────────────────────────────────

echo ""
echo "─────────────────────────────────"
TOTAL=$((PASS+FAIL))
echo -e "  ${BOLD}Results: ${GREEN}${PASS} passed${RESET}, ${RED}${FAIL} failed${RESET} (${TOTAL} total)"
echo "─────────────────────────────────"
echo ""

[ "$FAIL" -eq 0 ]   # exit 0 on all-pass, non-zero on any failure
