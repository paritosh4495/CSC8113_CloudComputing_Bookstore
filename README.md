# G1 Bookstore — Cloud-Native Application

> **CSC8113 Cloud Computing · Group 1 · Newcastle University**

A production-grade, cloud-native online bookstore deployed on **Azure Kubernetes Service (AKS)**. The system is built around three core DevOps objectives: automatic scaling under traffic spikes, infrastructure failure recovery without data loss, and zero-downtime application updates.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Local Development](#local-development)
- [Kubernetes Deployment](#kubernetes-deployment)
- [CI/CD Pipeline](#cicd-pipeline)
- [Scalability](#scalability)
- [Disaster Recovery](#disaster-recovery)
- [Blue-Green Deployment](#blue-green-deployment)
- [Security](#security)
- [Observability](#observability)
- [Load Testing](#load-testing)
- [API Documentation](#api-documentation)
- [Team](#team)

---

## Architecture Overview

All user traffic enters through an **NGINX Ingress Controller** and is forwarded to a Spring Boot **API Gateway**, which validates JWTs, enforces RBAC, and routes requests to the **Catalog Service** or **Cart Service**. Backend services are not publicly exposed. A **React SPA** is served through the same ingress. **Keycloak** provides identity and role management. **PostgreSQL** (deployed as a StatefulSet with a 2Gi PersistentVolumeClaim) provides durable storage. The cluster runs on AKS with **Cluster Autoscaling** and **HPA** configured.

```
Users
  │
  ▼
NGINX Ingress (TLS)
  │
  ├──► API Gateway (Spring Boot) ──JWT-validate──► Keycloak
  │         │
  │         ├──► Catalog Service (Blue / Green)
  │         └──► Cart Service
  │
  └──► Frontend (React + Vite)

PostgreSQL StatefulSet ◄── All backend services
       │
       └── Velero Backup ──► AWS S3
```

Infrastructure pipeline:

```
GitHub Repository
  │
  └── GitHub Actions (ci → docker → deploy)
        ├── Run tests (Testcontainers / WireMock)
        ├── Build & push Docker image (Cloud Native Buildpacks)
        └── kubectl apply + rollout status
```

---

## Technology Stack

| Layer | Technology | Notes |
|---|---|---|
| **Backend** | Java 21, Spring Boot 3.5 | Unified stack across all services |
| **Frontend** | React 19, Vite 7 | Served as static SPA via NGINX |
| **Auth** | Keycloak 26 | Realm config version-controlled as JSON |
| **Database** | PostgreSQL 16 | Kubernetes StatefulSet + PVC |
| **Container runtime** | Cloud Native Buildpacks (Paketo) | Non-root, OCI-compliant images |
| **Orchestration** | Kubernetes (AKS 1.33) | Cluster Autoscaler enabled |
| **CI/CD** | GitHub Actions | 3-stage gated pipeline |
| **Backup** | Velero + AWS S3 | Scheduled daily, cross-cloud |
| **Observability** | Azure Managed Prometheus + Grafana | Off-cluster scraping |
| **Load testing** | k6 | 500 VU ramp, 5-stage scenarios |
| **Ingress** | NGINX Ingress Controller | TLS termination, rewrite rules |

---

## Project Structure

```
CSC8113_CloudComputing_Bookstore/
├── .github/
│   └── workflows/                  # GitHub Actions pipelines
│       ├── api-gateway.yml
│       ├── cart-service.yml
│       ├── catalog-service.yml
│       └── front-end.yml
│
├── backend/
│   ├── api-gateway/                # Spring Cloud Gateway + OAuth2 resource server
│   ├── cart-service/               # Cart management, Resilience4j circuit breaker
│   └── catalog-service/            # Product catalogue, caching, HPA target
│
├── deployment/
│   ├── docker-compose/             # Local dev: infra + apps
│   │   ├── infra.yml               # PostgreSQL + Keycloak
│   │   ├── apps.yml                # All application services
│   │   ├── init.sql                # DB initialisation
│   │   └── realm-export.json       # Keycloak realm definition
│   └── k8s/                        # Kubernetes manifests
│       ├── secrets.yml
│       ├── postgres.yml            # StatefulSet + Velero annotation
│       ├── keycloak.yml
│       ├── catalog-service-blue.yml
│       ├── catalog-service-green.yml
│       ├── catalog-service-svc.yml # Traffic switch selector
│       ├── cart-service.yml
│       ├── api-gateway.yml
│       ├── frontend.yml
│       ├── hpa.yml                 # HPA targeting catalog-service-blue
│       └── ingress.yml
│
├── docs/
│   ├── Commands/                   # Azure CLI, Velero, k6 cheat sheets
│   ├── LoadTestResults/            # Raw k6 output (V1–V5)
│   └── LoadTestScript/             # k6 scripts
│
└── front-end/                      # React application
    ├── src/
    │   ├── Pages/                  # Catalogue, BookDetails, Cart, Checkout, Admin
    │   ├── components/             # Navbar, Footer, BookCard
    │   ├── context/                # AuthContext (Keycloak), CartContext
    │   ├── Services/               # API client functions
    │   └── config/                 # API base URL, Keycloak config
    └── Dockerfile                  # Multi-stage: node build → nginx:alpine
```

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 21 |
| Maven | 3.9+ (wrapper included) |
| Docker | 24+ |
| Node.js | 20 |
| kubectl | 1.29+ |
| Azure CLI | Latest |
| k6 | Latest |
| Velero CLI | Latest |

---

## Local Development

### 1. Start infrastructure (PostgreSQL + Keycloak)

```bash
cd deployment/docker-compose
docker compose -f infra.yml up -d
```

Wait for Keycloak to become healthy (~30 seconds), then verify:

```bash
docker compose -f infra.yml ps
```

### 2. Build backend services

```bash
cd backend
./mvnw clean verify
```

### 3. Run all services with Docker Compose

```bash
# From the backend/ directory
cd deployment/docker-compose
docker compose -f infra.yml -f apps.yml up -d
```

| Service | URL |
|---|---|
| API Gateway | http://localhost:9000 |
| Catalog Service | http://localhost:8081 |
| Cart Service | http://localhost:8082 |
| Keycloak | http://localhost:9191 |
| Frontend | http://localhost:5173 |

### 4. Run the frontend

```bash
cd front-end
npm ci
npm run dev
```

### 5. Run tests

```bash
# All services
cd backend
./mvnw clean verify

# Single service
cd backend/catalog-service
../mvnw clean verify
```

Tests use **Testcontainers** (ephemeral PostgreSQL containers) and **WireMock** (stubbed Catalog Service for Cart integration tests). No external dependencies are required.

---

## Kubernetes Deployment

### Provision the AKS cluster

```bash
# Login and register provider
az login
az provider register --namespace Microsoft.ContainerService

# Create resource group
az group create --name bookstore-rg --location swedencentral

# Create cluster with autoscaler enabled
az aks create \
  --resource-group bookstore-rg \
  --name bookstore-cluster \
  --node-count 2 \
  --node-vm-size Standard_B2s_v2 \
  --enable-cluster-autoscaler \
  --min-count 1 \
  --max-count 4 \
  --generate-ssh-keys

# Fetch credentials
az aks get-credentials --resource-group bookstore-rg --name bookstore-cluster
```

### Install NGINX Ingress and issue TLS certificate

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx && helm repo update
helm install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx --create-namespace \
  --set controller.service.annotations."service\.beta\.kubernetes\.io/azure-load-balancer-health-probe-request-path"="/healthz"

# Self-signed certificate (replace IP with your LoadBalancer IP)
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout tls.key -out tls.crt \
  -subj "/CN=<YOUR_IP>" \
  -addext "subjectAltName=IP:<YOUR_IP>"

kubectl create secret tls bookstore-tls --cert=tls.crt --key=tls.key
```

### Deploy application

```bash
cd deployment/k8s

kubectl apply -f secrets.yml
kubectl apply -f postgres.yml
kubectl rollout status statefulset/postgres --timeout=3m

kubectl create configmap keycloak-realm-config \
  --from-file=realm.json=realm-export.json

kubectl apply -f keycloak.yml
kubectl rollout status deployment/keycloak --timeout=6m

kubectl apply -f catalog-service-blue.yml
kubectl apply -f catalog-service-green.yml
kubectl apply -f catalog-service-svc.yml
kubectl apply -f cart-service.yml
kubectl apply -f api-gateway.yml
kubectl apply -f frontend.yml
kubectl apply -f ingress.yml
kubectl apply -f hpa.yml
```

### Verify deployment

```bash
kubectl get pods -w
kubectl get svc
kubectl get ingress
kubectl get hpa
```

### Stop / start cluster (to save credits)

```bash
az aks stop  --resource-group bookstore-rg --name bookstore-cluster
az aks start --resource-group bookstore-rg --name bookstore-cluster
```

---

## CI/CD Pipeline

Automated pipelines exist for the **API Gateway**, **Cart Service**, and **Frontend**. Each pipeline enforces a strict three-stage dependency chain:

```
push to main
    │
    ▼
[ci] Build & run full test suite
    │  (runs on every push and every PR)
    ▼
[docker] Build image via Cloud Native Buildpacks, push to Docker Hub
    │  (only after ci passes, only on merge to main)
    │  Tags: :latest  and  :<7-char-commit-hash>
    ▼
[deploy] kubectl rollout restart + kubectl rollout status
    │  (only after docker completes)
    └─ Failure → GitHub email notification; old pods keep serving traffic
```

The **Catalog Service** pipeline intentionally stops after the `docker` stage (no automated `deploy`) to preserve a stable blue/green environment for demonstration purposes.

Secrets required in GitHub repository settings:

| Secret | Description |
|---|---|
| `DOCKERHUB_USERNAME` | Docker Hub username |
| `DOCKERHUB_TOKEN` | Docker Hub access token |
| `AZURE_CLIENT_ID` | Service principal client ID |
| `AZURE_CLIENT_SECRET` | Service principal secret |
| `AZURE_SUBSCRIPTION_ID` | Azure subscription ID |
| `AZURE_TENANT_ID` | Azure tenant ID |
| `AKS_RESOURCE_GROUP` | Resource group name |
| `AKS_CLUSTER_NAME` | AKS cluster name |

---

## Scalability

### Horizontal Pod Autoscaler

The HPA monitors CPU utilisation on `catalog-service-blue` and scales between **1 and 8 replicas** when the average exceeds **60%** of the declared CPU request of `250m`.

```bash
# Watch HPA in real time
kubectl get hpa -w

# Manual apply
kubectl apply -f deployment/k8s/hpa.yml
```

The CPU request of `250m` was deliberately calibrated: under genuine load from 500 concurrent users, actual consumption crosses the 60% threshold, triggering a scale event. If set too high, the HPA would never fire; too low, and it would scale on background noise.

### Cluster Autoscaler

When the HPA schedules pods that exceed available node capacity, those pods enter `Pending` state. The AKS Cluster Autoscaler detects unschedulable pods and provisions new nodes from Azure's VM pool (min: 1 node, max: 4 nodes).

### Load Testing

```bash
cd docs/LoadTestScript
k6 run load-test.js
```

The default script ramps to **500 virtual users** over 5 stages, hitting catalogue list, search, genre filter, and individual product endpoints through the API Gateway. All five historical test runs (V1–V5) and their raw results are preserved in `docs/LoadTestResults/`.

---

## Disaster Recovery

### Velero backup to AWS S3

**Install Velero** (once per workstation):

```bash
winget install velero          # Windows
brew install velero            # macOS
```

**Create credentials file** (`./credentials-velero`):

```ini
[default]
aws_access_key_id=<YOUR_KEY>
aws_secret_access_key=<YOUR_SECRET>
```

**Install Velero into the cluster:**

```bash
velero install \
  --provider aws \
  --plugins velero/velero-plugin-for-aws:v1.9.0 \
  --bucket bookstore-velero-backups-csc8113 \
  --secret-file "./credentials-velero" \
  --backup-location-config region=eu-west-2 \
  --use-volume-snapshots=false \
  --use-node-agent
```

**Verify backup location is available:**

```bash
velero backup-location get
```

**Create a manual backup:**

```bash
velero backup create postgres-backup-manual --include-namespaces default --wait
velero backup describe postgres-backup-manual
```

**Create the daily automated schedule:**

```bash
velero schedule create daily-postgres-backup \
  --schedule="0 11 * * *" \
  --include-namespaces default \
  --ttl 72h0m0s
```

**Simulate disaster and restore:**

```bash
# Destroy database
kubectl delete statefulset postgres
kubectl delete pvc postgres-storage-postgres-0

# Restore from latest automated backup
velero backup get
velero restore create --from-backup <BACKUP_NAME> --wait

# Verify
kubectl get pods -w
kubectl exec -it postgres-0 -- psql -U postgres -d catalog -c "SELECT * FROM products LIMIT 5;"
```

The `backup.velero.io/backup-volumes: postgres-storage` annotation on the PostgreSQL pod template ensures the actual data volume is snapshotted, not just the Kubernetes resource definitions.

---

## Blue-Green Deployment

Two independent Deployments run simultaneously:

| Deployment | Image Tag | `/version` Response |
|---|---|---|
| `catalog-service-blue` | `:latest` | `{ "color": "blue" }` |
| `catalog-service-green` | `:ab2a2e4` (pinned) | `{ "color": "green" }` |

Traffic is routed by a single Kubernetes Service selector. Switching is **atomic** — there is no window in which traffic is split between versions.

**Switch traffic to green:**

```bash
# Edit catalog-service-svc.yml: change color: blue → color: green
kubectl apply -f deployment/k8s/catalog-service-svc.yml
```

**Roll back to blue:**

```bash
# Revert selector to color: blue
kubectl apply -f deployment/k8s/catalog-service-svc.yml
```

**Monitor the switch:**

```powershell
# PowerShell — poll /version every second
while ($true) {
    Invoke-RestMethod -Uri https://<YOUR_IP>/catalog/version -SkipCertificateCheck
    Start-Sleep -Seconds 1
}
```

Spring Boot's `server.shutdown: graceful` configuration ensures all in-flight requests on the outgoing deployment complete before the JVM exits. k6 load tests confirmed **0 HTTP failures** across the selector switch.

---

## Security

Security is implemented as **Defence in Depth** across two independent layers:

### Layer 1 — API Gateway

- All external traffic enters through NGINX Ingress and is forwarded to the API Gateway.
- The gateway validates JWTs against Keycloak's JWK endpoint.
- On success, it injects `X-User-Id` and `X-User-Role` headers into downstream requests via `SecurityHeaderFilter`.
- RBAC is enforced at the URL-matcher level:
  - `GET /catalog/**` — public
  - `POST/PUT/PATCH/DELETE /catalog/**` — `ROLE_admin` only
  - `/cart/**` — authenticated users only

### Layer 2 — Individual Services

Each backend service independently re-validates the JWT. Pod-to-pod traffic that bypasses the gateway is rejected at the service level. The Catalog Service and Cart Service each run their own `SecurityConfig` with `JwtAuthenticationConverter` and a `KeycloakRoleConverter`.

### Cart Isolation

Cart operations are scoped to the authenticated `X-User-Id` header. A user attempting to access another user's cart receives a `403 Forbidden`. The isolation is enforced at the database query level, not merely at the access-control rule level.

### Keycloak Configuration

The full realm configuration (roles, clients, redirect URIs) is exported to `realm-export.json` and mounted into the Keycloak pod at startup via a ConfigMap volume — fully reproducible and version-controlled.

---

## Observability

The system uses **Azure Managed Prometheus** and **Azure Managed Grafana**, which scrape and store metrics outside the cluster to avoid distorting load-test CPU readings.

- All services expose `/actuator/prometheus` via the Micrometer Prometheus registry.
- The HPA relies on the metrics server for pod-level CPU data.
- Liveness probes use `/actuator/health/liveness` (restarts the pod on failure).
- Readiness probes use `/actuator/health/readiness` (removes the pod from the service endpoint list without restarting it).

Probe timing is configured to accommodate JVM warm-up:

```yaml
livenessProbe:
  initialDelaySeconds: 60
  periodSeconds: 15
readinessProbe:
  initialDelaySeconds: 45
  periodSeconds: 20
```

---

## API Documentation

Swagger UI is available via the API Gateway at:

```
https://<YOUR_IP>/swagger-ui
```

Individual service docs (accessible locally):

| Service | URL |
|---|---|
| Catalog Service | http://localhost:8081/swagger-ui.html |
| Cart Service | http://localhost:8082/swagger-ui.html |
| API Gateway (aggregated) | http://localhost:9000/swagger-ui |

---

## Team

| Name | Role | Contribution |
|---|---|---|
| Paritosh Pundlik Pal | Backend, Frontend, DevOps, Reporting | 20% |
| Karthick Keeranagere Krishnaiah | Infrastructure, Observability, Reporting | 19% |
| Yazhini Vasudevan | Frontend, Testing, Reporting | 16% |
| Chhagan Gavit | DevOps, Reporting | 15% |
| Chaithanya Virupaksha | Frontend, Reporting | 15% |
| Nishika Gharde | DevOps, Reporting | 15% |

---

## Repository

[https://github.com/paritosh4495/CSC8113_CloudComputing_Bookstore](https://github.com/paritosh4495/CSC8113_CloudComputing_Bookstore)

---

*CSC8113 Cloud Computing Module · Newcastle University · 2026*
