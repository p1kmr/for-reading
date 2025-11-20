/**
 * ============================================================================
 * PHASE 2: OPERATORS & TYPE COERCION
 * ============================================================================
 */

// ============================================================================
// SECTION 1: ARITHMETIC OPERATORS
// ============================================================================

/**
 * WHY: Perform mathematical calculations
 * WHAT: +, -, *, /, %, ** (exponentiation)
 */

// Basic arithmetic
console.log(10 + 5);   // 15 - Addition
console.log(10 - 5);   // 5  - Subtraction
console.log(10 * 5);   // 50 - Multiplication
console.log(10 / 5);   // 2  - Division
console.log(10 % 3);   // 1  - Modulo (remainder)
console.log(2 ** 3);   // 8  - Exponentiation (ES7)

/**
 * The + operator is SPECIAL - it does both addition AND concatenation
 * WHY THIS DESIGN:
 * - Historical JavaScript decision
 * - String concatenation was common, needed a simple operator
 *
 * RULE: If EITHER operand is a string, + concatenates
 */

// + with strings (concatenation wins)
console.log("Hello" + "World");  // "HelloWorld"
console.log("5" + 3);            // "53" (number converted to string)
console.log(5 + "3");            // "53"
console.log(1 + 2 + "3");        // "33" (left-to-right: 1+2=3, 3+"3"="33")
console.log("1" + 2 + 3);        // "123" (all become strings after first)

// Other operators always convert to numbers
console.log("10" - 5);   // 5  (string to number)
console.log("10" * "2"); // 20
console.log("10" / "2"); // 5

/**
 * WHY OTHER OPERATORS DON'T CONCATENATE:
 * - No string operation for -, *, /
 * - Only option is numeric conversion
 * - Makes behavior predictable
 */

// Unary + (convert to number - fastest way)
console.log(+"42");      // 42
console.log(+true);      // 1
console.log(+false);     // 0
console.log(+null);      // 0
console.log(+undefined); // NaN
console.log(+"hello");   // NaN

// Unary - (convert and negate)
console.log(-"42");      // -42
console.log(-true);      // -1

// Increment/Decrement
let a = 5;
console.log(a++);  // 5 (returns then increments)
console.log(a);    // 6
console.log(++a);  // 7 (increments then returns)

/**
 * WHY PREFIX VS POSTFIX MATTERS:
 * - Postfix (a++): Use current value, then increment
 * - Prefix (++a): Increment first, then use
 *
 * INTERVIEW TIP: This is a common gotcha question!
 */

let b = 1;
let c = b++ + ++b;
// Step by step:
// b++ returns 1 (b becomes 2)
// ++b increments b to 3, returns 3
// 1 + 3 = 4
console.log(c); // 4
console.log(b); // 3

// ============================================================================
// SECTION 2: COMPARISON OPERATORS
// ============================================================================

/**
 * WHY TWO EQUALITY OPERATORS:
 * == allows type coercion (loose)
 * === requires same type (strict)
 *
 * BEST PRACTICE: Always use === for predictable behavior
 */

// Loose equality (==) - type coercion happens
console.log(5 == "5");           // true (string to number)
console.log(0 == false);         // true (false to 0)
console.log(1 == true);          // true (true to 1)
console.log(null == undefined);  // true (special case!)
console.log("" == false);        // true (both become 0)

// Strict equality (===) - no coercion
console.log(5 === "5");          // false
console.log(0 === false);        // false
console.log(null === undefined); // false

/**
 * LOOSE EQUALITY ALGORITHM (simplified):
 * 1. Same type? Compare directly
 * 2. null == undefined? true (special case)
 * 3. Number vs String? Convert string to number
 * 4. Boolean vs anything? Convert boolean to number (true=1, false=0)
 * 5. Object vs primitive? Convert object (ToPrimitive)
 */

// Famous tricky comparisons
console.log([] == false);  // true
// How: [] -> "" -> 0, false -> 0, 0 == 0

console.log([] == ![]);    // true (!)
// How: ![] = false, [] == false (see above)

console.log({} == "[object Object]"); // true
// How: {} -> "[object Object]"

// Relational operators
console.log(5 > 3);   // true
console.log(5 < 3);   // false
console.log(5 >= 5);  // true
console.log(5 <= 4);  // false

// String comparison (lexicographical)
console.log("apple" < "banana"); // true
console.log("10" < "9");         // true (string comparison: "1" < "9")
console.log(10 < 9);             // false (numeric comparison)

/**
 * WHY STRING COMPARISON IS DIFFERENT:
 * - Compares character by character using Unicode values
 * - "10" vs "9": '1' (49) < '9' (57), so "10" < "9"
 *
 * HOW TO COMPARE NUMERIC STRINGS:
 * Convert to numbers first: Number("10") < Number("9")
 */

// ============================================================================
// SECTION 3: LOGICAL OPERATORS
// ============================================================================

/**
 * WHY UNDERSTAND LOGICAL OPERATORS:
 * - Used everywhere in conditions
 * - Short-circuit evaluation is powerful
 * - Return VALUES, not just booleans
 */

// Basic logical operations
console.log(true && true);   // true
console.log(true && false);  // false
console.log(true || false);  // true
console.log(false || false); // false
console.log(!true);          // false

/**
 * SHORT-CIRCUIT EVALUATION:
 * && returns first falsy value OR last value
 * || returns first truthy value OR last value
 *
 * WHY: Efficiency - stops evaluating when result is determined
 */

// && short-circuit (returns first falsy or last value)
console.log(1 && 2 && 3);    // 3 (all truthy, returns last)
console.log(1 && 0 && 3);    // 0 (first falsy)
console.log(1 && "" && 3);   // "" (first falsy)

// || short-circuit (returns first truthy or last value)
console.log(0 || "" || null); // null (all falsy, returns last)
console.log(0 || "hi" || 3);  // "hi" (first truthy)

// Practical uses of short-circuit
const name = userName || "Guest";  // Default value

// Conditional execution
isLoggedIn && showDashboard();  // Only call if truthy

/**
 * WHY NOT OTHER WAY (ternary for defaults):
 * Using || for defaults is more concise
 * userName || "Guest" vs (userName ? userName : "Guest")
 *
 * BUT GOTCHA:
 * || treats 0 and "" as falsy!
 */

const count = 0;
const displayCount = count || "No items";
console.log(displayCount); // "No items" (not what we wanted!)

// SOLUTION: Nullish coalescing operator (??)

// ============================================================================
// SECTION 4: NULLISH COALESCING (??) - ES2020
// ============================================================================

/**
 * WHY ?? WAS NEEDED:
 * || treats 0, "", false as falsy
 * Sometimes you only want to check null/undefined
 *
 * ?? returns right side ONLY if left side is null or undefined
 */

console.log(0 || "default");    // "default" (0 is falsy)
console.log(0 ?? "default");    // 0 (0 is not null/undefined)

console.log("" || "default");   // "default" (empty string is falsy)
console.log("" ?? "default");   // "" (empty string is not null/undefined)

console.log(null ?? "default"); // "default"
console.log(undefined ?? "default"); // "default"

// Practical example
function greet(name) {
    // Using || would fail for empty string
    const displayName = name ?? "Guest";
    return `Hello, ${displayName}!`;
}

console.log(greet(""));   // "Hello, !" (empty name is valid)
console.log(greet(null)); // "Hello, Guest!"

// ============================================================================
// SECTION 5: OPTIONAL CHAINING (?.) - ES2020
// ============================================================================

/**
 * WHY OPTIONAL CHAINING:
 * - Safely access nested properties
 * - Avoids "Cannot read property of undefined" errors
 */

const user = {
    profile: {
        address: {
            city: "NYC"
        }
    }
};

// Old way (verbose and error-prone)
const city1 = user && user.profile && user.profile.address && user.profile.address.city;

// New way with optional chaining
const city2 = user?.profile?.address?.city;
console.log(city2); // "NYC"

// Works with missing properties (returns undefined instead of error)
const missing = user?.settings?.theme;
console.log(missing); // undefined (no error!)

// Works with arrays
const arr = [1, 2, 3];
console.log(arr?.[0]);  // 1
console.log(arr?.[10]); // undefined

// Works with function calls
const obj = {
    greet: () => "Hello"
};
console.log(obj.greet?.());    // "Hello"
console.log(obj.missing?.()); // undefined

// ============================================================================
// SECTION 6: ASSIGNMENT OPERATORS
// ============================================================================

let x = 10;
x += 5;   // x = x + 5 = 15
x -= 3;   // x = x - 3 = 12
x *= 2;   // x = x * 2 = 24
x /= 4;   // x = x / 4 = 6
x %= 4;   // x = x % 4 = 2
x **= 3;  // x = x ** 3 = 8

// Logical assignment operators (ES2021)

/**
 * ||= assigns if variable is falsy
 * &&= assigns if variable is truthy
 * ??= assigns if variable is null/undefined
 */

let y = null;
y ||= "default";  // y = y || "default"
console.log(y);   // "default"

let z = "exists";
z ||= "default";  // No change, z is truthy
console.log(z);   // "exists"

let count2 = 0;
count2 ||= 10;    // count2 becomes 10 (0 is falsy!)
console.log(count2);

let count3 = 0;
count3 ??= 10;    // count3 stays 0 (not null/undefined)
console.log(count3);

// ============================================================================
// SECTION 7: TERNARY OPERATOR
// ============================================================================

/**
 * WHY TERNARY:
 * - Concise conditional expressions
 * - Can be used in places where if/else cannot (e.g., inside JSX)
 *
 * SYNTAX: condition ? valueIfTrue : valueIfFalse
 */

const age = 20;
const status = age >= 18 ? "Adult" : "Minor";
console.log(status); // "Adult"

// Nested ternary (use sparingly - can be hard to read)
const score = 85;
const grade = score >= 90 ? "A" :
              score >= 80 ? "B" :
              score >= 70 ? "C" :
              score >= 60 ? "D" : "F";
console.log(grade); // "B"

/**
 * WHY NOT NESTED TERNARY (in most cases):
 * - Hard to read
 * - Hard to maintain
 * - Better alternatives: if/else or object lookup
 */

// Better alternative for multiple conditions
const gradeMap = {
    A: score >= 90,
    B: score >= 80,
    C: score >= 70,
    D: score >= 60,
    F: true
};

// ============================================================================
// SECTION 8: BITWISE OPERATORS
// ============================================================================

/**
 * WHY KNOW BITWISE:
 * - Rarely used in everyday JS
 * - But appear in interviews
 * - Used in: flags, permissions, low-level operations
 */

console.log(5 & 3);   // 1  (AND: 101 & 011 = 001)
console.log(5 | 3);   // 7  (OR:  101 | 011 = 111)
console.log(5 ^ 3);   // 6  (XOR: 101 ^ 011 = 110)
console.log(~5);      // -6 (NOT: inverts bits)
console.log(5 << 1);  // 10 (Left shift: 101 -> 1010)
console.log(5 >> 1);  // 2  (Right shift: 101 -> 10)

// Common trick: Double NOT for truncating
console.log(~~3.7);   // 3 (faster than Math.floor for positives)
console.log(~~-3.7);  // -3 (different from Math.floor!)

// Common trick: Check if even/odd
console.log(4 & 1);   // 0 (even)
console.log(5 & 1);   // 1 (odd)

// ============================================================================
// SECTION 9: TYPEOF AND INSTANCEOF
// ============================================================================

// typeof - returns string of type
console.log(typeof 42);           // "number"
console.log(typeof "hello");      // "string"
console.log(typeof true);         // "boolean"
console.log(typeof undefined);    // "undefined"
console.log(typeof null);         // "object" (historical bug!)
console.log(typeof {});           // "object"
console.log(typeof []);           // "object" (arrays are objects)
console.log(typeof function(){}); // "function"
console.log(typeof Symbol());     // "symbol"
console.log(typeof 10n);          // "bigint"

/**
 * HOW TO CHECK FOR ARRAY:
 * typeof returns "object" for arrays
 * Use Array.isArray() instead
 */
console.log(Array.isArray([]));     // true
console.log(Array.isArray({}));     // false

// instanceof - check prototype chain
class Animal {}
class Dog extends Animal {}

const dog = new Dog();
console.log(dog instanceof Dog);    // true
console.log(dog instanceof Animal); // true
console.log(dog instanceof Object); // true

// ============================================================================
// SECTION 10: COMMON INTERVIEW QUESTIONS
// ============================================================================

/**
 * Q1: What does this output?
 */
console.log(3 + 4 + "5");
// Answer: "75"
// Step: 3 + 4 = 7, then 7 + "5" = "75"

console.log("3" + 4 + 5);
// Answer: "345"
// Step: "3" + 4 = "34", then "34" + 5 = "345"

/**
 * Q2: What's the output?
 */
console.log([] + {});      // "[object Object]"
console.log({} + []);      // "[object Object]" (or 0 in some contexts)
console.log([] + []);      // ""
console.log({} + {});      // "[object Object][object Object]"

/**
 * Q3: Compare these
 */
console.log(null == undefined);  // true
console.log(null === undefined); // false
console.log(NaN == NaN);         // false (NaN is not equal to anything!)
console.log(NaN === NaN);        // false

/**
 * Q4: What's the output?
 */
console.log("2" > "10");   // true (string comparison: "2" > "1")
console.log("2" > 10);     // false (numeric: 2 > 10)

/**
 * Q5: Evaluate these expressions
 */
console.log(false == "");   // true (both become 0)
console.log(false == "0");  // true (false->0, "0"->0)
console.log(false === "0"); // false (different types)
console.log(" \n\t" == 0);  // true (whitespace string becomes 0)

/**
 * Q6: What does this return?
 */
function test() {
    return
    {
        message: "hello"
    };
}
console.log(test()); // undefined!

/**
 * WHY: Automatic Semicolon Insertion (ASI)
 * JavaScript inserts ; after return
 * The object becomes unreachable code
 *
 * FIX: Put { on same line as return
 */
function testFixed() {
    return {
        message: "hello"
    };
}
console.log(testFixed()); // { message: "hello" }
