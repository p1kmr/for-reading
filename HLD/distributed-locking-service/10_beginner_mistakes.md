# Common Beginner Mistakes & Best Practices

## Mistake 1: Using Redis as the Source of Truth

### ❌ Wrong Approach
```
Architecture:
Client → Lock Server → Redis (master-replica, async replication)

Problem:
1. Client A acquires lock → Redis master acknowledges
2. Redis master crashes BEFORE replicating to replica
3. Replica promoted to master (missing Client A's lock!)
4. Client B acquires same lock from new master
5. BOTH clients think they own lock → Data corruption! 🔥
```

### ✅ Correct Solution
```
Architecture:
Client → Lock Server → etcd (Raft consensus)

Why it works:
1. Client A acquires lock → etcd leader receives
2. Leader replicates to followers (Raft protocol)
3. Leader waits for quorum (2/3) acknowledgment
4. ONLY THEN acknowledges Client A
5. If leader crashes, new leader has lock record ✅

Key difference: Synchronous replication with quorum (etcd)
vs Asynchronous replication without quorum (Redis)
```

### When Redis is OK
```
Use Redis for:
- Caching (stale data acceptable)
- Rate limiting (approximate is fine)
- Session storage (can rebuild if lost)
- Non-critical locks (e.g., UI debouncing)

DON'T use Redis for:
- Financial transactions (bank transfers)
- Inventory management (prevent overselling)
- Critical system coordination (database migrations)
```

---

## Mistake 2: Forgetting to Set TTL (Time-To-Live)

### ❌ Wrong Approach
```python
def acquire_lock(resource_id):
    # Acquire lock WITHOUT TTL
    etcd.set(f"/locks/{resource_id}", client_id)
    # Lock held forever!

Problem:
1. Client acquires lock
2. Client crashes (network failure, power outage)
3. Lock NEVER released
4. Resource deadlocked forever
5. Manual intervention required to clean up ❌
```

### ✅ Correct Solution
```python
def acquire_lock(resource_id, ttl=30):
    # Acquire lock WITH TTL (auto-expires after 30 seconds)
    etcd.set(
        key=f"/locks/{resource_id}",
        value=client_id,
        ttl=30  # Auto-delete after 30 seconds ✅
    )

Benefits:
1. Client crashes → Lock auto-expires after 30s
2. No manual cleanup needed
3. System is self-healing!
```

### Choosing the Right TTL
```
Too short (5s):
- Pro: Fast recovery from crashes
- Con: Risk of lock expiring during legitimate processing

Too long (300s):
- Pro: No risk of premature expiry
- Con: Slow recovery from crashes (5 min wait!)

Sweet spot: 30 seconds
- Reasonable for most operations
- Fast recovery (acceptable downtime)
- Can renew if operation takes longer

Rule of thumb:
TTL = 2× expected operation time
Example: Processing takes 10s → Set TTL = 20s
         Can renew lock if operation takes longer
```

---

## Mistake 3: Not Validating Lock Ownership on Release

### ❌ Wrong Approach
```python
def release_lock(resource_id):
    # Delete lock WITHOUT checking ownership
    etcd.delete(f"/locks/{resource_id}")

Problem:
Time 0s: Client A acquires lock
Time 25s: Client A experiences network delay
Time 30s: Lock expires (TTL elapsed)
Time 31s: Client B acquires same lock
Time 32s: Client A's delayed request arrives
        → Deletes lock (but Client B owns it now!)
        → Client B thinks it has lock, but it's gone! 🔥
```

### ✅ Correct Solution
```python
def release_lock(resource_id, lock_token):
    # Only delete if token matches (atomic check-and-delete)
    lua_script = """
    if redis.call("get", KEYS[1]) == ARGV[1] then
        return redis.call("del", KEYS[1])
    else
        return 0  -- Wrong owner, reject!
    end
    """
    etcd.eval(lua_script, resource_id, lock_token)

Benefits:
- Client A's delayed release fails (wrong token)
- Client B's lock remains intact ✅
- No accidental deletion of others' locks
```

### Lock Token Pattern
```
When acquiring lock:
{
  "ownerId": "client-A",
  "lockToken": "550e8400-e29b-41d4-a716-446655440000",  // UUID
  "acquiredAt": "2025-01-17T12:00:00Z"
}

When releasing lock:
- Client must provide lockToken
- Server verifies: Does lockToken match stored value?
- Only then delete lock

This prevents:
- Accidental deletion (wrong client)
- Delayed deletion (lock already expired and re-acquired)
- Malicious deletion (attacker can't delete your lock)
```

---

## Mistake 4: No Handling of Clock Skew

### ❌ Wrong Approach
```python
# Client side
def acquire_lock(resource_id, ttl=30):
    lock = server.acquire_lock(resource_id, ttl)
    expires_at = time.now() + ttl  # Uses LOCAL clock

    # Work for 25 seconds
    do_work()

    # Check if lock still valid
    if time.now() < expires_at:  # LOCAL clock check
        # Client thinks: "I have 5 seconds left!"
        continue_work()
    else:
        renew_lock()

Problem:
- Client's clock is 10 seconds BEHIND server's clock
- Server: Lock expires at 12:00:30
- Client thinks: Lock expires at 12:00:40 (10s later)
- Client continues working after lock expired! 🔥
- Another client acquires lock
- Data corruption!
```

### ✅ Correct Solution
```python
# Server side (authoritative time)
def acquire_lock(resource_id, ttl=30):
    server_time_now = get_server_time()  # Use SERVER clock
    expires_at = server_time_now + ttl

    lock = {
        "expiresAt": expires_at,  # Server timestamp
        "ttl": ttl
    }
    etcd.set(resource_id, lock, ttl)
    return lock

# Client side
def check_lock_validity(lock):
    # Don't trust local clock!
    # Periodically check with server
    response = server.get_lock_status(lock.resource_id)

    if response.isLocked and response.ownerId == my_client_id:
        return True  # Still valid
    else:
        return False  # Expired or taken by someone else

# Or better: Use fencing tokens (no time dependency!)
```

### Best Practice: Use NTP
```bash
# Synchronize all servers with NTP
apt-get install ntp
systemctl start ntp

# Check clock synchronization
timedatectl status

# Should see:
# System clock synchronized: yes
# NTP service: active
```

---

## Mistake 5: Not Using Idempotent Operations

### ❌ Wrong Approach
```python
def process_order(order_id):
    # No idempotency check!
    lock = acquire_lock(f"order:{order_id}", ttl=30)

    charge_payment(order_id)  # $100 charged
    decrement_inventory(order_id)
    send_confirmation_email(order_id)

    release_lock(lock.token)

Problem:
1. Client sends request
2. Request times out (no response received)
3. Client retries (thinks previous request failed)
4. Previous request actually succeeded!
5. Payment charged TWICE! 🔥
6. Inventory decremented TWICE!
7. Customer gets two emails!
```

### ✅ Correct Solution
```python
def process_order(order_id, request_id):
    # Check if we already processed this request
    if redis.exists(f"processed:{request_id}"):
        # Return cached result (idempotent!)
        return redis.get(f"processed:{request_id}")

    lock = acquire_lock(f"order:{order_id}", ttl=30)

    # Idempotent operations (check before executing)
    if not is_payment_charged(order_id):
        charge_payment(order_id)

    if not is_inventory_decremented(order_id):
        decrement_inventory(order_id)

    if not is_email_sent(order_id):
        send_confirmation_email(order_id)

    release_lock(lock.token)

    # Cache result for 5 minutes (for retries)
    result = {"success": True, "order_id": order_id}
    redis.setex(f"processed:{request_id}", 300, result)

    return result
```

### Idempotency Patterns

**1. Idempotency Key (Stripe's approach)**
```http
POST /api/charges HTTP/1.1
Idempotency-Key: unique-key-123

If server sees same key twice:
→ Return cached response
→ Don't execute operation again
```

**2. Status State Machine**
```python
Order states: CREATED → PAYMENT_CHARGED → INVENTORY_UPDATED → COMPLETED

Each operation:
1. Check current state
2. Only execute if in correct state
3. Transition to next state atomically

Example:
if order.status == "CREATED":
    charge_payment()
    order.status = "PAYMENT_CHARGED"  # Atomic update

If retried:
- Order already in "PAYMENT_CHARGED" state
- Skip payment charge (idempotent!)
```

**3. Database Unique Constraints**
```sql
CREATE TABLE processed_orders (
    order_id VARCHAR(255) PRIMARY KEY,
    request_id VARCHAR(255) UNIQUE,  -- Prevents duplicates!
    processed_at TIMESTAMP
);

-- Attempt to insert
INSERT INTO processed_orders VALUES ('12345', 'req-abc', NOW());

-- If request_id already exists:
-- Unique constraint violation → Return cached result
```

---

## Mistake 6: Over-Locking (Too Coarse Granularity)

### ❌ Wrong Approach
```python
# Lock entire database table
lock = acquire_lock("table:orders")

# Process single order
update_order(order_id=12345)

release_lock(lock)

Problem:
- Locks ALL orders (thousands of rows)
- Only need to lock ONE order (row 12345)
- Other clients blocked unnecessarily
- Poor concurrency (everyone waits for each other)
```

### ✅ Correct Solution
```python
# Lock specific order (fine-grained)
lock = acquire_lock(f"order:{order_id}")

# Process this order only
update_order(order_id)

release_lock(lock)

Benefits:
- Other clients can process OTHER orders simultaneously
- High concurrency (1000 clients, 1000 different orders)
- Better throughput
```

### Granularity Guidelines

| Too Coarse (Bad) | Just Right (Good) | Too Fine (Overkill) |
|------------------|-------------------|---------------------|
| Lock entire database | Lock single resource | Lock individual fields |
| `lock:database` | `lock:order:12345` | `lock:order:12345:field:status` |
| **1 client at a time** | **1000 clients in parallel** | **Complex, unnecessary** |

**Rule of thumb:**
Lock the SMALLEST unit that needs coordination
- If updating order status → Lock single order
- If processing batch of 100 orders → Lock each order individually (not all at once!)
- If global system config → Lock global config (rare!)

---

## Mistake 7: Not Planning for Network Partitions

### ❌ Wrong Approach
```
Assumption: "Network is reliable, partitions won't happen"

Architecture:
- No quorum checks
- No split-brain prevention
- Assumes all regions always connected

Problem:
1. Transatlantic cable cut (yes, this happens!)
2. US and EU regions isolated
3. Both regions think they're primary
4. Both accept lock acquisitions for same resource
5. Split-brain scenario! 🔥
```

### ✅ Correct Solution
```
Assumption: "Network WILL partition, plan for it"

Architecture:
- Require quorum (majority) for lock acquisition
- 3 regions: Need 2/3 to acquire global lock
- Minority side rejects requests (safety first)

Scenario:
US-East + US-West (connected): 2/3 ✅ Can acquire locks
EU-West (isolated): 1/3 ❌ Rejects lock requests

Result:
- Only one side can acquire locks
- No split-brain! ✅
- Some downtime for EU users (acceptable)
- Correctness maintained (critical!)
```

### Testing for Network Partitions

**Chaos Engineering (Netflix's approach):**
```bash
# Simulate network partition
iptables -A INPUT -s <eu-region-ip> -j DROP
iptables -A OUTPUT -d <eu-region-ip> -j DROP

# Run tests:
1. Does minority side reject lock requests? ✅
2. Does majority side continue operating? ✅
3. When partition heals, do regions resynchronize? ✅

# Cleanup
iptables -F  # Flush rules
```

---

## Mistake 8: No Monitoring or Alerting

### ❌ Wrong Approach
```
"If it ain't broke, don't fix it"
- No metrics collection
- No dashboards
- No alerts
- Find out about issues from users: "Hey, your service is down!"
```

### ✅ Correct Solution
```
Proactive monitoring:
- Collect metrics every 15 seconds
- Dashboard shows real-time health
- Alerts trigger BEFORE users notice

Metrics to track:
1. Lock acquisition latency (P50, P95, P99)
2. Lock error rate (%)
3. etcd cluster health (leader election, disk usage)
4. Redis cache hit rate (%)
5. Active lock count
6. Lock contention rate

Alerts:
- Critical: Page engineer (P95 latency > 50ms, etcd down)
- Warning: Slack notification (disk 80% full, cache hit rate < 70%)
```

### Example: Prevented Outage
```
Without monitoring:
Time 0: etcd disk 95% full
Time 1: etcd crashes (out of disk)
Time 2: Lock service down
Time 3: Users report outage
Time 4: Engineer investigates (2 hours)
Result: 2 hours downtime ❌

With monitoring:
Time 0: etcd disk 80% full → Alert triggered
Time 1: Engineer adds disk space (15 minutes)
Time 2: Service continues operating
Result: 0 downtime ✅
```

---

## Mistake 9: Ignoring Lock Contention

### ❌ Wrong Approach
```python
def acquire_lock_with_retry(resource_id):
    while True:
        try:
            return acquire_lock(resource_id)
        except LockAlreadyHeldError:
            time.sleep(0.1)  # Wait 100ms
            # Retry immediately! (tight loop)

Problem:
- 1,000 clients all retry every 100ms
- 10,000 requests per second hammering server! (thundering herd)
- Lock server overloaded
- etcd overloaded
- Service degraded for everyone
```

### ✅ Correct Solution
```python
def acquire_lock_with_backoff(resource_id, max_retries=10):
    delay = 0.1  # Start with 100ms

    for attempt in range(max_retries):
        try:
            return acquire_lock(resource_id)
        except LockAlreadyHeldError as e:
            if attempt == max_retries - 1:
                raise  # Give up after 10 retries

            # Exponential backoff with jitter
            jitter = random.uniform(0, delay * 0.3)  # ±30% randomness
            sleep_time = delay + jitter
            time.sleep(sleep_time)

            delay = min(delay * 2, 10)  # Double, max 10 seconds

Benefits:
- Retry 1: 100ms (+jitter)
- Retry 2: 200ms (+jitter)
- Retry 3: 400ms (+jitter)
- ...
- Retry 10: 10s (+jitter)

Result:
- Clients spread out over time (jitter prevents synchronized retries)
- Lower server load
- Still eventually get lock
```

### Alternative: Queue-Based Approach
```python
# Instead of retry, enqueue and wait
def acquire_lock_with_queue(resource_id):
    position = redis.rpush(f"queue:{resource_id}", client_id)

    while True:
        # Check if we're first in queue
        first = redis.lindex(f"queue:{resource_id}", 0)
        if first == client_id:
            # Our turn! Try to acquire
            if acquire_lock(resource_id):
                redis.lpop(f"queue:{resource_id}")  # Remove from queue
                return True

        time.sleep(1)  # Check every second
```

---

## Mistake 10: Not Testing Failure Scenarios

### ❌ Wrong Approach
```
Testing only happy path:
✅ Lock acquired successfully
✅ Lock released successfully

NOT testing:
❌ What if etcd node crashes?
❌ What if client crashes mid-operation?
❌ What if network partition occurs?
❌ What if 10,000 clients retry simultaneously?
```

### ✅ Correct Solution
```
Test failure scenarios (chaos engineering):

1. Kill random etcd nodes:
   - Does Raft elect new leader?
   - Is quorum maintained?
   - Do lock operations succeed?

2. Inject network latency:
   - Add 1-second delay to all requests
   - Do TTLs still work correctly?
   - Do clients handle timeouts gracefully?

3. Crash clients mid-operation:
   - Kill client after acquiring lock
   - Does lock auto-expire?
   - Can other clients acquire it?

4. Partition network:
   - Isolate one region
   - Does quorum prevent split-brain?
   - Does minority side reject requests?

5. Load testing:
   - Send 100,000 requests/sec
   - Does system scale?
   - What is breaking point?

6. Clock skew simulation:
   - Set server clocks 10 seconds apart
   - Do locks still work correctly?
   - Are there race conditions?
```

### Example Test Suite
```python
def test_lock_survives_etcd_leader_crash():
    # Acquire lock
    lock = acquire_lock("resource:123")

    # Kill etcd leader
    kill_container("etcd-leader")

    # Wait for new leader election (5 seconds)
    time.sleep(5)

    # Lock should still be held
    status = get_lock_status("resource:123")
    assert status.isLocked == True
    assert status.ownerId == client_id

def test_lock_expires_after_client_crash():
    # Acquire lock with 30s TTL
    lock = acquire_lock("resource:123", ttl=30)

    # Simulate client crash (don't release lock)
    # Just wait...

    # After 31 seconds, lock should be gone
    time.sleep(31)

    status = get_lock_status("resource:123")
    assert status.isLocked == False  # Lock auto-expired ✅
```

---

## Summary: Checklist for Distributed Locking

### Must-Haves ✅
- [ ] Use consensus system (etcd/ZooKeeper), not async replication (Redis)
- [ ] Always set TTL on locks (prevent deadlocks)
- [ ] Validate lock ownership before release (use lock tokens)
- [ ] Implement exponential backoff (prevent thundering herd)
- [ ] Use idempotency keys (prevent duplicate operations)
- [ ] Plan for network partitions (quorum-based decisions)
- [ ] Monitor system health (metrics + alerts)
- [ ] Test failure scenarios (chaos engineering)

### Nice-to-Haves ✨
- [ ] Fencing tokens (prevent stale lock issues)
- [ ] Distributed tracing (debug cross-service issues)
- [ ] Audit logging (compliance, forensics)
- [ ] Multi-region deployment (low latency, high availability)
- [ ] Auto-scaling (handle traffic spikes)
- [ ] Circuit breakers (prevent cascade failures)

### Red Flags 🚩
- [ ] ❌ Using single Redis instance as source of truth
- [ ] ❌ No TTL on locks
- [ ] ❌ No validation on lock release
- [ ] ❌ Trusting client's clock for TTL calculations
- [ ] ❌ No monitoring or alerting
- [ ] ❌ Never tested failure scenarios

---

**Next Document:** [README - Complete Guide](README.md)
