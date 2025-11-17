# Data Flow Diagrams

## Critical Flows

### 1. Product Search Flow

```mermaid
sequenceDiagram
    actor User
    participant CDN
    participant App as App Server
    participant Cache as Redis
    participant ES as Elasticsearch
    participant DB as PostgreSQL

    User->>CDN: Load search page (HTML/CSS/JS)
    CDN-->>User: Static assets (20ms)
    
    User->>App: GET /api/v1/products/search?q=laptop
    
    App->>Cache: GET "search:laptop:page:1"
    
    alt Cache Hit (80%)
        Cache-->>App: Return cached results (10ms)
        App-->>User: 200 OK + Products (30ms total) ⚡
    else Cache Miss (20%)
        App->>ES: Search query "laptop"
        ES-->>App: Product IDs [12345, 12346, ...]
        
        App->>DB: SELECT * FROM products WHERE id IN (...)
        DB-->>App: Product details
        
        App->>Cache: SET "search:laptop:page:1" (TTL: 5 min)
        App-->>User: 200 OK + Products (150ms)
    end
```

**Time Breakdown:**
- Cache hit: 30ms (80% of requests)
- Cache miss: 150ms (20% of requests)
- Average: 0.8 × 30 + 0.2 × 150 = 54ms ✅

---

### 2. Complete Checkout Flow

```mermaid
sequenceDiagram
    actor User
    participant App as Order Service
    participant Payment as Payment Service
    participant DB as PostgreSQL Master
    participant Queue as RabbitMQ
    participant Email as Email Worker
    participant Inventory as Inventory Worker

    Note over User,Inventory: Synchronous Path (Critical)

    User->>App: POST /api/v1/orders<br/>{items, address, payment}
    
    App->>App: Validate cart items
    
    App->>DB: BEGIN TRANSACTION
    
    App->>DB: INSERT INTO orders
    App->>DB: INSERT INTO order_items
    App->>DB: UPDATE products<br/>SET stock = stock - qty
    
    App->>Payment: POST /payments/process<br/>{orderId, amount}
    
    Payment->>Payment: Validate payment
    Payment->>Payment: Charge via Stripe
    
    alt Payment Success
        Payment-->>App: 200 OK {transactionId}
        App->>DB: INSERT INTO payments<br/>{orderId, SUCCESS}
        App->>DB: COMMIT TRANSACTION
        
        Note over App,Inventory: Asynchronous Path (Background)
        
        App->>Queue: Publish OrderPlacedEvent
        App-->>User: 201 Created<br/>{orderId, status: PAID}
        
        Note over User: User sees success in 150ms! ⚡
        
        Queue->>Email: Consume OrderPlacedEvent
        Email->>Email: Send confirmation email
        
        Queue->>Inventory: Consume OrderPlacedEvent
        Inventory->>DB: Update inventory analytics
        
    else Payment Failed
        Payment-->>App: 402 Payment Failed
        App->>DB: ROLLBACK TRANSACTION
        App-->>User: 402 Payment Failed
    end
```

**Timeline:**
- T0: User clicks "Place Order"
- T50: Order inserted in database
- T100: Payment processed via Stripe
- T150: User sees "Order placed!" ⚡
- T2000: Email sent (background)
- T3000: SMS sent (background)

---

### 3. Product Recommendation Flow

```mermaid
sequenceDiagram
    actor User
    participant App as Product Service
    participant Rec as Recommendation Service
    participant Cache as Redis
    participant ML as ML Model (Python)
    participant Kafka

    User->>App: GET /api/v1/products/12345
    
    App->>App: Get product details
    
    par Get Product & Recommendations
        App->>Cache: GET "product:12345"
        Cache-->>App: Product data
    and
        App->>Rec: GET /recommend/similar/12345?userId=1001
        
        Rec->>Cache: GET "user_features:1001"
        Cache-->>Rec: User features
        
        Rec->>ML: predict(user_features, product_id)
        ML-->>Rec: Top 10 products [67890, 11111, ...]
        
        Rec-->>App: Recommended products
    end
    
    App-->>User: 200 OK {product, recommendations}
    
    Note over App,Kafka: Track event for future recommendations
    
    App->>Kafka: Publish "product.viewed"<br/>{userId, productId, timestamp}
```

---

### 4. Image Upload Flow

```mermaid
sequenceDiagram
    actor Seller
    participant App as Product Service
    participant S3 as AWS S3
    participant CDN as CloudFront CDN

    Seller->>App: POST /api/v1/products/12345/image/upload-url
    
    App->>S3: Generate presigned URL<br/>(valid for 1 hour)
    S3-->>App: Presigned URL
    
    App-->>Seller: 200 OK {uploadUrl, imageUrl}
    
    Note over Seller,S3: Direct upload (bypass server)
    
    Seller->>S3: PUT {presignedUrl}<br/>Content: image file
    S3-->>Seller: 200 OK (uploaded)
    
    Seller->>App: PUT /api/v1/products/12345<br/>{imageUrl}
    
    App->>App: Update product.image_urls
    
    App->>CDN: Invalidate cache<br/>/products/12345-*
    
    App-->>Seller: 200 OK (product updated)
```

**Benefits:**
- ✅ No server bandwidth used (direct to S3)
- ✅ Faster upload (no proxy)
- ✅ Scalable (S3 handles load)

---

## Failure Scenarios

### 1. Database Failover

```mermaid
sequenceDiagram
    participant App as App Servers
    participant Master as Master DB
    participant Standby as Standby DB
    participant LB as DB Load Balancer

    Note over Master: Master is healthy

    App->>LB: Write query
    LB->>Master: Forward query
    Master-->>App: Success

    Note over Master: Master crashes! 💥

    App->>LB: Write query
    LB->>Master: Forward query
    Master--xLB: Connection timeout

    LB->>LB: Detect failure<br/>Health check fails

    LB->>Standby: Promote to master
    Standby->>Standby: Enable writes
    
    Note over Standby: Standby is now master

    LB->>App: Update connection<br/>New master: Standby

    App->>Standby: Retry write query
    Standby-->>App: Success

    Note over App,Standby: Downtime: 30-60 seconds
```

### 2. Payment Service Retry

```mermaid
sequenceDiagram
    participant Order as Order Service
    participant Payment as Payment Service
    participant Stripe

    Order->>Payment: POST /payments/process
    
    Payment->>Stripe: Create charge
    Stripe--xPayment: 503 Service Unavailable
    
    Payment->>Payment: Retry 1 (after 1 second)
    Payment->>Stripe: Create charge (with idempotency key)
    Stripe--xPayment: Timeout
    
    Payment->>Payment: Retry 2 (after 2 seconds)
    Payment->>Stripe: Create charge (same idempotency key)
    Stripe-->>Payment: 200 OK {chargeId}
    
    Payment-->>Order: 200 OK {transactionId}
```

**Idempotency ensures:**
- ✅ User is never charged twice
- ✅ Retries are safe
- ✅ Same result for same request

---

**Previous**: [09_database_design.md](./09_database_design.md)
**Next**: [11_scalability_reliability.md](./11_scalability_reliability.md)
