# Capacity Planning & Calculations - Distributed Locking Service

## Why Capacity Planning Matters

**Beginner Analogy:** Planning capacity is like planning a wedding. You need to know:
- How many guests? (Users)
- How much food per person? (Requests per user)
- How big should the venue be? (Server capacity)
- What if more people show up? (Peak traffic)
- How much will it cost? (Infrastructure budget)

Without planning, you might run out of food (servers crash) or waste money on a venue that's too large (over-provisioning).

---

## 1. Traffic Capacity Planning

### Step 1: Start with Business Requirements

```
Given business requirements:
- 10 million client applications
- Each client acquires/releases locks frequently
- Target: Support 50,000 operations/sec average
```

### Step 2: Calculate Daily Active Clients

```
Total registered clients: 10,000,000
Activity rate: 20% (not all clients are active simultaneously)

Daily Active Clients (DAU) = 10,000,000 × 0.20 = 2,000,000 clients
```

**Beginner Question:** Why only 20% active?
**Answer:** Like your phone apps - you have 100 apps installed, but only use 20 regularly. Similarly, not all registered services use locks constantly.

### Step 3: Calculate Operations Per Second

**Approach A: Bottom-Up (from user behavior)**
```
Assumptions:
- Each active client performs 2 lock operations per minute
  (1 acquire + 1 release every 30 seconds)

Operations per minute = 2,000,000 clients × 2 ops = 4,000,000 ops/min
Operations per second = 4,000,000 / 60 = 66,667 ops/sec ✅

This matches our target of ~50,000 ops/sec!
```

**Approach B: Top-Down (from target throughput)**
```
Target: 50,000 ops/sec
Seconds per day: 86,400

Daily operations = 50,000 × 86,400 = 4,320,000,000 (4.32 billion ops/day)

Reverse calculation:
Operations per client per day = 4,320,000,000 / 2,000,000 = 2,160 ops/day
Operations per client per minute = 2,160 / 1,440 = 1.5 ops/min ✅

Both approaches align!
```

### Step 4: Account for Peak Traffic

```
Average traffic: 66,667 ops/sec

Peak traffic scenarios:
- Morning rush (9 AM): 2x average = 133,334 ops/sec
- Deployment windows: 3x average = 200,000 ops/sec
- Black Friday events: 5x average = 333,333 ops/sec

Design target: Handle 3x average = 200,000 ops/sec
```

**Beginner Visualization:**
```
Traffic Pattern (24 hours):

200k │                    ╱╲              Peak: 200k ops/sec
     │                   ╱  ╲             (deployment rush)
150k │                  ╱    ╲
     │                 ╱      ╲
100k │          ╱╲    ╱        ╲    ╱╲
     │         ╱  ╲  ╱          ╲  ╱  ╲
 50k │════════╱════╲╱════════════╲╱════╲═ Average: 67k ops/sec
     │       ╱                    ╲      ╲
   0 └──────────────────────────────────────
     0 AM    6 AM   12 PM   6 PM   12 AM

Notice: Traffic has patterns!
- Low: 2 AM - 6 AM (nighttime)
- High: 9 AM - 5 PM (business hours)
- Spikes: Deployment windows, month-end batches
```

### Step 5: Calculate Required Server Capacity

```
Assumptions:
- Single server capacity: 1,000 ops/sec (10ms per operation)
- Need to handle peak: 200,000 ops/sec

Servers needed = 200,000 / 1,000 = 200 servers

Add redundancy (3x for high availability):
Total servers = 200 × 3 = 600 servers

Add overhead for maintenance (20% extra):
Final servers = 600 × 1.2 = 720 servers
```

**Cost Breakdown:**
```
Cloud VM cost: $0.10/hour per server
Cost per server per month: $0.10 × 24 × 30 = $72/month
Total compute cost: 720 × $72 = $51,840/month

Wait, that's over budget ($10,000/month)!
```

**Optimization Strategy:**
```
Option 1: Use auto-scaling
- Min servers (off-peak): 200 servers → $14,400/month
- Scale up during peak hours only
- Average monthly cost: ~$20,000/month (still high)

Option 2: Use smaller VMs + optimize code
- Optimize lock operations: 5ms instead of 10ms
- Server capacity: 2,000 ops/sec (doubled!)
- Servers needed: 200,000 / 2,000 = 100 servers
- With redundancy: 100 × 3 = 300 servers
- Cost: 300 × $72 = $21,600/month (better)

Option 3: Use managed services
- Redis cluster: $5,000/month (fully managed)
- etcd cluster: $3,000/month
- Load balancers: $2,000/month
- Total: ~$10,000/month ✅ (meets budget!)
```

---

## 2. Storage Capacity Planning

### Step 1: Calculate Active Lock Storage

```
Concurrent active locks at any time:

Method 1: From lock duration
Average lock duration: 30 seconds
Lock acquisitions per second: 66,667
Concurrent locks = 66,667 × 30 = 2,000,000 locks

Method 2: From business metrics
2 million active clients, each holding 5 locks on average
Concurrent locks = 2,000,000 × 5 = 10,000,000 locks

We'll design for the higher estimate: 10 million concurrent locks
```

**Storage per lock:**
```json
{
  "lockId": "550e8400-e29b-41d4-a716-446655440000",  // 36 bytes
  "resourceId": "order:123456789",                   // 30 bytes avg
  "ownerId": "service-a-uuid",                       // 36 bytes
  "acquiredAt": 1738012345.123,                      // 8 bytes
  "expiresAt": 1738012375.123,                       // 8 bytes
  "ttl": 30,                                         // 4 bytes
  "lockToken": "sha256-hash-string",                 // 64 bytes
  "clientIp": "192.168.1.100",                       // 15 bytes
  "metadata": {"region": "us-east-1"}                // 50 bytes
}
Total: ~250 bytes per lock
```

**Total active storage:**
```
10,000,000 locks × 250 bytes = 2,500,000,000 bytes = 2.5 GB

Add indexing overhead:
- Index on resourceId: 1 GB
- Index on ownerId: 1 GB
- Index on expiresAt: 0.5 GB

Total with indexes: 2.5 + 1 + 1 + 0.5 = 5 GB

This easily fits in memory! (Redis/etcd can handle this)
```

### Step 2: Calculate Historical Audit Log Storage

```
Operations per second: 66,667
Operations per day: 66,667 × 86,400 = 5,760,000,000 (5.76 billion)

Log entry size: 300 bytes (includes timestamps, client info, etc.)

Daily log volume:
5.76 billion × 300 bytes = 1,728,000,000,000 bytes = 1.73 TB/day
```

**Storage growth projection:**
```
Day 1:    1.73 TB
Week 1:   1.73 × 7 = 12.1 TB
Month 1:  1.73 × 30 = 51.9 TB
Year 1:   1.73 × 365 = 631 TB
Year 5:   631 × 5 × 1.5 (growth) = 4,733 TB ≈ 4.7 PB
```

**Storage tiering strategy:**
```
Tier 1 (Hot - SSD): Last 7 days
- Size: 12 TB
- Cost: 12,000 GB × $0.10/GB/month = $1,200/month
- Use: Active queries, debugging

Tier 2 (Warm - HDD): Last 90 days
- Size: 52 TB × 3 = 156 TB
- Cost: 156,000 GB × $0.02/GB/month = $3,120/month
- Use: Compliance, audit reports

Tier 3 (Cold - S3 Glacier): > 90 days
- Size: Growing at 52 TB/month
- Cost: ~$0.004/GB/month
- First year archive: 12 months × 52 TB = 624 TB
- Cost: 624,000 GB × $0.004 = $2,496/month

Total storage cost: $1,200 + $3,120 + $2,496 = $6,816/month
```

### Step 3: Cache Storage (In-Memory)

```
What needs caching?
1. Frequently accessed locks (hot locks)
2. Client authentication tokens
3. Lock acquisition queue data

Hot locks (Pareto principle - 80/20 rule):
- 20% of locks are accessed 80% of the time
- Hot locks: 10,000,000 × 0.20 = 2,000,000 locks
- Cache size: 2,000,000 × 250 bytes = 500 MB

Client auth cache:
- Active clients: 2,000,000
- Token size: 256 bytes
- Cache: 2,000,000 × 256 = 512 MB

Lock queue data: 200 MB
Overhead: 300 MB

Total cache per region: 500 + 512 + 200 + 300 = 1.5 GB
Replicate across 3 regions: 1.5 × 3 = 4.5 GB total
```

**Redis cluster sizing:**
```
Memory needed: 5 GB (active locks) + 1.5 GB (cache) = 6.5 GB
With replication (3x): 6.5 × 3 = 19.5 GB
Add safety buffer (2x): 19.5 × 2 = 39 GB

Redis cluster configuration:
- 6 nodes (3 masters + 3 replicas)
- 8 GB RAM per node
- Total: 48 GB (sufficient!)
- Cost: ~$1,000/month (managed Redis)
```

---

## 3. Bandwidth Capacity Planning

### Step 1: Calculate Request/Response Sizes

**Acquire Lock Request:**
```http
POST /api/v1/locks/acquire HTTP/1.1
Host: lock-service.example.com
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "resourceId": "order:123456789",
  "ttl": 30,
  "clientId": "service-a-550e8400",
  "blocking": false,
  "timeout": 10
}

Size breakdown:
- HTTP headers: 300 bytes
- JSON body: 150 bytes
Total: ~450 bytes per request
```

**Acquire Lock Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "lockToken": "sha256-hash-token-string",
  "expiresAt": 1738012375123,
  "lockId": "550e8400-e29b-41d4-a716-446655440000"
}

Size: ~200 bytes
```

**Average per operation:** 450 (request) + 200 (response) = 650 bytes

### Step 2: Calculate Total Bandwidth

```
Operations per second: 66,667
Bytes per operation: 650

Bandwidth per second:
66,667 × 650 = 43,333,550 bytes/sec = 43.3 MB/sec = 347 Mbps

Peak traffic (3x):
347 Mbps × 3 = 1,041 Mbps ≈ 1 Gbps
```

**Daily data transfer:**
```
Operations per day: 5.76 billion
Data per day: 5.76B × 650 bytes = 3.744 TB/day
Data per month: 3.744 TB × 30 = 112 TB/month
```

**Network cost calculation:**
```
AWS data transfer pricing:
- Inbound: Free
- Outbound (first 10 TB): $0.09/GB
- Outbound (next 40 TB): $0.085/GB
- Outbound (next 100 TB): $0.07/GB

Monthly outbound: 112 TB = 112,000 GB
Cost breakdown:
- First 10 TB: 10,000 × $0.09 = $900
- Next 40 TB: 40,000 × $0.085 = $3,400
- Next 62 TB: 62,000 × $0.07 = $4,340
Total: $8,640/month (over budget!)
```

**Optimization:**
```
Option 1: Compression
- Enable gzip compression (2x reduction)
- New bandwidth: 56 TB/month
- Cost: ~$4,500/month ✅

Option 2: Regional deployments
- Keep traffic within region (no cross-region charges)
- Cost: ~$1,500/month ✅ (best option)
```

---

## 4. Database Capacity Planning

### Step 1: Determine Database Technology

**Option A: SQL Database (PostgreSQL)**
```
Pros:
+ ACID guarantees
+ Strong consistency
+ Complex queries (for audit logs)

Cons:
- Vertical scaling limits
- Write throughput limited
- Not ideal for distributed consensus

Verdict: Good for audit logs, NOT for active locks
```

**Option B: NoSQL (Redis)**
```
Pros:
+ In-memory (ultra-fast)
+ Built-in TTL (perfect for locks!)
+ Atomic operations (compare-and-set)
+ High write throughput

Cons:
- No complex queries
- Need persistence configuration
- Limited disk storage

Verdict: Perfect for active locks! ✅
```

**Option C: Distributed KV Store (etcd)**
```
Pros:
+ Built for distributed coordination
+ Raft consensus (strong consistency)
+ Watch API (event notifications)
+ Designed for locking use cases

Cons:
- More complex to operate
- Lower throughput than Redis

Verdict: Best for production-grade locking! ✅
```

**Final decision:** Hybrid approach
- **etcd** for active lock state (correctness > speed)
- **Redis** for caching and fast reads
- **PostgreSQL** for audit logs

### Step 2: Size etcd Cluster

```
Active lock data: 5 GB
Replication factor: 3 (Raft requirement)
Total: 5 GB × 3 = 15 GB

etcd recommendations:
- Max 8 GB per node (for performance)
- Use 3 nodes with 16 GB RAM each
- Storage: SSD (NVMe preferred)

etcd cluster setup:
- Node 1: 16 GB RAM, 100 GB SSD (leader)
- Node 2: 16 GB RAM, 100 GB SSD (follower)
- Node 3: 16 GB RAM, 100 GB SSD (follower)

Cost: 3 × $200/month = $600/month
```

### Step 3: Size Redis Cache Cluster

```
Cache size: 4.5 GB (from earlier calculation)
Replication: 3x (1 master + 2 replicas per shard)

Redis cluster:
- 3 shards (for horizontal scaling)
- Each shard: 1 master + 2 replicas = 3 nodes
- Total nodes: 3 × 3 = 9 nodes
- RAM per node: 4 GB
- Total RAM: 36 GB

Cost: 9 × $50/month = $450/month (self-hosted)
Or: $1,500/month (AWS ElastiCache managed)
```

### Step 4: Size PostgreSQL for Audit Logs

```
Hot data (7 days): 12 TB
Need fast SSD storage

PostgreSQL setup:
- Primary: 16 TB SSD, 32 GB RAM
- Read replica 1: 16 TB SSD, 32 GB RAM
- Read replica 2: 16 TB SSD, 32 GB RAM

Storage cost: 3 × 16 TB × $0.10/GB = $4,800/month
Compute cost: 3 × $200/month = $600/month
Total: $5,400/month

Optimization: Use PostgreSQL partitioning
- Partition by day (7 partitions)
- Drop old partitions automatically
- Reduces query time and storage
```

---

## 5. Summary: Total Infrastructure Cost

### Monthly Cost Breakdown

```
┌─────────────────────────────────────────────────────┐
│ Component              │ Cost/Month │ Notes         │
├─────────────────────────────────────────────────────┤
│ Load Balancers (HAProxy)│   $500    │ 3 regions     │
│ Application Servers     │ $3,000    │ Auto-scaling  │
│ etcd Cluster            │   $600    │ Lock state    │
│ Redis Cache             │ $1,500    │ Managed       │
│ PostgreSQL (Logs)       │ $5,400    │ 3 replicas    │
│ Storage (S3 Archive)    │ $2,500    │ Cold storage  │
│ Network/Bandwidth       │ $1,500    │ Compressed    │
│ Monitoring (Prometheus) │   $500    │ Metrics/logs  │
│ Security (WAF/SSL)      │   $300    │ DDoS protect  │
│ Backup & DR             │   $200    │ Snapshots     │
├─────────────────────────────────────────────────────┤
│ TOTAL                   │ $16,000   │               │
└─────────────────────────────────────────────────────┘
```

**Over budget by $6,000!** Let's optimize:

### Optimized Cost (within $10,000 budget)

```
┌─────────────────────────────────────────────────────┐
│ Component              │ Cost/Month │ Optimization  │
├─────────────────────────────────────────────────────┤
│ Load Balancers (ALB)   │   $300    │ AWS managed   │
│ Application Servers    │ $2,000    │ Spot instances│
│ etcd Cluster           │   $600    │ Self-hosted   │
│ Redis Cache            │   $800    │ Self-hosted   │
│ PostgreSQL (RDS)       │ $3,000    │ 1 hot replica │
│ Storage (Lifecycle)    │ $1,200    │ Auto-archive  │
│ Network (Regional)     │   $800    │ Same-region   │
│ Monitoring (CloudWatch)│   $400    │ AWS native    │
│ Security               │   $200    │ AWS Shield    │
│ Backup                 │   $100    │ Automated     │
├─────────────────────────────────────────────────────┤
│ TOTAL                  │ $9,400    │ ✅ Under budget│
└─────────────────────────────────────────────────────┘
```

---

## 6. Capacity Planning Checklist

### ✅ Completed Calculations

- [x] Traffic estimates (avg: 67k ops/sec, peak: 200k ops/sec)
- [x] Storage estimates (active: 5 GB, logs: 1.73 TB/day)
- [x] Bandwidth estimates (347 Mbps avg, 1 Gbps peak)
- [x] Server capacity (300-600 servers depending on optimization)
- [x] Database sizing (etcd: 15 GB, Redis: 36 GB, PostgreSQL: 12 TB)
- [x] Cost estimates ($9,400/month within $10k budget)
- [x] Growth projections (5-year: 8.3 PB total storage)

### 📊 Key Takeaways

1. **Write-heavy workload:** 95% writes (acquire/release), 5% reads
2. **In-memory preferred:** Low latency requirements demand Redis/etcd
3. **Storage tiering essential:** Use hot/warm/cold tiers to control costs
4. **Regional deployment:** Reduce bandwidth costs by keeping traffic local
5. **Auto-scaling critical:** Traffic varies 3x between peak and off-peak

---

## Next Step: Architecture Design

Now that we know our capacity requirements, we can design the architecture to meet these numbers!

In the next document, we'll create **Step 1: Basic Architecture** that can handle:
- ✅ 200,000 operations/sec (peak)
- ✅ 10 million concurrent locks
- ✅ < 10ms latency (P50)
- ✅ 99.99% availability
- ✅ $10,000/month budget

Let's build it! 🚀
