# Accordion Component - Easy Level

## Problem Statement

Create an accordion component where:
- Multiple sections with title and content
- Click title to expand/collapse content
- Only one section open at a time (optional: allow multiple)
- Smooth expand/collapse animation (optional)

## Difficulty: Easy ⭐

## Key Concepts Tested
- useState for tracking open section
- Conditional rendering
- Click handlers
- Component composition

---

## Quick Approach

**State needed:** Which section is currently open (index or id)
**Logic:** Click toggles section (if same, close; if different, open new)

---

## Implementation

```jsx
import React, { useState } from 'react'

function Accordion() {
  // DATA
  const sections = [
    {
      id: 1,
      title: 'What is React?',
      content: 'React is a JavaScript library for building user interfaces.'
    },
    {
      id: 2,
      title: 'What is JSX?',
      content: 'JSX is a syntax extension for JavaScript that looks like HTML.'
    },
    {
      id: 3,
      title: 'What are Hooks?',
      content: 'Hooks are functions that let you use React features in functional components.'
    }
  ]

  /**
   * STATE
   * Why use ID instead of index?
   * - More reliable if sections reorder
   * - Can handle dynamic sections
   *
   * Why null for closed?
   * - Falsy value, easy to check
   * - Distinguishes from id: 0
   */
  const [openSectionId, setOpenSectionId] = useState(null)

  /**
   * Toggle handler
   * Why check if already open?
   * - Clicking open section should close it
   * - Click-to-toggle UX pattern
   */
  const handleToggle = (id) => {
    setOpenSectionId(openSectionId === id ? null : id)
    // If same: close (null)
    // If different: open new one
  }

  return (
    <div className="accordion">
      {sections.map(section => {
        const isOpen = openSectionId === section.id

        return (
          <div key={section.id} className="accordion-item">
            {/* HEADER - Always visible */}
            <button
              className={`accordion-header ${isOpen ? 'active' : ''}`}
              onClick={() => handleToggle(section.id)}
            >
              {section.title}
              <span className="icon">{isOpen ? '−' : '+'}</span>
            </button>

            {/* CONTENT - Conditionally visible */}
            {isOpen && (
              <div className="accordion-content">
                {section.content}
              </div>
            )}
          </div>
        )
      })}
    </div>
  )
}

export default Accordion
```

---

## Variation: Multiple Sections Open

```jsx
function AccordionMultiple() {
  /**
   * Why Set or Array?
   * - Need to track MULTIPLE open sections
   * - Array: [1, 3] means sections 1 and 3 are open
   * - Set: faster lookups for large lists
   */
  const [openSections, setOpenSections] = useState([])

  const handleToggle = (id) => {
    setOpenSections(prev =>
      prev.includes(id)
        ? prev.filter(sectionId => sectionId !== id) // Remove if open
        : [...prev, id] // Add if closed
    )
  }

  const isOpen = (id) => openSections.includes(id)

  return (
    <div className="accordion">
      {sections.map(section => (
        <div key={section.id} className="accordion-item">
          <button onClick={() => handleToggle(section.id)}>
            {section.title}
            <span>{isOpen(section.id) ? '−' : '+'}</span>
          </button>

          {isOpen(section.id) && (
            <div className="accordion-content">
              {section.content}
            </div>
          )}
        </div>
      ))}
    </div>
  )
}
```

---

## CSS

```css
.accordion {
  max-width: 600px;
  margin: 20px auto;
}

.accordion-item {
  border: 1px solid #ddd;
  margin-bottom: 8px;
  border-radius: 4px;
  overflow: hidden;
}

.accordion-header {
  width: 100%;
  padding: 16px;
  background: #f5f5f5;
  border: none;
  text-align: left;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  transition: background 0.2s;
}

.accordion-header:hover {
  background: #e0e0e0;
}

.accordion-header.active {
  background: #2196F3;
  color: white;
}

.accordion-content {
  padding: 16px;
  background: white;
  animation: slideDown 0.3s ease;
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

.icon {
  font-size: 20px;
  font-weight: bold;
}
```

---

## Beginner Mistakes

### ❌ Mistake: Using boolean for each section
```jsx
// WRONG - Separate state for each
const [isOpen1, setIsOpen1] = useState(false)
const [isOpen2, setIsOpen2] = useState(false)
// Doesn't scale!

// RIGHT - Single state
const [openSectionId, setOpenSectionId] = useState(null)
```

---

## Key Takeaways

✅ Use single state to track active section
✅ Conditional rendering for content
✅ Toggle logic: same = close, different = open
✅ CSS transitions for smooth UX

---

## Related: FAQ Accordion, Expandable Sections
