# Phase 4: Arrays and Objects Deep Dive

## 📋 Table of Contents
1. [Arrays Introduction](#arrays-introduction)
2. [Array Methods](#array-methods)
3. [Objects Introduction](#objects-introduction)
4. [Object Methods](#object-methods)
5. [Destructuring](#destructuring)
6. [Spread and Rest](#spread-and-rest)
7. [Coding Examples](#coding-examples)
8. [Common Mistakes](#common-mistakes)

---

## Arrays Introduction

### The Why
Store and manage collections of data (lists, sequences, groups of similar items).

### The What
An array is an ordered collection that can hold multiple values.

### The How

```javascript
// Creating arrays
const empty = [];
const numbers = [1, 2, 3, 4, 5];
const mixed = [1, "hello", true, null, {name: "Alice"}];
const fruits = ["apple", "banana", "orange"];

// Accessing elements (0-indexed)
console.log(fruits[0]);  // "apple"
console.log(fruits[2]);  // "orange"

// Array length
console.log(fruits.length);  // 3

// Modifying elements
fruits[1] = "grape";
console.log(fruits);  // ["apple", "grape", "orange"]
```

**Diagram: Array Structure**
```
┌─────────────────────────────────────────────────┐
│             Array: ["apple", "banana", "orange"]│
│                                                 │
│   Index:      0         1          2            │
│            ┌───────┬────────┬────────┐         │
│   Value:   │"apple"│"banana"│"orange"│         │
│            └───────┴────────┴────────┘         │
│                                                 │
│   Length: 3                                     │
│   First: fruits[0] = "apple"                    │
│   Last:  fruits[fruits.length - 1] = "orange"   │
└─────────────────────────────────────────────────┘
```

---

## Array Methods

### 1. Adding/Removing Elements

```javascript
const arr = [1, 2, 3];

// Add to end
arr.push(4);
console.log(arr);  // [1, 2, 3, 4]

// Remove from end
const last = arr.pop();
console.log(last);  // 4
console.log(arr);   // [1, 2, 3]

// Add to beginning
arr.unshift(0);
console.log(arr);  // [0, 1, 2, 3]

// Remove from beginning
const first = arr.shift();
console.log(first);  // 0
console.log(arr);    // [1, 2, 3]
```

**Diagram: Push/Pop vs Shift/Unshift**
```
┌──────────────────────────────────────────┐
│         PUSH/POP (End operations)        │
│                                          │
│  [1, 2, 3]                               │
│            ← pop()    3                  │
│            push(4) → [1, 2, 3, 4]        │
│                                          │
│         Fast: O(1)                       │
└──────────────────────────────────────────┘

┌──────────────────────────────────────────┐
│    UNSHIFT/SHIFT (Beginning operations)  │
│                                          │
│       [1, 2, 3]                          │
│  0 ← unshift(0)                          │
│       [0, 1, 2, 3]                       │
│  shift() → 0                             │
│       [1, 2, 3]                          │
│                                          │
│         Slow: O(n) - Must shift all      │
└──────────────────────────────────────────┘
```

---

### 2. Splice (Add/Remove at Any Position)

```javascript
const arr = [1, 2, 3, 4, 5];

// Remove elements: splice(start, deleteCount)
arr.splice(2, 1);  // Remove 1 element at index 2
console.log(arr);  // [1, 2, 4, 5]

// Add elements: splice(start, deleteCount, items...)
arr.splice(2, 0, 3);  // Add 3 at index 2, remove 0
console.log(arr);     // [1, 2, 3, 4, 5]

// Replace elements
arr.splice(1, 2, 10, 20);  // Remove 2, add 10, 20 at index 1
console.log(arr);          // [1, 10, 20, 4, 5]
```

---

### 3. Slice (Extract Portion)

```javascript
const arr = [1, 2, 3, 4, 5];

// slice(start, end) - end not included
const sliced = arr.slice(1, 4);
console.log(sliced);  // [2, 3, 4]
console.log(arr);     // [1, 2, 3, 4, 5] (original unchanged)

// Copy entire array
const copy = arr.slice();
```

---

### 4. Searching

```javascript
const fruits = ["apple", "banana", "orange", "banana"];

// indexOf - first occurrence
console.log(fruits.indexOf("banana"));  // 1

// lastIndexOf - last occurrence
console.log(fruits.lastIndexOf("banana"));  // 3

// includes - check existence
console.log(fruits.includes("apple"));   // true
console.log(fruits.includes("grape"));   // false

// find - first element that matches condition
const numbers = [5, 12, 8, 130, 44];
const found = numbers.find(num => num > 10);
console.log(found);  // 12

// findIndex - index of first match
const index = numbers.findIndex(num => num > 10);
console.log(index);  // 1
```

---

### 5. Transformation Methods (Important!)

#### map() - Transform Each Element

```javascript
// Create new array by applying function to each element
const numbers = [1, 2, 3, 4, 5];

const doubled = numbers.map(num => num * 2);
console.log(doubled);  // [2, 4, 6, 8, 10]

const squared = numbers.map(num => num ** 2);
console.log(squared);  // [1, 4, 9, 16, 25]
```

**Diagram:**
```
┌──────────────────────────────────────────┐
│              MAP                         │
│                                          │
│  [1, 2, 3, 4, 5]                         │
│   │  │  │  │  │                          │
│   ▼  ▼  ▼  ▼  ▼  (multiply by 2)        │
│   2  4  6  8  10                         │
│                                          │
│  [2, 4, 6, 8, 10]                        │
│                                          │
│  Same length, transformed values         │
└──────────────────────────────────────────┘
```

---

#### filter() - Keep Elements That Pass Test

```javascript
const numbers = [1, 2, 3, 4, 5, 6];

const evens = numbers.filter(num => num % 2 === 0);
console.log(evens);  // [2, 4, 6]

const greaterThan3 = numbers.filter(num => num > 3);
console.log(greaterThan3);  // [4, 5, 6]
```

**Diagram:**
```
┌──────────────────────────────────────────┐
│             FILTER                       │
│                                          │
│  [1, 2, 3, 4, 5, 6]                      │
│   ✗  ✓  ✗  ✓  ✗  ✓  (even numbers)      │
│      │     │     │                       │
│      ▼     ▼     ▼                       │
│  [2, 4, 6]                               │
│                                          │
│  Different length, filtered values       │
└──────────────────────────────────────────┘
```

---

#### reduce() - Reduce to Single Value

```javascript
const numbers = [1, 2, 3, 4, 5];

// Sum all numbers
const sum = numbers.reduce((accumulator, current) => {
    return accumulator + current;
}, 0);  // 0 is initial value
console.log(sum);  // 15

// Product of all numbers
const product = numbers.reduce((acc, curr) => acc * curr, 1);
console.log(product);  // 120

// Find maximum
const max = numbers.reduce((max, curr) => curr > max ? curr : max);
console.log(max);  // 5
```

**Diagram:**
```
┌──────────────────────────────────────────┐
│             REDUCE (Sum)                 │
│                                          │
│  Initial: accumulator = 0                │
│                                          │
│  [1, 2, 3, 4, 5]                         │
│   │                                      │
│   └─► acc=0 + 1 = 1                      │
│        │                                 │
│        └─► acc=1 + 2 = 3                 │
│             │                            │
│             └─► acc=3 + 3 = 6            │
│                  │                       │
│                  └─► acc=6 + 4 = 10      │
│                       │                  │
│                       └─► acc=10 + 5 = 15│
│                                          │
│  Result: 15                              │
└──────────────────────────────────────────┘
```

---

### 6. Other Useful Methods

```javascript
const arr = [1, 2, 3];

// forEach - Execute function for each element
arr.forEach((num, index) => {
    console.log(`Index ${index}: ${num}`);
});

// join - Convert to string
const joined = arr.join(", ");
console.log(joined);  // "1, 2, 3"

// concat - Combine arrays
const arr2 = [4, 5];
const combined = arr.concat(arr2);
console.log(combined);  // [1, 2, 3, 4, 5]

// reverse - Reverse in place (mutates!)
const reversed = [1, 2, 3].reverse();
console.log(reversed);  // [3, 2, 1]

// sort - Sort in place (mutates!)
const nums = [3, 1, 4, 1, 5];
nums.sort((a, b) => a - b);  // Ascending
console.log(nums);  // [1, 1, 3, 4, 5]

// every - Check if all pass test
const allPositive = [1, 2, 3].every(num => num > 0);
console.log(allPositive);  // true

// some - Check if at least one passes test
const hasEven = [1, 3, 5, 6].some(num => num % 2 === 0);
console.log(hasEven);  // true
```

---

## Objects Introduction

### The Why
Store related data together with meaningful property names.

### The What
An object is an unordered collection of key-value pairs.

```javascript
// Creating objects
const person = {
    name: "Alice",
    age: 25,
    city: "New York",
    isStudent: true,
    hobbies: ["reading", "coding"],
    greet: function() {
        console.log("Hello!");
    }
};

// Accessing properties
console.log(person.name);       // "Alice" (dot notation)
console.log(person["age"]);     // 25 (bracket notation)

// Adding properties
person.email = "alice@example.com";

// Modifying properties
person.age = 26;

// Deleting properties
delete person.isStudent;

// Calling methods
person.greet();  // "Hello!"
```

**Diagram: Object Structure**
```
┌────────────────────────────────────────────┐
│         Object: person                     │
│                                            │
│   ┌────────────────────────────────┐      │
│   │ name: "Alice"                  │      │
│   ├────────────────────────────────┤      │
│   │ age: 25                        │      │
│   ├────────────────────────────────┤      │
│   │ city: "New York"               │      │
│   ├────────────────────────────────┤      │
│   │ hobbies: ["reading", "coding"] │      │
│   ├────────────────────────────────┤      │
│   │ greet: function() {...}        │      │
│   └────────────────────────────────┘      │
│                                            │
│   Access: person.name or person["name"]    │
└────────────────────────────────────────────┘
```

---

## Object Methods

```javascript
const person = {
    name: "Alice",
    age: 25,
    city: "New York"
};

// Object.keys() - Get all keys
const keys = Object.keys(person);
console.log(keys);  // ["name", "age", "city"]

// Object.values() - Get all values
const values = Object.values(person);
console.log(values);  // ["Alice", 25, "New York"]

// Object.entries() - Get key-value pairs
const entries = Object.entries(person);
console.log(entries);
// [["name", "Alice"], ["age", 25], ["city", "New York"]]

// Object.assign() - Copy/merge objects
const copy = Object.assign({}, person);
const merged = Object.assign({}, person, {email: "alice@ex.com"});

// hasOwnProperty - Check if property exists
console.log(person.hasOwnProperty("name"));  // true
console.log(person.hasOwnProperty("email")); // false
```

---

## Destructuring

### Array Destructuring

```javascript
// Extract values from arrays
const colors = ["red", "green", "blue"];

// Old way
const first = colors[0];
const second = colors[1];

// ✅ Modern way
const [first, second, third] = colors;
console.log(first);   // "red"
console.log(second);  // "green"
console.log(third);   // "blue"

// Skip elements
const [, , third] = colors;
console.log(third);  // "blue"

// Rest pattern
const numbers = [1, 2, 3, 4, 5];
const [one, two, ...rest] = numbers;
console.log(one);   // 1
console.log(two);   // 2
console.log(rest);  // [3, 4, 5]

// Default values
const [a, b, c = 3] = [1, 2];
console.log(c);  // 3

// Swap variables
let x = 1, y = 2;
[x, y] = [y, x];
console.log(x, y);  // 2 1
```

---

### Object Destructuring

```javascript
const person = {
    name: "Alice",
    age: 25,
    city: "New York"
};

// Old way
const name = person.name;
const age = person.age;

// ✅ Modern way
const { name, age, city } = person;
console.log(name);  // "Alice"
console.log(age);   // 25

// Rename variables
const { name: fullName, age: years } = person;
console.log(fullName);  // "Alice"
console.log(years);     // 25

// Default values
const { email = "N/A" } = person;
console.log(email);  // "N/A"

// Nested destructuring
const user = {
    id: 1,
    info: {
        name: "Bob",
        address: {
            city: "Boston"
        }
    }
};

const { info: { name, address: { city } } } = user;
console.log(name);  // "Bob"
console.log(city);  // "Boston"
```

**Diagram:**
```
┌────────────────────────────────────────────┐
│         DESTRUCTURING                      │
│                                            │
│  Object: { name: "Alice", age: 25 }        │
│                                            │
│  const { name, age } = person;             │
│           ↓     ↓                          │
│         name = "Alice"                     │
│         age = 25                           │
│                                            │
│  Like unpacking a box into separate items  │
└────────────────────────────────────────────┘
```

---

## Spread and Rest Operators

### Spread Operator (...)

```javascript
// Spread arrays
const arr1 = [1, 2, 3];
const arr2 = [4, 5, 6];

const combined = [...arr1, ...arr2];
console.log(combined);  // [1, 2, 3, 4, 5, 6]

// Copy array
const copy = [...arr1];

// Spread objects
const obj1 = { a: 1, b: 2 };
const obj2 = { c: 3, d: 4 };

const merged = { ...obj1, ...obj2 };
console.log(merged);  // { a: 1, b: 2, c: 3, d: 4 }

// Override properties
const person = { name: "Alice", age: 25 };
const updated = { ...person, age: 26 };
console.log(updated);  // { name: "Alice", age: 26 }

// Function arguments
function sum(a, b, c) {
    return a + b + c;
}
const numbers = [1, 2, 3];
console.log(sum(...numbers));  // 6
```

---

### Rest Operator (...)

```javascript
// Collect remaining arguments
function sum(...numbers) {
    return numbers.reduce((acc, curr) => acc + curr, 0);
}

console.log(sum(1, 2, 3));        // 6
console.log(sum(1, 2, 3, 4, 5));  // 15

// With other parameters
function greet(greeting, ...names) {
    return `${greeting} ${names.join(", ")}!`;
}

console.log(greet("Hello", "Alice", "Bob", "Charlie"));
// "Hello Alice, Bob, Charlie!"
```

**Diagram: Spread vs Rest**
```
┌────────────────────────────────────────────┐
│          SPREAD (Expand)                   │
│                                            │
│  const arr = [1, 2, 3];                    │
│  console.log(...arr);                      │
│               ↓                            │
│  console.log(1, 2, 3);                     │
│                                            │
│  Expands array into individual elements    │
└────────────────────────────────────────────┘

┌────────────────────────────────────────────┐
│          REST (Collect)                    │
│                                            │
│  function sum(...numbers) {}               │
│  sum(1, 2, 3, 4);                          │
│       ↓                                    │
│  numbers = [1, 2, 3, 4]                    │
│                                            │
│  Collects arguments into array             │
└────────────────────────────────────────────┘
```

---

## Coding Examples

### Example 1: Array Statistics (Easy)

```javascript
function getArrayStats(arr) {
    if (arr.length === 0) {
        return { min: null, max: null, avg: null };
    }

    const min = Math.min(...arr);
    const max = Math.max(...arr);
    const sum = arr.reduce((acc, curr) => acc + curr, 0);
    const avg = sum / arr.length;

    return { min, max, avg };
}

console.log(getArrayStats([1, 2, 3, 4, 5]));
// { min: 1, max: 5, avg: 3 }
```

---

### Example 2: Remove Duplicates (Medium)

```javascript
// Method 1: Using Set
function removeDuplicates1(arr) {
    return [...new Set(arr)];
}

// Method 2: Using filter
function removeDuplicates2(arr) {
    return arr.filter((item, index) => arr.indexOf(item) === index);
}

// Method 3: Using reduce
function removeDuplicates3(arr) {
    return arr.reduce((unique, item) => {
        return unique.includes(item) ? unique : [...unique, item];
    }, []);
}

const nums = [1, 2, 2, 3, 4, 4, 5];
console.log(removeDuplicates1(nums));  // [1, 2, 3, 4, 5]
```

---

### Example 3: Group By Property (Hard)

```javascript
function groupBy(arr, key) {
    return arr.reduce((grouped, item) => {
        const keyValue = item[key];
        if (!grouped[keyValue]) {
            grouped[keyValue] = [];
        }
        grouped[keyValue].push(item);
        return grouped;
    }, {});
}

const students = [
    { name: "Alice", grade: "A" },
    { name: "Bob", grade: "B" },
    { name: "Charlie", grade: "A" },
    { name: "David", grade: "C" },
    { name: "Eve", grade: "B" }
];

console.log(groupBy(students, "grade"));
/*
{
    A: [{ name: "Alice", grade: "A" }, { name: "Charlie", grade: "A" }],
    B: [{ name: "Bob", grade: "B" }, { name: "Eve", grade: "B" }],
    C: [{ name: "David", grade: "C" }]
}
*/
```

---

### Example 4: Deep Clone (Hard)

```javascript
// Shallow clone - only top level
const original = { a: 1, b: { c: 2 } };
const shallow = { ...original };
shallow.b.c = 3;
console.log(original.b.c);  // 3 (modified original!)

// Deep clone - all levels
function deepClone(obj) {
    if (obj === null || typeof obj !== "object") {
        return obj;
    }

    if (Array.isArray(obj)) {
        return obj.map(item => deepClone(item));
    }

    const cloned = {};
    for (const key in obj) {
        if (obj.hasOwnProperty(key)) {
            cloned[key] = deepClone(obj[key]);
        }
    }
    return cloned;
}

// Modern way (limited support)
const deep = structuredClone(original);

// JSON method (loses functions, undefined, etc.)
const jsonClone = JSON.parse(JSON.stringify(original));
```

---

## Common Mistakes

### Mistake 1: Mutating vs Non-Mutating

```javascript
const arr = [1, 2, 3];

// ❌ Mutating methods (change original)
arr.push(4);      // arr is now [1, 2, 3, 4]
arr.pop();        // arr is now [1, 2, 3]
arr.reverse();    // arr is now [3, 2, 1]
arr.sort();       // arr is modified

// ✅ Non-mutating methods (return new)
const mapped = arr.map(x => x * 2);  // arr unchanged
const filtered = arr.filter(x => x > 1);  // arr unchanged
const sliced = arr.slice(0, 2);  // arr unchanged
```

---

### Mistake 2: Array.sort() Default Behavior

```javascript
// ❌ Wrong - sorts as strings!
const numbers = [1, 5, 10, 20, 3];
numbers.sort();
console.log(numbers);  // [1, 10, 20, 3, 5] (Wrong!)

// ✅ Correct - provide compare function
numbers.sort((a, b) => a - b);  // Ascending
console.log(numbers);  // [1, 3, 5, 10, 20]

numbers.sort((a, b) => b - a);  // Descending
console.log(numbers);  // [20, 10, 5, 3, 1]
```

---

### Mistake 3: Reference vs Value

```javascript
// ❌ Copying reference, not value
const arr1 = [1, 2, 3];
const arr2 = arr1;  // Both point to same array!
arr2.push(4);
console.log(arr1);  // [1, 2, 3, 4] (modified!)

// ✅ Create actual copy
const arr3 = [...arr1];  // or arr1.slice()
arr3.push(5);
console.log(arr1);  // [1, 2, 3, 4] (unchanged)
```

---

## Key Takeaways

✅ Use map() to transform, filter() to select, reduce() to aggregate
✅ Remember: some methods mutate, others don't
✅ Use destructuring for cleaner code
✅ Spread operator (...) is powerful for copying and merging
✅ Objects are passed by reference, not value
✅ Always provide compare function to sort()

---

## Next Steps

🎉 **Congratulations!** You've mastered arrays and objects!

**Ready for more?** Move on to [Phase 5: Advanced Concepts](../phase-05-advanced-concepts/README.md)
