# Phase 18: Polyfills - Array Methods

## What Are Polyfills?

```mermaid
graph LR
    A[Modern JS Feature] --> B{Browser Support?}
    B -->|Yes| C[Use Native]
    B -->|No| D[Use Polyfill]
    D --> E[Same Behavior]

    style A fill:#e3f2fd
    style D fill:#fff3e0
```

## Why Polyfills in Interviews

```mermaid
pie title "Interview Skill Assessment"
    "Understanding Internals" : 30
    "Problem Solving" : 25
    "Edge Case Handling" : 20
    "Code Quality" : 15
    "Testing Approach" : 10
```

## Critical Polyfills to Know

```mermaid
graph TD
    subgraph "Array Methods"
        MAP[map]
        FILTER[filter]
        REDUCE[reduce]
        FIND[find]
        FLAT[flat]
    end

    subgraph "Function Methods"
        BIND[bind]
        CALL[call]
        APPLY[apply]
    end

    subgraph "Promise Methods"
        ALL[Promise.all]
        RACE[Promise.race]
    end

    subgraph "Utilities"
        DEBOUNCE[debounce]
        THROTTLE[throttle]
        DEEPCLONE[deepClone]
    end

    style MAP fill:#ffcdd2
    style FILTER fill:#ffcdd2
    style REDUCE fill:#ffcdd2
    style BIND fill:#c8e6c9
```

## Topics Covered

1. Array.prototype.map
2. Array.prototype.filter
3. Array.prototype.reduce
4. Array.prototype.find/findIndex
5. Array.prototype.forEach
6. Array.prototype.some/every
7. Array.prototype.flat/flatMap
8. Array.from
