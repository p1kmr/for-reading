# Movie Ticket Booking System - Complete Low-Level Design (LLD)

> A comprehensive, beginner-friendly guide to designing a production-ready Movie Ticket Booking System

## 📚 Table of Contents

1. [Overview](#overview)
2. [Document Structure](#document-structure)
3. [Quick Start Guide](#quick-start-guide)
4. [Key Features](#key-features)
5. [Learning Path](#learning-path)
6. [Design Highlights](#design-highlights)
7. [Technologies](#technologies)

---

## 🎯 Overview

This project provides a **complete Low-Level Design** for a Movie Ticket Booking System (similar to BookMyShow, Fandango, or Cineplex). It's designed for:

- **Beginners** learning system design
- **Interview preparation** for SDE roles
- **Reference implementation** for real-world projects

### What You'll Learn

- ✅ How to design entities and relationships (UML diagrams)
- ✅ How to prevent double-booking (concurrency control)
- ✅ How to handle payments and refunds
- ✅ How to apply SOLID principles and design patterns
- ✅ How to write production-ready Java code
- ✅ How to answer interview questions confidently

---

## 📂 Document Structure

### Phase 1: Requirements & Use Cases
| Document | Description | Key Concepts |
|----------|-------------|--------------|
| [01_requirements.md](./01_requirements.md) | Functional & non-functional requirements | Performance, scalability, concurrency |
| [02_use_case_diagram.md](./02_use_case_diagram.md) | Actors, use cases, and flows | Customer, Admin, System interactions |

**Start here if:** You want to understand what the system should do

---

### Phase 2-3: Domain Entities (Step-by-Step)
| Document | What's Added | Why Read This |
|----------|-------------|---------------|
| [03_step1_class_diagram.md](./03_step1_class_diagram.md) | **Step 1:** Core entities (Movie, Theater, Screen, Show) | Learn how to identify core domain entities |
| [04_step2_class_diagram.md](./04_step2_class_diagram.md) | **Step 2:** Add attributes & data types | Learn how to choose appropriate data types |
| [05_step3_class_diagram.md](./05_step3_class_diagram.md) | **Step 3:** Booking entities (Customer, Seat, ShowSeat, Booking) | **Critical:** Learn why ShowSeat is separate from Seat |
| [06_step4_class_diagram.md](./06_step4_class_diagram.md) | **Step 4:** Payment entity | Learn payment lifecycle and refund handling |

**Start here if:** You want to learn incremental design (step-by-step approach)

**Key Takeaway:** ShowSeat entity prevents double-booking by tracking seat status **per show**

---

### Phase 4-6: Architecture Layers
| Document | Layer | Key Concepts |
|----------|-------|--------------|
| [07_step5_service_layer.md](./07_step5_service_layer.md) | **Service Layer** | Business logic, orchestration, @Transactional |
| [08_step6_repository_layer.md](./08_step6_repository_layer.md) | **Repository Layer** | Data access, pessimistic locking, JPA |
| [09_step7_design_patterns.md](./09_step7_design_patterns.md) | **Design Patterns** | Strategy, Factory, Observer, Repository, SOLID |

**Start here if:** You want to understand layered architecture and design patterns

**Key Takeaway:**
- Service layer = Business logic (WHAT to do)
- Repository layer = Data access (HOW to persist)
- Patterns = Reusable solutions to common problems

---

### Phase 7-9: Implementation Details
| Document | Content | Why Critical |
|----------|---------|--------------|
| [10_sequence_diagrams.md](./10_sequence_diagrams.md) | Complete booking flow, cancellation, payment | Visualize interactions between components |
| [11_data_model.md](./11_data_model.md) | Database schema, ER diagram, indexes | Understand data structure and relationships |
| [12_java_code_complete.md](./12_java_code_complete.md) | Full Java implementation with comments | See theory applied in production code |

**Start here if:** You want to see how everything works in practice

**Key Takeaway:**
- Sequence diagrams show **time-based interactions**
- ER diagrams show **data relationships**
- Java code shows **actual implementation**

---

### Phase 10: Interview Preparation
| Document | Content | Use Case |
|----------|---------|----------|
| [13_interview_qa.md](./13_interview_qa.md) | 15+ interview questions with detailed answers | Practice for interviews |

**Start here if:** You have an interview coming up!

**Top 5 Questions Covered:**
1. How do you prevent double-booking? (Pessimistic locking)
2. How do you handle payment failures? (Transaction rollback, seat release)
3. Why separate Seat and ShowSeat? (Different status per show)
4. How would you scale to 1M users? (Sharding, caching, load balancing)
5. Which design patterns did you use? (Repository, Strategy, Factory, Observer)

---

## 🚀 Quick Start Guide

### For Beginners (First Time Learning LLD)

**Recommended Reading Order:**

1. **Day 1:** Requirements & Use Cases
   - Read: `01_requirements.md` + `02_use_case_diagram.md`
   - Goal: Understand what the system should do

2. **Day 2-3:** Domain Entities (Incremental)
   - Read: `03_step1` → `04_step2` → `05_step3` → `06_step4`
   - Goal: Learn how to design entities step-by-step

3. **Day 4:** Service & Repository Layers
   - Read: `07_step5` + `08_step6`
   - Goal: Understand layered architecture

4. **Day 5:** Design Patterns
   - Read: `09_step7_design_patterns.md`
   - Goal: Apply patterns to solve real problems

5. **Day 6:** Flows & Data Model
   - Read: `10_sequence_diagrams.md` + `11_data_model.md`
   - Goal: See how components interact

6. **Day 7:** Code & Interview Prep
   - Read: `12_java_code_complete.md` + `13_interview_qa.md`
   - Goal: Implement and practice interviews

**Total Time:** 1 week (1-2 hours per day)

---

### For Interview Preparation (Short on Time)

**Fast Track (2-3 Days):**

1. **Critical Documents:**
   - `05_step3_class_diagram.md` (ShowSeat concept - **most asked!**)
   - `10_sequence_diagrams.md` (Booking flow)
   - `13_interview_qa.md` (All interview questions)

2. **Quick Review:**
   - Scan `09_step7_design_patterns.md` for patterns
   - Skim `12_java_code_complete.md` for code snippets

3. **Practice:**
   - Draw class diagram from memory
   - Explain booking flow on whiteboard
   - Answer top 5 questions without notes

**Total Time:** 3 days (3-4 hours per day)

---

## 🎨 Key Features

### 1. Prevents Double-Booking ✅

**Problem:** Two users selecting same seat simultaneously

**Solution:** Pessimistic locking with `SELECT ... FOR UPDATE`

```java
// Acquires database row lock
ShowSeat seat = repo.findByIdWithLock("C7");  // 🔒 Locked!

if (seat.isAvailable()) {
    seat.block();  // Safe - no other transaction can modify
}
// Lock released on transaction commit 🔓
```

**Reference:** [08_step6_repository_layer.md](./08_step6_repository_layer.md#pessimistic-locking)

---

### 2. Handles Payment Failures Gracefully ✅

**Flow:**
```
Payment Failed
    ↓
Update payment.status = FAILED
    ↓
Update booking.status = EXPIRED
    ↓
Release seats (BLOCKED → AVAILABLE)
    ↓
Notify customer
```

**Reference:** [10_sequence_diagrams.md](./10_sequence_diagrams.md#payment-failure)

---

### 3. Auto-Releases Expired Bookings ✅

**Background Job (runs every minute):**
```java
@Scheduled(cron = "0 * * * * *")
public void releaseExpiredBookings() {
    // Find bookings where expires_at < NOW()
    List<Booking> expired = repo.findExpiredBookings();

    for (Booking b : expired) {
        b.setStatus(EXPIRED);
        releaseSeats(b.getSeatIds());
    }
}
```

**Reference:** [12_java_code_complete.md](./12_java_code_complete.md#background-job)

---

### 4. Separates Seat and ShowSeat ✅

**Why?**

Same physical seat has different status for different shows:
- Seat C7 @ 3 PM show: **BOOKED**
- Seat C7 @ 7 PM show: **AVAILABLE**

**Solution:**
- `Seat` = Physical attributes (row, number, type)
- `ShowSeat` = Availability per show (status, blocked_at)

**Reference:** [05_step3_class_diagram.md](./05_step3_class_diagram.md#showseat-entity)

---

### 5. Applies SOLID Principles ✅

| Principle | Example |
|-----------|---------|
| **Single Responsibility** | MovieService handles only movie operations |
| **Open/Closed** | Add new payment method without modifying existing code |
| **Liskov Substitution** | Swap JpaRepository with InMemoryRepository |
| **Interface Segregation** | Separate interfaces for different repositories |
| **Dependency Inversion** | Services depend on interfaces, not implementations |

**Reference:** [09_step7_design_patterns.md](./09_step7_design_patterns.md#solid-principles)

---

## 📊 Design Highlights

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Movie      │  │   Booking    │  │   Payment    │      │
│  │  Controller  │  │  Controller  │  │  Controller  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Movie      │  │   Booking    │  │   Payment    │      │
│  │   Service    │  │   Service    │  │   Service    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                              │
│  ↓ Uses                              ↓ Uses                 │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Notification │  │   Payment    │  │   Factory    │      │
│  │   Service    │  │  Strategies  │  │   Classes    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Movie      │  │   Booking    │  │  ShowSeat    │      │
│  │  Repository  │  │  Repository  │  │  Repository  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                              │
│  Implementations: JPA (MySQL) + In-Memory (Testing)          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                       DATABASE                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Tables: movies, theaters, screens, seats, shows,    │   │
│  │          show_seats, bookings, payments, customers   │   │
│  │                                                       │   │
│  │  Critical Table: show_seats (prevents double-booking)│   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

### Entity Relationships

```
Customer 1→M Booking M→1 Show M→1 Movie
                ↓
             M→M ShowSeat M→1 Seat M→1 Screen M→1 Theater
                ↓
              1→1 Payment
```

**Key:** 1→M (one-to-many), M→1 (many-to-one), M→M (many-to-many), 1→1 (one-to-one)

---

## 🛠 Technologies

| Layer | Technology | Why |
|-------|-----------|-----|
| **Language** | Java 17+ | Industry standard, strong typing |
| **Framework** | Spring Boot | Dependency injection, auto-configuration |
| **ORM** | Hibernate/JPA | Object-relational mapping, @Transactional |
| **Database** | MySQL | Relational, ACID transactions, row-level locking |
| **Cache** | Redis | In-memory, fast reads for movie listings |
| **Payment** | Stripe/Razorpay | PCI compliant, handles card data securely |
| **Messaging** | RabbitMQ/Kafka | Async notifications |
| **Testing** | JUnit, Mockito | Unit tests, integration tests |

---

## 📖 Learning Path

### Path 1: Complete Beginner

**Goal:** Learn LLD from scratch

1. Start with requirements (01, 02)
2. Follow step-by-step entities (03-06)
3. Understand architecture layers (07-09)
4. Study flows and code (10-12)
5. Practice interview questions (13)

**Time:** 7 days

---

### Path 2: Intermediate (Know Basic OOP)

**Goal:** Deepen understanding of design patterns and architecture

1. Skim requirements (01-02)
2. Focus on ShowSeat concept (05)
3. Deep dive into design patterns (09)
4. Study sequence diagrams (10)
5. Read Java code (12)
6. Practice interviews (13)

**Time:** 4 days

---

### Path 3: Advanced (Preparing for Senior Role)

**Goal:** Understand trade-offs and scalability

1. Focus on concurrency (08 - pessimistic locking)
2. Study design patterns (09 - SOLID)
3. Analyze sequence diagrams (10 - distributed transactions)
4. Review data model (11 - indexing strategies)
5. Master interview questions (13 - scalability questions)

**Time:** 2 days

---

## 🎯 Key Takeaways

### Top 3 Design Decisions

1. **ShowSeat Entity**
   - Why: Same seat, different status per show
   - Benefit: Tracks availability accurately

2. **Pessimistic Locking**
   - Why: Prevent double-booking race conditions
   - Benefit: Data consistency, no conflicts

3. **Booking Expiration**
   - Why: Don't hold seats forever
   - Benefit: Maximize seat utilization

### Top 3 Patterns Used

1. **Repository Pattern**
   - Separates data access from business logic
   - Easy to test (mock repositories)

2. **Strategy Pattern**
   - Multiple payment methods (UPI, Card, Wallet)
   - Easy to add new methods

3. **Factory Pattern**
   - Centralized object creation
   - Consistent defaults and validation

---

## 🏆 What Makes This LLD Special?

✅ **Beginner-Friendly:**
- Step-by-step approach (not overwhelming)
- Real-world examples (BookMyShow, Fandango)
- Common mistakes highlighted

✅ **Interview-Ready:**
- 15+ Q&A with detailed answers
- Whiteboard-friendly diagrams
- Trade-offs explained

✅ **Production-Ready:**
- Handles concurrency (pessimistic locking)
- Handles failures (payment, booking expiry)
- Scalable architecture (read replicas, caching)

✅ **Complete:**
- Requirements → Design → Code → Interview
- Multiple Mermaid diagrams (visual learning)
- 2000+ lines of commented Java code

---

## 🙏 Acknowledgments

This LLD is designed following industry best practices and common interview patterns from:
- System Design Interview books (Volume 1 & 2)
- Real-world systems (BookMyShow, Fandango)
- FAANG interview experiences

---

## 📝 License

This educational resource is free to use for learning purposes.

---

## 📧 Feedback

Found this helpful? Have suggestions?
- Star this repository ⭐
- Share with fellow learners
- Report issues or improvements

---

**Happy Learning! 🚀**

*Start your journey with [01_requirements.md](./01_requirements.md)*
