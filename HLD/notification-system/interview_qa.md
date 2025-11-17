# Interview Questions & Answers

## System Design Interview Questions

### Q1: How would you design a notification system to handle 10 million users?

**Answer:**

I would design it incrementally, starting simple and adding complexity as needed.

**Step 1: Requirements Gathering**
- Functional: Multi-channel (email, SMS, push), scheduled delivery, rate limiting
- Non-functional: 99.99% uptime, 10M DAU, 50M notifications/day, <1s latency

**Step 2: Capacity Estimation**
```
DAU: 10 million
Notifications/day: 50 million
RPS: 50M / 86400 = 600 req/sec (average), 1800 req/sec (peak)
Storage: 100 GB/day, 36 TB/year
```

**Step 3: High-Level Architecture**
```
Client → API Gateway → Load Balancer → API Servers
    ↓
Redis Cache (user preferences, rate limits)
    ↓
Database (PostgreSQL sharded by user_id)
    ↓
Message Queue (Kafka for async processing)
    ↓
Workers (email, SMS, push)
    ↓
External Providers (SendGrid, Twilio, FCM)
```

**Step 4: Database Design**
- Shard by user_id (3 shards initially, horizontally scalable)
- Master-slave replication for each shard
- Tables: notifications, user_preferences, templates

**Step 5: Key Design Decisions**
- **Async processing:** API queues to Kafka, workers process in background (fast API response)
- **Caching:** Redis for preferences (99% hit rate, reduce DB load by 75%)
- **Retry logic:** Exponential backoff, Dead Letter Queue for permanent failures
- **Rate limiting:** Redis counters, prevent spam

**Trade-offs:**
- Eventual consistency (notifications might appear in history 1-2 seconds after sending)
- Complexity (message queue adds operational overhead)
- Cost (SMS is expensive, prefer push when possible)

---

### Q2: How do you ensure exactly-once delivery of notifications?

**Answer:**

Exactly-once is challenging but achievable with idempotency and transactional processing.

**Approach 1: Idempotency Key**
```javascript
// Client generates unique idempotency key
POST /api/v1/notifications
Headers:
  X-Idempotency-Key: uuid-123-456-789

// Server stores: (idempotency_key → notification_id)
// If same key sent again, return same notification_id (no duplicate)
```

**Approach 2: Kafka Exactly-Once Semantics**
```javascript
const producer = kafka.producer({
    idempotent: true,  // Kafka deduplicates
    transactionalId: 'notification-producer'
});

// Transactional send
await producer.transaction(async (txn) => {
    await txn.send({ topic: 'email-notifications', messages: [...] });
    await db.query('INSERT INTO notifications ...');  // Atomic
});
```

**Approach 3: Database Unique Constraint**
```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    deduplication_key VARCHAR(255) UNIQUE,  -- (user_id + timestamp + template)
    ...
);

-- Insert fails if duplicate key exists
INSERT INTO notifications (user_id, deduplication_key, ...)
VALUES (123, 'user123_template456_20250115_10:30', ...);
```

**Trade-off:** Exactly-once adds latency (transactional processing) but prevents duplicate emails/SMS.

---

### Q3: How would you handle a sudden spike in traffic (10x normal)?

**Answer:**

**Short-term (Immediate):**
1. **Auto-scaling:** Kubernetes HPA scales API servers and workers (2 mins)
2. **Queue buffering:** Kafka holds excess messages temporarily (millions of messages)
3. **Rate limiting:** Protect system from abuse (reject low-priority traffic)

**Medium-term (Hours):**
1. **Database read replicas:** Add more slaves to handle increased read traffic
2. **Cache warming:** Pre-populate Redis with frequently accessed data
3. **Provider limits:** Contact SendGrid/Twilio to raise rate limits

**Long-term (Days/Weeks):**
1. **Add shards:** If database writes are bottleneck, add 4th shard
2. **Multi-region:** Deploy to additional regions for geographic distribution
3. **CDN optimization:** Offload more static content

**Example: Black Friday Spike**
```
Normal: 600 notifications/sec
Black Friday: 6,000 notifications/sec (10x)

Response:
- Auto-scale workers: 10 → 100 workers (handles 10,000/sec)
- Kafka queue buffers: 1M messages (20 min buffer at 6,000/sec)
- Prioritize transactional over promotional (delay promotional by 1 hour)
- Enable rate limiting for bulk sends
```

**Monitoring:**
```
Alert if:
- Queue lag > 100,000 messages
- API latency P99 > 2 seconds
- Error rate > 5%
→ Page on-call engineer
```

---

### Q4: SQL vs NoSQL for this system?

**Answer:**

I would choose **PostgreSQL (SQL)** for the following reasons:

**Why SQL (PostgreSQL):**
```
✓ ACID transactions (notification sent = status updated atomically)
✓ Complex queries (analytics: delivery rate by channel)
✓ Strong consistency (user sees accurate notification history)
✓ Relational data (users → preferences → notifications)
✓ Mature tooling (replication, backup, monitoring)
```

**When NoSQL (Cassandra/MongoDB) would be better:**
```
- Extreme write throughput (>100,000 writes/sec)
- Schema varies widely (email has different fields than SMS)
- Multi-datacenter writes (global distributed system)
- Eventual consistency acceptable (OK if history lags by minutes)
```

**Hybrid Approach:**
```
PostgreSQL: Critical data (notifications, user preferences)
Cassandra: Time-series data (analytics, event logs)
Redis: Cache layer (user preferences, rate limits)
S3: Blob storage (templates, attachments)
```

**Example Schema (PostgreSQL):**
```sql
-- Notifications table (core data)
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(20),
    created_at TIMESTAMP
);

-- Sharding key: user_id (deterministic routing)
SELECT * FROM notifications WHERE user_id = 123;  -- Always hits same shard
```

**Trade-off:** PostgreSQL is harder to scale horizontally than NoSQL, but our design (sharding by user_id) solves this for 100M+ users.

---

### Q5: How do you handle failures in external providers (SendGrid, Twilio)?

**Answer:**

**1. Retry with Exponential Backoff**
```
Attempt 1: Immediate
Attempt 2: 1 min later
Attempt 3: 5 min later
Attempt 4: 15 min later
→ Dead Letter Queue
```

**2. Circuit Breaker**
```javascript
if (sendGridFailureRate > 50%) {
    circuitBreaker.open();  // Stop sending to SendGrid
    useBackupProvider(mailgun);  // Switch to backup
}

// After 60 seconds, try SendGrid again (HALF_OPEN state)
```

**3. Multiple Providers (Failover)**
```
Primary: SendGrid (99.9% uptime)
Backup: Mailgun (99.9% uptime)
Probability both down: 0.001 × 0.001 = 0.000001 (99.9999% uptime)
```

**4. Queue for Later**
```
SendGrid down for 10 minutes
→ Queue all emails in Kafka (durable storage)
→ When SendGrid recovers, drain queue
→ Users receive emails 10 minutes late (acceptable)
```

**5. Monitoring & Alerts**
```
If provider failure rate > 10%:
→ Alert on-call engineer
→ Check provider status page
→ Switch to backup provider
→ Communicate with users (status page update)
```

**Real-world Example:**
```
2023-06-15: SendGrid outage (2 hours)

Our response:
1. Circuit breaker opens after 5 failures (30 seconds)
2. Switch to Mailgun automatically
3. Queue 1M emails in Kafka (durable)
4. SendGrid recovers after 2 hours
5. Drain queue over 30 minutes
6. Zero emails lost ✓
7. Some delayed by 2.5 hours (users notified)
```

---

### Q6: How do you prevent duplicate notifications?

**Answer:**

**1. Idempotency Key (Client-side)**
```http
POST /api/v1/notifications
Headers:
  X-Idempotency-Key: order-12345-confirmation

First request: Creates notification, returns 202
Second request (same key): Returns same notification_id, no duplicate ✓
```

**2. Deduplication in Database**
```sql
CREATE UNIQUE INDEX idx_notifications_dedup
ON notifications (user_id, template_id, deduplication_key);

-- Insert fails if duplicate
INSERT INTO notifications (user_id, template_id, deduplication_key)
VALUES (123, 'order_confirmation', 'order-12345');

-- Second insert with same deduplication_key → Constraint violation
```

**3. Kafka Exactly-Once Semantics**
```javascript
// Producer idempotence
const producer = kafka.producer({ idempotent: true });

// Consumer commits offset only after successful processing
await consumer.run({
    eachMessage: async ({ message }) => {
        await processNotification(message);
        await commitOffset(message.offset);  // Atomic
    }
});
```

**4. Provider-side Deduplication**
```javascript
// SendGrid: Use custom_id for deduplication
await sendGridClient.send({
    to: 'user@example.com',
    custom_args: {
        notification_id: '12345'  // SendGrid deduplicates by this
    }
});
```

**5. Rate Limiting (Prevent Spam)**
```
User gets max 10 emails/day
If code bug tries to send 1000 emails:
→ Rate limiter blocks after 10 ✓
```

---

### Q7: How do you scale to 100 million users?

**Answer:**

**Current Capacity (10M users):**
```
- 3 database shards
- 10 API servers
- 20 workers
Handles: 10M DAU, 50M notifications/day
```

**Scaling to 100M users (10x):**

**Database:**
```
Current: 3 shards (0-33M, 33M-66M, 66M-100M)
Scale to: 10 shards (0-10M, 10M-20M, ..., 90M-100M)

Approach:
1. Add shards incrementally (add shard 3, 4, 5 as needed)
2. Use consistent hashing (minimal data movement)
3. Automate shard routing in application
```

**API Servers:**
```
Current: 10 servers (60 req/sec each)
Scale to: 100 servers (60 req/sec each)

Auto-scaling based on CPU (Kubernetes HPA)
```

**Workers:**
```
Current: 20 workers (500 notifications/sec)
Scale to: 200 workers (5,000 notifications/sec)

Kafka partitions: Increase from 10 to 100
Each worker processes one partition
```

**Kafka:**
```
Current: 3 brokers, 10 partitions
Scale to: 10 brokers, 100 partitions

Replication factor: 3 (durability)
```

**Redis:**
```
Current: 150 GB cache (3 nodes)
Scale to: 1.5 TB cache (30 nodes)

Cluster mode: 10 shards × 3 replicas = 30 nodes
```

**Cost Estimate (100M users):**
```
Servers: $50,000/month
Databases: $20,000/month
Kafka: $10,000/month
Redis: $5,000/month
Providers (email/SMS): $1.5M/month
Total: $1.585M/month
```

**Bottleneck Analysis:**
```
Most expensive: SMS ($1.5M/month)
Optimization: Push 50% of SMS to push notifications
Savings: $750,000/month!
```

---

### Q8: How do you ensure 99.99% uptime?

**Answer:**

99.99% uptime = 52 minutes downtime per year

**Architecture for HA:**
```
1. No single points of failure
   - Multiple API servers (10+)
   - Load balancer with health checks
   - Database: Master-slave with auto-failover
   - Kafka: Replication factor 3

2. Multi-region deployment
   - 3 regions: US-East, EU-West, Asia-Pacific
   - DNS failover (Route53)
   - Cross-region database replication

3. Graceful degradation
   - Provider outage → Switch to backup provider
   - Database slow → Serve from cache
   - Queue full → Reject low-priority traffic (promotional)
```

**Failure Modes & Recovery:**
```
API server crash:
→ Load balancer detects (health check fails)
→ Routes traffic to healthy servers
→ New server auto-starts (Kubernetes)
Downtime: 0 seconds ✓

Database master crash:
→ Slave promoted to master (automatic)
→ App connection string updated
→ Downtime: 20 seconds ✓

Entire region down (datacenter fire):
→ DNS routes to EU-West region
→ Downtime: 5 minutes (DNS propagation)
→ Data loss: 0 (cross-region replication)
```

**Downtime Budget:**
```
99.99% = 52 minutes/year = 4.3 minutes/month

Allocated:
- Planned maintenance: 30 min/year (during low traffic)
- Unplanned outages: 22 min/year (buffer)
```

**Monitoring:**
```
Uptime checks:
- External: Pingdom (every 1 min from 10 locations)
- Internal: Health checks (every 10 sec)

If downtime:
→ Page on-call engineer immediately
→ Auto-failover to backup region
→ Incident post-mortem (5 Whys)
```

---

### Q9: How do you handle scheduled notifications across timezones?

**Answer:**

**Approach 1: Store in User's Timezone**
```sql
-- Store scheduled_for in user's local timezone
INSERT INTO notifications (user_id, scheduled_for, timezone)
VALUES (123, '2025-02-14 09:00:00', 'America/Los_Angeles');

-- Convert to UTC for comparison
SELECT * FROM notifications
WHERE status = 'scheduled'
  AND (scheduled_for AT TIME ZONE timezone) <= NOW()
LIMIT 1000;
```

**Approach 2: Store in UTC, Convert on Delivery**
```sql
-- Store in UTC
INSERT INTO notifications (user_id, scheduled_for)
VALUES (123, '2025-02-14 17:00:00+00');  -- 9 AM PST = 5 PM UTC

-- Scheduler checks every minute
SELECT * FROM notifications
WHERE status = 'scheduled'
  AND scheduled_for <= NOW()
LIMIT 1000;

-- Deliver at correct local time
```

**Scheduler Worker:**
```javascript
// Runs every 1 minute
setInterval(async () => {
    const now = new Date();

    // Fetch notifications due for delivery
    const notifications = await db.query(`
        SELECT * FROM notifications
        WHERE status = 'scheduled'
          AND scheduled_for <= $1
        ORDER BY scheduled_for ASC
        LIMIT 1000
    `, [now]);

    // Publish to Kafka for processing
    for (const notif of notifications) {
        await kafka.publish('email-notifications', notif);
        await db.query(
            'UPDATE notifications SET status=? WHERE id=?',
            ['pending', notif.id]
        );
    }
}, 60 * 1000);  // Every 60 seconds
```

**Edge Cases:**
```
Daylight Saving Time:
- User schedules 9 AM on Nov 1 (DST ends)
- 9 AM occurs twice (2:00 AM → 1:00 AM → 2:00 AM)
- Solution: Store timezone offset at scheduling time

Timezone Change:
- User schedules 9 AM PST, moves to EST
- Preference: Send at 9 AM new timezone (EST)
- Solution: Store timezone preference, recalculate on delivery
```

---

### Q10: How would you add a new notification channel (e.g., WhatsApp)?

**Answer:**

**Step 1: Add Channel to Schema**
```sql
ALTER TABLE notifications
ADD CONSTRAINT check_channel
CHECK (channel IN ('email', 'sms', 'push', 'whatsapp'));  -- Add whatsapp

ALTER TABLE user_preferences
ADD COLUMN whatsapp_enabled BOOLEAN DEFAULT TRUE,
ADD COLUMN whatsapp_transactional BOOLEAN DEFAULT TRUE,
ADD COLUMN whatsapp_promotional BOOLEAN DEFAULT TRUE;
```

**Step 2: Add Kafka Topic**
```
Create topic: whatsapp-notifications
Partitions: 10
Replication: 3
```

**Step 3: Create WhatsApp Worker**
```javascript
// whatsapp-worker.js
const whatsappClient = require('whatsapp-business-api');

consumer.subscribe({ topic: 'whatsapp-notifications' });

await consumer.run({
    eachMessage: async ({ message }) => {
        const notification = JSON.parse(message.value);

        // Send via WhatsApp Business API
        await whatsappClient.sendMessage({
            to: notification.phone_number,
            template: notification.template_id,
            parameters: notification.variables
        });

        // Update status
        await db.query('UPDATE notifications SET status=? WHERE id=?',
            ['sent', notification.id]);
    }
});
```

**Step 4: Update API**
```javascript
// Routing logic
function publishNotification(notification) {
    const topic = `${notification.channel}-notifications`;
    kafka.publish(topic, notification);
}

publishNotification({ channel: 'whatsapp', ... });  // Works automatically
```

**Step 5: Deploy**
```
1. Deploy database schema changes
2. Deploy worker (Kubernetes deployment)
3. Update API with new channel support
4. Test with small user group (A/B test)
5. Roll out to all users
```

**Timeline: 2 weeks**
- Week 1: Development + testing
- Week 2: Gradual rollout (10% → 50% → 100%)

---

**💡 Interview Tips:**
1. Start with requirements (clarify ambiguous points)
2. Estimate capacity (show calculations)
3. Draw architecture (start simple, iterate)
4. Discuss trade-offs (no perfect solution)
5. Address failure scenarios (what if X fails?)
6. Think about scale (how to 10x?)
7. Ask questions (show curiosity)
