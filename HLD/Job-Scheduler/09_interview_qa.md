# Interview Questions & Answers

## 🎯 Overview

This document provides **20+ common system design interview questions** specifically for job scheduler systems, with detailed answers.

---

## Section 1: Requirements & Scoping Questions

### Q1: How would you design a distributed job scheduler like Apache Airflow?

**Answer:**

**Step 1: Clarify requirements (always start here!)**
```
Interviewer Questions:
- Scale: How many jobs per day? → Assume 1M jobs/day initially
- Types: What kinds of jobs? → HTTP requests, scripts, SQL queries
- Latency: How precise does scheduling need to be? → ±5 seconds acceptable
- Priorities: Do some jobs need priority? → Yes, payment jobs are critical
- Dependencies: Can jobs depend on other jobs? → Future feature (start without)
```

**Step 2: Estimate capacity**
```
1M jobs/day = 12 jobs/sec average
Peak (3x) = 36 jobs/sec

Storage:
  - Job metadata: 1M jobs × 1 KB = 1 GB/day
  - Execution logs: 17M executions/day × 2 KB = 34 GB/day
  Total: ~35 GB/day → 12.7 TB/year
```

**Step 3: High-level architecture (draw diagram)**
```
Components:
1. API Gateway (REST API for job submission)
2. Scheduler (polls database, publishes to queue)
3. Message Queue (RabbitMQ/Kafka for job distribution)
4. Workers (execute jobs)
5. Database (PostgreSQL for job metadata)
6. Zookeeper (leader election, distributed locks)
```

**Step 4: Deep dive into specific areas**
```
- How do you prevent duplicate execution? → Distributed locks
- How do you handle scheduler failure? → Leader election with Zookeeper
- How do you scale workers? → Horizontal scaling, auto-scaling groups
- How do you monitor the system? → Prometheus, Grafana, ELK stack
```

---

### Q2: What are the functional and non-functional requirements for a job scheduler?

**Answer:**

**Functional Requirements:**
1. Users can submit one-time and recurring jobs
2. Jobs execute at scheduled time
3. Users can cancel jobs
4. System provides job status (queued, running, completed, failed)
5. Failed jobs are retried with exponential backoff
6. Execution logs are stored for debugging

**Non-Functional Requirements:**
1. **Scalability:** 1M jobs/day → 100M jobs/day
2. **Availability:** 99.99% uptime (52 min downtime/year)
3. **Latency:** Jobs execute within ±5 seconds of scheduled time
4. **Consistency:** Exactly-once execution (or at-least-once with idempotency)
5. **Durability:** Jobs and logs are not lost (database replication)
6. **Security:** Authentication, authorization, encrypted secrets

---

## Section 2: Component Design Questions

### Q3: How would you design the scheduler component?

**Answer:**

**Scheduler responsibilities:**
1. Poll database every 5 seconds for jobs where `next_run_time <= NOW()`
2. Acquire distributed lock for each job (prevent duplicates)
3. Publish job to message queue
4. Update job status and next_run_time

**Pseudocode:**
```python
while True:
    # Poll database
    pending_jobs = db.query("""
        SELECT * FROM jobs
        WHERE next_run_time <= NOW()
        AND status = 'PENDING'
        LIMIT 1000
    """)

    for job in pending_jobs:
        # Acquire lock
        if zookeeper.acquire_lock(f"/locks/job_{job.id}"):
            # Update status
            db.update(f"UPDATE jobs SET status='QUEUED' WHERE job_id={job.id}")

            # Publish to queue
            queue.publish(job)

            # Release lock
            zookeeper.release_lock(f"/locks/job_{job.id}")

            # Update next_run_time for recurring jobs
            if job.schedule:
                next_time = calculate_next_run(job.schedule)
                db.update(f"UPDATE jobs SET next_run_time='{next_time}', status='PENDING'")

    time.sleep(5)
```

**Optimization for high scale:**
- Replace polling with event-driven (Kafka)
- Partition jobs across multiple schedulers
- Use database indexes on `next_run_time`

---

### Q4: Should you use a message queue or direct worker assignment?

**Answer:**

**Option A: Direct Assignment (like Step 1)**
```
Scheduler → Directly calls Worker API
```

**Pros:**
- ✅ Simple to implement
- ✅ No extra dependency

**Cons:**
- ❌ Tight coupling (scheduler knows about all workers)
- ❌ No buffering (if workers busy, jobs fail)
- ❌ Scheduler must track worker health

**Option B: Message Queue (Recommended ✅)**
```
Scheduler → Queue → Workers consume
```

**Pros:**
- ✅ Decoupling (scheduler doesn't know workers)
- ✅ Buffering (queue handles traffic spikes)
- ✅ Automatic load balancing (workers pull when ready)
- ✅ Retry mechanism (if worker crashes, job stays in queue)

**Cons:**
- ❌ Extra dependency (RabbitMQ/Kafka)
- ❌ Slightly higher latency (+10-20ms)

**Interview Tip:** Always mention trade-offs! There's no "perfect" answer, only "good for this use case."

---

### Q5: How do you prevent a job from being executed twice?

**Answer:**

**Problem:**
```
Scenario 1: Network partition causes both Scheduler 1 and Scheduler 2 to publish same job
Scenario 2: Worker crashes after executing job but before ACKing message → Job redelivered
```

**Solutions:**

**1. Distributed Locks (Prevents duplicate publishing)**
```python
# Before publishing job
if zookeeper.create_lock(f"/locks/job_{job.id}", ephemeral=True):
    queue.publish(job)
    zookeeper.delete_lock(f"/locks/job_{job.id}")
else:
    # Lock already held by another scheduler, skip
    pass
```

**2. Idempotency Keys (Prevents duplicate execution)**
```python
def execute_job(job):
    key = f"{job.id}_{job.scheduled_time}"

    # Check if already executed
    if db.exists("SELECT * FROM idempotency_keys WHERE key=%s", key):
        return "Already executed"

    # Execute and store key
    result = do_work(job)
    db.insert("INSERT INTO idempotency_keys (key, result) VALUES (%s, %s)", key, result)
    return result
```

**3. Database Constraints (Last resort)**
```sql
-- Unique constraint on (job_id, scheduled_time)
CREATE UNIQUE INDEX idx_executions_unique ON executions (job_id, start_time);
-- If job executed twice, second INSERT will fail
```

**Interviewer Follow-up:** "What if Zookeeper is down?"
**Answer:** Scheduler cannot acquire locks → Stops publishing jobs → System becomes read-only until Zookeeper recovers. This is acceptable (availability trade-off for consistency).

---

## Section 3: Scalability Questions

### Q6: How would you scale this system to handle 1 million jobs per second?

**Answer:**

**Current bottlenecks at 1M jobs/sec:**

**1. Database bottleneck (single master)**
```
Solution: Horizontal sharding
- Shard by user_id: Shard 1 handles user_id 0-100K, Shard 2 handles 100K-200K, etc.
- Each shard: 100K jobs/sec → Need 10 shards
- Use consistent hashing for shard routing
```

**2. Queue bottleneck (RabbitMQ tops out at 50K/sec)**
```
Solution: Switch to Apache Kafka
- Kafka handles 1M+ messages/sec
- Partition topic by user_id (100 partitions)
- Each partition: 10K jobs/sec
```

**3. Scheduler bottleneck (single leader publishes all jobs)**
```
Solution: Partition scheduler workload
- Run 100 active schedulers (not just 1 leader!)
- Each scheduler handles jobs for specific user_id ranges
- Scheduler 1: user_id % 100 = 0
- Scheduler 2: user_id % 100 = 1
- ...
```

**4. Zookeeper bottleneck (10K ops/sec limit)**
```
Solution: Reduce lock operations
- Use optimistic locking (check-update-commit in database)
- Or: Leader election only (not per-job locks)
```

**5. Worker bottleneck**
```
Solution: Horizontal scaling with auto-scaling
- Current: 800 workers
- At 1M jobs/sec: Need 100,000 workers
- Use Kubernetes Horizontal Pod Autoscaler
- Scale based on queue depth
```

---

### Q7: How do you handle traffic spikes (10x normal load)?

**Answer:**

**Scenario:** Black Friday causes 10x traffic (360 jobs/sec → 3,600 jobs/sec)

**Solutions:**

**1. Auto-scaling Workers**
```
Trigger: Queue depth > 10,000
Action: Scale workers from 800 → 2,000

AWS Auto Scaling Group:
  min_instances: 800
  max_instances: 5,000
  target_metric: queue_depth < 5,000
```

**2. Rate Limiting with Quotas**
```
Free tier: 100 jobs/day (reject excess)
Pro tier: 10,000 jobs/day
Enterprise: Unlimited

Return 429 Too Many Requests if quota exceeded
```

**3. Priority Queueing**
```
Critical jobs (payments): Always execute immediately
Normal jobs: Execute when capacity available
Low-priority jobs (analytics): Delay by 1 hour if spike

This ensures critical workflows don't fail
```

**4. Graceful Degradation**
```
If database CPU > 90%:
  - Enable read-only mode (allow status checks, block new jobs)
  - Better than complete outage!
```

---

## Section 4: Failure Handling Questions

### Q8: What happens if the scheduler crashes?

**Answer:**

**Scenario:** Scheduler leader crashes at 2:00:00 PM

**Timeline:**
```
14:00:00 - Scheduler 1 (leader) crashes 💥
14:00:05 - Zookeeper detects missing heartbeat (5-second timeout)
14:00:05 - Zookeeper deletes /election/scheduler_0000000001 (ephemeral node)
14:00:06 - Scheduler 2 detects leader node deletion
14:00:06 - Scheduler 2 becomes new leader 🏆
14:00:07 - Scheduler 2 resumes job scheduling

Total downtime: 7 seconds ✅
```

**Impact:**
- Jobs scheduled during 7-second window might be delayed
- No jobs are lost (they're in the database, will be picked up by new leader)
- Recurring jobs continue as normal

**Prevention:**
- Run 3 schedulers (1 leader + 2 standby)
- Health checks every 1 second
- Automatic failover via Zookeeper leader election

---

### Q9: What if a worker crashes while executing a job?

**Answer:**

**Scenario:** Worker is executing job, crashes midway

**RabbitMQ behavior:**
```
1. Worker consumes message from queue (marked "unacknowledged")
2. Worker crashes before sending ACK 💥
3. RabbitMQ waits for ACK timeout (5 minutes)
4. No ACK received → RabbitMQ requeues message
5. Another worker consumes and executes the job ✅
```

**Database state:**
```
executions table:
  execution_id: 98765
  status: "RUNNING"  (from crashed worker)
  worker_id: "worker-1"

New worker updates:
  execution_id: 98766 (new execution record)
  status: "RUNNING"
  worker_id: "worker-2"

Result: Job executed by worker-2, original execution marked as FAILED
```

**Idempotency concern:**
If job is "charge customer $100", and partially completed before crash:
```
Solution: Idempotency keys
  - Before executing, check if execution_key exists in idempotency_keys table
  - If exists, skip execution (return cached result)
  - If not exists, execute and store key
```

---

### Q10: How do you handle database failover?

**Answer:**

**Setup:**
```
Master (Primary) - All writes
Slave 1 (Replica) - Reads
Slave 2 (Replica) - Reads
```

**Failover scenario:**
```
1. Master crashes 💥
2. Health check detects failure (10 seconds)
3. Automated failover tool (Patroni/pg_auto_failover) runs election
4. Slave 1 promoted to new master (30 seconds)
5. Slave 2 starts replicating from new master
6. Applications reconnect to new master (via VIP)

Total failover time: ~40 seconds
```

**Data loss:**
```
Synchronous replication: 0 data loss (slave has all committed transactions)
Asynchronous replication: ~5 seconds of data loss (acceptable for most cases)
```

**Application handling:**
```python
try:
    db.execute("INSERT INTO jobs ...")
except DatabaseConnectionError:
    # Retry with exponential backoff
    for attempt in range(3):
        time.sleep(2 ** attempt)
        try:
            db.reconnect()
            db.execute("INSERT INTO jobs ...")
            break
        except:
            continue
```

---

## Section 5: Design Trade-offs Questions

### Q11: SQL vs NoSQL for job scheduler database?

**Answer:**

| Factor | SQL (PostgreSQL) ✅ | NoSQL (MongoDB) |
|--------|---------------------|-----------------|
| **ACID Transactions** | Yes - Critical for job status updates | Limited (single-document only) |
| **Consistency** | Strong consistency (no duplicates) | Eventual consistency (risk of duplicates) |
| **Queries** | Complex queries with JOINs (dependencies) | No JOINs (must denormalize) |
| **Scalability** | Vertical (single master bottleneck) | Horizontal (easy to shard) |
| **Schema** | Fixed schema | Flexible schema |
| **Best for** | Job scheduler (our choice) | Social feeds, analytics |

**Our choice: PostgreSQL** because:
1. Cannot afford duplicate job execution → Need strong consistency ✅
2. Complex queries needed (find jobs with dependencies, filter by status) ✅
3. At 12 jobs/sec, PostgreSQL easily handles the load ✅

**When to use NoSQL:**
- Scale > 100,000 jobs/sec (need horizontal sharding)
- Schema varies widely per job type
- Eventual consistency acceptable

---

### Q12: Polling vs Event-driven for scheduler?

**Answer:**

**Option A: Polling (Step 1 approach)**
```python
while True:
    jobs = db.query("SELECT * FROM jobs WHERE next_run_time <= NOW()")
    for job in jobs:
        publish_to_queue(job)
    time.sleep(5)
```

**Pros:**
- ✅ Simple to implement
- ✅ Works fine for low traffic (10-100 jobs/sec)

**Cons:**
- ❌ Wastes CPU (polls even when no jobs)
- ❌ Up to 5-second delay before execution
- ❌ Inefficient at high scale

**Option B: Event-driven (Step 2 approach)**
```python
# When job created via API
api.create_job(job)
db.insert(job)
if job.schedule == "immediate":
    queue.publish(job)  # No polling needed!
```

**Pros:**
- ✅ Real-time (0-second delay)
- ✅ Efficient (no wasted polls)

**Cons:**
- ❌ More complex
- ❌ Doesn't work for scheduled jobs (still need polling for those)

**Hybrid approach (Best):**
- Immediate jobs → Event-driven (publish directly)
- Scheduled jobs → Polling (but optimized with indexes)

---

### Q13: Leader election vs Active-Active schedulers?

**Answer:**

**Option A: Leader Election (Our approach ✅)**
```
Scheduler 1: Leader (active) 🏆
Scheduler 2: Standby (idle)
Scheduler 3: Standby (idle)

Only the leader publishes jobs.
```

**Pros:**
- ✅ Simple coordination (no conflicts)
- ✅ No duplicate job risk

**Cons:**
- ❌ Underutilized resources (standbys idle)
- ❌ Leader is bottleneck at very high scale

**Option B: Active-Active**
```
Scheduler 1: Active 🏆
Scheduler 2: Active 🏆
Scheduler 3: Active 🏆

All schedulers publish jobs concurrently.
```

**Pros:**
- ✅ Higher throughput (3x parallelism)
- ✅ Better resource utilization

**Cons:**
- ❌ Complex coordination (need distributed locks for every job!)
- ❌ Higher lock contention
- ❌ Risk of duplicate if locks fail

**Our choice:** Leader election for MVP (simpler). Switch to Active-Active if leader becomes bottleneck (>10K jobs/sec).

---

## Section 6: Operational Questions

### Q14: How do you monitor job scheduler health?

**Answer:**

**Metrics to track (Prometheus):**
```
1. Jobs scheduled per second: rate(scheduler_jobs_scheduled_total[5m])
2. Job success rate: sum(rate(scheduler_jobs_completed_total[5m])) / sum(rate(scheduler_jobs_total[5m]))
3. Job latency: histogram_quantile(0.95, worker_job_duration_seconds)
4. Queue depth: queue_depth{queue="pending_jobs"}
5. Worker health: up{job="worker"}
6. Database connection pool: db_connections_active / db_connections_max
```

**Alerts (AlertManager):**
```
1. Queue depth > 10,000 for 5 minutes → Warning (workers overloaded)
2. Job failure rate > 10% for 5 minutes → Critical (investigate!)
3. Scheduler down for 1 minute → Critical (failover didn't work?)
4. Database CPU > 90% for 10 minutes → Warning (scale up or shard)
```

**Dashboards (Grafana):**
```
1. System Overview: Jobs/sec, success rate, queue depth, worker count
2. Job Execution: Latency p50/p95/p99, duration by job type
3. Infrastructure: Database CPU/memory, queue lag, worker CPU
```

**Logging (ELK):**
```
1. Job execution logs (stdout/stderr from workers)
2. Scheduler decisions (which jobs were published)
3. Error logs (failed jobs with stack traces)
```

---

### Q15: How do you estimate cost for running this system?

**Answer:**

**Infrastructure costs (AWS example):**

**1. API Servers (3 instances)**
```
Instance type: t3.medium (2 vCPU, 4 GB RAM)
Cost: $0.0416/hour × 3 = $0.125/hour = $90/month
```

**2. Schedulers (3 instances)**
```
Instance type: t3.small (2 vCPU, 2 GB RAM)
Cost: $0.0208/hour × 3 = $0.0624/hour = $45/month
```

**3. Workers (800 instances)**
```
Instance type: t3.micro (2 vCPU, 1 GB RAM)
Cost: $0.0104/hour × 800 = $8.32/hour = $6,000/month
```

**4. Database (PostgreSQL RDS)**
```
Instance type: db.r5.xlarge (4 vCPU, 32 GB RAM) × 3 (1 master + 2 replicas)
Cost: $0.24/hour × 3 = $0.72/hour = $518/month
Storage: 2 TB × $0.115/GB = $230/month
Total database: $748/month
```

**5. Message Queue (RabbitMQ on EC2)**
```
Instance type: t3.medium × 3 (cluster)
Cost: $0.0416/hour × 3 = $0.125/hour = $90/month
```

**6. Zookeeper (3 nodes)**
```
Instance type: t3.small × 3
Cost: $0.0208/hour × 3 = $0.0624/hour = $45/month
```

**7. Monitoring (Prometheus, Grafana)**
```
Instance type: t3.medium × 2
Cost: $0.0416/hour × 2 = $0.0832/hour = $60/month
```

**Total monthly cost: $7,078/month ≈ $85,000/year**

**Cost per job:**
```
1M jobs/day × 30 days = 30M jobs/month
Cost per job = $7,078 / 30M = $0.00024/job
```

**Cost optimization:**
- Use spot instances for workers (70% cost savings)
- Auto-scale workers during low traffic (night time)
- Use managed services (AWS Fargate, RDS Aurora Serverless)

---

## ✅ Interview Tips

1. **Always start with requirements** - Don't jump to design
2. **Draw diagrams** - Visualize your architecture
3. **Discuss trade-offs** - There's no perfect solution
4. **Think about failure scenarios** - What if X crashes?
5. **Estimate capacity** - Back up decisions with numbers
6. **Iterate** - Start simple (MVP), then add complexity
7. **Ask clarifying questions** - "How many users? What's the scale?"
8. **Be honest** - If you don't know, say so, but reason through it

