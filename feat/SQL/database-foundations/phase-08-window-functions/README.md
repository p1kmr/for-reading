# Phase 8: Window Functions & Analytics

## Table of Contents
- [Introduction to Window Functions](#introduction-to-window-functions)
- [Sample Tables](#sample-tables)
- [1. Ranking Functions](#1-ranking-functions)
- [2. Value Functions](#2-value-functions)
- [3. PARTITION BY Clause](#3-partition-by-clause)
- [4. ORDER BY within OVER Clause](#4-order-by-within-over-clause)
- [5. Frame Specifications](#5-frame-specifications)
- [6. Running Totals and Moving Averages](#6-running-totals-and-moving-averages)
- [Window Frame Visualization](#window-frame-visualization)
- [Code Examples](#code-examples)
- [Practice Questions](#practice-questions)
- [Common Mistakes](#common-mistakes)
- [Summary](#summary)

---

## Introduction to Window Functions

### WHY Use Window Functions?

Window functions solve problems that are difficult or impossible with standard SQL:

- **Calculate rankings** without losing individual row data
- **Compare rows** to previous or next rows in the same result set
- **Compute running totals** and moving averages
- **Find percentiles** and distributions within groups
- **Analyze trends** over time without complex self-joins
- **Avoid multiple scans** of the same data

**Real-World Use Cases:**
- Ranking products by sales within each category
- Calculating year-over-year growth
- Finding the top N performers per department
- Computing 7-day moving averages for stock prices
- Identifying first and last transactions per customer

### WHAT Makes Window Functions Different from GROUP BY?

| Feature | GROUP BY | Window Functions |
|---------|----------|------------------|
| Row preservation | Collapses rows into groups | Keeps all individual rows |
| Result set size | One row per group | Same number of rows as input |
| Access to detail | Only aggregated values | Both aggregate and detail |
| Row comparison | Cannot compare within results | Can compare adjacent rows |
| Multiple calculations | Requires subqueries | Single pass through data |

```sql
-- GROUP BY: Loses individual employee data
SELECT department_id, AVG(salary) AS avg_salary
FROM employees
GROUP BY department_id;

-- Result: 3 rows (one per department)
-- department_id | avg_salary
-- 1             | 89000
-- 2             | 77333
-- 3             | 51500

-- Window Function: Keeps all employees, adds department average
SELECT
    employee_id,
    first_name,
    department_id,
    salary,
    AVG(salary) OVER (PARTITION BY department_id) AS dept_avg_salary
FROM employees;

-- Result: 8 rows (all employees with department average)
-- employee_id | first_name | department_id | salary  | dept_avg_salary
-- 1           | John       | 1             | 75000   | 89000
-- 2           | Sarah      | 1             | 82000   | 89000
-- 6           | Lisa       | 1             | 110000  | 89000
-- ...
```

### HOW Window Functions Work

Window functions use the `OVER()` clause to define the "window" of rows for calculation:

```sql
function_name(arguments) OVER (
    [PARTITION BY partition_expression]
    [ORDER BY sort_expression]
    [frame_clause]
)
```

**Components:**
1. **Function**: The calculation (SUM, AVG, ROW_NUMBER, etc.)
2. **PARTITION BY**: Divides rows into groups (like GROUP BY, but keeps rows)
3. **ORDER BY**: Defines row order within each partition
4. **Frame Clause**: Specifies which rows to include in the calculation

```sql
-- Basic window function anatomy
SELECT
    employee_id,
    department_id,
    salary,
    SUM(salary) OVER (                          -- Function
        PARTITION BY department_id              -- Grouping
        ORDER BY salary DESC                    -- Ordering
        ROWS BETWEEN UNBOUNDED PRECEDING        -- Frame start
        AND CURRENT ROW                         -- Frame end
    ) AS running_total
FROM employees;
```

### WHEN to Use Window Functions

| Scenario | Example | Why Window Functions |
|----------|---------|---------------------|
| Rankings within groups | Top 3 products per category | ROW_NUMBER with PARTITION BY |
| Running calculations | Cumulative sales by date | SUM with ORDER BY |
| Comparing to previous | Month-over-month change | LAG function |
| Moving averages | 7-day rolling average | AVG with ROWS frame |
| Percentile analysis | Salary quartiles | NTILE or PERCENT_RANK |
| First/Last in group | First purchase per customer | FIRST_VALUE with PARTITION |

---

## Sample Tables

```sql
-- Employees table
CREATE TABLE employees (
    employee_id INT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100),
    hire_date DATE,
    salary DECIMAL(10,2),
    department_id INT,
    manager_id INT,
    phone VARCHAR(20),
    status VARCHAR(20)
);

INSERT INTO employees VALUES
(1, 'John', 'Smith', 'john.smith@company.com', '2020-03-15', 75000.00, 1, NULL, '555-0101', 'active'),
(2, 'Sarah', 'Johnson', 'sarah.j@company.com', '2019-07-22', 82000.00, 1, 1, '555-0102', 'active'),
(3, 'Michael', 'Williams', 'mike.w@company.com', '2021-01-10', 65000.00, 2, 1, '555-0103', 'active'),
(4, 'Emily', 'Brown', 'emily.brown@company.com', '2018-11-30', 95000.00, 2, NULL, '555-0104', 'active'),
(5, 'David', 'Jones', 'david.jones@company.com', '2022-05-18', 55000.00, 3, 4, NULL, 'probation'),
(6, 'Lisa', 'Davis', 'lisa.d@company.com', '2017-02-14', 110000.00, 1, NULL, '555-0106', 'active'),
(7, 'Robert', 'Miller', NULL, '2023-08-01', 48000.00, 3, 4, '555-0107', 'probation'),
(8, 'Jennifer', 'Wilson', 'jen.wilson@company.com', '2020-09-05', 72000.00, 2, 4, '555-0108', 'inactive');

-- Sales table
CREATE TABLE sales (
    sale_id INT PRIMARY KEY,
    employee_id INT,
    sale_date DATE,
    amount DECIMAL(10,2),
    product_category VARCHAR(50),
    region VARCHAR(50),
    customer_id INT
);

INSERT INTO sales VALUES
(1, 1, '2024-01-05', 1500.00, 'Electronics', 'North', 101),
(2, 2, '2024-01-07', 2300.00, 'Electronics', 'South', 102),
(3, 1, '2024-01-10', 800.00, 'Accessories', 'North', 103),
(4, 3, '2024-01-12', 3200.00, 'Electronics', 'East', 104),
(5, 2, '2024-01-15', 1100.00, 'Accessories', 'South', 105),
(6, 4, '2024-01-18', 4500.00, 'Electronics', 'West', 106),
(7, 1, '2024-01-20', 950.00, 'Office', 'North', 107),
(8, 3, '2024-01-22', 2800.00, 'Electronics', 'East', 108),
(9, 2, '2024-01-25', 1750.00, 'Office', 'South', 109),
(10, 4, '2024-01-28', 3100.00, 'Electronics', 'West', 110),
(11, 1, '2024-02-01', 2100.00, 'Electronics', 'North', 111),
(12, 2, '2024-02-03', 1900.00, 'Accessories', 'South', 112),
(13, 3, '2024-02-05', 2400.00, 'Electronics', 'East', 113),
(14, 4, '2024-02-08', 5200.00, 'Electronics', 'West', 114),
(15, 1, '2024-02-10', 1300.00, 'Office', 'North', 115),
(16, 2, '2024-02-12', 2800.00, 'Electronics', 'South', 116),
(17, 3, '2024-02-15', 1600.00, 'Accessories', 'East', 117),
(18, 4, '2024-02-18', 4100.00, 'Electronics', 'West', 118),
(19, 1, '2024-02-20', 900.00, 'Accessories', 'North', 119),
(20, 2, '2024-02-22', 3500.00, 'Electronics', 'South', 120);

-- Customers table
CREATE TABLE customers (
    customer_id INT PRIMARY KEY,
    customer_name VARCHAR(100),
    signup_date DATE,
    customer_tier VARCHAR(20),
    lifetime_value DECIMAL(10,2),
    region VARCHAR(50)
);

INSERT INTO customers VALUES
(101, 'Acme Corp', '2022-01-15', 'Gold', 45000.00, 'North'),
(102, 'Tech Solutions', '2022-03-20', 'Platinum', 120000.00, 'South'),
(103, 'Global Industries', '2022-06-10', 'Silver', 18000.00, 'North'),
(104, 'StartUp Inc', '2022-08-05', 'Gold', 52000.00, 'East'),
(105, 'DataCorp', '2022-10-12', 'Silver', 22000.00, 'South'),
(106, 'InfoTech', '2023-01-08', 'Platinum', 95000.00, 'West'),
(107, 'MegaSoft', '2023-03-15', 'Gold', 67000.00, 'North'),
(108, 'CloudBase', '2023-05-22', 'Silver', 15000.00, 'East'),
(109, 'NetWorks', '2023-07-30', 'Gold', 48000.00, 'South'),
(110, 'SecureData', '2023-09-18', 'Platinum', 88000.00, 'West'),
(111, 'FastTrack', '2023-11-25', 'Silver', 12000.00, 'North'),
(112, 'InnoVate', '2024-01-05', 'Gold', 35000.00, 'South'),
(113, 'DigiPro', '2024-01-20', 'Silver', 8000.00, 'East'),
(114, 'CyberLink', '2024-02-01', 'Platinum', 75000.00, 'West'),
(115, 'SmartSys', '2024-02-10', 'Gold', 28000.00, 'North'),
(116, 'ProData', '2024-02-15', 'Silver', 11000.00, 'South'),
(117, 'TechEdge', '2024-02-18', 'Gold', 42000.00, 'East'),
(118, 'NexGen', '2024-02-20', 'Platinum', 98000.00, 'West'),
(119, 'CoreTech', '2024-02-22', 'Silver', 9500.00, 'North'),
(120, 'DataFlow', '2024-02-25', 'Gold', 55000.00, 'South');
```

---

## 1. Ranking Functions

### ROW_NUMBER()

Assigns a unique sequential integer to each row within a partition.

```sql
-- Assign row numbers to employees by salary within each department
SELECT
    employee_id,
    first_name,
    department_id,
    salary,
    ROW_NUMBER() OVER (
        PARTITION BY department_id
        ORDER BY salary DESC
    ) AS salary_rank
FROM employees;
```

**Result:**
| employee_id | first_name | department_id | salary | salary_rank |
|-------------|------------|---------------|--------|-------------|
| 6 | Lisa | 1 | 110000 | 1 |
| 2 | Sarah | 1 | 82000 | 2 |
| 1 | John | 1 | 75000 | 3 |
| 4 | Emily | 2 | 95000 | 1 |
| 8 | Jennifer | 2 | 72000 | 2 |
| 3 | Michael | 2 | 65000 | 3 |
| 5 | David | 3 | 55000 | 1 |
| 7 | Robert | 3 | 48000 | 2 |

**Key Characteristic:** Always assigns unique numbers (1, 2, 3...) even for ties.

### RANK()

Assigns ranks with gaps for ties.

```sql
-- Rank sales by amount (with gaps for ties)
SELECT
    sale_id,
    employee_id,
    amount,
    RANK() OVER (ORDER BY amount DESC) AS sales_rank
FROM sales;
```

**Example with ties:**
| sale_id | amount | RANK |
|---------|--------|------|
| 14 | 5200 | 1 |
| 6 | 4500 | 2 |
| 18 | 4100 | 3 |
| 20 | 3500 | 4 |
| 4 | 3200 | 5 |
| 10 | 3100 | 6 |

**Key Characteristic:** Ties get the same rank; next rank skips (1, 2, 2, 4).

### DENSE_RANK()

Assigns ranks without gaps for ties.

```sql
-- Dense rank sales by amount (no gaps)
SELECT
    sale_id,
    employee_id,
    amount,
    DENSE_RANK() OVER (ORDER BY amount DESC) AS dense_rank
FROM sales;
```

**Key Characteristic:** Ties get the same rank; next rank is consecutive (1, 2, 2, 3).

### NTILE()

Divides rows into a specified number of approximately equal groups.

```sql
-- Divide employees into 4 salary quartiles
SELECT
    employee_id,
    first_name,
    salary,
    NTILE(4) OVER (ORDER BY salary DESC) AS salary_quartile
FROM employees;
```

**Result:**
| employee_id | first_name | salary | salary_quartile |
|-------------|------------|--------|-----------------|
| 6 | Lisa | 110000 | 1 |
| 4 | Emily | 95000 | 1 |
| 2 | Sarah | 82000 | 2 |
| 1 | John | 75000 | 2 |
| 8 | Jennifer | 72000 | 3 |
| 3 | Michael | 65000 | 3 |
| 5 | David | 55000 | 4 |
| 7 | Robert | 48000 | 4 |

### Ranking Functions Comparison Table

| Function | Ties Handling | Gaps | Use Case |
|----------|---------------|------|----------|
| ROW_NUMBER() | Unique numbers | No gaps | Pagination, deduplication |
| RANK() | Same rank | Gaps after ties | Competition rankings |
| DENSE_RANK() | Same rank | No gaps | Dense rankings needed |
| NTILE(n) | Distributed | N/A | Percentiles, buckets |

```sql
-- Compare all ranking functions side by side
SELECT
    employee_id,
    first_name,
    salary,
    ROW_NUMBER() OVER (ORDER BY salary DESC) AS row_num,
    RANK() OVER (ORDER BY salary DESC) AS rank_val,
    DENSE_RANK() OVER (ORDER BY salary DESC) AS dense_rank,
    NTILE(4) OVER (ORDER BY salary DESC) AS quartile
FROM employees;
```

**Visual comparison with tied salaries:**
```
+-------+--------+--------+----------+------+------------+----------+
| name  | salary | ROW_NUM| RANK     | DENSE| NTILE(3)   | Notes    |
+-------+--------+--------+----------+------+------------+----------+
| Lisa  | 110000 |   1    |    1     |  1   |     1      |          |
| Emily |  95000 |   2    |    2     |  2   |     1      |          |
| Sarah |  82000 |   3    |    3     |  3   |     1      |          |
| John  |  75000 |   4    |    4     |  4   |     2      | Tie      |
| Jen   |  75000 |   5    |    4     |  4   |     2      | Tie      |
| Mike  |  65000 |   6    |    6     |  5   |     2      | Gap here |
| David |  55000 |   7    |    7     |  6   |     3      |          |
| Robert|  48000 |   8    |    8     |  7   |     3      |          |
+-------+--------+--------+----------+------+------------+----------+
```

---

## 2. Value Functions

### LAG()

Accesses data from a previous row within the partition.

```sql
-- LAG(column, offset, default)
SELECT
    sale_id,
    sale_date,
    amount,
    LAG(amount, 1, 0) OVER (ORDER BY sale_date) AS prev_amount,
    amount - LAG(amount, 1, 0) OVER (ORDER BY sale_date) AS change
FROM sales;
```

**Result:**
| sale_id | sale_date | amount | prev_amount | change |
|---------|-----------|--------|-------------|--------|
| 1 | 2024-01-05 | 1500 | 0 | 1500 |
| 2 | 2024-01-07 | 2300 | 1500 | 800 |
| 3 | 2024-01-10 | 800 | 2300 | -1500 |
| 4 | 2024-01-12 | 3200 | 800 | 2400 |

**Parameters:**
- `column`: The column to retrieve
- `offset`: Number of rows back (default: 1)
- `default`: Value when no previous row exists

### LEAD()

Accesses data from a subsequent row within the partition.

```sql
-- LEAD(column, offset, default)
SELECT
    sale_id,
    sale_date,
    amount,
    LEAD(amount, 1, 0) OVER (ORDER BY sale_date) AS next_amount,
    LEAD(sale_date, 1) OVER (ORDER BY sale_date) AS next_sale_date
FROM sales;
```

**Result:**
| sale_id | sale_date | amount | next_amount | next_sale_date |
|---------|-----------|--------|-------------|----------------|
| 1 | 2024-01-05 | 1500 | 2300 | 2024-01-07 |
| 2 | 2024-01-07 | 2300 | 800 | 2024-01-10 |
| ... | ... | ... | ... | ... |
| 20 | 2024-02-22 | 3500 | 0 | NULL |

### FIRST_VALUE()

Returns the first value in an ordered partition.

```sql
-- Get highest salary in each department
SELECT
    employee_id,
    first_name,
    department_id,
    salary,
    FIRST_VALUE(first_name) OVER (
        PARTITION BY department_id
        ORDER BY salary DESC
    ) AS highest_paid_employee,
    FIRST_VALUE(salary) OVER (
        PARTITION BY department_id
        ORDER BY salary DESC
    ) AS highest_salary
FROM employees;
```

**Result:**
| employee_id | first_name | department_id | salary | highest_paid_employee | highest_salary |
|-------------|------------|---------------|--------|----------------------|----------------|
| 6 | Lisa | 1 | 110000 | Lisa | 110000 |
| 2 | Sarah | 1 | 82000 | Lisa | 110000 |
| 1 | John | 1 | 75000 | Lisa | 110000 |
| 4 | Emily | 2 | 95000 | Emily | 95000 |
| 8 | Jennifer | 2 | 72000 | Emily | 95000 |
| 3 | Michael | 2 | 65000 | Emily | 95000 |

### LAST_VALUE()

Returns the last value in an ordered partition.

**Important:** LAST_VALUE requires proper frame specification to work as expected!

```sql
-- Get lowest salary in each department
SELECT
    employee_id,
    first_name,
    department_id,
    salary,
    LAST_VALUE(first_name) OVER (
        PARTITION BY department_id
        ORDER BY salary DESC
        ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
    ) AS lowest_paid_employee,
    LAST_VALUE(salary) OVER (
        PARTITION BY department_id
        ORDER BY salary DESC
        ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
    ) AS lowest_salary
FROM employees;
```

**Result:**
| employee_id | first_name | department_id | salary | lowest_paid_employee | lowest_salary |
|-------------|------------|---------------|--------|---------------------|---------------|
| 6 | Lisa | 1 | 110000 | John | 75000 |
| 2 | Sarah | 1 | 82000 | John | 75000 |
| 1 | John | 1 | 75000 | John | 75000 |

### NTH_VALUE()

Returns the value from the Nth row in the partition.

```sql
-- Get the 2nd highest salary in each department
SELECT
    employee_id,
    first_name,
    department_id,
    salary,
    NTH_VALUE(salary, 2) OVER (
        PARTITION BY department_id
        ORDER BY salary DESC
        ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
    ) AS second_highest_salary
FROM employees;
```

---

## 3. PARTITION BY Clause

The `PARTITION BY` clause divides the result set into partitions where the window function is applied independently.

### Basic PARTITION BY

```sql
-- Calculate department statistics for each employee
SELECT
    employee_id,
    first_name,
    department_id,
    salary,
    AVG(salary) OVER (PARTITION BY department_id) AS dept_avg,
    MAX(salary) OVER (PARTITION BY department_id) AS dept_max,
    MIN(salary) OVER (PARTITION BY department_id) AS dept_min,
    COUNT(*) OVER (PARTITION BY department_id) AS dept_count
FROM employees;
```

### Visualization of PARTITION BY

```
+------------------------------------------------------------------+
|                    EMPLOYEES TABLE                                |
+------------------------------------------------------------------+
| ID | Name     | Dept | Salary | dept_avg | dept_max | dept_min   |
+------------------------------------------------------------------+
|                                                                   |
|  +-- PARTITION: Department 1 --------------------------------+   |
|  | 6  | Lisa    | 1    | 110000 |  89000  | 110000  |  75000  |   |
|  | 2  | Sarah   | 1    |  82000 |  89000  | 110000  |  75000  |   |
|  | 1  | John    | 1    |  75000 |  89000  | 110000  |  75000  |   |
|  +-----------------------------------------------------------+   |
|                                                                   |
|  +-- PARTITION: Department 2 --------------------------------+   |
|  | 4  | Emily   | 2    |  95000 |  77333  |  95000  |  65000  |   |
|  | 8  | Jennifer| 2    |  72000 |  77333  |  95000  |  65000  |   |
|  | 3  | Michael | 2    |  65000 |  77333  |  95000  |  65000  |   |
|  +-----------------------------------------------------------+   |
|                                                                   |
|  +-- PARTITION: Department 3 --------------------------------+   |
|  | 5  | David   | 3    |  55000 |  51500  |  55000  |  48000  |   |
|  | 7  | Robert  | 3    |  48000 |  51500  |  55000  |  48000  |   |
|  +-----------------------------------------------------------+   |
+------------------------------------------------------------------+
```

### Multiple PARTITION BY Columns

```sql
-- Partition by multiple columns
SELECT
    sale_id,
    employee_id,
    product_category,
    region,
    amount,
    SUM(amount) OVER (
        PARTITION BY product_category, region
    ) AS category_region_total,
    RANK() OVER (
        PARTITION BY product_category, region
        ORDER BY amount DESC
    ) AS category_region_rank
FROM sales;
```

### PARTITION BY vs GROUP BY

```sql
-- Compare approaches for same result

-- GROUP BY: One row per group
SELECT
    department_id,
    AVG(salary) AS avg_salary
FROM employees
GROUP BY department_id;

-- Window function: All rows preserved
SELECT DISTINCT
    department_id,
    AVG(salary) OVER (PARTITION BY department_id) AS avg_salary
FROM employees;

-- Full detail with window function
SELECT
    employee_id,
    first_name,
    department_id,
    salary,
    AVG(salary) OVER (PARTITION BY department_id) AS avg_salary,
    salary - AVG(salary) OVER (PARTITION BY department_id) AS diff_from_avg
FROM employees;
```

---

## 4. ORDER BY within OVER Clause

The `ORDER BY` clause within `OVER()` determines the logical order for ranking and cumulative calculations.

### Basic ORDER BY in OVER

```sql
-- Running total ordered by date
SELECT
    sale_id,
    sale_date,
    amount,
    SUM(amount) OVER (ORDER BY sale_date) AS running_total
FROM sales;
```

**Result:**
| sale_id | sale_date | amount | running_total |
|---------|-----------|--------|---------------|
| 1 | 2024-01-05 | 1500 | 1500 |
| 2 | 2024-01-07 | 2300 | 3800 |
| 3 | 2024-01-10 | 800 | 4600 |
| 4 | 2024-01-12 | 3200 | 7800 |

### ORDER BY with PARTITION BY

```sql
-- Running total per employee
SELECT
    sale_id,
    employee_id,
    sale_date,
    amount,
    SUM(amount) OVER (
        PARTITION BY employee_id
        ORDER BY sale_date
    ) AS employee_running_total
FROM sales
ORDER BY employee_id, sale_date;
```

### Multiple ORDER BY Columns

```sql
-- Order by multiple columns
SELECT
    employee_id,
    first_name,
    hire_date,
    salary,
    ROW_NUMBER() OVER (
        ORDER BY hire_date ASC, salary DESC
    ) AS seniority_rank
FROM employees;
```

### ORDER BY Direction

```sql
-- Ascending vs Descending rankings
SELECT
    employee_id,
    first_name,
    salary,
    RANK() OVER (ORDER BY salary ASC) AS rank_lowest_first,
    RANK() OVER (ORDER BY salary DESC) AS rank_highest_first
FROM employees;
```

### ORDER BY Effect on Aggregates

**Important:** ORDER BY changes the default frame for aggregates!

```sql
-- Without ORDER BY: Entire partition
SELECT
    sale_id,
    amount,
    SUM(amount) OVER () AS total_all_sales
FROM sales;

-- With ORDER BY: Running total (default frame changes)
SELECT
    sale_id,
    sale_date,
    amount,
    SUM(amount) OVER (ORDER BY sale_date) AS running_total
FROM sales;
```

---

## 5. Frame Specifications

Frame specifications define which rows are included in the window function calculation relative to the current row.

### Frame Syntax

```sql
{ ROWS | RANGE | GROUPS } BETWEEN frame_start AND frame_end

-- Frame boundaries:
-- UNBOUNDED PRECEDING   - First row of partition
-- n PRECEDING           - n rows before current
-- CURRENT ROW           - Current row
-- n FOLLOWING           - n rows after current
-- UNBOUNDED FOLLOWING   - Last row of partition
```

### ROWS BETWEEN

Defines frame by physical number of rows.

```sql
-- 3-row moving average (current row + 2 preceding)
SELECT
    sale_id,
    sale_date,
    amount,
    AVG(amount) OVER (
        ORDER BY sale_date
        ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
    ) AS moving_avg_3
FROM sales;
```

### RANGE BETWEEN

Defines frame by logical value range.

```sql
-- Sum of sales within 7 days before current date
SELECT
    sale_id,
    sale_date,
    amount,
    SUM(amount) OVER (
        ORDER BY sale_date
        RANGE BETWEEN INTERVAL 7 DAY PRECEDING AND CURRENT ROW
    ) AS sales_last_7_days
FROM sales;
```

### Common Frame Specifications

```sql
-- 1. All rows in partition (default without ORDER BY)
ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING

-- 2. Running total (default with ORDER BY)
ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW

-- 3. Previous row only
ROWS BETWEEN 1 PRECEDING AND 1 PRECEDING

-- 4. Current and next row
ROWS BETWEEN CURRENT ROW AND 1 FOLLOWING

-- 5. Centered window (2 before, current, 2 after)
ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING
```

### Frame Specification Examples

```sql
-- Compare different frame specifications
SELECT
    sale_id,
    sale_date,
    amount,
    -- Running total
    SUM(amount) OVER (
        ORDER BY sale_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS running_total,
    -- 3-day moving average
    AVG(amount) OVER (
        ORDER BY sale_date
        ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
    ) AS moving_avg_3,
    -- Centered 5-row average
    AVG(amount) OVER (
        ORDER BY sale_date
        ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING
    ) AS centered_avg_5,
    -- Total (entire partition)
    SUM(amount) OVER (
        ORDER BY sale_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
    ) AS total
FROM sales;
```

---

## 6. Running Totals and Moving Averages

### Running Total

```sql
-- Daily running total
SELECT
    sale_date,
    amount,
    SUM(amount) OVER (
        ORDER BY sale_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS running_total
FROM sales;
```

### Running Total per Category

```sql
-- Running total by product category
SELECT
    sale_id,
    sale_date,
    product_category,
    amount,
    SUM(amount) OVER (
        PARTITION BY product_category
        ORDER BY sale_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS category_running_total
FROM sales
ORDER BY product_category, sale_date;
```

### Moving Average (Simple)

```sql
-- 3-sale moving average
SELECT
    sale_id,
    sale_date,
    amount,
    ROUND(AVG(amount) OVER (
        ORDER BY sale_date
        ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
    ), 2) AS moving_avg_3
FROM sales;
```

### Moving Average (Centered)

```sql
-- 5-sale centered moving average
SELECT
    sale_id,
    sale_date,
    amount,
    ROUND(AVG(amount) OVER (
        ORDER BY sale_date
        ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING
    ), 2) AS centered_avg_5
FROM sales;
```

### Running Count and Running Average

```sql
-- Multiple running calculations
SELECT
    sale_id,
    sale_date,
    amount,
    COUNT(*) OVER (
        ORDER BY sale_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS running_count,
    SUM(amount) OVER (
        ORDER BY sale_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS running_total,
    ROUND(AVG(amount) OVER (
        ORDER BY sale_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ), 2) AS running_avg
FROM sales;
```

### Percentage of Running Total

```sql
-- Each sale as percentage of running total
SELECT
    sale_id,
    sale_date,
    amount,
    SUM(amount) OVER (
        ORDER BY sale_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS running_total,
    ROUND(
        amount * 100.0 / SUM(amount) OVER (
            ORDER BY sale_date
            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        ), 2
    ) AS pct_of_running
FROM sales;
```

---

## Window Frame Visualization

### ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW

```
Running Total: Sum from start to current row

+-----+--------+---------------+
| Row | Amount | Running Total |
+-----+--------+---------------+
|  1  |  1500  |     1500      | <- Sum of [1500]
|  2  |  2300  |     3800      | <- Sum of [1500, 2300]
|  3  |   800  |     4600      | <- Sum of [1500, 2300, 800]
|  4  |  3200  |     7800      | <- Sum of [1500, 2300, 800, 3200]
|  5  |  1100  |     8900      | <- Sum of all above + 1100
+-----+--------+---------------+

Frame visualization for row 4:
[====FRAME====]
 1500  2300  800  3200
  ^           ^     ^
  |           |     |
  start      ...  current
```

### ROWS BETWEEN 2 PRECEDING AND CURRENT ROW

```
3-Row Moving Average: Average of current and 2 previous

+-----+--------+-----------+
| Row | Amount | Avg of 3  |
+-----+--------+-----------+
|  1  |  1500  |   1500.00 | <- Only 1 row: [1500]
|  2  |  2300  |   1900.00 | <- 2 rows: [1500, 2300]
|  3  |   800  |   1533.33 | <- 3 rows: [1500, 2300, 800]
|  4  |  3200  |   2100.00 | <- 3 rows: [2300, 800, 3200]
|  5  |  1100  |   1700.00 | <- 3 rows: [800, 3200, 1100]
+-----+--------+-----------+

Frame visualization for row 4:
         [==FRAME===]
 1500  2300   800  3200  1100
         ^     ^    ^
         |     |    |
       -2    -1   current
```

### ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING

```
3-Row Centered Average: Previous, current, and next

+-----+--------+-----------+
| Row | Amount | Centered  |
+-----+--------+-----------+
|  1  |  1500  |   1900.00 | <- [1500, 2300]
|  2  |  2300  |   1533.33 | <- [1500, 2300, 800]
|  3  |   800  |   2100.00 | <- [2300, 800, 3200]
|  4  |  3200  |   1700.00 | <- [800, 3200, 1100]
|  5  |  1100  |   2150.00 | <- [3200, 1100]
+-----+--------+-----------+

Frame visualization for row 3:
       [==FRAME===]
 1500  2300   800  3200  1100
         ^     ^    ^
         |     |    |
        -1  current +1
```

### ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING

```
Total Partition Sum: Entire partition

+-----+--------+-------+
| Row | Amount | Total |
+-----+--------+-------+
|  1  |  1500  |  8900 | <- Sum of ALL rows
|  2  |  2300  |  8900 | <- Sum of ALL rows
|  3  |   800  |  8900 | <- Sum of ALL rows
|  4  |  3200  |  8900 | <- Sum of ALL rows
|  5  |  1100  |  8900 | <- Sum of ALL rows
+-----+--------+-------+

Frame visualization (same for all rows):
[========FRAME=========]
 1500  2300   800  3200  1100
  ^                       ^
  |                       |
unbounded             unbounded
preceding             following
```

### PARTITION BY with Frame

```
Running Total per Employee (Partition by employee_id)

+------+--------+--------+---------+
| Emp  |  Date  | Amount | Running |
+------+--------+--------+---------+
|      |        |        |         |
| +--- Employee 1 Partition ---+   |
| | 1  | Jan-05 |  1500  |  1500 | |
| | 1  | Jan-10 |   800  |  2300 | |
| | 1  | Jan-20 |   950  |  3250 | |
| +--------------------------------+
|      |        |        |         |
| +--- Employee 2 Partition ---+   |
| | 2  | Jan-07 |  2300  |  2300 | |
| | 2  | Jan-15 |  1100  |  3400 | |
| | 2  | Jan-25 |  1750  |  5150 | |
| +--------------------------------+
+------+--------+--------+---------+

Each partition has independent calculations!
```

---

## Code Examples

### Example 1: Ranking Within Groups

```sql
-- Find top 3 sales per region
SELECT *
FROM (
    SELECT
        sale_id,
        region,
        amount,
        sale_date,
        ROW_NUMBER() OVER (
            PARTITION BY region
            ORDER BY amount DESC
        ) AS rank_in_region
    FROM sales
) ranked
WHERE rank_in_region <= 3
ORDER BY region, rank_in_region;
```

### Example 2: Comparing to Previous/Next Rows

```sql
-- Calculate month-over-month sales change
SELECT
    sale_date,
    amount,
    LAG(amount, 1, 0) OVER (ORDER BY sale_date) AS prev_sale,
    amount - LAG(amount, 1, 0) OVER (ORDER BY sale_date) AS change,
    CASE
        WHEN LAG(amount, 1, 0) OVER (ORDER BY sale_date) = 0 THEN NULL
        ELSE ROUND(
            (amount - LAG(amount, 1, 0) OVER (ORDER BY sale_date)) * 100.0 /
            LAG(amount, 1, 0) OVER (ORDER BY sale_date), 2
        )
    END AS pct_change
FROM sales;
```

### Example 3: Running Calculations

```sql
-- Multiple running metrics
SELECT
    sale_date,
    amount,
    -- Running totals
    SUM(amount) OVER w AS running_total,
    -- Running average
    ROUND(AVG(amount) OVER w, 2) AS running_avg,
    -- Running max and min
    MAX(amount) OVER w AS running_max,
    MIN(amount) OVER w AS running_min,
    -- Percent of running total
    ROUND(amount * 100.0 / SUM(amount) OVER w, 2) AS pct_of_total
FROM sales
WINDOW w AS (ORDER BY sale_date ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW);
```

### Example 4: Percentile Calculations

```sql
-- Calculate percentiles for employee salaries
SELECT
    employee_id,
    first_name,
    salary,
    NTILE(4) OVER (ORDER BY salary) AS quartile,
    NTILE(10) OVER (ORDER BY salary) AS decile,
    NTILE(100) OVER (ORDER BY salary) AS percentile,
    PERCENT_RANK() OVER (ORDER BY salary) AS percent_rank,
    CUME_DIST() OVER (ORDER BY salary) AS cumulative_dist
FROM employees;
```

### Example 5: Year-Over-Year Comparison

```sql
-- Compare sales to same period last year
WITH monthly_sales AS (
    SELECT
        DATE_FORMAT(sale_date, '%Y-%m') AS month,
        YEAR(sale_date) AS year,
        MONTH(sale_date) AS month_num,
        SUM(amount) AS total_sales
    FROM sales
    GROUP BY DATE_FORMAT(sale_date, '%Y-%m'), YEAR(sale_date), MONTH(sale_date)
)
SELECT
    month,
    total_sales,
    LAG(total_sales, 12) OVER (ORDER BY month) AS same_month_last_year,
    total_sales - LAG(total_sales, 12) OVER (ORDER BY month) AS yoy_change,
    ROUND(
        (total_sales - LAG(total_sales, 12) OVER (ORDER BY month)) * 100.0 /
        NULLIF(LAG(total_sales, 12) OVER (ORDER BY month), 0), 2
    ) AS yoy_pct_change
FROM monthly_sales;
```

### Example 6: First and Last Values in Groups

```sql
-- Get first and last hire in each department
SELECT
    employee_id,
    first_name,
    department_id,
    hire_date,
    salary,
    FIRST_VALUE(first_name) OVER (
        PARTITION BY department_id
        ORDER BY hire_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
    ) AS first_hired,
    LAST_VALUE(first_name) OVER (
        PARTITION BY department_id
        ORDER BY hire_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
    ) AS last_hired,
    FIRST_VALUE(hire_date) OVER (
        PARTITION BY department_id
        ORDER BY hire_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
    ) AS earliest_hire_date
FROM employees;
```

### Example 7: Moving Averages with Different Windows

```sql
-- Compare different moving average windows
SELECT
    sale_date,
    amount,
    -- 3-day moving average
    ROUND(AVG(amount) OVER (
        ORDER BY sale_date
        ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
    ), 2) AS ma_3,
    -- 5-day moving average
    ROUND(AVG(amount) OVER (
        ORDER BY sale_date
        ROWS BETWEEN 4 PRECEDING AND CURRENT ROW
    ), 2) AS ma_5,
    -- 7-day moving average
    ROUND(AVG(amount) OVER (
        ORDER BY sale_date
        ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
    ), 2) AS ma_7
FROM sales;
```

### Example 8: Salary Distribution Analysis

```sql
-- Comprehensive salary analysis
SELECT
    employee_id,
    first_name,
    department_id,
    salary,
    -- Department stats
    AVG(salary) OVER (PARTITION BY department_id) AS dept_avg,
    -- Difference from average
    salary - AVG(salary) OVER (PARTITION BY department_id) AS diff_from_avg,
    -- Percentage of department total
    ROUND(
        salary * 100.0 / SUM(salary) OVER (PARTITION BY department_id), 2
    ) AS pct_of_dept_total,
    -- Rank within department
    RANK() OVER (PARTITION BY department_id ORDER BY salary DESC) AS dept_rank,
    -- Quartile within department
    NTILE(4) OVER (PARTITION BY department_id ORDER BY salary DESC) AS dept_quartile
FROM employees;
```

### Example 9: Gap Analysis

```sql
-- Find gaps between sales
SELECT
    sale_id,
    sale_date,
    LAG(sale_date) OVER (ORDER BY sale_date) AS prev_sale_date,
    DATEDIFF(sale_date, LAG(sale_date) OVER (ORDER BY sale_date)) AS days_since_last,
    CASE
        WHEN DATEDIFF(sale_date, LAG(sale_date) OVER (ORDER BY sale_date)) > 5
        THEN 'Large Gap'
        WHEN DATEDIFF(sale_date, LAG(sale_date) OVER (ORDER BY sale_date)) > 3
        THEN 'Medium Gap'
        ELSE 'Normal'
    END AS gap_category
FROM sales;
```

### Example 10: Customer Lifetime Value Ranking

```sql
-- Rank customers by lifetime value within regions
SELECT
    customer_id,
    customer_name,
    region,
    customer_tier,
    lifetime_value,
    -- Regional ranking
    RANK() OVER (
        PARTITION BY region
        ORDER BY lifetime_value DESC
    ) AS regional_rank,
    -- Tier ranking
    RANK() OVER (
        PARTITION BY customer_tier
        ORDER BY lifetime_value DESC
    ) AS tier_rank,
    -- Overall percentile
    ROUND(PERCENT_RANK() OVER (ORDER BY lifetime_value) * 100, 2) AS overall_percentile
FROM customers;
```

### Example 11: Sales Velocity Analysis

```sql
-- Calculate sales velocity (rate of change)
SELECT
    sale_id,
    sale_date,
    amount,
    LAG(sale_date) OVER (ORDER BY sale_date) AS prev_date,
    LAG(amount) OVER (ORDER BY sale_date) AS prev_amount,
    -- Amount change per day
    ROUND(
        (amount - LAG(amount) OVER (ORDER BY sale_date)) * 1.0 /
        NULLIF(DATEDIFF(sale_date, LAG(sale_date) OVER (ORDER BY sale_date)), 0), 2
    ) AS velocity
FROM sales;
```

### Example 12: Cumulative Distribution

```sql
-- Calculate cumulative distribution of sales
SELECT
    sale_id,
    amount,
    -- Cumulative count
    COUNT(*) OVER (ORDER BY amount) AS cumulative_count,
    -- Cumulative percentage
    ROUND(
        COUNT(*) OVER (ORDER BY amount) * 100.0 /
        COUNT(*) OVER (), 2
    ) AS cumulative_pct,
    -- Cumulative sum
    SUM(amount) OVER (ORDER BY amount) AS cumulative_sum,
    -- Cumulative sum percentage
    ROUND(
        SUM(amount) OVER (ORDER BY amount) * 100.0 /
        SUM(amount) OVER (), 2
    ) AS cumulative_sum_pct
FROM sales;
```

### Example 13: Identifying Consecutive Sequences

```sql
-- Identify consecutive sales days
WITH sale_groups AS (
    SELECT
        sale_id,
        sale_date,
        ROW_NUMBER() OVER (ORDER BY sale_date) AS rn,
        DATE_SUB(sale_date, INTERVAL ROW_NUMBER() OVER (ORDER BY sale_date) DAY) AS grp
    FROM sales
)
SELECT
    sale_id,
    sale_date,
    COUNT(*) OVER (PARTITION BY grp) AS consecutive_days,
    ROW_NUMBER() OVER (PARTITION BY grp ORDER BY sale_date) AS day_in_sequence
FROM sale_groups;
```

### Example 14: Comparing to Best and Worst

```sql
-- Compare each sale to best and worst in category
SELECT
    sale_id,
    product_category,
    amount,
    -- Best in category
    MAX(amount) OVER (PARTITION BY product_category) AS category_best,
    -- Worst in category
    MIN(amount) OVER (PARTITION BY product_category) AS category_worst,
    -- Gap from best
    MAX(amount) OVER (PARTITION BY product_category) - amount AS gap_from_best,
    -- Percentage of best
    ROUND(
        amount * 100.0 / MAX(amount) OVER (PARTITION BY product_category), 2
    ) AS pct_of_best
FROM sales;
```

### Example 15: Employee Performance Trend

```sql
-- Track employee sales performance over time
SELECT
    employee_id,
    sale_date,
    amount,
    -- Previous sale
    LAG(amount) OVER (PARTITION BY employee_id ORDER BY sale_date) AS prev_sale,
    -- Performance trend
    CASE
        WHEN amount > LAG(amount) OVER (PARTITION BY employee_id ORDER BY sale_date)
        THEN 'Improving'
        WHEN amount < LAG(amount) OVER (PARTITION BY employee_id ORDER BY sale_date)
        THEN 'Declining'
        ELSE 'Stable'
    END AS trend,
    -- Cumulative employee total
    SUM(amount) OVER (
        PARTITION BY employee_id
        ORDER BY sale_date
    ) AS cumulative_sales,
    -- Employee ranking by date
    RANK() OVER (
        PARTITION BY sale_date
        ORDER BY amount DESC
    ) AS daily_rank
FROM sales;
```

---

## Practice Questions

### Question 1: Uber Driver Analytics
**Company: Uber | Difficulty: Hard**

Uber wants to analyze driver performance. For each driver, calculate:
- Total trips and earnings
- Their rank among all drivers by earnings
- Moving average of last 3 trips
- Percentage of their earnings vs top earner
- Whether they're improving (current trip > average of previous trips)

```sql
-- Sample trips table
CREATE TABLE trips (
    trip_id INT,
    driver_id INT,
    trip_date DATE,
    fare DECIMAL(10,2),
    distance DECIMAL(10,2)
);

INSERT INTO trips VALUES
(1, 101, '2024-01-01', 25.00, 5.2),
(2, 102, '2024-01-01', 35.00, 8.1),
(3, 101, '2024-01-02', 18.00, 3.5),
(4, 103, '2024-01-02', 42.00, 10.5),
(5, 102, '2024-01-03', 28.00, 6.3),
(6, 101, '2024-01-03', 55.00, 12.8),
(7, 103, '2024-01-04', 30.00, 7.2),
(8, 102, '2024-01-04', 48.00, 11.0),
(9, 101, '2024-01-05', 22.00, 4.8),
(10, 103, '2024-01-05', 38.00, 9.1);
```

**Solution:**

```sql
WITH driver_metrics AS (
    SELECT
        trip_id,
        driver_id,
        trip_date,
        fare,
        -- Total trips per driver
        COUNT(*) OVER (PARTITION BY driver_id) AS total_trips,
        -- Total earnings per driver
        SUM(fare) OVER (PARTITION BY driver_id) AS total_earnings,
        -- Moving average of last 3 trips
        ROUND(AVG(fare) OVER (
            PARTITION BY driver_id
            ORDER BY trip_date
            ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
        ), 2) AS moving_avg_3,
        -- Average of all previous trips
        AVG(fare) OVER (
            PARTITION BY driver_id
            ORDER BY trip_date
            ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING
        ) AS prev_avg
    FROM trips
)
SELECT
    trip_id,
    driver_id,
    trip_date,
    fare,
    total_trips,
    total_earnings,
    -- Rank by total earnings
    DENSE_RANK() OVER (ORDER BY total_earnings DESC) AS earnings_rank,
    moving_avg_3,
    -- Percentage of top earner
    ROUND(
        total_earnings * 100.0 / MAX(total_earnings) OVER (), 2
    ) AS pct_of_top,
    -- Improving indicator
    CASE
        WHEN prev_avg IS NULL THEN 'First Trip'
        WHEN fare > prev_avg THEN 'Improving'
        WHEN fare < prev_avg THEN 'Declining'
        ELSE 'Stable'
    END AS performance_trend
FROM driver_metrics
ORDER BY driver_id, trip_date;
```

**Explanation:**
- Uses multiple window functions with different partitions and frames
- `PARTITION BY driver_id` groups calculations per driver
- Moving average uses `ROWS BETWEEN 2 PRECEDING AND CURRENT ROW`
- Previous average excludes current row with `1 PRECEDING`
- Final ranking is across all drivers (no partition)

**Alternative Approach - Using CTEs for clarity:**

```sql
WITH
-- Step 1: Calculate per-driver totals
driver_totals AS (
    SELECT
        driver_id,
        COUNT(*) AS total_trips,
        SUM(fare) AS total_earnings
    FROM trips
    GROUP BY driver_id
),
-- Step 2: Add rankings and moving metrics
trip_details AS (
    SELECT
        t.*,
        dt.total_trips,
        dt.total_earnings,
        AVG(t.fare) OVER (
            PARTITION BY t.driver_id
            ORDER BY t.trip_date
            ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
        ) AS moving_avg_3
    FROM trips t
    JOIN driver_totals dt ON t.driver_id = dt.driver_id
)
SELECT
    *,
    DENSE_RANK() OVER (ORDER BY total_earnings DESC) AS earnings_rank
FROM trip_details;
```

---

### Question 2: Stripe Payment Fraud Detection
**Company: Stripe | Difficulty: Hard**

Stripe needs to detect potentially fraudulent transactions. Flag transactions where:
- Amount is > 3x the customer's average
- It's within 1 hour of previous transaction
- Amount is in top 5% of all transactions

```sql
-- Sample transactions table
CREATE TABLE transactions (
    txn_id INT,
    customer_id INT,
    txn_time DATETIME,
    amount DECIMAL(10,2),
    merchant VARCHAR(100)
);

INSERT INTO transactions VALUES
(1, 201, '2024-01-15 09:00:00', 50.00, 'Coffee Shop'),
(2, 201, '2024-01-15 09:30:00', 5000.00, 'Electronics'),
(3, 202, '2024-01-15 10:00:00', 120.00, 'Restaurant'),
(4, 201, '2024-01-15 14:00:00', 75.00, 'Gas Station'),
(5, 203, '2024-01-15 11:00:00', 8500.00, 'Jewelry'),
(6, 202, '2024-01-15 10:15:00', 3500.00, 'Electronics'),
(7, 203, '2024-01-15 15:00:00', 200.00, 'Grocery'),
(8, 201, '2024-01-16 09:00:00', 45.00, 'Coffee Shop'),
(9, 202, '2024-01-16 12:00:00', 90.00, 'Restaurant'),
(10, 203, '2024-01-16 14:00:00', 150.00, 'Gas Station');
```

**Solution:**

```sql
WITH txn_analysis AS (
    SELECT
        txn_id,
        customer_id,
        txn_time,
        amount,
        merchant,
        -- Customer's average amount
        AVG(amount) OVER (PARTITION BY customer_id) AS customer_avg,
        -- Previous transaction time for this customer
        LAG(txn_time) OVER (
            PARTITION BY customer_id
            ORDER BY txn_time
        ) AS prev_txn_time,
        -- Overall percentile
        PERCENT_RANK() OVER (ORDER BY amount) AS amount_percentile
    FROM transactions
)
SELECT
    txn_id,
    customer_id,
    txn_time,
    amount,
    merchant,
    ROUND(customer_avg, 2) AS customer_avg,
    -- Flag: Amount > 3x average
    CASE
        WHEN amount > customer_avg * 3 THEN 'YES'
        ELSE 'NO'
    END AS flag_high_amount,
    -- Flag: Within 1 hour of previous
    CASE
        WHEN TIMESTAMPDIFF(MINUTE, prev_txn_time, txn_time) <= 60 THEN 'YES'
        ELSE 'NO'
    END AS flag_rapid_txn,
    -- Flag: Top 5%
    CASE
        WHEN amount_percentile >= 0.95 THEN 'YES'
        ELSE 'NO'
    END AS flag_top_5_pct,
    -- Overall fraud score
    (CASE WHEN amount > customer_avg * 3 THEN 1 ELSE 0 END +
     CASE WHEN prev_txn_time IS NOT NULL
          AND TIMESTAMPDIFF(MINUTE, prev_txn_time, txn_time) <= 60 THEN 1 ELSE 0 END +
     CASE WHEN amount_percentile >= 0.95 THEN 1 ELSE 0 END) AS fraud_score
FROM txn_analysis
ORDER BY fraud_score DESC, amount DESC;
```

**Explanation:**
- Uses `AVG() OVER (PARTITION BY customer_id)` for customer-specific average
- `LAG()` finds the previous transaction time per customer
- `PERCENT_RANK()` calculates overall percentile
- Fraud score combines multiple flags

---

### Question 3: Spotify Streaming Analytics
**Company: Spotify | Difficulty: Hard**

Analyze song streaming patterns:
- Rank songs by streams within each genre
- Calculate 7-day moving average of streams
- Find each song's peak day and current day's % of peak
- Identify songs with 3+ consecutive days of growth

```sql
-- Sample streams table
CREATE TABLE streams (
    stream_date DATE,
    song_id INT,
    song_name VARCHAR(100),
    genre VARCHAR(50),
    streams INT
);

INSERT INTO streams VALUES
('2024-01-01', 1, 'Song A', 'Pop', 10000),
('2024-01-02', 1, 'Song A', 'Pop', 12000),
('2024-01-03', 1, 'Song A', 'Pop', 15000),
('2024-01-04', 1, 'Song A', 'Pop', 18000),
('2024-01-05', 1, 'Song A', 'Pop', 14000),
('2024-01-01', 2, 'Song B', 'Rock', 8000),
('2024-01-02', 2, 'Song B', 'Rock', 9500),
('2024-01-03', 2, 'Song B', 'Rock', 11000),
('2024-01-04', 2, 'Song B', 'Rock', 10500),
('2024-01-05', 2, 'Song B', 'Rock', 12500),
('2024-01-01', 3, 'Song C', 'Pop', 20000),
('2024-01-02', 3, 'Song C', 'Pop', 19000),
('2024-01-03', 3, 'Song C', 'Pop', 17500),
('2024-01-04', 3, 'Song C', 'Pop', 16000),
('2024-01-05', 3, 'Song C', 'Pop', 15000);
```

**Solution:**

```sql
WITH stream_metrics AS (
    SELECT
        stream_date,
        song_id,
        song_name,
        genre,
        streams,
        -- Rank within genre (by total streams)
        RANK() OVER (
            PARTITION BY genre
            ORDER BY SUM(streams) OVER (PARTITION BY genre, song_id) DESC
        ) AS genre_rank,
        -- 7-day moving average
        ROUND(AVG(streams) OVER (
            PARTITION BY song_id
            ORDER BY stream_date
            ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
        ), 0) AS moving_avg_7,
        -- Peak streams for this song
        MAX(streams) OVER (PARTITION BY song_id) AS peak_streams,
        -- Previous day's streams
        LAG(streams) OVER (
            PARTITION BY song_id
            ORDER BY stream_date
        ) AS prev_streams,
        -- 2 days ago streams
        LAG(streams, 2) OVER (
            PARTITION BY song_id
            ORDER BY stream_date
        ) AS streams_2_days_ago
    FROM streams
)
SELECT
    stream_date,
    song_id,
    song_name,
    genre,
    streams,
    genre_rank,
    moving_avg_7,
    peak_streams,
    -- Percentage of peak
    ROUND(streams * 100.0 / peak_streams, 2) AS pct_of_peak,
    -- Growth streak indicator
    CASE
        WHEN streams > prev_streams
         AND prev_streams > streams_2_days_ago
         AND streams_2_days_ago IS NOT NULL
        THEN '3+ Day Growth Streak'
        WHEN streams > prev_streams THEN 'Growing'
        WHEN streams < prev_streams THEN 'Declining'
        ELSE 'Stable'
    END AS trend
FROM stream_metrics
ORDER BY song_id, stream_date;
```

**Explanation:**
- Uses nested window functions for genre ranking
- 7-day moving average uses `ROWS BETWEEN 6 PRECEDING AND CURRENT ROW`
- Peak detection uses `MAX() OVER (PARTITION BY song_id)`
- Growth streak checks multiple LAG values

---

### Question 4: Amazon E-commerce Retention Analysis
**Company: Amazon | Difficulty: Hard**

Calculate customer retention metrics:
- Days since first purchase
- Days since last purchase
- Purchase frequency (purchases per active day)
- Lifetime value ranking
- Cohort analysis (group by first purchase month)

```sql
-- Sample purchases table
CREATE TABLE purchases (
    purchase_id INT,
    customer_id INT,
    purchase_date DATE,
    amount DECIMAL(10,2),
    product_category VARCHAR(50)
);

INSERT INTO purchases VALUES
(1, 301, '2024-01-05', 150.00, 'Electronics'),
(2, 302, '2024-01-08', 75.00, 'Books'),
(3, 301, '2024-01-15', 200.00, 'Electronics'),
(4, 303, '2024-01-20', 50.00, 'Clothing'),
(5, 302, '2024-01-25', 120.00, 'Electronics'),
(6, 301, '2024-02-01', 80.00, 'Books'),
(7, 303, '2024-02-05', 90.00, 'Electronics'),
(8, 302, '2024-02-10', 200.00, 'Clothing'),
(9, 301, '2024-02-15', 175.00, 'Electronics'),
(10, 303, '2024-02-20', 65.00, 'Books');
```

**Solution:**

```sql
WITH customer_metrics AS (
    SELECT
        purchase_id,
        customer_id,
        purchase_date,
        amount,
        -- First purchase date
        FIRST_VALUE(purchase_date) OVER (
            PARTITION BY customer_id
            ORDER BY purchase_date
            ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
        ) AS first_purchase_date,
        -- Last purchase date
        LAST_VALUE(purchase_date) OVER (
            PARTITION BY customer_id
            ORDER BY purchase_date
            ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
        ) AS last_purchase_date,
        -- Total purchases
        COUNT(*) OVER (PARTITION BY customer_id) AS total_purchases,
        -- Total spend
        SUM(amount) OVER (PARTITION BY customer_id) AS lifetime_value,
        -- Running total
        SUM(amount) OVER (
            PARTITION BY customer_id
            ORDER BY purchase_date
        ) AS running_total
    FROM purchases
)
SELECT
    purchase_id,
    customer_id,
    purchase_date,
    amount,
    -- Cohort (first purchase month)
    DATE_FORMAT(first_purchase_date, '%Y-%m') AS cohort,
    -- Days since first purchase
    DATEDIFF(purchase_date, first_purchase_date) AS days_since_first,
    -- Customer age (first to last purchase)
    DATEDIFF(last_purchase_date, first_purchase_date) AS customer_age_days,
    -- Purchase frequency
    ROUND(
        total_purchases * 1.0 /
        GREATEST(DATEDIFF(last_purchase_date, first_purchase_date), 1) * 30, 2
    ) AS purchases_per_month,
    -- Lifetime value
    lifetime_value,
    -- LTV ranking
    RANK() OVER (ORDER BY lifetime_value DESC) AS ltv_rank,
    -- Running total
    running_total,
    -- Percentage of lifetime value
    ROUND(running_total * 100.0 / lifetime_value, 2) AS pct_of_ltv
FROM customer_metrics
ORDER BY customer_id, purchase_date;
```

---

### Question 5: LinkedIn Connection Network Analysis
**Company: LinkedIn | Difficulty: Hard**

Analyze user connection growth:
- Daily new connections
- Connection velocity (rate of growth)
- Ranking by total connections
- Days to reach 100, 500, 1000 connections
- Comparison to average user

```sql
-- Sample connections table
CREATE TABLE connections (
    connection_date DATE,
    user_id INT,
    new_connections INT
);

INSERT INTO connections VALUES
('2024-01-01', 401, 10),
('2024-01-02', 401, 15),
('2024-01-03', 401, 8),
('2024-01-04', 401, 25),
('2024-01-05', 401, 50),
('2024-01-01', 402, 5),
('2024-01-02', 402, 8),
('2024-01-03', 402, 12),
('2024-01-04', 402, 18),
('2024-01-05', 402, 22),
('2024-01-01', 403, 20),
('2024-01-02', 403, 35),
('2024-01-03', 403, 45),
('2024-01-04', 403, 30),
('2024-01-05', 403, 25);
```

**Solution:**

```sql
WITH connection_metrics AS (
    SELECT
        connection_date,
        user_id,
        new_connections,
        -- Running total of connections
        SUM(new_connections) OVER (
            PARTITION BY user_id
            ORDER BY connection_date
        ) AS total_connections,
        -- Previous day connections
        LAG(new_connections) OVER (
            PARTITION BY user_id
            ORDER BY connection_date
        ) AS prev_day_connections,
        -- Day number for this user
        ROW_NUMBER() OVER (
            PARTITION BY user_id
            ORDER BY connection_date
        ) AS day_number
    FROM connections
),
daily_avg AS (
    SELECT
        connection_date,
        AVG(new_connections) AS avg_new_connections
    FROM connections
    GROUP BY connection_date
)
SELECT
    cm.connection_date,
    cm.user_id,
    cm.new_connections,
    cm.total_connections,
    -- Growth rate
    CASE
        WHEN cm.prev_day_connections IS NULL THEN NULL
        ELSE ROUND(
            (cm.new_connections - cm.prev_day_connections) * 100.0 /
            cm.prev_day_connections, 2
        )
    END AS growth_rate_pct,
    -- Ranking by total connections
    RANK() OVER (ORDER BY cm.total_connections DESC) AS connection_rank,
    -- Milestone tracking
    CASE
        WHEN cm.total_connections >= 100
         AND LAG(cm.total_connections) OVER (
             PARTITION BY cm.user_id
             ORDER BY cm.connection_date
         ) < 100
        THEN cm.day_number
    END AS day_reached_100,
    -- Comparison to average
    cm.new_connections - da.avg_new_connections AS diff_from_avg,
    ROUND(
        cm.new_connections * 100.0 / da.avg_new_connections, 2
    ) AS pct_of_avg
FROM connection_metrics cm
JOIN daily_avg da ON cm.connection_date = da.connection_date
ORDER BY cm.user_id, cm.connection_date;
```

---

### Question 6: Netflix Content Performance
**Company: Netflix | Difficulty: Hard**

Analyze show performance:
- Rank shows by views within genre
- Calculate week-over-week growth
- Find breakout shows (>200% growth)
- Moving average completion rate
- Percentage of genre total views

```sql
-- Sample views table
CREATE TABLE show_views (
    view_date DATE,
    show_id INT,
    show_name VARCHAR(100),
    genre VARCHAR(50),
    views INT,
    completion_rate DECIMAL(5,2)
);

INSERT INTO show_views VALUES
('2024-01-07', 1, 'Drama A', 'Drama', 50000, 0.75),
('2024-01-14', 1, 'Drama A', 'Drama', 120000, 0.78),
('2024-01-21', 1, 'Drama A', 'Drama', 300000, 0.82),
('2024-01-07', 2, 'Comedy B', 'Comedy', 80000, 0.65),
('2024-01-14', 2, 'Comedy B', 'Comedy', 95000, 0.68),
('2024-01-21', 2, 'Comedy B', 'Comedy', 110000, 0.70),
('2024-01-07', 3, 'Drama C', 'Drama', 200000, 0.80),
('2024-01-14', 3, 'Drama C', 'Drama', 180000, 0.79),
('2024-01-21', 3, 'Drama C', 'Drama', 160000, 0.77);
```

**Solution:**

```sql
WITH show_metrics AS (
    SELECT
        view_date,
        show_id,
        show_name,
        genre,
        views,
        completion_rate,
        -- Previous week views
        LAG(views) OVER (
            PARTITION BY show_id
            ORDER BY view_date
        ) AS prev_week_views,
        -- Genre total views this week
        SUM(views) OVER (PARTITION BY genre, view_date) AS genre_total,
        -- Moving average completion rate
        AVG(completion_rate) OVER (
            PARTITION BY show_id
            ORDER BY view_date
            ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
        ) AS moving_avg_completion
    FROM show_views
)
SELECT
    view_date,
    show_id,
    show_name,
    genre,
    views,
    -- Rank within genre
    RANK() OVER (
        PARTITION BY genre, view_date
        ORDER BY views DESC
    ) AS genre_rank,
    -- Week-over-week growth
    CASE
        WHEN prev_week_views IS NULL THEN NULL
        ELSE ROUND(
            (views - prev_week_views) * 100.0 / prev_week_views, 2
        )
    END AS wow_growth_pct,
    -- Breakout indicator
    CASE
        WHEN prev_week_views IS NOT NULL
         AND views > prev_week_views * 3
        THEN 'BREAKOUT'
        WHEN prev_week_views IS NOT NULL
         AND views > prev_week_views * 2
        THEN 'Trending'
        ELSE 'Normal'
    END AS performance_label,
    -- Percentage of genre
    ROUND(views * 100.0 / genre_total, 2) AS pct_of_genre,
    -- Moving avg completion
    ROUND(moving_avg_completion, 4) AS avg_completion
FROM show_metrics
ORDER BY genre, view_date, genre_rank;
```

---

### Question 7: DoorDash Delivery Optimization
**Company: DoorDash | Difficulty: Hard**

Optimize delivery operations:
- Calculate rolling average delivery time
- Rank drivers by on-time delivery rate
- Identify peak hours per zone
- Compare each delivery to zone average
- Find consecutive late deliveries

```sql
-- Sample deliveries table
CREATE TABLE deliveries (
    delivery_id INT,
    driver_id INT,
    zone VARCHAR(20),
    delivery_time DATETIME,
    actual_minutes INT,
    estimated_minutes INT
);

INSERT INTO deliveries VALUES
(1, 501, 'Downtown', '2024-01-15 12:00:00', 25, 30),
(2, 502, 'Suburbs', '2024-01-15 12:15:00', 35, 25),
(3, 501, 'Downtown', '2024-01-15 13:00:00', 28, 30),
(4, 503, 'Downtown', '2024-01-15 12:30:00', 40, 30),
(5, 502, 'Suburbs', '2024-01-15 14:00:00', 32, 30),
(6, 501, 'Downtown', '2024-01-15 14:30:00', 22, 30),
(7, 503, 'Suburbs', '2024-01-15 15:00:00', 45, 35),
(8, 502, 'Downtown', '2024-01-15 15:30:00', 20, 25),
(9, 501, 'Suburbs', '2024-01-15 16:00:00', 38, 35),
(10, 503, 'Downtown', '2024-01-15 17:00:00', 35, 30);
```

**Solution:**

```sql
WITH delivery_metrics AS (
    SELECT
        delivery_id,
        driver_id,
        zone,
        delivery_time,
        HOUR(delivery_time) AS delivery_hour,
        actual_minutes,
        estimated_minutes,
        -- On-time indicator
        CASE WHEN actual_minutes <= estimated_minutes THEN 1 ELSE 0 END AS on_time,
        -- Zone average
        AVG(actual_minutes) OVER (PARTITION BY zone) AS zone_avg_time,
        -- Rolling average (last 3 deliveries)
        AVG(actual_minutes) OVER (
            PARTITION BY driver_id
            ORDER BY delivery_time
            ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
        ) AS rolling_avg_time,
        -- Previous delivery on-time status
        LAG(CASE WHEN actual_minutes <= estimated_minutes THEN 1 ELSE 0 END) OVER (
            PARTITION BY driver_id
            ORDER BY delivery_time
        ) AS prev_on_time
    FROM deliveries
)
SELECT
    delivery_id,
    driver_id,
    zone,
    delivery_time,
    actual_minutes,
    estimated_minutes,
    -- On-time rate per driver
    ROUND(
        SUM(on_time) OVER (PARTITION BY driver_id) * 100.0 /
        COUNT(*) OVER (PARTITION BY driver_id), 2
    ) AS driver_on_time_rate,
    -- Driver ranking
    RANK() OVER (
        ORDER BY SUM(on_time) OVER (PARTITION BY driver_id) * 1.0 /
        COUNT(*) OVER (PARTITION BY driver_id) DESC
    ) AS driver_rank,
    -- Comparison to zone average
    actual_minutes - ROUND(zone_avg_time, 2) AS diff_from_zone_avg,
    -- Rolling average
    ROUND(rolling_avg_time, 2) AS rolling_avg,
    -- Consecutive late indicator
    CASE
        WHEN on_time = 0 AND prev_on_time = 0 THEN 'Consecutive Late'
        WHEN on_time = 0 THEN 'Late'
        ELSE 'On Time'
    END AS delivery_status
FROM delivery_metrics
ORDER BY driver_id, delivery_time;
```

---

### Question 8: Airbnb Pricing Strategy
**Company: Airbnb | Difficulty: Hard**

Analyze listing prices:
- Rank by price within neighborhood
- Calculate price percentile
- Find price vs neighborhood average
- Identify price changes over time
- Seasonal adjustment recommendations

```sql
-- Sample listings table
CREATE TABLE listings (
    listing_id INT,
    neighborhood VARCHAR(50),
    bedrooms INT,
    list_date DATE,
    nightly_price DECIMAL(10,2)
);

INSERT INTO listings VALUES
(1, 'Downtown', 1, '2024-01-01', 150.00),
(1, 'Downtown', 1, '2024-02-01', 175.00),
(2, 'Beach', 2, '2024-01-01', 250.00),
(2, 'Beach', 2, '2024-02-01', 300.00),
(3, 'Downtown', 2, '2024-01-01', 200.00),
(3, 'Downtown', 2, '2024-02-01', 220.00),
(4, 'Beach', 1, '2024-01-01', 180.00),
(4, 'Beach', 1, '2024-02-01', 210.00),
(5, 'Suburbs', 3, '2024-01-01', 120.00),
(5, 'Suburbs', 3, '2024-02-01', 125.00);
```

**Solution:**

```sql
WITH listing_metrics AS (
    SELECT
        listing_id,
        neighborhood,
        bedrooms,
        list_date,
        nightly_price,
        -- Neighborhood average
        AVG(nightly_price) OVER (
            PARTITION BY neighborhood, list_date
        ) AS neighborhood_avg,
        -- Bedroom category average
        AVG(nightly_price) OVER (
            PARTITION BY bedrooms, list_date
        ) AS bedroom_avg,
        -- Previous price
        LAG(nightly_price) OVER (
            PARTITION BY listing_id
            ORDER BY list_date
        ) AS prev_price,
        -- Price percentile in neighborhood
        PERCENT_RANK() OVER (
            PARTITION BY neighborhood, list_date
            ORDER BY nightly_price
        ) AS neighborhood_percentile
    FROM listings
)
SELECT
    listing_id,
    neighborhood,
    bedrooms,
    list_date,
    nightly_price,
    -- Rank in neighborhood
    RANK() OVER (
        PARTITION BY neighborhood, list_date
        ORDER BY nightly_price DESC
    ) AS neighborhood_price_rank,
    -- Percentile
    ROUND(neighborhood_percentile * 100, 0) AS price_percentile,
    -- Comparison to averages
    ROUND(nightly_price - neighborhood_avg, 2) AS diff_from_neighborhood,
    ROUND(nightly_price - bedroom_avg, 2) AS diff_from_bedroom_type,
    -- Price change
    CASE
        WHEN prev_price IS NULL THEN 'New Listing'
        ELSE CONCAT(
            ROUND((nightly_price - prev_price) * 100.0 / prev_price, 1),
            '% change'
        )
    END AS price_change,
    -- Recommendation
    CASE
        WHEN nightly_price < neighborhood_avg * 0.8 THEN 'Consider price increase'
        WHEN nightly_price > neighborhood_avg * 1.2 THEN 'May be overpriced'
        ELSE 'Competitively priced'
    END AS pricing_recommendation
FROM listing_metrics
ORDER BY neighborhood, list_date, listing_id;
```

---

### Question 9: Robinhood Trading Patterns
**Company: Robinhood | Difficulty: Hard**

Analyze trading patterns:
- Calculate portfolio value changes
- Identify best and worst trading days
- Moving average of trade values
- Win/loss streaks
- Compare to market benchmark

```sql
-- Sample trades table
CREATE TABLE trades (
    trade_id INT,
    user_id INT,
    trade_date DATE,
    symbol VARCHAR(10),
    trade_type VARCHAR(10),
    shares INT,
    price DECIMAL(10,2)
);

INSERT INTO trades VALUES
(1, 601, '2024-01-02', 'AAPL', 'BUY', 10, 185.00),
(2, 601, '2024-01-03', 'AAPL', 'SELL', 10, 188.00),
(3, 601, '2024-01-05', 'GOOGL', 'BUY', 5, 140.00),
(4, 601, '2024-01-08', 'GOOGL', 'SELL', 5, 135.00),
(5, 601, '2024-01-10', 'MSFT', 'BUY', 8, 375.00),
(6, 601, '2024-01-12', 'MSFT', 'SELL', 8, 382.00),
(7, 602, '2024-01-02', 'AAPL', 'BUY', 20, 185.00),
(8, 602, '2024-01-04', 'AAPL', 'SELL', 20, 190.00),
(9, 602, '2024-01-06', 'TSLA', 'BUY', 15, 240.00),
(10, 602, '2024-01-09', 'TSLA', 'SELL', 15, 235.00);
```

**Solution:**

```sql
WITH trade_pairs AS (
    -- Match buys with sells
    SELECT
        t1.user_id,
        t1.symbol,
        t1.trade_date AS buy_date,
        t2.trade_date AS sell_date,
        t1.shares,
        t1.price AS buy_price,
        t2.price AS sell_price,
        (t2.price - t1.price) * t1.shares AS profit_loss
    FROM trades t1
    JOIN trades t2
        ON t1.user_id = t2.user_id
        AND t1.symbol = t2.symbol
        AND t1.trade_type = 'BUY'
        AND t2.trade_type = 'SELL'
        AND t2.trade_date > t1.trade_date
),
trade_metrics AS (
    SELECT
        *,
        -- Cumulative profit/loss
        SUM(profit_loss) OVER (
            PARTITION BY user_id
            ORDER BY sell_date
        ) AS cumulative_pl,
        -- Win indicator
        CASE WHEN profit_loss > 0 THEN 1 ELSE 0 END AS is_win,
        -- Trade number
        ROW_NUMBER() OVER (
            PARTITION BY user_id
            ORDER BY sell_date
        ) AS trade_num,
        -- Moving average profit
        AVG(profit_loss) OVER (
            PARTITION BY user_id
            ORDER BY sell_date
            ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
        ) AS moving_avg_pl
    FROM trade_pairs
)
SELECT
    user_id,
    symbol,
    buy_date,
    sell_date,
    profit_loss,
    -- Win rate
    ROUND(
        SUM(is_win) OVER (PARTITION BY user_id) * 100.0 /
        COUNT(*) OVER (PARTITION BY user_id), 2
    ) AS win_rate_pct,
    -- Cumulative P/L
    cumulative_pl,
    -- Moving average
    ROUND(moving_avg_pl, 2) AS moving_avg_pl,
    -- Best/worst trade
    CASE
        WHEN profit_loss = MAX(profit_loss) OVER (PARTITION BY user_id) THEN 'Best Trade'
        WHEN profit_loss = MIN(profit_loss) OVER (PARTITION BY user_id) THEN 'Worst Trade'
        ELSE ''
    END AS trade_label,
    -- Streak tracking
    CASE
        WHEN is_win = 1
         AND LAG(CASE WHEN profit_loss > 0 THEN 1 ELSE 0 END) OVER (
             PARTITION BY user_id ORDER BY sell_date
         ) = 1
        THEN 'Win Streak'
        WHEN is_win = 0
         AND LAG(CASE WHEN profit_loss > 0 THEN 1 ELSE 0 END) OVER (
             PARTITION BY user_id ORDER BY sell_date
         ) = 0
        THEN 'Loss Streak'
        ELSE ''
    END AS streak_status
FROM trade_metrics
ORDER BY user_id, sell_date;
```

---

### Question 10: Twitter Engagement Analysis
**Company: Twitter/X | Difficulty: Hard**

Analyze tweet engagement:
- Engagement rate trends
- Viral tweet detection
- Follower growth correlation
- Best posting times
- Comparison to account average

```sql
-- Sample tweets table
CREATE TABLE tweets (
    tweet_id INT,
    user_id INT,
    post_time DATETIME,
    impressions INT,
    engagements INT,
    retweets INT,
    followers_at_time INT
);

INSERT INTO tweets VALUES
(1, 701, '2024-01-15 09:00:00', 10000, 500, 50, 5000),
(2, 701, '2024-01-15 14:00:00', 15000, 1200, 150, 5100),
(3, 701, '2024-01-16 10:00:00', 8000, 300, 20, 5200),
(4, 701, '2024-01-16 18:00:00', 50000, 8000, 2000, 5500),
(5, 701, '2024-01-17 12:00:00', 12000, 600, 80, 7000),
(6, 702, '2024-01-15 08:00:00', 5000, 200, 15, 2000),
(7, 702, '2024-01-15 16:00:00', 6000, 350, 40, 2050),
(8, 702, '2024-01-16 11:00:00', 4500, 180, 10, 2100),
(9, 702, '2024-01-17 09:00:00', 8000, 600, 100, 2200),
(10, 702, '2024-01-17 15:00:00', 7000, 400, 45, 2300);
```

**Solution:**

```sql
WITH tweet_metrics AS (
    SELECT
        tweet_id,
        user_id,
        post_time,
        HOUR(post_time) AS post_hour,
        impressions,
        engagements,
        retweets,
        followers_at_time,
        -- Engagement rate
        ROUND(engagements * 100.0 / impressions, 2) AS engagement_rate,
        -- Previous tweet metrics
        LAG(engagements) OVER (
            PARTITION BY user_id
            ORDER BY post_time
        ) AS prev_engagements,
        -- Account average engagement
        AVG(engagements) OVER (PARTITION BY user_id) AS avg_engagements,
        -- Follower growth
        followers_at_time - LAG(followers_at_time) OVER (
            PARTITION BY user_id
            ORDER BY post_time
        ) AS follower_growth,
        -- Moving average engagement rate
        AVG(engagements * 100.0 / impressions) OVER (
            PARTITION BY user_id
            ORDER BY post_time
            ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
        ) AS moving_avg_rate
    FROM tweets
)
SELECT
    tweet_id,
    user_id,
    post_time,
    post_hour,
    impressions,
    engagements,
    engagement_rate,
    -- Viral detection (>3x average engagements)
    CASE
        WHEN engagements > avg_engagements * 3 THEN 'VIRAL'
        WHEN engagements > avg_engagements * 1.5 THEN 'Above Average'
        WHEN engagements < avg_engagements * 0.5 THEN 'Below Average'
        ELSE 'Normal'
    END AS performance_label,
    -- Growth from previous tweet
    CASE
        WHEN prev_engagements IS NULL THEN NULL
        ELSE ROUND(
            (engagements - prev_engagements) * 100.0 / prev_engagements, 2
        )
    END AS growth_pct,
    -- Follower growth
    COALESCE(follower_growth, 0) AS follower_growth,
    -- Moving average
    ROUND(moving_avg_rate, 2) AS moving_avg_rate,
    -- Best hour indicator
    CASE
        WHEN engagement_rate = MAX(engagement_rate) OVER (PARTITION BY user_id)
        THEN 'Best Performing'
        ELSE ''
    END AS performance_flag,
    -- Rank by engagement
    RANK() OVER (
        PARTITION BY user_id
        ORDER BY engagements DESC
    ) AS engagement_rank
FROM tweet_metrics
ORDER BY user_id, post_time;
```

---

## Common Mistakes

### 1. PARTITION BY Misuse

**Mistake: Forgetting PARTITION BY when needed**

```sql
-- WRONG: Ranking all employees together instead of by department
SELECT
    employee_id,
    department_id,
    salary,
    RANK() OVER (ORDER BY salary DESC) AS rank
FROM employees;

-- CORRECT: Ranking within each department
SELECT
    employee_id,
    department_id,
    salary,
    RANK() OVER (
        PARTITION BY department_id
        ORDER BY salary DESC
    ) AS dept_rank
FROM employees;
```

**Mistake: Wrong partition column**

```sql
-- WRONG: Partitioning by salary instead of department
SELECT
    employee_id,
    department_id,
    salary,
    ROW_NUMBER() OVER (PARTITION BY salary ORDER BY hire_date) AS rn
FROM employees;

-- The above creates a separate partition for each unique salary value!
```

### 2. Frame Boundary Errors

**Mistake: Not specifying frame for LAST_VALUE**

```sql
-- WRONG: Default frame stops at current row
SELECT
    employee_id,
    department_id,
    salary,
    LAST_VALUE(salary) OVER (
        PARTITION BY department_id
        ORDER BY salary DESC
    ) AS lowest_salary  -- Returns current row, not last!
FROM employees;

-- CORRECT: Extend frame to end of partition
SELECT
    employee_id,
    department_id,
    salary,
    LAST_VALUE(salary) OVER (
        PARTITION BY department_id
        ORDER BY salary DESC
        ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
    ) AS lowest_salary
FROM employees;
```

**Mistake: ROWS vs RANGE confusion**

```sql
-- ROWS: Physical row count (2 rows before current)
ROWS BETWEEN 2 PRECEDING AND CURRENT ROW

-- RANGE: Logical value range (values within 2 of current)
RANGE BETWEEN 2 PRECEDING AND CURRENT ROW

-- These produce different results when there are ties!
```

### 3. Window Function in WHERE Clause Error

**Mistake: Using window function directly in WHERE**

```sql
-- WRONG: Window functions not allowed in WHERE
SELECT employee_id, salary
FROM employees
WHERE RANK() OVER (ORDER BY salary DESC) <= 3;

-- ERROR: Window functions are not allowed in WHERE

-- CORRECT: Use subquery or CTE
SELECT *
FROM (
    SELECT
        employee_id,
        salary,
        RANK() OVER (ORDER BY salary DESC) AS salary_rank
    FROM employees
) ranked
WHERE salary_rank <= 3;

-- OR with CTE
WITH ranked_employees AS (
    SELECT
        employee_id,
        salary,
        RANK() OVER (ORDER BY salary DESC) AS salary_rank
    FROM employees
)
SELECT * FROM ranked_employees WHERE salary_rank <= 3;
```

### 4. ORDER BY Clause Confusion

**Mistake: Missing ORDER BY for ranking functions**

```sql
-- WRONG: ROW_NUMBER without ORDER BY is non-deterministic
SELECT
    employee_id,
    ROW_NUMBER() OVER (PARTITION BY department_id) AS rn  -- Random order!
FROM employees;

-- CORRECT: Always specify ORDER BY for ranking
SELECT
    employee_id,
    ROW_NUMBER() OVER (
        PARTITION BY department_id
        ORDER BY hire_date
    ) AS rn
FROM employees;
```

**Mistake: Confusing query ORDER BY with window ORDER BY**

```sql
-- Window ORDER BY determines calculation order
-- Query ORDER BY determines display order
SELECT
    employee_id,
    salary,
    SUM(salary) OVER (ORDER BY hire_date) AS running_total  -- By hire_date
FROM employees
ORDER BY salary DESC;  -- But displayed by salary
```

### 5. Performance Considerations

**Mistake: Multiple window functions with different partitions**

```sql
-- INEFFICIENT: Different partitions cause multiple sorts
SELECT
    sale_id,
    amount,
    SUM(amount) OVER (PARTITION BY region) AS region_total,
    SUM(amount) OVER (PARTITION BY product_category) AS category_total,
    SUM(amount) OVER (PARTITION BY employee_id) AS employee_total
FROM sales;

-- BETTER: Use CTEs to organize and potentially optimize
WITH region_totals AS (
    SELECT region, SUM(amount) AS region_total
    FROM sales GROUP BY region
),
category_totals AS (
    SELECT product_category, SUM(amount) AS category_total
    FROM sales GROUP BY product_category
)
SELECT
    s.sale_id,
    s.amount,
    r.region_total,
    c.category_total
FROM sales s
JOIN region_totals r ON s.region = r.region
JOIN category_totals c ON s.product_category = c.product_category;
```

**Mistake: Window function on large datasets without indexes**

```sql
-- Ensure proper indexing for window function ORDER BY columns
CREATE INDEX idx_sales_date ON sales(sale_date);
CREATE INDEX idx_sales_employee_date ON sales(employee_id, sale_date);
```

### 6. LAG/LEAD Default Value Issues

**Mistake: Not handling NULL for first/last rows**

```sql
-- WRONG: First row will have NULL for LAG
SELECT
    sale_date,
    amount,
    amount - LAG(amount) OVER (ORDER BY sale_date) AS change
FROM sales;
-- First row shows NULL for change

-- CORRECT: Provide default value
SELECT
    sale_date,
    amount,
    amount - LAG(amount, 1, amount) OVER (ORDER BY sale_date) AS change
FROM sales;
-- First row shows 0 for change (amount - amount)
```

### 7. Aggregate vs Window Function Confusion

**Mistake: Mixing aggregate and window function syntax**

```sql
-- WRONG: Can't mix GROUP BY with window functions on same column
SELECT
    department_id,
    AVG(salary),  -- Aggregate
    AVG(salary) OVER ()  -- Window function
FROM employees
GROUP BY department_id;  -- Error or unexpected results

-- CORRECT: Choose one approach
-- Option 1: All aggregates
SELECT department_id, AVG(salary) AS dept_avg
FROM employees
GROUP BY department_id;

-- Option 2: All window functions
SELECT DISTINCT
    department_id,
    AVG(salary) OVER (PARTITION BY department_id) AS dept_avg,
    AVG(salary) OVER () AS company_avg
FROM employees;
```

### 8. NTILE Edge Cases

**Mistake: Not understanding NTILE distribution**

```sql
-- With 8 rows and NTILE(3):
-- Group 1: 3 rows (8/3 = 2.67, rounds up)
-- Group 2: 3 rows
-- Group 3: 2 rows

-- Not always equal distribution!

-- To get exact percentiles, use PERCENT_RANK instead
SELECT
    employee_id,
    salary,
    NTILE(4) OVER (ORDER BY salary) AS quartile,
    ROUND(PERCENT_RANK() OVER (ORDER BY salary) * 100, 2) AS percentile
FROM employees;
```

### Common Mistakes Summary Table

| Mistake | Problem | Solution |
|---------|---------|----------|
| Window function in WHERE | Syntax error | Use subquery or CTE |
| Missing ORDER BY | Non-deterministic results | Always specify for rankings |
| Wrong frame for LAST_VALUE | Returns current row | Add UNBOUNDED FOLLOWING |
| ROWS vs RANGE confusion | Different results with ties | Understand the difference |
| No default for LAG/LEAD | NULL for edge rows | Provide default value |
| Multiple different partitions | Performance impact | Consider CTEs or joins |
| No indexes | Slow queries | Index ORDER BY columns |

---

## Summary

### When to Use Window Functions vs Subqueries vs GROUP BY

| Requirement | Best Approach | Example |
|-------------|---------------|---------|
| Summary only (no detail) | GROUP BY | Total sales by region |
| Detail + summary together | Window Function | Employee salary + dept average |
| Row comparison | Window Function | Compare to previous row |
| Running totals | Window Function | Cumulative sales |
| Top N per group | Window Function + Subquery | Top 3 products per category |
| Simple existence check | Subquery | Employees with sales > $1000 |
| Ranking within groups | Window Function | Sales rank per region |
| Moving averages | Window Function | 7-day rolling average |

### Decision Flowchart

```
Need aggregate calculations?
    |
    +-- Only aggregates needed? --> GROUP BY
    |
    +-- Need detail + aggregates? --> Window Function
           |
           +-- Need ranking? --> ROW_NUMBER/RANK/DENSE_RANK
           |
           +-- Need row comparison? --> LAG/LEAD
           |
           +-- Need running calculation? --> SUM/AVG with ORDER BY
           |
           +-- Need first/last value? --> FIRST_VALUE/LAST_VALUE
```

### Key Takeaways

1. **Window functions preserve rows** - Unlike GROUP BY, you keep all detail while adding calculations

2. **OVER() defines the window** - Use PARTITION BY for groups, ORDER BY for sorting, frame for boundaries

3. **Rankings handle ties differently**:
   - ROW_NUMBER: Always unique (1, 2, 3, 4)
   - RANK: Gaps after ties (1, 2, 2, 4)
   - DENSE_RANK: No gaps (1, 2, 2, 3)

4. **LAG/LEAD for row comparison** - Access previous/next rows without self-joins

5. **Frame specification matters** - Default frame changes with ORDER BY; be explicit for clarity

6. **Cannot use in WHERE** - Wrap in subquery or CTE to filter on window function results

7. **Performance considerations**:
   - Index ORDER BY columns
   - Minimize different partitions
   - Consider materialized views for heavy calculations

8. **Named windows improve readability**:
   ```sql
   SELECT
       col1,
       SUM(col2) OVER w AS running_sum,
       AVG(col2) OVER w AS running_avg
   FROM table
   WINDOW w AS (PARTITION BY col3 ORDER BY col4);
   ```

### Quick Reference

```sql
-- Ranking
ROW_NUMBER() OVER (PARTITION BY dept ORDER BY salary DESC)
RANK() OVER (ORDER BY score DESC)
DENSE_RANK() OVER (PARTITION BY category ORDER BY sales DESC)
NTILE(4) OVER (ORDER BY value)

-- Value
LAG(column, offset, default) OVER (ORDER BY date)
LEAD(column, offset, default) OVER (ORDER BY date)
FIRST_VALUE(column) OVER (PARTITION BY group ORDER BY date)
LAST_VALUE(column) OVER (PARTITION BY group ORDER BY date ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)

-- Aggregates
SUM(amount) OVER (ORDER BY date ROWS UNBOUNDED PRECEDING)
AVG(amount) OVER (ORDER BY date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW)
COUNT(*) OVER (PARTITION BY category)
MAX(value) OVER (PARTITION BY group)
MIN(value) OVER (PARTITION BY group)

-- Statistical
PERCENT_RANK() OVER (ORDER BY value)
CUME_DIST() OVER (ORDER BY value)
```

---

## Next Steps

- Practice with real datasets (Kaggle, public APIs)
- Explore database-specific extensions (PostgreSQL, SQL Server)
- Learn about query optimization for window functions (Phase 9)
- Study advanced patterns: gaps-and-islands, sessionization
- Combine window functions with CTEs for complex analytics
