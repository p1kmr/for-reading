# Phase 2: JavaScript Fundamentals

## 📋 Table of Contents
1. [Variables (var, let, const)](#variables)
2. [Data Types](#data-types)
3. [Operators](#operators)
4. [Type Conversion](#type-conversion)
5. [Coding Examples](#coding-examples)
6. [Common Mistakes](#common-mistakes)
7. [Practice Exercises](#practice-exercises)

---

## Variables

### Why Do We Need Variables?

Think of variables as **labeled boxes** where you store information. You can put things in, take things out, and change what's inside.

```
┌─────────────────────────────────────────────────┐
│          VARIABLE CONCEPT                       │
│                                                 │
│   ┌─────────┐    ┌─────────┐    ┌─────────┐  │
│   │  name   │    │   age   │    │  email  │  │
│   ├─────────┤    ├─────────┤    ├─────────┤  │
│   │ "Alice" │    │   25    │    │ "a@.."  │  │
│   └─────────┘    └─────────┘    └─────────┘  │
│                                                 │
│   Box Label      Box Label      Box Label      │
│   (name)         (name)          (name)        │
│       ↓              ↓               ↓         │
│   Contents       Contents        Contents      │
│   (value)        (value)          (value)      │
└─────────────────────────────────────────────────┘
```

---

### The Three Keywords: var, let, const

#### Diagram: Variable Keywords Comparison

```
┌──────────────────────────────────────────────────────────────┐
│                  VARIABLE DECLARATIONS                       │
├──────────────┬─────────────┬─────────────┬──────────────────┤
│   Keyword    │  Can Change │   Scope     │  When to Use     │
├──────────────┼─────────────┼─────────────┼──────────────────┤
│     var      │     ✅      │  Function   │  ❌ DON'T USE    │
│              │             │  (old way)  │  (outdated)      │
├──────────────┼─────────────┼─────────────┼──────────────────┤
│     let      │     ✅      │   Block     │  ✅ USE         │
│              │             │  (modern)   │  (when changing) │
├──────────────┼─────────────┼─────────────┼──────────────────┤
│    const     │     ❌      │   Block     │  ✅ USE         │
│              │             │  (modern)   │  (default)       │
└──────────────┴─────────────┴─────────────┴──────────────────┘
```

---

### 1. const (Use this by default!)

**Why:** Prevents accidental changes, makes code more predictable

**What:** Creates a constant (unchangeable) variable

**How:**
```javascript
// ✅ Correct usage
const pi = 3.14159;
const userName = "Alice";
const maxUsers = 100;

console.log(pi);  // 3.14159

// ❌ This will cause an error
pi = 3.14;  // Error: Assignment to constant variable
```

**When to use:**
- Values that shouldn't change
- Reference data (we'll cover this later)
- **Use const by default**, switch to let only if you need to change the value

---

### 2. let (Use when value will change)

**Why:** Provides block scope and prevents accidental redeclaration

**What:** Creates a variable that can be changed

**How:**
```javascript
// ✅ Correct usage
let score = 0;
console.log(score);  // 0

score = 10;          // ✅ Can change
console.log(score);  // 10

score = score + 5;   // ✅ Can update
console.log(score);  // 15
```

**When to use:**
- Counters in loops
- Values that change over time
- Accumulating/calculating results

---

### 3. var (DON'T USE - Old Way)

**Why:** Has confusing behavior and is being phased out

**What:** The old way of declaring variables

**How:**
```javascript
// ❌ Avoid using var
var oldWay = "This is outdated";

// Problems with var:
// 1. Can be redeclared
var name = "Alice";
var name = "Bob";  // No error, but confusing!

// 2. Has function scope, not block scope
if (true) {
    var x = 10;
}
console.log(x);  // 10 (x leaked out of the block!)
```

**When to use:** Never in modern JavaScript!

---

## Diagram: Variable Scope

```
┌───────────────────────────────────────────────────────────┐
│                    GLOBAL SCOPE                           │
│  const globalVar = "I'm available everywhere";            │
│                                                           │
│  ┌─────────────────────────────────────────────────────┐ │
│  │              FUNCTION SCOPE                         │ │
│  │  function myFunction() {                            │ │
│  │    const funcVar = "I'm in the function";           │ │
│  │                                                     │ │
│  │    ┌──────────────────────────────────────────┐    │ │
│  │    │         BLOCK SCOPE (if, loop, etc)     │    │ │
│  │    │  if (true) {                            │    │ │
│  │    │    const blockVar = "I'm in the block"; │    │ │
│  │    │    // Can access: blockVar, funcVar,    │    │ │
│  │    │    // globalVar                         │    │ │
│  │    │  }                                       │    │ │
│  │    │  // blockVar is NOT accessible here     │    │ │
│  │    └──────────────────────────────────────────┘    │ │
│  │    // funcVar IS accessible here                   │ │
│  │  }                                                  │ │
│  └─────────────────────────────────────────────────────┘ │
│  // funcVar is NOT accessible here                       │
└───────────────────────────────────────────────────────────┘

Rule: Inner scopes can access outer scopes, but not vice versa
```

---

## Data Types

### Why Do We Have Different Data Types?

Different types of information need different treatment:
- Numbers need math operations
- Text needs joining and splitting
- True/false needs logical operations

### The Seven Primitive Data Types

```
┌────────────────────────────────────────────────────────┐
│               PRIMITIVE DATA TYPES                     │
│                                                        │
│  1. String    → "text"         Text/characters        │
│  2. Number    → 42, 3.14       Numbers (int & float)  │
│  3. Boolean   → true, false    True or False          │
│  4. Undefined → undefined      Not yet assigned       │
│  5. Null      → null           Intentionally empty    │
│  6. Symbol    → Symbol()       Unique identifier      │
│  7. BigInt    → 123n           Very large numbers     │
│                                                        │
└────────────────────────────────────────────────────────┘
```

---

### 1. String (Text)

**Why:** To store and manipulate text

**What:** Sequence of characters in quotes

**How:**
```javascript
// Three ways to create strings
const singleQuotes = 'Hello';
const doubleQuotes = "Hello";
const backticks = `Hello`;  // Template literals (best for combining)

// String operations
const firstName = "John";
const lastName = "Doe";

// Concatenation (old way)
const fullName1 = firstName + " " + lastName;

// Template literals (modern way) ✅
const fullName2 = `${firstName} ${lastName}`;

console.log(fullName2);  // "John Doe"

// Common string operations
const text = "JavaScript";
console.log(text.length);        // 10 (number of characters)
console.log(text.toLowerCase()); // "javascript"
console.log(text.toUpperCase()); // "JAVASCRIPT"
console.log(text[0]);            // "J" (first character)
console.log(text.slice(0, 4));   // "Java"
```

**String Methods Diagram:**
```
    "Hello World"
    │││││ │││││
    0123456789...  ← Index positions

    .length        → 11
    .toUpperCase() → "HELLO WORLD"
    .toLowerCase() → "hello world"
    .slice(0, 5)   → "Hello"
    .indexOf("o")  → 4
    .replace("World", "JS") → "Hello JS"
```

---

### 2. Number

**Why:** To perform calculations and comparisons

**What:** Integers and decimals

**How:**
```javascript
// Different types of numbers
const integer = 42;
const decimal = 3.14;
const negative = -10;
const scientific = 2e3;  // 2000

// Special number values
const infinity = Infinity;
const negInfinity = -Infinity;
const notANumber = NaN;  // Not a Number

// Math operations
const sum = 10 + 5;          // 15
const difference = 10 - 5;   // 5
const product = 10 * 5;      // 50
const quotient = 10 / 5;     // 2
const remainder = 10 % 3;    // 1 (modulo)
const power = 2 ** 3;        // 8 (exponentiation)

// Math object
console.log(Math.round(3.7));   // 4
console.log(Math.floor(3.7));   // 3
console.log(Math.ceil(3.2));    // 4
console.log(Math.random());     // Random number between 0 and 1
console.log(Math.max(1, 5, 3)); // 5
console.log(Math.min(1, 5, 3)); // 1
```

**Number Precision Warning:**
```javascript
// ⚠️ Floating point precision issue
console.log(0.1 + 0.2);  // 0.30000000000000004 (not exactly 0.3!)

// Solution: Use toFixed() for decimals
const result = (0.1 + 0.2).toFixed(2);  // "0.30"
console.log(Number(result));             // 0.3
```

---

### 3. Boolean (True/False)

**Why:** To make decisions in your code

**What:** Only two values: `true` or `false`

**How:**
```javascript
// Direct boolean values
const isActive = true;
const isLoggedIn = false;

// Comparison results are booleans
const age = 25;
console.log(age > 18);      // true
console.log(age < 18);      // false
console.log(age === 25);    // true

// Used in conditions
if (isActive) {
    console.log("User is active");
}

// Boolean in variables
const hasPermission = true;
const isAdmin = false;
const canEdit = hasPermission && isAdmin;  // false
```

**Truthy and Falsy Values:**
```
┌─────────────────────────────────────────────────┐
│         FALSY VALUES (become false)             │
│                                                 │
│  • false                                        │
│  • 0                                            │
│  • "" (empty string)                            │
│  • null                                         │
│  • undefined                                    │
│  • NaN                                          │
│                                                 │
│         TRUTHY VALUES (become true)             │
│                                                 │
│  • true                                         │
│  • Any number except 0 (including negative)     │
│  • Any non-empty string (even "false")          │
│  • Arrays and objects (even empty ones)         │
└─────────────────────────────────────────────────┘
```

---

### 4. Undefined

**Why:** Indicates a variable exists but has no value yet

**What:** Automatically assigned when variable is declared but not initialized

**How:**
```javascript
let someVariable;
console.log(someVariable);  // undefined

function noReturn() {
    // No return statement
}
console.log(noReturn());  // undefined

const person = { name: "Alice" };
console.log(person.age);  // undefined (property doesn't exist)
```

---

### 5. Null

**Why:** To explicitly say "no value" or "empty"

**What:** Intentionally empty value

**How:**
```javascript
let selectedUser = null;  // Intentionally no user selected

// Difference between null and undefined
let a;              // undefined (forgot to assign)
let b = null;       // null (intentionally empty)

console.log(a);     // undefined
console.log(b);     // null

// Common use case
let currentUser = null;  // Start with no user

function login(username) {
    currentUser = username;  // Now has a value
}

function logout() {
    currentUser = null;  // Back to no user
}
```

**Undefined vs Null:**
```
┌─────────────────────────────────────────────────┐
│              UNDEFINED vs NULL                  │
├─────────────────┬───────────────────────────────┤
│   undefined     │         null                  │
├─────────────────┼───────────────────────────────┤
│ Automatic       │ Manual                        │
│ "I forgot"      │ "I explicitly set to nothing" │
│ Variable exists │ Intentionally empty           │
│ but no value    │                               │
└─────────────────┴───────────────────────────────┘
```

---

### 6. Symbol (Advanced - Skip for Now)

**Why:** To create unique identifiers

**What:** Guaranteed to be unique

**How:**
```javascript
const sym1 = Symbol("description");
const sym2 = Symbol("description");

console.log(sym1 === sym2);  // false (always unique!)
```

---

### 7. BigInt (For Very Large Numbers)

**Why:** Number type has limitations for very large integers

**What:** Can represent integers larger than 2^53 - 1

**How:**
```javascript
const bigNumber = 1234567890123456789012345678901234567890n;
console.log(bigNumber);

// Must use BigInt for operations
const big1 = 100n;
const big2 = 200n;
console.log(big1 + big2);  // 300n
```

---

## Reference Types

### Object

**Why:** To group related data together

**What:** Collection of key-value pairs

**How:**
```javascript
const person = {
    name: "Alice",
    age: 25,
    isStudent: true,
    greet: function() {
        console.log("Hello!");
    }
};

// Accessing properties
console.log(person.name);      // "Alice" (dot notation)
console.log(person["age"]);    // 25 (bracket notation)

person.greet();  // "Hello!"
```

### Array

**Why:** To store lists of items

**What:** Ordered collection

**How:**
```javascript
const fruits = ["apple", "banana", "orange"];

console.log(fruits[0]);     // "apple"
console.log(fruits.length); // 3

fruits.push("grape");       // Add to end
console.log(fruits);        // ["apple", "banana", "orange", "grape"]
```

---

## Type Checking

```javascript
// typeof operator - tells you the type
console.log(typeof "hello");      // "string"
console.log(typeof 42);           // "number"
console.log(typeof true);         // "boolean"
console.log(typeof undefined);    // "undefined"
console.log(typeof null);         // "object" ⚠️ (This is a bug!)
console.log(typeof {});           // "object"
console.log(typeof []);           // "object" (arrays are objects)
console.log(typeof function(){}); // "function"
```

---

## Operators

### Arithmetic Operators

```javascript
let a = 10, b = 3;

console.log(a + b);  // 13  Addition
console.log(a - b);  // 7   Subtraction
console.log(a * b);  // 30  Multiplication
console.log(a / b);  // 3.333... Division
console.log(a % b);  // 1   Modulo (remainder)
console.log(a ** b); // 1000 Exponentiation (10^3)

// Increment/Decrement
let count = 0;
count++;  // count = count + 1
console.log(count);  // 1

count--;  // count = count - 1
console.log(count);  // 0
```

**Diagram: Modulo Operator**
```
10 % 3 = 1

    10 ÷ 3 = 3 remainder 1
    │      │         │
    │      │         └─── This is the modulo result
    │      └─── Quotient (not used)
    └─── Dividend

    Think: "How many are left over?"
    10 = 3 + 3 + 3 + 1
              └── This is 10 % 3
```

---

### Assignment Operators

```javascript
let x = 10;

// Shorthand operators
x += 5;   // x = x + 5;   → 15
x -= 3;   // x = x - 3;   → 12
x *= 2;   // x = x * 2;   → 24
x /= 4;   // x = x / 4;   → 6
x %= 4;   // x = x % 4;   → 2

console.log(x);  // 2
```

---

### Comparison Operators

```javascript
// Equality (checks value only after type conversion)
console.log(5 == "5");   // true ⚠️
console.log(5 === "5");  // false ✅ (strict - checks type too)

// Inequality
console.log(5 != "5");   // false
console.log(5 !== "5");  // true ✅

// Relational
console.log(10 > 5);     // true
console.log(10 < 5);     // false
console.log(10 >= 10);   // true
console.log(10 <= 5);    // false
```

**Important: Always use === and !==**

```
┌─────────────────────────────────────────────────┐
│          == vs ===                              │
├─────────────────┬───────────────────────────────┤
│       ==        │         ===                   │
├─────────────────┼───────────────────────────────┤
│ Loose equality  │ Strict equality               │
│ Converts types  │ Checks value AND type         │
│ ❌ DON'T USE    │ ✅ ALWAYS USE                 │
├─────────────────┼───────────────────────────────┤
│ 5 == "5" ✓     │ 5 === "5" ✗                  │
│ 0 == false ✓   │ 0 === false ✗                │
│ null == undef ✓│ null === undefined ✗          │
└─────────────────┴───────────────────────────────┘
```

---

### Logical Operators

```javascript
// AND (&&) - both must be true
console.log(true && true);    // true
console.log(true && false);   // false

// OR (||) - at least one must be true
console.log(true || false);   // true
console.log(false || false);  // false

// NOT (!) - inverts the value
console.log(!true);   // false
console.log(!false);  // true

// Practical example
const age = 25;
const hasLicense = true;

const canDrive = age >= 18 && hasLicense;
console.log(canDrive);  // true
```

**Diagram: Logical Operators**
```
┌─────────────────────────────────────────┐
│         AND (&&)                        │
│                                         │
│   true  && true  = true                 │
│   true  && false = false                │
│   false && true  = false                │
│   false && false = false                │
│                                         │
│   Think: BOTH must be true              │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│         OR (||)                         │
│                                         │
│   true  || true  = true                 │
│   true  || false = true                 │
│   false || true  = true                 │
│   false || false = false                │
│                                         │
│   Think: AT LEAST ONE must be true      │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│         NOT (!)                         │
│                                         │
│   !true  = false                        │
│   !false = true                         │
│                                         │
│   Think: OPPOSITE                       │
└─────────────────────────────────────────┘
```

---

### Ternary Operator (Shorthand if-else)

```javascript
// Syntax: condition ? valueIfTrue : valueIfFalse

const age = 20;
const status = age >= 18 ? "Adult" : "Minor";
console.log(status);  // "Adult"

// Equivalent to:
let status2;
if (age >= 18) {
    status2 = "Adult";
} else {
    status2 = "Minor";
}
```

---

## Type Conversion

### Implicit Conversion (Automatic)

```javascript
// JavaScript automatically converts types

// String + Number = String
console.log("5" + 3);      // "53" (number becomes string)

// Number operations with strings
console.log("5" - 3);      // 2 (string becomes number)
console.log("5" * 2);      // 10
console.log("5" / 1);      // 5

// Boolean conversions
console.log(true + 1);     // 2 (true becomes 1)
console.log(false + 1);    // 1 (false becomes 0)
```

### Explicit Conversion (Manual)

```javascript
// String to Number
const str = "123";
const num1 = Number(str);       // 123
const num2 = parseInt(str);     // 123
const num3 = parseFloat("3.14"); // 3.14
const num4 = +"123";            // 123 (unary plus)

console.log(typeof num1);  // "number"

// Number to String
const num = 123;
const str1 = String(num);       // "123"
const str2 = num.toString();    // "123"
const str3 = "" + num;          // "123"

console.log(typeof str1);  // "string"

// To Boolean
console.log(Boolean(1));        // true
console.log(Boolean(0));        // false
console.log(Boolean("hello"));  // true
console.log(Boolean(""));       // false
```

**Conversion Chart:**
```
┌───────────────────────────────────────────────────┐
│            TYPE CONVERSION                        │
├─────────────┬──────────────┬──────────────────────┤
│   Original  │  To Number   │   To Boolean         │
├─────────────┼──────────────┼──────────────────────┤
│   "123"     │     123      │      true            │
│   "hello"   │     NaN      │      true            │
│     ""      │      0       │      false           │
│   true      │      1       │      true            │
│   false     │      0       │      false           │
│   null      │      0       │      false           │
│  undefined  │     NaN      │      false           │
└─────────────┴──────────────┴──────────────────────┘
```

---

## Coding Examples

### Example 1: Variable Practice (Easy)

```javascript
// Declare variables
const firstName = "John";
const lastName = "Doe";
let age = 25;
const isStudent = true;

// Combine information
const fullName = `${firstName} ${lastName}`;
console.log(fullName);  // "John Doe"

// Update age (let allows this)
age = age + 1;
console.log(`Next year I'll be ${age}`);  // "Next year I'll be 26"

// Try to change const (will error)
// firstName = "Jane";  // Error!
```

---

### Example 2: Type Checking (Easy)

```javascript
// Check types of different values
const name = "Alice";
const age = 30;
const isActive = true;
let notAssigned;
const empty = null;

console.log(typeof name);        // "string"
console.log(typeof age);         // "number"
console.log(typeof isActive);    // "boolean"
console.log(typeof notAssigned); // "undefined"
console.log(typeof empty);       // "object" (quirk!)

// Function to check and describe type
function describeType(value) {
    const type = typeof value;
    console.log(`The value ${value} is of type: ${type}`);
}

describeType(42);        // The value 42 is of type: number
describeType("Hello");   // The value Hello is of type: string
```

---

### Example 3: Calculator (Medium)

```javascript
// Simple calculator
function calculate(num1, num2, operator) {
    let result;

    if (operator === "+") {
        result = num1 + num2;
    } else if (operator === "-") {
        result = num1 - num2;
    } else if (operator === "*") {
        result = num1 * num2;
    } else if (operator === "/") {
        if (num2 === 0) {
            return "Error: Cannot divide by zero";
        }
        result = num1 / num2;
    } else {
        return "Error: Invalid operator";
    }

    return result;
}

// Test the calculator
console.log(calculate(10, 5, "+"));   // 15
console.log(calculate(10, 5, "-"));   // 5
console.log(calculate(10, 5, "*"));   // 50
console.log(calculate(10, 5, "/"));   // 2
console.log(calculate(10, 0, "/"));   // Error: Cannot divide by zero
```

---

### Example 4: Type Conversion Challenge (Medium)

```javascript
// Handle different input types
function processInput(input) {
    console.log(`Original: ${input}, Type: ${typeof input}`);

    // Convert to number
    const asNumber = Number(input);
    console.log(`As number: ${asNumber}`);

    // Convert to string
    const asString = String(input);
    console.log(`As string: "${asString}"`);

    // Convert to boolean
    const asBoolean = Boolean(input);
    console.log(`As boolean: ${asBoolean}`);

    console.log("---");
}

processInput(123);
processInput("456");
processInput(true);
processInput("");
processInput(0);
processInput(null);
```

**Output:**
```
Original: 123, Type: number
As number: 123
As string: "123"
As boolean: true
---
Original: 456, Type: string
As number: 456
As string: "456"
As boolean: true
---
Original: true, Type: boolean
As number: 1
As string: "true"
As boolean: true
---
Original: , Type: string
As number: 0
As string: ""
As boolean: false
---
Original: 0, Type: number
As number: 0
As string: "0"
As boolean: false
---
Original: null, Type: object
As number: 0
As string: "null"
As boolean: false
---
```

---

### Example 5: Advanced String Manipulation (Hard)

```javascript
// String utility functions
const StringUtils = {
    // Reverse a string
    reverse: function(str) {
        return str.split("").reverse().join("");
    },

    // Count vowels
    countVowels: function(str) {
        const vowels = "aeiouAEIOU";
        let count = 0;

        for (let char of str) {
            if (vowels.includes(char)) {
                count++;
            }
        }

        return count;
    },

    // Check if palindrome
    isPalindrome: function(str) {
        const cleaned = str.toLowerCase().replace(/[^a-z0-9]/g, "");
        return cleaned === this.reverse(cleaned);
    },

    // Title case
    toTitleCase: function(str) {
        return str
            .toLowerCase()
            .split(" ")
            .map(word => word[0].toUpperCase() + word.slice(1))
            .join(" ");
    },

    // Count words
    countWords: function(str) {
        return str.trim().split(/\s+/).length;
    }
};

// Test the utilities
console.log(StringUtils.reverse("hello"));              // "olleh"
console.log(StringUtils.countVowels("JavaScript"));     // 3
console.log(StringUtils.isPalindrome("racecar"));       // true
console.log(StringUtils.isPalindrome("hello"));         // false
console.log(StringUtils.toTitleCase("hello world"));    // "Hello World"
console.log(StringUtils.countWords("The quick brown")); // 3
```

---

### Example 6: Number Operations (Hard)

```javascript
// Advanced number operations
const NumberUtils = {
    // Check if prime
    isPrime: function(num) {
        if (num <= 1) return false;
        if (num <= 3) return true;

        for (let i = 2; i <= Math.sqrt(num); i++) {
            if (num % i === 0) return false;
        }

        return true;
    },

    // Factorial
    factorial: function(n) {
        if (n === 0 || n === 1) return 1;
        return n * this.factorial(n - 1);
    },

    // Fibonacci
    fibonacci: function(n) {
        if (n <= 1) return n;

        let a = 0, b = 1;
        for (let i = 2; i <= n; i++) {
            const temp = a + b;
            a = b;
            b = temp;
        }

        return b;
    },

    // GCD (Greatest Common Divisor)
    gcd: function(a, b) {
        while (b !== 0) {
            const temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    },

    // Random integer in range
    randomInt: function(min, max) {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }
};

// Test the utilities
console.log(NumberUtils.isPrime(17));        // true
console.log(NumberUtils.factorial(5));       // 120
console.log(NumberUtils.fibonacci(7));       // 13
console.log(NumberUtils.gcd(48, 18));        // 6
console.log(NumberUtils.randomInt(1, 10));   // Random between 1-10
```

---

## Common Mistakes

### Mistake 1: Using == Instead of ===

```javascript
// ❌ Wrong
if (5 == "5") {
    console.log("This will run!");  // Bad!
}

// ✅ Correct
if (5 === "5") {
    console.log("This won't run");  // Good!
}
```

**Why:** `==` converts types, leading to unexpected results. Always use `===`.

---

### Mistake 2: Changing const

```javascript
// ❌ Wrong
const score = 10;
score = 20;  // Error: Assignment to constant variable

// ✅ Correct
let score = 10;
score = 20;  // Works!
```

**Why:** `const` means constant - it cannot be reassigned.

---

### Mistake 3: typeof null

```javascript
// ❌ Careful!
console.log(typeof null);  // "object" (this is a bug!)

// ✅ Correct way to check for null
const value = null;
if (value === null) {
    console.log("It's null");
}
```

**Why:** `typeof null` returns "object" due to a historical bug in JavaScript.

---

### Mistake 4: NaN Comparison

```javascript
// ❌ Wrong
const result = Number("hello");
if (result === NaN) {  // This doesn't work!
    console.log("Not a number");
}

// ✅ Correct
if (isNaN(result)) {
    console.log("Not a number");
}

// ✅ Also correct (modern way)
if (Number.isNaN(result)) {
    console.log("Not a number");
}
```

**Why:** `NaN` is not equal to anything, including itself: `NaN === NaN` is `false`!

---

### Mistake 5: String and Number Concatenation

```javascript
// ❌ Unexpected behavior
console.log("5" + 3);   // "53" (string)
console.log("5" - 3);   // 2 (number)

// ✅ Explicit conversion
console.log(Number("5") + 3);  // 8
console.log("5" + String(3));  // "53"
```

**Why:** `+` with strings concatenates, but `-` always does math.

---

## Practice Exercises

### Exercise 1: Variable Swap (Easy)
Swap the values of two variables without using a third variable.

```javascript
let a = 5;
let b = 10;

// Your code here
// Hint: Use arithmetic or array destructuring

console.log(a);  // Should be 10
console.log(b);  // Should be 5
```

---

### Exercise 2: Type Converter (Medium)
Create a function that converts a value to a specific type.

```javascript
function convertTo(value, targetType) {
    // Your code here
    // targetType can be: "string", "number", "boolean"
}

console.log(convertTo(123, "string"));      // "123"
console.log(convertTo("456", "number"));    // 456
console.log(convertTo(1, "boolean"));       // true
```

---

### Exercise 3: Validator (Hard)
Create a function that validates different data types.

```javascript
function validateData(value, expectedType) {
    // Return object with:
    // - isValid: boolean
    // - actualType: string
    // - message: string

    // Your code here
}

console.log(validateData(123, "number"));
// { isValid: true, actualType: "number", message: "Valid" }

console.log(validateData("hello", "number"));
// { isValid: false, actualType: "string", message: "Expected number but got string" }
```

---

## Interview Questions

### Easy Questions

**Q1: What is the difference between let, const, and var?**

**Answer:**
- `let`: Block-scoped, can be reassigned, cannot be redeclared
- `const`: Block-scoped, cannot be reassigned, cannot be redeclared
- `var`: Function-scoped, can be reassigned, can be redeclared (avoid!)

---

**Q2: What are the primitive data types in JavaScript?**

**Answer:**
7 primitive types: String, Number, Boolean, Undefined, Null, Symbol, BigInt

---

**Q3: What is the difference between == and ===?**

**Answer:**
- `==`: Loose equality, converts types before comparing
- `===`: Strict equality, checks both value and type (always use this!)

---

### Medium Questions

**Q4: What is type coercion?**

**Answer:**
Type coercion is JavaScript's automatic conversion of values from one type to another. Example:
```javascript
console.log("5" + 3);  // "53" (number to string)
console.log("5" - 3);  // 2 (string to number)
```

---

**Q5: What is the difference between null and undefined?**

**Answer:**
- `undefined`: Variable declared but not assigned a value
- `null`: Intentionally assigned empty value
- `undefined` is automatic, `null` is manual

---

## Key Takeaways

✅ Use `const` by default, `let` when you need to reassign
✅ Never use `var`
✅ Always use `===` instead of `==`
✅ Understand the difference between primitive and reference types
✅ Be aware of type coercion and conversion
✅ `typeof` is your friend for checking types
✅ Practice with different data types to build intuition

---

## Next Steps

🎉 **Congratulations!** You've mastered JavaScript fundamentals!

**Ready for more?** Move on to [Phase 3: Control Flow and Functions](../phase-03-control-flow/README.md)

---

## Quick Reference Card

```javascript
// Variables
const name = "value";  // Cannot reassign
let count = 0;         // Can reassign

// Data Types
typeof "text"      // "string"
typeof 42          // "number"
typeof true        // "boolean"
typeof undefined   // "undefined"
typeof null        // "object" ⚠️
typeof {}          // "object"
typeof []          // "object"

// Operators
5 + 3              // 8 (addition)
5 === "5"          // false (strict equality)
true && false      // false (logical AND)
true || false      // true (logical OR)
!true              // false (logical NOT)

// Conversion
Number("123")      // 123
String(123)        // "123"
Boolean(1)         // true
```
