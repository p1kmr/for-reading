# Common Beginner Mistakes & Solutions

This document highlights common mistakes beginners make when designing large-scale systems, with specific examples from Uber's architecture.

---

## Mistake 1: Starting with Microservices

### ❌ Wrong Approach:
```
Day 1: Let's build Uber with 50 microservices!
  - User Service
  - Authentication Service
  - Profile Service
  - Notification Service
  - Email Service
  - SMS Service
  - Push Notification Service
  - ... (47 more services)

Result:
  - 6 months to build MVP
  - Complex debugging (distributed tracing needed)
  - Expensive infrastructure ($50K/month for 100 users)
  - Team overhead (managing 50 repos)
```

### ✅ Right Solution:
```
Day 1: Build monolith
  - Single codebase
  - All features in one app
  - Launch in 2 months

Month 6: Identify bottlenecks
  - Payment processing is slow → Extract Payment Service

Month 12: Extract more services as needed
  - Notification Service (high volume)
  - Analytics Service (separate workload)

Result:
  - Fast iteration
  - Lower costs initially
  - Migrate to microservices when actually needed
```

**Key Takeaway:** Start simple, evolve based on real metrics.

---

## Mistake 2: Not Considering Database Bottlenecks

### ❌ Wrong Approach:
```
System Design:
  - 1.32 million location updates/sec
  - Write directly to PostgreSQL
  - No caching

Interview:
Candidate: "We'll use PostgreSQL to store driver locations."
Interviewer: "How many writes per second can PostgreSQL handle?"
Candidate: "Um... a lot?"

Reality: PostgreSQL handles ~10K writes/sec (not 1.32M!)
```

### ✅ Right Solution:
```
1. Do the math:
   - 1.32M writes/sec required
   - PostgreSQL: 10K writes/sec max
   - Gap: Need 130x more capacity!

2. Solution:
   - Write to Redis (handles millions of writes/sec)
   - Batch write to PostgreSQL every 30 sec (for analytics)
   - Reduce DB writes from 1.32M/sec to 166/sec ✅
```

**Key Takeaway:** Always calculate throughput. Don't assume "database can handle it."

---

## Mistake 3: Ignoring Latency Requirements

### ❌ Wrong Approach:
```
API endpoint: GET /rides/789/driver-location

Implementation:
1. Query database for driver location (100ms)
2. Query Google Maps for ETA (200ms)
3. Return response

Total latency: 300ms ❌
Acceptable for user? No! (feels sluggish)
```

### ✅ Right Solution:
```
1. Cache driver location in Redis (5ms)
2. Pre-calculate ETA and cache (5ms)
3. Return response

Total latency: 10ms ✅
Much better user experience!
```

**Key Takeaway:** Know your latency budget. Every network call adds 10-100ms.

---

## Mistake 4: Over-Engineering for Scale You Don't Have Yet

### ❌ Wrong Approach:
```
Startup with 100 users:

"We need to design for 100 million users from day 1!"
  - 20 database shards
  - 10 Kafka brokers
  - Multi-region deployment
  - Complex microservices

Infrastructure cost: $50,000/month
Revenue: $500/month
Result: Company runs out of money before achieving scale
```

### ✅ Right Solution:
```
Day 1 (100 users):
  - Single server ($100/month)
  - SQLite database
  - Deploy in 1 week

1,000 users:
  - Move to PostgreSQL
  - Add Redis cache
  - Cost: $500/month

100,000 users:
  - Add read replicas
  - Horizontal scaling
  - Cost: $5,000/month

10,000,000 users:
  - Geographic sharding
  - CDN
  - Kafka
  - Cost: $100,000/month (but revenue is $10M/month)
```

**Key Takeaway:** Scale architecture as you scale users. Don't prematurely optimize.

---

## Mistake 5: Using Wrong Database for Use Case

### ❌ Wrong Approach:
```
Use Case: E-commerce payments (requires ACID transactions)

Wrong Choice: MongoDB (NoSQL, eventual consistency)

Problem:
BEGIN;
  deduct_from_user_wallet(100);  ← Succeeds
  credit_to_merchant(100);       ← Fails (network issue)
END;

Result: User charged $100, merchant receives $0 💥
MongoDB can't guarantee atomicity across documents!
```

### ✅ Right Solution:
```
Use PostgreSQL (ACID compliant):

BEGIN TRANSACTION;
  UPDATE wallets SET balance = balance - 100 WHERE user_id = 123;
  UPDATE wallets SET balance = balance + 100 WHERE user_id = 456;
COMMIT;

If either fails → entire transaction rolls back
User and merchant balances always consistent ✅
```

**Decision Matrix:**

| Requirement | SQL | NoSQL |
|-------------|-----|-------|
| ACID transactions | ✅ PostgreSQL | ❌ |
| Strong consistency | ✅ PostgreSQL | ❌ MongoDB |
| Complex joins | ✅ PostgreSQL | ❌ |
| Schema flexibility | ❌ | ✅ MongoDB |
| Horizontal scaling (writes) | ⚠️ Harder | ✅ Easy |
| Time-series data | ⚠️ TimescaleDB | ✅ Cassandra |

**Key Takeaway:** Match database to use case. Don't blindly choose NoSQL because it's trendy.

---

## Mistake 6: Not Planning for Failures

### ❌ Wrong Approach:
```
System Design:
  - Single database server
  - No backups
  - No health checks
  - No retry logic

What happens when database crashes?
  - Entire system down
  - Data loss
  - Manual recovery (hours of downtime)
```

### ✅ Right Solution:
```
1. Database replication:
   - Master + 2 replicas
   - Automatic failover (30 sec downtime)

2. Backups:
   - Daily snapshots
   - Point-in-time recovery (RPO: 1 minute)

3. Health checks:
   - Kubernetes liveness probes
   - Auto-restart crashed services

4. Retry logic:
   - Exponential backoff for failed requests
   - Circuit breaker for external services
```

**Key Takeaway:** Plan for failure. Everything fails eventually.

---

## Mistake 7: Synchronous API Calls Everywhere

### ❌ Wrong Approach:
```
User completes ride:

1. Update ride status in DB (100ms)
2. Call payment service (500ms) ← User waits
3. Send email (300ms) ← User waits
4. Send SMS (200ms) ← User waits
5. Update analytics (150ms) ← User waits

Total: 1250ms
User experience: Slow, frustrating
```

### ✅ Right Solution:
```
Synchronous (user waits):
1. Update ride status (100ms)
2. Publish event to Kafka (5ms)
3. Return 200 OK to user

Total: 105ms ✅

Asynchronous (background):
- Payment service processes payment
- Email service sends receipt
- SMS service sends confirmation
- Analytics service updates metrics

User doesn't wait for these!
```

**Key Takeaway:** Don't make users wait for non-critical operations. Use async processing.

---

## Mistake 8: Ignoring Cache Invalidation

### ❌ Wrong Approach:
```
1. Cache user profile (TTL: 24 hours)
2. User updates profile (email change)
3. Database updated ✅
4. Cache NOT updated ❌

Result:
  - User sees old email for 24 hours
  - Confusion, support tickets
```

### ✅ Right Solution:
```javascript
// Option 1: Invalidate on write
async function updateUserProfile(userId, updates) {
  await db.query('UPDATE users SET ... WHERE id = ?', [userId]);

  // Invalidate cache
  await redis.del(`user:${userId}`);

  // Next read will fetch fresh data from DB
}

// Option 2: Write-through cache
async function updateUserProfile(userId, updates) {
  await db.query('UPDATE users SET ... WHERE id = ?', [userId]);

  // Update cache immediately
  const user = await db.query('SELECT * FROM users WHERE id = ?', [userId]);
  await redis.setex(`user:${userId}`, 3600, JSON.stringify(user));
}
```

**Key Takeaway:** Cache invalidation is hard. Always have a strategy (explicit invalidation, TTL, or both).

---

## Mistake 9: Not Considering Geographic Distribution

### ❌ Wrong Approach:
```
System Design:
  - Single datacenter in US
  - Users worldwide (New York, London, Tokyo)

Problem:
  - Tokyo user → US server: 250ms latency
  - User experience: Slow, feels broken
```

### ✅ Right Solution:
```
Multi-region deployment:
  - US East shard (users: New York, Boston)
  - Europe shard (users: London, Paris)
  - Asia shard (users: Tokyo, Singapore)

Result:
  - Tokyo user → Asia shard: 10ms latency ✅
  - 25x faster!
```

**Key Takeaway:** For global apps, data locality matters. Store data close to users.

---

## Mistake 10: No Monitoring or Alerting

### ❌ Wrong Approach:
```
Launch system to production:
  - No metrics collection
  - No dashboards
  - No alerts

What happens:
  - Database CPU at 100% (you don't know)
  - Error rate spiking (you don't know)
  - Users complaining on Twitter (you find out 2 hours later)
```

### ✅ Right Solution:
```
1. Metrics collection (Prometheus):
   - Request latency (P50, P95, P99)
   - Error rate
   - CPU, memory, disk usage

2. Dashboards (Grafana):
   - Real-time visualization
   - Historical trends

3. Alerts (PagerDuty):
   - CPU > 80% for 5 minutes → Alert
   - Error rate > 1% → Page on-call engineer
   - Database down → Critical alert

4. Logging (ELK stack):
   - Centralized logs
   - Searchable, filterable
```

**Key Takeaway:** You can't fix what you can't measure. Monitor everything.

---

## Mistake 11: Premature Optimization

### ❌ Wrong Approach:
```
"I heard Redis is faster than PostgreSQL,
so let's store EVERYTHING in Redis!"

Problem:
  - Redis is in-memory (expensive: $1/GB/month)
  - Not durable (data lost on restart)
  - No complex queries (no SQL)

Use case: Store 5-year ride history (100 TB)
  - Redis: $100,000/month
  - PostgreSQL: $10,000/month
  - Plus Redis loses data on crash!
```

### ✅ Right Solution:
```
Use right tool for the job:
  - Hot data (last 1 hour): Redis
  - Recent data (last 30 days): PostgreSQL (SSD)
  - Old data (> 1 year): S3 Glacier (cold storage)

Cost:
  - Redis (10 GB): $10/month
  - PostgreSQL (1 TB): $1,000/month
  - S3 Glacier (100 TB): $400/month
  - Total: $1,410/month (vs $100K)
```

**Key Takeaway:** Optimize based on real bottlenecks, not assumptions.

---

## Mistake 12: Forgetting About Security

### ❌ Wrong Approach:
```
API design:
  GET /api/rides?user_id=123

Problem:
  - Anyone can change user_id parameter
  - GET /api/rides?user_id=456 → See other user's rides!
  - No authentication required
  - SQL injection vulnerable
```

### ✅ Right Solution:
```javascript
app.get('/api/rides', authenticate, async (req, res) => {
  // Extract user from JWT token (not query param)
  const userId = req.user.id;

  // Parameterized query (prevent SQL injection)
  const rides = await db.query(
    'SELECT * FROM rides WHERE rider_id = ?',
    [userId]
  );

  res.json(rides);
});
```

**Security Checklist:**
- ✅ Authentication (JWT tokens)
- ✅ Authorization (RBAC - only see your own data)
- ✅ HTTPS (TLS encryption)
- ✅ Rate limiting (prevent abuse)
- ✅ Input validation (prevent SQL injection, XSS)
- ✅ Secrets management (don't hardcode passwords)

---

## Mistake 13: Not Estimating Costs

### ❌ Wrong Approach:
```
Interview:
Candidate: "We'll use AWS for everything!"
Interviewer: "What's the estimated cost?"
Candidate: "Um... AWS is cheap, right?"

Reality: Infrastructure for 20M DAU can cost $100K-500K/month
```

### ✅ Right Solution:
```
Back-of-envelope calculation:

Compute (Kubernetes): 100 nodes × $250 = $25,000
Databases: 4 shards × $9K = $36,000
Cache (Redis): 3 clusters × $1K = $3,000
Kafka: 6 brokers × $500 = $3,000
CDN (CloudFlare): $2,000
S3 Storage (100 TB): $2,300
Bandwidth: $8,000
Monitoring: $5,000
External APIs (Maps, SMS): $15,000
Data Warehouse: $10,000

Total: ~$110,000/month

Revenue context: If $1B annual revenue, this is 0.13%
```

**Key Takeaway:** Always estimate costs. Show you think about business viability.

---

## Mistake 14: Underestimating Importance of Data Modeling

### ❌ Wrong Approach:
```sql
-- One giant "rides" table with everything
CREATE TABLE rides (
    ride_id BIGINT,
    rider_name VARCHAR(100),
    rider_email VARCHAR(255),
    rider_phone VARCHAR(20),
    driver_name VARCHAR(100),
    driver_email VARCHAR(255),
    driver_phone VARCHAR(20),
    driver_license VARCHAR(50),
    ...100 more columns
);

Problems:
  - Data duplication (rider info repeated for every ride)
  - Update anomalies (change rider email → update 1000 rides)
  - Slow queries (100 columns × 1B rows)
```

### ✅ Right Solution:
```sql
-- Normalized schema
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20)
);

CREATE TABLE drivers (
    driver_id BIGINT PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id),
    license VARCHAR(50),
    ...
);

CREATE TABLE rides (
    ride_id BIGINT PRIMARY KEY,
    rider_id BIGINT REFERENCES users(user_id),
    driver_id BIGINT REFERENCES drivers(driver_id),
    ...
);

Benefits:
  - No duplication
  - Easy updates
  - Smaller tables → faster queries
```

**Key Takeaway:** Spend time on database design. It's expensive to fix later.

---

## Mistake 15: Not Testing at Scale

### ❌ Wrong Approach:
```
Testing:
  - Unit tests with 10 users
  - Deploy to production with 1M users
  - Hope it works 🤞

Result:
  - System crashes under load
  - Database connection pool exhausted
  - Emergency midnight debugging session
```

### ✅ Right Solution:
```
1. Load testing (before launch):
   - Simulate 1M concurrent users
   - Identify bottlenecks
   - Fix issues before launch

2. Chaos engineering:
   - Randomly kill servers
   - Simulate network failures
   - Verify system recovers gracefully

3. Shadow traffic:
   - Send copy of production traffic to new version
   - Compare results
   - Catch issues before full rollout
```

**Key Takeaway:** Test at scale before you have scale.

---

## Summary: HLD Checklist

Before finalizing your design, ask yourself:

- ✅ Did I start with requirements gathering?
- ✅ Did I calculate throughput (requests/sec, storage, bandwidth)?
- ✅ Did I identify bottlenecks and address them?
- ✅ Did I choose the right database (SQL vs NoSQL)?
- ✅ Did I add caching where appropriate?
- ✅ Did I design for failure (replication, failover)?
- ✅ Did I use async processing for non-critical paths?
- ✅ Did I plan for horizontal scaling?
- ✅ Did I estimate costs?
- ✅ Did I add monitoring and alerting?
- ✅ Did I consider security (auth, encryption, rate limiting)?
- ✅ Did I explain trade-offs for each decision?

**Remember:** There's no perfect architecture. Only trade-offs. Show you understand them!
