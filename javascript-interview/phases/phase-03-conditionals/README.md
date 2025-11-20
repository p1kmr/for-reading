# Phase 3: Conditionals & Switch

## Control Flow Visualization

```mermaid
flowchart TD
    A[Start] --> B{Condition?}
    B -->|true| C[Execute if block]
    B -->|false| D{else if?}
    D -->|true| E[Execute else if block]
    D -->|false| F[Execute else block]
    C --> G[Continue]
    E --> G
    F --> G
```

## Switch Statement Flow

```mermaid
flowchart TD
    START[Switch Expression] --> EVAL{Evaluate}
    EVAL --> C1{case 1?}
    C1 -->|match| A1[Execute]
    C1 -->|no match| C2{case 2?}
    A1 -->|break| END[Exit]
    A1 -->|no break| FALL[Fall through]
    C2 -->|match| A2[Execute]
    C2 -->|no match| C3{case 3?}
    A2 -->|break| END
    FALL --> A2
    C3 -->|no match| DEF[Default case]
    DEF --> END

    style FALL fill:#fff3e0
```

## Topics Covered

1. if/else statements
2. else if chains
3. Switch statements
4. Fall-through behavior
5. Ternary operator patterns
6. Guard clauses
7. Truthy/falsy in conditions

## Key Concepts

- Guard clauses for early returns
- Switch uses strict equality (===)
- Fall-through can be intentional or a bug
