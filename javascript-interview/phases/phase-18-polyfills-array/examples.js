/**
 * ============================================================================
 * PHASE 18: POLYFILLS - ARRAY METHODS
 * ============================================================================
 *
 * INTERVIEW IMPORTANCE: Critical (Asked in 80%+ of JS interviews)
 *
 * For each polyfill:
 * 1. Understand the native method
 * 2. Know edge cases
 * 3. Implement step by step
 * 4. Test thoroughly
 */

// ============================================================================
// SECTION 1: Array.prototype.map
// ============================================================================

/**
 * NATIVE BEHAVIOR:
 * - Creates new array with results of calling callback on each element
 * - Callback receives: (element, index, array)
 * - Optional thisArg for callback context
 * - Preserves sparse array holes
 * - Does not mutate original
 */

Array.prototype.myMap = function(callback, thisArg) {
    // Error checking
    if (this == null) {
        throw new TypeError('Cannot read property "map" of null or undefined');
    }
    if (typeof callback !== 'function') {
        throw new TypeError(callback + ' is not a function');
    }

    // Convert to object (handles primitives)
    const O = Object(this);
    // Get length as unsigned 32-bit integer
    const len = O.length >>> 0;

    // Create result array
    const result = new Array(len);

    // Iterate
    for (let i = 0; i < len; i++) {
        // Check if index exists (sparse array handling)
        if (i in O) {
            // Call callback with thisArg context
            result[i] = callback.call(thisArg, O[i], i, O);
        }
        // Skip holes in sparse arrays
    }

    return result;
};

// Test
console.log([1, 2, 3].myMap(x => x * 2)); // [2, 4, 6]
console.log([1, , 3].myMap(x => x * 2)); // [2, empty, 6] (preserves hole)

// ============================================================================
// SECTION 2: Array.prototype.filter
// ============================================================================

/**
 * NATIVE BEHAVIOR:
 * - Creates new array with elements that pass test
 * - Callback must return truthy/falsy
 * - Does not include holes in result
 */

Array.prototype.myFilter = function(callback, thisArg) {
    if (this == null) {
        throw new TypeError('Cannot read property "filter" of null or undefined');
    }
    if (typeof callback !== 'function') {
        throw new TypeError(callback + ' is not a function');
    }

    const O = Object(this);
    const len = O.length >>> 0;
    const result = [];

    for (let i = 0; i < len; i++) {
        if (i in O) {
            const val = O[i];
            // Only push if callback returns truthy
            if (callback.call(thisArg, val, i, O)) {
                result.push(val);
            }
        }
    }

    return result;
};

// Test
console.log([1, 2, 3, 4, 5].myFilter(x => x > 2)); // [3, 4, 5]
console.log([1, , 3].myFilter(x => true)); // [1, 3] (no holes)

// ============================================================================
// SECTION 3: Array.prototype.reduce
// ============================================================================

/**
 * NATIVE BEHAVIOR:
 * - Reduces array to single value
 * - Callback receives: (accumulator, current, index, array)
 * - Initial value optional (but recommended)
 * - Without initial value, uses first element
 * - Throws on empty array without initial value
 */

Array.prototype.myReduce = function(callback, initialValue) {
    if (this == null) {
        throw new TypeError('Cannot read property "reduce" of null or undefined');
    }
    if (typeof callback !== 'function') {
        throw new TypeError(callback + ' is not a function');
    }

    const O = Object(this);
    const len = O.length >>> 0;

    let k = 0;  // Current index
    let accumulator;

    // Determine initial value
    if (arguments.length >= 2) {
        accumulator = initialValue;
    } else {
        // Find first existing element
        let kPresent = false;
        while (k < len && !kPresent) {
            if (k in O) {
                accumulator = O[k];
                kPresent = true;
            }
            k++;
        }

        // Empty array with no initial value
        if (!kPresent) {
            throw new TypeError('Reduce of empty array with no initial value');
        }
    }

    // Process remaining elements
    while (k < len) {
        if (k in O) {
            accumulator = callback(accumulator, O[k], k, O);
        }
        k++;
    }

    return accumulator;
};

// Test
console.log([1, 2, 3, 4].myReduce((acc, val) => acc + val, 0)); // 10
console.log([1, 2, 3, 4].myReduce((acc, val) => acc + val)); // 10 (no initial)
// [].myReduce((acc, val) => acc + val); // TypeError

// ============================================================================
// SECTION 4: Array.prototype.forEach
// ============================================================================

Array.prototype.myForEach = function(callback, thisArg) {
    if (this == null) {
        throw new TypeError('Cannot read property "forEach" of null or undefined');
    }
    if (typeof callback !== 'function') {
        throw new TypeError(callback + ' is not a function');
    }

    const O = Object(this);
    const len = O.length >>> 0;

    for (let i = 0; i < len; i++) {
        if (i in O) {
            callback.call(thisArg, O[i], i, O);
        }
    }

    // Returns undefined
};

// ============================================================================
// SECTION 5: Array.prototype.find / findIndex
// ============================================================================

Array.prototype.myFind = function(callback, thisArg) {
    if (typeof callback !== 'function') {
        throw new TypeError(callback + ' is not a function');
    }

    const O = Object(this);
    const len = O.length >>> 0;

    for (let i = 0; i < len; i++) {
        const value = O[i];
        if (callback.call(thisArg, value, i, O)) {
            return value;  // Return element
        }
    }

    return undefined;  // Not found
};

Array.prototype.myFindIndex = function(callback, thisArg) {
    if (typeof callback !== 'function') {
        throw new TypeError(callback + ' is not a function');
    }

    const O = Object(this);
    const len = O.length >>> 0;

    for (let i = 0; i < len; i++) {
        if (callback.call(thisArg, O[i], i, O)) {
            return i;  // Return index
        }
    }

    return -1;  // Not found
};

// Test
console.log([1, 5, 10, 15].myFind(x => x > 8)); // 10
console.log([1, 5, 10, 15].myFindIndex(x => x > 8)); // 2

// ============================================================================
// SECTION 6: Array.prototype.some / every
// ============================================================================

Array.prototype.mySome = function(callback, thisArg) {
    if (typeof callback !== 'function') {
        throw new TypeError(callback + ' is not a function');
    }

    const O = Object(this);
    const len = O.length >>> 0;

    for (let i = 0; i < len; i++) {
        if (i in O && callback.call(thisArg, O[i], i, O)) {
            return true;  // Short-circuit on first truthy
        }
    }

    return false;
};

Array.prototype.myEvery = function(callback, thisArg) {
    if (typeof callback !== 'function') {
        throw new TypeError(callback + ' is not a function');
    }

    const O = Object(this);
    const len = O.length >>> 0;

    for (let i = 0; i < len; i++) {
        if (i in O && !callback.call(thisArg, O[i], i, O)) {
            return false;  // Short-circuit on first falsy
        }
    }

    return true;
};

// Test
console.log([1, 2, 3].mySome(x => x > 2)); // true
console.log([1, 2, 3].myEvery(x => x > 0)); // true

// ============================================================================
// SECTION 7: Array.prototype.flat
// ============================================================================

Array.prototype.myFlat = function(depth = 1) {
    const O = Object(this);
    const len = O.length >>> 0;
    const result = [];

    const flatten = (arr, d) => {
        for (let i = 0; i < arr.length; i++) {
            if (i in arr) {
                const el = arr[i];
                if (Array.isArray(el) && d > 0) {
                    flatten(el, d - 1);
                } else {
                    result.push(el);
                }
            }
        }
    };

    flatten(O, depth);
    return result;
};

// Test
console.log([1, [2, [3, [4]]]].myFlat()); // [1, 2, [3, [4]]]
console.log([1, [2, [3, [4]]]].myFlat(2)); // [1, 2, 3, [4]]
console.log([1, [2, [3, [4]]]].myFlat(Infinity)); // [1, 2, 3, 4]

// ============================================================================
// SECTION 8: Array.from
// ============================================================================

Array.myFrom = function(arrayLike, mapFn, thisArg) {
    // Error checking
    if (arrayLike == null) {
        throw new TypeError('Cannot convert undefined or null to object');
    }
    if (mapFn !== undefined && typeof mapFn !== 'function') {
        throw new TypeError(mapFn + ' is not a function');
    }

    const items = Object(arrayLike);
    const len = items.length >>> 0;
    const result = [];

    for (let i = 0; i < len; i++) {
        const value = items[i];
        if (mapFn) {
            result[i] = mapFn.call(thisArg, value, i);
        } else {
            result[i] = value;
        }
    }

    return result;
};

// Test
console.log(Array.myFrom("abc")); // ["a", "b", "c"]
console.log(Array.myFrom([1, 2, 3], x => x * 2)); // [2, 4, 6]
console.log(Array.myFrom({ length: 3 }, (_, i) => i)); // [0, 1, 2]

// ============================================================================
// SECTION 9: Array.prototype.includes
// ============================================================================

Array.prototype.myIncludes = function(searchElement, fromIndex = 0) {
    const O = Object(this);
    const len = O.length >>> 0;

    if (len === 0) return false;

    // Handle negative fromIndex
    let n = fromIndex | 0;
    let k = Math.max(n >= 0 ? n : len + n, 0);

    while (k < len) {
        const el = O[k];
        // Use SameValueZero comparison (handles NaN)
        if (el === searchElement || (el !== el && searchElement !== searchElement)) {
            return true;
        }
        k++;
    }

    return false;
};

// Test
console.log([1, 2, NaN].myIncludes(NaN)); // true (unlike indexOf)
console.log([1, 2, 3].myIncludes(2, 2)); // false

// ============================================================================
// SECTION 10: Array.prototype.indexOf
// ============================================================================

Array.prototype.myIndexOf = function(searchElement, fromIndex = 0) {
    const O = Object(this);
    const len = O.length >>> 0;

    if (len === 0) return -1;

    let n = fromIndex | 0;
    if (n >= len) return -1;

    let k = n >= 0 ? n : Math.max(len + n, 0);

    while (k < len) {
        if (k in O && O[k] === searchElement) {
            return k;
        }
        k++;
    }

    return -1;
};

// ============================================================================
// INTERVIEW TIPS
// ============================================================================

/**
 * When implementing polyfills:
 *
 * 1. ALWAYS check for:
 *    - null/undefined this
 *    - callback is function
 *    - proper type coercion
 *
 * 2. Handle edge cases:
 *    - Empty arrays
 *    - Sparse arrays (holes)
 *    - Negative indices
 *    - NaN comparisons
 *
 * 3. Use these patterns:
 *    - Object(this) for type coercion
 *    - length >>> 0 for unsigned int
 *    - (i in O) for sparse array check
 *    - callback.call(thisArg, ...) for context
 *
 * 4. Return correct types:
 *    - map/filter: new array
 *    - reduce: accumulated value
 *    - forEach: undefined
 *    - find: element or undefined
 *    - findIndex: index or -1
 *    - some/every: boolean
 */
