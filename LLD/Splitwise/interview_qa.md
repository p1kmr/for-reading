# 🎤 Interview Q&A - Splitwise System Design

## 1. System Design Questions

### Q1: Design a Splitwise system for splitting expenses among users

**Answer Structure**:

**1. Requirements Clarification** (2 min)
- "Let me clarify the requirements. Users can create groups, add expenses, and settle balances, correct?"
- "What split types do we need? Equal, exact, percentage?"
- "Do we need to handle multiple currencies?"
- "What's the scale? 1M users, 10M expenses per day?"

**2. Core Entities** (3 min)
- User, Group, Expense, Transaction
- Draw boxes and relationships
- Mention cardinality (1:*, *:*)

**3. API Design** (2 min)
```
POST /users - Register
POST /groups - Create group
POST /groups/{id}/members - Add member
POST /expenses - Add expense
GET /users/{id}/balance - Get balance
POST /transactions - Settle up
```

**4. Database Schema** (2 min)
- Tables: users, groups, user_groups, expenses, expense_splits, transactions
- Mention indexes: idx_user_email, idx_expense_group

**5. Design Patterns** (2 min)
- Strategy Pattern for split types
- Repository Pattern for data access
- Factory Pattern for creating strategies

**6. Scalability** (2 min)
- Cache balances in Redis (TTL 5 min)
- Async notifications using RabbitMQ
- Database sharding by user_id

**7. Trade-offs** (1 min)
- Calculated vs stored balances (chose calculated for consistency)
- Optimistic vs pessimistic locking (chose optimistic for performance)

---

### Q2: How do you calculate balances efficiently?

**Answer**:

```java
public List<Balance> calculateUserBalance(String userId) {
    // 1. Get all expenses where user is participant
    List<Expense> expenses = expenseRepository.findByParticipant(userId);

    Map<String, BigDecimal> balances = new HashMap<>();

    // 2. For each expense, calculate what user owes/is owed
    for (Expense expense : expenses) {
        String payer = expense.getPaidBy();
        BigDecimal userShare = expense.getSplits().get(userId);

        if (payer.equals(userId)) {
            // User paid, others owe user
            for (Map.Entry<String, BigDecimal> split : expense.getSplits().entrySet()) {
                if (!split.getKey().equals(userId)) {
                    // Other person owes user
                    balances.merge(split.getKey(),
                                   split.getValue().negate(),  // Negative = owed
                                   BigDecimal::add);
                }
            }
        } else {
            // User owes payer
            balances.merge(payer, userShare, BigDecimal::add);
        }
    }

    // 3. Subtract transactions (settlements)
    List<Transaction> transactions = transactionRepository.findByUser(userId);
    for (Transaction txn : transactions) {
        if (txn.getPayerId().equals(userId)) {
            // User paid someone
            balances.merge(txn.getPayeeId(),
                           txn.getAmount().negate(),
                           BigDecimal::add);
        } else {
            // User received payment
            balances.merge(txn.getPayerId(),
                           txn.getAmount(),
                           BigDecimal::add);
        }
    }

    // 4. Convert to Balance objects
    return balances.entrySet().stream()
        .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) != 0)
        .map(e -> new Balance(e.getKey(), e.getValue()))
        .collect(Collectors.toList());
}
```

**Optimization**:
- Cache result in Redis for 5 minutes
- Invalidate cache when expense/transaction added

**Time Complexity**: O(E + T) where E = expenses, T = transactions
**Space Complexity**: O(U) where U = users

---

### Q3: How do you handle concurrent expense additions?

**Answer**:

**Problem**: Two users simultaneously add expenses
- Race condition: Lost updates
- Inconsistent balances

**Solution 1: Database Transactions**
```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public Expense addExpense(ExpenseDTO dto) {
    // All operations in single transaction
    // COMMIT or ROLLBACK atomically
}
```

**Solution 2: Optimistic Locking for Updates**
```java
@Entity
public class Expense {
    @Version
    private Long version;  // Auto-incremented
}
```

**Solution 3: Pessimistic Locking for Settlements**
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT t FROM Transaction t WHERE t.payerId = :userId")
List<Transaction> findByUserIdWithLock(@Param("userId") String userId);
```

**Trade-off**:
- Optimistic: Better performance, handles conflicts
- Pessimistic: Slower, prevents conflicts

---

### Q4: How do you minimize transactions in "simplify debts"?

**Answer**:

**Problem**: In a group, minimize number of settlements needed

**Example**:
```
Before:
- Alice owes Bob ₹100
- Bob owes Charlie ₹100
- Charlie owes David ₹100
Total: 3 transactions

After simplification:
- Alice owes David ₹100
Total: 1 transaction! 🎉
```

**Algorithm**: Greedy approach

```java
public List<Transaction> simplifyDebts(String groupId) {
    // 1. Calculate net balance for each user
    Map<String, BigDecimal> netBalances = calculateNetBalances(groupId);

    // 2. Separate creditors (positive) and debtors (negative)
    List<User> creditors = new ArrayList<>();
    List<User> debtors = new ArrayList<>();

    for (Map.Entry<String, BigDecimal> entry : netBalances.entrySet()) {
        if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
            creditors.add(new User(entry.getKey(), entry.getValue()));
        } else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
            debtors.add(new User(entry.getKey(), entry.getValue().abs()));
        }
    }

    // 3. Match debtors to creditors
    List<Transaction> simplified = new ArrayList<>();

    int i = 0, j = 0;
    while (i < debtors.size() && j < creditors.size()) {
        User debtor = debtors.get(i);
        User creditor = creditors.get(j);

        BigDecimal amount = debtor.amount.min(creditor.amount);

        // Create transaction
        simplified.add(new Transaction(debtor.id, creditor.id, amount));

        // Update remaining amounts
        debtor.amount = debtor.amount.subtract(amount);
        creditor.amount = creditor.amount.subtract(amount);

        if (debtor.amount.compareTo(BigDecimal.ZERO) == 0) i++;
        if (creditor.amount.compareTo(BigDecimal.ZERO) == 0) j++;
    }

    return simplified;
}
```

**Complexity**: O(N log N) where N = number of users

---

## 2. Java/OOP Questions

### Q5: Why use BigDecimal instead of double for money?

**Answer**:

```java
// ❌ WRONG: Using double
double price1 = 0.1;
double price2 = 0.2;
double total = price1 + price2;
System.out.println(total);  // 0.30000000000000004 😱

// ✅ RIGHT: Using BigDecimal
BigDecimal price1 = new BigDecimal("0.1");
BigDecimal price2 = new BigDecimal("0.2");
BigDecimal total = price1.add(price2);
System.out.println(total);  // 0.3 ✅
```

**Reason**:
- `double` uses binary floating-point (IEEE 754)
- Cannot represent 0.1 exactly in binary
- Leads to precision errors in calculations
- **BigDecimal** uses decimal representation (exact)

**Best Practice**:
- Always use `BigDecimal` for money
- Use `String` constructor: `new BigDecimal("0.1")`
- Not `double` constructor: `new BigDecimal(0.1)` ❌ (still has precision error!)

---

### Q6: Explain Strategy Pattern with code example

**Answer**:

**Definition**: Define a family of algorithms, encapsulate each one, make them interchangeable

**Example**: Split calculation strategies

```java
// 1. Strategy interface
public interface SplitStrategy {
    Map<String, BigDecimal> calculateSplit(BigDecimal amount,
                                           List<String> participants);
}

// 2. Concrete strategies
public class EqualSplitStrategy implements SplitStrategy {
    @Override
    public Map<String, BigDecimal> calculateSplit(BigDecimal amount,
                                                  List<String> participants) {
        int count = participants.size();
        BigDecimal splitAmount = amount.divide(
            new BigDecimal(count), 2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> splits = new HashMap<>();
        for (String userId : participants) {
            splits.put(userId, splitAmount);
        }
        return splits;
    }
}

public class ExactSplitStrategy implements SplitStrategy {
    @Override
    public Map<String, BigDecimal> calculateSplit(...) {
        // Use provided exact amounts
    }
}

// 3. Context class
public class SplitContext {
    private SplitStrategy strategy;

    public void setStrategy(SplitStrategy strategy) {
        this.strategy = strategy;
    }

    public Map<String, BigDecimal> executeSplit(...) {
        return strategy.calculateSplit(...);
    }
}

// 4. Usage
SplitContext context = new SplitContext();
context.setStrategy(new EqualSplitStrategy());
Map<String, BigDecimal> splits = context.executeSplit(new BigDecimal("3000"),
                                                       Arrays.asList("alice", "bob"));
```

**Benefits**:
- ✅ Easy to add new strategies (Open/Closed Principle)
- ✅ No if-else chains
- ✅ Each strategy is independent

---

### Q7: Difference between Aggregation and Composition?

**Answer**:

**Aggregation** (Has-A, weak):
- Whole and parts can exist independently
- Parts can outlive the whole

```java
// Group HAS Users (aggregation)
public class Group {
    private List<User> members;  // Users exist independently
}

// If group is deleted, users still exist ✅
```

**Composition** (Part-Of, strong):
- Parts cannot exist without whole
- Parts are destroyed when whole is destroyed

```java
// Expense CONTAINS Splits (composition)
public class Expense {
    private List<Split> splits;  // Splits are part of expense
}

// If expense is deleted, splits are deleted too ✅
```

**UML Notation**:
- Aggregation: Hollow diamond ◇—
- Composition: Filled diamond ♦—

---

### Q8: Explain Dependency Injection and its benefits

**Answer**:

**Definition**: Providing dependencies from outside rather than creating them inside

```java
// ❌ BAD: Creating dependency inside (tight coupling)
public class UserService {
    private UserRepository repository = new JpaUserRepository();

    public void registerUser(User user) {
        repository.save(user);
    }
}

// ✅ GOOD: Dependency injection (loose coupling)
public class UserService {
    private final UserRepository repository;

    // Constructor injection
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public void registerUser(User user) {
        repository.save(user);
    }
}

// Usage with Spring
@Service
public class UserService {
    private final UserRepository repository;

    @Autowired  // Spring injects automatically
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

**Benefits**:
1. **Testability**: Can inject mock repository for testing
2. **Flexibility**: Can swap implementations (JPA → In-Memory)
3. **Loose Coupling**: Service doesn't know concrete implementation
4. **Single Responsibility**: Service doesn't create dependencies

**Example Test**:
```java
@Test
public void testRegisterUser() {
    // Inject mock repository
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    UserService service = new UserService(mockRepo);

    User user = new User("Alice", "alice@example.com");
    service.registerUser(user);

    // Verify mock was called
    Mockito.verify(mockRepo).save(user);
}
```

---

## 3. Database Questions

### Q9: Design database schema for Splitwise

**Answer**:

```sql
-- Users table
CREATE TABLE users (
    user_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(15),
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
);

-- Groups table
CREATE TABLE groups (
    group_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_by VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- User-Group join table (Many-to-Many)
CREATE TABLE user_groups (
    user_id VARCHAR(36),
    group_id VARCHAR(36),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, group_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (group_id) REFERENCES groups(group_id)
);

-- Expenses table
CREATE TABLE expenses (
    expense_id VARCHAR(36) PRIMARY KEY,
    description VARCHAR(200) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    paid_by VARCHAR(36) NOT NULL,
    group_id VARCHAR(36),
    split_type ENUM('EQUAL', 'EXACT', 'PERCENTAGE') NOT NULL,
    expense_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (paid_by) REFERENCES users(user_id),
    FOREIGN KEY (group_id) REFERENCES groups(group_id),
    INDEX idx_group_id (group_id),
    INDEX idx_paid_by (paid_by),
    INDEX idx_expense_date (expense_date)
);

-- Expense splits table (who owes how much)
CREATE TABLE expense_splits (
    expense_id VARCHAR(36),
    user_id VARCHAR(36),
    amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
    PRIMARY KEY (expense_id, user_id),
    FOREIGN KEY (expense_id) REFERENCES expenses(expense_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_user_id (user_id)
);

-- Transactions table (settlements)
CREATE TABLE transactions (
    transaction_id VARCHAR(36) PRIMARY KEY,
    payer_id VARCHAR(36) NOT NULL,
    payee_id VARCHAR(36) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    group_id VARCHAR(36),
    status ENUM('PENDING', 'COMPLETED', 'CANCELLED') DEFAULT 'COMPLETED',
    notes TEXT,
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payer_id) REFERENCES users(user_id),
    FOREIGN KEY (payee_id) REFERENCES users(user_id),
    FOREIGN KEY (group_id) REFERENCES groups(group_id),
    CHECK (payer_id != payee_id),
    INDEX idx_payer_id (payer_id),
    INDEX idx_payee_id (payee_id),
    INDEX idx_transaction_date (transaction_date)
);
```

**Key Design Decisions**:
1. **UUID for IDs**: Globally unique, better for distributed systems
2. **DECIMAL(10,2)**: Exact decimal for money
3. **Indexes**: On foreign keys and date columns for fast queries
4. **CHECK constraints**: Ensure data validity (amount > 0, payer ≠ payee)
5. **ON DELETE CASCADE**: Delete splits when expense is deleted
6. **ENUM**: For split_type and status (type safety)

---

### Q10: How do you optimize balance calculation queries?

**Answer**:

**Strategy 1: Caching**
```java
@Cacheable(value = "userBalances", key = "#userId")
public List<Balance> getUserBalance(String userId) {
    // Expensive calculation
    // Result cached in Redis for 5 minutes
}

@CacheEvict(value = "userBalances", key = "#expense.paidBy")
public Expense addExpense(Expense expense) {
    // Invalidate cache when expense added
}
```

**Strategy 2: Denormalization (Pre-calculated balances)**
```sql
CREATE TABLE user_balances (
    user_id VARCHAR(36),
    other_user_id VARCHAR(36),
    net_balance DECIMAL(10, 2),
    last_updated TIMESTAMP,
    PRIMARY KEY (user_id, other_user_id)
);
```

**Trade-off**:
- **Calculated on-the-fly**: Always consistent, slower queries
- **Cached/Denormalized**: Faster queries, risk of stale data

**Recommendation**: Start with calculation, add caching if needed

---

## ✅ Interview Tips

1. **Clarify requirements first**: Don't assume!
2. **Start simple**: MVP first, then optimizations
3. **Explain trade-offs**: Show you understand pros/cons
4. **Use examples**: "For instance, if Alice adds expense..."
5. **Draw diagrams**: Visual aids help!
6. **Mention SOLID**: Shows you know best practices
7. **Discuss scalability**: "To handle 1M users, we'd..."
8. **Ask questions**: "Would you like me to dive deeper into..."

**Remember**: Interview is a conversation, not an interrogation!
