#!/usr/bin/env bash
# ================================================================
#  demo.sh — Convenience script for the Kubernetes hands-on lab
#  Usage:
#    ./scripts/demo.sh build      — Build Docker image
#    ./scripts/demo.sh deploy     — Apply all manifests
#    ./scripts/demo.sh open       — Port-forward and open app
#    ./scripts/demo.sh test       — Run all endpoint tests
#    ./scripts/demo.sh autoheal   — Demo auto-heal by deleting a pod
#    ./scripts/demo.sh load       — Generate CPU load to trigger HPA
#    ./scripts/demo.sh status     — Show full cluster status
#    ./scripts/demo.sh clean      — Delete all demo resources
# ================================================================

set -e

NS="k8s-demo"
DEPLOY="k8s-demo-deployment"
SVC="demo-service"
BASE_URL="http://localhost:8080"
PF_PID=""

header() { echo; echo "=== $1 ==="; echo; }

# ── Build ─────────────────────────────────────────────────────
build() {
  header "Building Docker image k8s-demo:1.0.0"
  docker build -t k8s-demo:1.0.0 .
  echo "Loading image into Minikube..."
  minikube image load k8s-demo:1.0.0
  echo "Done! Image loaded."
}

# ── Deploy ────────────────────────────────────────────────────
deploy() {
  header "Deploying all Kubernetes manifests"
  kubectl apply -f k8s/01-namespace.yml
  kubectl apply -f k8s/02-configmap.yml
  kubectl apply -f k8s/03-secret.yml
  kubectl apply -f k8s/06-persistent-volume.yml
  kubectl apply -f k8s/04-deployment.yml
  kubectl apply -f k8s/05-service.yml
  kubectl apply -f k8s/07-hpa.yml

  header "Waiting for deployment to be ready..."
  kubectl rollout status deployment/$DEPLOY -n $NS --timeout=120s

  header "All resources:"
  kubectl get all -n $NS
}

# ── Port Forward ──────────────────────────────────────────────
start_pf() {
  echo "Starting port-forward on :8080..."
  kubectl port-forward svc/$SVC 8080:80 -n $NS &>/dev/null &
  PF_PID=$!
  sleep 2
}

stop_pf() {
  [ -n "$PF_PID" ] && kill $PF_PID 2>/dev/null || true
}

# ── Open ──────────────────────────────────────────────────────
open_app() {
  start_pf
  echo "App is running at $BASE_URL"
  echo "Endpoints:"
  echo "  GET $BASE_URL/hello"
  echo "  GET $BASE_URL/info"
  echo "  GET $BASE_URL/config"
  echo "  GET $BASE_URL/secret"
  echo "  GET $BASE_URL/counter"
  echo "  GET $BASE_URL/storage"
  echo "  GET $BASE_URL/load"
  echo "  GET $BASE_URL/actuator/health"
  echo ""
  echo "Press Ctrl+C to stop port-forward"
  wait $PF_PID
}

# ── Test ─────────────────────────────────────────────────────
test_endpoints() {
  start_pf
  trap stop_pf EXIT

  header "Testing all endpoints"

  echo "[1] /hello"
  curl -s $BASE_URL/hello | python3 -m json.tool
  echo

  echo "[2] /info"
  curl -s $BASE_URL/info | python3 -m json.tool
  echo

  echo "[3] /config (from ConfigMap)"
  curl -s $BASE_URL/config | python3 -m json.tool
  echo

  echo "[4] /secret (from Secret)"
  curl -s $BASE_URL/secret | python3 -m json.tool
  echo

  echo "[5] /counter (stateless — resets on pod restart)"
  for i in 1 2 3; do
    echo -n "  Request $i: "
    curl -s $BASE_URL/counter | python3 -m json.tool | grep -E '"count|podName"'
  done
  echo

  echo "[6] /storage (persistent volume demo)"
  curl -s $BASE_URL/storage | python3 -m json.tool
  echo

  echo "[7] /actuator/health"
  curl -s $BASE_URL/actuator/health | python3 -m json.tool
  echo

  echo "[8] Load balancing — 5 requests, watch podName rotate:"
  for i in {1..5}; do
    curl -s $BASE_URL/hello | python3 -m json.tool | grep podName
  done

  stop_pf
}

# ── Auto-Heal Demo ────────────────────────────────────────────
autoheal() {
  header "Auto-Heal Demo"
  echo "Current pods:"
  kubectl get pods -n $NS

  POD=$(kubectl get pods -n $NS -l app=k8s-demo -o jsonpath='{.items[0].metadata.name}')
  echo ""
  echo "Deleting pod: $POD"
  kubectl delete pod $POD -n $NS

  echo ""
  echo "Watch Kubernetes immediately replace it (Ctrl+C to stop):"
  kubectl get pods -n $NS -w
}

# ── Load / HPA Demo ───────────────────────────────────────────
load_test() {
  start_pf
  trap stop_pf EXIT

  header "HPA Load Test — hammering /load endpoint"
  echo "In another terminal run:"
  echo "  kubectl get hpa,pods -n $NS -w"
  echo ""
  echo "Sending load for 60 seconds..."

  END=$((SECONDS + 60))
  while [ $SECONDS -lt $END ]; do
    curl -s "$BASE_URL/load?iterations=200000" > /dev/null
  done

  stop_pf
  echo ""
  echo "Load stopped. HPA will scale down after 5 minutes."
  echo "Watch: kubectl get hpa -n $NS -w"
}

# ── Status ────────────────────────────────────────────────────
status() {
  header "Cluster Status"
  kubectl get nodes -o wide
  echo ""

  header "All Resources in $NS"
  kubectl get all -n $NS
  echo ""

  header "ConfigMaps & Secrets"
  kubectl get configmaps,secrets -n $NS
  echo ""

  header "PV & PVC"
  kubectl get pv,pvc -n $NS
  echo ""

  header "HPA"
  kubectl get hpa -n $NS
  echo ""

  header "Events (last 20)"
  kubectl get events -n $NS --sort-by='.lastTimestamp' | tail -20
}

# ── Clean ─────────────────────────────────────────────────────
clean() {
  header "Cleaning up all demo resources"
  kubectl delete namespace $NS --ignore-not-found
  kubectl delete pv demo-pv --ignore-not-found
  kubectl delete clusterrole demo-cluster-pod-reader --ignore-not-found
  kubectl delete clusterrolebinding demo-cluster-pod-reader-binding --ignore-not-found
  echo "Cleanup complete."
}

# ── Main ──────────────────────────────────────────────────────
case "${1:-help}" in
  build)     build ;;
  deploy)    deploy ;;
  open)      open_app ;;
  test)      test_endpoints ;;
  autoheal)  autoheal ;;
  load)      load_test ;;
  status)    status ;;
  clean)     clean ;;
  *)
    echo "Usage: $0 {build|deploy|open|test|autoheal|load|status|clean}"
    exit 1
    ;;
esac
