/**
 * ═══════════════════════════════════════════════════════════════════
 * ATM SYSTEM - COMPLETE JAVA IMPLEMENTATION
 * ═══════════════════════════════════════════════════════════════════
 *
 * This file contains all classes for the ATM system in one place.
 * In a real project, each class would be in its own file.
 *
 * Architecture:
 * - Entities (Domain Model): Card, Account, Transaction, ATM, Customer
 * - Services (Business Logic): AuthenticationService, TransactionService
 * - Repositories (Data Access): AccountRepository, TransactionRepository
 * - Hardware (Interfaces): CardReader, Screen, Keypad, CashDispenser
 * - Main Orchestrator: ATMService (Facade pattern)
 *
 * SOLID Principles Applied:
 * S - Single Responsibility: Each class has one reason to change
 * O - Open/Closed: Easy to extend (Strategy pattern for transactions)
 * L - Liskov Substitution: Interfaces can be swapped
 * I - Interface Segregation: Small, focused interfaces
 * D - Dependency Inversion: Depend on abstractions, not concretions
 *
 * ═══════════════════════════════════════════════════════════════════
 */

import java.util.*;
import java.time.*;

// ═══════════════════════════════════════════════════════════════════
// DOMAIN ENTITIES
// ═══════════════════════════════════════════════════════════════════

/**
 * Card Entity - Represents an ATM card
 *
 * Responsibilities:
 * - Store card details
 * - Track PIN attempts
 * - Validate card status
 *
 * Design: Simple POJO with validation logic
 */
class Card {
    // Immutable fields (set once, never change)
    private final String cardNumber;          // 16-digit card number
    private final String cardHolderName;      // Name on card
    private final LocalDate expiryDate;       // MM/YY expiry

    // Mutable fields
    private CardStatus status;                 // ACTIVE, BLOCKED, EXPIRED
    private int pinAttempts;                   // Failed PIN attempts counter
    private static final int MAX_PIN_ATTEMPTS = 3;

    public Card(String cardNumber, String cardHolderName, LocalDate expiryDate) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.status = CardStatus.ACTIVE;
        this.pinAttempts = 0;
    }

    /**
     * Check if card is valid (active + not expired)
     */
    public boolean isValid() {
        return status == CardStatus.ACTIVE && !isExpired();
    }

    /**
     * Check if card has expired
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    /**
     * Increment failed PIN attempts
     * Blocks card after MAX_PIN_ATTEMPTS
     */
    public void incrementPinAttempt() {
        pinAttempts++;
        if (pinAttempts >= MAX_PIN_ATTEMPTS) {
            blockCard();
        }
    }

    /**
     * Reset PIN attempts (after successful login)
     */
    public void resetPinAttempts() {
        this.pinAttempts = 0;
    }

    /**
     * Block the card
     */
    public void blockCard() {
        this.status = CardStatus.BLOCKED;
    }

    // Getters
    public String getCardNumber() { return cardNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public CardStatus getStatus() { return status; }
    public int getPinAttempts() { return pinAttempts; }
}

/**
 * Card Status Enum
 */
enum CardStatus {
    ACTIVE,     // Card is active and can be used
    BLOCKED,    // Card is blocked (wrong PIN attempts or security)
    EXPIRED,    // Card has expired
    STOLEN,     // Card reported as stolen
    LOST        // Card reported as lost
}

/**
 * Account Entity - Represents a bank account
 *
 * Responsibilities:
 * - Maintain balance
 * - Handle debit/credit operations
 * - Enforce withdrawal limits
 *
 * Design: Encapsulated balance with validation
 */
class Account {
    private final String accountNumber;
    private final String accountHolderName;
    private double balance;                    // Current balance
    private AccountType accountType;           // SAVINGS, CURRENT, etc.
    private AccountStatus status;
    private double dailyWithdrawalLimit;       // Max withdrawal per day
    private double dailyWithdrawnToday;        // Amount withdrawn today
    private LocalDate lastWithdrawalDate;      // Track date for daily limit reset

    public Account(String accountNumber, String accountHolderName,
                   double initialBalance, AccountType accountType) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = initialBalance;
        this.accountType = accountType;
        this.status = AccountStatus.ACTIVE;
        this.dailyWithdrawalLimit = 50000.0;  // Default ₹50,000
        this.dailyWithdrawnToday = 0.0;
        this.lastWithdrawalDate = LocalDate.now();
    }

    /**
     * Debit (withdraw) money from account
     *
     * @throws InsufficientBalanceException if balance < amount
     */
    public void debit(double amount) {
        if (!hasSufficientBalance(amount)) {
            throw new InsufficientBalanceException("Insufficient balance. Available: " + balance);
        }
        balance -= amount;
        updateDailyWithdrawal(amount);
    }

    /**
     * Credit (deposit) money to account
     */
    public void credit(double amount) {
        balance += amount;
    }

    /**
     * Check if account has sufficient balance
     */
    public boolean hasSufficientBalance(double amount) {
        return balance >= amount;
    }

    /**
     * Check if withdrawal is within daily limit
     */
    public boolean isWithinDailyLimit(double amount) {
        resetDailyLimitIfNewDay();
        return (dailyWithdrawnToday + amount) <= dailyWithdrawalLimit;
    }

    /**
     * Update daily withdrawal amount
     */
    private void updateDailyWithdrawal(double amount) {
        resetDailyLimitIfNewDay();
        dailyWithdrawnToday += amount;
    }

    /**
     * Reset daily limit if new day
     */
    private void resetDailyLimitIfNewDay() {
        LocalDate today = LocalDate.now();
        if (lastWithdrawalDate == null || !lastWithdrawalDate.equals(today)) {
            dailyWithdrawnToday = 0.0;
            lastWithdrawalDate = today;
        }
    }

    // Getters
    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolderName() { return accountHolderName; }
    public double getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
}

enum AccountType {
    SAVINGS,    // Savings account
    CURRENT,    // Current account
    SALARY      // Salary account
}

enum AccountStatus {
    ACTIVE,     // Account is active
    FROZEN,     // Account is frozen (no transactions)
    CLOSED,     // Account is closed
    DORMANT     // Account is dormant (inactive)
}

/**
 * Transaction Entity - Records every ATM operation
 *
 * Responsibilities:
 * - Record transaction details
 * - Track status (pending, success, failed)
 * - Immutable after creation (audit compliance)
 *
 * Design: Immutable core fields, status can change
 */
class Transaction {
    // Immutable fields (final)
    private final String transactionId;
    private final String accountNumber;
    private final TransactionType type;
    private final double amount;
    private final LocalDateTime timestamp;
    private final String atmId;

    // Mutable fields
    private TransactionStatus status;
    private double balanceAfter;
    private String description;
    private String failureReason;

    public Transaction(String accountNumber, TransactionType type,
                       double amount, String atmId) {
        this.transactionId = UUID.randomUUID().toString();
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.atmId = atmId;
        this.timestamp = LocalDateTime.now();
        this.status = TransactionStatus.PENDING;
        this.description = generateDescription();
    }

    /**
     * Mark transaction as successful
     */
    public void markAsSuccess(double balanceAfter) {
        this.status = TransactionStatus.SUCCESS;
        this.balanceAfter = balanceAfter;
    }

    /**
     * Mark transaction as failed with reason
     */
    public void markAsFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
    }

    /**
     * Generate human-readable description
     */
    private String generateDescription() {
        switch (type) {
            case WITHDRAWAL:
                return "Cash withdrawal of ₹" + amount;
            case DEPOSIT:
                return "Cash deposit of ₹" + amount;
            case BALANCE_INQUIRY:
                return "Balance inquiry";
            default:
                return "ATM transaction";
        }
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public TransactionStatus getStatus() { return status; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getFailureReason() { return failureReason; }
    public String getDescription() { return description; }
}

enum TransactionType {
    WITHDRAWAL,         // Cash withdrawal
    DEPOSIT,            // Cash deposit
    BALANCE_INQUIRY,    // Check balance
    PIN_CHANGE          // Change PIN
}

enum TransactionStatus {
    PENDING,    // Transaction initiated
    SUCCESS,    // Transaction successful
    FAILED,     // Transaction failed
    REVERSED    // Transaction reversed (rollback)
}

/**
 * ATM Entity - Represents physical ATM machine
 *
 * Responsibilities:
 * - Manage cash inventory (multiple denominations)
 * - Dispense cash
 * - Accept cash deposits
 * - Track ATM status
 */
class ATM {
    private final String atmId;
    private ATMStatus status;
    private Map<Integer, CashBin> cashBins;  // denomination → CashBin
    private double totalCashAvailable;

    public ATM(String atmId) {
        this.atmId = atmId;
        this.status = ATMStatus.ACTIVE;
        this.cashBins = new HashMap<>();
        initializeCashBins();
    }

    /**
     * Initialize cash bins for different denominations
     */
    private void initializeCashBins() {
        cashBins.put(100, new CashBin(100, 2000));    // ₹100 bin
        cashBins.put(500, new CashBin(500, 1000));    // ₹500 bin
        cashBins.put(2000, new CashBin(2000, 500));   // ₹2000 bin
        updateTotalCash();
    }

    /**
     * Check if ATM has sufficient cash
     */
    public boolean hasSufficientCash(double amount) {
        return totalCashAvailable >= amount && canDispenseAmount(amount);
    }

    /**
     * Check if amount can be dispensed using available denominations
     */
    private boolean canDispenseAmount(double amount) {
        return calculateDispensePlan(amount) != null;
    }

    /**
     * Calculate cash dispense plan (greedy algorithm)
     * Returns map of denomination → count
     */
    private Map<Integer, Integer> calculateDispensePlan(double amount) {
        Map<Integer, Integer> plan = new HashMap<>();
        double remaining = amount;

        // Try denominations in descending order (2000, 500, 100)
        int[] denoms = {2000, 500, 100};
        for (int denom : denoms) {
            CashBin bin = cashBins.get(denom);
            int notesNeeded = (int) (remaining / denom);
            int notesAvailable = bin.getNoteCount();

            if (notesNeeded > 0) {
                int notesToDispense = Math.min(notesNeeded, notesAvailable);
                plan.put(denom, notesToDispense);
                remaining -= (notesToDispense * denom);
            }
        }

        return (remaining == 0) ? plan : null;
    }

    /**
     * Dispense cash physically
     */
    public Map<Integer, Integer> dispenseCash(double amount) {
        Map<Integer, Integer> plan = calculateDispensePlan(amount);
        if (plan == null) {
            return null;
        }

        // Deduct notes from bins
        for (Map.Entry<Integer, Integer> entry : plan.entrySet()) {
            int denom = entry.getKey();
            int count = entry.getValue();
            cashBins.get(denom).removeNotes(count);
        }

        updateTotalCash();
        return plan;
    }

    /**
     * Update total cash available
     */
    private void updateTotalCash() {
        totalCashAvailable = 0;
        for (CashBin bin : cashBins.values()) {
            totalCashAvailable += bin.getTotalValue();
        }
    }

    public String getAtmId() { return atmId; }
    public ATMStatus getStatus() { return status; }
    public double getTotalCashAvailable() { return totalCashAvailable; }
}

/**
 * CashBin - Stores cash of specific denomination
 */
class CashBin {
    private final int denomination;
    private int noteCount;
    private final int capacity;

    public CashBin(int denomination, int capacity) {
        this.denomination = denomination;
        this.capacity = capacity;
        this.noteCount = 0;
    }

    public void addNotes(int count) {
        this.noteCount += count;
    }

    public boolean removeNotes(int count) {
        if (count > noteCount) {
            return false;
        }
        noteCount -= count;
        return true;
    }

    public int getNoteCount() { return noteCount; }
    public double getTotalValue() { return denomination * noteCount; }
}

enum ATMStatus {
    ACTIVE,             // ATM is operational
    INACTIVE,           // ATM is offline
    OUT_OF_SERVICE,     // ATM has technical issues
    NO_CASH,            // ATM has no cash
    MAINTENANCE         // ATM under maintenance
}

/**
 * Customer Entity
 */
class Customer {
    private final String customerId;
    private final String name;
    private final String email;
    private final String phoneNumber;

    public Customer(String customerId, String name, String email, String phoneNumber) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
}

// ═══════════════════════════════════════════════════════════════════
// SERVICES (Business Logic)
// ═══════════════════════════════════════════════════════════════════

/**
 * BankingService Interface - Abstract backend communication
 *
 * Why Interface?
 * - Dependency Inversion Principle
 * - Easy to mock for testing
 * - Can swap implementations (different banks)
 */
interface BankingService {
    boolean validateCardWithBank(String cardNumber);
    boolean validatePinWithBank(String cardNumber, String pin);
    double getAccountBalance(String accountNumber);
    boolean debitAccount(String accountNumber, double amount);
    boolean creditAccount(String accountNumber, double amount);
    void blockCard(String cardNumber);
}

/**
 * Mock Banking Service (for testing)
 */
class MockBankingService implements BankingService {
    private static final String VALID_PIN = "1234";

    @Override
    public boolean validateCardWithBank(String cardNumber) {
        // Simulate API call
        return true;
    }

    @Override
    public boolean validatePinWithBank(String cardNumber, String pin) {
        return VALID_PIN.equals(pin);
    }

    @Override
    public double getAccountBalance(String accountNumber) {
        return 50000.0;  // Mock balance
    }

    @Override
    public boolean debitAccount(String accountNumber, double amount) {
        System.out.println("MOCK: Debited ₹" + amount + " from account " + accountNumber);
        return true;
    }

    @Override
    public boolean creditAccount(String accountNumber, double amount) {
        System.out.println("MOCK: Credited ₹" + amount + " to account " + accountNumber);
        return true;
    }

    @Override
    public void blockCard(String cardNumber) {
        System.out.println("MOCK: Blocked card " + cardNumber);
    }
}

/**
 * AuthenticationService - Handles user authentication
 *
 * Responsibilities:
 * - Validate card
 * - Validate PIN
 * - Block card after failed attempts
 */
class AuthenticationService {
    private final BankingService bankingService;

    public AuthenticationService(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    /**
     * Authenticate user with card and PIN
     */
    public boolean authenticateUser(Card card, String pin) {
        // Step 1: Validate card
        if (!validateCard(card)) {
            return false;
        }

        // Step 2: Validate PIN
        if (!validatePin(card, pin)) {
            card.incrementPinAttempt();
            if (card.getPinAttempts() >= 3) {
                blockCard(card);
            }
            return false;
        }

        // Success - reset attempts
        card.resetPinAttempts();
        return true;
    }

    private boolean validateCard(Card card) {
        if (!card.isValid()) {
            return false;
        }
        return bankingService.validateCardWithBank(card.getCardNumber());
    }

    private boolean validatePin(Card card, String pin) {
        return bankingService.validatePinWithBank(card.getCardNumber(), pin);
    }

    private void blockCard(Card card) {
        card.blockCard();
        bankingService.blockCard(card.getCardNumber());
    }
}

/**
 * TransactionService - Handles all transactions
 *
 * Responsibilities:
 * - Process withdrawals
 * - Process deposits
 * - Process balance inquiries
 * - Ensure atomicity (all-or-nothing)
 */
class TransactionService {
    private final BankingService bankingService;

    public TransactionService(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    /**
     * Process cash withdrawal
     *
     * Steps:
     * 1. Validate (balance, limits, ATM cash)
     * 2. Debit account
     * 3. Dispense cash
     * 4. Update backend
     * 5. Create transaction record
     */
    public Transaction withdraw(Account account, double amount, ATM atm) {
        Transaction transaction = new Transaction(
            account.getAccountNumber(),
            TransactionType.WITHDRAWAL,
            amount,
            atm.getAtmId()
        );

        try {
            // Validate
            if (!account.hasSufficientBalance(amount)) {
                transaction.markAsFailed("Insufficient balance");
                return transaction;
            }

            if (!account.isWithinDailyLimit(amount)) {
                transaction.markAsFailed("Daily limit exceeded");
                return transaction;
            }

            if (!atm.hasSufficientCash(amount)) {
                transaction.markAsFailed("ATM has insufficient cash");
                return transaction;
            }

            // Execute
            account.debit(amount);
            Map<Integer, Integer> dispensed = atm.dispenseCash(amount);

            if (dispensed == null) {
                // Rollback
                account.credit(amount);
                transaction.markAsFailed("Cash dispensing failed");
                return transaction;
            }

            // Update backend
            boolean updated = bankingService.debitAccount(account.getAccountNumber(), amount);
            if (!updated) {
                // Rollback
                account.credit(amount);
                transaction.markAsFailed("Backend update failed");
                return transaction;
            }

            // Success
            transaction.markAsSuccess(account.getBalance());
            return transaction;

        } catch (Exception e) {
            transaction.markAsFailed("Error: " + e.getMessage());
            return transaction;
        }
    }

    /**
     * Process cash deposit
     */
    public Transaction deposit(Account account, double amount, ATM atm) {
        Transaction transaction = new Transaction(
            account.getAccountNumber(),
            TransactionType.DEPOSIT,
            amount,
            atm.getAtmId()
        );

        try {
            account.credit(amount);
            boolean updated = bankingService.creditAccount(account.getAccountNumber(), amount);

            if (updated) {
                transaction.markAsSuccess(account.getBalance());
            } else {
                account.debit(amount);  // Rollback
                transaction.markAsFailed("Backend update failed");
            }

            return transaction;

        } catch (Exception e) {
            transaction.markAsFailed("Error: " + e.getMessage());
            return transaction;
        }
    }

    /**
     * Process balance inquiry
     */
    public Transaction balanceInquiry(Account account, ATM atm) {
        Transaction transaction = new Transaction(
            account.getAccountNumber(),
            TransactionType.BALANCE_INQUIRY,
            0.0,
            atm.getAtmId()
        );

        double balance = bankingService.getAccountBalance(account.getAccountNumber());
        transaction.markAsSuccess(balance);
        return transaction;
    }
}

// ═══════════════════════════════════════════════════════════════════
// HARDWARE INTERFACES (Mock implementations for demo)
// ═══════════════════════════════════════════════════════════════════

interface CardReader {
    Card readCard();
    void ejectCard();
    void retainCard();
}

class MockCardReader implements CardReader {
    @Override
    public Card readCard() {
        // Simulate card reading
        return new Card("1234567890123456", "John Doe",
                       LocalDate.now().plusYears(3));
    }

    @Override
    public void ejectCard() {
        System.out.println("[CARD READER] Card ejected");
    }

    @Override
    public void retainCard() {
        System.out.println("[CARD READER] Card retained for security");
    }
}

interface Screen {
    void displayMessage(String message);
    void displayMenu(String[] options);
}

class MockScreen implements Screen {
    @Override
    public void displayMessage(String message) {
        System.out.println("[SCREEN] " + message);
    }

    @Override
    public void displayMenu(String[] options) {
        System.out.println("[SCREEN] ═══ MENU ═══");
        for (String option : options) {
            System.out.println("[SCREEN] " + option);
        }
    }
}

interface Keypad {
    String readPin();
    double readAmount();
    int readMenuChoice();
}

class MockKeypad implements Keypad {
    private Scanner scanner = new Scanner(System.in);

    @Override
    public String readPin() {
        System.out.print("[KEYPAD] Enter PIN: ");
        return scanner.nextLine();
    }

    @Override
    public double readAmount() {
        System.out.print("[KEYPAD] Enter amount: ");
        return scanner.nextDouble();
    }

    @Override
    public int readMenuChoice() {
        System.out.print("[KEYPAD] Enter choice: ");
        return scanner.nextInt();
    }
}

// ═══════════════════════════════════════════════════════════════════
// CUSTOM EXCEPTIONS
// ═══════════════════════════════════════════════════════════════════

class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}

// ═══════════════════════════════════════════════════════════════════
// MAIN ATM SERVICE (Facade Pattern)
// ═══════════════════════════════════════════════════════════════════

/**
 * ATMService - Main orchestrator (Facade pattern)
 *
 * Responsibilities:
 * - Manage ATM session lifecycle
 * - Coordinate all components
 * - Provide simple interface to complex subsystem
 */
class ATMService {
    private final ATM atm;
    private final AuthenticationService authService;
    private final TransactionService transactionService;
    private final CardReader cardReader;
    private final Screen screen;
    private final Keypad keypad;

    public ATMService(ATM atm, AuthenticationService authService,
                      TransactionService transactionService,
                      CardReader cardReader, Screen screen, Keypad keypad) {
        this.atm = atm;
        this.authService = authService;
        this.transactionService = transactionService;
        this.cardReader = cardReader;
        this.screen = screen;
        this.keypad = keypad;
    }

    /**
     * Start ATM session - main entry point
     */
    public void startSession() {
        screen.displayMessage("Welcome to ATM!");
        screen.displayMessage("Please insert your card");

        // Read card
        Card card = cardReader.readCard();

        // Authenticate
        screen.displayMessage("Enter your PIN");
        String pin = keypad.readPin();

        if (!authService.authenticateUser(card, pin)) {
            screen.displayMessage("Authentication failed!");
            cardReader.ejectCard();
            return;
        }

        screen.displayMessage("Authentication successful!");

        // Create account (in real system, fetch from database)
        Account account = new Account("ACC123", card.getCardHolderName(),
                                     10000.0, AccountType.SAVINGS);

        // Main menu loop
        boolean running = true;
        while (running) {
            showMainMenu();
            int choice = keypad.readMenuChoice();

            switch (choice) {
                case 1:
                    processBalanceInquiry(account);
                    break;
                case 2:
                    processWithdrawal(account);
                    break;
                case 3:
                    processDeposit(account);
                    break;
                case 4:
                    running = false;
                    break;
                default:
                    screen.displayMessage("Invalid choice!");
            }
        }

        // End session
        screen.displayMessage("Thank you for using our ATM!");
        cardReader.ejectCard();
    }

    private void showMainMenu() {
        String[] options = {
            "1. Balance Inquiry",
            "2. Cash Withdrawal",
            "3. Cash Deposit",
            "4. Exit"
        };
        screen.displayMenu(options);
    }

    private void processBalanceInquiry(Account account) {
        Transaction txn = transactionService.balanceInquiry(account, atm);
        screen.displayMessage("Current Balance: ₹" + txn.getBalanceAfter());
    }

    private void processWithdrawal(Account account) {
        screen.displayMessage("Enter amount to withdraw:");
        double amount = keypad.readAmount();

        Transaction txn = transactionService.withdraw(account, amount, atm);

        if (txn.getStatus() == TransactionStatus.SUCCESS) {
            screen.displayMessage("Withdrawal successful!");
            screen.displayMessage("Please collect your cash");
            screen.displayMessage("New balance: ₹" + txn.getBalanceAfter());
        } else {
            screen.displayMessage("Withdrawal failed: " + txn.getFailureReason());
        }
    }

    private void processDeposit(Account account) {
        screen.displayMessage("Enter amount to deposit:");
        double amount = keypad.readAmount();

        Transaction txn = transactionService.deposit(account, amount, atm);

        if (txn.getStatus() == TransactionStatus.SUCCESS) {
            screen.displayMessage("Deposit successful!");
            screen.displayMessage("New balance: ₹" + txn.getBalanceAfter());
        } else {
            screen.displayMessage("Deposit failed: " + txn.getFailureReason());
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// MAIN DEMO
// ═══════════════════════════════════════════════════════════════════

public class ATMSystemDemo {
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("  ATM SYSTEM - COMPLETE IMPLEMENTATION");
        System.out.println("═══════════════════════════════════════\n");

        // Create components
        ATM atm = new ATM("ATM_001");

        // Load cash in ATM
        atm.cashBins.get(100).addNotes(1000);   // 1000×₹100 = ₹1,00,000
        atm.cashBins.get(500).addNotes(500);    // 500×₹500 = ₹2,50,000
        atm.cashBins.get(2000).addNotes(250);   // 250×₹2000 = ₹5,00,000
        // Total: ₹8,50,000

        BankingService bankingService = new MockBankingService();
        AuthenticationService authService = new AuthenticationService(bankingService);
        TransactionService transactionService = new TransactionService(bankingService);

        CardReader cardReader = new MockCardReader();
        Screen screen = new MockScreen();
        Keypad keypad = new MockKeypad();

        // Create ATMService (Facade)
        ATMService atmService = new ATMService(
            atm, authService, transactionService,
            cardReader, screen, keypad
        );

        // Start ATM session
        atmService.startSession();

        System.out.println("\n═══════════════════════════════════════");
        System.out.println("  SESSION ENDED");
        System.out.println("═══════════════════════════════════════");
    }
}

/*
 * ═══════════════════════════════════════════════════════════════════
 * HOW TO RUN:
 * ═══════════════════════════════════════════════════════════════════
 *
 * 1. Save this file as ATMSystemDemo.java
 * 2. Compile: javac ATMSystemDemo.java
 * 3. Run: java ATMSystemDemo
 * 4. Enter PIN: 1234 (hardcoded in MockBankingService)
 * 5. Select menu options
 *
 * ═══════════════════════════════════════════════════════════════════
 * SAMPLE INTERACTION:
 * ═══════════════════════════════════════════════════════════════════
 *
 * [SCREEN] Welcome to ATM!
 * [SCREEN] Please insert your card
 * [SCREEN] Enter your PIN
 * [KEYPAD] Enter PIN: 1234
 * [SCREEN] Authentication successful!
 * [SCREEN] ═══ MENU ═══
 * [SCREEN] 1. Balance Inquiry
 * [SCREEN] 2. Cash Withdrawal
 * [SCREEN] 3. Cash Deposit
 * [SCREEN] 4. Exit
 * [KEYPAD] Enter choice: 2
 * [SCREEN] Enter amount to withdraw:
 * [KEYPAD] Enter amount: 5000
 * MOCK: Debited ₹5000.0 from account ACC123
 * [SCREEN] Withdrawal successful!
 * [SCREEN] Please collect your cash
 * [SCREEN] New balance: ₹5000.0
 *
 * ═══════════════════════════════════════════════════════════════════
 */
