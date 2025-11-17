# Complete Java Code Implementation

## 📦 Project Structure

```
src/main/java/com/moviebooking/
├── domain/               # Entities (POJOs)
│   ├── Movie.java
│   ├── Theater.java
│   ├── Screen.java
│   ├── Seat.java
│   ├── Show.java
│   ├── ShowSeat.java
│   ├── Customer.java
│   ├── Booking.java
│   └── Payment.java
├── enums/                # Enumerations
│   ├── SeatType.java
│   ├── SeatStatus.java
│   ├── BookingStatus.java
│   ├── PaymentStatus.java
│   └── PaymentMethod.java
├── repository/           # Data access layer
│   ├── MovieRepository.java
│   ├── BookingRepository.java
│   ├── ShowSeatRepository.java
│   └── impl/             # JPA implementations
│       ├── JpaMovieRepository.java
│       └── JpaBookingRepository.java
├── service/              # Business logic layer
│   ├── MovieService.java
│   ├── ShowService.java
│   ├── BookingService.java
│   ├── PaymentService.java
│   └── NotificationService.java
├── controller/           # REST API layer
│   ├── MovieController.java
│   ├── ShowController.java
│   ├── BookingController.java
│   └── PaymentController.java
├── dto/                  # Data Transfer Objects
│   ├── BookingRequest.java
│   ├── BookingResponse.java
│   └── PaymentRequest.java
├── exception/            # Custom exceptions
│   ├── BookingNotFoundException.java
│   ├── SeatNotAvailableException.java
│   └── PaymentFailedException.java
└── config/               # Configuration
    └── DatabaseConfig.java
```

---

## 1. Domain Entities

### Movie.java
```java
package com.moviebooking.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * Represents a movie in the system
 * Contains movie metadata (title, duration, language, etc.)
 */
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @Column(name = "movie_id", length = 50)
    private String movieId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "language", nullable = false, length = 50)
    private String language;

    @Column(name = "genre", length = 50)
    private String genre;

    @Column(name = "release_date")
    @Temporal(TemporalType.DATE)
    private Date releaseDate;

    @Column(name = "rating", length = 10)
    private String rating;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Constructors
    public Movie() {}

    public Movie(String movieId, String title, int durationMinutes, String language) {
        this.movieId = movieId;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.language = language;
    }

    // Getters and Setters
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    // ... other getters/setters
}
```

---

### ShowSeat.java (CRITICAL!)
```java
package com.moviebooking.domain;

import com.moviebooking.enums.SeatStatus;
import javax.persistence.*;
import java.util.Date;

/**
 * CRITICAL ENTITY: Tracks seat availability for each show
 * Same physical seat can have different status for different shows
 *
 * Example:
 * - Seat C7 for Show 3PM: BOOKED
 * - Seat C7 for Show 7PM: AVAILABLE
 */
@Entity
@Table(name = "show_seats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "seat_id"}))
public class ShowSeat {

    @Id
    @Column(name = "show_seat_id", length = 50)
    private String showSeatId;

    @Column(name = "show_id", nullable = false, length = 50)
    private String showId;

    @Column(name = "seat_id", nullable = false, length = 50)
    private String seatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private SeatStatus status;

    @Column(name = "blocked_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date blockedAt;

    /**
     * Version for optimistic locking
     * Incremented on each update
     * If two transactions try to update same row, one will fail
     */
    @Version
    @Column(name = "version")
    private int version;

    // Constructors
    public ShowSeat() {
        this.status = SeatStatus.AVAILABLE;
        this.version = 0;
    }

    /**
     * Check if seat is available for booking
     * @return true if status is AVAILABLE
     */
    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }

    /**
     * Block this seat (temporarily hold during booking)
     * Sets status to BLOCKED and records timestamp
     */
    public void block() {
        if (!isAvailable()) {
            throw new IllegalStateException("Cannot block seat that is not available");
        }
        this.status = SeatStatus.BLOCKED;
        this.blockedAt = new Date();
    }

    /**
     * Release this seat (make available again)
     * Used when: payment fails, booking expires, booking cancelled
     */
    public void release() {
        this.status = SeatStatus.AVAILABLE;
        this.blockedAt = null;
    }

    /**
     * Book this seat (permanent reservation)
     * Sets status to BOOKED
     */
    public void book() {
        if (this.status != SeatStatus.BLOCKED) {
            throw new IllegalStateException("Can only book seats that are blocked");
        }
        this.status = SeatStatus.BOOKED;
        this.blockedAt = null;
    }

    // Getters and Setters
    public String getShowSeatId() { return showSeatId; }
    public void setShowSeatId(String showSeatId) { this.showSeatId = showSeatId; }

    public String getShowId() { return showId; }
    public void setShowId(String showId) { this.showId = showId; }

    public String getSeatId() { return seatId; }
    public void setSeatId(String seatId) { this.seatId = seatId; }

    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }

    public Date getBlockedAt() { return blockedAt; }
    public void setBlockedAt(Date blockedAt) { this.blockedAt = blockedAt; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
```

---

### Booking.java
```java
package com.moviebooking.domain;

import com.moviebooking.enums.BookingStatus;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a ticket booking/reservation
 * Links: Customer → Booking → Show + Seats
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @Column(name = "booking_id", length = 50)
    private String bookingId;

    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    @Column(name = "show_id", nullable = false, length = 50)
    private String showId;

    /**
     * List of ShowSeat IDs (not Seat IDs!)
     * Stored as comma-separated string in database
     * Example: "SS001,SS002,SS003"
     */
    @Column(name = "seat_ids", columnDefinition = "TEXT")
    private String seatIdsStr;  // Stored format

    @Transient  // Not persisted, computed from seatIdsStr
    private List<String> seatIds = new ArrayList<>();

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private BookingStatus status;

    @Column(name = "booking_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date bookingTime;

    /**
     * Booking expires at this time if payment not completed
     * Typically: booking_time + 10 minutes
     */
    @Column(name = "expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    // Constructors
    public Booking() {
        this.status = BookingStatus.PENDING_PAYMENT;
        this.bookingTime = new Date();
    }

    /**
     * Check if booking has expired
     * @return true if current time > expiresAt
     */
    public boolean isExpired() {
        return new Date().after(this.expiresAt);
    }

    /**
     * Confirm booking (after successful payment)
     */
    public void confirm() {
        if (this.status != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Can only confirm pending bookings");
        }
        if (isExpired()) {
            throw new IllegalStateException("Cannot confirm expired booking");
        }
        this.status = BookingStatus.CONFIRMED;
    }

    /**
     * Cancel booking
     */
    public void cancel() {
        if (this.status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Can only cancel confirmed bookings");
        }
        this.status = BookingStatus.CANCELLED;
    }

    // Persistence callbacks to convert between List and String
    @PrePersist
    @PreUpdate
    private void preSave() {
        // Convert List<String> to comma-separated string
        if (seatIds != null && !seatIds.isEmpty()) {
            this.seatIdsStr = String.join(",", seatIds);
        }
    }

    @PostLoad
    private void postLoad() {
        // Convert comma-separated string to List<String>
        if (seatIdsStr != null && !seatIdsStr.isEmpty()) {
            this.seatIds = List.of(seatIdsStr.split(","));
        }
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getShowId() { return showId; }
    public void setShowId(String showId) { this.showId = showId; }

    public List<String> getSeatIds() { return seatIds; }
    public void setSeatIds(List<String> seatIds) { this.seatIds = seatIds; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public Date getBookingTime() { return bookingTime; }
    public void setBookingTime(Date bookingTime) { this.bookingTime = bookingTime; }

    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
}
```

---

## 2. Enumerations

### SeatStatus.java
```java
package com.moviebooking.enums;

/**
 * Status of a seat for a specific show
 */
public enum SeatStatus {
    /**
     * Seat is available for booking
     */
    AVAILABLE,

    /**
     * Seat is temporarily blocked during booking process
     * Will be released if payment not completed within timeout
     */
    BLOCKED,

    /**
     * Seat is permanently booked (payment successful)
     */
    BOOKED,

    /**
     * Seat is not available (maintenance, damaged, etc.)
     */
    NOT_AVAILABLE
}
```

### BookingStatus.java
```java
package com.moviebooking.enums;

public enum BookingStatus {
    /**
     * Booking created, seats blocked, awaiting payment
     */
    PENDING_PAYMENT,

    /**
     * Payment successful, booking confirmed
     */
    CONFIRMED,

    /**
     * Booking cancelled by customer
     */
    CANCELLED,

    /**
     * Payment not completed within timeout, booking expired
     */
    EXPIRED
}
```

---

## 3. Repository Layer

### ShowSeatRepository.java (Interface)
```java
package com.moviebooking.repository;

import com.moviebooking.domain.ShowSeat;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ShowSeat data access
 * Defines contract for seat availability operations
 */
public interface ShowSeatRepository {

    /**
     * Find show seat by ID
     */
    Optional<ShowSeat> findById(String showSeatId);

    /**
     * CRITICAL: Find seat with pessimistic lock
     * Uses SELECT ... FOR UPDATE to prevent race conditions
     *
     * @param showSeatId the seat to lock
     * @return ShowSeat with database row lock
     */
    ShowSeat findByIdWithLock(String showSeatId);

    /**
     * Find all seats for a show
     */
    List<ShowSeat> findByShowId(String showId);

    /**
     * Save new show seat
     */
    String save(ShowSeat showSeat);

    /**
     * Update existing show seat
     */
    void update(ShowSeat showSeat);

    /**
     * Find all blocked seats that have expired
     * Used by background job to release seats
     *
     * @param timeoutMinutes how long seats are blocked (e.g., 10 minutes)
     * @return list of expired show seats
     */
    List<ShowSeat> findExpiredBlocked(int timeoutMinutes);
}
```

### JpaShowSeatRepository.java (Implementation)
```java
package com.moviebooking.repository.impl;

import com.moviebooking.domain.ShowSeat;
import com.moviebooking.enums.SeatStatus;
import com.moviebooking.repository.ShowSeatRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of ShowSeatRepository
 * Uses Hibernate for database operations
 */
@Repository
public class JpaShowSeatRepository implements ShowSeatRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<ShowSeat> findById(String showSeatId) {
        ShowSeat showSeat = entityManager.find(ShowSeat.class, showSeatId);
        return Optional.ofNullable(showSeat);
    }

    @Override
    public ShowSeat findByIdWithLock(String showSeatId) {
        /**
         * CRITICAL: Pessimistic locking
         * Generates SQL: SELECT * FROM show_seats WHERE show_seat_id = ? FOR UPDATE
         *
         * Row is locked until transaction commits
         * Other transactions wait (block) until lock is released
         * Prevents double-booking race condition
         */
        return entityManager.createQuery(
            "SELECT ss FROM ShowSeat ss WHERE ss.showSeatId = :id",
            ShowSeat.class
        )
        .setParameter("id", showSeatId)
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)  // 🔒 Lock acquired here!
        .getSingleResult();
    }

    @Override
    public List<ShowSeat> findByShowId(String showId) {
        return entityManager.createQuery(
            "SELECT ss FROM ShowSeat ss WHERE ss.showId = :showId ORDER BY ss.seatId",
            ShowSeat.class
        )
        .setParameter("showId", showId)
        .getResultList();
    }

    @Override
    @Transactional
    public String save(ShowSeat showSeat) {
        // Generate ID if not set
        if (showSeat.getShowSeatId() == null) {
            showSeat.setShowSeatId("SS" + UUID.randomUUID().toString().substring(0, 8));
        }

        entityManager.persist(showSeat);
        return showSeat.getShowSeatId();
    }

    @Override
    @Transactional
    public void update(ShowSeat showSeat) {
        entityManager.merge(showSeat);
    }

    @Override
    public List<ShowSeat> findExpiredBlocked(int timeoutMinutes) {
        // Calculate timeout threshold
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -timeoutMinutes);  // 10 minutes ago
        Date threshold = cal.getTime();

        /**
         * Find seats that are:
         * 1. Status = BLOCKED
         * 2. blocked_at < (current_time - timeout)
         *
         * Example: If timeout is 10 min and current time is 15:00
         * Find seats blocked before 14:50
         */
        return entityManager.createQuery(
            "SELECT ss FROM ShowSeat ss " +
            "WHERE ss.status = :status " +
            "AND ss.blockedAt < :threshold",
            ShowSeat.class
        )
        .setParameter("status", SeatStatus.BLOCKED)
        .setParameter("threshold", threshold)
        .getResultList();
    }
}
```

---

## 4. Service Layer

### BookingService.java
```java
package com.moviebooking.service;

import com.moviebooking.domain.*;
import com.moviebooking.dto.BookingRequest;
import com.moviebooking.enums.*;
import com.moviebooking.exception.*;
import com.moviebooking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Core booking service - handles entire booking lifecycle
 * Uses @Transactional to ensure atomicity
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatRepository seatRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @Autowired
    public BookingService(
        BookingRepository bookingRepository,
        ShowSeatRepository showSeatRepository,
        SeatRepository seatRepository,
        PaymentService paymentService,
        NotificationService notificationService
    ) {
        this.bookingRepository = bookingRepository;
        this.showSeatRepository = showSeatRepository;
        this.seatRepository = seatRepository;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }

    /**
     * Create new booking (Step 1 of booking flow)
     *
     * Process:
     * 1. Validate request
     * 2. Check seat availability with locking
     * 3. Block seats
     * 4. Calculate total amount
     * 5. Create booking with PENDING_PAYMENT status
     *
     * @param request booking request with showId and seatIds
     * @return created Booking
     * @throws SeatNotAvailableException if any seat is not available
     */
    @Transactional  // ✅ All or nothing!
    public Booking createBooking(BookingRequest request) {
        // 1. Validate
        validateBookingRequest(request);

        // 2. Block seats atomically (with pessimistic locking)
        List<ShowSeat> blockedSeats = blockSeats(request.getShowId(), request.getSeatIds());

        // 3. Calculate total
        double totalAmount = calculateTotal(blockedSeats);

        // 4. Create booking
        Booking booking = new Booking();
        booking.setBookingId(generateBookingId());
        booking.setCustomerId(request.getCustomerId());
        booking.setShowId(request.getShowId());
        booking.setSeatIds(request.getSeatIds());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setBookingTime(new Date());
        booking.setExpiresAt(calculateExpiryTime(10));  // 10 min from now

        // 5. Save booking
        bookingRepository.save(booking);

        return booking;
    }

    /**
     * Confirm booking after successful payment (Step 2 of booking flow)
     *
     * Process:
     * 1. Verify payment is successful
     * 2. Update booking status to CONFIRMED
     * 3. Update all seats from BLOCKED to BOOKED
     * 4. Send confirmation email
     *
     * @param bookingId the booking to confirm
     * @param payment the successful payment
     */
    @Transactional
    public void confirmBooking(String bookingId, Payment payment) {
        // 1. Get booking
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // 2. Verify payment success
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new PaymentNotSuccessfulException("Cannot confirm booking with failed payment");
        }

        // 3. Update booking
        booking.confirm();  // Sets status to CONFIRMED
        bookingRepository.update(booking);

        // 4. Book all seats (BLOCKED → BOOKED)
        for (String seatId : booking.getSeatIds()) {
            ShowSeat showSeat = showSeatRepository.findById(seatId)
                .orElseThrow();
            showSeat.book();  // Sets status to BOOKED
            showSeatRepository.update(showSeat);
        }

        // 5. Send notifications
        notificationService.sendBookingConfirmation(booking);
    }

    /**
     * Cancel booking and process refund
     *
     * @param bookingId the booking to cancel
     * @return true if cancellation successful
     */
    @Transactional
    public boolean cancelBooking(String bookingId) {
        // Get booking
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // Verify can be cancelled
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException("Only confirmed bookings can be cancelled");
        }

        // Check cancellation policy (2 hours before show)
        // ... (check show timing)

        // Process refund
        Payment payment = paymentService.findByBookingId(bookingId);
        boolean refundSuccess = paymentService.refund(payment.getPaymentId());

        if (!refundSuccess) {
            throw new RefundFailedException("Refund processing failed");
        }

        // Update booking
        booking.cancel();
        bookingRepository.update(booking);

        // Release seats (BOOKED → AVAILABLE)
        releaseSeats(booking.getSeatIds());

        // Send notification
        notificationService.sendCancellationEmail(booking);

        return true;
    }

    /**
     * Background job: Release expired bookings
     * Runs every 1 minute
     *
     * Finds bookings that:
     * - Status = PENDING_PAYMENT
     * - expires_at < current_time
     *
     * For each:
     * - Set status to EXPIRED
     * - Release seats (BLOCKED → AVAILABLE)
     */
    @Scheduled(cron = "0 * * * * *")  // Every minute
    public void releaseExpiredBookings() {
        Date now = new Date();

        // Find expired bookings
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(now);

        for (Booking booking : expiredBookings) {
            // Update booking status
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.update(booking);

            // Release seats
            releaseSeats(booking.getSeatIds());

            // Optional: Notify customer
            notificationService.sendPaymentTimeout(booking);
        }
    }

    /**
     * Block seats with pessimistic locking
     *
     * CRITICAL: Uses SELECT ... FOR UPDATE to prevent race conditions
     *
     * @param showId the show
     * @param seatIds list of seat IDs to block
     * @return list of blocked ShowSeat objects
     * @throws SeatNotAvailableException if any seat is not available
     */
    private List<ShowSeat> blockSeats(String showId, List<String> seatIds) {
        List<ShowSeat> blockedSeats = new ArrayList<>();

        for (String seatId : seatIds) {
            // Acquire lock on this row
            ShowSeat showSeat = showSeatRepository.findByIdWithLock(seatId);

            // Check if available
            if (!showSeat.isAvailable()) {
                // Release already blocked seats before throwing
                for (ShowSeat s : blockedSeats) {
                    s.release();
                    showSeatRepository.update(s);
                }
                throw new SeatNotAvailableException("Seat " + seatId + " is not available");
            }

            // Block seat
            showSeat.block();  // Sets status=BLOCKED, blocked_at=now
            showSeatRepository.update(showSeat);

            blockedSeats.add(showSeat);
        }

        return blockedSeats;
    }

    /**
     * Release seats (make available again)
     */
    private void releaseSeats(List<String> seatIds) {
        for (String seatId : seatIds) {
            ShowSeat showSeat = showSeatRepository.findById(seatId).orElseThrow();
            showSeat.release();  // Sets status=AVAILABLE, blocked_at=null
            showSeatRepository.update(showSeat);
        }
    }

    /**
     * Calculate total booking amount
     */
    private double calculateTotal(List<ShowSeat> showSeats) {
        double subtotal = 0.0;

        for (ShowSeat showSeat : showSeats) {
            Seat seat = seatRepository.findById(showSeat.getSeatId()).orElseThrow();
            subtotal += seat.getBasePrice();
        }

        // Add 18% tax
        double tax = subtotal * 0.18;
        return subtotal + tax;
    }

    /**
     * Generate unique booking ID
     * Format: BK + YYYYMMDD + sequence
     * Example: BK20251118001
     */
    private String generateBookingId() {
        // Implementation: Use date + sequence number or UUID
        return "BK" + System.currentTimeMillis();
    }

    /**
     * Calculate expiry time
     */
    private Date calculateExpiryTime(int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }

    /**
     * Validate booking request
     */
    private void validateBookingRequest(BookingRequest request) {
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new ValidationException("No seats selected");
        }
        if (request.getSeatIds().size() > 10) {
            throw new ValidationException("Maximum 10 seats per booking");
        }
    }
}
```

---

## 📝 Summary

**Total Lines of Code (LOC):** ~2000 lines

**Key Components:**
1. **Entities:** Movie, Show, ShowSeat, Booking, Payment (with JPA annotations)
2. **Repositories:** Interface + JPA implementation (pessimistic locking)
3. **Services:** Business logic (BookingService, PaymentService)
4. **Controllers:** REST API endpoints (next layer)

**Critical Features:**
- ✅ **Pessimistic Locking:** `findByIdWithLock()` prevents double-booking
- ✅ **@Transactional:** Ensures atomicity (all or nothing)
- ✅ **@Scheduled:** Background job for auto-releasing expired seats
- ✅ **@Version:** Optimistic locking for concurrent updates
- ✅ **Validation:** Input validation at multiple layers

**Next Document:** [13_interview_qa.md](./13_interview_qa.md)
