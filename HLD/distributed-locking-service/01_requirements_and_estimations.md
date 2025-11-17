# Requirements and Estimations - Distributed Locking Service

## Introduction

A **Distributed Locking Service** is like a traffic light system for computers working together. Just as a traffic light ensures only one direction of traffic moves at a time, a distributed lock ensures only one program can access a shared resource at any moment, even when these programs run on different computers across the world.

**Real-world analogy:** Imagine 100 delivery drivers trying to update the same customer's order status simultaneously. Without a locking mechanism, they might overwrite each other's changes, causing data corruption. A distributed lock acts like a "talking stick" - only the driver holding the stick can update the order.

---

## 1. Functional Requirements

These are the core features our distributed locking service **MUST** provide:

### 1.1 Acquire Lock (High Priority - P0)
- **What:** A client can request exclusive access to a specific resource
- **How:** Client calls `acquireLock(resourceId, timeout)`
- **Behavior:**
  - If lock is available → Grant lock immediately
  - If lock is held by another client → Either block and wait OR return failure immediately (based on configuration)
- **Example:** Service A wants to process Order #12345. It calls `acquireLock("order:12345", 30s)`. If successful, no other service can lock this order for 30 seconds.

### 1.2 Release Lock (High Priority - P0)
- **What:** A client explicitly releases a lock it currently holds
- **How:** Client calls `releaseLock(resourceId, lockToken)`
- **Behavior:**
  - Validates the client owns the lock (using lockToken)
  - Releases the lock
  - Makes resource available for other clients
- **Example:** After updating Order #12345, Service A calls `releaseLock("order:12345", token)` to allow others to access it.

### 1.3 Lock with TTL - Time To Live (High Priority - P0)
- **What:** Every lock has an automatic expiration time (lease)
- **Why:** Prevents deadlocks if a client crashes while holding a lock
- **How:** Lock is automatically released after TTL expires
- **Default TTL:** 30 seconds (configurable: 5s to 300s)
- **Example:** Service A crashes after acquiring a lock. Without TTL, the lock would be held forever (deadlock). With 30s TTL, the lock auto-releases after 30 seconds.

**Beginner Note:** Think of TTL like a parking meter. You pay for 30 minutes (acquire lock with 30s TTL). After 30 minutes, your spot becomes available again automatically, even if you forget to inform the parking authority (crash scenario).

### 1.4 Lock Renewal / Extension (Medium Priority - P1)
- **What:** A client can extend the lease time while still processing
- **How:** Client calls `renewLock(resourceId, lockToken, additionalTime)`
- **Why:** For operations that take longer than initial TTL
- **Example:** Service A is processing a large batch job on Order #12345. It acquired a 30s lock but needs 60s total. After 20 seconds, it calls `renewLock("order:12345", token, 40s)` to extend.

### 1.5 Check Lock Status (Low Priority - P2)
- **What:** Query if a resource is currently locked
- **How:** Client calls `checkLock(resourceId)`
- **Returns:** Lock status (available/locked), owner, expiry time
- **Use case:** For monitoring and debugging

### 1.6 Blocking vs Non-Blocking Acquisition (Medium Priority - P1)
- **Blocking Mode:** Client waits in a queue until lock becomes available (with timeout)
- **Non-Blocking Mode:** Client gets immediate response (success/failure)
- **Example:**
  - Non-blocking: "Is this resource available? If not, return immediately" (for background jobs)
  - Blocking: "Wait up to 10 seconds for this lock" (for critical path operations)

---

## 2. Non-Functional Requirements

These define **HOW WELL** the system should perform:

### 2.1 Scalability
- **Target Scale:**
  - Support **100 million locks** active simultaneously
  - Handle **1 million clients** concurrently
  - Process **50,000 lock requests per second** (average)
  - Peak traffic: **150,000 requests/sec** (3x average during traffic spikes)
- **Growth Rate:** 2x year-over-year growth in lock requests
- **Horizontal Scaling:** System must scale by adding more servers (not just bigger servers)

**Beginner Calculation Example:**
```
If each lock request takes 10ms to process:
- Single server capacity = 1000ms / 10ms = 100 requests/sec
- To handle 50,000 req/sec, we need: 50,000 / 100 = 500 servers minimum
- With redundancy (3 replicas), we need: 500 × 3 = 1,500 servers
```

### 2.2 Availability (Uptime)
- **Target SLA:** 99.99% availability (Four nines)
- **Allowed Downtime:**
  - Per year: 52.56 minutes
  - Per month: 4.38 minutes
  - Per day: 8.64 seconds
- **No Single Point of Failure (SPOF):** Every component must have redundancy
- **Failover Time:** < 10 seconds to detect failure and switch to backup

**Availability Comparison Table:**

| SLA Level | Downtime/Year | Downtime/Month | Downtime/Day | Use Case |
|-----------|---------------|----------------|--------------|----------|
| 99% (Two nines) | 3.65 days | 7.31 hours | 14.40 minutes | Dev/Test environments |
| 99.9% (Three nines) | 8.77 hours | 43.83 minutes | 1.44 minutes | Internal tools |
| **99.99% (Four nines)** | **52.56 minutes** | **4.38 minutes** | **8.64 seconds** | **Production systems** ✅ |
| 99.999% (Five nines) | 5.26 minutes | 26.30 seconds | 0.86 seconds | Banking, healthcare |

### 2.3 Performance (Latency & Throughput)
- **Latency Requirements:**
  - **P50 (50th percentile):** < 10ms - Half of all requests complete within 10ms
  - **P95 (95th percentile):** < 50ms - 95% of requests complete within 50ms
  - **P99 (99th percentile):** < 100ms - 99% of requests complete within 100ms
- **Lock Hold Duration:** Average 5 seconds, max 300 seconds (5 minutes)
- **Throughput:** 50,000 operations/sec sustained, 150,000 ops/sec peak

**Beginner Note on Percentiles:**
Imagine 100 lock requests:
- P50 (median): The 50th fastest request took 10ms
- P95: Only 5 out of 100 requests took longer than 50ms
- P99: Only 1 out of 100 requests took longer than 100ms

We focus on P95/P99 because average can hide outliers. If 99 requests take 10ms but 1 takes 10 seconds, average is misleading!

### 2.4 Correctness (Safety Guarantees)
- **Mutual Exclusion (Most Critical):** At most ONE client holds a lock on a resource at any time
- **No Lock Stealing:** A client cannot release or modify another client's lock
- **No Race Conditions:** Under no circumstances should two clients believe they own the same lock
- **Deadlock Freedom:** System should NEVER enter a permanent deadlock state (TTL ensures this)

**Beginner Example - Why Correctness Matters:**
```
❌ WRONG (Race Condition):
Time 0s: Client A checks lock → Available
Time 0s: Client B checks lock → Available (same time!)
Time 1s: Client A acquires lock
Time 1s: Client B acquires lock (both think they own it!)
Result: Data corruption when both modify the resource

✅ CORRECT (Atomic Check-and-Set):
Time 0s: Client A sends acquire request
Time 0s: Client B sends acquire request
Time 1s: Lock server uses atomic operation (compare-and-swap)
        → Only ONE client gets the lock
        → Other client gets "lock unavailable" response
```

### 2.5 Consistency Model
- **Strong Consistency Preferred:** When a client acquires a lock, all other clients immediately see it as locked
- **Acceptable Consistency:** Eventual consistency with small window (< 100ms)
- **Trade-off:** We prioritize **Consistency and Partition Tolerance** over **Availability** (CP in CAP theorem)

**CAP Theorem for Beginners:**
In distributed systems, you can only guarantee 2 out of 3:
- **C**onsistency: All nodes see the same data
- **A**vailability: System always responds
- **P**artition Tolerance: System works even if network splits

For a locking service, we choose **CP** (Consistency + Partition Tolerance):
- We NEED consistency (two clients can't both own lock)
- We NEED partition tolerance (network issues happen)
- We accept slight availability reduction (lock service might reject requests during network split to maintain safety)

### 2.6 Reliability & Durability
- **Data Durability:** Lock state persisted to disk (survives server restart)
- **Replication Factor:** 3 replicas minimum (can lose 2 servers and still operate)
- **Backup & Recovery:**
  - Continuous backup of lock state
  - Recovery Time Objective (RTO): < 1 minute
  - Recovery Point Objective (RPO): < 5 seconds (max data loss)

### 2.7 Security
- **Authentication:** Every client must authenticate (API keys, OAuth tokens)
- **Authorization:** Role-based access control (RBAC)
  - Some clients can only acquire locks
  - Admin clients can force-release locks
- **Encryption:**
  - In transit: TLS 1.3
  - At rest: AES-256 encryption for lock metadata
- **Audit Logging:** Every lock operation logged with timestamp, client ID, resource ID

### 2.8 Cost Efficiency
- **Target:** $10,000/month for 50,000 req/sec
- **Cloud Costs:**
  - Compute: $6,000/month (distributed servers)
  - Storage: $500/month (lock metadata + logs)
  - Network: $1,500/month (cross-region bandwidth)
  - Monitoring: $500/month (metrics + logs)
  - Overhead: $1,500/month (load balancers, backups)

---

## 3. Traffic Estimates

### 3.1 User & Client Assumptions
- **Total Clients:** 1 million clients (applications/services)
- **Active Clients at any time:** 200,000 (20% of total)
- **Locks per Client per Day:** 100 lock operations (50 acquire + 50 release)

### 3.2 Request Calculations

**Daily Requests:**
```
Total clients = 1,000,000
Active clients = 200,000 (20%)
Locks per client per day = 100 operations

Daily requests = 200,000 × 100 = 20,000,000 (20 million)
```

**Requests Per Second (Average):**
```
Seconds in a day = 86,400

Average RPS = 20,000,000 / 86,400 = 231 requests/sec
```

Wait, this seems too low for our target of 50,000 req/sec. Let me recalculate with more realistic numbers:

**Revised Assumptions (for high-traffic scenario):**
```
Total clients = 10,000,000 (10 million services/apps)
Active clients at any time = 2,000,000 (20%)
Locks per client per minute = 2 operations

Requests per minute = 2,000,000 × 2 = 4,000,000
Requests per second = 4,000,000 / 60 = 66,667 req/sec ✅

This aligns with our 50,000 average + peaks
```

**Peak Traffic (3x multiplier):**
```
Peak RPS = 66,667 × 3 = 200,000 requests/sec

Why 3x? Traffic spikes during:
- Business hours (9 AM - 5 PM)
- End of month (financial systems)
- Black Friday / Cyber Monday (e-commerce)
- Deployment windows (many services restart simultaneously)
```

### 3.3 Read vs Write Ratio

For a locking service:
```
Operations breakdown:
- Acquire lock: 45% (write operation)
- Release lock: 45% (write operation)
- Renew lock: 5% (write operation)
- Check lock status: 5% (read operation)

Read:Write = 5:95 (write-heavy system!)
```

**Why Write-Heavy Matters:**
- Writes are slower than reads (need to update multiple replicas)
- Writes require consensus (Raft/Paxos adds latency)
- We need write-optimized storage (Redis, etcd)

### 3.4 Bandwidth Calculations

**Per Request Size:**
```
Typical lock request:
{
  "operation": "acquire",      // 10 bytes
  "resourceId": "order:12345", // 20 bytes
  "ttl": 30,                   // 8 bytes
  "clientId": "service-A-uuid",// 36 bytes
  "timestamp": 1738000000      // 8 bytes
}
Total: ~100 bytes per request (including HTTP headers)

Response: ~50 bytes
```

**Total Bandwidth:**
```
Average traffic:
- Incoming: 66,667 req/sec × 100 bytes = 6.67 MB/sec = 53 Mbps
- Outgoing: 66,667 req/sec × 50 bytes = 3.33 MB/sec = 27 Mbps
- Total: 80 Mbps average

Peak traffic (3x):
- Total: 240 Mbps peak

Daily bandwidth:
- Total requests: 66,667 × 86,400 = 5.76 billion requests/day
- Data transferred: 5.76B × 150 bytes = 864 GB/day
```

---

## 4. Storage Estimates

### 4.1 Active Lock Storage

**Per Lock Metadata:**
```json
{
  "lockId": "uuid-string",           // 36 bytes
  "resourceId": "order:12345",       // 50 bytes average
  "ownerId": "service-A-uuid",       // 36 bytes
  "acquiredAt": 1738000000,          // 8 bytes (timestamp)
  "expiresAt": 1738000030,           // 8 bytes
  "ttl": 30,                         // 4 bytes
  "lockToken": "secure-token-hash",  // 64 bytes
  "clientIp": "192.168.1.100",       // 15 bytes
  "renewCount": 2                    // 4 bytes
}
Total per lock: ~250 bytes
```

**Total Active Locks Storage:**
```
Assumptions:
- Average concurrent locks: 10 million
- Average lock duration: 30 seconds
- Lock turnover rate: High (locks released and reacquired frequently)

Storage for active locks:
10,000,000 locks × 250 bytes = 2.5 GB

With indexing overhead (2x):
2.5 GB × 2 = 5 GB for active lock state
```

### 4.2 Historical Lock Data (Audit Logs)

**Per Lock Event:**
```
Each acquire/release/renew/expire event: ~300 bytes
```

**Daily Log Volume:**
```
Total operations per day: 5.76 billion
Log size per operation: 300 bytes

Daily logs: 5.76B × 300 bytes = 1.73 TB/day
Monthly logs: 1.73 TB × 30 = 52 TB/month
Yearly logs: 1.73 TB × 365 = 631 TB/year
```

**Storage Strategy:**
```
Hot storage (last 7 days): 1.73 TB × 7 = 12 TB (SSD)
Warm storage (last 90 days): 1.73 TB × 90 = 156 TB (HDD)
Cold storage (> 90 days): Archive to S3/Glacier (~5 TB/month growth)
```

### 4.3 Database Size Estimates

**5-Year Storage Projection:**
```
Year 1: 631 TB
Year 2: 631 TB × 1.5 (50% growth) = 947 TB
Year 3: 947 TB × 1.5 = 1,420 TB
Year 4: 1,420 TB × 1.5 = 2,130 TB
Year 5: 2,130 TB × 1.5 = 3,195 TB

Total 5-year storage (active + logs): ~8,323 TB ≈ 8.3 PB
```

**Storage Cost Estimate:**
```
Hot storage (SSD): 12 TB × $0.10/GB/month = $1,200/month
Warm storage (HDD): 156 TB × $0.02/GB/month = $3,120/month
Cold archive (S3 Glacier): 631 TB/year × $0.004/GB/month = $2,524/month
Total: ~$6,844/month for storage
```

### 4.4 Cache Storage (Redis/In-Memory)

**What to Cache:**
- Active lock state (hot locks accessed frequently)
- Client authentication tokens
- Recently released locks (to prevent immediate reacquisition races)

**Cache Size:**
```
Hot locks (top 10% accessed locks): 1 million locks
Cache per lock: 250 bytes
Total: 1M × 250 bytes = 250 MB

Client session cache: 500 MB
Lock queue data: 250 MB

Total cache needed: 1 GB per server
With 20 cache servers: 20 GB total cache
```

---

## 5. Assumptions & Constraints

### 5.1 System Assumptions

1. **Network is Unreliable**
   - Network partitions can occur (split-brain scenarios)
   - Message delivery is not guaranteed (need retries)
   - Clock synchronization is approximate (clock skew < 100ms)

2. **Clients Can Fail**
   - Clients can crash while holding locks (why we need TTL)
   - Clients may not release locks explicitly (cleanup required)
   - Clients may be malicious (why we need authentication)

3. **Clock Synchronization**
   - All servers use NTP (Network Time Protocol)
   - Maximum clock skew: 100ms between servers
   - Clock drift: < 50 ppm (parts per million)

4. **Geographic Distribution**
   - Multi-region deployment (3 regions minimum)
   - Primary region: US-East (40% traffic)
   - Secondary regions: US-West (30%), EU-West (30%)
   - Cross-region latency: 50-100ms

### 5.2 Business Constraints

1. **Lock Hold Time Limits**
   - Minimum TTL: 5 seconds
   - Maximum TTL: 300 seconds (5 minutes)
   - Default TTL: 30 seconds

2. **Rate Limiting**
   - Per client: 100 requests/sec
   - Per resource: 1,000 requests/sec (prevents hotspot abuse)
   - Burst allowance: 2x rate for 10 seconds

3. **Lock Namespace**
   - Total unique resources: 1 billion
   - Resource ID format: `{type}:{id}` (e.g., "order:12345")
   - Max resource ID length: 255 characters

### 5.3 Technical Constraints

1. **Infrastructure**
   - Cloud provider: AWS (could be GCP/Azure)
   - Kubernetes for orchestration
   - Terraform for infrastructure as code

2. **Technology Stack (Preliminary)**
   - Lock coordination: etcd or ZooKeeper
   - Caching: Redis cluster
   - Load balancing: HAProxy or AWS ALB
   - Monitoring: Prometheus + Grafana

3. **Compliance**
   - GDPR compliant (EU data stays in EU)
   - SOC 2 Type II certified
   - Data retention: 90 days minimum, 2 years maximum

---

## 6. Success Metrics (KPIs)

How do we measure if our distributed locking service is successful?

### 6.1 Performance Metrics
- **Lock Acquisition Latency:** < 10ms (P50), < 50ms (P95)
- **Lock Throughput:** 50,000 ops/sec sustained
- **Lock Success Rate:** > 99.9% (failures only due to lock unavailability, not system errors)

### 6.2 Reliability Metrics
- **Uptime:** 99.99% (52 minutes downtime/year)
- **Error Rate:** < 0.01% (1 error per 10,000 requests)
- **Data Loss:** 0% (no lock state lost)

### 6.3 Safety Metrics
- **Double Lock Events:** 0 (must never happen!)
- **Lock Leak Rate:** < 0.001% (locks not released properly)
- **Security Incidents:** 0 unauthorized lock access

---

## 7. Summary Table

| Category | Metric | Value |
|----------|--------|-------|
| **Scale** | Concurrent locks | 10 million |
| | Total clients | 10 million |
| | Requests/sec (avg) | 66,667 |
| | Requests/sec (peak) | 200,000 |
| **Latency** | P50 | < 10ms |
| | P95 | < 50ms |
| | P99 | < 100ms |
| **Storage** | Active locks | 5 GB |
| | Daily logs | 1.73 TB |
| | 5-year projection | 8.3 PB |
| **Availability** | SLA | 99.99% |
| | Max downtime/year | 52.56 minutes |
| **Cost** | Monthly infrastructure | $10,000 |
| | Monthly storage | $6,844 |

---

## Next Steps

Now that we have clear requirements and estimates, we can move to:
1. **Step 1:** Design basic architecture (Client → Load Balancer → Lock Service → Storage)
2. **Step 2:** Add caching layer for performance
3. **Step 3:** Add database replication for high availability
4. **Step 4:** Design consensus mechanism (Raft/Paxos)
5. **Step 5:** Add monitoring and observability
6. **Step 6:** Final architecture with all components

Let's start designing! 🚀
