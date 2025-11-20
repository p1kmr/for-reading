# JavaScript Concept Diagrams

A collection of Mermaid diagrams explaining core JavaScript concepts.

## Event Loop

```mermaid
graph TD
    subgraph "JavaScript Runtime"
        direction TB
        HEAP[Memory Heap]
        STACK[Call Stack]
    end

    subgraph "Web APIs"
        DOM[DOM]
        TIMER[Timers]
        FETCH[Fetch]
    end

    subgraph "Queues"
        MQ[Microtask Queue<br/>Promise callbacks]
        TQ[Task Queue<br/>setTimeout, I/O]
    end

    STACK -->|"Async call"| WEB
    WEB[Web APIs] -->|"Promise"| MQ
    WEB -->|"Timer"| TQ
    MQ -->|"Higher priority"| LOOP
    TQ -->|"Lower priority"| LOOP
    LOOP[Event Loop] -->|"When stack empty"| STACK

    style STACK fill:#ffebee
    style MQ fill:#e8f5e9
    style TQ fill:#fff3e0
```

## Execution Context

```mermaid
graph TD
    subgraph "Global Execution Context"
        GVO[Variable Object]
        GSC[Scope Chain]
        GT[this = window/global]
    end

    subgraph "Function Execution Context"
        VO[Variable Object]
        SC[Scope Chain]
        T["this = depends on call"]
        ARGS[arguments object]
    end

    GVO --> |"Outer reference"| null
    VO --> |"Outer reference"| GVO
```

## Hoisting Timeline

```mermaid
sequenceDiagram
    participant Parser
    participant Memory
    participant Execution

    Parser->>Memory: Scan for declarations
    Memory->>Memory: var -> undefined
    Memory->>Memory: function -> full function
    Memory->>Memory: let/const -> TDZ
    Parser->>Execution: Execute line by line
    Execution->>Memory: var assignment
    Execution->>Memory: let/const declaration exits TDZ
```

## Closure Scope Chain

```mermaid
graph TB
    subgraph "Global Scope"
        G[globalVar = 1]
    end

    subgraph "outer() Scope"
        O[outerVar = 2]
    end

    subgraph "inner() Scope"
        I[innerVar = 3]
    end

    subgraph "Closure"
        CL["inner() has access to:<br/>- innerVar<br/>- outerVar<br/>- globalVar"]
    end

    I --> O
    O --> G
    CL -.-> I
    CL -.-> O
    CL -.-> G
```

## This Binding Decision Tree

```mermaid
flowchart TD
    A[Function called] --> B{Called with new?}
    B -->|Yes| C["this = new object"]
    B -->|No| D{Called with call/apply/bind?}
    D -->|Yes| E["this = specified object"]
    D -->|No| F{Called as method?}
    F -->|Yes| G["this = containing object"]
    F -->|No| H{Arrow function?}
    H -->|Yes| I["this = lexical scope"]
    H -->|No| J["this = undefined/global"]

    style C fill:#c8e6c9
    style E fill:#fff3e0
    style G fill:#e3f2fd
    style I fill:#f3e5f5
    style J fill:#ffebee
```

## Promise Flow

```mermaid
stateDiagram-v2
    [*] --> Pending: new Promise(executor)
    Pending --> Fulfilled: resolve(value)
    Pending --> Rejected: reject(reason)

    Fulfilled --> ThenCallback: .then(onFulfilled)
    Rejected --> CatchCallback: .catch(onRejected)
    Rejected --> ThenCallback: .then(null, onRejected)

    ThenCallback --> NewPromise: Returns Promise
    CatchCallback --> NewPromise: Returns Promise

    NewPromise --> [*]
```

## Array Methods Comparison

```mermaid
graph TD
    subgraph "Transforming"
        MAP[map] -->|"New array, same length"| M1["[1,2,3].map(x=>x*2) = [2,4,6]"]
    end

    subgraph "Filtering"
        FILTER[filter] -->|"New array, subset"| F1["[1,2,3].filter(x=>x>1) = [2,3]"]
    end

    subgraph "Reducing"
        REDUCE[reduce] -->|"Single value"| R1["[1,2,3].reduce((a,b)=>a+b,0) = 6"]
    end

    subgraph "Searching"
        FIND[find] -->|"First match"| FI1["[1,2,3].find(x=>x>1) = 2"]
        FINDINDEX[findIndex] -->|"Index"| FI2["[1,2,3].findIndex(x=>x>1) = 1"]
    end
```

## Prototype Chain

```mermaid
graph BT
    OBJ["myObj<br/>{name: 'test'}"]
    CUSTOM["CustomClass.prototype<br/>{method: fn}"]
    OBJPROTO["Object.prototype<br/>{toString, hasOwnProperty}"]
    NULL[null]

    OBJ -->|"__proto__"| CUSTOM
    CUSTOM -->|"__proto__"| OBJPROTO
    OBJPROTO -->|"__proto__"| NULL
```

## Async/Await Transformation

```mermaid
graph LR
    subgraph "Async/Await"
        A1["async function fn() {<br/>  const x = await p1;<br/>  const y = await p2;<br/>  return x + y;<br/>}"]
    end

    subgraph "Promise Equivalent"
        P1["function fn() {<br/>  return p1.then(x => {<br/>    return p2.then(y => {<br/>      return x + y;<br/>    });<br/>  });<br/>}"]
    end

    A1 -->|"Compiles to"| P1
```

## Debounce vs Throttle

```mermaid
gantt
    title Function Execution Timeline
    dateFormat X
    axisFormat %S

    section Events
    Click 1 :a1, 0, 1
    Click 2 :a2, 2, 1
    Click 3 :a3, 4, 1
    Click 4 :a4, 6, 1
    Click 5 :a5, 10, 1

    section Debounce (3s)
    Wait    :crit, d1, 0, 9
    Execute :done, d2, 9, 1

    section Throttle (3s)
    Execute :done, t1, 0, 1
    Wait    :crit, t2, 1, 3
    Execute :done, t3, 6, 1
    Wait    :crit, t4, 7, 3
    Execute :done, t5, 10, 1
```

## Memory: Stack vs Heap

```mermaid
graph LR
    subgraph "Stack Memory"
        direction TB
        S1["number: 42"]
        S2["string: 'hello'"]
        S3["boolean: true"]
        S4["reference: 0x001"]
    end

    subgraph "Heap Memory"
        direction TB
        H1["0x001: {<br/>  name: 'Alice'<br/>}"]
        H2["0x002: [1, 2, 3]"]
    end

    S4 --> H1

    style S1 fill:#e3f2fd
    style S2 fill:#e3f2fd
    style S3 fill:#e3f2fd
    style H1 fill:#fff3e0
    style H2 fill:#fff3e0
```

## Module Systems Comparison

```mermaid
graph TD
    subgraph "CommonJS (Node.js)"
        CJS["module.exports = {...}<br/>const x = require('./x')"]
    end

    subgraph "ES Modules"
        ESM["export default/named<br/>import x from './x'"]
    end

    subgraph "Differences"
        D1["CJS: Sync, runtime<br/>ESM: Async, static"]
    end

    CJS --> D1
    ESM --> D1
```

---

All diagrams use Mermaid syntax and can be rendered in GitHub, VSCode, or any Mermaid-compatible viewer.
