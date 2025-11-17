# Google Docs - High-Level Design (HLD)

## 📚 Complete System Design for Interview Success

Welcome to the comprehensive Google Docs High-Level Design! This repository contains everything you need to understand, design, and explain a production-grade collaborative document editing system like Google Docs.

**Perfect for:**
- 🎯 System design interviews (Google, Meta, Amazon, etc.)
- 🏗️ Building real-time collaborative systems
- 📖 Learning distributed systems architecture
- 🚀 Understanding how Google Docs works under the hood

---

## 🗺️ Quick Navigation

### Core Design Documents
| # | Document | Description | Time to Read |
|---|----------|-------------|--------------|
| 01 | [Requirements & Estimations](./01_requirements_and_estimations.md) | Functional/non-functional requirements, capacity planning | 15 min |
| 02 | [Step 1: Basic Architecture](./02_step1_basic_architecture.md) | Client, LB, app servers, database | 10 min |
| 03 | [Step 2: Add Caching](./03_step2_add_caching.md) | Redis caching layer for performance | 10 min |
| 04 | [Step 3: Database Scaling](./04_step3_database_scaling.md) | Sharding and replication | 12 min |
| 05 | [Step 4: Real-Time & Messaging](./05_step4_realtime_and_messaging.md) | WebSocket, Kafka, OT for collaboration | 15 min |
| 06 | [Step 5: CDN & Storage](./06_step5_cdn_and_storage.md) | CloudFront, S3 for global delivery | 10 min |
| 07 | [Step 6: Final Architecture](./07_step6_final_architecture.md) | Complete system with all components | 20 min |

### Deep Dive Documents
| # | Document | Description | Time to Read |
|---|----------|-------------|--------------|
| 08 | [API Design](./08_api_design.md) | REST endpoints, WebSocket events | 15 min |
| 09 | [Database Design](./09_database_design.md) | Schema, indexes, queries | 15 min |
| 10 | [Data Flow Diagrams](./10_data_flow_diagrams.md) | Sequence diagrams for key operations | 12 min |
| 11 | [Scalability, Reliability & Security](./11_scalability_reliability_security.md) | Non-functional requirements deep dive | 25 min |

### Interview Preparation
| # | Document | Description | Time to Read |
|---|----------|-------------|--------------|
| 12 | [Interview Q&A](./12_interview_qa.md) | 20 common questions with model answers | 30 min |
| 13 | [Common Mistakes](./13_common_mistakes.md) | 15 mistakes to avoid + solutions | 20 min |
| 14 | [Summary](./14_summary.md) | One-page executive summary | 10 min |

**Total Reading Time:** ~4 hours (comprehensive understanding)
**Quick Read Time:** ~1 hour (step-by-step guides + summary)

---

## 🎯 Learning Paths

### Path 1: Interview Preparation (2 hours)
**Goal:** Prepare for Google Docs interview question

```
1. Read: 01_requirements_and_estimations.md (15 min)
   → Understand scale and requirements

2. Read: 07_step6_final_architecture.md (20 min)
   → See complete architecture

3. Read: 12_interview_qa.md (30 min)
   → Practice Q&A

4. Read: 13_common_mistakes.md (20 min)
   → Learn from errors

5. Read: 14_summary.md (10 min)
   → Quick reference cheat sheet

6. Practice: Whiteboard the design (30 min)
   → Simulate interview
```

**✅ You're interview-ready!**

---

### Path 2: Step-by-Step Learning (3 hours)
**Goal:** Build understanding incrementally

```
1. Read: 01_requirements_and_estimations.md (15 min)
   → Clarify requirements

2. Read in order: 02 → 03 → 04 → 05 → 06 → 07 (90 min)
   → Build architecture layer by layer

3. Read: 08_api_design.md (15 min)
   → Understand API contracts

4. Read: 09_database_design.md (15 min)
   → Understand data model

5. Read: 11_scalability_reliability_security.md (25 min)
   → Deep dive into non-functional requirements

6. Read: 14_summary.md (10 min)
   → Consolidate knowledge
```

**✅ You have deep understanding!**

---

### Path 3: Quick Overview (30 minutes)
**Goal:** High-level understanding for quick reference

```
1. Read: 01_requirements_and_estimations.md (10 min - skim)
   → Understand scale

2. Read: 07_step6_final_architecture.md (15 min - skim)
   → See final design

3. Read: 14_summary.md (5 min)
   → Key decisions and metrics
```

**✅ You can explain Google Docs HLD at a high level!**

---

### Path 4: Interview Practice (Ongoing)
**Goal:** Master the interview presentation

```
Week 1: Read & Understand
- Day 1-2: Read Path 2 (step-by-step)
- Day 3: Read interview Q&A
- Day 4: Read common mistakes
- Day 5: Memorize key numbers (from summary)

Week 2: Practice
- Day 1-2: Whiteboard design (30 min each day)
- Day 3-4: Mock interview with friend
- Day 5: Review feedback, refine

Week 3: Refine
- Day 1-7: Daily practice (15 min)
  - One day: Focus on requirements
  - Next day: Focus on architecture
  - Next day: Focus on database
  - Next day: Focus on real-time
  - Next day: Focus on scaling
  - Next day: Focus on security
  - Last day: Full end-to-end
```

**✅ You're confident and polished!**

---

## 🏗️ System Overview

### What We're Building
A collaborative document editing system like Google Docs that supports:
- ✅ Real-time collaboration (100 users editing simultaneously)
- ✅ Rich text formatting (bold, italics, images, tables)
- ✅ Version history (track changes, rollback)
- ✅ Sharing & permissions (viewer, commenter, editor, owner)
- ✅ Global scale (100M users, 10M concurrent, 5B documents)
- ✅ High availability (99.99% uptime)
- ✅ Low latency (<100ms real-time edits)

### System Scale
```
Users:                  100,000,000 (100M registered)
Daily Active Users:     10,000,000 (10M DAU)
Concurrent Users:       500,000
Documents:              5,000,000,000 (5B)
API Traffic:            17,630 req/sec (avg), 52,890 (peak)
Storage:                11 PB (Year 1)
Latency:                <100ms (real-time), <200ms (API)
Availability:           99.99% (52 min downtime/year)
Regions:                3 (US, EU, Asia)
Cost:                   $170K/month (~$2M/year)
```

### Key Technologies
```
Frontend:               React, Socket.io, Quill.js
Backend:                Node.js / Java Spring Boot
Database:               PostgreSQL (4 shards)
Cache:                  Redis Cluster
Message Queue:          Kafka
Real-Time:              WebSocket (Socket.io / Go)
Storage:                AWS S3
Search:                 Elasticsearch
CDN:                    CloudFront
Load Balancer:          ALB (Application Load Balancer)
DNS:                    Route 53 (geo-routing)
Monitoring:             Prometheus + Grafana + DataDog
Logging:                ELK Stack (Elasticsearch, Logstash, Kibana)
```

---

## 📖 Document Structure

Each document follows a consistent structure:

### Step-by-Step Guides (02-07)
```
1. Introduction
   - What we're adding
   - Why we need it
   - Problems it solves

2. Architecture Diagram
   - Visual representation
   - Component interactions
   - Data flow

3. Component Details
   - How it works
   - Configuration examples
   - Code snippets

4. Capacity Analysis
   - Traffic estimates
   - Storage requirements
   - Cost breakdown

5. Key Takeaways
   - Summary of additions
   - Next steps
```

### Deep Dive Documents (08-11)
```
1. Overview
   - Topic introduction
   - Importance

2. Detailed Explanation
   - In-depth technical details
   - Code examples
   - Best practices

3. Design Decisions
   - Trade-offs
   - Alternatives considered
   - Why this approach

4. Examples & Diagrams
   - Real-world scenarios
   - Sequence diagrams
   - Architecture diagrams
```

### Interview Documents (12-13)
```
1. Question
   - What interviewer asks
   - What they're looking for

2. Model Answer
   - Step-by-step response
   - Key points to cover
   - Example diagrams

3. Interview Tips
   - Do's and Don'ts
   - How to stand out
   - Common follow-ups
```

---

## 🎨 How to Use This HLD for Interviews

### Before the Interview

**1. Understand the Big Picture (1 hour)**
- Read the summary (14_summary.md)
- Review the final architecture diagram (07_step6_final_architecture.md)
- Understand key numbers (100M users, 17.6K req/sec, 11 PB storage)

**2. Master the Incremental Approach (2 hours)**
- Read step-by-step guides (02-07)
- Understand how to build architecture layer by layer
- Practice explaining each step in simple terms

**3. Prepare for Deep Dives (2 hours)**
- Read interview Q&A (12_interview_qa.md)
- Study common mistakes (13_common_mistakes.md)
- Review deep dive topics (API, database, real-time, scaling, security)

**4. Practice Whiteboarding (3 hours)**
- Draw basic architecture (5 minutes)
- Add caching (2 minutes)
- Add real-time (3 minutes)
- Add scaling (3 minutes)
- Practice explaining trade-offs
- Time yourself (30-minute limit)

---

### During the Interview

**Step 1: Clarify Requirements (3-5 minutes)**
```
Functional:
- Real-time collaboration? ✓
- Rich text formatting? ✓
- Version history? ✓
- Sharing & permissions? ✓
- Offline access? (Nice to have, not critical)

Non-Functional:
- Scale? (100M users, 10M DAU)
- Concurrent editors? (100 per document)
- Latency? (<200ms)
- Availability? (99.99%)
- Global? (Yes - US, EU, Asia)
```

**Step 2: Capacity Estimation (3-5 minutes)**
```
Users: 100M, 10M DAU, 500K concurrent
Traffic: 17.6K req/sec (avg), 52.9K (peak)
Storage: 11 PB (Year 1)
Bandwidth: 240 Mbps

(Show your math!)
```

**Step 3: Basic Architecture (5 minutes)**
```
Draw on whiteboard:

[ Users ] → [ Route 53 ]
              ↓
          [ CloudFront (CDN) ]
              ↓
          [ Load Balancer ]
              ↓
          [ App Servers ]
              ↓
      [ PostgreSQL ] [ S3 ]
              ↓
          [ Redis ]

Explain briefly why each component.
```

**Step 4: Add Real-Time (5 minutes)**
```
Add to diagram:

[ Users ] → [ WebSocket LB ]
              ↓
          [ WS Servers ]
              ↓
          [ Redis Pub/Sub ]
              ↓
          [ Kafka ]

Explain OT (Operational Transformation).
```

**Step 5: Add Scaling (3 minutes)**
```
Add to diagram:

[ Database ] → [ 4 Shards ]
            → [ Read Replicas ]

[ Multi-Region ]
  - US (primary)
  - EU (replica)
  - Asia (replica)

Explain sharding strategy.
```

**Step 6: Deep Dive (Based on Questions) (10-15 minutes)**
Be ready to explain:
- Real-time collaboration (OT vs CRDT)
- Database sharding (why user_id?)
- Caching strategy (what to cache, TTLs)
- Security (authentication, authorization, encryption)
- Monitoring (metrics, alerts, failure scenarios)

**Step 7: Trade-Offs & Wrap-Up (2-3 minutes)**
```
Key decisions:
- SQL vs NoSQL → Chose SQL (consistency)
- OT vs CRDT → Chose OT (proven at scale)
- Multi-region → 2x cost, worth it (latency, compliance)

Metrics achieved:
- 100M users ✓
- <100ms latency ✓
- 99.99% availability ✓
- $0.02/user/year ✓
```

**Total Time: ~30 minutes** (adjust based on interview length)

---

### Common Interview Questions

**Breadth Questions (High-Level):**
1. Design Google Docs from scratch
2. How does real-time collaboration work?
3. How do you scale to 100M users?
4. How do you ensure data isn't lost?
5. How do you handle multiple people editing the same document?

**Depth Questions (Specific Topics):**
1. Explain Operational Transformation
2. How do you shard the database?
3. What's your caching strategy?
4. How do you handle regional failures?
5. How do you prevent unauthorized access?

**Trade-Off Questions:**
1. Why SQL instead of NoSQL?
2. Why OT instead of CRDTs?
3. Why multi-region (expensive)?
4. Why Kafka instead of RabbitMQ?
5. Why not microservices for everything?

**Failure Scenarios:**
1. What if the database master fails?
2. What if the entire region goes down?
3. What if two users edit the same word simultaneously?
4. What if the cache is cleared?
5. What if Kafka is slow/down?

---

## 💡 Key Concepts Explained

### Operational Transformation (OT)
**Problem:** How do multiple users edit the same document without conflicts?

**Solution:** Transform operations so they work together
```
User A: insert('X', pos=0)
User B: insert('Y', pos=0)

Server transforms:
- A's operation: insert('X', pos=0) ✓
- B's operation: insert('Y', pos=1) ← adjusted!

Result: "XY..." (consistent for everyone)
```

**Why OT?**
- Proven at scale (Google Docs, Figma use it)
- Smaller data size than CRDTs
- Excellent for rich text

---

### Database Sharding
**Problem:** Single database can't handle 50K writes/sec

**Solution:** Split data across multiple databases
```
Sharding Key: user_id (hash-based)

Shard 0: Users A-G (25M users)
Shard 1: Users H-N (25M users)
Shard 2: Users O-T (25M users)
Shard 3: Users U-Z (25M users)

Write capacity: 10K → 40K writes/sec (4x)
```

**Why user_id?**
- Co-location (user's docs in same shard)
- No cross-shard queries for common operations
- Even distribution (hash function)

---

### Caching Strategy
**What to cache:**
- ✅ Small, frequently accessed data (sessions, permissions)
- ✅ Expensive queries (document list)
- ❌ Large objects (full documents - use S3)
- ❌ Rarely accessed data (old documents)

**TTLs:**
- Sessions: 24 hours
- Permissions: 10 minutes
- Document metadata: 5 minutes
- Document list: 2 minutes

**Invalidation:**
- Hybrid: TTL + event-based
- Update document → Delete cache key
- TTL as backup (if deletion fails)

---

### Multi-Region Deployment
**Why:**
- Lower latency (200ms → 50ms for EU/Asia)
- High availability (region failover)
- GDPR compliance (EU data in EU)

**Cost:**
- 2x infrastructure cost ($85K → $170K/month)
- Worth it? **YES** (better UX, compliance, availability)

**Replication:**
- Database: Async (100ms lag)
- S3: Cross-region (15-min lag)
- Redis: No replication (region-local cache)

---

## 🚀 Next Steps After This HLD

### 1. Implement a Simplified Version
Build a basic collaborative editor to solidify understanding:
```
Tech Stack:
- Frontend: React + Quill.js (rich text editor)
- Backend: Node.js + Socket.io (WebSocket)
- Database: PostgreSQL (single instance for MVP)
- Real-Time: Socket.io + in-memory OT (simple)

Features:
- Create/edit documents
- Real-time collaboration (2-3 users)
- Basic formatting (bold, italics)

Skip for MVP:
- Sharding (single DB is fine)
- Multi-region (single region)
- Complex OT (use library like Yjs)
- Heavy monitoring (basic logs)
```

**Learning:** Building solidifies concepts!

---

### 2. Study Similar Systems

**Notion** (Databases + Documents)
- How do they handle databases + documents together?
- Realtime collaboration on structured data
- Block-based editor

**Figma** (Real-Time Design)
- Real-time collaboration on canvas (not text)
- How does OT work for graphics?
- Conflict resolution for layers

**Confluence** (Enterprise Wiki)
- Similar to Google Docs, but enterprise focus
- How do they handle permissions at scale?
- Integration with other tools (Jira, etc.)

**Dropbox Paper** (Lightweight Docs)
- Markdown-based (simpler than rich text)
- Real-time collaboration
- File attachments

---

### 3. Practice More System Design Problems

**Similar Complexity:**
- Design Slack (real-time messaging)
- Design Trello (real-time kanban)
- Design Zoom (real-time video)
- Design Netflix (video streaming)
- Design Uber (real-time location)

**Beginner Friendly:**
- Design URL shortener (TinyURL)
- Design Pastebin (text storage)
- Design Instagram (photo sharing)

**Advanced:**
- Design YouTube (video at scale)
- Design Google Search (search at scale)
- Design WhatsApp (messaging at scale)

---

### 4. Read Recommended Books

**System Design:**
- *Designing Data-Intensive Applications* by Martin Kleppmann
- *System Design Interview* by Alex Xu (Vol 1 & 2)
- *Web Scalability for Startup Engineers* by Artur Ejsmont

**Distributed Systems:**
- *Database Internals* by Alex Petrov
- *Distributed Systems* by Maarten van Steen

**Real-Time Systems:**
- *Building Real-Time Applications* (blog posts, not book)
- Google Docs OT paper (academic paper)

---

### 5. Follow System Design Resources

**YouTube Channels:**
- Gaurav Sen (System Design)
- Tech Dummies (System Design)
- System Design Interview (channel)

**Blogs:**
- High Scalability (highscalability.com)
- AWS Architecture Blog
- Uber Engineering Blog
- Netflix Tech Blog

**GitHub Repos:**
- System Design Primer (donnemartin)
- Awesome System Design (madd86)

---

## ❓ FAQ

### Q: How long does it take to learn this HLD?
**A:** Depends on your goal:
- **Quick overview:** 30 minutes (summary + final architecture)
- **Interview prep:** 2-3 hours (requirements + Q&A + practice)
- **Deep understanding:** 4-6 hours (all documents + practice)
- **Mastery:** 2 weeks (read + implement + practice interviews)

---

### Q: Do I need to memorize all the numbers?
**A:** No! Memorize key numbers:
- 100M users, 10M DAU
- 17.6K req/sec (avg)
- 11 PB storage (Year 1)
- 99.99% availability
- <100ms latency (real-time)

Everything else can be derived using back-of-envelope calculations.

---

### Q: What if the interviewer asks about something not covered?
**A:**
1. **Don't panic!** No one knows everything.
2. **Think out loud:** "I haven't designed this before, but here's how I'd approach it..."
3. **Ask for hints:** "What would you recommend?"
4. **Make reasonable assumptions:** "I'd assume X because..."
5. **Be honest:** "I'm not sure, but I can learn about it."

Interviewers want to see your **thought process**, not perfection.

---

### Q: Should I design this exactly in an interview?
**A:** No! Tailor to the requirements:
- **Different scale?** Adjust (1M users = simpler, 1B users = more complex)
- **Different features?** Focus on what they ask (maybe no rich text, just plain text)
- **Time constraint?** Go breadth-first, then deep dive where they ask

Use this HLD as a **template**, not a script.

---

### Q: What if I disagree with a design decision?
**A:** Great! That means you're thinking critically.

Design choices depend on:
- Requirements (scale, latency, consistency)
- Team expertise (Node.js vs Java)
- Business constraints (budget, time)
- Trade-offs (complexity vs performance)

There's rarely one "correct" answer. Explain **why** your alternative is better for your specific context.

---

### Q: How do I practice whiteboarding?
**A:**
1. **Physical whiteboard:** Best for interview practice
2. **Excalidraw:** Free online whiteboard (excalidraw.com)
3. **Miro:** Collaborative whiteboard (miro.com)
4. **Paper + Pen:** Works in a pinch!

**Practice routine:**
1. Set timer for 30 minutes
2. Design Google Docs from scratch
3. Record yourself explaining (or explain to friend)
4. Review and refine
5. Repeat weekly

---

### Q: What if I get stuck during practice?
**A:**
1. **Refer back to this HLD** (that's why it exists!)
2. **Take a step back:** What problem am I solving?
3. **Start simple:** Basic architecture first, optimize later
4. **Ask yourself:** What would fail if I do X?
5. **Move on:** Don't get stuck on one component

**Remember:** Even experienced engineers don't know everything. The goal is to **think systematically**, not to have all the answers.

---

## 🎯 Success Criteria

You're ready for the interview when you can:

### Basic (Must Have)
- [ ] Draw basic architecture in 5 minutes (LB, app servers, DB, cache)
- [ ] Explain why you chose each component (SQL, Redis, S3)
- [ ] Calculate capacity (traffic, storage, bandwidth)
- [ ] Discuss 2-3 scaling strategies (caching, sharding, multi-region)
- [ ] Mention monitoring (metrics, logging, alerts)

### Intermediate (Should Have)
- [ ] Explain real-time collaboration (OT or CRDTs)
- [ ] Design database schema (documents, permissions, operations)
- [ ] Discuss failure scenarios (DB failure, region failure)
- [ ] Explain caching strategy (what to cache, TTLs, invalidation)
- [ ] Justify trade-offs (SQL vs NoSQL, OT vs CRDT)

### Advanced (Nice to Have)
- [ ] Deep dive into OT algorithm (with examples)
- [ ] Explain sharding strategy (why user_id, resharding)
- [ ] Discuss security architecture (auth, authz, encryption)
- [ ] Calculate cost breakdown (compute, storage, networking)
- [ ] Propose optimizations (compression, delta storage, CDN)

---

## 🙏 Acknowledgments

This HLD was created to help engineers succeed in system design interviews. It's based on:
- Real Google Docs architecture (publicly available information)
- Industry best practices (AWS Well-Architected, etc.)
- Personal experience designing production systems
- Feedback from 100+ mock interviews

**Special Thanks:**
- Google Docs team (for the original inspiration)
- System Design Primer community
- All the engineers who practice and provide feedback

---

## 📝 License

This HLD is provided for educational purposes. Feel free to:
- Use for interview preparation
- Share with friends and colleagues
- Adapt for your own learning

Not allowed:
- Selling or commercializing this content
- Claiming as your own work

---

## 🤝 Contributing

Found an error? Have a suggestion? Want to add a section?

**How to contribute:**
1. Open an issue (describe the problem/suggestion)
2. Submit a pull request (with proposed changes)
3. Provide feedback (what was helpful? what was confusing?)

**All contributions welcome!** 🎉

---

## 📞 Contact & Support

**Questions?**
- Open an issue in this repository
- Discuss in system design communities
- Practice with peers (mock interviews)

**Good luck with your interviews!** 🚀

Remember: **You don't need to be perfect. You need to show you can think systematically, make informed decisions, and communicate clearly.**

---

## 🎓 Final Thoughts

System design is a **skill**, not knowledge. You can't just read about it—you must **practice**.

**Your Learning Journey:**
```
Week 1: Read & Understand
  ├─ Read all documents
  ├─ Take notes
  └─ Draw diagrams

Week 2: Practice
  ├─ Whiteboard daily (30 min)
  ├─ Explain to a friend
  └─ Mock interviews

Week 3: Refine
  ├─ Review feedback
  ├─ Study weak areas
  └─ Practice until confident

Week 4+: Maintain
  ├─ Practice weekly
  ├─ Study new systems
  └─ Keep learning
```

**Most importantly:**
- Don't rush (understanding takes time)
- Ask questions (no question is stupid)
- Practice regularly (consistency beats intensity)
- Stay curious (why does X work this way?)

**You've got this!** 💪

Now go ace that interview! 🎯

---

**Document Version:** 1.0
**Last Updated:** 2025-01-15
**Next Review:** 2025-04-15

---

**Start Here:** Begin with [Requirements & Estimations](./01_requirements_and_estimations.md) or jump to [Final Architecture](./07_step6_final_architecture.md) for the big picture!
