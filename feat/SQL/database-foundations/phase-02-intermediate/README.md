# Phase 2: Basic CRUD Operations

## Table of Contents
- [Introduction to CRUD](#introduction-to-crud)
- [INSERT - Creating Data](#insert---creating-data)
- [SELECT - Reading Data](#select---reading-data)
- [UPDATE - Modifying Data](#update---modifying-data)
- [DELETE - Removing Data](#delete---removing-data)
- [ORDER BY - Sorting Results](#order-by---sorting-results)
- [LIMIT - Restricting Results](#limit---restricting-results)
- [Data Flow Diagrams](#data-flow-diagrams)
- [Practice Questions](#practice-questions)
- [Common Mistakes](#common-mistakes)

---

## Introduction to CRUD

### WHY
CRUD operations are the foundation of all database interactions. Every application - from social media to banking - performs these four basic operations:
- **Create**: Add new users, posts, transactions
- **Read**: Display profiles, search products, view history
- **Update**: Edit profiles, modify orders, change settings
- **Delete**: Remove accounts, cancel orders, delete posts

Mastering CRUD is essential because:
- 80% of SQL work involves CRUD operations
- Every database interaction falls into one of these categories
- Interview questions heavily focus on CRUD scenarios
- Understanding CRUD enables you to build real applications

### WHAT
CRUD stands for:
- **C**reate → `INSERT`
- **R**ead → `SELECT`
- **U**pdate → `UPDATE`
- **D**elete → `DELETE`

### CRUD Operation Flow
```
Application ────→ SQL Command ────→ Database
                                      │
User Action      CRUD Operation      Data Change
   │                   │                 │
   ▼                   ▼                 ▼
Register User  →   INSERT       →   New row added
View Profile   →   SELECT       →   Data retrieved
Edit Email     →   UPDATE       →   Row modified
Delete Account →   DELETE       →   Row removed
```

---

## INSERT - Creating Data

### WHY
INSERT adds new data to your database. Without INSERT, your database would remain empty! Real-world uses:
- User registration
- Creating new orders
- Adding products to inventory
- Posting comments
- Recording transactions

### WHAT
INSERT adds new rows (records) to a table.

### HOW - Basic Syntax
```sql
INSERT INTO table_name (column1, column2, column3)
VALUES (value1, value2, value3);
```

### WHEN
Use INSERT when you need to:
- Add new records
- Import data
- Create initial dataset
- Populate tables after creation

---

### Example 1: Insert Single Row
```sql
-- Insert a new employee
INSERT INTO employees (first_name, last_name, email, department_id, salary, hire_date)
VALUES ('Alice', 'Johnson', 'alice.j@company.com', 1, 95000.00, '2024-01-15');
```

**Explanation:**
1. Specify table name: `employees`
2. List columns in parentheses
3. Provide values in same order
4. Strings use single quotes
5. Numbers don't need quotes
6. Dates use 'YYYY-MM-DD' format

---

### Example 2: Insert Multiple Rows
```sql
-- Insert multiple employees at once
INSERT INTO employees (first_name, last_name, email, salary, hire_date)
VALUES
    ('Bob', 'Smith', 'bob.s@company.com', 85000.00, '2024-01-20'),
    ('Carol', 'White', 'carol.w@company.com', 90000.00, '2024-01-25'),
    ('David', 'Brown', 'david.b@company.com', 88000.00, '2024-02-01');
```

**Benefit:** More efficient than multiple single INSERT statements.

---

### Example 3: Insert Without Specifying Columns
```sql
-- Insert values for ALL columns in table order
INSERT INTO departments
VALUES (NULL, 'Customer Support', 180000.00, 'Austin', '2024-01-01');
```

**Warning:** This is risky! If table structure changes, query breaks.

**Best Practice:** Always specify columns:
```sql
INSERT INTO departments (name, budget, location, created_date)
VALUES ('Customer Support', 180000.00, 'Austin', '2024-01-01');
```

---

### Example 4: Insert with AUTO_INCREMENT
```sql
-- id is AUTO_INCREMENT, so we skip it
INSERT INTO employees (first_name, last_name, salary)
VALUES ('Eve', 'Davis', 92000.00);

-- Check the auto-generated ID
SELECT LAST_INSERT_ID();  -- Returns the generated ID
```

---

### Example 5: Insert with Default Values
```sql
-- Create table with defaults
CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT,
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert using defaults
INSERT INTO orders (customer_id)
VALUES (101);

-- Result: status='pending', created_at=current time
```

---

## SELECT - Reading Data

### WHY
SELECT retrieves data from the database. It's the most-used SQL command:
- Display user profiles
- Search for products
- Generate reports
- Fetch application data
- Analytics and insights

### WHAT
SELECT queries data and returns result sets (rows of data).

### HOW - Basic Syntax
```sql
SELECT column1, column2, ...
FROM table_name
WHERE condition
ORDER BY column
LIMIT number;
```

---

### Example 6: Select All Columns
```sql
-- Get everything from employees
SELECT * FROM employees;
```

**Output:**
```
+----+------------+-----------+------------------------+-------+-----------+
| id | first_name | last_name | email                  | salary| hire_date |
+----+------------+-----------+------------------------+-------+-----------+
|  1 | John       | Doe       | john.doe@company.com   |120000 | 2020-01-15|
|  2 | Jane       | Smith     | jane.smith@company.com | 95000 | 2020-03-20|
+----+------------+-----------+------------------------+-------+-----------+
```

---

### Example 7: Select Specific Columns
```sql
-- Get only name and salary
SELECT first_name, last_name, salary
FROM employees;
```

**Benefit:** Faster query, less data transfer.

---

### Example 8: Column Aliases
```sql
-- Rename columns in output
SELECT
    first_name AS 'First Name',
    last_name AS 'Last Name',
    salary AS 'Annual Salary'
FROM employees;
```

**Output:**
```
+------------+-----------+---------------+
| First Name | Last Name | Annual Salary |
+------------+-----------+---------------+
| John       | Doe       | 120000        |
+------------+-----------+---------------+
```

---

### Example 9: Calculated Columns
```sql
-- Calculate monthly salary
SELECT
    first_name,
    last_name,
    salary AS annual_salary,
    salary / 12 AS monthly_salary,
    salary * 0.15 AS estimated_tax
FROM employees;
```

**Output:**
```
+------------+-----------+---------------+-----------------+----------------+
| first_name | last_name | annual_salary | monthly_salary  | estimated_tax  |
+------------+-----------+---------------+-----------------+----------------+
| John       | Doe       | 120000        | 10000.00        | 18000.00       |
+------------+-----------+---------------+-----------------+----------------+
```

---

### Example 10: String Concatenation
```sql
-- Combine first and last name
SELECT
    CONCAT(first_name, ' ', last_name) AS full_name,
    email,
    salary
FROM employees;
```

**Output:**
```
+------------+------------------------+---------+
| full_name  | email                  | salary  |
+------------+------------------------+---------+
| John Doe   | john.doe@company.com   | 120000  |
+------------+------------------------+---------+
```

---

## UPDATE - Modifying Data

### WHY
UPDATE modifies existing data. Real-world uses:
- User edits their profile
- Price changes
- Status updates
- Correcting mistakes
- Bulk updates

### WHAT
UPDATE changes values in existing rows.

### HOW - Basic Syntax
```sql
UPDATE table_name
SET column1 = value1, column2 = value2
WHERE condition;
```

### ⚠️ CRITICAL WARNING
**ALWAYS use WHERE with UPDATE!** Without it, you'll update EVERY row!

---

### Example 11: Update Single Row
```sql
-- Give employee id=2 a raise
UPDATE employees
SET salary = 105000.00
WHERE id = 2;

-- Verify the change
SELECT first_name, last_name, salary
FROM employees
WHERE id = 2;
```

---

### Example 12: Update Multiple Columns
```sql
-- Update employee's email and salary
UPDATE employees
SET
    email = 'jane.newmail@company.com',
    salary = 110000.00
WHERE id = 2;
```

---

### Example 13: Update with Calculation
```sql
-- Give all employees a 10% raise
UPDATE employees
SET salary = salary * 1.10
WHERE department_id = 1;
```

---

### Example 14: Update Based on Another Column
```sql
-- Update status based on date
UPDATE projects
SET status = 'Completed'
WHERE end_date < CURRENT_DATE;
```

---

### Example 15: Update Multiple Rows
```sql
-- Update all HR department employees
UPDATE employees
SET salary = salary + 5000
WHERE department_id = 4;

-- Check affected rows
SELECT ROW_COUNT();  -- Returns number of updated rows
```

---

## DELETE - Removing Data

### WHY
DELETE removes data from the database. Real-world uses:
- User deletes their account
- Remove expired records
- Clean up test data
- Delete spam comments
- Archive old data

### WHAT
DELETE removes entire rows from a table.

### HOW - Basic Syntax
```sql
DELETE FROM table_name
WHERE condition;
```

### ⚠️ CRITICAL WARNING
**ALWAYS use WHERE with DELETE!** Without it, you'll delete EVERY row!

---

### Example 16: Delete Single Row
```sql
-- Delete employee with id=5
DELETE FROM employees
WHERE id = 5;
```

---

### Example 17: Delete Multiple Rows
```sql
-- Delete all employees hired before 2020
DELETE FROM employees
WHERE hire_date < '2020-01-01';
```

---

### Example 18: Delete with Multiple Conditions
```sql
-- Delete inactive customers from USA
DELETE FROM customers
WHERE status = 'Inactive' AND country = 'USA';
```

---

### Example 19: Safe Delete Pattern
```sql
-- ALWAYS test with SELECT first!
-- Step 1: See what you'll delete
SELECT * FROM employees
WHERE department_id = 99;

-- Step 2: If results look correct, change to DELETE
DELETE FROM employees
WHERE department_id = 99;
```

---

### Example 20: Delete All Rows (Use With Caution!)
```sql
-- Delete all rows but keep table structure
DELETE FROM test_table;

-- Alternative: TRUNCATE (faster, can't be rolled back)
TRUNCATE TABLE test_table;
```

**Difference:**
- `DELETE`: Can use WHERE, slower, can rollback
- `TRUNCATE`: No WHERE, faster, resets AUTO_INCREMENT, can't rollback

---

## ORDER BY - Sorting Results

### WHY
ORDER BY sorts query results. Essential for:
- Displaying top performers
- Chronological listings
- Alphabetical order
- Rankings and leaderboards

### WHAT
ORDER BY arranges results in ascending or descending order.

### HOW - Syntax
```sql
SELECT columns
FROM table
ORDER BY column1 [ASC|DESC], column2 [ASC|DESC];
```

---

### Example 21: Sort Ascending (Default)
```sql
-- Sort employees by salary (lowest to highest)
SELECT first_name, last_name, salary
FROM employees
ORDER BY salary;  -- ASC is default
```

---

### Example 22: Sort Descending
```sql
-- Sort employees by salary (highest to lowest)
SELECT first_name, last_name, salary
FROM employees
ORDER BY salary DESC;
```

---

### Example 23: Sort by Multiple Columns
```sql
-- Sort by department, then by salary within each department
SELECT first_name, last_name, department_id, salary
FROM employees
ORDER BY department_id ASC, salary DESC;
```

**Explanation:**
1. First sorts by department_id (ascending)
2. Within each department, sorts by salary (descending)

---

### Example 24: Sort by Column Position
```sql
-- Sort by 3rd column (salary)
SELECT first_name, last_name, salary
FROM employees
ORDER BY 3 DESC;
```

**Note:** Not recommended - unclear and breaks if column order changes.

---

### Example 25: Sort with Aliases
```sql
-- Sort by calculated column
SELECT
    first_name,
    last_name,
    salary,
    salary * 12 AS annual_total
FROM employees
ORDER BY annual_total DESC;
```

---

## LIMIT - Restricting Results

### WHY
LIMIT restricts the number of rows returned. Used for:
- Pagination (showing 20 items per page)
- Top N queries (top 10 products)
- Sampling data
- Performance (don't load 1 million rows!)

### WHAT
LIMIT caps the number of results returned.

### HOW - Syntax
```sql
SELECT columns
FROM table
ORDER BY column
LIMIT number;

-- With offset (pagination)
LIMIT offset, count;
```

---

### Example 26: Get Top 5 Results
```sql
-- Top 5 highest paid employees
SELECT first_name, last_name, salary
FROM employees
ORDER BY salary DESC
LIMIT 5;
```

---

### Example 27: Pagination
```sql
-- Page 1: First 10 results
SELECT * FROM employees
LIMIT 10;

-- Page 2: Next 10 results (skip first 10)
SELECT * FROM employees
LIMIT 10 OFFSET 10;

-- Alternative syntax
SELECT * FROM employees
LIMIT 10, 10;  -- offset, count
```

---

### Example 28: Sample Data
```sql
-- Get random sample of 5 employees
SELECT * FROM employees
ORDER BY RAND()
LIMIT 5;
```

---

### Example 29: Bottom N Query
```sql
-- 5 lowest paid employees
SELECT first_name, last_name, salary
FROM employees
ORDER BY salary ASC
LIMIT 5;
```

---

### Example 30: LIMIT with WHERE
```sql
-- Top 3 highest paid employees in Engineering
SELECT first_name, last_name, salary
FROM employees
WHERE department_id = 1
ORDER BY salary DESC
LIMIT 3;
```

---

## Data Flow Diagrams

### INSERT Flow
```
Application Data
      │
      ▼
┌─────────────────┐
│ INSERT Command  │
├─────────────────┤
│ INTO employees  │
│ VALUES (...)    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Validation      │
│ - Data types OK?│
│ - Constraints?  │
│ - NOT NULL?     │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
   Pass      Fail
    │         │
    ▼         ▼
┌──────┐  ┌──────┐
│Insert│  │Error │
│ Row  │  │Return│
└──────┘  └──────┘
```

### UPDATE Flow
```
UPDATE Command
      │
      ▼
┌──────────────────┐
│ Find rows WHERE  │
│ condition = TRUE │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ For each row:    │
│ SET column=value │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Verify           │
│ constraints      │
└────────┬─────────┘
         │
    ┌────┴────┐
    │         │
   Pass      Fail
    │         │
    ▼         ▼
┌──────┐  ┌──────┐
│Update│  │Error │
│Rows  │  │Rollbk│
└──────┘  └──────┘
```

### DELETE Flow
```
DELETE Command
      │
      ▼
┌──────────────────┐
│ Find rows WHERE  │
│ condition = TRUE │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Check Foreign    │
│ Key Constraints  │
└────────┬─────────┘
         │
    ┌────┴────┐
    │         │
   Safe    Has FK
    │         │
    ▼         ▼
┌──────┐  ┌──────┐
│Delete│  │Error │
│Rows  │  │Cannot│
└──────┘  └──────┘
```

---

## Practice Questions

### Easy Question 1: Insert New Customer

**Question**: Insert a new customer named "John Smith" with email "john.smith@email.com" from "USA" registered on "2024-01-15".

**Difficulty**: Easy

**Approach**: Use INSERT with specific columns.

**Solution**:
```sql
INSERT INTO customers (first_name, last_name, email, country, registration_date, status)
VALUES ('John', 'Smith', 'john.smith@email.com', 'USA', '2024-01-15', 'Active');
```

**Explanation**:
- List all columns you're providing values for
- Match values to column order
- Use single quotes for strings
- Date format: 'YYYY-MM-DD'

**Alternative Solution**:
```sql
-- Let id auto-generate, use default for status
INSERT INTO customers (first_name, last_name, email, country, registration_date)
VALUES ('John', 'Smith', 'john.smith@email.com', 'USA', '2024-01-15');
```

---

### Medium Question 2: Update Employee Salary

**Question**: Give a 15% raise to all employees in the Sales department (department_id = 3) who earn less than $80,000.

**Difficulty**: Medium

**Approach**: UPDATE with multiple WHERE conditions.

**Solution**:
```sql
UPDATE employees
SET salary = salary * 1.15
WHERE department_id = 3 AND salary < 80000;
```

**Explanation**:
- `salary = salary * 1.15` means new salary = old salary × 1.15
- AND combines two conditions
- Only rows matching BOTH conditions are updated

**Verification Query**:
```sql
-- Check who got the raise
SELECT first_name, last_name, salary
FROM employees
WHERE department_id = 3;
```

**Common Mistake**:
❌ `SET salary = 80000 * 1.15` (sets everyone to same value)
✅ `SET salary = salary * 1.15` (increases each person's current salary)

---

### Medium Question 3: Delete Old Records

**Question**: Delete all completed projects that ended before January 1, 2023.

**Difficulty**: Medium

**Approach**: DELETE with date comparison and status check.

**Solution**:
```sql
-- Step 1: Check what will be deleted
SELECT * FROM projects
WHERE status = 'Completed' AND end_date < '2023-01-01';

-- Step 2: If results look correct, delete
DELETE FROM projects
WHERE status = 'Completed' AND end_date < '2023-01-01';
```

**Explanation**:
- ALWAYS SELECT before DELETE to verify
- Date comparison: `< '2023-01-01'`
- Two conditions with AND

**Safety Tip**: Start a transaction if supported:
```sql
START TRANSACTION;
DELETE FROM projects WHERE status = 'Completed' AND end_date < '2023-01-01';
-- Check if correct
ROLLBACK;  -- If mistake
-- or
COMMIT;    -- If correct
```

---

### Medium Question 4: Top Spenders Query

**Question**: Find the top 10 customers by total purchase amount. Show customer name and total spent.

**Difficulty**: Medium

**Approach**: Use SUM with GROUP BY, ORDER BY, and LIMIT (preview of Phase 4).

**Solution**:
```sql
SELECT
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    SUM(s.amount) AS total_spent
FROM customers c
JOIN sales s ON c.id = s.customer_id
GROUP BY c.id, c.first_name, c.last_name
ORDER BY total_spent DESC
LIMIT 10;
```

**Explanation**:
- CONCAT combines first and last name
- SUM adds up all purchases per customer
- GROUP BY groups by customer
- ORDER BY DESC shows highest first
- LIMIT 10 restricts to top 10

---

### Medium Question 5: Bulk Insert

**Question**: Insert 3 new products into a products table in a single query.

**Difficulty**: Easy-Medium

**Approach**: Multi-row INSERT.

**Solution**:
```sql
INSERT INTO products (name, price, category, in_stock)
VALUES
    ('Laptop Pro', 1299.99, 'Electronics', TRUE),
    ('Wireless Mouse', 29.99, 'Electronics', TRUE),
    ('Office Chair', 299.99, 'Furniture', TRUE);
```

**Explanation**:
- One INSERT statement
- Multiple VALUE groups separated by commas
- More efficient than 3 separate INSERTs

**Performance Comparison**:
- 3 separate INSERTs: 3 database round-trips
- 1 multi-row INSERT: 1 database round-trip

---

## Common Mistakes

### Mistake 1: UPDATE Without WHERE

❌ **DISASTER:**
```sql
-- This updates EVERY employee to $100,000!
UPDATE employees
SET salary = 100000;
```

✅ **Correct:**
```sql
UPDATE employees
SET salary = 100000
WHERE id = 5;  -- Only update employee 5
```

💡 **Why it matters**: Without WHERE, all rows are updated!

🎯 **Interview Tip**: Interviewers test if you remember WHERE clauses. Always include them (or explicitly state you're updating all rows).

**Prevention Strategy**:
1. Always write WHERE first
2. Test with SELECT first
3. Use transactions for safety

---

### Mistake 2: DELETE Without WHERE

❌ **CATASTROPHIC:**
```sql
-- This deletes ALL employees!
DELETE FROM employees;
```

✅ **Correct:**
```sql
DELETE FROM employees
WHERE id = 5;  -- Only delete employee 5
```

💡 **Why it matters**: You just deleted the entire table data!

**Recovery:** If no backup, data is gone forever.

**Best Practice:**
```sql
-- Always do this first!
SELECT * FROM employees WHERE id = 5;  -- Preview
DELETE FROM employees WHERE id = 5;    -- Then delete
```

---

### Mistake 3: Wrong Quote Types

❌ **Wrong:**
```sql
INSERT INTO employees (first_name)
VALUES ("John");  -- Double quotes might not work

INSERT INTO employees (first_name)
VALUES (`John`);  -- Backticks are for identifiers!
```

✅ **Correct:**
```sql
INSERT INTO employees (first_name)
VALUES ('John');  -- Single quotes for strings
```

💡 **Why it matters**:
- Single quotes: string values
- Double quotes: may work but not standard
- Backticks: table/column names with spaces or reserved words

---

### Mistake 4: Forgetting ORDER BY with LIMIT

❌ **Unpredictable:**
```sql
-- Which 5 employees? Database decides!
SELECT * FROM employees
LIMIT 5;
```

✅ **Correct:**
```sql
-- Specific: 5 highest paid
SELECT * FROM employees
ORDER BY salary DESC
LIMIT 5;
```

💡 **Why it matters**: Without ORDER BY, results are unpredictable and may change between queries.

---

### Mistake 5: Incorrect INSERT Column Count

❌ **Wrong:**
```sql
INSERT INTO employees (first_name, last_name, salary)
VALUES ('John', 'Doe');  -- Only 2 values for 3 columns!
```

**Error**: `Column count doesn't match value count`

✅ **Correct:**
```sql
INSERT INTO employees (first_name, last_name, salary)
VALUES ('John', 'Doe', 85000.00);  -- All 3 values
```

---

### Mistake 6: Not Using Transactions for Critical Operations

❌ **Risky:**
```sql
-- If this fails halfway, partial update!
UPDATE employees SET salary = salary * 1.1 WHERE department_id = 1;
UPDATE departments SET budget = budget * 1.1 WHERE id = 1;
```

✅ **Correct:**
```sql
START TRANSACTION;

UPDATE employees SET salary = salary * 1.1 WHERE department_id = 1;
UPDATE departments SET budget = budget * 1.1 WHERE id = 1;

-- Check if both succeeded
COMMIT;  -- Or ROLLBACK if there's an error
```

---

## Summary & Next Steps

### What You Learned
✅ INSERT - Adding new data (single and multiple rows)
✅ SELECT - Reading and displaying data
✅ UPDATE - Modifying existing records
✅ DELETE - Removing data safely
✅ ORDER BY - Sorting results
✅ LIMIT - Restricting number of results
✅ Critical safety practices (WHERE clauses!)
✅ Transactions for data integrity

### Key Takeaways
1. **Always use WHERE** with UPDATE and DELETE (unless you truly want all rows)
2. **Test with SELECT** before UPDATE/DELETE
3. **ORDER BY is essential** with LIMIT for predictable results
4. **Multi-row INSERT** is more efficient than multiple single INSERTs
5. **Transactions** protect against partial updates

### CRUD Best Practices
| Operation | Best Practice |
|-----------|---------------|
| INSERT | Specify columns explicitly |
| SELECT | Select only needed columns |
| UPDATE | Always use WHERE (or transaction) |
| DELETE | SELECT first to verify, then DELETE |
| ORDER BY | Use with LIMIT for consistency |

### Practice Exercises
1. Insert 5 new customers from different countries
2. Update all products in 'Electronics' category to increase price by 5%
3. Delete all customers who registered before 2022 and have status 'Inactive'
4. Find top 10 employees by salary in each department
5. Create a transaction that updates employee salary and department budget together

### What's Next?
In **Phase 3: Filtering & Sorting Data**, you'll learn:
- Advanced WHERE conditions (AND, OR, NOT)
- IN, BETWEEN, LIKE operators
- NULL handling (IS NULL, IS NOT NULL)
- Pattern matching
- Complex filtering scenarios
- Interview questions on filtering

---

**Ready to master filtering? Let's go to Phase 3!** 🚀
