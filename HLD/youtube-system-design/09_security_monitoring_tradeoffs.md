# Security, Monitoring & Trade-offs

This guide covers the critical aspects of security, monitoring, and design trade-offs for building a production-ready YouTube-like system.

---

## Table of Contents
1. [Security Architecture](#security-architecture)
2. [Monitoring & Observability](#monitoring--observability)
3. [Trade-offs & Design Decisions](#trade-offs--design-decisions)
4. [Common Beginner Mistakes](#common-beginner-mistakes)

---

## Security Architecture

Security is not an afterthought—it must be designed into the system from the beginning. Here's how to protect a YouTube-like platform.

### 1. Authentication & Authorization

**Authentication** verifies WHO you are. **Authorization** determines WHAT you can do.

#### JWT (JSON Web Tokens)
```
User Login Flow:
1. User submits credentials → Auth Service
2. Auth Service validates → Returns JWT token
3. Client stores token (localStorage/cookie)
4. Every API request includes: Authorization: Bearer <JWT>
5. API Gateway validates token before forwarding request

JWT Structure:
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "user_id": "12345",
    "role": "premium_user",
    "exp": 1735689600  // Expiration timestamp
  },
  "signature": "..."
}
```

**Why JWT?**
- Stateless: No need to store sessions in database
- Scalable: Works well with microservices
- Self-contained: Contains all user info needed

**Security Best Practices:**
- Short expiration time (15-30 minutes for access tokens)
- Use refresh tokens for long-term sessions
- Store JWT in HttpOnly cookies to prevent XSS
- Sign tokens with strong secret keys (256-bit minimum)

#### OAuth 2.0
Used for third-party integrations (login with Google, Facebook).

```
OAuth Flow:
1. User clicks "Login with Google"
2. Redirect to Google's authorization page
3. User grants permissions
4. Google redirects back with authorization code
5. Backend exchanges code for access token
6. Use access token to fetch user profile
7. Create account or login user
```

**Benefits:**
- Users don't share passwords with your system
- Reduces liability (password breaches)
- Better user experience (one-click login)

#### API Keys
For programmatic access (YouTube Data API for developers).

```
API Key Usage:
- Create unique key per application/developer
- Rate limit per API key (e.g., 10,000 requests/day)
- Track usage for billing
- Easy to revoke if compromised

Example:
GET /api/v1/videos/trending
Headers:
  X-API-Key: AIzaSyC3xmpl3K3yV4lu3
```

**Implementation:**
```python
def validate_api_key(key: str) -> bool:
    # Check in cache first (Redis)
    cached = redis.get(f"api_key:{key}")
    if cached:
        return True

    # Check database
    api_key = db.query(APIKey).filter(key=key, active=True).first()
    if api_key:
        redis.setex(f"api_key:{key}", 3600, "valid")
        return True
    return False
```

### 2. Data Encryption

#### At Rest (AES-256)
Encrypt sensitive data in databases and storage.

```
What to Encrypt:
✅ User passwords (use bcrypt/argon2, not just AES)
✅ Payment information
✅ Private video content
✅ Personal identification data (PII)
✅ API keys and secrets

Database Example:
users table:
- password: $2b$12$hashed... (bcrypt)
- email_encrypted: AES256(email, encryption_key)
- phone_encrypted: AES256(phone, encryption_key)
```

**Implementation with AWS:**
```yaml
S3 Bucket Encryption:
  Type: AWS::S3::Bucket
  Properties:
    BucketEncryption:
      ServerSideEncryptionConfiguration:
        - ServerSideEncryptionByDefault:
            SSEAlgorithm: AES256

RDS Encryption:
  Enabled: true
  KMSKeyId: arn:aws:kms:region:account:key/key-id
```

#### In Transit (TLS 1.3)
Encrypt data as it moves between client and server.

```
TLS Configuration:
1. Obtain SSL/TLS certificate (Let's Encrypt for free)
2. Configure web server (Nginx/Apache)
3. Force HTTPS (redirect HTTP → HTTPS)
4. Enable HSTS (HTTP Strict Transport Security)

Nginx Config:
server {
    listen 443 ssl http2;
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    ssl_protocols TLSv1.3 TLSv1.2;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # HSTS
    add_header Strict-Transport-Security "max-age=31536000" always;
}
```

**Why TLS 1.3?**
- Faster handshake (1-RTT vs 2-RTT)
- Stronger encryption algorithms
- Removes vulnerable ciphers
- Better forward secrecy

### 3. DRM (Digital Rights Management)

Protects premium content from unauthorized copying/sharing.

```
DRM Solutions:
1. Google Widevine (Chrome, Android)
2. Apple FairPlay (Safari, iOS)
3. Microsoft PlayReady (Edge, Xbox)

DRM Flow:
1. User requests premium video
2. Video Service validates subscription
3. Generate license request token
4. Client sends token to DRM License Server
5. License Server returns decryption keys
6. Video player decrypts and plays (in secure enclave)
7. Keys expire after viewing session

Technology:
- EME (Encrypted Media Extensions) in browser
- Hardware-backed secure storage
- Cannot extract raw video file
```

**Implementation:**
```javascript
// Client-side DRM setup
const video = document.querySelector('video');
const config = {
  drm: {
    widevine: {
      LA_URL: 'https://license.youtube.com/proxy',
      headers: {
        'X-License-Token': 'user_license_token'
      }
    }
  }
};

player.load(videoUrl, config);
```

### 4. Rate Limiting & DDoS Protection

Prevent abuse and ensure fair resource usage.

#### Rate Limiting
```
Strategies:
1. Fixed Window: 100 requests per minute
2. Sliding Window: More accurate, accounts for burst
3. Token Bucket: Allows burst, refills over time
4. Leaky Bucket: Smooth output rate

Implementation Levels:
- API Gateway level (global rate limits)
- User level (per user limits)
- IP level (prevent anonymous abuse)
- API key level (developer quotas)

Rate Limit Tiers:
Free User:      100 req/hour
Premium User:   1000 req/hour
API Developer:  10,000 req/day
```

**Redis-based Implementation:**
```python
def check_rate_limit(user_id: str, limit: int, window: int) -> bool:
    """
    limit: max requests
    window: time window in seconds
    """
    key = f"rate_limit:{user_id}:{int(time.time() // window)}"
    current = redis.incr(key)

    if current == 1:
        redis.expire(key, window)

    return current <= limit

# Usage
if not check_rate_limit(user_id, 100, 60):
    return Response("Rate limit exceeded", status=429)
```

#### DDoS Protection
```
Layer 3/4 (Network/Transport):
- Use CloudFlare or AWS Shield
- Filter malicious IPs at edge
- SYN flood protection

Layer 7 (Application):
- Challenge-response (CAPTCHA)
- Behavioral analysis
- Geographic filtering
- Bot detection

Architecture:
Internet → CloudFlare (DDoS mitigation)
         → AWS WAF (Web Application Firewall)
         → Load Balancer
         → Application Servers
```

**WAF Rules Example:**
```yaml
AWS WAF Rules:
1. Rate-based rule (block >2000 req/5min from single IP)
2. SQL injection protection
3. XSS protection
4. Known bad inputs blocking
5. Geo-blocking (block specific countries if needed)
6. Size constraint (reject huge payloads)
```

### 5. Content Moderation & Copyright Protection

#### Content ID System
YouTube's automated copyright detection.

```
How Content ID Works:
1. Rights holders upload reference files
2. System generates audio/video fingerprints
3. Every uploaded video is fingerprinted
4. Match against reference database
5. If match found:
   - Block video
   - Monetize (ads go to rights holder)
   - Track (just statistics)
   - Mute audio

Technologies:
- Perceptual hashing (resistant to minor changes)
- Audio fingerprinting (Shazam-like)
- Machine learning models for matching
```

**Implementation:**
```python
def check_copyright(video_path: str) -> dict:
    # Generate fingerprint
    fingerprint = generate_fingerprint(video_path)

    # Search in database (approximate matching)
    matches = search_similar_fingerprints(fingerprint, threshold=0.85)

    if matches:
        return {
            'status': 'copyright_match',
            'match_id': matches[0].id,
            'match_score': matches[0].score,
            'action': matches[0].policy  # block/monetize/track
        }

    return {'status': 'clear'}
```

#### Content Moderation
```
Automated Moderation:
1. Computer Vision: Detect nudity, violence, gore
2. NLP: Detect hate speech, profanity in titles/descriptions
3. Audio Analysis: Detect offensive language

Human Review:
- Flagged content reviewed by moderators
- Appeals process for false positives
- Continuous model training from human feedback

Flow:
Upload → Automated Check → Queue for Human Review (if needed)
         ↓ (Clear)              ↓ (Approved/Rejected)
      Published              Published/Removed
```

### 6. Input Validation & SQL Injection Prevention

#### Input Validation
```python
# Bad: Direct input usage
def search_videos(query: str):
    return db.execute(f"SELECT * FROM videos WHERE title LIKE '%{query}%'")

# Good: Parameterized queries
def search_videos(query: str):
    # Validate input
    if len(query) > 100:
        raise ValueError("Query too long")

    if contains_sql_keywords(query):
        raise ValueError("Invalid characters")

    # Use parameterized query
    return db.execute(
        "SELECT * FROM videos WHERE title LIKE %s",
        (f"%{query}%",)
    )
```

**Validation Checklist:**
```
✅ Length limits (prevent buffer overflow)
✅ Character whitelist (alphanumeric + safe chars)
✅ Type checking (ensure integer is integer)
✅ Range validation (age between 1-120)
✅ Format validation (email, phone regex)
✅ Sanitize HTML input (prevent XSS)
```

#### ORM Usage
```python
# Using ORM (SQLAlchemy) - Safe from SQL injection
from sqlalchemy import select

def get_user_videos(user_id: int):
    # ORM automatically parameterizes
    query = select(Video).where(Video.user_id == user_id)
    return session.execute(query).scalars().all()
```

### 7. CORS (Cross-Origin Resource Sharing) Policies

Controls which domains can access your API from browsers.

```
CORS Headers:
Access-Control-Allow-Origin: https://youtube.com
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: Content-Type, Authorization
Access-Control-Max-Age: 86400
Access-Control-Allow-Credentials: true

Configuration:
# Allow specific origins (production)
ALLOWED_ORIGINS = [
    "https://youtube.com",
    "https://www.youtube.com",
    "https://m.youtube.com"
]

# For development
ALLOWED_ORIGINS = ["http://localhost:3000"]
```

**Implementation (FastAPI):**
```python
from fastapi.middleware.cors import CORSMiddleware

app.add_middleware(
    CORSMiddleware,
    allow_origins=["https://youtube.com"],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE"],
    allow_headers=["*"],
)
```

### 8. Network Security

#### VPC (Virtual Private Cloud)
```
Network Architecture:
Public Subnet:
- Load Balancers
- NAT Gateway
- Bastion Host (jump box)

Private Subnet:
- Application Servers
- Database Servers
- Cache Servers

Isolated Subnet:
- Encryption Services
- Secrets Management

Connectivity:
Internet → Internet Gateway
         → Public Subnet (Load Balancer)
         → Private Subnet (App Servers)
         → Database (no internet access)
```

#### Security Groups (Firewall Rules)
```yaml
Load Balancer Security Group:
  Inbound:
    - Port 443 (HTTPS) from 0.0.0.0/0
    - Port 80 (HTTP) from 0.0.0.0/0 → Redirect to 443
  Outbound:
    - All traffic to Application SG

Application Security Group:
  Inbound:
    - Port 8080 from Load Balancer SG only
  Outbound:
    - Port 5432 (PostgreSQL) to Database SG
    - Port 6379 (Redis) to Cache SG
    - Port 443 to Internet (for external APIs)

Database Security Group:
  Inbound:
    - Port 5432 from Application SG only
  Outbound:
    - None (no outbound needed)
```

**Principle of Least Privilege:**
- Only open ports that are absolutely necessary
- Restrict source to specific security groups (not 0.0.0.0/0)
- Use private subnets for sensitive resources
- Regularly audit security group rules

---

## Monitoring & Observability

"You can't improve what you can't measure." Monitoring helps you understand system health and diagnose issues quickly.

### 1. Metrics to Track

#### The Four Golden Signals (Google SRE)

**1. Latency**
```
What to measure:
- API response time (p50, p95, p99)
- Database query time
- Cache hit latency
- Video transcoding time
- CDN response time

Why p99 matters:
p50 (median): 100ms  → Half users experience this
p95: 300ms           → 5% users wait longer
p99: 1000ms          → 1% users have bad experience
                       (but 1% of 2B users = 20M users!)

Tools:
- Prometheus (metrics collection)
- Grafana (visualization)
```

**2. Error Rate**
```
Track errors by:
- HTTP status codes (4xx, 5xx)
- Service (which microservice failed)
- Endpoint (which API failed)
- User impact (how many users affected)

Error Budget:
SLO: 99.9% uptime
Error Budget: 0.1% = 43 minutes/month
If exceeded → Stop new features, fix reliability
```

**3. Throughput (Traffic)**
```
Metrics:
- Requests per second (RPS)
- Videos uploaded per hour
- Concurrent viewers
- Bandwidth usage (GB/s)
- Active WebSocket connections

Why it matters:
- Capacity planning
- Detect unusual traffic (DDoS or viral video)
- Cost optimization
```

**4. Saturation**
```
Resource utilization:
- CPU usage (keep < 70% for headroom)
- Memory usage
- Disk I/O
- Network bandwidth
- Database connections (% of max)
- Queue depth

Red flags:
- CPU consistently > 80%
- Memory > 90%
- Disk > 85%
- Connection pool exhausted
```

#### Application-Specific Metrics
```
Video Platform Metrics:
- Video upload success rate
- Transcoding queue length
- Average transcoding time per resolution
- CDN cache hit ratio
- Video playback buffer ratio
- Comment post latency
- Search relevance (click-through rate)
- Recommendation click rate
```

### 2. Logging Strategy

#### Centralized Logging with ELK Stack

**E**lasticsearch: Stores and indexes logs
**L**ogstash: Processes and transforms logs
**K**ibana: Visualizes logs and creates dashboards

```
Log Flow:
Application → Log File
            → Filebeat (log shipper)
            → Logstash (processing)
            → Elasticsearch (storage)
            → Kibana (visualization)

Alternative: Application → Fluentd → Elasticsearch → Kibana
```

#### Structured Logging
```json
// Bad: Unstructured log
"User 12345 uploaded video abc.mp4 at 2025-11-17 10:30:00"

// Good: Structured JSON log
{
  "timestamp": "2025-11-17T10:30:00Z",
  "level": "INFO",
  "service": "video-upload",
  "user_id": "12345",
  "video_id": "abc123",
  "filename": "abc.mp4",
  "file_size_mb": 150,
  "duration_seconds": 300,
  "trace_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "message": "Video upload successful"
}
```

**Benefits of Structured Logs:**
- Easy to search: `user_id:12345 AND level:ERROR`
- Easy to aggregate: Count errors by service
- Machine-readable: Automated alerting
- Consistent format: Easier to parse

#### Log Levels
```
DEBUG:   Detailed info for debugging (verbose)
         → Only in development/staging

INFO:    General informational messages
         → "User logged in", "Video uploaded"

WARNING: Something unexpected but handled
         → "Cache miss", "Retry attempted"

ERROR:   Error that doesn't stop the application
         → "Failed to send email", "Database query timeout"

CRITICAL: Severe error that may stop the application
         → "Database connection lost", "Out of memory"

Log Volume Management:
Production: INFO and above
Keep DEBUG logs for < 1% sample rate
```

#### What to Log
```
✅ Do Log:
- Request/response metadata (not full body)
- Authentication attempts (success/failure)
- Database queries (slow queries)
- External API calls
- Errors and exceptions (with stack traces)
- Performance metrics
- State changes (video published, user upgraded)

❌ Don't Log:
- Passwords (even hashed)
- Credit card numbers
- Session tokens
- API keys
- PII in production (unless encrypted)
```

### 3. Distributed Tracing

Tracks requests across multiple microservices.

#### Why Distributed Tracing?
```
Scenario: Video upload is slow (3 seconds)

Without tracing:
- Where is the bottleneck?
- Is it upload, transcoding, database, storage?
- Hard to diagnose across 10+ microservices

With tracing (Jaeger/OpenTelemetry):
Request trace shows:
- API Gateway: 10ms
- Auth Service: 50ms
- Upload Service: 200ms
- S3 Upload: 2500ms ← BOTTLENECK!
- Database Write: 100ms
- Notification Service: 50ms
Total: 2910ms
```

#### Implementation with OpenTelemetry
```python
from opentelemetry import trace
from opentelemetry.exporter.jaeger import JaegerExporter
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor

# Setup
tracer_provider = TracerProvider()
jaeger_exporter = JaegerExporter(
    agent_host_name="localhost",
    agent_port=6831,
)
tracer_provider.add_span_processor(BatchSpanProcessor(jaeger_exporter))
trace.set_tracer_provider(tracer_provider)
tracer = trace.get_tracer(__name__)

# Usage in code
@app.route('/upload')
def upload_video():
    with tracer.start_as_current_span("upload_video") as span:
        span.set_attribute("user_id", user_id)

        with tracer.start_as_current_span("validate_video"):
            validate(video)

        with tracer.start_as_current_span("upload_to_s3"):
            s3_url = upload_to_s3(video)

        with tracer.start_as_current_span("create_db_entry"):
            db.insert(video_metadata)

        return {"status": "success", "trace_id": span.get_span_context().trace_id}
```

#### Trace Visualization
```
Trace ID: f47ac10b-58cc-4372-a567-0e02b2c3d479
Total Duration: 2910ms

├─ API Gateway (10ms)
├─ Auth Service (50ms)
├─ Upload Service (200ms)
│  ├─ Video Validation (50ms)
│  ├─ S3 Upload (2500ms) ← SLOW
│  └─ Database Write (100ms)
└─ Notification Service (50ms)
```

### 4. Alerting Rules

Automate incident detection and notification.

#### Alert Tools
- **PagerDuty**: On-call management, escalation
- **Slack**: Team notifications
- **Email**: Lower priority alerts
- **SMS**: Critical alerts

#### Alert Severity Levels
```
P0 (Critical):
- Service is down (HTTP 5xx > 50%)
- Database is unreachable
- Data loss detected
→ Page on-call engineer immediately (PagerDuty)
→ SMS + Phone call

P1 (High):
- API latency p99 > 5 seconds
- Error rate > 5%
- Disk usage > 90%
→ Slack alert + PagerDuty
→ 15-minute response time SLA

P2 (Medium):
- API latency p99 > 2 seconds
- Error rate > 1%
- Cache hit rate < 80%
→ Slack alert
→ 1-hour response time SLA

P3 (Low):
- Slow query detected
- Unusual traffic pattern
- Certificate expiring in 30 days
→ Email
→ Next business day
```

#### Example Alert Rules (Prometheus)
```yaml
groups:
  - name: api_alerts
    rules:
      # High error rate
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value | humanizePercentage }}"

      # High latency
      - alert: HighLatency
        expr: histogram_quantile(0.99, rate(http_request_duration_seconds_bucket[5m])) > 5
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "API latency is high"
          description: "p99 latency is {{ $value }}s"

      # Database connections exhausted
      - alert: DatabaseConnectionPoolExhausted
        expr: database_connections_active / database_connections_max > 0.9
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool nearly exhausted"
```

#### Alert Best Practices
```
✅ Do:
- Set appropriate thresholds (avoid alert fatigue)
- Include runbook links in alerts
- Test alerts regularly
- Have escalation policies
- Alert on impact, not symptoms

❌ Don't:
- Alert on every minor issue
- Set unrealistic thresholds
- Send all alerts to everyone
- Ignore alerts (leads to alert fatigue)
```

### 5. SLA/SLO Definitions

**SLA (Service Level Agreement)**: Contract with users
**SLO (Service Level Objective)**: Internal target
**SLI (Service Level Indicator)**: Actual measurement

#### Example SLOs for YouTube-like System
```
Availability SLO:
- 99.99% uptime (52 minutes downtime/year)
- Measured as: Successful requests / Total requests

Latency SLO:
- Video start time < 1 second (p95)
- API response time < 500ms (p99)
- Search results < 300ms (p95)

Throughput SLO:
- Support 10M concurrent viewers
- Handle 1M video uploads/day
- Process 100M comments/day

Durability SLO:
- 99.999999999% (11 nines) for video files
- Zero data loss for user data
```

#### Calculating Error Budget
```
SLO: 99.9% availability
Error Budget: 0.1% = 43.2 minutes/month

Week 1: 10 minutes downtime → 33.2 minutes left
Week 2: 5 minutes downtime → 28.2 minutes left
Week 3: 30 minutes downtime → BUDGET EXCEEDED!

Action: Freeze new features, focus on reliability
```

### 6. Dashboard Examples

#### Infrastructure Dashboard (Grafana)
```
Panels:
1. Request Rate (line chart)
   - Total RPS
   - RPS by service
   - RPS by endpoint

2. Error Rate (line chart)
   - 4xx rate
   - 5xx rate
   - By service

3. Latency (line chart)
   - p50, p95, p99
   - By endpoint

4. Resource Utilization (gauge)
   - CPU %
   - Memory %
   - Disk %

5. Active Users (counter)
   - Current concurrent users
   - Peak today

6. Database Metrics
   - Query time
   - Connection pool usage
   - Slow queries count
```

#### Business Dashboard
```
Panels:
1. Videos Uploaded (today, this week)
2. Total Views (real-time counter)
3. Active Subscriptions (premium users)
4. Revenue (today, this month)
5. Top 10 Trending Videos
6. Geographic Distribution (world map)
7. User Growth (line chart)
```

---

## Trade-offs & Design Decisions

Every architectural decision involves trade-offs. Understanding them helps you make informed choices.

### 1. SQL vs NoSQL

| Aspect | SQL (PostgreSQL, MySQL) | NoSQL (MongoDB, Cassandra) |
|--------|------------------------|---------------------------|
| **Data Model** | Structured tables with fixed schema | Flexible documents/key-value |
| **Relationships** | Strong support with JOINs | Limited, use denormalization |
| **ACID** | Full ACID guarantees | Eventual consistency (BASE) |
| **Scalability** | Vertical (scale up), harder horizontal | Easy horizontal scaling |
| **Query Flexibility** | Complex queries, aggregations | Limited query capabilities |
| **Schema Changes** | Migrations needed, can be slow | Flexible, schema-less |
| **Use Cases** | Financial transactions, user data | Logs, time-series, flexible data |

**When to use SQL:**
```
✅ Structured data with clear relationships
✅ Need ACID transactions (user payments)
✅ Complex queries and reporting
✅ Data integrity is critical

Examples in YouTube:
- User accounts and authentication
- Subscription management
- Payment transactions
```

**When to use NoSQL:**
```
✅ Massive scale (billions of records)
✅ Flexible schema (frequent changes)
✅ High write throughput
✅ Eventual consistency acceptable

Examples in YouTube:
- Video metadata (views, likes can be eventually consistent)
- Comments (high write volume)
- Watch history
- Analytics data
```

**Hybrid Approach (Polyglot Persistence):**
```
Use both:
- SQL for critical transactional data
- NoSQL for high-volume, flexible data

YouTube likely uses:
- MySQL for user accounts, subscriptions
- Cassandra for video metadata, analytics
- Redis for caching
```

### 2. Monolith vs Microservices

| Aspect | Monolith | Microservices |
|--------|----------|---------------|
| **Deployment** | Single unit, all-or-nothing | Independent services |
| **Development** | Simpler for small teams | Complex coordination needed |
| **Scaling** | Scale entire app | Scale individual services |
| **Technology** | Single tech stack | Different stack per service |
| **Testing** | Easier integration testing | Complex distributed testing |
| **Failure Impact** | Entire app goes down | Isolated failures |
| **Network** | In-process calls (fast) | Network calls (slower) |
| **Data** | Shared database | Separate databases |

**When to use Monolith:**
```
✅ Small team (< 10 developers)
✅ Early stage startup (MVP)
✅ Clear, stable requirements
✅ Limited scalability needs

Example:
Starting a YouTube clone:
- Begin with monolith
- Faster to develop and deploy
- Easier to change architecture
```

**When to use Microservices:**
```
✅ Large team (50+ developers)
✅ Different scaling needs per component
✅ Need independent deployment
✅ Proven product with stable APIs

Example:
YouTube at scale:
- Video Service (handles uploads)
- Transcoding Service (processes videos)
- Recommendation Service (ML-heavy)
- Comment Service (high write volume)
- Each scales independently
```

**Evolution Path:**
```
Stage 1: Monolith (0-100K users)
Stage 2: Monolith + Separate Services for high-load components
Stage 3: Full Microservices (1M+ users)

Don't over-engineer early!
```

### 3. Synchronous vs Asynchronous

| Aspect | Synchronous | Asynchronous |
|--------|-------------|--------------|
| **Response** | Immediate response | Deferred response |
| **Coupling** | Tight coupling | Loose coupling |
| **Failure Handling** | Immediate failure | Retry mechanisms |
| **Performance** | Blocked waiting | Non-blocking |
| **Complexity** | Simpler to understand | More complex |
| **Use Case** | Real-time operations | Background jobs |

**Synchronous Example:**
```python
# User uploads video - waits for response
@app.post("/upload")
def upload_video(video):
    # This takes 2 seconds
    s3_url = upload_to_s3(video)

    # User waits here
    return {"status": "success", "url": s3_url}

Pros: Simple, immediate feedback
Cons: Slow for user, ties up server resources
```

**Asynchronous Example:**
```python
# User uploads video - immediate response
@app.post("/upload")
def upload_video(video):
    # Queue the job
    job_id = queue.enqueue("upload_task", video)

    # Return immediately
    return {"status": "processing", "job_id": job_id}

# Background worker processes it
def upload_task(video):
    s3_url = upload_to_s3(video)
    db.update(video_id, url=s3_url)
    notify_user("Upload complete")

Pros: Fast response, scalable
Cons: More complex, need job tracking
```

**Decision Guide:**
```
Use Synchronous for:
✅ Must complete immediately (user login)
✅ Simple, fast operations (< 100ms)
✅ Need immediate result (search query)

Use Asynchronous for:
✅ Long-running tasks (video transcoding)
✅ High-volume operations (millions of events)
✅ Non-critical path (sending emails)
✅ Can be retried (analytics processing)
```

### 4. Vertical vs Horizontal Scaling

| Aspect | Vertical (Scale Up) | Horizontal (Scale Out) |
|--------|---------------------|------------------------|
| **Method** | Add more resources to one machine | Add more machines |
| **Cost** | Expensive at high end | Linear cost growth |
| **Limit** | Hardware limit (largest instance) | Virtually unlimited |
| **Complexity** | Simple, no code changes | Requires load balancing |
| **Downtime** | Requires restart | Can add without downtime |
| **Database** | Easier (single instance) | Needs sharding/replication |

**Vertical Scaling Example:**
```
Start: t3.medium (2 vCPU, 4GB RAM) - $0.04/hr
→ t3.xlarge (4 vCPU, 16GB RAM) - $0.17/hr
→ t3.2xlarge (8 vCPU, 32GB RAM) - $0.33/hr
→ ... eventually hit limit

Pros:
- Simple, no code changes
- Good for databases

Cons:
- Expensive at high end
- Single point of failure
- Hardware limit
```

**Horizontal Scaling Example:**
```
Start: 3 x t3.medium
Need more capacity?
→ Add 3 more t3.medium instances

Pros:
- Virtually unlimited scale
- High availability (if one fails, others continue)
- Linear cost

Cons:
- Need load balancer
- Stateless application required
- More complex
```

**Best Practice:**
```
Use Both!
- Vertical: Database (until you need sharding)
- Horizontal: Web servers, API servers
- Horizontal: Stateless services

YouTube approach:
- Web/API servers: Horizontal (thousands of instances)
- Cache: Horizontal (Redis cluster)
- Database: Vertical + Read Replicas (horizontal reads)
```

### 5. Strong Consistency vs Eventual Consistency

| Aspect | Strong Consistency | Eventual Consistency |
|--------|-------------------|---------------------|
| **Guarantee** | Immediate consistency | Eventually consistent |
| **Read** | Always latest data | May read stale data |
| **Performance** | Slower (coordination needed) | Faster (no coordination) |
| **Availability** | Lower (may block) | Higher (always available) |
| **Use Case** | Financial, critical data | Social media, analytics |

**Strong Consistency Example:**
```
Scenario: Bank transfer $100 from A to B

With Strong Consistency:
1. User requests transfer
2. Begin transaction
3. Deduct $100 from A
4. Add $100 to B
5. Commit transaction
6. Both A and B see updated balances immediately

If system fails at step 4:
→ Rollback, no money lost/duplicated

Needed for:
- Payment transactions
- Inventory management
- Booking systems (prevent double-booking)
```

**Eventual Consistency Example:**
```
Scenario: User likes a video

With Eventual Consistency:
1. User clicks like
2. Update local cache (count: 1000 → 1001)
3. Send async event to update database
4. Other users may see 1000 for a few seconds
5. Eventually all users see 1001

Pros:
- Fast response to user
- High availability
- Scales better

Acceptable for:
- Like/view counts (doesn't need to be exact)
- Comments (few seconds delay OK)
- Non-critical metrics
```

**CAP Theorem:**
```
You can have only 2 of 3:
- Consistency (all nodes see same data)
- Availability (system always responds)
- Partition Tolerance (works despite network issues)

Real-world:
- SQL databases: Choose C + A (sacrifice P)
- NoSQL (Cassandra): Choose A + P (sacrifice C)
- Most systems: Choose A + P with eventual C
```

### 6. CDN vs Origin Serving

| Aspect | CDN | Origin Serving |
|--------|-----|----------------|
| **Latency** | Low (geographically close) | High (single location) |
| **Bandwidth Cost** | Lower (cached at edge) | Higher (always from origin) |
| **Freshness** | May serve stale content | Always fresh |
| **Complexity** | More complex (cache invalidation) | Simple |
| **Scale** | Handles massive traffic | Limited by origin capacity |

**CDN Architecture:**
```
User in Tokyo requests video:
1. Request goes to Tokyo CDN edge
2. If cached → Serve immediately (fast!)
3. If not cached → Fetch from origin → Cache → Serve
4. Next user in Tokyo gets cached version

Benefits:
- 10-50ms latency vs 200ms from origin
- Offload 90%+ traffic from origin
- DDoS protection at edge

Cost:
Origin bandwidth: $0.09/GB
CDN bandwidth: $0.02/GB
(Prices vary by provider)
```

**When to use CDN:**
```
✅ Static content (images, videos, CSS, JS)
✅ Global user base
✅ High traffic volume
✅ Content doesn't change frequently

YouTube:
- All videos served through CDN
- Thumbnails through CDN
- Static assets through CDN
```

**When to serve from Origin:**
```
✅ Personalized content (can't be cached)
✅ Real-time data (stock prices)
✅ Content changes frequently
✅ Small user base (CDN not cost-effective)

YouTube:
- API responses (personalized)
- User-specific recommendations
- Real-time comments
```

### 7. Push vs Pull for Notifications

| Aspect | Push (WebSockets, SSE) | Pull (Polling) |
|--------|----------------------|----------------|
| **Real-time** | Yes, instant | Delayed (polling interval) |
| **Server Load** | Moderate (persistent connections) | High (frequent requests) |
| **Client Complexity** | More complex | Simple |
| **Firewall** | May be blocked | Usually works |
| **Scalability** | Requires special handling | Easier to scale |

**Push Example (WebSockets):**
```javascript
// Client establishes WebSocket connection
const socket = new WebSocket('wss://youtube.com/notifications');

socket.onmessage = (event) => {
  const notification = JSON.parse(event.data);
  showNotification(notification); // Instant!
};

// Server pushes notification
server.clients.forEach(client => {
  if (client.userId === targetUserId) {
    client.send(JSON.stringify({
      type: 'new_comment',
      video: 'abc123',
      comment: 'Great video!'
    }));
  }
});

Pros: Real-time, low latency
Cons: Complex, persistent connections (resource-intensive)
```

**Pull Example (Polling):**
```javascript
// Client polls every 10 seconds
setInterval(async () => {
  const response = await fetch('/api/notifications');
  const notifications = await response.json();

  if (notifications.length > 0) {
    showNotifications(notifications);
  }
}, 10000);

Pros: Simple, works everywhere
Cons: Waste bandwidth, delayed notifications
```

**Hybrid: Long Polling:**
```javascript
// Server holds request until notification available
async function longPoll() {
  const response = await fetch('/api/notifications/longpoll');
  const notifications = await response.json();

  showNotifications(notifications);
  longPoll(); // Repeat
}

Better than polling, but not true real-time
```

**Decision Guide:**
```
Use Push for:
✅ True real-time needs (chat, live streams)
✅ Mobile apps (push notifications)
✅ Few notifications (not overwhelming server)

Use Pull for:
✅ Periodic updates (every minute OK)
✅ Simple infrastructure
✅ Firewall-friendly environments

YouTube likely uses:
- Push notifications for mobile apps
- WebSockets for live chat
- Polling for notification bell (acceptable delay)
```

---

## Common Beginner Mistakes

Learn from these common pitfalls to build better systems.

### 1. Not Considering Scalability Upfront

❌ **Wrong Approach:**
```python
# Single database instance, no caching
def get_video(video_id):
    video = db.query(Video).get(video_id)
    return video

# Works for 100 users, dies at 10,000 users
```

✅ **Right Solution:**
```python
# Add caching layer
def get_video(video_id):
    # Check cache first
    cached = redis.get(f"video:{video_id}")
    if cached:
        return json.loads(cached)

    # Cache miss - query database
    video = db.query(Video).get(video_id)

    # Cache for future requests
    redis.setex(f"video:{video_id}", 3600, json.dumps(video))
    return video

# Add read replicas for database
# Use load balancer for web servers
# Plan for horizontal scaling
```

**Key Lessons:**
- Design for 10x your current scale
- Add caching early
- Use connection pooling
- Plan for horizontal scaling
- Load test before launch

### 2. Over-Engineering (Premature Optimization)

❌ **Wrong Approach:**
```
Day 1 with 0 users:
- Deploy Kubernetes cluster (50+ microservices)
- Set up Kafka, Redis, Cassandra, ElasticSearch
- Implement complex event sourcing
- Build custom service mesh

Result: 6 months of development, high complexity, no users
```

✅ **Right Solution:**
```
Day 1:
- Simple monolith on single server
- PostgreSQL database
- Deploy on Heroku/Railway
- Ship in 2 weeks
- Get user feedback

Day 30 (if traffic grows):
- Add Redis caching
- Separate database server
- Add load balancer

Day 180 (if still growing):
- Consider microservices for bottlenecks
- Add message queue
```

**Key Lessons:**
- Start simple, iterate based on actual needs
- Premature optimization is the root of all evil
- YAGNI (You Aren't Gonna Need It)
- Build for today's problems, not hypothetical ones

### 3. Not Planning for Failures

❌ **Wrong Approach:**
```python
# No error handling
def upload_video(video):
    s3.upload(video)  # What if S3 is down?
    db.insert(metadata)  # What if database fails?
    return "Success"

# No retries, no fallback
# Single point of failure
```

✅ **Right Solution:**
```python
from tenacity import retry, stop_after_attempt, wait_exponential

@retry(
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=1, min=2, max=10)
)
def upload_to_s3(video):
    try:
        return s3.upload(video)
    except S3Error as e:
        logger.error(f"S3 upload failed: {e}")
        raise  # Will retry

def upload_video(video):
    try:
        # Upload with retries
        s3_url = upload_to_s3(video)

        # Database with transaction
        with db.transaction():
            db.insert(metadata)

        return {"status": "success", "url": s3_url}

    except Exception as e:
        # Fallback: Queue for later processing
        queue.enqueue("retry_upload", video)
        return {"status": "queued", "message": "Processing in background"}

# Additional measures:
# - Multiple availability zones
# - Database replicas
# - Health checks and auto-restart
# - Circuit breaker pattern
```

**Key Lessons:**
- Always assume external services can fail
- Implement retry logic with exponential backoff
- Have fallback mechanisms
- Use circuit breakers
- Design for failure, not just success

### 4. Ignoring Consistency Requirements

❌ **Wrong Approach:**
```python
# Treating all data the same
# Using eventual consistency for financial transactions
def process_payment(user_id, amount):
    # Deduct from user balance (async)
    queue.publish("deduct_balance", user_id, amount)

    # Grant premium access (async)
    queue.publish("grant_premium", user_id)

    return "Payment processed"

# Problem: User might get premium before payment succeeds
# Or payment fails but they already have premium
```

✅ **Right Solution:**
```python
def process_payment(user_id, amount):
    # Use strong consistency for financial transactions
    with db.transaction():
        # Check balance
        user = db.query(User).with_for_update().get(user_id)

        if user.balance < amount:
            raise InsufficientFunds()

        # Deduct balance
        user.balance -= amount

        # Grant premium (atomic)
        user.premium = True
        user.premium_expires = datetime.now() + timedelta(days=30)

        # Create payment record
        payment = Payment(user_id=user_id, amount=amount)
        db.add(payment)

        # All or nothing
        db.commit()

    # Non-critical actions can be async
    queue.publish("send_receipt_email", user_id)

    return "Payment successful"
```

**Key Lessons:**
- Identify which data needs strong consistency
- Financial transactions: Always strong consistency
- Social features: Often eventual consistency is fine
- Use transactions for critical operations
- Understand the trade-offs

### 5. Not Estimating Capacity

❌ **Wrong Approach:**
```
"We'll just deploy on a t2.micro and scale later"

Capacity planning: None
Load testing: None
Monitoring: None

Launch day:
- Server crashes immediately
- Database overwhelmed
- Users can't access
- Reputation damaged
```

✅ **Right Solution:**
```
Capacity Planning (before launch):

Expected users: 10,000 daily
Peak traffic: 1,000 concurrent
Videos per day: 100 uploads
Storage: 100 videos × 1GB avg = 100GB/day

Calculations:
- API requests: 1,000 users × 100 req/user = 100K req
- Requests per second: 100K / 86400 = ~1.2 RPS (avg)
- Peak RPS (10x avg): 12 RPS
- Database queries: ~5 queries per request = 60 QPS
- Bandwidth: 1,000 concurrent × 5 Mbps = 5 Gbps

Infrastructure:
- 3x t3.medium app servers (handle 20 RPS each)
- 1x db.t3.large database (handle 100 QPS)
- CDN for video delivery
- 1TB storage (10 days buffer)

Load Testing:
- Simulate 2,000 concurrent users
- Test for 1 hour
- Verify <500ms latency
- Verify 0% error rate
```

**Key Lessons:**
- Always do capacity planning
- Estimate: Users → Requests → Resources
- Load test before launch
- Plan for 2-5x peak capacity
- Monitor from day one

### 6. Single Point of Failure

❌ **Wrong Approach:**
```
Architecture:
- Single application server
- Single database
- Single availability zone

If any component fails → Entire system down
```

✅ **Right Solution:**
```
High Availability Architecture:

Application Tier:
- 3+ servers across multiple AZs
- Load balancer (auto-removes unhealthy servers)
- Auto-scaling group (replaces failed servers)

Database Tier:
- Primary database in AZ-1
- Replica in AZ-2 (automatic failover)
- Replica in AZ-3 (additional read capacity)

Cache Tier:
- Redis cluster (multiple nodes)
- Automatic failover

Storage:
- S3 (99.999999999% durability)
- Cross-region replication

Network:
- Multiple load balancers
- Health checks every 10 seconds
- Auto-route traffic to healthy instances

Result: If any component fails, system continues
```

**Key Lessons:**
- Identify all single points of failure
- Add redundancy for critical components
- Use multiple availability zones
- Implement health checks
- Test failover scenarios
- Use managed services (they handle HA)

### 7. Not Caching Aggressively

❌ **Wrong Approach:**
```python
# Every request hits database
@app.get("/video/{video_id}")
def get_video(video_id):
    video = db.query(Video).get(video_id)  # Database query
    comments = db.query(Comment).filter_by(video_id=video_id).all()  # Another query
    creator = db.query(User).get(video.creator_id)  # Third query

    return {
        "video": video,
        "comments": comments,
        "creator": creator
    }

# Problem:
# - 3 database queries per request
# - Same video requested 1000 times = 3000 queries
# - Database becomes bottleneck
```

✅ **Right Solution:**
```python
@app.get("/video/{video_id}")
def get_video(video_id):
    cache_key = f"video:{video_id}:full"

    # Try cache first
    cached = redis.get(cache_key)
    if cached:
        return json.loads(cached)

    # Cache miss - query database
    video = db.query(Video).get(video_id)
    comments = db.query(Comment).filter_by(video_id=video_id).all()
    creator = db.query(User).get(video.creator_id)

    response = {
        "video": video,
        "comments": comments,
        "creator": creator
    }

    # Cache for 5 minutes
    redis.setex(cache_key, 300, json.dumps(response))

    return response

# Additional caching:
# - Browser cache (Cache-Control headers)
# - CDN cache (for video files)
# - Application cache (in-memory)
# - Database query cache

Result: 99% cache hit rate = 99% less database load
```

**Key Lessons:**
- Cache at multiple levels
- Cache frequently accessed data
- Set appropriate TTL (Time To Live)
- Have cache invalidation strategy
- Monitor cache hit rate (should be >90%)

### 8. Poor Database Indexing

❌ **Wrong Approach:**
```sql
-- No indexes on frequently queried columns
CREATE TABLE videos (
    id BIGINT PRIMARY KEY,
    creator_id BIGINT,
    title TEXT,
    created_at TIMESTAMP
);

-- Query without index (slow!)
SELECT * FROM videos
WHERE creator_id = 12345
ORDER BY created_at DESC;

-- Full table scan: O(n) time
-- With 10M videos: 5+ seconds
```

✅ **Right Solution:**
```sql
-- Add indexes on queried columns
CREATE TABLE videos (
    id BIGINT PRIMARY KEY,
    creator_id BIGINT,
    title TEXT,
    created_at TIMESTAMP,

    -- Composite index for common query
    INDEX idx_creator_created (creator_id, created_at DESC)
);

-- Same query now uses index (fast!)
SELECT * FROM videos
WHERE creator_id = 12345
ORDER BY created_at DESC;

-- Index lookup: O(log n) time
-- With 10M videos: 50ms
```

**Indexing Guidelines:**
```sql
✅ Index columns used in:
- WHERE clauses (creator_id)
- JOIN conditions (foreign keys)
- ORDER BY (created_at)
- Frequently searched (email, username)

❌ Don't index:
- Columns with low cardinality (boolean)
- Rarely queried columns
- Very long text fields
- Too many indexes (slows writes)

Common indexes:
-- User lookups
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- Video queries
CREATE INDEX idx_videos_creator ON videos(creator_id);
CREATE INDEX idx_videos_created ON videos(created_at DESC);
CREATE INDEX idx_videos_views ON videos(view_count DESC);

-- Comments
CREATE INDEX idx_comments_video ON comments(video_id, created_at DESC);

-- Full-text search
CREATE INDEX idx_videos_title_fulltext ON videos USING GIN(to_tsvector('english', title));
```

**Key Lessons:**
- Always index foreign keys
- Index columns in WHERE clauses
- Use composite indexes for multi-column queries
- Use EXPLAIN to analyze query performance
- Monitor slow queries
- Don't over-index (impacts write performance)

---

## Summary

Building a production-ready system requires careful attention to:

**Security:**
- Implement multiple layers of security
- Encrypt data at rest and in transit
- Validate all inputs
- Use strong authentication
- Plan for DDoS attacks

**Monitoring:**
- Track the four golden signals
- Implement centralized logging
- Use distributed tracing
- Set up meaningful alerts
- Define clear SLOs

**Trade-offs:**
- Understand there's no perfect solution
- Choose based on actual requirements
- Start simple, evolve as needed
- Optimize for your specific use case

**Avoid Mistakes:**
- Plan for scale from the start
- Don't over-engineer prematurely
- Design for failure
- Cache aggressively
- Eliminate single points of failure

Remember: The best architecture is the one that meets your current needs while allowing for future growth. Don't build for hypothetical scale—build for real requirements and iterate based on actual usage patterns.

---

**Next Steps:**
1. Review your current architecture against this checklist
2. Identify security gaps and address them
3. Set up basic monitoring (start with logs)
4. Document your trade-off decisions
5. Learn from production incidents
6. Continuously iterate and improve

Good luck building your system!
