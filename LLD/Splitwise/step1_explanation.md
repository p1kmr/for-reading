# 📦 STEP 1: Identify Core Domain Entities

## 🎯 What Did We Add?

We identified **4 core entities** (domain objects):
1. **User**
2. **Group**
3. **Expense**
4. **Transaction**

---

## 🤔 Why Start Here?

### Beginner Principle: **Find the NOUNS**

When designing a system, always start by identifying the **nouns** (things) in the requirements:
- ✅ "**Users** can create **groups**"
- ✅ "**Users** can add **expenses**"
- ✅ "**Users** can record **payments**"

These nouns become our **Entities** (classes).

### Think of Entities as "Boxes That Hold Data"
- User = Box that holds user information
- Group = Box that holds group information
- Expense = Box that holds expense details
- Transaction = Box that holds payment records

---

## 🔍 How Did We Choose These Entities?

### Entity 1: **User** 👤

**From Requirements**:
- FR-1.1: "Users can register"
- FR-1.4: "Users can view their profile"
- FR-4.2: "Users can view balances"

**Why it's an Entity**:
- Represents a real person in the system
- Has its own data (name, email, etc.)
- Exists independently (user exists even without groups)

**Examples**: Alice, Bob, Charlie

---

### Entity 2: **Group** 👥

**From Requirements**:
- FR-2.1: "Users can create groups"
- FR-2.2: "Users can add members to groups"
- FR-3.5: "Expenses can be added to groups"

**Why it's an Entity**:
- Represents a collection of users
- Has its own data (name, description, members)
- Exists independently (group exists even if no expenses yet)

**Examples**: "Goa Trip 2025", "Office Lunch", "Roommate Expenses"

---

### Entity 3: **Expense** 💰

**From Requirements**:
- FR-3.1: "Users can add expenses"
- FR-3.2: "Support EQUAL split"
- FR-3.6: "Users can view expense history"

**Why it's an Entity**:
- Represents money spent
- Has its own data (amount, description, date, who paid)
- Core domain concept (system is all about expenses!)

**Examples**: "Hotel Booking ₹6000", "Dinner ₹3000"

---

### Entity 4: **Transaction** 💸

**From Requirements**:
- FR-5.1: "Users can record payments"
- FR-5.3: "Users can view transaction history"
- FR-5.2: "Transaction is recorded when user pays"

**Why it's an Entity**:
- Represents a payment (settlement)
- Different from Expense (Expense = spending money, Transaction = paying back)
- Has its own data (who paid, who received, amount, date)

**Examples**: "Bob paid Alice ₹500", "Charlie settled ₹1000 with Alice"

---

## 🚫 What We Did NOT Add (Yet)

### Why no Balance entity?
- Balance is **calculated** from Expenses and Transactions
- Not stored as separate entity (can be computed)
- We might add it later for optimization (caching)

### Why no Split entity?
- Split is part of Expense (how expense is divided)
- We'll add it in later steps as a component of Expense

### Why no Notification entity?
- Not core to the domain (nice-to-have feature)
- Can be added later (low priority for MVP)

---

## 📐 Design Decisions

### Decision 1: Keep it Simple (KISS Principle)
- ✅ Start with minimum entities needed for MVP
- ✅ Only 4 entities (easy to understand)
- ❌ Don't add entities for every requirement

### Decision 2: Focus on Domain Concepts
- ✅ These entities exist in real world (User, Group, Expense, Payment)
- ✅ Non-technical person can understand them
- ❌ Don't include technical concepts yet (Database, API, etc.)

### Decision 3: Independent Entities First
- ✅ Each entity can exist on its own
- ✅ Not worried about relationships yet
- 🎯 Next step: Connect them with relationships

---

## 🎓 Beginner Tips

### Tip 1: Entities vs Classes
**Entity** = Special type of class that:
- Represents a domain concept
- Has a unique ID
- Is stored in database
- Has a lifecycle (created, updated, deleted)

**Example**:
```java
// User is an ENTITY
class User {
    private String userId;  // Unique ID ✅
    private String name;
    private String email;
    // ... stored in database
}

// StringUtils is NOT an entity (just helper methods)
class StringUtils {
    static String capitalize(String s) { ... }
    // No ID, not stored in DB
}
```

### Tip 2: Entity Naming
- ✅ Use singular nouns: User (not Users)
- ✅ Use clear names: Expense (not Cost or Payment)
- ✅ Avoid abbreviations: Transaction (not Txn)

### Tip 3: How to Identify Entities in Interview

**Interviewer**: "Design Splitwise"

**Your thought process**:
1. Listen for **nouns**: user, group, expense, payment
2. Ask yourself: "Does this thing have its own data?"
   - User has name, email → YES, entity
   - "Login" is an action → NO, not entity
3. Check if it exists independently:
   - User exists even without groups → YES, entity
   - "Balance" is calculated from expenses → Maybe not entity

---

## 📊 Visual Thinking

Think of our system like a **filing cabinet**:
```
🗄️ Filing Cabinet (System)
   ├─ 📁 Users Drawer
   │    ├─ 📄 Alice
   │    ├─ 📄 Bob
   │    └─ 📄 Charlie
   │
   ├─ 📁 Groups Drawer
   │    ├─ 📄 Goa Trip 2025
   │    └─ 📄 Office Lunch
   │
   ├─ 📁 Expenses Drawer
   │    ├─ 📄 Hotel Booking ₹6000
   │    └─ 📄 Dinner ₹3000
   │
   └─ 📁 Transactions Drawer
        ├─ 📄 Bob → Alice ₹500
        └─ 📄 Charlie → Alice ₹1000
```

Each drawer = Entity type
Each file = Instance of entity

---

## 🎯 What Should You Draw First? (Whiteboard)

When interviewer says "Design Splitwise":

**Step-by-step**:
1. ☐ Draw 4 boxes (rectangles)
2. ☐ Label them: User, Group, Expense, Transaction
3. ☐ Add `<<Entity>>` stereotype (shows you know terminology)
4. ☐ Verbally explain: "These are the core domain entities representing our nouns"
5. ☐ Mention: "Next I'll add relationships between them"

**Time**: 2-3 minutes

**What to say**:
> "Let me start by identifying the core entities. Looking at the requirements, I see 4 main nouns: User for people using the system, Group to organize expenses, Expense to track spending, and Transaction to record settlements. These will be my domain entities."

---

## ❌ Common Mistakes & Solutions

### Mistake 1: Adding Too Many Entities Upfront
**Wrong**:
```
User, Group, Expense, Transaction, Balance, Notification,
Payment, Split, Category, Comment, Like, Report, ...
```
**Right**:
```
User, Group, Expense, Transaction (4 core entities)
```
**Solution**: Start small, add more later as needed

### Mistake 2: Forgetting Transaction Entity
**Wrong**: Only User, Group, Expense
**Right**: Add Transaction (how else do you track settlements?)
**Solution**: Think through complete flow: Add expense → View balance → Settle up (Transaction needed!)

### Mistake 3: Adding Technical Entities
**Wrong**: Database, API, Controller, Service as entities
**Right**: Only domain concepts (User, Group, Expense, Transaction)
**Solution**: Entities = business concepts, not technical components

### Mistake 4: Confusing Attributes with Entities
**Wrong**: "Email" as an entity
**Right**: Email is an attribute of User entity
**Solution**: Ask: "Does this have its own lifecycle?" Email doesn't exist independently.

---

## 🔄 What Changes from Previous Step?

**Previous Step**: None (this is step 1!)

**This Step**:
- ✅ Added 4 entity boxes
- ✅ No attributes or methods yet (intentional!)
- ✅ No relationships yet (next step!)

---

## 🎯 Next Step Preview

**Step 2**: Add relationships between entities
- User → Group (users belong to groups)
- Group → Expense (groups have expenses)
- Expense → User (user paid the expense)
- Transaction → User (user paid/received)

---

## ✅ Key Takeaways

1. **Start with Nouns**: Find nouns in requirements → Entities
2. **Keep it Simple**: 4 entities for MVP (can add more later)
3. **Domain First**: Focus on business concepts, not technical details
4. **Independent Entities**: Each entity exists on its own
5. **Entities ≠ All Classes**: Entities are special (stored in DB, have IDs)

---

**Confidence Check**:
- ✅ Can you explain why we chose these 4 entities?
- ✅ Can you identify entities from a new problem statement?
- ✅ Can you explain Entity vs regular class?

If yes → Ready for Step 2! 🚀
If no → Re-read this document and try exercise below

---

## 🏋️ Practice Exercise

**Problem**: Design a simple Library Management System

**Requirements**:
- Librarians can add books
- Members can borrow books
- Members can return books
- System tracks due dates

**Your Task**: Identify 3-4 core entities

**Solution** (don't peek!):
<details>
<summary>Click to reveal</summary>

1. **Book** (represents physical books)
2. **Member** (represents library members)
3. **Loan** (represents a borrowing transaction)
4. (Optional) **Librarian** (or just extend Member with role)

</details>

---

**Next**: Let's connect these entities with relationships in Step 2! 🔗
