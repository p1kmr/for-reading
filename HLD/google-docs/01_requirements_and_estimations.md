# Google Docs - Requirements & Capacity Planning

## Table of Contents
1. [Functional Requirements](#functional-requirements)
2. [Non-Functional Requirements](#non-functional-requirements)
3. [Traffic Estimates](#traffic-estimates)
4. [Storage Estimates](#storage-estimates)
5. [Key Assumptions](#key-assumptions)

---

## 1. Functional Requirements

### 1.1 Core Document Operations (Priority: Critical)
- **Create Documents**: Users should be able to create new blank documents or from templates
- **Edit Documents**: Users should be able to add, delete, and modify text with rich formatting
- **Retrieve Documents**: Users should be able to instantly open and view their documents
- **Delete Documents**: Users should be able to soft-delete documents (move to trash)
- **Search Documents**: Users should be able to search for documents by title, content, or metadata

### 1.2 Rich Text Formatting (Priority: Critical)
- **Text Formatting**: Bold, italic, underline, strikethrough, font size, font family, text color
- **Paragraph Formatting**: Headings (H1-H6), alignment (left, center, right, justify), line spacing
- **Lists**: Bulleted lists, numbered lists, nested lists
- **Special Elements**: Hyperlinks, images, tables, code blocks, equations
- **Document Structure**: Page breaks, headers, footers

### 1.3 Real-Time Collaboration (Priority: Critical)
- **Simultaneous Editing**: Multiple users can edit the same document at the same time
- **Live Updates**: Changes appear in near real-time (within 100-200ms)
- **Conflict Resolution**: System automatically resolves conflicting edits without data loss
- **Live Cursors**: See where other users are currently typing or selecting
- **Presence Indicators**: See who else is viewing/editing the document

### 1.4 Comments & Suggestions (Priority: High)
- **Comments**: Users can add comments on specific text selections
- **Mentions**: Users can @mention others in comments to notify them
- **Suggestions Mode**: Users can propose changes without directly editing
- **Resolve Comments**: Mark comments as resolved/unresolved

### 1.5 Version History (Priority: High)
- **Auto-Save**: Every change is automatically saved (no manual save button)
- **Version Snapshots**: System creates version snapshots periodically
- **View History**: Users can view previous versions with timestamps and authors
- **Restore Version**: Users can revert to any previous version
- **Compare Versions**: See differences between two versions

### 1.6 Sharing & Access Control (Priority: Critical)
- **Share by Email**: Share document with specific users via email
- **Share by Link**: Generate shareable links with different permission levels
- **Permission Levels**:
  - **Viewer**: Can only read the document
  - **Commenter**: Can read and add comments
  - **Editor**: Can read, comment, and edit
  - **Owner**: Full control including sharing and deleting
- **Revoke Access**: Owner can remove access at any time

### 1.7 Offline Access & Sync (Priority: Medium)
- **Offline Editing**: Users can edit documents without internet connection
- **Auto-Sync**: Changes sync automatically when connection is restored
- **Conflict Handling**: If same content was edited offline by multiple users, system should merge changes

### 1.8 Export & Import (Priority: Medium)
- **Export Formats**: PDF, DOCX (Microsoft Word), Plain Text, HTML
- **Import Formats**: DOCX, Plain Text, HTML
- **Download**: Users can download documents in various formats

---

## 2. Non-Functional Requirements

### 2.1 Scalability
- **Total Users**: Support 100 million registered users
- **Daily Active Users (DAU)**: 10 million users actively using the system daily
- **Concurrent Users**: Support up to 500,000 concurrent users
- **Concurrent Editors per Document**: Up to 100 users editing the same document simultaneously
- **Total Documents**: Support 1 billion documents in the system
- **Growth Rate**: 20% year-over-year user growth

### 2.2 Performance
- **Document Load Time**: Documents should load within 500ms for users on normal connections
- **Real-Time Latency**: Edits should propagate to other users within 100-200ms
- **API Response Time**: 95% of API requests should complete within 200ms
- **Search Response Time**: Document search should return results within 1 second

### 2.3 Availability
- **Uptime**: 99.99% availability (target downtime: 52 minutes per year)
- **Zero Data Loss**: Even during failures, no user data should be lost
- **Graceful Degradation**: If real-time features fail, users should still be able to read/edit documents

### 2.4 Consistency
- **Eventual Consistency**: All users should eventually see the same document state (within seconds)
- **Strong Consistency for Permissions**: Access control changes should take effect immediately
- **Conflict-Free**: System must prevent merge conflicts using Operational Transformation or CRDT

### 2.5 Reliability & Durability
- **Data Durability**: 99.999999999% (11 nines) - documents should not be lost
- **Backup Frequency**: Incremental backups every hour, full backups daily
- **Disaster Recovery**: Ability to recover from complete datacenter failure within 1 hour
- **Data Replication**: Data replicated across at least 3 availability zones

### 2.6 Security
- **Authentication**: Secure user authentication (OAuth 2.0, JWT tokens)
- **Authorization**: Fine-grained access control (RBAC)
- **Encryption at Rest**: All documents encrypted using AES-256
- **Encryption in Transit**: All communication over HTTPS/TLS 1.3
- **Audit Logs**: Track all access and modifications for compliance

### 2.7 Cost Optimization
- **Storage Efficiency**: Use compression and deduplication to reduce storage costs
- **CDN Usage**: Serve static assets from CDN to reduce bandwidth costs
- **Auto-Scaling**: Scale infrastructure based on demand to optimize costs

---

## 3. Traffic Estimates

Let's calculate the expected traffic load for our Google Docs system.

### 3.1 User Engagement Assumptions

```
Total Registered Users:        100,000,000 (100 million)
Daily Active Users (DAU):      10,000,000 (10 million) = 10% of total users
Average Session Duration:      30 minutes per day
Average Documents per User:    50 documents per user
Active Documents per Day:      3 documents per user per day
```

### 3.2 Request Breakdown

#### Document Operations
```
Total DAU:                     10,000,000 users

Document Opens:
- Each user opens 3 documents/day
- Total opens = 10M × 3 = 30,000,000 opens/day
- Opens per second = 30M / 86,400 = 347 opens/sec
- Peak (3x average) = 1,041 opens/sec

Document Edits (Keystrokes):
- Each user types ~500 characters/session (across all docs)
- Operations sent every 5 characters (batched) = 100 operations/session
- Total edit operations = 10M × 100 = 1,000,000,000 ops/day
- Ops per second = 1B / 86,400 = 11,574 ops/sec
- Peak (3x) = 34,722 ops/sec

Document Saves (Auto-save):
- Auto-save every 30 seconds when editing
- Average editing time = 20 minutes/session
- Saves per session = (20 × 60) / 30 = 40 saves/session
- Total saves = 10M × 40 = 400,000,000 saves/day
- Saves per second = 400M / 86,400 = 4,630 saves/sec
- Peak (3x) = 13,890 saves/sec

Document Retrieval (List view):
- Users check document list 5 times/day
- Total retrievals = 10M × 5 = 50,000,000 retrievals/day
- Retrievals per second = 50M / 86,400 = 579 retrievals/sec
- Peak (3x) = 1,737 retrievals/sec
```

### 3.3 Real-Time Collaboration Traffic

```
Concurrent Active Sessions:    500,000 concurrent users
Collaborative Documents:       20% of active sessions (100K documents with multiple users)
Average Users per Doc:         3 users per collaborative document

WebSocket Connections:         500,000 persistent connections

Edit Broadcasts:
- Edit operations: 11,574 ops/sec (from above)
- Each edit broadcasts to average 2 other users
- Broadcast messages = 11,574 × 2 = 23,148 messages/sec
- Peak = 69,444 messages/sec
```

### 3.4 Total API Requests Summary

```
Read Operations (80% of traffic):
- Document opens:              347 req/sec
- Document list retrieval:     579 req/sec
- Version history views:       200 req/sec
- Comment retrieval:           150 req/sec
- Total Reads:                 1,276 req/sec (average)
- Total Reads (peak):          3,828 req/sec

Write Operations (20% of traffic):
- Document edits:              11,574 ops/sec
- Auto-saves:                  4,630 req/sec
- Comment creation:            100 req/sec
- Share/permission changes:    50 req/sec
- Total Writes:                16,354 req/sec (average)
- Total Writes (peak):         49,062 req/sec

TOTAL API TRAFFIC:             17,630 req/sec (average)
TOTAL API TRAFFIC (peak):      52,890 req/sec
```

### 3.5 Bandwidth Requirements

```
Document Open Request:
- Average document size: 50 KB (text) + 10 KB (metadata) = 60 KB
- Opens per second: 347 opens/sec
- Bandwidth = 347 × 60 KB = 20.8 MB/sec = 166 Mbps

Document Edit Operation:
- Operation size: 200 bytes (operation data + metadata)
- Operations per second: 11,574 ops/sec
- Bandwidth = 11,574 × 0.2 KB = 2.3 MB/sec = 18.4 Mbps

Real-Time Broadcasts:
- Broadcast size: 300 bytes (operation + routing info)
- Broadcasts per second: 23,148 broadcasts/sec
- Bandwidth = 23,148 × 0.3 KB = 6.9 MB/sec = 55.2 Mbps

TOTAL BANDWIDTH (average):     30 MB/sec = 240 Mbps
TOTAL BANDWIDTH (peak):        90 MB/sec = 720 Mbps

Monthly Data Transfer:
- 30 MB/sec × 86,400 sec/day × 30 days = 77,760 GB = 76 TB/month
```

---

## 4. Storage Estimates

Let's calculate how much storage we need for all documents, versions, and metadata.

### 4.1 Document Storage

```
Total Users:                   100,000,000 users
Documents per User:            50 documents
Total Documents:               5,000,000,000 (5 billion documents)

Average Document Size:
- Small documents (70%):       10 KB (text-only, short notes)
- Medium documents (25%):      100 KB (formatted docs with images)
- Large documents (5%):        500 KB (complex docs with tables, images)

Weighted Average:
= (0.70 × 10 KB) + (0.25 × 100 KB) + (0.05 × 500 KB)
= 7 KB + 25 KB + 25 KB
= 57 KB ≈ 60 KB per document (rounded)

Total Document Storage:
= 5,000,000,000 docs × 60 KB
= 300,000,000,000 KB
= 300,000 GB
= 293 TB (base storage)
```

### 4.2 Version History Storage

```
Version History Strategy:
- Store incremental changes (deltas), not full copies
- Average delta size: 5 KB (10% of average doc size)
- Versions per document: 100 versions (average over document lifetime)

Version Storage:
= 5,000,000,000 docs × 100 versions × 5 KB
= 2,500,000,000,000 KB
= 2,500,000 GB
= 2,441 TB
```

### 4.3 Metadata Storage

```
Metadata per Document:
- Document ID, title, owner, timestamps: 1 KB
- Permissions (5 users per doc on avg): 500 bytes
- Comments (average 5 comments per doc): 2.5 KB
- Total metadata: 4 KB per document

Total Metadata Storage:
= 5,000,000,000 docs × 4 KB
= 20,000,000,000 KB
= 20,000 GB
= 19.5 TB
```

### 4.4 User Data Storage

```
Users:                         100,000,000 users
User Profile Data:             5 KB per user (name, email, preferences, settings)

Total User Storage:
= 100,000,000 × 5 KB
= 500,000,000 KB
= 500 GB
= 0.5 TB
```

### 4.5 Total Storage Summary (Year 1)

```
Document Base Storage:         293 TB
Version History (Deltas):      2,441 TB
Metadata (Docs + Comments):    19.5 TB
User Data:                     0.5 TB
─────────────────────────────────────
SUBTOTAL:                      2,754 TB

Replication Factor (3x):       × 3
TOTAL WITH REPLICATION:        8,262 TB = 8.3 PB

Backup Storage (1 full):       2,754 TB
─────────────────────────────────────
GRAND TOTAL (Year 1):          11,016 TB ≈ 11 PB
```

### 4.6 Storage Growth (5-Year Projection)

```
Year-over-Year Growth:         20% user growth + 15% existing user data growth

Year 1:  11 PB
Year 2:  14.85 PB  (11 × 1.35)
Year 3:  20.05 PB  (14.85 × 1.35)
Year 4:  27.07 PB  (20.05 × 1.35)
Year 5:  36.54 PB  (27.07 × 1.35)

Total 5-Year Storage:          ≈ 37 PB
```

### 4.7 Storage Cost Estimation (Rough)

```
Cloud Storage Cost:            $0.023/GB/month (AWS S3 Standard)

Year 1 Storage (11 PB):
= 11,000,000 GB × $0.023
= $253,000/month
= $3,036,000/year

Year 5 Storage (37 PB):
= 37,000,000 GB × $0.023
= $851,000/month
= $10,212,000/year

Note: Using cheaper storage classes (Glacier for old versions) can reduce costs by 70%
```

---

## 5. Key Assumptions

### 5.1 User Behavior
1. **10% Daily Active Rate**: Only 10% of registered users actively use the system each day (industry standard for productivity apps)
2. **3 Documents per Session**: Users typically work on 2-3 documents in a session
3. **30-minute Sessions**: Average user spends 30 minutes editing documents per day
4. **Collaborative Usage**: 20% of active documents have multiple simultaneous editors
5. **Mobile vs Desktop**: 70% desktop, 30% mobile (mobile users have lighter usage patterns)

### 5.2 Technical Assumptions
1. **Peak Traffic**: Peak traffic is 3x the average (based on typical office hours traffic patterns)
2. **Compression Ratio**: Text compression achieves 10:1 ratio, reducing actual storage by 90% for text
3. **Delta Storage**: Version history stores only changes (deltas), not full copies
4. **Cache Hit Rate**: 80% of document opens can be served from cache (Redis)
5. **Read:Write Ratio**: 80% reads, 20% writes (including real-time operations)

### 5.3 Infrastructure Assumptions
1. **Multi-Region Deployment**: System deployed across 3 geographic regions (US, EU, Asia)
2. **Availability Zones**: Each region has 3 availability zones for high availability
3. **Replication Factor**: All data replicated 3x across availability zones
4. **CDN Usage**: Static assets (JS, CSS, fonts) served from CDN (99% hit rate)
5. **Auto-Scaling**: Application servers auto-scale based on CPU (target: 70% utilization)

### 5.4 Business Assumptions
1. **Freemium Model**: 90% free users (15 GB storage limit), 10% paid users (unlimited storage)
2. **Storage Limits**: Free users limited to 1,000 documents or 15 GB
3. **Feature Access**: Real-time collaboration limited to 10 users for free, unlimited for paid
4. **Retention Policy**: Deleted documents retained in trash for 30 days before permanent deletion
5. **Version Retention**: Full version history retained for paid users, 90 days for free users

---

## Summary Table

| Metric | Value |
|--------|-------|
| **Daily Active Users** | 10 million |
| **Total API Requests/sec (avg)** | 17,630 req/sec |
| **Total API Requests/sec (peak)** | 52,890 req/sec |
| **Read Operations/sec (avg)** | 1,276 req/sec |
| **Write Operations/sec (avg)** | 16,354 req/sec |
| **Bandwidth (avg)** | 240 Mbps |
| **Bandwidth (peak)** | 720 Mbps |
| **Total Storage (Year 1)** | 11 PB |
| **Total Storage (Year 5)** | 37 PB |
| **Target Latency (Real-time)** | 100-200ms |
| **Target Availability** | 99.99% |
| **Concurrent Users** | 500,000 |

---

## Next Steps

With these requirements and estimates, we can now proceed to design the system architecture:
1. **Step 1**: Basic Architecture (Client, Load Balancer, App Servers, Database)
2. **Step 2**: Add Caching Layer (Redis)
3. **Step 3**: Database Scaling (Replication, Sharding)
4. **Step 4**: Real-Time Components (WebSocket servers, Message Queue)
5. **Step 5**: CDN and Object Storage
6. **Step 6**: Final Architecture with Conflict Resolution

→ Continue to [Step 1: Basic Architecture](./02_step1_basic_architecture.md)
