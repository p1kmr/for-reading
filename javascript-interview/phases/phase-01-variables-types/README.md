# Phase 1: Variables, Types & Fundamentals

## Overview

This phase covers the absolute foundation of JavaScript - understanding how data is stored, accessed, and typed in the language.

## Variable Declaration Comparison

```mermaid
graph TB
    subgraph "Variable Declaration Keywords"
        VAR[var]
        LET[let]
        CONST[const]
    end

    subgraph "Characteristics"
        VAR --> |"Function Scoped"| FS[Function/Global Scope]
        VAR --> |"Hoisted with undefined"| HU[Hoisting: undefined]
        VAR --> |"Can redeclare"| RD[Redeclarable]

        LET --> |"Block Scoped"| BS1[Block Scope]
        LET --> |"Hoisted to TDZ"| TDZ1[Temporal Dead Zone]
        LET --> |"No redeclare"| NRD1[Not Redeclarable]

        CONST --> |"Block Scoped"| BS2[Block Scope]
        CONST --> |"Hoisted to TDZ"| TDZ2[Temporal Dead Zone]
        CONST --> |"Must initialize"| MI[Must Initialize]
    end

    style VAR fill:#ffcdd2
    style LET fill:#c8e6c9
    style CONST fill:#bbdefb
```

## Memory Allocation: Primitives vs References

```mermaid
graph LR
    subgraph "Stack Memory (Primitives)"
        direction TB
        S1["name: 'John'"]
        S2["age: 25"]
        S3["isActive: true"]
    end

    subgraph "Heap Memory (References)"
        direction TB
        H1["{ name: 'John', age: 25 }"]
        H2["[1, 2, 3, 4, 5]"]
        H3["function() { ... }"]
    end

    subgraph "Stack (References)"
        R1["obj: 0x001"] --> H1
        R2["arr: 0x002"] --> H2
        R3["fn: 0x003"] --> H3
    end
```

## Type Coercion Flowchart

```mermaid
flowchart TD
    A[Operation with Mixed Types] --> B{What operation?}

    B -->|"+ with string"| C[Convert to String]
    B -->|"-, *, /"| D[Convert to Number]
    B -->|"Boolean context"| E[Convert to Boolean]
    B -->|"== comparison"| F[Complex Rules]

    C --> G["'5' + 3 = '53'"]
    D --> H["'5' - 3 = 2"]
    E --> I["Boolean('') = false"]
    F --> J["'5' == 5 = true"]

    style C fill:#fff3e0
    style D fill:#e3f2fd
    style E fill:#f3e5f5
    style F fill:#ffebee
```

## Topics Covered

1. What is JavaScript and where does it run
2. Variables: var, let, const
3. Data types: Primitives and References
4. Type coercion and conversion
5. Truthy and falsy values
6. Template literals

## Key Interview Questions

1. What is the difference between `var`, `let`, and `const`?
2. Explain hoisting with examples
3. What is the Temporal Dead Zone?
4. Why does `typeof null` return "object"?
5. What are falsy values in JavaScript?

## Quick Reference

| Feature | var | let | const |
|---------|-----|-----|-------|
| Scope | Function | Block | Block |
| Hoisting | Yes (undefined) | Yes (TDZ) | Yes (TDZ) |
| Redeclare | Yes | No | No |
| Reassign | Yes | Yes | No |
| Must Init | No | No | Yes |

## Files in This Phase

- `examples.js` - Comprehensive code examples
- `exercises.js` - Practice problems
- `interview-questions.md` - Detailed Q&A
