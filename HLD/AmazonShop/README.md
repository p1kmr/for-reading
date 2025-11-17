# Amazon Shop - High-Level Design (HLD)

A comprehensive, beginner-friendly guide to designing a large-scale e-commerce platform like Amazon.

## 📚 Overview

This repository contains a **complete system design** for Amazon Shop, a scalable e-commerce platform that handles:
- **10 million** daily active users
- **7,000 requests/second** (peak traffic)
- **5 million orders/day**
- **99.99% availability**
- **500 million products**

**Built for beginners** with step-by-step explanations, 15+ Mermaid diagrams, real-world calculations, and interview Q&A.

---

## 🎯 Learning Objectives

After studying this HLD, you will be able to:
- ✅ Design a system that scales to millions of users
- ✅ Explain trade-offs (SQL vs NoSQL, monolith vs microservices)
- ✅ Calculate capacity (traffic, storage, bandwidth, costs)
- ✅ Handle high availability (99.99% uptime)
- ✅ Implement caching strategies (80% hit rate)
- ✅ Design for security (PCI DSS, encryption, authentication)
- ✅ Answer system design interview questions with confidence

---

## 📖 Document Structure

### Phase 1: Requirements & Capacity Planning
**[01_requirements_and_estimations.md](./01_requirements_and_estimations.md)**
- Functional & non-functional requirements
- Traffic estimates (DAU, req/sec, bandwidth)
- Storage estimates (products, orders, images)
- Beginner-friendly calculations with step-by-step math

**Key takeaways:**
- 10M DAU → 7,000 req/sec peak traffic
- 1.6 PB total storage needed
- $355K/month infrastructure cost

---

### Phase 2-7: Architecture Evolution (Incremental Design)

**[02_step1_basic_architecture.md](./02_step1_basic_architecture.md)**
- Basic architecture: Client → LB → App → DB
- Component breakdown (load balancer, app servers, database)
- Capacity analysis (can handle 1,000 req/sec)

**[03_step2_add_caching.md](./03_step2_add_caching.md)**
- Redis caching layer (80% cache hit rate)
- Caching patterns (cache-aside, write-through)
- 70% latency improvement (100ms → 30ms)
- Cache invalidation strategies

**[04_step3_database_scaling.md](./04_step3_database_scaling.md)**
- Master-slave replication (99.99% availability)
- Read replicas (3x read capacity)
- Sharding strategy (for 10x growth)
- Automatic failover (30-60 seconds)

**[05_step4_add_message_queue.md](./05_step4_add_message_queue.md)**
- RabbitMQ for async processing
- Background workers (email, inventory, notifications)
- 35x faster checkout (3,900ms → 110ms)
- Retry logic & dead letter queues

**[06_step5_add_cdn_storage.md](./06_step5_add_cdn_storage.md)**
- CloudFront CDN (90% cache hit rate)
- S3 object storage (1.25 PB images)
- 7.5x faster image loading (150ms → 20ms)
- 75% bandwidth cost reduction

**[07_step6_microservices_final.md](./07_step6_microservices_final.md)**
- Break monolith into 6 microservices
- Product, Order, Payment, User, Inventory, Recommendation services
- Inter-service communication (REST, message queue, Kafka)
- Complete final architecture diagram

---

### Phase 8-9: API, Database & Data Flows

**[08_api_design.md](./08_api_design.md)**
- RESTful API specification
- Authentication (JWT) & authorization (RBAC)
- Rate limiting (100 req/min per user)
- Error handling & pagination

**[09_database_design.md](./09_database_design.md)**
- Complete database schema (SQL)
- Indexing strategy (B-tree, full-text)
- Partitioning (by month for orders)
- Sharding strategy (by user_id)

**[10_data_flow_diagrams.md](./10_data_flow_diagrams.md)**
- Product search flow (with cache)
- Complete checkout flow (order → payment → email)
- Recommendation flow (AI/ML)
- Image upload flow (direct to S3)
- Failure scenarios (database failover, payment retry)

---

### Phase 10: Scalability, Security & Reliability

**[11_scalability_reliability.md](./11_scalability_reliability.md)**
- Horizontal vs vertical scaling
- Auto-scaling policies (CPU-based)
- High availability (99.99% uptime)
- Reliability patterns (circuit breaker, retry, graceful degradation)
- Security architecture (encryption, PCI DSS, JWT)
- Monitoring & alerting (Prometheus, Grafana)

---

### Phase 11-12: Interview Prep

**[13_interview_qa.md](./13_interview_qa.md)**
- 15+ system design interview questions with detailed answers
- How to scale to 10x traffic?
- How to ensure 99.99% availability?
- SQL vs NoSQL? When to use each?
- How to handle database failover?
- How to prevent overselling?
- How to design recommendation system?
- How to handle payment failures?

**[14_beginner_mistakes.md](./14_beginner_mistakes.md)**
- Common mistakes with ❌ and ✅ examples
- Not considering scalability upfront
- Over-engineering (premature optimization)
- Not planning for failures
- Ignoring consistency requirements
- Not estimating capacity
- Storing sensitive data insecurely

**[15_summary_and_checklist.md](./15_summary_and_checklist.md)**
- One-page system summary
- Architecture evolution (Step 1 → Step 6)
- Key design decisions & trade-offs
- What to draw first (interview checklist)
- Final checklist (requirements, components, data flows)

---

## 🚀 Quick Start

### For Beginners:
1. Start with **[01_requirements_and_estimations.md](./01_requirements_and_estimations.md)** (understand the problem)
2. Read **Step 1** through **Step 6** in order (incremental learning)
3. Study **[08_api_design.md](./08_api_design.md)** and **[09_database_design.md](./09_database_design.md)** (implementation details)
4. Review **[13_interview_qa.md](./13_interview_qa.md)** (practice interview questions)

**Time investment:** 4-6 hours (deep understanding)

---

### For Interview Prep:
1. Read **[15_summary_and_checklist.md](./15_summary_and_checklist.md)** (1-page overview)
2. Study **What to Draw First** section (priority order)
3. Practice drawing architecture on whiteboard (5 min, 10 min, 20 min versions)
4. Memorize key metrics:
   - 10M DAU, 7K req/sec, 99.99% uptime, 1.6 PB storage
5. Review **[13_interview_qa.md](./13_interview_qa.md)** (common questions)

**Time investment:** 2-3 hours (interview ready)

---

### For Experienced Engineers:
1. Skim **[02-07]** (architecture evolution) for patterns
2. Focus on **trade-offs** in each step
3. Study **[11_scalability_reliability.md](./11_scalability_reliability.md)** (advanced topics)
4. Compare with your own production systems
5. Identify gaps in your current architecture

**Time investment:** 1-2 hours (advanced insights)

---

## 📊 Key Diagrams

### Final Complete Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Global Users                            │
│                  (10M DAU, 2M concurrent)                       │
└───────────────────────────┬─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                    CDN (CloudFront)                             │
│              90% cache hit, 20ms latency                        │
└───────────────────────────┬─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                    API Gateway                                  │
│        Authentication, Rate Limiting, Load Balancing            │
└───────────────────────────┬─────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼────────┐  ┌───────▼────────┐  ┌──────▼─────────┐
│  Product Svc   │  │   Order Svc    │  │  Payment Svc   │
│   (Node.js)    │  │   (Node.js)    │  │   (Node.js)    │
└───────┬────────┘  └───────┬────────┘  └───────┬────────┘
        │                   │                    │
┌───────▼────────┐  ┌───────▼────────┐  ┌───────▼────────┐
│  PostgreSQL    │  │  PostgreSQL    │  │  PostgreSQL    │
│  Master+3Slave │  │  Master+3Slave │  │  Master+Standby│
└────────────────┘  └───────┬────────┘  └────────────────┘
                            │
                    ┌───────▼────────┐
                    │   RabbitMQ     │
                    │  Message Queue │
                    └───────┬────────┘
                            │
        ┌───────────────────┼──────────────────┐
        │                   │                  │
┌───────▼───────┐  ┌────────▼────────┐  ┌─────▼──────────┐
│ Email Worker  │  │Inventory Worker │  │Analytics Worker│
└───────────────┘  └─────────────────┘  └────────────────┘
```

---

## 💡 Key Takeaways

### Performance Improvements Across Steps

| Metric | Step 1 | Step 6 (Final) | Improvement |
|--------|--------|----------------|-------------|
| **Checkout Time** | 3,900ms | 110ms | **35x faster** |
| **Image Load Time** | 200ms | 20ms | **10x faster** |
| **API Latency** | 100ms | 30ms | **70% faster** |
| **Database Load** | 48K QPS | 9.6K QPS | **80% reduction** |
| **Availability** | 99.5% | 99.99% | **50x less downtime** |

---

### Technology Stack

**Frontend:**
- React.js / Vue.js / Angular (web)
- React Native / Flutter (mobile)

**Backend:**
- Node.js (application servers)
- Express.js (web framework)
- PostgreSQL (primary database)
- Redis (cache)
- RabbitMQ (message queue)
- Kafka (event streaming)
- Elasticsearch (product search)

**Infrastructure:**
- AWS (cloud provider)
- Kubernetes (container orchestration)
- Docker (containerization)
- Nginx (load balancer)
- CloudFront (CDN)
- S3 (object storage)

**Monitoring:**
- Prometheus (metrics)
- Grafana (dashboards)
- ELK Stack (logging)
- Jaeger (distributed tracing)

---

## 📈 Scalability Path

```
0-10K users:
├─ Monolith on Heroku/Vercel
├─ PostgreSQL (single instance)
└─ Cost: $100/month

10K-100K users:
├─ Monolith on AWS/GCP
├─ PostgreSQL (RDS with backups)
├─ Redis cache
├─ CDN (CloudFlare)
└─ Cost: $1,000/month

100K-1M users:
├─ 3-5 microservices
├─ PostgreSQL (master + 3 replicas)
├─ Redis cluster
├─ RabbitMQ
├─ S3 + CloudFront
└─ Cost: $10,000/month

1M-10M users:
├─ 6+ microservices
├─ PostgreSQL (sharded)
├─ Elasticsearch
├─ Kafka
├─ Multi-region
└─ Cost: $100,000/month

10M+ users: (This HLD)
├─ Full microservices (6+ services)
├─ Database sharding (4+ shards)
├─ Multi-region active-active
├─ Advanced ML (recommendations)
└─ Cost: $355,000/month
```

---

## 🎓 Interview Tips

### 5-Minute Version (Breadth-First)
1. Draw basic architecture (Client → LB → App → DB)
2. Add caching (Redis)
3. Add database replication (master-slave)
4. Mention: "99.99% uptime, 7K req/sec, 80% cache hit"
5. Trade-offs: "SQL for ACID, async for speed"

### 15-Minute Version (Balanced)
1. Start with requirements (10M DAU, 7K req/sec)
2. Draw basic + cache + replication
3. Add message queue (async processing)
4. Add CDN (images)
5. Capacity calculations (9 servers, 3 replicas, 200 GB cache)
6. Discuss trade-offs (consistency, availability, cost)
7. Failure scenarios (database failover, retry logic)

### 45-Minute Version (Deep Dive)
1. Requirements (5 min)
2. Architecture evolution (Step 1 → Step 6) (15 min)
3. Database design (schema, indexes, sharding) (5 min)
4. API design (endpoints, auth, rate limiting) (5 min)
5. Data flows (search, checkout, recommendation) (5 min)
6. Scalability & reliability (10 min)
7. Trade-offs & alternatives (5 min)

---

## 🤝 Contributing

This is a learning resource. If you find errors or have suggestions:
1. Open an issue
2. Submit a pull request
3. Share feedback

---

## 📝 License

This educational material is free to use for learning purposes.

---

## ⭐ Acknowledgments

Inspired by real-world system design patterns from:
- Amazon.com
- eBay
- Shopify
- Alibaba

Built with ❤️ for aspiring system designers.

---

## 📞 Support

- **Questions?** Open a GitHub issue
- **Interview prep?** Practice with mock interviews
- **Production use?** Adapt patterns to your scale and requirements

---

**Last Updated:** 2025-11-17  
**Version:** 1.0  
**Status:** Complete (16 documents, 15+ diagrams, 60+ pages)

---

## 🔗 Quick Links

- [Requirements & Estimations](./01_requirements_and_estimations.md)
- [Basic Architecture](./02_step1_basic_architecture.md)
- [Add Caching](./03_step2_add_caching.md)
- [Database Scaling](./04_step3_database_scaling.md)
- [Message Queue](./05_step4_add_message_queue.md)
- [CDN & Storage](./06_step5_add_cdn_storage.md)
- [Microservices](./07_step6_microservices_final.md)
- [API Design](./08_api_design.md)
- [Database Design](./09_database_design.md)
- [Data Flows](./10_data_flow_diagrams.md)
- [Scalability & Security](./11_scalability_reliability.md)
- [Interview Q&A](./13_interview_qa.md)
- [Common Mistakes](./14_beginner_mistakes.md)
- [Summary & Checklist](./15_summary_and_checklist.md)

---

**Happy Learning! 🚀**
