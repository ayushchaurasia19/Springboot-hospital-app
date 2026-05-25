# Hospital Management System API

A Spring Boot monolithic web application for hospital scheduling and administration, featuring role-based JWT authentication, a responsive clinical dashboard, PostgreSQL persistent storage, and built-in OpenTelemetry observability configured for local Kubernetes environments.

---

## Key Features

*   **Role-Based Security**: Stateless JWT-based authentication isolating Receptionist permissions (scheduling, onboarding) from Administrator controls (doctor roster management).
*   **Persistent Storage**: Persistent relational database storage using PostgreSQL with automatic schema updates.
*   **Docker Containerization**: Multi-stage, non-root builder configuration separating JRE runtime execution from the compilation environments.
*   **Unified Observability**: Dynamic JVM runtime instrumentation using the official OpenTelemetry Java Agent capturing structured metrics and distributed transaction traces.
*   **Local Kubernetes Support**: Declarative K8s manifests configured with namespaces, secrets, configmaps, persistent volume claims, and NodePort services.

---

## Project Structure

```
├── Dockerfile                  # Multi-stage production build JRE setup
├── docker-compose.yml          # Container configuration for local testing
├── k8s/                        # Declarative Kubernetes configuration manifests
│   ├── 00-namespace.yaml       # Namespace isolation
│   ├── 01-secrets.yaml         # Encrypted database password
│   ├── 02-configmap.yaml       # Connection URLs and parameters
│   ├── 03-postgres-pvc.yaml    # 1Gi PersistentVolumeClaim for storage
│   ├── 04-postgres.yaml        # PostgreSQL database deployment and cluster service
│   ├── 05-app-deployment.yaml  # Monolith deployment, OTel configuration, and health probes
│   └── 06-observability-stack.yaml # Otel Collector, Prometheus, and Grafana services
└── src/main/resources/
    ├── application.properties  # Dynamic environment fallback parameters
    └── logback-spring.xml      # Console structured logging layout
```

---

## Local Development Setup

To run the application locally without Kubernetes:
1.  **H2 Fallback Database**: If database environment variables are omitted, the application automatically defaults to an in-memory H2 database instance (`jdbc:h2:mem:hospital`) for quick testing.
2.  **Local PostgreSQL**: Start the persistent containerized database:
    ```bash
    docker compose up -d
    ```

---

## Kubernetes Deployment Guide

To build, load, and deploy the entire monolithic system inside a local Kubernetes cluster, follow these steps sequentially:

### Step 1: Build the Container Image
Compile the source code and build the secure production JRE image:
```bash
docker build -t hospital-app:latest .
```

### Step 2: Load the Image Into the Cluster
Load the built image into your local Kubernetes engine:
*   **Docker Desktop**: Shares your host image registry automatically. No action is required.
*   **Minikube**:
    ```bash
    minikube image load hospital-app:latest
    ```
*   **K3s**:
    ```bash
    docker save hospital-app:latest | sudo k3s ctr images import -
    ```
*   **Kind**:
    ```bash
    kind load docker-image hospital-app:latest
    ```

### Step 3: Roll Out Workloads
Deploy all services, databases, and monitoring systems:
```bash
kubectl apply -f k8s/
```

### Step 4: Monitor Pod Rollout
Verify that all workloads start successfully:
```bash
# Verify application and database pods
kubectl get pods -n hospital-app

# Verify collector, metrics, and visualization pods
kubectl get pods -n hospital-observability
```
Ensure all pods transition to `Running` and display `READY: 1/1` before proceeding.

---

## Access Ports Matrix

Once deployed, the following endpoints are exposed on the host:

| Service / Interface | URL | Access Type | Purpose |
| :--- | :--- | :--- | :--- |
| **Hospital Application UI** | `http://localhost:30080` | NodePort | Patient scheduling and admin operations |
| **Prometheus Dashboard** | `http://localhost:30090` | NodePort | Scraping OTel and Spring Actuator metrics |
| **Grafana Dashboard** | `http://localhost:32000` | NodePort | Unified telemetry query and visualization |

*Note: Grafana has anonymous organization Admin permissions enabled for instant, credential-free dashboard usage.*

---

## Verification Commands

### 1. Verify Telemetry Instrumentation
Inspect logs on the application pod to ensure the OpenTelemetry Java Agent successfully initialized on startup:
```bash
kubectl logs -l app=hospital-app -n hospital-app --tail=100 -f
```

### 2. Verify Database Persistence
Verify database contents inside the running PostgreSQL deployment:
```bash
kubectl exec -it deployment/postgres-db -n hospital-app -- psql -U postgres -d hospital_db -c "\dt"
```

### 3. Check OTel Collector Pipelines
Inspect the runtime log records parsed by the OpenTelemetry Collector:
```bash
kubectl logs -l app=otel-collector -n hospital-observability --tail=50 -f
```
