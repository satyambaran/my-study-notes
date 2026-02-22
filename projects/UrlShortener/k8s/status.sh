#!/bin/bash
# status.sh — Show the live state of every resource in the urlshortener namespace.
NS=urlshortener

echo ""
echo "═══ PODS ════════════════════════════════════════════════════"
echo "(STATUS: Running=healthy, Pending=waiting, CrashLoop=problem)"
kubectl get pods -n "$NS" -o wide

echo ""
echo "═══ DEPLOYMENTS ═════════════════════════════════════════════"
echo "(READY shows current/desired replica count)"
kubectl get deployments -n "$NS"

echo ""
echo "═══ HPA  (autoscaler) ═══════════════════════════════════════"
echo "(TARGETS shows current CPU% / threshold. Scales up when over threshold.)"
kubectl get hpa -n "$NS"

echo ""
echo "═══ SERVICES ════════════════════════════════════════════════"
echo "(NodePort services are reachable from outside the cluster)"
kubectl get services -n "$NS"

echo ""
echo "═══ PERSISTENT VOLUME CLAIMS ════════════════════════════════"
echo "(Bound = disk is allocated and attached)"
kubectl get pvc -n "$NS"

echo ""
echo "═══ JOBS ════════════════════════════════════════════════════"
kubectl get jobs -n "$NS"

echo ""
APP_URL="http://$(minikube ip):30300/"
echo "App is reachable at: $APP_URL"
echo ""
