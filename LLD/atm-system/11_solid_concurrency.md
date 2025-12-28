# SOLID Principles & Concurrency (Phase 9)

> **Purpose**: Understand how SOLID principles are applied and handle concurrent access

---

## SOLID Principles in ATM System

### 1. Single Responsibility Principle (SRP)

**Definition**: A class should have only one reason to change

**Examples in our design**:

| Class | Single Responsibility | What it does NOT do |
|-------|----------------------|---------------------|
| `Account` | Manage account balance | ❌ Validate PIN, ❌ Dispense cash |
| `AuthenticationService` | Authenticate users | ❌ Process transactions, ❌ Print receipts |
| `TransactionService` | Process transactions | ❌ Validate PIN, ❌ Read card |
| `CardReader` | Read card hardware | ❌ Validate PIN, ❌ Check balance |

```java
// ❌ WRONG - Multiple responsibilities
class Account {
    private double balance;

    public void debit(double amount) { ... }  // OK
    public boolean validatePin(String pin) { ... }  // ❌ Not account's job
    public void dispenseCash(double amount) { ... }  // ❌ Not account's job
}

// ✅ RIGHT - Single responsibility
class Account {
    private double balance;
    public void debit(double amount) { ... }  // Only balance management
}

class AuthenticationService {
    public boolean validatePin(Card card, String pin) { ... }  // PIN validation
}
```

---

### 2. Open/Closed Principle (OCP)

**Definition**: Open for extension, closed for modification

**Example**: Adding new transaction types

```java
// ❌ WRONG - Need to modify existing code for new transaction type
class TransactionService {
    public Transaction process(TransactionType type, ...) {
        if (type == WITHDRAWAL) {
            // withdrawal logic
        } else if (type == DEPOSIT) {
            // deposit logic
        } else if (type == FUND_TRANSFER) {  // NEW type - modified existing code!
            // fund transfer logic
        }
    }
}

// ✅ RIGHT - Open for extension via Strategy pattern
interface TransactionStrategy {
    Transaction execute(Account account, double amount, ATM atm);
}

class WithdrawalStrategy implements TransactionStrategy { ... }
class DepositStrategy implements TransactionStrategy { ... }
class FundTransferStrategy implements TransactionStrategy { ... }  // NEW - no modification!

class TransactionService {
    private Map<TransactionType, TransactionStrategy> strategies;

    // Add new strategy without modifying existing code
    public void registerStrategy(TransactionType type, TransactionStrategy strategy) {
        strategies.put(type, strategy);
    }
}
```

---

### 3. Liskov Substitution Principle (LSP)

**Definition**: Subtypes must be substitutable for their base types

**Example**: Repository implementations

```java
// ✅ RIGHT - Any implementation can replace interface
AccountRepository repo;

// Production: use JPA
repo = new JpaAccountRepository(entityManager);
service.save(account);  // Works!

// Testing: use InMemory
repo = new InMemoryAccountRepository();
service.save(account);  // Still works! Same behavior

// Both implementations honor the contract
```

**Violation example**:
```java
// ❌ WRONG - Violates LSP
class InMemoryAccountRepository implements AccountRepository {
    @Override
    public void save(Account account) {
        throw new UnsupportedOperationException("Can't save in memory");
        // Violates contract! Interface says save() should work
    }
}

// ✅ RIGHT - Honor the contract
class InMemoryAccountRepository implements AccountRepository {
    private Map<String, Account> accounts = new HashMap<>();

    @Override
    public void save(Account account) {
        accounts.put(account.getAccountNumber(), account);  // Actually saves!
    }
}
```

---

### 4. Interface Segregation Principle (ISP)

**Definition**: Clients shouldn't depend on interfaces they don't use

**Example**: Separate hardware interfaces

```java
// ❌ WRONG - Fat interface
interface ATMHardware {
    Card readCard();
    void dispenseCash(Map<Integer, Integer> cash);
    void printReceipt(Transaction txn);
    String readPin();
    void displayMessage(String msg);
    // Too many methods! Classes implementing this must implement ALL
}

// ✅ RIGHT - Segregated interfaces
interface CardReader {
    Card readCard();
    void ejectCard();
}

interface CashDispenser {
    void dispenseCash(Map<Integer, Integer> cash);
}

interface ReceiptPrinter {
    void printReceipt(Transaction txn);
}

interface Keypad {
    String readPin();
}

interface Screen {
    void displayMessage(String msg);
}

// Now classes depend only on what they need
class AuthenticationService {
    private CardReader cardReader;  // Only needs CardReader
    private Keypad keypad;          // Only needs Keypad
    // Doesn't depend on CashDispenser or ReceiptPrinter
}
```

---

### 5. Dependency Inversion Principle (DIP)

**Definition**: Depend on abstractions, not concretions

**Example**: BankingService dependency

```java
// ❌ WRONG - Depends on concrete implementation
class TransactionService {
    private RealBankingService bankingService = new RealBankingService();
    // Tightly coupled to RealBankingService
    // Can't use mock for testing
}

// ✅ RIGHT - Depends on interface
class TransactionService {
    private BankingService bankingService;  // Interface

    // Dependency Injection via constructor
    public TransactionService(BankingService bankingService) {
        this.bankingService = bankingService;
    }
}

// Production
TransactionService service = new TransactionService(new RealBankingService());

// Testing
TransactionService service = new TransactionService(new MockBankingService());
```

---

## Concurrency & Consistency

### Problem 1: Concurrent Withdrawals from Same Account

**Scenario**: Two ATMs trying to withdraw from same account simultaneously

```
Initial Balance: ₹10,000

ATM-1: Withdraw ₹8,000 (checks balance = ₹10,000 ✅)
ATM-2: Withdraw ₹8,000 (checks balance = ₹10,000 ✅)

ATM-1: Deducts ₹8,000 → Balance = ₹2,000
ATM-2: Deducts ₹8,000 → Balance = -₹6,000 ❌ OVERDRAFT!
```

### Solution 1: Pessimistic Locking (Database)

```java
/**
 * Use database row-level locking
 */
@Transactional
public Transaction withdraw(Card card, Account account, double amount, ATM atm) {
    // Lock the account row for this transaction
    // Other transactions wait until this completes

    Account lockedAccount = accountRepository.findByIdWithLock(account.getAccountNumber());
    // SQL: SELECT * FROM accounts WHERE account_number = ? FOR UPDATE

    if (lockedAccount.getBalance() >= amount) {
        lockedAccount.debit(amount);
        accountRepository.save(lockedAccount);
        // Lock released after commit
        return new Transaction(..., SUCCESS);
    }

    return new Transaction(..., FAILED);
}
```

**SQL Example**:
```sql
-- Transaction 1 (ATM-1)
BEGIN TRANSACTION;
SELECT * FROM accounts WHERE account_number = 'ACC123' FOR UPDATE;
-- Row is LOCKED - ATM-2 waits here

UPDATE accounts SET balance = balance - 8000 WHERE account_number = 'ACC123';
COMMIT;  -- Lock released
```

### Solution 2: Optimistic Locking (Version Number)

```java
@Entity
public class Account {
    @Id
    private String accountNumber;
    private double balance;

    @Version  // JPA optimistic locking
    private int version;  // Auto-incremented on each update

    // getters/setters
}

@Transactional
public Transaction withdraw(...) {
    try {
        Account account = accountRepository.findById(accountNumber);
        // version = 5

        account.debit(amount);
        accountRepository.save(account);
        // UPDATE accounts SET balance = ?, version = 6 WHERE account_number = ? AND version = 5

        // If another transaction already updated (version changed to 6)
        // This update fails → OptimisticLockException

    } catch (OptimisticLockException e) {
        // Retry the transaction
        return retry();
    }
}
```

**How it works**:
```
ATM-1: Read account (version=5), balance=₹10,000
ATM-2: Read account (version=5), balance=₹10,000

ATM-1: Deduct ₹8,000, UPDATE ... WHERE version=5 → Success, version=6
ATM-2: Deduct ₹8,000, UPDATE ... WHERE version=5 → FAIL (version is now 6)

ATM-2: Retry → Read again (version=6, balance=₹2,000) → Insufficient balance → FAIL
```

---

### Problem 2: Cash Dispenser Concurrency

**Scenario**: ATM has ₹10,000. Two customers request ₹8,000 each.

```java
/**
 * Synchronized cash dispensing
 */
public class CashDispenser {
    private Map<Integer, Integer> cashBins;
    private final Object lock = new Object();

    public synchronized Map<Integer, Integer> dispenseCash(double amount) {
        synchronized (lock) {  // Only one thread at a time
            if (getTotalCash() < amount) {
                return null;  // Insufficient cash
            }

            // Calculate and dispense
            Map<Integer, Integer> plan = calculatePlan(amount);
            deductCash(plan);
            return plan;
        }
    }
}
```

---

### Problem 3: Transaction Atomicity

**Requirement**: Either ALL steps succeed, or ALL rollback

```java
@Transactional  // Spring annotation for DB transaction
public Transaction withdraw(Card card, Account account, double amount, ATM atm) {
    // Step 1: Debit account
    account.debit(amount);
    accountRepository.save(account);

    // Step 2: Dispense cash
    Map<Integer, Integer> dispensed = atm.dispenseCash(amount);
    if (dispensed == null) {
        throw new CashDispenseException("Dispense failed");
        // @Transactional automatically ROLLS BACK Step 1
    }

    // Step 3: Update backend
    boolean updated = bankingService.debitAccount(account.getAccountNumber(), amount);
    if (!updated) {
        throw new BackendException("Backend failed");
        // @Transactional automatically ROLLS BACK Steps 1 & 2
    }

    // All steps successful → COMMIT
    return new Transaction(..., SUCCESS);
}
```

---

### Comparison: Pessimistic vs Optimistic Locking

| Aspect | Pessimistic Locking | Optimistic Locking |
|--------|--------------------|--------------------|
| **When to use** | High contention | Low contention |
| **How** | Lock row (FOR UPDATE) | Version number |
| **Performance** | Slower (waiting) | Faster (no locks) |
| **Conflicts** | Prevented | Detected & retried |
| **Use case** | Withdraw (critical) | Balance inquiry (reads) |

---

## Common Beginner Mistakes

### Mistake 1: Not Handling Concurrency

```java
// ❌ WRONG - No locking
public void withdraw(Account account, double amount) {
    double balance = account.getBalance();  // Thread 1 reads: ₹10,000
    // Thread 2 reads: ₹10,000
    if (balance >= amount) {
        account.setBalance(balance - amount);  // Both threads deduct
    }
}

// ✅ RIGHT - Use database transactions
@Transactional
public void withdraw(Account account, double amount) {
    Account locked = repository.findByIdWithLock(account.getId());
    locked.debit(amount);
    repository.save(locked);
}
```

### Mistake 2: Not Rolling Back on Failure

```java
// ❌ WRONG - No rollback
public void withdraw(Account account, double amount) {
    account.debit(amount);  // Deducted
    atm.dispenseCash(amount);  // FAILS
    // Money deducted but no cash dispensed!
}

// ✅ RIGHT - Rollback on failure
@Transactional
public void withdraw(Account account, double amount) {
    account.debit(amount);
    if (!atm.dispenseCash(amount)) {
        throw new Exception();  // Triggers rollback
    }
}
```

---

## Interview Questions

**Q1: How do you handle race conditions in withdrawal?**
**A**: Use pessimistic locking (SELECT FOR UPDATE) or optimistic locking (@Version)

**Q2: What if two ATMs withdraw from same account simultaneously?**
**A**: Database transaction + row-level locking ensures only one succeeds at a time

**Q3: What is @Transactional annotation?**
**A**: Spring annotation that wraps method in database transaction. On exception, auto-rollback.

**Q4: Difference between synchronized and @Transactional?**
- `synchronized`: Java-level thread locking (single JVM)
- `@Transactional`: Database-level transaction (works across multiple ATMs/servers)

---

## Navigation
- [← Previous: Sequence Diagrams](10_sequence_diagrams.md)
- [→ Next: Final Documentation](12_final_complete_design.md)
