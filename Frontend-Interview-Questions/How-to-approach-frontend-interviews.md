# React Frontend Interview Questions - Comprehensive Guide

## How to Use This Resource

This comprehensive guide helps you prepare for React frontend coding interviews. Each question includes:
- ✅ **Step-by-step approach** - How to think through the problem
- ✅ **Complete implementation** - Working code with detailed comments
- ✅ **Hook explanations** - Why and how each hook is used
- ✅ **Diagrams** - Visual representations of component structure and state flow
- ✅ **Beginner mistakes** - Common pitfalls and how to avoid them
- ✅ **Interviewer Q&A** - Questions interviewers ask and how to answer
- ✅ **Optimization tips** - Performance improvements and best practices

---

## Interview Structure

Frontend React interviews typically follow this pattern:

### 1. Problem Understanding (5-10 minutes)
- Interviewer explains the task
- Ask clarifying questions about requirements
- Discuss edge cases and assumptions
- Confirm what's in scope vs out of scope

### 2. Planning & Design (5-10 minutes)
- Sketch component hierarchy
- Identify state requirements
- Plan data flow
- Discuss which hooks to use and why

### 3. Implementation (30-40 minutes)
- Start with basic structure
- Implement core functionality first
- Add edge case handling
- Test as you go

### 4. Testing & Refinement (5-10 minutes)
- Test the implementation
- Handle edge cases
- Discuss optimizations
- Answer follow-up questions

---

## Key Topics to Master

### 1. React Hooks

**useState**
- Managing local component state
- State updates are asynchronous
- Functional updates when state depends on previous value

```jsx
// ❌ Wrong - Direct state update
setCount(count + 1)
setCount(count + 1) // Won't increment by 2!

// ✅ Right - Functional update
setCount(prev => prev + 1)
setCount(prev => prev + 1) // Will increment by 2
```

**useEffect**
- Side effects (data fetching, subscriptions, DOM manipulation)
- Dependency array controls when effect runs
- Cleanup function prevents memory leaks

```jsx
// Why use useEffect?
// - Separate side effects from rendering logic
// - Synchronize component with external systems
// - Run code after render commits to DOM

useEffect(() => {
  // This runs after every render where 'id' changed
  const subscription = subscribeToData(id)

  // Cleanup runs before next effect and on unmount
  return () => subscription.unsubscribe()
}, [id]) // Dependency array
```

**useMemo**
- Memoize expensive calculations
- Only recalculates when dependencies change
- Prevents unnecessary re-computations

```jsx
// Why use useMemo?
// - Optimize performance for expensive operations
// - Prevent recalculating on every render
// - Maintain referential equality for objects/arrays

const expensiveValue = useMemo(() => {
  // This only runs when 'data' changes
  return data.filter(item => item.active)
    .map(item => complexTransform(item))
}, [data])
```

**useCallback**
- Memoize function references
- Prevents child re-renders when passing callbacks
- Useful with React.memo

```jsx
// Why use useCallback?
// - Prevent unnecessary child re-renders
// - Maintain function reference stability
// - Essential when function is in useEffect dependencies

const handleClick = useCallback(() => {
  // This function reference stays same unless 'id' changes
  doSomething(id)
}, [id])
```

**useRef**
- Persist values across renders without causing re-render
- Access DOM elements directly
- Store mutable values

```jsx
// Why use useRef?
// - Access DOM elements (focus, scroll, measure)
// - Store values that don't trigger re-renders
// - Keep track of previous values

const inputRef = useRef(null)
const previousValue = useRef(value)

// Focus input
inputRef.current.focus()

// Track previous value
useEffect(() => {
  previousValue.current = value
}, [value])
```

**useReducer**
- Complex state logic with multiple sub-values
- Alternative to useState for complex state
- Similar to Redux pattern

```jsx
// Why use useReducer?
// - Complex state transitions
// - Multiple related state values
// - State logic becomes easier to test

const reducer = (state, action) => {
  switch(action.type) {
    case 'INCREMENT': return { count: state.count + 1 }
    case 'DECREMENT': return { count: state.count - 1 }
    default: return state
  }
}

const [state, dispatch] = useReducer(reducer, { count: 0 })
```

---

## Common Interview Patterns

### Pattern 1: Form Handling & Validation

**When to use:**
- Login/signup forms
- Search inputs
- Multi-step forms

**Key concepts:**
- Controlled components
- Form validation
- Error handling
- Submit handling

```jsx
// Controlled input - React state is source of truth
const [value, setValue] = useState('')

<input
  value={value}  // Why? State controls input value
  onChange={e => setValue(e.target.value)} // Update state on change
/>
```

### Pattern 2: Data Fetching & Loading States

**When to use:**
- API calls
- Async operations
- Loading data on mount

**Key concepts:**
- useEffect for side effects
- Loading states
- Error handling
- Cleanup on unmount

```jsx
const [data, setData] = useState(null)
const [loading, setLoading] = useState(true)
const [error, setError] = useState(null)

useEffect(() => {
  let cancelled = false // Why? Prevent state updates if unmounted

  fetch('/api/data')
    .then(res => res.json())
    .then(data => {
      if (!cancelled) setData(data) // Only update if still mounted
    })
    .catch(err => {
      if (!cancelled) setError(err)
    })
    .finally(() => {
      if (!cancelled) setLoading(false)
    })

  return () => cancelled = true // Cleanup
}, [])
```

### Pattern 3: Lists & Keys

**When to use:**
- Rendering arrays
- Dynamic lists
- Todo lists, tables

**Key concepts:**
- Map over arrays
- Unique keys
- Filtering/sorting

```jsx
// Why use keys?
// - Help React identify which items changed
// - Improve rendering performance
// - Preserve component state

items.map(item => (
  <Item
    key={item.id}  // ✅ Use unique, stable ID
    data={item}
  />
))

// ❌ Don't use index as key if list can change order
items.map((item, index) => <Item key={index} />)
```

### Pattern 4: Conditional Rendering

**When to use:**
- Show/hide elements
- Loading states
- Different views based on state

**Key concepts:**
- Ternary operator
- && operator
- Early returns

```jsx
// Pattern 1: Ternary
{isLoading ? <Spinner /> : <Data />}

// Pattern 2: && operator
{error && <ErrorMessage error={error} />}

// Pattern 3: Early return
if (isLoading) return <Spinner />
if (error) return <ErrorMessage />
return <Data />
```

---

## Problem-Solving Approach

### Step 1: Understand Requirements
**Questions to ask:**
- What are the core features?
- What are the edge cases?
- What should happen on errors?
- Are there any performance requirements?
- Should state persist across refreshes?

### Step 2: Design Component Structure
**Think about:**
```
App (parent)
├── Controls (buttons, inputs)
├── Display (show current state)
└── List (show collection of items)
```

### Step 3: Identify State
**Ask yourself:**
- What data changes over time? → useState
- What data is derived from other state? → useMemo
- What data comes from APIs? → useState + useEffect
- What data is complex with related values? → useReducer

### Step 4: Implement Incrementally

**Priority 1: Core functionality**
```jsx
// Start with basic structure
function Component() {
  const [state, setState] = useState(initialValue)

  return <div>{/* Basic JSX */}</div>
}
```

**Priority 2: User interactions**
```jsx
const handleClick = () => {
  // Update state
  setState(newValue)
}
```

**Priority 3: Edge cases**
```jsx
// Validation
if (!value) return

// Error handling
try {
  doSomething()
} catch (error) {
  setError(error.message)
}
```

---

## Common Beginner Mistakes

### Mistake 1: Mutating State Directly
```jsx
// ❌ Wrong - Mutates state
const handleAdd = () => {
  items.push(newItem)
  setItems(items) // React may not detect change
}

// ✅ Right - Create new array
const handleAdd = () => {
  setItems([...items, newItem])
}

// Why? React uses shallow comparison to detect changes.
// Mutating state directly doesn't change reference, so React won't re-render.
```

### Mistake 2: Missing Dependencies in useEffect
```jsx
// ❌ Wrong - Missing dependency
useEffect(() => {
  doSomething(userId) // Uses userId but not in deps
}, [])

// ✅ Right - Include all dependencies
useEffect(() => {
  doSomething(userId)
}, [userId])

// Why? Effect won't re-run when userId changes, causing stale data.
```

### Mistake 3: Unnecessary Re-renders
```jsx
// ❌ Wrong - Creates new object every render
function Parent() {
  const config = { theme: 'dark' } // New object every time!
  return <Child config={config} />
}

// ✅ Right - Memoize object
function Parent() {
  const config = useMemo(() => ({ theme: 'dark' }), [])
  return <Child config={config} />
}

// Why? New object reference causes Child to re-render even if data same.
```

### Mistake 4: Infinite Loops in useEffect
```jsx
// ❌ Wrong - Infinite loop
useEffect(() => {
  setCount(count + 1) // Updates state
}, [count]) // Depends on state it updates!

// ✅ Right - Correct dependencies or remove
useEffect(() => {
  // Run only once on mount
  initializeCount()
}, [])

// Why? State update triggers effect, which updates state, infinite loop!
```

### Mistake 5: Not Handling Async Cleanup
```jsx
// ❌ Wrong - State update after unmount
useEffect(() => {
  fetchData().then(data => setData(data))
  // Component might unmount before fetch completes!
}, [])

// ✅ Right - Cancel on cleanup
useEffect(() => {
  let cancelled = false
  fetchData().then(data => {
    if (!cancelled) setData(data)
  })
  return () => cancelled = true
}, [])

// Why? Prevents "Can't perform state update on unmounted component" warning.
```

---

## Interviewer Questions & Answers

### Q1: "Why did you use useState here instead of useRef?"
**Answer:**
"I used useState because this value affects the UI rendering. When this value changes, we need the component to re-render to show the updated state. useRef would not trigger re-renders, so users wouldn't see the changes. useRef is better for values that don't affect rendering, like storing DOM references or tracking previous values."

### Q2: "Why did you add this to the dependency array?"
**Answer:**
"I added [userId] to the dependency array because the effect fetches data based on userId. When userId changes, we need to re-fetch the data for the new user. Without it, we'd show stale data from the previous user. The dependency array tells React to re-run the effect whenever these values change."

### Q3: "How would you optimize this component's performance?"
**Answer:**
"I'd optimize using:
1. **React.memo** - Prevent re-renders when props haven't changed
2. **useCallback** - Memoize callback functions passed to children
3. **useMemo** - Cache expensive calculations
4. **Virtualization** - For long lists, render only visible items
5. **Code splitting** - Lazy load components not immediately needed"

### Q4: "What happens if you don't provide a cleanup function?"
**Answer:**
"Without cleanup, we risk memory leaks. For example:
- Event listeners stay attached
- Timers keep running
- Subscriptions remain active
- Network requests update unmounted components

The cleanup function runs before the next effect and on unmount, preventing these issues."

### Q5: "Why use useCallback for this function?"
**Answer:**
"I use useCallback to memoize the function reference. Without it, a new function is created on every render. This causes child components to re-render unnecessarily because their prop (the function) has a new reference each time, even though the logic is the same. With useCallback, the function reference stays stable unless dependencies change."

---

## Diagram Types to Practice

### 1. Component Hierarchy
```
┌─────────────────────────┐
│     App (parent)        │
│  - manages global state │
└───────────┬─────────────┘
            │
    ┌───────┴────────┐
    ▼                ▼
┌─────────┐    ┌──────────┐
│ Controls│    │  Display │
│ - inputs│    │ - shows  │
│ - buttons│   │   data   │
└─────────┘    └──────────┘
```

### 2. State Flow
```
User Action
    │
    ▼
Event Handler
    │
    ▼
setState() call
    │
    ▼
State Update
    │
    ▼
Re-render
    │
    ▼
Updated UI
```

### 3. useEffect Lifecycle
```
Component Mounts
    │
    ▼
useEffect runs
    │
    ▼
Dependencies change?
    │
    ├─ No → Wait
    │
    └─ Yes → Cleanup → Effect runs
    │
    ▼
Component Unmounts
    │
    ▼
Cleanup runs
```

---

## Time Management Tips

**First 10 minutes:**
- Understand problem (3 min)
- Ask clarifying questions (3 min)
- Plan component structure (4 min)

**Next 30 minutes:**
- Basic structure & state (10 min)
- Core functionality (15 min)
- Edge cases & validation (5 min)

**Last 10 minutes:**
- Test edge cases (5 min)
- Discuss optimizations (3 min)
- Answer questions (2 min)

---

## Folder Structure

```
Frontend-Interview-Questions/
├── How-to-approach-frontend-interviews.md (this file)
├── README.md
├── Easy/
│   ├── counter-app.md
│   ├── todo-list-basic.md
│   ├── search-filter.md
│   ├── accordion.md
│   ├── toggle-theme.md
│   └── form-validation.md
├── Medium/
│   ├── infinite-scroll.md
│   ├── debounced-search.md
│   ├── pagination.md
│   ├── multi-step-form.md
│   ├── autocomplete.md
│   └── tabs-component.md
└── Hard/
    ├── nested-checkboxes.md
    ├── calendar-date-picker.md
    ├── connect-four-game.md
    ├── poll-widget.md
    ├── drag-drop-list.md
    ├── undo-redo.md
    └── virtual-scroll.md
```

---

## Final Tips

1. **Start simple** - Get basic functionality working first
2. **Think out loud** - Explain your reasoning
3. **Ask questions** - Clarify requirements early
4. **Handle edge cases** - Empty states, errors, loading
5. **Write clean code** - Meaningful names, proper structure
6. **Test as you go** - Don't wait until the end
7. **Know your hooks** - Understand when and why to use each
8. **Practice** - Build each question from scratch multiple times

Remember: Interviewers care more about your problem-solving approach and communication than perfect code. Show your thought process!
