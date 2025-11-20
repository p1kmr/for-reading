# Virtual Scrolling (Window)

**Difficulty:** Hard
**Time:** 60-90 minutes
**Prerequisites:** Advanced React hooks, performance optimization, DOM manipulation

---

## Problem Statement

Render a massive list (10,000+ items) efficiently by only rendering visible items in the viewport. This is a critical performance optimization technique used in applications dealing with large datasets.

### The Challenge

When rendering large lists naively, you face severe performance problems:

```
Rendering 10,000 items naively:
❌ All 10,000 DOM elements created
❌ Slow initial render (3-5 seconds)
❌ Laggy scrolling (frame drops)
❌ High memory usage (browser crashes)
❌ Poor user experience
```

### The Solution

Virtual scrolling (also called "windowing") solves this by:

```
Virtual scrolling approach:
✅ Only render ~20 visible items
✅ Fast initial render (<100ms)
✅ Smooth 60fps scrolling
✅ Low memory usage
✅ Excellent user experience
```

**Real-World Examples:**
- Twitter/X feed (infinite scroll)
- Gmail inbox (thousands of emails)
- VS Code file explorer
- Spotify playlists
- React DevTools component tree

---

## Requirements

### Must Have
- ✅ Display large list (10,000+ items)
- ✅ Smooth scrolling performance (60fps)
- ✅ Only render visible items in viewport
- ✅ Calculate visible range on scroll
- ✅ Proper scrollbar behavior (reflects total list size)

### Nice to Have
- ⭐ Variable item heights
- ⭐ Dynamic loading (infinite scroll)
- ⭐ Scroll to specific item
- ⭐ Keyboard navigation support

---

## Virtual Scrolling Logic

### Core Concepts

```javascript
// CONFIGURATION
const containerHeight = 600;  // Visible area height
const itemHeight = 50;        // Each item's height
const totalItems = 10000;     // Total number of items
const bufferCount = 5;        // Extra items to render (prevents flickering)

// CALCULATIONS
// 1. How many items fit in viewport?
const visibleCount = Math.ceil(containerHeight / itemHeight);
// 600 / 50 = 12 items visible at once

// 2. Total items to render (including buffer)
const renderCount = visibleCount + (bufferCount * 2);
// 12 + (5 * 2) = 22 items

// 3. Current scroll position
const scrollTop = 5000; // User scrolled to 5000px

// 4. Calculate start index
const startIndex = Math.floor(scrollTop / itemHeight) - bufferCount;
// Math.floor(5000 / 50) - 5 = 100 - 5 = 95

// 5. Calculate end index
const endIndex = startIndex + renderCount;
// 95 + 22 = 117

// RESULT: Render items 95-117 (only 22 DOM elements!)
// Instead of 10,000 DOM elements
// Performance improvement: 450x fewer elements!
```

### The Math Behind It

```
┌─────────────────────────────────┐
│  Container (600px height)       │  ← User sees this
│  ┌─────────────────────────┐    │
│  │  Buffer (5 items)       │    │  ← Prevents white flash
│  ├─────────────────────────┤    │
│  │                         │    │
│  │  Visible Items (12)     │    │  ← Actually visible
│  │                         │    │
│  ├─────────────────────────┤    │
│  │  Buffer (5 items)       │    │  ← Prevents white flash
│  └─────────────────────────┘    │
│                                  │
│  Total height: 500,000px         │  ← Creates scrollbar
└─────────────────────────────────┘

Efficiency:
- Total items: 10,000
- Rendered items: 22
- DOM reduction: 99.78%
- Memory saved: ~99%
```

---

## Complete Solution

```jsx
import { useState, useRef } from 'react';

function VirtualScroll() {
  // ======================
  // DATA GENERATION
  // ======================

  // Generate large dataset (10,000 items)
  // In real app, this would come from API
  const allItems = Array.from({ length: 10000 }, (_, i) => ({
    id: i,
    name: `Item ${i}`,
    description: `Description for item ${i}`,
    timestamp: new Date(Date.now() - i * 1000000).toISOString()
  }));

  // ======================
  // CONFIGURATION
  // ======================

  const ITEM_HEIGHT = 80;        // Height of each item in pixels (MUST be fixed)
  const CONTAINER_HEIGHT = 600;  // Height of scrollable container
  const BUFFER_SIZE = 5;         // Extra items above/below viewport (prevents flickering)

  // ======================
  // STATE & REFS
  // ======================

  // Track scroll position
  // Why state? Triggers re-render when user scrolls
  const [scrollTop, setScrollTop] = useState(0);

  // Reference to container DOM element
  // Why ref? Need to attach scroll event listener
  const containerRef = useRef(null);

  // ======================
  // CALCULATIONS
  // ======================

  // Calculate how many items fit in viewport
  const visibleCount = Math.ceil(CONTAINER_HEIGHT / ITEM_HEIGHT);
  // Example: 600 / 80 = 7.5 → 8 items

  // Calculate start index (first item to render)
  // Math.max ensures we don't go below 0
  // Subtract BUFFER_SIZE to render items above viewport
  const startIndex = Math.max(
    0,
    Math.floor(scrollTop / ITEM_HEIGHT) - BUFFER_SIZE
  );
  // Example: scroll=1000, item=80 → Math.floor(1000/80) - 5 = 12 - 5 = 7

  // Calculate end index (last item to render)
  // Math.min ensures we don't exceed array length
  // Add buffer items below viewport
  const endIndex = Math.min(
    allItems.length,
    startIndex + visibleCount + BUFFER_SIZE * 2
  );
  // Example: 7 + 8 + 10 = 25

  // Extract items to render (slice is O(1) for array views)
  const visibleItems = allItems.slice(startIndex, endIndex);

  // Calculate vertical offset
  // Why? Position items at correct scroll position
  const offsetY = startIndex * ITEM_HEIGHT;
  // Example: startIndex=7, itemHeight=80 → 7*80 = 560px

  // Calculate total height of all items
  // Why? Creates proper scrollbar size
  const totalHeight = allItems.length * ITEM_HEIGHT;
  // Example: 10000 * 80 = 800,000px

  // ======================
  // EVENT HANDLERS
  // ======================

  // Handle scroll event
  // Why? Update state when user scrolls
  const handleScroll = (e) => {
    // e.target.scrollTop = pixels scrolled from top
    setScrollTop(e.target.scrollTop);
  };

  // ======================
  // RENDER
  // ======================

  return (
    <div style={{ padding: '20px', fontFamily: 'system-ui' }}>
      <h1>Virtual Scroll - 10,000 Items</h1>
      <p style={{ color: '#666' }}>
        Only rendering <strong>{visibleItems.length}</strong> items at a time
        (instead of {allItems.length.toLocaleString()})!
      </p>
      <p style={{ color: '#28a745', fontWeight: 'bold' }}>
        Performance boost: {Math.round((allItems.length / visibleItems.length) * 10) / 10}x
      </p>

      {/* SCROLLABLE CONTAINER */}
      <div
        ref={containerRef}
        onScroll={handleScroll}
        style={{
          height: `${CONTAINER_HEIGHT}px`,
          overflow: 'auto',
          border: '2px solid #ddd',
          borderRadius: '8px',
          position: 'relative',
          backgroundColor: '#fafafa'
        }}
      >
        {/*
          SPACER DIV
          Purpose: Creates scrollbar representing total list size
          Height: Total height of ALL items (even non-rendered ones)
        */}
        <div style={{
          height: `${totalHeight}px`,
          position: 'relative',
          width: '100%'
        }}>
          {/*
            VISIBLE ITEMS CONTAINER
            Purpose: Holds only the visible items
            Position: Absolute with offset to appear at correct scroll position
          */}
          <div
            style={{
              position: 'absolute',
              top: `${offsetY}px`,      // Push down to correct position
              left: 0,
              right: 0,
              willChange: 'transform'   // Performance hint for browser
            }}
          >
            {visibleItems.map((item) => (
              <div
                key={item.id}
                style={{
                  height: `${ITEM_HEIGHT}px`,
                  padding: '15px 20px',
                  borderBottom: '1px solid #e0e0e0',
                  background: '#fff',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  transition: 'background 0.2s'
                }}
                onMouseEnter={(e) => e.currentTarget.style.background = '#f0f8ff'}
                onMouseLeave={(e) => e.currentTarget.style.background = '#fff'}
              >
                <div style={{ flex: 1 }}>
                  <h3 style={{
                    margin: '0 0 5px 0',
                    fontSize: '16px',
                    fontWeight: 600
                  }}>
                    {item.name}
                  </h3>
                  <p style={{
                    margin: 0,
                    color: '#666',
                    fontSize: '14px'
                  }}>
                    {item.description}
                  </p>
                </div>
                <div style={{
                  fontSize: '12px',
                  color: '#999',
                  marginLeft: '20px'
                }}>
                  ID: {item.id}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* DEBUG INFO */}
      <div style={{
        marginTop: '20px',
        padding: '15px',
        background: '#f5f5f5',
        borderRadius: '8px',
        fontFamily: 'monospace',
        fontSize: '13px'
      }}>
        <h3 style={{ marginTop: 0 }}>Debug Info</h3>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
          <div>📏 Scroll Position: <strong>{scrollTop}px</strong></div>
          <div>📊 Total Items: <strong>{allItems.length.toLocaleString()}</strong></div>
          <div>👁️ Visible Count: <strong>{visibleCount}</strong></div>
          <div>🎯 Rendered: <strong>{visibleItems.length}</strong></div>
          <div>🔢 Start Index: <strong>{startIndex}</strong></div>
          <div>🔢 End Index: <strong>{endIndex}</strong></div>
          <div>📐 Item Height: <strong>{ITEM_HEIGHT}px</strong></div>
          <div>📏 Total Height: <strong>{totalHeight.toLocaleString()}px</strong></div>
        </div>
      </div>
    </div>
  );
}

export default VirtualScroll;
```

---

## How It Works: Visual Diagram

```
VIEWPORT WINDOW (600px)
╔════════════════════════════════════╗
║                                    ║
║  ┌─────────── BUFFER ──────────┐  ║  Items 95-99 (not visible but rendered)
║  │  Item 95                    │  ║
║  │  Item 96                    │  ║
║  │  Item 97                    │  ║
║  │  Item 98                    │  ║
║  │  Item 99                    │  ║
║  └─────────────────────────────┘  ║
║  ┌────── VISIBLE ITEMS ────────┐  ║
║  │  Item 100  ← User sees      │  ║  Actually visible on screen
║  │  Item 101                   │  ║
║  │  Item 102                   │  ║
║  │  Item 103                   │  ║
║  │  Item 104                   │  ║
║  │  Item 105                   │  ║
║  │  Item 106                   │  ║
║  │  Item 107  ← User sees      │  ║
║  └─────────────────────────────┘  ║
║  ┌─────────── BUFFER ──────────┐  ║  Items 108-112 (not visible but rendered)
║  │  Item 108                   │  ║
║  │  Item 109                   │  ║
║  │  Item 110                   │  ║
║  │  Item 111                   │  ║
║  │  Item 112                   │  ║
║  └─────────────────────────────┘  ║
╚════════════════════════════════════╝

Spacer height: 800,000px (10,000 items × 80px)
Offset from top: 7,600px (95 items × 80px)
Items rendered: 18 (instead of 10,000!)
```

---

## Advanced: Variable Item Heights

The basic solution assumes all items have the same height. For variable heights:

```jsx
import { useState, useRef, useEffect } from 'react';

function VirtualScrollVariableHeight() {
  // Store measured heights for each item
  // Why? Need to know each item's actual height
  const [itemHeights, setItemHeights] = useState({});

  // Store refs to each rendered item
  // Why? Need to measure DOM elements
  const itemRefs = useRef({});

  const allItems = Array.from({ length: 10000 }, (_, i) => ({
    id: i,
    content: `Item ${i}`,
    // Variable content length
    description: 'Short description.'.repeat(Math.floor(Math.random() * 5) + 1)
  }));

  // Measure items after render
  useEffect(() => {
    const newHeights = {};
    let changed = false;

    // Measure all currently rendered items
    Object.keys(itemRefs.current).forEach(id => {
      const element = itemRefs.current[id];
      if (element) {
        const height = element.getBoundingClientRect().height;

        // Only update if height changed
        if (itemHeights[id] !== height) {
          newHeights[id] = height;
          changed = true;
        }
      }
    });

    // Update state only if heights changed
    if (changed) {
      setItemHeights(prev => ({ ...prev, ...newHeights }));
    }
  });

  // Calculate cumulative offset for an item
  // Why? Need to know vertical position of each item
  const getItemOffset = (index) => {
    let offset = 0;
    for (let i = 0; i < index; i++) {
      // Use measured height or default estimate
      offset += itemHeights[i] || 100;
    }
    return offset;
  };

  // Calculate total height of all items
  const getTotalHeight = () => {
    let total = 0;
    for (let i = 0; i < allItems.length; i++) {
      total += itemHeights[i] || 100;
    }
    return total;
  };

  // Find start index based on scroll position
  const getStartIndex = (scrollTop) => {
    let sum = 0;
    for (let i = 0; i < allItems.length; i++) {
      const height = itemHeights[i] || 100;
      if (sum + height > scrollTop) {
        return Math.max(0, i - 5); // Include buffer
      }
      sum += height;
    }
    return 0;
  };

  const [scrollTop, setScrollTop] = useState(0);
  const startIndex = getStartIndex(scrollTop);
  const endIndex = Math.min(allItems.length, startIndex + 20);
  const visibleItems = allItems.slice(startIndex, endIndex);

  return (
    <div
      onScroll={(e) => setScrollTop(e.target.scrollTop)}
      style={{
        height: '600px',
        overflow: 'auto',
        position: 'relative'
      }}
    >
      <div style={{ height: `${getTotalHeight()}px`, position: 'relative' }}>
        {visibleItems.map((item, index) => (
          <div
            key={item.id}
            ref={el => itemRefs.current[item.id] = el}
            style={{
              position: 'absolute',
              top: `${getItemOffset(startIndex + index)}px`,
              left: 0,
              right: 0,
              padding: '10px',
              background: '#fff',
              borderBottom: '1px solid #ddd'
            }}
          >
            <h4>{item.content}</h4>
            <p>{item.description}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
```

**Key Differences:**
1. **Measure Heights**: Use `getBoundingClientRect()` to measure each item
2. **Store Heights**: Keep map of `itemId → height`
3. **Calculate Offsets**: Sum heights to find each item's position
4. **Update on Render**: Re-measure when new items rendered

**Trade-offs:**
- ✅ Supports variable content
- ✅ More flexible
- ❌ More complex
- ❌ Slight performance cost (measurements)
- ❌ Can cause layout shift during scroll

---

## Common Mistakes & How to Avoid Them

### ❌ Mistake 1: No Buffer Items

```jsx
// BAD: No buffer
const visibleItems = allItems.slice(
  Math.floor(scrollTop / itemHeight),
  Math.floor(scrollTop / itemHeight) + visibleCount
);
```

**Problem:** White flashes when scrolling fast
**Fix:** Add buffer items above and below

```jsx
// GOOD: With buffer
const startIndex = Math.floor(scrollTop / itemHeight) - BUFFER_SIZE;
const endIndex = startIndex + visibleCount + BUFFER_SIZE * 2;
```

---

### ❌ Mistake 2: Forgetting Total Height Spacer

```jsx
// BAD: No spacer
<div onScroll={handleScroll}>
  {visibleItems.map(item => <div>{item.name}</div>)}
</div>
```

**Problem:** Scrollbar doesn't reflect total list size
**Fix:** Add spacer div with total height

```jsx
// GOOD: With spacer
<div onScroll={handleScroll}>
  <div style={{ height: `${totalHeight}px` }}>
    <div style={{ top: `${offsetY}px` }}>
      {visibleItems.map(item => <div>{item.name}</div>)}
    </div>
  </div>
</div>
```

---

### ❌ Mistake 3: Re-creating Items Array

```jsx
// BAD: Re-creates array on every render
function VirtualScroll() {
  const allItems = Array.from({ length: 10000 }, ...); // ← Recreated!
  // ...
}
```

**Problem:** Performance hit, unnecessary work
**Fix:** Move outside component or use useMemo

```jsx
// GOOD: Created once
const allItems = Array.from({ length: 10000 }, ...);

function VirtualScroll() {
  // Or use useMemo
  const allItems = useMemo(
    () => Array.from({ length: 10000 }, ...),
    []
  );
}
```

---

### ❌ Mistake 4: Not Using Keys Properly

```jsx
// BAD: Using index as key
{visibleItems.map((item, index) => (
  <div key={index}>{item.name}</div>
))}
```

**Problem:** React re-renders unnecessarily
**Fix:** Use stable unique ID

```jsx
// GOOD: Using item ID
{visibleItems.map((item) => (
  <div key={item.id}>{item.name}</div>
))}
```

---

## Performance Considerations

### Metrics Comparison

```
┌─────────────────┬──────────────┬────────────────┐
│ Metric          │ Naive        │ Virtual Scroll │
├─────────────────┼──────────────┼────────────────┤
│ Initial Render  │ 3-5 seconds  │ <100ms         │
│ DOM Nodes       │ 10,000       │ 20-30          │
│ Memory Usage    │ 50-100 MB    │ 1-2 MB         │
│ Scroll FPS      │ 10-20 fps    │ 60 fps         │
│ Paint Time      │ 100-200ms    │ 1-5ms          │
└─────────────────┴──────────────┴────────────────┘
```

### Optimization Tips

1. **Use `will-change` CSS**
```css
.scroll-container {
  will-change: transform;
}
```

2. **Debounce Scroll Events** (if doing heavy calculations)
```jsx
const debouncedScroll = useMemo(
  () => debounce((scrollTop) => setScrollTop(scrollTop), 16), // ~60fps
  []
);
```

3. **Use React.memo for Items**
```jsx
const VirtualItem = React.memo(({ item }) => (
  <div>{item.name}</div>
));
```

4. **Avoid Inline Styles** (extract to CSS classes)
```jsx
// Instead of inline styles, use classes
<div className="virtual-item">{item.name}</div>
```

---

## Interviewer Q&A

### Q1: Why use virtual scrolling instead of pagination?

**Answer:**
- **Better UX**: Smooth continuous scrolling vs. clicking through pages
- **Data exploration**: Users can scroll freely to find items
- **Mobile-friendly**: Natural scrolling behavior
- **Modern expectation**: Users expect infinite scroll (Twitter, Instagram)

But pagination is better when:
- SEO is important (each page = URL)
- Users need to return to specific page
- Backend can't handle large result sets

---

### Q2: What if items have different heights?

**Answer:**
Fixed heights are easier, but for variable heights:

1. **Measure each item** after rendering
2. **Store heights** in state/map
3. **Calculate offsets** by summing previous heights
4. **Accept layout shifts** during scroll

Libraries like `react-window` handle this automatically.

---

### Q3: How would you add infinite scroll to this?

**Answer:**
```jsx
useEffect(() => {
  // Detect when user scrolls near bottom
  if (scrollTop + containerHeight > totalHeight - 100) {
    loadMoreItems();
  }
}, [scrollTop]);

const loadMoreItems = () => {
  // Fetch next page from API
  fetchItems(page + 1).then(newItems => {
    setItems(prev => [...prev, ...newItems]);
  });
};
```

---

### Q4: What about accessibility (keyboard navigation)?

**Answer:**
```jsx
// Add keyboard event handlers
<div
  tabIndex={0}
  onKeyDown={(e) => {
    if (e.key === 'ArrowDown') {
      setScrollTop(prev => prev + ITEM_HEIGHT);
    } else if (e.key === 'ArrowUp') {
      setScrollTop(prev => Math.max(0, prev - ITEM_HEIGHT));
    }
  }}
>
```

Also add ARIA attributes:
- `role="list"` on container
- `role="listitem"` on items
- `aria-label` for screen readers

---

### Q5: Can you virtualize in both directions (horizontal + vertical)?

**Answer:**
Yes! Same concept, but calculate for both axes:

```jsx
const visibleX = Math.ceil(scrollLeft / itemWidth);
const visibleY = Math.ceil(scrollTop / itemHeight);

const visibleItems = [];
for (let row = startRow; row < endRow; row++) {
  for (let col = startCol; col < endCol; col++) {
    visibleItems.push(grid[row][col]);
  }
}
```

Used in:
- Spreadsheets (Excel-like apps)
- Data grids
- Image galleries

---

## Key Takeaways

### Essential Concepts

1. **Only Render Visible Items**
   - Calculate which items are in viewport
   - Render only those + small buffer
   - Massive performance improvement

2. **Spacer for Scrollbar**
   - Create div with total height
   - Gives proper scrollbar size
   - Preserves scroll behavior

3. **Position with Offset**
   - Calculate vertical offset
   - Position items at correct scroll position
   - Use absolute positioning

4. **Buffer Prevents Flickering**
   - Render extra items above/below viewport
   - Prevents white flashes during scroll
   - 5-10 items usually enough

5. **Fixed Heights Simplify Math**
   - All calculations become O(1)
   - Variable heights require O(n) calculations
   - Trade-off: flexibility vs. performance

### When to Use Virtual Scrolling

✅ **Good for:**
- Large lists (1,000+ items)
- Infinite scroll feeds
- Data tables with many rows
- File explorers
- Chat message history

❌ **Not needed for:**
- Small lists (<100 items)
- Lists with pagination
- Infrequently updated lists
- Simple, static content

### Production Libraries

Don't reinvent the wheel in production. Use:

1. **react-window** (recommended)
   - Lightweight (3kb)
   - Fixed and variable heights
   - Maintained by Google

2. **react-virtualized**
   - More features
   - Larger bundle (20kb)
   - Predecessor to react-window

3. **@tanstack/react-virtual**
   - Modern, TypeScript-first
   - Framework-agnostic core
   - Excellent DX

---

## Practice Exercises

1. **Basic**: Implement virtual scrolling for 5,000 items
2. **Intermediate**: Add search/filter to virtual list
3. **Advanced**: Implement bidirectional virtual grid
4. **Expert**: Add smooth scroll-to-item animation

---

## Final Tips

1. **Start with fixed heights** - easier to implement and debug
2. **Profile performance** - use React DevTools Profiler
3. **Test on low-end devices** - not everyone has fast computers
4. **Consider libraries** - don't reinvent for production
5. **Measure impact** - compare before/after performance

**Remember:** Virtual scrolling is a performance optimization. Only add it when you have a proven performance problem!

---

Good luck! This is a common senior-level interview question that tests your understanding of performance optimization and DOM manipulation. 🚀
