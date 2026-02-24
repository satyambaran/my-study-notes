# Kubernetes Hands-On Learning Guide
### A complete tour of Kubernetes using a real Java Spring Boot application

---

## Table of Contents

1. [What is Kubernetes?](#1-what-is-kubernetes)
2. [Architecture Deep Dive](#2-architecture-deep-dive)
   - [Control Plane — The Brain](#21-control-plane--the-brain)
   - [Worker Nodes](#22-worker-nodes)
   - [Networking Model](#23-networking-model)
3. [Project Structure](#3-project-structure)
4. [Setup & Prerequisites](#4-setup--prerequisites)
5. [Build & Deploy](#5-build--deploy)
6. [Kubernetes Objects — Hands-On](#6-kubernetes-objects--hands-on)
   - [Namespace](#61-namespace)
   - [Pod](#62-pod)
   - [Deployment & ReplicaSet](#63-deployment--replicaset)
   - [Service](#64-service)
   - [ConfigMap](#65-configmap)
   - [Secret](#66-secret)
   - [Persistent Volume & PVC](#67-persistent-volume--pvc)
   - [HPA — Auto-Scale](#68-hpa--auto-scale)
   - [StatefulSet](#69-statefulset)
   - [DaemonSet](#610-daemonset)
   - [Job & CronJob](#611-job--cronjob)
   - [Ingress](#612-ingress)
   - [NetworkPolicy](#613-networkpolicy)
   - [RBAC](#614-rbac)
7. [Auto-Heal Demo](#7-auto-heal-demo)
8. [Rolling Updates & Rollback](#8-rolling-updates--rollback)
9. [Debugging & Troubleshooting](#9-debugging--troubleshooting)
10. [Essential kubectl Cheatsheet](#10-essential-kubectl-cheatsheet)

---

## 1. What is Kubernetes?

Kubernetes (K8s) is an **open-source container orchestration platform** that automates:

- **Deployment** — place containers on the right machines
- **Scaling** — add/remove containers based on load
- **Self-healing** — restart crashed containers, replace failed nodes
- **Service discovery** — containers find each other by name, not IP
- **Configuration management** — inject config and secrets without rebuilding images
- **Rolling updates** — deploy new versions with zero downtime

The fundamental promise of Kubernetes is **desired state reconciliation**:
> You describe WHAT you want (e.g., "3 replicas of my app"). Kubernetes figures out HOW to make it happen and continuously drives the cluster toward that state.

---

## 2. Architecture Deep Dive

```
┌─────────────────────────────────────────────────────────────────────┐
│                        KUBERNETES CLUSTER                           │
│                                                                     │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │                    CONTROL PLANE (Master)                      │ │
│  │                                                                │ │
│  │  ┌──────────────┐  ┌─────────────┐  ┌───────────────────────┐  │ │
│  │  │  API Server  │  │    etcd     │  │  Controller Manager   │  │ │
│  │  │  (front door)│  │  (database) │  │  (reconcile loops)    │  │ │
│  │  └──────┬───────┘  └─────────────┘  └───────────────────────┘  │ │
│  │         │                                                      │ │
│  │  ┌──────┴───────┐  ┌────────────────────────────────────────┐  │ │
│  │  │  Scheduler   │  │  Cloud Controller Manager (optional).  │  │ │
│  │  │  (placement) │  │  (LBs, node lifecycle, routes)         │  │ │
│  │  └──────────────┘  └────────────────────────────────────────┘  │ │
│  └────────────────────────────────────────────────────────────────┘ │
│                                                                     │
│      ┌───────────────────────┐       ┌───────────────────────┐      │
│      │    WORKER NODE 1      │       │    WORKER NODE 2      │      │
│      │                       │       │                       │      │
│      │  ┌─────────────────┐  │       │  ┌─────────────────┐  │      │
│      │  │    kubelet      │  │       │  │    kubelet      │  │      │
│      │  │  (node agent)   │  │       │  │  (node agent)   │  │      │
│      │  └─────────────────┘  │       │  └─────────────────┘  │      │
│      │  ┌─────────────────┐  │       │  ┌─────────────────┐  │      │
│      │  │  kube-proxy     │  │       │  │  kube-proxy     │  │      │
│      │  │  (networking)   │  │       │  │  (networking)   │  │      │
│      │  └─────────────────┘  │       │  └─────────────────┘  │      │
│      │  ┌─────────────────┐  │       │  ┌─────────────────┐  │      │
│      │  │Container Runtime│  │       │  │Container Runtime│  │      │
│      │  │  (containerd)   │  │       │  │  (containerd)   │  │      │
│      │  └─────────────────┘  │       │  └─────────────────┘  │      │
│      │                       │       │                       │      │
│      │  ┌───────┐ ┌──────┐   │       │  ┌───────┐ ┌──────┐   │      │
│      │  │  Pod  │ │ Pod  │   │       │  │  Pod  │ │ Pod  │   │      │
│      │  └───────┘ └──────┘   │       │  └───────┘ └──────┘   │      │
│      └───────────────────────┘       └───────────────────────┘      │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.1 Control Plane — The Brain

#### API Server (`kube-apiserver`)
The **only** component that reads and writes to etcd. Everything goes through it.
- Validates and processes REST requests (from `kubectl`, controllers, kubelet)
- Implements authentication, authorization (RBAC), and admission control
- Stateless — you can run multiple replicas behind a load balancer

```bash
# The API server is what kubectl talks to:
kubectl cluster-info
# Kubernetes control plane is running at https://127.0.0.1:55000

# View raw API resources:
kubectl api-resources
kubectl api-versions
```

#### etcd — The Cluster Database
etcd is a **distributed key-value store** — the single source of truth for the entire cluster state.

- Stores ALL cluster data: pod specs, node info, secrets, service definitions
- Uses the **Raft consensus algorithm** (majority quorum required for writes)
- Run with an odd number of replicas (3 or 5) for HA
- **If etcd dies, the cluster brain dies** — no new pods can be scheduled
- Backup etcd regularly in production!

```bash
# View etcd as a pod in kube-system:
kubectl get pods -n kube-system | grep etcd

# Snapshot etcd (run inside the etcd pod or on the master node):
# ETCDCTL_API=3 etcdctl snapshot save /backup/etcd-snapshot.db \
#   --endpoints=https://127.0.0.1:2379 \
#   --cacert=/etc/kubernetes/pki/etcd/ca.crt \
#   --cert=/etc/kubernetes/pki/etcd/server.crt \
#   --key=/etc/kubernetes/pki/etcd/server.key
```

#### Scheduler (`kube-scheduler`)
Watches for **unscheduled Pods** (pods with no `spec.nodeName`) and assigns them to nodes.

Scheduling process:
1. **Filter** — eliminate nodes that can't run the pod (insufficient CPU/memory, taints, affinity rules)
2. **Score** — rank remaining nodes (spread across zones, least resource pressure, etc.)
3. **Bind** — write the chosen node to the pod spec in etcd

#### Controller Manager (`kube-controller-manager`)
A single binary running ~30+ controllers, each in its own goroutine:

| Controller | Job |
|---|---|
| **ReplicaSet controller** | Ensures N pod replicas exist; creates/deletes pods |
| **Deployment controller** | Manages ReplicaSets for rollout/rollback |
| **Node controller** | Marks nodes as NotReady, evicts pods after timeout |
| **Endpoints controller** | Populates Service → Pod IP mappings |
| **Job controller** | Creates pods for Jobs, tracks completions |
| **CronJob controller** | Creates Jobs on schedule |
| **Namespace controller** | Cleans up resources when namespace is deleted |

Each controller runs a **reconciliation loop**:
```
watch current state → compare to desired state → take action → repeat
```

```bash
# See all controllers running:
kubectl get pods -n kube-system | grep controller-manager
kubectl describe pod kube-controller-manager-<node> -n kube-system
```

---

### 2.2 Worker Nodes

#### kubelet — The Node Agent
Runs on **every** worker node. The kubelet's job:
1. Registers the node with the API server
2. Watches for Pods assigned to its node via the API server
3. Tells the container runtime (containerd/CRI-O) to pull images and run containers
4. Reports pod status and node health back to API server
5. Runs liveness/readiness probes and restarts failing containers
6. Manages pod lifecycle: start, stop, restart

```bash
# View kubelet logs on a node (SSH to node first):
# journalctl -u kubelet -f

# Node info (shows kubelet version, container runtime, etc.):
kubectl get nodes -o wide
kubectl describe node <node-name>
```

#### kube-proxy — The Networking Agent
Runs on every node. Maintains **iptables** or **IPVS** rules that implement Services.

When a Service is created, kube-proxy writes rules so that:
- Traffic to `ClusterIP:port` is DNAT'd to a real pod IP
- Traffic is load-balanced across all healthy pod IPs

```bash
# kube-proxy is also a DaemonSet:
kubectl get pods -n kube-system | grep kube-proxy
kubectl logs -n kube-system <kube-proxy-pod>
```

#### Container Runtime Interface (CRI)
Kubernetes doesn't run containers itself — it delegates to a CRI-compliant runtime:
- **containerd** (most common, Docker's runtime)
- **CRI-O** (lightweight, OpenShift default)
- Docker (deprecated in K8s 1.24+)

---

### 2.3 Networking Model

Kubernetes networking has 4 rules:
1. Every pod gets a **unique cluster-wide IP**
2. Pods on any node can talk to pods on any other node **without NAT**
3. Agents on a node (kubelet, kube-proxy) can talk to all pods on that node
4. Pods see their own IP the same way others see it (no SNAT)

This flat network is implemented by a **CNI (Container Network Interface) plugin**:
- **Calico** — BGP routing, full NetworkPolicy support
- **Cilium** — eBPF-based, high performance, full NetworkPolicy
- **Flannel** — simple VXLAN overlay, no NetworkPolicy
- **WeaveNet** — VXLAN overlay with NetworkPolicy

```
Pod A (10.244.1.5) → Service VIP (10.96.0.100) → kube-proxy DNAT → Pod B (10.244.2.7)

DNS: demo-service.k8s-demo.svc.cluster.local → 10.96.0.100 (ClusterIP)
     demo-0.demo-headless.k8s-demo.svc.cluster.local → 10.244.1.5 (Pod IP, headless)
```

---

## 3. Project Structure

```
kubernetes/
├── src/
│   └── main/
│       ├── java/com/k8sdemo/
│       │   ├── K8sDemoApplication.java       # Spring Boot entrypoint
│       │   └── controller/
│       │       └── DemoController.java        # REST endpoints
│       └── resources/
│           └── application.properties         # App config
├── pom.xml                                    # Maven build
├── Dockerfile                                 # Multi-stage Docker build
├── k8s/
│   ├── 01-namespace.yml        # Namespace + ResourceQuota + LimitRange
│   ├── 02-configmap.yml        # ConfigMap (env vars + file mounts)
│   ├── 03-secret.yml           # Secret (base64 encoded sensitive data)
│   ├── 04-deployment.yml       # Deployment + ReplicaSet + Pod spec
│   ├── 05-service.yml          # ClusterIP + NodePort + Headless services
│   ├── 06-persistent-volume.yml # PV + PVC (storage)
│   ├── 07-hpa.yml              # HorizontalPodAutoscaler
│   ├── 08-ingress.yml          # Ingress (L7 routing)
│   ├── 09-statefulset.yml      # StatefulSet (stateful apps)
│   ├── 10-daemonset.yml        # DaemonSet (one pod per node)
│   ├── 11-jobs.yml             # Job + CronJob
│   ├── 12-network-policy.yml   # NetworkPolicy (pod firewall)
│   └── 13-rbac.yml             # ServiceAccount + Role + RoleBinding
├── scripts/
│   └── demo.sh                 # Convenience scripts
└── README.md                   # This file
```

---

## 4. Setup & Prerequisites

### Install Tools

```bash
# 1. Docker (container runtime)
brew install --cask docker          # macOS
# Or: https://docs.docker.com/get-docker/

# 2. kubectl (Kubernetes CLI)
brew install kubectl
kubectl version --client

# 3. Minikube (local single-node cluster — easiest for learning)
brew install minikube

# 4. (Alternative) Kind — Kubernetes IN Docker (multi-node locally)
brew install kind

# 5. (Alternative) k3d — k3s in Docker (very lightweight)
brew install k3d

# 6. Helm (Kubernetes package manager — for installing ingress-nginx, etc.)
brew install helm
```

### Start a Local Cluster

```bash
# ── Option A: Minikube (recommended for beginners) ─────────────
minikube start \
  --driver=docker \
  --cpus=4 \
  --memory=8g \
  --kubernetes-version=v1.28.0

# Enable useful addons:
minikube addons enable ingress          # NGINX Ingress Controller
minikube addons enable metrics-server   # Required for HPA
minikube addons enable dashboard        # Web UI
minikube addons list                    # See all available addons

# Open the dashboard:
minikube dashboard

# ── Option B: Kind (multi-node simulation) ──────────────────────
cat > kind-config.yml << 'EOF'
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
  - role: control-plane
  - role: worker
  - role: worker
EOF
kind create cluster --config kind-config.yml --name demo-cluster

# ── Verify cluster is running ───────────────────────────────────
kubectl cluster-info
kubectl get nodes
kubectl get pods -A                    # All pods in all namespaces
```

---

## 5. Build & Deploy

### Build the Docker Image

```bash
# Navigate to the project root
cd /path/to/kubernetes

# Build the image
docker build -t k8s-demo:1.0.0 .

# For Minikube: load image directly (no registry needed)
minikube image load k8s-demo:1.0.0

# For Kind: load image
kind load docker-image k8s-demo:1.0.0 --name demo-cluster

# Verify image is available
minikube image ls | grep k8s-demo
```

### Deploy Everything

```bash
# Apply manifests in order (dependencies first)
kubectl apply -f k8s/01-namespace.yml
kubectl apply -f k8s/02-configmap.yml
kubectl apply -f k8s/03-secret.yml
kubectl apply -f k8s/06-persistent-volume.yml
kubectl apply -f k8s/04-deployment.yml
kubectl apply -f k8s/05-service.yml
kubectl apply -f k8s/07-hpa.yml
kubectl apply -f k8s/08-ingress.yml

# Or apply ALL at once (kubectl resolves dependencies)
kubectl apply -f k8s/

# Watch everything come up:
kubectl get all -n k8s-demo -w
```

### Access the App

```bash
# ── Minikube: use NodePort or tunnel ──────────────────────────
minikube service demo-service-nodeport -n k8s-demo --url
# Outputs: http://127.0.0.1:XXXXX — open in browser

# Or use port-forward (works everywhere):
kubectl port-forward svc/demo-service 8080:80 -n k8s-demo
# Then visit: http://localhost:8080

# Test all endpoints:
curl http://localhost:8080/hello
curl http://localhost:8080/info
curl http://localhost:8080/config
curl http://localhost:8080/secret
curl http://localhost:8080/counter
curl http://localhost:8080/storage
curl http://localhost:8080/actuator/health
```

---

## 6. Kubernetes Objects — Hands-On

### 6.1 Namespace

```bash
# Create and explore namespaces
kubectl apply -f k8s/01-namespace.yml

# List all namespaces
kubectl get namespaces
kubectl get ns            # short form

# See what's in each system namespace
kubectl get all -n kube-system
kubectl get all -n kube-public
kubectl get all -n kube-node-lease

# Check our namespace
kubectl get all -n k8s-demo
kubectl describe namespace k8s-demo

# Check ResourceQuota usage (how much of quota is consumed)
kubectl describe resourcequota demo-quota -n k8s-demo

# Check LimitRange
kubectl describe limitrange demo-limit-range -n k8s-demo

# Set default namespace (avoids typing -n k8s-demo every time)
kubectl config set-context --current --namespace=k8s-demo
kubectl get pods    # now defaults to k8s-demo

# Reset to default
kubectl config set-context --current --namespace=default

# Cross-namespace DNS test (from inside a pod):
# curl http://demo-service.k8s-demo.svc.cluster.local
```

---

### 6.2 Pod

A Pod is the **smallest deployable unit** in Kubernetes. It contains one or more containers that share:
- Same network namespace (same IP, same localhost)
- Same IPC namespace
- Optionally the same PID namespace

```bash
# View pods
kubectl get pods -n k8s-demo
kubectl get pods -n k8s-demo -o wide          # show node, pod IP
kubectl get pods -n k8s-demo -o yaml          # full YAML spec
kubectl get pods -n k8s-demo --show-labels    # show labels

# Describe a pod — shows events, container state, probes, volumes
kubectl describe pod <pod-name> -n k8s-demo

# Get pod logs
kubectl logs <pod-name> -n k8s-demo
kubectl logs <pod-name> -n k8s-demo --tail=50
kubectl logs <pod-name> -n k8s-demo -f          # follow/stream
kubectl logs <pod-name> -n k8s-demo -c k8s-demo # specific container

# Previous container's logs (after crash/restart)
kubectl logs <pod-name> -n k8s-demo --previous

# Shell into a running pod
kubectl exec -it <pod-name> -n k8s-demo -- /bin/sh

# Inside the pod — explore what Kubernetes injected:
env | grep -E "APP_|DB_|API_|POD_|NODE_"   # env vars from ConfigMap, Secret, Downward API
cat /config/app.properties                  # ConfigMap file mount
ls /data/                                   # PersistentVolume mount
cat /var/run/secrets/kubernetes.io/serviceaccount/token  # SA token

# Copy files to/from pod
kubectl cp <pod-name>:/data/visits.txt ./visits.txt -n k8s-demo
kubectl cp ./myfile.txt <pod-name>:/tmp/myfile.txt -n k8s-demo

# Run a one-off debug pod
kubectl run debug --image=busybox:1.36 -it --rm --restart=Never -- /bin/sh
kubectl run curl-test --image=curlimages/curl:latest -it --rm --restart=Never \
  -- curl http://demo-service.k8s-demo.svc.cluster.local/hello

# View pod resource usage (requires metrics-server)
kubectl top pod -n k8s-demo
kubectl top pod -n k8s-demo --sort-by=cpu
```

---

### 6.3 Deployment & ReplicaSet

```bash
# Apply the deployment
kubectl apply -f k8s/04-deployment.yml

# View deployment status
kubectl get deployments -n k8s-demo
kubectl describe deployment k8s-demo-deployment -n k8s-demo

# ReplicaSet — created automatically by the Deployment
kubectl get replicasets -n k8s-demo
kubectl describe replicaset -n k8s-demo

# Watch the relationship: Deployment → ReplicaSet → Pods
kubectl get deploy,rs,pods -n k8s-demo

# ── Scaling ─────────────────────────────────────────────────────
# Scale manually (overrides replicas in YAML)
kubectl scale deployment k8s-demo-deployment --replicas=4 -n k8s-demo
kubectl get pods -n k8s-demo -w    # Watch pods appear/disappear

# Scale back down
kubectl scale deployment k8s-demo-deployment --replicas=2 -n k8s-demo

# ── Desired State Demo ───────────────────────────────────────────
# Manually delete a pod — the ReplicaSet controller immediately creates a replacement
kubectl get pods -n k8s-demo
kubectl delete pod <any-pod-name> -n k8s-demo
kubectl get pods -n k8s-demo -w    # Watch replacement pod appear!

# This is DESIRED STATE in action:
# You said "2 replicas". Kubernetes makes it so. Always.
```

---

### 6.4 Service

```bash
# Apply services
kubectl apply -f k8s/05-service.yml

# List services
kubectl get services -n k8s-demo
kubectl get svc -n k8s-demo -o wide

# Describe service — see selector, endpoints, ports
kubectl describe svc demo-service -n k8s-demo

# View Endpoints — the actual Pod IPs the Service routes to
kubectl get endpoints -n k8s-demo
kubectl get endpoints demo-service -n k8s-demo

# Watch endpoints change as pods come and go:
kubectl get endpoints -n k8s-demo -w

# ── Port Forward (quick local access) ─────────────────────────
kubectl port-forward svc/demo-service 8080:80 -n k8s-demo

# ── NodePort access ────────────────────────────────────────────
minikube service demo-service-nodeport -n k8s-demo
# Or:
NODE_IP=$(minikube ip)
curl http://$NODE_IP:30080/hello

# ── DNS resolution test ────────────────────────────────────────
# From inside a pod:
kubectl exec -it <pod-name> -n k8s-demo -- /bin/sh
# nslookup demo-service.k8s-demo.svc.cluster.local
# curl http://demo-service/hello              (within same namespace)
# curl http://demo-service.k8s-demo/hello    (cross-namespace)

# ── Service load balancing demo ───────────────────────────────
# With multiple replicas, each request may hit a different pod.
# The 'podName' in the response shows which pod handled it.
for i in {1..10}; do curl -s http://localhost:8080/hello | python3 -m json.tool; done
# You'll see different podNames — that's kube-proxy load balancing!
```

---

### 6.5 ConfigMap

```bash
# Apply ConfigMap
kubectl apply -f k8s/02-configmap.yml

# View ConfigMap
kubectl get configmaps -n k8s-demo
kubectl describe configmap demo-config -n k8s-demo
kubectl get configmap demo-config -n k8s-demo -o yaml

# Create a ConfigMap from the CLI (good for quick tests):
kubectl create configmap my-test-config \
  --from-literal=KEY1=value1 \
  --from-literal=KEY2=value2 \
  -n k8s-demo

# Create from a file:
kubectl create configmap my-file-config \
  --from-file=nginx.conf=/path/to/nginx.conf \
  -n k8s-demo

# ── Update ConfigMap and see it live ───────────────────────────
kubectl edit configmap demo-config -n k8s-demo
# Change APP_GREETING value, save and quit
# File-mounted ConfigMaps update within ~1 minute automatically!
# Env var ConfigMaps require pod restart to pick up changes.

# Verify the /config/app.properties inside the pod updated:
kubectl exec -it <pod-name> -n k8s-demo -- cat /config/app.properties

# Test that the env var change was consumed (after pod restart):
kubectl rollout restart deployment k8s-demo-deployment -n k8s-demo
curl http://localhost:8080/config
```

---

### 6.6 Secret

```bash
# Apply secret
kubectl apply -f k8s/03-secret.yml

# List secrets (values are NOT shown)
kubectl get secrets -n k8s-demo
kubectl describe secret demo-secret -n k8s-demo

# Decode a secret value:
kubectl get secret demo-secret -n k8s-demo -o jsonpath='{.data.DB_PASSWORD}' | base64 -d
kubectl get secret demo-secret -n k8s-demo -o jsonpath='{.data.API_KEY}' | base64 -d

# Create a secret from the CLI:
kubectl create secret generic my-secret \
  --from-literal=password=mysecretpassword \
  -n k8s-demo

# Create a TLS secret:
# kubectl create secret tls demo-tls --cert=tls.crt --key=tls.key -n k8s-demo

# Test secret injection (shows masked values):
kubectl port-forward svc/demo-service 8080:80 -n k8s-demo
curl http://localhost:8080/secret
# Output: {"DB_PASSWORD":"******* (injected, 19 chars)","API_KEY":"******* (injected, 15 chars)"}
```

---

### 6.7 Persistent Volume & PVC

```bash
# Apply PV and PVC
kubectl apply -f k8s/06-persistent-volume.yml

# View PersistentVolumes (cluster-scoped, no namespace)
kubectl get persistentvolumes
kubectl get pv
kubectl describe pv demo-pv

# View PersistentVolumeClaims (namespace-scoped)
kubectl get persistentvolumeclaims -n k8s-demo
kubectl get pvc -n k8s-demo
kubectl describe pvc demo-pvc -n k8s-demo

# Status should be: Bound (PVC matched to PV)
# If it stays Pending: check storage class and access mode match

# ── Demo: persistence across pod restarts ─────────────────────
# Call the /storage endpoint multiple times
curl http://localhost:8080/storage    # count=1
curl http://localhost:8080/storage    # count=2
curl http://localhost:8080/storage    # count=3

# Delete the pod — the Deployment creates a new one
kubectl delete pod <pod-name> -n k8s-demo
# Wait for new pod
kubectl get pods -n k8s-demo -w

# Hit /storage again — count continues from where it left off!
curl http://localhost:8080/storage    # count=4 (not 1!)

# ── Counter vs Storage endpoint comparison ─────────────────────
# /counter   — in-memory AtomicLong, resets on pod restart (stateless)
# /storage   — writes to /data/visits.txt on PV, survives restarts (stateful)
curl http://localhost:8080/counter    # Resets to 1 after pod restart
curl http://localhost:8080/storage    # Continues counting

# Check what's on the node's filesystem (hostPath):
minikube ssh -- ls -la /tmp/k8s-demo-data/
minikube ssh -- cat /tmp/k8s-demo-data/visits.txt
```

---

### 6.8 HPA — Auto-Scale

```bash
# Prerequisites: metrics-server must be running
kubectl get pods -n kube-system | grep metrics-server

# Apply HPA
kubectl apply -f k8s/07-hpa.yml

# View HPA status
kubectl get hpa -n k8s-demo
kubectl describe hpa demo-hpa -n k8s-demo

# Watch HPA in real time:
kubectl get hpa -n k8s-demo -w

# ── Trigger scale-out ──────────────────────────────────────────
# Hit the /load endpoint to burn CPU and trigger HPA:

# In one terminal, watch the HPA and pods:
watch -n 2 'kubectl get hpa,pods -n k8s-demo'

# In another terminal, hammer the load endpoint:
while true; do
  curl -s http://localhost:8080/load?iterations=500000 > /dev/null
done

# Or use Apache Bench:
# ab -n 10000 -c 100 http://localhost:8080/load?iterations=100000

# You'll see:
#   1. CPU% rise above 60% threshold
#   2. HPA DESIRED replicas increase (e.g., 2 → 4 → 8)
#   3. New pods appear: kubectl get pods -n k8s-demo -w

# Stop the load. After 5 minutes (stabilizationWindowSeconds=300),
# HPA will scale back down to minReplicas=2.

# Manual override of HPA bounds:
kubectl patch hpa demo-hpa -n k8s-demo \
  -p '{"spec":{"minReplicas":3,"maxReplicas":10}}'
```

---

### 6.9 StatefulSet

```bash
# Apply StatefulSet (note: needs headless service and PVs)
kubectl apply -f k8s/09-statefulset.yml

# View StatefulSet
kubectl get statefulsets -n k8s-demo
kubectl describe statefulset k8s-demo-stateful -n k8s-demo

# Pods have STABLE, ORDERED names: demo-0, demo-1, demo-2
kubectl get pods -n k8s-demo -l app=k8s-demo-stateful

# Each pod gets its OWN PVC:
kubectl get pvc -n k8s-demo

# Stable DNS for each pod (run from inside the cluster):
kubectl run -it --rm dns-test --image=busybox:1.36 --restart=Never -- \
  nslookup k8s-demo-stateful-0.demo-headless.k8s-demo.svc.cluster.local

# ── Ordered startup/shutdown demo ─────────────────────────────
# StatefulSets start pods in order: 0, then 1, then 2
# Delete pod-2 — it's replaced BEFORE pod-0 or pod-1 would be deleted
kubectl delete pod k8s-demo-stateful-2 -n k8s-demo
kubectl get pods -n k8s-demo -w

# Scale StatefulSet
kubectl scale statefulset k8s-demo-stateful --replicas=5 -n k8s-demo
# Watch pods 3, 4 appear in order
kubectl get pods -n k8s-demo -l app=k8s-demo-stateful -w
```

---

### 6.10 DaemonSet

```bash
# Apply DaemonSet
kubectl apply -f k8s/10-daemonset.yml

# One pod per node — if you have 2 workers + 1 master = 2 or 3 pods
kubectl get daemonsets -n k8s-demo
kubectl describe daemonset demo-log-collector -n k8s-demo
kubectl get pods -n k8s-demo -l app=demo-log-collector -o wide

# Logs from each node's log collector:
kubectl logs -n k8s-demo -l app=demo-log-collector --prefix

# Real-world DaemonSets in kube-system:
kubectl get daemonsets -n kube-system
# kube-proxy, calico-node, aws-node (on EKS), etc.

# Add a new node (Minikube multi-node):
# minikube node add
# kubectl get nodes      # new node appears
# kubectl get pods -n k8s-demo -l app=demo-log-collector
# Pod automatically appears on the new node!
```

---

### 6.11 Job & CronJob

```bash
# Apply Job and CronJob
kubectl apply -f k8s/11-jobs.yml

# ── Job ──────────────────────────────────────────────────────
kubectl get jobs -n k8s-demo
kubectl describe job demo-db-migration -n k8s-demo

# Watch job completion:
kubectl get pods -n k8s-demo -l type=migration -w
kubectl logs -n k8s-demo -l type=migration

# Job pod status: Completed (not Running)
kubectl get pods -n k8s-demo -l type=migration

# ── CronJob ──────────────────────────────────────────────────
kubectl get cronjobs -n k8s-demo
kubectl describe cronjob demo-cleanup -n k8s-demo

# View jobs created by the CronJob
kubectl get jobs -n k8s-demo

# Manually trigger a CronJob immediately (useful for testing)
kubectl create job --from=cronjob/demo-cleanup manual-cleanup -n k8s-demo
kubectl get pods -n k8s-demo -l type=scheduled

# Suspend a CronJob (pause scheduling without deleting)
kubectl patch cronjob demo-cleanup -n k8s-demo -p '{"spec":{"suspend":true}}'
kubectl patch cronjob demo-cleanup -n k8s-demo -p '{"spec":{"suspend":false}}'  # resume
```

---

### 6.12 Ingress

```bash
# Enable ingress controller (Minikube)
minikube addons enable ingress

# Apply Ingress
kubectl apply -f k8s/08-ingress.yml

# View Ingress
kubectl get ingress -n k8s-demo
kubectl describe ingress demo-ingress -n k8s-demo

# Get the ingress IP:
kubectl get ingress -n k8s-demo -o wide
INGRESS_IP=$(kubectl get ingress demo-ingress -n k8s-demo -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
echo $INGRESS_IP

# Add to /etc/hosts for local testing:
echo "$INGRESS_IP demo.example.com" | sudo tee -a /etc/hosts

# Test host-based routing:
curl http://demo.example.com/hello
curl http://demo.example.com/info

# Minikube tunnel (for LoadBalancer services and Ingress):
minikube tunnel &
curl http://demo.example.com/hello
```

---

### 6.13 NetworkPolicy

```bash
# Apply NetworkPolicies
kubectl apply -f k8s/12-network-policy.yml

# View policies
kubectl get networkpolicies -n k8s-demo
kubectl describe networkpolicy allow-ingress-to-demo -n k8s-demo

# ── Test isolation ─────────────────────────────────────────────
# Run a pod in default namespace — should be BLOCKED by deny-all
kubectl run attacker --image=curlimages/curl:latest \
  --restart=Never --rm -it \
  -- curl --connect-timeout 3 http://demo-service.k8s-demo.svc.cluster.local/hello
# Should fail/timeout due to NetworkPolicy

# Run a pod in k8s-demo namespace — should be ALLOWED
kubectl run allowed-client -n k8s-demo \
  --image=curlimages/curl:latest --restart=Never --rm -it \
  -- curl http://demo-service/hello
# Should succeed!
```

---

### 6.14 RBAC

```bash
# Apply RBAC
kubectl apply -f k8s/13-rbac.yml

# View RBAC objects
kubectl get serviceaccounts -n k8s-demo
kubectl get roles -n k8s-demo
kubectl get rolebindings -n k8s-demo
kubectl get clusterroles | grep demo
kubectl get clusterrolebindings | grep demo

# Check what permissions a ServiceAccount has:
kubectl auth can-i get pods \
  --as=system:serviceaccount:k8s-demo:demo-service-account \
  -n k8s-demo
# yes

kubectl auth can-i delete pods \
  --as=system:serviceaccount:k8s-demo:demo-service-account \
  -n k8s-demo
# no

kubectl auth can-i get secrets \
  --as=system:serviceaccount:k8s-demo:demo-service-account \
  -n k8s-demo
# no (secrets not in the role)

# Describe what a role allows:
kubectl describe role demo-pod-reader -n k8s-demo

# Who can do what? (audit)
kubectl auth can-i --list -n k8s-demo \
  --as=system:serviceaccount:k8s-demo:demo-service-account
```

---

## 7. Auto-Heal Demo

This is one of Kubernetes' most impressive features. Let's break things and watch it recover.

```bash
# Ensure we have 2+ replicas running
kubectl get pods -n k8s-demo

# ── Scenario 1: Pod crash — auto-restart ──────────────────────
# Watch pods in one terminal
kubectl get pods -n k8s-demo -w

# Kill a container process (simulates app crash)
kubectl exec -it <pod-name> -n k8s-demo -- kill 1
# Watch: pod status → Error → CrashLoopBackOff → Running
# kubelet detects the crash via liveness probe → restarts container

# Check restart count:
kubectl get pod <pod-name> -n k8s-demo
# RESTARTS column shows how many times it was restarted

# ── Scenario 2: Pod deletion — ReplicaSet replacement ─────────
kubectl delete pod <pod-name> -n k8s-demo
# New pod appears immediately — ReplicaSet controller ensures desired state

# ── Scenario 3: Liveness probe failure simulation ─────────────
# If the app's /actuator/health/liveness returns 503, kubelet restarts the container.
# To simulate: port-forward then call an endpoint that blocks the app.

# ── Scenario 4: Node failure simulation (Minikube multi-node) ─
minikube node add           # Add a worker node
kubectl get nodes           # See 2 nodes
# Move pods to new node:
kubectl cordon minikube-m02             # Mark node as unschedulable
kubectl drain minikube-m02 --ignore-daemonsets --delete-emptydir-data
# All pods migrate to other nodes!
kubectl uncordon minikube-m02           # Bring it back

# ── Scenario 5: OOMKill (memory limit exceeded) ───────────────
# If a container exceeds memory limits, Linux OOM killer terminates it
# Kubernetes shows it as OOMKilled:
kubectl describe pod <pod-name> -n k8s-demo
# Look for: Last State: Terminated  Reason: OOMKilled
```

---

## 8. Rolling Updates & Rollback

```bash
# Current deployment image
kubectl describe deployment k8s-demo-deployment -n k8s-demo | grep Image

# ── Rolling Update ─────────────────────────────────────────────
# Build new image version
docker build -t k8s-demo:2.0.0 .
minikube image load k8s-demo:2.0.0

# Update deployment with new image
kubectl set image deployment/k8s-demo-deployment \
  k8s-demo=k8s-demo:2.0.0 \
  -n k8s-demo

# Watch the rolling update (zero downtime!):
kubectl rollout status deployment/k8s-demo-deployment -n k8s-demo -w
kubectl get pods -n k8s-demo -w
# Old pods terminate one-by-one, new pods start up
# At no point is the service fully down (maxUnavailable=0)

# ── Rollout History ────────────────────────────────────────────
kubectl rollout history deployment/k8s-demo-deployment -n k8s-demo
kubectl rollout history deployment/k8s-demo-deployment \
  --revision=2 -n k8s-demo

# ── Rollback ──────────────────────────────────────────────────
# Rollback to previous version:
kubectl rollout undo deployment/k8s-demo-deployment -n k8s-demo

# Rollback to specific revision:
kubectl rollout undo deployment/k8s-demo-deployment \
  --to-revision=1 -n k8s-demo

# Watch rollback:
kubectl rollout status deployment/k8s-demo-deployment -n k8s-demo

# ── Pause / Resume Rollout ─────────────────────────────────────
kubectl rollout pause deployment/k8s-demo-deployment -n k8s-demo
# Make multiple changes while paused...
kubectl set image deployment/k8s-demo-deployment k8s-demo=k8s-demo:3.0.0 -n k8s-demo
kubectl set resources deployment/k8s-demo-deployment \
  -c k8s-demo --limits=cpu=1,memory=1Gi -n k8s-demo
# Apply all changes at once:
kubectl rollout resume deployment/k8s-demo-deployment -n k8s-demo

# ── Annotate rollout (for history) ────────────────────────────
kubectl annotate deployment/k8s-demo-deployment \
  kubernetes.io/change-cause="Update to v2.0.0 - improved performance" \
  -n k8s-demo
```

---

## 9. Debugging & Troubleshooting

```bash
# ── Pod won't start — check events ────────────────────────────
kubectl describe pod <pod-name> -n k8s-demo
# Look for: Events section at the bottom
# Common issues:
#   - ImagePullBackOff:  image not found / registry auth
#   - CrashLoopBackOff: app crashes on start (check logs)
#   - Pending:          no node can fit the pod (resource pressure, affinity)
#   - OOMKilled:        increase memory limits

# ── Get all events in namespace ───────────────────────────────
kubectl get events -n k8s-demo
kubectl get events -n k8s-demo --sort-by='.lastTimestamp'
kubectl get events -n k8s-demo --field-selector reason=BackOff

# ── Debug with ephemeral container (Kubernetes 1.23+) ─────────
# Attach a debug container to a running pod without modifying it:
kubectl debug -it <pod-name> -n k8s-demo \
  --image=busybox:1.36 \
  --target=k8s-demo    # Share process namespace with main container

# ── Debug by copying a pod ─────────────────────────────────────
kubectl debug <pod-name> -n k8s-demo \
  --copy-to=debug-copy \
  --image=busybox:1.36 \
  --share-processes

# ── Node-level debugging ───────────────────────────────────────
kubectl get nodes
kubectl describe node <node-name>
kubectl top nodes

# Check node conditions:
kubectl get node <node-name> -o jsonpath='{.status.conditions[*].type}'

# ── Resource usage ────────────────────────────────────────────
kubectl top pods -n k8s-demo
kubectl top pods -n k8s-demo --containers      # per-container breakdown
kubectl top nodes

# ── JSON path queries ─────────────────────────────────────────
# Get all pod IPs:
kubectl get pods -n k8s-demo -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.status.podIP}{"\n"}{end}'

# Get container image for each pod:
kubectl get pods -n k8s-demo -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.spec.containers[0].image}{"\n"}{end}'

# ── Force delete stuck pod ────────────────────────────────────
kubectl delete pod <pod-name> -n k8s-demo --force --grace-period=0

# ── Dry run (preview changes without applying) ────────────────
kubectl apply -f k8s/04-deployment.yml --dry-run=client
kubectl apply -f k8s/04-deployment.yml --dry-run=server   # validates server-side

# ── Diff (show what would change) ─────────────────────────────
kubectl diff -f k8s/04-deployment.yml

# ── Port forward for any resource ─────────────────────────────
kubectl port-forward pod/<pod-name> 8080:8080 -n k8s-demo
kubectl port-forward svc/demo-service 8080:80 -n k8s-demo
kubectl port-forward deployment/k8s-demo-deployment 8080:8080 -n k8s-demo
```

---

## 10. Essential kubectl Cheatsheet

### Cluster Info
```bash
kubectl cluster-info
kubectl cluster-info dump              # Full cluster state (debugging)
kubectl version
kubectl api-resources                  # All available resource types
kubectl api-versions                   # All API versions
kubectl explain pod                    # Built-in docs for any resource
kubectl explain pod.spec.containers
kubectl explain deployment.spec.strategy
```

### Context & Config
```bash
kubectl config view                    # Show kubeconfig
kubectl config get-contexts            # List clusters/contexts
kubectl config use-context <name>      # Switch cluster
kubectl config set-context --current --namespace=k8s-demo  # Set default ns
```

### Get / Describe
```bash
kubectl get <resource> [-n <ns>] [-o wide|yaml|json|jsonpath|custom-columns]
kubectl get all -n k8s-demo            # Pods, services, deployments, etc.
kubectl get pods -A                    # All namespaces
kubectl describe <resource> <name> -n <ns>
```

### Labels & Selectors
```bash
kubectl get pods -l app=k8s-demo -n k8s-demo
kubectl get pods -l 'app in (k8s-demo,other-app)' -n k8s-demo
kubectl label pod <pod> tier=backend -n k8s-demo
kubectl label pod <pod> tier- -n k8s-demo        # Remove label
kubectl annotate deployment k8s-demo-deployment notes="testing" -n k8s-demo
```

### Create / Apply / Delete
```bash
kubectl apply -f <file-or-dir>         # Declarative — idempotent
kubectl create -f <file>               # Imperative — fails if exists
kubectl delete -f <file>
kubectl delete pod <name> -n k8s-demo
kubectl delete all --all -n k8s-demo   # Delete everything in namespace
```

### Imperative Object Creation
```bash
kubectl create deployment my-app --image=nginx:1.25 --replicas=3 -n k8s-demo
kubectl create service clusterip my-svc --tcp=80:8080 -n k8s-demo
kubectl create configmap my-cm --from-literal=key=val -n k8s-demo
kubectl create secret generic my-sec --from-literal=pass=secret -n k8s-demo
kubectl create job my-job --image=busybox -- echo hello -n k8s-demo
kubectl create cronjob my-cron --image=busybox --schedule="*/1 * * * *" -- echo hi -n k8s-demo
```

### Generate YAML Without Applying
```bash
kubectl create deployment my-app --image=nginx --dry-run=client -o yaml
kubectl create service clusterip my-svc --tcp=80:8080 --dry-run=client -o yaml
kubectl run mypod --image=nginx --dry-run=client -o yaml
```

### Logs
```bash
kubectl logs <pod> -n k8s-demo
kubectl logs <pod> -c <container> -n k8s-demo
kubectl logs <pod> --previous -n k8s-demo
kubectl logs -l app=k8s-demo -n k8s-demo --prefix
kubectl logs <pod> -f -n k8s-demo      # Stream
```

### Exec & Copy
```bash
kubectl exec -it <pod> -- /bin/sh
kubectl exec -it <pod> -c <container> -- bash
kubectl cp <pod>:/path/to/file ./local-file -n k8s-demo
kubectl cp ./local-file <pod>:/path/to/file -n k8s-demo
```

### Deployments
```bash
kubectl rollout status deployment/<name> -n k8s-demo
kubectl rollout history deployment/<name> -n k8s-demo
kubectl rollout undo deployment/<name> -n k8s-demo
kubectl rollout pause deployment/<name> -n k8s-demo
kubectl rollout resume deployment/<name> -n k8s-demo
kubectl set image deployment/<name> container=image:tag -n k8s-demo
kubectl scale deployment/<name> --replicas=5 -n k8s-demo
```

### Node Management
```bash
kubectl get nodes -o wide
kubectl top nodes
kubectl describe node <name>
kubectl cordon <node>                  # Mark unschedulable
kubectl uncordon <node>                # Re-enable scheduling
kubectl drain <node> --ignore-daemonsets --delete-emptydir-data
kubectl taint node <node> key=value:NoSchedule
kubectl taint node <node> key-         # Remove taint
```

### Patching
```bash
# Strategic merge patch (most common)
kubectl patch deployment k8s-demo-deployment -n k8s-demo \
  -p '{"spec":{"replicas":3}}'

# JSON merge patch
kubectl patch deployment k8s-demo-deployment -n k8s-demo \
  --type merge -p '{"spec":{"replicas":3}}'

# JSON patch (array operations)
kubectl patch deployment k8s-demo-deployment -n k8s-demo \
  --type json \
  -p '[{"op":"replace","path":"/spec/replicas","value":3}]'
```

---

## Cleanup

```bash
# Delete all k8s-demo resources
kubectl delete namespace k8s-demo

# Delete cluster-scoped resources
kubectl delete pv demo-pv
kubectl delete clusterrole demo-cluster-pod-reader
kubectl delete clusterrolebinding demo-cluster-pod-reader-binding

# Stop Minikube
minikube stop

# Delete Minikube (removes all data)
minikube delete
```

---

## Quick Reference: Kubernetes Object Hierarchy

```
Cluster
├── Namespace (isolation boundary)
│   ├── ResourceQuota (namespace resource caps)
│   ├── LimitRange (per-container defaults)
│   ├── ConfigMap (configuration)
│   ├── Secret (sensitive data)
│   ├── ServiceAccount (pod identity)
│   ├── Role + RoleBinding (namespace RBAC)
│   ├── NetworkPolicy (pod firewall)
│   │
│   ├── Deployment (manages ReplicaSets)
│   │   └── ReplicaSet (manages Pods)
│   │       └── Pod (1+ containers, shared network)
│   │           ├── Container (your app)
│   │           ├── InitContainer (pre-start setup)
│   │           └── Sidecar (helper container)
│   │
│   ├── StatefulSet (ordered, stable-identity pods + own PVCs)
│   ├── DaemonSet (one pod per node)
│   ├── Job (run-to-completion)
│   ├── CronJob (scheduled Jobs)
│   │
│   ├── Service (stable VIP + DNS for pods)
│   ├── Ingress (L7 HTTP routing → Services)
│   └── PersistentVolumeClaim (request for storage)
│
├── Node (worker machine)
│   ├── kubelet (node agent)
│   ├── kube-proxy (networking)
│   └── containerd (container runtime)
│
├── PersistentVolume (cluster storage resource)
├── StorageClass (dynamic provisioning template)
├── ClusterRole + ClusterRoleBinding (cluster-wide RBAC)
└── Namespace (itself cluster-scoped)
```
