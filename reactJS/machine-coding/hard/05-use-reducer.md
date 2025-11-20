# Complex State with useReducer

**Difficulty:** Hard
**Time:** 60-90 minutes
**Prerequisites:** useState, state management concepts, Redux basics (helpful)

---

## Problem Statement

Manage complex application state using `useReducer`. When your component has multiple related state values or complex update logic, `useReducer` provides a more predictable and maintainable alternative to `useState`.

### The Problem with useState

```jsx
// ❌ Complex state with useState becomes messy
function TodoApp() {
  const [todos, setTodos] = useState([]);
  const [filter, setFilter] = useState('all');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState('date');

  // Complex update logic scattered everywhere
  const addTodo = (text) => {
    setLoading(true);
    setError(null);
    // ... multiple state updates
    setTodos(prev => [...prev, newTodo]);
    setLoading(false);
  };

  // More complex logic with multiple state updates
  const deleteTodo = (id) => {
    setTodos(prev => prev.filter(t => t.id !== id));
    if (todos.length === 1) {
      setFilter('all');  // Reset filter if last todo deleted
    }
  };

  // State updates are spread across component
  // Hard to track what changes when
}
```

### The Solution with useReducer

```jsx
// ✅ Centralized state management with useReducer
function TodoApp() {
  const [state, dispatch] = useReducer(todoReducer, initialState);

  // Clean action dispatching
  const addTodo = (text) => {
    dispatch({ type: 'ADD_TODO', payload: text });
  };

  const deleteTodo = (id) => {
    dispatch({ type: 'DELETE_TODO', payload: id });
  };

  // All state logic in one place (reducer)
  // Predictable updates
  // Easy to test
}
```

**When to Use useReducer:**
- Multiple related state values
- Complex update logic
- State depends on previous state
- Deep component trees (with useContext)
- Testing is important
- Predictability matters

---

## Requirements

### Must Have
- ✅ Define reducer function
- ✅ Handle multiple action types
- ✅ Immutable state updates
- ✅ Return new state from reducer
- ✅ Dispatch actions to update state

### Nice to Have
- ⭐ Action creators (helper functions)
- ⭐ TypeScript types for actions
- ⭐ Middleware (logging, async)
- ⭐ Combine with useContext
- ⭐ State persistence

---

## Reducer Pattern Explained

### What is a Reducer?

```javascript
// A reducer is a pure function that:
// 1. Takes current state and an action
// 2. Returns new state
// 3. Does NOT mutate original state

function reducer(state, action) {
  // state = current state
  // action = { type: 'ACTION_NAME', payload: data }

  switch (action.type) {
    case 'ACTION_NAME':
      return { ...state, updated: true };  // New state object
    default:
      return state;  // Return unchanged state
  }
}

// PURE FUNCTION:
// - Same inputs → same output
// - No side effects
// - No mutations
```

### useState vs useReducer

```jsx
// ==========================================
// useState: Simple state
// ==========================================
const [count, setCount] = useState(0);

setCount(count + 1);        // Direct update
setCount(prev => prev + 1); // Function update

// USE WHEN:
// - Simple state (strings, numbers, booleans)
// - Independent state values
// - Simple update logic

// ==========================================
// useReducer: Complex state
// ==========================================
const [state, dispatch] = useReducer(reducer, {
  count: 0,
  name: '',
  todos: [],
  loading: false
});

dispatch({ type: 'INCREMENT' });
dispatch({ type: 'SET_NAME', payload: 'John' });

// USE WHEN:
// - Complex state object
// - Multiple related values
// - Complex update logic
// - State depends on previous state
// - Need predictable updates
```

### Visual Comparison

```
┌─────────────────────────────────────────┐
│           useState Flow                 │
├─────────────────────────────────────────┤
│                                         │
│   Component                             │
│      │                                  │
│      └─> setState(newValue)            │
│             │                           │
│             └─> State updated           │
│                    │                    │
│                    └─> Re-render        │
│                                         │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│          useReducer Flow                │
├─────────────────────────────────────────┤
│                                         │
│   Component                             │
│      │                                  │
│      └─> dispatch(action)              │
│             │                           │
│             └─> Reducer(state, action)  │
│                    │                    │
│                    └─> New state        │
│                           │             │
│                           └─> Re-render │
│                                         │
└─────────────────────────────────────────┘
```

---

## Complete Solution

```jsx
import { useReducer } from 'react';

// ==========================================
// STEP 1: Define Initial State
// ==========================================

const initialState = {
  todos: [],
  filter: 'all',        // 'all', 'active', 'completed'
  loading: false,
  error: null,
  lastAction: null      // For debugging
};

// ==========================================
// STEP 2: Define Action Types (Constants)
// ==========================================

// Why constants?
// - Prevents typos
// - Autocomplete in IDE
// - Easy to refactor
const ACTIONS = {
  ADD_TODO: 'ADD_TODO',
  TOGGLE_TODO: 'TOGGLE_TODO',
  DELETE_TODO: 'DELETE_TODO',
  EDIT_TODO: 'EDIT_TODO',
  SET_FILTER: 'SET_FILTER',
  CLEAR_COMPLETED: 'CLEAR_COMPLETED',
  SET_LOADING: 'SET_LOADING',
  SET_ERROR: 'SET_ERROR',
  TOGGLE_ALL: 'TOGGLE_ALL'
};

// ==========================================
// STEP 3: Define Reducer Function
// ==========================================

/**
 * Todo Reducer
 *
 * Pure function that takes state + action, returns new state
 *
 * @param {Object} state - Current state
 * @param {Object} action - { type: string, payload: any }
 * @returns {Object} New state
 */
function todoReducer(state, action) {
  // Log for debugging (remove in production)
  console.log('Action:', action.type, 'Payload:', action.payload);

  switch (action.type) {
    // =====================================
    // ADD_TODO: Add new todo to list
    // =====================================
    case ACTIONS.ADD_TODO:
      return {
        ...state,                           // Copy existing state
        todos: [
          ...state.todos,                   // Copy existing todos
          {
            id: Date.now(),                 // Simple ID generation
            text: action.payload,           // Todo text from action
            completed: false,
            createdAt: new Date().toISOString()
          }
        ],
        lastAction: 'Added todo'
      };

    // =====================================
    // TOGGLE_TODO: Toggle completed status
    // =====================================
    case ACTIONS.TOGGLE_TODO:
      return {
        ...state,
        todos: state.todos.map(todo =>
          todo.id === action.payload
            ? { ...todo, completed: !todo.completed }  // Toggle this one
            : todo                                      // Keep others unchanged
        ),
        lastAction: 'Toggled todo'
      };

    // =====================================
    // DELETE_TODO: Remove todo from list
    // =====================================
    case ACTIONS.DELETE_TODO:
      return {
        ...state,
        todos: state.todos.filter(todo => todo.id !== action.payload),
        lastAction: 'Deleted todo'
      };

    // =====================================
    // EDIT_TODO: Update todo text
    // =====================================
    case ACTIONS.EDIT_TODO:
      return {
        ...state,
        todos: state.todos.map(todo =>
          todo.id === action.payload.id
            ? { ...todo, text: action.payload.text }
            : todo
        ),
        lastAction: 'Edited todo'
      };

    // =====================================
    // SET_FILTER: Change active filter
    // =====================================
    case ACTIONS.SET_FILTER:
      return {
        ...state,
        filter: action.payload,  // 'all', 'active', or 'completed'
        lastAction: `Set filter to ${action.payload}`
      };

    // =====================================
    // CLEAR_COMPLETED: Remove all completed todos
    // =====================================
    case ACTIONS.CLEAR_COMPLETED:
      return {
        ...state,
        todos: state.todos.filter(todo => !todo.completed),
        lastAction: 'Cleared completed todos'
      };

    // =====================================
    // TOGGLE_ALL: Mark all as complete/incomplete
    // =====================================
    case ACTIONS.TOGGLE_ALL:
      const allCompleted = state.todos.every(todo => todo.completed);
      return {
        ...state,
        todos: state.todos.map(todo => ({
          ...todo,
          completed: !allCompleted  // If all complete, uncomplete; else complete
        })),
        lastAction: 'Toggled all todos'
      };

    // =====================================
    // SET_LOADING: Update loading state
    // =====================================
    case ACTIONS.SET_LOADING:
      return {
        ...state,
        loading: action.payload,
        lastAction: 'Set loading'
      };

    // =====================================
    // SET_ERROR: Update error state
    // =====================================
    case ACTIONS.SET_ERROR:
      return {
        ...state,
        error: action.payload,
        loading: false,  // Stop loading on error
        lastAction: 'Set error'
      };

    // =====================================
    // DEFAULT: Unknown action
    // =====================================
    default:
      // Important! Return current state for unknown actions
      // Prevents state from becoming undefined
      console.warn(`Unknown action type: ${action.type}`);
      return state;
  }
}

// ==========================================
// STEP 4: Main Component
// ==========================================

function TodoAppWithReducer() {
  // ======================
  // INITIALIZE REDUCER
  // ======================

  // useReducer(reducer, initialState)
  // Returns [state, dispatch]
  const [state, dispatch] = useReducer(todoReducer, initialState);

  // Destructure state for easier access
  const { todos, filter, loading, error, lastAction } = state;

  // ======================
  // LOCAL UI STATE
  // ======================

  // Input value doesn't need to be in reducer
  // (not related to todos state, just UI)
  const [inputValue, setInputValue] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [editingText, setEditingText] = useState('');

  // ======================
  // COMPUTED VALUES
  // ======================

  // Filter todos based on current filter
  const getFilteredTodos = () => {
    switch (filter) {
      case 'active':
        return todos.filter(todo => !todo.completed);
      case 'completed':
        return todos.filter(todo => todo.completed);
      default:
        return todos;
    }
  };

  const filteredTodos = getFilteredTodos();

  // Calculate stats
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

    // Dispatch ADD_TODO action
    dispatch({
      type: ACTIONS.ADD_TODO,
      payload: inputValue
    });

    setInputValue('');
  };

  const handleToggleTodo = (id) => {
    dispatch({
      type: ACTIONS.TOGGLE_TODO,
      payload: id
    });
  };

  const handleDeleteTodo = (id) => {
    dispatch({
      type: ACTIONS.DELETE_TODO,
      payload: id
    });
  };

  const handleEditTodo = (id) => {
    setEditingId(id);
    const todo = todos.find(t => t.id === id);
    setEditingText(todo.text);
  };

  const handleSaveEdit = () => {
    if (!editingText.trim()) return;

    dispatch({
      type: ACTIONS.EDIT_TODO,
      payload: { id: editingId, text: editingText }
    });

    setEditingId(null);
    setEditingText('');
  };

  const handleSetFilter = (newFilter) => {
    dispatch({
      type: ACTIONS.SET_FILTER,
      payload: newFilter
    });
  };

  const handleClearCompleted = () => {
    dispatch({
      type: ACTIONS.CLEAR_COMPLETED
    });
  };

  const handleToggleAll = () => {
    dispatch({
      type: ACTIONS.TOGGLE_ALL
    });
  };

  // ======================
  // RENDER
  // ======================

  return (
    <div style={{
      padding: '20px',
      maxWidth: '700px',
      margin: '0 auto',
      fontFamily: 'system-ui, -apple-system, sans-serif'
    }}>
      <h1>Todo App (useReducer) ⚙️</h1>
      <p style={{ color: '#666', marginBottom: '20px' }}>
        Complex state management with useReducer
      </p>

      {/* ERROR MESSAGE */}
      {error && (
        <div style={{
          padding: '15px',
          background: '#ffebee',
          color: '#c62828',
          borderRadius: '8px',
          marginBottom: '20px',
          border: '2px solid #ef5350'
        }}>
          ❌ Error: {error}
        </div>
      )}

      {/* ADD TODO INPUT */}
      <div style={{
        marginBottom: '20px',
        display: 'flex',
        gap: '10px'
      }}>
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleAddTodo()}
          placeholder="What needs to be done?"
          disabled={loading}
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
          disabled={loading}
          style={{
            padding: '12px 24px',
            background: '#28a745',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
            fontWeight: 600
          }}
        >
          {loading ? 'Adding...' : 'Add'}
        </button>
      </div>

      {/* FILTER BUTTONS */}
      <div style={{
        marginBottom: '20px',
        display: 'flex',
        gap: '10px',
        flexWrap: 'wrap'
      }}>
        {['all', 'active', 'completed'].map(f => (
          <button
            key={f}
            onClick={() => handleSetFilter(f)}
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

        {todos.length > 0 && (
          <button
            onClick={handleToggleAll}
            style={{
              padding: '8px 16px',
              background: '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer',
              fontWeight: 600
            }}
          >
            Toggle All
          </button>
        )}

        {stats.completed > 0 && (
          <button
            onClick={handleClearCompleted}
            style={{
              padding: '8px 16px',
              background: '#dc3545',
              color: 'white',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer',
              fontWeight: 600
            }}
          >
            Clear Completed
          </button>
        )}
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
            {editingId === todo.id ? (
              // EDIT MODE
              <div style={{ display: 'flex', gap: '10px', flex: 1 }}>
                <input
                  type="text"
                  value={editingText}
                  onChange={(e) => setEditingText(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSaveEdit()}
                  style={{
                    padding: '8px',
                    flex: 1,
                    border: '2px solid #007bff',
                    borderRadius: '5px'
                  }}
                  autoFocus
                />
                <button
                  onClick={handleSaveEdit}
                  style={{
                    padding: '8px 16px',
                    background: '#28a745',
                    color: 'white',
                    border: 'none',
                    borderRadius: '5px',
                    cursor: 'pointer'
                  }}
                >
                  Save
                </button>
                <button
                  onClick={() => setEditingId(null)}
                  style={{
                    padding: '8px 16px',
                    background: '#6c757d',
                    color: 'white',
                    border: 'none',
                    borderRadius: '5px',
                    cursor: 'pointer'
                  }}
                >
                  Cancel
                </button>
              </div>
            ) : (
              // VIEW MODE
              <>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flex: 1 }}>
                  <input
                    type="checkbox"
                    checked={todo.completed}
                    onChange={() => handleToggleTodo(todo.id)}
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
                      fontSize: '16px',
                      flex: 1
                    }}
                  >
                    {todo.text}
                  </span>
                </div>

                <div style={{ display: 'flex', gap: '10px' }}>
                  <button
                    onClick={() => handleEditTodo(todo.id)}
                    style={{
                      padding: '6px 12px',
                      background: '#ffc107',
                      color: '#000',
                      border: 'none',
                      borderRadius: '5px',
                      cursor: 'pointer',
                      fontSize: '14px'
                    }}
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => handleDeleteTodo(todo.id)}
                    style={{
                      padding: '6px 12px',
                      background: '#dc3545',
                      color: 'white',
                      border: 'none',
                      borderRadius: '5px',
                      cursor: 'pointer',
                      fontSize: '14px'
                    }}
                  >
                    Delete
                  </button>
                </div>
              </>
            )}
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
        display: 'grid',
        gridTemplateColumns: 'repeat(3, 1fr)',
        gap: '10px',
        border: '2px solid #b3d9ff'
      }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#007bff' }}>
            {stats.total}
          </div>
          <div style={{ fontSize: '12px', color: '#666' }}>Total</div>
        </div>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#ffc107' }}>
            {stats.active}
          </div>
          <div style={{ fontSize: '12px', color: '#666' }}>Active</div>
        </div>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#28a745' }}>
            {stats.completed}
          </div>
          <div style={{ fontSize: '12px', color: '#666' }}>Completed</div>
        </div>
      </div>

      {/* DEBUG INFO */}
      <div style={{
        marginTop: '20px',
        padding: '15px',
        background: '#f5f5f5',
        borderRadius: '8px',
        fontSize: '13px',
        fontFamily: 'monospace'
      }}>
        <h3 style={{ marginTop: 0 }}>Debug Info</h3>
        <div><strong>Last Action:</strong> {lastAction || 'None'}</div>
        <div><strong>Current Filter:</strong> {filter}</div>
        <div><strong>Loading:</strong> {loading ? 'Yes' : 'No'}</div>
        <div><strong>Error:</strong> {error || 'None'}</div>
      </div>
    </div>
  );
}

export default TodoAppWithReducer;
```

---

## How It Works: Action Flow

### Example: Adding a Todo

```
USER INTERACTION
│
├─ User types "Learn React"
│  └─ setInputValue("Learn React")
│
├─ User clicks "Add" button
│  └─ handleAddTodo() called
│     │
│     └─ dispatch({
│          type: 'ADD_TODO',
│          payload: "Learn React"
│        })
│
ACTION DISPATCHED
│
├─ todoReducer(state, action) called
│  │
│  ├─ switch (action.type)
│  │  └─ case 'ADD_TODO':
│  │
│  ├─ Create new todo object:
│  │  {
│  │    id: 1637123456789,
│  │    text: "Learn React",
│  │    completed: false,
│  │    createdAt: "2023-11-20T10:30:00.000Z"
│  │  }
│  │
│  └─ Return new state:
│     {
│       ...state,
│       todos: [...state.todos, newTodo]
│     }
│
NEW STATE RETURNED
│
└─ React re-renders component with new state
```

---

## Advanced: Async Actions with useReducer

```jsx
// Reducer handles loading/error states
function dataReducer(state, action) {
  switch (action.type) {
    case 'FETCH_START':
      return { ...state, loading: true, error: null };

    case 'FETCH_SUCCESS':
      return {
        ...state,
        loading: false,
        data: action.payload
      };

    case 'FETCH_ERROR':
      return {
        ...state,
        loading: false,
        error: action.payload
      };

    default:
      return state;
  }
}

function DataFetcher() {
  const [state, dispatch] = useReducer(dataReducer, {
    data: null,
    loading: false,
    error: null
  });

  const fetchData = async () => {
    dispatch({ type: 'FETCH_START' });

    try {
      const response = await fetch('/api/data');
      const data = await response.json();
      dispatch({ type: 'FETCH_SUCCESS', payload: data });
    } catch (error) {
      dispatch({ type: 'FETCH_ERROR', payload: error.message });
    }
  };

  return (
    <div>
      {state.loading && <p>Loading...</p>}
      {state.error && <p>Error: {state.error}</p>}
      {state.data && <pre>{JSON.stringify(state.data, null, 2)}</pre>}
      <button onClick={fetchData}>Fetch Data</button>
    </div>
  );
}
```

---

## Advanced: useReducer + useContext

Combine for global state management (like mini-Redux):

```jsx
// Create context
const TodoContext = React.createContext();

// Provider component
function TodoProvider({ children }) {
  const [state, dispatch] = useReducer(todoReducer, initialState);

  return (
    <TodoContext.Provider value={{ state, dispatch }}>
      {children}
    </TodoContext.Provider>
  );
}

// Custom hook for easy access
function useTodos() {
  const context = React.useContext(TodoContext);
  if (!context) {
    throw new Error('useTodos must be used within TodoProvider');
  }
  return context;
}

// Usage in any component
function TodoList() {
  const { state, dispatch } = useTodos();

  return (
    <div>
      {state.todos.map(todo => (
        <div key={todo.id}>
          {todo.text}
          <button onClick={() => dispatch({ type: 'DELETE_TODO', payload: todo.id })}>
            Delete
          </button>
        </div>
      ))}
    </div>
  );
}

// App component
function App() {
  return (
    <TodoProvider>
      <TodoList />
      <AddTodo />
      <FilterButtons />
    </TodoProvider>
  );
}
```

---

## Common Mistakes & How to Avoid Them

### ❌ Mistake 1: Mutating State

```jsx
// BAD: Mutating state directly
function todoReducer(state, action) {
  switch (action.type) {
    case 'ADD_TODO':
      state.todos.push(action.payload);  // ❌ Mutation!
      return state;
  }
}
```

**Fix:** Always return new objects/arrays

```jsx
// GOOD: Immutable update
function todoReducer(state, action) {
  switch (action.type) {
    case 'ADD_TODO':
      return {
        ...state,
        todos: [...state.todos, action.payload]  // New array
      };
  }
}
```

---

### ❌ Mistake 2: Forgetting Default Case

```jsx
// BAD: No default case
function reducer(state, action) {
  switch (action.type) {
    case 'INCREMENT':
      return { count: state.count + 1 };
    // Missing default!
  }
}
// Returns undefined for unknown actions!
```

**Fix:** Always have default case

```jsx
// GOOD: With default
function reducer(state, action) {
  switch (action.type) {
    case 'INCREMENT':
      return { count: state.count + 1 };
    default:
      return state;  // ← Essential!
  }
}
```

---

### ❌ Mistake 3: Side Effects in Reducer

```jsx
// BAD: Side effects in reducer
function reducer(state, action) {
  switch (action.type) {
    case 'ADD_TODO':
      localStorage.setItem('todos', JSON.stringify(state.todos));  // ❌ Side effect!
      return { ...state, todos: [...state.todos, action.payload] };
  }
}
```

**Fix:** Side effects in useEffect

```jsx
// GOOD: Side effects outside reducer
function TodoApp() {
  const [state, dispatch] = useReducer(reducer, initialState);

  useEffect(() => {
    localStorage.setItem('todos', JSON.stringify(state.todos));
  }, [state.todos]);
}
```

---

### ❌ Mistake 4: Complex Logic in Component

```jsx
// BAD: Complex logic in component
const handleAddTodo = (text) => {
  const newTodo = {
    id: Date.now(),
    text: text,
    completed: false,
    priority: text.includes('!') ? 'high' : 'normal',  // Logic in component
    tags: text.match(/#\w+/g) || []                   // Logic in component
  };
  dispatch({ type: 'ADD_TODO', payload: newTodo });
};
```

**Fix:** Move logic to reducer

```jsx
// GOOD: Logic in reducer
function reducer(state, action) {
  case 'ADD_TODO':
    const text = action.payload;
    const newTodo = {
      id: Date.now(),
      text: text,
      completed: false,
      priority: text.includes('!') ? 'high' : 'normal',
      tags: text.match(/#\w+/g) || []
    };
    return { ...state, todos: [...state.todos, newTodo] };
}

// Component just dispatches
const handleAddTodo = (text) => {
  dispatch({ type: 'ADD_TODO', payload: text });
};
```

---

## Performance Considerations

### useMemo with Filtered Data

```jsx
// Expensive computation
const filteredTodos = useMemo(() => {
  return todos.filter(todo => {
    // Complex filtering logic
    if (filter === 'active') return !todo.completed;
    if (filter === 'completed') return todo.completed;
    return true;
  });
}, [todos, filter]);  // Only recalculate when these change
```

### Split Reducers

```jsx
// Too much in one reducer
const megaReducer = (state, action) => {
  // Handles todos, user, settings, notifications...
  // Gets complex fast!
};

// Better: Split into multiple reducers
const todosReducer = (state, action) => { /* ... */ };
const userReducer = (state, action) => { /* ... */ };
const settingsReducer = (state, action) => { /* ... */ };

function App() {
  const [todos, dispatchTodos] = useReducer(todosReducer, []);
  const [user, dispatchUser] = useReducer(userReducer, null);
  const [settings, dispatchSettings] = useReducer(settingsReducer, {});
}
```

---

## Interviewer Q&A

### Q1: When should I use useReducer vs useState?

**Answer:**

**Use useState when:**
- Simple, independent state values
- State updates are straightforward
- Component is small and simple

**Use useReducer when:**
- Complex state object with multiple properties
- Multiple related state values
- Complex update logic
- State depends on previous state
- Need predictable state transitions
- Want to centralize state logic
- Testing is important

**Example:**
```jsx
// useState is fine
const [isOpen, setIsOpen] = useState(false);

// useReducer is better
const [state, dispatch] = useReducer(reducer, {
  user: null,
  todos: [],
  filter: 'all',
  loading: false,
  error: null
});
```

---

### Q2: How is useReducer different from Redux?

**Answer:**

**Similarities:**
- Both use reducers
- Both dispatch actions
- Both provide predictable state updates

**Differences:**

| Feature | useReducer | Redux |
|---------|-----------|-------|
| **Scope** | Component-level | Global |
| **Setup** | Built-in hook | External library |
| **DevTools** | No | Yes (time-travel) |
| **Middleware** | No | Yes (thunk, saga) |
| **Learning curve** | Simple | Steeper |
| **Bundle size** | 0kb | ~2-3kb |

**When to use each:**
- **useReducer**: Single component or small app
- **Redux**: Large app, need DevTools, middleware

---

### Q3: Can you have multiple reducers in one component?

**Answer:**
Yes! Each handles different slice of state:

```jsx
function App() {
  const [todos, dispatchTodos] = useReducer(todoReducer, []);
  const [user, dispatchUser] = useReducer(userReducer, null);
  const [ui, dispatchUI] = useReducer(uiReducer, { theme: 'light' });

  // Each reducer manages its own state
}
```

Or combine them:

```jsx
function combineReducers(reducers) {
  return (state, action) => {
    return Object.keys(reducers).reduce((nextState, key) => {
      nextState[key] = reducers[key](state[key], action);
      return nextState;
    }, {});
  };
}

const rootReducer = combineReducers({
  todos: todoReducer,
  user: userReducer,
  ui: uiReducer
});
```

---

### Q4: How would you add middleware (like logging)?

**Answer:**
```jsx
function useReducerWithMiddleware(reducer, initialState, middleware) {
  const [state, dispatch] = useReducer(reducer, initialState);

  const dispatchWithMiddleware = (action) => {
    // Run middleware before dispatch
    middleware.forEach(fn => fn(state, action));
    dispatch(action);
  };

  return [state, dispatchWithMiddleware];
}

// Logging middleware
const logger = (state, action) => {
  console.group(action.type);
  console.log('Previous State:', state);
  console.log('Action:', action);
  console.groupEnd();
};

// Usage
const [state, dispatch] = useReducerWithMiddleware(
  reducer,
  initialState,
  [logger]
);
```

---

### Q5: How to handle async actions with useReducer?

**Answer:**
useReducer is synchronous, but you can dispatch before/after async operations:

```jsx
const fetchTodos = async () => {
  dispatch({ type: 'FETCH_START' });

  try {
    const response = await fetch('/api/todos');
    const data = await response.json();
    dispatch({ type: 'FETCH_SUCCESS', payload: data });
  } catch (error) {
    dispatch({ type: 'FETCH_ERROR', payload: error.message });
  }
};
```

Or create action creators:

```jsx
const actionCreators = {
  fetchTodos: (dispatch) => async () => {
    dispatch({ type: 'FETCH_START' });
    try {
      const data = await fetchTodosAPI();
      dispatch({ type: 'FETCH_SUCCESS', payload: data });
    } catch (error) {
      dispatch({ type: 'FETCH_ERROR', payload: error });
    }
  }
};
```

---

## Key Takeaways

### Essential Concepts

1. **Reducer is a Pure Function**
   - Takes (state, action) → returns new state
   - No mutations
   - No side effects
   - Predictable

2. **Actions Describe What Happened**
   - `{ type: 'ACTION_TYPE', payload: data }`
   - Type describes the event
   - Payload contains data
   - Use constants for types

3. **Centralized State Logic**
   - All updates in one place (reducer)
   - Easier to debug
   - Easier to test
   - More maintainable

4. **Immutable Updates**
   - Always return new objects/arrays
   - Use spread operator
   - Never mutate state directly
   - React detects changes

5. **Default Case is Essential**
   - Returns current state
   - Prevents undefined state
   - Handles unknown actions
   - Good practice

### useState vs useReducer Decision Tree

```
Is state a simple value (string, number, boolean)?
├─ YES → Use useState
└─ NO → Continue

Does state have multiple related properties?
├─ YES → Use useReducer
└─ NO → Continue

Is update logic complex or conditional?
├─ YES → Use useReducer
└─ NO → Use useState

Do multiple actions update the same state?
├─ YES → Use useReducer
└─ NO → Use useState
```

---

## Practice Exercises

1. **Basic**: Build counter with useReducer (increment, decrement, reset)
2. **Intermediate**: Build todo app with useReducer
3. **Advanced**: Add undo/redo functionality
4. **Expert**: Build shopping cart with useReducer + useContext

---

## Final Tips

1. **Start with useState** - only switch to useReducer when needed
2. **Use constants for action types** - prevents typos
3. **Keep reducers pure** - no side effects
4. **Always return state in default** - prevents undefined
5. **Test reducers independently** - they're just functions!

**Remember:** useReducer is about managing complex state in a predictable way. It's not always needed, but when it is, it makes your code much cleaner!

---

Good luck! useReducer is an advanced pattern that shows you understand state management at a deep level! ⚙️
