# ReactJS Frontend Coding Interview Questions

> Comprehensive guide for React frontend interviews with detailed explanations, diagrams, and best practices

## 📚 Table of Contents

- [Overview](#overview)
- [How to Use This Guide](#how-to-use-this-guide)
- [Difficulty Levels](#difficulty-levels)
- [Key Concepts Covered](#key-concepts-covered)
- [Study Path](#study-path)

---

## Overview

This repository contains comprehensive React interview questions organized by difficulty level. Each question includes:

✅ **Detailed code examples** with extensive comments
✅ **Architecture diagrams** (Mermaid) to visualize concepts
✅ **How to think and approach** the problem
✅ **Common beginner mistakes** and solutions
✅ **Interviewer Q&A** sections
✅ **Hook explanations** (useState, useEffect, useMemo, useCallback, etc.)
✅ **Performance considerations**

**Focus**: Functionality over CSS. Essential styling is included, but the emphasis is on React concepts and JavaScript logic.

---

## How to Use This Guide

### For Interview Preparation

1. **Start with Easy**: Master fundamentals before moving to complex topics
2. **Read the comments**: Every line is explained with WHY and HOW
3. **Study the diagrams**: Visual understanding is crucial
4. **Practice the mistakes**: Learn what NOT to do
5. **Answer the Q&A**: Test your understanding

### For Interviewers

- Questions are **production-ready** scenarios
- Each level tests **specific competencies**
- **Detailed rubrics** in comments show what to look for
- Can be used as **take-home assignments** or **live coding**

### For Learners

- **Beginner-friendly explanations**: No jargon without explanation
- **Progressive complexity**: Builds on previous concepts
- **Real-world examples**: Practical, not theoretical
- **Multiple approaches**: Learn different patterns

---

## Difficulty Levels

### 🟢 Easy Level
**File**: [`easy/ReactJS-Easy-Interview-Questions.md`](./easy/ReactJS-Easy-Interview-Questions.md)

**Topics Covered**:
- ✅ Counter Component (useState basics)
- ✅ Toggle Component (boolean state, conditional rendering)
- ✅ Todo List (array state, CRUD operations)
- ✅ Simple Form with Validation
- ✅ Controlled Input Component

**Key Concepts**:
- `useState` hook
- Event handling
- Conditional rendering (`&&`, ternary)
- Form handling
- Controlled components
- Array methods (map, filter)

**Time to Complete**: 4-6 hours
**Suitable For**: Junior developers, bootcamp graduates, career switchers

---

### 🟡 Medium Level
**File**: [`medium/ReactJS-Medium-Interview-Questions.md`](./medium/ReactJS-Medium-Interview-Questions.md)

**Topics Covered**:
- ✅ Tabs Component (state management, composition)
- ✅ Image Carousel / Slider (useEffect, intervals, cleanup)
- ✅ Search with Debounce (performance optimization)
- ✅ Infinite Scrolling List (Intersection Observer, pagination)
- ✅ Autocomplete / Typeahead (keyboard navigation, accessibility)
- ✅ Data Table with Sorting & Filtering (useMemo, array sorting)
- ⏳ Modal Dialog (portals, focus management)
- ⏳ File Explorer / Tree View (recursion, tree structures)

**Key Concepts**:
- `useEffect` hook (side effects, cleanup)
- `useRef` hook (DOM access, persisting values)
- `useMemo` hook (memoization)
- `useCallback` hook (function memoization)
- Debouncing technique
- Intersection Observer API
- Keyboard accessibility
- Performance optimization

**Time to Complete**: 12-15 hours
**Suitable For**: Mid-level developers, 1-3 years React experience

---

### 🔴 Hard Level
**File**: [`hard/ReactJS-Hard-Interview-Questions.md`](./hard/ReactJS-Hard-Interview-Questions.md)

**Topics Covered**:
- ✅ Custom Hook: useFetch with Caching
- ✅ Compound Component Pattern
- ✅ Performance Optimization (React.memo, useCallback, useMemo)
- ✅ Context API with useReducer (global state)
- ⏳ Advanced Form with Field Arrays (dynamic forms)

**Key Concepts**:
- Custom hooks creation
- AbortController (fetch cancellation)
- Caching strategies
- Compound component pattern
- Context API
- `useReducer` hook
- React.memo (component memoization)
- Advanced performance techniques
- Design patterns

**Time to Complete**: 20+ hours
**Suitable For**: Senior developers, 3+ years React experience, architectural roles

---

## Key Concepts Covered

### Hooks Deep Dive

#### useState
```jsx
const [state, setState] = useState(initialValue);
```
- State management in functional components
- Functional updates: `setState(prev => prev + 1)`
- Multiple state variables
- **Covered in**: Easy level (Counter, Toggle, Todo)

#### useEffect
```jsx
useEffect(() => {
  // Effect code
  return () => {
    // Cleanup code
  };
}, [dependencies]);
```
- Side effects (API calls, subscriptions, timers)
- Cleanup functions (prevent memory leaks)
- Dependency arrays
- **Covered in**: Medium level (Carousel, Debounce, Infinite Scroll)

#### useRef
```jsx
const ref = useRef(initialValue);
ref.current = newValue;
```
- DOM access
- Persisting values across renders
- No re-render on change
- **Covered in**: Medium level (Infinite Scroll, Autocomplete)

#### useMemo
```jsx
const memoizedValue = useMemo(() => expensiveCalculation(), [deps]);
```
- Memoize expensive calculations
- Prevent unnecessary recalculations
- Performance optimization
- **Covered in**: Medium (Data Table), Hard (Performance)

#### useCallback
```jsx
const memoizedFn = useCallback(() => { /* ... */ }, [deps]);
```
- Memoize function references
- Prevent child re-renders
- Stable callbacks for useEffect
- **Covered in**: Medium (Infinite Scroll), Hard (Performance)

#### useReducer
```jsx
const [state, dispatch] = useReducer(reducer, initialState);
```
- Complex state logic
- Multiple sub-values
- Predictable state updates
- **Covered in**: Hard (Context API)

---

## Study Path

### Week 1-2: Foundations (Easy Level)
**Goal**: Master React basics

1. **Day 1-2**: Counter & Toggle
   - Learn useState
   - Understand re-rendering
   - Event handling

2. **Day 3-5**: Todo List
   - Array state management
   - Controlled components
   - List rendering with keys

3. **Day 6-7**: Forms
   - Form validation
   - Multiple inputs
   - Error handling

**Practice**: Build small apps using these concepts

---

### Week 3-4: Intermediate (Medium Level)
**Goal**: Master hooks and performance

1. **Day 8-10**: Tabs & Carousel
   - useEffect for side effects
   - Cleanup functions
   - Interval management

2. **Day 11-13**: Debounce & Infinite Scroll
   - Performance optimization
   - Intersection Observer
   - useCallback basics

3. **Day 14-15**: Autocomplete & Data Table
   - Keyboard navigation
   - useMemo for expensive operations
   - ARIA accessibility

**Practice**: Combine concepts in a mini-project

---

### Week 5+: Advanced (Hard Level)
**Goal**: Master advanced patterns

1. **Week 5**: Custom Hooks
   - Hook composition
   - Reusable logic
   - Testing hooks

2. **Week 6**: Patterns & Performance
   - Compound components
   - React.memo optimization
   - Render optimization

3. **Week 7**: State Management
   - Context API
   - useReducer pattern
   - Global state

**Practice**: Build a full application using all concepts

---

## Common Interview Topics

### Questions You Should Be Ready For

1. **What is useState and how does it work?**
   - Answer: Easy level - Counter component

2. **Explain useEffect and its cleanup function**
   - Answer: Medium level - Carousel component

3. **How do you optimize React performance?**
   - Answer: Hard level - Performance Optimization

4. **What's the difference between useMemo and useCallback?**
   - Answer: Medium (Data Table) + Hard (Performance)

5. **How do you handle forms in React?**
   - Answer: Easy level - Form Validation

6. **Explain the concept of keys in lists**
   - Answer: Easy level - Todo List

7. **How do you prevent unnecessary re-renders?**
   - Answer: Hard level - React.memo section

8. **When would you use useReducer over useState?**
   - Answer: Hard level - Context API

---

## Best Practices Highlighted

### Code Organization
- ✅ Component extraction
- ✅ Custom hooks for reusable logic
- ✅ Clear naming conventions
- ✅ Single responsibility principle

### Performance
- ✅ Avoid inline function creation in renders
- ✅ Use keys properly in lists
- ✅ Memoization when appropriate
- ✅ Code splitting and lazy loading

### Accessibility
- ✅ ARIA attributes
- ✅ Keyboard navigation
- ✅ Semantic HTML
- ✅ Focus management

### Error Handling
- ✅ Input validation
- ✅ Error boundaries
- ✅ Loading states
- ✅ Empty states

---

## Quick Reference

### Hook Cheatsheet

| Hook | Purpose | Use When |
|------|---------|----------|
| `useState` | State management | Need to track data that changes |
| `useEffect` | Side effects | API calls, subscriptions, timers |
| `useRef` | DOM access / persist values | Access DOM, store values without re-render |
| `useMemo` | Memoize values | Expensive calculations |
| `useCallback` | Memoize functions | Pass callbacks to optimized children |
| `useReducer` | Complex state | Multiple related state values |
| `useContext` | Global state | Share data across components |

### Common Patterns

| Pattern | File Location | Difficulty |
|---------|---------------|------------|
| Controlled Component | Easy - Todo List | 🟢 |
| Conditional Rendering | Easy - Toggle | 🟢 |
| List Rendering | Easy - Todo List | 🟢 |
| Debouncing | Medium - Search | 🟡 |
| Infinite Scroll | Medium - Infinite List | 🟡 |
| Custom Hooks | Hard - useFetch | 🔴 |
| Compound Components | Hard - Dropdown | 🔴 |
| Context + Reducer | Hard - Cart | 🔴 |

---

## Additional Resources

### Official Documentation
- [React Docs](https://react.dev/) - New React documentation
- [React Hooks](https://react.dev/reference/react) - Hooks API reference

### Practice Platforms
- [Frontend Mentor](https://www.frontendmentor.io/) - Build real projects
- [LeetCode](https://leetcode.com/) - Frontend interview questions
- [GreatFrontEnd](https://www.greatfrontend.com/) - React-specific practice

### Design Patterns
- [Patterns.dev](https://www.patterns.dev/) - React patterns
- [React Patterns](https://reactpatterns.com/) - Common patterns

---

## Contributing

Found a mistake? Want to add more questions?

1. Fork the repository
2. Create a feature branch
3. Add your question following the existing format
4. Include diagrams, comments, and Q&A
5. Submit a pull request

---

## License

This guide is free to use for educational purposes.

---

## Feedback

Questions or suggestions? Open an issue or reach out!

**Happy Learning! 🚀**

---

### Legend
- ✅ Completed
- ⏳ Coming soon
- 🟢 Easy
- 🟡 Medium
- 🔴 Hard
