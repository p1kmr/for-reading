# ATM System - Whiteboard/Interview Checklist

> **Purpose**: Step-by-step guide for drawing LLD on whiteboard in 45 minutes

---

## Time Allocation (45 minutes total)

| Phase | Time | What to do |
|-------|------|-----------|
| Requirements Clarification | 5 min | Ask questions, list assumptions |
| Core Entities | 5 min | Draw main classes |
| Relationships | 5 min | Connect entities |
| Services Layer | 10 min | Add business logic |
| Design Patterns | 5 min | Identify and explain patterns |
| Sequence Diagram | 10 min | Draw main flow |
| Discussion | 5 min | Handle questions, discuss trade-offs |

---

## Step 1: Clarify Requirements (2-3 minutes)

### Questions to Ask Interviewer:

**Functional**:
- "Should we support balance inquiry, withdrawal, and deposit?"
- "Do we need to handle multiple card types or just one?"
- "Should we track transaction history?"

**Non-Functional**:
- "How many ATMs should the system support?"
- "What about concurrency - multiple ATMs accessing same account?"
- "Do we need to handle hardware failures?"

### State Your Assumptions:
```
Assumptions:
- Single currency (INR)
- One account per card
- Fixed denominations (₹100, ₹500, ₹2000)
- Backend banking API exists
- MySQL database
```

---

## Step 2: Draw Core Entities (5 minutes)

### What to Draw First (Priority Order):

```
┌─────────────────────┐
│   1. Account        │  ← Start here (most important)
│ ─────────────────── │
│ - accountNumber     │
│ - balance           │
│ + debit()           │
│ + credit()          │
└─────────────────────┘

┌─────────────────────┐
│   2. Card           │  ← Second
│ ─────────────────── │
│ - cardNumber        │
│ - expiryDate        │
│ + isValid()         │
└─────────────────────┘

┌─────────────────────┐
│   3. Transaction    │  ← Third
│ ─────────────────── │
│ - transactionId     │
│ - type              │
│ - amount            │
│ - status            │
└─────────────────────┘

┌─────────────────────┐
│   4. ATM            │  ← Fourth
│ ─────────────────── │
│ - atmId             │
│ - cashBins          │
│ + dispenseCash()    │
└─────────────────────┘
```

**Drawing Tip**: Use boxes, write class name on top, underline it

---

## Step 3: Show Relationships (3 minutes)

```
Customer ──1──┬──*── Account
              │
              └──*── Card

Account ───1──┬──*── Transaction

ATM ───*───1── Transaction  (processed at)
```

**Legend**:
- `1` = one
- `*` = many
- `──` = association
- `◇──` = aggregation (empty diamond)
- `◆──` = composition (filled diamond)

**Explain relationships**:
- "Customer owns multiple accounts"
- "Each account has many transactions"
- "Transactions are processed at specific ATM"

---

## Step 4: Add Service Layer (7 minutes)

### Draw Service Classes:

```
┌──────────────────────────┐
│ AuthenticationService    │
│ ──────────────────────── │
│ + authenticateUser()     │
│ + validateCard()         │
│ + validatePin()          │
└──────────────────────────┘
            ↓ uses
┌──────────────────────────┐
│  <<interface>>           │
│  BankingService          │
│ ──────────────────────── │
│ + validatePinWithBank()  │
│ + debitAccount()         │
└──────────────────────────┘

┌──────────────────────────┐
│ TransactionService       │
│ ──────────────────────── │
│ + withdraw()             │
│ + deposit()              │
│ + balanceInquiry()       │
└──────────────────────────┘
            ↓ uses
       [Entities]
```

**Explain**:
- "Services contain business logic"
- "Entities are just data holders"
- "Services orchestrate entities"

---

## Step 5: Show Main Design Patterns (5 minutes)

### Callout Patterns on Diagram:

```
┌──────────────┐
│  ATMService  │  ← Label: "Facade Pattern"
│              │     "Simplifies interface"
└──────────────┘

┌──────────────┐
│ Transaction  │  ← Label: "Strategy Pattern"
│   Strategy   │     "Different transaction types"
└──────────────┘

┌──────────────┐
│ BankingService│ ← Label: "Dependency Inversion"
│ <<interface>> │     "Depend on abstraction"
└──────────────┘
```

**Mention**:
- Facade (ATMService)
- Strategy (Transaction types)
- Repository (Data access)
- Dependency Injection (Constructor)

---

## Step 6: Draw Sequence Diagram (8-10 minutes)

### Pick ONE main flow (Withdrawal):

```
Customer  ATMService  AuthService  TransactionService  Account  ATM
   │           │           │                │             │      │
   │──Insert──>│           │                │             │      │
   │           │──validate─>│                │             │      │
   │           │<──OK──────│                │             │      │
   │           │                                                  │
   │──Amount──>│           │                │             │      │
   │           │───────────┼─── withdraw ──>│             │      │
   │           │           │                │──debit()──->│      │
   │           │           │                │───dispense──┼─────>│
   │           │           │                │<────OK──────┼──────│
   │           │<──────────┼───SUCCESS─────│             │      │
   │<──Cash───│           │                │             │      │
```

**Label key steps**:
1. Insert card & authenticate
2. Enter amount
3. Validate balance
4. Debit account
5. Dispense cash
6. Return success

---

## Step 7: Discuss Concurrency (3 minutes)

### Draw simple example:

```
ATM-1 ─┐
       ├─→ SELECT * FROM accounts WHERE id=123 FOR UPDATE
ATM-2 ─┘   (Second one WAITS for lock)

Timeline:
t1: ATM-1 locks row
t2: ATM-2 tries to lock (WAITS)
t3: ATM-1 updates balance, commits (releases lock)
t4: ATM-2 acquires lock, reads updated balance
```

**Explain**:
- "Pessimistic locking prevents race conditions"
- "Use database transactions for atomicity"

---

## Step 8: Discuss Scalability (if time permits)

### Quick architecture diagram:

```
Multiple ATMs → Load Balancer → [Service Cluster]
                                       ↓
                                 Database (Sharded)
                                       ↓
                                    Redis Cache
```

**Mention**:
- Stateless services (horizontal scaling)
- Database sharding by region
- Caching for read-heavy operations

---

## Quick Reference: What to Draw at Each Stage

### Minimum Viable Design (if short on time):

**Must Have** (15 minutes):
1. ✅ 4 core entities (Card, Account, Transaction, ATM)
2. ✅ Relationships between them
3. ✅ 2 services (AuthService, TransactionService)
4. ✅ 1 sequence diagram (withdrawal flow)

**Nice to Have** (if time permits):
5. Enums (CardStatus, TransactionStatus)
6. Hardware interfaces (CardReader, CashDispenser)
7. Design patterns callouts
8. Concurrency handling

**Skip if Running Out of Time**:
- Repository layer details
- Database schema
- Multiple sequence diagrams

---

## Common Interview Questions & Quick Answers

| Question | Quick Answer (30 seconds) |
|----------|--------------------------|
| "How do you handle concurrent withdrawals?" | "Pessimistic locking - SELECT FOR UPDATE in database transaction" |
| "What if cash dispensing fails?" | "Rollback account debit, mark transaction as failed, alert admin" |
| "Which design patterns did you use?" | "Facade (ATMService), Strategy (Transaction types), Repository (Data access)" |
| "How do you ensure data consistency?" | "Database transactions (@Transactional), ACID properties" |
| "How would you scale this?" | "Microservices, load balancing, database sharding, caching" |

---

## Presentation Tips

### Do's ✅:
- **Start big**: Draw boxes large enough to add details
- **Label everything**: Class names, relationships, patterns
- **Use legend**: Explain symbols (1, *, ◇, ◆)
- **Think aloud**: Explain while drawing
- **Prioritize**: Core entities first, details later
- **Ask questions**: "Should I focus more on X or Y?"

### Don'ts ❌:
- **Don't draw everything**: Focus on key components
- **Don't use tiny text**: Interviewer should be able to read
- **Don't erase**: Cross out and redraw if needed
- **Don't spend too long on one part**: Keep moving
- **Don't forget to explain**: Drawing without explanation is useless

---

## Sample Opening Statement (30 seconds)

> "Let me start by clarifying requirements. For this ATM system, I'll assume we need to support basic operations: balance inquiry, withdrawal, and deposit. The system should handle multiple ATMs accessing the same backend, so concurrency is important. I'll use a layered architecture with entities, services, and repositories, following SOLID principles. The main design patterns I'll use are Facade for simplification, Strategy for different transaction types, and Repository for data access. Does that sound good, or should I focus on any specific area?"

---

## Sample Closing Statement (30 seconds)

> "To summarize: We have a layered architecture with clear separation between entities (data), services (business logic), and repositories (data access). The design handles concurrency using database locking, ensures atomicity with transactions, and follows SOLID principles throughout. The main patterns used are Facade, Strategy, and Repository. For scalability, we can shard the database by region and use caching for read operations. The system is extensible - adding new transaction types is easy via the Strategy pattern. Do you have any questions about specific parts of the design?"

---

## Whiteboard Layout Template

```
┌──────────────────────────────────────────────────────────┐
│                     TOP-LEFT CORNER                      │
│  Write assumptions & requirements here                   │
│  - Assumption 1                                          │
│  - Assumption 2                                          │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│                      CENTER-LEFT                         │
│  Draw class diagram here (entities + services)           │
│                                                           │
│  [Card]  [Account]  [Transaction]  [ATM]                │
│     ↓        ↓           ↓           ↓                    │
│  [AuthService]    [TransactionService]                   │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│                      CENTER-RIGHT                        │
│  Draw sequence diagram here                              │
│                                                           │
│  Customer → ATM → Service → Entity                       │
│     │        │       │        │                           │
│     │────────│───────│────────│                          │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│                     BOTTOM                               │
│  Design patterns, concurrency notes, trade-offs          │
│  - Facade Pattern: ATMService                            │
│  - Pessimistic Locking: SELECT FOR UPDATE                │
│  - Trade-off: Greedy algorithm vs Dynamic Programming    │
└──────────────────────────────────────────────────────────┘
```

---

## Final Checklist Before Finishing

- [ ] Drew 4 core entities (Card, Account, Transaction, ATM)
- [ ] Showed relationships with cardinality
- [ ] Added 2-3 service classes
- [ ] Drew at least 1 sequence diagram
- [ ] Mentioned 3+ design patterns
- [ ] Explained concurrency handling
- [ ] Discussed one trade-off (e.g., locking strategy)
- [ ] Answered interviewer's questions
- [ ] Summarized key design decisions

---

**Remember**: Interviewer cares more about your **thinking process** than perfect diagrams. Explain your choices, discuss trade-offs, and show you can handle real-world concerns like concurrency and scalability!

**Good luck!** 🚀
