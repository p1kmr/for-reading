# Uber HLD - Summary & Design Checklist

## One-Page Summary

### System Overview
**Platform:** Ride-hailing service connecting riders with drivers
**Scale:** 20M daily active users, 180K requests/sec peak, 1.32M location updates/sec
**Target:** 99.99% availability, < 200ms latency

### Key Design Decisions

| Component | Technology | Reason |
|-----------|------------|--------|
| **Architecture** | Microservices | Independent scaling, fault isolation |
| **API Gateway** | Kong / AWS API Gateway | Rate limiting, authentication, routing |
| **Cache** | Redis Cluster (7 nodes) | 1.32M location updates/sec, geospatial queries |
| **Database** | PostgreSQL (4 shards) | ACID for payments, PostGIS for geospatial |
| **Message Queue** | Apache Kafka | 1.5M events/sec, event-driven architecture |
| **CDN** | CloudFlare | 70% bandwidth savings, DDoS protection |
| **Object Storage** | AWS S3 Multi-region | Driver photos, documents, receipts |
| **Monitoring** | Prometheus + Grafana | Real-time metrics, alerting |
| **Orchestration** | Kubernetes | Auto-scaling, self-healing |

### Architecture Evolution

**Step 1: Basic Architecture**
- Client → Load Balancer → App Servers → PostgreSQL
- **Bottleneck:** Database can't handle 1.32M location updates/sec

**Step 2: Add Caching**
- Redis for driver locations (99% reduction in DB load)
- **Improvement:** 10ms latency (vs 100ms), 10x faster

**Step 3: Database Scaling**
- Geographic sharding (US, Europe, Asia)
- Master-slave replication for high availability
- **Improvement:** 25x lower latency for global users

**Step 4: Event-Driven (Kafka)**
- Async notifications, payments, analytics
- **Improvement:** 7x faster API responses (105ms vs 750ms)

**Step 5: CDN & Object Storage**
- Map tiles cached at edge
- Driver photos in S3 (not database)
- **Improvement:** 70% cost savings on bandwidth

**Step 6: Microservices**
- Split into User, Driver, Ride, Payment, Notification services
- **Improvement:** Independent scaling, faster development

### Key Metrics Achieved

| Metric | Target | Achieved |
|--------|--------|----------|
| Daily Active Users | 20M | ✅ 25M capacity |
| API Latency (P95) | < 200ms | ✅ 150ms |
| Availability | 99.99% | ✅ 99.99% (52 min/year downtime) |
| Location Updates/sec | 1.32M | ✅ 2M capacity (Redis) |
| Database Queries/sec | 40K (peak) | ✅ 42K capacity (4 shards) |
| Cost | Reasonable | ✅ $108K/month (0.13% of $1B revenue) |

### Trade-Offs Accepted

| Decision | Pro | Con | Verdict |
|----------|-----|-----|---------|
| Geographic Sharding | Low latency, fault isolation | Complex cross-shard queries | ✅ Worth it |
| Eventual Consistency (Locations) | High throughput | 5-sec stale data | ✅ Acceptable |
| Microservices | Independent scaling | Operational complexity | ✅ At scale, worth it |
| Kafka over RabbitMQ | Higher throughput (1.5M msg/sec) | More complex setup | ✅ Need the throughput |
| PostgreSQL over MongoDB | ACID for payments | Harder to scale writes | ✅ Payments need ACID |
| CloudFlare over CloudFront | Flat pricing, better DDoS | Fewer edge locations | ✅ Cost savings |

---

## Design Checklist (Interview Whiteboard)

### Phase 1: Requirements (5 minutes)

- [ ] **Functional Requirements**
  - [ ] Ride request & matching
  - [ ] Real-time tracking
  - [ ] Payment processing
  - [ ] Ratings system

- [ ] **Non-Functional Requirements**
  - [ ] Scale: 20M DAU, 180K req/sec peak
  - [ ] Latency: < 200ms API, < 10ms location updates
  - [ ] Availability: 99.99%
  - [ ] Consistency: Strong for payments, eventual for locations

### Phase 2: Capacity Estimation (5 minutes)

- [ ] **Traffic Estimates**
  - [ ] Rides/sec: 231 avg, 693 peak
  - [ ] Location updates: 1.32M/sec
  - [ ] API requests: 60K avg, 180K peak
  - [ ] Read:Write ratio: 85:15

- [ ] **Storage Estimates**
  - [ ] User data: 50 GB
  - [ ] Ride data (5 years): 22 TB
  - [ ] Location history (30 days): 110 TB
  - [ ] Total: ~600 TB (with buffer)

- [ ] **Bandwidth Estimates**
  - [ ] Peak: 8.22 Gbps
  - [ ] Monthly: 2.67 PB

### Phase 3: High-Level Design (15 minutes)

- [ ] **Draw Basic Architecture First**
  - [ ] Clients → Load Balancer → App Servers → Database
  - [ ] Identify bottleneck: DB can't handle 1.32M writes/sec

- [ ] **Add Caching Layer**
  - [ ] Redis for driver locations
  - [ ] Geospatial queries (GEORADIUS)
  - [ ] TTL: 10 seconds

- [ ] **Database Scaling**
  - [ ] Master-slave replication (high availability)
  - [ ] Geographic sharding (US, Europe, Asia)
  - [ ] Read replicas (scale reads)

- [ ] **Event-Driven Architecture**
  - [ ] Kafka for async operations
  - [ ] Topics: ride.created, payment.completed
  - [ ] Decouple services

- [ ] **CDN & Storage**
  - [ ] CloudFlare for map tiles
  - [ ] S3 for driver photos
  - [ ] 70% cost savings

### Phase 4: Deep Dive (15 minutes)

- [ ] **Driver-Rider Matching Algorithm**
  - [ ] GEORADIUS to find drivers within 5 km
  - [ ] Scoring: (1/distance) × rating
  - [ ] Atomic assignment (database transaction)

- [ ] **Surge Pricing**
  - [ ] Kafka Streams (1-min window)
  - [ ] Formula: min(demand/supply, 3.0)
  - [ ] Cache in Redis (2-min TTL)

- [ ] **Payment Processing**
  - [ ] Async via Kafka
  - [ ] Retry with exponential backoff
  - [ ] Dead letter queue for failures
  - [ ] ACID transactions (PostgreSQL)

- [ ] **Real-Time Tracking**
  - [ ] WebSocket connection
  - [ ] Driver location updates (4 sec interval)
  - [ ] Rider polls location (5 sec interval)

### Phase 5: Scalability & Reliability (5 minutes)

- [ ] **Horizontal Scaling**
  - [ ] Stateless app servers (Kubernetes HPA)
  - [ ] Auto-scale: CPU > 70% → add pods

- [ ] **High Availability**
  - [ ] No single point of failure
  - [ ] Automatic failover (< 30 sec)
  - [ ] Multi-region deployment

- [ ] **Failure Handling**
  - [ ] Circuit breakers (prevent cascade failures)
  - [ ] Retry logic (exponential backoff)
  - [ ] Graceful degradation

- [ ] **Monitoring**
  - [ ] Prometheus for metrics
  - [ ] Grafana dashboards
  - [ ] PagerDuty alerts

### Phase 6: Discuss Trade-Offs (5 minutes)

- [ ] **SQL vs NoSQL**: PostgreSQL for ACID compliance
- [ ] **Sync vs Async**: Async for notifications (better UX)
- [ ] **Consistency**: Eventual for locations (acceptable 5-sec lag)
- [ ] **Cost**: $108K/month for 20M DAU (0.13% of revenue)

---

## What to Draw First (Priority Order)

### Must Draw (Do These First)

1. **Basic Architecture** (2 minutes)
   ```
   [Clients] → [Load Balancer] → [App Servers] → [Database]
   ```

2. **Traffic Estimates** (1 minute)
   ```
   DAU: 20M
   Peak: 180K req/sec
   Locations: 1.32M updates/sec
   ```

3. **Caching Layer** (2 minutes)
   ```
   [App Servers] → [Redis] → [Database]
   Cache hit: 90%+ → 10x faster
   ```

### Should Draw (If Time Permits)

4. **Database Sharding** (3 minutes)
   ```
   [US Shard] [Europe Shard] [Asia Shard]
   Each: Master + 2 Replicas
   ```

5. **Message Queue** (3 minutes)
   ```
   [Ride Service] → [Kafka] → [Payment Service]
                            → [Notification Service]
   ```

6. **Sequence Diagram** (5 minutes)
   ```
   User → API → Ride Service → Redis → Database
   ```

### Nice to Have (Advanced Topics)

7. **Microservices Breakdown** (3 minutes)
8. **CDN & Storage** (2 minutes)
9. **Monitoring & Alerting** (2 minutes)

---

## Technology Stack Summary

### Frontend
- React Native (iOS & Android)
- React.js (Web)
- WebSockets (Real-time tracking)

### Backend
- **Language:** Node.js, Java Spring Boot, Go, Python
- **API Gateway:** Kong / AWS API Gateway
- **Authentication:** OAuth 2.0, JWT

### Data Layer
- **Database:** PostgreSQL 15 (sharded, replicated)
- **Cache:** Redis Cluster (7 nodes)
- **Search:** Elasticsearch
- **Analytics:** Snowflake Data Warehouse

### Messaging & Streaming
- **Message Queue:** Apache Kafka
- **Stream Processing:** Kafka Streams
- **Real-time:** WebSockets, Server-Sent Events

### Storage & CDN
- **Object Storage:** AWS S3 (multi-region)
- **CDN:** CloudFlare
- **Backups:** AWS S3 Glacier

### Infrastructure
- **Orchestration:** Kubernetes
- **CI/CD:** Jenkins, GitHub Actions
- **Load Balancing:** Nginx, AWS ALB

### Observability
- **Metrics:** Prometheus
- **Dashboards:** Grafana
- **Logging:** ELK Stack (Elasticsearch, Logstash, Kibana)
- **Tracing:** Jaeger
- **Alerting:** PagerDuty

### External Services
- **Maps:** Google Maps API
- **Payments:** Stripe
- **SMS:** Twilio
- **Email:** SendGrid
- **Push Notifications:** Firebase Cloud Messaging

---

## Cost Breakdown Summary

| Category | Monthly Cost |
|----------|--------------|
| Compute (Kubernetes) | $24,000 |
| Databases (4 shards) | $36,000 |
| Cache (Redis) | $3,000 |
| Kafka Cluster | $3,000 |
| CDN (CloudFlare) | $2,000 |
| Object Storage (S3) | $2,300 |
| Bandwidth | $8,000 |
| Monitoring | $5,000 |
| External APIs | $15,000 |
| Data Warehouse | $10,000 |
| **Total** | **~$108,000/month** |

**Business Context:**
- Annual infrastructure cost: $1.3M
- If revenue is $1B/year: 0.13% of revenue
- Industry benchmark: 5-15% for tech companies
- **Verdict:** Extremely efficient ✅

---

## Key Insights for Interviewers

### What Makes This Design Good?

1. **Incremental Approach:** Started simple, added complexity based on requirements
2. **Numbers-Driven:** All decisions backed by calculations
3. **Trade-offs Explained:** No perfect solution, justified choices
4. **Real Technologies:** Specific tools (not just "database", "cache")
5. **Failure Planning:** Replication, failover, circuit breakers
6. **Cost-Aware:** Estimated infrastructure costs vs revenue

### Common Interview Questions You Can Answer

- "How do you handle 1M+ location updates/sec?" → Redis geospatial
- "How do you ensure high availability?" → Replication, multi-region
- "SQL or NoSQL?" → PostgreSQL for ACID (payments need it)
- "How do you scale databases?" → Sharding + read replicas
- "How do you handle sudden traffic spikes?" → Auto-scaling, caching
- "What's your caching strategy?" → Cache-aside, TTL, geo-distributed
- "How do you prevent single point of failure?" → Replication everywhere
- "What about monitoring?" → Prometheus, Grafana, PagerDuty

---

## Final Checklist Before Interview

- [ ] Memorize key numbers (20M DAU, 180K req/sec, 1.32M locations/sec)
- [ ] Practice drawing architecture diagram in 5 minutes
- [ ] Know why each technology was chosen (Redis vs Memcached, etc.)
- [ ] Understand trade-offs (consistency vs availability)
- [ ] Calculate costs (show business thinking)
- [ ] Prepare for deep-dive questions (surge pricing, matching algorithm)
- [ ] Know failure scenarios and recovery strategies
- [ ] Review sequence diagrams (ride booking flow)

**Remember:**
- Start with requirements, not solutions
- Draw simple architecture first, then add complexity
- Explain your thought process (thinking > perfect answer)
- Ask clarifying questions (shows collaboration)
- Discuss trade-offs (there's no perfect design)

Good luck! 🚀
