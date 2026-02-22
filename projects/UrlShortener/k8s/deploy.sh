#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# deploy.sh — Build the app image and deploy everything to Minikube.
#
# What it does, step by step:
#   1. Ensure Minikube is installed and running
#   2. Enable metrics-server (required for the HPA autoscaler)
#   3. Point the local Docker CLI at Minikube's internal Docker daemon
#      so the image we build is immediately visible to Kubernetes — no
#      external registry needed
#   4. Build the Go app image
#   5. Apply the namespace + secrets
#   6. Deploy Postgres, wait until it is accepting connections
#   7. Deploy Redis, wait until it is ready
#   8. Run the redis-init Job to seed the ID counter at 1,000,000
#   9. Deploy the app (2 replicas to start) + Service + HPA
#  10. Patch BASE_URL so generated short URLs point to the real service address
#  11. Print the URL and a quick-reference cheat sheet
# ─────────────────────────────────────────────────────────────────────────────
set -e

NS=urlshortener
IMAGE=urlshortener:latest
NODE_PORT=30300          # must match the nodePort in 05-app.yaml

# ── helpers ──────────────────────────────────────────────────────────────────
header() { echo ""; echo "▶  $1"; echo "────────────────────────────────────"; }
ok()     { echo "✓  $1"; }

# Run this script from the repository root
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

# ── Step 1: Minikube ──────────────────────────────────────────────────────────
header "Checking Minikube"
if ! command -v minikube &>/dev/null; then
    echo "ERROR: minikube is not installed."
    echo "       Install it from https://minikube.sigs.k8s.io/docs/start/"
    exit 1
fi

if ! minikube status | grep -q "Running"; then
    echo "Minikube is not running — starting it now..."
    minikube start
fi
ok "Minikube is running"

# ── Step 2: metrics-server (needed for HPA) ───────────────────────────────────
header "Enabling metrics-server addon"
minikube addons enable metrics-server
ok "metrics-server enabled"

# ── Step 3: Point Docker at Minikube's daemon ─────────────────────────────────
header "Configuring Docker to use Minikube's daemon"
# eval exports env vars into the current shell so 'docker build' below
# builds the image inside Minikube, making it visible to Kubernetes
# without pushing to any external registry.
eval "$(minikube docker-env)"
ok "Docker now talks to Minikube's daemon"

# ── Step 4: Build the app image ───────────────────────────────────────────────
header "Building app image: $IMAGE"
docker build -t "$IMAGE" .
ok "Image built"

# ── Step 5: Namespace + Secrets ───────────────────────────────────────────────
header "Applying namespace and secrets"
kubectl apply -f k8s/manifests/00-namespace.yaml
kubectl apply -f k8s/manifests/01-secret.yaml
ok "Namespace 'urlshortener' and secrets applied"

# ── Step 6: Postgres ──────────────────────────────────────────────────────────
header "Deploying Postgres"
kubectl apply -f k8s/manifests/02-postgres.yaml
echo "Waiting for Postgres pod to be ready..."
kubectl rollout status deployment/postgres -n "$NS" --timeout=120s
ok "Postgres is ready"

# ── Step 7: Redis ─────────────────────────────────────────────────────────────
header "Deploying Redis"
kubectl apply -f k8s/manifests/03-redis.yaml
echo "Waiting for Redis pod to be ready..."
kubectl rollout status deployment/redis -n "$NS" --timeout=120s
ok "Redis is ready"

# ── Step 8: Seed the ID counter ───────────────────────────────────────────────
header "Seeding Redis ID counter (SET NX 1,000,000)"
# Always delete + recreate the Job so it actually runs on re-deploys.
# The SET NX inside is idempotent — it won't overwrite a live counter.
kubectl delete job redis-init -n "$NS" --ignore-not-found
kubectl apply -f k8s/manifests/04-redis-init-job.yaml
echo "Waiting for redis-init Job to complete..."
kubectl wait --for=condition=complete job/redis-init -n "$NS" --timeout=60s
ok "Counter seeded"

# ── Step 9: App ───────────────────────────────────────────────────────────────
header "Deploying URL Shortener app"
kubectl apply -f k8s/manifests/05-app.yaml
echo "Waiting for app pods to be ready..."
kubectl rollout status deployment/urlshortener-app -n "$NS" --timeout=120s
ok "App is running"

# ── Step 10: Patch BASE_URL ───────────────────────────────────────────────────
header "Setting BASE_URL"
MINIKUBE_IP=$(minikube ip)
BASE_URL="http://${MINIKUBE_IP}:${NODE_PORT}/"
kubectl set env deployment/urlshortener-app BASE_URL="$BASE_URL" -n "$NS"
echo "Waiting for rolling restart to finish..."
kubectl rollout status deployment/urlshortener-app -n "$NS" --timeout=60s
ok "BASE_URL set to $BASE_URL"

# ── Step 11: Summary ──────────────────────────────────────────────────────────
echo ""
echo "════════════════════════════════════════"
echo "  Deployment complete!"
echo "════════════════════════════════════════"
echo ""
echo "  App URL : $BASE_URL"
echo ""
echo "  Quick test:"
echo "    curl -X POST ${BASE_URL}shorten \\"
echo "         -H 'Content-Type: application/json' \\"
echo "         -d '{\"url\": \"https://example.com/very/long/url\"}'"
echo ""
echo "  Cheat sheet:"
echo "    ./k8s/status.sh               — pods, services, HPA state"
echo "    ./k8s/scale.sh <n>            — manually set replica count"
echo "    ./k8s/logs.sh                 — tail logs from all app pods"
echo "    ./k8s/stop.sh                 — scale everything to 0 (data kept)"
echo "    ./k8s/stop.sh --wipe          — delete namespace + all data"
echo "    ./k8s/stop.sh --minikube      — stop Minikube VM entirely"
echo ""
