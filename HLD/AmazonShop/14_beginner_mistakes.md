# Common Beginner Mistakes & Solutions

## Mistake 1: Not Considering Scalability Upfront

### ❌ Wrong Approach

```javascript
// Single database server
const db = new Database('localhost:5432');

// No caching
app.get('/products/:id', async (req, res) => {
    const product = await db.query('SELECT * FROM products WHERE id = $1', [req.params.id]);
    res.json(product);
});

// Synchronous file uploads (blocks server)
app.post('/upload', (req, res) => {
    const file = req.file;
    fs.writeFileSync(`/uploads/${file.name}`, file.data);  // Blocks!
    res.json({ uploaded: true });
});
```

**Problems:**
- Single database can handle only 10,000 QPS (we need 70,000)
- No caching → every request hits database
- File uploads block Node.js event loop

---

### ✅ Right Solution

```javascript
// Connection pool with read replicas
const masterDB = new Pool({ host: 'master.db.internal', max: 20 });
const replicaDBs = [
    new Pool({ host: 'replica1.db.internal', max: 20 }),
    new Pool({ host: 'replica2.db.internal', max: 20 })
];

// Caching layer
const redis = new Redis({ host: 'redis.internal' });

app.get('/products/:id', async (req, res) => {
    // Check cache first
    const cached = await redis.get(`product:${req.params.id}`);
    if (cached) return res.json(JSON.parse(cached));

    // Cache miss: query database (use replica for reads)
    const replica = replicaDBs[Math.floor(Math.random() * replicaDBs.length)];
    const product = await replica.query('SELECT * FROM products WHERE id = $1', [req.params.id]);

    // Cache for 1 hour
    await redis.setex(`product:${req.params.id}`, 3600, JSON.stringify(product));

    res.json(product);
});

// Async file upload to S3
app.post('/upload', async (req, res) => {
    const file = req.file;

    // Upload to S3 (async, doesn't block)
    const s3Url = await s3.upload({
        Bucket: 'amazonshop-uploads',
        Key: file.name,
        Body: file.data
    }).promise();

    res.json({ url: s3Url.Location });
});
```

**Benefits:**
- ✅ Read replicas scale reads to 30,000 QPS
- ✅ Caching reduces database load by 80%
- ✅ Async uploads don't block server

---

## Mistake 2: Over-Engineering (Premature Optimization)

### ❌ Wrong Approach

```
Day 1 of startup (0 users):
- Implement Kubernetes with 50 microservices
- Set up Kafka, Cassandra, Elasticsearch, Redis
- Implement complex sharding across 10 databases
- Write custom load balancer in Go
```

**Problems:**
- Waste 6 months building infrastructure (no users yet!)
- Operational complexity (need 5 DevOps engineers)
- High costs ($50,000/month for 0 users)

---

### ✅ Right Solution

```
Day 1 (MVP):
- Monolith application (Node.js/Rails/Django)
- Single PostgreSQL database (RDS)
- Deploy to Heroku/Vercel (managed platform)
- Total cost: $100/month

Month 3 (1,000 users):
- Still monolith
- Add caching (Redis)
- Add CDN (CloudFlare)
- Cost: $500/month

Month 12 (100,000 users):
- Split into 3-5 microservices
- Add read replicas (3)
- Add message queue (RabbitMQ)
- Cost: $5,000/month

Year 2 (1M+ users):
- Full microservices architecture
- Database sharding
- Multi-region deployment
- Cost: $50,000/month
```

**Principle:** Start simple, scale when needed!

---

## Mistake 3: Not Planning for Failures

### ❌ Wrong Approach

```javascript
// No error handling
async function placeOrder(userId, items, total) {
    const order = await db.query('INSERT INTO orders VALUES (...)');
    await paymentService.charge(total);  // What if this fails?
    await emailService.send(userId);     // What if this fails?
    return order;
}

// No retry logic
async function sendEmail(userId) {
    await emailAPI.send({ to: user.email });  // Fails silently
}

// No health checks
app.listen(3000);  // Server may crash, load balancer keeps sending traffic
```

**Problems:**
- Payment fails → order created but not paid ❌
- Email fails → user never notified ❌
- Server crashes → traffic sent to dead server ❌

---

### ✅ Right Solution

```javascript
// Transaction with rollback
async function placeOrder(userId, items, total) {
    const client = await db.connect();

    try {
        await client.query('BEGIN');

        const order = await client.query('INSERT INTO orders VALUES (...)');

        // Charge payment (within transaction)
        const payment = await paymentService.charge(total);
        if (!payment.success) {
            throw new Error('Payment failed');
        }

        await client.query('INSERT INTO payments VALUES (...)', [payment.id]);
        await client.query('COMMIT');

        // Async email (fire and forget, retries handled by worker)
        await queue.publish('order.placed', { orderId: order.id, userId });

        return order;

    } catch (error) {
        await client.query('ROLLBACK');  // Undo order creation
        throw error;
    } finally {
        client.release();
    }
}

// Retry logic with exponential backoff
async function sendEmailWithRetry(userId, maxRetries = 3) {
    for (let i = 0; i < maxRetries; i++) {
        try {
            await emailAPI.send({ to: user.email });
            return;  // Success
        } catch (error) {
            if (i === maxRetries - 1) {
                // Move to dead letter queue
                await queue.publish('email.failed', { userId, error });
                throw error;
            }
            await sleep(Math.pow(2, i) * 1000);  // 1s, 2s, 4s
        }
    }
}

// Health check endpoint
app.get('/health', async (req, res) => {
    try {
        await db.query('SELECT 1');  // Check database connection
        await redis.ping();          // Check Redis connection
        res.status(200).json({ status: 'healthy' });
    } catch (error) {
        res.status(503).json({ status: 'unhealthy', error: error.message });
    }
});

app.listen(3000);
```

**Benefits:**
- ✅ ACID transactions prevent data corruption
- ✅ Retry logic handles transient failures
- ✅ Health checks enable automatic recovery

---

## Mistake 4: Ignoring Consistency Requirements

### ❌ Wrong Approach

```javascript
// Use NoSQL for financial data (eventually consistent)
const ordersDB = new MongoDB();

async function placeOrder(userId, items, total) {
    // Insert order
    await ordersDB.insert({ userId, items, total, status: 'PENDING' });

    // Update inventory (separate operation, no transaction)
    await ordersDB.update({ productId: items[0].id }, { $inc: { stock: -1 } });

    // What if inventory update fails? ❌
    // What if we oversell (2 users buy last item)? ❌
}
```

**Problems:**
- No ACID → data corruption
- Race conditions → overselling
- No referential integrity → orphaned records

---

### ✅ Right Solution

```javascript
// Use SQL for financial data (strong consistency)
const db = new PostgreSQL();

async function placeOrder(userId, items, total) {
    const client = await db.connect();

    try {
        await client.query('BEGIN');

        // Lock inventory row (prevent overselling)
        const inventory = await client.query(
            'SELECT stock FROM products WHERE id = $1 FOR UPDATE',
            [items[0].id]
        );

        if (inventory.rows[0].stock < items[0].quantity) {
            throw new Error('Out of stock');
        }

        // Insert order
        await client.query('INSERT INTO orders (user_id, total, status) VALUES ($1, $2, $3)',
            [userId, total, 'PENDING']);

        // Update inventory
        await client.query('UPDATE products SET stock = stock - $1 WHERE id = $2',
            [items[0].quantity, items[0].id]);

        await client.query('COMMIT');

    } catch (error) {
        await client.query('ROLLBACK');
        throw error;
    } finally {
        client.release();
    }
}
```

**When to use SQL vs NoSQL:**

| Data Type | Database | Reason |
|-----------|----------|--------|
| Orders | ✅ SQL | ACID, transactions, no data loss |
| Payments | ✅ SQL | ACID, exactly-once processing |
| Inventory | ✅ SQL | Strong consistency (prevent overselling) |
| User profiles | ✅ SQL | Relationships (addresses, payment methods) |
| Product reviews | ⚠️ NoSQL | High write volume, eventual consistency OK |
| User activity logs | ⚠️ NoSQL | Append-only, time-series |
| Analytics | ⚠️ NoSQL | Aggregations, no JOINs needed |

---

## Mistake 5: Not Estimating Capacity

### ❌ Wrong Approach

```
Developer: "We'll use AWS, it scales automatically!"
```

**No calculations:**
- How many requests/sec?
- How much storage?
- How much bandwidth?
- How much will it cost?

**Result:** Launch day arrives, system crashes under load!

---

### ✅ Right Solution

**Calculate Everything:**

```
Users:
- 10 million DAU (daily active users)
- 2 million peak concurrent users

Traffic:
- 20 actions per user per day
- 200M requests/day
- 2,315 requests/sec (average)
- 7,000 requests/sec (peak, 3x average)

Servers needed:
- 1 server handles 1,000 req/sec
- Need 7 servers minimum (peak)
- Add 30% buffer → 9 servers

Storage:
- 100M users × 2 KB = 200 GB (user data)
- 500M products × 5 KB = 2.5 TB (products)
- 5M orders/day × 3 KB × 365 = 5.4 TB/year (orders)
- Total: 8 TB (year 1)

Bandwidth:
- Images: 500 KB × 3 per page × 7,000 users/sec = 10 GB/sec
- With CDN (90% cache hit): 1 GB/sec origin bandwidth
- Cost: 1 GB/sec × 86400 sec/day × 30 days × $0.08/GB = $207,360/mo

Budget: $250,000/month (infrastructure)
```

---

## Mistake 6: Storing Sensitive Data Insecurely

### ❌ Wrong Approach

```javascript
// Store passwords in plaintext
CREATE TABLE users (
    user_id INT,
    email VARCHAR(255),
    password VARCHAR(255)  -- 🚨 NEVER DO THIS!
);

INSERT INTO users VALUES (1, 'user@example.com', 'password123');

// Store credit cards in database
CREATE TABLE payment_methods (
    card_number VARCHAR(16),  -- 🚨 NEVER DO THIS! (PCI DSS violation)
    cvv VARCHAR(3),           -- 🚨 NEVER STORE CVV!
    expires DATE
);
```

**Consequences:**
- Database leak → all passwords exposed
- Credit card leak → PCI DSS fines ($50,000-$500,000)
- Lawsuits, loss of customer trust

---

### ✅ Right Solution

```javascript
// Hash passwords (bcrypt)
const bcrypt = require('bcrypt');

async function registerUser(email, password) {
    const passwordHash = await bcrypt.hash(password, 10);  // 10 rounds

    await db.query(
        'INSERT INTO users (email, password_hash) VALUES ($1, $2)',
        [email, passwordHash]
    );
}

async function loginUser(email, password) {
    const user = await db.query('SELECT * FROM users WHERE email = $1', [email]);
    const valid = await bcrypt.compare(password, user.password_hash);

    if (!valid) throw new Error('Invalid credentials');
    return user;
}

// Tokenize credit cards (Stripe)
async function addPaymentMethod(userId, cardDetails) {
    // Send card to Stripe, get token (card never touches our server!)
    const token = await stripe.tokens.create({
        card: {
            number: cardDetails.number,
            exp_month: cardDetails.expMonth,
            exp_year: cardDetails.expYear,
            cvc: cardDetails.cvc
        }
    });

    // Store only token (never store raw card number!)
    await db.query(
        'INSERT INTO payment_methods (user_id, token, last_four) VALUES ($1, $2, $3)',
        [userId, token.id, cardDetails.number.slice(-4)]
    );
}
```

**Security Checklist:**
- ✅ Passwords: bcrypt/argon2 (never plaintext)
- ✅ Credit cards: Tokenization (Stripe, PayPal)
- ✅ API keys: Environment variables (never hardcoded)
- ✅ Database: Encryption at rest (AES-256)
- ✅ Communication: TLS 1.3 (HTTPS everywhere)

---

**Previous**: [13_interview_qa.md](./13_interview_qa.md)
**Next**: [15_summary_and_checklist.md](./15_summary_and_checklist.md)
