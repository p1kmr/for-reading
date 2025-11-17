# Vending Machine LLD - Design Summary & Checklist

## Executive Summary

This document provides a comprehensive Low-Level Design (LLD) for a Vending Machine system implementing **cash-based** product dispensing with change calculation.

**Core Technologies**: Java, State Pattern, SOLID Principles
**Primary Pattern**: State Pattern (requirement)
**Supporting Patterns**: Singleton, Value Object
**Algorithm**: Greedy Change Calculation (O(n))

---

## Design Decisions Summary

### 1. Architecture Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **State Management** | State Pattern | Eliminates if-else, easy to extend, clear transitions |
| **VendingMachine Instance** | Singleton | Only one machine exists physically |
| **Managers** | Separate classes | Single Responsibility Principle |
| **Money Representation** | Immutable Value Object | Thread-safe, predictable, value semantics |
| **Collections** | HashMap (O(1)) | Fast product/inventory lookup |
| **Thread Safety** | Synchronized methods | Simpler than locks, sufficient for use case |
| **Change Algorithm** | Greedy | Optimal for Indian currency (canonical system) |

---

### 2. State Pattern Implementation

**States Implemented**:
1. **IdleState**: Default, waiting for user
2. **ProductSelectionState**: Product selected, awaiting payment
3. **PaymentState**: Accumulating payment
4. **DispenseState**: Dispensing product
5. **ReturnChangeState**: Returning change

**State Transitions**:
```
Idle → ProductSelection → Payment → Dispense → ReturnChange → Idle
                ↑______________|             |
                     (cancel)               ↓
                                        (no change)
                                           Idle
```

**Benefits**:
- ✅ No if-else chains
- ✅ Each state = separate class (SRP)
- ✅ Easy to add states (OCP)
- ✅ Clear, explicit transitions

---

### 3. Class Responsibilities

| Class | Single Responsibility |
|-------|----------------------|
| `VendingMachine` | Coordinates all operations (Facade) |
| `ProductCatalog` | Manages product collection |
| `InventoryManager` | Manages stock quantities |
| `CashInventory` | Manages coins/notes for change |
| `ChangeCalculator` | Calculates change breakdown |
| `PaymentProcessor` | Processes payments |
| `Product` | Represents sellable item |
| `Inventory` | Tracks quantity of one product |
| `Money` | Immutable monetary value |
| `Transaction` | Records purchase history |

---

### 4. Relationships Used

| Relationship | Example | UML Symbol |
|--------------|---------|------------|
| **Composition** | VendingMachine owns ProductCatalog | ◆ (filled diamond) |
| **Aggregation** | ProductCatalog manages Products | ◇ (empty diamond) |
| **Association** | VendingMachine uses Product | → (arrow) |
| **Inheritance** | IdleState implements VendingMachineState | ◁ (triangle) |
| **Dependency** | ChangeCalculator uses CashInventory | ⇢ (dashed arrow) |

---

### 5. SOLID Principles Applied

**Single Responsibility**:
- Each manager handles ONE concern
- ProductCatalog ≠ InventoryManager ≠ CashInventory

**Open/Closed**:
- Can add new states without modifying existing ones
- Can add new payment methods (future) via Strategy

**Liskov Substitution**:
- All states interchangeable via VendingMachineState interface
- Any state can replace another without breaking system

**Interface Segregation**:
- State interface focused on state operations only
- Admin operations separate from user operations

**Dependency Inversion**:
- VendingMachine depends on VendingMachineState (abstraction)
- Not on concrete state classes

---

### 6. Algorithms & Complexity

**Change Calculation (Greedy Algorithm)**:
```
Denominations: [500, 100, 50, 20, 10, 5, 2, 1]  // DESC order

for each denomination:
    use = min(needed, available)
    add to result
    remaining -= (denomination * use)

if remaining == 0: SUCCESS
else: FAILURE
```

**Time Complexity**: O(n) where n = 8 (constant denominations)
**Space Complexity**: O(n) for result map
**Optimality**: Guaranteed optimal for Indian currency

---

### 7. Thread Safety Mechanisms

| Component | Mechanism | Purpose |
|-----------|-----------|---------|
| `Inventory.deductQuantity()` | synchronized | Atomic check-and-deduct |
| `CashInventory.deductCoins()` | synchronized | Prevent negative inventory |
| `InventoryManager.deductProduct()` | synchronized | Coordinate inventory updates |
| `VendingMachine.getInstance()` | synchronized | Thread-safe singleton creation |

---

### 8. Error Handling Strategies

| Error Scenario | Handling |
|----------------|----------|
| **Invalid product code** | Display error, stay in Idle |
| **Product out of stock** | Display error, stay in Idle |
| **Insufficient payment** | Display remaining amount |
| **Cannot make change** | Reject transaction, refund |
| **Dispense failure** | Rollback inventory, refund |
| **Timeout** | Cancel transaction, refund |

**Rollback Mechanism**:
```java
try {
    deductInventory();
    dispenseProduct();
    // Success
} catch (DispenseException e) {
    rollbackInventory();  // Add back
    refund();
    resetState();
}
```

---

### 9. Extension Points (Future)

**Easy to Add** (Open/Closed Principle):

1. **New States**:
   ```java
   class MaintenanceState implements VendingMachineState { }
   // No modification to existing states!
   ```

2. **New Payment Methods** (Strategy Pattern):
   ```java
   class CardPaymentStrategy implements PaymentStrategy { }
   class UPIPaymentStrategy implements PaymentStrategy { }
   ```

3. **New Product Types**:
   ```java
   class RefrigeratedProduct extends Product {
       private double temperature;
   }
   ```

4. **Promotions** (Decorator Pattern):
   ```java
   class PercentageDiscount extends DiscountDecorator { }
   class BuyNGetMFree extends DiscountDecorator { }
   ```

---

### 10. Testing Strategy

**Unit Tests**:
- ChangeCalculator: Various amounts, edge cases
- Inventory: Concurrent deductions
- Money: Immutability, operations
- States: Valid/invalid actions

**Integration Tests**:
- Complete purchase flow
- Cancellation flow
- Change return flow
- Error scenarios

**State Transition Tests**:
- Verify correct state after each action
- Invalid transitions rejected

---

## Whiteboard Checklist: "What to Draw First"

When designing vending machine LLD in an interview or on whiteboard:

### Phase 1: Requirements (2 minutes)
- [ ] List functional requirements
- [ ] List non-functional requirements
- [ ] Identify actors (User, Admin, System)

### Phase 2: Core Entities (3 minutes)
- [ ] Draw Product class
- [ ] Draw Inventory class
- [ ] Draw Money class (value object)
- [ ] Draw Coin and Note enums

### Phase 3: Managers (3 minutes)
- [ ] Draw ProductCatalog
- [ ] Draw InventoryManager
- [ ] Draw CashInventory
- [ ] Show VendingMachine owns these (composition)

### Phase 4: State Pattern (5 minutes)
- [ ] Draw VendingMachineState interface
- [ ] Draw 3-5 concrete states (Idle, Payment, Dispense)
- [ ] Show state transition diagram
- [ ] Explain why State pattern over if-else

### Phase 5: Change Calculation (3 minutes)
- [ ] Draw ChangeCalculator class
- [ ] Write greedy algorithm pseudocode
- [ ] Mention time complexity O(n)
- [ ] Explain why greedy works for Indian currency

### Phase 6: Sequence Diagram (4 minutes)
- [ ] Draw sequence for successful purchase
- [ ] Show state transitions
- [ ] Highlight change calculation step
- [ ] Show error handling (if time permits)

**Total Time**: ~20 minutes for comprehensive design

---

## Common Beginner Mistakes & Solutions

### Mistake 1: God Class
**Problem**:
```java
class VendingMachine {
    private List<Product> products;  // Product management
    private Map<String, Integer> inventory;  // Inventory management
    private Map<Coin, Integer> coins;  // Cash management
    // 500 lines of code...
}
```

**Solution**: Separate into ProductCatalog, InventoryManager, CashInventory

---

### Mistake 2: Using Primitives for Domain Concepts
**Problem**:
```java
double money = 20.0;  // Just a number
String productCode = "A1";  // Just a string
```

**Solution**: Create domain classes
```java
Money money = new Money(20.0);  // Value object
Product product = catalog.getByCode("A1");  // Rich object
```

---

### Mistake 3: if-else State Management
**Problem**:
```java
if (state.equals("IDLE")) {
    // ...
} else if (state.equals("PAYMENT")) {
    // ...
} // 10+ states = 100+ lines
```

**Solution**: State Pattern with separate classes

---

### Mistake 4: Mutable Value Objects
**Problem**:
```java
class Money {
    private double amount;
    public void setAmount(double amt) { this.amount = amt; }
    // NOT thread-safe, unpredictable
}
```

**Solution**: Immutable Money class

---

### Mistake 5: No Change Validation Before Dispense
**Problem**:
```java
dispenseProduct();  // Dispense first
calculateChange();  // Oops! Can't make change!
// Customer got product but machine can't return change
```

**Solution**: Check change BEFORE dispensing
```java
if (canMakeChange(changeAmount)) {
    dispenseProduct();
    returnChange();
} else {
    rejectTransaction();
}
```

---

### Mistake 6: Not Handling Concurrent Access
**Problem**:
```java
if (inventory > 0) {
    inventory--;  // NOT atomic!
    // Two threads might pass check simultaneously
}
```

**Solution**: Synchronized or atomic operations
```java
public synchronized boolean deduct() {
    if (inventory > 0) {
        inventory--;
        return true;
    }
    return false;
}
```

---

## Key Interview Talking Points

When presenting this design, emphasize:

1. **State Pattern is Core**: "The requirement explicitly asks for State pattern. Here's why it's perfect for this use case..."

2. **SOLID Throughout**: "Notice how each class has a single responsibility. ProductCatalog manages products, InventoryManager manages quantities..."

3. **Greedy Algorithm**: "For change calculation, I used greedy algorithm because Indian currency is a canonical coin system. Time complexity is O(n) where n is constant (8 denominations)..."

4. **Thread Safety**: "I used synchronized methods for inventory to prevent race conditions. For production, could use ReentrantLock or AtomicInteger for better performance..."

5. **Extensibility**: "The design follows Open/Closed. To add card payments, I'd use Strategy pattern without modifying existing code..."

6. **Error Handling**: "If dispense fails, I rollback the inventory and refund the customer. Transaction atomicity is critical..."

7. **Immutability**: "Money is immutable for thread-safety and predictability. All operations return new Money objects..."

---

## Design Pattern Summary

| Pattern | Where Used | Benefit |
|---------|------------|---------|
| **Singleton** | VendingMachine | Single instance |
| **State** | State management | Clean state behavior |
| **Value Object** | Money | Immutability |
| **Facade** | VendingMachine | Simplifies subsystem |
| **Strategy** | (Future) Payment methods | Interchangeable algorithms |
| **Observer** | (Future) Inventory alerts | Decoupled notifications |
| **Factory** | (Future) Product creation | Centralized object creation |

---

## Files Delivered

### Documentation
- ✅ `docs/01-requirements.md` - Comprehensive requirements
- ✅ `docs/02-solid-and-design-patterns.md` - SOLID principles & patterns
- ✅ `docs/03-interview-qa.md` - 15 interview questions
- ✅ `docs/04-design-summary.md` - This summary

### Diagrams
- ✅ `diagrams/usecase-diagram.md` - Use cases and flows
- ✅ `diagrams/step1-class-diagram.md` - Basic entities
- ✅ `diagrams/step2-class-diagram.md` - Managers & VendingMachine
- ✅ `diagrams/step3-class-diagram.md` - State pattern
- ✅ `diagrams/step4-class-diagram.md` - State integration
- ✅ `diagrams/step5-payment-handling.md` - Payment & change calculation
- ✅ `diagrams/sequence-diagrams.md` - 5 sequence diagrams

### Code
- ✅ `code/VendingMachineComplete.java` - Full implementation with comments

---

## Assumptions Made

1. **Single User**: Machine handles one transaction at a time
2. **Cash Only**: No digital payments (cards, UPI) in initial design
3. **Indian Currency**: ₹1, ₹2, ₹5, ₹10, ₹20, ₹50, ₹100, ₹500
4. **Synchronous Operations**: No async/concurrent transactions
5. **No Refrigeration**: Temperature control out of scope
6. **No Expiry Tracking**: Product expiry not managed
7. **Manual Refill**: Admin physically refills inventory
8. **Persistent Storage**: Optional (can add database layer)
9. **Fixed Slot Size**: All products fit standard slots
10. **Hardware Abstraction**: Assume hardware controllers available

---

## Trade-offs Made

| Decision | Chosen | Alternative | Trade-off |
|----------|--------|-------------|-----------|
| Singleton vs DI | Singleton | Dependency Injection | Simpler but less testable |
| Synchronized vs Locks | Synchronized | ReentrantLock | Simpler but less performant |
| Greedy vs DP | Greedy | Dynamic Programming | O(n) vs O(n*m) but works for canonical system |
| Synchronous vs Async | Synchronous | Async with queues | Simpler but no concurrency |
| In-Memory vs Database | In-Memory | JPA/Hibernate | Faster but no persistence |

---

## Extensibility Roadmap

**Phase 1 (Current)**:
- ✅ Cash payments
- ✅ Basic inventory
- ✅ Change calculation
- ✅ State management

**Phase 2 (Easy Extensions)**:
- 🔄 Card/UPI payments (Strategy pattern)
- 🔄 Transaction history (already designed)
- 🔄 Admin dashboard
- 🔄 Low stock alerts (Observer pattern)

**Phase 3 (Future)**:
- 🔜 Multiple machines management
- 🔜 Real-time inventory sync
- 🔜 Dynamic pricing
- 🔜 IoT integration
- 🔜 Mobile app
- 🔜 Analytics & reporting

---

## Success Criteria

The design is successful if:

✅ User can purchase product end-to-end
✅ System correctly handles insufficient payment
✅ System correctly calculates and returns change
✅ System prevents dispensing when out of stock
✅ System prevents dispensing when can't make change
✅ State pattern implemented correctly
✅ Inventory accurately maintained
✅ System handles cancellation and refunds
✅ Code follows SOLID principles
✅ All components unit testable

---

## Final Checklist for Interview

Before submitting design:

- [ ] All requirements addressed
- [ ] State pattern clearly shown
- [ ] SOLID principles applied
- [ ] Thread safety considered
- [ ] Error handling discussed
- [ ] Complexity analyzed
- [ ] Trade-offs explained
- [ ] Extension points identified
- [ ] UML diagrams clear
- [ ] Code skeleton provided
- [ ] Edge cases handled
- [ ] Assumptions listed

---

## Conclusion

This Vending Machine LLD demonstrates:

- **Strong OOP**: SOLID principles, clean architecture
- **Design Patterns**: State, Singleton, Value Object
- **Algorithm Design**: Greedy change calculation
- **System Thinking**: Error handling, concurrency, extensibility
- **Interview Skills**: Clear communication, incremental design

**Remember**: In interviews, the PROCESS matters more than the final solution. Show your thinking, discuss trade-offs, and ask clarifying questions!

---

**Document Version**: 1.0
**Last Updated**: 2025-11-17
**Status**: Complete ✅
