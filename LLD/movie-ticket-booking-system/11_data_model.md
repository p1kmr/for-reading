# Database Schema & ER Diagram

## 🗄️ Entity-Relationship Diagram

```mermaid
erDiagram
    CUSTOMER ||--o{ BOOKING : makes
    BOOKING }o--|| SHOW : "booked for"
    BOOKING ||--|| PAYMENT : has
    BOOKING }o--{ SHOW_SEAT : reserves

    SHOW }o--|| MOVIE : displays
    SHOW }o--|| SCREEN : "scheduled in"
    SHOW ||--o{ SHOW_SEAT : "has availability"

    SCREEN }o--|| THEATER : "belongs to"
    SCREEN ||--o{ SEAT : contains
    SEAT ||--o{ SHOW_SEAT : "tracked by"

    CUSTOMER {
        varchar customer_id PK
        varchar name
        varchar email UK
        varchar phone_number UK
        varchar password_hash
        datetime registration_date
    }

    MOVIE {
        varchar movie_id PK
        varchar title
        int duration_minutes
        varchar language
        varchar genre
        date release_date
        varchar rating
        text description
    }

    THEATER {
        varchar theater_id PK
        varchar name
        varchar city
        varchar address
        int total_screens
    }

    SCREEN {
        varchar screen_id PK
        varchar screen_name
        int capacity
        varchar screen_type
        varchar theater_id FK
    }

    SEAT {
        varchar seat_id PK
        varchar screen_id FK
        varchar row_number
        varchar seat_number
        varchar seat_type
        decimal base_price
    }

    SHOW {
        varchar show_id PK
        varchar movie_id FK
        varchar screen_id FK
        date show_date
        time show_time
        varchar status
    }

    SHOW_SEAT {
        varchar show_seat_id PK
        varchar show_id FK
        varchar seat_id FK
        varchar status
        datetime blocked_at
        int version "optimistic lock"
    }

    BOOKING {
        varchar booking_id PK
        varchar customer_id FK
        varchar show_id FK
        decimal total_amount
        varchar status
        datetime booking_time
        datetime expires_at
    }

    PAYMENT {
        varchar payment_id PK
        varchar booking_id FK
        decimal amount
        varchar method
        varchar status
        varchar transaction_id
        datetime payment_time
        datetime refunded_at
        varchar refund_transaction_id
    }
```

---

## 📋 Table Schemas with Indexes

### 1. customers

```sql
CREATE TABLE customers (
    customer_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    registration_date DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_email (email),
    INDEX idx_phone (phone_number)
);
```

**Indexes:**
- `email`, `phone_number`: For login and duplicate check

---

### 2. movies

```sql
CREATE TABLE movies (
    movie_id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    duration_minutes INT NOT NULL,
    language VARCHAR(50) NOT NULL,
    genre VARCHAR(50),
    release_date DATE,
    rating VARCHAR(10),
    description TEXT,

    INDEX idx_title (title),
    INDEX idx_language (language),
    INDEX idx_release_date (release_date)
);
```

**Indexes:**
- `title`: For search
- `language`, `release_date`: For filtering

---

### 3. theaters

```sql
CREATE TABLE theaters (
    theater_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address TEXT,
    total_screens INT,

    INDEX idx_city (city),
    INDEX idx_name (name)
);
```

---

### 4. screens

```sql
CREATE TABLE screens (
    screen_id VARCHAR(50) PRIMARY KEY,
    screen_name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    screen_type VARCHAR(50),
    theater_id VARCHAR(50) NOT NULL,

    FOREIGN KEY (theater_id) REFERENCES theaters(theater_id)
        ON DELETE CASCADE,

    INDEX idx_theater (theater_id)
);
```

**Foreign Key:** `theater_id` → theaters(theater_id)
- `ON DELETE CASCADE`: If theater deleted, delete all its screens

---

### 5. seats

```sql
CREATE TABLE seats (
    seat_id VARCHAR(50) PRIMARY KEY,
    screen_id VARCHAR(50) NOT NULL,
    row_number VARCHAR(10) NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    seat_type VARCHAR(50),
    base_price DECIMAL(10, 2) NOT NULL,

    FOREIGN KEY (screen_id) REFERENCES screens(screen_id)
        ON DELETE CASCADE,

    UNIQUE KEY uk_screen_row_seat (screen_id, row_number, seat_number),
    INDEX idx_screen (screen_id)
);
```

**Unique constraint:** Prevents duplicate seats (same screen can't have two A1 seats)

---

### 6. shows

```sql
CREATE TABLE shows (
    show_id VARCHAR(50) PRIMARY KEY,
    movie_id VARCHAR(50) NOT NULL,
    screen_id VARCHAR(50) NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    status VARCHAR(50) DEFAULT 'SCHEDULED',

    FOREIGN KEY (movie_id) REFERENCES movies(movie_id)
        ON DELETE CASCADE,
    FOREIGN KEY (screen_id) REFERENCES screens(screen_id)
        ON DELETE CASCADE,

    INDEX idx_movie (movie_id),
    INDEX idx_screen (screen_id),
    INDEX idx_date_time (show_date, show_time)
);
```

**Compound index:** `(show_date, show_time)` for efficient queries:
```sql
-- Fast query
SELECT * FROM shows
WHERE show_date = '2025-11-18'
  AND show_time BETWEEN '15:00' AND '20:00';
```

---

### 7. show_seats (CRITICAL TABLE!)

```sql
CREATE TABLE show_seats (
    show_seat_id VARCHAR(50) PRIMARY KEY,
    show_id VARCHAR(50) NOT NULL,
    seat_id VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'AVAILABLE',
    blocked_at DATETIME NULL,
    version INT DEFAULT 0,  -- For optimistic locking

    FOREIGN KEY (show_id) REFERENCES shows(show_id)
        ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seats(seat_id)
        ON DELETE CASCADE,

    UNIQUE KEY uk_show_seat (show_id, seat_id),
    INDEX idx_show (show_id),
    INDEX idx_status (status),
    INDEX idx_blocked_at (blocked_at)
);
```

**Unique constraint:** `(show_id, seat_id)` prevents duplicates
**Indexes:**
- `show_id`: Fast retrieval of all seats for a show
- `status`: Find all available seats quickly
- `blocked_at`: Background job to find expired blockings

**Pessimistic Locking Query:**
```sql
SELECT * FROM show_seats
WHERE show_seat_id = 'SS001'
FOR UPDATE;  -- 🔒 Locks this row
```

---

### 8. bookings

```sql
CREATE TABLE bookings (
    booking_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    show_id VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING_PAYMENT',
    booking_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,

    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (show_id) REFERENCES shows(show_id),

    INDEX idx_customer (customer_id),
    INDEX idx_show (show_id),
    INDEX idx_status (status),
    INDEX idx_expires_at (expires_at)
);
```

**Index on `expires_at`:** For background job to find expired bookings
```sql
SELECT * FROM bookings
WHERE status = 'PENDING_PAYMENT'
  AND expires_at < NOW();
```

---

### 9. booking_seats (Junction table)

```sql
CREATE TABLE booking_seats (
    booking_id VARCHAR(50) NOT NULL,
    show_seat_id VARCHAR(50) NOT NULL,

    PRIMARY KEY (booking_id, show_seat_id),
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE,
    FOREIGN KEY (show_seat_id) REFERENCES show_seats(show_seat_id)
        ON DELETE CASCADE
);
```

**Purpose:** Many-to-many relationship between Booking and ShowSeat
- One booking can have multiple seats
- Each seat belongs to one booking

---

### 10. payments

```sql
CREATE TABLE payments (
    payment_id VARCHAR(50) PRIMARY KEY,
    booking_id VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    method VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    transaction_id VARCHAR(100),
    payment_time DATETIME,
    refunded_at DATETIME NULL,
    refund_transaction_id VARCHAR(100),

    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE,

    INDEX idx_booking (booking_id),
    INDEX idx_status (status),
    INDEX idx_transaction_id (transaction_id)
);
```

**Unique constraint on `booking_id`:** 1:1 relationship with bookings

---

## 🔍 Important Queries

### Query 1: Search shows by movie and city
```sql
SELECT s.*, m.title, t.name as theater_name, sc.screen_name
FROM shows s
JOIN movies m ON s.movie_id = m.movie_id
JOIN screens sc ON s.screen_id = sc.screen_id
JOIN theaters t ON sc.theater_id = t.theater_id
WHERE m.title LIKE '%Inception%'
  AND t.city = 'Delhi'
  AND s.show_date = '2025-11-18'
ORDER BY s.show_time;
```

### Query 2: Get seat availability for a show
```sql
SELECT
    ss.show_seat_id,
    s.row_number,
    s.seat_number,
    s.seat_type,
    ss.status,
    s.base_price
FROM show_seats ss
JOIN seats s ON ss.seat_id = s.seat_id
WHERE ss.show_id = 'SHW001'
ORDER BY s.row_number, s.seat_number;
```

### Query 3: Find expired bookings (Background job)
```sql
SELECT * FROM bookings
WHERE status = 'PENDING_PAYMENT'
  AND expires_at < NOW();
```

### Query 4: Customer booking history
```sql
SELECT
    b.booking_id,
    b.booking_time,
    b.status,
    b.total_amount,
    m.title,
    s.show_date,
    s.show_time,
    t.name as theater_name
FROM bookings b
JOIN shows s ON b.show_id = s.show_id
JOIN movies m ON s.movie_id = m.movie_id
JOIN screens sc ON s.screen_id = sc.screen_id
JOIN theaters t ON sc.theater_id = t.theater_id
WHERE b.customer_id = 'CUST001'
ORDER BY b.booking_time DESC;
```

---

## 📊 Sample Data

### Sample Booking Flow Data

**Step 1: Initial State**
```sql
-- Show created
INSERT INTO shows VALUES ('SHW001', 'MOV001', 'SCR001', '2025-11-18', '19:00:00', 'SCHEDULED');

-- ShowSeats created (all available)
INSERT INTO show_seats VALUES ('SS001', 'SHW001', 'SEAT001', 'AVAILABLE', NULL, 0);
INSERT INTO show_seats VALUES ('SS002', 'SHW001', 'SEAT002', 'AVAILABLE', NULL, 0);
```

**Step 2: Customer selects seats**
```sql
-- Create booking
INSERT INTO bookings VALUES (
    'BK001', 'CUST001', 'SHW001', 400.00, 'PENDING_PAYMENT',
    '2025-11-18 14:35:00', '2025-11-18 14:45:00'
);

-- Block seats
UPDATE show_seats
SET status = 'BLOCKED', blocked_at = '2025-11-18 14:35:00'
WHERE show_seat_id IN ('SS001', 'SS002');

-- Link booking to seats
INSERT INTO booking_seats VALUES ('BK001', 'SS001');
INSERT INTO booking_seats VALUES ('BK001', 'SS002');
```

**Step 3: Payment successful**
```sql
-- Create payment
INSERT INTO payments VALUES (
    'PAY001', 'BK001', 400.00, 'UPI', 'SUCCESS',
    'razorpay_123', '2025-11-18 14:38:00', NULL, NULL
);

-- Confirm booking
UPDATE bookings SET status = 'CONFIRMED' WHERE booking_id = 'BK001';

-- Book seats
UPDATE show_seats
SET status = 'BOOKED', blocked_at = NULL
WHERE show_seat_id IN ('SS001', 'SS002');
```

---

## 📝 Summary

| Table | Purpose | Critical Indexes |
|-------|---------|------------------|
| **show_seats** | Seat availability per show | show_id, status, blocked_at |
| **bookings** | Ticket reservations | customer_id, show_id, expires_at |
| **payments** | Payment transactions | booking_id, transaction_id |
| **shows** | Movie screenings | movie_id, (show_date, show_time) |

**Key Design Decisions:**
1. **show_seats** table separates physical seats from availability per show
2. **Unique constraints** prevent data integrity issues
3. **Indexes** on foreign keys and frequently queried columns
4. **ON DELETE CASCADE** maintains referential integrity

**Next Document:** [12_java_code_implementation.md](./12_java_code_implementation.md)
