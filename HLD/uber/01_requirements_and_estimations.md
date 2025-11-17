# Uber - Requirements and Capacity Estimations

## Table of Contents
1. [Functional Requirements](#functional-requirements)
2. [Non-Functional Requirements](#non-functional-requirements)
3. [Traffic Estimates](#traffic-estimates)
4. [Storage Estimates](#storage-estimates)
5. [Bandwidth Estimates](#bandwidth-estimates)
6. [Assumptions](#assumptions)

---

## 1. Functional Requirements

### 1.1 Rider Features (User-Facing)
- **Ride Request**: Riders should be able to input pickup and destination locations
- **ETA & Fare Estimation**: System shows estimated time of arrival and fare before booking
- **Driver Matching**: System matches riders with nearby available drivers
- **Real-time Tracking**: Riders can track driver's location in real-time
- **Ride History**: Users can view past rides
- **Ratings & Reviews**: Both riders and drivers can rate each other (1-5 stars)
- **Payment Processing**: Multiple payment methods (card, wallet, cash)
- **Ride Cancellation**: Users can cancel rides with cancellation policy

### 1.2 Driver Features
- **Accept/Decline Rides**: Drivers receive ride requests and can accept or decline
- **Navigation**: Turn-by-turn navigation to pickup and destination
- **Earnings Dashboard**: Drivers can view earnings, ratings, and trip history
- **Status Management**: Drivers can go online/offline
- **Location Updates**: Driver's location is sent to the system every 3-5 seconds

### 1.3 Admin/System Features
- **Surge Pricing**: Dynamic pricing based on demand-supply ratio
- **Driver Management**: Onboarding, verification, background checks
- **Analytics**: Real-time dashboards for business metrics
- **Fraud Detection**: Identify suspicious activities
- **Support System**: Handle customer complaints and issues

---

## 2. Non-Functional Requirements

### 2.1 Scalability
- **Global Scale**: Support 100+ million users worldwide
- **Daily Active Users (DAU)**: 20 million riders, 5 million drivers
- **Peak Load**: 3x normal traffic during rush hours and events
- **Geographic Distribution**: Operate in 500+ cities across 50+ countries

### 2.2 Availability
- **Target Uptime**: 99.99% availability (52 minutes downtime per year)
- **Reason**: Ride-hailing is a critical service; downtime = lost revenue
- **Multi-region deployment**: For disaster recovery

### 2.3 Performance (Critical for User Experience)
- **Driver Matching**: < 5 seconds to find and match a driver
- **Location Updates**: Real-time updates every 3-5 seconds
- **API Latency**: < 200ms for most API calls
- **ETA Calculation**: < 1 second
- **Map Rendering**: < 2 seconds

### 2.4 Consistency
- **Payment**: Strong consistency (ACID transactions required)
- **Location Data**: Eventual consistency acceptable (3-5 second delay OK)
- **Ride Status**: Strong consistency (only one driver can accept a ride)
- **Surge Pricing**: Eventual consistency (updates every 1-2 minutes)

### 2.5 Reliability
- **Data Durability**: 99.999999999% (11 nines) for payment and ride data
- **GPS Accuracy**: Within 10-20 meters
- **Failover**: Automatic failover to backup systems < 30 seconds

### 2.6 Security
- **Data Encryption**: TLS for data in transit, AES-256 for data at rest
- **Authentication**: OAuth 2.0, JWT tokens, Two-factor authentication
- **Privacy**: GDPR compliant, anonymize user data, secure payment processing (PCI DSS)
- **Rate Limiting**: Prevent API abuse (1000 requests/minute per user)

---

## 3. Traffic Estimates

### 3.1 User Base
```
Total Registered Users: 100 million
Daily Active Riders (DAU): 20 million
Daily Active Drivers: 5 million
Peak Active Users (rush hour): 60 million
```

### 3.2 Ride Statistics
```
Average Rides per Day: 20 million rides
Average Ride Duration: 20 minutes
Rides per Second (average): 20M / 86400 = 231 rides/sec
Rides per Second (peak): 231 × 3 = 693 rides/sec
```

### 3.3 Location Update Traffic
**Driver Location Updates:**
```
Active drivers at any time: 5 million
Update frequency: Every 4 seconds
Location updates/sec = 5M / 4 = 1.25 million updates/sec
```

**Rider Location Updates (during active ride):**
```
Active rides at any time:
  Average ride duration = 20 minutes
  Rides/day = 20M
  Concurrent rides = (20M × 20 min) / (24 × 60 min) = 277,777 rides

Rider updates/sec = 277,777 / 4 = 69,444 updates/sec

Total location updates/sec = 1.25M + 69K ≈ 1.32 million/sec
```

### 3.4 API Request Traffic
```
Ride Requests:
- Search for ride: 231 req/sec
- Get ETA/Fare: 231 req/sec
- Book ride: 231 req/sec
- Track ride (polling every 5 sec): 277,777 / 5 = 55,555 req/sec
- Complete ride: 231 req/sec

Driver Operations:
- Receive ride requests: 231 req/sec
- Accept/Decline: 231 req/sec
- Update status: 1000 req/sec

Total API Requests/sec (average): ~60,000 req/sec
Total API Requests/sec (peak 3x): ~180,000 req/sec
```

### 3.5 Read vs Write Ratio
```
Reads (location tracking, ETA, map data): 85%
Writes (location updates, bookings, payments): 15%

Read requests: 180K × 0.85 = 153K req/sec (peak)
Write requests: 180K × 0.15 = 27K req/sec (peak)
```

---

## 4. Storage Estimates

### 4.1 User Data
```
User Profile:
- User ID: 8 bytes
- Name, email, phone: 200 bytes
- Profile photo URL: 100 bytes
- Payment info: 100 bytes
- Total per user: ~500 bytes

Total users: 100 million
Storage = 100M × 500 bytes = 50 GB
```

### 4.2 Driver Data
```
Driver Profile:
- Driver ID: 8 bytes
- Name, license, vehicle info: 500 bytes
- Documents (stored as URLs): 200 bytes
- Total per driver: ~700 bytes

Total drivers: 10 million
Storage = 10M × 700 bytes = 7 GB
```

### 4.3 Ride Data
```
Per Ride Record:
- Ride ID: 8 bytes
- Rider ID, Driver ID: 16 bytes
- Pickup/drop-off location (lat, long): 32 bytes
- Timestamps (start, end): 16 bytes
- Fare, distance, duration: 24 bytes
- Status, payment method: 20 bytes
- Route polyline (compressed): 500 bytes
- Total per ride: ~600 bytes

Daily rides: 20 million
Daily storage = 20M × 600 bytes = 12 GB/day
Yearly storage = 12 GB × 365 = 4.38 TB/year
5-year storage = 4.38 TB × 5 = 21.9 TB
```

### 4.4 Location History Data
```
Location Update:
- Driver/Rider ID: 8 bytes
- Latitude, Longitude: 16 bytes
- Timestamp: 8 bytes
- Total: 32 bytes

Location updates/day:
- 1.32M updates/sec × 86400 sec = 114 billion updates/day
- Daily storage = 114B × 32 bytes = 3.65 TB/day

Retention policy: Keep 30 days
Total location storage = 3.65 TB × 30 = 109.5 TB
```

### 4.5 Total Storage Summary
```
User data: 50 GB
Driver data: 7 GB
Ride data (5 years): 21.9 TB
Location history (30 days): 109.5 TB
Analytics & logs: 50 TB
Backup & replication (3x): (131.5 TB) × 3 = 394.5 TB

Total Storage Needed: ~400 TB
With 50% buffer: ~600 TB
```

---

## 5. Bandwidth Estimates

### 5.1 Incoming Bandwidth
```
Location Updates:
- 1.32M updates/sec × 32 bytes = 42.2 MB/sec = 337 Mbps

API Requests (average payload 1 KB):
- 60K req/sec × 1 KB = 60 MB/sec = 480 Mbps

Total Incoming (peak 3x): (337 + 480) × 3 = 2.45 Gbps
```

### 5.2 Outgoing Bandwidth
```
Map Tiles & Static Assets:
- 20M users × 10 map requests/day × 50 KB = 10 TB/day
- 10 TB / 86400 sec = 115 MB/sec = 920 Mbps

Location Updates to Riders:
- 277K active rides × updates every 5 sec
- 55K updates/sec × 100 bytes = 5.5 MB/sec = 44 Mbps

API Responses:
- 60K req/sec × 2 KB = 120 MB/sec = 960 Mbps

Total Outgoing (peak 3x): (920 + 44 + 960) × 3 = 5.77 Gbps
```

### 5.3 Total Bandwidth
```
Peak Total = 2.45 Gbps (in) + 5.77 Gbps (out) = 8.22 Gbps
Monthly bandwidth = 8.22 Gbps × 86400 sec × 30 days / 8 = 2.67 PB/month
```

---

## 6. Assumptions

### 6.1 Traffic Patterns
- Peak traffic is 3x average (rush hours: 7-9 AM, 5-8 PM)
- 80% of traffic is concentrated in 20% of cities (major metros)
- Weekend traffic is 1.5x weekday traffic in entertainment areas
- New Year's Eve, holidays can see 5x spikes

### 6.2 Data Patterns
- 70% of rides are short trips (< 5 km)
- Average ride: 20 minutes, 8 km
- Cancellation rate: 5%
- Driver acceptance rate: 80% (first driver contacted)

### 6.3 Geographic Distribution
- Users are geo-distributed (city-based sharding)
- Most queries are localized (within 5-10 km radius)
- Cross-region traffic < 5%

### 6.4 Growth Rate
- User growth: 20% year-over-year
- Ride volume growth: 25% year-over-year
- Storage needs double every 2 years

### 6.5 Performance
- 95th percentile latency targets: < 500ms
- 99th percentile latency: < 1 second
- Acceptable data staleness for location: 5 seconds

### 6.6 Cost Optimization
- CDN for static assets reduces bandwidth costs by 70%
- Caching reduces database load by 60%
- Location data older than 30 days moved to cold storage

---

## Summary: Key Numbers to Remember

| Metric | Value |
|--------|-------|
| Daily Active Users | 20M riders + 5M drivers |
| Rides per Second | 231 avg, 693 peak |
| Location Updates/sec | 1.32 million |
| API Requests/sec | 60K avg, 180K peak |
| Storage Required | 600 TB (with buffer) |
| Bandwidth | 8.22 Gbps peak |
| Read:Write Ratio | 85:15 |
| Availability Target | 99.99% |
| API Latency | < 200ms |

---

**Next Steps:**
Now that we understand the scale and requirements, we'll design the system architecture step by step, starting with a basic architecture and gradually adding complexity to meet these requirements.

**Key Insight for Beginners:**
Notice how we broke down the problem into numbers. In interviews, always start with these estimates - they guide your architectural decisions. For example, 1.32 million location updates/sec tells us we need a highly scalable write system, probably NoSQL or time-series database!
