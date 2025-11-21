# Phase 7: DOM Manipulation and Events

## 📋 Table of Contents
1. [What is the DOM?](#what-is-the-dom)
2. [Selecting Elements](#selecting-elements)
3. [Modifying Elements](#modifying-elements)
4. [Creating and Removing Elements](#creating-and-removing-elements)
5. [Events](#events)
6. [Event Delegation](#event-delegation)

---

## What is the DOM?

### The Why
To interact with and manipulate web pages dynamically.

### The What
DOM (Document Object Model) is a tree-like representation of an HTML document.

**Diagram:**
```
┌──────────────────────────────────────────────┐
│              HTML                            │
│  <html>                                      │
│    <body>                                    │
│      <div id="container">                    │
│        <h1>Title</h1>                        │
│        <p>Text</p>                           │
│      </div>                                  │
│    </body>                                   │
│  </html>                                     │
└──────────────────────────────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────────┐
│              DOM TREE                        │
│                                              │
│           document                           │
│              │                               │
│           <html>                             │
│              │                               │
│           <body>                             │
│              │                               │
│         <div id="container">                 │
│          /          \                        │
│      <h1>          <p>                       │
│    "Title"       "Text"                      │
│                                              │
└──────────────────────────────────────────────┘
```

---

## Selecting Elements

### Methods to Select Elements

```javascript
// By ID
const element = document.getElementById('myId');

// By class name (returns HTMLCollection)
const elements = document.getElementsByClassName('myClass');

// By tag name
const paragraphs = document.getElementsByTagName('p');

// ✅ Modern: Query selector (single element)
const element2 = document.querySelector('.myClass');
const element3 = document.querySelector('#myId');
const element4 = document.querySelector('div > p');

// ✅ Modern: Query selector all (NodeList)
const elements2 = document.querySelectorAll('.myClass');
const elements3 = document.querySelectorAll('p');
```

**Comparison:**
```
┌────────────────────────────────────────────┐
│       SELECTING METHODS                    │
├───────────────────┬────────────────────────┤
│ Old Way           │ Modern Way (Preferred) │
├───────────────────┼────────────────────────┤
│ getElementById    │ querySelector          │
│ getElementsByClass│ querySelectorAll       │
│ getElementsByTag  │ querySelectorAll       │
├───────────────────┼────────────────────────┤
│ HTMLCollection    │ NodeList               │
│ (Live)            │ (Static)               │
└───────────────────┴────────────────────────┘
```

### Traversing the DOM

```javascript
const element = document.querySelector('#myDiv');

// Parent
element.parentElement
element.parentNode

// Children
element.children           // HTMLCollection
element.firstElementChild
element.lastElementChild

// Siblings
element.nextElementSibling
element.previousElementSibling

// All descendants
element.querySelectorAll('p')
```

---

## Modifying Elements

### Changing Content

```javascript
const element = document.querySelector('#myDiv');

// Text content (safer)
element.textContent = "New text";

// HTML content (can execute scripts - be careful!)
element.innerHTML = "<p>New <strong>HTML</strong></p>";

// Value (for inputs)
const input = document.querySelector('#myInput');
input.value = "New value";
```

**textContent vs innerHTML:**
```
┌────────────────────────────────────────────┐
│     textContent vs innerHTML               │
├───────────────────┬────────────────────────┤
│ textContent       │ innerHTML              │
├───────────────────┼────────────────────────┤
│ Just text         │ Parses HTML tags       │
│ Faster            │ Slower                 │
│ ✅ Safe           │ ⚠️ XSS risk           │
│ <p>Hi</p> → text  │ <p>Hi</p> → paragraph  │
└───────────────────┴────────────────────────┘
```

---

### Changing Styles

```javascript
const element = document.querySelector('#myDiv');

// Inline styles
element.style.color = "red";
element.style.backgroundColor = "blue";
element.style.fontSize = "20px";

// Multiple styles
Object.assign(element.style, {
    color: "white",
    backgroundColor: "black",
    padding: "10px"
});

// Get computed style
const styles = window.getComputedStyle(element);
console.log(styles.color);
```

---

### Changing Classes

```javascript
const element = document.querySelector('#myDiv');

// Add class
element.classList.add('active');

// Remove class
element.classList.remove('inactive');

// Toggle class
element.classList.toggle('visible');

// Check if has class
if (element.classList.contains('active')) {
    console.log('Element is active');
}

// Replace class
element.classList.replace('old-class', 'new-class');
```

---

### Changing Attributes

```javascript
const link = document.querySelector('a');

// Get attribute
const href = link.getAttribute('href');

// Set attribute
link.setAttribute('href', 'https://example.com');
link.setAttribute('target', '_blank');

// Remove attribute
link.removeAttribute('target');

// Check if has attribute
if (link.hasAttribute('href')) {
    console.log('Link has href');
}

// Data attributes
const element = document.querySelector('#myDiv');
element.dataset.userId = '123';
element.dataset.role = 'admin';

console.log(element.dataset.userId);  // "123"
```

---

## Creating and Removing Elements

### Creating Elements

```javascript
// Create element
const div = document.createElement('div');
div.textContent = "Hello";
div.classList.add('myClass');

// Append to DOM
document.body.appendChild(div);

// Insert at specific position
const parent = document.querySelector('#parent');

// End of parent
parent.appendChild(div);

// Beginning of parent
parent.prepend(div);

// Before element
parent.before(div);

// After element
parent.after(div);

// Insert at specific position
const referenceNode = document.querySelector('#reference');
parent.insertBefore(div, referenceNode);
```

---

### Removing Elements

```javascript
const element = document.querySelector('#myDiv');

// Remove element
element.remove();

// Remove child (old way)
const parent = document.querySelector('#parent');
const child = document.querySelector('#child');
parent.removeChild(child);

// Remove all children
parent.innerHTML = '';  // Fast but may leak memory
// Or
while (parent.firstChild) {
    parent.removeChild(parent.firstChild);
}
```

---

### Cloning Elements

```javascript
const original = document.querySelector('#original');

// Shallow clone (no children)
const shallowClone = original.cloneNode(false);

// Deep clone (with children)
const deepClone = original.cloneNode(true);

document.body.appendChild(deepClone);
```

---

## Events

### The Why
To respond to user interactions (clicks, typing, mouse movement, etc.).

### Adding Event Listeners

```javascript
const button = document.querySelector('#myButton');

// Add event listener
button.addEventListener('click', function(event) {
    console.log('Button clicked!');
    console.log('Event:', event);
});

// Arrow function
button.addEventListener('click', (event) => {
    console.log('Clicked!');
});

// Named function (can be removed later)
function handleClick(event) {
    console.log('Clicked!');
}
button.addEventListener('click', handleClick);

// Remove event listener
button.removeEventListener('click', handleClick);
```

---

### Common Events

```javascript
// Mouse events
element.addEventListener('click', handler);
element.addEventListener('dblclick', handler);
element.addEventListener('mouseenter', handler);
element.addEventListener('mouseleave', handler);
element.addEventListener('mousemove', handler);
element.addEventListener('mousedown', handler);
element.addEventListener('mouseup', handler);

// Keyboard events
document.addEventListener('keydown', handler);
document.addEventListener('keyup', handler);
document.addEventListener('keypress', handler);

// Form events
form.addEventListener('submit', handler);
input.addEventListener('input', handler);
input.addEventListener('change', handler);
input.addEventListener('focus', handler);
input.addEventListener('blur', handler);

// Window events
window.addEventListener('load', handler);
window.addEventListener('resize', handler);
window.addEventListener('scroll', handler);
```

---

### Event Object

```javascript
button.addEventListener('click', (event) => {
    // Event properties
    console.log(event.type);          // "click"
    console.log(event.target);        // Element clicked
    console.log(event.currentTarget); // Element with listener

    // Mouse position
    console.log(event.clientX);       // X relative to viewport
    console.log(event.clientY);       // Y relative to viewport

    // Keyboard
    console.log(event.key);           // Key pressed
    console.log(event.code);          // Physical key

    // Prevent default behavior
    event.preventDefault();

    // Stop propagation
    event.stopPropagation();
});
```

---

### Event Propagation

**Diagram:**
```
┌──────────────────────────────────────────────┐
│       EVENT PROPAGATION                      │
│                                              │
│  ┌────────────────────────────────────────┐ │
│  │ document        (3) ▲                  │ │
│  │  └─ body         (2) ▲                 │ │
│  │      └─ div      (1) ▲ BUBBLING        │ │
│  │          └─ button ● CLICK             │ │
│  │              │                          │ │
│  │              ▼ (1) CAPTURING           │ │
│  │          div ▼ (2)                     │ │
│  │      body    ▼ (3)                     │ │
│  │  document                              │ │
│  └────────────────────────────────────────┘ │
│                                              │
│  1. Capturing: document → target           │
│  2. Target: event fires on target          │
│  3. Bubbling: target → document (default)  │
└──────────────────────────────────────────────┘
```

```javascript
// Bubbling (default)
element.addEventListener('click', handler);

// Capturing
element.addEventListener('click', handler, true);

// Stop propagation
element.addEventListener('click', (event) => {
    event.stopPropagation();  // Stops bubbling
});
```

---

## Event Delegation

### The Why
To handle events efficiently for multiple elements.

### The What
Add one listener to a parent instead of many listeners to children.

```javascript
// ❌ Bad: Add listener to each item
const items = document.querySelectorAll('.item');
items.forEach(item => {
    item.addEventListener('click', handleClick);
});

// ✅ Good: Add one listener to parent
const list = document.querySelector('#list');
list.addEventListener('click', (event) => {
    // Check if clicked element is an item
    if (event.target.classList.contains('item')) {
        handleClick(event);
    }
});
```

**Diagram:**
```
┌──────────────────────────────────────────────┐
│      Without Delegation                      │
│                                              │
│  ul                                          │
│   ├─ li ← listener                           │
│   ├─ li ← listener                           │
│   ├─ li ← listener                           │
│   ├─ li ← listener                           │
│   └─ li ← listener                           │
│                                              │
│  Problem: Many listeners, doesn't work for   │
│  dynamically added elements                  │
└──────────────────────────────────────────────┘

┌──────────────────────────────────────────────┐
│      With Delegation                         │
│                                              │
│  ul ← ONE listener                           │
│   ├─ li                                      │
│   ├─ li                                      │
│   ├─ li                                      │
│   ├─ li                                      │
│   └─ li                                      │
│                                              │
│  Solution: One listener, works for           │
│  dynamically added elements                  │
└──────────────────────────────────────────────┘
```

---

## Practical Examples

### Example 1: Todo List (Medium)

```javascript
const todoList = document.querySelector('#todoList');
const todoInput = document.querySelector('#todoInput');
const addButton = document.querySelector('#addButton');

// Add todo
addButton.addEventListener('click', () => {
    const text = todoInput.value.trim();

    if (text) {
        const li = document.createElement('li');
        li.innerHTML = `
            <span>${text}</span>
            <button class="delete">Delete</button>
        `;
        todoList.appendChild(li);
        todoInput.value = '';
    }
});

// Delete todo (event delegation)
todoList.addEventListener('click', (event) => {
    if (event.target.classList.contains('delete')) {
        event.target.parentElement.remove();
    }
});

// Toggle complete
todoList.addEventListener('click', (event) => {
    if (event.target.tagName === 'SPAN') {
        event.target.parentElement.classList.toggle('completed');
    }
});
```

---

### Example 2: Form Validation (Medium)

```javascript
const form = document.querySelector('#myForm');
const email = document.querySelector('#email');
const password = document.querySelector('#password');

form.addEventListener('submit', (event) => {
    event.preventDefault();

    let isValid = true;

    // Validate email
    if (!email.value.includes('@')) {
        showError(email, 'Invalid email');
        isValid = false;
    } else {
        clearError(email);
    }

    // Validate password
    if (password.value.length < 8) {
        showError(password, 'Password must be 8+ characters');
        isValid = false;
    } else {
        clearError(password);
    }

    if (isValid) {
        console.log('Form submitted!');
        form.submit();
    }
});

function showError(input, message) {
    const formGroup = input.parentElement;
    formGroup.classList.add('error');

    const error = formGroup.querySelector('.error-message');
    if (error) {
        error.textContent = message;
    } else {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-message';
        errorDiv.textContent = message;
        formGroup.appendChild(errorDiv);
    }
}

function clearError(input) {
    const formGroup = input.parentElement;
    formGroup.classList.remove('error');

    const error = formGroup.querySelector('.error-message');
    if (error) {
        error.remove();
    }
}
```

---

### Example 3: Modal (Hard)

```javascript
const modal = document.querySelector('#modal');
const openBtn = document.querySelector('#openModal');
const closeBtn = document.querySelector('#closeModal');
const overlay = document.querySelector('#overlay');

// Open modal
function openModal() {
    modal.classList.add('active');
    overlay.classList.add('active');
    document.body.style.overflow = 'hidden';
}

// Close modal
function closeModal() {
    modal.classList.remove('active');
    overlay.classList.remove('active');
    document.body.style.overflow = '';
}

// Event listeners
openBtn.addEventListener('click', openModal);
closeBtn.addEventListener('click', closeModal);
overlay.addEventListener('click', closeModal);

// Close on Escape key
document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && modal.classList.contains('active')) {
        closeModal();
    }
});
```

---

## Key Takeaways

✅ Use querySelector/querySelectorAll for selecting elements
✅ Prefer textContent over innerHTML for security
✅ Use classList for managing classes
✅ Event delegation improves performance
✅ Always preventDefault() for forms when handling with JS
✅ Remove event listeners when no longer needed

---

## Next Steps

🎉 **Congratulations!** You've mastered DOM manipulation!

**Ready for more?** Move on to [Phase 8: Common Mistakes](../phase-08-common-mistakes/README.md)
