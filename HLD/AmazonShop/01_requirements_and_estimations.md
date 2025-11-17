# Amazon Shop - Requirements and Estimations

## Overview
Amazon Shop is an e-commerce platform that allows users to browse products, manage their cart, place orders, and track shipments. This document outlines the functional and non-functional requirements, along with detailed capacity estimations.

---

## 1. Functional Requirements

### 1.1 Core Features (High Priority - Must Have)

#### Product Catalog Management
- ✅ **Browse Products**: Users can browse millions of products across various categories
- ✅ **Product Details**: View detailed product information (images, descriptions, specifications, reviews, ratings)
- ✅ **Inventory Management**: Real-time inventory tracking and stock level updates
- ✅ **Category Management**: Organize products into hierarchical categories and subcategories

#### Search & Discovery
- ✅ **Product Search**: Search products by name, category, brand, specifications
- ✅ **Filters & Sorting**: Filter by price, rating, brand, availability; Sort by relevance, price, popularity
- ✅ **Auto-suggestions**: Real-time search suggestions as user types
- ✅ **Search History**: Track and display user's recent searches

#### Order Management
- ✅ **Shopping Cart**: Add/remove items, update quantities, save for later
- ✅ **Checkout Flow**: Review cart, select shipping address, choose payment method
- ✅ **Order Placement**: Create order with order ID, confirmation email
- ✅ **Order Tracking**: Track order status (placed, packed, shipped, delivered)
- ✅ **Order History**: View past orders, order details, invoices

#### Payment Processing
- ✅ **Multiple Payment Methods**: Credit/debit cards, digital wallets, UPI, net banking, cash on delivery
- ✅ **Payment Gateway Integration**: Secure payment processing with third-party gateways
- ✅ **Payment Verification**: Real-time payment status updates
- ✅ **Refund Processing**: Handle refunds for canceled or returned orders

#### Shipment & Fulfillment
- ✅ **Shipping Options**: Standard, express, same-day delivery
- ✅ **Address Management**: Save multiple delivery addresses
- ✅ **Shipment Tracking**: Real-time tracking with carrier integration
- ✅ **Delivery Confirmation**: Notify users on successful delivery

#### Customer Management
- ✅ **User Registration & Login**: Email/phone signup, social login (Google, Facebook)
- ✅ **User Profile**: Manage personal information, addresses, payment methods
- ✅ **Wishlist**: Save products for future purchase
- ✅ **Product Reviews & Ratings**: Submit reviews, rate products (1-5 stars)

### 1.2 Extended Features (Medium Priority - Good to Have)

#### Analytics & Insights
- 📊 **Sales Analytics**: Track daily/monthly sales, revenue, popular products
- 📊 **User Behavior Analytics**: Page views, click-through rates, conversion funnel
- 📊 **Inventory Analytics**: Stock turnover rate, low-stock alerts
- 📊 **Business Dashboards**: Real-time metrics for sellers and admins

#### AI-Powered Recommendations
- 🤖 **Personalized Recommendations**: "Recommended for you" based on browsing history
- 🤖 **Similar Products**: "Customers who bought this also bought..."
- 🤖 **Trending Products**: Showcase trending and popular items
- 🤖 **Smart Search**: Use ML to improve search relevance and ranking

### 1.3 Additional Features (Low Priority - Nice to Have)

#### Seller Features
- 🏪 **Seller Dashboard**: Manage products, orders, inventory
- 🏪 **Product Listing**: Add new products, update prices, manage stock
- 🏪 **Order Fulfillment**: Process orders, update shipping status

#### Admin Features
- 👨‍💼 **User Management**: View users, manage accounts, handle disputes
- 👨‍💼 **Content Moderation**: Review and approve product listings, reviews
- 👨‍💼 **System Monitoring**: Monitor system health, performance metrics

---

## 2. Non-Functional Requirements

### 2.1 Scalability 📈

**User Scale:**
- Support **100 million registered users**
- Handle **10 million Daily Active Users (DAU)**
- Peak during sale events: **50 million concurrent users**

**Product Scale:**
- Catalog of **500 million products**
- **1 million new products added per month**
- **10 million sellers** on the platform

**Transaction Scale:**
- **5 million orders per day** (average)
- **20 million orders per day** during sale events (4x peak)
- **100 million searches per day**

**Growth Rate:**
- User base growing at **30% year-over-year**
- Product catalog growing at **25% year-over-year**
- Order volume growing at **40% year-over-year**

### 2.2 Availability 🟢

**Uptime Requirements:**
- **99.99% availability** (52 minutes downtime per year)
- **99.999% availability** for payment services (5 minutes downtime per year)

**Why 99.99%?**
- 1 hour downtime = **$10 million in lost revenue** (estimated)
- Customer trust is critical for e-commerce
- Competitive advantage (competitors also offer 99.9%+)

**Disaster Recovery:**
- **RPO (Recovery Point Objective)**: < 5 minutes (maximum data loss)
- **RTO (Recovery Time Objective)**: < 15 minutes (maximum downtime)
- Multi-region deployment with automatic failover

### 2.3 Performance ⚡

**Latency Requirements:**
- **Homepage load**: < 500ms (p95)
- **Product search**: < 300ms (p95)
- **Product page load**: < 1 second (p95)
- **Add to cart**: < 200ms (p95)
- **Checkout**: < 2 seconds (p95)
- **API response time**: < 100ms (p50), < 500ms (p95)

**Throughput:**
- **Search queries**: 50,000 queries per second (peak)
- **API requests**: 200,000 requests per second (peak)
- **Image serving**: 1 million images per second (via CDN)

**Why these numbers?**
- **100ms delay = 1% drop in sales** (Amazon study)
- **1 second delay = 7% reduction in conversions** (industry standard)
- Users expect instant responses for search and cart operations

### 2.4 Consistency 🔄

**Strong Consistency Required:**
- ✅ **Inventory**: Prevent overselling (can't sell more than available stock)
- ✅ **Payment**: Ensure exactly-once payment processing
- ✅ **Order status**: Accurate order state transitions

**Eventual Consistency Acceptable:**
- ⏱️ **Product reviews**: Can take a few seconds to appear
- ⏱️ **Recommendation updates**: Can be stale by minutes
- ⏱️ **Analytics data**: Can be delayed by hours
- ⏱️ **Search index**: Can be updated asynchronously (5-10 second delay)

**Why this trade-off?**
- Strong consistency for critical operations prevents data corruption
- Eventual consistency allows better performance and scalability for non-critical features

### 2.5 Reliability 🛡️

**Data Durability:**
- **99.999999999% (11 nines)** for order data (S3 standard)
- **Database backups**: Every 6 hours, retained for 30 days
- **Point-in-time recovery**: Last 7 days

**Error Handling:**
- **Retry mechanism**: Exponential backoff for transient failures
- **Circuit breaker**: Prevent cascade failures
- **Graceful degradation**: Show cached data if service is down

**Data Integrity:**
- **ACID transactions** for order placement
- **Idempotency keys** for payment processing (prevent duplicate charges)
- **Data validation** at API layer

### 2.6 Security 🔒

**Authentication & Authorization:**
- **Multi-factor authentication (MFA)** for sensitive operations
- **OAuth 2.0** for third-party integrations
- **JWT tokens** with 15-minute expiry for session management
- **Role-Based Access Control (RBAC)**: User, Seller, Admin roles

**Data Encryption:**
- **Encryption in transit**: TLS 1.3 for all API calls
- **Encryption at rest**: AES-256 for database and storage
- **PCI DSS compliance** for payment data
- **Tokenization** for credit card storage (never store raw card numbers)

**API Security:**
- **Rate limiting**: 100 requests/minute per user, 1000/minute per IP
- **Input validation**: Prevent SQL injection, XSS, CSRF
- **API key rotation**: Every 90 days
- **DDoS protection**: CloudFlare or AWS Shield

**Compliance:**
- **GDPR**: Right to deletion, data portability
- **PCI DSS Level 1**: Payment card data security
- **SOC 2 Type II**: Security and availability controls

### 2.7 Cost Optimization 💰

**Budget Constraints:**
- **Infrastructure cost**: $5 million/month (for 10M DAU)
- **Cost per user**: $0.50/month
- **Cost per transaction**: $0.10

**Optimization Strategies:**
- Use **CDN** to reduce bandwidth costs (90% cache hit rate)
- Use **auto-scaling** to match capacity with demand
- Use **spot instances** for batch processing (70% cost savings)
- Use **S3 lifecycle policies** to move old data to cheaper storage (Glacier)

---

## 3. Traffic Estimates (Detailed Calculations)

### 3.1 User Metrics

**Given:**
- Total registered users: **100 million**
- Daily Active Users (DAU): **10 million** (10% of registered users)
- Peak concurrent users: **2 million** (20% of DAU)

**Assumptions:**
- Average user session: **15 minutes**
- Average actions per session: **20** (browse 10 products, search 3 times, add to cart 2 items, etc.)
- Peak traffic is **3x average** (during flash sales, 12 PM - 2 PM)

### 3.2 Request Calculations

**Daily Requests:**
```
Total requests per day = DAU × Actions per user
                       = 10,000,000 × 20
                       = 200,000,000 requests/day
```

**Requests per Second (Average):**
```
Seconds in a day = 24 × 60 × 60 = 86,400 seconds

Average requests/sec = 200,000,000 / 86,400
                     = 2,315 requests/sec
```

**Requests per Second (Peak):**
```
Peak requests/sec = Average × 3
                  = 2,315 × 3
                  = 6,945 requests/sec ≈ 7,000 requests/sec
```

### 3.3 Read vs Write Ratio

**Request Breakdown:**
- **Read operations**: Browse products, search, view cart, view orders = **80%**
- **Write operations**: Add to cart, place order, update profile = **20%**

```
Read requests/sec (average) = 2,315 × 0.80 = 1,852 req/sec
Write requests/sec (average) = 2,315 × 0.20 = 463 req/sec

Read requests/sec (peak) = 7,000 × 0.80 = 5,600 req/sec
Write requests/sec (peak) = 7,000 × 0.20 = 1,400 req/sec
```

**Why 80:20 ratio?**
- Users browse 10 products before buying 1 (typical conversion rate is 2-5%)
- Each purchase involves multiple read operations (search, compare, read reviews)

### 3.4 Bandwidth Calculations

**Average Request Size:**
- API request (JSON): **2 KB**
- API response (JSON): **10 KB** (product list, search results)
- Product image: **500 KB** (compressed)

**Incoming Bandwidth (Upload to server):**
```
Incoming = Requests/sec × Request size
         = 7,000 × 2 KB
         = 14,000 KB/sec
         = 14 MB/sec (peak)
```

**Outgoing Bandwidth (Download from server):**
```
API responses = 7,000 × 10 KB = 70 MB/sec

Image requests = Assume 50% of page loads include 3 images
               = (7,000 × 0.5) × 3 × 500 KB
               = 5,250 MB/sec
               = 5.25 GB/sec (peak)

Total outgoing = 70 MB/sec + 5,250 MB/sec
               = 5,320 MB/sec
               = 5.3 GB/sec (peak without CDN)
```

**With CDN (90% cache hit rate):**
```
Origin server outgoing = 5.3 GB/sec × 0.10
                       = 530 MB/sec (peak)
```

**Why CDN is critical:**
- **10x bandwidth reduction** at origin server
- **Cost savings**: CDN bandwidth is 10x cheaper than origin bandwidth
- **Performance**: CDN edge servers are geographically closer to users

### 3.5 Database Query Estimates

**Read Queries:**
- Product search: **30,000 queries/sec** (peak)
- Product details: **10,000 queries/sec** (peak)
- Cart operations: **5,000 queries/sec** (peak)
- User profile: **3,000 queries/sec** (peak)

**Write Queries:**
- Add to cart: **800 queries/sec** (peak)
- Place order: **200 queries/sec** (peak)
- Update inventory: **400 queries/sec** (peak)

**Total Database Load:**
```
Read queries: 48,000 queries/sec (peak)
Write queries: 1,400 queries/sec (peak)
```

**With caching (80% cache hit rate):**
```
Database read queries = 48,000 × 0.20 = 9,600 queries/sec
Database write queries = 1,400 queries/sec (no caching for writes)
```

---

## 4. Storage Estimates (Detailed Calculations)

### 4.1 User Data

**User Profile:**
- Per user: **2 KB** (name, email, phone, hashed password, addresses)
- Total users: **100 million**

```
User storage = 100,000,000 × 2 KB
             = 200,000,000 KB
             = 200 GB
```

**User Activity Logs:**
- Per user per day: **10 KB** (browsing history, searches, clicks)
- Retention: **90 days**

```
Daily logs = 10,000,000 DAU × 10 KB = 100 GB/day
90-day logs = 100 GB × 90 = 9,000 GB = 9 TB
```

### 4.2 Product Data

**Product Catalog:**
- Per product: **5 KB** (name, description, price, category, seller, specifications)
- Total products: **500 million**

```
Product catalog = 500,000,000 × 5 KB
                = 2,500,000,000 KB
                = 2,500 GB
                = 2.5 TB
```

**Product Images:**
- Per product: **5 images** at **500 KB each** (compressed)
- Total products: **500 million**

```
Product images = 500,000,000 × 5 × 500 KB
               = 1,250,000,000,000 KB
               = 1,250 TB
               = 1.25 PB (petabytes)
```

**Image Storage Strategy:**
- **Original images**: Store in S3 Glacier (rarely accessed) = 1.25 PB
- **Compressed images**: Store in S3 Standard (frequently accessed) = 300 TB
- **CDN cache**: 10% of popular images = 30 TB

### 4.3 Order Data

**Orders Per Day:**
- Average: **5 million orders/day**
- Per order: **3 KB** (order ID, user ID, items, total, status, timestamps)

```
Daily order data = 5,000,000 × 3 KB = 15 GB/day
Monthly = 15 GB × 30 = 450 GB/month
Yearly = 450 GB × 12 = 5,400 GB = 5.4 TB/year
5 years = 5.4 TB × 5 = 27 TB
```

### 4.4 Review & Rating Data

**Reviews:**
- **10% of orders** result in a review
- Per review: **1 KB** (text, rating, user ID, product ID, timestamp)

```
Daily reviews = 5,000,000 × 0.10 = 500,000 reviews/day
Daily storage = 500,000 × 1 KB = 500 MB/day
Yearly = 500 MB × 365 = 182.5 GB/year
5 years = 182.5 GB × 5 = 912.5 GB ≈ 1 TB
```

### 4.5 Inventory Data

**Inventory Tracking:**
- Per product per seller: **500 bytes** (product ID, seller ID, stock count, warehouse location)
- Products: **500 million**

```
Inventory data = 500,000,000 × 500 bytes
               = 250,000,000,000 bytes
               = 250 GB
```

### 4.6 Cache Storage

**Redis Cache:**
- **Product catalog cache**: 100 GB (most popular 10 million products)
- **Search results cache**: 50 GB (popular search queries)
- **User session cache**: 20 GB (2 million concurrent users × 10 KB each)

```
Total cache = 100 GB + 50 GB + 20 GB = 170 GB
```

**Recommendation:** Use **200 GB Redis cluster** with replication (400 GB total)

### 4.7 Total Storage Summary

| Data Type | Size | Storage Type |
|-----------|------|--------------|
| User profiles | 200 GB | PostgreSQL |
| User activity logs (90 days) | 9 TB | ElasticSearch |
| Product catalog | 2.5 TB | PostgreSQL/MongoDB |
| Product images (original) | 1.25 PB | S3 Glacier |
| Product images (compressed) | 300 TB | S3 Standard |
| Order data (5 years) | 27 TB | PostgreSQL |
| Reviews (5 years) | 1 TB | PostgreSQL |
| Inventory | 250 GB | PostgreSQL |
| Redis cache | 200 GB | Redis |
| **Total** | **~1.6 PB** | **Mixed** |

**Cost Implications:**
```
S3 Glacier: 1.25 PB × $0.004/GB = $5,000/month
S3 Standard: 300 TB × $0.023/GB = $6,900/month
PostgreSQL (RDS): 30 TB × $0.10/GB = $3,000/month
Redis: 200 GB × $0.50/GB = $100/month

Total storage cost ≈ $15,000/month
```

---

## 5. Assumptions

### 5.1 Traffic Assumptions
1. **DAU is 10% of total users** (industry standard for e-commerce)
2. **Peak traffic is 3x average** (based on Amazon's Black Friday data)
3. **80:20 read-write ratio** (typical for e-commerce)
4. **User session duration is 15 minutes** (average browsing time)
5. **Conversion rate is 2%** (2% of visitors make a purchase)

### 5.2 Data Assumptions
1. **Product catalog grows 25% yearly** (new sellers, new products)
2. **User base grows 30% yearly** (market expansion)
3. **Average order value is $50** (across all categories)
4. **10% of users leave reviews** (industry average)
5. **Each product has 5 images** (standard product listing)

### 5.3 Performance Assumptions
1. **CDN cache hit rate is 90%** (for images and static content)
2. **Database cache hit rate is 80%** (for Redis)
3. **Average API response size is 10 KB** (JSON data)
4. **Average image size is 500 KB** (compressed)

### 5.4 Business Assumptions
1. **System operates 24/7** (global audience across time zones)
2. **Peak hours are 12 PM - 2 PM local time** (lunch break shopping)
3. **Sale events cause 4x traffic spike** (Black Friday, Prime Day)
4. **99.99% availability is required** (competitive necessity)
5. **PCI DSS compliance is mandatory** (for payment processing)

### 5.5 Technology Assumptions
1. **PostgreSQL can handle 10,000 QPS** per instance (with proper indexing)
2. **Redis can handle 100,000 QPS** per instance (in-memory speed)
3. **Load balancer can handle 50,000 connections** (Nginx/HAProxy)
4. **Application server can handle 1,000 concurrent requests** (Node.js/Java)

---

## 6. Summary

### Key Metrics at a Glance

| Metric | Value |
|--------|-------|
| **Total Users** | 100 million |
| **Daily Active Users** | 10 million |
| **Peak Concurrent Users** | 2 million |
| **Products** | 500 million |
| **Orders/Day** | 5 million (avg), 20 million (peak) |
| **Searches/Day** | 100 million |
| **Requests/Sec** | 2,315 (avg), 7,000 (peak) |
| **Bandwidth** | 5.3 GB/sec (peak, no CDN), 530 MB/sec (with CDN) |
| **Total Storage** | 1.6 PB |
| **Availability** | 99.99% |
| **API Latency (p95)** | < 500ms |

### Next Steps

In the following documents, we will:
1. **Design the architecture** incrementally (Step 1 to Step 6)
2. **Choose appropriate databases** (SQL vs NoSQL)
3. **Implement caching strategies** to achieve 80% cache hit rate
4. **Design for high availability** with multi-region deployment
5. **Ensure security** with encryption, authentication, and compliance
6. **Optimize costs** with CDN, auto-scaling, and efficient storage

---

**Document Version**: 1.0
**Last Updated**: 2025-11-17
**Next Document**: [02_step1_basic_architecture.md](./02_step1_basic_architecture.md)
