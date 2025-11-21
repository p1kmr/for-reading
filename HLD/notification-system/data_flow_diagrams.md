# Data Flow Diagrams

## Flow 1: Send Immediate Notification (Happy Path)

```mermaid
sequenceDiagram
    actor User
    participant Order as Order Service
    participant Gateway as API Gateway
    participant NotifAPI as Notification API
    participant Redis as Redis Cache
    participant DB as PostgreSQL (Master)
    participant Kafka as Apache Kafka
    participant Worker as Email Worker
    participant SendGrid as SendGrid API
    participant EmailUser as User's Email

    Note over User,EmailUser: User places order

    User->>Order: POST /checkout {order_id: 123}
    Order->>Order: Process payment ✓

    Note over Order: Payment successful!<br/>Trigger notification

    Order->>Gateway: POST /api/v1/notifications<br/>{user_id: 456, channel: email,<br/>template: order_confirmation}
    Gateway->>Gateway: Validate API key ✓<br/>Check rate limit ✓

    Gateway->>NotifAPI: Forward request

    NotifAPI->>Redis: GET user_pref:456
    Redis-->>NotifAPI: {email_enabled: true}

    Note over NotifAPI: User allows email ✓

    NotifAPI->>Redis: INCR rate_limit:456:email:2025-01-15
    Redis-->>NotifAPI: 3 (under limit of 10)

    Note over NotifAPI: Rate limit OK ✓

    NotifAPI->>DB: INSERT INTO notifications<br/>(user_id, channel, status='pending')<br/>RETURNING id
    DB-->>NotifAPI: notification_id: 789

    NotifAPI->>Kafka: PUBLISH email-notifications<br/>{notification_id: 789, ...}
    Kafka-->>NotifAPI: Offset: 12345

    NotifAPI-->>Gateway: 202 Accepted<br/>{notification_id: 789, status: 'queued'}
    Gateway-->>Order: 202 Accepted
    Order-->>User: Order Confirmed!

    Note over User: Total time: 50ms ⚡<br/>User gets instant response

    Note over Kafka,Worker: Async processing (background)

    Worker->>Kafka: CONSUME message
    Kafka-->>Worker: {notification_id: 789, user_id: 456, template: order_confirmation}

    Worker->>DB: SELECT email FROM users WHERE id=456
    DB-->>Worker: user@example.com

    Worker->>Redis: GET template:order_confirmation
    alt Template cached
        Redis-->>Worker: {subject: "Order {{order_id}}", body: "..."}
    else Template not cached
        Worker->>DB: SELECT * FROM templates WHERE id=order_confirmation
        DB-->>Worker: Template data
        Worker->>Redis: SET template:order_confirmation
    end

    Worker->>Worker: Render template<br/>"Order ORD-123 confirmed"

    Worker->>SendGrid: POST /v3/mail/send<br/>{to: user@example.com, ...}
    SendGrid-->>Worker: 202 Accepted

    Worker->>DB: UPDATE notifications<br/>SET status='sent', sent_at=NOW()<br/>WHERE id=789
    DB-->>Worker: OK

    Worker->>Kafka: COMMIT offset 12345
    Kafka-->>Worker: OK

    Note over SendGrid,EmailUser: SendGrid delivers email

    SendGrid->>EmailUser: 📧 Your order ORD-123 confirmed
    EmailUser->>EmailUser: Opens email ✓

    EmailUser->>SendGrid: Email opened (tracking pixel)
    SendGrid->>Worker: Webhook: email.opened<br/>{notification_id: 789}
    Worker->>DB: UPDATE notifications<br/>SET opened_at=NOW() WHERE id=789
```

**Timing Breakdown:**
```
0ms    - User submits order
50ms   - API responds (notification queued) ← User sees confirmation
...
2000ms - Email worker processes (background)
2500ms - Email sent to SendGrid
5000ms - User receives email in inbox
```

---

## Flow 2: Send Notification with Retry (Failure Scenario)

```mermaid
sequenceDiagram
    participant Worker as Email Worker
    participant SendGrid as SendGrid API
    participant DB as Database
    participant Kafka as Kafka Queue
    participant DLQ as Dead Letter Queue
    participant Alert as PagerDuty

    Note over Worker: Processing notification_id: 999

    Worker->>SendGrid: POST /v3/mail/send
    SendGrid--XWorker: 500 Internal Server Error

    Note over Worker: Attempt 1 failed!

    Worker->>DB: UPDATE notifications<br/>SET retry_count=1, error='500 error'
    DB-->>Worker: OK

    Worker->>Kafka: PUBLISH email-notifications<br/>{notification_id: 999, retry_count: 1}<br/>Delay: 1 minute
    Kafka-->>Worker: OK

    Note over Kafka: Wait 1 minute...

    Worker->>Kafka: CONSUME (retry attempt 2)
    Kafka-->>Worker: {notification_id: 999, retry_count: 1}

    Worker->>SendGrid: POST /v3/mail/send
    SendGrid--XWorker: 503 Service Unavailable

    Note over Worker: Attempt 2 failed!

    Worker->>DB: UPDATE notifications<br/>SET retry_count=2
    Worker->>Kafka: PUBLISH with retry_count=2<br/>Delay: 5 minutes

    Note over Kafka: Wait 5 minutes...

    Worker->>Kafka: CONSUME (retry attempt 3)
    Worker->>SendGrid: POST /v3/mail/send
    SendGrid-->>Worker: 202 Accepted ✓

    Note over Worker: Success on attempt 3!

    Worker->>DB: UPDATE notifications<br/>SET status='sent', retry_count=3
    Worker->>Kafka: COMMIT offset

    Note over Worker: Alternative: Max retries exceeded

    Worker->>SendGrid: POST /v3/mail/send (attempt 4)
    SendGrid--XWorker: Timeout

    Note over Worker: Attempt 4 failed!<br/>Max retries (3) exceeded<br/>→ Move to DLQ

    Worker->>DLQ: PUBLISH {notification_id: 999,<br/>error: 'Max retries exceeded',<br/>retry_count: 4}
    Worker->>DB: UPDATE notifications<br/>SET status='failed'
    Worker->>Alert: Send alert: High failure rate<br/>for SendGrid (>5%)

    Alert->>Alert: Page on-call engineer
```

---

## Flow 3: Scheduled Notification

```mermaid
sequenceDiagram
    actor User
    participant API as Notification API
    participant DB as Database
    participant Scheduler as Scheduler Worker
    participant Kafka as Kafka Queue
    participant Worker as Email Worker
    participant SendGrid as SendGrid

    Note over User: Friday 5 PM:<br/>Schedule birthday email<br/>for Monday 9 AM

    User->>API: POST /api/v1/notifications/schedule<br/>{user_id: 123, template: birthday,<br/>scheduled_for: "2025-01-20 09:00 PST"}

    API->>API: Validate scheduled_for > NOW() ✓
    API->>DB: INSERT INTO notifications<br/>(user_id, status='scheduled',<br/>scheduled_for='2025-01-20 09:00 PST')
    DB-->>API: notification_id: 555

    API-->>User: 201 Created<br/>{notification_id: 555,<br/>status: 'scheduled'}

    Note over Scheduler: Cron job runs every 1 minute

    loop Every 1 minute
        Scheduler->>DB: SELECT * FROM notifications<br/>WHERE status='scheduled'<br/>AND scheduled_for <= NOW()<br/>LIMIT 1000
        DB-->>Scheduler: [notification_id: 555, ...]

        Note over Scheduler: Monday 9:00 AM:<br/>scheduled_for time reached!

        Scheduler->>Kafka: PUBLISH email-notifications<br/>{notification_id: 555}
        Kafka-->>Scheduler: OK

        Scheduler->>DB: UPDATE notifications<br/>SET status='pending' WHERE id=555
        DB-->>Scheduler: OK
    end

    Note over Worker: Regular processing flow

    Worker->>Kafka: CONSUME message
    Kafka-->>Worker: {notification_id: 555}

    Worker->>Worker: Render template:<br/>"Happy Birthday! Here's 20% off"

    Worker->>SendGrid: POST /v3/mail/send
    SendGrid-->>Worker: 202 Accepted

    Worker->>DB: UPDATE notifications<br/>SET status='sent', sent_at=NOW()
```

**Scheduler Efficiency:**
```
Without batching:
- 1000 scheduled notifications
- 1000 database queries
- 1000 Kafka publishes

With batching:
- 1 database query (LIMIT 1000)
- 1 batch Kafka publish (1000 messages)
- 100x more efficient!
```

---

## Flow 4: Bulk Notification (1M users)

```mermaid
sequenceDiagram
    actor Admin
    participant API as Notification API
    participant DB as Database
    participant Kafka as Kafka (partitioned)
    participant W1 as Worker Pool 1<br/>(10 workers)
    participant W2 as Worker Pool 2<br/>(10 workers)
    participant W3 as Worker Pool 3<br/>(10 workers)
    participant SendGrid as SendGrid

    Admin->>API: POST /api/v1/notifications/bulk<br/>{user_ids: [1...1000000],<br/>template: 'promo_sale'}

    Note over API: Process in batches of 10,000

    loop For each batch of 10,000
        API->>DB: INSERT INTO notifications<br/>(VALUES ...) x10000<br/>(batch insert)
        DB-->>API: IDs: 1-10000

        API->>Kafka: PUBLISH batch to partition 0<br/>(user_ids % 3 == 0)
        API->>Kafka: PUBLISH batch to partition 1<br/>(user_ids % 3 == 1)
        API->>Kafka: PUBLISH batch to partition 2<br/>(user_ids % 3 == 2)
    end

    API-->>Admin: 202 Accepted<br/>{job_id: 'bulk-123',<br/>total: 1000000, status: 'processing'}

    Note over Kafka: Messages distributed across<br/>3 partitions (parallelism)

    par Process Partition 0
        W1->>Kafka: CONSUME partition 0
        Kafka-->>W1: 333,333 messages
        W1->>SendGrid: Send emails (10 workers)
    and Process Partition 1
        W2->>Kafka: CONSUME partition 1
        Kafka-->>W2: 333,333 messages
        W2->>SendGrid: Send emails (10 workers)
    and Process Partition 2
        W3->>Kafka: CONSUME partition 2
        Kafka-->>W3: 333,334 messages
        W3->>SendGrid: Send emails (10 workers)
    end

    Note over W1,W3: Total: 30 workers<br/>Each processes 100 emails/sec<br/>30 × 100 = 3000 emails/sec<br/><br/>1,000,000 / 3000 = 333 seconds<br/>~5.5 minutes total!

    W1->>DB: Batch update statuses<br/>(UPDATE ... WHERE id IN (...))<br/>1000 IDs at a time
    W2->>DB: Batch update statuses
    W3->>DB: Batch update statuses
```

**Performance Calculation:**
```
1 million email notifications

Sequential (single worker):
100 emails/sec × 1 worker = 100 emails/sec
1,000,000 / 100 = 10,000 seconds (2.7 hours)

Parallel (30 workers, 3 Kafka partitions):
100 emails/sec × 30 workers = 3,000 emails/sec
1,000,000 / 3,000 = 333 seconds (5.5 minutes)

54x faster! 🎉
```

---

## Flow 5: User Updates Preferences (Cache Invalidation)

```mermaid
sequenceDiagram
    actor User
    participant API as Preference API
    participant Redis as Redis Cache
    participant DB as PostgreSQL
    participant NotifAPI as Notification API

    User->>API: PUT /api/v1/preferences/123<br/>{email_enabled: false}

    Note over User: User opts out of emails

    API->>DB: BEGIN TRANSACTION
    API->>DB: UPDATE user_preferences<br/>SET email_enabled=false<br/>WHERE user_id=123
    DB-->>API: OK

    API->>Redis: DEL user_pref:123
    Redis-->>API: OK (cache invalidated)

    API->>DB: COMMIT
    DB-->>API: OK

    API-->>User: 200 OK<br/>{email_enabled: false}

    Note over NotifAPI: Later: New notification arrives

    NotifAPI->>Redis: GET user_pref:123
    Redis-->>NotifAPI: null (cache miss)

    NotifAPI->>DB: SELECT * FROM user_preferences<br/>WHERE user_id=123
    DB-->>NotifAPI: {email_enabled: false}

    NotifAPI->>Redis: SET user_pref:123 {...}<br/>TTL: 1 hour
    Redis-->>NotifAPI: OK

    NotifAPI->>NotifAPI: Check preference:<br/>email_enabled = false<br/>→ Skip email notification ✓

    Note over NotifAPI: User's preference respected!<br/>No unwanted email sent
```

---

## Flow 6: Monitoring & Alerting

```mermaid
sequenceDiagram
    participant Worker as Email Worker
    participant Prometheus as Prometheus
    participant Alertmanager as Alertmanager
    participant PagerDuty as PagerDuty
    participant ELK as ELK Stack
    participant Grafana as Grafana

    loop Every 10 seconds
        Worker->>Prometheus: Metrics:<br/>- emails_sent_total: 1000<br/>- email_failures_total: 50<br/>- email_latency_ms: 250
    end

    Prometheus->>Prometheus: Calculate error rate:<br/>50 / 1000 = 5%

    Note over Prometheus: Error rate > 3% threshold!

    Prometheus->>Alertmanager: Alert: HighEmailFailureRate<br/>severity: warning

    Alertmanager->>Alertmanager: Check if alert fired before<br/>(avoid duplicate pages)

    Alertmanager->>PagerDuty: Create incident:<br/>"Email failure rate 5%"

    PagerDuty->>PagerDuty: Page on-call engineer

    Note over Worker: Worker logs error

    Worker->>ELK: Log: ERROR SendGrid 500<br/>{notification_id: 789, user_id: 123}

    Note over Grafana: Engineer opens dashboard

    Grafana->>Prometheus: Query: email_failures_total
    Prometheus-->>Grafana: Timeseries data

    Grafana->>Grafana: Render graph:<br/>Failure spike at 10:30 AM

    Grafana->>ELK: Query: Show errors at 10:30
    ELK-->>Grafana: Logs:<br/>"SendGrid API down"

    Note over Grafana: Engineer diagnoses:<br/>SendGrid outage (not our fault)<br/>Wait for recovery
```

---

## Key Patterns Used

### 1. Asynchronous Processing
- API responds immediately (202 Accepted)
- Actual work done in background by workers
- User doesn't wait for slow operations

### 2. Retry with Exponential Backoff
- Failed operations retried automatically
- Increasing delays (1 min, 5 min, 15 min)
- Dead Letter Queue for permanent failures

### 3. Cache-Aside Pattern
- Check cache first
- On miss, query database and populate cache
- Invalidate cache on updates

### 4. Database Sharding
- Route queries to correct shard by user_id
- Parallel processing across shards

### 5. Consumer Groups (Load Balancing)
- Kafka distributes messages across workers
- Each partition consumed by one worker
- Auto-rebalancing on worker failure

### 6. Circuit Breaker (Resilience)
- Detect provider failures
- Stop sending requests temporarily
- Retry after cooldown period

---

**💡 Beginner Tip:** Data flows show the "story" of your system. Follow a single request from user to database to worker to provider, understanding what happens at each step and why!
