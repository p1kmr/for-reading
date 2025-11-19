# Phase 1: SQL Fundamentals & Database Basics

## Table of Contents
- [What is a Database?](#what-is-a-database)
- [Why SQL?](#why-sql)
- [Database Installation](#database-installation)
- [Understanding Tables](#understanding-tables)
- [Data Types](#data-types)
- [Database Structure Diagrams](#database-structure-diagrams)
- [Code Examples](#code-examples)
- [Practice Questions](#practice-questions)
- [Common Beginner Mistakes](#common-beginner-mistakes)

---

## What is a Database?

### WHY
In the real world, organizations need to store, organize, and retrieve massive amounts of data efficiently. Imagine a company like Amazon trying to manage millions of products, customers, and orders using Excel spreadsheets - it would be impossible! Databases solve this problem by providing:
- **Structured storage**: Organized data in tables
- **Fast retrieval**: Quick access to specific information
- **Data integrity**: Rules to keep data accurate
- **Concurrent access**: Multiple users can work simultaneously
- **Security**: Control who can access what data

### WHAT
A **database** is an organized collection of structured data stored electronically. Think of it as a digital filing cabinet where:
- Data is organized in **tables** (like spreadsheet tabs)
- Each table has **rows** (records) and **columns** (fields)
- **Relationships** connect tables together
- **Rules** ensure data stays consistent

### Real-World Examples
- **E-commerce**: Product catalogs, customer orders, inventory
- **Banking**: Account information, transactions, customer data
- **Social Media**: User profiles, posts, comments, likes
- **Healthcare**: Patient records, appointments, prescriptions
- **Education**: Student information, courses, grades

---

## Why SQL?

### WHY
SQL (Structured Query Language) is the universal language for talking to databases. Learning SQL is essential because:
- **Industry Standard**: 90%+ of companies use SQL databases
- **High Demand**: SQL skills are required for most tech jobs
- **Versatile**: Works with MySQL, PostgreSQL, SQL Server, Oracle, etc.
- **Powerful**: Can handle simple to complex data operations
- **Interview Essential**: Almost every tech interview includes SQL questions

### WHAT
SQL is a declarative language, meaning you tell the database **what** you want, not **how** to get it. Unlike programming languages like Python or Java, you describe the desired result, and the database figures out the best way to retrieve it.

### Key SQL Capabilities
```
┌─────────────────────────────────────────┐
│         SQL OPERATIONS (CRUD)           │
├─────────────────────────────────────────┤
│  CREATE  →  Add new data/tables         │
│  READ    →  Retrieve/query data         │
│  UPDATE  →  Modify existing data        │
│  DELETE  →  Remove data                 │
└─────────────────────────────────────────┘
```

### SQL vs Other Technologies

| Feature | SQL | Excel | Python/Pandas |
|---------|-----|-------|---------------|
| Data Volume | Millions+ rows | ~1 million rows | Limited by RAM |
| Speed | Very Fast | Slow with large data | Medium |
| Multi-user | Yes | Limited | No |
| Learning Curve | Easy-Medium | Easy | Medium-Hard |
| Industry Use | Universal | Business analysis | Data science |

---

## Database Installation

### MySQL Installation (Recommended for Beginners)

#### **Windows**
```bash
# Download MySQL Installer from mysql.com
# Choose MySQL Server 8.0+
# During installation, set root password
# Install MySQL Workbench (GUI tool)
```

#### **macOS**
```bash
# Using Homebrew
brew install mysql

# Start MySQL service
brew services start mysql

# Secure installation
mysql_secure_installation
```

#### **Linux (Ubuntu/Debian)**
```bash
# Update package index
sudo apt update

# Install MySQL Server
sudo apt install mysql-server

# Start MySQL service
sudo systemctl start mysql

# Secure installation
sudo mysql_secure_installation
```

### PostgreSQL Installation (Alternative)

#### **macOS**
```bash
brew install postgresql
brew services start postgresql
```

#### **Linux**
```bash
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

### Connecting to Database
```bash
# MySQL
mysql -u root -p

# PostgreSQL
psql -U postgres
```

### Using Online Alternatives (No Installation)
- **DB Fiddle**: https://www.db-fiddle.com/
- **SQL Fiddle**: http://sqlfiddle.com/
- **SQLite Online**: https://sqliteonline.com/
- **W3Schools SQL Tryit**: https://www.w3schools.com/sql/trysql.asp?filename=trysql_select_all

---

## Understanding Tables

### WHY
Tables are the foundation of databases. They organize data into a structured format that's easy to query and manage.

### WHAT
A table is a collection of related data organized in rows and columns:
- **Row (Record/Tuple)**: A single entry of data
- **Column (Field/Attribute)**: A category of data
- **Cell**: Intersection of row and column

### Visual Representation
```
employees TABLE:
┌────┬────────────┬──────────────┬─────────────┬────────┐
│ id │ first_name │  last_name   │    email    │ salary │
├────┼────────────┼──────────────┼─────────────┼────────┤
│ 1  │ John       │ Doe          │ john@co.com │ 120000 │  ← Row 1
│ 2  │ Jane       │ Smith        │ jane@co.com │ 95000  │  ← Row 2
│ 3  │ Mike       │ Johnson      │ mike@co.com │ 85000  │  ← Row 3
└────┴────────────┴──────────────┴─────────────┴────────┘
  ↑       ↑            ↑              ↑           ↑
Column  Column      Column         Column      Column
```

### Table Components

#### Primary Key
- **WHY**: Uniquely identifies each row
- **WHAT**: A column (or combination) that must be unique and not null
- **Example**: `id` column in the employees table

#### Foreign Key
- **WHY**: Creates relationships between tables
- **WHAT**: A column that references the primary key of another table
- **Example**: `department_id` in employees references `id` in departments

```
┌─────────────────┐           ┌──────────────────┐
│   departments   │           │    employees     │
├─────────────────┤           ├──────────────────┤
│ id (PK)         │◄─────────┤│ department_id(FK)│
│ name            │     1:N   ││ first_name       │
│ budget          │           ││ salary           │
└─────────────────┘           └──────────────────┘
     One department              Many employees
```

---

## Data Types

### WHY
Data types tell the database what kind of data each column holds and how to store it efficiently. Using the correct data type:
- Saves storage space
- Improves query performance
- Prevents invalid data entry
- Enables proper operations (can't multiply text!)

### WHAT
Data types define the nature of data: numbers, text, dates, etc.

### Common SQL Data Types

#### 1. **Numeric Types**

| Type | Description | Example Use Case | Range |
|------|-------------|------------------|-------|
| `INT` | Whole numbers | IDs, quantities, age | -2,147,483,648 to 2,147,483,647 |
| `BIGINT` | Large whole numbers | Large IDs, populations | -9,223,372,036,854,775,808 to ... |
| `DECIMAL(p,s)` | Fixed-point numbers | Money, precise calculations | p=total digits, s=decimals |
| `FLOAT` | Approximate decimals | Scientific data | 7 decimal digits precision |
| `DOUBLE` | Double precision | Complex calculations | 15-16 decimal digits |

**Examples:**
```sql
salary DECIMAL(10, 2)     -- $99,999,999.99 max
age INT                    -- 25
price DECIMAL(8, 2)        -- $999,999.99
temperature FLOAT          -- 98.6
```

#### 2. **String/Text Types**

| Type | Description | Max Length | Use Case |
|------|-------------|------------|----------|
| `CHAR(n)` | Fixed-length | n characters | Country codes (US, UK) |
| `VARCHAR(n)` | Variable-length | n characters | Names, emails, addresses |
| `TEXT` | Long text | ~65,535 chars | Descriptions, comments |
| `LONGTEXT` | Very long text | 4GB | Articles, documents |

**Examples:**
```sql
country_code CHAR(2)        -- 'US', 'UK' (always 2 chars)
email VARCHAR(100)          -- 'user@example.com' (up to 100)
description TEXT            -- Long product description
```

#### 3. **Date and Time Types**

| Type | Format | Example | Use Case |
|------|--------|---------|----------|
| `DATE` | YYYY-MM-DD | 2023-12-25 | Birth dates, deadlines |
| `TIME` | HH:MM:SS | 14:30:00 | Time of day |
| `DATETIME` | YYYY-MM-DD HH:MM:SS | 2023-12-25 14:30:00 | Created timestamps |
| `TIMESTAMP` | Auto-updating datetime | 2023-12-25 14:30:00 | Last modified |
| `YEAR` | YYYY | 2023 | Year only |

**Examples:**
```sql
hire_date DATE               -- 2023-01-15
meeting_time TIME            -- 14:30:00
created_at DATETIME          -- 2023-01-15 14:30:00
last_login TIMESTAMP         -- Auto-updates
```

#### 4. **Boolean Type**

| Type | Values | Use Case |
|------|--------|----------|
| `BOOLEAN` or `TINYINT(1)` | TRUE/FALSE or 1/0 | Active status, flags |

**Examples:**
```sql
is_active BOOLEAN            -- TRUE or FALSE
is_verified TINYINT(1)       -- 1 or 0
```

#### 5. **Special Types**

| Type | Description | Example |
|------|-------------|---------|
| `ENUM` | Predefined set of values | ENUM('small','medium','large') |
| `JSON` | JSON formatted data | {"name": "John", "age": 30} |
| `BLOB` | Binary large objects | Images, files |

### Choosing the Right Data Type

```
Decision Tree:
┌────────────────────┐
│  What kind of data?│
└─────────┬──────────┘
          │
    ┌─────┼─────┬──────────┬────────┐
    │     │     │          │        │
  Number Text  Date    True/False  Other
    │     │     │          │        │
    ▼     ▼     ▼          ▼        ▼
┌───────┐ ┌────┐ ┌────┐ ┌───────┐ ┌────┐
│Money? │ │How │ │What│ │BOOLEAN│ │JSON│
│DECIMAL│ │long│ │time│ └───────┘ │ENUM│
└───────┘ │?   │ │info│           │BLOB│
          │    │ │?   │           └────┘
          ▼    │ ▼    │
        ┌────┐ ┌────┐
        │<255│ │DATE│
        │VARCH│ │TIME│
        │AR  │ │TIMES│
        │>255│ │TAMP│
        │TEXT│ └────┘
        └────┘
```

---

## Database Structure Diagrams

### Basic Database Architecture
```
┌─────────────────────────────────────────────────┐
│                 DATABASE SERVER                  │
├─────────────────────────────────────────────────┤
│                                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────┐│
│  │ Database 1  │  │ Database 2  │  │Database 3││
│  ├─────────────┤  ├─────────────┤  ├──────────┤│
│  │             │  │             │  │          ││
│  │ ┌─────────┐ │  │ ┌─────────┐ │  │ Tables   ││
│  │ │ Table A │ │  │ │ Table X │ │  │ Views    ││
│  │ │ Table B │ │  │ │ Table Y │ │  │ Indexes  ││
│  │ │ Table C │ │  │ │ Table Z │ │  │ etc.     ││
│  │ └─────────┘ │  │ └─────────┘ │  │          ││
│  └─────────────┘  └─────────────┘  └──────────┘│
└─────────────────────────────────────────────────┘
```

### Table Relationship Types

#### One-to-Many (1:N)
```
┌───────────────┐         ┌──────────────┐
│  departments  │ 1     N │  employees   │
│──────────────│───────│──────────────│
│ id (PK)       │◄───────┤│ dept_id (FK) │
│ name          │         ││ name         │
└───────────────┘         └──────────────┘
Example: One department has many employees
```

#### Many-to-Many (N:M)
```
┌──────────┐         ┌────────────────┐         ┌──────────┐
│employees │ N     N │employee_projects│ N     N │ projects │
│──────────│───────│────────────────│───────│──────────│
│ id (PK)  │◄───────┤│ emp_id (FK)    │         ││ id (PK)  │
│ name     │         ││ proj_id (FK)   │────────►││ name     │
└──────────┘         │  hours_worked  │         └──────────┘
                     └────────────────┘
Example: Employees work on many projects, projects have many employees
```

#### One-to-One (1:1)
```
┌──────────┐         ┌──────────────┐
│  users   │ 1     1 │user_profiles │
│──────────│───────│──────────────│
│ id (PK)  │◄───────┤│ user_id (FK) │
│ username │         ││ bio          │
└──────────┘         │  avatar      │
                     └──────────────┘
Example: Each user has one profile
```

---

## Code Examples

### Example 1: Creating Your First Database
```sql
-- Create a new database
CREATE DATABASE company_db;

-- Select the database to use
USE company_db;

-- Verify you're in the correct database
SELECT DATABASE();
```

**Output:**
```
+-------------+
| DATABASE()  |
+-------------+
| company_db  |
+-------------+
```

### Example 2: Creating Your First Table
```sql
-- Create a simple employees table
CREATE TABLE employees (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    hire_date DATE,
    salary DECIMAL(10, 2)
);
```

**Explanation:**
- `id`: Auto-incrementing unique identifier
- `PRIMARY KEY`: Ensures each id is unique
- `AUTO_INCREMENT`: Automatically generates next number
- `NOT NULL`: Field cannot be empty
- `UNIQUE`: No duplicate emails allowed
- `DECIMAL(10, 2)`: Up to 99,999,999.99

### Example 3: Viewing Table Structure
```sql
-- See the table structure
DESCRIBE employees;

-- Alternative
SHOW COLUMNS FROM employees;
```

**Output:**
```
+------------+--------------+------+-----+---------+----------------+
| Field      | Type         | Null | Key | Default | Extra          |
+------------+--------------+------+-----+---------+----------------+
| id         | int          | NO   | PRI | NULL    | auto_increment |
| first_name | varchar(50)  | NO   |     | NULL    |                |
| last_name  | varchar(50)  | NO   |     | NULL    |                |
| email      | varchar(100) | YES  | UNI | NULL    |                |
| hire_date  | date         | YES  |     | NULL    |                |
| salary     | decimal(10,2)| YES  |     | NULL    |                |
+------------+--------------+------+-----+---------+----------------+
```

### Example 4: Creating Table with Foreign Key
```sql
-- First create the departments table
CREATE TABLE departments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    budget DECIMAL(15, 2)
);

-- Then create employees with foreign key
CREATE TABLE employees (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    department_id INT,
    salary DECIMAL(10, 2),
    FOREIGN KEY (department_id) REFERENCES departments(id)
);
```

### Example 5: Inserting Your First Data
```sql
-- Insert a department
INSERT INTO departments (name, budget)
VALUES ('Engineering', 500000.00);

-- Insert an employee
INSERT INTO employees (first_name, last_name, department_id, salary)
VALUES ('John', 'Doe', 1, 120000.00);
```

### Example 6: Retrieving Data
```sql
-- Get all employees
SELECT * FROM employees;

-- Get specific columns
SELECT first_name, last_name, salary
FROM employees;
```

### Example 7: Basic WHERE Clause
```sql
-- Find employees with salary > 100000
SELECT first_name, last_name, salary
FROM employees
WHERE salary > 100000;
```

### Example 8: Dropping Tables and Databases
```sql
-- Drop a table (CAREFUL!)
DROP TABLE employees;

-- Drop a database (VERY CAREFUL!)
DROP DATABASE company_db;
```

### Example 9: Working with Different Data Types
```sql
CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(8, 2),
    in_stock BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    size ENUM('Small', 'Medium', 'Large'),
    weight FLOAT
);
```

### Example 10: Adding Comments to Tables
```sql
-- Create table with comments
CREATE TABLE customers (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique customer ID',
    email VARCHAR(100) UNIQUE COMMENT 'Customer email address',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Account creation time'
) COMMENT='Customer information table';
```

---

## Practice Questions

### Easy Question 1: Select All Data

**Question**: Write a query to retrieve all columns from the `departments` table.

**Difficulty**: Easy

**Approach**: Use the SELECT statement with asterisk (*) to get all columns.

**Solution**:
```sql
SELECT * FROM departments;
```

**Explanation**:
- `SELECT *` means "select all columns"
- `FROM departments` specifies the table
- Result includes every column and every row

**Alternative Solutions**:
```sql
-- Explicitly list all columns (better practice)
SELECT id, name, budget, location, created_date
FROM departments;
```

**Interview Tip**: While `SELECT *` is convenient, in production code, explicitly list columns for better performance and clarity.

---

### Easy Question 2: Select Specific Columns

**Question**: Retrieve only the `first_name` and `salary` of all employees.

**Difficulty**: Easy

**Approach**: List specific column names after SELECT.

**Solution**:
```sql
SELECT first_name, salary
FROM employees;
```

**Explanation**:
- Only specified columns are returned
- Reduces data transfer
- Order of columns in result matches order in SELECT

**Output Example**:
```
+------------+-----------+
| first_name | salary    |
+------------+-----------+
| John       | 120000.00 |
| Jane       |  95000.00 |
| Mike       |  85000.00 |
+------------+-----------+
```

---

### Easy Question 3: Basic WHERE Clause

**Question**: Find all employees in department with id = 1.

**Difficulty**: Easy

**Approach**: Use WHERE clause to filter rows.

**Solution**:
```sql
SELECT first_name, last_name, department_id
FROM employees
WHERE department_id = 1;
```

**Explanation**:
- `WHERE` filters rows before returning results
- `=` is the equality operator
- Only rows matching the condition are returned

**Common Mistakes**:
- ❌ Using `==` instead of `=` (wrong: `WHERE department_id == 1`)
- ❌ Forgetting quotes for strings (correct: `WHERE name = 'John'`)
- ❌ Using assignment operator in some languages

---

## Common Beginner Mistakes

### Mistake 1: Missing Semicolon

❌ **Wrong:**
```sql
SELECT * FROM employees
SELECT * FROM departments
```

✅ **Correct:**
```sql
SELECT * FROM employees;
SELECT * FROM departments;
```

💡 **Why it matters**: SQL statements should end with semicolons, especially when running multiple queries.

🎯 **Interview Tip**: Always end SQL statements with semicolons. Some tools are forgiving, but interviews expect proper syntax.

---

### Mistake 2: Case Sensitivity Confusion

❌ **Wrong (might work but inconsistent):**
```sql
select * from EMPLOYEES where First_Name = 'john';
```

✅ **Correct (consistent style):**
```sql
SELECT * FROM employees WHERE first_name = 'John';
```

💡 **Why it matters**:
- SQL keywords are not case-sensitive (`SELECT` = `select`)
- Table and column names: depends on database (MySQL is case-insensitive on Windows, case-sensitive on Linux)
- String values ARE case-sensitive ('John' ≠ 'john')

🎯 **Interview Tip**:
- Use UPPERCASE for keywords
- Use lowercase for table/column names
- Match exact case for string values

---

### Mistake 3: Forgetting FROM Clause

❌ **Wrong:**
```sql
SELECT first_name, last_name;
```

✅ **Correct:**
```sql
SELECT first_name, last_name
FROM employees;
```

💡 **Why it matters**: You must specify which table to query.

**Error Message:** `ERROR: No tables used`

---

### Mistake 4: Wrong Data Type Usage

❌ **Wrong:**
```sql
CREATE TABLE employees (
    salary VARCHAR(10)  -- Storing numbers as text!
);
```

✅ **Correct:**
```sql
CREATE TABLE employees (
    salary DECIMAL(10, 2)  -- Proper numeric type
);
```

💡 **Why it matters**:
- Can't perform mathematical operations on text
- Takes more storage
- Allows invalid values like "abc"

---

### Mistake 5: Not Using NOT NULL Appropriately

❌ **Wrong:**
```sql
CREATE TABLE employees (
    first_name VARCHAR(50)  -- Can be NULL!
);
```

✅ **Correct:**
```sql
CREATE TABLE employees (
    first_name VARCHAR(50) NOT NULL  -- Must have a value
);
```

💡 **Why it matters**: Important fields should never be empty.

---

## Summary & Next Steps

### What You Learned
✅ What databases are and why they're important
✅ Why SQL is the industry standard
✅ How to install MySQL/PostgreSQL
✅ Understanding tables, rows, and columns
✅ SQL data types and when to use them
✅ Basic database structure and relationships
✅ Creating databases and tables
✅ Simple SELECT queries
✅ Common beginner mistakes to avoid

### Key Takeaways
1. **Databases organize data** into tables with rows and columns
2. **SQL is declarative** - you describe what you want, not how to get it
3. **Data types matter** - choose the right type for each column
4. **Primary keys** uniquely identify rows
5. **Foreign keys** create relationships between tables

### Practice Exercises
Before moving to Phase 2, practice:
1. Install MySQL or PostgreSQL on your machine
2. Create a database called `practice_db`
3. Create a `students` table with: id, name, email, enrollment_date, gpa
4. Create a `courses` table with: id, name, credits, instructor
5. Insert 5 students and 3 courses
6. Write SELECT queries to retrieve different columns

### What's Next?
In **Phase 2: Basic CRUD Operations**, you'll learn:
- INSERT: Adding new data
- UPDATE: Modifying existing data
- DELETE: Removing data
- Advanced SELECT with ORDER BY and LIMIT
- Best practices for data manipulation

---

## Additional Resources

### Documentation
- [MySQL Official Documentation](https://dev.mysql.com/doc/)
- [PostgreSQL Official Documentation](https://www.postgresql.org/docs/)
- [W3Schools SQL Tutorial](https://www.w3schools.com/sql/)

### Practice Platforms
- [LeetCode SQL](https://leetcode.com/problemset/database/)
- [HackerRank SQL](https://www.hackerrank.com/domains/sql)
- [SQLZoo](https://sqlzoo.net/)
- [DataLemur](https://datalemur.com/)

### Books
- "SQL in 10 Minutes" by Ben Forta
- "Learning SQL" by Alan Beaulieu
- "SQL Cookbook" by Anthony Molinaro

---

**Ready for Phase 2? Let's master CRUD operations!** 🚀
