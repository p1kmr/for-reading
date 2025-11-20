# React Frontend Interview Questions - Complete Guide

> A comprehensive collection of React coding interview questions with detailed explanations, diagrams, and best practices

## 📚 Table of Contents

- [Overview](#overview)
- [How to Use This Resource](#how-to-use-this-resource)
- [Question Categories](#question-categories)
- [Learning Path](#learning-path)
- [Quick Reference](#quick-reference)

---

## Overview

This repository contains **real-world React interview questions** asked at top tech companies. Each question includes:

✅ **Complete working implementation**
✅ **Step-by-step approach guide**
✅ **Detailed code comments explaining "why"**
✅ **Multiple diagrams (state flow, component hierarchy, algorithms)**
✅ **Hook explanations (useState, useEffect, useMemo, useCallback, etc.)**
✅ **Common beginner mistakes and solutions**
✅ **Interviewer Q&A with model answers**
✅ **Optimization techniques**
✅ **Related questions for practice**

**Focus:** Functionality first, minimal essential CSS

---

## How to Use This Resource

### 1. Start with the Guide
Read [`How-to-approach-frontend-interviews.md`](./How-to-approach-frontend-interviews.md) first to understand:
- Interview structure and timing
- How to think through problems
- React hooks deep dive
- Common patterns and mistakes
- Problem-solving framework

### 2. Follow the Learning Path
Work through questions in order:
1. **Easy** - Build fundamentals
2. **Medium** - Add complexity (coming soon)
3. **Hard** - Master advanced patterns

### 3. Practice Actively
- **Don't just read** - Code each solution from scratch
- **Use the approach section** - Think before coding
- **Review mistakes section** - Learn what to avoid
- **Answer interviewer questions** - Practice explaining your code

### 4. Time Yourself
Simulate real interviews:
- Easy: 20-25 minutes
- Medium: 30-35 minutes
- Hard: 40-45 minutes

---

## Question Categories

### 📗 Easy (Beginner-Friendly)

Perfect for learning React fundamentals and basic state management.

| Question | Key Concepts | Time |
|----------|-------------|------|
| [Counter App](./Easy/counter-app.md) | useState, event handlers, conditional disabling | 20 min |
| [Todo List (Basic)](./Easy/todo-list-basic.md) | Array state, map/filter, form handling, keys | 25 min |
| [Search Filter](./Easy/search-filter.md) | Derived state, array filtering, controlled inputs | 20 min |
| [Accordion](./Easy/accordion.md) | Conditional rendering, toggle state, component composition | 20 min |

**Focus:**
- useState basics
- Event handlers
- Array manipulation (map, filter)
- Conditional rendering
- Form handling

---

### 📘 Medium (Intermediate)

Coming soon! Will include:
- Debounced Search
- Infinite Scroll
- Pagination
- Multi-step Forms
- Autocomplete with API
- Tabs Component

**Focus:**
- useEffect with cleanup
- API integration
- Debouncing/throttling
- More complex state
- Performance basics

---

### 📕 Hard (Advanced)

Complex components requiring advanced React patterns and algorithms.

| Question | Key Concepts | Time |
|----------|-------------|------|
| [Nested Checkboxes](./Hard/nested-checkboxes.md) | Recursive components, tree traversal, useRef, indeterminate state | 45 min |
| [Calendar/Date Picker](./Hard/calendar-date-picker.md) | Date API, grid calculations, useMemo, edge cases (leap years) | 40 min |
| [Connect Four Game](./Hard/connect-four-game.md) | 2D arrays, game logic, win detection algorithm, immutability | 45 min |
| [Poll/Quiz Widget](./Hard/poll-quiz-widget.md) | localStorage, percentage calculations, progress bars, vote management | 40 min |
| [Undo/Redo](./Hard/undo-redo-functionality.md) | Custom hooks, history management, keyboard events, time-travel | 40 min |

**Focus:**
- Complex state management
- Recursive logic
- Algorithm implementation
- Custom hooks
- Performance optimization
- Browser APIs (localStorage, keyboard events)

---

## Learning Path

### Path 1: Complete Beginner
```
1. Read approach guide
2. Counter App (understand useState)
3. Todo List (arrays and state)
4. Search Filter (derived state)
5. Accordion (conditional rendering)
```

### Path 2: Interview Prep (1 Week)
```
Week Plan:
Day 1-2: All Easy questions (build foundation)
Day 3-4: Medium questions (apply knowledge)
Day 5-6: Hard questions (master patterns)
Day 7: Review and mock interview
```

### Path 3: Topic-Based
```
State Management:
- Counter → Todo List → Nested Checkboxes

Forms & Inputs:
- Search Filter → Poll Widget

Algorithms:
- Connect Four → Calendar

Advanced Patterns:
- Undo/Redo → Nested Checkboxes
```

---

## Quick Reference

### React Hooks Cheat Sheet

**useState**
```jsx
const [state, setState] = useState(initialValue)

// Update
setState(newValue)
// or
setState(prev => newValue)
```
**When?** Any value that affects rendering

---

**useEffect**
```jsx
useEffect(() => {
  // Side effect here

  return () => {
    // Cleanup
  }
}, [dependencies])
```
**When?** Side effects (API calls, subscriptions, DOM manipulation)

---

**useMemo**
```jsx
const value = useMemo(() => expensiveCalculation(a, b), [a, b])
```
**When?** Expensive calculations, prevent recalculations

---

**useCallback**
```jsx
const fn = useCallback(() => doSomething(a), [a])
```
**When?** Memoize functions passed to children

---

**useRef**
```jsx
const ref = useRef(initialValue)
// Access: ref.current
```
**When?** DOM access, persist values without re-render

---

### Common Patterns

**Controlled Input**
```jsx
const [value, setValue] = useState('')
<input value={value} onChange={e => setValue(e.target.value)} />
```

**List Rendering**
```jsx
{items.map(item => (
  <Item key={item.id} data={item} />
))}
```

**Conditional Rendering**
```jsx
{condition && <Component />}
{condition ? <A /> : <B />}
```

**Form Submission**
```jsx
<form onSubmit={(e) => {
  e.preventDefault()
  handleSubmit()
}}>
```

---

## Key Principles

### 1. Immutability
```jsx
// ❌ Wrong
array.push(item)
setState(array)

// ✅ Right
setState([...array, item])
```

### 2. Derived State
```jsx
// ❌ Wrong - Duplicate state
const [items, setItems] = useState([])
const [count, setCount] = useState(0)

// ✅ Right - Compute from items
const count = items.length
```

### 3. Keys in Lists
```jsx
// ❌ Wrong
{items.map((item, index) => <Item key={index} />)}

// ✅ Right
{items.map(item => <Item key={item.id} />)}
```

---

## Interview Tips

### Before Coding
1. **Understand the problem** - Ask clarifying questions
2. **Plan your approach** - Sketch component structure
3. **Identify state** - What data changes?
4. **Discuss trade-offs** - Why this approach?

### While Coding
1. **Think out loud** - Explain your reasoning
2. **Start simple** - Basic version first
3. **Use meaningful names** - Clear variable/function names
4. **Add comments** - Explain complex logic
5. **Test as you go** - Don't wait until the end

### After Coding
1. **Test edge cases** - Empty states, errors
2. **Discuss optimizations** - Performance improvements
3. **Explain trade-offs** - Why you chose this solution
4. **Ask questions** - Show interest in feedback

---

## Common Beginner Mistakes

| Mistake | Why Wrong | Solution |
|---------|-----------|----------|
| `array.push()` then `setState(array)` | Mutates state | `setState([...array, item])` |
| Missing dependencies in `useEffect` | Stale data | Include all dependencies |
| Using index as key | Breaks on reorder | Use unique `id` |
| `indeterminate` as prop | Not a React prop | Use `ref` + `useEffect` |
| Storing derived state | Sync issues | Compute from source |
| Not preventing default on forms | Page reloads | `e.preventDefault()` |

---

## Performance Optimization

### When to Optimize

**Profile first!** - Don't optimize prematurely

Signs you need optimization:
- Lag when typing in inputs
- Slow list rendering (1000+ items)
- Unnecessary re-renders
- Heavy calculations on every render

### Optimization Techniques

1. **React.memo** - Prevent component re-renders
```jsx
export default React.memo(MyComponent)
```

2. **useCallback** - Memoize functions
```jsx
const handleClick = useCallback(() => {
  doSomething(id)
}, [id])
```

3. **useMemo** - Memoize values
```jsx
const filtered = useMemo(() =>
  items.filter(item => item.active),
  [items]
)
```

4. **Virtualization** - Render only visible items
```jsx
import { FixedSizeList } from 'react-window'
```

5. **Debouncing** - Delay expensive operations
```jsx
const debouncedValue = useDebounce(value, 300)
```

---

## Resources

### Official Documentation
- [React Docs](https://react.dev)
- [React Hooks](https://react.dev/reference/react)

### Practice Platforms
- [GreatFrontEnd](https://www.greatfrontend.com)
- [LeetCode](https://leetcode.com/problemset/all/?page=1&topicSlugs=react)
- [Frontend Mentor](https://www.frontendmentor.io)

### Additional Learning
- [JavaScript.info](https://javascript.info)
- [MDN Web Docs](https://developer.mozilla.org)

---

## File Structure

```
Frontend-Interview-Questions/
├── README.md (this file)
├── How-to-approach-frontend-interviews.md
│
├── Easy/
│   ├── counter-app.md
│   ├── todo-list-basic.md
│   ├── search-filter.md
│   └── accordion.md
│
├── Medium/ (coming soon)
│   ├── debounced-search.md
│   ├── infinite-scroll.md
│   ├── pagination.md
│   └── ...
│
└── Hard/
    ├── nested-checkboxes.md
    ├── calendar-date-picker.md
    ├── connect-four-game.md
    ├── poll-quiz-widget.md
    └── undo-redo-functionality.md
```

---

## Contributing

Found an error? Have a suggestion? Want to add a question?
- Open an issue
- Submit a pull request
- Share feedback

---

## License

This resource is for educational purposes. Feel free to use for interview preparation.

---

## Acknowledgments

Questions and patterns inspired by real interviews at:
- Meta/Facebook
- Google
- Amazon
- Microsoft
- Startups

---

## Final Thoughts

**Remember:**
- Interviewers care about your **thinking process** more than perfect code
- **Communication** is as important as coding
- **Ask questions** - it shows you think critically
- **Practice** - build each question from scratch multiple times
- **Understand** - don't just memorize

Good luck with your interviews! 🚀

---

**Star this repo if you found it helpful!**
