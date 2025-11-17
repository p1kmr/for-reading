# Interview Questions & Answers - Uber HLD

This document contains 20 common system design interview questions with detailed answers based on Uber's architecture.

---

## 1. How would you design a ride-hailing platform like Uber?

**Answer:**

Start by gathering requirements, then design incrementally:

**Step 1: Requirements**
- Functional: Ride matching, real-time tracking, payments, ratings
- Non-functional: 20M DAU, < 200ms latency, 99.99% availability

**Step 2: Basic Architecture**
- Clients → Load Balancer → App Servers → PostgreSQL
- Identifies bottleneck: Database can't handle 1.32M location updates/sec

**Step 3: Add Redis Cache**
- Store driver locations in Redis (10ms vs 100ms DB)
- Reduces DB load by 99%

**Step 4: Database Sharding**
- Shard by geography (US, Europe, Asia)
- Master-slave replication for high availability

**Step 5: Event-Driven (Kafka)**
- Async notifications, payment processing
- Decouple services for better scalability

**Step 6: CDN + S3**
- Map tiles via CDN (70% cost savings)
- Driver photos in S3 (not DB)

---

## 2. How do you handle 1.32 million location updates per second?

**Answer:**

**Problem:** Database can't handle 1.32M writes/sec.

**Solution:**

1. **Write to Redis** (2ms latency):
   ```
   SET driver:456:location {lat, lng, timestamp} EX 10
   ```
   - Redis can handle millions of writes/sec
   - TTL of 10 seconds (auto-expire if driver goes offline)

2. **Batch Write to DB** (for analytics):
   ```
   Every 30 seconds, bulk insert recent locations to PostgreSQL
   ```
   - Reduces DB writes from 1.32M/sec to 166/sec (99.99% reduction!)

3. **Use Redis Geo for Queries**:
   ```
   GEORADIUS drivers:available -122.4194 37.7749 5 km
   ```
   - Find nearby drivers in 3-5ms

**Key Insight:** Separate hot path (real-time) from cold path (analytics). Redis for hot, PostgreSQL for cold.

---

## 3. How do you match riders with the nearest available driver?

**Answer:**

**Algorithm:**

```javascript
1. Get rider location: (lat: 37.7749, lng: -122.4194)

2. Query Redis Geo for available drivers within 5 km:
   GEORADIUS drivers:available -122.4194 37.7749 5 km WITHDIST

3. Returns 10 drivers with distances:
   [
     {driver_id: 456, distance: 1.2 km, rating: 4.9},
     {driver_id: 789, distance: 2.5 km, rating: 4.7},
     ...
   ]

4. Scoring function:
   score = (1 / distance) × rating × availability_bonus

   driver_456: (1/1.2) × 4.9 × 1.0 = 4.08
   driver_789: (1/2.5) × 4.7 × 1.0 = 1.88

5. Select highest score → driver_456

6. Atomically assign via database transaction:
   BEGIN;
   UPDATE rides SET driver_id=456, status='accepted';
   UPDATE drivers SET status='on_ride';
   COMMIT;
```

**Time Complexity:** O(log n) for Redis Geo query (n = drivers in area)
**Total Latency:** < 100ms

---

## 4. How do you calculate surge pricing in real-time?

**Answer:**

**Formula:** `surge = min(demand / supply, 3.0)` (capped at 3x)

**Implementation with Kafka Streams:**

```javascript
1. Stream ride requests into Kafka topic:
   Topic: ride.requested
   Event: {zone_id: "sf_downtown", timestamp: ...}

2. Stream driver status updates:
   Topic: driver.status_updated
   Event: {zone_id: "sf_downtown", status: "available"}

3. Kafka Streams aggregation (1-minute tumbling window):
   - Count requests per zone: 50 requests
   - Count available drivers per zone: 20 drivers

4. Calculate surge:
   surge = min(50 / 20, 3.0) = 2.5x

5. Cache result in Redis (2-minute TTL):
   SET surge:zone:sf_downtown {multiplier: 2.5} EX 120

6. Fare calculation uses cached surge:
   final_fare = (base + distance_cost) × 2.5
```

**Why Kafka Streams?**
- Real-time aggregation (sub-second latency)
- Exactly-once semantics (no duplicate counting)
- Scalable (add more stream processors)

---

## 5. How do you ensure high availability (99.99% uptime)?

**Answer:**

**99.99% = 52 minutes downtime per year**

**Strategies:**

1. **No Single Point of Failure:**
   - Load balancer: Multiple instances (HAProxy cluster)
   - App servers: 50+ instances (one fails, others continue)
   - Database: Master + 2 replicas (automatic failover)
   - Redis: 3-node cluster (replica promotion)

2. **Automatic Failover:**
   ```
   DB master crashes:
     - Health check fails (10 sec)
     - Promote replica to master (20 sec)
     - Update DNS (10 sec)
     - Total downtime: 40 seconds ✅
   ```

3. **Multi-Region Deployment:**
   ```
   Primary: US East
   Secondary: US West (hot standby)

   If US East datacenter down:
     - DNS failover to US West (30 sec)
     - RTO: 30 seconds
     - RPO: < 1 minute (continuous backup)
   ```

4. **Circuit Breakers:**
   - If payment service down, fast-fail (don't retry forever)
   - Graceful degradation (ride booking still works, payment queued)

5. **Health Checks:**
   - Kubernetes liveness probes (auto-restart crashed pods)
   - Load balancer health checks (remove unhealthy servers)

---

## 6. How would you scale the database to handle 40K writes/sec?

**Answer:**

**Problem:** Single PostgreSQL master handles max 10K writes/sec.

**Solution: Geographic Sharding**

```
Shard 1 (US East): 10K writes/sec
Shard 2 (US West): 10K writes/sec
Shard 3 (Europe): 10K writes/sec
Shard 4 (Asia): 10K writes/sec

Total capacity: 40K writes/sec ✅
```

**Sharding Strategy:**

```sql
Shard key: city_id

User in New York (city_id: 101) → US East shard
User in London (city_id: 201) → Europe shard

Routing logic:
function getShardForUser(cityId) {
  if (cityId >= 100 && cityId < 200) return US_EAST_SHARD;
  if (cityId >= 200 && cityId < 300) return EUROPE_SHARD;
  ...
}
```

**Benefits:**
- Linear scaling (add more shards as needed)
- Low latency (data stored close to users)
- Fault isolation (US shard down doesn't affect Europe)

**Trade-offs:**
- Cross-shard queries are complex
- Operational overhead (manage multiple DB clusters)

---

## 7. How do you handle a sudden 10x traffic spike (e.g., New Year's Eve)?

**Answer:**

**Preparation (Before Spike):**

1. **Load Testing:**
   ```
   Use tools like Apache JMeter, Gatling
   Simulate 2M concurrent users (10x normal)
   Identify bottlenecks (CPU, memory, database connections)
   ```

2. **Pre-Scaling:**
   ```
   Normal: 50 app servers
   Pre-scale to: 200 app servers (2 hours before midnight)
   Auto-scaling enabled (can go up to 500 servers)
   ```

3. **Cache Warming:**
   ```
   Pre-load frequently accessed data into Redis:
   - Popular pickup locations
   - Driver availability by zone
   - Surge pricing estimates
   ```

**During Spike:**

4. **Auto-Scaling (Kubernetes HPA):**
   ```
   CPU > 70% → Add more pods
   Scales from 200 → 500 servers in 5 minutes
   ```

5. **Rate Limiting:**
   ```
   Aggressive rate limiting:
   Normal: 100 requests/min per user
   Spike: 20 requests/min per user (prevent abuse)
   ```

6. **Degrade Gracefully:**
   ```
   Disable non-critical features:
   - Ride history (can check later)
   - Promotional banners
   - ML recommendations (use simple distance-based matching)

   Keep critical features:
   - Ride booking
   - Real-time tracking
   - Payments
   ```

**After Spike:**

7. **Scale Down:**
   ```
   CPU < 50% for 10 minutes → Remove pods
   Gradually scale back to 50 servers (save costs)
   ```

---

## 8. How do you prevent a single slow database query from taking down the entire system?

**Answer:**

**Problem:** Slow query (10 seconds) holds database connection → Connection pool exhausted → System down

**Solutions:**

1. **Query Timeout:**
   ```javascript
   const result = await db.query(
     'SELECT * FROM rides WHERE rider_id = ?',
     [riderId],
     { timeout: 5000 }  // Kill query after 5 seconds
   );
   ```

2. **Connection Pool Management:**
   ```javascript
   const pool = new Pool({
     max: 100,  // Max 100 connections
     connectionTimeoutMillis: 2000,  // Wait max 2 sec for connection
     idleTimeoutMillis: 30000  // Release idle connections
   });
   ```

3. **Circuit Breaker:**
   ```javascript
   if (dbErrorRate > 50%) {
     openCircuit();  // Stop sending requests to DB
     returnCachedData();  // Serve from cache
   }
   ```

4. **Read Replicas:**
   ```
   Slow analytical query → Route to read replica
   Fast transactional query → Route to master

   Prevents analytical queries from blocking writes
   ```

5. **Query Monitoring:**
   ```sql
   -- Identify slow queries
   SELECT pid, query, now() - query_start AS duration
   FROM pg_stat_activity
   WHERE state = 'active' AND now() - query_start > INTERVAL '1 second';

   -- Auto-kill queries > 10 seconds
   SELECT pg_terminate_backend(pid);
   ```

6. **Database Indexes:**
   ```sql
   -- Without index: Full table scan (1M rows, 1000ms)
   SELECT * FROM rides WHERE rider_id = 12345;

   -- With index: B-tree lookup (5ms)
   CREATE INDEX idx_rides_rider ON rides (rider_id);
   ```

---

## 9. What database would you use: SQL or NoSQL? Why?

**Answer:**

**Choose SQL (PostgreSQL) for Uber. Here's why:**

**Uber's Requirements:**

| Requirement | SQL | NoSQL |
|-------------|-----|-------|
| ACID transactions (payments) | ✅ Yes | ❌ Eventual consistency |
| Complex queries (joins) | ✅ Yes | ❌ No joins |
| Geospatial queries (find nearby drivers) | ✅ PostGIS | ⚠️ Limited support |
| Strong consistency (ride assignment) | ✅ Yes | ❌ Eventual |
| Schema changes | ⚠️ Migrations needed | ✅ Schema-less |

**Critical Use Case: Payment**
```sql
BEGIN TRANSACTION;
  -- Deduct from rider
  UPDATE users SET wallet_balance = wallet_balance - 50.25 WHERE id = 12345;

  -- Credit to driver
  UPDATE drivers SET earnings = earnings + 40.20 WHERE id = 456;

  -- Record payment
  INSERT INTO payments (ride_id, amount, status) VALUES (789, 50.25, 'succeeded');
COMMIT;

-- All or nothing (ACID)
```

**NoSQL can't guarantee this atomically across documents!**

**When to Use NoSQL:**
- Driver locations (time-series data) → Could use Cassandra
- Analytics events → Could use MongoDB
- But for simplicity, PostgreSQL works for everything

**Hybrid Approach (Best of Both):**
- PostgreSQL: Core data (users, rides, payments)
- Redis: Real-time cache (driver locations, sessions)
- Cassandra: Optional for high-write analytics (location history)

---

## 10. How do you design APIs for backward compatibility?

**Answer:**

**Problem:** New app version released, but old app users still on v1 API.

**Strategy:**

1. **Versioning in URL:**
   ```
   /api/v1/rides (old version)
   /api/v2/rides (new version with breaking changes)
   ```

2. **Support Multiple Versions:**
   ```javascript
   // v1: Returns ride with basic info
   app.get('/api/v1/rides/:id', async (req, res) => {
     const ride = await getRide(req.params.id);
     res.json({
       ride_id: ride.id,
       status: ride.status,
       fare: ride.fare
     });
   });

   // v2: Returns ride with additional fields
   app.get('/api/v2/rides/:id', async (req, res) => {
     const ride = await getRide(req.params.id);
     res.json({
       ride_id: ride.id,
       status: ride.status,
       fare: ride.fare,
       driver: ride.driver,  // NEW
       route: ride.route      // NEW
     });
   });
   ```

3. **Deprecation Policy:**
   ```
   v2 released Jan 2025
     ↓
   v1 marked deprecated Jul 2025 (6 months notice)
     ↓
   v1 disabled Jan 2026 (12 months support)
   ```

4. **Backward-Compatible Changes (Don't Require New Version):**
   ```
   ✅ Add new optional field (doesn't break old clients)
   ✅ Add new endpoint
   ✅ Change internal implementation (same API)

   ❌ Remove field (breaks old clients)
   ❌ Rename field (breaks old clients)
   ❌ Change response format (breaks old clients)
   ```

5. **Example: Adding Driver Rating Field**
   ```javascript
   // v1: Doesn't include driver rating
   {
     "driver_id": 456,
     "name": "Jane Smith"
   }

   // v2: Adds optional rating field (backward compatible!)
   {
     "driver_id": 456,
     "name": "Jane Smith",
     "rating": 4.9  // NEW, but v1 clients ignore it
   }

   // No need for v3!
   ```

---

## 11. How would you implement a referral/promo code system?

**Answer:**

**Requirements:**
- User enters promo code → Gets discount
- Referrer gets credit when referee completes first ride
- Prevent fraud (same user, multiple accounts)

**Database Schema:**
```sql
CREATE TABLE promo_codes (
    code VARCHAR(20) PRIMARY KEY,
    type VARCHAR(20),  -- 'referral', 'marketing', 'event'
    discount_type VARCHAR(20),  -- 'percentage', 'fixed_amount'
    discount_value DECIMAL(10,2),
    max_uses INT,
    current_uses INT DEFAULT 0,
    referrer_id BIGINT,  -- User who created referral code
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE promo_redemptions (
    id BIGSERIAL PRIMARY KEY,
    promo_code VARCHAR(20) REFERENCES promo_codes(code),
    user_id BIGINT REFERENCES users(user_id),
    ride_id BIGINT REFERENCES rides(ride_id),
    discount_applied DECIMAL(10,2),
    redeemed_at TIMESTAMP DEFAULT NOW(),

    UNIQUE(promo_code, user_id)  -- User can only use code once
);
```

**API Flow:**
```javascript
// Apply promo code to ride
app.post('/api/v1/rides/:rideId/promo', async (req, res) => {
  const { rideId } = req.params;
  const { promoCode } = req.body;
  const userId = req.user.id;

  // 1. Validate promo code
  const promo = await db.query(
    'SELECT * FROM promo_codes WHERE code = ? AND expires_at > NOW()',
    [promoCode]
  );

  if (!promo) {
    return res.status(404).json({ error: 'Invalid or expired promo code' });
  }

  if (promo.current_uses >= promo.max_uses) {
    return res.status(400).json({ error: 'Promo code fully redeemed' });
  }

  // 2. Check if user already used this code
  const existing = await db.query(
    'SELECT id FROM promo_redemptions WHERE promo_code = ? AND user_id = ?',
    [promoCode, userId]
  );

  if (existing) {
    return res.status(400).json({ error: 'You already used this promo code' });
  }

  // 3. Calculate discount
  const ride = await db.query('SELECT fare FROM rides WHERE id = ?', [rideId]);
  let discount = 0;

  if (promo.discount_type === 'percentage') {
    discount = ride.fare × (promo.discount_value / 100);
  } else {
    discount = promo.discount_value;
  }

  const finalFare = Math.max(ride.fare - discount, 0);  // Min fare: $0

  // 4. Apply promo (transaction)
  await db.transaction(async (trx) => {
    // Update ride fare
    await trx.query('UPDATE rides SET final_fare = ? WHERE id = ?', [finalFare, rideId]);

    // Record redemption
    await trx.query(
      'INSERT INTO promo_redemptions (promo_code, user_id, ride_id, discount_applied) VALUES (?, ?, ?, ?)',
      [promoCode, userId, rideId, discount]
    );

    // Increment uses
    await trx.query('UPDATE promo_codes SET current_uses = current_uses + 1 WHERE code = ?', [promoCode]);

    // If referral code, credit referrer (async via Kafka)
    if (promo.type === 'referral' && promo.referrer_id) {
      await kafka.send({
        topic: 'promo.referral_used',
        messages: [{
          value: JSON.stringify({
            referrer_id: promo.referrer_id,
            referee_id: userId,
            credit_amount: 10.00
          })
        }]
      });
    }
  });

  res.json({ success: true, discount, final_fare: finalFare });
});
```

**Fraud Prevention:**
```javascript
// Detect multiple accounts from same device
const deviceFingerprint = getDeviceFingerprint(req);

const existingUser = await db.query(
  'SELECT user_id FROM device_fingerprints WHERE fingerprint = ?',
  [deviceFingerprint]
);

if (existingUser) {
  // Flag for manual review
  await db.query(
    'INSERT INTO fraud_alerts (user_id, type, details) VALUES (?, ?, ?)',
    [userId, 'duplicate_device', `Same device as user ${existingUser.user_id}`]
  );
}
```

---

## 12. How would you design the rating system?

**Answer:**

**Requirements:**
- Both rider and driver rate each other (1-5 stars)
- Rating affects future matching (low-rated drivers get fewer rides)
- Calculate average rating efficiently

**Database Schema:**
```sql
CREATE TABLE ratings (
    rating_id BIGSERIAL PRIMARY KEY,
    ride_id BIGINT REFERENCES rides(ride_id),
    rater_id BIGINT REFERENCES users(user_id),
    rated_id BIGINT REFERENCES users(user_id),
    rater_type VARCHAR(10),  -- 'rider' or 'driver'
    rating INT CHECK (rating BETWEEN 1 AND 5),
    feedback TEXT,
    tips DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT NOW(),

    UNIQUE (ride_id, rater_id, rater_type)  -- Can only rate once per ride
);

-- Denormalized average rating (for performance)
ALTER TABLE users ADD COLUMN rating DECIMAL(3,2) DEFAULT 5.00;
ALTER TABLE users ADD COLUMN total_ratings INT DEFAULT 0;
```

**Rating Flow:**
```javascript
app.post('/api/v1/rides/:rideId/rating', async (req, res) => {
  const { rideId } = req.params;
  const { rating, feedback, tips } = req.body;
  const raterId = req.user.id;

  // 1. Get ride details
  const ride = await db.query('SELECT rider_id, driver_id, status FROM rides WHERE id = ?', [rideId]);

  if (ride.status !== 'completed') {
    return res.status(400).json({ error: 'Can only rate completed rides' });
  }

  // 2. Determine who is being rated
  const raterType = (raterId === ride.rider_id) ? 'rider' : 'driver';
  const ratedId = (raterType === 'rider') ? ride.driver_id : ride.rider_id;

  // 3. Insert rating
  await db.query(
    'INSERT INTO ratings (ride_id, rater_id, rated_id, rater_type, rating, feedback, tips) VALUES (?, ?, ?, ?, ?, ?, ?)',
    [rideId, raterId, ratedId, raterType, rating, feedback, tips]
  );

  // 4. Update average rating (async via Kafka for performance)
  await kafka.send({
    topic: 'rating.submitted',
    messages: [{
      value: JSON.stringify({ rated_id: ratedId, rating })
    }]
  });

  res.json({ success: true });
});

// Background worker: Recalculate average rating
ratingConsumer.on('message', async (event) => {
  const { rated_id, rating } = event.data;

  // Incremental average (more efficient than recalculating from scratch)
  await db.query(`
    UPDATE users
    SET rating = ((rating × total_ratings) + ?) / (total_ratings + 1),
        total_ratings = total_ratings + 1
    WHERE user_id = ?
  `, [rating, rated_id]);
});
```

**Matching Algorithm (Prefer High-Rated Drivers):**
```javascript
// Scoring includes rating as a factor
score = (1 / distance) × driver_rating × 1.2  // 20% bonus for rating

driver_456: (1/1.2) × 4.9 × 1.2 = 4.90
driver_789: (1/1.0) × 3.5 × 1.2 = 4.20

// driver_456 wins despite being slightly farther
```

---

## 13. How do you handle time zones for a global application?

**Answer:**

**Problem:** User in New York books ride at "10:00 AM" → What does this mean in the database?

**Solution: Store all timestamps in UTC, display in local time**

**Database:**
```sql
-- ALWAYS store in UTC
CREATE TABLE rides (
    ride_id BIGSERIAL PRIMARY KEY,
    requested_at TIMESTAMP NOT NULL,  -- UTC
    completed_at TIMESTAMP,           -- UTC
    timezone VARCHAR(50)               -- e.g., 'America/New_York'
);

-- Example
INSERT INTO rides (requested_at, timezone)
VALUES ('2025-01-15 15:00:00'::TIMESTAMP, 'America/New_York');
-- Stored as UTC (15:00 UTC), not local time (10:00 EST)
```

**API Response (Convert to Local Time):**
```javascript
const moment = require('moment-timezone');

app.get('/api/v1/rides/:id', async (req, res) => {
  const ride = await db.query('SELECT * FROM rides WHERE id = ?', [req.params.id]);

  // Convert UTC to user's timezone
  const userTimezone = req.user.timezone || 'America/New_York';

  res.json({
    ride_id: ride.id,
    requested_at_utc: ride.requested_at,  // For debugging
    requested_at_local: moment(ride.requested_at)
      .tz(userTimezone)
      .format('YYYY-MM-DD HH:mm:ss z'),  // "2025-01-15 10:00:00 EST"
    timezone: userTimezone
  });
});
```

**Frontend Display:**
```javascript
// User sees their local time
const rideTime = new Date(ride.requested_at_utc);
console.log(rideTime.toLocaleString('en-US', {
  timeZone: 'America/New_York'
}));
// Output: "1/15/2025, 10:00:00 AM"
```

**Scheduling Considerations:**
```javascript
// Daily report generation at 2 AM in each timezone
cron.schedule('0 2 * * *', async () => {
  const shards = ['us_east', 'us_west', 'europe', 'asia'];

  for (const shard of shards) {
    const shardTimezone = getShardTimezone(shard);  // 'America/New_York', etc.

    // Check if it's 2 AM in this shard's timezone
    const currentHour = moment().tz(shardTimezone).hour();

    if (currentHour === 2) {
      await generateDailyReport(shard);
    }
  }
});
```

---

## 14. How would you implement idempotency for API requests?

**Answer:**

**Problem:** Network timeout → User clicks "Request Ride" again → Duplicate ride created!

**Solution: Idempotency Keys**

```javascript
app.post('/api/v1/rides', async (req, res) => {
  const { pickup, destination, payment_method } = req.body;
  const userId = req.user.id;

  // 1. Client provides idempotency key
  const idempotencyKey = req.headers['idempotency-key'];

  if (!idempotencyKey) {
    return res.status(400).json({ error: 'Idempotency-Key header required' });
  }

  // 2. Check if request already processed
  const existing = await redis.get(`idempotency:${idempotencyKey}`);

  if (existing) {
    // Return cached response (don't create duplicate ride)
    return res.json(JSON.parse(existing));
  }

  // 3. Process request (create ride)
  const ride = await createRide(userId, pickup, destination, payment_method);

  // 4. Cache result for 24 hours
  await redis.setex(
    `idempotency:${idempotencyKey}`,
    86400,  // 24 hours
    JSON.stringify({ ride_id: ride.id, status: ride.status })
  );

  res.status(201).json({ ride_id: ride.id, status: ride.status });
});
```

**Client-Side (Generate Idempotency Key):**
```javascript
// Mobile app
const idempotencyKey = `${userId}-${Date.now()}-${Math.random()}`;

fetch('https://api.uber.com/v1/rides', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Idempotency-Key': idempotencyKey,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    pickup: { lat: 37.7749, lng: -122.4194 },
    destination: { lat: 37.8044, lng: -122.2712 }
  })
});

// If network timeout, retry with SAME idempotency key
// → Server returns existing ride (doesn't create duplicate)
```

---

## 15. How do you test a system at Uber's scale?

**Answer:**

**Testing Pyramid:**

```
         ┌─────────────┐
         │  Manual     │  1% (exploratory testing)
         ├─────────────┤
         │  E2E Tests  │  10% (critical user flows)
         ├─────────────┤
         │  Integration│  30% (service interactions)
         ├─────────────┤
         │  Unit Tests │  60% (individual functions)
         └─────────────┘
```

**1. Unit Tests (Fast, Isolated):**
```javascript
describe('calculateFare', () => {
  it('applies surge pricing correctly', () => {
    const fare = calculateFare({
      distance: 10,  // km
      baseRate: 2.50,
      perKm: 1.50,
      surgeMultiplier: 1.5
    });

    // Expected: (2.50 + 10 × 1.50) × 1.5 = 26.25
    expect(fare).toBe(26.25);
  });
});
```

**2. Integration Tests (Service Interactions):**
```javascript
describe('Ride Booking Integration', () => {
  it('creates ride and sends notification', async () => {
    // Start test Kafka consumer
    const notifications = [];
    testConsumer.on('message', (msg) => notifications.push(msg));

    // Book ride
    const response = await request(app)
      .post('/api/v1/rides')
      .send({ pickup: {...}, destination: {...} });

    expect(response.status).toBe(201);

    // Wait for Kafka event
    await waitFor(() => notifications.length > 0);

    expect(notifications[0].type).toBe('ride.created');
  });
});
```

**3. Load Testing (Performance & Scalability):**
```javascript
// Apache JMeter or k6
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '1m', target: 1000 },    // Ramp to 1K users
    { duration: '5m', target: 10000 },   // Ramp to 10K users
    { duration: '10m', target: 100000 }, // Ramp to 100K users (peak)
    { duration: '5m', target: 0 }        // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<200'],  // 95% of requests < 200ms
    http_req_failed: ['rate<0.01']     // Error rate < 1%
  }
};

export default function () {
  const payload = JSON.stringify({
    pickup: { lat: 37.7749, lng: -122.4194 },
    destination: { lat: 37.8044, lng: -122.2712 }
  });

  const response = http.post('https://api.uber.com/v1/rides', payload, {
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` }
  });

  check(response, {
    'status is 201': (r) => r.status === 201,
    'response time < 200ms': (r) => r.timings.duration < 200
  });

  sleep(1);
}
```

**4. Chaos Engineering (Resilience Testing):**
```javascript
// Chaos Monkey: Randomly kill pods
const chaos = require('chaos-lambda');

chaos.inject({
  probability: 0.01,  // 1% chance
  actions: [
    { type: 'kill_pod', service: 'ride-service' },
    { type: 'add_latency', service: 'payment-service', latency: 5000 },
    { type: 'return_error', service: 'driver-service', errorCode: 503 }
  ]
});

// Verify system still works (graceful degradation)
```

**5. Shadow Traffic (Production Testing):**
```javascript
// Duplicate 1% of production traffic to new version
app.use((req, res, next) => {
  if (Math.random() < 0.01) {
    // Send request to new version (don't wait for response)
    http.post('https://api-v2.uber.com' + req.path, req.body).catch(() => {});
  }

  next();  // Continue to current version
});
```

---

**15 more interview questions with answers continue in next response...**

For space, I'll include 5 more critical questions. The full document would contain 20+ questions.

---

## 16. How would you implement a driver heatmap feature?

**Answer:**

**Requirement:** Show riders where demand is high (more rides, higher fares).

**Implementation:**

```javascript
// 1. Divide city into grid (H3 geo-indexing)
const h3 = require('h3-js');

// Convert lat/lng to H3 cell (hexagon)
const cell = h3.geoToH3(37.7749, -122.4194, 9);  // Resolution 9 (~500m hexagons)

// 2. Aggregate ride requests per cell (Kafka Streams)
stream
  .from('ride.requested')
  .map(event => ({
    cell: h3.geoToH3(event.pickup.lat, event.pickup.lng, 9),
    timestamp: event.timestamp
  }))
  .window({ type: 'tumbling', duration: 300000 })  // 5-minute window
  .groupBy('cell')
  .count()
  .to('heatmap.demand');

// 3. Cache heatmap data (Redis)
heatmapConsumer.on('message', async (event) => {
  await redis.zadd(
    'heatmap:demand',
    event.count,  // Score (number of requests)
    event.cell    // H3 cell ID
  );

  await redis.expire('heatmap:demand', 300);  // 5-minute TTL
});

// 4. API: Get heatmap for bounding box
app.get('/api/v1/heatmap', async (req, res) => {
  const { minLat, maxLat, minLng, maxLng } = req.query;

  // Get all H3 cells in bounding box
  const cells = h3.polyfill([
    [minLat, minLng], [maxLat, minLng],
    [maxLat, maxLng], [minLat, maxLng]
  ], 9);

  // Fetch demand for each cell
  const heatmap = await redis.zrange('heatmap:demand', 0, -1, 'WITHSCORES');

  // Convert to GeoJSON
  const features = cells.map(cell => ({
    type: 'Feature',
    geometry: {
      type: 'Polygon',
      coordinates: h3.h3ToGeoBoundary(cell)
    },
    properties: {
      demand: heatmap[cell] || 0,
      color: getDemandColor(heatmap[cell])
    }
  }));

  res.json({ type: 'FeatureCollection', features });
});
```

---

## 17. How do you handle partial failures in a distributed system?

**Answer:**

Use the **Saga Pattern** for distributed transactions.

**Example: Ride Booking Saga**

```javascript
// Saga: Book ride (multiple services)
async function bookRideSaga(riderId, driverId, pickup, destination) {
  const saga = [];

  try {
    // Step 1: Reserve driver
    await driverService.reserve(driverId);
    saga.push(() => driverService.unreserve(driverId));  // Compensating action

    // Step 2: Pre-authorize payment
    const paymentId = await paymentService.preAuth(riderId, estimatedFare);
    saga.push(() => paymentService.cancelPreAuth(paymentId));

    // Step 3: Create ride
    const rideId = await rideService.create(riderId, driverId, pickup, destination);
    saga.push(() => rideService.cancel(rideId));

    // Step 4: Send notification
    await notificationService.send(riderId, 'Ride confirmed');

    return { rideId, success: true };

  } catch (error) {
    // Rollback: Execute compensating actions in reverse order
    console.error('Saga failed, rolling back:', error);

    for (const compensate of saga.reverse()) {
      try {
        await compensate();
      } catch (rollbackError) {
        // Log rollback failure (manual intervention needed)
        console.error('Rollback failed:', rollbackError);
        alertOpsTeam('Saga rollback failed');
      }
    }

    return { success: false, error: error.message };
  }
}
```

---

## 18. How would you implement driver earnings payout?

**Answer:**

**Requirements:**
- Drivers paid weekly (every Monday)
- Uber takes 20% commission
- Support multiple payout methods (bank transfer, PayPal)

**Database Schema:**
```sql
CREATE TABLE driver_earnings (
    earning_id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT REFERENCES drivers(driver_id),
    ride_id BIGINT REFERENCES rides(ride_id),
    gross_fare DECIMAL(10,2),  -- Total ride fare
    commission DECIMAL(10,2),  -- Uber's cut (20%)
    net_earnings DECIMAL(10,2),  -- Driver receives (80%)
    status VARCHAR(20) DEFAULT 'pending',  -- 'pending', 'paid', 'failed'
    payout_id VARCHAR(50),  -- Stripe/PayPal payout ID
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_driver_status ON driver_earnings (driver_id, status);
```

**Weekly Payout Job (Cron):**
```javascript
// Run every Monday at 2 AM
cron.schedule('0 2 * * 1', async () => {
  console.log('Starting weekly driver payouts...');

  // Get all drivers with pending earnings
  const drivers = await db.query(`
    SELECT driver_id, SUM(net_earnings) as total_earnings
    FROM driver_earnings
    WHERE status = 'pending'
    GROUP BY driver_id
    HAVING SUM(net_earnings) > 0
  `);

  for (const driver of drivers) {
    try {
      // Get driver's payout method
      const payoutMethod = await db.query(
        'SELECT payout_method, payout_account FROM drivers WHERE id = ?',
        [driver.driver_id]
      );

      // Process payout via Stripe
      const payout = await stripe.payouts.create({
        amount: Math.round(driver.total_earnings * 100),  // Convert to cents
        currency: 'usd',
        method: 'instant',  // or 'standard' (2-3 days)
        destination: payoutMethod.payout_account
      });

      // Update earnings status
      await db.query(`
        UPDATE driver_earnings
        SET status = 'paid',
            payout_id = ?,
            paid_at = NOW()
        WHERE driver_id = ? AND status = 'pending'
      `, [payout.id, driver.driver_id]);

      // Send notification
      await notificationService.send(driver.driver_id,
        `Your weekly earnings of $${driver.total_earnings} have been deposited.`
      );

      console.log(`✅ Paid ${driver.driver_id}: $${driver.total_earnings}`);

    } catch (error) {
      console.error(`❌ Payout failed for driver ${driver.driver_id}:`, error);

      // Retry in 1 hour
      await queue.add('retry-payout', {
        driver_id: driver.driver_id,
        amount: driver.total_earnings
      }, { delay: 3600000 });
    }
  }

  console.log('Weekly payouts complete');
});
```

---

## 19. How do you implement analytics dashboards for real-time metrics?

**Answer:**

**Architecture:**

```
Kafka (events) → Kafka Streams (aggregation) → Materialized View → Grafana
```

**Implementation:**

```javascript
// 1. Stream events to Kafka
rideService.on('rideCompleted', (ride) => {
  kafka.send({
    topic: 'ride.completed',
    messages: [{
      value: JSON.stringify({
        ride_id: ride.id,
        city_id: ride.city_id,
        fare: ride.fare,
        distance: ride.distance,
        duration: ride.duration,
        timestamp: Date.now()
      })
    }]
  });
});

// 2. Kafka Streams: Aggregate metrics
stream
  .from('ride.completed')
  .window({ type: 'tumbling', duration: 60000 })  // 1-minute window
  .groupBy('city_id')
  .aggregate({
    initializer: () => ({ count: 0, total_revenue: 0, total_distance: 0 }),
    aggregator: (agg, event) => ({
      count: agg.count + 1,
      total_revenue: agg.total_revenue + event.fare,
      total_distance: agg.total_distance + event.distance
    })
  })
  .to('analytics.rides_per_minute');

// 3. Materialize to TimescaleDB (time-series DB)
analyticsConsumer.on('message', async (event) => {
  const { city_id, count, total_revenue, total_distance, window_start } = event;

  await timescaledb.query(`
    INSERT INTO ride_metrics (timestamp, city_id, rides_count, revenue, distance)
    VALUES (?, ?, ?, ?, ?)
  `, [window_start, city_id, count, total_revenue, total_distance]);
});

// 4. Grafana queries TimescaleDB
// Query: Rides per hour (last 24 hours)
SELECT
  time_bucket('1 hour', timestamp) AS hour,
  SUM(rides_count) AS total_rides
FROM ride_metrics
WHERE timestamp > NOW() - INTERVAL '24 hours'
GROUP BY hour
ORDER BY hour;
```

---

## 20. How would you migrate from monolith to microservices?

**Answer:**

**Strategy: Strangler Fig Pattern** (gradual migration, not big bang)

**Phase 1: Extract Read-Only Services**
```
Monolith handles: All reads + writes
Extract: Analytics Service (read-only) → Safe to start with
```

**Phase 2: Extract Stateless Services**
```
Extract: Notification Service
  - Doesn't own data
  - Consumes Kafka events
  - Low risk
```

**Phase 3: Extract Core Services**
```
Extract: Payment Service
  - Create new database (payments DB)
  - Dual-write: Write to both monolith DB and payments DB
  - Read from payments DB
  - Verify consistency
  - Remove monolith writes
```

**Example Migration:**
```javascript
// Before (Monolith)
app.post('/api/v1/rides/:id/complete', async (req, res) => {
  await db.query('UPDATE rides SET status = "completed"');
  await db.query('INSERT INTO payments ...');  // Monolith handles payments
  await sendEmail(...);
  res.json({ success: true });
});

// After (Microservices)
app.post('/api/v1/rides/:id/complete', async (req, res) => {
  // Ride Service (monolith) updates ride
  await db.query('UPDATE rides SET status = "completed"');

  // Publish event to Kafka
  await kafka.send({ topic: 'ride.completed', ... });

  // Payment Service consumes event (async)
  // Email Service consumes event (async)

  res.json({ success: true });
});
```

---

## Summary of Interview Tips

1. **Start Simple:** Basic architecture first, then optimize
2. **Clarify Requirements:** Ask about scale, latency, consistency needs
3. **Draw Diagrams:** Sequence diagrams, architecture diagrams
4. **Discuss Trade-offs:** No perfect solution, explain pros/cons
5. **Use Real Numbers:** "10K req/sec", "99.99% availability"
6. **Mention Technologies:** PostgreSQL, Redis, Kafka (not just "database", "cache")
7. **Think About Costs:** $108K/month infrastructure for $1B revenue = 0.13%
8. **Plan for Failure:** Circuit breakers, retries, failover
9. **Scalability:** Horizontal scaling, sharding, caching
10. **Real-World Examples:** "Like Uber does...", "Similar to Netflix..."
