/**
 * ============================================================================
 * PHASE 19: POLYFILLS - FUNCTION & OBJECT METHODS
 * ============================================================================
 *
 * These are among the most frequently asked polyfills in interviews
 */

// ============================================================================
// SECTION 1: Function.prototype.call
// ============================================================================

/**
 * NATIVE BEHAVIOR:
 * - Calls function with given this value and arguments
 * - Arguments passed individually
 * - Returns function's return value
 */

Function.prototype.myCall = function(context, ...args) {
    // Handle null/undefined context
    context = context ?? globalThis;

    // Ensure context is an object
    context = Object(context);

    // Create unique property to avoid collision
    const fnSymbol = Symbol('fn');

    // Attach function to context
    context[fnSymbol] = this;

    // Call function with arguments
    const result = context[fnSymbol](...args);

    // Clean up
    delete context[fnSymbol];

    return result;
};

// Test
function greet(greeting, punctuation) {
    return `${greeting}, ${this.name}${punctuation}`;
}

const person = { name: 'Alice' };
console.log(greet.myCall(person, 'Hello', '!')); // "Hello, Alice!"

// ============================================================================
// SECTION 2: Function.prototype.apply
// ============================================================================

/**
 * NATIVE BEHAVIOR:
 * - Same as call but arguments as array
 */

Function.prototype.myApply = function(context, argsArray) {
    context = context ?? globalThis;
    context = Object(context);

    // Handle missing or invalid argsArray
    if (argsArray == null) {
        argsArray = [];
    }
    if (!Array.isArray(argsArray) && typeof argsArray !== 'object') {
        throw new TypeError('CreateListFromArrayLike called on non-object');
    }

    const fnSymbol = Symbol('fn');
    context[fnSymbol] = this;

    const result = context[fnSymbol](...argsArray);

    delete context[fnSymbol];

    return result;
};

// Test
console.log(greet.myApply(person, ['Hi', '?'])); // "Hi, Alice?"
console.log(Math.max.myApply(null, [1, 5, 3])); // 5

// ============================================================================
// SECTION 3: Function.prototype.bind
// ============================================================================

/**
 * NATIVE BEHAVIOR:
 * - Returns new function with bound this and partial arguments
 * - Bound function can be used as constructor
 * - When used with new, this is ignored
 */

Function.prototype.myBind = function(context, ...boundArgs) {
    if (typeof this !== 'function') {
        throw new TypeError('Bind must be called on a function');
    }

    const fn = this;

    // Return bound function
    const boundFn = function(...args) {
        // Check if called as constructor
        const isNew = this instanceof boundFn;

        // If new, use instance as this; otherwise use bound context
        return fn.apply(
            isNew ? this : context,
            [...boundArgs, ...args]
        );
    };

    // Maintain prototype chain for constructor case
    if (fn.prototype) {
        boundFn.prototype = Object.create(fn.prototype);
    }

    return boundFn;
};

// Test
const boundGreet = greet.myBind(person, 'Hey');
console.log(boundGreet('!!!')); // "Hey, Alice!!!"

// Test with constructor
function Point(x, y) {
    this.x = x;
    this.y = y;
}
const BoundPoint = Point.myBind(null, 0);
const point = new BoundPoint(5);
console.log(point.x, point.y); // 0, 5

// ============================================================================
// SECTION 4: Object.create
// ============================================================================

/**
 * NATIVE BEHAVIOR:
 * - Creates object with specified prototype
 * - Optional property descriptors
 */

Object.myCreate = function(proto, propertiesObject) {
    if (proto !== null && typeof proto !== 'object') {
        throw new TypeError('Object prototype may only be an Object or null');
    }

    // Create empty function as constructor
    function F() {}
    F.prototype = proto;
    const obj = new F();

    // Add properties if provided
    if (propertiesObject !== undefined) {
        Object.defineProperties(obj, propertiesObject);
    }

    // Handle null prototype
    if (proto === null) {
        obj.__proto__ = null;
    }

    return obj;
};

// Test
const parent = { greet() { return 'Hello'; } };
const child = Object.myCreate(parent);
console.log(child.greet()); // "Hello"
console.log(Object.getPrototypeOf(child) === parent); // true

// ============================================================================
// SECTION 5: Object.assign
// ============================================================================

/**
 * NATIVE BEHAVIOR:
 * - Copies enumerable own properties from sources to target
 * - Modifies and returns target
 * - Shallow copy only
 */

Object.myAssign = function(target, ...sources) {
    if (target == null) {
        throw new TypeError('Cannot convert undefined or null to object');
    }

    const to = Object(target);

    for (const source of sources) {
        if (source != null) {
            // Get own enumerable properties including Symbols
            const keys = [
                ...Object.keys(source),
                ...Object.getOwnPropertySymbols(source).filter(
                    sym => Object.prototype.propertyIsEnumerable.call(source, sym)
                )
            ];

            for (const key of keys) {
                to[key] = source[key];
            }
        }
    }

    return to;
};

// Test
const obj1 = { a: 1 };
const obj2 = { b: 2 };
const merged = Object.myAssign({}, obj1, obj2);
console.log(merged); // { a: 1, b: 2 }

// ============================================================================
// SECTION 6: Object.keys / values / entries
// ============================================================================

Object.myKeys = function(obj) {
    if (obj == null) {
        throw new TypeError('Cannot convert undefined or null to object');
    }

    const result = [];
    for (const key in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, key)) {
            result.push(key);
        }
    }
    return result;
};

Object.myValues = function(obj) {
    if (obj == null) {
        throw new TypeError('Cannot convert undefined or null to object');
    }

    return Object.myKeys(obj).map(key => obj[key]);
};

Object.myEntries = function(obj) {
    if (obj == null) {
        throw new TypeError('Cannot convert undefined or null to object');
    }

    return Object.myKeys(obj).map(key => [key, obj[key]]);
};

// ============================================================================
// SECTION 7: debounce
// ============================================================================

/**
 * DEBOUNCE:
 * - Delays execution until after wait time has passed
 * - Resets delay if called again during wait
 * - Use case: Search input, window resize
 */

function debounce(fn, wait, options = {}) {
    let timeoutId = null;
    let lastArgs = null;
    let lastThis = null;
    let result;

    const { leading = false, trailing = true } = options;

    function invokeFunc() {
        const args = lastArgs;
        const thisArg = lastThis;
        lastArgs = null;
        lastThis = null;
        result = fn.apply(thisArg, args);
        return result;
    }

    function debounced(...args) {
        lastArgs = args;
        lastThis = this;

        const isInvoking = !timeoutId;

        // Clear existing timeout
        clearTimeout(timeoutId);

        // Set new timeout
        timeoutId = setTimeout(() => {
            timeoutId = null;
            if (trailing && lastArgs) {
                invokeFunc();
            }
        }, wait);

        // Invoke on leading edge
        if (leading && isInvoking) {
            invokeFunc();
        }

        return result;
    }

    // Cancel pending invocation
    debounced.cancel = function() {
        clearTimeout(timeoutId);
        timeoutId = null;
        lastArgs = null;
        lastThis = null;
    };

    return debounced;
}

// Simple version for interviews
function debounceSimple(fn, delay) {
    let timeoutId;
    return function(...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => fn.apply(this, args), delay);
    };
}

// Test
const debouncedSearch = debounce((query) => {
    console.log('Searching:', query);
}, 300);

// ============================================================================
// SECTION 8: throttle
// ============================================================================

/**
 * THROTTLE:
 * - Limits execution to at most once per wait period
 * - Use case: Scroll events, button clicks
 */

function throttle(fn, wait, options = {}) {
    let timeoutId = null;
    let lastArgs = null;
    let lastThis = null;
    let lastCallTime = 0;

    const { leading = true, trailing = true } = options;

    function invokeFunc() {
        const args = lastArgs;
        const thisArg = lastThis;
        lastArgs = null;
        lastThis = null;
        lastCallTime = Date.now();
        return fn.apply(thisArg, args);
    }

    function throttled(...args) {
        const now = Date.now();
        const elapsed = now - lastCallTime;
        const remaining = wait - elapsed;

        lastArgs = args;
        lastThis = this;

        // Should invoke now?
        if (remaining <= 0) {
            clearTimeout(timeoutId);
            timeoutId = null;
            return invokeFunc();
        }

        // Schedule trailing call
        if (!timeoutId && trailing) {
            timeoutId = setTimeout(() => {
                timeoutId = null;
                if (trailing && lastArgs) {
                    invokeFunc();
                }
            }, remaining);
        }

        // Leading call
        if (leading && !lastCallTime) {
            return invokeFunc();
        }
    }

    throttled.cancel = function() {
        clearTimeout(timeoutId);
        timeoutId = null;
        lastCallTime = 0;
        lastArgs = null;
        lastThis = null;
    };

    return throttled;
}

// Simple version for interviews
function throttleSimple(fn, limit) {
    let inThrottle = false;
    return function(...args) {
        if (!inThrottle) {
            fn.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// ============================================================================
// SECTION 9: Other Important Polyfills
// ============================================================================

// instanceof
function myInstanceof(obj, Constructor) {
    if (obj === null || typeof obj !== 'object') {
        return false;
    }

    let proto = Object.getPrototypeOf(obj);
    while (proto !== null) {
        if (proto === Constructor.prototype) {
            return true;
        }
        proto = Object.getPrototypeOf(proto);
    }

    return false;
}

// new operator
function myNew(Constructor, ...args) {
    // Create object with Constructor's prototype
    const obj = Object.create(Constructor.prototype);

    // Execute constructor with obj as this
    const result = Constructor.apply(obj, args);

    // Return result if it's an object, otherwise obj
    return (result !== null && typeof result === 'object') ? result : obj;
}

// ============================================================================
// SECTION 10: Interview Summary
// ============================================================================

/**
 * KEY DIFFERENCES:
 *
 * call vs apply vs bind:
 * - call: invoke now, separate args
 * - apply: invoke now, array args
 * - bind: return bound function
 *
 * debounce vs throttle:
 * - debounce: wait until done, then execute
 * - throttle: execute at most once per period
 *
 * EDGE CASES TO REMEMBER:
 * - null/undefined context becomes globalThis
 * - bind with new ignores bound context
 * - Symbols as property keys
 * - Prototype chain maintenance
 */
