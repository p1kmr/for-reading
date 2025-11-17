# Scalability, Reliability & Security

> **Learning Goal**: Understand how to build systems that scale to billions of users, stay available 99.99% of the time, and protect user data!

---

## Scalability Strategies

### Horizontal vs Vertical Scaling

```
Vertical Scaling (Scale UP):
- Buy bigger server (more CPU, RAM)
- Example: 4 vCPU → 32 vCPU
- Limits: Single server max ~1TB RAM, 96 vCPU
- Cost: Exponentially expensive

Horizontal Scaling (Scale OUT):
- Buy more servers
- Example: 1 server → 100 servers
- Limits: Nearly unlimited
- Cost: Linearly increases

Instagram Choice: Horizontal scaling everywhere ✅
```

---

### Scaling Each Component

| Component | How to Scale | Current | Max Capacity | How to Add More |
|-----------|--------------|---------|--------------|-----------------|
| **App Servers** | Add more behind LB | 100 | Unlimited | Auto-scaling group (AWS) |
| **Redis Cache** | Add shards | 32 nodes | Unlimited | Add nodes to cluster |
| **PostgreSQL** | Add shards/replicas | 150 nodes | Thousands | Add shard + replicas |
| **Kafka** | Add brokers/partitions | 3 brokers | Hundreds | Add broker to cluster |
| **Elasticsearch** | Add nodes | 10 nodes | Hundreds | Add node to cluster |
| **S3** | N/A (unlimited) | 2.2 EB | Unlimited | Just keep uploading |
| **CDN** | N/A (managed) | 715 PB/mo | Unlimited | CloudFront auto-scales |

---

### Auto-Scaling Strategy

```yaml
# AWS Auto Scaling Group configuration

app_servers:
  min_instances: 50
  max_instances: 500
  desired_capacity: 100

  scaling_policies:
    scale_up:
      metric: CPUUtilization
      threshold: 70%
      action: Add 10 instances
      cooldown: 300s  # Wait 5 min before scaling again

    scale_down:
      metric: CPUUtilization
      threshold: 30%
      action: Remove 5 instances
      cooldown: 600s  # Wait 10 min

  health_check:
    endpoint: /health
    interval: 30s
    unhealthy_threshold: 2  # 2 failed checks = remove instance
```

**Example**:
```
Normal load (10K req/sec):
- CPU: 40%
- Instances: 100 ✅

Traffic spike (30K req/sec):
- CPU: 85% → Trigger scale-up
- Add 10 instances → Now 110 instances
- CPU drops to 65% ✅

Traffic returns to normal:
- CPU: 25% → Trigger scale-down
- Remove 5 instances → Now 105 instances
- CPU: 28% ✅
```

---

## High Availability (99.99% Uptime)

### What is 99.99% Uptime?

```
99%     = 87.6 hours downtime/year (3.6 days!)
99.9%   = 8.76 hours downtime/year (525 minutes)
99.99%  = 52.6 minutes downtime/year ← Instagram target
99.999% = 5.26 minutes downtime/year (very expensive!)
```

---

### Eliminating Single Points of Failure

```mermaid
graph TB
    subgraph "❌ Single Point of Failure"
        Client1[Client] --> SingleLB[Load Balancer<br/>💥 If this fails,<br/>entire app down!]
        SingleLB --> App1[App Server]
        App1 --> SingleDB[(Database<br/>💥 SPOF!)]
    end

    subgraph "✅ No Single Point of Failure"
        Client2[Client] --> LB1[Load Balancer 1]
        Client2 --> LB2[Load Balancer 2<br/>Standby]

        LB1 --> AppA[App Server 1]
        LB1 --> AppB[App Server 2]
        LB1 --> AppC[App Server 3]

        AppA --> Master[(DB Master)]
        AppB --> Replica1[(DB Replica 1)]
        AppC --> Replica2[(DB Replica 2)]

        Master -.Replication.-> Replica1
        Master -.Replication.-> Replica2
    end

    style SingleLB fill:#ffccbc,stroke:#f44336,stroke-width:4px
    style SingleDB fill:#ffccbc,stroke:#f44336,stroke-width:4px
    style LB1 fill:#c8e6c9,stroke:#4caf50,stroke-width:2px
    style LB2 fill:#c8e6c9,stroke:#4caf50,stroke-width:2px
```

---

### Multi-AZ Deployment (Availability Zones)

```
Single AZ (Availability Zone):
- All servers in one data center
- Data center power outage → Everything down! ❌

Multi-AZ:
- Servers spread across 3 data centers (AZs)
- AZ-1 goes down → AZ-2 and AZ-3 keep running ✅

Instagram Setup:
- 3 Availability Zones (us-east-1a, us-east-1b, us-east-1c)
- Each AZ has: App servers, Redis, Database replicas
- Load balancer distributes across all AZs
```

```mermaid
graph TB
    LB[Load Balancer<br/>Multi-AZ]

    subgraph "AZ-1 (us-east-1a)"
        App1[App Servers × 33]
        Redis1[Redis Nodes × 10]
        DB1[(DB Replicas × 50)]
    end

    subgraph "AZ-2 (us-east-1b)"
        App2[App Servers × 33]
        Redis2[Redis Nodes × 11]
        DB2[(DB Replicas × 50)]
    end

    subgraph "AZ-3 (us-east-1c)"
        App3[App Servers × 34]
        Redis3[Redis Nodes × 11]
        DB3[(DB Replicas × 50)]
    end

    LB --> App1
    LB --> App2
    LB --> App3

    style AZ-1 fill:#e3f2fd
    style AZ-2 fill:#e8f5e9
    style AZ-3 fill:#fff9c4
```

**Result**: If entire AZ-1 goes down (33% of servers), system still operates at 67% capacity ✅

---

### Health Checks & Auto-Recovery

```javascript
// Health check endpoint
app.get('/health', async (req, res) => {
  const checks = {
    status: 'healthy',
    timestamp: new Date().toISOString(),
    checks: {}
  };

  // Check database connection
  try {
    await db.query('SELECT 1');
    checks.checks.database = 'ok';
  } catch (err) {
    checks.checks.database = 'failed';
    checks.status = 'unhealthy';
  }

  // Check Redis connection
  try {
    await redis.ping();
    checks.checks.redis = 'ok';
  } catch (err) {
    checks.checks.redis = 'failed';
    checks.status = 'unhealthy';
  }

  // Check disk space
  const diskUsage = await checkDiskUsage();
  if (diskUsage > 90) {
    checks.checks.disk = 'warning';
    checks.status = 'degraded';
  } else {
    checks.checks.disk = 'ok';
  }

  const statusCode = checks.status === 'healthy' ? 200 : 503;
  res.status(statusCode).json(checks);
});

// Load balancer checks /health every 30 seconds
// 2 consecutive failures → remove server from pool
// Server auto-restarts → added back to pool
```

---

### Circuit Breaker Pattern

```
Problem: Database is slow → All app servers wait → All users affected!

Solution: Circuit Breaker (stop sending requests to failing service)

States:
1. CLOSED (normal): Requests go through
2. OPEN (failure detected): Requests fail fast, don't wait
3. HALF-OPEN (testing recovery): Try 1 request, if success → CLOSED

Like a home circuit breaker: Electrical fault → breaker trips → prevents fire
```

**Implementation**:
```javascript
class CircuitBreaker {
  constructor(threshold = 5, timeout = 60000) {
    this.state = 'CLOSED';
    this.failureCount = 0;
    this.threshold = threshold;
    this.timeout = timeout;
  }

  async execute(operation) {
    if (this.state === 'OPEN') {
      throw new Error('Circuit breaker is OPEN - service unavailable');
    }

    try {
      const result = await operation();
      this.onSuccess();
      return result;
    } catch (error) {
      this.onFailure();
      throw error;
    }
  }

  onSuccess() {
    this.failureCount = 0;
    if (this.state === 'HALF-OPEN') {
      this.state = 'CLOSED';  // Recovery confirmed
      console.log('Circuit breaker CLOSED - service recovered');
    }
  }

  onFailure() {
    this.failureCount++;
    if (this.failureCount >= this.threshold) {
      this.state = 'OPEN';
      console.log('Circuit breaker OPEN - too many failures');

      // After timeout, try again (HALF-OPEN)
      setTimeout(() => {
        this.state = 'HALF-OPEN';
        console.log('Circuit breaker HALF-OPEN - testing recovery');
      }, this.timeout);
    }
  }
}

// Usage
const dbCircuitBreaker = new CircuitBreaker(5, 60000);

async function queryDatabase(sql) {
  return dbCircuitBreaker.execute(async () => {
    return await db.query(sql);
  });
}
```

---

### Graceful Degradation

```
All systems fail eventually. Plan for it!

Level 1 (Normal):
✅ All features work
✅ Feed loads in 15ms
✅ Photos uploaded and resized

Level 2 (Degraded - Redis cache down):
✅ Feed still works (query DB directly, slower: 120ms)
✅ Photos uploaded (slower, no cache)
⚠️ Performance degraded, but functional

Level 3 (Critical - Database replicas down):
✅ Reads from master (slower, but works)
✅ Writes still work
⚠️ Read latency 3x higher

Level 4 (Emergency - Search down):
✅ Core features work (feed, upload, like)
❌ Search unavailable (show cached results or disable)
⚠️ Non-critical feature disabled
```

**Implementation**:
```javascript
async function getUserFeed(userId) {
  try {
    // Try cache first (fastest)
    return await getFeedFromCache(userId);
  } catch (err) {
    console.warn('Cache failed, falling back to database');

    try {
      // Fallback to database (slower)
      return await getFeedFromDatabase(userId);
    } catch (err) {
      console.error('Database failed, returning stale feed');

      // Last resort: Return stale feed from backup cache
      return await getStaleBackupFeed(userId);
    }
  }
}
```

---

## Security

### Authentication & Authorization

#### JWT (JSON Web Token) Authentication

```
How it works:
1. User logs in with email/password
2. Server verifies → Generates JWT token (signed)
3. Client stores token (secure storage)
4. Client includes token in all requests (Authorization header)
5. Server validates signature → Extracts user_id → Processes request
```

**JWT Token Structure**:
```javascript
// Header
{
  "alg": "HS256",  // HMAC SHA-256 algorithm
  "typ": "JWT"
}

// Payload
{
  "user_id": 123,
  "username": "john_doe",
  "iat": 1704067200,  // Issued at
  "exp": 1704070800   // Expires in 1 hour
}

// Signature
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  SECRET_KEY
)

// Final token:
// eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoxMjMsInVzZXJuYW1lIjoiam9obl9kb2UiLCJpYXQiOjE3MDQwNjcyMDAsImV4cCI6MTcwNDA3MDgwMH0.signature_here
```

**Verification**:
```javascript
const jwt = require('jsonwebtoken');

function verifyToken(token) {
  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    return decoded;  // {user_id: 123, username: "john_doe", ...}
  } catch (err) {
    if (err.name === 'TokenExpiredError') {
      throw new Error('Token expired - please login again');
    }
    throw new Error('Invalid token');
  }
}
```

---

### Password Security

```javascript
const bcrypt = require('bcrypt');

// Registration: Hash password before storing
async function registerUser(username, password) {
  // Generate salt (random data)
  const salt = await bcrypt.genSalt(10);  // 10 rounds (2^10 = 1024 iterations)

  // Hash password with salt
  const passwordHash = await bcrypt.hash(password, salt);
  // Result: $2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
  //         ↑    ↑  ↑                                                       ↑
  //      algo rounds salt (22 chars)                                    hash (31 chars)

  // Store hash in database (NOT plaintext password!)
  await db.query('INSERT INTO users (username, password_hash) VALUES ($1, $2)',
    [username, passwordHash]);
}

// Login: Compare input password with hash
async function loginUser(username, password) {
  const user = await db.query('SELECT password_hash FROM users WHERE username = $1',
    [username]);

  if (!user) {
    throw new Error('User not found');
  }

  // Compare (automatically extracts salt from hash)
  const isValid = await bcrypt.compare(password, user.password_hash);

  if (!isValid) {
    throw new Error('Invalid password');
  }

  return generateJWT(user);
}
```

---

### Rate Limiting (Prevent Abuse)

```javascript
// Redis-based rate limiting
class RateLimiter {
  constructor(redis) {
    this.redis = redis;
  }

  async checkLimit(userId, endpoint, limit, windowSeconds) {
    const key = `ratelimit:${userId}:${endpoint}`;

    // Increment counter
    const count = await this.redis.incr(key);

    // Set expiry on first request
    if (count === 1) {
      await this.redis.expire(key, windowSeconds);
    }

    if (count > limit) {
      const ttl = await this.redis.ttl(key);
      throw new RateLimitError(`Rate limit exceeded. Try again in ${ttl} seconds`);
    }

    return {
      limit,
      remaining: limit - count,
      reset: Date.now() + (await this.redis.ttl(key)) * 1000
    };
  }
}

// Usage
app.post('/api/v1/posts', async (req, res) => {
  try {
    // Max 10 posts per hour
    await rateLimiter.checkLimit(req.userId, 'create_post', 10, 3600);

    // Create post...
  } catch (err) {
    if (err instanceof RateLimitError) {
      res.status(429).json({ error: err.message });
    }
  }
});
```

**Rate Limits for Instagram**:
```
Action               | Limit      | Window
---------------------|------------|--------
Login attempts       | 5          | 15 min
Post creation        | 10         | 1 hour
Follow/Unfollow      | 60         | 1 hour
Likes                | 350        | 1 hour
Comments             | 60         | 1 hour
API requests (total) | 5000       | 1 hour
```

---

### Input Validation (Prevent Attacks)

```javascript
const validator = require('validator');
const sanitizeHtml = require('sanitize-html');

function validatePostInput(data) {
  const errors = [];

  // Caption validation
  if (!data.caption || typeof data.caption !== 'string') {
    errors.push('Caption is required and must be a string');
  } else if (data.caption.length > 2200) {
    errors.push('Caption cannot exceed 2200 characters');
  }

  // Sanitize HTML (prevent XSS attacks)
  data.caption = sanitizeHtml(data.caption, {
    allowedTags: [],  // Strip all HTML tags
    allowedAttributes: {}
  });

  // Media validation
  if (!Array.isArray(data.media) || data.media.length === 0) {
    errors.push('At least one media file is required');
  } else if (data.media.length > 10) {
    errors.push('Maximum 10 media files per post');
  }

  // Validate media types
  const allowedTypes = ['image/jpeg', 'image/png', 'video/mp4'];
  data.media.forEach((file, i) => {
    if (!allowedTypes.includes(file.mimetype)) {
      errors.push(`Media ${i + 1}: Invalid type. Allowed: JPEG, PNG, MP4`);
    }

    if (file.mimetype.startsWith('image/') && file.size > 10 * 1024 * 1024) {
      errors.push(`Media ${i + 1}: Image too large (max 10MB)`);
    }

    if (file.mimetype.startsWith('video/') && file.size > 100 * 1024 * 1024) {
      errors.push(`Media ${i + 1}: Video too large (max 100MB)`);
    }
  });

  if (errors.length > 0) {
    throw new ValidationError(errors);
  }

  return data;  // Sanitized and validated
}
```

---

### SQL Injection Prevention

```javascript
// ❌ BAD: Vulnerable to SQL injection
async function getUserByUsername(username) {
  const query = `SELECT * FROM users WHERE username = '${username}'`;
  // Attacker input: username = "' OR '1'='1"
  // Resulting query: SELECT * FROM users WHERE username = '' OR '1'='1'
  // Returns ALL users! 💀

  return db.query(query);
}

// ✅ GOOD: Use parameterized queries
async function getUserByUsername(username) {
  const query = 'SELECT * FROM users WHERE username = $1';
  // Database automatically escapes input
  // Attacker input treated as literal string, not SQL code ✅

  return db.query(query, [username]);
}
```

---

### Encryption

#### Encryption at Rest (Stored Data)

```
AWS S3: Server-side encryption (AES-256)
- All media files encrypted automatically
- Instagram doesn't manage encryption keys (AWS does)

PostgreSQL: Transparent Data Encryption (TDE)
- Database files encrypted on disk
- Queries decrypt data automatically (transparent to app)
```

#### Encryption in Transit (Network)

```
HTTPS/TLS: All API requests encrypted

Client → Load Balancer: TLS 1.3
Load Balancer → App Server: TLS 1.2 (internal network)
App Server → Database: TLS 1.2 (encrypted connection)
```

---

## Monitoring & Alerting

### Metrics to Track

```yaml
Application Metrics:
  - Request rate (req/sec)
  - Error rate (errors/sec, %)
  - Latency (p50, p95, p99)
  - Active users (current DAU)

Infrastructure Metrics:
  - CPU utilization (%)
  - Memory utilization (%)
  - Disk I/O (MB/sec)
  - Network I/O (MB/sec)

Database Metrics:
  - Queries per second (QPS)
  - Slow queries (> 1 second)
  - Connection pool utilization (%)
  - Replication lag (seconds)

Cache Metrics:
  - Hit rate (%)
  - Evictions per second
  - Memory utilization (%)

Business Metrics:
  - Posts created per day
  - DAU (Daily Active Users)
  - Engagement rate (likes, comments)
  - Upload success rate (%)
```

---

### Alerting Rules

```yaml
# Prometheus alerting rules

groups:
  - name: instagram_critical
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 5m
        annotations:
          summary: "High error rate detected"
          description: "{{ $value }}% of requests are failing"
        labels:
          severity: critical
          page: ops-team

      - alert: DatabaseDown
        expr: up{job="postgresql"} == 0
        for: 1m
        annotations:
          summary: "PostgreSQL database is down"
        labels:
          severity: critical
          page: ops-team

      - alert: HighLatency
        expr: http_request_duration_seconds{quantile="0.99"} > 1
        for: 5m
        annotations:
          summary: "p99 latency > 1 second"
          description: "99% of requests taking > 1s"
        labels:
          severity: warning

      - alert: LowCacheHitRate
        expr: redis_cache_hit_rate < 0.80
        for: 10m
        annotations:
          summary: "Redis cache hit rate below 80%"
        labels:
          severity: warning
```

---

## Disaster Recovery

### Backup Strategy

```
PostgreSQL:
- Automated snapshots: Every 6 hours
- Retained for: 30 days
- Point-in-time recovery: Any time within last 30 days

S3 Media:
- Versioning enabled: Keep all versions of files
- Cross-region replication: Copy to us-west-2 (disaster recovery)
- Glacier backup: Archive to Glacier after 90 days

Redis:
- RDB snapshots: Every 1 hour
- AOF (Append-Only File): Every second
- Can restore to within 1 second of crash
```

---

### Multi-Region Strategy

```
Primary Region: us-east-1 (Virginia)
- Handles 100% of traffic
- All writes go here

Backup Region: us-west-2 (Oregon)
- Real-time replication of data
- Standby mode (no traffic)
- Activates if us-east-1 fails

Failover Process:
1. Detect us-east-1 failure (health checks fail)
2. Update DNS to point to us-west-2 (5-minute TTL)
3. Promote us-west-2 databases from replica to master
4. Resume operations in us-west-2

Recovery Time Objective (RTO): 15 minutes
Recovery Point Objective (RPO): 0 (no data loss, real-time replication)
```

---

## Key Takeaways

1. **Horizontal scaling** = Add more servers (nearly unlimited)
2. **Eliminate SPOFs** = Redundancy at every layer
3. **Multi-AZ deployment** = Survive data center failures
4. **Circuit breakers** = Prevent cascade failures
5. **Graceful degradation** = Core features work even when others fail
6. **JWT authentication** = Stateless, scalable auth
7. **Rate limiting** = Prevent abuse, ensure fair use
8. **Input validation** = Prevent XSS, SQL injection
9. **Encryption everywhere** = At rest and in transit
10. **Monitoring & alerting** = Detect issues before users notice
11. **Disaster recovery** = Multi-region, automated backups

