/**
 * ============================================================================
 * PHASE 10: ARRAY ITERATION METHODS
 * ============================================================================
 *
 * INTERVIEW IMPORTANCE: Very High
 * These methods are asked in almost every JavaScript interview
 */

// ============================================================================
// SECTION 1: forEach vs map
// ============================================================================

const numbers = [1, 2, 3, 4, 5];

/**
 * forEach: Iterate for side effects, returns undefined
 * map: Transform elements, returns new array
 */

// forEach - just do something with each element
numbers.forEach((num, index, array) => {
    console.log(`Index ${index}: ${num}`);
});
// Returns: undefined

// map - transform each element
const doubled = numbers.map((num, index, array) => {
    return num * 2;
});
console.log(doubled); // [2, 4, 6, 8, 10]

/**
 * WHY USE MAP OVER forEach:
 * - Returns new array (functional, no mutation)
 * - Can be chained with other methods
 * - More declarative intent
 *
 * WHEN TO USE forEach:
 * - Only for side effects (logging, API calls)
 * - When you don't need return value
 */

// COMMON MISTAKE: Using map without return
const mistake = numbers.map(num => {
    num * 2;  // No return! Returns undefined
});
console.log(mistake); // [undefined, undefined, undefined, undefined, undefined]

// ============================================================================
// SECTION 2: filter
// ============================================================================

/**
 * filter: Keep elements that pass test
 * Returns new array with matching elements
 */

const mixed = [1, -2, 3, -4, 5, -6];

// Get positive numbers
const positives = mixed.filter(num => num > 0);
console.log(positives); // [1, 3, 5]

// Get even numbers
const evens = mixed.filter(num => num % 2 === 0);
console.log(evens); // [-2, -4, -6]

// Filter objects
const users = [
    { name: "Alice", age: 25, active: true },
    { name: "Bob", age: 30, active: false },
    { name: "Charlie", age: 35, active: true }
];

const activeUsers = users.filter(user => user.active);
const adults = users.filter(user => user.age >= 30);

// ============================================================================
// SECTION 3: reduce
// ============================================================================

/**
 * reduce: Accumulate array to single value
 * Most powerful but also most complex
 *
 * Syntax: array.reduce((accumulator, current, index, array) => {}, initialValue)
 */

// Sum array
const sum = numbers.reduce((acc, num) => acc + num, 0);
console.log(sum); // 15

// How it works:
// acc=0, num=1 => 0+1 = 1
// acc=1, num=2 => 1+2 = 3
// acc=3, num=3 => 3+3 = 6
// acc=6, num=4 => 6+4 = 10
// acc=10, num=5 => 10+5 = 15

// Find max
const max = numbers.reduce((acc, num) => num > acc ? num : acc, -Infinity);
console.log(max); // 5

// Count occurrences
const fruits = ["apple", "banana", "apple", "orange", "banana", "apple"];
const count = fruits.reduce((acc, fruit) => {
    acc[fruit] = (acc[fruit] || 0) + 1;
    return acc;
}, {});
console.log(count); // { apple: 3, banana: 2, orange: 1 }

// Group by property
const grouped = users.reduce((acc, user) => {
    const key = user.active ? "active" : "inactive";
    if (!acc[key]) acc[key] = [];
    acc[key].push(user);
    return acc;
}, {});

// Flatten array (before flat() existed)
const nested = [[1, 2], [3, 4], [5]];
const flat = nested.reduce((acc, arr) => acc.concat(arr), []);
console.log(flat); // [1, 2, 3, 4, 5]

/**
 * IMPORTANT: Always provide initial value!
 * Without it, first element becomes initial value
 * This can cause bugs with empty arrays
 */
// [].reduce((a, b) => a + b); // TypeError!
[].reduce((a, b) => a + b, 0); // 0 (safe)

// ============================================================================
// SECTION 4: find and findIndex
// ============================================================================

/**
 * find: Return first element that passes test
 * findIndex: Return index of first element that passes test
 */

const numbers2 = [1, 5, 10, 15, 20];

// Find first number > 10
const found = numbers2.find(num => num > 10);
console.log(found); // 15 (not [15, 20])

// Find index
const foundIndex = numbers2.findIndex(num => num > 10);
console.log(foundIndex); // 3

// Not found returns undefined/-1
const notFound = numbers2.find(num => num > 100);
console.log(notFound); // undefined

const notFoundIndex = numbers2.findIndex(num => num > 100);
console.log(notFoundIndex); // -1

// Find in objects
const user = users.find(u => u.name === "Bob");
console.log(user); // { name: "Bob", age: 30, active: false }

// ============================================================================
// SECTION 5: some and every
// ============================================================================

/**
 * some: Does at least one element pass?
 * every: Do all elements pass?
 */

const nums = [1, 2, 3, 4, 5];

// some - at least one
console.log(nums.some(n => n > 4)); // true (5 > 4)
console.log(nums.some(n => n > 10)); // false

// every - all elements
console.log(nums.every(n => n > 0)); // true (all positive)
console.log(nums.every(n => n > 3)); // false (1, 2, 3 fail)

// Practical use: Validation
const formFields = [
    { name: "email", valid: true },
    { name: "password", valid: true },
    { name: "phone", valid: false }
];

const isFormValid = formFields.every(field => field.valid);
const hasAnyError = formFields.some(field => !field.valid);

// Short-circuit behavior
// some stops at first true
// every stops at first false

// ============================================================================
// SECTION 6: flat and flatMap
// ============================================================================

/**
 * flat: Flatten nested arrays
 * flatMap: Map then flatten (one level)
 */

// flat
const nested2 = [1, [2, 3], [4, [5, 6]]];
console.log(nested2.flat());    // [1, 2, 3, 4, [5, 6]] (depth 1)
console.log(nested2.flat(2));   // [1, 2, 3, 4, 5, 6] (depth 2)
console.log(nested2.flat(Infinity)); // Flatten completely

// flatMap
const sentences = ["Hello World", "Foo Bar"];
const words = sentences.flatMap(s => s.split(" "));
console.log(words); // ["Hello", "World", "Foo", "Bar"]

// Without flatMap
const wordsOld = sentences.map(s => s.split(" ")).flat();

// ============================================================================
// SECTION 7: METHOD CHAINING
// ============================================================================

/**
 * Chain methods for complex transformations
 * Order matters!
 */

const products = [
    { name: "Laptop", price: 1000, category: "Electronics" },
    { name: "Phone", price: 500, category: "Electronics" },
    { name: "Shirt", price: 50, category: "Clothing" },
    { name: "Pants", price: 80, category: "Clothing" }
];

// Get total price of electronics over $600
const expensiveElectronicsTotal = products
    .filter(p => p.category === "Electronics")  // Filter electronics
    .filter(p => p.price > 600)                  // Filter expensive
    .map(p => p.price)                           // Get prices
    .reduce((sum, price) => sum + price, 0);     // Sum them

console.log(expensiveElectronicsTotal); // 1000

// Get names of affordable items (< 100)
const affordableNames = products
    .filter(p => p.price < 100)
    .map(p => p.name)
    .sort();

console.log(affordableNames); // ["Pants", "Shirt"]

// ============================================================================
// SECTION 8: POLYFILL IMPLEMENTATIONS
// ============================================================================

/**
 * Understanding internals helps in interviews
 */

// map polyfill
Array.prototype.myMap = function(callback, thisArg) {
    if (typeof callback !== 'function') {
        throw new TypeError(callback + ' is not a function');
    }

    const result = [];
    for (let i = 0; i < this.length; i++) {
        if (i in this) {  // Handle sparse arrays
            result[i] = callback.call(thisArg, this[i], i, this);
        }
    }
    return result;
};

// filter polyfill
Array.prototype.myFilter = function(callback, thisArg) {
    const result = [];
    for (let i = 0; i < this.length; i++) {
        if (i in this && callback.call(thisArg, this[i], i, this)) {
            result.push(this[i]);
        }
    }
    return result;
};

// reduce polyfill
Array.prototype.myReduce = function(callback, initialValue) {
    if (this.length === 0 && initialValue === undefined) {
        throw new TypeError('Reduce of empty array with no initial value');
    }

    let accumulator = initialValue !== undefined ? initialValue : this[0];
    let startIndex = initialValue !== undefined ? 0 : 1;

    for (let i = startIndex; i < this.length; i++) {
        if (i in this) {
            accumulator = callback(accumulator, this[i], i, this);
        }
    }
    return accumulator;
};

// ============================================================================
// SECTION 9: INTERVIEW QUESTIONS
// ============================================================================

/**
 * Q1: Implement Array.flat() without using built-in flat
 */
function flatten(arr, depth = 1) {
    if (depth === 0) return arr.slice();

    return arr.reduce((acc, val) => {
        if (Array.isArray(val)) {
            acc.push(...flatten(val, depth - 1));
        } else {
            acc.push(val);
        }
        return acc;
    }, []);
}

/**
 * Q2: Remove duplicates from array
 */
// Using Set
const unique1 = arr => [...new Set(arr)];

// Using filter
const unique2 = arr => arr.filter((item, index) => arr.indexOf(item) === index);

// Using reduce
const unique3 = arr => arr.reduce((acc, item) => {
    if (!acc.includes(item)) acc.push(item);
    return acc;
}, []);

/**
 * Q3: Find most frequent element
 */
function mostFrequent(arr) {
    const counts = arr.reduce((acc, item) => {
        acc[item] = (acc[item] || 0) + 1;
        return acc;
    }, {});

    return Object.entries(counts)
        .reduce((max, [item, count]) =>
            count > max[1] ? [item, count] : max
        )[0];
}

/**
 * Q4: Chunk array into groups of n
 */
function chunk(arr, size) {
    return arr.reduce((acc, _, i) => {
        if (i % size === 0) {
            acc.push(arr.slice(i, i + size));
        }
        return acc;
    }, []);
}

console.log(chunk([1, 2, 3, 4, 5], 2)); // [[1, 2], [3, 4], [5]]

/**
 * Q5: Group array of objects by property
 */
function groupBy(arr, key) {
    return arr.reduce((acc, obj) => {
        const groupKey = obj[key];
        if (!acc[groupKey]) acc[groupKey] = [];
        acc[groupKey].push(obj);
        return acc;
    }, {});
}

/**
 * Q6: What's the output?
 */
console.log([1, 2, 3].map(parseInt));
// [1, NaN, NaN]
// Because: parseInt(1, 0), parseInt(2, 1), parseInt(3, 2)
// map passes (value, index, array) but parseInt expects (string, radix)

// Fix:
console.log([1, 2, 3].map(num => parseInt(num)));
// or
console.log([1, 2, 3].map(Number));
