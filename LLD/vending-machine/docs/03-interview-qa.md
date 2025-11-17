# Vending Machine - Interview Q&A

## Common LLD Interview Questions with Detailed Answers

---

### Q1: Why did you use the State pattern for the vending machine?

**Answer**:

The State pattern was chosen because the vending machine's behavior varies significantly based on its current state:

**Without State Pattern (Problems)**:
```java
class VendingMachine {
    private String currentState = "IDLE";

    public void insertMoney(Coin coin) {
        if (currentState.equals("IDLE")) {
            System.out.println("Select product first");
        } else if (currentState.equals("PRODUCT_SELECTED")) {
            // Accept money
            currentState = "PAYMENT";
        } else if (currentState.equals("PAYMENT")) {
            // Add to payment
        } else if (currentState.equals("DISPENSE")) {
            // Can't insert during dispense
        }
        // 50+ lines of if-else for each method!
    }
}
```

**Problems**:
- 50+ lines of if-else per method
- Hard to add new states
- State transitions scattered
- Difficult to test
- Violates Open/Closed Principle

**With State Pattern (Benefits)**:
```java
interface VendingMachineState {
    void insertMoney(Coin coin);
}

class IdleState implements VendingMachineState {
    public void insertMoney(Coin coin) {
        System.out.println("Select product first");
    }
}

class PaymentState implements VendingMachineState {
    public void insertMoney(Coin coin) {
        // Accept coin - valid in this state
        machine.addPayment(coin);
    }
}
```

**Benefits**:
- ✅ Each state in separate class (SRP)
- ✅ Easy to add new states (OCP)
- ✅ Clear state transitions
- ✅ Testable independently
- ✅ No if-else mess

---

### Q2: Explain the change calculation algorithm. What's its time complexity?

**Answer**:

**Algorithm**: Greedy (largest denomination first)

**Pseudocode**:
```
function calculateChange(amount):
    denominations = [500, 100, 50, 20, 10, 5, 2, 1] // DESC order
    result = {}
    remaining = amount

    for each denom in denominations:
        needed = floor(remaining / denom)
        available = inventory[denom]
        use = min(needed, available)

        if use > 0:
            result[denom] = use
            remaining -= (denom * use)

    return remaining == 0 ? result : FAIL
```

**Time Complexity**: O(n) where n = number of denominations (constant = 8)
**Space Complexity**: O(n) for result map

**Example**:
```
Change needed: ₹27
Inventory: ₹20(x2), ₹10(x3), ₹5(x5), ₹2(x10)

Step 1: Use ₹20 x 1 → remaining ₹7
Step 2: Use ₹5 x 1 → remaining ₹2
Step 3: Use ₹2 x 1 → remaining ₹0
Result: ₹20 + ₹5 + ₹2 = ₹27 ✅
```

**Why Greedy Works**: Indian currency forms a "canonical coin system" where greedy always gives optimal solution.

**Edge Case Handling**:
- If can't make exact change → Reject transaction
- Floating-point precision → Use epsilon comparison (< 0.01)
- Thread safety → Synchronized inventory updates

---

### Q3: How do you ensure thread safety in inventory management?

**Answer**:

**Problem**: Multiple concurrent transactions could:
- Sell same product twice
- Cause negative inventory
- Race conditions on coin/note counts

**Solution 1: Synchronized Methods (Our Choice)**
```java
class Inventory {
    private int quantity;

    public synchronized boolean deductQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false;
    }

    public synchronized void addQuantity(int amount) {
        quantity += amount;
    }
}
```

**Benefits**:
- ✅ Simple to implement
- ✅ Thread-safe by default
- ✅ Atomic check-and-deduct

**Trade-offs**:
- ❌ Coarse-grained locking (entire method locked)
- ❌ May reduce concurrency

**Solution 2: ReentrantLock (More Fine-Grained)**
```java
class Inventory {
    private int quantity;
    private final ReentrantLock lock = new ReentrantLock();

    public boolean deductQuantity(int amount) {
        lock.lock();
        try {
            if (quantity >= amount) {
                quantity -= amount;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
}
```

**Solution 3: AtomicInteger (Lock-Free)**
```java
class Inventory {
    private AtomicInteger quantity = new AtomicInteger(0);

    public boolean deductQuantity(int amount) {
        while (true) {
            int current = quantity.get();
            if (current < amount) {
                return false;
            }
            if (quantity.compareAndSet(current, current - amount)) {
                return true;
            }
        }
    }
}
```

**Recommendation**: For vending machine (single user at a time), synchronized methods are sufficient.

---

### Q4: Why use Singleton for VendingMachine? What are the trade-offs?

**Answer**:

**Why Singleton?**
- Only ONE physical vending machine exists
- Need centralized state management
- Global access point

**Implementation**:
```java
class VendingMachine {
    private static VendingMachine instance;

    private VendingMachine() {
        // Private constructor
    }

    public static synchronized VendingMachine getInstance() {
        if (instance == null) {
            instance = new VendingMachine();
        }
        return instance;
    }
}
```

**Benefits**:
- ✅ Controlled access to single instance
- ✅ Lazy initialization
- ✅ Consistent state

**Trade-offs**:
- ❌ Global state (harder to test)
- ❌ Tight coupling
- ❌ Cannot manage multiple machines easily

**When NOT to use Singleton?**
- Managing multiple vending machines
- Need dependency injection for testing
- Parallel instances required

**Alternative**: Dependency Injection
```java
class VendingMachineManager {
    private List<VendingMachine> machines;

    public VendingMachine getMachine(String machineId) {
        // Return specific machine
    }
}
```

---

### Q5: How would you add credit card payment support?

**Answer**:

**Current Design (Cash Only)**:
```java
class PaymentState {
    public void insertCoin(Coin coin) {
        // Handle coin
    }

    public void insertNote(Note note) {
        // Handle note
    }
}
```

**Enhanced Design (Strategy Pattern)**:

**Step 1: Create PaymentStrategy Interface**
```java
interface PaymentStrategy {
    boolean processPayment(Money amount);
    boolean refund(String transactionId, Money amount);
    String getPaymentMethod();
}
```

**Step 2: Implement Concrete Strategies**
```java
class CashPaymentStrategy implements PaymentStrategy {
    private CashInventory cashInventory;
    private ChangeCalculator calculator;

    public boolean processPayment(Money amount) {
        // Check if can make change
        return calculator.canMakeChange(amount.getAmount());
    }

    public boolean refund(String txnId, Money amount) {
        ChangeResult result = calculator.calculateChange(amount.getAmount());
        // Dispense change
        return result.isPossible();
    }

    public String getPaymentMethod() {
        return "CASH";
    }
}

class CardPaymentStrategy implements PaymentStrategy {
    private PaymentGateway gateway;

    public boolean processPayment(Money amount) {
        // Call payment gateway
        PaymentResult result = gateway.charge(amount);
        return result.isSuccess();
    }

    public boolean refund(String txnId, Money amount) {
        return gateway.refund(txnId, amount);
    }

    public String getPaymentMethod() {
        return "CARD";
    }
}

class UPIPaymentStrategy implements PaymentStrategy {
    private UPIHandler upiHandler;

    public boolean processPayment(Money amount) {
        String qrCode = upiHandler.generateQR(amount);
        // Display QR code, wait for payment
        return upiHandler.waitForPayment(30000); // 30 sec timeout
    }

    public boolean refund(String txnId, Money amount) {
        return upiHandler.refund(txnId);
    }

    public String getPaymentMethod() {
        return "UPI";
    }
}
```

**Step 3: Use Strategy in PaymentState**
```java
class PaymentState {
    private PaymentStrategy strategy;

    public void setPaymentMethod(PaymentMethod method) {
        switch (method) {
            case CASH:
                strategy = new CashPaymentStrategy();
                break;
            case CARD:
                strategy = new CardPaymentStrategy();
                break;
            case UPI:
                strategy = new UPIPaymentStrategy();
                break;
        }
    }

    public void processPayment(Money amount) {
        if (strategy.processPayment(amount)) {
            // Proceed to dispense
        } else {
            // Refund
        }
    }
}
```

**Benefits**:
- ✅ Easy to add new payment methods (OCP)
- ✅ Each strategy independent
- ✅ Runtime selection of payment method
- ✅ No modification to existing code

---

### Q6: What happens if dispense fails after payment is received?

**Answer**:

**Problem**: Customer paid, but motor jammed or product stuck.

**Solution: Transaction Rollback**

```java
class DispenseState {
    public void dispenseProduct() {
        Product product = machine.getSelectedProduct();
        Money payment = machine.getCurrentPayment();

        try {
            // Step 1: Deduct inventory
            boolean inventoryOk = machine.getInventoryManager()
                                         .deductProduct(product.getProductId());

            if (!inventoryOk) {
                throw new DispenseException("Inventory deduction failed");
            }

            // Step 2: Physical dispense (hardware operation)
            boolean dispensed = hardwareController.dispenseProduct(product.getCode());

            if (!dispensed) {
                // ROLLBACK: Add inventory back
                machine.getInventoryManager()
                       .refillProduct(product.getProductId(), 1);

                throw new DispenseException("Physical dispense failed");
            }

            // Step 3: Verify product picked up (sensor)
            boolean pickedUp = hardwareController.isProductCollected(5000); // 5 sec timeout

            if (!pickedUp) {
                // Product dispensed but not collected - log for admin
                logger.warn("Product dispensed but not collected: " + product);
            }

            // SUCCESS - proceed to change
            proceedToChangeReturn();

        } catch (DispenseException e) {
            // FULL REFUND
            System.out.println("Dispense failed. Refunding payment...");

            Money price = new Money(product.getPrice());
            Money totalRefund = payment;  // Full refund (not just product price)

            processRefund(totalRefund);

            // Reset and return to idle
            machine.resetTransaction();
            machine.setState(new IdleState(machine));

            // Log incident for admin
            logger.error("Dispense failure: " + e.getMessage());
        }
    }

    private void processRefund(Money amount) {
        ChangeCalculator calc = new ChangeCalculator(machine.getCashInventory());
        ChangeResult result = calc.calculateChange(amount.getAmount());

        if (result.isPossible()) {
            // Dispense refund
            for (Map.Entry<Object, Integer> entry : result.getBreakdown().entrySet()) {
                // Dispense coins/notes
            }
            System.out.println("Refund dispensed: " + amount);
        } else {
            // CRITICAL: Can't refund - escalate
            System.out.println("CRITICAL: Cannot dispense refund. Admin alert sent.");
            alertAdmin("Refund failure", amount);
        }
    }
}
```

**Key Points**:
- ✅ Atomic operations with rollback
- ✅ Full refund on failure
- ✅ Logging for audit trail
- ✅ Admin alerts for critical issues

---

### Q7: How would you handle concurrent users (future enhancement)?

**Answer**:

**Current Design**: Single user at a time

**Enhanced Design**: Queue-based multi-user support

**Option 1: Request Queue**
```java
class VendingMachine {
    private Queue<Transaction> requestQueue = new LinkedBlockingQueue<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public CompletableFuture<TransactionResult> purchaseAsync(String productCode, Money payment) {
        Transaction txn = new Transaction(productCode, payment);
        requestQueue.add(txn);

        return CompletableFuture.supplyAsync(() -> {
            // Process when machine available
            return processTransaction(txn);
        }, executor);
    }

    private TransactionResult processTransaction(Transaction txn) {
        // Synchronized processing
        synchronized (this) {
            selectProduct(txn.getProductCode());
            // ... rest of transaction
            return new TransactionResult(txn, Status.SUCCESS);
        }
    }
}
```

**Option 2: Session-based**
```java
class VendingMachineSession {
    private String sessionId;
    private VendingMachine machine;
    private long startTime;
    private static final long TIMEOUT = 60000; // 60 seconds

    public void startSession() {
        this.sessionId = UUID.randomUUID().toString();
        this.startTime = System.currentTimeMillis();

        // Auto-timeout after 60 seconds
        scheduler.schedule(() -> {
            if (isExpired()) {
                cancelSession();
            }
        }, TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - startTime > TIMEOUT;
    }
}

class SessionManager {
    private Map<String, VendingMachineSession> activeSessions = new ConcurrentHashMap<>();

    public synchronized VendingMachineSession createSession() {
        if (activeSessions.isEmpty()) {
            VendingMachineSession session = new VendingMachineSession();
            session.startSession();
            activeSessions.put(session.getSessionId(), session);
            return session;
        } else {
            throw new MachineNotAvailableException("Machine busy");
        }
    }
}
```

**Benefits**:
- ✅ Fair queuing (FIFO)
- ✅ Timeout handling
- ✅ Prevents race conditions
- ✅ Scalable design

---

### Q8: Explain the difference between aggregation and composition in your design.

**Answer**:

**Composition (Strong Ownership)**:

VendingMachine **composes** ProductCatalog, InventoryManager, CashInventory:

```java
class VendingMachine {
    // Created inside - COMPOSITION
    private ProductCatalog catalog = new ProductCatalog();
    private InventoryManager inventoryMgr = new InventoryManager();

    // When VendingMachine destroyed, these are also destroyed
}
```

**Characteristics**:
- Parent creates children
- Children cannot exist without parent
- Lifecycle tied together
- **UML**: Filled diamond (◆)

**Analogy**: Human **composes** Heart (heart can't exist without human)

---

**Aggregation (Weak Ownership)**:

ProductCatalog **aggregates** Products:

```java
class ProductCatalog {
    private Map<String, Product> products = new HashMap<>();

    public void addProduct(Product product) {  // Passed from outside
        products.put(product.getProductId(), product);
    }
}

// Products created elsewhere, then added
Product coke = new Product(...);  // Created outside catalog
catalog.addProduct(coke);  // Catalog aggregates it
```

**Characteristics**:
- Parent manages children but doesn't create them
- Children can exist independently
- Passed via parameters/setters
- **UML**: Empty diamond (◇)

**Analogy**: University **aggregates** Students (students exist independently)

---

**Quick Test**:
- If you create object inside class → **Composition**
- If you pass object from outside → **Aggregation**

---

### Q9: How do SOLID principles apply to this design?

**Answer**:

**1. Single Responsibility Principle (SRP)**

Each class has ONE reason to change:
- `ProductCatalog`: Manages products only
- `InventoryManager`: Manages quantities only
- `CashInventory`: Manages coins/notes only
- `ChangeCalculator`: Calculates change only

**Counter-example (Violation)**:
```java
class VendingMachine {
    // Too many responsibilities!
    private List<Product> products;  // Product management
    private Map<String, Integer> inventory;  // Inventory management
    private Map<Coin, Integer> coins;  // Cash management

    // Violates SRP - 3+ reasons to change!
}
```

---

**2. Open/Closed Principle (OCP)**

Can add new states WITHOUT modifying existing code:

```java
// New state - no changes to existing states!
class MaintenanceState implements VendingMachineState {
    public void selectProduct(String code) {
        System.out.println("Under maintenance");
    }
    // ... implement interface
}
```

---

**3. Liskov Substitution Principle (LSP)**

All states are substitutable:

```java
VendingMachineState state;

state = new IdleState(machine);     // ✅ Works
state = new PaymentState(machine);  // ✅ Works
state = new DispenseState(machine); // ✅ Works

// All behave correctly
state.selectProduct("A1");
```

---

**4. Interface Segregation Principle (ISP)**

States only implement what they need. If we had separate interfaces:

```java
// Instead of one fat interface
interface UserOperations {
    void selectProduct(String code);
    void insertCoin(Coin coin);
}

interface AdminOperations {
    void refillInventory(String id, int qty);
    void updatePrice(String id, double price);
}

// States implement only UserOperations (not forced to implement admin methods)
```

---

**5. Dependency Inversion Principle (DIP)**

Depend on abstractions:

```java
// High-level depends on abstraction
class PaymentState {
    private VendingMachineState nextState;  // Interface, not concrete class

    public void transition() {
        nextState = new DispenseState(machine);  // Concrete created here
        // But type is interface
    }
}
```

---

### Q10: What design patterns would you add for future enhancements?

**Answer**:

**1. Observer Pattern - Inventory Alerts**
```java
interface InventoryObserver {
    void onLowStock(String productId);
    void onOutOfStock(String productId);
}

class AdminNotifier implements InventoryObserver {
    public void onLowStock(String productId) {
        sendEmail("admin@company.com", "Low stock: " + productId);
    }
}

class InventoryManager {
    private List<InventoryObserver> observers = new ArrayList<>();

    public void deductProduct(String productId) {
        // ... deduct logic
        if (inventory.needsRefill()) {
            notifyLowStock(productId);
        }
    }

    private void notifyLowStock(String productId) {
        for (InventoryObserver observer : observers) {
            observer.onLowStock(productId);
        }
    }
}
```

---

**2. Factory Pattern - Product Creation**
```java
class ProductFactory {
    public static Product createProduct(ProductType type, Map<String, Object> config) {
        switch (type) {
            case BEVERAGE:
                return new BeverageProduct(config);
            case SNACK:
                return new SnackProduct(config);
            case REFRIGERATED:
                return new RefrigeratedProduct(config);  // Future
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }
}
```

---

**3. Command Pattern - Transaction History**
```java
interface Command {
    void execute();
    void undo();
}

class DispenseProductCommand implements Command {
    private Product product;
    private InventoryManager inventory;

    public void execute() {
        inventory.deductProduct(product.getProductId());
    }

    public void undo() {
        inventory.refillProduct(product.getProductId(), 1);
    }
}

class TransactionHistory {
    private Stack<Command> history = new Stack<>();

    public void executeCommand(Command cmd) {
        cmd.execute();
        history.push(cmd);
    }

    public void undoLast() {
        if (!history.isEmpty()) {
            Command cmd = history.pop();
            cmd.undo();
        }
    }
}
```

---

**4. Template Method Pattern - Payment Processing**
```java
abstract class PaymentProcessor {
    // Template method
    public final boolean processPayment(Money amount) {
        if (!validateAmount(amount)) {
            return false;
        }

        if (!authorize(amount)) {
            return false;
        }

        return capture(amount);
    }

    protected abstract boolean validateAmount(Money amount);
    protected abstract boolean authorize(Money amount);
    protected abstract boolean capture(Money amount);
}

class CashPaymentProcessor extends PaymentProcessor {
    protected boolean validateAmount(Money amount) {
        // Check if can make change
    }

    protected boolean authorize(Money amount) {
        // No authorization needed for cash
        return true;
    }

    protected boolean capture(Money amount) {
        // Accept physical cash
        return true;
    }
}

class CardPaymentProcessor extends PaymentProcessor {
    protected boolean validateAmount(Money amount) {
        // Check minimum/maximum limits
    }

    protected boolean authorize(Money amount) {
        // Call bank for authorization
    }

    protected boolean capture(Money amount) {
        // Capture authorized payment
    }
}
```

---

### Q11: How would you test this system?

**Answer**:

**Unit Tests**:

```java
@Test
public void testChangeCalculator_ExactChange() {
    // Arrange
    CashInventory cash = new CashInventory();
    cash.addCoins(Coin.TEN, 10);
    cash.addCoins(Coin.FIVE, 10);
    cash.addCoins(Coin.TWO, 10);

    ChangeCalculator calc = new ChangeCalculator(cash);

    // Act
    ChangeResult result = calc.calculateChange(17.0);

    // Assert
    assertTrue(result.isPossible());
    assertEquals(3, result.getBreakdown().size());
    assertEquals(17.0, result.getTotalValue(), 0.01);
}

@Test
public void testChangeCalculator_InsufficientInventory() {
    // Arrange
    CashInventory cash = new CashInventory();
    cash.addCoins(Coin.TEN, 1);  // Only one ₹10 coin

    ChangeCalculator calc = new ChangeCalculator(cash);

    // Act
    ChangeResult result = calc.calculateChange(27.0);

    // Assert
    assertFalse(result.isPossible());
}

@Test
public void testInventoryManager_ConcurrentDeduction() throws InterruptedException {
    // Arrange
    InventoryManager inv = new InventoryManager();
    inv.addInventory("P001", 10);

    CountDownLatch latch = new CountDownLatch(10);
    AtomicInteger successCount = new AtomicInteger(0);

    // Act - 10 threads try to deduct simultaneously
    for (int i = 0; i < 10; i++) {
        new Thread(() -> {
            if (inv.deductProduct("P001")) {
                successCount.incrementAndGet();
            }
            latch.countDown();
        }).start();
    }

    latch.await();

    // Assert
    assertEquals(10, successCount.get());  // All 10 succeeded
    assertEquals(0, inv.getQuantity("P001"));  // Exactly 0 left
}
```

**Integration Tests**:

```java
@Test
public void testCompletePurchaseFlow() {
    // Arrange
    VendingMachine machine = VendingMachine.getInstance();
    Product coke = new Product("P001", "Coke", 20.0, "A1", ProductType.BEVERAGE);
    machine.addProduct(coke, 5);
    machine.refillCash(Note.TWENTY, 10);

    // Act
    machine.selectProduct("A1");
    machine.insertNote(Note.TWENTY);

    // Assert
    assertEquals(4, machine.getInventoryManager().getQuantity("P001"));
    assertTrue(machine.getState() instanceof IdleState);
}
```

**State Transition Tests**:

```java
@Test
public void testStateTransitions() {
    VendingMachine machine = VendingMachine.getInstance();

    // Initial state
    assertTrue(machine.getState() instanceof IdleState);

    // Select product
    machine.selectProduct("A1");
    assertTrue(machine.getState() instanceof ProductSelectionState);

    // Insert money
    machine.insertCoin(Coin.TEN);
    assertTrue(machine.getState() instanceof PaymentState);

    // Cancel
    machine.cancelTransaction();
    assertTrue(machine.getState() instanceof IdleState);
}
```

---

### Q12: What are the main failure points and how do you handle them?

**Answer**:

**Failure Point 1: Dispense Motor Jam**
- **Detection**: Sensor doesn't detect product drop
- **Handling**: Refund full amount, alert admin, log incident
- **Recovery**: Admin manually retrieves product, resets machine

**Failure Point 2: Cannot Make Change**
- **Detection**: ChangeCalculator returns false
- **Handling**: Reject transaction BEFORE dispensing product
- **Recovery**: Display message "Use smaller notes" or "Exact change only"

**Failure Point 3: Out of Stock (Race Condition)**
- **Detection**: Inventory check fails after selection
- **Handling**: Synchronized inventory operations
- **Recovery**: Transaction cancelled, user selects different product

**Failure Point 4: Power Failure**
- **Detection**: UPS/battery backup
- **Handling**: Persist transaction state to disk (JPA/database)
- **Recovery**: On restart, check for incomplete transactions, refund if needed

**Failure Point 5: Coin/Note Acceptor Jam**
- **Detection**: Hardware sensor
- **Handling**: Reject insertion, display error
- **Recovery**: Admin clears jam, machine auto-recovers

**Circuit Breaker Pattern** (for external payment gateway):
```java
class PaymentGatewayCircuitBreaker {
    private int failureCount = 0;
    private long lastFailureTime = 0;
    private static final int FAILURE_THRESHOLD = 3;
    private static final long RECOVERY_TIMEOUT = 60000; // 1 min

    public boolean isOpen() {
        if (failureCount >= FAILURE_THRESHOLD) {
            if (System.currentTimeMillis() - lastFailureTime > RECOVERY_TIMEOUT) {
                failureCount = 0;  // Try again
                return false;
            }
            return true;  // Circuit open, don't call gateway
        }
        return false;
    }

    public void recordFailure() {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();
    }

    public void recordSuccess() {
        failureCount = 0;
    }
}
```

---

### Q13: How would you optimize this design for performance?

**Answer**:

**Optimization 1: Caching (Product Catalog)**
```java
class ProductCatalog {
    private Map<String, Product> products;
    private Map<String, List<Product>> categoryCache;  // NEW
    private long lastCacheUpdate;

    public List<Product> getProductsByCategory(ProductType type) {
        if (isCacheValid()) {
            return categoryCache.get(type.name());
        }

        // Rebuild cache
        rebuildCache();
        return categoryCache.get(type.name());
    }

    private boolean isCacheValid() {
        return System.currentTimeMillis() - lastCacheUpdate < 300000; // 5 min
    }
}
```

**Optimization 2: Lazy Initialization**
```java
class VendingMachine {
    private ChangeCalculator changeCalculator;  // Don't create immediately

    public ChangeCalculator getChangeCalculator() {
        if (changeCalculator == null) {
            changeCalculator = new ChangeCalculator(cashInventory);
        }
        return changeCalculator;
    }
}
```

**Optimization 3: Object Pooling (Transactions)**
```java
class TransactionPool {
    private Queue<Transaction> pool = new LinkedList<>();

    public Transaction acquire() {
        if (pool.isEmpty()) {
            return new Transaction();
        }
        return pool.poll();
    }

    public void release(Transaction txn) {
        txn.reset();
        pool.offer(txn);
    }
}
```

**Optimization 4: Async Logging**
```java
class AsyncLogger {
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>();

    public void log(String message) {
        logQueue.offer(new LogEntry(message));
    }

    // Background thread processes queue
    private void processLogs() {
        executor.submit(() -> {
            while (true) {
                LogEntry entry = logQueue.take();
                // Write to disk
                writeToFile(entry);
            }
        });
    }
}
```

---

### Q14: Explain the Money class. Why is it immutable?

**Answer**:

**Money as Value Object**:

```java
final class Money {  // final = cannot be subclassed
    private final double amount;  // final = cannot be changed

    public Money(double amount) {
        this.amount = amount;
    }

    // No setters!

    // Operations return NEW objects
    public Money add(Money other) {
        return new Money(this.amount + other.amount);  // New object
    }
}
```

**Why Immutable?**

**Reason 1: Thread Safety**
```java
// Two threads can safely share Money
Money price = new Money(20.0);

// Thread 1
Money payment1 = price.add(new Money(10));

// Thread 2
Money payment2 = price.add(new Money(5));

// price is UNCHANGED! No race condition
```

**Reason 2: Predictability**
```java
Money original = new Money(100);
Money result = original.subtract(new Money(50));

// original is still 100 (not modified)
assertEquals(100, original.getAmount());
assertEquals(50, result.getAmount());
```

**Reason 3: Hashcode Stability**
```java
Map<Money, String> map = new HashMap<>();
Money key = new Money(20.0);
map.put(key, "Twenty rupees");

// If Money was mutable:
key.setAmount(30.0);  // Would break HashMap!

// With immutable Money:
// Hashcode never changes, HashMap works correctly
```

**Reason 4: Value Semantics**
```java
Money m1 = new Money(20.0);
Money m2 = new Money(20.0);

m1.equals(m2);  // true (value equality, not identity)

// Two different objects but same VALUE
```

---

### Q15: How would you extend this to support product discounts and promotions?

**Answer**:

**Solution: Decorator Pattern + Strategy Pattern**

```java
// Base interface
interface PricingStrategy {
    Money calculatePrice(Product product, int quantity);
}

// Standard pricing
class StandardPricing implements PricingStrategy {
    public Money calculatePrice(Product product, int quantity) {
        return new Money(product.getPrice() * quantity);
    }
}

// Discount decorator
abstract class DiscountDecorator implements PricingStrategy {
    protected PricingStrategy basePricing;

    public DiscountDecorator(PricingStrategy basePricing) {
        this.basePricing = basePricing;
    }
}

// 10% discount
class PercentageDiscount extends DiscountDecorator {
    private double percentage;

    public PercentageDiscount(PricingStrategy base, double percentage) {
        super(base);
        this.percentage = percentage;
    }

    public Money calculatePrice(Product product, int quantity) {
        Money basePrice = basePricing.calculatePrice(product, quantity);
        double discount = basePrice.getAmount() * (percentage / 100.0);
        return basePrice.subtract(new Money(discount));
    }
}

// Buy 2 Get 1 Free
class BuyNGetMFree extends DiscountDecorator {
    private int buy;
    private int free;

    public BuyNGetMFree(PricingStrategy base, int buy, int free) {
        super(base);
        this.buy = buy;
        this.free = free;
    }

    public Money calculatePrice(Product product, int quantity) {
        int sets = quantity / (buy + free);
        int chargeableQty = (sets * buy) + (quantity % (buy + free));
        return new Money(product.getPrice() * chargeableQty);
    }
}

// Usage
PricingStrategy pricing = new StandardPricing();

// Apply 10% discount
pricing = new PercentageDiscount(pricing, 10.0);

// Also apply Buy 2 Get 1 Free
pricing = new BuyNGetMFree(pricing, 2, 1);

// Calculate final price
Money finalPrice = pricing.calculatePrice(product, 3);
// Buy 3, pay for 2, then 10% off
```

**Time-based promotions**:
```java
class HappyHourDiscount extends DiscountDecorator {
    private int startHour;
    private int endHour;

    public Money calculatePrice(Product product, int quantity) {
        int currentHour = LocalDateTime.now().getHour();

        if (currentHour >= startHour && currentHour < endHour) {
            return basePricing.calculatePrice(product, quantity)
                              .subtract(new Money(5.0)); // ₹5 off
        }

        return basePricing.calculatePrice(product, quantity);
    }
}
```

---

## Summary

This vending machine design demonstrates:

✅ **State Pattern** for clean state management
✅ **Singleton Pattern** for single instance
✅ **SOLID Principles** throughout
✅ **Thread Safety** with synchronized operations
✅ **Greedy Algorithm** for change calculation (O(n))
✅ **Value Object Pattern** for immutable Money
✅ **Composition over Inheritance**
✅ **Error Handling** with rollback mechanisms
✅ **Extensibility** for future enhancements
✅ **Testability** with clear separation of concerns

**Key Takeaways for Interview**:
- Justify every design decision
- Discuss trade-offs (pros and cons)
- Show awareness of edge cases
- Demonstrate SOLID knowledge
- Know multiple patterns and when to use each
- Think about scalability and extensibility
