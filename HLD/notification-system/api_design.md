# API Design

## RESTful API Specification

### Base URL
```
Production: https://api.notifications.company.com/v1
Staging: https://api-staging.notifications.company.com/v1
```

---

## Authentication

### API Key Authentication
```http
POST /api/v1/notifications
Headers:
  X-API-Key: sk_live_abc123def456ghi789
  Content-Type: application/json
```

### OAuth 2.0 (Enterprise)
```http
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id=xyz123
&client_secret=secret456

Response:
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}

Usage:
POST /api/v1/notifications
Headers:
  Authorization: Bearer eyJhbGc...
```

---

## Notification APIs

### 1. Send Notification

**Endpoint:** `POST /api/v1/notifications`

**Description:** Send a single notification immediately

**Request:**
```json
{
  "user_id": 12345,
  "channel": "email",
  "type": "transactional",
  "template_id": "order_confirmation",
  "variables": {
    "order_id": "ORD-789",
    "amount": "$99.99",
    "product_name": "Wireless Headphones"
  },
  "metadata": {
    "campaign_id": "black-friday-2025",
    "source": "checkout-service"
  }
}
```

**Response:** `202 Accepted`
```json
{
  "notification_id": "notif_1a2b3c4d5e",
  "status": "queued",
  "queued_at": "2025-01-15T10:30:00Z",
  "estimated_delivery": "2025-01-15T10:30:05Z"
}
```

**Error Responses:**
```json
400 Bad Request
{
  "error": "validation_error",
  "message": "Invalid channel. Must be one of: email, sms, push, in-app",
  "field": "channel"
}

429 Too Many Requests
{
  "error": "rate_limit_exceeded",
  "message": "Rate limit of 100 requests per hour exceeded",
  "retry_after": 3600
}

403 Forbidden
{
  "error": "user_opted_out",
  "message": "User has opted out of email notifications",
  "user_id": 12345,
  "channel": "email"
}
```

---

### 2. Send Bulk Notifications

**Endpoint:** `POST /api/v1/notifications/bulk`

**Description:** Send notifications to multiple users

**Request:**
```json
{
  "user_ids": [123, 456, 789, 1011],
  "channel": "push",
  "type": "promotional",
  "template_id": "flash_sale",
  "variables": {
    "discount": "50%",
    "end_time": "11:59 PM"
  },
  "batch_size": 1000,
  "rate_limit": 1000
}
```

**Response:** `202 Accepted`
```json
{
  "job_id": "bulk_abc123",
  "total_users": 4,
  "status": "processing",
  "created_at": "2025-01-15T10:30:00Z",
  "estimated_completion": "2025-01-15T10:35:00Z",
  "status_url": "/api/v1/jobs/bulk_abc123"
}
```

**Check Job Status:**
```http
GET /api/v1/jobs/bulk_abc123

Response: 200 OK
{
  "job_id": "bulk_abc123",
  "status": "completed",
  "total_users": 4,
  "sent": 3,
  "failed": 1,
  "skipped": 0,
  "started_at": "2025-01-15T10:30:00Z",
  "completed_at": "2025-01-15T10:32:30Z",
  "failures": [
    {
      "user_id": 789,
      "reason": "User opted out",
      "error_code": "user_opted_out"
    }
  ]
}
```

---

### 3. Schedule Notification

**Endpoint:** `POST /api/v1/notifications/schedule`

**Description:** Schedule a notification for future delivery

**Request:**
```json
{
  "user_id": 12345,
  "channel": "email",
  "template_id": "birthday_discount",
  "variables": {
    "discount_code": "BDAY20"
  },
  "scheduled_for": "2025-02-14T09:00:00-08:00",
  "timezone": "America/Los_Angeles"
}
```

**Response:** `201 Created`
```json
{
  "notification_id": "notif_scheduled_xyz",
  "status": "scheduled",
  "scheduled_for": "2025-02-14T09:00:00-08:00",
  "created_at": "2025-01-15T10:30:00Z",
  "cancel_url": "/api/v1/notifications/notif_scheduled_xyz"
}
```

---

### 4. Get Notification Status

**Endpoint:** `GET /api/v1/notifications/:id`

**Response:** `200 OK`
```json
{
  "notification_id": "notif_1a2b3c4d5e",
  "user_id": 12345,
  "channel": "email",
  "type": "transactional",
  "status": "delivered",
  "created_at": "2025-01-15T10:30:00Z",
  "queued_at": "2025-01-15T10:30:00Z",
  "sent_at": "2025-01-15T10:30:03Z",
  "delivered_at": "2025-01-15T10:30:05Z",
  "opened_at": "2025-01-15T10:35:20Z",
  "clicked_at": "2025-01-15T10:36:45Z",
  "retry_count": 0,
  "provider": "sendgrid",
  "provider_message_id": "sg_abc123",
  "metadata": {
    "campaign_id": "black-friday-2025"
  }
}
```

**Status Values:**
- `queued`: In Kafka queue
- `pending`: Being processed by worker
- `sent`: Sent to provider
- `delivered`: Provider confirmed delivery
- `failed`: Delivery failed after retries
- `scheduled`: Waiting for scheduled time

---

### 5. Cancel Scheduled Notification

**Endpoint:** `DELETE /api/v1/notifications/:id`

**Response:** `200 OK`
```json
{
  "notification_id": "notif_scheduled_xyz",
  "status": "cancelled",
  "cancelled_at": "2025-01-15T11:00:00Z"
}
```

**Error:**
```json
409 Conflict
{
  "error": "cannot_cancel",
  "message": "Notification already sent",
  "status": "delivered"
}
```

---

## User Preference APIs

### 1. Get User Preferences

**Endpoint:** `GET /api/v1/preferences/:user_id`

**Response:** `200 OK`
```json
{
  "user_id": 12345,
  "channels": {
    "email": {
      "enabled": true,
      "types": {
        "transactional": true,
        "promotional": false,
        "system": true
      }
    },
    "sms": {
      "enabled": true,
      "types": {
        "transactional": true,
        "promotional": false,
        "system": false
      }
    },
    "push": {
      "enabled": true,
      "types": {
        "transactional": true,
        "promotional": true,
        "system": true
      }
    }
  },
  "quiet_hours": {
    "enabled": true,
    "start": "22:00",
    "end": "08:00",
    "timezone": "America/Los_Angeles"
  },
  "updated_at": "2025-01-10T14:20:00Z"
}
```

---

### 2. Update User Preferences

**Endpoint:** `PUT /api/v1/preferences/:user_id`

**Request:**
```json
{
  "channels": {
    "email": {
      "enabled": false
    }
  }
}
```

**Response:** `200 OK`
```json
{
  "user_id": 12345,
  "channels": {
    "email": {
      "enabled": false,
      "types": {
        "transactional": true,
        "promotional": false,
        "system": true
      }
    }
  },
  "updated_at": "2025-01-15T11:00:00Z"
}
```

---

### 3. Unsubscribe from All

**Endpoint:** `POST /api/v1/preferences/unsubscribe`

**Request:**
```json
{
  "email": "user@example.com",
  "token": "unsubscribe_token_abc123"
}
```

**Response:** `200 OK`
```json
{
  "message": "Successfully unsubscribed from all promotional emails",
  "user_id": 12345,
  "unsubscribed_at": "2025-01-15T11:05:00Z"
}
```

---

## Template APIs

### 1. List Templates

**Endpoint:** `GET /api/v1/templates`

**Query Parameters:**
- `channel`: Filter by channel (email, sms, push)
- `page`: Page number (default: 1)
- `limit`: Results per page (default: 20, max: 100)

**Response:** `200 OK`
```json
{
  "templates": [
    {
      "template_id": "order_confirmation",
      "name": "Order Confirmation",
      "channel": "email",
      "subject": "Your order {{order_id}} is confirmed!",
      "version": 2,
      "active": true,
      "created_at": "2024-01-01T00:00:00Z",
      "updated_at": "2025-01-01T00:00:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 45,
    "total_pages": 3
  }
}
```

---

### 2. Render Template (Preview)

**Endpoint:** `POST /api/v1/templates/:id/render`

**Request:**
```json
{
  "variables": {
    "user_name": "John Doe",
    "order_id": "ORD-123",
    "amount": "$99.99"
  }
}
```

**Response:** `200 OK`
```json
{
  "template_id": "order_confirmation",
  "channel": "email",
  "rendered": {
    "subject": "Your order ORD-123 is confirmed!",
    "body_html": "<html>...",
    "body_text": "Hi John Doe, your order ORD-123 for $99.99 is confirmed."
  }
}
```

---

## Analytics APIs

### 1. Get Summary Stats

**Endpoint:** `GET /api/v1/analytics/summary`

**Query Parameters:**
- `start_date`: Start date (ISO 8601)
- `end_date`: End date (ISO 8601)
- `channel`: Filter by channel

**Response:** `200 OK`
```json
{
  "period": {
    "start": "2025-01-01T00:00:00Z",
    "end": "2025-01-15T23:59:59Z"
  },
  "total_sent": 1500000,
  "by_channel": {
    "email": {
      "sent": 600000,
      "delivered": 588000,
      "opened": 140000,
      "clicked": 30000,
      "bounced": 12000,
      "delivery_rate": 0.98,
      "open_rate": 0.238,
      "click_rate": 0.214
    },
    "sms": {
      "sent": 150000,
      "delivered": 147000,
      "failed": 3000,
      "delivery_rate": 0.98
    },
    "push": {
      "sent": 675000,
      "delivered": 650000,
      "opened": 95000,
      "delivery_rate": 0.963,
      "open_rate": 0.146
    }
  },
  "cost": {
    "total": 2250.50,
    "by_channel": {
      "email": 60.00,
      "sms": 1500.00,
      "push": 690.50
    }
  }
}
```

---

## Rate Limiting

### Headers
```http
X-RateLimit-Limit: 1000        # Total allowed per hour
X-RateLimit-Remaining: 750     # Remaining in current window
X-RateLimit-Reset: 1642252800  # Unix timestamp when limit resets
```

### Exceeded Response
```json
429 Too Many Requests
{
  "error": "rate_limit_exceeded",
  "message": "API rate limit exceeded. Limit: 1000 requests per hour",
  "retry_after": 3600
}
```

---

## Pagination

**Request:**
```http
GET /api/v1/notifications?user_id=123&page=2&limit=50
```

**Response:**
```json
{
  "notifications": [...],
  "pagination": {
    "page": 2,
    "limit": 50,
    "total": 523,
    "total_pages": 11,
    "has_next": true,
    "has_prev": true,
    "next_page": "/api/v1/notifications?user_id=123&page=3&limit=50",
    "prev_page": "/api/v1/notifications?user_id=123&page=1&limit=50"
  }
}
```

---

## Webhooks

**Endpoint:** User provides webhook URL when creating notification

**Provider Events:**
```http
POST https://your-app.com/webhooks/notifications
Headers:
  X-Webhook-Signature: sha256=abc123...
  Content-Type: application/json

{
  "event": "notification.delivered",
  "notification_id": "notif_1a2b3c4d5e",
  "user_id": 12345,
  "channel": "email",
  "timestamp": "2025-01-15T10:30:05Z",
  "provider": "sendgrid",
  "provider_data": {
    "message_id": "sg_abc123",
    "ip": "192.168.1.1"
  }
}
```

**Event Types:**
- `notification.queued`
- `notification.sent`
- `notification.delivered`
- `notification.opened`
- `notification.clicked`
- `notification.bounced`
- `notification.failed`

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `validation_error` | 400 | Invalid request parameters |
| `authentication_failed` | 401 | Invalid API key |
| `forbidden` | 403 | Insufficient permissions |
| `not_found` | 404 | Resource not found |
| `rate_limit_exceeded` | 429 | Too many requests |
| `internal_error` | 500 | Server error |
| `service_unavailable` | 503 | Temporary outage |

---

## API Versioning

**URL Versioning (Current):**
```
/v1/notifications  # Version 1 (current)
/v2/notifications  # Version 2 (future)
```

**Header Versioning (Alternative):**
```http
GET /api/notifications
Headers:
  X-API-Version: 2
```

**Deprecation Notice:**
```http
Response Headers:
X-API-Deprecated: true
X-API-Sunset: 2026-01-01
Link: </v2/notifications>; rel="successor-version"
```

---

**💡 Best Practices:**
1. Always use HTTPS
2. Include request IDs for tracing
3. Return proper HTTP status codes
4. Use pagination for list endpoints
5. Document all error codes
6. Version APIs from day 1
