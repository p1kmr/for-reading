# API Design - Distributed Locking Service

## API Design Principles

### RESTful API Best Practices
1. **Use HTTP methods correctly:** GET (read), POST (create), PUT (update), DELETE (remove)
2. **Resource-based URLs:** `/locks/{resourceId}` not `/acquireLock`
3. **Versioning:** `/api/v1/` for backward compatibility
4. **Stateless:** Each request contains all needed information
5. **Idempotent operations:** Retry-safe (same request twice = same result)

---

## API Endpoints

### 1. Acquire Lock

**Endpoint:**
```http
POST /api/v1/locks
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
X-Request-ID: <unique-request-id>  # For idempotency
```

**Request Body:**
```json
{
  "resourceId": "order:12345",
  "ttl": 30,
  "blocking": false,
  "timeout": 10,
  "metadata": {
    "requestor": "payment-service",
    "operation": "process-payment"
  }
}
```

**Response (Success):**
```http
HTTP/1.1 201 Created
Content-Type: application/json
X-Request-ID: <same-request-id>

{
  "success": true,
  "lockId": "550e8400-e29b-41d4-a716-446655440000",
  "lockToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "resourceId": "order:12345",
  "expiresAt": "2025-01-17T12:35:45Z",
  "ttl": 30,
  "acquiredAt": "2025-01-17T12:35:15Z",
  "region": "us-east-1"
}
```

**Response (Failure - Lock Already Held):**
```http
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "success": false,
  "error": {
    "code": "LOCK_ALREADY_HELD",
    "message": "Resource is currently locked by another client",
    "resourceId": "order:12345",
    "currentOwner": "inventory-service",
    "expiresAt": "2025-01-17T12:36:00Z",
    "retryAfter": 45
  }
}
```

**Response (Failure - Rate Limit):**
```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/json
Retry-After: 60

{
  "success": false,
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Client has exceeded rate limit of 100 requests per minute",
    "limit": 100,
    "remaining": 0,
    "resetAt": "2025-01-17T12:36:00Z"
  }
}
```

**Beginner Example:**
```bash
# Using curl to acquire a lock
curl -X POST https://lock-service.example.com/api/v1/locks \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Request-ID: req-123-abc" \
  -d '{
    "resourceId": "order:12345",
    "ttl": 30,
    "blocking": false
  }'
```

---

### 2. Release Lock

**Endpoint:**
```http
DELETE /api/v1/locks/{lockId}
Authorization: Bearer <JWT_TOKEN>
X-Lock-Token: <lock-token>
```

**Response (Success):**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "lockId": "550e8400-e29b-41d4-a716-446655440000",
  "resourceId": "order:12345",
  "releasedAt": "2025-01-17T12:35:30Z",
  "holdDuration": 15.2
}
```

**Response (Failure - Invalid Token):**
```http
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "success": false,
  "error": {
    "code": "INVALID_LOCK_TOKEN",
    "message": "Lock token is invalid or expired",
    "lockId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

**Response (Failure - Lock Not Found):**
```http
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "error": {
    "code": "LOCK_NOT_FOUND",
    "message": "Lock does not exist or has already expired",
    "lockId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

---

### 3. Renew / Extend Lock

**Endpoint:**
```http
PUT /api/v1/locks/{lockId}/renew
Authorization: Bearer <JWT_TOKEN>
X-Lock-Token: <lock-token>
```

**Request Body:**
```json
{
  "additionalTtl": 30
}
```

**Response (Success):**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "lockId": "550e8400-e29b-41d4-a716-446655440000",
  "resourceId": "order:12345",
  "newExpiresAt": "2025-01-17T12:36:15Z",
  "totalTtl": 60,
  "renewedAt": "2025-01-17T12:35:45Z"
}
```

**Response (Failure - Max TTL Exceeded):**
```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "success": false,
  "error": {
    "code": "MAX_TTL_EXCEEDED",
    "message": "Total TTL cannot exceed 300 seconds",
    "currentTtl": 280,
    "requestedAdditional": 30,
    "maxAllowed": 300
  }
}
```

---

### 4. Check Lock Status

**Endpoint:**
```http
GET /api/v1/locks?resourceId=order:12345
Authorization: Bearer <JWT_TOKEN>
```

**Response (Locked):**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "resourceId": "order:12345",
  "isLocked": true,
  "lockId": "550e8400-e29b-41d4-a716-446655440000",
  "ownerId": "client-A-uuid",
  "ownerName": "payment-service",
  "acquiredAt": "2025-01-17T12:35:15Z",
  "expiresAt": "2025-01-17T12:35:45Z",
  "remainingTtl": 22,
  "region": "us-east-1"
}
```

**Response (Available):**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "resourceId": "order:12345",
  "isLocked": false,
  "lastReleasedAt": "2025-01-17T12:35:30Z",
  "lastOwner": "payment-service"
}
```

---

### 5. Health Check

**Endpoint:**
```http
GET /health
```

**Response (Healthy):**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "status": "healthy",
  "version": "1.2.3",
  "region": "us-east-1",
  "dependencies": {
    "etcd": {
      "status": "healthy",
      "latency": 8.5,
      "leader": "etcd-1A"
    },
    "redis": {
      "status": "healthy",
      "latency": 1.2,
      "hitRate": 0.82
    },
    "kafka": {
      "status": "healthy",
      "lag": 120
    }
  },
  "metrics": {
    "activeConnections": 1250,
    "requestRate": 8432,
    "errorRate": 0.02
  }
}
```

**Response (Degraded):**
```http
HTTP/1.1 503 Service Unavailable
Content-Type: application/json

{
  "status": "degraded",
  "reason": "etcd cluster has only 2/3 nodes healthy",
  "dependencies": {
    "etcd": {
      "status": "degraded",
      "healthyNodes": 2,
      "totalNodes": 3
    }
  }
}
```

---

## Authentication & Authorization

### JWT Token Structure

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "client-A-uuid",
    "name": "payment-service",
    "iat": 1738012345,
    "exp": 1738015945,
    "permissions": ["lock:read", "lock:write", "lock:delete"],
    "rateLimit": {
      "requestsPerMinute": 100,
      "burstSize": 200
    },
    "allowedResources": ["order:*", "user:*"],
    "region": "us-east-1"
  },
  "signature": "..."
}
```

### Permission Model

| Permission | Allows |
|------------|--------|
| `lock:read` | Check lock status |
| `lock:write` | Acquire and renew locks |
| `lock:delete` | Release locks |
| `lock:admin` | Force-release any lock (admin only) |

---

## Rate Limiting

### Strategy: Token Bucket Algorithm

```
Each client has a bucket with tokens
- Capacity: 100 tokens (burst size)
- Refill rate: 100 tokens per minute (1.67 tokens/sec)

Request flow:
1. Client makes request
2. Check bucket: Does it have ≥1 token?
3. Yes → Consume 1 token, process request
4. No → Reject with 429 Too Many Requests

Example:
Time 0s: Client has 100 tokens (full bucket)
Time 1s: Client sends 10 requests → 90 tokens remain
Time 2s: Client sends 95 requests → 0 tokens remain (5 rejected)
Time 3s: Bucket refills +1.67 tokens → 1 token available
Time 4s: Client can send 1 request
```

### Rate Limit Headers

```http
HTTP/1.1 200 OK
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 73
X-RateLimit-Reset: 1738012400
```

---

## Idempotency

### Why Idempotency Matters

**Scenario without idempotency:**
```
Time 0s: Client sends "Acquire lock for order:12345"
Time 10s: No response (network timeout)
Time 11s: Client retries "Acquire lock for order:12345"
Result: Client now has TWO locks? Or error? Confusion!
```

**With idempotency (using X-Request-ID):**
```
Time 0s: Client sends request with X-Request-ID: "req-123"
        → Server acquires lock, stores request ID
Time 10s: No response (network timeout)
Time 11s: Client retries with SAME X-Request-ID: "req-123"
        → Server recognizes duplicate request
        → Returns SAME response (lock already acquired)
Result: Exactly-once semantics! ✅
```

### Implementation

```go
func AcquireLock(requestID string, resourceID string, ttl int) Response {
    // Check if we've seen this request ID before (last 5 minutes)
    cached := redis.Get("idempotency:" + requestID)
    if cached != nil {
        // Return cached response (idempotent)
        return cached
    }

    // New request, acquire lock
    result := etcd.AcquireLock(resourceID, ttl)

    // Cache response for 5 minutes (for idempotency)
    redis.Set("idempotency:" + requestID, result, ttl=300)

    return result
}
```

---

## Error Codes Reference

| Code | HTTP Status | Meaning | Action |
|------|-------------|---------|--------|
| `LOCK_ALREADY_HELD` | 409 Conflict | Resource is locked | Wait and retry |
| `INVALID_LOCK_TOKEN` | 403 Forbidden | Lock token invalid | Don't retry |
| `LOCK_NOT_FOUND` | 404 Not Found | Lock doesn't exist | Don't retry |
| `LOCK_EXPIRED` | 410 Gone | Lock has expired | Acquire new lock |
| `RATE_LIMIT_EXCEEDED` | 429 Too Many | Too many requests | Wait and retry |
| `INVALID_REQUEST` | 400 Bad Request | Malformed JSON | Fix request |
| `UNAUTHORIZED` | 401 Unauthorized | Missing/invalid auth | Check credentials |
| `FORBIDDEN` | 403 Forbidden | No permission | Check permissions |
| `INTERNAL_ERROR` | 500 Internal Error | Server error | Retry with backoff |
| `SERVICE_UNAVAILABLE` | 503 Unavailable | System overloaded | Retry with backoff |
| `ETCD_TIMEOUT` | 504 Gateway Timeout | etcd unreachable | Retry with backoff |

---

## Client SDK Example (Python)

```python
from lock_service import LockClient, LockAlreadyHeldError

# Initialize client
client = LockClient(
    api_url="https://lock-service.example.com",
    api_key="your-api-key",
    region="us-east-1"
)

# Example 1: Simple acquire and release
try:
    lock = client.acquire_lock(
        resource_id="order:12345",
        ttl=30
    )
    print(f"Lock acquired: {lock.lock_id}")

    # Do critical work here
    process_order("12345")

except LockAlreadyHeldError as e:
    print(f"Resource is locked by {e.current_owner}, expires at {e.expires_at}")
    print(f"Retry after {e.retry_after} seconds")

finally:
    # Always release lock
    if lock:
        client.release_lock(lock.lock_token)
        print("Lock released")

# Example 2: Using context manager (auto-release)
with client.lock("order:12345", ttl=30) as lock:
    process_order("12345")
    # Lock automatically released when exiting context

# Example 3: Blocking acquisition (wait for lock)
lock = client.acquire_lock(
    resource_id="order:12345",
    ttl=30,
    blocking=True,  # Wait for lock to become available
    timeout=10  # Wait up to 10 seconds
)

# Example 4: Renew lock (extend TTL)
lock = client.acquire_lock("order:12345", ttl=30)
time.sleep(20)  # Work for 20 seconds
client.renew_lock(lock.lock_token, additional_ttl=30)  # Extend by 30 more seconds
```

---

## Summary

### API Design Decisions

✅ **RESTful:** Standard HTTP methods, resource-based URLs
✅ **Versioned:** `/api/v1/` for backward compatibility
✅ **Idempotent:** X-Request-ID prevents duplicate operations
✅ **Authenticated:** JWT tokens with fine-grained permissions
✅ **Rate Limited:** Token bucket algorithm (100 req/min per client)
✅ **Well-Documented:** Clear error codes and responses
✅ **Client-Friendly:** SDK available in multiple languages

### Key Features

- **Blocking Mode:** Client can wait for lock to become available
- **Lock Renewal:** Extend TTL without releasing
- **Metadata:** Attach custom data to locks
- **Audit Trail:** X-Request-ID for request tracing
- **Health Checks:** Monitor system health before sending requests

---

**Next Document:** [Data Flow Diagrams](08_data_flow_diagrams.md)
