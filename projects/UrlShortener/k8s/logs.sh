#!/bin/bash
# logs.sh — Tail live logs from all running app pods simultaneously.
#
# --prefix      prints the pod name before each line so you can tell
#               which pod a log line came from (important when running
#               multiple replicas)
# --all-containers follows all containers in each pod (just 'app' for now)
#
# Usage:
#   ./k8s/logs.sh           — tail all app pods
#   ./k8s/logs.sh postgres  — tail the postgres pod
#   ./k8s/logs.sh redis     — tail the redis pod

NS=urlshortener
TARGET="${1:-urlshortener-app}"

echo "Tailing logs for label app=$TARGET in namespace '$NS' (Ctrl+C to stop)..."
echo ""
kubectl logs -n "$NS" -l "app=$TARGET" --all-containers -f --prefix
