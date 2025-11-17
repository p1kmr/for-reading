# Requirements and Capacity Estimations - WhatsApp Messaging System

## Table of Contents
1. [Functional Requirements](#1-functional-requirements)
2. [Non-Functional Requirements](#2-non-functional-requirements)
3. [Capacity Planning & Estimations](#3-capacity-planning--estimations)
4. [Assumptions](#4-assumptions)

---

## 1. Functional Requirements

### 1.1 Core Messaging Features
- **1:1 Real-time Messaging**: Users can send and receive text messages instantly to other users
- **Message Delivery Status**: Show message status indicators:
  - ✓ Single tick = Message sent to server
  - ✓✓ Double tick = Message delivered to recipient's device
  - 🔵 Blue double tick = Message read by recipient
- **Online/Offline Status**: Display when users are "online" or show "last seen" timestamp
- **Typing Indicators**: Show "typing..." when the other person is composing a message

### 1.2 Media Sharing
- **Image Sharing**: Send and receive photos (JPEG, PNG)
- **Video Sharing**: Send and receive video clips (MP4, up to 16MB)
- **Audio Messages**: Record and send voice notes
- **Document Sharing**: Share PDFs, Word documents, etc.

### 1.3 Group Features
- **Group Conversations**: Create groups with up to 100 members
- **Group Admin Controls**: Add/remove members, change group name and icon
- **Group Messaging**: Send messages to all group members simultaneously

### 1.4 Notifications & Offline Support
- **Push Notifications**: Notify users of new messages when app is closed
- **Offline Message Storage**: Store messages on server when recipient is offline
- **Message History**: Retrieve and display chat history when user logs in

### 1.5 User Management
- **User Registration**: Sign up using phone number with OTP verification
- **Contact Sync**: Automatically find which contacts are on WhatsApp
- **Profile Management**: Set profile picture, status message, and display name

---

## 2. Non-Functional Requirements

### 2.1 Scalability
- **User Base**: Support 2.5 billion active users worldwide
- **Concurrent Connections**: Handle 500 million concurrent online users
- **Message Volume**: Process 100 billion messages per day
- **Growth Rate**: Scale to accommodate 20% annual user growth

### 2.2 Performance
- **Message Latency**: Deliver messages within 100 milliseconds (0.1 seconds)
- **API Response Time**: REST API calls should respond within 200ms
- **Media Upload**: Support uploading 10MB files within 5 seconds on average connection
- **Connection Establishment**: WebSocket connection should be established within 1 second

### 2.3 Availability
- **Uptime**: Achieve 99.99% availability
  - **What this means**: Only 52 minutes of downtime per year
  - **Comparison**:
    - 99.9% = 8.76 hours downtime/year (not acceptable for messaging)
    - 99.99% = 52 minutes downtime/year (industry standard)
    - 99.999% = 5 minutes downtime/year (expensive to achieve)
- **No Single Point of Failure**: System should continue working even if servers fail
- **Disaster Recovery**: Ability to recover from data center failure within 5 minutes

### 2.4 Reliability
- **Message Delivery Guarantee**: Messages should never be lost (at-least-once delivery)
- **Data Durability**: Chat history should be persisted with 99.999999999% durability (11 nines)
- **Ordered Delivery**: Messages should arrive in the order they were sent

### 2.5 Consistency
- **Eventual Consistency**: It's acceptable if message status (read receipts) takes a few seconds to sync
- **Strong Consistency**: Message order must be strongly consistent
- **Last Seen Accuracy**: Last seen timestamp can be eventually consistent (few seconds delay OK)

### 2.6 Security
- **End-to-End Encryption**: All messages encrypted so only sender and receiver can read them
- **Authentication**: Phone number based authentication with OTP
- **Authorization**: Users can only access their own messages and groups they're part of
- **Data at Rest**: All stored data encrypted using AES-256
- **Data in Transit**: All network communication over TLS 1.3

### 2.7 Cost Optimization
- **Storage Costs**: Minimize storage costs while maintaining performance
- **Bandwidth Costs**: Compress media files to reduce data transfer costs
- **Infrastructure**: Use auto-scaling to avoid over-provisioning

---

## 3. Capacity Planning & Estimations

> **Beginner Note**: Capacity planning helps us calculate how many servers, databases, and storage we need. We'll break down each calculation step-by-step with simple math.

### 3.1 Traffic Estimates

#### Step 1: Daily Active Users (DAU)
```
Given:
- Total Users: 2.5 billion
- Daily Active Users: 50% of total users
- DAU = 2.5 billion × 50% = 1.25 billion users per day
```

#### Step 2: Messages Per Day
```
Given:
- 100 billion messages sent per day (from requirements)
- Average messages per user = 100 billion ÷ 1.25 billion users
- Average messages per user = 80 messages/day
```

#### Step 3: Requests Per Second (Average)
```
Messages per second (average):
- Messages/day = 100 billion
- Seconds in a day = 24 hours × 60 minutes × 60 seconds = 86,400 seconds
- Messages/second = 100,000,000,000 ÷ 86,400
- Messages/second ≈ 1,157,407 messages/sec (average)
```

#### Step 4: Peak Traffic (3x Average)
```
Peak hours (morning and evening):
- Peak multiplier = 3x average
- Peak messages/second = 1,157,407 × 3
- Peak messages/second ≈ 3,472,222 messages/sec
```

**Beginner Explanation**: Think of it like a restaurant. During lunch rush (12-1pm), you serve 3x more customers than the daily average per hour. Same concept here!

#### Step 5: Read vs Write Ratio
```
Operations breakdown:
- Sending a message = 1 Write operation
- Receiving a message = 1 Read operation
- Reading chat history = Multiple Read operations
- Checking online status = Read operation

Estimated ratio:
- Read operations: 80% (checking messages, history, status)
- Write operations: 20% (sending messages, updating status)

At peak traffic:
- Total operations/sec = 3,472,222
- Write operations = 3,472,222 × 20% = 694,444 writes/sec
- Read operations = 3,472,222 × 80% = 2,777,778 reads/sec
```

#### Step 6: API Calls (In addition to messages)
```
Other operations users perform:
- Checking online status: 5 times/day per user
- Uploading media: 10% of messages have media
- Group operations: 5% of messages are to groups

Status check requests:
- 1.25 billion users × 5 checks/day = 6.25 billion requests/day
- Requests/sec = 6.25 billion ÷ 86,400 ≈ 72,337 requests/sec

Media upload requests:
- 100 billion messages × 10% = 10 billion media files/day
- Uploads/sec = 10 billion ÷ 86,400 ≈ 115,740 uploads/sec
```

#### Summary Table: Traffic Estimates

| Metric | Average | Peak (3x) |
|--------|---------|-----------|
| Messages/second | 1,157,407 | 3,472,222 |
| Write operations/sec | 231,481 | 694,444 |
| Read operations/sec | 925,926 | 2,777,778 |
| Media uploads/sec | 115,740 | 347,222 |
| Status checks/sec | 72,337 | 217,011 |
| **Total Requests/sec** | **1,502,905** | **4,508,716** |

---

### 3.2 Storage Estimates

#### Step 1: Message Storage (Text Only)
```
Assumptions per text message:
- Average message size: 100 bytes (short text messages)
- Message metadata (sender, receiver, timestamp, IDs): 200 bytes
- Total per message: 300 bytes

Daily storage for messages:
- Messages/day: 100 billion
- Storage/day = 100 billion × 300 bytes
- Storage/day = 30,000,000,000,000 bytes
- Storage/day = 30 TB (terabytes)

Yearly storage:
- Storage/year = 30 TB × 365 days
- Storage/year = 10,950 TB ≈ 11 PB (petabytes)

5-year projection:
- Storage/5 years = 11 PB × 5 = 55 PB
```

**Beginner Note**:
- 1 KB (kilobyte) = 1,000 bytes (a small text file)
- 1 MB (megabyte) = 1,000 KB (a photo)
- 1 GB (gigabyte) = 1,000 MB (a movie)
- 1 TB (terabyte) = 1,000 GB (a large hard drive)
- 1 PB (petabyte) = 1,000 TB (huge data center storage)

#### Step 2: Media Storage
```
Assumptions:
- 10% of messages include media (10 billion media files/day)
- Average media file size:
  - Images: 500 KB (60% of media)
  - Videos: 2 MB (30% of media)
  - Audio: 100 KB (10% of media)

Weighted average media size:
- Average = (500 KB × 60%) + (2000 KB × 30%) + (100 KB × 10%)
- Average = 300 KB + 600 KB + 10 KB
- Average = 910 KB ≈ 1 MB per media file

Daily media storage:
- Media files/day: 10 billion
- Storage/day = 10 billion × 1 MB
- Storage/day = 10,000,000 GB = 10,000 TB = 10 PB/day

Yearly media storage:
- Storage/year = 10 PB × 365 = 3,650 PB ≈ 3.65 EB (exabytes)
```

#### Step 3: User Profile Storage
```
Data per user:
- Profile picture: 200 KB
- Username, phone, status: 1 KB
- Metadata: 1 KB
- Total: ~200 KB per user

Total user storage:
- 2.5 billion users × 200 KB
- 500,000,000,000 KB = 500 TB
```

#### Step 4: Database Indexes and Metadata
```
Estimate: 20% overhead for indexes, metadata, backups
- Text messages: 11 PB/year × 1.2 = 13.2 PB/year
- Media: 3.65 EB/year × 1.2 = 4.38 EB/year
```

#### Summary Table: Storage Estimates

| Data Type | Daily | Yearly | 5-Year |
|-----------|-------|--------|--------|
| Text Messages | 30 TB | 11 PB | 55 PB |
| Media Files | 10 PB | 3.65 EB | 18.25 EB |
| User Profiles | - | 500 TB | 500 TB |
| Indexes (20% overhead) | 2 PB | 730 PB | 3.65 EB |
| **Total** | **12 PB** | **4.4 EB** | **22 EB** |

**Beginner Tip**: This is MASSIVE storage! That's why we need distributed storage systems like Amazon S3, not regular hard drives.

---

### 3.3 Bandwidth Estimates

#### Step 1: Incoming Bandwidth (Upload)
```
Message uploads:
- Messages/sec (peak): 3,472,222
- Average message size: 300 bytes
- Bandwidth = 3,472,222 × 300 bytes = 1,041,666,600 bytes/sec
- Bandwidth = 1,041 MB/sec ≈ 1 GB/sec

Media uploads (peak):
- Media uploads/sec: 347,222
- Average media size: 1 MB
- Bandwidth = 347,222 × 1 MB = 347,222 MB/sec
- Bandwidth = 347 GB/sec

Total incoming bandwidth (peak):
- Text + Media = 1 GB/sec + 347 GB/sec
- Total = 348 GB/sec = 2.78 Tbps (terabits per second)
```

#### Step 2: Outgoing Bandwidth (Download)
```
Each message is delivered to recipient:
- Outgoing ≈ Incoming (1:1 messages)
- For group messages (5% of total), multiply by average group size (20 members)

Rough estimate:
- Outgoing bandwidth ≈ 1.2x incoming (accounting for groups)
- Outgoing = 348 GB/sec × 1.2 = 418 GB/sec
- Outgoing = 3.34 Tbps
```

#### Step 3: Total Bandwidth
```
Total bandwidth (peak):
- Incoming: 2.78 Tbps
- Outgoing: 3.34 Tbps
- Total: 6.12 Tbps (terabits per second)
```

#### Summary Table: Bandwidth Requirements

| Direction | Peak Bandwidth | Daily Data Transfer |
|-----------|----------------|---------------------|
| Incoming (Upload) | 2.78 Tbps | 12 PB |
| Outgoing (Download) | 3.34 Tbps | 14.4 PB |
| **Total** | **6.12 Tbps** | **26.4 PB/day** |

**Beginner Explanation**:
- 1 Mbps (megabit per second) = Home internet speed
- 1 Gbps = 1,000 Mbps = Fast office internet
- 1 Tbps = 1,000 Gbps = Data center internet backbone
- We need 6 Tbps = 6,000 Gbps! That's why we use CDNs and multiple data centers.

---

### 3.4 Memory (RAM) Estimates for Caching

#### Step 1: Active User Sessions
```
Concurrent online users:
- 500 million users online simultaneously
- Session data per user: 1 KB (user ID, session token, online status)
- RAM needed = 500 million × 1 KB = 500 GB
```

#### Step 2: Recent Messages Cache
```
Cache last 100 messages per active user:
- Active users: 500 million
- Messages per user in cache: 100
- Size per message: 300 bytes
- RAM = 500 million × 100 × 300 bytes = 15 TB
```

#### Step 3: Online Status Cache
```
All users' online status:
- Total users: 2.5 billion
- Status data per user: 100 bytes (user_id, status, last_seen timestamp)
- RAM = 2.5 billion × 100 bytes = 250 GB
```

#### Summary: Cache Memory Required
```
Total RAM for caching:
- User sessions: 500 GB
- Recent messages: 15 TB
- Online status: 250 GB
- Buffer (20%): 3.2 TB
- Total: ~19 TB of Redis/Memcached cache

Note: This will be distributed across multiple cache servers
```

---

### 3.5 Number of Servers Required

#### Application Servers
```
Assumptions:
- Each server can handle 10,000 concurrent WebSocket connections
- Need to support 500 million concurrent users

Servers needed:
- 500 million ÷ 10,000 = 50,000 application servers
- Add 20% for redundancy = 60,000 servers
```

#### Database Servers
```
Assumptions:
- Each database server handles 10,000 writes/sec
- Peak writes: 694,444 writes/sec

Master databases needed:
- 694,444 ÷ 10,000 = 70 master database shards

Read replicas (Read:Write = 80:20):
- Read operations: 2,777,778 reads/sec
- Each replica handles 50,000 reads/sec
- Replicas needed: 2,777,778 ÷ 50,000 = 56 read replicas

Total database servers:
- 70 masters + 56 read replicas = 126 database servers
- With replication factor of 3 (for backup): 126 × 3 = 378 servers
```

#### Cache Servers
```
Assumptions:
- Each Redis server has 512 GB RAM
- Need 19 TB total cache

Cache servers needed:
- 19,000 GB ÷ 512 GB = 37 cache servers
- Add replication (3x): 37 × 3 = 111 cache servers
```

#### Summary: Server Requirements

| Component | Servers Needed | Purpose |
|-----------|----------------|---------|
| Application Servers (WebSocket) | 60,000 | Handle real-time connections |
| Database Masters | 70 | Handle write operations |
| Database Read Replicas | 308 | Handle read operations |
| Cache Servers (Redis) | 111 | Store frequently accessed data |
| Load Balancers | 100 | Distribute traffic |
| Message Queue Brokers | 50 | Handle async operations |
| **Total** | **~60,639** | **Across multiple data centers** |

**Beginner Note**: These servers are distributed globally across multiple data centers (US, Europe, Asia, etc.) to serve users with low latency.

---

## 4. Assumptions

### 4.1 User Behavior Assumptions
- 50% of users are active daily (DAU = 1.25 billion)
- 20% of daily active users are online simultaneously (500 million concurrent)
- Average user sends 80 messages per day
- Users check the app 50 times per day (each session ~5 minutes)
- 10% of messages include media attachments
- 5% of messages are sent to groups

### 4.2 Technical Assumptions
- Average message size: 100 bytes text + 200 bytes metadata
- Average media file: 1 MB (mixture of images, videos, audio)
- Network latency within region: 50ms
- Database query time: 10-50ms
- Cache hit ratio: 80% (80% of requests served from cache)
- WebSocket connection establishment time: <1 second

### 4.3 Infrastructure Assumptions
- Multi-region deployment (US, Europe, Asia, South America)
- Each region has full stack (app servers, databases, cache)
- Data is sharded by user_id (consistent hashing)
- Media stored in object storage (S3-like) with CDN
- Messages stored for unlimited time (never deleted unless user deletes)

### 4.4 Business Assumptions
- 20% annual user growth rate
- Storage costs: $0.02 per GB/month (object storage pricing)
- Bandwidth costs: $0.05 per GB transferred
- Acceptable monthly infrastructure cost: $100 million+
  - Storage: ~$1 million/month (55 PB × $0.02)
  - Bandwidth: ~$40 million/month (26 PB/day × 30 days × $0.05)
  - Compute: ~$50 million/month (60,000+ servers)
  - Total: ~$90-100 million/month operational costs

### 4.5 Operational Assumptions
- 99.99% uptime target = 52 minutes downtime/year
- RTO (Recovery Time Objective): 5 minutes
- RPO (Recovery Point Objective): 0 minutes (no data loss)
- Backup frequency: Real-time replication + daily snapshots
- Disaster recovery: Multi-region failover capability

---

## Next Steps

With these requirements and estimations, we can now design:

1. **Architecture diagrams** (Step 1-6) showing how components scale
2. **Database schema** optimized for high throughput
3. **API design** for messaging operations
4. **Caching strategy** to achieve 80% cache hit ratio
5. **Sharding strategy** to distribute 11 PB+ data
6. **Real-time messaging** using WebSockets
7. **High availability** setup to achieve 99.99% uptime

Continue to: [Step 1: Basic Architecture →](./02_step1_basic_architecture.md)

---

## Quick Reference: Key Numbers

| Metric | Value |
|--------|-------|
| Daily Active Users | 1.25 billion |
| Messages/day | 100 billion |
| Peak messages/sec | 3.5 million |
| Storage/year | 4.4 EB |
| Bandwidth (peak) | 6.12 Tbps |
| Application Servers | 60,000 |
| Database Servers | 378 |
| Cache Servers | 111 |
| Required Uptime | 99.99% |
| Max Message Latency | 100ms |
