/**
 * ============================================================================
 * PHASE 5: FUNCTION DECLARATIONS & EXPRESSIONS
 * ============================================================================
 */

// ============================================================================
// SECTION 1: FUNCTION DECLARATION
// ============================================================================

/**
 * WHY: Define reusable blocks of code
 * WHAT: function name(params) { body }
 * HOW: Hoisted completely - can be called before declaration
 */

// Function declaration
function greet(name) {
    return `Hello, ${name}!`;
}

console.log(greet("Alice")); // "Hello, Alice!"

/**
 * HOISTING: Function declarations are hoisted completely
 * WHY: JavaScript processes declarations in two phases
 * Phase 1: Memory allocation (hoisting)
 * Phase 2: Execution
 */

// This works! Function is hoisted
console.log(add(2, 3)); // 5

function add(a, b) {
    return a + b;
}

// ============================================================================
// SECTION 2: FUNCTION EXPRESSION
// ============================================================================

/**
 * WHY: Assign function to variable, pass as argument
 * WHAT: const name = function(params) { body }
 * HOW: NOT hoisted - must define before use
 */

// Anonymous function expression
const multiply = function(a, b) {
    return a * b;
};

console.log(multiply(3, 4)); // 12

// Named function expression
const factorial = function fact(n) {
    if (n <= 1) return 1;
    return n * fact(n - 1); // Can reference itself by name
};

console.log(factorial(5)); // 120
// console.log(fact(5)); // ReferenceError: fact is not defined (name is local)

/**
 * WHY NAMED FUNCTION EXPRESSIONS:
 * 1. Better stack traces for debugging
 * 2. Self-reference for recursion
 * 3. Self-documenting code
 */

// Function expressions are NOT hoisted
// console.log(subtract(5, 3)); // TypeError: subtract is not a function

const subtract = function(a, b) {
    return a - b;
};

// ============================================================================
// SECTION 3: PARAMETERS AND ARGUMENTS
// ============================================================================

/**
 * Parameters: Variables in function definition
 * Arguments: Values passed when calling function
 */

function example(param1, param2) {  // param1, param2 are parameters
    console.log(arguments);  // arguments object (array-like)
    console.log(arguments.length);
}

example("a", "b", "c");  // "a", "b", "c" are arguments
// arguments: { 0: 'a', 1: 'b', 2: 'c' }
// arguments.length: 3

/**
 * ARGUMENTS OBJECT:
 * - Array-like (has length, indexed access)
 * - NOT a real array (no map, filter, etc.)
 * - Deprecated in modern code - use rest parameters
 */

// Converting arguments to array (old way)
function oldWay() {
    const args = Array.prototype.slice.call(arguments);
    // or: const args = [].slice.call(arguments);
    return args;
}

// ============================================================================
// SECTION 4: DEFAULT PARAMETERS (ES6)
// ============================================================================

/**
 * WHY: Provide fallback values for missing arguments
 * WHAT: function(param = defaultValue) { }
 * HOW: Evaluated when argument is undefined
 */

// Old way (before ES6)
function greetOld(name) {
    name = name || "Guest";  // Problem: "" becomes "Guest"
    return `Hello, ${name}`;
}

// ES6 default parameters
function greetNew(name = "Guest") {
    return `Hello, ${name}`;
}

console.log(greetNew());         // "Hello, Guest"
console.log(greetNew("Alice"));  // "Hello, Alice"
console.log(greetNew(""));       // "Hello, " (empty string is valid!)

// Default with null vs undefined
console.log(greetNew(undefined)); // "Hello, Guest" (triggers default)
console.log(greetNew(null));      // "Hello, null" (null doesn't trigger default)

/**
 * WHY null DOESN'T TRIGGER DEFAULT:
 * - null is an intentional "no value"
 * - undefined means "not provided"
 * - Default parameters only replace undefined
 */

// Complex default values
function createUser(
    name,
    age = 18,
    role = "user",
    createdAt = new Date()  // Evaluated at call time!
) {
    return { name, age, role, createdAt };
}

// Default can reference earlier parameters
function greetWithTitle(name, title = name.toUpperCase()) {
    return `${title}: ${name}`;
}

console.log(greetWithTitle("alice")); // "ALICE: alice"

// ============================================================================
// SECTION 5: REST PARAMETERS (ES6)
// ============================================================================

/**
 * WHY: Collect multiple arguments into array
 * WHAT: function(...args) { }
 * HOW: Must be last parameter
 *
 * WHY BETTER THAN arguments:
 * - Real array (has all array methods)
 * - Clearly named
 * - Works in arrow functions
 */

function sum(...numbers) {
    return numbers.reduce((total, n) => total + n, 0);
}

console.log(sum(1, 2, 3, 4, 5)); // 15

// Rest must be last
function logArgs(first, second, ...rest) {
    console.log("First:", first);
    console.log("Second:", second);
    console.log("Rest:", rest);
}

logArgs(1, 2, 3, 4, 5);
// First: 1
// Second: 2
// Rest: [3, 4, 5]

// Cannot have multiple rest parameters
// function bad(...a, ...b) {} // SyntaxError

// ============================================================================
// SECTION 6: RETURN VALUES
// ============================================================================

/**
 * Functions return undefined if no return statement
 * Or if return has no value
 */

function noReturn() {
    console.log("No return");
}
console.log(noReturn()); // undefined

function emptyReturn() {
    return;  // Returns undefined
}
console.log(emptyReturn()); // undefined

// Return exits function immediately
function checkAge(age) {
    if (age < 0) {
        return "Invalid age";  // Early return
    }
    return age >= 18 ? "Adult" : "Minor";
}

// Returning multiple values (using object or array)
function getMinMax(arr) {
    return {
        min: Math.min(...arr),
        max: Math.max(...arr)
    };
}

const { min, max } = getMinMax([1, 5, 3, 9, 2]);
console.log(min, max); // 1, 9

// ============================================================================
// SECTION 7: IIFE (Immediately Invoked Function Expression)
// ============================================================================

/**
 * WHY: Create private scope, avoid global pollution
 * WHAT: Function that runs immediately after definition
 * HOW: (function() { })() or (function() { }())
 *
 * HISTORICAL USE:
 * - Module pattern before ES6 modules
 * - Private variables before let/const
 *
 * MODERN USE:
 * - Still useful for one-time setup
 * - Async IIFE for top-level await
 */

// Basic IIFE
(function() {
    const privateVar = "I'm private";
    console.log(privateVar);
})();
// privateVar is not accessible here

// IIFE with parameters
(function(name) {
    console.log(`Hello, ${name}`);
})("World");

// Named IIFE (for better stack traces)
(function init() {
    console.log("Initializing...");
})();

// Arrow function IIFE
(() => {
    console.log("Arrow IIFE");
})();

// Async IIFE (for top-level await)
(async () => {
    const data = await fetchData();
    console.log(data);
})();

/**
 * WHY THE PARENTHESES:
 * Without them, `function() {}()` is a syntax error
 * Parser sees function declaration, not expression
 * Wrapping in () makes it an expression
 */

// Alternative syntax (less common)
!function() { console.log("Bang IIFE"); }();
+function() { console.log("Plus IIFE"); }();
void function() { console.log("Void IIFE"); }();

// ============================================================================
// SECTION 8: FIRST-CLASS FUNCTIONS
// ============================================================================

/**
 * WHY THIS MATTERS:
 * Functions are values - can be passed around like any data
 * This enables functional programming patterns
 */

// Function as variable
const operation = add;
console.log(operation(2, 3)); // 5

// Function as argument (callback)
function processNumbers(a, b, callback) {
    return callback(a, b);
}

console.log(processNumbers(5, 3, add));      // 8
console.log(processNumbers(5, 3, multiply)); // 15

// Function returning function (higher-order function)
function createMultiplier(factor) {
    return function(number) {
        return number * factor;
    };
}

const double = createMultiplier(2);
const triple = createMultiplier(3);

console.log(double(5)); // 10
console.log(triple(5)); // 15

// ============================================================================
// SECTION 9: COMMON INTERVIEW QUESTIONS
// ============================================================================

/**
 * Q1: What's the output?
 */
console.log(foo()); // "foo"
console.log(bar()); // TypeError: bar is not a function

function foo() {
    return "foo";
}

var bar = function() {
    return "bar";
};

/**
 * EXPLANATION:
 * - foo is hoisted completely (declaration)
 * - bar is hoisted as undefined (var), assigned function later
 */

/**
 * Q2: What's the output?
 */
function test(a, b = a * 2, c = b + 1) {
    return [a, b, c];
}

console.log(test(2));       // [2, 4, 5]
console.log(test(2, 3));    // [2, 3, 4]
console.log(test(2, 3, 4)); // [2, 3, 4]

/**
 * Q3: Implement a function that returns a function
 */
function counter(start = 0) {
    let count = start;
    return function() {
        return count++;
    };
}

const myCounter = counter(10);
console.log(myCounter()); // 10
console.log(myCounter()); // 11
console.log(myCounter()); // 12

/**
 * Q4: What's wrong with this code?
 */
function greetWrong(name = "Guest", greeting) {
    return `${greeting}, ${name}`;
}

console.log(greetWrong("Alice")); // "undefined, Alice"
// Problem: Can't skip arguments, need to pass undefined explicitly
console.log(greetWrong(undefined, "Hello")); // "Hello, Guest"

// Better: Put required params first
function greetRight(greeting, name = "Guest") {
    return `${greeting}, ${name}`;
}
