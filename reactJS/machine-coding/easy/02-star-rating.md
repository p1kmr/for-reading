# Star Rating Component - Easy Level

**Difficulty:** Beginner
**Time:** 30-45 minutes
**Prerequisites:** Basic React, useState, event handling

---

## 📋 Problem Statement

Build a star rating component (like Amazon product ratings). Display a row of stars. When a user clicks on a star, fill that star and all stars before it. The component should also support hover effects.

### Example:
```
★ ★ ★ ☆ ☆  (3 stars selected)

On hover over 4th star:
★ ★ ★ ★ ☆  (preview)
```

---

## 🎯 Requirements

**Must Have:**
- ✅ Display 5 stars
- ✅ Click a star to set rating
- ✅ Fill all stars up to the clicked star
- ✅ Show hover preview

**Nice to Have:**
- ⭐ Configurable number of stars
- ⭐ Half-star support
- ⭐ Read-only mode

---

## 🤔 How to Think About This Problem

### State Needed:
1. `rating` - permanent selection
2. `hoverRating` - temporary hover preview

### Visual Logic:
```
Rating = 3
Star 1: (1 <= 3) → filled ★
Star 2: (2 <= 3) → filled ★
Star 3: (3 <= 3) → filled ★
Star 4: (4 <= 3) → empty ☆
Star 5: (5 <= 3) → empty ☆
```

---

## 💡 Complete Solution

```jsx
import { useState } from 'react';

function StarRating() {
  // State 1: Permanent rating (what user clicked)
  // Why useState? Rating changes on user interaction
  const [rating, setRating] = useState(0);

  // State 2: Temporary hover preview
  // Why separate? Hover shouldn't affect actual rating until click
  const [hoverRating, setHoverRating] = useState(0);

  const totalStars = 5;

  const handleClick = (starIndex) => {
    setRating(starIndex);
  };

  const handleMouseEnter = (starIndex) => {
    setHoverRating(starIndex);
  };

  const handleMouseLeave = () => {
    setHoverRating(0); // Reset hover preview
  };

  return (
    <div className="star-rating">
      <h2>Rate this product</h2>

      <div className="stars">
        {Array.from({ length: totalStars }, (_, index) => {
          const starIndex = index + 1; // Convert to 1-based (1-5)

          // Determine if star should be filled
          // Use hoverRating if hovering, otherwise use actual rating
          const isFilled = starIndex <= (hoverRating || rating);
          // Why ||? If hoverRating is 0 (falsy), use rating

          return (
            <span
              key={starIndex}
              onClick={() => handleClick(starIndex)}
              onMouseEnter={() => handleMouseEnter(starIndex)}
              onMouseLeave={handleMouseLeave}
              style={{
                cursor: 'pointer',
                fontSize: '40px',
                color: isFilled ? '#FFD700' : '#CCCCCC',
              }}
            >
              {isFilled ? '★' : '☆'}
            </span>
          );
        })}
      </div>

      <div className="rating-value">
        {rating > 0 ? (
          <p>You rated: {rating} out of {totalStars} stars</p>
        ) : (
          <p>No rating yet. Click a star to rate!</p>
        )}
      </div>
    </div>
  );
}

export default StarRating;
```

---

## 🧠 Key Concepts Explained

### Why Two States?

**Question:** Can't we just use one state?

**Answer:** No, because:
- `rating` = permanent selection (persists after mouse leaves)
- `hoverRating` = preview only (resets when mouse leaves)

If we only had one state, hovering would change the rating without clicking!

### How Does `hoverRating || rating` Work?

```javascript
// Scenario 1: Not hovering
hoverRating = 0 (falsy)
0 || 3 → 3 (use rating)

// Scenario 2: Hovering over star 5
hoverRating = 5 (truthy)
5 || 3 → 5 (use hoverRating)
```

This elegantly handles both preview and actual rating with one line!

---

## 🧠 Visual State Flow

```
┌────────────────────────────────────────┐
│ Initial State                          │
│ rating = 0, hoverRating = 0            │
│ Display: ☆ ☆ ☆ ☆ ☆                    │
└────────────────────────────────────────┘
           │
           │ User clicks star 3
           ▼
┌────────────────────────────────────────┐
│ onClick(3) → setRating(3)              │
│ rating = 3, hoverRating = 0            │
│ Display: ★ ★ ★ ☆ ☆                    │
└────────────────────────────────────────┘
           │
           │ User hovers star 5
           ▼
┌────────────────────────────────────────┐
│ onMouseEnter(5) → setHoverRating(5)    │
│ rating = 3, hoverRating = 5            │
│ Display: ★ ★ ★ ★ ★ (preview)          │
└────────────────────────────────────────┘
           │
           │ Mouse leaves
           ▼
┌────────────────────────────────────────┐
│ onMouseLeave() → setHoverRating(0)     │
│ rating = 3, hoverRating = 0            │
│ Display: ★ ★ ★ ☆ ☆ (back to actual)  │
└────────────────────────────────────────┘
```

---

## 🐛 Common Mistakes

### Mistake 1: Using 0-Based Indexing

```jsx
// ❌ WRONG: Gives rating 0-4 instead of 1-5
{Array.from({ length: 5 }, (_, index) => (
  <span onClick={() => setRating(index)}>★</span>
))}

// ✅ CORRECT: Convert to 1-based
{Array.from({ length: 5 }, (_, index) => {
  const starIndex = index + 1; // 1, 2, 3, 4, 5
  return <span onClick={() => setRating(starIndex)}>★</span>;
})}
```

### Mistake 2: Not Resetting Hover

```jsx
// ❌ WRONG: No onMouseLeave
<span onMouseEnter={() => setHoverRating(3)}>★</span>
// Result: Hover state persists!

// ✅ CORRECT: Reset on leave
<span
  onMouseEnter={() => setHoverRating(3)}
  onMouseLeave={() => setHoverRating(0)}
>★</span>
```

### Mistake 3: Wrong Comparison Logic

```jsx
// ❌ WRONG: Only exact match (single star fills)
const isFilled = starIndex === rating;

// ✅ CORRECT: Less than or equal (all stars up to rating fill)
const isFilled = starIndex <= rating;
```

### Mistake 4: Using Only One State

```jsx
// ❌ WRONG: Hover changes actual rating
const [rating, setRating] = useState(0);

const handleHover = (index) => {
  setRating(index); // This changes the actual rating!
};

// ✅ CORRECT: Separate hover state
const [rating, setRating] = useState(0);
const [hoverRating, setHoverRating] = useState(0);

const handleHover = (index) => {
  setHoverRating(index); // This only changes preview
};
```

---

## 🎤 Interviewer Q&A

### Q1: Why separate states for rating and hoverRating?

**Answer:**
> "I need two states because they serve different purposes. `rating` stores the permanent selection (what the user clicked), while `hoverRating` is just for preview. If I used one state, hovering would change the rating without a click, which isn't the desired behavior."

### Q2: How would you make this a controlled component?

**Answer:**
> "I'd accept `rating` and `onRatingChange` as props from the parent:

```jsx
function StarRating({ rating, onRatingChange }) {
  const [hoverRating, setHoverRating] = useState(0);

  const handleClick = (starIndex) => {
    onRatingChange(starIndex); // Tell parent to update
  };

  // ... rest of component uses rating prop
}
```"

### Q3: How would you add half-star support?

**Answer:**
```jsx
function StarRating() {
  const [rating, setRating] = useState(0);

  const handleClick = (e, starIndex) => {
    const rect = e.target.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const starWidth = rect.width;

    // If clicked left half, set .5, otherwise full star
    const newRating = clickX < starWidth / 2
      ? starIndex - 0.5
      : starIndex;

    setRating(newRating);
  };

  const renderStar = (starIndex) => {
    if (starIndex - 0.5 === rating) {
      return '⯨'; // Half star
    }
    return starIndex <= rating ? '★' : '☆';
  };

  return (
    <div>
      {Array.from({ length: 5 }, (_, i) => {
        const starIndex = i + 1;
        return (
          <span
            key={starIndex}
            onClick={(e) => handleClick(e, starIndex)}
          >
            {renderStar(starIndex)}
          </span>
        );
      })}
    </div>
  );
}
```

### Q4: How would you make it read-only?

**Answer:**
```jsx
function StarRating({ rating, readOnly = false }) {
  const [hoverRating, setHoverRating] = useState(0);

  return (
    <div className="star-rating">
      {Array.from({ length: 5 }, (_, i) => {
        const starIndex = i + 1;
        const isFilled = starIndex <= (hoverRating || rating);

        return (
          <span
            key={starIndex}
            onClick={() => !readOnly && setRating(starIndex)}
            onMouseEnter={() => !readOnly && setHoverRating(starIndex)}
            onMouseLeave={() => !readOnly && setHoverRating(0)}
            style={{
              cursor: readOnly ? 'default' : 'pointer',
              color: isFilled ? '#FFD700' : '#CCCCCC'
            }}
          >
            {isFilled ? '★' : '☆'}
          </span>
        );
      })}
    </div>
  );
}
```

### Q5: How would you make the number of stars configurable?

**Answer:**
```jsx
function StarRating({ maxStars = 5 }) {
  const [rating, setRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);

  return (
    <div>
      {Array.from({ length: maxStars }, (_, i) => {
        const starIndex = i + 1;
        const isFilled = starIndex <= (hoverRating || rating);

        return (
          <span
            key={starIndex}
            onClick={() => setRating(starIndex)}
            onMouseEnter={() => setHoverRating(starIndex)}
            onMouseLeave={() => setHoverRating(0)}
          >
            {isFilled ? '★' : '☆'}
          </span>
        );
      })}
      <p>You rated: {rating} out of {maxStars}</p>
    </div>
  );
}

// Usage
<StarRating maxStars={10} />
```

---

## 📊 Component Structure Diagram

```
┌─────────────────────────────────────────────────┐
│          StarRating Component                   │
│                                                 │
│  State:                                         │
│  rating = 3                                     │
│  hoverRating = 0                                │
│                                                 │
│  ┌───────────────────────────────────────────┐ │
│  │  Star 1 (index=1)                         │ │
│  │  isFilled = 1 <= (0 || 3) = true          │ │
│  │  Display: ★                                │ │
│  └───────────────────────────────────────────┘ │
│                                                 │
│  ┌───────────────────────────────────────────┐ │
│  │  Star 2 (index=2)                         │ │
│  │  isFilled = 2 <= (0 || 3) = true          │ │
│  │  Display: ★                                │ │
│  └───────────────────────────────────────────┘ │
│                                                 │
│  ┌───────────────────────────────────────────┐ │
│  │  Star 3 (index=3)                         │ │
│  │  isFilled = 3 <= (0 || 3) = true          │ │
│  │  Display: ★                                │ │
│  └───────────────────────────────────────────┘ │
│                                                 │
│  ┌───────────────────────────────────────────┐ │
│  │  Star 4 (index=4)                         │ │
│  │  isFilled = 4 <= (0 || 3) = false         │ │
│  │  Display: ☆                                │ │
│  └───────────────────────────────────────────┘ │
│                                                 │
│  ┌───────────────────────────────────────────┐ │
│  │  Star 5 (index=5)                         │ │
│  │  isFilled = 5 <= (0 || 3) = false         │ │
│  │  Display: ☆                                │ │
│  └───────────────────────────────────────────┘ │
│                                                 │
│  Result: ★ ★ ★ ☆ ☆                            │
└─────────────────────────────────────────────────┘
```

---

## 🔑 Key Takeaways

1. **Two States Needed:** Separate permanent rating from hover preview
2. **Short-Circuit Evaluation:** Use `hoverRating || rating` elegantly
3. **Event Handlers:** onClick for permanent, onMouseEnter/Leave for preview
4. **1-Based Indexing:** Convert array index to star number (1-5, not 0-4)
5. **Comparison Logic:** Use `<=` not `===` to fill all stars up to selection
6. **User Experience:** Visual feedback on hover before committing with click

---

## 📈 Next Steps

After mastering the star rating component, try:
- Adding half-star support
- Making it a controlled component
- Adding animations
- Creating a read-only mode for displaying ratings
- Adding size variants (small, medium, large)
- Supporting custom icons instead of stars

Keep practicing! 🚀
