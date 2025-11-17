# Vending Machine - Low Level Design (LLD)

## Overview

Complete Low-Level Design for a Vending Machine system implementing cash-based product dispensing with State Pattern, SOLID principles, and comprehensive change calculation.

## Problem Statement

Design a Vending Machine that:
- Manages product catalog and inventory
- Accepts coins and notes as payment
- Calculates and returns change
- Uses **State design pattern** for operation management
- Handles errors gracefully

---

## Quick Navigation

### 📚 Documentation
- **[Requirements](docs/01-requirements.md)** - Functional & non-functional requirements, assumptions
- **[SOLID & Design Patterns](docs/02-solid-and-design-patterns.md)** - Detailed pattern explanations
- **[Interview Q&A](docs/03-interview-qa.md)** - 15 common interview questions with answers
- **[Design Summary](docs/04-design-summary.md)** - Executive summary & checklist

### 📊 Diagrams
- **[Use Case Diagram](diagrams/usecase-diagram.md)** - Actors and use cases
- **[Step 1: Core Entities](diagrams/step1-class-diagram.md)** - Product, Inventory, Money
- **[Step 2: Managers](diagrams/step2-class-diagram.md)** - VendingMachine, ProductCatalog
- **[Step 3: State Pattern](diagrams/step3-class-diagram.md)** - State interface & concrete states
- **[Step 4: State Integration](diagrams/step4-class-diagram.md)** - Complete state flow
- **[Step 5: Payment Handling](diagrams/step5-payment-handling.md)** - Change calculation algorithm
- **[Sequence Diagrams](diagrams/sequence-diagrams.md)** - 5 interaction flows

### 💻 Code
- **[Complete Implementation](code/VendingMachineComplete.java)** - Full Java code with detailed comments

---

## Key Features

✅ **State Pattern** - Clean state management (Idle, Payment, Dispense, etc.)
✅ **Greedy Algorithm** - Optimal change calculation in O(n) time
✅ **SOLID Principles** - All 5 principles demonstrated
✅ **Thread Safety** - Synchronized inventory operations
✅ **Error Handling** - Rollback on dispense failure
✅ **Immutability** - Money as Value Object
✅ **Extensible** - Easy to add new states, payment methods

---

## Design Highlights

### State Pattern (Core Requirement)
```
Idle → ProductSelection → Payment → Dispense → ReturnChange → Idle
         ↑_________________|              |
              (cancel)                   ↓
                                    (no change) → Idle
```

### Change Calculation
- **Algorithm**: Greedy (largest denomination first)
- **Complexity**: O(n) where n = 8 denominations
- **Works**: For canonical coin systems (Indian Rupees)

### Class Structure
- **VendingMachine**: Singleton, coordinates all operations
- **ProductCatalog**: Manages products (O(1) lookup)
- **InventoryManager**: Manages stock quantities
- **CashInventory**: Manages coins/notes for change
- **ChangeCalculator**: Implements greedy algorithm
- **States**: 5 concrete states implementing VendingMachineState

---

## Design Patterns Used

| Pattern | Where | Purpose |
|---------|-------|---------|
| **Singleton** | VendingMachine | Ensure single instance |
| **State** | State management | Eliminate if-else chains |
| **Value Object** | Money | Immutability & thread-safety |
| **Facade** | VendingMachine | Simplify complex subsystem |
| **Strategy** | (Future) Payments | Interchangeable algorithms |

---

## SOLID Principles Applied

1. **Single Responsibility**: Each class has one reason to change
2. **Open/Closed**: Can add new states without modifying existing ones
3. **Liskov Substitution**: All states interchangeable
4. **Interface Segregation**: Focused interfaces
5. **Dependency Inversion**: Depend on abstractions

---

## How to Use This Repository

### For Learning
1. Start with **[Requirements](docs/01-requirements.md)**
2. Follow **Step 1-5 class diagrams** in order
3. Read **[SOLID & Patterns](docs/02-solid-and-design-patterns.md)** for deep dive
4. Study **[Sequence Diagrams](diagrams/sequence-diagrams.md)** for flows
5. Review **[Complete Code](code/VendingMachineComplete.java)**

### For Interview Preparation
1. Read **[Design Summary](docs/04-design-summary.md)** for overview
2. Practice **[Interview Q&A](docs/03-interview-qa.md)**
3. Use **Whiteboard Checklist** from summary
4. Memorize state transitions and key algorithms

### For Implementation
1. Copy **[VendingMachineComplete.java](code/VendingMachineComplete.java)**
2. Run the demo in main() method
3. Extend with new features (see extension points)

---

## File Structure

```
vending-machine/
├── README.md (this file)
├── docs/
│   ├── 01-requirements.md
│   ├── 02-solid-and-design-patterns.md
│   ├── 03-interview-qa.md
│   └── 04-design-summary.md
├── diagrams/
│   ├── usecase-diagram.md
│   ├── step1-class-diagram.md
│   ├── step2-class-diagram.md
│   ├── step3-class-diagram.md
│   ├── step4-class-diagram.md
│   ├── step5-payment-handling.md
│   └── sequence-diagrams.md
└── code/
    └── VendingMachineComplete.java
```

---

## Technologies

- **Language**: Java 8+
- **Diagrams**: Mermaid
- **Patterns**: State, Singleton, Value Object
- **Concurrency**: Synchronized methods

---

## Key Algorithms

### Change Calculation (Greedy)
```
Denominations: [500, 100, 50, 20, 10, 5, 2, 1]

for each denom in DESC order:
    use = min(needed, available)
    remaining -= (denom * use)

return remaining == 0 ? SUCCESS : FAILURE
```

**Time**: O(n) where n = 8 (constant)
**Space**: O(n) for result map

---

## Extensions Implemented

- ✅ Multiple product types (Beverage, Snack, Food, Candy)
- ✅ Transaction tracking
- ✅ Admin operations (refill inventory, refill cash)
- ✅ Low stock detection
- ✅ Change calculation validation before dispense

## Future Enhancements

- 🔜 Card/UPI payments (Strategy pattern)
- 🔜 Database persistence (JPA/Hibernate)
- 🔜 Inventory alerts (Observer pattern)
- 🔜 Multiple machines management
- 🔜 REST API
- 🔜 Mobile app integration

---

## Common Interview Questions

1. **Why State pattern?** → Eliminates if-else, follows OCP
2. **Thread safety?** → Synchronized methods for inventory
3. **Change algorithm?** → Greedy O(n), works for canonical systems
4. **Why Singleton?** → One machine, centralized state
5. **How to add card payment?** → Strategy pattern

See **[Interview Q&A](docs/03-interview-qa.md)** for detailed answers.

---

## Assumptions

- Single user at a time
- Cash payments only (initial version)
- Indian currency (₹1, ₹2, ₹5, ₹10, ₹20, ₹50, ₹100, ₹500)
- No refrigeration/temperature control
- Manual inventory refill by admin
- Synchronous operations

---

## Success Criteria

✅ User can purchase product end-to-end
✅ System calculates and returns change correctly
✅ System prevents dispensing when out of stock
✅ System validates change availability before dispensing
✅ State pattern eliminates if-else chains
✅ All SOLID principles applied
✅ Thread-safe inventory operations
✅ Graceful error handling with rollback

---

## Author

LLD Design Team - Created as educational resource for learning Low-Level Design

---

## License

Educational use - Free to learn from and adapt

---

## Feedback

For improvements or questions, please create an issue in the repository.

---

**Remember**: This is a LEARNING resource. The design prioritizes clarity and educational value over production optimization.
