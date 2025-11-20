# Phase 2: Operators & Type Coercion

## Overview

Master JavaScript operators and the implicit type coercion rules that drive many interview questions.

## Operator Precedence

```mermaid
graph TD
    A["() Grouping"] --> B["++ -- Unary"]
    B --> C["* / % Multiplication"]
    C --> D["+ - Addition"]
    D --> E["< > <= >= Comparison"]
    E --> F["== === != !== Equality"]
    F --> G["&& Logical AND"]
    G --> H["|| Logical OR"]
    H --> I["?: Ternary"]
    I --> J["= += -= Assignment"]

    style A fill:#e8f5e9
    style J fill:#ffebee
```

## Type Coercion Decision Tree

```mermaid
flowchart TD
    START[Binary Operation] --> CHECK{Operator Type?}

    CHECK -->|"+"| PLUS{Either operand string?}
    PLUS -->|Yes| TOSTR[Convert both to String]
    PLUS -->|No| TONUM1[Convert both to Number]

    CHECK -->|"- * / %"| TONUM2[Convert both to Number]

    CHECK -->|"< > <= >="| COMPARE{Both strings?}
    COMPARE -->|Yes| LEXICAL[Lexicographical compare]
    COMPARE -->|No| TONUM3[Convert to Number]

    CHECK -->|"== !="| LOOSE{Same type?}
    LOOSE -->|Yes| DIRECT[Direct compare]
    LOOSE -->|No| COERCE[Apply coercion rules]

    CHECK -->|"=== !=="| STRICT[No coercion - compare directly]

    TOSTR --> RESULT[Result]
    TONUM1 --> RESULT
    TONUM2 --> RESULT
    LEXICAL --> RESULT
    TONUM3 --> RESULT
    DIRECT --> RESULT
    COERCE --> RESULT
    STRICT --> RESULT
```

## Loose Equality Coercion Rules

```mermaid
graph LR
    subgraph "== Coercion Priority"
        A[null == undefined] --> |true| T1[Special case]
        B[Number == String] --> |String to Number| T2[ToNumber]
        C[Boolean == Anything] --> |Boolean to Number| T3[ToNumber]
        D[Object == Primitive] --> |Object to Primitive| T4[ToPrimitive]
    end
```

## Topics Covered

1. Arithmetic operators
2. Comparison operators
3. Logical operators
4. Assignment operators
5. Bitwise operators
6. Type coercion rules
7. Short-circuit evaluation
8. Nullish coalescing

## Key Interview Questions

1. What is operator precedence?
2. Explain short-circuit evaluation
3. What's the difference between || and ??
4. How does type coercion work with ==?
5. Explain the + operator's dual behavior
