# Top 100 SQL Interview Questions

A comprehensive collection of SQL interview questions commonly asked at top tech companies in 2024. Questions are organized by difficulty level and cover essential concepts from basic queries to advanced analytics.

---

## Easy Questions (1-30)

Questions covering basic SELECT queries, WHERE clause filtering, ORDER BY and LIMIT, basic aggregates, and simple joins.

---

### Question 1: Select All Employees

**Difficulty**: Easy
**Category**: Basic SELECT
**Company**: General

**Problem Statement**:
Write a query to retrieve all columns from the employees table.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id, salary, hire_date)
```

**Expected Output Format**:
| employee_id | first_name | last_name | department_id | salary | hire_date |
|-------------|------------|-----------|---------------|--------|-----------|
| 1 | John | Doe | 101 | 50000 | 2020-01-15 |

**Solution**:
```sql
SELECT *
FROM employees;
```

**Explanation**:
The asterisk (*) is a wildcard that selects all columns from the specified table. While useful for quick exploration, in production code it's better to explicitly name columns for clarity and performance.

---

### Question 2: Filter by Department

**Difficulty**: Easy
**Category**: WHERE Clause
**Company**: Amazon

**Problem Statement**:
Find all employees who work in department 101.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id, salary)
```

**Expected Output Format**:
| employee_id | first_name | last_name | salary |
|-------------|------------|-----------|--------|
| 1 | John | Doe | 50000 |

**Solution**:
```sql
SELECT employee_id, first_name, last_name, salary
FROM employees
WHERE department_id = 101;
```

**Explanation**:
The WHERE clause filters rows based on a specified condition. Only rows where department_id equals 101 will be included in the result set.

---

### Question 3: Sort by Salary

**Difficulty**: Easy
**Category**: ORDER BY
**Company**: Google

**Problem Statement**:
List all employees sorted by salary in descending order (highest first).

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, salary)
```

**Expected Output Format**:
| employee_id | first_name | last_name | salary |
|-------------|------------|-----------|--------|
| 5 | Alice | Johnson | 85000 |
| 3 | Bob | Smith | 72000 |

**Solution**:
```sql
SELECT employee_id, first_name, last_name, salary
FROM employees
ORDER BY salary DESC;
```

**Explanation**:
ORDER BY sorts the result set by the specified column. DESC orders from highest to lowest, while ASC (default) orders from lowest to highest.

---

### Question 4: Top N Results

**Difficulty**: Easy
**Category**: LIMIT
**Company**: Meta

**Problem Statement**:
Find the top 5 highest-paid employees.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, salary)
```

**Expected Output Format**:
| employee_id | first_name | last_name | salary |
|-------------|------------|-----------|--------|
| 5 | Alice | Johnson | 85000 |

**Solution**:
```sql
SELECT employee_id, first_name, last_name, salary
FROM employees
ORDER BY salary DESC
LIMIT 5;
```

**Explanation**:
LIMIT restricts the number of rows returned. Combined with ORDER BY DESC, this gives us the top 5 highest-paid employees. In SQL Server, use TOP 5 instead.

---

### Question 5: Count Total Records

**Difficulty**: Easy
**Category**: Aggregates
**Company**: Microsoft

**Problem Statement**:
Count the total number of employees in the company.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id)
```

**Expected Output Format**:
| total_employees |
|-----------------|
| 150 |

**Solution**:
```sql
SELECT COUNT(*) AS total_employees
FROM employees;
```

**Explanation**:
COUNT(*) counts all rows in the table including those with NULL values. Using an alias (AS total_employees) makes the output more readable.

---

### Question 6: Calculate Average Salary

**Difficulty**: Easy
**Category**: Aggregates
**Company**: Netflix

**Problem Statement**:
Calculate the average salary across all employees.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, salary)
```

**Expected Output Format**:
| average_salary |
|----------------|
| 65000.00 |

**Solution**:
```sql
SELECT ROUND(AVG(salary), 2) AS average_salary
FROM employees;
```

**Explanation**:
AVG() calculates the arithmetic mean of a numeric column. ROUND() is used to limit decimal places for cleaner output. NULL values are automatically excluded from the calculation.

---

### Question 7: Find Maximum and Minimum

**Difficulty**: Easy
**Category**: Aggregates
**Company**: Apple

**Problem Statement**:
Find the highest and lowest salary in the company.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, salary)
```

**Expected Output Format**:
| max_salary | min_salary |
|------------|------------|
| 150000 | 35000 |

**Solution**:
```sql
SELECT MAX(salary) AS max_salary, MIN(salary) AS min_salary
FROM employees;
```

**Explanation**:
MAX() and MIN() return the largest and smallest values in a column respectively. These aggregate functions can be used together in the same query.

---

### Question 8: Sum of Salaries

**Difficulty**: Easy
**Category**: Aggregates
**Company**: Uber

**Problem Statement**:
Calculate the total salary expense for all employees.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, salary)
```

**Expected Output Format**:
| total_salary_expense |
|----------------------|
| 9750000 |

**Solution**:
```sql
SELECT SUM(salary) AS total_salary_expense
FROM employees;
```

**Explanation**:
SUM() adds up all values in a numeric column. This is commonly used for calculating totals, budgets, and financial metrics.

---

### Question 9: Distinct Values

**Difficulty**: Easy
**Category**: DISTINCT
**Company**: Airbnb

**Problem Statement**:
List all unique department IDs from the employees table.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id)
```

**Expected Output Format**:
| department_id |
|---------------|
| 101 |
| 102 |
| 103 |

**Solution**:
```sql
SELECT DISTINCT department_id
FROM employees
ORDER BY department_id;
```

**Explanation**:
DISTINCT eliminates duplicate values from the result set. This is useful for understanding the unique categories or values present in a column.

---

### Question 10: Multiple Conditions with AND

**Difficulty**: Easy
**Category**: WHERE Clause
**Company**: Stripe

**Problem Statement**:
Find employees who work in department 101 AND have a salary greater than 50000.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id, salary)
```

**Expected Output Format**:
| employee_id | first_name | last_name | salary |
|-------------|------------|-----------|--------|
| 3 | Bob | Smith | 72000 |

**Solution**:
```sql
SELECT employee_id, first_name, last_name, salary
FROM employees
WHERE department_id = 101 AND salary > 50000;
```

**Explanation**:
The AND operator combines multiple conditions where all must be true. This allows for more specific filtering of data.

---

### Question 11: Multiple Conditions with OR

**Difficulty**: Easy
**Category**: WHERE Clause
**Company**: Google

**Problem Statement**:
Find employees who work in either department 101 OR department 102.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id)
```

**Expected Output Format**:
| employee_id | first_name | last_name | department_id |
|-------------|------------|-----------|---------------|
| 1 | John | Doe | 101 |
| 2 | Jane | Smith | 102 |

**Solution**:
```sql
SELECT employee_id, first_name, last_name, department_id
FROM employees
WHERE department_id = 101 OR department_id = 102;
```

**Explanation**:
The OR operator returns rows where at least one condition is true. This can also be written using the IN operator for cleaner syntax.

---

### Question 12: Using IN Operator

**Difficulty**: Easy
**Category**: WHERE Clause
**Company**: Amazon

**Problem Statement**:
Find employees who work in departments 101, 102, or 103.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id)
```

**Expected Output Format**:
| employee_id | first_name | last_name | department_id |
|-------------|------------|-----------|---------------|
| 1 | John | Doe | 101 |

**Solution**:
```sql
SELECT employee_id, first_name, last_name, department_id
FROM employees
WHERE department_id IN (101, 102, 103);
```

**Explanation**:
The IN operator is a shorthand for multiple OR conditions. It's more readable and efficient when checking against multiple values.

---

### Question 13: Using BETWEEN

**Difficulty**: Easy
**Category**: WHERE Clause
**Company**: Meta

**Problem Statement**:
Find employees with salaries between 50000 and 80000 (inclusive).

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, salary)
```

**Expected Output Format**:
| employee_id | first_name | last_name | salary |
|-------------|------------|-----------|--------|
| 1 | John | Doe | 50000 |
| 3 | Bob | Smith | 72000 |

**Solution**:
```sql
SELECT employee_id, first_name, last_name, salary
FROM employees
WHERE salary BETWEEN 50000 AND 80000;
```

**Explanation**:
BETWEEN is inclusive of both boundary values. It's equivalent to salary >= 50000 AND salary <= 80000 but more readable.

---

### Question 14: Pattern Matching with LIKE

**Difficulty**: Easy
**Category**: WHERE Clause
**Company**: Netflix

**Problem Statement**:
Find all employees whose first name starts with 'J'.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, email)
```

**Expected Output Format**:
| employee_id | first_name | last_name |
|-------------|------------|-----------|
| 1 | John | Doe |
| 2 | Jane | Smith |

**Solution**:
```sql
SELECT employee_id, first_name, last_name
FROM employees
WHERE first_name LIKE 'J%';
```

**Explanation**:
LIKE enables pattern matching. The % wildcard matches any sequence of characters. 'J%' matches any string starting with 'J'.

---

### Question 15: NULL Value Check

**Difficulty**: Easy
**Category**: WHERE Clause
**Company**: Microsoft

**Problem Statement**:
Find all employees who do not have a manager assigned (manager_id is NULL).

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, manager_id)
```

**Expected Output Format**:
| employee_id | first_name | last_name |
|-------------|------------|-----------|
| 1 | John | Doe |

**Solution**:
```sql
SELECT employee_id, first_name, last_name
FROM employees
WHERE manager_id IS NULL;
```

**Explanation**:
NULL represents missing or unknown data. You cannot use = NULL; instead, use IS NULL or IS NOT NULL to check for NULL values.

---

### Question 16: Simple INNER JOIN

**Difficulty**: Easy
**Category**: JOINs
**Company**: Google

**Problem Statement**:
List all employees with their department names.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id)
departments (department_id, department_name)
```

**Expected Output Format**:
| employee_id | first_name | last_name | department_name |
|-------------|------------|-----------|-----------------|
| 1 | John | Doe | Engineering |

**Solution**:
```sql
SELECT e.employee_id, e.first_name, e.last_name, d.department_name
FROM employees e
INNER JOIN departments d ON e.department_id = d.department_id;
```

**Explanation**:
INNER JOIN returns only rows where there's a match in both tables. Table aliases (e and d) make queries more readable and are required when column names are ambiguous.

---

### Question 17: LEFT JOIN Basics

**Difficulty**: Easy
**Category**: JOINs
**Company**: Amazon

**Problem Statement**:
List all employees and their department names, including employees without a department.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id)
departments (department_id, department_name)
```

**Expected Output Format**:
| employee_id | first_name | last_name | department_name |
|-------------|------------|-----------|-----------------|
| 1 | John | Doe | Engineering |
| 5 | Tom | Wilson | NULL |

**Solution**:
```sql
SELECT e.employee_id, e.first_name, e.last_name, d.department_name
FROM employees e
LEFT JOIN departments d ON e.department_id = d.department_id;
```

**Explanation**:
LEFT JOIN returns all rows from the left table (employees) and matched rows from the right table (departments). NULL appears where there's no match.

---

### Question 18: Count by Group

**Difficulty**: Easy
**Category**: GROUP BY
**Company**: Meta

**Problem Statement**:
Count the number of employees in each department.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id)
```

**Expected Output Format**:
| department_id | employee_count |
|---------------|----------------|
| 101 | 15 |
| 102 | 22 |

**Solution**:
```sql
SELECT department_id, COUNT(*) AS employee_count
FROM employees
GROUP BY department_id
ORDER BY employee_count DESC;
```

**Explanation**:
GROUP BY groups rows with the same values. Aggregate functions like COUNT() then operate on each group separately, giving you summary statistics per group.

---

### Question 19: Average by Group

**Difficulty**: Easy
**Category**: GROUP BY
**Company**: Apple

**Problem Statement**:
Calculate the average salary for each department.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id, salary)
```

**Expected Output Format**:
| department_id | avg_salary |
|---------------|------------|
| 101 | 72500.00 |
| 102 | 65000.00 |

**Solution**:
```sql
SELECT department_id, ROUND(AVG(salary), 2) AS avg_salary
FROM employees
GROUP BY department_id
ORDER BY avg_salary DESC;
```

**Explanation**:
When using GROUP BY, non-aggregated columns in SELECT must appear in GROUP BY. This query calculates average salary for each department independently.

---

### Question 20: Alias Usage

**Difficulty**: Easy
**Category**: Basic SELECT
**Company**: Uber

**Problem Statement**:
Select employee names and rename the columns to 'First' and 'Last'.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, salary)
```

**Expected Output Format**:
| First | Last |
|-------|------|
| John | Doe |

**Solution**:
```sql
SELECT first_name AS First, last_name AS Last
FROM employees;
```

**Explanation**:
Column aliases rename columns in the output. AS is optional but recommended for readability. Aliases are useful for calculated fields and JOIN queries.

---

### Question 21: NOT IN Operator

**Difficulty**: Easy
**Category**: WHERE Clause
**Company**: Airbnb

**Problem Statement**:
Find all employees who are NOT in departments 101 or 102.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id)
```

**Expected Output Format**:
| employee_id | first_name | last_name | department_id |
|-------------|------------|-----------|---------------|
| 5 | Tom | Wilson | 103 |

**Solution**:
```sql
SELECT employee_id, first_name, last_name, department_id
FROM employees
WHERE department_id NOT IN (101, 102);
```

**Explanation**:
NOT IN excludes rows matching any value in the list. Be cautious with NULL values in NOT IN as they can cause unexpected results.

---

### Question 22: Concatenate Strings

**Difficulty**: Easy
**Category**: String Functions
**Company**: Stripe

**Problem Statement**:
Create a full name by combining first and last names.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name)
```

**Expected Output Format**:
| employee_id | full_name |
|-------------|-----------|
| 1 | John Doe |

**Solution**:
```sql
SELECT employee_id, CONCAT(first_name, ' ', last_name) AS full_name
FROM employees;
```

**Explanation**:
CONCAT() combines multiple strings into one. In some databases, you can use || (Oracle, PostgreSQL) or + (SQL Server) for concatenation.

---

### Question 23: OFFSET for Pagination

**Difficulty**: Easy
**Category**: LIMIT/OFFSET
**Company**: Netflix

**Problem Statement**:
Get employees 11-20 when sorted by employee_id (page 2 with 10 per page).

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, salary)
```

**Expected Output Format**:
| employee_id | first_name | last_name |
|-------------|------------|-----------|
| 11 | Mark | Johnson |

**Solution**:
```sql
SELECT employee_id, first_name, last_name
FROM employees
ORDER BY employee_id
LIMIT 10 OFFSET 10;
```

**Explanation**:
OFFSET skips the specified number of rows before returning results. Combined with LIMIT, this enables pagination. Page N with size S uses OFFSET (N-1)*S.

---

### Question 24: Date Filtering

**Difficulty**: Easy
**Category**: Date Functions
**Company**: Google

**Problem Statement**:
Find all employees hired in 2023.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, hire_date)
```

**Expected Output Format**:
| employee_id | first_name | last_name | hire_date |
|-------------|------------|-----------|-----------|
| 45 | Sarah | Brown | 2023-03-15 |

**Solution**:
```sql
SELECT employee_id, first_name, last_name, hire_date
FROM employees
WHERE YEAR(hire_date) = 2023;
```

**Explanation**:
YEAR() extracts the year from a date. Note that using functions on columns can prevent index usage. An alternative is: hire_date >= '2023-01-01' AND hire_date < '2024-01-01'.

---

### Question 25: NOT NULL Check

**Difficulty**: Easy
**Category**: WHERE Clause
**Company**: Microsoft

**Problem Statement**:
Find all employees who have a phone number on file.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, phone_number)
```

**Expected Output Format**:
| employee_id | first_name | last_name | phone_number |
|-------------|------------|-----------|--------------|
| 1 | John | Doe | 555-1234 |

**Solution**:
```sql
SELECT employee_id, first_name, last_name, phone_number
FROM employees
WHERE phone_number IS NOT NULL;
```

**Explanation**:
IS NOT NULL filters out rows where the specified column has no value. This is essential for data quality checks and ensuring complete records.

---

### Question 26: Multiple Column Sorting

**Difficulty**: Easy
**Category**: ORDER BY
**Company**: Amazon

**Problem Statement**:
Sort employees by department_id (ascending), then by salary (descending).

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id, salary)
```

**Expected Output Format**:
| employee_id | first_name | department_id | salary |
|-------------|------------|---------------|--------|
| 3 | Bob | 101 | 85000 |
| 1 | John | 101 | 72000 |

**Solution**:
```sql
SELECT employee_id, first_name, department_id, salary
FROM employees
ORDER BY department_id ASC, salary DESC;
```

**Explanation**:
Multiple columns in ORDER BY create a hierarchy. Rows are first sorted by department_id; within each department, they're sorted by salary descending.

---

### Question 27: Count Distinct

**Difficulty**: Easy
**Category**: Aggregates
**Company**: Meta

**Problem Statement**:
Count how many different departments have employees.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id)
```

**Expected Output Format**:
| distinct_departments |
|----------------------|
| 12 |

**Solution**:
```sql
SELECT COUNT(DISTINCT department_id) AS distinct_departments
FROM employees;
```

**Explanation**:
COUNT(DISTINCT column) counts only unique non-NULL values. This is useful for understanding the diversity or spread of categorical data.

---

### Question 28: LIKE with Underscore

**Difficulty**: Easy
**Category**: WHERE Clause
**Company**: Apple

**Problem Statement**:
Find employees whose first name is exactly 4 characters long.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name)
```

**Expected Output Format**:
| employee_id | first_name | last_name |
|-------------|------------|-----------|
| 1 | John | Doe |
| 2 | Jane | Smith |

**Solution**:
```sql
SELECT employee_id, first_name, last_name
FROM employees
WHERE first_name LIKE '____';
```

**Explanation**:
The underscore (_) wildcard matches exactly one character. Four underscores match strings with exactly four characters. This is useful for fixed-length pattern matching.

---

### Question 29: Simple Calculation

**Difficulty**: Easy
**Category**: Basic SELECT
**Company**: Uber

**Problem Statement**:
Calculate the annual salary (monthly_salary * 12) for all employees.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, monthly_salary)
```

**Expected Output Format**:
| employee_id | first_name | annual_salary |
|-------------|------------|---------------|
| 1 | John | 72000 |

**Solution**:
```sql
SELECT employee_id, first_name, monthly_salary * 12 AS annual_salary
FROM employees;
```

**Explanation**:
SQL allows arithmetic operations (+, -, *, /) in SELECT statements. Calculated fields should be given meaningful aliases for clarity.

---

### Question 30: NOT LIKE

**Difficulty**: Easy
**Category**: WHERE Clause
**Company**: Airbnb

**Problem Statement**:
Find employees whose email does not end with '@company.com'.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, email)
```

**Expected Output Format**:
| employee_id | first_name | email |
|-------------|------------|-------|
| 5 | Tom | tom@external.com |

**Solution**:
```sql
SELECT employee_id, first_name, email
FROM employees
WHERE email NOT LIKE '%@company.com';
```

**Explanation**:
NOT LIKE excludes rows matching the pattern. This query finds employees with non-standard email domains, useful for data validation and cleanup.

---

## Medium Questions (31-80)

Questions covering complex JOINs, GROUP BY with HAVING, subqueries, CASE statements, date functions, and string manipulation.

---

### Question 31: HAVING Clause

**Difficulty**: Medium
**Category**: GROUP BY/HAVING
**Company**: Google

**Problem Statement**:
Find departments with more than 10 employees.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id)
```

**Expected Output Format**:
| department_id | employee_count |
|---------------|----------------|
| 101 | 15 |
| 102 | 22 |

**Solution**:
```sql
SELECT department_id, COUNT(*) AS employee_count
FROM employees
GROUP BY department_id
HAVING COUNT(*) > 10
ORDER BY employee_count DESC;
```

**Explanation**:
HAVING filters groups after aggregation, while WHERE filters rows before. Use HAVING when filtering on aggregate results like COUNT, SUM, or AVG.

---

### Question 32: Subquery in WHERE

**Difficulty**: Medium
**Category**: Subqueries
**Company**: Amazon

**Problem Statement**:
Find employees who earn more than the company average salary.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, salary)
```

**Expected Output Format**:
| employee_id | first_name | last_name | salary |
|-------------|------------|-----------|--------|
| 3 | Alice | Johnson | 95000 |

**Solution**:
```sql
SELECT employee_id, first_name, last_name, salary
FROM employees
WHERE salary > (SELECT AVG(salary) FROM employees);
```

**Explanation**:
A subquery (inner query) executes first and its result is used by the outer query. This scalar subquery returns a single value used for comparison.

---

### Question 33: CASE Statement

**Difficulty**: Medium
**Category**: CASE
**Company**: Meta

**Problem Statement**:
Categorize employees by salary: 'Low' (<50000), 'Medium' (50000-80000), 'High' (>80000).

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, salary)
```

**Expected Output Format**:
| employee_id | first_name | salary | salary_category |
|-------------|------------|--------|-----------------|
| 1 | John | 45000 | Low |
| 2 | Jane | 72000 | Medium |

**Solution**:
```sql
SELECT employee_id, first_name, salary,
       CASE
           WHEN salary < 50000 THEN 'Low'
           WHEN salary <= 80000 THEN 'Medium'
           ELSE 'High'
       END AS salary_category
FROM employees;
```

**Explanation**:
CASE provides conditional logic similar to if-else. Conditions are evaluated in order; the first match determines the result. ELSE handles unmatched cases.

---

### Question 34: Self JOIN

**Difficulty**: Medium
**Category**: JOINs
**Company**: Microsoft

**Problem Statement**:
List employees with their manager's name.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, manager_id)
```

**Expected Output Format**:
| employee_name | manager_name |
|---------------|--------------|
| John Doe | Alice Johnson |

**Solution**:
```sql
SELECT CONCAT(e.first_name, ' ', e.last_name) AS employee_name,
       CONCAT(m.first_name, ' ', m.last_name) AS manager_name
FROM employees e
LEFT JOIN employees m ON e.manager_id = m.employee_id;
```

**Explanation**:
A self JOIN joins a table to itself using different aliases. This is useful for hierarchical data like org charts. LEFT JOIN includes employees without managers.

---

### Question 35: Multiple JOINs

**Difficulty**: Medium
**Category**: JOINs
**Company**: Netflix

**Problem Statement**:
List employee names with their department and location names.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id)
departments (department_id, department_name, location_id)
locations (location_id, city, country)
```

**Expected Output Format**:
| first_name | department_name | city | country |
|------------|-----------------|------|---------|
| John | Engineering | San Francisco | USA |

**Solution**:
```sql
SELECT e.first_name, d.department_name, l.city, l.country
FROM employees e
INNER JOIN departments d ON e.department_id = d.department_id
INNER JOIN locations l ON d.location_id = l.location_id;
```

**Explanation**:
Multiple JOINs chain tables together through their relationships. Each JOIN connects two tables through a common column, building up the complete result set.

---

### Question 36: EXISTS Subquery

**Difficulty**: Medium
**Category**: Subqueries
**Company**: Apple

**Problem Statement**:
Find departments that have at least one employee.

**Sample Tables**:
```sql
departments (department_id, department_name)
employees (employee_id, first_name, department_id)
```

**Expected Output Format**:
| department_id | department_name |
|---------------|-----------------|
| 101 | Engineering |

**Solution**:
```sql
SELECT d.department_id, d.department_name
FROM departments d
WHERE EXISTS (
    SELECT 1 FROM employees e
    WHERE e.department_id = d.department_id
);
```

**Explanation**:
EXISTS returns TRUE if the subquery returns any rows. It's often more efficient than IN for large datasets because it stops at the first match.

---

### Question 37: NOT EXISTS

**Difficulty**: Medium
**Category**: Subqueries
**Company**: Uber

**Problem Statement**:
Find departments that have no employees.

**Sample Tables**:
```sql
departments (department_id, department_name)
employees (employee_id, first_name, department_id)
```

**Expected Output Format**:
| department_id | department_name |
|---------------|-----------------|
| 105 | Research |

**Solution**:
```sql
SELECT d.department_id, d.department_name
FROM departments d
WHERE NOT EXISTS (
    SELECT 1 FROM employees e
    WHERE e.department_id = d.department_id
);
```

**Explanation**:
NOT EXISTS returns TRUE when the subquery returns no rows. This is ideal for finding orphaned or unused records in related tables.

---

### Question 38: Correlated Subquery

**Difficulty**: Medium
**Category**: Subqueries
**Company**: Airbnb

**Problem Statement**:
Find employees who earn more than the average salary in their department.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, department_id, salary)
```

**Expected Output Format**:
| employee_id | first_name | department_id | salary |
|-------------|------------|---------------|--------|
| 3 | Bob | 101 | 85000 |

**Solution**:
```sql
SELECT e1.employee_id, e1.first_name, e1.department_id, e1.salary
FROM employees e1
WHERE e1.salary > (
    SELECT AVG(e2.salary)
    FROM employees e2
    WHERE e2.department_id = e1.department_id
);
```

**Explanation**:
A correlated subquery references the outer query (e1.department_id). It executes once for each row of the outer query, allowing row-by-row comparisons.

---

### Question 39: UNION

**Difficulty**: Medium
**Category**: Set Operations
**Company**: Stripe

**Problem Statement**:
Get a combined list of all customer and supplier cities (no duplicates).

**Sample Tables**:
```sql
customers (customer_id, customer_name, city)
suppliers (supplier_id, supplier_name, city)
```

**Expected Output Format**:
| city |
|------|
| New York |
| Los Angeles |

**Solution**:
```sql
SELECT city FROM customers
UNION
SELECT city FROM suppliers
ORDER BY city;
```

**Explanation**:
UNION combines results from two queries and removes duplicates. Both queries must have the same number of columns with compatible data types.

---

### Question 40: UNION ALL

**Difficulty**: Medium
**Category**: Set Operations
**Company**: Google

**Problem Statement**:
Get a list of all customer and supplier cities (including duplicates).

**Sample Tables**:
```sql
customers (customer_id, customer_name, city)
suppliers (supplier_id, supplier_name, city)
```

**Expected Output Format**:
| city |
|------|
| New York |
| New York |
| Los Angeles |

**Solution**:
```sql
SELECT city FROM customers
UNION ALL
SELECT city FROM suppliers
ORDER BY city;
```

**Explanation**:
UNION ALL keeps all rows including duplicates. It's faster than UNION because it doesn't need to remove duplicates. Use when duplicates are acceptable or needed.

---

### Question 41: Date Difference

**Difficulty**: Medium
**Category**: Date Functions
**Company**: Amazon

**Problem Statement**:
Calculate how many days each employee has been with the company.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, hire_date)
```

**Expected Output Format**:
| employee_id | first_name | days_employed |
|-------------|------------|---------------|
| 1 | John | 1245 |

**Solution**:
```sql
SELECT employee_id, first_name,
       DATEDIFF(CURDATE(), hire_date) AS days_employed
FROM employees
ORDER BY days_employed DESC;
```

**Explanation**:
DATEDIFF calculates the difference between two dates. Syntax varies by database: MySQL uses DATEDIFF(end, start), SQL Server uses DATEDIFF(day, start, end).

---

### Question 42: Extract Month/Year

**Difficulty**: Medium
**Category**: Date Functions
**Company**: Meta

**Problem Statement**:
Count how many employees were hired each month in 2023.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, hire_date)
```

**Expected Output Format**:
| hire_month | hire_count |
|------------|------------|
| 1 | 5 |
| 2 | 8 |

**Solution**:
```sql
SELECT MONTH(hire_date) AS hire_month, COUNT(*) AS hire_count
FROM employees
WHERE YEAR(hire_date) = 2023
GROUP BY MONTH(hire_date)
ORDER BY hire_month;
```

**Explanation**:
MONTH() and YEAR() extract parts of a date. This pattern is common for trend analysis and reporting by time periods.

---

### Question 43: String Length

**Difficulty**: Medium
**Category**: String Functions
**Company**: Netflix

**Problem Statement**:
Find employees whose last name is longer than 8 characters.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name)
```

**Expected Output Format**:
| employee_id | first_name | last_name |
|-------------|------------|-----------|
| 5 | Tom | Washington |

**Solution**:
```sql
SELECT employee_id, first_name, last_name
FROM employees
WHERE LENGTH(last_name) > 8;
```

**Explanation**:
LENGTH() (or LEN() in SQL Server) returns the number of characters in a string. This is useful for data validation and formatting checks.

---

### Question 44: UPPER and LOWER

**Difficulty**: Medium
**Category**: String Functions
**Company**: Microsoft

**Problem Statement**:
Display employee names in uppercase and their emails in lowercase.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, email)
```

**Expected Output Format**:
| upper_name | lower_email |
|------------|-------------|
| JOHN DOE | john.doe@company.com |

**Solution**:
```sql
SELECT UPPER(CONCAT(first_name, ' ', last_name)) AS upper_name,
       LOWER(email) AS lower_email
FROM employees;
```

**Explanation**:
UPPER() converts to uppercase, LOWER() to lowercase. These are essential for case-insensitive comparisons and standardizing data format.

---

### Question 45: SUBSTRING

**Difficulty**: Medium
**Category**: String Functions
**Company**: Apple

**Problem Statement**:
Extract the first 3 characters of each employee's first name.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name)
```

**Expected Output Format**:
| employee_id | first_name | short_name |
|-------------|------------|------------|
| 1 | John | Joh |

**Solution**:
```sql
SELECT employee_id, first_name,
       SUBSTRING(first_name, 1, 3) AS short_name
FROM employees;
```

**Explanation**:
SUBSTRING(string, start, length) extracts part of a string. Start position is 1-based in most databases. Useful for parsing and formatting data.

---

### Question 46: COALESCE for NULL Handling

**Difficulty**: Medium
**Category**: NULL Handling
**Company**: Uber

**Problem Statement**:
Display employee phone numbers, showing 'Not provided' for NULL values.

**Sample Tables**:
```sql
employees (employee_id, first_name, phone_number)
```

**Expected Output Format**:
| employee_id | first_name | phone |
|-------------|------------|-------|
| 1 | John | 555-1234 |
| 2 | Jane | Not provided |

**Solution**:
```sql
SELECT employee_id, first_name,
       COALESCE(phone_number, 'Not provided') AS phone
FROM employees;
```

**Explanation**:
COALESCE returns the first non-NULL value from its arguments. It's essential for providing default values and handling missing data gracefully.

---

### Question 47: Multiple Table Aggregation

**Difficulty**: Medium
**Category**: JOINs/Aggregates
**Company**: Airbnb

**Problem Statement**:
Calculate total sales amount per customer.

**Sample Tables**:
```sql
customers (customer_id, customer_name)
orders (order_id, customer_id, order_date, amount)
```

**Expected Output Format**:
| customer_id | customer_name | total_sales |
|-------------|---------------|-------------|
| 1 | Acme Corp | 15000.00 |

**Solution**:
```sql
SELECT c.customer_id, c.customer_name,
       COALESCE(SUM(o.amount), 0) AS total_sales
FROM customers c
LEFT JOIN orders o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.customer_name
ORDER BY total_sales DESC;
```

**Explanation**:
Joining before aggregating allows calculation across related tables. LEFT JOIN includes customers without orders, COALESCE handles their NULL sums.

---

### Question 48: Subquery in FROM

**Difficulty**: Medium
**Category**: Subqueries
**Company**: Stripe

**Problem Statement**:
Find the department with the highest average salary.

**Sample Tables**:
```sql
employees (employee_id, first_name, department_id, salary)
departments (department_id, department_name)
```

**Expected Output Format**:
| department_name | avg_salary |
|-----------------|------------|
| Engineering | 95000.00 |

**Solution**:
```sql
SELECT d.department_name, dept_avg.avg_salary
FROM (
    SELECT department_id, ROUND(AVG(salary), 2) AS avg_salary
    FROM employees
    GROUP BY department_id
) dept_avg
JOIN departments d ON dept_avg.department_id = d.department_id
ORDER BY dept_avg.avg_salary DESC
LIMIT 1;
```

**Explanation**:
A subquery in FROM (derived table) creates a temporary result set that can be joined with other tables. This allows multi-step aggregations and complex calculations.

---

### Question 49: Conditional Aggregation

**Difficulty**: Medium
**Category**: CASE/Aggregates
**Company**: Google

**Problem Statement**:
Count male and female employees per department in a single query.

**Sample Tables**:
```sql
employees (employee_id, first_name, department_id, gender)
```

**Expected Output Format**:
| department_id | male_count | female_count |
|---------------|------------|--------------|
| 101 | 10 | 8 |

**Solution**:
```sql
SELECT department_id,
       SUM(CASE WHEN gender = 'M' THEN 1 ELSE 0 END) AS male_count,
       SUM(CASE WHEN gender = 'F' THEN 1 ELSE 0 END) AS female_count
FROM employees
GROUP BY department_id;
```

**Explanation**:
Conditional aggregation uses CASE inside aggregate functions to count or sum based on conditions. This pivots row data into columns efficiently.

---

### Question 50: RIGHT JOIN

**Difficulty**: Medium
**Category**: JOINs
**Company**: Amazon

**Problem Statement**:
List all departments and their employees, including departments without employees.

**Sample Tables**:
```sql
employees (employee_id, first_name, department_id)
departments (department_id, department_name)
```

**Expected Output Format**:
| department_name | first_name |
|-----------------|------------|
| Engineering | John |
| Research | NULL |

**Solution**:
```sql
SELECT d.department_name, e.first_name
FROM employees e
RIGHT JOIN departments d ON e.department_id = d.department_id
ORDER BY d.department_name;
```

**Explanation**:
RIGHT JOIN keeps all rows from the right table (departments). It's equivalent to swapping tables in a LEFT JOIN. Most developers prefer LEFT JOIN for readability.

---

### Question 51: FULL OUTER JOIN

**Difficulty**: Medium
**Category**: JOINs
**Company**: Meta

**Problem Statement**:
List all employees and all departments, showing NULL where there's no match.

**Sample Tables**:
```sql
employees (employee_id, first_name, department_id)
departments (department_id, department_name)
```

**Expected Output Format**:
| first_name | department_name |
|------------|-----------------|
| John | Engineering |
| NULL | Research |
| Tom | NULL |

**Solution**:
```sql
SELECT e.first_name, d.department_name
FROM employees e
FULL OUTER JOIN departments d ON e.department_id = d.department_id;
```

**Explanation**:
FULL OUTER JOIN returns all rows from both tables, with NULL where there's no match. Note: MySQL doesn't support FULL OUTER JOIN; use UNION of LEFT and RIGHT JOINs.

---

### Question 52: Finding Duplicates

**Difficulty**: Medium
**Category**: GROUP BY/HAVING
**Company**: Netflix

**Problem Statement**:
Find email addresses that appear more than once in the employees table.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, email)
```

**Expected Output Format**:
| email | count |
|-------|-------|
| john@company.com | 2 |

**Solution**:
```sql
SELECT email, COUNT(*) AS count
FROM employees
GROUP BY email
HAVING COUNT(*) > 1;
```

**Explanation**:
GROUP BY with HAVING COUNT(*) > 1 identifies duplicates. This is a common data quality check. The pattern works for any column that should be unique.

---

### Question 53: Second Highest Value

**Difficulty**: Medium
**Category**: Subqueries
**Company**: Microsoft

**Problem Statement**:
Find the employee with the second highest salary.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name, salary)
```

**Expected Output Format**:
| employee_id | first_name | salary |
|-------------|------------|--------|
| 3 | Bob | 92000 |

**Solution**:
```sql
SELECT employee_id, first_name, salary
FROM employees
WHERE salary = (
    SELECT DISTINCT salary
    FROM employees
    ORDER BY salary DESC
    LIMIT 1 OFFSET 1
);
```

**Explanation**:
This subquery finds the second highest distinct salary value using OFFSET. Multiple employees with that salary will all be returned.

---

### Question 54: TRIM Function

**Difficulty**: Medium
**Category**: String Functions
**Company**: Apple

**Problem Statement**:
Remove leading and trailing spaces from employee names.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name)
```

**Expected Output Format**:
| employee_id | clean_first | clean_last |
|-------------|-------------|------------|
| 1 | John | Doe |

**Solution**:
```sql
SELECT employee_id,
       TRIM(first_name) AS clean_first,
       TRIM(last_name) AS clean_last
FROM employees;
```

**Explanation**:
TRIM removes leading and trailing whitespace. LTRIM and RTRIM remove only left or right spaces respectively. Essential for data cleaning.

---

### Question 55: REPLACE Function

**Difficulty**: Medium
**Category**: String Functions
**Company**: Uber

**Problem Statement**:
Replace all occurrences of 'Inc.' with 'Incorporated' in company names.

**Sample Tables**:
```sql
companies (company_id, company_name)
```

**Expected Output Format**:
| company_id | updated_name |
|------------|--------------|
| 1 | Acme Incorporated |

**Solution**:
```sql
SELECT company_id,
       REPLACE(company_name, 'Inc.', 'Incorporated') AS updated_name
FROM companies;
```

**Explanation**:
REPLACE(string, old, new) substitutes all occurrences of a substring. It's case-sensitive in most databases. Useful for data standardization.

---

### Question 56: Date Formatting

**Difficulty**: Medium
**Category**: Date Functions
**Company**: Airbnb

**Problem Statement**:
Display hire dates in 'Month DD, YYYY' format.

**Sample Tables**:
```sql
employees (employee_id, first_name, hire_date)
```

**Expected Output Format**:
| employee_id | first_name | formatted_date |
|-------------|------------|----------------|
| 1 | John | January 15, 2020 |

**Solution**:
```sql
SELECT employee_id, first_name,
       DATE_FORMAT(hire_date, '%M %d, %Y') AS formatted_date
FROM employees;
```

**Explanation**:
DATE_FORMAT (MySQL) formats dates as strings. SQL Server uses FORMAT(), PostgreSQL uses TO_CHAR(). Format codes vary by database system.

---

### Question 57: Adding Dates

**Difficulty**: Medium
**Category**: Date Functions
**Company**: Stripe

**Problem Statement**:
Calculate the date 90 days after each employee's hire date (probation end).

**Sample Tables**:
```sql
employees (employee_id, first_name, hire_date)
```

**Expected Output Format**:
| employee_id | first_name | probation_end |
|-------------|------------|---------------|
| 1 | John | 2020-04-14 |

**Solution**:
```sql
SELECT employee_id, first_name,
       DATE_ADD(hire_date, INTERVAL 90 DAY) AS probation_end
FROM employees;
```

**Explanation**:
DATE_ADD adds a specified interval to a date. Intervals can be DAY, MONTH, YEAR, etc. SQL Server uses DATEADD(day, 90, hire_date).

---

### Question 58: GROUP_CONCAT/STRING_AGG

**Difficulty**: Medium
**Category**: Aggregates
**Company**: Google

**Problem Statement**:
List all employee names per department as a comma-separated string.

**Sample Tables**:
```sql
employees (employee_id, first_name, department_id)
```

**Expected Output Format**:
| department_id | employee_names |
|---------------|----------------|
| 101 | John, Jane, Bob |

**Solution**:
```sql
SELECT department_id,
       GROUP_CONCAT(first_name ORDER BY first_name SEPARATOR ', ') AS employee_names
FROM employees
GROUP BY department_id;
```

**Explanation**:
GROUP_CONCAT (MySQL) aggregates strings into a list. PostgreSQL uses STRING_AGG(), SQL Server uses STRING_AGG() or FOR XML PATH. Useful for reporting.

---

### Question 59: Nth Highest with Subquery

**Difficulty**: Medium
**Category**: Subqueries
**Company**: Amazon

**Problem Statement**:
Find the 3rd highest salary in the company.

**Sample Tables**:
```sql
employees (employee_id, first_name, salary)
```

**Expected Output Format**:
| third_highest_salary |
|----------------------|
| 85000 |

**Solution**:
```sql
SELECT DISTINCT salary AS third_highest_salary
FROM employees e1
WHERE 3 = (
    SELECT COUNT(DISTINCT e2.salary)
    FROM employees e2
    WHERE e2.salary >= e1.salary
);
```

**Explanation**:
This correlated subquery counts how many distinct salaries are greater than or equal to each salary. When count equals N, that's the Nth highest salary.

---

### Question 60: IFNULL/NULLIF

**Difficulty**: Medium
**Category**: NULL Handling
**Company**: Meta

**Problem Statement**:
Calculate bonus percentage, avoiding division by zero when salary is 0.

**Sample Tables**:
```sql
employees (employee_id, first_name, salary, bonus)
```

**Expected Output Format**:
| employee_id | first_name | bonus_percentage |
|-------------|------------|------------------|
| 1 | John | 10.00 |

**Solution**:
```sql
SELECT employee_id, first_name,
       ROUND(bonus / NULLIF(salary, 0) * 100, 2) AS bonus_percentage
FROM employees;
```

**Explanation**:
NULLIF(a, b) returns NULL if a equals b. This prevents division by zero errors by converting 0 to NULL, which makes the entire expression NULL.

---

### Question 61: Cross Join

**Difficulty**: Medium
**Category**: JOINs
**Company**: Netflix

**Problem Statement**:
Generate all possible employee-project combinations.

**Sample Tables**:
```sql
employees (employee_id, first_name)
projects (project_id, project_name)
```

**Expected Output Format**:
| first_name | project_name |
|------------|--------------|
| John | Alpha |
| John | Beta |

**Solution**:
```sql
SELECT e.first_name, p.project_name
FROM employees e
CROSS JOIN projects p
ORDER BY e.first_name, p.project_name;
```

**Explanation**:
CROSS JOIN produces a Cartesian product - every row from the first table paired with every row from the second. Use carefully as results grow multiplicatively.

---

### Question 62: CTE Basics

**Difficulty**: Medium
**Category**: CTEs
**Company**: Microsoft

**Problem Statement**:
Use a CTE to find departments with above-average employee counts.

**Sample Tables**:
```sql
employees (employee_id, first_name, department_id)
```

**Expected Output Format**:
| department_id | employee_count |
|---------------|----------------|
| 101 | 25 |

**Solution**:
```sql
WITH dept_counts AS (
    SELECT department_id, COUNT(*) AS employee_count
    FROM employees
    GROUP BY department_id
)
SELECT department_id, employee_count
FROM dept_counts
WHERE employee_count > (SELECT AVG(employee_count) FROM dept_counts);
```

**Explanation**:
A CTE (Common Table Expression) defines a temporary named result set. It improves readability and allows referencing the same subquery multiple times.

---

### Question 63: Multiple CTEs

**Difficulty**: Medium
**Category**: CTEs
**Company**: Apple

**Problem Statement**:
Find employees who earn above their department average AND above company average.

**Sample Tables**:
```sql
employees (employee_id, first_name, department_id, salary)
```

**Expected Output Format**:
| employee_id | first_name | salary |
|-------------|------------|--------|
| 3 | Alice | 95000 |

**Solution**:
```sql
WITH company_avg AS (
    SELECT AVG(salary) AS avg_sal FROM employees
),
dept_avg AS (
    SELECT department_id, AVG(salary) AS avg_sal
    FROM employees GROUP BY department_id
)
SELECT e.employee_id, e.first_name, e.salary
FROM employees e
JOIN dept_avg d ON e.department_id = d.department_id
CROSS JOIN company_avg c
WHERE e.salary > d.avg_sal AND e.salary > c.avg_sal;
```

**Explanation**:
Multiple CTEs are separated by commas. Each can reference previously defined CTEs. This structures complex queries into logical, readable steps.

---

### Question 64: IN with Subquery

**Difficulty**: Medium
**Category**: Subqueries
**Company**: Uber

**Problem Statement**:
Find employees who work in departments located in 'New York'.

**Sample Tables**:
```sql
employees (employee_id, first_name, department_id)
departments (department_id, department_name, location_id)
locations (location_id, city)
```

**Expected Output Format**:
| employee_id | first_name |
|-------------|------------|
| 1 | John |

**Solution**:
```sql
SELECT employee_id, first_name
FROM employees
WHERE department_id IN (
    SELECT d.department_id
    FROM departments d
    JOIN locations l ON d.location_id = l.location_id
    WHERE l.city = 'New York'
);
```

**Explanation**:
IN with a subquery checks if a value exists in a list returned by the inner query. This is an alternative to JOIN that can be more readable for simple lookups.

---

### Question 65: EXCEPT/MINUS

**Difficulty**: Medium
**Category**: Set Operations
**Company**: Airbnb

**Problem Statement**:
Find customers who have never placed an order.

**Sample Tables**:
```sql
customers (customer_id, customer_name)
orders (order_id, customer_id, order_date)
```

**Expected Output Format**:
| customer_id | customer_name |
|-------------|---------------|
| 5 | Inactive Corp |

**Solution**:
```sql
SELECT customer_id, customer_name
FROM customers
WHERE customer_id NOT IN (
    SELECT DISTINCT customer_id FROM orders WHERE customer_id IS NOT NULL
);
```

**Explanation**:
This finds customers with no matching orders. Note the NULL check in the subquery - NOT IN fails if the list contains NULL. EXCEPT works similarly in SQL Server.

---

### Question 66: Percentage Calculation

**Difficulty**: Medium
**Category**: Aggregates
**Company**: Stripe

**Problem Statement**:
Calculate each department's employee count as a percentage of total employees.

**Sample Tables**:
```sql
employees (employee_id, first_name, department_id)
```

**Expected Output Format**:
| department_id | employee_count | percentage |
|---------------|----------------|------------|
| 101 | 15 | 10.00 |

**Solution**:
```sql
SELECT department_id,
       COUNT(*) AS employee_count,
       ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM employees), 2) AS percentage
FROM employees
GROUP BY department_id
ORDER BY percentage DESC;
```

**Explanation**:
Divide the group count by total count and multiply by 100. Using 100.0 ensures decimal division. The subquery calculates the total for each row.

---

### Question 67: Year-over-Year Comparison

**Difficulty**: Medium
**Category**: Date Functions
**Company**: Google

**Problem Statement**:
Compare total sales for each month with the same month last year.

**Sample Tables**:
```sql
sales (sale_id, sale_date, amount)
```

**Expected Output Format**:
| month | year_2023 | year_2022 |
|-------|-----------|-----------|
| 1 | 50000 | 42000 |

**Solution**:
```sql
SELECT MONTH(sale_date) AS month,
       SUM(CASE WHEN YEAR(sale_date) = 2023 THEN amount ELSE 0 END) AS year_2023,
       SUM(CASE WHEN YEAR(sale_date) = 2022 THEN amount ELSE 0 END) AS year_2022
FROM sales
WHERE YEAR(sale_date) IN (2022, 2023)
GROUP BY MONTH(sale_date)
ORDER BY month;
```

**Explanation**:
Conditional aggregation with CASE pivots yearly data into columns. This creates a side-by-side comparison for analysis without multiple queries.

---

### Question 68: Running Total with Subquery

**Difficulty**: Medium
**Category**: Subqueries
**Company**: Amazon

**Problem Statement**:
Calculate running total of sales ordered by date.

**Sample Tables**:
```sql
sales (sale_id, sale_date, amount)
```

**Expected Output Format**:
| sale_date | amount | running_total |
|-----------|--------|---------------|
| 2023-01-01 | 100 | 100 |
| 2023-01-02 | 150 | 250 |

**Solution**:
```sql
SELECT s1.sale_date, s1.amount,
       (SELECT SUM(s2.amount)
        FROM sales s2
        WHERE s2.sale_date <= s1.sale_date) AS running_total
FROM sales s1
ORDER BY s1.sale_date;
```

**Explanation**:
The correlated subquery sums all amounts up to the current row's date. This is the pre-window-function approach to calculating running totals.

---

### Question 69: INTERSECT

**Difficulty**: Medium
**Category**: Set Operations
**Company**: Meta

**Problem Statement**:
Find products that appear in both the 'Electronics' and 'Gifts' categories.

**Sample Tables**:
```sql
electronics (product_id, product_name)
gifts (product_id, product_name)
```

**Expected Output Format**:
| product_id | product_name |
|------------|--------------|
| 101 | Tablet |

**Solution**:
```sql
SELECT product_id, product_name FROM electronics
INTERSECT
SELECT product_id, product_name FROM gifts;
```

**Explanation**:
INTERSECT returns only rows that appear in both result sets. It's like finding the common elements between two queries. Not supported in all MySQL versions.

---

### Question 70: Complex WHERE with Multiple Operators

**Difficulty**: Medium
**Category**: WHERE Clause
**Company**: Netflix

**Problem Statement**:
Find senior employees (>5 years) earning below 60000 OR managers earning above 100000.

**Sample Tables**:
```sql
employees (employee_id, first_name, hire_date, salary, is_manager)
```

**Expected Output Format**:
| employee_id | first_name | salary |
|-------------|------------|--------|
| 3 | Bob | 55000 |

**Solution**:
```sql
SELECT employee_id, first_name, salary
FROM employees
WHERE (DATEDIFF(CURDATE(), hire_date) > 1825 AND salary < 60000)
   OR (is_manager = 1 AND salary > 100000);
```

**Explanation**:
Parentheses group conditions to control evaluation order. AND has higher precedence than OR, so parentheses clarify intent and prevent logical errors.

---

### Question 71: CASE in ORDER BY

**Difficulty**: Medium
**Category**: CASE
**Company**: Microsoft

**Problem Statement**:
Sort employees with managers first, then by salary descending.

**Sample Tables**:
```sql
employees (employee_id, first_name, salary, is_manager)
```

**Expected Output Format**:
| employee_id | first_name | salary | is_manager |
|-------------|------------|--------|------------|
| 1 | Alice | 120000 | 1 |

**Solution**:
```sql
SELECT employee_id, first_name, salary, is_manager
FROM employees
ORDER BY CASE WHEN is_manager = 1 THEN 0 ELSE 1 END,
         salary DESC;
```

**Explanation**:
CASE in ORDER BY enables custom sort logic. Managers get sort value 0 (first), others get 1 (second). Within each group, salary sorts descending.

---

### Question 72: UPDATE with JOIN

**Difficulty**: Medium
**Category**: DML
**Company**: Apple

**Problem Statement**:
Give a 10% raise to all employees in the 'Sales' department.

**Sample Tables**:
```sql
employees (employee_id, first_name, department_id, salary)
departments (department_id, department_name)
```

**Expected Output Format**:
```
Query OK, 15 rows affected
```

**Solution**:
```sql
UPDATE employees e
JOIN departments d ON e.department_id = d.department_id
SET e.salary = e.salary * 1.10
WHERE d.department_name = 'Sales';
```

**Explanation**:
UPDATE with JOIN allows updates based on conditions from related tables. Always test with SELECT first and consider using transactions for safety.

---

### Question 73: DELETE with Subquery

**Difficulty**: Medium
**Category**: DML
**Company**: Uber

**Problem Statement**:
Delete all orders from customers who have been inactive (no orders in last year).

**Sample Tables**:
```sql
orders (order_id, customer_id, order_date)
customers (customer_id, customer_name)
```

**Expected Output Format**:
```
Query OK, 45 rows affected
```

**Solution**:
```sql
DELETE FROM orders
WHERE customer_id IN (
    SELECT customer_id
    FROM customers c
    WHERE NOT EXISTS (
        SELECT 1 FROM orders o
        WHERE o.customer_id = c.customer_id
        AND o.order_date >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
    )
);
```

**Explanation**:
DELETE with subquery removes rows based on complex conditions. This finds customers with no recent orders and deletes all their historical orders.

---

### Question 74: INSERT with SELECT

**Difficulty**: Medium
**Category**: DML
**Company**: Airbnb

**Problem Statement**:
Archive all orders older than 2 years into an archive table.

**Sample Tables**:
```sql
orders (order_id, customer_id, order_date, amount)
orders_archive (order_id, customer_id, order_date, amount)
```

**Expected Output Format**:
```
Query OK, 1000 rows affected
```

**Solution**:
```sql
INSERT INTO orders_archive (order_id, customer_id, order_date, amount)
SELECT order_id, customer_id, order_date, amount
FROM orders
WHERE order_date < DATE_SUB(CURDATE(), INTERVAL 2 YEAR);
```

**Explanation**:
INSERT ... SELECT copies data from one table to another based on conditions. Useful for archiving, data migration, and creating summary tables.

---

### Question 75: Pivoting Data

**Difficulty**: Medium
**Category**: CASE/Aggregates
**Company**: Stripe

**Problem Statement**:
Pivot quarterly sales data to show quarters as columns.

**Sample Tables**:
```sql
sales (sale_id, product_id, sale_date, amount, quarter)
```

**Expected Output Format**:
| product_id | Q1 | Q2 | Q3 | Q4 |
|------------|-----|-----|-----|-----|
| 101 | 5000 | 6000 | 5500 | 7000 |

**Solution**:
```sql
SELECT product_id,
       SUM(CASE WHEN quarter = 1 THEN amount ELSE 0 END) AS Q1,
       SUM(CASE WHEN quarter = 2 THEN amount ELSE 0 END) AS Q2,
       SUM(CASE WHEN quarter = 3 THEN amount ELSE 0 END) AS Q3,
       SUM(CASE WHEN quarter = 4 THEN amount ELSE 0 END) AS Q4
FROM sales
GROUP BY product_id;
```

**Explanation**:
Manual pivoting uses conditional aggregation to transform rows into columns. SQL Server has PIVOT operator; this approach works across all databases.

---

### Question 76: Finding Gaps in Sequences

**Difficulty**: Medium
**Category**: Subqueries
**Company**: Google

**Problem Statement**:
Find missing employee IDs in the sequence.

**Sample Tables**:
```sql
employees (employee_id, first_name, last_name)
```

**Expected Output Format**:
| missing_id |
|------------|
| 3 |
| 7 |

**Solution**:
```sql
SELECT a.employee_id + 1 AS missing_id
FROM employees a
LEFT JOIN employees b ON a.employee_id + 1 = b.employee_id
WHERE b.employee_id IS NULL
  AND a.employee_id + 1 < (SELECT MAX(employee_id) FROM employees);
```

**Explanation**:
This self JOIN finds where an ID exists but the next consecutive ID doesn't. The condition ensures we don't report gaps after the maximum ID.

---

### Question 77: Consecutive Days

**Difficulty**: Medium
**Category**: Date Functions
**Company**: Amazon

**Problem Statement**:
Find customers who placed orders on consecutive days.

**Sample Tables**:
```sql
orders (order_id, customer_id, order_date)
```

**Expected Output Format**:
| customer_id | order_date | next_order_date |
|-------------|------------|-----------------|
| 101 | 2023-01-15 | 2023-01-16 |

**Solution**:
```sql
SELECT DISTINCT o1.customer_id, o1.order_date,
       DATE_ADD(o1.order_date, INTERVAL 1 DAY) AS next_order_date
FROM orders o1
INNER JOIN orders o2
    ON o1.customer_id = o2.customer_id
    AND o2.order_date = DATE_ADD(o1.order_date, INTERVAL 1 DAY);
```

**Explanation**:
Self JOIN matches orders where the next day's order exists for the same customer. This pattern identifies consecutive activities or streaks.

---

### Question 78: Mode (Most Frequent Value)

**Difficulty**: Medium
**Category**: Aggregates
**Company**: Meta

**Problem Statement**:
Find the most common salary in the company.

**Sample Tables**:
```sql
employees (employee_id, first_name, salary)
```

**Expected Output Format**:
| salary | frequency |
|--------|-----------|
| 65000 | 15 |

**Solution**:
```sql
SELECT salary, COUNT(*) AS frequency
FROM employees
GROUP BY salary
ORDER BY frequency DESC
LIMIT 1;
```

**Explanation**:
Group by the value and count occurrences. ORDER BY count DESC with LIMIT 1 returns the most frequent value (mode). Multiple modes require more complex handling.

---

### Question 79: Median Calculation

**Difficulty**: Medium
**Category**: Aggregates
**Company**: Netflix

**Problem Statement**:
Calculate the median salary of employees.

**Sample Tables**:
```sql
employees (employee_id, first_name, salary)
```

**Expected Output Format**:
| median_salary |
|---------------|
| 67500.00 |

**Solution**:
```sql
SELECT AVG(salary) AS median_salary
FROM (
    SELECT salary
    FROM employees
    ORDER BY salary
    LIMIT 2 - (SELECT COUNT(*) FROM employees) % 2
    OFFSET (SELECT (COUNT(*) - 1) / 2 FROM employees)
) AS median_values;
```

**Explanation**:
This finds the middle value(s) by calculating the offset to the median position. For odd counts, it selects 1 value; for even, it averages 2.

---

### Question 80: Working Days Calculation

**Difficulty**: Medium
**Category**: Date Functions
**Company**: Microsoft

**Problem Statement**:
Calculate the number of weekdays between hire date and today.

**Sample Tables**:
```sql
employees (employee_id, first_name, hire_date)
```

**Expected Output Format**:
| employee_id | first_name | working_days |
|-------------|------------|--------------|
| 1 | John | 892 |

**Solution**:
```sql
SELECT employee_id, first_name,
       (DATEDIFF(CURDATE(), hire_date) + 1)
       - (FLOOR(DATEDIFF(CURDATE(), hire_date) / 7) * 2)
       - (CASE WHEN DAYOFWEEK(hire_date) = 1 THEN 1 ELSE 0 END)
       - (CASE WHEN DAYOFWEEK(CURDATE()) = 7 THEN 1 ELSE 0 END) AS working_days
FROM employees;
```

**Explanation**:
Calculate total days, subtract weekends (2 per week), then adjust for start/end day edge cases. This approximation doesn't account for holidays.

---

## Hard Questions (81-100)

Questions covering window functions, complex analytics, optimization scenarios, and database design.

---

### Question 81: ROW_NUMBER

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Google

**Problem Statement**:
Assign a unique row number to each employee within their department, ordered by salary descending.

**Sample Tables**:
```sql
employees (employee_id, first_name, department_id, salary)
```

**Expected Output Format**:
| employee_id | first_name | department_id | salary | row_num |
|-------------|------------|---------------|--------|---------|
| 3 | Alice | 101 | 95000 | 1 |
| 1 | John | 101 | 72000 | 2 |

**Solution**:
```sql
SELECT employee_id, first_name, department_id, salary,
       ROW_NUMBER() OVER (PARTITION BY department_id ORDER BY salary DESC) AS row_num
FROM employees;
```

**Explanation**:
ROW_NUMBER() assigns unique sequential integers within each partition. PARTITION BY groups data; ORDER BY determines the sequence. No ties - each row gets unique number.

---

### Question 82: RANK and DENSE_RANK

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Amazon

**Problem Statement**:
Rank employees by salary, showing both RANK and DENSE_RANK.

**Sample Tables**:
```sql
employees (employee_id, first_name, salary)
```

**Expected Output Format**:
| first_name | salary | rank | dense_rank |
|------------|--------|------|------------|
| Alice | 95000 | 1 | 1 |
| Bob | 85000 | 2 | 2 |
| John | 85000 | 2 | 2 |
| Jane | 72000 | 4 | 3 |

**Solution**:
```sql
SELECT first_name, salary,
       RANK() OVER (ORDER BY salary DESC) AS rank,
       DENSE_RANK() OVER (ORDER BY salary DESC) AS dense_rank
FROM employees;
```

**Explanation**:
RANK() assigns same rank to ties but skips numbers (1,2,2,4). DENSE_RANK() also handles ties but doesn't skip (1,2,2,3). Choose based on use case.

---

### Question 83: LAG and LEAD

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Meta

**Problem Statement**:
Show each employee's salary alongside the previous and next employee's salary (ordered by hire date).

**Sample Tables**:
```sql
employees (employee_id, first_name, hire_date, salary)
```

**Expected Output Format**:
| first_name | salary | prev_salary | next_salary |
|------------|--------|-------------|-------------|
| John | 50000 | NULL | 62000 |
| Jane | 62000 | 50000 | 75000 |

**Solution**:
```sql
SELECT first_name, salary,
       LAG(salary) OVER (ORDER BY hire_date) AS prev_salary,
       LEAD(salary) OVER (ORDER BY hire_date) AS next_salary
FROM employees;
```

**Explanation**:
LAG() accesses previous row's value; LEAD() accesses next row's value. Optional parameters: LAG(col, offset, default). Essential for time-series analysis.

---

### Question 84: Running Sum with Window Functions

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Netflix

**Problem Statement**:
Calculate the running total of sales amount per product.

**Sample Tables**:
```sql
sales (sale_id, product_id, sale_date, amount)
```

**Expected Output Format**:
| product_id | sale_date | amount | running_total |
|------------|-----------|--------|---------------|
| 101 | 2023-01-01 | 100 | 100 |
| 101 | 2023-01-02 | 150 | 250 |

**Solution**:
```sql
SELECT product_id, sale_date, amount,
       SUM(amount) OVER (PARTITION BY product_id ORDER BY sale_date
                        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_total
FROM sales
ORDER BY product_id, sale_date;
```

**Explanation**:
Window functions with SUM() compute running totals efficiently. The frame clause defines which rows to include. PARTITION BY restarts the sum for each product.

---

### Question 85: Moving Average

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Microsoft

**Problem Statement**:
Calculate a 3-day moving average of daily sales.

**Sample Tables**:
```sql
daily_sales (sale_date, total_amount)
```

**Expected Output Format**:
| sale_date | total_amount | moving_avg |
|-----------|--------------|------------|
| 2023-01-03 | 300 | 200.00 |

**Solution**:
```sql
SELECT sale_date, total_amount,
       ROUND(AVG(total_amount) OVER (ORDER BY sale_date
             ROWS BETWEEN 2 PRECEDING AND CURRENT ROW), 2) AS moving_avg
FROM daily_sales;
```

**Explanation**:
Moving average uses AVG with a sliding window. ROWS BETWEEN 2 PRECEDING AND CURRENT ROW includes current row plus 2 prior rows for a 3-day average.

---

### Question 86: FIRST_VALUE and LAST_VALUE

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Apple

**Problem Statement**:
For each department, show the highest and lowest paid employee names.

**Sample Tables**:
```sql
employees (employee_id, first_name, department_id, salary)
```

**Expected Output Format**:
| department_id | first_name | salary | highest_paid | lowest_paid |
|---------------|------------|--------|--------------|-------------|
| 101 | Alice | 95000 | Alice | Tom |

**Solution**:
```sql
SELECT department_id, first_name, salary,
       FIRST_VALUE(first_name) OVER (PARTITION BY department_id ORDER BY salary DESC) AS highest_paid,
       FIRST_VALUE(first_name) OVER (PARTITION BY department_id ORDER BY salary ASC) AS lowest_paid
FROM employees;
```

**Explanation**:
FIRST_VALUE returns the first value in the window. By ordering DESC we get highest, ASC gives lowest. LAST_VALUE needs frame specification due to default behavior.

---

### Question 87: NTILE for Percentiles

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Uber

**Problem Statement**:
Divide employees into 4 salary quartiles.

**Sample Tables**:
```sql
employees (employee_id, first_name, salary)
```

**Expected Output Format**:
| first_name | salary | quartile |
|------------|--------|----------|
| Alice | 95000 | 1 |
| Bob | 72000 | 2 |

**Solution**:
```sql
SELECT first_name, salary,
       NTILE(4) OVER (ORDER BY salary DESC) AS quartile
FROM employees;
```

**Explanation**:
NTILE(n) divides rows into n roughly equal groups. It's perfect for percentile analysis. Quartile 1 = top 25%, quartile 4 = bottom 25%.

---

### Question 88: Cumulative Distribution

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Airbnb

**Problem Statement**:
Calculate the percentile rank of each employee's salary.

**Sample Tables**:
```sql
employees (employee_id, first_name, salary)
```

**Expected Output Format**:
| first_name | salary | percentile |
|------------|--------|------------|
| Alice | 95000 | 0.95 |
| Bob | 72000 | 0.65 |

**Solution**:
```sql
SELECT first_name, salary,
       ROUND(PERCENT_RANK() OVER (ORDER BY salary), 2) AS percentile
FROM employees;
```

**Explanation**:
PERCENT_RANK() calculates relative rank as (rank - 1) / (total rows - 1). CUME_DIST() is similar but includes current row. Both return values between 0 and 1.

---

### Question 89: Gap Analysis in Time Series

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Stripe

**Problem Statement**:
Find gaps in daily login data (days with no logins).

**Sample Tables**:
```sql
user_logins (login_date, user_count)
```

**Expected Output Format**:
| prev_date | next_date | gap_days |
|-----------|-----------|----------|
| 2023-01-15 | 2023-01-18 | 2 |

**Solution**:
```sql
WITH login_gaps AS (
    SELECT login_date,
           LEAD(login_date) OVER (ORDER BY login_date) AS next_date
    FROM user_logins
)
SELECT login_date AS prev_date, next_date,
       DATEDIFF(next_date, login_date) - 1 AS gap_days
FROM login_gaps
WHERE DATEDIFF(next_date, login_date) > 1;
```

**Explanation**:
LEAD finds the next date; subtracting gives the gap. Filter for gaps > 1 day. This pattern identifies missing periods in time series data.

---

### Question 90: Top N Per Group

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Google

**Problem Statement**:
Find the top 3 highest-paid employees in each department.

**Sample Tables**:
```sql
employees (employee_id, first_name, department_id, salary)
```

**Expected Output Format**:
| department_id | first_name | salary | rank |
|---------------|------------|--------|------|
| 101 | Alice | 95000 | 1 |
| 101 | Bob | 85000 | 2 |

**Solution**:
```sql
WITH ranked AS (
    SELECT employee_id, first_name, department_id, salary,
           DENSE_RANK() OVER (PARTITION BY department_id ORDER BY salary DESC) AS rank
    FROM employees
)
SELECT department_id, first_name, salary, rank
FROM ranked
WHERE rank <= 3
ORDER BY department_id, rank;
```

**Explanation**:
Use DENSE_RANK() in a CTE to rank within each department, then filter for top 3. DENSE_RANK handles ties by including all tied employees in top N.

---

### Question 91: Year-over-Year Growth

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Amazon

**Problem Statement**:
Calculate year-over-year revenue growth percentage.

**Sample Tables**:
```sql
yearly_revenue (year, revenue)
```

**Expected Output Format**:
| year | revenue | prev_revenue | yoy_growth |
|------|---------|--------------|------------|
| 2023 | 1200000 | 1000000 | 20.00 |

**Solution**:
```sql
SELECT year, revenue,
       LAG(revenue) OVER (ORDER BY year) AS prev_revenue,
       ROUND((revenue - LAG(revenue) OVER (ORDER BY year)) * 100.0 /
             LAG(revenue) OVER (ORDER BY year), 2) AS yoy_growth
FROM yearly_revenue;
```

**Explanation**:
LAG retrieves previous year's revenue. Growth = (current - previous) / previous * 100. This is a fundamental business metric for tracking performance trends.

---

### Question 92: Session Analysis

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Meta

**Problem Statement**:
Calculate session duration for each user (sessions are sequences of activities within 30 minutes).

**Sample Tables**:
```sql
user_activity (user_id, activity_timestamp, activity_type)
```

**Expected Output Format**:
| user_id | session_id | session_start | session_end | duration_mins |
|---------|------------|---------------|-------------|---------------|
| 101 | 1 | 2023-01-15 10:00 | 2023-01-15 10:45 | 45 |

**Solution**:
```sql
WITH activity_gaps AS (
    SELECT user_id, activity_timestamp,
           CASE WHEN TIMESTAMPDIFF(MINUTE,
                LAG(activity_timestamp) OVER (PARTITION BY user_id ORDER BY activity_timestamp),
                activity_timestamp) > 30
                OR LAG(activity_timestamp) OVER (PARTITION BY user_id ORDER BY activity_timestamp) IS NULL
           THEN 1 ELSE 0 END AS new_session
    FROM user_activity
),
sessions AS (
    SELECT user_id, activity_timestamp,
           SUM(new_session) OVER (PARTITION BY user_id ORDER BY activity_timestamp) AS session_id
    FROM activity_gaps
)
SELECT user_id, session_id,
       MIN(activity_timestamp) AS session_start,
       MAX(activity_timestamp) AS session_end,
       TIMESTAMPDIFF(MINUTE, MIN(activity_timestamp), MAX(activity_timestamp)) AS duration_mins
FROM sessions
GROUP BY user_id, session_id;
```

**Explanation**:
This identifies session breaks (>30 min gaps), assigns session IDs using cumulative sum of break indicators, then aggregates to get session metrics.

---

### Question 93: Funnel Analysis

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Netflix

**Problem Statement**:
Calculate conversion rates through a signup funnel (visit -> signup -> purchase).

**Sample Tables**:
```sql
funnel_events (user_id, event_type, event_date)
```

**Expected Output Format**:
| step | event_type | users | conversion_rate |
|------|------------|-------|-----------------|
| 1 | visit | 10000 | 100.00 |
| 2 | signup | 3000 | 30.00 |
| 3 | purchase | 900 | 9.00 |

**Solution**:
```sql
WITH step_counts AS (
    SELECT 1 AS step, 'visit' AS event_type, COUNT(DISTINCT user_id) AS users FROM funnel_events WHERE event_type = 'visit'
    UNION ALL
    SELECT 2, 'signup', COUNT(DISTINCT user_id) FROM funnel_events WHERE event_type = 'signup'
    UNION ALL
    SELECT 3, 'purchase', COUNT(DISTINCT user_id) FROM funnel_events WHERE event_type = 'purchase'
)
SELECT step, event_type, users,
       ROUND(users * 100.0 / FIRST_VALUE(users) OVER (ORDER BY step), 2) AS conversion_rate
FROM step_counts;
```

**Explanation**:
Count unique users at each funnel step, then calculate conversion as percentage of initial step. FIRST_VALUE gets the baseline (visit count).

---

### Question 94: Cohort Retention Analysis

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Airbnb

**Problem Statement**:
Calculate monthly retention rates for user cohorts based on signup month.

**Sample Tables**:
```sql
users (user_id, signup_date)
user_activity (user_id, activity_date)
```

**Expected Output Format**:
| cohort_month | month_number | retained_users | retention_rate |
|--------------|--------------|----------------|----------------|
| 2023-01 | 0 | 1000 | 100.00 |
| 2023-01 | 1 | 650 | 65.00 |

**Solution**:
```sql
WITH user_cohorts AS (
    SELECT user_id, DATE_FORMAT(signup_date, '%Y-%m') AS cohort_month
    FROM users
),
activity_months AS (
    SELECT DISTINCT user_id, DATE_FORMAT(activity_date, '%Y-%m') AS activity_month
    FROM user_activity
),
cohort_size AS (
    SELECT cohort_month, COUNT(*) AS total_users
    FROM user_cohorts GROUP BY cohort_month
)
SELECT uc.cohort_month,
       TIMESTAMPDIFF(MONTH, STR_TO_DATE(CONCAT(uc.cohort_month, '-01'), '%Y-%m-%d'),
                     STR_TO_DATE(CONCAT(am.activity_month, '-01'), '%Y-%m-%d')) AS month_number,
       COUNT(DISTINCT uc.user_id) AS retained_users,
       ROUND(COUNT(DISTINCT uc.user_id) * 100.0 / cs.total_users, 2) AS retention_rate
FROM user_cohorts uc
JOIN activity_months am ON uc.user_id = am.user_id
JOIN cohort_size cs ON uc.cohort_month = cs.cohort_month
GROUP BY uc.cohort_month, month_number, cs.total_users
ORDER BY uc.cohort_month, month_number;
```

**Explanation**:
Cohort analysis groups users by signup month and tracks their activity over time. This calculates what percentage of each cohort remains active in subsequent months.

---

### Question 95: Finding Consecutive Streaks

**Difficulty**: Hard
**Category**: Window Functions
**Company**: Stripe

**Problem Statement**:
Find the longest consecutive login streak for each user.

**Sample Tables**:
```sql
logins (user_id, login_date)
```

**Expected Output Format**:
| user_id | longest_streak |
|---------|----------------|
| 101 | 15 |
| 102 | 8 |

**Solution**:
```sql
WITH date_groups AS (
    SELECT user_id, login_date,
           DATE_SUB(login_date, INTERVAL ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date) DAY) AS grp
    FROM (SELECT DISTINCT user_id, login_date FROM logins) t
),
streaks AS (
    SELECT user_id, grp, COUNT(*) AS streak_length
    FROM date_groups
    GROUP BY user_id, grp
)
SELECT user_id, MAX(streak_length) AS longest_streak
FROM streaks
GROUP BY user_id;
```

**Explanation**:
Subtracting row number from date creates groups where consecutive dates have the same value. Counting per group gives streak length. The gaps-and-islands technique.

---

### Question 96: Recursive CTE for Hierarchy

**Difficulty**: Hard
**Category**: CTEs
**Company**: Microsoft

**Problem Statement**:
List all employees under a specific manager (including indirect reports).

**Sample Tables**:
```sql
employees (employee_id, first_name, manager_id)
```

**Expected Output Format**:
| employee_id | first_name | level |
|-------------|------------|-------|
| 2 | Jane | 1 |
| 5 | Bob | 2 |

**Solution**:
```sql
WITH RECURSIVE emp_hierarchy AS (
    -- Base case: direct reports
    SELECT employee_id, first_name, manager_id, 1 AS level
    FROM employees
    WHERE manager_id = 1  -- Starting manager ID

    UNION ALL

    -- Recursive case: reports of reports
    SELECT e.employee_id, e.first_name, e.manager_id, h.level + 1
    FROM employees e
    INNER JOIN emp_hierarchy h ON e.manager_id = h.employee_id
)
SELECT employee_id, first_name, level
FROM emp_hierarchy
ORDER BY level, employee_id;
```

**Explanation**:
Recursive CTEs traverse hierarchical data. The base case gets direct reports; the recursive part finds each level's subordinates. Essential for org charts and tree structures.

---

### Question 97: Query Optimization Problem

**Difficulty**: Hard
**Category**: Optimization
**Company**: Google

**Problem Statement**:
Optimize this slow query that finds customers with orders exceeding their credit limit.

**Sample Tables**:
```sql
customers (customer_id, customer_name, credit_limit)
orders (order_id, customer_id, order_date, amount)
```

**Original Slow Query**:
```sql
SELECT c.customer_name
FROM customers c
WHERE (SELECT SUM(amount) FROM orders WHERE customer_id = c.customer_id) > c.credit_limit;
```

**Solution**:
```sql
-- Optimized version using JOIN and GROUP BY
SELECT c.customer_name
FROM customers c
INNER JOIN (
    SELECT customer_id, SUM(amount) AS total_orders
    FROM orders
    GROUP BY customer_id
) o ON c.customer_id = o.customer_id
WHERE o.total_orders > c.credit_limit;

-- Also ensure indexes exist:
-- CREATE INDEX idx_orders_customer ON orders(customer_id);
-- CREATE INDEX idx_customers_credit ON customers(customer_id, credit_limit);
```

**Explanation**:
Correlated subqueries execute once per row (O(n*m) complexity). JOIN with pre-aggregated subquery executes aggregation once (O(n+m)). Adding proper indexes further improves performance.

---

### Question 98: Index Design Problem

**Difficulty**: Hard
**Category**: Database Design
**Company**: Amazon

**Problem Statement**:
Design indexes for a query that searches orders by date range and status, then sorts by amount.

**Sample Tables**:
```sql
orders (order_id, customer_id, order_date, status, amount)
```

**Query to Optimize**:
```sql
SELECT * FROM orders
WHERE order_date BETWEEN '2023-01-01' AND '2023-12-31'
  AND status = 'completed'
ORDER BY amount DESC
LIMIT 100;
```

**Solution**:
```sql
-- Composite index for WHERE clause (most selective first)
CREATE INDEX idx_orders_status_date ON orders(status, order_date);

-- Or if date is more selective:
CREATE INDEX idx_orders_date_status ON orders(order_date, status);

-- For covering index that avoids table lookup:
CREATE INDEX idx_orders_covering ON orders(status, order_date, amount, order_id, customer_id);

-- Analysis: Check selectivity
SELECT status, COUNT(*) FROM orders GROUP BY status;
SELECT YEAR(order_date), COUNT(*) FROM orders GROUP BY YEAR(order_date);
```

**Explanation**:
Composite indexes should list columns in order of: equality conditions, range conditions, order by. Put most selective column first. Covering indexes include all SELECT columns.

---

### Question 99: Deadlock Prevention

**Difficulty**: Hard
**Category**: Database Design
**Company**: Meta

**Problem Statement**:
Rewrite this transaction to prevent deadlocks when transferring money between accounts.

**Sample Tables**:
```sql
accounts (account_id, balance)
```

**Original Problematic Code**:
```sql
-- Session 1: Transfer A -> B
BEGIN;
UPDATE accounts SET balance = balance - 100 WHERE account_id = 'A';
UPDATE accounts SET balance = balance + 100 WHERE account_id = 'B';
COMMIT;

-- Session 2: Transfer B -> A (can deadlock)
BEGIN;
UPDATE accounts SET balance = balance - 50 WHERE account_id = 'B';
UPDATE accounts SET balance = balance + 50 WHERE account_id = 'A';
COMMIT;
```

**Solution**:
```sql
-- Always lock accounts in consistent order (by account_id)
DELIMITER //
CREATE PROCEDURE transfer_funds(
    IN from_account VARCHAR(10),
    IN to_account VARCHAR(10),
    IN amount DECIMAL(10,2)
)
BEGIN
    DECLARE first_account VARCHAR(10);
    DECLARE second_account VARCHAR(10);

    -- Determine lock order (alphabetically)
    IF from_account < to_account THEN
        SET first_account = from_account;
        SET second_account = to_account;
    ELSE
        SET first_account = to_account;
        SET second_account = from_account;
    END IF;

    START TRANSACTION;

    -- Lock in consistent order
    SELECT balance FROM accounts WHERE account_id = first_account FOR UPDATE;
    SELECT balance FROM accounts WHERE account_id = second_account FOR UPDATE;

    -- Perform transfer
    UPDATE accounts SET balance = balance - amount WHERE account_id = from_account;
    UPDATE accounts SET balance = balance + amount WHERE account_id = to_account;

    COMMIT;
END //
DELIMITER ;
```

**Explanation**:
Deadlocks occur when sessions lock resources in different orders. The solution acquires locks in a consistent order (alphabetically), preventing circular wait conditions.

---

### Question 100: Data Warehouse Star Schema

**Difficulty**: Hard
**Category**: Database Design
**Company**: Netflix

**Problem Statement**:
Design a star schema for analyzing sales data with dimensions for product, customer, time, and store.

**Expected Output Format**:
A star schema design with fact and dimension tables.

**Solution**:
```sql
-- Dimension Tables

CREATE TABLE dim_date (
    date_key INT PRIMARY KEY,
    full_date DATE,
    day_of_week VARCHAR(10),
    day_of_month INT,
    month INT,
    month_name VARCHAR(10),
    quarter INT,
    year INT,
    is_weekend BOOLEAN,
    is_holiday BOOLEAN
);

CREATE TABLE dim_product (
    product_key INT PRIMARY KEY,
    product_id VARCHAR(20),
    product_name VARCHAR(100),
    category VARCHAR(50),
    subcategory VARCHAR(50),
    brand VARCHAR(50),
    unit_cost DECIMAL(10,2),
    unit_price DECIMAL(10,2)
);

CREATE TABLE dim_customer (
    customer_key INT PRIMARY KEY,
    customer_id VARCHAR(20),
    customer_name VARCHAR(100),
    email VARCHAR(100),
    city VARCHAR(50),
    state VARCHAR(50),
    country VARCHAR(50),
    segment VARCHAR(20)
);

CREATE TABLE dim_store (
    store_key INT PRIMARY KEY,
    store_id VARCHAR(20),
    store_name VARCHAR(100),
    city VARCHAR(50),
    state VARCHAR(50),
    country VARCHAR(50),
    store_type VARCHAR(20)
);

-- Fact Table

CREATE TABLE fact_sales (
    sale_key BIGINT PRIMARY KEY AUTO_INCREMENT,
    date_key INT,
    product_key INT,
    customer_key INT,
    store_key INT,
    quantity INT,
    unit_price DECIMAL(10,2),
    discount_amount DECIMAL(10,2),
    sales_amount DECIMAL(10,2),
    cost_amount DECIMAL(10,2),
    profit_amount DECIMAL(10,2),
    FOREIGN KEY (date_key) REFERENCES dim_date(date_key),
    FOREIGN KEY (product_key) REFERENCES dim_product(product_key),
    FOREIGN KEY (customer_key) REFERENCES dim_customer(customer_key),
    FOREIGN KEY (store_key) REFERENCES dim_store(store_key)
);

-- Indexes for common query patterns
CREATE INDEX idx_fact_date ON fact_sales(date_key);
CREATE INDEX idx_fact_product ON fact_sales(product_key);
CREATE INDEX idx_fact_customer ON fact_sales(customer_key);
CREATE INDEX idx_fact_store ON fact_sales(store_key);

-- Sample analytical query
SELECT
    d.year,
    d.quarter,
    p.category,
    s.country,
    SUM(f.sales_amount) AS total_sales,
    SUM(f.profit_amount) AS total_profit,
    AVG(f.discount_amount) AS avg_discount
FROM fact_sales f
JOIN dim_date d ON f.date_key = d.date_key
JOIN dim_product p ON f.product_key = p.product_key
JOIN dim_store s ON f.store_key = s.store_key
WHERE d.year = 2023
GROUP BY d.year, d.quarter, p.category, s.country
ORDER BY total_sales DESC;
```

**Explanation**:
Star schemas optimize analytical queries with a central fact table (measurable events) surrounded by dimension tables (descriptive attributes). Surrogate keys, denormalization in dimensions, and indexed foreign keys enable fast aggregations across multiple dimensions.

---

## Summary

This compilation covers the essential SQL concepts tested in technical interviews at top tech companies:

**Easy (1-30)**: Foundation skills including SELECT, WHERE, ORDER BY, LIMIT, basic aggregates, and simple JOINs.

**Medium (31-80)**: Intermediate concepts including HAVING, subqueries, CASE statements, CTEs, date/string functions, and complex JOINs.

**Hard (81-100)**: Advanced topics including window functions (ROW_NUMBER, RANK, LAG/LEAD, etc.), optimization, and database design.

### Key Topics by Frequency:
- Window Functions: Most commonly asked at FAANG
- JOINs: Essential for all levels
- Aggregation with GROUP BY/HAVING: Very frequent
- Subqueries: Common for problem-solving questions
- CASE Statements: Important for data transformation
- Date Functions: Common in reporting questions
- CTEs: Modern SQL practice

### Practice Recommendations:
1. Master window functions - they're the most differentiating skill
2. Practice writing queries without running them (whiteboard practice)
3. Learn to optimize queries and understand execution plans
4. Understand when to use different JOIN types
5. Practice explaining your approach before writing code

Good luck with your SQL interviews!
