# Phase 14: Async/Await & Event Loop

## The JavaScript Event Loop

```mermaid
graph TD
    subgraph "JavaScript Runtime"
        CS[Call Stack]
        WA[Web APIs]
        TQ[Task Queue<br/>Macrotasks]
        MQ[Microtask Queue]
        EL[Event Loop]
    end

    CS -->|"setTimeout, fetch"| WA
    WA -->|"Callback ready"| TQ
    WA -->|"Promise resolved"| MQ

    EL -->|"1. Check Stack Empty"| CS
    EL -->|"2. Process ALL Microtasks"| MQ
    EL -->|"3. Take ONE Macrotask"| TQ
    TQ -->|"Push to Stack"| CS
    MQ -->|"Push to Stack"| CS

    style CS fill:#ffebee
    style MQ fill:#e8f5e9
    style TQ fill:#fff3e0
```

## Promise States

```mermaid
stateDiagram-v2
    [*] --> Pending: new Promise()
    Pending --> Fulfilled: resolve(value)
    Pending --> Rejected: reject(error)
    Fulfilled --> [*]
    Rejected --> [*]

    note right of Pending
        Initial state
        Async operation in progress
    end note

    note right of Fulfilled
        Operation completed successfully
        Has a value
    end note

    note right of Rejected
        Operation failed
        Has a reason (error)
    end note
```

## Async/Await Flow

```mermaid
sequenceDiagram
    participant Main as Main Code
    participant Async as Async Function
    participant Promise as Promise

    Main->>Async: Call async function
    Async->>Promise: await promise
    Note over Async: Pauses here
    Async-->>Main: Returns Promise
    Note over Main: Continues execution

    Promise->>Async: Resolved/Rejected
    Async->>Async: Resumes after await
    Async->>Main: Resolves returned Promise
```

## Topics Covered

1. Event loop mechanics
2. Call stack
3. Task queue vs Microtask queue
4. Promises in depth
5. Promise methods (all, race, allSettled, any)
6. Async/await syntax
7. Error handling patterns
8. Parallel vs sequential execution
