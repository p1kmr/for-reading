# YouTube System Design - High-Level Design (HLD)

> **A Comprehensive, Beginner-Friendly Guide to Designing a Large-Scale Video Streaming Platform**

This repository contains a complete, step-by-step guide to designing YouTube from scratch, covering everything from basic architecture to advanced microservices, with detailed capacity calculations, trade-offs, and real-world examples.

---

## 🎯 What You'll Learn

By following this guide, you'll understand:

✅ How to design systems that scale to **2.5 billion users**
✅ How to handle **470 Petabytes/day** of video streaming
✅ Database sharding, replication, and scaling strategies
✅ CDN architecture and how it saves **$11 billion/year**
✅ Microservices architecture with 10+ services
✅ Message queues for async processing (Kafka)
✅ Security, monitoring, and observability
✅ How to ace system design interviews

---

## 📚 Learning Path (Recommended Order)

### **Phase 1: Understanding the Problem**
Start here to understand YouTube's scale and requirements.

**📖 [01. Requirements & Capacity Estimations](./01_requirements_and_estimations.md)**
- Functional requirements (upload, stream, search, comment, subscribe)
- Non-functional requirements (scalability, availability, latency, durability)
- **Traffic estimates:** 500M DAU, 2.5B views/day, 87k req/sec peak
- **Storage estimates:** 1 Exabyte total, 25.8 TB/day growth
- **Bandwidth estimates:** 470 PB/day egress, $8.6B/year cost
- **Key numbers to remember**

---

### **Phase 2: Building the Foundation (Steps 1-3)**

**📖 [02. Step 1: Basic Architecture](./02_step1_basic_architecture.md)**
- Start simple: Client → Load Balancer → App Servers → Database
- Components: Nginx, Node.js, PostgreSQL
- Request flows (upload, streaming)
- Limitations and why it doesn't scale

**📖 [03. Step 2: Add Caching Layer](./03_step2_add_caching.md)**
- Redis cache cluster (95% hit rate)
- Cache-aside pattern, write-through, write-behind
- What to cache (metadata, sessions, view counts, trending)
- Cache invalidation strategies (TTL, active invalidation)
- **Performance:** 100ms → 10ms latency (10x faster!)
- Capacity planning: 320 GB Redis cluster

**📖 [04. Step 3: Database Scaling](./04_step3_database_scaling.md)**
- Master-slave replication (1 master + 3 slaves)
- Read/write splitting
- Database sharding (4 shards by user_id or video_id)
- Consistent hashing for re-sharding
- Failover and high availability
- **Capacity:** 40k queries/sec (4x improvement)

---

### **Phase 3: Optimizing for Scale (Steps 4-5)**

**📖 [05. Step 4: Add Message Queue & Async Processing](./05_step4_add_message_queue.md)**
- Apache Kafka for async tasks
- Background workers (24,000 transcode workers)
- Video transcoding pipeline (FFmpeg)
- Retry logic with exponential backoff
- Dead letter queue (DLQ)
- **Performance:** Upload latency 36 min → 2 sec (1080x faster!)
- Cost optimization: Spot instances, parallel transcoding

**📖 [06. Step 5: CDN & Object Storage](./06_step5_cdn_and_storage.md)**
- Google Cloud CDN (420 PoPs worldwide)
- AWS S3 object storage (1 Exabyte, 11 nines durability)
- Adaptive bitrate streaming (HLS/DASH)
- Storage lifecycle policies (S3 Standard → Glacier)
- Multi-region replication
- **Cost savings:** $15.4B → $4B/year (73% reduction!)
- **Latency:** 200ms → 50ms (4x faster globally)

---

### **Phase 4: Advanced Architecture (Step 6)**

**📖 [07. Step 6: Microservices & Final Architecture](./07_step6_final_microservices_architecture.md)**
- 10 microservices (Upload, Stream, Search, User, Comment, Analytics, etc.)
- Service-to-service communication (REST, gRPC, Kafka)
- API Gateway (Kong) for routing, auth, rate limiting
- Service discovery (Consul)
- Circuit breaker pattern (Hystrix)
- Distributed tracing (Jaeger)
- Kubernetes deployment (auto-scaling, self-healing)
- **Capacity:** 200k req/sec total across all services

---

### **Phase 5: Deep Dive (Technical Details)**

**📖 [08. API Design, Database Schema & Data Flow](./08_api_database_dataflow.md)**
- **RESTful API design:**
  - Video API (upload, get, update, delete)
  - User API (register, login, profile, subscribe)
  - Comment API (create, reply, like)
  - Search API (full-text search, autocomplete)
  - Analytics API (record view, get stats)
  - JWT authentication, rate limiting, pagination
- **Database schemas:**
  - PostgreSQL (users, videos, channels, subscriptions)
  - MongoDB (comments with nested replies)
  - Elasticsearch (search index mapping)
  - Cassandra (time-series analytics)
- **Sequence diagrams:**
  - Video upload flow
  - Video streaming flow
  - Search flow
  - Comment flow

---

### **Phase 6: Security, Monitoring & Best Practices**

**📖 [09. Security, Monitoring, Trade-offs & Common Mistakes](./09_security_monitoring_tradeoffs.md)**
- **Security Architecture:**
  - Authentication (JWT, OAuth 2.0)
  - Encryption (AES-256 at rest, TLS 1.3 in transit)
  - DRM (Widevine, FairPlay)
  - Rate limiting & DDoS protection
  - Content moderation & copyright protection
- **Monitoring & Observability:**
  - Metrics (latency, error rate, throughput, saturation)
  - Logging (ELK stack)
  - Distributed tracing (Jaeger)
  - Alerting (PagerDuty, Slack)
  - SLA/SLO definitions (99.99% uptime)
- **Trade-offs:**
  - SQL vs NoSQL
  - Monolith vs Microservices
  - Sync vs Async
  - Strong Consistency vs Eventual Consistency
- **Common Mistakes:**
  - Not considering scalability upfront
  - Over-engineering
  - Not planning for failures
  - Poor database indexing

---

### **Phase 7: Interview Preparation**

**📖 [10. Interview Q&A & Summary](./10_interview_qa_summary.md)**
- **20 Interview Questions** with detailed answers:
  - How to scale to billions of users?
  - Video transcoding at scale?
  - CDN architecture?
  - Database sharding strategy?
  - Handling viral videos?
  - Search implementation?
  - Recommendation system?
  - Disaster recovery?
  - Cost optimization?
- **Design Summary:**
  - Key decisions recap
  - Technologies chosen (with justifications)
  - Architecture evolution (MVP → Final)
  - Cost breakdown ($15B annually)
- **Whiteboard Interview Checklist:**
  - What to draw first (time-boxed guide)
  - Priority 1-4 components
- **Key Takeaways for Beginners**

---

## 🚀 Quick Start for Interviews

If you have **limited time** before an interview:

### 30-Minute Crash Course:
1. Read [01. Requirements](./01_requirements_and_estimations.md) - Understand scale (10 min)
2. Read [02. Basic Architecture](./02_step1_basic_architecture.md) - Draw this first! (10 min)
3. Read [10. Interview Q&A](./10_interview_qa_summary.md) - Practice questions (10 min)

### 2-Hour Deep Dive:
1. Requirements (15 min)
2. Steps 1-3: Basic → Caching → DB Scaling (45 min)
3. Steps 4-5: Message Queue → CDN (30 min)
4. Interview Q&A (30 min)

### Full Study (1 Week):
- Day 1: Requirements + Basic Architecture
- Day 2: Caching + DB Scaling
- Day 3: Message Queue + CDN
- Day 4: Microservices
- Day 5: API + Database Design
- Day 6: Security + Monitoring + Trade-offs
- Day 7: Interview Q&A + Practice

---

## 📊 System Overview at a Glance

### Final Architecture Highlights

| Component | Technology | Purpose | Scale |
|-----------|-----------|---------|-------|
| **CDN** | Google Cloud CDN | Edge caching | 420 PoPs, 95% hit rate |
| **API Gateway** | Kong | Routing, auth, rate limiting | 200k req/sec |
| **Upload Service** | Go | Handle video uploads | 50 uploads/sec |
| **Streaming Service** | Nginx | Serve video playback | 87k req/sec |
| **Search Service** | Node.js + Elasticsearch | Full-text search | 10k queries/sec |
| **User Service** | Java Spring Boot | Auth, profiles, subscriptions | 5k req/sec |
| **Comment Service** | Node.js + MongoDB | Comments, likes | 1k writes/sec |
| **Analytics Service** | Python + Spark + Cassandra | View tracking, metrics | 100k events/sec |
| **Recommendation** | Python + TensorFlow | ML-based suggestions | 20k req/sec |
| **Cache** | Redis Cluster | Metadata, sessions | 5 nodes, 320 GB |
| **Message Queue** | Apache Kafka | Async tasks | 10-node cluster |
| **Transcode Workers** | Python + FFmpeg | Video encoding | 24,000 workers |
| **Primary DB** | PostgreSQL (sharded) | User, video metadata | 4 shards, 12 servers |
| **Search DB** | Elasticsearch | Full-text search | 10-node cluster |
| **Analytics DB** | Cassandra | Time-series data | Distributed |
| **Object Storage** | AWS S3 | Videos, thumbnails | 1 Exabyte, multi-region |

---

### System Capacity

| Metric | Value |
|--------|-------|
| **Daily Active Users** | 500 million |
| **Daily Video Views** | 2.5 billion |
| **Peak Requests/Sec** | 200,000 req/sec |
| **Daily Uploads** | 4.3 million videos |
| **Daily Storage Growth** | 25.8 TB |
| **Daily Bandwidth (Egress)** | 470 PB |
| **Read:Write Ratio** | 600:1 (read-heavy) |
| **Target Availability** | 99.99% (52 min downtime/year) |
| **Video Start Latency** | < 1 second |
| **Cache Hit Ratio** | 95% |

---

### Cost Breakdown (Annual)

| Category | Before Optimization | After Optimization | Savings |
|----------|---------------------|-------------------|---------|
| **Bandwidth** | $15.4 billion | $4 billion | 73% |
| **Storage** | $240 million | $124 million | 48% |
| **Compute** | $2.2 billion | $650 million | 70% |
| **Database** | $300 million | $200 million | 33% |
| **Total** | **$18.1 billion** | **$5 billion** | **72%** |

**Key Optimizations:**
- CDN (95% cache hit) → $11.4B saved
- S3 lifecycle policies → $116M saved
- Spot instances for workers → $1.5B saved
- Database sharding + read replicas → $100M saved

---

## 🎓 For Beginners: Start Here

### Prerequisites
- Basic understanding of web applications (HTTP, REST APIs)
- Familiarity with databases (SQL basics)
- Understanding of client-server architecture

### Don't Worry If You Don't Know:
- Advanced database concepts (we explain everything!)
- Kafka, Redis, Elasticsearch (we introduce each technology)
- Microservices, CDN, sharding (that's what you're here to learn!)

### How This Guide Helps You
- ✅ **Incremental learning:** Start simple, add complexity step-by-step
- ✅ **Real calculations:** Actual numbers, not hand-waving
- ✅ **Beginner analogies:** "CDN is like a local bookstore vs. traveling to a library"
- ✅ **Why, not just what:** Every decision is explained
- ✅ **Interview-ready:** Practice questions with model answers

---

## 🛠️ Technologies Covered

### Languages & Frameworks
- **Go** (high-performance I/O for uploads)
- **Java Spring Boot** (enterprise user service)
- **Python** (ML, data processing, workers)
- **Node.js** (async I/O for APIs)

### Databases
- **PostgreSQL** (relational data, ACID transactions)
- **MongoDB** (schema-less, nested comments)
- **Elasticsearch** (full-text search, autocomplete)
- **Cassandra** (time-series analytics)
- **Redis** (caching, sessions)

### Infrastructure
- **Nginx** (load balancer, file streaming)
- **Apache Kafka** (message queue, event streaming)
- **AWS S3** (object storage, videos)
- **Google Cloud CDN** (edge caching)
- **Kubernetes** (container orchestration)
- **Docker** (containerization)

### Monitoring & Security
- **Prometheus + Grafana** (metrics)
- **ELK Stack** (logging)
- **Jaeger** (distributed tracing)
- **JWT** (authentication)
- **OAuth 2.0** (third-party login)
- **TLS 1.3** (encryption)
- **Widevine/FairPlay** (DRM)

---

## 🧩 Architecture Evolution Timeline

```
Step 1: Basic MVP
  Client → Load Balancer → App Server → Database → Local Storage
  Can handle: 1,000 users
  Problems: Single point of failure, doesn't scale

Step 2: Add Caching
  + Redis Cache (95% hit rate)
  Can handle: 100,000 users
  Improvement: 10x faster queries

Step 3: Database Scaling
  + Master-Slave Replication (4x read capacity)
  + Sharding (4 shards)
  Can handle: 10 million users
  Improvement: 4x read/write capacity

Step 4: Async Processing
  + Apache Kafka
  + 24,000 Transcode Workers
  Can handle: 50 uploads/sec
  Improvement: Upload latency 36 min → 2 sec

Step 5: CDN & Cloud Storage
  + Google Cloud CDN (420 PoPs)
  + AWS S3 (1 Exabyte)
  Can handle: 500 million DAU
  Improvement: $15.4B → $4B/year cost, 200ms → 50ms latency

Step 6: Microservices
  + 10 independent services
  + API Gateway (Kong)
  + Service Discovery (Consul)
  Can handle: 2.5 billion daily views
  Improvement: Independent scaling, fault isolation
```

---

## 💡 Key Design Principles

1. **Start Simple, Scale Incrementally**
   - Don't over-engineer from day 1
   - Add complexity only when needed

2. **Use the Right Tool for the Job**
   - PostgreSQL for transactional data
   - Elasticsearch for search
   - Cassandra for time-series
   - Redis for caching

3. **CDN is Critical for Video**
   - 95% cache hit rate
   - Saves $11 billion/year
   - Reduces latency 4x

4. **Cache Aggressively**
   - 80/20 rule: 20% of videos = 80% of views
   - Cache metadata, sessions, popular videos

5. **Eventual Consistency is OK**
   - View counts can be delayed
   - Comments can take 5 seconds to appear
   - Trade consistency for availability

6. **Plan for Failures**
   - Database replication (no single point of failure)
   - Circuit breaker (prevent cascade failures)
   - Retry with exponential backoff

7. **Monitor Everything**
   - Metrics, logs, traces
   - Alert on anomalies (p95 latency > 500ms)

---

## 📝 Interview Tips

### What Interviewers Look For
✅ **Clarify requirements** (functional vs non-functional)
✅ **Estimate capacity** (traffic, storage, bandwidth)
✅ **Start with basic architecture** (don't jump to microservices)
✅ **Identify bottlenecks** (database, bandwidth, storage)
✅ **Add optimizations incrementally** (cache, CDN, sharding)
✅ **Explain trade-offs** (consistency vs availability, cost vs performance)
✅ **Think out loud** (show your reasoning)

### Common Mistakes to Avoid
❌ Jumping straight to the final complex architecture
❌ Not asking clarifying questions
❌ Ignoring capacity estimates
❌ Not mentioning trade-offs
❌ Forgetting about failures and monitoring

---

## 🎯 Practice Exercises

After reading this guide, try these exercises:

1. **Design Instagram** (image-heavy vs video-heavy)
2. **Design TikTok** (short-form vs long-form videos)
3. **Design Netflix** (video streaming similarities)
4. **Design Twitch** (live streaming differences)
5. **Modify YouTube for:**
   - 10x more uploads (scale transcode workers)
   - Half the budget (aggressive caching, S3 Glacier)
   - Strong consistency (change DB strategy)

---

## 📖 Additional Resources

### Books
- *Designing Data-Intensive Applications* by Martin Kleppmann
- *System Design Interview* by Alex Xu (Vol 1 & 2)

### Websites
- [System Design Primer](https://github.com/donnemartin/system-design-primer)
- [ByteByteGo](https://bytebytego.com/)

### Real-World Case Studies
- [Netflix Architecture Blog](https://netflixtechblog.com/)
- [YouTube Engineering Blog](https://blog.youtube/)

---

## 🤝 Contributing

Found a typo or want to improve explanations? PRs welcome!

---

## 📄 License

This guide is for educational purposes. Use it to learn and ace your interviews!

---

## ⭐ Final Thoughts

**Remember:** System design is not about memorizing solutions. It's about:
- Understanding trade-offs
- Making informed decisions
- Explaining your reasoning
- Adapting to changing requirements

**Practice makes perfect!** Go through this guide multiple times, draw the diagrams yourself, and explain them out loud.

Good luck with your interviews! 🚀

---

**Made with ❤️ for aspiring system designers and software engineers**
