# Database Design

## Database Schema

### Products Database (PostgreSQL)

```sql
-- Products table
CREATE TABLE products (
    product_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    category_id INT REFERENCES categories(category_id),
    seller_id BIGINT REFERENCES sellers(seller_id),
    stock_quantity INT DEFAULT 0 CHECK (stock_quantity >= 0),
    rating DECIMAL(3,2) DEFAULT 0.0,
    review_count INT DEFAULT 0,
    image_urls TEXT[],  -- Array of image URLs
    specifications JSONB,  -- Flexible product attributes
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    -- Indexes
    INDEX idx_category (category_id),
    INDEX idx_seller (seller_id),
    INDEX idx_price (price),
    INDEX idx_rating (rating DESC),
    FULLTEXT INDEX idx_search (name, description)
);

-- Categories table
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id INT REFERENCES categories(category_id),
    level INT DEFAULT 0,  -- Hierarchy level (0=root, 1=subcategory, etc.)
    path VARCHAR(1000)    -- e.g., "Electronics > Computers > Laptops"
);

-- Product reviews
CREATE TABLE product_reviews (
    review_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(product_id),
    user_id BIGINT REFERENCES users(user_id),
    rating INT CHECK (rating BETWEEN 1 AND 5),
    title VARCHAR(255),
    comment TEXT,
    helpful_count INT DEFAULT 0,
    verified_purchase BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    
    -- Constraints
    UNIQUE (product_id, user_id),  -- One review per user per product
    INDEX idx_product (product_id),
    INDEX idx_user (user_id),
    INDEX idx_rating (rating)
);
```

### Users Database (PostgreSQL)

```sql
-- Users table
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    phone VARCHAR(20),
    role VARCHAR(50) DEFAULT 'user',  -- user, seller, admin
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    last_login_at TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- User addresses
CREATE TABLE addresses (
    address_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    label VARCHAR(50),  -- Home, Work, etc.
    address_line1 VARCHAR(500) NOT NULL,
    address_line2 VARCHAR(500),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) DEFAULT 'USA',
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    
    INDEX idx_user (user_id)
);

-- Payment methods (tokenized)
CREATE TABLE payment_methods (
    method_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    type VARCHAR(50),  -- CARD, PAYPAL, UPI
    provider VARCHAR(50),  -- STRIPE, PAYPAL
    token VARCHAR(255) NOT NULL,  -- Tokenized (never store raw card!)
    last_four VARCHAR(4),
    expires_at DATE,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    
    INDEX idx_user (user_id)
);
```

### Orders Database (PostgreSQL - Partitioned)

```sql
-- Orders table (partitioned by month for performance)
CREATE TABLE orders (
    order_id BIGSERIAL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',  
    -- PENDING, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    shipping_address_id BIGINT,
    payment_method_id BIGINT,
    tracking_number VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    PRIMARY KEY (order_id, created_at),
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) PARTITION BY RANGE (created_at);

-- Partitions (one per month)
CREATE TABLE orders_2025_01 PARTITION OF orders
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
    
CREATE TABLE orders_2025_02 PARTITION OF orders
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
-- ... create partitions for each month

-- Order items
CREATE TABLE order_items (
    order_item_id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(500),  -- Snapshot (product may change)
    quantity INT NOT NULL CHECK (quantity > 0),
    price DECIMAL(10,2) NOT NULL,  -- Price at time of order
    created_at TIMESTAMP DEFAULT NOW(),
    
    INDEX idx_order (order_id),
    INDEX idx_product (product_id)
);

-- Payments
CREATE TABLE payments (
    payment_id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,  -- One payment per order
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    status VARCHAR(50) DEFAULT 'PENDING',  
    -- PENDING, SUCCESS, FAILED, REFUNDED
    transaction_id VARCHAR(255),  -- External payment gateway ID
    idempotency_key VARCHAR(255) UNIQUE,  -- Prevent duplicate charges
    created_at TIMESTAMP DEFAULT NOW(),
    
    INDEX idx_order (order_id),
    INDEX idx_status (status),
    INDEX idx_idempotency (idempotency_key)
);
```

---

## Indexing Strategy

### Why Indexes?

**Without index:**
```sql
-- Full table scan (slow!)
SELECT * FROM products WHERE category_id = 5;
-- Scans all 500M rows → 30 seconds ❌
```

**With index:**
```sql
-- Index seek (fast!)
SELECT * FROM products WHERE category_id = 5;
-- Uses idx_category → 50ms ✅
```

### Index Examples

```sql
-- B-tree index (default) - for equality and range queries
CREATE INDEX idx_price ON products (price);
-- Good for: WHERE price BETWEEN 100 AND 500

-- Composite index - for multi-column queries
CREATE INDEX idx_category_price ON products (category_id, price);
-- Good for: WHERE category_id = 5 AND price > 100

-- Partial index - for filtered queries
CREATE INDEX idx_active_products ON products (product_id) WHERE stock_quantity > 0;
-- Good for: WHERE stock_quantity > 0 (only indexes in-stock products)

-- Full-text search index - for text search
CREATE INDEX idx_product_search ON products USING GIN(to_tsvector('english', name || ' ' || description));
-- Good for: WHERE to_tsvector('english', name) @@ to_tsquery('laptop')
```

---

## Sharding Strategy

### When to Shard?

**Current load:** 1,400 writes/sec (within single DB capacity)

**Future load (10x growth):** 14,000 writes/sec (exceeds single DB)

**Solution:** Shard by user_id (hash-based)

```python
# Sharding function
def get_shard(user_id, num_shards=4):
    return user_id % num_shards

# Examples:
# User 1001 → Shard 1
# User 1002 → Shard 2
# User 1003 → Shard 3
# User 1004 → Shard 0
```

### Shard Distribution

```
Shard 0 (25% of users):
- Users: 1000, 1004, 1008, ...
- Orders for these users
- Payments for these orders

Shard 1 (25% of users):
- Users: 1001, 1005, 1009, ...
- Orders for these users
- Payments for these orders

... and so on
```

### Query Routing

```javascript
// Single-shard query (fast)
async function getUserOrders(userId) {
    const shard = getShardForUser(userId);
    const db = databaseShards[shard];
    return db.query('SELECT * FROM orders WHERE user_id = $1', [userId]);
}

// Multi-shard query (slower - scatter-gather)
async function getTopProducts() {
    const results = await Promise.all(
        databaseShards.map(shard => 
            shard.query('SELECT product_id, SUM(quantity) FROM order_items GROUP BY product_id')
        )
    );
    
    // Merge results from all shards
    return mergeAndSort(results);
}
```

---

## Backup & Recovery

### Backup Strategy

```
Full Backup:
- Frequency: Daily at 2 AM (low traffic)
- Retention: 30 days
- Storage: S3 Glacier

Incremental Backup:
- Frequency: Every 6 hours
- Retention: 7 days
- Storage: S3 Standard

Point-in-Time Recovery:
- WAL (Write-Ahead Log) archival
- Can restore to any point in last 7 days
```

### Disaster Recovery

```
RPO (Recovery Point Objective): < 5 minutes (max data loss)
RTO (Recovery Time Objective): < 15 minutes (max downtime)

Strategy:
1. Multi-AZ deployment (Primary + Standby)
2. Continuous replication to standby
3. Automatic failover (30-60 seconds)
4. Daily backups to S3 (for catastrophic failure)
```

---

**Previous**: [08_api_design.md](./08_api_design.md)
**Next**: [10_data_flow_diagrams.md](./10_data_flow_diagrams.md)
