# Database Design

## Database Choice: PostgreSQL

**Why PostgreSQL?**
- ACID compliance (critical for notification tracking)
- Rich indexing (B-tree, GIN, BRIN for different query patterns)
- JSON support (flexible metadata storage)
- Mature replication and sharding tools
- Excellent query performance for structured data

---

## Schema Design

### Table 1: notifications

**Purpose:** Core table storing all notifications

```sql
CREATE TABLE notifications (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- User & Channel
    user_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('email', 'sms', 'push', 'in-app')),
    type VARCHAR(20) NOT NULL CHECK (type IN ('transactional', 'promotional', 'system')),

    -- Template & Content
    template_id VARCHAR(50),
    subject VARCHAR(500),
    body_text TEXT,
    body_html TEXT,
    variables JSONB,  -- Template variables: {"order_id": "123", "amount": "$99"}

    -- Status Tracking
    status VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (
        status IN ('pending', 'queued', 'sent', 'delivered', 'opened', 'clicked', 'failed', 'scheduled', 'cancelled')
    ),

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    scheduled_for TIMESTAMP,  -- NULL if immediate, set if scheduled
    queued_at TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,

    -- Retry & Error Handling
    retry_count INT DEFAULT 0,
    error_message TEXT,

    -- Provider Info
    provider VARCHAR(50),  -- sendgrid, twilio, fcm
    provider_message_id VARCHAR(255),

    -- Metadata
    metadata JSONB,  -- Flexible: {"campaign_id": "123", "source": "checkout"}

    -- Soft Delete
    deleted_at TIMESTAMP
);
```

**Indexes:**
```sql
-- Most common query: Get notifications by user
CREATE INDEX idx_notifications_user_id ON notifications(user_id) WHERE deleted_at IS NULL;

-- Query by status (for workers processing pending notifications)
CREATE INDEX idx_notifications_status ON notifications(status) WHERE status IN ('pending', 'queued');

-- Query scheduled notifications (scheduler worker)
CREATE INDEX idx_notifications_scheduled ON notifications(scheduled_for)
    WHERE status = 'scheduled' AND scheduled_for IS NOT NULL;

-- Query by created_at for analytics and cleanup
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- Composite index for user's recent notifications
CREATE INDEX idx_notifications_user_recent ON notifications(user_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- GIN index for metadata queries (e.g., find all notifications for campaign_id)
CREATE INDEX idx_notifications_metadata ON notifications USING GIN(metadata);
```

**Partitioning Strategy (for scale):**
```sql
-- Partition by created_at (monthly partitions)
CREATE TABLE notifications (
    -- columns same as above
) PARTITION BY RANGE (created_at);

-- Create partitions
CREATE TABLE notifications_2025_01 PARTITION OF notifications
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE TABLE notifications_2025_02 PARTITION OF notifications
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

-- Auto-create partitions with pg_partman extension
```

**Estimated Size:**
```
50 million notifications/day
Average row size: 2 KB
Daily size: 100 GB
Monthly size: 3 TB

With partitioning:
- Hot partition (current month): 3 TB (frequently queried)
- Old partitions: Moved to cheaper storage or archived
```

---

### Table 2: user_preferences

**Purpose:** Store user notification preferences

```sql
CREATE TABLE user_preferences (
    user_id BIGINT PRIMARY KEY,

    -- Channel Preferences
    email_enabled BOOLEAN DEFAULT TRUE,
    sms_enabled BOOLEAN DEFAULT TRUE,
    push_enabled BOOLEAN DEFAULT TRUE,
    in_app_enabled BOOLEAN DEFAULT TRUE,

    -- Type Preferences (per channel)
    email_transactional BOOLEAN DEFAULT TRUE,   -- Can't opt out (legal requirements)
    email_promotional BOOLEAN DEFAULT TRUE,
    email_system BOOLEAN DEFAULT TRUE,

    sms_transactional BOOLEAN DEFAULT TRUE,
    sms_promotional BOOLEAN DEFAULT FALSE,  -- Default OFF for expensive SMS

    push_transactional BOOLEAN DEFAULT TRUE,
    push_promotional BOOLEAN DEFAULT TRUE,
    push_system BOOLEAN DEFAULT TRUE,

    -- Quiet Hours
    quiet_hours_enabled BOOLEAN DEFAULT FALSE,
    quiet_hours_start TIME,  -- e.g., '22:00'
    quiet_hours_end TIME,    -- e.g., '08:00'
    timezone VARCHAR(50) DEFAULT 'UTC',  -- e.g., 'America/Los_Angeles'

    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Index for fast lookups (heavily cached in Redis)
CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);
```

**Caching Strategy:**
```
Redis key: user_pref:{user_id}
TTL: 1 hour
Cache hit rate: >99% (users rarely change preferences)

Cache invalidation: On UPDATE user_preferences
```

---

### Table 3: templates

**Purpose:** Notification templates with versioning

```sql
CREATE TABLE templates (
    id BIGSERIAL PRIMARY KEY,
    template_id VARCHAR(50) NOT NULL,  -- e.g., 'order_confirmation'
    version INT NOT NULL DEFAULT 1,

    -- Template Details
    name VARCHAR(200) NOT NULL,
    description TEXT,
    channel VARCHAR(20) NOT NULL,

    -- Content (for email)
    subject VARCHAR(500),
    body_text TEXT,  -- Plain text version
    body_html TEXT,  -- HTML version

    -- Content (for SMS/Push)
    body VARCHAR(500),  -- Short message for SMS/Push

    -- Variables
    variables JSONB,  -- Expected variables: ["order_id", "user_name", "amount"]

    -- Status
    active BOOLEAN DEFAULT FALSE,  -- Only one version active at a time
    approved_by BIGINT,  -- Admin who approved
    approved_at TIMESTAMP,

    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    -- Unique constraint: template_id + version
    UNIQUE(template_id, version)
);

-- Index for querying active template
CREATE INDEX idx_templates_active ON templates(template_id, active) WHERE active = TRUE;

-- Example data:
-- template_id='order_confirmation', version=1, active=false (old)
-- template_id='order_confirmation', version=2, active=true  (current)
-- template_id='order_confirmation', version=3, active=false (draft)
```

**Template Rendering:**
```javascript
// Fetch template from database or cache
const template = await getTemplate('order_confirmation');

// Render with variables
const rendered = template.body_html.replace(/\{\{(\w+)\}\}/g, (match, key) => {
    return variables[key] || match;
});

// Input:  "Your order {{order_id}} for {{amount}} is confirmed"
// Output: "Your order ORD-123 for $99.99 is confirmed"
```

---

### Table 4: notification_events

**Purpose:** Event log for notification lifecycle (for analytics)

```sql
CREATE TABLE notification_events (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL REFERENCES notifications(id),

    -- Event Details
    event_type VARCHAR(50) NOT NULL,  -- queued, sent, delivered, opened, clicked, bounced, failed
    occurred_at TIMESTAMP NOT NULL DEFAULT NOW(),

    -- Event Data
    provider_response JSONB,  -- Raw response from SendGrid/Twilio
    user_agent TEXT,  -- For email opens/clicks
    ip_address INET,  -- For tracking

    -- Metadata
    metadata JSONB
);

-- Partition by occurred_at (weekly partitions for high volume)
CREATE TABLE notification_events_2025_w01 PARTITION OF notification_events
    FOR VALUES FROM ('2025-01-01') TO ('2025-01-08');

-- Index for querying events by notification
CREATE INDEX idx_notification_events_notification_id ON notification_events(notification_id, occurred_at);

-- Index for analytics queries
CREATE INDEX idx_notification_events_type ON notification_events(event_type, occurred_at);
```

**Event Examples:**
```sql
-- Event: Notification queued
INSERT INTO notification_events (notification_id, event_type, occurred_at)
VALUES (123, 'queued', NOW());

-- Event: Email delivered
INSERT INTO notification_events (notification_id, event_type, provider_response)
VALUES (123, 'delivered', '{"message_id": "sg_abc123", "status": "delivered"}');

-- Event: Email opened
INSERT INTO notification_events (notification_id, event_type, user_agent, ip_address)
VALUES (123, 'opened', 'Mozilla/5.0...', '192.168.1.1');
```

---

### Table 5: rate_limits (Alternative to Redis)

**Purpose:** Track rate limits (can use Redis instead for better performance)

```sql
CREATE TABLE rate_limits (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    date DATE NOT NULL,  -- Track daily limits

    count INT DEFAULT 0,  -- Current count

    -- Composite key
    UNIQUE(user_id, channel, date)
);

-- Index for fast lookups
CREATE INDEX idx_rate_limits_lookup ON rate_limits(user_id, channel, date);

-- Check and increment rate limit (atomic)
INSERT INTO rate_limits (user_id, channel, date, count)
VALUES (123, 'email', CURRENT_DATE, 1)
ON CONFLICT (user_id, channel, date)
DO UPDATE SET count = rate_limits.count + 1
RETURNING count;

-- If count > limit, reject request
```

**Redis vs PostgreSQL for Rate Limiting:**
```
Redis:
✓ Faster (0.5ms vs 10ms)
✓ Atomic INCR operation
✓ Auto-expiry with TTL
✓ Lower database load

PostgreSQL:
✓ Persistent (survives restarts)
✓ No additional infrastructure
✓ Easier to query/analyze

Recommendation: Use Redis for rate limiting, PostgreSQL for historical analysis
```

---

## Sharding Strategy

### Sharding Key: user_id

```python
def get_shard(user_id):
    """Determine shard for this user"""
    return user_id % 3  # 3 shards

# user_id 100 → Shard 0
# user_id 101 → Shard 1
# user_id 102 → Shard 2
# user_id 103 → Shard 0 (wraps around)
```

**Shard Distribution:**
```
Shard 0: user_id % 3 == 0 → 33.3% of users
Shard 1: user_id % 3 == 1 → 33.3% of users
Shard 2: user_id % 3 == 2 → 33.4% of users
```

**Application-Level Routing:**
```javascript
class ShardedDatabase {
    constructor() {
        this.shards = {
            0: new PostgreSQL({ host: 'shard0.db.example.com' }),
            1: new PostgreSQL({ host: 'shard1.db.example.com' }),
            2: new PostgreSQL({ host: 'shard2.db.example.com' })
        };
    }

    getConnection(user_id) {
        const shardIndex = user_id % 3;
        return this.shards[shardIndex];
    }

    async insertNotification(notification) {
        const db = this.getConnection(notification.user_id);
        return await db.query(
            'INSERT INTO notifications (...) VALUES (...)',
            [notification.user_id, notification.channel, ...]
        );
    }

    async getUserNotifications(user_id) {
        const db = this.getConnection(user_id);
        return await db.query(
            'SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 20',
            [user_id]
        );
    }
}
```

---

## Replication Setup

### Master-Slave Replication

```
Shard 0:
  - Master (writes): shard0-master.db.example.com
  - Slave 1 (reads): shard0-slave1.db.example.com
  - Slave 2 (reads): shard0-slave2.db.example.com

Shard 1:
  - Master: shard1-master.db.example.com
  - Slave: shard1-slave.db.example.com

Shard 2:
  - Master: shard2-master.db.example.com
  - Slave: shard2-slave.db.example.com
```

**PostgreSQL Replication Config:**
```sql
-- On master: postgresql.conf
wal_level = replica
max_wal_senders = 10
wal_keep_size = 1GB

-- On slave: postgresql.conf
hot_standby = on
primary_conninfo = 'host=shard0-master.db.example.com port=5432 user=replicator'
```

**Application: Read-Write Splitting**
```javascript
async function sendNotification(notification) {
    const masterDB = getMasterConnection(notification.user_id);
    const slaveDB = getSlaveConnection(notification.user_id);

    // Write to master
    const notificationId = await masterDB.query(
        'INSERT INTO notifications (...) VALUES (...) RETURNING id',
        [...]
    );

    // Read from slave (user preferences)
    const preferences = await slaveDB.query(
        'SELECT * FROM user_preferences WHERE user_id = ?',
        [notification.user_id]
    );

    return { notificationId, preferences };
}
```

---

## Data Lifecycle

### Hot Data (Last 30 Days)
```
Storage: SSD
Access: Frequent (user checks notification history)
Queries: Fast (<10ms)
```

### Warm Data (30-90 Days)
```
Storage: HDD or separate table
Access: Occasional (analytics, user support)
Queries: Slower (50-100ms OK)
```

### Cold Data (>90 Days)
```
Storage: Archive database or S3 (Parquet files)
Access: Rare (compliance, audits)
Queries: Very slow (minutes OK)
```

**Data Archival Process:**
```sql
-- Monthly job: Archive old data

-- 1. Copy to archive table
INSERT INTO notifications_archive
SELECT * FROM notifications
WHERE created_at < NOW() - INTERVAL '90 days';

-- 2. Delete from main table
DELETE FROM notifications
WHERE created_at < NOW() - INTERVAL '90 days';

-- 3. Vacuum to reclaim space
VACUUM FULL notifications;
```

---

## Sample Queries

### Query 1: Get User's Recent Notifications
```sql
-- Fast query (uses idx_notifications_user_recent)
SELECT id, channel, subject, status, created_at
FROM notifications
WHERE user_id = 12345
  AND deleted_at IS NULL
ORDER BY created_at DESC
LIMIT 20;

-- Query plan: Index Scan on idx_notifications_user_recent (cost=0..50)
```

### Query 2: Find Pending Notifications (Worker)
```sql
-- Worker processes pending notifications
SELECT id, user_id, channel, template_id, variables
FROM notifications
WHERE status = 'pending'
ORDER BY created_at ASC
LIMIT 1000
FOR UPDATE SKIP LOCKED;  -- Prevents multiple workers from processing same notification

-- Query plan: Index Scan on idx_notifications_status
```

### Query 3: Analytics - Delivery Rate by Channel
```sql
SELECT
    channel,
    COUNT(*) as total_sent,
    COUNT(CASE WHEN status = 'delivered' THEN 1 END) as delivered,
    ROUND(
        COUNT(CASE WHEN status = 'delivered' THEN 1 END)::NUMERIC / COUNT(*) * 100,
        2
    ) as delivery_rate_pct
FROM notifications
WHERE created_at >= NOW() - INTERVAL '7 days'
GROUP BY channel;

-- Result:
-- channel | total_sent | delivered | delivery_rate_pct
-- email   | 1000000    | 980000    | 98.00
-- sms     | 200000     | 196000    | 98.00
-- push    | 500000     | 475000    | 95.00
```

### Query 4: Find Notifications for Campaign (Metadata Query)
```sql
-- Use GIN index on metadata JSONB column
SELECT id, user_id, channel, status
FROM notifications
WHERE metadata @> '{"campaign_id": "black-friday-2025"}'::jsonb
ORDER BY created_at DESC;

-- Query plan: Bitmap Index Scan on idx_notifications_metadata
```

---

## Backup Strategy

### Daily Backups
```bash
# Backup each shard
pg_dump -h shard0-master.db.example.com -U postgres notifications > backup_shard0_$(date +%Y%m%d).sql
pg_dump -h shard1-master.db.example.com -U postgres notifications > backup_shard1_$(date +%Y%m%d).sql
pg_dump -h shard2-master.db.example.com -U postgres notifications > backup_shard2_$(date +%Y%m%d).sql

# Upload to S3
aws s3 cp backup_shard0_$(date +%Y%m%d).sql s3://notifications-backups/
```

### Point-in-Time Recovery (WAL Archiving)
```sql
-- postgresql.conf
archive_mode = on
archive_command = 'cp %p /archive/location/%f'

-- Restore to specific timestamp
pg_basebackup + replay WAL logs up to 2025-01-15 10:30:00
```

---

**💡 Key Takeaways:**
1. **Shard by user_id** for even distribution and single-shard queries
2. **Partition by created_at** for efficient data archival
3. **Index strategically** based on query patterns
4. **Use JSONB** for flexible metadata
5. **Replicate** for high availability and read scaling
6. **Archive old data** to keep tables small and fast
