# Phase 8: Database & API Design

## Database Schema Design

### ER Diagram (Entity-Relationship)

```mermaid
erDiagram
    USERS ||--o{ ACCOUNTS : owns
    USERS ||--o{ LOANS : applies
    USERS ||--o{ FIXED_DEPOSITS : creates
    USERS {
        varchar user_id PK
        varchar email UK
        varchar password_hash
        varchar phone UK
        varchar name
        timestamp created_at
        timestamp last_login
        varchar kyc_status
        json metadata
    }

    ACCOUNTS ||--o{ TRANSACTIONS : from
    ACCOUNTS ||--o{ TRANSACTIONS : to
    ACCOUNTS ||--o{ BENEFICIARIES : has
    ACCOUNTS {
        varchar account_id PK
        varchar user_id FK
        varchar account_type
        decimal balance
        varchar currency
        varchar status
        timestamp created_at
        decimal min_balance
    }

    TRANSACTIONS {
        varchar transaction_id PK
        varchar from_account FK
        varchar to_account FK
        decimal amount
        varchar type
        varchar status
        varchar description
        timestamp created_at
        json metadata
    }

    BENEFICIARIES {
        varchar beneficiary_id PK
        varchar user_id FK
        varchar account_id FK
        varchar beneficiary_name
        varchar beneficiary_account
        varchar ifsc_code
        boolean verified
        timestamp added_at
    }

    LOANS ||--o{ LOAN_REPAYMENTS : has
    LOANS {
        varchar loan_id PK
        varchar user_id FK
        varchar loan_type
        decimal amount
        decimal interest_rate
        integer tenure_months
        varchar status
        timestamp applied_at
        timestamp approved_at
        decimal emi_amount
    }

    LOAN_REPAYMENTS {
        varchar repayment_id PK
        varchar loan_id FK
        decimal amount
        timestamp due_date
        timestamp paid_at
        varchar status
    }

    FIXED_DEPOSITS {
        varchar fd_id PK
        varchar user_id FK
        decimal amount
        decimal interest_rate
        integer tenure_months
        timestamp start_date
        timestamp maturity_date
        varchar status
        boolean auto_renew
    }
```

---

## Table Schemas (PostgreSQL)

### 1. users Table

```sql
CREATE TABLE users (
    user_id VARCHAR(16) PRIMARY KEY DEFAULT ('USR' || LPAD(nextval('user_seq')::TEXT, 12, '0')),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,  -- bcrypt hash
    phone VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    kyc_status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, VERIFIED, REJECTED
    kyc_documents JSONB,  -- {aadhaar: "s3://...", pan: "s3://..."}
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    last_login TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',  -- ACTIVE, FROZEN, CLOSED
    metadata JSONB,  -- {registration_ip: "...", referral_code: "..."}

    CONSTRAINT chk_email CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$'),
    CONSTRAINT chk_phone CHECK (phone ~* '^\+?[1-9]\d{1,14}$')
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_kyc_status ON users(kyc_status);
CREATE INDEX idx_users_created_at ON users(created_at);
```

**Sample Data**:
```sql
INSERT INTO users (email, password_hash, phone, name, date_of_birth) VALUES
('john.doe@example.com', '$2a$12$...', '+911234567890', 'John Doe', '1990-01-15'),
('jane.smith@example.com', '$2a$12$...', '+919876543210', 'Jane Smith', '1985-05-20');
```

---

### 2. accounts Table

```sql
CREATE TABLE accounts (
    account_id VARCHAR(16) PRIMARY KEY DEFAULT ('ACC' || LPAD(nextval('account_seq')::TEXT, 12, '0')),
    user_id VARCHAR(16) NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    account_type VARCHAR(20) NOT NULL,  -- SAVINGS, CURRENT, SALARY
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'INR',
    status VARCHAR(20) DEFAULT 'ACTIVE',  -- ACTIVE, FROZEN, CLOSED
    min_balance DECIMAL(10, 2) DEFAULT 1000.00,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT chk_balance CHECK (balance >= 0),
    CONSTRAINT chk_account_type CHECK (account_type IN ('SAVINGS', 'CURRENT', 'SALARY'))
);

-- Indexes
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_accounts_created_at ON accounts(created_at);
```

**Sample Data**:
```sql
INSERT INTO accounts (user_id, account_type, balance) VALUES
('USR000000000001', 'SAVINGS', 50000.00),
('USR000000000001', 'CURRENT', 100000.00),
('USR000000000002', 'SAVINGS', 75000.00);
```

---

### 3. transactions Table

```sql
CREATE TABLE transactions (
    transaction_id VARCHAR(20) PRIMARY KEY DEFAULT ('TXN' || LPAD(nextval('txn_seq')::TEXT, 16, '0')),
    from_account VARCHAR(16) REFERENCES accounts(account_id),
    to_account VARCHAR(16) REFERENCES accounts(account_id),
    amount DECIMAL(15, 2) NOT NULL,
    type VARCHAR(30) NOT NULL,  -- FUND_TRANSFER, BILL_PAYMENT, LOAN_DISBURSEMENT, etc.
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, SUCCESS, FAILED, BLOCKED
    description VARCHAR(255),
    reference_number VARCHAR(50),  -- For external transactions (RTGS/NEFT)
    created_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP,
    metadata JSONB,  -- {fee: 10.00, tax: 1.80, beneficiary_name: "..."}

    CONSTRAINT chk_amount CHECK (amount > 0),
    CONSTRAINT chk_different_accounts CHECK (from_account != to_account),
    CONSTRAINT chk_type CHECK (type IN (
        'FUND_TRANSFER', 'BILL_PAYMENT', 'LOAN_DISBURSEMENT',
        'LOAN_REPAYMENT', 'FD_DEPOSIT', 'FD_WITHDRAWAL'
    ))
);

-- Indexes (critical for performance!)
CREATE INDEX idx_txn_from_account ON transactions(from_account);
CREATE INDEX idx_txn_to_account ON transactions(to_account);
CREATE INDEX idx_txn_created_at ON transactions(created_at DESC);
CREATE INDEX idx_txn_status ON transactions(status);
CREATE INDEX idx_txn_type ON transactions(type);

-- Composite index for common query (account + date range)
CREATE INDEX idx_txn_account_date ON transactions(from_account, created_at DESC);

-- Partition by month (for large datasets)
CREATE TABLE transactions_2025_01 PARTITION OF transactions
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
```

**Sample Data**:
```sql
INSERT INTO transactions (from_account, to_account, amount, type, status, description) VALUES
('ACC000000000001', 'ACC000000000003', 5000.00, 'FUND_TRANSFER', 'SUCCESS', 'Monthly rent'),
('ACC000000000002', 'ACC000000000001', 1000.00, 'FUND_TRANSFER', 'SUCCESS', 'Loan repayment');
```

---

### 4. beneficiaries Table

```sql
CREATE TABLE beneficiaries (
    beneficiary_id VARCHAR(16) PRIMARY KEY DEFAULT ('BEN' || LPAD(nextval('ben_seq')::TEXT, 12, '0')),
    user_id VARCHAR(16) NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    account_id VARCHAR(16) NOT NULL REFERENCES accounts(account_id),
    beneficiary_name VARCHAR(100) NOT NULL,
    beneficiary_account VARCHAR(20) NOT NULL,
    beneficiary_bank VARCHAR(100),
    ifsc_code VARCHAR(11),
    verified BOOLEAN DEFAULT FALSE,
    added_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT uk_user_beneficiary UNIQUE (user_id, beneficiary_account)
);

CREATE INDEX idx_ben_user_id ON beneficiaries(user_id);
```

---

### 5. loans Table

```sql
CREATE TABLE loans (
    loan_id VARCHAR(16) PRIMARY KEY DEFAULT ('LOAN' || LPAD(nextval('loan_seq')::TEXT, 10, '0')),
    user_id VARCHAR(16) NOT NULL REFERENCES users(user_id),
    loan_type VARCHAR(20) NOT NULL,  -- PERSONAL, HOME, AUTO, EDUCATION
    amount DECIMAL(15, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,  -- 8.50 (represents 8.50%)
    tenure_months INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'APPLIED',  -- APPLIED, APPROVED, DISBURSED, CLOSED, REJECTED
    applied_at TIMESTAMP DEFAULT NOW(),
    approved_at TIMESTAMP,
    disbursed_at TIMESTAMP,
    emi_amount DECIMAL(10, 2),  -- Monthly EMI
    total_repayment DECIMAL(15, 2),  -- Principal + Interest
    metadata JSONB,  -- {documents: [...], credit_score: 750}

    CONSTRAINT chk_loan_amount CHECK (amount > 0),
    CONSTRAINT chk_interest_rate CHECK (interest_rate > 0 AND interest_rate < 30),
    CONSTRAINT chk_tenure CHECK (tenure_months BETWEEN 6 AND 360)  -- 6 months to 30 years
);

CREATE INDEX idx_loans_user_id ON loans(user_id);
CREATE INDEX idx_loans_status ON loans(status);
```

**EMI Calculation**:
```sql
-- Formula: EMI = [P × R × (1+R)^N] / [(1+R)^N - 1]
-- P = Principal, R = Monthly interest rate, N = Tenure in months

CREATE OR REPLACE FUNCTION calculate_emi(
    principal DECIMAL,
    annual_rate DECIMAL,
    tenure_months INTEGER
) RETURNS DECIMAL AS $$
DECLARE
    monthly_rate DECIMAL;
    emi DECIMAL;
BEGIN
    monthly_rate := annual_rate / 12 / 100;  -- Convert annual % to monthly decimal
    emi := (principal * monthly_rate * POWER(1 + monthly_rate, tenure_months))
           / (POWER(1 + monthly_rate, tenure_months) - 1);
    RETURN ROUND(emi, 2);
END;
$$ LANGUAGE plpgsql;

-- Example: ₹500,000 loan at 8.5% for 60 months
SELECT calculate_emi(500000, 8.5, 60);
-- Result: ₹10,253.22/month
```

---

### 6. loan_repayments Table

```sql
CREATE TABLE loan_repayments (
    repayment_id VARCHAR(20) PRIMARY KEY DEFAULT ('REP' || LPAD(nextval('rep_seq')::TEXT, 15, '0')),
    loan_id VARCHAR(16) NOT NULL REFERENCES loans(loan_id),
    emi_number INTEGER NOT NULL,  -- 1, 2, 3, ..., 60
    amount DECIMAL(10, 2) NOT NULL,
    principal_component DECIMAL(10, 2),
    interest_component DECIMAL(10, 2),
    due_date DATE NOT NULL,
    paid_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, PAID, OVERDUE
    transaction_id VARCHAR(20) REFERENCES transactions(transaction_id),

    CONSTRAINT uk_loan_emi UNIQUE (loan_id, emi_number)
);

CREATE INDEX idx_repay_loan_id ON loan_repayments(loan_id);
CREATE INDEX idx_repay_due_date ON loan_repayments(due_date);
CREATE INDEX idx_repay_status ON loan_repayments(status);
```

---

### 7. fixed_deposits Table

```sql
CREATE TABLE fixed_deposits (
    fd_id VARCHAR(16) PRIMARY KEY DEFAULT ('FD' || LPAD(nextval('fd_seq')::TEXT, 13, '0')),
    user_id VARCHAR(16) NOT NULL REFERENCES users(user_id),
    account_id VARCHAR(16) NOT NULL REFERENCES accounts(account_id),
    amount DECIMAL(15, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,  -- 6.50 (6.50% per annum)
    tenure_months INTEGER NOT NULL,
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    maturity_date DATE NOT NULL,
    maturity_amount DECIMAL(15, 2),  -- Principal + Interest
    status VARCHAR(20) DEFAULT 'ACTIVE',  -- ACTIVE, MATURED, BROKEN
    auto_renew BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT chk_fd_amount CHECK (amount >= 10000),  -- Minimum ₹10,000
    CONSTRAINT chk_fd_tenure CHECK (tenure_months BETWEEN 3 AND 120)  -- 3 months to 10 years
);

CREATE INDEX idx_fd_user_id ON fixed_deposits(user_id);
CREATE INDEX idx_fd_maturity_date ON fixed_deposits(maturity_date);
```

**Maturity Calculation** (Compound Interest):
```sql
CREATE OR REPLACE FUNCTION calculate_fd_maturity(
    principal DECIMAL,
    annual_rate DECIMAL,
    tenure_months INTEGER
) RETURNS DECIMAL AS $$
DECLARE
    rate_decimal DECIMAL;
    years DECIMAL;
    maturity DECIMAL;
BEGIN
    rate_decimal := annual_rate / 100;
    years := tenure_months / 12.0;
    maturity := principal * POWER(1 + rate_decimal / 4, 4 * years);  -- Quarterly compounding
    RETURN ROUND(maturity, 2);
END;
$$ LANGUAGE plpgsql;

-- Example: ₹100,000 FD at 6.5% for 12 months
SELECT calculate_fd_maturity(100000, 6.5, 12);
-- Result: ₹106,660.36
```

---

## RESTful API Design

### API Versioning & Base URL

```
Base URL: https://api.mybank.com/api/v1
```

---

### 1. Authentication APIs

#### Register User
```
POST /api/v1/auth/register

Request:
{
  "email": "john@example.com",
  "password": "SecurePass123!",
  "phone": "+911234567890",
  "name": "John Doe",
  "date_of_birth": "1990-01-15"
}

Response: 201 Created
{
  "user_id": "USR000000000001",
  "email": "john@example.com",
  "message": "Registration successful. Please verify email."
}

Errors:
- 400: Email/phone already exists
- 422: Invalid email format or weak password
```

#### Login
```
POST /api/v1/auth/login

Request:
{
  "email": "john@example.com",
  "password": "SecurePass123!"
}

Response: 200 OK
{
  "message": "OTP sent to +91XXXXXX7890",
  "session_id": "SES123456",
  "expires_in": 300  // 5 minutes to enter OTP
}

Errors:
- 401: Invalid credentials
- 429: Too many failed attempts (rate limited)
```

#### Verify OTP (MFA)
```
POST /api/v1/auth/verify-otp

Request:
{
  "session_id": "SES123456",
  "otp": "123456"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,  // 1 hour
  "user": {
    "user_id": "USR000000000001",
    "name": "John Doe",
    "email": "john@example.com"
  }
}

Errors:
- 401: Invalid OTP
- 410: OTP expired
```

---

### 2. Account APIs

#### Get All Accounts (for logged-in user)
```
GET /api/v1/accounts
Authorization: Bearer {token}

Response: 200 OK
{
  "accounts": [
    {
      "account_id": "ACC000000000001",
      "account_type": "SAVINGS",
      "balance": 50000.00,
      "currency": "INR",
      "status": "ACTIVE",
      "min_balance": 1000.00,
      "created_at": "2025-01-01T10:00:00Z"
    },
    {
      "account_id": "ACC000000000002",
      "account_type": "CURRENT",
      "balance": 100000.00,
      "currency": "INR",
      "status": "ACTIVE"
    }
  ],
  "total_balance": 150000.00
}
```

#### Get Account Balance
```
GET /api/v1/accounts/{account_id}/balance
Authorization: Bearer {token}

Response: 200 OK
{
  "account_id": "ACC000000000001",
  "balance": 50000.00,
  "available_balance": 49000.00,  // balance - min_balance
  "currency": "INR",
  "as_of": "2025-01-15T10:30:45Z"
}

Errors:
- 403: Not your account
- 404: Account not found
```

---

### 3. Transaction APIs

#### Get Transaction History
```
GET /api/v1/accounts/{account_id}/transactions?limit=50&offset=0&start_date=2025-01-01&end_date=2025-01-31
Authorization: Bearer {token}

Response: 200 OK
{
  "account_id": "ACC000000000001",
  "transactions": [
    {
      "transaction_id": "TXN0000000000000001",
      "type": "FUND_TRANSFER",
      "amount": -5000.00,  // Negative = debit
      "to_account": "ACC000000000003",
      "beneficiary_name": "Jane Smith",
      "description": "Monthly rent",
      "status": "SUCCESS",
      "created_at": "2025-01-15T10:30:00Z",
      "balance_after": 45000.00
    },
    {
      "transaction_id": "TXN0000000000000002",
      "type": "FUND_TRANSFER",
      "amount": 10000.00,  // Positive = credit
      "from_account": "ACC000000000005",
      "sender_name": "Employer Inc",
      "description": "Salary",
      "status": "SUCCESS",
      "created_at": "2025-01-01T09:00:00Z",
      "balance_after": 50000.00
    }
  ],
  "total_count": 120,
  "page": 1,
  "limit": 50
}
```

#### Fund Transfer
```
POST /api/v1/transfers
Authorization: Bearer {token}

Request:
{
  "from_account": "ACC000000000001",
  "to_account": "ACC000000000003",
  "amount": 5000.00,
  "description": "Monthly rent",
  "otp": "123456"  // For transactions > ₹10,000
}

Response: 200 OK
{
  "transaction_id": "TXN0000000000000001",
  "status": "SUCCESS",
  "from_account": "ACC000000000001",
  "to_account": "ACC000000000003",
  "amount": 5000.00,
  "fee": 0.00,
  "balance_after": 45000.00,
  "created_at": "2025-01-15T10:30:00Z",
  "reference_number": "REF123456789"
}

Errors:
- 400: Insufficient balance
- 403: Daily limit exceeded (₹100,000/day)
- 422: Invalid account number
- 429: Too many requests (rate limited)
```

---

### 4. Beneficiary APIs

#### Add Beneficiary
```
POST /api/v1/beneficiaries
Authorization: Bearer {token}

Request:
{
  "account_id": "ACC000000000001",
  "beneficiary_name": "Jane Smith",
  "beneficiary_account": "ACC000000000003",
  "ifsc_code": "SBIN0001234"
}

Response: 201 Created
{
  "beneficiary_id": "BEN000000000001",
  "message": "Beneficiary added successfully",
  "verified": false,
  "verification_pending": "Small amount deposited for verification"
}
```

#### List Beneficiaries
```
GET /api/v1/beneficiaries
Authorization: Bearer {token}

Response: 200 OK
{
  "beneficiaries": [
    {
      "beneficiary_id": "BEN000000000001",
      "beneficiary_name": "Jane Smith",
      "beneficiary_account": "ACC000000000003",
      "ifsc_code": "SBIN0001234",
      "verified": true,
      "added_at": "2025-01-10T08:00:00Z"
    }
  ]
}
```

---

### 5. Loan APIs

#### Apply for Loan
```
POST /api/v1/loans
Authorization: Bearer {token}

Request:
{
  "loan_type": "PERSONAL",
  "amount": 500000.00,
  "tenure_months": 60,
  "purpose": "Home renovation",
  "documents": [
    "s3://bucket/loans/USR123/salary-slip.pdf",
    "s3://bucket/loans/USR123/bank-statement.pdf"
  ]
}

Response: 201 Created
{
  "loan_id": "LOAN0000000001",
  "status": "APPLIED",
  "estimated_emi": 10253.22,
  "interest_rate": 8.5,
  "message": "Loan application submitted. You will receive an update within 48 hours."
}
```

#### Get Loan Details
```
GET /api/v1/loans/{loan_id}
Authorization: Bearer {token}

Response: 200 OK
{
  "loan_id": "LOAN0000000001",
  "loan_type": "PERSONAL",
  "amount": 500000.00,
  "interest_rate": 8.5,
  "tenure_months": 60,
  "emi_amount": 10253.22,
  "status": "APPROVED",
  "disbursed_at": "2025-01-10T10:00:00Z",
  "repayment_schedule": [
    {
      "emi_number": 1,
      "due_date": "2025-02-10",
      "amount": 10253.22,
      "principal": 6711.55,
      "interest": 3541.67,
      "status": "PENDING"
    },
    {
      "emi_number": 2,
      "due_date": "2025-03-10",
      "amount": 10253.22,
      "principal": 6759.05,
      "interest": 3494.17,
      "status": "PENDING"
    }
    // ... 58 more
  ]
}
```

---

### 6. Fixed Deposit APIs

#### Create FD
```
POST /api/v1/fixed-deposits
Authorization: Bearer {token}

Request:
{
  "account_id": "ACC000000000001",
  "amount": 100000.00,
  "tenure_months": 12,
  "auto_renew": false
}

Response: 201 Created
{
  "fd_id": "FD0000000000001",
  "amount": 100000.00,
  "interest_rate": 6.5,
  "tenure_months": 12,
  "start_date": "2025-01-15",
  "maturity_date": "2026-01-15",
  "maturity_amount": 106660.36,
  "message": "Fixed Deposit created successfully"
}
```

---

## API Best Practices

### 1. Pagination
```
GET /api/v1/transactions?limit=50&offset=0

Response headers:
X-Total-Count: 1200
X-Page: 1
X-Limit: 50
Link: <https://api.mybank.com/api/v1/transactions?limit=50&offset=50>; rel="next"
```

### 2. Rate Limiting
```
Response headers:
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1673784600  // Unix timestamp

Error (429 Too Many Requests):
{
  "error": "RATE_LIMIT_EXCEEDED",
  "message": "Too many requests. Retry after 60 seconds.",
  "retry_after": 60
}
```

### 3. Error Responses
```json
{
  "error": "INSUFFICIENT_BALANCE",
  "message": "Your account balance (₹5,000) is insufficient for this transfer (₹10,000).",
  "code": "ERR_4001",
  "timestamp": "2025-01-15T10:30:45Z",
  "request_id": "REQ123456789"
}
```

---

**Next**: Phase 9 - Scalability, Reliability & Monitoring! 🚀
