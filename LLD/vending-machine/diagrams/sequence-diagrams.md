# Sequence Diagrams - Vending Machine Flows

## What are Sequence Diagrams?

**Sequence diagrams** show how objects interact over TIME. They focus on:
- **Order of operations** (top to bottom = time flows)
- **Messages** between objects (method calls)
- **Lifelines** of objects (vertical dashed lines)
- **Activations** (when object is active, shown as rectangles)

---

## Sequence Diagram 1: Successful Product Purchase (Exact Payment)

```mermaid
sequenceDiagram
    actor User
    participant VM as VendingMachine
    participant IS as IdleState
    participant PSS as ProductSelectionState
    participant PS as PaymentState
    participant DS as DispenseState
    participant Cat as ProductCatalog
    participant Inv as InventoryManager

    Note over VM: Initial State: IdleState

    User->>+VM: selectProduct("A1")
    VM->>+IS: selectProduct("A1")
    IS->>+Cat: getProductByCode("A1")
    Cat-->>-IS: Product(Coke, ₹20)
    IS->>+Inv: isProductAvailable("P001")
    Inv-->>-IS: true
    IS->>IS: Set selectedProduct
    IS->>VM: setState(ProductSelectionState)
    IS-->>-VM: void
    VM-->>-User: "Selected: Coke - ₹20"

    Note over VM: State: ProductSelectionState

    User->>+VM: insertNote(TWENTY)
    VM->>+PSS: insertNote(TWENTY)
    PSS->>VM: setState(PaymentState)
    VM->>+PS: insertNote(TWENTY)
    PS->>PS: Add ₹20 to currentPayment
    PS->>PS: Check payment >= price (₹20 >= ₹20)
    PS-->>-PSS: void
    PSS-->>-VM: void
    Note over PS: Payment sufficient!
    PS->>VM: setState(DispenseState)
    VM->>+DS: dispenseProduct()
    DS->>+Inv: deductProduct("P001")
    Inv->>Inv: quantity--
    Inv-->>-DS: true
    DS->>DS: Calculate change (₹20 - ₹20 = ₹0)
    Note over DS: No change needed
    DS->>VM: resetTransaction()
    DS->>VM: setState(IdleState)
    DS-->>-VM: void
    VM-->>-User: "Thank you! Product dispensed"

    Note over VM: State: IdleState (ready for next customer)
```

**Key Points**:
- Every user action goes through VendingMachine (facade pattern)
- VendingMachine delegates to current state
- States transition automatically when conditions met
- Inventory updated atomically during dispense

---

## Sequence Diagram 2: Product Purchase with Change Return

```mermaid
sequenceDiagram
    actor User
    participant VM as VendingMachine
    participant PS as PaymentState
    participant DS as DispenseState
    participant RCS as ReturnChangeState
    participant PP as PaymentProcessor
    participant CC as ChangeCalculator
    participant Cash as CashInventory

    Note over VM: State: PaymentState<br/>Product: Chips (₹15)<br/>Payment so far: ₹0

    User->>+VM: insertCoin(TEN)
    VM->>+PS: insertCoin(TEN)
    PS->>PS: currentPayment += ₹10
    PS->>PS: Check: ₹10 < ₹15
    PS-->>-VM: "Insert ₹5 more"
    VM-->>-User: Display message

    User->>+VM: insertNote(TWENTY)
    VM->>+PS: insertNote(TWENTY)
    PS->>PS: currentPayment += ₹20 (total ₹30)
    PS->>PS: Check: ₹30 >= ₹15
    Note over PS: Payment sufficient!
    PS->>VM: setState(DispenseState)
    PS-->>-VM: void

    VM->>+DS: dispenseProduct()
    DS->>DS: Dispense physical product
    DS->>DS: Calculate change: ₹30 - ₹15 = ₹15
    Note over DS: Change needed: ₹15
    DS->>VM: setState(ReturnChangeState)
    DS-->>-VM: void

    VM->>+RCS: returnChange()
    RCS->>+PP: returnChange(₹30, ₹15)
    PP->>+CC: calculateChange(₹15)
    CC->>+Cash: Check available coins/notes
    Cash-->>-CC: Inventory counts
    CC->>CC: Greedy algorithm:<br/>₹10(x1) + ₹5(x1)
    CC-->>-PP: ChangeResult(possible=true)
    PP->>+Cash: deductNotes(TEN, 1)
    Cash->>Cash: Update inventory
    Cash-->>-PP: true
    PP->>+Cash: deductCoins(FIVE, 1)
    Cash->>Cash: Update inventory
    Cash-->>-PP: true
    PP-->>-RCS: ChangeResult
    RCS->>RCS: Dispense change physically
    RCS->>VM: resetTransaction()
    RCS->>VM: setState(IdleState)
    RCS-->>-VM: void
    VM-->>User: "Collect change: ₹10 + ₹5"

    Note over VM: State: IdleState
```

**Key Points**:
- Change calculation happens BEFORE dispensing change
- Greedy algorithm finds optimal denomination breakdown
- Cash inventory updated atomically
- If change calculation fails, entire transaction would rollback

---

## Sequence Diagram 3: Transaction Cancellation

```mermaid
sequenceDiagram
    actor User
    participant VM as VendingMachine
    participant PS as PaymentState
    participant PP as PaymentProcessor
    participant CC as ChangeCalculator
    participant Cash as CashInventory
    participant IS as IdleState

    Note over VM: State: PaymentState<br/>Product: Water (₹10)<br/>Payment: ₹25 (inserted)

    User->>+VM: cancelTransaction()
    VM->>+PS: cancelTransaction()
    Note over PS: User changed mind

    PS->>+PP: processRefund(₹25)
    PP->>+CC: calculateChange(₹25)
    CC->>+Cash: Check inventory
    Cash-->>-CC: Available denominations
    CC->>CC: Calculate: ₹20(x1) + ₹5(x1)
    CC-->>-PP: ChangeResult(possible=true)

    PP->>+Cash: deductNotes(TWENTY, 1)
    Cash-->>-PP: true
    PP->>+Cash: deductCoins(FIVE, 1)
    Cash-->>-PP: true
    PP-->>-PS: ChangeResult

    PS->>VM: resetTransaction()
    PS->>VM: setState(IdleState)
    PS-->>-VM: void
    VM-->>-User: "Refunded: ₹25"

    Note over VM: State: IdleState<br/>Ready for next customer
```

**Key Points**:
- Cancellation uses same change calculation logic as normal change
- All inserted money refunded
- Transaction data cleared
- Machine returns to Idle state

---

## Sequence Diagram 4: Insufficient Change Scenario

```mermaid
sequenceDiagram
    actor User
    participant VM as VendingMachine
    participant PS as PaymentState
    participant PP as PaymentProcessor
    participant CC as ChangeCalculator
    participant Cash as CashInventory

    Note over VM: State: ProductSelectionState<br/>Selected: Snack (₹15)

    User->>+VM: insertNote(HUNDRED)
    VM->>+PS: insertNote(HUNDRED)
    PS->>PS: currentPayment = ₹100
    PS->>+PP: canProcessTransaction(product, ₹100)
    PP->>PP: validatePayment(): true (₹100 >= ₹15)
    PP->>PP: Calculate change needed: ₹85

    PP->>+CC: canMakeChange(₹85)
    CC->>+Cash: Check inventory
    Cash-->>-CC: Available: ₹20(x1), ₹10(x2), ₹5(x0)...
    CC->>CC: Greedy algorithm attempt:<br/>₹20 + ₹10 + ₹10 = ₹40 only
    Note over CC: Cannot make ₹85!<br/>Short by ₹45
    CC-->>-PP: false

    PP-->>-PS: Cannot process
    PS->>+PP: processRefund(₹100)
    PP->>+CC: calculateChange(₹100)
    CC->>CC: Try to refund ₹100...
    Note over CC: Assuming refund possible
    CC-->>-PP: ChangeResult
    PP->>Cash: Return ₹100 note
    PP-->>-PS: Refunded
    PS->>VM: resetTransaction()
    PS->>VM: setState(IdleState)
    PS-->>-VM: void
    VM-->>-User: "Cannot make change.<br/>Please use smaller notes.<br/>Refunded: ₹100"

    Note over VM: State: IdleState<br/>Transaction prevented
```

**Key Points**:
- Change calculation happens BEFORE dispensing product (safe!)
- If change cannot be made, transaction rejected
- User gets full refund
- Prevents inventory loss due to insufficient change

---

## Sequence Diagram 5: Admin Operations - Refill Inventory

```mermaid
sequenceDiagram
    actor Admin
    participant VM as VendingMachine
    participant Cat as ProductCatalog
    participant Inv as InventoryManager

    Note over Admin: Admin has physical access

    Admin->>+VM: refillProduct("P001", 20)
    VM->>+Inv: refillProduct("P001", 20)
    Inv->>Inv: Find inventory for P001
    Inv->>Inv: quantity += 20
    Inv-->>-VM: void
    VM-->>-Admin: "Refilled P001 with 20 units"

    Admin->>+VM: displayInventory()
    VM->>+Cat: getAllProducts()
    Cat-->>-VM: List<Product>
    loop For each product
        VM->>+Inv: getQuantity(productId)
        Inv-->>-VM: quantity
        VM->>VM: Format display
    end
    VM-->>-Admin: Display inventory table

    Note over Admin: Verify refill successful
```

---

## Component Diagram - System Architecture

```mermaid
graph TB
    subgraph "Presentation Layer"
        UI[Display/Keypad<br/>Hardware Interface]
    end

    subgraph "Application Layer - Core"
        VM[VendingMachine<br/>Singleton]
        SM[State Manager<br/>currentState]
    end

    subgraph "Business Logic Layer"
        Cat[ProductCatalog]
        Inv[InventoryManager]
        Cash[CashInventory]
        PP[PaymentProcessor]
        CC[ChangeCalculator]
        TH[TransactionHistory]
    end

    subgraph "State Pattern"
        IS[IdleState]
        PSS[ProductSelectionState]
        PS[PaymentState]
        DS[DispenseState]
        RCS[ReturnChangeState]
    end

    subgraph "Domain Entities"
        Prod[Product]
        Inv2[Inventory]
        Mon[Money]
        Txn[Transaction]
    end

    subgraph "Data Layer (Optional)"
        DB[(Database<br/>Product Catalog<br/>Inventory<br/>Transactions)]
    end

    subgraph "Hardware Layer"
        CoinAcc[Coin Acceptor]
        NoteAcc[Note Acceptor]
        Dispenser[Product Dispenser]
        CoinDisp[Coin/Note Dispenser]
    end

    UI --> VM
    VM --> SM
    VM --> Cat
    VM --> Inv
    VM --> Cash
    VM --> PP
    VM --> TH

    PP --> CC
    CC --> Cash

    SM --> IS
    SM --> PSS
    SM --> PS
    SM --> DS
    SM --> RCS

    Cat -.-> Prod
    Inv -.-> Inv2
    TH -.-> Txn

    VM -.-> DB
    Cat -.-> DB
    Inv -.-> DB

    VM --> CoinAcc
    VM --> NoteAcc
    VM --> Dispenser
    VM --> CoinDisp

    style VM fill:#FFD700,stroke:#333,stroke-width:3px
    style SM fill:#87CEEB,stroke:#333,stroke-width:2px
    style PP fill:#90EE90,stroke:#333,stroke-width:2px
    style CC fill:#90EE90,stroke:#333,stroke-width:2px
```

---

## Deployment Diagram (Physical Architecture)

```mermaid
graph TB
    subgraph "Vending Machine Hardware"
        subgraph "Embedded Computer"
            CPU[ARM Processor<br/>1GB RAM]
            OS[Embedded Linux]
            JVM[Java Runtime]
            App[VendingMachine.jar]
        end

        subgraph "Storage"
            SD[SD Card<br/>Local Database]
        end

        subgraph "I/O Devices"
            Display[LCD Display<br/>20x4 Characters]
            Keypad[Numeric Keypad]
            CoinSlot[Coin Validator]
            NoteSlot[Note Validator]
            Motor[Stepper Motors<br/>Product Dispense]
            Hopper[Coin Hopper<br/>Change Dispense]
        end

        subgraph "Sensors"
            Sensor1[Product Sensors]
            Sensor2[Coin Sensors]
            Sensor3[Temperature]
        end

        subgraph "Power"
            PSU[Power Supply<br/>12V/24V DC]
        end
    end

    subgraph "Optional Cloud Services"
        Cloud[Analytics Server<br/>Inventory Sync<br/>Remote Monitoring]
    end

    JVM --> App
    OS --> JVM
    CPU --> OS
    App --> SD
    App --> Display
    App --> Keypad
    App --> CoinSlot
    App --> NoteSlot
    App --> Motor
    App --> Hopper
    App -.->|Optional WiFi| Cloud

    style App fill:#FFD700,stroke:#333,stroke-width:3px
    style Cloud fill:#87CEEB,stroke:#333,stroke-width:2px,stroke-dasharray: 5 5
```

---

## Sequence Diagram Lessons for Beginners

### Lesson 1: Read Top to Bottom
Time flows from top to bottom. Earlier operations are higher up.

### Lesson 2: Solid vs Dashed Arrows
- **Solid arrow (→)**: Synchronous call (caller waits for response)
- **Dashed arrow (⇢)**: Return/response
- **Dashed arrow (⇢)** can also mean: Asynchronous call (fire and forget)

### Lesson 3: Activation Boxes
Rectangles on lifelines show when object is "active" (processing).

### Lesson 4: Alt/Opt/Loop Fragments
- **alt**: Alternative paths (if-else)
- **opt**: Optional (if only)
- **loop**: Iteration

### Lesson 5: Notes
Use notes to add context (state information, comments, etc.)

---

## What Sequence Diagrams Teach Us

1. **Order matters**: Payment must come before dispensing
2. **Dependencies**: Change calculation depends on cash inventory
3. **State transitions**: Clear progression through states
4. **Error handling**: Graceful failures with refunds
5. **Atomicity**: Operations grouped together (dispense + inventory update)

---

**Sequence Diagrams Complete!** ✅

These diagrams show the **dynamic behavior** of the system - how it operates over time.
