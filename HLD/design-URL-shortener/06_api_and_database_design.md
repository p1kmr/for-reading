# Phase 6: API Design & Database Schema

> **For Beginners:** API = How external apps talk to our service. Database Schema = How we organize and store data. We'll design both to be clean, fast, and scalable!

---

## 🎯 API Design Principles

### RESTful API Best Practices:
1. ✅ Use HTTP methods correctly (GET, POST, PUT, DELETE)
2. ✅ Use nouns for resources (not verbs)
3. ✅ Version your API (/v1/, /v2/)
4. ✅ Use proper HTTP status codes
5. ✅ Include pagination for lists
6. ✅ Provide clear error messages

---

## 📋 Complete API Specification

### Base URL
```
Production: https://api.short.ly/v1
Staging: https://api-staging.short.ly/v1
```

---

### API 1: Shorten URL (Create)

**Endpoint:**
```http
POST /v1/urls
```

**Request Headers:**
```http
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>  (optional, for authenticated users)
X-RateLimit-Limit: 10             (echoed from server)
X-RateLimit-Remaining: 7
```

**Request Body:**
```json
{
  "original_url": "https://www.example.com/very/long/path/to/article?id=12345",
  "custom_alias": "my-blog",        // Optional: custom short code
  "expires_at": "2025-12-31T23:59:59Z",  // Optional: expiration
  "password": "secret123",          // Optional: password protection
  "tags": ["marketing", "twitter"]  // Optional: for organization
}
```

**Validation Rules:**
```javascript
{
  original_url: {
    required: true,
    maxLength: 2048,
    pattern: /^https?:\/\/.+/,  // Must start with http:// or https://
    blacklist: ['malware.com', 'spam.net']  // Block malicious domains
  },
  custom_alias: {
    required: false,
    minLength: 3,
    maxLength: 20,
    pattern: /^[a-zA-Z0-9-_]+$/,  // Alphanumeric, dash, underscore only
    reserved: ['api', 'www', 'admin', 'login']  // Reserved keywords
  },
  expires_at: {
    required: false,
    minValue: Date.now(),  // Can't be in the past
    maxValue: Date.now() + (365 * 24 * 60 * 60 * 1000)  // Max 1 year
  },
  password: {
    required: false,
    minLength: 6,
    maxLength: 50
  }
}
```

**Response (Success - 201 Created):**
```json
{
  "success": true,
  "data": {
    "short_code": "aB7x3K",
    "short_url": "https://short.ly/aB7x3K",
    "original_url": "https://www.example.com/very/long/path/to/article?id=12345",
    "created_at": "2025-01-15T10:30:00Z",
    "expires_at": "2025-12-31T23:59:59Z",
    "qr_code_url": "https://api.short.ly/v1/urls/aB7x3K/qr",
    "analytics_url": "https://api.short.ly/v1/urls/aB7x3K/analytics"
  },
  "metadata": {
    "request_id": "req_550e8400",
    "rate_limit": {
      "limit": 10,
      "remaining": 9,
      "reset_at": "2025-01-15T10:31:00Z"
    }
  }
}
```

**Response (Error - 400 Bad Request):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_URL",
    "message": "The URL provided is not valid",
    "details": {
      "field": "original_url",
      "reason": "URL must start with http:// or https://"
    }
  },
  "metadata": {
    "request_id": "req_550e8401",
    "timestamp": "2025-01-15T10:30:00Z"
  }
}
```

**Response (Error - 409 Conflict):**
```json
{
  "success": false,
  "error": {
    "code": "ALIAS_TAKEN",
    "message": "The custom alias 'my-blog' is already taken",
    "suggestion": "Try 'my-blog-2' or 'my-blog-2025'"
  }
}
```

**Response (Error - 429 Too Many Requests):**
```json
{
  "success": false,
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "You have exceeded the rate limit of 10 URLs per minute",
    "retry_after": 45  // Seconds until retry
  },
  "metadata": {
    "rate_limit": {
      "limit": 10,
      "remaining": 0,
      "reset_at": "2025-01-15T10:31:00Z"
    }
  }
}
```

---

### API 2: Redirect to Original URL

**Endpoint:**
```http
GET /{short_code}
```

**Example:**
```http
GET /aB7x3K
```

**Response (Success - 302 Found):**
```http
HTTP/1.1 302 Found
Location: https://www.example.com/very/long/path/to/article?id=12345
X-Short-Code: aB7x3K
X-Created-At: 2025-01-15T10:30:00Z
X-Clicks: 1523
```

**Response (Error - 404 Not Found):**
```json
{
  "success": false,
  "error": {
    "code": "URL_NOT_FOUND",
    "message": "The short URL 'aB7x3K' does not exist or has expired"
  }
}
```

**Response (Password Protected - 403 Forbidden):**
```json
{
  "success": false,
  "error": {
    "code": "PASSWORD_REQUIRED",
    "message": "This URL is password protected"
  },
  "action": {
    "method": "POST",
    "endpoint": "/v1/urls/aB7x3K/unlock",
    "body": {
      "password": "your_password_here"
    }
  }
}
```

---

### API 3: Get URL Analytics

**Endpoint:**
```http
GET /v1/urls/{short_code}/analytics
```

**Query Parameters:**
```
?period=7d         // 24h, 7d, 30d, 90d, all
&groupBy=country   // country, device, browser, hour, day
&timezone=UTC
```

**Example:**
```http
GET /v1/urls/aB7x3K/analytics?period=7d&groupBy=country
```

**Response (Success - 200 OK):**
```json
{
  "success": true,
  "data": {
    "short_code": "aB7x3K",
    "period": "7d",
    "summary": {
      "total_clicks": 15234,
      "unique_clicks": 8912,
      "average_per_day": 2176,
      "peak_hour": "14:00"
    },
    "clicks_by_country": [
      { "country": "US", "clicks": 7234, "percentage": 47.5 },
      { "country": "GB", "clicks": 2156, "percentage": 14.2 },
      { "country": "CA", "clicks": 1523, "percentage": 10.0 },
      { "country": "AU", "clicks": 987, "percentage": 6.5 },
      { "country": "Others", "clicks": 3334, "percentage": 21.8 }
    ],
    "clicks_by_device": [
      { "device": "mobile", "clicks": 9902, "percentage": 65.0 },
      { "device": "desktop", "clicks": 4570, "percentage": 30.0 },
      { "device": "tablet", "clicks": 762, "percentage": 5.0 }
    ],
    "clicks_by_day": [
      { "date": "2025-01-09", "clicks": 1523 },
      { "date": "2025-01-10", "clicks": 2134 },
      { "date": "2025-01-11", "clicks": 2987 },
      { "date": "2025-01-12", "clicks": 3421 },
      { "date": "2025-01-13", "clicks": 2156 },
      { "date": "2025-01-14", "clicks": 1890 },
      { "date": "2025-01-15", "clicks": 1123 }
    ],
    "top_referrers": [
      { "referrer": "twitter.com", "clicks": 5234, "percentage": 34.4 },
      { "referrer": "facebook.com", "clicks": 3123, "percentage": 20.5 },
      { "referrer": "direct", "clicks": 2987, "percentage": 19.6 },
      { "referrer": "google.com", "clicks": 1456, "percentage": 9.6 }
    ]
  }
}
```

---

### API 4: Update URL

**Endpoint:**
```http
PATCH /v1/urls/{short_code}
```

**Request Body:**
```json
{
  "original_url": "https://example.com/new-url",  // Optional
  "expires_at": "2026-12-31T23:59:59Z",          // Optional
  "is_active": false                             // Optional: disable URL
}
```

**Response (Success - 200 OK):**
```json
{
  "success": true,
  "data": {
    "short_code": "aB7x3K",
    "original_url": "https://example.com/new-url",
    "updated_at": "2025-01-15T11:00:00Z"
  }
}
```

---

### API 5: Delete URL

**Endpoint:**
```http
DELETE /v1/urls/{short_code}
```

**Response (Success - 204 No Content):**
```http
HTTP/1.1 204 No Content
```

---

### API 6: Bulk Create URLs

**Endpoint:**
```http
POST /v1/urls/bulk
```

**Request Body:**
```json
{
  "urls": [
    { "original_url": "https://example.com/page1" },
    { "original_url": "https://example.com/page2" },
    { "original_url": "https://example.com/page3" }
  ]
}
```

**Response (Success - 201 Created):**
```json
{
  "success": true,
  "data": {
    "created": 3,
    "failed": 0,
    "results": [
      { "short_code": "aB7x3K", "original_url": "https://example.com/page1" },
      { "short_code": "xY9zK2", "original_url": "https://example.com/page2" },
      { "short_code": "pQ8mN1", "original_url": "https://example.com/page3" }
    ]
  }
}
```

---

### API 7: Get QR Code

**Endpoint:**
```http
GET /v1/urls/{short_code}/qr
```

**Query Parameters:**
```
?size=300          // Image size in pixels (default: 200)
&format=png        // png, svg, jpeg
&color=000000      // Hex color (no #)
&bgColor=FFFFFF    // Background color
```

**Response (Success - 200 OK):**
```http
HTTP/1.1 200 OK
Content-Type: image/png

[PNG binary data]
```

---

### API 8: List User's URLs

**Endpoint:**
```http
GET /v1/urls
```

**Query Parameters:**
```
?limit=20          // Results per page (max 100)
&offset=0          // Pagination offset
&sort=created_at   // created_at, clicks, expires_at
&order=desc        // asc, desc
&tag=marketing     // Filter by tag
&search=blog       // Search in original_url
```

**Response (Success - 200 OK):**
```json
{
  "success": true,
  "data": {
    "urls": [
      {
        "short_code": "aB7x3K",
        "short_url": "https://short.ly/aB7x3K",
        "original_url": "https://example.com/page1",
        "created_at": "2025-01-15T10:30:00Z",
        "clicks": 1523,
        "tags": ["marketing", "twitter"]
      },
      {
        "short_code": "xY9zK2",
        "short_url": "https://short.ly/xY9zK2",
        "original_url": "https://example.com/page2",
        "created_at": "2025-01-14T08:15:00Z",
        "clicks": 842,
        "tags": ["blog"]
      }
    ],
    "pagination": {
      "total": 156,
      "limit": 20,
      "offset": 0,
      "has_more": true,
      "next_offset": 20
    }
  }
}
```

---

## 📊 Database Schema Design

### Database: PostgreSQL (URL Storage)

#### Table 1: urls

```sql
CREATE TABLE urls (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- URL Data
    short_code VARCHAR(10) UNIQUE NOT NULL,
    original_url TEXT NOT NULL,

    -- Ownership & Access
    user_id BIGINT,                    -- NULL for anonymous users
    password_hash VARCHAR(255),        -- NULL if not password protected

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,              -- NULL if never expires
    last_accessed_at TIMESTAMP,

    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    is_custom_alias BOOLEAN DEFAULT FALSE,

    -- Analytics (Denormalized for Performance)
    click_count BIGINT DEFAULT 0,
    unique_click_count BIGINT DEFAULT 0,

    -- Constraints
    CONSTRAINT valid_url CHECK (original_url ~ '^https?://'),
    CONSTRAINT short_code_length CHECK (length(short_code) >= 3 AND length(short_code) <= 10)
);

-- Indexes for Fast Lookups
CREATE UNIQUE INDEX idx_urls_short_code ON urls(short_code);
CREATE INDEX idx_urls_user_id ON urls(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX idx_urls_created_at ON urls(created_at DESC);
CREATE INDEX idx_urls_expires_at ON urls(expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_urls_active ON urls(is_active) WHERE is_active = TRUE;

-- Partial Index for Active URLs Only (Most Common Query)
CREATE UNIQUE INDEX idx_urls_active_short_code ON urls(short_code) WHERE is_active = TRUE;
```

**Why These Indexes?**
```
idx_urls_short_code:
  - Most critical! Every redirect needs this
  - Unique constraint prevents duplicates

idx_urls_user_id:
  - User wants to see "my URLs"
  - Partial index (only non-NULL) saves space

idx_urls_expires_at:
  - Cleanup job: Find expired URLs to delete
  - Partial index (only non-NULL) for efficiency
```

---

#### Table 2: users

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    -- Authentication
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    -- Profile
    full_name VARCHAR(255),
    avatar_url TEXT,

    -- Subscription
    tier VARCHAR(20) DEFAULT 'free',  -- free, pro, enterprise
    subscription_expires_at TIMESTAMP,

    -- Quotas
    daily_url_limit INT DEFAULT 100,
    monthly_url_limit INT DEFAULT 1000,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,

    -- Status
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE UNIQUE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_tier ON users(tier);
```

---

#### Table 3: url_tags

```sql
CREATE TABLE url_tags (
    id BIGSERIAL PRIMARY KEY,
    url_id BIGINT REFERENCES urls(id) ON DELETE CASCADE,
    tag VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(url_id, tag)  -- Prevent duplicate tags on same URL
);

CREATE INDEX idx_url_tags_url_id ON url_tags(url_id);
CREATE INDEX idx_url_tags_tag ON url_tags(tag);
```

---

### Database: ClickHouse (Analytics)

#### Table 1: clicks (Raw Events)

```sql
CREATE TABLE clicks (
    -- Event Identity
    event_id UUID DEFAULT generateUUIDv4(),
    short_code String,

    -- Timestamp
    timestamp DateTime,
    date Date DEFAULT toDate(timestamp),

    -- User Info
    ip String,
    session_id String,
    user_id UInt64,  -- 0 for anonymous

    -- Geographic
    country FixedString(2),  -- ISO country code (US, GB, etc.)
    city String,
    latitude Float32,
    longitude Float32,

    -- Device & Browser
    device_type LowCardinality(String),  -- mobile, desktop, tablet
    os LowCardinality(String),           -- iOS, Android, Windows, macOS
    browser LowCardinality(String),      -- Chrome, Firefox, Safari
    user_agent String,

    -- Referrer
    referrer String,
    referrer_domain LowCardinality(String),

    -- Performance
    response_time_ms UInt16

) ENGINE = MergeTree()
PARTITION BY toYYYYMM(date)  -- Partition by month (easy to drop old data)
ORDER BY (short_code, date, timestamp)
TTL date + INTERVAL 1 YEAR;  -- Auto-delete after 1 year
```

**Why This Schema?**
```
LowCardinality:
  - For columns with few unique values (device_type, os, browser)
  - 10x compression! (saves disk and memory)

PARTITION BY toYYYYMM(date):
  - Each month is a separate partition
  - Easy to drop: ALTER TABLE clicks DROP PARTITION '202501'

TTL date + INTERVAL 1 YEAR:
  - Auto-delete data older than 1 year
  - No manual cleanup needed!

ORDER BY (short_code, date, timestamp):
  - Optimizes queries: WHERE short_code = 'aB7x3K' AND date = '2025-01-15'
  - Data stored sorted on disk
```

---

#### Table 2: daily_stats (Aggregated)

```sql
CREATE TABLE daily_stats (
    short_code String,
    date Date,

    -- Counts
    click_count UInt64,
    unique_ip_count AggregateFunction(uniq, String),  -- HyperLogLog sketch
    unique_session_count AggregateFunction(uniq, String),

    -- Top Values
    top_country String,
    top_device String,
    top_referrer String

) ENGINE = AggregatingMergeTree()
PARTITION BY toYYYYMM(date)
ORDER BY (short_code, date);

-- Materialized View (Auto-Updates daily_stats)
CREATE MATERIALIZED VIEW daily_stats_mv TO daily_stats
AS SELECT
    short_code,
    toDate(timestamp) as date,
    count() as click_count,
    uniqState(ip) as unique_ip_count,
    uniqState(session_id) as unique_session_count,
    any(country) as top_country,  -- Simplified, should use topK
    any(device_type) as top_device,
    any(referrer_domain) as top_referrer
FROM clicks
GROUP BY short_code, date;
```

---

## 🔑 Short Code Generation Strategies

### Strategy 1: Counter-Based (Simple)

**How It Works:**
```
Global counter starts at 1
Each new URL increments counter
Encode counter in Base62

Counter: 1 → Base62: "1"
Counter: 62 → Base62: "10"
Counter: 3843 → Base62: "100"
Counter: 238327 → Base62: "1000"
```

**Base62 Encoding:**
```
Characters: 0-9, a-z, A-Z (62 total)
Base62 = More compact than decimal

Decimal:  123456 (6 digits)
Base62:   "W7E"  (3 characters)
```

**Code:**
```javascript
const BASE62 = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';

function encodeBase62(num) {
  if (num === 0) return '0';

  let encoded = '';
  while (num > 0) {
    encoded = BASE62[num % 62] + encoded;
    num = Math.floor(num / 62);
  }
  return encoded;
}

function decodeBase62(str) {
  let decoded = 0;
  for (let i = 0; i < str.length; i++) {
    decoded = decoded * 62 + BASE62.indexOf(str[i]);
  }
  return decoded;
}

// Generate short code
async function generateShortCode() {
  // Atomic increment in Redis
  const counter = await redis.incr('url_counter');
  return encodeBase62(counter).padStart(7, '0');  // Min 7 characters
}

// Example:
// Counter: 1 → Short Code: "0000001"
// Counter: 3521614606208 → Short Code: "zzzzzzz" (max 7-char Base62)
```

**Pros:**
- ✅ Simple to implement
- ✅ Predictable length
- ✅ No collisions (counter is unique)

**Cons:**
- ❌ Sequential (can guess next URL: "0000001", "0000002", "0000003")
- ❌ Reveals total URLs created (security/privacy concern)
- ❌ Single point of failure (counter must be centralized)

---

### Strategy 2: Hash-Based (Our Choice!)

**How It Works:**
```
1. Take original URL + timestamp + random salt
2. Hash using MD5 or SHA256
3. Take first 7 characters of hash
4. Encode in Base62
5. Check if exists in DB, if yes → retry with different salt
```

**Code:**
```javascript
const crypto = require('crypto');

function generateShortCodeHash(originalURL) {
  let shortCode;
  let attempts = 0;
  const maxAttempts = 5;

  while (attempts < maxAttempts) {
    // Create unique input (URL + timestamp + random salt)
    const input = `${originalURL}${Date.now()}${Math.random()}`;

    // Hash with MD5 (fast, collision resistance not critical here)
    const hash = crypto.createHash('md5').update(input).digest('hex');

    // Take first 7 characters and convert to Base62
    shortCode = hash.substring(0, 7);

    // Check if exists in database
    const exists = await db.query(
      'SELECT 1 FROM urls WHERE short_code = $1',
      [shortCode]
    );

    if (!exists) {
      return shortCode;  // Found unique code!
    }

    attempts++;
  }

  // Fallback to counter-based if too many collisions (very rare!)
  return encodeBase62(await redis.incr('url_counter_fallback'));
}
```

**Pros:**
- ✅ Random (can't guess next URL)
- ✅ No central counter needed (distributed-friendly)
- ✅ Obfuscates total URL count

**Cons:**
- ❌ Collision possible (need retry logic)
- ❌ Slightly more complex

---

### Strategy 3: UUID-Based (Not Recommended)

**How It Works:**
```
Generate UUID: 550e8400-e29b-41d4-a716-446655440000
Take first 8 characters: 550e8400
Use as short code
```

**Cons:**
- ❌ Too long (8+ characters)
- ❌ Not URL-friendly (contains dashes)
- ❌ Wastes short code space

---

### Collision Probability Calculation

**Question:** With 1 billion URLs and 7-character Base62, what's collision probability?

**Math:**
```
Total possible codes: 62^7 = 3,521,614,606,208 (3.5 trillion)
URLs created: 1,000,000,000 (1 billion)

Collision probability ≈ (n^2) / (2 × N)
Where:
  n = number of URLs (1 billion)
  N = total possible codes (3.5 trillion)

P(collision) ≈ (1,000,000,000)^2 / (2 × 3,521,614,606,208)
             ≈ 1,000,000,000,000,000,000 / 7,043,229,212,416
             ≈ 0.142 (14.2%)
```

**Beginner Insight:**
```
With 1 billion URLs:
- 14% chance of at least one collision ever
- But we retry on collision, so it's handled!

Birthday Paradox applies here:
- Collision probability grows faster than you think
- But still very low for first few million URLs
```

---

## 🎓 Beginner Concepts Explained

### Concept 1: Database Normalization vs Denormalization

**Normalized (Separate Tables):**
```sql
-- urls table
id | short_code | original_url | user_id
1  | aB7x3K     | https://...  | 123

-- url_clicks table (separate)
id | url_id | clicked_at
1  | 1      | 2025-01-15 10:30
2  | 1      | 2025-01-15 10:31
3  | 1      | 2025-01-15 10:32

-- To get click count:
SELECT COUNT(*) FROM url_clicks WHERE url_id = 1;  -- Slow! Scans all rows
```

**Denormalized (click_count in urls table):**
```sql
-- urls table
id | short_code | original_url | user_id | click_count
1  | aB7x3K     | https://...  | 123     | 3

-- To get click count:
SELECT click_count FROM urls WHERE id = 1;  -- Fast! Single row lookup
```

**Trade-Off:**
```
Normalized:
  ✅ No data duplication
  ❌ Slower queries (need JOINs)

Denormalized:
  ✅ Faster queries
  ❌ Data duplication (click count stored twice: urls table + clicks table)
  ❌ Risk of inconsistency
```

**Our Choice:** Denormalize click_count for fast reads!

---

### Concept 2: Database Indexes - Deep Dive

**Without Index:**
```sql
-- Find URL with short_code = 'aB7x3K'
-- Database scans EVERY row (Full Table Scan)

Row 1: aB7x3K? No
Row 2: xY9zK2? No
Row 3: pQ8mN1? No
...
Row 1,000,000,000: aB7x3K? YES! Found it!

Time: 10 seconds 💀
```

**With Index (B-Tree):**
```sql
-- Index is a sorted tree structure

         [m]
        /   \
      [d]   [t]
     /  \   /  \
   [a] [f] [p] [x]
    |
 [aB7x3K] → Points to Row 1

Search steps:
1. Start at root: 'a' < 'm', go left
2. 'a' < 'd', go left
3. Found 'a' branch
4. Binary search in 'a' branch for 'aB7x3K'

Total comparisons: ~30 (log₂ of 1 billion)

Time: 0.001 seconds ⚡ (10,000x faster!)
```

---

### Concept 3: Partial Index

**Regular Index:**
```sql
CREATE INDEX idx_expires_at ON urls(expires_at);

-- Indexes ALL rows, even where expires_at IS NULL
-- Wastes space!
```

**Partial Index:**
```sql
CREATE INDEX idx_expires_at ON urls(expires_at)
WHERE expires_at IS NOT NULL;

-- Only indexes rows with expiration
-- If 90% of URLs don't expire → 90% smaller index!
```

---

## ✅ Summary

### API Design:
✅ **8 RESTful endpoints** (create, redirect, analytics, update, delete, bulk, QR, list)
✅ **Proper HTTP status codes** (201, 302, 400, 404, 429)
✅ **Rate limiting headers** (X-RateLimit-*)
✅ **Pagination** for list endpoints
✅ **Versioning** (/v1/)
✅ **Detailed error messages**

### Database Design:
✅ **PostgreSQL** for URL storage (ACID compliance)
✅ **ClickHouse** for analytics (60x faster queries)
✅ **Optimized indexes** (unique on short_code, partial on expires_at)
✅ **Denormalized** click_count for performance
✅ **Partitioning** by month in ClickHouse
✅ **Auto-cleanup** with TTL (1 year)

### Short Code Generation:
✅ **Hash-based** strategy (random, secure)
✅ **Base62 encoding** (compact)
✅ **7 characters** = 3.5 trillion combinations
✅ **Collision handling** with retry logic

---

**Previous:** [← Step 4 - Analytics & Rate Limiting](05_step4_analytics_and_rate_limiting.md)
**Next:** [Data Flow Diagrams →](07_data_flow_diagrams.md)
