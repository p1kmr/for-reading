# 🎭 Use Case Diagram - Detailed Explanation

## 📚 What is a Use Case Diagram?

A **Use Case Diagram** shows:
- **Who** uses the system (Actors)
- **What** they can do (Use Cases)
- **How** different actions relate to each other

Think of it as a **"menu of features"** that shows what each type of user can do.

---

## 👥 Actors (Who uses the system?)

### 1. 👤 User (Registered User)
- **Definition**: Any person who has created an account and logged in
- **Capabilities**: Can perform most operations in the system
- **Examples**: Alice, Bob, Charlie who split expenses together

### 2. 👑 Group Admin (Group Creator)
- **Definition**: A User who created a specific group
- **Special Powers**:
  - Can add/remove members from THEIR group
  - Can delete the group
  - Has all User capabilities PLUS admin privileges
- **Note**: Same person can be Admin in one group and regular User in another

### 3. 🤖 System (Background Services)
- **Definition**: Automated processes that run without user interaction
- **Responsibilities**:
  - Calculate balances automatically
  - Send notifications
  - Maintain data consistency

---

## 🎯 Use Cases by Category

### 📦 Category 1: User Management

| Use Case | Description | Actor | Example |
|----------|-------------|-------|---------|
| **Register Account** | Create new account with email, name, password | User | Alice signs up with alice@gmail.com |
| **Login/Logout** | Authenticate and start/end session | User | Alice logs in to see her balances |
| **Update Profile** | Change name, email, phone, password | User | Alice updates her phone number |
| **View Profile & Balance** | See personal info and overall balance summary | User | Alice sees "You owe ₹500 overall" |

**Beginner Tip**: Start with these! Every application needs user management.

---

### 📦 Category 2: Group Management

| Use Case | Description | Actor | Example |
|----------|-------------|-------|---------|
| **Create Group** | Make new group with name and description | User (becomes Admin) | Alice creates "Goa Trip 2025" |
| **Add Members** | Invite users to join group | Group Admin | Alice adds Bob and Charlie |
| **Remove Members** | Remove user from group | Group Admin | Alice removes Bob (he canceled trip) |
| **View Groups** | See all groups user is part of | User | Alice sees "Goa Trip", "Office Lunch" |
| **Leave Group** | Exit a group (only if balance = 0) | User | Bob leaves "Goa Trip" after settling |
| **Delete Group** | Permanently remove group | Group Admin | Alice deletes group after trip |

**Beginner Tip**: Groups are like "folders" to organize expenses. Example: "Roommate Expenses", "Trip to Paris"

---

### 📦 Category 3: Expense Management

| Use Case | Description | Actor | Example |
|----------|-------------|-------|---------|
| **Add Expense** | Record a new expense with details | User | Alice adds "Dinner ₹3000" |
| **Split Expense - EQUAL** | Divide amount equally among all | User | ₹3000 ÷ 3 people = ₹1000 each |
| **Split Expense - EXACT** | Specify exact amount for each person | User | Bob: ₹1200, Charlie: ₹1800 |
| **Split Expense - PERCENTAGE** | Specify percentage for each person | User | Bob: 40% (₹1200), Charlie: 60% (₹1800) |
| **Edit Expense** | Modify expense details | User (who created it) | Alice changes amount from 3000 to 3200 |
| **Delete Expense** | Remove expense from system | User (who created it) | Alice deletes wrong entry |
| **View Expense History** | See all past expenses | User | Alice sees all group expenses |

**Beginner Tip**:
- **EQUAL**: Most common (friends splitting equally)
- **EXACT**: When people ordered different items
- **PERCENTAGE**: Rare (maybe based on income/usage)

**Relationships**:
- "Add Expense" **includes** choosing a split type (EQUAL/EXACT/PERCENTAGE)
- This means you MUST choose a split type when adding expense

---

### 📦 Category 4: Balance Management

| Use Case | Description | Actor | Example |
|----------|-------------|-------|---------|
| **View Overall Balance** | See total amount you owe or are owed | User | "You owe ₹500 in total" |
| **View Individual Balances** | See balance with each person | User | "Bob owes you ₹200, You owe Charlie ₹700" |
| **View Group Balances** | See balances within a specific group | User | In "Goa Trip": You owe ₹1500 |
| **Calculate Net Balance** | System optimizes to reduce transactions | System (Auto) | Alice owes Bob ₹100, Bob owes Alice ₹60 → Net: Alice owes Bob ₹40 |

**Beginner Tip**:
- Balance = **Who owes whom how much**
- Positive balance = Others owe you (you'll receive money)
- Negative balance = You owe others (you need to pay)

**Why System calculates automatically?**
- When Alice adds expense "Dinner ₹3000", system automatically:
  1. Calculates splits (₹1000 each for 3 people)
  2. Updates balances (Bob owes Alice ₹1000, Charlie owes Alice ₹1000)
  3. Shows updated balances to all users

---

### 📦 Category 5: Settlement Management

| Use Case | Description | Actor | Example |
|----------|-------------|-------|---------|
| **Record Payment** | Mark that you paid someone | User | Bob records "Paid Alice ₹500" |
| **Settle Up** | Pay all outstanding balances to one person | User | Bob settles all ₹1000 with Alice |
| **Partial Settlement** | Pay part of what you owe | User | Bob pays ₹300 out of ₹1000 owed |
| **View Transaction History** | See all past settlements | User | Alice sees Bob paid her on Jan 15 |

**Beginner Tip**:
- Settlement happens OUTSIDE the app (cash, UPI, bank transfer)
- The app just **records** that it happened
- This updates balances accordingly

**Relationship**:
- "Settle Up" **includes** "Record Payment"
- Settle Up is a convenience feature that records multiple payments at once

---

### 📦 Category 6: Notifications & Reporting

| Use Case | Description | Actor | Example |
|----------|-------------|-------|---------|
| **Send Notifications** | Alert users about events | System (Auto) | "Alice added expense in 'Goa Trip'" |
| **View Activity Feed** | See recent activities in groups | User | Alice sees Bob added expense yesterday |
| **Generate Reports** | Create monthly/category reports | User | Alice sees "Spent ₹5000 in January" |
| **Export Data** | Download expense data to CSV/PDF | User | Alice exports all expenses to Excel |

**Beginner Tip**: Notifications keep everyone updated without them constantly checking the app.

---

## 🔗 Relationships Between Use Cases

### 1. **Includes** (必须包含)
Shown as: `UC-A -.includes.-> UC-B`

**Meaning**: UC-A MUST use UC-B to complete
- Cannot do A without doing B

**Examples**:
```
Add Expense -.includes.-> Split Expense (EQUAL/EXACT/PERCENTAGE)
```
- When adding expense, you MUST choose a split type
- Cannot add expense without specifying how to split

```
Settle Up -.includes.-> Record Payment
```
- Settle Up uses Record Payment multiple times internally

### 2. **Triggers** (触发)
Shown as: `UC-A -.triggers.-> UC-B`

**Meaning**: Doing UC-A causes UC-B to happen automatically
- B happens as a side effect of A

**Examples**:
```
Add Expense -.triggers.-> Calculate Balance
```
- When Alice adds expense, system automatically recalculates balances

```
Add Expense -.triggers.-> Send Notifications
```
- When Alice adds expense, system automatically notifies group members

---

## 🎬 Use Case Flows (Step-by-Step)

### Flow 1: Adding an Expense (EQUAL split)

**Actors**: User (Alice), System

**Preconditions**:
- Alice is logged in
- Group "Goa Trip" exists with Alice, Bob, Charlie

**Main Flow**:
1. Alice clicks "Add Expense"
2. Alice enters:
   - Description: "Hotel Booking"
   - Amount: ₹6000
   - Paid by: Alice
   - Split type: EQUAL
   - Participants: Alice, Bob, Charlie
3. System validates:
   - Amount > 0 ✅
   - All participants exist ✅
   - Alice is part of the group ✅
4. System calculates splits:
   - ₹6000 ÷ 3 = ₹2000 each
5. System updates balances:
   - Bob owes Alice: ₹2000
   - Charlie owes Alice: ₹2000
   - Alice's net: +₹4000 (she's owed)
6. System sends notifications to Bob and Charlie
7. System shows success message: "Expense added!"

**Postconditions**:
- Expense saved in database
- Balances updated
- All participants notified

---

### Flow 2: Settling Up

**Actors**: User (Bob), System

**Preconditions**:
- Bob owes Alice ₹2000

**Main Flow**:
1. Bob clicks "Settle Up with Alice"
2. System shows: "You owe Alice ₹2000"
3. Bob confirms: "Paid via UPI"
4. System records payment:
   - From: Bob
   - To: Alice
   - Amount: ₹2000
   - Date: 2025-01-15
5. System updates balances:
   - Bob owes Alice: ₹0 (settled)
6. System sends notification to Alice: "Bob settled ₹2000"
7. System shows success: "Balance settled!"

**Postconditions**:
- Payment recorded
- Balance between Bob and Alice = 0
- Alice notified

---

### Flow 3: Creating a Group

**Actors**: User (Alice), System

**Main Flow**:
1. Alice clicks "Create Group"
2. Alice enters:
   - Name: "Goa Trip 2025"
   - Description: "Beach vacation with friends"
3. System creates group:
   - Assigns Alice as Admin
   - Generates unique Group ID
4. Alice clicks "Add Members"
5. Alice selects: Bob, Charlie from her friends
6. System sends invitations/notifications
7. Bob and Charlie are added to group

**Postconditions**:
- Group created with 3 members
- Alice is Admin
- All members can now add expenses to this group

---

## 🎯 Why Use Case Diagrams?

### For Developers:
- ✅ Understand what features to build
- ✅ Identify actors and their permissions
- ✅ See relationships between features

### For Interviewers:
- ✅ Shows you understand the problem
- ✅ Helps structure the discussion
- ✅ Makes it easy to prioritize features (MVP first)

### For Beginners:
- ✅ Visual overview before diving into code
- ✅ Helps plan API endpoints (each use case = ~1 API)
- ✅ Easier to explain to non-technical people

---

## 📝 Common Mistakes & Solutions

### ❌ Mistake 1: Confusing Actors with Systems
**Wrong**: "Database" as an actor
**Right**: Actors are external entities (humans or external systems that initiate actions)
**Solution**: Database is part of implementation, not an actor

### ❌ Mistake 2: Too Many Details
**Wrong**: Including implementation details ("Hash password using bcrypt")
**Right**: High-level functionality ("Register Account")
**Solution**: Use Case = What, not How

### ❌ Mistake 3: Forgetting System Actor
**Wrong**: Only showing User actors
**Right**: System does automatic tasks (calculate balance, send notifications)
**Solution**: Add System actor for background processes

### ❌ Mistake 4: Not Showing Relationships
**Wrong**: All use cases isolated
**Right**: Show includes/extends/triggers relationships
**Solution**: Helps understand dependencies

---

## 🎓 Interview Tips

### Question: "How would you design Splitwise?"

**Answer Structure**:
1. **Start with actors**: "We have Users, Group Admins, and System"
2. **List main use cases**: "Core features are: User Management, Expense Management, Balance Calculation, Settlement"
3. **Show relationships**: "When a user adds an expense, it triggers balance calculation automatically"
4. **Prioritize for MVP**: "For MVP, I'd start with: User Registration, Add Expense (EQUAL split), View Balances, Record Payment"
5. **Mention trade-offs**: "We could add real-time notifications later, but MVP can work with just activity feed"

---

## ✅ Checklist: What to Draw First

When drawing Use Case Diagram on whiteboard:

1. ☐ Draw 3 actors: User, Group Admin, System
2. ☐ Draw main categories (5-6 boxes for grouping use cases)
3. ☐ Add core use cases:
   - Register, Login
   - Add Expense, Split Expense
   - View Balances
   - Settle Up
4. ☐ Connect actors to use cases (arrows)
5. ☐ Add 2-3 important relationships (includes/triggers)
6. ☐ Add brief notes for clarification

**Time**: 5-7 minutes on whiteboard

---

**Next Step**: Now that we understand WHAT the system does, let's design HOW to build it with Class Diagrams!
