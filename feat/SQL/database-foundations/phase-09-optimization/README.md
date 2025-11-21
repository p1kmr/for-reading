# Phase 9: Performance & Optimization

## Table of Contents
- [Introduction](#introduction)
- [Sample Tables](#sample-tables)
- [1. Indexes](#1-indexes)
  - [What Are Indexes?](#what-are-indexes)
  - [B-Tree Indexes](#b-tree-indexes)
  - [Hash Indexes](#hash-indexes)
  - [Composite Indexes](#composite-indexes)
  - [Covering Indexes](#covering-indexes)
  - [When to Use/Not Use Indexes](#when-to-usenot-use-indexes)
- [2. EXPLAIN and Query Execution Plans](#2-explain-and-query-execution-plans)
  - [Reading EXPLAIN Output](#reading-explain-output)
  - [Key Metrics](#key-metrics)
  - [Identifying Bottlenecks](#identifying-bottlenecks)
- [3. Query Optimization Techniques](#3-query-optimization-techniques)
  - [SELECT Only Needed Columns](#select-only-needed-columns)
  - [Proper JOIN Order](#proper-join-order)
  - [Avoiding SELECT *](#avoiding-select-)
  - [Optimizing WHERE Clauses](#optimizing-where-clauses)
  - [Avoiding Functions on Indexed Columns](#avoiding-functions-on-indexed-columns)
- [4. Transaction Management](#4-transaction-management)
  - [ACID Properties](#acid-properties)
  - [BEGIN, COMMIT, ROLLBACK](#begin-commit-rollback)
  - [Isolation Levels](#isolation-levels)
  - [Deadlocks](#deadlocks)
- [5. Normalization](#5-normalization)
  - [First Normal Form (1NF)](#first-normal-form-1nf)
  - [Second Normal Form (2NF)](#second-normal-form-2nf)
  - [Third Normal Form (3NF)](#third-normal-form-3nf)
  - [Boyce-Codd Normal Form (BCNF)](#boyce-codd-normal-form-bcnf)
  - [When to Denormalize](#when-to-denormalize)
- [6. Code Examples](#6-code-examples)
- [7. Interview Questions](#7-interview-questions)
- [8. Performance Testing Tips](#8-performance-testing-tips)
- [9. Common Anti-Patterns](#9-common-anti-patterns)
- [Quick Reference Cheat Sheet](#quick-reference-cheat-sheet)

---

## Introduction

Performance optimization is the art and science of making your database queries run faster and use fewer resources. This phase covers the essential concepts every database professional must master: indexes for faster data retrieval, query execution plans for understanding database behavior, optimization techniques for writing efficient SQL, transaction management for data integrity, and normalization for proper database design.

Understanding these concepts separates junior developers from senior database engineers. A well-optimized query can run in milliseconds instead of minutes, saving both time and computing resources.

---

## Sample Tables

```sql
-- Create the database schema for optimization examples
CREATE TABLE customers (
    customer_id INT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    city VARCHAR(50),
    state VARCHAR(2),
    country VARCHAR(50),
    created_at DATETIME,
    last_login DATETIME,
    status VARCHAR(20)
);

CREATE TABLE products (
    product_id INT PRIMARY KEY,
    product_name VARCHAR(100),
    category VARCHAR(50),
    subcategory VARCHAR(50),
    price DECIMAL(10,2),
    cost DECIMAL(10,2),
    stock_quantity INT,
    supplier_id INT,
    created_at DATETIME
);

CREATE TABLE orders (
    order_id INT PRIMARY KEY,
    customer_id INT,
    order_date DATETIME,
    ship_date DATE,
    total_amount DECIMAL(12,2),
    status VARCHAR(20),
    shipping_method VARCHAR(50),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE order_items (
    item_id INT PRIMARY KEY,
    order_id INT,
    product_id INT,
    quantity INT,
    unit_price DECIMAL(10,2),
    discount DECIMAL(5,2),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE inventory_log (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT,
    change_type VARCHAR(20),
    quantity_change INT,
    change_date DATETIME,
    notes TEXT
);

-- Insert sample data
INSERT INTO customers VALUES
(1, 'John', 'Smith', 'john.smith@email.com', '555-0101', 'New York', 'NY', 'USA', '2022-01-15 10:30:00', '2024-03-10 14:22:00', 'active'),
(2, 'Sarah', 'Johnson', 'sarah.j@email.com', '555-0102', 'Los Angeles', 'CA', 'USA', '2022-03-20 09:15:00', '2024-03-12 08:45:00', 'active'),
(3, 'Michael', 'Williams', 'mike.w@email.com', '555-0103', 'Chicago', 'IL', 'USA', '2022-06-10 14:00:00', '2024-02-28 16:30:00', 'active'),
(4, 'Emily', 'Brown', 'emily.b@email.com', '555-0104', 'Houston', 'TX', 'USA', '2022-09-05 11:20:00', '2024-03-01 09:10:00', 'inactive'),
(5, 'David', 'Jones', 'david.jones@email.com', '555-0105', 'Phoenix', 'AZ', 'USA', '2023-01-18 16:45:00', '2024-03-15 12:00:00', 'active');

INSERT INTO products VALUES
(101, 'Laptop Pro 15', 'Electronics', 'Computers', 1299.99, 900.00, 45, 1, '2023-01-15 08:00:00'),
(102, 'Wireless Mouse', 'Electronics', 'Accessories', 29.99, 15.00, 200, 2, '2023-02-20 10:30:00'),
(103, 'USB-C Hub', 'Electronics', 'Accessories', 49.99, 25.00, 150, 2, '2023-03-10 14:15:00'),
(104, 'Monitor 27"', 'Electronics', 'Displays', 399.99, 280.00, 30, 1, '2023-04-05 09:00:00'),
(105, 'Keyboard Mech', 'Electronics', 'Accessories', 89.99, 45.00, 75, 3, '2023-05-12 11:45:00');

INSERT INTO orders VALUES
(1001, 1, '2024-01-15 09:30:00', '2024-01-17', 1549.97, 'delivered', 'Express'),
(1002, 2, '2024-01-20 14:45:00', '2024-01-23', 429.98, 'delivered', 'Standard'),
(1003, 1, '2024-02-01 11:00:00', '2024-02-04', 89.99, 'delivered', 'Standard'),
(1004, 3, '2024-02-10 08:15:00', '2024-02-12', 1699.97, 'shipped', 'Express'),
(1005, 5, '2024-03-01 16:20:00', NULL, 179.97, 'pending', 'Standard');

INSERT INTO order_items VALUES
(1, 1001, 101, 1, 1299.99, 0.00),
(2, 1001, 102, 2, 29.99, 0.00),
(3, 1001, 103, 1, 49.99, 10.00),
(4, 1002, 104, 1, 399.99, 0.00),
(5, 1002, 102, 1, 29.99, 0.00),
(6, 1003, 105, 1, 89.99, 0.00),
(7, 1004, 101, 1, 1299.99, 0.00),
(8, 1004, 104, 1, 399.99, 0.00),
(9, 1005, 102, 3, 29.99, 5.00),
(10, 1005, 105, 1, 89.99, 0.00);
```

---

## 1. Indexes

### What Are Indexes?

#### WHY Use Indexes?
- **Speed up data retrieval** - Indexes allow the database to find rows without scanning the entire table
- **Enforce uniqueness** - Unique indexes prevent duplicate values
- **Improve JOIN performance** - Indexes on foreign keys dramatically speed up joins
- **Enable efficient sorting** - Avoid expensive filesort operations
- **Support range queries** - Quickly find values within a range

#### WHAT is an Index?
An index is a data structure that improves the speed of data retrieval operations on a database table. Think of it like a book's index - instead of reading every page to find a topic, you look it up in the index and go directly to the relevant pages.

```
Without Index:                    With Index:
+------------------+              +------------------+
|  Table Scan      |              |  Index Lookup    |
|  Check row 1     |              |  Jump to row 847 |
|  Check row 2     |              +------------------+
|  Check row 3     |
|  ...             |              Time: O(log n)
|  Check row 1000  |
+------------------+
Time: O(n)
```

#### HOW Indexes Work

Indexes maintain a sorted copy of selected column(s) with pointers to the actual table rows:

```
                    INDEX STRUCTURE

    Index on 'last_name'          Actual Table
    +---------------+-----+       +----+------------+-------+
    | Value         | Ptr |       | ID | last_name  | email |
    +---------------+-----+       +----+------------+-------+
    | Brown         | →───────→   | 4  | Brown      | ...   |
    | Johnson       | →───────→   | 2  | Johnson    | ...   |
    | Jones         | →───────→   | 5  | Jones      | ...   |
    | Smith         | →───────→   | 1  | Smith      | ...   |
    | Williams      | →───────→   | 3  | Williams   | ...   |
    +---------------+-----+       +----+------------+-------+
```

#### WHEN to Create Indexes
- Columns frequently used in WHERE clauses
- Columns used in JOIN conditions
- Columns used in ORDER BY clauses
- Columns with high cardinality (many unique values)
- Foreign key columns

---

### B-Tree Indexes

The most common index type, B-tree (Balanced Tree) indexes organize data in a tree structure that maintains sorted order and allows searches, insertions, and deletions in logarithmic time.

#### B-Tree Structure Visualization

```
                        B-TREE INDEX VISUALIZATION

                              [Root Node]
                            +-----+-----+
                            | 50  | 100 |
                            +--+--+--+--+
                              /    |    \
                             /     |     \
                            /      |      \
              [Internal]   /  [Internal]   \  [Internal]
            +-----+-----+ / +-----+-----+ \ +-----+-----+
            | 25  | 35  |/  | 70  | 85  |  \| 120 | 150 |
            +--+--+--+--+   +--+--+--+--+   +--+--+--+--+
              /  |   \        /  |   \        /  |   \
             /   |    \      /   |    \      /   |    \
            v    v     v    v    v     v    v    v     v
         [Leaf Nodes - Contain actual data pointers]

    +-------+  +-------+  +-------+  +-------+  +-------+
    |10|15|20| |28|30|33| |55|60|65| |72|78|82| |125|140|
    +-------+  +-------+  +-------+  +-------+  +-------+
        ↓          ↓          ↓          ↓          ↓
    [Row Ptrs] [Row Ptrs] [Row Ptrs] [Row Ptrs] [Row Ptrs]


    Search for value 78:
    1. Start at root: 78 > 50, 78 < 100 → go middle
    2. At internal: 78 > 70, 78 < 85 → go middle
    3. At leaf: Found 78! Return row pointer

    Total comparisons: 3 (instead of scanning all rows)
```

#### B-Tree Properties
- **Balanced** - All leaf nodes are at the same depth
- **Sorted** - Values are stored in order
- **Efficient** - O(log n) for search, insert, delete
- **Range-friendly** - Excellent for range queries (BETWEEN, <, >)

```sql
-- Example 1: Creating a B-Tree Index
CREATE INDEX idx_customers_city ON customers(city);

-- B-tree indexes are default in most databases
CREATE INDEX idx_orders_date ON orders(order_date);

-- Verify index creation
SHOW INDEX FROM customers;
```

---

### Hash Indexes

Hash indexes use a hash function to map keys to bucket locations. They're optimized for exact-match lookups only.

#### Hash Index Structure

```
                    HASH INDEX STRUCTURE

    Input Value → Hash Function → Bucket Location

    +------------+     +----------+     +--------+
    | "Smith"    | →   | hash()   | →   | Bucket |
    +------------+     +----------+     |   3    |
                                        +--------+

    Hash Table:
    +--------+------------------+
    | Bucket | Values           |
    +--------+------------------+
    |   0    | ["Brown" → Row4] |
    |   1    | (empty)          |
    |   2    | ["Jones" → Row5] |
    |   3    | ["Smith" → Row1] |
    |   4    | ["Williams"→Row3]|
    |   5    | ["Johnson"→Row2] |
    +--------+------------------+

    Lookup: O(1) average case

    LIMITATION: Cannot do range queries!
    WHERE name > 'M'  ← Hash index CANNOT help here
```

```sql
-- Example 2: Creating a Hash Index (MySQL syntax)
-- Note: InnoDB converts HASH to B-tree; MEMORY engine supports true hash
CREATE TABLE session_data (
    session_id VARCHAR(64) PRIMARY KEY,
    user_id INT,
    data TEXT,
    created_at DATETIME,
    INDEX USING HASH (session_id)
) ENGINE=MEMORY;

-- PostgreSQL hash index
-- CREATE INDEX idx_session_hash ON session_data USING HASH (session_id);
```

#### Hash vs B-Tree Comparison

| Feature | B-Tree | Hash |
|---------|--------|------|
| Exact match (=) | Good | Excellent |
| Range queries (<, >, BETWEEN) | Excellent | Cannot use |
| Pattern matching (LIKE 'abc%') | Good | Cannot use |
| ORDER BY | Can use | Cannot use |
| Disk-based | Yes | Limited |

---

### Composite Indexes

Composite (multi-column) indexes include multiple columns and are crucial for queries filtering on multiple conditions.

#### Composite Index Structure

```
            COMPOSITE INDEX ON (last_name, first_name, city)

    +------------+------------+----------+--------+
    | last_name  | first_name | city     | RowPtr |
    +------------+------------+----------+--------+
    | Brown      | Emily      | Houston  | → Row4 |
    | Johnson    | Sarah      | LA       | → Row2 |
    | Jones      | David      | Phoenix  | → Row5 |
    | Smith      | John       | NYC      | → Row1 |
    | Williams   | Michael    | Chicago  | → Row3 |
    +------------+------------+----------+--------+

    ↑ Sorted by last_name first, then first_name, then city


    THE LEFTMOST PREFIX RULE:

    Index: (A, B, C)

    ✓ Can use index for:          ✗ Cannot use index for:
      WHERE A = ?                    WHERE B = ?
      WHERE A = ? AND B = ?          WHERE C = ?
      WHERE A = ? AND B = ? AND C    WHERE B = ? AND C = ?
      WHERE A = ? AND C = ?
      (uses A only)
```

```sql
-- Example 3: Creating and Using Composite Indexes
-- Create composite index for common query pattern
CREATE INDEX idx_orders_customer_date ON orders(customer_id, order_date);

-- This query FULLY uses the index
SELECT * FROM orders
WHERE customer_id = 1 AND order_date > '2024-01-01';

-- This query uses the index (leftmost column)
SELECT * FROM orders WHERE customer_id = 1;

-- This query CANNOT use the index (missing leftmost column)
SELECT * FROM orders WHERE order_date > '2024-01-01';

-- Create index for filtering and sorting
CREATE INDEX idx_products_category_price
ON products(category, price);

-- Efficient: uses index for both WHERE and ORDER BY
SELECT * FROM products
WHERE category = 'Electronics'
ORDER BY price;
```

---

### Covering Indexes

A covering index contains all the columns needed for a query, allowing the database to satisfy the query entirely from the index without accessing the table.

#### Covering Index Concept

```
            COVERING INDEX VISUALIZATION

    Query: SELECT email, last_login FROM customers WHERE city = 'NYC'


    Without Covering Index:          With Covering Index:

    Step 1: Search index             Step 1: Search index
            ↓                                ↓
    Step 2: Get row pointer          DONE! All data in index
            ↓
    Step 3: Fetch from table         (No table access needed)
            ↓
    DONE


    Covering Index Structure:
    +----------+------------------+---------------------+--------+
    | city     | email            | last_login          | RowPtr |
    +----------+------------------+---------------------+--------+
    | Chicago  | mike.w@email.com | 2024-02-28 16:30:00 | → Row3 |
    | Houston  | emily.b@email.com| 2024-03-01 09:10:00 | → Row4 |
    | LA       | sarah.j@email.com| 2024-03-12 08:45:00 | → Row2 |
    | NYC      | john.smith@...   | 2024-03-10 14:22:00 | → Row1 |
    | Phoenix  | david.jones@...  | 2024-03-15 12:00:00 | → Row5 |
    +----------+------------------+---------------------+--------+

    Query reads ONLY from index - much faster!
```

```sql
-- Example 4: Creating Covering Indexes
-- Index that covers: SELECT email, phone FROM customers WHERE city = ?
CREATE INDEX idx_customers_city_covering
ON customers(city, email, phone);

-- Query uses covering index (no table lookup)
SELECT email, phone
FROM customers
WHERE city = 'New York';

-- EXPLAIN will show "Using index" in Extra column

-- Another covering index example
CREATE INDEX idx_orders_status_covering
ON orders(status, order_date, total_amount);

-- Covered query
SELECT order_date, total_amount
FROM orders
WHERE status = 'pending';
```

---

### When to Use/Not Use Indexes

#### When TO Create Indexes

| Scenario | Reason |
|----------|--------|
| Primary keys | Automatic, ensures uniqueness |
| Foreign keys | Speed up JOINs |
| Columns in WHERE clauses | Faster filtering |
| Columns in ORDER BY | Avoid sorting |
| Columns in GROUP BY | Faster grouping |
| High-cardinality columns | More selective |

#### When NOT TO Create Indexes

| Scenario | Reason |
|----------|--------|
| Small tables (<1000 rows) | Full scan is fast enough |
| Columns rarely queried | Wasted storage |
| Low-cardinality columns | Not selective (e.g., boolean) |
| Frequently updated columns | Index maintenance overhead |
| Tables with heavy INSERT/UPDATE/DELETE | Write performance penalty |

#### Index Trade-offs Diagram

```
                INDEX TRADE-OFFS

    Benefits                    Costs
    +------------------+        +------------------+
    | Faster SELECT    |        | Slower INSERT    |
    | Faster WHERE     |   vs   | Slower UPDATE    |
    | Faster JOIN      |        | Slower DELETE    |
    | Faster ORDER BY  |        | Storage space    |
    +------------------+        +------------------+


    Rule of Thumb:
    +--------------------------------------------------------+
    |                                                        |
    |   Read-Heavy Workload  →  More indexes                 |
    |   Write-Heavy Workload →  Fewer, strategic indexes     |
    |                                                        |
    +--------------------------------------------------------+
```

---

## 2. EXPLAIN and Query Execution Plans

### Reading EXPLAIN Output

EXPLAIN shows how the database executes a query, revealing opportunities for optimization.

```sql
-- Example 5: Using EXPLAIN to Analyze Queries
EXPLAIN SELECT * FROM customers WHERE city = 'New York';
```

**Sample Output:**
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra |
|----|-------------|-------|------|---------------|-----|---------|-----|------|-------|
| 1 | SIMPLE | customers | ref | idx_city | idx_city | 203 | const | 1 | NULL |

#### EXPLAIN Output Breakdown

```
                EXPLAIN OUTPUT ANATOMY

+----------------------------------------------------------------------+
|                         EXPLAIN Result                                |
+----------------------------------------------------------------------+
| id: 1                    ← Query identifier (for subqueries)         |
| select_type: SIMPLE      ← Query type (SIMPLE, SUBQUERY, UNION, etc) |
| table: customers         ← Table being accessed                       |
| type: ref                ← Access type (CRITICAL for performance)     |
| possible_keys: idx_city  ← Indexes that could be used                 |
| key: idx_city            ← Index actually chosen                      |
| key_len: 203             ← Bytes of index used                        |
| ref: const               ← What's compared to the index               |
| rows: 1                  ← Estimated rows to examine                  |
| Extra: Using index       ← Additional information                     |
+----------------------------------------------------------------------+
```

---

### Key Metrics

#### Access Types (Best to Worst)

```
            ACCESS TYPE PERFORMANCE SCALE

    BEST ←─────────────────────────────────→ WORST

    system  const  eq_ref  ref  range  index  ALL
      ↓       ↓       ↓     ↓     ↓      ↓     ↓
      │       │       │     │     │      │     │
    Single  PK/UQ  Join   Non-  Range  Full  Full
    row     lookup  on    unique scan   index table
                    PK    index        scan  scan


    Examples:
    +----------+----------------------------------------+
    | Type     | Example Query                          |
    +----------+----------------------------------------+
    | const    | WHERE id = 1 (PK lookup)               |
    | eq_ref   | JOIN on primary key                    |
    | ref      | WHERE status = 'active' (indexed)      |
    | range    | WHERE date BETWEEN '2024-01-01' AND... |
    | index    | SELECT indexed_col FROM table          |
    | ALL      | SELECT * FROM table (no index used)    |
    +----------+----------------------------------------+
```

#### Important Extra Values

| Value | Meaning | Action |
|-------|---------|--------|
| Using index | Covering index (good!) | Keep as is |
| Using where | Filtering after index | Consider better index |
| Using filesort | Sorting without index | Add index for ORDER BY |
| Using temporary | Temp table needed | Optimize GROUP BY/DISTINCT |
| Using index condition | Index condition pushdown | Good optimization |

---

### Identifying Bottlenecks

```sql
-- Example 6: Analyzing a Slow Query
-- Bad query (no index on order_date)
EXPLAIN SELECT c.first_name, c.last_name, o.order_date, o.total_amount
FROM customers c
JOIN orders o ON c.customer_id = o.customer_id
WHERE YEAR(o.order_date) = 2024
ORDER BY o.total_amount DESC;
```

**Problem indicators:**
- `type: ALL` - Full table scan
- `rows: 10000` - Examining many rows
- `Extra: Using filesort` - Expensive sorting
- `Extra: Using temporary` - Temp table created

```
            BOTTLENECK IDENTIFICATION FLOWCHART

                    Start EXPLAIN
                          ↓
                    type = ALL?
                     /        \
                   Yes         No
                   ↓           ↓
              Add index    Check rows
                          /          \
                     High rows    Low rows
                        ↓            ↓
                  Better index     OK!
                        ↓
                  Using filesort?
                     /        \
                   Yes         No
                   ↓           ↓
            Add index for    Check Extra
            ORDER BY         for issues
```

---

## 3. Query Optimization Techniques

### SELECT Only Needed Columns

#### WHY Avoid SELECT *
- Transfers unnecessary data over the network
- Consumes more memory
- Prevents covering index usage
- Can break application if schema changes

```sql
-- Example 7: Selecting Only Needed Columns

-- BAD: Selecting all columns
SELECT * FROM customers WHERE city = 'New York';
-- Transfers: customer_id, first_name, last_name, email, phone,
--            city, state, country, created_at, last_login, status

-- GOOD: Select only what you need
SELECT customer_id, first_name, email
FROM customers
WHERE city = 'New York';
-- Transfers: Just 3 columns

-- With covering index, this is even faster:
CREATE INDEX idx_city_email_name ON customers(city, email, first_name);
```

---

### Proper JOIN Order

The database optimizer usually handles this, but understanding it helps write better queries.

```sql
-- Example 8: Optimizing JOIN Order

-- Let the optimizer choose (usually best)
SELECT c.first_name, o.order_date, oi.quantity, p.product_name
FROM customers c
JOIN orders o ON c.customer_id = o.customer_id
JOIN order_items oi ON o.order_id = oi.order_id
JOIN products p ON oi.product_id = p.product_id
WHERE c.city = 'New York';

-- When to hint: if optimizer makes wrong choice
-- STRAIGHT_JOIN forces left-to-right order (MySQL)
SELECT STRAIGHT_JOIN c.first_name, o.order_date
FROM customers c
JOIN orders o ON c.customer_id = o.customer_id
WHERE c.city = 'New York';
```

#### JOIN Optimization Guidelines

```
            JOIN ORDER STRATEGY

    1. Start with the most filtered table
       (smallest result set after WHERE)

    2. Join to tables in order of selectivity

    3. Ensure JOIN columns are indexed


    Example Analysis:

    customers (5 rows after WHERE city='NYC')
         ↓
    orders (1000 total, but only ~10 for this customer)
         ↓
    order_items (5000 total, but only ~30 for these orders)
         ↓
    products (100 total, need only ~10)

    Result: 5 → 10 → 30 → 10 rows examined
```

---

### Avoiding SELECT *

```sql
-- Problems with SELECT *

-- Problem 1: Breaks when schema changes
SELECT * FROM customers;  -- What if new column added?

-- Problem 2: No covering indexes
SELECT * FROM customers WHERE city = 'NYC';
-- Must read entire row from table

-- Problem 3: Unnecessary data in subqueries
SELECT * FROM customers
WHERE customer_id IN (SELECT customer_id FROM orders WHERE total_amount > 1000);
-- Only need customer_id from subquery!

-- BETTER:
SELECT c.customer_id, c.first_name, c.email
FROM customers c
WHERE c.customer_id IN (
    SELECT o.customer_id FROM orders o WHERE o.total_amount > 1000
);
```

---

### Optimizing WHERE Clauses

```sql
-- Example 9: WHERE Clause Optimization

-- Use specific conditions first (most selective)
-- BAD: Less selective condition first
SELECT * FROM orders
WHERE status = 'active'           -- Many rows
  AND customer_id = 1;            -- Few rows

-- GOOD: More selective condition first
SELECT * FROM orders
WHERE customer_id = 1             -- Few rows
  AND status = 'active';          -- Filter remaining

-- Use IN instead of multiple ORs
-- BAD:
SELECT * FROM products
WHERE category = 'Electronics'
   OR category = 'Accessories'
   OR category = 'Office';

-- GOOD:
SELECT * FROM products
WHERE category IN ('Electronics', 'Accessories', 'Office');

-- Avoid NOT IN with NULLs
-- BAD: Returns unexpected results if subquery has NULLs
SELECT * FROM customers
WHERE customer_id NOT IN (SELECT customer_id FROM orders);

-- GOOD: Use NOT EXISTS instead
SELECT * FROM customers c
WHERE NOT EXISTS (
    SELECT 1 FROM orders o WHERE o.customer_id = c.customer_id
);
```

---

### Avoiding Functions on Indexed Columns

When you apply a function to an indexed column, the database cannot use the index.

```sql
-- Example 10: Functions on Indexed Columns

-- BAD: Function prevents index usage
SELECT * FROM orders
WHERE YEAR(order_date) = 2024;

-- GOOD: Rewrite to use index
SELECT * FROM orders
WHERE order_date >= '2024-01-01'
  AND order_date < '2025-01-01';

-- BAD: UPPER() prevents index usage
SELECT * FROM customers
WHERE UPPER(email) = 'JOHN.SMITH@EMAIL.COM';

-- GOOD: Store normalized data or use functional index
SELECT * FROM customers
WHERE email = 'john.smith@email.com';

-- Alternative: Create functional index (MySQL 8.0+)
CREATE INDEX idx_email_upper ON customers((UPPER(email)));

-- BAD: Calculation on indexed column
SELECT * FROM products
WHERE price * 1.1 > 100;

-- GOOD: Move calculation to the other side
SELECT * FROM products
WHERE price > 100 / 1.1;
```

---

## 4. Transaction Management

### ACID Properties

```
                    ACID PROPERTIES

+-------------------+----------------------------------------+
|    Atomicity      |    All or Nothing                      |
|       (A)         |    Transaction completes fully or      |
|                   |    not at all - no partial updates     |
+-------------------+----------------------------------------+
|    Consistency    |    Valid State to Valid State          |
|       (C)         |    Database constraints are always     |
|                   |    maintained (FK, unique, etc.)       |
+-------------------+----------------------------------------+
|    Isolation      |    Transactions Don't Interfere        |
|       (I)         |    Concurrent transactions appear      |
|                   |    to run sequentially                 |
+-------------------+----------------------------------------+
|    Durability     |    Committed = Permanent               |
|       (D)         |    Once committed, data survives       |
|                   |    crashes and power failures          |
+-------------------+----------------------------------------+


    ACID in Action - Bank Transfer Example:

    Transfer $100 from Account A to Account B

    1. Atomicity: Both debit AND credit must succeed
    2. Consistency: Total money remains the same
    3. Isolation: Other queries see old OR new state, not middle
    4. Durability: After COMMIT, transfer is permanent
```

---

### BEGIN, COMMIT, ROLLBACK

```sql
-- Example 11: Basic Transaction Control

-- Start a transaction
START TRANSACTION;  -- or BEGIN

-- Perform operations
UPDATE customers SET status = 'inactive' WHERE customer_id = 4;
UPDATE orders SET status = 'cancelled' WHERE customer_id = 4;

-- If everything is OK, commit
COMMIT;

-- OR if something went wrong, rollback
ROLLBACK;


-- Practical Example: Transferring inventory between products
START TRANSACTION;

-- Check current stock
SELECT product_id, stock_quantity FROM products WHERE product_id IN (101, 102);

-- Reduce stock from product 101
UPDATE products SET stock_quantity = stock_quantity - 10 WHERE product_id = 101;

-- Add stock to product 102
UPDATE products SET stock_quantity = stock_quantity + 10 WHERE product_id = 102;

-- Verify the transfer
SELECT product_id, stock_quantity FROM products WHERE product_id IN (101, 102);

-- Commit if successful
COMMIT;
```

#### SAVEPOINT for Partial Rollback

```sql
-- Using SAVEPOINTs
START TRANSACTION;

INSERT INTO customers (customer_id, first_name, last_name, email, city, state, country, created_at, status)
VALUES (6, 'Test', 'User', 'test@email.com', 'Miami', 'FL', 'USA', NOW(), 'active');

SAVEPOINT after_customer;

INSERT INTO orders (order_id, customer_id, order_date, total_amount, status)
VALUES (1006, 6, NOW(), 100.00, 'pending');

-- Oops, need to undo the order but keep the customer
ROLLBACK TO after_customer;

-- Continue with different order
INSERT INTO orders (order_id, customer_id, order_date, total_amount, status)
VALUES (1006, 6, NOW(), 150.00, 'pending');

COMMIT;
```

---

### Isolation Levels

```
            TRANSACTION ISOLATION LEVELS

    Level               | Dirty | Non-Repeatable | Phantom
                        | Read  | Read           | Read
    --------------------+-------+----------------+---------
    READ UNCOMMITTED    |  Yes  |      Yes       |   Yes
    READ COMMITTED      |  No   |      Yes       |   Yes
    REPEATABLE READ     |  No   |      No        |   Yes
    SERIALIZABLE        |  No   |      No        |   No


    ↑ Increasing Isolation (Safer but Slower) ↑
    ↓ Increasing Performance (Faster but Riskier) ↓


    Problem Definitions:

    Dirty Read:       Reading uncommitted data from another transaction
    Non-Repeatable:   Same query returns different results within transaction
    Phantom Read:     New rows appear in repeated query due to other inserts
```

```sql
-- Set isolation level
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;

-- Or for the session
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- Check current level (MySQL)
SELECT @@transaction_isolation;
```

#### Isolation Level Examples

```sql
-- READ COMMITTED Example (default in PostgreSQL, Oracle)
-- Session 1:
START TRANSACTION;
SELECT balance FROM accounts WHERE id = 1;  -- Returns 1000
-- Session 2 updates balance to 500 and COMMITs
SELECT balance FROM accounts WHERE id = 1;  -- Returns 500 (non-repeatable read)
COMMIT;

-- REPEATABLE READ Example (default in MySQL)
-- Session 1:
START TRANSACTION;
SELECT balance FROM accounts WHERE id = 1;  -- Returns 1000
-- Session 2 updates balance to 500 and COMMITs
SELECT balance FROM accounts WHERE id = 1;  -- Still returns 1000!
COMMIT;
```

---

### Deadlocks

A deadlock occurs when two transactions are waiting for each other to release locks.

```
            DEADLOCK VISUALIZATION

    Transaction 1              Transaction 2
         │                          │
         ↓                          ↓
    Lock Row A                 Lock Row B
         │                          │
         ↓                          ↓
    Try Lock Row B ←────────→ Try Lock Row A
         │        WAITING           │
         │        FOR EACH          │
         ↓        OTHER!            ↓
      BLOCKED                    BLOCKED
         │                          │
         └────────DEADLOCK!─────────┘


    Resolution: Database detects deadlock and rolls back one transaction
```

#### Preventing Deadlocks

```sql
-- Strategy 1: Consistent lock ordering
-- Always lock tables/rows in the same order

-- BAD: Different order causes deadlock
-- Transaction 1: Lock A then B
-- Transaction 2: Lock B then A

-- GOOD: Same order
-- Transaction 1: Lock A then B
-- Transaction 2: Lock A then B

-- Strategy 2: Keep transactions short
START TRANSACTION;
-- Do work quickly
COMMIT;

-- Strategy 3: Use lower isolation levels when possible
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- Strategy 4: Use NOWAIT or SKIP LOCKED (MySQL 8.0+)
SELECT * FROM products WHERE product_id = 101 FOR UPDATE NOWAIT;
-- Fails immediately if locked, instead of waiting

SELECT * FROM products WHERE stock_quantity > 0
FOR UPDATE SKIP LOCKED LIMIT 10;
-- Skips locked rows, gets only available ones
```

---

## 5. Normalization

Normalization is the process of organizing data to reduce redundancy and improve data integrity.

### Normalization Process Flow

```
            NORMALIZATION JOURNEY

    Unnormalized Data
           │
           ↓
    ┌─────────────┐
    │     1NF     │  Eliminate repeating groups
    │             │  Atomic values only
    └──────┬──────┘
           │
           ↓
    ┌─────────────┐
    │     2NF     │  Remove partial dependencies
    │             │  Full functional dependency on PK
    └──────┬──────┘
           │
           ↓
    ┌─────────────┐
    │     3NF     │  Remove transitive dependencies
    │             │  No non-key → non-key dependencies
    └──────┬──────┘
           │
           ↓
    ┌─────────────┐
    │    BCNF     │  Every determinant is a candidate key
    │             │  Stricter version of 3NF
    └─────────────┘
```

---

### First Normal Form (1NF)

**Rules:**
1. Each cell contains a single (atomic) value
2. Each row is unique (has a primary key)
3. No repeating groups

```
            1NF TRANSFORMATION EXAMPLE

    BEFORE (Not 1NF):
    +----------+------------------+---------------------------+
    | order_id | customer         | products                  |
    +----------+------------------+---------------------------+
    | 1001     | John Smith       | Laptop, Mouse, USB Hub    |
    | 1002     | Sarah Johnson    | Monitor, Mouse            |
    +----------+------------------+---------------------------+

    Problems:
    - products column has multiple values (not atomic)
    - How to query "all orders with Mouse"?


    AFTER (1NF):
    +----------+---------------+------------+
    | order_id | customer      | product    |
    +----------+---------------+------------+
    | 1001     | John Smith    | Laptop     |
    | 1001     | John Smith    | Mouse      |
    | 1001     | John Smith    | USB Hub    |
    | 1002     | Sarah Johnson | Monitor    |
    | 1002     | Sarah Johnson | Mouse      |
    +----------+---------------+------------+

    Now we can: SELECT * FROM orders WHERE product = 'Mouse';
```

---

### Second Normal Form (2NF)

**Rules:**
1. Must be in 1NF
2. All non-key columns must depend on the ENTIRE primary key (no partial dependencies)

```
            2NF TRANSFORMATION EXAMPLE

    BEFORE (1NF but not 2NF):
    Primary Key: (order_id, product_id)

    +----------+------------+---------------+-------------+------------+
    | order_id | product_id | customer_name | product_name| quantity   |
    +----------+------------+---------------+-------------+------------+
    | 1001     | 101        | John Smith    | Laptop      | 1          |
    | 1001     | 102        | John Smith    | Mouse       | 2          |
    | 1002     | 104        | Sarah Johnson | Monitor     | 1          |
    +----------+------------+---------------+-------------+------------+

    Problems:
    - customer_name depends only on order_id (partial dependency)
    - product_name depends only on product_id (partial dependency)


    AFTER (2NF) - Split into tables:

    Orders:                    Products:              Order_Items:
    +----------+-------------+ +------------+---------+ +----------+------------+----------+
    | order_id | customer_   | | product_id | product | | order_id | product_id | quantity |
    |          | name        | |            | _name   | |          |            |          |
    +----------+-------------+ +------------+---------+ +----------+------------+----------+
    | 1001     | John Smith  | | 101        | Laptop  | | 1001     | 101        | 1        |
    | 1002     | Sarah       | | 102        | Mouse   | | 1001     | 102        | 2        |
    |          | Johnson     | | 104        | Monitor | | 1002     | 104        | 1        |
    +----------+-------------+ +------------+---------+ +----------+------------+----------+

    Now each non-key attribute depends on the WHOLE key of its table
```

---

### Third Normal Form (3NF)

**Rules:**
1. Must be in 2NF
2. No transitive dependencies (non-key column depending on another non-key column)

```
            3NF TRANSFORMATION EXAMPLE

    BEFORE (2NF but not 3NF):

    +-------------+---------------+----------+---------------+
    | customer_id | customer_name | zip_code | city          |
    +-------------+---------------+----------+---------------+
    | 1           | John Smith    | 10001    | New York      |
    | 2           | Sarah Johnson | 90001    | Los Angeles   |
    | 3           | Mike Williams | 10001    | New York      |
    +-------------+---------------+----------+---------------+

    Problem:
    - city depends on zip_code (transitive dependency)
    - customer_id → zip_code → city
    - city is stored redundantly


    AFTER (3NF) - Split into tables:

    Customers:                          Zip_Codes:
    +-------------+---------------+----------+    +----------+-------------+
    | customer_id | customer_name | zip_code |    | zip_code | city        |
    +-------------+---------------+----------+    +----------+-------------+
    | 1           | John Smith    | 10001    |    | 10001    | New York    |
    | 2           | Sarah Johnson | 90001    |    | 90001    | Los Angeles |
    | 3           | Mike Williams | 10001    |    +----------+-------------+
    +-------------+---------------+----------+

    Benefits:
    - Update city name in one place
    - No redundant storage
    - Data consistency guaranteed
```

---

### Boyce-Codd Normal Form (BCNF)

**Rules:**
1. Must be in 3NF
2. For every functional dependency X → Y, X must be a superkey

BCNF is a stricter version of 3NF that handles certain edge cases.

```
            BCNF TRANSFORMATION EXAMPLE

    BEFORE (3NF but not BCNF):

    Student_Courses (Candidate keys: {student, course} and {student, instructor})

    +----------+----------+------------+
    | student  | course   | instructor |
    +----------+----------+------------+
    | John     | Math     | Dr. Smith  |
    | John     | Physics  | Dr. Brown  |
    | Sarah    | Math     | Dr. Smith  |
    | Sarah    | Chemistry| Dr. White  |
    +----------+----------+------------+

    Functional Dependencies:
    - {student, course} → instructor
    - instructor → course (each instructor teaches one course)

    Problem: instructor → course, but instructor is NOT a superkey


    AFTER (BCNF):

    Student_Instructor:              Instructor_Course:
    +----------+------------+        +------------+----------+
    | student  | instructor |        | instructor | course   |
    +----------+------------+        +------------+----------+
    | John     | Dr. Smith  |        | Dr. Smith  | Math     |
    | John     | Dr. Brown  |        | Dr. Brown  | Physics  |
    | Sarah    | Dr. Smith  |        | Dr. White  | Chemistry|
    | Sarah    | Dr. White  |        +------------+----------+
    +----------+------------+

    Now all determinants are superkeys
```

---

### When to Denormalize

Sometimes breaking normalization rules improves performance.

```
            DENORMALIZATION DECISION MATRIX

    +------------------+-------------------+----------------------+
    | Scenario         | Normalize?        | Reason               |
    +------------------+-------------------+----------------------+
    | OLTP (many       | YES               | Data integrity,      |
    | writes)          |                   | avoid update anomaly |
    +------------------+-------------------+----------------------+
    | OLAP (analytics, | Often NO          | Fewer JOINs = faster |
    | many reads)      | (denormalize)     | queries              |
    +------------------+-------------------+----------------------+
    | Reporting tables | NO (denormalize)  | Pre-join for speed   |
    +------------------+-------------------+----------------------+
    | Frequently       | NO (denormalize)  | Cache expensive      |
    | calculated       |                   | calculations         |
    | aggregates       |                   |                      |
    +------------------+-------------------+----------------------+


    Common Denormalization Techniques:

    1. Add calculated columns
       - Store total_amount instead of calculating from items

    2. Add redundant columns
       - Store customer_name in orders table

    3. Create summary tables
       - Daily/monthly aggregates

    4. Use materialized views
       - Pre-computed query results
```

```sql
-- Example: Denormalized reporting table
CREATE TABLE order_summary (
    order_id INT PRIMARY KEY,
    customer_id INT,
    customer_name VARCHAR(100),      -- Denormalized!
    customer_email VARCHAR(100),     -- Denormalized!
    order_date DATETIME,
    item_count INT,                  -- Pre-calculated!
    subtotal DECIMAL(12,2),          -- Pre-calculated!
    tax_amount DECIMAL(10,2),
    total_amount DECIMAL(12,2),
    INDEX idx_customer (customer_id),
    INDEX idx_date (order_date)
);

-- Faster reporting query (no JOINs needed)
SELECT customer_name, SUM(total_amount), COUNT(*)
FROM order_summary
WHERE order_date >= '2024-01-01'
GROUP BY customer_name;
```

---

## 6. Code Examples

### Example 12: Complete Index Strategy for an E-commerce Database

```sql
-- Primary indexes (automatically created)
-- Already have: customers.customer_id, products.product_id, orders.order_id

-- Foreign key indexes (critical for JOINs)
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- Common query pattern indexes
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_city_status ON customers(city, status);
CREATE INDEX idx_orders_date_status ON orders(order_date, status);
CREATE INDEX idx_products_category_price ON products(category, price);

-- Covering index for frequent report
CREATE INDEX idx_orders_report ON orders(status, order_date, customer_id, total_amount);

-- Check all indexes on a table
SHOW INDEX FROM orders;

-- Drop unused index
DROP INDEX idx_unused ON some_table;
```

### Example 13: Query Analysis and Optimization

```sql
-- Original slow query
SELECT
    c.first_name,
    c.last_name,
    c.email,
    COUNT(o.order_id) as order_count,
    SUM(o.total_amount) as total_spent
FROM customers c
LEFT JOIN orders o ON c.customer_id = o.customer_id
WHERE c.status = 'active'
  AND c.created_at >= '2022-01-01'
GROUP BY c.customer_id
HAVING SUM(o.total_amount) > 1000
ORDER BY total_spent DESC;

-- Step 1: Analyze with EXPLAIN
EXPLAIN SELECT ...;

-- Step 2: Create supporting indexes
CREATE INDEX idx_customers_status_created ON customers(status, created_at);
CREATE INDEX idx_orders_customer_total ON orders(customer_id, total_amount);

-- Step 3: Rewrite for better performance
SELECT
    c.first_name,
    c.last_name,
    c.email,
    order_stats.order_count,
    order_stats.total_spent
FROM customers c
INNER JOIN (
    SELECT
        customer_id,
        COUNT(*) as order_count,
        SUM(total_amount) as total_spent
    FROM orders
    GROUP BY customer_id
    HAVING SUM(total_amount) > 1000
) order_stats ON c.customer_id = order_stats.customer_id
WHERE c.status = 'active'
  AND c.created_at >= '2022-01-01'
ORDER BY order_stats.total_spent DESC;
```

### Example 14: Transaction with Error Handling

```sql
-- Using procedures for transaction management (MySQL)
DELIMITER //

CREATE PROCEDURE transfer_inventory(
    IN from_product INT,
    IN to_product INT,
    IN quantity INT
)
BEGIN
    DECLARE current_stock INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'Error: Transaction rolled back' as message;
    END;

    START TRANSACTION;

    -- Check if source has enough stock
    SELECT stock_quantity INTO current_stock
    FROM products
    WHERE product_id = from_product
    FOR UPDATE;  -- Lock the row

    IF current_stock < quantity THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Insufficient stock';
    END IF;

    -- Perform transfer
    UPDATE products
    SET stock_quantity = stock_quantity - quantity
    WHERE product_id = from_product;

    UPDATE products
    SET stock_quantity = stock_quantity + quantity
    WHERE product_id = to_product;

    -- Log the transfer
    INSERT INTO inventory_log (product_id, change_type, quantity_change, change_date, notes)
    VALUES
        (from_product, 'transfer_out', -quantity, NOW(), CONCAT('To product ', to_product)),
        (to_product, 'transfer_in', quantity, NOW(), CONCAT('From product ', from_product));

    COMMIT;
    SELECT 'Transfer successful' as message;
END //

DELIMITER ;

-- Call the procedure
CALL transfer_inventory(101, 102, 5);
```

### Example 15: Performance Comparison Query

```sql
-- Compare performance of different query approaches

-- Approach 1: Subquery in WHERE
SELECT * FROM customers
WHERE customer_id IN (
    SELECT customer_id FROM orders
    WHERE total_amount > 1000
);

-- Approach 2: JOIN with DISTINCT
SELECT DISTINCT c.*
FROM customers c
JOIN orders o ON c.customer_id = o.customer_id
WHERE o.total_amount > 1000;

-- Approach 3: EXISTS (often fastest)
SELECT * FROM customers c
WHERE EXISTS (
    SELECT 1 FROM orders o
    WHERE o.customer_id = c.customer_id
    AND o.total_amount > 1000
);

-- Use EXPLAIN to compare:
EXPLAIN SELECT * FROM customers WHERE customer_id IN (...);
EXPLAIN SELECT DISTINCT c.* FROM customers c JOIN ...;
EXPLAIN SELECT * FROM customers c WHERE EXISTS (...);
```

---

## 7. Interview Questions

### Question 1: Optimizing a Slow Report Query

**Scenario:** A daily sales report is taking 5 minutes to run. The query joins 4 tables and aggregates data by product category and date.

```sql
-- The slow query
SELECT
    p.category,
    DATE(o.order_date) as sale_date,
    COUNT(DISTINCT o.order_id) as order_count,
    SUM(oi.quantity) as items_sold,
    SUM(oi.quantity * oi.unit_price) as revenue
FROM products p
JOIN order_items oi ON p.product_id = oi.product_id
JOIN orders o ON oi.order_id = o.order_id
JOIN customers c ON o.customer_id = c.customer_id
WHERE o.order_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
  AND c.country = 'USA'
GROUP BY p.category, DATE(o.order_date)
ORDER BY sale_date DESC, revenue DESC;
```

**Solution:**

```sql
-- Step 1: Analyze with EXPLAIN
EXPLAIN SELECT ...;

-- Step 2: Identify issues
-- - Missing indexes on join columns
-- - DATE() function prevents index use on order_date
-- - Unnecessary join with customers if only filtering

-- Step 3: Create appropriate indexes
CREATE INDEX idx_orders_date ON orders(order_date);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_oi_order ON order_items(order_id);
CREATE INDEX idx_oi_product ON order_items(product_id);
CREATE INDEX idx_customers_country ON customers(country);

-- Step 4: Optimize the query
SELECT
    p.category,
    DATE(o.order_date) as sale_date,
    COUNT(DISTINCT o.order_id) as order_count,
    SUM(oi.quantity) as items_sold,
    SUM(oi.quantity * oi.unit_price) as revenue
FROM orders o
INNER JOIN customers c ON o.customer_id = c.customer_id AND c.country = 'USA'
INNER JOIN order_items oi ON o.order_id = oi.order_id
INNER JOIN products p ON oi.product_id = p.product_id
WHERE o.order_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
  AND o.order_date < CURDATE() + INTERVAL 1 DAY
GROUP BY p.category, DATE(o.order_date)
ORDER BY sale_date DESC, revenue DESC;

-- Step 5: Consider creating a summary table for frequent access
CREATE TABLE daily_category_sales (
    sale_date DATE,
    category VARCHAR(50),
    order_count INT,
    items_sold INT,
    revenue DECIMAL(12,2),
    PRIMARY KEY (sale_date, category),
    INDEX idx_date (sale_date)
);

-- Populate nightly via scheduled job
INSERT INTO daily_category_sales
SELECT DATE(o.order_date), p.category, ...
FROM ...
ON DUPLICATE KEY UPDATE ...;
```

---

### Question 2: Designing Indexes for a Multi-Tenant Application

**Scenario:** You have a SaaS application where every query filters by tenant_id. How would you design indexes?

**Solution:**

```sql
-- All queries will include tenant_id, so it should be first in composite indexes

-- Table structure
CREATE TABLE tenant_data (
    id INT AUTO_INCREMENT,
    tenant_id INT NOT NULL,
    data_type VARCHAR(50),
    created_at DATETIME,
    status VARCHAR(20),
    value TEXT,
    PRIMARY KEY (id),
    -- tenant_id first in all indexes!
    INDEX idx_tenant_type (tenant_id, data_type),
    INDEX idx_tenant_date (tenant_id, created_at),
    INDEX idx_tenant_status (tenant_id, status)
);

-- Query patterns will all be efficient:
SELECT * FROM tenant_data
WHERE tenant_id = 123 AND data_type = 'config';

SELECT * FROM tenant_data
WHERE tenant_id = 123
ORDER BY created_at DESC
LIMIT 100;

-- Alternative: Partition by tenant_id for very large datasets
ALTER TABLE tenant_data
PARTITION BY HASH(tenant_id)
PARTITIONS 10;
```

---

### Question 3: Handling Deadlocks in a High-Concurrency System

**Scenario:** Your order processing system frequently encounters deadlocks when multiple transactions try to update inventory simultaneously.

**Solution:**

```sql
-- Problem code (causes deadlocks):
-- Transaction 1: Updates product A then B
-- Transaction 2: Updates product B then A

-- Solution 1: Consistent lock ordering
-- Always lock in product_id order
START TRANSACTION;

-- Get all product IDs we need to update, sorted
SELECT product_id FROM order_items
WHERE order_id = ?
ORDER BY product_id
FOR UPDATE;  -- Lock in consistent order

-- Now update in the same order
UPDATE products SET stock = stock - quantity
WHERE product_id IN (...) ORDER BY product_id;

COMMIT;

-- Solution 2: Use SKIP LOCKED for queue-like processing
START TRANSACTION;

SELECT * FROM orders
WHERE status = 'pending'
LIMIT 1
FOR UPDATE SKIP LOCKED;

-- Process order...

UPDATE orders SET status = 'processing' WHERE order_id = ?;

COMMIT;

-- Solution 3: Optimistic locking with version numbers
-- Add version column to products
ALTER TABLE products ADD COLUMN version INT DEFAULT 0;

-- Read with version
SELECT product_id, stock_quantity, version
FROM products
WHERE product_id = 101;

-- Update only if version matches
UPDATE products
SET stock_quantity = stock_quantity - 10,
    version = version + 1
WHERE product_id = 101
  AND version = @read_version;

-- Check if update succeeded
-- If affected_rows = 0, retry (another transaction modified it)
```

---

### Question 4: Normalizing a Denormalized Legacy Table

**Scenario:** You inherit a legacy table with severe redundancy issues. Normalize it to 3NF.

```sql
-- Legacy table (violations everywhere!)
CREATE TABLE legacy_orders (
    id INT,
    customer_name VARCHAR(100),
    customer_email VARCHAR(100),
    customer_address VARCHAR(200),
    customer_city VARCHAR(50),
    customer_state VARCHAR(2),
    product1_name VARCHAR(100),
    product1_price DECIMAL(10,2),
    product1_qty INT,
    product2_name VARCHAR(100),
    product2_price DECIMAL(10,2),
    product2_qty INT,
    product3_name VARCHAR(100),
    product3_price DECIMAL(10,2),
    product3_qty INT,
    order_date DATE,
    total DECIMAL(10,2)
);

-- Solution: Normalize to 3NF

-- Step 1: Create normalized tables
CREATE TABLE customers_normalized (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    address VARCHAR(200),
    city VARCHAR(50),
    state VARCHAR(2)
);

CREATE TABLE products_normalized (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2)
);

CREATE TABLE orders_normalized (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    order_date DATE,
    FOREIGN KEY (customer_id) REFERENCES customers_normalized(customer_id)
);

CREATE TABLE order_items_normalized (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    product_id INT,
    quantity INT,
    unit_price DECIMAL(10,2),
    FOREIGN KEY (order_id) REFERENCES orders_normalized(order_id),
    FOREIGN KEY (product_id) REFERENCES products_normalized(product_id)
);

-- Step 2: Migrate data
-- Insert customers (deduplicated)
INSERT INTO customers_normalized (name, email, address, city, state)
SELECT DISTINCT customer_name, customer_email, customer_address,
       customer_city, customer_state
FROM legacy_orders;

-- Insert products (deduplicated)
INSERT INTO products_normalized (name, price)
SELECT DISTINCT product1_name, product1_price FROM legacy_orders
WHERE product1_name IS NOT NULL
UNION
SELECT DISTINCT product2_name, product2_price FROM legacy_orders
WHERE product2_name IS NOT NULL
UNION
SELECT DISTINCT product3_name, product3_price FROM legacy_orders
WHERE product3_name IS NOT NULL;

-- Insert orders and order_items...
-- (Complex migration script required)
```

---

### Question 5: Choosing the Right Isolation Level

**Scenario:** You need to implement a banking system with balance transfers and account statements. What isolation levels would you use for different operations?

**Solution:**

```sql
-- Operation 1: Balance Transfer (needs highest consistency)
-- Use SERIALIZABLE to prevent any anomalies
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;

START TRANSACTION;

SELECT balance FROM accounts WHERE id = @from_account FOR UPDATE;
SELECT balance FROM accounts WHERE id = @to_account FOR UPDATE;

-- Check sufficient balance
-- Perform transfer
UPDATE accounts SET balance = balance - @amount WHERE id = @from_account;
UPDATE accounts SET balance = balance + @amount WHERE id = @to_account;

-- Record transaction
INSERT INTO transactions (from_account, to_account, amount, timestamp)
VALUES (@from_account, @to_account, @amount, NOW());

COMMIT;


-- Operation 2: Generate Account Statement (read consistency within statement)
-- Use REPEATABLE READ to ensure consistent view
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;

START TRANSACTION;

SELECT balance FROM accounts WHERE id = @account_id;

SELECT * FROM transactions
WHERE (from_account = @account_id OR to_account = @account_id)
  AND timestamp BETWEEN @start_date AND @end_date
ORDER BY timestamp;

-- All reads see same snapshot even if other transactions commit
COMMIT;


-- Operation 3: Dashboard Statistics (can tolerate slight inconsistency)
-- Use READ COMMITTED for better performance
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;

SELECT
    COUNT(*) as total_accounts,
    SUM(balance) as total_deposits,
    AVG(balance) as avg_balance
FROM accounts;
-- Slight inconsistency OK for dashboard metrics


-- Operation 4: Real-time Balance Check (single read, needs latest)
-- Use READ COMMITTED
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
SELECT balance FROM accounts WHERE id = @account_id;
```

---

## 8. Performance Testing Tips

### Benchmarking Queries

```sql
-- MySQL: Enable profiling
SET profiling = 1;

-- Run your query
SELECT * FROM orders WHERE customer_id = 1;

-- View profile
SHOW PROFILES;
SHOW PROFILE FOR QUERY 1;

-- Detailed timing breakdown
SHOW PROFILE ALL FOR QUERY 1;

-- Reset
SET profiling = 0;
```

### Testing with Realistic Data

```sql
-- Generate test data (MySQL)
-- Create a procedure to insert many rows

DELIMITER //
CREATE PROCEDURE generate_test_orders(IN num_orders INT)
BEGIN
    DECLARE i INT DEFAULT 0;

    WHILE i < num_orders DO
        INSERT INTO orders (customer_id, order_date, total_amount, status, shipping_method)
        VALUES (
            FLOOR(1 + RAND() * 5),  -- Random customer 1-5
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY),
            ROUND(RAND() * 1000, 2),
            ELT(FLOOR(1 + RAND() * 4), 'pending', 'processing', 'shipped', 'delivered'),
            ELT(FLOOR(1 + RAND() * 2), 'Standard', 'Express')
        );
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- Generate 100,000 test orders
CALL generate_test_orders(100000);
```

### Key Performance Metrics to Track

```
            PERFORMANCE METRICS CHECKLIST

    +------------------+-------------+------------------+
    | Metric           | Good        | Needs Work       |
    +------------------+-------------+------------------+
    | Query time       | < 100ms     | > 1000ms         |
    | Rows examined    | ~= rows     | >> rows returned |
    |                  | returned    |                  |
    +------------------+-------------+------------------+
    | Index usage      | Using key   | NULL key         |
    | Table scans      | 0           | > 0 on large     |
    |                  |             | tables           |
    +------------------+-------------+------------------+
    | Temp tables      | 0           | > 0              |
    | Filesort         | 0           | > 0              |
    +------------------+-------------+------------------+
```

### MySQL Performance Schema

```sql
-- Enable performance monitoring
UPDATE performance_schema.setup_instruments
SET ENABLED = 'YES', TIMED = 'YES'
WHERE NAME LIKE '%statement/%';

-- Find slowest queries
SELECT
    DIGEST_TEXT,
    COUNT_STAR as exec_count,
    AVG_TIMER_WAIT/1000000000 as avg_ms,
    SUM_ROWS_EXAMINED,
    SUM_ROWS_SENT
FROM performance_schema.events_statements_summary_by_digest
ORDER BY AVG_TIMER_WAIT DESC
LIMIT 10;
```

---

## 9. Common Anti-Patterns

### Anti-Pattern 1: Using SELECT * Everywhere

```sql
-- BAD
SELECT * FROM customers WHERE customer_id = 1;

-- WHY IT'S BAD:
-- 1. Fetches unnecessary columns
-- 2. Prevents covering index optimization
-- 3. Breaks if schema changes

-- GOOD
SELECT customer_id, first_name, email FROM customers WHERE customer_id = 1;
```

### Anti-Pattern 2: N+1 Query Problem

```sql
-- BAD: Query for each customer (N+1 problem)
-- First query: SELECT * FROM customers;
-- Then for EACH customer: SELECT * FROM orders WHERE customer_id = ?;

-- GOOD: Single JOIN query
SELECT c.customer_id, c.first_name, o.order_id, o.total_amount
FROM customers c
LEFT JOIN orders o ON c.customer_id = o.customer_id;

-- OR use subquery with IN
SELECT * FROM orders
WHERE customer_id IN (SELECT customer_id FROM customers WHERE status = 'active');
```

### Anti-Pattern 3: Not Using Indexes

```sql
-- BAD: No index on frequently filtered column
SELECT * FROM orders WHERE status = 'pending';
-- Full table scan on millions of rows!

-- GOOD: Create appropriate index
CREATE INDEX idx_orders_status ON orders(status);
```

### Anti-Pattern 4: Using LIKE with Leading Wildcard

```sql
-- BAD: Leading wildcard prevents index usage
SELECT * FROM products WHERE product_name LIKE '%laptop%';
-- Full scan required

-- GOOD: If possible, no leading wildcard
SELECT * FROM products WHERE product_name LIKE 'laptop%';
-- Can use index

-- ALTERNATIVE: Full-text search
ALTER TABLE products ADD FULLTEXT(product_name);
SELECT * FROM products WHERE MATCH(product_name) AGAINST('laptop');
```

### Anti-Pattern 5: Implicit Type Conversion

```sql
-- BAD: String compared to integer (implicit conversion)
SELECT * FROM customers WHERE customer_id = '1';
-- May prevent index usage

-- GOOD: Match data types
SELECT * FROM customers WHERE customer_id = 1;
```

### Anti-Pattern 6: Using OR Instead of UNION

```sql
-- BAD: OR can prevent index usage
SELECT * FROM products
WHERE category = 'Electronics' OR product_id = 105;

-- GOOD: UNION ALL with separate index usage
SELECT * FROM products WHERE category = 'Electronics'
UNION ALL
SELECT * FROM products WHERE product_id = 105 AND category != 'Electronics';
```

### Anti-Pattern 7: Not Using LIMIT in Development

```sql
-- BAD: Testing with full result set
SELECT * FROM large_table;
-- Returns millions of rows

-- GOOD: Always use LIMIT during development
SELECT * FROM large_table LIMIT 100;
```

### Anti-Pattern 8: Storing Formatted Data

```sql
-- BAD: Storing formatted phone numbers
INSERT INTO customers (phone) VALUES ('(555) 123-4567');
-- Inconsistent formats, hard to search

-- GOOD: Store normalized, format on display
INSERT INTO customers (phone) VALUES ('5551234567');
-- Format in application layer
```

### Anti-Pattern 9: Missing Foreign Key Indexes

```sql
-- BAD: Foreign key without index
ALTER TABLE orders ADD FOREIGN KEY (customer_id) REFERENCES customers(customer_id);
-- JOINs will be slow!

-- GOOD: Index foreign key columns
CREATE INDEX idx_orders_customer ON orders(customer_id);
ALTER TABLE orders ADD FOREIGN KEY (customer_id) REFERENCES customers(customer_id);
```

### Anti-Pattern 10: Over-Indexing

```sql
-- BAD: Index on every column
CREATE INDEX idx1 ON products(product_name);
CREATE INDEX idx2 ON products(category);
CREATE INDEX idx3 ON products(price);
CREATE INDEX idx4 ON products(stock_quantity);
CREATE INDEX idx5 ON products(supplier_id);
-- Slow INSERTs/UPDATEs, wasted storage

-- GOOD: Strategic indexes based on actual queries
CREATE INDEX idx_products_category_price ON products(category, price);
-- Covers: WHERE category = ? AND price < ?
-- Also covers: WHERE category = ? ORDER BY price
```

---

## Quick Reference Cheat Sheet

### Index Commands

```sql
-- Create indexes
CREATE INDEX idx_name ON table(column);
CREATE INDEX idx_name ON table(col1, col2);  -- Composite
CREATE UNIQUE INDEX idx_name ON table(column);

-- View indexes
SHOW INDEX FROM table_name;
SHOW CREATE TABLE table_name;

-- Drop index
DROP INDEX idx_name ON table_name;
```

### EXPLAIN Quick Reference

```sql
EXPLAIN SELECT ...;
EXPLAIN ANALYZE SELECT ...;  -- With actual execution stats

-- Key things to check:
-- type: ALL (bad) vs ref/range/const (good)
-- rows: Lower is better
-- Extra: "Using index" (good), "Using filesort" (bad)
```

### Transaction Commands

```sql
START TRANSACTION;
COMMIT;
ROLLBACK;
SAVEPOINT name;
ROLLBACK TO name;
SET TRANSACTION ISOLATION LEVEL level;
```

### Isolation Levels

```sql
READ UNCOMMITTED  -- Fastest, least safe
READ COMMITTED    -- Default in PostgreSQL
REPEATABLE READ   -- Default in MySQL
SERIALIZABLE      -- Slowest, safest
```

### Normalization Quick Test

- **1NF**: All values atomic? No repeating groups?
- **2NF**: All non-key columns depend on ENTIRE key?
- **3NF**: No non-key depends on another non-key?
- **BCNF**: All determinants are superkeys?

---

## Next Steps

After mastering performance optimization:
1. Practice with real-world datasets (millions of rows)
2. Learn database-specific optimization features
3. Study query optimizer internals
4. Explore advanced topics: partitioning, sharding, replication
5. Learn monitoring tools: MySQL Workbench, pgAdmin, etc.

Remember: **Measure before optimizing!** Use EXPLAIN and profiling to identify actual bottlenecks before making changes.
