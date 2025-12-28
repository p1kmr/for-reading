# Car Rental System - Requirements Document

## Overview
A comprehensive car rental system that allows customers to search, book, modify, and cancel car reservations with integrated payment processing and notifications.

---

## 1. Functional Requirements

### 1.1 User Management
- **FR-1.1**: System must allow customers to register with name, email, phone, and driver's license details
- **FR-1.2**: System must validate driver's license validity (expiry date)
- **FR-1.3**: System must authenticate users before allowing reservations
- **FR-1.4**: Admin users can add, update, or remove cars from the system

### 1.2 Car Inventory Management
- **FR-2.1**: System must maintain car inventory with details:
  - Car ID (unique identifier)
  - Make and Model (e.g., Toyota Camry)
  - Year of manufacture
  - Car type (Sedan, SUV, Truck, Luxury, etc.)
  - Daily rental rate
  - Current status (Available, Rented, Under Maintenance, Retired)
  - Location (branch/city)
  - Features (GPS, sunroof, automatic transmission, etc.)

- **FR-2.2**: Admin can add new cars to inventory
- **FR-2.3**: Admin can update car status and details
- **FR-2.4**: Admin can mark cars for maintenance or retire them

### 1.3 Search & Browse
- **FR-3.1**: Customers can search available cars by:
  - Date range (pickup and return dates)
  - Location (city/branch)
  - Car type
  - Price range
  - Features/amenities

- **FR-3.2**: System must show real-time availability
- **FR-3.3**: Search results must display car details and daily rates
- **FR-3.4**: System must calculate total cost based on rental period

### 1.4 Reservation Management
- **FR-4.1**: Customer can create a new reservation with:
  - Selected car
  - Pickup date and time
  - Return date and time
  - Pickup location
  - Drop-off location (same or different)

- **FR-4.2**: System must validate:
  - Car availability for the requested period
  - No overlapping reservations for the same car
  - Valid driver's license
  - Future booking dates only

- **FR-4.3**: Customer can view all their reservations (past and upcoming)
- **FR-4.4**: Customer can modify an existing reservation:
  - Change dates (if car is available)
  - Upgrade/downgrade car (with price adjustment)
  - Cannot modify within 24 hours of pickup

- **FR-4.5**: Customer can cancel a reservation:
  - Full refund if cancelled 48+ hours before pickup
  - 50% refund if cancelled 24-48 hours before pickup
  - No refund if cancelled within 24 hours

- **FR-4.6**: System automatically cancels unpaid reservations after 15 minutes

### 1.5 Payment Processing
- **FR-5.1**: System must support multiple payment methods:
  - Credit/Debit Card
  - PayPal
  - Cryptocurrency (optional)
  - Cash at pickup (hold reservation)

- **FR-5.2**: System must process payment securely via payment gateway
- **FR-5.3**: System must generate payment confirmation and transaction ID
- **FR-5.4**: System must handle refunds for cancellations
- **FR-5.5**: System must store payment history for audit

### 1.6 Notifications
- **FR-6.1**: System must send email confirmation upon successful reservation
- **FR-6.2**: System must send SMS reminder 24 hours before pickup
- **FR-6.3**: System must notify about payment confirmation
- **FR-6.4**: System must notify about cancellation and refund status
- **FR-6.5**: System must notify admin about new reservations

### 1.7 Reporting & Analytics
- **FR-7.1**: Admin can view reports on:
  - Revenue per car/location/time period
  - Most rented car types
  - Occupancy rates
  - Customer booking patterns

---

## 2. Non-Functional Requirements

### 2.1 Performance
- **NFR-1.1**: Search results must load within 2 seconds for 10,000+ cars
- **NFR-1.2**: Reservation creation must complete within 3 seconds
- **NFR-1.3**: System must handle 1000 concurrent users
- **NFR-1.4**: API response time should be < 500ms for 95% of requests

### 2.2 Scalability
- **NFR-2.1**: System must scale horizontally to handle peak loads (holidays, weekends)
- **NFR-2.2**: Database must support partitioning/sharding for large datasets
- **NFR-2.3**: Caching layer (Redis) for frequently accessed data (available cars, search results)

### 2.3 Availability & Reliability
- **NFR-3.1**: System uptime must be 99.9% (maximum 8.76 hours downtime/year)
- **NFR-3.2**: Graceful degradation: if payment gateway is down, allow cash bookings
- **NFR-3.3**: Database replication for high availability
- **NFR-3.4**: Automatic failover for critical services

### 2.4 Concurrency & Consistency
- **NFR-4.1**: **CRITICAL**: Prevent double-booking of the same car
  - Use pessimistic locking (SELECT FOR UPDATE) during reservation
  - Use optimistic locking with version numbers for updates
  - Transaction isolation level: REPEATABLE READ

- **NFR-4.2**: Handle race conditions when multiple users book the same car simultaneously
- **NFR-4.3**: Eventual consistency acceptable for search results (5-minute cache)
- **NFR-4.4**: Strong consistency required for reservations and payments

### 2.5 Security
- **NFR-5.1**: All sensitive data encrypted at rest (AES-256)
- **NFR-5.2**: All communication over HTTPS (TLS 1.3)
- **NFR-5.3**: Authentication using JWT tokens (stateless)
- **NFR-5.4**: Payment card data must be PCI DSS compliant (never store CVV)
- **NFR-5.5**: Rate limiting to prevent DDoS (100 requests/minute per user)
- **NFR-5.6**: Input validation to prevent SQL injection, XSS

### 2.6 Maintainability
- **NFR-6.1**: Modular architecture with clear separation of concerns
- **NFR-6.2**: SOLID principles must be followed
- **NFR-6.3**: Code coverage > 80% for unit tests
- **NFR-6.4**: API documentation using Swagger/OpenAPI
- **NFR-6.5**: Logging for all critical operations (ELK stack)

### 2.7 Usability
- **NFR-7.1**: Mobile-responsive design
- **NFR-7.2**: Support for multiple languages (i18n)
- **NFR-7.3**: Accessible (WCAG 2.1 Level AA compliance)

### 2.8 Data Integrity
- **NFR-8.1**: Database ACID properties must be maintained
- **NFR-8.2**: Soft delete for reservations and payments (audit trail)
- **NFR-8.3**: Data retention policy: keep records for 7 years
- **NFR-8.4**: Regular automated backups (daily full, hourly incremental)

---

## 3. Assumptions

1. **Single Currency**: System operates in USD only (internationalization future scope)
2. **Same City Returns**: Cars must be returned to the same location (no one-way rentals initially)
3. **Age Restriction**: Drivers must be 21+ years old
4. **Insurance**: Basic insurance included in daily rate
5. **Fuel Policy**: Cars are provided with full tank and must be returned full
6. **Business Hours**: Pickup/drop-off between 6 AM - 10 PM
7. **Advance Booking**: Reservations can be made up to 6 months in advance
8. **Minimum Rental**: Minimum rental period is 1 day
9. **Payment Timing**: Payment must be completed within 15 minutes of reservation creation
10. **Car Maintenance**: Cars require mandatory service every 10,000 km or 6 months

---

## 4. Constraints

1. **Technology Stack**:
   - Backend: Java 17+ with Spring Boot
   - Database: MySQL 8.0+ (relational)
   - Cache: Redis
   - Message Queue: RabbitMQ or Kafka
   - Payment Gateway: Stripe or PayPal API

2. **Budget**: Limited to cloud infrastructure costs < $5000/month initially

3. **Team Size**: 3 backend developers, 2 frontend developers, 1 DevOps

4. **Timeline**: MVP in 3 months

5. **Compliance**: Must comply with:
   - GDPR (data privacy)
   - PCI DSS (payment security)
   - Local rental regulations

---

## 5. Out of Scope (Future Enhancements)

1. Dynamic pricing based on demand
2. Loyalty program and rewards
3. Mobile app (iOS/Android)
4. Integration with third-party insurance providers
5. AI-based car recommendations
6. Multi-language support (initial version English only)
7. Corporate accounts and bulk bookings
8. Car damage reporting and claims
9. GPS tracking of rented vehicles
10. Airport pickup/drop-off service

---

## 6. Success Criteria

1. ✅ Zero double-bookings in production
2. ✅ 95% of payments processed successfully
3. ✅ Average search time < 2 seconds
4. ✅ 99.9% system uptime
5. ✅ 80%+ customer satisfaction rating
6. ✅ Handle Black Friday traffic (10x normal load)

---

## Next Steps
- Create Use Case Diagrams
- Design Class Diagrams (incremental approach)
- Define Database Schema
- Design API endpoints
- Plan concurrency handling strategy
