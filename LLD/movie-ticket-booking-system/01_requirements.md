# Movie Ticket Booking System - Requirements Analysis

## 📋 Overview
This document outlines the requirements for designing a **Movie Ticket Booking System**. We'll break down the requirements into **Functional** (what the system should do) and **Non-Functional** (how well the system should perform).

---

## 🎯 Functional Requirements

### 1. Movie Management
**What it means:** The system should be able to store and manage information about movies.

- **FR1.1**: Add new movies to the system with details:
  - Movie title (e.g., "Inception")
  - Duration in minutes (e.g., 148 minutes)
  - Language (e.g., English, Hindi, Spanish)
  - Genre (e.g., Action, Comedy, Drama)
  - Release date
  - Rating (e.g., PG-13, R)
  - Description/synopsis

- **FR1.2**: Update movie information (change description, rating, etc.)

- **FR1.3**: Delete movies that are no longer showing

- **FR1.4**: Search movies by:
  - Title
  - Language
  - Genre
  - Release date range

**Real-world example:** Like searching for "Avatar" in English on BookMyShow or Fandango.

---

### 2. Theater Management
**What it means:** The system manages physical theaters where movies are shown.

- **FR2.1**: Add theater information:
  - Theater name (e.g., "PVR Cinemas - Mall Road")
  - Location/address with city
  - Total number of screens/halls
  - Facilities (parking, food court, wheelchair access)

- **FR2.2**: Each theater has multiple **screens/halls**:
  - Screen name (e.g., "Screen 1", "IMAX Screen")
  - Total seating capacity
  - Screen type (Regular, IMAX, 3D, 4K)

- **FR2.3**: Update theater details

- **FR2.4**: Search theaters by:
  - City
  - Theater name
  - Movie currently showing

**Real-world example:** Selecting "AMC Empire 25" in New York City.

---

### 3. Show Management
**What it means:** A "show" is a specific screening of a movie at a particular time in a specific screen.

- **FR3.1**: Schedule shows with details:
  - Which movie
  - Which screen in which theater
  - Show date and time (e.g., "2025-11-18 7:00 PM")
  - Show duration (inherited from movie)
  - Show status (Scheduled, Ongoing, Completed, Cancelled)

- **FR3.2**: Multiple shows per screen per day (morning, afternoon, evening, night)

- **FR3.3**: No overlapping shows in the same screen
  - If a movie runs 7:00 PM - 9:30 PM, the next show can't start before 9:30 PM (with cleanup time)

- **FR3.4**: Update show timings

- **FR3.5**: Cancel shows

- **FR3.6**: Search shows by:
  - Movie
  - Theater
  - Date and time range
  - City

**Real-world example:** "Inception" at 7:00 PM in Screen 2 of PVR Select City Walk.

---

### 4. Seat Management
**What it means:** Managing individual seats in each screen and tracking their availability.

- **FR4.1**: Define seat layout for each screen:
  - Rows (A, B, C, ...) and seat numbers (1, 2, 3, ...)
  - Example: A1, A2, B1, B2, etc.

- **FR4.2**: Different seat types with different pricing:
  - **Regular/Standard**: Base price
  - **Premium/Recliner**: Higher price
  - **VIP**: Highest price
  - **Wheelchair accessible**: Same as regular but marked

- **FR4.3**: Track seat status for each show:
  - **Available**: Can be booked
  - **Blocked**: Temporarily held during booking process (5-10 minutes)
  - **Booked**: Confirmed booking
  - **Not Available**: Damaged or maintenance

- **FR4.4**: Release blocked seats automatically if booking not completed within timeout

- **FR4.5**: Display seat availability in real-time during selection

**Real-world example:** Selecting seat C7 (Premium) for 7:00 PM show of "Interstellar".

---

### 5. Booking Management
**What it means:** Handling the entire ticket booking lifecycle.

- **FR5.1**: Create a new booking:
  - Customer selects show
  - Customer selects one or more seats
  - System blocks seats temporarily (5-10 minutes)
  - Customer proceeds to payment
  - On successful payment, booking is confirmed
  - Seats are marked as "Booked"

- **FR5.2**: Generate unique booking ID/PNR (e.g., "BK20251118001")

- **FR5.3**: Booking details include:
  - Booking ID
  - Customer information
  - Show details (movie, theater, screen, date, time)
  - Selected seats
  - Total amount
  - Booking status (Pending, Confirmed, Cancelled)
  - Booking timestamp

- **FR5.4**: View booking details by booking ID

- **FR5.5**: Cancel booking:
  - Only before show starts (with cancellation policy)
  - Refund processing
  - Seats become available again

- **FR5.6**: Booking confirmation via:
  - Email with booking details and QR code
  - SMS with booking ID

- **FR5.7**: Search bookings by:
  - Booking ID
  - Customer email/phone
  - Show

**Real-world example:** Booking 3 seats for "Dune 2" and receiving confirmation email with QR code.

---

### 6. Customer Management

- **FR6.1**: Register customer:
  - Name
  - Email (unique)
  - Phone number (unique)
  - Password (encrypted)

- **FR6.2**: Customer login/authentication

- **FR6.3**: View booking history

- **FR6.4**: Update profile information

**Real-world example:** Creating an account on Cineplex or Regal Cinemas.

---

### 7. Payment Management

- **FR7.1**: Support multiple payment methods:
  - Credit/Debit Card
  - UPI (India)
  - Digital Wallets (PayPal, Google Pay, Apple Pay)
  - Net Banking

- **FR7.2**: Payment transaction tracking:
  - Transaction ID
  - Amount
  - Payment method
  - Payment status (Pending, Success, Failed, Refunded)
  - Timestamp

- **FR7.3**: Process refunds for cancelled bookings

- **FR7.4**: Payment gateway integration

**Real-world example:** Paying ₹500 via Google Pay for 2 movie tickets.

---

### 8. Pricing Management

- **FR8.1**: Dynamic pricing based on:
  - Seat type (Regular, Premium, VIP)
  - Show timing (matinee cheaper, evening/night expensive)
  - Day of week (weekday vs weekend)
  - Special shows (premiere, first day first show)

- **FR8.2**: Apply discounts and promo codes

- **FR8.3**: Calculate total price including taxes and fees

**Real-world example:** Matinee show costs $8, evening show costs $15 for the same seat.

---

### 9. Admin Management

- **FR9.1**: Admin can:
  - Add/update/delete movies, theaters, screens
  - Schedule shows
  - View all bookings
  - Generate reports (revenue, occupancy, popular movies)
  - Manage seat status manually (mark damaged seats)

---

## ⚡ Non-Functional Requirements

### 1. **Performance**
- **NFR1.1**: System should handle **10,000+ concurrent users** during peak times (evening, weekends)
- **NFR1.2**: Seat availability should update in **real-time** (within 1-2 seconds)
- **NFR1.3**: Booking confirmation should complete within **5 seconds** after payment
- **NFR1.4**: Search results (movies, theaters, shows) should load within **2 seconds**

**Why it matters:** During blockbuster movie releases, thousands of users try to book simultaneously.

---

### 2. **Concurrency & Consistency**
- **NFR2.1**: **Prevent double-booking**: Two users cannot book the same seat for the same show
- **NFR2.2**: Use **locking mechanisms** (pessimistic or optimistic) for seat booking
- **NFR2.3**: **ACID transactions** for booking + payment operations
- **NFR2.4**: **Seat blocking timeout**: Release seats if payment not completed in 10 minutes

**Why it matters:** Multiple users might select the same seat at the same time. System must handle this gracefully.

**Beginner explanation:**
- **Pessimistic locking**: Lock the seat as soon as user clicks it (like "reserved" sign on a chair)
- **Optimistic locking**: Allow selection but check at payment time if still available (faster but may fail)

---

### 3. **Availability & Reliability**
- **NFR3.1**: System should have **99.9% uptime** (less than 9 hours downtime per year)
- **NFR3.2**: **Database replication** for backup
- **NFR3.3**: **Graceful degradation**: If payment gateway is down, show error message but don't crash
- **NFR3.4**: **Automatic retry** for failed payment transactions (up to 3 times)

**Why it matters:** System should be available especially during movie release times.

---

### 4. **Security**
- **NFR4.1**: **Encrypt passwords** using bcrypt or similar
- **NFR4.2**: **HTTPS** for all communication
- **NFR4.3**: **PCI DSS compliance** for payment data (don't store credit card CVV)
- **NFR4.4**: **JWT tokens** for API authentication
- **NFR4.5**: **Input validation** to prevent SQL injection, XSS attacks
- **NFR4.6**: **Rate limiting**: Prevent brute force attacks (max 5 login attempts per minute)

**Why it matters:** Protecting customer payment information and personal data.

---

### 5. **Scalability**
- **NFR5.1**: **Horizontal scaling**: Add more servers during high traffic
- **NFR5.2**: **Database sharding**: Partition data by city or theater
- **NFR5.3**: **Caching**: Cache movie/show data using Redis (reduce DB calls)
- **NFR5.4**: **Load balancer**: Distribute traffic across multiple servers

**Why it matters:** System should scale from 100 users to 1 million users.

**Beginner explanation:**
- **Horizontal scaling**: Add more machines (like adding more checkout counters in a store)
- **Vertical scaling**: Make one machine more powerful (like training one cashier to work faster)

---

### 6. **Maintainability**
- **NFR6.1**: **Modular code**: Separate layers (Controller, Service, Repository)
- **NFR6.2**: **SOLID principles** for clean code
- **NFR6.3**: **Logging**: Log all important operations (bookings, payments, errors)
- **NFR6.4**: **Monitoring**: Track system health, response times, error rates
- **NFR6.5**: **Documentation**: Code comments, API documentation (Swagger)

**Why it matters:** Easy to add new features, fix bugs, and onboard new developers.

---

### 7. **Usability**
- **NFR7.1**: **Simple UI**: Easy to search movies and book tickets (within 3-4 clicks)
- **NFR7.2**: **Mobile responsive**: Works on phones, tablets, desktops
- **NFR7.3**: **Accessibility**: Screen reader support for visually impaired users

---

### 8. **Data Consistency**
- **NFR8.1**: **Strong consistency** for seat booking (no stale data)
- **NFR8.2**: **Eventual consistency** acceptable for movie listings, reviews
- **NFR8.3**: **Database transactions** for booking + payment (both succeed or both fail)

**Beginner explanation:**
- **Strong consistency**: Always show the latest data (critical for seat availability)
- **Eventual consistency**: Data might be slightly outdated (okay for movie descriptions)

---

### 9. **Backup & Recovery**
- **NFR9.1**: **Daily database backups**
- **NFR9.2**: **Point-in-time recovery**: Restore database to any point in last 30 days
- **NFR9.3**: **Disaster recovery plan**: Recover system within 4 hours of major failure

---

### 10. **Legal & Compliance**
- **NFR10.1**: **GDPR compliance**: Users can delete their data
- **NFR10.2**: **Age restrictions**: Enforce age limits for R-rated movies
- **NFR10.3**: **Refund policy**: Clear cancellation and refund terms

---

## 🎯 Key Assumptions

1. **Single city initially**: System starts with one city, can expand to multiple cities
2. **Prepaid only**: No cash on counter, only online payment
3. **Show cleanup time**: 15 minutes between shows for cleaning
4. **Booking window**: Can book tickets up to 7 days in advance
5. **Cancellation policy**: Cancel up to 2 hours before show start
6. **Seat blocking**: Seats blocked for 10 minutes during booking process
7. **Currency**: Single currency (can extend to multi-currency)
8. **Language**: UI in English initially (can add localization)

---

## 📊 Success Metrics

1. **Booking success rate**: > 95% of initiated bookings should complete
2. **Average booking time**: < 3 minutes from search to confirmation
3. **System response time**: < 2 seconds for 95% of requests
4. **Zero double-bookings**: No seat should be double-booked
5. **Payment success rate**: > 98% of payment attempts should succeed

---

## 🚀 Next Steps

With these requirements defined, we'll now move to:
1. **Use Case Diagram**: Visualize actors and their interactions
2. **Class Diagrams**: Design the system structure step-by-step
3. **Sequence Diagrams**: Show how booking flow works
4. **Implementation**: Java code with design patterns

---

## 📝 Summary Table

| Category | Count | Examples |
|----------|-------|----------|
| **Functional Requirements** | 9 areas | Movie, Theater, Show, Seat, Booking, Customer, Payment, Pricing, Admin |
| **Non-Functional Requirements** | 10 areas | Performance, Concurrency, Security, Scalability, Reliability |
| **Actors** | 3 | Customer, Admin, System |
| **Core Entities** | 7 | Movie, Theater, Screen, Show, Seat, Booking, Payment |

---

**Next Document:** [02_use_case_diagram.md](./02_use_case_diagram.md) - Visual representation of user interactions
