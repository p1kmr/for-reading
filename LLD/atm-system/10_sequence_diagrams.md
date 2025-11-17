# Sequence Diagrams - ATM System Flows (Phase 8)

> **Purpose**: Show how objects interact over time for key operations

---

## 1. Authentication Flow

```mermaid
sequenceDiagram
    actor Customer
    participant ATMService
    participant CardReader
    participant Screen
    participant Keypad
    participant AuthService
    participant BankingService
    participant Card

    Customer->>ATMService: Insert Card
    ATMService->>CardReader: readCard()
    CardReader-->>ATMService: Card object
    ATMService->>Screen: displayMessage("Enter PIN")

    Customer->>Keypad: Enter PIN
    ATMService->>Keypad: readPin()
    Keypad-->>ATMService: "1234"

    ATMService->>AuthService: authenticateUser(card, "1234")
    AuthService->>Card: isValid()
    Card-->>AuthService: true

    AuthService->>BankingService: validatePinWithBank(cardNumber, "1234")
    BankingService-->>AuthService: true

    AuthService->>Card: resetPinAttempts()
    AuthService-->>ATMService: true

    ATMService->>Screen: displayMessage("Authentication Successful")
    ATMService->>Screen: displayMenu()
```

**Explanation**:
1. Customer inserts card → CardReader reads it
2. ATM prompts for PIN → Keypad captures it
3. AuthService validates card locally
4. AuthService validates PIN with backend
5. On success, reset PIN attempts
6. Show main menu

---

## 2. Cash Withdrawal Flow

```mermaid
sequenceDiagram
    actor Customer
    participant ATMService
    participant Screen
    participant Keypad
    participant TransactionService
    participant Account
    participant ATM
    participant CashDispenser
    participant BankingService
    participant TransactionRepo

    Customer->>ATMService: Select "Withdraw"
    ATMService->>Screen: displayMessage("Enter Amount")

    Customer->>Keypad: Enter 2000
    ATMService->>Keypad: readAmount()
    Keypad-->>ATMService: 2000

    ATMService->>TransactionService: withdraw(card, account, 2000, atm)

    %% Validation
    TransactionService->>Account: hasSufficientBalance(2000)
    Account-->>TransactionService: true

    TransactionService->>ATM: hasSufficientCash(2000)
    ATM-->>TransactionService: true

    %% Execution
    TransactionService->>Account: debit(2000)
    Account-->>TransactionService: success

    TransactionService->>ATM: dispenseCash(2000)
    ATM-->>TransactionService: {500: 4}

    %% Backend update
    TransactionService->>BankingService: debitAccount("ACC123", 2000)
    BankingService-->>TransactionService: true

    %% Create transaction record
    TransactionService->>TransactionRepo: save(transaction)
    TransactionService-->>ATMService: Transaction(SUCCESS)

    %% Physical cash dispensing
    ATMService->>CashDispenser: dispenseCash({500: 4})
    CashDispenser-->>ATMService: true

    ATMService->>Screen: displayMessage("Please collect cash")
```

**Key Steps**:
1. Validate: Check balance and ATM cash
2. Debit: Remove from account
3. Dispense Plan: Calculate denominations
4. Backend: Update bank backend
5. Persist: Save transaction
6. Hardware: Physically dispense cash

---

## 3. Cash Deposit Flow

```mermaid
sequenceDiagram
    actor Customer
    participant ATMService
    participant Screen
    participant TransactionService
    participant ATM
    participant Account
    participant BankingService
    participant TransactionRepo

    Customer->>ATMService: Select "Deposit"
    ATMService->>Screen: displayMessage("Insert Cash")

    Customer->>ATM: Insert ₹3000 (6×₹500)
    ATM->>ATM: countCash()
    ATM-->>ATMService: {500: 6} (₹3000)

    ATMService->>Screen: displayMessage("Counted: ₹3000. Confirm?")
    Customer->>ATMService: Confirm

    ATMService->>TransactionService: deposit(card, account, 3000, atm, {500:6})

    TransactionService->>ATM: acceptCash({500: 6})
    ATM-->>TransactionService: true

    TransactionService->>Account: credit(3000)
    Account-->>TransactionService: success

    TransactionService->>BankingService: creditAccount("ACC123", 3000)
    BankingService-->>TransactionService: true

    TransactionService->>TransactionRepo: save(transaction)
    TransactionService-->>ATMService: Transaction(SUCCESS)

    ATMService->>Screen: displayMessage("Deposit Successful")
```

---

## 4. Balance Inquiry Flow

```mermaid
sequenceDiagram
    actor Customer
    participant ATMService
    participant TransactionService
    participant BankingService
    participant Screen
    participant ReceiptPrinter

    Customer->>ATMService: Select "Balance Inquiry"

    ATMService->>TransactionService: balanceInquiry(card, account, atm)

    TransactionService->>BankingService: getAccountBalance("ACC123")
    BankingService-->>TransactionService: 15000.0

    TransactionService-->>ATMService: Transaction(balance=15000)

    ATMService->>Screen: displayMessage("Balance: ₹15,000")
    ATMService->>Screen: displayMessage("Print receipt? (Y/N)")

    Customer->>ATMService: Yes
    ATMService->>ReceiptPrinter: printReceipt(transaction)
```

---

## 5. Failed Transaction (Insufficient Balance)

```mermaid
sequenceDiagram
    actor Customer
    participant ATMService
    participant TransactionService
    participant Account
    participant Screen
    participant TransactionRepo

    Customer->>ATMService: Withdraw ₹10,000
    ATMService->>TransactionService: withdraw(card, account, 10000, atm)

    TransactionService->>Account: hasSufficientBalance(10000)
    Account-->>TransactionService: false (balance = 5000)

    TransactionService->>TransactionService: createFailedTransaction("Insufficient balance")
    TransactionService->>TransactionRepo: save(failedTransaction)
    TransactionService-->>ATMService: Transaction(FAILED)

    ATMService->>Screen: showErrorMessage("Insufficient balance")
    ATMService->>Screen: displayMenu()
```

---

## 6. PIN Validation Failure (3 Attempts)

```mermaid
sequenceDiagram
    actor Customer
    participant ATMService
    participant AuthService
    participant Card
    participant BankingService
    participant CardReader
    participant Screen

    loop 3 attempts
        Customer->>ATMService: Enter PIN
        ATMService->>AuthService: authenticateUser(card, wrongPin)
        AuthService->>BankingService: validatePinWithBank(card, wrongPin)
        BankingService-->>AuthService: false

        AuthService->>Card: incrementPinAttempt()
        Card->>Card: attempts = 1, 2, 3

        alt attempts < 3
            AuthService-->>ATMService: false
            ATMService->>Screen: showErrorMessage("Wrong PIN. Try again")
        else attempts == 3
            Card->>Card: blockCard()
            AuthService->>BankingService: blockCard(cardNumber)
            AuthService-->>ATMService: false
            ATMService->>Screen: showErrorMessage("Card blocked")
            ATMService->>CardReader: retainCard()
        end
    end
```

---

## 7. Component Interaction Diagram

```mermaid
graph TB
    subgraph "Presentation Layer"
        ATMService
    end

    subgraph "Service Layer"
        AuthService[AuthenticationService]
        TxnService[TransactionService]
    end

    subgraph "Domain Layer"
        Card
        Account
        Transaction
        ATM
    end

    subgraph "Infrastructure Layer"
        CardReader
        CashDispenser
        ReceiptPrinter
        BankingService
        TransactionRepo
    end

    ATMService --> AuthService
    ATMService --> TxnService
    ATMService --> CardReader
    ATMService --> CashDispenser
    ATMService --> ReceiptPrinter

    AuthService --> BankingService
    AuthService --> Card

    TxnService --> Account
    TxnService --> ATM
    TxnService --> Transaction
    TxnService --> BankingService
    TxnService --> TransactionRepo
```

---

## Interview Questions

**Q: What happens if cash dispensing fails after account debit?**
**A**: Rollback - credit the amount back to account and mark transaction as REVERSED

**Q: How to ensure atomicity across services?**
**A**: Use database transactions (`@Transactional`) and compensating transactions for rollback

**Q: What if banking backend is down during withdrawal?**
**A**:
1. Retry with exponential backoff
2. If still fails, rollback local changes
3. Mark transaction as FAILED
4. Don't dispense cash

---

## Navigation
- [← Previous: Design Patterns](09_design_patterns.md)
- [→ Next: SOLID & Concurrency](11_solid_concurrency.md)
