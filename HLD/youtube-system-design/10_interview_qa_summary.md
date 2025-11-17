# YouTube System Design - Interview Q&A & Summary

## Table of Contents
1. [Interview Q&A Section](#interview-qa-section)
2. [Design Summary](#design-summary)
3. [What to Draw First - Whiteboard Interview Checklist](#what-to-draw-first---whiteboard-interview-checklist)
4. [Key Takeaways for Beginners](#key-takeaways-for-beginners)

---

## Interview Q&A Section

### Q1: How would you scale YouTube to handle billions of users?

**Answer:**
To scale YouTube for billions of users, I would implement a multi-layered approach:

1. **Horizontal Scaling:** Use load balancers to distribute traffic across multiple application servers
2. **Database Sharding:** Partition data across multiple database instances based on user_id or video_id
3. **CDN Integration:** Serve 95%+ of video traffic from edge locations worldwide
4. **Caching Strategy:** Implement Redis for metadata, user sessions, and popular content
5. **Microservices Architecture:** Split monolith into independent services (Upload, Streaming, Search, etc.)
6. **Async Processing:** Use message queues (Kafka) for non-blocking operations like transcoding

**Trade-offs:**
- **Complexity:** More moving parts means harder debugging and operational overhead
- **Consistency:** Distributed systems introduce eventual consistency challenges
- **Cost:** More infrastructure increases operational costs

**Real-World Example:**
YouTube actually uses Google's global infrastructure with 420+ edge locations (PoPs - Points of Presence) to serve content. They also use multiple data centers in different regions for redundancy.

**Technologies:**
- Load Balancers: NGINX, HAProxy, AWS ALB
- Databases: PostgreSQL (sharded), Cassandra (analytics)
- CDN: Google Cloud CDN, Cloudflare
- Cache: Redis Cluster, Memcached
- Message Queue: Apache Kafka

---

### Q2: How does video transcoding work at scale, and how would you handle 500 hours of video uploaded every minute?

**Answer:**
Video transcoding is the process of converting uploaded videos into multiple formats and resolutions:

**Architecture:**
1. **Upload Phase:** User uploads raw video to S3 using presigned URLs
2. **Event Trigger:** Upload service publishes message to Kafka topic: `video.transcode`
3. **Worker Pool:** 24,000+ dedicated transcode workers consume messages from Kafka
4. **Processing:** Each worker uses FFmpeg to transcode video to 7 resolutions (240p to 4K)
5. **Parallel Processing:** Multiple workers can process same video (different resolutions)
6. **Storage:** Transcoded files uploaded back to S3, organized by video_id and quality

**Trade-offs:**
- **Time vs Quality:** Higher quality = longer processing time (4K video can take hours)
- **Cost vs Redundancy:** Using spot instances saves 70% cost but can be interrupted
- **Storage vs Bandwidth:** Storing all resolutions uses 6GB per video but reduces transcoding load

**Real-World Example:**
Netflix uses a similar approach with 50+ different video profiles optimized for different devices and network conditions. They pre-transcode everything during off-peak hours.

**Technologies:**
- **Transcoding Engine:** FFmpeg, AWS MediaConvert, Bitmovin
- **Worker Orchestration:** Kubernetes, AWS ECS
- **Message Queue:** Apache Kafka (handles 100k+ messages/sec)
- **Compute:** AWS EC2 Spot Instances (70% cost savings), Google Cloud Preemptible VMs

**Math:**
```
500 hours/minute uploaded
= 30,000 hours/hour
= 720,000 hours/day

Average transcoding time: 2x real-time (10 min video = 20 min processing)
Workers needed: (720,000 × 2) / 24 = 60,000 worker-hours/day = 2,500 concurrent workers
With 7 resolutions: 2,500 × 7 = 17,500 workers (we use 24,000 for headroom)
```

---

### Q3: How does CDN work and why is it critical for YouTube?

**Answer:**
CDN (Content Delivery Network) is a geographically distributed network of servers that cache and serve content from locations closest to users.

**How CDN Works:**
1. **Origin Server:** S3 stores original transcoded videos
2. **Edge Locations:** 420+ data centers worldwide (PoPs - Points of Presence)
3. **Cache Strategy:** Popular videos cached at edge, rare videos served from origin
4. **Request Flow:**
   - User in Tokyo requests video
   - Request goes to nearest CDN edge (Tokyo)
   - If cached (cache hit): Serve from Tokyo edge (5ms latency)
   - If not cached (cache miss): Fetch from origin (200ms), cache, then serve

**Why Critical:**
- **Latency Reduction:** 5ms vs 200ms for international users (40x faster)
- **Bandwidth Cost:** CDN costs $0.02/GB vs origin egress $0.09/GB (78% savings)
- **Origin Offloading:** 95% of traffic served from CDN, only 5% hits origin
- **Scalability:** CDN handles traffic spikes (viral videos) without origin server changes

**Trade-offs:**
- **Cache Invalidation:** Updating video requires purging CDN cache (complex)
- **Cost:** CDN adds infrastructure cost (~$0.02/GB), but saves on origin bandwidth
- **Consistency:** Edge caches may serve stale content (eventual consistency)

**Real-World Example:**
YouTube uses Google Cloud CDN with 420+ edge locations. A viral video getting 10M views in 24 hours would cost $200K in bandwidth without CDN, but only $40K with CDN (5x savings).

**Technologies:**
- Google Cloud CDN, Cloudflare, Akamai, AWS CloudFront
- Cache Control Headers: `Cache-Control: public, max-age=31536000`
- CDN Invalidation: Fastly API, CloudFlare purge API

---

### Q4: What database sharding strategy would you use for YouTube, and why?

**Answer:**
Sharding is partitioning data across multiple database instances to handle scale beyond a single server's capacity.

**Sharding Strategies:**

**1. Hash-Based Sharding (Recommended for YouTube):**
```
shard_id = hash(video_id) % num_shards
Example: hash("vid_abc123") % 4 = 2 → Shard 2
```

**Pros:**
- ✅ Even data distribution (no hot shards)
- ✅ Simple implementation
- ✅ Fast lookups by video_id

**Cons:**
- ❌ Difficult to rebalance (adding shards requires rehashing)
- ❌ Range queries impossible (can't get "all videos from channel_id")

**2. Range-Based Sharding:**
```
Shard 1: video_id A-F
Shard 2: video_id G-M
Shard 3: video_id N-S
Shard 4: video_id T-Z
```

**Pros:**
- ✅ Easy to rebalance (just split ranges)
- ✅ Range queries possible

**Cons:**
- ❌ Uneven distribution (hot shards if certain letters are popular)

**3. Geographic Sharding:**
```
Shard 1: US videos
Shard 2: EU videos
Shard 3: Asia videos
```

**Pros:**
- ✅ Low latency for regional users
- ✅ Data locality for compliance (GDPR)

**Cons:**
- ❌ Uneven distribution (US has more videos than small countries)
- ❌ Complex cross-region queries

**YouTube's Approach:**
- **Primary:** Hash-based sharding on video_id (4 shards initially, 16 shards for billions of videos)
- **Reads:** Replicate each shard 3x for read scaling (1 primary + 2 read replicas)
- **Writes:** All writes go to primary, async replicated to replicas

**Trade-offs:**
- **Complexity:** Application must know which shard to query
- **Joins:** Cross-shard joins are expensive (fetch data from multiple shards, join in application)
- **Transactions:** Distributed transactions across shards are complex (use Saga pattern)

**Real-World Example:**
Instagram uses hash-based sharding on user_id. They started with 2 shards and now have 1000+ shards handling billions of users.

**Technologies:**
- **Sharding Middleware:** Vitess (YouTube uses this!), Citus (PostgreSQL), ProxySQL
- **Database:** PostgreSQL with pgpool-II, MySQL with sharding proxy
- **Monitoring:** Track shard health, rebalance when one shard is >80% full

**Capacity Planning:**
```
Single PostgreSQL instance: ~1TB, 10k queries/sec
With 16 shards: 16TB capacity, 160k queries/sec
With 3x replication: 48TB total, 480k queries/sec (reads)
```

---

### Q5: How would you handle the consistency vs availability trade-off (CAP theorem)?

**Answer:**
CAP theorem states you can only have 2 out of 3: Consistency, Availability, Partition Tolerance. Since network partitions are inevitable, you must choose between Consistency (CP) or Availability (AP).

**YouTube's Strategy (Hybrid Approach):**

**CP (Consistency over Availability) for Critical Data:**
1. **User Authentication:** Must be strongly consistent (can't allow invalid logins)
2. **Subscriptions:** Must be accurate (can't show duplicate subscriptions)
3. **Payment Transactions:** Must be exact (can't charge twice)

**AP (Availability over Consistency) for Non-Critical Data:**
1. **View Counts:** Eventual consistency is fine (showing 1,234 vs 1,235 views doesn't matter)
2. **Comments:** 5-second delay is acceptable
3. **Recommendations:** Stale recommendations are okay

**Implementation:**

**Strong Consistency (CP):**
```sql
-- PostgreSQL with ACID transactions
BEGIN TRANSACTION;
INSERT INTO subscriptions (user_id, channel_id) VALUES ('user_123', 'chan_456');
COMMIT;
-- If partition happens, system returns error (unavailable) rather than wrong data
```

**Eventual Consistency (AP):**
```python
# Cassandra - always available, eventually consistent
session.execute("""
    UPDATE video_views_daily
    SET view_count = view_count + 1
    WHERE video_id = 'vid_123' AND date = '2024-01-15'
""")
# If partition happens, system accepts write to available replica,
# syncs to other replicas later (eventual consistency)
```

**Trade-offs:**
- **User Experience:** Stale data (AP) may confuse users, but downtime (CP) frustrates users
- **System Complexity:** Managing different consistency levels adds complexity
- **Cost:** Strong consistency requires synchronous replication (slower, more expensive)

**Real-World Example:**
- **Facebook:** Uses eventual consistency for likes/comments (you may see different counts on refresh)
- **Banking Systems:** Use strong consistency for transactions (ATM won't dispense money if balance check fails)

**Technologies:**
- **CP Systems:** PostgreSQL, MySQL with synchronous replication, etcd, Consul
- **AP Systems:** Cassandra, DynamoDB, Riak
- **Tunable Consistency:** Cassandra allows per-query consistency levels (ONE, QUORUM, ALL)

**YouTube's Specific Choices:**
| Feature | Consistency Model | Rationale |
|---------|------------------|-----------|
| User Login | Strong (CP) | Security critical |
| Video Upload | Strong (CP) | Creator expects immediate visibility |
| View Count | Eventual (AP) | Not critical, high write volume |
| Comments | Eventual (AP) | 5-second delay acceptable |
| Search Index | Eventual (AP) | Millions of updates/sec, slight staleness OK |
| Subscriptions | Strong (CP) | User expects immediate effect |

---

### Q6: How would you handle traffic spikes from viral videos (e.g., 10M views in 1 hour)?

**Answer:**
Viral videos create massive traffic spikes that can overwhelm systems if not designed for elasticity.

**Strategy:**

**1. CDN Auto-Scaling:**
- CDN automatically distributes content to more edge locations
- No configuration needed - CDN handles this transparently
- Cost: Pay per bandwidth usage (scales with traffic)

**2. Application Server Auto-Scaling:**
```yaml
# Kubernetes HorizontalPodAutoscaler
minReplicas: 10
maxReplicas: 1000
targetCPUUtilization: 70%
scaleUp: Add 10 pods when CPU > 70% for 2 minutes
scaleDown: Remove 10% pods when CPU < 50% for 10 minutes
```

**3. Database Read Replicas:**
- Automatically add read replicas when query load increases
- Route read traffic to replicas via load balancer
- Example: 1 primary → 10 read replicas during spike

**4. Cache Warming:**
- Pre-populate Redis cache with likely-to-be-requested data
- For trending videos, cache is hit 99%+ times (origin barely touched)

**5. Rate Limiting:**
```python
# Rate limit per user
if user_requests_last_minute > 1000:
    return 429 Too Many Requests
# Prevents single user from DDoS-ing the system
```

**6. Queue-Based Load Leveling:**
```
Analytics writes → Kafka (queue) → Workers process at steady rate
Even if 10M views/hour, Kafka buffers and workers process at max capacity
```

**Trade-offs:**
- **Cost:** Auto-scaling means paying for 1000 servers during spike vs 10 during normal times
- **Latency:** Scaling up takes 2-5 minutes (not instant), initial spike may see degraded performance
- **Capacity Planning:** Must provision max capacity in advance (can't scale beyond quota)

**Real-World Example:**
"Gangnam Style" by Psy (2012) was so popular it broke YouTube's view counter (maxed out at 2.1B). YouTube had to upgrade counter from 32-bit to 64-bit integer!

When "Baby Shark" video went viral:
- Normal: 10k req/sec
- Peak: 500k req/sec (50x increase)
- CDN automatically scaled from 100 edge servers to 2,000 edge servers
- Cost increased from $10k/day to $200k/day during peak week

**Technologies:**
- **Auto-Scaling:** Kubernetes HPA, AWS Auto Scaling Groups, Google Cloud Autoscaler
- **CDN:** Handles unlimited traffic (Google Cloud CDN served 10 Pbps+ for YouTube in 2020)
- **Rate Limiting:** Redis (token bucket algorithm), NGINX rate limit module
- **Monitoring:** Prometheus + Grafana for real-time metrics, alerting on PagerDuty

**Math:**
```
Viral video: 10M views in 1 hour
= 10,000,000 / 3,600 seconds
= 2,778 requests/sec (just for that one video!)

Normal YouTube traffic: ~30k req/sec total
One viral video adds 10% to entire platform traffic!

CDN bandwidth:
10M views × 200 MB (average video size) = 2 PB in 1 hour!
Without CDN: Would cost 2 PB × $0.09/GB = $180,000 in bandwidth
With CDN: 2 PB × $0.02/GB = $40,000 (4.5x cheaper)
```

---

### Q7: How would you implement YouTube's search functionality?

**Answer:**
Search is one of the most complex features, requiring full-text search, ranking, filtering, and autocomplete.

**Architecture:**

**1. Search Index (Elasticsearch):**
```json
{
  "video_id": "vid_123",
  "title": "System Design Interview Tips",
  "description": "Learn how to ace system design interviews",
  "tags": ["interview", "system-design", "tech"],
  "transcript": "Hello everyone, today we'll discuss...",
  "view_count": 125430,
  "published_at": "2024-01-15T10:30:00Z",
  "channel_name": "Tech Academy",
  "category": "Education"
}
```

**2. Indexing Pipeline:**
```
Video Upload → Kafka → Indexer Worker → Elasticsearch
Video Update → Kafka → Indexer Worker → Update ES Document
New Comment → Kafka → Update ES Document (increment comment_count)
```

**3. Search Query Processing:**
```python
# Multi-field search with boosting
query = {
    "multi_match": {
        "query": "system design tutorial",
        "fields": [
            "title^3",        # Title matches weighted 3x
            "tags^2",         # Tag matches weighted 2x
            "description",    # Description matches weighted 1x
            "transcript"      # Transcript matches weighted 1x
        ]
    }
}

# Filters
filters = [
    {"term": {"category": "Education"}},
    {"range": {"duration": {"gte": 600}}},  # > 10 minutes
    {"range": {"published_at": {"gte": "now-30d"}}}  # Last 30 days
]

# Sorting
sort = [
    {"_score": "desc"},      # Relevance first
    {"view_count": "desc"}   # Then by popularity
]
```

**4. Autocomplete (Edge N-Grams):**
```
User types: "sys"
Index contains: "system design", "system architecture", "systematic approach"
Edge n-grams: "s", "sy", "sys", "syst", "syste", "system"
Match found: "system design" (most popular) → Suggest first
```

**5. Ranking Algorithm:**
```python
relevance_score = (
    0.4 * text_match_score +      # How well query matches content
    0.3 * popularity_score +       # log(view_count)
    0.2 * freshness_score +        # Newer videos boosted
    0.1 * engagement_score         # (likes + comments) / views
)
```

**Trade-offs:**
- **Indexing Latency:** New videos appear in search after ~5 seconds (eventual consistency)
- **Storage Cost:** Elasticsearch requires 3x storage vs raw data (inverted index overhead)
- **Query Complexity:** Complex queries can be slow (need to cache popular searches)

**Real-World Example:**
Google Search uses a similar multi-field ranking approach with 200+ ranking factors. PageRank is just one factor, relevance, freshness, user location also matter.

**Technologies:**
- **Search Engine:** Elasticsearch (most common), Apache Solr, Typesense
- **Autocomplete:** Redis (cached suggestions), Elasticsearch (edge n-grams)
- **Ranking:** Custom ML models (Python + TensorFlow) trained on user behavior data
- **Caching:** Redis for popular search queries (cache hit rate >80%)

**Performance:**
```
Elasticsearch cluster: 10 nodes, 100M indexed videos
Query time: 50ms (p50), 200ms (p99)
Index update latency: 5 seconds (eventual consistency)
Cache hit rate: 85% (popular searches cached in Redis)
```

---

### Q8: How would you design YouTube's recommendation system?

**Answer:**
Recommendation system is critical - 70% of YouTube watch time comes from recommendations!

**Architecture:**

**1. Candidate Generation (Narrow down billions → thousands):**
```python
# Collaborative Filtering
user_watch_history = ["vid_1", "vid_2", "vid_3"]
similar_users = find_users_with_similar_history(user_watch_history)  # 1000 similar users
candidate_videos = get_videos_watched_by(similar_users)  # 10,000 candidates
```

**2. Ranking (Thousands → 20 final recommendations):**
```python
# ML Model (Neural Network)
features = {
    "user_features": {
        "watch_history": [...],
        "liked_videos": [...],
        "subscriptions": [...],
        "demographics": {"age": 25, "country": "US"}
    },
    "video_features": {
        "category": "Education",
        "duration": 600,
        "view_count": 125430,
        "like_ratio": 0.95,
        "published_days_ago": 5
    },
    "context_features": {
        "time_of_day": "evening",
        "device": "mobile",
        "previous_session_duration": 1800
    }
}

score = ml_model.predict(features)  # Probability user will watch this video
top_20_videos = sort_by_score_desc(candidate_videos)[:20]
```

**3. Diversity & Freshness:**
```python
# Don't recommend only similar content
recommendations = [
    top_20_videos[:15],           # 15 personalized recommendations
    trending_videos[:3],          # 3 trending videos (exploration)
    new_from_subscriptions[:2]    # 2 recent uploads from subscribed channels
]
```

**4. Real-Time Updates:**
```
User watches video → Kafka → Update user profile in Redis
User likes video → Kafka → Retrain model (batch job every 6 hours)
User skips video → Kafka → Negative signal (reduce similar recommendations)
```

**Trade-offs:**
- **Cold Start Problem:** New users have no history (show trending videos instead)
- **Filter Bubble:** Recommending similar content creates echo chamber (add diversity)
- **Compute Cost:** Training ML models on billions of interactions is expensive (use GPUs)
- **Latency:** Real-time recommendations must load in <200ms (cache aggressively)

**Real-World Example:**
Netflix's recommendation system saves them $1B/year in reduced churn. 80% of what people watch comes from recommendations, not search.

**Technologies:**
- **ML Framework:** TensorFlow, PyTorch (YouTube uses TensorFlow)
- **Feature Store:** Feast, Tecton (store user/video features)
- **Model Serving:** TensorFlow Serving, TorchServe (low-latency predictions)
- **Caching:** Redis for user embeddings and top-N recommendations per user
- **Training Infrastructure:** Google Cloud TPUs, AWS SageMaker

**System Scale:**
```
Users: 500M daily active users
Videos: 1B+ videos in catalog
Interactions: 10B+ events/day (views, likes, skips)

Model training:
- Input: 10B events/day
- Training data: Last 30 days = 300B events
- Model size: 10GB (neural network with 100M parameters)
- Training time: 8 hours on 100 GPUs
- Retraining frequency: Every 6 hours

Inference:
- Requests/sec: 100k (real-time recommendations)
- Latency: <200ms (p99)
- Cache hit rate: 60% (recommendations cached per user)
```

---

### Q9: How would you implement disaster recovery for YouTube?

**Answer:**
Disaster recovery ensures business continuity when entire data centers fail (natural disasters, power outages, cyberattacks).

**Strategy:**

**1. Multi-Region Architecture:**
```
Primary Region: US-East (Virginia)
Secondary Region: US-West (Oregon)
Tertiary Region: EU-West (Ireland)

Normal operation:
- US users → US-East (50%) + US-West (50%)
- EU users → EU-West (100%)

Disaster (US-East fails):
- US users → US-West (100%)
- EU users → EU-West (100%)
```

**2. Data Replication:**

**Hot Backup (Real-Time Replication):**
```
PostgreSQL:
- Primary: US-East
- Standby Replica: US-West (streaming replication, <1 second lag)
- If primary fails, promote standby to primary (automatic failover)

S3 (Video Storage):
- Cross-region replication enabled
- Videos uploaded to US-East automatically replicated to US-West
- Replication lag: 5-10 minutes
```

**Cold Backup (Point-in-Time Snapshots):**
```
PostgreSQL:
- Daily snapshot to S3 (retained for 30 days)
- Point-in-time recovery (can restore to any time in last 30 days)

Video Storage:
- Daily snapshot to Glacier (long-term archival)
- Recovery time: 12 hours (Glacier retrieval time)
```

**3. Failover Strategy:**

**Automatic Failover (RTO: 2 minutes):**
```
Health check fails in US-East → DNS updates to point to US-West
- Route53 health checks every 30 seconds
- If 3 consecutive failures → Failover to US-West
- DNS TTL: 60 seconds (clients switch within 1 minute)
```

**Manual Failover (RTO: 15 minutes):**
```
Admin detects catastrophic failure → Manual DNS switch + database promotion
Used for: Major incidents, planned maintenance
```

**4. Recovery Metrics:**

**RTO (Recovery Time Objective):** How long until service restored?
- Critical services (video playback): 2 minutes
- Non-critical (recommendations): 15 minutes

**RPO (Recovery Point Objective):** How much data loss is acceptable?
- User data: 0 seconds (real-time replication)
- Analytics: 5 minutes (can tolerate recent data loss)

**Trade-offs:**
- **Cost:** Running multi-region infrastructure doubles infrastructure cost
- **Complexity:** Synchronizing data across regions is complex (consistency challenges)
- **Performance:** Cross-region replication adds latency (100ms+ between US-EU)

**Real-World Example:**
In 2021, AWS us-east-1 region went down for 7 hours. Companies with multi-region setup (Netflix, Airbnb) were unaffected. Companies relying solely on us-east-1 were completely offline.

**Technologies:**
- **DNS Failover:** AWS Route53, Cloudflare DNS (health checks + automatic failover)
- **Database Replication:** PostgreSQL streaming replication, MySQL binlog replication
- **Storage Replication:** AWS S3 Cross-Region Replication, GCS Multi-Region
- **Monitoring:** PagerDuty, Datadog, New Relic (alert on region failures)

**Disaster Recovery Test:**
```
Quarterly DR drill:
1. Simulate US-East failure (10:00 AM)
2. Monitor automatic failover (should complete by 10:02 AM)
3. Verify all services operational in US-West
4. Test database consistency (no data loss)
5. Test rollback (switch back to US-East)
6. Document lessons learned
```

---

### Q10: How would you optimize costs for YouTube's infrastructure?

**Answer:**
At YouTube's scale, 1% cost optimization = millions of dollars saved annually.

**Cost Breakdown (Estimated for YouTube):**
```
Total annual cost: ~$15 billion
- Bandwidth (CDN): $8B (53%)
- Storage (videos): $4B (27%)
- Compute (servers): $2B (13%)
- Database: $500M (3%)
- Other (monitoring, tools): $500M (3%)
```

**Optimization Strategies:**

**1. CDN Optimization (Biggest Savings):**
```python
# Strategy: Aggressive caching for popular videos
if video.view_count > 1_000_000:
    cache_ttl = "7 days"  # High traffic, cache longer
elif video.view_count > 100_000:
    cache_ttl = "1 day"
else:
    cache_ttl = "1 hour"  # Low traffic, cache shorter

# Cache hit rate improvement: 90% → 95%
# Cost saving: 5% of $8B = $400M/year!
```

**2. Storage Tiering:**
```
Recently uploaded videos (< 30 days):
- S3 Standard ($0.023/GB/month)
- Accessed frequently, need low latency

Old popular videos (> 30 days, high views):
- S3 Standard ($0.023/GB/month)
- Still accessed frequently

Old unpopular videos (> 90 days, < 1000 views/month):
- S3 Infrequent Access ($0.0125/GB/month) - 46% cheaper!
- Accessed rarely, can tolerate higher latency

Ancient videos (> 1 year, < 100 views/month):
- Glacier Deep Archive ($0.00099/GB/month) - 96% cheaper!
- Almost never accessed, retrieval takes 12 hours

Cost saving: 30% of $4B = $1.2B/year!
```

**3. Compute Optimization:**
```
# Use Spot Instances for batch jobs (transcoding)
On-Demand Instance: $0.50/hour
Spot Instance: $0.15/hour (70% cheaper!)

Risk: Spot instances can be terminated with 2-minute notice
Mitigation: Use for stateless jobs (transcoding), save progress to S3

Transcoding cost:
- 24,000 workers × 24 hours × $0.50 = $288k/day
- With spot instances: 24,000 × 24 × $0.15 = $86k/day
- Savings: $202k/day = $73M/year!
```

**4. Database Optimization:**
```sql
-- Index optimization (reduce query time = less CPU)
CREATE INDEX idx_videos_trending ON videos(published_at DESC, view_count DESC);
-- Query time: 2000ms → 20ms (100x faster!)

-- Caching (reduce database load)
SELECT * FROM videos WHERE video_id = 'vid_123';  -- Hit PostgreSQL
-- Cache in Redis for 1 hour
-- Subsequent requests hit Redis (1ms vs 20ms)

-- Database cost reduction: 20% of $500M = $100M/year
```

**5. Compression:**
```python
# Video compression (AV1 codec)
H.264 codec: 100 MB for 10-minute 1080p video
AV1 codec: 50 MB for same quality (50% smaller!)

Storage saving: 50% of $4B = $2B/year!
Bandwidth saving: 50% of $8B = $4B/year!

Trade-off: AV1 encoding takes 10x longer (increase transcoding cost by $50M/year)
Net saving: $6B - $50M = $5.95B/year (HUGE!)
```

**6. Reserved Instances (1-3 year commitment):**
```
On-Demand: $0.50/hour
1-Year Reserved: $0.35/hour (30% cheaper)
3-Year Reserved: $0.25/hour (50% cheaper)

For steady-state workloads (streaming servers), use reserved instances
Savings: 30% of $2B = $600M/year
```

**Trade-offs:**
- **Performance vs Cost:** Cheaper storage tiers have higher latency
- **Reliability vs Cost:** Spot instances can be terminated unexpectedly
- **Compression vs Quality:** Aggressive compression can reduce video quality
- **Cache vs Freshness:** Longer cache TTL means stale data

**Real-World Example:**
Dropbox saved $75M/year by moving from AWS S3 to their own storage infrastructure. But this required 2 years of engineering effort and 100+ engineers.

**Technologies:**
- **Cost Monitoring:** AWS Cost Explorer, Google Cloud Billing, Datadog Cloud Cost
- **Storage Lifecycle:** AWS S3 Lifecycle Policies, Google Cloud Storage Lifecycle
- **Spot Instances:** AWS EC2 Spot, Google Cloud Preemptible VMs
- **Compression:** AV1 codec (ffmpeg-based), VP9 codec

**Cost Optimization Roadmap:**
```
Year 1 (Quick Wins):
- Storage tiering: Save $1.2B
- CDN optimization: Save $400M
- Total Year 1 Savings: $1.6B

Year 2 (Medium Effort):
- Spot instances: Save $73M
- Reserved instances: Save $600M
- Database optimization: Save $100M
- Total Year 2 Savings: $773M

Year 3 (Long-Term):
- AV1 compression migration: Save $6B
- Custom CDN (like Dropbox): Save $2B
- Total Year 3 Savings: $8B

Cumulative 3-Year Savings: $10.4B!
```

---

### Q11: What security measures would you implement for YouTube?

**Answer:**
Security is critical to protect user data, prevent abuse, and comply with regulations (GDPR, COPPA).

**Security Layers:**

**1. Authentication & Authorization:**
```python
# JWT (JSON Web Token) for stateless authentication
token = jwt.encode({
    "user_id": "usr_123",
    "email": "user@example.com",
    "exp": datetime.utcnow() + timedelta(hours=1)  # Expires in 1 hour
}, secret_key, algorithm="HS256")

# Authorization (RBAC - Role-Based Access Control)
if user.role == "admin":
    allow_delete_video()
elif user.role == "creator":
    if video.owner_id == user.id:
        allow_edit_video()
    else:
        deny("Unauthorized: Not video owner")
```

**2. Data Encryption:**
```
At Rest:
- Database: AES-256 encryption for all PostgreSQL data
- Storage: S3 server-side encryption (SSE-S3)
- Backups: Encrypted snapshots in Glacier

In Transit:
- HTTPS/TLS 1.3 for all API communication
- Certificate pinning for mobile apps (prevent MITM attacks)
- gRPC with mutual TLS for inter-service communication
```

**3. Rate Limiting & DDoS Protection:**
```python
# Prevent brute-force attacks
if failed_login_attempts(user_email) > 5:
    block_login_for(user_email, duration="30 minutes")

# Prevent API abuse
if user_requests_last_minute > 1000:
    return 429 Too Many Requests

# CloudFlare DDoS Protection
# - Filters 10M+ malicious requests/sec
# - Blocks traffic from known bot networks
```

**4. Input Validation & Sanitization:**
```python
# Prevent SQL injection
cursor.execute(
    "SELECT * FROM users WHERE email = %s",  # Parameterized query ✅
    (user_email,)
)
# DON'T DO: f"SELECT * FROM users WHERE email = '{user_email}'"  # ❌ SQL injection!

# Prevent XSS (Cross-Site Scripting)
def sanitize_comment(comment_text):
    # Remove <script> tags, javascript: URLs
    clean_text = bleach.clean(comment_text, tags=[], strip=True)
    return html.escape(clean_text)
```

**5. Content Security:**
```
- Content ID System: Detect copyrighted content (audio fingerprinting)
- Abuse Detection: ML model flags inappropriate videos (violence, spam)
- Age Restrictions: COPPA compliance for children's content
- Private Videos: Generate signed URLs with expiration (can't share link)
```

**6. Infrastructure Security:**
```
- Firewall: Only allow HTTPS (443), SSH (22 from VPN only)
- VPC: Isolate database in private subnet (no internet access)
- IAM: Principle of least privilege (service accounts have minimal permissions)
- Secrets Management: Vault, AWS Secrets Manager (no hardcoded passwords)
```

**Trade-offs:**
- **Security vs UX:** 2FA adds friction but prevents account takeover
- **Security vs Performance:** Encryption adds 5-10ms latency
- **Cost:** CloudFlare DDoS protection costs $200k/month

**Real-World Example:**
In 2014, celebrity iCloud accounts were hacked due to weak passwords and no 2FA. Apple now enforces 2FA for all accounts.

**Technologies:**
- **Authentication:** Auth0, Okta, AWS Cognito
- **Encryption:** AWS KMS, HashiCorp Vault
- **DDoS Protection:** CloudFlare, AWS Shield
- **Security Scanning:** Snyk, OWASP ZAP, Burp Suite
- **Monitoring:** Splunk, Datadog Security Monitoring

---

### Q12: How would you design live streaming for YouTube Live?

**Answer:**
Live streaming is fundamentally different from on-demand video - content is generated in real-time with <5 second latency.

**Architecture:**

**1. Ingest (Creator → YouTube):**
```
Creator's Encoder (OBS, Streamlabs):
- Captures webcam + microphone
- Encodes to H.264 video + AAC audio
- Streams to YouTube via RTMP protocol

YouTube Ingest Server:
- Receives RTMP stream
- Validates stream key (authentication)
- Writes raw stream to buffer
```

**2. Transcoding (Real-Time):**
```
Raw stream (1080p, 6 Mbps) → Transcoder
- Transcode to multiple qualities (360p, 480p, 720p, 1080p)
- Generate HLS segments (2-second chunks)
- Segment 1: 0-2 sec
- Segment 2: 2-4 sec
- Segment 3: 4-6 sec (created in real-time as creator streams)
```

**3. Distribution (YouTube → Viewers):**
```
HLS Segments → CDN → Viewers
- CDN caches segments for 2-10 seconds
- Viewers request latest segment every 2 seconds
- Latency: 5-15 seconds (glass-to-glass)
```

**4. Low-Latency Streaming (LL-HLS):**
```
Traditional HLS: 15-30 second latency
LL-HLS (Low-Latency HLS): 2-5 second latency

How it works:
- Smaller chunks (200ms instead of 2 seconds)
- HTTP/2 push (server pushes chunks before client requests)
- Partial segment delivery (don't wait for entire chunk)
```

**5. Chat & Interactions:**
```
WebSocket connection:
- Bi-directional real-time communication
- Chat messages propagated to all viewers via pub/sub (Redis)
- Viewer count updated every second
```

**Trade-offs:**
- **Latency vs Quality:** Lower latency = smaller buffers = more buffering/stuttering
- **Cost:** Live transcoding is 10x more expensive than on-demand (must happen in real-time)
- **Scalability:** 100k concurrent viewers = 100k WebSocket connections (resource intensive)

**Real-World Example:**
Twitch uses LL-HLS for <3 second latency. Traditional HLS had 20-30 second delay, making real-time interactions (chat, polls) feel disconnected.

**Technologies:**
- **Ingest Protocol:** RTMP, SRT, WebRTC
- **Transcoding:** FFmpeg (real-time mode), AWS MediaLive, Wowza
- **Streaming Protocol:** HLS, LL-HLS, DASH
- **Chat:** WebSocket (Socket.io, Pusher), Redis Pub/Sub

**System Scale:**
```
Peak concurrent live streams: 50,000
Average viewers per stream: 100
Peak concurrent viewers: 5 million

Bandwidth:
- 5M viewers × 2 Mbps (720p) = 10 Tbps!
- CDN distributes 99% of this traffic

Transcoding cost:
- 50k streams × $0.05/minute = $2,500/minute = $3.6M/day!
```

---

### Q13: How would you implement YouTube's notification system?

**Answer:**
Notification system must deliver millions of notifications per second across multiple channels (push, email, in-app).

**Architecture:**

**1. Event Collection:**
```python
# Producer (various services)
kafka.publish("notification.new_video", {
    "channel_id": "chan_123",
    "video_id": "vid_456",
    "title": "New Tutorial Released!",
    "timestamp": "2024-01-15T10:00:00Z"
})

kafka.publish("notification.new_comment", {
    "video_id": "vid_789",
    "commenter": "user_abc",
    "comment": "Great video!",
    "video_owner": "user_xyz"
})
```

**2. Subscriber Lookup:**
```python
# Notification Service (consumer)
def on_new_video(event):
    channel_id = event["channel_id"]

    # Get all subscribers (batch query)
    subscribers = db.query("""
        SELECT user_id, notification_preferences
        FROM subscriptions
        WHERE channel_id = %s
        AND notifications_enabled = TRUE
    """, channel_id)

    # 1M subscribers → 1M notifications to send!
```

**3. Fanout Strategy:**

**Push Fanout (Write-Heavy):**
```python
# Write to each user's notification feed immediately
for subscriber in subscribers:
    redis.lpush(f"notifications:{subscriber.user_id}", notification)
    # 1M subscribers = 1M Redis writes

Pros: Fast reads (just query user's feed)
Cons: Slow writes (1M writes for popular channel)
```

**Pull Fanout (Read-Heavy):**
```python
# Store notification once, users query for relevant notifications
redis.set(f"notification:{notification_id}", notification_data)

# When user opens app:
subscribed_channels = db.get_subscriptions(user_id)
notifications = db.query("""
    SELECT * FROM notifications
    WHERE channel_id IN (%s)
    AND created_at > %s
    ORDER BY created_at DESC
    LIMIT 20
""", subscribed_channels, last_seen_time)

Pros: Fast writes (single write)
Cons: Slow reads (must query and filter)
```

**Hybrid Approach (YouTube's Strategy):**
```python
# Push fanout for users with < 10k subscribers
# Pull fanout for celebrities with > 10k subscribers

if subscriber_count < 10000:
    push_fanout()  # Write to each subscriber's feed
else:
    pull_fanout()  # User queries on feed load
```

**4. Delivery Channels:**
```python
# Multi-channel delivery
async def deliver_notification(user_id, notification):
    user_prefs = get_preferences(user_id)

    if user_prefs.push_enabled:
        await send_push_notification(user_id, notification)  # Firebase Cloud Messaging

    if user_prefs.email_enabled:
        await queue_email(user_id, notification)  # SendGrid, SES

    if user_prefs.sms_enabled:
        await send_sms(user_id, notification)  # Twilio
```

**5. Deduplication:**
```python
# Prevent duplicate notifications
notification_key = f"{user_id}:{video_id}:new_video"
if redis.exists(notification_key):
    return  # Already notified

redis.setex(notification_key, ttl=86400, value=1)  # 24-hour dedup window
send_notification()
```

**Trade-offs:**
- **Latency vs Throughput:** Batch notifications for efficiency vs real-time delivery
- **Cost:** Push notifications to 1M users = 1M API calls to Firebase ($50/day)
- **Reliability:** Email may be delayed/filtered, push requires user to install app

**Real-World Example:**
When MrBeast (100M subscribers) uploads a video:
- 100M notifications need to be sent
- Using pull fanout: 1 write to notifications table
- Users query for notifications when opening app
- Saves 100M writes vs push fanout!

**Technologies:**
- **Message Queue:** Kafka (handles 1M+ events/sec)
- **Push Notifications:** Firebase Cloud Messaging, Apple Push Notification Service (APNS)
- **Email:** SendGrid, AWS SES, Mailgun
- **SMS:** Twilio, AWS SNS
- **In-App:** WebSocket (Socket.io), Server-Sent Events (SSE)

**Performance:**
```
Peak notification load: 10M notifications/minute (viral video uploaded)
Kafka throughput: 1M messages/sec (more than enough)
FCM (Firebase) throughput: 500k push notifications/sec
Email throughput: 100k emails/sec (SendGrid)

Delivery SLA:
- Push: <5 seconds (90% delivered)
- Email: <1 minute (95% delivered)
- In-app: Real-time (WebSocket)
```

---

### Q14: How would you handle the "cold start" problem for new users/videos?

**Answer:**
Cold start problem: New users have no watch history, new videos have no views. How to provide recommendations?

**For New Users (No Watch History):**

**Strategy 1: Popular Content:**
```python
# Show trending videos (most popular in last 24 hours)
trending_videos = db.query("""
    SELECT video_id, title, view_count
    FROM videos
    WHERE published_at > NOW() - INTERVAL '24 hours'
    ORDER BY view_count DESC
    LIMIT 20
""")
```

**Strategy 2: Geographic Personalization:**
```python
# Show popular videos in user's country
if user.country == "US":
    show_trending_in_us()
elif user.country == "IN":
    show_trending_in_india()
```

**Strategy 3: Onboarding Quiz:**
```
During sign-up:
"What topics interest you?"
[ ] Technology
[ ] Gaming
[ ] Music
[ ] Education
[ ] Sports

Use selections to bootstrap recommendations
```

**Strategy 4: Hybrid Approach:**
```python
recommendations = [
    global_trending[:5],          # 5 globally popular videos
    country_trending[:5],         # 5 popular in user's country
    category_trending[:5],        # 5 popular in selected categories
    recent_uploads[:5]            # 5 recently uploaded videos
]
```

**For New Videos (No Views/Engagement):**

**Strategy 1: Bootstrapping Period:**
```python
# For first 24 hours, show to subset of users
if video.age < 24_hours:
    # Show to 10k random users + all subscribers
    show_to_random_sample(size=10000)
    show_to_subscribers(channel_id)

    # Monitor engagement
    if click_through_rate > 0.1:
        # High engagement, promote to more users
        expand_audience(multiplier=10)
```

**Strategy 2: Content-Based Filtering:**
```python
# Recommend based on video attributes (not user behavior)
similar_videos = find_videos_with_similar({
    "title": "System Design Tutorial",
    "tags": ["programming", "interview"],
    "category": "Education",
    "duration": 600
})
# Show new video to users who watched similar videos
```

**Strategy 3: Explore/Exploit Trade-off:**
```python
# 80% exploit (show proven recommendations)
# 20% explore (show new/random videos)
if random.random() < 0.2:
    show_new_or_random_video()  # Exploration
else:
    show_personalized_recommendation()  # Exploitation
```

**Trade-offs:**
- **Accuracy vs Discovery:** Showing trending content is safe but not personalized
- **Creator Experience:** New creators struggle to get initial views (chicken-egg problem)
- **Diversity vs Relevance:** Exploration reduces short-term engagement but improves long-term

**Real-World Example:**
TikTok's "For You Page" aggressively explores new content. They show new videos to small audience, measure engagement (watch time, likes), then promote high-performing videos exponentially. This helps new creators go viral quickly.

**Technologies:**
- **A/B Testing:** Optimizely, Google Optimize (test different cold start strategies)
- **ML Framework:** TensorFlow, PyTorch (content-based filtering models)
- **Analytics:** Amplitude, Mixpanel (measure new user engagement)

---

### Q15: How would you design YouTube's analytics dashboard for creators?

**Answer:**
Creators need insights into video performance to optimize content strategy.

**Architecture:**

**1. Data Collection (Real-Time Events):**
```python
# Every video view generates analytics event
analytics_event = {
    "event_type": "video_view",
    "video_id": "vid_123",
    "user_id": "usr_456",
    "timestamp": "2024-01-15T10:30:00Z",
    "watch_duration": 450,  # seconds
    "quality": "1080p",
    "device": "mobile",
    "country": "US",
    "traffic_source": "youtube_search",
    "referrer": "google.com"
}

kafka.publish("analytics.events", analytics_event)
# 100k events/sec peak
```

**2. Stream Processing (Apache Spark):**
```python
# Real-time aggregation
spark_stream = spark.readStream.kafka("analytics.events")

# Aggregate by 1-minute windows
aggregated = (
    spark_stream
    .groupBy(
        window("timestamp", "1 minute"),
        "video_id"
    )
    .agg(
        count("*").alias("view_count"),
        sum("watch_duration").alias("total_watch_time"),
        countDistinct("user_id").alias("unique_viewers"),
        avg("watch_duration").alias("avg_watch_duration")
    )
)

# Write to Cassandra (time-series database)
aggregated.writeStream.cassandra("video_stats_realtime")
```

**3. Batch Processing (Daily Rollups):**
```python
# Nightly job: Aggregate all time-series data
daily_stats = db.query("""
    SELECT
        video_id,
        DATE(timestamp) as date,
        SUM(view_count) as daily_views,
        SUM(watch_time) as daily_watch_time,
        AVG(avg_view_percentage) as avg_completion
    FROM video_stats_realtime
    WHERE timestamp >= CURRENT_DATE - INTERVAL '1 day'
    GROUP BY video_id, DATE(timestamp)
""")

# Store in PostgreSQL (queryable by dashboard)
```

**4. Dashboard API:**
```python
GET /analytics/videos/{video_id}?start_date=2024-01-01&end_date=2024-01-31

Response:
{
    "summary": {
        "total_views": 125430,
        "total_watch_time_hours": 215432,
        "avg_view_duration": 1845,  # seconds
        "avg_percentage_viewed": 51.25,
        "subscriber_growth": 2340
    },
    "daily_stats": [
        {
            "date": "2024-01-15",
            "views": 8234,
            "watch_time_hours": 14567,
            "unique_viewers": 7456,
            "likes": 623,
            "comments": 145
        }
    ],
    "demographics": {
        "age_groups": {"18-24": 0.25, "25-34": 0.45, ...},
        "countries": {"US": 0.40, "IN": 0.25, ...},
        "devices": {"mobile": 0.60, "desktop": 0.30, "tv": 0.10}
    },
    "traffic_sources": {
        "youtube_search": 0.35,
        "suggested_videos": 0.30,
        "external": 0.20,
        "direct": 0.15
    },
    "audience_retention": [
        {"timestamp": 0, "percentage": 1.0},
        {"timestamp": 10, "percentage": 0.85},
        {"timestamp": 60, "percentage": 0.60},
        {"timestamp": 300, "percentage": 0.30}
    ]
}
```

**5. Caching Strategy:**
```python
# Cache dashboard queries (expensive aggregations)
cache_key = f"analytics:{video_id}:{start_date}:{end_date}"

if redis.exists(cache_key):
    return redis.get(cache_key)  # Cache hit

# Cache miss - query database
result = query_analytics_database()
redis.setex(cache_key, ttl=3600, value=result)  # Cache for 1 hour
return result
```

**Trade-offs:**
- **Real-Time vs Accuracy:** Real-time stats may have 5% error, batch processing is 100% accurate
- **Storage Cost:** Storing per-minute stats for all videos = 100TB+ data
- **Query Performance:** Complex analytics queries can take 10+ seconds (needs caching)

**Real-World Example:**
YouTube Studio (creator dashboard) shows:
- Real-time view count (updated every minute)
- Revenue estimate (updated daily)
- Audience retention graph (shows drop-off points)
- Traffic source breakdown (where viewers came from)

**Technologies:**
- **Stream Processing:** Apache Spark Streaming, Apache Flink, Apache Kafka Streams
- **Time-Series Database:** Cassandra, InfluxDB, TimescaleDB
- **Batch Processing:** Apache Spark, Apache Hadoop
- **Caching:** Redis, Memcached
- **Visualization:** Chart.js, D3.js, Grafana

**System Scale:**
```
Analytics events: 10B/day (100k/sec peak)
Data storage: 500TB (time-series data for all videos)
Query latency: <1 second (p95)
Dashboard requests: 50k/sec (creators checking stats)
Cache hit rate: 80% (popular queries cached)
```

---

### Q16: How would you implement rate limiting to prevent abuse?

**Answer:**
Rate limiting prevents abuse (spam, DDoS, scraping) by limiting requests per user/IP.

**Algorithms:**

**1. Token Bucket (Most Common):**
```python
class TokenBucket:
    def __init__(self, capacity=100, refill_rate=10):
        self.capacity = capacity  # Max tokens
        self.tokens = capacity    # Current tokens
        self.refill_rate = refill_rate  # Tokens/second
        self.last_refill = time.time()

    def allow_request(self):
        # Refill tokens based on time elapsed
        now = time.time()
        elapsed = now - self.last_refill
        self.tokens = min(self.capacity, self.tokens + elapsed * self.refill_rate)
        self.last_refill = now

        # Check if token available
        if self.tokens >= 1:
            self.tokens -= 1
            return True  # Allow request
        return False  # Reject request

# Usage
bucket = redis.get(f"rate_limit:{user_id}")
if bucket.allow_request():
    process_request()
else:
    return 429 Too Many Requests
```

**2. Fixed Window:**
```python
# Allow 1000 requests per hour
def rate_limit_fixed_window(user_id):
    current_hour = datetime.now().strftime("%Y-%m-%d-%H")
    key = f"rate_limit:{user_id}:{current_hour}"

    count = redis.incr(key)
    if count == 1:
        redis.expire(key, 3600)  # Expire after 1 hour

    if count > 1000:
        return False  # Rate limit exceeded
    return True  # Allow request
```

**3. Sliding Window Log:**
```python
# More accurate than fixed window
def rate_limit_sliding_window(user_id, window=60, limit=100):
    key = f"rate_limit:{user_id}"
    now = time.time()

    # Remove old requests (outside window)
    redis.zremrangebyscore(key, 0, now - window)

    # Count requests in window
    count = redis.zcard(key)

    if count < limit:
        redis.zadd(key, {now: now})  # Add current request
        redis.expire(key, window)
        return True  # Allow
    return False  # Reject

# Example: 100 requests per minute
# More fair than fixed window (no burst at window boundary)
```

**4. Distributed Rate Limiting (Redis):**
```python
# Using Redis for distributed rate limiting
from redis import Redis
import time

redis_client = Redis(host='redis-cluster', port=6379)

def distributed_rate_limit(user_id, limit=1000, window=60):
    key = f"rate_limit:{user_id}"

    pipe = redis_client.pipeline()
    pipe.incr(key)
    pipe.expire(key, window)
    result = pipe.execute()

    request_count = result[0]

    if request_count > limit:
        raise RateLimitExceeded(
            f"Rate limit exceeded: {request_count}/{limit} requests in {window}s"
        )
```

**5. Different Limits for Different Endpoints:**
```python
rate_limits = {
    "/search": {"limit": 100, "window": 60},          # 100/min
    "/upload": {"limit": 10, "window": 3600},         # 10/hour
    "/videos/{id}": {"limit": 1000, "window": 60},    # 1000/min
    "/comments": {"limit": 20, "window": 60}          # 20/min (prevent spam)
}

def check_rate_limit(user_id, endpoint):
    config = rate_limits[endpoint]
    return rate_limit(user_id, config["limit"], config["window"])
```

**Trade-offs:**
- **Token Bucket vs Fixed Window:** Token bucket allows bursts, fixed window is simpler
- **Accuracy vs Performance:** Sliding window is most accurate but requires more Redis operations
- **User Experience:** Strict limits prevent abuse but may frustrate legitimate power users

**Real-World Example:**
Twitter API rate limits:
- Free tier: 500 requests/15 minutes
- Paid tier: 10,000 requests/15 minutes
- If exceeded: 429 Too Many Requests + wait time in header

**Technologies:**
- **Implementation:** Redis (distributed rate limiting)
- **Middleware:** NGINX (ngx_http_limit_req_module), Kong (rate limiting plugin)
- **Libraries:** python-ratelimit, express-rate-limit (Node.js)

**Response Headers:**
```
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1674654000
Retry-After: 60
```

---

### Q17: How would you handle GDPR compliance and data privacy?

**Answer:**
GDPR (General Data Protection Regulation) requires protecting user privacy and giving users control over their data.

**Key Requirements:**

**1. Right to Access (User can request all data):**
```python
GET /users/{user_id}/data-export

# Generate downloadable archive with:
# - User profile
# - Watch history
# - Liked videos
# - Comments
# - Uploaded videos
# - Search history

def export_user_data(user_id):
    data = {
        "profile": db.get_user(user_id),
        "watch_history": db.get_watch_history(user_id),
        "likes": db.get_liked_videos(user_id),
        "comments": db.get_user_comments(user_id),
        "videos": db.get_uploaded_videos(user_id),
        "searches": db.get_search_history(user_id)
    }

    # Create JSON file
    with open(f"user_{user_id}_data.json", "w") as f:
        json.dump(data, f)

    # Send download link via email
    send_email(user.email, download_link)
```

**2. Right to Delete (User can request data deletion):**
```python
DELETE /users/{user_id}

# Hard delete vs soft delete
def delete_user(user_id):
    # Soft delete (recommended for business continuity)
    db.update_user(user_id, {
        "status": "deleted",
        "deleted_at": datetime.now(),
        "email": None,  # Anonymize PII
        "name": None,
        "phone": None
    })

    # Keep:
    # - Video views (for analytics, anonymized)
    # - Comments (attribute to "Deleted User")

    # Delete:
    # - User profile
    # - Watch history
    # - Liked videos
    # - Search history

    # Propagate deletion to all services
    kafka.publish("user.deleted", {"user_id": user_id})
```

**3. Data Minimization (Only collect necessary data):**
```python
# DON'T collect:
signup_form = {
    "email": required,
    "password": required,
    # "social_security_number": NOT NEEDED ❌
    # "home_address": NOT NEEDED ❌
}

# Only collect what's necessary for service
```

**4. Consent Management:**
```python
# Explicit consent for data processing
consent_options = {
    "necessary": True,  # Required (authentication)
    "analytics": user_choice,  # Optional
    "marketing": user_choice,  # Optional
    "personalization": user_choice  # Optional
}

if user.consent.analytics == False:
    # Don't track user behavior for analytics
    skip_analytics_event()
```

**5. Data Retention Policies:**
```python
# Auto-delete old data
retention_policies = {
    "watch_history": "2 years",
    "search_history": "18 months",
    "deleted_user_data": "30 days",  # Keep for 30 days in case of account recovery
    "logs": "90 days"
}

# Nightly job
def cleanup_old_data():
    db.delete_where("watch_history", "created_at < NOW() - INTERVAL '2 years'")
    db.delete_where("search_history", "created_at < NOW() - INTERVAL '18 months'")
```

**6. Encryption & Security:**
```python
# Encrypt PII at rest
encrypted_email = encrypt(user.email, encryption_key)
db.save_user({"email": encrypted_email})

# Encrypt in transit
use_https_only = True
tls_version = "TLSv1.3"
```

**7. Geographic Data Residency:**
```python
# EU users' data must be stored in EU
if user.country in EU_COUNTRIES:
    database_region = "eu-west-1"
elif user.country in US_COUNTRIES:
    database_region = "us-east-1"

# Compliance: Data doesn't cross borders unless necessary
```

**Trade-offs:**
- **Privacy vs Personalization:** Less data = worse recommendations
- **Compliance vs Performance:** Encryption adds 5-10ms latency
- **Cost:** GDPR compliance team, legal reviews, infrastructure changes ($1M+/year)

**Real-World Example:**
In 2021, WhatsApp faced $266M GDPR fine for unclear privacy policy. In 2023, Meta (Facebook) fined $1.3B for transferring EU user data to US servers.

**Technologies:**
- **Encryption:** AWS KMS, HashiCorp Vault
- **Consent Management:** OneTrust, Cookiebot
- **Data Discovery:** BigID, Immuta (find PII in databases)
- **Compliance Monitoring:** Vanta, Drata

---

### Q18: How would you implement A/B testing infrastructure?

**Answer:**
A/B testing allows YouTube to test new features with subset of users before full rollout.

**Architecture:**

**1. Experiment Configuration:**
```python
experiment = {
    "experiment_id": "rec_algo_v2",
    "name": "New Recommendation Algorithm",
    "variants": [
        {"id": "control", "weight": 0.5},    # 50% see old algorithm
        {"id": "treatment", "weight": 0.5}   # 50% see new algorithm
    ],
    "targeting": {
        "countries": ["US", "CA"],           # Only US/Canada users
        "platforms": ["android", "ios"],     # Only mobile
        "user_percentage": 0.1               # 10% of eligible users
    },
    "start_date": "2024-01-15",
    "end_date": "2024-01-30",
    "metrics": [
        "watch_time_per_user",
        "videos_per_session",
        "session_duration",
        "bounce_rate"
    ]
}
```

**2. User Assignment (Consistent Hashing):**
```python
def get_experiment_variant(user_id, experiment_id):
    # Deterministic assignment (same user always gets same variant)
    hash_value = hash(f"{user_id}:{experiment_id}")
    bucket = hash_value % 100

    # Example: 50/50 split
    if bucket < 50:
        return "control"
    else:
        return "treatment"

# User usr_123 always assigned to same variant
variant = get_experiment_variant("usr_123", "rec_algo_v2")
if variant == "treatment":
    use_new_recommendation_algorithm()
else:
    use_old_recommendation_algorithm()
```

**3. Event Tracking:**
```python
# Track all user actions with experiment context
analytics.track("video_watched", {
    "user_id": "usr_123",
    "video_id": "vid_456",
    "experiment_id": "rec_algo_v2",
    "variant": "treatment",
    "watch_duration": 450,
    "timestamp": "2024-01-15T10:30:00Z"
})
```

**4. Statistical Analysis:**
```python
# After 2 weeks, analyze results
def analyze_experiment(experiment_id):
    control_metrics = db.query("""
        SELECT
            AVG(watch_duration) as avg_watch_time,
            AVG(videos_per_session) as avg_videos_watched
        FROM analytics_events
        WHERE experiment_id = %s AND variant = 'control'
    """, experiment_id)

    treatment_metrics = db.query("""
        SELECT
            AVG(watch_duration) as avg_watch_time,
            AVG(videos_per_session) as avg_videos_watched
        FROM analytics_events
        WHERE experiment_id = %s AND variant = 'treatment'
    """, experiment_id)

    # Statistical significance test (t-test)
    p_value = stats.ttest_ind(control_metrics, treatment_metrics)

    if p_value < 0.05:
        print("Statistically significant difference!")
        if treatment_metrics > control_metrics:
            print("🎉 Treatment wins! Roll out to 100%")
        else:
            print("❌ Treatment loses. Keep control.")
    else:
        print("No significant difference. Inconclusive.")
```

**5. Feature Flags:**
```python
# Gradual rollout after successful experiment
rollout_percentage = 0.1  # Start with 10%

if is_feature_enabled("new_rec_algo", user_id, rollout_percentage):
    use_new_recommendation_algorithm()
else:
    use_old_recommendation_algorithm()

# Increase rollout: 10% → 25% → 50% → 100%
# If any issues, instant rollback by setting rollout_percentage = 0
```

**Trade-offs:**
- **Sample Size vs Speed:** Large sample (1M users) gives significance faster, but risks if feature is broken
- **Duration vs Confidence:** Longer experiments (4+ weeks) more accurate, but slow development
- **Metrics:** Optimizing for watch time may hurt user satisfaction

**Real-World Example:**
Netflix A/B tested different thumbnail images for each movie. Found that changing thumbnails increased click-through rate by 30%! Now every user sees personalized thumbnails.

**Technologies:**
- **Experimentation Platform:** Optimizely, LaunchDarkly, Split.io
- **Analytics:** Amplitude, Mixpanel
- **Statistical Analysis:** Python (scipy, statsmodels), R
- **Feature Flags:** LaunchDarkly, Unleash, ConfigCat

**Example A/B Tests at YouTube:**
1. Autoplay next video (resulted in 10% more watch time)
2. Red notification bell (increased subscriptions by 15%)
3. Recommended videos on right sidebar (now standard feature)

---

### Q19: How would you design the system for mobile clients with poor network connectivity?

**Answer:**
Mobile users often have limited bandwidth, high latency, and intermittent connectivity.

**Optimizations:**

**1. Adaptive Bitrate Streaming (ABR):**
```python
# Client-side logic
network_speed = measure_network_speed()  # Mbps

if network_speed > 5:
    quality = "1080p"  # High quality
elif network_speed > 2:
    quality = "720p"   # Medium quality
elif network_speed > 1:
    quality = "480p"   # Low quality
else:
    quality = "360p"   # Very low quality

# Request appropriate quality from CDN
request_video_stream(video_id, quality)

# Dynamically adjust during playback
if buffer_level < 5_seconds:
    downgrade_quality()  # Prevent buffering
elif buffer_level > 30_seconds and network_speed_increased:
    upgrade_quality()    # Better experience
```

**2. Offline Download:**
```python
# Download video for offline viewing
POST /videos/{video_id}/download

# Server generates time-limited download token
download_token = generate_token(video_id, expires_in=24_hours)

# Client downloads video + metadata
download(f"https://cdn.youtube.com/download/{video_id}?token={download_token}")

# Store encrypted on device (DRM protection)
save_encrypted_video(video_id, encryption_key)

# When offline, play from local storage
if is_offline():
    play_from_local_storage(video_id)
else:
    play_from_cdn(video_id)
```

**3. Prefetching:**
```python
# Preload next video in queue (while user watches current video)
current_video = "vid_123"
next_video = get_next_in_queue()

# Prefetch first 10 seconds of next video
prefetch_video_segments(next_video, segments=5)

# When user clicks "next", playback starts instantly (no buffering)
```

**4. Delta Sync (Minimize Data Transfer):**
```python
# Client requests only new data since last sync
last_sync_timestamp = "2024-01-15T10:00:00Z"

GET /feed?since=2024-01-15T10:00:00Z

# Server returns only new videos (not entire feed)
{
    "new_videos": [...],  # Only videos uploaded since last sync
    "sync_timestamp": "2024-01-15T12:00:00Z"
}

# Saves bandwidth vs sending entire feed every time
```

**5. Image Compression:**
```python
# Serve smaller thumbnails for mobile
desktop: 1280×720 (200 KB)
mobile: 640×360 (50 KB)  # 4x smaller!

# Use WebP format (30% smaller than JPEG)
thumbnail.webp instead of thumbnail.jpg

# Lazy load images (only load visible thumbnails)
if thumbnail.is_visible():
    load_image()
```

**6. API Response Compression:**
```python
# Gzip compression
Request Header: Accept-Encoding: gzip
Response Header: Content-Encoding: gzip

# 100 KB JSON → 20 KB gzipped (5x smaller)
```

**7. Request Batching:**
```python
# Instead of 10 separate API calls:
GET /videos/vid_1
GET /videos/vid_2
...
GET /videos/vid_10

# Batch into single request:
POST /videos/batch
{
    "video_ids": ["vid_1", "vid_2", ..., "vid_10"]
}

# Reduces latency (1 network round-trip instead of 10)
```

**8. Progressive Image Loading:**
```python
# Low-quality image loads first (5 KB), then high-quality (200 KB)
<img src="thumbnail_lowres.jpg" />  # Fast load
<img src="thumbnail_highres.jpg" />  # Loads in background
```

**Trade-offs:**
- **Quality vs Bandwidth:** Lower quality saves bandwidth but worse UX
- **Storage vs Convenience:** Offline downloads use device storage
- **Complexity:** Adaptive streaming adds client-side complexity

**Real-World Example:**
YouTube Go (now discontinued) was designed for India market with poor connectivity:
- Download videos in advance over WiFi
- Share videos peer-to-peer via Bluetooth (no internet needed)
- Preview videos (3-second clip) before watching full video

**Technologies:**
- **ABR:** HLS, DASH (built-in adaptive streaming)
- **Compression:** Gzip, Brotli (HTTP compression)
- **Image Optimization:** WebP, AVIF formats
- **Caching:** Service Workers (PWA), Cache API

---

### Q20: How would you monitor and debug YouTube's distributed system?

**Answer:**
Distributed systems are complex - issues span multiple services, databases, and servers.

**Monitoring Stack:**

**1. Metrics (What is happening?):**
```python
# Prometheus metrics
video_upload_duration_seconds = Histogram("video_upload_duration_seconds")
video_upload_errors_total = Counter("video_upload_errors_total")
active_users_gauge = Gauge("active_users_count")

# Record metrics
@metrics.track_duration(video_upload_duration_seconds)
def upload_video():
    try:
        # Upload logic
        pass
    except Exception as e:
        video_upload_errors_total.inc()
        raise

# Query metrics (Grafana dashboards)
rate(video_upload_errors_total[5m])  # Error rate in last 5 minutes
histogram_quantile(0.95, video_upload_duration_seconds)  # p95 latency
```

**2. Logs (What happened in detail?):**
```python
import logging

logger = logging.getLogger("youtube.upload_service")

# Structured logging (JSON format)
logger.info("Video upload started", extra={
    "video_id": "vid_123",
    "user_id": "usr_456",
    "file_size": 524288000,
    "trace_id": "trace_abc123"  # For distributed tracing
})

# Centralized logging (ELK stack)
# All logs sent to Elasticsearch
# Searchable via Kibana: "Show all errors for user_id=usr_456 in last hour"
```

**3. Distributed Tracing (How did request flow through services?):**
```python
# OpenTelemetry tracing
from opentelemetry import trace

tracer = trace.get_tracer("youtube.upload_service")

with tracer.start_as_current_span("upload_video") as span:
    span.set_attribute("video_id", "vid_123")
    span.set_attribute("user_id", "usr_456")

    # Call downstream services (traces linked)
    with tracer.start_as_current_span("upload_to_s3"):
        s3.upload(file)

    with tracer.start_as_current_span("publish_kafka_event"):
        kafka.publish("video.uploaded", event)

# Jaeger UI shows full trace:
# API Gateway (10ms) → Upload Service (200ms) → S3 (150ms) + Kafka (5ms)
# Identifies bottlenecks (S3 upload is slow!)
```

**4. Alerting:**
```yaml
# Prometheus alerts
alerts:
  - name: HighErrorRate
    expr: rate(video_upload_errors_total[5m]) > 0.05  # >5% errors
    for: 5m
    severity: critical
    actions:
      - pagerduty: oncall-team
      - slack: #incidents

  - name: HighLatency
    expr: histogram_quantile(0.95, video_upload_duration_seconds) > 10  # p95 > 10s
    for: 10m
    severity: warning
    actions:
      - slack: #alerts
```

**5. Health Checks:**
```python
# Kubernetes liveness probe
@app.route("/health/live")
def liveness():
    return {"status": "ok"}  # Service is running

# Kubernetes readiness probe
@app.route("/health/ready")
def readiness():
    # Check dependencies
    if not db.is_connected():
        return {"status": "not_ready"}, 503
    if not kafka.is_connected():
        return {"status": "not_ready"}, 503
    return {"status": "ready"}
```

**6. Error Tracking:**
```python
import sentry_sdk

sentry_sdk.init(dsn="https://...")

# Automatically capture exceptions
try:
    process_video()
except Exception as e:
    sentry_sdk.capture_exception(e)
    # Sentry shows:
    # - Stack trace
    # - User context
    # - Breadcrumbs (recent actions)
    # - Release version
    raise
```

**7. Chaos Engineering:**
```python
# Test resilience by injecting failures
# Chaos Monkey: Randomly kill 10% of servers
# Chaos Kong: Kill entire AWS region

# If system survives chaos tests → Confident in production
```

**Trade-offs:**
- **Observability vs Cost:** Detailed logging/tracing expensive (1TB+ logs/day)
- **Alerting Fatigue:** Too many alerts → ignored, too few → miss incidents
- **Sampling:** Trace 1% of requests (vs 100%) to reduce overhead

**Real-World Example:**
In 2020, Cloudflare outage took down 50% of internet. Root cause: Bad regex in WAF rule caused CPU spike. Monitoring detected spike in 2 minutes, rollback completed in 27 minutes.

**Technologies:**
- **Metrics:** Prometheus, Datadog, New Relic
- **Logging:** ELK Stack (Elasticsearch, Logstash, Kibana), Splunk
- **Tracing:** Jaeger, Zipkin, AWS X-Ray
- **Alerting:** PagerDuty, Opsgenie, AlertManager
- **Error Tracking:** Sentry, Rollbar, Bugsnag
- **Dashboards:** Grafana, Datadog, Kibana

---

## Design Summary

### Key Design Decisions Recap

**1. Storage Architecture:**
- **Video Storage:** AWS S3 / Google Cloud Storage (object storage, 11 nines durability)
- **Metadata:** PostgreSQL (relational data, ACID transactions)
- **Search:** Elasticsearch (full-text search, autocomplete)
- **Comments:** MongoDB (nested documents, flexible schema)
- **Analytics:** Cassandra (time-series data, high write throughput)
- **Cache:** Redis (sub-millisecond latency, 95%+ hit rate)

**Rationale:** Different data has different access patterns - use specialized databases for each use case (polyglot persistence).

**2. Content Delivery:**
- **CDN:** Google Cloud CDN with 420+ edge locations worldwide
- **Cache Strategy:** Popular videos (80/20 rule) cached at edge, long-tail served from origin
- **Protocol:** HLS/DASH for adaptive bitrate streaming

**Rationale:** 470 PB/day bandwidth - CDN reduces latency (200ms → 5ms) and cost (78% savings).

**3. Video Processing:**
- **Transcoding:** Async processing via Kafka + 24,000 workers (FFmpeg)
- **Resolutions:** 7 qualities (240p to 4K)
- **Compute:** Spot instances (70% cost savings)

**Rationale:** Real-time transcoding is impossible (10 min video = 2+ hours processing). Async processing decouples upload from playback.

**4. Database Scaling:**
- **Sharding:** Hash-based sharding on video_id (4-16 shards)
- **Replication:** 1 primary + 2 read replicas per shard (3x read capacity)
- **Partitioning:** Monthly partitions for videos table (query optimization)

**Rationale:** Single PostgreSQL instance maxes out at ~10k QPS. Sharding + replication → 480k QPS.

**5. Consistency Model:**
- **Strong Consistency (CP):** User auth, subscriptions, payments
- **Eventual Consistency (AP):** View counts, comments, recommendations

**Rationale:** Availability is critical for UX. Slight staleness in view counts is acceptable, but wrong auth is not.

**6. Scalability:**
- **Horizontal Scaling:** Auto-scale from 10 → 1000 servers based on CPU
- **Load Balancing:** NGINX/AWS ALB distributes traffic
- **Microservices:** 10 independent services, scale each individually

**Rationale:** Handle traffic spikes (viral videos) without manual intervention.

---

### Technologies Chosen and Why

| Component | Technology | Why? |
|-----------|-----------|------|
| **Video Storage** | AWS S3, GCS | Unlimited scalability, 11 nines durability, $0.023/GB |
| **Metadata DB** | PostgreSQL | ACID transactions, mature ecosystem, JSON support |
| **Search** | Elasticsearch | Full-text search, autocomplete, 50ms query time |
| **Analytics DB** | Cassandra | High write throughput (100k events/sec), time-series optimized |
| **Cache** | Redis | Sub-millisecond latency, 95%+ hit rate |
| **Message Queue** | Apache Kafka | 1M+ messages/sec, durable, replayable |
| **CDN** | Google Cloud CDN | 420+ PoPs, 95% cache hit, $0.02/GB |
| **Transcoding** | FFmpeg | Open-source, supports all formats, battle-tested |
| **Orchestration** | Kubernetes | Auto-scaling, self-healing, 100k+ pods |
| **Monitoring** | Prometheus + Grafana | Open-source, powerful querying, beautiful dashboards |
| **Tracing** | Jaeger | OpenTelemetry compatible, distributed tracing |

---

### Architecture Evolution (Step 1 → Step 6)

**Step 1: Basic Architecture (MVP)**
```
Client → Load Balancer → App Server → PostgreSQL
                                    → S3 (raw videos)
```
- Single app server (monolith)
- Single database
- No caching, no CDN
- **Capacity:** 100 concurrent users

**Step 2: Add Caching**
```
Client → LB → App Server → Redis Cache → PostgreSQL
                        → S3
```
- Redis for metadata, user sessions
- 80%+ cache hit rate
- **Capacity:** 10k concurrent users

**Step 3: Database Scaling**
```
Client → LB → App Server → Redis
                        → PostgreSQL (Sharded: 4 shards × 3 replicas)
                        → S3
```
- Sharding for write scalability
- Read replicas for read scalability
- **Capacity:** 100k concurrent users

**Step 4: Add Message Queue**
```
Client → LB → App Server → Redis
                        → PostgreSQL
                        → S3
                        → Kafka → Transcode Workers (24k workers)
```
- Async transcoding via Kafka
- Decouple upload from processing
- **Capacity:** 500 uploads/sec

**Step 5: CDN & Object Storage**
```
Client → CDN (420 PoPs) → Origin (S3)
       → LB → App Server → Redis
                        → PostgreSQL
                        → Kafka → Workers
```
- CDN serves 95% of traffic
- Reduced origin load, lower latency
- **Capacity:** 87k req/sec (peak)

**Step 6: Microservices**
```
Client → CDN → API Gateway → Upload Service → PostgreSQL
                          → Streaming Service → Redis
                          → Search Service → Elasticsearch
                          → User Service → PostgreSQL
                          → Comment Service → MongoDB
                          → Analytics Service → Cassandra
                          → Recommendation Service → Redis + ML
```
- 10 independent services
- Technology diversity (Go, Java, Python, Node.js)
- Independent scaling and deployment
- **Capacity:** 200k+ req/sec (total across all services)

---

### Final System Capacity Numbers

| Metric | Capacity | Notes |
|--------|----------|-------|
| **Daily Active Users** | 500 million | 20% of 2.5B monthly users |
| **Video Views/Day** | 2.5 billion | 5 videos per user |
| **Peak Requests/Sec** | 200,000 | Across all services |
| **Video Uploads/Sec** | 50 | Peak: 100/sec |
| **Daily Storage Growth** | 25.8 TB | 4.3M videos × 6 GB |
| **Daily Bandwidth** | 470 PB | Egress (streaming) |
| **Database QPS** | 480,000 | 16 shards × 3 replicas × 10k QPS |
| **Cache Hit Rate** | 95%+ | Redis for metadata |
| **CDN Cache Hit Rate** | 95% | Popular videos at edge |
| **Transcoding Workers** | 24,000 | FFmpeg on spot instances |
| **Availability** | 99.99% | 52 minutes downtime/year |
| **Video Start Latency** | <1 second | P95 metric |

---

### Cost Breakdown (Annual Estimates)

| Category | Cost | % of Total |
|----------|------|-----------|
| **CDN Bandwidth** | $8 billion | 53% |
| **Storage** | $4 billion | 27% |
| **Compute** | $2 billion | 13% |
| **Database** | $500 million | 3% |
| **Monitoring & Tools** | $500 million | 3% |
| **Total** | **~$15 billion** | 100% |

**Key Cost Optimizations:**
- Storage tiering (S3 → Glacier): Save $1.2B/year
- CDN optimization (longer cache TTLs): Save $400M/year
- Spot instances for transcoding: Save $73M/year
- AV1 compression (future): Save $6B/year

---

## What to Draw First - Whiteboard Interview Checklist

When you have 45 minutes for a system design interview, here's the optimal order to tackle the YouTube design:

### First 5 Minutes: Requirements & Scope
☐ **Functional Requirements** (write on board):
  - Upload videos
  - Stream videos
  - Search videos
  - Comments & likes

☐ **Non-Functional Requirements** (write on board):
  - 500M daily active users
  - 2.5B video views/day
  - 99.99% availability
  - <1 second video start time

☐ **Out of Scope** (explicitly state):
  - Video editing
  - Live streaming (mention you can discuss if time permits)
  - Monetization

### Minutes 5-10: Capacity Estimation
☐ Draw a simple table on the board:
```
Traffic:
- DAU: 500M
- Views/day: 2.5B
- Peak QPS: 87k

Storage:
- Videos/day: 4.3M
- Daily growth: 25.8 TB

Bandwidth:
- Egress: 470 PB/day
```

### Minutes 10-15: High-Level Architecture (Most Important!)
☐ Draw this **exact diagram** (practice before interview!):

```
┌─────────┐
│  User   │
└────┬────┘
     │
     v
┌─────────────┐
│     CDN     │ ← Draw this FIRST (most important for video)
└─────┬───────┘
      │
      v
┌──────────────┐
│ Load Balancer│
└──────┬───────┘
       │
       v
┌──────────────┐      ┌──────────┐
│  App Server  │─────→│ Database │
└──────┬───────┘      └──────────┘
       │
       │ ┌──────────┐
       └→│  Cache   │
         └──────────┘

┌──────────────┐
│  S3 Storage  │ ← Videos stored here
└──────────────┘
```

☐ **Explain the flow:**
1. User requests video → CDN
2. CDN cache hit (95%) → stream from edge
3. CDN cache miss (5%) → fetch from S3, cache, then stream
4. Metadata (title, views) → App server → Cache → Database

### Minutes 15-25: Deep Dive #1 - Video Upload & Transcoding
☐ Draw upload flow:
```
User → App Server → S3 (presigned URL)
                  ↓
                Kafka
                  ↓
          Transcode Workers (24k)
                  ↓
              S3 (7 qualities)
                  ↓
                 CDN
```

☐ **Key points to mention:**
- Presigned URLs (direct S3 upload)
- Async processing (Kafka)
- Multiple resolutions (240p → 4K)
- Spot instances (cost optimization)

### Minutes 25-35: Deep Dive #2 - Database Design
☐ Draw sharding diagram:
```
Hash(video_id) % 4
     ↓
Shard 1: Primary + 2 Replicas
Shard 2: Primary + 2 Replicas
Shard 3: Primary + 2 Replicas
Shard 4: Primary + 2 Replicas
```

☐ **Mention polyglot persistence:**
- PostgreSQL: Users, videos (relational)
- MongoDB: Comments (nested)
- Elasticsearch: Search
- Cassandra: Analytics
- Redis: Cache

### Minutes 35-40: Additional Components (if time permits)
☐ **Microservices** (draw boxes):
```
┌────────┐ ┌──────────┐ ┌────────┐
│ Upload │ │ Streaming│ │ Search │
└────────┘ └──────────┘ └────────┘
┌────────┐ ┌──────────┐ ┌────────┐
│  User  │ │ Comment  │ │Analytics│
└────────┘ └──────────┘ └────────┘
```

☐ **Search** (mention Elasticsearch)
☐ **Recommendations** (mention ML/collaborative filtering)
☐ **Monitoring** (mention Prometheus + Grafana)

### Minutes 40-45: Trade-offs & Questions
☐ **Trade-offs to discuss:**
- Consistency vs Availability (eventual consistency for view counts)
- Cost vs Quality (storage tiering, AV1 compression)
- Latency vs Accuracy (caching metadata)

☐ **Ask clarifying questions:**
- "Should I discuss live streaming?"
- "Do you want me to deep dive into the recommendation algorithm?"
- "Should I cover disaster recovery?"

### Interview Tips:
1. **Start simple, add complexity:** Don't jump to microservices immediately
2. **Use numbers:** Quantify everything (QPS, storage, bandwidth)
3. **Think out loud:** Explain your reasoning
4. **Draw cleanly:** Use boxes, arrows, and labels
5. **Prioritize:** Focus on video storage/delivery (core of YouTube)
6. **Watch the clock:** Leave 5 minutes for questions

### Common Mistakes to Avoid:
❌ Starting with microservices (too complex for MVP)
❌ Ignoring CDN (most important component for video!)
❌ Not doing capacity estimation
❌ Drawing too detailed (zoom out, show high-level first)
❌ Not asking clarifying questions

---

## Key Takeaways for Beginners

### 1. Start Simple, Scale Incrementally
- **Don't design for 1 billion users on day 1!**
- MVP: Load balancer + App server + Database + S3
- Add complexity as you encounter bottlenecks
- YouTube itself started as a simple PHP monolith in 2005

### 2. Use the Right Tool for the Job
- **Not everything needs to be in one database**
- PostgreSQL for relational data (users, videos)
- MongoDB for nested data (comments)
- Elasticsearch for search
- Cassandra for analytics
- Redis for caching
- This is called **polyglot persistence**

### 3. CDN is Critical for Video
- **Videos are huge** (200 MB per 10-minute video)
- Without CDN: Latency is 200ms+, bandwidth costs are 5x higher
- With CDN: Latency is 5ms, 95% of traffic served from edge
- **Golden Rule:** If you're streaming large files, use a CDN!

### 4. Async Processing for Heavy Jobs
- **Transcoding takes hours** - user can't wait
- Use message queue (Kafka) to decouple upload from processing
- User gets immediate confirmation, processing happens in background
- Applies to: Email sending, image processing, report generation, etc.

### 5. Cache Aggressively
- **80/20 rule:** 20% of videos get 80% of views
- Cache popular content in Redis (metadata) and CDN (videos)
- Target: 90%+ cache hit rate
- **Every cache hit = saved database query + lower latency**

### 6. Eventual Consistency is OK (Sometimes)
- **Strong consistency:** User login, payments (must be exact)
- **Eventual consistency:** View counts, recommendations (5-second delay is fine)
- Trading consistency for availability improves user experience
- Example: Twitter shows different follower counts when you refresh - that's OK!

### 7. Scale Horizontally, Not Vertically
- **Vertical scaling:** Buy bigger server (limited by hardware, expensive)
- **Horizontal scaling:** Add more servers (unlimited, cost-effective)
- YouTube has 10k+ servers, not 1 giant server
- Use load balancers to distribute traffic

### 8. Monitoring is Not Optional
- **If you can't measure it, you can't improve it**
- Track: Error rate, latency (p50, p95, p99), throughput, CPU/memory
- Set alerts: Page on-call engineer if error rate > 1%
- Use: Prometheus (metrics), ELK (logs), Jaeger (tracing)

### 9. Design for Failure
- **Servers crash, networks partition, databases fail**
- Multi-region deployment (if US-East fails, failover to US-West)
- Database replication (if primary dies, promote replica)
- Circuit breakers (if service is down, fail fast instead of retrying forever)

### 10. Cost Optimization Matters at Scale
- **At YouTube's scale, 1% cost reduction = $150M/year**
- Use spot instances (70% cheaper for batch jobs)
- Storage tiering (move old videos to Glacier - 96% cheaper)
- CDN optimization (longer cache TTLs reduce origin load)
- AV1 compression (50% smaller files = 50% bandwidth savings)

### 11. Security is Everyone's Responsibility
- **Encrypt everything:** TLS in transit, AES-256 at rest
- **Least privilege:** Services should only have minimum permissions needed
- **Rate limiting:** Prevent abuse (1000 requests/min per user)
- **Input validation:** Prevent SQL injection, XSS attacks
- GDPR compliance: Give users control over their data

### 12. Learn by Numbers
**Memorize these latency numbers:**
- L1 cache: 0.5 ns
- RAM: 100 ns
- SSD: 100 μs
- Network within datacenter: 0.5 ms
- Network across regions: 50-100 ms
- Disk seek: 10 ms
- **Takeaway:** Network calls are 10,000x slower than RAM - cache aggressively!

**Memorize these capacity numbers:**
- 1 TB HDD: $20
- 1 Gbps network: $0.01/GB egress
- 1 vCPU server: $50/month
- PostgreSQL: 10k QPS per instance
- Redis: 100k QPS per instance
- **Takeaway:** Cache is 10x faster and cheaper than database queries

### 13. Read/Write Ratio Matters
- **YouTube:** 600:1 (reads vastly outnumber writes)
- Optimize for reads: caching, read replicas, CDN
- **Twitter:** 1000:1 (most users read tweets, few post)
- **Banking:** 10:1 (more balanced, different optimization strategy)
- **Takeaway:** Know your workload before designing!

### 14. Communication is Key in Interviews
- **Think out loud:** Explain your reasoning
- **Ask questions:** "Should I focus on upload or streaming?"
- **Discuss trade-offs:** "Strong consistency is slower but more accurate"
- **Use examples:** "Similar to how Netflix does X"
- **Draw diagrams:** Visual communication is powerful
- **Interviewers want to see how you think, not just the final answer!**

### 15. Practice, Practice, Practice
- **You won't nail system design on first try**
- Practice drawing architectures on paper/whiteboard
- Time yourself (45 minutes)
- Review real-world architectures:
  - Netflix Tech Blog
  - Uber Engineering Blog
  - AWS Architecture Blog
- Do mock interviews with friends
- **Consistent practice beats cramming!**

---

## Additional Resources

### Must-Read Papers
- **Google File System (GFS):** How Google stores exabytes of data
- **Bigtable:** Google's NoSQL database (inspired Cassandra, HBase)
- **MapReduce:** Distributed data processing (inspired Hadoop, Spark)
- **Dynamo:** Amazon's highly available key-value store

### Recommended Books
- **"Designing Data-Intensive Applications" by Martin Kleppmann:** Best book on system design
- **"System Design Interview" by Alex Xu:** Interview-focused, great diagrams
- **"Database Internals" by Alex Petrov:** Deep dive into database architecture

### YouTube Channels
- **ByteByteGo:** System design concepts explained visually
- **Tech Dummies:** Simplified explanations for beginners
- **Gaurav Sen:** Popular system design channel in India

### Websites
- **High Scalability:** Real-world architectures (highscalability.com)
- **System Design Primer (GitHub):** Comprehensive resource (github.com/donnemartin/system-design-primer)
- **ByteByteGo Newsletter:** Weekly system design insights

---

## Final Words

Designing YouTube is **hard** - don't expect to master it overnight!

**Key mindset shifts for beginners:**

1. **There is no "perfect" design** - every decision is a trade-off
2. **Scale changes everything** - what works for 1000 users breaks at 1M users
3. **Start simple** - MVP first, then optimize bottlenecks
4. **Learn from real systems** - read engineering blogs from Netflix, Uber, Airbnb
5. **Practice consistently** - 1 hour/day for 3 months > 8 hours/day for 1 week

**Remember:** Every senior engineer was once a beginner struggling with the same concepts. The difference is they kept practicing, kept learning, and kept building.

You've got this! 🚀

---

**Document End**

Total Lines: ~780 lines
