# YouTube System Design - Requirements & Capacity Planning

## Table of Contents
1. [Functional Requirements](#functional-requirements)
2. [Non-Functional Requirements](#non-functional-requirements)
3. [Traffic Estimates](#traffic-estimates)
4. [Storage Estimates](#storage-estimates)
5. [Bandwidth Estimates](#bandwidth-estimates)
6. [Key Assumptions](#key-assumptions)

---

## Functional Requirements

### Core Features (Must Have - Priority 1)

#### 1. Video Upload
- Users can upload video files (various formats: MP4, AVI, MOV, etc.)
- Support for videos up to 12 hours in length
- Support for file sizes up to 256 GB
- Upload progress tracking
- Resume capability for interrupted uploads

#### 2. Video Transcoding
- Automatically convert uploaded videos to multiple resolutions:
  - **240p** (426 × 240) - Low bandwidth
  - **360p** (640 × 360) - Medium quality
  - **480p** (854 × 480) - Standard definition
  - **720p** (1280 × 720) - HD
  - **1080p** (1920 × 1080) - Full HD
  - **1440p** (2560 × 1440) - 2K
  - **2160p** (3840 × 2160) - 4K
- Support multiple formats (MP4, WebM, HLS)
- Generate thumbnails automatically

#### 3. Video Streaming
- Real-time video playback with minimal buffering
- **Adaptive Bitrate Streaming** (ABR): automatically adjust quality based on:
  - Network speed
  - Device capabilities
  - User preferences
- Support for seeking (jump to any timestamp)
- Playback speed control (0.25x to 2x)

#### 4. Video Search
- Search by title, description, tags, channel name
- Autocomplete suggestions
- Filter by:
  - Upload date (last hour, today, this week, this month, this year)
  - Duration (short <4min, medium 4-20min, long >20min)
  - Quality (4K, HD, subtitles)
  - Features (live, 360°, VR180, HDR)

#### 5. User Interactions
- **Like/Dislike** videos (with count display)
- **Comment** on videos (text-based)
- **Reply** to comments (nested comments)
- **Share** videos (generate shareable links)

#### 6. Channel Management
- Users can create channels
- Subscribe to other channels
- Notification preferences (all, personalized, none)
- Channel analytics (views, watch time, subscribers)

### Additional Features (Nice to Have - Priority 2)

- Live streaming
- Playlist creation
- Video recommendations (personalized)
- Watch history
- Watch later queue
- Community posts
- Video monetization (ads)

---

## Non-Functional Requirements

### 1. Scalability 🚀
- **Users:** Support 2.5 billion monthly active users (MAU)
- **Daily Active Users (DAU):** ~500 million users per day
- **Concurrent Users:** Handle 100 million concurrent streaming users during peak hours
- **Video Uploads:** Support 500+ hours of video uploaded every minute
- **Growth:** System should scale linearly with user growth

**Why it matters:** YouTube is the 2nd most visited website globally. The system must handle massive scale without performance degradation.

### 2. High Availability 🟢
- **Uptime Target:** 99.99% availability (52 minutes downtime per year)
- **Zero downtime deployments** for critical services
- **Graceful degradation:** If recommendations fail, video playback continues
- **Multi-region deployment** for disaster recovery

**Why it matters:** Downtime directly impacts user experience and revenue (ads, subscriptions).

### 3. Low Latency ⚡
- **Video Start Time:** < 1 second for video playback to begin
- **Buffering:** Minimal buffering after initial load (< 1% rebuffering ratio)
- **Search Results:** Return results within 200ms
- **API Response Time:** < 100ms for 95th percentile

**Why it matters:** Users expect instant gratification. High latency leads to user churn.

### 4. Durability 💾
- **Video Storage:** 99.999999999% (11 nines) durability
- **No data loss** even during hardware failures
- **Multi-region replication** for all uploaded videos
- **Regular backups** of metadata and user data

**Why it matters:** Creators rely on YouTube to preserve their content permanently.

### 5. Cost Efficiency 💰
- **Storage:** Optimize for cold storage (old, rarely watched videos)
- **Bandwidth:** Use CDN to reduce origin server load
- **Compute:** Use spot instances for non-critical batch jobs (transcoding)
- **Target:** Keep cost per user < $10/year

**Why it matters:** At YouTube's scale, a 10% cost optimization saves hundreds of millions of dollars annually.

### 6. Consistency 🔄
- **Video Metadata:** Eventual consistency is acceptable (views count can be slightly delayed)
- **User Auth:** Strong consistency required
- **Comments:** Eventual consistency acceptable (delay up to 5 seconds)
- **Subscriptions:** Strong consistency required

---

## Traffic Estimates

### Assumptions
Let's make realistic assumptions based on public data:

- **Monthly Active Users (MAU):** 2.5 billion
- **Daily Active Users (DAU):** 500 million (20% of MAU)
- **Average videos watched per user per day:** 5 videos
- **Average video duration:** 10 minutes
- **Upload to View Ratio:** 1:1000 (for every 1 upload, there are 1000 views)

---

### Daily Traffic

#### Video Views
```
DAU = 500 million users
Videos watched per user per day = 5 videos
Total video views per day = 500M × 5 = 2.5 billion views/day
```

#### Requests Per Second (Video Playback)
```
Total views per day = 2.5 billion
Seconds in a day = 86,400
Average requests/sec = 2.5B / 86,400 ≈ 28,935 requests/sec

Peak traffic (assume 3x average) = 28,935 × 3 ≈ 87,000 requests/sec
```

**Translation for Beginners:** At peak times (evenings, weekends), YouTube needs to handle ~87,000 video playback requests every second!

---

#### Video Uploads
```
Total video hours uploaded per minute = 500 hours (public data)
Total video hours uploaded per day = 500 × 60 × 24 = 720,000 hours/day

Average video duration = 10 minutes = 0.167 hours
Number of videos uploaded per day = 720,000 / 0.167 ≈ 4.3 million videos/day

Upload requests per second = 4.3M / 86,400 ≈ 50 uploads/sec
Peak upload traffic (2x) = 100 uploads/sec
```

---

#### Read:Write Ratio
```
Video Views (Reads) = 2.5 billion/day
Video Uploads (Writes) = 4.3 million/day

Read:Write Ratio = 2,500,000,000 / 4,300,000 ≈ 581:1

Simplified: ~600:1 (reads vastly outnumber writes)
```

**Why this matters:** This read-heavy pattern means we should optimize for reads (caching, CDN, read replicas).

---

### Monthly Traffic
```
Monthly video views = 2.5B × 30 = 75 billion views/month
Monthly uploads = 4.3M × 30 = 129 million videos/month
Monthly watch time = 2.5B × 10 min = 25 billion minutes/month
                    = 416.7 million hours/month
```

---

## Storage Estimates

### Assumptions
- **Average video file size (raw upload):** 1 GB for 10-minute video
- **Transcoding multiplier:** Videos are transcoded to 7 resolutions (240p, 360p, 480p, 720p, 1080p, 1440p, 4K)
- **Compression efficiency:**
  - 240p: 0.05 GB
  - 360p: 0.10 GB
  - 480p: 0.20 GB
  - 720p: 0.40 GB
  - 1080p: 0.80 GB
  - 1440p: 1.50 GB
  - 4K: 3.00 GB
  - **Total after transcoding:** ~6 GB per video

---

### Daily Storage Growth
```
Videos uploaded per day = 4.3 million
Storage per video (after transcoding) = 6 GB

Daily storage growth = 4.3M × 6 GB = 25.8 TB/day
```

**Translation:** YouTube needs to add ~26 Terabytes of storage capacity every single day!

---

### Yearly Storage Growth
```
Daily growth = 25.8 TB
Yearly growth = 25.8 TB × 365 = 9,417 TB/year ≈ 9.4 Petabytes/year
```

---

### 5-Year Storage Projection
```
5-year storage = 9.4 PB × 5 = 47 Petabytes
```

**Reality Check:** YouTube has been around since 2005 (20 years), so actual storage is estimated at **1 Exabyte (1000 Petabytes)** or more!

---

### Metadata Storage
```
Per video metadata:
- Video ID: 16 bytes
- Title: 200 bytes
- Description: 5 KB
- Tags: 500 bytes
- Channel ID: 16 bytes
- Upload timestamp: 8 bytes
- View count: 8 bytes
- Like/Dislike count: 8 bytes
- Comment count: 8 bytes
- Thumbnail URLs: 500 bytes

Total per video ≈ 6.3 KB

Daily metadata = 4.3M videos × 6.3 KB ≈ 27 GB/day
Yearly metadata = 27 GB × 365 ≈ 9.8 TB/year
```

**Good News:** Metadata is tiny compared to video files (GB vs PB).

---

## Bandwidth Estimates

### Assumptions
- **Average video bitrate:**
  - 240p: 400 Kbps
  - 360p: 800 Kbps
  - 480p: 1.5 Mbps
  - 720p: 2.5 Mbps
  - 1080p: 5 Mbps
  - 1440p: 9 Mbps
  - 4K: 20 Mbps
- **Average resolution watched:** 720p (2.5 Mbps)
- **Average watch duration:** 10 minutes

---

### Bandwidth for Video Streaming (Egress)
```
Data per video stream (720p, 10 min):
= 2.5 Mbps × 10 minutes × 60 seconds
= 2.5 × 600 = 1,500 Megabits
= 187.5 Megabytes ≈ 188 MB

Daily video views = 2.5 billion
Daily bandwidth = 2.5B × 188 MB = 470,000,000 GB = 470 Petabytes/day
```

**Translation:** YouTube delivers ~470 Petabytes of video data to users every day!

---

### Bandwidth for Video Uploads (Ingress)
```
Videos uploaded per day = 4.3 million
Average raw upload size = 1 GB

Daily ingress bandwidth = 4.3M × 1 GB = 4.3 PB/day
```

---

### Total Bandwidth
```
Egress (streaming) = 470 PB/day
Ingress (uploads) = 4.3 PB/day

Total bandwidth = 474.3 PB/day
```

**Cost Implication:** At $0.05/GB CDN cost, daily bandwidth cost ≈ $23.7 million/day = $8.6 billion/year! (This is why CDN optimization is critical)

---

## Key Assumptions

### Traffic Patterns
- **Peak hours:** 6 PM - 11 PM (local time in each region)
- **Peak traffic multiplier:** 3x average
- **Geographic distribution:**
  - North America: 25%
  - Europe: 25%
  - Asia: 40%
  - Rest of World: 10%

### User Behavior
- **80/20 rule:** 20% of videos account for 80% of views (optimize caching for popular content)
- **Long-tail content:** Most videos have < 1000 views
- **Viral spikes:** Some videos get 10M+ views in 24 hours

### Business Constraints
- **Revenue model:** Ad-supported + YouTube Premium subscriptions
- **Content moderation:** Required by law (DMCA, community guidelines)
- **Copyright protection:** Content ID system for detecting copyrighted material

---

## Summary: Key Numbers to Remember

| Metric | Value |
|--------|-------|
| Daily Active Users | 500 million |
| Daily Video Views | 2.5 billion |
| Peak Requests/Sec | 87,000 req/sec |
| Daily Uploads | 4.3 million videos |
| Daily Storage Growth | 25.8 TB |
| Daily Bandwidth (Egress) | 470 PB |
| Read:Write Ratio | 600:1 (read-heavy) |
| Target Availability | 99.99% |
| Video Start Latency | < 1 second |

---

## What's Next?

Now that we understand the **scale** and **requirements**, we'll design the architecture step-by-step:

1. **Step 1:** Basic architecture (client, servers, database)
2. **Step 2:** Add caching for performance
3. **Step 3:** Scale the database (replication, sharding)
4. **Step 4:** Add message queues for async processing (transcoding)
5. **Step 5:** Add CDN and object storage for video delivery
6. **Step 6:** Break into microservices

Let's begin! 🚀
