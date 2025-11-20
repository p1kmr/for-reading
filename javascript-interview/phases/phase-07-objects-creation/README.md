# Phase 7: Objects Creation & Properties

## Object Creation Methods

```mermaid
graph TD
    A[Object Creation] --> B[Object Literal]
    A --> C[Constructor Function]
    A --> D[Object.create]
    A --> E[ES6 Class]

    B --> |"{}"| B1["Simple, direct"]
    C --> |"new Function()"| C1["Before ES6"]
    D --> |"Prototype control"| D1["Inheritance"]
    E --> |"class syntax"| E1["Modern standard"]

    style B fill:#c8e6c9
    style E fill:#e3f2fd
```

## Property Descriptors

```mermaid
graph LR
    subgraph "Data Descriptor"
        V[value] --> |"The property value"| V1[any]
        W[writable] --> |"Can be changed?"| W1[boolean]
    end

    subgraph "Common to Both"
        E[enumerable] --> |"Shows in for...in?"| E1[boolean]
        C[configurable] --> |"Can delete/modify?"| C1[boolean]
    end

    subgraph "Accessor Descriptor"
        G[get] --> |"Getter function"| G1[function]
        S[set] --> |"Setter function"| S1[function]
    end
```

## Topics Covered

1. Object literal syntax
2. Constructor functions
3. Object.create()
4. Property access (dot vs bracket)
5. Property descriptors
6. Getters and setters
7. Object methods (keys, values, entries)
8. Object immutability
