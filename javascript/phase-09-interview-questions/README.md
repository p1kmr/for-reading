# Phase 9: Interview Questions and Answers

## 📋 Table of Contents
1. [Easy Questions](#easy-questions)
2. [Medium Questions](#medium-questions)
3. [Hard Questions](#hard-questions)
4. [Coding Challenges](#coding-challenges)
5. [System Design Questions](#system-design-questions)

---

## Easy Questions

### Q1: What is JavaScript?

**Answer:**
JavaScript is a high-level, interpreted programming language primarily used for making web pages interactive. It runs in browsers (client-side) and can also run on servers using Node.js.

**Key Points:**
- Dynamic typing
- Prototype-based
- First-class functions
- Event-driven
- Single-threaded with async capabilities

---

### Q2: What are the data types in JavaScript?

**Answer:**
JavaScript has 8 data types:

**Primitive (7):**
1. String - text data
2. Number - numeric values
3. Boolean - true/false
4. Undefined - declared but not assigned
5. Null - intentionally empty
6. Symbol - unique identifier
7. BigInt - large integers

**Reference (1):**
8. Object - collections of data

```javascript
typeof "hello"     // "string"
typeof 42          // "number"
typeof true        // "boolean"
typeof undefined   // "undefined"
typeof null        // "object" (historical bug)
typeof Symbol()    // "symbol"
typeof 123n        // "bigint"
typeof {}          // "object"
```

---

### Q3: What is the difference between == and ===?

**Answer:**

```
┌────────────────────────────────────────┐
│        == vs ===                       │
├───────────────┬────────────────────────┤
│      ==       │        ===             │
├───────────────┼────────────────────────┤
│ Loose         │ Strict                 │
│ equality      │ equality               │
├───────────────┼────────────────────────┤
│ Type coercion │ No type coercion       │
│ (converts     │ (checks type first)    │
│ types)        │                        │
├───────────────┼────────────────────────┤
│ 5 == "5"      │ 5 === "5"              │
│ ✓ true        │ ✗ false                │
└───────────────┴────────────────────────┘
```

```javascript
// == examples
5 == "5"           // true
0 == false         // true
null == undefined  // true

// === examples (always use this!)
5 === "5"          // false
0 === false        // false
null === undefined // false
```

---

### Q4: What is hoisting?

**Answer:**
Hoisting is JavaScript's behavior of moving declarations to the top of their scope during compilation.

```javascript
// What you write:
console.log(x);  // undefined (not error!)
var x = 5;

// How JavaScript interprets it:
var x;           // Declaration hoisted
console.log(x);  // undefined
x = 5;           // Assignment stays

// Function declarations are fully hoisted
greet();  // Works!

function greet() {
    console.log("Hello");
}

// But function expressions are not
greet2();  // Error!

const greet2 = function() {
    console.log("Hello");
};
```

---

### Q5: What is the difference between let, const, and var?

**Answer:**

```
┌─────────────────────────────────────────────────┐
│         let vs const vs var                     │
├──────────┬───────────┬────────────┬─────────────┤
│          │    var    │    let     │    const    │
├──────────┼───────────┼────────────┼─────────────┤
│ Scope    │ Function  │ Block      │ Block       │
├──────────┼───────────┼────────────┼─────────────┤
│ Reassign │ ✓         │ ✓          │ ✗           │
├──────────┼───────────┼────────────┼─────────────┤
│ Redeclare│ ✓         │ ✗          │ ✗           │
├──────────┼───────────┼────────────┼─────────────┤
│ Hoisted  │ ✓         │ TDZ        │ TDZ         │
├──────────┼───────────┼────────────┼─────────────┤
│ Use?     │ ❌ Never  │ ✓ When     │ ✓ Default   │
│          │           │  changing  │  choice     │
└──────────┴───────────┴────────────┴─────────────┘
```

---

## Medium Questions

### Q6: What is a closure?

**Answer:**
A closure is a function that has access to variables from its outer (enclosing) scope, even after the outer function has finished executing.

```javascript
function createCounter() {
    let count = 0;  // Private variable

    return function() {
        count++;
        return count;
    };
}

const counter = createCounter();
console.log(counter());  // 1
console.log(counter());  // 2
console.log(counter());  // 3

// 'count' is private and maintained between calls
```

**Diagram:**
```
┌──────────────────────────────────────────┐
│  function createCounter() {              │
│      let count = 0; ◄──────────┐        │
│                                 │        │
│      return function() {        │        │
│          count++; ──────────────┘        │
│          return count;                   │
│      };                                  │
│  }                                       │
│                                          │
│  Inner function "closes over" count      │
└──────────────────────────────────────────┘
```

**Use Cases:**
- Private variables
- Data encapsulation
- Factories
- Event handlers

---

### Q7: Explain 'this' keyword

**Answer:**
`this` refers to the object that is executing the current function. Its value depends on how the function is called.

```javascript
// 1. Global context
console.log(this);  // Window (browser)

// 2. Object method
const obj = {
    name: "Alice",
    greet() {
        console.log(this.name);  // "Alice"
    }
};
obj.greet();

// 3. Regular function
function show() {
    console.log(this);  // Window or undefined (strict mode)
}
show();

// 4. Arrow function (inherits from parent)
const obj2 = {
    name: "Bob",
    greet: () => {
        console.log(this.name);  // undefined (this = window)
    }
};

// 5. Constructor
function Person(name) {
    this.name = name;  // this = new object
}
const alice = new Person("Alice");

// 6. call/apply/bind
function greet() {
    console.log(this.name);
}
greet.call({ name: "Charlie" });  // "Charlie"
```

---

### Q8: What is the Event Loop?

**Answer:**
The event loop is how JavaScript handles asynchronous operations despite being single-threaded.

**Diagram:**
```
┌──────────────────────────────────────────┐
│         EVENT LOOP                       │
│                                          │
│  Call Stack ◄──────────┐                │
│      │                 │                │
│      ▼                 │                │
│  Web APIs              │                │
│  (setTimeout, fetch)   │                │
│      │                 │                │
│      ▼                 │                │
│  Callback Queue ───────┘                │
│                                          │
│  Flow:                                   │
│  1. Execute sync code in call stack     │
│  2. Send async operations to Web APIs   │
│  3. When complete, callback → queue     │
│  4. Event loop checks if stack is empty │
│  5. Move callback from queue to stack   │
└──────────────────────────────────────────┘
```

```javascript
console.log("1");

setTimeout(() => {
    console.log("2");
}, 0);

Promise.resolve().then(() => {
    console.log("3");
});

console.log("4");

// Output: 1, 4, 3, 2
// Why? Microtasks (Promises) before Macrotasks (setTimeout)
```

---

### Q9: What is the difference between call, apply, and bind?

**Answer:**

```javascript
function greet(greeting, punctuation) {
    return `${greeting}, ${this.name}${punctuation}`;
}

const person = { name: "Alice" };

// call - invoke immediately, args separately
greet.call(person, "Hello", "!");  // "Hello, Alice!"

// apply - invoke immediately, args as array
greet.apply(person, ["Hi", "?"]);  // "Hi, Alice?"

// bind - return new function, args optional
const greetAlice = greet.bind(person);
greetAlice("Hey", "!!!");  // "Hey, Alice!!!"

// Partial application with bind
const sayHello = greet.bind(person, "Hello");
sayHello("!");  // "Hello, Alice!"
```

**Summary:**
- `call`: Execute now, arguments separate
- `apply`: Execute now, arguments as array
- `bind`: Return new function, doesn't execute

---

### Q10: What are Promises?

**Answer:**
A Promise is an object representing the eventual completion or failure of an asynchronous operation.

**States:**
```
┌────────────────────────────────────────┐
│           PROMISE STATES               │
│                                        │
│         Pending                        │
│            │                           │
│      ┌─────┴─────┐                    │
│      ▼           ▼                    │
│  Fulfilled    Rejected                 │
│     (resolved)  (error)                │
└────────────────────────────────────────┘
```

```javascript
// Creating a promise
const promise = new Promise((resolve, reject) => {
    setTimeout(() => {
        const success = true;

        if (success) {
            resolve("Success!");
        } else {
            reject("Failed!");
        }
    }, 1000);
});

// Using the promise
promise
    .then(result => console.log(result))
    .catch(error => console.log(error))
    .finally(() => console.log("Done"));
```

---

## Hard Questions

### Q11: Explain prototypal inheritance

**Answer:**
JavaScript uses prototypal inheritance where objects inherit from other objects.

```javascript
// Constructor function
function Animal(name) {
    this.name = name;
}

// Add method to prototype
Animal.prototype.speak = function() {
    console.log(`${this.name} makes a sound`);
};

// Create instance
const dog = new Animal("Dog");
dog.speak();  // "Dog makes a sound"

// Inheritance chain
function Dog(name) {
    Animal.call(this, name);
}

Dog.prototype = Object.create(Animal.prototype);
Dog.prototype.constructor = Dog;

Dog.prototype.bark = function() {
    console.log(`${this.name} barks`);
};

const myDog = new Dog("Buddy");
myDog.speak();  // Inherited from Animal
myDog.bark();   // Own method
```

**Diagram:**
```
┌────────────────────────────────────────┐
│      PROTOTYPE CHAIN                   │
│                                        │
│  myDog                                 │
│    │                                   │
│    └──► Dog.prototype                  │
│            │                           │
│            └──► Animal.prototype       │
│                    │                   │
│                    └──► Object.prototype│
│                            │           │
│                            └──► null   │
└────────────────────────────────────────┘
```

---

### Q12: What is debouncing and throttling?

**Answer:**

**Debouncing:** Execute function only after a certain time has passed since last call.

```javascript
function debounce(func, delay) {
    let timeoutId;

    return function(...args) {
        clearTimeout(timeoutId);

        timeoutId = setTimeout(() => {
            func.apply(this, args);
        }, delay);
    };
}

// Usage
const debouncedSearch = debounce((query) => {
    console.log("Searching:", query);
}, 300);

// Only searches after user stops typing for 300ms
input.addEventListener('input', (e) => {
    debouncedSearch(e.target.value);
});
```

**Throttling:** Execute function at most once per time period.

```javascript
function throttle(func, limit) {
    let inThrottle;

    return function(...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;

            setTimeout(() => {
                inThrottle = false;
            }, limit);
        }
    };
}

// Usage
const throttledResize = throttle(() => {
    console.log("Window resized");
}, 1000);

// Executes at most once per second
window.addEventListener('resize', throttledResize);
```

**Diagram:**
```
Time: ─────────────────────────────────►

Events: ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓

Debounce:                       ↓
(waits for quiet period)

Throttle:   ↓       ↓       ↓
(regular intervals)
```

---

### Q13: What is currying?

**Answer:**
Currying transforms a function with multiple arguments into a sequence of functions each taking a single argument.

```javascript
// Regular function
function add(a, b, c) {
    return a + b + c;
}

add(1, 2, 3);  // 6

// Curried version
function curriedAdd(a) {
    return function(b) {
        return function(c) {
            return a + b + c;
        };
    };
}

curriedAdd(1)(2)(3);  // 6

// Generic curry function
function curry(fn) {
    return function curried(...args) {
        if (args.length >= fn.length) {
            return fn.apply(this, args);
        } else {
            return function(...nextArgs) {
                return curried.apply(this, args.concat(nextArgs));
            };
        }
    };
}

// Usage
const curriedAdd2 = curry(add);

curriedAdd2(1)(2)(3);     // 6
curriedAdd2(1, 2)(3);     // 6
curriedAdd2(1)(2, 3);     // 6
```

---

### Q14: Explain event bubbling and capturing

**Answer:**

**Diagram:**
```
┌──────────────────────────────────────────┐
│         EVENT PROPAGATION                │
│                                          │
│  CAPTURING (top → target)                │
│    document                              │
│      ↓                                   │
│    body                                  │
│      ↓                                   │
│    div                                   │
│      ↓                                   │
│   button (target)                        │
│                                          │
│  BUBBLING (target → top)                 │
│   button (target)                        │
│      ↑                                   │
│    div                                   │
│      ↑                                   │
│    body                                  │
│      ↑                                   │
│    document                              │
└──────────────────────────────────────────┘
```

```javascript
// Bubbling (default)
element.addEventListener('click', handler);

// Capturing
element.addEventListener('click', handler, true);

// Stop propagation
element.addEventListener('click', (event) => {
    event.stopPropagation();  // Stops bubbling/capturing
});

// Prevent default and stop propagation
element.addEventListener('click', (event) => {
    event.preventDefault();      // Prevent default behavior
    event.stopPropagation();     // Stop propagation
});
```

---

### Q15: What is memoization?

**Answer:**
Memoization is an optimization technique that caches function results.

```javascript
function memoize(fn) {
    const cache = new Map();

    return function(...args) {
        const key = JSON.stringify(args);

        if (cache.has(key)) {
            console.log("From cache");
            return cache.get(key);
        }

        const result = fn.apply(this, args);
        cache.set(key, result);
        return result;
    };
}

// Example: Fibonacci
function fibonacci(n) {
    if (n <= 1) return n;
    return fibonacci(n - 1) + fibonacci(n - 2);
}

const memoizedFib = memoize(fibonacci);

console.time("First call");
console.log(memoizedFib(40));  // Slow
console.timeEnd("First call");

console.time("Second call");
console.log(memoizedFib(40));  // Fast (from cache)
console.timeEnd("Second call");
```

---

## Coding Challenges

### Challenge 1: Reverse a String (Easy)

```javascript
// Multiple solutions
function reverseString(str) {
    // Method 1: Array reverse
    return str.split('').reverse().join('');

    // Method 2: Loop
    let reversed = '';
    for (let i = str.length - 1; i >= 0; i--) {
        reversed += str[i];
    }
    return reversed;

    // Method 3: Reduce
    return str.split('').reduce((rev, char) => char + rev, '');
}

console.log(reverseString("hello"));  // "olleh"
```

---

### Challenge 2: Find Duplicates (Medium)

```javascript
function findDuplicates(arr) {
    const seen = new Set();
    const duplicates = new Set();

    for (const item of arr) {
        if (seen.has(item)) {
            duplicates.add(item);
        } else {
            seen.add(item);
        }
    }

    return Array.from(duplicates);
}

console.log(findDuplicates([1, 2, 3, 2, 4, 5, 3]));
// [2, 3]
```

---

### Challenge 3: Flatten Array (Hard)

```javascript
function flattenArray(arr) {
    // Method 1: Recursive
    function flatten(arr) {
        const result = [];

        for (const item of arr) {
            if (Array.isArray(item)) {
                result.push(...flatten(item));
            } else {
                result.push(item);
            }
        }

        return result;
    }

    // Method 2: Modern
    return arr.flat(Infinity);

    // Method 3: Reduce
    return arr.reduce((flat, item) => {
        return flat.concat(Array.isArray(item) ? flattenArray(item) : item);
    }, []);
}

const nested = [1, [2, [3, [4]], 5]];
console.log(flattenArray(nested));
// [1, 2, 3, 4, 5]
```

---

### Challenge 4: Deep Clone (Hard)

```javascript
function deepClone(obj) {
    // Handle primitives and null
    if (obj === null || typeof obj !== 'object') {
        return obj;
    }

    // Handle Date
    if (obj instanceof Date) {
        return new Date(obj.getTime());
    }

    // Handle Array
    if (Array.isArray(obj)) {
        return obj.map(item => deepClone(item));
    }

    // Handle Object
    const cloned = {};
    for (const key in obj) {
        if (obj.hasOwnProperty(key)) {
            cloned[key] = deepClone(obj[key]);
        }
    }

    return cloned;
}

const original = {
    name: "Alice",
    scores: [90, 85, 88],
    meta: {
        date: new Date(),
        tags: ["js", "web"]
    }
};

const cloned = deepClone(original);
cloned.scores.push(95);
console.log(original.scores);  // [90, 85, 88] (unchanged)
console.log(cloned.scores);    // [90, 85, 88, 95]
```

---

## System Design Questions

### Q16: Design a Todo List Application

**Answer:**

```javascript
class TodoList {
    constructor() {
        this.todos = [];
        this.idCounter = 1;
    }

    add(text) {
        const todo = {
            id: this.idCounter++,
            text,
            completed: false,
            createdAt: new Date()
        };

        this.todos.push(todo);
        return todo;
    }

    remove(id) {
        const index = this.todos.findIndex(todo => todo.id === id);
        if (index !== -1) {
            return this.todos.splice(index, 1)[0];
        }
        return null;
    }

    toggle(id) {
        const todo = this.todos.find(todo => todo.id === id);
        if (todo) {
            todo.completed = !todo.completed;
        }
        return todo;
    }

    filter(status) {
        if (status === 'completed') {
            return this.todos.filter(todo => todo.completed);
        } else if (status === 'active') {
            return this.todos.filter(todo => !todo.completed);
        }
        return this.todos;
    }

    clear() {
        this.todos = [];
    }
}
```

---

## Key Takeaways

✅ Understand core concepts deeply, not just syntax
✅ Practice explaining concepts clearly
✅ Know multiple solutions to problems
✅ Understand time/space complexity
✅ Can identify and fix common mistakes
✅ Know when to use different approaches

---

## Next Steps

🎉 **Congratulations!** You're prepared for interviews!

**Final phase:** [Phase 10: Problem-Solving Strategies](../phase-10-problem-solving/README.md)
