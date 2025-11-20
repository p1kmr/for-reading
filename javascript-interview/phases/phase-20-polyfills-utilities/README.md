# Phase 20: Polyfills - Promise & Utility Functions

## Promise.all vs Promise.allSettled

```mermaid
graph TD
    subgraph "Promise.all"
        ALL[Promise.all] --> |"All fulfill"| SUCCESS1[Resolve with array]
        ALL --> |"Any reject"| FAIL1[Reject immediately]
    end

    subgraph "Promise.allSettled"
        SETTLED[Promise.allSettled] --> |"All settle"| SUCCESS2[Always resolve]
        SUCCESS2 --> RESULT["[{status, value/reason}]"]
    end

    style FAIL1 fill:#ffebee
```

## Utility Functions Overview

```mermaid
graph LR
    subgraph "Async Utilities"
        PROMISEALL[Promise.all]
        PROMISERACE[Promise.race]
        RETRY[retry]
        TIMEOUT[timeout]
    end

    subgraph "Function Utilities"
        CURRY[curry]
        COMPOSE[compose]
        MEMOIZE[memoize]
        ONCE[once]
    end

    subgraph "Data Utilities"
        DEEPCLONE[deepClone]
        DEEPEQUAL[deepEqual]
        FLATTEN[flatten]
    end
```

## Topics Covered

1. Promise.all polyfill
2. Promise.race polyfill
3. Promise.allSettled polyfill
4. Promise.any polyfill
5. Deep clone implementation
6. Deep equality check
7. Event emitter pattern
8. Retry with backoff
