#!/bin/bash
# scale.sh — Manually override the replica count for the app.
#
# The HPA will continue to auto-scale after this, so think of a manual
# scale as just giving the HPA a new starting point.
#
# Usage:
#   ./k8s/scale.sh <replica-count>
#
# Examples:
#   ./k8s/scale.sh 1    — scale down to 1 pod (minimum)
#   ./k8s/scale.sh 5    — scale up to 5 pods
#   ./k8s/scale.sh 0    — stop the app (pods removed, data kept in PVCs)

NS=urlshortener

if [ -z "$1" ]; then
    echo "Usage: ./k8s/scale.sh <replica-count>"
    echo ""
    echo "Current replicas:"
    kubectl get deployment urlshortener-app -n "$NS" \
        -o jsonpath='  desired={.spec.replicas}  ready={.status.readyReplicas}{"\n"}'
    exit 1
fi

echo "Scaling urlshortener-app to $1 replica(s)..."
kubectl scale deployment urlshortener-app -n "$NS" --replicas="$1"

echo ""
echo "Watching pods (Ctrl+C to stop watching):"
kubectl get pods -n "$NS" -l app=urlshortener-app -w
