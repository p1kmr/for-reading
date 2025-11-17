# API Design

## API Design Principles

### 1. RESTful Standards
- Use HTTP methods correctly (GET, POST, PUT, DELETE)
- Resource-based URLs (/products, /orders)
- Stateless (no server-side sessions)
- Versioning (/api/v1/, /api/v2/)

### 2. Response Format (JSON)
```json
{
  "success": true,
  "data": { ... },
  "meta": {
    "timestamp": "2025-11-17T10:30:00Z",
    "requestId": "req-12345"
  }
}
```

---

## Complete API Specification

### Product API

```
GET    /api/v1/products?page=1&limit=20&sort=price&order=asc
GET    /api/v1/products/{productId}
GET    /api/v1/products/search?q=laptop&category=electronics&minPrice=500&maxPrice=1000
POST   /api/v1/products (Admin/Seller only)
PUT    /api/v1/products/{productId} (Admin/Seller only)
DELETE /api/v1/products/{productId} (Admin only)
POST   /api/v1/products/{productId}/reviews
GET    /api/v1/products/{productId}/reviews?page=1&limit=10
```

**Example: Search Products**
```bash
# Request
curl -X GET "https://api.amazonshop.com/api/v1/products/search?q=laptop&minPrice=500&maxPrice=1500" \
  -H "Authorization: Bearer {jwt_token}"

# Response (200 OK)
{
  "success": true,
  "data": {
    "products": [
      {
        "productId": 12345,
        "name": "Dell Laptop 15-inch",
        "description": "High performance laptop...",
        "price": 899.99,
        "category": "Electronics",
        "rating": 4.5,
        "reviewCount": 1024,
        "stock": 50,
        "images": [
          "https://cdn.amazonshop.com/products/12345-main.jpg",
          "https://cdn.amazonshop.com/products/12345-1.jpg"
        ],
        "seller": {
          "sellerId": 5001,
          "name": "Dell Official Store",
          "rating": 4.8
        }
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 156,
      "totalPages": 8
    }
  },
  "meta": {
    "timestamp": "2025-11-17T10:30:00Z",
    "requestId": "req-search-12345"
  }
}
```

### User API

```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
POST   /api/v1/auth/refresh-token
GET    /api/v1/users/me
PUT    /api/v1/users/me
POST   /api/v1/users/me/addresses
GET    /api/v1/users/me/addresses
PUT    /api/v1/users/me/addresses/{addressId}
DELETE /api/v1/users/me/addresses/{addressId}
```

**Example: User Registration**
```bash
# Request
curl -X POST "https://api.amazonshop.com/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!",
    "name": "John Doe",
    "phone": "+1234567890"
  }'

# Response (201 Created)
{
  "success": true,
  "data": {
    "userId": 1001,
    "email": "user@example.com",
    "name": "John Doe",
    "createdAt": "2025-11-17T10:30:00Z",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here"
  }
}
```

### Order API

```
POST   /api/v1/cart/add
GET    /api/v1/cart
PUT    /api/v1/cart/items/{itemId}
DELETE /api/v1/cart/items/{itemId}
POST   /api/v1/orders
GET    /api/v1/orders
GET    /api/v1/orders/{orderId}
PUT    /api/v1/orders/{orderId}/cancel
GET    /api/v1/orders/{orderId}/track
```

**Example: Place Order**
```bash
# Request
curl -X POST "https://api.amazonshop.com/api/v1/orders" \
  -H "Authorization: Bearer {jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      { "productId": 12345, "quantity": 1 },
      { "productId": 67890, "quantity": 2 }
    ],
    "shippingAddressId": 501,
    "paymentMethodId": 201
  }'

# Response (201 Created)
{
  "success": true,
  "data": {
    "orderId": 50001,
    "userId": 1001,
    "status": "PENDING",
    "totalAmount": 1599.97,
    "items": [
      {
        "productId": 12345,
        "name": "Dell Laptop",
        "quantity": 1,
        "price": 899.99
      },
      {
        "productId": 67890,
        "name": "Wireless Mouse",
        "quantity": 2,
        "price": 349.99
      }
    ],
    "shippingAddress": {
      "addressId": 501,
      "line1": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001"
    },
    "createdAt": "2025-11-17T10:30:00Z",
    "estimatedDelivery": "2025-11-20T18:00:00Z"
  }
}
```

### Payment API

```
POST   /api/v1/payments/process
GET    /api/v1/payments/{paymentId}
POST   /api/v1/payments/refund
POST   /api/v1/payments/methods (Add payment method)
GET    /api/v1/payments/methods
DELETE /api/v1/payments/methods/{methodId}
```

---

## Authentication & Authorization

### JWT Token Structure
```javascript
{
  "userId": 1001,
  "email": "user@example.com",
  "role": "user",  // user, seller, admin
  "iat": 1700218800,  // Issued at
  "exp": 1700219700   // Expires in 15 minutes
}
```

### Authorization Levels

| Role | Permissions |
|------|-------------|
| **Guest** | Browse products, search |
| **User** | Browse, search, add to cart, place order, review |
| **Seller** | All user permissions + manage own products, view sales |
| **Admin** | All permissions + manage all products, users, orders |

### Rate Limiting

```
Per User:
- 100 requests/minute (authenticated)
- 1,000 requests/hour

Per IP:
- 20 requests/minute (unauthenticated)
- 500 requests/hour

Per API Key (for partners):
- 10,000 requests/minute
```

---

## Error Handling

### Error Response Format
```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "Product with ID 12345 not found",
    "details": {
      "productId": 12345
    }
  },
  "meta": {
    "timestamp": "2025-11-17T10:30:00Z",
    "requestId": "req-error-12345"
  }
}
```

### HTTP Status Codes

| Code | Meaning | Example |
|------|---------|---------|
| **200** | OK | Successful GET, PUT |
| **201** | Created | Successful POST (create) |
| **204** | No Content | Successful DELETE |
| **400** | Bad Request | Invalid input |
| **401** | Unauthorized | Missing/invalid token |
| **403** | Forbidden | No permission |
| **404** | Not Found | Resource doesn't exist |
| **409** | Conflict | Duplicate resource |
| **429** | Too Many Requests | Rate limit exceeded |
| **500** | Internal Server Error | Server error |
| **503** | Service Unavailable | Server overloaded |

---

## Pagination

```bash
# Request
GET /api/v1/products?page=2&limit=20

# Response
{
  "data": { ... },
  "pagination": {
    "page": 2,
    "limit": 20,
    "total": 5000,
    "totalPages": 250,
    "hasNext": true,
    "hasPrev": true,
    "links": {
      "first": "/api/v1/products?page=1&limit=20",
      "prev": "/api/v1/products?page=1&limit=20",
      "next": "/api/v1/products?page=3&limit=20",
      "last": "/api/v1/products?page=250&limit=20"
    }
  }
}
```

---

**Previous**: [07_step6_microservices_final.md](./07_step6_microservices_final.md)
**Next**: [09_database_design.md](./09_database_design.md)
