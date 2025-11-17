# Scalability & Reliability Strategies

## Scalability Overview

**Goal:** Handle 20M DAU, 180K requests/sec peak, 1.32M location updates/sec

---

## Horizontal vs Vertical Scaling

### Vertical Scaling (Scale Up)
**What:** Upgrade to bigger servers (more CPU, RAM)

**Example:**
```
Current: m5.xlarge (4 vCPUs, 16 GB RAM)
Upgrade to: m5.4xlarge (16 vCPUs, 64 GB RAM)
```

**✅ Pros:**
- Simple (no code changes)
- Lower latency (single machine)

**❌ Cons:**
- Expensive ($500/month → $2000/month for 4x capacity)
- Hardware limits (can't infinitely scale)
- Single point of failure

**When to Use:** Databases (up to a point), caches

### Horizontal Scaling (Scale Out)
**What:** Add more servers

**Example:**
```
Current: 10 app servers
Scale to: 50 app servers
```

**✅ Pros:**
- Cost-effective (add cheap commodity servers)
- No theoretical limit
- High availability (one fails, others continue)

**❌ Cons:**
- Requires stateless design
- Load balancing needed
- More operational complexity

**When to Use:** Stateless services (app servers, API gateways)

---

## Component-Specific Scaling Strategies

### 1. Application Servers (Stateless)

**Scaling Method:** Horizontal (Kubernetes HPA)

**Auto-scaling Rules:**
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: ride-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: ride-service
  minReplicas: 10
  maxReplicas: 100
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Pods
    pods:
      metric:
        name: http_requests_per_second
      target:
        type: AverageValue
        averageValue: "5000"
```

**How It Works:**
```
Normal load: 10 pods (5K req/sec each) = 50K total
Peak load: CPU > 70% → Scale to 50 pods = 250K req/sec
Scale-down: CPU < 50% for 5 min → Remove pods gradually
```

**Cost:**
- 10 pods × $100/month = $1,000 (normal)
- 50 pods × $100/month = $5,000 (peak hours only)
- Average: $2,000/month (pods scale down off-peak)

---

### 2. Redis Cache

**Scaling Method:** Vertical + Clustering

**Stage 1: Vertical Scaling (0-100K ops/sec)**
```
Start: r6g.large (2 vCPUs, 16 GB RAM) → 50K ops/sec
Scale: r6g.2xlarge (8 vCPUs, 64 GB RAM) → 200K ops/sec
```

**Stage 2: Clustering (> 200K ops/sec)**
```
3 master nodes + 3 replicas = 6-node cluster
Each master: 200K ops/sec
Total: 600K ops/sec

For Uber (1.32M location updates/sec):
  Need 7 master nodes
  Total: 14 nodes (7 masters + 7 replicas)
```

**Redis Cluster Configuration:**
```javascript
const Redis = require('ioredis');

const cluster = new Redis.Cluster([
  { host: 'redis-node1.uber.internal', port: 6379 },
  { host: 'redis-node2.uber.internal', port: 6379 },
  { host: 'redis-node3.uber.internal', port: 6379 }
], {
  redisOptions: {
    password: process.env.REDIS_PASSWORD
  },
  scaleReads: 'slave'  // Read from replicas
});
```

**Sharding Strategy:**
```
Key: driver:456:location
Hash slot: CRC16(driver:456:location) mod 16384 = 1234
Shard: slot 1234 → Node 1

Key: driver:789:location
Hash slot: CRC16(driver:789:location) mod 16384 = 8765
Shard: slot 8765 → Node 2
```

---

### 3. PostgreSQL Database

**Stage 1: Vertical Scaling**
```
db.m5.large (2 vCPUs, 8 GB RAM) → 1K queries/sec
db.r5.4xlarge (16 vCPUs, 128 GB RAM) → 10K queries/sec
db.r5.12xlarge (48 vCPUs, 384 GB RAM) → 30K queries/sec (max practical)
```

**Stage 2: Read Replicas (Horizontal for Reads)**
```
Master: 10K writes/sec
Replica 1: 8K reads/sec
Replica 2: 8K reads/sec
Replica 3: 8K reads/sec
Replica 4: 8K reads/sec

Total capacity: 10K writes + 32K reads = 42K queries/sec
```

**Stage 3: Geographic Sharding (Horizontal for Writes)**
```
US East Shard: 10K writes/sec
US West Shard: 10K writes/sec
Europe Shard: 10K writes/sec
Asia Shard: 10K writes/sec

Total write capacity: 40K writes/sec
```

**Connection Pooling:**
```javascript
// pgBouncer configuration
[databases]
uber_us_east = host=master.us-east.uber.db port=5432 dbname=uber

[pgbouncer]
pool_mode = transaction  // Connection released after transaction
max_client_conn = 10000  // Max client connections
default_pool_size = 100  // Connections to PostgreSQL per user
reserve_pool_size = 25   // Emergency connections
```

---

### 4. Kafka Message Queue

**Scaling Method:** Horizontal (Add Brokers)

**Partitioning Strategy:**
```
Topic: ride.created
Partitions: 20 (for parallel processing)

Message key: ride_id (ensures ordering per ride)

Partition assignment:
  partition = hash(ride_id) mod 20

ride_id: 789 → partition 9 → broker 3
ride_id: 790 → partition 10 → broker 1
```

**Consumer Scaling:**
```
Topic: ride.created (20 partitions)
Consumer group: payment-service (20 consumers)

Each consumer handles 1 partition
Throughput: 20 × 10K msg/sec = 200K msg/sec
```

**Adding Capacity:**
```
Current: 3 brokers, 10K msg/sec each = 30K total
Need: 100K msg/sec
Solution: Add 7 brokers (10 total) = 100K msg/sec

Kafka automatically rebalances partitions across brokers
```

---

## Load Balancing Strategies

### Layer 4 (Transport Layer) vs Layer 7 (Application Layer)

**Layer 4 Load Balancer (TCP/UDP):**
- Fast (just routes packets based on IP:Port)
- No SSL termination
- No content-based routing
- Use: Internal service-to-service communication

**Layer 7 Load Balancer (HTTP/HTTPS):**
- Content-aware routing (based on URL, headers)
- SSL termination
- Caching, compression
- Use: External traffic (clients → API Gateway)

**Example (Nginx Layer 7):**
```nginx
http {
    upstream ride_service {
        least_conn;  // Route to server with fewest connections
        server app1.uber.internal:8080;
        server app2.uber.internal:8080;
        server app3.uber.internal:8080;
    }

    upstream payment_service {
        server payment1.uber.internal:8080;
        server payment2.uber.internal:8080;
    }

    server {
        listen 443 ssl;
        server_name api.uber.com;

        location /api/v1/rides {
            proxy_pass http://ride_service;
        }

        location /api/v1/payments {
            proxy_pass http://payment_service;
        }
    }
}
```

### Load Balancing Algorithms

**1. Round Robin:**
```
Request 1 → Server A
Request 2 → Server B
Request 3 → Server C
Request 4 → Server A (cycle repeats)
```
- **Pro:** Simple, fair distribution
- **Con:** Doesn't consider server load

**2. Least Connections:**
```
Server A: 100 active connections
Server B: 50 active connections
Server C: 75 active connections

Next request → Server B (least connections)
```
- **Pro:** Adapts to server load
- **Con:** Requires tracking connection count

**3. IP Hash (Sticky Sessions):**
```
User IP: 192.168.1.100
hash(192.168.1.100) mod 3 = 1 → Server B

Same user always goes to Server B
```
- **Pro:** Session affinity (useful for stateful apps)
- **Con:** Uneven distribution if some IPs generate more traffic

---

## Caching Strategy for Scalability

### Multi-Level Caching

```
Request flow:

1. Client-side cache (Browser)
   Cache-Control: max-age=3600
   Hit: Instant (0ms)
   Use for: Static assets (JS, CSS, images)

2. CDN cache (CloudFlare Edge)
   Hit: 10-20ms
   Use for: Map tiles, driver photos

3. Application-level cache (Redis)
   Hit: 2-5ms
   Use for: Driver locations, ride status, session data

4. Database query cache (PostgreSQL)
   Hit: 10-20ms
   Use for: Complex aggregation queries

5. Database (miss on all caches)
   Miss: 50-100ms
   Last resort
```

### Cache Penetration Prevention

**Problem:** Malicious users request non-existent data → cache misses → database overwhelmed

**Solution: Bloom Filter**
```javascript
// Bloom filter: Probabilistic data structure
const BloomFilter = require('bloomfilter').BloomFilter;

const filter = new BloomFilter(1000000, 4);  // 1M items, 4 hash functions

// Add all valid user IDs to filter
userIds.forEach(id => filter.add(id));

// Check before querying database
async function getUser(userId) {
  // Bloom filter check (instant)
  if (!filter.test(userId)) {
    // Definitely doesn't exist
    return { error: 'User not found' };
  }

  // Might exist, check cache
  const cached = await redis.get(`user:${userId}`);
  if (cached) return cached;

  // Check database
  const user = await db.query('SELECT * FROM users WHERE id = ?', [userId]);

  if (user) {
    await redis.setex(`user:${userId}`, 3600, JSON.stringify(user));
  }

  return user;
}
```

---

## Reliability & High Availability

### Target Availability: 99.99% (Four Nines)

**Allowable Downtime:**
```
99.9% (three nines):  8.76 hours/year  (43.8 min/month)
99.99% (four nines):  52.6 min/year    (4.38 min/month)
99.999% (five nines): 5.26 min/year    (26 sec/month)
```

**Achieving 99.99%:**
- No single point of failure
- Automatic failover (< 30 sec)
- Multi-region deployment
- Health checks and circuit breakers

---

### Circuit Breaker Pattern

**Problem:** External service (Stripe) is down → all payment requests fail → system overwhelmed with retries

**Solution:** Circuit Breaker

```javascript
const CircuitBreaker = require('opossum');

// Payment service with circuit breaker
const paymentOptions = {
  timeout: 5000,        // Timeout after 5 sec
  errorThresholdPercentage: 50,  // Open circuit if 50% fail
  resetTimeout: 30000   // Try again after 30 sec
};

const paymentBreaker = new CircuitBreaker(processPayment, paymentOptions);

// States:
// CLOSED: Normal operation, requests pass through
// OPEN: Too many failures, reject requests immediately (fast fail)
// HALF_OPEN: Test if service recovered

paymentBreaker.on('open', () => {
  console.log('Circuit breaker OPEN - Stripe is down');
  alertOpsTeam('Payment service circuit breaker opened');
});

paymentBreaker.on('close', () => {
  console.log('Circuit breaker CLOSED - Stripe recovered');
});

// Usage
try {
  const result = await paymentBreaker.fire(rideId, amount);
} catch (err) {
  if (err.code === 'EBROKEN') {
    // Circuit is open, fail fast
    return { error: 'Payment service unavailable, please try again later' };
  }
  // Other error
  throw err;
}
```

---

### Health Checks & Auto-Recovery

**Kubernetes Liveness & Readiness Probes:**

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: ride-service
spec:
  containers:
  - name: ride-service
    image: uber/ride-service:v1.2.3

    # Liveness probe: Is the app running?
    livenessProbe:
      httpGet:
        path: /health/live
        port: 8080
      initialDelaySeconds: 30
      periodSeconds: 10
      failureThreshold: 3  // Restart if fails 3 times

    # Readiness probe: Is the app ready to serve traffic?
    readinessProbe:
      httpGet:
        path: /health/ready
        port: 8080
      initialDelaySeconds: 10
      periodSeconds: 5
      failureThreshold: 2  // Remove from load balancer if fails
```

**Health Check Endpoint:**
```javascript
// /health/live - Check if app is alive
app.get('/health/live', (req, res) => {
  res.status(200).json({ status: 'UP' });
});

// /health/ready - Check if app is ready (DB connected, etc.)
app.get('/health/ready', async (req, res) => {
  try {
    // Check database connection
    await db.query('SELECT 1');

    // Check Redis connection
    await redis.ping();

    res.status(200).json({
      status: 'UP',
      database: 'healthy',
      cache: 'healthy'
    });
  } catch (err) {
    res.status(503).json({
      status: 'DOWN',
      error: err.message
    });
  }
});
```

---

### Graceful Degradation

**Principle:** When a component fails, system continues at reduced capacity (not complete failure)

**Example:**
```javascript
// Recommendation service is down, but ride booking still works

async function bookRide(riderId, pickup, destination) {
  let suggestedDrivers = [];

  try {
    // Try to get ML-recommended drivers
    suggestedDrivers = await recommendationService.getSuggestedDrivers(riderId);
  } catch (err) {
    console.error('Recommendation service down, falling back to distance-based matching');
    // Graceful degradation: Use simple distance-based matching
    suggestedDrivers = await findNearestDrivers(pickup);
  }

  // Continue with ride booking
  const driver = suggestedDrivers[0];
  return await createRide(riderId, driver.id, pickup, destination);
}
```

---

### Rate Limiting (Protect from Overload)

**Token Bucket Algorithm:**

```javascript
const Redis = require('ioredis');
const redis = new Redis();

async function rateLimit(userId, maxRequests = 100, windowSeconds = 60) {
  const key = `rate_limit:${userId}`;
  const now = Date.now();
  const windowStart = now - (windowSeconds * 1000);

  // Sliding window log
  await redis.zremrangebyscore(key, 0, windowStart);  // Remove old requests
  const requestCount = await redis.zcard(key);        // Count requests in window

  if (requestCount >= maxRequests) {
    return {
      allowed: false,
      retryAfter: windowSeconds
    };
  }

  // Add current request
  await redis.zadd(key, now, `${now}-${Math.random()}`);
  await redis.expire(key, windowSeconds);

  return {
    allowed: true,
    remaining: maxRequests - requestCount - 1
  };
}

// Middleware
app.use(async (req, res, next) => {
  const userId = req.user?.id || req.ip;
  const limit = await rateLimit(userId);

  if (!limit.allowed) {
    return res.status(429).json({
      error: 'Too many requests',
      retryAfter: limit.retryAfter
    });
  }

  res.setHeader('X-RateLimit-Remaining', limit.remaining);
  next();
});
```

---

### Monitoring & Alerting

**Golden Signals (SRE Best Practices):**

1. **Latency:** How long do requests take?
2. **Traffic:** How many requests per second?
3. **Errors:** What's the error rate?
4. **Saturation:** How full is the system (CPU, memory, disk)?

**Prometheus Metrics:**
```javascript
const prometheus = require('prom-client');

// Request duration histogram
const httpRequestDuration = new prometheus.Histogram({
  name: 'http_request_duration_seconds',
  help: 'Duration of HTTP requests in seconds',
  labelNames: ['method', 'route', 'status_code'],
  buckets: [0.01, 0.05, 0.1, 0.5, 1, 2, 5]
});

// Request counter
const httpRequestTotal = new prometheus.Counter({
  name: 'http_requests_total',
  help: 'Total number of HTTP requests',
  labelNames: ['method', 'route', 'status_code']
});

// Middleware to collect metrics
app.use((req, res, next) => {
  const start = Date.now();

  res.on('finish', () => {
    const duration = (Date.now() - start) / 1000;

    httpRequestDuration
      .labels(req.method, req.route?.path || req.path, res.statusCode)
      .observe(duration);

    httpRequestTotal
      .labels(req.method, req.route?.path || req.path, res.statusCode)
      .inc();
  });

  next();
});

// Expose metrics endpoint
app.get('/metrics', async (req, res) => {
  res.set('Content-Type', prometheus.register.contentType);
  res.end(await prometheus.register.metrics());
});
```

**Grafana Dashboard Alerts:**
```yaml
alerts:
  - name: HighLatency
    condition: |
      http_request_duration_seconds{quantile="0.99"} > 1
      for 5 minutes
    severity: warning
    action: Slack notification

  - name: HighErrorRate
    condition: |
      rate(http_requests_total{status_code=~"5.."}[5m])
      / rate(http_requests_total[5m]) > 0.01
      for 2 minutes
    severity: critical
    action: PagerDuty alert (wake up on-call engineer)

  - name: DatabaseDown
    condition: |
      up{job="postgres"} == 0
      for 1 minute
    severity: critical
    action: PagerDuty + Auto-failover to replica
```

---

## Disaster Recovery Plan

### RTO & RPO

- **RTO (Recovery Time Objective):** How long can we be down? **Target: 5 minutes**
- **RPO (Recovery Point Objective):** How much data can we lose? **Target: < 1 minute**

### Backup Strategy

**1. Continuous Backup (PostgreSQL WAL):**
```
Every transaction logged to Write-Ahead Log
WAL files backed up to S3 every 60 seconds
Enables point-in-time recovery (PITR)

Example: Accidental DELETE at 10:30 AM
  → Restore database to 10:29 AM (before deletion)
```

**2. Daily Snapshots:**
```
Automated snapshot at 2 AM UTC daily
Retained for 30 days
Cross-region replication (US → EU, Asia)
```

**3. Multi-Region Failover:**
```
Primary region: US East (active)
Secondary region: US West (hot standby)

If US East goes down:
  1. DNS failover to US West (30 seconds)
  2. Promote US West read replica to master (30 seconds)
  3. Total downtime: 60 seconds ✅
```

---

## Capacity Planning

**Formula:**
```
Required capacity = (Peak traffic × 1.3) / (Utilization target)

Example (App Servers):
Peak traffic: 180K req/sec
Safety margin: 1.3x = 234K req/sec
Utilization target: 70% (leave 30% headroom)

Required capacity: 234K / 0.70 = 334K req/sec

Each server: 5K req/sec
Servers needed: 334K / 5K = 67 servers

Deploy: 70 servers (round up)
```

---

## Summary: Scalability Checklist

- ✅ Stateless services (enable horizontal scaling)
- ✅ Database sharding (geographic distribution)
- ✅ Read replicas (scale reads independently from writes)
- ✅ Caching at multiple levels (CDN, Redis, app cache)
- ✅ Asynchronous processing (Kafka for non-critical tasks)
- ✅ Auto-scaling (Kubernetes HPA based on CPU/requests)
- ✅ Load balancing (distribute traffic evenly)
- ✅ Circuit breakers (prevent cascade failures)
- ✅ Rate limiting (protect from abuse)
- ✅ Health checks (auto-recovery from failures)
- ✅ Monitoring & alerting (Prometheus, Grafana, PagerDuty)
- ✅ Disaster recovery (multi-region, backups, failover)

---

## Key Takeaways for Beginners

1. **Horizontal > Vertical for Services:** Add more servers, not bigger servers
2. **Vertical + Horizontal for Databases:** Scale up first, then shard
3. **Cache Aggressively:** 90%+ cache hit rate = 10x less database load
4. **Async Where Possible:** Don't make users wait for emails, analytics
5. **Plan for Failure:** Everything fails eventually - design for resilience
6. **Monitor Everything:** Can't fix what you can't measure

**Interview Tip:**
When asked "How would you scale X to 10M users?", always discuss:
1. Current bottleneck identification (measure first!)
2. Caching strategy (low-hanging fruit)
3. Horizontal scaling (add more servers)
4. Database sharding (if needed)
5. Async processing (decouple services)
6. Cost estimation (show you think about budget!)
