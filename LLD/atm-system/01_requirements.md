# ATM System - Requirements Analysis

> **For Beginners**: Requirements are the foundation of any design. We split them into two types:
> - **Functional Requirements**: WHAT the system should do (features)
> - **Non-Functional Requirements**: HOW the system should behave (performance, security, etc.)

---

## Problem Statement

Design and implement an **ATM (Automated Teller Machine) system** that allows users to perform basic banking operations such as balance inquiry, cash withdrawal, and cash deposit, with secure authentication and proper cash management.

---

## 1. Functional Requirements

> **Beginner Tip**: Think of functional requirements as user stories - "As a user, I want to..."

### 1.1 User Authentication
- **FR-1.1**: Users must insert a valid ATM card to start a session
- **FR-1.2**: System must validate the card (check if card is valid, not expired, not blocked)
- **FR-1.3**: Users must enter a 4-digit PIN for authentication
- **FR-1.4**: System allows maximum 3 PIN attempts; card is blocked after 3 failed attempts
- **FR-1.5**: System must validate PIN against the banking backend
- **FR-1.6**: Session timeout after 2 minutes of inactivity

### 1.2 Balance Inquiry
- **FR-2.1**: Authenticated users can check their account balance
- **FR-2.2**: System displays current balance in real-time
- **FR-2.3**: System can show available balance vs total balance (considering pending transactions)
- **FR-2.4**: User can choose to print a balance receipt (optional)

### 1.3 Cash Withdrawal
- **FR-3.1**: Users can withdraw cash in denominations of 100, 500, 2000 (configurable)
- **FR-3.2**: Minimum withdrawal: ₹100, Maximum withdrawal: ₹20,000 per transaction
- **FR-3.3**: Daily withdrawal limit: ₹50,000 per card
- **FR-3.4**: System validates sufficient account balance before dispensing cash
- **FR-3.5**: System validates sufficient cash in ATM before dispensing
- **FR-3.6**: System dispenses cash only after successful account deduction
- **FR-3.7**: System prints a transaction receipt after successful withdrawal
- **FR-3.8**: If cash dispensing fails, system must reverse the transaction (rollback)

### 1.4 Cash Deposit
- **FR-4.1**: Users can deposit cash into their account
- **FR-4.2**: System accepts notes in denominations of 100, 500, 2000
- **FR-4.3**: Maximum deposit: ₹50,000 per transaction
- **FR-4.4**: System counts and validates deposited cash
- **FR-4.5**: System credits account only after successful cash validation
- **FR-4.6**: System prints deposit receipt with transaction details

### 1.5 Transaction Management
- **FR-5.1**: Every transaction must have a unique transaction ID
- **FR-5.2**: System records transaction type, amount, timestamp, status
- **FR-5.3**: System maintains transaction history for audit
- **FR-5.4**: Failed transactions must be logged with reason
- **FR-5.5**: System supports transaction reversal in case of failures

### 1.6 Banking Service Integration
- **FR-6.1**: ATM communicates with backend banking service for account validation
- **FR-6.2**: Real-time balance updates to banking backend
- **FR-6.3**: PIN validation through secure banking API
- **FR-6.4**: Transaction posting to central banking system
- **FR-6.5**: Handle backend service failures gracefully (retry, timeout)

### 1.7 Cash Dispenser Management
- **FR-7.1**: ATM maintains cash inventory by denomination
- **FR-7.2**: System alerts when cash falls below threshold (e.g., 20% of capacity)
- **FR-7.3**: System prevents withdrawal when insufficient cash
- **FR-7.4**: Administrator can reload cash (admin operation)
- **FR-7.5**: System tracks cash dispensed vs cash loaded

### 1.8 User Interface
- **FR-8.1**: Display welcome screen when idle
- **FR-8.2**: Show menu options clearly (Balance, Withdraw, Deposit, Exit)
- **FR-8.3**: Display clear instructions at each step
- **FR-8.4**: Show error messages in user-friendly language
- **FR-8.5**: Allow users to cancel transaction at any step
- **FR-8.6**: Return card before dispensing cash (security)

### 1.9 Extensibility (Future Features)
- **FR-9.1**: Support for mini-statement (last 5 transactions)
- **FR-9.2**: Fund transfer between accounts
- **FR-9.3**: Multi-currency support
- **FR-9.4**: Bill payment integration
- **FR-9.5**: Cardless withdrawal (OTP-based)

---

## 2. Non-Functional Requirements

> **Beginner Tip**: Non-functional requirements define quality attributes. They answer "How well should it work?"

### 2.1 Security
- **NFR-1.1**: PIN must be encrypted during transmission and storage
- **NFR-1.2**: Card data must not be logged or stored locally
- **NFR-1.3**: Session data must be cleared after user completes transaction
- **NFR-1.4**: All communications with banking backend must use TLS/SSL
- **NFR-1.5**: Physical cash dispenser must have anti-tampering mechanisms
- **NFR-1.6**: Implement audit logging for all operations

### 2.2 Performance
- **NFR-2.1**: Balance inquiry response time: < 2 seconds
- **NFR-2.2**: Cash withdrawal process: < 30 seconds end-to-end
- **NFR-2.3**: PIN validation: < 1 second
- **NFR-2.4**: System should handle at least 100 transactions per day per ATM
- **NFR-2.5**: Database query response time: < 500ms

### 2.3 Reliability & Availability
- **NFR-3.1**: System uptime: 99.5% (excluding scheduled maintenance)
- **NFR-3.2**: Automatic retry for transient backend failures (max 3 retries)
- **NFR-3.3**: Graceful degradation: if one service fails, others continue
- **NFR-3.4**: Transaction rollback in case of failures (ACID properties)
- **NFR-3.5**: System logging for debugging and monitoring

### 2.4 Concurrency & Consistency
- **NFR-4.1**: Support only one active session per ATM at a time
- **NFR-4.2**: Handle concurrent access to backend (optimistic/pessimistic locking)
- **NFR-4.3**: Prevent race conditions in cash dispensing
- **NFR-4.4**: Ensure atomicity: deduct balance + dispense cash must be atomic
- **NFR-4.5**: Handle concurrent transactions to same account from different ATMs

### 2.5 Maintainability
- **NFR-5.1**: Code must follow SOLID principles
- **NFR-5.2**: Use design patterns for extensibility
- **NFR-5.3**: Clear separation of concerns (layered architecture)
- **NFR-5.4**: Comprehensive logging for troubleshooting
- **NFR-5.5**: Code documentation and comments

### 2.6 Usability
- **NFR-6.1**: Interface must be intuitive for non-technical users
- **NFR-6.2**: Support multiple languages (English, Hindi, regional)
- **NFR-6.3**: Accessible design for visually impaired (audio support)
- **NFR-6.4**: Clear timeout warnings before session expiry

### 2.7 Scalability
- **NFR-7.1**: Design should support multiple ATMs connecting to same backend
- **NFR-7.2**: Backend should handle 1000+ ATMs concurrently
- **NFR-7.3**: Database should support millions of transaction records
- **NFR-7.4**: Easy to add new transaction types without major refactoring

---

## 3. Key Assumptions

> **Beginner Tip**: Always list assumptions to clarify scope and boundaries

1. **Card Type**: We assume magnetic stripe or chip-based cards (not contactless/NFC initially)
2. **Network**: ATM has reliable internet connectivity to backend (handle failures gracefully)
3. **Cash Denominations**: Support for ₹100, ₹500, ₹2000 notes only
4. **Single Account**: One card maps to one primary account (no account selection initially)
5. **Currency**: Single currency support (INR) initially; multi-currency is future scope
6. **Banking Backend**: We assume a REST API exists for account validation, balance inquiry, transaction posting
7. **Hardware**: We assume physical hardware (card reader, cash dispenser, keypad, screen) interfaces are abstracted
8. **Admin Operations**: Cash loading, ATM configuration handled separately (admin interface)
9. **Transaction Limits**: Limits are configurable per bank policy
10. **No Check Deposit**: Only cash deposit supported initially (check deposit is future scope)
11. **No Passbook Printing**: Receipt printing only (passbook update not supported)
12. **Session Handling**: One user at a time per ATM (no parallel sessions)

---

## 4. Out of Scope (Not Covered in This LLD)

> **Interview Tip**: Clarifying out-of-scope items shows clear thinking

1. Mobile app integration
2. Biometric authentication (fingerprint/face recognition)
3. Check deposit functionality
4. Passbook printing
5. Loan disbursement
6. Account opening/KYC
7. Multi-factor authentication (MFA)
8. ATM network routing (switch/gateway design)
9. Fraud detection and prevention algorithms
10. Hardware-level security (skimming prevention, camera surveillance)

---

## 5. Success Criteria

> **How do we know the design is successful?**

1. ✅ All functional requirements are addressed in the design
2. ✅ Non-functional requirements are considered (security, performance, concurrency)
3. ✅ Design follows SOLID principles and uses appropriate design patterns
4. ✅ Design is extensible (easy to add new features)
5. ✅ Clear separation of concerns (layered architecture)
6. ✅ Concurrency and consistency are handled properly
7. ✅ Code is beginner-friendly with proper comments and explanations

---

## Next Steps

Now that we have clear requirements, we'll proceed to:
1. **Use Case Diagram** - Visualize actors and their interactions
2. **Class Diagrams** - Design the object-oriented structure (step-by-step)
3. **Sequence Diagrams** - Show how objects interact over time
4. **Design Patterns** - Apply proven solutions to common problems

---

**Ready to move to Phase 2: Use Case Modeling!** 🚀
