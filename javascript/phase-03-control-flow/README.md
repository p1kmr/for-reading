# Phase 3: Control Flow and Functions

## 📋 Table of Contents
1. [Control Flow Introduction](#control-flow-introduction)
2. [If-Else Statements](#if-else-statements)
3. [Switch Statements](#switch-statements)
4. [Loops](#loops)
5. [Functions](#functions)
6. [Coding Examples](#coding-examples)
7. [Common Mistakes](#common-mistakes)
8. [Practice Exercises](#practice-exercises)

---

## Control Flow Introduction

### Why Do We Need Control Flow?

Programs need to make decisions and repeat tasks. Without control flow, code would only run line by line, top to bottom.

```
┌────────────────────────────────────────────────┐
│        WITHOUT CONTROL FLOW                    │
│                                                │
│   Line 1 ──► Execute                          │
│   Line 2 ──► Execute                          │
│   Line 3 ──► Execute                          │
│   Line 4 ──► Execute                          │
│                                                │
│   (No decisions, no repetition)                │
└────────────────────────────────────────────────┘

┌────────────────────────────────────────────────┐
│        WITH CONTROL FLOW                       │
│                                                │
│   Start                                        │
│     │                                          │
│     ▼                                          │
│   Decision? ──Yes──► Do A                     │
│     │                                          │
│     No                                         │
│     │                                          │
│     ▼                                          │
│   Loop 5 times ──► Do B repeatedly            │
│     │                                          │
│     ▼                                          │
│   End                                          │
│                                                │
└────────────────────────────────────────────────┘
```

---

## If-Else Statements

### The Why
To make decisions based on conditions.

### The What
Execute different code based on whether a condition is true or false.

### The How

#### Basic If Statement

```javascript
// Syntax
if (condition) {
    // Code runs if condition is true
}

// Example
const age = 20;

if (age >= 18) {
    console.log("You are an adult");
}
// Output: "You are an adult"
```

**Diagram:**
```
         Start
           │
           ▼
      ┌─────────┐
      │ age >=18│──── No ────► (skip)
      └─────────┘                │
           │ Yes                 │
           ▼                     │
    Print "adult"                │
           │                     │
           └──────────┬──────────┘
                      ▼
                     End
```

---

#### If-Else Statement

```javascript
const age = 15;

if (age >= 18) {
    console.log("You are an adult");
} else {
    console.log("You are a minor");
}
// Output: "You are a minor"
```

**Diagram:**
```
         Start
           │
           ▼
      ┌─────────┐
      │ age >=18│
      └─────────┘
       /        \
     Yes        No
     /            \
    ▼              ▼
"adult"        "minor"
    \              /
     \            /
      ▼          ▼
         End
```

---

#### If-Else If-Else (Multiple Conditions)

```javascript
const score = 75;

if (score >= 90) {
    console.log("Grade: A");
} else if (score >= 80) {
    console.log("Grade: B");
} else if (score >= 70) {
    console.log("Grade: C");
} else if (score >= 60) {
    console.log("Grade: D");
} else {
    console.log("Grade: F");
}
// Output: "Grade: C"
```

**Diagram:**
```
    Start
      │
      ▼
  score >= 90? ──Yes──► "A"
      │ No               │
      ▼                  │
  score >= 80? ──Yes──► "B"
      │ No               │
      ▼                  │
  score >= 70? ──Yes──► "C"
      │ No               │
      ▼                  │
  score >= 60? ──Yes──► "D"
      │ No               │
      ▼                  │
      "F" ◄──────────────┘
      │
      ▼
     End
```

---

#### Nested If Statements

```javascript
const age = 25;
const hasLicense = true;

if (age >= 18) {
    if (hasLicense) {
        console.log("You can drive");
    } else {
        console.log("You need a license");
    }
} else {
    console.log("You are too young to drive");
}
// Output: "You can drive"
```

**Diagram:**
```
         Start
           │
           ▼
      age >= 18?
       /      \
     No        Yes
     /          \
    ▼            ▼
"too young"  hasLicense?
    │         /        \
    │       Yes        No
    │       /            \
    │      ▼              ▼
    │  "can drive"  "need license"
    │      │              │
    └──────┴──────────────┘
           │
           ▼
          End
```

---

### Example: Complete Decision System (Easy)

```javascript
function checkEligibility(age, hasID) {
    if (age < 0) {
        return "Invalid age";
    } else if (age < 13) {
        return "Child - No ID required";
    } else if (age < 18) {
        if (hasID) {
            return "Teen - ID verified";
        } else {
            return "Teen - ID required";
        }
    } else {
        if (hasID) {
            return "Adult - Access granted";
        } else {
            return "Adult - ID required";
        }
    }
}

console.log(checkEligibility(25, true));   // "Adult - Access granted"
console.log(checkEligibility(16, false));  // "Teen - ID required"
console.log(checkEligibility(10, false));  // "Child - No ID required"
console.log(checkEligibility(-5, true));   // "Invalid age"
```

---

## Switch Statements

### The Why
When you have many specific values to check against one variable, switch is cleaner than multiple if-else.

### The What
Checks a value against multiple cases.

### The How

```javascript
const day = "Monday";

switch (day) {
    case "Monday":
        console.log("Start of the week");
        break;
    case "Tuesday":
    case "Wednesday":
    case "Thursday":
        console.log("Middle of the week");
        break;
    case "Friday":
        console.log("Almost weekend!");
        break;
    case "Saturday":
    case "Sunday":
        console.log("Weekend!");
        break;
    default:
        console.log("Invalid day");
}
// Output: "Start of the week"
```

**Important: The `break` keyword!**
```javascript
// ❌ Without break (fall-through)
const num = 1;
switch (num) {
    case 1:
        console.log("One");
        // No break! Falls through to next case
    case 2:
        console.log("Two");
        // No break! Falls through to next case
    case 3:
        console.log("Three");
}
// Output: "One" "Two" "Three"

// ✅ With break
switch (num) {
    case 1:
        console.log("One");
        break;  // Stops here
    case 2:
        console.log("Two");
        break;
    case 3:
        console.log("Three");
        break;
}
// Output: "One"
```

**Diagram: Switch Flow**
```
         Start
           │
           ▼
       [value]
           │
           ├──── case 1? ──Yes──► Action 1 ──► break ──┐
           │                                            │
           ├──── case 2? ──Yes──► Action 2 ──► break ──┤
           │                                            │
           ├──── case 3? ──Yes──► Action 3 ──► break ──┤
           │                                            │
           └──── default ──────► Default Action ───────┤
                                                        │
                                                        ▼
                                                       End
```

---

### Example: Grade Calculator (Medium)

```javascript
function getGrade(score) {
    // Normalize score to letter
    let grade;

    if (score >= 90) grade = 'A';
    else if (score >= 80) grade = 'B';
    else if (score >= 70) grade = 'C';
    else if (score >= 60) grade = 'D';
    else grade = 'F';

    // Provide feedback based on grade
    switch (grade) {
        case 'A':
            return "Excellent! Score: " + score;
        case 'B':
            return "Good job! Score: " + score;
        case 'C':
            return "Satisfactory. Score: " + score;
        case 'D':
            return "Needs improvement. Score: " + score;
        case 'F':
            return "Failed. Score: " + score;
        default:
            return "Invalid score";
    }
}

console.log(getGrade(95));  // "Excellent! Score: 95"
console.log(getGrade(75));  // "Satisfactory. Score: 75"
console.log(getGrade(55));  // "Failed. Score: 55"
```

---

## Loops

### The Why
To repeat code multiple times without writing it multiple times.

### The What
Loops execute a block of code repeatedly.

---

### 1. For Loop

**When to use:** When you know how many times to loop.

```javascript
// Syntax
for (initialization; condition; increment) {
    // Code to repeat
}

// Example: Print numbers 1 to 5
for (let i = 1; i <= 5; i++) {
    console.log(i);
}
// Output: 1 2 3 4 5
```

**Diagram: For Loop Execution**
```
┌──────────────────────────────────────────┐
│         FOR LOOP FLOW                    │
│                                          │
│  1. Initialize: let i = 1                │
│           │                              │
│           ▼                              │
│  2. Check condition: i <= 5? ────No────► Exit
│           │ Yes                          │
│           ▼                              │
│  3. Execute code block                   │
│           │                              │
│           ▼                              │
│  4. Increment: i++                       │
│           │                              │
│           └─────► Go back to step 2      │
│                                          │
└──────────────────────────────────────────┘

Iterations:
i = 1: Check (1 <= 5)? Yes → Execute → i becomes 2
i = 2: Check (2 <= 5)? Yes → Execute → i becomes 3
i = 3: Check (3 <= 5)? Yes → Execute → i becomes 4
i = 4: Check (4 <= 5)? Yes → Execute → i becomes 5
i = 5: Check (5 <= 5)? Yes → Execute → i becomes 6
i = 6: Check (6 <= 5)? No  → Exit loop
```

**Common patterns:**
```javascript
// Count up
for (let i = 0; i < 5; i++) {
    console.log(i);  // 0, 1, 2, 3, 4
}

// Count down
for (let i = 5; i > 0; i--) {
    console.log(i);  // 5, 4, 3, 2, 1
}

// Count by 2s
for (let i = 0; i <= 10; i += 2) {
    console.log(i);  // 0, 2, 4, 6, 8, 10
}

// Loop through array
const fruits = ["apple", "banana", "orange"];
for (let i = 0; i < fruits.length; i++) {
    console.log(fruits[i]);
}
```

---

### 2. While Loop

**When to use:** When you don't know how many times to loop.

```javascript
// Syntax
while (condition) {
    // Code to repeat
}

// Example: Count to 5
let count = 1;
while (count <= 5) {
    console.log(count);
    count++;
}
// Output: 1 2 3 4 5
```

**Diagram:**
```
    Start
      │
      ▼
  Initialize count = 1
      │
      ▼
  ┌───────────┐
◄─┤ count <=5?│
│ └───────────┘
│   │ No
│   ▼
│  Exit
│
│ Yes
└──► Execute code
     │
     Increment count
     │
     (Go back to check)
```

**Important: Avoid infinite loops!**
```javascript
// ❌ INFINITE LOOP - Don't do this!
let i = 0;
while (i < 5) {
    console.log(i);
    // Forgot to increment i!
    // This will run forever!
}

// ✅ Correct
let i = 0;
while (i < 5) {
    console.log(i);
    i++;  // Don't forget this!
}
```

---

### 3. Do-While Loop

**When to use:** When you want to execute at least once, then check condition.

```javascript
// Syntax
do {
    // Code to repeat (runs at least once)
} while (condition);

// Example
let num = 0;
do {
    console.log(num);
    num++;
} while (num < 3);
// Output: 0 1 2

// Difference: runs once even if condition is false
let x = 10;
do {
    console.log("This runs once!");
} while (x < 5);  // False, but still ran once
```

**Diagram: While vs Do-While**
```
┌─────────────────────┐  ┌─────────────────────┐
│    WHILE LOOP       │  │   DO-WHILE LOOP     │
│                     │  │                     │
│   Check condition   │  │   Execute code      │
│         │           │  │         │           │
│      False          │  │         ▼           │
│         ▼           │  │   Check condition   │
│    Exit (skip)      │  │         │           │
│         │           │  │      False          │
│      True           │  │         ▼           │
│         ▼           │  │       Exit          │
│   Execute code      │  │                     │
│         │           │  │   Runs at least     │
│    (loop back)      │  │   once!             │
│                     │  │                     │
└─────────────────────┘  └─────────────────────┘
```

---

### 4. For...of Loop (Modern)

**When to use:** To loop through arrays easily.

```javascript
const fruits = ["apple", "banana", "orange"];

// Old way with for loop
for (let i = 0; i < fruits.length; i++) {
    console.log(fruits[i]);
}

// ✅ Modern way with for...of
for (const fruit of fruits) {
    console.log(fruit);
}
// Output: "apple" "banana" "orange"
```

---

### 5. For...in Loop

**When to use:** To loop through object properties.

```javascript
const person = {
    name: "Alice",
    age: 25,
    city: "New York"
};

for (const key in person) {
    console.log(`${key}: ${person[key]}`);
}
// Output:
// "name: Alice"
// "age: 25"
// "city: New York"
```

---

### Loop Control: Break and Continue

#### Break - Exit loop early

```javascript
// Find first even number
for (let i = 1; i <= 10; i++) {
    if (i % 2 === 0) {
        console.log("First even number:", i);
        break;  // Exit the loop
    }
}
// Output: "First even number: 2"
```

#### Continue - Skip current iteration

```javascript
// Print only odd numbers
for (let i = 1; i <= 5; i++) {
    if (i % 2 === 0) {
        continue;  // Skip to next iteration
    }
    console.log(i);
}
// Output: 1 3 5
```

**Diagram:**
```
┌──────────────────────────────────────────┐
│             BREAK                        │
│                                          │
│  for (let i = 1; i <= 5; i++) {          │
│      if (i === 3) break;                 │
│      print(i);                           │
│  }                                       │
│                                          │
│  i=1: print 1                            │
│  i=2: print 2                            │
│  i=3: BREAK → exit loop                  │
│  (4 and 5 never execute)                 │
│                                          │
└──────────────────────────────────────────┘

┌──────────────────────────────────────────┐
│            CONTINUE                      │
│                                          │
│  for (let i = 1; i <= 5; i++) {          │
│      if (i === 3) continue;              │
│      print(i);                           │
│  }                                       │
│                                          │
│  i=1: print 1                            │
│  i=2: print 2                            │
│  i=3: CONTINUE → skip print, go to i=4   │
│  i=4: print 4                            │
│  i=5: print 5                            │
│                                          │
└──────────────────────────────────────────┘
```

---

## Functions

### The Why
- Reusability: Write once, use many times
- Organization: Break complex problems into smaller pieces
- Abstraction: Hide implementation details

### The What
A function is a reusable block of code that performs a specific task.

---

### 1. Function Declaration

```javascript
// Syntax
function functionName(parameters) {
    // Code to execute
    return result;
}

// Example
function greet(name) {
    return "Hello, " + name + "!";
}

const message = greet("Alice");
console.log(message);  // "Hello, Alice!"
```

**Diagram: Function Anatomy**
```
┌────────────────────────────────────────────────┐
│         function greet(name) {                 │
│            │       │     │                     │
│            │       │     └──► Parameter        │
│            │       └────────► Function name    │
│            └────────────────► Keyword          │
│                                                │
│             return "Hello, " + name;           │
│                │                               │
│                └───────────────► Return value  │
│         }                                      │
└────────────────────────────────────────────────┘

Function Call:
┌────────────────────────────────────────────────┐
│         greet("Alice")                         │
│           │     │                              │
│           │     └──────► Argument              │
│           └────────────► Function name         │
│                                                │
│  Process:                                      │
│  1. Call function with "Alice"                 │
│  2. name parameter receives "Alice"            │
│  3. Execute function body                      │
│  4. Return "Hello, Alice!"                     │
└────────────────────────────────────────────────┘
```

---

### 2. Function Expression

```javascript
// Store function in a variable
const greet = function(name) {
    return "Hello, " + name + "!";
};

console.log(greet("Bob"));  // "Hello, Bob!"
```

**Difference: Hoisting**
```javascript
// ✅ Function Declaration - Hoisted
greet1("Alice");  // Works!

function greet1(name) {
    return "Hello, " + name;
}

// ❌ Function Expression - Not Hoisted
greet2("Bob");  // Error: Cannot access before initialization

const greet2 = function(name) {
    return "Hello, " + name;
};
```

---

### 3. Arrow Functions (Modern)

```javascript
// Traditional function
const add1 = function(a, b) {
    return a + b;
};

// ✅ Arrow function (ES6+)
const add2 = (a, b) => {
    return a + b;
};

// ✅ Even shorter (implicit return)
const add3 = (a, b) => a + b;

// Single parameter - no parentheses needed
const square = x => x * x;

// No parameters - need empty parentheses
const greet = () => "Hello!";

console.log(add3(5, 3));   // 8
console.log(square(4));    // 16
console.log(greet());      // "Hello!"
```

**Arrow Function Evolution:**
```
┌────────────────────────────────────────────────┐
│         ARROW FUNCTION SHORTHAND               │
│                                                │
│  // Full syntax                                │
│  const add = (a, b) => {                       │
│      return a + b;                             │
│  };                                            │
│                                                │
│  // Remove braces for single expression        │
│  const add = (a, b) => a + b;                  │
│                                                │
│  // Single parameter - remove parentheses      │
│  const square = x => x * x;                    │
│                                                │
│  // No parameters - keep parentheses           │
│  const greet = () => "Hello";                  │
│                                                │
└────────────────────────────────────────────────┘
```

---

### Parameters and Arguments

#### Default Parameters

```javascript
// Without default
function greet1(name) {
    return "Hello, " + name;
}
console.log(greet1());  // "Hello, undefined"

// ✅ With default
function greet2(name = "Guest") {
    return "Hello, " + name;
}
console.log(greet2());          // "Hello, Guest"
console.log(greet2("Alice"));   // "Hello, Alice"
```

#### Rest Parameters

```javascript
// Collect multiple arguments into an array
function sum(...numbers) {
    let total = 0;
    for (const num of numbers) {
        total += num;
    }
    return total;
}

console.log(sum(1, 2, 3));        // 6
console.log(sum(1, 2, 3, 4, 5));  // 15
```

---

### Return Statement

```javascript
// Function with return
function add(a, b) {
    return a + b;
    console.log("This never runs");  // Unreachable code
}

// Function without return (returns undefined)
function greet(name) {
    console.log("Hello, " + name);
    // No return statement
}

const result1 = add(5, 3);
console.log(result1);  // 8

const result2 = greet("Alice");
console.log(result2);  // undefined
```

**Return Flow:**
```
┌────────────────────────────────────────┐
│      function calculate(x) {           │
│          if (x < 0) {                  │
│              return "Negative";        │
│              // Stops here if true     │
│          }                             │
│                                        │
│          const result = x * 2;         │
│          return result;                │
│      }                                 │
│                                        │
│  calculate(-5) → "Negative"            │
│  calculate(10) → 20                    │
└────────────────────────────────────────┘
```

---

## Coding Examples

### Example 1: FizzBuzz (Easy-Medium)

```javascript
// Print numbers 1-100, but:
// - "Fizz" for multiples of 3
// - "Buzz" for multiples of 5
// - "FizzBuzz" for multiples of both

function fizzBuzz(n) {
    for (let i = 1; i <= n; i++) {
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
}

fizzBuzz(15);
// Output: 1 2 Fizz 4 Buzz Fizz 7 8 Fizz Buzz 11 Fizz 13 14 FizzBuzz
```

---

### Example 2: Factorial (Medium)

```javascript
// Factorial: n! = n × (n-1) × (n-2) × ... × 1
// Example: 5! = 5 × 4 × 3 × 2 × 1 = 120

// Iterative approach
function factorialIterative(n) {
    if (n < 0) return undefined;
    if (n === 0 || n === 1) return 1;

    let result = 1;
    for (let i = 2; i <= n; i++) {
        result *= i;
    }
    return result;
}

// Recursive approach
function factorialRecursive(n) {
    if (n < 0) return undefined;
    if (n === 0 || n === 1) return 1;
    return n * factorialRecursive(n - 1);
}

console.log(factorialIterative(5));   // 120
console.log(factorialRecursive(5));   // 120
```

**Recursion Diagram:**
```
factorial(5)
    │
    └─► 5 * factorial(4)
            │
            └─► 4 * factorial(3)
                    │
                    └─► 3 * factorial(2)
                            │
                            └─► 2 * factorial(1)
                                    │
                                    └─► 1 (base case)

Unwinding:
1
2 * 1 = 2
3 * 2 = 6
4 * 6 = 24
5 * 24 = 120
```

---

### Example 3: Palindrome Checker (Medium)

```javascript
// Check if a string reads the same forwards and backwards

function isPalindrome(str) {
    // Clean the string: lowercase, remove non-alphanumeric
    const cleaned = str.toLowerCase().replace(/[^a-z0-9]/g, '');

    // Check if it equals its reverse
    const reversed = cleaned.split('').reverse().join('');
    return cleaned === reversed;
}

// Alternative: Two-pointer approach
function isPalindrome2(str) {
    const cleaned = str.toLowerCase().replace(/[^a-z0-9]/g, '');
    let left = 0;
    let right = cleaned.length - 1;

    while (left < right) {
        if (cleaned[left] !== cleaned[right]) {
            return false;
        }
        left++;
        right--;
    }

    return true;
}

console.log(isPalindrome("racecar"));              // true
console.log(isPalindrome("A man a plan a canal Panama"));  // true
console.log(isPalindrome("hello"));                // false
```

**Two-Pointer Visualization:**
```
"racecar"
 ↑     ↑
 L     R   Compare: r === r ✓

"racecar"
  ↑   ↑
  L   R    Compare: a === a ✓

"racecar"
   ↑ ↑
   L R     Compare: c === c ✓

"racecar"
    ↑
   L/R     L >= R, stop → Palindrome!
```

---

### Example 4: Prime Number Checker (Hard)

```javascript
// Check if a number is prime (only divisible by 1 and itself)

function isPrime(num) {
    // Handle edge cases
    if (num <= 1) return false;
    if (num <= 3) return true;

    // Check if divisible by 2 or 3
    if (num % 2 === 0 || num % 3 === 0) return false;

    // Check for divisors up to √num
    // Only check numbers of form 6k ± 1
    for (let i = 5; i * i <= num; i += 6) {
        if (num % i === 0 || num % (i + 2) === 0) {
            return false;
        }
    }

    return true;
}

// Get all primes up to n
function getPrimesUpTo(n) {
    const primes = [];
    for (let i = 2; i <= n; i++) {
        if (isPrime(i)) {
            primes.push(i);
        }
    }
    return primes;
}

console.log(isPrime(17));          // true
console.log(isPrime(20));          // false
console.log(getPrimesUpTo(20));    // [2, 3, 5, 7, 11, 13, 17, 19]
```

**Why check only up to √num?**
```
Example: Is 36 prime?

Factors of 36: 1×36, 2×18, 3×12, 4×9, 6×6

Notice: After 6 (√36), factors repeat in reverse
        6 is the "middle" point

Therefore: If no divisor found up to √n,
          then n is prime!

Efficiency:
- Naive: Check all numbers from 2 to n-1
- Optimized: Check only up to √n
- For n=100: Check 2-99 (97 checks) vs 2-10 (8 checks)
```

---

### Example 5: Pattern Printing (Medium)

```javascript
// Print various patterns

// Pattern 1: Right triangle
function printTriangle(n) {
    for (let i = 1; i <= n; i++) {
        let row = '';
        for (let j = 1; j <= i; j++) {
            row += '* ';
        }
        console.log(row);
    }
}

printTriangle(5);
/*
*
* *
* * *
* * * *
* * * * *
*/

// Pattern 2: Pyramid
function printPyramid(n) {
    for (let i = 1; i <= n; i++) {
        let row = '';

        // Add spaces
        for (let j = 1; j <= n - i; j++) {
            row += '  ';
        }

        // Add stars
        for (let j = 1; j <= 2 * i - 1; j++) {
            row += '* ';
        }

        console.log(row);
    }
}

printPyramid(5);
/*
        *
      * * *
    * * * * *
  * * * * * * *
* * * * * * * * *
*/

// Pattern 3: Number pyramid
function printNumberPyramid(n) {
    for (let i = 1; i <= n; i++) {
        let row = '';

        // Spaces
        for (let j = 1; j <= n - i; j++) {
            row += '  ';
        }

        // Ascending numbers
        for (let j = 1; j <= i; j++) {
            row += j + ' ';
        }

        // Descending numbers
        for (let j = i - 1; j >= 1; j--) {
            row += j + ' ';
        }

        console.log(row);
    }
}

printNumberPyramid(5);
/*
        1
      1 2 1
    1 2 3 2 1
  1 2 3 4 3 2 1
1 2 3 4 5 4 3 2 1
*/
```

---

## Common Mistakes

### Mistake 1: Infinite Loops

```javascript
// ❌ Wrong - Infinite loop
let i = 0;
while (i < 5) {
    console.log(i);
    // Forgot to increment!
}

// ✅ Correct
let i = 0;
while (i < 5) {
    console.log(i);
    i++;  // Don't forget this!
}
```

---

### Mistake 2: Off-by-One Errors

```javascript
// ❌ Wrong - Loops 6 times (0-5)
for (let i = 0; i <= 5; i++) {
    console.log(i);
}

// ✅ Correct - Loops 5 times (0-4)
for (let i = 0; i < 5; i++) {
    console.log(i);
}
```

---

### Mistake 3: Forgetting Return

```javascript
// ❌ Wrong
function add(a, b) {
    a + b;  // Calculated but not returned!
}

const result = add(5, 3);
console.log(result);  // undefined

// ✅ Correct
function add(a, b) {
    return a + b;
}
```

---

### Mistake 4: Modifying Loop Variable

```javascript
// ❌ Wrong - Confusing
for (let i = 0; i < 5; i++) {
    i = i + 2;  // Don't do this!
    console.log(i);
}

// ✅ Correct - Use appropriate increment
for (let i = 0; i < 5; i += 3) {
    console.log(i);
}
```

---

### Mistake 5: Switch Without Break

```javascript
// ❌ Wrong - Falls through
const day = "Monday";
switch (day) {
    case "Monday":
        console.log("Start of week");
        // No break!
    case "Tuesday":
        console.log("Second day");
        // No break!
    case "Wednesday":
        console.log("Mid week");
}
// Prints all three!

// ✅ Correct
switch (day) {
    case "Monday":
        console.log("Start of week");
        break;
    case "Tuesday":
        console.log("Second day");
        break;
    case "Wednesday":
        console.log("Mid week");
        break;
}
```

---

## Practice Exercises

### Exercise 1: Sum of Array (Easy)
```javascript
// Write a function that returns the sum of all numbers in an array
function sumArray(arr) {
    // Your code here
}

console.log(sumArray([1, 2, 3, 4, 5]));  // 15
```

---

### Exercise 2: Reverse String (Medium)
```javascript
// Write a function that reverses a string without using .reverse()
function reverseString(str) {
    // Your code here
}

console.log(reverseString("hello"));  // "olleh"
```

---

### Exercise 3: Fibonacci Sequence (Hard)
```javascript
// Return the nth Fibonacci number
// Sequence: 0, 1, 1, 2, 3, 5, 8, 13, 21...
function fibonacci(n) {
    // Your code here
}

console.log(fibonacci(7));  // 13
```

---

## Key Takeaways

✅ Use if-else for general conditions, switch for multiple specific values
✅ Choose the right loop: for (known iterations), while (unknown iterations)
✅ Always increment loop counters to avoid infinite loops
✅ Use arrow functions for concise code
✅ Remember to return values from functions
✅ Break and continue control loop execution
✅ Test edge cases (empty inputs, negative numbers, etc.)

---

## Next Steps

🎉 **Congratulations!** You've mastered control flow and functions!

**Ready for more?** Move on to [Phase 4: Arrays and Objects](../phase-04-arrays-objects/README.md)

---

## Quick Reference

```javascript
// If-Else
if (condition) {
    // code
} else if (otherCondition) {
    // code
} else {
    // code
}

// Switch
switch (value) {
    case x:
        // code
        break;
    default:
        // code
}

// Loops
for (let i = 0; i < n; i++) { }
while (condition) { }
do { } while (condition);
for (const item of array) { }
for (const key in object) { }

// Functions
function name(params) { return value; }
const name = (params) => value;

// Break/Continue
break;      // Exit loop
continue;   // Skip to next iteration
```
