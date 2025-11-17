# 📋 Splitwise System - Requirements

## 🎯 Problem Statement
Design and implement a **Splitwise System** that allows users to split expenses among groups and individuals. The system should handle expense tracking, balance calculations, and settlement of debts between users.

---

## ✅ Functional Requirements

### 1. User Management
- **FR-1.1**: Users can register with email, name, and phone number
- **FR-1.2**: Users can login/logout securely
- **FR-1.3**: Users can update their profile (name, phone, email)
- **FR-1.4**: Users can view their profile with total balance summary
- **FR-1.5**: System should track user relationships (friends who share expenses)

**Example**: "Alice creates an account with email alice@example.com"

---

### 2. Group Management
- **FR-2.1**: Users can create groups with a name and description
- **FR-2.2**: Users can add members to groups (multiple users)
- **FR-2.3**: Users can remove members from groups (if they're admin)
- **FR-2.4**: Users can view all groups they're part of
- **FR-2.5**: Groups can have an admin/creator
- **FR-2.6**: Users can leave a group if their balance is settled
- **FR-2.7**: Groups can be deleted by admin if all balances are zero

**Example**: "Alice creates a group 'Goa Trip 2025' and adds Bob, Charlie"

---

### 3. Expense Management
- **FR-3.1**: Users can add expenses with:
  - Description (e.g., "Dinner at Restaurant")
  - Amount (e.g., 3000.00)
  - Paid by (who paid the amount)
  - Split type (EQUAL, EXACT, PERCENTAGE)
  - Date and time

- **FR-3.2**: Support for **EQUAL split**: Amount divided equally among all participants
  - Example: 3000 split equally among 3 people = 1000 each

- **FR-3.3**: Support for **EXACT split**: Specify exact amount for each person
  - Example: Alice paid 3000. Bob owes 1200, Charlie owes 1800

- **FR-3.4**: Support for **PERCENTAGE split**: Specify percentage for each person
  - Example: Alice paid 3000. Bob owes 40% (1200), Charlie owes 60% (1800)

- **FR-3.5**: Expenses can be added to groups or between individuals
- **FR-3.6**: Users can view expense history (individual & group)
- **FR-3.7**: Users can edit/delete their own expenses
- **FR-3.8**: System should validate that split amounts sum up to total expense

**Example**: "Alice adds expense 'Hotel booking' of 6000, paid by Alice, split equally among Alice, Bob, Charlie (2000 each)"

---

### 4. Balance Management
- **FR-4.1**: System calculates balances between users automatically
- **FR-4.2**: Users can view:
  - Overall balance (total you owe / are owed)
  - Individual balances (who owes you / whom you owe)
  - Group-wise balances

- **FR-4.3**: Balance should be updated in real-time after expense is added
- **FR-4.4**: System should show net balance (optimized - reduce transactions)
  - Example: If Alice owes Bob 100, and Bob owes Alice 60, net = Alice owes Bob 40

- **FR-4.5**: Users can view balance history/timeline

**Example**: "Alice sees: Bob owes you ₹500, You owe Charlie ₹300"

---

### 5. Transaction Management (Settlement)
- **FR-5.1**: Users can record payments/settlements
- **FR-5.2**: When user A pays user B:
  - Transaction is recorded
  - Balances are updated
  - Both users are notified

- **FR-5.3**: Users can view transaction history
- **FR-5.4**: System should support "Settle Up" feature:
  - Show all outstanding balances
  - Allow user to settle all at once

- **FR-5.5**: Support partial settlements
- **FR-5.6**: Generate receipt/confirmation for settlements

**Example**: "Bob pays Alice ₹500 to settle their balance"

---

### 6. Notification & Activity Feed
- **FR-6.1**: Users receive notifications when:
  - Added to a group
  - New expense is added that involves them
  - Someone settles up with them

- **FR-6.2**: Users can view activity feed showing recent activities
- **FR-6.3**: Support email/push notifications (configurable)

---

### 7. Reporting & Analytics
- **FR-7.1**: Users can view:
  - Monthly expense reports
  - Category-wise spending
  - Group-wise spending analysis

- **FR-7.2**: Export expense history to CSV/PDF
- **FR-7.3**: Visualize spending patterns (charts/graphs)

---

## 🔒 Non-Functional Requirements

### 1. Performance
- **NFR-1.1**: System should handle **1000+ concurrent users**
- **NFR-1.2**: Expense addition should complete within **200ms**
- **NFR-1.3**: Balance calculation should be **real-time** (< 100ms)
- **NFR-1.4**: Database queries should be optimized with proper indexes
- **NFR-1.5**: API response time should be < 500ms for 95th percentile

---

### 2. Scalability
- **NFR-2.1**: System should scale horizontally (add more servers)
- **NFR-2.2**: Database should support **10 million+ users**
- **NFR-2.3**: Should handle **100k+ expenses per day**
- **NFR-2.4**: Use caching for frequently accessed data (balances, user profiles)

---

### 3. Reliability & Availability
- **NFR-3.1**: System uptime: **99.9%** (3 nines)
- **NFR-3.2**: Database should have automatic backups (daily)
- **NFR-3.3**: Implement retry mechanisms for failed transactions
- **NFR-3.4**: Graceful degradation (if one service fails, others should work)

---

### 4. Consistency & Concurrency
- **NFR-4.1**: Balance calculations must be **ACID compliant**
- **NFR-4.2**: Handle concurrent expense additions without data corruption
- **NFR-4.3**: Use **optimistic locking** for balance updates
- **NFR-4.4**: Prevent race conditions in settlement transactions
- **NFR-4.5**: Ensure eventual consistency for non-critical operations (notifications)

**Critical**: If Alice and Bob simultaneously add expenses, balances should remain consistent

---

### 5. Security
- **NFR-5.1**: Passwords should be hashed using **bcrypt/Argon2**
- **NFR-5.2**: Implement **JWT-based authentication**
- **NFR-5.3**: API should be protected with rate limiting
- **NFR-5.4**: Validate all user inputs to prevent SQL injection
- **NFR-5.5**: Implement role-based access control (RBAC)
- **NFR-5.6**: Audit log for all financial transactions

---

### 6. Maintainability
- **NFR-6.1**: Code should follow **SOLID principles**
- **NFR-6.2**: Use **design patterns** (Strategy, Factory, Repository)
- **NFR-6.3**: Comprehensive unit test coverage (> 80%)
- **NFR-6.4**: Well-documented APIs (Swagger/OpenAPI)
- **NFR-6.5**: Clear separation of concerns (Layered architecture)

---

### 7. Usability
- **NFR-7.1**: Simple and intuitive UI
- **NFR-7.2**: Support for multiple currencies (USD, INR, EUR)
- **NFR-7.3**: Mobile-responsive design
- **NFR-7.4**: Accessibility compliance (WCAG 2.1)

---

### 8. Data Integrity
- **NFR-8.1**: All monetary values should use **Decimal/BigDecimal** (no float)
- **NFR-8.2**: Expense splits must always sum to total amount
- **NFR-8.3**: Database constraints to ensure referential integrity
- **NFR-8.4**: No negative balances without proper validation

---

## 🎯 Assumptions

1. **Single Currency**: For simplicity, assume all transactions in one currency (can be extended later)
2. **Trust Model**: Users trust each other (no fraud detection initially)
3. **Internet Connectivity**: Users have stable internet connection
4. **Group Size**: Typical group size is 2-20 people (not 1000s)
5. **Expense Frequency**: Users add expenses occasionally (not real-time streaming)
6. **Settlement**: Users settle outside the app (cash/UPI) and just record it in the system
7. **Tax Calculation**: No tax/tip calculations in MVP (can be added later)

---

## 🚫 Out of Scope (Future Enhancements)

1. Integration with payment gateways (UPI, PayPal) for in-app settlements
2. Multi-currency support with exchange rates
3. Recurring expenses (subscriptions)
4. Bill scanning using OCR
5. Social features (comments, likes on expenses)
6. Credit limit tracking
7. Integration with accounting software

---

## 📊 Priority Matrix

| Requirement | Priority | Complexity | MVP? |
|-------------|----------|------------|------|
| User Registration/Login | HIGH | Low | ✅ Yes |
| Add Expense (EQUAL split) | HIGH | Medium | ✅ Yes |
| Add Expense (EXACT/PERCENTAGE) | MEDIUM | Medium | ✅ Yes |
| Balance Calculation | HIGH | High | ✅ Yes |
| Settle Up | HIGH | Medium | ✅ Yes |
| Group Management | HIGH | Low | ✅ Yes |
| Notifications | LOW | Medium | ❌ No |
| Reports & Analytics | LOW | High | ❌ No |
| Multi-currency | LOW | High | ❌ No |

---

## ✅ Success Metrics

1. **User can add an expense in < 30 seconds**
2. **Balance calculations are accurate to 2 decimal places**
3. **System handles 1000 concurrent users without performance degradation**
4. **Zero data loss for financial transactions**
5. **API uptime > 99.9%**

---

**Next Steps**: Create Use Case Diagram to visualize user interactions
