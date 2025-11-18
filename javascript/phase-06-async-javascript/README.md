# Phase 6: Asynchronous JavaScript

## 📋 Table of Contents
1. [Synchronous vs Asynchronous](#synchronous-vs-asynchronous)
2. [Callbacks](#callbacks)
3. [Promises](#promises)
4. [Async/Await](#async-await)
5. [Error Handling](#error-handling)
6. [Event Loop](#event-loop)

---

## Synchronous vs Asynchronous

### Synchronous (Blocking)

```
┌────────────────────────────────────────┐
│      SYNCHRONOUS                       │
│                                        │
│  Task 1 ──► Complete ──┐               │
│                        ▼               │
│  Task 2 ──► Complete ──┐               │
│                        ▼               │
│  Task 3 ──► Complete ──┐               │
│                        ▼               │
│  Done                                  │
│                                        │
│  Each task waits for previous to finish│
└────────────────────────────────────────┘
```

```javascript
console.log("Start");
console.log("Middle");
console.log("End");

// Output: Start, Middle, End
```

### Asynchronous (Non-Blocking)

```
┌────────────────────────────────────────┐
│      ASYNCHRONOUS                      │
│                                        │
│  Task 1 ──► Start ────────┐            │
│  Task 2 ──► Start ─────┐  │            │
│  Task 3 ──► Complete   │  │            │
│                  ▼     │  │            │
│  Task 2 Complete ◄─────┘  │            │
│                  ▼        │            │
│  Task 1 Complete ◄────────┘            │
│                  ▼                     │
│  Done                                  │
│                                        │
│  Tasks can run concurrently            │
└────────────────────────────────────────┘
```

```javascript
console.log("Start");

setTimeout(() => {
    console.log("Async task");
}, 0);

console.log("End");

// Output: Start, End, Async task
```

---

## Callbacks

### The Why
To handle operations that take time (network requests, file reading, timers).

### The What
A callback is a function passed as an argument to be executed later.

```javascript
// Simple callback
function greet(name, callback) {
    console.log("Hello, " + name);
    callback();
}

greet("Alice", function() {
    console.log("Callback executed!");
});

// Async callback
setTimeout(() => {
    console.log("This runs after 2 seconds");
}, 2000);
```

### Callback Hell (Pyramid of Doom)

```javascript
// ❌ Hard to read and maintain
getData(function(a) {
    getMoreData(a, function(b) {
        getMoreData(b, function(c) {
            getMoreData(c, function(d) {
                getMoreData(d, function(e) {
                    console.log(e);
                });
            });
        });
    });
});
```

**Diagram:**
```
┌────────────────────────────────────────┐
│       CALLBACK HELL                    │
│                                        │
│  getData(fn() {                        │
│      ┌───────────────┐                │
│      │ getMore(fn() {│                │
│      │    ┌──────────┼─┐             │
│      │    │getMore() │ │             │
│      │    │   ┌──────┼─┼─┐          │
│      │    │   │      │ │ │          │
│      └────┼───┼──────┘ │ │          │
│           └───┼────────┘ │          │
│               └──────────┘          │
│                                        │
│  Hard to read, debug, and maintain     │
└────────────────────────────────────────┘
```

---

## Promises

### The Why
To avoid callback hell and make async code more readable.

### The What
A Promise represents a value that may be available now, later, or never.

**States:**
```
┌────────────────────────────────────────┐
│         PROMISE STATES                 │
│                                        │
│           ┌──────────┐                │
│           │ PENDING  │                │
│           └─────┬────┘                │
│                 │                     │
│        ┌────────┴────────┐           │
│        ▼                 ▼           │
│   ┌─────────┐      ┌──────────┐     │
│   │FULFILLED│      │ REJECTED │     │
│   └────┬────┘      └────┬─────┘     │
│        │                │           │
│        ▼                ▼           │
│    .then()          .catch()        │
└────────────────────────────────────────┘
```

### Creating Promises

```javascript
// Basic promise
const promise = new Promise((resolve, reject) => {
    // Async operation
    const success = true;

    if (success) {
        resolve("Operation successful!");
    } else {
        reject("Operation failed!");
    }
});

// Using the promise
promise
    .then(result => {
        console.log(result);  // "Operation successful!"
    })
    .catch(error => {
        console.log(error);
    });
```

### Promise Example: Async Operation

```javascript
function fetchUser(id) {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            if (id > 0) {
                resolve({ id, name: "Alice" });
            } else {
                reject("Invalid ID");
            }
        }, 1000);
    });
}

// Using the promise
fetchUser(1)
    .then(user => {
        console.log("User:", user);
        return user.id;
    })
    .then(id => {
        console.log("User ID:", id);
    })
    .catch(error => {
        console.log("Error:", error);
    })
    .finally(() => {
        console.log("Operation complete");
    });
```

### Promise Chaining

```javascript
fetchUser(1)
    .then(user => {
        console.log("Step 1:", user);
        return fetchPosts(user.id);
    })
    .then(posts => {
        console.log("Step 2:", posts);
        return fetchComments(posts[0].id);
    })
    .then(comments => {
        console.log("Step 3:", comments);
    })
    .catch(error => {
        console.log("Error at any step:", error);
    });
```

**Diagram:**
```
┌────────────────────────────────────────┐
│      PROMISE CHAINING                  │
│                                        │
│  Promise                               │
│    ▼                                   │
│  .then(value => {                      │
│      return newPromise;                │
│  })                                    │
│    ▼                                   │
│  .then(value => {                      │
│      return anotherPromise;            │
│  })                                    │
│    ▼                                   │
│  .then(value => {                      │
│      return result;                    │
│  })                                    │
│    ▼                                   │
│  .catch(error => {                     │
│      // Catches any error above        │
│  })                                    │
└────────────────────────────────────────┘
```

### Promise Combinators

```javascript
// Promise.all - Wait for all
const promise1 = Promise.resolve(1);
const promise2 = Promise.resolve(2);
const promise3 = Promise.resolve(3);

Promise.all([promise1, promise2, promise3])
    .then(results => {
        console.log(results);  // [1, 2, 3]
    });

// Promise.race - First to complete
Promise.race([promise1, promise2, promise3])
    .then(result => {
        console.log(result);  // 1 (fastest)
    });

// Promise.allSettled - Wait for all, get all results
Promise.allSettled([promise1, promise2, Promise.reject("error")])
    .then(results => {
        console.log(results);
        // [
        //   { status: 'fulfilled', value: 1 },
        //   { status: 'fulfilled', value: 2 },
        //   { status: 'rejected', reason: 'error' }
        // ]
    });

// Promise.any - First to fulfill
Promise.any([promise1, promise2, promise3])
    .then(result => {
        console.log(result);  // 1
    });
```

---

## Async/Await

### The Why
To write asynchronous code that looks synchronous.

### The What
`async`/`await` is syntactic sugar over Promises.

```javascript
// With Promises
function getUser() {
    return fetchUser(1)
        .then(user => {
            return fetchPosts(user.id);
        })
        .then(posts => {
            return posts;
        })
        .catch(error => {
            console.log(error);
        });
}

// ✅ With async/await (cleaner!)
async function getUserAsync() {
    try {
        const user = await fetchUser(1);
        const posts = await fetchPosts(user.id);
        return posts;
    } catch (error) {
        console.log(error);
    }
}
```

### Rules for Async/Await

```javascript
// 1. async function always returns a Promise
async function example() {
    return "Hello";
}

example().then(result => console.log(result));  // "Hello"

// 2. await only works inside async functions
async function getData() {
    const data = await fetchData();  // ✓ Works
}

function getData2() {
    const data = await fetchData();  // ❌ Error!
}

// 3. await pauses execution until Promise resolves
async function demo() {
    console.log("Before await");
    const result = await somePromise;
    console.log("After await");  // Waits for promise
}
```

### Multiple Async Operations

```javascript
// ❌ Sequential (slow) - 6 seconds total
async function sequential() {
    const user = await fetchUser(1);      // 2 seconds
    const posts = await fetchPosts(1);    // 2 seconds
    const comments = await fetchComments(1); // 2 seconds

    return { user, posts, comments };
}

// ✅ Parallel (fast) - 2 seconds total
async function parallel() {
    const [user, posts, comments] = await Promise.all([
        fetchUser(1),
        fetchPosts(1),
        fetchComments(1)
    ]);

    return { user, posts, comments };
}
```

**Diagram:**
```
┌────────────────────────────────────────┐
│      SEQUENTIAL vs PARALLEL            │
│                                        │
│  Sequential:                           │
│  ──Task1──┐                            │
│           └──Task2──┐                  │
│                     └──Task3──         │
│  Total: t1 + t2 + t3                   │
│                                        │
│  Parallel:                             │
│  ──Task1──                             │
│  ──Task2──                             │
│  ──Task3──                             │
│  Total: max(t1, t2, t3)                │
└────────────────────────────────────────┘
```

---

## Error Handling

### With Promises

```javascript
fetchUser(1)
    .then(user => {
        return fetchPosts(user.id);
    })
    .catch(error => {
        console.log("Error:", error);
        // Can return fallback value
        return { posts: [] };
    })
    .then(posts => {
        // Continues even after error
        console.log(posts);
    });
```

### With Async/Await

```javascript
async function getData() {
    try {
        const user = await fetchUser(1);
        const posts = await fetchPosts(user.id);
        return posts;
    } catch (error) {
        console.log("Error:", error);
        return { posts: [] };  // Fallback
    } finally {
        console.log("Cleanup");  // Always runs
    }
}
```

### Handling Multiple Errors

```javascript
async function multipleOperations() {
    try {
        const user = await fetchUser(1);

        try {
            const posts = await fetchPosts(user.id);
            return posts;
        } catch (postError) {
            console.log("Post error:", postError);
            return [];
        }
    } catch (userError) {
        console.log("User error:", userError);
        return null;
    }
}
```

---

## Event Loop

### The Why
To understand how JavaScript handles async operations.

### The What
JavaScript is single-threaded but can handle async operations through the event loop.

**Diagram:**
```
┌─────────────────────────────────────────────────┐
│         JAVASCRIPT EVENT LOOP                   │
│                                                 │
│  ┌──────────────┐                              │
│  │  Call Stack  │  ◄─── Executes code          │
│  └──────┬───────┘                              │
│         │                                       │
│         │                                       │
│  ┌──────▼────────────────────────────────────┐ │
│  │         Web APIs                          │ │
│  │  (setTimeout, fetch, DOM events, etc.)    │ │
│  └──────┬────────────────────────────────────┘ │
│         │                                       │
│         │ When complete                         │
│         ▼                                       │
│  ┌─────────────────┐                           │
│  │  Callback Queue │                           │
│  └──────┬──────────┘                           │
│         │                                       │
│         │ Event Loop checks if stack is empty  │
│         └──────────────────────────────────────►│
│                                                 │
└─────────────────────────────────────────────────┘
```

### Example:

```javascript
console.log("1");

setTimeout(() => {
    console.log("2");
}, 0);

Promise.resolve().then(() => {
    console.log("3");
});

console.log("4");

// Output: 1, 4, 3, 2
```

**Execution Flow:**
```
1. Execute: console.log("1")           → Output: 1
2. setTimeout → Send to Web APIs
3. Promise → Send to Microtask Queue
4. Execute: console.log("4")           → Output: 4
5. Call stack empty → Check Microtask Queue
6. Execute: Promise callback           → Output: 3
7. Microtask queue empty → Check Callback Queue
8. Execute: setTimeout callback        → Output: 2
```

---

## Coding Examples

### Example 1: Retry Logic (Medium)

```javascript
async function retry(fn, maxAttempts = 3, delay = 1000) {
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
        try {
            return await fn();
        } catch (error) {
            if (attempt === maxAttempts) {
                throw error;
            }
            console.log(`Attempt ${attempt} failed, retrying...`);
            await new Promise(resolve => setTimeout(resolve, delay));
        }
    }
}

// Usage
retry(() => fetchData())
    .then(data => console.log(data))
    .catch(error => console.log("All attempts failed:", error));
```

---

### Example 2: Promise Timeout (Medium)

```javascript
function withTimeout(promise, ms) {
    const timeout = new Promise((_, reject) => {
        setTimeout(() => reject(new Error("Timeout")), ms);
    });

    return Promise.race([promise, timeout]);
}

// Usage
withTimeout(fetchData(), 5000)
    .then(data => console.log(data))
    .catch(error => console.log("Error:", error.message));
```

---

### Example 3: Sequential Processing (Hard)

```javascript
async function processSequentially(items, asyncFn) {
    const results = [];

    for (const item of items) {
        const result = await asyncFn(item);
        results.push(result);
    }

    return results;
}

// Usage
const urls = ["url1", "url2", "url3"];
processSequentially(urls, fetch)
    .then(responses => console.log(responses));
```

---

## Key Takeaways

✅ Callbacks → Promises → Async/Await (evolution)
✅ Async/await makes async code look synchronous
✅ Always use try/catch with async/await
✅ Use Promise.all for parallel operations
✅ Understand the event loop and task queues
✅ Handle errors at appropriate levels

---

## Next Steps

🎉 **Congratulations!** You've mastered asynchronous JavaScript!

**Ready for more?** Move on to [Phase 7: DOM Manipulation](../phase-07-dom-manipulation/README.md)
