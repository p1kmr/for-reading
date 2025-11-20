# Custom Hooks - useLocalStorage

**Difficulty:** Hard
**Time:** 45-60 minutes
**Prerequisites:** React Hooks, localStorage API, custom hook patterns

---

## Problem Statement

Create a reusable custom hook that synchronizes React state with browser localStorage. Any changes to state automatically persist across page refreshes, browser sessions, and tabs.

### The Challenge

Without this hook, persisting state requires repetitive code:

```jsx
// ❌ Repetitive code in every component
function TodoApp() {
  // Read from localStorage on mount
  const [todos, setTodos] = useState(() => {
    const saved = localStorage.getItem('todos');
    return saved ? JSON.parse(saved) : [];
  });

  // Write to localStorage on every change
  useEffect(() => {
    localStorage.setItem('todos', JSON.stringify(todos));
  }, [todos]);

  // Same boilerplate for every piece of state!
  const [username, setUsername] = useState(() => {
    const saved = localStorage.getItem('username');
    return saved ? JSON.parse(saved) : '';
  });

  useEffect(() => {
    localStorage.setItem('username', JSON.stringify(username));
  }, [username]);
}
```

### The Solution

Custom hook abstracts this logic:

```jsx
// ✅ Clean, reusable
function TodoApp() {
  const [todos, setTodos] = useLocalStorage('todos', []);
  const [username, setUsername] = useLocalStorage('username', '');
  // Just works! Auto-synced with localStorage
}
```

**Real-World Use Cases:**
- User preferences (theme, language)
- Shopping cart data
- Form draft saving
- Authentication tokens
- Recently viewed items
- User settings

---

## Requirements

### Must Have
- ✅ API identical to useState
- ✅ Read from localStorage on mount
- ✅ Write to localStorage on state change
- ✅ Handle JSON serialization/parsing
- ✅ Handle localStorage errors gracefully
- ✅ Support any data type (strings, objects, arrays, numbers)

### Nice to Have
- ⭐ Sync across browser tabs
- ⭐ Support custom serializers
- ⭐ Type-safe (TypeScript)
- ⭐ Clear/remove functionality
- ⭐ Storage event listeners

---

## localStorage API Overview

### Browser localStorage Basics

```javascript
// STORAGE API (Web Storage API)
// ============================

// Set item (only stores strings!)
localStorage.setItem('key', 'value');

// Get item (returns string or null)
const value = localStorage.getItem('key');

// Remove item
localStorage.removeItem('key');

// Clear all items
localStorage.clear();

// Get number of items
const count = localStorage.length;

// Get key by index
const key = localStorage.key(0);

// IMPORTANT CONSTRAINTS:
// =====================
// 1. Only stores strings (must serialize objects)
// 2. ~5-10MB storage limit per domain
// 3. Synchronous API (blocks main thread)
// 4. Only works in browsers (not Node.js)
// 5. Same-origin policy applies
```

### Common Pitfalls

```javascript
// ❌ WRONG: Storing objects directly
localStorage.setItem('user', { name: 'John' });
// Stores "[object Object]" as string!

// ✅ CORRECT: JSON serialize first
localStorage.setItem('user', JSON.stringify({ name: 'John' }));

// ❌ WRONG: Forgetting to parse
const user = localStorage.getItem('user');
console.log(user.name);  // undefined! user is a string

// ✅ CORRECT: Parse JSON
const user = JSON.parse(localStorage.getItem('user'));
console.log(user.name);  // "John"
```

---

## Complete Solution

```jsx
import { useState, useEffect } from 'react';

/**
 * useLocalStorage Hook
 *
 * Syncs state with localStorage. API matches useState exactly.
 *
 * @param {string} key - localStorage key
 * @param {any} initialValue - Default value if key doesn't exist
 * @returns {[any, Function]} - [storedValue, setValue] tuple
 *
 * @example
 * const [name, setName] = useLocalStorage('name', 'John');
 * // name is synced with localStorage.getItem('name')
 * // setName('Jane') updates both state and localStorage
 */
function useLocalStorage(key, initialValue) {
  // ==============================================
  // STEP 1: Initialize state from localStorage
  // ==============================================

  // Why function form of useState?
  // - Only runs ONCE on component mount
  // - Lazy initialization (doesn't run on every render)
  // - Performance: avoid reading localStorage repeatedly
  const [storedValue, setStoredValue] = useState(() => {
    // SSR safety check
    if (typeof window === 'undefined') {
      return initialValue;
    }

    try {
      // Get from localStorage
      const item = window.localStorage.getItem(key);

      // Parse stored JSON or return initialValue if nothing stored
      return item ? JSON.parse(item) : initialValue;

    } catch (error) {
      // localStorage might be:
      // - Disabled (private browsing)
      // - Full (quota exceeded)
      // - Invalid JSON
      console.error(`Error reading localStorage key "${key}":`, error);
      return initialValue;
    }
  });

  // ==============================================
  // STEP 2: Save to localStorage when state changes
  // ==============================================

  useEffect(() => {
    // SSR safety check
    if (typeof window === 'undefined') {
      return;
    }

    try {
      // Serialize and save to localStorage
      // Why JSON.stringify?
      // - localStorage only stores strings
      // - Converts objects/arrays/numbers to strings
      window.localStorage.setItem(key, JSON.stringify(storedValue));

    } catch (error) {
      // Handle quota exceeded or other errors
      console.error(`Error setting localStorage key "${key}":`, error);
    }

  }, [key, storedValue]);  // Re-run when key or value changes

  // ==============================================
  // STEP 3: Return API matching useState
  // ==============================================

  // [value, setValue] - same as useState!
  return [storedValue, setStoredValue];
}

// ==========================================
// EXAMPLE USAGE: Todo App with Persistence
// ==========================================

function TodoAppWithPersistence() {
  // ======================
  // STATE (PERSISTED)
  // ======================

  // Todos persist across refreshes!
  const [todos, setTodos] = useLocalStorage('todos', []);

  // Input value (doesn't need to persist)
  const [inputValue, setInputValue] = useState('');

  // Filter state (persisted)
  const [filter, setFilter] = useLocalStorage('filter', 'all');

  // ======================
  // COMPUTED VALUES
  // ======================

  const filteredTodos = todos.filter(todo => {
    if (filter === 'active') return !todo.completed;
    if (filter === 'completed') return todo.completed;
    return true;  // 'all'
  });

  const stats = {
    total: todos.length,
    active: todos.filter(t => !t.completed).length,
    completed: todos.filter(t => t.completed).length
  };

  // ======================
  // EVENT HANDLERS
  // ======================

  const handleAddTodo = () => {
    if (!inputValue.trim()) return;

    const newTodo = {
      id: Date.now(),
      text: inputValue,
      completed: false,
      createdAt: new Date().toISOString()
    };

    setTodos([...todos, newTodo]);
    // ✨ Automatically saved to localStorage!

    setInputValue('');
  };

  const handleToggle = (id) => {
    setTodos(todos.map(todo =>
      todo.id === id
        ? { ...todo, completed: !todo.completed }
        : todo
    ));
    // ✨ Automatically saved to localStorage!
  };

  const handleDelete = (id) => {
    setTodos(todos.filter(todo => todo.id !== id));
    // ✨ Automatically saved to localStorage!
  };

  const handleClearCompleted = () => {
    setTodos(todos.filter(todo => !todo.completed));
  };

  // ======================
  // RENDER
  // ======================

  return (
    <div style={{
      padding: '20px',
      maxWidth: '600px',
      margin: '0 auto',
      fontFamily: 'system-ui, -apple-system, sans-serif'
    }}>
      <h1>Todos (Persisted) 💾</h1>
      <p style={{ color: '#666', marginBottom: '20px' }}>
        Refresh the page - your todos will persist!
        <br />
        Open in a new tab - same todos!
      </p>

      {/* ADD TODO INPUT */}
      <div style={{ marginBottom: '20px', display: 'flex', gap: '10px' }}>
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleAddTodo()}
          placeholder="What needs to be done?"
          style={{
            padding: '12px',
            flex: 1,
            border: '2px solid #ddd',
            borderRadius: '8px',
            fontSize: '14px'
          }}
        />
        <button
          onClick={handleAddTodo}
          style={{
            padding: '12px 24px',
            background: '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
            fontWeight: 600
          }}
        >
          Add
        </button>
      </div>

      {/* FILTER BUTTONS */}
      <div style={{ marginBottom: '20px', display: 'flex', gap: '10px' }}>
        {['all', 'active', 'completed'].map(f => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            style={{
              padding: '8px 16px',
              background: filter === f ? '#007bff' : 'white',
              color: filter === f ? 'white' : '#333',
              border: '2px solid #007bff',
              borderRadius: '6px',
              cursor: 'pointer',
              textTransform: 'capitalize',
              fontWeight: 600
            }}
          >
            {f}
          </button>
        ))}
      </div>

      {/* TODO LIST */}
      <ul style={{ listStyle: 'none', padding: 0 }}>
        {filteredTodos.map(todo => (
          <li
            key={todo.id}
            style={{
              padding: '15px',
              background: '#f9f9f9',
              marginBottom: '10px',
              borderRadius: '8px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              border: '2px solid #e0e0e0'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <input
                type="checkbox"
                checked={todo.completed}
                onChange={() => handleToggle(todo.id)}
                style={{
                  width: '20px',
                  height: '20px',
                  cursor: 'pointer'
                }}
              />
              <span
                style={{
                  textDecoration: todo.completed ? 'line-through' : 'none',
                  color: todo.completed ? '#999' : '#000',
                  fontSize: '16px'
                }}
              >
                {todo.text}
              </span>
            </div>

            <button
              onClick={() => handleDelete(todo.id)}
              style={{
                padding: '6px 12px',
                background: '#ff4444',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '14px'
              }}
            >
              Delete
            </button>
          </li>
        ))}
      </ul>

      {/* EMPTY STATE */}
      {filteredTodos.length === 0 && (
        <p style={{
          textAlign: 'center',
          color: '#999',
          padding: '40px',
          background: '#f9f9f9',
          borderRadius: '8px',
          border: '2px dashed #ddd'
        }}>
          {filter === 'all' && 'No todos yet. Add one above!'}
          {filter === 'active' && 'No active todos. Great job!'}
          {filter === 'completed' && 'No completed todos yet.'}
        </p>
      )}

      {/* STATS */}
      <div style={{
        marginTop: '20px',
        padding: '15px',
        background: '#e8f4ff',
        borderRadius: '8px',
        display: 'flex',
        justifyContent: 'space-around',
        border: '2px solid #b3d9ff'
      }}>
        <div>
          <strong>{stats.total}</strong> total
        </div>
        <div>
          <strong>{stats.active}</strong> active
        </div>
        <div>
          <strong>{stats.completed}</strong> completed
        </div>
      </div>

      {/* CLEAR COMPLETED */}
      {stats.completed > 0 && (
        <button
          onClick={handleClearCompleted}
          style={{
            marginTop: '20px',
            padding: '10px 20px',
            background: '#ff9800',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
            width: '100%',
            fontWeight: 600
          }}
        >
          Clear Completed ({stats.completed})
        </button>
      )}
    </div>
  );
}

export default TodoAppWithPersistence;
```

---

## How It Works: Step-by-Step

### Initialization Flow

```
Component Mounts
│
├─ useState(() => { ... }) called
│  │
│  ├─ Check if localStorage has key "todos"
│  │  │
│  │  ├─ YES: localStorage.getItem("todos")
│  │  │      → "[\n  { \"id\": 1, \"text\": \"Learn React\" }\n]"
│  │  │      → JSON.parse(...)
│  │  │      → [{ id: 1, text: "Learn React" }]
│  │  │      → Return parsed value
│  │  │
│  │  └─ NO: Return initialValue ([])
│  │
│  └─ State initialized with value
│
└─ Component renders with initial state
```

### Update Flow

```
User calls setTodos(newValue)
│
├─ State updates (React re-renders)
│
└─ useEffect runs (dependency: [key, storedValue])
   │
   ├─ JSON.stringify(newValue)
   │  → "[\n  { \"id\": 1, \"text\": \"Learn React\" },\n  { \"id\": 2, \"text\": \"Build App\" }\n]"
   │
   └─ localStorage.setItem("todos", stringifiedValue)
      → Persisted to browser storage!
```

---

## Advanced: Cross-Tab Synchronization

Sync state across multiple browser tabs:

```jsx
function useLocalStorage(key, initialValue) {
  const [storedValue, setStoredValue] = useState(() => {
    if (typeof window === 'undefined') return initialValue;
    try {
      const item = window.localStorage.getItem(key);
      return item ? JSON.parse(item) : initialValue;
    } catch (error) {
      console.error(error);
      return initialValue;
    }
  });

  // Save to localStorage
  useEffect(() => {
    if (typeof window === 'undefined') return;
    try {
      window.localStorage.setItem(key, JSON.stringify(storedValue));
    } catch (error) {
      console.error(error);
    }
  }, [key, storedValue]);

  // ==============================================
  // NEW: Listen for storage events from other tabs
  // ==============================================

  useEffect(() => {
    if (typeof window === 'undefined') return;

    // Handler for storage events
    const handleStorageChange = (e) => {
      // Check if this is our key
      if (e.key === key && e.newValue !== null) {
        try {
          // Update state with new value from other tab
          const newValue = JSON.parse(e.newValue);
          setStoredValue(newValue);
        } catch (error) {
          console.error(error);
        }
      }
    };

    // Listen for storage events
    // Note: Only fires when ANOTHER tab changes localStorage
    window.addEventListener('storage', handleStorageChange);

    // Cleanup
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };

  }, [key]);  // Re-run if key changes

  return [storedValue, setStoredValue];
}
```

**How it works:**
1. User changes state in Tab A
2. localStorage updated in Tab A
3. Browser fires `storage` event in Tab B
4. Tab B's useEffect catches event
5. Tab B updates state to match
6. Both tabs now in sync!

---

## Advanced: Remove/Clear Functionality

```jsx
function useLocalStorage(key, initialValue) {
  const [storedValue, setStoredValue] = useState(() => {
    // ... initialization logic
  });

  useEffect(() => {
    // ... save logic
  }, [key, storedValue]);

  // ==============================================
  // NEW: Remove value from localStorage
  // ==============================================

  const removeValue = () => {
    try {
      window.localStorage.removeItem(key);
      setStoredValue(initialValue);  // Reset to initial value
    } catch (error) {
      console.error(error);
    }
  };

  // Return [value, setValue, removeValue]
  return [storedValue, setStoredValue, removeValue];
}

// Usage:
function App() {
  const [user, setUser, removeUser] = useLocalStorage('user', null);

  const handleLogout = () => {
    removeUser();  // Clears localStorage and resets state
  };
}
```

---

## Common Mistakes & How to Avoid Them

### ❌ Mistake 1: Not Using Function Form of useState

```jsx
// BAD: Reads localStorage on EVERY render
const [value, setValue] = useState(
  JSON.parse(localStorage.getItem(key)) || initialValue
);
// Re-reads localStorage even though value hasn't changed!
```

**Fix:** Use function form (lazy initialization)

```jsx
// GOOD: Only reads ONCE on mount
const [value, setValue] = useState(() => {
  const item = localStorage.getItem(key);
  return item ? JSON.parse(item) : initialValue;
});
```

---

### ❌ Mistake 2: Forgetting try-catch

```jsx
// BAD: Will crash if localStorage is disabled or full
const item = JSON.parse(localStorage.getItem(key));
```

**Fix:** Always wrap in try-catch

```jsx
// GOOD: Handles errors gracefully
try {
  const item = JSON.parse(localStorage.getItem(key));
} catch (error) {
  console.error('localStorage error:', error);
  return initialValue;
}
```

---

### ❌ Mistake 3: Storing Functions or Circular References

```jsx
// BAD: Can't serialize functions
const [callback, setCallback] = useLocalStorage('callback', () => {});
// Error: JSON.stringify can't serialize functions

// BAD: Circular references
const obj = { name: 'John' };
obj.self = obj;
setCallback(obj);
// Error: Converting circular structure to JSON
```

**Fix:** Only store serializable data

```jsx
// GOOD: Primitive values, objects, arrays
const [user, setUser] = useLocalStorage('user', { name: 'John', age: 30 });
const [todos, setTodos] = useLocalStorage('todos', []);
const [count, setCount] = useLocalStorage('count', 0);
```

---

### ❌ Mistake 4: Not Checking SSR

```jsx
// BAD: Crashes during server-side rendering
const [value, setValue] = useState(() => {
  const item = window.localStorage.getItem(key);  // window is undefined!
});
```

**Fix:** Check for window

```jsx
// GOOD: SSR-safe
const [value, setValue] = useState(() => {
  if (typeof window === 'undefined') return initialValue;
  const item = window.localStorage.getItem(key);
  // ...
});
```

---

## Performance Considerations

### localStorage is Synchronous

```javascript
// ⚠️ WARNING: localStorage blocks main thread!
localStorage.setItem('huge-data', JSON.stringify(hugeArray));
// UI freezes until this completes

// For large data (>1MB), consider:
// 1. IndexedDB (asynchronous)
// 2. Debouncing writes
// 3. Web Workers
```

### Debouncing Writes

```jsx
function useLocalStorage(key, initialValue) {
  const [storedValue, setStoredValue] = useState(() => {
    // ... initialization
  });

  // Debounce localStorage writes
  useEffect(() => {
    const timeoutId = setTimeout(() => {
      try {
        window.localStorage.setItem(key, JSON.stringify(storedValue));
      } catch (error) {
        console.error(error);
      }
    }, 500);  // Wait 500ms after last change

    return () => clearTimeout(timeoutId);
  }, [key, storedValue]);

  return [storedValue, setStoredValue];
}
```

---

## Interviewer Q&A

### Q1: Why use a custom hook instead of directly using localStorage?

**Answer:**
1. **DRY**: Don't repeat serialization/parsing logic
2. **React integration**: Automatic re-renders on state change
3. **Type safety**: Can add TypeScript generics
4. **Error handling**: Centralized try-catch
5. **SSR support**: One check instead of many
6. **Testability**: Easier to mock

---

### Q2: What are the limitations of localStorage?

**Answer:**
1. **Storage limit**: ~5-10MB per domain
2. **Synchronous**: Blocks main thread
3. **String-only**: Must serialize objects
4. **Same-origin**: Can't share across domains
5. **Not secure**: Accessible via JavaScript (XSS risk)
6. **No expiration**: Data persists forever

**Alternatives:**
- **sessionStorage**: Clears on tab close
- **IndexedDB**: Larger storage, asynchronous
- **Cookies**: Can set expiration, sent with requests
- **Server**: Most secure, accessible across devices

---

### Q3: How would you add TypeScript support?

**Answer:**
```typescript
function useLocalStorage<T>(
  key: string,
  initialValue: T
): [T, (value: T) => void] {
  const [storedValue, setStoredValue] = useState<T>(() => {
    if (typeof window === 'undefined') return initialValue;
    try {
      const item = window.localStorage.getItem(key);
      return item ? (JSON.parse(item) as T) : initialValue;
    } catch (error) {
      console.error(error);
      return initialValue;
    }
  });

  useEffect(() => {
    if (typeof window === 'undefined') return;
    try {
      window.localStorage.setItem(key, JSON.stringify(storedValue));
    } catch (error) {
      console.error(error);
    }
  }, [key, storedValue]);

  return [storedValue, setStoredValue];
}

// Usage with type inference
const [user, setUser] = useLocalStorage<User>('user', { name: '', age: 0 });
```

---

### Q4: How would you handle migration/versioning of stored data?

**Answer:**
```jsx
function useLocalStorage(key, initialValue, version = 1) {
  const [storedValue, setStoredValue] = useState(() => {
    try {
      const item = window.localStorage.getItem(key);
      if (!item) return initialValue;

      const parsed = JSON.parse(item);

      // Check version
      if (parsed._version !== version) {
        // Migration logic
        const migrated = migrateData(parsed, parsed._version, version);
        return migrated;
      }

      return parsed.data;
    } catch {
      return initialValue;
    }
  });

  useEffect(() => {
    const dataToStore = {
      _version: version,
      data: storedValue
    };
    localStorage.setItem(key, JSON.stringify(dataToStore));
  }, [key, storedValue, version]);

  return [storedValue, setStoredValue];
}
```

---

### Q5: How to handle localStorage quota exceeded?

**Answer:**
```jsx
const setValue = (value) => {
  try {
    window.localStorage.setItem(key, JSON.stringify(value));
    setStoredValue(value);
  } catch (error) {
    if (error.name === 'QuotaExceededError') {
      // Storage full! Handle gracefully:

      // Option 1: Clear old data
      clearOldData();

      // Option 2: Notify user
      alert('Storage full. Some data may not be saved.');

      // Option 3: Fall back to memory-only storage
      setStoredValue(value);  // Update state only
    }
  }
};
```

---

## Key Takeaways

### Essential Concepts

1. **Custom Hooks Extract Logic**
   - Reusable across components
   - Same API as built-in hooks
   - Cleaner component code

2. **localStorage + React State**
   - Read on mount (useState initializer)
   - Write on change (useEffect)
   - Automatic serialization/parsing

3. **Error Handling is Critical**
   - localStorage can be disabled
   - Quota can be exceeded
   - JSON parsing can fail
   - Always use try-catch

4. **SSR Compatibility**
   - Check for window object
   - Return initialValue on server
   - Use lazy initialization

5. **Cross-Tab Sync**
   - storage event fires in other tabs
   - Listen and update state
   - Keeps all tabs in sync

### When to Use useLocalStorage

✅ **Good for:**
- User preferences (theme, language)
- Form drafts
- Shopping cart
- Recently viewed
- Simple app state

❌ **Not good for:**
- Sensitive data (passwords, tokens)
- Large datasets (>1MB)
- Real-time sync needs
- Server-side rendering (use with caution)

---

## Practice Exercises

1. **Basic**: Implement useLocalStorage hook
2. **Intermediate**: Add cross-tab synchronization
3. **Advanced**: Add data migration/versioning
4. **Expert**: Build useSessionStorage and useIndexedDB

---

## Final Tips

1. **Test in private browsing** - localStorage might be disabled
2. **Handle quota errors** - storage can fill up
3. **Consider alternatives** - IndexedDB for large data
4. **Don't store secrets** - localStorage is not secure
5. **Use libraries in production** - usehooks-ts, react-use

**Remember:** Custom hooks are about creating reusable, composable logic. This is a fundamental React pattern!

---

Good luck! Custom hooks are essential knowledge for any React developer! 🪝
