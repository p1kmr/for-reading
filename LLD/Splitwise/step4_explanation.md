# 💼 STEP 4: Add Service Layer

## 🎯 What Did We Add?

We added **5 Service classes** for business logic:
1. **UserService** - Manages user operations
2. **GroupService** - Manages group operations
3. **ExpenseService** - Manages expense operations (most complex!)
4. **TransactionService** - Manages settlements
5. **BalanceService** - Calculates balances

**Plus 2 Helper classes**:
- **SplitCalculator** - Calculates expense splits
- **PasswordEncoder** - Hashes passwords

---

## 🤔 Why Add Service Layer?

### The Problem Without Services:

**Bad Design** (All logic in entities):
```java
class User {
    public void register() {
        // Validate email
        // Check if email already exists (database query?)
        // Hash password
        // Save to database
        // Send welcome email
        // Create audit log
        // ... 100 lines of code!
    }
}
```

**Problems**:
- ❌ Entity has too many responsibilities
- ❌ Hard to test (entity coupled to database, email service, etc.)
- ❌ Violates Single Responsibility Principle
- ❌ Can't reuse logic elsewhere

---

### The Solution: Service Layer

**Good Design** (Logic in services):
```java
// Entity: Just data
class User {
    private String userId;
    private String name;
    private String email;
    // ... only getters/setters
}

// Service: Business logic
class UserService {
    public User registerUser(UserDTO dto) {
        // Validate email
        // Check if exists
        // Hash password
        // Save to DB
        // Send email
        // Return user
    }
}
```

**Benefits**:
- ✅ Entity is simple (just data holder)
- ✅ Service is testable (can mock dependencies)
- ✅ Follows Single Responsibility Principle
- ✅ Can inject dependencies (database, email service)

---

## 🎨 Analogy: Restaurant

Think of system as a **restaurant**:

### Entities = Ingredients
- **User** = Chicken
- **Expense** = Rice
- **Transaction** = Spices

Ingredients don't cook themselves!

### Services = Chefs
- **UserService** = Chef who prepares chicken
- **ExpenseService** = Chef who cooks rice + chicken
- **BalanceService** = Chef who calculates bill

**Chefs (Services) use Ingredients (Entities) to create Dishes (Features)**

---

## 📦 Service 1: UserService

### Responsibilities:
- Register new users
- Authenticate users (login)
- Update user profiles
- Get user information
- Calculate user balances

### Code Structure:

```java
@Service  // Spring annotation (dependency injection)
public class UserService {

    // === DEPENDENCIES (injected) ===
    private final UserRepository userRepository;      // Database access (Step 5)
    private final PasswordEncoder passwordEncoder;    // Password hashing

    // Constructor injection (best practice)
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // === METHODS ===

    /**
     * Register a new user
     * @param dto User registration data (name, email, password)
     * @return Created user
     * @throws EmailAlreadyExistsException if email taken
     */
    public User registerUser(UserDTO dto) {
        // 1. Validate input
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new ValidationException("Email is required");
        }

        // 2. Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        // 3. Hash password
        String hashedPassword = passwordEncoder.encode(dto.getPassword());

        // 4. Create user entity
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(hashedPassword);
        user.setCreatedAt(new Date());

        // 5. Save to database
        userRepository.save(user);

        // 6. Return user (don't return password!)
        return user;
    }

    /**
     * Authenticate user (login)
     * @return JWT token if successful
     * @throws AuthenticationException if credentials invalid
     */
    public String login(String email, String password) {
        // 1. Find user by email
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        // 2. Check password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        // 3. Generate JWT token
        String token = generateJwtToken(user);
        return token;
    }

    /**
     * Get user's balance (all groups)
     */
    public List<Balance> getUserBalance(String userId) {
        // Delegate to BalanceService
        return balanceService.calculateUserBalance(userId);
    }
}
```

### Key Concepts:

#### 1. Dependency Injection
```java
// ❌ BAD: Create dependencies inside
public class UserService {
    private UserRepository repo = new UserRepository();  // Tight coupling!
}

// ✅ GOOD: Inject dependencies via constructor
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;  // Loose coupling!
    }
}
```

**Benefits**:
- Easy to test (inject mock repository)
- Loose coupling (can swap implementations)
- Single Responsibility (service doesn't create dependencies)

#### 2. Validation in Service (Not in Entity)
```java
// ✅ Validate in service
public User registerUser(UserDTO dto) {
    if (dto.getEmail() == null) {
        throw new ValidationException("Email required");
    }
    // ...
}
```

**Why?**
- Entity is just data (no validation logic)
- Service knows business rules
- Can validate across multiple entities

---

## 📦 Service 2: GroupService

### Responsibilities:
- Create/delete groups
- Add/remove members
- Validate permissions (is user admin?)
- Check constraints (balance = 0 before leaving)

### Code Structure:

```java
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserService userService;  // Dependency on another service

    public GroupService(GroupRepository groupRepository,
                        UserService userService) {
        this.groupRepository = groupRepository;
        this.userService = userService;
    }

    /**
     * Create a new group
     * @return Created group with creator as admin
     */
    public Group createGroup(String name, String description, String creatorId) {
        // 1. Validate creator exists
        User creator = userService.getUserById(creatorId);
        if (creator == null) {
            throw new NotFoundException("Creator not found");
        }

        // 2. Create group
        Group group = new Group();
        group.setGroupId(UUID.randomUUID().toString());
        group.setName(name);
        group.setDescription(description);
        group.setCreatedBy(creatorId);  // Creator is admin
        group.setCreatedAt(new Date());

        // 3. Save to database
        groupRepository.save(group);

        // 4. Add creator as first member
        addMember(group.getGroupId(), creatorId);

        return group;
    }

    /**
     * Add member to group
     * @throws PermissionDeniedException if caller not admin
     */
    public void addMember(String groupId, String userId, String callerId) {
        // 1. Get group
        Group group = getGroupById(groupId);

        // 2. Check if caller is admin
        if (!group.getCreatedBy().equals(callerId)) {
            throw new PermissionDeniedException("Only admin can add members");
        }

        // 3. Validate user exists
        User user = userService.getUserById(userId);

        // 4. Check if already member
        if (isMember(groupId, userId)) {
            throw new AlreadyMemberException("User already in group");
        }

        // 5. Add to group (creates user-group relationship)
        groupRepository.addMemberToGroup(groupId, userId);

        // 6. Notify user (future: notification service)
    }

    /**
     * Remove member from group
     * Business rule: Can only remove if balance with all members = 0
     */
    public void removeMember(String groupId, String userId, String callerId) {
        // 1. Check permissions
        Group group = getGroupById(groupId);
        if (!group.getCreatedBy().equals(callerId)) {
            throw new PermissionDeniedException("Only admin can remove");
        }

        // 2. Check balance is zero
        List<Balance> balances = balanceService.calculateUserBalanceInGroup(userId, groupId);
        for (Balance balance : balances) {
            if (balance.getAmount().compareTo(BigDecimal.ZERO) != 0) {
                throw new BalanceNotSettledException(
                    "User has unsettled balances. Settle first.");
            }
        }

        // 3. Remove from group
        groupRepository.removeMemberFromGroup(groupId, userId);
    }
}
```

### Key Concepts:

#### 1. Service Orchestration
```java
// GroupService orchestrates multiple services
public void addMember(...) {
    User user = userService.getUserById(userId);  // Uses UserService
    // ... add member logic
    balanceService.checkBalance(userId);          // Uses BalanceService
}
```

**Services work together to implement complex features**

#### 2. Business Rules in Service
```java
// Business rule: Can't leave group with outstanding balance
public void removeMember(...) {
    if (hasOutstandingBalance(userId, groupId)) {
        throw new BalanceNotSettledException("Settle balances first");
    }
    // ...
}
```

---

## 📦 Service 3: ExpenseService (Most Complex!)

### Responsibilities:
- Add/edit/delete expenses
- Calculate splits based on split type
- Validate participants
- Update balances
- Notify participants

### Code Structure:

```java
@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupService groupService;
    private final UserService userService;
    private final SplitCalculator splitCalculator;  // Helper class

    /**
     * Add expense to group
     * This is the most complex operation!
     */
    public Expense addExpense(ExpenseDTO dto) {
        // 1. Validate group exists
        Group group = groupService.getGroupById(dto.getGroupId());

        // 2. Validate payer is member of group
        if (!groupService.isMember(dto.getGroupId(), dto.getPaidBy())) {
            throw new NotMemberException("Payer not in group");
        }

        // 3. Validate all participants are members
        for (String participantId : dto.getParticipants()) {
            if (!groupService.isMember(dto.getGroupId(), participantId)) {
                throw new NotMemberException(
                    "Participant " + participantId + " not in group");
            }
        }

        // 4. Calculate splits based on split type
        Map<String, BigDecimal> splits = splitCalculator.calculateSplits(
            dto.getSplitType(),
            dto.getAmount(),
            dto.getParticipants(),
            dto.getSplitDetails()  // For EXACT/PERCENTAGE
        );

        // 5. Validate splits sum to total amount
        BigDecimal splitSum = splits.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (splitSum.compareTo(dto.getAmount()) != 0) {
            throw new InvalidSplitException(
                "Splits don't sum to total amount");
        }

        // 6. Create expense entity
        Expense expense = new Expense();
        expense.setExpenseId(UUID.randomUUID().toString());
        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setPaidBy(dto.getPaidBy());
        expense.setGroupId(dto.getGroupId());
        expense.setSplitType(dto.getSplitType());
        expense.setSplits(splits);
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setCreatedAt(new Date());

        // 7. Save to database
        expenseRepository.save(expense);

        // 8. Notify participants (async)
        notificationService.notifyExpenseAdded(expense);

        // 9. Invalidate balance cache (if using cache)
        cacheService.invalidateGroupBalances(dto.getGroupId());

        return expense;
    }

    /**
     * Calculate splits for an expense
     * Delegates to SplitCalculator based on split type
     */
    private Map<String, BigDecimal> calculateSplits(Expense expense) {
        return splitCalculator.calculateSplits(
            expense.getSplitType(),
            expense.getAmount(),
            expense.getParticipants(),
            expense.getSplitDetails()
        );
    }
}
```

### Why ExpenseService is Complex?

It orchestrates **5 different concerns**:
1. **Validation**: Group exists, users are members
2. **Calculation**: Split amounts based on type
3. **Persistence**: Save expense to database
4. **Notifications**: Notify participants
5. **Cache**: Invalidate cached balances

This is the **heart of the system**!

---

## 📦 Service 4: TransactionService

### Responsibilities:
- Record settlements (payments)
- Update balances
- Validate payment amounts
- Transaction history

### Code Structure:

```java
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BalanceService balanceService;

    /**
     * Record a payment between two users
     * @return Created transaction
     */
    public Transaction recordPayment(String payerId, String payeeId,
                                      BigDecimal amount, String groupId) {
        // 1. Validate amount > 0
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }

        // 2. Validate payer ≠ payee
        if (payerId.equals(payeeId)) {
            throw new InvalidTransactionException("Can't pay yourself");
        }

        // 3. Check current balance
        BigDecimal currentBalance = balanceService.getBalanceBetweenUsers(
            payerId, payeeId);

        // 4. Validate payment doesn't exceed balance
        if (amount.compareTo(currentBalance.abs()) > 0) {
            throw new ExcessivePaymentException(
                "Payment exceeds outstanding balance");
        }

        // 5. Create transaction
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setPayerId(payerId);
        transaction.setPayeeId(payeeId);
        transaction.setAmount(amount);
        transaction.setGroupId(groupId);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTransactionDate(new Date());

        // 6. Save to database
        transactionRepository.save(transaction);

        // 7. Invalidate balance cache
        cacheService.invalidateUserBalances(payerId, payeeId);

        // 8. Notify both users
        notificationService.notifyPaymentRecorded(transaction);

        return transaction;
    }
}
```

---

## 📦 Service 5: BalanceService

### Responsibilities:
- Calculate user balances (from expenses & transactions)
- Calculate group balances
- Simplify debts (optimize transactions)

### Code Structure:

```java
@Service
public class BalanceService {

    private final ExpenseRepository expenseRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Calculate all balances for a user across all groups
     * @return List of balances (who owes user, whom user owes)
     */
    public List<Balance> calculateUserBalance(String userId) {
        // 1. Get all expenses involving user
        List<Expense> expenses = expenseRepository.findByParticipant(userId);

        // 2. Calculate what user owes from expenses
        Map<String, BigDecimal> owes = new HashMap<>();

        for (Expense expense : expenses) {
            String payer = expense.getPaidBy();
            BigDecimal userShare = expense.getSplits().get(userId);

            if (!payer.equals(userId)) {
                // User owes payer
                owes.merge(payer, userShare, BigDecimal::add);
            } else {
                // User paid, others owe user
                for (Map.Entry<String, BigDecimal> split : expense.getSplits().entrySet()) {
                    if (!split.getKey().equals(userId)) {
                        owes.merge(split.getKey(),
                                   split.getValue().negate(),  // Negative = owed
                                   BigDecimal::add);
                    }
                }
            }
        }

        // 3. Get all transactions involving user
        List<Transaction> transactions = transactionRepository.findByUser(userId);

        // 4. Adjust balances based on transactions
        for (Transaction txn : transactions) {
            if (txn.getPayerId().equals(userId)) {
                // User paid, reduce balance owed
                owes.merge(txn.getPayeeId(),
                           txn.getAmount().negate(),
                           BigDecimal::add);
            } else {
                // User received, increase balance owed to user
                owes.merge(txn.getPayerId(),
                           txn.getAmount(),
                           BigDecimal::add);
            }
        }

        // 5. Convert to Balance objects
        List<Balance> balances = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : owes.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) != 0) {
                Balance balance = new Balance();
                balance.setUserId(entry.getKey());
                balance.setAmount(entry.getValue().abs());
                balance.setType(entry.getValue().compareTo(BigDecimal.ZERO) > 0
                                ? BalanceType.OWE
                                : BalanceType.OWED);
                balances.add(balance);
            }
        }

        return balances;
    }

    /**
     * Simplify debts in a group using graph algorithm
     * Reduces number of transactions needed
     */
    public List<Transaction> simplifyDebts(String groupId) {
        // Algorithm: Minimize number of transactions
        // 1. Calculate net balance for each user
        // 2. Create creditors list (positive balance)
        // 3. Create debtors list (negative balance)
        // 4. Match debtors to creditors optimally
        // ... (complex algorithm, explained in Phase 7)
    }
}
```

### Key Concept: Balance Calculation Algorithm

**Given**:
- Expenses: Who paid, who owes
- Transactions: Who settled with whom

**Calculate**: Net balance between users

**Example**:
```
Expenses:
1. Alice paid ₹3000, split among Alice, Bob, Charlie (₹1000 each)
   → Bob owes Alice ₹1000
   → Charlie owes Alice ₹1000

2. Bob paid ₹600, split among Bob, Charlie (₹300 each)
   → Charlie owes Bob ₹300

Transactions:
3. Bob paid Alice ₹500
   → Bob owes Alice ₹500 now (was ₹1000)

Final Balances:
- Bob owes Alice: ₹500
- Charlie owes Alice: ₹1000
- Charlie owes Bob: ₹300
```

---

## 🔧 Helper Class: SplitCalculator

```java
public class SplitCalculator {

    /**
     * Calculate EQUAL split
     */
    public Map<String, BigDecimal> calculateEqualSplit(
            BigDecimal amount,
            List<String> participants) {

        int count = participants.size();
        BigDecimal splitAmount = amount.divide(
            new BigDecimal(count),
            2,  // 2 decimal places
            RoundingMode.HALF_UP
        );

        Map<String, BigDecimal> splits = new HashMap<>();
        for (String userId : participants) {
            splits.put(userId, splitAmount);
        }

        // Handle rounding: adjust last person if needed
        BigDecimal total = splitAmount.multiply(new BigDecimal(count));
        if (!total.equals(amount)) {
            BigDecimal diff = amount.subtract(total);
            String lastPerson = participants.get(count - 1);
            splits.put(lastPerson, splits.get(lastPerson).add(diff));
        }

        return splits;
    }

    /**
     * Calculate EXACT split
     */
    public Map<String, BigDecimal> calculateExactSplit(
            BigDecimal amount,
            Map<String, BigDecimal> exactAmounts) {

        // Validate sum equals total
        BigDecimal sum = exactAmounts.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(amount) != 0) {
            throw new InvalidSplitException(
                "Split amounts don't sum to total");
        }

        return new HashMap<>(exactAmounts);
    }

    /**
     * Calculate PERCENTAGE split
     */
    public Map<String, BigDecimal> calculatePercentageSplit(
            BigDecimal amount,
            Map<String, BigDecimal> percentages) {

        // Validate percentages sum to 100
        BigDecimal totalPercent = percentages.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPercent.compareTo(new BigDecimal("100")) != 0) {
            throw new InvalidSplitException(
                "Percentages must sum to 100");
        }

        // Calculate amounts
        Map<String, BigDecimal> splits = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : percentages.entrySet()) {
            BigDecimal splitAmount = amount
                .multiply(entry.getValue())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            splits.put(entry.getKey(), splitAmount);
        }

        return splits;
    }
}
```

---

## 🔄 What Changed from Step 3?

### Step 3:
- ✅ Entities had methods like `user.register()`, `expense.calculateSplits()`
- ❌ Too much logic in entities
- ❌ Hard to test

### Step 4:
- ✅ Moved logic to services
- ✅ Entities are now simple POJOs (Plain Old Java Objects)
- ✅ Services are testable (can inject mocks)
- ✅ Clear separation of concerns

**Before (Step 3)**:
```java
Expense expense = new Expense();
expense.calculateSplits();  // ❌ Entity does calculation
```

**After (Step 4)**:
```java
ExpenseService service = new ExpenseService(...);
Expense expense = service.addExpense(dto);  // ✅ Service does calculation
```

---

## 📐 Design Decisions

### Decision 1: Thin Entities, Fat Services
**Why?**
- Entities = Data holders (getters/setters only)
- Services = Business logic
- Easier to test services independently
- Follows Single Responsibility Principle

**Alternative**: Fat entities with logic
**Trade-off**: Harder to test, tight coupling

---

### Decision 2: Service Dependencies
```java
// ExpenseService depends on:
- ExpenseRepository (data access)
- GroupService (validate group)
- UserService (validate users)
- SplitCalculator (calculate splits)
```

**Why?**
- Services orchestrate multiple concerns
- Each service has single responsibility
- Reusable across different contexts

---

### Decision 3: Separate BalanceService
**Why not in ExpenseService?**
- Balance calculation is complex
- Used by multiple services (Expense, Transaction, Group)
- Separation of Concerns

**Benefit**: Can optimize balance calculation separately (caching, algorithms)

---

## ❌ Common Mistakes & Solutions

### Mistake 1: Business Logic in Entities
**Wrong**:
```java
class Expense {
    public void addExpense() {
        // Query database
        // Send notifications
        // Update balances
        // ... lots of logic
    }
}
```
**Right**: Move to service
```java
class ExpenseService {
    public Expense addExpense(ExpenseDTO dto) {
        // Business logic here
    }
}
```

---

### Mistake 2: Service Doing Too Much
**Wrong**:
```java
class SuperService {
    public void doEverything() {
        // User management
        // Group management
        // Expense management
        // ... 1000 lines
    }
}
```
**Right**: One service per entity
```java
class UserService { ... }
class GroupService { ... }
class ExpenseService { ... }
```

---

### Mistake 3: Cyclic Dependencies
**Wrong**:
```java
class UserService {
    private GroupService groupService;
}

class GroupService {
    private UserService userService;  // Cyclic!
}
```
**Problem**: Cannot instantiate either service!

**Solution**: Introduce interface or use event-driven approach

---

### Mistake 4: No Dependency Injection
**Wrong**:
```java
class ExpenseService {
    private ExpenseRepository repo = new ExpenseRepository();  // ❌
}
```
**Right**:
```java
class ExpenseService {
    private final ExpenseRepository repo;

    public ExpenseService(ExpenseRepository repo) {  // ✅ Inject
        this.repo = repo;
    }
}
```

---

## 🎓 Interview Tips

### Question: "Why separate services from entities?"

**Good Answer**:
> "Entities represent the domain model - they're just data holders. Services contain business logic. This separation follows the Single Responsibility Principle: entities are responsible for data, services for operations. It also makes testing easier - I can test services independently by mocking dependencies like repositories. In a real system, entities map to database tables, while services contain the business rules."

---

### Question: "How do services communicate?"

**Good Answer**:
> "Services communicate through dependency injection. For example, ExpenseService depends on UserService to validate users. I inject UserService into ExpenseService's constructor. This creates a dependency graph where each service has a clear role. For complex operations like 'add expense', ExpenseService orchestrates multiple services: UserService for validation, SplitCalculator for calculations, and NotificationService for alerts."

---

## ✅ Verification Checklist

- ☐ Each entity has a corresponding service
- ☐ Services use dependency injection (constructor injection)
- ☐ Entities are simple (no business logic)
- ☐ Each service has single responsibility
- ☐ Complex operations orchestrate multiple services
- ☐ No cyclic dependencies between services

---

## 🎯 What's Next?

**Step 5**: Add Repository Layer
- UserRepository, GroupRepository, ExpenseRepository
- Interface-based design (Repository Pattern)
- Separation of service logic from data access

This completes the **layered architecture**:
- **Entities** = Domain model
- **Services** = Business logic
- **Repositories** = Data access (Step 5)

---

**Key Takeaways**:
1. Services contain business logic, entities hold data
2. One service per entity (UserService, GroupService, etc.)
3. Services orchestrate multiple concerns
4. Use dependency injection for loose coupling
5. ExpenseService is the most complex (core domain logic)
6. BalanceService is separate (reusable calculation)
7. Helper classes (SplitCalculator) for pure functions

Ready for Step 5? Let's add Repositories! 🗄️
