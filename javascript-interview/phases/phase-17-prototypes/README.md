# Phase 17: Prototypes & Inheritance

## Prototype Chain

```mermaid
graph TD
    subgraph "Instance"
        OBJ[myDog]
    end

    subgraph "Constructor Prototype"
        DOG["Dog.prototype"]
    end

    subgraph "Parent Prototype"
        ANIMAL["Animal.prototype"]
    end

    subgraph "Base Prototype"
        OBJPROTO["Object.prototype"]
    end

    subgraph "End"
        NULL[null]
    end

    OBJ -->|"__proto__"| DOG
    DOG -->|"__proto__"| ANIMAL
    ANIMAL -->|"__proto__"| OBJPROTO
    OBJPROTO -->|"__proto__"| NULL

    style OBJ fill:#e3f2fd
    style DOG fill:#c8e6c9
    style ANIMAL fill:#fff3e0
    style OBJPROTO fill:#f3e5f5
```

## Constructor and Prototype Relationship

```mermaid
graph LR
    subgraph "Constructor Function"
        FN[Person]
    end

    subgraph "Prototype Object"
        PROTO["Person.prototype"]
    end

    subgraph "Instance"
        INST[person1]
    end

    FN -->|".prototype"| PROTO
    PROTO -->|".constructor"| FN
    INST -->|"__proto__"| PROTO
    FN -->|"new"| INST

    style FN fill:#ffcdd2
    style PROTO fill:#c8e6c9
    style INST fill:#e3f2fd
```

## Topics Covered

1. Prototype chain
2. __proto__ vs prototype
3. Constructor functions
4. Object.create()
5. Class inheritance
6. Mixins and composition
7. hasOwnProperty vs in
8. Property shadowing
