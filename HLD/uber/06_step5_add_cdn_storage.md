# Step 5: CDN & Object Storage - Optimizing Static Content Delivery

## What We're Solving

After Step 4, our backend is fast and event-driven, but we're wasting bandwidth and money on static content:

**Problems:**
1. ❌ **Map Tiles**: 20M users × 50 requests/day = 1B map tile requests (2.67 PB/month bandwidth!)
2. ❌ **Static Assets**: CSS, JS, images served from app servers (inefficient)
3. ❌ **Driver Photos**: Stored in database as BLOBs (slow, expensive, doesn't scale)
4. ❌ **Global Latency**: User in Tokyo loads maps from US server (200ms)
5. ❌ **High Bandwidth Cost**: $0.09/GB = $240K/month just for maps!

**Solutions in This Step:**
1. ✅ **CDN (Content Delivery Network)**: Cache static content at edge locations worldwide
2. ✅ **Object Storage (S3)**: Store driver photos, receipts, route polylines
3. ✅ **Reduce Bandwidth**: 70% cost savings ($240K → $72K/month)
4. ✅ **Improve Latency**: Maps load from nearby CDN edge (10ms vs 200ms)

---

## What Changed from Step 4

### Before (Step 4): All Content from Origin Servers
```
User requests map tile
  ↓
Request goes to Load Balancer (US)
  ↓
App Server fetches from Google Maps API
  ↓
Sends 50 KB tile to user in Tokyo (200ms latency)
  ↓
Bandwidth cost: $0.09/GB
```

### After (Step 5): CDN + Object Storage
```
User requests map tile
  ↓
CDN edge server in Tokyo checks cache
  ├─ Cache Hit (95%) → Return instantly (10ms) ✅
  └─ Cache Miss (5%) → Fetch from origin → Cache for next time

Bandwidth saved: 95%
Latency: 20x faster
Cost: 70% reduction
```

---

## Architecture Diagram

```mermaid
graph TB
    subgraph "Users Worldwide"
        US_USER[User in<br/>New York]
        EU_USER[User in<br/>London]
        ASIA_USER[User in<br/>Tokyo]
    end

    subgraph "CDN Layer - NEW! (Edge Locations)"
        CDN_US[CloudFlare Edge<br/>New York<br/><br/>Cached:<br/>• Map tiles<br/>• Static assets<br/>• JS/CSS<br/>TTL: 24 hours]

        CDN_EU[CloudFlare Edge<br/>London<br/><br/>Cached:<br/>• Map tiles<br/>• Static assets<br/>• JS/CSS<br/>TTL: 24 hours]

        CDN_ASIA[CloudFlare Edge<br/>Tokyo<br/><br/>Cached:<br/>• Map tiles<br/>• Static assets<br/>• JS/CSS<br/>TTL: 24 hours]
    end

    subgraph "Object Storage - NEW!"
        S3_US[AWS S3<br/>US East<br/><br/>Stores:<br/>• Driver photos<br/>• Vehicle documents<br/>• Ride receipts<br/>• Route polylines]

        S3_EU[AWS S3<br/>Europe<br/><br/>Stores:<br/>• Driver photos<br/>• Vehicle documents<br/>• Ride receipts<br/>• Route polylines]

        S3_ASIA[AWS S3<br/>Asia Pacific<br/><br/>Stores:<br/>• Driver photos<br/>• Vehicle documents<br/>• Ride receipts<br/>• Route polylines]
    end

    subgraph "Application Layer"
        LB[Load Balancer]
        APP[App Servers]
    end

    subgraph "Cache & Database"
        REDIS[(Redis Cache)]
        DB[(PostgreSQL)]
    end

    subgraph "External Services"
        GMAPS[Google Maps API<br/>Tiles, Geocoding]
    end

    US_USER --> CDN_US
    EU_USER --> CDN_EU
    ASIA_USER --> CDN_ASIA

    CDN_US -.Cache Miss.-> LB
    CDN_EU -.Cache Miss.-> LB
    CDN_ASIA -.Cache Miss.-> LB

    LB --> APP

    APP --> REDIS
    APP --> DB
    APP --> GMAPS

    APP --> S3_US
    APP --> S3_EU
    APP --> S3_ASIA

    CDN_US -.Fetch Static Assets.-> S3_US
    CDN_EU -.Fetch Static Assets.-> S3_EU
    CDN_ASIA -.Fetch Static Assets.-> S3_ASIA

    style CDN_US fill:#4fc3f7,stroke:#0288d1,stroke-width:3px
    style CDN_EU fill:#4fc3f7,stroke:#0288d1,stroke-width:3px
    style CDN_ASIA fill:#4fc3f7,stroke:#0288d1,stroke-width:3px
    style S3_US fill:#81c784,stroke:#388e3c,stroke-width:3px
    style S3_EU fill:#81c784,stroke:#388e3c,stroke-width:3px
    style S3_ASIA fill:#81c784,stroke:#388e3c,stroke-width:3px
```

---

## What is a CDN? (Simple Explanation)

### Analogy: Warehouse vs Local Stores

**Without CDN:**
```
All customers order from central warehouse (origin server)
  ↓
Customer in Tokyo orders → Ships from US warehouse
  ↓
Long delivery time (high latency) ❌
Expensive shipping cost ❌
```

**With CDN:**
```
Local stores cache popular items (edge servers)
  ↓
Customer in Tokyo orders → Local Tokyo store has it in stock
  ↓
Instant pickup (low latency) ✅
No shipping cost ✅
```

### Technical Definition

**CDN (Content Delivery Network):**
- Global network of edge servers (200+ locations worldwide)
- Caches static content close to users
- Reduces latency by 10-20x
- Reduces origin server load by 70-95%
- DDoS protection and SSL termination

**How It Works:**
```
1. User requests: https://cdn.uber.com/map-tile/12/34/56.png
2. DNS routes to nearest CDN edge (Tokyo)
3. Tokyo edge checks cache:
   - Hit: Return cached tile (10ms)
   - Miss: Fetch from origin → Cache → Return (200ms first time, 10ms after)
4. Cache stored for TTL (24 hours)
```

---

## What We Cache in CDN

### 1. Map Tiles (Biggest Bandwidth Saver!)

**Map Tile Basics:**
Maps are composed of thousands of small image tiles (256×256 pixels).

**Tile URL Format:**
```
https://cdn.uber.com/tiles/{zoom}/{x}/{y}.png

Example:
https://cdn.uber.com/tiles/12/1234/5678.png
  └─ Zoom level 12 (city level)
  └─ Tile coordinates (1234, 5678)
```

**Traffic Volume:**
```
20 million DAU
Average 10 map pans/drags per session
Each pan loads 20 tiles
Total: 20M × 10 × 20 = 4 billion tiles/day

Tile size: 10 KB average
Daily bandwidth: 4B × 10 KB = 40 TB/day

Without CDN: $0.09/GB × 40,000 GB = $3,600/day = $108K/month
With CDN (95% hit rate): $108K × 0.05 = $5.4K/month
Savings: $102.6K/month!
```

**CDN Configuration:**
```javascript
// CloudFlare CDN cache rules
{
  "rules": [
    {
      "pattern": "/tiles/*",
      "cache_level": "aggressive",
      "cache_ttl": 86400,  // 24 hours
      "cache_key": {
        "query_string": { "ignore": true }
      }
    }
  ]
}
```

### 2. Static Assets (App Performance)

**What to Cache:**
- **JavaScript bundles**: app.js (500 KB)
- **CSS stylesheets**: styles.css (100 KB)
- **Images**: logo.png, icons.png
- **Fonts**: Uber-Font.woff2 (50 KB)

**Cache Headers:**
```javascript
// Express.js: Set cache headers for static assets
app.use('/static', express.static('public', {
  maxAge: '1y',          // Cache for 1 year
  immutable: true,       // File never changes (use versioning)
  setHeaders: (res, path) => {
    if (path.endsWith('.html')) {
      res.setHeader('Cache-Control', 'no-cache');  // HTML not cached
    } else {
      res.setHeader('Cache-Control', 'public, max-age=31536000, immutable');
    }
  }
}));

// URLs with versioning (cache busting)
<link href="/static/css/app.v1234.css" rel="stylesheet">
<script src="/static/js/app.v1234.js"></script>
```

### 3. Driver Profile Photos

**Problem:**
Storing photos in database as BLOBs:
- Increases database size (1 MB photo × 10M drivers = 10 TB)
- Slows down queries (BLOBs are inefficient)
- Expensive database storage ($0.50/GB vs $0.023/GB for S3)

**Solution:**
Store in S3, serve via CDN.

**Upload Flow:**
```javascript
// Driver uploads profile photo
async function uploadDriverPhoto(driverId, photoFile) {
  // 1. Generate unique filename
  const filename = `drivers/${driverId}/profile-${Date.now()}.jpg`;

  // 2. Upload to S3
  await s3.putObject({
    Bucket: 'uber-driver-photos',
    Key: filename,
    Body: photoFile,
    ContentType: 'image/jpeg',
    ACL: 'public-read',           // Publicly accessible
    CacheControl: 'max-age=31536000'  // Cache for 1 year
  });

  // 3. Generate CDN URL
  const photoUrl = `https://cdn.uber.com/drivers/${driverId}/profile-${Date.now()}.jpg`;

  // 4. Store URL in database (not the image!)
  await db.query(
    'UPDATE drivers SET profile_photo_url = ? WHERE id = ?',
    [photoUrl, driverId]
  );

  return { photoUrl };
}

// Retrieve photo (served from CDN)
const driver = await db.query('SELECT profile_photo_url FROM drivers WHERE id = ?', [driverId]);
// Returns: https://cdn.uber.com/drivers/123/profile-1642345678.jpg
// User's browser fetches from nearest CDN edge
```

---

## Object Storage (AWS S3)

### What is S3? (Simple Explanation)

**Analogy:**
Think of S3 as an infinite filing cabinet where you can store any file.

**Technical Definition:**
- **S3 (Simple Storage Service)**: Scalable object storage
- **Durability**: 99.999999999% (11 nines) - virtually never lose data
- **Availability**: 99.99% uptime
- **Scalability**: Store unlimited files
- **Cost**: $0.023/GB/month (10x cheaper than database storage)

### S3 Bucket Structure for Uber

```
uber-production-bucket/
├── drivers/
│   ├── {driver_id}/
│   │   ├── profile-photo.jpg
│   │   ├── license-front.jpg
│   │   ├── license-back.jpg
│   │   └── vehicle-photo.jpg
├── receipts/
│   ├── {year}/{month}/
│   │   └── {ride_id}-receipt.pdf
├── routes/
│   ├── {ride_id}/
│   │   └── route-polyline.json
└── analytics/
    └── {date}/
        └── daily-report.csv
```

### S3 Configuration

```javascript
const AWS = require('aws-sdk');

// Configure S3 client
const s3 = new AWS.S3({
  region: 'us-east-1',
  signatureVersion: 'v4'
});

// Upload file with metadata
async function uploadToS3(file, key, metadata = {}) {
  const params = {
    Bucket: 'uber-production-bucket',
    Key: key,
    Body: file,
    ContentType: metadata.contentType || 'application/octet-stream',
    CacheControl: metadata.cacheControl || 'max-age=86400',
    Metadata: metadata.custom || {},
    ServerSideEncryption: 'AES256',  // Encrypt at rest
    StorageClass: metadata.storageClass || 'STANDARD'
  };

  const result = await s3.upload(params).promise();
  return result.Location;  // URL of uploaded file
}

// Generate pre-signed URL (temporary access)
function generatePresignedUrl(key, expiresIn = 3600) {
  return s3.getSignedUrl('getObject', {
    Bucket: 'uber-production-bucket',
    Key: key,
    Expires: expiresIn  // URL valid for 1 hour
  });
}
```

### S3 Storage Classes (Cost Optimization)

```
Driver photos (frequent access):
  └─ S3 Standard: $0.023/GB/month ✅

Ride receipts (occasional access):
  └─ S3 Infrequent Access: $0.0125/GB/month ✅

Old driver documents (rare access):
  └─ S3 Glacier: $0.004/GB/month ✅

Example cost for 100 TB:
  - S3 Standard: 100,000 GB × $0.023 = $2,300/month
  - S3 IA: 100,000 GB × $0.0125 = $1,250/month
  - S3 Glacier: 100,000 GB × $0.004 = $400/month
```

### S3 Lifecycle Policies (Automatic Cost Optimization)

```javascript
// Lifecycle policy: Auto-move old files to cheaper storage
{
  "Rules": [
    {
      "Id": "Archive old receipts",
      "Status": "Enabled",
      "Filter": {
        "Prefix": "receipts/"
      },
      "Transitions": [
        {
          "Days": 90,
          "StorageClass": "STANDARD_IA"  // Move to Infrequent Access after 90 days
        },
        {
          "Days": 365,
          "StorageClass": "GLACIER"  // Move to Glacier after 1 year
        }
      ]
    },
    {
      "Id": "Delete old analytics reports",
      "Status": "Enabled",
      "Filter": {
        "Prefix": "analytics/"
      },
      "Expiration": {
        "Days": 730  // Delete after 2 years
      }
    }
  ]
}
```

---

## CDN Providers Comparison

### CloudFlare vs AWS CloudFront vs Fastly

| Feature | CloudFlare | AWS CloudFront | Fastly |
|---------|------------|----------------|--------|
| **Edge Locations** | 200+ (better coverage) | 225+ (best coverage) | 70+ (limited) |
| **DDoS Protection** | ✅ Excellent (free) | ✅ Good (paid) | ✅ Basic |
| **Real-time Purge** | ✅ Instant | ❌ 5-10 min | ✅ Instant |
| **Cost** | $20-200/month (flat) | Pay-per-use ($0.085/GB) | $0.12/GB (expensive) |
| **SSL** | ✅ Free | ✅ Free | ✅ Free |
| **Image Optimization** | ✅ Yes | ❌ No (need Lambda@Edge) | ✅ Yes |
| **WebSocket Support** | ✅ Yes | ✅ Yes | ✅ Yes |

### Why CloudFlare for Uber?

**✅ CloudFlare Wins:**
1. **Cost**: Flat pricing ($200/month) vs pay-per-GB (saves $$$)
2. **DDoS Protection**: Industry-leading (important for public apps)
3. **Instant Cache Purge**: Update maps instantly (not 10 min delay)
4. **Image Optimization**: Resize/compress on-the-fly

**When to Choose AWS CloudFront:**
- Already all-in on AWS ecosystem
- Need deep integration with S3, Lambda
- Need more edge locations (225 vs 200)

---

## Image Optimization (CloudFlare)

### Automatic Image Optimization

**Problem:**
Driver uploads 5 MB photo → Users on slow networks wait forever.

**Solution:**
CloudFlare automatically resizes/compresses images.

**Usage:**
```html
<!-- Original URL (5 MB, 4000x3000 px) -->
<img src="https://cdn.uber.com/drivers/123/profile.jpg">

<!-- Optimized URL (50 KB, 300x300 px) -->
<img src="https://cdn.uber.com/cdn-cgi/image/width=300,quality=85/drivers/123/profile.jpg">

<!-- Responsive images (serve different sizes based on device) -->
<img src="https://cdn.uber.com/drivers/123/profile.jpg"
     srcset="
       https://cdn.uber.com/cdn-cgi/image/width=150/drivers/123/profile.jpg 1x,
       https://cdn.uber.com/cdn-cgi/image/width=300/drivers/123/profile.jpg 2x
     ">
```

**Savings:**
```
Original: 5 MB image × 10M drivers = 50 TB bandwidth/day
Optimized: 50 KB image × 10M drivers = 500 GB bandwidth/day
Bandwidth saved: 99% (50 TB → 500 GB)
```

---

## Performance Improvements (Step 4 → Step 5)

### Latency Comparison (Global)

| User Location | Resource | Before (No CDN) | After (CDN) | Improvement |
|---------------|----------|-----------------|-------------|-------------|
| New York | Map tile | 50ms (US origin) | 10ms (local edge) | 5x faster |
| London | Map tile | 200ms (US origin) | 12ms (local edge) | 17x faster |
| Tokyo | Map tile | 250ms (US origin) | 15ms (local edge) | 17x faster |
| All | Static assets (JS/CSS) | 100-250ms | 10-20ms | 10x faster |

### Cost Comparison (Monthly)

| Resource | Before (No CDN) | After (CDN) | Savings |
|----------|-----------------|-------------|---------|
| Map tiles | $108,000 | $5,400 | $102,600 (95%) |
| Static assets | $15,000 | $500 | $14,500 (97%) |
| Driver photos | $5,000 (DB storage) | $2,300 (S3) + $200 (CDN) = $2,500 | $2,500 (50%) |
| **Total** | **$128,000** | **$8,400** | **$119,600 (93%)** |

---

## Cache Invalidation Strategies

### Problem: Stale Content

**Scenario:**
```
Uber updates logo from old-logo.png to new-logo.png
↓
Users still see old logo (cached for 24 hours)
↓
Bad UX! ❌
```

### Solution 1: Cache Busting (Versioning)

```html
<!-- Old version (cached) -->
<img src="/static/logo.png">

<!-- New version (new URL → CDN fetches fresh) -->
<img src="/static/logo.v2.png">

<!-- Or use query string -->
<img src="/static/logo.png?v=2">

<!-- Or use hash (best practice) -->
<img src="/static/logo.a1b2c3d4.png">
```

**How It Works:**
- URL changes → CDN sees new URL → Fetches from origin
- Old cached version stays in CDN (no purge needed)
- No propagation delay (instant update)

### Solution 2: Manual Cache Purge

```javascript
// Purge CDN cache via API (CloudFlare example)
const cloudflare = require('cloudflare')({
  email: 'admin@uber.com',
  key: 'YOUR_API_KEY'
});

// Purge specific files
await cloudflare.zones.purgeCache('ZONE_ID', {
  files: [
    'https://cdn.uber.com/static/logo.png',
    'https://cdn.uber.com/tiles/12/1234/5678.png'
  ]
});

// Purge everything (use sparingly!)
await cloudflare.zones.purgeCache('ZONE_ID', { purge_everything: true });
```

**When to Use:**
- Emergency fixes (bug in JavaScript file)
- Map tile updates (new roads, buildings)
- Policy changes (terms of service PDF)

---

## Security Considerations

### 1. Prevent Hotlinking (Others Using Your Bandwidth)

**Problem:**
Other websites embed your images → You pay bandwidth cost!

**Solution: Referrer Check**
```javascript
// CloudFlare Page Rule: Block if referrer is not uber.com
{
  "pattern": "*.cdn.uber.com/*",
  "rules": {
    "referrer_check": {
      "allowed_domains": ["uber.com", "*.uber.com"],
      "block_external": true
    }
  }
}
```

### 2. Signed URLs (Private Content)

**Use Case:**
Driver's license photo should only be visible to Uber staff, not public.

**Solution:**
```javascript
// Generate signed URL (expires in 1 hour)
const signedUrl = s3.getSignedUrl('getObject', {
  Bucket: 'uber-private-docs',
  Key: `drivers/${driverId}/license.jpg`,
  Expires: 3600  // 1 hour
});

// URL looks like:
// https://s3.amazonaws.com/uber-private-docs/drivers/123/license.jpg?
// AWSAccessKeyId=AKIAIOSFODNN7EXAMPLE&
// Expires=1642349278&
// Signature=abc123xyz...

// After 1 hour, URL is invalid (403 Forbidden)
```

### 3. DDoS Protection (CDN Benefit)

**CloudFlare DDoS Protection:**
- Absorbs attacks at edge (not your origin servers)
- Rate limiting (max 100 requests/sec per IP)
- Challenge page for suspicious traffic (CAPTCHA)

**Example Attack:**
```
Attacker sends 1 million requests/sec
  ↓
CloudFlare edge servers absorb traffic
  ↓
Only legitimate ~1000 requests/sec reach origin
  ↓
Your servers: Unaffected ✅
```

---

## Monitoring & Metrics

### CDN Metrics to Track

```
1. Cache Hit Rate:
   - Target: > 90%
   - Formula: (Cache Hits / Total Requests) × 100
   - If < 80%: Investigate (short TTL? dynamic content?)

2. Bandwidth Savings:
   - Origin bandwidth vs CDN bandwidth
   - Target: 70-95% reduction

3. Latency (P50, P95, P99):
   - P50: 10-20ms (median user)
   - P95: 50ms (95th percentile)
   - P99: 100ms (99th percentile)

4. Error Rate:
   - Target: < 0.1%
   - 4xx errors: Client issues (404 not found)
   - 5xx errors: Origin server issues

5. Origin Load:
   - Requests reaching origin servers
   - Target: < 10% of total traffic
```

### CloudFlare Analytics Dashboard

```javascript
// Fetch CloudFlare analytics via API
const analytics = await cloudflare.zones.analytics('ZONE_ID', {
  since: '-24h',
  until: 'now'
});

console.log(analytics);
// Output:
{
  requests: {
    all: 10000000,
    cached: 9500000,
    uncached: 500000
  },
  bandwidth: {
    all: 500 GB,
    cached: 475 GB,
    uncached: 25 GB
  },
  cache_hit_rate: 95%,  ✅ Excellent!
  latency: {
    p50: 12ms,
    p95: 45ms,
    p99: 89ms
  }
}
```

---

## Multi-Region S3 Replication

### Why Replicate?

**Use Case:**
Driver in Tokyo uploads photo → Stored in US S3 → User in Tokyo views photo → Fetches from US (slow!)

**Solution: Cross-Region Replication**

```mermaid
graph LR
    DRIVER[Driver in Tokyo<br/>Uploads Photo] --> S3_ASIA[S3 Asia Pacific<br/>Primary Storage]

    S3_ASIA -.Replication.-> S3_US[S3 US East<br/>Replica]
    S3_ASIA -.Replication.-> S3_EU[S3 Europe<br/>Replica]

    S3_ASIA --> CDN_ASIA[CDN Edge Tokyo<br/>Serves photo]
    S3_US --> CDN_US[CDN Edge New York<br/>Serves photo]
    S3_EU --> CDN_EU[CDN Edge London<br/>Serves photo]

    US_USER[User in New York] --> CDN_US
    EU_USER[User in London] --> CDN_EU
```

### S3 Replication Configuration

```javascript
// Enable cross-region replication
{
  "ReplicationConfiguration": {
    "Role": "arn:aws:iam::123456789:role/s3-replication",
    "Rules": [
      {
        "Status": "Enabled",
        "Priority": 1,
        "Filter": {
          "Prefix": "drivers/"  // Only replicate driver photos
        },
        "Destination": {
          "Bucket": "arn:aws:s3:::uber-us-bucket",
          "ReplicationTime": {
            "Status": "Enabled",
            "Time": {
              "Minutes": 15  // Replicate within 15 minutes
            }
          }
        }
      }
    ]
  }
}
```

**Benefits:**
- Low latency (data stored close to users)
- Disaster recovery (if one region goes down, use replica)
- Compliance (EU data stored in EU region - GDPR)

---

## Code Example: Complete Upload Flow

```javascript
// ========== Upload Driver Profile Photo ==========

const multer = require('multer');
const sharp = require('sharp');  // Image processing
const { v4: uuidv4 } = require('uuid');

// Configure file upload
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 10 * 1024 * 1024 },  // Max 10 MB
  fileFilter: (req, file, cb) => {
    if (!file.mimetype.startsWith('image/')) {
      return cb(new Error('Only image files allowed'));
    }
    cb(null, true);
  }
});

// API endpoint: Upload driver photo
app.post('/api/drivers/:driverId/photo', upload.single('photo'), async (req, res) => {
  try {
    const { driverId } = req.params;
    const photoFile = req.file;

    // 1. Validate driver exists
    const driver = await db.query('SELECT id FROM drivers WHERE id = ?', [driverId]);
    if (!driver) {
      return res.status(404).json({ error: 'Driver not found' });
    }

    // 2. Process image (resize, compress)
    const processedImage = await sharp(photoFile.buffer)
      .resize(800, 800, { fit: 'cover' })   // Resize to 800x800
      .jpeg({ quality: 85 })                // Compress to 85% quality
      .toBuffer();

    // 3. Generate unique filename
    const filename = `drivers/${driverId}/profile-${uuidv4()}.jpg`;

    // 4. Upload to S3
    const uploadResult = await s3.upload({
      Bucket: 'uber-driver-photos',
      Key: filename,
      Body: processedImage,
      ContentType: 'image/jpeg',
      ACL: 'public-read',
      CacheControl: 'public, max-age=31536000',  // Cache 1 year
      Metadata: {
        'driver-id': driverId.toString(),
        'upload-timestamp': Date.now().toString()
      }
    }).promise();

    // 5. Generate CDN URL
    const photoUrl = `https://cdn.uber.com/${filename}`;

    // 6. Update database
    await db.query(
      'UPDATE drivers SET profile_photo_url = ?, updated_at = NOW() WHERE id = ?',
      [photoUrl, driverId]
    );

    // 7. Invalidate old cached photo (if exists)
    const oldPhoto = driver.profile_photo_url;
    if (oldPhoto) {
      await cloudflare.zones.purgeCache(ZONE_ID, {
        files: [oldPhoto]
      });
    }

    // 8. Return success
    res.json({
      success: true,
      photo_url: photoUrl,
      size: processedImage.length
    });

    console.log(`✅ Driver ${driverId} photo uploaded: ${photoUrl}`);

  } catch (error) {
    console.error('Photo upload failed:', error);
    res.status(500).json({ error: 'Upload failed' });
  }
});

// ========== Get Driver Photo ==========

app.get('/api/drivers/:driverId/photo', async (req, res) => {
  const { driverId } = req.params;

  // Query database for photo URL
  const driver = await db.query(
    'SELECT profile_photo_url FROM drivers WHERE id = ?',
    [driverId]
  );

  if (!driver || !driver.profile_photo_url) {
    return res.status(404).json({ error: 'Photo not found' });
  }

  // Return CDN URL (browser fetches from CDN, not our server)
  res.json({
    photo_url: driver.profile_photo_url
    // Example: https://cdn.uber.com/drivers/123/profile-abc123.jpg
  });
});
```

---

## What's Next?

Our static content is now optimized and globally distributed, but our monolithic app server needs to be broken down:

**Remaining Issues:**
1. ❌ **Monolithic Services**: All logic in one app (ride, payment, notification)
2. ❌ **No Service Isolation**: Payment service crash brings down entire system
3. ❌ **Hard to Scale**: Can't scale payment service independently from ride service

**In Step 6 (Final Architecture), we'll:**
- Break monolith into microservices (Ride, Driver, Payment, Notification)
- Add service mesh for inter-service communication
- Implement API gateway for routing
- Show complete final architecture

---

## Key Takeaways for Beginners

1. **CDN = Performance + Cost Savings**: 10-20x faster, 70-95% bandwidth savings
2. **Cache Static Content**: Map tiles, images, CSS/JS → CDN
3. **Store Files in S3, Not Database**: 10x cheaper, scales infinitely
4. **Versioning for Cache Busting**: Change URL when file changes (no manual purge)
5. **Geographic Distribution**: Store data close to users (multi-region S3)
6. **Image Optimization**: Resize/compress automatically (save 99% bandwidth)

**Interview Tip:**
When asked "How do you optimize static content delivery?", immediately mention CDN and object storage. Explain cache hit rate, TTL, and the trade-off between freshness and performance. Bonus points for mentioning image optimization and multi-region replication!
