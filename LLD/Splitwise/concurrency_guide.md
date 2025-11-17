# 🔒 Concurrency & Consistency in Splitwise

## 🎯 Why Concurrency Matters?

### Scenario: Race Condition

**Problem**: Alice and Bob simultaneously add expenses in "Goa Trip"

```
Time    Alice's Request                 Bob's Request
─────────────────────────────────────────────────────────────
T1      Read balance: Alice-Bob = ₹0    Read balance: Alice-Bob = ₹0
T2      Add expense: ₹3000
T3      Calculate: Bob owes ₹1000       Add expense: ₹2000
T4      Write: Bob owes ₹1000           Calculate: Alice owes ₹1000
T5                                      Write: Alice owes ₹1000
```

**Result**: Only Bob's expense is saved! Alice's expense is lost! ❌

**Expected**: Bob owes Alice ₹1000, Alice owes Bob ₹1000 → Net = ₹0

---

## 🔐 Solutions

### 1. Database Transactions (ACID)

```java
@Service
@Transactional  // Spring annotation
public class ExpenseService {

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Expense addExpense(ExpenseDTO dto) {
        // BEGIN TRANSACTION
        try {
            // 1. Validate
            // 2. Calculate splits
            Expense expense = new Expense(...);

            // 3. Save expense
            expenseRepository.save(expense);

            // 4. Save splits
            for (Map.Entry<String, BigDecimal> split : expense.getSplits().entrySet()) {
                expenseSplitRepository.save(new ExpenseSplit(
                    expense.getExpenseId(),
                    split.getKey(),
                    split.getValue()
                ));
            }

            // COMMIT TRANSACTION
            return expense;
        } catch (Exception e) {
            // ROLLBACK TRANSACTION
            throw e;
        }
    }
}
```

**Benefits**:
- ✅ All-or-nothing (atomicity)
- ✅ Consistent state
- ✅ Isolated from other transactions

---

### 2. Optimistic Locking

**Use Case**: Update expense

```java
@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    private String expenseId;

    private BigDecimal amount;

    // Version field for optimistic locking
    @Version
    private Long version;  // Starts at 0, incremented on each update

    // ... other fields
}
```

**How it works**:

```
Time    Alice's Update              Bob's Update
─────────────────────────────────────────────────────────
T1      Read expense (version=1)    Read expense (version=1)
T2      Modify amount: ₹3000→₹3200
T3      Save with version=1         Modify amount: ₹3000→₹3500
T4      DB: Update WHERE            Save with version=1
        version=1 → Success!
        New version=2
T5                                  DB: Update WHERE
                                    version=1 → FAIL! ❌
                                    (version is now 2)
```

**Code**:

```java
public Expense updateExpense(String expenseId, ExpenseDTO dto) {
    // Read expense (version included)
    Expense expense = expenseRepository.findById(expenseId)
        .orElseThrow(() -> new NotFoundException("Expense not found"));

    // Modify
    expense.setAmount(dto.getAmount());
    expense.setDescription(dto.getDescription());

    try {
        // Save (JPA checks version automatically)
        expenseRepository.save(expense);
        // Version incremented to 2
        return expense;
    } catch (OptimisticLockException e) {
        // Someone else updated the expense!
        throw new ConcurrentModificationException(
            "Expense was modified by another user. Please refresh and try again.");
    }
}
```

**SQL Generated**:
```sql
UPDATE expenses
SET amount = ?, description = ?, version = version + 1
WHERE expense_id = ? AND version = ?
-- If version doesn't match, UPDATE affects 0 rows → Exception
```

**Benefits**:
- ✅ No locks (better performance)
- ✅ Detects conflicts
- ✅ User can retry

**When to use**: Updates to existing expenses

---

### 3. Pessimistic Locking

**Use Case**: Settle up (must prevent concurrent settlements)

```java
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Transaction t WHERE t.payerId = :userId OR t.payeeId = :userId")
    List<Transaction> findByUserIdWithLock(@Param("userId") String userId);
}
```

**How it works**:

```
Time    Alice's Settlement          Bob's Settlement (for same balance)
──────────────────────────────────────────────────────────────────────
T1      BEGIN TRANSACTION
        SELECT ... FOR UPDATE       BEGIN TRANSACTION
        (Lock acquired)             SELECT ... FOR UPDATE
T2                                  (Waits for Alice's lock...)
T3      Calculate balance
T4      Create transaction
T5      COMMIT (Lock released)
T6                                  (Lock acquired now)
                                    Calculate balance (updated)
T7                                  Create transaction
T8                                  COMMIT
```

**Code**:

```java
@Transactional
public Transaction settleUp(String payerId, String payeeId, BigDecimal amount) {
    // Acquire lock on user's transactions
    List<Transaction> userTransactions = transactionRepository
        .findByUserIdWithLock(payerId);

    // Calculate current balance (with lock held)
    BigDecimal currentBalance = balanceService.calculateBalance(payerId, payeeId);

    // Validate
    if (amount.compareTo(currentBalance) > 0) {
        throw new ExcessivePaymentException("Amount exceeds balance");
    }

    // Create transaction
    Transaction txn = new Transaction();
    txn.setPayerId(payerId);
    txn.setPayeeId(payeeId);
    txn.setAmount(amount);
    txn.setStatus(TransactionStatus.COMPLETED);

    transactionRepository.save(txn);

    // Lock released on commit
    return txn;
}
```

**SQL Generated**:
```sql
SELECT * FROM transactions
WHERE payer_id = ? OR payee_id = ?
FOR UPDATE;  -- Locks selected rows
```

**Benefits**:
- ✅ Prevents concurrent modifications
- ✅ Guaranteed consistency

**Drawbacks**:
- ❌ Performance impact (other users must wait)
- ❌ Risk of deadlock

**When to use**: Critical operations (settlements, balance updates)

---

## 4. Database Constraints

### Prevent Invalid Data

```sql
-- Ensure amount is positive
ALTER TABLE expenses
ADD CONSTRAINT chk_positive_amount
CHECK (amount > 0);

-- Ensure payer ≠ payee in transactions
ALTER TABLE transactions
ADD CONSTRAINT chk_different_users
CHECK (payer_id != payee_id);

-- Unique email
ALTER TABLE users
ADD CONSTRAINT uk_email
UNIQUE (email);
```

---

## 5. Idempotency

**Problem**: User clicks "Add Expense" twice (double-click)

**Solution**: Idempotency key

```java
@PostMapping("/expenses")
public ResponseEntity<Expense> addExpense(
        @RequestBody ExpenseDTO dto,
        @RequestHeader("Idempotency-Key") String idempotencyKey) {

    // Check if request already processed
    Optional<Expense> existing = expenseRepository
        .findByIdempotencyKey(idempotencyKey);

    if (existing.isPresent()) {
        // Return existing expense (don't create duplicate)
        return ResponseEntity.ok(existing.get());
    }

    // Process request
    Expense expense = expenseService.addExpense(dto);
    expense.setIdempotencyKey(idempotencyKey);
    expenseRepository.save(expense);

    return ResponseEntity.status(201).body(expense);
}
```

**Client sends**:
```http
POST /expenses
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000

{
  "description": "Hotel",
  "amount": 6000,
  ...
}
```

**Benefits**:
- ✅ Safe to retry
- ✅ No duplicate expenses

---

## 🎯 Concurrency Strategy Summary

| Operation | Strategy | Reason |
|-----------|----------|--------|
| **Add Expense** | Transaction + Validation | Ensure atomic save |
| **Update Expense** | Optimistic Locking | Handle conflicts |
| **Settle Up** | Pessimistic Locking | Prevent double settlement |
| **Calculate Balance** | Read-only (no lock) | Performance |
| **Double-click** | Idempotency Key | Prevent duplicates |

---

## 🔥 Advanced: Deadlock Prevention

### Deadlock Scenario

```
Time    Transaction A               Transaction B
──────────────────────────────────────────────────────────
T1      Lock User Alice             Lock User Bob
T2      (Processing...)             (Processing...)
T3      Try to lock User Bob        Try to lock User Alice
        (Waits for B...)            (Waits for A...)
        ⚠️ DEADLOCK!
```

### Solution: Lock Ordering

```java
public Transaction settleUp(String payerId, String payeeId, BigDecimal amount) {
    // Always lock users in alphabetical order
    String firstId = payerId.compareTo(payeeId) < 0 ? payerId : payeeId;
    String secondId = payerId.compareTo(payeeId) < 0 ? payeeId : payerId;

    // Lock in order
    lockUser(firstId);
    lockUser(secondId);

    // ... settlement logic

    // Unlock in reverse order
    unlockUser(secondId);
    unlockUser(firstId);
}
```

---

## ✅ Best Practices

1. **Keep transactions short**: Minimize lock duration
2. **Use appropriate isolation level**: READ_COMMITTED for most cases
3. **Handle conflicts gracefully**: Show user-friendly error messages
4. **Test concurrency**: Use JMeter, Gatling for load testing
5. **Monitor deadlocks**: Log and alert on database deadlocks
6. **Use connection pooling**: HikariCP for optimal performance

---

## 🏋️ Interview Question

**Q**: "How would you handle two users simultaneously adding expenses that involve each other?"

**Good Answer**:
> "I'd use database transactions with READ_COMMITTED isolation level for adding expenses. Each expense addition runs in a transaction, ensuring atomicity. For balance calculations, I'd use optimistic locking with a version field to detect conflicts. For settlements, I'd use pessimistic locking (SELECT FOR UPDATE) to prevent double payments. I'd also implement idempotency using request IDs to prevent duplicate expenses from double-clicks. Finally, I'd establish lock ordering to prevent deadlocks."

---

**Key Takeaway**: Use the right concurrency control for each operation!
