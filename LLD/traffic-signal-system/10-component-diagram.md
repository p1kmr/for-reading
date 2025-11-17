# Component and Deployment Diagrams

> **For Beginners**: Component Diagrams show the BIG PICTURE - how different parts of the system fit together and how they're deployed. Think of it like a building blueprint showing rooms, floors, and how utilities connect!

---

## 🎯 WHAT ARE COMPONENT DIAGRAMS?

**Component Diagram**: Shows major components and their dependencies
**Deployment Diagram**: Shows hardware/infrastructure where system runs
**Data Model**: Shows how data is structured and persisted

---

## 📊 Component Diagram - Logical Architecture

```mermaid
graph TB
    subgraph "Presentation Layer"
        CLI[CLI Interface<br/>Command Line]
        WebUI[Web Dashboard<br/>Future]
        API[REST API<br/>Future]
    end

    subgraph "Application Layer"
        Controller[Traffic Controller<br/>Component]
        Timer[Signal Timer<br/>Component]
        Scheduler[Round Robin Scheduler<br/>Component]
    end

    subgraph "Domain Layer"
        Signal[Traffic Signal<br/>Component]
        State[State Pattern<br/>Component]
        Observer[Observer Pattern<br/>Component]
        Validator[Validation<br/>Component]
    end

    subgraph "Infrastructure Layer"
        Logger[Logging Service<br/>Component]
        Config[Configuration<br/>Component]
        Persistence[Data Persistence<br/>Future]
    end

    %% Dependencies
    CLI --> Controller
    WebUI -.-> API
    API -.-> Controller

    Controller --> Timer
    Controller --> Scheduler
    Controller --> Signal
    Controller --> Logger

    Signal --> State
    Signal --> Observer
    Signal --> Validator
    Signal --> Config

    Observer --> Logger
    State --> Config

    %% Styling
    style CLI fill:#e1f5fe,stroke:#01579b
    style WebUI fill:#f3e5f5,stroke:#4a148c
    style API fill:#fff3e0,stroke:#e65100
    style Controller fill:#ffeb3b,stroke:#f57f17,stroke-width:3px
    style Timer fill:#4caf50,stroke:#1b5e20
    style Scheduler fill:#2196f3,stroke:#0d47a1
    style Signal fill:#ff9800,stroke:#e65100
    style State fill:#e1bee7,stroke:#6a1b9a
    style Observer fill:#c8e6c9,stroke:#2e7d32
    style Validator fill:#ffccbc,stroke:#d84315
    style Logger fill:#b2dfdb,stroke:#00695c
    style Config fill:#fff9c4,stroke:#f57f17
    style Persistence fill:#cfd8dc,stroke:#455a64
```

**Layer Responsibilities**:

| Layer | Purpose | Components |
|-------|---------|------------|
| **Presentation** | User interaction | CLI, Web UI, API |
| **Application** | Business orchestration | Controller, Timer, Scheduler |
| **Domain** | Core business logic | Signal, State, Observer, Validator |
| **Infrastructure** | Technical services | Logger, Config, Persistence |

---

## 📊 Component Details

### 1. Traffic Controller Component

```mermaid
graph TB
    subgraph "Traffic Controller Component"
        Main[TrafficController<br/>Main Class]
        Lock[ReentrantLock<br/>Thread Safety]
        SignalMap[Map&lt;Direction, TrafficSignal&gt;<br/>Signal Registry]
    end

    subgraph "Provides Interfaces"
        IStart[start&#40;&#41;]
        IStop[stop&#40;&#41;]
        ITick[tick&#40;&#41;]
        IOverride[manualOverride&#40;&#41;]
        IQuery[getSignalStatus&#40;&#41;]
    end

    subgraph "Requires Interfaces"
        RTimer[Timer.start&#40;&#41;]
        RSignal[Signal.setState&#40;&#41;]
        RScheduler[Scheduler.getNext&#40;&#41;]
    end

    Main --> IStart
    Main --> IStop
    Main --> ITick
    Main --> IOverride
    Main --> IQuery

    Main ..> RTimer
    Main ..> RSignal
    Main ..> RScheduler

    style Main fill:#ffeb3b,stroke:#f57f17,stroke-width:3px
```

**Provides**: Control operations (start, stop, override, query)
**Requires**: Timer, Signal, Scheduler services
**Dependencies**: Thread-safe, requires locking

---

### 2. Traffic Signal Component

```mermaid
graph TB
    subgraph "Traffic Signal Component"
        SignalMain[TrafficSignal<br/>Main Class]
        ListenerList[List&lt;StateChangeListener&gt;<br/>Observer Registry]
    end

    subgraph "Provides Interfaces"
        ISetState[setState&#40;&#41;]
        ITransition[transitionToNextState&#40;&#41;]
        IQuery[getCurrentState&#40;&#41;]
        IListener[addListener&#40;&#41;/removeListener&#40;&#41;]
    end

    subgraph "Requires Interfaces"
        RState[State.handleState&#40;&#41;]
        RValidator[Validator.isValidTransition&#40;&#41;]
        RConfig[Config.getDuration&#40;&#41;]
        RObserver[Observer.onStateChange&#40;&#41;]
    end

    SignalMain --> ISetState
    SignalMain --> ITransition
    SignalMain --> IQuery
    SignalMain --> IListener

    SignalMain ..> RState
    SignalMain ..> RValidator
    SignalMain ..> RConfig
    SignalMain ..> RObserver

    style SignalMain fill:#ff9800,stroke:#e65100,stroke-width:3px
```

**Provides**: State management, observer registration
**Requires**: State objects, validator, config, observers
**Dependencies**: State pattern, observer pattern

---

## 📊 Deployment Diagram - Single Intersection

```mermaid
graph TB
    subgraph "Embedded Device (Raspberry Pi / Arduino)"
        subgraph "JVM Runtime"
            App[Traffic Signal<br/>Application<br/>JAR file]
            JDK[Java 8+ Runtime]
        end

        subgraph "Operating System"
            OS[Linux / Raspbian]
            Drivers[GPIO Drivers<br/>Hardware Interface]
        end
    end

    subgraph "Physical Hardware"
        LEDs[LED Lights<br/>Red/Yellow/Green]
        Sensors[Traffic Sensors<br/>Vehicle Detection]
        Network[Network Interface<br/>Remote Monitoring]
    end

    subgraph "External Systems"
        Monitor[Monitoring Dashboard<br/>Central Control]
        Admin[Admin Console<br/>Configuration]
    end

    App --> JDK
    JDK --> OS
    OS --> Drivers
    Drivers --> LEDs
    Drivers --> Sensors
    App -.->|Network| Monitor
    Admin -.->|Network| App

    style App fill:#ffeb3b,stroke:#f57f17,stroke-width:3px
    style JDK fill:#ff5722,stroke:#bf360c
    style OS fill:#4caf50,stroke:#1b5e20
    style LEDs fill:#f44336,stroke:#c62828
    style Monitor fill:#2196f3,stroke:#0d47a1
```

**Hardware Requirements**:
- **CPU**: 1 GHz+ (Raspberry Pi 3 or better)
- **RAM**: 256 MB minimum (512 MB recommended)
- **Storage**: 100 MB for application + logs
- **GPIO Pins**: 12 pins (4 directions × 3 states)
- **Network**: Optional (for remote monitoring)

**Software Requirements**:
- **OS**: Linux-based (Raspbian, Ubuntu)
- **Java**: JDK 8 or higher
- **Dependencies**: None (pure Java)

---

## 📊 Deployment Diagram - Multi-Intersection

```mermaid
graph TB
    subgraph "Central Control Center"
        Dashboard[Web Dashboard]
        Database[(Central Database<br/>PostgreSQL)]
        Analytics[Analytics Service<br/>Traffic Patterns]
    end

    subgraph "Intersection 1"
        Device1[Embedded Device 1<br/>Traffic App]
        Hardware1[LED Hardware 1]
    end

    subgraph "Intersection 2"
        Device2[Embedded Device 2<br/>Traffic App]
        Hardware2[LED Hardware 2]
    end

    subgraph "Intersection 3"
        Device3[Embedded Device 3<br/>Traffic App]
        Hardware3[LED Hardware 3]
    end

    subgraph "Intersection N"
        DeviceN[Embedded Device N<br/>Traffic App]
        HardwareN[LED Hardware N]
    end

    Device1 -.->|MQTT/HTTP| Dashboard
    Device2 -.->|MQTT/HTTP| Dashboard
    Device3 -.->|MQTT/HTTP| Dashboard
    DeviceN -.->|MQTT/HTTP| Dashboard

    Dashboard --> Database
    Dashboard --> Analytics

    Device1 --> Hardware1
    Device2 --> Hardware2
    Device3 --> Hardware3
    DeviceN --> HardwareN

    style Dashboard fill:#2196f3,stroke:#0d47a1,stroke-width:3px
    style Database fill:#4caf50,stroke:#1b5e20
    style Analytics fill:#ff9800,stroke:#e65100
```

**Communication Protocol**: MQTT or HTTP/REST
**Data Flow**:
1. Each intersection runs independently
2. Sends status updates to central dashboard
3. Dashboard aggregates and displays
4. Analytics service processes historical data

---

## 📊 Data Model

### State Transition Log (for persistence)

```mermaid
erDiagram
    INTERSECTION {
        int id PK
        string name
        double latitude
        double longitude
        timestamp created_at
    }

    SIGNAL {
        int id PK
        int intersection_id FK
        string direction
        timestamp created_at
    }

    STATE_TRANSITION {
        int id PK
        int signal_id FK
        string from_state
        string to_state
        timestamp transition_time
        boolean is_automatic
        string reason
        int duration
    }

    MANUAL_OVERRIDE {
        int id PK
        int intersection_id FK
        string direction
        string operator_id
        string reason
        timestamp override_time
        int duration
    }

    CONFIGURATION {
        int id PK
        int signal_id FK
        string state
        int duration
        timestamp updated_at
    }

    INTERSECTION ||--o{ SIGNAL : has
    SIGNAL ||--o{ STATE_TRANSITION : records
    SIGNAL ||--o{ CONFIGURATION : has
    INTERSECTION ||--o{ MANUAL_OVERRIDE : has
```

**Schema Details**:

1. **INTERSECTION**: Metadata about the intersection
   - Location (GPS coordinates)
   - Installation date
   - Name/identifier

2. **SIGNAL**: Individual signals at intersection
   - Direction (NORTH, SOUTH, EAST, WEST)
   - Foreign key to intersection

3. **STATE_TRANSITION**: Historical log of all state changes
   - Immutable audit log
   - Tracks automatic and manual transitions
   - Used for analytics and debugging

4. **MANUAL_OVERRIDE**: Log of operator interventions
   - Who triggered override
   - Why (reason)
   - When and for how long
   - Compliance and audit purposes

5. **CONFIGURATION**: Current settings
   - Duration for each state
   - Version controlled (historical tracking)

---

## 📊 Process Flow Diagram

```mermaid
flowchart TD
    Start([System Boot]) --> Init[Initialize Components]
    Init --> LoadConfig[Load Configuration]
    LoadConfig --> CreateSignals[Create 4 Traffic Signals]
    CreateSignals --> SetRed[Set All Signals to RED]
    SetRed --> StartTimer[Start Timer Thread]
    StartTimer --> FirstGreen[Set First Direction to GREEN]
    FirstGreen --> Running{System Running?}

    Running -->|Yes| Tick[Timer Tick Every Second]
    Tick --> Decrement[Decrement Active Signal Time]
    Decrement --> Expired{Time Expired?}

    Expired -->|No| Running
    Expired -->|Yes| CheckState{Current State?}

    CheckState -->|GREEN| ToYellow[Transition to YELLOW]
    ToYellow --> Running

    CheckState -->|YELLOW| ToRed[Transition to RED]
    ToRed --> NextDir[Get Next Direction]
    NextDir --> SetGreen[Set Next Signal to GREEN]
    SetGreen --> Running

    Running -->|Override?| Override[Manual Override Request]
    Override --> StopCurrent[Stop Current Signal<br/>GREEN → YELLOW → RED]
    StopCurrent --> ForceGreen[Force Target to GREEN]
    ForceGreen --> Running

    Running -->|Stop Command| Shutdown[Stop Timer Thread]
    Shutdown --> AllRed[Set All Signals to RED]
    AllRed --> Cleanup[Cleanup Resources]
    Cleanup --> End([System Shutdown])

    style Start fill:#4caf50,stroke:#1b5e20
    style Running fill:#ffeb3b,stroke:#f57f17
    style End fill:#f44336,stroke:#c62828
    style Override fill:#ff9800,stroke:#e65100
```

---

## 📊 Network Communication (Future Enhancement)

```mermaid
sequenceDiagram
    participant Device as Embedded Device
    participant MQTT as MQTT Broker
    participant Dashboard as Web Dashboard
    participant DB as Database

    Note over Device,DB: Real-time Status Updates

    loop Every 5 seconds
        Device->>MQTT: Publish status<br/>topic: /intersection/1/status<br/>payload: {direction: NORTH, state: GREEN, time: 45}
        MQTT->>Dashboard: Forward status
        Dashboard->>DB: Store status (optional)
    end

    Note over Device,DB: State Change Events

    Device->>Device: State transition occurs
    Device->>MQTT: Publish event<br/>topic: /intersection/1/events<br/>payload: {from: GREEN, to: YELLOW, time: 2025-11-17T10:30:15}
    MQTT->>Dashboard: Forward event
    Dashboard->>Dashboard: Update UI in real-time
    Dashboard->>DB: Log event

    Note over Device,DB: Manual Override

    Dashboard->>MQTT: Publish command<br/>topic: /intersection/1/commands<br/>payload: {action: override, direction: EAST, reason: "Ambulance"}
    MQTT->>Device: Forward command
    Device->>Device: Execute override
    Device->>MQTT: Publish result<br/>topic: /intersection/1/responses<br/>payload: {status: success}
    MQTT->>Dashboard: Forward result
    Dashboard->>Dashboard: Show confirmation
```

**MQTT Topics**:
- `/intersection/{id}/status` - Status updates
- `/intersection/{id}/events` - State change events
- `/intersection/{id}/commands` - Commands from dashboard
- `/intersection/{id}/responses` - Command responses

---

## 📊 Scalability Architecture

```mermaid
graph TB
    subgraph "Load Balancer"
        LB[Nginx Load Balancer]
    end

    subgraph "API Layer (Horizontal Scaling)"
        API1[API Server 1]
        API2[API Server 2]
        API3[API Server N]
    end

    subgraph "Message Queue"
        Queue[RabbitMQ / Kafka]
    end

    subgraph "Database Cluster"
        Master[(Master DB<br/>Write)]
        Slave1[(Replica 1<br/>Read)]
        Slave2[(Replica 2<br/>Read)]
    end

    subgraph "Intersections (Edge Devices)"
        Int1[Intersection 1]
        Int2[Intersection 2]
        IntN[Intersection N]
    end

    Int1 --> LB
    Int2 --> LB
    IntN --> LB

    LB --> API1
    LB --> API2
    LB --> API3

    API1 --> Queue
    API2 --> Queue
    API3 --> Queue

    Queue --> Master
    Master -.->|Replication| Slave1
    Master -.->|Replication| Slave2

    API1 -.->|Read| Slave1
    API2 -.->|Read| Slave2

    style LB fill:#2196f3,stroke:#0d47a1
    style Queue fill:#ff9800,stroke:#e65100
    style Master fill:#4caf50,stroke:#1b5e20
```

**Scalability Features**:
1. **Horizontal Scaling**: Multiple API servers
2. **Load Balancing**: Distribute traffic
3. **Message Queue**: Decouple components
4. **Database Replication**: Read replicas for scaling
5. **Edge Computing**: Each intersection runs independently

---

## 🎯 Deployment Strategies

### Strategy 1: Standalone (Current Implementation)
- **Use Case**: Single intersection, no network
- **Pros**: Simple, no dependencies
- **Cons**: No remote monitoring

### Strategy 2: Centralized Monitoring
- **Use Case**: Multiple intersections, city-wide
- **Pros**: Central visibility, analytics
- **Cons**: Network dependency

### Strategy 3: Hybrid (Recommended)
- **Use Case**: Critical infrastructure
- **Pros**: Works offline, syncs when online
- **Cons**: More complex implementation

---

## 🎯 Key Takeaways

1. **Component Diagram**: Shows logical structure
2. **Deployment Diagram**: Shows physical infrastructure
3. **Data Model**: Defines persistence structure
4. **Scalability**: System can grow from 1 to 1000s of intersections
5. **Independence**: Each intersection runs autonomously

---

## 🔜 What's Next?

Next we'll cover:
- **Concurrency and Thread Safety**: Deep dive into locking
- **SOLID Principles**: How we applied them
- **Design Patterns**: Complete catalog

---

**Remember**: Good architecture is about layers, separation of concerns, and scalability!
