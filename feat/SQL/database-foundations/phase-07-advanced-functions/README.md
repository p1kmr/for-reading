# Phase 7: Advanced SQL Functions

## Table of Contents
- [Introduction](#introduction)
- [Sample Tables](#sample-tables)
- [1. CASE Statements](#1-case-statements)
- [2. String Functions](#2-string-functions)
- [3. Date/Time Functions](#3-datetime-functions)
- [4. Type Casting and Conversion](#4-type-casting-and-conversion)
- [5. Numeric Functions](#5-numeric-functions)
- [6. NULL Handling Functions](#6-null-handling-functions)
- [Function Demonstrations](#function-demonstrations)
- [Practice Questions](#practice-questions)
- [Common Mistakes](#common-mistakes)
- [Function Reference Cheat Sheet](#function-reference-cheat-sheet)

---

## Introduction

Advanced SQL functions transform raw data into meaningful insights. This phase covers conditional logic, string manipulation, date operations, type conversions, numeric calculations, and NULL handling - essential skills for data analysis and reporting.

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

-- Orders table
CREATE TABLE orders (
    order_id INT PRIMARY KEY,
    customer_name VARCHAR(100),
    order_date DATETIME,
    ship_date DATE,
    total_amount DECIMAL(10,2),
    discount_percent DECIMAL(5,2),
    status VARCHAR(20),
    notes TEXT
);

INSERT INTO orders VALUES
(1001, 'Acme Corp', '2024-01-15 09:30:00', '2024-01-17', 1500.50, 10.00, 'shipped', 'Priority shipping'),
(1002, 'Tech Solutions', '2024-01-16 14:45:00', '2024-01-20', 3200.00, NULL, 'delivered', NULL),
(1003, 'Global Industries', '2024-01-18 11:00:00', NULL, 890.25, 5.50, 'pending', 'Awaiting payment'),
(1004, 'StartUp Inc', '2024-02-01 08:15:00', '2024-02-03', 2100.75, 15.00, 'shipped', 'Express delivery'),
(1005, 'DataCorp', '2024-02-10 16:20:00', '2024-02-12', 4500.00, NULL, 'delivered', NULL),
(1006, 'InfoTech', '2024-02-15 10:00:00', NULL, 675.50, 8.25, 'cancelled', 'Customer request'),
(1007, 'MegaSoft', '2024-03-01 13:30:00', '2024-03-05', 8900.00, 20.00, 'delivered', 'Bulk order discount'),
(1008, 'CloudBase', '2024-03-10 09:45:00', NULL, 1200.00, NULL, 'pending', NULL);

-- Products table
CREATE TABLE products (
    product_id INT PRIMARY KEY,
    product_name VARCHAR(100),
    category VARCHAR(50),
    price DECIMAL(10,2),
    stock_quantity INT,
    created_at DATETIME,
    description TEXT
);

INSERT INTO products VALUES
(1, 'Laptop Pro 15', 'Electronics', 1299.99, 45, '2023-01-15 08:00:00', 'High-performance laptop'),
(2, 'Wireless Mouse', 'Accessories', 29.99, 200, '2023-02-20 10:30:00', 'Ergonomic design'),
(3, 'USB-C Hub', 'Accessories', 49.99, 150, '2023-03-10 14:15:00', '7-port hub'),
(4, 'Monitor 27"', 'Electronics', 399.99, 30, '2023-04-05 09:00:00', '4K resolution'),
(5, 'Keyboard Mech', 'Accessories', 89.99, 75, '2023-05-12 11:45:00', 'Mechanical switches'),
(6, 'Webcam HD', 'Electronics', 79.99, 100, '2023-06-18 16:00:00', '1080p streaming'),
(7, 'Desk Lamp', 'Office', 34.99, 120, '2023-07-22 08:30:00', 'LED adjustable'),
(8, 'Cable Organizer', 'Office', 12.99, 300, '2023-08-30 13:00:00', 'Set of 10');
```

---

## 1. CASE Statements

### WHY Use CASE Statements?
- Transform data values into meaningful categories
- Implement conditional logic within queries
- Create calculated fields based on multiple conditions
- Replace multiple queries with a single, flexible query
- Generate dynamic reports with custom classifications

### WHAT is a CASE Statement?
A CASE statement is SQL's way of implementing IF-THEN-ELSE logic. It evaluates conditions and returns a value when a condition is met.

### HOW to Use CASE Statements

#### Simple CASE (Equality Comparison)

```sql
-- Compare a single expression against multiple values
SELECT
    employee_id,
    first_name,
    status,
    CASE status
        WHEN 'active' THEN 'Currently Employed'
        WHEN 'inactive' THEN 'On Leave'
        WHEN 'probation' THEN 'Trial Period'
        ELSE 'Unknown Status'
    END AS employment_status
FROM employees;
```

**Result:**
| employee_id | first_name | status    | employment_status   |
|-------------|------------|-----------|---------------------|
| 1           | John       | active    | Currently Employed  |
| 5           | David      | probation | Trial Period        |
| 8           | Jennifer   | inactive  | On Leave            |

#### Searched CASE (Complex Conditions)

```sql
-- Evaluate multiple boolean expressions
SELECT
    employee_id,
    first_name,
    salary,
    CASE
        WHEN salary >= 100000 THEN 'Executive'
        WHEN salary >= 75000 THEN 'Senior'
        WHEN salary >= 50000 THEN 'Mid-Level'
        ELSE 'Entry-Level'
    END AS salary_band
FROM employees
ORDER BY salary DESC;
```

**Result:**
| employee_id | first_name | salary    | salary_band  |
|-------------|------------|-----------|--------------|
| 6           | Lisa       | 110000.00 | Executive    |
| 4           | Emily      | 95000.00  | Senior       |
| 2           | Sarah      | 82000.00  | Senior       |
| 7           | Robert     | 48000.00  | Entry-Level  |

#### CASE in Aggregations

```sql
-- Count employees by salary band
SELECT
    CASE
        WHEN salary >= 100000 THEN 'Executive'
        WHEN salary >= 75000 THEN 'Senior'
        WHEN salary >= 50000 THEN 'Mid-Level'
        ELSE 'Entry-Level'
    END AS salary_band,
    COUNT(*) AS employee_count,
    AVG(salary) AS avg_salary
FROM employees
GROUP BY
    CASE
        WHEN salary >= 100000 THEN 'Executive'
        WHEN salary >= 75000 THEN 'Senior'
        WHEN salary >= 50000 THEN 'Mid-Level'
        ELSE 'Entry-Level'
    END
ORDER BY avg_salary DESC;
```

#### CASE for Pivot-Style Queries

```sql
-- Create a summary report
SELECT
    department_id,
    SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) AS active_count,
    SUM(CASE WHEN status = 'inactive' THEN 1 ELSE 0 END) AS inactive_count,
    SUM(CASE WHEN status = 'probation' THEN 1 ELSE 0 END) AS probation_count
FROM employees
GROUP BY department_id;
```

### WHEN to Use Each Type

| Type | Use When | Example |
|------|----------|---------|
| Simple CASE | Comparing one expression to multiple values | Status codes, categories |
| Searched CASE | Complex conditions with ranges or multiple columns | Salary bands, date ranges |

---

## 2. String Functions

### CONCAT - Combine Strings

```sql
-- Basic concatenation
SELECT
    CONCAT(first_name, ' ', last_name) AS full_name,
    CONCAT('ID: ', employee_id) AS employee_code
FROM employees;

-- Multiple values
SELECT CONCAT(first_name, ' ', last_name, ' - ', COALESCE(email, 'No Email')) AS contact_info
FROM employees;
```

### SUBSTRING - Extract Part of String

```sql
-- SUBSTRING(string, start_position, length)
SELECT
    email,
    SUBSTRING(email, 1, LOCATE('@', email) - 1) AS username,
    SUBSTRING(email, LOCATE('@', email) + 1) AS domain
FROM employees
WHERE email IS NOT NULL;

-- Get first 3 characters
SELECT first_name, SUBSTRING(first_name, 1, 3) AS initials
FROM employees;
```

### LENGTH - String Length

```sql
-- Find string lengths
SELECT
    first_name,
    LENGTH(first_name) AS name_length,
    email,
    LENGTH(email) AS email_length
FROM employees;

-- Find long names
SELECT first_name, last_name
FROM employees
WHERE LENGTH(first_name) + LENGTH(last_name) > 12;
```

### TRIM, LTRIM, RTRIM - Remove Whitespace

```sql
-- Remove leading/trailing spaces
SELECT
    TRIM('   Hello World   ') AS trimmed,           -- 'Hello World'
    LTRIM('   Hello World   ') AS left_trimmed,     -- 'Hello World   '
    RTRIM('   Hello World   ') AS right_trimmed;    -- '   Hello World'

-- Remove specific characters
SELECT TRIM(BOTH '-' FROM '---Product---') AS cleaned;  -- 'Product'
```

### UPPER and LOWER - Case Conversion

```sql
-- Convert case
SELECT
    first_name,
    UPPER(first_name) AS upper_name,
    LOWER(email) AS lower_email,
    CONCAT(UPPER(SUBSTRING(first_name, 1, 1)), LOWER(SUBSTRING(first_name, 2))) AS proper_case
FROM employees;
```

### REPLACE - Substitute Characters

```sql
-- Replace text
SELECT
    email,
    REPLACE(email, '@company.com', '@newdomain.com') AS new_email,
    REPLACE(phone, '-', '') AS phone_digits
FROM employees;

-- Multiple replacements (nested)
SELECT REPLACE(REPLACE('Hello World', 'Hello', 'Hi'), 'World', 'Everyone') AS greeting;
-- Result: 'Hi Everyone'
```

### LEFT and RIGHT - Extract from Ends

```sql
-- Get characters from left or right
SELECT
    email,
    LEFT(email, 5) AS first_five,
    RIGHT(email, 3) AS last_three,
    RIGHT(phone, 4) AS last_four_digits
FROM employees
WHERE email IS NOT NULL;
```

### REVERSE - Reverse String

```sql
-- Reverse string order
SELECT
    first_name,
    REVERSE(first_name) AS reversed_name
FROM employees;

-- Check for palindromes
SELECT product_name
FROM products
WHERE product_name = REVERSE(product_name);
```

### Additional String Functions

```sql
-- LOCATE/POSITION - Find substring position
SELECT email, LOCATE('@', email) AS at_position
FROM employees WHERE email IS NOT NULL;

-- LPAD/RPAD - Pad strings
SELECT
    employee_id,
    LPAD(employee_id, 5, '0') AS padded_id  -- '00001'
FROM employees;

-- REPEAT - Repeat string
SELECT REPEAT('*', 5) AS stars;  -- '*****'
```

---

## 3. Date/Time Functions

### NOW(), CURDATE(), CURTIME() - Current Date/Time

```sql
-- Get current date and time
SELECT
    NOW() AS current_datetime,      -- '2024-03-15 14:30:45'
    CURDATE() AS current_date,      -- '2024-03-15'
    CURTIME() AS current_time,      -- '14:30:45'
    CURRENT_TIMESTAMP AS timestamp; -- '2024-03-15 14:30:45'

-- Use in calculations
SELECT
    first_name,
    hire_date,
    DATEDIFF(CURDATE(), hire_date) AS days_employed
FROM employees;
```

### DATE_ADD and DATE_SUB - Date Arithmetic

```sql
-- Add time intervals
SELECT
    order_date,
    DATE_ADD(order_date, INTERVAL 30 DAY) AS due_date,
    DATE_ADD(order_date, INTERVAL 1 MONTH) AS next_month,
    DATE_ADD(order_date, INTERVAL 1 YEAR) AS next_year
FROM orders;

-- Subtract time intervals
SELECT
    CURDATE() AS today,
    DATE_SUB(CURDATE(), INTERVAL 7 DAY) AS week_ago,
    DATE_SUB(CURDATE(), INTERVAL 3 MONTH) AS three_months_ago;

-- Complex intervals
SELECT DATE_ADD(NOW(), INTERVAL '1 2:30:00' DAY_SECOND) AS future_time;
-- Adds 1 day, 2 hours, 30 minutes
```

### DATEDIFF - Calculate Date Difference

```sql
-- Days between dates
SELECT
    order_id,
    order_date,
    ship_date,
    DATEDIFF(ship_date, order_date) AS processing_days
FROM orders
WHERE ship_date IS NOT NULL;

-- Employee tenure
SELECT
    first_name,
    hire_date,
    DATEDIFF(CURDATE(), hire_date) AS total_days,
    ROUND(DATEDIFF(CURDATE(), hire_date) / 365.25, 1) AS years_employed
FROM employees;
```

### YEAR, MONTH, DAY - Extract Components

```sql
-- Extract date parts
SELECT
    order_date,
    YEAR(order_date) AS order_year,
    MONTH(order_date) AS order_month,
    DAY(order_date) AS order_day,
    DAYNAME(order_date) AS day_name,
    MONTHNAME(order_date) AS month_name,
    QUARTER(order_date) AS quarter,
    WEEK(order_date) AS week_number,
    DAYOFWEEK(order_date) AS day_of_week,
    DAYOFYEAR(order_date) AS day_of_year
FROM orders;

-- Group by month
SELECT
    YEAR(order_date) AS year,
    MONTH(order_date) AS month,
    COUNT(*) AS order_count,
    SUM(total_amount) AS total_sales
FROM orders
GROUP BY YEAR(order_date), MONTH(order_date)
ORDER BY year, month;
```

### DATE_FORMAT - Custom Formatting

```sql
-- Format dates for display
SELECT
    order_date,
    DATE_FORMAT(order_date, '%Y-%m-%d') AS iso_date,
    DATE_FORMAT(order_date, '%M %d, %Y') AS long_date,
    DATE_FORMAT(order_date, '%m/%d/%Y') AS us_date,
    DATE_FORMAT(order_date, '%d-%b-%Y') AS short_date,
    DATE_FORMAT(order_date, '%W, %M %e, %Y') AS full_date,
    DATE_FORMAT(order_date, '%H:%i:%s') AS time_only,
    DATE_FORMAT(order_date, '%h:%i %p') AS time_12hr
FROM orders;
```

**Common Format Specifiers:**
| Specifier | Description | Example |
|-----------|-------------|---------|
| %Y | 4-digit year | 2024 |
| %y | 2-digit year | 24 |
| %M | Month name | January |
| %m | Month number (01-12) | 01 |
| %d | Day (01-31) | 15 |
| %e | Day (1-31) | 15 |
| %W | Weekday name | Monday |
| %H | Hour (00-23) | 14 |
| %h | Hour (01-12) | 02 |
| %i | Minutes | 30 |
| %s | Seconds | 45 |
| %p | AM/PM | PM |

### Time Functions

```sql
-- Extract time components
SELECT
    order_date,
    HOUR(order_date) AS hour,
    MINUTE(order_date) AS minute,
    SECOND(order_date) AS second,
    TIME(order_date) AS time_portion
FROM orders;

-- Time differences
SELECT TIMEDIFF('14:30:00', '09:15:00') AS time_diff;  -- '05:15:00'
```

---

## 4. Type Casting and Conversion

### CAST Function

```sql
-- CAST(expression AS data_type)
SELECT
    order_id,
    CAST(total_amount AS SIGNED) AS amount_int,
    CAST(total_amount AS CHAR(10)) AS amount_string,
    CAST(order_date AS DATE) AS date_only,
    CAST('2024-03-15' AS DATETIME) AS string_to_datetime
FROM orders;

-- Useful conversions
SELECT
    CAST('123.45' AS DECIMAL(10,2)) AS string_to_decimal,
    CAST(12345 AS CHAR) AS int_to_string,
    CAST('2024-03-15 14:30:00' AS DATE) AS datetime_to_date;
```

### CONVERT Function

```sql
-- CONVERT(expression, type) or CONVERT(expression USING charset)
SELECT
    order_id,
    CONVERT(total_amount, SIGNED) AS amount_int,
    CONVERT(total_amount, CHAR) AS amount_string;

-- Character set conversion
SELECT CONVERT('Hello' USING utf8mb4) AS utf8_string;
```

### Implicit vs Explicit Conversion

```sql
-- Implicit conversion (automatic)
SELECT
    '10' + 5 AS implicit_add,          -- 15 (string '10' converted to number)
    CONCAT(100, ' items') AS implicit_concat;  -- '100 items'

-- Explicit conversion (recommended for clarity)
SELECT
    CAST('10' AS SIGNED) + 5 AS explicit_add,
    CONCAT(CAST(100 AS CHAR), ' items') AS explicit_concat;

-- Pitfalls of implicit conversion
SELECT '10.5abc' + 0;  -- Returns 10.5 (stops at non-numeric)
SELECT 'abc10' + 0;    -- Returns 0 (starts with non-numeric)
```

### Common Type Conversions

```sql
-- String to Number
SELECT CAST('1234.56' AS DECIMAL(10,2));

-- Number to String (with formatting)
SELECT LPAD(CAST(employee_id AS CHAR), 6, '0') AS formatted_id
FROM employees;

-- Date to String
SELECT DATE_FORMAT(hire_date, '%Y%m%d') AS date_string
FROM employees;

-- String to Date
SELECT CAST('2024-03-15' AS DATE);
SELECT STR_TO_DATE('15/03/2024', '%d/%m/%Y');
```

---

## 5. Numeric Functions

### ROUND - Round Numbers

```sql
-- ROUND(number, decimal_places)
SELECT
    price,
    ROUND(price) AS rounded_whole,
    ROUND(price, 1) AS rounded_1dp,
    ROUND(price, -1) AS rounded_tens,  -- Round to nearest 10
    ROUND(price, -2) AS rounded_hundreds
FROM products;

-- Example values:
-- 1299.99 -> 1300, 1300.0, 1300, 1300
```

### CEILING and FLOOR - Round Up/Down

```sql
-- Always round up or down
SELECT
    price,
    CEILING(price) AS ceil_price,   -- Always rounds up
    FLOOR(price) AS floor_price     -- Always rounds down
FROM products;

-- 29.99 -> CEILING: 30, FLOOR: 29
-- 399.01 -> CEILING: 400, FLOOR: 399
```

### ABS - Absolute Value

```sql
-- Get positive value
SELECT
    ABS(-100) AS absolute_neg,      -- 100
    ABS(100) AS absolute_pos,       -- 100
    ABS(price - 100) AS diff_from_100
FROM products;
```

### MOD - Modulo (Remainder)

```sql
-- Get remainder after division
SELECT
    product_id,
    MOD(product_id, 2) AS is_odd,   -- 0 for even, 1 for odd
    MOD(stock_quantity, 10) AS remainder
FROM products;

-- Practical use: Alternate row styling
SELECT
    product_id,
    product_name,
    CASE WHEN MOD(product_id, 2) = 0 THEN 'even' ELSE 'odd' END AS row_type
FROM products;
```

### POWER and SQRT - Exponents

```sql
-- Powers and roots
SELECT
    POWER(2, 10) AS two_to_ten,     -- 1024
    POWER(price, 2) AS price_squared,
    SQRT(16) AS square_root,        -- 4
    SQRT(price) AS price_sqrt
FROM products;
```

### Other Numeric Functions

```sql
-- Various numeric operations
SELECT
    SIGN(-50) AS sign_neg,          -- -1
    SIGN(50) AS sign_pos,           -- 1
    SIGN(0) AS sign_zero,           -- 0
    TRUNCATE(1234.5678, 2) AS trunc,-- 1234.56 (no rounding)
    RAND() AS random_value,         -- Random 0-1
    FLOOR(RAND() * 100) AS random_100;  -- Random 0-99
```

---

## 6. NULL Handling Functions

### COALESCE - First Non-NULL Value

```sql
-- Returns first non-NULL argument
SELECT
    employee_id,
    first_name,
    COALESCE(email, phone, 'No Contact') AS contact_info,
    COALESCE(email, 'No Email') AS email_display
FROM employees;

-- Multiple fallbacks
SELECT
    order_id,
    COALESCE(notes, status, 'No information') AS order_info
FROM orders;
```

### IFNULL - Two-Value NULL Check (MySQL)

```sql
-- Returns second argument if first is NULL
SELECT
    employee_id,
    first_name,
    IFNULL(email, 'Not Provided') AS email_display,
    IFNULL(phone, 'N/A') AS phone_display
FROM employees;

-- Calculate with defaults
SELECT
    order_id,
    total_amount,
    IFNULL(discount_percent, 0) AS discount,
    total_amount * (1 - IFNULL(discount_percent, 0) / 100) AS final_amount
FROM orders;
```

### NULLIF - Return NULL on Match

```sql
-- Returns NULL if both arguments are equal
SELECT
    product_id,
    product_name,
    stock_quantity,
    NULLIF(stock_quantity, 0) AS stock_or_null  -- NULL if stock is 0
FROM products;

-- Avoid division by zero
SELECT
    order_id,
    total_amount,
    discount_percent,
    total_amount / NULLIF(discount_percent, 0) AS amount_per_discount_point
FROM orders;
```

### Combined NULL Handling

```sql
-- Complex NULL handling scenarios
SELECT
    employee_id,
    first_name,
    last_name,
    -- Build contact string with available info
    CONCAT_WS(' | ',
        NULLIF(email, ''),
        NULLIF(phone, '')
    ) AS contact_methods,
    -- Calculate with safe defaults
    salary * (1 + COALESCE(
        CASE WHEN status = 'active' THEN 0.10
             WHEN status = 'probation' THEN 0.05
             ELSE NULL
        END, 0)) AS adjusted_salary
FROM employees;
```

---

## Function Demonstrations

### Demonstration 1: Employee Contact Directory

```sql
-- Create a formatted employee directory
SELECT
    LPAD(employee_id, 4, '0') AS emp_id,
    CONCAT(UPPER(last_name), ', ', first_name) AS name,
    COALESCE(
        CONCAT('Email: ', LOWER(email)),
        CONCAT('Phone: ', REPLACE(phone, '-', '.')),
        'No contact available'
    ) AS contact,
    CASE
        WHEN DATEDIFF(CURDATE(), hire_date) > 1825 THEN 'Veteran (5+ yrs)'
        WHEN DATEDIFF(CURDATE(), hire_date) > 365 THEN 'Experienced'
        ELSE 'New Hire'
    END AS tenure_status
FROM employees
ORDER BY last_name;
```

### Demonstration 2: Order Processing Report

```sql
-- Comprehensive order analysis
SELECT
    order_id,
    customer_name,
    DATE_FORMAT(order_date, '%b %d, %Y') AS order_date_fmt,
    COALESCE(
        DATE_FORMAT(ship_date, '%b %d, %Y'),
        'Not Shipped'
    ) AS ship_date_fmt,
    CASE
        WHEN ship_date IS NULL THEN 'Awaiting'
        WHEN DATEDIFF(ship_date, order_date) <= 2 THEN 'Express'
        WHEN DATEDIFF(ship_date, order_date) <= 5 THEN 'Standard'
        ELSE 'Delayed'
    END AS shipping_speed,
    CONCAT('$', FORMAT(total_amount, 2)) AS order_total,
    CONCAT(COALESCE(discount_percent, 0), '%') AS discount,
    CONCAT('$', FORMAT(
        total_amount * (1 - COALESCE(discount_percent, 0) / 100), 2
    )) AS final_amount
FROM orders
ORDER BY order_date DESC;
```

### Demonstration 3: Product Inventory Analysis

```sql
-- Stock level analysis with recommendations
SELECT
    product_id,
    CONCAT(
        LEFT(product_name, 15),
        CASE WHEN LENGTH(product_name) > 15 THEN '...' ELSE '' END
    ) AS product_short,
    category,
    CONCAT('$', FORMAT(price, 2)) AS price,
    stock_quantity,
    CASE
        WHEN stock_quantity = 0 THEN 'OUT OF STOCK'
        WHEN stock_quantity < 50 THEN 'Low Stock'
        WHEN stock_quantity < 100 THEN 'Moderate'
        ELSE 'Well Stocked'
    END AS stock_status,
    CONCAT('$', FORMAT(price * stock_quantity, 2)) AS inventory_value,
    DATEDIFF(CURDATE(), created_at) AS days_in_catalog
FROM products
ORDER BY stock_quantity ASC;
```

### Demonstration 4: Email Domain Analysis

```sql
-- Analyze email patterns
SELECT
    employee_id,
    email,
    SUBSTRING(email, 1, LOCATE('@', email) - 1) AS username,
    SUBSTRING(email, LOCATE('@', email) + 1) AS domain,
    LENGTH(SUBSTRING(email, 1, LOCATE('@', email) - 1)) AS username_length,
    CASE
        WHEN LOCATE('.', SUBSTRING(email, 1, LOCATE('@', email) - 1)) > 0
        THEN 'Full Name Format'
        ELSE 'Short Format'
    END AS email_format
FROM employees
WHERE email IS NOT NULL;
```

### Demonstration 5: Salary Quartile Analysis

```sql
-- Categorize salaries into quartiles
SELECT
    employee_id,
    first_name,
    last_name,
    salary,
    CASE
        WHEN salary >= (SELECT ROUND(AVG(salary) + STDDEV(salary), 2) FROM employees) THEN 'Q4 - Top'
        WHEN salary >= (SELECT AVG(salary) FROM employees) THEN 'Q3 - Above Avg'
        WHEN salary >= (SELECT ROUND(AVG(salary) - STDDEV(salary), 2) FROM employees) THEN 'Q2 - Below Avg'
        ELSE 'Q1 - Bottom'
    END AS salary_quartile,
    ROUND((salary / (SELECT AVG(salary) FROM employees) - 1) * 100, 1) AS pct_from_avg
FROM employees
ORDER BY salary DESC;
```

### Demonstration 6: Date-Based Reporting

```sql
-- Monthly sales summary with formatting
SELECT
    DATE_FORMAT(order_date, '%Y-%m') AS month,
    MONTHNAME(order_date) AS month_name,
    COUNT(*) AS total_orders,
    SUM(CASE WHEN status = 'delivered' THEN 1 ELSE 0 END) AS delivered,
    SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) AS pending,
    CONCAT('$', FORMAT(SUM(total_amount), 2)) AS gross_sales,
    CONCAT('$', FORMAT(
        SUM(total_amount * (1 - COALESCE(discount_percent, 0) / 100)), 2
    )) AS net_sales,
    CONCAT(
        ROUND(AVG(COALESCE(discount_percent, 0)), 1), '%'
    ) AS avg_discount
FROM orders
GROUP BY DATE_FORMAT(order_date, '%Y-%m'), MONTHNAME(order_date)
ORDER BY month;
```

### Demonstration 7: Phone Number Formatting

```sql
-- Clean and format phone numbers
SELECT
    employee_id,
    first_name,
    phone AS original_phone,
    COALESCE(
        CONCAT(
            '(',
            SUBSTRING(REPLACE(phone, '-', ''), 1, 3),
            ') ',
            SUBSTRING(REPLACE(phone, '-', ''), 4, 3),
            '-',
            SUBSTRING(REPLACE(phone, '-', ''), 7, 4)
        ),
        'No Phone'
    ) AS formatted_phone
FROM employees;
```

### Demonstration 8: Product Price Tiers

```sql
-- Create price tiers with rounding
SELECT
    product_id,
    product_name,
    price,
    FLOOR(price / 50) * 50 AS price_tier_floor,
    CEILING(price / 50) * 50 AS price_tier_ceiling,
    ROUND(price, -1) AS rounded_to_10,
    CASE
        WHEN price < 50 THEN 'Budget'
        WHEN price < 100 THEN 'Standard'
        WHEN price < 500 THEN 'Premium'
        ELSE 'Luxury'
    END AS price_category
FROM products
ORDER BY price;
```

### Demonstration 9: Employee Anniversary Tracking

```sql
-- Find upcoming work anniversaries
SELECT
    employee_id,
    CONCAT(first_name, ' ', last_name) AS employee_name,
    hire_date,
    DATE_FORMAT(hire_date, '%M %d') AS anniversary_date,
    YEAR(CURDATE()) - YEAR(hire_date) AS years_of_service,
    DATEDIFF(
        DATE_ADD(hire_date,
            INTERVAL (YEAR(CURDATE()) - YEAR(hire_date) +
                CASE WHEN DAYOFYEAR(hire_date) < DAYOFYEAR(CURDATE())
                     THEN 1 ELSE 0 END) YEAR),
        CURDATE()
    ) AS days_until_anniversary
FROM employees
WHERE status = 'active'
ORDER BY DAYOFYEAR(hire_date);
```

### Demonstration 10: Order Value Classification

```sql
-- Classify and analyze orders
SELECT
    order_id,
    customer_name,
    total_amount,
    CASE
        WHEN total_amount >= 5000 THEN 'Enterprise'
        WHEN total_amount >= 2000 THEN 'Business'
        WHEN total_amount >= 1000 THEN 'Professional'
        ELSE 'Standard'
    END AS order_tier,
    CONCAT(
        REPEAT('*',
            LEAST(CEILING(total_amount / 1000), 10)
        ),
        ' (', CEILING(total_amount / 1000), ')'
    ) AS visual_scale,
    ROUND(total_amount / (SELECT AVG(total_amount) FROM orders) * 100, 0) AS pct_of_avg
FROM orders
ORDER BY total_amount DESC;
```

### Demonstration 11: Data Cleansing Pipeline

```sql
-- Clean and standardize customer names
SELECT
    order_id,
    customer_name AS original,
    TRIM(
        REPLACE(
            REPLACE(
                REPLACE(customer_name, '  ', ' '),
                'Corp', 'Corporation'
            ),
            'Inc', 'Incorporated'
        )
    ) AS standardized,
    UPPER(LEFT(customer_name, 1)) AS first_initial,
    LENGTH(TRIM(customer_name)) AS name_length
FROM orders;
```

### Demonstration 12: Time-of-Day Analysis

```sql
-- Analyze orders by time of day
SELECT
    order_id,
    customer_name,
    order_date,
    DATE_FORMAT(order_date, '%H:%i') AS order_time,
    CASE
        WHEN HOUR(order_date) < 9 THEN 'Early Morning'
        WHEN HOUR(order_date) < 12 THEN 'Morning'
        WHEN HOUR(order_date) < 14 THEN 'Lunch'
        WHEN HOUR(order_date) < 17 THEN 'Afternoon'
        ELSE 'Evening'
    END AS time_slot,
    CASE
        WHEN DAYOFWEEK(order_date) IN (1, 7) THEN 'Weekend'
        ELSE 'Weekday'
    END AS day_type
FROM orders
ORDER BY HOUR(order_date);
```

### Demonstration 13: Dynamic Status Messages

```sql
-- Generate contextual status messages
SELECT
    order_id,
    customer_name,
    status,
    ship_date,
    CASE status
        WHEN 'pending' THEN
            CONCAT('Order awaiting processing since ',
                   DATE_FORMAT(order_date, '%b %d'))
        WHEN 'shipped' THEN
            CONCAT('Shipped on ',
                   DATE_FORMAT(ship_date, '%b %d'),
                   ' (', DATEDIFF(CURDATE(), ship_date), ' days ago)')
        WHEN 'delivered' THEN
            CONCAT('Successfully delivered on ',
                   DATE_FORMAT(ship_date, '%b %d'))
        WHEN 'cancelled' THEN
            CONCAT('Cancelled: ', COALESCE(notes, 'No reason provided'))
        ELSE 'Status unknown'
    END AS status_message
FROM orders;
```

### Demonstration 14: Inventory Value Formatting

```sql
-- Format large numbers for readability
SELECT
    category,
    COUNT(*) AS product_count,
    SUM(stock_quantity) AS total_units,
    SUM(price * stock_quantity) AS raw_value,
    CASE
        WHEN SUM(price * stock_quantity) >= 100000
        THEN CONCAT('$', FORMAT(SUM(price * stock_quantity) / 1000, 1), 'K')
        ELSE CONCAT('$', FORMAT(SUM(price * stock_quantity), 0))
    END AS formatted_value
FROM products
GROUP BY category
ORDER BY SUM(price * stock_quantity) DESC;
```

### Demonstration 15: Complex Search Patterns

```sql
-- Find products matching multiple criteria
SELECT
    product_id,
    product_name,
    category,
    price,
    CASE
        WHEN LOWER(product_name) LIKE '%pro%' AND price > 1000 THEN 'Premium Pro'
        WHEN LOWER(product_name) LIKE '%pro%' THEN 'Pro Line'
        WHEN LOWER(category) = 'electronics' THEN 'Electronics'
        WHEN price < 50 THEN 'Budget Item'
        ELSE 'Standard'
    END AS product_classification,
    CONCAT(
        UPPER(LEFT(category, 3)), '-',
        LPAD(product_id, 4, '0')
    ) AS sku_code
FROM products;
```

---

## Practice Questions

### Question 1: Employee Compensation Report
**Difficulty: Medium**

Create a report showing each employee's compensation analysis including:
- Full name in "LAST, First" format
- Current salary formatted as currency
- A salary grade (A: 100k+, B: 75k-99k, C: 50k-74k, D: below 50k)
- Years of service (rounded to 1 decimal)
- Projected salary after a merit increase (10% for active, 5% for probation, 0% for inactive)

```sql
-- Solution 1: Using CASE extensively
SELECT
    CONCAT(UPPER(last_name), ', ', first_name) AS employee_name,
    CONCAT('$', FORMAT(salary, 2)) AS current_salary,
    CASE
        WHEN salary >= 100000 THEN 'A'
        WHEN salary >= 75000 THEN 'B'
        WHEN salary >= 50000 THEN 'C'
        ELSE 'D'
    END AS salary_grade,
    ROUND(DATEDIFF(CURDATE(), hire_date) / 365.25, 1) AS years_of_service,
    CONCAT('$', FORMAT(
        salary * (1 + CASE
            WHEN status = 'active' THEN 0.10
            WHEN status = 'probation' THEN 0.05
            ELSE 0
        END), 2
    )) AS projected_salary
FROM employees
ORDER BY salary DESC;

-- Solution 2: Using subquery for salary grades
SELECT
    CONCAT(UPPER(last_name), ', ', first_name) AS employee_name,
    CONCAT('$', FORMAT(salary, 2)) AS current_salary,
    (SELECT
        CASE
            WHEN e.salary >= 100000 THEN 'A'
            WHEN e.salary >= 75000 THEN 'B'
            WHEN e.salary >= 50000 THEN 'C'
            ELSE 'D'
        END
    ) AS salary_grade,
    ROUND(DATEDIFF(CURDATE(), hire_date) / 365.25, 1) AS years_of_service,
    CONCAT('$', FORMAT(salary * (1 + COALESCE(
        NULLIF(
            CASE status
                WHEN 'active' THEN 0.10
                WHEN 'probation' THEN 0.05
            END, NULL
        ), 0)), 2
    )) AS projected_salary
FROM employees e
ORDER BY salary DESC;
```

---

### Question 2: Order Fulfillment Dashboard
**Difficulty: Medium-Hard**

Build an order dashboard showing:
- Order ID and customer (first 20 chars)
- Order date in "Mon DD, YYYY" format
- Days to ship (or "Pending" if not shipped)
- Final amount after discount
- Performance rating (Excellent: <=2 days, Good: 3-4 days, Needs Improvement: 5+ days, N/A: not shipped)

```sql
-- Solution 1: Direct approach
SELECT
    order_id,
    LEFT(customer_name, 20) AS customer,
    DATE_FORMAT(order_date, '%b %d, %Y') AS order_date,
    COALESCE(
        CAST(DATEDIFF(ship_date, order_date) AS CHAR),
        'Pending'
    ) AS days_to_ship,
    CONCAT('$', FORMAT(
        total_amount * (1 - COALESCE(discount_percent, 0) / 100), 2
    )) AS final_amount,
    CASE
        WHEN ship_date IS NULL THEN 'N/A'
        WHEN DATEDIFF(ship_date, order_date) <= 2 THEN 'Excellent'
        WHEN DATEDIFF(ship_date, order_date) <= 4 THEN 'Good'
        ELSE 'Needs Improvement'
    END AS performance_rating
FROM orders
ORDER BY order_date DESC;

-- Solution 2: Using CTEs for clarity
WITH order_metrics AS (
    SELECT
        order_id,
        customer_name,
        order_date,
        ship_date,
        total_amount,
        COALESCE(discount_percent, 0) AS discount_pct,
        DATEDIFF(ship_date, order_date) AS ship_days
    FROM orders
)
SELECT
    order_id,
    LEFT(customer_name, 20) AS customer,
    DATE_FORMAT(order_date, '%b %d, %Y') AS order_date,
    IFNULL(CAST(ship_days AS CHAR), 'Pending') AS days_to_ship,
    CONCAT('$', FORMAT(total_amount * (1 - discount_pct / 100), 2)) AS final_amount,
    CASE
        WHEN ship_days IS NULL THEN 'N/A'
        WHEN ship_days <= 2 THEN 'Excellent'
        WHEN ship_days <= 4 THEN 'Good'
        ELSE 'Needs Improvement'
    END AS performance_rating
FROM order_metrics
ORDER BY order_date DESC;
```

---

### Question 3: Product SKU Generator
**Difficulty: Medium**

Generate product SKUs with the following format:
- First 3 letters of category (uppercase)
- Hyphen
- Product ID padded to 4 digits
- Hyphen
- First letter of each word in product name (max 4 letters)

Example: "Laptop Pro 15" in Electronics = "ELE-0001-LP1"

```sql
-- Solution 1: Using string functions
SELECT
    product_id,
    product_name,
    category,
    CONCAT(
        UPPER(LEFT(category, 3)),
        '-',
        LPAD(product_id, 4, '0'),
        '-',
        UPPER(
            CONCAT(
                LEFT(product_name, 1),
                COALESCE(
                    SUBSTRING(product_name,
                        LOCATE(' ', product_name) + 1, 1),
                    ''
                ),
                COALESCE(
                    SUBSTRING(product_name,
                        LOCATE(' ', product_name,
                            LOCATE(' ', product_name) + 1) + 1, 1),
                    ''
                )
            )
        )
    ) AS sku
FROM products;

-- Solution 2: Simplified version
SELECT
    product_id,
    product_name,
    category,
    CONCAT(
        UPPER(LEFT(category, 3)), '-',
        LPAD(product_id, 4, '0'), '-',
        UPPER(LEFT(REPLACE(product_name, ' ', ''), 3))
    ) AS sku_simple
FROM products;
```

---

### Question 4: Employee Contact Validation
**Difficulty: Medium-Hard**

Create a report that validates and categorizes employee contact information:
- Identify if email is valid (contains @ and .)
- Format phone as (XXX) XXX-XXXX
- Show contact quality score (2 points each for valid email and phone, 1 point for partial)
- Recommend action based on score

```sql
-- Solution
SELECT
    employee_id,
    first_name,
    last_name,
    email,
    phone,
    -- Email validation
    CASE
        WHEN email IS NULL THEN 'Missing'
        WHEN email LIKE '%@%.%' THEN 'Valid'
        ELSE 'Invalid'
    END AS email_status,
    -- Phone formatting
    CASE
        WHEN phone IS NULL THEN 'Missing'
        ELSE CONCAT(
            '(',
            LEFT(REPLACE(phone, '-', ''), 3),
            ') ',
            SUBSTRING(REPLACE(phone, '-', ''), 4, 3),
            '-',
            RIGHT(REPLACE(phone, '-', ''), 4)
        )
    END AS formatted_phone,
    -- Contact quality score
    (
        CASE WHEN email LIKE '%@%.%' THEN 2
             WHEN email IS NOT NULL THEN 1
             ELSE 0 END +
        CASE WHEN phone IS NOT NULL THEN 2 ELSE 0 END
    ) AS contact_score,
    -- Recommended action
    CASE
        WHEN email IS NULL AND phone IS NULL THEN 'URGENT: Collect all contact info'
        WHEN email IS NULL THEN 'Request email address'
        WHEN phone IS NULL THEN 'Request phone number'
        WHEN email NOT LIKE '%@%.%' THEN 'Verify email format'
        ELSE 'Contact info complete'
    END AS recommended_action
FROM employees
ORDER BY
    (CASE WHEN email LIKE '%@%.%' THEN 2
          WHEN email IS NOT NULL THEN 1
          ELSE 0 END +
     CASE WHEN phone IS NOT NULL THEN 2 ELSE 0 END);
```

---

### Question 5: Monthly Sales Trend Analysis
**Difficulty: Hard**

Create a monthly sales report showing:
- Month and year
- Total orders and revenue
- Average order value
- Month-over-month change indicator (UP, DOWN, SAME)
- Revenue formatted with K suffix for thousands

```sql
-- Solution using window functions (if available) or self-join
SELECT
    DATE_FORMAT(order_date, '%Y-%m') AS month,
    MONTHNAME(order_date) AS month_name,
    COUNT(*) AS total_orders,
    CONCAT(
        '$',
        CASE
            WHEN SUM(total_amount) >= 1000
            THEN CONCAT(FORMAT(SUM(total_amount)/1000, 1), 'K')
            ELSE FORMAT(SUM(total_amount), 0)
        END
    ) AS revenue,
    CONCAT('$', FORMAT(AVG(total_amount), 2)) AS avg_order,
    -- Compare to previous month (simplified)
    CASE
        WHEN MONTH(order_date) = 1 THEN 'N/A'
        WHEN SUM(total_amount) >
            (SELECT SUM(o2.total_amount)
             FROM orders o2
             WHERE DATE_FORMAT(o2.order_date, '%Y-%m') =
                   DATE_FORMAT(DATE_SUB(order_date, INTERVAL 1 MONTH), '%Y-%m'))
        THEN 'UP'
        WHEN SUM(total_amount) <
            (SELECT SUM(o2.total_amount)
             FROM orders o2
             WHERE DATE_FORMAT(o2.order_date, '%Y-%m') =
                   DATE_FORMAT(DATE_SUB(order_date, INTERVAL 1 MONTH), '%Y-%m'))
        THEN 'DOWN'
        ELSE 'SAME'
    END AS trend
FROM orders
GROUP BY DATE_FORMAT(order_date, '%Y-%m'), MONTHNAME(order_date)
ORDER BY month;
```

---

### Question 6: Dynamic Email Builder
**Difficulty: Medium**

Generate personalized email content for each employee:
- Subject line with their name
- Greeting based on time of day (before noon = Good morning, etc.)
- Custom message based on their status
- Formatted hire anniversary date

```sql
-- Solution
SELECT
    employee_id,
    email,
    -- Subject line
    CONCAT('Important Update for ', first_name, ' ', last_name) AS subject,
    -- Greeting (assuming current time check)
    CONCAT(
        CASE
            WHEN HOUR(NOW()) < 12 THEN 'Good morning'
            WHEN HOUR(NOW()) < 17 THEN 'Good afternoon'
            ELSE 'Good evening'
        END,
        ', ', first_name, '!'
    ) AS greeting,
    -- Custom message
    CASE status
        WHEN 'active' THEN
            CONCAT('Thank you for your ',
                   FLOOR(DATEDIFF(CURDATE(), hire_date)/365),
                   ' years of dedicated service.')
        WHEN 'probation' THEN
            'We hope you are settling in well during your probationary period.'
        WHEN 'inactive' THEN
            'We look forward to your return to active status.'
        ELSE 'Please contact HR for more information.'
    END AS message_body,
    -- Anniversary info
    CONCAT(
        'Your work anniversary is on ',
        DATE_FORMAT(hire_date, '%M %D'),
        '.'
    ) AS anniversary_note
FROM employees
WHERE email IS NOT NULL;
```

---

### Question 7: Inventory Reorder Report
**Difficulty: Medium-Hard**

Create an inventory management report showing:
- Products below reorder point (50 units)
- Quantity to order (round up to nearest 25)
- Estimated cost based on price
- Priority level (Critical: 0 stock, High: 1-25, Medium: 26-50)
- Days since added to catalog

```sql
-- Solution
SELECT
    product_id,
    product_name,
    stock_quantity,
    -- Quantity to order (round up to nearest 25)
    CASE
        WHEN stock_quantity >= 50 THEN 0
        ELSE CEILING((100 - stock_quantity) / 25) * 25
    END AS order_quantity,
    -- Estimated cost
    CONCAT('$', FORMAT(
        price * CASE
            WHEN stock_quantity >= 50 THEN 0
            ELSE CEILING((100 - stock_quantity) / 25) * 25
        END, 2
    )) AS estimated_cost,
    -- Priority level
    CASE
        WHEN stock_quantity = 0 THEN 'CRITICAL'
        WHEN stock_quantity <= 25 THEN 'HIGH'
        WHEN stock_quantity <= 50 THEN 'MEDIUM'
        ELSE 'LOW'
    END AS priority,
    -- Days in catalog
    DATEDIFF(CURDATE(), created_at) AS days_in_catalog,
    -- Turnover indicator
    CASE
        WHEN stock_quantity < 50 AND DATEDIFF(CURDATE(), created_at) < 90
        THEN 'Fast Moving'
        WHEN stock_quantity < 50
        THEN 'Steady Demand'
        ELSE 'Review Needed'
    END AS turnover_status
FROM products
WHERE stock_quantity < 100
ORDER BY
    CASE
        WHEN stock_quantity = 0 THEN 1
        WHEN stock_quantity <= 25 THEN 2
        ELSE 3
    END,
    stock_quantity;
```

---

### Question 8: Customer Order Summary
**Difficulty: Hard**

Generate a comprehensive customer summary showing:
- Customer name (cleaned and standardized)
- Total orders and revenue
- Average days to ship
- Favorite order day of week
- Customer segment based on total revenue

```sql
-- Solution
SELECT
    -- Standardize customer name
    TRIM(REPLACE(REPLACE(customer_name, 'Corp', 'Corporation'), 'Inc', 'Incorporated')) AS customer,
    COUNT(*) AS total_orders,
    CONCAT('$', FORMAT(SUM(total_amount), 2)) AS total_revenue,
    -- Average shipping days
    CONCAT(
        COALESCE(
            ROUND(AVG(DATEDIFF(ship_date, order_date)), 1),
            'N/A'
        ),
        ' days'
    ) AS avg_ship_time,
    -- Most common order day
    (SELECT DAYNAME(o2.order_date)
     FROM orders o2
     WHERE o2.customer_name = orders.customer_name
     GROUP BY DAYNAME(o2.order_date)
     ORDER BY COUNT(*) DESC
     LIMIT 1) AS favorite_day,
    -- Customer segment
    CASE
        WHEN SUM(total_amount) >= 10000 THEN 'Enterprise'
        WHEN SUM(total_amount) >= 5000 THEN 'Business'
        WHEN SUM(total_amount) >= 2000 THEN 'Growth'
        ELSE 'Starter'
    END AS customer_segment,
    -- Engagement score
    CONCAT(
        ROUND(
            (COUNT(*) * 10 + SUM(total_amount) / 1000) /
            GREATEST(DATEDIFF(CURDATE(), MIN(order_date)), 1) * 30,
            1
        ),
        '/100'
    ) AS engagement_score
FROM orders
GROUP BY customer_name
ORDER BY SUM(total_amount) DESC;
```

---

### Question 9: Employee Hire Date Analysis
**Difficulty: Medium**

Analyze hiring patterns:
- Show hire month and year
- Count of hires
- Day of week hired
- Season (Q1=Winter, Q2=Spring, Q3=Summer, Q4=Fall)
- Average starting salary for that period

```sql
-- Solution
SELECT
    DATE_FORMAT(hire_date, '%Y-%m') AS hire_period,
    MONTHNAME(hire_date) AS month,
    COUNT(*) AS hires,
    -- Most common hire day
    GROUP_CONCAT(DISTINCT DAYNAME(hire_date)) AS hire_days,
    -- Season
    CASE
        WHEN MONTH(hire_date) IN (12, 1, 2) THEN 'Winter'
        WHEN MONTH(hire_date) IN (3, 4, 5) THEN 'Spring'
        WHEN MONTH(hire_date) IN (6, 7, 8) THEN 'Summer'
        ELSE 'Fall'
    END AS season,
    -- Quarter
    CONCAT('Q', QUARTER(hire_date)) AS quarter,
    -- Average salary
    CONCAT('$', FORMAT(AVG(salary), 2)) AS avg_starting_salary,
    -- Salary range
    CONCAT(
        '$', FORMAT(MIN(salary), 0),
        ' - $', FORMAT(MAX(salary), 0)
    ) AS salary_range
FROM employees
GROUP BY
    DATE_FORMAT(hire_date, '%Y-%m'),
    MONTHNAME(hire_date),
    CASE
        WHEN MONTH(hire_date) IN (12, 1, 2) THEN 'Winter'
        WHEN MONTH(hire_date) IN (3, 4, 5) THEN 'Spring'
        WHEN MONTH(hire_date) IN (6, 7, 8) THEN 'Summer'
        ELSE 'Fall'
    END,
    QUARTER(hire_date)
ORDER BY hire_period;
```

---

### Question 10: Complete Order Processing Pipeline
**Difficulty: Hard**

Create a comprehensive order processing view with:
- All monetary values formatted consistently
- Dates in multiple formats
- Status with color codes (for UI: Green/Yellow/Red)
- Processing metrics
- Dynamic notes generation

```sql
-- Solution
SELECT
    -- Order identification
    CONCAT('ORD-', LPAD(order_id, 6, '0')) AS order_number,
    customer_name,

    -- Multiple date formats
    DATE_FORMAT(order_date, '%Y-%m-%d') AS order_date_iso,
    DATE_FORMAT(order_date, '%b %d, %Y %h:%i %p') AS order_date_display,
    COALESCE(DATE_FORMAT(ship_date, '%b %d, %Y'), 'Not Shipped') AS ship_date_display,

    -- Monetary values
    CONCAT('$', FORMAT(total_amount, 2)) AS subtotal,
    CONCAT(COALESCE(discount_percent, 0), '%') AS discount,
    CONCAT('$', FORMAT(
        total_amount * COALESCE(discount_percent, 0) / 100, 2
    )) AS discount_amount,
    CONCAT('$', FORMAT(
        total_amount * (1 - COALESCE(discount_percent, 0) / 100), 2
    )) AS final_total,

    -- Status with color codes
    status,
    CASE status
        WHEN 'delivered' THEN 'GREEN'
        WHEN 'shipped' THEN 'BLUE'
        WHEN 'pending' THEN 'YELLOW'
        WHEN 'cancelled' THEN 'RED'
        ELSE 'GRAY'
    END AS status_color,

    -- Processing metrics
    CASE
        WHEN ship_date IS NOT NULL
        THEN CONCAT(DATEDIFF(ship_date, order_date), ' days')
        WHEN status = 'cancelled' THEN 'Cancelled'
        ELSE CONCAT(DATEDIFF(CURDATE(), order_date), ' days waiting')
    END AS processing_time,

    -- Age of order
    CASE
        WHEN DATEDIFF(CURDATE(), order_date) > 30 THEN 'Over 30 days'
        WHEN DATEDIFF(CURDATE(), order_date) > 7 THEN 'Over 1 week'
        ELSE 'Recent'
    END AS order_age,

    -- Dynamic notes
    CONCAT_WS(' | ',
        COALESCE(notes, ''),
        CASE WHEN discount_percent >= 15 THEN 'HIGH DISCOUNT' END,
        CASE WHEN total_amount >= 5000 THEN 'LARGE ORDER' END,
        CASE WHEN status = 'pending' AND DATEDIFF(CURDATE(), order_date) > 7
             THEN 'NEEDS ATTENTION' END
    ) AS system_notes

FROM orders
ORDER BY
    CASE status
        WHEN 'pending' THEN 1
        WHEN 'shipped' THEN 2
        WHEN 'delivered' THEN 3
        ELSE 4
    END,
    order_date DESC;
```

---

## Common Mistakes

### CASE Statement Mistakes

| Mistake | Example | Problem | Correct Version |
|---------|---------|---------|-----------------|
| Missing ELSE | `CASE WHEN x=1 THEN 'A' END` | Returns NULL for unmatched | Add `ELSE 'Unknown' END` |
| Wrong order | `WHEN salary > 50000... WHEN salary > 100000` | First match wins | Order from highest to lowest |
| Simple vs Searched confusion | `CASE col WHEN > 10 THEN...` | Can't use operators in simple CASE | Use searched CASE |
| Forgetting END | `CASE WHEN x=1 THEN 'A'` | Syntax error | Add `END` keyword |

```sql
-- WRONG: Conditions in wrong order
CASE WHEN salary > 50000 THEN 'Mid'
     WHEN salary > 100000 THEN 'High'  -- Never reached!
END

-- CORRECT: Highest condition first
CASE WHEN salary > 100000 THEN 'High'
     WHEN salary > 50000 THEN 'Mid'
END
```

### String Function Mistakes

| Mistake | Example | Problem | Correct Version |
|---------|---------|---------|-----------------|
| 1-based indexing confusion | `SUBSTRING(str, 0, 3)` | SQL uses 1-based indexing | `SUBSTRING(str, 1, 3)` |
| NULL concatenation | `CONCAT(NULL, 'text')` | Returns NULL in some DBs | Use `CONCAT_WS` or `COALESCE` |
| Case sensitivity | `WHERE name = 'john'` | May not match 'John' | Use `LOWER()` or `UPPER()` |
| LOCATE returns 0 | `SUBSTRING(s, LOCATE('@', s))` | Returns whole string if not found | Check for 0 first |

```sql
-- WRONG: SUBSTRING with 0 index
SELECT SUBSTRING('Hello', 0, 2);  -- Returns empty or 'H' depending on DB

-- CORRECT: Start from 1
SELECT SUBSTRING('Hello', 1, 2);  -- Returns 'He'
```

### Date Function Mistakes

| Mistake | Example | Problem | Correct Version |
|---------|---------|---------|-----------------|
| Wrong date format | `STR_TO_DATE('2024-01-15', '%d-%m-%Y')` | Format doesn't match string | Match format to string |
| DATEDIFF order | `DATEDIFF(start_date, end_date)` | Returns negative | Put later date first |
| Missing time component | `WHERE order_date = '2024-01-15'` | Won't match datetime | Use `DATE()` or range |
| Invalid date arithmetic | `date + 30` | Doesn't work in all DBs | Use `DATE_ADD()` |

```sql
-- WRONG: Comparing datetime to date
WHERE order_date = '2024-01-15'  -- Misses times other than 00:00:00

-- CORRECT: Use DATE() or range
WHERE DATE(order_date) = '2024-01-15'
-- OR
WHERE order_date >= '2024-01-15' AND order_date < '2024-01-16'
```

### Type Casting Mistakes

| Mistake | Example | Problem | Correct Version |
|---------|---------|---------|-----------------|
| Precision loss | `CAST(123.456 AS DECIMAL(3,1))` | Loses precision | Use adequate precision |
| Implicit conversion | `'10' + '5'` | May concatenate or add | Be explicit |
| Invalid cast | `CAST('abc' AS SIGNED)` | Returns 0 or error | Validate data first |
| Overflow | `CAST(999999 AS SIGNED)` | May overflow | Use appropriate type |

```sql
-- WRONG: Relying on implicit conversion
SELECT '10' + 5;  -- Could be 15 or '105'

-- CORRECT: Explicit conversion
SELECT CAST('10' AS SIGNED) + 5;  -- Always 15
```

### Numeric Function Mistakes

| Mistake | Example | Problem | Correct Version |
|---------|---------|---------|-----------------|
| ROUND vs TRUNCATE | `ROUND(2.5)` vs `TRUNCATE(2.5, 0)` | Different results | Choose based on need |
| Division by zero | `amount / quantity` | Error if quantity = 0 | Use `NULLIF(quantity, 0)` |
| MOD with negative | `MOD(-5, 3)` | Result varies by DB | Be aware of DB behavior |
| RAND() in ORDER BY | Multiple `RAND()` calls | Different values each call | Use single call or seed |

```sql
-- WRONG: Division by zero risk
SELECT total / discount_percent FROM orders;

-- CORRECT: Handle zero
SELECT total / NULLIF(discount_percent, 0) FROM orders;
```

### NULL Handling Mistakes

| Mistake | Example | Problem | Correct Version |
|---------|---------|---------|-----------------|
| Comparing with NULL | `WHERE email = NULL` | Always false | Use `IS NULL` |
| NOT IN with NULL | `WHERE id NOT IN (1, NULL)` | Returns no rows | Exclude NULLs or use NOT EXISTS |
| COALESCE type mismatch | `COALESCE(int_col, 'N/A')` | Type conflict | Match types |
| Aggregate ignores NULL | `AVG(nullable_col)` | Excludes NULLs | Consider `COALESCE` |

```sql
-- WRONG: Comparing with NULL
SELECT * FROM employees WHERE email = NULL;  -- Returns nothing

-- CORRECT: Use IS NULL
SELECT * FROM employees WHERE email IS NULL;

-- WRONG: NOT IN with potential NULL
SELECT * FROM orders WHERE customer_id NOT IN (SELECT id FROM inactive);

-- CORRECT: Handle NULLs
SELECT * FROM orders WHERE customer_id NOT IN (
    SELECT id FROM inactive WHERE id IS NOT NULL
);
```

---

## Function Reference Cheat Sheet

### String Functions Quick Reference

| Function | Syntax | Example | Result |
|----------|--------|---------|--------|
| CONCAT | `CONCAT(s1, s2, ...)` | `CONCAT('Hello', ' ', 'World')` | 'Hello World' |
| CONCAT_WS | `CONCAT_WS(sep, s1, s2)` | `CONCAT_WS('-', 'A', 'B', 'C')` | 'A-B-C' |
| SUBSTRING | `SUBSTRING(s, start, len)` | `SUBSTRING('Hello', 2, 3)` | 'ell' |
| LEFT | `LEFT(s, n)` | `LEFT('Hello', 2)` | 'He' |
| RIGHT | `RIGHT(s, n)` | `RIGHT('Hello', 2)` | 'lo' |
| LENGTH | `LENGTH(s)` | `LENGTH('Hello')` | 5 |
| CHAR_LENGTH | `CHAR_LENGTH(s)` | `CHAR_LENGTH('Hello')` | 5 |
| UPPER | `UPPER(s)` | `UPPER('Hello')` | 'HELLO' |
| LOWER | `LOWER(s)` | `LOWER('Hello')` | 'hello' |
| TRIM | `TRIM(s)` | `TRIM('  Hi  ')` | 'Hi' |
| LTRIM | `LTRIM(s)` | `LTRIM('  Hi')` | 'Hi' |
| RTRIM | `RTRIM(s)` | `RTRIM('Hi  ')` | 'Hi' |
| REPLACE | `REPLACE(s, from, to)` | `REPLACE('Hello', 'l', 'L')` | 'HeLLo' |
| REVERSE | `REVERSE(s)` | `REVERSE('Hello')` | 'olleH' |
| LOCATE | `LOCATE(sub, s)` | `LOCATE('l', 'Hello')` | 3 |
| LPAD | `LPAD(s, len, pad)` | `LPAD('5', 3, '0')` | '005' |
| RPAD | `RPAD(s, len, pad)` | `RPAD('5', 3, '0')` | '500' |
| REPEAT | `REPEAT(s, n)` | `REPEAT('*', 5)` | '*****' |

### Date/Time Functions Quick Reference

| Function | Syntax | Example | Result |
|----------|--------|---------|--------|
| NOW | `NOW()` | `NOW()` | '2024-03-15 14:30:00' |
| CURDATE | `CURDATE()` | `CURDATE()` | '2024-03-15' |
| CURTIME | `CURTIME()` | `CURTIME()` | '14:30:00' |
| DATE | `DATE(dt)` | `DATE('2024-03-15 14:30')` | '2024-03-15' |
| TIME | `TIME(dt)` | `TIME('2024-03-15 14:30')` | '14:30:00' |
| YEAR | `YEAR(d)` | `YEAR('2024-03-15')` | 2024 |
| MONTH | `MONTH(d)` | `MONTH('2024-03-15')` | 3 |
| DAY | `DAY(d)` | `DAY('2024-03-15')` | 15 |
| HOUR | `HOUR(t)` | `HOUR('14:30:00')` | 14 |
| MINUTE | `MINUTE(t)` | `MINUTE('14:30:00')` | 30 |
| SECOND | `SECOND(t)` | `SECOND('14:30:45')` | 45 |
| DAYNAME | `DAYNAME(d)` | `DAYNAME('2024-03-15')` | 'Friday' |
| MONTHNAME | `MONTHNAME(d)` | `MONTHNAME('2024-03-15')` | 'March' |
| DAYOFWEEK | `DAYOFWEEK(d)` | `DAYOFWEEK('2024-03-15')` | 6 |
| QUARTER | `QUARTER(d)` | `QUARTER('2024-03-15')` | 1 |
| WEEK | `WEEK(d)` | `WEEK('2024-03-15')` | 11 |
| DATE_ADD | `DATE_ADD(d, INTERVAL n unit)` | `DATE_ADD('2024-03-15', INTERVAL 1 MONTH)` | '2024-04-15' |
| DATE_SUB | `DATE_SUB(d, INTERVAL n unit)` | `DATE_SUB('2024-03-15', INTERVAL 1 WEEK)` | '2024-03-08' |
| DATEDIFF | `DATEDIFF(d1, d2)` | `DATEDIFF('2024-03-15', '2024-03-01')` | 14 |
| DATE_FORMAT | `DATE_FORMAT(d, fmt)` | `DATE_FORMAT('2024-03-15', '%M %d, %Y')` | 'March 15, 2024' |
| STR_TO_DATE | `STR_TO_DATE(s, fmt)` | `STR_TO_DATE('15-03-2024', '%d-%m-%Y')` | '2024-03-15' |

### Numeric Functions Quick Reference

| Function | Syntax | Example | Result |
|----------|--------|---------|--------|
| ROUND | `ROUND(n, d)` | `ROUND(3.567, 2)` | 3.57 |
| CEILING | `CEILING(n)` | `CEILING(3.1)` | 4 |
| FLOOR | `FLOOR(n)` | `FLOOR(3.9)` | 3 |
| TRUNCATE | `TRUNCATE(n, d)` | `TRUNCATE(3.567, 2)` | 3.56 |
| ABS | `ABS(n)` | `ABS(-5)` | 5 |
| MOD | `MOD(n, m)` | `MOD(10, 3)` | 1 |
| POWER | `POWER(n, p)` | `POWER(2, 3)` | 8 |
| SQRT | `SQRT(n)` | `SQRT(16)` | 4 |
| SIGN | `SIGN(n)` | `SIGN(-5)` | -1 |
| RAND | `RAND()` | `RAND()` | 0.123... |

### NULL Handling Functions Quick Reference

| Function | Syntax | Example | Result |
|----------|--------|---------|--------|
| COALESCE | `COALESCE(v1, v2, ...)` | `COALESCE(NULL, 'B', 'C')` | 'B' |
| IFNULL | `IFNULL(v, default)` | `IFNULL(NULL, 'N/A')` | 'N/A' |
| NULLIF | `NULLIF(v1, v2)` | `NULLIF(5, 5)` | NULL |
| IF | `IF(cond, true, false)` | `IF(1>0, 'Yes', 'No')` | 'Yes' |

### Type Casting Quick Reference

| Function | Syntax | Example | Result |
|----------|--------|---------|--------|
| CAST | `CAST(expr AS type)` | `CAST('123' AS SIGNED)` | 123 |
| CONVERT | `CONVERT(expr, type)` | `CONVERT('123', SIGNED)` | 123 |

**Common CAST Types:**
- `SIGNED` / `UNSIGNED` - Integer
- `DECIMAL(M,D)` - Decimal with precision
- `CHAR(N)` - Fixed-length string
- `DATE` - Date only
- `DATETIME` - Date and time
- `TIME` - Time only

### Date Format Specifiers

| Specifier | Description | Example |
|-----------|-------------|---------|
| `%Y` | 4-digit year | 2024 |
| `%y` | 2-digit year | 24 |
| `%M` | Full month name | January |
| `%b` | Abbreviated month | Jan |
| `%m` | Month (01-12) | 01 |
| `%c` | Month (1-12) | 1 |
| `%d` | Day (01-31) | 01 |
| `%e` | Day (1-31) | 1 |
| `%D` | Day with suffix | 1st |
| `%W` | Full weekday | Monday |
| `%a` | Abbreviated weekday | Mon |
| `%H` | Hour 24h (00-23) | 14 |
| `%h` | Hour 12h (01-12) | 02 |
| `%i` | Minutes (00-59) | 30 |
| `%s` | Seconds (00-59) | 45 |
| `%p` | AM/PM | PM |
| `%r` | Time 12h format | 02:30:45 PM |
| `%T` | Time 24h format | 14:30:45 |

---

## Summary

Advanced SQL functions transform raw data into actionable insights. Key takeaways:

1. **CASE statements** provide conditional logic within queries
2. **String functions** manipulate and format text data
3. **Date/Time functions** handle temporal calculations and formatting
4. **Type casting** ensures data compatibility and precision
5. **Numeric functions** perform mathematical operations
6. **NULL handling** prevents unexpected results from missing data

Master these functions to write more expressive, efficient, and maintainable SQL queries.

---

## Next Steps

- Practice combining multiple functions in single queries
- Explore database-specific function variations
- Learn window functions for advanced analytics (Phase 8)
- Study query optimization for function-heavy queries (Phase 9)
