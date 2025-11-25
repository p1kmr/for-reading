# Data Flow Diagrams & API Design

## 🎯 Overview

This document provides **detailed sequence diagrams** for all critical workflows and a complete **REST API specification**.

---

## 📊 Sequence Diagram 1: Submit One-Time Job

### User Story
"As a user, I want to submit a job to run immediately"

```mermaid
sequenceDiagram
    actor User
    participant LB as Load Balancer
    participant API as API Server
    participant DB as PostgreSQL
    participant Queue as RabbitMQ
    participant Worker as Worker Node

    User->>LB: POST /api/v1/jobs<br/>{name, type, payload, schedule="immediate"}
    LB->>API: Forward request

    Note over API: 1. Authenticate user (JWT)
    API->>API: Validate JWT token
    Note over API: 2. Validate request
    API->>API: Check required fields<br/>Validate payload format

    API->>DB: BEGIN TRANSACTION
    API->>DB: INSERT INTO jobs<br/>(user_id, job_name, status='QUEUED')
    DB-->>API: job_id=12345

    API->>DB: INSERT INTO executions<br/>(job_id, status='QUEUED')
    DB-->>API: execution_id=98765

    API->>DB: COMMIT

    Note over API: 3. Publish to queue
    API->>Queue: PUBLISH message<br/>{job_id: 12345, type, payload}
    Queue-->>API: ACK

    API-->>User: 201 Created<br/>{job_id: 12345, status: "QUEUED"}

    Note over Queue,Worker: Async execution
    Worker->>Queue: CONSUME message
    Queue-->>Worker: {job_id: 12345, ...}

    Worker->>DB: UPDATE executions<br/>SET status='RUNNING', start_time=NOW()

    Note over Worker: Execute job<br/>(HTTP call, script, etc.)
    Worker->>Worker: result = execute_job()

    alt Job Succeeds
        Worker->>DB: UPDATE executions<br/>SET status='COMPLETED', end_time=NOW()
        Worker->>Queue: ACK message (remove from queue)
    else Job Fails
        Worker->>DB: UPDATE executions<br/>SET status='FAILED', error_message
        alt Retry < Max Retries
            Worker->>Queue: NACK + Requeue with delay
        else Max Retries Exceeded
            Worker->>Queue: Publish to Dead Letter Queue
            Worker->>Queue: ACK original message
        end
    end
```

---

## 📊 Sequence Diagram 2: Submit Recurring Job

### User Story
"As a user, I want to schedule a job to run daily at 9 AM"

```mermaid
sequenceDiagram
    actor User
    participant API as API Server
    participant DB as PostgreSQL
    participant Scheduler as Scheduler (Leader)
    participant ZK as Zookeeper
    participant Queue as RabbitMQ
    participant Worker as Worker

    User->>API: POST /api/v1/jobs<br/>{schedule: "0 9 * * *"}
    API->>DB: INSERT INTO jobs<br/>(schedule, next_run_time='2025-11-18 09:00:00'<br/>status='PENDING')
    DB-->>API: job_id=12345
    API-->>User: 201 Created

    Note over Scheduler: Scheduler polls every 5 seconds

    loop Every 5 seconds
        Scheduler->>DB: SELECT * FROM jobs<br/>WHERE next_run_time <= NOW()<br/>AND status='PENDING'
    end

    Note over Scheduler: At 2025-11-18 09:00:00

    DB-->>Scheduler: [job_id: 12345, ...]

    Note over Scheduler,ZK: Acquire distributed lock
    Scheduler->>ZK: CREATE /locks/job_12345 (ephemeral)
    ZK-->>Scheduler: Lock acquired ✅

    Scheduler->>DB: UPDATE jobs<br/>SET status='QUEUED'<br/>WHERE job_id=12345

    Scheduler->>Queue: PUBLISH {job_id: 12345, ...}
    Queue-->>Scheduler: ACK

    Scheduler->>ZK: DELETE /locks/job_12345
    ZK-->>Scheduler: Lock released

    Note over Scheduler: Calculate next run time
    Scheduler->>DB: UPDATE jobs<br/>SET next_run_time='2025-11-19 09:00:00'<br/>status='PENDING'

    Worker->>Queue: CONSUME message
    Worker->>DB: UPDATE executions<br/>SET status='RUNNING'
    Worker->>Worker: Execute job
    Worker->>DB: UPDATE executions<br/>SET status='COMPLETED'
    Worker->>Queue: ACK
```

---

## 📊 Sequence Diagram 3: Cancel Job

### User Story
"As a user, I want to cancel a scheduled job"

```mermaid
sequenceDiagram
    actor User
    participant API as API Server
    participant DB as PostgreSQL
    participant Queue as RabbitMQ
    participant Audit as Audit Log

    User->>API: DELETE /api/v1/jobs/12345
    API->>API: Authenticate & authorize

    API->>DB: SELECT * FROM jobs<br/>WHERE job_id=12345
    DB-->>API: {job_id, status: 'PENDING', user_id: 789}

    Note over API: Check ownership
    API->>API: if current_user != job.user_id:<br/>  return 403 Forbidden

    API->>DB: BEGIN TRANSACTION

    API->>DB: UPDATE jobs<br/>SET status='CANCELLED', updated_at=NOW()<br/>WHERE job_id=12345
    DB-->>API: 1 row updated

    API->>Audit: INSERT INTO audit_logs<br/>(action='CANCEL_JOB', resource_id=12345)

    API->>DB: COMMIT

    Note over API,Queue: If job was already queued
    alt Job in queue
        API->>Queue: PURGE message with job_id=12345
        Queue-->>API: 1 message purged
    end

    API-->>User: 200 OK<br/>{message: "Job cancelled successfully"}
```

---

## 📊 Sequence Diagram 4: Worker Failure & Job Retry

### Failure Scenario
"Worker crashes while executing a job"

```mermaid
sequenceDiagram
    participant Queue as RabbitMQ
    participant Worker1 as Worker 1
    participant Worker2 as Worker 2
    participant DB as PostgreSQL

    Queue->>Worker1: DELIVER message<br/>{job_id: 12345}<br/>(not ACKed yet)

    Worker1->>DB: UPDATE executions<br/>SET status='RUNNING'

    Worker1->>Worker1: Execute job...

    Note over Worker1: 💥 Worker crashes!<br/>No ACK sent to queue

    Note over Queue: After 5-minute timeout<br/>(no ACK received)

    Queue->>Queue: Message not ACKed<br/>→ Mark as "unacknowledged"

    Queue->>Queue: Requeue message

    Note over Queue,Worker2: Automatic retry

    Queue->>Worker2: REDELIVER message<br/>{job_id: 12345, retry_count: 1}

    Worker2->>DB: SELECT * FROM executions<br/>WHERE job_id=12345
    DB-->>Worker2: {status: 'RUNNING'} (from Worker1)

    Worker2->>DB: UPDATE executions<br/>SET status='RUNNING', worker_id='worker-2'<br/>start_time=NOW()

    Worker2->>Worker2: Execute job successfully ✅

    Worker2->>DB: UPDATE executions<br/>SET status='COMPLETED'

    Worker2->>Queue: ACK message
    Queue-->>Worker2: Message removed from queue
```

---

## 📊 Sequence Diagram 5: Scheduler Failover

### Failure Scenario
"Scheduler leader crashes, standby takes over"

```mermaid
sequenceDiagram
    participant ZK as Zookeeper
    participant Sched1 as Scheduler 1 (Leader)
    participant Sched2 as Scheduler 2 (Standby)
    participant Queue as RabbitMQ

    Note over Sched1: Scheduler 1 is active leader

    loop Every 1 second
        Sched1->>ZK: Send heartbeat
        ZK-->>Sched1: ACK
    end

    loop Scheduler 2 watches leader
        Sched2->>ZK: Watch /election/scheduler_0000000001
        ZK-->>Sched2: Leader is alive
    end

    Note over Sched1: 💥 Scheduler 1 crashes!

    Note over ZK: Heartbeat timeout (5 seconds)

    ZK->>ZK: Delete /election/scheduler_0000000001<br/>(ephemeral node)

    ZK->>Sched2: Event: Leader node deleted

    Note over Sched2: Detect leader failure

    Sched2->>ZK: GET /election children
    ZK-->>Sched2: [scheduler_0000000002, scheduler_0000000003]

    Sched2->>Sched2: I have lowest sequence<br/>→ I am new leader!

    Note over Sched2: Promote to leader 🏆

    Sched2->>ZK: Create /leader_lock (ephemeral)
    Sched2->>Queue: Resume job scheduling

    Note over Sched2: Total failover time: ~5 seconds ✅
```

---

## 🌐 REST API Design

### Base URL
```
https://scheduler.example.com/api/v1
```

### Authentication
```
All requests require JWT token in Authorization header:
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

### API Endpoints

#### 1. Create Job

```http
POST /api/v1/jobs
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "daily_sales_report",
  "description": "Generate sales report and email to management",
  "type": "http_request",
  "schedule": "0 9 * * *",  // Cron syntax OR "immediate"
  "timezone": "America/New_York",
  "payload": {
    "url": "https://api.example.com/reports/generate",
    "method": "POST",
    "headers": {
      "X-API-Key": "secret123"
    },
    "body": {
      "report_type": "sales",
      "format": "pdf"
    }
  },
  "retry_policy": {
    "max_retries": 3,
    "backoff_factor": 3
  },
  "timeout_seconds": 300,
  "priority": 1  // 0=low, 1=medium, 2=high (future feature)
}
```

**Response (201 Created):**
```json
{
  "job_id": 12345,
  "name": "daily_sales_report",
  "status": "PENDING",
  "next_run_time": "2025-11-18T09:00:00Z",
  "created_at": "2025-11-17T14:30:00Z",
  "created_by": 789
}
```

**Error Responses:**
```json
// 400 Bad Request - Invalid cron syntax
{
  "error": "INVALID_SCHEDULE",
  "message": "Invalid cron syntax: '0 9 * *'"
}

// 401 Unauthorized - Missing/invalid token
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or expired JWT token"
}

// 429 Too Many Requests - Rate limit exceeded
{
  "error": "RATE_LIMIT_EXCEEDED",
  "message": "You have exceeded your quota (100 jobs/day for free tier)",
  "retry_after": 3600  // seconds
}
```

---

#### 2. Get Job Details

```http
GET /api/v1/jobs/{job_id}
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "job_id": 12345,
  "name": "daily_sales_report",
  "description": "Generate sales report",
  "type": "http_request",
  "schedule": "0 9 * * *",
  "timezone": "America/New_York",
  "status": "PENDING",
  "next_run_time": "2025-11-18T09:00:00Z",
  "last_run_time": "2025-11-17T09:00:00Z",
  "created_at": "2025-11-01T10:00:00Z",
  "updated_at": "2025-11-17T09:00:05Z",
  "execution_count": 17,
  "success_count": 16,
  "failure_count": 1,
  "avg_execution_time_ms": 4500
}
```

---

#### 3. List Jobs (with filtering & pagination)

```http
GET /api/v1/jobs?status=PENDING&limit=20&offset=0
Authorization: Bearer {token}
```

**Query Parameters:**
- `status`: Filter by status (PENDING, RUNNING, COMPLETED, FAILED, CANCELLED)
- `type`: Filter by job type (http_request, script, sql_query)
- `limit`: Results per page (default: 20, max: 100)
- `offset`: Pagination offset (default: 0)
- `sort`: Sort field (created_at, next_run_time, name) (default: created_at)
- `order`: Sort order (asc, desc) (default: desc)

**Response (200 OK):**
```json
{
  "jobs": [
    {
      "job_id": 12345,
      "name": "daily_sales_report",
      "status": "PENDING",
      "next_run_time": "2025-11-18T09:00:00Z"
    },
    ...
  ],
  "total": 150,
  "limit": 20,
  "offset": 0,
  "has_more": true
}
```

---

#### 4. Update Job

```http
PUT /api/v1/jobs/{job_id}
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "daily_sales_report_v2",
  "schedule": "0 10 * * *",  // Changed from 9 AM to 10 AM
  "payload": {
    "url": "https://api.example.com/reports/generate_v2"
  }
}
```

**Response (200 OK):**
```json
{
  "job_id": 12345,
  "message": "Job updated successfully",
  "next_run_time": "2025-11-18T10:00:00Z"
}
```

---

#### 5. Delete/Cancel Job

```http
DELETE /api/v1/jobs/{job_id}
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "message": "Job cancelled successfully"
}
```

---

#### 6. Pause Job

```http
POST /api/v1/jobs/{job_id}/pause
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "job_id": 12345,
  "status": "PAUSED",
  "message": "Job paused. It will not be executed until resumed."
}
```

---

#### 7. Resume Job

```http
POST /api/v1/jobs/{job_id}/resume
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "job_id": 12345,
  "status": "PENDING",
  "next_run_time": "2025-11-18T09:00:00Z",
  "message": "Job resumed"
}
```

---

#### 8. Get Job Execution History

```http
GET /api/v1/jobs/{job_id}/executions?limit=10&offset=0
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "executions": [
    {
      "execution_id": 98765,
      "status": "COMPLETED",
      "worker_id": "worker-node-2",
      "start_time": "2025-11-17T09:00:02Z",
      "end_time": "2025-11-17T09:00:08Z",
      "duration_ms": 6000,
      "exit_code": 0,
      "error_message": null
    },
    {
      "execution_id": 98764,
      "status": "FAILED",
      "worker_id": "worker-node-1",
      "start_time": "2025-11-16T09:00:01Z",
      "end_time": "2025-11-16T09:00:35Z",
      "duration_ms": 34000,
      "exit_code": 1,
      "error_message": "Connection timeout to https://api.example.com"
    }
  ],
  "total": 17,
  "limit": 10,
  "offset": 0
}
```

---

#### 9. Get Execution Logs

```http
GET /api/v1/executions/{execution_id}/logs
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "execution_id": 98765,
  "logs": "Starting job execution...\nFetching data from API...\nData received: 1,234 records\nGenerating report...\nReport generated successfully!\nSent email to recipients\n",
  "size_bytes": 187
}
```

---

#### 10. Retry Failed Job Manually

```http
POST /api/v1/jobs/{job_id}/retry
Authorization: Bearer {token}
```

**Response (202 Accepted):**
```json
{
  "job_id": 12345,
  "execution_id": 98770,
  "status": "QUEUED",
  "message": "Job queued for retry"
}
```

---

## 🔒 Rate Limiting

### Per-User Rate Limits

```
Rate limit headers (returned in every response):

X-RateLimit-Limit: 1000       // Requests allowed per hour
X-RateLimit-Remaining: 847    // Requests remaining
X-RateLimit-Reset: 1700236800 // Unix timestamp when limit resets
```

**Rate limit tiers:**
```
Free tier:      100 jobs/day,    1000 API requests/hour
Pro tier:       10,000 jobs/day, 10,000 API requests/hour
Enterprise:     Unlimited
```

**Rate limit exceeded (429):**
```json
{
  "error": "RATE_LIMIT_EXCEEDED",
  "message": "Rate limit exceeded. Please try again in 15 minutes.",
  "retry_after": 900  // seconds
}
```

---

## 🔐 Authentication & Authorization

### JWT Token Structure

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "user_id": 789,
    "email": "user@example.com",
    "role": "admin",  // "user", "admin", "enterprise"
    "tier": "pro",    // "free", "pro", "enterprise"
    "iat": 1700150400,  // Issued at
    "exp": 1700236800   // Expires at (24 hours)
  },
  "signature": "..."
}
```

### Authorization Rules

```
Operation             | Required Role
----------------------|---------------
Create job            | Any authenticated user
Get own job           | Job owner
Update own job        | Job owner
Delete own job        | Job owner
Get all jobs          | Admin only
Delete any job        | Admin only
View system metrics   | Admin only
```

---

## ✅ Summary

✅ 5 detailed sequence diagrams (submit, recurring, cancel, failure, failover)
✅ Complete REST API specification (10 endpoints)
✅ Request/response examples for all endpoints
✅ Error handling & status codes
✅ Rate limiting strategy
✅ Authentication & authorization model
✅ Pagination & filtering support

