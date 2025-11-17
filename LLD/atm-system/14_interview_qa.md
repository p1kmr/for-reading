# ATM System - Interview Q&A (20+ Questions)

> **Purpose**: Common LLD interview questions with detailed answers and code examples

---

## Part 1: Design & Architecture Questions

### Q1: Walk me through the high-level architecture of your ATM system

**Answer**:
Our ATM system follows a **layered architecture** with clear separation of concerns:

1. **Presentation Layer**: `ATMService` (Facade pattern) - Entry point
2. **Service Layer**: `AuthenticationService`, `TransactionService` - Business logic
3. **Domain Layer**: `Card`, `Account`, `Transaction`, `ATM` - Entities
4. **Infrastructure Layer**: Repositories, Hardware interfaces, BankingService

**Flow**:
```
User → ATMService → AuthService/TransactionService → Entities → Repository → Database
                 ↓
              Hardware (CardReader, CashDispenser, etc.)
```

**Key Patterns**:
- Facade (ATMService)
- Repository (data access abstraction)
- Strategy (different transaction types)
- Dependency Injection (loose coupling)

---

### Q2: How does your design follow SOLID principles?

**Answer with examples**:

**S - Single Responsibility**:
```java
// Each class has one responsibility
class AuthenticationService {
    // ONLY handles authentication
    boolean authenticateUser(Card card, String pin) { ... }
}

class TransactionService {
    // ONLY handles transactions
    Transaction withdraw(...) { ... }
}
// NOT mixed together!
```

**O - Open/Closed**:
```java
// Adding new transaction type without modifying existing code
interface TransactionStrategy {
    Transaction execute(...);
}

class FundTransferStrategy implements TransactionStrategy { ... }  // NEW!
// No changes to existing WithdrawalStrategy or DepositStrategy
```

**L - Liskov Substitution**:
```java
AccountRepository repo;
repo = new JpaAccountRepository();      // Production
repo = new InMemoryAccountRepository(); // Testing
// Both work identically - substitutable
```

**I - Interface Segregation**:
```java
// Small, focused interfaces
interface CardReader { Card readCard(); void ejectCard(); }
interface CashDispenser { void dispenseCash(...); }
// NOT one big ATMHardware interface
```

**D - Dependency Inversion**:
```java
class TransactionService {
    private BankingService bankingService;  // Interface, not concrete class

    public TransactionService(BankingService service) {
        this.bankingService = service;  // Injected
    }
}
```

---

### Q3: Why use Repository pattern? What's the benefit?

**Answer**:

**Without Repository** (BAD):
```java
class TransactionService {
    public void withdraw(...) {
        // Direct database access - tightly coupled
        EntityManager em = ...;
        em.persist(transaction);
        // Hard to test! Requires real database
    }
}
```

**With Repository** (GOOD):
```java
class TransactionService {
    private TransactionRepository repo;  // Interface

    public void withdraw(...) {
        repo.save(transaction);  // Abstracted
    }
}

// Testing
TransactionService service = new TransactionService(
    new InMemoryTransactionRepository()  // No database needed!
);
```

**Benefits**:
1. **Testability**: Use in-memory implementation for tests
2. **Flexibility**: Easy to swap databases (MySQL → MongoDB)
3. **Centralization**: All data access logic in one place
4. **Maintainability**: Change database queries without touching business logic

---

### Q4: How do you handle concurrency when two ATMs try to withdraw from the same account?

**Answer**:

**Problem**:
```
Account balance: ₹10,000
ATM-1: Withdraw ₹8,000 (reads balance = ₹10,000 ✅)
ATM-2: Withdraw ₹8,000 (reads balance = ₹10,000 ✅)
Both proceed → Overdraft! ❌
```

**Solution: Pessimistic Locking**
```java
@Transactional
public Transaction withdraw(...) {
    // Lock the row
    Account account = accountRepository.findByIdWithLock(accountNumber);
    // SQL: SELECT * FROM accounts WHERE id = ? FOR UPDATE

    if (account.getBalance() >= amount) {
        account.debit(amount);
        accountRepository.save(account);
    }
    // Lock released after commit
}
```

**How it works**:
```
ATM-1: Acquires lock, reads ₹10,000, deducts ₹8,000, commits (balance = ₹2,000)
ATM-2: WAITS for lock, then reads ₹2,000, insufficient balance → FAIL ✅
```

**Alternative: Optimistic Locking**
```java
@Entity
class Account {
    @Version
    private int version;  // Auto-incremented on each update
}

// If two transactions modify same row, second fails with OptimisticLockException
```

---

### Q5: What design patterns have you used and why?

**Answer**:

| Pattern | Where | Code Example | Why |
|---------|-------|--------------|-----|
| **Facade** | ATMService | `atmService.startSession()` | Simplify complex subsystem |
| **Strategy** | Transaction types | `TransactionStrategy.execute()` | Different algorithms |
| **Repository** | Data access | `accountRepo.save()` | Abstract persistence |
| **State** | ATM states | `IdleState`, `AuthenticatedState` | Behavior changes with state |
| **Singleton** | ATM instance | `ATM.getInstance()` | One instance per machine |
| **Observer** | Notifications | `EmailNotifier`, `SMSNotifier` | Decouple events |
| **Template Method** | Transaction processing | `processTransaction()` | Define skeleton |
| **Dependency Injection** | All services | Constructor injection | Loose coupling |

**Code Example - Strategy Pattern**:
```java
interface TransactionStrategy {
    Transaction execute(Account account, double amount, ATM atm);
}

class WithdrawalStrategy implements TransactionStrategy {
    public Transaction execute(...) {
        account.debit(amount);
        atm.dispenseCash(amount);
        // ...
    }
}

class TransactionService {
    private Map<TransactionType, TransactionStrategy> strategies;

    public Transaction process(TransactionType type, ...) {
        TransactionStrategy strategy = strategies.get(type);
        return strategy.execute(...);
    }
}
```

---

## Part 2: Implementation Questions

### Q6: How do you ensure atomicity in withdrawal? (All steps succeed or all rollback)

**Answer**:

**Problem**: What if account is debited but cash dispensing fails?

**Solution: Database Transactions + Compensating Actions**
```java
@Transactional  // Spring annotation - auto-rollback on exception
public Transaction withdraw(Account account, double amount, ATM atm) {
    // Step 1: Debit account
    account.debit(amount);
    accountRepository.save(account);

    // Step 2: Dispense cash
    Map<Integer, Integer> dispensed = atm.dispenseCash(amount);
    if (dispensed == null) {
        // Trigger rollback by throwing exception
        throw new CashDispenseException("Failed to dispense");
        // @Transactional will automatically rollback Step 1
    }

    // Step 3: Update backend
    boolean updated = bankingService.debitAccount(accountNumber, amount);
    if (!updated) {
        throw new BackendException("Backend failed");
        // @Transactional will rollback Steps 1 & 2
    }

    // All steps successful → Commit
    return transaction;
}
```

**How @Transactional works**:
1. Spring starts database transaction
2. Execute method
3. If exception thrown → ROLLBACK
4. If no exception → COMMIT

---

### Q7: How do you handle the case where cash dispensing fails after debiting the account?

**Answer**:

**Compensating Transaction** (Manual Rollback):
```java
public Transaction withdraw(Account account, double amount, ATM atm) {
    Transaction transaction = new Transaction(...);

    try {
        // Debit account
        account.debit(amount);
        accountRepository.save(account);

        // Try to dispense cash
        Map<Integer, Integer> dispensed = atm.dispenseCash(amount);

        if (dispensed == null) {
            // ROLLBACK: Credit the amount back
            account.credit(amount);
            accountRepository.save(account);

            transaction.markAsFailed("Cash dispensing failed");
            transactionRepository.save(transaction);  // Record failed attempt

            // Alert admin
            alertService.notifyAdmin("Cash dispenser failure at " + atm.getAtmId());

            return transaction;
        }

        transaction.markAsSuccess(account.getBalance());
        transactionRepository.save(transaction);
        return transaction;

    } catch (Exception e) {
        // Rollback everything
        transaction.markAsFailed(e.getMessage());
        return transaction;
    }
}
```

**Key Points**:
1. Always save failed transactions (audit trail)
2. Compensating action (credit back)
3. Alert administrators
4. Never leave inconsistent state

---

### Q8: How do you calculate which denominations to dispense?

**Answer**:

**Greedy Algorithm** (minimize note count):
```java
/**
 * Calculate cash dispense plan
 * Goal: Minimize number of notes
 *
 * Example: ₹5,500
 * Best: 2×₹2000 + 3×₹500 = 5 notes
 * Not: 55×₹100 = 55 notes (wastes small notes)
 */
private Map<Integer, Integer> calculateDispensePlan(double amount) {
    Map<Integer, Integer> plan = new HashMap<>();
    double remaining = amount;

    // Try largest denomination first
    int[] denominations = {2000, 500, 100};

    for (int denom : denominations) {
        if (remaining == 0) break;

        CashBin bin = cashBins.get(denom);
        int notesNeeded = (int) (remaining / denom);
        int notesAvailable = bin.getNoteCount();

        if (notesNeeded > 0 && notesAvailable > 0) {
            int notesToDispense = Math.min(notesNeeded, notesAvailable);
            plan.put(denom, notesToDispense);
            remaining -= (notesToDispense * denom);
        }
    }

    // Check if we can create exact amount
    if (remaining == 0) {
        return plan;  // Success
    } else {
        return null;  // Cannot create exact amount
    }
}
```

**Example Execution**:
```
Amount: ₹5,500
ATM has: {₹2000: 10 notes, ₹500: 20 notes, ₹100: 50 notes}

Step 1: Try ₹2000
  notesNeeded = 5500 / 2000 = 2
  notesAvailable = 10
  Dispense: min(2, 10) = 2 notes
  remaining = 5500 - 4000 = 1500

Step 2: Try ₹500
  notesNeeded = 1500 / 500 = 3
  notesAvailable = 20
  Dispense: min(3, 20) = 3 notes
  remaining = 1500 - 1500 = 0

Result: {₹2000: 2, ₹500: 3} = 5 notes total
```

**Edge Case**:
```
Amount: ₹1,200
ATM has only ₹500 notes

Step 1: Try ₹500
  1200 / 500 = 2.4 → 2 notes
  remaining = 1200 - 1000 = 200

Step 2: Try ₹100
  No ₹100 notes available
  remaining = 200 (still non-zero)

Result: null (cannot create ₹1,200 with only ₹500 notes)
```

---

### Q9: How do you handle PIN validation securely?

**Answer**:

**Security Principles**:
1. **Never store PIN** locally
2. **Encrypt in transit** (TLS/SSL)
3. **Validate with backend** (banking system)
4. **Block after 3 attempts**
5. **No PIN in logs**

**Implementation**:
```java
class AuthenticationService {
    public boolean authenticateUser(Card card, String pin) {
        // Step 1: Validate card locally
        if (!card.isValid()) {
            return false;
        }

        // Step 2: Validate PIN with secure backend
        // NEVER store PIN! Only send for validation
        boolean validPin = bankingService.validatePinWithBank(
            card.getCardNumber(),
            pin  // Encrypted in transit via HTTPS
        );

        if (!validPin) {
            // Increment failed attempts
            card.incrementPinAttempt();

            if (card.getPinAttempts() >= 3) {
                // Block card locally AND in backend
                card.blockCard();
                bankingService.blockCard(card.getCardNumber());
                // Retain physical card
                cardReader.retainCard();
            }
            return false;
        }

        // Success - reset attempts
        card.resetPinAttempts();
        return true;
    }
}
```

**Backend API (Secure)**:
```java
// Backend service (NOT in ATM)
class BankingAPI {
    public boolean validatePin(String cardNumber, String pin) {
        // Fetch encrypted PIN from database
        String encryptedStoredPin = database.getEncryptedPin(cardNumber);

        // Encrypt entered PIN with same algorithm
        String encryptedEnteredPin = encrypt(pin);

        // Compare encrypted values (never compare plain text)
        return encryptedStoredPin.equals(encryptedEnteredPin);
    }

    private String encrypt(String pin) {
        // Use SHA-256 or bcrypt
        return BCrypt.hashpw(pin, BCrypt.gensalt());
    }
}
```

**Security Checklist**:
- ✅ PIN encrypted in transit (HTTPS)
- ✅ PIN never logged
- ✅ PIN never stored in ATM
- ✅ PIN validated server-side
- ✅ Card blocked after 3 attempts
- ✅ Physical card retained

**Common Mistake** ❌:
```java
class Card {
    private String pin;  // ❌ NEVER DO THIS!

    public boolean validatePin(String enteredPin) {
        return this.pin.equals(enteredPin);  // ❌ INSECURE!
    }
}
```

---

### Q10: How would you add a new transaction type (e.g., Fund Transfer)?

**Answer**:

**Using Strategy Pattern** - Open/Closed Principle

**Step 1: Add enum value**
```java
enum TransactionType {
    WITHDRAWAL,
    DEPOSIT,
    BALANCE_INQUIRY,
    FUND_TRANSFER  // NEW - no existing code modified
}
```

**Step 2: Create strategy**
```java
class FundTransferStrategy implements TransactionStrategy {
    @Override
    public Transaction execute(Account fromAccount, Account toAccount,
                               double amount, ATM atm) {
        Transaction txn = new Transaction(...);

        try {
            // Validate
            if (!fromAccount.hasSufficientBalance(amount)) {
                txn.markAsFailed("Insufficient balance");
                return txn;
            }

            // Execute
            fromAccount.debit(amount);
            toAccount.credit(amount);

            // Update backend
            bankingService.transferFunds(fromAccount.getAccountNumber(),
                                        toAccount.getAccountNumber(),
                                        amount);

            txn.markAsSuccess(fromAccount.getBalance());
            return txn;

        } catch (Exception e) {
            txn.markAsFailed(e.getMessage());
            return txn;
        }
    }
}
```

**Step 3: Register strategy**
```java
class TransactionService {
    private Map<TransactionType, TransactionStrategy> strategies;

    public TransactionService() {
        strategies = new HashMap<>();
        strategies.put(WITHDRAWAL, new WithdrawalStrategy());
        strategies.put(DEPOSIT, new DepositStrategy());
        strategies.put(FUND_TRANSFER, new FundTransferStrategy());  // Register
    }
}
```

**Step 4: Add UI option**
```java
class ATMService {
    private void showMainMenu() {
        String[] options = {
            "1. Balance Inquiry",
            "2. Cash Withdrawal",
            "3. Cash Deposit",
            "4. Fund Transfer",  // NEW
            "5. Exit"
        };
        screen.displayMenu(options);
    }
}
```

**Result**: New feature added WITHOUT modifying existing WithdrawalStrategy or DepositStrategy ✅

---

## Part 3: Scalability & Performance

### Q11: How would you scale this system to handle 10,000 ATMs?

**Answer**:

**Architecture Changes**:

1. **Microservices Architecture**
```
ATM-1 ─┐
ATM-2 ─┼→ API Gateway → Auth Service (cluster)
ATM-3 ─┤              → Transaction Service (cluster)
...    ↓              → Account Service (cluster)
ATM-N ─┘              → Notification Service (cluster)
```

2. **Database Sharding**
```java
// Shard accounts by region
class AccountRepository {
    public Account findById(String accountNumber) {
        String region = accountNumber.substring(0, 3);  // First 3 digits = region
        DataSource ds = shardMap.get(region);
        return ds.query("SELECT * FROM accounts WHERE account_number = ?", accountNumber);
    }
}
```

3. **Caching (Redis)**
```java
@Cacheable(value = "accounts", key = "#accountNumber")
public Account getAccount(String accountNumber) {
    return accountRepository.findById(accountNumber);
}

// Cache invalidation on update
@CacheEvict(value = "accounts", key = "#account.accountNumber")
public void updateAccount(Account account) {
    accountRepository.save(account);
}
```

4. **Message Queue for Notifications**
```java
// Don't wait for email/SMS - async processing
public void withdraw(...) {
    // ... withdrawal logic

    // Send to queue (non-blocking)
    messageQueue.send("notifications", new NotificationEvent(transaction));

    return transaction;
}

// Separate worker processes notifications
@RabbitListener(queues = "notifications")
public void handleNotification(NotificationEvent event) {
    emailService.send(...);
    smsService.send(...);
}
```

5. **Load Balancer**
```
        Load Balancer (HAProxy/Nginx)
               ↓
    ┌──────────┼──────────┐
    ↓          ↓          ↓
  Server1   Server2   Server3
```

---

### Q12: How do you prevent double withdrawal if network fails?

**Answer**:

**Problem**: Network timeout during withdrawal
```
1. ATM sends withdrawal request
2. Backend processes (deducts money)
3. Network times out before response reaches ATM
4. ATM retries → DOUBLE DEDUCTION! ❌
```

**Solution: Idempotency Key**
```java
class Transaction {
    private final String idempotencyKey;  // UUID generated once

    public Transaction(...) {
        this.idempotencyKey = UUID.randomUUID().toString();
    }
}

// Backend API
class BankingAPI {
    private Set<String> processedKeys = new HashSet<>();  // In real: Redis

    @PostMapping("/debit")
    public Response debitAccount(@RequestBody DebitRequest request) {
        String idempotencyKey = request.getIdempotencyKey();

        // Check if already processed
        if (processedKeys.contains(idempotencyKey)) {
            // Return previous result (don't process again)
            return previousResult(idempotencyKey);
        }

        // Process transaction
        debit(request.getAccountNumber(), request.getAmount());
        processedKeys.add(idempotencyKey);

        return Response.success();
    }
}
```

**Flow**:
```
Request 1: idempotencyKey = "abc123" → Process → Save "abc123"
Request 2: idempotencyKey = "abc123" → Already exists → Return previous result (no double deduction)
```

---

## Part 4: Tricky Interview Questions

### Q13: What happens if ATM crashes during cash dispensing?

**Answer**:

**Scenario**:
```
1. Account debited ✅
2. Cash dispensing started
3. ATM CRASHES ❌
4. Cash dispensed? Unknown!
```

**Solution: Transaction Log + Reconciliation**

```java
enum TransactionPhase {
    INITIATED,
    ACCOUNT_DEBITED,
    CASH_DISPENSING,  // Critical phase
    CASH_DISPENSED,
    COMPLETED
}

class Transaction {
    private TransactionPhase phase;
    private LocalDateTime lastUpdated;
}

// Before each step, update phase
public void withdraw(...) {
    transaction.setPhase(INITIATED);
    transactionRepository.save(transaction);

    account.debit(amount);
    transaction.setPhase(ACCOUNT_DEBITED);
    transactionRepository.save(transaction);

    boolean dispensed = cashDispenser.dispense(amount);
    transaction.setPhase(CASH_DISPENSING);
    transactionRepository.save(transaction);

    if (dispensed) {
        transaction.setPhase(CASH_DISPENSED);
    }
    // If crash happens here, we know cash was dispensed

    transaction.setPhase(COMPLETED);
    transactionRepository.save(transaction);
}
```

**Recovery Process**:
```java
@Scheduled(fixedDelay = 60000)  // Every minute
public void recoverIncompleteTransactions() {
    List<Transaction> stuck = transactionRepository
        .findByPhaseIn(CASH_DISPENSING, CASH_DISPENSED)
        .where(lastUpdated < 5 minutes ago);

    for (Transaction txn : stuck) {
        if (txn.getPhase() == CASH_DISPENSING) {
            // Unknown if cash dispensed
            // Manual investigation + customer support
            alertService.escalate(txn);
        } else if (txn.getPhase() == CASH_DISPENSED) {
            // Cash was dispensed, just update
            txn.setPhase(COMPLETED);
            save(txn);
        }
    }
}
```

---

### Q14: How do you test this system without real hardware?

**Answer**:

**Mock Implementations**:

```java
// Production
CardReader cardReader = new PhysicalCardReader();  // Talks to hardware

// Testing
CardReader cardReader = new MockCardReader();  // No hardware needed

// Mock implementation
class MockCardReader implements CardReader {
    private Card testCard;

    public MockCardReader(Card testCard) {
        this.testCard = testCard;
    }

    @Override
    public Card readCard() {
        return testCard;  // Return predefined card
    }

    @Override
    public void ejectCard() {
        System.out.println("MOCK: Card ejected");
    }
}

// Unit test
@Test
public void testWithdrawal() {
    // Arrange
    Card testCard = new Card("1234567890123456", "Test User", LocalDate.now().plusYears(3));
    Account testAccount = new Account("ACC123", "Test User", 10000, SAVINGS);

    CardReader mockCardReader = new MockCardReader(testCard);
    CashDispenser mockDispenser = new MockCashDispenser();
    BankingService mockBanking = new MockBankingService();

    ATMService atmService = new ATMService(
        atm, authService, transactionService,
        mockCardReader,  // Mock!
        mockScreen,      // Mock!
        mockKeypad,      // Mock!
        mockDispenser    // Mock!
    );

    // Act
    Transaction txn = transactionService.withdraw(testAccount, 2000, atm);

    // Assert
    assertEquals(TransactionStatus.SUCCESS, txn.getStatus());
    assertEquals(8000, testAccount.getBalance(), 0.01);
}
```

**Benefits**:
- ✅ Fast tests (no hardware delays)
- ✅ Deterministic (no hardware failures)
- ✅ Easy to test edge cases
- ✅ CI/CD friendly

---

### Q15: What if two transactions have the exact same timestamp?

**Answer**:

**Use UUID for unique IDs** (not timestamp):

```java
class Transaction {
    // ❌ WRONG - timestamp may collide
    private String transactionId = String.valueOf(System.currentTimeMillis());

    // ✅ RIGHT - UUID is always unique
    private String transactionId = UUID.randomUUID().toString();

    // Even if two transactions happen at exact same millisecond,
    // UUIDs will be different:
    // Transaction 1: "550e8400-e29b-41d4-a716-446655440000"
    // Transaction 2: "7c9e6679-7425-40de-944b-e07fc1f90ae7"
}
```

**Database Index**:
```sql
CREATE TABLE transactions (
    transaction_id VARCHAR(36) PRIMARY KEY,  -- UUID (unique)
    timestamp TIMESTAMP NOT NULL,
    INDEX idx_timestamp (timestamp)  -- For queries by time
);
```

---

## Part 5: Bonus Questions

### Q16: How would you implement mini-statement (last 5 transactions)?

```java
class TransactionRepository {
    List<Transaction> findLastNTransactions(String accountNumber, int n) {
        return entityManager.createQuery(
            "SELECT t FROM Transaction t WHERE t.accountNumber = :accNum " +
            "ORDER BY t.timestamp DESC",
            Transaction.class
        )
        .setParameter("accNum", accountNumber)
        .setMaxResults(n)
        .getResultList();
    }
}

// Usage in ATMService
public void printMiniStatement(Account account) {
    List<Transaction> last5 = transactionRepository
        .findLastNTransactions(account.getAccountNumber(), 5);

    screen.displayMessage("=== MINI STATEMENT ===");
    for (Transaction txn : last5) {
        screen.displayMessage(txn.getDescription());
    }

    receiptPrinter.printMiniStatement(last5);
}
```

---

### Q17: How do you handle multiple currencies?

```java
enum Currency {
    INR, USD, EUR, GBP
}

class Account {
    private Currency currency;
    private double balance;
}

class ATM {
    private Currency supportedCurrency;  // Each ATM supports one currency
    private Map<Integer, CashBin> cashBins;  // Denominations in that currency
}

class TransactionService {
    public Transaction withdraw(Account account, double amount, ATM atm) {
        // Validate currency match
        if (account.getCurrency() != atm.getSupportedCurrency()) {
            throw new CurrencyMismatchException("ATM supports " +
                atm.getSupportedCurrency() + ", account is in " +
                account.getCurrency());
        }

        // Rest of logic...
    }
}
```

---

### Q18: How do you implement daily withdrawal limits that reset at midnight?

**Answer**: (Already in Account class)

```java
class Account {
    private double dailyWithdrawalLimit;
    private double dailyWithdrawnToday;
    private LocalDate lastWithdrawalDate;

    public void debit(double amount) {
        resetDailyLimitIfNewDay();

        if (!isWithinDailyLimit(amount)) {
            throw new DailyLimitExceededException("Daily limit: " +
                dailyWithdrawalLimit + ", already withdrawn: " +
                dailyWithdrawnToday);
        }

        balance -= amount;
        dailyWithdrawnToday += amount;
    }

    private void resetDailyLimitIfNewDay() {
        LocalDate today = LocalDate.now();
        if (lastWithdrawalDate == null || !lastWithdrawalDate.equals(today)) {
            dailyWithdrawnToday = 0.0;  // Reset at midnight
            lastWithdrawalDate = today;
        }
    }
}
```

---

### Q19: Explain how you'd handle network failures with the banking backend

**Answer**:

**Retry with Exponential Backoff**:

```java
class BankingService {
    private static final int MAX_RETRIES = 3;

    public boolean debitAccount(String accountNumber, double amount) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                // Make API call
                return httpClient.post("/api/debit", ...);

            } catch (NetworkException e) {
                if (attempt == MAX_RETRIES) {
                    // Max retries exhausted
                    throw e;
                }

                // Wait before retry (exponential backoff)
                long waitTime = (long) Math.pow(2, attempt) * 1000;  // 2s, 4s, 8s
                Thread.sleep(waitTime);
                System.out.println("Retry attempt " + attempt);
            }
        }
        return false;
    }
}
```

**Circuit Breaker Pattern**:
```java
class BankingService {
    private CircuitBreaker circuitBreaker = new CircuitBreaker(
        failureThreshold: 5,  // Open after 5 failures
        timeout: 60000        // Try again after 1 minute
    );

    public boolean debitAccount(...) {
        if (circuitBreaker.isOpen()) {
            throw new ServiceUnavailableException("Banking service down");
        }

        try {
            boolean result = httpClient.post(...);
            circuitBreaker.recordSuccess();
            return result;

        } catch (Exception e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }
}
```

---

### Q20: How would you implement session timeout?

```java
class ATMSession {
    private Card card;
    private LocalDateTime lastActivity;
    private static final long TIMEOUT_SECONDS = 120;  // 2 minutes

    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now()
            .isAfter(lastActivity.plusSeconds(TIMEOUT_SECONDS));
    }
}

class ATMService {
    private ATMSession session;

    public void processWithdrawal() {
        // Check timeout before each operation
        if (session.isExpired()) {
            screen.displayMessage("Session expired. Please start again.");
            cardReader.ejectCard();
            return;
        }

        // Update activity
        session.updateActivity();

        // Process withdrawal...
    }
}
```

---

## Summary: Key Takeaways for Interviews

1. ✅ **Know SOLID principles** with concrete examples
2. ✅ **Explain design patterns** used and why
3. ✅ **Handle concurrency** (pessimistic/optimistic locking)
4. ✅ **Ensure atomicity** (@Transactional, rollback)
5. ✅ **Security** (PIN encryption, never store sensitive data)
6. ✅ **Scalability** (sharding, caching, load balancing)
7. ✅ **Testability** (mock implementations, dependency injection)
8. ✅ **Edge cases** (network failures, crashes, concurrent access)
9. ✅ **Real-world considerations** (audit logging, monitoring)
10. ✅ **Trade-offs** (pessimistic vs optimistic locking, greedy vs dynamic programming)

---

**Good luck with your interview!** 🎯
