# Traffic Signal System - Requirements Analysis

> **For Beginners**: Requirements are like a shopping list before building something. They tell us WHAT we need to build, not HOW to build it. We split them into two types:
> - **Functional Requirements**: What the system DOES (features, actions)
> - **Non-Functional Requirements**: How the system PERFORMS (speed, reliability, quality)

---

## 1. Functional Requirements

> **Think of these as "User Stories"** - What can users/system do?

### 1.1 Core Traffic Signal Management

**FR-1: Multiple Direction Support**
- The system **MUST** support traffic signals for multiple directions (NORTH, SOUTH, EAST, WEST)
- Each direction operates independently with its own signal
- Minimum 4 directions, but should be extensible to add more (e.g., NORTH_EAST for complex intersections)

**FR-2: Traffic Light States**
- Each traffic signal **MUST** have three states:
  - **RED**: Stop (vehicles must not cross)
  - **YELLOW**: Prepare to stop (caution/warning state)
  - **GREEN**: Go (vehicles can cross)
- States must follow a valid sequence: GREEN → YELLOW → RED → GREEN (cyclic)
- Invalid transitions (e.g., RED → GREEN directly) should be prevented

**FR-3: Configurable Signal Durations**
- Each direction **MUST** have configurable duration for each state
- Example configuration:
  ```
  NORTH: GREEN=60s, YELLOW=5s, RED=65s
  SOUTH: GREEN=60s, YELLOW=5s, RED=65s
  EAST:  GREEN=45s, YELLOW=5s, RED=110s
  WEST:  GREEN=45s, YELLOW=5s, RED=110s
  ```
- Durations should be modifiable at runtime (future enhancement)
- Default durations should be provided if not configured

### 1.2 Automatic Signal Operations

**FR-4: Automatic Signal Cycling**
- The system **MUST** automatically cycle through signals in a round-robin fashion
- Round-robin means: NORTH → SOUTH → EAST → WEST → NORTH (repeat)
- Only ONE direction should be GREEN at any time (safety constraint)
- All other directions should be RED when one is GREEN
- Cycling should continue indefinitely until system shutdown

**FR-5: State Transition Rules**
- When a signal turns GREEN, it must:
  1. Stay GREEN for configured duration
  2. Transition to YELLOW for warning duration
  3. Transition to RED
  4. Move to next direction in sequence
- Transitions must be atomic (no partial state changes)

### 1.3 Manual Override Capability

**FR-6: Emergency Manual Override**
- System **MUST** allow manual override to set any specific direction to GREEN
- Use case: Emergency vehicles, VIP convoy, traffic jam management
- When manual override is triggered:
  1. Current signal should complete YELLOW transition (safety)
  2. Then switch to RED
  3. Target direction should turn GREEN immediately
  4. System should resume automatic cycling after override completes

**FR-7: Override Safety**
- Manual override must maintain safety constraints:
  - Cannot have multiple directions GREEN simultaneously
  - Must respect YELLOW warning period before changing direction
  - Should log all manual overrides for audit trail

### 1.4 System Monitoring & Observability

**FR-8: State Monitoring**
- System **MUST** provide current state of all signals
- Users should be able to query: "What is current state of NORTH signal?"
- Response format: `{direction: NORTH, state: GREEN, timeRemaining: 45s}`

**FR-9: Logging & Audit Trail**
- All state transitions must be logged with timestamp
- Manual overrides must be logged with reason/operator
- Log format: `[2025-11-17 10:30:15] NORTH: RED → GREEN (Automatic Cycle)`

---

## 2. Non-Functional Requirements

> **Think of these as "Quality Attributes"** - How well should it work?

### 2.1 Performance Requirements

**NFR-1: Response Time**
- State transitions **MUST** occur within **100ms** of timer expiry
- Manual override **MUST** be processed within **200ms**
- Query operations **MUST** respond within **50ms**

**NFR-2: Throughput**
- System **MUST** handle at least **1000 state queries per second**
- Support minimum **100 concurrent manual override requests** (queued)

**NFR-3: Resource Utilization**
- CPU usage **MUST NOT** exceed 10% during normal operation
- Memory footprint **MUST NOT** exceed 50MB for single intersection
- Should scale linearly: 10 intersections ≈ 500MB

### 2.2 Reliability Requirements

**NFR-4: Availability**
- System **MUST** be available **99.99%** of the time (critical infrastructure)
- Downtime allowance: ~52 minutes per year
- No single point of failure

**NFR-5: Fault Tolerance**
- If timer thread fails, system **MUST** fail-safe to ALL RED
- Automatic recovery mechanism within 10 seconds
- Persistent state recovery after crash

**NFR-6: Data Consistency**
- Signal states **MUST** be consistent across all observers
- No race conditions between timer and manual override
- Thread-safe state transitions

### 2.3 Concurrency Requirements

**NFR-7: Thread Safety**
- System **MUST** be thread-safe for concurrent access
- Multiple threads should be able to query state simultaneously
- Manual override from multiple sources must be serialized
- Use proper synchronization mechanisms (locks, atomic operations)

**NFR-8: Deadlock Prevention**
- System **MUST NOT** deadlock under any circumstances
- Lock acquisition order must be well-defined
- Timeout mechanisms for lock acquisition (fail-fast)

### 2.4 Extensibility Requirements

**NFR-9: Easy Addition of New Directions**
- Adding a new direction **MUST NOT** require changes to core logic
- Should be configuration-driven
- Example: Add NORTH_EAST without modifying TrafficController class

**NFR-10: Easy Addition of New States**
- System should support adding new states in future (e.g., FLASHING_YELLOW for pedestrian crossings)
- State pattern should make this straightforward

**NFR-11: Plugin Architecture for Rules**
- Future: Support custom transition rules (e.g., rush hour priority)
- Should use Strategy pattern for rule selection

### 2.5 Maintainability Requirements

**NFR-12: Code Quality**
- Code **MUST** follow SOLID principles
- Each class should have single responsibility
- Use design patterns appropriately (State, Observer, Singleton)
- Code coverage **MUST** be at least 80%

**NFR-13: Documentation**
- All public APIs must be Javadoc documented
- Design decisions must be documented
- Beginner-friendly comments for complex logic

**NFR-14: Testability**
- All components must be unit testable
- Mock-friendly design (dependency injection)
- Integration tests for end-to-end scenarios

### 2.6 Security Requirements

**NFR-15: Access Control**
- Manual override **MUST** require authentication
- Role-based access: Only ADMIN can trigger override
- Audit log of all override operations

**NFR-16: Input Validation**
- All inputs must be validated
- Invalid direction/state should throw IllegalArgumentException
- Duration values must be positive integers

### 2.7 Scalability Requirements

**NFR-17: Horizontal Scalability**
- Design should support multiple intersections
- Each intersection runs independently
- Future: Central monitoring dashboard for all intersections

---

## 3. Assumptions

> **Important**: These are things we assume to be true. If they change, we may need to redesign.

1. **Single Intersection Focus**: Current design focuses on ONE intersection. Multi-intersection coordination is future scope.

2. **No Pedestrian Signals**: We're not handling pedestrian crossing signals (walk/don't walk). They would require separate state management.

3. **No Sensor Integration**: We assume fixed durations. Dynamic adjustment based on traffic density (sensors) is not in scope.

4. **Timer Accuracy**: We assume system timer is accurate within ±100ms. We don't handle clock drift.

5. **No Network Failures**: For single intersection, we assume no network issues. Distributed system challenges are future scope.

6. **Configuration is Static**: Once system starts, durations are fixed. Runtime reconfiguration is future enhancement.

7. **JVM Environment**: System runs on JVM. We leverage Java concurrency utilities.

8. **No Hardware Integration**: We're building software model only. Integration with actual traffic light hardware is separate concern.

---

## 4. Constraints

> **Limitations** we must work within

1. **Technology Stack**: Must use Java (version 8 or higher)
2. **No External Dependencies**: Should work with Java standard library only (no Spring, no external frameworks)
3. **Memory Constraint**: Must work on embedded systems with 256MB RAM
4. **Startup Time**: System must be ready within 5 seconds of startup
5. **Language**: All code and comments in English

---

## 5. Success Criteria

> **How do we know we succeeded?**

1. ✅ All functional requirements implemented and tested
2. ✅ No race conditions or deadlocks in 24-hour stress test
3. ✅ Manual override works within 200ms in 99% of cases
4. ✅ Zero safety violations (no two directions GREEN simultaneously)
5. ✅ Code review passes with senior engineer approval
6. ✅ Beginner developer can understand and extend the code

---

## 6. Out of Scope (For Current Version)

> **What we're NOT building (yet)**

1. ❌ Multi-intersection coordination
2. ❌ Pedestrian crossing signals
3. ❌ Adaptive signal timing based on traffic density
4. ❌ Web UI for monitoring/control
5. ❌ Mobile app integration
6. ❌ Integration with city traffic management system
7. ❌ Machine learning for optimal timing
8. ❌ Weather-based adjustments
9. ❌ Emergency vehicle detection (automated)
10. ❌ Historical analytics and reporting

---

## 7. Glossary

> **Beginner-Friendly Definitions**

| Term | Definition | Example |
|------|------------|---------|
| **State** | Current condition of a signal | RED, YELLOW, GREEN |
| **Direction** | Road direction at intersection | NORTH, SOUTH, EAST, WEST |
| **Transition** | Changing from one state to another | GREEN → YELLOW |
| **Cycle** | Complete round of all directions | NORTH → SOUTH → EAST → WEST |
| **Duration** | How long a state lasts | GREEN for 60 seconds |
| **Override** | Manual control interrupting automatic | Emergency vehicle forcing GREEN |
| **Atomic** | Happens completely or not at all | State change can't be half-done |
| **Thread-Safe** | Works correctly with multiple threads | No data corruption from concurrent access |
| **Round-Robin** | Taking turns in circular order | Like dealing cards: player1, player2, player3, player1... |

---

**Next Steps**: Now that we have clear requirements, we'll create:
1. Use Case Diagram (visual representation of requirements)
2. Class Diagrams (step-by-step design)
3. Implementation (Java code)
