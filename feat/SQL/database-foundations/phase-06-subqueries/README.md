# Phase 6: Subqueries & Nested Queries

## Table of Contents
- [Introduction to Subqueries](#introduction-to-subqueries)
- [Subqueries in SELECT](#subqueries-in-select)
- [Subqueries in WHERE](#subqueries-in-where)
- [Subqueries in FROM (Derived Tables)](#subqueries-in-from-derived-tables)
- [Correlated vs Non-Correlated Subqueries](#correlated-vs-non-correlated-subqueries)
- [EXISTS and NOT EXISTS](#exists-and-not-exists)
- [IN and NOT IN with Subqueries](#in-and-not-in-with-subqueries)
- [ANY and ALL Operators](#any-and-all-operators)
- [Common Table Expressions (CTEs)](#common-table-expressions-ctes)
- [Query Execution Flow](#query-execution-flow)
- [Code Examples](#code-examples)
- [Practice Questions (Hard)](#practice-questions-hard)
- [Common Mistakes](#common-mistakes)
- [Subquery vs JOIN Comparison](#subquery-vs-join-comparison)
- [Summary & Next Steps](#summary--next-steps)

---

## Sample Tables Reference

Throughout this guide, we'll use these tables:

```sql
-- employees table
CREATE TABLE employees (
    id INT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100),
    department_id INT,
    manager_id INT,
    salary DECIMAL(10, 2),
    hire_date DATE
);

-- departments table
CREATE TABLE departments (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    budget DECIMAL(15, 2),
    location VARCHAR(100)
);

-- sales table
CREATE TABLE sales (
    id INT PRIMARY KEY,
    employee_id INT,
    customer_id INT,
    product VARCHAR(100),
    amount DECIMAL(10, 2),
    sale_date DATE,
    category VARCHAR(50)
);

-- customers table
CREATE TABLE customers (
    id INT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100),
    country VARCHAR(50),
    registration_date DATE
);

-- projects table
CREATE TABLE projects (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    department_id INT,
    budget DECIMAL(15, 2),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20)
);

-- employee_projects table
CREATE TABLE employee_projects (
    employee_id INT,
    project_id INT,
    role VARCHAR(50),
    hours_allocated INT,
    PRIMARY KEY (employee_id, project_id)
);
```

---

## Introduction to Subqueries

### WHY
Subqueries enable you to build complex queries by embedding one query inside another. Real-world scenarios:
- Find employees who earn above the average salary
- List customers who made purchases exceeding a threshold
- Find products that have never been sold
- Compare individual values to aggregated results
- Build dynamic filters based on other data

90% of advanced SQL queries involve subqueries. Mastering them is essential for:
- Complex data analysis
- Report generation
- Dynamic filtering
- Interview success (almost always asked!)

### WHAT
A subquery (also called inner query or nested query) is a query placed inside another query. The outer query uses the subquery's result.

### HOW
```sql
SELECT column1
FROM table1
WHERE column2 = (SELECT column FROM table2 WHERE condition);
```

### WHEN
Use subqueries when you need to:
- Compare values to aggregated results
- Filter based on data from other tables
- Create derived/calculated columns
- Build complex multi-step logic

### Subquery Execution Model
```
┌─────────────────────────────────────┐
│          OUTER QUERY                │
│  ┌───────────────────────────┐      │
│  │      INNER QUERY          │      │
│  │   (executes first*)       │      │
│  └───────────────────────────┘      │
│        Result passed to             │
│        outer query                  │
└─────────────────────────────────────┘
* For non-correlated subqueries
```

---

## Subqueries in SELECT

### WHY
Use subqueries in SELECT to create calculated columns that depend on aggregated or looked-up data from other tables/queries.

### WHAT
A subquery in the SELECT clause returns a single value (scalar subquery) for each row of the outer query.

### HOW
```sql
SELECT
    column1,
    (SELECT aggregate_function(column) FROM table WHERE condition) AS alias
FROM table1;
```

### WHEN
- When you need to display a calculated value alongside each row
- When you need to look up values from related tables
- When creating denormalized result sets

---

### Example 1: Scalar Subquery - Compare to Average
```sql
-- Show each employee's salary vs company average
SELECT
    first_name,
    last_name,
    salary,
    (SELECT AVG(salary) FROM employees) AS company_avg,
    salary - (SELECT AVG(salary) FROM employees) AS diff_from_avg
FROM employees
ORDER BY diff_from_avg DESC;
```

**Result:**
```
first_name | last_name | salary   | company_avg | diff_from_avg
-----------+-----------+----------+-------------+--------------
Alice      | Johnson   | 120000   | 85000       | 35000
Bob        | Smith     | 95000    | 85000       | 10000
Carol      | Williams  | 80000    | 85000       | -5000
```

**Explanation:**
- The subquery `(SELECT AVG(salary) FROM employees)` executes once
- Its result (85000) is used for every row
- We can reference it multiple times without re-calculation

---

### Example 2: Subquery with Lookup
```sql
-- Show each employee with their department name
SELECT
    e.first_name,
    e.last_name,
    e.salary,
    (SELECT d.name
     FROM departments d
     WHERE d.id = e.department_id) AS department_name
FROM employees e;
```

**Note:** While this works, a JOIN is typically more efficient for this use case.

---

## Subqueries in WHERE

### WHY
Subqueries in WHERE create dynamic filters based on calculated or aggregated values. This is the most common use of subqueries.

### WHAT
A subquery in WHERE filters rows based on the subquery's result, which can be a single value, multiple values, or a boolean.

### HOW
```sql
-- Single value comparison
WHERE column = (SELECT single_value FROM ...)

-- Multiple value comparison
WHERE column IN (SELECT values FROM ...)

-- Existence check
WHERE EXISTS (SELECT 1 FROM ... WHERE condition)
```

### WHEN
- Filtering by aggregated values (average, max, min)
- Checking membership in dynamic lists
- Verifying existence of related records
- Building complex conditional logic

---

### Example 3: Compare to Aggregate
```sql
-- Find employees earning above average
SELECT first_name, last_name, salary
FROM employees
WHERE salary > (SELECT AVG(salary) FROM employees);
```

**Execution Flow:**
```
1. Execute subquery: AVG(salary) = 85000
2. Replace subquery with result: WHERE salary > 85000
3. Execute outer query with filter
```

---

### Example 4: Multiple Conditions with Subqueries
```sql
-- Find employees earning between average and maximum salary
SELECT first_name, last_name, salary
FROM employees
WHERE salary > (SELECT AVG(salary) FROM employees)
  AND salary < (SELECT MAX(salary) FROM employees);
```

---

## Subqueries in FROM (Derived Tables)

### WHY
Derived tables let you create temporary result sets to query against. This enables multi-step transformations and complex aggregations.

### WHAT
A subquery in FROM creates a virtual table that exists only for the duration of the query. It MUST have an alias.

### HOW
```sql
SELECT *
FROM (
    SELECT column1, aggregate_function(column2) AS agg_col
    FROM table
    GROUP BY column1
) AS derived_table_alias
WHERE agg_col > some_value;
```

### WHEN
- When you need to filter on aggregated results (alternative to HAVING)
- When building multi-step transformations
- When you need to join aggregated data with detail data
- When simplifying complex logic

---

### Example 5: Derived Table for Aggregation Filtering
```sql
-- Find departments with average salary above $80,000
SELECT
    dept_id,
    dept_avg_salary
FROM (
    SELECT
        department_id AS dept_id,
        AVG(salary) AS dept_avg_salary
    FROM employees
    GROUP BY department_id
) AS dept_averages
WHERE dept_avg_salary > 80000;
```

**Why use this instead of HAVING?**
- More readable for complex conditions
- Can join with other tables/subqueries
- Can apply window functions to aggregated results

---

### Example 6: Derived Table with JOIN
```sql
-- Get employee details along with their department's average salary
SELECT
    e.first_name,
    e.last_name,
    e.salary,
    da.avg_salary AS dept_avg_salary,
    e.salary - da.avg_salary AS diff_from_dept_avg
FROM employees e
JOIN (
    SELECT department_id, AVG(salary) AS avg_salary
    FROM employees
    GROUP BY department_id
) AS da ON e.department_id = da.department_id
ORDER BY diff_from_dept_avg DESC;
```

---

## Correlated vs Non-Correlated Subqueries

### Non-Correlated Subqueries

### WHY
Non-correlated subqueries are independent - they execute once and their result is used by the outer query.

### WHAT
A non-correlated subquery doesn't reference any columns from the outer query. It can run on its own.

### HOW
```sql
-- The inner query has no reference to outer query columns
SELECT * FROM employees
WHERE salary > (SELECT AVG(salary) FROM employees);
```

### WHEN
- Comparing to global aggregates
- Checking membership in static lists
- Simple lookups

**Execution Flow - Non-Correlated:**
```
┌─────────────────────────────────┐
│  1. Execute inner query ONCE    │
│     Result: 85000               │
├─────────────────────────────────┤
│  2. Execute outer query         │
│     WHERE salary > 85000        │
└─────────────────────────────────┘
```

---

### Correlated Subqueries

### WHY
Correlated subqueries enable row-by-row comparisons where each row's filter depends on its own values.

### WHAT
A correlated subquery references columns from the outer query. It executes once for EACH row of the outer query.

### HOW
```sql
-- The inner query references 'e.department_id' from outer query
SELECT * FROM employees e
WHERE salary > (
    SELECT AVG(salary)
    FROM employees
    WHERE department_id = e.department_id  -- Correlation!
);
```

### WHEN
- Comparing to group-specific aggregates
- Finding relative rankings within groups
- Row-by-row existence checks
- Complex conditional logic

**Execution Flow - Correlated:**
```
┌─────────────────────────────────────────────┐
│  FOR EACH row in outer query:               │
│    1. Get current row's department_id       │
│    2. Execute inner query with that value   │
│    3. Compare current row against result    │
│    4. Include/exclude row based on result   │
└─────────────────────────────────────────────┘
```

---

### Example 7: Correlated Subquery - Above Department Average
```sql
-- Find employees earning above their department's average
SELECT
    e.first_name,
    e.last_name,
    e.department_id,
    e.salary
FROM employees e
WHERE e.salary > (
    SELECT AVG(e2.salary)
    FROM employees e2
    WHERE e2.department_id = e.department_id  -- Correlated!
);
```

**Explanation:**
- For each employee row, the subquery calculates that specific department's average
- Employee in department 1: compared to department 1's average
- Employee in department 2: compared to department 2's average

**Performance Note:** Correlated subqueries can be slow on large datasets because they execute once per row.

---

### Example 8: Correlated Subquery with Ranking
```sql
-- Find the highest-paid employee in each department
SELECT
    e.first_name,
    e.last_name,
    e.department_id,
    e.salary
FROM employees e
WHERE e.salary = (
    SELECT MAX(e2.salary)
    FROM employees e2
    WHERE e2.department_id = e.department_id
);
```

---

## EXISTS and NOT EXISTS

### WHY
EXISTS checks whether a subquery returns any rows. It's often more efficient than IN for existence checks, especially with large datasets.

### WHAT
- `EXISTS`: Returns TRUE if subquery returns at least one row
- `NOT EXISTS`: Returns TRUE if subquery returns no rows

### HOW
```sql
SELECT * FROM table1 t1
WHERE EXISTS (
    SELECT 1 FROM table2 t2
    WHERE t2.foreign_key = t1.primary_key
);
```

### WHEN
- Checking for existence of related records
- Finding rows with/without matching records in another table
- Performance-critical existence checks (often faster than IN)

---

### Example 9: EXISTS - Employees with Sales
```sql
-- Find employees who have made at least one sale
SELECT e.first_name, e.last_name
FROM employees e
WHERE EXISTS (
    SELECT 1
    FROM sales s
    WHERE s.employee_id = e.id
);
```

**Why `SELECT 1`?**
- EXISTS only checks if rows exist, it doesn't use the selected values
- `SELECT 1` or `SELECT *` - performance is the same
- `SELECT 1` makes the intent clearer

---

### Example 10: NOT EXISTS - Employees without Sales
```sql
-- Find employees who have never made a sale
SELECT e.first_name, e.last_name
FROM employees e
WHERE NOT EXISTS (
    SELECT 1
    FROM sales s
    WHERE s.employee_id = e.id
);
```

**Equivalent using LEFT JOIN:**
```sql
SELECT e.first_name, e.last_name
FROM employees e
LEFT JOIN sales s ON s.employee_id = e.id
WHERE s.id IS NULL;
```

---

### Example 11: EXISTS with Multiple Conditions
```sql
-- Find departments that have employees earning over $100,000
SELECT d.name
FROM departments d
WHERE EXISTS (
    SELECT 1
    FROM employees e
    WHERE e.department_id = d.id
      AND e.salary > 100000
);
```

---

## IN and NOT IN with Subqueries

### WHY
IN with subqueries creates dynamic membership lists based on query results.

### WHAT
- `IN`: Returns TRUE if value matches any value in the subquery result
- `NOT IN`: Returns TRUE if value doesn't match any value in the subquery result

### HOW
```sql
SELECT * FROM table1
WHERE column IN (SELECT column FROM table2 WHERE condition);
```

### WHEN
- Filtering by a dynamically generated list
- Simple membership checks
- When the subquery returns a small to medium result set

---

### Example 12: IN with Subquery
```sql
-- Find customers who have made purchases over $500
SELECT c.first_name, c.last_name, c.email
FROM customers c
WHERE c.id IN (
    SELECT DISTINCT s.customer_id
    FROM sales s
    WHERE s.amount > 500
);
```

---

### Example 13: NOT IN with Subquery
```sql
-- Find products that have never been sold
SELECT p.name, p.category
FROM products p
WHERE p.name NOT IN (
    SELECT DISTINCT s.product
    FROM sales s
);
```

**DANGER: NULL Values in NOT IN**

```sql
-- If any customer_id in sales is NULL, this returns NO ROWS!
SELECT * FROM customers
WHERE id NOT IN (SELECT customer_id FROM sales);

-- Safe version:
SELECT * FROM customers
WHERE id NOT IN (
    SELECT customer_id FROM sales
    WHERE customer_id IS NOT NULL
);
```

---

## ANY and ALL Operators

### ANY (SOME)

### WHY
ANY compares a value to each value in a list and returns TRUE if any comparison is true.

### WHAT
`value operator ANY (subquery)` - TRUE if comparison is TRUE for at least one value

### HOW
```sql
WHERE column > ANY (SELECT column FROM ...)
-- Equivalent to: WHERE column > MIN(subquery_results)
```

### WHEN
- Finding values greater/less than the minimum/maximum of a group
- Flexible comparisons against multiple values

---

### Example 14: ANY - Salary Comparison
```sql
-- Find employees earning more than ANY employee in department 1
-- (i.e., more than the lowest-paid in department 1)
SELECT first_name, last_name, salary
FROM employees
WHERE salary > ANY (
    SELECT salary
    FROM employees
    WHERE department_id = 1
);
```

**Equivalents:**
```sql
-- > ANY is equivalent to > MIN
WHERE salary > (SELECT MIN(salary) FROM employees WHERE department_id = 1);

-- = ANY is equivalent to IN
WHERE department_id = ANY (SELECT id FROM departments WHERE location = 'NYC');
-- Same as:
WHERE department_id IN (SELECT id FROM departments WHERE location = 'NYC');
```

---

### ALL

### WHY
ALL compares a value to every value in a list and returns TRUE only if all comparisons are true.

### WHAT
`value operator ALL (subquery)` - TRUE if comparison is TRUE for every value

### HOW
```sql
WHERE column > ALL (SELECT column FROM ...)
-- Equivalent to: WHERE column > MAX(subquery_results)
```

### WHEN
- Finding values greater than all values in a group
- Ensuring a condition holds against every value

---

### Example 15: ALL - Top Earners
```sql
-- Find employees earning more than ALL employees in department 2
-- (i.e., more than the highest-paid in department 2)
SELECT first_name, last_name, salary, department_id
FROM employees
WHERE salary > ALL (
    SELECT salary
    FROM employees
    WHERE department_id = 2
);
```

**Equivalents:**
```sql
-- > ALL is equivalent to > MAX
WHERE salary > (SELECT MAX(salary) FROM employees WHERE department_id = 2);

-- <> ALL is equivalent to NOT IN
WHERE department_id <> ALL (SELECT id FROM departments WHERE budget < 100000);
-- Same as:
WHERE department_id NOT IN (SELECT id FROM departments WHERE budget < 100000);
```

---

### ANY vs ALL Summary

| Operator | Meaning | Equivalent |
|----------|---------|------------|
| `> ANY` | Greater than at least one | `> MIN(...)` |
| `< ANY` | Less than at least one | `< MAX(...)` |
| `= ANY` | Equal to at least one | `IN (...)` |
| `> ALL` | Greater than every value | `> MAX(...)` |
| `< ALL` | Less than every value | `< MIN(...)` |
| `<> ALL` | Not equal to any value | `NOT IN (...)` |

---

## Common Table Expressions (CTEs)

### WHY
CTEs make complex queries readable by breaking them into named, logical building blocks. They're like temporary named result sets.

### WHAT
A CTE defines a temporary result set that you can reference within a SELECT, INSERT, UPDATE, or DELETE statement.

### HOW
```sql
WITH cte_name AS (
    SELECT column1, column2
    FROM table
    WHERE condition
)
SELECT * FROM cte_name WHERE condition;
```

### WHEN
- When you have complex queries with multiple steps
- When you reference the same subquery multiple times
- When you want self-documenting, readable code
- As an alternative to derived tables
- For recursive queries

---

### Example 16: Basic CTE
```sql
-- Calculate department statistics and filter
WITH dept_stats AS (
    SELECT
        department_id,
        AVG(salary) AS avg_salary,
        COUNT(*) AS employee_count
    FROM employees
    GROUP BY department_id
)
SELECT
    d.name,
    ds.avg_salary,
    ds.employee_count
FROM dept_stats ds
JOIN departments d ON ds.department_id = d.id
WHERE ds.avg_salary > 80000;
```

---

### Example 17: Multiple CTEs
```sql
-- Complex analysis with multiple CTEs
WITH
high_earners AS (
    SELECT id, first_name, last_name, department_id, salary
    FROM employees
    WHERE salary > 100000
),
dept_counts AS (
    SELECT department_id, COUNT(*) AS high_earner_count
    FROM high_earners
    GROUP BY department_id
)
SELECT
    d.name AS department,
    COALESCE(dc.high_earner_count, 0) AS high_earners
FROM departments d
LEFT JOIN dept_counts dc ON d.id = dc.department_id
ORDER BY high_earners DESC;
```

---

### Example 18: CTE with Multiple References
```sql
-- Reference the same CTE multiple times
WITH avg_salaries AS (
    SELECT department_id, AVG(salary) AS avg_sal
    FROM employees
    GROUP BY department_id
)
SELECT
    e.first_name,
    e.last_name,
    e.salary,
    a1.avg_sal AS own_dept_avg,
    a2.avg_sal AS manager_dept_avg
FROM employees e
JOIN avg_salaries a1 ON e.department_id = a1.department_id
JOIN employees mgr ON e.manager_id = mgr.id
JOIN avg_salaries a2 ON mgr.department_id = a2.department_id;
```

---

### CTE vs Subquery

| Feature | CTE | Subquery |
|---------|-----|----------|
| Readability | Higher (named) | Lower (nested) |
| Reusability | Can reference multiple times | Must repeat |
| Recursion | Supported | Not supported |
| Performance | Usually same | Usually same |
| Use case | Complex multi-step queries | Simple one-time use |

---

## Query Execution Flow

### Subquery Execution Order

Understanding when subqueries execute is crucial for optimization:

```
┌───────────────────────────────────────────────────────────┐
│                    EXECUTION ORDER                         │
├───────────────────────────────────────────────────────────┤
│                                                           │
│  1. FROM clause subqueries (derived tables)               │
│     - Create virtual tables first                         │
│                                                           │
│  2. WHERE clause subqueries                               │
│     - Non-correlated: Execute once, cache result          │
│     - Correlated: Execute per outer row                   │
│                                                           │
│  3. SELECT clause subqueries                              │
│     - Execute for each row in result set                  │
│                                                           │
│  4. HAVING clause subqueries                              │
│     - Execute after GROUP BY                              │
│                                                           │
└───────────────────────────────────────────────────────────┘
```

### Detailed Execution Example

```sql
SELECT
    e.first_name,
    e.salary,
    (SELECT AVG(salary) FROM employees) AS company_avg  -- Step 4
FROM employees e
JOIN (
    SELECT department_id, AVG(salary) AS dept_avg
    FROM employees
    GROUP BY department_id
) AS da ON e.department_id = da.department_id           -- Step 1
WHERE e.salary > (SELECT AVG(salary) FROM employees)    -- Step 2
  AND EXISTS (                                          -- Step 3
      SELECT 1 FROM sales s WHERE s.employee_id = e.id
  );
```

**Execution Steps:**
```
Step 1: Execute FROM subquery
        → Create derived table 'da' with dept averages

Step 2: Execute non-correlated WHERE subquery
        → Calculate company AVG(salary) = 85000

Step 3: For each row, execute correlated EXISTS
        → Check if employee has sales

Step 4: For each result row, execute SELECT subquery
        → Get company average (may be cached)
```

---

## Code Examples

### Example 19: Nested Subqueries (Three Levels)
```sql
-- Find employees in departments that have projects with budget above average
SELECT first_name, last_name, department_id
FROM employees
WHERE department_id IN (
    SELECT department_id
    FROM projects
    WHERE budget > (
        SELECT AVG(budget)
        FROM projects
    )
);
```

**Execution (inside out):**
1. Calculate average project budget
2. Find departments with above-average budget projects
3. Find employees in those departments

---

### Example 20: Subquery with Aggregate in Having
```sql
-- Find departments where all employees earn above company average
SELECT
    d.name,
    COUNT(*) AS employee_count,
    MIN(e.salary) AS min_salary
FROM departments d
JOIN employees e ON d.id = e.department_id
GROUP BY d.id, d.name
HAVING MIN(e.salary) > (SELECT AVG(salary) FROM employees);
```

---

### Example 21: Correlated Subquery for Running Total
```sql
-- Calculate running total of sales per employee
SELECT
    s.id,
    s.employee_id,
    s.amount,
    s.sale_date,
    (
        SELECT SUM(s2.amount)
        FROM sales s2
        WHERE s2.employee_id = s.employee_id
          AND s2.sale_date <= s.sale_date
    ) AS running_total
FROM sales s
ORDER BY s.employee_id, s.sale_date;
```

---

### Example 22: Multiple Subqueries for Comparison
```sql
-- Categorize employees by salary relative to department
SELECT
    first_name,
    last_name,
    salary,
    department_id,
    CASE
        WHEN salary >= (
            SELECT MAX(salary) FROM employees e2
            WHERE e2.department_id = e.department_id
        ) THEN 'Highest in Dept'
        WHEN salary <= (
            SELECT MIN(salary) FROM employees e2
            WHERE e2.department_id = e.department_id
        ) THEN 'Lowest in Dept'
        ELSE 'Middle'
    END AS salary_rank
FROM employees e;
```

---

### Example 23: Subquery in UPDATE
```sql
-- Give 10% raise to employees in departments with above-average budget
UPDATE employees
SET salary = salary * 1.10
WHERE department_id IN (
    SELECT id
    FROM departments
    WHERE budget > (SELECT AVG(budget) FROM departments)
);
```

---

### Example 24: Subquery in DELETE
```sql
-- Delete sales records for inactive customers
DELETE FROM sales
WHERE customer_id IN (
    SELECT id
    FROM customers
    WHERE status = 'Inactive'
);
```

---

### Example 25: Complex CTE with Window Function Preview
```sql
-- Find employees who are top 3 earners in their department
WITH ranked_employees AS (
    SELECT
        id,
        first_name,
        last_name,
        department_id,
        salary,
        RANK() OVER (PARTITION BY department_id ORDER BY salary DESC) AS dept_rank
    FROM employees
)
SELECT
    re.first_name,
    re.last_name,
    d.name AS department,
    re.salary,
    re.dept_rank
FROM ranked_employees re
JOIN departments d ON re.department_id = d.id
WHERE re.dept_rank <= 3
ORDER BY d.name, re.dept_rank;
```

---

## Practice Questions (Hard)

### Hard Question 1: Multi-Level Subquery

**Question**: Find all customers who have spent more than the average spending of customers from their own country.

**Difficulty**: Hard

**Approach**: Use a correlated subquery to compare each customer's total spending against their country's average.

**Solution**:
```sql
SELECT
    c.first_name,
    c.last_name,
    c.country,
    customer_total.total_spent
FROM customers c
JOIN (
    SELECT customer_id, SUM(amount) AS total_spent
    FROM sales
    GROUP BY customer_id
) AS customer_total ON c.id = customer_total.customer_id
WHERE customer_total.total_spent > (
    SELECT AVG(country_totals.total)
    FROM (
        SELECT c2.country, SUM(s2.amount) AS total
        FROM customers c2
        JOIN sales s2 ON c2.id = s2.customer_id
        WHERE c2.country = c.country
        GROUP BY c2.id
    ) AS country_totals
)
ORDER BY c.country, customer_total.total_spent DESC;
```

**Alternative Using CTE**:
```sql
WITH customer_spending AS (
    SELECT
        c.id,
        c.first_name,
        c.last_name,
        c.country,
        SUM(s.amount) AS total_spent
    FROM customers c
    JOIN sales s ON c.id = s.customer_id
    GROUP BY c.id, c.first_name, c.last_name, c.country
),
country_avg AS (
    SELECT country, AVG(total_spent) AS avg_spent
    FROM customer_spending
    GROUP BY country
)
SELECT
    cs.first_name,
    cs.last_name,
    cs.country,
    cs.total_spent,
    ca.avg_spent AS country_average
FROM customer_spending cs
JOIN country_avg ca ON cs.country = ca.country
WHERE cs.total_spent > ca.avg_spent
ORDER BY cs.country, cs.total_spent DESC;
```

**Explanation**:
- First, calculate each customer's total spending
- Then, calculate the average spending per country
- Finally, compare each customer to their country's average

---

### Hard Question 2: Employees with No Projects

**Question**: Find employees who are not assigned to any active project, but work in departments that have at least one active project.

**Difficulty**: Hard

**Approach**: Combine NOT EXISTS with EXISTS to create the complex condition.

**Solution**:
```sql
SELECT
    e.first_name,
    e.last_name,
    d.name AS department
FROM employees e
JOIN departments d ON e.department_id = d.id
WHERE NOT EXISTS (
    -- Employee has no active projects
    SELECT 1
    FROM employee_projects ep
    JOIN projects p ON ep.project_id = p.id
    WHERE ep.employee_id = e.id
      AND p.status = 'Active'
)
AND EXISTS (
    -- But department has active projects
    SELECT 1
    FROM projects p
    WHERE p.department_id = e.department_id
      AND p.status = 'Active'
);
```

**Alternative Using JOINs**:
```sql
SELECT DISTINCT
    e.first_name,
    e.last_name,
    d.name AS department
FROM employees e
JOIN departments d ON e.department_id = d.id
JOIN projects dept_projects ON dept_projects.department_id = d.id
    AND dept_projects.status = 'Active'
LEFT JOIN employee_projects ep ON ep.employee_id = e.id
LEFT JOIN projects emp_projects ON ep.project_id = emp_projects.id
    AND emp_projects.status = 'Active'
WHERE emp_projects.id IS NULL;
```

**Explanation**:
- NOT EXISTS finds employees with no active project assignments
- EXISTS ensures their department has active projects
- The JOIN alternative uses LEFT JOIN with NULL check

---

### Hard Question 3: Top Performer Per Department

**Question**: Find the employee with the highest total sales in each department. Show their name, department, and total sales amount.

**Difficulty**: Hard

**Approach**: Use correlated subquery to find max sales per department.

**Solution**:
```sql
SELECT
    e.first_name,
    e.last_name,
    d.name AS department,
    emp_sales.total_sales
FROM employees e
JOIN departments d ON e.department_id = d.id
JOIN (
    SELECT employee_id, SUM(amount) AS total_sales
    FROM sales
    GROUP BY employee_id
) AS emp_sales ON e.id = emp_sales.employee_id
WHERE emp_sales.total_sales = (
    SELECT MAX(dept_totals.total_sales)
    FROM (
        SELECT e2.department_id, e2.id, SUM(s.amount) AS total_sales
        FROM employees e2
        JOIN sales s ON e2.id = s.employee_id
        GROUP BY e2.department_id, e2.id
    ) AS dept_totals
    WHERE dept_totals.department_id = e.department_id
);
```

**Alternative Using CTE**:
```sql
WITH employee_sales AS (
    SELECT
        e.id,
        e.first_name,
        e.last_name,
        e.department_id,
        SUM(s.amount) AS total_sales
    FROM employees e
    JOIN sales s ON e.id = s.employee_id
    GROUP BY e.id, e.first_name, e.last_name, e.department_id
),
dept_max AS (
    SELECT department_id, MAX(total_sales) AS max_sales
    FROM employee_sales
    GROUP BY department_id
)
SELECT
    es.first_name,
    es.last_name,
    d.name AS department,
    es.total_sales
FROM employee_sales es
JOIN dept_max dm ON es.department_id = dm.department_id
    AND es.total_sales = dm.max_sales
JOIN departments d ON es.department_id = d.id
ORDER BY es.total_sales DESC;
```

**Explanation**:
- First get each employee's total sales
- Then find the maximum per department
- Finally match employees to their department's max

---

### Hard Question 4: Consecutive Activity Check

**Question**: Find customers who made purchases in at least 3 different months during 2023.

**Difficulty**: Hard

**Approach**: Extract months from sale dates and count distinct months.

**Solution**:
```sql
SELECT
    c.first_name,
    c.last_name,
    c.email,
    active_months.month_count
FROM customers c
JOIN (
    SELECT
        customer_id,
        COUNT(DISTINCT DATE_FORMAT(sale_date, '%Y-%m')) AS month_count
    FROM sales
    WHERE sale_date BETWEEN '2023-01-01' AND '2023-12-31'
    GROUP BY customer_id
    HAVING COUNT(DISTINCT DATE_FORMAT(sale_date, '%Y-%m')) >= 3
) AS active_months ON c.id = active_months.customer_id
ORDER BY active_months.month_count DESC;
```

**PostgreSQL Version**:
```sql
SELECT
    c.first_name,
    c.last_name,
    c.email,
    active_months.month_count
FROM customers c
JOIN (
    SELECT
        customer_id,
        COUNT(DISTINCT TO_CHAR(sale_date, 'YYYY-MM')) AS month_count
    FROM sales
    WHERE sale_date BETWEEN '2023-01-01' AND '2023-12-31'
    GROUP BY customer_id
    HAVING COUNT(DISTINCT TO_CHAR(sale_date, 'YYYY-MM')) >= 3
) AS active_months ON c.id = active_months.customer_id
ORDER BY active_months.month_count DESC;
```

**Explanation**:
- Extract year-month from each sale
- Count distinct months per customer
- Filter customers with 3+ months of activity

---

### Hard Question 5: Budget Utilization Analysis

**Question**: Find projects where the allocated employee hours (assuming $100/hour rate) exceed 80% of the project budget. Show project name, budget, total allocated hours, and utilization percentage.

**Difficulty**: Hard

**Approach**: Calculate total hours per project and compare to budget.

**Solution**:
```sql
SELECT
    p.name AS project_name,
    p.budget,
    project_hours.total_hours,
    (project_hours.total_hours * 100) AS labor_cost,
    ROUND((project_hours.total_hours * 100 / p.budget) * 100, 2) AS utilization_pct
FROM projects p
JOIN (
    SELECT
        project_id,
        SUM(hours_allocated) AS total_hours
    FROM employee_projects
    GROUP BY project_id
) AS project_hours ON p.id = project_hours.project_id
WHERE (project_hours.total_hours * 100) > (p.budget * 0.8);
```

**Alternative with CTE and Additional Analysis**:
```sql
WITH project_labor AS (
    SELECT
        project_id,
        SUM(hours_allocated) AS total_hours,
        COUNT(*) AS employee_count
    FROM employee_projects
    GROUP BY project_id
),
project_analysis AS (
    SELECT
        p.id,
        p.name,
        p.budget,
        pl.total_hours,
        pl.employee_count,
        (pl.total_hours * 100) AS labor_cost,
        ROUND((pl.total_hours * 100 / p.budget) * 100, 2) AS utilization_pct
    FROM projects p
    JOIN project_labor pl ON p.id = pl.project_id
)
SELECT
    name AS project_name,
    budget,
    total_hours,
    labor_cost,
    utilization_pct,
    employee_count
FROM project_analysis
WHERE utilization_pct > 80
ORDER BY utilization_pct DESC;
```

**Explanation**:
- Calculate total allocated hours per project
- Calculate labor cost (hours * $100)
- Calculate utilization percentage
- Filter projects over 80% utilization

---

### Hard Question 6: Hierarchical Employee Query

**Question**: Find employees whose salary is higher than their manager's salary.

**Difficulty**: Hard

**Approach**: Self-join or correlated subquery comparing employee to manager.

**Solution using Correlated Subquery**:
```sql
SELECT
    e.first_name,
    e.last_name,
    e.salary AS employee_salary,
    (
        SELECT m.salary
        FROM employees m
        WHERE m.id = e.manager_id
    ) AS manager_salary
FROM employees e
WHERE e.salary > (
    SELECT m.salary
    FROM employees m
    WHERE m.id = e.manager_id
)
AND e.manager_id IS NOT NULL;
```

**Alternative using JOIN**:
```sql
SELECT
    e.first_name AS employee_first,
    e.last_name AS employee_last,
    e.salary AS employee_salary,
    m.first_name AS manager_first,
    m.last_name AS manager_last,
    m.salary AS manager_salary
FROM employees e
JOIN employees m ON e.manager_id = m.id
WHERE e.salary > m.salary;
```

**Explanation**:
- Self-join employees table (employee e to manager m)
- Compare salaries where employee's is higher
- JOIN is more efficient for this use case

---

### Hard Question 7: Year-over-Year Growth

**Question**: Find products that had higher sales in 2023 than in 2022. Show the product, both years' totals, and growth percentage.

**Difficulty**: Hard

**Approach**: Calculate yearly totals and compare using subqueries or CTEs.

**Solution**:
```sql
WITH yearly_sales AS (
    SELECT
        product,
        YEAR(sale_date) AS sale_year,
        SUM(amount) AS total_sales
    FROM sales
    WHERE YEAR(sale_date) IN (2022, 2023)
    GROUP BY product, YEAR(sale_date)
)
SELECT
    s2023.product,
    s2022.total_sales AS sales_2022,
    s2023.total_sales AS sales_2023,
    ROUND(
        ((s2023.total_sales - s2022.total_sales) / s2022.total_sales) * 100,
        2
    ) AS growth_pct
FROM yearly_sales s2023
JOIN yearly_sales s2022
    ON s2023.product = s2022.product
WHERE s2023.sale_year = 2023
  AND s2022.sale_year = 2022
  AND s2023.total_sales > s2022.total_sales
ORDER BY growth_pct DESC;
```

**Alternative without CTE**:
```sql
SELECT
    s2023.product,
    s2022.total_sales AS sales_2022,
    s2023.total_sales AS sales_2023,
    ROUND(
        ((s2023.total_sales - s2022.total_sales) / s2022.total_sales) * 100,
        2
    ) AS growth_pct
FROM (
    SELECT product, SUM(amount) AS total_sales
    FROM sales
    WHERE YEAR(sale_date) = 2023
    GROUP BY product
) AS s2023
JOIN (
    SELECT product, SUM(amount) AS total_sales
    FROM sales
    WHERE YEAR(sale_date) = 2022
    GROUP BY product
) AS s2022 ON s2023.product = s2022.product
WHERE s2023.total_sales > s2022.total_sales
ORDER BY growth_pct DESC;
```

**Explanation**:
- Calculate yearly totals per product
- Join 2022 and 2023 results on product
- Calculate growth percentage
- Filter for positive growth

---

### Hard Question 8: Complex Eligibility Check

**Question**: Find employees who are eligible for a bonus. Eligibility requires:
1. Employed for at least 1 year
2. Have made sales totaling at least $10,000
3. Work in a department with budget over $500,000
4. Not already at the maximum salary in their department

**Difficulty**: Hard

**Approach**: Combine multiple subquery conditions.

**Solution**:
```sql
SELECT
    e.first_name,
    e.last_name,
    e.hire_date,
    e.salary,
    d.name AS department,
    emp_sales.total_sales
FROM employees e
JOIN departments d ON e.department_id = d.id
JOIN (
    SELECT employee_id, SUM(amount) AS total_sales
    FROM sales
    GROUP BY employee_id
) AS emp_sales ON e.id = emp_sales.employee_id
WHERE
    -- Condition 1: Employed at least 1 year
    e.hire_date <= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
    -- Condition 2: Sales >= $10,000
    AND emp_sales.total_sales >= 10000
    -- Condition 3: Department budget > $500,000
    AND d.budget > 500000
    -- Condition 4: Not at max salary in department
    AND e.salary < (
        SELECT MAX(e2.salary)
        FROM employees e2
        WHERE e2.department_id = e.department_id
    )
ORDER BY emp_sales.total_sales DESC;
```

**Alternative with CTE for Clarity**:
```sql
WITH employee_sales AS (
    SELECT employee_id, SUM(amount) AS total_sales
    FROM sales
    GROUP BY employee_id
),
dept_max_salary AS (
    SELECT department_id, MAX(salary) AS max_salary
    FROM employees
    GROUP BY department_id
)
SELECT
    e.first_name,
    e.last_name,
    e.hire_date,
    e.salary,
    d.name AS department,
    d.budget AS dept_budget,
    es.total_sales,
    dms.max_salary AS dept_max_salary
FROM employees e
JOIN departments d ON e.department_id = d.id
JOIN employee_sales es ON e.id = es.employee_id
JOIN dept_max_salary dms ON e.department_id = dms.department_id
WHERE
    e.hire_date <= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
    AND es.total_sales >= 10000
    AND d.budget > 500000
    AND e.salary < dms.max_salary
ORDER BY es.total_sales DESC;
```

**Explanation**:
- Multiple conditions combined with AND
- Derived table for sales totals
- Correlated subquery (or pre-calculated) for department max salary
- CTE version is more readable and may be more efficient

---

## Common Mistakes

### Mistake 1: Performance Issues with Correlated Subqueries

**Problem**: Correlated subqueries execute once per row, causing severe performance issues on large tables.

**Wrong (Slow):**
```sql
-- Executes subquery for EVERY employee row
SELECT
    first_name,
    salary,
    (SELECT AVG(salary) FROM employees e2
     WHERE e2.department_id = e.department_id) AS dept_avg
FROM employees e;
```

**Better (Faster):**
```sql
-- Subquery executes once, then JOIN
SELECT
    e.first_name,
    e.salary,
    da.avg_salary AS dept_avg
FROM employees e
JOIN (
    SELECT department_id, AVG(salary) AS avg_salary
    FROM employees
    GROUP BY department_id
) AS da ON e.department_id = da.department_id;
```

**Performance Comparison:**
```
Correlated subquery: O(n * m) - n outer rows, m inner rows per outer
JOIN approach:       O(n + m) - calculate aggregates once, then join
```

**When Correlated is OK:**
- Small tables (< 1000 rows)
- EXISTS checks (often optimized by database)
- When JOIN alternative is too complex

---

### Mistake 2: Correlation Errors

**Problem**: Accidentally using wrong alias or missing correlation.

**Wrong:**
```sql
-- Missing correlation - returns same value for all rows!
SELECT e.first_name, e.salary
FROM employees e
WHERE e.salary > (
    SELECT AVG(salary)
    FROM employees
    -- Missing: WHERE department_id = e.department_id
);
```

**Correct:**
```sql
SELECT e.first_name, e.salary
FROM employees e
WHERE e.salary > (
    SELECT AVG(salary)
    FROM employees e2
    WHERE e2.department_id = e.department_id  -- Correlation added!
);
```

**Common Correlation Errors:**
- Forgetting alias for outer query table
- Using same alias for inner and outer
- Wrong column in correlation condition

---

### Mistake 3: NULL Handling in IN/NOT IN

**Problem**: NOT IN returns no rows when the subquery contains NULL.

**Disastrous:**
```sql
-- If ANY customer_id in sales is NULL, this returns ZERO rows!
SELECT * FROM customers
WHERE id NOT IN (SELECT customer_id FROM sales);
```

**Why?**
```
NOT IN (1, 2, NULL) evaluates as:
id <> 1 AND id <> 2 AND id <> NULL
= TRUE AND TRUE AND UNKNOWN
= UNKNOWN
= Not included in results
```

**Safe Solutions:**

```sql
-- Solution 1: Filter out NULLs
SELECT * FROM customers
WHERE id NOT IN (
    SELECT customer_id
    FROM sales
    WHERE customer_id IS NOT NULL
);

-- Solution 2: Use NOT EXISTS instead (preferred)
SELECT * FROM customers c
WHERE NOT EXISTS (
    SELECT 1 FROM sales s WHERE s.customer_id = c.id
);

-- Solution 3: Use LEFT JOIN
SELECT c.*
FROM customers c
LEFT JOIN sales s ON c.id = s.customer_id
WHERE s.customer_id IS NULL;
```

**Recommendation:** Prefer NOT EXISTS over NOT IN for existence checks.

---

### Mistake 4: Subquery Returns Multiple Rows Error

**Problem**: Using = with a subquery that returns more than one row.

**Error:**
```sql
-- ERROR: Subquery returns more than one row
SELECT * FROM employees
WHERE department_id = (
    SELECT department_id
    FROM projects
    WHERE status = 'Active'  -- Returns multiple rows!
);
```

**Solutions:**

```sql
-- Solution 1: Use IN for multiple values
SELECT * FROM employees
WHERE department_id IN (
    SELECT department_id
    FROM projects
    WHERE status = 'Active'
);

-- Solution 2: Use ANY
SELECT * FROM employees
WHERE department_id = ANY (
    SELECT department_id
    FROM projects
    WHERE status = 'Active'
);

-- Solution 3: Add aggregate to ensure single value
SELECT * FROM employees
WHERE department_id = (
    SELECT MIN(department_id)  -- Forces single value
    FROM projects
    WHERE status = 'Active'
);
```

---

### Mistake 5: Forgetting Derived Table Alias

**Problem**: Subqueries in FROM must have an alias.

**Wrong:**
```sql
-- ERROR: Every derived table must have its own alias
SELECT *
FROM (
    SELECT department_id, AVG(salary)
    FROM employees
    GROUP BY department_id
);  -- Missing alias!
```

**Correct:**
```sql
SELECT *
FROM (
    SELECT department_id, AVG(salary) AS avg_salary
    FROM employees
    GROUP BY department_id
) AS dept_averages;  -- Alias required!
```

---

### Mistake 6: Incorrect Subquery Column References

**Problem**: Selecting columns from outer query inside subquery incorrectly.

**Wrong:**
```sql
-- Can't select outer query columns in subquery result
SELECT e.first_name
FROM employees e
WHERE EXISTS (
    SELECT e.first_name  -- Don't do this!
    FROM sales s
    WHERE s.employee_id = e.id
);
```

**Correct:**
```sql
SELECT e.first_name
FROM employees e
WHERE EXISTS (
    SELECT 1  -- Just check existence
    FROM sales s
    WHERE s.employee_id = e.id
);
```

---

### Mistake 7: Misunderstanding ANY/ALL with Empty Sets

**Problem**: ANY and ALL with empty results have surprising behavior.

```sql
-- ALL with empty subquery returns TRUE!
SELECT * FROM employees
WHERE salary > ALL (SELECT salary FROM employees WHERE 1=0);
-- Returns ALL employees!

-- ANY with empty subquery returns FALSE!
SELECT * FROM employees
WHERE salary > ANY (SELECT salary FROM employees WHERE 1=0);
-- Returns NO employees!
```

**Best Practice:** Always ensure subqueries return expected results, or handle empty cases explicitly.

---

## Subquery vs JOIN Comparison

### When to Use Subqueries

| Scenario | Recommendation | Reason |
|----------|----------------|--------|
| Compare to aggregates | Subquery | Natural fit for "find above average" |
| Existence checks | EXISTS subquery | Often most efficient |
| Simple membership | IN subquery | Clear and readable |
| Multi-step logic | CTE | Best readability |
| Need result set once | Subquery | No need for JOIN overhead |

### When to Use JOINs

| Scenario | Recommendation | Reason |
|----------|----------------|--------|
| Display columns from both tables | JOIN | Subqueries can't do this |
| Large result sets | JOIN | Usually better optimized |
| Complex relationships | JOIN | More explicit relationships |
| Need data from multiple tables | JOIN | More flexible |
| UPDATE/DELETE with conditions | JOIN | Clearer than correlated subquery |

---

### Side-by-Side Comparison

**Scenario: Find employees with sales**

**Subquery (IN):**
```sql
SELECT first_name, last_name
FROM employees
WHERE id IN (SELECT DISTINCT employee_id FROM sales);
```

**Subquery (EXISTS):**
```sql
SELECT first_name, last_name
FROM employees e
WHERE EXISTS (SELECT 1 FROM sales s WHERE s.employee_id = e.id);
```

**JOIN:**
```sql
SELECT DISTINCT e.first_name, e.last_name
FROM employees e
JOIN sales s ON e.id = s.employee_id;
```

**Performance Notes:**
- EXISTS: Stops at first match (efficient for "at least one")
- IN: May be slower with large lists
- JOIN: Requires DISTINCT if employee has multiple sales

---

**Scenario: Find employees without sales**

**Subquery (NOT IN) - CAREFUL!:**
```sql
SELECT first_name, last_name
FROM employees
WHERE id NOT IN (
    SELECT employee_id FROM sales
    WHERE employee_id IS NOT NULL  -- Required!
);
```

**Subquery (NOT EXISTS) - Preferred:**
```sql
SELECT first_name, last_name
FROM employees e
WHERE NOT EXISTS (SELECT 1 FROM sales s WHERE s.employee_id = e.id);
```

**JOIN (LEFT JOIN + NULL check):**
```sql
SELECT e.first_name, e.last_name
FROM employees e
LEFT JOIN sales s ON e.id = s.employee_id
WHERE s.id IS NULL;
```

---

**Scenario: Display employee with department info**

**Subquery (possible but awkward):**
```sql
SELECT
    first_name,
    last_name,
    (SELECT name FROM departments WHERE id = e.department_id) AS dept_name
FROM employees e;
```

**JOIN (preferred):**
```sql
SELECT
    e.first_name,
    e.last_name,
    d.name AS dept_name
FROM employees e
JOIN departments d ON e.department_id = d.id;
```

---

### Performance Decision Tree

```
Need columns from multiple tables?
├── YES → Use JOIN
└── NO
    └── Existence check?
        ├── YES → Use EXISTS/NOT EXISTS
        └── NO
            └── Comparing to aggregate?
                ├── YES → Use Subquery
                └── NO
                    └── Filtering by list?
                        ├── YES (small list) → Use IN
                        ├── YES (large list) → Consider JOIN
                        └── NO → Evaluate both approaches
```

---

## Summary & Next Steps

### What You Learned

- Subqueries in SELECT, WHERE, FROM
- Correlated vs non-correlated subqueries
- EXISTS, NOT EXISTS for existence checks
- IN, NOT IN for membership testing
- ANY, ALL for set comparisons
- Common Table Expressions (CTEs)
- Query execution flow
- Performance considerations
- Common mistakes and how to avoid them
- When to use subqueries vs JOINs

### Key Takeaways

| Concept | Remember |
|---------|----------|
| Non-correlated | Executes once, independent |
| Correlated | Executes per row, references outer |
| EXISTS | Returns TRUE if any row exists |
| NOT IN danger | NULL in list breaks everything |
| CTE | Named, reusable, readable |
| = subquery | Must return single value |
| Derived table | Must have alias |

### Subquery Type Cheat Sheet

```
Scalar subquery     → Returns single value    → Use with =, <, >
Row subquery        → Returns single row      → Use with IN, =
Table subquery      → Returns multiple rows   → Use in FROM, with IN/EXISTS
Correlated subquery → References outer query  → Executes per row
```

### Best Practices

1. **Prefer EXISTS over NOT IN** - Handles NULLs correctly
2. **Use CTEs for complex queries** - Improves readability
3. **Avoid correlated subqueries in SELECT** - Consider JOINs
4. **Always alias derived tables** - Required by SQL
5. **Test subqueries independently** - Ensure they return expected results
6. **Use EXPLAIN to check performance** - Compare subquery vs JOIN

### Practice Exercises

1. Find all employees earning above the average of their department
2. Find customers who have purchased every product category
3. Find departments with no employees
4. Find the second-highest salary in each department
5. Find employees who have worked on all active projects
6. Calculate running totals of sales per customer
7. Find products that outsold the average by more than 50%
8. Find employees whose total sales exceed their salary

### What's Next?

In **Phase 7: Advanced Functions**, you'll learn:
- String manipulation functions
- Date and time functions
- Numeric functions
- Conditional functions (CASE, IF, COALESCE)
- Type conversion functions
- Advanced aggregate functions
- User-defined functions basics

---

**Ready to master advanced functions? Let's go to Phase 7!**
