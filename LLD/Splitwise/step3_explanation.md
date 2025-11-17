# 🎨 STEP 3: Add Attributes and Methods

## 🎯 What Did We Add?

We added **concrete details** to our entities:
1. **Attributes** (data fields): `userId`, `name`, `email`, `amount`, etc.
2. **Methods** (operations): `register()`, `addExpense()`, `calculateSplits()`, etc.
3. **Enums** (fixed values): `SplitType`, `TransactionStatus`, `BalanceType`
4. **Value Objects**: `Balance` (calculated, not stored)

---

## 🤔 Why Add Attributes and Methods?

### Before Step 3:
- Entities were **abstract boxes** (just names)
- Like saying "I have a User" without saying what data it holds

### After Step 3:
- Entities are **concrete and implementable**
- Can actually write Java/Python code now!

**Analogy**: Think of Step 2 as defining a **form template**, Step 3 fills in the **field names**
- Form: "User Registration"
- Fields: Name, Email, Phone, Password ← (Step 3 adds these!)

---

## 📦 Entity 1: User

### Attributes:

```java
class User {
    // === IDENTITY ===
    private String userId;          // Unique ID: "usr_abc123"

    // === PERSONAL INFO ===
    private String name;            // "Alice Johnson"
    private String email;           // "alice@example.com"
    private String phoneNumber;     // "+1234567890"

    // === SECURITY ===
    private String passwordHash;    // Hashed password (never plain text!)

    // === AUDIT FIELDS ===
    private Date createdAt;         // When user registered
    private Date updatedAt;         // Last profile update
}
```

### Why These Attributes?

#### 1. `userId` (String)
- **Purpose**: Unique identifier for each user
- **Why String?**: Can use UUID for global uniqueness
- **Example**: `"usr_550e8400-e29b-41d4-a716-446655440000"`
- **Alternative**: `long id` (database auto-increment)

#### 2. `name` (String)
- **Purpose**: Display name for user
- **Why needed?**: Show "Alice paid ₹500" instead of "usr_123 paid ₹500"

#### 3. `email` (String)
- **Purpose**: Login credential & unique identifier
- **Why unique?**: Two users can't have same email
- **Validation**: Must match email pattern

#### 4. `phoneNumber` (String)
- **Purpose**: Contact info & alternate login
- **Why String not int?**: Phone numbers have leading zeros, country codes

#### 5. `passwordHash` (String)
- **Purpose**: Authenticate user securely
- **Why Hash?**: NEVER store plain passwords!
- **How?**: Use bcrypt/Argon2 to hash
- **Example**: `"$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"`

#### 6. `createdAt`, `updatedAt` (Date)
- **Purpose**: Audit trail (when user joined, last update)
- **Why needed?**: Debugging, compliance, analytics
- **Best Practice**: Always add these to entities

---

### Methods:

```java
class User {
    // === USER MANAGEMENT ===
    public void register() {
        // Create new user account
        // Validate email, hash password, save to DB
    }

    public void login() {
        // Authenticate user
        // Check password hash, generate session token
    }

    public void updateProfile() {
        // Modify name, email, phone
        // Update updatedAt timestamp
    }

    // === BALANCE OPERATIONS ===
    public List<Balance> getBalance() {
        // Calculate balances from expenses & transactions
        // Return list of: "Bob owes you ₹500", "You owe Charlie ₹300"
    }

    // === EXPENSE OPERATIONS ===
    public void addExpense() {
        // Create new expense in a group
    }

    // === SETTLEMENT ===
    public void settleUp() {
        // Record payment to settle balance
    }
}
```

### Why These Methods?

Each method = **One thing the user can DO**
- `register()` → User creates account (Use Case)
- `getBalance()` → User views balances (Use Case)
- `settleUp()` → User records payment (Use Case)

---

## 📦 Entity 2: Group

### Attributes:

```java
class Group {
    // === IDENTITY ===
    private String groupId;         // Unique ID: "grp_xyz789"

    // === GROUP INFO ===
    private String name;            // "Goa Trip 2025"
    private String description;     // "Beach vacation with friends"

    // === ADMIN ===
    private String createdBy;       // User ID of creator (admin)

    // === AUDIT ===
    private Date createdAt;
    private Date updatedAt;
}
```

### Why These Attributes?

#### 1. `name` (String)
- **Purpose**: Human-readable group identifier
- **Example**: "Roommate Expenses", "Office Lunch"

#### 2. `description` (String)
- **Purpose**: Optional details about group
- **Example**: "Monthly apartment expenses for 4 roommates"

#### 3. `createdBy` (String)
- **Purpose**: Track who created group (they become admin)
- **Why?**: Admin can add/remove members, delete group
- **Stores**: User ID of creator

---

### Methods:

```java
class Group {
    public void addMember(User user) {
        // Add user to group
        // Check: Is caller the admin?
        // Create user-group relationship
    }

    public void removeMember(User user) {
        // Remove user from group
        // Check: Is user's balance = 0?
        // Delete user-group relationship
    }

    public List<User> getMembers() {
        // Return all users in group
    }

    public void addExpense(Expense expense) {
        // Add expense to this group
    }

    public List<Expense> getExpenses() {
        // Get all expenses in group
    }

    public List<Balance> getGroupBalance() {
        // Calculate balances within this group
    }
}
```

---

## 📦 Entity 3: Expense

### Attributes:

```java
class Expense {
    // === IDENTITY ===
    private String expenseId;       // Unique ID

    // === EXPENSE DETAILS ===
    private String description;     // "Hotel Booking"
    private BigDecimal amount;      // 6000.00 (NEVER use double!)
    private String paidBy;          // User ID who paid
    private String groupId;         // Group ID (optional)
    private SplitType splitType;    // EQUAL, EXACT, PERCENTAGE
    private Date expenseDate;       // When expense occurred
    private Date createdAt;         // When added to system

    // === SPLIT DETAILS ===
    private Map<String, BigDecimal> splits;  // userId → amount owed
    // Example: {"usr_alice": 2000.00, "usr_bob": 2000.00, "usr_charlie": 2000.00}
}
```

### Why These Attributes?

#### 1. `amount` (BigDecimal, NOT double!)
- **Wrong**: `double amount = 3000.0;`
  - Problem: `0.1 + 0.2 = 0.30000000000000004` (floating-point error!)
- **Right**: `BigDecimal amount = new BigDecimal("3000.00");`
  - Exact decimal arithmetic ✅
  - Critical for financial calculations!

#### 2. `splitType` (Enum)
- **Purpose**: How to divide the expense
- **Values**: EQUAL, EXACT, PERCENTAGE
- **Why Enum?**: Only these 3 options (not free-form string)

#### 3. `splits` (Map<String, BigDecimal>)
- **Purpose**: Store who owes how much
- **Key**: User ID
- **Value**: Amount owed
- **Example**:
  ```java
  {
    "usr_alice": 2000.00,
    "usr_bob": 2000.00,
    "usr_charlie": 2000.00
  }
  ```

#### 4. `expenseDate` vs `createdAt`
- **expenseDate**: When expense actually happened (Jan 15)
- **createdAt**: When added to system (Jan 20)
- **Why both?**: Alice might add expense 5 days late

---

### Methods:

```java
class Expense {
    public void calculateSplits() {
        // Based on splitType, calculate how much each person owes
        // EQUAL: amount ÷ number of participants
        // EXACT: use provided amounts
        // PERCENTAGE: amount × percentage

        // Update splits map
    }

    public void updateExpense() {
        // Modify expense details
        // Recalculate splits
    }

    public void deleteExpense() {
        // Remove expense
        // Recalculate balances
    }

    public List<User> getParticipants() {
        // Return users involved in this expense
    }
}
```

---

## 📦 Entity 4: Transaction

### Attributes:

```java
class Transaction {
    // === IDENTITY ===
    private String transactionId;

    // === TRANSACTION DETAILS ===
    private String payerId;         // User ID who is paying
    private String payeeId;         // User ID who is receiving
    private BigDecimal amount;      // Amount paid
    private Date transactionDate;   // When payment was made
    private String groupId;         // Optional: which group (can be null)
    private TransactionStatus status;  // PENDING, COMPLETED, CANCELLED
    private String notes;           // Optional: "Paid via UPI"
}
```

### Why These Attributes?

#### 1. `payerId` and `payeeId`
- **Purpose**: Who sends and who receives money
- **Example**: Bob (payer) → Alice (payee)
- **Why both?**: Transaction is directional (A to B)

#### 2. `status` (Enum)
- **PENDING**: Payment initiated but not confirmed
- **COMPLETED**: Payment successful
- **CANCELLED**: Payment was cancelled
- **Why?**: Track payment lifecycle

#### 3. `notes` (String)
- **Purpose**: Additional context
- **Example**: "Paid via UPI", "Cash payment", "Bank transfer ref: 123456"

---

### Methods:

```java
class Transaction {
    public void recordPayment() {
        // Save transaction to database
        // Update balances:
        //   - Payer's balance decreases
        //   - Payee's balance increases
    }

    public void cancelTransaction() {
        // Mark transaction as CANCELLED
        // Revert balance changes
    }

    public void getTransactionDetails() {
        // Return transaction info
    }
}
```

---

## 🎨 Enums (Fixed Value Types)

### Enum 1: SplitType

```java
public enum SplitType {
    EQUAL,      // Divide equally among all participants
    EXACT,      // Specify exact amount for each person
    PERCENTAGE  // Specify percentage for each person
}
```

**Why Enum?**
- ✅ Only these 3 options (not infinite strings)
- ✅ Type-safe (can't accidentally use "HALF" or "RANDOM")
- ✅ Easy to add new types later (e.g., SHARES)

**Usage**:
```java
Expense expense = new Expense();
expense.setSplitType(SplitType.EQUAL);  ✅

expense.setSplitType("EQUAL");  ❌ (compile error if using enum)
```

---

### Enum 2: TransactionStatus

```java
public enum TransactionStatus {
    PENDING,     // Payment initiated
    COMPLETED,   // Payment successful
    CANCELLED    // Payment cancelled
}
```

**Why?**
- Track payment lifecycle
- Can add more states later: FAILED, REFUNDED

---

### Enum 3: BalanceType

```java
public enum BalanceType {
    OWE,   // You owe someone
    OWED   // Someone owes you
}
```

**Usage**:
```java
// Alice's perspective
Balance b1 = new Balance("Bob", 500.00, BalanceType.OWE);
// Alice owes Bob ₹500

Balance b2 = new Balance("Charlie", 300.00, BalanceType.OWED);
// Charlie owes Alice ₹300
```

---

## 💎 Value Object: Balance

```java
public class Balance {
    private String userId;          // Other person
    private BigDecimal amount;      // How much
    private BalanceType type;       // OWE or OWED

    public BigDecimal getNetBalance() {
        // Return signed balance
        // OWE: negative (-500)
        // OWED: positive (+300)
    }
}
```

### What is a Value Object?

**Value Object** = Object defined by its values, not by identity
- No unique ID
- Immutable (cannot change)
- Calculated, not stored in database

**Example**:
```java
Balance b1 = new Balance("Bob", 500, BalanceType.OWE);
Balance b2 = new Balance("Bob", 500, BalanceType.OWE);

b1 == b2;  // false (different objects)
b1.equals(b2);  // true (same values)
```

### Why Balance is Value Object?

- ✅ Balance is **calculated** from expenses & transactions
- ✅ Not stored in database (computed on-demand)
- ✅ Immutable (once created, doesn't change)
- ✅ No unique ID (identified by values)

**Alternative**: Store balances in database (denormalized)
- **Pros**: Faster queries (no calculation needed)
- **Cons**: Risk of inconsistency (must update on every expense/transaction)
- **Decision**: Calculate on-the-fly for simplicity (can optimize later)

---

## 🔄 What Changed from Step 2?

### Step 2:
- ✅ Had relationships (connections)
- ❌ No data fields (what does User store?)
- ❌ No operations (what can User do?)

### Step 3:
- ✅ Added attributes (User has name, email, etc.)
- ✅ Added methods (User can register(), login(), etc.)
- ✅ Added enums (SplitType, TransactionStatus)
- ✅ Added value objects (Balance)

**Visual Comparison**:
```
Step 2:                Step 3:
┌────────┐            ┌──────────────────┐
│  User  │            │  User            │
└────────┘            │ - userId         │
                      │ - name           │
                      │ - email          │
                      │ + register()     │
                      │ + login()        │
                      └──────────────────┘
```

---

## 📐 Design Decisions

### Decision 1: Use BigDecimal for Money
**Why?**
- `double` has precision errors: `0.1 + 0.2 = 0.30000000000000004`
- Financial calculations must be EXACT
- Industry standard for money

**Example**:
```java
// ❌ WRONG
double amount = 3000.0;
double split = amount / 3;  // 999.9999999999999 😱

// ✅ RIGHT
BigDecimal amount = new BigDecimal("3000.00");
BigDecimal split = amount.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
// 1000.00 ✅
```

---

### Decision 2: Store passwordHash, Not password
**Why?**
- Security! If database is hacked, passwords are safe
- Use bcrypt/Argon2 for hashing (not MD5/SHA1!)

**Example**:
```java
// User registers
String plainPassword = "alice123";
String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
// Store: "$2a$10$N9qo8uLOickgx2ZMRZoMye..."

// User logs in
boolean isCorrect = BCrypt.checkpw(plainPassword, hashedPassword);  // true ✅
```

---

### Decision 3: splits as Map<String, BigDecimal>
**Why?**
- Efficient lookup: "How much does Bob owe?" → O(1)
- Flexible: Can have different amounts per person
- Easy to validate: Sum of splits must equal total amount

**Alternative**: Separate Split entity
```java
class Split {
    String userId;
    BigDecimal amount;
}
```
**Trade-off**: More complex, but more flexible (can add extra fields like "paid", "pending")

---

### Decision 4: Audit Fields (createdAt, updatedAt)
**Why add to EVERY entity?**
- ✅ Debugging: "When was this expense added?"
- ✅ Compliance: "Show me all transactions on Jan 15"
- ✅ Analytics: "User activity over time"
- ✅ Best Practice: Always add these!

---

## ❌ Common Mistakes & Solutions

### Mistake 1: Using double for Money
**Wrong**:
```java
private double amount;
```
**Problem**: Precision errors in calculations
**Right**:
```java
private BigDecimal amount;
```

---

### Mistake 2: Storing Plain Password
**Wrong**:
```java
private String password;  // Plain text!
```
**Problem**: Security vulnerability
**Right**:
```java
private String passwordHash;  // Hashed with bcrypt
```

---

### Mistake 3: Too Many Methods in Entity
**Wrong**:
```java
class User {
    public void sendEmail() { ... }          // ❌ Not User's responsibility
    public void validateCreditCard() { ... } // ❌ Not User's responsibility
    public void generateReport() { ... }     // ❌ Not User's responsibility
}
```
**Problem**: Violates Single Responsibility Principle
**Right**: Keep only core entity operations
```java
class User {
    public void register() { ... }     // ✅ Core operation
    public void updateProfile() { ... } // ✅ Core operation
}

// Move other operations to services
class EmailService {
    public void sendEmail(User user) { ... }
}
```

---

### Mistake 4: No Validation in Setters
**Wrong**:
```java
public void setAmount(BigDecimal amount) {
    this.amount = amount;  // ❌ No validation
}
```
**Problem**: Can set negative amounts!
**Right**:
```java
public void setAmount(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Amount must be positive");
    }
    this.amount = amount;  // ✅ Validated
}
```

---

## 🎓 Interview Tips

### Question: "What attributes would User entity have?"

**Bad Answer**: "Just name and email"

**Good Answer**:
> "User entity would have:
> 1. **Identity**: userId for unique identification
> 2. **Personal Info**: name, email, phoneNumber
> 3. **Security**: passwordHash (never plain password)
> 4. **Audit**: createdAt and updatedAt for tracking
>
> I'd use String for userId to support UUIDs, and BigDecimal for any financial amounts to avoid floating-point errors."

---

### Question: "Why use BigDecimal instead of double?"

**Bad Answer**: "Because it's better"

**Good Answer**:
> "Double has floating-point precision errors. For example, 0.1 + 0.2 = 0.30000000000000004 in Java. For financial calculations where exactness is critical, we use BigDecimal which provides arbitrary-precision decimal arithmetic. It's the industry standard for money calculations."

---

## 🏋️ Practice Exercise

**Task**: Add attributes to a Book entity for a Library System

**Given**:
```java
class Book {
    // TODO: Add attributes
}
```

**Requirements**:
- Unique identifier
- Title and author
- ISBN
- Publication year
- Number of copies available
- Track when book was added to library

**Solution**:
<details>
<summary>Click to reveal</summary>

```java
class Book {
    // Identity
    private String bookId;

    // Book Info
    private String title;
    private String author;
    private String isbn;
    private int publicationYear;

    // Availability
    private int totalCopies;
    private int availableCopies;

    // Audit
    private Date addedAt;

    // Methods
    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public void borrowBook() {
        if (availableCopies > 0) {
            availableCopies--;
        }
    }

    public void returnBook() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }
}
```

</details>

---

## ✅ Verification Checklist

Before moving to Step 4:

- ☐ Every entity has a unique ID field (userId, groupId, etc.)
- ☐ Using BigDecimal for all monetary amounts
- ☐ Passwords are hashed (passwordHash), not plain text
- ☐ Audit fields (createdAt, updatedAt) in all entities
- ☐ Enums used for fixed value types (SplitType, Status)
- ☐ Methods represent entity operations (what entity can DO)
- ☐ No business logic in entities (keep them simple)

---

## 🎯 What's Next?

**Step 4**: Add Service Layer
- UserService, GroupService, ExpenseService
- Services contain business logic (entities just hold data)
- Services orchestrate multiple entities

**Why separate?**
- **Entities** = Data holders (POJOs)
- **Services** = Business logic (operations)
- Separation of concerns (SOLID principle)

---

**Key Takeaways**:
1. Attributes = Data fields (what entity STORES)
2. Methods = Operations (what entity can DO)
3. Use BigDecimal for money, String for IDs
4. Always add audit fields (createdAt, updatedAt)
5. Enums for fixed value types
6. Value Objects for calculated data (Balance)
7. Keep entities simple (no complex business logic)

Ready for Step 4? Let's add Services! 🚀
