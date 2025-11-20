/**
 * ============================================================================
 * PHASE 4: LOOPS DEEP DIVE
 * ============================================================================
 */

// ============================================================================
// SECTION 1: FOR LOOP
// ============================================================================

/**
 * WHY: Most versatile loop, control over initialization, condition, and increment
 * WHAT: for (init; condition; update) { body }
 * WHEN: Known number of iterations, array indices
 */

// Basic for loop
for (let i = 0; i < 5; i++) {
    console.log(i); // 0, 1, 2, 3, 4
}

// Counting backwards
for (let i = 5; i > 0; i--) {
    console.log(i); // 5, 4, 3, 2, 1
}

// Multiple variables
for (let i = 0, j = 10; i < j; i++, j--) {
    console.log(i, j); // 0,10 1,9 2,8 3,7 4,6
}

// Iterating arrays
const arr = ['a', 'b', 'c'];
for (let i = 0; i < arr.length; i++) {
    console.log(arr[i]);
}

/**
 * PERFORMANCE TIP: Cache array length
 * WHY: arr.length is evaluated each iteration
 */
// Optimized (for very large arrays)
for (let i = 0, len = arr.length; i < len; i++) {
    console.log(arr[i]);
}

// ============================================================================
// SECTION 2: WHILE LOOP
// ============================================================================

/**
 * WHY: When you don't know iterations in advance
 * WHAT: while (condition) { body }
 * WHEN: Until a condition is met
 */

let count = 0;
while (count < 5) {
    console.log(count);
    count++;
}

// Reading until done
let input = getInput(); // hypothetical function
while (input !== "quit") {
    process(input);
    input = getInput();
}

/**
 * DANGER: Infinite loops
 * Always ensure condition will eventually be false
 */
// BAD - Infinite loop!
// while (true) { console.log("forever"); }

// GOOD - Has exit condition
let attempts = 0;
while (true) {
    if (attempts >= 3) break;
    attempts++;
}

// ============================================================================
// SECTION 3: DO...WHILE LOOP
// ============================================================================

/**
 * WHY: Execute at least once, then check condition
 * WHAT: do { body } while (condition)
 * WHEN: Input validation, menu systems
 */

let num;
do {
    num = prompt("Enter a number > 10");
} while (num <= 10);

// Always executes once even if condition is false
let x = 100;
do {
    console.log(x); // Runs once: 100
    x++;
} while (x < 10);

// ============================================================================
// SECTION 4: FOR...IN LOOP
// ============================================================================

/**
 * WHY: Iterate over object property names
 * WHAT: for (key in object) { }
 * WHEN: Need to access object keys
 *
 * WARNING: Also iterates inherited properties!
 */

const person = {
    name: "Alice",
    age: 25,
    city: "NYC"
};

for (const key in person) {
    console.log(`${key}: ${person[key]}`);
}
// name: Alice
// age: 25
// city: NYC

/**
 * WHY NOT USE for...in ON ARRAYS:
 * 1. Iterates over ALL enumerable properties, not just indices
 * 2. Order not guaranteed
 * 3. Index is a string, not number
 */

const arr2 = [10, 20, 30];
arr2.customProp = "oops"; // Arrays are objects, can have properties

for (const i in arr2) {
    console.log(i, typeof i); // "0", "1", "2", "customProp" - all strings!
}

// Protect against inherited properties
for (const key in person) {
    if (person.hasOwnProperty(key)) {
        console.log(key); // Only own properties
    }
}

// ============================================================================
// SECTION 5: FOR...OF LOOP (ES6)
// ============================================================================

/**
 * WHY: Clean iteration over iterable values
 * WHAT: for (value of iterable) { }
 * WHEN: Arrays, strings, Maps, Sets, NodeLists
 *
 * WHY BETTER THAN for...in FOR ARRAYS:
 * - Gets values, not keys
 * - Only iterates actual elements
 * - Order guaranteed
 */

const colors = ['red', 'green', 'blue'];

// for...of - iterate values
for (const color of colors) {
    console.log(color); // red, green, blue
}

// Works with strings
for (const char of "hello") {
    console.log(char); // h, e, l, l, o
}

// Works with Maps
const map = new Map([['a', 1], ['b', 2]]);
for (const [key, value] of map) {
    console.log(key, value); // a 1, b 2
}

// Works with Sets
const set = new Set([1, 2, 3]);
for (const value of set) {
    console.log(value); // 1, 2, 3
}

/**
 * DOES NOT WORK WITH PLAIN OBJECTS!
 * Objects are not iterable by default
 */
// for (const val of person) {} // TypeError!

// To iterate object values, use Object.values()
for (const value of Object.values(person)) {
    console.log(value);
}

// ============================================================================
// SECTION 6: BREAK AND CONTINUE
// ============================================================================

/**
 * break: Exit loop entirely
 * continue: Skip to next iteration
 */

// break example - find first even
const numbers = [1, 3, 5, 6, 7, 8];
let firstEven;

for (const num of numbers) {
    if (num % 2 === 0) {
        firstEven = num;
        break; // Exit loop when found
    }
}
console.log(firstEven); // 6

// continue example - sum only positives
const mixed = [1, -2, 3, -4, 5];
let sum = 0;

for (const num of mixed) {
    if (num < 0) continue; // Skip negatives
    sum += num;
}
console.log(sum); // 9

// ============================================================================
// SECTION 7: LABELED STATEMENTS
// ============================================================================

/**
 * WHY: Break/continue outer loops from nested loops
 * WHAT: label: for () { for () { break label; } }
 * WHEN: Searching in 2D arrays
 */

outerLoop:
for (let i = 0; i < 3; i++) {
    for (let j = 0; j < 3; j++) {
        if (i === 1 && j === 1) {
            break outerLoop; // Breaks outer loop!
        }
        console.log(i, j);
    }
}
// Output: 0,0 0,1 0,2 1,0 (stops at 1,1)

// Without label, break only exits inner loop
for (let i = 0; i < 3; i++) {
    for (let j = 0; j < 3; j++) {
        if (j === 1) {
            break; // Only breaks inner loop
        }
        console.log(i, j);
    }
}
// Output: 0,0 1,0 2,0

// ============================================================================
// SECTION 8: LOOP ALTERNATIVES
// ============================================================================

/**
 * Modern JavaScript often uses array methods instead of loops
 * WHY: More declarative, less error-prone
 */

const nums = [1, 2, 3, 4, 5];

// Instead of: for loop to transform
const doubled = [];
for (let i = 0; i < nums.length; i++) {
    doubled.push(nums[i] * 2);
}

// Use: map
const doubledMap = nums.map(n => n * 2);

// Instead of: for loop to filter
const evens = [];
for (const num of nums) {
    if (num % 2 === 0) evens.push(num);
}

// Use: filter
const evensFilter = nums.filter(n => n % 2 === 0);

// Instead of: for loop to sum
let total = 0;
for (const num of nums) {
    total += num;
}

// Use: reduce
const totalReduce = nums.reduce((acc, n) => acc + n, 0);

// ============================================================================
// SECTION 9: COMMON INTERVIEW QUESTIONS
// ============================================================================

/**
 * Q1: What's wrong with this code?
 */
for (var i = 0; i < 3; i++) {
    setTimeout(() => console.log(i), 100);
}
// Output: 3, 3, 3 (not 0, 1, 2!)
// WHY: var is function-scoped, same i for all iterations

// Fix 1: Use let
for (let i = 0; i < 3; i++) {
    setTimeout(() => console.log(i), 100);
}

// Fix 2: Use IIFE
for (var i = 0; i < 3; i++) {
    ((j) => {
        setTimeout(() => console.log(j), 100);
    })(i);
}

/**
 * Q2: Reverse a string using a loop
 */
function reverseString(str) {
    let reversed = "";
    for (let i = str.length - 1; i >= 0; i--) {
        reversed += str[i];
    }
    return reversed;
}

// Alternative: Array methods
const reverseAlt = str => [...str].reverse().join('');

/**
 * Q3: Find largest number
 */
function findMax(arr) {
    if (arr.length === 0) return undefined;

    let max = arr[0];
    for (let i = 1; i < arr.length; i++) {
        if (arr[i] > max) {
            max = arr[i];
        }
    }
    return max;
}

// Alternative: Math.max with spread
const findMaxAlt = arr => Math.max(...arr);

/**
 * Q4: FizzBuzz
 */
for (let i = 1; i <= 100; i++) {
    if (i % 15 === 0) {
        console.log("FizzBuzz");
    } else if (i % 3 === 0) {
        console.log("Fizz");
    } else if (i % 5 === 0) {
        console.log("Buzz");
    } else {
        console.log(i);
    }
}
