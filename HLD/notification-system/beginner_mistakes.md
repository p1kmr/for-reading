# Common Beginner Mistakes & Solutions

## Mistake 1: Starting with Microservices

### ❌ Wrong Approach
```
Day 1: Let's build 10 microservices!
- Notification Service
- User Service
- Template Service
- Analytics Service
- Rate Limit Service
- ...

Result:
- Over-engineered for MVP
- Complex inter-service communication
- Hard to debug
- Slow development
```

### ✅ Right Approach
```
Day 1: Start with monolith
- Single API service (all endpoints)
- Single database
- Simple deployment

Week 4: Identify bottlenecks
- Analytics queries slow → Separate service

Month 3: Split when necessary
- Each service solves specific problem
- Clear boundaries

Principle: Evolve architecture based on real problems, not imagined ones
```

**Why:**
- Premature optimization is the root of all evil
- Microservices add complexity (network, deployment, monitoring)
- Monoliths are fast to build and easy to refactor

---

## Mistake 2: Not Considering Failures

### ❌ Wrong Approach
```javascript
// Sending notification (no error handling)
async function sendNotification(notification) {
    await sendGridClient.send(notification);
    await db.query('UPDATE notifications SET status=?', ['sent']);
    return 'Success';
}

Problems:
- SendGrid is down → Request fails, user sees error
- Network timeout → Notification lost forever
- Database update fails → Status never updated
```

### ✅ Right Approach
```javascript
async function sendNotification(notification) {
    try {
        // 1. Save to database first (source of truth)
        await db.query('INSERT INTO notifications ...');

        // 2. Try primary provider
        await sendGridClient.send(notification);

    } catch (primaryError) {
        console.error('Primary provider failed:', primaryError);

        try {
            // 3. Fallback to secondary provider
            await mailgunClient.send(notification);

        } catch (secondaryError) {
            // 4. Both failed → Queue for retry
            await kafka.publish('retry-queue', notification);
            await db.query('UPDATE notifications SET status=?', ['retry']);

            // 5. Log for investigation
            logger.error('All providers failed', {
                notification_id: notification.id,
                errors: [primaryError, secondaryError]
            });
        }
    }
}
```

**Best Practices:**
- Assume everything will fail (Murphy's Law)
- Implement retry logic with exponential backoff
- Use Dead Letter Queue for permanent failures
- Log all errors with context
- Monitor failure rates

---

## Mistake 3: Synchronous Processing

### ❌ Wrong Approach
```javascript
app.post('/api/notifications', async (req, res) => {
    // User waits for everything to complete
    await db.insert(notification);  // 50ms
    await sendEmail(notification);  // 500ms ← SLOW!
    await updateAnalytics();  // 100ms

    res.status(200).send('Sent');  // Total: 650ms
});

Problems:
- Slow API response (user waits 650ms)
- If email provider is slow, entire request is slow
- Can't scale (API tied to slow external calls)
```

### ✅ Right Approach
```javascript
app.post('/api/notifications', async (req, res) => {
    // Save to database
    const notification = await db.insert(...);  // 50ms

    // Queue for background processing
    await kafka.publish('email-queue', notification);  // 5ms

    // Respond immediately
    res.status(202).send({
        notification_id: notification.id,
        status: 'queued'
    });

    // Total: 55ms (12x faster!)
});

// Background worker (separate process)
worker.consume('email-queue', async (notification) => {
    await sendEmail(notification);  // Happens in background
    await updateAnalytics();
});
```

**Benefits:**
- Fast API response (55ms vs 650ms)
- Resilient (queue buffers if provider is slow)
- Scalable (add more workers to process faster)

---

## Mistake 4: No Capacity Planning

### ❌ Wrong Approach
```
"We'll just use AWS, it scales automatically!"

Launch day:
- 1 million users try to sign up
- Database crashes (max connections: 100)
- API servers overwhelmed (3 servers × 1000 req/sec = overload)
- Entire system down
```

### ✅ Right Approach
```
Before launch, calculate capacity:

Expected users: 1 million
Peak concurrent: 100,000 (10%)
Notifications/user/day: 5
Total notifications/day: 5 million
Peak RPS: 5M / 86400 × 3 (peak factor) = 174 req/sec

Database:
- Each notification: 2 KB
- Daily storage: 10 GB
- Need: 100 GB for 10 days retention

API Servers:
- Each handles 100 req/sec
- Need: 174 / 100 = 2 servers (provision 5 for buffer)

Workers:
- Each processes 50 notifications/sec
- Need: 174 / 50 = 4 workers (provision 10)

Load test before launch:
- Simulate 200 req/sec for 1 hour
- Monitor latency, errors, resource usage
- Fix bottlenecks before real traffic
```

**Tools for Capacity Planning:**
- Apache JMeter (load testing)
- Locust (Python load testing)
- AWS Auto Scaling (dynamic scaling)

---

## Mistake 5: Storing Everything in One Database Table

### ❌ Wrong Approach
```sql
-- Single "notifications" table (giant table)
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,
    email_subject TEXT,
    email_body TEXT,
    sms_message TEXT,
    push_title TEXT,
    push_body TEXT,
    email_opened_at TIMESTAMP,
    sms_delivered_at TIMESTAMP,
    ...  -- 50+ columns
);

Problems:
- Sparse data (email notification has empty SMS fields)
- Slow queries (scanning 50 columns)
- Hard to add new channels (ALTER TABLE)
- Index bloat (too many indexes)
```

### ✅ Right Approach
```sql
-- Core notification table (lean)
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,
    channel VARCHAR(20),  -- email, sms, push
    template_id VARCHAR(50),
    status VARCHAR(20),
    created_at TIMESTAMP
);

-- Channel-specific tables
CREATE TABLE email_notifications (
    notification_id BIGINT REFERENCES notifications(id),
    subject VARCHAR(500),
    body_html TEXT,
    opened_at TIMESTAMP
);

CREATE TABLE sms_notifications (
    notification_id BIGINT REFERENCES notifications(id),
    message VARCHAR(500),
    delivered_at TIMESTAMP
);

-- Event log (append-only)
CREATE TABLE notification_events (
    id BIGINT PRIMARY KEY,
    notification_id BIGINT,
    event_type VARCHAR(50),  -- sent, delivered, opened
    occurred_at TIMESTAMP
);
```

**Benefits:**
- Lean tables (fast queries)
- Easy to add channels (new table)
- Proper normalization (no sparse data)

---

## Mistake 6: Not Caching User Preferences

### ❌ Wrong Approach
```javascript
// Check user preference on every notification
async function sendNotification(userId, notification) {
    const prefs = await db.query(
        'SELECT * FROM user_preferences WHERE user_id=?',
        [userId]
    );  // Database query every time!

    if (!prefs.email_enabled) {
        return 'User opted out';
    }

    await sendEmail(notification);
}

Problem:
- 600 notifications/sec = 600 database queries/sec
- User preferences rarely change (1x per month)
- Wasting database resources
```

### ✅ Right Approach
```javascript
async function sendNotification(userId, notification) {
    // Check Redis cache first
    const cacheKey = `user_pref:${userId}`;
    let prefs = await redis.get(cacheKey);

    if (!prefs) {
        // Cache miss - query database
        prefs = await db.query(
            'SELECT * FROM user_preferences WHERE user_id=?',
            [userId]
        );

        // Cache for 1 hour
        await redis.setex(cacheKey, 3600, JSON.stringify(prefs));
    } else {
        prefs = JSON.parse(prefs);
    }

    if (!prefs.email_enabled) {
        return 'User opted out';
    }

    await sendEmail(notification);
}

// Invalidate cache on preference update
async function updatePreferences(userId, newPrefs) {
    await db.query('UPDATE user_preferences ...');
    await redis.del(`user_pref:${userId}`);  // Invalidate!
}
```

**Impact:**
```
Cache hit rate: 99%
Database queries: 600 → 6 per second (100x reduction!)
```

---

## Mistake 7: Using Database for Rate Limiting

### ❌ Wrong Approach
```sql
-- Check rate limit with database
SELECT COUNT(*) FROM notifications
WHERE user_id = 123
  AND created_at > NOW() - INTERVAL '1 DAY'
  AND channel = 'email';

-- If count > 10, reject

Problems:
- Slow (50ms database query)
- Race conditions (two requests check simultaneously)
- Database load (600 checks/sec)
```

### ✅ Right Approach
```javascript
// Use Redis atomic increment
async function checkRateLimit(userId, channel) {
    const key = `rate_limit:${userId}:${channel}:${today}`;

    // Atomic increment
    const count = await redis.incr(key);

    // Set expiry on first increment
    if (count === 1) {
        await redis.expire(key, 86400);  // 24 hours
    }

    if (count > 10) {
        throw new Error('Rate limit exceeded');
    }

    return count;
}
```

**Benefits:**
- Fast (1ms vs 50ms)
- Atomic (no race conditions)
- Auto-expiry (key deleted after 24 hours)
- Low database load

---

## Mistake 8: No Monitoring & Alerts

### ❌ Wrong Approach
```
Launch system → Hope it works → Check logs when users complain

Result:
- Provider outage for 2 hours before noticed
- Users angry (no emails received)
- Revenue lost (transactional emails failed)
```

### ✅ Right Approach
```
Monitoring:
1. Application metrics (Prometheus)
   - Request rate, error rate, latency
   - Queue lag, worker throughput

2. Infrastructure metrics (CloudWatch)
   - CPU, memory, disk usage
   - Network throughput

3. Business metrics (Custom dashboards)
   - Notifications sent/hour
   - Delivery rate by channel
   - Cost per notification

Alerts:
- Error rate > 5% for 5 min → Page on-call
- Database replication lag > 10 sec → Warning
- Queue lag > 10,000 messages → Warning
- Provider down → Critical alert

Status Page (for users):
- Show real-time system status
- Update during outages
- Transparency builds trust
```

**Tools:**
- Prometheus + Grafana (metrics & dashboards)
- ELK Stack (logs)
- PagerDuty (alerting)
- StatusPage.io (public status page)

---

## Mistake 9: Ignoring Data Retention & Privacy

### ❌ Wrong Approach
```
Store all notifications forever

Problems:
- GDPR violation (must delete user data on request)
- Database bloat (36 TB/year!)
- Slow queries (scanning billions of rows)
- High storage cost
```

### ✅ Right Approach
```
Data Retention Policy:
- Hot data (last 30 days): Keep in main database (SSD)
- Warm data (30-90 days): Move to archive table (HDD)
- Cold data (90-365 days): Move to S3 (Glacier)
- Older than 1 year: Delete

GDPR Compliance:
- Right to be forgotten: DELETE FROM notifications WHERE user_id=?
- Data export: API to export user's notification history
- Consent: Store user preference for each channel

Automated Cleanup:
-- Monthly job
DELETE FROM notifications
WHERE created_at < NOW() - INTERVAL '1 YEAR';

VACUUM FULL notifications;  -- Reclaim disk space
```

---

## Mistake 10: Not Testing Failure Scenarios

### ❌ Wrong Approach
```
Test only happy path:
✓ Send notification → Works!
✓ Update preferences → Works!

Launch → Provider outage → System crashes
```

### ✅ Right Approach
```
Test failure scenarios (Chaos Engineering):

1. Provider failures
   - Kill SendGrid → Does system switch to backup?
   - Slow response (5 sec timeout) → Does circuit breaker open?

2. Database failures
   - Kill master → Does slave get promoted?
   - Replication lag 60 sec → Does system serve from cache?

3. Network failures
   - Partition network → Does system handle partial failure?
   - Timeout between API and database → Does request retry?

4. Load testing
   - 10x traffic → Does auto-scaling work?
   - Queue 1M messages → Does system drain queue?

Tools:
- Chaos Monkey (random service failures)
- Toxiproxy (network latency/failures)
- JMeter (load testing)
```

**Test Before Production:**
```
Staging environment = Production clone
Run chaos tests weekly
Measure: MTTR (Mean Time To Recovery)
Goal: Recover from any single failure in < 5 minutes
```

---

**💡 Key Lessons:**
1. Start simple, evolve based on real needs
2. Assume everything fails, plan for it
3. Use async processing for slow operations
4. Cache heavily, invalidate carefully
5. Monitor everything, alert proactively
6. Test failures, not just happy path
7. Plan capacity before launch
8. Respect user privacy (GDPR, retention)

**Remember:** Every complex system started simple. Don't over-engineer on day 1!
