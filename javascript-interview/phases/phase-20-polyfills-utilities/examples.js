/**
 * ============================================================================
 * PHASE 20: POLYFILLS - PROMISE & UTILITY FUNCTIONS
 * ============================================================================
 */

// ============================================================================
// SECTION 1: Promise.all
// ============================================================================

Promise.myAll = function(promises) {
    return new Promise((resolve, reject) => {
        // Handle non-array input
        if (!Array.isArray(promises)) {
            return reject(new TypeError('Argument must be an array'));
        }

        const results = [];
        let completed = 0;

        // Handle empty array
        if (promises.length === 0) {
            return resolve(results);
        }

        promises.forEach((promise, index) => {
            // Wrap non-promises in Promise.resolve
            Promise.resolve(promise)
                .then(value => {
                    results[index] = value;  // Maintain order
                    completed++;

                    if (completed === promises.length) {
                        resolve(results);
                    }
                })
                .catch(reject);  // Reject immediately on first error
        });
    });
};

// Test
Promise.myAll([
    Promise.resolve(1),
    Promise.resolve(2),
    Promise.resolve(3)
]).then(console.log); // [1, 2, 3]

// ============================================================================
// SECTION 2: Promise.race
// ============================================================================

Promise.myRace = function(promises) {
    return new Promise((resolve, reject) => {
        if (!Array.isArray(promises)) {
            return reject(new TypeError('Argument must be an array'));
        }

        promises.forEach(promise => {
            // First to settle wins
            Promise.resolve(promise)
                .then(resolve)
                .catch(reject);
        });
    });
};

// ============================================================================
// SECTION 3: Promise.allSettled
// ============================================================================

Promise.myAllSettled = function(promises) {
    return new Promise((resolve) => {
        if (!Array.isArray(promises)) {
            return resolve([]);
        }

        const results = [];
        let completed = 0;

        if (promises.length === 0) {
            return resolve(results);
        }

        promises.forEach((promise, index) => {
            Promise.resolve(promise)
                .then(value => {
                    results[index] = { status: 'fulfilled', value };
                })
                .catch(reason => {
                    results[index] = { status: 'rejected', reason };
                })
                .finally(() => {
                    completed++;
                    if (completed === promises.length) {
                        resolve(results);
                    }
                });
        });
    });
};

// ============================================================================
// SECTION 4: Promise.any
// ============================================================================

Promise.myAny = function(promises) {
    return new Promise((resolve, reject) => {
        if (!Array.isArray(promises)) {
            return reject(new TypeError('Argument must be an array'));
        }

        const errors = [];
        let rejected = 0;

        if (promises.length === 0) {
            return reject(new AggregateError([], 'All promises were rejected'));
        }

        promises.forEach((promise, index) => {
            Promise.resolve(promise)
                .then(resolve)  // First to fulfill wins
                .catch(error => {
                    errors[index] = error;
                    rejected++;

                    if (rejected === promises.length) {
                        reject(new AggregateError(errors, 'All promises were rejected'));
                    }
                });
        });
    });
};

// ============================================================================
// SECTION 5: DEEP CLONE
// ============================================================================

function deepClone(obj, hash = new WeakMap()) {
    // Handle primitives and null
    if (obj === null || typeof obj !== 'object') {
        return obj;
    }

    // Handle circular references
    if (hash.has(obj)) {
        return hash.get(obj);
    }

    // Handle Date
    if (obj instanceof Date) {
        return new Date(obj.getTime());
    }

    // Handle RegExp
    if (obj instanceof RegExp) {
        return new RegExp(obj.source, obj.flags);
    }

    // Handle Map
    if (obj instanceof Map) {
        const clonedMap = new Map();
        hash.set(obj, clonedMap);
        obj.forEach((value, key) => {
            clonedMap.set(deepClone(key, hash), deepClone(value, hash));
        });
        return clonedMap;
    }

    // Handle Set
    if (obj instanceof Set) {
        const clonedSet = new Set();
        hash.set(obj, clonedSet);
        obj.forEach(value => {
            clonedSet.add(deepClone(value, hash));
        });
        return clonedSet;
    }

    // Handle Array
    if (Array.isArray(obj)) {
        const clonedArr = [];
        hash.set(obj, clonedArr);
        obj.forEach((item, index) => {
            clonedArr[index] = deepClone(item, hash);
        });
        return clonedArr;
    }

    // Handle Object
    const clonedObj = Object.create(Object.getPrototypeOf(obj));
    hash.set(obj, clonedObj);

    // Include Symbol keys
    const allKeys = [
        ...Object.keys(obj),
        ...Object.getOwnPropertySymbols(obj)
    ];

    allKeys.forEach(key => {
        clonedObj[key] = deepClone(obj[key], hash);
    });

    return clonedObj;
}

// Test
const original = {
    date: new Date(),
    nested: { a: 1 },
    array: [1, 2, { b: 3 }]
};
const cloned = deepClone(original);

// ============================================================================
// SECTION 6: DEEP EQUAL
// ============================================================================

function deepEqual(a, b) {
    // Same reference or primitive equal
    if (a === b) return true;

    // Handle null/undefined
    if (a == null || b == null) return a === b;

    // Different types
    if (typeof a !== typeof b) return false;

    // Non-objects are not equal (already checked ===)
    if (typeof a !== 'object') return false;

    // Handle Date
    if (a instanceof Date && b instanceof Date) {
        return a.getTime() === b.getTime();
    }

    // Handle RegExp
    if (a instanceof RegExp && b instanceof RegExp) {
        return a.toString() === b.toString();
    }

    // Handle Array
    if (Array.isArray(a) !== Array.isArray(b)) return false;

    // Compare keys
    const keysA = Object.keys(a);
    const keysB = Object.keys(b);

    if (keysA.length !== keysB.length) return false;

    // Compare each key
    return keysA.every(key =>
        keysB.includes(key) && deepEqual(a[key], b[key])
    );
}

// Test
console.log(deepEqual({ a: 1 }, { a: 1 })); // true
console.log(deepEqual({ a: 1 }, { a: 2 })); // false

// ============================================================================
// SECTION 7: EVENT EMITTER
// ============================================================================

class EventEmitter {
    constructor() {
        this.events = {};
    }

    on(event, listener) {
        if (!this.events[event]) {
            this.events[event] = [];
        }
        this.events[event].push(listener);
        return this;
    }

    off(event, listener) {
        if (!this.events[event]) return this;

        this.events[event] = this.events[event].filter(l => l !== listener);
        return this;
    }

    emit(event, ...args) {
        if (!this.events[event]) return false;

        this.events[event].forEach(listener => {
            listener.apply(this, args);
        });
        return true;
    }

    once(event, listener) {
        const wrapper = (...args) => {
            listener.apply(this, args);
            this.off(event, wrapper);
        };
        return this.on(event, wrapper);
    }
}

// Test
const emitter = new EventEmitter();
emitter.on('greet', name => console.log(`Hello, ${name}!`));
emitter.emit('greet', 'Alice'); // "Hello, Alice!"

// ============================================================================
// SECTION 8: RETRY WITH BACKOFF
// ============================================================================

async function retry(fn, options = {}) {
    const {
        retries = 3,
        delay = 1000,
        backoff = 2,
        onRetry = () => {}
    } = options;

    let lastError;

    for (let attempt = 0; attempt <= retries; attempt++) {
        try {
            return await fn();
        } catch (error) {
            lastError = error;

            if (attempt < retries) {
                const waitTime = delay * Math.pow(backoff, attempt);
                onRetry(error, attempt + 1, waitTime);
                await new Promise(resolve => setTimeout(resolve, waitTime));
            }
        }
    }

    throw lastError;
}

// ============================================================================
// SECTION 9: FLATTEN OBJECT
// ============================================================================

function flattenObject(obj, prefix = '') {
    return Object.keys(obj).reduce((acc, key) => {
        const pre = prefix ? `${prefix}.` : '';

        if (typeof obj[key] === 'object' &&
            obj[key] !== null &&
            !Array.isArray(obj[key])) {
            Object.assign(acc, flattenObject(obj[key], pre + key));
        } else {
            acc[pre + key] = obj[key];
        }

        return acc;
    }, {});
}

// Test
const nested = { a: { b: { c: 1 } }, d: 2 };
console.log(flattenObject(nested));
// { "a.b.c": 1, "d": 2 }

// ============================================================================
// SECTION 10: ASYNC UTILITIES
// ============================================================================

// Timeout wrapper
function withTimeout(promise, ms) {
    const timeout = new Promise((_, reject) =>
        setTimeout(() => reject(new Error('Timeout')), ms)
    );
    return Promise.race([promise, timeout]);
}

// Delay/sleep
const delay = ms => new Promise(resolve => setTimeout(resolve, ms));

// Sequential promise execution
async function sequential(fns) {
    const results = [];
    for (const fn of fns) {
        results.push(await fn());
    }
    return results;
}

// Parallel with limit
async function parallelLimit(fns, limit) {
    const results = [];
    const executing = [];

    for (const fn of fns) {
        const p = fn().then(result => {
            executing.splice(executing.indexOf(p), 1);
            return result;
        });

        results.push(p);
        executing.push(p);

        if (executing.length >= limit) {
            await Promise.race(executing);
        }
    }

    return Promise.all(results);
}

// ============================================================================
// INTERVIEW SUMMARY
// ============================================================================

/**
 * Most important polyfills to know:
 *
 * 1. Array: map, filter, reduce, flat
 * 2. Function: bind, call, apply
 * 3. Promise: all, race, allSettled
 * 4. Utilities: debounce, throttle, deepClone
 *
 * Key patterns:
 * - Error handling
 * - Edge cases (null, empty, types)
 * - Performance considerations
 * - Memory management
 */
