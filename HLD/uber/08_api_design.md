# API Design - RESTful Endpoints for Uber

## API Design Principles

**REST Best Practices:**
- Use nouns for resources (`/users`, `/rides`, not `/getUser`, `/createRide`)
- HTTP methods: GET (read), POST (create), PUT (update), DELETE (delete)
- Versioning: `/api/v1/` for backward compatibility
- Pagination: Use `limit` and `offset` for large datasets
- Filtering: Query parameters for filtering (` GET /rides?status=completed`)
- Response format: JSON
- HTTP status codes: 200 (OK), 201 (Created), 400 (Bad Request), 401 (Unauthorized), 404 (Not Found), 500 (Server Error)

---

## Authentication

All API requests (except login/signup) require authentication.

**Authentication Flow:**
```
1. User logs in → Receive JWT token
2. Include token in header: Authorization: Bearer <token>
3. API validates token → Extract user_id → Process request
```

**Example:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "phone": "+1-415-555-0123",
  "otp": "123456"
}

Response:
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
  "expires_in": 3600,
  "user": {
    "user_id": 12345,
    "name": "John Doe",
    "role": "rider"
  }
}
```

---

## User APIs

### 1. Register User
```http
POST /api/v1/users
Content-Type: application/json

Request:
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1-415-555-0123",
  "password": "hashed_password",
  "role": "rider"
}

Response: 201 Created
{
  "user_id": 12345,
  "name": "John Doe",
  "email": "john@example.com",
  "created_at": "2025-01-15T10:00:00Z"
}
```

### 2. Get User Profile
```http
GET /api/v1/users/12345
Authorization: Bearer <token>

Response: 200 OK
{
  "user_id": 12345,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1-415-555-0123",
  "rating": 4.8,
  "total_rides": 127,
  "member_since": "2023-05-10"
}
```

### 3. Update User Profile
```http
PUT /api/v1/users/12345
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "name": "John Smith",
  "email": "john.smith@example.com"
}

Response: 200 OK
{
  "user_id": 12345,
  "name": "John Smith",
  "email": "john.smith@example.com",
  "updated_at": "2025-01-15T11:00:00Z"
}
```

---

## Driver APIs

### 1. Update Driver Location
```http
POST /api/v1/drivers/456/location
Authorization: Bearer <driver_token>
Content-Type: application/json

Request:
{
  "latitude": 37.7749,
  "longitude": -122.4194,
  "bearing": 45,
  "speed": 30,
  "timestamp": "2025-01-15T10:30:00Z"
}

Response: 200 OK
{
  "success": true,
  "location_id": 789012
}
```

### 2. Update Driver Availability
```http
PATCH /api/v1/drivers/456/status
Authorization: Bearer <driver_token>
Content-Type: application/json

Request:
{
  "status": "available"
}

Response: 200 OK
{
  "driver_id": 456,
  "status": "available",
  "updated_at": "2025-01-15T10:35:00Z"
}
```

### 3. Get Driver Earnings
```http
GET /api/v1/drivers/456/earnings?start_date=2025-01-01&end_date=2025-01-15
Authorization: Bearer <driver_token>

Response: 200 OK
{
  "driver_id": 456,
  "period": {
    "start": "2025-01-01",
    "end": "2025-01-15"
  },
  "total_earnings": 2450.75,
  "total_rides": 87,
  "avg_ride_fare": 28.17,
  "breakdown": [
    {"date": "2025-01-15", "earnings": 185.50, "rides": 8},
    {"date": "2025-01-14", "earnings": 220.00, "rides": 10}
  ]
}
```

---

## Ride APIs

### 1. Request Ride (Estimate Fare & ETA)
```http
POST /api/v1/rides/estimate
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "pickup": {
    "latitude": 37.7749,
    "longitude": -122.4194,
    "address": "123 Main St, San Francisco"
  },
  "destination": {
    "latitude": 37.8044,
    "longitude": -122.2712,
    "address": "456 Oak Ave, Oakland"
  },
  "ride_type": "uberx"
}

Response: 200 OK
{
  "estimate_id": "est_abc123",
  "fare_estimate": {
    "min": 42.00,
    "max": 52.00,
    "currency": "USD",
    "breakdown": {
      "base_fare": 2.50,
      "distance_cost": 30.00,
      "time_cost": 8.50,
      "surge_multiplier": 1.5,
      "service_fee": 3.00
    }
  },
  "eta_seconds": 300,
  "distance_km": 15.2,
  "duration_minutes": 18
}
```

### 2. Book Ride
```http
POST /api/v1/rides
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "estimate_id": "est_abc123",
  "pickup": {
    "latitude": 37.7749,
    "longitude": -122.4194,
    "address": "123 Main St, San Francisco"
  },
  "destination": {
    "latitude": 37.8044,
    "longitude": -122.2712,
    "address": "456 Oak Ave, Oakland"
  },
  "ride_type": "uberx",
  "payment_method_id": "pm_card_visa_4242"
}

Response: 201 Created
{
  "ride_id": 789,
  "status": "searching",
  "fare_estimate": 48.00,
  "created_at": "2025-01-15T10:40:00Z",
  "message": "Finding you a driver..."
}
```

### 3. Get Ride Status
```http
GET /api/v1/rides/789
Authorization: Bearer <token>

Response: 200 OK
{
  "ride_id": 789,
  "status": "driver_arriving",
  "rider": {
    "user_id": 12345,
    "name": "John Doe",
    "phone": "+1-415-555-0123",
    "rating": 4.8
  },
  "driver": {
    "driver_id": 456,
    "name": "Jane Smith",
    "phone": "+1-415-555-9999",
    "rating": 4.9,
    "vehicle": {
      "make": "Toyota",
      "model": "Camry",
      "year": 2022,
      "color": "Silver",
      "license_plate": "ABC1234"
    },
    "current_location": {
      "latitude": 37.7755,
      "longitude": -122.4185
    }
  },
  "pickup": {
    "latitude": 37.7749,
    "longitude": -122.4194,
    "address": "123 Main St, San Francisco"
  },
  "destination": {
    "latitude": 37.8044,
    "longitude": -122.2712,
    "address": "456 Oak Ave, Oakland"
  },
  "eta_to_pickup_seconds": 120,
  "fare_estimate": 48.00
}
```

### 4. Cancel Ride
```http
DELETE /api/v1/rides/789
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "reason": "Found alternative transportation"
}

Response: 200 OK
{
  "ride_id": 789,
  "status": "cancelled",
  "cancellation_fee": 5.00,
  "cancelled_at": "2025-01-15T10:45:00Z"
}
```

### 5. Complete Ride
```http
PATCH /api/v1/rides/789/complete
Authorization: Bearer <driver_token>
Content-Type: application/json

Request:
{
  "end_location": {
    "latitude": 37.8044,
    "longitude": -122.2712
  },
  "actual_distance_km": 15.8,
  "actual_duration_minutes": 20
}

Response: 200 OK
{
  "ride_id": 789,
  "status": "completed",
  "final_fare": 50.25,
  "completed_at": "2025-01-15T11:00:00Z"
}
```

### 6. Rate Ride
```http
POST /api/v1/rides/789/rating
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "rating": 5,
  "feedback": "Great driver, smooth ride!",
  "tips": 5.00
}

Response: 200 OK
{
  "ride_id": 789,
  "rating": 5,
  "tips": 5.00,
  "rated_at": "2025-01-15T11:05:00Z"
}
```

### 7. Get Ride History
```http
GET /api/v1/users/12345/rides?limit=20&offset=0&status=completed
Authorization: Bearer <token>

Response: 200 OK
{
  "total": 127,
  "limit": 20,
  "offset": 0,
  "rides": [
    {
      "ride_id": 789,
      "date": "2025-01-15T11:00:00Z",
      "driver_name": "Jane Smith",
      "pickup_address": "123 Main St, San Francisco",
      "destination_address": "456 Oak Ave, Oakland",
      "fare": 50.25,
      "distance_km": 15.8,
      "duration_minutes": 20,
      "rating": 5
    }
  ]
}
```

---

## Payment APIs

### 1. Add Payment Method
```http
POST /api/v1/users/12345/payment-methods
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "type": "card",
  "card_number": "4242424242424242",
  "exp_month": 12,
  "exp_year": 2027,
  "cvc": "123",
  "billing_zip": "94102"
}

Response: 201 Created
{
  "payment_method_id": "pm_card_visa_4242",
  "type": "card",
  "brand": "visa",
  "last4": "4242",
  "exp_month": 12,
  "exp_year": 2027,
  "is_default": true
}
```

### 2. Process Payment
```http
POST /api/v1/payments
Authorization: Bearer <system_token>
Content-Type: application/json

Request:
{
  "ride_id": 789,
  "user_id": 12345,
  "amount": 50.25,
  "currency": "USD",
  "payment_method_id": "pm_card_visa_4242"
}

Response: 201 Created
{
  "payment_id": "pay_xyz789",
  "ride_id": 789,
  "amount": 50.25,
  "status": "succeeded",
  "charged_at": "2025-01-15T11:01:00Z",
  "receipt_url": "https://cdn.uber.com/receipts/pay_xyz789.pdf"
}
```

---

## Surge Pricing API

```http
GET /api/v1/pricing/surge?latitude=37.7749&longitude=-122.4194
Authorization: Bearer <token>

Response: 200 OK
{
  "zone_id": "sf_downtown",
  "surge_multiplier": 1.8,
  "updated_at": "2025-01-15T10:40:00Z",
  "message": "High demand in your area"
}
```

---

## WebSocket API (Real-Time Updates)

For real-time location tracking and ride status updates.

### Connect to WebSocket
```javascript
const ws = new WebSocket('wss://api.uber.com/v1/realtime?token=<jwt_token>');

ws.onopen = () => {
  // Subscribe to ride updates
  ws.send(JSON.stringify({
    type: 'subscribe',
    channel: 'ride:789'
  }));
};

ws.onmessage = (event) => {
  const update = JSON.parse(event.data);
  console.log(update);
};

// Example updates received:
{
  "type": "driver_location_update",
  "ride_id": 789,
  "driver_location": {
    "latitude": 37.7760,
    "longitude": -122.4180
  },
  "eta_seconds": 90,
  "timestamp": "2025-01-15T10:42:00Z"
}

{
  "type": "ride_status_update",
  "ride_id": 789,
  "status": "driver_arrived",
  "timestamp": "2025-01-15T10:43:00Z"
}
```

---

## Rate Limiting

**Limits:**
- Guest users: 10 requests/minute
- Authenticated users: 100 requests/minute
- Drivers (location updates): 1000 requests/minute
- Enterprise API: 10,000 requests/minute

**Response when rate limit exceeded:**
```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/json
Retry-After: 60

{
  "error": "rate_limit_exceeded",
  "message": "Too many requests. Please try again in 60 seconds.",
  "limit": 100,
  "remaining": 0,
  "reset_at": "2025-01-15T10:45:00Z"
}
```

---

## Error Responses

### Standard Error Format
```json
{
  "error": {
    "code": "RIDE_NOT_FOUND",
    "message": "Ride with ID 789 not found",
    "details": {
      "ride_id": 789
    },
    "request_id": "req_abc123xyz",
    "timestamp": "2025-01-15T10:40:00Z"
  }
}
```

### Common Error Codes

| HTTP Status | Error Code | Message |
|-------------|------------|---------|
| 400 | INVALID_REQUEST | Missing required fields |
| 401 | UNAUTHORIZED | Invalid or expired token |
| 403 | FORBIDDEN | Insufficient permissions |
| 404 | RIDE_NOT_FOUND | Ride not found |
| 404 | USER_NOT_FOUND | User not found |
| 409 | RIDE_ALREADY_ACTIVE | User already has an active ride |
| 422 | PAYMENT_FAILED | Payment processing failed |
| 429 | RATE_LIMIT_EXCEEDED | Too many requests |
| 500 | INTERNAL_SERVER_ERROR | Unexpected server error |
| 503 | SERVICE_UNAVAILABLE | Service temporarily unavailable |

---

## API Versioning Strategy

**Current version: v1**

```
/api/v1/rides  → Current stable API
/api/v2/rides  → Future version (if breaking changes needed)
```

**Deprecation Policy:**
- New version released → v1 supported for 12 months
- After 12 months → v1 returns deprecation warning
- After 18 months → v1 disabled

**Example deprecation header:**
```http
GET /api/v1/rides/789
...
Deprecation: true
Sunset: 2026-01-15
Link: </api/v2/rides/789>; rel="successor-version"
```

---

## Pagination Best Practices

```http
GET /api/v1/users/12345/rides?limit=20&offset=40

Response:
{
  "total": 127,
  "limit": 20,
  "offset": 40,
  "has_more": true,
  "rides": [...],
  "links": {
    "first": "/api/v1/users/12345/rides?limit=20&offset=0",
    "prev": "/api/v1/users/12345/rides?limit=20&offset=20",
    "next": "/api/v1/users/12345/rides?limit=20&offset=60",
    "last": "/api/v1/users/12345/rides?limit=20&offset=120"
  }
}
```

---

## API Security Checklist

- ✅ All endpoints use HTTPS (TLS 1.3)
- ✅ JWT tokens expire after 1 hour (refresh token for renewal)
- ✅ Rate limiting per user/IP
- ✅ Input validation (prevent SQL injection, XSS)
- ✅ CORS policy (only allow uber.com domains)
- ✅ Request signing for sensitive operations (payments)
- ✅ Audit logging for all API calls
- ✅ IP whitelisting for admin APIs

---

## Performance Optimization

**Response Compression:**
```http
GET /api/v1/rides/789
Accept-Encoding: gzip

Response:
Content-Encoding: gzip
Content-Length: 512  (vs 2048 uncompressed)
```

**Conditional Requests (Caching):**
```http
GET /api/v1/users/12345
If-None-Match: "abc123"

Response:
HTTP/1.1 304 Not Modified  (if data unchanged)
ETag: "abc123"
```

**Batch Requests:**
```http
POST /api/v1/batch
Content-Type: application/json

{
  "requests": [
    {"method": "GET", "url": "/api/v1/users/12345"},
    {"method": "GET", "url": "/api/v1/rides/789"}
  ]
}

Response:
{
  "responses": [
    {"status": 200, "body": {...}},
    {"status": 200, "body": {...}}
  ]
}
```
