# Scalability, Reliability & Security

## Scalability Strategies

### 1. Horizontal Scaling (Scale Out)

**Application Servers:**
```
Current: 9 servers (7,000 req/sec peak)
10x growth: 90 servers (70,000 req/sec)

Auto-scaling policy:
- Scale up: CPU > 70% for 2 minutes → add 3 servers
- Scale down: CPU < 30% for 5 minutes → remove 1 server
- Min servers: 5
- Max servers: 100
```

**Database Read Replicas:**
```
Current: 3 replicas (30,000 reads/sec)
10x growth: 10 replicas (100,000 reads/sec)

Distribution:
- Replica 1-3: General read queries
- Replica 4-6: Search queries (Elasticsearch sync)
- Replica 7-8: Analytics queries (heavy, isolated)
- Replica 9-10: Reporting (non-critical)
```

**Worker Scaling:**
```
Email workers: Scale based on queue depth
- Queue < 100: 2 workers
- Queue 100-1000: 5 workers
- Queue > 1000: 10 workers

Inventory workers: Scale based on order volume
- < 1000 orders/hour: 2 workers
- 1000-5000 orders/hour: 5 workers
- > 5000 orders/hour: 10 workers
```

---

### 2. Vertical Scaling (Scale Up)

**When to use:**
- Database master (needs high IOPS, memory)
- Redis cache (needs large memory)

**Example:**
```
Current master: db.r6g.2xlarge (8 vCPU, 64 GB RAM) - $500/mo
10x growth: db.r6g.8xlarge (32 vCPU, 256 GB RAM) - $2,000/mo
```

**Limits:**
- ⚠️ Single point of failure (still need replication)
- ⚠️ Maximum size (can't scale beyond largest instance)
- ⚠️ Expensive (cost grows exponentially)

---

### 3. Caching at Multiple Levels

```
L1 Cache (Browser):
- Static assets (CSS, JS): Cache-Control: max-age=31536000
- Product images: Cache-Control: max-age=86400

L2 Cache (CDN):
- Hit rate: 90%
- Latency: 20ms
- Cost: $0.02/GB

L3 Cache (Redis):
- Hit rate: 80%
- Latency: 10ms
- Cost: $0.50/GB-month

L4 Cache (Application memory):
- Hot data (top 1000 products)
- Hit rate: 95%
- Latency: 1ms
- Cost: Free (in-memory)
```

---

## High Availability

### Target: 99.99% Uptime

**Calculation:**
```
99.99% = (365 × 24 × 60) - downtime = 525,600 - downtime
Downtime allowed: 52.56 minutes/year
```

**How to achieve:**

**1. Eliminate Single Points of Failure**
```
Component          | Single Instance | High Availability
-------------------|-----------------|-------------------
Load Balancer      | ❌ SPOF         | ✅ 2+ LBs (active-passive)
App Servers        | ❌ SPOF         | ✅ 9+ servers (auto-scaling)
Database Master    | ❌ SPOF         | ✅ Multi-AZ (master + standby)
Redis Cache        | ❌ SPOF         | ✅ Redis Cluster (3 masters + 3 slaves)
Message Queue      | ❌ SPOF         | ✅ RabbitMQ Cluster (3 nodes)
```

**2. Multi-Region Deployment**
```
Primary Region (US-East):
- Handles 80% of traffic
- Full stack (apps, databases, cache)

Secondary Region (US-West):
- Handles 20% of traffic (West coast users)
- Full stack (apps, databases, cache)
- Can handle 100% if primary fails

Failover:
- DNS failover (Route53 health checks)
- Automated: 5 minutes
- Manual: 15 minutes (if automation fails)
```

**3. Health Checks & Monitoring**
```javascript
// Health check endpoint
app.get('/health', async (req, res) => {
    const checks = {
        database: await checkDatabase(),
        redis: await checkRedis(),
        queue: await checkRabbitMQ(),
        disk: checkDiskSpace(),
        memory: checkMemory()
    };

    const allHealthy = Object.values(checks).every(c => c.healthy);

    res.status(allHealthy ? 200 : 503).json({
        status: allHealthy ? 'healthy' : 'unhealthy',
        checks,
        timestamp: new Date().toISOString()
    });
});
```

---

## Reliability Patterns

### 1. Circuit Breaker

**Problem:** If payment service is down, don't keep trying (wastes resources)

**Solution:** Circuit breaker pattern

```javascript
class CircuitBreaker {
    constructor() {
        this.state = 'CLOSED';  // CLOSED, OPEN, HALF_OPEN
        this.failureCount = 0;
        this.successCount = 0;
        this.threshold = 5;
        this.timeout = 60000;  // 1 minute
    }

    async call(fn) {
        if (this.state === 'OPEN') {
            throw new Error('Circuit breaker is OPEN');
        }

        try {
            const result = await fn();
            this.onSuccess();
            return result;
        } catch (error) {
            this.onFailure();
            throw error;
        }
    }

    onSuccess() {
        this.failureCount = 0;
        if (this.state === 'HALF_OPEN') {
            this.state = 'CLOSED';
        }
    }

    onFailure() {
        this.failureCount++;
        if (this.failureCount >= this.threshold) {
            this.state = 'OPEN';
            setTimeout(() => {
                this.state = 'HALF_OPEN';  // Try again after timeout
            }, this.timeout);
        }
    }
}

// Usage
const paymentCircuit = new CircuitBreaker();

async function processPayment(orderId, amount) {
    return paymentCircuit.call(async () => {
        return await paymentService.charge(orderId, amount);
    });
}
```

---

### 2. Retry with Exponential Backoff

```javascript
async function retryWithBackoff(fn, maxRetries = 3) {
    for (let i = 0; i < maxRetries; i++) {
        try {
            return await fn();
        } catch (error) {
            if (i === maxRetries - 1) throw error;

            const delay = Math.pow(2, i) * 1000;  // 1s, 2s, 4s
            console.log(`Retry ${i + 1} after ${delay}ms`);
            await new Promise(resolve => setTimeout(resolve, delay));
        }
    }
}
```

---

### 3. Graceful Degradation

**Example:** If recommendation service is down, show trending products instead

```javascript
async function getRecommendations(userId) {
    try {
        // Try personalized recommendations
        return await recommendationService.getPersonalized(userId);
    } catch (error) {
        console.warn('Recommendation service down, falling back to trending');
        // Fallback: show trending products (from cache)
        return await cache.get('trending_products');
    }
}
```

---

## Security Architecture

### 1. Authentication & Authorization

**JWT-based Authentication:**
```javascript
// Login
async function login(email, password) {
    const user = await db.query('SELECT * FROM users WHERE email = $1', [email]);
    const valid = await bcrypt.compare(password, user.password_hash);
    if (!valid) throw new Error('Invalid credentials');

    // Generate short-lived access token
    const accessToken = jwt.sign(
        { userId: user.id, role: user.role },
        process.env.JWT_SECRET,
        { expiresIn: '15m' }
    );

    // Generate long-lived refresh token
    const refreshToken = jwt.sign(
        { userId: user.id },
        process.env.REFRESH_SECRET,
        { expiresIn: '7d' }
    );

    // Store refresh token in database
    await db.query('INSERT INTO refresh_tokens (user_id, token) VALUES ($1, $2)',
        [user.id, refreshToken]);

    return { accessToken, refreshToken };
}
```

**Authorization Middleware:**
```javascript
function authorize(roles) {
    return (req, res, next) => {
        const token = req.headers.authorization?.split(' ')[1];
        if (!token) return res.status(401).json({ error: 'Unauthorized' });

        try {
            const decoded = jwt.verify(token, process.env.JWT_SECRET);
            if (!roles.includes(decoded.role)) {
                return res.status(403).json({ error: 'Forbidden' });
            }
            req.user = decoded;
            next();
        } catch (error) {
            return res.status(401).json({ error: 'Invalid token' });
        }
    };
}

// Usage
app.post('/api/v1/products', authorize(['admin', 'seller']), createProduct);
app.delete('/api/v1/users/:id', authorize(['admin']), deleteUser);
```

---

### 2. Data Encryption

**Encryption at Rest:**
```
Database: AES-256 encryption (RDS encryption)
S3: Server-side encryption (SSE-S3 or SSE-KMS)
Backups: Encrypted before upload
```

**Encryption in Transit:**
```
All API calls: TLS 1.3
Database connections: SSL/TLS
Redis: TLS (optional, recommended)
```

---

### 3. API Security

**Rate Limiting:**
```javascript
const rateLimit = require('express-rate-limit');

// Per-user rate limit
const userLimiter = rateLimit({
    windowMs: 60 * 1000,  // 1 minute
    max: 100,  // 100 requests per minute
    keyGenerator: (req) => req.user.userId,
    message: 'Too many requests, please try again later'
});

// Per-IP rate limit (for unauthenticated)
const ipLimiter = rateLimit({
    windowMs: 60 * 1000,
    max: 20,
    keyGenerator: (req) => req.ip
});
```

**Input Validation:**
```javascript
const { body, validationResult } = require('express-validator');

app.post('/api/v1/products',
    [
        body('name').isLength({ min: 3, max: 500 }),
        body('price').isFloat({ min: 0 }),
        body('category').isInt(),
    ],
    (req, res) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ errors: errors.array() });
        }
        // Process request
    }
);
```

**SQL Injection Prevention:**
```javascript
// ❌ BAD: Vulnerable to SQL injection
app.get('/products', (req, res) => {
    const query = `SELECT * FROM products WHERE name = '${req.query.name}'`;
    db.query(query);  // NEVER DO THIS!
});

// ✅ GOOD: Use parameterized queries
app.get('/products', (req, res) => {
    db.query('SELECT * FROM products WHERE name = $1', [req.query.name]);
});
```

---

### 4. PCI DSS Compliance (Payment Security)

**Never store:**
- ❌ Full credit card number (use tokenization)
- ❌ CVV/CVC code
- ❌ PIN

**Tokenization:**
```javascript
// Create payment method (via Stripe)
async function addPaymentMethod(userId, cardDetails) {
    // Send card to Stripe, get token
    const token = await stripe.tokens.create({
        card: {
            number: cardDetails.number,
            exp_month: cardDetails.expMonth,
            exp_year: cardDetails.expYear,
            cvc: cardDetails.cvc
        }
    });

    // Store only token (never store raw card!)
    await db.query(
        'INSERT INTO payment_methods (user_id, token, last_four) VALUES ($1, $2, $3)',
        [userId, token.id, cardDetails.number.slice(-4)]
    );

    return { tokenId: token.id, lastFour: cardDetails.number.slice(-4) };
}
```

---

## Monitoring & Alerting

### Key Metrics

**Application Metrics:**
```
- Request rate (req/sec)
- Response time (p50, p95, p99)
- Error rate (4xx, 5xx)
- Throughput (MB/sec)
```

**Infrastructure Metrics:**
```
- CPU utilization (%)
- Memory usage (%)
- Disk I/O (IOPS)
- Network bandwidth (Gbps)
```

**Business Metrics:**
```
- Orders per minute
- Revenue per hour
- Cart abandonment rate
- Conversion rate
```

### Alerting

```yaml
# Prometheus alert rules
groups:
  - name: api_alerts
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 5m
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }}% for the last 5 minutes"

      - alert: DatabaseSlowQueries
        expr: mysql_slow_queries > 10
        for: 2m
        annotations:
          summary: "Database slow queries detected"

      - alert: QueueBacklog
        expr: rabbitmq_queue_messages{queue="email"} > 10000
        annotations:
          summary: "Email queue has {{ $value }} pending messages"
```

---

**Previous**: [10_data_flow_diagrams.md](./10_data_flow_diagrams.md)
**Next**: [12_security_architecture.md](./12_security_architecture.md)
