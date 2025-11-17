# React Frontend Interview Questions - Complete Beginner's Guide

## 📚 Complete Guide for Frontend Coding Interviews

This comprehensive guide walks you through **React machine coding interview questions** from beginner to advanced level. Each problem includes step-by-step solutions, explanations, and best practices.

---

## 🎯 How to Use This Guide

### For Interview Preparation
1. **Start with Easy problems** - Build foundational skills
2. **Practice coding without looking at solutions** - Try for 30 minutes first
3. **Understand the "Why"** - Don't just memorize code
4. **Time yourself** - Easy: 15-20 min, Medium: 30-45 min, Hard: 45-60 min

### Key React Concepts Covered
- ✅ useState & useEffect hooks
- ✅ Controlled components
- ✅ Event handling
- ✅ Conditional rendering
- ✅ List rendering with keys
- ✅ Props and component composition
- ✅ Performance optimization (useMemo, useCallback)
- ✅ useRef and DOM manipulation
- ✅ Custom hooks
- ✅ Context API (for advanced problems)

---

## 📖 Table of Contents

### Easy Problems (15-20 minutes each)
1. [Accordion Component](#1-accordion-component-easy)
2. [Star Rating Component](#2-star-rating-component-easy)
3. [Todo List](#3-todo-list-easy)
4. [Contact Form](#4-contact-form-easy)

### Medium Problems (30-45 minutes each)
5. [Tabs Component](#5-tabs-component-medium)
6. [Image Carousel](#6-image-carousel-medium)
7. [Search with Debounce](#7-search-with-debounce-medium)
8. [Infinite Scrolling List](#8-infinite-scrolling-list-medium)
9. [Autocomplete Input](#9-autocomplete-input-medium)
10. [Data Table with Sorting](#10-data-table-with-sorting-medium)
11. [Modal Dialog](#11-modal-dialog-medium)
12. [File Explorer / Tree View](#12-file-explorer-tree-view-medium)

### Hard Problems (45-60 minutes each)
13. [Nested Checkboxes](#13-nested-checkboxes-hard)
14. [Calendar / Date Picker](#14-calendar-date-picker-hard)
15. [Connect Four Game](#15-connect-four-game-hard)
16. [Poll Widget](#16-poll-widget-hard)

---

## Easy Problems

---

## 1. Accordion Component (Easy)

### Problem Statement
Build an accordion component with multiple collapsible sections. When a user clicks a section header, it should toggle showing/hiding that section's content. Only one section should be open at a time (or allow multiple, based on variant).

### Key Concepts
- `useState` - Track which panel is open
- Conditional rendering - Show/hide content
- Event handling - onClick
- Mapping over arrays - Render multiple panels

### Visual Example
```
┌─────────────────────────┐
│ ▼ Section 1 (Open)      │
├─────────────────────────┤
│ This is content for     │
│ section 1...            │
└─────────────────────────┘
┌─────────────────────────┐
│ ► Section 2 (Closed)    │
└─────────────────────────┘
┌─────────────────────────┐
│ ► Section 3 (Closed)    │
└─────────────────────────┘
```

### Step-by-Step Approach

**Step 1: Design the data structure**
```javascript
const accordionData = [
  {
    id: 1,
    title: "What is React?",
    content: "React is a JavaScript library for building user interfaces."
  },
  {
    id: 2,
    title: "What are hooks?",
    content: "Hooks are functions that let you use state and other React features."
  },
  {
    id: 3,
    title: "What is JSX?",
    content: "JSX is a syntax extension for JavaScript."
  }
];
```

**Step 2: Set up state**
```javascript
// Track which panel is open (by index or id)
const [activeIndex, setActiveIndex] = useState(null);

// For multiple panels open at once (alternate version)
const [openPanels, setOpenPanels] = useState(new Set());
```

**Step 3: Create toggle function**
```javascript
const togglePanel = (index) => {
  // If clicked panel is already open, close it
  if (activeIndex === index) {
    setActiveIndex(null);
  } else {
    setActiveIndex(index);
  }
};
```

**Step 4: Render UI**
```javascript
return (
  <div className="accordion">
    {accordionData.map((item, index) => (
      <div key={item.id} className="accordion-item">
        <div
          className="accordion-header"
          onClick={() => togglePanel(index)}
        >
          <span>{activeIndex === index ? '▼' : '►'}</span>
          <h3>{item.title}</h3>
        </div>
        {activeIndex === index && (
          <div className="accordion-content">
            {item.content}
          </div>
        )}
      </div>
    ))}
  </div>
);
```

### Complete Solution

```javascript
import React, { useState } from 'react';
import './Accordion.css';

const Accordion = ({ data }) => {
  const [activeIndex, setActiveIndex] = useState(null);

  const togglePanel = (index) => {
    setActiveIndex(activeIndex === index ? null : index);
  };

  return (
    <div className="accordion">
      {data.map((item, index) => (
        <div key={item.id} className="accordion-item">
          <button
            className={`accordion-header ${activeIndex === index ? 'active' : ''}`}
            onClick={() => togglePanel(index)}
            aria-expanded={activeIndex === index}
          >
            <span className="accordion-icon">
              {activeIndex === index ? '▼' : '►'}
            </span>
            <span className="accordion-title">{item.title}</span>
          </button>

          {activeIndex === index && (
            <div className="accordion-content">
              <p>{item.content}</p>
            </div>
          )}
        </div>
      ))}
    </div>
  );
};

export default Accordion;
```

### CSS (Accordion.css)
```css
.accordion {
  max-width: 600px;
  margin: 0 auto;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
}

.accordion-item {
  border-bottom: 1px solid #ddd;
}

.accordion-item:last-child {
  border-bottom: none;
}

.accordion-header {
  width: 100%;
  padding: 16px;
  background-color: #f5f5f5;
  border: none;
  text-align: left;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: background-color 0.3s;
}

.accordion-header:hover {
  background-color: #e0e0e0;
}

.accordion-header.active {
  background-color: #007bff;
  color: white;
}

.accordion-icon {
  font-size: 12px;
  transition: transform 0.3s;
}

.accordion-title {
  font-size: 16px;
  font-weight: 500;
}

.accordion-content {
  padding: 16px;
  background-color: white;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

### Variant: Multiple Panels Open

```javascript
const AccordionMultiple = ({ data }) => {
  const [openPanels, setOpenPanels] = useState(new Set());

  const togglePanel = (index) => {
    const newOpenPanels = new Set(openPanels);
    if (newOpenPanels.has(index)) {
      newOpenPanels.delete(index);
    } else {
      newOpenPanels.add(index);
    }
    setOpenPanels(newOpenPanels);
  };

  return (
    <div className="accordion">
      {data.map((item, index) => (
        <div key={item.id} className="accordion-item">
          <button
            className="accordion-header"
            onClick={() => togglePanel(index)}
          >
            <span>{openPanels.has(index) ? '▼' : '►'}</span>
            <span>{item.title}</span>
          </button>
          {openPanels.has(index) && (
            <div className="accordion-content">
              <p>{item.content}</p>
            </div>
          )}
        </div>
      ))}
    </div>
  );
};
```

### Common Mistakes

❌ **Mistake 1: Using array index as key**
```javascript
// Wrong
{data.map((item, index) => (
  <div key={index}> {/* ❌ Index can change */}
```

✅ **Right**
```javascript
// Correct
{data.map((item) => (
  <div key={item.id}> {/* ✓ Stable unique ID */}
```

❌ **Mistake 2: Not handling null state**
```javascript
// Wrong - will show first panel open initially
const [activeIndex, setActiveIndex] = useState(0);
```

✅ **Right**
```javascript
// Correct - all panels closed initially
const [activeIndex, setActiveIndex] = useState(null);
```

❌ **Mistake 3: Missing accessibility**
```javascript
// Wrong
<div onClick={() => togglePanel(index)}>
```

✅ **Right**
```javascript
// Correct - use button for keyboard accessibility
<button
  onClick={() => togglePanel(index)}
  aria-expanded={activeIndex === index}
>
```

### Interview Tips
- ✅ Ask: "Should multiple panels be open at once?"
- ✅ Consider accessibility (ARIA attributes, keyboard navigation)
- ✅ Add smooth animations (CSS transitions)
- ✅ Mention performance: This is efficient as re-renders are minimal

### Time Complexity
- **Toggle operation**: O(1)
- **Rendering**: O(n) where n = number of items

---

## 2. Star Rating Component (Easy)

### Problem Statement
Create a star rating widget (like Amazon reviews). User should be able to click on a star to set rating (1-5). Hovering over stars should preview the rating.

### Key Concepts
- `useState` - Store current rating and hover state
- Event handling - onClick, onMouseEnter, onMouseLeave
- Conditional styling - Filled vs empty stars
- Array mapping - Render 5 stars

### Visual Example
```
Hover over 3rd star:
★ ★ ★ ☆ ☆  (3 stars highlighted)

Click on 4th star:
★ ★ ★ ★ ☆  (4 stars selected)
```

### Step-by-Step Approach

**Step 1: Set up state**
```javascript
const [rating, setRating] = useState(0);      // Current rating (0-5)
const [hover, setHover] = useState(0);        // Hover preview (0-5)
```

**Step 2: Create star rendering logic**
```javascript
const totalStars = 5;

// Determine if a star should be filled
const isFilled = (starIndex) => {
  // If hovering, use hover value; otherwise use rating
  return starIndex <= (hover || rating);
};
```

**Step 3: Handle events**
```javascript
const handleClick = (starIndex) => {
  setRating(starIndex);
};

const handleMouseEnter = (starIndex) => {
  setHover(starIndex);
};

const handleMouseLeave = () => {
  setHover(0);  // Reset hover
};
```

### Complete Solution

```javascript
import React, { useState } from 'react';
import './StarRating.css';

const StarRating = ({
  totalStars = 5,
  initialRating = 0,
  onRatingChange
}) => {
  const [rating, setRating] = useState(initialRating);
  const [hover, setHover] = useState(0);

  const handleClick = (starIndex) => {
    setRating(starIndex);
    if (onRatingChange) {
      onRatingChange(starIndex);
    }
  };

  return (
    <div className="star-rating">
      <div className="stars">
        {[...Array(totalStars)].map((_, index) => {
          const starIndex = index + 1;  // 1-indexed
          const isFilled = starIndex <= (hover || rating);

          return (
            <button
              key={starIndex}
              type="button"
              className={`star ${isFilled ? 'filled' : ''}`}
              onClick={() => handleClick(starIndex)}
              onMouseEnter={() => setHover(starIndex)}
              onMouseLeave={() => setHover(0)}
              aria-label={`Rate ${starIndex} out of ${totalStars}`}
            >
              <span className="star-icon">
                {isFilled ? '★' : '☆'}
              </span>
            </button>
          );
        })}
      </div>
      <p className="rating-text">
        {rating > 0 ? `${rating} out of ${totalStars}` : 'No rating'}
      </p>
    </div>
  );
};

export default StarRating;
```

### CSS
```css
.star-rating {
  display: inline-block;
}

.stars {
  display: flex;
  gap: 4px;
}

.star {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  transition: transform 0.2s;
}

.star:hover {
  transform: scale(1.2);
}

.star-icon {
  font-size: 32px;
  color: #ddd;
  transition: color 0.2s;
}

.star.filled .star-icon {
  color: #ffc107;  /* Gold color */
}

.rating-text {
  margin-top: 8px;
  font-size: 14px;
  color: #666;
}
```

### Advanced: Half-Star Support

```javascript
const StarRatingWithHalf = ({ totalStars = 5, initialRating = 0 }) => {
  const [rating, setRating] = useState(initialRating);
  const [hover, setHover] = useState(0);

  const handleClick = (starIndex, isHalf) => {
    setRating(isHalf ? starIndex - 0.5 : starIndex);
  };

  return (
    <div className="star-rating">
      {[...Array(totalStars)].map((_, index) => {
        const starIndex = index + 1;
        const currentRating = hover || rating;

        // Determine star state: empty, half, or full
        const isFull = starIndex <= currentRating;
        const isHalf = starIndex - 0.5 === currentRating;

        return (
          <div
            key={starIndex}
            className="star-wrapper"
            onMouseLeave={() => setHover(0)}
          >
            {/* Left half */}
            <button
              className="star-half left"
              onClick={() => handleClick(starIndex, true)}
              onMouseEnter={() => setHover(starIndex - 0.5)}
            >
              {isFull || isHalf ? '★' : '☆'}
            </button>

            {/* Right half */}
            <button
              className="star-half right"
              onClick={() => handleClick(starIndex, false)}
              onMouseEnter={() => setHover(starIndex)}
            >
              {isFull ? '★' : '☆'}
            </button>
          </div>
        );
      })}
      <p>Rating: {rating}</p>
    </div>
  );
};
```

### Common Mistakes

❌ **Mistake 1: Forgetting to reset hover on mouse leave**
```javascript
// Wrong - hover stays after mouse leaves
onMouseEnter={() => setHover(starIndex)}
// Missing onMouseLeave!
```

✅ **Right**
```javascript
onMouseEnter={() => setHover(starIndex)}
onMouseLeave={() => setHover(0)}
```

❌ **Mistake 2: Using 0-indexed stars**
```javascript
// Wrong - first star is 0, confusing
{[...Array(5)].map((_, index) => (
  <Star value={index} /> // 0, 1, 2, 3, 4
))}
```

✅ **Right**
```javascript
// Correct - first star is 1
{[...Array(5)].map((_, index) => {
  const starIndex = index + 1;  // 1, 2, 3, 4, 5
  return <Star value={starIndex} />
})}
```

### Interview Tips
- ✅ Ask: "Should we support half stars?"
- ✅ Ask: "Should rating be editable or read-only?"
- ✅ Consider making it a controlled component (accept value and onChange props)
- ✅ Add keyboard support (arrow keys to change rating)

---

## 3. Todo List (Easy)

### Problem Statement
Build a simple todo list application where users can:
- Add new tasks
- Mark tasks as complete/incomplete
- Delete tasks
- (Optional) Edit tasks, filter by status

### Key Concepts
- `useState` with arrays - Managing list of todos
- Controlled components - Input field
- Array methods - map, filter, splice
- Unique keys - Each todo needs a unique ID

### Step-by-Step Approach

**Step 1: Design the todo data structure**
```javascript
{
  id: 1,
  text: "Buy groceries",
  completed: false
}
```

**Step 2: Set up state**
```javascript
const [todos, setTodos] = useState([]);
const [inputValue, setInputValue] = useState('');
```

**Step 3: Implement add function**
```javascript
const addTodo = () => {
  if (inputValue.trim() === '') return;  // Don't add empty todos

  const newTodo = {
    id: Date.now(),  // Simple unique ID
    text: inputValue,
    completed: false
  };

  setTodos([...todos, newTodo]);
  setInputValue('');  // Clear input
};
```

**Step 4: Implement toggle and delete**
```javascript
const toggleTodo = (id) => {
  setTodos(todos.map(todo =>
    todo.id === id ? { ...todo, completed: !todo.completed } : todo
  ));
};

const deleteTodo = (id) => {
  setTodos(todos.filter(todo => todo.id !== id));
};
```

### Complete Solution

```javascript
import React, { useState } from 'react';
import './TodoList.css';

const TodoList = () => {
  const [todos, setTodos] = useState([]);
  const [inputValue, setInputValue] = useState('');

  const addTodo = (e) => {
    e.preventDefault();  // Prevent form submission

    if (inputValue.trim() === '') {
      alert('Please enter a task!');
      return;
    }

    const newTodo = {
      id: Date.now(),  // Or use uuid library for production
      text: inputValue,
      completed: false,
      createdAt: new Date().toISOString()
    };

    setTodos([...todos, newTodo]);
    setInputValue('');
  };

  const toggleTodo = (id) => {
    setTodos(todos.map(todo =>
      todo.id === id
        ? { ...todo, completed: !todo.completed }
        : todo
    ));
  };

  const deleteTodo = (id) => {
    setTodos(todos.filter(todo => todo.id !== id));
  };

  const deleteAllCompleted = () => {
    setTodos(todos.filter(todo => !todo.completed));
  };

  const completedCount = todos.filter(todo => todo.completed).length;
  const activeCount = todos.length - completedCount;

  return (
    <div className="todo-container">
      <h1>Todo List</h1>

      <form onSubmit={addTodo} className="todo-form">
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="What needs to be done?"
          className="todo-input"
        />
        <button type="submit" className="add-btn">
          Add
        </button>
      </form>

      {todos.length > 0 && (
        <div className="todo-stats">
          <span>{activeCount} active</span>
          <span>{completedCount} completed</span>
          {completedCount > 0 && (
            <button
              onClick={deleteAllCompleted}
              className="clear-btn"
            >
              Clear completed
            </button>
          )}
        </div>
      )}

      <ul className="todo-list">
        {todos.length === 0 ? (
          <p className="empty-message">No tasks yet. Add one above!</p>
        ) : (
          todos.map(todo => (
            <li
              key={todo.id}
              className={`todo-item ${todo.completed ? 'completed' : ''}`}
            >
              <input
                type="checkbox"
                checked={todo.completed}
                onChange={() => toggleTodo(todo.id)}
                className="todo-checkbox"
              />
              <span className="todo-text">{todo.text}</span>
              <button
                onClick={() => deleteTodo(todo.id)}
                className="delete-btn"
                aria-label="Delete task"
              >
                ✕
              </button>
            </li>
          ))
        )}
      </ul>
    </div>
  );
};

export default TodoList;
```

### CSS
```css
.todo-container {
  max-width: 600px;
  margin: 40px auto;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.todo-form {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.todo-input {
  flex: 1;
  padding: 12px;
  font-size: 16px;
  border: 2px solid #ddd;
  border-radius: 4px;
}

.todo-input:focus {
  outline: none;
  border-color: #007bff;
}

.add-btn {
  padding: 12px 24px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.add-btn:hover {
  background-color: #0056b3;
}

.todo-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #666;
}

.clear-btn {
  margin-left: auto;
  background: none;
  border: none;
  color: #dc3545;
  cursor: pointer;
  text-decoration: underline;
}

.todo-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.todo-item:hover {
  background-color: #f8f9fa;
}

.todo-item.completed .todo-text {
  text-decoration: line-through;
  color: #999;
}

.todo-checkbox {
  width: 20px;
  height: 20px;
  cursor: pointer;
}

.todo-text {
  flex: 1;
  font-size: 16px;
}

.delete-btn {
  background: none;
  border: none;
  color: #dc3545;
  font-size: 20px;
  cursor: pointer;
  padding: 4px 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.todo-item:hover .delete-btn {
  opacity: 1;
}

.empty-message {
  text-align: center;
  color: #999;
  padding: 40px 0;
}
```

### Advanced: With Filtering

```javascript
const TodoListWithFilters = () => {
  const [todos, setTodos] = useState([]);
  const [inputValue, setInputValue] = useState('');
  const [filter, setFilter] = useState('all');  // 'all', 'active', 'completed'

  // ... (add, toggle, delete functions same as above)

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

  return (
    <div className="todo-container">
      {/* ... form ... */}

      <div className="filter-buttons">
        <button
          onClick={() => setFilter('all')}
          className={filter === 'all' ? 'active' : ''}
        >
          All
        </button>
        <button
          onClick={() => setFilter('active')}
          className={filter === 'active' ? 'active' : ''}
        >
          Active
        </button>
        <button
          onClick={() => setFilter('completed')}
          className={filter === 'completed' ? 'active' : ''}
        >
          Completed
        </button>
      </div>

      <ul className="todo-list">
        {filteredTodos.map(todo => (
          // ... render todo items
        ))}
      </ul>
    </div>
  );
};
```

### Common Mistakes

❌ **Mistake 1: Mutating state directly**
```javascript
// Wrong - mutates state
const addTodo = () => {
  todos.push(newTodo);  // ❌ Direct mutation
  setTodos(todos);
};
```

✅ **Right**
```javascript
// Correct - create new array
const addTodo = () => {
  setTodos([...todos, newTodo]);  // ✓ New array
};
```

❌ **Mistake 2: Using array index as key**
```javascript
// Wrong - keys can change when deleting
{todos.map((todo, index) => (
  <li key={index}>{todo.text}</li>  // ❌
))}
```

✅ **Right**
```javascript
// Correct - use stable unique ID
{todos.map(todo => (
  <li key={todo.id}>{todo.text}</li>  // ✓
))}
```

### Interview Tips
- ✅ Ask: "Should todos persist after page refresh?" (localStorage)
- ✅ Mention: Could use useReducer for complex state logic
- ✅ Consider adding edit functionality
- ✅ Add keyboard shortcuts (Enter to add, Escape to cancel edit)

---

## 4. Contact Form (Easy)

### Problem Statement
Create a contact/feedback form with validation. Should include name, email, message fields with appropriate validation rules.

### Key Concepts
- Controlled components - Form inputs
- Form validation - Email format, required fields
- Error handling - Show validation errors
- Form submission - preventDefault

### Step-by-Step Approach

**Step 1: Set up form state**
```javascript
const [formData, setFormData] = useState({
  name: '',
  email: '',
  message: ''
});

const [errors, setErrors] = useState({});
const [isSubmitting, setIsSubmitting] = useState(false);
```

**Step 2: Handle input changes**
```javascript
const handleChange = (e) => {
  const { name, value } = e.target;
  setFormData(prev => ({
    ...prev,
    [name]: value
  }));

  // Clear error for this field when user types
  if (errors[name]) {
    setErrors(prev => ({
      ...prev,
      [name]: ''
    }));
  }
};
```

**Step 3: Validation logic**
```javascript
const validate = () => {
  const newErrors = {};

  // Name validation
  if (!formData.name.trim()) {
    newErrors.name = 'Name is required';
  } else if (formData.name.trim().length < 2) {
    newErrors.name = 'Name must be at least 2 characters';
  }

  // Email validation
  if (!formData.email.trim()) {
    newErrors.email = 'Email is required';
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
    newErrors.email = 'Invalid email format';
  }

  // Message validation
  if (!formData.message.trim()) {
    newErrors.message = 'Message is required';
  } else if (formData.message.trim().length < 10) {
    newErrors.message = 'Message must be at least 10 characters';
  }

  return newErrors;
};
```

### Complete Solution

```javascript
import React, { useState } from 'react';
import './ContactForm.css';

const ContactForm = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    message: ''
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const validate = () => {
    const newErrors = {};

    // Name validation
    if (!formData.name.trim()) {
      newErrors.name = 'Name is required';
    } else if (formData.name.trim().length < 2) {
      newErrors.name = 'Name must be at least 2 characters';
    }

    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!emailRegex.test(formData.email)) {
      newErrors.email = 'Please enter a valid email';
    }

    // Message validation
    if (!formData.message.trim()) {
      newErrors.message = 'Message is required';
    } else if (formData.message.trim().length < 10) {
      newErrors.message = 'Message must be at least 10 characters';
    }

    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const validationErrors = validate();

    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setIsSubmitting(true);

    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1500));

      console.log('Form submitted:', formData);

      // Reset form
      setFormData({ name: '', email: '', message: '' });
      setSubmitSuccess(true);

      // Clear success message after 3 seconds
      setTimeout(() => setSubmitSuccess(false), 3000);

    } catch (error) {
      setErrors({ submit: 'Something went wrong. Please try again.' });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="contact-form-container">
      <h2>Contact Us</h2>

      {submitSuccess && (
        <div className="success-message">
          Thank you! Your message has been sent successfully.
        </div>
      )}

      <form onSubmit={handleSubmit} className="contact-form" noValidate>
        {/* Name Field */}
        <div className="form-group">
          <label htmlFor="name">
            Name <span className="required">*</span>
          </label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            className={errors.name ? 'error' : ''}
            placeholder="Enter your name"
          />
          {errors.name && (
            <span className="error-message">{errors.name}</span>
          )}
        </div>

        {/* Email Field */}
        <div className="form-group">
          <label htmlFor="email">
            Email <span className="required">*</span>
          </label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            className={errors.email ? 'error' : ''}
            placeholder="your.email@example.com"
          />
          {errors.email && (
            <span className="error-message">{errors.email}</span>
          )}
        </div>

        {/* Message Field */}
        <div className="form-group">
          <label htmlFor="message">
            Message <span className="required">*</span>
          </label>
          <textarea
            id="message"
            name="message"
            value={formData.message}
            onChange={handleChange}
            className={errors.message ? 'error' : ''}
            placeholder="Enter your message..."
            rows="5"
          />
          {errors.message && (
            <span className="error-message">{errors.message}</span>
          )}
          <span className="char-count">
            {formData.message.length} / 500
          </span>
        </div>

        {errors.submit && (
          <div className="error-message submit-error">
            {errors.submit}
          </div>
        )}

        <button
          type="submit"
          className="submit-btn"
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Sending...' : 'Send Message'}
        </button>
      </form>
    </div>
  );
};

export default ContactForm;
```

### CSS
```css
.contact-form-container {
  max-width: 600px;
  margin: 40px auto;
  padding: 30px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.contact-form-container h2 {
  margin-bottom: 24px;
  color: #333;
}

.success-message {
  padding: 12px;
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
  border-radius: 4px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.required {
  color: #dc3545;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border: 2px solid #ddd;
  border-radius: 4px;
  transition: border-color 0.3s;
  font-family: inherit;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #007bff;
}

.form-group input.error,
.form-group textarea.error {
  border-color: #dc3545;
}

.error-message {
  display: block;
  color: #dc3545;
  font-size: 14px;
  margin-top: 4px;
}

.submit-error {
  padding: 12px;
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
  margin-bottom: 16px;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-btn:hover:not(:disabled) {
  background-color: #0056b3;
}

.submit-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}
```

### Common Mistakes

❌ **Mistake 1: Not preventing default form submission**
```javascript
// Wrong - page will reload
const handleSubmit = () => {
  validate();  // ❌ Missing e.preventDefault()
};
```

✅ **Right**
```javascript
const handleSubmit = (e) => {
  e.preventDefault();  // ✓ Prevent page reload
  validate();
};
```

❌ **Mistake 2: Validating on every keystroke**
```javascript
// Wrong - too aggressive
const handleChange = (e) => {
  setFormData({...formData, [e.target.name]: e.target.value});
  validate();  // ❌ Validates while typing
};
```

✅ **Right**
```javascript
// Correct - validate on blur or submit
const handleChange = (e) => {
  setFormData({...formData, [e.target.name]: e.target.value});
  // Clear existing errors, don't validate immediately
};
```

### Interview Tips
- ✅ Discuss different validation strategies (on blur, on submit, on change)
- ✅ Consider using a form library (Formik, React Hook Form) for complex forms
- ✅ Add loading state during submission
- ✅ Implement character count for textarea

---

*Due to length constraints, I'll create a separate section for Medium and Hard problems. Would you like me to continue with the Medium problems (Tabs, Carousel, Debounce, etc.) in the same file or create a Part 2?*

---

## Summary of Easy Problems

### Time Estimates
- **Accordion**: 15-20 minutes
- **Star Rating**: 15-20 minutes
- **Todo List**: 20-25 minutes
- **Contact Form**: 20-25 minutes

### Key Concepts Mastered
✅ useState hook
✅ Event handling
✅ Controlled components
✅ Conditional rendering
✅ Array manipulation
✅ Form validation
✅ CSS styling

### Next Steps
Practice these problems until you can code them from scratch in under 20 minutes. Then move on to Medium problems!

---

**Continue reading for Medium and Hard problems...**
