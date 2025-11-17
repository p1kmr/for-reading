# Step 3: Adding Coordination Service (Distributed Locks)

## 🎯 What We're Solving in This Step

**Problem with Step 2:**
- What if we run 2 schedulers for high availability?
- Both schedulers might publish the same job to the queue! 😱
- Result: Job executes TWICE (duplicate execution)

**Example disaster scenario:**
```
Job: "Charge customer $100 for monthly subscription"
Scheduler 1: Sees job at 09:00:00 → Publishes to queue
Scheduler 2: Sees job at 09:00:00 → Publishes to queue
Worker 1: Executes job → Customer charged $100 ✅
Worker 2: Executes job → Customer charged $100 AGAIN ❌❌
Customer gets double-charged! 💸💸
```

**Solution in Step 3:**
- Add **Distributed Coordination Service** (Zookeeper or etcd)
- **Distributed Locks** ensure only ONE scheduler publishes each job
- **Leader Election** ensures only ONE scheduler is active at a time

---

## 📊 Architecture Diagram - Step 3

```mermaid
graph TB
    subgraph "Client Layer"
        Client[👤 User/Application]
    end

    subgraph "API Layer"
        API[🌐 API Gateway]
    end

    subgraph "Coordination Layer"
        Coord[🔒 Zookeeper/etcd Cluster<br/>Distributed Locks<br/>Leader Election<br/>3 nodes for HA]
    end

    subgraph "Scheduler Layer"
        Sched1[⏰ Scheduler 1<br/>🏆 LEADER]
        Sched2[⏰ Scheduler 2<br/>⏸️ STANDBY]
        Sched3[⏰ Scheduler 3<br/>⏸️ STANDBY]
    end

    subgraph "Message Queue Layer"
        Queue[📨 RabbitMQ<br/>Job Queue]
        DLQ[💀 Dead Letter Queue]
    end

    subgraph "Worker Layer"
        Worker1[⚙️ Worker 1]
        Worker2[⚙️ Worker 2]
        Worker3[⚙️ Worker 3]
    end

    subgraph "Database Layer"
        DBMaster[(🗄️ PostgreSQL Master<br/>Writes)]
        DBSlave1[(🗄️ PostgreSQL Slave 1<br/>Reads)]
        DBSlave2[(🗄️ PostgreSQL Slave 2<br/>Reads)]
    end

    Client --> API
    API --> DBMaster

    Sched1 -.1. Heartbeat.-> Coord
    Sched2 -.1. Heartbeat.-> Coord
    Sched3 -.1. Heartbeat.-> Coord

    Coord -->|2. Elect Leader| Sched1
    Coord -.2. Notify Standby.-> Sched2
    Coord -.2. Notify Standby.-> Sched3

    Sched1 -->|3. Acquire Lock| Coord
    Sched1 -->|4. Query Jobs| DBSlave1
    Sched1 -->|5. Publish Job| Queue

    Queue --> Worker1
    Queue --> Worker2
    Queue --> Worker3

    Worker1 --> DBMaster
    Worker2 --> DBMaster
    Worker3 --> DBMaster

    DBMaster -.Replication.-> DBSlave1
    DBMaster -.Replication.-> DBSlave2

    Worker1 -.Failed Jobs.-> DLQ
    Worker2 -.Failed Jobs.-> DLQ
    Worker3 -.Failed Jobs.-> DLQ

    style Client fill:#e1f5ff,stroke:#01579b,stroke-width:2px
    style API fill:#fff3e0,stroke:#e65100,stroke-width:2px
    style Coord fill:#fff9c4,stroke:#f57f17,stroke-width:3px
    style Sched1 fill:#c8e6c9,stroke:#2e7d32,stroke-width:3px
    style Sched2 fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    style Sched3 fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    style Queue fill:#ffccbc,stroke:#bf360c,stroke-width:2px
    style Worker1 fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px
    style Worker2 fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px
    style Worker3 fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px
    style DBMaster fill:#ffcdd2,stroke:#c62828,stroke-width:2px
    style DBSlave1 fill:#e0f2f1,stroke:#004d40,stroke-width:2px
    style DBSlave2 fill:#e0f2f1,stroke:#004d40,stroke-width:2px
```

---

## 🆕 What Changed from Step 2?

### Before (Step 2):
```
Scheduler (single instance) → Queue → Workers
```
- Only 1 scheduler (Single Point of Failure!)
- If scheduler crashes → System is DOWN ❌

### After (Step 3):
```
Zookeeper: Elects Leader
Scheduler 1 (🏆 LEADER) → Queue → Workers
Scheduler 2 (⏸️ Standby) → Watching for leader failure
Scheduler 3 (⏸️ Standby) → Watching for leader failure
```
- 3 schedulers, but only 1 LEADER is active
- If leader crashes → Standby immediately becomes leader ✅
- Distributed locks prevent duplicate job execution

---

## 🧩 New Components Added

### 1. Zookeeper/etcd Cluster (Yellow 💛)

**What it is:**
- A distributed coordination service
- Think of it as a **traffic cop** that ensures order in distributed systems 🚦

**What it does:**
1. **Leader Election:** Decides which scheduler is the active leader
2. **Distributed Locks:** Ensures only one scheduler processes each job
3. **Configuration Management:** Stores system-wide config (which schedulers are alive)
4. **Health Monitoring:** Tracks heartbeats from all schedulers

**Analogy:**
Imagine a classroom with 3 teachers:
- Without Zookeeper: All 3 teachers might teach the same lesson (chaos!)
- With Zookeeper: Zookeeper says "Teacher 1 is the main teacher today, others assist"

---

### Technology Options: Zookeeper vs etcd vs Consul

| Feature | Apache Zookeeper | etcd | Consul |
|---------|------------------|------|--------|
| **Language** | Java | Go | Go |
| **Use Case** | Coordination, locks, leader election | Coordination, key-value store | Service discovery, health checks |
| **Performance** | 10K ops/sec | 10K ops/sec | 5K ops/sec |
| **Ease of Use** | Complex (Java ecosystem) | Simpler (REST API) | Easiest (built-in UI) |
| **Community** | Large (Hadoop, Kafka use it) | Growing (Kubernetes uses it) | Medium (HashiCorp ecosystem) |
| **Best for** | Big data systems, job schedulers ✅ | Kubernetes, cloud-native apps | Microservices, service mesh |

**Our choice: Zookeeper** because:
- ✅ Industry standard for job schedulers (Airflow, Quartz use it)
- ✅ Battle-tested (used by LinkedIn, Netflix, Twitter)
- ✅ Strong consistency guarantees (CP in CAP theorem)
- ✅ Excellent leader election support

---

### 2. Multiple Scheduler Instances (HA)

**Why 3 schedulers?**
- **High Availability:** If 1 dies, 2 others are ready
- **Fault Tolerance:** Can tolerate 1 failure and still have 2 healthy schedulers
- **Failover:** Standby promotes to leader in <5 seconds

**Scheduler states:**
- **LEADER (🏆):** Actively scanning database and publishing jobs
- **STANDBY (⏸️):** Waiting, monitoring leader's health
- **DEAD (💀):** Crashed or network partitioned

**Why not 2 schedulers?**
- If 1 dies → Only 1 left → Single point of failure again!
- If both think they're leader (split-brain) → Duplicate execution!

**Why not 5 or 7 schedulers?**
- More schedulers = More cost, no extra benefit
- 3 is the sweet spot (1 active + 2 backup)

---

## 🔒 Distributed Locks Explained (For Beginners)

### Problem: The Race Condition

**Scenario:** Two schedulers check database at the same time:

```
Time    Scheduler 1                          Scheduler 2
09:00   Query: "SELECT job_id=123"
09:00   Result: Status=PENDING ✅
09:00                                        Query: "SELECT job_id=123"
09:00                                        Result: Status=PENDING ✅
09:01   UPDATE status=RUNNING
09:01   Publish job to queue
09:01                                        UPDATE status=RUNNING
09:01                                        Publish job to queue

Result: Job 123 published TWICE! ❌❌
```

### Solution: Distributed Lock

**With lock:**
```
Time    Scheduler 1                          Scheduler 2
09:00   Acquire lock for job_id=123 ✅
09:00   Query: "SELECT job_id=123"
09:00                                        Try to acquire lock for job_id=123
09:00                                        Lock denied (Scheduler 1 has it) ❌
09:00   UPDATE status=RUNNING
09:01   Publish job to queue
09:01   Release lock
09:01                                        Lock now available (but job already RUNNING)

Result: Job 123 published ONCE ✅
```

---

### How Zookeeper Locks Work

**Zookeeper uses "ephemeral znodes" for locks:**

```
Zookeeper structure:
/locks
  /job_123  ← ephemeral node (disappears when client disconnects)
```

**Lock acquisition flow:**
```python
# Scheduler 1 tries to acquire lock
lock_path = f"/locks/job_{job_id}"

try:
    zk.create(lock_path, ephemeral=True)
    # Success! Lock acquired
    print("Lock acquired for job_123")

    # Do work (publish job to queue)
    publish_job_to_queue(job)

finally:
    # Release lock (delete node)
    zk.delete(lock_path)
    print("Lock released")

```

**Scheduler 2 tries to acquire the same lock:**
```python
try:
    zk.create(lock_path, ephemeral=True)
except NodeExistsException:
    # Lock already held by Scheduler 1
    print("Lock already acquired, skipping this job")
    return
```

**What if Scheduler 1 crashes while holding lock?**
- Ephemeral node `/locks/job_123` **automatically deleted** by Zookeeper!
- Lock becomes available immediately
- Prevents deadlock (lock stuck forever)

**📝 Beginner Analogy:**
Think of a lock as a **bathroom key**:
- Only 1 person can hold the key at a time
- When they're done, they return the key
- If they forget to return it (crash), janitor (Zookeeper) retrieves it after timeout

---

## 🏆 Leader Election Explained

### Why Leader Election?

**Problem:**
- We have 3 schedulers for HA
- If all 3 are active → All 3 scan database → All 3 publish same jobs → CHAOS! 😱

**Solution:**
- Only 1 scheduler is **LEADER** (active)
- Others are **STANDBY** (watching)
- If leader dies → Standby promotes to leader

---

### How Zookeeper Leader Election Works

**Concept:** "Lowest sequence number becomes leader"

**1. All schedulers register in Zookeeper:**
```
Zookeeper structure:
/election
  /scheduler_0000000001  ← Scheduler 1 (lowest = LEADER 🏆)
  /scheduler_0000000002  ← Scheduler 2 (standby)
  /scheduler_0000000003  ← Scheduler 3 (standby)
```

**2. Each scheduler checks its sequence number:**
```python
def elect_leader():
    # Create ephemeral sequential node
    my_node = zk.create("/election/scheduler_", ephemeral=True, sequential=True)
    # Returns: "/election/scheduler_0000000002"

    # Get all nodes
    all_nodes = zk.get_children("/election")
    # Returns: ["scheduler_0000000001", "scheduler_0000000002", "scheduler_0000000003"]

    # Sort nodes
    all_nodes.sort()

    # Check if I'm the leader (lowest sequence number)
    if all_nodes[0] == my_node:
        print("I am the LEADER! 🏆")
        start_scheduling()
    else:
        print("I am STANDBY, watching the leader")
        watch_leader()
```

**3. Leader fails (crashes):**
```
Before:
/election
  /scheduler_0000000001  ← LEADER (crashes! 💥)
  /scheduler_0000000002  ← Standby
  /scheduler_0000000003  ← Standby

After (ephemeral node deleted):
/election
  /scheduler_0000000002  ← NEW LEADER 🏆
  /scheduler_0000000003  ← Standby
```

**4. Scheduler 2 detects leader failure:**
```python
def watch_leader():
    # Watch the node before me
    previous_node = all_nodes[my_index - 1]
    zk.watch(previous_node, on_node_deleted=promote_to_leader)

def promote_to_leader():
    print("Previous leader died! I am now LEADER 🏆")
    start_scheduling()
```

**Failover time:** Typically **2-5 seconds** ⚡

---

## 🔄 Updated Data Flow with Coordination

### Flow: Scheduled Job Execution (with Lock)

```
1. User submits job:
   User → API: POST /api/jobs {"schedule": "0 9 * * *"}
   API → Database: INSERT INTO jobs (next_run_time = "2025-11-18 09:00:00")

2. Scheduler election:
   Scheduler 1 → Zookeeper: Create /election/scheduler_0000000001
   Scheduler 2 → Zookeeper: Create /election/scheduler_0000000002
   Scheduler 3 → Zookeeper: Create /election/scheduler_0000000003
   Zookeeper: Scheduler 1 has lowest sequence → LEADER 🏆

3. Leader polls database (only Scheduler 1 is active):
   Scheduler 1 → Database: SELECT * FROM jobs WHERE next_run_time <= NOW()
   Database returns: [job_id: 123]

4. Leader acquires distributed lock:
   Scheduler 1 → Zookeeper: Create /locks/job_123 (ephemeral)
   Zookeeper: Lock acquired ✅

5. Leader publishes job:
   Scheduler 1 → Database: UPDATE jobs SET status='QUEUED' WHERE job_id=123
   Scheduler 1 → Queue: PUBLISH job_123
   Scheduler 1 → Zookeeper: Delete /locks/job_123 (release lock)

6. Worker executes job:
   Worker 1 → Queue: CONSUME job_123
   Worker 1: Execute job
   Worker 1 → Database: UPDATE executions SET status='COMPLETED'

7. Leader crash scenario:
   Scheduler 1 crashes! 💥
   Zookeeper: Detects missing heartbeat after 5 seconds
   Zookeeper: Deletes /election/scheduler_0000000001
   Scheduler 2: Detects leader node deleted → Promotes self to LEADER 🏆
   Scheduler 2: Continues scheduling (no downtime!)
```

---

## 🛡️ Preventing Duplicate Execution - Detailed Example

### Scenario: Network Partition (Split-Brain)

**What is split-brain?**
- Network fails, but servers are still running
- Scheduler 1 thinks Scheduler 2 is dead
- Scheduler 2 thinks Scheduler 1 is dead
- **Both might think they're leader!** 😱

**Without Zookeeper:**
```
Scheduler 1: "I'm leader, I'll publish job_123"
Scheduler 2: "I'm leader, I'll publish job_123"
Result: Job executes TWICE ❌
```

**With Zookeeper (using Quorum):**

Zookeeper runs with 3 nodes (quorum = 2):
```
Network partition:
[Scheduler 1] --- X --- [Zookeeper1, Zookeeper2, Scheduler 2]
                Partition!

Scheduler 1 side:
  - Can't reach 2 Zookeeper nodes
  - Lost quorum (1 < 2)
  - **Cannot acquire locks** ❌
  - Steps down from leadership

Scheduler 2 side:
  - Can reach 2 Zookeeper nodes
  - Has quorum (2 ≥ 2)
  - **Can acquire locks** ✅
  - Becomes leader

Result: Only Scheduler 2 publishes jobs ✅
```

**📝 Math Tip: Quorum Calculation**
```
Quorum = (N / 2) + 1
Where N = total number of Zookeeper nodes

3 nodes → Quorum = (3/2) + 1 = 1.5 + 1 = 2.5 → Round down = 2
5 nodes → Quorum = (5/2) + 1 = 2.5 + 1 = 3.5 → Round down = 3
7 nodes → Quorum = (7/2) + 1 = 3.5 + 1 = 4.5 → Round down = 4
```

**Why always use odd numbers (3, 5, 7)?**
- 3 nodes: Can tolerate 1 failure (2 nodes still form quorum)
- 4 nodes: Can tolerate 1 failure (but 4th node is wasted cost!)
- 5 nodes: Can tolerate 2 failures (3 nodes form quorum)
- 6 nodes: Can tolerate 2 failures (but 6th node is wasted!)

**Rule:** Use smallest odd number that meets your failure tolerance!

---

## ⚖️ Design Decisions & Trade-offs

### Decision 1: Zookeeper vs Database Locks

**Option A: Use PostgreSQL locks (pg_advisory_lock)**
```sql
-- Acquire lock
SELECT pg_advisory_lock(job_id);

-- Do work
UPDATE jobs SET status='RUNNING' WHERE job_id=123;

-- Release lock
SELECT pg_advisory_unlock(job_id);
```

**Pros:**
- ✅ No extra dependency (already using PostgreSQL)
- ✅ Simpler setup

**Cons:**
- ❌ Locks tied to database connection (if connection dies, lock stuck!)
- ❌ Database becomes bottleneck (every lock is a DB query)
- ❌ No leader election support

**Option B: Use Zookeeper (Our choice ✅)**

**Pros:**
- ✅ Ephemeral nodes (locks auto-release when client dies)
- ✅ Built-in leader election
- ✅ Designed for distributed coordination
- ✅ Scales independently of database

**Cons:**
- ❌ Extra dependency (one more system to maintain)
- ❌ Operational complexity (need 3-node cluster)

**When to use Zookeeper:**
- Distributed systems with multiple schedulers (our case ✅)
- Need leader election
- Need fault-tolerant locks

**When to use DB locks:**
- Single scheduler (no HA needed)
- Simple use case
- Want to minimize dependencies

---

### Decision 2: Leader Election vs Active-Active Schedulers

**Option A: Leader Election (Our choice ✅)**
- Only 1 scheduler active (leader)
- Others are standby (warm backup)

**Pros:**
- ✅ Simple coordination (no conflicts)
- ✅ Clear ownership (only leader touches jobs)
- ✅ No duplicate execution risk

**Cons:**
- ❌ Underutilized resources (standby schedulers idle)

**Option B: Active-Active (All schedulers work concurrently)**
- All 3 schedulers scan database
- Use distributed locks for each job

**Pros:**
- ✅ Higher throughput (3x parallelism)
- ✅ Better resource utilization

**Cons:**
- ❌ More lock contention (3 schedulers competing)
- ❌ Higher Zookeeper load
- ❌ Complex coordination logic

**Our choice:** Leader election for simplicity. At 612 jobs/sec, 1 scheduler is enough!

**When to switch to Active-Active:**
- If we reach 10,000+ jobs/sec
- When leader becomes the bottleneck

---

### Decision 3: Lock Granularity

**Option A: Lock per job (fine-grained) - Our choice ✅**
```
/locks/job_123
/locks/job_124
/locks/job_125
```

**Pros:**
- ✅ High parallelism (different jobs can be processed concurrently)
- ✅ Minimal lock contention

**Cons:**
- ❌ More Zookeeper nodes (1 per job)

**Option B: Single global lock (coarse-grained)**
```
/locks/scheduler_lock
```

**Pros:**
- ✅ Simple (only 1 lock)

**Cons:**
- ❌ No parallelism (only 1 job processed at a time) ❌
- ❌ Bottleneck at scale

**Our choice:** Lock per job for better parallelism!

---

## 📊 Performance Impact

### Latency Overhead

**Step 2 (no coordination):**
```
Scheduler → Queue → Worker
Latency: 100 milliseconds
```

**Step 3 (with Zookeeper locks):**
```
Scheduler → Zookeeper (acquire lock: 10ms)
         → Queue
         → Worker
         → Zookeeper (release lock: 10ms)
Latency: 120 milliseconds
```

**Overhead:** 20ms per job (acceptable! ✅)

---

### Zookeeper Capacity

**Zookeeper throughput:** ~10,000 ops/sec (3-node cluster)

**Our load:**
```
Operations per job:
  - Acquire lock: 1 op
  - Release lock: 1 op
  Total: 2 ops per job

Peak load: 612 jobs/sec
Zookeeper ops: 612 × 2 = 1,224 ops/sec

Utilization: 1,224 / 10,000 = 12.24% ✅
```

**Verdict:** Zookeeper can easily handle our load!

---

## 🚨 Failure Scenarios

### Scenario 1: Leader Crashes

```
Before:
  Scheduler 1 (LEADER 🏆)
  Scheduler 2 (Standby)
  Scheduler 3 (Standby)

Event: Scheduler 1 crashes 💥

After 5 seconds:
  Scheduler 1 (DEAD 💀)
  Scheduler 2 (NEW LEADER 🏆)
  Scheduler 3 (Standby)

Recovery: No downtime! Jobs keep being scheduled ✅
```

---

### Scenario 2: Zookeeper Node Fails

```
Zookeeper cluster: 3 nodes (need quorum = 2)

Before:
  Zookeeper1 ✅
  Zookeeper2 ✅
  Zookeeper3 ✅

Event: Zookeeper3 crashes 💥

After:
  Zookeeper1 ✅
  Zookeeper2 ✅
  Zookeeper3 💀

Quorum: 2 ≥ 2 → Still operational ✅
Recovery: No downtime! System continues working
```

**What if 2 Zookeeper nodes crash?**
```
  Zookeeper1 ✅
  Zookeeper2 💀
  Zookeeper3 💀

Quorum: 1 < 2 → Lost quorum ❌
Result: System halts (cannot acquire locks)
Action: Restart failed Zookeeper nodes immediately!
```

---

### Scenario 3: Network Partition

```
Partition:
  [Scheduler 1] --- X --- [Zookeeper1, Zookeeper2, Zookeeper3, Scheduler 2]

Scheduler 1 side:
  - Cannot reach Zookeeper cluster
  - Cannot acquire locks
  - Steps down from leadership ✅

Scheduler 2 side:
  - Can reach Zookeeper cluster
  - Acquires locks and becomes leader ✅

Result: No split-brain! Only Scheduler 2 is active ✅
```

---

## ✅ What We Achieved in Step 3

✅ Distributed locks prevent duplicate job execution
✅ Leader election for scheduler HA (3 schedulers, 1 active)
✅ Automatic failover (<5 seconds if leader crashes)
✅ Split-brain prevention (quorum-based consensus)
✅ Fault-tolerant coordination (Zookeeper cluster)
✅ Lock granularity per job (high parallelism)
✅ Ephemeral locks (auto-release on crash)

---

## 🚀 Next Steps

**Proceed to Step 4:**
- Add **Monitoring & Observability** (Prometheus, Grafana, ELK)
- Database design deep-dive (schema, indexes, sharding)
- Job status tracking and audit logs

**Why we need it:**
- Currently blind to system health (is scheduler overloaded?)
- No visibility into failed jobs
- No alerts when things go wrong

