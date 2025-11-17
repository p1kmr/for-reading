# Common Mistakes & How to Avoid Them

## Table of Contents
1. [Architecture Mistakes](#architecture-mistakes)
2. [Database Mistakes](#database-mistakes)
3. [Caching Mistakes](#caching-mistakes)
4. [Real-Time System Mistakes](#real-time-system-mistakes)
5. [Scalability Mistakes](#scalability-mistakes)
6. [Security Mistakes](#security-mistakes)
7. [Interview-Specific Mistakes](#interview-specific-mistakes)

---

## Introduction

Learning from mistakes is crucial for becoming a better system designer. This document covers **15 common mistakes** beginners make when designing Google Docs (or similar systems), with:

- ❌ **Wrong Approach**: What not to do
- ✅ **Right Solution**: How to do it correctly
- 🎯 **Why It Matters**: Real-world impact
- 💡 **Code Example**: Practical implementation

---

## Architecture Mistakes

### Mistake #1: Storing Document Content in Database

❌ **Wrong Approach:**

```python
# Storing full document content in PostgreSQL
CREATE TABLE documents (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(255),
    title VARCHAR(500),
    content TEXT,  # ❌ This can be 1 MB+
    created_at TIMESTAMP
);

# Fetching document
def get_document(doc_id):
    # Fetches entire 1 MB document from database
    doc = db.query("SELECT * FROM documents WHERE id = ?", doc_id)
    return doc
```

**Problems:**
- Database bloat (documents can be 100 KB - 10 MB)
- Slow queries (fetching large TEXT fields)
- Expensive database storage (50 TB of docs)
- Backup/restore takes forever
- Database not optimized for large objects

---

✅ **Right Solution:**

```python
# Store metadata in database, content in S3
CREATE TABLE documents (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(255),
    title VARCHAR(500),
    content_s3_key VARCHAR(1000),  # ✅ Pointer to S3 object
    size_bytes INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

# Store content in S3
def save_document(doc_id, content):
    # 1. Save content to S3 (object storage)
    s3_key = f"documents/{doc_id}/content.json"
    s3.put_object(
        Bucket='docs-content',
        Key=s3_key,
        Body=content,
        ContentType='application/json'
    )

    # 2. Save metadata to database (small, fast)
    db.execute("""
        UPDATE documents
        SET content_s3_key = ?,
            size_bytes = ?,
            updated_at = NOW()
        WHERE id = ?
    """, s3_key, len(content), doc_id)

# Fetch document
def get_document(doc_id):
    # 1. Get metadata from database (fast)
    metadata = db.query("SELECT * FROM documents WHERE id = ?", doc_id)

    # 2. Get content from S3 (in parallel)
    content = s3.get_object(
        Bucket='docs-content',
        Key=metadata.content_s3_key
    )

    return {
        **metadata,
        'content': content
    }
```

**Benefits:**
✅ Database stays small and fast (only metadata)
✅ S3 is cheaper ($0.023/GB vs $0.115/GB for database)
✅ S3 provides 99.999999999% durability (11 nines)
✅ Easy to serve via CDN for global access
✅ Database backups are quick

---

🎯 **Why It Matters:**

```
Cost Comparison (5 billion documents, 60 KB average):

Database Storage (PostgreSQL):
- Size: 5B × 60 KB = 300 TB
- Cost: 300,000 GB × $0.115/GB = $34,500/month

S3 Storage:
- Size: 5B × 60 KB = 300 TB
- Cost: 300,000 GB × $0.023/GB = $6,900/month

Savings: $27,600/month = $331,200/year! 💰

Plus:
- Database query time: 500ms → 50ms (10x faster)
- Database backup time: 10 hours → 30 minutes
```

---

### Mistake #2: Not Planning for Multi-Region from the Start

❌ **Wrong Approach:**

```
Single Region Architecture:
┌──────────────────────────────┐
│   ALL infrastructure in      │
│   US-EAST-1 only             │
│                              │
│   - App Servers              │
│   - Databases                │
│   - Storage                  │
└──────────────────────────────┘

Problems:
- EU users: 200ms latency (vs 50ms if regional)
- Asia users: 300ms latency
- Single point of failure (region outage = total downtime)
- Can't comply with GDPR (EU data must stay in EU)
```

---

✅ **Right Solution:**

```
Multi-Region Architecture (Planned from Day 1):

┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   US-EAST-1     │  │   EU-WEST-1     │  │  AP-SOUTHEAST-1 │
│   (Primary)     │  │   (Secondary)   │  │   (Secondary)   │
├─────────────────┤  ├─────────────────┤  ├─────────────────┤
│ - App Servers   │  │ - App Servers   │  │ - App Servers   │
│ - DB (Master)   │  │ - DB (Replica)  │  │ - DB (Replica)  │
│ - S3 (Primary)  │  │ - S3 (Replica)  │  │ - S3 (Replica)  │
└─────────────────┘  └─────────────────┘  └─────────────────┘
         │                    │                    │
         └────────────────────┴────────────────────┘
                        │
                    Route 53
                  (Geo-Routing)
                        │
                      Users

Design Decisions:
✓ Use region-agnostic IDs (UUID, not auto-increment)
✓ Design for eventual consistency (cross-region lag)
✓ Use cross-region replication (RDS, S3)
✓ Abstract region logic (code doesn't hardcode region)
```

**Code Example:**

```python
# ❌ BAD: Hardcoded region
S3_BUCKET = "docs-content-us-east-1"  # Can't expand to EU!

# ✅ GOOD: Region-aware configuration
import boto3
from config import get_current_region

def get_s3_bucket():
    region = get_current_region()  # us-east-1, eu-west-1, etc.
    return f"docs-content-{region}"

# ❌ BAD: Auto-increment IDs (different in each region)
CREATE TABLE documents (
    id SERIAL PRIMARY KEY  # US: id=1, EU: id=1 (collision!)
);

# ✅ GOOD: UUID (globally unique)
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid()
    # US: id=abc-123, EU: id=def-456 (no collision!)
);
```

---

🎯 **Why It Matters:**

```
Latency Impact:

User Location | Single Region | Multi-Region | Improvement
------------- | ------------- | ------------ | -----------
US            | 50ms          | 50ms         | 0ms
EU            | 200ms         | 50ms         | 150ms (4x faster!)
Asia          | 300ms         | 50ms         | 250ms (6x faster!)

Business Impact:
- Better UX → Higher retention (10-20% improvement)
- GDPR compliance → Can operate in EU (30% of users)
- High availability → 99.99% uptime (failover to another region)

Cost:
- 2x infrastructure cost ($170K vs $85K/month)
- Worth it? YES (better UX, compliance, availability)
```

---

## Database Mistakes

### Mistake #3: Not Using Database Indexes

❌ **Wrong Approach:**

```sql
-- No indexes on frequently queried columns
CREATE TABLE documents (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(255),  # ❌ No index!
    title VARCHAR(500),
    created_at TIMESTAMP,   # ❌ No index!
    updated_at TIMESTAMP
);

-- This query is SLOW (full table scan)
SELECT * FROM documents
WHERE owner_id = 'user_123'
ORDER BY updated_at DESC
LIMIT 50;

-- Execution Plan:
-- Seq Scan on documents (cost=0..250000 rows=5000000)
-- Planning time: 0.5ms
-- Execution time: 1850ms  ❌ Way too slow!
```

**Problems:**
- Full table scan (scans all 5M documents)
- Query takes 1.8 seconds (unacceptable)
- Database CPU at 95% (slow queries consume resources)
- Poor user experience (slow document list loading)

---

✅ **Right Solution:**

```sql
-- Add indexes on frequently queried columns
CREATE TABLE documents (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(255),
    title VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Index for: "Get user's documents"
CREATE INDEX idx_documents_owner ON documents(owner_id);

-- Composite index for: "Get user's documents, sorted by update time"
CREATE INDEX idx_owner_updated ON documents(owner_id, updated_at DESC);

-- Now the query is FAST (index scan)
SELECT * FROM documents
WHERE owner_id = 'user_123'
ORDER BY updated_at DESC
LIMIT 50;

-- Execution Plan:
-- Index Scan using idx_owner_updated (cost=0..25 rows=50)
-- Planning time: 0.3ms
-- Execution time: 12ms  ✓ 150x faster!
```

**Index Strategy:**

```sql
-- Rule 1: Index foreign keys
CREATE INDEX idx_permissions_doc ON permissions(document_id);
CREATE INDEX idx_permissions_user ON permissions(user_id);

-- Rule 2: Index frequently filtered columns
CREATE INDEX idx_documents_owner ON documents(owner_id);

-- Rule 3: Composite index for common query patterns
-- Query: Get user's recent documents
CREATE INDEX idx_owner_updated ON documents(owner_id, updated_at DESC);

-- Rule 4: Don't over-index!
-- Each index has cost:
-- - Write overhead (every INSERT/UPDATE must update index)
-- - Storage overhead (indexes take disk space)
-- - Maintenance overhead (VACUUM, REINDEX)

-- Only index if:
✓ Query is slow without it (>100ms)
✓ Query is frequent (>1000/sec)
✓ Table is large (>100K rows)
```

**Index Monitoring:**

```sql
-- Check if index is actually used
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,  -- How many times used
    idx_tup_read  -- Tuples read
FROM pg_stat_user_indexes
WHERE tablename = 'documents'
ORDER BY idx_scan DESC;

-- Result:
| indexname          | idx_scan | idx_tup_read |
|--------------------|----------|--------------|
| idx_owner_updated  | 5000000  | 250000000    | ✓ Heavily used
| idx_documents_owner| 1000000  | 50000000     | ✓ Used
| idx_created_at     | 10       | 500          | ❌ Rarely used (remove?)

-- Drop unused indexes (saves write overhead)
DROP INDEX idx_created_at;
```

---

🎯 **Why It Matters:**

```
Performance Impact:

Without Index:
- Query time: 1850ms (P99)
- Queries/sec server can handle: ~50
- Database CPU: 95% (maxed out)
- User experience: Slow, frustrating

With Index:
- Query time: 12ms (P99) ✓ 150x faster
- Queries/sec server can handle: ~8,000
- Database CPU: 40%
- User experience: Fast, responsive ✓

Capacity Improvement:
- Before: Need to shard at 10M users
- After: Can handle 100M users on single shard ✓
```

---

### Mistake #4: Using Auto-Increment IDs Instead of UUIDs

❌ **Wrong Approach:**

```sql
-- Auto-increment IDs
CREATE TABLE documents (
    id SERIAL PRIMARY KEY,  -- ❌ 1, 2, 3, 4...
    owner_id INTEGER,
    title VARCHAR(500)
);

-- Problems in distributed system:

Problem 1: ID Conflicts in Multi-Region
US Region: INSERT → id = 1
EU Region: INSERT → id = 1  ❌ Conflict!

Problem 2: Predictable IDs (Security Risk)
Document URLs: /documents/1, /documents/2, ...
Attacker can enumerate: /documents/1, /documents/2, ...
Can access all documents if authorization check missing!

Problem 3: Hard to Shard
Shard 0: ids 1-1000000
Shard 1: ids 1000001-2000000
What if Shard 0 fills up? Need to rebalance (expensive)

Problem 4: Exposes Business Metrics
Document id = 5000000
Competitor knows: "They have 5M documents"
```

---

✅ **Right Solution:**

```sql
-- Use UUIDs (Universally Unique Identifiers)
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID,  -- Also UUID
    title VARCHAR(500)
);

-- Example UUIDs:
-- 550e8400-e29b-41d4-a716-446655440000
-- 6ba7b810-9dad-11d1-80b4-00c04fd430c8

-- Benefits:

Benefit 1: Globally Unique
US Region: id = 550e8400-e29b-41d4-a716-446655440000
EU Region: id = 6ba7b810-9dad-11d1-80b4-00c04fd430c8
No conflicts! ✓

Benefit 2: Non-Predictable (Security)
Document URL: /documents/550e8400-e29b-41d4-a716-446655440000
Attacker can't enumerate (2^128 possibilities)

Benefit 3: Easy to Shard
Shard by hash(uuid) - evenly distributed
No need to coordinate ID generation across shards

Benefit 4: No Business Info Leakage
UUID doesn't reveal how many documents you have
```

**Code Example:**

```python
import uuid

# Generate UUID v4 (random)
doc_id = str(uuid.uuid4())
# Output: '550e8400-e29b-41d4-a716-446655440000'

# Insert into database
db.execute("""
    INSERT INTO documents (id, owner_id, title)
    VALUES (?, ?, ?)
""", doc_id, owner_id, title)

# URL
url = f"/api/documents/{doc_id}"
# https://docs.example.com/api/documents/550e8400-e29b-41d4-a716-446655440000
```

**UUID Types:**

```
UUID v1: Time-based + MAC address
- Example: 6ba7b810-9dad-11d1-80b4-00c04fd430c8
- Pros: Can extract timestamp
- Cons: Leaks MAC address (privacy concern)
- Use case: Audit logs (need timestamp)

UUID v4: Random
- Example: 550e8400-e29b-41d4-a716-446655440000
- Pros: Fully random, no leaks
- Cons: No ordering
- Use case: Primary keys ✓ (Recommended)

UUID v7: Time-ordered (new in 2025)
- Example: 017f22e2-79b0-7cc3-98c4-dc0c0c07398f
- Pros: Sortable by time, random
- Cons: New standard (not all DBs support)
- Use case: Best of both worlds ✓
```

---

🎯 **Why It Matters:**

```
Security Impact:

Auto-Increment IDs:
- Document 1: /documents/1
- Document 2: /documents/2
- ...
- Document 5000000: /documents/5000000

Attacker script:
for i in range(1, 5000001):
    access(f"/documents/{i}")

If ANY document has broken authorization:
→ Attacker finds it! ❌

UUIDs:
- Document: /documents/550e8400-e29b-41d4-a716-446655440000
- Attack: Try random UUIDs (2^128 = 340 undecillion possibilities)
- Probability of guessing: Effectively 0 ✓

Real-world breach:
- Company X used auto-increment IDs
- Authorization check had bug
- Attacker enumerated all documents
- Leaked 10M private documents
- Cost: $50M fine + reputation damage
```

---

## Caching Mistakes

### Mistake #5: Caching Everything (Including Large Objects)

❌ **Wrong Approach:**

```python
# Cache entire documents in Redis
def get_document(doc_id):
    # Check cache
    cached = redis.get(f"doc:{doc_id}")
    if cached:
        return json.loads(cached)

    # Fetch from database/S3
    doc = fetch_document_from_s3(doc_id)

    # ❌ Cache entire document (can be 1 MB!)
    redis.setex(
        f"doc:{doc_id}",
        300,  # 5 minutes
        json.dumps(doc)  # ❌ Large object!
    )

    return doc

# Problems:
# 1. Redis memory exhaustion
#    - 1000 documents × 1 MB = 1 GB (just for docs)
#    - Evicts other useful small objects (sessions, permissions)
#
# 2. Network overhead
#    - Fetching 1 MB from Redis takes 100ms (network bound)
#    - Same as fetching from S3 via CDN!
#    - No performance benefit!
#
# 3. Cache invalidation complexity
#    - Document edited → Need to invalidate entire 1 MB
#    - Wasted bandwidth
```

---

✅ **Right Solution:**

```python
# Cache only small, frequently accessed metadata
def get_document_metadata(doc_id):
    # Check cache (small object: ~2 KB)
    cached = redis.get(f"doc:{doc_id}:metadata")
    if cached:
        return json.loads(cached)

    # Fetch from database
    metadata = db.query("""
        SELECT id, title, owner_id, created_at, updated_at, size_bytes
        FROM documents
        WHERE id = ?
    """, doc_id)

    # ✅ Cache metadata only (small!)
    redis.setex(
        f"doc:{doc_id}:metadata",
        300,  # 5 minutes
        json.dumps(metadata)
    )

    return metadata

def get_document_content(doc_id):
    # ✅ Never cache content - fetch from S3 directly
    # S3 is fast (50ms via CDN)
    # S3 has unlimited storage
    return s3.get_object(
        Bucket='docs-content',
        Key=f"documents/{doc_id}/content.json"
    )

def get_full_document(doc_id):
    # Fetch both in parallel
    metadata, content = await asyncio.gather(
        get_document_metadata(doc_id),
        get_document_content(doc_id)
    )

    return {**metadata, 'content': content}
```

**What to Cache:**

```python
# ✅ GOOD: Cache small, frequently accessed data

# 1. User Sessions (5 KB, accessed on every request)
redis.setex(f"session:{session_id}", 86400, session_data)

# 2. Permissions (500 bytes, checked on every operation)
redis.setex(f"perm:{user_id}:{doc_id}", 600, permission_role)

# 3. Document Metadata (2 KB, displayed in document list)
redis.setex(f"doc:{doc_id}:metadata", 300, metadata)

# 4. User's Document List (10 KB, frequently accessed)
redis.setex(f"user:{user_id}:docs", 120, document_list)

# ❌ BAD: Don't cache large or rarely accessed data

# 1. Full Document Content (1 MB, changes frequently)
# → Fetch from S3 instead

# 2. Version History (100 MB per document)
# → Store in S3, fetch on demand

# 3. Exports (PDF - 5 MB)
# → Generate once, store in S3

# 4. Analytics Data (GB of logs)
# → Use Elasticsearch or BigQuery
```

**Cache Size Planning:**

```python
# Calculate cache memory needed

data = {
    "sessions": {
        "count": 500000,  # 500K concurrent users
        "size_per": 5 * 1024,  # 5 KB
        "total_mb": 500000 * 5 / 1024  # 2,441 MB
    },
    "permissions": {
        "count": 1000000,  # 1M permission checks/min (cache hot ones)
        "size_per": 500,  # 500 bytes
        "total_mb": 1000000 * 500 / 1024 / 1024  # 476 MB
    },
    "doc_metadata": {
        "count": 100000,  # Cache top 100K docs
        "size_per": 2 * 1024,  # 2 KB
        "total_mb": 100000 * 2 / 1024  # 195 MB
    }
}

total_mb = sum(d["total_mb"] for d in data.values())
# Total: 3,112 MB ≈ 3 GB

# Redis instance: r5.xlarge (26 GB RAM)
# Usage: 3 GB / 26 GB = 11.5% ✓ Plenty of headroom!

# If we cached full documents (1 MB each):
# 100K docs × 1 MB = 100 GB
# Would need: r5.8xlarge (208 GB RAM) = 10x more expensive! ❌
```

---

🎯 **Why It Matters:**

```
Performance & Cost Impact:

Scenario: Cache full documents (wrong)
┌────────────────────────────────────┐
│ Redis Memory: 100 GB               │
│ Instance: r5.8xlarge (208 GB)      │
│ Cost: $2.40/hr = $1,752/month      │
│                                    │
│ Performance:                       │
│ - Fetch from Redis: 100ms (1 MB)  │
│ - Eviction rate: High (thrashing) │
│ - Hit rate: 60% (poor)            │
└────────────────────────────────────┘

Scenario: Cache metadata only (right)
┌────────────────────────────────────┐
│ Redis Memory: 3 GB                 │
│ Instance: r5.xlarge (26 GB)        │
│ Cost: $0.30/hr = $219/month ✓      │
│                                    │
│ Performance:                       │
│ - Fetch from Redis: 2ms (2 KB)    │
│ - Eviction rate: Low               │
│ - Hit rate: 95% (excellent) ✓     │
└────────────────────────────────────┘

Savings: $1,533/month = $18,396/year 💰
Plus: Better performance (95% hit rate vs 60%)
```

---

### Mistake #6: Not Handling Cache Invalidation

❌ **Wrong Approach:**

```python
# Cache without invalidation
def update_document_title(doc_id, new_title):
    # Update database
    db.execute("""
        UPDATE documents
        SET title = ?, updated_at = NOW()
        WHERE id = ?
    """, new_title, doc_id)

    # ❌ Forgot to invalidate cache!
    # Cache still has old title!
    # Users see stale data for 5 minutes (TTL)

# Problem: Inconsistent data
# - Database: "New Title"
# - Cache: "Old Title" (stale!)
# - User sees: "Old Title" ❌
```

---

✅ **Right Solution:**

```python
# Cache with proper invalidation
def update_document_title(doc_id, new_title):
    # 1. Update database
    db.execute("""
        UPDATE documents
        SET title = ?, updated_at = NOW()
        WHERE id = ?
    """, new_title, doc_id)

    # 2. Invalidate cache (event-based)
    redis.delete(f"doc:{doc_id}:metadata")

    # 3. Also invalidate owner's document list
    owner_id = get_document_owner(doc_id)
    redis.delete(f"user:{owner_id}:docs")

    # 4. Optional: Pre-populate cache (cache warming)
    new_metadata = fetch_document_metadata(doc_id)
    redis.setex(
        f"doc:{doc_id}:metadata",
        300,
        json.dumps(new_metadata)
    )

# Now users always see fresh data! ✓
```

**Cache Invalidation Strategies:**

```python
# Strategy 1: Time-based (TTL only)
# Pros: Simple, no coordination needed
# Cons: Stale data for TTL duration
redis.setex("key", 300, value)  # Expires after 5 minutes

# Strategy 2: Event-based (Invalidate on change)
# Pros: Always fresh data
# Cons: Must remember to invalidate everywhere
def update():
    db.update()
    redis.delete("key")  # ✓ Invalidate

# Strategy 3: Hybrid (TTL + Event-based) ← RECOMMENDED
# Pros: Fresh data + fallback if invalidation fails
# Cons: Slightly more complex
def update():
    db.update()
    redis.delete("key")  # Invalidate
    # Even if delete fails, cache expires in 5 min (TTL)

redis.setex("key", 300, value)  # TTL as backup

# Strategy 4: Write-through cache
# Pros: Cache always consistent with DB
# Cons: Every write hits both DB and cache
def update():
    db.update()
    redis.setex("key", 300, new_value)  # Update cache

# Strategy 5: Write-behind cache (Async)
# Pros: Fastest writes (async DB update)
# Cons: Risk of data loss if cache fails
def update():
    redis.setex("key", 300, value)  # Write to cache
    queue.send("update_db", value)  # Async DB update
```

**Common Invalidation Patterns:**

```python
# Pattern 1: Single key invalidation
redis.delete(f"doc:{doc_id}")

# Pattern 2: Pattern-based invalidation (all keys matching pattern)
# When user's documents change, invalidate all caches
keys = redis.keys(f"user:{user_id}:*")
if keys:
    redis.delete(*keys)

# Pattern 3: Cascade invalidation
# Document shared → Invalidate all affected users' caches
def share_document(doc_id, shared_with_user_id):
    db.create_permission(doc_id, shared_with_user_id, 'viewer')

    # Invalidate sharer's cache
    owner_id = get_owner(doc_id)
    redis.delete(f"user:{owner_id}:docs")

    # Invalidate recipient's cache
    redis.delete(f"user:{shared_with_user_id}:docs")

    # Invalidate document metadata (permission count changed)
    redis.delete(f"doc:{doc_id}:metadata")

# Pattern 4: Tag-based invalidation (Advanced)
# Tag cache entries, invalidate by tag
redis.sadd(f"tag:user:{user_id}", "doc:123:metadata")
redis.sadd(f"tag:user:{user_id}", "doc:456:metadata")

# Invalidate all user's caches
keys = redis.smembers(f"tag:user:{user_id}")
redis.delete(*keys)
```

---

🎯 **Why It Matters:**

```
Data Consistency:

Without Invalidation:
Time  Action                  Database        Cache          User Sees
0:00  Create doc "Draft"      "Draft"         "Draft"        "Draft" ✓
0:01  Update to "Final"       "Final"         "Draft" (stale!) "Draft" ❌
0:02  User refreshes          "Final"         "Draft" (stale!) "Draft" ❌
0:05  Cache expires (TTL)     "Final"         (empty)         "Final" ✓

Problem: Users see stale data for 5 minutes!

With Invalidation:
Time  Action                  Database        Cache          User Sees
0:00  Create doc "Draft"      "Draft"         "Draft"        "Draft" ✓
0:01  Update to "Final"       "Final"         (invalidated)  "Final" ✓
0:02  User refreshes          "Final"         "Final"        "Final" ✓

✓ Users always see fresh data!

Real-world impact:
- User edits document title
- Without invalidation: Others see old title (confusing!)
- With invalidation: Everyone sees new title immediately ✓
```

---

## Real-Time System Mistakes

### Mistake #7: Using HTTP Polling Instead of WebSocket

❌ **Wrong Approach:**

```javascript
// Client polls server every second for updates
function pollForUpdates(documentId) {
    setInterval(() => {
        // ❌ HTTP request every second!
        fetch(`/api/documents/${documentId}/updates?since=${lastVersion}`)
            .then(response => response.json())
            .then(updates => {
                if (updates.length > 0) {
                    applyUpdates(updates);
                }
            });
    }, 1000);  // Poll every 1 second
}

// Problems:
// 1. High latency: 1 second delay (unacceptable for real-time)
// 2. Wasteful: 99% of requests return empty (no updates)
// 3. Overhead: Each request has HTTP headers (~500 bytes)
// 4. Server load: 10M users × 1 req/sec = 10M req/sec!
```

**Problems with Polling:**

```
Latency:
- User A types "Hello" at 0:00:00.000
- User B polls at 0:00:01.000
- User B sees "Hello" at 0:00:01.000
- Delay: 1 second (unacceptable for real-time!)

Overhead:
10M concurrent users polling every second:
- Requests: 10M req/sec
- Each request: 500 bytes (headers) + 100 bytes (empty response)
- Bandwidth: 10M × 600 bytes = 6 GB/sec = 48 Gbps!
- Cost: $10K/month just for polling empty responses ❌

Server Load:
- 10M req/sec → Need 500 app servers
- Each server: 20K req/sec (just to say "no updates")
- Wasteful!
```

---

✅ **Right Solution:**

```javascript
// Use WebSocket for real-time updates
const socket = new WebSocket('wss://docs.example.com/ws');

// Connect to document channel
socket.onopen = () => {
    socket.send(JSON.stringify({
        type: 'subscribe',
        document_id: documentId
    }));
};

// Receive real-time updates
socket.onmessage = (event) => {
    const update = JSON.parse(event.data);

    switch (update.type) {
        case 'edit':
            // Apply edit immediately (<100ms latency)
            applyOperation(update.operation);
            break;

        case 'cursor':
            // Show other user's cursor
            updateCursor(update.user_id, update.position);
            break;

        case 'presence':
            // Update who's online
            updatePresence(update.active_users);
            break;
    }
};

// Send edits to server
function sendEdit(operation) {
    socket.send(JSON.stringify({
        type: 'edit',
        operation: operation
    }));
}

// Benefits:
// ✓ Low latency: < 100ms (10x faster than polling)
// ✓ Efficient: Only send data when there are updates
// ✓ Bi-directional: Server can push to client
// ✓ Lower bandwidth: No HTTP headers on every message
```

**WebSocket vs Polling Comparison:**

```
Feature              | HTTP Polling     | WebSocket
-------------------- | ---------------- | -------------------
Latency              | 1 second         | <100ms ✓
Requests/sec (10M users) | 10M req/sec  | 0 (persistent connection)
Bandwidth (10M users)| 48 Gbps          | 2.4 Gbps ✓ (20x less)
Server load          | Very high        | Low ✓
Overhead             | HTTP headers     | Minimal ✓
Real-time            | No               | Yes ✓

Winner: WebSocket ✓
```

**Implementation:**

```python
# Server-side WebSocket handler (Python/asyncio)
import asyncio
import websockets
import json

# Store active connections per document
document_connections = {}  # {doc_id: [ws1, ws2, ws3, ...]}

async def handle_websocket(websocket, path):
    # Client connects
    doc_id = None

    try:
        async for message in websocket:
            data = json.loads(message)

            if data['type'] == 'subscribe':
                # Subscribe to document
                doc_id = data['document_id']

                # Authorize
                if not can_access(websocket.user_id, doc_id):
                    await websocket.send(json.dumps({
                        'error': 'Forbidden'
                    }))
                    await websocket.close()
                    return

                # Add to connections
                if doc_id not in document_connections:
                    document_connections[doc_id] = []
                document_connections[doc_id].append(websocket)

                # Send current active users
                await websocket.send(json.dumps({
                    'type': 'presence',
                    'active_users': len(document_connections[doc_id])
                }))

            elif data['type'] == 'edit':
                # Broadcast edit to all users in document
                operation = data['operation']

                # Apply OT transformation
                transformed_op = apply_ot(operation)

                # Broadcast to all users (except sender)
                await broadcast(doc_id, {
                    'type': 'edit',
                    'user_id': websocket.user_id,
                    'operation': transformed_op
                }, exclude=websocket)

    finally:
        # Client disconnects
        if doc_id and doc_id in document_connections:
            document_connections[doc_id].remove(websocket)

            # Notify others
            await broadcast(doc_id, {
                'type': 'presence',
                'active_users': len(document_connections[doc_id])
            })

async def broadcast(doc_id, message, exclude=None):
    """Broadcast message to all connections for a document"""
    if doc_id not in document_connections:
        return

    message_json = json.dumps(message)

    tasks = []
    for ws in document_connections[doc_id]:
        if ws != exclude:
            tasks.append(ws.send(message_json))

    # Send to all in parallel
    await asyncio.gather(*tasks, return_exceptions=True)
```

---

🎯 **Why It Matters:**

```
Cost Impact (10M concurrent users):

HTTP Polling:
- Requests: 10M req/sec
- App servers needed: 500 (20K req/sec each)
- Cost: 500 × $0.34/hr × 730hr = $124,100/month
- Bandwidth: 48 Gbps = $10,000/month
- Total: $134,100/month

WebSocket:
- Persistent connections: 10M
- WebSocket servers: 200 (50K connections each)
- Cost: 200 × $0.17/hr × 730hr = $24,820/month
- Bandwidth: 2.4 Gbps = $500/month
- Total: $25,320/month

Savings: $108,780/month = $1,305,360/year! 💰

Plus:
- Latency: 1s → 100ms (10x faster)
- User experience: Much better ✓
```

---

## Scalability Mistakes

### Mistake #8: Not Sharding the Database

❌ **Wrong Approach:**

```
Single Database (No Sharding):

┌─────────────────────────────┐
│    Single PostgreSQL DB     │
│                             │
│  - 100M users               │
│  - 5B documents             │
│  - 10K writes/sec           │
│                             │
│  Problems:                  │
│  - CPU: 95% (maxed out!)    │
│  - Disk I/O: 10K IOPS max   │
│  - Single point of failure  │
│  - Can't scale writes       │
└─────────────────────────────┘

Limitations:
- Max instance: db.r5.24xlarge (96 vCPU, 768 GB RAM)
- Cost: $13,104/month (expensive!)
- Still maxes out at ~20K writes/sec
- What if we need 50K writes/sec? ❌ Can't!
```

---

✅ **Right Solution:**

```
Sharded Database (4 Shards):

┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│ Shard 0  │  │ Shard 1  │  │ Shard 2  │  │ Shard 3  │
│ Users A-G│  │ Users H-N│  │ Users O-T│  │ Users U-Z│
├──────────┤  ├──────────┤  ├──────────┤  ├──────────┤
│ 25M users│  │ 25M users│  │ 25M users│  │ 25M users│
│ 1.25B docs│  │1.25B docs│  │1.25B docs│  │1.25B docs│
│ 2.5K w/s │  │ 2.5K w/s │  │ 2.5K w/s │  │ 2.5K w/s │
└──────────┘  └──────────┘  └──────────┘  └──────────┘

Benefits:
✓ Write capacity: 10K → 40K writes/sec (4x)
✓ Cheaper: 4 × db.r5.4xlarge < 1 × db.r5.24xlarge
✓ Better fault isolation (one shard down ≠ total outage)
✓ Linear scalability (8 shards → 80K writes/sec)
```

**Sharding Logic:**

```python
import hashlib

NUM_SHARDS = 4

def get_shard_id(user_id: str) -> int:
    """
    Determine which shard contains user's data

    Uses consistent hashing for even distribution
    """
    # Hash user_id (MD5 for consistent distribution)
    hash_value = int(hashlib.md5(user_id.encode()).hexdigest(), 16)

    # Modulo to get shard ID (0-3)
    shard_id = hash_value % NUM_SHARDS

    return shard_id

# Example:
get_shard_id("user_alice")  # → 2 (always goes to Shard 2)
get_shard_id("user_bob")    # → 0 (always goes to Shard 0)

# Create connection pool for each shard
shard_pools = {
    0: create_pool("shard-0.db.example.com"),
    1: create_pool("shard-1.db.example.com"),
    2: create_pool("shard-2.db.example.com"),
    3: create_pool("shard-3.db.example.com")
}

def get_user_documents(user_id: str):
    """Fetch user's documents from correct shard"""
    # 1. Determine shard
    shard_id = get_shard_id(user_id)

    # 2. Get connection to that shard
    db = shard_pools[shard_id].get_connection()

    # 3. Query (single shard, fast!)
    documents = db.query("""
        SELECT * FROM documents
        WHERE owner_id = ?
        ORDER BY updated_at DESC
    """, user_id)

    return documents
```

**When to Shard:**

```
Signs you need sharding:

1. Write throughput maxed out:
   - Current: 10K writes/sec
   - Capacity: 10K writes/sec
   - Utilization: 100% ❌
   - Action: Shard NOW!

2. Database size too large:
   - Current: 5 TB
   - Max recommended: 1 TB per instance
   - Action: Shard into 5+ shards

3. Queries getting slow:
   - Query time: 500ms (despite indexes)
   - Table size: 5B rows (too large)
   - Action: Shard to reduce table size

4. Replication lag increasing:
   - Lag: 5 seconds (was 100ms)
   - Cause: Too many writes
   - Action: Shard to reduce write load per DB

Recommended: Shard BEFORE hitting limits!
- Plan sharding at 70% capacity
- Implement at 80% capacity
- Don't wait until 100% (emergency!)
```

---

🎯 **Why It Matters:**

```
Scalability:

Without Sharding (Single DB):
- Max writes: 20K/sec
- What if we grow to 50K writes/sec?
  → Can't handle it! ❌
  → Need to shard (takes weeks to implement)
  → Emergency scramble!

With Sharding (4 Shards):
- Max writes: 40K/sec (current)
- Growing to 50K writes/sec?
  → Add 2 more shards (6 total) ✓
  → 60K writes/sec capacity
  → Planned, not emergency

Cost:

Single Large DB:
- Instance: db.r5.24xlarge
- Cost: $13,104/month
- Max writes: 20K/sec

4 Smaller DBs (Sharded):
- Instance: 4 × db.r5.4xlarge
- Cost: 4 × $2,976 = $11,904/month ✓ Cheaper!
- Max writes: 40K/sec ✓ 2x capacity!

Winner: Sharding (cheaper + more scalable)
```

---

## Security Mistakes

### Mistake #9: Storing Passwords in Plain Text

❌ **Wrong Approach:**

```python
# ❌ NEVER DO THIS!
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255),
    password VARCHAR(255)  -- ❌ Plain text!
);

def register(email, password):
    # ❌ Store password as-is (plain text)
    db.execute("""
        INSERT INTO users (email, password)
        VALUES (?, ?)
    """, email, password)

def login(email, password):
    user = db.query("""
        SELECT * FROM users
        WHERE email = ? AND password = ?
    """, email, password)

    if user:
        return "Login successful"
    else:
        return "Invalid credentials"

# Database contents:
| id   | email           | password    |
|------|-----------------|-------------|
| 1    | alice@gmail.com | MyPassword123 |  ❌ VISIBLE!
| 2    | bob@gmail.com   | SecretPass456 |  ❌ VISIBLE!

# Consequences:
# 1. Database leak → All passwords exposed
# 2. Admin can see passwords
# 3. Attacker who gains DB access gets all passwords
# 4. Users who reuse passwords → Attacker can access other accounts
```

---

✅ **Right Solution:**

```python
import bcrypt

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255),
    password_hash VARCHAR(255)  -- ✓ Hashed password
);

def register(email, password):
    # Hash password before storing
    salt = bcrypt.gensalt(rounds=12)  # 12 rounds = strong but not too slow
    password_hash = bcrypt.hashpw(password.encode(), salt)

    db.execute("""
        INSERT INTO users (email, password_hash)
        VALUES (?, ?)
    """, email, password_hash.decode())

def login(email, password):
    user = db.query("""
        SELECT * FROM users
        WHERE email = ?
    """, email)

    if not user:
        return "Invalid credentials"

    # Compare password with hash
    if bcrypt.checkpw(password.encode(), user.password_hash.encode()):
        return "Login successful"
    else:
        return "Invalid credentials"

# Database contents:
| id   | email           | password_hash                                                  |
|------|-----------------|----------------------------------------------------------------|
| 1    | alice@gmail.com | $2b$12$abcdefghijklmnopqrstuvwxyz123456789...                  |  ✓ Safe!
| 2    | bob@gmail.com   | $2b$12$xyzabcdefghijklmnopqrstuvwxyz987654...                  |  ✓ Safe!

# Even if database is leaked:
# - Attacker sees hashes (not passwords)
# - Can't reverse hash to get password
# - Would take millions of years to brute force (bcrypt is slow)
```

**Password Security Best Practices:**

```python
# 1. Use bcrypt (or argon2, scrypt - NOT md5/sha1!)
# Why? Bcrypt is:
# - Slow (takes 100ms to hash - good! Makes brute force hard)
# - Salted (unique salt per password - prevents rainbow tables)
# - Adaptive (can increase rounds as computers get faster)

# 2. Never log passwords
# ❌ BAD:
logger.info(f"User {email} logging in with password {password}")

# ✅ GOOD:
logger.info(f"User {email} attempting login")

# 3. Enforce strong passwords
def validate_password(password):
    if len(password) < 12:
        raise ValueError("Password must be at least 12 characters")

    if not any(c.isupper() for c in password):
        raise ValueError("Password must contain uppercase letter")

    if not any(c.isdigit() for c in password):
        raise ValueError("Password must contain a number")

    # Check against common passwords
    if password in common_passwords:
        raise ValueError("Password is too common")

    return True

# 4. Implement rate limiting (prevent brute force)
def check_login_rate_limit(email):
    key = f"login_attempts:{email}"
    attempts = redis.incr(key)

    if attempts == 1:
        redis.expire(key, 300)  # 5 minutes

    if attempts > 5:
        raise ValueError("Too many login attempts. Try again in 5 minutes.")

# 5. Use 2FA (Two-Factor Authentication)
def login_with_2fa(email, password, totp_code):
    # 1. Verify password
    if not verify_password(email, password):
        return "Invalid credentials"

    # 2. Verify TOTP code (Google Authenticator)
    if not verify_totp(email, totp_code):
        return "Invalid 2FA code"

    return "Login successful"
```

---

🎯 **Why It Matters:**

```
Real-World Breach:

Company X (2019):
- Stored 150M passwords in plain text
- Database breached by attacker
- All passwords leaked

Impact:
- $500M fine (GDPR violation)
- Reputation destroyed
- Users' other accounts compromised (password reuse)
- Class-action lawsuit ($200M settlement)

Total cost: $700M+ 💸

With bcrypt:
- Attacker gets hashes (useless)
- Can't brute force (would take millions of years)
- Users safe ✓
- Fine: $0 ✓
```

---

### Mistake #10: Missing Authorization Checks

❌ **Wrong Approach:**

```python
@app.route('/api/documents/<doc_id>', methods=['GET'])
def get_document(doc_id):
    # ❌ No authorization check!
    # Any authenticated user can access any document!

    document = db.query("SELECT * FROM documents WHERE id = ?", doc_id)

    if not document:
        return jsonify({"error": "Not found"}), 404

    return jsonify(document)

# Vulnerability:
# User A can access User B's private document!
# GET /api/documents/user_b_private_doc_123
# → Returns User B's document ❌
```

**Real Attack:**

```bash
# Attacker finds document ID somehow (Google Doc link, email, etc.)
curl https://api.docs.example.com/api/documents/abc-123-def-456 \
  -H "Authorization: Bearer attacker_token"

# Response: 200 OK
# { "title": "Company Secrets", "content": "..." }
# ❌ Attacker accessed private document!
```

---

✅ **Right Solution:**

```python
@app.route('/api/documents/<doc_id>', methods=['GET'])
@require_auth  # Ensures user is authenticated
def get_document(doc_id):
    user_id = get_current_user_id()

    # ✓ Authorization check (REQUIRED!)
    if not can_access_document(user_id, doc_id):
        # Log security event
        log_security_event("unauthorized_access_attempt", user_id, {
            "document_id": doc_id,
            "ip": request.remote_addr
        })

        return jsonify({"error": "Forbidden"}), 403

    # User is authorized - fetch document
    document = db.query("SELECT * FROM documents WHERE id = ?", doc_id)

    if not document:
        return jsonify({"error": "Not found"}), 404

    return jsonify(document)

def can_access_document(user_id: str, document_id: str) -> bool:
    """
    Check if user has permission to access document
    """
    # Check if user is owner
    doc = db.query("SELECT owner_id FROM documents WHERE id = ?", document_id)
    if not doc:
        return False

    if doc.owner_id == user_id:
        return True  # Owner always has access

    # Check permissions table
    permission = db.query("""
        SELECT role FROM permissions
        WHERE document_id = ? AND user_id = ?
    """, document_id, user_id)

    return permission is not None  # Has any permission (viewer, editor, etc.)
```

**Authorization Check Checklist:**

```python
# ✓ Always check authorization for:

# 1. Read operations
@app.route('/api/documents/<doc_id>', methods=['GET'])
def get_document(doc_id):
    if not can_access_document(user_id, doc_id):
        return 403  # ✓

# 2. Write operations
@app.route('/api/documents/<doc_id>', methods=['PUT'])
def update_document(doc_id):
    if not can_edit_document(user_id, doc_id):
        return 403  # ✓

# 3. Delete operations
@app.route('/api/documents/<doc_id>', methods=['DELETE'])
def delete_document(doc_id):
    if not can_delete_document(user_id, doc_id):
        return 403  # ✓

# 4. Sharing operations
@app.route('/api/documents/<doc_id>/share', methods=['POST'])
def share_document(doc_id):
    if not is_owner(user_id, doc_id):
        return 403  # ✓ Only owner can share

# 5. List operations
@app.route('/api/users/<user_id>/documents', methods=['GET'])
def list_user_documents(user_id):
    current_user = get_current_user_id()
    if current_user != user_id:
        return 403  # ✓ Can't list other users' documents
```

**Defense in Depth:**

```python
# Layer 1: API Authorization (Primary)
def get_document(doc_id):
    if not can_access_document(user_id, doc_id):
        return 403

# Layer 2: Database Row-Level Security (Backup)
# PostgreSQL RLS (Row-Level Security)
CREATE POLICY document_access_policy ON documents
FOR SELECT
USING (
    owner_id = current_user_id()
    OR
    EXISTS (
        SELECT 1 FROM permissions
        WHERE document_id = documents.id
        AND user_id = current_user_id()
    )
);

# Even if API check fails, database won't return unauthorized rows ✓

# Layer 3: Audit Logging (Detection)
log_security_event("document_access", user_id, {
    "document_id": doc_id,
    "action": "read",
    "authorized": is_authorized
})

# Detect anomalies (user accessing 1000 docs/min = data exfiltration?)
```

---

🎯 **Why It Matters:**

```
Real-World Breach:

Company Y (2021):
- API endpoints missing authorization checks
- Attacker enumerated document IDs:
  /api/documents/1
  /api/documents/2
  ...
  /api/documents/5000000

- Accessed 5M private documents
- Including:
  - Medical records
  - Financial documents
  - Personal information

Impact:
- HIPAA violation: $10M fine
- Class-action lawsuit: $50M settlement
- Reputation destroyed
- CEO resigned

Total cost: $60M+

With authorization checks:
- Attacker gets 403 Forbidden for all unauthorized documents
- 0 documents leaked ✓
- Fine: $0 ✓
```

---

## Interview-Specific Mistakes

### Mistake #11: Jumping to Solutions Without Understanding Requirements

❌ **Wrong Approach:**

```
Interviewer: "Design Google Docs."

Candidate: "Okay, so we'll use Kubernetes with 100 microservices,
Kafka, Cassandra, Elasticsearch, Redis, and we'll deploy across
20 regions globally..."

Interviewer: "Wait, how many users?"

Candidate: "Uh... I don't know. Didn't clarify requirements."

❌ Jumping to complex solutions without understanding the problem!
```

---

✅ **Right Solution:**

```
Interviewer: "Design Google Docs."

Candidate: "Great! Before I start, let me clarify a few things:

FUNCTIONAL REQUIREMENTS:
1. Do we need real-time collaboration (multiple users editing simultaneously)?
2. What about rich text formatting (bold, italics, images)?
3. Version history and rollback?
4. Sharing and permissions?
5. Offline access?

NON-FUNCTIONAL REQUIREMENTS:
1. How many users? (100, 1M, 100M?)
2. How many concurrent editors per document? (2-3 or 100+?)
3. Latency requirements? (<100ms, <1 second?)
4. Availability requirements? (99.9%, 99.99%?)
5. Geographic distribution? (Single country or global?)

Interviewer: "Good questions! Let's say 100M users, 10M DAU,
up to 100 concurrent editors per document, <200ms latency,
99.99% availability, global."

Candidate: "Perfect! Let me also do some back-of-envelope calculations..."

✓ Shows structured thinking
✓ Clarifies before designing
✓ Avoids over/under-engineering
```

---

### Mistake #12: Not Discussing Trade-Offs

❌ **Wrong Approach:**

```
Interviewer: "Why did you choose SQL over NoSQL?"

Candidate: "Because SQL is better."

Interviewer: "But NoSQL scales horizontally easily..."

Candidate: "Oh... uh... I didn't think about that."

❌ No analysis of alternatives or trade-offs!
```

---

✅ **Right Solution:**

```
Interviewer: "Why did you choose SQL over NoSQL?"

Candidate: "Great question! I considered both options:

SQL (PostgreSQL) - CHOSEN:
✓ Pros:
  - Strong consistency (needed for permissions)
  - ACID transactions (needed for sharing)
  - Complex queries (JOINs for permissions)
  - Team expertise

❌ Cons:
  - Harder to scale writes (mitigated with sharding)
  - Schema changes require migrations

NoSQL (MongoDB):
✓ Pros:
  - Easy horizontal scaling
  - Flexible schema

❌ Cons:
  - Eventual consistency (problematic for permissions)
  - No multi-document transactions
  - Complex queries difficult (no JOINs)

For Google Docs, I chose SQL because:
1. Strong consistency critical for access control
2. Complex permission relationships need JOINs
3. Can scale horizontally with sharding (addresses SQL's weakness)

However, I'd use NoSQL (S3) for document content storage
and Elasticsearch for full-text search. Hybrid approach! ✓"

Interviewer: "Excellent analysis!"

✓ Considered alternatives
✓ Explained trade-offs
✓ Justified decision
✓ Acknowledged hybrid approach
```

---

### Mistake #13: Not Mentioning Monitoring & Failure Scenarios

❌ **Wrong Approach:**

```
Interviewer: "How do you know the system is working correctly?"

Candidate: "Uh... users will complain if it's broken?"

Interviewer: "What if the database fails?"

Candidate: "I... didn't think about that."

❌ No proactive monitoring or failure planning!
```

---

✅ **Right Solution:**

```
Candidate: "For monitoring, I'd implement:

METRICS (Prometheus + Grafana):
- Request rate (req/sec)
- Latency (P50, P95, P99)
- Error rate (%)
- Active users
- Database metrics (CPU, connections, replication lag)
- Cache hit rate

LOGGING (ELK Stack):
- Structured logs (JSON format)
- Error logs (track failures)
- Security logs (unauthorized access attempts)

ALERTING (PagerDuty):
- Critical: Error rate >5%, P99 latency >1 second
- Warning: Error rate >1%, CPU >70%

For failure scenarios:

Database Master Fails:
1. Auto-failover to slave (30-60 seconds)
2. Impact: Brief write downtime, reads unaffected
3. Recovery: Automatic (Patroni/RDS)

Entire Region Fails:
1. Route53 detects failure (1 minute)
2. Route traffic to another region (2 minutes)
3. Impact: 3 minutes elevated latency
4. RTO: 3 minutes, RPO: 0 (no data loss)

App Server Crashes:
1. Load balancer health check fails (30 seconds)
2. Remove from pool
3. Auto-scaling launches replacement (5 minutes)
4. Impact: None (other servers handle traffic)

✓ Proactive monitoring
✓ Thought through failures
✓ Defined RTO/RPO
✓ Shows production readiness"
```

---

## Summary: Golden Rules

### Design Principles
1. ✅ **Clarify requirements first** - Don't jump to solutions
2. ✅ **Start simple, then optimize** - Basic architecture first, then add complexity
3. ✅ **Use the right tool for the job** - SQL for structured data, S3 for large objects, etc.
4. ✅ **Plan for scale from the start** - UUIDs, region-agnostic design, sharding strategy
5. ✅ **Design for failure** - Everything will fail, plan for it
6. ✅ **Monitor everything** - You can't fix what you can't measure

### Technical Best Practices
1. ✅ **Use UUIDs, not auto-increment IDs** - Multi-region compatibility
2. ✅ **Store large objects in S3, not database** - Cost and performance
3. ✅ **Index frequently queried columns** - 100x+ performance improvement
4. ✅ **Hash passwords with bcrypt** - Never plain text
5. ✅ **Always check authorization** - Defense in depth
6. ✅ **Use WebSocket for real-time** - Not HTTP polling
7. ✅ **Cache metadata, not large objects** - Redis for small, hot data
8. ✅ **Invalidate cache on updates** - TTL + event-based
9. ✅ **Shard before you need to** - Plan at 70%, implement at 80%
10. ✅ **Multi-region from day 1** - Global design, not retrofit

### Interview Best Practices
1. ✅ **Ask clarifying questions** - Understand requirements
2. ✅ **Do back-of-envelope calculations** - Show you can estimate scale
3. ✅ **Draw diagrams** - Visual explanations are clearer
4. ✅ **Discuss trade-offs** - No perfect solution, explain why you chose X over Y
5. ✅ **Think about failures** - What happens when X fails?
6. ✅ **Mention monitoring** - How do you know it works?
7. ✅ **Consider alternatives** - Show you explored options
8. ✅ **Think about security** - Authentication, authorization, encryption

---

**Next:** [Summary & Final Architecture](./14_summary.md) - One-page executive summary of the entire system!
