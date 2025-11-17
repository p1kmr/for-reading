# Data Flow Diagrams & Scalability - Distributed Locking Service

## Critical Data Flows (Sequence Diagrams)

### Flow 1: Successful Lock Acquisition

```mermaid
sequenceDiagram
    participant Client as Client Application
    participant DNS as Route53 DNS
    participant LB as Load Balancer
    participant LS as Lock Server
    participant Redis as Redis Cache
    participant etcd as etcd Cluster
    participant Kafka as Kafka Queue
    participant Audit as Audit Logger

    Client->>DNS: Resolve lock-service.example.com
    DNS-->>Client: Return nearest region IP (us-east-1)

    Client->>LB: POST /api/v1/locks<br/>{resourceId: "order:12345", ttl: 30}
    Note over Client,LB: HTTPS, JWT authentication

    LB->>LS: Forward request to Lock Server 1

    LS->>LS: Validate JWT token
    LS->>LS: Check rate limit

    LS->>Redis: GET "lock:order:12345"
    Redis-->>LS: Cache MISS (lock status unknown)

    LS->>etcd: CompareAndSet("/locks/order:12345", nil → lockData)
    Note over LS,etcd: Atomic operation via Raft consensus

    etcd->>etcd: Raft: Replicate to followers
    etcd->>etcd: Raft: Commit (quorum reached)
    etcd-->>LS: SUCCESS (lock acquired)

    LS->>Redis: SET "lock:order:12345" {isLocked: true} TTL=5s
    Note over LS,Redis: Cache for future reads

    LS->>Kafka: Publish {event: "LOCK_ACQUIRED", resourceId: "order:12345"}
    Note over LS,Kafka: Async, fire-and-forget

    LS-->>LB: 201 Created<br/>{success: true, lockToken: "abc123..."}
    LB-->>Client: Return response

    Kafka->>Audit: Consumer reads event
    Audit->>Audit: Write to PostgreSQL
    Note over Kafka,Audit: Async background processing
```

**Time Breakdown:**
```
1. DNS resolution: 10ms (cached after first request)
2. TLS handshake: 20ms (cached for connection reuse)
3. Load balancer: 1ms
4. Lock server validation: 2ms
5. Redis cache check: 1ms (miss)
6. etcd CompareAndSet: 10ms (Raft consensus)
7. Redis cache update: 1ms
8. Kafka publish: 1ms (async)
9. Response: 1ms

Total: ~47ms (first request)
Subsequent requests (cached DNS/TLS): ~17ms
P50 target: < 10ms (achieved for cached operations)
```

---

### Flow 2: Failed Lock Acquisition (Already Held)

```mermaid
sequenceDiagram
    participant ClientB as Client B
    participant LB as Load Balancer
    participant LS as Lock Server
    participant Redis as Redis Cache
    participant etcd as etcd Cluster

    Note over ClientB: Tries to acquire lock<br/>already held by Client A

    ClientB->>LB: POST /api/v1/locks<br/>{resourceId: "order:12345"}
    LB->>LS: Forward request

    LS->>Redis: GET "lock:order:12345"
    Redis-->>LS: Cache HIT {isLocked: true, owner: "client-A", expiresAt: +25s}
    Note over LS,Redis: Fast path: 1ms

    alt Cache shows locked
        LS-->>ClientB: 409 Conflict<br/>{error: "LOCK_ALREADY_HELD", retryAfter: 25}
        Note over LS,ClientB: No etcd query needed!
    else Cache expired, recheck etcd
        LS->>etcd: GET "/locks/order:12345"
        etcd-->>LS: Lock held by client-A
        LS->>Redis: Update cache
        LS-->>ClientB: 409 Conflict
    end
```

**Time Breakdown (Cache Hit):**
```
1. Load balancer: 1ms
2. Redis cache check: 1ms (HIT!)
3. Response: 1ms

Total: ~3ms (very fast! ✅)

Why so fast?
- Cache hit avoided slow etcd query (10ms saved)
- This is why we have caching!
```

---

### Flow 3: Lock Release

```mermaid
sequenceDiagram
    participant Client as Client Application
    participant LB as Load Balancer
    participant LS as Lock Server
    participant Redis as Redis Cache
    participant etcd as etcd Cluster
    participant Kafka as Kafka Queue
    participant Audit as Audit Logger

    Client->>LB: DELETE /api/v1/locks/{lockId}<br/>X-Lock-Token: "abc123..."
    LB->>LS: Forward request

    LS->>LS: Validate lock token

    LS->>etcd: CompareAndDelete("/locks/order:12345", expectedToken: "abc123")
    Note over LS,etcd: Only delete if token matches<br/>(prevents accidental release by wrong client)

    etcd->>etcd: Verify token matches
    etcd->>etcd: Delete lock entry
    etcd-->>LS: SUCCESS (lock released)

    LS->>Redis: DEL "lock:order:12345"
    Note over LS,Redis: Invalidate cache

    LS->>Redis: SET "lock:order:12345" {isLocked: false} TTL=2s
    Note over LS,Redis: Cache "available" status<br/>to prevent thundering herd

    LS->>Kafka: Publish {event: "LOCK_RELEASED", resourceId: "order:12345"}

    LS-->>Client: 200 OK {success: true, holdDuration: 15.2s}

    Kafka->>Audit: Consumer reads event
    Audit->>Audit: Write to PostgreSQL
```

**Time Breakdown:**
```
1. Load balancer: 1ms
2. Token validation: 1ms
3. etcd CompareAndDelete: 10ms
4. Redis invalidation: 1ms
5. Kafka publish: 1ms
6. Response: 1ms

Total: ~15ms
```

---

### Flow 4: Lock Expiry (Automatic TTL)

```mermaid
sequenceDiagram
    participant Client as Client (Crashed!)
    participant etcd as etcd Cluster
    participant Watch as etcd Watcher
    participant LS as Lock Server
    participant Redis as Redis Cache
    participant Kafka as Kafka Queue

    Note over Client: Client crashes while holding lock

    etcd->>etcd: TTL countdown: 30s → 0s
    etcd->>etcd: Lock expires, auto-delete

    etcd->>Watch: Emit DELETE event<br/>(watch on "/locks/*")

    Watch->>LS: Notify: Lock expired for "order:12345"

    LS->>Redis: DEL "lock:order:12345"
    Note over LS,Redis: Invalidate stale cache

    LS->>Kafka: Publish {event: "LOCK_EXPIRED", resourceId: "order:12345"}

    Note over Client,Kafka: Lock automatically cleaned up!<br/>No manual intervention needed ✅
```

**Why This Matters:**
```
Without TTL:
❌ Client crashes → Lock held forever → Deadlock!
❌ Manual intervention required → Slow recovery

With TTL:
✅ Client crashes → Lock auto-expires after 30s
✅ Other clients can acquire lock → No manual work
✅ System is self-healing!
```

---

### Flow 5: Lock Renewal / Extension

```mermaid
sequenceDiagram
    participant Client as Client Application
    participant LB as Load Balancer
    participant LS as Lock Server
    participant etcd as etcd Cluster
    participant Redis as Redis Cache
    participant Kafka as Kafka Queue

    Note over Client: Processing takes longer than expected<br/>Need to extend lock

    Client->>LB: PUT /api/v1/locks/{lockId}/renew<br/>{additionalTtl: 30}
    LB->>LS: Forward request

    LS->>LS: Validate lock token

    LS->>etcd: RefreshLease("/locks/order:12345", +30s)
    Note over LS,etcd: Extend TTL by 30 seconds

    etcd->>etcd: Update expiresAt timestamp
    etcd-->>LS: SUCCESS (new expiresAt)

    LS->>Redis: SET "lock:order:12345" {expiresAt: +60s} TTL=5s
    Note over LS,Redis: Update cache with new expiry

    LS->>Kafka: Publish {event: "LOCK_RENEWED", additionalTtl: 30}

    LS-->>Client: 200 OK {newExpiresAt: +60s, totalTtl: 60}

    Note over Client: Can continue processing safely
```

**Use Case Example:**
```
Scenario: Video processing service

Time 0s: Acquire lock on "video:12345" (TTL 30s)
Time 15s: Video processing 50% done
Time 20s: Renew lock (+30s) → New expiry: 40s from now
Time 35s: Video processing 90% done
Time 40s: Renew lock again (+30s)
Time 55s: Processing complete, release lock

Without renewal:
❌ Lock expires at 30s → Another service grabs it
❌ Both services process same video → Wasted compute!

With renewal:
✅ Lock held continuously until done
✅ No duplicate work ✅
```

---

### Flow 6: Blocking Lock Acquisition (Wait for Lock)

```mermaid
sequenceDiagram
    participant ClientB as Client B
    participant LB as Load Balancer
    participant LS as Lock Server
    participant etcd as etcd Cluster
    participant Redis as Redis Cache

    ClientB->>LB: POST /api/v1/locks<br/>{resourceId: "order:12345", blocking: true, timeout: 10}
    LB->>LS: Forward request

    LS->>etcd: Try CompareAndSet
    etcd-->>LS: FAILURE (lock held by Client A)

    Note over LS: Enter wait loop

    loop Every 1 second (up to 10 seconds)
        LS->>LS: Sleep 1 second
        LS->>etcd: Try CompareAndSet again
        alt Lock acquired
            etcd-->>LS: SUCCESS
            LS-->>ClientB: 201 Created {lockToken: "xyz..."}
        else Still locked
            etcd-->>LS: FAILURE (still held)
        end
    end

    alt Timeout reached (10 seconds)
        LS-->>ClientB: 409 Conflict {error: "TIMEOUT", message: "Could not acquire lock within 10 seconds"}
    end
```

**Optimization: etcd Watch API**
```
Instead of polling every 1 second (inefficient):

Better approach:
1. Try acquire → Fails
2. Subscribe to etcd Watch on this key
3. Wait for DELETE event (lock released)
4. Immediately try to acquire
5. Return result

Benefits:
- No polling overhead
- Immediate notification (0.1s vs 1s delay)
- Lower etcd load
```

---

## Scalability Strategies

### Horizontal Scaling

#### Lock Servers (Stateless)
```
Current: 240 servers
Traffic: 66,667 ops/sec

To scale to 200,000 ops/sec:
New servers needed: 200,000 / 2,000 = 100 servers per region × 3 regions = 300 servers

How to add:
1. Deploy new servers (Kubernetes: kubectl scale deployment lock-service --replicas=300)
2. Load balancer auto-detects via health checks
3. Traffic automatically distributed
4. Total time: ~5 minutes

Cost: Linear scaling (2x traffic = ~2x servers)
```

#### Redis Cache
```
Current: 9 nodes (3 shards, 3 replicas each)
Memory: 36 GB total

To scale to 100 GB:
New setup: 9 shards, 3 replicas = 27 nodes

How to add:
1. Add 6 new shards (18 nodes)
2. Redis resharding (move hash slots)
3. Gradual migration (no downtime)
4. Total time: ~2 hours

Cost: Linear scaling
```

#### etcd Cluster
```
Current: 3 nodes per region
Storage: 100 GB per node

Vertical scaling (easier):
- Upgrade to larger instances (more CPU, RAM, disk)
- No resharding needed
- Downtime: ~5 minutes per node (rolling upgrade)

Horizontal scaling (harder):
- Add more nodes (3 → 5)
- Better fault tolerance (can lose 2 nodes instead of 1)
- Slower writes (more Raft replicas)
- Only needed if write throughput is bottleneck
```

---

### Database Sharding

**When to shard?**
- Single etcd cluster handles 10 million locks (current)
- Max etcd capacity: ~50 million locks per cluster
- Shard when > 40 million locks (80% capacity)

**Sharding Strategy: By Resource Type**
```
Shard 1 (orders): "order:*" keys
- etcd cluster 1 (3 nodes)
- Handles 15 million order locks

Shard 2 (users): "user:*" keys
- etcd cluster 2 (3 nodes)
- Handles 20 million user locks

Shard 3 (other): "*" (catch-all)
- etcd cluster 3 (3 nodes)
- Handles 5 million misc locks

Lock server routing logic:
if resourceId.startsWith("order:"):
    route to etcd cluster 1
elif resourceId.startsWith("user:"):
    route to etcd cluster 2
else:
    route to etcd cluster 3
```

**Alternative: Consistent Hashing**
```
hash(resourceId) % numShards = shardId

Example:
hash("order:12345") = 8432
8432 % 3 = 2 → Shard 2

Pros:
+ Even distribution
+ Easy to add shards (rehash only 1/N keys)

Cons:
- Harder to query (which shard has "order:*"?)
- Cross-shard operations difficult
```

---

## Failure Recovery

### Scenario: etcd Node Failure

```mermaid
graph TB
    A[etcd-1A CRASHES] --> B{Raft detects failure<br/>3 seconds}
    B --> C{Remaining nodes: 2/3}
    C -->|Quorum exists| D[Promote etcd-1B to leader<br/>2 seconds]
    D --> E[Lock servers reconnect<br/>1 second]
    E --> F[Normal operations resume<br/>Total downtime: 6s]

    style A fill:#ff6b6b
    style F fill:#51cf66
```

**Impact:**
```
Downtime: 6 seconds
Requests affected: 66,667 ops/sec × 6s = 400,000 requests
With retry: 99.9% will succeed on retry

Action needed: None (auto-recovery) ✅
Post-recovery: Replace failed node (within 1 hour)
```

---

### Scenario: Region Failure

```mermaid
graph TB
    A[US-East Region DOWN] --> B{Route53 health checks fail<br/>90 seconds}
    B --> C[Route53 stops returning US-East IP]
    C --> D[Clients re-resolve DNS]
    D --> E[Route to US-West instead<br/>2 minutes]
    E --> F[US-West scales up<br/>5 minutes]
    F --> G[Normal operations resume<br/>Total impact: 7 minutes]

    style A fill:#ff6b6b
    style G fill:#51cf66
```

**Impact:**
```
Affected users: 40% (US-East users)
Degraded service: 7 minutes
Requests lost: Minimal (clients retry with exponential backoff)
SLA impact: 7 min × 40% = 2.8 min downtime (within 52 min annual budget ✅)
```

---

## Performance Tuning

### Latency Optimization Techniques

| Technique | Latency Improvement | Complexity |
|-----------|---------------------|------------|
| **Add Redis cache** | 10ms → 1ms (10x) | Medium |
| **Use gRPC instead of HTTP** | 17ms → 12ms (30%) | Medium |
| **Connection pooling** | Save 20ms TLS handshake | Low |
| **Regional deployment** | 150ms → 10ms (15x) | High |
| **Batch operations** | N × 17ms → 1 × 20ms | Medium |
| **Async logging** | No blocking (async) | Low |

**Priority:**
1. Regional deployment (biggest win)
2. Caching (easy, big impact)
3. Connection pooling (easy)
4. gRPC (medium effort, medium gain)

---

## Summary

### Key Data Flows
✅ Lock acquisition: 17ms (P50), 10ms with cache
✅ Lock release: 15ms
✅ Lock expiry: Automatic (no manual intervention)
✅ Lock renewal: 12ms
✅ Blocking acquisition: Optimized with etcd Watch API

### Scalability Limits
- **Current:** 66k ops/sec, 10M concurrent locks
- **Max with current architecture:** 250k ops/sec, 50M locks
- **Beyond:** Shard etcd clusters, add more regions

### Failure Recovery
- **Single server:** 0s downtime (load balancer handles)
- **etcd node:** 6s downtime (Raft failover)
- **Entire region:** 7 min degraded (DNS failover + scaling)

---

**Next Document:** [Interview Q&A](09_interview_qa.md)
