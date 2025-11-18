/**
 * ============================================================================
 * PHASE 1: VARIABLES, TYPES & FUNDAMENTALS
 * ============================================================================
 *
 * This file contains comprehensive examples covering JavaScript fundamentals.
 * Each section includes:
 * - WHY: Purpose and real-world applications
 * - WHAT: Concept explanation
 * - HOW: Implementation details
 * - WHY NOT OTHER WAY: Alternative approaches and trade-offs
 */

// ============================================================================
// SECTION 1: VARIABLE DECLARATIONS (var, let, const)
// ============================================================================

/**
 * WHY: Variables store data that our program needs to work with.
 * WHAT: JavaScript provides three keywords for variable declaration.
 * HOW: Each has different scoping and hoisting behavior.
 *
 * INTERVIEW TIP: Understanding the differences between var, let, and const
 * is one of the most common interview questions. Know the nuances!
 */

// -----------------------------------------------------------------------------
// 1.1 VAR - The Legacy Way (ES5)
// -----------------------------------------------------------------------------

/**
 * WHY USE var (historically):
 * - Only option before ES6 (2015)
 * - Function-scoped, which was useful for certain patterns
 *
 * WHY NOT USE var (modern code):
 * - Function scope leads to unexpected bugs
 * - Hoisting with 'undefined' causes confusion
 * - Can be redeclared, leading to accidental overwrites
 * - No temporal dead zone protection
 */

// Example 1.1.1: var is function-scoped, not block-scoped
function varScopeExample() {
    if (true) {
        var x = 10; // This is scoped to the FUNCTION, not the if-block
    }
    console.log(x); // Output: 10 (accessible outside if-block!)

    // WHY THIS HAPPENS:
    // var doesn't respect block boundaries (if, for, while)
    // It only respects function boundaries

    // WHY THIS IS PROBLEMATIC:
    // Variables "leak" out of blocks, causing unexpected behavior
}

// Example 1.1.2: var hoisting
/**
 * HOISTING: JavaScript moves variable declarations to the top of their scope
 * BUT only the declaration, not the initialization
 */
function varHoistingExample() {
    console.log(hoistedVar); // Output: undefined (not ReferenceError!)
    var hoistedVar = "I'm hoisted";
    console.log(hoistedVar); // Output: "I'm hoisted"

    // WHAT JAVASCRIPT ACTUALLY DOES (conceptually):
    // var hoistedVar;           // Declaration hoisted to top
    // console.log(hoistedVar);  // undefined
    // hoistedVar = "I'm hoisted"; // Assignment stays in place
    // console.log(hoistedVar);  // "I'm hoisted"

    // WHY THIS IS CONFUSING:
    // You can use a variable before declaring it, which is counterintuitive
}

// Example 1.1.3: var can be redeclared (dangerous!)
var userName = "Alice";
var userName = "Bob"; // No error! This silently overwrites
console.log(userName); // Output: "Bob"

// WHY THIS IS DANGEROUS:
// In large codebases, you might accidentally redeclare a variable
// and overwrite important data without any warning

// -----------------------------------------------------------------------------
// 1.2 LET - The Modern Way (ES6+)
// -----------------------------------------------------------------------------

/**
 * WHY USE let:
 * - Block-scoped (respects if, for, while blocks)
 * - Cannot be redeclared (prevents accidental overwrites)
 * - Temporal Dead Zone provides better error messages
 *
 * WHEN TO USE let:
 * - When you need to reassign the variable later
 * - Loop counters
 * - Values that change over time
 */

// Example 1.2.1: let is block-scoped
function letScopeExample() {
    if (true) {
        let y = 20; // This is scoped to the if-block ONLY
    }
    // console.log(y); // ReferenceError: y is not defined

    // WHY THIS IS BETTER:
    // Variables stay contained in their logical blocks
    // Prevents accidental access to variables outside their intended scope
}

// Example 1.2.2: let and the Temporal Dead Zone (TDZ)
/**
 * TEMPORAL DEAD ZONE (TDZ):
 * The time between entering a scope and the variable being declared
 * During TDZ, accessing the variable throws a ReferenceError
 */
function tdzExample() {
    // TDZ starts here
    // console.log(tdzVar); // ReferenceError: Cannot access 'tdzVar' before initialization
    let tdzVar = "I'm in the TDZ until this line";
    // TDZ ends here
    console.log(tdzVar); // Output: "I'm in the TDZ until this line"

    // WHY TDZ IS BETTER THAN var's HOISTING:
    // - Makes bugs obvious immediately (ReferenceError vs silent undefined)
    // - Forces proper code organization (declare before use)
    // - Catches potential bugs at development time
}

// Example 1.2.3: let cannot be redeclared
let userAge = 25;
// let userAge = 30; // SyntaxError: Identifier 'userAge' has already been declared

// WHY THIS IS SAFER:
// Forces you to use a different variable name or consciously reassign
userAge = 30; // This is fine - reassignment is allowed

// -----------------------------------------------------------------------------
// 1.3 CONST - For Immutable Bindings (ES6+)
// -----------------------------------------------------------------------------

/**
 * WHY USE const:
 * - Signals intent that the binding won't change
 * - Prevents accidental reassignment
 * - Self-documenting code
 *
 * IMPORTANT MISCONCEPTION:
 * const makes the BINDING immutable, not the VALUE
 * Objects and arrays declared with const can still be modified!
 */

// Example 1.3.1: const must be initialized
// const uninitializedConst; // SyntaxError: Missing initializer in const declaration

const initializedConst = "I must have a value";

// Example 1.3.2: const cannot be reassigned
const PI = 3.14159;
// PI = 3.14; // TypeError: Assignment to constant variable

// Example 1.3.3: BUT const objects can be mutated (IMPORTANT!)
const user = { name: "Alice", age: 25 };
user.age = 26;           // This is ALLOWED!
user.email = "a@b.com";  // This is ALLOWED!
console.log(user);       // Output: { name: "Alice", age: 26, email: "a@b.com" }

// user = { name: "Bob" }; // TypeError: Assignment to constant variable

/**
 * WHY const ALLOWS MUTATION:
 * const only prevents reassigning the VARIABLE, not modifying the VALUE
 * The variable 'user' always points to the same object in memory
 * But that object's contents can change
 *
 * ANALOGY:
 * Think of const as locking a house's address - you can't move to a new house,
 * but you can rearrange the furniture inside
 *
 * HOW TO MAKE TRULY IMMUTABLE:
 * Use Object.freeze() for shallow immutability
 * Or use libraries like Immutable.js for deep immutability
 */

const frozenUser = Object.freeze({ name: "Alice", age: 25 });
// frozenUser.age = 26; // Silently fails in non-strict mode, TypeError in strict mode
console.log(frozenUser.age); // Output: 25 (unchanged)

// -----------------------------------------------------------------------------
// 1.4 BEST PRACTICES FOR VARIABLE DECLARATION
// -----------------------------------------------------------------------------

/**
 * RECOMMENDED APPROACH:
 * 1. Use const by default
 * 2. Use let only when you need to reassign
 * 3. Never use var in modern code
 *
 * WHY const BY DEFAULT:
 * - Makes code predictable
 * - Documents intent
 * - Catches accidental reassignments
 * - Easier to reason about
 */

// BAD APPROACH (using var everywhere)
var badCounter = 0;
var badMax = 10;
for (var badI = 0; badI < badMax; badI++) {
    badCounter++;
}
console.log(badI); // 10 - i leaked out of the loop!

// GOOD APPROACH (using const and let appropriately)
let goodCounter = 0;      // let: will be reassigned
const goodMax = 10;       // const: won't change
for (let goodI = 0; goodI < goodMax; goodI++) {  // let: loop counter changes
    goodCounter++;
}
// console.log(goodI); // ReferenceError: goodI is not defined (properly contained!)

// ============================================================================
// SECTION 2: DATA TYPES
// ============================================================================

/**
 * JavaScript has 8 data types:
 *
 * PRIMITIVES (7): Immutable, stored on stack, compared by value
 * 1. String
 * 2. Number
 * 3. Boolean
 * 4. null
 * 5. undefined
 * 6. Symbol (ES6)
 * 7. BigInt (ES2020)
 *
 * NON-PRIMITIVE (1): Mutable, stored on heap, compared by reference
 * 8. Object (includes arrays, functions, dates, etc.)
 */

// -----------------------------------------------------------------------------
// 2.1 PRIMITIVES
// -----------------------------------------------------------------------------

// STRING
const str1 = "Hello";                    // Double quotes
const str2 = 'World';                    // Single quotes (same as double)
const str3 = `Hello ${str2}`;            // Template literals (interpolation)

/**
 * WHY THREE WAYS TO DEFINE STRINGS:
 * - Double/Single quotes: Legacy, no interpolation
 * - Template literals: Modern, allows interpolation and multiline
 *
 * BEST PRACTICE:
 * Use template literals for strings with variables
 * Use single/double quotes for simple strings (be consistent!)
 */

// NUMBER
const integer = 42;
const float = 3.14;
const negative = -17;
const infinity = Infinity;
const notANumber = NaN;

/**
 * WHY JavaScript HAS ONLY ONE NUMBER TYPE:
 * - Simplicity: No need to choose between int, float, double, etc.
 * - All numbers are 64-bit floating point (IEEE 754)
 *
 * GOTCHA - FLOATING POINT PRECISION:
 */
console.log(0.1 + 0.2);           // Output: 0.30000000000000004 (not 0.3!)
console.log(0.1 + 0.2 === 0.3);   // Output: false

/**
 * WHY THIS HAPPENS:
 * Binary floating-point cannot exactly represent some decimal fractions
 * 0.1 and 0.2 have infinite binary representations
 *
 * HOW TO HANDLE:
 * 1. Use toFixed() for display: (0.1 + 0.2).toFixed(2) === "0.30"
 * 2. Use integers for money (cents instead of dollars)
 * 3. Use libraries like decimal.js for precise calculations
 */

// BOOLEAN
const isActive = true;
const isComplete = false;

// NULL - Intentional absence of value
/**
 * WHY USE null:
 * - Explicitly indicate "no value"
 * - Intentional emptiness
 * - Example: User hasn't selected anything yet
 */
let selectedItem = null;

// UNDEFINED - Unintentional absence of value
/**
 * WHY undefined EXISTS:
 * - JavaScript's default for uninitialized variables
 * - Return value when function doesn't return anything
 * - Missing object properties
 *
 * BEST PRACTICE:
 * Use null for intentional absence, let JS use undefined for unintentional
 */
let uninitializedVar;
console.log(uninitializedVar); // Output: undefined

function noReturnValue() {
    // No return statement
}
console.log(noReturnValue()); // Output: undefined

// TYPEOF GOTCHA
console.log(typeof null);      // Output: "object" (THIS IS A BUG!)
console.log(typeof undefined); // Output: "undefined"

/**
 * WHY typeof null RETURNS "object":
 * This is a historical bug from the first JavaScript implementation
 * null was represented with all-zero bits, which matched the object tag
 * It cannot be fixed now because it would break existing code
 *
 * HOW TO CHECK FOR null:
 */
const value = null;
console.log(value === null); // Output: true (use strict equality)

// SYMBOL (ES6) - Unique identifiers
const sym1 = Symbol('description');
const sym2 = Symbol('description');
console.log(sym1 === sym2); // Output: false (every Symbol is unique!)

/**
 * WHY USE SYMBOLS:
 * - Create unique property keys that won't collide
 * - Implement well-known behaviors (Symbol.iterator)
 * - Hide properties from for...in loops
 */
const uniqueKey = Symbol('id');
const objWithSymbol = {
    [uniqueKey]: 12345,
    name: "Test"
};
console.log(objWithSymbol[uniqueKey]); // Output: 12345
console.log(Object.keys(objWithSymbol)); // Output: ["name"] (Symbol hidden!)

// BIGINT (ES2020) - Arbitrary precision integers
const bigNumber = 9007199254740991n; // Note the 'n' suffix
const anotherBig = BigInt("9007199254740992");

/**
 * WHY BIGINT EXISTS:
 * Regular numbers lose precision above Number.MAX_SAFE_INTEGER
 */
console.log(Number.MAX_SAFE_INTEGER);        // 9007199254740991
console.log(Number.MAX_SAFE_INTEGER + 1);    // 9007199254740992
console.log(Number.MAX_SAFE_INTEGER + 2);    // 9007199254740992 (SAME! Lost precision)
console.log(BigInt(Number.MAX_SAFE_INTEGER) + 2n); // 9007199254740993n (correct!)

// -----------------------------------------------------------------------------
// 2.2 REFERENCE TYPES (Objects)
// -----------------------------------------------------------------------------

/**
 * WHY DISTINGUISH PRIMITIVES FROM REFERENCES:
 * - Primitives are copied by VALUE
 * - Objects are copied by REFERENCE
 * This affects how data is passed and compared
 */

// PRIMITIVE COPY (by value)
let a = 10;
let b = a;    // b gets a COPY of the value
b = 20;
console.log(a); // Output: 10 (unchanged!)
console.log(b); // Output: 20

// OBJECT COPY (by reference)
const obj1 = { value: 10 };
const obj2 = obj1;  // obj2 gets a REFERENCE to the same object
obj2.value = 20;
console.log(obj1.value); // Output: 20 (CHANGED!)
console.log(obj2.value); // Output: 20

/**
 * WHY THIS MATTERS:
 * - Accidental mutations are a common source of bugs
 * - Need to explicitly create copies if you want independent data
 *
 * HOW TO CREATE COPIES:
 */

// Shallow copy methods
const original = { a: 1, b: { c: 2 } };
const shallowCopy1 = { ...original };           // Spread operator
const shallowCopy2 = Object.assign({}, original); // Object.assign

// Deep copy (for nested objects)
const deepCopy = JSON.parse(JSON.stringify(original));

/**
 * WHEN TO USE EACH:
 * - Spread/Object.assign: Simple objects without nesting
 * - JSON parse/stringify: Nested objects (but loses functions, undefined, Symbols)
 * - structuredClone(): Modern deep clone (loses functions)
 * - Custom function or lodash.cloneDeep: Full control
 */

// ============================================================================
// SECTION 3: TYPE COERCION AND CONVERSION
// ============================================================================

/**
 * TYPE COERCION: JavaScript automatically converts types
 * TYPE CONVERSION: You explicitly convert types
 *
 * WHY UNDERSTAND THIS:
 * - Common source of bugs
 * - Interview favorite
 * - Necessary for writing predictable code
 */

// -----------------------------------------------------------------------------
// 3.1 IMPLICIT COERCION (Automatic)
// -----------------------------------------------------------------------------

// String coercion (+ with string wins)
console.log("5" + 3);      // Output: "53" (number to string)
console.log("5" + true);   // Output: "5true"
console.log("5" + null);   // Output: "5null"

// Number coercion (-, *, /, % force numbers)
console.log("5" - 3);      // Output: 2 (string to number)
console.log("5" * "2");    // Output: 10
console.log("10" / "2");   // Output: 5
console.log("5" - true);   // Output: 4 (true = 1)
console.log("5" - null);   // Output: 5 (null = 0)

/**
 * WHY + IS DIFFERENT:
 * + is both addition AND concatenation
 * If either operand is a string, it concatenates
 * Other operators only do math, so they convert to numbers
 */

// Boolean coercion (in boolean contexts)
if ("hello") {
    console.log("Truthy!"); // This runs
}

if (0) {
    console.log("This won't run"); // 0 is falsy
}

// -----------------------------------------------------------------------------
// 3.2 EXPLICIT CONVERSION
// -----------------------------------------------------------------------------

// To String
String(123);          // "123"
(123).toString();     // "123"
123 + "";             // "123" (implicit but commonly used)

// To Number
Number("123");        // 123
parseInt("123");      // 123
parseFloat("123.45"); // 123.45
+"123";               // 123 (unary plus)

/**
 * WHY MULTIPLE WAYS:
 * - Number(): Strict, fails on invalid input (returns NaN)
 * - parseInt(): Parses until invalid character, returns integer
 * - parseFloat(): Same but keeps decimals
 * - Unary +: Shorthand for Number()
 */

console.log(Number("123abc"));    // Output: NaN
console.log(parseInt("123abc"));  // Output: 123 (stops at 'a')
console.log(parseFloat("123.45abc")); // Output: 123.45

// To Boolean
Boolean(1);      // true
Boolean(0);      // false
!!1;             // true (double negation - common shorthand)

// ============================================================================
// SECTION 4: TRUTHY AND FALSY VALUES
// ============================================================================

/**
 * FALSY VALUES (only 8 in JavaScript):
 * - false
 * - 0
 * - -0
 * - 0n (BigInt zero)
 * - "" (empty string)
 * - null
 * - undefined
 * - NaN
 *
 * EVERYTHING ELSE IS TRUTHY (including "0", "false", [], {})
 */

// Common interview gotchas
console.log(Boolean("0"));     // true (non-empty string!)
console.log(Boolean("false")); // true (non-empty string!)
console.log(Boolean([]));      // true (empty array is truthy!)
console.log(Boolean({}));      // true (empty object is truthy!)

/**
 * WHY [] AND {} ARE TRUTHY:
 * They are objects, and all objects are truthy
 * Even empty ones - they still exist in memory
 *
 * HOW TO CHECK FOR EMPTY ARRAY:
 */
const arr = [];
if (arr.length === 0) {
    console.log("Array is empty");
}

// ============================================================================
// SECTION 5: EQUALITY COMPARISON (== vs ===)
// ============================================================================

/**
 * == (Loose Equality): Compares with type coercion
 * === (Strict Equality): Compares without type coercion
 *
 * BEST PRACTICE: Always use === unless you have a specific reason for ==
 */

// Loose equality (type coercion happens)
console.log(5 == "5");     // true (string converted to number)
console.log(true == 1);    // true (boolean converted to number)
console.log(null == undefined); // true (special case)
console.log([] == false);  // true ([] -> "" -> 0, false -> 0)

// Strict equality (no coercion)
console.log(5 === "5");    // false (different types)
console.log(true === 1);   // false
console.log(null === undefined); // false

/**
 * WHY USE ===:
 * - Predictable behavior
 * - Avoids subtle bugs
 * - Clearer intent
 * - Faster (no coercion needed)
 *
 * WHEN == MIGHT BE OKAY:
 * - Checking for null or undefined: value == null
 *   (matches both null and undefined)
 */

// ============================================================================
// SECTION 6: TEMPLATE LITERALS
// ============================================================================

const name = "Alice";
const age = 25;

// Old way (string concatenation)
const oldWay = "Hello, my name is " + name + " and I am " + age + " years old.";

// New way (template literals)
const newWay = `Hello, my name is ${name} and I am ${age} years old.`;

/**
 * WHY TEMPLATE LITERALS ARE BETTER:
 * 1. More readable
 * 2. Support multiline strings
 * 3. Can embed expressions
 * 4. Tagged templates for advanced use cases
 */

// Multiline strings
const multiline = `
    This is line 1
    This is line 2
    This is line 3
`;

// Expressions in template literals
const calculation = `The sum of 5 + 3 is ${5 + 3}`;
const conditional = `Status: ${age >= 18 ? 'Adult' : 'Minor'}`;

// ============================================================================
// SECTION 7: COMMON MISTAKES AND HOW TO AVOID THEM
// ============================================================================

/**
 * Mistake #1: Using == instead of ===
 */
// BAD
if (userInput == "5") { /* ... */ }
// GOOD
if (userInput === "5") { /* ... */ }

/**
 * Mistake #2: Not understanding const with objects
 */
// BAD ASSUMPTION
const config = { debug: false };
// config = { debug: true }; // Error!
// WHAT WORKS
config.debug = true; // This is fine!

/**
 * Mistake #3: Using var in modern code
 */
// BAD
for (var i = 0; i < 5; i++) {
    setTimeout(() => console.log(i), 100);
}
// Output: 5, 5, 5, 5, 5 (i is shared across iterations!)

// GOOD
for (let j = 0; j < 5; j++) {
    setTimeout(() => console.log(j), 100);
}
// Output: 0, 1, 2, 3, 4 (each iteration has its own j)

/**
 * Mistake #4: Floating point comparison
 */
// BAD
if (0.1 + 0.2 === 0.3) { /* won't work */ }
// GOOD
if (Math.abs((0.1 + 0.2) - 0.3) < Number.EPSILON) { /* works */ }

/**
 * Mistake #5: Not checking for NaN properly
 */
// BAD
if (value === NaN) { /* won't work - NaN !== NaN */ }
// GOOD
if (Number.isNaN(value)) { /* works */ }
// OR
if (value !== value) { /* clever trick - only NaN is not equal to itself */ }

// ============================================================================
// SECTION 8: PRACTICE EXERCISES
// ============================================================================

/**
 * Exercise 1: What will this log?
 */
console.log(typeof typeof 1);
// Answer: "string"
// Explanation: typeof 1 = "number", typeof "number" = "string"

/**
 * Exercise 2: What will this log?
 */
let x = 1;
let y = x++;
console.log(x, y);
// Answer: 2, 1
// Explanation: Post-increment returns original value, then increments

/**
 * Exercise 3: What will this log?
 */
console.log([] + []);
// Answer: "" (empty string)
// Explanation: Arrays convert to strings and concatenate

console.log([] + {});
// Answer: "[object Object]"
// Explanation: [] -> "", {} -> "[object Object]"

console.log({} + []);
// Answer: "[object Object]" or 0 (depends on context)
// In console: might be 0 ({} treated as block)
// In expression: "[object Object]"

/**
 * Exercise 4: Predict the output
 */
console.log(1 < 2 < 3); // true (1 < 2 = true, true < 3 = 1 < 3 = true)
console.log(3 > 2 > 1); // false (3 > 2 = true, true > 1 = 1 > 1 = false)
