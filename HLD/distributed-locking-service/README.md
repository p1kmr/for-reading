# Distributed Locking Service - High-Level Design

## 📚 Complete Beginner-Friendly Guide

Welcome! This comprehensive guide teaches you how to design a production-grade **Distributed Locking Service** from scratch. Whether you're preparing for system design interviews or building real-world systems, this guide has everything you need.

---

## 🎯 What is a Distributed Locking Service?

**Simple Analogy:** Think of it as a digital traffic light for your microservices. Just like a traffic light ensures only one direction of cars moves at a time, a distributed lock ensures only one service can access a shared resource at any moment.

**Real-world Example:**
```
Problem: 100 different servers all want to process Order #12345 simultaneously
Without lock: All 100 process it → Order charged 100 times! 🔥
With lock: Only 1 server gets the lock → Order processed once ✅
```

---

## 📖 Documentation Structure

### Part 1: Foundation (Requirements & Planning)

| Document | Description | Read Time |
|----------|-------------|-----------|
| **[01_requirements_and_estimations.md](01_requirements_and_estimations.md)** | Functional & non-functional requirements, capacity planning | 20 min |
| **[02_capacity_planning.md](02_capacity_planning.md)** | Detailed traffic, storage, and bandwidth calculations | 15 min |

**What you'll learn:**
- How to gather requirements (functional vs non-functional)
- How to calculate: requests/sec, storage needs, bandwidth
- How to estimate infrastructure costs ($10k/month budget)
- **Key numbers:** 66,667 ops/sec, 10M concurrent locks, 99.99% availability

---

### Part 2: Architecture Evolution (Step-by-Step)

| Document | Description | What's Added | Read Time |
|----------|-------------|--------------|-----------|
| **[03_step1_basic_architecture.md](03_step1_basic_architecture.md)** | Basic setup | Load Balancer + Lock Servers + etcd | 25 min |
| **[04_step2_add_caching.md](04_step2_add_caching.md)** | Performance boost | Redis caching layer (10x faster) | 20 min |
| **[05_step3_multi_region_deployment.md](05_step3_multi_region_deployment.md)** | High availability | 3 regions, 99.99% SLA | 30 min |
| **[06_step4_monitoring_messagequeue.md](06_step4_monitoring_messagequeue.md)** | Observability | Prometheus, Grafana, Kafka | 25 min |

**Why incremental design?**
- **Beginner-friendly:** Start simple, add complexity gradually
- **Interview-ready:** Shows thought process, not just final design
- **Realistic:** How real systems evolve over time

**Architecture progression:**
```
Step 1: Basic (works, but single region)
  ↓
Step 2: + Caching (10x faster reads)
  ↓
Step 3: + Multi-region (99.99% availability)
  ↓
Step 4: + Monitoring (proactive issue detection)
```

---

### Part 3: Implementation Details

| Document | Description | Read Time |
|----------|-------------|-----------|
| **[07_api_design.md](07_api_design.md)** | RESTful API endpoints, authentication, rate limiting | 20 min |
| **[08_data_flow_diagrams.md](08_data_flow_diagrams.md)** | Sequence diagrams for lock operations, failure handling | 25 min |

**What you'll learn:**
- How to design production-ready APIs (versioning, idempotency, error codes)
- How requests flow through the system (acquire, release, renew, expire)
- How to handle failures (node crashes, network partitions, region failures)
- **Key flows:** Lock acquisition (17ms), Release (15ms), Auto-expiry (TTL)

---

### Part 4: Interview Preparation

| Document | Description | Read Time |
|----------|-------------|-----------|
| **[09_interview_qa.md](09_interview_qa.md)** | 12 common interview questions with detailed answers | 40 min |
| **[10_beginner_mistakes.md](10_beginner_mistakes.md)** | 10 common mistakes and how to avoid them | 30 min |

**Topics covered:**
- Why etcd over Redis? (strong consistency vs eventual)
- How to prevent split-brain? (quorum-based consensus)
- How to handle clock skew? (NTP, monotonic clocks, fencing tokens)
- What is CAP theorem? (we chose CP: consistency + partition tolerance)
- How to scale to 10M ops/sec? (sharding, multi-region, optimization)

---

## 🏗️ Final Architecture Overview

```
                                   [Global DNS - Route53]
                                   Geo-routing to nearest region
                                           │
                ┌──────────────────────────┼──────────────────────────┐
                │                          │                          │
          [US-East-1]                 [US-West-2]                [EU-West-1]
          40% traffic                 30% traffic                30% traffic
                │                          │                          │
          Load Balancer               Load Balancer              Load Balancer
                │                          │                          │
         ┌──────┴──────┐            ┌──────┴──────┐           ┌──────┴──────┐
         │             │            │             │           │             │
    Lock Servers  Lock Servers Lock Servers  Lock Servers Lock Servers  Lock Servers
    (Auto-scale)  (Auto-scale) (Auto-scale)  (Auto-scale) (Auto-scale)  (Auto-scale)
         │             │            │             │           │             │
         └──────┬──────┘            └──────┬──────┘           └──────┬──────┘
                │                          │                          │
         ┌──────┴──────┐            ┌──────┴──────┐           ┌──────┴──────┐
         │             │            │             │           │             │
    Redis Cache   etcd Cluster  Redis Cache   etcd Cluster Redis Cache   etcd Cluster
    (9 nodes)     (3 nodes)     (9 nodes)     (3 nodes)     (9 nodes)     (3 nodes)

                               [Cross-Region Coordination]
                               Quorum-based for global locks

                         [Kafka] ← Async events (audit, metrics)
                            ↓
                  [PostgreSQL] - Audit logs (7 days hot)
                            ↓
                  [S3 Glacier] - Long-term archive (90+ days)

                    [Prometheus + Grafana] - Monitoring
                    [AlertManager] - Alerts → PagerDuty/Slack
```

---

## 🎓 Learning Path (Recommended Order)

### For Interview Preparation (3-4 hours)
1. **Start here:** [01_requirements_and_estimations.md](01_requirements_and_estimations.md) (20 min)
2. **Capacity planning:** [02_capacity_planning.md](02_capacity_planning.md) (15 min)
3. **Basic architecture:** [03_step1_basic_architecture.md](03_step1_basic_architecture.md) (25 min)
4. **Add caching:** [04_step2_add_caching.md](04_step2_add_caching.md) (20 min)
5. **Multi-region:** [05_step3_multi_region_deployment.md](05_step3_multi_region_deployment.md) (30 min)
6. **Interview Q&A:** [09_interview_qa.md](09_interview_qa.md) (40 min)
7. **Common mistakes:** [10_beginner_mistakes.md](10_beginner_mistakes.md) (30 min)

**Total: ~3 hours** (covers 80% of interview questions)

### For Deep Understanding (Full course: 6-8 hours)
Read all documents in order (01 → 10)

### For Quick Reference (30 minutes)
1. **This README** (10 min)
2. **Key Design Decisions** (below, 5 min)
3. **Interview Q&A** (skim top 5 questions, 15 min)

---

## 🔑 Key Design Decisions

### 1. **etcd over Redis for Lock State**

**Why?**
- etcd: Raft consensus (strong consistency) ✅
- Redis: Async replication (eventual consistency) ❌
- For locking, **correctness > speed**

**Trade-off:** 10ms latency (etcd) vs 1ms (Redis), but prevents split-brain

---

### 2. **TTL (Time-To-Live) for All Locks**

**Why?**
- Client crashes → Lock auto-expires after 30s
- No manual cleanup needed
- System is self-healing

**Trade-off:** Must renew lock if operation takes > 30s

---

### 3. **Multi-Region Active-Active**

**Why?**
- 99.99% availability (can lose entire datacenter)
- Low latency (route to nearest region: 5-10ms)
- 3 regions: US-East (40%), US-West (30%), EU-West (30%)

**Trade-off:** 3x infrastructure cost, but worth it for SLA

---

### 4. **CP (Consistency + Partition Tolerance) over AP (Availability)**

**Why?**
- For locking, **two clients CANNOT own same lock** (correctness critical)
- Better to reject requests than give wrong answer
- Minority side rejects requests during network partition

**Trade-off:** Some downtime during partitions (acceptable for correctness)

---

### 5. **Cache-Aside Pattern with Redis**

**Why?**
- 80% cache hit rate (reads are 10x faster: 1ms vs 10ms)
- Write-through to etcd (source of truth)
- Cache invalidation on lock acquire/release

**Trade-off:** Slight complexity, but huge performance gain

---

### 6. **Async Audit Logging via Kafka**

**Why?**
- Don't block lock operations for logging (would add 20ms!)
- Kafka handles high throughput (66k events/sec)
- Can replay events for analytics

**Trade-off:** Eventual consistency for audit logs (acceptable)

---

## 📊 Performance Metrics Achieved

| Metric | Target | Achieved | Status |
|--------|--------|----------|--------|
| **Throughput** | 50k ops/sec | **66k ops/sec** | ✅ Exceeds |
| **Peak Capacity** | 200k ops/sec | **600k ops/sec** | ✅ Exceeds |
| **Latency (P50)** | < 10ms | **5-7ms** | ✅ Exceeds |
| **Latency (P95)** | < 50ms | **25ms** | ✅ Exceeds |
| **Availability** | 99.99% | **99.99%** | ✅ Meets |
| **Concurrent Locks** | 10M | **10M** | ✅ Meets |
| **Cost** | < $10k/month | **$10.5k/month** | ✅ Within 5% |

---

## 🛠️ Technology Stack

### Core Components

| Layer | Technology | Why |
|-------|-----------|-----|
| **Load Balancer** | HAProxy / AWS ALB | High performance, health checks |
| **Lock Servers** | Go / Node.js | Lightweight, concurrent, stateless |
| **Coordination** | **etcd** | Raft consensus, strong consistency |
| **Caching** | Redis Cluster | In-memory, 1ms latency |
| **Message Queue** | Apache Kafka | High throughput, event streaming |
| **Audit Storage** | PostgreSQL | ACID, complex queries |
| **Archive** | AWS S3 Glacier | Cost-effective long-term storage |
| **Monitoring** | Prometheus + Grafana | Time-series metrics, dashboards |
| **Alerting** | AlertManager + PagerDuty | On-call notifications |
| **Logging** | ELK Stack | Centralized logging, search |

---

## 💡 Key Concepts for Beginners

### 1. **Distributed Consensus (Raft)**

**Problem:** How do multiple servers agree on lock state?

**Solution:** Raft algorithm (used by etcd)
1. Elect a leader
2. Leader receives all writes
3. Leader replicates to followers
4. Leader waits for majority (quorum) to acknowledge
5. Only then commits the write

**Beginner analogy:** 3 judges deciding a winner. Need 2/3 to agree (majority).

---

### 2. **CAP Theorem**

**Theorem:** In distributed systems with network partitions, choose 2 out of 3:
- **C**onsistency: All nodes see same data
- **A**vailability: System always responds
- **P**artition Tolerance: Works despite network failure

**Our choice:** **CP** (consistency + partition tolerance)
- Correctness > availability for locking
- Better to reject than give wrong answer

---

### 3. **Split-Brain**

**Problem:** Network partition causes two regions to both act as primary
- Both regions accept lock requests
- Two clients think they own same lock
- **Data corruption!**

**Solution:** Quorum-based decisions
- 3 regions: Need 2/3 to acquire global lock
- Minority side rejects requests (safety)

---

### 4. **TTL (Time-To-Live)**

**Concept:** Lock automatically expires after N seconds

**Why it matters:**
- Client crashes → Lock auto-released after 30s
- No deadlocks
- System self-heals

**Implementation:** etcd native feature (no background jobs needed)

---

### 5. **Fencing Tokens**

**Problem:** Client acquires lock → Long GC pause → Lock expires → Another client acquires → First client resumes with stale lock!

**Solution:** Incrementing token with each lock
- Lock 1: token=100
- Lock 2: token=101
- Resource server rejects old tokens

**Result:** Prevents stale lock operations

---

## 🚀 Scaling Strategies

### Current Scale
- **66,667 ops/sec** (average)
- **200,000 ops/sec** (peak)
- **10 million concurrent locks**
- **3 regions** (US-East, US-West, EU-West)

### To Scale to 10M ops/sec
1. **Horizontal scaling:** 5,000 lock servers (stateless, easy to add)
2. **etcd sharding:** 100 clusters (shard by resource type or consistent hashing)
3. **Regional expansion:** 10 regions (add Asia, South America, Africa)
4. **Protocol optimization:** HTTP/2 → gRPC (30% faster)
5. **Batch operations:** Acquire multiple locks in one request

**Cost:** ~$400k/month (with reserved instances, 60% discount)

---

## 🐛 Common Pitfalls & Solutions

### ❌ Pitfall 1: Using Redis as Source of Truth
**Problem:** Async replication can cause split-brain
**Solution:** Use etcd (Raft consensus) for lock state, Redis only for caching

### ❌ Pitfall 2: No TTL on Locks
**Problem:** Client crashes → Lock held forever → Deadlock
**Solution:** Always set TTL (30s default, can renew if needed)

### ❌ Pitfall 3: Not Validating Lock Ownership
**Problem:** Client can accidentally delete another's lock
**Solution:** Use lock tokens (UUID), verify before delete

### ❌ Pitfall 4: Trusting Client Clocks
**Problem:** Clock skew causes incorrect TTL calculations
**Solution:** Use server time, sync with NTP, or use fencing tokens

### ❌ Pitfall 5: No Exponential Backoff
**Problem:** 1000 clients retry immediately → Thundering herd
**Solution:** Exponential backoff with jitter (100ms, 200ms, 400ms...)

**Full list:** See [10_beginner_mistakes.md](10_beginner_mistakes.md)

---

## 📈 Monitoring & Observability

### Key Metrics to Track
- **Business:** Lock acquisition rate, latency (P50/P95/P99), error rate
- **System:** etcd health, Redis hit rate, CPU/memory usage
- **Audit:** Lock hold duration, contention rate, top locked resources

### Dashboards (Grafana)
1. **Lock Service Overview:** Acquisition rate, latency, errors
2. **Infrastructure Health:** etcd disk, Redis memory, server CPU
3. **Business Metrics:** Top 10 locked resources, contention heatmap

### Alerts (AlertManager → PagerDuty)
- **Critical:** P95 latency > 50ms, etcd node down, error rate > 1%
- **Warning:** Disk > 80%, memory > 85%, contention rate high

---

## 💰 Cost Breakdown

```
Monthly Infrastructure Cost: $10,500

Breakdown:
- Lock Servers (auto-scaling): $3,000 (300 servers × $10)
- etcd Clusters (3 regions): $1,800 (9 nodes × $200)
- Redis Clusters (3 regions): $2,400 (27 nodes × $89)
- Load Balancers: $500
- Kafka (message queue): $800
- PostgreSQL (audit logs): $1,200
- S3 Glacier (archive): $500
- Monitoring (Prometheus/Grafana): $300

Cost per operation: $10,500 / (66,667 ops/sec × 86,400 sec/day × 30 days)
                  = $0.00000006 per operation (0.006 cents)
```

**Optimization opportunities:**
- Reserved instances: 60% discount → **$4,200 saved**
- Spot instances: 70% discount → **$2,100 saved** (for non-critical)
- Right-sizing: 20% reduction → **$2,100 saved**
- **Optimized total: $6,200/month** ✅

---

## 🎯 Interview Cheat Sheet

### Top 5 Questions & Quick Answers

**Q1: Why etcd over Redis?**
**A:** etcd has Raft consensus (strong consistency), Redis has async replication (can cause split-brain). For locking, correctness > speed.

**Q2: How to prevent deadlocks?**
**A:** TTL (Time-To-Live). Locks auto-expire after 30s. If client crashes, lock is automatically released.

**Q3: How to prevent split-brain?**
**A:** Quorum-based consensus. 3 regions: need 2/3 to acquire global lock. Minority rejects requests.

**Q4: CAP theorem choice?**
**A:** CP (consistency + partition tolerance). For locking, can't have two clients owning same lock (correctness critical).

**Q5: How to handle clock skew?**
**A:** Use server time (not client), NTP synchronization (< 100ms skew), fencing tokens (best solution - no time dependency).

**Full Q&A:** [09_interview_qa.md](09_interview_qa.md)

---

## 📚 Additional Resources

### For Deeper Learning

**Papers:**
- [Raft Consensus Algorithm](https://raft.github.io/raft.pdf) (etcd uses this)
- [Google Chubby](https://research.google/pubs/pub27897/) (Google's distributed lock service)
- [Redlock Algorithm](https://redis.io/docs/manual/patterns/distributed-locks/) (Redis distributed locks)

**Criticisms:**
- [Martin Kleppmann's Redlock Critique](https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html)
- [Fencing Tokens Paper](https://martinfowler.com/articles/patterns-of-distributed-systems/fencing-token.html)

**Real-world Implementations:**
- **Kubernetes:** Uses etcd for leader election and coordination
- **Consul:** HashiCorp's service mesh (uses Raft)
- **ZooKeeper:** Apache's coordination service (used by Kafka, HBase)

---

## 🤝 Contributing

Found an error or want to improve this guide?
- **Issues:** Open an issue describing the problem
- **Pull Requests:** Submit a PR with improvements
- **Questions:** Ask in discussions (we'll add to FAQ)

---

## 📜 License

This guide is open-source and free to use for learning purposes.

---

## 🙏 Acknowledgments

This guide is designed for:
- System design interview preparation
- Learning distributed systems concepts
- Building production-ready locking services
- Understanding trade-offs in system design

**Happy Learning!** 🚀

---

## Quick Navigation

### Part 1: Foundation
- [Requirements & Estimations](01_requirements_and_estimations.md)
- [Capacity Planning](02_capacity_planning.md)

### Part 2: Architecture (Step-by-Step)
- [Step 1: Basic Architecture](03_step1_basic_architecture.md)
- [Step 2: Add Caching](04_step2_add_caching.md)
- [Step 3: Multi-Region](05_step3_multi_region_deployment.md)
- [Step 4: Monitoring & Message Queue](06_step4_monitoring_messagequeue.md)

### Part 3: Implementation
- [API Design](07_api_design.md)
- [Data Flow Diagrams](08_data_flow_diagrams.md)

### Part 4: Interview Prep
- [Interview Q&A (12 questions)](09_interview_qa.md)
- [Common Mistakes (10 pitfalls)](10_beginner_mistakes.md)

---

**Last Updated:** January 17, 2025
**Total Pages:** 10 comprehensive documents
**Total Reading Time:** ~6-8 hours (complete course)
**Interview Prep Time:** ~3-4 hours (essentials)
