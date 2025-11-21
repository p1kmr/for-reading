# File Explorer / Tree View

**Difficulty:** Hard
**Time:** 60-90 minutes
**Prerequisites:** Recursion, nested data structures, component composition

---

## Problem Statement

Build a nested file/folder explorer similar to VS Code's sidebar. Support expanding/collapsing folders, visual hierarchy, and file operations.

### Real-World Examples

```
VS Code Sidebar          macOS Finder          Windows Explorer
===============          ============          ================
📁 src                   📁 Documents          📁 My Computer
  📁 components            📁 Work               📁 C:
    📄 Header.jsx            📄 Report.pdf         📁 Users
    📄 Footer.jsx          📁 Personal               📁 John
  📁 utils                   📄 Photo.jpg              📁 Documents
    📄 helpers.js
📁 public
  📄 index.html
📄 package.json
```

**Used in:**
- Code editors (VS Code, WebStorm)
- File managers (Finder, Explorer)
- Documentation sites (navigation trees)
- Email clients (folder structure)
- Organization charts

---

## Requirements

### Must Have
- ✅ Display nested folder structure
- ✅ Expand/collapse folders
- ✅ Visual indentation for nesting levels
- ✅ Distinguish files from folders (icons)
- ✅ Click to expand/collapse

### Nice to Have
- ⭐ Add/delete/rename files
- ⭐ Drag and drop to move files
- ⭐ Search/filter functionality
- ⭐ Keyboard navigation (arrow keys)
- ⭐ Context menu (right-click)
- ⭐ Multi-select

---

## Data Structure

### Tree Structure Explained

A file system is naturally a **tree structure**:

```javascript
// Each node can be:
// 1. A FILE (leaf node - no children)
// 2. A FOLDER (branch node - can have children)

const fileTree = {
  id: 'root',           // Unique identifier
  name: 'root',         // Display name
  type: 'folder',       // 'file' or 'folder'
  children: [           // Only folders have children
    {
      id: '1',
      name: 'src',
      type: 'folder',
      children: [       // Nested structure (tree within tree)
        {
          id: '2',
          name: 'components',
          type: 'folder',
          children: [   // Can nest infinitely deep
            { id: '3', name: 'Header.jsx', type: 'file' },
            { id: '4', name: 'Footer.jsx', type: 'file' }
          ]
        },
        {
          id: '5',
          name: 'utils',
          type: 'folder',
          children: [
            { id: '6', name: 'helpers.js', type: 'file' }
          ]
        },
        { id: '7', name: 'App.jsx', type: 'file' }
      ]
    },
    { id: '8', name: 'package.json', type: 'file' }
  ]
};
```

### Why This Structure?

```
TREE STRUCTURE                     BENEFITS
==============                     ========
   Root                            ✅ Natural representation
    ├── src                        ✅ Easy to traverse
    │   ├── components             ✅ Mirrors file system
    │   │   ├── Header.jsx         ✅ Supports infinite nesting
    │   │   └── Footer.jsx         ✅ Recursive algorithms work well
    │   └── utils
    │       └── helpers.js
    └── package.json
```

---

## Recursive Component Pattern

### Why Recursion?

A folder can contain folders, which can contain folders... This is **recursion** in the data structure, so we use **recursion** in the component!

```jsx
function FileTreeNode({ node }) {
  return (
    <div>
      {/* Render current node */}
      <div>{node.name}</div>

      {/* If folder with children, render each child */}
      {node.children && node.children.map(child => (
        <FileTreeNode node={child} />  {/* ← Recursion! Component calls itself */}
      ))}
    </div>
  );
}
```

**How Recursion Works Here:**

```
Call Stack:
1. FileTreeNode({name: 'root', children: [...]})
   2. FileTreeNode({name: 'src', children: [...]})
      3. FileTreeNode({name: 'components', children: [...]})
         4. FileTreeNode({name: 'Header.jsx'})  ← Base case (no children)
         4. FileTreeNode({name: 'Footer.jsx'})  ← Base case (no children)
      3. FileTreeNode({name: 'utils', children: [...]})
         4. FileTreeNode({name: 'helpers.js'})  ← Base case (no children)
```

---

## Complete Solution

```jsx
import { useState } from 'react';

// ==========================================
// SAMPLE DATA
// ==========================================

const initialFileTree = {
  id: 'root',
  name: 'Project',
  type: 'folder',
  children: [
    {
      id: '1',
      name: 'src',
      type: 'folder',
      children: [
        {
          id: '2',
          name: 'components',
          type: 'folder',
          children: [
            { id: '3', name: 'Header.jsx', type: 'file' },
            { id: '4', name: 'Footer.jsx', type: 'file' },
            { id: '5', name: 'Navbar.jsx', type: 'file' }
          ]
        },
        {
          id: '6',
          name: 'utils',
          type: 'folder',
          children: [
            { id: '7', name: 'helpers.js', type: 'file' },
            { id: '8', name: 'constants.js', type: 'file' }
          ]
        },
        {
          id: '9',
          name: 'hooks',
          type: 'folder',
          children: [
            { id: '10', name: 'useAuth.js', type: 'file' },
            { id: '11', name: 'useFetch.js', type: 'file' }
          ]
        },
        { id: '12', name: 'App.jsx', type: 'file' },
        { id: '13', name: 'index.js', type: 'file' }
      ]
    },
    {
      id: '14',
      name: 'public',
      type: 'folder',
      children: [
        { id: '15', name: 'index.html', type: 'file' },
        { id: '16', name: 'favicon.ico', type: 'file' },
        { id: '17', name: 'robots.txt', type: 'file' }
      ]
    },
    {
      id: '18',
      name: 'tests',
      type: 'folder',
      children: [
        { id: '19', name: 'App.test.js', type: 'file' },
        { id: '20', name: 'setup.js', type: 'file' }
      ]
    },
    { id: '21', name: 'package.json', type: 'file' },
    { id: '22', name: 'README.md', type: 'file' },
    { id: '23', name: '.gitignore', type: 'file' }
  ]
};

// ==========================================
// MAIN COMPONENT
// ==========================================

function FileExplorer() {
  // ======================
  // STATE MANAGEMENT
  // ======================

  // Track which folders are expanded
  // Why object instead of array?
  // - O(1) lookup: expandedFolders['folder-1'] is instant
  // - Array would be O(n): includes('folder-1') scans entire array
  const [expandedFolders, setExpandedFolders] = useState({
    root: true,  // Root starts expanded
    '1': true,   // src starts expanded
    '2': true    // components starts expanded
  });

  // Track selected file/folder
  const [selectedId, setSelectedId] = useState(null);

  // ======================
  // EVENT HANDLERS
  // ======================

  // Toggle folder open/closed
  const toggleFolder = (folderId) => {
    setExpandedFolders(prev => ({
      ...prev,
      [folderId]: !prev[folderId]  // Flip boolean
    }));
  };

  // Select a file/folder
  const handleSelect = (nodeId) => {
    setSelectedId(nodeId);
  };

  // ======================
  // RENDER
  // ======================

  return (
    <div style={{
      padding: '20px',
      maxWidth: '800px',
      margin: '0 auto',
      fontFamily: 'system-ui, -apple-system, sans-serif'
    }}>
      <h1>File Explorer</h1>
      <p style={{ color: '#666', marginBottom: '20px' }}>
        Click folders to expand/collapse. Click files to select.
      </p>

      {/* File tree container */}
      <div
        style={{
          border: '1px solid #ddd',
          borderRadius: '8px',
          padding: '15px',
          background: '#fafafa',
          minHeight: '400px'
        }}
      >
        {/* Recursive tree starts here */}
        <FileTreeNode
          node={initialFileTree}
          level={0}
          expandedFolders={expandedFolders}
          toggleFolder={toggleFolder}
          selectedId={selectedId}
          onSelect={handleSelect}
        />
      </div>

      {/* Selected item info */}
      {selectedId && (
        <div style={{
          marginTop: '20px',
          padding: '15px',
          background: '#e8f4ff',
          borderRadius: '8px',
          border: '1px solid #b3d9ff'
        }}>
          <strong>Selected:</strong> {selectedId}
        </div>
      )}
    </div>
  );
}

// ==========================================
// RECURSIVE TREE NODE COMPONENT
// ==========================================

// This component calls itself for each child node
// Why recursive? Each folder can contain more folders (infinite nesting)
function FileTreeNode({
  node,              // Current node to render
  level,             // Nesting depth (for indentation)
  expandedFolders,   // Which folders are open
  toggleFolder,      // Function to toggle folder
  selectedId,        // Currently selected item
  onSelect           // Function to select item
}) {
  // ======================
  // DERIVED STATE
  // ======================

  const isFolder = node.type === 'folder';
  const isExpanded = expandedFolders[node.id];
  const hasChildren = node.children && node.children.length > 0;
  const isSelected = selectedId === node.id;

  // Calculate indentation based on nesting level
  // level 0: 0px
  // level 1: 20px
  // level 2: 40px, etc.
  const indentation = level * 20;

  // ======================
  // EVENT HANDLERS
  // ======================

  const handleClick = () => {
    if (isFolder) {
      toggleFolder(node.id);
    }
    onSelect(node.id);
  };

  // ======================
  // ICONS
  // ======================

  const getIcon = () => {
    if (isFolder) {
      return isExpanded ? '📂' : '📁';  // Open vs closed folder
    }

    // File icons based on extension
    const extension = node.name.split('.').pop();
    switch (extension) {
      case 'jsx':
      case 'js':
        return '📜';
      case 'json':
        return '⚙️';
      case 'md':
        return '📝';
      case 'html':
        return '🌐';
      case 'css':
        return '🎨';
      default:
        return '📄';
    }
  };

  // ======================
  // RENDER
  // ======================

  return (
    <div>
      {/* CURRENT NODE */}
      <div
        onClick={handleClick}
        style={{
          display: 'flex',
          alignItems: 'center',
          padding: '6px 10px',
          paddingLeft: `${indentation + 10}px`,
          cursor: 'pointer',
          background: isSelected ? '#d4e9ff' : 'transparent',
          borderRadius: '4px',
          transition: 'all 0.2s',
          userSelect: 'none',
          marginBottom: '2px'
        }}
        onMouseEnter={(e) => {
          if (!isSelected) {
            e.currentTarget.style.background = '#e8f4ff';
          }
        }}
        onMouseLeave={(e) => {
          if (!isSelected) {
            e.currentTarget.style.background = 'transparent';
          }
        }}
      >
        {/* Expand/Collapse Arrow (only for folders with children) */}
        {isFolder && hasChildren && (
          <span
            style={{
              marginRight: '6px',
              fontSize: '12px',
              transition: 'transform 0.2s',
              transform: isExpanded ? 'rotate(90deg)' : 'rotate(0deg)',
              display: 'inline-block',
              width: '12px',
              textAlign: 'center'
            }}
          >
            ▶
          </span>
        )}

        {/* Spacer for files (align with folders that have arrows) */}
        {(!isFolder || !hasChildren) && (
          <span style={{ marginRight: '6px', width: '12px', display: 'inline-block' }} />
        )}

        {/* Icon */}
        <span
          style={{
            marginRight: '8px',
            fontSize: '16px'
          }}
        >
          {getIcon()}
        </span>

        {/* Name */}
        <span
          style={{
            fontSize: '14px',
            fontWeight: isFolder ? 600 : 400,
            color: isFolder ? '#333' : '#666'
          }}
        >
          {node.name}
        </span>

        {/* File count (for folders) */}
        {isFolder && hasChildren && (
          <span style={{
            marginLeft: '8px',
            fontSize: '12px',
            color: '#999'
          }}>
            ({node.children.length})
          </span>
        )}
      </div>

      {/* CHILDREN (RECURSIVE PART) */}
      {/* Only render if:
          1. Node is a folder
          2. Folder has children
          3. Folder is expanded
      */}
      {isFolder && hasChildren && isExpanded && (
        <div>
          {node.children.map((child) => (
            // ← RECURSION HAPPENS HERE
            // Component calls itself for each child
            <FileTreeNode
              key={child.id}
              node={child}
              level={level + 1}  // Increment level for indentation
              expandedFolders={expandedFolders}
              toggleFolder={toggleFolder}
              selectedId={selectedId}
              onSelect={onSelect}
            />
          ))}
        </div>
      )}
    </div>
  );
}

export default FileExplorer;
```

---

## How It Works: Visual Breakdown

### Component Structure

```
<FileExplorer>                              State: expandedFolders, selectedId
  │
  └── <FileTreeNode node={root} level={0}>  Render: Project folder
       │
       ├── <FileTreeNode node={src} level={1}>  Render: src folder
       │    │
       │    ├── <FileTreeNode node={components} level={2}>  Render: components folder
       │    │    │
       │    │    ├── <FileTreeNode node={Header.jsx} level={3}>  Render: Header.jsx
       │    │    └── <FileTreeNode node={Footer.jsx} level={3}>  Render: Footer.jsx
       │    │
       │    └── <FileTreeNode node={utils} level={2}>  Render: utils folder
       │         └── <FileTreeNode node={helpers.js} level={3}>  Render: helpers.js
       │
       └── <FileTreeNode node={package.json} level={1}>  Render: package.json
```

### State Management Strategy

```javascript
// ❌ BAD: Array of expanded folder IDs
const [expanded, setExpanded] = useState(['root', 'src']);

// To check if folder is expanded:
const isExpanded = expanded.includes(folderId);  // O(n) - slow!

// To toggle:
setExpanded(prev =>
  prev.includes(id)
    ? prev.filter(x => x !== id)  // O(n)
    : [...prev, id]               // O(n)
);

// ✅ GOOD: Object mapping
const [expanded, setExpanded] = useState({
  root: true,
  src: true
});

// To check if folder is expanded:
const isExpanded = expanded[folderId];  // O(1) - fast!

// To toggle:
setExpanded(prev => ({
  ...prev,
  [id]: !prev[id]  // O(1)
}));
```

**Why object is better:**
- Lookup: O(1) vs O(n)
- Update: O(1) vs O(n)
- More readable: `expanded['folder-1']` vs `expanded.includes('folder-1')`

---

## Advanced: Add/Delete Files

```jsx
function FileExplorerAdvanced() {
  // Store entire tree in state (need to modify it)
  const [fileTree, setFileTree] = useState(initialFileTree);
  const [expandedFolders, setExpandedFolders] = useState({ root: true });

  // ======================
  // ADD FILE/FOLDER
  // ======================

  const addNode = (parentFolderId, nodeName, nodeType) => {
    const newNode = {
      id: Date.now().toString(),  // Simple ID generation
      name: nodeName,
      type: nodeType,
      ...(nodeType === 'folder' && { children: [] })
    };

    // Recursive function to find parent and add child
    const addToFolder = (node) => {
      // Found parent folder
      if (node.id === parentFolderId) {
        return {
          ...node,
          children: [...(node.children || []), newNode]
        };
      }

      // Keep searching in children
      if (node.children) {
        return {
          ...node,
          children: node.children.map(addToFolder)
        };
      }

      // Not found in this branch
      return node;
    };

    setFileTree(addToFolder(fileTree));

    // Auto-expand parent folder
    setExpandedFolders(prev => ({
      ...prev,
      [parentFolderId]: true
    }));
  };

  // ======================
  // DELETE FILE/FOLDER
  // ======================

  const deleteNode = (nodeId) => {
    // Recursive function to remove node
    const removeNode = (node) => {
      if (node.children) {
        return {
          ...node,
          children: node.children
            .filter(child => child.id !== nodeId)  // Remove matching child
            .map(removeNode)                       // Recurse on remaining children
        };
      }
      return node;
    };

    setFileTree(removeNode(fileTree));
  };

  // ======================
  // RENAME FILE/FOLDER
  // ======================

  const renameNode = (nodeId, newName) => {
    const updateName = (node) => {
      // Found node to rename
      if (node.id === nodeId) {
        return { ...node, name: newName };
      }

      // Keep searching
      if (node.children) {
        return {
          ...node,
          children: node.children.map(updateName)
        };
      }

      return node;
    };

    setFileTree(updateName(fileTree));
  };

  // ======================
  // MOVE FILE/FOLDER (Drag & Drop)
  // ======================

  const moveNode = (nodeId, targetFolderId) => {
    let nodeToMove = null;

    // Step 1: Find and remove node from tree
    const removeAndCapture = (node) => {
      if (node.children) {
        // Check if one of the children is the node we want
        const matchingChild = node.children.find(child => child.id === nodeId);
        if (matchingChild) {
          nodeToMove = matchingChild;
        }

        return {
          ...node,
          children: node.children
            .filter(child => child.id !== nodeId)
            .map(removeAndCapture)
        };
      }
      return node;
    };

    // Step 2: Add node to target folder
    const addToTarget = (node) => {
      if (node.id === targetFolderId && nodeToMove) {
        return {
          ...node,
          children: [...(node.children || []), nodeToMove]
        };
      }

      if (node.children) {
        return {
          ...node,
          children: node.children.map(addToTarget)
        };
      }

      return node;
    };

    let newTree = removeAndCapture(fileTree);
    newTree = addToTarget(newTree);
    setFileTree(newTree);
  };

  return (
    <div>
      {/* Render tree with add/delete/rename buttons */}
      <FileTreeNode
        node={fileTree}
        onAdd={addNode}
        onDelete={deleteNode}
        onRename={renameNode}
        onMove={moveNode}
      />
    </div>
  );
}
```

---

## Common Mistakes & How to Avoid Them

### ❌ Mistake 1: Not Using Unique Keys

```jsx
// BAD: Using index as key
{node.children.map((child, index) => (
  <FileTreeNode key={index} node={child} />
))}
```

**Problem:** React can't track items correctly when adding/removing
**Fix:** Use unique IDs

```jsx
// GOOD: Using stable ID
{node.children.map((child) => (
  <FileTreeNode key={child.id} node={child} />
))}
```

---

### ❌ Mistake 2: Forgetting to Prevent Infinite Recursion

```jsx
// BAD: No base case
function FileTreeNode({ node }) {
  return (
    <div>
      {node.name}
      {node.children.map(child => <FileTreeNode node={child} />)}
    </div>
  );
}
```

**Problem:** Crashes if file has `children` by mistake
**Fix:** Check if folder AND has children

```jsx
// GOOD: Proper base case
function FileTreeNode({ node }) {
  const hasChildren = node.children && node.children.length > 0;

  return (
    <div>
      {node.name}
      {node.type === 'folder' && hasChildren && (
        node.children.map(child => <FileTreeNode node={child} />)
      )}
    </div>
  );
}
```

---

### ❌ Mistake 3: Mutating State Directly

```jsx
// BAD: Mutating state
const addFile = (folderId, file) => {
  const folder = findFolder(fileTree, folderId);
  folder.children.push(file);  // ❌ Mutation!
  setFileTree(fileTree);       // Won't trigger re-render
};
```

**Problem:** React won't detect change, no re-render
**Fix:** Create new objects/arrays

```jsx
// GOOD: Immutable update
const addFile = (folderId, file) => {
  const addToFolder = (node) => {
    if (node.id === folderId) {
      return {
        ...node,                              // New object
        children: [...node.children, file]    // New array
      };
    }
    // ... rest of logic
  };
  setFileTree(addToFolder(fileTree));
};
```

---

### ❌ Mistake 4: Not Handling Edge Cases

```jsx
// BAD: Assumes children always exists
const folderCount = node.children.length;  // ❌ Crashes if children is undefined
```

**Fix:** Always check

```jsx
// GOOD: Defensive programming
const folderCount = node.children?.length || 0;
// or
const folderCount = (node.children && node.children.length) || 0;
```

---

## Performance Considerations

### Optimization Techniques

1. **Memoize Tree Nodes**
```jsx
const FileTreeNode = React.memo(({ node, level, ...props }) => {
  // Only re-render if props actually change
}, (prevProps, nextProps) => {
  return (
    prevProps.node === nextProps.node &&
    prevProps.level === nextProps.level &&
    prevProps.expandedFolders[prevProps.node.id] ===
      nextProps.expandedFolders[nextProps.node.id]
  );
});
```

2. **Virtualize for Large Trees**
```jsx
// If tree has 10,000+ nodes, use virtual scrolling
import { FixedSizeTree } from 'react-vtree';
```

3. **Lazy Load Children**
```jsx
const [children, setChildren] = useState(null);

const loadChildren = async (folderId) => {
  const data = await fetchFolderContents(folderId);
  setChildren(data);
};

useEffect(() => {
  if (isExpanded && !children) {
    loadChildren(node.id);
  }
}, [isExpanded]);
```

---

## Interviewer Q&A

### Q1: How would you implement search/filter in the tree?

**Answer:**
```jsx
const [searchTerm, setSearchTerm] = useState('');

// Filter tree to only show matching nodes (and their parents)
const filterTree = (node, term) => {
  // Base case: node matches
  if (node.name.toLowerCase().includes(term.toLowerCase())) {
    return node;
  }

  // Recursive case: check children
  if (node.children) {
    const filteredChildren = node.children
      .map(child => filterTree(child, term))
      .filter(Boolean);  // Remove null results

    if (filteredChildren.length > 0) {
      return {
        ...node,
        children: filteredChildren
      };
    }
  }

  return null;  // No match in this branch
};

const filteredTree = searchTerm
  ? filterTree(fileTree, searchTerm)
  : fileTree;
```

---

### Q2: How would you persist expanded state to localStorage?

**Answer:**
```jsx
const [expandedFolders, setExpandedFolders] = useState(() => {
  const saved = localStorage.getItem('expandedFolders');
  return saved ? JSON.parse(saved) : { root: true };
});

useEffect(() => {
  localStorage.setItem('expandedFolders', JSON.stringify(expandedFolders));
}, [expandedFolders]);
```

---

### Q3: What about keyboard navigation?

**Answer:**
```jsx
const [focusedId, setFocusedId] = useState(null);

const handleKeyDown = (e, node) => {
  switch (e.key) {
    case 'ArrowRight':
      if (node.type === 'folder' && !expandedFolders[node.id]) {
        toggleFolder(node.id);
      }
      break;
    case 'ArrowLeft':
      if (node.type === 'folder' && expandedFolders[node.id]) {
        toggleFolder(node.id);
      }
      break;
    case 'ArrowDown':
      // Focus next node in tree
      break;
    case 'ArrowUp':
      // Focus previous node in tree
      break;
    case 'Enter':
      // Open file or toggle folder
      break;
  }
};
```

---

### Q4: How to handle very deep nesting (1000+ levels)?

**Answer:**
- **Set max depth**: Prevent rendering beyond certain level
- **Use iteration instead of recursion**: Avoid call stack overflow
- **Virtualize**: Only render visible nodes

```jsx
const MAX_DEPTH = 50;

function FileTreeNode({ node, level }) {
  if (level > MAX_DEPTH) {
    return <div>Maximum nesting depth reached</div>;
  }
  // ... rest of component
}
```

---

### Q5: How would you implement multi-select?

**Answer:**
```jsx
const [selectedIds, setSelectedIds] = useState(new Set());

const handleSelect = (nodeId, e) => {
  setSelectedIds(prev => {
    const newSet = new Set(prev);

    if (e.ctrlKey || e.metaKey) {
      // Multi-select: toggle
      if (newSet.has(nodeId)) {
        newSet.delete(nodeId);
      } else {
        newSet.add(nodeId);
      }
    } else if (e.shiftKey) {
      // Range select: select from last to current
      // (complex - need to track tree order)
    } else {
      // Single select: clear others
      newSet.clear();
      newSet.add(nodeId);
    }

    return newSet;
  });
};
```

---

## Key Takeaways

### Essential Concepts

1. **Recursive Components**
   - Component renders itself for nested children
   - Natural fit for tree structures
   - Base case prevents infinite recursion

2. **State Management**
   - Use objects for O(1) lookups
   - Keep tree data separate from UI state
   - Immutable updates for React

3. **Indentation = Depth**
   - Track nesting level
   - Multiply by constant (20px)
   - Visual hierarchy

4. **Expand/Collapse State**
   - Track by folder ID
   - Object lookup is faster than array
   - Can persist to localStorage

5. **Recursive Tree Operations**
   - Add: Recursively find parent, add child
   - Delete: Recursively filter children
   - Update: Recursively map and update

### When to Use This Pattern

✅ **Good for:**
- File systems
- Organization charts
- Navigation menus
- Category hierarchies
- Comment threads
- JSON visualizers

❌ **Not ideal for:**
- Flat lists (use simple map)
- Very wide trees (thousands of direct children)
- Frequently changing structure (performance issues)

---

## Practice Exercises

1. **Basic**: Implement expand/collapse
2. **Intermediate**: Add file creation/deletion
3. **Advanced**: Implement drag-and-drop to move files
4. **Expert**: Add search with highlighted results

---

## Final Tips

1. **Start simple**: Get basic expand/collapse working first
2. **Test edge cases**: Empty folders, single item, deep nesting
3. **Think recursively**: If data is recursive, component should be too
4. **Optimize later**: Get it working, then optimize with memo/virtualization
5. **Use libraries in production**: `react-complex-tree`, `react-arborist`

**Remember:** This pattern tests your understanding of recursion and tree data structures - fundamental CS concepts!

---

Good luck! File explorers are a classic interview question that appears frequently at companies building developer tools! 🌲
