/**
 * ============================================================================
 * PHASE 3: CONDITIONALS & SWITCH
 * ============================================================================
 */

// ============================================================================
// SECTION 1: IF/ELSE STATEMENTS
// ============================================================================

/**
 * WHY: Make decisions in code based on conditions
 * WHAT: Execute different code blocks based on boolean expressions
 * HOW: if (condition) { } else if (condition) { } else { }
 */

// Basic if/else
const age = 18;
if (age >= 18) {
    console.log("Adult");
} else {
    console.log("Minor");
}

// Multiple conditions with else if
const score = 85;
let grade;

if (score >= 90) {
    grade = "A";
} else if (score >= 80) {
    grade = "B";
} else if (score >= 70) {
    grade = "C";
} else if (score >= 60) {
    grade = "D";
} else {
    grade = "F";
}

/**
 * WHY ORDER MATTERS:
 * Conditions are checked top to bottom
 * First match executes, rest are skipped
 */

// BAD: Wrong order
const num = 95;
if (num >= 60) {
    console.log("Pass"); // This runs, even though 95 >= 90
} else if (num >= 90) {
    console.log("Excellent"); // Never reached!
}

// GOOD: Most specific first
if (num >= 90) {
    console.log("Excellent"); // Correct!
} else if (num >= 60) {
    console.log("Pass");
}

// ============================================================================
// SECTION 2: GUARD CLAUSES
// ============================================================================

/**
 * WHY: Reduce nesting, improve readability
 * WHAT: Handle edge cases early with early returns
 * HOW: Check invalid cases first, return early
 */

// BAD: Deep nesting
function processUserBad(user) {
    if (user) {
        if (user.isActive) {
            if (user.hasPermission) {
                // Finally, the actual logic
                return `Processing ${user.name}`;
            } else {
                return "No permission";
            }
        } else {
            return "User inactive";
        }
    } else {
        return "No user";
    }
}

// GOOD: Guard clauses
function processUserGood(user) {
    // Guard clauses - early returns
    if (!user) return "No user";
    if (!user.isActive) return "User inactive";
    if (!user.hasPermission) return "No permission";

    // Main logic - not nested!
    return `Processing ${user.name}`;
}

/**
 * WHY GUARD CLAUSES ARE BETTER:
 * 1. Less nesting = easier to read
 * 2. Handle errors upfront
 * 3. Main logic is at normal indentation
 * 4. Each condition is independent
 */

// ============================================================================
// SECTION 3: SWITCH STATEMENTS
// ============================================================================

/**
 * WHY: Cleaner than many else-if chains for discrete values
 * WHAT: Match expression against multiple cases
 * HOW: Uses strict equality (===) for matching
 */

const day = "Monday";
let dayType;

switch (day) {
    case "Monday":
    case "Tuesday":
    case "Wednesday":
    case "Thursday":
    case "Friday":
        dayType = "Weekday";
        break;
    case "Saturday":
    case "Sunday":
        dayType = "Weekend";
        break;
    default:
        dayType = "Invalid day";
}

/**
 * IMPORTANT: BREAK STATEMENT
 * Without break, execution "falls through" to next case
 * This can be intentional (like grouping Mon-Fri above)
 * Or a bug if forgotten
 */

// Fall-through bug example
const fruit = "apple";
switch (fruit) {
    case "apple":
        console.log("Apple selected");
        // Missing break! Falls through
    case "banana":
        console.log("Banana selected");
        break;
}
// Output: "Apple selected" AND "Banana selected"!

/**
 * WHY NOT SWITCH (sometimes):
 * - Only works with strict equality
 * - Can't use complex conditions
 * - Object lookup might be cleaner
 */

// Alternative: Object lookup
const dayTypeMap = {
    Monday: "Weekday",
    Tuesday: "Weekday",
    Wednesday: "Weekday",
    Thursday: "Weekday",
    Friday: "Weekday",
    Saturday: "Weekend",
    Sunday: "Weekend"
};

const dayResult = dayTypeMap[day] || "Invalid day";

// ============================================================================
// SECTION 4: TRUTHY/FALSY IN CONDITIONS
// ============================================================================

/**
 * Conditions don't need explicit booleans
 * Any value is coerced to boolean
 */

const userName = "";

// BAD: Unnecessary comparison
if (userName === "" || userName === null || userName === undefined) {
    console.log("No name");
}

// GOOD: Use falsy check
if (!userName) {
    console.log("No name");
}

// BUT BE CAREFUL with 0 and ""
const count = 0;
if (!count) {
    console.log("No count"); // Runs even though 0 might be valid!
}

// Better for numbers
if (count === undefined || count === null) {
    console.log("Count not set");
}
// Or use nullish check
if (count == null) { // Matches null and undefined only
    console.log("Count not set");
}

// ============================================================================
// SECTION 5: COMMON PATTERNS
// ============================================================================

// Pattern 1: Default values
function greet(name) {
    if (!name) {
        name = "Guest";
    }
    return `Hello, ${name}`;
}
// Modern: Use default parameters
function greetModern(name = "Guest") {
    return `Hello, ${name}`;
}

// Pattern 2: Toggle boolean
let isActive = false;
isActive = !isActive; // true

// Pattern 3: Ensure boolean
const value = "something";
const boolValue = !!value; // true (double negation)

// Pattern 4: Conditional property
const user = {
    name: "Alice",
    ...(true && { admin: true }) // Conditionally add property
};

// Pattern 5: Multiple conditions
const isValid =
    user.name &&
    user.email &&
    user.age >= 18;

// ============================================================================
// SECTION 6: INTERVIEW QUESTIONS
// ============================================================================

/**
 * Q1: Rewrite using ternary
 */
let result;
const x = 10;
if (x > 5) {
    result = "big";
} else {
    result = "small";
}
// Answer:
const result2 = x > 5 ? "big" : "small";

/**
 * Q2: What's wrong with this code?
 */
function checkValue(val) {
    if (val = 10) { // BUG: Assignment, not comparison!
        return "Is ten";
    }
    return "Not ten";
}
// Always returns "Is ten" because assignment returns the value (10, which is truthy)
// Fix: Use === for comparison

/**
 * Q3: Simplify this code
 */
function isAdult(age) {
    if (age >= 18) {
        return true;
    } else {
        return false;
    }
}
// Answer:
function isAdultSimple(age) {
    return age >= 18;
}

/**
 * Q4: Why doesn't this work?
 */
const num2 = 5;
switch (num2) {
    case num2 > 3:  // This compares 5 === true, not 5 > 3
        console.log("Greater than 3");
        break;
}
// Fix: Switch needs literal values, not expressions
// Use if/else for complex conditions
