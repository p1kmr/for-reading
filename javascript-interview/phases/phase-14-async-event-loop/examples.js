/**
 * ============================================================================
 * PHASE 14: ASYNC/AWAIT & EVENT LOOP
 * ============================================================================
 *
 * INTERVIEW IMPORTANCE: Critical
 * Event loop and async questions appear in 90%+ of JS interviews
 */

// ============================================================================
// SECTION 1: EVENT LOOP BASICS
// ============================================================================

/**
 * EVENT LOOP ORDER:
 * 1. Execute all synchronous code (Call Stack)
 * 2. Execute ALL microtasks (Promise callbacks, queueMicrotask)
 * 3. Execute ONE macrotask (setTimeout, setInterval, I/O)
 * 4. Repeat from step 2
 */

console.log("1 - Start");  // Sync

setTimeout(() => {
    console.log("2 - setTimeout");  // Macrotask
}, 0);

Promise.resolve().then(() => {
    console.log("3 - Promise");  // Microtask
});

console.log("4 - End");  // Sync

// Output: 1, 4, 3, 2
// WHY: Sync first, then microtasks, then macrotasks

// More complex example
console.log("A");

setTimeout(() => console.log("B"), 0);

Promise.resolve()
    .then(() => console.log("C"))
    .then(() => console.log("D"));

Promise.resolve().then(() => {
    console.log("E");
    setTimeout(() => console.log("F"), 0);
});

console.log("G");

// Output: A, G, C, E, D, B, F
// Explanation:
// Sync: A, G
// Microtasks: C, E (C's then queues D), D
// Macrotasks: B, F

// ============================================================================
// SECTION 2: PROMISES
// ============================================================================

/**
 * Promise: Object representing eventual completion/failure
 * States: pending -> fulfilled/rejected (settled)
 */

// Creating a promise
const myPromise = new Promise((resolve, reject) => {
    const success = true;

    if (success) {
        resolve("Operation succeeded");  // Fulfill
    } else {
        reject(new Error("Operation failed"));  // Reject
    }
});

// Consuming promises
myPromise
    .then(result => {
        console.log(result);  // "Operation succeeded"
        return "Next value";  // Return for chaining
    })
    .then(nextResult => {
        console.log(nextResult);  // "Next value"
    })
    .catch(error => {
        console.error(error);  // Handle rejection
    })
    .finally(() => {
        console.log("Cleanup");  // Always runs
    });

// Promisifying callback-based functions
function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

delay(1000).then(() => console.log("1 second passed"));

// ============================================================================
// SECTION 3: PROMISE STATIC METHODS
// ============================================================================

const p1 = Promise.resolve(1);
const p2 = Promise.resolve(2);
const p3 = Promise.resolve(3);
const pFail = Promise.reject("Error");

// Promise.all - Wait for all, fail if any fails
Promise.all([p1, p2, p3])
    .then(results => console.log(results))  // [1, 2, 3]
    .catch(error => console.error(error));

// With one rejection
Promise.all([p1, pFail, p3])
    .then(results => console.log(results))
    .catch(error => console.error(error));  // "Error"

// Promise.allSettled - Wait for all, get all results
Promise.allSettled([p1, pFail, p3])
    .then(results => console.log(results));
// [
//   { status: "fulfilled", value: 1 },
//   { status: "rejected", reason: "Error" },
//   { status: "fulfilled", value: 3 }
// ]

// Promise.race - First to settle wins
const slow = new Promise(resolve => setTimeout(() => resolve("slow"), 200));
const fast = new Promise(resolve => setTimeout(() => resolve("fast"), 100));

Promise.race([slow, fast])
    .then(winner => console.log(winner));  // "fast"

// Promise.any - First to fulfill wins (ignores rejections)
Promise.any([pFail, p1, p2])
    .then(first => console.log(first));  // 1

// ============================================================================
// SECTION 4: ASYNC/AWAIT
// ============================================================================

/**
 * async/await: Syntactic sugar over promises
 * Makes async code look synchronous
 *
 * async function always returns a Promise
 * await pauses execution until Promise settles
 */

// Basic async/await
async function fetchUserData() {
    const response = await fetch('/api/user');
    const data = await response.json();
    return data;
}

// Equivalent with promises
function fetchUserDataPromise() {
    return fetch('/api/user')
        .then(response => response.json());
}

// Error handling with try/catch
async function fetchWithError() {
    try {
        const response = await fetch('/api/data');
        if (!response.ok) {
            throw new Error(`HTTP error: ${response.status}`);
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Fetch failed:", error);
        throw error;  // Re-throw if needed
    } finally {
        console.log("Cleanup");
    }
}

// ============================================================================
// SECTION 5: PARALLEL VS SEQUENTIAL EXECUTION
// ============================================================================

// Sequential - each awaits previous (SLOW)
async function sequential() {
    const user = await fetchUser();       // Wait
    const posts = await fetchPosts();     // Wait
    const comments = await fetchComments(); // Wait
    return { user, posts, comments };
}
// Total time: user + posts + comments

// Parallel - all at once (FAST)
async function parallel() {
    const [user, posts, comments] = await Promise.all([
        fetchUser(),
        fetchPosts(),
        fetchComments()
    ]);
    return { user, posts, comments };
}
// Total time: max(user, posts, comments)

// Parallel with error handling
async function parallelSafe() {
    const results = await Promise.allSettled([
        fetchUser(),
        fetchPosts(),
        fetchComments()
    ]);

    return results.map(r =>
        r.status === 'fulfilled' ? r.value : null
    );
}

// ============================================================================
// SECTION 6: COMMON PATTERNS
// ============================================================================

// Pattern 1: Retry mechanism
async function fetchWithRetry(url, retries = 3) {
    for (let i = 0; i < retries; i++) {
        try {
            return await fetch(url);
        } catch (error) {
            if (i === retries - 1) throw error;
            await delay(1000 * (i + 1));  // Exponential backoff
        }
    }
}

// Pattern 2: Timeout wrapper
function withTimeout(promise, ms) {
    const timeout = new Promise((_, reject) =>
        setTimeout(() => reject(new Error('Timeout')), ms)
    );
    return Promise.race([promise, timeout]);
}

// Pattern 3: Sequential processing of array
async function processSequentially(items) {
    const results = [];
    for (const item of items) {
        const result = await processItem(item);
        results.push(result);
    }
    return results;
}

// Pattern 4: Parallel with concurrency limit
async function parallelLimit(items, limit, fn) {
    const results = [];
    const executing = [];

    for (const item of items) {
        const p = fn(item).then(result => {
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
// SECTION 7: COMMON MISTAKES
// ============================================================================

// Mistake 1: Forgetting await
async function mistake1() {
    const data = fetch('/api');  // Missing await!
    console.log(data);  // Promise, not data
}

// Mistake 2: await in forEach (doesn't work as expected)
async function mistake2(items) {
    items.forEach(async (item) => {
        await processItem(item);  // These run in parallel!
    });
    console.log("Done");  // Runs before items processed
}

// Fix: Use for...of
async function fixed2(items) {
    for (const item of items) {
        await processItem(item);
    }
    console.log("Done");  // Runs after all items
}

// Mistake 3: Not handling errors
async function mistake3() {
    const data = await fetch('/api');  // Unhandled rejection!
}

// Fix: Always handle errors
async function fixed3() {
    try {
        const data = await fetch('/api');
    } catch (error) {
        console.error(error);
    }
}

// ============================================================================
// SECTION 8: INTERVIEW QUESTIONS
// ============================================================================

/**
 * Q1: What's the output?
 */
async function test() {
    console.log("1");
    await Promise.resolve();
    console.log("2");
}

console.log("3");
test();
console.log("4");

// Output: 3, 1, 4, 2
// Why: 3 (sync), 1 (sync in async), 4 (sync),
//      2 (microtask after await)

/**
 * Q2: What's the output?
 */
const promise = new Promise((resolve, reject) => {
    console.log(1);
    resolve();
    console.log(2);
});

promise.then(() => {
    console.log(3);
});

console.log(4);

// Output: 1, 2, 4, 3
// Why: Promise executor is sync, then callback is microtask

/**
 * Q3: Implement Promise.all()
 */
function promiseAll(promises) {
    return new Promise((resolve, reject) => {
        if (!Array.isArray(promises)) {
            return reject(new TypeError('Argument must be array'));
        }

        const results = [];
        let completed = 0;

        if (promises.length === 0) {
            return resolve(results);
        }

        promises.forEach((promise, index) => {
            Promise.resolve(promise)
                .then(value => {
                    results[index] = value;
                    completed++;
                    if (completed === promises.length) {
                        resolve(results);
                    }
                })
                .catch(reject);
        });
    });
}

/**
 * Q4: What's the output?
 */
console.log('start');

setTimeout(() => console.log('timeout1'), 0);
setTimeout(() => {
    console.log('timeout2');
    Promise.resolve().then(() => console.log('promise inside timeout'));
}, 0);

Promise.resolve()
    .then(() => {
        console.log('promise1');
        setTimeout(() => console.log('timeout inside promise'), 0);
    })
    .then(() => console.log('promise2'));

console.log('end');

// Output:
// start, end (sync)
// promise1, promise2 (microtasks)
// timeout1, timeout2, promise inside timeout (macrotasks + microtask)
// timeout inside promise (macrotask)

/**
 * Q5: Implement a sleep function
 */
const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

async function demo() {
    console.log('Start');
    await sleep(1000);
    console.log('After 1 second');
}

/**
 * Q6: Convert callback to promise
 */
function promisify(fn) {
    return function(...args) {
        return new Promise((resolve, reject) => {
            fn(...args, (error, result) => {
                if (error) reject(error);
                else resolve(result);
            });
        });
    };
}

// Usage
const readFileAsync = promisify(fs.readFile);
