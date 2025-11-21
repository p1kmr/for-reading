# Phase 3: Filtering & Sorting Data

## Table of Contents
- [Introduction to Filtering](#introduction-to-filtering)
- [Logical Operators (AND, OR, NOT)](#logical-operators)
- [IN Operator](#in-operator)
- [BETWEEN Operator](#between-operator)
- [LIKE Operator & Pattern Matching](#like-operator--pattern-matching)
- [NULL Handling](#null-handling)
- [Comparison Operators](#comparison-operators)
- [Combining Multiple Conditions](#combining-multiple-conditions)
- [Practice Questions](#practice-questions)
- [Common Mistakes](#common-mistakes)

---

## Introduction to Filtering

### WHY
Filtering is crucial for finding specific data in large datasets. Real-world scenarios:
- Find all orders from last month
- Search for users in specific cities
- Filter products within a price range
- Find employees earning above a threshold
- Search for names matching a pattern

80% of SQL queries involve filtering. Mastering WHERE conditions is essential for:
- Data analysis
- Report generation
- Application development
- Interview success

### WHAT
Filtering uses the WHERE clause to select only rows that meet specific conditions.

### Query Execution Order
```
FROM    →  Start with table
WHERE   →  Filter rows (what we're learning!)
SELECT  →  Choose columns
ORDER BY →  Sort results
LIMIT   →  Restrict count
```

---

## Logical Operators

### AND Operator

**WHY**: Combine multiple conditions where ALL must be true.

**WHAT**: Returns rows only if ALL conditions are TRUE.

**WHEN**: Use when you need to narrow results with multiple requirements.

#### Example 1: AND with Two Conditions
```sql
-- Find employees in Engineering earning over $90,000
SELECT first_name, last_name, department_id, salary
FROM employees
WHERE department_id = 1 AND salary > 90000;
```

**Truth Table:**
```
Condition 1  │  Condition 2  │  Result
─────────────┼───────────────┼─────────
   TRUE      │     TRUE      │   TRUE   ✓
   TRUE      │     FALSE     │   FALSE
   FALSE     │     TRUE      │   FALSE
   FALSE     │     FALSE     │   FALSE
```

#### Example 2: Multiple AND Conditions
```sql
-- Find high-value recent sales
SELECT product, amount, sale_date
FROM sales
WHERE amount > 500
  AND sale_date >= '2023-01-01'
  AND category = 'Electronics';
```

---

### OR Operator

**WHY**: Find rows matching ANY of several conditions.

**WHAT**: Returns rows if AT LEAST ONE condition is TRUE.

**WHEN**: Use when you have alternative criteria.

#### Example 3: OR with Two Conditions
```sql
-- Find employees in Engineering OR Marketing
SELECT first_name, last_name, department_id
FROM employees
WHERE department_id = 1 OR department_id = 2;
```

**Truth Table:**
```
Condition 1  │  Condition 2  │  Result
─────────────┼───────────────┼─────────
   TRUE      │     TRUE      │   TRUE   ✓
   TRUE      │     FALSE     │   TRUE   ✓
   FALSE     │     TRUE      │   TRUE   ✓
   FALSE     │     FALSE     │   FALSE
```

#### Example 4: Multiple OR Conditions
```sql
-- Find customers from multiple countries
SELECT first_name, last_name, country
FROM customers
WHERE country = 'USA'
   OR country = 'Canada'
   OR country = 'UK';
```

---

### NOT Operator

**WHY**: Exclude specific conditions.

**WHAT**: Reverses/negates a condition.

**WHEN**: Use to exclude certain values or patterns.

#### Example 5: NOT with Equality
```sql
-- Find all employees NOT in department 1
SELECT first_name, last_name, department_id
FROM employees
WHERE NOT department_id = 1;

-- Alternative (preferred)
SELECT first_name, last_name, department_id
FROM employees
WHERE department_id != 1;  -- or department_id <> 1
```

#### Example 6: NOT with IN
```sql
-- Find customers not from USA, Canada, or UK
SELECT first_name, last_name, country
FROM customers
WHERE country NOT IN ('USA', 'Canada', 'UK');
```

---

### Combining AND, OR, NOT

#### Example 7: AND with OR (Need Parentheses!)
```sql
-- Find employees in Engineering OR Marketing earning over $80,000
-- WRONG WAY (without parentheses):
SELECT * FROM employees
WHERE department_id = 1 OR department_id = 2 AND salary > 80000;
-- This is parsed as: dept=1 OR (dept=2 AND salary>80000)

-- CORRECT WAY (with parentheses):
SELECT * FROM employees
WHERE (department_id = 1 OR department_id = 2)
  AND salary > 80000;
```

**Logical Flow Diagram:**
```
┌────────────────────────────────────────┐
│  (dept=1 OR dept=2) AND salary>80000   │
└───────────┬────────────────────────────┘
            │
    ┌───────┴────────┐
    ▼                ▼
┌────────┐      ┌─────────┐
│dept=1  │      │salary   │
│  OR    │  AND │ > 80000 │
│dept=2  │      │         │
└────────┘      └─────────┘
   Both must be TRUE
```

#### Example 8: Complex Combination
```sql
-- Find high-value sales from USA or Canada in Electronics or Furniture
SELECT product, category, amount, country
FROM sales s
JOIN customers c ON s.customer_id = c.id
WHERE (c.country = 'USA' OR c.country = 'Canada')
  AND (s.category = 'Electronics' OR s.category = 'Furniture')
  AND s.amount > 100;
```

---

## IN Operator

### WHY
IN simplifies OR conditions with the same column. Instead of writing multiple ORs, use one IN.

### WHAT
Checks if a value exists in a list of values.

### HOW
```sql
column_name IN (value1, value2, value3, ...)
```

### WHEN
Use when checking if a column matches any value in a list.

---

### Example 9: IN with Values
```sql
-- Employees from specific departments
SELECT first_name, last_name, department_id
FROM employees
WHERE department_id IN (1, 2, 3);

-- Equivalent to:
-- WHERE department_id = 1 OR department_id = 2 OR department_id = 3
```

### Example 10: IN with Strings
```sql
-- Customers from specific countries
SELECT first_name, last_name, country
FROM customers
WHERE country IN ('USA', 'Canada', 'UK', 'France');
```

### Example 11: NOT IN
```sql
-- Customers NOT from these countries
SELECT first_name, last_name, country
FROM customers
WHERE country NOT IN ('USA', 'Canada');
```

### Example 12: IN with Subquery (Preview)
```sql
-- Employees working on active projects
SELECT first_name, last_name
FROM employees
WHERE id IN (
    SELECT employee_id
    FROM employee_projects
    WHERE project_id IN (SELECT id FROM projects WHERE status = 'Active')
);
```

**Performance Note:**
- IN is usually faster than multiple ORs
- For large lists, consider temporary tables or joins

---

## BETWEEN Operator

### WHY
BETWEEN simplifies range queries. Instead of `>= AND <=`, use BETWEEN.

### WHAT
Checks if a value falls within a range (inclusive).

### HOW
```sql
column_name BETWEEN value1 AND value2
-- Equivalent to: column_name >= value1 AND column_name <= value2
```

### WHEN
Use for date ranges, salary ranges, price ranges, etc.

---

### Example 13: BETWEEN with Numbers
```sql
-- Employees earning between $80,000 and $100,000
SELECT first_name, last_name, salary
FROM employees
WHERE salary BETWEEN 80000 AND 100000;

-- Equivalent to:
-- WHERE salary >= 80000 AND salary <= 100000
```

### Example 14: BETWEEN with Dates
```sql
-- Sales in Q1 2023
SELECT product, amount, sale_date
FROM sales
WHERE sale_date BETWEEN '2023-01-01' AND '2023-03-31';
```

### Example 15: NOT BETWEEN
```sql
-- Employees NOT in middle salary range
SELECT first_name, last_name, salary
FROM employees
WHERE salary NOT BETWEEN 70000 AND 100000;
```

### Example 16: BETWEEN with Strings (Alphabetical)
```sql
-- Customers with last names from A to M
SELECT first_name, last_name
FROM customers
WHERE last_name BETWEEN 'A' AND 'M';
```

**Important Notes:**
- BETWEEN is inclusive (includes both endpoints)
- Order matters: BETWEEN lower AND higher
- Works with numbers, dates, and strings

---

## LIKE Operator & Pattern Matching

### WHY
LIKE enables pattern matching and partial string searches. Essential for:
- Search functionality
- Finding names with specific patterns
- Email domain searches
- Flexible filtering

### WHAT
LIKE matches strings against patterns using wildcards:
- `%` : Matches zero or more characters
- `_` : Matches exactly one character

### WHEN
Use for partial matches, patterns, and searches.

---

### Wildcard `%` - Zero or More Characters

#### Example 17: Starts With Pattern
```sql
-- Find employees whose first name starts with 'J'
SELECT first_name, last_name
FROM employees
WHERE first_name LIKE 'J%';
-- Matches: John, Jane, James, etc.
```

#### Example 18: Ends With Pattern
```sql
-- Find emails ending with '@company.com'
SELECT first_name, last_name, email
FROM employees
WHERE email LIKE '%@company.com';
```

#### Example 19: Contains Pattern
```sql
-- Find products containing 'Laptop'
SELECT name, price
FROM products
WHERE name LIKE '%Laptop%';
-- Matches: 'Laptop Pro', 'Gaming Laptop', 'Laptop', etc.
```

#### Example 20: NOT LIKE
```sql
-- Find customers whose email is NOT Gmail
SELECT first_name, email
FROM customers
WHERE email NOT LIKE '%@gmail.com';
```

---

### Wildcard `_` - Exactly One Character

#### Example 21: Specific Position Wildcard
```sql
-- Find names where 2nd letter is 'a'
SELECT first_name
FROM employees
WHERE first_name LIKE '_a%';
-- Matches: Jane, James, David, etc.
```

#### Example 22: Multiple Underscores
```sql
-- Find 4-letter first names
SELECT first_name
FROM employees
WHERE first_name LIKE '____';
-- Matches: John, Mike, etc. (exactly 4 characters)
```

#### Example 23: Combining Wildcards
```sql
-- Find products: starts with 'L', 6+ characters, has 'p' as 3rd letter
SELECT name
FROM products
WHERE name LIKE 'L_p%';
-- Matches: Laptop, Laptop Pro, etc.
```

---

### LIKE with ESCAPE (Special Characters)

#### Example 24: Searching for Literal % or _
```sql
-- Find products with actual % in name (like "50% Off")
SELECT name
FROM products
WHERE name LIKE '%\%%' ESCAPE '\';
-- The \% means literal %, not wildcard
```

---

### Case Sensitivity in LIKE

**MySQL**: LIKE is case-insensitive by default
```sql
-- Finds 'john', 'John', 'JOHN'
WHERE first_name LIKE 'john%';
```

**PostgreSQL**: LIKE is case-sensitive
```sql
-- Case-insensitive version
WHERE first_name ILIKE 'john%';
```

---

## NULL Handling

### WHY
NULL represents missing or unknown data. Proper NULL handling prevents bugs and incorrect results.

### WHAT
NULL is NOT a value - it's a marker for "no value". Key facts:
- NULL ≠ 0
- NULL ≠ empty string ''
- NULL ≠ NULL (even NULL doesn't equal itself!)

### Common NULL Scenarios
- Optional fields (middle name, phone)
- Missing data (unknown salary)
- Not applicable (end_date for ongoing projects)

---

### NULL Comparison Operators

**NEVER use = or != with NULL**

❌ **Wrong:**
```sql
WHERE email = NULL    -- Always returns 0 rows!
WHERE email != NULL   -- Always returns 0 rows!
```

✅ **Correct:**
```sql
WHERE email IS NULL       -- Finds NULL values
WHERE email IS NOT NULL   -- Finds non-NULL values
```

---

### Example 25: Find NULL Values
```sql
-- Employees without assigned department
SELECT first_name, last_name, department_id
FROM employees
WHERE department_id IS NULL;
```

### Example 26: Find Non-NULL Values
```sql
-- Customers with phone numbers
SELECT first_name, last_name, phone
FROM customers
WHERE phone IS NOT NULL;
```

### Example 27: NULL in Calculations
```sql
-- NULL in arithmetic results in NULL
SELECT
    first_name,
    salary,
    bonus,
    salary + bonus AS total  -- If bonus is NULL, total is NULL
FROM employees;

-- Solution: Use COALESCE or IFNULL
SELECT
    first_name,
    salary,
    COALESCE(bonus, 0) AS bonus,
    salary + COALESCE(bonus, 0) AS total
FROM employees;
```

### Example 28: NULL with AND/OR

**NULL in logic:**
```
TRUE AND NULL   = NULL
FALSE AND NULL  = FALSE
TRUE OR NULL    = TRUE
FALSE OR NULL   = NULL
NOT NULL        = NULL
```

```sql
-- This might not work as expected if bonus is NULL
SELECT * FROM employees
WHERE salary > 80000 AND bonus > 5000;  -- Excludes NULL bonus

-- Better approach
SELECT * FROM employees
WHERE salary > 80000 AND (bonus > 5000 OR bonus IS NULL);
```

---

## Comparison Operators

### All Comparison Operators

| Operator | Description | Example |
|----------|-------------|---------|
| `=` | Equal to | `salary = 100000` |
| `!=` or `<>` | Not equal to | `status != 'Inactive'` |
| `>` | Greater than | `salary > 80000` |
| `<` | Less than | `price < 100` |
| `>=` | Greater than or equal | `hire_date >= '2023-01-01'` |
| `<=` | Less than or equal | `age <= 30` |

### Example 29: All Operators in Action
```sql
-- Complex filtering with various operators
SELECT
    product,
    category,
    price,
    quantity
FROM products
WHERE category = 'Electronics'           -- Equal
  AND price >= 100                       -- Greater or equal
  AND price <= 1000                      -- Less or equal
  AND quantity > 0                       -- Greater than
  AND status <> 'Discontinued';          -- Not equal
```

---

## Combining Multiple Conditions

### Example 30: Real-World Complex Query
```sql
-- Find valuable active customers who made recent purchases
SELECT
    c.first_name,
    c.last_name,
    c.email,
    c.country,
    c.registration_date
FROM customers c
WHERE c.status = 'Active'                          -- Active customers
  AND c.country IN ('USA', 'Canada', 'UK')        -- Specific countries
  AND c.registration_date >= '2022-01-01'         -- Registered recently
  AND c.id IN (                                    -- Have made purchases
      SELECT customer_id
      FROM sales
      WHERE sale_date >= '2023-01-01'             -- In 2023
        AND amount > 100                          -- High value
  )
ORDER BY c.registration_date DESC;
```

---

## Practice Questions

### Medium Question 1: Multiple Conditions

**Question**: Find all employees who:
- Work in Engineering (department_id = 1) OR Sales (department_id = 3)
- Earn between $70,000 and $110,000
- Were hired in 2020 or later

**Difficulty**: Medium

**Approach**: Combine IN, BETWEEN, and date comparison.

**Solution**:
```sql
SELECT
    first_name,
    last_name,
    department_id,
    salary,
    hire_date
FROM employees
WHERE department_id IN (1, 3)
  AND salary BETWEEN 70000 AND 110000
  AND hire_date >= '2020-01-01'
ORDER BY hire_date DESC;
```

**Explanation**:
- `IN (1, 3)`: Engineering or Sales
- `BETWEEN`: Salary range (inclusive)
- `>=`: Date comparison
- All three conditions must be TRUE (implicit AND)

---

### Medium Question 2: Pattern Matching

**Question**: Find all customers whose:
- Email ends with '.com'
- First name starts with 'A', 'E', or 'J'
- Are from 'USA' or 'UK'

**Difficulty**: Medium

**Approach**: Combine LIKE with OR and IN.

**Solution**:
```sql
SELECT
    first_name,
    last_name,
    email,
    country
FROM customers
WHERE email LIKE '%.com'
  AND (
      first_name LIKE 'A%'
      OR first_name LIKE 'E%'
      OR first_name LIKE 'J%'
  )
  AND country IN ('USA', 'UK');
```

**Alternative Solution** (using REGEXP if supported):
```sql
SELECT
    first_name,
    last_name,
    email,
    country
FROM customers
WHERE email LIKE '%.com'
  AND first_name REGEXP '^[AEJ]'
  AND country IN ('USA', 'UK');
```

---

### Medium Question 3: NULL Handling

**Question**: Find all employees who either:
- Don't have a manager assigned (manager_id IS NULL)
- OR have a salary above $100,000

Then sort by salary descending.

**Difficulty**: Medium

**Approach**: Combine IS NULL with OR condition.

**Solution**:
```sql
SELECT
    first_name,
    last_name,
    manager_id,
    salary
FROM employees
WHERE manager_id IS NULL
   OR salary > 100000
ORDER BY salary DESC;
```

**Explanation**:
- `IS NULL`: Finds employees without managers
- `OR`: Either condition can be true
- No parentheses needed (same column not involved in complex logic)

---

### Hard Question 4: Complex Filtering

**Question**: Find all sales where:
- Product category is either 'Electronics' OR 'Furniture'
- Sale amount is NOT between $50 and $100
- Sale was made in Q1 2023 (Jan-Mar)
- Customer is from 'USA', 'Canada', or 'UK'

**Difficulty**: Hard

**Approach**: Multiple conditions with NOT BETWEEN, IN, date range, and JOIN.

**Solution**:
```sql
SELECT
    s.product,
    s.category,
    s.amount,
    s.sale_date,
    c.country
FROM sales s
JOIN customers c ON s.customer_id = c.id
WHERE s.category IN ('Electronics', 'Furniture')
  AND s.amount NOT BETWEEN 50 AND 100
  AND s.sale_date BETWEEN '2023-01-01' AND '2023-03-31'
  AND c.country IN ('USA', 'Canada', 'UK')
ORDER BY s.sale_date, s.amount DESC;
```

**Verification Query**:
```sql
-- Test the NOT BETWEEN logic
SELECT amount FROM sales WHERE amount NOT BETWEEN 50 AND 100;
-- Should show: <50 or >100
```

---

### Hard Question 5: Advanced Pattern Search

**Question**: Find employees where:
- Last name contains 'son' OR 'sen'
- Email domain is NOT 'gmail.com'
- Hired between 2020 and 2022
- Department is not NULL

**Difficulty**: Hard

**Approach**: Multiple LIKE conditions with AND/OR.

**Solution**:
```sql
SELECT
    first_name,
    last_name,
    email,
    hire_date,
    department_id
FROM employees
WHERE (last_name LIKE '%son%' OR last_name LIKE '%sen%')
  AND email NOT LIKE '%@gmail.com'
  AND hire_date BETWEEN '2020-01-01' AND '2022-12-31'
  AND department_id IS NOT NULL
ORDER BY last_name;
```

**Alternative Solutions**:
```sql
-- Using REGEXP (MySQL/PostgreSQL)
SELECT first_name, last_name, email, hire_date
FROM employees
WHERE last_name REGEXP 'son|sen'
  AND email NOT LIKE '%@gmail.com'
  AND YEAR(hire_date) BETWEEN 2020 AND 2022
  AND department_id IS NOT NULL;
```

---

## Common Mistakes

### Mistake 1: NULL Comparison with = or !=

❌ **Wrong:**
```sql
SELECT * FROM employees
WHERE manager_id = NULL;  -- Returns 0 rows always!
```

✅ **Correct:**
```sql
SELECT * FROM employees
WHERE manager_id IS NULL;
```

💡 **Why it matters**: `NULL = NULL` is NULL (unknown), not TRUE.

---

### Mistake 2: Missing Parentheses with AND/OR

❌ **Wrong:**
```sql
-- Intended: (dept 1 or 2) AND salary > 80000
SELECT * FROM employees
WHERE department_id = 1 OR department_id = 2 AND salary > 80000;
-- Actually means: dept=1 OR (dept=2 AND salary>80000)
```

✅ **Correct:**
```sql
SELECT * FROM employees
WHERE (department_id = 1 OR department_id = 2)
  AND salary > 80000;
```

💡 **Why it matters**: AND has higher precedence than OR!

**Precedence Order** (high to low):
1. Parentheses `()`
2. NOT
3. AND
4. OR

---

### Mistake 3: LIKE Pattern Errors

❌ **Wrong:**
```sql
-- Trying to find names starting with 'J'
WHERE first_name LIKE 'J';    -- Finds only exactly 'J'
WHERE first_name LIKE '%J%';  -- Finds J anywhere, not just start
```

✅ **Correct:**
```sql
WHERE first_name LIKE 'J%';   -- Starts with J
```

**Common Patterns:**
```
'J%'      → Starts with J      (John, Jane)
'%son'    → Ends with son      (Johnson, Wilson)
'%son%'   → Contains son       (Johnson, Tyson)
'J___'    → J + 3 chars        (John, Jane)
```

---

### Mistake 4: BETWEEN Backwards

❌ **Wrong:**
```sql
-- Backwards range
WHERE salary BETWEEN 100000 AND 50000;  -- Returns 0 rows
```

✅ **Correct:**
```sql
WHERE salary BETWEEN 50000 AND 100000;  -- Lower first, higher second
```

---

### Mistake 5: Case Sensitivity Confusion

❌ **Potentially Wrong:**
```sql
-- Might miss 'john@email.com' if database is case-sensitive
WHERE email = 'JOHN@email.com';
```

✅ **Correct (case-insensitive search):**
```sql
-- MySQL
WHERE LOWER(email) = LOWER('JOHN@email.com');

-- PostgreSQL
WHERE email ILIKE 'john@email.com';
```

---

### Mistake 6: IN with NULL

❌ **Wrong:**
```sql
-- This won't find NULL values!
WHERE department_id IN (1, 2, NULL);
```

✅ **Correct:**
```sql
WHERE department_id IN (1, 2) OR department_id IS NULL;
```

💡 **Why it matters**: IN doesn't match NULL values.

---

## Summary & Next Steps

### What You Learned
✅ Logical operators: AND, OR, NOT
✅ IN operator for multiple value matching
✅ BETWEEN for range queries
✅ LIKE for pattern matching with wildcards
✅ Proper NULL handling (IS NULL, IS NOT NULL)
✅ All comparison operators
✅ Combining complex conditions
✅ Operator precedence and parentheses

### Key Takeaways

| Concept | Remember |
|---------|----------|
| AND | All conditions must be TRUE |
| OR | At least one condition TRUE |
| IN | Cleaner than multiple ORs |
| BETWEEN | Inclusive range |
| LIKE | `%` = any chars, `_` = one char |
| NULL | Use IS NULL, never = NULL |
| Precedence | () > NOT > AND > OR |

### Pattern Cheat Sheet
```
LIKE 'A%'      → Starts with A
LIKE '%z'      → Ends with z
LIKE '%abc%'   → Contains abc
LIKE 'A%z'     → Starts with A, ends with z
LIKE '_a%'     → 2nd char is a
LIKE '____'    → Exactly 4 characters
NOT LIKE 'A%'  → Doesn't start with A
```

### Practice Exercises
1. Find all products priced between $50 and $200 in Electronics or Furniture
2. Find customers whose last name starts with 'A' through 'M' from USA or Canada
3. Find employees with NULL manager_id OR salary > $100,000
4. Find sales NOT in January, February, or March 2023
5. Find emails ending with '.com' or '.org' but not containing 'test'

### What's Next?
In **Phase 4: Aggregate Functions & Grouping**, you'll learn:
- COUNT, SUM, AVG, MIN, MAX
- GROUP BY for grouping data
- HAVING clause (WHERE for groups)
- Combining aggregates with filtering
- Statistical analysis
- Interview questions on aggregation

---

**Ready to master aggregates? Let's go to Phase 4!** 🚀
