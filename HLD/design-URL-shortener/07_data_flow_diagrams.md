# Phase 7: Data Flow Diagrams

> **For Beginners:** Data flow diagrams show exactly how data moves through our system, step by step. Like a recipe that shows each cooking step in order!

---

## 🎯 What We'll Cover

We'll create detailed sequence diagrams for:
1. URL Creation (Shortening)
2. URL Redirect (Clicking short link)
3. Analytics Processing
4. Password-Protected URL Access
5. Custom Alias Creation
6. Bulk URL Creation
7. Failure Scenarios

---

## 📊 Flow 1: URL Creation (Happy Path)

**Scenario:** User creates a short URL

```mermaid
sequenceDiagram
    actor User
    participant LB as Load Balancer
    participant App as App Server
    participant Redis as Redis Cache
    participant DB as PostgreSQL
    participant MQ as Kafka Queue
    participant Worker as Background Worker

    User->>LB: POST /v1/urls<br/>{original_url: "https://example.com/..."}

    LB->>App: Forward request

    Note over App: Step 1: Validate input

    App->>App: Validate URL format<br/>Check URL length<br/>Check blacklist

    Note over App: Step 2: Check rate limit

    App->>Redis: GET rate:user_123

    Redis-->>App: Count: 7 (limit: 10)

    App->>Redis: INCR rate:user_123

    Note over App: Step 3: Generate short code

    App->>App: Generate hash<br/>hash = MD5(url + timestamp + random)<br/>short_code = hash.substring(0, 7)<br/>→ "aB7x3K"

    Note over App: Step 4: Check uniqueness

    App->>DB: SELECT 1 FROM urls<br/>WHERE short_code = 'aB7x3K'

    DB-->>App: No rows (unique!)

    Note over App: Step 5: Save to database

    App->>DB: BEGIN TRANSACTION

    App->>DB: INSERT INTO urls<br/>(short_code, original_url, user_id, created_at)<br/>VALUES ('aB7x3K', 'https://...', 123, NOW())

    DB-->>App: Row inserted (id: 1)

    App->>DB: COMMIT TRANSACTION

    Note over App: Step 6: Cache it immediately

    App->>Redis: SET short:aB7x3K "https://example.com/..."<br/>EX 86400

    Redis-->>App: OK

    Note over App: Step 7: Publish event (async)

    App->>MQ: Publish event<br/>{type: "url.created", short_code: "aB7x3K"}

    Note over App,MQ: Fire-and-forget (1ms)

    App-->>User: 201 Created<br/>{<br/>  short_code: "aB7x3K",<br/>  short_url: "https://short.ly/aB7x3K"<br/>}

    Note over User: Total time: ~150ms

    Note over MQ,Worker: ━━━ Meanwhile (Async) ━━━

    MQ->>Worker: Deliver event

    Worker->>Worker: Send welcome email<br/>Update user stats<br/>Log to analytics
```

**Step-by-Step Breakdown:**

| Step | Time | What Happens |
|------|------|--------------|
| 1. Validate | 1ms | Check URL format, length, blacklist |
| 2. Rate Limit | 5ms | Check Redis counter (allow/reject) |
| 3. Generate Code | 2ms | Hash URL + timestamp + random |
| 4. Check Uniqueness | 10ms | Query DB (indexed lookup) |
| 5. Save to DB | 100ms | Transaction: INSERT URL |
| 6. Cache | 5ms | Store in Redis for fast access |
| 7. Publish Event | 1ms | Send to Kafka (async) |
| **Total** | **~150ms** | Response to user |

---

## 📊 Flow 2: URL Redirect (Happy Path - Cache Hit)

**Scenario:** User clicks short.ly/aB7x3K

```mermaid
sequenceDiagram
    actor User
    participant LB as Load Balancer
    participant App as App Server
    participant Redis as Redis Cache
    participant MQ as Kafka Queue

    User->>LB: GET /aB7x3K

    LB->>App: Route to App Server

    Note over App: Step 1: Check cache first

    App->>Redis: GET short:aB7x3K

    Redis-->>App: "https://example.com/long-url"

    Note over App: ✅ Cache HIT! (80% of requests)

    Note over App: Step 2: Publish click event (async)

    App->>MQ: Publish event (async)<br/>{<br/>  type: "url.clicked",<br/>  short_code: "aB7x3K",<br/>  ip: "203.0.113.45",<br/>  user_agent: "Chrome/Mobile",<br/>  timestamp: "2025-01-15T10:30:00Z"<br/>}

    Note over App,MQ: Takes 1ms (non-blocking)

    Note over App: Step 3: Redirect user immediately

    App-->>User: 302 Redirect<br/>Location: https://example.com/long-url

    Note over User: Total time: ~10ms ⚡<br/>User redirected!

    User->>User: Browser follows redirect<br/>to original URL
```

**Performance:**
- Cache lookup: **5ms**
- Publish event: **1ms**
- Response: **4ms**
- **Total: ~10ms** (10x faster than database query!)

---

## 📊 Flow 3: URL Redirect (Cache Miss)

**Scenario:** Short code not in cache (first time or expired)

```mermaid
sequenceDiagram
    actor User
    participant App as App Server
    participant Redis as Redis Cache
    participant DB as PostgreSQL
    participant MQ as Kafka Queue

    User->>App: GET /xY9zK2

    Note over App: Step 1: Check cache

    App->>Redis: GET short:xY9zK2

    Redis-->>App: NULL

    Note over App: ❌ Cache MISS (20% of requests)

    Note over App: Step 2: Query database

    App->>DB: SELECT original_url, expires_at, is_active<br/>FROM urls<br/>WHERE short_code = 'xY9zK2'<br/>AND is_active = TRUE

    DB-->>App: {<br/>  original_url: "https://example.com/other",<br/>  expires_at: NULL,<br/>  is_active: true<br/>}

    Note over App: Step 3: Check expiration

    App->>App: expires_at == NULL → Not expired ✅

    Note over App: Step 4: Store in cache for next time

    App->>Redis: SET short:xY9zK2<br/>"https://example.com/other"<br/>EX 86400

    Redis-->>App: OK

    Note over App: Step 5: Publish analytics (async)

    App->>MQ: Publish click event

    Note over App: Step 6: Redirect

    App-->>User: 302 Redirect<br/>Location: https://example.com/other

    Note over User: Total time: ~100ms<br/>(First time only!)

    Note over User: Next 1M users → Cache hit (10ms) ⚡
```

**Performance Comparison:**

| Scenario | Time | Frequency |
|----------|------|-----------|
| **Cache Hit** | 10ms | 80% of requests |
| **Cache Miss** | 100ms | 20% of requests |
| **Average** | (0.8 × 10) + (0.2 × 100) = **28ms** | - |

---

## 📊 Flow 4: Analytics Processing (Background)

**Scenario:** Worker processes click event

```mermaid
sequenceDiagram
    participant MQ as Kafka Queue
    participant Worker as Analytics Worker
    participant GeoIP as GeoIP Service
    participant ClickHouse as ClickHouse DB
    participant Aggregator as Aggregation Worker

    MQ->>Worker: Consume event<br/>{<br/>  type: "url.clicked",<br/>  short_code: "aB7x3K",<br/>  ip: "203.0.113.45",<br/>  user_agent: "Mozilla/5.0 (iPhone...)"<br/>}

    Note over Worker: Step 1: Enrich event data

    Worker->>GeoIP: Lookup IP: 203.0.113.45

    GeoIP-->>Worker: {<br/>  country: "US",<br/>  city: "San Francisco",<br/>  lat: 37.7749,<br/>  lon: -122.4194<br/>}

    Worker->>Worker: Parse user_agent<br/>→ device: "mobile"<br/>→ os: "iOS"<br/>→ browser: "Safari"

    Note over Worker: Step 2: Insert raw event

    Worker->>ClickHouse: INSERT INTO clicks<br/>(event_id, short_code, timestamp,<br/> ip, country, city, device_type, os, browser)

    ClickHouse-->>Worker: Inserted

    Note over Worker: Step 3: Update daily aggregates

    Worker->>ClickHouse: INSERT INTO daily_stats<br/>(short_code, date, click_count)<br/>VALUES ('aB7x3K', '2025-01-15', 1)<br/>ON CONFLICT (short_code, date)<br/>DO UPDATE SET click_count = click_count + 1

    ClickHouse-->>Worker: Updated

    Note over Worker: Step 4: Update Redis counter (hot cache)

    Worker->>Worker: If viral link (clicks > 1000)<br/>→ Cache hot stats in Redis

    Note over Worker: Total processing time: 50-100ms<br/>(User already redirected!)

    Note over Aggregator: ━━━ Hourly Aggregation Job ━━━

    Note over Aggregator: Runs every hour

    Aggregator->>ClickHouse: SELECT short_code, country, COUNT(*)<br/>FROM clicks<br/>WHERE timestamp >= now() - INTERVAL 1 HOUR<br/>GROUP BY short_code, country

    Aggregator->>ClickHouse: INSERT INTO hourly_stats ...

    Note over Aggregator: Pre-compute hourly stats<br/>for fast dashboard loading
```

---

## 📊 Flow 5: Password-Protected URL Access

**Scenario:** User tries to access password-protected URL

```mermaid
sequenceDiagram
    actor User
    participant App as App Server
    participant Redis as Redis Cache
    participant DB as PostgreSQL

    User->>App: GET /aB7x3K

    App->>Redis: GET short:aB7x3K

    alt Cache Hit
        Redis-->>App: {url: "https://...", password_protected: true}
    else Cache Miss
        App->>DB: SELECT original_url, password_hash<br/>FROM urls WHERE short_code = 'aB7x3K'
        DB-->>App: {url: "https://...", password_hash: "$2b$..."}
    end

    Note over App: Detected password protection!

    App-->>User: 403 Forbidden<br/>{<br/>  error: "PASSWORD_REQUIRED",<br/>  action: {<br/>    method: "POST",<br/>    endpoint: "/v1/urls/aB7x3K/unlock"<br/>  }<br/>}

    User->>User: Show password input form

    User->>App: POST /v1/urls/aB7x3K/unlock<br/>{password: "secret123"}

    App->>DB: SELECT password_hash<br/>FROM urls WHERE short_code = 'aB7x3K'

    DB-->>App: "$2b$10$hash..."

    App->>App: bcrypt.compare("secret123", hash)

    alt Password Correct
        App->>App: Generate unlock token (JWT)<br/>Valid for 1 hour

        App-->>User: 200 OK<br/>{<br/>  unlock_token: "eyJhbG...",<br/>  redirect_url: "https://example.com/..."<br/>}

        User->>User: Save token in browser<br/>Redirect to original URL
    else Password Incorrect
        App-->>User: 401 Unauthorized<br/>{<br/>  error: "INVALID_PASSWORD",<br/>  attempts_remaining: 2<br/>}
    end
```

---

## 📊 Flow 6: Custom Alias Creation

**Scenario:** User wants custom alias "my-blog"

```mermaid
sequenceDiagram
    actor User
    participant App as App Server
    participant DB as PostgreSQL
    participant Redis as Redis Cache

    User->>App: POST /v1/urls<br/>{<br/>  original_url: "https://...",<br/>  custom_alias: "my-blog"<br/>}

    Note over App: Step 1: Validate custom alias

    App->>App: Check format: /^[a-zA-Z0-9-_]+$/<br/>Check length: 3-20 chars<br/>Check reserved: not in ["api", "admin", ...]

    alt Invalid Format
        App-->>User: 400 Bad Request<br/>{error: "Invalid alias format"}
    end

    Note over App: Step 2: Check availability

    App->>DB: SELECT 1 FROM urls<br/>WHERE short_code = 'my-blog'<br/>OR short_code = 'my-blog-' || generate_random(3)

    alt Alias Taken
        DB-->>App: Row exists

        App-->>User: 409 Conflict<br/>{<br/>  error: "ALIAS_TAKEN",<br/>  suggestions: ["my-blog-2", "my-blog-2025"]<br/>}
    else Available
        DB-->>App: No rows

        Note over App: Step 3: Create URL with custom alias

        App->>DB: INSERT INTO urls<br/>(short_code, original_url, is_custom_alias)<br/>VALUES ('my-blog', 'https://...', TRUE)

        DB-->>App: Success

        App->>Redis: SET short:my-blog "https://..."

        App-->>User: 201 Created<br/>{<br/>  short_code: "my-blog",<br/>  short_url: "https://short.ly/my-blog"<br/>}
    end
```

---

## 📊 Flow 7: Expired URL Access

**Scenario:** User tries to access expired URL

```mermaid
sequenceDiagram
    actor User
    participant App as App Server
    participant Redis as Redis Cache
    participant DB as PostgreSQL

    User->>App: GET /aB7x3K

    App->>Redis: GET short:aB7x3K

    Redis-->>App: NULL (not cached or expired)

    App->>DB: SELECT original_url, expires_at<br/>FROM urls<br/>WHERE short_code = 'aB7x3K'

    DB-->>App: {<br/>  original_url: "https://...",<br/>  expires_at: "2025-01-01T00:00:00Z"<br/>}

    App->>App: Check expiration:<br/>expires_at (Jan 1) < now (Jan 15)<br/>→ EXPIRED! ❌

    App-->>User: 410 Gone<br/>{<br/>  error: "URL_EXPIRED",<br/>  message: "This link expired on 2025-01-01",<br/>  expired_at: "2025-01-01T00:00:00Z"<br/>}

    Note over User: Show friendly "Link Expired" page
```

---

## 📊 Flow 8: Database Failover (Failure Scenario)

**Scenario:** Master database crashes during write

```mermaid
sequenceDiagram
    actor User
    participant App as App Server
    participant DBMaster as Master DB
    participant DBSlave as Slave DB
    participant Monitor as Health Monitor

    User->>App: POST /v1/urls<br/>{original_url: "https://..."}

    App->>App: Generate short code: "aB7x3K"

    App->>DBMaster: INSERT INTO urls ...

    Note over DBMaster: 💀 MASTER CRASHES!

    DBMaster-->>App: ❌ Connection Error<br/>Timeout

    App->>App: Retry logic<br/>Attempt 1 of 3

    App->>DBMaster: INSERT INTO urls ...

    DBMaster-->>App: ❌ Still down

    Note over Monitor: Health check detects failure

    Monitor->>Monitor: Master down for 10 seconds<br/>Initiate failover!

    Monitor->>DBSlave: PROMOTE TO MASTER

    DBSlave->>DBSlave: Stop replication<br/>Enable writes<br/>Now NEW MASTER ✅

    Monitor->>App: Update DB config<br/>New master: Slave IP

    App->>App: Retry logic<br/>Attempt 2 of 3

    App->>DBSlave: INSERT INTO urls ...<br/>(Now writing to new master)

    DBSlave-->>App: Success!

    App-->>User: 201 Created<br/>{short_code: "aB7x3K"}

    Note over User: Total time: 15 seconds<br/>(Retry + Failover)<br/>User might see brief delay

    Note over DBMaster,Monitor: Total downtime: 10-30 seconds
```

---

## 📊 Flow 9: Rate Limit Exceeded (Failure Scenario)

**Scenario:** User exceeds rate limit

```mermaid
sequenceDiagram
    actor User
    participant App as App Server
    participant Redis as Redis Cache

    User->>App: POST /v1/urls (Request #11)

    App->>Redis: GET rate:user_123

    Redis-->>App: {<br/>  count: 10,<br/>  reset_at: "2025-01-15T10:31:00Z"<br/>}

    App->>App: Check limit:<br/>count (10) >= limit (10)<br/>→ RATE LIMITED! ❌

    App-->>User: 429 Too Many Requests<br/>{<br/>  error: "RATE_LIMIT_EXCEEDED",<br/>  message: "Max 10 URLs per minute",<br/>  retry_after: 45,<br/>  rate_limit: {<br/>    limit: 10,<br/>    remaining: 0,<br/>    reset_at: "2025-01-15T10:31:00Z"<br/>  }<br/>}

    Note over User: Wait 45 seconds

    Note over User: ⏰ 45 seconds later

    User->>App: POST /v1/urls (Retry)

    App->>Redis: GET rate:user_123

    Redis-->>App: NULL (counter reset!)

    App->>Redis: INCR rate:user_123

    App->>App: Continue normal flow...

    App-->>User: 201 Created ✅
```

---

## 📊 Flow 10: Bulk URL Creation (Optimized)

**Scenario:** User creates 1000 URLs at once

```mermaid
sequenceDiagram
    actor User
    participant App as App Server
    participant DB as PostgreSQL
    participant Redis as Redis Cache
    participant MQ as Kafka

    User->>App: POST /v1/urls/bulk<br/>{urls: [{...}, {...}, ... 1000 items]}

    Note over App: Step 1: Validate all URLs

    App->>App: Parallel validation<br/>Check formats<br/>Check blacklist

    alt Validation Failed
        App-->>User: 400 Bad Request<br/>{invalid_urls: [index: 42, error: "..."]}
    end

    Note over App: Step 2: Generate all short codes

    App->>App: Generate 1000 codes in parallel<br/>["aB7x3K", "xY9zK2", ...]

    Note over App: Step 3: Batch insert (fast!)

    App->>DB: BEGIN TRANSACTION

    App->>DB: INSERT INTO urls<br/>(short_code, original_url, user_id)<br/>VALUES<br/>  ('aB7x3K', 'https://...', 123),<br/>  ('xY9zK2', 'https://...', 123),<br/>  ... (1000 rows)

    Note over App,DB: Single query for all 1000 URLs!<br/>Much faster than 1000 separate INSERTs

    DB-->>App: 1000 rows inserted

    App->>DB: COMMIT

    Note over App: Step 4: Batch cache (async)

    App->>Redis: MSET<br/>  short:aB7x3K "https://..."<br/>  short:xY9zK2 "https://..."<br/>  ... (1000 keys)

    Note over App: Step 5: Publish bulk event

    App->>MQ: Publish bulk_created event<br/>{count: 1000, user_id: 123}

    App-->>User: 201 Created<br/>{<br/>  created: 1000,<br/>  failed: 0,<br/>  results: [{...}, {...}, ...]<br/>}

    Note over User: Total time: ~2 seconds<br/>(vs 150 seconds if done one-by-one!)
```

**Performance Optimization:**
```
Individual inserts: 150ms × 1000 = 150 seconds
Batch insert: 2 seconds

Speedup: 75x faster! 🚀
```

---

## 🎓 Beginner Tips: Reading Sequence Diagrams

### Tip 1: Time Flows Downward
```
Top of diagram = Start
Bottom of diagram = End
Vertical axis = Time
```

### Tip 2: Arrows Show Communication
```
Solid arrow (→): Synchronous call (wait for response)
Dashed arrow (⇢): Asynchronous call (don't wait)
Return arrow (←--): Response
```

### Tip 3: Boxes Show Different Systems
```
Each vertical line = A different component
(User, App Server, Database, etc.)
```

### Tip 4: Notes Explain "Why"
```
"Note over App: This is why we do this step"
```

---

## 📊 Complete Flow Summary Table

| Flow | Avg Time | Frequency | Complexity |
|------|----------|-----------|------------|
| **URL Creation** | 150ms | 6/sec | Medium |
| **Redirect (Cache Hit)** | 10ms | 1,387/sec | Low |
| **Redirect (Cache Miss)** | 100ms | 347/sec | Medium |
| **Analytics Processing** | 50-100ms | Async | Medium |
| **Password-Protected** | 200ms | Rare | High |
| **Custom Alias** | 150ms | 10% of creates | Medium |
| **Expired URL** | 100ms | Rare | Low |
| **Rate Limit** | 5ms | Varies | Low |
| **Bulk Create (1000)** | 2000ms | Rare | High |
| **Database Failover** | 10-30 sec | Very rare | High |

---

## ✅ Key Takeaways

### Performance Optimization:
✅ **Caching reduces redirect time by 10x** (10ms vs 100ms)
✅ **Async analytics** (user doesn't wait for processing)
✅ **Batch operations** (75x faster for bulk creates)

### Reliability:
✅ **Retry logic** (handles transient failures)
✅ **Database failover** (10-30 sec recovery)
✅ **Rate limiting** (protects from abuse)

### User Experience:
✅ **Fast redirects** (< 10ms for cache hits)
✅ **Clear error messages** (tell user what went wrong)
✅ **Password protection** (secure sensitive links)

---

**Previous:** [← API & Database Design](06_api_and_database_design.md)
**Next:** [Scalability & Reliability →](08_scalability_and_reliability.md)
