# 📊 Design Summary - Splitwise System

## 🎯 One-Page Overview

### Core Design Decisions

| Decision | Choice | Reason | Trade-off |
|----------|--------|--------|-----------|
| **Architecture** | Layered (Controller → Service → Repository → DB) | Clear separation of concerns | More classes, but maintainable |
| **Split Calculation** | Strategy Pattern | Easy to add new split types | Slightly more code upfront |
| **Data Access** | Repository Pattern | Abstraction over DB | Easier testing, loose coupling |
| **Money Type** | BigDecimal | Exact decimal arithmetic | Slightly slower than double |
| **Balance Storage** | Calculated on-the-fly | Always consistent | Slower queries (mitigated with cache) |
| **Locking** | Optimistic for updates, Pessimistic for settlements | Balance performance & consistency | Conflicts possible (handled gracefully) |
| **ID Type** | UUID (String) | Globally unique, distributed-friendly | 36 characters vs 8 bytes for long |
| **API Style** | RESTful | Industry standard, simple | Not as flexible as GraphQL |
| **Database** | MySQL (Relational) | ACID compliance, joins for balances | Not as scalable as NoSQL |
| **Caching** | Redis for balances (5 min TTL) | Performance boost | Stale data risk (acceptable) |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                  PRESENTATION LAYER                 │
│  ┌───────────┐ ┌───────────┐ ┌─────────────────┐  │
│  │   User    │ │   Group   │ │    Expense      │  │
│  │ Controller│ │ Controller│ │   Controller    │  │
│  └───────────┘ └───────────┘ └─────────────────┘  │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│                   SERVICE LAYER                     │
│  ┌───────────┐ ┌───────────┐ ┌─────────────────┐  │
│  │   User    │ │   Group   │ │    Expense      │  │
│  │  Service  │ │  Service  │ │    Service      │  │
│  └───────────┘ └───────────┘ └─────────────────┘  │
│  ┌──────────────┐ ┌──────────────────────────┐    │
│  │ Transaction  │ │      Balance             │    │
│  │   Service    │ │      Service             │    │
│  └──────────────┘ └──────────────────────────┘    │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│                 REPOSITORY LAYER                    │
│  ┌───────────┐ ┌───────────┐ ┌─────────────────┐  │
│  │   User    │ │   Group   │ │    Expense      │  │
│  │Repository │ │Repository │ │   Repository    │  │
│  └───────────┘ └───────────┘ └─────────────────┘  │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│                  DATA LAYER                         │
│              MySQL Database + Redis Cache           │
└─────────────────────────────────────────────────────┘
```

---

## 📦 Core Entities

1. **User**: Person using the system
2. **Group**: Collection of users for organizing expenses
3. **Expense**: Money spent by someone, split among participants
4. **Transaction**: Settlement payment from payer to payee

**Relationships**:
- User ↔ Group (Many-to-Many)
- Group → Expense (One-to-Many)
- User → Expense (One-to-Many as payer, Many-to-Many as participants)
- User → Transaction (One-to-Many as payer/payee)

---

## 🎨 Design Patterns Used

### 1. Strategy Pattern
**Where**: Split calculation (EQUAL, EXACT, PERCENTAGE)
**Why**: Easy to add new split types without modifying existing code
**Benefit**: Open/Closed Principle

### 2. Repository Pattern
**Where**: Data access layer (UserRepository, ExpenseRepository)
**Why**: Abstraction over database operations
**Benefit**: Easy to swap implementations (JPA ↔ In-Memory for testing)

### 3. Factory Pattern
**Where**: Creating split strategies
**Why**: Centralized object creation
**Benefit**: Single place to manage strategy creation

### 4. Dependency Injection
**Where**: All services
**Why**: Loose coupling, testability
**Benefit**: Can inject mocks for testing

---

## 🎯 SOLID Principles Applied

| Principle | Application |
|-----------|-------------|
| **S**ingle Responsibility | UserService only handles users, GroupService only handles groups |
| **O**pen/Closed | Strategy pattern allows adding new split types without modifying code |
| **L**iskov Substitution | Can swap JpaRepository with InMemoryRepository transparently |
| **I**nterface Segregation | Separate interfaces for read/write operations |
| **D**ependency Inversion | Services depend on repository interfaces, not concrete classes |

---

## 🔒 Concurrency Strategy

| Operation | Strategy | Reason |
|-----------|----------|--------|
| Add Expense | Database Transaction + Validation | Ensure atomicity |
| Update Expense | Optimistic Locking (@Version) | Handle conflicts, better performance |
| Settle Up | Pessimistic Locking (SELECT FOR UPDATE) | Prevent double settlement |
| Calculate Balance | Read-only (no lock) | Performance |
| Duplicate Prevention | Idempotency Key | Handle double-clicks |

---

## 🗄️ Database Schema

**Tables**: 7 tables
1. `users` - User accounts
2. `groups` - Expense groups
3. `user_groups` - Join table (User ↔ Group)
4. `expenses` - Expenses
5. `expense_splits` - Individual splits
6. `transactions` - Settlements
7. `user_balances` (optional) - Denormalized balances for performance

**Key Indexes**:
- `idx_user_email` (for login)
- `idx_expense_group` (for group expense queries)
- `idx_transaction_user` (for user transaction history)

---

## ⚡ Performance Optimizations

1. **Caching**: Redis cache for user balances (5 min TTL)
2. **Indexes**: On all foreign keys and frequently queried columns
3. **Async Notifications**: RabbitMQ for email/push notifications
4. **Connection Pooling**: HikariCP for optimal database connections
5. **Lazy Loading**: Load group members only when needed

---

## 📈 Scalability Considerations

**Current Design** (handles 100K users):
- Single MySQL database
- Redis cache for hot data
- Vertical scaling (bigger server)

**Future Enhancements** (for 10M+ users):
1. **Database Sharding**: Shard by user_id
2. **Read Replicas**: Separate read/write databases
3. **CDN**: For static assets
4. **Microservices**: Split into User Service, Expense Service, etc.
5. **Event-Driven**: Use Kafka for async processing
6. **NoSQL**: Consider Cassandra for write-heavy operations

---

## 🚀 API Endpoints

### User Management
- `POST /api/v1/users` - Register user
- `POST /api/v1/auth/login` - Login
- `GET /api/v1/users/{id}` - Get user profile
- `PUT /api/v1/users/{id}` - Update profile

### Group Management
- `POST /api/v1/groups` - Create group
- `GET /api/v1/groups/{id}` - Get group details
- `POST /api/v1/groups/{id}/members` - Add member
- `DELETE /api/v1/groups/{id}/members/{userId}` - Remove member

### Expense Management
- `POST /api/v1/expenses` - Add expense
- `GET /api/v1/expenses/{id}` - Get expense details
- `PUT /api/v1/expenses/{id}` - Update expense
- `DELETE /api/v1/expenses/{id}` - Delete expense
- `GET /api/v1/groups/{id}/expenses` - Get group expenses

### Balance & Settlement
- `GET /api/v1/users/{id}/balances` - Get user balances
- `GET /api/v1/groups/{id}/balances` - Get group balances
- `POST /api/v1/transactions` - Record payment
- `POST /api/v1/transactions/settle-up` - Settle all balances

---

## ❌ Common Mistakes to Avoid

1. **Using double for money** → Use BigDecimal
2. **No transaction management** → Wrap operations in @Transactional
3. **Ignoring concurrency** → Add locking strategies
4. **Storing plain passwords** → Hash with bcrypt
5. **No validation** → Validate all inputs
6. **Fat entities** → Move logic to services
7. **Tight coupling** → Use dependency injection
8. **No indexes** → Add indexes on foreign keys
9. **Synchronous notifications** → Use async/queue
10. **No error handling** → Handle exceptions gracefully

---

## ✅ What to Draw First (Whiteboard Interview)

**Time: 15-20 minutes**

1. **Requirements** (2 min): List FR & NFR
2. **Entities** (3 min): User, Group, Expense, Transaction boxes
3. **Relationships** (3 min): Connect entities with cardinality
4. **Class Diagram** (4 min): Add key attributes and methods
5. **Service Layer** (2 min): UserService, ExpenseService, etc.
6. **Database Schema** (3 min): Key tables and relationships
7. **Scalability** (2 min): Caching, async, sharding

**Order matters!** Start high-level, add details gradually.

---

## 🎓 Key Learnings

### For Beginners:
1. **Start simple**: 4 entities (User, Group, Expense, Transaction)
2. **Separate concerns**: Entities = data, Services = logic
3. **Use patterns**: Strategy, Repository, Factory
4. **Think relationships**: 1:1, 1:*, *:*
5. **Always use BigDecimal** for money!

### For Intermediate:
1. **Apply SOLID**: Makes code maintainable
2. **Handle concurrency**: Transactions, locking, idempotency
3. **Optimize wisely**: Measure first, then optimize
4. **Design for testability**: Dependency injection
5. **Think scalability**: Cache, async, sharding

### For Advanced:
1. **Trade-offs matter**: Document why you chose X over Y
2. **Consistency vs Performance**: Know when to compromise
3. **Event-driven architecture**: For loose coupling
4. **Observability**: Logs, metrics, traces
5. **Failure scenarios**: What if DB is down?

---

## 📚 Further Reading

1. **Books**:
   - "Designing Data-Intensive Applications" - Martin Kleppmann
   - "Clean Architecture" - Robert C. Martin
   - "Domain-Driven Design" - Eric Evans

2. **Patterns**:
   - Strategy Pattern
   - Repository Pattern
   - CQRS (Command Query Responsibility Segregation)

3. **Scalability**:
   - Database Sharding
   - CAP Theorem
   - Eventual Consistency

---

## 🎯 Success Metrics

**Functional**:
- ✅ Users can add expenses (3 split types)
- ✅ Balances calculated correctly
- ✅ Settlements recorded
- ✅ No duplicate expenses

**Non-Functional**:
- ✅ 99.9% uptime
- ✅ < 200ms response time (95th percentile)
- ✅ Handles 1000 concurrent users
- ✅ Zero financial data loss

---

## 🏁 Conclusion

This design balances:
- **Simplicity**: Easy to understand and extend
- **Robustness**: Handles concurrency and failures
- **Performance**: Optimized with caching
- **Maintainability**: SOLID principles, design patterns
- **Scalability**: Can scale to millions of users with enhancements

**Remember**: Start simple, iterate, optimize based on real needs!

---

**Total Files Created**: 20+
**Lines of Documentation**: 5000+
**Diagrams**: 15+ (Mermaid)
**Code Examples**: 50+

**Your LLD is interview-ready!** 🚀
