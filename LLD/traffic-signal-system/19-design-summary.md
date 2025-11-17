# Traffic Signal System - One-Page Design Summary

> **Quick Reference**: This is a one-page summary of all design decisions. Perfect for interview review!

---

## 📋 SYSTEM OVERVIEW

**Problem**: Design a traffic signal system for a 4-way intersection with automatic cycling and manual override.

**Solution**: Object-oriented system using State Pattern, Observer Pattern, and thread-safe controller.

---

## 🎯 CORE REQUIREMENTS

| Type | Requirements |
|------|--------------|
| **Functional** | 4 directions (N,S,E,W), 3 states (RED/YELLOW/GREEN), automatic round-robin cycling, manual override, only one GREEN at a time |
| **Non-Functional** | Thread-safe, <100ms response, fail-safe (all RED on crash), embedded device compatible |
| **Constraints** | Pure Java, no external dependencies, <50MB memory, single intersection |

---

## 🏗️ ARCHITECTURE

```
┌─────────────────────────────────────────────┐
│           TrafficController                 │  ← Facade, coordinates all
│  - Manages 4 signals                        │
│  - Runs timer thread                        │
│  - Handles manual overrides                 │
└────────────────┬────────────────────────────┘
                 │
        ┌────────┴─────────┬──────────────┐
        ↓                  ↓              ↓
  TrafficSignal      SignalTimer    RoundRobinScheduler
  (State Pattern)    (Background     (Strategy Pattern)
                      Thread)
```

---

## 📊 KEY CLASSES

| Class | Responsibility | Pattern | LOC |
|-------|----------------|---------|-----|
| **TrafficController** | Coordinate all signals | Facade | ~300 |
| **TrafficSignal** | Manage single signal state | Context (State Pattern) | ~200 |
| **RedState/YellowState/GreenState** | State-specific behavior | State Pattern + Singleton | ~100 each |
| **SignalTimer** | Automatic timing | Thread management | ~150 |
| **StateTransitionValidator** | Validate transitions | Validator | ~50 |
| **RoundRobinScheduler** | Direction cycling | Strategy | ~50 |
| **StateChangeListener** | Observe state changes | Observer | ~100 |

**Total**: ~1,500 LOC

---

## 🎨 DESIGN PATTERNS

| Pattern | Where Used | Why | Benefit |
|---------|------------|-----|---------|
| **State** | TrafficLightState | Signal behavior depends on state | Easy to add new states |
| **Singleton** | RedState, YellowState, GreenState | Only need one instance | Memory efficient |
| **Observer** | StateChangeListener | Multiple components react to changes | Loose coupling |
| **Strategy** | RoundRobinScheduler | Swappable scheduling algorithms | Flexible |
| **Facade** | TrafficController | Simplify complex subsystem | Easy to use |

---

## 🔒 THREAD SAFETY

**Mechanisms**:
1. **ReentrantLock** in TrafficController (fair, prevents starvation)
2. **Volatile flags** (isRunning, isPaused) for visibility
3. **Synchronized methods** for atomic operations
4. **ConcurrentHashMap** for signal storage
5. **Immutable events** (all fields final)

**Critical Sections**:
- `tick()` - decrement time, check expiry
- `manualOverride()` - change active direction
- `start()` / `stop()` - lifecycle operations

**Deadlock Prevention**:
- Single lock per controller (no nested locks)
- Always use try-finally for unlock
- Fair lock ordering (FIFO)

---

## ✅ SOLID PRINCIPLES

| Principle | Application | Example |
|-----------|-------------|---------|
| **S** Single Responsibility | Each class has one job | TrafficSignal manages state, Validator validates, Logger logs |
| **O** Open/Closed | Add new states without modifying existing | New FlashingYellowState just implements interface |
| **L** Liskov Substitution | All states interchangeable | Any TrafficLightState works where expected |
| **I** Interface Segregation | Small, focused interfaces | StateChangeListener, not giant interface |
| **D** Dependency Inversion | Depend on abstractions | Controller uses StateChangeListener, not concrete Logger |

---

## 🔄 STATE TRANSITIONS

```
Start → All RED → First GREEN

GREEN (60s) → YELLOW (5s) → RED (65s) → Next Direction GREEN

Manual Override:
  Current GREEN → YELLOW → RED → Target GREEN
  (Never skip YELLOW - safety!)
```

**Validation**: All transitions validated, invalid ones throw IllegalStateException

---

## 🎯 KEY OPERATIONS

| Operation | Time Complexity | Thread-Safe | Description |
|-----------|----------------|-------------|-------------|
| `start()` | O(D) | Yes | Initialize and start cycling |
| `stop()` | O(D) | Yes | Stop and set all to RED |
| `tick()` | O(1) | Yes | Decrement time, check expiry |
| `manualOverride()` | O(L) | Yes | Force direction to GREEN |
| `getSignalStatus()` | O(1) | Yes | Query current state |

D = directions (4), L = listeners (typically 2-5)

---

## 📈 SCALABILITY

**Current**: Single intersection, ~50MB RAM, <10% CPU

**Scaling Path**:
1. **10 intersections**: Linear scaling (10× resources)
2. **100 intersections**: Distributed architecture, central monitoring
3. **1000+ intersections**: Load balancer, API cluster, database replication

**Architecture for Scale**:
```
[Intersections] → [MQTT Broker] → [Load Balancer] → [API Servers] → [Database]
```

Each intersection runs independently (edge computing), syncs status to central system.

---

## 🧪 TESTING STRATEGY

1. **Unit Tests**: Each class independently (80% coverage)
2. **Integration Tests**: Full cycles, manual overrides
3. **Concurrency Tests**: Multiple threads, stress testing
4. **Safety Tests**: Never two GREEN, always valid transitions
5. **Performance Tests**: 24-hour continuous operation

---

## 🚨 ERROR HANDLING

**Fail-Safe**:
- On crash: All signals go RED
- Invalid transition: Throw IllegalStateException
- Null inputs: Throw IllegalArgumentException
- Lock timeout: Log warning, retry

**Logging**:
- All state transitions logged
- Manual overrides logged with reason
- Errors logged with stack trace

---

## 🎯 TRADE-OFFS

| Decision | Pros | Cons | Why Chosen |
|----------|------|------|------------|
| Synchronous design | Simple, predictable | Blocking calls | Meets requirements, easy to maintain |
| ReentrantLock | Fair, flexible | Manual unlock needed | Thread safety critical for infrastructure |
| Round-robin | Fair, simple | Not traffic-aware | Sufficient for single intersection |
| No persistence | Fast, simple | State lost on crash | Can add later if needed |
| State Pattern | Extensible, clean | More classes | Future-proofing for new states |

---

## 📚 FILE STRUCTURE

```
LLD/traffic-signal-system/
├── 01-requirements.md              ← Functional & non-functional requirements
├── 02-use-case-diagram.md          ← Actors and use cases
├── 03-step1-class-diagram.md       ← Basic enums
├── 04-step2-class-diagram.md       ← Core classes
├── 05-step3-state-pattern.md       ← State Pattern implementation
├── 06-step4-integration.md         ← Observer Pattern & validation
├── 07-step5-controller.md          ← Controller & timer
├── 08-final-class-diagram.md       ← Complete class diagram
├── 09-sequence-diagrams.md         ← Interaction flows
├── 10-component-diagram.md         ← Architecture & deployment
├── 11-concurrency.md               ← Thread safety details
├── 12-solid-principles.md          ← SOLID application
├── 13-design-patterns.md           ← Pattern catalog
├── 14-relationships.md             ← UML relationships
├── 15-interview-qa.md              ← 10+ interview questions
├── 16-common-mistakes.md           ← Pitfalls to avoid
├── 17-whiteboard-checklist.md      ← Interview process
├── 18-complete-implementation.md   ← Full working code
├── 19-design-summary.md            ← This file
└── 20-README.md                    ← Overview
```

---

## 🎯 INTERVIEW TALKING POINTS

**When discussing this design, emphasize**:
1. ✅ State Pattern for extensibility
2. ✅ Thread safety (ReentrantLock, volatile)
3. ✅ Observer Pattern for loose coupling
4. ✅ Validation prevents invalid transitions
5. ✅ Fail-safe design (all RED on error)
6. ✅ SOLID principles throughout
7. ✅ Scalable architecture
8. ✅ Comprehensive testing strategy

**Be ready to discuss**:
- Why State Pattern over switch-case?
- How to handle concurrent overrides?
- How to scale to 1000 intersections?
- What if adding pedestrian signals?
- How to test thread safety?

---

## 🎯 ONE-SENTENCE SUMMARY

**"An object-oriented traffic signal system using State Pattern for signal behavior, Observer Pattern for event notifications, and ReentrantLock for thread-safe coordination of four signals cycling through RED/YELLOW/GREEN states with manual override capability."**

---

## 📊 METRICS

- **Classes**: 14
- **Interfaces**: 2
- **Enums**: 2
- **Design Patterns**: 5
- **Lines of Code**: ~1,500
- **Test Coverage**: 80%+
- **Memory**: <50MB
- **Response Time**: <100ms
- **Uptime**: 99.99% target

---

**Perfect for**: Last-minute interview review, quick reference during discussion, explaining design to others.
