# ATM System - Low Level Design (LLD)

> **Comprehensive, beginner-friendly guide to designing an ATM system**

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Documentation Structure](#documentation-structure)
3. [Quick Start](#quick-start)
4. [Key Features](#key-features)
5. [Architecture](#architecture)
6. [Design Patterns](#design-patterns)
7. [SOLID Principles](#solid-principles)
8. [How to Use This Guide](#how-to-use-this-guide)
9. [Interview Preparation](#interview-preparation)

---

## Overview

This repository contains a **complete Low-Level Design (LLD)** for an ATM (Automated Teller Machine) system, designed specifically for **beginners** learning software design and preparing for **interviews**.

**What you'll learn**:
- How to break down a complex system into manageable components
- Applying SOLID principles in real-world scenarios
- Using design patterns effectively
- Handling concurrency and data consistency
- Writing clean, maintainable code
- Preparing for LLD interviews

---

## Documentation Structure

This LLD is organized into **10 phases** with **step-by-step** progression:

| Phase | File | Description |
|-------|------|-------------|
| **1** | [01_requirements.md](01_requirements.md) | Functional & Non-functional requirements |
| **2** | [02_usecase_diagram.md](02_usecase_diagram.md) | Use cases and actors |
| **3** | [03_step1_class_diagram.md](03_step1_class_diagram.md) | Core entities (Card, Account, Customer) |
| **3** | [04_step2_class_diagram.md](04_step2_class_diagram.md) | Adding Transaction entity |
| **3** | [05_step3_class_diagram.md](05_step3_class_diagram.md) | Relationships & ATM entity |
| **4** | [06_step4_class_diagram.md](06_step4_class_diagram.md) | Service layer (Auth, Transaction) |
| **4** | [07_step5_class_diagram.md](07_step5_class_diagram.md) | ATM orchestration & hardware |
| **5** | [08_step6_repository_layer.md](08_step6_repository_layer.md) | Repository pattern & data access |
| **6** | [09_design_patterns.md](09_design_patterns.md) | Design patterns (State, Strategy, etc.) |
| **8** | [10_sequence_diagrams.md](10_sequence_diagrams.md) | Interaction flows |
| **9** | [11_solid_concurrency.md](11_solid_concurrency.md) | SOLID principles & concurrency |
| **10** | [12_final_complete_design.md](12_final_complete_design.md) | Complete architecture overview |
| **10** | [13_complete_java_implementation.java](13_complete_java_implementation.java) | **Full working code** |
| **10** | [14_interview_qa.md](14_interview_qa.md) | **20+ interview Q&A** |
| **10** | [15_whiteboard_checklist.md](15_whiteboard_checklist.md) | **Whiteboard guide** |
| **10** | [16_common_mistakes.md](16_common_mistakes.md) | **Common mistakes & solutions** |

---

## Quick Start

### For Learning:
1. **Start with requirements**: Read [01_requirements.md](01_requirements.md)
2. **Follow steps 1-10**: Read files in order
3. **Study the code**: Read [13_complete_java_implementation.java](13_complete_java_implementation.java)
4. **Practice**: Try to implement it yourself

### For Interview Prep:
1. **Read**: [14_interview_qa.md](14_interview_qa.md) - 20+ questions with answers
2. **Practice**: [15_whiteboard_checklist.md](15_whiteboard_checklist.md) - Whiteboard guide
3. **Avoid mistakes**: [16_common_mistakes.md](16_common_mistakes.md) - Learn from errors

### To Run the Code:
```bash
# Compile
javac 13_complete_java_implementation.java

# Run
java ATMSystemDemo

# Enter PIN when prompted: 1234
```

---

## Key Features

### Functional
- ✅ User authentication (card + PIN)
- ✅ Balance inquiry
- ✅ Cash withdrawal (multiple denominations)
- ✅ Cash deposit
- ✅ Transaction history
- ✅ Receipt printing

### Non-Functional
- ✅ **Security**: PIN encryption, card blocking
- ✅ **Concurrency**: Handles multiple ATMs
- ✅ **Consistency**: Database transactions (ACID)
- ✅ **Scalability**: Designed for 1000+ ATMs
- ✅ **Maintainability**: SOLID principles, clean code
- ✅ **Extensibility**: Easy to add features

---

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────┐
│         PRESENTATION LAYER                  │
│         ATMService (Facade)                 │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│          SERVICE LAYER                      │
│  AuthenticationService, TransactionService  │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│          DOMAIN LAYER                       │
│  Card, Account, Transaction, ATM, Customer  │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│       REPOSITORY LAYER                      │
│  AccountRepository, TransactionRepository   │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│          DATABASE                           │
│          MySQL                              │
└─────────────────────────────────────────────┘
```

**Key Principle**: **Separation of Concerns**
- **Presentation**: User interaction
- **Service**: Business logic
- **Domain**: Core entities
- **Repository**: Data access
- **Database**: Persistence

---

## Design Patterns

| Pattern | Where Used | Purpose |
|---------|------------|---------|
| **Facade** | `ATMService` | Simplify complex subsystem |
| **State** | ATM states | Manage state transitions |
| **Strategy** | Transaction types | Different algorithms for each type |
| **Singleton** | ATM instance | One instance per machine |
| **Factory** | Transaction creation | Complex object creation |
| **Observer** | Notifications | Decouple events from handlers |
| **Repository** | Data access | Abstract persistence |
| **Template Method** | Transaction processing | Define algorithm skeleton |
| **Dependency Injection** | All services | Loose coupling |

**See**: [09_design_patterns.md](09_design_patterns.md) for detailed examples

---

## SOLID Principles

### S - Single Responsibility
```java
// Each class has ONE job
class AuthenticationService {
    // Only handles authentication
}

class TransactionService {
    // Only handles transactions
}
```

### O - Open/Closed
```java
// Easy to add new transaction types without modifying existing code
interface TransactionStrategy { ... }
class FundTransferStrategy implements TransactionStrategy { ... }  // NEW!
```

### L - Liskov Substitution
```java
AccountRepository repo;
repo = new JpaAccountRepository();      // Production
repo = new InMemoryAccountRepository(); // Testing
// Both work identically
```

### I - Interface Segregation
```java
// Small, focused interfaces
interface CardReader { ... }
interface CashDispenser { ... }
// NOT one big ATMHardware interface
```

### D - Dependency Inversion
```java
// Depend on abstractions, not concretions
class TransactionService {
    private BankingService bankingService;  // Interface
}
```

**See**: [11_solid_concurrency.md](11_solid_concurrency.md) for detailed explanations

---

## How to Use This Guide

### For Beginners (Learning LLD):

**Week 1: Understand Requirements**
- Day 1: Read requirements and use cases
- Day 2: Understand actors and interactions
- Day 3: Study core entities

**Week 2: Design Progression**
- Day 1: Study Steps 1-3 (Entities)
- Day 2: Study Steps 4-5 (Services)
- Day 3: Study Step 6 (Repository)
- Day 4: Study design patterns
- Day 5: Study sequence diagrams

**Week 3: Implementation & Practice**
- Day 1-2: Read and understand complete Java code
- Day 3-4: Implement it yourself (without looking)
- Day 5: Compare with original

**Week 4: Interview Prep**
- Day 1-2: Study interview Q&A
- Day 3: Practice whiteboard design
- Day 4: Review common mistakes
- Day 5: Mock interview with friend

### For Interview Preparation:

**3 Days Before Interview**:
- Read [14_interview_qa.md](14_interview_qa.md) - All 20 questions
- Practice explaining 3 design patterns
- Review SOLID principles with examples

**1 Day Before Interview**:
- Review [15_whiteboard_checklist.md](15_whiteboard_checklist.md)
- Practice drawing class diagram (20 min)
- Practice drawing sequence diagram (10 min)

**Morning of Interview**:
- Review [16_common_mistakes.md](16_common_mistakes.md)
- Review concurrency handling (pessimistic locking)
- Review one complete flow (withdrawal)

---

## Interview Preparation

### Most Common Interview Questions:

1. **"Design an ATM system"**
   - Use: [15_whiteboard_checklist.md](15_whiteboard_checklist.md)

2. **"How do you handle concurrent withdrawals?"**
   - Answer: Pessimistic locking (SELECT FOR UPDATE)
   - See: [11_solid_concurrency.md](11_solid_concurrency.md)

3. **"Which design patterns did you use?"**
   - Answer: Facade, Strategy, Repository, State, Singleton
   - See: [09_design_patterns.md](09_design_patterns.md)

4. **"How do you ensure atomicity?"**
   - Answer: @Transactional, rollback on failure
   - See: [14_interview_qa.md](14_interview_qa.md) Q6

5. **"How would you scale this to 10,000 ATMs?"**
   - Answer: Microservices, sharding, caching
   - See: [14_interview_qa.md](14_interview_qa.md) Q11

### Whiteboard Strategy (45 minutes):

**0-5 min**: Clarify requirements
**5-10 min**: Draw core entities
**10-15 min**: Add relationships
**15-25 min**: Add service layer
**25-35 min**: Draw sequence diagram
**35-40 min**: Discuss concurrency
**40-45 min**: Answer questions

**See**: [15_whiteboard_checklist.md](15_whiteboard_checklist.md) for detailed guide

---

## Key Concepts Covered

### Software Engineering
- Layered architecture
- Separation of concerns
- Dependency injection
- Interface-based design
- SOLID principles

### Design Patterns
- Creational: Factory, Singleton
- Structural: Facade, Repository
- Behavioral: Strategy, State, Observer, Template Method

### Database
- Entity-relationship modeling
- ACID properties
- Pessimistic locking (SELECT FOR UPDATE)
- Optimistic locking (@Version)
- Database transactions

### Concurrency
- Race conditions
- Deadlock prevention
- Thread safety
- Transaction isolation

### Best Practices
- Clean code
- Code comments
- Error handling
- Logging
- Testing (unit, integration)

---

## Technologies Used

- **Language**: Java
- **Database**: MySQL
- **ORM**: JPA/Hibernate
- **Framework**: Spring (annotations)
- **Testing**: JUnit, Mockito
- **Diagrams**: Mermaid

---

## Sample Code Snippet

```java
/**
 * ATM Service - Main orchestrator (Facade pattern)
 */
class ATMService {
    private final ATM atm;
    private final AuthenticationService authService;
    private final TransactionService transactionService;
    private final CardReader cardReader;
    private final Screen screen;

    public void startSession() {
        screen.displayMessage("Welcome to ATM!");
        Card card = cardReader.readCard();

        // Authenticate
        if (!authService.authenticateUser(card, pin)) {
            screen.displayMessage("Authentication failed");
            return;
        }

        // Process transactions...
    }
}
```

**See**: [13_complete_java_implementation.java](13_complete_java_implementation.java) for complete working code

---

## What Makes This LLD Unique?

### ✨ Beginner-Friendly
- Clear explanations at every step
- "Why" behind every decision
- Common mistakes highlighted
- Real-world analogies

### ✨ Interview-Focused
- 20+ interview questions with answers
- Whiteboard drawing guide
- Time-boxed approach
- Common follow-up questions

### ✨ Comprehensive
- Requirements → Implementation → Testing
- UML diagrams → Sequence diagrams → Code
- Theory → Practice → Interview

### ✨ Incremental
- Step-by-step progression
- Each step builds on previous
- Clear evolution of design

### ✨ Production-Ready
- SOLID principles
- Design patterns
- Concurrency handling
- Security considerations
- Scalability

---

## FAQs

**Q: Do I need to memorize all the code?**
A: No! Understand the concepts, patterns, and trade-offs. Code is for reference.

**Q: Which file should I read first?**
A: Start with [01_requirements.md](01_requirements.md), then follow the numbered files.

**Q: How long to complete this guide?**
A: 2-3 weeks for complete understanding, 3 days for interview prep.

**Q: Can I use this for other systems?**
A: Yes! The patterns and principles apply to any LLD (Parking Lot, Library, etc.).

**Q: Is this enough for interviews?**
A: This is comprehensive. Also practice other systems (Elevator, Parking Lot, Hotel).

---

## Contributing

Found a mistake or have a suggestion? Open an issue!

---

## License

This educational content is free to use for learning and interview preparation.

---

## Acknowledgments

Inspired by:
- Gang of Four Design Patterns
- Robert C. Martin (Clean Code, SOLID)
- Martin Fowler (Refactoring)
- Real-world ATM systems

---

## Final Thoughts

**Remember**: LLD interviews test your ability to:
1. **Think systematically** (break down complex problems)
2. **Design clean solutions** (SOLID, patterns)
3. **Handle edge cases** (concurrency, failures)
4. **Communicate clearly** (explain trade-offs)

This guide prepares you for all four!

**Good luck with your interviews!** 🚀

---

## Quick Links

- **Start Learning**: [01_requirements.md](01_requirements.md)
- **Interview Q&A**: [14_interview_qa.md](14_interview_qa.md)
- **Whiteboard Guide**: [15_whiteboard_checklist.md](15_whiteboard_checklist.md)
- **Complete Code**: [13_complete_java_implementation.java](13_complete_java_implementation.java)
- **Common Mistakes**: [16_common_mistakes.md](16_common_mistakes.md)

---

**Happy Learning!** 📚
