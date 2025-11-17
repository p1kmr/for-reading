# Instagram HLD - Requirements & Capacity Estimations

> **Beginner Note**: Before designing any system, we must understand WHAT we're building (functional requirements), HOW WELL it should perform (non-functional requirements), and HOW MUCH resources we need (capacity planning). This is like planning a restaurant - you need to know what dishes to serve, how many customers to handle, and how much kitchen space you need!

---

## 1. Functional Requirements

### 1.1 Core Features (In Scope)

#### User Management
- ✅ Users can create an account and login
- ✅ Users can create and edit their profile (bio, profile picture, username)
- ✅ Users can follow/unfollow other users
- ✅ Users can search for other users by username

#### Content Management
- ✅ Users can upload photos (single or multiple in carousel format)
- ✅ Users can upload short videos (up to 60 seconds)
- ✅ Users can add captions to posts
- ✅ Users can add hashtags to posts
- ✅ Users can delete their own posts

#### Social Interactions
- ✅ Users can like posts
- ✅ Users can unlike posts
- ✅ Users can comment on posts
- ✅ Users can view comments on posts
- ✅ Users can share posts

#### Feed & Discovery
- ✅ Users can view their personalized feed (posts from accounts they follow)
- ✅ Users can search by hashtags
- ✅ Feed should be ranked/sorted intelligently (popular posts appear first)

### 1.2 Out of Scope (Not Building in This Design)
- ❌ Direct messaging (DMs)
- ❌ Stories (24-hour ephemeral content)
- ❌ Reels (short-form video content)
- ❌ Live streaming
- ❌ Push notifications
- ❌ Ads and monetization
- ❌ Shopping features

> **Why limit scope?** In system design interviews and real projects, it's crucial to define boundaries. Trying to design everything at once leads to confusion. Start with core features, get them right, then expand.

---

## 2. Non-Functional Requirements

### 2.1 Performance Requirements

#### Latency
- **Feed loading**: < 100ms (users expect instant feed)
- **Photo upload**: < 2 seconds for processing
- **Video upload**: < 10 seconds for processing
- **Like/Comment action**: < 50ms (real-time feedback)

> **Beginner Tip**: Latency = time taken to respond. 100ms means user sees feed in 0.1 seconds. Beyond 200ms, users notice lag!

#### Throughput
- **Handle 500 million Daily Active Users (DAU)**
- **1 billion photos uploaded per day**
- **100 million videos uploaded per day**
- **Peak traffic**: 3x average (during events like New Year)

### 2.2 Scalability Requirements
- System should scale to **2 billion registered users**
- Support **100 billion total posts** in database
- Handle **10,000 requests per second** (average)
- Handle **30,000 requests per second** (peak)

### 2.3 Availability
- **99.99% uptime** (only 52 minutes of downtime per year)
- No single point of failure
- Graceful degradation (if some features fail, core features still work)

### 2.4 Consistency
- **Eventual consistency is acceptable** for:
  - Feed updates (okay if new post appears after 1-2 seconds)
  - Like counts (okay if count updates after few seconds)
  - Follower counts

- **Strong consistency required** for:
  - User authentication (login/logout)
  - Post creation (user should immediately see their own post)
  - Follow/Unfollow actions

> **Beginner Explanation**:
> - **Strong Consistency**: Everyone sees the same data immediately (like a scoreboard)
> - **Eventual Consistency**: Different users might see slightly different data for a brief moment, but eventually everyone sees the same thing (like social media likes - count might vary by 1-2 for few seconds)

### 2.5 Durability
- **Zero data loss** for uploaded photos/videos
- **99.999999999% durability** (11 nines - AWS S3 standard)
- Automated backups every 24 hours

### 2.6 Security
- HTTPS/TLS encryption for all data in transit
- Encryption at rest for user data
- Authentication via JWT tokens
- Input validation to prevent SQL injection, XSS attacks
- Rate limiting to prevent abuse (max 100 posts/day per user)

---

## 3. Traffic Estimations (With Beginner-Friendly Calculations)

### 3.1 Daily Active Users (DAU)
```
Assumption: 500 million DAU
Monthly Active Users (MAU): 2 billion
DAU/MAU ratio: 25% (industry standard for social media)
```

### 3.2 Read vs Write Traffic

#### Assumptions
- Average user scrolls feed: **30 minutes per day**
- Average user views: **100 posts per session**
- Average user uploads: **1 post per day** (only 20% of users post daily)

#### Calculations

**Total Reads (Feed views):**
```
- 500M DAU × 100 posts viewed = 50 billion post views per day
- Requests per second (avg) = 50B / 86,400 seconds = 578,703 req/sec
- Peak (3x) = 1.7 million req/sec
```

**Total Writes (Uploads):**
```
- 500M DAU × 20% active posters × 1 post = 100 million posts per day
- Requests per second (avg) = 100M / 86,400 = 1,157 req/sec
- Peak (3x) = 3,472 req/sec
```

**Read:Write Ratio = 50B : 100M = 500:1**

> **Key Insight**: Instagram is heavily **READ-heavy** (500 reads for every 1 write). This means we need to optimize for fast reads (use caching, CDN, read replicas).

### 3.3 Bandwidth Requirements

#### Upload Bandwidth

**Photos:**
```
- Average photo size: 2 MB (after compression)
- Photos uploaded per day: 100 million
- Daily upload bandwidth: 100M × 2 MB = 200 TB/day
- Per second: 200 TB / 86,400 = 2.31 GB/sec
```

**Videos:**
```
- Average video size: 50 MB (30 sec at 720p)
- Videos uploaded per day: 20 million (20% of posts are videos)
- Daily upload bandwidth: 20M × 50 MB = 1,000 TB/day = 1 PB/day
- Per second: 1 PB / 86,400 = 11.57 GB/sec
```

**Total Upload Bandwidth: 2.31 + 11.57 = ~14 GB/sec**

#### Download Bandwidth

```
- 50 billion post views per day
- Average post size (served from CDN): 500 KB (compressed, cached)
- Daily download: 50B × 500 KB = 25 PB/day
- Per second: 25 PB / 86,400 = 289 GB/sec
```

**Total Download Bandwidth: ~290 GB/sec (peak: 870 GB/sec)**

> **Beginner Note**: 1 GB/sec = 8 Gbps. 290 GB/sec = 2,320 Gbps = 2.32 Tbps! This is why we need CDN (Content Delivery Network) to distribute this load globally.

---

## 4. Storage Estimations

### 4.1 Media Storage (Photos & Videos)

#### Photos
```
- Photos per day: 100 million
- Average size: 2 MB
- Daily storage: 100M × 2 MB = 200 TB/day

Storage growth:
- 1 month: 200 TB × 30 = 6 PB
- 1 year: 200 TB × 365 = 73 PB
- 5 years: 73 PB × 5 = 365 PB
```

#### Videos
```
- Videos per day: 20 million
- Average size: 50 MB
- Daily storage: 20M × 50 MB = 1 PB/day

Storage growth:
- 1 month: 1 PB × 30 = 30 PB
- 1 year: 1 PB × 365 = 365 PB
- 5 years: 365 PB × 5 = 1,825 PB = 1.8 EB (Exabytes!)
```

**Total Media Storage (5 years): 365 PB + 1,825 PB = 2,190 PB = 2.2 Exabytes**

> **Storage Strategy**: Use object storage (AWS S3) with lifecycle policies:
> - Hot storage (< 30 days): S3 Standard
> - Warm storage (30-90 days): S3 Infrequent Access
> - Cold storage (> 90 days): S3 Glacier

### 4.2 Metadata Storage (Database)

#### Users Table
```
- Total users: 2 billion
- Data per user: 1 KB (user_id, email, username, password_hash, bio, profile_pic_url)
- Total: 2B × 1 KB = 2 TB
```

#### Posts Table
```
- Total posts (5 years): 100M posts/day × 365 × 5 = 182.5 billion posts
- Data per post: 2 KB (post_id, user_id, caption, timestamp, media_urls, likes_count, comments_count)
- Total: 182.5B × 2 KB = 365 TB
```

#### Likes Table
```
- Average likes per post: 100
- Total likes: 182.5B posts × 100 = 18.25 trillion likes
- Data per like: 20 bytes (user_id + post_id + timestamp)
- Total: 18.25T × 20 bytes = 365 TB
```

#### Comments Table
```
- Average comments per post: 10
- Total comments: 182.5B × 10 = 1.825 trillion
- Data per comment: 500 bytes (comment_id, user_id, post_id, text, timestamp)
- Total: 1.825T × 500 bytes = 912 TB
```

#### Follows Table
```
- Average follows per user: 200
- Total users: 2 billion
- Total relationships: 2B × 200 = 400 billion
- Data per relationship: 24 bytes (follower_id + following_id + timestamp)
- Total: 400B × 24 bytes = 9.6 TB
```

**Total Metadata Storage (5 years):**
```
Users:     2 TB
Posts:     365 TB
Likes:     365 TB
Comments:  912 TB
Follows:   10 TB
------------------------
TOTAL:     1,654 TB ≈ 1.7 PB
```

### 4.3 Cache Storage (Redis)

```
Cache goals:
- Cache hot feeds for top 10% most active users (50M users)
- Cache 100 posts per user feed
- Each post metadata: 1 KB

Total cache needed:
50M users × 100 posts × 1 KB = 5 TB

Add 50% buffer for other cache (profiles, hashtags, trending): 7.5 TB
```

**Recommended Redis cluster: 8 TB capacity (distributed across multiple nodes)**

---

## 5. Summary of Estimations

### Traffic
| Metric | Average | Peak (3x) |
|--------|---------|-----------|
| **Reads (req/sec)** | 578,000 | 1,700,000 |
| **Writes (req/sec)** | 1,157 | 3,472 |
| **Read:Write Ratio** | 500:1 | - |
| **Upload Bandwidth** | 14 GB/sec | 42 GB/sec |
| **Download Bandwidth** | 290 GB/sec | 870 GB/sec |

### Storage (5 Years)
| Storage Type | Size |
|--------------|------|
| **Media (Photos)** | 365 PB |
| **Media (Videos)** | 1,825 PB |
| **Metadata (DB)** | 1.7 PB |
| **Cache (Redis)** | 8 TB |
| **TOTAL** | ~2,192 PB = 2.2 EB |

### Key Numbers to Remember
- **DAU**: 500 million
- **Posts/day**: 100 million
- **Read:Write**: 500:1 (read-heavy)
- **Latency target**: < 100ms for feed
- **Availability**: 99.99% (52 min downtime/year)
- **Storage growth**: 1.2 PB/day

---

## 6. Assumptions & Constraints

### Assumptions
1. **User Behavior**:
   - Average user spends 30 min/day on Instagram
   - 20% of users post content daily
   - 80% of users are passive consumers

2. **Content Characteristics**:
   - 80% of posts are photos, 20% are videos
   - Photos are compressed to 2 MB average
   - Videos are 30 seconds at 720p (50 MB)

3. **Traffic Patterns**:
   - Peak traffic is 3x average (during major events)
   - Traffic is global (different timezones smooth out peaks)

4. **Technology**:
   - Use cloud infrastructure (AWS/GCP)
   - Budget is not a primary constraint
   - Focus on performance and reliability over cost

### Constraints
1. **Latency**: Feed must load in < 100ms (user expectation)
2. **Durability**: Zero data loss for uploaded media
3. **Compliance**: GDPR, data privacy laws
4. **Mobile-first**: 90% of traffic comes from mobile apps

---

## 7. Design Priorities

Based on requirements, our design should prioritize:

1. **🚀 Read Performance** (500:1 read:write ratio)
   - Heavy caching strategy
   - CDN for media delivery
   - Database read replicas

2. **📦 Storage Scalability** (2.2 EB in 5 years)
   - Object storage (S3) for media
   - Database sharding for metadata
   - Lifecycle policies for cost optimization

3. **⚡ Low Latency** (< 100ms feed)
   - Multi-level caching
   - Efficient feed generation algorithm
   - Geographic distribution (edge servers)

4. **🛡️ High Availability** (99.99%)
   - No single point of failure
   - Replication and failover
   - Graceful degradation

5. **🔒 Security & Privacy**
   - Encryption everywhere
   - Secure authentication
   - Rate limiting and abuse prevention

---

> **Next Step**: With requirements clear and numbers calculated, we'll design the architecture step-by-step, starting from a simple client-server model and gradually adding components to meet our non-functional requirements.
>
> **Remember**: These numbers guide our design decisions. For example, 500:1 read:write ratio tells us to invest heavily in read optimization (caching, CDN, read replicas) rather than write optimization.

