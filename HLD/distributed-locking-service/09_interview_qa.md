# Interview Q&A - Distributed Locking Service

## Common Interview Questions & Model Answers

### Q1: How would you design a distributed locking service?

**Answer:**

I'd start by clarifying requirements:
- Scale (how many locks, clients, requests/sec)
- Latency requirements (P50, P95, P99)
- Consistency vs availability trade-offs (CAP theorem)

**High-level approach:**

1. **Core Components:**
   - Stateless lock servers (for scalability)
   - etcd cluster for lock state (strong consistency via Raft)
   - Redis cache for performance
   - Load balancer for traffic distribution

2. **Key Design Decisions:**
   - Use etcd instead of Redis for correctness (Raft consensus vs async replication)
   - TTL-based locks to prevent deadlocks (auto-expire after 30s)
   - Regional deployment for low latency and high availability
   - Cache-aside pattern for reads (80% cache hit rate)

3. **API:**
   - `POST /locks` - Acquire lock
   - `DELETE /locks/{id}` - Release lock
   - `PUT /locks/{id}/renew` - Extend TTL

4. **Failure Handling:**
   - Client crashes → TTL expires automatically
   - etcd node fails → Raft elects new leader
   - Region fails → DNS routes to backup region

**Trade-offs:**
- Chose **CP** (consistency + partition tolerance) over **AP** (availability)
- Correctness > speed for lock decisions
- Cost: ~$10k/month for 66k ops/sec

---

### Q2: Why use etcd instead of Redis for distributed locking?

**Answer:**

| Aspect | etcd | Redis |
|--------|------|-------|
| **Consistency** | Strong (Raft consensus) ✅ | Eventual (async replication) ❌ |
| **Split-brain** | Prevented (quorum) ✅ | Possible (no quorum) ❌ |
| **Atomic operations** | Native CAS ✅ | Native CAS ✅ |
| **Speed** | ~10ms writes | ~1ms writes |

**Why etcd wins for locking:**

Redis problem scenario:
```
1. Master receives lock request from Client A
2. Master acknowledges Client A (lock acquired)
3. Master crashes BEFORE replicating to replica
4. Replica promoted to master (has no record of lock!)
5. Client B acquires same lock from new master
6. SPLIT-BRAIN: Both clients own lock! 🔥
```

etcd prevents this:
```
1. Leader receives lock request from Client A
2. Leader replicates to followers via Raft
3. Leader waits for quorum (2/3) to acknowledge
4. Only then acknowledges Client A
5. Even if leader crashes, followers have the lock record
6. New leader elected, Client B correctly sees lock is held ✅
```

**When to use Redis:**
- Caching layer (fast reads, eventual consistency OK)
- Non-critical locks (where correctness isn't life-or-death)
- Rate limiting, session storage

**When to use etcd:**
- Distributed coordination (locks, leader election)
- Strong consistency required (financial transactions, inventory)
- Configuration management (Kubernetes uses etcd!)

---

### Q3: How do you handle the case where a client holds a lock but doesn't release it?

**Answer:**

This is solved with **TTL (Time-To-Live) / Lease-based locks**:

1. **Every lock has an expiration time:**
   ```
   Lock acquired at: 12:00:00
   TTL: 30 seconds
   Auto-expires at: 12:00:30
   ```

2. **etcd automatically deletes expired locks:**
   - No background cleanup job needed
   - Native etcd feature

3. **If client crashes:**
   ```
   Time 0s: Client acquires lock (TTL 30s)
   Time 10s: Client crashes (network cable unplugged!)
   Time 30s: Lock expires automatically
   Time 31s: Other clients can acquire the lock
   ```

4. **If client is slow:**
   - Client can **renew/extend** the lock before it expires
   - `renewLock(lockToken, additionalTtl=30)`
   - Prevents expiry during long-running operations

**Edge case: Clock skew**
```
Problem: What if server clocks are out of sync?

Solution:
- Use NTP (Network Time Protocol) to sync clocks
- Allow max clock skew: 100ms
- TTL is long enough (30s) that 100ms skew doesn't matter
- etcd uses monotonic clocks internally (immune to clock jumps)
```

---

### Q4: How do you prevent split-brain in a multi-region deployment?

**Answer:**

**Split-brain:** When network partition causes two regions to both think they're the primary.

**Prevention: Quorum-Based Consensus**

Setup: 3 regions (US-East, US-West, EU-West)

```
Rule: Need majority (2/3) to acquire global lock

Scenario: Network partition (US ↔ EU)

Side A (US-East + US-West):
- 2 regions connected
- 2 ≥ quorum (2) ✅
- Can acquire locks

Side B (EU-West):
- 1 region isolated
- 1 < quorum (2) ❌
- REJECTS lock requests
- Returns: "Cannot reach quorum"

Result: Only one side can acquire locks → No split-brain! ✅
```

**Implementation:**
```go
func AcquireGlobalLock(resourceId) bool {
    votes := []
    votes.append(proposeToRegion("us-east"))
    votes.append(proposeToRegion("us-west"))
    votes.append(proposeToRegion("eu-west"))

    successVotes := count(votes where vote == "agree")

    if successVotes >= 2 {  // Quorum
        commitLock(resourceId)
        return true
    }
    return false  // Reject (safety first!)
}
```

**Alternative approaches:**
1. **Odd number of regions** (3, 5, 7) for clear majority
2. **Witness/arbiter node** in third region (tie-breaker)
3. **Fencing tokens** (incrementing token, reject old tokens)

---

### Q5: What is the CAP theorem and how does it apply to your design?

**Answer:**

**CAP Theorem:** In a distributed system with network partitions, you can only guarantee 2 out of 3:
- **C**onsistency: All nodes see the same data
- **A**vailability: System always responds
- **P**artition Tolerance: System works despite network failures

**Our choice: CP (Consistency + Partition Tolerance)**

**Why sacrifice availability?**

Example scenario:
```
Network partition: US ↔ EU

Option 1 (AP - Availability):
- Both sides continue accepting lock requests
- Risk: Two clients own same lock (data corruption!)
- 100% uptime, but WRONG results ❌

Option 2 (CP - Consistency):
- Only majority side accepts requests
- Minority side rejects (returns error)
- Some downtime, but results are CORRECT ✅
```

**For locking, correctness > availability:**
- Better to say "no" than give wrong answer
- Banking analogy: ATM should reject transaction if can't verify balance

**Where availability > consistency:**
- Social media feeds (eventual consistency OK)
- DNS (stale data acceptable for few seconds)
- CDN (stale content OK)

---

### Q6: How do you handle high contention on a single resource?

**Answer:**

**High contention:** Many clients trying to acquire lock on same resource simultaneously.

**Example:**
```
1,000 clients all want lock on "popular-product:12345"
Only 1 client can hold it at a time
999 clients will fail → Retry → Fail again → Thundering herd!
```

**Solutions:**

**1. Exponential Backoff**
```python
def acquire_with_backoff(resourceId):
    retries = 0
    delay = 0.1  # Start with 100ms

    while retries < 10:
        if acquire_lock(resourceId):
            return True

        # Exponential backoff: 100ms, 200ms, 400ms, 800ms...
        time.sleep(delay)
        delay *= 2
        retries += 1

    return False  # Give up after 10 retries
```

**2. Queue-Based Approach**
```
Instead of all clients competing:
1. Clients enqueue in Redis: LPUSH "queue:product:12345" clientId
2. Lock service processes queue in order (FIFO)
3. Client polls: "Am I next?" (or use pub/sub notification)

Benefits:
- Fair (first-come-first-served)
- No thundering herd
- Predictable wait time
```

**3. Sharding by Sub-Resource**
```
Instead of locking entire product:
Lock specific inventory unit:
- "product:12345:unit:001" (Client A)
- "product:12345:unit:002" (Client B)
- "product:12345:unit:003" (Client C)

Multiple clients can process same product simultaneously!
```

**4. Optimistic Locking (for databases)**
```sql
-- Don't lock at distributed level, use DB version field
UPDATE inventory
SET quantity = quantity - 1, version = version + 1
WHERE product_id = 12345 AND version = 5;

-- If 0 rows affected, version changed (conflict) → Retry
```

**5. Rate Limiting**
```
Limit lock acquisition attempts:
- Per client: 100 requests/min
- Per resource: 1,000 requests/min

Prevents single hot resource from overloading system
```

---

### Q7: How would you scale this system to 10 million requests per second?

**Answer:**

**Current:** 66,667 ops/sec
**Target:** 10,000,000 ops/sec (150x scale!)

**Approach:**

**1. Horizontal Scaling (Stateless Layers)**
```
Lock Servers:
Current: 240 servers × 2,000 ops/sec = 480k capacity
Target: 10M / 2,000 = 5,000 servers needed
Cost: 5,000 × $20/month = $100k/month (10x current)

Redis Cache:
Current: 9 nodes (36 GB)
Target: 135 nodes (540 GB) (15x scale)
```

**2. Sharding (Stateful Layers)**
```
etcd Clusters:
Current: 1 cluster (3 nodes) per region
Max capacity: 100k writes/sec per cluster

Target: 10M writes/sec
Need: 10M / 100k = 100 etcd clusters

Sharding strategy:
- Shard by resource type: order:*, user:*, product:*
- Consistent hashing: hash(resourceId) % 100 = clusterId
```

**3. Regional Expansion**
```
Current: 3 regions (US-East, US-West, EU-West)
Target: 10 regions (add Asia-Pacific, South America, Africa, Middle East)

Benefits:
- Lower latency (closer to users)
- Higher capacity (10x regions = 10x capacity)
- Better availability (more redundancy)
```

**4. Optimization Techniques**
```
a) Protocol upgrade: HTTP/2 → gRPC (30% faster)
b) Batch operations: Acquire 10 locks in 1 request
c) Lock coalescing: Multiple clients share same lock
d) Bloom filters: Quick "definitely not locked" check
e) Read replicas: Scale reads independently of writes
```

**5. Cost Optimization**
```
Current: $10k/month
Target: ~$1M/month (100x scale)

Optimizations:
- Spot instances (40% discount) → $600k
- Reserved instances (60% discount) → $400k ✅
- Multi-tenancy (share infrastructure) → $200k
- Edge caching (reduce origin traffic) → $150k
```

**Architecture changes:**
```
New layer: Lock Coordinator (global)
- Manages 100 etcd clusters
- Routes requests to correct shard
- Handles cross-shard transactions
```

---

### Q8: How do you handle clock skew in a distributed system?

**Answer:**

**Clock skew:** Servers have slightly different times (network latency, drift).

**Why it matters for locking:**
```
Scenario:
Server A clock: 12:00:00
Server B clock: 12:00:10 (10 seconds ahead!)

Client acquires lock at "Server A time"
Expires at: 12:00:30 (Server A)
But Server B thinks current time is: 12:00:10
Server B calculates: Lock expires in 20 seconds (WRONG! Should be 30)
```

**Solutions:**

**1. NTP (Network Time Protocol)**
```
- All servers sync with NTP pool (pool.ntp.org)
- Clock drift: < 50 ppm (parts per million)
- Max skew: 100ms (well within safety margin)

With 30-second TTL, 100ms skew = 0.3% error (acceptable)
```

**2. Monotonic Clocks (etcd's approach)**
```
Don't use wall clock time for TTL calculation
Use monotonic clock (always increases, never jumps)

Go example:
start := time.Now()  // Wall clock: 12:00:00
// NTP adjusts wall clock: 12:00:10 (jumped 10 seconds!)
elapsed := time.Since(start)  // Monotonic: 5 seconds (correct!)
```

**3. Logical Clocks (Lamport timestamps)**
```
Instead of "expires at 12:00:30":
Use event counters: "expires after 100 events"

Each server maintains counter:
- Event 1: Lock acquired
- Event 50: Halfway
- Event 100: Lock expires

No dependency on wall clock time!
```

**4. Google TrueTime (ideal but expensive)**
```
Used by Google Spanner
Hardware-backed time synchronization
Uncertainty interval: ±7 ms

Returns: [earliest, latest] time range
System waits for uncertainty window to pass before committing
```

**5. Fencing Tokens (Facebook's approach)**
```
Don't rely on time at all!

Each lock gets incrementing token:
- Lock 1: token = 1
- Lock 2: token = 2
- Lock 3: token = 3

Resource server rejects requests with old tokens:
if request.token < latest_token:
    reject()  # Stale lock, someone else has newer lock

Immune to clock skew! ✅
```

---

### Q9: Explain the differences between distributed locks and database transactions.

**Answer:**

| Aspect | Distributed Lock | Database Transaction |
|--------|------------------|----------------------|
| **Scope** | Cross-service coordination | Single database operations |
| **Granularity** | Coarse (lock entire resource) | Fine (row-level locks) |
| **Duration** | Long (seconds to minutes) | Short (milliseconds) |
| **Deadlock** | Prevented by TTL | Detected and rolled back |
| **Rollback** | Manual (app logic) | Automatic (ACID) |
| **Use Case** | Microservices coordination | Data consistency within DB |

**When to use Distributed Lock:**
```
Scenario: Process order across 3 microservices
1. Payment service charges credit card
2. Inventory service decrements stock
3. Shipping service creates shipment

Lock "order:12345" for entire workflow (30 seconds)
→ Ensures only one instance processes this order
```

**When to use Database Transaction:**
```
Scenario: Transfer money between accounts
BEGIN TRANSACTION;
  UPDATE accounts SET balance = balance - 100 WHERE id = 1;
  UPDATE accounts SET balance = balance + 100 WHERE id = 2;
COMMIT;

→ Ensures atomicity within single database
```

**Can you use both?**
```
Yes! Distributed lock for coordination, DB transaction for data consistency:

1. Acquire distributed lock on "order:12345"
2. Start DB transaction
3. Update order status
4. Charge payment
5. Commit DB transaction
6. Release distributed lock

Lock ensures only one worker processes order
Transaction ensures database consistency
```

---

### Q10: How would you implement a distributed lock using Redis? (Redlock algorithm)

**Answer:**

**Redlock Algorithm** (proposed by Redis creator Salvatore Sanfilippo):

**Setup:** 5 independent Redis masters (odd number for quorum)

**Acquire Lock:**
```python
def acquire_redlock(resource_id, ttl):
    lock_token = generate_unique_token()
    start_time = time.now()

    # Step 1: Try to acquire lock on all 5 Redis instances
    successful_locks = 0
    for redis in redis_instances:
        try:
            # SET key value NX PX ttl
            # NX = only set if not exists
            # PX = expire in milliseconds
            result = redis.set(
                key=f"lock:{resource_id}",
                value=lock_token,
                nx=True,  # Only if not exists
                px=ttl * 1000  # TTL in milliseconds
            )
            if result:
                successful_locks += 1
        except:
            pass  # Skip failed instances

    # Step 2: Check if we got quorum (majority)
    elapsed = time.now() - start_time
    validity_time = ttl - elapsed - drift_time

    if successful_locks >= 3 and validity_time > 0:
        # We got the lock!
        return (True, lock_token, validity_time)
    else:
        # Failed to get quorum, release locks
        release_redlock(resource_id, lock_token)
        return (False, None, 0)
```

**Release Lock:**
```python
def release_redlock(resource_id, lock_token):
    # Use Lua script for atomic check-and-delete
    lua_script = """
    if redis.call("get", KEYS[1]) == ARGV[1] then
        return redis.call("del", KEYS[1])
    else
        return 0
    end
    """

    for redis in redis_instances:
        redis.eval(lua_script, f"lock:{resource_id}", lock_token)
```

**Why 5 instances?**
```
Quorum = 3 out of 5

Can tolerate: 2 failures
- 3 healthy instances still form quorum
- If only 2 instances up, lock requests rejected (safety)

Compared to 3 instances:
- 3 instances: quorum = 2, can tolerate 1 failure
- 5 instances: quorum = 3, can tolerate 2 failures (better!)
```

**Criticisms of Redlock:**
1. **Clock drift issues** (relies on TTL expiry)
2. **Network partitions** (quorum can split)
3. **Complexity** (5 Redis instances to manage)
4. **No protection against process pauses** (GC pause, swap)

**Martin Kleppmann's critique:**
- Redlock is unsafe under certain conditions
- Better to use proper consensus (Raft/Paxos) like etcd/ZooKeeper

**Our recommendation:**
- Use **etcd** for critical locks (strong consistency)
- Use **single Redis** for non-critical locks (caching, rate limiting)
- Avoid **Redlock** (complex, not significantly better than single Redis)

---

### Q11: What happens if a client acquires a lock, then experiences a long GC pause?

**Answer:**

This is a **classic distributed lock problem!**

**Scenario:**
```
Time 0s: Client A acquires lock (TTL 30s)
Time 10s: Client A starts processing
Time 11s: Client A experiences 25-second GC pause (JVM stop-the-world)
Time 30s: Lock expires (TTL elapsed)
Time 36s: Client B acquires same lock
Time 36s: Client A wakes up from GC pause
         → Thinks it still has lock!
         → Both clients process same resource! 🔥
```

**Solutions:**

**1. Fencing Tokens (Recommended) ✅**
```
Each lock acquisition gets incrementing token:
- Client A: lock token = 100
- Client B: lock token = 101 (higher = newer)

Resource server tracks highest token seen:
def process_request(data, lock_token):
    if lock_token < highest_token_seen:
        reject()  # Stale lock!

    highest_token_seen = max(highest_token_seen, lock_token)
    process(data)

When Client A wakes up:
- Sends request with token=100
- Server rejects (has seen token=101)
- Client A realizes lock is stale, acquires new lock ✅
```

**2. Heartbeat / Lock Renewal**
```
Client periodically renews lock while processing:

while processing:
    if time_since_last_renewal > 15s:
        renew_lock(lock_token)

    do_work()

If GC pauses for 25s:
- Client fails to renew
- Lock expires
- When client wakes up, renewal fails
- Client detects: "I lost the lock!" → Stop processing
```

**3. Distributed Transaction Log**
```
Before taking action, write to append-only log:

1. Acquire lock
2. Write to log: "Client A processing order:12345"
3. Process order
4. Write to log: "Client A completed order:12345"

If Client B acquires lock:
- Check log: "Is this order already processed?"
- If yes, skip (idempotency)

Trade-off: Extra log writes, but safer
```

**4. Set TTL longer than max GC pause**
```
If max GC pause = 10s:
Set TTL = 60s (6x safety margin)

Cons:
- Longer recovery time if client crashes
- Not a complete solution (still need fencing tokens)
```

**Real-world example: AWS DynamoDB**
```
DynamoDB uses fencing tokens for conditional writes:

put_item(
    table="orders",
    item={"id": 12345, "status": "processed"},
    condition="attribute_not_exists(processed) OR lock_token > :token"
)

Prevents duplicate processing even with lock expiry!
```

---

### Q12: How do you monitor and debug distributed lock issues?

**Answer:**

**Key Metrics to Track:**

**1. Lock Acquisition Metrics**
```
- lock_acquisition_rate (ops/sec)
- lock_acquisition_latency_p50/p95/p99 (ms)
- lock_acquisition_success_rate (%)
- lock_acquisition_failure_reasons (breakdown by error type)
```

**2. Lock Contention Metrics**
```
- lock_contention_rate (failed acquisitions due to lock held)
- top_10_most_contended_resources
- average_wait_time_for_lock (for blocking mode)
```

**3. Lock Hold Duration**
```
- lock_hold_duration_p50/p95/p99 (seconds)
- locks_held_longer_than_ttl (should be 0!)
- lock_renewal_rate
```

**4. System Health**
```
- etcd_write_latency
- etcd_raft_proposal_duration
- redis_cache_hit_rate
- kafka_consumer_lag
```

**Dashboards (Grafana):**
```
Dashboard 1: Lock Operations Overview
- Acquisition rate (time series)
- Latency percentiles (line graph)
- Error rate (percentage)
- Regional breakdown (heatmap)

Dashboard 2: Lock Contention
- Top 10 most locked resources (bar chart)
- Contention rate over time (line graph)
- Average hold duration by resource type (histogram)

Dashboard 3: System Health
- etcd cluster status (gauges)
- Redis memory usage (gauge)
- Lock server CPU/memory (heatmap)
```

**Alerting Rules:**
```yaml
# High error rate
- alert: HighLockErrorRate
  expr: lock_error_rate > 1%
  action: Page on-call engineer

# High contention
- alert: HighLockContention
  expr: lock_contention_rate > 1000/sec
  action: Slack notification (investigate hot resources)

# Stale locks
- alert: StaleLocks
  expr: locks_held_longer_than_max_ttl > 0
  action: Page on-call (potential deadlock!)
```

**Debugging Tools:**

**1. Lock Audit Logs**
```sql
-- Who last held this lock?
SELECT * FROM lock_audit
WHERE resource_id = 'order:12345'
ORDER BY timestamp DESC
LIMIT 10;

-- Which client is causing high contention?
SELECT owner_id, COUNT(*) as lock_count
FROM lock_audit
WHERE event_type = 'ACQUIRED'
  AND timestamp > NOW() - INTERVAL '1 hour'
GROUP BY owner_id
ORDER BY lock_count DESC;
```

**2. Distributed Tracing (Jaeger/Zipkin)**
```
Trace lock acquisition across services:

Span 1: Client → Lock Service (17ms)
  Span 1.1: Lock Service → Redis (cache miss, 1ms)
  Span 1.2: Lock Service → etcd (acquire lock, 10ms)
  Span 1.3: Lock Service → Kafka (publish event, 1ms)

Visualize entire flow, identify bottlenecks
```

**3. etcd Health Check**
```bash
# Check etcd cluster health
etcdctl endpoint health --cluster

# Check Raft status
etcdctl endpoint status --cluster -w table

# Get lock details
etcdctl get /locks/order:12345

# Watch for lock changes (real-time)
etcdctl watch /locks/ --prefix
```

---

## Summary: Key Interview Takeaways

### Design Principles
✅ **Correctness over performance** (use etcd, not Redis, for lock state)
✅ **TTL for automatic cleanup** (prevent deadlocks)
✅ **Fencing tokens** (prevent stale lock issues)
✅ **Quorum-based consensus** (prevent split-brain)
✅ **Regional deployment** (low latency, high availability)
✅ **Comprehensive monitoring** (detect issues before users do)

### Trade-offs Made
- **CP over AP** (consistency over availability)
- **etcd over Redis** (correctness over speed)
- **Multi-region cost** ($10k/month for 99.99% SLA)
- **Complexity** (simple for clients, complex internally)

### Scale Achieved
- **66,667 ops/sec** (can scale to 10M ops/sec)
- **10 million concurrent locks** (can scale to 50M)
- **< 10ms P50 latency** (meets SLA)
- **99.99% availability** (52 min downtime/year)

---

**Next Document:** [Beginner Mistakes & Best Practices](10_beginner_mistakes.md)
