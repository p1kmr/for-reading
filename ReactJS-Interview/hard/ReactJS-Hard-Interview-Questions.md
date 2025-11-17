# ReactJS Frontend Interview Questions - HARD LEVEL

## Table of Contents
1. [Custom Hook: useFetch with Caching](#1-custom-hook-usefetch-with-caching)
2. [Compound Component Pattern](#2-compound-component-pattern)
3. [Performance Optimization - React.memo, useCallback, useMemo](#3-performance-optimization)
4. [Context API with useReducer](#4-context-api-with-usereducer)
5. [Advanced Form with Field Arrays](#5-advanced-form-with-field-arrays)

---

## 1. Custom Hook: useFetch with Caching

### Problem Statement
Create a reusable custom hook for fetching data with loading states, error handling, and caching.

### What Interviewer is Testing
- Custom hooks creation
- Caching strategy
- Error boundaries
- AbortController for cleanup
- Generic/reusable patterns

### Solution with Detailed Comments

```jsx
import { useState, useEffect, useRef, useCallback } from 'react';

/**
 * CACHE STORE
 *
 * WHY outside component?
 * - Shared across all hook instances
 * - Persists between component unmounts
 * - Acts as global cache
 *
 * WHY Map instead of object?
 * - Better performance for frequent additions/deletions
 * - Can use any type as key (not just strings)
 * - Has size property
 */
const cache = new Map();

/**
 * useFetch Custom Hook - Hard Level
 *
 * PURPOSE: Reusable data fetching with caching, loading, and error states
 *
 * @param {string} url - API endpoint to fetch
 * @param {object} options - Fetch options (method, headers, etc.)
 * @returns {object} { data, loading, error, refetch }
 *
 * FEATURES:
 * 1. Loading state management
 * 2. Error handling
 * 3. Request cancellation (cleanup)
 * 4. Caching (avoid duplicate requests)
 * 5. Manual refetch
 */
function useFetch(url, options = {}) {
  // STATE 1: Fetched data
  const [data, setData] = useState(null);

  // STATE 2: Loading indicator
  const [loading, setLoading] = useState(false);

  // STATE 3: Error object
  const [error, setError] = useState(null);

  // REF: AbortController for cancelling requests
  // WHY useRef?
  // - Need to persist across renders
  // - Don't need re-render when it changes
  const abortControllerRef = useRef(null);

  /**
   * FETCH FUNCTION
   *
   * WHY useCallback?
   * - Can be called manually (refetch)
   * - Used in useEffect dependency array
   * - Prevent unnecessary re-creations
   */
  const fetchData = useCallback(async () => {
    // CACHE CHECK: Return cached data if available
    if (cache.has(url)) {
      console.log(`✓ Cache hit for: ${url}`);
      setData(cache.get(url));
      setLoading(false);
      return;
    }

    console.log(`↻ Fetching: ${url}`);

    // CREATE ABORT CONTROLLER
    // WHY?
    // - Cancel fetch if component unmounts
    // - Cancel previous fetch if new fetch starts
    // - Prevent memory leaks and state updates on unmounted component
    abortControllerRef.current = new AbortController();

    setLoading(true);
    setError(null);

    try {
      const response = await fetch(url, {
        ...options,
        signal: abortControllerRef.current.signal  // ← Connect signal
      });

      // CHECK RESPONSE STATUS
      if (!response.ok) {
        throw new Error(`HTTP Error: ${response.status} ${response.statusText}`);
      }

      const result = await response.json();

      // CACHE THE RESULT
      cache.set(url, result);
      console.log(`✓ Cached: ${url}`);

      // UPDATE STATE
      setData(result);
      setLoading(false);

    } catch (err) {
      // HANDLE ABORT (not a real error)
      if (err.name === 'AbortError') {
        console.log(`⊗ Fetch aborted: ${url}`);
        return;  // Don't set error state for aborts
      }

      // HANDLE REAL ERRORS
      console.error(`✗ Fetch error: ${url}`, err);
      setError(err.message);
      setLoading(false);
    }
  }, [url, options]);  // Re-create if url or options change

  /**
   * EFFECT: Fetch on mount or when dependencies change
   */
  useEffect(() => {
    fetchData();

    // CLEANUP: Abort fetch if component unmounts or url changes
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
        console.log(`⊗ Cleanup: Aborting fetch for ${url}`);
      }
    };
  }, [fetchData]);

  /**
   * REFETCH FUNCTION
   *
   * PURPOSE: Manually trigger re-fetch (bypass cache)
   * USE CASE: Refresh button, pull-to-refresh
   */
  const refetch = useCallback(() => {
    cache.delete(url);  // Clear cache for this URL
    fetchData();
  }, [url, fetchData]);

  return { data, loading, error, refetch };
}

/**
 * USAGE EXAMPLE
 */
function UserProfile({ userId }) {
  const {
    data: user,
    loading,
    error,
    refetch
  } = useFetch(`https://api.example.com/users/${userId}`);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;
  if (!user) return null;

  return (
    <div>
      <h1>{user.name}</h1>
      <p>{user.email}</p>
      <button onClick={refetch}>Refresh</button>
    </div>
  );
}

export default useFetch;
```

### Interviewer Follow-up Questions & Answers

**Q1: What is AbortController and why use it?**
```
A: AbortController cancels fetch requests

PROBLEM:
- Component unmounts while fetch is in progress
- Fetch completes after unmount
- Tries to setState on unmounted component
- React warning + potential memory leak

SOLUTION:
const controller = new AbortController();
fetch(url, { signal: controller.signal });
controller.abort();  // Cancel the request

HOW IT WORKS:
1. Create AbortController
2. Pass signal to fetch
3. Call abort() to cancel
4. Fetch throws AbortError (catch it)

WHEN TO USE:
✓ Component unmounts during fetch
✓ User navigates away
✓ New fetch replaces old one (search)
✓ Timeout (abort after X seconds)
```

**Q2: Why store cache outside the hook?**
```
A: Cache needs to persist across all instances

IF INSIDE HOOK:
function useFetch(url) {
  const [cache] = useState(new Map());  // ← New cache per instance!
  // Component A's cache ≠ Component B's cache
}

IF OUTSIDE HOOK:
const cache = new Map();  // ← Shared cache!
function useFetch(url) {
  // All components share same cache
}

BENEFITS:
✓ Avoid duplicate requests across components
✓ Faster (data already cached)
✓ Better UX (instant load)

TRADE-OFFS:
- Cache grows over time (may need eviction strategy)
- Stale data (may need TTL or manual invalidation)
```

---

## 2. Compound Component Pattern

### Problem Statement
Create a flexible, composable dropdown component using the compound component pattern.

### What Interviewer is Testing
- Advanced component patterns
- Context API
- Component composition
- Flexible APIs
- Separation of concerns

### Solution with Detailed Comments

```jsx
import React, { createContext, useContext, useState } from 'react';

/**
 * COMPOUND COMPONENT PATTERN
 *
 * CONCEPT: Parent component shares state with children via Context
 *
 * WHY?
 * - Flexible composition
 * - Children can be reordered
 * - Parent controls behavior, children control rendering
 * - Better than props drilling
 */

/**
 * STEP 1: Create Context
 */
const DropdownContext = createContext();

/**
 * CUSTOM HOOK: useDropdownContext
 *
 * PURPOSE: Access dropdown state in child components
 * ERROR: Throw if used outside Dropdown
 */
function useDropdownContext() {
  const context = useContext(DropdownContext);

  if (!context) {
    throw new Error('Dropdown compound components must be used within <Dropdown>');
  }

  return context;
}

/**
 * PARENT COMPONENT: Dropdown
 *
 * RESPONSIBILITIES:
 * - Manage open/close state
 * - Provide context to children
 * - Handle outside click
 */
function Dropdown({ children }) {
  const [isOpen, setIsOpen] = useState(false);
  const [selectedValue, setSelectedValue] = useState(null);

  const toggle = () => setIsOpen(prev => !prev);
  const open = () => setIsOpen(true);
  const close = () => setIsOpen(false);

  const select = (value) => {
    setSelectedValue(value);
    close();
  };

  // CONTEXT VALUE: Share with children
  const value = {
    isOpen,
    selectedValue,
    toggle,
    open,
    close,
    select
  };

  return (
    <DropdownContext.Provider value={value}>
      <div style={{ position: 'relative', display: 'inline-block' }}>
        {children}
      </div>
    </DropdownContext.Provider>
  );
}

/**
 * CHILD COMPONENT: Dropdown.Trigger
 *
 * RESPONSIBILITY: Button to open/close dropdown
 */
Dropdown.Trigger = function DropdownTrigger({ children }) {
  const { toggle, selectedValue } = useDropdownContext();

  return (
    <button
      onClick={toggle}
      style={{
        padding: '10px 20px',
        border: '1px solid #ddd',
        borderRadius: '4px',
        background: 'white',
        cursor: 'pointer'
      }}
    >
      {children || selectedValue || 'Select...'}
    </button>
  );
};

/**
 * CHILD COMPONENT: Dropdown.Menu
 *
 * RESPONSIBILITY: Container for dropdown items
 */
Dropdown.Menu = function DropdownMenu({ children }) {
  const { isOpen } = useDropdownContext();

  if (!isOpen) return null;

  return (
    <div style={{
      position: 'absolute',
      top: '100%',
      left: 0,
      marginTop: '4px',
      minWidth: '200px',
      background: 'white',
      border: '1px solid #ddd',
      borderRadius: '4px',
      boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
      zIndex: 1000
    }}>
      {children}
    </div>
  );
};

/**
 * CHILD COMPONENT: Dropdown.Item
 *
 * RESPONSIBILITY: Individual selectable item
 */
Dropdown.Item = function DropdownItem({ value, children }) {
  const { select, selectedValue } = useDropdownContext();

  const isSelected = selectedValue === value;

  return (
    <div
      onClick={() => select(value)}
      style={{
        padding: '10px 20px',
        cursor: 'pointer',
        background: isSelected ? '#f0f0f0' : 'white',
        fontWeight: isSelected ? 'bold' : 'normal'
      }}
      onMouseEnter={(e) => e.target.style.background = '#f5f5f5'}
      onMouseLeave={(e) => e.target.style.background = isSelected ? '#f0f0f0' : 'white'}
    >
      {children}
    </div>
  );
};

/**
 * USAGE EXAMPLE
 */
function App() {
  return (
    <div style={{ padding: '50px' }}>
      <h1>Compound Component Pattern</h1>

      {/* FLEXIBLE COMPOSITION */}
      <Dropdown>
        <Dropdown.Trigger>Choose a fruit</Dropdown.Trigger>
        <Dropdown.Menu>
          <Dropdown.Item value="apple">🍎 Apple</Dropdown.Item>
          <Dropdown.Item value="banana">🍌 Banana</Dropdown.Item>
          <Dropdown.Item value="cherry">🍒 Cherry</Dropdown.Item>
        </Dropdown.Menu>
      </Dropdown>

      {/* CAN REORDER CHILDREN */}
      <Dropdown>
        <Dropdown.Menu>
          <Dropdown.Item value="react">React</Dropdown.Item>
          <Dropdown.Item value="vue">Vue</Dropdown.Item>
        </Dropdown.Menu>
        <Dropdown.Trigger />
      </Dropdown>

      {/* CAN CUSTOMIZE */}
      <Dropdown>
        <Dropdown.Trigger>
          <strong>Pick one</strong>
        </Dropdown.Trigger>
        <Dropdown.Menu>
          <Dropdown.Item value="1">Option 1</Dropdown.Item>
        </Dropdown.Menu>
      </Dropdown>
    </div>
  );
}

export default Dropdown;
```

### Interviewer Follow-up: Benefits of Compound Components?

```
TRADITIONAL PROP-BASED:
<Dropdown
  trigger={<button>Click</button>}
  items={[
    { value: 'a', label: 'A' },
    { value: 'b', label: 'B' }
  ]}
  renderItem={(item) => <div>{item.label}</div>}
/>

PROBLEMS:
- Many props (complex API)
- Limited flexibility
- Hard to customize

COMPOUND COMPONENT:
<Dropdown>
  <Dropdown.Trigger>Click</Dropdown.Trigger>
  <Dropdown.Menu>
    <Dropdown.Item value="a">A</Dropdown.Item>
    <Dropdown.Item value="b">B</Dropdown.Item>
  </Dropdown.Menu>
</Dropdown>

BENEFITS:
✓ Flexible composition
✓ Easy to customize
✓ Clear hierarchy
✓ Reorderable children
✓ Clean API (fewer props)

EXAMPLES IN LIBRARIES:
- Radix UI: <Tabs>, <Accordion>
- Reach UI: <Menu>, <Tabs>
- Headless UI: <Listbox>, <Disclosure>
```

---

## 3. Performance Optimization

### Problem Statement
Demonstrate proper use of React.memo, useCallback, and useMemo to prevent unnecessary re-renders.

### Solution with Detailed Comments

```jsx
import React, { useState, useCallback, useMemo, memo } from 'react';

/**
 * PROBLEM SCENARIO: Expensive List with Filters
 *
 * WITHOUT OPTIMIZATION:
 * - Parent re-renders → All children re-render
 * - Filter function recreated → Child prop changes → Re-render
 * - Expensive calculations run every render
 */

/**
 * CHILD COMPONENT: ListItem
 *
 * WHY React.memo?
 * - Only re-render if props actually change
 * - Prevents unnecessary re-renders from parent
 *
 * SYNTAX:
 * const Memoized = memo(Component, arePropsEqual?)
 */
const ListItem = memo(function ListItem({ item, onDelete }) {
  console.log(`Rendering item: ${item.name}`);

  return (
    <div style={{
      padding: '10px',
      margin: '5px 0',
      border: '1px solid #ddd',
      display: 'flex',
      justifyContent: 'space-between'
    }}>
      <span>{item.name} - ${item.price}</span>
      <button onClick={() => onDelete(item.id)}>Delete</button>
    </div>
  );
}, (prevProps, nextProps) => {
  // CUSTOM COMPARISON (optional)
  // Return true if props are equal (skip re-render)
  // Return false if props changed (re-render)

  return (
    prevProps.item.id === nextProps.item.id &&
    prevProps.item.name === nextProps.item.name &&
    prevProps.item.price === nextProps.item.price &&
    prevProps.onDelete === nextProps.onDelete
  );
});

/**
 * PARENT COMPONENT: ProductList
 */
function ProductList() {
  const [products, setProducts] = useState([
    { id: 1, name: 'Laptop', price: 1000, category: 'electronics' },
    { id: 2, name: 'Mouse', price: 25, category: 'electronics' },
    { id: 3, name: 'Desk', price: 300, category: 'furniture' },
    { id: 4, name: 'Chair', price: 150, category: 'furniture' }
  ]);

  const [filter, setFilter] = useState('');
  const [sortBy, setSortBy] = useState('name');

  // UNRELATED STATE (causes re-renders but shouldn't affect list)
  const [count, setCount] = useState(0);

  /**
   * DELETE HANDLER
   *
   * WHY useCallback?
   * - Function is passed as prop to child
   * - Without useCallback: new function every render
   * - New function → child sees prop change → re-renders
   * - With useCallback: same function reference (unless deps change)
   *
   * DEPENDENCIES: [products]
   * - Only recreate function when products change
   */
  const handleDelete = useCallback((id) => {
    setProducts(prev => prev.filter(item => item.id !== id));
  }, []);  // Empty deps: function never recreated

  /**
   * FILTERED & SORTED PRODUCTS
   *
   * WHY useMemo?
   * - Expensive operation (filter + sort)
   * - Without useMemo: runs on EVERY render (even unrelated state changes)
   * - With useMemo: only runs when dependencies change
   *
   * DEPENDENCIES: [products, filter, sortBy]
   * - Recalculate only when these change
   * - NOT when count changes
   */
  const filteredAndSortedProducts = useMemo(() => {
    console.log('🔄 Recalculating filtered products...');

    // FILTER
    let result = products;
    if (filter) {
      result = result.filter(p =>
        p.name.toLowerCase().includes(filter.toLowerCase()) ||
        p.category.toLowerCase().includes(filter.toLowerCase())
      );
    }

    // SORT
    result = [...result].sort((a, b) => {
      if (sortBy === 'name') {
        return a.name.localeCompare(b.name);
      }
      return a.price - b.price;
    });

    return result;
  }, [products, filter, sortBy]);

  /**
   * EXPENSIVE CALCULATION (for demonstration)
   *
   * WITHOUT useMemo: Runs on every render
   * WITH useMemo: Only when products change
   */
  const totalPrice = useMemo(() => {
    console.log('💰 Calculating total price...');
    return products.reduce((sum, p) => sum + p.price, 0);
  }, [products]);

  return (
    <div style={{ padding: '20px', maxWidth: '600px' }}>
      <h1>Product List - Performance Optimized</h1>

      {/* UNRELATED STATE (to demonstrate optimization) */}
      <div style={{ marginBottom: '20px', padding: '10px', background: '#f0f0f0' }}>
        <p>Unrelated counter (shouldn't trigger list re-render): {count}</p>
        <button onClick={() => setCount(c => c + 1)}>Increment</button>
      </div>

      {/* FILTERS */}
      <input
        type="text"
        value={filter}
        onChange={(e) => setFilter(e.target.value)}
        placeholder="Filter by name or category..."
        style={{ width: '100%', padding: '8px', marginBottom: '10px' }}
      />

      <select
        value={sortBy}
        onChange={(e) => setSortBy(e.target.value)}
        style={{ padding: '8px', marginBottom: '20px' }}
      >
        <option value="name">Sort by Name</option>
        <option value="price">Sort by Price</option>
      </select>

      {/* TOTAL PRICE (memoized) */}
      <div style={{ marginBottom: '20px', fontWeight: 'bold' }}>
        Total Price: ${totalPrice}
      </div>

      {/* LIST */}
      <div>
        {filteredAndSortedProducts.map(product => (
          <ListItem
            key={product.id}
            item={product}
            onDelete={handleDelete}
          />
        ))}
      </div>

      {/* INSTRUCTIONS */}
      <div style={{ marginTop: '20px', padding: '10px', background: '#e7f3ff' }}>
        <strong>Try this:</strong>
        <ol>
          <li>Click "Increment" → Only counter re-renders (not list items) ✓</li>
          <li>Type in filter → Filtered products recalculated ✓</li>
          <li>Delete item → Only that item re-renders ✓</li>
        </ol>
      </div>
    </div>
  );
}

export default ProductList;
```

### Performance Comparison Table

```
| Scenario | Without Optimization | With Optimization |
|----------|---------------------|-------------------|
| Click "Increment" | All items re-render | No items re-render |
| Type in filter | Filter runs + all items re-render | Filter runs + only affected items re-render |
| Change sort | Sort runs + all items re-render | Sort runs + only if needed |
| Delete item | All items re-render | Only deleted item unmounts |
```

### Interviewer Follow-up: When to use each optimization?

```
React.memo:
✓ Component renders often with same props
✓ Heavy render logic
✗ Props change frequently (memo overhead wasted)
✗ Cheap render (memo overhead > render cost)

useCallback:
✓ Function passed to optimized child (memo)
✓ Function in dependency array
✗ Not passed to children
✗ No performance issue

useMemo:
✓ Expensive calculations
✓ Creating objects/arrays for child props
✗ Simple calculations (overhead > benefit)
✗ Not used by children

GOLDEN RULE:
Measure first, optimize second!
Don't prematurely optimize.
```

---

## 4. Context API with useReducer

### Problem Statement
Build a global state management solution using Context API and useReducer for a shopping cart.

### Solution with Detailed Comments

```jsx
import React, { createContext, useContext, useReducer } from 'react';

/**
 * CONTEXT + useReducer PATTERN
 *
 * WHY useReducer over useState?
 * - Complex state logic
 * - Multiple sub-values
 * - Next state depends on previous
 * - Predictable state updates (actions)
 *
 * SIMILAR TO: Redux (but built-in)
 */

/**
 * STEP 1: Define Action Types
 *
 * WHY constants?
 * - Avoid typos
 * - Autocomplete in IDE
 * - Easy refactoring
 */
const ADD_ITEM = 'ADD_ITEM';
const REMOVE_ITEM = 'REMOVE_ITEM';
const UPDATE_QUANTITY = 'UPDATE_QUANTITY';
const CLEAR_CART = 'CLEAR_CART';

/**
 * STEP 2: Create Context
 */
const CartContext = createContext();

/**
 * STEP 3: Reducer Function
 *
 * SIGNATURE: (state, action) => newState
 *
 * RULES:
 * - Must be pure (no side effects)
 * - Must return new state (immutable)
 * - Same input → same output
 */
function cartReducer(state, action) {
  switch (action.type) {
    case ADD_ITEM: {
      const existingItem = state.items.find(
        item => item.id === action.payload.id
      );

      // If item exists, increase quantity
      if (existingItem) {
        return {
          ...state,
          items: state.items.map(item =>
            item.id === action.payload.id
              ? { ...item, quantity: item.quantity + 1 }
              : item
          )
        };
      }

      // If new item, add to cart
      return {
        ...state,
        items: [...state.items, { ...action.payload, quantity: 1 }]
      };
    }

    case REMOVE_ITEM:
      return {
        ...state,
        items: state.items.filter(item => item.id !== action.payload)
      };

    case UPDATE_QUANTITY:
      return {
        ...state,
        items: state.items.map(item =>
          item.id === action.payload.id
            ? { ...item, quantity: action.payload.quantity }
            : item
        )
      };

    case CLEAR_CART:
      return {
        ...state,
        items: []
      };

    default:
      return state;
  }
}

/**
 * STEP 4: Provider Component
 */
export function CartProvider({ children }) {
  const [state, dispatch] = useReducer(cartReducer, {
    items: []
  });

  // ACTION CREATORS (for cleaner usage)
  const addItem = (item) => dispatch({ type: ADD_ITEM, payload: item });
  const removeItem = (id) => dispatch({ type: REMOVE_ITEM, payload: id });
  const updateQuantity = (id, quantity) =>
    dispatch({ type: UPDATE_QUANTITY, payload: { id, quantity } });
  const clearCart = () => dispatch({ type: CLEAR_CART });

  // COMPUTED VALUES
  const totalItems = state.items.reduce((sum, item) => sum + item.quantity, 0);
  const totalPrice = state.items.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  );

  const value = {
    items: state.items,
    totalItems,
    totalPrice,
    addItem,
    removeItem,
    updateQuantity,
    clearCart
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

/**
 * STEP 5: Custom Hook
 */
export function useCart() {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within CartProvider');
  }
  return context;
}

/**
 * USAGE EXAMPLE
 */
function ProductCard({ product }) {
  const { addItem } = useCart();

  return (
    <div style={{ border: '1px solid #ddd', padding: '15px', margin: '10px' }}>
      <h3>{product.name}</h3>
      <p>${product.price}</p>
      <button onClick={() => addItem(product)}>Add to Cart</button>
    </div>
  );
}

function CartSummary() {
  const { items, totalItems, totalPrice, removeItem, clearCart } = useCart();

  return (
    <div style={{ padding: '20px', border: '2px solid #333' }}>
      <h2>Cart ({totalItems} items)</h2>
      {items.map(item => (
        <div key={item.id} style={{ marginBottom: '10px' }}>
          {item.name} x {item.quantity} = ${item.price * item.quantity}
          <button onClick={() => removeItem(item.id)}>Remove</button>
        </div>
      ))}
      <div style={{ marginTop: '20px', fontWeight: 'bold' }}>
        Total: ${totalPrice.toFixed(2)}
      </div>
      <button onClick={clearCart}>Clear Cart</button>
    </div>
  );
}

function App() {
  const products = [
    { id: 1, name: 'Laptop', price: 1000 },
    { id: 2, name: 'Mouse', price: 25 },
    { id: 3, name: 'Keyboard', price: 75 }
  ];

  return (
    <CartProvider>
      <div style={{ padding: '20px' }}>
        <h1>Shopping Cart with Context + useReducer</h1>
        <div style={{ display: 'flex', gap: '20px' }}>
          <div>
            <h2>Products</h2>
            {products.map(product => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
          <div>
            <CartSummary />
          </div>
        </div>
      </div>
    </CartProvider>
  );
}

export default App;
```

---

## Summary: Hard Level Concepts

### Key Takeaways

1. **Custom Hooks**: Reusable logic extraction
2. **Compound Components**: Flexible, composable APIs
3. **Performance**: React.memo, useCallback, useMemo
4. **State Management**: Context + useReducer
5. **Advanced Patterns**: Render props, HOCs, hooks

### When to Use What

```
useState: Simple state
useReducer: Complex state with multiple actions
Context: Global state (theme, auth, etc.)
Custom Hooks: Reusable logic
React.memo: Prevent unnecessary re-renders
useMemo: Expensive calculations
useCallback: Stable function references
```

### Interview Success Tips

1. **Explain your thinking**: Why you chose a pattern
2. **Discuss trade-offs**: Nothing is perfect
3. **Consider edge cases**: Loading, errors, empty states
4. **Performance**: Know when and why to optimize
5. **Accessibility**: ARIA attributes, keyboard nav
6. **Code organization**: Clean, readable, maintainable
