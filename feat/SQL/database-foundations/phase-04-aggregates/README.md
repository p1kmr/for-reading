# Phase 4: Aggregate Functions & Grouping

## Table of Contents
- [Introduction to Aggregates](#introduction-to-aggregates)
- [COUNT Function](#count-function)
- [SUM Function](#sum-function)
- [AVG Function](#avg-function)
- [MIN and MAX Functions](#min-and-max-functions)
- [GROUP BY Fundamentals](#group-by-fundamentals)
- [HAVING vs WHERE](#having-vs-where)
- [Aggregates with JOINs](#aggregates-with-joins)
- [Practice Questions](#practice-questions)
- [Common Mistakes](#common-mistakes)
- [Summary & Key Takeaways](#summary--key-takeaways)

---

## Introduction to Aggregates

### WHY
Aggregate functions are essential for data analysis and business intelligence. Real-world scenarios:
- Calculate total sales revenue per quarter
- Find average employee salary by department
- Count customers per region
- Identify highest and lowest performing products
- Generate statistical reports and dashboards

90% of business reports require aggregation. Mastering these concepts is crucial for:
- Data analytics
- Business intelligence
- Performance metrics
- Interview success (frequently tested!)

### WHAT
Aggregate functions perform calculations on multiple rows and return a single result. They "summarize" data.

### Query Execution Order
```
FROM       ->  Start with table(s)
WHERE      ->  Filter individual rows
GROUP BY   ->  Group rows together
HAVING     ->  Filter groups
SELECT     ->  Choose columns & aggregates
ORDER BY   ->  Sort results
LIMIT      ->  Restrict count
```

### Aggregation Visualization

```
┌─────────────────────────────────────────────────────────────┐
│                   AGGREGATION DATA FLOW                     │
└─────────────────────────────────────────────────────────────┘

Raw Data                    Grouped Data                Final Result
┌──────────┐               ┌──────────────┐            ┌───────────┐
│ dept │sal│               │ dept=1       │            │dept│total │
├──────┼───┤               │ ┌───┐┌───┐   │            ├────┼──────┤
│  1   │80K│  ──────>      │ │80K││90K│   │  ──────>   │ 1  │ 170K │
│  1   │90K│   GROUP       │ └───┘└───┘   │   SUM()    │ 2  │ 150K │
│  2   │70K│    BY         ├──────────────┤            │ 3  │ 85K  │
│  2   │80K│   dept        │ dept=2       │            └────┴──────┘
│  3   │85K│               │ ┌───┐┌───┐   │
└──────┴───┘               │ │70K││80K│   │
                           │ └───┘└───┘   │
                           ├──────────────┤
                           │ dept=3       │
                           │ ┌───┐        │
                           │ │85K│        │
                           │ └───┘        │
                           └──────────────┘
```

---

## COUNT Function

### WHY
COUNT is the most frequently used aggregate. Essential for:
- Counting records in a table
- Finding number of unique values
- Checking data completeness
- Inventory and user statistics

### WHAT
COUNT returns the number of rows matching a condition.

### HOW
```sql
COUNT(*)          -- Count all rows
COUNT(column)     -- Count non-NULL values in column
COUNT(DISTINCT column)  -- Count unique non-NULL values
```

### WHEN
Use when you need to know "how many" of something exists.

---

### Example 1: COUNT All Rows
```sql
-- How many employees do we have?
SELECT COUNT(*) AS total_employees
FROM employees;
```

**Result:**
```
total_employees
───────────────
      15
```

### Example 2: COUNT Non-NULL Values
```sql
-- How many employees have a manager assigned?
SELECT
    COUNT(*) AS total_employees,
    COUNT(manager_id) AS with_manager,
    COUNT(*) - COUNT(manager_id) AS without_manager
FROM employees;
```

**Result:**
```
total_employees │ with_manager │ without_manager
────────────────┼──────────────┼────────────────
      15        │     12       │       3
```

**Note:** COUNT(column) ignores NULL values!

### Example 3: COUNT DISTINCT
```sql
-- How many different departments have employees?
SELECT COUNT(DISTINCT department_id) AS dept_count
FROM employees
WHERE department_id IS NOT NULL;

-- How many unique countries do customers come from?
SELECT COUNT(DISTINCT country) AS country_count
FROM customers;
```

---

## SUM Function

### WHY
SUM calculates totals - critical for financial reports, inventory, and metrics:
- Total revenue
- Sum of quantities sold
- Total hours worked
- Budget calculations

### WHAT
SUM adds up all numeric values in a column.

### HOW
```sql
SUM(column)           -- Sum all non-NULL values
SUM(DISTINCT column)  -- Sum unique values only
```

### WHEN
Use when you need to add up values (money, quantities, durations).

---

### Example 4: Basic SUM
```sql
-- Total salary expense
SELECT SUM(salary) AS total_salary_expense
FROM employees;

-- Total sales amount
SELECT SUM(amount) AS total_revenue
FROM sales;
```

### Example 5: SUM with Conditions
```sql
-- Total salary for Engineering department
SELECT SUM(salary) AS engineering_total
FROM employees
WHERE department_id = 1;

-- Total sales in Q1 2023
SELECT SUM(amount) AS q1_revenue
FROM sales
WHERE sale_date BETWEEN '2023-01-01' AND '2023-03-31';
```

### Example 6: SUM with Expressions
```sql
-- Total compensation (salary + bonus)
SELECT
    SUM(salary) AS total_salary,
    SUM(COALESCE(bonus, 0)) AS total_bonus,
    SUM(salary + COALESCE(bonus, 0)) AS total_compensation
FROM employees;
```

---

## AVG Function

### WHY
AVG calculates averages for statistical analysis:
- Average order value
- Mean salary by department
- Average response time
- Performance metrics

### WHAT
AVG returns the arithmetic mean of numeric values.

### HOW
```sql
AVG(column)           -- Average of non-NULL values
AVG(DISTINCT column)  -- Average of unique values
```

### WHEN
Use when you need the mean/average of a numeric column.

---

### Example 7: Basic AVG
```sql
-- Average employee salary
SELECT
    AVG(salary) AS avg_salary,
    ROUND(AVG(salary), 2) AS avg_salary_rounded
FROM employees;

-- Average sale amount
SELECT ROUND(AVG(amount), 2) AS avg_sale_amount
FROM sales;
```

**Important:** AVG ignores NULL values in calculation!

### Example 8: AVG vs Manual Calculation
```sql
-- Understanding AVG with NULLs
SELECT
    AVG(bonus) AS avg_bonus,           -- Ignores NULLs
    SUM(bonus) / COUNT(*) AS manual_avg,    -- Includes NULLs as 0
    SUM(bonus) / COUNT(bonus) AS correct_avg -- Same as AVG
FROM employees;
```

```
Sample data: bonus = [5000, NULL, 3000, NULL, 4000]

AVG(bonus) = (5000 + 3000 + 4000) / 3 = 4000
SUM/COUNT(*) = 12000 / 5 = 2400  <- Different!
```

---

## MIN and MAX Functions

### WHY
MIN and MAX find extremes - useful for:
- Finding highest/lowest prices
- Date ranges (earliest/latest)
- Performance boundaries
- Identifying outliers

### WHAT
- MIN returns the smallest value
- MAX returns the largest value

### HOW
```sql
MIN(column)  -- Smallest value (number, date, or string)
MAX(column)  -- Largest value (number, date, or string)
```

### WHEN
Use when finding boundaries, extremes, or ranges.

---

### Example 9: Basic MIN and MAX
```sql
-- Salary range
SELECT
    MIN(salary) AS lowest_salary,
    MAX(salary) AS highest_salary,
    MAX(salary) - MIN(salary) AS salary_range
FROM employees;

-- Date range of sales
SELECT
    MIN(sale_date) AS first_sale,
    MAX(sale_date) AS last_sale
FROM sales;
```

### Example 10: MIN/MAX with Strings
```sql
-- First and last customer alphabetically
SELECT
    MIN(last_name) AS first_alphabetically,
    MAX(last_name) AS last_alphabetically
FROM customers;

-- Result: MIN='Adams', MAX='Zhang'
```

### Example 11: Combining All Aggregates
```sql
-- Complete sales statistics
SELECT
    COUNT(*) AS total_sales,
    COUNT(DISTINCT customer_id) AS unique_customers,
    SUM(amount) AS total_revenue,
    ROUND(AVG(amount), 2) AS avg_sale,
    MIN(amount) AS smallest_sale,
    MAX(amount) AS largest_sale
FROM sales
WHERE sale_date >= '2023-01-01';
```

---

## GROUP BY Fundamentals

### WHY
GROUP BY enables analysis by categories:
- Sales by region
- Employees by department
- Orders by customer
- Revenue by product category

Without GROUP BY, you get one result for the entire table. With GROUP BY, you get one result per group.

### WHAT
GROUP BY divides rows into groups based on column values, then applies aggregate functions to each group.

### HOW
```sql
SELECT group_column, AGGREGATE_FUNCTION(column)
FROM table
GROUP BY group_column;
```

### WHEN
Use when you need to calculate aggregates for different categories.

---

### GROUP BY Visualization

```
┌───────────────────────────────────────────────────────────────┐
│               GROUP BY Single Column                          │
└───────────────────────────────────────────────────────────────┘

Original Table:                    After GROUP BY department_id:
┌────┬───────┬────────┬───────┐   ┌────────────┬─────────┬────────┐
│ id │ name  │dept_id │salary │   │ dept_id    │ COUNT(*) │ AVG()  │
├────┼───────┼────────┼───────┤   ├────────────┼──────────┼────────┤
│ 1  │ Alice │   1    │ 80000 │   │     1      │    3     │ 90000  │
│ 2  │ Bob   │   1    │ 90000 │   │     2      │    2     │ 75000  │
│ 3  │ Carol │   1    │100000 │   │     3      │    1     │ 85000  │
│ 4  │ David │   2    │ 70000 │   └────────────┴──────────┴────────┘
│ 5  │ Eve   │   2    │ 80000 │
│ 6  │ Frank │   3    │ 85000 │
└────┴───────┴────────┴───────┘
```

### Example 12: GROUP BY Single Column
```sql
-- Count employees per department
SELECT
    department_id,
    COUNT(*) AS employee_count
FROM employees
GROUP BY department_id
ORDER BY employee_count DESC;

-- Average salary by department
SELECT
    department_id,
    COUNT(*) AS emp_count,
    ROUND(AVG(salary), 2) AS avg_salary,
    SUM(salary) AS total_salary
FROM employees
GROUP BY department_id;
```

### Example 13: GROUP BY Multiple Columns
```sql
-- Sales by category and year
SELECT
    category,
    YEAR(sale_date) AS sale_year,
    COUNT(*) AS num_sales,
    SUM(amount) AS total_amount
FROM sales
GROUP BY category, YEAR(sale_date)
ORDER BY category, sale_year;
```

**Multiple Column GROUP BY Visualization:**
```
┌────────────────────────────────────────────────────────────┐
│           GROUP BY category, year                          │
└────────────────────────────────────────────────────────────┘

Groups created:
┌─────────────────┬──────┬─────────┬───────────┐
│    category     │ year │ COUNT(*) │   SUM()   │
├─────────────────┼──────┼──────────┼───────────┤
│ Electronics     │ 2022 │    45    │  125,000  │
│ Electronics     │ 2023 │    62    │  178,500  │
│ Furniture       │ 2022 │    30    │   45,000  │
│ Furniture       │ 2023 │    38    │   52,300  │
│ Clothing        │ 2022 │    80    │   32,000  │
│ Clothing        │ 2023 │    95    │   41,200  │
└─────────────────┴──────┴──────────┴───────────┘
```

### Example 14: GROUP BY with Expressions
```sql
-- Sales by month
SELECT
    YEAR(sale_date) AS year,
    MONTH(sale_date) AS month,
    COUNT(*) AS num_sales,
    SUM(amount) AS monthly_revenue
FROM sales
GROUP BY YEAR(sale_date), MONTH(sale_date)
ORDER BY year, month;

-- Employees by salary range
SELECT
    CASE
        WHEN salary < 60000 THEN 'Entry Level'
        WHEN salary BETWEEN 60000 AND 90000 THEN 'Mid Level'
        WHEN salary > 90000 THEN 'Senior Level'
    END AS salary_tier,
    COUNT(*) AS employee_count
FROM employees
GROUP BY
    CASE
        WHEN salary < 60000 THEN 'Entry Level'
        WHEN salary BETWEEN 60000 AND 90000 THEN 'Mid Level'
        WHEN salary > 90000 THEN 'Senior Level'
    END;
```

---

## HAVING vs WHERE

### WHY
You need two different filters:
- **WHERE**: Filters individual rows BEFORE grouping
- **HAVING**: Filters groups AFTER aggregation

### WHAT
HAVING is like WHERE, but for aggregate results.

### Key Difference

```
┌─────────────────────────────────────────────────────────────┐
│                WHERE vs HAVING                               │
└─────────────────────────────────────────────────────────────┘

                    ┌─────────┐
    Raw Rows  ────> │  WHERE  │ ────> Filtered Rows
                    └─────────┘
                         │
                         ▼
                    ┌──────────┐
                    │ GROUP BY │
                    └──────────┘
                         │
                         ▼
                    ┌─────────┐
    Groups    ────> │ HAVING  │ ────> Filtered Groups
                    └─────────┘
```

### Comparison Table

| Feature | WHERE | HAVING |
|---------|-------|--------|
| Filters | Individual rows | Groups |
| Timing | Before GROUP BY | After GROUP BY |
| Can use | Column values | Aggregates (COUNT, SUM, etc.) |
| Example | `WHERE salary > 50000` | `HAVING COUNT(*) > 5` |
| Performance | Faster (fewer rows to group) | Slower (groups already formed) |

---

### Example 15: HAVING with COUNT
```sql
-- Departments with more than 3 employees
SELECT
    department_id,
    COUNT(*) AS emp_count
FROM employees
GROUP BY department_id
HAVING COUNT(*) > 3;

-- Customers who made more than 5 purchases
SELECT
    customer_id,
    COUNT(*) AS purchase_count,
    SUM(amount) AS total_spent
FROM sales
GROUP BY customer_id
HAVING COUNT(*) > 5
ORDER BY total_spent DESC;
```

### Example 16: HAVING with SUM and AVG
```sql
-- Categories with total sales over $10,000
SELECT
    category,
    SUM(amount) AS total_sales
FROM sales
GROUP BY category
HAVING SUM(amount) > 10000
ORDER BY total_sales DESC;

-- Departments with average salary over $80,000
SELECT
    department_id,
    ROUND(AVG(salary), 2) AS avg_salary,
    COUNT(*) AS emp_count
FROM employees
GROUP BY department_id
HAVING AVG(salary) > 80000;
```

### Example 17: WHERE and HAVING Together
```sql
-- Find high-performing product categories in 2023
-- WHERE: Filter to 2023 sales only (row-level)
-- HAVING: Only categories with >$5000 total (group-level)

SELECT
    category,
    COUNT(*) AS num_sales,
    SUM(amount) AS total_revenue,
    ROUND(AVG(amount), 2) AS avg_sale
FROM sales
WHERE sale_date >= '2023-01-01'           -- Filter rows FIRST
  AND sale_date < '2024-01-01'
GROUP BY category
HAVING SUM(amount) > 5000                  -- Filter groups AFTER
ORDER BY total_revenue DESC;
```

**Execution Order:**
1. FROM sales
2. WHERE (filter to 2023)
3. GROUP BY category
4. HAVING (filter groups > $5000)
5. SELECT
6. ORDER BY

---

## Aggregates with JOINs

### WHY
Real analysis requires combining tables:
- Sales with customer information
- Employees with department names
- Projects with team member counts

### Example 18: Aggregate with JOIN
```sql
-- Sales summary by department name (not just ID)
SELECT
    d.department_name,
    COUNT(e.id) AS employee_count,
    SUM(e.salary) AS total_salary,
    ROUND(AVG(e.salary), 2) AS avg_salary
FROM departments d
LEFT JOIN employees e ON d.id = e.department_id
GROUP BY d.id, d.department_name
ORDER BY total_salary DESC;
```

### Example 19: Customer Sales Report
```sql
-- Top customers by total purchases
SELECT
    c.first_name,
    c.last_name,
    c.country,
    COUNT(s.id) AS num_purchases,
    SUM(s.amount) AS total_spent,
    ROUND(AVG(s.amount), 2) AS avg_purchase
FROM customers c
JOIN sales s ON c.id = s.customer_id
GROUP BY c.id, c.first_name, c.last_name, c.country
HAVING SUM(s.amount) > 1000
ORDER BY total_spent DESC
LIMIT 10;
```

### Example 20: Project Team Statistics
```sql
-- Projects with team size and total assigned hours
SELECT
    p.name AS project_name,
    p.status,
    COUNT(ep.employee_id) AS team_size,
    SUM(ep.hours_allocated) AS total_hours
FROM projects p
LEFT JOIN employee_projects ep ON p.id = ep.project_id
GROUP BY p.id, p.name, p.status
HAVING COUNT(ep.employee_id) >= 2
ORDER BY team_size DESC;
```

### Example 21: Multi-Level Aggregation Report
```sql
-- Complete sales report by country and category
SELECT
    c.country,
    s.category,
    COUNT(*) AS num_sales,
    SUM(s.amount) AS total_revenue,
    ROUND(AVG(s.amount), 2) AS avg_sale,
    MIN(s.amount) AS min_sale,
    MAX(s.amount) AS max_sale
FROM sales s
JOIN customers c ON s.customer_id = c.id
WHERE s.sale_date >= '2023-01-01'
GROUP BY c.country, s.category
HAVING COUNT(*) >= 5
ORDER BY c.country, total_revenue DESC;
```

---

## Practice Questions

### Medium Question 1: Department Statistics

**Question**: Write a query to find the average salary, minimum salary, maximum salary, and total number of employees for each department. Only include departments with more than 2 employees. Order by average salary descending.

**Difficulty**: Medium

**Approach**:
1. Use GROUP BY on department_id
2. Apply AVG, MIN, MAX, COUNT aggregates
3. Use HAVING to filter departments with > 2 employees
4. Order by average salary

**Solution**:
```sql
SELECT
    department_id,
    COUNT(*) AS employee_count,
    ROUND(AVG(salary), 2) AS avg_salary,
    MIN(salary) AS min_salary,
    MAX(salary) AS max_salary
FROM employees
GROUP BY department_id
HAVING COUNT(*) > 2
ORDER BY avg_salary DESC;
```

**Explanation**:
- `GROUP BY department_id`: Creates one group per department
- `COUNT(*)`: Counts all employees in each department
- `AVG(salary)`: Calculates mean salary per department
- `HAVING COUNT(*) > 2`: Excludes small departments
- `ORDER BY avg_salary DESC`: Highest paying departments first

**Alternative Solution**:
```sql
-- Include department name using JOIN
SELECT
    d.department_name,
    COUNT(e.id) AS employee_count,
    ROUND(AVG(e.salary), 2) AS avg_salary,
    MIN(e.salary) AS min_salary,
    MAX(e.salary) AS max_salary
FROM departments d
JOIN employees e ON d.id = e.department_id
GROUP BY d.id, d.department_name
HAVING COUNT(e.id) > 2
ORDER BY avg_salary DESC;
```

**Common Mistakes**:
- Using WHERE instead of HAVING for `COUNT(*) > 2`
- Forgetting to include department_id in SELECT or GROUP BY
- Not using ROUND for cleaner decimal output

---

### Medium Question 2: Monthly Sales Analysis

**Question**: Calculate the total revenue, number of sales, and average sale amount for each month in 2023. Only show months with total revenue exceeding $5,000.

**Difficulty**: Medium

**Approach**:
1. Filter to 2023 using WHERE
2. Group by year and month
3. Calculate aggregates
4. Filter groups with HAVING

**Solution**:
```sql
SELECT
    YEAR(sale_date) AS sale_year,
    MONTH(sale_date) AS sale_month,
    COUNT(*) AS num_sales,
    SUM(amount) AS total_revenue,
    ROUND(AVG(amount), 2) AS avg_sale
FROM sales
WHERE sale_date >= '2023-01-01'
  AND sale_date < '2024-01-01'
GROUP BY YEAR(sale_date), MONTH(sale_date)
HAVING SUM(amount) > 5000
ORDER BY sale_year, sale_month;
```

**Explanation**:
- `WHERE`: Filters rows to 2023 before grouping (efficient)
- `GROUP BY YEAR(), MONTH()`: Groups by month
- `HAVING SUM(amount) > 5000`: Filters low-revenue months
- Note: Year is included in GROUP BY for correctness

**Alternative Solution**:
```sql
-- Using DATE_FORMAT for better readability
SELECT
    DATE_FORMAT(sale_date, '%Y-%m') AS month,
    COUNT(*) AS num_sales,
    SUM(amount) AS total_revenue,
    ROUND(AVG(amount), 2) AS avg_sale
FROM sales
WHERE YEAR(sale_date) = 2023
GROUP BY DATE_FORMAT(sale_date, '%Y-%m')
HAVING SUM(amount) > 5000
ORDER BY month;
```

**Common Mistakes**:
- Using HAVING to filter by date (should use WHERE)
- Not including all GROUP BY columns in SELECT
- Confusing date extraction functions between databases

---

### Medium Question 3: Customer Purchase Patterns

**Question**: Find customers who have made exactly 3 purchases. Show their name, total amount spent, and average purchase amount.

**Difficulty**: Medium

**Approach**:
1. Join customers with sales
2. Group by customer
3. Use HAVING to filter for exactly 3 purchases

**Solution**:
```sql
SELECT
    c.first_name,
    c.last_name,
    COUNT(*) AS purchase_count,
    SUM(s.amount) AS total_spent,
    ROUND(AVG(s.amount), 2) AS avg_purchase
FROM customers c
JOIN sales s ON c.id = s.customer_id
GROUP BY c.id, c.first_name, c.last_name
HAVING COUNT(*) = 3
ORDER BY total_spent DESC;
```

**Explanation**:
- `JOIN`: Connects customers to their sales
- `GROUP BY c.id, ...`: Groups all sales per customer
- `HAVING COUNT(*) = 3`: Filters to exactly 3 purchases
- Including `c.id` in GROUP BY ensures uniqueness

**Alternative Solution**:
```sql
-- Using subquery approach
SELECT
    c.first_name,
    c.last_name,
    stats.purchase_count,
    stats.total_spent,
    stats.avg_purchase
FROM customers c
JOIN (
    SELECT
        customer_id,
        COUNT(*) AS purchase_count,
        SUM(amount) AS total_spent,
        ROUND(AVG(amount), 2) AS avg_purchase
    FROM sales
    GROUP BY customer_id
    HAVING COUNT(*) = 3
) stats ON c.id = stats.customer_id
ORDER BY stats.total_spent DESC;
```

**Common Mistakes**:
- Forgetting to include customer_id in GROUP BY
- Using WHERE COUNT(*) = 3 instead of HAVING
- Not joining properly and getting incorrect counts

---

### Medium Question 4: Product Category Performance

**Question**: For each product category, calculate the total sales, count of transactions, and identify the percentage of total revenue. Only include categories with at least 10 transactions.

**Difficulty**: Medium

**Approach**:
1. Group by category
2. Calculate totals
3. Use subquery or window function for percentage
4. Filter with HAVING

**Solution**:
```sql
SELECT
    category,
    COUNT(*) AS transaction_count,
    SUM(amount) AS category_revenue,
    ROUND(
        SUM(amount) * 100.0 / (SELECT SUM(amount) FROM sales),
        2
    ) AS revenue_percentage
FROM sales
GROUP BY category
HAVING COUNT(*) >= 10
ORDER BY category_revenue DESC;
```

**Explanation**:
- Subquery `(SELECT SUM(amount) FROM sales)` gets total revenue
- Division calculates percentage
- `* 100.0` ensures decimal division
- HAVING filters categories with < 10 transactions

**Alternative Solution**:
```sql
-- Using CTE for clarity
WITH category_stats AS (
    SELECT
        category,
        COUNT(*) AS transaction_count,
        SUM(amount) AS category_revenue
    FROM sales
    GROUP BY category
    HAVING COUNT(*) >= 10
),
total_revenue AS (
    SELECT SUM(amount) AS total FROM sales
)
SELECT
    cs.category,
    cs.transaction_count,
    cs.category_revenue,
    ROUND(cs.category_revenue * 100.0 / tr.total, 2) AS revenue_percentage
FROM category_stats cs
CROSS JOIN total_revenue tr
ORDER BY cs.category_revenue DESC;
```

**Common Mistakes**:
- Integer division (forgetting `100.0`)
- Calculating percentage on filtered data instead of total
- Not handling NULL values in division

---

### Medium Question 5: Employee Project Allocation

**Question**: Find all employees who are assigned to more than 2 projects. Show their name, number of projects, and total hours allocated across all projects.

**Difficulty**: Medium

**Approach**:
1. Join employees with employee_projects
2. Group by employee
3. Count projects and sum hours
4. Filter with HAVING

**Solution**:
```sql
SELECT
    e.first_name,
    e.last_name,
    COUNT(ep.project_id) AS project_count,
    SUM(ep.hours_allocated) AS total_hours
FROM employees e
JOIN employee_projects ep ON e.id = ep.employee_id
GROUP BY e.id, e.first_name, e.last_name
HAVING COUNT(ep.project_id) > 2
ORDER BY project_count DESC, total_hours DESC;
```

**Explanation**:
- `JOIN`: Connects employees to their project assignments
- `COUNT(ep.project_id)`: Counts projects per employee
- `SUM(ep.hours_allocated)`: Total hours across projects
- `HAVING COUNT(...) > 2`: Filters busy employees

**Alternative Solution**:
```sql
-- Including project names as a list (MySQL)
SELECT
    e.first_name,
    e.last_name,
    COUNT(ep.project_id) AS project_count,
    SUM(ep.hours_allocated) AS total_hours,
    GROUP_CONCAT(p.name SEPARATOR ', ') AS projects
FROM employees e
JOIN employee_projects ep ON e.id = ep.employee_id
JOIN projects p ON ep.project_id = p.id
GROUP BY e.id, e.first_name, e.last_name
HAVING COUNT(ep.project_id) > 2
ORDER BY project_count DESC;
```

**Common Mistakes**:
- Using COUNT(*) instead of COUNT(project_id) (could count NULLs differently)
- Not joining all required tables
- Forgetting that same employee can have multiple project assignments

---

### Medium Question 6: Sales Comparison

**Question**: Compare the number of sales and total revenue between 'Electronics' and 'Furniture' categories for the year 2023. Show which category performs better.

**Difficulty**: Medium

**Approach**:
1. Filter to 2023 and specific categories
2. Group by category
3. Calculate metrics

**Solution**:
```sql
SELECT
    category,
    COUNT(*) AS num_sales,
    SUM(amount) AS total_revenue,
    ROUND(AVG(amount), 2) AS avg_sale,
    RANK() OVER (ORDER BY SUM(amount) DESC) AS revenue_rank
FROM sales
WHERE sale_date >= '2023-01-01'
  AND sale_date < '2024-01-01'
  AND category IN ('Electronics', 'Furniture')
GROUP BY category
ORDER BY total_revenue DESC;
```

**Explanation**:
- `WHERE`: Filters dates and categories before grouping
- `IN ('Electronics', 'Furniture')`: Limits to comparison categories
- `RANK()`: Shows which category performs better
- Multiple metrics for comprehensive comparison

**Alternative Solution**:
```sql
-- Side-by-side comparison using CASE
SELECT
    SUM(CASE WHEN category = 'Electronics' THEN 1 ELSE 0 END) AS electronics_count,
    SUM(CASE WHEN category = 'Furniture' THEN 1 ELSE 0 END) AS furniture_count,
    SUM(CASE WHEN category = 'Electronics' THEN amount ELSE 0 END) AS electronics_revenue,
    SUM(CASE WHEN category = 'Furniture' THEN amount ELSE 0 END) AS furniture_revenue
FROM sales
WHERE sale_date >= '2023-01-01'
  AND sale_date < '2024-01-01'
  AND category IN ('Electronics', 'Furniture');
```

**Common Mistakes**:
- Using HAVING to filter categories (use WHERE for row-level filters)
- Not handling date ranges properly
- Comparing aggregates incorrectly

---

### Medium Question 7: Department Headcount Analysis

**Question**: Find departments where the number of employees earning above $80,000 is greater than the number earning $80,000 or below. Show department ID, high earners count, and low earners count.

**Difficulty**: Medium

**Approach**:
1. Use conditional aggregation (CASE inside COUNT/SUM)
2. Group by department
3. Compare counts with HAVING

**Solution**:
```sql
SELECT
    department_id,
    COUNT(CASE WHEN salary > 80000 THEN 1 END) AS high_earners,
    COUNT(CASE WHEN salary <= 80000 THEN 1 END) AS low_earners
FROM employees
WHERE department_id IS NOT NULL
GROUP BY department_id
HAVING COUNT(CASE WHEN salary > 80000 THEN 1 END) >
       COUNT(CASE WHEN salary <= 80000 THEN 1 END)
ORDER BY department_id;
```

**Explanation**:
- `CASE WHEN ... THEN 1 END`: Returns 1 or NULL
- `COUNT(CASE ...)`: Counts non-NULL values (conditional count)
- HAVING compares the two conditional counts
- WHERE filters out NULL department_id

**Alternative Solution**:
```sql
-- Using SUM with boolean expression
SELECT
    department_id,
    SUM(CASE WHEN salary > 80000 THEN 1 ELSE 0 END) AS high_earners,
    SUM(CASE WHEN salary <= 80000 THEN 1 ELSE 0 END) AS low_earners
FROM employees
WHERE department_id IS NOT NULL
GROUP BY department_id
HAVING SUM(CASE WHEN salary > 80000 THEN 1 ELSE 0 END) >
       SUM(CASE WHEN salary <= 80000 THEN 1 ELSE 0 END)
ORDER BY department_id;
```

**Common Mistakes**:
- Incorrect CASE syntax
- Using COUNT(*) instead of conditional count
- Not handling NULL salaries or department_ids
- Using WHERE instead of HAVING for aggregate comparison

---

## Common Mistakes

### Mistake 1: Mixing Grouped and Non-Grouped Columns

This is the most common aggregate error!

**Wrong:**
```sql
-- ERROR: first_name is not in GROUP BY
SELECT
    department_id,
    first_name,        -- Which first_name? There are many!
    COUNT(*)
FROM employees
GROUP BY department_id;
```

**Correct:**
```sql
-- Option 1: Add to GROUP BY (different result!)
SELECT
    department_id,
    first_name,
    COUNT(*)
FROM employees
GROUP BY department_id, first_name;

-- Option 2: Use aggregate on the column
SELECT
    department_id,
    MIN(first_name) AS sample_name,  -- Or MAX, etc.
    COUNT(*)
FROM employees
GROUP BY department_id;

-- Option 3: Remove from SELECT
SELECT
    department_id,
    COUNT(*)
FROM employees
GROUP BY department_id;
```

**Why it matters**: When grouping, each non-aggregated column must be in GROUP BY because SQL doesn't know which value to pick from the group.

**Visual Explanation:**
```
department_id = 1 group contains:
- Alice
- Bob
- Carol

Which name should appear in SELECT first_name?
SQL can't decide, so it's an error!
```

---

### Mistake 2: Using WHERE Instead of HAVING for Aggregates

**Wrong:**
```sql
-- ERROR: Cannot use aggregate in WHERE
SELECT
    department_id,
    COUNT(*) AS emp_count
FROM employees
WHERE COUNT(*) > 3    -- WRONG! Aggregates don't exist yet
GROUP BY department_id;
```

**Correct:**
```sql
SELECT
    department_id,
    COUNT(*) AS emp_count
FROM employees
GROUP BY department_id
HAVING COUNT(*) > 3;   -- CORRECT! Filter after grouping
```

**Why it matters**: WHERE filters rows BEFORE grouping; aggregates only exist AFTER grouping.

**Execution Order Reminder:**
```
FROM -> WHERE -> GROUP BY -> HAVING -> SELECT -> ORDER BY

WHERE: Filters individual rows (COUNT doesn't exist yet!)
HAVING: Filters groups (COUNT has been calculated)
```

---

### Mistake 3: NULL Handling in Aggregates

**Issue:** Aggregates ignore NULLs, which can give unexpected results.

**Problem Example:**
```sql
-- Table has: bonus = [5000, NULL, 3000, NULL, 4000]

SELECT AVG(bonus) FROM employees;  -- Returns 4000, not 2400!
-- AVG calculates: (5000 + 3000 + 4000) / 3 = 4000
-- NULLs are ignored in both sum and count
```

**Correct Approaches:**
```sql
-- If NULLs should be treated as 0:
SELECT AVG(COALESCE(bonus, 0)) FROM employees;  -- Returns 2400

-- If you want to see the impact:
SELECT
    AVG(bonus) AS avg_non_null,
    AVG(COALESCE(bonus, 0)) AS avg_with_zeros,
    COUNT(bonus) AS non_null_count,
    COUNT(*) AS total_count
FROM employees;
```

**COUNT Behavior:**
```sql
SELECT
    COUNT(*) AS all_rows,           -- Counts everything
    COUNT(bonus) AS non_null_bonus  -- Counts only non-NULL
FROM employees;
```

**Why it matters**: Not understanding NULL handling leads to incorrect business metrics!

---

### Mistake 4: GROUP BY Column Order Issues

**Issue:** While GROUP BY order doesn't affect results, it affects readability and can cause confusion.

**Less Clear:**
```sql
SELECT
    department_id,
    YEAR(hire_date) AS hire_year,
    COUNT(*)
FROM employees
GROUP BY YEAR(hire_date), department_id;  -- Order doesn't match SELECT
```

**Better:**
```sql
SELECT
    department_id,
    YEAR(hire_date) AS hire_year,
    COUNT(*)
FROM employees
GROUP BY department_id, YEAR(hire_date);  -- Matches SELECT order
```

**Important Note on Aliases:**
```sql
-- Some databases don't allow aliases in GROUP BY
SELECT
    YEAR(hire_date) AS hire_year,
    COUNT(*)
FROM employees
GROUP BY hire_year;  -- May fail! Use the expression instead:
-- GROUP BY YEAR(hire_date)
```

---

### Mistake 5: Forgetting DISTINCT in COUNT

**Issue:** Counting duplicates when you need unique values.

**Wrong:**
```sql
-- Counting all customer references, not unique customers
SELECT COUNT(customer_id) AS customers_served
FROM sales;  -- Returns 1000 (includes repeat customers)
```

**Correct:**
```sql
SELECT COUNT(DISTINCT customer_id) AS unique_customers
FROM sales;  -- Returns 250 (unique customers only)
```

**Use Cases:**
```sql
SELECT
    COUNT(*) AS total_transactions,
    COUNT(customer_id) AS transactions_with_customer,
    COUNT(DISTINCT customer_id) AS unique_customers,
    COUNT(DISTINCT category) AS categories_sold
FROM sales;
```

---

### Mistake 6: Incorrect Aggregate Placement

**Wrong:**
```sql
-- Using aggregate in WHERE
SELECT * FROM employees
WHERE salary > AVG(salary);  -- ERROR!

-- Using aggregate without GROUP BY when comparing
SELECT department_id, salary
FROM employees
WHERE salary = MAX(salary);  -- ERROR!
```

**Correct:**
```sql
-- Use subquery for row-level comparison
SELECT * FROM employees
WHERE salary > (SELECT AVG(salary) FROM employees);

-- For per-department max, use correlated subquery or JOIN
SELECT e.*
FROM employees e
WHERE e.salary = (
    SELECT MAX(e2.salary)
    FROM employees e2
    WHERE e2.department_id = e.department_id
);
```

---

### Mistake 7: HAVING Without GROUP BY

**Issue:** HAVING can technically work without GROUP BY (treats whole table as one group), but it's usually a mistake.

**Likely Wrong:**
```sql
-- This works but probably not intended
SELECT COUNT(*) AS total
FROM employees
HAVING COUNT(*) > 10;  -- Filters the single total, returns nothing or total
```

**What You Probably Meant:**
```sql
-- Per-department filter
SELECT department_id, COUNT(*) AS emp_count
FROM employees
GROUP BY department_id
HAVING COUNT(*) > 10;
```

---

## Summary & Key Takeaways

### What You Learned
- COUNT, SUM, AVG, MIN, MAX aggregate functions
- GROUP BY for categorizing data
- HAVING for filtering groups
- Combining aggregates with JOINs
- Execution order: WHERE -> GROUP BY -> HAVING
- NULL handling in aggregates

### Aggregate Functions Quick Reference

| Function | Purpose | NULL Handling | Example |
|----------|---------|---------------|---------|
| COUNT(*) | Count all rows | Includes NULLs | `COUNT(*)` |
| COUNT(col) | Count non-NULL values | Ignores NULLs | `COUNT(email)` |
| COUNT(DISTINCT col) | Count unique values | Ignores NULLs | `COUNT(DISTINCT country)` |
| SUM(col) | Total of values | Ignores NULLs | `SUM(amount)` |
| AVG(col) | Mean of values | Ignores NULLs | `AVG(salary)` |
| MIN(col) | Smallest value | Ignores NULLs | `MIN(price)` |
| MAX(col) | Largest value | Ignores NULLs | `MAX(sale_date)` |

### WHERE vs HAVING Cheat Sheet

```
┌─────────────┬─────────────────────────────────────┐
│   Clause    │              Use For                │
├─────────────┼─────────────────────────────────────┤
│   WHERE     │ Filtering individual rows           │
│             │ Before grouping occurs              │
│             │ Regular column conditions           │
│             │ Example: WHERE salary > 50000       │
├─────────────┼─────────────────────────────────────┤
│   HAVING    │ Filtering groups                    │
│             │ After grouping occurs               │
│             │ Aggregate conditions                │
│             │ Example: HAVING COUNT(*) > 5        │
└─────────────┴─────────────────────────────────────┘
```

### Common Query Patterns

**Pattern 1: Basic Grouping**
```sql
SELECT category, COUNT(*), SUM(amount)
FROM sales
GROUP BY category;
```

**Pattern 2: Grouping with Filter**
```sql
SELECT category, SUM(amount)
FROM sales
WHERE sale_date >= '2023-01-01'  -- Filter rows
GROUP BY category
HAVING SUM(amount) > 1000;       -- Filter groups
```

**Pattern 3: Grouping with JOIN**
```sql
SELECT d.name, COUNT(e.id), AVG(e.salary)
FROM departments d
LEFT JOIN employees e ON d.id = e.department_id
GROUP BY d.id, d.name;
```

**Pattern 4: Conditional Aggregation**
```sql
SELECT
    department_id,
    COUNT(CASE WHEN salary > 80000 THEN 1 END) AS high_earners,
    COUNT(CASE WHEN salary <= 80000 THEN 1 END) AS others
FROM employees
GROUP BY department_id;
```

### Interview Tips

1. **Always mention execution order** - Shows you understand WHERE vs HAVING
2. **Consider NULL values** - Ask about NULL handling in the data
3. **Use aliases** - Makes results readable
4. **Round decimals** - `ROUND(AVG(salary), 2)` looks professional
5. **Explain your GROUP BY** - Why these columns specifically?

### Practice Exercises

1. Find the total and average salary for each department
2. Count customers by country, only show countries with > 5 customers
3. Calculate monthly revenue trends for the past year
4. Find the top 3 customers by total purchase amount
5. Compare sales performance across product categories

### What's Next?

In **Phase 5: JOINs**, you'll learn:
- INNER JOIN, LEFT JOIN, RIGHT JOIN, FULL JOIN
- Self JOINs
- Multiple table JOINs
- JOIN with aggregates
- Performance considerations
- Complex multi-table queries

---

**Ready to connect tables? Let's go to Phase 5!**
