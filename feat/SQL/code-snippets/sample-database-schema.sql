-- ================================================================
-- SAMPLE DATABASE SCHEMA FOR SQL LEARNING
-- ================================================================
-- This schema contains all sample tables used throughout the
-- SQL learning phases. Execute this script to set up your practice
-- environment.
-- ================================================================

-- Drop existing tables if they exist (in reverse order due to foreign keys)
DROP TABLE IF EXISTS employee_projects;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS customers;

-- ================================================================
-- TABLE 1: DEPARTMENTS
-- ================================================================
CREATE TABLE departments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    budget DECIMAL(15, 2),
    location VARCHAR(100),
    created_date DATE DEFAULT CURRENT_DATE
);

-- ================================================================
-- TABLE 2: EMPLOYEES
-- ================================================================
CREATE TABLE employees (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    department_id INT,
    salary DECIMAL(10, 2),
    hire_date DATE NOT NULL,
    manager_id INT,
    job_title VARCHAR(100),
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (manager_id) REFERENCES employees(id)
);

-- ================================================================
-- TABLE 3: PROJECTS
-- ================================================================
CREATE TABLE projects (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    budget DECIMAL(15, 2),
    status VARCHAR(20) DEFAULT 'Active',
    department_id INT,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- ================================================================
-- TABLE 4: EMPLOYEE_PROJECTS (Junction Table)
-- ================================================================
CREATE TABLE employee_projects (
    employee_id INT,
    project_id INT,
    hours_worked DECIMAL(6, 2),
    role VARCHAR(100),
    assignment_date DATE,
    PRIMARY KEY (employee_id, project_id),
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

-- ================================================================
-- TABLE 5: CUSTOMERS
-- ================================================================
CREATE TABLE customers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    registration_date DATE,
    country VARCHAR(50),
    city VARCHAR(50),
    status VARCHAR(20) DEFAULT 'Active'
);

-- ================================================================
-- TABLE 6: SALES
-- ================================================================
CREATE TABLE sales (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    amount DECIMAL(10, 2) NOT NULL,
    quantity INT,
    sale_date DATE NOT NULL,
    customer_id INT,
    employee_id INT,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- ================================================================
-- SAMPLE DATA INSERTION
-- ================================================================

-- Insert Departments
INSERT INTO departments (name, budget, location) VALUES
('Engineering', 500000.00, 'San Francisco'),
('Marketing', 250000.00, 'New York'),
('Sales', 300000.00, 'Chicago'),
('HR', 150000.00, 'Los Angeles'),
('Finance', 200000.00, 'Boston'),
('Product', 400000.00, 'Seattle');

-- Insert Employees
INSERT INTO employees (first_name, last_name, email, department_id, salary, hire_date, manager_id, job_title) VALUES
('John', 'Doe', 'john.doe@company.com', 1, 120000.00, '2020-01-15', NULL, 'VP Engineering'),
('Jane', 'Smith', 'jane.smith@company.com', 1, 95000.00, '2020-03-20', 1, 'Senior Software Engineer'),
('Mike', 'Johnson', 'mike.j@company.com', 1, 85000.00, '2021-06-10', 1, 'Software Engineer'),
('Sarah', 'Williams', 'sarah.w@company.com', 2, 80000.00, '2020-02-01', NULL, 'Marketing Director'),
('Tom', 'Brown', 'tom.b@company.com', 2, 65000.00, '2021-04-15', 4, 'Marketing Specialist'),
('Lisa', 'Davis', 'lisa.d@company.com', 3, 90000.00, '2019-11-20', NULL, 'Sales Director'),
('James', 'Wilson', 'james.w@company.com', 3, 75000.00, '2020-07-01', 6, 'Sales Manager'),
('Emma', 'Martinez', 'emma.m@company.com', 3, 70000.00, '2021-01-10', 6, 'Sales Representative'),
('David', 'Anderson', 'david.a@company.com', 4, 85000.00, '2020-05-05', NULL, 'HR Director'),
('Amy', 'Taylor', 'amy.t@company.com', 4, 60000.00, '2021-08-20', 9, 'HR Coordinator'),
('Chris', 'Lee', 'chris.l@company.com', 5, 95000.00, '2019-09-15', NULL, 'Finance Director'),
('Nancy', 'White', 'nancy.w@company.com', 5, 70000.00, '2020-10-30', 11, 'Financial Analyst'),
('Robert', 'Harris', 'robert.h@company.com', 6, 105000.00, '2020-04-12', NULL, 'Product Director'),
('Susan', 'Clark', 'susan.c@company.com', 6, 80000.00, '2021-02-28', 13, 'Product Manager'),
('Kevin', 'Lewis', 'kevin.l@company.com', 1, 78000.00, '2021-09-05', 2, 'Junior Developer');

-- Insert Projects
INSERT INTO projects (name, description, start_date, end_date, budget, status, department_id) VALUES
('Mobile App Development', 'New iOS and Android app', '2023-01-01', '2023-12-31', 300000.00, 'Active', 1),
('Website Redesign', 'Complete website overhaul', '2023-03-01', '2023-09-30', 150000.00, 'Completed', 1),
('Q4 Marketing Campaign', 'Holiday season campaign', '2023-10-01', '2023-12-31', 100000.00, 'Active', 2),
('CRM Implementation', 'New customer relationship system', '2023-02-15', '2024-02-15', 200000.00, 'Active', 3),
('Employee Training Program', 'Annual training initiative', '2023-01-01', '2023-12-31', 50000.00, 'Active', 4),
('Financial Audit', 'Year-end financial review', '2023-11-01', '2024-01-31', 75000.00, 'Active', 5),
('Product Launch', 'New product line introduction', '2023-06-01', '2023-12-31', 250000.00, 'Active', 6);

-- Insert Employee-Project Assignments
INSERT INTO employee_projects (employee_id, project_id, hours_worked, role, assignment_date) VALUES
(2, 1, 320.50, 'Lead Developer', '2023-01-01'),
(3, 1, 280.00, 'Backend Developer', '2023-01-01'),
(15, 1, 250.00, 'Frontend Developer', '2023-01-01'),
(2, 2, 150.00, 'Technical Lead', '2023-03-01'),
(3, 2, 140.00, 'Developer', '2023-03-01'),
(5, 3, 200.00, 'Campaign Manager', '2023-10-01'),
(7, 4, 180.00, 'CRM Specialist', '2023-02-15'),
(8, 4, 160.00, 'Implementation Consultant', '2023-02-15'),
(10, 5, 120.00, 'Training Coordinator', '2023-01-01'),
(12, 6, 100.00, 'Audit Analyst', '2023-11-01'),
(14, 7, 220.00, 'Product Manager', '2023-06-01');

-- Insert Customers
INSERT INTO customers (first_name, last_name, email, phone, registration_date, country, city, status) VALUES
('Alice', 'Cooper', 'alice.c@email.com', '555-0101', '2022-01-15', 'USA', 'New York', 'Active'),
('Bob', 'Dylan', 'bob.d@email.com', '555-0102', '2022-02-20', 'USA', 'Los Angeles', 'Active'),
('Charlie', 'Parker', 'charlie.p@email.com', '555-0103', '2022-03-10', 'Canada', 'Toronto', 'Active'),
('Diana', 'Ross', 'diana.r@email.com', '555-0104', '2022-04-05', 'UK', 'London', 'Active'),
('Elvis', 'Presley', 'elvis.p@email.com', '555-0105', '2022-05-12', 'USA', 'Chicago', 'Inactive'),
('Frank', 'Sinatra', 'frank.s@email.com', '555-0106', '2022-06-18', 'USA', 'Miami', 'Active'),
('Grace', 'Kelly', 'grace.k@email.com', '555-0107', '2022-07-22', 'France', 'Paris', 'Active'),
('Henry', 'Ford', 'henry.f@email.com', '555-0108', '2022-08-30', 'Germany', 'Berlin', 'Active'),
('Irene', 'Dunne', 'irene.d@email.com', '555-0109', '2022-09-14', 'Spain', 'Madrid', 'Active'),
('Jack', 'Nicholson', 'jack.n@email.com', '555-0110', '2022-10-25', 'USA', 'San Francisco', 'Active'),
('Kate', 'Winslet', 'kate.w@email.com', '555-0111', '2022-11-08', 'USA', 'Boston', 'Inactive'),
('Leo', 'DiCaprio', 'leo.d@email.com', '555-0112', '2022-12-15', 'USA', 'Seattle', 'Active'),
('Mary', 'Pickford', 'mary.p@email.com', '555-0113', '2023-01-20', 'Australia', 'Sydney', 'Active'),
('Neil', 'Armstrong', 'neil.a@email.com', '555-0114', '2023-02-14', 'USA', 'Houston', 'Active'),
('Olivia', 'Newton', 'olivia.n@email.com', '555-0115', '2023-03-30', 'UK', 'Manchester', 'Active');

-- Insert Sales
INSERT INTO sales (product, category, amount, quantity, sale_date, customer_id, employee_id) VALUES
('Laptop Pro', 'Electronics', 1299.99, 1, '2023-01-15', 1, 8),
('Wireless Mouse', 'Electronics', 29.99, 2, '2023-01-16', 1, 8),
('Office Chair', 'Furniture', 299.99, 1, '2023-01-20', 2, 7),
('Standing Desk', 'Furniture', 599.99, 1, '2023-02-01', 3, 7),
('Monitor 27"', 'Electronics', 349.99, 2, '2023-02-05', 4, 8),
('Keyboard Mechanical', 'Electronics', 89.99, 1, '2023-02-10', 5, 8),
('Webcam HD', 'Electronics', 79.99, 1, '2023-02-15', 6, 7),
('Desk Lamp', 'Furniture', 45.99, 3, '2023-03-01', 7, 8),
('Laptop Standard', 'Electronics', 799.99, 1, '2023-03-10', 8, 7),
('Tablet', 'Electronics', 499.99, 1, '2023-03-15', 9, 8),
('Headphones', 'Electronics', 149.99, 1, '2023-04-01', 10, 7),
('Phone Case', 'Accessories', 19.99, 5, '2023-04-05', 11, 8),
('USB Hub', 'Electronics', 39.99, 2, '2023-04-20', 12, 7),
('Laptop Bag', 'Accessories', 59.99, 1, '2023-05-01', 13, 8),
('Portable SSD', 'Electronics', 129.99, 1, '2023-05-15', 14, 7),
('Wireless Charger', 'Electronics', 34.99, 3, '2023-06-01', 15, 8),
('Bookshelf', 'Furniture', 189.99, 1, '2023-06-10', 1, 7),
('Coffee Maker', 'Appliances', 99.99, 1, '2023-06-20', 2, 8),
('Printer', 'Electronics', 249.99, 1, '2023-07-01', 3, 7),
('Scanner', 'Electronics', 179.99, 1, '2023-07-15', 4, 8),
('Desk Organizer', 'Accessories', 24.99, 2, '2023-08-01', 5, 7),
('Monitor Stand', 'Furniture', 49.99, 1, '2023-08-10', 6, 8),
('Cable Management', 'Accessories', 15.99, 4, '2023-08-20', 7, 7),
('Laptop Pro Max', 'Electronics', 1899.99, 1, '2023-09-01', 8, 8),
('Smart Watch', 'Electronics', 399.99, 1, '2023-09-15', 9, 7);

-- ================================================================
-- VIEWS FOR COMMON QUERIES
-- ================================================================

-- View: Employee Details with Department
CREATE OR REPLACE VIEW employee_details AS
SELECT
    e.id,
    CONCAT(e.first_name, ' ', e.last_name) AS full_name,
    e.email,
    e.job_title,
    e.salary,
    e.hire_date,
    d.name AS department_name,
    d.location,
    CONCAT(m.first_name, ' ', m.last_name) AS manager_name
FROM employees e
LEFT JOIN departments d ON e.department_id = d.id
LEFT JOIN employees m ON e.manager_id = m.id;

-- View: Sales Summary
CREATE OR REPLACE VIEW sales_summary AS
SELECT
    s.id,
    s.product,
    s.category,
    s.amount,
    s.quantity,
    s.sale_date,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    c.country,
    CONCAT(e.first_name, ' ', e.last_name) AS employee_name
FROM sales s
JOIN customers c ON s.customer_id = c.id
JOIN employees e ON s.employee_id = e.id;

-- ================================================================
-- VERIFICATION QUERIES
-- ================================================================

-- Check row counts
SELECT 'departments' AS table_name, COUNT(*) AS row_count FROM departments
UNION ALL
SELECT 'employees', COUNT(*) FROM employees
UNION ALL
SELECT 'projects', COUNT(*) FROM projects
UNION ALL
SELECT 'employee_projects', COUNT(*) FROM employee_projects
UNION ALL
SELECT 'customers', COUNT(*) FROM customers
UNION ALL
SELECT 'sales', COUNT(*) FROM sales;

-- ================================================================
-- NOTES:
-- ================================================================
-- 1. This schema uses MySQL/MariaDB syntax
-- 2. For PostgreSQL, change AUTO_INCREMENT to SERIAL
-- 3. For SQL Server, change AUTO_INCREMENT to IDENTITY(1,1)
-- 4. Adjust data types as needed for your specific database
-- 5. The schema includes realistic data for practice queries
-- ================================================================
