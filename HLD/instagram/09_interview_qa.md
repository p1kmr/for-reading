# Instagram HLD - Interview Q&A

> **Purpose**: Practice answering common system design interview questions with real examples from Instagram architecture.

---

## Core Architecture Questions

### Q1: How would you design Instagram's feed generation system?

**Answer**:

I would use a **hybrid fanout approach** combining fanout-on-write and fanout-on-read:

**For regular users (< 1M followers)**:
- Fanout-on-write: When user posts, immediately add post to all followers' pre-computed feeds (stored in Redis sorted sets)
- Benefit: Feed loading is instant (< 10ms) - just read from Redis
- Trade-off: Writes are slower, but acceptable for regular users

**For celebrities (> 1M followers)**:
- Skip fanout (don't write to 100M feeds!)
- Fanout-on-read: When follower opens feed, merge pre-computed feed with recent celebrity posts (query on-demand)
- Benefit: Fast writes, slightly slower reads (still < 100ms)

**Feed ranking**: Apply ML model to rank posts by predicted engagement (user affinity, post quality, recency)

**Data structures**:
- Redis sorted set: `feed:user:{user_id}` (score = timestamp)
- Keep top 1000 posts per user (trim older posts)
- TTL: 24 hours (regenerate daily)

---

### Q2: How do you handle database scaling for billions of posts?

**Answer**:

**Sharding strategy**:
- Shard by `user_id` (not `post_id`!) using hash-based sharding
- Formula: `shard_id = user_id % num_shards`
- Reason: All user's posts on same shard → fast user timeline query

**Shard configuration**:
- 50 shards (each handles ~20M users)
- Master-slave replication: 1 master + 4 replicas per shard
- Total: 250 database servers (50 masters + 200 replicas)

**Read scaling**:
- Direct reads to replicas (4 replicas per shard = 4x read capacity)
- Total read capacity: 200 replicas × 5K req/sec = 1M req/sec

**Write scaling**:
- All writes go to master
- Total write capacity: 50 masters × 2K req/sec = 100K req/sec

**Cross-shard queries**: Use scatter-gather for feed (query multiple shards in parallel), but prefer pre-computed feeds to avoid this.

**Tools**: Vitess for shard routing, Orchestrator for automatic failover.

---

### Q3: Why use Redis cache? What do you cache and how do you invalidate?

**Answer**:

**Why Redis**:
- Instagram is 500:1 read:write ratio → optimize reads
- Database query: 150ms, Redis query: 5ms → **30x faster**
- Database load reduction: 90% of reads served from cache

**What to cache**:
1. User feeds (highest priority): Pre-computed top 1000 posts, TTL 30s
2. User profiles: Username, bio, follower count, TTL 5 min
3. Post metadata: Likes count, comments count, TTL 1 min
4. Like status: Has user liked this post? TTL 10 min
5. Media URLs: CDN paths to images/videos, TTL 1 hour

**Cache invalidation strategies**:
1. **TTL (Time-To-Live)**: Automatic expiry (30s for feeds, 5min for profiles)
2. **Explicit invalidation**: Delete cache when data changes
   - User posts → Delete user's feed + all followers' feeds
   - User follows → Delete follower's feed
   - User likes → Delete like status cache
3. **Hybrid (recommended)**: TTL as safety net + explicit invalidation for immediate consistency

**Implementation**: Redis Cluster with 16 shards (8 TB capacity), hash slot distribution for even load.

---

### Q4: How do you ensure high availability (99.99% uptime)?

**Answer**:

**Eliminate single points of failure**:
1. **Load balancers**: Multiple LBs with health checks (AWS ALB auto-scales)
2. **App servers**: 100+ servers across 3 availability zones (AZ-1, AZ-2, AZ-3)
3. **Databases**: Master-slave replication + automatic failover (Orchestrator)
4. **Redis**: Cluster mode with replicas (1 master + 2 replicas per shard)
5. **Kafka**: 3 brokers with replication factor 3

**Multi-AZ deployment**:
- Spread servers across 3 data centers (availability zones)
- If one AZ fails (power outage, network issue) → other 2 AZs handle 100% traffic
- Load balancer automatically routes away from unhealthy AZ

**Health checks**:
- Load balancer checks `/health` endpoint every 30 seconds
- 2 consecutive failures → remove server from pool
- Server recovers → auto-added back

**Circuit breaker pattern**:
- If database is slow/down, fail fast (don't wait)
- Return cached data or error immediately
- Prevents cascade failures (all servers waiting for DB)

**Graceful degradation**:
- Critical services (feed, upload) always work
- Non-critical services (search) can degrade or fail
- Example: Search down → show cached results or disable feature

**Result**: 99.99% uptime = 52 minutes downtime/year (includes deployments, maintenance).

---

### Q5: How do you handle media storage (photos/videos)?

**Answer**:

**Object storage (AWS S3)** instead of file system because:
- **Unlimited capacity**: File system limited to ~10 TB/server, S3 unlimited
- **Durability**: 11 nines (99.999999999%) - virtually no data loss
- **Availability**: 99.99% uptime
- **Cost-effective**: $0.023/GB/month (cheaper than EBS/NAS)

**Bucket strategy**:
1. `original-media`: User uploads (Glacier after 90 days for cost savings)
2. `optimized-media`: Resized versions (thumbnail 150×150, medium 640×640, full 1080×1080)
3. `videos-encoded`: Transcoded videos (360p, 720p, 1080p)

**Upload flow**:
1. User uploads → Save original to S3
2. Publish event to Kafka (`photo.uploaded`)
3. Background worker resizes → Upload to `optimized-media`
4. Update database with URLs
5. Total time: 550ms upload + 3-5s background processing (async)

**Content Delivery Network (CDN)**:
- CloudFront CDN caches media at 200+ edge locations worldwide
- User in Tokyo → Served from Tokyo edge (10ms) instead of US origin (200ms)
- Cache hit rate: 95% → Only 5% requests hit S3
- Cost savings: 74% cheaper than serving from S3 ($17.7M vs $67.7M/month)

**Lifecycle policies**:
- Hot storage (< 30 days): S3 Standard ($0.023/GB/month)
- Warm storage (30-90 days): S3 Infrequent Access ($0.0125/GB/month)
- Cold storage (> 90 days): S3 Glacier ($0.004/GB/month)

---

## Scalability Questions

### Q6: Your system suddenly gets 10x traffic. How do you handle it?

**Answer**:

**Immediate actions (within minutes)**:

1. **Auto-scaling**:
   - App servers: Auto-scaling group adds 10x servers (50 → 500)
   - CloudFront CDN: Automatically handles surge (managed service)
   - AWS ALB: Auto-scales load balancers

2. **Leverage existing capacity**:
   - Redis: Already provisioned for 1.6M req/sec (current: 578K) → 2.7x headroom
   - PostgreSQL replicas: Add read replicas (can add 100+ in 15 minutes)
   - Kafka: Already handles 300K msg/sec (current: 1.2K) → 250x headroom

3. **Rate limiting**:
   - Enforce strict limits: Max 100 posts/hour per user
   - Block abusive IPs

**Medium-term (hours to days)**:

4. **Database scaling**:
   - Add more read replicas (vertical scale replicas if needed)
   - Increase cache TTL to reduce DB load (30s → 5min)

5. **Optimize expensive operations**:
   - Defer non-critical tasks (analytics, recommendations)
   - Reduce feed size (20 posts → 10 posts initially)

**Long-term (weeks)**:

6. **Add more shards**: If write capacity saturated, add database shards
7. **Optimize code**: Profile slow endpoints, add more caching
8. **Infrastructure**: Provision more baseline capacity

**What NOT to do**:
- ❌ Don't panic-delete data
- ❌ Don't turn off features completely (graceful degrade instead)
- ❌ Don't vertical scale (buy bigger servers) - won't scale to 10x

---

### Q7: How do you handle a celebrity with 100 million followers posting?

**Answer**:

**Problem**: Fanout-on-write for 100M followers = 100M Redis writes! (minutes to complete)

**Solution**: Hybrid fanout

1. **Mark as celebrity**: Users with > 1M followers flagged as `is_celebrity = true`

2. **Skip fanout**: Don't write post to 100M follower feeds

3. **Fanout-on-read** for celebrity content:
   - When follower opens feed:
     a) Fetch pre-computed feed (non-celebrity posts)
     b) Fetch recent posts from celebrities user follows (last 48h)
     c) Merge + rank + return top 20

4. **Cache celebrity posts separately**:
   - `celebrity_posts:user:{celebrity_id}` → sorted set of last 100 posts
   - TTL: 1 hour (long TTL since many followers will access)

**Performance**:
- Celebrity posts: ~30ms additional latency (1 extra DB query)
- Total feed load: 15ms (cache) + 30ms (celebrity posts) = 45ms ✅ (still under 100ms target)

**Write performance**:
- Celebrity posts: 500ms (no fanout!) ✅
- vs. 10+ minutes for 100M fanout ✅

**Benefits**:
- ✅ Fast writes for celebrities
- ✅ Acceptable read latency for followers
- ✅ System doesn't crash under load

---

## Database Questions

### Q8: SQL vs NoSQL for Instagram?

**Answer**:

**I would choose SQL (PostgreSQL)** for Instagram:

| Factor | SQL (PostgreSQL) ✅ | NoSQL (MongoDB) |
|--------|---------------------|-----------------|
| **Data relationships** | Native (foreign keys, JOINs) | Manual (denormalization, duplication) |
| **ACID transactions** | Yes (critical for follows, likes) | Limited (eventual consistency) |
| **Consistency** | Strong consistency | Eventual consistency |
| **Query flexibility** | Complex queries, aggregations | Limited joins, no aggregations |
| **Schema changes** | Harder (migrations) | Easier (schema-less) |

**Why PostgreSQL**:
1. **Relational data**: Users → Posts → Comments → Likes (clear relationships)
2. **ACID needed**: Follow/unfollow must be immediately consistent
3. **Complex queries**: Feed generation requires JOINs (posts + users + likes)
4. **PostgreSQL JSON support**: Get flexibility of NoSQL where needed
5. **Mature ecosystem**: Battle-tested at scale (Instagram actually uses PostgreSQL!)

**Scaling PostgreSQL**:
- Vertical: Single server up to 4TB RAM, 128 vCPUs
- Horizontal: Sharding (50 shards) + read replicas (4 per shard)
- Result: 100K writes/sec, 1M reads/sec ✅

**When to use NoSQL**:
- If data is document-based (no relationships)
- If eventual consistency is acceptable everywhere
- If need extreme write throughput (Cassandra: 1M writes/sec per node)

For Instagram, **relationships and consistency are critical** → SQL wins.

---

### Q9: How do you handle database failover?

**Answer**:

**Architecture**: Master-slave replication (1 master + 4 replicas per shard)

**Automatic failover (Orchestrator)**:

1. **Health monitoring**: Orchestrator pings master every 5 seconds

2. **Failure detection**: 3 consecutive ping failures (15 seconds) = master down

3. **Replica promotion**:
   - Choose replica with least replication lag (most up-to-date)
   - Promote to master (switch from read-only to read-write mode)

4. **DNS update**: Update DNS to point to new master
   - TTL: 30 seconds (clients pick up change within 30s)

5. **Reconfigure other replicas**: Point to new master for replication

6. **Notification**: Alert ops team via PagerDuty

**Timeline**:
- Detection: 15 seconds (3 failed pings)
- Promotion: 10 seconds (reconfigure replica)
- DNS propagation: 30 seconds
- **Total downtime**: ~1 minute per failover

**Data loss**: Minimal (asynchronous replication ~50ms lag, so up to 50ms of writes lost)

**Prevention**: For zero data loss, use semi-synchronous replication (1 replica confirms before write succeeds)

**Failback**: When original master recovers, leave new master in place (don't fail back automatically to avoid thrashing)

---

## Performance Questions

### Q10: How do you achieve < 100ms feed latency?

**Answer**:

**Breakdown of feed load**:
1. Authentication: 5ms (validate JWT token)
2. Cache lookup: 10ms (Redis GET feed:user:123)
3. Like status check: 5ms (parallel Redis GET for 20 posts)
4. Response serialization: 5ms
5. Network: 10ms
6. **Total: 35ms** ✅ (65% under target!)

**Optimizations applied**:

1. **Pre-computed feeds (fanout-on-write)**:
   - Don't query database for feed (150ms → 10ms)
   - Background workers maintain feeds asynchronously

2. **Redis cache everywhere**:
   - Feed data: 10ms (vs 150ms DB query)
   - Profile data: 5ms (vs 50ms DB query)
   - Like status: 5ms (vs 20ms DB query)

3. **Parallel queries**:
   - Check like status for all 20 posts simultaneously (Promise.all)
   - 20 × 5ms sequential = 100ms ❌
   - 20 × 5ms parallel = 5ms ✅

4. **Connection pooling**:
   - Reuse Redis/DB connections (avoid handshake overhead)
   - Connection pool: 100 connections pre-established

5. **Data denormalization**:
   - Store username + profile_pic in feed cache (avoid JOIN)
   - Trade-off: Redundant data, but 3x faster

6. **Edge caching (client-side)**:
   - Cache feed for 5 seconds on mobile app
   - Pull-to-refresh invalidates cache

**Monitoring**: p99 latency < 50ms (99% of requests under 50ms)

---

### Q11: How do you optimize for a global user base?

**Answer**:

**Challenge**: User in Tokyo accessing US servers = 200ms latency

**Solutions**:

1. **CDN (CloudFront)**:
   - 200+ edge locations worldwide
   - Media served from nearest edge (Tokyo user → Tokyo edge = 10ms)
   - Cache hit rate: 95% → only 5% go to origin

2. **Multi-region deployment** (future):
   - Primary: us-east-1 (serves Americas, Europe)
   - Secondary: ap-southeast-1 (serves Asia-Pacific)
   - Route users to nearest region via GeoDNS
   - Challenge: Data synchronization (eventual consistency)

3. **Database read replicas** in multiple regions:
   - Master in us-east-1 (all writes)
   - Read replicas in eu-west-1, ap-southeast-1
   - Reads served locally (faster for non-US users)
   - Replication lag: 100-500ms (acceptable for feeds)

4. **API Gateway** in each region:
   - AWS API Gateway edge-optimized endpoints
   - Terminate TLS at edge (faster handshake)

5. **Optimize payload size**:
   - Use WebP images (30% smaller than JPEG)
   - Gzip API responses (70% size reduction)
   - Lazy load images (load as user scrolls)

**Result**:
- US users: 35ms feed load ✅
- Europe users: 50ms ✅
- Asia users: 80ms (with single region) → 40ms (with multi-region) ✅

---

## Trade-offs & Design Decisions

### Q12: Fanout-on-write vs fanout-on-read - which is better?

**Answer**:

| Factor | Fanout-on-Write ✅ | Fanout-on-Read |
|--------|-------------------|----------------|
| **Read latency** | Very fast (10ms) | Slower (100-200ms) |
| **Write latency** | Slower (100-1000ms) | Fast (50ms) |
| **Storage** | High (each user's feed cached) | Low (compute on demand) |
| **Consistency** | Eventual (fanout takes time) | Immediate (query live data) |
| **Scalability** | Limited (celebrity problem) | Better (no fanout) |
| **Complexity** | Higher (background workers) | Lower (simple queries) |

**Instagram choice**: **Fanout-on-write with hybrid for celebrities**

**Reasoning**:
1. **Optimize for reads**: 500:1 read:write ratio → reads more important
2. **User experience**: Feed load must be < 100ms (can't query on-read)
3. **Write latency acceptable**: Users can wait 500ms for post to upload
4. **Storage cheap**: Redis cache costs $11K/month (acceptable)

**Hybrid approach** solves celebrity problem:
- Regular users: Fanout-on-write (fast reads)
- Celebrities: Fanout-on-read (avoid 100M writes)
- Best of both worlds ✅

---

### Q13: How do you handle eventual consistency in Instagram?

**Answer**:

**Where eventual consistency is acceptable**:
1. **Feed updates**: User posts → appears in follower's feed after 1-2 seconds ✅
2. **Like counts**: Displayed count might be off by 1-2 for few seconds ✅
3. **Follower counts**: Can be stale for up to 5 minutes ✅
4. **Search results**: New users appear in search after few seconds ✅

**Where strong consistency is required**:
1. **Authentication**: User logs in → must see own posts immediately ✅
2. **Follow/unfollow**: User follows someone → must see their posts in feed ✅
3. **User's own actions**: User likes post → ❤️ must turn red immediately ✅
4. **Payments** (if applicable): Transaction must be ACID ✅

**Implementation strategies**:

**Read-your-own-writes**:
```javascript
async function getUserFeed(userId, currentUserId) {
  if (userId === currentUserId) {
    // User viewing own feed → read from MASTER (strong consistency)
    return await masterDB.query(...);
  } else {
    // User viewing others' feed → read from REPLICA (eventual consistency OK)
    return await replicaDB.query(...);
  }
}
```

**Optimistic UI updates**:
```javascript
// User clicks like
async function likePost(postId) {
  // 1. Immediately update UI (optimistic)
  updateUIShowLiked(postId);

  // 2. Send request to server (async)
  try {
    await api.post(`/posts/${postId}/like`);
  } catch (err) {
    // 3. If fails, revert UI
    updateUIShowNotLiked(postId);
    showError('Failed to like post');
  }
}
```

**Cache invalidation**:
- Explicitly delete cache when data changes (don't wait for TTL)
- Example: User posts → delete all followers' feed caches immediately

**Result**: Users perceive strong consistency (UI updates instantly), while system benefits from eventual consistency (better performance).

---

## Architecture Evolution Questions

### Q14: How would you evolve this architecture for Stories feature?

**Answer**:

**Stories requirements**:
- Ephemeral content (disappears after 24 hours)
- Very high volume (users post multiple stories per day)
- Real-time views (must see viewers immediately)

**Architecture changes**:

1. **Separate storage**:
   - Stories table: `(story_id, user_id, media_url, created_at, expires_at)`
   - Partition by `created_at` (TTL partition drops after 24h)
   - No need to keep stories long-term (reduces storage)

2. **No database storage for media**:
   - Store directly in S3 with 24-hour lifecycle policy (auto-delete)
   - No need for "original" bucket (stories are disposable)

3. **Separate feed**:
   - `stories:feed:user:{userId}` → Redis sorted set (score = created_at)
   - TTL: 24 hours (auto-expire)
   - Fanout only to active followers (viewed app in last 24h)

4. **View tracking**:
   - Store viewers in Redis set: `story:{story_id}:viewers`
   - Persist to database every 5 minutes (batch writes)
   - Real-time view count from Redis

5. **Push notifications**:
   - When user posts story → notify followers via FCM/APNs
   - Only notify top 100 followers (most engagement) to limit push volume

**Infrastructure additions**:
- Separate Kafka topic: `story.created`
- Separate workers: Story processors (lighter than photo processing)
- Redis memory increase: +2 TB for story feeds

**Result**: Stories feature with minimal impact on core Instagram performance.

---

### Q15: How would you add real-time messaging (DMs)?

**Answer**:

**Messaging requirements**:
- Real-time delivery (< 1 second)
- Read receipts
- Typing indicators
- Message history (persistent)

**Architecture** (separate from main Instagram):

1. **WebSocket servers**:
   - Maintain persistent connections to clients
   - 1 million concurrent WebSocket connections per server
   - 100 servers = 100M concurrent connections

2. **Message database** (separate from main DB):
   - NoSQL (Cassandra) for high write throughput
   - Schema: `(conversation_id, message_id, sender_id, content, timestamp)`
   - Partition by `conversation_id` (all messages in conversation on same node)

3. **Presence service** (Redis):
   - Track online/offline status
   - `user:{user_id}:presence` → {status: 'online', last_seen: timestamp}
   - TTL: 5 minutes (if user disconnects, marked offline after 5 min)

4. **Message queue** (Kafka):
   - Topic: `messages.sent`
   - Workers process: Save to Cassandra, send push notifications

5. **Delivery flow**:
   ```
   User A sends message to User B:
   1. WebSocket server receives message
   2. Check if User B online (Redis presence)
   3. If online: Send via WebSocket (< 100ms) ✅
   4. If offline: Publish to Kafka → Worker sends push notification
   5. Save to Cassandra (async)
   ```

6. **Read receipts**:
   - User B reads message → Send event via WebSocket to User A
   - Update message status in Cassandra: `status = 'read'`

**Scaling**:
- WebSocket servers: Auto-scale based on connection count
- Cassandra: Add nodes for more write capacity
- Redis presence: Cluster mode (32 nodes)

**Result**: Real-time messaging with 99.9% delivery within 1 second.

---

## Monitoring & Debugging

### Q16: How would you debug slow feed loading?

**Answer**:

**Methodology**:

1. **Reproduce the issue**:
   - Which user? (user_id)
   - Which endpoint? (GET /api/v1/feed)
   - When? (timestamp)
   - How slow? (p50, p95, p99 latency)

2. **Check metrics** (Grafana dashboards):
   - Request latency: p99 > 1s? (normally 35ms)
   - Error rate: HTTP 500s increasing?
   - Cache hit rate: Dropped from 95% to 60%? ← Likely culprit!

3. **Distributed tracing** (Jaeger):
   ```
   Request trace for feed load:
   - Total: 1200ms ← Way too slow!
     - JWT validation: 5ms ✅
     - Redis GET: 1100ms ❌ ← Bottleneck!
     - Serialization: 95ms ⚠️
   ```

4. **Diagnose Redis slowness**:
   - Check Redis logs: `SLOWLOG GET 10`
   - Found: Large keys being returned (10 MB!)
   - Root cause: Feed cache grew too large (should be max 200 KB)

5. **Root cause**:
   - Background worker bug: Not trimming feeds (should keep only 1000 posts)
   - Feeds grew to 100,000 posts (100x expected!)

6. **Immediate fix**:
   - Truncate oversized feeds: `ZREMRANGEBYRANK feed:user:{id} 0 -1001`
   - Restart background workers with bug fix

7. **Verification**:
   - Latency drops: p99 = 1200ms → 40ms ✅
   - Cache hit rate recovers: 60% → 95% ✅

**Monitoring setup** to catch this early:
- Alert: Redis GET latency > 50ms for 5 minutes
- Alert: Cache hit rate < 80% for 10 minutes
- Alert: Feed key size > 500 KB

---

## Key Interview Tips

1. **Start with requirements**: Clarify functional/non-functional before designing
2. **Estimate capacity**: Calculate DAU, requests/sec, storage needs (show math!)
3. **Start simple**: Basic client-server-DB, then add components incrementally
4. **Justify decisions**: Explain WHY you chose PostgreSQL over MongoDB
5. **Discuss trade-offs**: Every decision has pros/cons - mention both
6. **Mention alternatives**: "I chose X, but Y is also valid if..."
7. **Draw diagrams**: Visualize architecture (whiteboard or mermaid)
8. **Handle failures**: Discuss what happens when DB goes down
9. **Monitoring**: Always mention metrics, alerts, debugging
10. **Be specific**: Don't say "use a database" - say "PostgreSQL sharded by user_id"

