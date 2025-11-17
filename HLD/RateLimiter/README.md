# Rate Limiter - High-Level Design (HLD)

## 🎯 Overview

This is a **comprehensive, beginner-friendly** High-Level Design (HLD) document for a **Distributed Rate Limiter** system. It covers everything from basic concepts to production-ready architecture.

**What is a Rate Limiter?**

A rate limiter is a system that controls the number of requests a user can make to an API within a specific time window. It:
- 🛡️ Protects services from abuse and DDoS attacks
- ⚡ Prevents resource exhaustion
- ⚖️ Ensures fair usage among clients
- 💰 Enforces tiered pricing (Free, Premium, Enterprise)

**Example:** If you allow 100 requests per minute, the 101st request gets rejected with **HTTP 429 Too Many Requests**.

---

## 📚 Document Structure

This HLD is organized into **9 comprehensive documents**, designed to be read in sequence:

### 📖 Learning Path

| # | Document | Topics Covered | Time |
|---|----------|----------------|------|
| **1** | [Requirements & Estimations](./1_requirements_and_estimations.md) | Functional/non-functional requirements, capacity planning, traffic estimates | 20 min |
| **2** | [Step 1: Basic Architecture](./2_step1_basic_architecture.md) | Client → Gateway → Rate Limiter → Backend flow, in-memory counters | 15 min |
| **3** | [Step 2: Add Distributed Cache](./3_step2_add_caching.md) | Redis cluster for shared counters, solving distributed challenges | 20 min |
| **4** | [Step 3: Rate Limiting Algorithms](./4_step3_rate_limiting_algorithms.md) | 5 algorithms (Fixed Window, Sliding Window, Token Bucket, Leaky Bucket, Sliding Window Log) with examples | 30 min |
| **5** | [Step 4: Database Design](./5_step4_database_design.md) | PostgreSQL schema, API keys, rate limit rules, caching strategy | 20 min |
| **6** | [Step 5: API Design & Data Flow](./6_step5_api_design.md) | RESTful APIs, sequence diagrams, response headers, error handling | 20 min |
| **7** | [Step 6: Final Architecture](./7_step6_final_architecture.md) | Complete production architecture with all components integrated | 25 min |
| **8** | [Scalability & Reliability](./8_scalability_reliability.md) | Scaling to 10x traffic, high availability (99.99%), monitoring | 25 min |
| **9** | [Interview Q&A & Summary](./9_interview_qa_and_summary.md) | 15 interview questions, common mistakes, design summary | 30 min |

**Total Learning Time:** ~3.5 hours

---

## 🚀 Quick Start

### For Beginners (Never designed a system before)

**Start here:** Read documents in order from #1 to #9

**Key Milestones:**
- After Doc #2: Understand basic architecture
- After Doc #4: Know all 5 algorithms
- After Doc #7: Can draw complete system architecture
- After Doc #9: Ready for interviews!

### For Intermediate (Have some system design experience)

**Quick Path:**
1. [Requirements](./1_requirements_and_estimations.md) - Scan capacity calculations
2. [Algorithms](./4_step3_rate_limiting_algorithms.md) - Deep dive into algorithms
3. [Final Architecture](./7_step6_final_architecture.md) - Complete production design
4. [Interview Q&A](./9_interview_qa_and_summary.md) - Prepare for interviews

### For Advanced (Reviewing for interviews)

**Interview Prep Path:**
1. [Algorithms](./4_step3_rate_limiting_algorithms.md) - Refresh algorithm knowledge
2. [Final Architecture](./7_step6_final_architecture.md) - Review complete design
3. [Scalability](./8_scalability_reliability.md) - Scaling and reliability patterns
4. [Interview Q&A](./9_interview_qa_and_summary.md) - Practice questions

---

## 🎓 What You'll Learn

### Core Concepts

✅ **Rate Limiting Fundamentals**
- What is rate limiting and why is it needed?
- Functional vs non-functional requirements
- Traffic estimation and capacity planning

✅ **5 Rate Limiting Algorithms**
- Fixed Window Counter (simple, has edge cases)
- Sliding Window Log (accurate, memory-intensive)
- Sliding Window Counter (recommended for production)
- Token Bucket (allows bursts)
- Leaky Bucket (smooths traffic)

✅ **Distributed Systems**
- Shared counters across multiple servers
- Redis cluster (sharding, replication)
- Strong consistency requirements
- Handling clock skew

### System Design

✅ **Architecture Components**
- API Gateway (centralized enforcement)
- Rate Limiter Service (stateless, scalable)
- Redis Cluster (distributed counters)
- PostgreSQL (configuration storage)
- Monitoring (Prometheus, Grafana)

✅ **Database Design**
- User tiers (Free, Premium, Enterprise)
- Rate limit rules (per tier, per endpoint)
- API keys (authentication)
- Violations tracking (analytics)

✅ **API Design**
- RESTful management APIs
- Standard rate limit headers (X-RateLimit-*)
- Error responses (HTTP 429)
- Client best practices

### Production Readiness

✅ **Scalability**
- Horizontal scaling (3 → 100+ instances)
- Redis cluster expansion (3 → 9+ shards)
- Auto-scaling (Kubernetes HPA)
- Multi-region deployment

✅ **High Availability (99.99%)**
- No single point of failure
- Automatic failover (Redis Sentinel)
- Circuit breaker pattern
- Graceful degradation

✅ **Monitoring & Observability**
- Key metrics (latency, throughput, errors)
- Dashboards (Grafana)
- Alerting (PagerDuty)
- Debugging techniques

---

## 📊 System Capabilities

### Performance Metrics

| Metric | Value |
|--------|-------|
| **Latency (P50)** | < 2ms |
| **Latency (P99)** | < 10ms |
| **Throughput** | 3,500 req/sec (scalable to 100K+) |
| **Availability** | 99.99% (52 min downtime/year) |
| **DAU Supported** | 1M (scalable to 100M+) |

### Architecture Highlights

```
Client Layer
    ↓
CDN & DDoS Protection (CloudFlare)
    ↓
Load Balancer (Nginx, 2 instances)
    ↓
API Gateway (Kong, 3+ instances)
    ↓
Rate Limiter Service (3+ instances, auto-scaling)
    ↓
Redis Cluster (3 masters + 3 replicas, sharding)
    ↓
PostgreSQL (1 master + 2 replicas)
    ↓
Backend API (3+ instances)
```

---

## 🔍 Key Features

### Functional Features

✅ **Per-User Rate Limiting**
- 100 requests per minute (configurable)
- HTTP 429 response with Retry-After header

✅ **Multi-Window Support**
- Per second (burst protection)
- Per minute (standard limits)
- Per hour (long-term quotas)

✅ **Flexible Configuration**
- Different limits per user tier (Free, Premium, Enterprise)
- Different limits per endpoint (/login: 5/min, /api: 100/min)
- Priority-based rule matching

✅ **Global Enforcement**
- Consistent limits across all servers
- No bypassing by server-hopping

### Non-Functional Features

✅ **Low Latency:** < 5ms rate limit check (P99)
✅ **High Throughput:** 100,000+ req/sec per node
✅ **Scalability:** Horizontal scaling (add more instances)
✅ **Availability:** 99.99% uptime (auto-failover)
✅ **Strong Consistency:** All nodes see same counters
✅ **Fault Tolerance:** Graceful degradation on failures

---

## 💡 Algorithm Comparison

| Algorithm | Accuracy | Memory | Complexity | Best For |
|-----------|----------|--------|------------|----------|
| **Fixed Window** | ⚠️ Medium | ✅ Low | ✅ Simple | Internal APIs |
| **Sliding Window Log** | ✅ Perfect | ❌ High | ⚠️ Complex | Financial APIs |
| **Sliding Window Counter** | ✅ Good | ✅ Low | ⚠️ Medium | **Production (Recommended)** |
| **Token Bucket** | ✅ Good | ✅ Low | ⚠️ Medium | File uploads |
| **Leaky Bucket** | ✅ Good | ✅ Low | ⚠️ Medium | Background jobs |

**Recommendation:** Use **Sliding Window Counter** for 90% of use cases.

---

## 🎯 Design Decisions Summary

| Decision | Choice | Reason |
|----------|--------|--------|
| **Counter Storage** | Redis | Fast (1-2ms), atomic operations, TTL support |
| **Config Storage** | PostgreSQL | ACID compliance, complex queries (JOINs) |
| **Algorithm** | Sliding Window Counter | Best balance of accuracy, memory, performance |
| **Placement** | API Gateway | Centralized, cannot be bypassed |
| **Caching** | Multi-level (Local + Redis) | 99% hit rate, < 2ms latency |
| **High Availability** | Redis Sentinel + DB Replicas | Auto-failover < 30 sec |
| **Monitoring** | Prometheus + Grafana | Real-time metrics, alerting |
| **Scaling** | Kubernetes auto-scaling | Linear scaling, cost-effective |

---

## 📈 Capacity Planning Example

**Assumptions:**
- 1 million DAU (Daily Active Users)
- 100 requests per user per day
- Peak traffic = 3x average

**Calculations:**
```
Total requests/day: 1M × 100 = 100 million
Requests/sec (avg): 100M / 86,400 = 1,157 req/sec
Requests/sec (peak): 1,157 × 3 = 3,500 req/sec

Redis memory needed: ~2 GB per instance
PostgreSQL storage: ~350 MB (config data)

Estimated cost: $1,000-$1,500/month (AWS)
```

---

## 🛠️ Technology Stack

### Core Components

| Layer | Technology | Purpose |
|-------|------------|---------|
| **CDN** | CloudFlare | DDoS protection, SSL termination |
| **Load Balancer** | Nginx / HAProxy | Traffic distribution, health checks |
| **API Gateway** | Kong / Tyk | Authentication, routing |
| **Rate Limiter** | Go / Rust / Java | Core rate limiting logic |
| **Cache** | Redis Cluster | Distributed counters (sharding, replication) |
| **Database** | PostgreSQL | Configuration storage (ACID) |
| **Message Queue** | Apache Kafka | Violations logging (async) |
| **Monitoring** | Prometheus + Grafana | Metrics, dashboards, alerts |
| **Logging** | ElasticSearch | Centralized log aggregation |
| **Orchestration** | Kubernetes | Container orchestration, auto-scaling |

---

## 🔥 Interview Questions Covered

This HLD prepares you for **15+ common interview questions**:

1. Why do we need a rate limiter?
2. Where should the rate limiter be placed?
3. Redis vs Database for counters?
4. Fixed Window vs Sliding Window?
5. How to handle distributed systems?
6. What if Redis goes down?
7. How to handle clock skew?
8. Per-endpoint rate limiting?
9. Different tiers (free, premium)?
10. Scaling to 10x traffic?
11. Testing strategies?
12. Algorithm trade-offs?
13. Debugging production issues?
14. WebSocket rate limiting?
15. Handling traffic spikes?

**Full answers with examples:** See [Interview Q&A](./9_interview_qa_and_summary.md)

---

## 🎨 Diagrams & Visualizations

This HLD includes **20+ Mermaid diagrams**:

- ✅ Architecture diagrams (step-by-step evolution)
- ✅ Sequence diagrams (request flows)
- ✅ Entity-relationship diagrams (database schema)
- ✅ State diagrams (circuit breaker)
- ✅ Timeline diagrams (algorithm explanations)
- ✅ Component diagrams (system layers)

**All diagrams are beginner-friendly with explanations!**

---

## 📊 Real-World Examples

**Companies using rate limiters:**

- **GitHub:** 5,000 requests/hour (Fixed Window)
- **Twitter:** 300 tweets per 3 hours (prevent spam)
- **Stripe:** Token Bucket (handle payment bursts)
- **CloudFlare:** Sliding Window Counter (production standard)
- **AWS API Gateway:** Multiple algorithms (user choice)
- **Shopify:** Token Bucket (e-commerce APIs)

---

## 🎓 Prerequisites

**No prerequisites required!** This HLD is designed for beginners.

**Helpful to know (but not required):**
- Basic understanding of HTTP (GET, POST, status codes)
- Familiarity with Redis (or any cache)
- Basic SQL (SELECT, INSERT, UPDATE)
- Understanding of APIs (REST)

**Everything else is explained from scratch!**

---

## 📝 How to Use This HLD

### For Learning

1. **Read sequentially** (Documents #1 → #9)
2. **Take notes** on key concepts
3. **Draw diagrams** yourself (practice!)
4. **Try to explain** to someone else
5. **Complete the checklist** in Document #9

### For Interview Prep

1. **Skim all documents** to get overview
2. **Deep dive** into algorithms (Document #4)
3. **Memorize** final architecture (Document #7)
4. **Practice** answering questions (Document #9)
5. **Draw on whiteboard** without looking!

### For Implementation

1. Start with **Step 1** (basic architecture)
2. Implement **Redis integration** (Step 2)
3. Choose **algorithm** based on use case (Step 3)
4. Design **database schema** (Step 4)
5. Add **monitoring** (Step 8)
6. **Load test** and optimize!

---

## 🚨 Common Pitfalls (Avoid These!)

❌ **Don't use local in-memory counters** (not distributed)
❌ **Don't forget TTL on Redis keys** (memory leak)
❌ **Don't use database for counters** (too slow)
❌ **Don't ignore clock skew** (align to boundaries)
❌ **Don't forget error handling** (Redis failures)
❌ **Don't skip monitoring** (you'll regret it!)

✅ **See full list:** [Common Mistakes](./9_interview_qa_and_summary.md#part-2-common-beginner-mistakes)

---

## 📚 Further Reading

**Advanced Topics (not covered here):**
- Geographic rate limiting (per-region limits)
- Machine learning-based anomaly detection
- Dynamic rate limit adjustment (based on load)
- Rate limiting for GraphQL (operation-based)
- Distributed rate limiting across clouds

**Recommended Resources:**
- [Redis documentation](https://redis.io/docs/)
- [CloudFlare blog on rate limiting](https://blog.cloudflare.com/)
- [Stripe API design](https://stripe.com/docs/rate-limits)
- [System Design Interview book](https://www.amazon.com/System-Design-Interview-insiders-Second/dp/B08CMF2CQF)

---

## 🎯 Success Criteria

**After completing this HLD, you should be able to:**

✅ Explain what a rate limiter is and why it's needed
✅ Draw the complete system architecture from memory
✅ Compare 5 rate limiting algorithms (pros/cons)
✅ Design a distributed rate limiter using Redis
✅ Handle failures gracefully (Redis down, clock skew)
✅ Scale the system to 10x traffic
✅ Answer 15+ interview questions confidently
✅ Estimate capacity (traffic, memory, cost)
✅ Debug production issues (high latency, violations)
✅ Implement a basic rate limiter in your language

---

## 🙏 Acknowledgments

This HLD was created following industry best practices from:
- **CloudFlare** (rate limiting at scale)
- **AWS API Gateway** (multi-algorithm support)
- **Stripe** (token bucket for payments)
- **GitHub** (API rate limiting)
- **Redis Labs** (distributed counters)

---

## 📧 Feedback

**Found this helpful?** Star the repo!
**Found an error?** Open an issue!
**Have suggestions?** Pull requests welcome!

---

## 📄 License

This HLD is provided for **educational purposes**. Feel free to use it for learning, interviews, and implementation!

---

## 🚀 Get Started Now!

**Ready to dive in?**

👉 **Start here:** [1. Requirements & Estimations](./1_requirements_and_estimations.md)

**Happy Learning!** 🎉

---

**Document Version:** 1.0
**Last Updated:** 2025-11-17
**Author:** System Design Expert
**Estimated Reading Time:** 3.5 hours (all documents)

---

## 📋 Quick Reference

**Key Metrics:**
- Latency: < 5ms (P99)
- Throughput: 3,500 req/sec (scalable to 100K+)
- Availability: 99.99%
- DAU: 1M (scalable to 100M+)

**Recommended Stack:**
- Redis (counters)
- PostgreSQL (config)
- Sliding Window Counter (algorithm)
- Kubernetes (orchestration)
- Prometheus (monitoring)

**Total Cost:** $1,000-$1,500/month (AWS, 1M DAU)

---

**Now go build an amazing rate limiter!** 💪
