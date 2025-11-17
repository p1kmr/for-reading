# Summary & Design Checklist

## System Overview

**Amazon Shop** is a large-scale e-commerce platform designed to handle:
- ✅ **100 million** registered users
- ✅ **10 million** daily active users
- ✅ **7,000 requests/sec** peak traffic
- ✅ **5 million orders/day**
- ✅ **99.99% availability** (52 min downtime/year)
- ✅ **500 million** products in catalog
- ✅ **1.6 PB** total storage

---

## Architecture Evolution

### Step 1: Basic Architecture
```
Client → Load Balancer → App Servers (3) → PostgreSQL
```
**Capacity:** 1,000 req/sec, 100K users

---

### Step 2: Add Caching
```
Client → Load Balancer → App Servers (3) → Redis Cache → PostgreSQL
```
**Improvements:**
- 80% cache hit rate
- 70% latency reduction (100ms → 30ms)
- 80% database load reduction

---

### Step 3: Database Scaling
```
App Servers → Cache → Master DB (writes)
                      ↓ replication
                    Slave 1, 2, 3 (reads)
```
**Improvements:**
- 99.99% availability (Multi-AZ)
- 3x read capacity (30,000 QPS)
- Automatic failover (30-60 sec)

---

### Step 4: Message Queue
```
App Servers → Create Order → Database
            ↓
          RabbitMQ → Workers (email, inventory, notifications)
```
**Improvements:**
- 35x faster checkout (3,900ms → 110ms)
- 70% reduced cart abandonment
- Async background processing

---

### Step 5: CDN & Object Storage
```
User → CDN (90% cache hit) → S3 (images, 1.25 PB)
     → Load Balancer → App Servers
```
**Improvements:**
- 7.5x faster image loading (150ms → 20ms)
- 75% bandwidth cost reduction
- Global performance (20ms worldwide)

---

### Step 6: Microservices (Final)
```
API Gateway
├─ Product Service (PostgreSQL)
├─ Order Service (PostgreSQL)
├─ Payment Service (PostgreSQL)
├─ User Service (PostgreSQL)
├─ Inventory Service
└─ Recommendation Service (AI/ML)
```
**Improvements:**
- Independent scaling (scale orders during sales)
- Fault isolation (payment down ≠ browsing down)
- Team autonomy (6 teams, 1 service each)

---

## Key Design Decisions

### 1. SQL vs NoSQL

**Chose SQL (PostgreSQL) for core data:**
- ✅ ACID transactions (orders, payments)
- ✅ Complex queries (filter, join, aggregate)
- ✅ Referential integrity (foreign keys)
- ✅ Scale reads with replicas (30,000 QPS)

**Use NoSQL for specific cases:**
- User activity logs (Elasticsearch)
- Real-time analytics (Cassandra, ClickHouse)
- Event streaming (Kafka)

---

### 2. Monolith vs Microservices

**Started with Monolith (0-100K users):**
- ✅ Fast development
- ✅ Simple deployment
- ✅ Low complexity

**Migrated to Microservices (1M+ users):**
- ✅ Independent scaling
- ✅ Fault isolation
- ✅ Team autonomy
- ⚠️ Higher complexity

---

### 3. Synchronous vs Asynchronous

**Synchronous (REST API):**
- Order placement → Payment processing
- Product search → Database query
- User login → Session creation

**Asynchronous (Message Queue):**
- Email notifications
- Inventory updates
- Analytics tracking
- Recommendation updates

---

### 4. RabbitMQ vs Kafka

**RabbitMQ:** Task queues (email, notifications)
- Low latency (< 10ms)
- Simple API

**Kafka:** Event streaming (analytics, logs)
- High throughput (1M msg/sec)
- Message retention (replay events)

---

### 5. Caching Strategy

**Multi-Level Caching:**
```
L1 (Browser): Static assets (CSS, JS) - Cache-Control: max-age=31536000
L2 (CDN): Images - Cache-Control: max-age=86400
L3 (Redis): Products, search results - TTL: 5-60 min
L4 (App memory): Hot data (top 1000 products) - LRU cache
```

---

## Capacity Planning Summary

### Traffic Estimates

| Metric | Average | Peak (3x) |
|--------|---------|-----------|
| **Requests/sec** | 2,315 | 7,000 |
| **Reads/sec** | 1,852 | 5,600 |
| **Writes/sec** | 463 | 1,400 |
| **Bandwidth** | 1.8 GB/sec | 5.3 GB/sec |
| **Bandwidth (with CDN)** | 180 MB/sec | 530 MB/sec |

### Storage Estimates

| Data Type | Size |
|-----------|------|
| User data | 200 GB |
| Product catalog | 2.5 TB |
| Product images | 1.25 PB |
| Orders (5 years) | 27 TB |
| Reviews (5 years) | 1 TB |
| **Total** | **~1.6 PB** |

### Cost Estimates (Monthly)

| Component | Cost |
|-----------|------|
| App servers (90) | $45,000 |
| Databases (master + replicas) | $25,000 |
| Redis cluster | $5,000 |
| RabbitMQ cluster | $2,000 |
| S3 storage | $31,000 |
| CDN bandwidth | $247,000 |
| **Total** | **~$355,000/month** |

---

## What to Draw First (Interview Checklist)

### Priority 1: Core Architecture (5 minutes)

```
✅ User → Load Balancer → App Servers → Database
✅ Label each component (Nginx, Node.js, PostgreSQL)
✅ Show data flow (arrows)
```

### Priority 2: Scalability (5 minutes)

```
✅ Add Redis cache (show cache-aside pattern)
✅ Add database replication (master-slave)
✅ Add read replicas (3)
✅ Explain: "80% cache hit, 80% load reduction"
```

### Priority 3: High Availability (3 minutes)

```
✅ Multi-AZ deployment (primary + standby)
✅ Automatic failover (30-60 sec)
✅ Health checks (load balancer → app servers)
```

### Priority 4: Async Processing (3 minutes)

```
✅ Add message queue (RabbitMQ)
✅ Add background workers
✅ Show async flow (order → queue → email worker)
```

### Priority 5: CDN & Storage (2 minutes)

```
✅ Add CDN layer (CloudFront)
✅ Add S3 object storage
✅ Show cache hit flow (CDN → S3)
```

### Priority 6: Microservices (if time permits)

```
✅ Break into services (Product, Order, Payment, User)
✅ Show inter-service communication (REST, message queue)
✅ Show separate databases per service
```

---

## Key Metrics to Mention

### Performance Metrics

```
✅ API latency: p50 < 100ms, p95 < 500ms
✅ Search latency: < 300ms
✅ Checkout latency: < 2 seconds
✅ Image load time: < 500ms (p95)
```

### Scalability Metrics

```
✅ Can handle 7,000 req/sec (peak)
✅ Can scale to 70,000 req/sec (10x growth)
✅ Database: 30,000 reads/sec, 10,000 writes/sec
✅ Cache hit rate: 80-90%
```

### Availability Metrics

```
✅ 99.99% uptime (52 min downtime/year)
✅ Automatic failover: 30-60 seconds
✅ No single point of failure
✅ Multi-region deployment (disaster recovery)
```

---

## Trade-offs Accepted

### 1. Eventual Consistency for Non-Critical Data

**Trade-off:**
- Product reviews may take 1-2 seconds to appear (eventual consistency)
- But: Orders and payments are immediately consistent (ACID)

**Justification:**
- 10x better performance (NoSQL for reviews)
- Reviews are not critical path

---

### 2. Async Processing Increases Complexity

**Trade-off:**
- Message queue adds complexity (monitoring, retries, DLQ)
- But: 35x faster checkout (3,900ms → 110ms)

**Justification:**
- User experience improvement worth the complexity
- Decouple services (email down ≠ orders blocked)

---

### 3. Microservices Increase Operational Overhead

**Trade-off:**
- 6 services vs 1 monolith (harder to deploy, monitor)
- But: Independent scaling, fault isolation

**Justification:**
- At 1M+ users, monolith can't scale
- Team autonomy (6 teams work independently)

---

### 4. CDN Cost vs Performance

**Trade-off:**
- CDN costs $247K/month (expensive!)
- But: 7.5x faster image loading, global performance

**Justification:**
- Better user experience = higher conversion rate
- Conversion improvement pays for CDN cost

---

## Final Checklist

### ✅ Functional Requirements Covered

- ✅ Product catalog (browse, search, filters)
- ✅ Shopping cart (add, remove, update)
- ✅ Order management (place, track, history)
- ✅ Payment processing (multiple methods)
- ✅ User management (register, login, profile)
- ✅ Recommendations (AI/ML)

### ✅ Non-Functional Requirements Met

- ✅ Scalability: 7,000 req/sec (can scale to 70,000)
- ✅ Availability: 99.99% (Multi-AZ, automatic failover)
- ✅ Performance: < 500ms API latency (p95)
- ✅ Consistency: ACID for orders/payments
- ✅ Reliability: Automatic retries, DLQ
- ✅ Security: TLS, JWT, bcrypt, PCI DSS

### ✅ Components Designed

- ✅ Load balancer (Nginx)
- ✅ App servers (Node.js, auto-scaling)
- ✅ Database (PostgreSQL, master + 3 replicas)
- ✅ Cache (Redis cluster)
- ✅ Message queue (RabbitMQ)
- ✅ CDN (CloudFront)
- ✅ Object storage (S3)
- ✅ API Gateway

### ✅ Data Flows Documented

- ✅ Product search flow
- ✅ Order placement flow
- ✅ Payment processing flow
- ✅ Image upload flow
- ✅ Recommendation flow

### ✅ Failure Scenarios Handled

- ✅ Database failover (30-60 sec)
- ✅ Payment service retry (exponential backoff)
- ✅ Message queue DLQ (failed messages)
- ✅ Circuit breaker (prevent cascade failures)
- ✅ Graceful degradation (fallback to cache)

---

## One-Page Summary

**Amazon Shop** is a highly scalable e-commerce platform built with:
- **Microservices architecture** (6 services)
- **PostgreSQL** for ACID compliance
- **Redis** for 80% cache hit rate
- **RabbitMQ** for async processing
- **CloudFront CDN** for global performance
- **Multi-AZ deployment** for 99.99% availability

**Key achievements:**
- ✅ Handles 7,000 req/sec (10M DAU)
- ✅ 99.99% uptime (52 min/year downtime)
- ✅ Sub-500ms API latency
- ✅ PCI DSS compliant
- ✅ Can scale to 70,000 req/sec (10x growth)

**Technologies:**
- Node.js (app servers)
- PostgreSQL (databases)
- Redis (cache)
- RabbitMQ (message queue)
- Kafka (event streaming)
- AWS S3 (object storage)
- CloudFront (CDN)
- Kubernetes (orchestration)
- Prometheus + Grafana (monitoring)

---

**Document Version**: 1.0
**Last Updated**: 2025-11-17
**Previous**: [14_beginner_mistakes.md](./14_beginner_mistakes.md)
**Next**: [README.md](./README.md)
