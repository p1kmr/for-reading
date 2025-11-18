# Phase 5: Function Declarations & Expressions

## Function Types Comparison

```mermaid
graph TD
    subgraph "Function Types"
        FD[Function Declaration]
        FE[Function Expression]
        AF[Arrow Function]
        IIFE[IIFE]
    end

    subgraph "Characteristics"
        FD -->|"Hoisted completely"| H1[Can call before definition]
        FE -->|"Not hoisted"| H2[Must define before call]
        AF -->|"Lexical this"| H3[No own this/arguments]
        IIFE -->|"Self-executing"| H4[Runs immediately]
    end

    style FD fill:#c8e6c9
    style FE fill:#fff3e0
    style AF fill:#e3f2fd
    style IIFE fill:#f3e5f5
```

## Execution Context Creation

```mermaid
sequenceDiagram
    participant Code as Source Code
    participant Engine as JS Engine
    participant EC as Execution Context

    Code->>Engine: Script loaded
    Engine->>EC: Create Global EC
    EC->>EC: Hoist function declarations
    EC->>EC: Hoist var (as undefined)
    EC->>EC: let/const in TDZ
    Engine->>Code: Execute line by line
    Code->>Engine: Function call
    Engine->>EC: Create Function EC
    EC->>EC: Set up arguments
    EC->>EC: Hoist local declarations
    Engine->>Code: Execute function body
```

## Topics Covered

1. Function declarations
2. Function expressions
3. Anonymous functions
4. Named function expressions
5. Parameters and arguments
6. Default parameters
7. Rest parameters
8. Return values
9. Hoisting behavior
10. IIFE pattern
