# Phase 10: Array Iteration Methods

## Array Methods Flowchart

```mermaid
flowchart TD
    START[Array Operation] --> Q1{Need what?}

    Q1 -->|"Transform each element"| MAP[map]
    Q1 -->|"Select elements"| FILTER[filter]
    Q1 -->|"Single value from all"| REDUCE[reduce]
    Q1 -->|"Just iterate"| FOREACH[forEach]
    Q1 -->|"Find one element"| FIND[find]
    Q1 -->|"Check condition"| CHECK{Which check?}

    CHECK -->|"All pass?"| EVERY[every]
    CHECK -->|"Any pass?"| SOME[some]
    CHECK -->|"Element exists?"| INCLUDES[includes]

    MAP --> RET1["Returns: new array"]
    FILTER --> RET2["Returns: new array"]
    REDUCE --> RET3["Returns: single value"]
    FOREACH --> RET4["Returns: undefined"]
    FIND --> RET5["Returns: element or undefined"]
    EVERY --> RET6["Returns: boolean"]
    SOME --> RET7["Returns: boolean"]
    INCLUDES --> RET8["Returns: boolean"]

    style MAP fill:#c8e6c9
    style FILTER fill:#c8e6c9
    style REDUCE fill:#fff3e0
```

## Method Comparison Table

```mermaid
graph LR
    subgraph "Returns New Array"
        MAP[map]
        FILTER[filter]
        FLAT[flat]
        FLATMAP[flatMap]
    end

    subgraph "Returns Boolean"
        EVERY[every]
        SOME[some]
        INCLUDES[includes]
    end

    subgraph "Returns Single Value"
        REDUCE[reduce]
        FIND[find]
        FINDINDEX[findIndex]
    end

    subgraph "Returns Undefined"
        FOREACH[forEach]
    end
```

## Topics Covered

1. forEach vs map
2. filter for selection
3. reduce for aggregation
4. find and findIndex
5. some and every
6. flat and flatMap
7. Method chaining
8. Performance considerations
