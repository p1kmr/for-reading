# Notification System - High-Level Design (HLD)

A comprehensive, beginner-friendly guide to designing a scalable notification system that handles 10 million daily active users and 50 million notifications per day.

---

## Table of Contents

### Getting Started
1. [Requirements & Capacity Estimations](./requirements_and_estimations.md) - Start here!
   - Functional and non-functional requirements
   - Traffic and storage calculations with examples
   - Assumptions and constraints

### Architecture Evolution (Step-by-Step)

2. [Step 1: Basic Architecture](./step1_basic_architecture.md)
   - Client → Load Balancer → API → Database
   - Component breakdown with analogies
   - Synchronous processing (simple but slow)

3. [Step 2: Adding Caching Layer](./step2_add_caching.md)
   - Redis for user preferences and rate limiting
   - Cache-aside vs write-through patterns
   - 75% reduction in database load

4. [Step 3: Database Scaling](./step3_database_scaling.md)
   - Master-slave replication for high availability
   - Sharding by user_id (3 shards)
   - Read-write separation

5. [Step 4: Message Queue](./step4_add_message_queue.md)
   - Apache Kafka for async processing
   - Worker pools (email, SMS, push)
   - Retry logic and Dead Letter Queue
   - API response: 500ms → 50ms (10x faster!)

6. [Step 5: CDN & Object Storage](./step5_add_cdn_storage.md)
   - CloudFlare CDN for global distribution
   - AWS S3 for templates and attachments
   - Email size: 1 MB → 50 KB (20x smaller)

7. [Step 6: Final Architecture](./step6_final_architecture.md)
   - Microservices breakdown
   - API Gateway (Kong)
   - Auto-scaling with Kubernetes
   - Multi-region deployment

### Deep Dives

8. [Data Flow Diagrams](./data_flow_diagrams.md)
   - Send notification (happy path)
   - Retry with failure handling
   - Scheduled notifications
   - Bulk notifications (1M users)
   - Cache invalidation flow

9. [API Design](./api_design.md)
   - RESTful endpoints
   - Authentication (API keys, OAuth)
   - Rate limiting
   - Webhooks for events

10. [Database Design](./database_design.md)
    - Schema (notifications, user_preferences, templates)
    - Indexes and partitioning
    - Sharding strategy
    - Replication setup

11. [Scalability & Reliability](./scalability_reliability.md)
    - Horizontal vs vertical scaling
    - Circuit breaker pattern
    - Retry with exponential backoff
    - Multi-region disaster recovery
    - Security (encryption, authentication)
    - Monitoring and alerting

### Interview Preparation

12. [Interview Q&A](./interview_qa.md) - Must read!
    - 10 common system design questions with detailed answers
    - Capacity planning examples
    - Trade-off discussions
    - Failure scenario handling

13. [Common Beginner Mistakes](./beginner_mistakes.md)
    - 10 mistakes to avoid
    - Wrong vs right approaches
    - Code examples showing pitfalls

14. [Summary & Checklist](./summary_and_checklist.md)
    - Design decisions summary
    - Technologies chosen and why
    - Cost breakdown
    - What to draw first in interview
    - Complete checklist

---

## Learning Path

### For Beginners (First Time Learning HLD)

**Week 1: Understand the Problem**
1. Read [Requirements & Estimations](./requirements_and_estimations.md)
2. Practice capacity calculations with different numbers
3. Read [Step 1: Basic Architecture](./step1_basic_architecture.md)

**Week 2: Build Complexity Incrementally**
4. Read Steps 2-4 (caching, database scaling, message queue)
5. Understand why each component is added
6. Draw diagrams yourself (pen and paper!)

**Week 3: Complete the Design**
7. Read Steps 5-6 (CDN, final architecture)
8. Read [Data Flow Diagrams](./data_flow_diagrams.md)
9. Trace notification flow from client to delivery

**Week 4: Deep Dives & Interview Prep**
10. Read [API Design](./api_design.md) and [Database Design](./database_design.md)
11. Read [Interview Q&A](./interview_qa.md)
12. Practice whiteboard design (30-minute timer)

---

### For Interview Preparation (1-2 Weeks Before)

**Day 1-2: Review Architecture**
- Skim all architecture steps (Step 1-6)
- Focus on **why** each component was added
- Memorize capacity numbers (10M DAU, 600 RPS, 36 TB/year)

**Day 3-4: Practice Drawing**
- Draw basic architecture 5 times (timed: 10 minutes)
- Draw final architecture 3 times (timed: 15 minutes)
- Practice explaining verbally while drawing

**Day 5-6: Study Trade-offs**
- Read [Summary & Checklist](./summary_and_checklist.md)
- For each decision, know: Why chosen? What's the alternative? Trade-offs?
- Practice: "Why SQL over NoSQL?" "Why Kafka over RabbitMQ?"

**Day 7: Mock Interview**
- Give yourself problem: "Design a notification system"
- 45 minutes on whiteboard (or digital tool)
- Record yourself explaining
- Compare with this guide

**Day 8-10: Q&A Practice**
- Read all [Interview Q&A](./interview_qa.md)
- Practice answering out loud
- Focus on failure scenarios and scaling

**Day 11-12: Common Mistakes**
- Read [Common Beginner Mistakes](./beginner_mistakes.md)
- Identify which mistakes you might make
- Review correct approaches

**Day 13-14: Final Review**
- Quick skim of all documents
- Focus on [Summary & Checklist](./summary_and_checklist.md)
- Practice "What to draw first" checklist

---

## Key Concepts Covered

### Architecture Patterns
- Microservices vs Monolith
- Async processing with message queues
- Master-slave database replication
- Database sharding
- Multi-layer caching (L1, L2, L3)
- API Gateway pattern
- Circuit Breaker pattern
- Exactly-once delivery
- Idempotency

### Scalability
- Horizontal scaling (scale out)
- Vertical scaling (scale up)
- Auto-scaling (Kubernetes HPA)
- Load balancing (round-robin, least connections)
- Database scaling (sharding, replication)
- Caching strategies (cache-aside, write-through)

### Reliability
- High availability (99.99% uptime)
- Disaster recovery (multi-region)
- Graceful degradation
- Retry with exponential backoff
- Dead Letter Queue
- Health checks and monitoring
- Circuit breaker

### Data Modeling
- SQL vs NoSQL (when to use each)
- Schema design (normalization)
- Indexes (B-tree, GIN)
- Partitioning (by time)
- Sharding (by user_id)
- JSONB for flexible data

### Performance
- Latency optimization (<100ms P99)
- Throughput scaling (600 → 6,000 RPS)
- Cache hit rates (>99%)
- Database query optimization
- CDN for global distribution

---

## System Specifications

| Metric | Value |
|--------|-------|
| **Users** | 10M DAU, 100M registered |
| **Throughput** | 600 req/sec (avg), 6,000 req/sec (peak) |
| **Notifications/Day** | 50 million |
| **Channels** | Email, SMS, Push, In-App |
| **Latency (P99)** | < 100ms |
| **Uptime** | 99.99% (52 min downtime/year) |
| **Delivery Rate** | 99.9% |
| **Storage** | 100 GB/day, 36 TB/year |
| **Cost** | $8,235/month (~$0.0055 per notification) |

---

## Technologies Used

| Component | Technology | Alternatives Considered |
|-----------|-----------|------------------------|
| Database | PostgreSQL | MySQL, MongoDB, Cassandra |
| Cache | Redis | Memcached |
| Message Queue | Apache Kafka | RabbitMQ, AWS SQS |
| Load Balancer | Nginx | HAProxy, AWS ALB |
| API Gateway | Kong | AWS API Gateway, Apigee |
| CDN | CloudFlare | AWS CloudFront, Akamai |
| Object Storage | AWS S3 | Google Cloud Storage, Azure Blob |
| Monitoring | Prometheus + Grafana | Datadog, New Relic |
| Logging | ELK Stack | Splunk, Loki |
| Orchestration | Kubernetes | Docker Swarm, ECS |
| Email Provider | SendGrid | Mailgun, AWS SES |
| SMS Provider | Twilio | Nexmo, AWS SNS |
| Push Provider | Firebase FCM | OneSignal, AWS SNS |

---

## Mermaid Diagrams Included

This guide includes **15+ Mermaid diagrams** showing:
- Architecture evolution (Step 1-6)
- Data flow sequences
- Database replication
- Kafka partitions
- Cache architecture
- Multi-region setup
- Failure scenarios

All diagrams are rendered inline and can be copied for your presentations!

---

## Beginner-Friendly Features

This guide is designed for beginners with:

### Real-World Analogies
- Load Balancer = Restaurant host assigning tables
- Cache = Tools on your desk vs toolbox
- CDN = McDonald's in every city
- Message Queue = Post office

### Step-by-Step Calculations
```
10M users × 5 notifications/day = 50M notifications/day
50M / 86,400 seconds = 578 req/sec
Peak (3x) = 1,800 req/sec
```

### Code Examples
- JavaScript/Node.js code for all components
- SQL queries with explanations
- Configuration files (Kubernetes, Redis, PostgreSQL)

### Before/After Comparisons
- API latency: 500ms → 50ms (with queue)
- Database load: 2,400 queries/sec → 600 queries/sec (with cache)
- Email size: 1 MB → 50 KB (with CDN)

### Multiple Visualizations
- Architecture diagrams
- Sequence diagrams
- Data flow diagrams
- Comparison tables
- Mermaid charts

---

## Interview Tips

### Do's ✓
- Start with requirements (clarify ambiguous points)
- Show capacity calculations (DAU → RPS → Storage)
- Draw architecture incrementally (simple → complex)
- Discuss trade-offs for each decision
- Address failure scenarios ("What if database crashes?")
- Think out loud (interviewer sees your thought process)
- Ask clarifying questions

### Don'ts ✗
- Jump to final architecture immediately
- Use buzzwords without understanding ("Let's use blockchain!")
- Ignore scaling ("This works for 1000 users, ship it!")
- Forget about costs
- Skip capacity planning
- Design for perfection (perfect is the enemy of good)

---

## Practice Questions

After reading this guide, practice these variations:

1. **Scale Up:** Design for 100M users instead of 10M
2. **Scale Down:** Design for 100K users (what can you simplify?)
3. **New Channel:** Add WhatsApp or Slack notifications
4. **Stricter Latency:** Reduce P99 latency to <10ms
5. **Lower Cost:** Reduce monthly cost by 50%
6. **Higher Reliability:** Achieve 99.999% uptime (5 nines)
7. **Different Problem:** Use same patterns for email marketing system
8. **Constraint:** Design without using cloud (on-premise only)

---

## Contributing

Found a typo? Have a suggestion? This is a learning resource!

Common improvements:
- Add more code examples
- Explain concepts in different ways
- Add diagrams for clarity
- Include more interview questions
- Add failure scenario examples

---

## Credits & References

**Inspired by:**
- Designing Data-Intensive Applications (Martin Kleppmann)
- System Design Interview (Alex Xu)
- Real-world notification systems: SendGrid, Twilio, Firebase

**Technologies:**
- Apache Kafka, PostgreSQL, Redis, Nginx, Kubernetes
- AWS, Google Cloud, CloudFlare

---

## License

This educational resource is free to use for learning and interview preparation.

---

## Quick Reference Card

**Elevator Pitch (30 seconds):**
```
"I would design a notification system with asynchronous processing using Kafka,
sharded PostgreSQL databases for horizontal scaling, Redis for caching with 99%
hit rate, and CDN for global asset delivery. This handles 10 million daily users
sending 50 million notifications per day with 99.99% uptime and sub-100ms latency.
The system costs $8,000/month and can scale to 100M users by adding more shards
and workers."
```

**One-Sentence Summary:**
Async processing with message queue, sharded database, multi-layer caching, and multi-region deployment.

---

**Start your learning journey:** Begin with [Requirements & Estimations](./requirements_and_estimations.md) →

**Ready for interview:** Read [Summary & Checklist](./summary_and_checklist.md) →

**Practice questions:** Study [Interview Q&A](./interview_qa.md) →

Good luck with your system design interview! 🚀
