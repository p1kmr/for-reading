# Phase 4: Loops Deep Dive

## Loop Types Comparison

```mermaid
flowchart LR
    subgraph "Counting Loops"
        FOR[for] --> |"Known iterations"| K1[Array indices]
        FOR --> K2[Range of numbers]
    end

    subgraph "Condition Loops"
        WHILE[while] --> |"Unknown iterations"| W1[Until condition met]
        DO[do...while] --> |"At least once"| D1[Input validation]
    end

    subgraph "Iteration Loops"
        FORIN[for...in] --> |"Object keys"| I1[Enumerate properties]
        FOROF[for...of] --> |"Iterable values"| I2[Arrays/Strings/Maps]
    end
```

## Loop Control Flow

```mermaid
flowchart TD
    START[Initialize] --> CHECK{Condition?}
    CHECK -->|true| BODY[Execute body]
    BODY --> CONT{continue?}
    CONT -->|yes| UPDATE
    CONT -->|no| BREAK{break?}
    BREAK -->|yes| END[Exit loop]
    BREAK -->|no| UPDATE[Update counter]
    UPDATE --> CHECK
    CHECK -->|false| END

    style CONT fill:#fff3e0
    style BREAK fill:#ffebee
```

## Topics Covered

1. for loop (classic)
2. while loop
3. do...while loop
4. for...in (object properties)
5. for...of (iterable values)
6. break and continue
7. Labeled statements
8. Loop performance
