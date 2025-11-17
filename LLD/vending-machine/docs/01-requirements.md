# Vending Machine - Low Level Design (LLD)

## Problem Statement
Design and implement a Vending Machine system that allows users to select products, insert coins/notes, dispense products, and return change. The system should manage inventory, handle payments, and use the State design pattern for its operations.

---

## 1. Functional Requirements

### 1.1 Product Management
- **FR-1.1.1**: System must maintain a catalog of products with the following attributes:
  - Product ID (unique identifier)
  - Product Name
  - Price
  - Category (Beverage, Snack, Food, etc.)

- **FR-1.1.2**: Each product must have product code/slot number for selection (e.g., A1, B2, C3)

- **FR-1.1.3**: System must display available products with:
  - Product name
  - Price
  - Availability status (In Stock / Out of Stock)

### 1.2 Inventory Management
- **FR-2.1**: System must track quantity of each product in real-time

- **FR-2.2**: System must prevent product dispensing when:
  - Product quantity is zero
  - Product slot is empty

- **FR-2.3**: System must update inventory after successful product dispense

- **FR-2.4**: System must support inventory refill by admin/operator

- **FR-2.5**: System must alert when product quantity falls below minimum threshold (e.g., 2 items)

### 1.3 Payment Handling
- **FR-3.1**: System must accept multiple forms of payment:
  - Coins (₹1, ₹2, ₹5, ₹10)
  - Notes (₹10, ₹20, ₹50, ₹100, ₹500)

- **FR-3.2**: System must track total amount inserted by user

- **FR-3.3**: System must validate payment against selected product price

- **FR-3.4**: System must calculate and return change when:
  - Payment > Product Price

- **FR-3.5**: System must maintain coin/note inventory for giving change

- **FR-3.6**: System must handle "insufficient change" scenario gracefully

- **FR-3.7**: System must refund full amount if:
  - User cancels transaction
  - Product becomes unavailable
  - Exact change cannot be returned

### 1.4 State Management (Core Requirement)
- **FR-4.1**: System must operate in following states:
  - **Idle State**: Default state, waiting for user interaction
  - **Product Selection State**: User can browse and select products
  - **Payment State**: User inserts coins/notes
  - **Dispense State**: Product is being dispensed
  - **Return Change State**: Change is being calculated and returned

- **FR-4.2**: System must enforce valid state transitions

- **FR-4.3**: Each state must handle specific operations only

### 1.5 User Interaction
- **FR-5.1**: User must be able to:
  - View available products and prices
  - Select a product by entering product code
  - Insert coins/notes one at a time
  - Cancel transaction at any time before dispensing
  - Collect product from dispense tray
  - Collect change from change tray

- **FR-5.2**: System must provide clear feedback messages:
  - "Please select a product"
  - "Insert ₹X more"
  - "Dispensing product..."
  - "Please collect your change: ₹X"
  - "Transaction cancelled. Collect refund: ₹X"

### 1.6 Admin Operations
- **FR-6.1**: Admin must be able to:
  - Add new products to catalog
  - Remove products from catalog
  - Update product prices
  - Refill product inventory
  - Refill coin/note inventory
  - View sales reports
  - Reset machine to idle state

---

## 2. Non-Functional Requirements

### 2.1 Performance
- **NFR-1.1**: Product selection must respond within 100ms
- **NFR-1.2**: Payment validation must complete within 200ms
- **NFR-1.3**: Product dispense mechanism must complete within 3 seconds
- **NFR-1.4**: Change calculation must complete within 500ms

### 2.2 Reliability
- **NFR-2.1**: System must have 99.9% uptime during operating hours
- **NFR-2.2**: System must handle hardware failures gracefully (dispense motor jam, coin acceptor jam)
- **NFR-2.3**: System must maintain transaction log for audit purposes
- **NFR-2.4**: System must not lose user payment data during failures

### 2.3 Concurrency
- **NFR-3.1**: System must handle only ONE transaction at a time (single-threaded operation)
- **NFR-3.2**: System must lock resources during active transaction
- **NFR-3.3**: System must queue multiple users if needed (optional enhancement)
- **NFR-3.4**: Inventory updates must be atomic (all-or-nothing)

### 2.4 Security
- **NFR-4.1**: Admin operations must require authentication (PIN/Password)
- **NFR-4.2**: System must detect and reject counterfeit coins/notes (using hardware sensors)
- **NFR-4.3**: System must maintain tamper-proof transaction logs
- **NFR-4.4**: Cash storage compartment must be physically secure

### 2.5 Usability
- **NFR-5.1**: User interface must be intuitive for non-technical users
- **NFR-5.2**: Product codes must be clearly visible on machine
- **NFR-5.3**: Error messages must be clear and actionable
- **NFR-5.4**: Maximum transaction time should be under 60 seconds

### 2.6 Maintainability
- **NFR-6.1**: Code must follow SOLID principles
- **NFR-6.2**: System must use design patterns appropriately (State, Singleton, Factory)
- **NFR-6.3**: Code must be well-documented with comments
- **NFR-6.4**: Components must be loosely coupled for easy testing

### 2.7 Scalability
- **NFR-7.1**: System should support up to 50 product slots
- **NFR-7.2**: System should support up to 100 items per product type
- **NFR-7.3**: Design should allow easy addition of new payment methods (cards, UPI)

### 2.8 Data Persistence
- **NFR-8.1**: Inventory data must persist across system restarts
- **NFR-8.2**: Transaction history must be stored for at least 30 days
- **NFR-8.3**: Configuration data (prices, products) must be persisted

---

## 3. Assumptions

### 3.1 Business Assumptions
- Machine operates 24/7 with periodic maintenance windows
- Only one user can interact with machine at a time
- Admin has physical access to machine for refills
- All prices are in Indian Rupees (₹)
- Machine is located in a secure, monitored area

### 3.2 Technical Assumptions
- Hardware sensors (coin validator, note validator) are pre-integrated
- Dispense motor/mechanism is controllable via software commands
- System has persistent storage (database or file system)
- System runs on embedded Linux or Java-capable hardware
- Network connectivity is optional (standalone operation)

### 3.3 Payment Assumptions
- Only cash payments initially (coins and notes)
- Machine has sufficient change at start of day
- Invalid/damaged currency is rejected by hardware
- Partial payments are not saved (must complete in one session)

### 3.4 Inventory Assumptions
- Products do not expire (or expiry tracking is separate concern)
- All products fit standard slot sizes
- Products do not require refrigeration (or cooling is separate system)
- Products cannot be partially dispensed (binary: success or failure)

---

## 4. Constraints

### 4.1 Technical Constraints
- Must use Java programming language
- Must implement State design pattern for state management
- Cannot use external payment gateway APIs (cash only)
- Limited memory: must optimize data structures
- Limited display: text-based UI (no touch screen)

### 4.2 Business Constraints
- Maximum product price: ₹500
- Minimum product price: ₹5
- Maximum single payment: ₹500 note
- Cannot give change exceeding ₹500

### 4.3 Hardware Constraints
- Fixed number of product slots (e.g., 30-50 slots)
- Fixed cash storage capacity
- Dispense mechanism can handle products up to 500g weight
- Display limited to 20x4 character LCD

### 4.4 Operational Constraints
- Admin must refill inventory manually
- Machine must be serviced weekly
- Transaction timeout: 60 seconds
- Idle timeout: 30 seconds (returns to idle state)

---

## 5. Out of Scope (Not Included in This LLD)

### 5.1 Not Implemented
- Digital payments (credit card, debit card, UPI, mobile wallets)
- Touchscreen interface
- Network-based inventory management
- Remote monitoring and diagnostics
- Multi-machine management system
- Refrigeration control
- Product expiry tracking
- Customer loyalty programs
- Receipt printing
- Real-time analytics dashboard

### 5.2 Future Enhancements
- Integration with payment gateways
- IoT connectivity for remote monitoring
- Dynamic pricing based on demand
- Product recommendations
- Mobile app integration
- Cashless operation

---

## 6. Success Criteria

The Vending Machine LLD will be considered successful if:

1. ✅ User can successfully purchase a product end-to-end
2. ✅ System correctly handles insufficient payment scenarios
3. ✅ System correctly calculates and returns change
4. ✅ System prevents dispensing when product is out of stock
5. ✅ System prevents dispensing when exact change cannot be given
6. ✅ State pattern is correctly implemented with clear state transitions
7. ✅ Inventory is accurately maintained after each transaction
8. ✅ System handles cancellation and refunds correctly
9. ✅ Code follows SOLID principles and uses appropriate design patterns
10. ✅ All components are unit testable

---

## 7. Key Design Challenges

### Challenge 1: Change Calculation Algorithm
**Problem**: How to calculate optimal change with limited coin/note inventory?

**Solution**: Use greedy algorithm (largest denomination first) with validation

### Challenge 2: State Transition Management
**Problem**: How to prevent invalid state transitions (e.g., dispense before payment)?

**Solution**: Use State design pattern with strict transition rules

### Challenge 3: Inventory Consistency
**Problem**: How to ensure inventory doesn't go negative in concurrent scenarios?

**Solution**: Use synchronized methods or database transactions

### Challenge 4: Transaction Atomicity
**Problem**: What if dispense fails after payment is received?

**Solution**: Implement transaction rollback mechanism (refund user)

### Challenge 5: Insufficient Change
**Problem**: What if machine can't give exact change?

**Solution**: Reject transaction upfront or dispense product with IOU for change

---

## Next Steps

With requirements clearly defined, we will now proceed to:

1. **Phase 2**: Create use case diagrams and identify actors
2. **Phase 3**: Design core domain entities (Product, Inventory, Money)
3. **Phase 4**: Implement State pattern for state management
4. **Phase 5**: Design payment and change calculation logic
5. **Phase 6-10**: Complete design, patterns, sequences, and implementation

---

**Document Version**: 1.0
**Last Updated**: 2025-11-17
**Author**: LLD Design Team
