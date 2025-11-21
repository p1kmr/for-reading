# Phase 10: Database Design & Architecture

## Table of Contents
- [ER Diagrams and Data Modeling](#er-diagrams-and-data-modeling)
- [Keys and Constraints](#keys-and-constraints)
- [Design Patterns and Best Practices](#design-patterns-and-best-practices)
- [Real-World Design Exercises](#real-world-design-exercises)
- [Interview Design Questions](#interview-design-questions)
- [Anti-Patterns to Avoid](#anti-patterns-to-avoid)
- [Scalability Considerations](#scalability-considerations)

---

## ER Diagrams and Data Modeling

### WHY
Good database design is critical for:
- Data integrity and consistency
- Query performance
- Application scalability
- Ease of maintenance
- Preventing data anomalies

### Entity-Relationship Basics

**Entities**: Objects or concepts (tables)
**Attributes**: Properties of entities (columns)
**Relationships**: Connections between entities (foreign keys)

### Cardinality Types

#### One-to-One (1:1)
```
┌──────────┐       ┌──────────────┐
│  users   │ 1───1 │user_profiles │
├──────────┤       ├──────────────┤
│ id (PK)  │◄──────┤│ user_id (FK) │
│ username │       ││ bio          │
│ email    │       ││ avatar       │
└──────────┘       └──────────────┘
Example: Each user has one profile
```

#### One-to-Many (1:N)
```
┌───────────────┐         ┌──────────────┐
│  departments  │ 1─────N │  employees   │
├───────────────┤         ├──────────────┤
│ id (PK)       │◄────────┤│ dept_id (FK) │
│ name          │         ││ name         │
│ budget        │         ││ salary       │
└───────────────┘         └──────────────┘
Example: One department has many employees
```

#### Many-to-Many (N:M)
```
┌──────────┐         ┌─────────────────┐         ┌──────────┐
│ students │ N─────N │student_courses  │ N─────N │ courses  │
├──────────┤         ├─────────────────┤         ├──────────┤
│ id (PK)  │◄────────┤│ student_id (FK) │────────►││ id (PK)  │
│ name     │         ││ course_id (FK)  │         ││ name     │
└──────────┘         │  enrollment_date│         └──────────┘
                     └─────────────────┘
Example: Students take many courses, courses have many students
```

---

## Keys and Constraints

### Primary Keys

#### Natural vs Surrogate Keys

| Type | Description | Pros | Cons |
|------|-------------|------|------|
| **Natural** | Business data (email, SSN) | Meaningful, no extra column | May change, composite, larger |
| **Surrogate** | Auto-generated (INT, UUID) | Stable, simple, performant | No business meaning |

**Best Practice**: Use surrogate keys (AUTO_INCREMENT or UUID)

```sql
-- Surrogate key (recommended)
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE NOT NULL
);

-- Natural key
CREATE TABLE countries (
    country_code CHAR(2) PRIMARY KEY,  -- ISO code
    name VARCHAR(100) NOT NULL
);
```

### Foreign Keys

```sql
CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    order_date DATE NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);
```

**Referential Actions:**
- `CASCADE`: Propagate changes
- `RESTRICT`: Prevent changes if references exist
- `SET NULL`: Set FK to NULL
- `SET DEFAULT`: Set FK to default value

### Other Constraints

```sql
CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    sku VARCHAR(50) UNIQUE NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    quantity INT DEFAULT 0 CHECK (quantity >= 0),
    category ENUM('Electronics', 'Clothing', 'Food') NOT NULL
);
```

---

## Design Patterns and Best Practices

### Naming Conventions
- Tables: plural, snake_case (`order_items`)
- Columns: singular, snake_case (`first_name`)
- Primary keys: `id` or `table_id`
- Foreign keys: `referenced_table_id` (`customer_id`)
- Boolean: prefix with `is_`, `has_`, `can_` (`is_active`)
- Timestamps: `created_at`, `updated_at`, `deleted_at`

### Soft Deletes
```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL
);

-- Query active users
SELECT * FROM users WHERE is_deleted = FALSE;
```

### Audit Trails
```sql
CREATE TABLE audit_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    table_name VARCHAR(50) NOT NULL,
    record_id INT NOT NULL,
    action ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    old_values JSON,
    new_values JSON,
    user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Real-World Design Exercises

### Exercise 1: Hotel Booking System

#### Step 1: Identify Entities
- Hotels, Rooms, Room Types, Guests, Bookings, Payments

#### Step 2: Define Relationships
```
Hotels ──1:N──► Rooms
Room Types ──1:N──► Rooms
Guests ──1:N──► Bookings
Rooms ──1:N──► Bookings
Bookings ──1:N──► Payments
```

#### Step 3-5: Complete Schema

```sql
-- Hotels
CREATE TABLE hotels (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(50) NOT NULL,
    stars TINYINT CHECK (stars BETWEEN 1 AND 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Room Types
CREATE TABLE room_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,  -- Standard, Deluxe, Suite
    description TEXT,
    base_price DECIMAL(10,2) NOT NULL,
    max_occupancy INT NOT NULL
);

-- Rooms
CREATE TABLE rooms (
    id INT PRIMARY KEY AUTO_INCREMENT,
    hotel_id INT NOT NULL,
    room_type_id INT NOT NULL,
    room_number VARCHAR(10) NOT NULL,
    floor INT,
    is_available BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    FOREIGN KEY (room_type_id) REFERENCES room_types(id),
    UNIQUE (hotel_id, room_number)
);

-- Guests
CREATE TABLE guests (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    id_document VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bookings
CREATE TABLE bookings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    guest_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    num_guests INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('pending', 'confirmed', 'checked_in', 'checked_out', 'cancelled') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    CHECK (check_out_date > check_in_date)
);

-- Payments
CREATE TABLE payments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('credit_card', 'debit_card', 'cash', 'bank_transfer') NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('pending', 'completed', 'refunded') DEFAULT 'pending',
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);
```

---

### Exercise 2: E-commerce Platform

#### ER Diagram
```
┌──────────┐     ┌────────────┐     ┌──────────┐
│  users   │     │   orders   │     │ products │
├──────────┤     ├────────────┤     ├──────────┤
│ id       │◄────┤│ user_id    │     ││ id       │
│ email    │     ││ total      │     ││ name     │
│ name     │     │  status    │     │  price   │
└──────────┘     └─────┬──────┘     └────┬─────┘
                       │                  │
                       ▼                  │
                ┌─────────────┐           │
                │ order_items │           │
                ├─────────────┤           │
                │ order_id    │           │
                │ product_id  │───────────┘
                │ quantity    │
                │ price       │
                └─────────────┘
```

#### Complete Schema

```sql
-- Users
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Categories
CREATE TABLE categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    parent_id INT,
    description TEXT,
    FOREIGN KEY (parent_id) REFERENCES categories(id)
);

-- Products
CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    category_id INT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT DEFAULT 0,
    sku VARCHAR(50) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Addresses
CREATE TABLE addresses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    address_line1 VARCHAR(200) NOT NULL,
    address_line2 VARCHAR(200),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(50) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Orders
CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    shipping_address_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('pending', 'processing', 'shipped', 'delivered', 'cancelled') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (shipping_address_id) REFERENCES addresses(id)
);

-- Order Items
CREATE TABLE order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Reviews
CREATE TABLE reviews (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE (user_id, product_id)
);
```

---

### Exercise 3: Social Media Database

```sql
-- Users
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    bio TEXT,
    avatar_url VARCHAR(500),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Posts
CREATE TABLE posts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    media_url VARCHAR(500),
    is_public BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Comments
CREATE TABLE comments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    parent_comment_id INT,  -- For nested comments
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (parent_comment_id) REFERENCES comments(id)
);

-- Likes (posts and comments)
CREATE TABLE likes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    likeable_type ENUM('post', 'comment') NOT NULL,
    likeable_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE (user_id, likeable_type, likeable_id)
);

-- Follows
CREATE TABLE follows (
    follower_id INT NOT NULL,
    following_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, following_id),
    FOREIGN KEY (follower_id) REFERENCES users(id),
    FOREIGN KEY (following_id) REFERENCES users(id),
    CHECK (follower_id != following_id)
);

-- Messages
CREATE TABLE messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);
```

---

### Exercise 4: Library Management System

```sql
-- Authors
CREATE TABLE authors (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    birth_date DATE,
    nationality VARCHAR(50)
);

-- Books
CREATE TABLE books (
    id INT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    publication_year YEAR,
    publisher VARCHAR(100),
    total_copies INT DEFAULT 1,
    available_copies INT DEFAULT 1
);

-- Book Authors (M:N)
CREATE TABLE book_authors (
    book_id INT NOT NULL,
    author_id INT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (author_id) REFERENCES authors(id)
);

-- Members
CREATE TABLE members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    membership_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

-- Loans
CREATE TABLE loans (
    id INT PRIMARY KEY AUTO_INCREMENT,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    loan_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status ENUM('active', 'returned', 'overdue') DEFAULT 'active',
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (member_id) REFERENCES members(id)
);

-- Fines
CREATE TABLE fines (
    id INT PRIMARY KEY AUTO_INCREMENT,
    loan_id INT NOT NULL,
    amount DECIMAL(6,2) NOT NULL,
    reason VARCHAR(200),
    is_paid BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (loan_id) REFERENCES loans(id)
);
```

---

## Interview Design Questions

### Question 1: URL Shortener Database

**Problem**: Design a database for a URL shortening service like bit.ly.

**Solution**:
```sql
CREATE TABLE urls (
    id INT PRIMARY KEY AUTO_INCREMENT,
    short_code VARCHAR(10) UNIQUE NOT NULL,
    original_url TEXT NOT NULL,
    user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_short_code (short_code)
);

CREATE TABLE clicks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    url_id INT NOT NULL,
    clicked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    referrer VARCHAR(500),
    country VARCHAR(50),
    FOREIGN KEY (url_id) REFERENCES urls(id)
);
```

---

### Question 2: Movie Rating System

**Problem**: Design a database for a movie rating platform like IMDB.

**Solution**:
```sql
CREATE TABLE movies (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    release_year YEAR,
    duration_minutes INT,
    synopsis TEXT,
    avg_rating DECIMAL(3,2) DEFAULT 0,
    total_ratings INT DEFAULT 0
);

CREATE TABLE genres (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE movie_genres (
    movie_id INT,
    genre_id INT,
    PRIMARY KEY (movie_id, genre_id),
    FOREIGN KEY (movie_id) REFERENCES movies(id),
    FOREIGN KEY (genre_id) REFERENCES genres(id)
);

CREATE TABLE ratings (
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 10),
    review TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, movie_id),
    FOREIGN KEY (movie_id) REFERENCES movies(id)
);
```

---

### Question 3: Chat Application Database

**Problem**: Design a database for a messaging app with groups.

**Solution**:
```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100),
    last_seen TIMESTAMP
);

CREATE TABLE conversations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    is_group BOOLEAN DEFAULT FALSE,
    name VARCHAR(100),  -- For group chats
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE conversation_participants (
    conversation_id INT,
    user_id INT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_admin BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (conversation_id, user_id),
    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    conversation_id INT NOT NULL,
    sender_id INT NOT NULL,
    content TEXT NOT NULL,
    message_type ENUM('text', 'image', 'file') DEFAULT 'text',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

CREATE TABLE message_reads (
    message_id INT,
    user_id INT,
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id, user_id),
    FOREIGN KEY (message_id) REFERENCES messages(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

### Question 4: Inventory Management System

**Problem**: Design a database for warehouse inventory tracking.

**Solution**:
```sql
CREATE TABLE warehouses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(200) NOT NULL,
    capacity INT
);

CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sku VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    unit_price DECIMAL(10,2) NOT NULL,
    reorder_level INT DEFAULT 10
);

CREATE TABLE inventory (
    warehouse_id INT,
    product_id INT,
    quantity INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (warehouse_id, product_id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE inventory_transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id INT NOT NULL,
    product_id INT NOT NULL,
    transaction_type ENUM('in', 'out', 'transfer', 'adjustment') NOT NULL,
    quantity INT NOT NULL,
    reference_id VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

---

### Question 5: Parking Lot System

**Problem**: Design a database for a parking lot management system.

**Solution**:
```sql
CREATE TABLE parking_lots (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200) NOT NULL,
    total_spots INT NOT NULL
);

CREATE TABLE spot_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,  -- Compact, Regular, Large, Handicapped
    hourly_rate DECIMAL(6,2) NOT NULL
);

CREATE TABLE parking_spots (
    id INT PRIMARY KEY AUTO_INCREMENT,
    lot_id INT NOT NULL,
    spot_type_id INT NOT NULL,
    spot_number VARCHAR(10) NOT NULL,
    floor INT,
    is_available BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (lot_id) REFERENCES parking_lots(id),
    FOREIGN KEY (spot_type_id) REFERENCES spot_types(id),
    UNIQUE (lot_id, spot_number)
);

CREATE TABLE vehicles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    license_plate VARCHAR(20) UNIQUE NOT NULL,
    vehicle_type ENUM('motorcycle', 'car', 'van', 'truck') NOT NULL,
    owner_name VARCHAR(100),
    owner_phone VARCHAR(20)
);

CREATE TABLE parking_tickets (
    id INT PRIMARY KEY AUTO_INCREMENT,
    spot_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    entry_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exit_time TIMESTAMP,
    total_amount DECIMAL(8,2),
    is_paid BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (spot_id) REFERENCES parking_spots(id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);
```

---

## Anti-Patterns to Avoid

### 1. God Table
❌ One huge table with 50+ columns
✅ Split into related entities with proper relationships

### 2. Entity-Attribute-Value (EAV)
❌ `id, entity, attribute, value` for everything
✅ Use proper schema design (or JSON columns if truly dynamic)

### 3. Polymorphic Associations
❌ `comment_id, commentable_type, commentable_id`
✅ Use separate junction tables or single-table inheritance

### 4. Storing Comma-Separated Values
❌ `tags VARCHAR(500)` → "sql,database,design"
✅ Use junction tables for M:N relationships

### 5. No Foreign Keys
❌ Relying on application code for integrity
✅ Always define foreign key constraints

### 6. Storing Calculated Values
❌ Storing `age` when you have `birth_date`
✅ Calculate on query or use views/generated columns

---

## Scalability Considerations

### Horizontal vs Vertical Scaling
- **Vertical**: Bigger server (limited)
- **Horizontal**: More servers (sharding, replication)

### Sharding Strategies
- **By customer/tenant**: Each customer's data on specific shard
- **By geography**: Data stored near users
- **By hash**: Distribute by hash of primary key

### Read Replicas
- Master for writes
- Multiple replicas for reads
- Eventual consistency consideration

### Caching Layer
- Redis/Memcached for frequently accessed data
- Invalidation strategies

### Partitioning
```sql
-- Partition by date range
CREATE TABLE sales (
    id INT,
    sale_date DATE,
    amount DECIMAL(10,2)
)
PARTITION BY RANGE (YEAR(sale_date)) (
    PARTITION p2022 VALUES LESS THAN (2023),
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025)
);
```

---

## Summary & Next Steps

### Key Takeaways
1. **Identify entities** before writing any SQL
2. **Use proper cardinality** (1:1, 1:N, N:M)
3. **Always use foreign keys** for data integrity
4. **Normalize** to reduce redundancy (then denormalize strategically)
5. **Plan for scale** from the beginning
6. **Document your schema** with ER diagrams

### Design Checklist
- [ ] All entities identified
- [ ] Relationships defined with cardinality
- [ ] Primary keys chosen (prefer surrogate)
- [ ] Foreign keys with proper actions
- [ ] Constraints added (NOT NULL, UNIQUE, CHECK)
- [ ] Indexes planned for common queries
- [ ] Naming conventions consistent

### What You've Completed!
Congratulations! You've completed the 10-phase SQL learning journey:
- Phase 1-3: SQL Fundamentals
- Phase 4-6: Intermediate SQL
- Phase 7-8: Advanced SQL
- Phase 9-10: Expert Level

**Next**: Review the Interview Questions and Common Mistakes guides for interview preparation!
