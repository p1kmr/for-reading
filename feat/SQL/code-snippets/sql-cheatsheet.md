# SQL Quick Reference Cheat Sheet

## Table of Contents
- [Query Structure](#query-structure)
- [SELECT Basics](#select-basics)
- [Filtering Data](#filtering-data)
- [Aggregate Functions](#aggregate-functions)
- [JOINs](#joins)
- [Subqueries](#subqueries)
- [Window Functions](#window-functions)
- [String Functions](#string-functions)
- [Date Functions](#date-functions)
- [NULL Handling](#null-handling)
- [CASE Statements](#case-statements)
- [Common Patterns](#common-patterns)

---

## Query Structure

### Execution Order
```
FROM    → Source tables
WHERE   → Filter rows
GROUP BY → Group rows
HAVING  → Filter groups
SELECT  → Choose columns
ORDER BY → Sort results
LIMIT   → Restrict count
```

### Basic Syntax
```sql
SELECT column1, column2
FROM table_name
WHERE condition
GROUP BY column1
HAVING aggregate_condition
ORDER BY column1 DESC
LIMIT 10;
```

---

## SELECT Basics

### Select Columns
```sql
SELECT * FROM table;                    -- All columns
SELECT col1, col2 FROM table;           -- Specific columns
SELECT col AS alias FROM table;         -- With alias
SELECT DISTINCT col FROM table;         -- Unique values
```

### Sorting & Limiting
```sql
ORDER BY col ASC;                       -- Ascending (default)
ORDER BY col DESC;                      -- Descending
ORDER BY col1, col2 DESC;               -- Multiple columns
LIMIT 10;                               -- First 10 rows
LIMIT 10 OFFSET 20;                     -- Skip 20, get 10
```

---

## Filtering Data

### Comparison Operators
```sql
WHERE col = value                       -- Equal
WHERE col != value                      -- Not equal
WHERE col <> value                      -- Not equal (alt)
WHERE col > value                       -- Greater than
WHERE col >= value                      -- Greater or equal
WHERE col < value                       -- Less than
WHERE col <= value                      -- Less or equal
```

### Logical Operators
```sql
WHERE cond1 AND cond2                   -- Both true
WHERE cond1 OR cond2                    -- Either true
WHERE NOT condition                     -- Negation
WHERE (cond1 OR cond2) AND cond3        -- With parentheses
```

### Range & List
```sql
WHERE col BETWEEN 10 AND 20             -- Range (inclusive)
WHERE col IN (1, 2, 3)                  -- In list
WHERE col NOT IN (1, 2, 3)              -- Not in list
```

### Pattern Matching
```sql
WHERE col LIKE 'A%'                     -- Starts with A
WHERE col LIKE '%z'                     -- Ends with z
WHERE col LIKE '%word%'                 -- Contains word
WHERE col LIKE '_a%'                    -- 2nd char is a
WHERE col LIKE '____'                   -- Exactly 4 chars
```

---

## Aggregate Functions

### Basic Aggregates
```sql
COUNT(*)                                -- Count all rows
COUNT(column)                           -- Count non-NULL
COUNT(DISTINCT column)                  -- Count unique
SUM(column)                             -- Total
AVG(column)                             -- Average
MIN(column)                             -- Minimum
MAX(column)                             -- Maximum
```

### GROUP BY & HAVING
```sql
SELECT department, COUNT(*), AVG(salary)
FROM employees
GROUP BY department
HAVING COUNT(*) > 5;                    -- Filter groups
```

### Multiple Aggregates
```sql
SELECT
    department,
    COUNT(*) AS total,
    AVG(salary) AS avg_salary,
    MAX(salary) AS max_salary
FROM employees
GROUP BY department;
```

---

## JOINs

### Join Types Visual
```
INNER JOIN:    A ∩ B (only matching)
LEFT JOIN:     A (all A + matching B)
RIGHT JOIN:    B (all B + matching A)
FULL JOIN:     A ∪ B (all from both)
```

### INNER JOIN
```sql
SELECT e.name, d.name
FROM employees e
INNER JOIN departments d ON e.dept_id = d.id;
```

### LEFT JOIN
```sql
SELECT e.name, d.name
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.id;
-- Includes employees without departments (NULL)
```

### Multiple JOINs
```sql
SELECT e.name, d.name, p.name
FROM employees e
JOIN departments d ON e.dept_id = d.id
JOIN projects p ON e.proj_id = p.id;
```

### SELF JOIN
```sql
-- Employee with manager name
SELECT e.name, m.name AS manager
FROM employees e
LEFT JOIN employees m ON e.manager_id = m.id;
```

### Find Unmatched
```sql
-- Employees without department
SELECT e.name
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.id
WHERE d.id IS NULL;
```

---

## Subqueries

### Subquery in WHERE
```sql
-- Employees earning above average
SELECT name, salary
FROM employees
WHERE salary > (SELECT AVG(salary) FROM employees);
```

### Subquery with IN
```sql
-- Employees in departments with budget > 100k
SELECT name
FROM employees
WHERE dept_id IN (
    SELECT id FROM departments WHERE budget > 100000
);
```

### EXISTS
```sql
-- Departments with employees
SELECT name
FROM departments d
WHERE EXISTS (
    SELECT 1 FROM employees e WHERE e.dept_id = d.id
);
```

### Correlated Subquery
```sql
-- Employees earning more than dept average
SELECT name, salary
FROM employees e1
WHERE salary > (
    SELECT AVG(salary)
    FROM employees e2
    WHERE e2.dept_id = e1.dept_id
);
```

### CTE (Common Table Expression)
```sql
WITH dept_stats AS (
    SELECT dept_id, AVG(salary) AS avg_sal
    FROM employees
    GROUP BY dept_id
)
SELECT e.name, e.salary, d.avg_sal
FROM employees e
JOIN dept_stats d ON e.dept_id = d.dept_id;
```

---

## Window Functions

### Ranking Functions
```sql
ROW_NUMBER() OVER (ORDER BY col)        -- 1,2,3,4,5
RANK() OVER (ORDER BY col)              -- 1,2,2,4,5 (gaps)
DENSE_RANK() OVER (ORDER BY col)        -- 1,2,2,3,4 (no gaps)
NTILE(4) OVER (ORDER BY col)            -- Quartiles
```

### With PARTITION BY
```sql
-- Rank within department
ROW_NUMBER() OVER (
    PARTITION BY department
    ORDER BY salary DESC
)
```

### Value Functions
```sql
LAG(col, 1) OVER (ORDER BY date)        -- Previous row
LEAD(col, 1) OVER (ORDER BY date)       -- Next row
FIRST_VALUE(col) OVER (ORDER BY date)   -- First in window
LAST_VALUE(col) OVER (...)              -- Last in window
```

### Running Calculations
```sql
-- Running total
SUM(amount) OVER (ORDER BY date)

-- Moving average (3 rows)
AVG(amount) OVER (
    ORDER BY date
    ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
)
```

### Common Window Patterns
```sql
-- Top N per group
WITH ranked AS (
    SELECT *,
        ROW_NUMBER() OVER (
            PARTITION BY department
            ORDER BY salary DESC
        ) AS rn
    FROM employees
)
SELECT * FROM ranked WHERE rn <= 3;

-- Year-over-year comparison
SELECT
    month,
    sales,
    LAG(sales) OVER (ORDER BY month) AS prev_month,
    sales - LAG(sales) OVER (ORDER BY month) AS diff
FROM monthly_sales;
```

---

## String Functions

```sql
CONCAT(str1, str2)                      -- Combine strings
CONCAT_WS('-', a, b, c)                 -- With separator
SUBSTRING(str, start, len)              -- Extract part
LEFT(str, n)                            -- First n chars
RIGHT(str, n)                           -- Last n chars
LENGTH(str)                             -- String length
UPPER(str)                              -- Uppercase
LOWER(str)                              -- Lowercase
TRIM(str)                               -- Remove spaces
LTRIM(str) / RTRIM(str)                 -- Left/right trim
REPLACE(str, old, new)                  -- Replace text
LOCATE(substr, str)                     -- Find position
```

---

## Date Functions

### Current Date/Time
```sql
NOW()                                   -- Current datetime
CURDATE()                               -- Current date
CURTIME()                               -- Current time
```

### Extract Parts
```sql
YEAR(date)                              -- Extract year
MONTH(date)                             -- Extract month
DAY(date)                               -- Extract day
HOUR(datetime)                          -- Extract hour
DAYNAME(date)                           -- Day name
MONTHNAME(date)                         -- Month name
QUARTER(date)                           -- Quarter (1-4)
```

### Date Arithmetic
```sql
DATE_ADD(date, INTERVAL 7 DAY)          -- Add days
DATE_SUB(date, INTERVAL 1 MONTH)        -- Subtract
DATEDIFF(date1, date2)                  -- Days between
TIMESTAMPDIFF(MONTH, d1, d2)            -- Months between
```

### Formatting
```sql
DATE_FORMAT(date, '%Y-%m-%d')           -- 2024-01-15
DATE_FORMAT(date, '%M %d, %Y')          -- January 15, 2024
DATE_FORMAT(date, '%W')                 -- Monday
```

---

## NULL Handling

### Checking NULL
```sql
WHERE col IS NULL                       -- Is null
WHERE col IS NOT NULL                   -- Is not null
-- NEVER: WHERE col = NULL              -- Wrong!
```

### NULL Functions
```sql
COALESCE(col, default)                  -- First non-null
COALESCE(a, b, c, 0)                    -- Multiple fallbacks
IFNULL(col, default)                    -- MySQL specific
NULLIF(a, b)                            -- NULL if equal
```

### NULL in Aggregates
```sql
COUNT(*)                                -- Counts NULLs
COUNT(column)                           -- Excludes NULLs
SUM/AVG/MIN/MAX                         -- Ignore NULLs
```

---

## CASE Statements

### Simple CASE
```sql
CASE status
    WHEN 'A' THEN 'Active'
    WHEN 'I' THEN 'Inactive'
    ELSE 'Unknown'
END AS status_text
```

### Searched CASE
```sql
CASE
    WHEN salary >= 100000 THEN 'High'
    WHEN salary >= 50000 THEN 'Medium'
    ELSE 'Low'
END AS salary_band
```

### CASE in Aggregates
```sql
SELECT
    SUM(CASE WHEN status = 'Active' THEN 1 ELSE 0 END) AS active,
    SUM(CASE WHEN status = 'Inactive' THEN 1 ELSE 0 END) AS inactive
FROM employees;
```

---

## Common Patterns

### Find Duplicates
```sql
SELECT email, COUNT(*)
FROM users
GROUP BY email
HAVING COUNT(*) > 1;
```

### Second Highest Salary
```sql
-- Method 1: Subquery
SELECT MAX(salary) FROM employees
WHERE salary < (SELECT MAX(salary) FROM employees);

-- Method 2: Window function
SELECT salary FROM (
    SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rn
    FROM employees
) t WHERE rn = 2;
```

### Top N per Group
```sql
WITH ranked AS (
    SELECT *, ROW_NUMBER() OVER (
        PARTITION BY category ORDER BY sales DESC
    ) AS rn
    FROM products
)
SELECT * FROM ranked WHERE rn <= 3;
```

### Consecutive Days
```sql
SELECT id, date,
    date - ROW_NUMBER() OVER (ORDER BY date) AS grp
FROM logins;
-- Same grp = consecutive
```

### Running Total
```sql
SELECT date, amount,
    SUM(amount) OVER (ORDER BY date) AS running_total
FROM sales;
```

### Gap Analysis
```sql
SELECT
    date,
    LEAD(date) OVER (ORDER BY date) AS next_date,
    DATEDIFF(LEAD(date) OVER (ORDER BY date), date) AS gap
FROM orders;
```

### Pivot Data
```sql
SELECT
    product,
    SUM(CASE WHEN month = 'Jan' THEN amount ELSE 0 END) AS jan,
    SUM(CASE WHEN month = 'Feb' THEN amount ELSE 0 END) AS feb,
    SUM(CASE WHEN month = 'Mar' THEN amount ELSE 0 END) AS mar
FROM sales
GROUP BY product;
```

### Cumulative Percentage
```sql
SELECT
    product,
    sales,
    SUM(sales) OVER (ORDER BY sales DESC) AS cumulative,
    SUM(sales) OVER (ORDER BY sales DESC) * 100.0 /
        SUM(sales) OVER () AS pct
FROM products;
```

---

## Interview Quick Tips

### Remember
- Always consider NULL cases
- Use table aliases for readability
- GROUP BY all non-aggregated columns
- HAVING filters after GROUP BY
- Window functions can't be in WHERE

### Common Traps
- `NULL = NULL` → Unknown (use IS NULL)
- `IN (NULL)` → Won't match NULL values
- `BETWEEN` is inclusive
- `LIKE '%'` doesn't match NULL
- Division by zero

### Query Checklist
- [ ] Correct JOINs with proper conditions
- [ ] All columns in GROUP BY
- [ ] NULLs handled
- [ ] ORDER BY if needed
- [ ] LIMIT for performance

---

**Good luck with your interviews!**
