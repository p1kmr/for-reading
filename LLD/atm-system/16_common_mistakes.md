# Common Beginner Mistakes in ATM LLD

> **Purpose**: Learn from common mistakes and avoid them in your design

---

## Mistake 1: Putting Business Logic in Entities ❌

### Wrong Approach:
```java
class Account {
    private double balance;
    private ATM atm;
    private BankingService bankingService;

    // ❌ Account is doing too much!
    public void withdraw(double amount, ATM atm) {
        // Validate
        if (balance < amount) {
            throw new Exception("Insufficient balance");
        }

        // Debit
        balance -= amount;

        // Dispense cash (Account shouldn't know about ATM!)
        atm.dispenseCash(amount);

        // Update backend (Account shouldn't do this!)
        bankingService.debitAccount(accountNumber, amount);
    }
}
```

**Problems**:
- Violates Single Responsibility Principle
- Account has too many dependencies
- Hard to test
- Tight coupling

### Correct Approach:
```java
// ✅ Account is just a data holder
class Account {
    private double balance;

    public void debit(double amount) {
        if (balance < amount) {
            throw new InsufficientBalanceException();
        }
        balance -= amount;  // Simple operation
    }
}

// ✅ Service orchestrates
class TransactionService {
    public Transaction withdraw(Account account, double amount, ATM atm) {
        account.debit(amount);
        atm.dispenseCash(amount);
        bankingService.debitAccount(...);
    }
}
```

**Why Better**:
- Clear separation: Entities = data, Services = logic
- Easy to test (mock dependencies in service)
- Loose coupling

---

## Mistake 2: Not Using Enums for Status ❌

### Wrong Approach:
```java
class Card {
    private String status = "ACTIVE";  // ❌ Prone to typos!

    public void block() {
        status = "BLOKED";  // ❌ Typo! Runtime error
    }

    public boolean isActive() {
        return status.equals("active");  // ❌ Case mismatch!
    }
}
```

**Problems**:
- Typos cause runtime bugs
- No IDE autocomplete
- Invalid values possible ("UNKNOWN_STATUS")

### Correct Approach:
```java
enum CardStatus {
    ACTIVE, BLOCKED, EXPIRED, STOLEN
}

class Card {
    private CardStatus status = CardStatus.ACTIVE;  // ✅ Type-safe

    public void block() {
        status = CardStatus.BLOCKED;  // ✅ Compiler checks
    }

    public boolean isActive() {
        return status == CardStatus.ACTIVE;  // ✅ No typos possible
    }
}
```

**Why Better**:
- Compile-time checking
- IDE autocomplete
- Limited set of valid values

---

## Mistake 3: Storing PIN in ATM/Card ❌

### Wrong Approach:
```java
class Card {
    private String pin;  // ❌ NEVER DO THIS!

    public boolean validatePin(String enteredPin) {
        return this.pin.equals(enteredPin);  // ❌ Security risk!
    }
}
```

**Problems**:
- Major security vulnerability
- PIN can be logged
- PIN can be extracted from memory dumps
- Violates PCI DSS compliance

### Correct Approach:
```java
class Card {
    // ✅ NO PIN field at all!
}

class AuthenticationService {
    public boolean authenticateUser(Card card, String pin) {
        // ✅ Validate with secure backend
        return bankingService.validatePinWithBank(
            card.getCardNumber(),
            pin  // Encrypted in transit via HTTPS
        );
        // PIN never stored locally!
    }
}
```

**Why Better**:
- PIN never stored in ATM
- Validates with secure backend
- Encrypted in transit
- Complies with security standards

---

## Mistake 4: Not Handling Concurrent Access ❌

### Wrong Approach:
```java
public Transaction withdraw(Account account, double amount) {
    // ❌ No concurrency control!
    if (account.getBalance() >= amount) {
        // What if another ATM withdraws here?
        account.debit(amount);
        // Possible overdraft!
    }
}
```

**Problem**: Race condition
```
ATM-1: Read balance = ₹10,000
ATM-2: Read balance = ₹10,000  (both read simultaneously)
ATM-1: Deduct ₹8,000 → balance = ₹2,000
ATM-2: Deduct ₹8,000 → balance = -₹6,000  ❌ OVERDRAFT!
```

### Correct Approach:
```java
@Transactional  // ✅ Database transaction
public Transaction withdraw(Account account, double amount) {
    // ✅ Lock the account row
    Account locked = accountRepository.findByIdWithLock(account.getId());
    // SQL: SELECT * FROM accounts WHERE id = ? FOR UPDATE

    if (locked.getBalance() >= amount) {
        locked.debit(amount);
        accountRepository.save(locked);
    }
    // Lock released after commit
}
```

**Why Better**:
- Database ensures only one transaction at a time
- Prevents overdraft
- ACID guarantees

---

## Mistake 5: Not Validating Before Dispensing Cash ❌

### Wrong Approach:
```java
public void withdraw(Account account, double amount) {
    // ❌ No validation!
    account.debit(amount);
    atm.dispenseCash(amount);  // What if ATM has no cash?
}
```

**Problem**:
```
Account debited: -₹5,000 ✅
ATM has no cash: Dispense fails ❌
Result: Money deducted but no cash → Customer complaint!
```

### Correct Approach:
```java
public Transaction withdraw(Account account, double amount, ATM atm) {
    // ✅ Validate BEFORE debiting
    if (!account.hasSufficientBalance(amount)) {
        return failedTransaction("Insufficient balance");
    }

    if (!atm.hasSufficientCash(amount)) {
        return failedTransaction("ATM has insufficient cash");
    }

    // Only debit if all validations pass
    account.debit(amount);
    Map<Integer, Integer> dispensed = atm.dispenseCash(amount);

    if (dispensed == null) {
        // Rollback
        account.credit(amount);
        return failedTransaction("Cash dispensing failed");
    }

    return successTransaction();
}
```

**Why Better**:
- Validate first, act later
- Rollback if any step fails
- Consistent state always

---

## Mistake 6: Not Using Dependency Injection ❌

### Wrong Approach:
```java
class TransactionService {
    // ❌ Creating dependencies inside class
    private BankingService bankingService = new RealBankingService();

    public Transaction withdraw(...) {
        bankingService.debitAccount(...);
    }
}
```

**Problems**:
- Tightly coupled to `RealBankingService`
- Can't use mock for testing
- Hard to swap implementations

**Testing is painful**:
```java
@Test
public void testWithdrawal() {
    TransactionService service = new TransactionService();
    // ❌ Can't inject mock! Will call real banking API in test
}
```

### Correct Approach:
```java
class TransactionService {
    private BankingService bankingService;  // ✅ Abstraction

    // ✅ Dependency Injection via constructor
    public TransactionService(BankingService bankingService) {
        this.bankingService = bankingService;
    }
}
```

**Testing is easy**:
```java
@Test
public void testWithdrawal() {
    BankingService mock = new MockBankingService();  // ✅ Inject mock
    TransactionService service = new TransactionService(mock);
    // Easy to test!
}
```

**Why Better**:
- Loose coupling
- Easy to test
- Easy to swap implementations

---

## Mistake 7: Deleting Failed Transactions ❌

### Wrong Approach:
```java
public void withdraw(...) {
    Transaction txn = new Transaction(...);

    if (withdrawal fails) {
        // ❌ Deleting failed transaction
        transactionRepository.delete(txn);
    }
}
```

**Problems**:
- Lost audit trail
- Can't investigate failures
- Compliance violation

### Correct Approach:
```java
public Transaction withdraw(...) {
    Transaction txn = new Transaction(...);

    if (withdrawal fails) {
        // ✅ Mark as failed, but KEEP the record
        txn.markAsFailed("Insufficient balance");
        transactionRepository.save(txn);
    }

    return txn;
}
```

**Why Better**:
- Complete audit trail
- Can analyze failure patterns
- Regulatory compliance

---

## Mistake 8: Not Handling Rollback ❌

### Wrong Approach:
```java
public void withdraw(Account account, double amount, ATM atm) {
    account.debit(amount);  // ✅ Success
    atm.dispenseCash(amount);  // ❌ Fails!
    // ❌ No rollback! Money deducted but no cash dispensed
}
```

### Correct Approach:
```java
public Transaction withdraw(Account account, double amount, ATM atm) {
    account.debit(amount);

    Map<Integer, Integer> dispensed = atm.dispenseCash(amount);
    if (dispensed == null) {
        // ✅ ROLLBACK
        account.credit(amount);  // Return money
        return failedTransaction("Cash dispense failed");
    }

    return successTransaction();
}
```

**Why Better**:
- No inconsistent state
- Customer doesn't lose money
- Reliable system

---

## Mistake 9: Using Strings Instead of Value Objects ❌

### Wrong Approach:
```java
class Transaction {
    private String transactionId = "123";  // ❌ Simple string
    private String timestamp = "2025-11-17 10:30";  // ❌ String date
}

// ❌ Easy to make mistakes
String id = "456";
String date = "17-11-2025";  // ❌ Different format!
```

### Correct Approach:
```java
class Transaction {
    private String transactionId = UUID.randomUUID().toString();  // ✅ Unique
    private LocalDateTime timestamp = LocalDateTime.now();  // ✅ Type-safe
}

// ✅ Type-safe operations
LocalDateTime tomorrow = timestamp.plusDays(1);
boolean expired = timestamp.isBefore(LocalDateTime.now().minusDays(30));
```

**Why Better**:
- Type safety
- Built-in methods
- Harder to make mistakes

---

## Mistake 10: Monolithic Class (God Object) ❌

### Wrong Approach:
```java
class ATMSystem {
    // ❌ Everything in one class!
    public void insertCard() { ... }
    public boolean validatePin() { ... }
    public void withdraw() { ... }
    public void deposit() { ... }
    public void dispenseCash() { ... }
    public void printReceipt() { ... }
    public void updateDatabase() { ... }
    // 1000+ lines of code!
}
```

**Problems**:
- Violates Single Responsibility
- Hard to maintain
- Hard to test
- Hard to extend

### Correct Approach:
```java
// ✅ Separate responsibilities

class AuthenticationService {
    public boolean authenticateUser(...) { ... }
}

class TransactionService {
    public Transaction withdraw(...) { ... }
    public Transaction deposit(...) { ... }
}

class ATMService {  // Facade
    private AuthenticationService authService;
    private TransactionService transactionService;

    public void startSession() {
        // Orchestrate
    }
}
```

**Why Better**:
- Single Responsibility
- Easy to test each class
- Easy to maintain
- Clear structure

---

## Mistake 11: Not Using Interfaces for Hardware ❌

### Wrong Approach:
```java
class TransactionService {
    private PhysicalCashDispenser cashDispenser;  // ❌ Concrete class

    public void withdraw(...) {
        cashDispenser.dispense(amount);  // ❌ Tied to physical hardware
    }
}

// ❌ Can't test without real hardware!
```

### Correct Approach:
```java
interface CashDispenser {  // ✅ Abstract interface
    void dispenseCash(Map<Integer, Integer> denominations);
}

class PhysicalCashDispenser implements CashDispenser {
    // Real hardware
}

class MockCashDispenser implements CashDispenser {
    // Testing
}

class TransactionService {
    private CashDispenser cashDispenser;  // ✅ Abstraction

    // ✅ Can inject mock for testing
    public TransactionService(CashDispenser cashDispenser) {
        this.cashDispenser = cashDispenser;
    }
}
```

**Why Better**:
- Testable without hardware
- Vendor independence
- Easy to swap implementations

---

## Mistake 12: Not Tracking Transaction Status ❌

### Wrong Approach:
```java
class Transaction {
    // ❌ No status field!
    private double amount;
}

// ❌ Can't tell if transaction succeeded or failed
```

### Correct Approach:
```java
enum TransactionStatus {
    PENDING, SUCCESS, FAILED, REVERSED
}

class Transaction {
    private TransactionStatus status = TransactionStatus.PENDING;  // ✅

    public void markAsSuccess() {
        this.status = TransactionStatus.SUCCESS;
    }

    public void markAsFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
    }
}

// ✅ Can query by status
List<Transaction> failed = repository.findByStatus(FAILED);
```

**Why Better**:
- Track transaction lifecycle
- Easy to query failed transactions
- Better debugging

---

## Mistake 13: Hard-coding Values ❌

### Wrong Approach:
```java
public void withdraw(Account account, double amount) {
    if (amount > 50000) {  // ❌ Magic number!
        throw new Exception("Limit exceeded");
    }

    if (account.getPinAttempts() >= 3) {  // ❌ Another magic number!
        account.block();
    }
}
```

### Correct Approach:
```java
class Account {
    private static final double DAILY_WITHDRAWAL_LIMIT = 50000.0;  // ✅ Named constant
    private double dailyWithdrawalLimit = DAILY_WITHDRAWAL_LIMIT;

    public boolean isWithinDailyLimit(double amount) {
        return amount <= dailyWithdrawalLimit;
    }
}

class Card {
    private static final int MAX_PIN_ATTEMPTS = 3;  // ✅ Named constant

    public void incrementPinAttempt() {
        pinAttempts++;
        if (pinAttempts >= MAX_PIN_ATTEMPTS) {
            blockCard();
        }
    }
}
```

**Why Better**:
- Self-documenting code
- Easy to change limits
- No magic numbers

---

## Mistake 14: Ignoring Edge Cases ❌

### Common Edge Cases to Handle:

1. **Amount not dispensable**
```java
// ATM has only ₹500 notes
// Customer requests ₹1,200
// ❌ Wrong: Try to dispense anyway
// ✅ Right: Check canDispenseAmount() first
```

2. **Daily limit at midnight**
```java
// ❌ Wrong: Daily limit never resets
// ✅ Right: Reset at midnight
private void resetDailyLimitIfNewDay() {
    if (!lastWithdrawalDate.equals(LocalDate.now())) {
        dailyWithdrawnToday = 0.0;
    }
}
```

3. **Card expires during transaction**
```java
// ✅ Check expiry at start of transaction
if (card.isExpired()) {
    return failedTransaction("Card expired");
}
```

4. **Network timeout**
```java
// ✅ Retry with timeout
@Retry(maxAttempts = 3, backoff = @Backoff(delay = 2000))
public boolean debitAccount(...) { ... }
```

---

## Quick Checklist: Avoid These Mistakes ✓

- [ ] ✅ Services handle logic, Entities hold data
- [ ] ✅ Use enums for status/types
- [ ] ✅ Never store PIN locally
- [ ] ✅ Handle concurrency (locking)
- [ ] ✅ Validate before acting
- [ ] ✅ Use Dependency Injection
- [ ] ✅ Keep failed transactions (audit)
- [ ] ✅ Always rollback on failure
- [ ] ✅ Use value objects (UUID, LocalDateTime)
- [ ] ✅ Break down large classes (SRP)
- [ ] ✅ Abstract hardware with interfaces
- [ ] ✅ Track transaction status
- [ ] ✅ Use named constants (no magic numbers)
- [ ] ✅ Handle edge cases

---

**Remember**: These mistakes are common even among experienced developers. The key is to learn from them and apply good design principles consistently! 🎯
