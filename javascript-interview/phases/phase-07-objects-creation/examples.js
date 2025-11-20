/**
 * ============================================================================
 * PHASE 7: OBJECTS CREATION & PROPERTIES
 * ============================================================================
 */

// ============================================================================
// SECTION 1: CREATING OBJECTS
// ============================================================================

// Method 1: Object Literal (most common)
const user1 = {
    name: "Alice",
    age: 25,
    greet() {
        return `Hello, I'm ${this.name}`;
    }
};

// Method 2: Constructor Function
function User(name, age) {
    this.name = name;
    this.age = age;
    this.greet = function() {
        return `Hello, I'm ${this.name}`;
    };
}
const user2 = new User("Bob", 30);

/**
 * WHAT 'new' DOES:
 * 1. Creates empty object {}
 * 2. Sets prototype to Constructor.prototype
 * 3. Executes constructor with 'this' = new object
 * 4. Returns object (unless constructor returns object)
 */

// Method 3: Object.create()
const personProto = {
    greet() {
        return `Hello, I'm ${this.name}`;
    }
};
const user3 = Object.create(personProto);
user3.name = "Charlie";
user3.age = 35;

/**
 * WHY Object.create():
 * - Direct control over prototype
 * - Can create object with no prototype: Object.create(null)
 * - Cleaner inheritance than constructor functions
 */

// Method 4: ES6 Class
class Person {
    constructor(name, age) {
        this.name = name;
        this.age = age;
    }

    greet() {
        return `Hello, I'm ${this.name}`;
    }
}
const user4 = new Person("Diana", 28);

// ============================================================================
// SECTION 2: PROPERTY ACCESS
// ============================================================================

const obj = {
    name: "Alice",
    "multi word": "value",
    123: "numeric key"
};

// Dot notation (preferred for valid identifiers)
console.log(obj.name); // "Alice"

// Bracket notation (required for special keys)
console.log(obj["multi word"]); // "value"
console.log(obj[123]);          // "numeric key"

// Dynamic property access
const key = "name";
console.log(obj[key]); // "Alice"

// Adding properties
obj.email = "alice@example.com";
obj["phone"] = "123-456";

// Deleting properties
delete obj.phone;

// Checking property existence
console.log("name" in obj);           // true (checks prototype too)
console.log(obj.hasOwnProperty("name")); // true (own property only)

// ============================================================================
// SECTION 3: COMPUTED PROPERTY NAMES (ES6)
// ============================================================================

const propName = "dynamicProp";
const suffix = "Name";

const dynamicObj = {
    [propName]: "value",              // dynamicProp: "value"
    ["user" + suffix]: "Alice",       // userName: "Alice"
    [`computed_${1 + 1}`]: "two"      // computed_2: "two"
};

// Useful for creating objects from variables
function createPerson(key, value) {
    return {
        [key]: value
    };
}

// ============================================================================
// SECTION 4: SHORTHAND SYNTAX (ES6)
// ============================================================================

const name = "Alice";
const age = 25;

// Old way
const oldUser = {
    name: name,
    age: age,
    greet: function() {
        return "Hello";
    }
};

// ES6 shorthand
const newUser = {
    name,           // Property shorthand
    age,
    greet() {       // Method shorthand
        return "Hello";
    }
};

// ============================================================================
// SECTION 5: PROPERTY DESCRIPTORS
// ============================================================================

/**
 * Every property has a descriptor with attributes:
 * - value: The property value
 * - writable: Can value be changed?
 * - enumerable: Shows in for...in?
 * - configurable: Can be deleted/modified?
 */

const person = { name: "Alice" };

// Get descriptor
const descriptor = Object.getOwnPropertyDescriptor(person, "name");
console.log(descriptor);
// { value: "Alice", writable: true, enumerable: true, configurable: true }

// Define property with custom descriptor
Object.defineProperty(person, "id", {
    value: 12345,
    writable: false,      // Cannot change
    enumerable: false,    // Won't show in for...in
    configurable: false   // Cannot delete or modify descriptor
});

person.id = 99999;        // Fails silently (or throws in strict mode)
console.log(person.id);   // 12345

// Define multiple properties
Object.defineProperties(person, {
    firstName: { value: "Alice", writable: true },
    lastName: { value: "Smith", writable: true }
});

// ============================================================================
// SECTION 6: GETTERS AND SETTERS
// ============================================================================

/**
 * WHY: Computed properties, validation, encapsulation
 * WHAT: Functions that look like property access
 */

const circle = {
    radius: 5,

    // Getter
    get diameter() {
        return this.radius * 2;
    },

    // Setter
    set diameter(value) {
        this.radius = value / 2;
    },

    // Computed property
    get area() {
        return Math.PI * this.radius ** 2;
    }
};

console.log(circle.diameter); // 10 (calls getter)
circle.diameter = 20;         // Calls setter
console.log(circle.radius);   // 10

// Validation with setter
const user = {
    _age: 0,

    get age() {
        return this._age;
    },

    set age(value) {
        if (value < 0) {
            throw new Error("Age cannot be negative");
        }
        this._age = value;
    }
};

user.age = 25;  // OK
// user.age = -5; // Error!

// ============================================================================
// SECTION 7: OBJECT METHODS
// ============================================================================

const sampleObj = { a: 1, b: 2, c: 3 };

// Object.keys() - array of own enumerable property names
console.log(Object.keys(sampleObj)); // ["a", "b", "c"]

// Object.values() - array of own enumerable property values
console.log(Object.values(sampleObj)); // [1, 2, 3]

// Object.entries() - array of [key, value] pairs
console.log(Object.entries(sampleObj)); // [["a", 1], ["b", 2], ["c", 3]]

// Object.fromEntries() - create object from entries
const entries = [["x", 1], ["y", 2]];
const fromEntries = Object.fromEntries(entries);
console.log(fromEntries); // { x: 1, y: 2 }

// Object.assign() - copy/merge objects
const target = { a: 1 };
const source = { b: 2 };
const merged = Object.assign(target, source);
console.log(merged); // { a: 1, b: 2 }
// NOTE: target is modified!

// Better: Use spread for immutable merge
const merged2 = { ...target, ...source };

// ============================================================================
// SECTION 8: OBJECT IMMUTABILITY
// ============================================================================

// Object.freeze() - cannot add, delete, or modify
const frozen = Object.freeze({ name: "Alice" });
frozen.name = "Bob";      // Fails silently
frozen.age = 25;          // Fails silently
delete frozen.name;       // Fails silently
console.log(frozen);      // { name: "Alice" }

// Object.seal() - cannot add or delete, CAN modify
const sealed = Object.seal({ name: "Alice" });
sealed.name = "Bob";      // Works!
sealed.age = 25;          // Fails silently
delete sealed.name;       // Fails silently
console.log(sealed);      // { name: "Bob" }

// Object.preventExtensions() - cannot add, CAN delete and modify
const noExtend = Object.preventExtensions({ name: "Alice" });
noExtend.name = "Bob";    // Works!
noExtend.age = 25;        // Fails silently
delete noExtend.name;     // Works!

// Check state
console.log(Object.isFrozen(frozen));      // true
console.log(Object.isSealed(sealed));      // true
console.log(Object.isExtensible(noExtend)); // false

/**
 * IMPORTANT: These are SHALLOW!
 * Nested objects are not frozen
 */
const deepObj = Object.freeze({
    nested: { value: 1 }
});
deepObj.nested.value = 2; // Works! Nested is not frozen
console.log(deepObj.nested.value); // 2

// Deep freeze (recursive)
function deepFreeze(obj) {
    Object.keys(obj).forEach(key => {
        if (typeof obj[key] === 'object' && obj[key] !== null) {
            deepFreeze(obj[key]);
        }
    });
    return Object.freeze(obj);
}

// ============================================================================
// SECTION 9: INTERVIEW QUESTIONS
// ============================================================================

/**
 * Q1: How to check if object is empty?
 */
function isEmpty(obj) {
    return Object.keys(obj).length === 0;
}

/**
 * Q2: Deep clone an object
 */
// Simple (loses functions, undefined)
const clone1 = JSON.parse(JSON.stringify(obj));

// Modern (loses functions)
const clone2 = structuredClone(obj);

// Manual (handles more cases)
function deepClone(obj) {
    if (obj === null || typeof obj !== 'object') return obj;
    if (obj instanceof Date) return new Date(obj);
    if (obj instanceof Array) return obj.map(deepClone);

    const cloned = {};
    for (const key of Object.keys(obj)) {
        cloned[key] = deepClone(obj[key]);
    }
    return cloned;
}

/**
 * Q3: Merge objects deeply
 */
function deepMerge(target, source) {
    for (const key of Object.keys(source)) {
        if (source[key] instanceof Object && key in target) {
            Object.assign(source[key], deepMerge(target[key], source[key]));
        }
    }
    return { ...target, ...source };
}

/**
 * Q4: What's the output?
 */
const a = { x: 1 };
const b = { x: 1 };
console.log(a === b); // false (different references)
console.log(a.x === b.x); // true (same primitive value)
