# Phase 1: Requirements Analysis & Capacity Planning

## Table of Contents
1. [Functional Requirements](#functional-requirements)
2. [Non-Functional Requirements](#non-functional-requirements)
3. [Traffic Estimations](#traffic-estimations)
4. [Storage Estimations](#storage-estimations)
5. [Assumptions](#assumptions)

---

## Functional Requirements

### Priority 1: Core Banking Operations (Must Have)

**FR1: User Registration & Account Creation**
- Users can register with email/phone number
- KYC verification (identity documents upload)
- Create savings/current accounts
- Generate unique account numbers
- Initial deposit setup

**FR2: Login & Multi-Factor Authentication (MFA)**
- Email/phone + password authentication
- OTP-based MFA (SMS/Email/Authenticator App)
- Session management with timeout
- Device fingerprinting for security
- Password reset functionality

**FR3: View Account Details & Balance**
- Real-time account balance display
- Account holder information
- Account type and status
- Available vs. total balance
- Minimum balance requirements

**FR4: View Transaction History**
- Last 50 transactions quick view
- Filter by date range (last 7 days, 30 days, 90 days, custom)
- Search by transaction type (debit/credit)
- Export to PDF/CSV
- Transaction details (time, amount, beneficiary, reference)

**FR5: Fund Transfer**
- **Own Account Transfer**: Between user's own accounts (instant)
- **Same Bank Transfer**: To other accounts in same bank (IMPS/NEFT)
- **Other Bank Transfer**: RTGS/NEFT/UPI to external banks
- Add/manage beneficiaries
- Transaction limits (daily/per-transaction)
- Transfer confirmation with OTP

### Priority 2: Enhanced Features (Should Have)

**FR6: Schedule Recurring Transfers**
- Set up automatic payments (monthly rent, EMI)
- Choose frequency (daily/weekly/monthly/quarterly)
- Start and end date configuration
- Pause/resume recurring transfers
- Email notifications before each transfer

**FR7: Bill Payments**
- Utility bills (electricity, water, gas)
- Mobile/DTH recharge
- Credit card bill payments
- Integration with BBPS (Bharat Bill Payment System)
- Save billers for quick pay
- Bill payment reminders

**FR8: Loan Management**
- Apply for loans (personal, home, auto)
- Upload required documents
- View loan eligibility calculator
- Track application status
- View repayment schedule (EMI dates, amounts)
- Make prepayments
- Download loan statement

**FR9: Fixed Deposit & Investments**
- Open fixed deposits online
- Choose tenure (1 month to 10 years)
- Interest rate calculator
- Auto-renewal options
- Premature withdrawal with penalty calculation
- Investment in mutual funds (if applicable)

### Priority 3: Admin & Support Features (Could Have)

**FR10: Admin Functionalities**
- User management (view all users)
- Account freeze/unfreeze
- Transaction monitoring and fraud detection
- Generate reports (daily transactions, new accounts)
- Audit trail for all admin actions
- Role-based access control (admin, manager, support)

**FR11: Notifications & Alerts**
- Transaction alerts (SMS/Email/Push)
- Low balance alerts
- Large transaction alerts (> configured threshold)
- Login from new device alerts
- Bill payment due reminders
- Fixed deposit maturity alerts

**FR12: External System Integrations**
- **Payment Gateways**: For credit/debit card payments
- **KYC Service**: Aadhaar/PAN verification API
- **NPCI Integration**: For UPI/IMPS/NEFT
- **Credit Bureau**: CIBIL score check
- **SMS Gateway**: Twilio/AWS SNS for OTP
- **Email Service**: AWS SES/SendGrid

---

## Non-Functional Requirements

### NFR1: Performance
**Requirement**: System response time < 2 seconds for core operations

**Details**:
- View balance: < 500ms (p95)
- Fund transfer: < 1.5s (p95)
- Transaction history (last 50): < 1s (p95)
- Login with MFA: < 2s (p95)

**Why it Matters**: Banking users expect instant responses. Slow systems lead to poor user experience and trust issues.

**How to Achieve**:
- Database indexing on frequently queried columns
- Redis caching for balance and recent transactions
- Connection pooling for database
- CDN for static assets

---

### NFR2: Availability
**Requirement**: System availability ≥ 99.99% (Four Nines)

**What does 99.99% mean?**
- **Downtime allowed per year**: 52.56 minutes
- **Downtime allowed per month**: 4.38 minutes
- **Downtime allowed per day**: 8.64 seconds

**Calculation**:
```
Availability = (Uptime / Total Time) × 100
99.99% = (525,600 - 52.56) / 525,600 × 100

For comparison:
- 99.9% (Three Nines) = 8.76 hours downtime/year
- 99.99% (Four Nines) = 52.56 minutes downtime/year ✓ (Target)
- 99.999% (Five Nines) = 5.26 minutes downtime/year
```

**How to Achieve**:
- Multi-AZ deployment (at least 2 availability zones)
- Database master-slave replication with auto-failover
- Load balancer health checks
- Redundant application servers
- Automated monitoring and alerting

---

### NFR3: Scalability
**Requirement**: Support for 10,000 concurrent users

**Details**:
- **Concurrent Users**: 10,000 users actively using the system
- **Total Registered Users**: 1,000,000 (1 million)
- **Peak Concurrent Users**: 15,000 (during salary credit days: 1st-5th of month)

**Why 10,000 concurrent users?**
```
Assumption: 1 million registered users
Active users per day (DAU): 20% = 200,000 users
Peak hour traffic: 15% of DAU = 30,000 users
Concurrent users during peak: ~33% = 10,000 users
```

**How to Achieve**:
- Horizontal scaling (auto-scaling groups)
- Stateless application servers
- Load balancing across multiple servers
- Database read replicas

---

### NFR4: Security - Data Encryption
**Requirement**: Data encrypted in transit and at rest (AES-256/TLS 1.2+)

**Encryption Standards**:
- **In Transit**: TLS 1.2 or higher (HTTPS only)
- **At Rest**: AES-256 encryption for database
- **Passwords**: bcrypt hashing (work factor: 12)
- **Sensitive Data**: PII encrypted with application-level encryption

**Example (Beginner-Friendly)**:
```
Regular Data: "John's balance is $5000"
Encrypted Data: "8f7a3bc2d9e1f4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9"

When hacker gets database:
- Without encryption: "John's balance is $5000" ❌ (Hacker can read)
- With encryption: "8f7a3bc2..." ✓ (Hacker cannot read without key)
```

**Compliance**: PCI-DSS, GDPR, RBI guidelines

---

### NFR5: Audit & Compliance
**Requirement**: Audit logs retained for minimum 7 years

**What to Log**:
- All transactions (time, user, amount, beneficiary)
- Login attempts (successful/failed)
- Account modifications
- Admin actions
- API calls with request/response

**Log Format** (JSON):
```json
{
  "timestamp": "2025-01-15T10:30:45.123Z",
  "user_id": "USER123456",
  "action": "FUND_TRANSFER",
  "amount": 5000.00,
  "from_account": "ACC789012",
  "to_account": "ACC345678",
  "status": "SUCCESS",
  "ip_address": "192.168.1.1",
  "session_id": "SES987654"
}
```

**Storage Requirements**:
- Average log size: 500 bytes
- Logs per day: 5 million transactions × 500 bytes = 2.5 GB/day
- 7 years storage: 2.5 GB × 365 × 7 = 6.4 TB
- Use AWS S3 Glacier for cost-effective long-term storage

---

### NFR6: Disaster Recovery
**Requirement**: RTO = 1 hour, RPO = 5 minutes

**Beginner Explanation**:
- **RTO (Recovery Time Objective)**: How long can the system be down?
  - Target: 1 hour (system must be back online within 1 hour of failure)

- **RPO (Recovery Point Objective)**: How much data can we lose?
  - Target: 5 minutes (maximum 5 minutes of data loss)

**Real-World Scenario**:
```
Disaster happens at 10:00 AM:
- With RTO = 1 hour: System must be restored by 11:00 AM
- With RPO = 5 minutes: Data up to 9:55 AM must be recovered

What we might lose: Transactions between 9:55 AM - 10:00 AM
```

**How to Achieve**:
- Database snapshots every 5 minutes → AWS RDS automated backups
- Multi-region replication (Primary: us-east-1, Backup: us-west-2)
- Hot standby database (always running, ready to take over)
- Disaster recovery drills quarterly

---

### NFR7: Accessibility
**Requirement**: UI accessible on mobile (iOS/Android) and desktop with WCAG 2.1 compliance

**Platform Support**:
- **Web**: Chrome, Firefox, Safari, Edge (latest 2 versions)
- **Mobile Web**: Responsive design (320px - 1920px)
- **Mobile Apps**: iOS 14+, Android 10+

**WCAG 2.1 Level AA Compliance**:
- Screen reader compatible
- Keyboard navigation
- Sufficient color contrast (4.5:1 for text)
- Text resizing up to 200%

---

### NFR8: Idempotency
**Requirement**: Transactions must be idempotent (no duplicates)

**Beginner Explanation**:
```
Problem: User clicks "Transfer $100" button twice by accident
Without Idempotency: $200 transferred ❌
With Idempotency: $100 transferred (second click ignored) ✓

How it works:
1. User clicks transfer → Generate unique request ID: "REQ-ABC123"
2. User clicks again → Same request ID: "REQ-ABC123"
3. Server checks: "REQ-ABC123 already processed? Yes → Ignore"
```

**Implementation**:
- Generate unique transaction ID (UUID v4)
- Store processed transaction IDs in Redis (TTL: 24 hours)
- Return same response for duplicate requests

---

### NFR9: Input Validation & Security
**Requirement**: Validate all inputs to prevent SQLi/XSS/CSRF attacks

**Common Attacks & Prevention**:

**SQL Injection (SQLi)**:
```javascript
// ❌ Wrong (Vulnerable)
query = "SELECT * FROM users WHERE email = '" + userInput + "'";
// Attacker inputs: ' OR '1'='1
// Query becomes: SELECT * FROM users WHERE email = '' OR '1'='1'
// Returns all users! ❌

// ✓ Right (Safe)
query = "SELECT * FROM users WHERE email = ?"; // Parameterized query
db.execute(query, [userInput]); // Input is escaped
```

**Cross-Site Scripting (XSS)**:
```javascript
// ❌ Wrong
<div>Welcome, {userName}</div>
// Attacker inputs: <script>alert('Hacked!')</script>

// ✓ Right
<div>Welcome, {escapeHtml(userName)}</div>
// Output: Welcome, &lt;script&gt;alert('Hacked!')&lt;/script&gt;
```

**CSRF (Cross-Site Request Forgery)**:
- Use CSRF tokens for all state-changing operations
- Validate Origin/Referer headers

---

### NFR10: Horizontal Scalability
**Requirement**: Scale horizontally by adding nodes without code changes

**Vertical vs Horizontal Scaling**:

```
Vertical Scaling (Scale Up):
Server 1: 4 CPU, 8GB RAM → Upgrade to → 16 CPU, 64GB RAM
- Limited by hardware
- Single point of failure
- Expensive

Horizontal Scaling (Scale Out): ✓
Server 1: 4 CPU, 8GB RAM
Server 2: 4 CPU, 8GB RAM  } Add more servers
Server 3: 4 CPU, 8GB RAM  } based on load
- Unlimited scaling
- No single point of failure
- Cost-effective
```

**Requirements for Horizontal Scaling**:
- **Stateless Servers**: No session data stored on server (use Redis)
- **Shared Database**: All servers connect to same database
- **Load Balancer**: Distributes traffic evenly
- **Auto-Scaling**: Add/remove servers based on CPU/memory usage

---

## Traffic Estimations

### Given Assumptions
- **Total Registered Users**: 1,000,000 (1 million)
- **Daily Active Users (DAU)**: 20% = 200,000 users
- **Average Requests per User per Day**: 15 requests
  - Login: 1
  - View Balance: 5
  - View Transactions: 3
  - Fund Transfer: 2
  - Bill Payment: 1
  - Other: 3

---

### Beginner-Friendly Calculation: Requests Per Second

**Step 1: Total Requests Per Day**
```
Total Requests/Day = DAU × Requests per User
Total Requests/Day = 200,000 × 15
Total Requests/Day = 3,000,000 (3 million requests)
```

**Step 2: Average Requests Per Second**
```
Seconds in a day = 24 hours × 60 minutes × 60 seconds = 86,400 seconds

Average RPS = Total Requests/Day ÷ Seconds in a day
Average RPS = 3,000,000 ÷ 86,400
Average RPS = 34.7 ≈ 35 requests/second
```

**Step 3: Peak Requests Per Second**
```
Peak hours (9 AM - 11 AM, 6 PM - 8 PM): 4 hours = 33% of traffic
Peak traffic multiplier: 3x average

Peak RPS = Average RPS × 3
Peak RPS = 35 × 3
Peak RPS = 105 requests/second

On salary days (1st-5th of month), peak can be 5x:
Extreme Peak RPS = 35 × 5 = 175 requests/second
```

---

### Read vs Write Ratio

**Typical Banking Operations**:
- **Read Operations (80%)**:
  - View balance
  - View transaction history
  - Check loan status
  - View fixed deposits

- **Write Operations (20%)**:
  - Fund transfer
  - Bill payment
  - Loan application
  - Account updates

**Calculation**:
```
Total RPS = 105 (peak)

Read RPS = 105 × 80% = 84 requests/second
Write RPS = 105 × 20% = 21 requests/second
```

**Why This Matters**:
- Read-heavy → Use read replicas (scale reads independently)
- Write-heavy → Need database sharding
- Banking is read-heavy → Focus on caching balance and transactions

---

### Bandwidth Estimations

**Average Request/Response Sizes**:
- **Request**: 2 KB (headers + payload)
- **Response**: 5 KB (JSON data)
- **Total per Request**: 7 KB

**Bandwidth Calculation**:
```
Peak RPS = 105 requests/second
Data per request = 7 KB

Bandwidth = 105 × 7 KB = 735 KB/second
Bandwidth = 735 KB/s × 8 = 5.88 Mbps
```

**Daily Bandwidth**:
```
Total Requests/Day = 3,000,000
Data per request = 7 KB

Daily Bandwidth = 3,000,000 × 7 KB = 21 GB/day
Monthly Bandwidth = 21 GB × 30 = 630 GB/month
```

**Bandwidth Requirements**:
- **Application Servers**: 10 Mbps (with 2x buffer)
- **Database**: 5 Mbps
- **Total**: 15-20 Mbps

---

## Storage Estimations

### User Data Storage

**User Record Size**:
```json
{
  "user_id": "USR123456789012",          // 16 bytes
  "email": "user@example.com",           // 50 bytes
  "phone": "+911234567890",              // 20 bytes
  "name": "John Doe",                    // 50 bytes
  "kyc_documents": "s3://bucket/user/",  // 100 bytes (reference)
  "created_at": "2025-01-15T10:30:45Z",  // 25 bytes
  "metadata": {...}                      // 200 bytes (JSON)
}
```
**Total User Record Size**: ~500 bytes

**Calculation**:
```
Total Users = 1,000,000
User data per record = 500 bytes

Total User Data = 1,000,000 × 500 bytes
Total User Data = 500 MB
```

---

### Account Data Storage

**Assumptions**:
- Each user has an average of 1.5 accounts (savings + current)
- Total accounts = 1,000,000 × 1.5 = 1,500,000

**Account Record Size**:
```json
{
  "account_id": "ACC987654321098",       // 16 bytes
  "user_id": "USR123456789012",          // 16 bytes
  "account_type": "SAVINGS",             // 20 bytes
  "balance": 50000.00,                   // 8 bytes (DECIMAL)
  "currency": "INR",                     // 5 bytes
  "status": "ACTIVE",                    // 10 bytes
  "created_at": "2025-01-15T10:30:45Z",  // 25 bytes
  "metadata": {...}                      // 100 bytes
}
```
**Total Account Record Size**: ~200 bytes

**Calculation**:
```
Total Accounts = 1,500,000
Account data per record = 200 bytes

Total Account Data = 1,500,000 × 200 bytes
Total Account Data = 300 MB
```

---

### Transaction Data Storage (Most Critical)

**Transaction Volume**:
- 200,000 DAU × 2 transactions/day (average) = 400,000 transactions/day
- 400,000 × 365 days = 146,000,000 transactions/year (146 million)

**Transaction Record Size**:
```json
{
  "transaction_id": "TXN1234567890123456", // 20 bytes
  "from_account": "ACC987654321098",       // 16 bytes
  "to_account": "ACC123456789012",         // 16 bytes
  "amount": 5000.00,                       // 8 bytes
  "type": "FUND_TRANSFER",                 // 20 bytes
  "status": "SUCCESS",                     // 10 bytes
  "timestamp": "2025-01-15T10:30:45.123Z", // 30 bytes
  "description": "Monthly rent",           // 100 bytes
  "metadata": {...}                        // 180 bytes (fees, tax, etc.)
}
```
**Total Transaction Record Size**: ~400 bytes

**1 Year Storage**:
```
Transactions/Year = 146,000,000
Transaction size = 400 bytes

Storage (1 year) = 146,000,000 × 400 bytes
Storage (1 year) = 58.4 GB
```

**5 Year Storage**:
```
Storage (5 years) = 58.4 GB × 5
Storage (5 years) = 292 GB
```

---

### Loan & Fixed Deposit Storage

**Assumptions**:
- 10% users have loans = 100,000 loans
- 20% users have fixed deposits = 200,000 FDs

**Loan Record Size**: ~1 KB (includes repayment schedule)
**FD Record Size**: ~500 bytes

**Calculation**:
```
Loan Storage = 100,000 × 1 KB = 100 MB
FD Storage = 200,000 × 500 bytes = 100 MB
Total = 200 MB
```

---

### Document Storage (KYC, Statements)

**KYC Documents per User**:
- Aadhaar Card: 500 KB (scan)
- PAN Card: 300 KB
- Photo: 200 KB
- **Total per user**: ~1 MB

**Calculation**:
```
Total Users = 1,000,000
KYC docs per user = 1 MB

Total KYC Storage = 1,000,000 × 1 MB = 1 TB
```

**Bank Statements**:
- Monthly statements for 1 million users
- Average statement size: 500 KB (PDF)
- 1,000,000 × 500 KB × 12 months = 6 TB/year

**Total Document Storage (5 years)**:
```
KYC: 1 TB
Statements: 6 TB × 5 = 30 TB
Total: 31 TB
```

**Storage Strategy**: Use AWS S3 Standard (1 year) → Glacier (>1 year) for cost savings

---

### Total Storage Summary

| Data Type | 1 Year | 5 Years | Growth Rate |
|-----------|--------|---------|-------------|
| User Data | 500 MB | 1 GB | +20%/year (new users) |
| Account Data | 300 MB | 500 MB | +10%/year |
| Transactions | 58.4 GB | 292 GB | Linear growth |
| Loans & FDs | 200 MB | 400 MB | +15%/year |
| Documents (S3) | 7 TB | 31 TB | Linear growth |
| **Total Database** | **~60 GB** | **~295 GB** | — |
| **Total Object Storage** | **7 TB** | **31 TB** | — |

---

### Cache Storage (Redis)

**What to Cache**:
- User sessions: 10,000 concurrent users × 10 KB = 100 MB
- Account balances (hot data): 50,000 accounts × 200 bytes = 10 MB
- Recent transactions: 50,000 users × 50 transactions × 400 bytes = 1 GB
- API responses (TTL: 30s): 500 MB

**Total Redis Cache**: ~2 GB (provision 4 GB for safety)

---

## Assumptions

### Traffic & Usage
1. **Daily Active Users (DAU)**: 20% of total registered users
2. **Peak Hours**: 9-11 AM, 6-8 PM (33% of daily traffic)
3. **Read:Write Ratio**: 80:20 (banking is read-heavy)
4. **Average Session Duration**: 5 minutes
5. **Salary Days (1st-5th)**: 5x peak traffic

### User Behavior
1. **Average Requests per Session**: 15 requests
2. **Transactions per User per Day**: 2 (fund transfer, bill payment)
3. **Login Frequency**: Once per day
4. **Balance Checks**: 5 times per day
5. **Transaction History Views**: 3 times per day

### Data & Storage
1. **User Growth Rate**: 20% annually (1M → 1.2M in year 2)
2. **Average Accounts per User**: 1.5
3. **Document Retention**: 7 years (compliance)
4. **Transaction Retention**: Indefinite (archival after 2 years)

### Infrastructure
1. **Deployment Region**: Multi-AZ (AWS us-east-1)
2. **Disaster Recovery Region**: AWS us-west-2
3. **Database**: PostgreSQL (primary), Redis (cache)
4. **Object Storage**: AWS S3 (documents)
5. **Load Balancer**: AWS Application Load Balancer

### Security & Compliance
1. **Encryption**: TLS 1.2+, AES-256
2. **Password Policy**: Min 8 chars, uppercase, number, special char
3. **Session Timeout**: 15 minutes inactivity
4. **MFA**: Mandatory for transactions > $1000
5. **Compliance**: PCI-DSS, GDPR, RBI guidelines

### Business Constraints
1. **Budget**: $10,000/month infrastructure cost
2. **Team Size**: 5 backend, 3 frontend, 1 DevOps
3. **Go-Live Date**: 6 months from start
4. **MVP Features**: FR1-FR5 (core banking only)

---

## Next Steps

After understanding the requirements and capacity planning, we'll move to:
- **Phase 2**: Design the basic architecture (Client → LB → App → DB)
- **Phase 3**: Add caching layer for performance
- **Phase 4**: Scale the database with replication and sharding

**Learning Tip**: Always start with requirements and estimations before designing. This ensures your architecture can handle the expected load! 🎯
