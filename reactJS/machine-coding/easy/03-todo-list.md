# 3. Todo List

## 📋 Problem Statement

Build a classic todo list application. Users should be able to add new tasks, mark them as complete, and delete tasks.

### Example:
```
[_______] [Add]

[ ] Buy groceries
[✓] Learn React
[ ] Build project

Clicking checkbox toggles completion
Clicking delete removes the todo
```

---

## 🎯 Requirements

**Must Have:**
- ✅ Display list of todos
- ✅ Add new todo
- ✅ Mark as complete/incomplete
- ✅ Delete todo

**Nice to Have:**
- ⭐ Edit todo
- ⭐ Filter (all/active/completed)
- ⭐ LocalStorage persistence

---

## 🤔 Data Structure

```javascript
// Simple (just strings) - ❌ Not good
const todos = ['Buy milk', 'Learn React'];
// Problem: No way to track completion or unique IDs

// Objects - ✅ Use this
const todos = [
  { id: 1, text: 'Buy milk', completed: false },
  { id: 2, text: 'Learn React', completed: true }
];
```

---

## 💡 Complete Solution

```jsx
import { useState } from 'react';

function TodoApp() {
  // State 1: Array of todo objects
  // Why array? We have multiple todos
  const [todos, setTodos] = useState([]);

  // State 2: Input field value
  // Why separate? Input changes independently of todo list
  const [inputValue, setInputValue] = useState('');

  // CREATE: Add new todo
  const handleAddTodo = () => {
    // Validation: Don't add empty todos
    if (!inputValue.trim()) {
      return;
    }

    const newTodo = {
      id: Date.now(), // Simple unique ID
      // Why Date.now()? Timestamp is unique enough for interviews
      text: inputValue.trim(),
      completed: false
    };

    // Add to array (immutably)
    // Why spread? Must create NEW array for React to detect change
    setTodos([...todos, newTodo]);

    // Clear input
    setInputValue('');
  };

  // UPDATE: Toggle completion
  const handleToggleTodo = (id) => {
    setTodos(todos.map(todo =>
      todo.id === id
        ? { ...todo, completed: !todo.completed } // Toggle this one
        : todo // Keep others unchanged
    ));
    // Why map? Creates new array with updated object
  };

  // DELETE: Remove todo
  const handleDeleteTodo = (id) => {
    setTodos(todos.filter(todo => todo.id !== id));
    // Why filter? Creates new array without deleted item
  };

  // Handle Enter key
  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleAddTodo();
    }
  };

  return (
    <div className="todo-app" style={{ maxWidth: '600px', margin: '50px auto' }}>
      <h1>My Todo List</h1>

      {/* INPUT SECTION */}
      <div className="todo-input" style={{ marginBottom: '20px' }}>
        <input
          type="text"
          value={inputValue}
          // Why controlled? React manages value (single source of truth)
          onChange={(e) => setInputValue(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="What needs to be done?"
          style={{ padding: '10px', fontSize: '16px', width: '70%' }}
        />
        <button
          onClick={handleAddTodo}
          style={{ padding: '10px 20px', fontSize: '16px', cursor: 'pointer' }}
        >
          Add Todo
        </button>
      </div>

      {/* TODO LIST */}
      {todos.length === 0 ? (
        <p style={{ textAlign: 'center', color: '#999' }}>
          No todos yet. Add one above!
        </p>
      ) : (
        <ul style={{ listStyle: 'none', padding: 0 }}>
          {todos.map(todo => (
            <li
              key={todo.id}
              // Why key? React needs unique keys for efficient updates
              // ❌ Don't use index (can cause bugs with reordering)
              style={{
                padding: '15px',
                marginBottom: '10px',
                background: '#f9f9f9',
                borderRadius: '5px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <input
                  type="checkbox"
                  checked={todo.completed}
                  onChange={() => handleToggleTodo(todo.id)}
                  // Why arrow function? Need to pass the ID
                  style={{ marginRight: '10px', cursor: 'pointer' }}
                />
                <span
                  style={{
                    textDecoration: todo.completed ? 'line-through' : 'none',
                    color: todo.completed ? '#999' : '#000',
                  }}
                >
                  {todo.text}
                </span>
              </div>

              <button
                onClick={() => handleDeleteTodo(todo.id)}
                style={{
                  padding: '5px 10px',
                  background: '#ff4444',
                  color: 'white',
                  border: 'none',
                  borderRadius: '3px',
                  cursor: 'pointer'
                }}
              >
                Delete
              </button>
            </li>
          ))}
        </ul>
      )}

      {/* STATS */}
      {todos.length > 0 && (
        <div style={{ marginTop: '20px', textAlign: 'center', color: '#666' }}>
          <p>
            Total: {todos.length} |
            Active: {todos.filter(t => !t.completed).length} |
            Completed: {todos.filter(t => t.completed).length}
          </p>
        </div>
      )}
    </div>
  );
}

export default TodoApp;
```

---

## 🧠 CRUD Operations Explained

```javascript
// CREATE: Add to array
setTodos([...todos, newTodo]);
// Creates new array: [existing items, new item]

// READ: Display list
todos.map(todo => <TodoItem />)

// UPDATE: Modify item
setTodos(todos.map(todo =>
  todo.id === id ? { ...todo, completed: !todo.completed } : todo
));
// Creates new array with one object updated

// DELETE: Remove from array
setTodos(todos.filter(todo => todo.id !== id));
// Creates new array without the deleted item
```

---

## 🐛 Common Mistakes

### Mistake 1: Mutating State

```jsx
// ❌ WRONG: Pushing to array (mutation)
todos.push(newTodo);
setTodos(todos); // React won't re-render!

// ✅ CORRECT: Create new array
setTodos([...todos, newTodo]);
```

### Mistake 2: Not Validating Input

```jsx
// ❌ WRONG: Can add empty todos
setTodos([...todos, { text: inputValue }]);

// ✅ CORRECT: Validate first
if (!inputValue.trim()) return;
setTodos([...todos, { text: inputValue.trim() }]);
```

### Mistake 3: Index as Key

```jsx
// ❌ WRONG: Index as key (causes bugs)
{todos.map((todo, index) => (
  <li key={index}>{todo.text}</li>
))}
// Problem: If you delete first todo, indexes shift!

// ✅ CORRECT: Unique ID as key
{todos.map(todo => (
  <li key={todo.id}>{todo.text}</li>
))}
```

---

## 🚀 Advanced: LocalStorage Persistence

```jsx
import { useState, useEffect } from 'react';

function TodoApp() {
  // Initialize from localStorage
  // Why function form? Only runs once on mount
  const [todos, setTodos] = useState(() => {
    const saved = localStorage.getItem('todos');
    return saved ? JSON.parse(saved) : [];
    // Why JSON.parse? localStorage stores strings
  });

  // Save to localStorage whenever todos change
  // Why useEffect? Side effect that runs after render
  useEffect(() => {
    localStorage.setItem('todos', JSON.stringify(todos));
    // Why JSON.stringify? localStorage only stores strings
  }, [todos]); // Run when todos changes

  // ... rest of component
}
```

**How useEffect Works Here:**

```
Component mounts:
1. useState loads todos from localStorage
2. Initial render with loaded todos
3. useEffect runs, saves current todos to localStorage

User adds todo:
1. setTodos updates state
2. Component re-renders with new todos
3. useEffect runs again, saves new todos to localStorage
```

---

## 📊 Data Flow Diagram

```
User types → onChange → setInputValue
User clicks Add → handleAddTodo → setTodos([...todos, newTodo])
State updates → React re-renders → Display new todo
User clicks checkbox → handleToggleTodo → Update completed status
User clicks delete → handleDeleteTodo → Remove from array
```

---

## 🎤 Interviewer Q&A

**Q: How would you handle todos from an API?**

```jsx
useEffect(() => {
  fetch('/api/todos')
    .then(res => res.json())
    .then(data => setTodos(data));
}, []); // Fetch once on mount
```

**Q: How would you optimize for large lists?**

A: "Use React.memo for TodoItem, useCallback for handlers, or virtualization for thousands of items."

---
