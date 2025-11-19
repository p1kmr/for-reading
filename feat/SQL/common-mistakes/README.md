# Common SQL Mistakes: A Comprehensive Guide

This guide covers 25+ common SQL mistakes that developers make, from beginner syntax errors to advanced performance anti-patterns. Each mistake includes practical examples, explanations, and interview tips.

---

## Table of Contents

1. [Syntax Errors](#syntax-errors)
2. [NULL Handling](#null-handling)
3. [JOIN Errors](#join-errors)
4. [Aggregation Mistakes](#aggregation-mistakes)
5. [Performance Anti-Patterns](#performance-anti-patterns)
6. [Data Modification Disasters](#data-modification-disasters)

---

# Syntax Errors

## Mistake 1: Missing Semicolons

### Category: Beginner

**The Mistake:**
```sql
-- Running multiple statements without semicolons
SELECT * FROM users
SELECT * FROM orders
```

**The Problem:**
Without semicolons, the database cannot determine where one statement ends and another begins. This results in a syntax error. While some database clients auto-terminate single statements, multiple statements always require explicit semicolons.

**The Solution:**
```sql
-- Properly terminated statements
SELECT * FROM users;
SELECT * FROM orders;
```

**Why It Matters:**
In production, stored procedures, migrations, and batch scripts contain multiple statements. Missing semicolons cause entire scripts to fail, potentially leaving databases in inconsistent states during deployments.

**Interview Tip:**
Always include semicolons in your SQL answers, even for single statements. It demonstrates attention to detail and shows you write production-ready code.

---

## Mistake 2: Wrong Clause Order

### Category: Beginner

**The Mistake:**
```sql
-- Clauses in wrong order
SELECT name, department
FROM employees
ORDER BY name
WHERE salary > 50000
GROUP BY department
HAVING COUNT(*) > 5;
```

**The Problem:**
SQL requires clauses in a specific order: SELECT, FROM, WHERE, GROUP BY, HAVING, ORDER BY, LIMIT. The query above will fail because WHERE must come before ORDER BY, and GROUP BY must come before HAVING.

**The Solution:**
```sql
-- Correct clause order
SELECT department, COUNT(*) as employee_count
FROM employees
WHERE salary > 50000
GROUP BY department
HAVING COUNT(*) > 5
ORDER BY employee_count DESC;
```

**Why It Matters:**
This is one of the most common errors that breaks queries entirely. In production, incorrect clause order in dynamically generated queries can cause application failures.

**Interview Tip:**
Memorize the order: SELECT, FROM, WHERE, GROUP BY, HAVING, ORDER BY, LIMIT. When writing queries during interviews, mentally verify the order before submitting.

---

## Mistake 3: Quote Type Confusion

### Category: Beginner

**The Mistake:**
```sql
-- Using wrong quote types
SELECT * FROM users WHERE name = "John";  -- Double quotes for strings
SELECT * FROM 'users';                     -- Single quotes for table names
SELECT * FROM users WHERE status = active; -- No quotes for strings
```

**The Problem:**
In standard SQL, single quotes (`'`) are for string literals, double quotes (`"`) are for identifiers (table/column names). Using them incorrectly causes syntax errors or unexpected behavior. Unquoted strings are interpreted as column names.

**The Solution:**
```sql
-- Correct quote usage
SELECT * FROM users WHERE name = 'John';           -- Single quotes for strings
SELECT * FROM "users";                              -- Double quotes for identifiers
SELECT * FROM users WHERE status = 'active';        -- String values need quotes

-- For reserved words as identifiers
SELECT * FROM "order" WHERE "group" = 'A';
```

**Why It Matters:**
Different databases handle quotes differently. MySQL uses backticks for identifiers, while PostgreSQL and SQL Server use double quotes. Incorrect quoting causes portability issues and subtle bugs.

**Interview Tip:**
Know your target database's quoting conventions. When in doubt, use single quotes for strings and mention that identifier quoting varies by database.

---

## Mistake 4: Case Sensitivity Issues

### Category: Beginner

**The Mistake:**
```sql
-- Assuming case-insensitive comparison
SELECT * FROM users WHERE email = 'John@Example.com';

-- When database has: 'john@example.com'
-- Result: No rows returned (in case-sensitive databases)
```

**The Problem:**
Case sensitivity varies by database and collation settings. PostgreSQL is case-sensitive by default; MySQL depends on collation. This leads to failed lookups and inconsistent results across environments.

**The Solution:**
```sql
-- Explicit case-insensitive comparison
SELECT * FROM users WHERE LOWER(email) = LOWER('John@Example.com');

-- Or use database-specific functions
SELECT * FROM users WHERE email ILIKE 'john@example.com';  -- PostgreSQL

-- Better: Normalize data on insert
INSERT INTO users (email) VALUES (LOWER('John@Example.com'));
```

**Why It Matters:**
In production, case sensitivity issues cause login failures, duplicate records, and data integrity problems. Search features may miss results, frustrating users.

**Interview Tip:**
Mention that you normalize data (e.g., lowercase emails) on insert and use appropriate comparison methods. This shows awareness of data consistency.

---

## Mistake 5: Using Reserved Keywords as Identifiers

### Category: Beginner

**The Mistake:**
```sql
-- Using reserved keywords without escaping
CREATE TABLE order (
    id INT PRIMARY KEY,
    date DATE,
    user INT,
    group VARCHAR(50)
);

SELECT order, date, group FROM order;
```

**The Problem:**
`ORDER`, `DATE`, `USER`, and `GROUP` are reserved SQL keywords. Using them as identifiers without proper escaping causes syntax errors because the parser interprets them as keywords.

**The Solution:**
```sql
-- Option 1: Escape with appropriate quotes
CREATE TABLE "order" (
    id INT PRIMARY KEY,
    "date" DATE,
    "user" INT,
    "group" VARCHAR(50)
);

SELECT "order"."date", "order"."group" FROM "order";

-- Option 2: Better naming (recommended)
CREATE TABLE orders (
    id INT PRIMARY KEY,
    order_date DATE,
    user_id INT,
    group_name VARCHAR(50)
);

SELECT order_date, group_name FROM orders;
```

**Why It Matters:**
Reserved keyword conflicts cause immediate failures in production deployments. They also reduce code readability and cause maintenance headaches.

**Interview Tip:**
Avoid reserved keywords entirely by using better names (e.g., `orders` instead of `order`, `created_at` instead of `date`). If asked about existing schemas with reserved keywords, explain escaping mechanisms.

---

# NULL Handling

## Mistake 6: NULL = NULL Comparison

### Category: Intermediate

**The Mistake:**
```sql
-- Trying to find NULL values with equality
SELECT * FROM employees WHERE manager_id = NULL;

-- Trying to find non-NULL values
SELECT * FROM employees WHERE manager_id != NULL;
-- OR
SELECT * FROM employees WHERE manager_id <> NULL;
```

**The Problem:**
NULL represents "unknown" in SQL. Any comparison with NULL (including `NULL = NULL`) returns NULL (not true or false). The WHERE clause only includes rows where the condition is true, so these queries return no rows.

**The Solution:**
```sql
-- Correct NULL checks
SELECT * FROM employees WHERE manager_id IS NULL;
SELECT * FROM employees WHERE manager_id IS NOT NULL;

-- For comparing nullable columns
SELECT * FROM orders
WHERE COALESCE(discount_code, '') = COALESCE(promo_code, '');

-- Or use NULL-safe equality (MySQL)
SELECT * FROM orders WHERE discount_code <=> promo_code;
```

**Why It Matters:**
This is one of the most common SQL bugs. Queries appear to work (no errors) but return incorrect results. In production, this causes missing data in reports and incorrect business logic.

**Interview Tip:**
Always use IS NULL/IS NOT NULL. If asked to compare two nullable columns, explain that you'd use COALESCE or database-specific NULL-safe operators.

---

## Mistake 7: NULL in IN Clause

### Category: Intermediate

**The Mistake:**
```sql
-- Expecting NULL to match in IN clause
SELECT * FROM products WHERE category_id IN (1, 2, NULL);

-- Expecting NOT IN to exclude NULLs properly
SELECT * FROM products
WHERE category_id NOT IN (SELECT category_id FROM discontinued);
-- If discontinued has any NULL category_id, this returns NO rows!
```

**The Problem:**
`IN` uses equality comparisons, so NULL never matches. More dangerously, `NOT IN` with any NULL value returns no rows because `x NOT IN (1, NULL)` evaluates to `NULL` for all x.

**The Solution:**
```sql
-- Handle NULL explicitly with IN
SELECT * FROM products
WHERE category_id IN (1, 2) OR category_id IS NULL;

-- Safe NOT IN with NULL exclusion
SELECT * FROM products
WHERE category_id NOT IN (
    SELECT category_id FROM discontinued WHERE category_id IS NOT NULL
);

-- Better: Use NOT EXISTS
SELECT * FROM products p
WHERE NOT EXISTS (
    SELECT 1 FROM discontinued d WHERE d.category_id = p.category_id
);
```

**Why It Matters:**
The `NOT IN` with NULL trap is particularly dangerous because it silently returns empty results. This has caused major production incidents where entire reports showed no data.

**Interview Tip:**
When using NOT IN with subqueries, always mention that you'd add `WHERE column IS NOT NULL` or use NOT EXISTS. This shows deep SQL knowledge.

---

## Mistake 8: NULL in Aggregate Functions

### Category: Intermediate

**The Mistake:**
```sql
-- Assuming COUNT counts NULLs
SELECT COUNT(commission) FROM salespeople;
-- If 3 out of 10 have NULL commission, returns 7, not 10

-- Expecting AVG to treat NULL as 0
SELECT AVG(bonus) FROM employees;
-- NULLs are excluded, potentially inflating the average
```

**The Problem:**
Aggregate functions (except COUNT(*)) ignore NULL values. COUNT(column) counts non-NULL values only. AVG, SUM, MAX, MIN all exclude NULLs, which can give misleading results.

**The Solution:**
```sql
-- Count all rows including NULLs
SELECT COUNT(*) FROM salespeople;

-- Count NULLs explicitly
SELECT
    COUNT(*) as total,
    COUNT(commission) as with_commission,
    COUNT(*) - COUNT(commission) as without_commission
FROM salespeople;

-- Treat NULL as 0 for averages
SELECT AVG(COALESCE(bonus, 0)) FROM employees;

-- Show the difference
SELECT
    AVG(bonus) as avg_excluding_null,
    AVG(COALESCE(bonus, 0)) as avg_including_null_as_zero
FROM employees;
```

**Why It Matters:**
Incorrect NULL handling in aggregates leads to wrong business metrics. An average salary that excludes unpaid interns (NULL salary) misrepresents actual compensation costs.

**Interview Tip:**
Explicitly state your NULL handling strategy when writing aggregate queries. Ask clarifying questions: "Should NULL bonuses be treated as 0 or excluded?"

---

## Mistake 9: NULL in String Concatenation

### Category: Intermediate

**The Mistake:**
```sql
-- Concatenating with NULL
SELECT first_name || ' ' || middle_name || ' ' || last_name AS full_name
FROM employees;
-- If middle_name is NULL, entire result is NULL in standard SQL

-- Example result:
-- 'John' || ' ' || NULL || ' ' || 'Doe' = NULL
```

**The Problem:**
In standard SQL and many databases (PostgreSQL, Oracle), concatenating any value with NULL produces NULL. The entire expression becomes NULL if any part is NULL.

**The Solution:**
```sql
-- Use COALESCE to handle NULLs
SELECT
    first_name || ' ' || COALESCE(middle_name || ' ', '') || last_name AS full_name
FROM employees;

-- Or use CONCAT function (MySQL, SQL Server ignore NULLs)
SELECT CONCAT(first_name, ' ', middle_name, ' ', last_name) FROM employees;

-- More readable with CONCAT_WS (with separator)
SELECT CONCAT_WS(' ', first_name, middle_name, last_name) AS full_name
FROM employees;

-- Handle all edge cases
SELECT TRIM(
    COALESCE(first_name, '') || ' ' ||
    COALESCE(middle_name || ' ', '') ||
    COALESCE(last_name, '')
) AS full_name
FROM employees;
```

**Why It Matters:**
NULL propagation in concatenation causes missing data in reports, emails, and user interfaces. Users see blank names or empty addresses instead of partial information.

**Interview Tip:**
Use COALESCE or CONCAT_WS to handle NULLs in string operations. Mention that behavior varies by database and you'd test your specific platform.

---

# JOIN Errors

## Mistake 10: Missing JOIN Condition (Cartesian Product)

### Category: Beginner

**The Mistake:**
```sql
-- Forgotten JOIN condition
SELECT o.order_id, c.customer_name
FROM orders o, customers c;

-- Or with explicit CROSS JOIN not intended
SELECT o.order_id, c.customer_name
FROM orders o
JOIN customers c;  -- Missing ON clause
```

**The Problem:**
Without a JOIN condition, every row from the first table is combined with every row from the second table. With 1,000 orders and 500 customers, this produces 500,000 rows instead of 1,000. This causes massive performance issues and incorrect results.

**The Solution:**
```sql
-- Explicit JOIN with condition
SELECT o.order_id, c.customer_name
FROM orders o
INNER JOIN customers c ON o.customer_id = c.customer_id;

-- If you need a Cartesian product, be explicit
SELECT sizes.size_name, colors.color_name
FROM sizes
CROSS JOIN colors;  -- Intentional: all size-color combinations
```

**Why It Matters:**
Accidental Cartesian products have crashed production databases. A missing JOIN condition in a report query can consume all available memory and CPU, causing outages.

**Interview Tip:**
Always use explicit JOIN syntax with ON clauses. Avoid comma-separated tables in FROM. If your query returns unexpectedly many rows, check for missing JOIN conditions first.

---

## Mistake 11: LEFT JOIN vs INNER JOIN Confusion

### Category: Intermediate

**The Mistake:**
```sql
-- Using INNER JOIN when you need all left records
SELECT c.customer_name, o.order_id
FROM customers c
INNER JOIN orders o ON c.customer_id = o.customer_id;
-- Customers with no orders are excluded!

-- Using LEFT JOIN but filtering it to INNER
SELECT c.customer_name, o.order_id
FROM customers c
LEFT JOIN orders o ON c.customer_id = o.customer_id
WHERE o.order_date > '2024-01-01';  -- NULLs filtered out!
```

**The Problem:**
INNER JOIN excludes rows without matches, which may not be intended. Also, adding WHERE conditions on the right table of a LEFT JOIN effectively converts it to an INNER JOIN because NULL values are filtered out.

**The Solution:**
```sql
-- Include customers without orders
SELECT c.customer_name, o.order_id
FROM customers c
LEFT JOIN orders o ON c.customer_id = o.customer_id;

-- Filter in JOIN condition to preserve LEFT JOIN behavior
SELECT c.customer_name, o.order_id
FROM customers c
LEFT JOIN orders o ON c.customer_id = o.customer_id
    AND o.order_date > '2024-01-01';

-- Or handle NULL explicitly
SELECT c.customer_name, o.order_id
FROM customers c
LEFT JOIN orders o ON c.customer_id = o.customer_id
WHERE o.order_date > '2024-01-01' OR o.order_id IS NULL;
```

**Why It Matters:**
Wrong JOIN type causes missing data in reports. A customer list that accidentally excludes customers without orders misses important prospects. Financial reports may miss entities with no transactions.

**Interview Tip:**
Clearly state why you're choosing LEFT vs INNER JOIN. Show you understand the difference by explaining: "I'll use LEFT JOIN because we want all customers, even those without orders."

---

## Mistake 12: Ambiguous Column Names

### Category: Beginner

**The Mistake:**
```sql
-- Ambiguous column reference
SELECT customer_id, name, order_date
FROM customers
JOIN orders ON customers.customer_id = orders.customer_id;
-- Error: "customer_id" is ambiguous
```

**The Problem:**
When joining tables with columns of the same name, you must specify which table the column comes from. The database cannot determine which `customer_id` you want in the SELECT list.

**The Solution:**
```sql
-- Use table aliases and qualify all columns
SELECT
    c.customer_id,
    c.name AS customer_name,
    o.order_id,
    o.order_date
FROM customers c
JOIN orders o ON c.customer_id = o.customer_id;

-- In complex queries, always qualify
SELECT
    c.customer_id,
    c.name,
    o.order_id,
    p.name AS product_name  -- Disambiguate duplicate column names
FROM customers c
JOIN orders o ON c.customer_id = o.customer_id
JOIN products p ON o.product_id = p.product_id;
```

**Why It Matters:**
Ambiguous columns cause errors that prevent queries from running. In dynamically generated SQL, this creates runtime failures that are hard to debug.

**Interview Tip:**
Always use table aliases (short, meaningful ones like `c` for customers, `o` for orders). Qualify every column in JOIN queries. This makes your code clear and prevents errors.

---

## Mistake 13: Incorrect JOIN Order with Multiple Tables

### Category: Advanced

**The Mistake:**
```sql
-- Wrong order loses data
SELECT c.name, o.order_id, r.return_reason
FROM orders o
LEFT JOIN customers c ON o.customer_id = c.customer_id
LEFT JOIN returns r ON o.order_id = r.order_id;
-- Gets orders and their returns, but starts from orders

-- Intent: All customers, their orders, and any returns
-- Problem: Customers without orders are missing
```

**The Problem:**
JOIN order matters, especially with LEFT JOINs. The "left" table determines which rows are preserved. Starting from the wrong table excludes records you need.

**The Solution:**
```sql
-- Start from the table you want ALL records from
SELECT c.name, o.order_id, r.return_reason
FROM customers c
LEFT JOIN orders o ON c.customer_id = o.customer_id
LEFT JOIN returns r ON o.order_id = r.order_id;
-- All customers, with their orders (if any), and returns (if any)

-- For complex requirements, visualize the relationships
-- customers --(1:N)--> orders --(1:N)--> returns
-- Start from customers to get all customers
```

**Why It Matters:**
In production reporting, wrong JOIN order causes incomplete data. A sales report starting from orders misses sales reps with no orders. Customer analytics miss inactive customers.

**Interview Tip:**
Before writing JOINs, identify which table's rows you need to preserve. State this clearly: "I'm starting from customers because we need all customers, including those without orders."

---

# Aggregation Mistakes

## Mistake 14: Non-Aggregated Columns in SELECT with GROUP BY

### Category: Intermediate

**The Mistake:**
```sql
-- Selecting non-aggregated column not in GROUP BY
SELECT department, employee_name, AVG(salary)
FROM employees
GROUP BY department;
-- Error: employee_name must be in GROUP BY or aggregate
```

**The Problem:**
When using GROUP BY, every column in SELECT must either be in the GROUP BY clause or inside an aggregate function. `employee_name` isn't aggregated and isn't grouped, so the database doesn't know which employee name to show for each department.

**The Solution:**
```sql
-- Option 1: Only select grouped/aggregated columns
SELECT department, AVG(salary) as avg_salary
FROM employees
GROUP BY department;

-- Option 2: Include in GROUP BY (if that's what you want)
SELECT department, employee_name, salary
FROM employees
GROUP BY department, employee_name, salary;

-- Option 3: Use aggregate to pick one
SELECT
    department,
    MAX(employee_name) as sample_employee,
    AVG(salary) as avg_salary
FROM employees
GROUP BY department;

-- Option 4: Use window functions for per-row detail with aggregates
SELECT
    department,
    employee_name,
    salary,
    AVG(salary) OVER (PARTITION BY department) as dept_avg_salary
FROM employees;
```

**Why It Matters:**
Some databases (MySQL with specific settings) allow this but return arbitrary values, causing incorrect results. Strict databases reject the query. Either way, it indicates unclear logic.

**Interview Tip:**
Every SELECT column must be justified in GROUP BY queries. If asked why, explain: "Each group produces one row, so non-aggregated columns would have multiple values with no rule to pick one."

---

## Mistake 15: Using WHERE Instead of HAVING

### Category: Intermediate

**The Mistake:**
```sql
-- Trying to filter on aggregate in WHERE
SELECT department, COUNT(*) as employee_count
FROM employees
WHERE COUNT(*) > 5  -- Error: aggregates not allowed in WHERE
GROUP BY department;
```

**The Problem:**
WHERE filters individual rows before aggregation. HAVING filters groups after aggregation. You cannot reference aggregates in WHERE because they haven't been calculated yet.

**The Solution:**
```sql
-- Use HAVING for aggregate conditions
SELECT department, COUNT(*) as employee_count
FROM employees
GROUP BY department
HAVING COUNT(*) > 5;

-- Combine WHERE and HAVING appropriately
SELECT department, COUNT(*) as employee_count
FROM employees
WHERE status = 'active'      -- Filter rows first
GROUP BY department
HAVING COUNT(*) > 5;         -- Filter groups after

-- Remember: WHERE = before grouping, HAVING = after grouping
```

**Why It Matters:**
This is a fundamental SQL concept that causes immediate errors. Understanding the logical processing order (FROM -> WHERE -> GROUP BY -> HAVING -> SELECT -> ORDER BY) is essential.

**Interview Tip:**
Explain the difference clearly: "WHERE filters rows before grouping, HAVING filters groups after aggregation." Use both when needed to show mastery.

---

## Mistake 16: COUNT(*) vs COUNT(column) Confusion

### Category: Intermediate

**The Mistake:**
```sql
-- Expecting same results
SELECT
    COUNT(*) as total,
    COUNT(manager_id) as managers
FROM employees;
-- Results differ if manager_id has NULLs

-- Counting distinct incorrectly
SELECT COUNT(department) FROM employees;  -- Counts duplicate departments
```

**The Problem:**
`COUNT(*)` counts all rows. `COUNT(column)` counts non-NULL values in that column. `COUNT(DISTINCT column)` counts unique non-NULL values. Using the wrong one gives incorrect counts.

**The Solution:**
```sql
-- Understand the differences
SELECT
    COUNT(*) as total_rows,
    COUNT(manager_id) as has_manager,
    COUNT(*) - COUNT(manager_id) as no_manager,
    COUNT(DISTINCT department) as unique_departments
FROM employees;

-- Be explicit about what you're counting
SELECT
    COUNT(*) as total_orders,
    COUNT(discount_code) as orders_with_discount,
    COUNT(DISTINCT customer_id) as unique_customers
FROM orders;
```

**Why It Matters:**
Incorrect COUNT usage causes wrong metrics. Counting `COUNT(email)` vs `COUNT(*)` for user registrations will differ if email is optional. Business decisions based on wrong counts are costly.

**Interview Tip:**
Be explicit: say "COUNT(*) for total rows" or "COUNT(column) for non-NULL values." When asked about distinct counts, always use COUNT(DISTINCT column).

---

## Mistake 17: GROUP BY Column Order Issues

### Category: Intermediate

**The Mistake:**
```sql
-- GROUP BY with incorrect granularity
SELECT year, month, region, SUM(sales)
FROM sales_data
GROUP BY year, region;  -- Missing month - Error!

-- Grouping by wrong column (column number)
SELECT department, COUNT(*)
FROM employees
GROUP BY 1;  -- Works but fragile - breaks if SELECT order changes
```

**The Problem:**
All non-aggregated SELECT columns must be in GROUP BY. Also, GROUP BY using column numbers (1, 2, etc.) is fragile and unclear - if someone reorders SELECT columns, the query breaks or gives wrong results.

**The Solution:**
```sql
-- Include all non-aggregated columns
SELECT year, month, region, SUM(sales) as total_sales
FROM sales_data
GROUP BY year, month, region;

-- Use explicit column names, not positions
SELECT department, COUNT(*) as employee_count
FROM employees
GROUP BY department;

-- For expressions, repeat or use alias (database-dependent)
SELECT
    DATE_TRUNC('month', order_date) as order_month,
    COUNT(*) as order_count
FROM orders
GROUP BY DATE_TRUNC('month', order_date);  -- Repeat expression
-- Or GROUP BY 1 if you must (less maintainable)
```

**Why It Matters:**
Positional GROUP BY causes maintenance issues. When queries change, positional references break silently or cause wrong groupings. Explicit names are self-documenting.

**Interview Tip:**
Always use explicit column names in GROUP BY. If the interviewer uses positional syntax, note that you prefer explicit names for maintainability.

---

# Performance Anti-Patterns

## Mistake 18: SELECT * in Production Queries

### Category: Beginner

**The Mistake:**
```sql
-- Selecting all columns
SELECT * FROM users WHERE user_id = 123;

-- In application code
SELECT * FROM products WHERE category = 'Electronics';
```

**The Problem:**
`SELECT *` retrieves all columns, including large TEXT/BLOB fields you don't need. It prevents covering indexes, increases network traffic, and breaks when columns are added/removed.

**The Solution:**
```sql
-- Select only needed columns
SELECT user_id, username, email FROM users WHERE user_id = 123;

-- Even for "all" columns, be explicit
SELECT
    product_id,
    name,
    price,
    category,
    description,
    created_at
FROM products
WHERE category = 'Electronics';

-- SELECT * is acceptable for:
-- 1. Quick exploration in development
-- 2. When you genuinely need all columns AND schema is stable
-- 3. EXISTS subqueries (SELECT 1 is better)
```

**Why It Matters:**
`SELECT *` in production causes performance degradation. Retrieving a 10MB image blob when you only need the filename wastes bandwidth. Adding a column to a table breaks applications expecting specific columns.

**Interview Tip:**
Never use SELECT * in interview answers (except for quick exploration). Always list specific columns. Explain: "I avoid SELECT * because it prevents index-only scans and breaks when schema changes."

---

## Mistake 19: Functions on Indexed Columns

### Category: Advanced

**The Mistake:**
```sql
-- Functions prevent index usage
SELECT * FROM orders
WHERE YEAR(order_date) = 2024;

SELECT * FROM users
WHERE LOWER(email) = 'john@example.com';

SELECT * FROM products
WHERE price * 1.1 > 100;
```

**The Problem:**
Applying functions to indexed columns prevents the database from using the index. The database must scan every row, apply the function, then compare. This turns an instant indexed lookup into a full table scan.

**The Solution:**
```sql
-- Rewrite to keep column clean
SELECT * FROM orders
WHERE order_date >= '2024-01-01' AND order_date < '2025-01-01';

-- Use function-based index (if needed often)
CREATE INDEX idx_users_email_lower ON users(LOWER(email));
SELECT * FROM users WHERE LOWER(email) = 'john@example.com';

-- Move calculation to the other side
SELECT * FROM products WHERE price > 100 / 1.1;  -- ~90.91

-- For case-insensitive search, use collation or dedicated index
CREATE INDEX idx_email_ci ON users(email COLLATE NOCASE);
```

**Why It Matters:**
Function-wrapped columns are a leading cause of slow queries. A query that runs in 1ms with an index can take 10+ seconds with a table scan on millions of rows.

**Interview Tip:**
When writing WHERE clauses, keep indexed columns "clean." If you must use a function, mention creating a functional index. This shows performance awareness.

---

## Mistake 20: N+1 Query Problem

### Category: Advanced

**The Mistake:**
```sql
-- Application code that causes N+1
-- Step 1: Get all orders
SELECT * FROM orders WHERE date = '2024-01-01';  -- Returns 100 orders

-- Step 2: For EACH order, get customer (100 more queries!)
SELECT * FROM customers WHERE customer_id = ?;  -- Executed 100 times!
```

**The Problem:**
N+1 queries execute one query to get a list, then N additional queries to get related data for each item. With 100 orders, you make 101 queries instead of 1-2. This devastates performance.

**The Solution:**
```sql
-- Option 1: JOIN to get everything in one query
SELECT o.*, c.name, c.email
FROM orders o
JOIN customers c ON o.customer_id = c.customer_id
WHERE o.date = '2024-01-01';

-- Option 2: Two queries with IN clause
-- Query 1: Get orders
SELECT * FROM orders WHERE date = '2024-01-01';

-- Query 2: Get all relevant customers in ONE query
SELECT * FROM customers
WHERE customer_id IN (101, 102, 103, ...);  -- IDs from query 1

-- Application joins results in memory
```

**Why It Matters:**
N+1 is one of the most common performance problems in applications. Database latency multiplied by N becomes significant. A page that makes 100 queries is noticeably slow.

**Interview Tip:**
When asked about loading related data, immediately mention N+1 awareness. Explain you'd use JOINs or batch loading. ORMs often cause this - mention eager loading.

---

## Mistake 21: Missing Indexes on Frequently Queried Columns

### Category: Advanced

**The Mistake:**
```sql
-- Common queries without supporting indexes
SELECT * FROM orders WHERE customer_id = 123;
-- No index on customer_id - full table scan

SELECT * FROM products WHERE category = 'Electronics' ORDER BY price;
-- No composite index - scans then sorts

SELECT * FROM logs WHERE created_at BETWEEN '2024-01-01' AND '2024-01-31';
-- No index on created_at - scans millions of rows
```

**The Problem:**
Without indexes, the database scans entire tables for every query. As tables grow, query times grow linearly. A 1-second query on 1 million rows becomes 10 seconds on 10 million rows.

**The Solution:**
```sql
-- Add indexes for WHERE clause columns
CREATE INDEX idx_orders_customer ON orders(customer_id);

-- Composite indexes for combined filtering and sorting
CREATE INDEX idx_products_category_price ON products(category, price);

-- Index for range queries
CREATE INDEX idx_logs_created ON logs(created_at);

-- Check existing indexes before adding
-- PostgreSQL:
SELECT indexname FROM pg_indexes WHERE tablename = 'orders';

-- Consider covering indexes for SELECT columns
CREATE INDEX idx_orders_covering ON orders(customer_id)
    INCLUDE (order_date, total);
```

**Why It Matters:**
Missing indexes are the #1 cause of slow database queries. Production databases with millions of rows require proper indexing. Index creation should be part of schema design.

**Interview Tip:**
When writing queries, mention what indexes you'd create to support them. Show awareness of the query-index relationship. Ask about table sizes and query patterns.

---

# Data Modification Disasters

## Mistake 22: UPDATE Without WHERE Clause

### Category: Beginner

**The Mistake:**
```sql
-- Forgetting WHERE clause
UPDATE employees SET salary = 50000;
-- Updates EVERY employee's salary!

-- Typo in WHERE
UPDATE users SET status = 'inactive' WHERE id = 0;
-- No user with id = 0, but query succeeds with 0 rows affected
```

**The Problem:**
UPDATE without WHERE modifies every row in the table. This is often irreversible without backups. Even with WHERE, typos or wrong conditions cause mass updates to wrong rows.

**The Solution:**
```sql
-- Always include WHERE
UPDATE employees SET salary = 50000 WHERE employee_id = 123;

-- Safe update pattern: SELECT first
SELECT * FROM employees WHERE employee_id = 123;  -- Verify target rows

-- Then UPDATE with same condition
UPDATE employees SET salary = 50000 WHERE employee_id = 123;

-- Check rows affected
-- Should be 1 for single row update

-- Use transactions for safety
BEGIN;
UPDATE employees SET salary = 50000 WHERE department = 'Engineering';
-- Check affected rows count
-- If correct: COMMIT;
-- If wrong: ROLLBACK;
```

**Why It Matters:**
Mass unintended updates cause data corruption and business disasters. Resetting all passwords, clearing all balances, or deleting all orders can shut down businesses.

**Interview Tip:**
Always show the WHERE clause prominently. Mention that you'd SELECT first to verify rows, then UPDATE. In production, use transactions for safety.

---

## Mistake 23: DELETE Without WHERE Clause

### Category: Beginner

**The Mistake:**
```sql
-- Forgetting WHERE - deletes everything!
DELETE FROM orders;

-- Wrong condition deletes wrong data
DELETE FROM users WHERE registration_date > '2024-01-01';
-- Oops, meant < '2024-01-01' (old users, not new ones)
```

**The Problem:**
DELETE without WHERE removes all rows. Unlike TRUNCATE, it's logged and triggers fire, but the data is gone. Wrong conditions delete valuable data that may be unrecoverable.

**The Solution:**
```sql
-- Always include WHERE
DELETE FROM orders WHERE order_id = 456;

-- Safe delete pattern: COUNT first
SELECT COUNT(*) FROM users WHERE registration_date < '2024-01-01';
-- Verify count is reasonable

-- Then SELECT to review
SELECT * FROM users
WHERE registration_date < '2024-01-01'
LIMIT 10;  -- Spot check

-- Then DELETE with same condition
DELETE FROM users WHERE registration_date < '2024-01-01';

-- Use LIMIT for safety in batches
DELETE FROM logs WHERE created_at < '2024-01-01' LIMIT 1000;
```

**Why It Matters:**
Accidental deletion of production data causes business losses, legal issues, and customer trust damage. Recovery from backups takes time and may lose recent data.

**Interview Tip:**
Show your safety process: SELECT COUNT, SELECT sample, then DELETE. Mention backups and transactions. Some interviewers will ask how you'd recover from accidental deletion.

---

## Mistake 24: TRUNCATE vs DELETE Confusion

### Category: Intermediate

**The Mistake:**
```sql
-- Using DELETE when TRUNCATE is appropriate
DELETE FROM temp_import_data;
-- Slow, fully logged, keeps auto-increment

-- Using TRUNCATE when DELETE is needed
TRUNCATE TABLE audit_log;
-- Can't be rolled back (in many databases)
-- Resets auto-increment
-- Triggers don't fire
```

**The Problem:**
DELETE logs each row deletion, is slow on large tables, can be rolled back, and triggers fire. TRUNCATE is fast but can't be rolled back (in most databases), resets identity columns, and skips triggers.

**The Solution:**
```sql
-- Use TRUNCATE for:
-- - Clearing staging/temp tables
-- - Fast removal of all data
-- - When you want to reset auto-increment
TRUNCATE TABLE temp_staging_data;

-- Use DELETE for:
-- - Conditional removal
-- - When triggers must fire
-- - When you need rollback capability
-- - Audit/compliance requirements
DELETE FROM orders WHERE status = 'cancelled';

-- Safe approach for large deletes
BEGIN;
DELETE FROM logs WHERE created_at < '2024-01-01';
-- Check affected count
COMMIT;  -- or ROLLBACK;
```

**Why It Matters:**
Using DELETE to clear 10 million rows takes minutes; TRUNCATE takes seconds. But TRUNCATE's inability to rollback has caused data loss when run accidentally or with wrong table name.

**Interview Tip:**
Know the trade-offs. Say: "TRUNCATE is faster and resets identity, but DELETE is transactional and triggers fire." Choose based on requirements.

---

## Mistake 25: Not Using Transactions for Multi-Statement Operations

### Category: Advanced

**The Mistake:**
```sql
-- Multiple related operations without transaction
INSERT INTO orders (customer_id, total) VALUES (123, 99.99);
-- Returns order_id = 456

INSERT INTO order_items (order_id, product_id, quantity) VALUES (456, 789, 2);
-- Error: Foreign key violation (product_id 789 doesn't exist)

-- Now we have an order with no items!
```

**The Problem:**
Without transactions, partial failures leave data inconsistent. The first statement succeeds, the second fails, and you have orphaned records. This violates data integrity and causes application bugs.

**The Solution:**
```sql
-- Wrap related operations in transaction
BEGIN;

INSERT INTO orders (customer_id, total) VALUES (123, 99.99);
-- Get the order_id (database-specific)

INSERT INTO order_items (order_id, product_id, quantity)
VALUES (LASTVAL(), 789, 2);  -- PostgreSQL

-- If everything succeeded
COMMIT;

-- If anything failed
ROLLBACK;

-- Or use stored procedures with error handling
CREATE OR REPLACE FUNCTION create_order(
    p_customer_id INT,
    p_product_id INT,
    p_quantity INT,
    p_total DECIMAL
) RETURNS INT AS $$
DECLARE
    v_order_id INT;
BEGIN
    INSERT INTO orders (customer_id, total)
    VALUES (p_customer_id, p_total)
    RETURNING id INTO v_order_id;

    INSERT INTO order_items (order_id, product_id, quantity)
    VALUES (v_order_id, p_product_id, p_quantity);

    RETURN v_order_id;
EXCEPTION WHEN OTHERS THEN
    RAISE;
END;
$$ LANGUAGE plpgsql;
```

**Why It Matters:**
Data consistency is fundamental. Partial updates cause bugs, incorrect reports, and customer complaints. Financial transactions absolutely require atomic operations.

**Interview Tip:**
Always mention transactions for multi-statement operations. Explain ACID properties (especially Atomicity). Show you understand isolation levels if asked.

---

## Mistake 26: Implicit Data Type Conversion

### Category: Advanced

**The Mistake:**
```sql
-- String compared to number without explicit cast
SELECT * FROM orders WHERE order_id = '123';
-- May work but prevents index usage in some databases

-- Number compared to string
SELECT * FROM products WHERE price > '10';
-- Unpredictable sorting: '9' > '10' (string comparison)

-- Date string in wrong format
SELECT * FROM events WHERE event_date = '01-15-2024';
-- Fails or wrong results in YYYY-MM-DD databases
```

**The Problem:**
Implicit type conversions can prevent index usage, cause incorrect comparisons, and produce unpredictable results. String '9' is greater than '10' in string comparison.

**The Solution:**
```sql
-- Use correct types
SELECT * FROM orders WHERE order_id = 123;  -- Number
SELECT * FROM products WHERE price > 10;     -- Number

-- Explicit casting when needed
SELECT * FROM orders WHERE order_id = CAST('123' AS INTEGER);

-- Use ISO date format (always works)
SELECT * FROM events WHERE event_date = '2024-01-15';

-- Or use date literals
SELECT * FROM events WHERE event_date = DATE '2024-01-15';

-- Validate in application code before sending to database
```

**Why It Matters:**
Type mismatches cause silent bugs that only appear with certain data. Performance degradation from lost index usage is hard to diagnose. Cross-database portability breaks.

**Interview Tip:**
Use correct types and ISO date formats. Mention that implicit conversions hurt performance. If using string numbers, explain why and cast explicitly.

---

## Mistake 27: Not Handling Duplicates Correctly

### Category: Intermediate

**The Mistake:**
```sql
-- INSERT may fail on duplicate
INSERT INTO users (email, name) VALUES ('john@example.com', 'John');
-- Error: Duplicate key violation if email exists

-- UNION without ALL duplicates data
SELECT product_id FROM current_orders
UNION ALL
SELECT product_id FROM current_orders;  -- Double counts!

-- Missing DISTINCT in subquery
SELECT * FROM products WHERE category_id IN (
    SELECT category_id FROM featured_categories
);  -- Works but inefficient if subquery has duplicates
```

**The Problem:**
Not handling duplicates causes insert failures, double-counting, and incorrect results. Applications need strategies for duplicate keys and deduplication in queries.

**The Solution:**
```sql
-- Upsert (INSERT or UPDATE)
-- PostgreSQL:
INSERT INTO users (email, name) VALUES ('john@example.com', 'John')
ON CONFLICT (email) DO UPDATE SET name = EXCLUDED.name;

-- MySQL:
INSERT INTO users (email, name) VALUES ('john@example.com', 'John')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Use UNION for deduplication
SELECT product_id FROM current_orders
UNION
SELECT product_id FROM archived_orders;  -- UNION deduplicates

-- Use UNION ALL only when you want duplicates
SELECT product_id FROM order_items
UNION ALL
SELECT product_id FROM wishlist_items;  -- Keep all for counting

-- EXISTS instead of IN for clarity
SELECT * FROM products p WHERE EXISTS (
    SELECT 1 FROM featured_categories fc
    WHERE fc.category_id = p.category_id
);
```

**Why It Matters:**
Duplicate handling affects data integrity and query correctness. Failed inserts cause bad user experience. Double-counting inflates metrics and causes wrong business decisions.

**Interview Tip:**
Know UNION vs UNION ALL difference. Show INSERT ON CONFLICT/ON DUPLICATE KEY for upserts. Mention EXISTS as alternative to IN with subqueries.

---

# Summary and Interview Preparation

## Quick Reference Table

| Category | Common Mistakes | Key Prevention |
|----------|----------------|----------------|
| Syntax | Missing semicolons, wrong clause order | Always use; SELECT-FROM-WHERE-GROUP-HAVING-ORDER |
| NULL | = NULL, IN with NULL, aggregate ignoring | Use IS NULL, COALESCE, understand NULL semantics |
| JOINs | Cartesian, wrong join type, ambiguous columns | Always use ON, qualify columns, choose LEFT/INNER carefully |
| Aggregation | Non-grouped columns, WHERE vs HAVING | All SELECT columns in GROUP BY or aggregate |
| Performance | SELECT *, functions on indexes, N+1 | Specific columns, clean indexes, batch queries |
| Data Modification | No WHERE, no transaction | SELECT first, always WHERE, always transaction |

## Interview Checklist

Before submitting any SQL query in an interview:

1. **Syntax**: Semicolon included? Clauses in correct order?
2. **Columns**: All qualified? All in GROUP BY or aggregated?
3. **NULLs**: Using IS NULL? COALESCE where needed?
4. **JOINs**: All have ON conditions? Correct join type for requirements?
5. **Performance**: Specific columns? Functions off indexed columns?
6. **Safety**: WHERE clause present? Would you use a transaction?

## Practice Problems

To master these concepts, practice queries that combine multiple challenges:

1. "Find customers with no orders" (NULL, LEFT JOIN)
2. "Average order value by month" (Aggregation, date functions)
3. "Top 10 products by revenue" (Aggregation, performance)
4. "Update prices for discontinued products" (Safe UPDATE)
5. "Find duplicate emails" (GROUP BY, HAVING)

Remember: In interviews, showing awareness of potential issues is as valuable as writing correct queries. Verbalize your thought process!
