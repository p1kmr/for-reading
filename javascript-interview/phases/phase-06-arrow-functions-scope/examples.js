/**
 * ============================================================================
 * PHASE 6: ARROW FUNCTIONS & SCOPE
 * ============================================================================
 */

// ============================================================================
// SECTION 1: ARROW FUNCTION SYNTAX
// ============================================================================

/**
 * WHY ARROW FUNCTIONS:
 * 1. Shorter syntax
 * 2. Lexical this binding
 * 3. Better for callbacks
 */

// Basic arrow function
const add = (a, b) => {
    return a + b;
};

// Implicit return (single expression)
const addShort = (a, b) => a + b;

// Single parameter (parentheses optional)
const double = x => x * 2;

// No parameters (parentheses required)
const greet = () => "Hello!";

// Returning object literal (wrap in parentheses)
const createUser = (name, age) => ({ name, age });
// Without parentheses: { name, age } would be interpreted as function body

console.log(createUser("Alice", 25)); // { name: "Alice", age: 25 }

// Multiline with explicit return
const calculate = (a, b) => {
    const sum = a + b;
    const product = a * b;
    return { sum, product };
};

// ============================================================================
// SECTION 2: LEXICAL THIS BINDING
// ============================================================================

/**
 * WHY THIS IS IMPORTANT:
 * Arrow functions don't have their own 'this'
 * They inherit 'this' from enclosing scope
 *
 * PROBLEM WITH REGULAR FUNCTIONS:
 * 'this' depends on HOW function is called
 */

// Problem with regular functions
const user = {
    name: "Alice",
    friends: ["Bob", "Charlie"],

    printFriendsBad: function() {
        this.friends.forEach(function(friend) {
            // 'this' is undefined (strict mode) or window (non-strict)
            console.log(`${this.name} is friends with ${friend}`);
        });
    },

    // Solution 1: Arrow function
    printFriendsArrow: function() {
        this.friends.forEach(friend => {
            // 'this' is inherited from printFriendsArrow
            console.log(`${this.name} is friends with ${friend}`);
        });
    },

    // Solution 2: Save this reference (old way)
    printFriendsSelf: function() {
        const self = this;
        this.friends.forEach(function(friend) {
            console.log(`${self.name} is friends with ${friend}`);
        });
    },

    // Solution 3: Bind (old way)
    printFriendsBind: function() {
        this.friends.forEach(function(friend) {
            console.log(`${this.name} is friends with ${friend}`);
        }.bind(this));
    }
};

// ============================================================================
// SECTION 3: WHEN NOT TO USE ARROW FUNCTIONS
// ============================================================================

/**
 * Arrow functions should NOT be used when:
 * 1. Object methods (need own 'this')
 * 2. Event handlers (need 'this' to be element)
 * 3. Constructors (can't use 'new')
 * 4. When you need 'arguments' object
 */

// BAD: Arrow function as object method
const badObj = {
    value: 42,
    getValue: () => this.value  // 'this' is NOT badObj!
};
console.log(badObj.getValue()); // undefined

// GOOD: Regular function as object method
const goodObj = {
    value: 42,
    getValue() {  // Shorthand method syntax
        return this.value;
    }
};
console.log(goodObj.getValue()); // 42

// BAD: Arrow function in event handler
// button.addEventListener('click', () => {
//     console.log(this);  // 'this' is NOT the button
// });

// GOOD: Regular function in event handler
// button.addEventListener('click', function() {
//     console.log(this);  // 'this' is the button
// });

// BAD: Arrow function as constructor
// const Person = (name) => {
//     this.name = name;  // Error!
// };
// const p = new Person("Alice"); // TypeError: Person is not a constructor

// GOOD: Regular function as constructor
function Person(name) {
    this.name = name;
}
const p = new Person("Alice");

// BAD: Arrow function when you need arguments
const badArguments = () => {
    // console.log(arguments);  // ReferenceError
};

// GOOD: Use rest parameters instead
const goodArguments = (...args) => {
    console.log(args);  // Real array!
};

// ============================================================================
// SECTION 4: SCOPE TYPES
// ============================================================================

/**
 * THREE TYPES OF SCOPE:
 * 1. Global scope - accessible everywhere
 * 2. Function scope - var, function declarations
 * 3. Block scope - let, const
 */

// Global scope
var globalVar = "I'm global";
let globalLet = "I'm also global";

function demonstrateScope() {
    // Function scope
    var functionVar = "I'm function-scoped";

    if (true) {
        // Block scope
        var blockVar = "I'm function-scoped (var ignores blocks)";
        let blockLet = "I'm block-scoped";
        const blockConst = "I'm also block-scoped";

        console.log(blockLet);   // Works
        console.log(blockConst); // Works
    }

    console.log(blockVar);   // Works (var is function-scoped)
    // console.log(blockLet);   // ReferenceError
    // console.log(blockConst); // ReferenceError
}

// ============================================================================
// SECTION 5: LEXICAL SCOPING
// ============================================================================

/**
 * LEXICAL SCOPING (Static Scoping):
 * Scope is determined by where code is written, not where it's called
 *
 * WHY: Predictable variable lookup
 * HOW: Nested functions have access to outer scope
 */

const outerVar = "outer";

function outer() {
    const middleVar = "middle";

    function inner() {
        const innerVar = "inner";

        // Can access all outer scopes
        console.log(outerVar);  // "outer"
        console.log(middleVar); // "middle"
        console.log(innerVar);  // "inner"
    }

    inner();
    // console.log(innerVar); // ReferenceError
}

// Scope chain lookup
function scopeChainDemo() {
    const a = 1;

    function level1() {
        const b = 2;

        function level2() {
            const c = 3;

            // JavaScript looks up the scope chain
            console.log(a); // Found in scopeChainDemo
            console.log(b); // Found in level1
            console.log(c); // Found here
        }

        level2();
    }

    level1();
}

// ============================================================================
// SECTION 6: VARIABLE SHADOWING
// ============================================================================

/**
 * SHADOWING: Inner scope variable hides outer scope variable
 */

const value = "global";

function shadowDemo() {
    const value = "function"; // Shadows global

    if (true) {
        const value = "block"; // Shadows function
        console.log(value);    // "block"
    }

    console.log(value); // "function"
}

console.log(value); // "global"

/**
 * BEST PRACTICE:
 * Avoid shadowing - use different variable names
 * Makes code harder to understand
 */

// ============================================================================
// SECTION 7: CLOSURES (Introduction)
// ============================================================================

/**
 * CLOSURE: Function that remembers its lexical scope
 * Even when executed outside that scope
 * (Deep dive in Phase 23)
 */

function createCounter() {
    let count = 0;  // Private variable

    return function() {
        count++;    // Closes over 'count'
        return count;
    };
}

const counter = createCounter();
console.log(counter()); // 1
console.log(counter()); // 2
console.log(counter()); // 3

// Each call to createCounter creates new closure
const counter2 = createCounter();
console.log(counter2()); // 1 (independent count)

// ============================================================================
// SECTION 8: PRACTICAL EXAMPLES
// ============================================================================

// Array methods with arrow functions
const numbers = [1, 2, 3, 4, 5];

const doubled = numbers.map(n => n * 2);
const evens = numbers.filter(n => n % 2 === 0);
const sum = numbers.reduce((acc, n) => acc + n, 0);

// Chaining with arrow functions
const result = numbers
    .filter(n => n > 2)
    .map(n => n * 2)
    .reduce((acc, n) => acc + n, 0);

console.log(result); // 24 (3*2 + 4*2 + 5*2)

// Sorting with arrow functions
const people = [
    { name: "Alice", age: 30 },
    { name: "Bob", age: 25 },
    { name: "Charlie", age: 35 }
];

// Sort by age
const sortedByAge = [...people].sort((a, b) => a.age - b.age);

// Currying with arrow functions
const multiply = a => b => c => a * b * c;
console.log(multiply(2)(3)(4)); // 24

// ============================================================================
// SECTION 9: INTERVIEW QUESTIONS
// ============================================================================

/**
 * Q1: What's the output?
 */
const obj = {
    name: "Object",
    regularFunc: function() {
        return this.name;
    },
    arrowFunc: () => {
        return this.name;
    }
};

console.log(obj.regularFunc()); // "Object"
console.log(obj.arrowFunc());   // undefined (or global name)

/**
 * Q2: Fix this code
 */
class Timer {
    constructor() {
        this.seconds = 0;
    }

    // BAD: 'this' is lost
    startBad() {
        setInterval(function() {
            this.seconds++;  // 'this' is undefined
        }, 1000);
    }

    // GOOD: Arrow function preserves 'this'
    startGood() {
        setInterval(() => {
            this.seconds++;  // 'this' is Timer instance
        }, 1000);
    }
}

/**
 * Q3: What's the output?
 */
var x = 1;
function outer() {
    var x = 2;
    function inner() {
        var x = 3;
        console.log(x);
    }
    inner();
    console.log(x);
}
outer();
console.log(x);
// Output: 3, 2, 1 (scope chain)

/**
 * Q4: Can you convert this to arrow function?
 */
const calculator = {
    value: 0,
    add: function(n) {
        this.value += n;
        return this;
    }
};

// NO! Arrow function won't have correct 'this'
// Must use regular function for object methods

/**
 * Q5: What's the difference?
 */
const arr1 = [1, 2, 3].map(function(x) { return x * 2; });
const arr2 = [1, 2, 3].map(x => x * 2);
// Same result, arrow function is just shorter

const arr3 = [1, 2, 3].map(x => { x * 2 });
// Returns [undefined, undefined, undefined]
// Missing return! Braces require explicit return
