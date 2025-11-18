# Phase 19: Polyfills - Function & Object Methods

## Function.prototype Methods

```mermaid
graph TD
    subgraph "Context Methods"
        CALL[call] --> |"Invoke immediately"| C1["fn.call(context, arg1, arg2)"]
        APPLY[apply] --> |"Invoke with array"| A1["fn.apply(context, [args])"]
        BIND[bind] --> |"Return new function"| B1["fn.bind(context, arg1)"]
    end

    style CALL fill:#ffcdd2
    style APPLY fill:#c8e6c9
    style BIND fill:#bbdefb
```

## bind vs call vs apply

```mermaid
sequenceDiagram
    participant F as Function
    participant C as Context
    participant A as Arguments

    Note over F,A: call - Invoke immediately
    F->>C: Set this
    F->>A: Pass args separately
    F-->>F: Execute and return

    Note over F,A: apply - Invoke with array
    F->>C: Set this
    F->>A: Pass args as array
    F-->>F: Execute and return

    Note over F,A: bind - Return bound function
    F->>C: Set this
    F->>A: Partial application
    F-->>F: Return new function
```

## Topics Covered

1. Function.prototype.call
2. Function.prototype.apply
3. Function.prototype.bind
4. Object.create
5. Object.assign
6. Object.keys/values/entries
7. debounce
8. throttle
