# System Design Components - Complete Overview

> **For Complete Beginners**: This guide covers all fundamental building blocks of system design. Start here to understand what components exist and when to use them!

---

## 📋 Table of Contents

### Core Components
1. [Load Balancers](#1-load-balancers) - Distribute traffic across servers
2. [Databases](#2-databases) - Store and retrieve data
3. [Caching](#3-caching) - Speed up data access
4. [Message Queues](#4-message-queues) - Async communication
5. [API Design](#5-api-design) - Client-server communication
6. [CDN (Content Delivery Network)](#6-cdn) - Global content distribution
7. [Object Storage](#7-object-storage) - Store files (images, videos)

### Advanced Concepts
8. [Scalability Patterns](#8-scalability-patterns) - Handle growth
9. [Networking Basics](#9-networking-basics) - How internet works
10. [CAP Theorem](#10-cap-theorem) - Consistency vs Availability
11. [Monitoring & Observability](#11-monitoring) - Track system health

---

## 1. Load Balancers

### What is it?
Distributes incoming requests across multiple servers to prevent overload.

### Analogy
Restaurant host distributing customers across multiple tables/waiters.

### When to use?
- When you have multiple servers
- To improve availability (if one server fails, others handle traffic)
- To handle more traffic than one server can manage

### Popular Options
- **Nginx** (open-source, widely used)
- **HAProxy** (high performance)
- **AWS ALB** (managed, cloud-native)
- **Cloudflare Load Balancing**

### Learn More
👉 [Detailed Guide: 01_load-balancers.md](./01_load-balancers.md)

---

## 2. Databases

### What is it?
Persistent storage for structured data (users, posts, transactions).

### Types
- **SQL (Relational)**: PostgreSQL, MySQL - structured, ACID, relations
- **NoSQL (Non-relational)**: MongoDB, Cassandra - flexible schema, high throughput

### When to use SQL?
- Need relationships (users → posts → comments)
- Need ACID transactions (banking, e-commerce)
- Complex queries with JOINs

### When to use NoSQL?
- Flexible schema (data structure changes often)
- Massive write throughput (millions of writes/sec)
- Simple key-value or document storage

### Popular Options
**SQL**: PostgreSQL, MySQL, Oracle, SQL Server
**NoSQL**: MongoDB, Cassandra, DynamoDB, Redis

### Learn More
👉 [Detailed Guide: 02_databases.md](./02_databases.md)

---

## 3. Caching

### What is it?
Store frequently accessed data in memory (RAM) for fast retrieval.

### Analogy
Keeping your most-used books on desk instead of walking to library every time.

### Speed Comparison
- **RAM (cache)**: 0.1ms
- **SSD (disk)**: 10ms (100x slower)
- **Network DB**: 50-100ms (500-1000x slower)

### When to use?
- Read-heavy workloads (Instagram feed, YouTube homepage)
- Expensive computations (results that take time to calculate)
- Reduce database load

### Popular Options
- **Redis** (in-memory, rich data structures, persistence)
- **Memcached** (simple, fast, no persistence)
- **CDN** (cache at edge locations globally)

### Learn More
👉 [Detailed Guide: 03_caching.md](./03_caching.md)

---

## 4. Message Queues

### What is it?
Asynchronous communication between services via messages.

### Analogy
Post office: You drop letter (message) in mailbox, recipient gets it later (async).

### When to use?
- Decouple services (sender doesn't wait for receiver)
- Handle traffic spikes (queue absorbs burst)
- Background processing (send email, process video)

### Popular Options
- **Apache Kafka** (high throughput, event streaming)
- **RabbitMQ** (feature-rich, reliable)
- **AWS SQS** (managed, simple)
- **Google Pub/Sub**

### Learn More
👉 [Detailed Guide: 04_message-queues.md](./04_message-queues.md)

---

## 5. API Design

### What is it?
Interface for clients (mobile/web) to communicate with backend.

### Types
- **REST**: HTTP-based, stateless, widely used
- **GraphQL**: Query exactly what you need, flexible
- **gRPC**: High performance, binary protocol

### When to use REST?
- Standard CRUD operations
- Simple request/response
- Wide compatibility (all languages/platforms)

### When to use GraphQL?
- Flexible queries (avoid over-fetching)
- Multiple data sources
- Mobile apps (minimize network requests)

### When to use gRPC?
- Microservices communication
- Low latency requirements
- Streaming data

### Learn More
👉 [Detailed Guide: 05_api-design.md](./05_api-design.md)

---

## 6. CDN (Content Delivery Network)

### What is it?
Distribute static content (images, videos, CSS, JS) across global servers for fast delivery.

### How it works?
Content cached at edge servers worldwide. User gets content from nearest server.

### Speed Improvement
- **Without CDN**: User in Tokyo → US server = 200ms
- **With CDN**: User in Tokyo → Tokyo edge = 10ms (20x faster!)

### When to use?
- Global user base
- Static content (images, videos, files)
- High bandwidth costs

### Popular Options
- **CloudFront** (AWS, 200+ edge locations)
- **Cloudflare** (DDoS protection, free tier)
- **Akamai** (enterprise, largest network)
- **Fastly** (real-time updates)

### Learn More
👉 [Detailed Guide: 06_cdn.md](./06_cdn.md)

---

## 7. Object Storage

### What is it?
Scalable storage for files (images, videos, documents, backups).

### vs File System?
- **File System**: Hierarchical (/folder/subfolder/file.jpg), limited scale
- **Object Storage**: Flat (key-value), unlimited scale

### When to use?
- Store user uploads (photos, videos)
- Backups and archives
- Static website hosting
- Big data analytics

### Popular Options
- **AWS S3** (industry standard, 99.999999999% durability)
- **Google Cloud Storage**
- **Azure Blob Storage**
- **MinIO** (self-hosted, S3-compatible)

### Learn More
👉 [Detailed Guide: 07_object-storage.md](./07_object-storage.md)

---

## 8. Scalability Patterns

### Vertical Scaling (Scale Up)
Buy bigger server (more CPU, RAM).

**Pros**: Simple
**Cons**: Limited (max server size), expensive, single point of failure

### Horizontal Scaling (Scale Out)
Add more servers.

**Pros**: Nearly unlimited, cost-effective, high availability
**Cons**: Complex (need load balancer, distributed data)

### Database Scaling
- **Replication**: Copy data to multiple servers (read scaling)
- **Sharding**: Split data across servers (write scaling)
- **Partitioning**: Divide table into smaller pieces

### When to use?
Always plan for horizontal scaling from the start!

### Learn More
👉 [Detailed Guide: 08_scalability-patterns.md](./08_scalability-patterns.md)

---

## 9. Networking Basics

### DNS (Domain Name System)
Converts domain names (google.com) to IP addresses (142.250.185.46).

### HTTP/HTTPS
Protocol for web communication. HTTPS adds encryption (TLS/SSL).

### TCP/IP
Foundation of internet communication. TCP ensures reliable delivery.

### Load Balancing Layers
- **Layer 4 (Transport)**: Routes based on IP/port (fast, simple)
- **Layer 7 (Application)**: Routes based on content (flexible, slower)

### Learn More
👉 [Detailed Guide: 09_networking-basics.md](./09_networking-basics.md)

---

## 10. CAP Theorem

### What is it?
You can only have 2 of 3: Consistency, Availability, Partition Tolerance.

### Consistency
All nodes see the same data at the same time.

### Availability
Every request gets a response (no failures).

### Partition Tolerance
System works even if network splits.

### Real-world Trade-offs
- **CA (Consistency + Availability)**: Traditional SQL (no partition tolerance - single DC)
- **CP (Consistency + Partition Tolerance)**: MongoDB, HBase (sacrifice availability)
- **AP (Availability + Partition Tolerance)**: Cassandra, DynamoDB (eventual consistency)

### Learn More
👉 [Detailed Guide: 10_cap-theorem.md](./10_cap-theorem.md)

---

## 11. Monitoring & Observability

### Metrics
Numbers: CPU usage, latency, error rate, requests/sec.

### Logs
Text records of events: "User 123 logged in", "Error: Database connection failed".

### Traces
Track single request through entire system (distributed tracing).

### Popular Tools
- **Prometheus** (metrics collection)
- **Grafana** (visualization, dashboards)
- **ELK Stack** (Elasticsearch, Logstash, Kibana - logs)
- **Jaeger** (distributed tracing)
- **Datadog** (all-in-one, commercial)

### Learn More
👉 [Detailed Guide: 11_monitoring.md](./11_monitoring.md)

---

## Quick Comparison Matrix

| Component | Purpose | When NOT to use | Popular Choice |
|-----------|---------|-----------------|----------------|
| **Load Balancer** | Distribute traffic | Single server only | Nginx |
| **SQL Database** | Relational data, ACID | Flexible schema needed | PostgreSQL |
| **NoSQL Database** | High throughput, flexible | Need ACID, JOINs | MongoDB |
| **Cache** | Speed up reads | Write-heavy workload | Redis |
| **Message Queue** | Async processing | Need immediate response | Kafka |
| **REST API** | CRUD operations | Need flexible queries | Express.js |
| **GraphQL** | Flexible queries | Simple CRUD only | Apollo |
| **CDN** | Global content delivery | All users in one region | CloudFront |
| **Object Storage** | Files (images, videos) | Small files, DB storage | AWS S3 |

---

## Learning Path for Beginners

### Week 1: Core Components
- Day 1: Load Balancers
- Day 2: Databases (SQL vs NoSQL)
- Day 3: Caching
- Day 4: Practice: Design a simple blog

### Week 2: Communication & Storage
- Day 5: Message Queues
- Day 6: API Design (REST)
- Day 7: CDN & Object Storage
- Day 8: Practice: Design Instagram (simple version)

### Week 3: Advanced Concepts
- Day 9: Scalability Patterns
- Day 10: Networking Basics
- Day 11: CAP Theorem
- Day 12: Monitoring
- Day 13: Practice: Design Twitter
- Day 14: Review & Mock Interview

---

## How to Use This Guide

### For Complete Beginners
1. Start with this overview (you're here!)
2. Read each component guide in order (01 → 11)
3. Draw diagrams yourself (don't just read!)
4. Build small projects to practice

### For Interview Prep
1. Focus on comparison tables
2. Understand trade-offs (when to use X vs Y)
3. Practice explaining to someone else
4. Do mock system design interviews

### For Quick Reference
1. Use comparison matrix
2. Jump to specific component guide
3. Review diagrams

---

## Common Beginner Questions

**Q: Do I need to learn all components?**
A: Start with core 7 (load balancer, database, cache, message queue, API, CDN, storage). Advanced concepts come later.

**Q: Which database should I learn first?**
A: PostgreSQL (SQL). It's versatile and teaches you relational concepts. Then explore MongoDB (NoSQL).

**Q: When do I need a CDN?**
A: When you have users globally OR high bandwidth costs OR static content.

**Q: REST vs GraphQL?**
A: Start with REST (simpler, widely used). Learn GraphQL when you need flexible queries.

**Q: How much detail do I need for interviews?**
A: Understand what each component does, when to use it, and trade-offs vs alternatives. Don't memorize implementation details.

---

## Next Steps

1. **Read detailed guides**: Start with [01_load-balancers.md](./01_load-balancers.md)
2. **Practice drawing**: Whiteboard practice is critical
3. **Build projects**: Simple blog → Twitter clone → Instagram clone
4. **Mock interviews**: Practice explaining your designs

---

## File Structure

```
HLD/foundation/
├── 00_system-design-topics.md          ← You are here
├── 01_load-balancers.md                 ← Types, comparisons, diagrams
├── 02_databases.md                      ← SQL vs NoSQL deep dive
├── 03_caching.md                        ← Redis, strategies, patterns
├── 04_message-queues.md                 ← Kafka, RabbitMQ, SQS
├── 05_api-design.md                     ← REST, GraphQL, gRPC
├── 06_cdn.md                            ← CloudFront, Cloudflare
├── 07_object-storage.md                 ← S3, GCS, Azure Blob
├── 08_scalability-patterns.md           ← Sharding, replication
├── 09_networking-basics.md              ← DNS, HTTP, TCP/IP
├── 10_cap-theorem.md                    ← Consistency models
├── 11_monitoring.md                     ← Prometheus, Grafana
└── README.md                            ← Learning paths, FAQ
```

---

## Resources

### Books
- "Designing Data-Intensive Applications" by Martin Kleppmann
- "System Design Interview" by Alex Xu (Volumes 1 & 2)

### Online
- [System Design Primer (GitHub)](https://github.com/donnemartin/system-design-primer)
- [ByteByteGo](https://bytebytego.com/)
- [High Scalability Blog](http://highscalability.com/)

### Practice
- Mock interviews with friends
- LeetCode System Design section
- Design popular systems (Twitter, YouTube, Uber)

---

**Remember**: System design is learned by doing, not just reading. Draw diagrams, build projects, and explain your designs to others!

