# Database Design - Schema & Data Model

## Database Choice: PostgreSQL with PostGIS

**Why PostgreSQL?**
- ACID compliance (critical for payments)
- PostGIS extension for geospatial queries (find nearby drivers)
- Rich indexing (B-tree, GiST, GIN)
- JSON support (flexible schema where needed)
- Mature ecosystem and tooling

---

## Sharding Strategy

**Shard Key: `city_id`**

Data partitioned by geographic region:
- US East Shard: New York, Boston, Washington DC
- US West Shard: San Francisco, Los Angeles, Seattle
- Europe Shard: London, Paris, Berlin
- Asia Shard: Tokyo, Singapore, Mumbai

---

## Database Schema

### 1. Users Table

```sql
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    city_id INT NOT NULL,  -- Shard key
    profile_photo_url TEXT,
    rating DECIMAL(3,2) DEFAULT 5.00,
    total_rides INT DEFAULT 0,
    role VARCHAR(20) CHECK (role IN ('rider', 'driver', 'both')),
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_city (city_id)
);

-- Example data
INSERT INTO users VALUES (
    12345,
    '+1-415-555-0123',
    'john@example.com',
    '$2b$10$hashed...',
    'John Doe',
    101,  -- San Francisco
    'https://cdn.uber.com/users/12345/profile.jpg',
    4.8,
    127,
    'rider',
    'active',
    '2023-05-10 10:00:00',
    '2025-01-15 10:00:00'
);
```

---

### 2. Drivers Table

```sql
CREATE TABLE drivers (
    driver_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id),
    license_number VARCHAR(50) UNIQUE NOT NULL,
    license_expiry DATE NOT NULL,
    vehicle_make VARCHAR(50),
    vehicle_model VARCHAR(50),
    vehicle_year INT,
    vehicle_color VARCHAR(30),
    license_plate VARCHAR(20),
    vehicle_photo_url TEXT,
    status VARCHAR(20) DEFAULT 'offline' CHECK (status IN ('available', 'on_ride', 'offline')),
    rating DECIMAL(3,2) DEFAULT 5.00,
    total_rides INT DEFAULT 0,
    total_earnings DECIMAL(10,2) DEFAULT 0.00,
    city_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    INDEX idx_status (status),
    INDEX idx_city (city_id),
    INDEX idx_rating (rating)
);

-- Example data
INSERT INTO drivers VALUES (
    456,
    45678,  -- References users.user_id
    'DL123456',
    '2027-12-31',
    'Toyota',
    'Camry',
    2022,
    'Silver',
    'ABC1234',
    'https://cdn.uber.com/drivers/456/vehicle.jpg',
    'available',
    4.9,
    1823,
    45250.75,
    101,  -- San Francisco
    '2021-03-15 09:00:00',
    '2025-01-15 10:00:00'
);
```

---

### 3. Driver Locations Table (Time-Series Data)

**Note:** Current locations stored in Redis for real-time queries. This table for historical analytics.

```sql
CREATE TABLE driver_locations (
    location_id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    location GEOGRAPHY(POINT, 4326) NOT NULL,  -- PostGIS geography type
    bearing INT,  -- Direction (0-360 degrees)
    speed INT,    -- km/h
    timestamp TIMESTAMP NOT NULL,

    INDEX idx_driver_timestamp (driver_id, timestamp DESC),
    INDEX idx_location USING GIST (location)
) PARTITION BY RANGE (timestamp);

-- Partition by month for efficient queries
CREATE TABLE driver_locations_2025_01 PARTITION OF driver_locations
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE TABLE driver_locations_2025_02 PARTITION OF driver_locations
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

-- Example data
INSERT INTO driver_locations (driver_id, location, bearing, speed, timestamp)
VALUES (
    456,
    ST_SetSRID(ST_MakePoint(-122.4194, 37.7749), 4326),  -- Longitude, Latitude
    45,
    30,
    '2025-01-15 10:30:00'
);

-- Query: Find drivers within 5 km of a point
SELECT driver_id,
       ST_Distance(location, ST_SetSRID(ST_MakePoint(-122.4194, 37.7749), 4326)) AS distance_meters
FROM driver_locations
WHERE ST_DWithin(
    location,
    ST_SetSRID(ST_MakePoint(-122.4194, 37.7749), 4326),
    5000  -- 5 km in meters
)
AND timestamp > NOW() - INTERVAL '1 minute'  -- Only recent locations
ORDER BY distance_meters
LIMIT 10;
```

---

### 4. Rides Table

```sql
CREATE TABLE rides (
    ride_id BIGSERIAL PRIMARY KEY,
    rider_id BIGINT NOT NULL REFERENCES users(user_id),
    driver_id BIGINT REFERENCES drivers(driver_id),

    -- Locations
    pickup_lat DECIMAL(10, 8) NOT NULL,
    pickup_lng DECIMAL(11, 8) NOT NULL,
    pickup_address TEXT,
    destination_lat DECIMAL(10, 8) NOT NULL,
    destination_lng DECIMAL(11, 8) NOT NULL,
    destination_address TEXT,

    -- Ride details
    ride_type VARCHAR(20) NOT NULL,  -- uberx, uberxl, uberblack
    status VARCHAR(20) NOT NULL CHECK (status IN (
        'searching', 'accepted', 'driver_arriving',
        'in_progress', 'completed', 'cancelled'
    )),

    -- Pricing
    fare_estimate DECIMAL(10,2),
    final_fare DECIMAL(10,2),
    surge_multiplier DECIMAL(3,2) DEFAULT 1.00,

    -- Metrics
    distance_km DECIMAL(8,2),
    duration_minutes INT,

    -- Timestamps
    requested_at TIMESTAMP NOT NULL DEFAULT NOW(),
    accepted_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,

    -- Route polyline (compressed)
    route_polyline TEXT,

    city_id INT NOT NULL,  -- Shard key

    INDEX idx_rider (rider_id, requested_at DESC),
    INDEX idx_driver (driver_id, requested_at DESC),
    INDEX idx_status (status),
    INDEX idx_city (city_id),
    INDEX idx_requested_at (requested_at DESC)
);

-- Example data
INSERT INTO rides VALUES (
    789,
    12345,  -- John Doe
    456,    -- Jane Smith (driver)
    37.7749, -122.4194, '123 Main St, San Francisco, CA',
    37.8044, -122.2712, '456 Oak Ave, Oakland, CA',
    'uberx',
    'completed',
    48.00,
    50.25,
    1.5,  -- 1.5x surge
    15.8,
    20,
    '2025-01-15 10:40:00',
    '2025-01-15 10:41:00',
    '2025-01-15 10:43:00',
    '2025-01-15 11:00:00',
    NULL,
    'encoded_polyline_string...',
    101
);
```

---

### 5. Payments Table

```sql
CREATE TABLE payments (
    payment_id VARCHAR(50) PRIMARY KEY,  -- Stripe payment ID
    ride_id BIGINT NOT NULL REFERENCES rides(ride_id),
    rider_id BIGINT NOT NULL REFERENCES users(user_id),

    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    payment_method_id VARCHAR(50) NOT NULL,

    status VARCHAR(20) NOT NULL CHECK (status IN (
        'pending', 'processing', 'succeeded', 'failed', 'refunded'
    )),

    -- Stripe details
    stripe_charge_id VARCHAR(50),
    stripe_receipt_url TEXT,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    succeeded_at TIMESTAMP,
    failed_at TIMESTAMP,
    refunded_at TIMESTAMP,

    -- Failure details
    failure_reason TEXT,

    INDEX idx_ride (ride_id),
    INDEX idx_rider (rider_id, created_at DESC),
    INDEX idx_status (status)
);

-- Example data
INSERT INTO payments VALUES (
    'pay_xyz789',
    789,
    12345,
    50.25,
    'USD',
    'pm_card_visa_4242',
    'succeeded',
    'ch_stripe123',
    'https://stripe.com/receipts/ch_stripe123',
    '2025-01-15 11:01:00',
    '2025-01-15 11:01:05',
    NULL,
    NULL,
    NULL
);
```

---

### 6. Ratings Table

```sql
CREATE TABLE ratings (
    rating_id BIGSERIAL PRIMARY KEY,
    ride_id BIGINT NOT NULL REFERENCES rides(ride_id),
    rater_id BIGINT NOT NULL REFERENCES users(user_id),
    rated_id BIGINT NOT NULL REFERENCES users(user_id),
    rater_type VARCHAR(10) NOT NULL CHECK (rater_type IN ('rider', 'driver')),

    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    feedback TEXT,
    tips DECIMAL(10,2) DEFAULT 0.00,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    INDEX idx_rated (rated_id, created_at DESC),
    INDEX idx_ride (ride_id),
    UNIQUE (ride_id, rater_id, rater_type)  -- Can only rate once
);

-- Example data
INSERT INTO ratings VALUES (
    1,
    789,
    12345,  -- John (rider) rates
    456,    -- Jane (driver)
    'rider',
    5,
    'Great driver, smooth ride!',
    5.00,
    '2025-01-15 11:05:00'
);
```

---

### 7. Payment Methods Table

```sql
CREATE TABLE payment_methods (
    payment_method_id VARCHAR(50) PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id),

    type VARCHAR(20) NOT NULL CHECK (type IN ('card', 'paypal', 'apple_pay', 'google_pay')),

    -- Card details (tokenized by Stripe)
    card_brand VARCHAR(20),
    card_last4 VARCHAR(4),
    card_exp_month INT,
    card_exp_year INT,

    is_default BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'active',

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    INDEX idx_user (user_id)
);
```

---

### 8. Surge Pricing Zones Table

```sql
CREATE TABLE surge_zones (
    zone_id VARCHAR(50) PRIMARY KEY,
    city_id INT NOT NULL,
    zone_name VARCHAR(100),
    zone_polygon GEOGRAPHY(POLYGON, 4326),  -- GeoJSON polygon

    INDEX idx_city (city_id),
    INDEX idx_polygon USING GIST (zone_polygon)
);

-- Current surge pricing (frequently updated, can also be in Redis)
CREATE TABLE surge_pricing (
    zone_id VARCHAR(50) REFERENCES surge_zones(zone_id),
    multiplier DECIMAL(3,2) NOT NULL,
    demand INT,  -- Number of ride requests
    supply INT,  -- Number of available drivers
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    PRIMARY KEY (zone_id, updated_at)
);
```

---

## Indexes Strategy

### Why Indexes Matter:
Without index: Full table scan (100ms for 1M rows)
With index: B-tree lookup (5ms)

### Critical Indexes:

```sql
-- User lookups by phone (login)
CREATE INDEX idx_users_phone ON users (phone);

-- Ride history queries
CREATE INDEX idx_rides_rider_date ON rides (rider_id, requested_at DESC);
CREATE INDEX idx_rides_driver_date ON rides (driver_id, requested_at DESC);

-- Find available drivers
CREATE INDEX idx_drivers_status_city ON drivers (status, city_id) WHERE status = 'available';

-- Geospatial queries (find nearby drivers)
CREATE INDEX idx_driver_locations_geo ON driver_locations USING GIST (location);

-- Payment queries
CREATE INDEX idx_payments_rider ON payments (rider_id, created_at DESC);
CREATE INDEX idx_payments_ride ON payments (ride_id);
```

---

## Partitioning Strategy

### Time-Series Partitioning (driver_locations, surge_pricing)

```sql
-- Partition by month for efficient old data archival
CREATE TABLE driver_locations (
    ...
) PARTITION BY RANGE (timestamp);

-- Auto-create partitions
CREATE TABLE driver_locations_2025_01 PARTITION OF driver_locations
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

-- Retention policy: Drop partitions older than 30 days
DROP TABLE driver_locations_2024_12;
```

**Benefits:**
- Query only relevant partition (faster)
- Easy to archive/delete old data
- Each partition can be on different storage tier

---

## Data Retention Policy

| Table | Retention Period | Storage Tier |
|-------|------------------|--------------|
| users | Forever | Hot (SSD) |
| drivers | Forever | Hot (SSD) |
| rides | Forever | Hot (< 1 year), Cold (> 1 year) |
| driver_locations | 30 days | Hot (7 days), Warm (30 days) |
| payments | Forever | Hot (SSD) |
| surge_pricing | 7 days | Hot |

**Cold Storage:**
- Move to AWS S3 Glacier
- 10x cheaper ($0.004/GB vs $0.10/GB)
- Query via AWS Athena (slower but acceptable for historical analysis)

---

## Data Consistency Requirements

### Strong Consistency (ACID Required):

**Payments:**
```sql
BEGIN TRANSACTION;
  -- Deduct from rider
  INSERT INTO payments (ride_id, rider_id, amount, status)
  VALUES (789, 12345, 50.25, 'processing');

  -- Credit to driver
  UPDATE drivers SET total_earnings = total_earnings + 40.20 WHERE driver_id = 456;

  -- Update ride status
  UPDATE rides SET status = 'completed', final_fare = 50.25 WHERE ride_id = 789;
COMMIT;
-- All or nothing (atomic)
```

**Ride Acceptance:**
```sql
BEGIN TRANSACTION;
  -- Ensure ride still available
  SELECT ride_id FROM rides WHERE ride_id = 789 AND status = 'searching' FOR UPDATE;

  -- Assign driver
  UPDATE rides SET driver_id = 456, status = 'accepted', accepted_at = NOW()
  WHERE ride_id = 789 AND status = 'searching';

  -- Mark driver as on_ride
  UPDATE drivers SET status = 'on_ride' WHERE driver_id = 456;
COMMIT;
```

### Eventual Consistency (Acceptable):

**Driver Locations:**
- Redis update (instant)
- Database update every 30 seconds (batched for efficiency)
- 30-second lag acceptable for analytics

**Ratings:**
- Driver rating recalculated asynchronously
- 1-minute delay acceptable

---

## Database Performance Tuning

### Connection Pooling

```javascript
// Node.js with pg-pool
const { Pool } = require('pg');

const pool = new Pool({
  host: 'postgres-master.uber.internal',
  port: 5432,
  database: 'uber_us_east',
  user: 'uber_app',
  password: process.env.DB_PASSWORD,
  max: 100,           // Max 100 connections
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 2000,
});

// Reuse connections (don't open/close per query)
const result = await pool.query('SELECT * FROM users WHERE user_id = $1', [12345]);
```

### Prepared Statements (Prevent SQL Injection + Performance)

```javascript
// Prepared statement (cached query plan)
const query = {
  name: 'get-user',
  text: 'SELECT * FROM users WHERE user_id = $1',
  values: [12345]
};

const result = await pool.query(query);
```

### Query Optimization

```sql
-- BAD: Full table scan
SELECT * FROM rides WHERE rider_id = 12345 ORDER BY requested_at DESC LIMIT 20;

-- GOOD: Uses idx_rides_rider_date index
EXPLAIN ANALYZE
SELECT * FROM rides WHERE rider_id = 12345 ORDER BY requested_at DESC LIMIT 20;

-- Output shows:
Index Scan using idx_rides_rider_date on rides  (cost=0.43..123.45 rows=20)
  Index Cond: (rider_id = 12345)
```

---

## Backup & Disaster Recovery

### Backup Strategy:

**1. Continuous Backup (Point-in-Time Recovery):**
```sql
-- PostgreSQL WAL (Write-Ahead Logging)
-- Enables recovery to any point in time (RPO: < 1 minute)

-- Example: Restore to 5 minutes ago
pg_restore --time="2025-01-15 10:55:00" uber_backup.dump
```

**2. Daily Snapshots:**
- Automated snapshots at 2 AM UTC
- Retained for 30 days
- Cross-region replication (US snapshot → EU, Asia)

**3. Disaster Recovery Test:**
- Monthly DR drill: Restore from backup in < 5 minutes
- Verify data integrity

---

## Database Monitoring

**Key Metrics:**

```sql
-- Active connections
SELECT count(*) FROM pg_stat_activity WHERE state = 'active';

-- Slow queries (> 1 second)
SELECT pid, now() - query_start AS duration, query
FROM pg_stat_activity
WHERE state = 'active' AND now() - query_start > INTERVAL '1 second';

-- Cache hit rate (should be > 99%)
SELECT
  sum(heap_blks_hit) / (sum(heap_blks_hit) + sum(heap_blks_read)) AS cache_hit_ratio
FROM pg_statio_user_tables;

-- Index usage
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

**Alerts:**
- CPU > 80% → Add read replica
- Connections > 450 (of 500 max) → Scale up or add connection pooler
- Replication lag > 1 second → Investigate network/disk
- Cache hit rate < 95% → Increase shared_buffers

---

## Summary: Database Design Highlights

**✅ What We Achieved:**
- Geographic sharding for low latency
- PostGIS for efficient geospatial queries (find nearby drivers in 5ms)
- Proper indexing (fast lookups)
- Partitioning for time-series data (easy archival)
- ACID transactions for payments (data integrity)
- Read replicas for scaling reads
- Comprehensive backup strategy (RPO < 1 minute, RTO < 5 minutes)

**Key Trade-offs:**
- Strong consistency for payments vs eventual consistency for locations
- Normalized schema vs denormalized for performance
- SSD (fast, expensive) vs HDD/cold storage (slow, cheap)
