/**
 * ============================================================================
 * PHASE 23: CLOSURES, SCOPE & THIS
 * ============================================================================
 *
 * INTERVIEW IMPORTANCE: Critical for senior positions
 * These are deep conceptual questions that show true understanding
 */

// ============================================================================
// SECTION 1: UNDERSTANDING CLOSURES
// ============================================================================

/**
 * CLOSURE: A function that remembers and accesses variables from its
 * lexical scope even when executing outside that scope.
 *
 * WHY: Closures enable data privacy, function factories, and callbacks
 * HOW: Functions carry a reference to their lexical environment
 */

// Basic closure example
function outer() {
    const message = "Hello from outer";  // Enclosed variable

    function inner() {
        console.log(message);  // Accesses outer's variable
    }

    return inner;  // Return the inner function
}

const closureFunc = outer();  // outer() has finished executing
closureFunc();  // But inner() still has access to 'message'
// Output: "Hello from outer"

/**
 * WHY THIS WORKS:
 * - inner() was created inside outer()
 * - inner() has a reference to outer()'s environment
 * - This reference persists even after outer() returns
 * - This is the closure
 */

// ============================================================================
// SECTION 2: PRACTICAL CLOSURE PATTERNS
// ============================================================================

// Pattern 1: Data Privacy / Encapsulation
function createBankAccount(initialBalance) {
    let balance = initialBalance;  // Private variable

    return {
        deposit(amount) {
            if (amount > 0) {
                balance += amount;
                return balance;
            }
        },
        withdraw(amount) {
            if (amount > 0 && amount <= balance) {
                balance -= amount;
                return balance;
            }
            return "Insufficient funds";
        },
        getBalance() {
            return balance;
        }
    };
}

const account = createBankAccount(100);
console.log(account.getBalance()); // 100
account.deposit(50);
console.log(account.getBalance()); // 150
// account.balance is undefined - truly private!

// Pattern 2: Function Factory
function createMultiplier(factor) {
    return function(number) {
        return number * factor;  // Closes over 'factor'
    };
}

const double = createMultiplier(2);
const triple = createMultiplier(3);
console.log(double(5));  // 10
console.log(triple(5));  // 15

// Pattern 3: Memoization
function memoize(fn) {
    const cache = {};  // Closed over by returned function

    return function(...args) {
        const key = JSON.stringify(args);
        if (key in cache) {
            console.log("From cache");
            return cache[key];
        }
        const result = fn.apply(this, args);
        cache[key] = result;
        return result;
    };
}

const factorial = memoize(function(n) {
    if (n <= 1) return 1;
    return n * factorial(n - 1);
});

console.log(factorial(5));  // Calculates
console.log(factorial(5));  // From cache

// Pattern 4: Partial Application
function partial(fn, ...presetArgs) {
    return function(...laterArgs) {
        return fn(...presetArgs, ...laterArgs);
    };
}

const greet = (greeting, name) => `${greeting}, ${name}!`;
const sayHello = partial(greet, "Hello");
console.log(sayHello("Alice"));  // "Hello, Alice!"

// Pattern 5: Once Function
function once(fn) {
    let called = false;
    let result;

    return function(...args) {
        if (!called) {
            called = true;
            result = fn.apply(this, args);
        }
        return result;
    };
}

const initializeOnce = once(() => {
    console.log("Initializing...");
    return "Initialized";
});

initializeOnce();  // "Initializing..."
initializeOnce();  // (nothing, returns cached result)

// ============================================================================
// SECTION 3: CLOSURE GOTCHAS
// ============================================================================

// Classic loop problem
for (var i = 0; i < 3; i++) {
    setTimeout(function() {
        console.log(i);  // 3, 3, 3 (not 0, 1, 2)
    }, 100);
}

/**
 * WHY: var is function-scoped, same 'i' for all iterations
 * All callbacks close over the same 'i'
 * By the time callbacks run, i is 3
 */

// Solution 1: Use let (block-scoped)
for (let i = 0; i < 3; i++) {
    setTimeout(function() {
        console.log(i);  // 0, 1, 2
    }, 100);
}

// Solution 2: IIFE (before ES6)
for (var i = 0; i < 3; i++) {
    (function(j) {
        setTimeout(function() {
            console.log(j);  // 0, 1, 2
        }, 100);
    })(i);
}

// Solution 3: Extra function
for (var i = 0; i < 3; i++) {
    setTimeout(createLogger(i), 100);
}

function createLogger(num) {
    return function() {
        console.log(num);
    };
}

// ============================================================================
// SECTION 4: THE 'THIS' KEYWORD
// ============================================================================

/**
 * 'this' is determined by HOW a function is called, not where it's defined
 * (except for arrow functions)
 *
 * FOUR RULES (in order of precedence):
 * 1. new binding: this = newly created object
 * 2. Explicit binding: call/apply/bind
 * 3. Implicit binding: method call obj.fn()
 * 4. Default binding: undefined (strict) or global
 */

// Rule 4: Default binding
function showThis() {
    console.log(this);
}
showThis();  // undefined (strict mode) or window/global

// Rule 3: Implicit binding
const obj = {
    name: "Object",
    showThis: function() {
        console.log(this.name);
    }
};
obj.showThis();  // "Object"

// But 'this' can be lost!
const extracted = obj.showThis;
extracted();  // undefined (lost implicit binding)

// Rule 2: Explicit binding
const anotherObj = { name: "Another" };
obj.showThis.call(anotherObj);  // "Another"

// Rule 1: new binding
function Person(name) {
    this.name = name;
}
const person = new Person("Alice");
console.log(person.name);  // "Alice"

// ============================================================================
// SECTION 5: EXPLICIT BINDING (call, apply, bind)
// ============================================================================

function introduce(greeting, punctuation) {
    return `${greeting}, I'm ${this.name}${punctuation}`;
}

const alice = { name: "Alice" };
const bob = { name: "Bob" };

// call - invoke immediately with individual args
console.log(introduce.call(alice, "Hello", "!"));  // "Hello, I'm Alice!"

// apply - invoke immediately with array of args
console.log(introduce.apply(bob, ["Hi", "?"]));  // "Hi, I'm Bob?"

// bind - return new function with bound this
const aliceIntro = introduce.bind(alice);
console.log(aliceIntro("Hey", "..."));  // "Hey, I'm Alice..."

// Partial application with bind
const aliceHello = introduce.bind(alice, "Hello");
console.log(aliceHello("!!!"));  // "Hello, I'm Alice!!!"

// ============================================================================
// SECTION 6: ARROW FUNCTIONS AND THIS
// ============================================================================

/**
 * Arrow functions DON'T have their own 'this'
 * They inherit 'this' from the enclosing lexical scope
 * Cannot be changed with call/apply/bind
 */

const objWithArrow = {
    name: "Object",

    // Regular function - has own 'this'
    regular: function() {
        console.log("Regular:", this.name);
    },

    // Arrow function - inherits 'this' from enclosing scope
    arrow: () => {
        console.log("Arrow:", this.name);  // 'this' is outer scope
    },

    // Method with nested function problem
    nestedProblem: function() {
        function inner() {
            console.log(this.name);  // undefined (default binding)
        }
        inner();
    },

    // Solution: Arrow function preserves 'this'
    nestedSolution: function() {
        const inner = () => {
            console.log(this.name);  // "Object" (lexical this)
        };
        inner();
    }
};

objWithArrow.regular();  // "Object"
objWithArrow.arrow();    // undefined (arrow doesn't get object's this)
objWithArrow.nestedProblem();   // undefined
objWithArrow.nestedSolution();  // "Object"

// ============================================================================
// SECTION 7: CURRYING
// ============================================================================

/**
 * CURRYING: Transform function of multiple args into sequence of functions
 * f(a, b, c) -> f(a)(b)(c)
 */

// Manual currying
const add = a => b => c => a + b + c;
console.log(add(1)(2)(3));  // 6

// Generic curry function
function curry(fn) {
    return function curried(...args) {
        if (args.length >= fn.length) {
            return fn.apply(this, args);
        }
        return function(...moreArgs) {
            return curried.apply(this, [...args, ...moreArgs]);
        };
    };
}

function sum(a, b, c) {
    return a + b + c;
}

const curriedSum = curry(sum);
console.log(curriedSum(1)(2)(3));     // 6
console.log(curriedSum(1, 2)(3));     // 6
console.log(curriedSum(1)(2, 3));     // 6

// ============================================================================
// SECTION 8: COMMON INTERVIEW QUESTIONS
// ============================================================================

/**
 * Q1: What's the output?
 */
var x = 10;
function foo() {
    console.log(x);
    var x = 20;
}
foo();
// Output: undefined (hoisting - var x is hoisted, not its value)

/**
 * Q2: What's the output?
 */
const obj2 = {
    a: 10,
    b: () => console.log(this.a, this),
    c: function() {
        console.log(this.a, this);
    }
};
obj2.b();  // undefined, window/global
obj2.c();  // 10, obj2

/**
 * Q3: Create a counter with closure
 */
function createCounter() {
    let count = 0;
    return {
        increment: () => ++count,
        decrement: () => --count,
        getCount: () => count
    };
}

/**
 * Q4: What's the output?
 */
function foo2() {
    let a = b = 0;  // b is global! (no let/var)
    a++;
    return a;
}
foo2();
console.log(typeof a);  // "undefined"
console.log(typeof b);  // "number" (b leaked to global!)

/**
 * Q5: Fix this code
 */
const user = {
    name: "Alice",
    friends: ["Bob", "Charlie"],
    printFriends() {
        this.friends.forEach(function(friend) {
            console.log(this.name + " knows " + friend);  // this is undefined!
        });
    }
};

// Fix 1: Arrow function
const userFixed1 = {
    name: "Alice",
    friends: ["Bob", "Charlie"],
    printFriends() {
        this.friends.forEach(friend => {
            console.log(this.name + " knows " + friend);
        });
    }
};

// Fix 2: thisArg in forEach
const userFixed2 = {
    name: "Alice",
    friends: ["Bob", "Charlie"],
    printFriends() {
        this.friends.forEach(function(friend) {
            console.log(this.name + " knows " + friend);
        }, this);  // Pass this as second argument
    }
};

/**
 * Q6: Implement compose/pipe
 */
const compose = (...fns) => x => fns.reduceRight((acc, fn) => fn(acc), x);
const pipe = (...fns) => x => fns.reduce((acc, fn) => fn(acc), x);

const add1 = x => x + 1;
const multiply2 = x => x * 2;

console.log(pipe(add1, multiply2)(5));     // 12 ((5+1)*2)
console.log(compose(add1, multiply2)(5));  // 11 ((5*2)+1)

/**
 * Q7: What's the output?
 */
var a = 1;
function outer() {
    var a = 2;
    function inner() {
        a++;
        var a = 3;
        console.log(a);
    }
    inner();
    console.log(a);
}
outer();
console.log(a);
// Output: 3, 2, 1
// inner's var a is hoisted, so a++ increments undefined -> NaN, then a = 3

/**
 * Q8: Create private methods with closure
 */
const Calculator = (function() {
    // Private
    let history = [];

    function addToHistory(operation) {
        history.push(operation);
    }

    // Public
    return {
        add(a, b) {
            const result = a + b;
            addToHistory(`${a} + ${b} = ${result}`);
            return result;
        },
        getHistory() {
            return [...history];
        }
    };
})();
