# Interview Q&A & Whiteboard Checklist

## Part 1: Common Interview Questions

---

### Q1: How would you prevent double-booking of cars?

**Answer**:

Multiple approaches exist, each with trade-offs:

**1. Pessimistic Locking (Recommended for this system)**:
```java
@Transactional
public Reservation createReservation(...) {
    // Lock the car row using SELECT FOR UPDATE
    Car car = entityManager.find(Car.class, carId, LockModeType.PESSIMISTIC_WRITE);

    // Check for overlapping reservations
    List<Reservation> overlapping = findOverlappingReservations(carId, startDate, endDate);

    if (!overlapping.isEmpty()) {
        throw new CarAlreadyBookedException();
    }

    // Create reservation (other transactions wait here)
    Reservation reservation = ...;
    entityManager.persist(reservation);

    return reservation;
}
```

**Trade-offs**:
- ✅ Pros: Prevents double-booking 100%, simple implementation
- ❌ Cons: Lower throughput, potential deadlocks

**2. Optimistic Locking**:
- Use `@Version` field
- Retry on conflict
- Better for high-concurrency, but users may see "try again" errors

**3. Distributed Lock (Redis)**:
- Works across multiple app servers
- Lock per car: `lock:car:{carId}`
- Release lock after reservation

**My recommendation**: Pessimistic locking because:
- Reservations are critical (can't fail)
- Medium traffic expected (not millions of concurrent bookings)
- Acceptable wait time for users

---

### Q2: How would you handle payment processing failures?

**Answer**:

**Multi-layered approach**:

**1. Retry Logic with Exponential Backoff**:
```java
public Payment processPayment(Reservation reservation) {
    int maxRetries = 3;
    int retryCount = 0;

    while (retryCount < maxRetries) {
        try {
            return strategy.processPayment(reservation.getTotalCost());
        } catch (PaymentGatewayException e) {
            retryCount++;
            if (retryCount >= maxRetries) {
                // Move to dead letter queue
                paymentRetryQueue.send(reservation);
                throw new PaymentFailedException();
            }
            // Exponential backoff: 2s, 4s, 8s
            Thread.sleep(1000 * Math.pow(2, retryCount));
        }
    }
}
```

**2. Idempotency**:
- Store `paymentId` to prevent duplicate charges
- Check if payment already processed before retrying

**3. Graceful Degradation**:
- If payment gateway down, allow "Cash at pickup" option
- Keep reservation in "PENDING_PAYMENT" status

**4. Background Workers**:
- Failed payments go to `payment-retry-queue`
- Worker retries periodically
- Admin notification after 3 failures

**5. Compensation (Saga Pattern)**:
- If payment succeeds but confirmation fails:
  - Reverse payment (refund)
  - Cancel reservation
  - Maintain consistency

---

### Q3: Design trade-offs: Why use Strategy Pattern for payments instead of if-else?

**Answer**:

**Without Strategy (Bad)**:
```java
public void processPayment(PaymentMethod method) {
    if (method == CREDIT_CARD) {
        // Stripe logic
    } else if (method == PAYPAL) {
        // PayPal logic
    } else if (method == CRYPTO) {
        // Crypto logic
    }
    // ❌ To add Apple Pay, must MODIFY this class!
}
```

**Problems**:
- ❌ Violates Open/Closed Principle
- ❌ Hard to test (how to test all branches?)
- ❌ Growing complexity (10+ payment methods = giant if-else)

**With Strategy (Good)**:
```java
interface PaymentStrategy {
    boolean processPayment(double amount);
}

class CreditCardStrategy implements PaymentStrategy { }
class PayPalStrategy implements PaymentStrategy { }
class CryptoStrategy implements PaymentStrategy { }

// ✅ Add new method: just create new class!
class ApplePayStrategy implements PaymentStrategy { }
```

**Benefits**:
- ✅ Open/Closed: Add new methods without modifying existing code
- ✅ Testable: Test each strategy independently
- ✅ Single Responsibility: Each strategy handles one payment method
- ✅ Runtime flexibility: Switch strategies dynamically

**When NOT to use Strategy**:
- Only 1-2 payment methods (overkill)
- Logic is trivial (simple calculations)

---

### Q4: How do you ensure data consistency across services?

**Answer**:

**1. Transactional Boundaries**:
```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public Reservation createReservation(...) {
    // All operations in one transaction
    reservationRepo.save(reservation);
    carRepo.updateStatus(carId, RENTED);
    paymentRepo.save(payment);
    // Either all succeed or all rollback
}
```

**2. Database Constraints**:
```sql
ALTER TABLE reservations
ADD CONSTRAINT chk_dates CHECK (end_date > start_date);

ALTER TABLE reservations
ADD CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id);
```

**3. Eventual Consistency for Non-Critical Operations**:
- Notifications: Async via message queue (if fails, retry later)
- Analytics: Can be delayed by a few minutes
- Cache: Eventual consistency (5-minute TTL)

**4. Saga Pattern for Distributed Transactions**:
If payment service and reservation service are separate microservices:
- **Choreography**: Each service publishes events, others react
- **Orchestration**: Central coordinator manages the flow

**5. Idempotency**:
- Use unique IDs for operations
- Prevent duplicate processing (check before insert)

---

### Q5: How would you scale the system to handle 10x more traffic?

**Answer**:

**1. Horizontal Scaling (Application Tier)**:
- Stateless app servers → easy to scale
- Load balancer distributes traffic
- Auto-scaling based on CPU/memory

**2. Database Scaling**:
- **Read Replicas**: Route read queries to replicas
- **Sharding**: Partition data by region/customer ID
- **Connection Pooling**: Reuse connections (HikariCP)

**3. Caching**:
- **Redis**: Cache search results, session data
- **CDN**: Cache static assets (images, CSS, JS)
- **Application-level**: Cache frequently accessed data

**4. Async Processing**:
- **Message Queue**: Notifications, emails, SMS
- **Background Workers**: Non-critical tasks
- **Event-Driven**: React to events instead of synchronous calls

**5. Database Optimization**:
- **Indexes**: Create indexes on frequently queried columns
- **Query Optimization**: Avoid N+1 queries
- **Denormalization**: For read-heavy tables
- **Archiving**: Move old data to archive tables

**6. Content Delivery**:
- **CDN**: Serve static content from edge locations
- **Geographic Distribution**: Deploy in multiple regions

**7. Rate Limiting**:
- Prevent abuse (100 requests/minute per user)
- Protect backend from overload

---

### Q6: Explain the difference between Association, Aggregation, and Composition.

**Answer**:

| Relationship | Ownership | Lifecycle | Example | Code |
|--------------|-----------|-----------|---------|------|
| **Association** | No | Independent | Reservation → Customer | `private Customer customer;` (passed via constructor) |
| **Aggregation** | Shared | Independent | Company ◇→ Location | `private List<Location> locations;` (passed from outside) |
| **Composition** | Strong | Dependent | Car ◆→ Engine | `private Engine engine = new Engine();` (created inside) |

**Quick Test**:
- If **Part** is created **inside** Whole → **Composition**
- If **Part** is passed from **outside** → **Association/Aggregation**
- If **Part** can exist independently → **Aggregation**
- If **Part** cannot exist without Whole → **Composition**

**Example**:
```java
// Composition: Engine created inside Car
class Car {
    private Engine engine;
    public Car() {
        this.engine = new Engine();  // Created here!
    }
}

// Association: Customer passed from outside
class Reservation {
    private Customer customer;
    public Reservation(Customer customer) {
        this.customer = customer;  // Passed in!
    }
}
```

---

### Q7: How would you handle concurrent reservation modifications?

**Answer**:

**Scenario**: User modifies reservation while system is auto-cancelling it (payment timeout).

**Solution: Optimistic Locking**:
```java
@Entity
public class Reservation {
    @Version
    private int version;  // Incremented on every update
}

@Transactional
public void modifyReservation(String id, Date newStartDate) {
    try {
        Reservation reservation = reservationRepo.findById(id);

        // Check version
        reservation.setStartDate(newStartDate);
        reservationRepo.update(reservation);  // Version check happens here

    } catch (OptimisticLockException e) {
        // Version conflict!
        throw new ConcurrentModificationException(
            "Reservation was modified by another process. Please refresh and try again."
        );
    }
}
```

**SQL**:
```sql
UPDATE reservations
SET start_date = ?, version = version + 1
WHERE reservation_id = ?
  AND version = ?;  -- ⚡ Version check!

-- If 0 rows updated → version mismatch → exception
```

---

### Q8: What design patterns did you use and why?

**Answer**:

| Pattern | Problem | Solution | Benefit |
|---------|---------|----------|---------|
| **Strategy** | Multiple payment methods | `PaymentStrategy` interface | Add new methods without modifying code |
| **Factory** | Complex reservation creation | `ReservationFactory` | Centralize validation and calculations |
| **Repository** | Data access abstraction | `CarRepository` interface | Easy testing, switch databases |
| **Observer** | Multiple notification channels | `NotificationObserver` | Decouple notification logic |
| **Singleton** | Single DB connection pool | `DatabaseConnectionPool` | Resource management |

**Example**:
```java
// Strategy Pattern
interface PaymentStrategy {
    boolean processPayment(double amount);
}

class CreditCardStrategy implements PaymentStrategy { }
class PayPalStrategy implements PaymentStrategy { }

// Easy to add new strategies!
class CryptoStrategy implements PaymentStrategy { }
```

---

### Q9: How do you ensure car availability is accurate in real-time?

**Answer**:

**Multi-pronged approach**:

**1. Database Query (Source of Truth)**:
```sql
SELECT c.*
FROM cars c
WHERE c.car_id NOT IN (
    SELECT r.car_id
    FROM reservations r
    WHERE r.start_date <= ?  -- requested end
      AND r.end_date >= ?    -- requested start
      AND r.status IN ('CONFIRMED', 'ACTIVE')
);
```

**2. Cache Invalidation**:
- When reservation created/cancelled → invalidate cache
- Cache TTL: 5 minutes (balance freshness vs performance)

**3. Pessimistic Locking During Booking**:
- Lock car row during reservation creation
- Prevents race conditions

**4. Heartbeat/Cleanup Job**:
- Cron job runs every hour
- Auto-cancel reservations with status "PENDING_PAYMENT" > 15 minutes
- Free up cars

**5. Eventually Consistent Search**:
- Search can show slightly stale data (acceptable)
- Final availability check during booking (real-time)

---

### Q10: How would you handle rollback if payment succeeds but notification fails?

**Answer**:

**Two approaches**:

**Approach 1: Two-Phase Commit (NOT Recommended)**:
- Coordinate payment and notification
- Complex, error-prone

**Approach 2: Eventual Consistency (Recommended)**:
```java
@Transactional
public Reservation createReservation(...) {
    // Step 1: Create reservation
    Reservation reservation = ...;
    reservationRepo.save(reservation);

    // Step 2: Process payment (within transaction)
    Payment payment = paymentService.processPayment(...);
    paymentRepo.save(payment);

    // COMMIT TRANSACTION HERE

    // Step 3: Send notification (OUTSIDE transaction)
    try {
        notificationService.send(reservation);
    } catch (Exception e) {
        // ⚠️ Notification failed but reservation is saved!
        // Log error, send to retry queue
        logger.error("Notification failed for reservation: " + reservation.getId());
        notificationRetryQueue.send(reservation);
    }

    return reservation;
}
```

**Why this works**:
- Notification is NOT critical for business logic
- Can retry later (eventual consistency)
- Reservation and payment are consistent (both in DB)

**If notification MUST succeed**:
- Use **Saga Pattern** with compensation
- If notification fails → refund payment → cancel reservation

---

## Part 2: Whiteboard Checklist

### What to Draw First (Priority Order)

When designing a system on a whiteboard in an interview, follow this order:

---

### ✅ Step 1: Clarify Requirements (5 minutes)
**Ask questions**:
- How many cars? How many users?
- Read-heavy or write-heavy?
- Strong consistency or eventual consistency?
- Which features are critical? (search, book, cancel)

**Document**:
- Functional requirements (bullet points)
- Non-functional requirements (performance, scale)

---

### ✅ Step 2: Draw Core Entities (5 minutes)
**Start with NOUNS**:
```
┌─────────┐    ┌──────────┐    ┌─────────────┐
│   Car   │    │ Customer │    │ Reservation │
└─────────┘    └──────────┘    └─────────────┘
```

**Add key attributes**:
```
Car:
- carId
- make, model
- dailyRate
- status

Customer:
- customerId
- name, email
- driverLicense

Reservation:
- reservationId
- car, customer
- dates
- totalCost
```

---

### ✅ Step 3: Draw Relationships (3 minutes)
```
Reservation ──> Customer
Reservation ──> Car
```

**Explain cardinalities**:
- One customer can have many reservations
- One car can have many reservations
- One reservation belongs to one customer and one car

---

### ✅ Step 4: Add Service Layer (5 minutes)
**Draw services**:
```
┌──────────────────────┐
│ ReservationService   │ → Creates/cancels reservations
├──────────────────────┤
│ CarService           │ → Searches cars, checks availability
├──────────────────────┤
│ PaymentService       │ → Processes payments
└──────────────────────┘
```

**Explain responsibilities**:
- Each service has one job (Single Responsibility)

---

### ✅ Step 5: Add Repository Layer (3 minutes)
```
┌────────────────┐
│ CarRepository  │ → Interface (findAvailable)
├────────────────┤
│ JpaCarRepo     │ → Implementation (MySQL)
└────────────────┘
```

**Explain**: Services depend on interfaces (Dependency Inversion)

---

### ✅ Step 6: Critical Flow: Create Reservation (7 minutes)
**Draw sequence**:
```
User → Controller → ReservationService → CarService (check availability)
                                       → PaymentService (process payment)
                                       → NotificationService (send email)
```

**Highlight**:
- Lock car row (prevent double-booking)
- Transaction boundary
- Error handling

---

### ✅ Step 7: Handle Concurrency (5 minutes)
**Explain locking**:
```sql
BEGIN TRANSACTION;
SELECT * FROM cars WHERE car_id = ? FOR UPDATE;  -- Lock!
-- Check overlaps
-- Create reservation
COMMIT;
```

**Trade-offs**:
- Pessimistic: Lower throughput, prevents conflicts
- Optimistic: Higher throughput, retry on conflict

---

### ✅ Step 8: Deployment Architecture (5 minutes)
**Draw boxes**:
```
[Load Balancer]
      ↓
[App Server 1] [App Server 2] [App Server 3]
      ↓
[Database: MySQL Primary + Replicas]
[Cache: Redis]
[Queue: RabbitMQ]
```

**Explain scaling**:
- Horizontal scaling (add more app servers)
- Read replicas (scale reads)
- Caching (reduce DB load)

---

### ✅ Step 9: Design Patterns (if time permits)
**Mention briefly**:
- Strategy (payments)
- Factory (reservation creation)
- Repository (data access)
- Observer (notifications)

---

### ⏱️ Time Management (45-minute interview)

| Step | Time | Total |
|------|------|-------|
| Clarify requirements | 5 min | 5 min |
| Draw entities | 5 min | 10 min |
| Draw relationships | 3 min | 13 min |
| Add services | 5 min | 18 min |
| Add repositories | 3 min | 21 min |
| Create reservation flow | 7 min | 28 min |
| Concurrency handling | 5 min | 33 min |
| Deployment architecture | 5 min | 38 min |
| Q&A buffer | 7 min | 45 min |

---

## Common Beginner Mistakes in Interviews

### ❌ Mistake 1: Jumping to Code Too Early
**Fix**: Start with high-level design, then drill down

### ❌ Mistake 2: Not Asking Clarifying Questions
**Fix**: Always clarify requirements first!

### ❌ Mistake 3: Ignoring Non-Functional Requirements
**Fix**: Discuss scalability, consistency, availability

### ❌ Mistake 4: Forgetting Edge Cases
**Fix**: Mention: double-booking, payment failures, concurrent updates

### ❌ Mistake 5: Not Explaining Trade-Offs
**Fix**: "I chose pessimistic locking because... but optimistic would work if..."

### ❌ Mistake 6: Overengineering
**Fix**: Start simple, then add complexity if asked

### ❌ Mistake 7: Not Listening to Interviewer
**Fix**: If they redirect, follow their lead!

---

## Quick Reference Card

### Key Concepts to Remember
- **SOLID Principles**: Single Responsibility, Open/Closed, Liskov, Interface Segregation, Dependency Inversion
- **Design Patterns**: Strategy, Factory, Repository, Observer, Singleton
- **Concurrency**: Pessimistic locking, Optimistic locking, Distributed locks
- **Scaling**: Horizontal scaling, Caching, Read replicas, Sharding
- **Consistency**: ACID transactions, Isolation levels, Eventual consistency

---

## Next Steps
✅ Interview Q&A with detailed answers
✅ Whiteboard checklist with time management
✅ Common mistakes and fixes
➡️ Final summary document
➡️ Complete Java code implementation
