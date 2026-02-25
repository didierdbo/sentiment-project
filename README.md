# Sentiment Analysis Platform

A production-ready sentiment analysis platform built with microservices architecture and deployed on Kubernetes.

## Architecture

```
Client → Ingress → API Service (Java/Spring Boot)
                        ├── ML Service (Python/Flask + DistilBERT)
                        ├── PostgreSQL  (persistence)
                        └── Redis       (caching)

Observability: Prometheus + Grafana
Autoscaling:   HPA on API and ML services
```

## Services

### ML Service — `ml-service/`
- **Model:** `distilbert-base-uncased-finetuned-sst-2-english` (HuggingFace Transformers)
- **API:** `POST /predict` → `{ text, sentiment, confidence }`
- **Health/Readiness probes:** `/health`, `/ready`
- **Stack:** Python, Flask, Transformers

### API Service — `api-service/`
- REST API exposing sentiment analysis to clients
- Caches results in **Redis** to avoid redundant model inference
- Persists history in **PostgreSQL** (Spring Data JPA)
- Exposes **Prometheus metrics** via Micrometer + Spring Actuator
- **Stack:** Java 21, Spring Boot 3.2, Gradle

## Kubernetes — `k8s/`

| Component | Description |
|---|---|
| `api-service/` | Deployment + Service for the Spring Boot API |
| `ml-service/` | Deployment + Service for the Flask ML server |
| `postgres/` | StatefulSet + PVC for PostgreSQL |
| `redis/` | Deployment for Redis cache |
| `ingress/` | NGINX Ingress routing |
| `hpa/` | Horizontal Pod Autoscaler for both services |
| `prometheus/` | Metrics scraping configuration |
| `grafana/` | Dashboards for observability |
| `namespace/` | Namespace isolation |

## Key Design Decisions

- **Redis caching layer:** Identical text inputs skip model inference entirely
- **HPA autoscaling:** ML service scales independently from the API under load
- **Readiness probe on `/ready`:** Kubernetes waits for the model to fully load before routing traffic
- **Micrometer + Prometheus:** API latency, cache hit rate, and JVM metrics exposed out of the box

## Tech Stack

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=flat&logo=springboot&logoColor=white)
![Python](https://img.shields.io/badge/Python-Flask-3776AB?style=flat&logo=python&logoColor=white)
![HuggingFace](https://img.shields.io/badge/HuggingFace-DistilBERT-FFD21F?style=flat)
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=flat&logo=kubernetes&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=flat&logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=flat&logo=grafana&logoColor=white)
