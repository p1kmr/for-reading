# ReactJS Frontend Machine Coding Interview Guide - Complete Approach

You are a React expert and frontend interview coach. This guide will help you master ReactJS machine coding interviews through structured learning, detailed explanations, and hands-on practice.

**Focus:** Build functional, interactive UI components with proper React patterns. CSS styling is minimal - concentrate on functionality first.

---

## 📋 Table of Contents

1. [Understanding Machine Coding Interviews](#understanding-machine-coding-interviews)
2. [Core React Concepts to Master](#core-react-concepts-to-master)
3. [Interview Approach Framework](#interview-approach-framework)
4. [Common Patterns & Best Practices](#common-patterns--best-practices)
5. [Hooks Deep Dive](#hooks-deep-dive)
6. [Debugging & Testing Mindset](#debugging--testing-mindset)
7. [Time Management Strategy](#time-management-strategy)
8. [Common Mistakes & Solutions](#common-mistakes--solutions)
9. [Interview Evaluation Criteria](#interview-evaluation-criteria)
10. [Question Directory](#question-directory)

---

## 1. Understanding Machine Coding Interviews

### What is Machine Coding?

Machine coding rounds test your ability to:
- **Build working UI components** from scratch in 60-90 minutes
- **Write clean, readable code** with proper React patterns
- **Handle state management** effectively
- **Think through edge cases** and user interactions
- **Explain your decisions** while coding

### What Interviewers Look For:

✅ **Functionality:** Does the component work as expected?
✅ **Code Quality:** Clean, readable, well-structured code
✅ **React Best Practices:** Proper use of hooks, components, props
✅ **Problem-Solving:** How you approach and break down problems
✅ **Communication:** Explaining your thought process
✅ **Edge Cases:** Handling errors, empty states, loading states

❌ **NOT Evaluated (usually):**
- Perfect CSS styling (basic styling is enough)
- Complex animations
- Backend integration (unless specified)

---

## 2. Core React Concepts to Master

### 2.1 Component Architecture

```
┌─────────────────────────────────┐
│       Parent Component          │
│  (Manages state & logic)        │
│                                 │
│  ┌──────────┐  ┌─────────────┐ │
│  │  Child 1 │  │   Child 2   │ │
│  │  (UI)    │  │   (UI)      │ │
│  └──────────┘  └─────────────┘ │
└─────────────────────────────────┘
```

**Key Principle:**
- **Lift state up** to the nearest common ancestor
- **Pass data down** via props
- **Pass callbacks up** to handle events

### 2.2 State Management Hierarchy

```
Local Component State (useState)
         ↓
    Shared State (lifting state up)
         ↓
    Context API (for deeply nested props)
         ↓
    External State Management (Redux, Zustand)
```

**Rule of Thumb:** Start with the simplest solution (useState), escalate only when needed.

---

## 3. Interview Approach Framework

### Step 1: Clarify Requirements (5 minutes)

**Ask These Questions:**

```
1. What are the core features required?
2. What user interactions should be supported?
3. Should I handle edge cases (empty states, errors)?
4. Is there any specific behavior for [feature]?
5. Do I need to persist data (localStorage)?
6. What should happen when [user action]?
```

**Example for Todo List:**
```
✅ Can I assume todos are just strings or should they be objects?
✅ Should completed todos be visually different?
✅ Do we need edit functionality or just add/delete?
✅ Should todos persist on page refresh?
```

### Step 2: Plan Architecture (5-10 minutes)

Draw a quick component structure:

```
TodoApp (parent)
  ├── TodoInput (add new todos)
  ├── TodoList
  │     └── TodoItem (individual todo)
  │           ├── checkbox (mark complete)
  │           └── delete button
  └── TodoStats (show counts)
```

**Identify State:**
- What data changes? → State
- What stays constant? → Props or constants

### Step 3: Start with Basic Structure (10 minutes)

```jsx
// 1. Create component skeleton
function TodoApp() {
  return <div>Todo App</div>
}

// 2. Add state
const [todos, setTodos] = useState([]);

// 3. Add basic JSX structure
return (
  <div className="todo-app">
    <h1>My Todos</h1>
    {/* Add components here */}
  </div>
);
```

### Step 4: Implement Core Functionality (30-40 minutes)

**Priority Order:**
1. ✅ Display data (render list)
2. ✅ Add new items
3. ✅ Basic interactions (click, toggle)
4. ✅ Delete/Remove
5. ⭐ Nice-to-have features (edit, filter, etc.)

### Step 5: Handle Edge Cases (10 minutes)

```jsx
// Empty state
{todos.length === 0 && <p>No todos yet. Add one above!</p>}

// Input validation
if (!inputValue.trim()) return; // Don't add empty todos

// Error boundaries (if time permits)
```

### Step 6: Test & Explain (5-10 minutes)

- Walk through the code
- Demonstrate functionality
- Explain key decisions
- Discuss potential improvements

---

## 4. Common Patterns & Best Practices

### Pattern 1: Controlled Components

```jsx
// ❌ Uncontrolled (avoid in interviews)
<input ref={inputRef} />

// ✅ Controlled (preferred)
const [value, setValue] = useState('');
<input value={value} onChange={(e) => setValue(e.target.value)} />
```

**Why?** React controls the input value, making it predictable and testable.

### Pattern 2: Event Handlers

```jsx
// ✅ Good: Clear, reusable handler
const handleAddTodo = () => {
  if (!inputValue.trim()) return;
  setTodos([...todos, { id: Date.now(), text: inputValue, completed: false }]);
  setInputValue('');
};

// ❌ Avoid: Inline logic in JSX (hard to read)
<button onClick={() => {
  if (!inputValue.trim()) return;
  setTodos([...todos, ...]);
}}>Add</button>
```

### Pattern 3: Immutable State Updates

```jsx
// ✅ Correct: Create new array
setTodos([...todos, newTodo]);

// ❌ Wrong: Mutate existing array (React won't detect change)
todos.push(newTodo);
setTodos(todos);
```

### Pattern 4: Unique Keys in Lists

```jsx
// ✅ Good: Stable, unique ID
{todos.map(todo => (
  <TodoItem key={todo.id} todo={todo} />
))}

// ❌ Avoid: Index as key (can cause bugs with reordering)
{todos.map((todo, index) => (
  <TodoItem key={index} todo={todo} />
))}
```

---

## 5. Hooks Deep Dive

### 5.1 useState - Component State

**Purpose:** Store and update component data that changes over time.

```jsx
// Basic usage
const [count, setCount] = useState(0);

// With object (common mistake: partial updates)
const [user, setUser] = useState({ name: '', age: 0 });

// ❌ Wrong: Overwrites entire object
setUser({ name: 'John' }); // age is lost!

// ✅ Correct: Spread existing properties
setUser({ ...user, name: 'John' }); // age preserved
```

**When to use:**
- Form inputs (text, checkboxes)
- Toggle states (open/closed, visible/hidden)
- Lists of items (todos, comments)
- Simple counters or flags

**State Update Patterns:**

```jsx
// 1. Direct update (when new value doesn't depend on old value)
setCount(5);

// 2. Functional update (when new value depends on previous state)
// ✅ Correct: Use functional form
setCount(prevCount => prevCount + 1);

// ❌ Wrong: Can cause bugs with multiple rapid updates
setCount(count + 1);
```

**Why functional updates?** React batches state updates. With functional form, you always get the latest state.

### 5.2 useEffect - Side Effects

**Purpose:** Perform side effects (data fetching, subscriptions, DOM manipulation) after render.

```jsx
// Pattern 1: Run once on mount (like componentDidMount)
useEffect(() => {
  console.log('Component mounted');
  // Fetch data, set up subscriptions
}, []); // Empty dependency array

// Pattern 2: Run when dependencies change
useEffect(() => {
  console.log(`Count changed to ${count}`);
}, [count]); // Runs when count changes

// Pattern 3: Cleanup function (like componentWillUnmount)
useEffect(() => {
  const timer = setInterval(() => console.log('tick'), 1000);

  // Cleanup: runs before component unmounts or before re-running effect
  return () => {
    clearInterval(timer);
  };
}, []);
```

**Common Use Cases:**

```jsx
// 1. Fetching data
useEffect(() => {
  fetch('/api/todos')
    .then(res => res.json())
    .then(data => setTodos(data));
}, []); // Fetch once on mount

// 2. Subscribing to events
useEffect(() => {
  const handleResize = () => setWidth(window.innerWidth);
  window.addEventListener('resize', handleResize);

  return () => window.removeEventListener('resize', handleResize);
}, []);

// 3. Syncing with localStorage
useEffect(() => {
  localStorage.setItem('todos', JSON.stringify(todos));
}, [todos]); // Save whenever todos change
```

**⚠️ Common Mistakes:**

```jsx
// ❌ Mistake 1: Infinite loop
useEffect(() => {
  setCount(count + 1); // Updates state
}, [count]); // Watches count -> triggers again -> infinite loop!

// ❌ Mistake 2: Missing dependencies
useEffect(() => {
  console.log(count); // Uses count
}, []); // Should include count in dependencies!

// ✅ Correct
useEffect(() => {
  console.log(count);
}, [count]);

// ❌ Mistake 3: Not cleaning up subscriptions
useEffect(() => {
  const interval = setInterval(() => {}, 1000);
  // Missing cleanup! Memory leak!
}, []);

// ✅ Correct
useEffect(() => {
  const interval = setInterval(() => {}, 1000);
  return () => clearInterval(interval); // Cleanup
}, []);
```

### 5.3 useMemo - Memoize Expensive Calculations

**Purpose:** Cache expensive computation results, recompute only when dependencies change.

```jsx
// Without useMemo: Recalculates on every render (even if todos didn't change)
const expensiveValue = calculateExpensiveValue(todos);

// With useMemo: Recalculates only when todos change
const expensiveValue = useMemo(() => {
  return calculateExpensiveValue(todos);
}, [todos]);
```

**When to use:**
- Expensive calculations (filtering large lists, complex computations)
- Preventing unnecessary re-renders in child components

**Example: Filtering a large list**

```jsx
function TodoList({ todos, filter }) {
  // ❌ Without useMemo: Filters on every render
  // If parent re-renders (even for unrelated state), this recalculates
  const filteredTodos = todos.filter(todo => {
    // Imagine this is expensive (thousands of todos)
    return filter === 'all' ? true :
           filter === 'completed' ? todo.completed :
           !todo.completed;
  });

  // ✅ With useMemo: Filters only when todos or filter changes
  const filteredTodos = useMemo(() => {
    return todos.filter(todo => {
      return filter === 'all' ? true :
             filter === 'completed' ? todo.completed :
             !todo.completed;
    });
  }, [todos, filter]); // Only recompute when these change

  return (
    <ul>
      {filteredTodos.map(todo => <li key={todo.id}>{todo.text}</li>)}
    </ul>
  );
}
```

**⚠️ Don't Overuse:**
```jsx
// ❌ Unnecessary: Simple calculation
const doubled = useMemo(() => count * 2, [count]);

// ✅ Just do it directly
const doubled = count * 2;
```

**Rule of Thumb:** Use `useMemo` when:
1. Calculation is genuinely expensive (loops through large arrays, complex math)
2. You've profiled and identified a performance issue
3. The computed value is passed to a memoized component

### 5.4 useCallback - Memoize Functions

**Purpose:** Cache function instances between renders to prevent unnecessary re-renders of child components.

```jsx
// Without useCallback: New function instance on every render
const handleClick = () => {
  console.log('Clicked');
};

// With useCallback: Same function instance unless dependencies change
const handleClick = useCallback(() => {
  console.log('Clicked');
}, []); // Dependencies array
```

**Why it matters:**

```jsx
function ParentComponent() {
  const [count, setCount] = useState(0);

  // ❌ Problem: handleDelete is a new function on every render
  // Even if todos didn't change, all TodoItem components re-render
  const handleDelete = (id) => {
    setTodos(todos.filter(todo => todo.id !== id));
  };

  return (
    <div>
      <button onClick={() => setCount(count + 1)}>Count: {count}</button>
      {todos.map(todo => (
        <TodoItem
          key={todo.id}
          todo={todo}
          onDelete={handleDelete} // New function reference every render!
        />
      ))}
    </div>
  );
}

// ✅ Solution: Wrap with useCallback
const handleDelete = useCallback((id) => {
  setTodos(todos.filter(todo => todo.id !== id));
}, [todos]); // Only recreate function when todos changes
```

**Common Pattern: Event handlers passed to children**

```jsx
function TodoApp() {
  const [todos, setTodos] = useState([]);

  // ✅ Memoize handlers that are passed to child components
  const handleToggle = useCallback((id) => {
    setTodos(todos.map(todo =>
      todo.id === id ? { ...todo, completed: !todo.completed } : todo
    ));
  }, [todos]);

  const handleDelete = useCallback((id) => {
    setTodos(todos.filter(todo => todo.id !== id));
  }, [todos]);

  return (
    <ul>
      {todos.map(todo => (
        <TodoItem
          key={todo.id}
          todo={todo}
          onToggle={handleToggle}
          onDelete={handleDelete}
        />
      ))}
    </ul>
  );
}

// Child component should be memoized to benefit from useCallback
const TodoItem = React.memo(({ todo, onToggle, onDelete }) => {
  // Only re-renders if todo, onToggle, or onDelete changes
  return (
    <li>
      <input
        type="checkbox"
        checked={todo.completed}
        onChange={() => onToggle(todo.id)}
      />
      <span>{todo.text}</span>
      <button onClick={() => onDelete(todo.id)}>Delete</button>
    </li>
  );
});
```

**⚠️ Common Mistake: Stale Closures**

```jsx
// ❌ Problem: handleClick always logs initial count (0)
const handleClick = useCallback(() => {
  console.log(count); // Closure captures count from first render
}, []); // Empty deps -> function never updates

// ✅ Solution: Include count in dependencies
const handleClick = useCallback(() => {
  console.log(count);
}, [count]); // Function updates when count changes
```

### 5.5 useRef - Persist Values Without Re-rendering

**Purpose:** Store mutable values that persist across renders without causing re-renders.

```jsx
const myRef = useRef(initialValue);
// Access value: myRef.current
```

**Use Case 1: Accessing DOM Elements**

```jsx
function FocusInput() {
  const inputRef = useRef(null);

  const handleFocus = () => {
    inputRef.current.focus(); // Direct DOM manipulation
  };

  return (
    <div>
      <input ref={inputRef} type="text" />
      <button onClick={handleFocus}>Focus Input</button>
    </div>
  );
}
```

**Use Case 2: Storing Previous Values**

```jsx
function Counter() {
  const [count, setCount] = useState(0);
  const prevCountRef = useRef();

  useEffect(() => {
    prevCountRef.current = count; // Store current value for next render
  });

  const prevCount = prevCountRef.current;

  return (
    <div>
      <p>Current: {count}, Previous: {prevCount}</p>
      <button onClick={() => setCount(count + 1)}>Increment</button>
    </div>
  );
}
```

**Use Case 3: Storing Timers/Intervals**

```jsx
function Timer() {
  const [seconds, setSeconds] = useState(0);
  const intervalRef = useRef(null);

  const startTimer = () => {
    intervalRef.current = setInterval(() => {
      setSeconds(s => s + 1);
    }, 1000);
  };

  const stopTimer = () => {
    clearInterval(intervalRef.current);
  };

  return (
    <div>
      <p>Seconds: {seconds}</p>
      <button onClick={startTimer}>Start</button>
      <button onClick={stopTimer}>Stop</button>
    </div>
  );
}
```

**Key Difference: useState vs useRef**

```jsx
// useState: Triggers re-render when updated
const [count, setCount] = useState(0);
setCount(1); // ✅ Component re-renders

// useRef: No re-render when updated
const countRef = useRef(0);
countRef.current = 1; // ❌ Component does NOT re-render
```

### 5.6 useReducer - Complex State Logic

**Purpose:** Alternative to useState for complex state logic with multiple sub-values or transitions.

```jsx
const [state, dispatch] = useReducer(reducer, initialState);
```

**When to use:**
- State has multiple related values
- Next state depends on previous state
- Complex update logic (like Redux)

**Example: Todo List with useReducer**

```jsx
// 1. Define initial state
const initialState = {
  todos: [],
  filter: 'all'
};

// 2. Define reducer function
function todoReducer(state, action) {
  switch (action.type) {
    case 'ADD_TODO':
      return {
        ...state,
        todos: [...state.todos, action.payload]
      };

    case 'DELETE_TODO':
      return {
        ...state,
        todos: state.todos.filter(todo => todo.id !== action.payload)
      };

    case 'TOGGLE_TODO':
      return {
        ...state,
        todos: state.todos.map(todo =>
          todo.id === action.payload
            ? { ...todo, completed: !todo.completed }
            : todo
        )
      };

    case 'SET_FILTER':
      return {
        ...state,
        filter: action.payload
      };

    default:
      return state;
  }
}

// 3. Use in component
function TodoApp() {
  const [state, dispatch] = useReducer(todoReducer, initialState);

  const handleAddTodo = (text) => {
    dispatch({
      type: 'ADD_TODO',
      payload: { id: Date.now(), text, completed: false }
    });
  };

  const handleToggle = (id) => {
    dispatch({ type: 'TOGGLE_TODO', payload: id });
  };

  return (
    <div>
      {/* Component JSX */}
    </div>
  );
}
```

**useState vs useReducer:**

```jsx
// ✅ Use useState for simple state
const [count, setCount] = useState(0);
const [name, setName] = useState('');

// ✅ Use useReducer for complex state with multiple related values
const [state, dispatch] = useReducer(reducer, {
  user: null,
  posts: [],
  comments: [],
  loading: false,
  error: null
});
```

---

## 6. Debugging & Testing Mindset

### During Interview: Quick Debugging

```jsx
// 1. Add console.logs at key points
console.log('State updated:', todos);

// 2. Check if event handlers are called
const handleClick = () => {
  console.log('Handler called'); // Verify this prints
  // ... rest of logic
};

// 3. Verify state updates
useEffect(() => {
  console.log('Todos changed:', todos);
}, [todos]);
```

### Common Bugs & Quick Fixes

```jsx
// Bug 1: State not updating
// ❌ Problem: Mutating state directly
todos.push(newTodo);
setTodos(todos);

// ✅ Fix: Create new array
setTodos([...todos, newTodo]);

// Bug 2: Stale state in callbacks
// ❌ Problem: Using state directly in useCallback
const handler = useCallback(() => {
  console.log(todos); // Always logs initial todos!
}, []); // Missing dependency

// ✅ Fix: Include dependencies
const handler = useCallback(() => {
  console.log(todos);
}, [todos]);

// Bug 3: Key warnings in lists
// ❌ Problem: Missing or non-unique keys
{todos.map(todo => <li>{todo.text}</li>)}

// ✅ Fix: Add unique key
{todos.map(todo => <li key={todo.id}>{todo.text}</li>)}
```

---

## 7. Time Management Strategy

### 60-Minute Interview Breakdown

```
0-5 min:   Clarify requirements, ask questions
5-10 min:  Plan component structure, identify state
10-15 min: Set up basic component skeleton
15-35 min: Implement core functionality (adding, displaying, basic interactions)
35-45 min: Implement secondary features (delete, edit, filter)
45-50 min: Handle edge cases (empty states, validation)
50-55 min: Add minimal styling, test functionality
55-60 min: Walk through code, explain decisions
```

### Priority Matrix

```
┌────────────────────────┬────────────────────────┐
│  HIGH PRIORITY         │  MEDIUM PRIORITY       │
│  (Must Have)           │  (Should Have)         │
├────────────────────────┼────────────────────────┤
│  • Display data        │  • Edit functionality  │
│  • Add new items       │  • Filter/Search       │
│  • Basic interactions  │  • Sorting             │
│  • Delete items        │  • Form validation     │
└────────────────────────┴────────────────────────┘
┌────────────────────────┬────────────────────────┐
│  LOW PRIORITY          │  SKIP (Unless Time)    │
│  (Nice to Have)        │                        │
├────────────────────────┼────────────────────────┤
│  • Animations          │  • Complex styling     │
│  • Keyboard shortcuts  │  • Advanced animations │
│  • Accessibility       │  • Performance         │
│  • Empty states        │    optimization        │
└────────────────────────┴────────────────────────┘
```

---

## 8. Common Mistakes & Solutions

### Mistake 1: Overthinking the Solution

```jsx
// ❌ Over-engineered (for a 60-min interview)
function TodoApp() {
  const [state, dispatch] = useReducer(complexReducer, initialState);
  const memoizedTodos = useMemo(() => selectTodos(state), [state]);
  const optimizedHandler = useCallback(() => {}, []);
  // ... 50 more lines of optimization
}

// ✅ Keep it simple
function TodoApp() {
  const [todos, setTodos] = useState([]);
  const [input, setInput] = useState('');

  const handleAddTodo = () => {
    setTodos([...todos, { id: Date.now(), text: input }]);
    setInput('');
  };

  return (/* simple JSX */);
}
```

**Lesson:** Start simple. Optimize only if asked or if you have extra time.

### Mistake 2: Not Handling Edge Cases

```jsx
// ❌ No validation
const handleAddTodo = () => {
  setTodos([...todos, input]); // What if input is empty?
};

// ✅ With validation
const handleAddTodo = () => {
  if (!input.trim()) {
    return; // Don't add empty todos
  }
  setTodos([...todos, input.trim()]);
  setInput('');
};
```

### Mistake 3: Poor Component Structure

```jsx
// ❌ Everything in one component (hard to read)
function TodoApp() {
  return (
    <div>
      <input {...} />
      <button {...} />
      <ul>
        {todos.map(todo => (
          <li>
            <input type="checkbox" {...} />
            <span>{todo.text}</span>
            <button>Delete</button>
            <button>Edit</button>
            {/* More complex JSX */}
          </li>
        ))}
      </ul>
    </div>
  );
}

// ✅ Split into smaller components
function TodoApp() {
  return (
    <div>
      <TodoInput onAdd={handleAdd} />
      <TodoList todos={todos} onToggle={handleToggle} onDelete={handleDelete} />
    </div>
  );
}

function TodoList({ todos, onToggle, onDelete }) {
  return (
    <ul>
      {todos.map(todo => (
        <TodoItem
          key={todo.id}
          todo={todo}
          onToggle={onToggle}
          onDelete={onDelete}
        />
      ))}
    </ul>
  );
}

function TodoItem({ todo, onToggle, onDelete }) {
  return (
    <li>
      <input type="checkbox" checked={todo.completed} onChange={() => onToggle(todo.id)} />
      <span>{todo.text}</span>
      <button onClick={() => onDelete(todo.id)}>Delete</button>
    </li>
  );
}
```

### Mistake 4: Forgetting Keys in Lists

```jsx
// ❌ Missing key (React warning)
{todos.map(todo => <li>{todo.text}</li>)}

// ❌ Using index as key (can cause bugs)
{todos.map((todo, index) => <li key={index}>{todo.text}</li>)}

// ✅ Stable unique ID
{todos.map(todo => <li key={todo.id}>{todo.text}</li>)}
```

### Mistake 5: Mutating State

```jsx
// ❌ Mutating array (React won't detect change)
const handleToggle = (id) => {
  const todo = todos.find(t => t.id === id);
  todo.completed = !todo.completed; // Mutation!
  setTodos(todos);
};

// ✅ Creating new array
const handleToggle = (id) => {
  setTodos(todos.map(todo =>
    todo.id === id ? { ...todo, completed: !todo.completed } : todo
  ));
};
```

---

## 9. Interview Evaluation Criteria

### What Gets You Hired:

✅ **Working code** that meets requirements
✅ **Clean structure** with reusable components
✅ **Proper React patterns** (hooks, props, state)
✅ **Good communication** - explaining as you code
✅ **Handling edge cases** (empty states, validation)
✅ **Thoughtful design** - scalable and maintainable

### What Raises Red Flags:

❌ Non-functional code with bugs
❌ Messy, unreadable code
❌ Ignoring interviewer's hints
❌ Not testing the solution
❌ Over-engineering simple problems
❌ Hardcoding values everywhere

---

## 10. Question Directory

### 📁 [Easy Level](./easy/readme.md)
**Time:** 30-45 minutes per question
**Focus:** Basic React concepts, simple state management

1. **Accordion Component** - Collapsible sections, toggle state
2. **Star Rating** - Interactive rating, hover effects
3. **Todo List** - Add, display, delete tasks
4. **Contact Form** - Form handling, validation

### 📁 [Medium Level](./medium/readme.md)
**Time:** 45-60 minutes per question
**Focus:** Complex state, API integration, performance

1. **Pagination** - Client-side pagination with state
2. **Infinite Scroll** - Load more on scroll, async data
3. **Auto-complete/Type-ahead** - Debounced search, filtering
4. **Debounce Search** - Custom debounce hook, performance
5. **Modal/Dialog** - Portal, focus management, keyboard events

### 📁 [Hard Level](./hard/readme.md)
**Time:** 60-90 minutes per question
**Focus:** Advanced patterns, optimization, complex interactions

1. **Virtual Scrolling** - Render only visible items, performance
2. **File Explorer (Tree View)** - Recursive components, nested state
3. **Drag and Drop** - Event handling, state updates
4. **Custom Hooks** - Reusable logic extraction
5. **Complex State Management** - useReducer, Context API

---

## Quick Reference: Common Code Snippets

### Generate Unique IDs
```jsx
const id = Date.now(); // Simple
const id = `${Date.now()}-${Math.random()}`; // More unique
const id = crypto.randomUUID(); // Best (modern browsers)
```

### Debounce Function
```jsx
function useDebounce(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);

  return debouncedValue;
}
```

### LocalStorage Sync
```jsx
// Save to localStorage
useEffect(() => {
  localStorage.setItem('todos', JSON.stringify(todos));
}, [todos]);

// Load from localStorage
const [todos, setTodos] = useState(() => {
  const saved = localStorage.getItem('todos');
  return saved ? JSON.parse(saved) : [];
});
```

### Array Operations (Immutable)
```jsx
// Add
setItems([...items, newItem]);

// Remove
setItems(items.filter(item => item.id !== idToRemove));

// Update
setItems(items.map(item =>
  item.id === idToUpdate ? { ...item, ...updates } : item
));

// Toggle
setItems(items.map(item =>
  item.id === idToToggle ? { ...item, active: !item.active } : item
));
```

---

## Final Tips

1. **Start Simple:** Don't reach for useReducer or Context right away
2. **Communicate:** Think out loud, explain your decisions
3. **Ask Questions:** Clarify requirements before diving in
4. **Test Early:** Run your code frequently, catch bugs early
5. **Time Management:** Don't get stuck on one feature, move on
6. **Stay Calm:** Interviews are stressful, take a breath, you got this!

---

**Good luck with your interviews! 🚀**

Remember: The goal isn't perfect code, it's demonstrating your problem-solving skills and React knowledge.
