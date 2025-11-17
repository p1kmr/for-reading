# Interview Questions & Answers

## System Design Interview Questions

### Q1: How would you scale Amazon Shop to handle 10x more traffic?

**Answer:**

**Current:** 7,000 req/sec peak
**Target:** 70,000 req/sec (10x)

**Scaling Strategy:**

1. **Application Layer:**
   - Scale from 9 servers to 90 servers (horizontal scaling)
   - Use auto-scaling groups (AWS ASG, Kubernetes HPA)
   - Add more load balancers (NGINX → AWS ALB with multiple instances)

2. **Database Layer:**
   - Add more read replicas: 3 → 10 replicas
   - Implement sharding for writes: 1 master → 4 sharded masters
   - Each shard handles 25% of users (shard by user_id % 4)

3. **Caching:**
   - Scale Redis cluster: 3 masters → 10 masters
   - Increase cache size: 200 GB → 1 TB
   - Add application-level caching (in-memory LRU cache)

4. **Message Queue:**
   - Scale RabbitMQ cluster: 3 nodes → 10 nodes
   - Add more workers for email/notifications
   - Use Kafka for high-throughput event streaming

5. **CDN:**
   - Already scales automatically (CloudFront)
   - Increase cache TTL for static content
   - Add more edge locations (regional caching)

**Cost:**
- Current: $25,000/month
- 10x scale: $180,000/month (economies of scale)

---

### Q2: How do you ensure 99.99% availability?

**Answer:**

**99.99% = 52 minutes downtime/year**

**Strategy:**

1. **Eliminate Single Points of Failure:**
   - Load Balancer: 2+ instances (active-passive)
   - App Servers: 9+ instances (auto-healing)
   - Database: Multi-AZ (master + standby)
   - Redis: Cluster mode (3 masters + 3 slaves)

2. **Multi-Region Deployment:**
   - Primary: US-East (handles 80% traffic)
   - Secondary: US-West (handles 20%, can do 100%)
   - Automated DNS failover (Route53 health checks)

3. **Automatic Failover:**
   - Database: 30-60 seconds (RDS Multi-AZ)
   - App Servers: < 10 seconds (k8s health checks)
   - Load Balancer: < 5 seconds (heartbeat)

4. **Monitoring & Alerting:**
   - Health checks every 10 seconds
   - Alert on: error rate > 5%, latency > 500ms, downtime
   - On-call rotation (PagerDuty)

5. **Graceful Degradation:**
   - If recommendations down → show trending products
   - If email down → queue for later (don't block orders)

---

### Q3: SQL vs NoSQL for product catalog?

**Answer:**

**Choose SQL (PostgreSQL)**

**Reasoning:**

| Factor | SQL (PostgreSQL) | NoSQL (MongoDB) |
|--------|------------------|-----------------|
| **ACID** | ✅ Required for orders/payments | ❌ Eventual consistency |
| **Relationships** | ✅ Orders → Products, Users | ⚠️ Denormalize (duplicate data) |
| **Complex Queries** | ✅ JOINs, aggregations | ❌ Limited |
| **Schema** | ✅ Enforced (data integrity) | ⚠️ Flexible (risk of inconsistency) |
| **Transactions** | ✅ Multi-table transactions | ⚠️ Limited |
| **Scalability** | ⚠️ Vertical scaling (harder) | ✅ Horizontal scaling (easier) |

**For Amazon Shop:**
- ✅ Orders require ACID (can't charge user twice)
- ✅ Complex queries (filter by category, price, rating)
- ✅ Relationships (order → product → seller)
- ⚠️ Scale reads with replicas (solves scalability)

**When to use NoSQL:**
- Product reviews (high write volume, no JOINs)
- User activity logs (append-only, time-series)
- Real-time analytics (Cassandra, ClickHouse)

---

### Q4: How do you handle database failover?

**Answer:**

**Setup: RDS Multi-AZ (PostgreSQL)**

1. **Normal State:**
   - Primary DB (AZ-A): Handles all writes + reads
   - Standby DB (AZ-B): Receives synchronous replication
   - Read Replicas (AZ-B, C): Handle read queries

2. **Failure Detection:**
   - RDS health checks every 5 seconds
   - If primary fails 3 consecutive checks (15 sec) → initiate failover

3. **Failover Process:**
   ```
   T0:    Primary DB crashes
   T15:   RDS detects failure (3 failed health checks)
   T20:   Promote standby to primary
   T30:   Update DNS (primary.db.internal → standby IP)
   T45:   Applications reconnect to new primary
   T60:   Failover complete
   
   Total downtime: 60 seconds
   ```

4. **Application Handling:**
   ```javascript
   // Use connection pool with retry logic
   const db = new Pool({
       connectionTimeoutMillis: 30000,
       maxRetries: 3
   });

   async function queryWithRetry(query, params) {
       for (let i = 0; i < 3; i++) {
           try {
               return await db.query(query, params);
           } catch (error) {
               if (i === 2) throw error;
               await sleep(1000 * Math.pow(2, i));  // Exponential backoff
           }
       }
   }
   ```

5. **Post-Failover:**
   - Old primary becomes standby (after recovery)
   - Alert ops team to investigate root cause
   - Review CloudWatch logs

---

### Q5: How do you prevent overselling (selling more stock than available)?

**Answer:**

**Problem:**
```
Stock: 1 laptop remaining
User A: Buys laptop (simultaneously)
User B: Buys laptop (simultaneously)
Result: 2 orders, 1 laptop ❌
```

**Solution: Database Transactions + Row-Level Locking**

```sql
BEGIN TRANSACTION;

-- Lock the row for update (other transactions must wait)
SELECT stock_quantity FROM products 
WHERE product_id = 12345 
FOR UPDATE;

-- Check if enough stock
-- If stock_quantity >= 1:
UPDATE products 
SET stock_quantity = stock_quantity - 1
WHERE product_id = 12345 
AND stock_quantity >= 1;

-- This returns 1 row if successful, 0 if out of stock
-- If 0 rows updated: ROLLBACK (no stock available)

INSERT INTO orders (user_id, product_id, quantity) VALUES (...);

COMMIT;
```

**Timeline:**
```
T0: User A starts transaction (locks product row)
T1: User B starts transaction (waits for lock)
T2: User A updates stock (1 → 0) and commits
T3: User B's transaction executes
T4: User B's UPDATE returns 0 rows (stock is 0)
T5: User B's transaction rolls back
T6: User B sees "Out of stock"
```

**Alternative: Optimistic Locking**
```sql
-- Add version column
ALTER TABLE products ADD COLUMN version INT DEFAULT 0;

-- Update with version check
UPDATE products 
SET stock_quantity = stock_quantity - 1,
    version = version + 1
WHERE product_id = 12345 
AND version = {current_version}
AND stock_quantity >= 1;

-- If 0 rows updated: version changed (someone else bought it)
```

---

### Q6: How do you design the recommendation system?

**Answer:**

**Architecture:**

```
Data Collection → Feature Engineering → Model Training → Model Serving
```

**1. Data Collection (Kafka):**
```javascript
// Track user events
kafka.publish('user.event', {
    userId: 1001,
    eventType: 'product_viewed',
    productId: 12345,
    timestamp: Date.now()
});

kafka.publish('user.event', {
    userId: 1001,
    eventType: 'product_purchased',
    productId: 12345,
    price: 899.99,
    timestamp: Date.now()
});
```

**2. Feature Engineering (Spark/Python):**
```python
# User features
user_features = {
    'userId': 1001,
    'age_group': '25-34',
    'location': 'US-CA',
    'purchase_history': [12345, 67890, ...],
    'avg_price': 850.0,
    'favorite_categories': ['Electronics', 'Books']
}

# Product features
product_features = {
    'productId': 12345,
    'category': 'Electronics',
    'price': 899.99,
    'rating': 4.5,
    'popularity_score': 0.85
}
```

**3. Model Training (Nightly Batch Job):**
```python
from sklearn.decomposition import NMF

# User-product interaction matrix
# Rows: users (10M), Columns: products (500M), Values: implicit feedback (1=viewed, 2=purchased)
interaction_matrix = load_interactions()  # Sparse matrix

# Collaborative filtering
model = NMF(n_components=100, init='random')
user_factors = model.fit_transform(interaction_matrix)  # (10M, 100)
product_factors = model.components_  # (100, 500M)

# Save to S3
save_model(model, 's3://amazonshop-ml/models/recommendations/2025-11-17.pkl')
```

**4. Model Serving (Real-time API):**
```python
# Flask API
@app.route('/recommend/user/<user_id>')
def get_recommendations(user_id):
    # Load user factors from Redis
    user_vector = redis.get(f'user_factors:{user_id}')
    
    # Load product factors (top 10k products)
    product_factors = load_top_products()
    
    # Compute scores
    scores = np.dot(user_vector, product_factors)
    top_products = np.argsort(scores)[::-1][:10]
    
    return jsonify({'products': top_products.tolist()})
```

**5. A/B Testing:**
```
Group A (50%): Collaborative filtering
Group B (50%): Trending products
Metric: Click-through rate (CTR)

Result: Group A has 25% higher CTR → deploy collaborative filtering
```

---

### Q7: How do you handle payment failures?

**Answer:**

**Payment Failure Scenarios:**

1. **User Insufficient Funds:**
   ```
   - Try to charge card
   - Stripe returns: insufficient_funds
   - Show user: "Payment failed. Please use a different card."
   - Don't create order (fail fast)
   ```

2. **Payment Gateway Timeout:**
   ```
   - Try to charge card
   - Stripe times out (network issue)
   - Retry 3 times with exponential backoff
   - Use idempotency key (prevent duplicate charges)
   ```

3. **Idempotent Payment Processing:**
   ```javascript
   async function processPayment(orderId, amount, idempotencyKey) {
       // Check if already processed
       const existing = await db.query(
           'SELECT * FROM payments WHERE idempotency_key = $1', 
           [idempotencyKey]
       );
       
       if (existing.rows.length > 0) {
           // Already processed, return existing result
           return existing.rows[0];
       }

       // Process payment
       const charge = await stripe.charges.create({
           amount: amount * 100,
           currency: 'usd',
           source: paymentToken,
           idempotency_key: idempotencyKey  // Stripe-level idempotency
       });

       // Save payment
       await db.query(
           'INSERT INTO payments (order_id, amount, transaction_id, idempotency_key, status) VALUES ($1, $2, $3, $4, $5)',
           [orderId, amount, charge.id, idempotencyKey, 'SUCCESS']
       );

       return charge;
   }

   // Usage
   const idempotencyKey = `order-${orderId}-${Date.now()}`;
   await processPayment(orderId, 99.99, idempotencyKey);
   ```

4. **Payment Reconciliation (Daily Job):**
   ```
   - Compare our payment records with Stripe webhooks
   - Find mismatches (e.g., successful charge but order marked as failed)
   - Alert ops team for manual investigation
   ```

---

### Q8: How do you handle a sudden traffic spike (10x)?

**Answer:**

**Scenario: Flash Sale (Black Friday) - 70,000 req/sec (10x normal)**

**1. Pre-Scale (Before Event):**
```
- 1 week before: Load test with 10x traffic
- 1 day before: Pre-warm caches (top products, search results)
- 1 hour before: Scale up app servers (9 → 50 servers)
- 30 min before: Scale up database read replicas (3 → 8)
- 15 min before: Notify CDN provider (increase bandwidth)
```

**2. Auto-Scaling (During Event):**
```
- App servers: Scale from 50 → 90 servers (based on CPU)
- Message queue workers: Scale from 5 → 20 (based on queue depth)
- Database connections: Increase pool size (20 → 100 connections per server)
```

**3. Throttling (Protect Backend):**
```javascript
// Queue system: FIFO queue
const queue = new Queue();

app.post('/api/v1/orders', async (req, res) => {
    if (queue.length > 10000) {
        return res.status(503).json({ 
            error: 'System is at capacity. Please try again in 1 minute.' 
        });
    }

    await queue.add(async () => {
        return processOrder(req.body);
    });

    res.json({ status: 'queued', estimatedTime: queue.length * 0.1 });
});
```

**4. Graceful Degradation:**
```
- Disable non-essential features (recommendations, reviews)
- Show static "Top Deals" page (cached)
- Reduce API response size (fewer fields)
```

**5. Post-Event:**
```
- Scale down gradually (avoid sudden drop)
- Analyze metrics (identify bottlenecks)
- Update runbook for next event
```

---

**Previous**: [11_scalability_reliability.md](./11_scalability_reliability.md)
**Next**: [14_beginner_mistakes.md](./14_beginner_mistakes.md)
