# Drag and Drop

**Difficulty:** Hard
**Time:** 60-90 minutes
**Prerequisites:** Event handling, DOM manipulation, state management

---

## Problem Statement

Implement drag and drop functionality to reorder items in a list. This is a fundamental interaction pattern used in modern web applications.

### Real-World Examples

```
Trello / Jira          Notion            Gmail
============          ======            =====
📋 To Do              📝 Page 1         📧 Inbox
📋 In Progress        📝 Page 2           └─ 📁 Work
📋 Done               📝 Page 3           └─ 📁 Personal

User can drag cards   Drag to reorder   Drag emails to folders
between columns       pages             to organize
```

**Used in:**
- Kanban boards (Trello, Jira, Asana)
- Page builders (Notion, Confluence)
- File managers (drag files to folders)
- Playlist editors (Spotify, YouTube)
- Email clients (Gmail, Outlook)
- Form builders (drag fields to reorder)

---

## Requirements

### Must Have
- ✅ Drag items to reorder them
- ✅ Visual feedback (item follows cursor)
- ✅ Drop to new position
- ✅ List updates with new order
- ✅ Smooth animation

### Nice to Have
- ⭐ Drag between multiple lists
- ⭐ Drag handles (only drag from specific area)
- ⭐ Prevent dragging certain items
- ⭐ Copy instead of move (hold Ctrl/Cmd)
- ⭐ Touch support (mobile)
- ⭐ Accessibility (keyboard support)

---

## Drag and Drop Events

### HTML5 Drag Events

There are 7 drag events in the lifecycle:

```javascript
// ON DRAGGABLE ELEMENT:
// 1. dragstart   - User starts dragging
// 2. drag        - Element is being dragged (fires continuously)
// 3. dragend     - User releases mouse (drag ends)

// ON DROP TARGET:
// 4. dragenter   - Dragged element enters target
// 5. dragover    - Dragged element is over target (fires continuously)
// 6. dragleave   - Dragged element leaves target
// 7. drop        - User releases mouse over target
```

### Event Flow Visualization

```
┌─────────────────────────────────────┐
│  User clicks and holds Item 1       │
│  ↓                                   │
│  dragstart fired on Item 1          │  ← Set draggedItem state
│                                      │
│  User moves mouse                    │
│  ↓                                   │
│  dragover fired on Item 2           │  ← e.preventDefault() to allow drop
│                                      │
│  User releases mouse over Item 2    │
│  ↓                                   │
│  drop fired on Item 2               │  ← Reorder array
│  dragend fired on Item 1            │  ← Clear draggedItem state
└─────────────────────────────────────┘
```

---

## Core Algorithm

### Reordering Logic

```javascript
// Current array:
const items = ['A', 'B', 'C', 'D', 'E'];

// User drags 'B' (index 1) to position of 'D' (index 3)
// Desired result: ['A', 'C', 'D', 'B', 'E']

// STEP 1: Find indexes
const draggedIndex = 1;   // 'B'
const targetIndex = 3;    // 'D'

// STEP 2: Create copy of array
const newItems = [...items];  // ['A', 'B', 'C', 'D', 'E']

// STEP 3: Remove dragged item
const [removed] = newItems.splice(draggedIndex, 1);
// removed = 'B'
// newItems = ['A', 'C', 'D', 'E']

// STEP 4: Insert at new position
newItems.splice(targetIndex, 0, removed);
// Insert 'B' at index 3
// newItems = ['A', 'C', 'D', 'B', 'E']

// STEP 5: Update state
setItems(newItems);
```

### Why splice()?

```javascript
// splice(start, deleteCount, itemToInsert)

// Remove item:
array.splice(2, 1);
// Starting at index 2, remove 1 item

// Insert item:
array.splice(2, 0, 'X');
// Starting at index 2, remove 0 items, insert 'X'

// Both operations in one:
const [removed] = array.splice(2, 1);  // Remove
array.splice(4, 0, removed);           // Insert
```

---

## Complete Solution

```jsx
import { useState } from 'react';

function DragAndDrop() {
  // ======================
  // STATE MANAGEMENT
  // ======================

  // List of items to display
  const [items, setItems] = useState([
    { id: 1, text: 'Learn React Basics', color: '#ffebee', priority: 'High' },
    { id: 2, text: 'Build Todo App', color: '#e3f2fd', priority: 'Medium' },
    { id: 3, text: 'Master Hooks', color: '#e8f5e9', priority: 'High' },
    { id: 4, text: 'Study State Management', color: '#fff3e0', priority: 'Low' },
    { id: 5, text: 'Deploy to Production', color: '#f3e5f5', priority: 'Medium' }
  ]);

  // Track which item is being dragged
  // Why? Need to know what to move when user drops
  const [draggedItem, setDraggedItem] = useState(null);

  // Track which item is currently under cursor (for visual feedback)
  const [draggedOverItem, setDraggedOverItem] = useState(null);

  // ======================
  // EVENT HANDLERS
  // ======================

  /**
   * Handle drag start
   * Fires when user starts dragging an item
   */
  const handleDragStart = (item) => {
    setDraggedItem(item);
    // Store item being dragged
  };

  /**
   * Handle drag over
   * Fires continuously while item is dragged over target
   * MUST call preventDefault() to allow drop
   */
  const handleDragOver = (e) => {
    e.preventDefault();
    // ⚠️ CRITICAL: Without this, drop event won't fire!
    // Browser default is to NOT allow dropping
  };

  /**
   * Handle drag enter
   * Fires when dragged item enters a drop target
   */
  const handleDragEnter = (item) => {
    setDraggedOverItem(item);
    // Visual feedback: highlight drop target
  };

  /**
   * Handle drag leave
   * Fires when dragged item leaves a drop target
   */
  const handleDragLeave = () => {
    setDraggedOverItem(null);
    // Remove visual feedback
  };

  /**
   * Handle drop
   * Fires when user releases mouse over drop target
   * This is where we reorder the array
   */
  const handleDrop = (targetItem) => {
    if (!draggedItem) return;
    if (draggedItem.id === targetItem.id) return;  // Dropped on itself

    // Find indexes
    const draggedIndex = items.findIndex(item => item.id === draggedItem.id);
    const targetIndex = items.findIndex(item => item.id === targetItem.id);

    // Create new array (immutable update)
    const newItems = [...items];

    // Remove dragged item from old position
    const [removed] = newItems.splice(draggedIndex, 1);
    // splice(index, 1) removes 1 item at index, returns removed items as array

    // Insert at new position
    newItems.splice(targetIndex, 0, removed);
    // splice(index, 0, item) inserts item at index without removing anything

    // Update state
    setItems(newItems);

    // Clear drag state
    setDraggedItem(null);
    setDraggedOverItem(null);
  };

  /**
   * Handle drag end
   * Fires when drag operation ends (whether successful or not)
   */
  const handleDragEnd = () => {
    setDraggedItem(null);
    setDraggedOverItem(null);
    // Clean up state
  };

  // ======================
  // RENDER
  // ======================

  return (
    <div style={{
      padding: '20px',
      maxWidth: '700px',
      margin: '0 auto',
      fontFamily: 'system-ui, -apple-system, sans-serif'
    }}>
      <h1>Drag and Drop List</h1>
      <p style={{ color: '#666', marginBottom: '20px' }}>
        Drag items to reorder them. Try dragging a task up or down!
      </p>

      {/* DRAG AND DROP LIST */}
      <div style={{ marginTop: '20px' }}>
        {items.map((item, index) => (
          <div
            key={item.id}
            draggable  // ← Makes element draggable (HTML5 attribute)
            onDragStart={() => handleDragStart(item)}
            onDragOver={handleDragOver}   // Must preventDefault
            onDragEnter={() => handleDragEnter(item)}
            onDragLeave={handleDragLeave}
            onDrop={() => handleDrop(item)}
            onDragEnd={handleDragEnd}
            style={{
              padding: '20px',
              margin: '10px 0',
              background: item.color,
              borderRadius: '8px',
              cursor: 'move',
              userSelect: 'none',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',

              // Visual feedback during drag
              border: draggedOverItem?.id === item.id
                ? '3px dashed #333'             // Hovering over this item
                : draggedItem?.id === item.id
                  ? '3px dashed #999'           // This item is being dragged
                  : '3px solid transparent',    // Normal state

              opacity: draggedItem?.id === item.id ? 0.5 : 1,
              transform: draggedItem?.id === item.id ? 'scale(0.95)' : 'scale(1)',
              transition: 'all 0.2s ease',
              boxShadow: draggedOverItem?.id === item.id
                ? '0 4px 12px rgba(0,0,0,0.15)'
                : '0 2px 4px rgba(0,0,0,0.1)'
            }}
          >
            {/* Drag handle icon */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '15px',
              flex: 1
            }}>
              <span style={{ fontSize: '20px', cursor: 'grab' }}>
                ⋮⋮
              </span>

              <div>
                <div style={{
                  fontSize: '16px',
                  fontWeight: 600,
                  marginBottom: '5px'
                }}>
                  {item.text}
                </div>
                <div style={{
                  fontSize: '12px',
                  color: '#666'
                }}>
                  Priority: {item.priority}
                </div>
              </div>
            </div>

            {/* Item number */}
            <div style={{
              background: 'rgba(0,0,0,0.1)',
              borderRadius: '50%',
              width: '32px',
              height: '32px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '14px',
              fontWeight: 'bold'
            }}>
              {index + 1}
            </div>
          </div>
        ))}
      </div>

      {/* DEBUG INFO */}
      <div style={{
        marginTop: '30px',
        padding: '15px',
        background: '#f5f5f5',
        borderRadius: '8px',
        fontSize: '13px',
        fontFamily: 'monospace'
      }}>
        <h3 style={{ marginTop: 0 }}>Debug Info</h3>
        <div>
          <strong>Dragging:</strong>{' '}
          {draggedItem ? draggedItem.text : 'None'}
        </div>
        <div>
          <strong>Hovering over:</strong>{' '}
          {draggedOverItem ? draggedOverItem.text : 'None'}
        </div>
        <div style={{ marginTop: '10px' }}>
          <strong>Current order:</strong> {items.map(item => item.id).join(' → ')}
        </div>
      </div>
    </div>
  );
}

export default DragAndDrop;
```

---

## How It Works: Step-by-Step

### Example: Dragging Item 2 to Item 4's Position

```
INITIAL STATE:
┌────────────┐
│  Item 1    │  index 0
├────────────┤
│  Item 2    │  index 1  ← User grabs this
├────────────┤
│  Item 3    │  index 2
├────────────┤
│  Item 4    │  index 3  ← User drops here
├────────────┤
│  Item 5    │  index 4
└────────────┘

STEP 1: dragstart on Item 2
  → setDraggedItem(Item 2)

STEP 2: User moves mouse over Item 4
  → dragenter on Item 4
  → dragover on Item 4 (fires repeatedly)
  → setDraggedOverItem(Item 4)

STEP 3: drop on Item 4
  → draggedIndex = 1
  → targetIndex = 3

  → newItems = [Item 1, Item 2, Item 3, Item 4, Item 5]
  → newItems.splice(1, 1)  // Remove Item 2
  → newItems = [Item 1, Item 3, Item 4, Item 5]

  → newItems.splice(3, 0, Item 2)  // Insert Item 2 at index 3
  → newItems = [Item 1, Item 3, Item 4, Item 2, Item 5]

  → setItems(newItems)

FINAL STATE:
┌────────────┐
│  Item 1    │  index 0
├────────────┤
│  Item 3    │  index 1  ← Moved up
├────────────┤
│  Item 4    │  index 2  ← Moved up
├────────────┤
│  Item 2    │  index 3  ← Moved down
├────────────┤
│  Item 5    │  index 4
└────────────┘
```

---

## Advanced: Drag Between Lists

```jsx
function KanbanBoard() {
  // Multiple lists (columns)
  const [columns, setColumns] = useState({
    todo: [
      { id: 1, text: 'Design mockups' },
      { id: 2, text: 'Write specs' }
    ],
    inProgress: [
      { id: 3, text: 'Implement login' }
    ],
    done: [
      { id: 4, text: 'Setup project' }
    ]
  });

  const [draggedItem, setDraggedItem] = useState(null);
  const [sourceColumn, setSourceColumn] = useState(null);

  const handleDragStart = (item, columnId) => {
    setDraggedItem(item);
    setSourceColumn(columnId);
  };

  const handleDrop = (targetColumnId, targetItem) => {
    if (!draggedItem || !sourceColumn) return;

    // Create copy of columns
    const newColumns = { ...columns };

    // Remove from source column
    newColumns[sourceColumn] = newColumns[sourceColumn].filter(
      item => item.id !== draggedItem.id
    );

    // Add to target column
    if (targetItem) {
      // Drop on specific item (insert at position)
      const targetIndex = newColumns[targetColumnId].findIndex(
        item => item.id === targetItem.id
      );
      newColumns[targetColumnId].splice(targetIndex, 0, draggedItem);
    } else {
      // Drop on empty space (append to end)
      newColumns[targetColumnId].push(draggedItem);
    }

    setColumns(newColumns);
    setDraggedItem(null);
    setSourceColumn(null);
  };

  return (
    <div style={{ display: 'flex', gap: '20px' }}>
      {Object.entries(columns).map(([columnId, items]) => (
        <div
          key={columnId}
          onDragOver={(e) => e.preventDefault()}
          onDrop={() => handleDrop(columnId, null)}  // Drop on column
          style={{
            flex: 1,
            minHeight: '400px',
            background: '#f5f5f5',
            padding: '20px',
            borderRadius: '8px'
          }}
        >
          <h3>{columnId}</h3>
          {items.map(item => (
            <div
              key={item.id}
              draggable
              onDragStart={() => handleDragStart(item, columnId)}
              onDrop={(e) => {
                e.stopPropagation();  // Prevent column drop
                handleDrop(columnId, item);  // Drop on item
              }}
              style={{
                padding: '15px',
                background: 'white',
                marginBottom: '10px',
                borderRadius: '5px',
                cursor: 'move'
              }}
            >
              {item.text}
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}
```

---

## Advanced: Drag Handles

Only allow dragging from specific area:

```jsx
function DragHandleExample() {
  const [items, setItems] = useState([...]);
  const [draggedItem, setDraggedItem] = useState(null);
  const dragHandleRef = useRef(null);

  const handleMouseDown = (e, item) => {
    // Check if mousedown was on drag handle
    if (dragHandleRef.current && dragHandleRef.current.contains(e.target)) {
      setDraggedItem(item);
    }
  };

  return (
    <div>
      {items.map(item => (
        <div
          key={item.id}
          draggable={draggedItem?.id === item.id}  // Only draggable when handle grabbed
          onMouseDown={(e) => handleMouseDown(e, item)}
          style={{ display: 'flex' }}
        >
          {/* Drag handle */}
          <div
            ref={dragHandleRef}
            style={{
              padding: '10px',
              cursor: 'grab',
              background: '#eee'
            }}
          >
            ⋮⋮
          </div>

          {/* Content (not draggable) */}
          <div style={{ padding: '10px', flex: 1 }}>
            {item.text}
          </div>
        </div>
      ))}
    </div>
  );
}
```

---

## Common Mistakes & How to Avoid Them

### ❌ Mistake 1: Forgetting preventDefault() on dragover

```jsx
// BAD: No preventDefault
<div onDragOver={handleDragOver}>
```

**Problem:** Drop event never fires!
**Fix:** Always call `e.preventDefault()`

```jsx
// GOOD: With preventDefault
<div onDragOver={(e) => e.preventDefault()}>

// Or in handler:
const handleDragOver = (e) => {
  e.preventDefault();  // ← Essential!
};
```

---

### ❌ Mistake 2: Mutating State

```jsx
// BAD: Mutating array
const handleDrop = (targetItem) => {
  const draggedIndex = items.indexOf(draggedItem);
  items.splice(draggedIndex, 1);  // ❌ Mutating state!
  setItems(items);  // Won't trigger re-render
};
```

**Fix:** Create new array

```jsx
// GOOD: Immutable update
const newItems = [...items];
newItems.splice(draggedIndex, 1);
setItems(newItems);
```

---

### ❌ Mistake 3: Not Checking for Same Item

```jsx
// BAD: No check
const handleDrop = (targetItem) => {
  // Reorders even if dropped on itself
  const draggedIndex = items.indexOf(draggedItem);
  const targetIndex = items.indexOf(targetItem);
  // ... splice operations
};
```

**Fix:** Early return if same

```jsx
// GOOD: Check first
const handleDrop = (targetItem) => {
  if (draggedItem.id === targetItem.id) return;  // ← Early exit
  // ... rest of logic
};
```

---

### ❌ Mistake 4: Using Index Instead of ID

```jsx
// BAD: Using array index
{items.map((item, index) => (
  <div key={index} onDragStart={() => setDraggedItem(index)}>
))}
```

**Problem:** Index changes when items reorder
**Fix:** Use stable ID

```jsx
// GOOD: Using item ID
{items.map((item) => (
  <div key={item.id} onDragStart={() => setDraggedItem(item)}>
))}
```

---

## Performance Considerations

### Optimization Tips

1. **Debounce dragover** (fires very frequently)
```jsx
const debouncedDragOver = useMemo(
  () => debounce((item) => setDraggedOverItem(item), 50),
  []
);
```

2. **Use CSS transforms** (not top/left)
```css
/* Fast - GPU accelerated */
transform: translateY(10px);

/* Slow - triggers layout */
top: 10px;
```

3. **Memoize items** (prevent re-renders)
```jsx
const DraggableItem = React.memo(({ item, onDragStart }) => (
  <div draggable onDragStart={() => onDragStart(item)}>
    {item.text}
  </div>
));
```

---

## Browser Compatibility

### DataTransfer API (Alternative Approach)

The complete solution uses state. For better browser compatibility, use DataTransfer:

```jsx
const handleDragStart = (e, item) => {
  e.dataTransfer.effectAllowed = 'move';
  e.dataTransfer.setData('text/plain', item.id);
  // Store data in drag event
};

const handleDrop = (e, targetItem) => {
  e.preventDefault();
  const draggedId = e.dataTransfer.getData('text/plain');
  // Retrieve data from drag event
  const draggedItem = items.find(item => item.id === draggedId);
  // ... reorder logic
};
```

**Pros:** Works without React state
**Cons:** Can't store complex objects (only strings)

---

## Interviewer Q&A

### Q1: How would you add animations during drag?

**Answer:**
```jsx
// CSS transitions
<div style={{
  transition: 'all 0.2s ease',
  transform: isDragging ? 'scale(1.05) rotate(2deg)' : 'scale(1)',
  boxShadow: isDragging ? '0 10px 20px rgba(0,0,0,0.2)' : 'none'
}}>

// Or use Framer Motion
import { motion, Reorder } from 'framer-motion';

<Reorder.Group values={items} onReorder={setItems}>
  {items.map(item => (
    <Reorder.Item key={item.id} value={item}>
      {item.text}
    </Reorder.Item>
  ))}
</Reorder.Group>
```

---

### Q2: How to support touch devices (mobile)?

**Answer:**
HTML5 drag API doesn't work well on touch devices. Use:

```jsx
// Touch event handlers
const [touchStart, setTouchStart] = useState(null);

<div
  onTouchStart={(e) => {
    setTouchStart({
      x: e.touches[0].clientX,
      y: e.touches[0].clientY,
      item: item
    });
  }}
  onTouchMove={(e) => {
    // Calculate delta, update position
  }}
  onTouchEnd={() => {
    // Determine drop target, reorder
  }}
>
```

Or use libraries: `react-dnd`, `react-beautiful-dnd`, `dnd-kit`

---

### Q3: How to implement copy-on-drag (hold Ctrl to copy)?

**Answer:**
```jsx
const handleDragStart = (e, item) => {
  setDraggedItem(item);

  // Check if Ctrl/Cmd key is pressed
  if (e.ctrlKey || e.metaKey) {
    e.dataTransfer.effectAllowed = 'copy';
    setIsCopyMode(true);
  } else {
    e.dataTransfer.effectAllowed = 'move';
    setIsCopyMode(false);
  }
};

const handleDrop = (targetItem) => {
  if (isCopyMode) {
    // Create copy
    const newItem = { ...draggedItem, id: Date.now() };
    setItems(prev => [...prev, newItem]);
  } else {
    // Move (normal behavior)
    // ... reorder logic
  }
};
```

---

### Q4: How would you prevent dragging certain items?

**Answer:**
```jsx
<div
  draggable={!item.locked}  // Don't make locked items draggable
  style={{
    cursor: item.locked ? 'not-allowed' : 'move',
    opacity: item.locked ? 0.5 : 1
  }}
>
  {item.locked && '🔒 '}{item.text}
</div>
```

---

### Q5: How to implement auto-scroll when dragging near edges?

**Answer:**
```jsx
const handleDragOver = (e) => {
  e.preventDefault();

  const container = e.currentTarget;
  const { top, bottom } = container.getBoundingClientRect();
  const mouseY = e.clientY;

  // Near top edge
  if (mouseY - top < 50) {
    container.scrollTop -= 10;
  }

  // Near bottom edge
  if (bottom - mouseY < 50) {
    container.scrollTop += 10;
  }
};
```

---

## Key Takeaways

### Essential Concepts

1. **HTML5 Drag Events**
   - `dragstart`, `drag`, `dragend` on draggable element
   - `dragenter`, `dragover`, `dragleave`, `drop` on drop target
   - Must call `preventDefault()` on `dragover`

2. **Reordering Algorithm**
   - Find indexes of dragged and target items
   - Use `splice()` to remove from old position
   - Use `splice()` to insert at new position
   - Immutable update for React

3. **Visual Feedback**
   - Reduce opacity of dragged item
   - Highlight drop targets
   - Show placeholder where item will be inserted
   - Smooth CSS transitions

4. **State Management**
   - Track draggedItem (what's being dragged)
   - Track draggedOverItem (current drop target)
   - Clear state on dragend

5. **Edge Cases**
   - Prevent dropping on itself
   - Handle empty lists
   - Support locked/disabled items
   - Mobile touch support

### When to Use Libraries

✅ **Build from scratch for:**
- Simple single-list reordering
- Learning/interview practice
- Custom behavior needed

✅ **Use libraries for production:**
- `dnd-kit` (modern, lightweight, accessible)
- `react-beautiful-dnd` (Atlassian, beautiful animations)
- `react-dnd` (complex use cases, highly customizable)

---

## Practice Exercises

1. **Basic**: Reorder items in a single list
2. **Intermediate**: Drag between two lists (like Trello)
3. **Advanced**: Build a Kanban board with 3+ columns
4. **Expert**: Add nested drag-and-drop (folders)

---

## Final Tips

1. **Always preventDefault()** on dragover - most common mistake!
2. **Use unique IDs** - never use array indexes
3. **Test on mobile** - touch events are different
4. **Add visual feedback** - users need to see what's happening
5. **Consider accessibility** - add keyboard alternatives

**Remember:** Drag and drop is about UX! Make it feel smooth and intuitive.

---

Good luck! This is a practical question that tests event handling, state management, and attention to UX details! 🎯
