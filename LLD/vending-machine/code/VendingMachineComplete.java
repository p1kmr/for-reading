/**
 * VENDING MACHINE - COMPLETE IMPLEMENTATION
 *
 * This file contains the complete implementation of a Vending Machine system
 * using Object-Oriented Design principles and design patterns.
 *
 * DESIGN PATTERNS USED:
 * 1. Singleton Pattern - VendingMachine (single instance)
 * 2. State Pattern - VendingMachineState and concrete states
 * 3. Value Object Pattern - Money class
 * 4. Factory Pattern - For creating products
 *
 * SOLID PRINCIPLES APPLIED:
 * 1. Single Responsibility - Each class has one purpose
 * 2. Open/Closed - Easy to extend (new states, payment methods)
 * 3. Liskov Substitution - All states interchangeable
 * 4. Interface Segregation - Focused interfaces
 * 5. Dependency Inversion - Depend on abstractions
 *
 * @author LLD Design Team
 * @version 1.0
 */

package com.vendingmachine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// ===========================
// ENUMERATIONS
// ===========================

/**
 * Product types/categories
 * Using enum ensures type-safety and prevents invalid values
 */
enum ProductType {
    BEVERAGE("Beverages"),
    SNACK("Snacks"),
    FOOD("Food Items"),
    CANDY("Candies");

    private final String displayName;

    ProductType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

/**
 * Coin denominations (Indian Rupees)
 * Enum ensures only valid coin values can be created
 */
enum Coin {
    ONE(1.0),
    TWO(2.0),
    FIVE(5.0),
    TEN(10.0);

    private final double value;

    Coin(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}

/**
 * Note denominations (Indian Rupees)
 */
enum Note {
    TEN(10.0),
    TWENTY(20.0),
    FIFTY(50.0),
    HUNDRED(100.0),
    FIVE_HUNDRED(500.0);

    private final double value;

    Note(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}

/**
 * Transaction status lifecycle
 */
enum TransactionStatus {
    INITIATED,
    PAYMENT_RECEIVED,
    PRODUCT_DISPENSED,
    CHANGE_RETURNED,
    COMPLETED,
    CANCELLED,
    FAILED
}

// ===========================
// DOMAIN ENTITIES
// ===========================

/**
 * Product - Represents an item that can be purchased
 *
 * Design Decision: Immutable (except price) to prevent accidental modifications
 */
class Product {
    private final String productId;      // Internal identifier (database)
    private final String name;            // Display name
    private final String code;            // External code (customer sees "A1")
    private final ProductType type;       // Category
    private double price;                 // Only mutable field (admin can update)

    public Product(String productId, String name, double price,
                   String code, ProductType type) {
        // Validation
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (productId == null || name == null || code == null) {
            throw new IllegalArgumentException("Fields cannot be null");
        }

        this.productId = productId;
        this.name = name;
        this.price = price;
        this.code = code;
        this.type = type;
    }

    // Getters only (immutability)
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCode() { return code; }
    public ProductType getType() { return type; }

    // Only price can be updated (business requirement)
    public void setPrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (₹%.2f)", code, name, price);
    }
}

/**
 * Inventory - Tracks quantity of a product
 *
 * Design Decision: Synchronized methods for thread-safety
 */
class Inventory {
    private final String productId;
    private int quantity;
    private int minThreshold;   // Alert when quantity <= threshold

    public Inventory(String productId, int initialQuantity) {
        this.productId = productId;
        this.quantity = initialQuantity;
        this.minThreshold = 2;  // Default threshold
    }

    /**
     * Deduct quantity atomically
     * Returns false if insufficient quantity
     * Thread-safe using synchronized
     */
    public synchronized boolean deductQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false;
    }

    public synchronized void addQuantity(int amount) {
        quantity += amount;
    }

    public boolean isAvailable() {
        return quantity > 0;
    }

    public boolean needsRefill() {
        return quantity <= minThreshold;
    }

    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public void setMinThreshold(int threshold) { this.minThreshold = threshold; }
}

/**
 * Money - Value Object Pattern
 *
 * Immutable class representing monetary amount
 * Operations return NEW Money objects (functional style)
 */
final class Money {
    private final double amount;

    public Money(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    // All operations return NEW Money objects (immutability)
    public Money add(Money other) {
        return new Money(this.amount + other.amount);
    }

    public Money subtract(Money other) {
        if (this.amount < other.amount) {
            throw new IllegalArgumentException("Cannot subtract larger amount");
        }
        return new Money(this.amount - other.amount);
    }

    public boolean isGreaterThan(Money other) {
        return this.amount > other.amount;
    }

    public boolean isEqualTo(Money other) {
        // Use epsilon for floating-point comparison
        return Math.abs(this.amount - other.amount) < 0.001;
    }

    // Value-based equality
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Money)) return false;
        Money other = (Money) obj;
        return Math.abs(this.amount - other.amount) < 0.001;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(amount);
    }

    @Override
    public String toString() {
        return String.format("₹%.2f", amount);
    }
}

// ===========================
// MANAGERS (Single Responsibility)
// ===========================

/**
 * ProductCatalog - Manages all products
 *
 * Uses HashMap for O(1) lookups
 */
class ProductCatalog {
    // Two maps for different access patterns
    private Map<String, Product> productsById;      // Lookup by ID
    private Map<String, Product> productsByCode;    // Lookup by code (A1, B2, etc.)

    public ProductCatalog() {
        this.productsById = new HashMap<>();
        this.productsByCode = new HashMap<>();
    }

    public void addProduct(Product product) {
        productsById.put(product.getProductId(), product);
        productsByCode.put(product.getCode(), product);
    }

    public void removeProduct(String productId) {
        Product product = productsById.remove(productId);
        if (product != null) {
            productsByCode.remove(product.getCode());
        }
    }

    public Product getProductByCode(String code) {
        return productsByCode.get(code);
    }

    public Product getProductById(String productId) {
        return productsById.get(productId);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(productsById.values());
    }

    public void updatePrice(String productId, double newPrice) {
        Product product = productsById.get(productId);
        if (product != null) {
            product.setPrice(newPrice);
        }
    }

    public boolean productExists(String productId) {
        return productsById.containsKey(productId);
    }
}

/**
 * InventoryManager - Manages all inventories
 *
 * Thread-safe operations
 */
class InventoryManager {
    private Map<String, Inventory> inventories;

    public InventoryManager() {
        this.inventories = new HashMap<>();
    }

    public void addInventory(String productId, int initialQuantity) {
        inventories.put(productId, new Inventory(productId, initialQuantity));
    }

    public synchronized boolean deductProduct(String productId) {
        Inventory inventory = inventories.get(productId);
        return inventory != null && inventory.deductQuantity(1);
    }

    public synchronized void refillProduct(String productId, int quantity) {
        Inventory inventory = inventories.get(productId);
        if (inventory != null) {
            inventory.addQuantity(quantity);
        } else {
            addInventory(productId, quantity);
        }
    }

    public boolean isProductAvailable(String productId) {
        Inventory inventory = inventories.get(productId);
        return inventory != null && inventory.isAvailable();
    }

    public int getQuantity(String productId) {
        Inventory inventory = inventories.get(productId);
        return inventory != null ? inventory.getQuantity() : 0;
    }

    public List<String> getLowStockProducts() {
        List<String> lowStock = new ArrayList<>();
        for (Inventory inv : inventories.values()) {
            if (inv.needsRefill()) {
                lowStock.add(inv.getProductId());
            }
        }
        return lowStock;
    }
}

/**
 * CashInventory - Manages coins and notes for giving change
 *
 * Critical for change calculation
 */
class CashInventory {
    private Map<Coin, Integer> coinInventory;
    private Map<Note, Integer> noteInventory;

    public CashInventory() {
        this.coinInventory = new HashMap<>();
        this.noteInventory = new HashMap<>();

        // Initialize with zero counts
        for (Coin coin : Coin.values()) {
            coinInventory.put(coin, 0);
        }
        for (Note note : Note.values()) {
            noteInventory.put(note, 0);
        }
    }

    public synchronized void addCoins(Coin coin, int count) {
        coinInventory.put(coin, coinInventory.get(coin) + count);
    }

    public synchronized void addNotes(Note note, int count) {
        noteInventory.put(note, noteInventory.get(note) + count);
    }

    public synchronized boolean deductCoins(Coin coin, int count) {
        int current = coinInventory.get(coin);
        if (current >= count) {
            coinInventory.put(coin, current - count);
            return true;
        }
        return false;
    }

    public synchronized boolean deductNotes(Note note, int count) {
        int current = noteInventory.get(note);
        if (current >= count) {
            noteInventory.put(note, current - count);
            return true;
        }
        return false;
    }

    public int getCoinCount(Coin coin) {
        return coinInventory.get(coin);
    }

    public int getNoteCount(Note note) {
        return noteInventory.get(note);
    }

    public Money getTotalCash() {
        double total = 0;

        for (Map.Entry<Coin, Integer> entry : coinInventory.entrySet()) {
            total += entry.getKey().getValue() * entry.getValue();
        }

        for (Map.Entry<Note, Integer> entry : noteInventory.entrySet()) {
            total += entry.getKey().getValue() * entry.getValue();
        }

        return new Money(total);
    }
}

// ===========================
// CHANGE CALCULATION (Algorithm)
// ===========================

/**
 * ChangeResult - Result of change calculation
 *
 * Immutable result object
 */
class ChangeResult {
    private final boolean possible;
    private final Map<Object, Integer> breakdown;  // Coin/Note -> Count
    private final String message;

    public ChangeResult(boolean possible, Map<Object, Integer> breakdown, String message) {
        this.possible = possible;
        this.breakdown = breakdown != null ? new HashMap<>(breakdown) : new HashMap<>();
        this.message = message;
    }

    public boolean isPossible() { return possible; }
    public Map<Object, Integer> getBreakdown() { return new HashMap<>(breakdown); }
    public String getMessage() { return message; }

    public double getTotalValue() {
        double total = 0;
        for (Map.Entry<Object, Integer> entry : breakdown.entrySet()) {
            Object denom = entry.getKey();
            int count = entry.getValue();

            if (denom instanceof Coin) {
                total += ((Coin) denom).getValue() * count;
            } else if (denom instanceof Note) {
                total += ((Note) denom).getValue() * count;
            }
        }
        return total;
    }
}

/**
 * ChangeCalculator - Implements greedy algorithm for change calculation
 *
 * ALGORITHM: Greedy (always use largest denomination first)
 * TIME COMPLEXITY: O(n) where n = number of denominations (constant = 8)
 * SPACE COMPLEXITY: O(n) for result map
 *
 * NOTE: Greedy works for Indian currency (canonical coin system)
 */
class ChangeCalculator {
    private CashInventory cashInventory;

    // All denominations in descending order (critical for greedy algorithm)
    private static final List<Double> DENOMINATIONS = Arrays.asList(
        500.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.0, 1.0
    );

    public ChangeCalculator(CashInventory cashInventory) {
        this.cashInventory = cashInventory;
    }

    /**
     * Calculate change using greedy algorithm
     *
     * @param amount Amount of change needed
     * @return ChangeResult with breakdown or failure
     */
    public ChangeResult calculateChange(double amount) {
        // Edge case: no change needed
        if (amount == 0) {
            return new ChangeResult(true, new HashMap<>(), "No change needed");
        }

        Map<Object, Integer> breakdown = new HashMap<>();
        double remaining = amount;

        // Greedy: use largest denominations first
        for (double denom : DENOMINATIONS) {
            if (remaining < 0.01) {  // Floating-point tolerance
                break;
            }

            // How many of this denomination needed?
            int needed = (int) (remaining / denom);

            if (needed == 0) {
                continue;  // This denomination too large
            }

            // How many available in inventory?
            int available = getAvailableCount(denom);

            // Use as many as possible (min of needed and available)
            int use = Math.min(needed, available);

            if (use > 0) {
                Object denominationKey = getDenominationObject(denom);
                breakdown.put(denominationKey, use);
                remaining -= (denom * use);

                // Handle floating-point precision
                remaining = Math.round(remaining * 100.0) / 100.0;
            }
        }

        // Check if exact change made
        if (Math.abs(remaining) < 0.01) {
            return new ChangeResult(true, breakdown, "Change calculated successfully");
        } else {
            return new ChangeResult(false, null,
                "Cannot make exact change. Short by ₹" + String.format("%.2f", remaining));
        }
    }

    public boolean canMakeChange(double amount) {
        return calculateChange(amount).isPossible();
    }

    private int getAvailableCount(double value) {
        for (Coin coin : Coin.values()) {
            if (Math.abs(coin.getValue() - value) < 0.01) {
                return cashInventory.getCoinCount(coin);
            }
        }
        for (Note note : Note.values()) {
            if (Math.abs(note.getValue() - value) < 0.01) {
                return cashInventory.getNoteCount(note);
            }
        }
        return 0;
    }

    private Object getDenominationObject(double value) {
        for (Coin coin : Coin.values()) {
            if (Math.abs(coin.getValue() - value) < 0.01) {
                return coin;
            }
        }
        for (Note note : Note.values()) {
            if (Math.abs(note.getValue() - value) < 0.01) {
                return note;
            }
        }
        throw new IllegalArgumentException("Invalid denomination: " + value);
    }
}

// ===========================
// STATE PATTERN IMPLEMENTATION
// ===========================

/**
 * VendingMachineState - State interface
 *
 * All states implement this interface (Polymorphism)
 */
interface VendingMachineState {
    void selectProduct(String code);
    void insertCoin(Coin coin);
    void insertNote(Note note);
    void dispenseProduct();
    void returnChange();
    void cancelTransaction();
}

/**
 * IdleState - Default state, waiting for user
 *
 * ALLOWED: Select product
 * DISALLOWED: Insert money, dispense, return change
 */
class IdleState implements VendingMachineState {
    private VendingMachine machine;

    public IdleState(VendingMachine machine) {
        this.machine = machine;
        System.out.println("🟢 Machine is IDLE. Please select a product.");
    }

    @Override
    public void selectProduct(String code) {
        Product product = machine.getCatalog().getProductByCode(code);

        if (product == null) {
            System.out.println("❌ Error: Invalid product code");
            return;
        }

        if (!machine.getInventoryManager().isProductAvailable(product.getProductId())) {
            System.out.println("❌ Error: Product out of stock");
            return;
        }

        // Valid selection
        machine.setSelectedProduct(product);
        System.out.println("✅ Selected: " + product.getName() + " - ₹" + product.getPrice());
        System.out.println("Please insert payment...");
        machine.setState(new ProductSelectionState(machine));
    }

    @Override
    public void insertCoin(Coin coin) {
        System.out.println("❌ Please select a product first");
    }

    @Override
    public void insertNote(Note note) {
        System.out.println("❌ Please select a product first");
    }

    @Override
    public void dispenseProduct() {
        System.out.println("❌ No product selected");
    }

    @Override
    public void returnChange() {
        System.out.println("❌ No transaction in progress");
    }

    @Override
    public void cancelTransaction() {
        System.out.println("ℹ️ No transaction to cancel");
    }
}

/**
 * ProductSelectionState - Product selected, awaiting payment
 *
 * ALLOWED: Insert money, cancel, change product
 * DISALLOWED: Dispense
 */
class ProductSelectionState implements VendingMachineState {
    private VendingMachine machine;

    public ProductSelectionState(VendingMachine machine) {
        this.machine = machine;
    }

    @Override
    public void selectProduct(String code) {
        System.out.println("ℹ️ Changing product selection...");
        machine.setState(new IdleState(machine));
        machine.getState().selectProduct(code);
    }

    @Override
    public void insertCoin(Coin coin) {
        machine.setState(new PaymentState(machine));
        machine.getState().insertCoin(coin);
    }

    @Override
    public void insertNote(Note note) {
        machine.setState(new PaymentState(machine));
        machine.getState().insertNote(note);
    }

    @Override
    public void dispenseProduct() {
        System.out.println("❌ Please insert payment first");
    }

    @Override
    public void returnChange() {
        System.out.println("❌ No payment made yet");
    }

    @Override
    public void cancelTransaction() {
        System.out.println("🔄 Transaction cancelled");
        machine.resetTransaction();
        machine.setState(new IdleState(machine));
    }
}

/**
 * PaymentState - Accumulating payment
 *
 * ALLOWED: Insert more money, cancel
 * AUTO-TRANSITION: When payment >= price, move to DispenseState
 */
class PaymentState implements VendingMachineState {
    private VendingMachine machine;

    public PaymentState(VendingMachine machine) {
        this.machine = machine;
    }

    @Override
    public void selectProduct(String code) {
        System.out.println("❌ Cancel current transaction first");
    }

    @Override
    public void insertCoin(Coin coin) {
        Money payment = machine.getCurrentPayment();
        payment = payment.add(new Money(coin.getValue()));
        machine.setCurrentPayment(payment);
        machine.getCashInventory().addCoins(coin, 1);

        System.out.println("💰 Inserted: ₹" + coin.getValue());
        System.out.println("💰 Total payment: " + payment);

        checkPaymentSufficient();
    }

    @Override
    public void insertNote(Note note) {
        Money payment = machine.getCurrentPayment();
        payment = payment.add(new Money(note.getValue()));
        machine.setCurrentPayment(payment);
        machine.getCashInventory().addNotes(note, 1);

        System.out.println("💵 Inserted: ₹" + note.getValue());
        System.out.println("💰 Total payment: " + payment);

        checkPaymentSufficient();
    }

    private void checkPaymentSufficient() {
        Money payment = machine.getCurrentPayment();
        Product product = machine.getSelectedProduct();
        Money price = new Money(product.getPrice());

        if (payment.isGreaterThan(price) || payment.isEqualTo(price)) {
            // Check if we can make change
            double changeAmount = payment.getAmount() - price.getAmount();
            if (changeAmount > 0) {
                ChangeCalculator calc = new ChangeCalculator(machine.getCashInventory());
                if (!calc.canMakeChange(changeAmount)) {
                    System.out.println("❌ Cannot make change for this amount.");
                    System.out.println("🔄 Refunding payment...");
                    cancelTransaction();
                    return;
                }
            }

            System.out.println("✅ Payment sufficient. Dispensing product...");
            machine.setState(new DispenseState(machine));
            machine.getState().dispenseProduct();
        } else {
            Money remaining = price.subtract(payment);
            System.out.println("ℹ️ Please insert ₹" + String.format("%.2f", remaining.getAmount()) + " more");
        }
    }

    @Override
    public void dispenseProduct() {
        System.out.println("❌ Insufficient payment");
    }

    @Override
    public void returnChange() {
        System.out.println("❌ Complete payment first");
    }

    @Override
    public void cancelTransaction() {
        Money refund = machine.getCurrentPayment();
        System.out.println("🔄 Transaction cancelled. Refunding: " + refund);

        // Process refund
        ChangeCalculator calc = new ChangeCalculator(machine.getCashInventory());
        ChangeResult result = calc.calculateChange(refund.getAmount());

        if (result.isPossible()) {
            // Deduct refund from inventory
            for (Map.Entry<Object, Integer> entry : result.getBreakdown().entrySet()) {
                Object denom = entry.getKey();
                int count = entry.getValue();
                if (denom instanceof Coin) {
                    machine.getCashInventory().deductCoins((Coin) denom, count);
                } else if (denom instanceof Note) {
                    machine.getCashInventory().deductNotes((Note) denom, count);
                }
            }
            System.out.println("💸 Please collect your refund from tray");
        } else {
            System.out.println("⚠️ Warning: Refund processing issue");
        }

        machine.resetTransaction();
        machine.setState(new IdleState(machine));
    }
}

/**
 * DispenseState - Dispensing product
 *
 * AUTO-STATE: User cannot interact during dispense
 * AUTO-TRANSITION: To ReturnChangeState or IdleState
 */
class DispenseState implements VendingMachineState {
    private VendingMachine machine;

    public DispenseState(VendingMachine machine) {
        this.machine = machine;
    }

    @Override
    public void selectProduct(String code) {
        System.out.println("❌ Product being dispensed");
    }

    @Override
    public void insertCoin(Coin coin) {
        System.out.println("❌ Product being dispensed");
    }

    @Override
    public void insertNote(Note note) {
        System.out.println("❌ Product being dispensed");
    }

    @Override
    public void dispenseProduct() {
        Product product = machine.getSelectedProduct();

        // Deduct from inventory
        boolean success = machine.getInventoryManager().deductProduct(product.getProductId());

        if (!success) {
            System.out.println("❌ Dispense failed. Refunding...");
            machine.setState(new PaymentState(machine));
            machine.getState().cancelTransaction();
            return;
        }

        System.out.println("📦 Dispensing: " + product.getName());
        System.out.println("✅ Product dispensed! Please collect from tray.");

        // Calculate change
        Money payment = machine.getCurrentPayment();
        Money price = new Money(product.getPrice());

        if (payment.isEqualTo(price)) {
            // Exact payment, no change
            System.out.println("✅ Thank you! Exact payment.");
            machine.resetTransaction();
            machine.setState(new IdleState(machine));
        } else {
            // Need to return change
            machine.setState(new ReturnChangeState(machine));
            machine.getState().returnChange();
        }
    }

    @Override
    public void returnChange() {
        System.out.println("❌ Dispense in progress");
    }

    @Override
    public void cancelTransaction() {
        System.out.println("❌ Cannot cancel during dispense");
    }
}

/**
 * ReturnChangeState - Returning change to user
 *
 * AUTO-STATE: User cannot interact
 * AUTO-TRANSITION: To IdleState after change returned
 */
class ReturnChangeState implements VendingMachineState {
    private VendingMachine machine;

    public ReturnChangeState(VendingMachine machine) {
        this.machine = machine;
    }

    @Override
    public void selectProduct(String code) {
        System.out.println("❌ Returning change");
    }

    @Override
    public void insertCoin(Coin coin) {
        System.out.println("❌ Returning change");
    }

    @Override
    public void insertNote(Note note) {
        System.out.println("❌ Returning change");
    }

    @Override
    public void dispenseProduct() {
        System.out.println("❌ Already dispensed");
    }

    @Override
    public void returnChange() {
        Money payment = machine.getCurrentPayment();
        Money price = new Money(machine.getSelectedProduct().getPrice());
        double changeAmount = payment.getAmount() - price.getAmount();

        System.out.println("💵 Calculating change: ₹" + String.format("%.2f", changeAmount));

        ChangeCalculator calc = new ChangeCalculator(machine.getCashInventory());
        ChangeResult result = calc.calculateChange(changeAmount);

        if (result.isPossible()) {
            System.out.println("💸 Dispensing change:");
            // Deduct change from cash inventory
            for (Map.Entry<Object, Integer> entry : result.getBreakdown().entrySet()) {
                Object denom = entry.getKey();
                int count = entry.getValue();

                double value = 0;
                if (denom instanceof Coin) {
                    value = ((Coin) denom).getValue();
                    machine.getCashInventory().deductCoins((Coin) denom, count);
                } else if (denom instanceof Note) {
                    value = ((Note) denom).getValue();
                    machine.getCashInventory().deductNotes((Note) denom, count);
                }

                System.out.println("  💰 ₹" + value + " x " + count);
            }
            System.out.println("✅ Please collect your change: ₹" + String.format("%.2f", changeAmount));
        } else {
            System.out.println("⚠️ Warning: " + result.getMessage());
        }

        machine.resetTransaction();
        machine.setState(new IdleState(machine));
    }

    @Override
    public void cancelTransaction() {
        System.out.println("❌ Cannot cancel, product already dispensed");
    }
}

// ===========================
// VENDING MACHINE (Singleton + Facade)
// ===========================

/**
 * VendingMachine - Main controller class
 *
 * PATTERNS:
 * - Singleton: Only one instance
 * - Facade: Simplifies complex subsystem
 * - State: Delegates behavior to current state
 */
class VendingMachine {
    // Singleton instance
    private static VendingMachine instance;

    // Managers (Composition - VendingMachine owns these)
    private ProductCatalog catalog;
    private InventoryManager inventoryManager;
    private CashInventory cashInventory;

    // State Pattern
    private VendingMachineState currentState;

    // Transaction state
    private Product selectedProduct;
    private Money currentPayment;

    // Private constructor (Singleton)
    private VendingMachine() {
        this.catalog = new ProductCatalog();
        this.inventoryManager = new InventoryManager();
        this.cashInventory = new CashInventory();
        this.currentPayment = new Money(0);

        // Initialize to Idle state
        this.currentState = new IdleState(this);
    }

    // Get singleton instance (thread-safe)
    public static synchronized VendingMachine getInstance() {
        if (instance == null) {
            instance = new VendingMachine();
        }
        return instance;
    }

    // === STATE DELEGATION METHODS ===
    public void selectProduct(String code) {
        currentState.selectProduct(code);
    }

    public void insertCoin(Coin coin) {
        currentState.insertCoin(coin);
    }

    public void insertNote(Note note) {
        currentState.insertNote(note);
    }

    public void cancelTransaction() {
        currentState.cancelTransaction();
    }

    // === STATE MANAGEMENT ===
    public void setState(VendingMachineState newState) {
        this.currentState = newState;
    }

    public VendingMachineState getState() {
        return currentState;
    }

    // === TRANSACTION MANAGEMENT ===
    public void setSelectedProduct(Product product) {
        this.selectedProduct = product;
    }

    public Product getSelectedProduct() {
        return selectedProduct;
    }

    public void setCurrentPayment(Money payment) {
        this.currentPayment = payment;
    }

    public Money getCurrentPayment() {
        return currentPayment;
    }

    public void resetTransaction() {
        this.selectedProduct = null;
        this.currentPayment = new Money(0);
    }

    // === GETTERS FOR MANAGERS ===
    public ProductCatalog getCatalog() { return catalog; }
    public InventoryManager getInventoryManager() { return inventoryManager; }
    public CashInventory getCashInventory() { return cashInventory; }

    // === ADMIN OPERATIONS ===
    public void addProduct(Product product, int initialQuantity) {
        catalog.addProduct(product);
        inventoryManager.addInventory(product.getProductId(), initialQuantity);
        System.out.println("➕ Added: " + product.getName() + " (Qty: " + initialQuantity + ")");
    }

    public void refillProduct(String productId, int quantity) {
        inventoryManager.refillProduct(productId, quantity);
        System.out.println("🔄 Refilled product: " + productId + " (+" + quantity + " units)");
    }

    public void refillCash(Coin coin, int count) {
        cashInventory.addCoins(coin, count);
        System.out.println("💰 Refilled ₹" + coin.getValue() + " coins: +" + count);
    }

    public void refillCash(Note note, int count) {
        cashInventory.addNotes(note, count);
        System.out.println("💵 Refilled ₹" + note.getValue() + " notes: +" + count);
    }

    public void displayInventory() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📦 PRODUCT INVENTORY");
        System.out.println("=".repeat(60));
        System.out.printf("%-6s %-20s %-10s %-6s %s\n", "Code", "Product", "Price", "Qty", "Status");
        System.out.println("-".repeat(60));

        for (Product product : catalog.getAllProducts()) {
            int qty = inventoryManager.getQuantity(product.getProductId());
            String status = qty > 0 ? "✅ In Stock" : "❌ Out of Stock";
            System.out.printf("%-6s %-20s ₹%-9.2f %-6d %s\n",
                product.getCode(), product.getName(), product.getPrice(), qty, status);
        }
        System.out.println("=".repeat(60) + "\n");
    }

    public void displayCashInventory() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("💰 CASH INVENTORY");
        System.out.println("=".repeat(40));

        System.out.println("Coins:");
        for (Coin coin : Coin.values()) {
            System.out.printf("  ₹%-5.0f : %d\n", coin.getValue(), cashInventory.getCoinCount(coin));
        }

        System.out.println("\nNotes:");
        for (Note note : Note.values()) {
            System.out.printf("  ₹%-5.0f : %d\n", note.getValue(), cashInventory.getNoteCount(note));
        }

        System.out.println("\nTotal Cash: " + cashInventory.getTotalCash());
        System.out.println("=".repeat(40) + "\n");
    }
}

// ===========================
// DEMO / MAIN CLASS
// ===========================

/**
 * Main class demonstrating vending machine functionality
 */
public class VendingMachineComplete {
    public static void main(String[] args) {
        System.out.println("🏪 VENDING MACHINE SYSTEM");
        System.out.println("=".repeat(60));

        // Get singleton instance
        VendingMachine machine = VendingMachine.getInstance();

        // === ADMIN: SETUP ===
        System.out.println("\n👨‍💼 ADMIN: Setting up machine...\n");

        // Add products
        Product coke = new Product("P001", "Coca Cola", 20.0, "A1", ProductType.BEVERAGE);
        Product chips = new Product("P002", "Lays Chips", 15.0, "A2", ProductType.SNACK);
        Product water = new Product("P003", "Water Bottle", 10.0, "A3", ProductType.BEVERAGE);
        Product chocolate = new Product("P004", "Dairy Milk", 25.0, "B1", ProductType.CANDY);

        machine.addProduct(coke, 10);
        machine.addProduct(chips, 5);
        machine.addProduct(water, 20);
        machine.addProduct(chocolate, 8);

        // Refill cash for change
        machine.refillCash(Coin.ONE, 50);
        machine.refillCash(Coin.TWO, 30);
        machine.refillCash(Coin.FIVE, 20);
        machine.refillCash(Coin.TEN, 15);
        machine.refillCash(Note.TEN, 10);
        machine.refillCash(Note.TWENTY, 10);
        machine.refillCash(Note.FIFTY, 5);

        machine.displayInventory();

        // === SCENARIO 1: Exact Payment ===
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🛒 SCENARIO 1: Buying Coke with exact payment");
        System.out.println("=".repeat(60) + "\n");

        machine.selectProduct("A1");  // Coke - ₹20
        machine.insertNote(Note.TWENTY);  // Exact payment

        // === SCENARIO 2: Overpayment (Change Needed) ===
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🛒 SCENARIO 2: Buying Chips with overpayment");
        System.out.println("=".repeat(60) + "\n");

        machine.selectProduct("A2");  // Chips - ₹15
        machine.insertCoin(Coin.TEN);    // ₹10
        machine.insertCoin(Coin.TEN);    // ₹10 (total ₹20)

        // === SCENARIO 3: Incremental Payment ===
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🛒 SCENARIO 3: Buying Chocolate incrementally");
        System.out.println("=".repeat(60) + "\n");

        machine.selectProduct("B1");  // Chocolate - ₹25
        machine.insertCoin(Coin.TEN);    // ₹10
        machine.insertCoin(Coin.TEN);    // ₹10 (total ₹20)
        machine.insertCoin(Coin.FIVE);   // ₹5 (total ₹25)

        // === SCENARIO 4: Cancelled Transaction ===
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🛒 SCENARIO 4: Cancelled transaction");
        System.out.println("=".repeat(60) + "\n");

        machine.selectProduct("A3");  // Water - ₹10
        machine.insertCoin(Coin.FIVE);   // ₹5
        machine.insertCoin(Coin.TWO);    // ₹2 (total ₹7)
        machine.cancelTransaction();      // Changed mind

        // === FINAL STATE ===
        System.out.println("\n👨‍💼 ADMIN: Final inventory check");
        machine.displayInventory();
        machine.displayCashInventory();

        System.out.println("\n✅ Vending Machine Demo Complete!");
    }
}
