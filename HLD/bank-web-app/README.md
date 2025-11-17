# Bank Web Application - High-Level Design (HLD)

## 📚 Complete Guide for System Design Interviews

This comprehensive guide walks you through designing a **production-ready, scalable banking web application** from scratch. Perfect for beginners preparing for system design interviews!

---

## 🎯 What You'll Learn

- ✅ Requirements gathering & capacity planning
- ✅ Incremental architecture design (6 evolutionary steps)
- ✅ Database scaling strategies (replication, sharding, read replicas)
- ✅ Caching for 12x performance improvement
- ✅ Asynchronous processing with message queues
- ✅ CDN & object storage for global reach
- ✅ Security & compliance (PCI-DSS, GDPR, encryption)
- ✅ Database schema design with real SQL examples
- ✅ RESTful API design with authentication
- ✅ High availability (99.99% uptime)
- ✅ Disaster recovery planning (RTO, RPO)
- ✅ Monitoring, alerting, and debugging
- ✅ Interview Q&A with detailed answers
- ✅ Common beginner mistakes and how to avoid them

---

## 📖 Table of Contents

### Phase 1: Foundation
**[01_requirements_and_estimations.md](./01_requirements_and_estimations.md)**
- Functional requirements (12 core features)
- Non-functional requirements (performance, availability, security)
- Traffic estimations with step-by-step calculations
- Storage estimations (5-year projection)
- **Key Takeaway**: Learn to calculate DAU, requests/sec, storage needs

### Phase 2: Basic Architecture (Step 1)
**[02_step1_basic_architecture.md](./02_step1_basic_architecture.md)**
- Client → Load Balancer → App Servers → Database
- Component justification (why each component?)
- Technology choices (Java Spring Boot, PostgreSQL, Nginx)
- Request flow with sequence diagrams
- **Key Takeaway**: Establish solid foundation with proven technologies

### Phase 3: Add Caching (Step 2)
**[03_step2_add_caching.md](./03_step2_add_caching.md)**
- Redis cache integration
- Caching strategies (what to cache, TTL values)
- Cache invalidation patterns (cache-aside, write-through)
- Performance improvement: 200ms → 17ms (12x faster!)
- **Key Takeaway**: Reduce database load by 90%, improve response time dramatically

### Phase 4: Database Scaling (Step 3)
**[04_step3_database_scaling.md](./04_step3_database_scaling.md)**
- Master-slave replication for high availability
- Read replicas for read-heavy workloads
- Sharding strategies (hash-based, geography-based)
- Handling replication lag
- **Key Takeaway**: Achieve 99.99% availability with automatic failover

### Phase 5: Message Queue (Step 4)
**[05_step4_add_message_queue.md](./05_step4_add_message_queue.md)**
- Apache Kafka for asynchronous processing
- Event-driven architecture
- Async operations (email, SMS, PDF generation)
- Response time improvement: 1250ms → 55ms (23x faster!)
- **Key Takeaway**: Decouple services, handle traffic spikes gracefully

### Phase 6: CDN & Object Storage (Step 5)
**[06_step5_add_cdn_storage.md](./06_step5_add_cdn_storage.md)**
- CloudFront CDN for static assets
- AWS S3 for document storage
- Storage tiering for cost optimization
- Global performance: 1400ms → 150ms (9x faster for international users)
- **Key Takeaway**: Serve global users with low latency, reduce costs by 71%

### Phase 7: Security & Compliance (Step 6)
**[07_step6_security_compliance.md](./07_step6_security_compliance.md)**
- Multi-factor authentication (MFA) flow
- Encryption (TLS 1.2+, AES-256)
- RBAC (Role-Based Access Control)
- Audit logging (7-year retention)
- PCI-DSS & GDPR compliance
- **Key Takeaway**: Build bank-grade security into your system

### Phase 8: Database & API Design
**[08_database_api_design.md](./08_database_api_design.md)**
- Complete database schema (users, accounts, transactions, loans)
- ER diagrams and SQL table definitions
- RESTful API design with examples
- Request/response formats with error handling
- **Key Takeaway**: Design normalized schemas and intuitive APIs

### Phase 9: Scalability & Monitoring
**[09_scalability_monitoring.md](./09_scalability_monitoring.md)**
- Final architecture (all components integrated)
- Auto-scaling configuration
- High availability setup (Multi-AZ)
- Disaster recovery (RTO=1hr, RPO=5min)
- Monitoring stack (Prometheus, Grafana, ELK)
- **Key Takeaway**: Build resilient systems that self-heal and scale

### Phase 10: Interview Prep & Summary
**[10_interview_qa_summary.md](./10_interview_qa_summary.md)**
- 9 comprehensive interview questions with detailed answers
- Common beginner mistakes and solutions
- System design checklist
- Technology trade-offs explained
- Final architecture summary
- **Key Takeaway**: Ace your system design interviews with confidence!

---

## 🚀 Quick Start Guide

### For Interview Preparation
1. **Read in order** (Phases 1-10) to build concepts incrementally
2. **Practice drawing** architecture diagrams on a whiteboard
3. **Memorize key numbers**: 99.99% uptime, RTO=1hr, RPO=5min
4. **Understand trade-offs**: Why PostgreSQL? Why async replication?

### For Hands-On Learning
1. **Start with Phase 1**: Understand requirements first
2. **Build Phase 2**: Simple Spring Boot + PostgreSQL app
3. **Add Phase 3**: Integrate Redis cache
4. **Iterate**: Add complexity one phase at a time

### For Quick Reference
- Jump to **Phase 10** for interview Q&A
- Review **Phase 9** for monitoring & alerting
- Check **Phase 8** for API design patterns

---

## 🎓 Learning Path

### Beginner (Week 1-2)
- [ ] Phase 1: Requirements & Estimations
- [ ] Phase 2: Basic Architecture
- [ ] Phase 3: Caching
- [ ] Phase 10: Interview Q&A (first read)

### Intermediate (Week 3-4)
- [ ] Phase 4: Database Scaling
- [ ] Phase 5: Message Queue
- [ ] Phase 8: Database & API Design
- [ ] Build a simple prototype

### Advanced (Week 5-6)
- [ ] Phase 6: CDN & Storage
- [ ] Phase 7: Security & Compliance
- [ ] Phase 9: Scalability & Monitoring
- [ ] Mock interviews with peers

---

## 📊 System Metrics at a Glance

| Metric | Initial (Step 1) | Final (Step 9) | Improvement |
|--------|------------------|----------------|-------------|
| **Response Time** | 200ms | 17ms | **12x faster** |
| **Throughput** | 2.2 req/sec | 54 req/sec | **25x higher** |
| **Database Load** | 105 queries/sec | 10.5 queries/sec | **90% reduction** |
| **Availability** | 99.5% | 99.99% | **10x less downtime** |
| **Global Latency** | 1400ms | 150ms | **9x faster** |
| **Cost/User** | - | $0.006/month | **Half a cent!** |

---

## 🏗️ Architecture Evolution

```
Step 1: Basic
Client → LB → App → DB

Step 2: Add Caching
Client → LB → App → Redis → DB

Step 3: Scale Database
Client → LB → App → Redis → [DB Master + 2 Replicas]

Step 4: Add Message Queue
Client → LB → App → Redis → DB
                    ↓
                  Kafka → Workers

Step 5: Add CDN & S3
CDN → Client → LB → App → Redis → DB
                    ↓
                   S3

Step 6: Add Security
[WAF] → CDN → Client → LB → App → Redis → DB
                              ↓
                         [Encryption, MFA, Audit]
```

---

## 💰 Cost Breakdown

| Component | Monthly Cost | Purpose |
|-----------|--------------|---------|
| Load Balancers | $50 | Distribute traffic |
| App Servers (auto-scale) | $3,000 | Business logic |
| PostgreSQL (master + 2 replicas) | $450 | ACID transactions |
| Redis Cluster | $450 | Caching (12x perf boost) |
| Kafka Cluster | $600 | Async processing |
| S3 Storage (20 TB, tiered) | $604 | Documents, statements |
| CloudFront CDN | $27 | Global static assets |
| Monitoring & Logging | $300 | Observability |
| Disaster Recovery | $500 | US-West-2 standby |
| **Total** | **$5,981/month** | — |

**ROI**: 1103% (Revenue: $72K, Cost: $6K, Profit: $66K/month)

---

## 🔑 Key Design Decisions

### Why PostgreSQL over MongoDB?
✅ **ACID transactions** (money can't disappear)
✅ **Complex queries** (JOINs across tables)
✅ **Strong consistency** (balance must be accurate)
❌ MongoDB: Eventual consistency (not acceptable for banking)

### Why Async Replication?
✅ **Fast writes** (no waiting for replicas)
✅ **Acceptable RPO** (5 minutes data loss tolerable)
❌ Sync replication: 5x slower writes

### Why Redis over Memcached?
✅ **Persistence** (can recover cache after restart)
✅ **Rich data structures** (lists, sets, sorted sets)
✅ **Pub/sub** (for real-time notifications)

### Why Kafka over RabbitMQ?
✅ **High throughput** (millions of messages/sec)
✅ **Persistent** (messages stored on disk)
✅ **Replay** (can reprocess old messages)

---

## 📝 Interview Tips

### What to Draw First (Priority Order)
1. ✅ Client → Load Balancer → App → Database (basic architecture)
2. ✅ Traffic estimates (users, requests/sec)
3. ✅ Database choice (SQL vs NoSQL) with justification
4. ✅ Caching layer (what to cache, TTL)
5. ✅ Database replication (master-slave)
6. ✅ CDN for static content

### Common Interview Questions
- "How do you ensure high availability?" → Multi-AZ, replication, auto-failover
- "How do you handle 10x traffic spike?" → Auto-scaling, caching, rate limiting
- "SQL vs NoSQL for banking?" → PostgreSQL (ACID transactions required)
- "How to prevent race conditions?" → Database row locking (FOR UPDATE)
- "How to ensure data consistency?" → Saga pattern, 2-phase commit

### Things Interviewers Look For
- ✅ Requirements clarification (ask questions!)
- ✅ Capacity planning (show calculations)
- ✅ Incremental design (don't jump to final solution)
- ✅ Trade-offs discussion (pros/cons of each decision)
- ✅ Scalability mindset (how to grow from 1K → 1M users)
- ✅ Failure scenarios (what if database crashes?)

---

## 🛠️ Technologies Used

| Layer | Technology | Purpose |
|-------|------------|---------|
| **Load Balancer** | Nginx / AWS ALB | Distribute traffic, SSL termination |
| **App Server** | Java Spring Boot | Business logic, ACID transactions |
| **Database** | PostgreSQL | ACID compliance, relational data |
| **Cache** | Redis | Fast reads (<2ms), session storage |
| **Message Queue** | Apache Kafka | Async processing, event streaming |
| **CDN** | CloudFront | Global content delivery |
| **Object Storage** | AWS S3 | Documents, statements (cheap) |
| **Monitoring** | Prometheus + Grafana | Metrics, dashboards |
| **Logging** | ELK Stack | Centralized logs, search |
| **Tracing** | Jaeger | Distributed request tracing |
| **Alerting** | PagerDuty | On-call notifications |

---

## 🎯 Requirements Supported

### Functional Requirements (12 Features)
✅ User registration & KYC
✅ Login with MFA
✅ View account balance & details
✅ Transaction history
✅ Fund transfers (own account, other bank)
✅ Recurring transfers
✅ Bill payments
✅ Loan application & management
✅ Fixed deposits
✅ Admin functionalities
✅ Notifications (SMS, email, push)
✅ External integrations (payment gateways, KYC)

### Non-Functional Requirements
✅ Response time: <2 seconds (achieved: 17ms)
✅ Availability: 99.99% (52 min downtime/year)
✅ Concurrent users: 10,000 (scalable to 100K+)
✅ Encryption: TLS 1.2+, AES-256
✅ Audit logs: 7-year retention
✅ Disaster recovery: RTO=1hr, RPO=5min
✅ Accessibility: Mobile (iOS/Android) + Desktop
✅ Idempotency: No duplicate transactions
✅ Security: SQL injection, XSS, CSRF prevention
✅ Horizontal scalability: Auto-scaling ready

---

## 🤝 Contributing

This is a learning resource! If you find errors or have suggestions:
1. Fork the repository
2. Create a feature branch
3. Submit a pull request

---

## 📚 Additional Resources

### Books
- "Designing Data-Intensive Applications" by Martin Kleppmann
- "System Design Interview" by Alex Xu (Volumes 1 & 2)

### Online Courses
- Educative: "Grokking the System Design Interview"
- Coursera: "Cloud Computing Specialization"

### Practice
- LeetCode System Design problems
- InterviewBit System Design track
- Mock interviews with peers

---

## ⚡ Quick Navigation

### By Topic
- **Scalability**: Phase 4, Phase 9
- **Performance**: Phase 3 (Caching), Phase 5 (Async)
- **Security**: Phase 7
- **APIs**: Phase 8
- **Interview Prep**: Phase 10

### By Difficulty
- **Beginner**: Phase 1, 2, 3
- **Intermediate**: Phase 4, 5, 8
- **Advanced**: Phase 6, 7, 9

---

## 🎉 Success Metrics

After completing this guide, you should be able to:
- ✅ Design a scalable system supporting 1M+ users
- ✅ Calculate capacity requirements (traffic, storage)
- ✅ Explain trade-offs between different technologies
- ✅ Design for high availability (99.99% uptime)
- ✅ Implement security best practices
- ✅ Answer common system design interview questions
- ✅ Estimate infrastructure costs
- ✅ Set up monitoring and alerting

---

## 📞 Contact & Support

Questions? Feedback?
- Open an issue on GitHub
- Share your learnings with the community
- Practice with peers

---

**Happy Learning! Best of luck with your system design interviews! 🚀**

---

*Last Updated: January 2025*
*Author: System Design Learning Series*
*License: Educational Use*
