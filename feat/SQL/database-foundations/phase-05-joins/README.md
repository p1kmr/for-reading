# Phase 5: SQL Joins Deep Dive

## Table of Contents
1. [Introduction](#introduction)
2. [Sample Tables Schema](#sample-tables-schema)
3. [Join Types](#join-types)
   - [INNER JOIN](#inner-join)
   - [LEFT JOIN](#left-join-left-outer-join)
   - [RIGHT JOIN](#right-join-right-outer-join)
   - [FULL OUTER JOIN](#full-outer-join)
   - [CROSS JOIN](#cross-join)
   - [SELF JOIN](#self-join)
4. [Code Examples](#code-examples)
5. [Practice Questions](#practice-questions)
6. [Common Mistakes](#common-mistakes)
7. [Join Execution Flow](#join-execution-flow)
8. [Performance Considerations](#performance-considerations)

---

## Introduction

SQL Joins are the cornerstone of relational database querying, allowing you to combine data from multiple tables based on related columns. Understanding joins is essential for writing efficient queries and building complex data retrieval operations.

This phase provides a comprehensive deep dive into all join types, with practical examples and real-world scenarios to solidify your understanding.

---

## Sample Tables Schema

Throughout this guide, we'll use the following tables:

```sql
-- Employees table (includes manager_id for self-join examples)
CREATE TABLE employees (
    employee_id INT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100),
    department_id INT,
    manager_id INT,
    hire_date DATE,
    salary DECIMAL(10, 2)
);

-- Departments table
CREATE TABLE departments (
    department_id INT PRIMARY KEY,
    department_name VARCHAR(100),
    location VARCHAR(100),
    budget DECIMAL(12, 2)
);

-- Customers table
CREATE TABLE customers (
    customer_id INT PRIMARY KEY,
    customer_name VARCHAR(100),
    email VARCHAR(100),
    city VARCHAR(50),
    registration_date DATE
);

-- Sales table
CREATE TABLE sales (
    sale_id INT PRIMARY KEY,
    customer_id INT,
    employee_id INT,
    product_name VARCHAR(100),
    sale_amount DECIMAL(10, 2),
    sale_date DATE
);

-- Projects table
CREATE TABLE projects (
    project_id INT PRIMARY KEY,
    project_name VARCHAR(100),
    department_id INT,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20)
);

-- Employee_Projects junction table (many-to-many)
CREATE TABLE employee_projects (
    employee_id INT,
    project_id INT,
    role VARCHAR(50),
    hours_allocated INT,
    PRIMARY KEY (employee_id, project_id)
);
```

### Sample Data

```sql
-- Employees
INSERT INTO employees VALUES
(1, 'John', 'Smith', 'john.smith@company.com', 1, NULL, '2020-01-15', 75000),
(2, 'Sarah', 'Johnson', 'sarah.j@company.com', 1, 1, '2020-03-20', 65000),
(3, 'Michael', 'Brown', 'michael.b@company.com', 2, 1, '2019-08-10', 80000),
(4, 'Emily', 'Davis', 'emily.d@company.com', 2, 3, '2021-02-01', 55000),
(5, 'David', 'Wilson', 'david.w@company.com', 3, 1, '2020-11-30', 70000),
(6, 'Lisa', 'Taylor', 'lisa.t@company.com', NULL, 3, '2022-01-10', 50000),
(7, 'James', 'Anderson', 'james.a@company.com', 4, NULL, '2018-05-15', 90000);

-- Departments
INSERT INTO departments VALUES
(1, 'Engineering', 'Building A', 500000),
(2, 'Sales', 'Building B', 300000),
(3, 'Marketing', 'Building B', 250000),
(5, 'HR', 'Building C', 150000);

-- Customers
INSERT INTO customers VALUES
(1, 'Acme Corp', 'acme@email.com', 'New York', '2021-01-10'),
(2, 'Tech Solutions', 'tech@email.com', 'San Francisco', '2021-03-15'),
(3, 'Global Industries', 'global@email.com', 'Chicago', '2020-08-20'),
(4, 'StartUp Inc', 'startup@email.com', 'Austin', '2022-02-01'),
(5, 'Big Data LLC', 'bigdata@email.com', 'Seattle', '2021-11-30');

-- Sales
INSERT INTO sales VALUES
(1, 1, 3, 'Software License', 15000, '2023-01-15'),
(2, 2, 3, 'Consulting', 8000, '2023-02-20'),
(3, 1, 4, 'Support Package', 5000, '2023-03-10'),
(4, 3, 3, 'Software License', 12000, '2023-03-25'),
(5, NULL, 4, 'Training', 3000, '2023-04-01'),
(6, 2, NULL, 'Hardware', 7500, '2023-04-15');

-- Projects
INSERT INTO projects VALUES
(1, 'Website Redesign', 1, '2023-01-01', '2023-06-30', 'Active'),
(2, 'Mobile App', 1, '2023-03-01', '2023-12-31', 'Active'),
(3, 'CRM Integration', 2, '2023-02-15', '2023-08-15', 'Completed'),
(4, 'Data Migration', NULL, '2023-04-01', '2023-09-30', 'Active');

-- Employee_Projects
INSERT INTO employee_projects VALUES
(1, 1, 'Lead', 40),
(2, 1, 'Developer', 30),
(3, 3, 'Manager', 20),
(4, 3, 'Analyst', 35),
(1, 2, 'Architect', 25),
(5, 2, 'Designer', 30);
```

---

## Join Types

---

### INNER JOIN

#### WHY
Use INNER JOIN when you need to retrieve only the records that have matching values in both tables. This is the most common join type and ensures data integrity by excluding unmatched records.

#### WHAT
An INNER JOIN returns only the rows where there is a match in both tables based on the join condition. If a row in either table doesn't have a corresponding match in the other table, it is excluded from the result set.

#### HOW
```sql
SELECT columns
FROM table1
INNER JOIN table2
ON table1.column = table2.column;
```

#### WHEN
- When you need only records that exist in both tables
- When NULL values in the relationship should be excluded
- For reporting where incomplete data should not appear
- Most common in transactional queries

#### Venn Diagram
```
    Table A         Table B
   +-------+       +-------+
  /         \     /         \
 /           \   /           \
|             | |             |
|      +------###------+      |
|      |      ###      |      |
|      |    RESULT     |      |
|      +------###------+      |
|             | |             |
 \           /   \           /
  \         /     \         /
   +-------+       +-------+

INNER JOIN: Only the intersection (###)
```

#### Visual Table Example

**employees table:**
| employee_id | first_name | department_id |
|-------------|------------|---------------|
| 1           | John       | 1             |
| 2           | Sarah      | 1             |
| 3           | Michael    | 2             |
| 6           | Lisa       | NULL          |
| 7           | James      | 4             |

**departments table:**
| department_id | department_name |
|---------------|-----------------|
| 1             | Engineering     |
| 2             | Sales           |
| 3             | Marketing       |
| 5             | HR              |

**INNER JOIN Result:**
```sql
SELECT e.employee_id, e.first_name, d.department_name
FROM employees e
INNER JOIN departments d ON e.department_id = d.department_id;
```

| employee_id | first_name | department_name |
|-------------|------------|-----------------|
| 1           | John       | Engineering     |
| 2           | Sarah      | Engineering     |
| 3           | Michael    | Sales           |

**Step-by-step execution:**
1. Take employee_id=1 (John, dept=1) - Match found in departments (Engineering) - **INCLUDED**
2. Take employee_id=2 (Sarah, dept=1) - Match found in departments (Engineering) - **INCLUDED**
3. Take employee_id=3 (Michael, dept=2) - Match found in departments (Sales) - **INCLUDED**
4. Take employee_id=6 (Lisa, dept=NULL) - No match (NULL != any dept_id) - **EXCLUDED**
5. Take employee_id=7 (James, dept=4) - No match (dept 4 doesn't exist) - **EXCLUDED**

---

### LEFT JOIN (LEFT OUTER JOIN)

#### WHY
Use LEFT JOIN when you need all records from the left table regardless of whether they have matches in the right table. This is essential for identifying orphan records or maintaining complete lists with optional related data.

#### WHAT
A LEFT JOIN returns all rows from the left table and the matched rows from the right table. If there's no match, NULL values are returned for columns from the right table.

#### HOW
```sql
SELECT columns
FROM table1
LEFT JOIN table2
ON table1.column = table2.column;
```

#### WHEN
- When you need all records from the primary table
- To find records without matches (orphan records)
- For optional relationships
- When NULL results are acceptable/meaningful

#### Venn Diagram
```
    Table A         Table B
   +-------+       +-------+
  /  ###   \     /         \
 /   ###    \   /           \
|    ###     | |             |
|    ###-----###------+      |
|    ###     ###      |      |
|    ###   RESULT     |      |
|    ###-----###------+      |
|    ###     | |             |
 \   ###    /   \           /
  \  ###   /     \         /
   +-------+       +-------+

LEFT JOIN: All of A plus intersection
```

#### Visual Table Example

**LEFT JOIN Result:**
```sql
SELECT e.employee_id, e.first_name, d.department_name
FROM employees e
LEFT JOIN departments d ON e.department_id = d.department_id;
```

| employee_id | first_name | department_name |
|-------------|------------|-----------------|
| 1           | John       | Engineering     |
| 2           | Sarah      | Engineering     |
| 3           | Michael    | Sales           |
| 6           | Lisa       | NULL            |
| 7           | James      | NULL            |

**Step-by-step execution:**
1. Take employee_id=1 (John, dept=1) - Match found - **department_name = 'Engineering'**
2. Take employee_id=2 (Sarah, dept=1) - Match found - **department_name = 'Engineering'**
3. Take employee_id=3 (Michael, dept=2) - Match found - **department_name = 'Sales'**
4. Take employee_id=6 (Lisa, dept=NULL) - No match - **department_name = NULL**
5. Take employee_id=7 (James, dept=4) - No match - **department_name = NULL**

---

### RIGHT JOIN (RIGHT OUTER JOIN)

#### WHY
Use RIGHT JOIN when you need all records from the right table regardless of matches in the left table. This is less common than LEFT JOIN but useful for maintaining the perspective of the secondary table.

#### WHAT
A RIGHT JOIN returns all rows from the right table and the matched rows from the left table. If there's no match, NULL values are returned for columns from the left table.

#### HOW
```sql
SELECT columns
FROM table1
RIGHT JOIN table2
ON table1.column = table2.column;
```

#### WHEN
- When you need all records from the secondary table
- When the right table contains reference data
- Can usually be rewritten as LEFT JOIN by swapping table order
- Less commonly used than LEFT JOIN

#### Venn Diagram
```
    Table A         Table B
   +-------+       +-------+
  /         \     /   ###  \
 /           \   /    ###   \
|             | |     ###    |
|      +------###-----###    |
|      |      ###     ###    |
|      |    RESULT    ###    |
|      +------###-----###    |
|             | |     ###    |
 \           /   \    ###   /
  \         /     \   ###  /
   +-------+       +-------+

RIGHT JOIN: All of B plus intersection
```

#### Visual Table Example

**RIGHT JOIN Result:**
```sql
SELECT e.employee_id, e.first_name, d.department_name
FROM employees e
RIGHT JOIN departments d ON e.department_id = d.department_id;
```

| employee_id | first_name | department_name |
|-------------|------------|-----------------|
| 1           | John       | Engineering     |
| 2           | Sarah      | Engineering     |
| 3           | Michael    | Sales           |
| NULL        | NULL       | Marketing       |
| NULL        | NULL       | HR              |

**Step-by-step execution:**
1. Take department_id=1 (Engineering) - Matches John, Sarah - **Include both rows**
2. Take department_id=2 (Sales) - Matches Michael - **Include row**
3. Take department_id=3 (Marketing) - No employee match - **employee columns = NULL**
4. Take department_id=5 (HR) - No employee match - **employee columns = NULL**

---

### FULL OUTER JOIN

#### WHY
Use FULL OUTER JOIN when you need a complete picture of both tables, including all matched and unmatched records from both sides. This is useful for data reconciliation and finding discrepancies.

#### WHAT
A FULL OUTER JOIN returns all rows when there is a match in either the left or right table. It combines the results of both LEFT and RIGHT joins.

#### HOW
```sql
SELECT columns
FROM table1
FULL OUTER JOIN table2
ON table1.column = table2.column;
```

#### WHEN
- Data reconciliation between systems
- Finding orphan records in both tables
- Comprehensive reporting
- Comparing two datasets

#### Venn Diagram
```
    Table A         Table B
   +-------+       +-------+
  /  ###   \     /   ###  \
 /   ###    \   /    ###   \
|    ###     | |     ###    |
|    ###-----###-----###    |
|    ###     ###     ###    |
|    ###   RESULT    ###    |
|    ###-----###-----###    |
|    ###     | |     ###    |
 \   ###    /   \    ###   /
  \  ###   /     \   ###  /
   +-------+       +-------+

FULL OUTER JOIN: All of A and all of B
```

#### Visual Table Example

**FULL OUTER JOIN Result:**
```sql
SELECT e.employee_id, e.first_name, d.department_id, d.department_name
FROM employees e
FULL OUTER JOIN departments d ON e.department_id = d.department_id;
```

| employee_id | first_name | department_id | department_name |
|-------------|------------|---------------|-----------------|
| 1           | John       | 1             | Engineering     |
| 2           | Sarah      | 1             | Engineering     |
| 3           | Michael    | 2             | Sales           |
| 6           | Lisa       | NULL          | NULL            |
| 7           | James      | NULL          | NULL            |
| NULL        | NULL       | 3             | Marketing       |
| NULL        | NULL       | 5             | HR              |

**Step-by-step execution:**
1. Match all records where department_id matches - **3 rows**
2. Add unmatched employees (Lisa, James) with NULL department info - **2 rows**
3. Add unmatched departments (Marketing, HR) with NULL employee info - **2 rows**
4. Total: **7 rows**

---

### CROSS JOIN

#### WHY
Use CROSS JOIN when you need to generate all possible combinations between two tables. This is useful for creating test data, generating combinations, or mathematical operations.

#### WHAT
A CROSS JOIN returns the Cartesian product of both tables - every row from the first table is paired with every row from the second table. No join condition is required.

#### HOW
```sql
SELECT columns
FROM table1
CROSS JOIN table2;

-- Alternative syntax
SELECT columns
FROM table1, table2;
```

#### WHEN
- Generating all possible combinations
- Creating test data sets
- Matrix-like operations
- Pairing every item with every other item

#### Venn Diagram
```
    Table A         Table B
   +-------+       +-------+
  /  ###   \     /   ###  \
 /   ###    \   /    ###   \
|    ###     | |     ###    |
|    ###  x  ###  =  ###    |  MULTIPLY!
|    ###     ###     ###    |
|    ###   A x B     ###    |
|    ###     ###     ###    |
|    ###     | |     ###    |
 \   ###    /   \    ###   /
  \  ###   /     \   ###  /
   +-------+       +-------+

CROSS JOIN: Every row in A paired with every row in B
Result rows = (rows in A) x (rows in B)
```

#### Visual Table Example

**Simple CROSS JOIN:**

**colors table:**
| color |
|-------|
| Red   |
| Blue  |

**sizes table:**
| size   |
|--------|
| Small  |
| Medium |
| Large  |

**CROSS JOIN Result:**
```sql
SELECT c.color, s.size
FROM colors c
CROSS JOIN sizes s;
```

| color | size   |
|-------|--------|
| Red   | Small  |
| Red   | Medium |
| Red   | Large  |
| Blue  | Small  |
| Blue  | Medium |
| Blue  | Large  |

**Result: 2 colors x 3 sizes = 6 rows**

---

### SELF JOIN

#### WHY
Use SELF JOIN when you need to compare rows within the same table or work with hierarchical data. This is essential for organizational structures, bill of materials, or finding related records in the same table.

#### WHAT
A SELF JOIN joins a table to itself. The table appears twice in the query with different aliases. You can use any join type (INNER, LEFT, etc.) for a self join.

#### HOW
```sql
SELECT columns
FROM table1 alias1
JOIN table1 alias2
ON alias1.column = alias2.column;
```

#### WHEN
- Hierarchical relationships (employee-manager)
- Finding duplicates
- Comparing rows in the same table
- Bill of materials / part assemblies

#### Venn Diagram
```
         Table (as A)        Table (as B)
        +----------+        +----------+
       /            \      /            \
      /   Employee   \    /   Manager    \
     |                |  |                |
     |    +----------###---------+        |
     |    |          ###         |        |
     |    |     manager_id       |        |
     |    |    = employee_id     |        |
     |    +----------###---------+        |
     |                |  |                |
      \              /    \              /
       \            /      \            /
        +----------+        +----------+

SELF JOIN: Same table joined to itself with aliases
```

#### Visual Table Example

**employees table (hierarchical):**
| employee_id | first_name | manager_id |
|-------------|------------|------------|
| 1           | John       | NULL       |
| 2           | Sarah      | 1          |
| 3           | Michael    | 1          |
| 4           | Emily      | 3          |
| 5           | David      | 1          |
| 6           | Lisa       | 3          |

**SELF JOIN Result (Employee with Manager):**
```sql
SELECT
    e.employee_id,
    e.first_name AS employee_name,
    m.first_name AS manager_name
FROM employees e
LEFT JOIN employees m ON e.manager_id = m.employee_id;
```

| employee_id | employee_name | manager_name |
|-------------|---------------|--------------|
| 1           | John          | NULL         |
| 2           | Sarah         | John         |
| 3           | Michael       | John         |
| 4           | Emily         | Michael      |
| 5           | David         | John         |
| 6           | Lisa          | Michael      |

**Step-by-step execution:**
1. Employee John (manager_id=NULL) - No match - **manager_name = NULL**
2. Employee Sarah (manager_id=1) - Match employee_id=1 - **manager_name = 'John'**
3. Employee Michael (manager_id=1) - Match employee_id=1 - **manager_name = 'John'**
4. Employee Emily (manager_id=3) - Match employee_id=3 - **manager_name = 'Michael'**
5. Employee David (manager_id=1) - Match employee_id=1 - **manager_name = 'John'**
6. Employee Lisa (manager_id=3) - Match employee_id=3 - **manager_name = 'Michael'**

---

## Code Examples

### Example 1: Basic Two-Table INNER JOIN

**Scenario:** Get all employees with their department names.

```sql
SELECT
    e.employee_id,
    e.first_name,
    e.last_name,
    d.department_name,
    d.location
FROM employees e
INNER JOIN departments d
    ON e.department_id = d.department_id
ORDER BY e.last_name;
```

**Result:**
| employee_id | first_name | last_name | department_name | location   |
|-------------|------------|-----------|-----------------|------------|
| 3           | Michael    | Brown     | Sales           | Building B |
| 4           | Emily      | Davis     | Sales           | Building B |
| 2           | Sarah      | Johnson   | Engineering     | Building A |
| 1           | John       | Smith     | Engineering     | Building A |
| 5           | David      | Wilson    | Marketing       | Building B |

---

### Example 2: LEFT JOIN to Find Employees Without Departments

**Scenario:** List all employees, highlighting those without an assigned department.

```sql
SELECT
    e.employee_id,
    e.first_name,
    e.last_name,
    COALESCE(d.department_name, 'Unassigned') AS department_name
FROM employees e
LEFT JOIN departments d
    ON e.department_id = d.department_id
ORDER BY d.department_name NULLS LAST;
```

**Result:**
| employee_id | first_name | last_name | department_name |
|-------------|------------|-----------|-----------------|
| 1           | John       | Smith     | Engineering     |
| 2           | Sarah      | Johnson   | Engineering     |
| 5           | David      | Wilson    | Marketing       |
| 3           | Michael    | Brown     | Sales           |
| 4           | Emily      | Davis     | Sales           |
| 6           | Lisa       | Taylor    | Unassigned      |
| 7           | James      | Anderson  | Unassigned      |

---

### Example 3: Find Departments Without Employees

**Scenario:** Identify departments that have no employees assigned.

```sql
SELECT
    d.department_id,
    d.department_name,
    d.budget
FROM departments d
LEFT JOIN employees e
    ON d.department_id = e.department_id
WHERE e.employee_id IS NULL;
```

**Result:**
| department_id | department_name | budget   |
|---------------|-----------------|----------|
| 3             | Marketing       | 250000   |
| 5             | HR              | 150000   |

---

### Example 4: Three-Table Join - Sales with Customer and Employee Details

**Scenario:** Get comprehensive sales report with customer and salesperson information.

```sql
SELECT
    s.sale_id,
    c.customer_name,
    CONCAT(e.first_name, ' ', e.last_name) AS salesperson,
    s.product_name,
    s.sale_amount,
    s.sale_date
FROM sales s
INNER JOIN customers c
    ON s.customer_id = c.customer_id
INNER JOIN employees e
    ON s.employee_id = e.employee_id
ORDER BY s.sale_date DESC;
```

**Result:**
| sale_id | customer_name     | salesperson   | product_name     | sale_amount | sale_date  |
|---------|-------------------|---------------|------------------|-------------|------------|
| 4       | Global Industries | Michael Brown | Software License | 12000       | 2023-03-25 |
| 3       | Acme Corp         | Emily Davis   | Support Package  | 5000        | 2023-03-10 |
| 2       | Tech Solutions    | Michael Brown | Consulting       | 8000        | 2023-02-20 |
| 1       | Acme Corp         | Michael Brown | Software License | 15000       | 2023-01-15 |

---

### Example 5: Multiple Table Join with LEFT JOINs

**Scenario:** Get all sales including those without customer or employee info.

```sql
SELECT
    s.sale_id,
    COALESCE(c.customer_name, 'Walk-in') AS customer_name,
    COALESCE(CONCAT(e.first_name, ' ', e.last_name), 'Online') AS salesperson,
    s.product_name,
    s.sale_amount
FROM sales s
LEFT JOIN customers c
    ON s.customer_id = c.customer_id
LEFT JOIN employees e
    ON s.employee_id = e.employee_id
ORDER BY s.sale_id;
```

**Result:**
| sale_id | customer_name     | salesperson   | product_name     | sale_amount |
|---------|-------------------|---------------|------------------|-------------|
| 1       | Acme Corp         | Michael Brown | Software License | 15000       |
| 2       | Tech Solutions    | Michael Brown | Consulting       | 8000        |
| 3       | Acme Corp         | Emily Davis   | Support Package  | 5000        |
| 4       | Global Industries | Michael Brown | Software License | 12000       |
| 5       | Walk-in           | Emily Davis   | Training         | 3000        |
| 6       | Tech Solutions    | Online        | Hardware         | 7500        |

---

### Example 6: Four-Table Join - Projects with Full Details

**Scenario:** Get project information with department, employee assignments, and roles.

```sql
SELECT
    p.project_name,
    d.department_name,
    CONCAT(e.first_name, ' ', e.last_name) AS employee_name,
    ep.role,
    ep.hours_allocated
FROM projects p
LEFT JOIN departments d
    ON p.department_id = d.department_id
LEFT JOIN employee_projects ep
    ON p.project_id = ep.project_id
LEFT JOIN employees e
    ON ep.employee_id = e.employee_id
ORDER BY p.project_name, ep.role;
```

**Result:**
| project_name     | department_name | employee_name  | role      | hours_allocated |
|------------------|-----------------|----------------|-----------|-----------------|
| CRM Integration  | Sales           | Michael Brown  | Manager   | 20              |
| CRM Integration  | Sales           | Emily Davis    | Analyst   | 35              |
| Data Migration   | NULL            | NULL           | NULL      | NULL            |
| Mobile App       | Engineering     | John Smith     | Architect | 25              |
| Mobile App       | Engineering     | David Wilson   | Designer  | 30              |
| Website Redesign | Engineering     | John Smith     | Lead      | 40              |
| Website Redesign | Engineering     | Sarah Johnson  | Developer | 30              |

---

### Example 7: Self Join - Employee Hierarchy

**Scenario:** Display the complete organizational hierarchy with employee and manager names.

```sql
SELECT
    e.employee_id,
    CONCAT(e.first_name, ' ', e.last_name) AS employee_name,
    e.salary AS employee_salary,
    COALESCE(CONCAT(m.first_name, ' ', m.last_name), 'No Manager') AS manager_name,
    COALESCE(m.salary, 0) AS manager_salary
FROM employees e
LEFT JOIN employees m
    ON e.manager_id = m.employee_id
ORDER BY m.employee_id NULLS FIRST, e.first_name;
```

**Result:**
| employee_id | employee_name   | employee_salary | manager_name | manager_salary |
|-------------|-----------------|-----------------|--------------|----------------|
| 7           | James Anderson  | 90000           | No Manager   | 0              |
| 1           | John Smith      | 75000           | No Manager   | 0              |
| 5           | David Wilson    | 70000           | John Smith   | 75000          |
| 3           | Michael Brown   | 80000           | John Smith   | 75000          |
| 2           | Sarah Johnson   | 65000           | John Smith   | 75000          |
| 4           | Emily Davis     | 55000           | Michael Brown| 80000          |
| 6           | Lisa Taylor     | 50000           | Michael Brown| 80000          |

---

### Example 8: Self Join - Find Employees with Same Manager

**Scenario:** Find pairs of employees who share the same manager (colleagues).

```sql
SELECT
    e1.first_name AS employee1,
    e2.first_name AS employee2,
    m.first_name AS shared_manager
FROM employees e1
INNER JOIN employees e2
    ON e1.manager_id = e2.manager_id
    AND e1.employee_id < e2.employee_id  -- Avoid duplicates and self-pairs
INNER JOIN employees m
    ON e1.manager_id = m.employee_id
ORDER BY m.first_name, e1.first_name;
```

**Result:**
| employee1 | employee2 | shared_manager |
|-----------|-----------|----------------|
| David     | Michael   | John           |
| David     | Sarah     | John           |
| Michael   | Sarah     | John           |
| Emily     | Lisa      | Michael        |

---

### Example 9: Join with Aggregates - Department Sales Summary

**Scenario:** Calculate total sales per department with employee count.

```sql
SELECT
    d.department_name,
    COUNT(DISTINCT e.employee_id) AS employee_count,
    COUNT(s.sale_id) AS total_sales,
    COALESCE(SUM(s.sale_amount), 0) AS total_revenue,
    ROUND(COALESCE(AVG(s.sale_amount), 0), 2) AS avg_sale_amount
FROM departments d
LEFT JOIN employees e
    ON d.department_id = e.department_id
LEFT JOIN sales s
    ON e.employee_id = s.employee_id
GROUP BY d.department_id, d.department_name
ORDER BY total_revenue DESC;
```

**Result:**
| department_name | employee_count | total_sales | total_revenue | avg_sale_amount |
|-----------------|----------------|-------------|---------------|-----------------|
| Sales           | 2              | 5           | 43000         | 8600.00         |
| Engineering     | 2              | 0           | 0             | 0.00            |
| Marketing       | 0              | 0           | 0             | 0.00            |
| HR              | 0              | 0           | 0             | 0.00            |

---

### Example 10: Join with Aggregates - Customer Purchase Analysis

**Scenario:** Analyze customer purchasing patterns with total and average spend.

```sql
SELECT
    c.customer_name,
    c.city,
    COUNT(s.sale_id) AS purchase_count,
    SUM(s.sale_amount) AS total_spent,
    ROUND(AVG(s.sale_amount), 2) AS avg_purchase,
    MIN(s.sale_date) AS first_purchase,
    MAX(s.sale_date) AS last_purchase
FROM customers c
LEFT JOIN sales s
    ON c.customer_id = s.customer_id
GROUP BY c.customer_id, c.customer_name, c.city
HAVING COUNT(s.sale_id) > 0
ORDER BY total_spent DESC;
```

**Result:**
| customer_name     | city          | purchase_count | total_spent | avg_purchase | first_purchase | last_purchase |
|-------------------|---------------|----------------|-------------|--------------|----------------|---------------|
| Acme Corp         | New York      | 2              | 20000       | 10000.00     | 2023-01-15     | 2023-03-10    |
| Tech Solutions    | San Francisco | 2              | 15500       | 7750.00      | 2023-02-20     | 2023-04-15    |
| Global Industries | Chicago       | 1              | 12000       | 12000.00     | 2023-03-25     | 2023-03-25    |

---

### Example 11: CROSS JOIN - Generate All Possible Assignments

**Scenario:** Generate all possible employee-project assignment combinations.

```sql
SELECT
    CONCAT(e.first_name, ' ', e.last_name) AS employee_name,
    p.project_name,
    'Potential Assignment' AS status
FROM employees e
CROSS JOIN projects p
WHERE e.department_id = 1  -- Engineering employees only
ORDER BY e.last_name, p.project_name;
```

**Result:**
| employee_name | project_name     | status               |
|---------------|------------------|----------------------|
| Sarah Johnson | CRM Integration  | Potential Assignment |
| Sarah Johnson | Data Migration   | Potential Assignment |
| Sarah Johnson | Mobile App       | Potential Assignment |
| Sarah Johnson | Website Redesign | Potential Assignment |
| John Smith    | CRM Integration  | Potential Assignment |
| John Smith    | Data Migration   | Potential Assignment |
| John Smith    | Mobile App       | Potential Assignment |
| John Smith    | Website Redesign | Potential Assignment |

---

### Example 12: Complex Join - Employee Performance Report

**Scenario:** Create a comprehensive employee performance report with sales metrics.

```sql
SELECT
    CONCAT(e.first_name, ' ', e.last_name) AS employee_name,
    d.department_name,
    CONCAT(m.first_name, ' ', m.last_name) AS reports_to,
    COUNT(DISTINCT s.sale_id) AS sales_count,
    COALESCE(SUM(s.sale_amount), 0) AS total_sales,
    COUNT(DISTINCT ep.project_id) AS projects_assigned,
    COALESCE(SUM(ep.hours_allocated), 0) AS total_hours
FROM employees e
LEFT JOIN departments d
    ON e.department_id = d.department_id
LEFT JOIN employees m
    ON e.manager_id = m.employee_id
LEFT JOIN sales s
    ON e.employee_id = s.employee_id
LEFT JOIN employee_projects ep
    ON e.employee_id = ep.employee_id
GROUP BY e.employee_id, e.first_name, e.last_name,
         d.department_name, m.first_name, m.last_name
ORDER BY total_sales DESC;
```

**Result:**
| employee_name  | department_name | reports_to    | sales_count | total_sales | projects_assigned | total_hours |
|----------------|-----------------|---------------|-------------|-------------|-------------------|-------------|
| Michael Brown  | Sales           | John Smith    | 3           | 35000       | 1                 | 20          |
| Emily Davis    | Sales           | Michael Brown | 2           | 8000        | 1                 | 35          |
| John Smith     | Engineering     | NULL          | 0           | 0           | 2                 | 65          |
| David Wilson   | Marketing       | John Smith    | 0           | 0           | 1                 | 30          |
| Sarah Johnson  | Engineering     | John Smith    | 0           | 0           | 1                 | 30          |
| Lisa Taylor    | NULL            | Michael Brown | 0           | 0           | 0                 | 0           |
| James Anderson | NULL            | NULL          | 0           | 0           | 0                 | 0           |

---

### Example 13: Finding Unmatched Records with FULL OUTER JOIN

**Scenario:** Identify all data integrity issues - orphan employees and empty departments.

```sql
SELECT
    CASE
        WHEN e.employee_id IS NULL THEN 'Empty Department'
        WHEN d.department_id IS NULL THEN 'Orphan Employee'
        ELSE 'Matched'
    END AS status,
    e.employee_id,
    e.first_name,
    d.department_id,
    d.department_name
FROM employees e
FULL OUTER JOIN departments d
    ON e.department_id = d.department_id
WHERE e.employee_id IS NULL
   OR d.department_id IS NULL
   OR e.department_id IS NULL
ORDER BY status;
```

**Result:**
| status           | employee_id | first_name | department_id | department_name |
|------------------|-------------|------------|---------------|-----------------|
| Empty Department | NULL        | NULL       | 3             | Marketing       |
| Empty Department | NULL        | NULL       | 5             | HR              |
| Orphan Employee  | 6           | Lisa       | NULL          | NULL            |
| Orphan Employee  | 7           | James      | NULL          | NULL            |

---

### Example 14: Multiple Self Joins - Three-Level Hierarchy

**Scenario:** Display three levels of management hierarchy.

```sql
SELECT
    e.first_name AS employee,
    m1.first_name AS direct_manager,
    m2.first_name AS senior_manager
FROM employees e
LEFT JOIN employees m1
    ON e.manager_id = m1.employee_id
LEFT JOIN employees m2
    ON m1.manager_id = m2.employee_id
ORDER BY m2.first_name NULLS FIRST, m1.first_name NULLS FIRST;
```

**Result:**
| employee | direct_manager | senior_manager |
|----------|----------------|----------------|
| John     | NULL           | NULL           |
| James    | NULL           | NULL           |
| Sarah    | John           | NULL           |
| Michael  | John           | NULL           |
| David    | John           | NULL           |
| Emily    | Michael        | John           |
| Lisa     | Michael        | John           |

---

### Example 15: Real-World Scenario - Invoice Generation

**Scenario:** Generate invoice data combining multiple related tables.

```sql
SELECT
    'INV-' || LPAD(s.sale_id::TEXT, 5, '0') AS invoice_number,
    s.sale_date AS invoice_date,
    c.customer_name,
    c.email AS customer_email,
    c.city AS customer_city,
    s.product_name,
    s.sale_amount,
    CONCAT(e.first_name, ' ', e.last_name) AS processed_by,
    d.department_name AS department
FROM sales s
INNER JOIN customers c
    ON s.customer_id = c.customer_id
INNER JOIN employees e
    ON s.employee_id = e.employee_id
INNER JOIN departments d
    ON e.department_id = d.department_id
ORDER BY s.sale_date;
```

**Result:**
| invoice_number | invoice_date | customer_name     | customer_email    | customer_city | product_name     | sale_amount | processed_by  | department |
|----------------|--------------|-------------------|-------------------|---------------|------------------|-------------|---------------|------------|
| INV-00001      | 2023-01-15   | Acme Corp         | acme@email.com    | New York      | Software License | 15000       | Michael Brown | Sales      |
| INV-00002      | 2023-02-20   | Tech Solutions    | tech@email.com    | San Francisco | Consulting       | 8000        | Michael Brown | Sales      |
| INV-00003      | 2023-03-10   | Acme Corp         | acme@email.com    | New York      | Support Package  | 5000        | Emily Davis   | Sales      |
| INV-00004      | 2023-03-25   | Global Industries | global@email.com  | Chicago       | Software License | 12000       | Michael Brown | Sales      |

---

## Practice Questions

### Question 1: Employee Department Analysis (Medium)

**Task:** Write a query to find all employees who earn more than the average salary of their department. Include the employee name, department name, their salary, and the department's average salary.

<details>
<summary>Solution</summary>

```sql
WITH dept_avg AS (
    SELECT
        department_id,
        AVG(salary) AS avg_salary
    FROM employees
    WHERE department_id IS NOT NULL
    GROUP BY department_id
)
SELECT
    CONCAT(e.first_name, ' ', e.last_name) AS employee_name,
    d.department_name,
    e.salary,
    ROUND(da.avg_salary, 2) AS dept_avg_salary,
    ROUND(e.salary - da.avg_salary, 2) AS difference
FROM employees e
INNER JOIN departments d
    ON e.department_id = d.department_id
INNER JOIN dept_avg da
    ON e.department_id = da.department_id
WHERE e.salary > da.avg_salary
ORDER BY difference DESC;
```

**Expected Result:**
| employee_name | department_name | salary | dept_avg_salary | difference |
|---------------|-----------------|--------|-----------------|------------|
| Michael Brown | Sales           | 80000  | 67500.00        | 12500.00   |
| John Smith    | Engineering     | 75000  | 70000.00        | 5000.00    |

</details>

---

### Question 2: Manager Team Size (Medium)

**Task:** Find all managers and the number of employees who report directly to them. Include managers who have no direct reports.

<details>
<summary>Solution</summary>

```sql
SELECT
    CONCAT(m.first_name, ' ', m.last_name) AS manager_name,
    m.salary AS manager_salary,
    COUNT(e.employee_id) AS direct_reports,
    COALESCE(SUM(e.salary), 0) AS team_total_salary
FROM employees m
LEFT JOIN employees e
    ON m.employee_id = e.manager_id
GROUP BY m.employee_id, m.first_name, m.last_name, m.salary
HAVING COUNT(e.employee_id) > 0
    OR m.employee_id IN (SELECT DISTINCT manager_id FROM employees WHERE manager_id IS NOT NULL)
ORDER BY direct_reports DESC;
```

**Expected Result:**
| manager_name  | manager_salary | direct_reports | team_total_salary |
|---------------|----------------|----------------|-------------------|
| John Smith    | 75000          | 3              | 215000            |
| Michael Brown | 80000          | 2              | 105000            |

</details>

---

### Question 3: Customer Sales Gap Analysis (Medium-Hard)

**Task:** Find customers who have made purchases but haven't purchased anything in the last 60 days (from the most recent sale date in the database). Show their last purchase date and total historical spend.

<details>
<summary>Solution</summary>

```sql
WITH max_date AS (
    SELECT MAX(sale_date) AS latest_date FROM sales
),
customer_stats AS (
    SELECT
        c.customer_id,
        c.customer_name,
        c.city,
        MAX(s.sale_date) AS last_purchase,
        COUNT(s.sale_id) AS total_purchases,
        SUM(s.sale_amount) AS total_spent
    FROM customers c
    INNER JOIN sales s
        ON c.customer_id = s.customer_id
    GROUP BY c.customer_id, c.customer_name, c.city
)
SELECT
    cs.customer_name,
    cs.city,
    cs.last_purchase,
    cs.total_purchases,
    cs.total_spent,
    md.latest_date - cs.last_purchase AS days_since_purchase
FROM customer_stats cs
CROSS JOIN max_date md
WHERE cs.last_purchase < md.latest_date - INTERVAL '60 days'
ORDER BY cs.last_purchase;
```

**Expected Result:**
| customer_name | city     | last_purchase | total_purchases | total_spent | days_since_purchase |
|---------------|----------|---------------|-----------------|-------------|---------------------|
| Acme Corp     | New York | 2023-03-10    | 2               | 20000       | 36                  |

</details>

---

### Question 4: Project Resource Allocation (Medium-Hard)

**Task:** For each project, calculate the total hours allocated, number of team members, and list all team members with their roles. Show projects even if they have no team members assigned.

<details>
<summary>Solution</summary>

```sql
SELECT
    p.project_name,
    p.status,
    COALESCE(d.department_name, 'Unassigned') AS department,
    COUNT(DISTINCT ep.employee_id) AS team_size,
    COALESCE(SUM(ep.hours_allocated), 0) AS total_hours,
    STRING_AGG(
        CONCAT(e.first_name, ' (', ep.role, ')'),
        ', '
        ORDER BY ep.role
    ) AS team_members
FROM projects p
LEFT JOIN departments d
    ON p.department_id = d.department_id
LEFT JOIN employee_projects ep
    ON p.project_id = ep.project_id
LEFT JOIN employees e
    ON ep.employee_id = e.employee_id
GROUP BY p.project_id, p.project_name, p.status, d.department_name
ORDER BY total_hours DESC;
```

**Expected Result:**
| project_name     | status    | department  | team_size | total_hours | team_members                              |
|------------------|-----------|-------------|-----------|-------------|-------------------------------------------|
| Website Redesign | Active    | Engineering | 2         | 70          | Sarah (Developer), John (Lead)            |
| Mobile App       | Active    | Engineering | 2         | 55          | John (Architect), David (Designer)        |
| CRM Integration  | Completed | Sales       | 2         | 55          | Emily (Analyst), Michael (Manager)        |
| Data Migration   | Active    | Unassigned  | 0         | 0           | NULL                                      |

</details>

---

### Question 5: Multi-Level Reporting Chain (Hard)

**Task:** Find all employees who are exactly 2 levels below a top-level manager (an employee with no manager). Show the employee, their manager, and the top-level manager.

<details>
<summary>Solution</summary>

```sql
SELECT
    CONCAT(e.first_name, ' ', e.last_name) AS employee_name,
    e.salary AS employee_salary,
    CONCAT(m1.first_name, ' ', m1.last_name) AS direct_manager,
    CONCAT(m2.first_name, ' ', m2.last_name) AS top_manager
FROM employees e
INNER JOIN employees m1
    ON e.manager_id = m1.employee_id
INNER JOIN employees m2
    ON m1.manager_id = m2.employee_id
WHERE m2.manager_id IS NULL
ORDER BY top_manager, direct_manager, employee_name;
```

**Expected Result:**
| employee_name | employee_salary | direct_manager | top_manager |
|---------------|-----------------|----------------|-------------|
| Emily Davis   | 55000           | Michael Brown  | John Smith  |
| Lisa Taylor   | 50000           | Michael Brown  | John Smith  |

</details>

---

### Question 6: Sales Performance Comparison (Medium-Hard)

**Task:** Compare each salesperson's performance against their department's average. Show the employee, their total sales, department average, and performance rating (Above/Below/Average).

<details>
<summary>Solution</summary>

```sql
WITH employee_sales AS (
    SELECT
        e.employee_id,
        e.first_name,
        e.last_name,
        e.department_id,
        COUNT(s.sale_id) AS num_sales,
        COALESCE(SUM(s.sale_amount), 0) AS total_sales
    FROM employees e
    LEFT JOIN sales s
        ON e.employee_id = s.employee_id
    GROUP BY e.employee_id, e.first_name, e.last_name, e.department_id
),
dept_avg AS (
    SELECT
        department_id,
        AVG(total_sales) AS avg_sales
    FROM employee_sales
    WHERE department_id IS NOT NULL
    GROUP BY department_id
)
SELECT
    CONCAT(es.first_name, ' ', es.last_name) AS employee_name,
    d.department_name,
    es.num_sales,
    es.total_sales,
    ROUND(da.avg_sales, 2) AS dept_avg_sales,
    CASE
        WHEN es.total_sales > da.avg_sales * 1.1 THEN 'Above Average'
        WHEN es.total_sales < da.avg_sales * 0.9 THEN 'Below Average'
        ELSE 'Average'
    END AS performance_rating
FROM employee_sales es
INNER JOIN departments d
    ON es.department_id = d.department_id
INNER JOIN dept_avg da
    ON es.department_id = da.department_id
WHERE es.total_sales > 0 OR es.department_id IS NOT NULL
ORDER BY es.total_sales DESC;
```

**Expected Result:**
| employee_name | department_name | num_sales | total_sales | dept_avg_sales | performance_rating |
|---------------|-----------------|-----------|-------------|----------------|--------------------|
| Michael Brown | Sales           | 3         | 35000       | 21500.00       | Above Average      |
| Emily Davis   | Sales           | 2         | 8000        | 21500.00       | Below Average      |
| John Smith    | Engineering     | 0         | 0           | 0.00           | Average            |
| Sarah Johnson | Engineering     | 0         | 0           | 0.00           | Average            |

</details>

---

### Question 7: Orphan Records Detection (Medium)

**Task:** Write a comprehensive query to find all orphan records across the database - employees without valid departments, sales without valid customers or employees, and projects without valid departments.

<details>
<summary>Solution</summary>

```sql
-- Employees without valid departments
SELECT
    'Employee without department' AS issue_type,
    'employees' AS table_name,
    employee_id::TEXT AS record_id,
    CONCAT(first_name, ' ', last_name) AS description
FROM employees e
WHERE department_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM departments d WHERE d.department_id = e.department_id
  )

UNION ALL

-- Sales without valid customers
SELECT
    'Sale without customer' AS issue_type,
    'sales' AS table_name,
    sale_id::TEXT AS record_id,
    CONCAT('Sale #', sale_id, ': ', product_name) AS description
FROM sales s
WHERE customer_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM customers c WHERE c.customer_id = s.customer_id
  )

UNION ALL

-- Sales without valid employees
SELECT
    'Sale without employee' AS issue_type,
    'sales' AS table_name,
    sale_id::TEXT AS record_id,
    CONCAT('Sale #', sale_id, ': ', product_name) AS description
FROM sales s
WHERE employee_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM employees e WHERE e.employee_id = s.employee_id
  )

UNION ALL

-- Projects without valid departments
SELECT
    'Project without department' AS issue_type,
    'projects' AS table_name,
    project_id::TEXT AS record_id,
    project_name AS description
FROM projects p
WHERE department_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM departments d WHERE d.department_id = p.department_id
  )

ORDER BY issue_type, table_name;
```

**Expected Result:**
| issue_type                    | table_name | record_id | description                    |
|-------------------------------|------------|-----------|--------------------------------|
| Employee without department   | employees  | 7         | James Anderson                 |

</details>

---

### Question 8: Cross-Department Collaboration (Hard)

**Task:** Find all pairs of employees from different departments who are working on the same project. Show the project name and both employees with their departments.

<details>
<summary>Solution</summary>

```sql
SELECT DISTINCT
    p.project_name,
    CONCAT(e1.first_name, ' ', e1.last_name) AS employee1,
    d1.department_name AS dept1,
    CONCAT(e2.first_name, ' ', e2.last_name) AS employee2,
    d2.department_name AS dept2
FROM employee_projects ep1
INNER JOIN employee_projects ep2
    ON ep1.project_id = ep2.project_id
    AND ep1.employee_id < ep2.employee_id
INNER JOIN employees e1
    ON ep1.employee_id = e1.employee_id
INNER JOIN employees e2
    ON ep2.employee_id = e2.employee_id
INNER JOIN projects p
    ON ep1.project_id = p.project_id
LEFT JOIN departments d1
    ON e1.department_id = d1.department_id
LEFT JOIN departments d2
    ON e2.department_id = d2.department_id
WHERE e1.department_id != e2.department_id
   OR e1.department_id IS NULL
   OR e2.department_id IS NULL
ORDER BY p.project_name;
```

**Expected Result:**
| project_name | employee1   | dept1       | employee2    | dept2     |
|--------------|-------------|-------------|--------------|-----------|
| Mobile App   | John Smith  | Engineering | David Wilson | Marketing |

</details>

---

### Question 9: Revenue Attribution Analysis (Hard)

**Task:** Calculate revenue attribution showing what percentage of total company revenue each employee is responsible for, including employees with no sales.

<details>
<summary>Solution</summary>

```sql
WITH total_revenue AS (
    SELECT SUM(sale_amount) AS company_total FROM sales
),
employee_revenue AS (
    SELECT
        e.employee_id,
        e.first_name,
        e.last_name,
        d.department_name,
        COUNT(s.sale_id) AS sales_count,
        COALESCE(SUM(s.sale_amount), 0) AS employee_total
    FROM employees e
    LEFT JOIN departments d
        ON e.department_id = d.department_id
    LEFT JOIN sales s
        ON e.employee_id = s.employee_id
    GROUP BY e.employee_id, e.first_name, e.last_name, d.department_name
)
SELECT
    CONCAT(er.first_name, ' ', er.last_name) AS employee_name,
    COALESCE(er.department_name, 'Unassigned') AS department,
    er.sales_count,
    er.employee_total AS revenue,
    tr.company_total AS total_revenue,
    ROUND(
        CASE
            WHEN tr.company_total = 0 THEN 0
            ELSE (er.employee_total / tr.company_total * 100)
        END, 2
    ) AS revenue_percentage
FROM employee_revenue er
CROSS JOIN total_revenue tr
ORDER BY revenue_percentage DESC, er.first_name;
```

**Expected Result:**
| employee_name  | department  | sales_count | revenue | total_revenue | revenue_percentage |
|----------------|-------------|-------------|---------|---------------|--------------------|
| Michael Brown  | Sales       | 3           | 35000   | 50500         | 69.31              |
| Emily Davis    | Sales       | 2           | 8000    | 50500         | 15.84              |
| David Wilson   | Marketing   | 0           | 0       | 50500         | 0.00               |
| James Anderson | Unassigned  | 0           | 0       | 50500         | 0.00               |
| John Smith     | Engineering | 0           | 0       | 50500         | 0.00               |
| Lisa Taylor    | Unassigned  | 0           | 0       | 50500         | 0.00               |
| Sarah Johnson  | Engineering | 0           | 0       | 50500         | 0.00               |

</details>

---

### Question 10: Comprehensive Business Report (Hard)

**Task:** Create a comprehensive department dashboard showing: department name, employee count, average salary, total projects, active projects, total sales revenue, and top performer in each department.

<details>
<summary>Solution</summary>

```sql
WITH dept_employees AS (
    SELECT
        d.department_id,
        d.department_name,
        COUNT(e.employee_id) AS employee_count,
        ROUND(AVG(e.salary), 2) AS avg_salary
    FROM departments d
    LEFT JOIN employees e
        ON d.department_id = e.department_id
    GROUP BY d.department_id, d.department_name
),
dept_projects AS (
    SELECT
        d.department_id,
        COUNT(p.project_id) AS total_projects,
        SUM(CASE WHEN p.status = 'Active' THEN 1 ELSE 0 END) AS active_projects
    FROM departments d
    LEFT JOIN projects p
        ON d.department_id = p.department_id
    GROUP BY d.department_id
),
dept_sales AS (
    SELECT
        e.department_id,
        COALESCE(SUM(s.sale_amount), 0) AS total_revenue
    FROM employees e
    LEFT JOIN sales s
        ON e.employee_id = s.employee_id
    WHERE e.department_id IS NOT NULL
    GROUP BY e.department_id
),
top_performers AS (
    SELECT DISTINCT ON (e.department_id)
        e.department_id,
        CONCAT(e.first_name, ' ', e.last_name) AS top_performer,
        COALESCE(SUM(s.sale_amount), 0) AS performer_sales
    FROM employees e
    LEFT JOIN sales s
        ON e.employee_id = s.employee_id
    WHERE e.department_id IS NOT NULL
    GROUP BY e.department_id, e.employee_id, e.first_name, e.last_name
    ORDER BY e.department_id, performer_sales DESC
)
SELECT
    de.department_name,
    de.employee_count,
    COALESCE(de.avg_salary, 0) AS avg_salary,
    COALESCE(dp.total_projects, 0) AS total_projects,
    COALESCE(dp.active_projects, 0) AS active_projects,
    COALESCE(ds.total_revenue, 0) AS total_revenue,
    COALESCE(tp.top_performer, 'N/A') AS top_performer,
    COALESCE(tp.performer_sales, 0) AS top_performer_sales
FROM dept_employees de
LEFT JOIN dept_projects dp
    ON de.department_id = dp.department_id
LEFT JOIN dept_sales ds
    ON de.department_id = ds.department_id
LEFT JOIN top_performers tp
    ON de.department_id = tp.department_id
ORDER BY total_revenue DESC;
```

**Expected Result:**
| department_name | employee_count | avg_salary | total_projects | active_projects | total_revenue | top_performer | top_performer_sales |
|-----------------|----------------|------------|----------------|-----------------|---------------|---------------|---------------------|
| Sales           | 2              | 67500.00   | 1              | 0               | 43000         | Michael Brown | 35000               |
| Engineering     | 2              | 70000.00   | 2              | 2               | 0             | John Smith    | 0                   |
| Marketing       | 1              | 70000.00   | 0              | 0               | 0             | David Wilson  | 0                   |
| HR              | 0              | 0          | 0              | 0               | 0             | N/A           | 0                   |

</details>

---

## Common Mistakes

### 1. Join Condition Errors

**Mistake:** Using the wrong columns or operators in join conditions.

```sql
-- WRONG: Comparing wrong columns
SELECT e.first_name, d.department_name
FROM employees e
JOIN departments d ON e.employee_id = d.department_id;  -- Wrong!

-- CORRECT: Matching the relationship columns
SELECT e.first_name, d.department_name
FROM employees e
JOIN departments d ON e.department_id = d.department_id;  -- Correct!
```

**Why it matters:** This produces incorrect results or no results at all, with no error message to alert you.

---

### 2. Cartesian Products (Accidental CROSS JOIN)

**Mistake:** Forgetting the ON clause or using comma syntax without WHERE.

```sql
-- WRONG: Missing ON clause creates Cartesian product
SELECT e.first_name, d.department_name
FROM employees e
JOIN departments d;  -- Creates all combinations!

-- WRONG: Old comma syntax without WHERE
SELECT e.first_name, d.department_name
FROM employees e, departments d;  -- Creates all combinations!

-- CORRECT: Always include ON clause
SELECT e.first_name, d.department_name
FROM employees e
JOIN departments d ON e.department_id = d.department_id;
```

**Result of mistake:** If you have 7 employees and 4 departments, you get 28 rows instead of the correct result.

---

### 3. Missing ON Clause in Multiple Joins

**Mistake:** Forgetting to specify join conditions when joining multiple tables.

```sql
-- WRONG: Second join missing ON clause
SELECT s.sale_id, c.customer_name, e.first_name
FROM sales s
JOIN customers c ON s.customer_id = c.customer_id
JOIN employees e;  -- Missing ON clause!

-- CORRECT: All joins have conditions
SELECT s.sale_id, c.customer_name, e.first_name
FROM sales s
JOIN customers c ON s.customer_id = c.customer_id
JOIN employees e ON s.employee_id = e.employee_id;
```

---

### 4. Ambiguous Column Names

**Mistake:** Not qualifying columns that exist in multiple tables.

```sql
-- WRONG: Ambiguous column reference
SELECT employee_id, first_name, department_id, department_name
FROM employees
JOIN departments ON employees.department_id = departments.department_id;
-- Error: column "department_id" is ambiguous

-- CORRECT: Use table aliases and qualify columns
SELECT
    e.employee_id,
    e.first_name,
    e.department_id,  -- or d.department_id
    d.department_name
FROM employees e
JOIN departments d ON e.department_id = d.department_id;
```

**Best Practice:** Always use table aliases and qualify all columns in JOINs.

---

### 5. LEFT vs INNER JOIN Confusion

**Mistake:** Using INNER JOIN when you need all records from the left table.

```sql
-- WRONG: INNER JOIN excludes employees without departments
SELECT e.first_name, d.department_name
FROM employees e
INNER JOIN departments d ON e.department_id = d.department_id;
-- Lisa and James are excluded!

-- CORRECT: LEFT JOIN includes all employees
SELECT e.first_name, COALESCE(d.department_name, 'Unassigned')
FROM employees e
LEFT JOIN departments d ON e.department_id = d.department_id;
-- All employees included
```

**Rule of thumb:**
- Use INNER JOIN when you only want matching records
- Use LEFT JOIN when you need all records from the primary table

---

### 6. Incorrect NULL Handling

**Mistake:** Using = instead of IS NULL to check for NULLs.

```sql
-- WRONG: NULL cannot be compared with =
SELECT e.first_name
FROM employees e
LEFT JOIN departments d ON e.department_id = d.department_id
WHERE d.department_id = NULL;  -- Returns nothing!

-- CORRECT: Use IS NULL
SELECT e.first_name
FROM employees e
LEFT JOIN departments d ON e.department_id = d.department_id
WHERE d.department_id IS NULL;  -- Returns Lisa and James
```

---

### 7. Filtering in ON vs WHERE

**Mistake:** Putting filter conditions in the wrong place with OUTER JOINs.

```sql
-- WRONG: Filter in WHERE nullifies LEFT JOIN
SELECT e.first_name, d.department_name
FROM employees e
LEFT JOIN departments d ON e.department_id = d.department_id
WHERE d.location = 'Building A';
-- This excludes Lisa and James!

-- CORRECT: Filter in ON preserves LEFT JOIN behavior
SELECT e.first_name, d.department_name
FROM employees e
LEFT JOIN departments d
    ON e.department_id = d.department_id
    AND d.location = 'Building A';
-- All employees included, only Building A departments shown
```

---

### 8. Aggregate Functions with Joins

**Mistake:** Forgetting that JOINs can duplicate rows, affecting aggregates.

```sql
-- WRONG: Counts duplicated due to multiple projects per employee
SELECT
    d.department_name,
    SUM(e.salary) AS total_salary  -- Wrong! Salary counted multiple times
FROM departments d
JOIN employees e ON d.department_id = e.department_id
JOIN employee_projects ep ON e.employee_id = ep.employee_id
GROUP BY d.department_name;

-- CORRECT: Use DISTINCT or subquery
SELECT
    d.department_name,
    SUM(DISTINCT e.salary) AS total_salary  -- Or use subquery
FROM departments d
JOIN employees e ON d.department_id = e.department_id
JOIN employee_projects ep ON e.employee_id = ep.employee_id
GROUP BY d.department_name;

-- BETTER: Use subquery to avoid duplication
SELECT
    d.department_name,
    (SELECT SUM(salary) FROM employees WHERE department_id = d.department_id) AS total_salary
FROM departments d;
```

---

### 9. Self-Join Without Proper Aliases

**Mistake:** Not using clear aliases in self-joins.

```sql
-- WRONG: Confusing without descriptive aliases
SELECT a.first_name, b.first_name
FROM employees a
JOIN employees b ON a.manager_id = b.employee_id;
-- Which is the employee? Which is the manager?

-- CORRECT: Use descriptive aliases
SELECT
    emp.first_name AS employee_name,
    mgr.first_name AS manager_name
FROM employees emp
LEFT JOIN employees mgr ON emp.manager_id = mgr.employee_id;
```

---

### 10. Join Order Performance

**Mistake:** Not considering the order of joins for performance.

```sql
-- POTENTIALLY SLOW: Starting with larger table
SELECT *
FROM sales s              -- Large table
JOIN customers c ON s.customer_id = c.customer_id
WHERE c.city = 'New York';

-- BETTER: Filter early, reduce dataset
SELECT *
FROM customers c          -- Filter first
JOIN sales s ON c.customer_id = s.customer_id
WHERE c.city = 'New York';

-- BEST: Use EXISTS for existence checks
SELECT *
FROM sales s
WHERE EXISTS (
    SELECT 1 FROM customers c
    WHERE c.customer_id = s.customer_id
    AND c.city = 'New York'
);
```

---

## Join Execution Flow

Understanding how the database processes joins helps write better queries.

```
                    SQL JOIN EXECUTION FLOW
    ============================================================

    Query:
    SELECT e.name, d.dept_name, s.amount
    FROM employees e
    INNER JOIN departments d ON e.dept_id = d.dept_id
    LEFT JOIN sales s ON e.emp_id = s.emp_id
    WHERE e.salary > 50000
    ORDER BY s.amount DESC;

    ============================================================

    Step 1: FROM Clause Processing
    +------------------+
    |   employees (e)  |
    |   Base Table     |
    +------------------+
           |
           v

    Step 2: First JOIN - INNER JOIN departments
    +------------------+     +------------------+
    |   employees (e)  | --> |  departments (d) |
    +------------------+     +------------------+
           |                          |
           +------------+-------------+
                        |
                        v
              +------------------+
              |  Joined Result   |
              |  (e + d matched) |
              +------------------+
                        |
                        v

    Step 3: Second JOIN - LEFT JOIN sales
    +------------------+     +------------------+
    |  Joined Result   | --> |    sales (s)     |
    +------------------+     +------------------+
           |                          |
           +------------+-------------+
                        |
                        v
              +------------------+
              | Second Joined    |
              | Result           |
              | (all e+d, some s)|
              +------------------+
                        |
                        v

    Step 4: WHERE Clause Filter
              +------------------+
              |     Filter       |
              | e.salary > 50000 |
              +------------------+
                        |
                        v
              +------------------+
              | Filtered Result  |
              +------------------+
                        |
                        v

    Step 5: SELECT Columns
              +------------------+
              |  Project Columns |
              | e.name, d.dept,  |
              | s.amount         |
              +------------------+
                        |
                        v

    Step 6: ORDER BY
              +------------------+
              |      Sort        |
              | s.amount DESC    |
              +------------------+
                        |
                        v
              +------------------+
              |   Final Result   |
              +------------------+

    ============================================================

    LOGICAL ORDER OF OPERATIONS:

    1. FROM        - Identify base table
    2. JOIN        - Combine tables based on conditions
    3. WHERE       - Filter rows
    4. GROUP BY    - Group remaining rows (if applicable)
    5. HAVING      - Filter groups (if applicable)
    6. SELECT      - Choose columns
    7. DISTINCT    - Remove duplicates (if applicable)
    8. ORDER BY    - Sort results
    9. LIMIT       - Limit output (if applicable)

    ============================================================

    JOIN ALGORITHM TYPES (used by optimizer):

    +------------------+   +------------------+   +------------------+
    |   Nested Loop    |   |   Hash Join      |   |   Merge Join     |
    +------------------+   +------------------+   +------------------+
    | For each row in  |   | Build hash table |   | Sort both tables |
    | outer table,     |   | from smaller     |   | on join key,     |
    | scan inner table |   | table, probe     |   | merge sorted     |
    |                  |   | with larger      |   | results          |
    +------------------+   +------------------+   +------------------+
    | Best for:        |   | Best for:        |   | Best for:        |
    | - Small tables   |   | - Large tables   |   | - Pre-sorted     |
    | - Index exists   |   | - No index       |   | - Range queries  |
    | - Few iterations |   | - Equality joins |   | - Sorted output  |
    +------------------+   +------------------+   +------------------+
```

---

## Performance Considerations

### 1. Index Your Join Columns

Join operations perform best when join columns are indexed.

```sql
-- Create indexes on frequently joined columns
CREATE INDEX idx_employees_dept ON employees(department_id);
CREATE INDEX idx_employees_manager ON employees(manager_id);
CREATE INDEX idx_sales_customer ON sales(customer_id);
CREATE INDEX idx_sales_employee ON sales(employee_id);
CREATE INDEX idx_projects_dept ON projects(department_id);
```

### 2. Use Appropriate Join Types

| Scenario | Recommended Join | Why |
|----------|------------------|-----|
| Only matching records needed | INNER JOIN | Most efficient, smallest result set |
| Need all from one table | LEFT JOIN | Preserves all primary records |
| Checking existence | EXISTS/IN | Often faster than JOIN |
| Need all combinations | CROSS JOIN | Use sparingly, large result sets |

### 3. Filter Early

Apply WHERE conditions as early as possible to reduce the dataset before joining.

```sql
-- Less efficient: Filter after join
SELECT e.first_name, d.department_name
FROM employees e
JOIN departments d ON e.department_id = d.department_id
WHERE e.hire_date > '2020-01-01';

-- More efficient: Filter in subquery
SELECT e.first_name, d.department_name
FROM (SELECT * FROM employees WHERE hire_date > '2020-01-01') e
JOIN departments d ON e.department_id = d.department_id;
```

### 4. Avoid SELECT *

Only select the columns you need to reduce I/O and memory usage.

```sql
-- Inefficient
SELECT * FROM employees e
JOIN departments d ON e.department_id = d.department_id;

-- Efficient
SELECT e.first_name, e.last_name, d.department_name
FROM employees e
JOIN departments d ON e.department_id = d.department_id;
```

### 5. Use EXPLAIN to Analyze Queries

```sql
EXPLAIN ANALYZE
SELECT e.first_name, d.department_name
FROM employees e
JOIN departments d ON e.department_id = d.department_id;
```

This shows:
- Join algorithm used (Nested Loop, Hash, Merge)
- Estimated vs actual rows
- Index usage
- Cost estimates

### 6. Consider Join Order

The database optimizer usually handles this, but for complex queries:

```sql
-- Put the most filtered table first
-- Put smaller tables before larger ones
-- Put tables with indexes on join columns first
```

### 7. Avoid Functions on Join Columns

Functions prevent index usage on join columns.

```sql
-- Inefficient: Function on join column
SELECT e.first_name, d.department_name
FROM employees e
JOIN departments d ON UPPER(e.dept_code) = UPPER(d.dept_code);

-- Efficient: Use consistent data types and formats
SELECT e.first_name, d.department_name
FROM employees e
JOIN departments d ON e.department_id = d.department_id;
```

### 8. Limit Result Sets

Use LIMIT for large queries during development and testing.

```sql
SELECT e.first_name, d.department_name
FROM employees e
JOIN departments d ON e.department_id = d.department_id
LIMIT 1000;
```

### 9. Use EXISTS Instead of JOIN for Existence Checks

```sql
-- Less efficient for simple existence check
SELECT DISTINCT c.customer_name
FROM customers c
JOIN sales s ON c.customer_id = s.customer_id;

-- More efficient
SELECT c.customer_name
FROM customers c
WHERE EXISTS (
    SELECT 1 FROM sales s WHERE s.customer_id = c.customer_id
);
```

### 10. Batch Large Operations

For very large joins, consider processing in batches:

```sql
-- Process in batches
SELECT e.first_name, d.department_name
FROM employees e
JOIN departments d ON e.department_id = d.department_id
WHERE e.employee_id BETWEEN 1 AND 10000;

-- Then 10001-20000, etc.
```

---

## Summary

SQL Joins are essential for working with relational databases. Key takeaways:

1. **Choose the right join type** for your use case
2. **Always use table aliases** for clarity
3. **Qualify all column names** to avoid ambiguity
4. **Be mindful of NULL values** and how different joins handle them
5. **Index join columns** for better performance
6. **Filter early** to reduce dataset size
7. **Use EXPLAIN** to understand query execution
8. **Practice regularly** with complex scenarios

Master these concepts and you'll be able to write efficient, maintainable SQL queries for any data retrieval task.

---

## Next Steps

Continue to [Phase 6: Subqueries](../phase-06-subqueries/README.md) to learn about nested queries and advanced filtering techniques.
