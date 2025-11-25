# Job Scheduler System - High-Level Design (HLD)

## 📚 Complete Beginner-Friendly System Design Guide

Welcome to the **most comprehensive Job Scheduler HLD** designed specifically for beginners! 🎉

This repository contains a **complete step-by-step guide** to designing a distributed job scheduler system from scratch, similar to **Apache Airflow**, **Kubernetes CronJobs**, or **Quartz Scheduler**.

---

## 🎯 What You'll Learn

By studying this HLD, you will understand:

✅ **System Design Fundamentals**
- How to gather and refine requirements
- Capacity planning with beginner-friendly math
- Estimating traffic, storage, and resources
- Making architecture trade-offs

✅ **Distributed Systems Concepts**
- Distributed locks (preventing duplicate work)
- Leader election (high availability)
- Message queues (decoupling components)
- Database replication (fault tolerance)
- Graceful degradation (handling failures)

✅ **Architecture Evolution**
- Starting with simple MVP architecture
- Iteratively adding complexity (5 steps)
- When to optimize vs premature optimization
- Scaling from 10 jobs/sec to 1M jobs/sec

✅ **Real-World Engineering**
- Monitoring & observability (Prometheus, Grafana, ELK)
- API design (RESTful endpoints, authentication)
- Database schema design (indexes, partitioning)
- Cost estimation (AWS pricing)

✅ **Interview Preparation**
- 20+ system design interview questions with answers
- Common mistakes and how to avoid them
- What to draw first in whiteboard interviews
- How to articulate trade-offs

---

## 📖 Documentation Structure

This HLD is organized into **11 comprehensive documents** covering every aspect of the system:

### Phase 1: Foundations
**📄 [01 - Requirements & Estimations](./01_requirements_and_estimations.md)**
- Functional requirements (submit, cancel, execute, monitor jobs)
- Non-functional requirements (99.99% uptime, ±5s latency)
- Traffic estimates with beginner-friendly math tips
- Storage calculations (job metadata, logs, retention)
- Resource planning (schedulers, workers, databases)

> **Beginner Tips:** Start here! Contains math breakdowns like "How to convert GB → TB" and "Understanding percentages for the math-challenged"

---

### Phase 2: Architecture Evolution (Step-by-Step)

**📄 [02 - Step 1: Basic Architecture](./02_step1_basic_architecture.md)**
- Simplest possible job scheduler
- Components: API Gateway → Scheduler → Worker → Database
- Database schema (jobs table, executions table)
- Polling approach (every 5 seconds)
- Limitations and when to evolve

**📄 [03 - Step 2: Add Message Queue](./03_step2_add_message_queue.md)**
- Replace polling with event-driven architecture
- RabbitMQ for job distribution
- Retry logic with exponential backoff
- Dead Letter Queue (DLQ) for failed jobs
- Automatic load balancing across workers

**📄 [04 - Step 3: Add Coordination Service](./04_step3_add_coordination_service.md)**
- Zookeeper for distributed locks
- Prevent duplicate job execution
- Leader election for scheduler HA
- Quorum and split-brain prevention
- Ephemeral locks (auto-release on crash)

**📄 [05 - Step 4: Monitoring & Database Design](./05_step4_monitoring_and_database_design.md)**
- Prometheus for metrics collection
- Grafana dashboards (jobs/sec, queue depth, latency)
- ELK stack for centralized logging
- AlertManager for PagerDuty/Slack alerts
- Database schema deep-dive (indexes, partitioning)

**📄 [06 - Step 5: High Availability & Fault Tolerance](./06_step5_high_availability_and_fault_tolerance.md)**
- Database replication (master-slave)
- Automatic failover (<40 seconds)
- Multi-region disaster recovery
- Circuit breaker pattern
- Achieving 99.99% uptime

> **Key Feature:** Each step shows **Mermaid diagrams** with incremental changes highlighted!

---

### Phase 3: API & Data Flow

**📄 [07 - Data Flow & API Design](./07_data_flow_and_api_design.md)**
- 5 detailed sequence diagrams (submit job, recurring job, cancel, failure, failover)
- Complete REST API specification (10 endpoints)
- Request/response examples
- Rate limiting & quotas
- Authentication & authorization (JWT, RBAC)

> **Includes:** Copy-paste ready API examples and `curl` commands!

---

### Phase 4: Advanced Features

**📄 [08 - Advanced Features & Trade-offs](./08_advanced_features_and_tradeoffs.md)**
- Job priority scheduling (critical jobs first)
- Job dependencies (DAG - Directed Acyclic Graph)
- Idempotency keys (prevent duplicate charges)
- Quotas & cost tracking (multi-tenancy)
- Secret management (HashiCorp Vault)
- Scaling to 1M jobs/second (database sharding, Kafka)

> **Real-World Examples:** Cycle detection for DAGs, secret reference syntax, shard routing logic

---

### Phase 5: Interview Preparation

**📄 [09 - Interview Q&A](./09_interview_qa.md)**
- 20+ system design interview questions
- Detailed answers with trade-off analysis
- Capacity calculations explained
- How to approach "design a job scheduler" question
- Cost estimation (AWS pricing breakdown)

**📄 [10 - Common Mistakes & Best Practices](./10_common_mistakes_and_best_practices.md)**
- Top 10 beginner mistakes (with examples)
- Why each mistake is bad (disaster scenarios)
- Correct approaches (with code)
- Best practices checklist (architecture, database, scaling, monitoring)

> **Interview Gold:** Shows you understand pitfalls, not just happy path!

---

### Phase 6: Summary

**📄 [11 - Summary & Key Decisions](./11_summary_and_key_decisions.md)**
- Final architecture diagram (all components)
- Key design decisions (SQL vs NoSQL, RabbitMQ vs Kafka, etc.)
- Capacity planning results (utilization percentages)
- Trade-offs summary table
- Cost estimate ($7,000/month, optimized to $3,000)
- Success metrics (99.99% uptime achieved)

---

## 🗺️ Learning Paths

### Path 1: Interview Preparation (Fast Track - 2 hours)
If you're preparing for a system design interview **tomorrow**:

1. **[Requirements & Estimations](./01_requirements_and_estimations.md)** - Learn capacity planning math (30 min)
2. **[Step 1: Basic Architecture](./02_step1_basic_architecture.md)** - Draw the simple diagram (20 min)
3. **[Summary](./11_summary_and_key_decisions.md)** - Memorize key decisions and trade-offs (20 min)
4. **[Interview Q&A](./09_interview_qa.md)** - Practice answering top 5 questions (30 min)
5. **[Common Mistakes](./10_common_mistakes_and_best_practices.md)** - Know what NOT to do (20 min)

**Practice:** Draw the architecture from memory. Explain trade-offs out loud.

---

### Path 2: Deep Understanding (Complete - 8-10 hours)
If you want to **truly understand** distributed systems:

**Week 1: Foundations**
- Day 1: Requirements & Estimations (2 hours)
- Day 2: Step 1 - Basic Architecture (1.5 hours)
- Day 3: Step 2 - Message Queue (2 hours)

**Week 2: Advanced Concepts**
- Day 4: Step 3 - Coordination Service (2 hours)
- Day 5: Step 4 - Monitoring & Database (2 hours)
- Day 6: Step 5 - High Availability (2 hours)

**Week 3: Application**
- Day 7: Data Flow & API Design (1.5 hours)
- Day 8: Advanced Features (2 hours)

**Week 4: Interview Prep**
- Day 9: Interview Q&A (2 hours)
- Day 10: Common Mistakes + Summary (1.5 hours)

**Practice:** Implement a simplified version (use Redis as message queue, SQLite as database)

---

### Path 3: Reference Guide (As Needed)
If you're **building a real job scheduler**:

- Use **[Requirements](./01_requirements_and_estimations.md)** to calculate your capacity needs
- Follow **[Step 1-5](./02_step1_basic_architecture.md)** incrementally (start simple, iterate)
- Refer to **[Database Design](./05_step4_monitoring_and_database_design.md)** for schema
- Use **[API Design](./07_data_flow_and_api_design.md)** as a blueprint
- Avoid **[Common Mistakes](./10_common_mistakes_and_best_practices.md)**

---

## 🎨 Visualizations

This HLD includes **15+ Mermaid diagrams**:

- ✅ Architecture evolution (Step 1 → Step 5)
- ✅ Sequence diagrams (5 critical flows)
- ✅ Component interaction diagrams
- ✅ Database replication flows
- ✅ Failure scenario visualizations

**All color-coded for easy understanding!**

---

## 📊 Key System Specifications

### Scale
- **Initial:** 1 million jobs/day (12 jobs/sec)
- **Peak:** 3x average = 36 jobs/sec
- **Target Scale:** 100 million jobs/day (future)

### Performance
- **Uptime:** 99.99% (52 minutes downtime/year)
- **Latency:** Jobs execute within ±5 seconds of scheduled time
- **Throughput:** System can handle 50,000 jobs/sec (over-provisioned)

### Infrastructure
- **API Servers:** 3 (active-active with load balancer)
- **Schedulers:** 3 (1 leader + 2 standby)
- **Workers:** 800 (auto-scaling to 5,000)
- **Database:** 1 master + 2 replicas (PostgreSQL)
- **Message Queue:** RabbitMQ cluster (3 nodes)
- **Coordination:** Zookeeper cluster (3 nodes)

### Cost
- **Standard:** ~$7,130/month
- **Optimized:** ~$3,000/month (spot instances, auto-scaling)
- **Per Job:** $0.00024

---

## 🛠️ Technologies Used

| Category | Technology | Why? |
|----------|-----------|------|
| **API** | Node.js + Express | Async I/O, fast, popular |
| **Database** | PostgreSQL 14 | ACID, strong consistency |
| **Queue** | RabbitMQ 3.11 | Reliable, FIFO, simple |
| **Coordination** | Apache Zookeeper 3.8 | Leader election, locks |
| **Cache** | Redis 7 | Fast, TTL support |
| **Monitoring** | Prometheus + Grafana | Industry standard |
| **Logging** | ELK Stack | Centralized, searchable |
| **Load Balancer** | Nginx / AWS ALB | Health checks, failover |
| **Orchestration** | Kubernetes | Auto-scaling, self-healing |

---

## 🚀 Quick Start: What to Draw First (Whiteboard Interview)

### Priority 1 (Must Have - 5 minutes)
1. **Components:** Client → Load Balancer → API → Scheduler → Queue → Workers → Database
2. **Draw boxes** for each component
3. **Label** what each component does
4. **Mention** PostgreSQL for database, RabbitMQ for queue

### Priority 2 (Important - Next 5 minutes)
5. Add **Zookeeper** (for distributed locks)
6. Add **Database replication** (1 master + 2 slaves)
7. Show **3 schedulers** (1 leader + 2 standby)

### Priority 3 (Good to Have - If Time Permits)
8. Add **monitoring** (Prometheus, Grafana)
9. Add **Dead Letter Queue** for failed jobs
10. Show **multi-region DR** (disaster recovery)

### Talk Through (While Drawing)
- "Users submit jobs via REST API..."
- "Scheduler polls database for jobs where next_run_time <= NOW..."
- "Uses distributed locks to prevent duplicate execution..."
- "RabbitMQ distributes jobs to workers..."
- "Workers update status in database after execution..."

---

## 💡 Beginner-Friendly Features

### Math Help
- **Simple explanations:** "1 day = 24 hours × 60 minutes × 60 seconds = 86,400 seconds"
- **Percentage tips:** "20% = 20/100 = 0.2, so 20% of 1M = 1M × 0.2 = 200K"
- **Unit conversions:** "1 KB = 1,024 bytes, 1 MB = 1,024 KB, 1 GB = 1,024 MB"
- **Speedup calculations:** "500x faster = Old time / New time"

### Analogies
- **Load Balancer:** "Restaurant host assigning tables to diners"
- **Message Queue:** "Conveyor belt in a factory"
- **Distributed Lock:** "Bathroom key (only 1 person can hold it)"
- **Leader Election:** "Electing a class president (only 1 leader)"

### Step-by-Step Code
- Pseudocode for every algorithm
- Comments explaining each line
- Error handling shown (not just happy path)

---

## 🎯 Who Is This For?

### ✅ Perfect for:
- **Interview candidates** preparing for senior/staff engineer roles
- **Beginners** learning distributed systems
- **Bootcamp grads** wanting real-world system design knowledge
- **Mid-level engineers** transitioning to architecture roles
- **Students** studying for system design courses

### ⚠️ Not for:
- People looking for code implementation (this is HLD, not LLD)
- Experts who already design distributed systems daily
- Those wanting a 5-minute overview (this is comprehensive!)

---

## 📚 Additional Resources

### Similar Systems to Study
- **Apache Airflow** - Python-based workflow scheduler (has DAG support)
- **Kubernetes CronJobs** - Container-based job scheduling
- **AWS Step Functions** - Serverless workflow orchestration
- **Temporal** - Microservice orchestration platform
- **Quartz Scheduler** - Java-based job scheduling library

### Related HLD Topics
- URL Shortener (simpler system to start with)
- Rate Limiter (distributed locks, Redis)
- Twitter/Social Media Feed (high read throughput)
- Netflix/YouTube (CDN, video streaming)
- Uber/Lyft (real-time location, matching)

### Books
- **Designing Data-Intensive Applications** by Martin Kleppmann (Chapter 6: Partitioning)
- **System Design Interview** by Alex Xu (Volume 1 & 2)
- **Database Internals** by Alex Petrov

### Online Courses
- **Grokking the System Design Interview** (Educative.io)
- **System Design Primer** (GitHub - free!)
- **ByteByteGo** (YouTube channel by Alex Xu)

---

## 🤝 How to Use This HLD

### For Interviews
1. Read **Requirements** and **Summary** first
2. Practice drawing **Step 1** architecture from memory
3. Memorize **key trade-offs** (SQL vs NoSQL, etc.)
4. Review **Interview Q&A** for common questions
5. Avoid **Common Mistakes**

### For Learning
1. Read in order (01 → 11)
2. Draw diagrams yourself (don't just read!)
3. Ask yourself "Why?" for each decision
4. Think about alternative approaches
5. Try to implement a simplified version

### For Building
1. Use as a **reference architecture**
2. Start with **Step 1** (MVP)
3. Add complexity **incrementally** (Step 2, 3, 4, 5)
4. Adapt to your specific requirements
5. Don't over-engineer!

---

## ✅ System Requirements Achieved

| Requirement | Target | Achieved | Status |
|-------------|--------|----------|--------|
| **Scalability** | 1M → 100M jobs/day | Architecture supports it | ✅ |
| **Availability** | 99.99% uptime | 99.995% (FMEA analysis) | ✅ |
| **Latency** | ±5 seconds | <3 seconds (95th percentile) | ✅ |
| **Consistency** | Exactly-once execution | Distributed locks + idempotency | ✅ |
| **Fault Tolerance** | Auto-recovery | Failover <40 seconds | ✅ |

---

## 🎓 Key Takeaways

After studying this HLD, you should be able to:

1. **Gather requirements** for any distributed system
2. **Estimate capacity** with back-of-the-envelope calculations
3. **Design architectures** that evolve from simple to complex
4. **Make trade-offs** between consistency, availability, and partition tolerance
5. **Explain decisions** clearly in interviews
6. **Avoid common mistakes** that cause production outages
7. **Think about failure scenarios** and design for resilience
8. **Monitor and observe** systems in production

**Most importantly:** You'll understand **WHY** each component exists, not just WHAT it does!

---

## 📞 Interview Simulation

**Interviewer:** "Design a distributed job scheduler like cron."

**You (following this HLD):**

1. **Clarify:** "Let me ask about scale - how many jobs per day? Any latency requirements?"
2. **Estimate:** "If 1M jobs/day, that's ~12 jobs/sec average, 36 peak. I'll need..."
3. **Draw:** [Sketch Step 1 architecture with clear labels]
4. **Iterate:** "For high availability, I'll add Zookeeper for leader election..."
5. **Trade-offs:** "I chose PostgreSQL over MongoDB because we need ACID transactions to prevent duplicate execution. The trade-off is..."
6. **Failures:** "If the scheduler crashes, Zookeeper detects it in 5 seconds and promotes a standby..."
7. **Scale:** "To scale to 1M jobs/sec, we'd need to shard the database and switch to Kafka..."

**Result:** Senior/Staff level response! 🎉

---

## 🏆 What Makes This HLD Special?

✅ **Beginner-friendly math** - No advanced calculus, just arithmetic with tips
✅ **Step-by-step evolution** - Architecture grows incrementally (5 steps)
✅ **Visual diagrams** - 15+ Mermaid diagrams (color-coded for clarity)
✅ **Real-world examples** - Disaster scenarios, cost estimates, AWS pricing
✅ **Interview focus** - 20+ Q&A, common mistakes, what to draw first
✅ **Complete coverage** - Requirements → Architecture → API → Advanced → Summary
✅ **Trade-off analysis** - Every decision includes pros, cons, and when to revisit
✅ **Failure scenarios** - What happens when X crashes? (FMEA table)

---

## 📝 Feedback & Contributions

This HLD is designed to be a living document. If you find:
- ❓ Confusing explanations
- 🐛 Technical errors
- 💡 Missing topics
- 📊 Better diagrams

Please provide feedback to improve it for future learners!

---

## 🎉 Congratulations!

By completing this HLD, you've learned:
- ✅ Distributed systems fundamentals
- ✅ Database design & optimization
- ✅ API design best practices
- ✅ Monitoring & observability
- ✅ High availability patterns
- ✅ System design interview skills

**You're now ready to:**
- 🎯 Ace system design interviews
- 🏗️ Build production-grade distributed systems
- 📚 Study more advanced topics (Paxos, Raft, CRDTs)
- 🚀 Level up to senior/staff engineer roles

---

## 📖 Document Navigation

| # | Document | Topics | Time |
|---|----------|--------|------|
| 01 | [Requirements & Estimations](./01_requirements_and_estimations.md) | Requirements, capacity planning, math tips | 45 min |
| 02 | [Step 1: Basic Architecture](./02_step1_basic_architecture.md) | Simple scheduler, polling, database schema | 30 min |
| 03 | [Step 2: Message Queue](./03_step2_add_message_queue.md) | RabbitMQ, event-driven, retry logic | 40 min |
| 04 | [Step 3: Coordination Service](./04_step3_add_coordination_service.md) | Zookeeper, distributed locks, leader election | 45 min |
| 05 | [Step 4: Monitoring & Database](./05_step4_monitoring_and_database_design.md) | Prometheus, Grafana, ELK, schema design | 50 min |
| 06 | [Step 5: High Availability](./06_step5_high_availability_and_fault_tolerance.md) | Replication, failover, circuit breakers | 45 min |
| 07 | [Data Flow & API Design](./07_data_flow_and_api_design.md) | Sequence diagrams, REST API, authentication | 40 min |
| 08 | [Advanced Features](./08_advanced_features_and_tradeoffs.md) | Priority, DAG, idempotency, scaling | 50 min |
| 09 | [Interview Q&A](./09_interview_qa.md) | 20+ questions, answers, trade-offs | 60 min |
| 10 | [Common Mistakes](./10_common_mistakes_and_best_practices.md) | Top 10 mistakes, best practices checklist | 35 min |
| 11 | [Summary & Key Decisions](./11_summary_and_key_decisions.md) | Final architecture, trade-offs, cost | 30 min |

**Total study time:** ~8 hours for deep understanding

---

**Happy learning! 🚀 May your systems always be distributed and your databases always consistent!** 🎊

