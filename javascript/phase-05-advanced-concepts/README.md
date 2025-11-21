# Phase 5: Advanced Concepts

## 📋 Table of Contents
1. [Scope](#scope)
2. [Hoisting](#hoisting)
3. [Closures](#closures)
4. [The 'this' Keyword](#the-this-keyword)
5. [Call, Apply, Bind](#call-apply-bind)
6. [Coding Examples](#coding-examples)

---

## Scope

### The Why
To control where variables can be accessed and prevent naming conflicts.

### The What
Scope determines the visibility/accessibility of variables.

### Types of Scope

```
┌─────────────────────────────────────────────┐
│            SCOPE HIERARCHY                  │
│                                             │
│  ┌───────────────────────────────────────┐ │
│  │       GLOBAL SCOPE                    │ │
│  │  (Accessible everywhere)              │ │
│  │                                       │ │
│  │  ┌─────────────────────────────────┐ │ │
│  │  │    FUNCTION SCOPE              │ │ │
│  │  │  (Accessible in function)      │ │ │
│  │  │                                │ │ │
│  │  │  ┌──────────────────────────┐  │ │ │
│  │  │  │   BLOCK SCOPE           │  │ │ │
│  │  │  │  (Accessible in block)  │  │ │ │
│  │  │  └──────────────────────────┘  │ │ │
│  │  └─────────────────────────────────┘ │ │
│  └───────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
```

```javascript
// Global scope
const globalVar = "I'm global";

function outerFunction() {
    // Function scope
    const functionVar = "I'm in function";

    if (true) {
        // Block scope
        const blockVar = "I'm in block";
        let blockLet = "Me too";

        console.log(globalVar);     // ✓ Accessible
        console.log(functionVar);   // ✓ Accessible
        console.log(blockVar);      // ✓ Accessible
    }

    console.log(blockVar);  // ❌ Error: not defined
}
```

**Scope Chain:**
```
┌──────────────────────────────────────────┐
│     Inner scope looks outward            │
│                                          │
│  function outer() {                      │
│      const x = 1;                        │
│                                          │
│      function inner() {                  │
│          const y = 2;                    │
│          console.log(x);  // ✓ Found x   │
│          console.log(y);  // ✓ Found y   │
│      }                                   │
│  }                                       │
│                                          │
│  Search order: inner → outer → global    │
└──────────────────────────────────────────┘
```

---

## Hoisting

### The Why
JavaScript moves declarations to the top of their scope before execution.

### The What
Variable and function declarations are "hoisted" (moved up).

```javascript
// What you write:
console.log(x);  // undefined (not error!)
var x = 5;

// How JavaScript interprets it:
var x;           // Declaration hoisted
console.log(x);  // undefined
x = 5;           // Assignment stays
```

**Diagram:**
```
┌────────────────────────────────────────────┐
│          HOISTING                          │
│                                            │
│  CODE:                INTERPRETED AS:      │
│                                            │
│  greet();            function greet() {    │
│                          return "Hi";      │
│  function greet() {  }                     │
│      return "Hi";                          │
│  }                   greet();              │
│                                            │
│  Function declarations are fully hoisted   │
└────────────────────────────────────────────┘
```

### Hoisting Rules

```javascript
// ✅ Function declaration - fully hoisted
greet();  // Works!

function greet() {
    return "Hello";
}

// ❌ Function expression - not hoisted
greet2();  // Error!

const greet2 = function() {
    return "Hello";
};

// ❌ Arrow function - not hoisted
greet3();  // Error!

const greet3 = () => "Hello";

// var - hoisted but undefined
console.log(a);  // undefined
var a = 5;

// let/const - hoisted but in "temporal dead zone"
console.log(b);  // Error: Cannot access before initialization
let b = 5;
```

---

## Closures

### The Why
To create private variables and data encapsulation.

### The What
A closure is a function that remembers variables from its outer scope.

```javascript
function outer() {
    const secret = "I'm private";

    function inner() {
        console.log(secret);  // Can access outer variable
    }

    return inner;
}

const myFunction = outer();
myFunction();  // "I'm private"
```

**Diagram:**
```
┌────────────────────────────────────────────────┐
│              CLOSURE                           │
│                                                │
│  function outer() {                            │
│      const secret = "hidden"; ◄────────┐      │
│                                        │      │
│      function inner() {                │      │
│          console.log(secret); ─────────┘      │
│      }                                         │
│                                                │
│      return inner;                             │
│  }                                             │
│                                                │
│  inner() "closes over" the variable secret     │
│  and keeps it alive even after outer() ends    │
└────────────────────────────────────────────────┘
```

### Practical Example: Counter

```javascript
function createCounter() {
    let count = 0;  // Private variable

    return {
        increment: function() {
            count++;
            return count;
        },
        decrement: function() {
            count--;
            return count;
        },
        getCount: function() {
            return count;
        }
    };
}

const counter = createCounter();
console.log(counter.increment());  // 1
console.log(counter.increment());  // 2
console.log(counter.decrement());  // 1
console.log(counter.getCount());   // 1
console.log(counter.count);        // undefined (private!)
```

### Loop Closure Issue

```javascript
// ❌ Common mistake
for (var i = 0; i < 3; i++) {
    setTimeout(function() {
        console.log(i);  // Prints: 3 3 3
    }, 1000);
}

// ✅ Solution 1: Use let
for (let i = 0; i < 3; i++) {
    setTimeout(function() {
        console.log(i);  // Prints: 0 1 2
    }, 1000);
}

// ✅ Solution 2: IIFE (Immediately Invoked Function Expression)
for (var i = 0; i < 3; i++) {
    (function(j) {
        setTimeout(function() {
            console.log(j);  // Prints: 0 1 2
        }, 1000);
    })(i);
}
```

---

## The 'this' Keyword

### The Why
To refer to the object that is executing the current function.

### The What
`this` refers to different things depending on how a function is called.

### Rules for 'this'

```javascript
// 1. Global context
console.log(this);  // Window (browser) or global (Node.js)

// 2. Object method
const person = {
    name: "Alice",
    greet: function() {
        console.log(this.name);  // "Alice"
    }
};
person.greet();  // this = person

// 3. Regular function
function showThis() {
    console.log(this);  // Window (or undefined in strict mode)
}
showThis();

// 4. Arrow function (inherits 'this' from parent)
const obj = {
    name: "Bob",
    greet: () => {
        console.log(this.name);  // undefined (this = global)
    }
};

// 5. Constructor function
function Person(name) {
    this.name = name;  // this = new object
}
const alice = new Person("Alice");

// 6. Event handler
button.addEventListener('click', function() {
    console.log(this);  // this = button element
});
```

**Diagram:**
```
┌────────────────────────────────────────────┐
│         'this' DETERMINATION               │
│                                            │
│  How was function called?                  │
│                                            │
│  new Function()      → new object          │
│  obj.method()        → obj                 │
│  func.call(obj)      → obj                 │
│  func()              → global/undefined    │
│  Arrow function      → parent scope        │
│  Event handler       → element             │
└────────────────────────────────────────────┘
```

### Arrow Functions and 'this'

```javascript
const obj = {
    name: "Alice",

    // Regular function
    regularGreet: function() {
        console.log(this.name);  // "Alice"

        setTimeout(function() {
            console.log(this.name);  // undefined ('this' changed!)
        }, 1000);
    },

    // Arrow function
    arrowGreet: function() {
        console.log(this.name);  // "Alice"

        setTimeout(() => {
            console.log(this.name);  // "Alice" (inherits 'this')
        }, 1000);
    }
};
```

---

## Call, Apply, Bind

### The Why
To explicitly control what `this` refers to.

### call() - Invoke with specific 'this'

```javascript
function greet(greeting, punctuation) {
    return `${greeting}, ${this.name}${punctuation}`;
}

const person = { name: "Alice" };

// call(thisArg, arg1, arg2, ...)
console.log(greet.call(person, "Hello", "!"));
// "Hello, Alice!"
```

### apply() - Like call, but array of arguments

```javascript
// apply(thisArg, [argsArray])
console.log(greet.apply(person, ["Hi", "?"]));
// "Hi, Alice?"
```

### bind() - Create new function with bound 'this'

```javascript
// bind(thisArg) - returns NEW function
const boundGreet = greet.bind(person);

console.log(boundGreet("Hey", "!!!"));
// "Hey, Alice!!!"

// Partial application
const helloAlice = greet.bind(person, "Hello");
console.log(helloAlice("!"));  // "Hello, Alice!"
```

**Diagram:**
```
┌────────────────────────────────────────────┐
│      CALL, APPLY, BIND                     │
│                                            │
│  func.call(obj, a, b)                      │
│      ↓                                     │
│  Execute immediately with obj as 'this'    │
│                                            │
│  func.apply(obj, [a, b])                   │
│      ↓                                     │
│  Execute immediately, args as array        │
│                                            │
│  func.bind(obj, a, b)                      │
│      ↓                                     │
│  Return NEW function with bound 'this'     │
│  (doesn't execute immediately)             │
└────────────────────────────────────────────┘
```

---

## Coding Examples

### Example 1: Module Pattern (Medium)

```javascript
const BankAccount = (function() {
    // Private variables
    let balance = 0;
    const transactions = [];

    // Private function
    function logTransaction(type, amount) {
        transactions.push({
            type,
            amount,
            date: new Date(),
            balance: balance
        });
    }

    // Public API
    return {
        deposit: function(amount) {
            if (amount > 0) {
                balance += amount;
                logTransaction('deposit', amount);
                return balance;
            }
            return 'Invalid amount';
        },

        withdraw: function(amount) {
            if (amount > 0 && amount <= balance) {
                balance -= amount;
                logTransaction('withdraw', amount);
                return balance;
            }
            return 'Insufficient funds or invalid amount';
        },

        getBalance: function() {
            return balance;
        },

        getTransactions: function() {
            return [...transactions];  // Return copy
        }
    };
})();

console.log(BankAccount.deposit(100));    // 100
console.log(BankAccount.withdraw(30));    // 70
console.log(BankAccount.getBalance());    // 70
console.log(BankAccount.balance);         // undefined (private!)
```

---

### Example 2: Curry Function (Hard)

```javascript
// Currying: Transform f(a, b, c) → f(a)(b)(c)

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

// Example usage
function add(a, b, c) {
    return a + b + c;
}

const curriedAdd = curry(add);

console.log(curriedAdd(1)(2)(3));     // 6
console.log(curriedAdd(1, 2)(3));     // 6
console.log(curriedAdd(1)(2, 3));     // 6
console.log(curriedAdd(1, 2, 3));     // 6
```

---

### Example 3: Memoization (Hard)

```javascript
// Cache function results for performance

function memoize(fn) {
    const cache = new Map();

    return function(...args) {
        const key = JSON.stringify(args);

        if (cache.has(key)) {
            console.log('From cache');
            return cache.get(key);
        }

        const result = fn.apply(this, args);
        cache.set(key, result);
        return result;
    };
}

// Expensive function
function fibonacci(n) {
    if (n <= 1) return n;
    return fibonacci(n - 1) + fibonacci(n - 2);
}

const memoizedFib = memoize(fibonacci);

console.log(memoizedFib(40));  // Slow first time
console.log(memoizedFib(40));  // Fast from cache
```

---

## Key Takeaways

✅ Understand scope: global, function, block
✅ Be aware of hoisting behavior
✅ Use closures for private data
✅ Know how 'this' works in different contexts
✅ Arrow functions inherit 'this' from parent
✅ Use call/apply/bind to control 'this'

---

## Next Steps

🎉 **Congratulations!** You've mastered advanced concepts!

**Ready for more?** Move on to [Phase 6: Asynchronous JavaScript](../phase-06-async-javascript/README.md)
