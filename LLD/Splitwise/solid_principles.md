# 🎯 SOLID Principles in Splitwise Design

## What is SOLID?

**SOLID** = 5 principles for writing maintainable object-oriented code

- **S** - Single Responsibility Principle
- **O** - Open/Closed Principle
- **L** - Liskov Substitution Principle
- **I** - Interface Segregation Principle
- **D** - Dependency Inversion Principle

---

## 1. Single Responsibility Principle (SRP)

### Definition:
**"A class should have one, and only one, reason to change"**

### In Splitwise:

#### ✅ Good Example: UserService

```java
@Service
public class UserService {
    // ONLY responsible for user operations
    public User registerUser(UserDTO dto) { ... }
    public User getUserById(String id) { ... }
    public void updateProfile(String id, UserDTO dto) { ... }
}
```

**Reason to change**: Only when user-related business rules change

#### ✅ Good Example: Separate Services

```java
// UserService: User operations only
public class UserService { ... }

// EmailService: Email operations only
public class EmailService {
    public void sendWelcomeEmail(User user) { ... }
}

// PasswordService: Password operations only
public class PasswordService {
    public String hashPassword(String password) { ... }
}
```

#### ❌ Bad Example: God Class

```java
// ❌ Too many responsibilities!
public class UserService {
    public User register() { ... }           // User management
    public void sendEmail() { ... }          // Email sending
    public void generateReport() { ... }     // Reporting
    public void processPayment() { ... }     // Payment
    public void logActivity() { ... }        // Logging
}
```

**Problems**:
- Changes in email logic affect user management
- Hard to test (too many dependencies)
- Hard to reuse (email logic tied to UserService)

---

## 2. Open/Closed Principle (OCP)

### Definition:
**"Software entities should be open for extension but closed for modification"**

### In Splitwise:

#### ✅ Good Example: Strategy Pattern for Split Types

```java
// Interface is CLOSED for modification
public interface SplitStrategy {
    Map<String, BigDecimal> calculateSplit(...);
}

// OPEN for extension: Add new strategy without modifying existing code
public class EqualSplitStrategy implements SplitStrategy { ... }
public class ExactSplitStrategy implements SplitStrategy { ... }
public class PercentageSplitStrategy implements SplitStrategy { ... }

// Want to add new split type? Just create new class!
public class SharesSplitStrategy implements SplitStrategy { ... }  // NEW!
```

**Benefits**:
- Add SharesSplitStrategy without modifying existing strategies
- No risk of breaking existing code
- Existing tests still pass

#### ❌ Bad Example: if-else Chain

```java
public class ExpenseService {
    public Map<String, BigDecimal> calculateSplit(SplitType type, ...) {
        if (type == SplitType.EQUAL) {
            // Equal split logic
        } else if (type == SplitType.EXACT) {
            // Exact split logic
        } else if (type == SplitType.PERCENTAGE) {
            // Percentage split logic
        }
        // Adding new type requires modifying this method! ❌
    }
}
```

**Problem**: Every new split type requires modifying this method

---

## 3. Liskov Substitution Principle (LSP)

### Definition:
**"Objects of a superclass should be replaceable with objects of a subclass without breaking the application"**

### In Splitwise:

#### ✅ Good Example: Repository Pattern

```java
// Interface
public interface UserRepository {
    void save(User user);
    Optional<User> findById(String id);
}

// Implementation 1: JPA
public class JpaUserRepository implements UserRepository {
    public void save(User user) {
        // Save to MySQL
    }
}

// Implementation 2: In-Memory
public class InMemoryUserRepository implements UserRepository {
    private Map<String, User> users = new HashMap<>();

    public void save(User user) {
        users.put(user.getUserId(), user);
    }
}

// Service uses interface
public class UserService {
    private UserRepository repository;  // Could be JPA or In-Memory

    public void registerUser(User user) {
        repository.save(user);  // Works with BOTH implementations!
    }
}
```

**Benefits**:
- Can swap JPA with In-Memory for testing
- Service doesn't know the difference
- Both implementations behave consistently

#### ❌ Bad Example: Violating LSP

```java
public class JpaUserRepository implements UserRepository {
    public void save(User user) {
        if (user.getEmail() == null) {
            throw new NullPointerException("Email required");  // OK
        }
    }
}

public class InMemoryUserRepository implements UserRepository {
    public void save(User user) {
        // Accepts null email - INCONSISTENT! ❌
        users.put(user.getUserId(), user);
    }
}
```

**Problem**: Implementations behave differently!

---

## 4. Interface Segregation Principle (ISP)

### Definition:
**"Clients should not be forced to depend on interfaces they don't use"**

### In Splitwise:

#### ✅ Good Example: Separate Interfaces

```java
// Small, focused interfaces
public interface ReadableRepository<T> {
    Optional<T> findById(String id);
    List<T> findAll();
}

public interface WritableRepository<T> {
    void save(T entity);
    void update(T entity);
    void delete(String id);
}

// Combine only if needed
public interface UserRepository extends ReadableRepository<User>,
                                         WritableRepository<User> {
    Optional<User> findByEmail(String email);
}

// Service that only reads doesn't need write operations
public class UserQueryService {
    private ReadableRepository<User> repository;  // Only needs read!

    public User getUser(String id) {
        return repository.findById(id).orElse(null);
    }
}
```

#### ❌ Bad Example: Fat Interface

```java
// ❌ Too many methods!
public interface UserRepository {
    void save(User user);
    void update(User user);
    void delete(String id);
    Optional<User> findById(String id);
    List<User> findAll();
    void sendEmail(User user);           // ❌ Not related to repository!
    void generateReport(User user);      // ❌ Not related to repository!
    void processPayment(User user);      // ❌ Not related to repository!
}

// Service only needs findById but must depend on entire interface
public class UserQueryService {
    private UserRepository repository;  // Depends on unnecessary methods!
}
```

**Problem**: Service depends on methods it doesn't use

---

## 5. Dependency Inversion Principle (DIP)

### Definition:
**"High-level modules should not depend on low-level modules. Both should depend on abstractions"**

### In Splitwise:

#### ✅ Good Example: Service depends on Interface

```java
// HIGH-LEVEL: ExpenseService
@Service
public class ExpenseService {
    // Depends on INTERFACE (abstraction), not concrete class
    private final ExpenseRepository repository;  // Interface!

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public Expense addExpense(ExpenseDTO dto) {
        Expense expense = new Expense(...);
        repository.save(expense);  // Don't care about implementation!
        return expense;
    }
}

// LOW-LEVEL: JpaExpenseRepository
@Repository
public class JpaExpenseRepository implements ExpenseRepository {
    // Concrete implementation
    public void save(Expense expense) {
        // MySQL operations
    }
}

// Dependency flow:
// ExpenseService → ExpenseRepository ← JpaExpenseRepository
//   (high-level)    (abstraction)        (low-level)
```

**Benefits**:
- Can swap JpaExpenseRepository with InMemoryExpenseRepository
- ExpenseService doesn't change
- Easy to test (inject mock repository)

#### ❌ Bad Example: Service depends on Concrete Class

```java
// ❌ BAD: Service depends on concrete class
public class ExpenseService {
    private JpaExpenseRepository repository;  // Concrete class! ❌

    public ExpenseService() {
        this.repository = new JpaExpenseRepository();  // Creates directly! ❌
    }

    public void addExpense(Expense expense) {
        repository.save(expense);
    }
}
```

**Problems**:
- Can't swap implementations
- Can't test (can't inject mock)
- Tight coupling to MySQL/JPA

---

## 🎯 SOLID Summary in Splitwise

| Principle | Where Applied | Benefit |
|-----------|---------------|---------|
| **SRP** | Separate services (UserService, GroupService, etc.) | Each class has one responsibility |
| **OCP** | Strategy pattern for split types | Add new split types without modifying code |
| **LSP** | Repository implementations (JPA, In-Memory) | Can swap implementations seamlessly |
| **ISP** | Small, focused repository interfaces | Clients depend only on what they need |
| **DIP** | Services depend on interfaces, not concrete classes | Easy to test, loose coupling |

---

## 🏋️ Practice Exercise

**Question**: Which SOLID principle is violated?

```java
public class UserService {
    private MySQLDatabase database = new MySQLDatabase();  // Concrete class

    public void registerUser(User user) {
        String hashedPassword = BCrypt.hashpw(user.getPassword());
        user.setPasswordHash(hashedPassword);
        database.saveUser(user);
        sendWelcomeEmail(user);  // Sending email
        logActivity("User registered: " + user.getEmail());  // Logging
        generateReport(user);  // Reporting
    }

    private void sendWelcomeEmail(User user) { ... }
    private void logActivity(String message) { ... }
    private void generateReport(User user) { ... }
}
```

**Answer**:
<details>
<summary>Click to reveal</summary>

Violates **3 principles**:

1. **SRP (Single Responsibility)**: UserService does too much (user management, email, logging, reporting)
2. **DIP (Dependency Inversion)**: Depends on concrete MySQLDatabase, not interface
3. **OCP (Open/Closed)**: Adding new notification channel requires modifying UserService

**Fix**:
```java
public class UserService {
    private final UserRepository repository;  // Interface (DIP ✓)
    private final PasswordService passwordService;
    private final EmailService emailService;
    private final Logger logger;

    // Constructor injection
    public UserService(UserRepository repository,
                       PasswordService passwordService,
                       EmailService emailService,
                       Logger logger) {
        this.repository = repository;
        this.passwordService = passwordService;
        this.emailService = emailService;
        this.logger = logger;
    }

    public void registerUser(User user) {
        // Only user registration logic (SRP ✓)
        String hashedPassword = passwordService.hash(user.getPassword());
        user.setPasswordHash(hashedPassword);
        repository.save(user);

        // Delegate to other services
        emailService.sendWelcomeEmail(user);
        logger.info("User registered: " + user.getEmail());
    }
}
```

</details>

---

## ✅ Key Takeaways

1. **SRP**: One class = one responsibility
2. **OCP**: Use interfaces and inheritance to extend without modifying
3. **LSP**: Implementations must be substitutable
4. **ISP**: Small, focused interfaces
5. **DIP**: Depend on abstractions, not concrete classes

**Follow SOLID → Maintainable, testable, extensible code!**
