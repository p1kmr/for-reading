# API Design & Data Flow Diagrams

> **Learning Goal**: Understand how to design clean, RESTful APIs and trace request flows through the entire system with sequence diagrams!

---

## RESTful API Endpoints

### Authentication APIs

#### POST /api/v1/auth/register
**Description**: Create new user account

**Request**:
```json
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "full_name": "John Doe"
}
```

**Response** (201 Created):
```json
{
  "user_id": 123,
  "username": "john_doe",
  "email": "john@example.com",
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
  "token_expires_in": 3600
}
```

---

#### POST /api/v1/auth/login
**Description**: User login

**Request**:
```json
POST /api/v1/auth/login

{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Response** (200 OK):
```json
{
  "user_id": 123,
  "access_token": "eyJhbGci...",
  "refresh_token": "dGhpcyB...",
  "token_expires_in": 3600
}
```

**JWT Token Payload**:
```json
{
  "user_id": 123,
  "username": "john_doe",
  "iat": 1704067200,    // Issued at
  "exp": 1704070800     // Expires at (1 hour later)
}
```

---

### User APIs

#### GET /api/v1/users/{user_id}
**Description**: Get user profile

**Request**:
```http
GET /api/v1/users/123
Authorization: Bearer eyJhbGci...
```

**Response** (200 OK):
```json
{
  "user_id": 123,
  "username": "john_doe",
  "full_name": "John Doe",
  "bio": "Photographer 📷 | Travel enthusiast",
  "profile_pic_url": "https://cdn.instagram.com/profiles/123/pic.jpg",
  "followers_count": 1250,
  "following_count": 430,
  "posts_count": 89,
  "is_following": false,
  "is_followed_by": false,
  "created_at": "2024-06-15T10:30:00Z"
}
```

---

#### PUT /api/v1/users/{user_id}
**Description**: Update user profile

**Request**:
```json
PUT /api/v1/users/123
Authorization: Bearer eyJhbGci...

{
  "bio": "New bio text",
  "profile_pic_url": "https://cdn.../new_pic.jpg"
}
```

**Response** (200 OK):
```json
{
  "user_id": 123,
  "bio": "New bio text",
  "profile_pic_url": "https://cdn.../new_pic.jpg",
  "updated_at": "2025-01-17T14:00:00Z"
}
```

---

### Post APIs

#### POST /api/v1/posts
**Description**: Create new post (photo/video)

**Request**:
```json
POST /api/v1/posts
Authorization: Bearer eyJhbGci...
Content-Type: multipart/form-data

{
  "caption": "Beautiful sunset at the beach 🌅",
  "media": [File1, File2],    // Binary files
  "tags": ["#sunset", "#beach", "#photography"]
}
```

**Response** (201 Created):
```json
{
  "post_id": 789,
  "user_id": 123,
  "caption": "Beautiful sunset at the beach 🌅",
  "media": [
    {
      "type": "image",
      "url": "https://cdn.instagram.com/posts/789/img1.jpg",
      "thumbnail_url": "https://cdn.instagram.com/posts/789/thumb1.jpg"
    }
  ],
  "likes_count": 0,
  "comments_count": 0,
  "created_at": "2025-01-17T14:30:00Z",
  "status": "processing"    // Image being resized in background
}
```

---

#### GET /api/v1/posts/{post_id}
**Description**: Get single post details

**Request**:
```http
GET /api/v1/posts/789
Authorization: Bearer eyJhbGci...
```

**Response** (200 OK):
```json
{
  "post_id": 789,
  "user": {
    "user_id": 123,
    "username": "john_doe",
    "profile_pic_url": "https://cdn.../profile.jpg"
  },
  "caption": "Beautiful sunset 🌅",
  "media": [...],
  "likes_count": 1250,
  "comments_count": 34,
  "has_liked": false,
  "has_saved": false,
  "created_at": "2025-01-17T14:30:00Z",
  "location": {
    "name": "Santa Monica Beach",
    "latitude": 34.0195,
    "longitude": -118.4912
  }
}
```

---

#### DELETE /api/v1/posts/{post_id}
**Description**: Delete post

**Request**:
```http
DELETE /api/v1/posts/789
Authorization: Bearer eyJhbGci...
```

**Response** (204 No Content)

---

### Feed APIs

#### GET /api/v1/feed
**Description**: Get personalized feed (posts from accounts user follows)

**Request**:
```http
GET /api/v1/feed?limit=20&offset=0
Authorization: Bearer eyJhbGci...
```

**Response** (200 OK):
```json
{
  "posts": [
    {
      "post_id": 789,
      "user": {...},
      "caption": "Beautiful sunset 🌅",
      "media": [...],
      "likes_count": 1250,
      "comments_count": 34,
      "has_liked": false,
      "created_at": "2025-01-17T14:30:00Z"
    },
    // ... 19 more posts
  ],
  "pagination": {
    "next_offset": 20,
    "has_more": true,
    "total_count": 1500
  }
}
```

**Query Parameters**:
- `limit`: Number of posts to return (default: 20, max: 50)
- `offset`: Pagination offset (default: 0)
- `max_timestamp`: Return posts older than this timestamp (for infinite scroll)

---

### Social Interaction APIs

#### POST /api/v1/posts/{post_id}/like
**Description**: Like a post

**Request**:
```http
POST /api/v1/posts/789/like
Authorization: Bearer eyJhbGci...
```

**Response** (201 Created):
```json
{
  "post_id": 789,
  "likes_count": 1251,
  "has_liked": true
}
```

---

#### DELETE /api/v1/posts/{post_id}/like
**Description**: Unlike a post

**Request**:
```http
DELETE /api/v1/posts/789/like
Authorization: Bearer eyJhbGci...
```

**Response** (200 OK):
```json
{
  "post_id": 789,
  "likes_count": 1250,
  "has_liked": false
}
```

---

#### POST /api/v1/posts/{post_id}/comments
**Description**: Add comment to post

**Request**:
```json
POST /api/v1/posts/789/comments
Authorization: Bearer eyJhbGci...

{
  "text": "Amazing photo! 😍"
}
```

**Response** (201 Created):
```json
{
  "comment_id": 5678,
  "post_id": 789,
  "user": {
    "user_id": 456,
    "username": "jane_doe",
    "profile_pic_url": "https://cdn.../profile.jpg"
  },
  "text": "Amazing photo! 😍",
  "likes_count": 0,
  "created_at": "2025-01-17T15:00:00Z"
}
```

---

#### GET /api/v1/posts/{post_id}/comments
**Description**: Get comments for a post

**Request**:
```http
GET /api/v1/posts/789/comments?limit=10&offset=0
Authorization: Bearer eyJhbGci...
```

**Response** (200 OK):
```json
{
  "comments": [
    {
      "comment_id": 5678,
      "user": {...},
      "text": "Amazing photo! 😍",
      "likes_count": 5,
      "created_at": "2025-01-17T15:00:00Z"
    },
    // ... more comments
  ],
  "pagination": {
    "next_offset": 10,
    "has_more": true,
    "total_count": 34
  }
}
```

---

### Follow/Unfollow APIs

#### POST /api/v1/users/{user_id}/follow
**Description**: Follow a user

**Request**:
```http
POST /api/v1/users/456/follow
Authorization: Bearer eyJhbGci...
```

**Response** (201 Created):
```json
{
  "following_id": 456,
  "is_following": true,
  "followers_count": 1251
}
```

---

#### DELETE /api/v1/users/{user_id}/follow
**Description**: Unfollow a user

**Request**:
```http
DELETE /api/v1/users/456/follow
Authorization: Bearer eyJhbGci...
```

**Response** (200 OK):
```json
{
  "following_id": 456,
  "is_following": false,
  "followers_count": 1250
}
```

---

### Search APIs

#### GET /api/v1/search/users
**Description**: Search users by username

**Request**:
```http
GET /api/v1/search/users?q=john&limit=10
Authorization: Bearer eyJhbGci...
```

**Response** (200 OK):
```json
{
  "users": [
    {
      "user_id": 123,
      "username": "john_doe",
      "full_name": "John Doe",
      "profile_pic_url": "https://cdn.../profile.jpg",
      "followers_count": 1250,
      "is_following": false
    },
    // ... more results
  ],
  "total_count": 45
}
```

---

#### GET /api/v1/search/tags
**Description**: Search posts by hashtag

**Request**:
```http
GET /api/v1/search/tags?q=sunset&limit=20
Authorization: Bearer eyJhbGci...
```

**Response** (200 OK):
```json
{
  "tag": "sunset",
  "post_count": 1500000,
  "posts": [
    {
      "post_id": 789,
      "thumbnail_url": "https://cdn.../thumb.jpg",
      "likes_count": 1250,
      "comments_count": 34
    },
    // ... more posts
  ]
}
```

---

## Data Flow Diagrams (Sequence Diagrams)

### Flow 1: User Registration

```mermaid
sequenceDiagram
    actor User
    participant Mobile
    participant LB as Load Balancer
    participant App as App Server
    participant DB as PostgreSQL (Shard Router)
    participant Redis

    User->>Mobile: Enter username, email, password
    Mobile->>LB: POST /api/v1/auth/register

    LB->>App: Route to App Server

    App->>App: Validate input<br/>(email format, password strength)

    App->>DB: Check username exists<br/>SELECT * FROM users<br/>WHERE username = 'john_doe'

    alt Username exists
        DB-->>App: User found
        App-->>Mobile: 409 Conflict<br/>"Username already taken"
        Mobile->>User: Show error
    else Username available
        DB-->>App: No results

        App->>App: Hash password<br/>(bcrypt, 10 rounds)

        App->>DB: INSERT INTO users<br/>(username, email, password_hash)
        DB-->>App: user_id: 123

        App->>App: Generate JWT token<br/>(user_id, exp: 1h)

        App->>Redis: SET session:{user_id}<br/>Value: {token, metadata}<br/>TTL: 3600s

        App-->>Mobile: 201 Created<br/>{user_id, access_token, refresh_token}

        Mobile->>User: Navigate to home screen
    end
```

---

### Flow 2: User Login

```mermaid
sequenceDiagram
    actor User
    participant Mobile
    participant App as App Server
    participant DB as PostgreSQL
    participant Redis

    User->>Mobile: Enter email, password
    Mobile->>App: POST /api/v1/auth/login

    App->>DB: SELECT user_id, password_hash<br/>FROM users WHERE email='...'

    alt User not found
        DB-->>App: No results
        App-->>Mobile: 401 Unauthorized<br/>"Invalid credentials"
    else User found
        DB-->>App: {user_id: 123, password_hash: '...'}

        App->>App: Compare password<br/>bcrypt.compare(input, hash)

        alt Password incorrect
            App-->>Mobile: 401 Unauthorized
        else Password correct
            App->>App: Generate JWT token

            App->>Redis: SET session:123<br/>EX 3600

            App-->>Mobile: 200 OK<br/>{access_token, refresh_token}

            Mobile->>User: Navigate to feed
        end
    end
```

---

### Flow 3: Upload Photo (Complete Flow)

```mermaid
sequenceDiagram
    actor User
    participant Mobile
    participant App as App Server
    participant S3 as AWS S3
    participant DB as PostgreSQL
    participant Kafka
    participant Worker as Image Resize Worker
    participant CDN as CloudFront

    User->>Mobile: Select photo, add caption
    Mobile->>Mobile: Compress image<br/>(client-side, 2MB max)

    Mobile->>App: POST /api/v1/posts<br/>(multipart/form-data)

    App->>App: Validate JWT token

    Note over App: Generate unique IDs
    App->>App: post_id = generateId()<br/>s3_key = user_123/post_789/original.jpg

    par Upload original to S3
        App->>S3: PUT original.jpg
        S3-->>App: URL
    and Save metadata to DB
        App->>DB: INSERT INTO posts<br/>(post_id, user_id, caption, s3_key, status='processing')
        DB-->>App: Success
    end

    App->>Kafka: Publish event<br/>Topic: photo.uploaded<br/>{post_id, user_id, s3_key}

    App-->>Mobile: 201 Created<br/>{post_id, status: 'processing'}

    Mobile->>User: "Post uploaded! 🎉"

    Note over User,CDN: User sees success immediately<br/>Processing happens in background

    Kafka->>Worker: Consume event

    par Resize to thumbnail
        Worker->>S3: Download original
        Worker->>Worker: Resize 150x150
        Worker->>S3: Upload thumb.jpg
    and Resize to medium
        Worker->>Worker: Resize 640x640
        Worker->>S3: Upload medium.jpg
    and Resize to full
        Worker->>Worker: Resize 1080x1080
        Worker->>S3: Upload full.jpg
    end

    Worker->>DB: UPDATE posts SET<br/>thumb_url='...', medium_url='...',<br/>full_url='...', status='complete'

    Worker->>CDN: Warm cache<br/>(prefetch URLs to edge)

    Note over Worker: Processing complete (3-5 sec)<br/>Post now visible to all users
```

---

### Flow 4: View Feed (Cache Hit)

```mermaid
sequenceDiagram
    actor User
    participant Mobile
    participant App as App Server
    participant Redis
    participant DB as PostgreSQL

    User->>Mobile: Open Instagram app
    Mobile->>App: GET /api/v1/feed?limit=20

    App->>App: Validate JWT<br/>Extract user_id: 123

    App->>Redis: GET feed:user:123

    alt Cache HIT ✅
        Redis-->>App: JSON array of 100 posts<br/>(pre-computed feed)

        Note over App: Filter top 20 posts<br/>Check if user has liked each

        par Check likes in parallel
            App->>Redis: GET like:user:123:post:789
            App->>Redis: GET like:user:123:post:788
            App->>Redis: GET like:user:123:post:787
        end

        Redis-->>App: Like statuses

        App-->>Mobile: 200 OK<br/>{posts: [...20 posts]}

        Mobile->>User: Display feed

        Note over User,DB: Total time: 15ms ✅<br/>85% under target!

    else Cache MISS ❌
        Redis-->>App: null

        App->>DB: SELECT following_id<br/>FROM follows<br/>WHERE follower_id=123

        DB-->>App: [user_456, user_789, ...]

        App->>DB: SELECT posts FROM posts<br/>WHERE user_id IN (...)<br/>ORDER BY created_at DESC

        DB-->>App: Posts data

        App->>Redis: SET feed:user:123<br/>EX 30

        App-->>Mobile: 200 OK

        Note over User,DB: Total time: 120ms<br/>(first time only)
    end
```

---

### Flow 5: Like a Post

```mermaid
sequenceDiagram
    actor User
    participant Mobile
    participant App as App Server
    participant DB as PostgreSQL (Shard Router)
    participant Redis
    participant Kafka
    participant Worker as Notification Worker

    User->>Mobile: Tap ❤️ on post
    Mobile->>App: POST /api/v1/posts/789/like

    App->>App: Validate JWT<br/>user_id: 456

    par Write to DB
        App->>DB: INSERT INTO likes<br/>(user_id=456, post_id=789)<br/>ON CONFLICT DO NOTHING
        DB-->>App: Success

        App->>DB: UPDATE posts<br/>SET likes_count = likes_count + 1<br/>WHERE post_id=789
        DB-->>App: likes_count: 1251
    and Update cache
        App->>Redis: SET like:user:456:post:789<br/>Value: 1, EX 600

        App->>Redis: DEL post:789<br/>(invalidate post cache)
    end

    App->>Kafka: Publish event<br/>Topic: like.added<br/>{post_id: 789, user_id: 456, author_id: 123}

    App-->>Mobile: 201 Created<br/>{likes_count: 1251, has_liked: true}

    Mobile->>User: ❤️ turns red<br/>Count: 1,251

    Note over Mobile: Optimistic update!<br/>UI updated instantly

    Kafka->>Worker: Consume event

    Worker->>DB: Get post author info
    DB-->>Worker: author_id: 123

    Worker->>Worker: Check if should notify<br/>(don't notify if author liked own post)

    alt Should notify
        Worker->>DB: INSERT INTO notifications<br/>(user_id=123, type='like', actor_id=456)

        Note over Worker: In real Instagram, this would<br/>trigger push notification via FCM/APNs
    end
```

---

### Flow 6: Follow User (With Feed Backfill)

```mermaid
sequenceDiagram
    actor Alice
    participant Mobile
    participant App as App Server
    participant DB as PostgreSQL
    participant Redis
    participant Kafka
    participant Worker as Feed Worker

    Alice->>Mobile: Tap "Follow" on Bob's profile
    Mobile->>App: POST /api/v1/users/bob_id/follow

    App->>App: Validate JWT<br/>follower_id: alice_id

    App->>DB: INSERT INTO follows<br/>(follower_id=alice_id, following_id=bob_id)
    DB-->>App: Success

    par Update counters
        App->>DB: UPDATE users SET following_count++<br/>WHERE user_id=alice_id

        App->>DB: UPDATE users SET followers_count++<br/>WHERE user_id=bob_id
    and Invalidate caches
        App->>Redis: DEL user:profile:alice_id
        App->>Redis: DEL user:profile:bob_id
        App->>Redis: DEL feed:user:alice_id<br/>(feed will now include Bob's posts)
    end

    App->>Kafka: Publish event<br/>Topic: user.followed<br/>{follower_id: alice_id, following_id: bob_id}

    App-->>Mobile: 201 Created<br/>{is_following: true}

    Mobile->>Alice: "Following" button

    Kafka->>Worker: Consume event

    Note over Worker: Backfill Alice's feed with Bob's recent posts

    Worker->>DB: SELECT * FROM posts<br/>WHERE user_id=bob_id<br/>ORDER BY created_at DESC<br/>LIMIT 100

    DB-->>Worker: Bob's recent 100 posts

    Worker->>Redis: ZADD feed:user:alice_id<br/>(add Bob's posts to Alice's feed)

    Note over Worker: Next time Alice opens feed,<br/>she'll see Bob's posts!
```

---

## API Best Practices

### 1. Versioning
```
Use URL versioning: /api/v1/, /api/v2/

Why?
- Breaking changes don't break existing clients
- Mobile apps can update gradually
- Can deprecate old versions gracefully

Example:
v1: /api/v1/posts (returns all fields)
v2: /api/v2/posts (returns optimized payload, 30% smaller)
```

### 2. Pagination
```javascript
GET /api/v1/feed?limit=20&offset=40

// Cursor-based pagination (better for real-time feeds)
GET /api/v1/feed?limit=20&max_id=789
// Returns 20 posts with post_id < 789
```

### 3. Rate Limiting
```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1704070800
Retry-After: 60

{
  "error": "Rate limit exceeded",
  "message": "Max 100 requests per hour. Try again in 60 seconds."
}
```

**Implementation**:
```javascript
// Redis-based rate limiting
async function checkRateLimit(userId, endpoint) {
  const key = `ratelimit:${userId}:${endpoint}`;
  const count = await redis.incr(key);

  if (count === 1) {
    await redis.expire(key, 3600);  // 1 hour window
  }

  if (count > 100) {
    throw new RateLimitError('Too many requests');
  }

  return {
    limit: 100,
    remaining: 100 - count,
    reset: await redis.ttl(key)
  };
}
```

### 4. Error Handling
```json
{
  "error": {
    "code": "INVALID_INPUT",
    "message": "Caption cannot exceed 2200 characters",
    "field": "caption",
    "timestamp": "2025-01-17T15:30:00Z",
    "request_id": "req_abc123"
  }
}
```

**HTTP Status Codes**:
- 200: Success
- 201: Created
- 400: Bad Request (invalid input)
- 401: Unauthorized (invalid token)
- 403: Forbidden (valid token, but no permission)
- 404: Not Found
- 409: Conflict (username already exists)
- 429: Too Many Requests
- 500: Internal Server Error
- 503: Service Unavailable

### 5. Authentication
```javascript
// JWT token in Authorization header
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

// Middleware to validate token
async function authenticateRequest(req, res, next) {
  const token = req.headers.authorization?.split(' ')[1];

  if (!token) {
    return res.status(401).json({ error: 'No token provided' });
  }

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.userId = decoded.user_id;
    next();
  } catch (err) {
    return res.status(401).json({ error: 'Invalid token' });
  }
}
```

---

## Summary

### API Design Principles
- ✅ RESTful design (nouns, not verbs)
- ✅ Versioning (/v1/, /v2/)
- ✅ Pagination (limit, offset, cursor)
- ✅ Rate limiting (prevent abuse)
- ✅ JWT authentication (stateless)
- ✅ Clear error messages
- ✅ Idempotency (PUT, DELETE)

### Key Data Flows
1. **Upload**: Mobile → App → S3 → Kafka → Worker → CDN
2. **Feed**: Mobile → App → Redis (cache) → DB (miss) → Fanout
3. **Like**: Mobile → App → DB → Redis → Kafka → Notification
4. **Follow**: Mobile → App → DB → Redis invalidation → Feed backfill

### Performance Targets Achieved
- Feed load: < 20ms (cache hit)
- Upload: < 550ms (async processing)
- Like/Comment: < 50ms
- Search: < 100ms (ElasticSearch - next section)

