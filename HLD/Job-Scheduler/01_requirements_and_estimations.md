# Job Scheduler System - Requirements & Capacity Estimations

## 📋 Overview
A **Job Scheduler** is a distributed system that manages the execution of tasks at specified times or intervals. Think of it as an "alarm clock" for your software - it wakes up and runs tasks automatically when needed!

**Real-world examples:**
- 🔔 Sending daily email reports at 8 AM
- 💾 Database backups every midnight
- 📊 Generating monthly analytics on the 1st of each month
- 🔄 Syncing data between systems every 5 minutes

**Similar systems:** Cron, Apache Airflow, Kubernetes CronJobs, Quartz Scheduler

---

## 1. Functional Requirements

These are the features our system MUST support:

### 1.1 Core Features (Priority 1 - Must Have)
✅ **FR1: Submit Jobs**
- Users can create one-time jobs (run once at specific time)
- Users can create periodic/recurring jobs (run at intervals: hourly, daily, weekly, monthly)
- Support cron-like syntax: `0 0 * * *` (every midnight)
- Support simple intervals: "every 5 minutes", "every 2 hours"

✅ **FR2: Cancel Jobs**
- Users can cancel scheduled jobs before execution
- Users can stop recurring jobs permanently
- System should handle in-flight jobs gracefully

✅ **FR3: Execute Jobs Across Workers**
- Distribute jobs to multiple worker nodes for parallel execution
- Support different types of jobs (HTTP calls, script execution, database queries)
- Execute jobs at the scheduled time with minimal delay

✅ **FR4: Monitor Job Status**
- Track job lifecycle: `QUEUED → RUNNING → COMPLETED/FAILED`
- Provide real-time status updates
- Store execution history and logs

✅ **FR5: Prevent Duplicate Execution**
- Ensure a job runs only once at the scheduled time
- Handle network partitions and worker failures without duplicating jobs
- Distributed locking to prevent race conditions

### 1.2 Admin Features (Priority 2 - Important)
✅ **FR6: Job Management Dashboard**
- View all jobs (active, completed, failed)
- Filter and search jobs by name, status, schedule
- Pause/Resume jobs without deleting them

✅ **FR7: Retry Failed Jobs**
- Automatic retry with exponential backoff
- Configurable retry policy (max retries, backoff strategy)
- Dead Letter Queue (DLQ) for permanently failed jobs

✅ **FR8: Job Execution Logs**
- Capture stdout/stderr from job execution
- Store logs for debugging and auditing
- Log retention policy (keep logs for 30 days)

### 1.3 Advanced Features (Out of Scope Initially)
⏸️ **FR9: Job Priority Scheduling** (Phase 10)
- High-priority jobs should execute before low-priority jobs
- Priority queue implementation

⏸️ **FR10: Job Dependencies (DAG)** (Phase 10)
- Job B runs only after Job A completes successfully
- Directed Acyclic Graph (DAG) for complex workflows
- Similar to Apache Airflow's DAG concept

---

## 2. Non-Functional Requirements

These define HOW WELL the system should perform:

### 2.1 Scalability 📈
**Requirement:** Handle **1 million jobs per day** initially, scale to **100 million jobs/day**

**What this means:**
- 1M jobs/day = ~12 jobs/second average
- Peak traffic (3x average) = 36 jobs/second
- With 100M jobs/day = 1,157 jobs/second average
- Need horizontal scaling (add more schedulers and workers)

**Why it matters:** As your business grows, more teams want to schedule tasks. Black Friday might have 10x normal traffic!

### 2.2 High Availability (HA) 🔄
**Requirement:** 99.99% uptime = **52.56 minutes downtime per year**

**What this means:**
- No single point of failure (SPOF)
- If scheduler crashes, backup scheduler takes over
- If worker dies, job gets rescheduled to another worker
- Database replication (master-slave setup)

**Downtime comparison (beginner-friendly):**
- 99.9% uptime = 8.76 hours/year offline ❌ Not enough for critical systems
- 99.99% uptime = 52 minutes/year offline ✅ Good for production
- 99.999% uptime = 5 minutes/year offline 🌟 Enterprise-grade (expensive!)

### 2.3 Low Latency ⚡
**Requirement:** Jobs execute within **±5 seconds** of scheduled time

**What this means:**
- If job scheduled at 10:00:00 AM, it should start between 09:59:55 and 10:00:05
- Scheduler checks for pending jobs every 1-5 seconds
- Worker picks up job within 1-2 seconds

**Why it matters:** Sending "Happy Birthday" email at 12:05 PM is less magical than at 12:00 PM sharp!

### 2.4 Consistency 🎯
**Requirement:** **Exactly-once execution** semantics (or at-least-once with idempotency)

**What this means:**
- A job scheduled at 10 AM should run EXACTLY once, not zero times, not twice
- In distributed systems, exactly-once is hard (network failures, crashes)
- Fallback: At-least-once + Idempotent jobs (running twice = same result)

**Example of idempotency:**
- ❌ Non-idempotent: `balance = balance + 100` (running twice adds $200!)
- ✅ Idempotent: `if payment_id not exists: add $100` (running twice = still $100)

### 2.5 Fault Tolerance 🛡️
**Requirement:** Graceful degradation and automatic recovery

**What this means:**
- Worker crashes → Job gets reassigned to healthy worker
- Scheduler crashes → Standby scheduler becomes leader
- Database fails → Failover to replica in <30 seconds
- Network partition → Jobs don't get lost or duplicated

### 2.6 Performance 🚀
**Requirement:** Support **100,000 concurrent workers**

**What this means:**
- At any moment, 100K jobs could be running simultaneously
- Workers should not overwhelm the scheduler
- Efficient job distribution algorithm

### 2.7 Security 🔒
**Requirement:** Secure job execution and access control

**What this means:**
- Authentication: Only authorized users can submit jobs
- Authorization: Users can only cancel their own jobs
- Encryption: Job payloads encrypted in transit (TLS) and at rest (AES-256)
- Audit logs: Track who created/modified/deleted jobs

---

## 3. Capacity Planning & Estimations

> **For Beginners:** Don't panic if you're weak in math! We'll break down every calculation step-by-step with tips.

### 3.1 Traffic Estimates 🚦

#### Assumption 1: Daily Active Jobs
Let's assume:
- **1 million jobs per day** (initial scale)
- **80% are one-time jobs** (800,000 one-time)
- **20% are recurring jobs** (200,000 recurring, but they repeat!)

**📝 Math Tip for Beginners:**
Percentages are your friend!
- 20% of 1 million = 20/100 × 1,000,000 = 200,000
- Or think: 20% = 1/5, so 1M ÷ 5 = 200K ✅

#### Calculation 1: Jobs Scheduled Per Second (Write Requests)

```
Given: 1 million jobs created per day
Seconds in a day = 24 hours × 60 minutes × 60 seconds = 86,400 seconds

Average jobs/second = 1,000,000 / 86,400 = 11.57 jobs/second ≈ 12 jobs/sec
```

**📝 Math Tip:**
Can't remember 86,400? Think:
- 1 day = 24 hours
- 1 hour = 60 × 60 = 3,600 seconds
- 1 day = 24 × 3,600 = 86,400 seconds ✅

**Peak traffic (3x average):**
```
Peak jobs/second = 12 × 3 = 36 jobs/sec
```

**Why 3x?** In real systems, traffic isn't uniform. Morning (9 AM) might have 3x more job submissions than midnight!

#### Calculation 2: Jobs Executed Per Second (Worker Load)

Recurring jobs create more executions!

```
One-time jobs executed/day = 800,000 (execute once)
Recurring jobs:
  - 100,000 run hourly = 100K × 24 = 2,400,000 executions/day
  - 50,000 run every 5 min = 50K × 288 (5-min intervals/day) = 14,400,000 executions/day
  - 50,000 run daily = 50,000 executions/day

Total executions/day = 800K + 2.4M + 14.4M + 50K = 17,650,000 executions/day
```

**📝 Math Tip:**
How many 5-minute intervals in a day?
- 1 day = 24 hours = 24 × 60 = 1,440 minutes
- Intervals = 1,440 / 5 = 288 ✅

```
Average execution rate = 17,650,000 / 86,400 = 204 jobs/sec
Peak execution rate (3x) = 204 × 3 = 612 jobs/sec
```

#### Calculation 3: Read Requests (Status Checks, Dashboard)

Users check job status frequently!

```
Assume:
  - 10,000 active users
  - Each user checks status 50 times/day (refreshing dashboard)

Total reads/day = 10,000 × 50 = 500,000 reads/day
Reads/second = 500,000 / 86,400 = 5.8 reads/sec ≈ 6 reads/sec
Peak reads/sec = 6 × 3 = 18 reads/sec
```

#### Summary Table: Traffic Estimates

| Metric | Average | Peak (3x) |
|--------|---------|-----------|
| Job Submissions (writes) | 12/sec | 36/sec |
| Job Executions (worker load) | 204/sec | 612/sec |
| Status Reads | 6/sec | 18/sec |
| **Total Requests** | **222/sec** | **666/sec** |

**Read:Write Ratio = 6:12 = 1:2** (More writes than reads for a scheduler!)

---

### 3.2 Storage Estimates 💾

#### Calculation 4: Job Metadata Storage

Each job stores:
```
Job metadata:
  - job_id (8 bytes)
  - user_id (8 bytes)
  - job_name (100 bytes)
  - schedule (cron string: 50 bytes)
  - job_type (20 bytes)
  - payload (500 bytes on average - e.g., HTTP endpoint, script path)
  - status (10 bytes: QUEUED, RUNNING, etc.)
  - created_at, updated_at (16 bytes)
  - retry_policy (50 bytes)

Total per job ≈ 762 bytes ≈ 1 KB (rounding up for indexing overhead)
```

**📝 Math Tip for Beginners:**
Remember byte conversions:
- 1 KB = 1,024 bytes ≈ 1,000 bytes (we round for simplicity)
- 1 MB = 1,024 KB ≈ 1 million bytes
- 1 GB = 1,024 MB ≈ 1 billion bytes
- 1 TB = 1,024 GB ≈ 1 trillion bytes

**Storage for 1 year:**
```
Jobs created/day = 1,000,000
Jobs/year = 1M × 365 = 365,000,000 jobs
Storage = 365M jobs × 1 KB = 365,000,000 KB = 365,000 MB = 365 GB
```

**📝 Math Tip:**
Converting KB → GB:
- 365,000,000 KB
- ÷ 1,000 → 365,000 MB
- ÷ 1,000 → 365 GB ✅

**For 5 years:**
```
Storage = 365 GB × 5 = 1,825 GB ≈ 1.8 TB
```

Add 30% buffer for indexes, backups = **1.8 TB × 1.3 ≈ 2.4 TB**

#### Calculation 5: Execution Logs Storage

Each execution log:
```
Execution log:
  - execution_id (8 bytes)
  - job_id (8 bytes)
  - worker_id (8 bytes)
  - status (10 bytes)
  - start_time, end_time (16 bytes)
  - stdout/stderr logs (2 KB average - some jobs print a lot!)

Total per execution ≈ 2.05 KB ≈ 2 KB
```

**Storage for 1 year:**
```
Executions/year = 17,650,000/day × 365 = 6,442,250,000 executions
Storage = 6.44 billion × 2 KB = 12,884,500,000 KB ≈ 12.9 TB
```

**📝 Math Tip:**
Big numbers scary? Break them down:
- 6.44 billion KB
- = 6.44 million MB (divide by 1,000)
- = 6,440 GB (divide by 1,000)
- = 6.44 TB (divide by 1,000) ... wait, I got 12.9 TB above!

Let me recalculate:
- 12,884,500,000 KB
- ÷ 1,000,000 = 12,884.5 GB
- ÷ 1,000 = 12.88 TB ✅

**Retention policy:** Keep logs for 30 days (not 1 year!)
```
Storage for 30 days = 12.9 TB / 12 months = 1.07 TB ≈ 1 TB
```

#### Summary Table: Storage Estimates

| Data Type | Size per Record | Records/Year | Total Storage (1 year) |
|-----------|-----------------|--------------|------------------------|
| Job Metadata | 1 KB | 365 million | 365 GB |
| Execution Logs (30-day retention) | 2 KB | ~530 million/month | 1 TB |
| **Total Database Storage** | - | - | **~1.4 TB** |

Add replication (2 replicas) = **1.4 TB × 3 = 4.2 TB total**

---

### 3.3 Resource Estimates 🖥️

#### Calculation 6: Number of Scheduler Nodes

Schedulers are responsible for:
1. Checking which jobs need to run (scanning database)
2. Pushing jobs to queue

**Scheduler capacity:**
- Assume 1 scheduler can handle 100 job submissions/sec
- Peak load = 36 jobs/sec
- Required schedulers = 36 / 100 = 0.36 ≈ **1 scheduler**

But for **High Availability**, we need at least **3 schedulers** (1 leader + 2 standby)

#### Calculation 7: Number of Worker Nodes

Workers execute jobs. Assume:
- Average job takes 10 seconds to execute
- Each worker has 10 threads (can run 10 jobs concurrently)

**Worker capacity:**
```
Jobs executed/sec (peak) = 612 jobs/sec
If each job takes 10 seconds, then:
  - Jobs in-flight at any moment = 612 jobs/sec × 10 sec = 6,120 concurrent jobs

Workers needed = 6,120 concurrent jobs / 10 threads per worker = 612 workers
```

**📝 Math Tip:**
Why "jobs/sec × duration = concurrent jobs"?
- Think of a water pipe: If 10 liters/sec flow, and it takes 5 seconds to fill a bucket,
  you have 10 × 5 = 50 liters in the pipe at any moment! 🚰

For **High Availability**, add 30% buffer:
```
Workers = 612 × 1.3 = 795 ≈ 800 workers
```

#### Calculation 8: Message Queue Capacity

Queue holds jobs waiting for workers.

**Queue depth:**
```
In worst case (all workers busy), queue can pile up:
  - Jobs arriving/sec = 612
  - If workers are 100% busy for 1 minute, queue grows by:
    Queue depth = 612 jobs/sec × 60 sec = 36,720 jobs

With 1 KB per job message = 36,720 KB ≈ 36 MB (tiny!)
```

Kafka/RabbitMQ can easily handle this.

#### Calculation 9: Cache Size (Redis)

Cache frequently accessed data:
- Active jobs (to check status quickly)
- User sessions (10K users × 5 KB = 50 MB)

Assume:
```
Active jobs in cache = 100,000 jobs × 1 KB = 100 MB
User sessions = 50 MB
Total cache = 150 MB (very small!)
```

Even a small Redis instance (1 GB RAM) is enough.

---

## 4. Assumptions 📝

We made several assumptions. Always state them clearly!

1. **Traffic Pattern:**
   - Uniform distribution throughout the day (in reality, peaks at 9 AM, 12 PM, 6 PM)
   - Peak traffic = 3x average (industry standard)

2. **Job Characteristics:**
   - Average job execution time = 10 seconds
   - 80% one-time, 20% recurring
   - Job payload size = 500 bytes (small HTTP requests, not video encoding!)

3. **Growth:**
   - System will grow from 1M to 100M jobs/day in 3 years
   - Linear growth (in reality, might be exponential!)

4. **User Behavior:**
   - 10,000 active users (admins managing jobs)
   - Each user checks status 50 times/day

5. **Retention:**
   - Job metadata: Keep forever (or 5 years)
   - Execution logs: Keep for 30 days

6. **Reliability:**
   - 99.99% uptime required (not 99.999% - too expensive for MVP)
   - Jobs can tolerate ±5 seconds delay

7. **Geographic Distribution:**
   - Single region initially (US-East)
   - Multi-region expansion in Phase 2

---

## 5. Summary: Scale We're Designing For

| Metric | Value |
|--------|-------|
| **Daily Active Jobs** | 1 million → 100 million |
| **Peak Job Submissions** | 36/sec → 3,600/sec |
| **Peak Job Executions** | 612/sec → 61,200/sec |
| **Concurrent Jobs** | 6,120 → 612,000 |
| **Scheduler Nodes** | 3 (HA) |
| **Worker Nodes** | 800 → 80,000 |
| **Database Storage** | 1.4 TB/year |
| **Uptime SLA** | 99.99% (52 min downtime/year) |
| **Execution Delay** | ±5 seconds |

---

## 6. Next Steps 🚀

Now that we have clear requirements and estimates, we can design the architecture!

**Proceed to:**
- Step 1: Basic Architecture (Client → Scheduler → Worker → Database)
- Step 2: Add Distributed Queue (Kafka for job distribution)
- Step 3: Add Coordination Service (Zookeeper for preventing duplicates)
- ...and so on!

---

**Pro Tip for Interviews:**
Always start with requirements and estimations! Jumping straight to architecture without knowing "how many users" or "how much data" is the #1 beginner mistake. 📊

