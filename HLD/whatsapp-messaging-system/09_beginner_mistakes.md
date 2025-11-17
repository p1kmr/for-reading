# Common Beginner Mistakes in System Design

## Introduction

When designing large-scale systems like WhatsApp, beginners often make predictable mistakes. This guide shows the **wrong approach** (❌) and the **correct approach** (✅) with clear explanations.

---

## Mistake 1: Storing Everything in One Database

### ❌ Wrong Approach

```
All data in single PostgreSQL database:
- 2.5 billion users
- 100 billion messages/day
- 10 PB media files
- All on one server

Result:
- Database can't handle 694k writes/sec (limit: 10k/sec) FAIL! ❌
- Single point of failure
- Slow queries (TB of data in one table)
- Expensive vertical scaling (96-core server = $20k/month)
```

### ✅ Correct Approach

```
Horizontal scaling with sharding:
- 70 database shards (by user_id)
- Each shard handles 9,920 writes/sec ✓
- Each shard stores 157 TB (manageable)
- 4 read replicas per shard (read scalability)
- Media in S3 (not in database)

Result:
- Can handle 700k writes/sec ✓
- No single point of failure ✓
- Fast queries (smaller datasets per shard) ✓
- Cost-effective ($500/month per server × 350 servers = $175k/month)
```

**Why this matters**: Single database is beginner's #1 mistake. Always think horizontal scaling!

---

## Mistake 2: Using HTTP Polling for Real-Time Features

### ❌ Wrong Approach

```javascript
// Client polls every 2 seconds
setInterval(async () => {
  const newMessages = await fetch('/api/messages/new');
  if (newMessages.length > 0) {
    displayMessages(newMessages);
  }
}, 2000);

Problem:
- 500M users × 30 polls/minute = 250M requests/sec
- 95% of polls return empty (wasted bandwidth)
- 2-second latency (not real-time)
- Battery drain on mobile devices
```

### ✅ Correct Approach

```javascript
// WebSocket persistent connection
const ws = new WebSocket('wss://whatsapp.com/ws');

ws.onmessage = (event) => {
  const message = JSON.parse(event.data);
  displayMessage(message); // Instant delivery!
};

// Send message
ws.send(JSON.stringify({ to: 'Bob', text: 'Hello!' }));

Benefits:
- One connection, not 30 requests/minute
- Instant push (<100ms latency)
- Efficient (idle when no messages)
- Battery friendly
```

**Key learning**: HTTP polling = beginner mistake. Use WebSockets for real-time!

---

## Mistake 3: Not Considering Caching

### ❌ Wrong Approach

```javascript
// Every request hits database
async function getUserProfile(userId) {
  return await db.query('SELECT * FROM users WHERE user_id = ?', [userId]);
  // 100ms database query EVERY TIME
}

// User profile displayed on every message
// 1000 messages in chat = 1000 database queries = 100 seconds! ❌
```

### ✅ Correct Approach

```javascript
async function getUserProfile(userId) {
  // 1. Check cache first
  const cached = await redis.get(`user:${userId}`);
  if (cached) {
    return JSON.parse(cached); // 5ms cache hit ✓
  }

  // 2. Cache miss - query database
  const user = await db.query('SELECT * FROM users WHERE user_id = ?', [userId]);

  // 3. Store in cache for next time
  await redis.setex(`user:${userId}`, 86400, JSON.stringify(user));

  return user;
}

// First request: 100ms (database)
// Next 999 requests: 5ms each (cache)
// Total: 100ms + (999 × 5ms) = 5 seconds (20x faster!)
```

**Impact**:
- Database load reduced by 80%
- Latency reduced from 100ms to 5ms (20x)
- Can serve 20x more requests with same infrastructure

**Beginner mistake**: "Database is fast enough" - NO! Cache everything possible!

---

## Mistake 4: Storing Media in Database

### ❌ Wrong Approach

```sql
CREATE TABLE messages (
  message_id BIGINT PRIMARY KEY,
  sender_id BIGINT,
  receiver_id BIGINT,
  message_text TEXT,
  media_blob BYTEA -- ❌ Storing 5 MB image in database!
);

Problems:
- Database bloat (11 PB of images in Postgres)
- Slow queries (reading 5 MB blob = 500ms)
- Expensive ($0.10/GB vs S3 $0.023/GB = 4x more expensive)
- Difficult backups (backing up PBs from database = days)
```

### ✅ Correct Approach

```sql
-- Database: Store only metadata
CREATE TABLE media (
  media_id VARCHAR(50) PRIMARY KEY,
  user_id BIGINT,
  file_path VARCHAR(500), -- S3 key: "media/user_123/photo.jpg"
  file_type VARCHAR(50),
  file_size BIGINT,
  created_at TIMESTAMP
);

-- Actual media in S3
S3: whatsapp-media/media/user_123/photo.jpg (5 MB)

-- Messages table: Reference media
CREATE TABLE messages (
  message_id BIGINT PRIMARY KEY,
  sender_id BIGINT,
  receiver_id BIGINT,
  message_text TEXT,
  media_id VARCHAR(50) -- Foreign key to media table
);

Benefits:
- Database stays small (only metadata)
- Fast queries (no blobs to read)
- Cheap storage (S3 = $0.023/GB)
- Easy backups (S3 automatic snapshots)
- CDN integration (CloudFront caches S3 files)
```

**Rule**: Never store large binary data in SQL database!

---

## Mistake 5: Not Planning for Failures

### ❌ Wrong Approach

```
Architecture:
Client → Load Balancer → App Server → Database

What happens if:
- Load balancer crashes? → Entire system down ❌
- App server crashes? → Users disconnected ❌
- Database crashes? → All writes fail ❌

"It won't crash" - WRONG! Everything crashes eventually!
```

### ✅ Correct Approach

```
Architecture with redundancy:

Load Balancer:
- 3 load balancers in active-active mode
- If one fails, others continue

App Servers:
- 50,000 servers
- If 100 crash, 49,900 still serving
- Users reconnect automatically

Database:
- Each master has 4 read replicas
- If master crashes, replica promoted (30 seconds)
- Writes resume automatically

Multi-Region:
- Active in US-East
- Standby in US-West, EU, Asia
- If entire datacenter fails, failover to standby (70 seconds)
```

**Design for failure from day one!**

---

## Mistake 6: Over-Engineering Early

### ❌ Wrong Approach

```
Startup with 1000 users:
- Microservices (user service, message service, media service, ...)
- Kubernetes cluster (10 nodes)
- Database sharding (10 shards)
- Multi-region deployment
- Service mesh (Istio)

Result:
- Takes 6 months to build
- Complex to maintain
- 10x more expensive than needed
- No users yet to justify complexity!
```

### ✅ Correct Approach

```
Startup (0-100k users):
- Monolithic application (simple!)
- 3 servers behind load balancer
- Single database + 1 replica
- Redis cache (1 server)
- Deploy in 2 weeks, iterate quickly

Growth (100k-1M users):
- Split critical services (auth, messages)
- Add database sharding (5 shards)
- Multi-region if needed

Scale (1M+ users):
- Full microservices
- Database sharding (70 shards)
- CDN, Kafka, etc.

"Make it work, make it right, make it fast" - in that order!
```

**Premature optimization is the root of all evil!**

---

## Mistake 7: Ignoring Capacity Planning

### ❌ Wrong Approach

```
"We'll use AWS, it scales automatically"
"We'll worry about scale when we get there"

Interview:
Q: How many servers do you need?
A: "I don't know, AWS will handle it"

WRONG! ❌
```

### ✅ Correct Approach

```
Show capacity calculations:

Daily Active Users: 1.25 billion
Messages/day: 100 billion
Messages/sec (peak): 3.5 million

WebSocket servers:
- Concurrent connections: 500 million
- Per server: 10,000 connections
- Servers needed: 500M ÷ 10k = 50,000 servers ✓

Database servers:
- Write operations/sec: 694,444
- Per server: 10,000 writes/sec
- Shards needed: 694k ÷ 10k = 70 shards ✓

Storage:
- Text messages: 11 PB/year
- Media files: 3.65 EB/year
- Total: 3.66 EB/year ✓

Interviewer: Impressed! You know your numbers!
```

**Always do the math!**

---

## Mistake 8: Wrong Database Choice

### ❌ Wrong Approach

```
"MongoDB is web-scale, let's use it for everything!"

Messages in MongoDB:
- Eventual consistency
- Messages might appear out of order ❌
- No ACID transactions
- Complex joins difficult

Result: Users see "Hello!" before "Hi" (wrong order)
```

### ✅ Correct Approach

```
Choose database based on requirements:

For Messages: PostgreSQL ✓
- Strong consistency (messages in order)
- ACID transactions
- Complex joins (get all messages in group)

For Analytics: Cassandra ✓
- High write throughput
- Time-series data
- Eventual consistency OK

For Caching: Redis ✓
- In-memory speed
- Pub/sub for real-time
- Simple data structures

For Media: S3 ✓
- Object storage
- Cheap and durable
- CDN integration

Right tool for the right job!
```

---

## Mistake 9: Security as Afterthought

### ❌ Wrong Approach

```
"Let's build the feature first, add security later"

Result:
- Messages sent in plain text (anyone can read) ❌
- No authentication (anyone can send messages as you) ❌
- SQL injection vulnerabilities ❌
- DDoS attack takes down entire system ❌

Security breach = company destroyed!
```

### ✅ Correct Approach

```
Security from day one:

1. End-to-End Encryption
   - Messages encrypted on sender device
   - Server cannot read messages
   - Only recipient can decrypt

2. Authentication
   - JWT tokens for API requests
   - Phone number + OTP verification
   - Tokens expire after 7 days

3. Authorization
   - Users can only access their own messages
   - Verify sender_id matches authenticated user

4. Input Validation
   - Prevent SQL injection
   - Sanitize user input
   - Rate limiting (100 requests/minute per user)

5. Network Security
   - TLS 1.3 for all connections
   - VPC with private subnets
   - WAF (Web Application Firewall) for DDoS protection

Security = Feature #1!
```

---

## Mistake 10: Not Monitoring Production

### ❌ Wrong Approach

```
Deploy to production and hope for the best

User reports: "WhatsApp is down!"
You: "Let me check..."
[Logs into server, checks logs manually]
[Takes 30 minutes to find issue]

By then: 10 million users affected!
```

### ✅ Correct Approach

```
Comprehensive monitoring:

1. Metrics (Prometheus)
   - Messages/sec, latency, error rate
   - Database connections, query time
   - Cache hit ratio, memory usage
   - Alert if:
     - Error rate > 1%
     - Latency > 200ms
     - Database connections > 90%

2. Logging (ELK Stack)
   - Centralized logs from all servers
   - Search: "Show all errors in last hour"
   - Correlation: "What happened before crash?"

3. Distributed Tracing (Jaeger)
   - Track request across services
   - "Message took 500ms, where's the bottleneck?"
   - Visual waterfall diagram

4. Alerting (PagerDuty)
   - Critical: Page on-call engineer (3am wake-up)
   - Warning: Slack notification
   - Info: Email digest

5. Dashboards (Grafana)
   - Real-time system health
   - CEO can see "Is WhatsApp working?"

Problem detected in 10 seconds, fixed in 5 minutes!
```

---

## Summary Checklist: Avoid These Mistakes

- [ ] ❌ Single database → ✅ Database sharding
- [ ] ❌ HTTP polling → ✅ WebSockets
- [ ] ❌ No caching → ✅ Redis cache with 80% hit ratio
- [ ] ❌ Media in database → ✅ S3 + CDN
- [ ] ❌ No redundancy → ✅ Multi-region, replicas, failover
- [ ] ❌ Over-engineering → ✅ Start simple, scale as needed
- [ ] ❌ No capacity plan → ✅ Calculate servers, storage, bandwidth
- [ ] ❌ Wrong database → ✅ SQL for structured, NoSQL for flexible
- [ ] ❌ Security later → ✅ Encryption, auth, validation from day 1
- [ ] ❌ No monitoring → ✅ Metrics, logs, traces, alerts

**Remember**: Every successful system started simple and evolved!
