# Interview Questions & Answers

## 🎯 Core Design Questions

### Q1: How do you prevent double-booking of the same seat?

**Answer:**
I use **pessimistic locking** with database row-level locks to prevent double-booking. Here's the approach:

1. **ShowSeat Entity:** I separated physical Seat from ShowSeat (seat availability per show)
2. **Pessimistic Lock:** When booking, I use `SELECT ... FOR UPDATE`:

```java
@Override
public ShowSeat findByIdWithLock(String showSeatId) {
    return entityManager.createQuery(
        "SELECT ss FROM ShowSeat ss WHERE ss.showSeatId = :id",
        ShowSeat.class
    )
    .setParameter("id", showSeatId)
    .setLockMode(LockModeType.PESSIMISTIC_WRITE)  // 🔒 Locks row
    .getSingleResult();
}
```

3. **Race Condition Prevented:**
```
Time    User A                      User B
────────────────────────────────────────────
14:00   SELECT C7 FOR UPDATE 🔒
14:00                               SELECT C7 FOR UPDATE (WAITING...)
14:01   UPDATE C7 = BLOCKED
14:01   COMMIT 🔓
14:01                               SELECT C7 (already BLOCKED)
14:01                               ERROR: Seat not available
```

**Follow-up: Why not optimistic locking?**

Optimistic locking allows both users to select the seat, but only one succeeds at commit time. The other gets an error AFTER they've already chosen seats - bad UX. Pessimistic locking blocks the second user immediately, giving them instant feedback.

---

### Q2: What happens if payment fails?

**Answer:**
I handle payment failures gracefully:

1. **Create Payment Record:** Even if payment fails, I create a Payment record with `status='FAILED'` for audit trail
2. **Update Booking:** Set booking `status='EXPIRED'`
3. **Release Seats:** Immediately update ShowSeats from `BLOCKED` → `AVAILABLE`
4. **Notify Customer:** Send error message with reason

```java
PaymentResult result = gateway.charge(amount);

if (!result.isSuccess()) {
    // Update payment
    payment.setStatus(PaymentStatus.FAILED);
    paymentRepository.update(payment);

    // Expire booking
    booking.setStatus(BookingStatus.EXPIRED);
    bookingRepository.update(booking);

    // Release seats
    releaseSeats(booking.getSeatIds());

    throw new PaymentFailedException(result.getError());
}
```

**Why create Payment record even on failure?**
- Fraud detection (multiple failed attempts from same user)
- Analytics (which payment methods fail most?)
- Customer support (user calls saying payment failed - we have record)

---

### Q3: How do you handle booking expiration?

**Answer:**
I use a two-pronged approach:

**1. Database-level expiry tracking:**
```java
booking.setExpiresAt(new Date().plus(10, MINUTES));
```

**2. Background job (scheduled):**
```java
@Scheduled(cron = "0 * * * * *")  // Every minute
public void releaseExpiredBookings() {
    Date now = new Date();

    // Find expired bookings
    List<Booking> expired = bookingRepository.findExpiredBookings(now);
    // SQL: SELECT * FROM bookings
    //      WHERE status='PENDING_PAYMENT' AND expires_at < NOW()

    for (Booking booking : expired) {
        booking.setStatus(BookingStatus.EXPIRED);
        releaseSeats(booking.getSeatIds());
    }
}
```

**Why not rely on application-level timers?**
- Application might crash/restart
- Timer state is lost
- Background job is stateless, can run on any server

---

### Q4: Why separate Seat and ShowSeat entities?

**Answer:**
Same physical seat has different availability for different shows.

**Problem if using just Seat:**
```java
❌ class Seat {
       private SeatStatus status;  // Status for which show???
   }

// Seat C7:
// - 3 PM show: BOOKED
// - 7 PM show: AVAILABLE
// Can't track both with one status field!
```

**Solution: ShowSeat**
```java
✅ class Seat {
       // Physical attributes only
       private String seatNumber;
       private SeatType type;
   }

✅ class ShowSeat {
       private String showId;      // Which show
       private String seatId;      // Which physical seat
       private SeatStatus status;  // Status for THIS show
   }
```

**Database:**
```sql
-- Seat table (one row per physical seat)
seat_id | screen_id | row | number
SEAT001 | SCR001    | C   | 7

-- ShowSeat table (multiple rows per seat - one per show)
show_seat_id | show_id | seat_id | status
SS001        | SHW001  | SEAT001 | BOOKED     (3 PM show)
SS002        | SHW002  | SEAT001 | AVAILABLE  (7 PM show)
```

---

### Q5: How would you scale this system to 1 million concurrent users?

**Answer:**

**1. Database Scaling:**
- **Read Replicas:** Read-heavy operations (search movies, view shows) from replicas
- **Sharding:** Partition by city (Delhi DB, Mumbai DB, etc.)
- **Connection Pooling:** HikariCP with 20-50 connections per instance

**2. Caching:**
- **Redis:** Cache movie listings, show timings (5-minute TTL)
- **Cache Invalidation:** When show added/cancelled, clear cache for that theater+date

```java
@Cacheable(value = "shows", key = "#movieId + '-' + #city + '-' + #date")
public List<Show> getShows(String movieId, String city, Date date) {
    return showRepository.findByMovieAndCityAndDate(movieId, city, date);
}
```

**3. Horizontal Scaling:**
- **Load Balancer:** Nginx/AWS ALB distributes traffic across multiple app servers
- **Stateless Services:** No session state, can scale up/down easily
- **Database Connection Pool:** Each server maintains own connection pool

**4. Async Processing:**
- **Message Queue (RabbitMQ/Kafka):** For notifications
- **Payment Processing:** Async with webhooks (don't block booking flow)

**5. Database Optimization:**
- **Indexes:** On show_date, show_time, city, status
- **Partitioning:** Partition bookings table by month

**Architecture:**
```
[Load Balancer]
       ↓
[App Server 1] [App Server 2] ... [App Server N]
       ↓
[Redis Cache]  ← Hot data
       ↓
[DB Master]  ← Writes (bookings, payments)
       ↓
[DB Replica 1] [DB Replica 2]  ← Reads (search, view)
       ↓
[Message Queue]  ← Async notifications
```

---

## 🎨 Design Pattern Questions

### Q6: Which design patterns did you use and why?

**Answer:**

**1. Repository Pattern:**
- **Why:** Separate data access from business logic
- **Benefit:** Can swap MySQL with MongoDB without changing BookingService

```java
public class BookingService {
    private BookingRepository repo;  // Interface, not JpaBookingRepository!

    // Can inject JpaBookingRepository OR InMemoryBookingRepository
}
```

**2. Strategy Pattern (Payment):**
- **Why:** Multiple payment methods (UPI, Card, Wallet)
- **Benefit:** Easy to add new payment method without modifying existing code

```java
Map<PaymentMethod, PaymentStrategy> strategies;
PaymentStrategy strategy = strategies.get(payment.getMethod());
strategy.processPayment(amount);
```

**3. Factory Pattern:**
- **Why:** Complex object creation (Booking with validation, defaults)
- **Benefit:** Centralized creation logic

**4. Observer Pattern:**
- **Why:** Multiple notification channels (Email, SMS, Push)
- **Benefit:** BookingService doesn't know about Email/SMS implementation

**5. Singleton Pattern:**
- **Why:** Database connection pool
- **Benefit:** Single instance, shared connections

---

### Q7: How does your design follow SOLID principles?

**Answer:**

**S - Single Responsibility:**
```java
✅ MovieService   - Only movie operations
✅ BookingService - Only booking operations
✅ PaymentService - Only payment processing

❌ BookingService handling movies + bookings + payments = violates SRP
```

**O - Open/Closed:**
```java
// Adding new payment method (e.g., Cryptocurrency)
✅ Create CryptoPaymentStrategy (NEW CLASS)
   WITHOUT modifying PaymentService

❌ Adding if-else in PaymentService.processPayment()
```

**L - Liskov Substitution:**
```java
// Can substitute any Repository implementation
BookingRepository repo = new JpaBookingRepository();
// OR
BookingRepository repo = new InMemoryBookingRepository();

// BookingService code remains same!
```

**I - Interface Segregation:**
```java
❌ One huge Repository interface with 50 methods
✅ Specific interfaces: BookingRepository, MovieRepository
```

**D - Dependency Inversion:**
```java
✅ BookingService depends on BookingRepository (interface)
   NOT on JpaBookingRepository (implementation)

// Injected via constructor
public BookingService(BookingRepository repo) {
    this.repo = repo;
}
```

---

## 🔧 Technical Questions

### Q8: How do you handle transactions?

**Answer:**
I use **@Transactional** annotation for atomicity:

```java
@Transactional  // All or nothing!
public Booking createBooking(BookingRequest request) {
    blockSeats(seatIds);         // Step 1
    Booking b = new Booking();
    bookingRepository.save(b);   // Step 2

    // If Step 2 fails, Step 1 is rolled back
    // Seats are NOT blocked
}
```

**Transaction scope:**
- **createBooking:** Block seats + save booking
- **confirmBooking:** Update booking + book seats + create payment
- **cancelBooking:** Update booking + refund + release seats

**Why transactional?**
Without it, if booking save fails after blocking seats, seats remain blocked forever!

---

### Q9: What indexes would you create?

**Answer:**

**1. show_seats table (most critical):**
```sql
CREATE INDEX idx_show ON show_seats(show_id);          -- Get all seats for a show
CREATE INDEX idx_status ON show_seats(status);          -- Find available seats
CREATE INDEX idx_blocked_at ON show_seats(blocked_at);  -- Background job
```

**2. bookings table:**
```sql
CREATE INDEX idx_customer ON bookings(customer_id);     -- User's booking history
CREATE INDEX idx_expires_at ON bookings(expires_at);    -- Find expired bookings
CREATE INDEX idx_status ON bookings(status);            -- Filter by status
```

**3. shows table:**
```sql
CREATE INDEX idx_movie ON shows(movie_id);
CREATE INDEX idx_date_time ON shows(show_date, show_time);  -- Compound index
CREATE INDEX idx_screen ON shows(screen_id);
```

**Compound index example:**
```sql
-- Fast query with compound index
SELECT * FROM shows
WHERE show_date = '2025-11-18'
  AND show_time BETWEEN '15:00' AND '20:00';
```

---

### Q10: How do you handle race conditions?

**Answer:**

**Race Condition:** Two users booking same seat simultaneously

**Solution: Pessimistic Locking**
```java
// User A
ShowSeat seat = repo.findByIdWithLock("C7");  // 🔒 Locks row

// User B (happens simultaneously)
ShowSeat seat = repo.findByIdWithLock("C7");  // ⏳ WAITS for lock

// User A
seat.block();
repo.update(seat);  // Commits, releases lock 🔓

// User B (now gets lock)
// seat.status is already BLOCKED
if (!seat.isAvailable()) {
    throw new SeatNotAvailableException();
}
```

**Alternative: Optimistic Locking**
```java
@Version
private int version;  // Incremented on each update

// Both users read: version = 1
// User A updates: version = 2 ✅
// User B updates: version = 2 (expects 1) ❌ Conflict!
```

**I prefer pessimistic** because:
- Prevents users from selecting unavailable seats
- Better UX (immediate feedback)
- Rare conflicts in practice (users select different seats)

---

## 🚀 System Design Questions

### Q11: How would you handle a flash sale (new movie release)?

**Answer:**

**Problem:** 1 million users try to book tickets at 12:00 AM for "Avengers" premiere

**Solution:**

**1. Pre-create ShowSeat entries:**
```java
// Before release, create all ShowSeat entries
// Don't wait for first booking
for (Show show : shows) {
    initializeShowSeats(show.getShowId());
}
```

**2. Queue-based booking:**
```
[Users] → [Queue (Redis)] → [Workers (10 servers)] → [Database]

User → "You're in queue. Position: 12,453"
Workers → Process 1000 bookings/sec
```

**3. Cache seat availability:**
```java
@Cacheable(value = "seat-availability", key = "#showId", ttl = 10)
public List<ShowSeat> getSeatAvailability(String showId) {
    return showSeatRepo.findByShowId(showId);
}
```

**4. Rate limiting:**
```java
@RateLimiter(limit = 10, window = "1 minute", per = "IP")
public Booking createBooking() {
    // Max 10 booking attempts per IP per minute
}
```

**5. Database optimizations:**
- Read replicas for seat availability queries
- Master only for booking writes
- Connection pool size: 100 (from 20)

---

### Q12: How would you design the payment integration?

**Answer:**

**Architecture:**
```
[BookingService] → [PaymentService] → [PaymentGateway (Stripe/Razorpay)]
                                            ↓
                                    [Webhook Handler]
                                            ↓
                                    [BookingService.confirmBooking()]
```

**Flow:**

**Step 1: Initiate Payment**
```java
public Payment initiatePayment(Booking booking) {
    // Create payment record
    Payment payment = new Payment();
    payment.setStatus(PaymentStatus.PENDING);
    paymentRepo.save(payment);

    // Call gateway
    String paymentUrl = gateway.createPaymentSession(booking.getTotalAmount());

    // Redirect user to paymentUrl
    return payment;
}
```

**Step 2: Webhook Handler (Async)**
```java
@PostMapping("/webhooks/payment")
public void paymentWebhook(@RequestBody PaymentWebhook webhook) {
    // Verify signature
    if (!gateway.verifySignature(webhook)) {
        throw new SecurityException("Invalid signature");
    }

    // Update payment
    Payment payment = paymentRepo.findByGatewayId(webhook.getPaymentId());
    payment.setStatus(webhook.getStatus());
    payment.setTransactionId(webhook.getTransactionId());
    paymentRepo.update(payment);

    // Confirm booking
    if (webhook.getStatus() == "success") {
        bookingService.confirmBooking(payment.getBookingId(), payment);
    } else {
        bookingService.expireBooking(payment.getBookingId());
    }
}
```

**Why webhooks?**
- Don't make user wait for payment processing (might take 10 seconds)
- Payment gateway calls our webhook when done
- Async, non-blocking

**Security:**
- Verify webhook signature (HMAC)
- Use HTTPS only
- Idempotency (same webhook might be sent twice)

---

### Q13: How would you handle refunds?

**Answer:**

**Refund Policy:**
```
> 24 hours before show: 100% refund
2-24 hours before show: 50% refund
< 2 hours before show: No refund
```

**Implementation:**
```java
public boolean cancelBooking(String bookingId) {
    Booking booking = bookingRepo.findById(bookingId);
    Show show = showRepo.findById(booking.getShowId());

    // Calculate hours until show
    long hoursUntilShow = calculateHoursUntilShow(show);

    double refundAmount;
    if (hoursUntilShow > 24) {
        refundAmount = booking.getTotalAmount();  // 100%
    } else if (hoursUntilShow >= 2) {
        refundAmount = booking.getTotalAmount() * 0.5;  // 50%
    } else {
        throw new CancellationNotAllowedException("Too late to cancel");
    }

    // Process refund via gateway
    Payment payment = paymentRepo.findByBookingId(bookingId);
    RefundResult result = gateway.refund(payment.getTransactionId(), refundAmount);

    if (result.isSuccess()) {
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundTransactionId(result.getRefundId());
        paymentRepo.update(payment);

        // Release seats
        releaseSeats(booking.getSeatIds());

        return true;
    }

    return false;
}
```

**Partial Refund Tracking:**
```java
class Payment {
    private double amount;             // Original: 400
    private double refundedAmount;     // Refunded: 200 (50%)
    private PaymentStatus status;      // PARTIALLY_REFUNDED
}
```

---

## 📝 Common Mistakes & Solutions

### Q14: What are common mistakes beginners make in this design?

**Answer:**

**Mistake 1: Not separating Seat and ShowSeat**
```java
❌ class Seat {
       private SeatStatus status;  // For which show???
   }

✅ class Seat { /* physical attributes */ }
   class ShowSeat { /* status per show */ }
```

**Mistake 2: No locking for seat selection**
```java
❌ ShowSeat seat = repo.findById(id);
   if (seat.isAvailable()) {
       seat.block();  // Race condition!
   }

✅ ShowSeat seat = repo.findByIdWithLock(id);  // Lock acquired
   if (seat.isAvailable()) {
       seat.block();  // Safe!
   }
```

**Mistake 3: No booking expiration**
```java
❌ Seats stay BLOCKED forever if payment not completed

✅ booking.setExpiresAt(now + 10 minutes);
   Background job releases expired bookings
```

**Mistake 4: Storing credit card details**
```java
❌ class Payment {
       private String cardNumber;  // NEVER!
       private String cvv;         // NEVER!
   }

✅ Use payment gateway (Stripe, Razorpay)
   Store only transactionId
```

**Mistake 5: No transaction boundaries**
```java
❌ public void createBooking() {
       blockSeats();     // Succeeds
       saveBooking();    // Fails
       // Seats remain blocked!
   }

✅ @Transactional
   public void createBooking() {
       blockSeats();
       saveBooking();
       // If saveBooking fails, blockSeats is rolled back
   }
```

---

## 📝 Summary

**Key Interview Points:**
1. **Prevent double-booking:** Pessimistic locking with `FOR UPDATE`
2. **Separate Seat and ShowSeat:** Physical vs availability per show
3. **Booking expiration:** Database expiry + background job
4. **Transactions:** `@Transactional` for atomicity
5. **SOLID principles:** Single Responsibility, Dependency Inversion
6. **Design patterns:** Repository, Strategy, Factory, Observer
7. **Scalability:** Read replicas, caching, sharding, message queues

**Next Document:** [README.md](./README.md) - Project overview
