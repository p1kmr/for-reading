# Common Beginner Mistakes & Best Practices

## 🎯 Overview

This document highlights **common mistakes** beginners make when designing job schedulers and provides **best practices** to avoid them.

---

## ❌ Mistake 1: Not Preventing Duplicate Execution

### The Mistake
```python
# Scheduler without distributed locks
def schedule_jobs():
    jobs = db.query("SELECT * FROM jobs WHERE next_run_time <= NOW()")
    for job in jobs:
        queue.publish(job)  # No lock! ❌
        db.update(f"UPDATE jobs SET status='QUEUED' WHERE job_id={job.id}")
```

### Why It's Bad
```
If 2 schedulers run concurrently:

Time    Scheduler 1                    Scheduler 2
10:00   Query DB: job_id=123 found
10:00                                  Query DB: job_id=123 found
10:01   Publish job_123 to queue
10:01                                  Publish job_123 to queue
10:02   Update status=QUEUED
10:02                                  Update status=QUEUED

Result: Job 123 published TWICE! 💥
If it's "charge customer $100" → Customer charged $200! 😱
```

### ✅ Correct Approach
```python
def schedule_jobs():
    jobs = db.query("SELECT * FROM jobs WHERE next_run_time <= NOW()")
    for job in jobs:
        # Acquire distributed lock
        lock = zookeeper.acquire_lock(f"/locks/job_{job.id}")
        if lock:
            try:
                queue.publish(job)
                db.update(f"UPDATE jobs SET status='QUEUED' WHERE job_id={job.id}")
            finally:
                zookeeper.release_lock(f"/locks/job_{job.id}")
```

**Key Takeaway:** Always use distributed locks when multiple schedulers might publish the same job!

---

## ❌ Mistake 2: Single Point of Failure

### The Mistake
```
Architecture:
  API → Scheduler (single instance) → Workers

If scheduler crashes → Entire system down! ❌
```

### Why It's Bad
```
99.9% uptime goal = 8.76 hours downtime/year
If scheduler crashes for 2 hours → Already exceeded budget! 😱

Real scenario:
  - Scheduler crashes at 3 AM
  - On-call engineer wakes up at 6 AM
  - 3 hours of downtime → Missed SLA ❌
```

### ✅ Correct Approach
```
Run 3 schedulers with leader election:
  Scheduler 1: Leader (active)
  Scheduler 2: Standby (monitors leader)
  Scheduler 3: Standby (monitors leader)

If Scheduler 1 crashes:
  - Zookeeper detects failure in 5 seconds
  - Scheduler 2 automatically becomes leader
  - No manual intervention needed! ✅
  - Downtime: 5 seconds (acceptable)
```

**Key Takeaway:** Never have a single point of failure for critical components!

---

## ❌ Mistake 3: Not Considering Scale from Day 1

### The Mistake
```python
# Storing job payload in database as TEXT
CREATE TABLE jobs (
    job_id BIGINT,
    payload TEXT,  -- "http://example.com?param1=value1&param2=value2..."
    ...
);

# Querying payload
SELECT * FROM jobs WHERE payload LIKE '%example.com%';  -- Full table scan! ❌
```

### Why It's Bad
```
At scale:
  - 1 million jobs in table
  - Query scans ALL rows (no index on TEXT)
  - Query time: 30 seconds ❌
  - Database CPU: 100% (locks up entire system)
```

### ✅ Correct Approach
```sql
-- Use JSONB with GIN index (PostgreSQL)
CREATE TABLE jobs (
    job_id BIGINT,
    payload JSONB,  -- {"url": "http://example.com", "method": "POST"}
    ...
);

-- Create GIN index for fast JSON queries
CREATE INDEX idx_jobs_payload ON jobs USING GIN(payload);

-- Query by JSON field (uses index!)
SELECT * FROM jobs WHERE payload @> '{"url": "http://example.com"}';
-- Query time: 50 milliseconds ✅
```

**Key Takeaway:** Design for scale from day 1. Indexes, data types, and query patterns matter!

---

## ❌ Mistake 4: Over-Engineering (Premature Optimization)

### The Mistake
```
MVP requirements: 10 jobs/sec

Beginner's design:
  - 10 microservices (API, Scheduler, Worker, Executor, Monitor, etc.)
  - Kubernetes cluster with auto-scaling
  - Kafka with 100 partitions
  - Database sharding across 10 shards
  - Multi-region deployment

Result:
  - 6 months to build MVP
  - Complex to debug
  - Expensive infrastructure ($50K/month for 10 jobs/sec!) 💸
```

### Why It's Bad
```
"Premature optimization is the root of all evil" - Donald Knuth

At 10 jobs/sec:
  - Single PostgreSQL instance can handle 10,000 jobs/sec ✅
  - Single RabbitMQ can handle 50,000 jobs/sec ✅
  - All that complexity is wasted!
```

### ✅ Correct Approach
```
Start simple (MVP):
  - Monolithic API + Scheduler
  - Single RabbitMQ instance
  - Single PostgreSQL master + 1 replica
  - Deploy to 3 EC2 instances
  Cost: $500/month ✅
  Time to build: 2 weeks ✅

Iterate when you hit limits:
  - At 1,000 jobs/sec → Add caching (Redis)
  - At 10,000 jobs/sec → Shard database
  - At 100,000 jobs/sec → Microservices + Kafka

"Make it work, make it right, make it fast" (in that order!)
```

**Key Takeaway:** Start with the simplest architecture that meets requirements. Optimize based on real metrics!

---

## ❌ Mistake 5: Ignoring Database Indexes

### The Mistake
```sql
-- Table without indexes
CREATE TABLE jobs (
    job_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    status VARCHAR(20),
    next_run_time TIMESTAMP
);

-- Scheduler query (runs every 5 seconds)
SELECT * FROM jobs WHERE next_run_time <= NOW() AND status = 'PENDING';
```

### Why It's Bad
```
Without indexes:
  - PostgreSQL scans ENTIRE table (1 million rows)
  - Query time: 5 seconds ❌
  - Scheduler can only poll every 5+ seconds (due to query slowness)
  - Jobs delayed by 5-10 seconds ❌

Database CPU:
  - 100% CPU (constant full table scans)
  - Other queries blocked (API becomes slow)
```

### ✅ Correct Approach
```sql
-- Add composite index
CREATE INDEX idx_jobs_next_run_status ON jobs (next_run_time, status)
  WHERE status = 'PENDING';  -- Partial index (only pending jobs)

-- Now the query is FAST!
SELECT * FROM jobs WHERE next_run_time <= NOW() AND status = 'PENDING';
-- Query time: 10 milliseconds ✅
```

**Performance improvement:**
```
Without index: 5,000 ms (scans 1M rows)
With index: 10 ms (scans 100 rows)
Speedup: 500x faster! 🚀
```

**📝 Beginner Math:**
```
Speedup = Old time / New time
        = 5,000 ms / 10 ms
        = 500x
```

**Key Takeaway:** Always index columns used in WHERE clauses! Use EXPLAIN to verify index usage.

---

## ❌ Mistake 6: Not Handling Worker Failures

### The Mistake
```python
# Worker without error handling
def execute_job(job):
    result = requests.post(job.payload.url)  # What if this times out? ❌
    db.update(f"UPDATE executions SET status='COMPLETED'")
    queue.ack(job)
```

### Why It's Bad
```
Scenario:
  1. Worker starts executing job
  2. HTTP request hangs (server is down)
  3. Worker waits forever (no timeout!) 😱
  4. Worker's thread pool exhausted (all threads stuck)
  5. Worker can't execute any more jobs ❌
  6. Queue builds up → System grinds to a halt
```

### ✅ Correct Approach
```python
def execute_job(job):
    try:
        # Set timeout!
        result = requests.post(
            job.payload.url,
            timeout=30  # Fail after 30 seconds ✅
        )

        if result.status_code == 200:
            db.update(f"UPDATE executions SET status='COMPLETED'")
            queue.ack(job)
        else:
            raise Exception(f"HTTP {result.status_code}")

    except requests.Timeout:
        # Handle timeout
        db.update(f"UPDATE executions SET status='FAILED', error='Timeout'")
        if job.retry_count < job.max_retries:
            queue.nack(job, requeue=True)  # Retry
        else:
            queue.publish_to_dlq(job)  # Dead Letter Queue
            queue.ack(job)

    except Exception as e:
        # Handle all other errors
        db.update(f"UPDATE executions SET status='FAILED', error='{e}'")
        queue.publish_to_dlq(job)
        queue.ack(job)
```

**Key Takeaway:** Always set timeouts, handle errors, and use Dead Letter Queue for failed jobs!

---

## ❌ Mistake 7: Not Monitoring the System

### The Mistake
```
System goes to production without monitoring.

Problem occurs:
  - Queue depth grows to 100,000 jobs 📈
  - No alerts configured 😴
  - Team doesn't notice until users complain (2 hours later!)
  - By then, system is completely overloaded 💥
```

### Why It's Bad
```
"You can't improve what you don't measure"

Without monitoring:
  - Don't know when system is degraded
  - Can't debug issues (no logs)
  - Can't optimize (don't know bottlenecks)
  - Can't meet SLAs (don't track uptime)
```

### ✅ Correct Approach
```
Monitoring stack:
  1. Metrics (Prometheus):
     - Jobs scheduled per second
     - Queue depth
     - Worker health
     - Database CPU/memory
     - API response time

  2. Dashboards (Grafana):
     - Real-time graphs
     - Historical trends
     - Drill-down capabilities

  3. Alerts (AlertManager):
     - Queue depth > 10,000 → Slack alert
     - Job failure rate > 10% → PagerDuty page
     - Scheduler down → Critical alert

  4. Logs (ELK):
     - Centralized logging
     - Searchable (find all logs for job_id=123)
     - Retention (30 days)
```

**Key Takeaway:** Monitoring is not optional! Add metrics, dashboards, and alerts from day 1.

---

## ❌ Mistake 8: Storing Secrets in Plaintext

### The Mistake
```sql
-- Job payload with API key in plaintext
INSERT INTO jobs (payload) VALUES ('{
  "url": "https://api.stripe.com/charge",
  "api_key": "sk_live_51Hxyz..."  ← Stored in database! ❌
}');

-- Now:
- Any developer with database access can see API key
- If database is compromised, all secrets leaked 😱
- Compliance violation (PCI-DSS, SOC 2)
```

### ✅ Correct Approach
```
1. Store secrets in secret management service:
   - AWS Secrets Manager
   - HashiCorp Vault
   - Google Secret Manager

2. Reference secrets in job payload:
   INSERT INTO jobs (payload) VALUES ('{
     "url": "https://api.stripe.com/charge",
     "api_key": "{{secrets/stripe/api_key}}"  ← Reference, not actual key ✅
   }');

3. Worker fetches secret at runtime:
   def execute_job(job):
       # Resolve secret reference
       api_key = vault.get_secret("secrets/stripe/api_key")

       # Use secret
       requests.post(job.url, headers={"Authorization": f"Bearer {api_key}"})

       # Secret never logged or stored permanently! ✅
```

**Key Takeaway:** Never store secrets in plaintext! Use secret management services.

---

## ❌ Mistake 9: Not Planning for Capacity

### The Mistake
```
"Let's launch! We'll figure out scaling later."

Launch day:
  - Expected: 1,000 users
  - Actual: 10,000 users (10x!)
  - System crashes under load 💥
  - Database max connections exceeded
  - Workers overwhelmed
  - Site down for 6 hours 😱
```

### ✅ Correct Approach
```
Before launch, calculate capacity:

Expected load:
  - 10,000 DAU
  - Each user creates 10 jobs/day
  - Total: 100,000 jobs/day = 1.16 jobs/sec average
  - Peak (3x): 3.48 jobs/sec

Capacity needed:
  - Database: Can handle 1,000 writes/sec ✅ (way over-provisioned, good!)
  - Queue: Can handle 50,000 jobs/sec ✅
  - Workers: Need 10 workers (each handles 0.35 jobs/sec)
  - But launch with 50 workers (5x buffer for safety)

Auto-scaling rules:
  - Scale workers if queue depth > 1,000
  - Scale up to 500 workers max
  - Scale down after queue clears
```

**Key Takeaway:** Always do capacity planning! Provision for 3-5x expected load.

---

## ❌ Mistake 10: Ignoring Latency SLAs

### The Mistake
```
Requirement: Jobs execute within ±5 seconds of scheduled time

Design:
  - Scheduler polls database every 60 seconds (to reduce database load)

Problem:
  - Job scheduled for 10:00:00
  - Scheduler polls at 10:00:30
  - Job executes at 10:00:35
  - Latency: 35 seconds ❌ (requirement was ±5 seconds!)
```

### ✅ Correct Approach
```
To meet ±5 second SLA:
  - Scheduler must poll every ≤5 seconds
  - Or: Use event-driven (publish to queue immediately)

Polling approach:
  while True:
      jobs = db.query("... WHERE next_run_time <= NOW()")
      for job in jobs:
          publish_to_queue(job)
      time.sleep(3)  # Poll every 3 seconds (well under 5-second requirement) ✅

Event-driven approach (better):
  - When job is created with schedule="immediate"
  - Directly publish to queue (0-second delay) ✅
```

**Key Takeaway:** Design decisions must be driven by SLAs, not assumptions!

---

## ✅ Best Practices Checklist

### Architecture
- [ ] No single point of failure (HA for all critical components)
- [ ] Distributed locks for preventing duplicate execution
- [ ] Leader election for scheduler failover
- [ ] Database replication (master + replicas)
- [ ] Message queue for decoupling
- [ ] Circuit breakers for failure isolation

### Database
- [ ] Indexes on all columns used in WHERE clauses
- [ ] JSONB for flexible payload (with GIN index)
- [ ] Partitioning for large tables (executions)
- [ ] Connection pooling (avoid max connections error)
- [ ] Regular VACUUM and ANALYZE (PostgreSQL maintenance)

### Scaling
- [ ] Horizontal scaling for workers (auto-scaling)
- [ ] Stateless components (can add/remove servers easily)
- [ ] Caching for frequently accessed data
- [ ] Rate limiting to prevent abuse
- [ ] Quotas per user/team

### Reliability
- [ ] Timeouts on all external calls
- [ ] Retry with exponential backoff
- [ ] Dead Letter Queue for permanently failed jobs
- [ ] Idempotency keys (prevent duplicate charges)
- [ ] Health checks for all components

### Monitoring
- [ ] Metrics (Prometheus): jobs/sec, queue depth, latency
- [ ] Dashboards (Grafana): real-time visibility
- [ ] Alerts (PagerDuty): critical issues paged
- [ ] Logs (ELK): centralized, searchable
- [ ] Tracing (Jaeger): distributed request tracing

### Security
- [ ] Authentication (JWT tokens)
- [ ] Authorization (RBAC - users can only access their own jobs)
- [ ] Secrets management (Vault, AWS Secrets Manager)
- [ ] Encryption in transit (TLS)
- [ ] Encryption at rest (database encryption)
- [ ] Audit logs (who did what when)

### Operations
- [ ] Capacity planning (calculate before launch)
- [ ] Load testing (simulate 10x traffic)
- [ ] Disaster recovery plan (multi-region)
- [ ] Runbooks (what to do when X fails)
- [ ] On-call rotation (24/7 coverage)

---

## 🎓 Interview Tip: Acknowledge Mistakes

When asked about your design in an interview:

**❌ Bad response:**
"My design is perfect, there are no trade-offs."

**✅ Good response:**
"Here are some limitations of my design:
1. Single scheduler is a bottleneck at >10K jobs/sec
2. Polling wastes CPU compared to event-driven
3. At very high scale, we'd need database sharding

But for the requirements (1M jobs/day), this design is sufficient. We can iterate later if we hit these limits."

**Interviewers love candidates who:**
- Acknowledge trade-offs
- Understand limitations
- Plan for iteration
- Balance simplicity vs complexity

