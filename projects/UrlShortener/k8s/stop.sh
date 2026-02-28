#!/bin/bash
# stop.sh — Stop or tear down the Kubernetes deployment.
#
# Modes:
#   ./k8s/stop.sh              Scale all deployments to 0.
#                              Pods are removed but PVCs (disk data) are kept.
#                              Run deploy.sh again to bring everything back.
#
#   ./k8s/stop.sh --wipe       Delete the entire namespace.
#                              Removes all pods, services, AND persistent data.
#                              The next deploy.sh will start completely fresh.
#
#   ./k8s/stop.sh --minikube   Stop the Minikube VM entirely.
#                              Fastest way to free all CPU/RAM on your machine.
#                              Run 'minikube start' + deploy.sh to resume.

NS=urlshortener

case "$1" in
  --wipe)
    echo "Deleting namespace '$NS' — this removes ALL resources and data..."
    kubectl delete namespace "$NS" --ignore-not-found
    echo "Done. Run './k8s/deploy.sh' to start fresh."
    ;;

  --minikube)
    echo "Stopping the Minikube VM..."
    minikube stop
    echo "Done. Run 'minikube start' then './k8s/deploy.sh' to resume."
    ;;

  *)
    echo "Scaling all deployments to 0 (data is preserved in PVCs)..."
    kubectl scale deployment urlshortener-app postgres redis \
        --replicas=0 -n "$NS" 2>/dev/null || true
    echo ""
    echo "All pods stopped. Disk data is intact."
    echo "  Resume  : ./k8s/deploy.sh"
    echo "  Full wipe: ./k8s/stop.sh --wipe"
    echo "  Stop VM  : ./k8s/stop.sh --minikube"
    ;;
esac
