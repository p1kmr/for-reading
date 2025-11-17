# Rate Limiter - Requirements and Estimations

## Problem Statement

A **rate limiter** is a system that controls the number of requests a user or client can make to an API or service within a specific time window. It helps:
- 🛡️ Protect services from abuse and attacks
- ⚡ Prevent resource exhaustion
- ⚖️ Ensure fair usage among clients

**Example:** If a system allows a maximum of 100 requests per minute, any request beyond that limit would be rejected with an **HTTP 429 Too Many Requests** response.

---

## 1. Functional Requirements

### 1.1 Core Features

✅ **Per-User Rate Limiting**
- Enforce a fixed number of requests per user/API key within a defined time window
- Example: 100 requests per minute per user
- Reject excess requests with HTTP 429 status code
- Include helpful headers: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `Retry-After`

✅ **Global Enforcement Across Distributed Systems**
- Rate limits must be enforced consistently across all server nodes
- Users cannot bypass limits by switching servers
- Shared state across the distributed system

✅ **Multi-Window Support**
- Support multiple time granularities simultaneously:
  - Per second (burst protection)
  - Per minute (standard API limits)
  - Per hour (long-term quota management)
- Example: Maximum 10 requests/second AND 100 requests/minute AND 1000 requests/hour

✅ **Flexible Rate Limit Configuration**
- Support different limits for different:
  - User tiers (Free: 100/min, Premium: 1000/min, Enterprise: 10000/min)
  - API endpoints (Login: 5/min, Search: 100/min, Upload: 10/min)
  - Geographic regions
  - Time of day (peak vs off-peak hours)

✅ **Rate Limit Rules Management**
- Create, read, update, delete rate limit rules
- Support for wildcard patterns (e.g., `/api/v1/*`)
- Priority-based rule matching
- Hot-reload configuration without server restart

### 1.2 API Response Requirements

✅ **Allowed Requests** (HTTP 200)
```http
HTTP/1.1 200 OK
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 45
X-RateLimit-Reset: 1735689600
```

✅ **Blocked Requests** (HTTP 429)
```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1735689600
Retry-After: 30

{
  "error": "Rate limit exceeded",
  "message": "You have exceeded the rate limit of 100 requests per minute",
  "retryAfter": 30
}
```

---

## 2. Non-Functional Requirements

### 2.1 Performance

🚀 **Low Latency**
- Rate limit check should add < 5 milliseconds to request processing
- P99 latency < 10 milliseconds
- Why: Rate limiter is on critical path; cannot slow down every request

⚡ **High Throughput**
- Support at least **100,000 requests per second** per node
- Support **1 million concurrent users**
- Scale to handle **10 billion requests per day**

### 2.2 Scalability

📈 **Horizontal Scalability**
- Add more nodes to handle increased load
- Linear scalability (2x nodes = 2x capacity)
- No single point of failure

📊 **Storage Scalability**
- Efficiently store rate limit counters for millions of users
- Automatic cleanup of expired counters
- Memory-efficient data structures

### 2.3 Availability

🔄 **High Availability: 99.99% uptime**
- **99.99% availability** = 52 minutes of downtime per year
- Graceful degradation: If rate limiter fails, either:
  - **Fail-open:** Allow all requests (prefer availability)
  - **Fail-closed:** Reject all requests (prefer security)
- Redis cluster with replication and automatic failover

### 2.4 Consistency

🔐 **Strong Consistency**
- All nodes must have a consistent view of request counts
- Prevent users from bypassing limits by routing through different servers
- Use distributed locks or atomic operations
- Race condition handling for concurrent requests

**Why Strong Consistency Matters:**
```
❌ Without Consistency:
User makes 100 requests to Server A → Allowed
User makes 100 requests to Server B → Allowed (different counter!)
Total: 200 requests (bypassed the 100 limit!)

✅ With Consistency:
All servers share the same counter in Redis
User makes 100 requests across any servers → 101st request is blocked
```

### 2.5 Reliability

💾 **Data Durability**
- Rate limit counters can be ephemeral (stored in memory)
- Rate limit configurations must be persisted (database)
- Survive node crashes without losing configuration

🔧 **Fault Tolerance**
- Handle Redis failures gracefully
- Automatic retry with exponential backoff
- Circuit breaker pattern to prevent cascade failures

### 2.6 Security

🔒 **DDoS Protection**
- Rate limiter itself should not be vulnerable to DDoS
- Efficient algorithms that don't consume excessive memory
- Protection against distributed attacks

🛡️ **API Key Management**
- Secure storage of API keys
- Support for API key rotation
- Detect and block stolen/compromised keys

---

## 3. Traffic Estimates

### 3.1 User Base Assumptions

```
Total Users: 10 million
Daily Active Users (DAU): 1 million (10% of total)
Peak Active Users: 100,000 concurrent users
Geographic Distribution: Global (multi-region)
```

### 3.2 Request Volume Calculations

**Average Traffic:**
```
Daily Active Users (DAU): 1,000,000
Average requests per user per day: 100

Total requests per day: 1,000,000 × 100 = 100,000,000 (100 million)
Total requests per second (average): 100,000,000 / 86,400 = 1,157 req/sec
```

**Peak Traffic (3x average):**
```
Peak requests per second: 1,157 × 3 = 3,471 req/sec ≈ 3,500 req/sec
Peak requests per minute: 3,500 × 60 = 210,000 req/min
```

**Annual Traffic:**
```
Requests per year: 100,000,000 × 365 = 36.5 billion requests
```

### 3.3 Rate Limit Check Volume

**Every incoming request requires a rate limit check:**
```
Rate limit checks per second (average): 1,157 checks/sec
Rate limit checks per second (peak): 3,500 checks/sec
Rate limit checks per day: 100 million checks
```

**Read:Write Ratio for Rate Limiter:**
```
Every rate limit check involves:
- 1 READ operation (get current counter)
- 1 WRITE operation (increment counter)

Read:Write ratio = 1:1 (equal reads and writes)
```

### 3.4 Bandwidth Estimates

**Request Payload:**
```
Average API request size: 1 KB
Average API response size: 2 KB
Total per request: 3 KB

Bandwidth (average): 1,157 req/sec × 3 KB = 3.47 MB/sec ≈ 28 Mbps
Bandwidth (peak): 3,500 req/sec × 3 KB = 10.5 MB/sec ≈ 84 Mbps
```

**Rate Limiter Overhead:**
```
Rate limit counter read: ~50 bytes (Redis GET)
Rate limit counter write: ~50 bytes (Redis INCR)
Response headers: ~200 bytes

Total rate limiter overhead per request: ~300 bytes
Overhead bandwidth (peak): 3,500 × 300 bytes = 1.05 MB/sec ≈ 8 Mbps
```

---

## 4. Storage Estimates

### 4.1 Rate Limit Counter Storage (Redis)

**Data per user:**
```
User identifier (API key): 32 bytes (UUID)
Counter value: 8 bytes (64-bit integer)
Timestamp: 8 bytes
TTL metadata: 8 bytes

Total per counter: 56 bytes
```

**Total storage for counters:**
```
Active users at any moment: 100,000 (peak concurrent)
Multiple time windows per user: 3 (per-second, per-minute, per-hour)

Total counters: 100,000 × 3 = 300,000 counters
Total storage: 300,000 × 56 bytes = 16.8 MB

With 10x safety margin: 168 MB
With overhead (Redis metadata): ~250 MB
```

**For advanced algorithms (Sliding Window Log):**
```
Each request timestamp: 16 bytes (timestamp + metadata)
Max 100 timestamps per user per minute

Total per user: 100 × 16 bytes = 1.6 KB
For 100,000 active users: 100,000 × 1.6 KB = 160 MB

Total Redis memory needed: ~500 MB - 1 GB
```

### 4.2 Rate Limit Configuration Storage (Database)

**Configuration data:**
```sql
-- Rate limit rules
Table: rate_limit_rules
- rule_id: 8 bytes
- user_tier: 50 bytes (VARCHAR)
- endpoint_pattern: 200 bytes (VARCHAR)
- limit: 8 bytes
- window_size: 8 bytes
- created_at: 8 bytes
- updated_at: 8 bytes

Size per rule: ~300 bytes
Total rules: ~10,000 (different endpoints, tiers, regions)

Total configuration storage: 10,000 × 300 bytes = 3 MB
```

### 4.3 Logs and Analytics (Optional)

**Rate limit violations log:**
```
Each violation event: ~500 bytes (user_id, timestamp, endpoint, IP, reason)
Violations per day (assume 1% of requests blocked): 1,000,000 events
Storage per day: 1,000,000 × 500 bytes = 500 MB/day

Storage for 30 days retention: 500 MB × 30 = 15 GB
Storage for 1 year retention: 500 MB × 365 = 182.5 GB
```

---

## 5. Memory Estimates (Redis)

### 5.1 Redis Memory Breakdown

```
Counter data: 500 MB (calculated above)
Redis overhead (30%): 150 MB
Connection overhead: 50 MB
Safety buffer (2x): 1.4 GB

Total Redis memory per instance: ~2 GB
```

### 5.2 Redis Cluster Sizing

```
Total memory needed: 2 GB
Replication factor: 3 (1 master + 2 replicas for HA)

Total cluster memory: 2 GB × 3 = 6 GB

Recommended Redis instances:
- 3 nodes in cluster mode
- 2 GB RAM per node
- Total: 6 GB across cluster
```

---

## 6. System Scale Summary

| Metric | Value |
|--------|-------|
| **Users** | 10M total, 1M DAU |
| **Requests/sec (avg)** | 1,157 req/sec |
| **Requests/sec (peak)** | 3,500 req/sec |
| **Requests/day** | 100 million |
| **Requests/year** | 36.5 billion |
| **Rate limit checks/sec** | 3,500 checks/sec (peak) |
| **Redis memory** | 2 GB per instance |
| **Redis cluster size** | 3 nodes (6 GB total) |
| **Config DB size** | ~10 MB |
| **Logs storage (30d)** | 15 GB |
| **Bandwidth (peak)** | 84 Mbps |
| **Latency requirement** | < 5ms (P50), < 10ms (P99) |
| **Availability** | 99.99% |

---

## 7. Key Assumptions

📌 **Traffic Patterns:**
- Peak traffic is 3x average (common for API services)
- Traffic is evenly distributed across the day (no specific peak hours assumed)
- 1% of requests are blocked by rate limiter

📌 **User Behavior:**
- Average user makes 100 API calls per day
- 10% of registered users are active daily
- Users are distributed globally

📌 **Time Windows:**
- Primary window: 1 minute
- Secondary windows: 1 second, 1 hour
- Most common limits: 100 req/min, 10 req/sec, 1000 req/hour

📌 **System Constraints:**
- Budget: Not specified (assume cloud-based infrastructure)
- Geographic regions: 3 regions (US, EU, APAC)
- Compliance: GDPR compliant (data residency)

📌 **Technology Choices:**
- Redis for distributed counters (in-memory, fast, supports atomic operations)
- PostgreSQL/MySQL for configuration storage (ACID compliance)
- Containerized deployment (Kubernetes)

---

## 8. Success Metrics

### 8.1 Performance Metrics

✅ **Latency:**
- P50 latency < 2ms
- P99 latency < 10ms
- P999 latency < 20ms

✅ **Throughput:**
- Support 100,000 req/sec per node
- Linear horizontal scaling

✅ **Accuracy:**
- 99.99% accuracy in rate limit enforcement
- < 0.01% false positives (legitimate requests blocked)
- < 0.01% false negatives (violating requests allowed)

### 8.2 Reliability Metrics

✅ **Availability:**
- 99.99% uptime (52 minutes downtime/year)
- Zero data loss for configuration
- Graceful degradation on failures

✅ **Consistency:**
- Strong consistency across all nodes
- No race conditions in concurrent scenarios

---

## 9. Out of Scope (V1)

🚫 **Not included in initial version:**
- Dynamic rate limit adjustment based on load
- Machine learning-based anomaly detection
- Per-IP rate limiting (focus on API key only)
- Geographic-based routing and limits
- Rate limit analytics dashboard (UI)
- Whitelist/blacklist management UI

**Note:** These features can be added in future iterations based on priority.

---

## Next Steps

With requirements and estimations complete, we can now move to:
1. ✅ **Step 1: Basic Architecture Design** → Design the high-level components
2. ✅ **Step 2: Add Caching Layer** → Integrate Redis for distributed counters
3. ✅ **Step 3: Rate Limiting Algorithms** → Choose and implement the algorithm
4. ✅ **Step 4: API Design** → Define API contracts and data flows
5. ✅ **Step 5: Scalability & Reliability** → Add replication, sharding, monitoring

---

## Beginner-Friendly Analogy 🎯

**Think of a rate limiter like a nightclub bouncer:**

- **User = Club Visitor:** Each visitor (user) wants to enter the club (make API requests)
- **Rate Limit = Club Capacity:** The club has a maximum capacity (e.g., 100 people per hour)
- **Bouncer = Rate Limiter:** The bouncer checks how many times you've entered in the past hour
- **Counter = Guest List:** The bouncer maintains a list with timestamps of each visitor's entries
- **HTTP 429 = "Sorry, club is full":** If you've exceeded the limit, you're told to come back later
- **Retry-After = "Come back in 30 minutes":** The bouncer tells you exactly when you can try again

Just like a bouncer needs to coordinate with other bouncers (distributed system) to track visitors across multiple entrances, our rate limiter must work across multiple servers to enforce limits consistently!

---

**Document Version:** 1.0
**Last Updated:** 2025-11-17
**Next Document:** [Step 1: Basic Architecture](./2_step1_basic_architecture.md)
