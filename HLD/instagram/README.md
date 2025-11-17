# Instagram High-Level Design (HLD) - Complete Guide

> **A comprehensive, beginner-friendly guide to designing Instagram at scale**

**Author**: AI System Design Expert
**Last Updated**: January 2025
**Difficulty**: Beginner to Advanced
**Estimated Reading Time**: 4-6 hours

---

## 📚 Table of Contents

1. [Overview](#overview)
2. [Learning Path](#learning-path)
3. [Document Structure](#document-structure)
4. [Quick Start](#quick-start)
5. [Key Concepts](#key-concepts)
6. [Technologies Used](#technologies-used)
7. [System Metrics](#system-metrics)
8. [How to Use This Guide](#how-to-use-this-guide)
9. [Interview Preparation](#interview-preparation)
10. [Additional Resources](#additional-resources)

---

## Overview

This repository contains a **complete high-level design** for Instagram, covering:

- ✅ Requirements & capacity planning
- ✅ Step-by-step architecture evolution (4 steps)
- ✅ Database design (sharding, replication)
- ✅ Caching strategies (Redis)
- ✅ CDN & media storage (S3, CloudFront)
- ✅ Message queues (Kafka) & async processing
- ✅ API design & data flows
- ✅ Advanced features (feed ranking, search, analytics)
- ✅ Scalability, reliability & security
- ✅ Interview Q&A (15 questions with detailed answers)
- ✅ Common mistakes & solutions

### What Makes This Special?

- **Beginner-friendly**: Explains every concept from first principles
- **Visual**: 20+ Mermaid diagrams showing architecture evolution
- **Practical**: Real calculations with actual numbers (not hand-waving!)
- **Comprehensive**: 10 detailed documents covering every aspect
- **Interview-ready**: 15 Q&A covering all common interview questions

---

## Learning Path

### For Beginners (New to System Design)
```
Day 1: Requirements & Capacity Planning
→ Read: 01_requirements_and_estimations.md
→ Time: 1 hour
→ Learn: How to calculate DAU, storage, bandwidth

Day 2: Basic Architecture
→ Read: 02_step1_basic_architecture.md
→ Time: 1 hour
→ Learn: Client-server architecture, load balancing, databases

Day 3: Caching & Performance
→ Read: 03_step2_add_caching.md
→ Time: 1.5 hours
→ Learn: Redis, cache strategies, cache invalidation

Day 4: Database Scaling
→ Read: 04_step3_database_scaling.md
→ Time: 2 hours
→ Learn: Replication, sharding, consistency

Day 5: Advanced Components
→ Read: 05_step4_message_queue_cdn_storage.md
→ Time: 1.5 hours
→ Learn: Kafka, S3, CDN, async processing

Day 6: APIs & Data Flow
→ Read: 06_api_design_data_flow.md
→ Time: 1 hour
→ Learn: RESTful APIs, sequence diagrams

Day 7: Final Architecture & Review
→ Read: 07_final_architecture_advanced_features.md
→ Read: 10_beginner_mistakes.md
→ Time: 2 hours
→ Learn: Complete system, common pitfalls
```

### For Intermediate (Some System Design Experience)
```
Focus Areas:
1. Capacity planning (01_requirements_and_estimations.md)
2. Database sharding strategy (04_step3_database_scaling.md)
3. Feed generation algorithm (07_final_architecture_advanced_features.md)
4. Scalability & reliability (08_scalability_reliability_security.md)
5. Interview Q&A (09_interview_qa.md)

Time: 3-4 hours
```

### For Advanced (Preparing for FAANG Interviews)
```
Deep Dive Topics:
1. Feed ranking algorithm & fanout strategies
2. Multi-region deployment & disaster recovery
3. Security architecture & rate limiting
4. Monitoring & debugging production issues
5. Trade-offs & design decisions

Focus: 07, 08, 09 + interview practice
Time: 2-3 hours
```

---

## Document Structure

### 📄 01_requirements_and_estimations.md
**What you'll learn**:
- Functional requirements (upload, feed, like, comment, follow, search)
- Non-functional requirements (latency < 100ms, 99.99% availability, 500M DAU)
- Traffic estimations (578K reads/sec, 1.2K writes/sec, 500:1 ratio)
- Storage estimations (2.2 EB in 5 years!)
- Bandwidth calculations (290 GB/sec download, 14 GB/sec upload)

**Key takeaway**: Instagram is massively read-heavy (500:1 ratio) → optimize for reads!

---

### 📄 02_step1_basic_architecture.md
**What you'll learn**:
- Client layer (mobile app, web browser)
- Load balancer (distribute traffic, health checks)
- App servers (stateless, horizontal scaling)
- Database (PostgreSQL, why not NoSQL?)
- File storage (NAS initially, S3 later)

**Key diagrams**: Basic 3-tier architecture

**What's missing**: Caching (feed too slow: 300ms), database scaling (single point of failure)

---

### 📄 03_step2_add_caching.md
**What you'll learn**:
- Redis cluster architecture (16 shards, 8 TB capacity)
- What to cache (feeds, profiles, like status, media URLs)
- Cache strategies (cache-aside, write-through, hybrid)
- Cache invalidation (TTL + explicit, hardest problem!)
- Performance gains (300ms → 25ms feed load, 90% DB load reduction)

**Key diagrams**: Cache architecture, cache hit/miss flows

**Key takeaway**: Caching reduces database load by 90% and makes feed 12x faster!

---

### 📄 04_step3_database_scaling.md
**What you'll learn**:
- Master-slave replication (1 master + 4 replicas per shard)
- Sharding strategy (hash-based by user_id, 50 shards)
- Shard routing (Vitess for automatic routing)
- Failover (Orchestrator for automatic promotion)
- Cross-shard queries (scatter-gather, pre-compute instead)

**Key diagrams**: Sharding architecture, replication flow, failover process

**Capacity**: 100K writes/sec, 500K reads/sec, 1.7 PB storage

---

### 📄 05_step4_message_queue_cdn_storage.md
**What you'll learn**:
- AWS S3 (unlimited object storage, 11 nines durability)
- CloudFront CDN (200+ edge locations, 95% cache hit rate)
- Apache Kafka (message queue for async processing)
- Background workers (image resize, video encode, feed fanout)
- Lifecycle policies (hot → warm → cold storage)

**Key diagrams**: CDN architecture, Kafka flow, upload sequence

**Performance**: 200ms global latency → 10ms (CDN), $67M/mo → $17M/mo (74% savings!)

---

### 📄 06_api_design_data_flow.md
**What you'll learn**:
- RESTful API endpoints (auth, posts, feed, likes, follows, search)
- Request/response formats (JSON examples)
- Authentication (JWT tokens)
- Rate limiting (prevent abuse)
- Sequence diagrams (registration, login, upload, feed, like, follow)

**Key diagrams**: 6 detailed sequence diagrams

**Best practices**: Versioning, pagination, error handling, idempotency

---

### 📄 07_final_architecture_advanced_features.md
**What you'll learn**:
- Complete system architecture (all components integrated)
- Feed ranking algorithm (ML model for engagement prediction)
- Fanout-on-write vs fanout-on-read (trade-offs)
- Hybrid approach for celebrities (avoid 100M fanout)
- Elasticsearch for search (users, hashtags)
- ClickHouse for analytics (100x faster than PostgreSQL)

**Key diagrams**: Complete architecture, feed algorithm flowchart, search architecture

**Infrastructure cost**: $438K/month for 500M DAU

---

### 📄 08_scalability_reliability_security.md
**What you'll learn**:
- Horizontal scaling (add more servers, not bigger servers)
- Auto-scaling policies (CPU threshold, min/max instances)
- High availability (99.99% = 52 min downtime/year)
- Multi-AZ deployment (survive data center failures)
- Circuit breakers (prevent cascade failures)
- Graceful degradation (core features always work)
- Security (JWT, bcrypt, rate limiting, input validation, encryption)
- Monitoring (Prometheus, Grafana, Jaeger)
- Disaster recovery (backups, multi-region)

**Key concepts**: Eliminate SPOFs, plan for failures, monitor everything

---

### 📄 09_interview_qa.md
**What you'll learn**:
- 15 common system design interview questions
- Detailed answers with examples from Instagram
- How to approach each question systematically
- Trade-offs to discuss
- What interviewers look for

**Sample questions**:
1. How would you design Instagram's feed generation?
2. How do you handle database scaling for billions of posts?
3. Why use Redis cache? What do you cache?
4. How do you ensure 99.99% uptime?
5. How do you handle media storage?
6. Your system gets 10x traffic - how do you handle it?
7. How do you handle a celebrity with 100M followers posting?
8. SQL vs NoSQL for Instagram?
9. How do you handle database failover?
10. How do you achieve < 100ms feed latency?
11. How do you optimize for global users?
12. Fanout-on-write vs fanout-on-read?
13. How do you handle eventual consistency?
14. How would you add Stories feature?
15. How would you add real-time messaging?

---

### 📄 10_beginner_mistakes.md
**What you'll learn**:
- 14 common mistakes beginners make
- Why each approach is wrong
- Correct approach with examples

**Mistakes covered**:
1. Not clarifying requirements
2. Over-engineering initial design
3. Ignoring capacity planning
4. Wrong database choice
5. Forgetting caching
6. Single point of failure
7. Ignoring consistency requirements
8. Not handling failures
9. Inefficient database queries (N+1 problem)
10. Storing media in database
11. Not using message queues
12. Poor shard key choice
13. Not monitoring
14. Neglecting security

**Bonus**: System design checklist (verify your design)

---

## Quick Start

### I have 30 minutes
→ Read: 01_requirements_and_estimations.md
→ Read: 02_step1_basic_architecture.md
→ Focus: Understand requirements + basic architecture

### I have 2 hours
→ Read: 01, 02, 03 (requirements, basic, caching)
→ Skim: 07 (final architecture diagram)
→ Read: 10 (common mistakes)

### I have 4 hours (Full learning path)
→ Read all documents in order
→ Practice: Draw architecture on whiteboard
→ Review: Interview Q&A

### I have an interview tomorrow
→ Read: 01 (requirements & capacity calculations)
→ Read: 07 (final architecture)
→ Read: 09 (interview Q&A)
→ Review: 10 (common mistakes)
→ Practice: Explain feed generation + database sharding

---

## Key Concepts

### Scale
- **500 million DAU** (Daily Active Users)
- **578,000 requests/sec** (reads, average)
- **1.2 million requests/sec** (peak)
- **100 million posts/day**
- **2.2 Exabytes** storage (5 years)

### Performance
- Feed load: **< 100ms target**, **25ms achieved** (75% under!)
- Upload: **< 2s target**, **550ms achieved**
- Like/Comment: **< 50ms**

### Availability
- **99.99% uptime** (52 minutes downtime/year)
- Multi-AZ deployment (3 availability zones)
- No single point of failure

### Read:Write Ratio
- **500:1** (read-heavy!)
- Optimize reads aggressively (caching, CDN, read replicas)

---

## Technologies Used

### Infrastructure
| Component | Technology | Why? |
|-----------|-----------|------|
| **CDN** | CloudFront | 200+ edge locations, 74% cost savings |
| **Load Balancer** | AWS ALB | Managed, auto-scaling, health checks |
| **App Server** | Node.js | High I/O throughput, async |
| **Cache** | Redis Cluster | 16 shards, 8 TB, 1.6M req/sec |
| **Database** | PostgreSQL | ACID, relational, JSON support |
| **Shard Router** | Vitess | Automatic shard routing |
| **Failover** | Orchestrator | Automatic replica promotion |
| **Message Queue** | Apache Kafka | High throughput, durable |
| **Object Storage** | AWS S3 | Unlimited, 11 nines durability |
| **Search** | Elasticsearch | Full-text, fuzzy search |
| **Analytics** | ClickHouse | Column-store, 100x faster |
| **Monitoring** | Prometheus + Grafana | Metrics, dashboards |
| **Tracing** | Jaeger | Distributed tracing |
| **Logging** | ELK Stack | Centralized logging |

---

## System Metrics

### Traffic
| Metric | Value |
|--------|-------|
| Daily Active Users (DAU) | 500 million |
| Reads (average) | 578,000 req/sec |
| Reads (peak) | 1.7 million req/sec |
| Writes (average) | 1,157 req/sec |
| Writes (peak) | 3,472 req/sec |
| Read:Write Ratio | 500:1 |
| Bandwidth (download) | 290 GB/sec |
| Bandwidth (upload) | 14 GB/sec |

### Storage (5 years)
| Type | Size |
|------|------|
| Photos | 365 PB |
| Videos | 1,825 PB |
| Metadata (DB) | 1.7 PB |
| Total | **2.2 EB** |

### Infrastructure
| Component | Count | Cost/Month |
|-----------|-------|------------|
| App Servers | 100 | $20,000 |
| Redis Cluster | 32 nodes | $11,520 |
| PostgreSQL | 150 nodes | $75,000 |
| Kafka | 3 brokers | $1,200 |
| Elasticsearch | 10 nodes | $7,000 |
| S3 Storage | 2.2 EB | $300,000 |
| CDN | 715 PB/mo | $14,300 |
| **Total** | - | **$438,420** |

---

## How to Use This Guide

### For Self-Study
1. Read documents in order (01 → 10)
2. Draw diagrams yourself (don't just read!)
3. Calculate numbers yourself (verify estimations)
4. Ask yourself: "Why this choice over alternatives?"

### For Interview Prep
1. Start with requirements (01)
2. Practice drawing Step 1 architecture (02)
3. Add caching (03), sharding (04), CDN (05)
4. Memorize key numbers (500M DAU, 578K req/sec, 2.2 EB)
5. Practice answering Q&A (09)
6. Review mistakes (10)

### For Teaching
1. Use as lecture material (step-by-step)
2. Show Mermaid diagrams in presentations
3. Use calculations as exercises
4. Discuss trade-offs for each decision

### For Reference
1. Bookmark specific sections
2. Use as template for similar designs (Twitter, Facebook)
3. Adapt numbers for different scales

---

## Interview Preparation

### Key Questions You Should Be Able to Answer

**Requirements** (5 min):
- What features are in scope? (Upload, feed, like, comment, follow, search)
- How many users? (500M DAU)
- What's the read:write ratio? (500:1)
- Latency target? (< 100ms for feed)
- Availability? (99.99%)

**High-Level Design** (15 min):
- Draw basic architecture (client → LB → app → cache → DB)
- Why PostgreSQL over MongoDB? (ACID, relations, consistency)
- How to scale database? (Replication + sharding)
- How to cache? (Redis, what to cache, invalidation)

**Deep Dives** (20 min):
- Feed generation: Fanout-on-write vs fanout-on-read
- Database sharding: By user_id, hash-based, 50 shards
- CDN: Why necessary? (Global users, cost savings)
- Message queue: Why async? (Video encoding, notifications)
- Monitoring: What metrics? (Latency, error rate, CPU)

**Trade-offs** (10 min):
- SQL vs NoSQL
- Fanout-on-write vs fanout-on-read
- Strong vs eventual consistency
- Horizontal vs vertical scaling

### Practice Exercises

1. **Draw from memory**: Complete architecture in 20 minutes
2. **Calculate capacity**: For 1B DAU (2x current)
3. **Explain trade-offs**: Why PostgreSQL? Why Redis?
4. **Handle failures**: Database down, what happens?
5. **Optimize**: Feed is 500ms, how to fix?

---

## Additional Resources

### Real-World Instagram Engineering
- [Instagram Engineering Blog](https://instagram-engineering.com/)
- [Scaling Instagram Infrastructure (2017)](https://instagram-engineering.com/what-powers-instagram-hundreds-of-instances-dozens-of-technologies-adf2e22da2ad)
- [Cassandra at Instagram (2012)](https://instagram-engineering.com/cassandra-data-model-4a1a7c9c9c06)

### System Design Fundamentals
- [Designing Data-Intensive Applications](https://www.oreilly.com/library/view/designing-data-intensive-applications/9781491903063/) (Book by Martin Kleppmann)
- [System Design Primer](https://github.com/donnemartin/system-design-primer) (GitHub)
- [ByteByteGo](https://bytebytego.com/) (Alex Xu's system design course)

### Technologies Deep Dive
- [Redis Documentation](https://redis.io/documentation)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Elasticsearch Documentation](https://www.elastic.co/guide/index.html)

---

## FAQ

**Q: Is this how real Instagram is built?**
A: This design is inspired by Instagram's public engineering talks, but simplified for learning. Real Instagram is even more complex (Stories, Reels, DMs, ads, ML recommendations).

**Q: Do I need to memorize all numbers?**
A: No! Understand the process of calculation. In interviews, you'll derive numbers based on given requirements.

**Q: Which document is most important for interviews?**
A: If forced to choose one: 07_final_architecture_advanced_features.md (complete system + feed algorithm). But really, read them all!

**Q: Can I use this for other designs (Twitter, Facebook)?**
A: Absolutely! The principles apply to any social media platform. Adjust numbers and features as needed.

**Q: I'm a complete beginner. Where do I start?**
A: Start with 01_requirements_and_estimations.md. Take it slow, one document per day.

**Q: How long to prepare for FAANG system design interview?**
A: 2-4 weeks if studying 2 hours/day. Practice multiple designs (Instagram, Twitter, YouTube, Uber, WhatsApp).

---

## Contributing

Found a mistake? Have a suggestion?
- Open an issue or pull request
- Email: [not applicable - this is educational content]

---

## License

This content is provided for **educational purposes only**. Free to use for personal learning and interview preparation.

---

## Acknowledgments

- Instagram Engineering team for public talks
- System design community for shared knowledge
- All the engineers who made system design fun!

---

## Final Words

**System design is a skill, not knowledge.**

You can read all these documents, but real learning happens when you:
1. **Draw** architectures yourself
2. **Calculate** capacity yourself
3. **Explain** trade-offs out loud
4. **Practice** with friends

Good luck with your interviews! 🚀

---

**Next Steps**:
1. Start with [01_requirements_and_estimations.md](./01_requirements_and_estimations.md)
2. Draw the architecture on paper/whiteboard
3. Ask yourself: "Why this choice?"
4. Practice explaining to someone else
5. Review [09_interview_qa.md](./09_interview_qa.md) before interviews

**Remember**: The goal is not to memorize this design, but to understand the *thinking process* behind it!

