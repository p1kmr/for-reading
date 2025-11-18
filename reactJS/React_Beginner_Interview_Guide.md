# React Beginner Interview Questions & Answers Guide

## Table of Contents
1. [Basic Concepts](#basic-concepts)
2. [Components & Props](#components--props)
3. [State Management](#state-management)
4. [Hooks (useState & useEffect)](#hooks-usestate--useeffect)
5. [Event Handling](#event-handling)
6. [Common Mistakes & Solutions](#common-mistakes--solutions)
7. [Practical Coding Questions](#practical-coding-questions)

---

## Basic Concepts

### Q1: What is React?
**Answer:** React is a JavaScript library for building user interfaces, particularly single-page applications. It was developed by Facebook and allows developers to create reusable UI components.

**Key Features:**
- Component-based architecture
- Virtual DOM for efficient rendering
- Unidirectional data flow
- JSX syntax

```javascript
// Simple React component example
import React from 'react';

function Welcome() {
  return <h1>Hello, React!</h1>;
}

export default Welcome;
```

---

### Q2: What is JSX?
**Answer:** JSX (JavaScript XML) is a syntax extension for JavaScript that allows you to write HTML-like code within JavaScript. It makes React code more readable and expressive.

```javascript
// JSX Example
const element = <h1>Hello, World!</h1>;

// JSX with expressions
const name = "John";
const greeting = <h1>Hello, {name}!</h1>;

// JSX with attributes
const link = <a href="https://react.dev">Learn React</a>;
```

**Common Mistake:**
```javascript
// ❌ WRONG - class is a reserved keyword in JavaScript
<div class="container">Content</div>

// ✅ CORRECT - use className instead
<div className="container">Content</div>
```

---

### Q3: What is the Virtual DOM?
**Answer:** The Virtual DOM is a lightweight copy of the actual DOM. React uses it to optimize updates by:
1. Creating a virtual representation of the UI
2. Comparing it with the previous version (diffing)
3. Updating only the changed parts in the real DOM

**Benefits:**
- Faster updates
- Better performance
- Efficient re-rendering

---

### Q4: What is the difference between React and ReactDOM?
**Answer:**
- **React**: Core library for creating components and managing state
- **ReactDOM**: Provides DOM-specific methods to render React components in the browser

```javascript
import React from 'react';
import ReactDOM from 'react-dom/client';

// React creates the component
const App = () => <h1>Hello React!</h1>;

// ReactDOM renders it to the DOM
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
```

---

## Components & Props

### Q5: What are Components in React?
**Answer:** Components are the building blocks of a React application. They are reusable pieces of code that return HTML elements.

**Types of Components:**

**1. Functional Components (Modern approach)**
```javascript
function Greeting(props) {
  return <h1>Hello, {props.name}!</h1>;
}

// Arrow function syntax
const Greeting = (props) => {
  return <h1>Hello, {props.name}!</h1>;
};
```

**2. Class Components (Legacy approach)**
```javascript
import React, { Component } from 'react';

class Greeting extends Component {
  render() {
    return <h1>Hello, {this.props.name}!</h1>;
  }
}
```

---

### Q6: What are Props?
**Answer:** Props (short for properties) are read-only data passed from parent to child components. They allow components to be dynamic and reusable.

```javascript
// Parent component
function App() {
  return (
    <div>
      <UserCard name="Alice" age={25} />
      <UserCard name="Bob" age={30} />
    </div>
  );
}

// Child component
function UserCard(props) {
  return (
    <div>
      <h2>{props.name}</h2>
      <p>Age: {props.age}</p>
    </div>
  );
}

// With destructuring (cleaner)
function UserCard({ name, age }) {
  return (
    <div>
      <h2>{name}</h2>
      <p>Age: {age}</p>
    </div>
  );
}
```

**Common Mistake:**
```javascript
// ❌ WRONG - Trying to modify props
function UserCard(props) {
  props.name = "Changed"; // Props are read-only!
  return <h2>{props.name}</h2>;
}

// ✅ CORRECT - Props should never be modified
function UserCard({ name }) {
  const displayName = name.toUpperCase(); // Create new variable instead
  return <h2>{displayName}</h2>;
}
```

---

### Q7: What is the difference between State and Props?
**Answer:**

| Feature | Props | State |
|---------|-------|-------|
| Mutability | Immutable (read-only) | Mutable (can be changed) |
| Ownership | Passed from parent | Owned by component |
| Purpose | Configure component | Manage component data |
| Changes | Parent re-renders | Component re-renders |

```javascript
// Props example
function Child({ message }) {
  return <p>{message}</p>; // Receives data from parent
}

function Parent() {
  return <Child message="Hello from parent" />;
}

// State example
import { useState } from 'react';

function Counter() {
  const [count, setCount] = useState(0); // Manages its own data

  return (
    <div>
      <p>Count: {count}</p>
      <button onClick={() => setCount(count + 1)}>Increment</button>
    </div>
  );
}
```

---

## State Management

### Q8: What is State in React?
**Answer:** State is a built-in object that stores component data that can change over time. When state changes, the component re-renders.

```javascript
import { useState } from 'react';

function ToggleButton() {
  const [isOn, setIsOn] = useState(false);

  return (
    <button onClick={() => setIsOn(!isOn)}>
      {isOn ? 'ON' : 'OFF'}
    </button>
  );
}
```

---

### Q9: How do you update State?
**Answer:** Never modify state directly. Always use the setter function provided by `useState`.

```javascript
import { useState } from 'react';

function Counter() {
  const [count, setCount] = useState(0);

  // ❌ WRONG - Direct mutation
  const wrongIncrement = () => {
    count = count + 1; // This won't work!
  };

  // ✅ CORRECT - Using setter function
  const correctIncrement = () => {
    setCount(count + 1);
  };

  // ✅ BEST - Using functional update (when based on previous state)
  const bestIncrement = () => {
    setCount(prevCount => prevCount + 1);
  };

  return (
    <div>
      <p>Count: {count}</p>
      <button onClick={bestIncrement}>Increment</button>
    </div>
  );
}
```

---

## Hooks (useState & useEffect)

### Q10: What are Hooks?
**Answer:** Hooks are functions that let you use state and other React features in functional components. They were introduced in React 16.8.

**Rules of Hooks:**
1. Only call hooks at the top level (not inside loops, conditions, or nested functions)
2. Only call hooks from React functional components or custom hooks

```javascript
import { useState, useEffect } from 'react';

function Example() {
  const [count, setCount] = useState(0); // ✅ Top level

  // ❌ WRONG - Hook inside condition
  if (count > 0) {
    const [name, setName] = useState(''); // Error!
  }

  // ❌ WRONG - Hook inside loop
  for (let i = 0; i < 5; i++) {
    useEffect(() => {}); // Error!
  }

  return <div>Count: {count}</div>;
}
```

---

### Q11: Explain useState Hook
**Answer:** `useState` is a Hook that lets you add state to functional components.

**Syntax:** `const [stateVariable, setStateFunction] = useState(initialValue);`

```javascript
import { useState } from 'react';

function Form() {
  // String state
  const [name, setName] = useState('');

  // Number state
  const [age, setAge] = useState(0);

  // Boolean state
  const [isSubmitted, setIsSubmitted] = useState(false);

  // Object state
  const [user, setUser] = useState({
    firstName: '',
    lastName: ''
  });

  // Array state
  const [items, setItems] = useState([]);

  const handleSubmit = () => {
    console.log({ name, age });
    setIsSubmitted(true);
  };

  return (
    <div>
      <input
        value={name}
        onChange={(e) => setName(e.target.value)}
        placeholder="Name"
      />
      <input
        type="number"
        value={age}
        onChange={(e) => setAge(Number(e.target.value))}
        placeholder="Age"
      />
      <button onClick={handleSubmit}>Submit</button>
      {isSubmitted && <p>Form submitted!</p>}
    </div>
  );
}
```

**Common Mistake with Objects:**
```javascript
// ❌ WRONG - Mutating state directly
const updateUser = () => {
  user.firstName = "John"; // Don't do this!
  setUser(user);
};

// ✅ CORRECT - Creating new object
const updateUser = () => {
  setUser({
    ...user,
    firstName: "John"
  });
};
```

---

### Q12: Explain useEffect Hook
**Answer:** `useEffect` lets you perform side effects in functional components (like data fetching, subscriptions, or DOM manipulation).

**Syntax:** `useEffect(effectFunction, dependencyArray)`

```javascript
import { useState, useEffect } from 'react';

function DataFetcher() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);

  // Run once on mount (empty dependency array)
  useEffect(() => {
    console.log('Component mounted');

    // Cleanup function (runs on unmount)
    return () => {
      console.log('Component will unmount');
    };
  }, []);

  // Run when 'data' changes
  useEffect(() => {
    console.log('Data changed:', data);
  }, [data]);

  // Run on every render (no dependency array - usually avoid this)
  useEffect(() => {
    console.log('Component rendered');
  });

  return <div>Data: {JSON.stringify(data)}</div>;
}
```

**Different useEffect patterns:**

```javascript
// 1. Run once on mount
useEffect(() => {
  fetchData();
}, []); // Empty array

// 2. Run when specific value changes
useEffect(() => {
  saveToLocalStorage(count);
}, [count]); // Runs when 'count' changes

// 3. Run on every render (avoid if possible)
useEffect(() => {
  console.log('Rendered');
}); // No dependency array

// 4. With cleanup
useEffect(() => {
  const timer = setInterval(() => {
    console.log('Tick');
  }, 1000);

  return () => {
    clearInterval(timer); // Cleanup
  };
}, []);
```

---

## Event Handling

### Q13: How do you handle events in React?
**Answer:** Event handling in React is similar to HTML, but uses camelCase naming and passes functions as handlers.

```javascript
function EventExample() {
  const handleClick = () => {
    alert('Button clicked!');
  };

  const handleInputChange = (event) => {
    console.log('Input value:', event.target.value);
  };

  const handleSubmit = (event) => {
    event.preventDefault(); // Prevent default form submission
    console.log('Form submitted');
  };

  return (
    <div>
      {/* ❌ WRONG - HTML style */}
      {/* <button onclick="handleClick()">Click</button> */}

      {/* ✅ CORRECT - React style */}
      <button onClick={handleClick}>Click Me</button>

      <input onChange={handleInputChange} />

      <form onSubmit={handleSubmit}>
        <button type="submit">Submit</button>
      </form>
    </div>
  );
}
```

**Common Mistake:**
```javascript
// ❌ WRONG - Calling function immediately
<button onClick={handleClick()}>Click</button>

// ✅ CORRECT - Passing function reference
<button onClick={handleClick}>Click</button>

// ✅ CORRECT - Using arrow function (when passing arguments)
<button onClick={() => handleClick('argument')}>Click</button>
```

---

### Q14: How do you pass arguments to event handlers?
**Answer:** Use arrow functions to pass arguments to event handlers.

```javascript
function TodoList() {
  const [todos, setTodos] = useState(['Task 1', 'Task 2', 'Task 3']);

  const deleteTodo = (index) => {
    const newTodos = todos.filter((_, i) => i !== index);
    setTodos(newTodos);
  };

  return (
    <ul>
      {todos.map((todo, index) => (
        <li key={index}>
          {todo}
          {/* Using arrow function to pass index */}
          <button onClick={() => deleteTodo(index)}>Delete</button>
        </li>
      ))}
    </ul>
  );
}
```

---

## Common Mistakes & Solutions

### Mistake 1: Using Array Index as Key
**Problem:** Using array indices as keys can cause issues when items are reordered or deleted.

```javascript
// ❌ WRONG - Using index as key
function List({ items }) {
  return (
    <ul>
      {items.map((item, index) => (
        <li key={index}>{item}</li>
      ))}
    </ul>
  );
}

// ✅ CORRECT - Using unique ID
function List({ items }) {
  return (
    <ul>
      {items.map((item) => (
        <li key={item.id}>{item.name}</li>
      ))}
    </ul>
  );
}
```

---

### Mistake 2: Infinite Loop in useEffect
**Problem:** Missing dependency array or updating state that's in the dependency array.

```javascript
import { useState, useEffect } from 'react';

// ❌ WRONG - Infinite loop
function BadComponent() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    setCount(count + 1); // This runs forever!
  }); // No dependency array

  return <div>{count}</div>;
}

// ✅ CORRECT - With dependency array
function GoodComponent() {
  const [data, setData] = useState([]);

  useEffect(() => {
    fetchData().then(setData);
  }, []); // Runs once on mount

  return <div>{data.length}</div>;
}

// ✅ CORRECT - Conditional update
function ConditionalUpdate() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    if (count < 10) {
      setCount(count + 1);
    }
  }, [count]); // Runs when count changes, but stops at 10

  return <div>{count}</div>;
}
```

---

### Mistake 3: Not Cleaning Up useEffect
**Problem:** Memory leaks from subscriptions, timers, or event listeners.

```javascript
import { useState, useEffect } from 'react';

// ❌ WRONG - No cleanup
function BadTimer() {
  const [seconds, setSeconds] = useState(0);

  useEffect(() => {
    setInterval(() => {
      setSeconds(s => s + 1);
    }, 1000);
    // Missing cleanup - timer keeps running after unmount!
  }, []);

  return <div>{seconds}</div>;
}

// ✅ CORRECT - With cleanup
function GoodTimer() {
  const [seconds, setSeconds] = useState(0);

  useEffect(() => {
    const intervalId = setInterval(() => {
      setSeconds(s => s + 1);
    }, 1000);

    return () => {
      clearInterval(intervalId); // Cleanup on unmount
    };
  }, []);

  return <div>{seconds}</div>;
}
```

---

### Mistake 4: Async/Await in useEffect
**Problem:** useEffect callback cannot be async directly.

```javascript
// ❌ WRONG - useEffect cannot be async
function BadDataFetch() {
  const [data, setData] = useState(null);

  useEffect(async () => { // Error!
    const result = await fetchData();
    setData(result);
  }, []);

  return <div>{data}</div>;
}

// ✅ CORRECT - Define async function inside
function GoodDataFetch() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);
        const result = await fetchData();
        setData(result);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, []);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;
  return <div>{JSON.stringify(data)}</div>;
}
```

---

### Mistake 5: Directly Mutating State
**Problem:** Modifying state directly instead of using setter function.

```javascript
// ❌ WRONG - Direct mutation
function BadTodoList() {
  const [todos, setTodos] = useState(['Task 1']);

  const addTodo = () => {
    todos.push('New Task'); // Don't mutate state directly!
    setTodos(todos); // React won't detect this change
  };

  return <div>{todos.length}</div>;
}

// ✅ CORRECT - Create new array
function GoodTodoList() {
  const [todos, setTodos] = useState(['Task 1']);

  const addTodo = () => {
    setTodos([...todos, 'New Task']); // Create new array
  };

  // Alternative using concat
  const addTodoAlt = () => {
    setTodos(todos.concat('New Task'));
  };

  return (
    <div>
      <p>Total: {todos.length}</p>
      <button onClick={addTodo}>Add Todo</button>
    </div>
  );
}
```

---

### Mistake 6: Not Using Functional Updates
**Problem:** Using stale state values when updating based on previous state.

```javascript
// ❌ RISKY - May use stale state
function Counter() {
  const [count, setCount] = useState(0);

  const increment = () => {
    setCount(count + 1);
    setCount(count + 1); // This won't increment by 2!
    // Both use the same 'count' value
  };

  return <button onClick={increment}>{count}</button>;
}

// ✅ CORRECT - Using functional update
function Counter() {
  const [count, setCount] = useState(0);

  const increment = () => {
    setCount(prevCount => prevCount + 1);
    setCount(prevCount => prevCount + 1); // This works correctly!
  };

  return <button onClick={increment}>{count}</button>;
}
```

---

### Mistake 7: Forgetting to Prevent Default
**Problem:** Not preventing default form submission behavior.

```javascript
// ❌ WRONG - Page will reload
function BadForm() {
  const handleSubmit = () => {
    console.log('Submitted');
    // Page reloads!
  };

  return (
    <form onSubmit={handleSubmit}>
      <button type="submit">Submit</button>
    </form>
  );
}

// ✅ CORRECT - Prevent default behavior
function GoodForm() {
  const handleSubmit = (event) => {
    event.preventDefault(); // Prevent page reload
    console.log('Submitted');
  };

  return (
    <form onSubmit={handleSubmit}>
      <button type="submit">Submit</button>
    </form>
  );
}
```

---

## Practical Coding Questions

### Exercise 1: Simple Counter
**Task:** Create a counter with increment, decrement, and reset buttons.

```javascript
import { useState } from 'react';

function Counter() {
  const [count, setCount] = useState(0);

  const increment = () => setCount(count + 1);
  const decrement = () => setCount(count - 1);
  const reset = () => setCount(0);

  return (
    <div>
      <h1>Count: {count}</h1>
      <button onClick={increment}>+1</button>
      <button onClick={decrement}>-1</button>
      <button onClick={reset}>Reset</button>
    </div>
  );
}

export default Counter;
```

---

### Exercise 2: Todo List
**Task:** Create a simple todo list with add and delete functionality.

```javascript
import { useState } from 'react';

function TodoList() {
  const [todos, setTodos] = useState([]);
  const [inputValue, setInputValue] = useState('');

  const addTodo = () => {
    if (inputValue.trim()) {
      setTodos([...todos, {
        id: Date.now(),
        text: inputValue,
        completed: false
      }]);
      setInputValue('');
    }
  };

  const deleteTodo = (id) => {
    setTodos(todos.filter(todo => todo.id !== id));
  };

  const toggleComplete = (id) => {
    setTodos(todos.map(todo =>
      todo.id === id ? { ...todo, completed: !todo.completed } : todo
    ));
  };

  return (
    <div>
      <h1>Todo List</h1>
      <input
        value={inputValue}
        onChange={(e) => setInputValue(e.target.value)}
        placeholder="Add a new todo"
        onKeyPress={(e) => e.key === 'Enter' && addTodo()}
      />
      <button onClick={addTodo}>Add</button>

      <ul>
        {todos.map(todo => (
          <li key={todo.id} style={{ textDecoration: todo.completed ? 'line-through' : 'none' }}>
            <input
              type="checkbox"
              checked={todo.completed}
              onChange={() => toggleComplete(todo.id)}
            />
            {todo.text}
            <button onClick={() => deleteTodo(todo.id)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default TodoList;
```

---

### Exercise 3: Form with Validation
**Task:** Create a form with name and email validation.

```javascript
import { useState } from 'react';

function FormWithValidation() {
  const [formData, setFormData] = useState({
    name: '',
    email: ''
  });
  const [errors, setErrors] = useState({});
  const [submitted, setSubmitted] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors({
        ...errors,
        [name]: ''
      });
    }
  };

  const validate = () => {
    const newErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Name is required';
    } else if (formData.name.length < 3) {
      newErrors.name = 'Name must be at least 3 characters';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid';
    }

    return newErrors;
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    const validationErrors = validate();

    if (Object.keys(validationErrors).length === 0) {
      setSubmitted(true);
      console.log('Form submitted:', formData);
      // Reset form
      setFormData({ name: '', email: '' });
    } else {
      setErrors(validationErrors);
    }
  };

  return (
    <div>
      <h1>Registration Form</h1>
      {submitted && <p style={{ color: 'green' }}>Form submitted successfully!</p>}

      <form onSubmit={handleSubmit}>
        <div>
          <label>Name:</label>
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleChange}
          />
          {errors.name && <p style={{ color: 'red' }}>{errors.name}</p>}
        </div>

        <div>
          <label>Email:</label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
          />
          {errors.email && <p style={{ color: 'red' }}>{errors.email}</p>}
        </div>

        <button type="submit">Submit</button>
      </form>
    </div>
  );
}

export default FormWithValidation;
```

---

### Exercise 4: Fetch Data from API
**Task:** Fetch and display data from an API with loading and error states.

```javascript
import { useState, useEffect } from 'react';

function DataFetcher() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        const response = await fetch('https://jsonplaceholder.typicode.com/users');

        if (!response.ok) {
          throw new Error('Failed to fetch data');
        }

        const result = await response.json();
        setData(result);
        setError(null);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, []);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div style={{ color: 'red' }}>Error: {error}</div>;
  }

  return (
    <div>
      <h1>User List</h1>
      <ul>
        {data.map(user => (
          <li key={user.id}>
            <strong>{user.name}</strong> - {user.email}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default DataFetcher;
```

---

### Exercise 5: Toggle Dark Mode
**Task:** Create a dark mode toggle button.

```javascript
import { useState } from 'react';

function DarkModeToggle() {
  const [isDark, setIsDark] = useState(false);

  const styles = {
    container: {
      backgroundColor: isDark ? '#333' : '#fff',
      color: isDark ? '#fff' : '#333',
      minHeight: '100vh',
      padding: '20px',
      transition: 'all 0.3s ease'
    },
    button: {
      padding: '10px 20px',
      backgroundColor: isDark ? '#fff' : '#333',
      color: isDark ? '#333' : '#fff',
      border: 'none',
      borderRadius: '5px',
      cursor: 'pointer'
    }
  };

  return (
    <div style={styles.container}>
      <h1>Dark Mode Example</h1>
      <p>Click the button to toggle dark mode</p>
      <button
        style={styles.button}
        onClick={() => setIsDark(!isDark)}
      >
        {isDark ? '☀️ Light Mode' : '🌙 Dark Mode'}
      </button>
    </div>
  );
}

export default DarkModeToggle;
```

---

## Quick Reference Cheat Sheet

### Common Hooks
```javascript
// useState - manage state
const [state, setState] = useState(initialValue);

// useEffect - side effects
useEffect(() => {
  // effect code
  return () => {
    // cleanup
  };
}, [dependencies]);

// useRef - DOM reference or mutable value
const ref = useRef(initialValue);

// useContext - consume context
const value = useContext(MyContext);
```

### Component Lifecycle with Hooks
```javascript
// Mount (runs once)
useEffect(() => {
  console.log('Component mounted');
}, []);

// Update (runs when dependency changes)
useEffect(() => {
  console.log('Value changed');
}, [value]);

// Unmount (cleanup)
useEffect(() => {
  return () => {
    console.log('Component will unmount');
  };
}, []);
```

### Array Operations (Immutable)
```javascript
const [items, setItems] = useState([1, 2, 3]);

// Add
setItems([...items, 4]);
setItems([0, ...items]);

// Remove
setItems(items.filter(item => item !== 2));

// Update
setItems(items.map(item => item === 2 ? 20 : item));
```

### Object Operations (Immutable)
```javascript
const [user, setUser] = useState({ name: 'John', age: 25 });

// Update property
setUser({ ...user, age: 26 });

// Add property
setUser({ ...user, email: 'john@example.com' });

// Remove property
const { age, ...rest } = user;
setUser(rest);
```

---

## Best Practices for Beginners

1. **Use Functional Components** - Prefer functional components with hooks over class components
2. **Keep Components Small** - Each component should do one thing well
3. **Use Meaningful Names** - Component and variable names should be descriptive
4. **Don't Mutate State** - Always create new objects/arrays when updating state
5. **Use Keys Properly** - Use unique, stable IDs as keys (not array indices)
6. **Clean Up Effects** - Always clean up subscriptions, timers, and listeners
7. **Handle Loading & Error States** - Always show loading and error states for async operations
8. **Use PropTypes or TypeScript** - Add type checking to catch errors early
9. **Follow the Rules of Hooks** - Only call hooks at the top level
10. **Keep State Local** - Only lift state up when necessary

---

## Resources for Further Learning

1. **Official React Documentation**: https://react.dev
2. **React Tutorial**: https://react.dev/learn
3. **React Hooks Reference**: https://react.dev/reference/react
4. **JavaScript Info**: https://javascript.info
5. **FreeCodeCamp React Course**: https://www.freecodecamp.org/learn

---

**Good luck with your React interviews! 🚀**
