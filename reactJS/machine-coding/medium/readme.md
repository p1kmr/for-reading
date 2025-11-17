# ReactJS Medium Level - Machine Coding Questions

**Difficulty:** Intermediate
**Time:** 45-60 minutes per question
**Prerequisites:** useState, useEffect, API calls, performance optimization

---

## Table of Contents

1. [Pagination Component](#1-pagination-component)
2. [Infinite Scroll](#2-infinite-scroll)
3. [Auto-complete / Type-ahead](#3-auto-complete--type-ahead)
4. [Debounce Search](#4-debounce-search)
5. [Modal / Dialog](#5-modal--dialog)

---

# 1. Pagination Component

## =Ë Problem Statement

Build a pagination component that displays a large list of items across multiple pages. Users can navigate between pages using Previous/Next buttons and page numbers.

### Example:
```
Items 1-10 of 100

[Item 1]
[Item 2]
...
[Item 10]

[Previous] [1] [2] [3] ... [10] [Next]
```

---

## <¯ Requirements

**Must Have:**
-  Display items for current page
-  Page number buttons
-  Previous/Next buttons
-  Disable buttons at boundaries
-  Highlight current page

**Nice to Have:**
- P Page size selector (10, 20, 50 items)
- P Jump to page input
- P Show total pages and items

---

## > Pagination Logic

### Key Concepts:

```javascript
// Given:
totalItems = 100
itemsPerPage = 10

// Calculate:
totalPages = Math.ceil(totalItems / itemsPerPage) // 10 pages

// For page 3:
startIndex = (currentPage - 1) * itemsPerPage // (3-1)*10 = 20
endIndex = startIndex + itemsPerPage          // 20 + 10 = 30
currentItems = allItems.slice(startIndex, endIndex) // Items 20-29
```

### State Needed:
1. `currentPage` - which page user is on
2. `itemsPerPage` - how many items per page
3. All items data

---

## =¡ Complete Solution

```jsx
import { useState } from 'react';

function Pagination() {
  // Sample data (in real app, this comes from API)
  const allItems = Array.from({ length: 100 }, (_, i) => ({
    id: i + 1,
    name: `Item ${i + 1}`
  }));

  // State: Current page number
  // Why useState? Page changes when user clicks navigation
  const [currentPage, setCurrentPage] = useState(1);

  // Configuration: Items per page
  // Why not state? This rarely changes, can be a constant
  const itemsPerPage = 10;

  // Calculate total pages
  // Why Math.ceil? If 95 items and 10 per page, need 10 pages (not 9.5)
  const totalPages = Math.ceil(allItems.length / itemsPerPage);

  // Calculate items for current page
  // Why calculate? Derived data, no need to store in state
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = allItems.slice(indexOfFirstItem, indexOfLastItem);

  // Event handlers
  const handleNextPage = () => {
    setCurrentPage(prev => Math.min(prev + 1, totalPages));
    // Why Math.min? Prevent going beyond last page
  };

  const handlePrevPage = () => {
    setCurrentPage(prev => Math.max(prev - 1, 1));
    // Why Math.max? Prevent going below page 1
  };

  const handlePageClick = (pageNumber) => {
    setCurrentPage(pageNumber);
  };

  // Generate page numbers array
  // Why function? Logic for showing limited page numbers (e.g., 1 ... 5 6 7 ... 10)
  const getPageNumbers = () => {
    const pages = [];
    const maxPagesToShow = 5;

    if (totalPages <= maxPagesToShow) {
      // Show all pages if total is small
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Show limited pages with ellipsis
      // Always show: first page, last page, current page, and 2 around current
      pages.push(1);

      let startPage = Math.max(2, currentPage - 1);
      let endPage = Math.min(totalPages - 1, currentPage + 1);

      if (startPage > 2) pages.push('...');

      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }

      if (endPage < totalPages - 1) pages.push('...');

      pages.push(totalPages);
    }

    return pages;
  };

  return (
    <div style={{ maxWidth: '800px', margin: '50px auto', padding: '20px' }}>
      <h1>Pagination Demo</h1>

      {/* Stats */}
      <div style={{ marginBottom: '20px', color: '#666' }}>
        Showing {indexOfFirstItem + 1}-{Math.min(indexOfLastItem, allItems.length)} of {allItems.length} items
      </div>

      {/* Items List */}
      <div style={{ marginBottom: '30px' }}>
        {currentItems.map(item => (
          <div
            key={item.id}
            style={{
              padding: '15px',
              margin: '10px 0',
              background: '#f9f9f9',
              borderRadius: '5px'
            }}
          >
            {item.name}
          </div>
        ))}
      </div>

      {/* Pagination Controls */}
      <div style={{ display: 'flex', gap: '5px', justifyContent: 'center', alignItems: 'center' }}>
        {/* Previous Button */}
        <button
          onClick={handlePrevPage}
          disabled={currentPage === 1}
          // Why disabled? Can't go before page 1
          style={{
            padding: '8px 16px',
            cursor: currentPage === 1 ? 'not-allowed' : 'pointer',
            opacity: currentPage === 1 ? 0.5 : 1
          }}
        >
          Previous
        </button>

        {/* Page Numbers */}
        {getPageNumbers().map((page, index) => (
          page === '...' ? (
            <span key={`ellipsis-${index}`} style={{ padding: '8px' }}>...</span>
          ) : (
            <button
              key={page}
              onClick={() => handlePageClick(page)}
              style={{
                padding: '8px 12px',
                background: currentPage === page ? '#007bff' : 'white',
                color: currentPage === page ? 'white' : 'black',
                border: '1px solid #ddd',
                cursor: 'pointer',
                fontWeight: currentPage === page ? 'bold' : 'normal'
              }}
            >
              {page}
            </button>
          )
        ))}

        {/* Next Button */}
        <button
          onClick={handleNextPage}
          disabled={currentPage === totalPages}
          style={{
            padding: '8px 16px',
            cursor: currentPage === totalPages ? 'not-allowed' : 'pointer',
            opacity: currentPage === totalPages ? 0.5 : 1
          }}
        >
          Next
        </button>
      </div>

      {/* Page Info */}
      <div style={{ textAlign: 'center', marginTop: '20px', color: '#666' }}>
        Page {currentPage} of {totalPages}
      </div>
    </div>
  );
}

export default Pagination;
```

---

## >à Key Concepts Explained

### Why Calculate Instead of Store?

```jsx
// L WRONG: Store derived data in state
const [currentItems, setCurrentItems] = useState([]);
useEffect(() => {
  const items = allItems.slice(start, end);
  setCurrentItems(items);
}, [currentPage]);

//  CORRECT: Calculate from existing data
const currentItems = allItems.slice(
  (currentPage - 1) * itemsPerPage,
  currentPage * itemsPerPage
);
```

**Why?** Derived data should be calculated, not stored. Less state = less bugs.

### Boundary Checks

```jsx
// Prevent going beyond limits
const handleNextPage = () => {
  setCurrentPage(prev => Math.min(prev + 1, totalPages));
};

const handlePrevPage = () => {
  setCurrentPage(prev => Math.max(prev - 1, 1));
};
```

---

## =€ Advanced: Server-Side Pagination

```jsx
import { useState, useEffect } from 'react';

function PaginationAPI() {
  const [items, setItems] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  // Fetch data when page changes
  // Why useEffect? Side effect (API call) triggered by page change
  useEffect(() => {
    const fetchItems = async () => {
      setLoading(true);
      try {
        const response = await fetch(
          `/api/items?page=${currentPage}&limit=10`
        );
        const data = await response.json();

        setItems(data.items);
        setTotalPages(data.totalPages);
      } catch (error) {
        console.error('Failed to fetch items:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchItems();
  }, [currentPage]); // Re-fetch when page changes

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      {items.map(item => (
        <div key={item.id}>{item.name}</div>
      ))}

      <button
        onClick={() => setCurrentPage(prev => prev - 1)}
        disabled={currentPage === 1}
      >
        Previous
      </button>

      <span>Page {currentPage} of {totalPages}</span>

      <button
        onClick={() => setCurrentPage(prev => prev + 1)}
        disabled={currentPage === totalPages}
      >
        Next
      </button>
    </div>
  );
}
```

---

## = Common Mistakes

### Mistake 1: 0-Based vs 1-Based Indexing

```jsx
// L WRONG: Using 0-based page numbers (confusing for users)
const [currentPage, setCurrentPage] = useState(0); // Page 0, 1, 2...

//  CORRECT: Use 1-based (Page 1, 2, 3...)
const [currentPage, setCurrentPage] = useState(1);
```

### Mistake 2: Not Handling Edge Cases

```jsx
// L WRONG: Can go to page 0 or beyond totalPages
const handleNextPage = () => {
  setCurrentPage(currentPage + 1); // No limit!
};

//  CORRECT: Check boundaries
const handleNextPage = () => {
  if (currentPage < totalPages) {
    setCurrentPage(currentPage + 1);
  }
};
```

---

## <¤ Interviewer Q&A

**Q: How would you handle large page numbers (e.g., 1000 pages)?**

A: "I'd show limited page numbers with ellipsis:
`[Prev] [1] ... [48] [49] [50] ... [1000] [Next]`

Show first page, last page, current page, and a few around current."

**Q: How would you add a 'Jump to Page' feature?**

```jsx
const [jumpToPage, setJumpToPage] = useState('');

const handleJumpToPage = () => {
  const pageNum = parseInt(jumpToPage);
  if (pageNum >= 1 && pageNum <= totalPages) {
    setCurrentPage(pageNum);
    setJumpToPage('');
  }
};

return (
  <div>
    <input
      type="number"
      value={jumpToPage}
      onChange={(e) => setJumpToPage(e.target.value)}
      placeholder="Page number"
    />
    <button onClick={handleJumpToPage}>Go</button>
  </div>
);
```

---

# 2. Infinite Scroll

## =Ë Problem Statement

Load more items automatically when user scrolls to the bottom of the page. This is common in social media feeds (Twitter, Instagram).

### Example:
```
[Item 1]
[Item 2]
...
[Item 20]
(user scrolls down)
’ Load items 21-40
[Item 21]
...
```

---

## <¯ Requirements

**Must Have:**
-  Display initial items
-  Detect when user scrolls to bottom
-  Load more items automatically
-  Show loading indicator
-  Handle "no more items" state

---

## > Scroll Detection Logic

```javascript
// Detect if user scrolled to bottom:
window.innerHeight          // Viewport height
document.documentElement.scrollTop  // How far scrolled from top
document.documentElement.scrollHeight  // Total document height

// At bottom when:
scrollTop + innerHeight >= scrollHeight - threshold
```

---

## =¡ Complete Solution

```jsx
import { useState, useEffect, useRef } from 'react';

function InfiniteScroll() {
  // State: Current items
  const [items, setItems] = useState([]);

  // State: Page number for fetching
  const [page, setPage] = useState(1);

  // State: Loading indicator
  const [loading, setLoading] = useState(false);

  // State: Has more items to load?
  const [hasMore, setHasMore] = useState(true);

  // useRef: Track if currently loading (prevent duplicate requests)
  // Why useRef? Value persists but doesn't cause re-render
  const loadingRef = useRef(false);

  // Simulate API call to fetch items
  const fetchItems = async (pageNumber) => {
    // Prevent duplicate requests
    if (loadingRef.current) return;

    loadingRef.current = true;
    setLoading(true);

    try {
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 1000));

      // Simulate fetching 20 items per page
      const newItems = Array.from({ length: 20 }, (_, i) => ({
        id: (pageNumber - 1) * 20 + i + 1,
        name: `Item ${(pageNumber - 1) * 20 + i + 1}`,
        description: `Description for item ${(pageNumber - 1) * 20 + i + 1}`
      }));

      // Add new items to existing items
      setItems(prevItems => [...prevItems, ...newItems]);

      // Stop loading after 100 items (simulate end of data)
      if (pageNumber >= 5) {
        setHasMore(false);
      }

    } catch (error) {
      console.error('Failed to fetch items:', error);
    } finally {
      setLoading(false);
      loadingRef.current = false;
    }
  };

  // Load initial items
  // Why useEffect with empty deps? Run once on mount
  useEffect(() => {
    fetchItems(1);
  }, []);

  // Handle scroll event
  // Why useEffect? Set up event listener (side effect)
  useEffect(() => {
    const handleScroll = () => {
      // Skip if already loading or no more items
      if (loading || !hasMore) return;

      // Calculate if user scrolled to bottom
      const scrollTop = document.documentElement.scrollTop;
      const scrollHeight = document.documentElement.scrollHeight;
      const clientHeight = document.documentElement.clientHeight;

      // Trigger load when 200px from bottom
      // Why 200px threshold? Start loading before reaching absolute bottom (better UX)
      if (scrollTop + clientHeight >= scrollHeight - 200) {
        setPage(prevPage => prevPage + 1);
      }
    };

    // Add event listener
    window.addEventListener('scroll', handleScroll);

    // Cleanup: Remove listener on unmount
    // Why cleanup? Prevent memory leaks
    return () => window.removeEventListener('scroll', handleScroll);
  }, [loading, hasMore]); // Re-run when loading or hasMore changes

  // Fetch more items when page changes
  // Why separate useEffect? Separation of concerns
  useEffect(() => {
    if (page > 1) { // Don't fetch on initial render (already fetched in first useEffect)
      fetchItems(page);
    }
  }, [page]);

  return (
    <div style={{ maxWidth: '800px', margin: '50px auto', padding: '20px' }}>
      <h1>Infinite Scroll Demo</h1>

      {/* Items List */}
      <div>
        {items.map(item => (
          <div
            key={item.id}
            style={{
              padding: '20px',
              margin: '10px 0',
              background: '#f9f9f9',
              borderRadius: '5px'
            }}
          >
            <h3>{item.name}</h3>
            <p>{item.description}</p>
          </div>
        ))}
      </div>

      {/* Loading Indicator */}
      {loading && (
        <div style={{ textAlign: 'center', padding: '20px', color: '#666' }}>
          Loading more items...
        </div>
      )}

      {/* No More Items */}
      {!hasMore && (
        <div style={{ textAlign: 'center', padding: '20px', color: '#999' }}>
          No more items to load
        </div>
      )}
    </div>
  );
}

export default InfiniteScroll;
```

---

## >à Key Concepts

### Why useRef for Loading Flag?

```jsx
const loadingRef = useRef(false);

//  Use useRef:
// - Value persists across renders
// - Updating doesn't cause re-render
// - Good for tracking flags

// L Don't use useState for this:
// const [loadingFlag, setLoadingFlag] = useState(false);
// - Would cause unnecessary re-renders
```

### Scroll Event Optimization

```jsx
// Problem: Scroll event fires many times per second
// Solution 1: Add threshold (200px before bottom)
if (scrollTop + clientHeight >= scrollHeight - 200) {
  loadMore();
}

// Solution 2: Debounce (covered in next problem)
// Solution 3: Use Intersection Observer (advanced)
```

---

## =€ Advanced: Using Intersection Observer

```jsx
import { useState, useEffect, useRef } from 'react';

function InfiniteScrollIO() {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);

  // Ref for the "load more" trigger element
  const observerTarget = useRef(null);

  // Fetch items function (same as before)
  const fetchItems = async (pageNum) => {
    // ... fetch logic
  };

  // Set up Intersection Observer
  // Why IO? More efficient than scroll events
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        // entries[0].isIntersecting = true when element is visible
        if (entries[0].isIntersecting && hasMore && !loading) {
          setPage(prev => prev + 1);
        }
      },
      { threshold: 1.0 } // Trigger when 100% of element is visible
    );

    if (observerTarget.current) {
      observer.observe(observerTarget.current);
    }

    return () => {
      if (observerTarget.current) {
        observer.unobserve(observerTarget.current);
      }
    };
  }, [hasMore, loading]);

  return (
    <div>
      {items.map(item => (
        <div key={item.id}>{item.name}</div>
      ))}

      {/* Observer target element */}
      <div ref={observerTarget} style={{ height: '20px' }}>
        {loading && <p>Loading...</p>}
      </div>
    </div>
  );
}
```

---

## = Common Mistakes

### Mistake 1: Duplicate Fetch Requests

```jsx
// L WRONG: Can trigger multiple fetches
const handleScroll = () => {
  if (atBottom) {
    fetchItems(); // Fires multiple times!
  }
};

//  CORRECT: Use loading flag
const handleScroll = () => {
  if (atBottom && !loading) {
    fetchItems();
  }
};
```

### Mistake 2: Not Cleaning Up Event Listener

```jsx
// L WRONG: Memory leak!
useEffect(() => {
  window.addEventListener('scroll', handleScroll);
  // Missing cleanup!
}, []);

//  CORRECT: Clean up
useEffect(() => {
  window.addEventListener('scroll', handleScroll);
  return () => window.removeEventListener('scroll', handleScroll);
}, []);
```

---

# 3. Auto-complete / Type-ahead

## =Ë Problem Statement

Build a search input that shows suggestions as the user types. Common in search bars (Google, Amazon).

### Example:
```
Search: "reac" [___]

Suggestions:
- React.js
- React Native
- React Router
- React Hooks
```

---

## <¯ Requirements

**Must Have:**
-  Input field
-  Show suggestions matching input
-  Filter suggestions as user types
-  Click suggestion to select
-  Keyboard navigation (arrow keys, enter)

---

## =¡ Complete Solution

```jsx
import { useState, useRef, useEffect } from 'react';

function Autocomplete() {
  // Sample data (in real app, fetch from API)
  const allSuggestions = [
    'React.js',
    'React Native',
    'React Router',
    'React Hooks',
    'Redux',
    'JavaScript',
    'TypeScript',
    'Node.js',
    'Next.js',
    'Vue.js',
    'Angular'
  ];

  // State: Input value
  const [inputValue, setInputValue] = useState('');

  // State: Filtered suggestions
  const [suggestions, setSuggestions] = useState([]);

  // State: Show/hide suggestions dropdown
  const [showSuggestions, setShowSuggestions] = useState(false);

  // State: Selected suggestion index (for keyboard navigation)
  const [selectedIndex, setSelectedIndex] = useState(-1);

  // Ref: Input element (for focus management)
  const inputRef = useRef(null);

  // Filter suggestions based on input
  // Why useEffect? Run filtering whenever input changes
  useEffect(() => {
    if (inputValue.trim() === '') {
      setSuggestions([]);
      setShowSuggestions(false);
      return;
    }

    // Filter suggestions that include input text (case-insensitive)
    const filtered = allSuggestions.filter(suggestion =>
      suggestion.toLowerCase().includes(inputValue.toLowerCase())
    );

    setSuggestions(filtered);
    setShowSuggestions(filtered.length > 0);
    setSelectedIndex(-1); // Reset selection when suggestions change
  }, [inputValue]);

  // Handle input change
  const handleInputChange = (e) => {
    setInputValue(e.target.value);
  };

  // Handle suggestion click
  const handleSuggestionClick = (suggestion) => {
    setInputValue(suggestion);
    setShowSuggestions(false);
    setSuggestions([]);
  };

  // Handle keyboard navigation
  const handleKeyDown = (e) => {
    if (!showSuggestions) return;

    switch (e.key) {
      case 'ArrowDown':
        // Move selection down
        e.preventDefault(); // Prevent cursor movement
        setSelectedIndex(prev =>
          prev < suggestions.length - 1 ? prev + 1 : prev
        );
        break;

      case 'ArrowUp':
        // Move selection up
        e.preventDefault();
        setSelectedIndex(prev => (prev > 0 ? prev - 1 : -1));
        break;

      case 'Enter':
        // Select highlighted suggestion
        e.preventDefault();
        if (selectedIndex >= 0) {
          handleSuggestionClick(suggestions[selectedIndex]);
        }
        break;

      case 'Escape':
        // Close suggestions
        setShowSuggestions(false);
        break;

      default:
        break;
    }
  };

  // Close suggestions when clicking outside
  // Why useEffect? Set up global click listener
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (inputRef.current && !inputRef.current.contains(event.target)) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div style={{ maxWidth: '600px', margin: '50px auto', padding: '20px' }}>
      <h1>Auto-complete Search</h1>

      <div style={{ position: 'relative' }} ref={inputRef}>
        {/* Input */}
        <input
          type="text"
          value={inputValue}
          onChange={handleInputChange}
          onKeyDown={handleKeyDown}
          placeholder="Search..."
          style={{
            width: '100%',
            padding: '12px',
            fontSize: '16px',
            border: '2px solid #ddd',
            borderRadius: '5px'
          }}
        />

        {/* Suggestions Dropdown */}
        {showSuggestions && (
          <ul
            style={{
              position: 'absolute',
              top: '100%',
              left: 0,
              right: 0,
              margin: 0,
              padding: 0,
              listStyle: 'none',
              background: 'white',
              border: '1px solid #ddd',
              borderTop: 'none',
              borderRadius: '0 0 5px 5px',
              maxHeight: '200px',
              overflowY: 'auto',
              zIndex: 1000
            }}
          >
            {suggestions.map((suggestion, index) => (
              <li
                key={suggestion}
                onClick={() => handleSuggestionClick(suggestion)}
                style={{
                  padding: '10px 12px',
                  cursor: 'pointer',
                  background: index === selectedIndex ? '#e3f2fd' : 'white',
                  // Highlight selected item
                  borderBottom: index < suggestions.length - 1 ? '1px solid #eee' : 'none'
                }}
                onMouseEnter={() => setSelectedIndex(index)}
                // Update selection on hover
              >
                {suggestion}
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Selected Value Display */}
      {inputValue && (
        <div style={{ marginTop: '20px', color: '#666' }}>
          Selected: <strong>{inputValue}</strong>
        </div>
      )}
    </div>
  );
}

export default Autocomplete;
```

---

## >à Key Concepts

### Keyboard Navigation

```javascript
// Arrow Down: Move selection down
setSelectedIndex(prev => Math.min(prev + 1, suggestions.length - 1));

// Arrow Up: Move selection up
setSelectedIndex(prev => Math.max(prev - 1, -1));

// Enter: Select current
if (selectedIndex >= 0) {
  selectSuggestion(suggestions[selectedIndex]);
}
```

### Click Outside Detection

```jsx
useEffect(() => {
  const handleClickOutside = (event) => {
    if (containerRef.current && !containerRef.current.contains(event.target)) {
      closeSuggestions();
    }
  };

  document.addEventListener('mousedown', handleClickOutside);
  return () => document.removeEventListener('mousedown', handleClickOutside);
}, []);
```

---

# 4. Debounce Search

## =Ë Problem Statement

Implement a search input that debounces API calls. Instead of calling the API on every keystroke, wait for the user to stop typing.

### Example:
```
User types: "r" ’ Wait
User types: "re" ’ Wait
User types: "rea" ’ Wait
... (pause 500ms) ’ Call API with "react"
```

---

## <¯ Why Debouncing?

**Problem without debounce:**
```
User types "react" (5 characters)
’ 5 API calls fired!
L Wastes bandwidth
L Slows down app
L Costs money (API charges)
```

**Solution with debounce:**
```
User types "react"
Wait 500ms after last keystroke
’ 1 API call fired!
 Efficient
 Fast
 Cost-effective
```

---

## =¡ Complete Solution

```jsx
import { useState, useEffect } from 'react';

function DebounceSearch() {
  // State: Search query
  const [searchQuery, setSearchQuery] = useState('');

  // State: Search results
  const [results, setResults] = useState([]);

  // State: Loading
  const [loading, setLoading] = useState(false);

  // Debounced search
  // Why useEffect? Run side effect (API call) when searchQuery changes
  useEffect(() => {
    // Don't search for empty query
    if (!searchQuery.trim()) {
      setResults([]);
      return;
    }

    // Set loading state immediately
    setLoading(true);

    // Create timer
    // Why setTimeout? Delay API call until user stops typing
    const timerId = setTimeout(() => {
      // This code runs after 500ms delay
      performSearch(searchQuery);
    }, 500); // 500ms debounce delay

    // Cleanup function
    // Why cleanup? Cancel timer if user types again before 500ms
    return () => {
      clearTimeout(timerId);
      // If user types "a", then "b" within 500ms:
      // 1. Timer for "a" is cleared (cancelled)
      // 2. New timer for "ab" is created
      // Result: Only final value "ab" triggers API call
    };
  }, [searchQuery]); // Re-run when searchQuery changes

  // Perform search (simulate API call)
  const performSearch = async (query) => {
    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 500));

      // Simulate search results
      const mockResults = [
        `${query} - Result 1`,
        `${query} - Result 2`,
        `${query} - Result 3`,
        `${query} - Documentation`,
        `${query} - Tutorial`
      ];

      setResults(mockResults);
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setLoading(false);
    }
  };

  // Handle input change
  const handleInputChange = (e) => {
    setSearchQuery(e.target.value);
    // Note: API call happens in useEffect, not here!
  };

  return (
    <div style={{ maxWidth: '600px', margin: '50px auto', padding: '20px' }}>
      <h1>Debounced Search</h1>

      {/* Search Input */}
      <input
        type="text"
        value={searchQuery}
        onChange={handleInputChange}
        placeholder="Search... (waits 500ms after you stop typing)"
        style={{
          width: '100%',
          padding: '12px',
          fontSize: '16px',
          border: '2px solid #ddd',
          borderRadius: '5px'
        }}
      />

      {/* Loading Indicator */}
      {loading && (
        <div style={{ marginTop: '20px', color: '#666' }}>
          Searching...
        </div>
      )}

      {/* Results */}
      {!loading && results.length > 0 && (
        <ul style={{ marginTop: '20px', listStyle: 'none', padding: 0 }}>
          {results.map((result, index) => (
            <li
              key={index}
              style={{
                padding: '12px',
                margin: '8px 0',
                background: '#f9f9f9',
                borderRadius: '5px'
              }}
            >
              {result}
            </li>
          ))}
        </ul>
      )}

      {/* No Results */}
      {!loading && searchQuery && results.length === 0 && (
        <div style={{ marginTop: '20px', color: '#999' }}>
          No results found
        </div>
      )}
    </div>
  );
}

export default DebounceSearch;
```

---

## >à How Debouncing Works

### Timeline Diagram:

```
Time: 0ms   100ms  200ms  300ms  400ms  500ms  600ms  700ms  800ms
Input: "r"   "e"    "a"    "c"    "t"    (pause)
Timer:  [----cancel----]
         [----cancel----]
          [----cancel----]
           [----cancel----]
            [--------------- API CALL]

Only the LAST timer completes ’ API called once with "react"
```

### Code Flow:

```javascript
// User types "r"
setSearchQuery("r")
’ useEffect runs
’ setTimeout(() => search("r"), 500) // Timer A

// User types "e" (100ms later)
setSearchQuery("re")
’ Cleanup runs: clearTimeout(Timer A) // Cancel Timer A
’ useEffect runs again
’ setTimeout(() => search("re"), 500) // Timer B

// ... continues for "a", "c", "t"

// User stops typing (500ms pass)
’ Final timer completes
’ search("react") is called
```

---

## =€ Custom Debounce Hook

```jsx
// Custom hook for reusable debouncing
function useDebounce(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    // Set up timer
    const timerId = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    // Cleanup: cancel timer on value change
    return () => {
      clearTimeout(timerId);
    };
  }, [value, delay]);

  return debouncedValue;
}

// Usage
function SearchComponent() {
  const [searchQuery, setSearchQuery] = useState('');
  const debouncedQuery = useDebounce(searchQuery, 500);
  // debouncedQuery only updates 500ms after user stops typing

  useEffect(() => {
    if (debouncedQuery) {
      // Perform search with debounced value
      performSearch(debouncedQuery);
    }
  }, [debouncedQuery]);

  return (
    <input
      value={searchQuery}
      onChange={(e) => setSearchQuery(e.target.value)}
    />
  );
}
```

---

## = Common Mistakes

### Mistake 1: Not Cleaning Up Timer

```jsx
// L WRONG: Timer not cancelled (multiple API calls!)
useEffect(() => {
  setTimeout(() => {
    search(query);
  }, 500);
  // Missing: return () => clearTimeout(timerId)
}, [query]);

//  CORRECT: Clean up timer
useEffect(() => {
  const timerId = setTimeout(() => search(query), 500);
  return () => clearTimeout(timerId);
}, [query]);
```

### Mistake 2: Calling API in onChange

```jsx
// L WRONG: API called on every keystroke
const handleChange = (e) => {
  setQuery(e.target.value);
  performSearch(e.target.value); // Called immediately!
};

//  CORRECT: Let useEffect handle debounced search
const handleChange = (e) => {
  setQuery(e.target.value);
  // Search happens in useEffect after delay
};
```

---

# 5. Modal / Dialog

## =Ë Problem Statement

Build a modal (popup) component that overlays the main content. Used for confirmations, forms, images, etc.

### Example:
```
[Open Modal Button]

(Click button)

                            
  Modal Title          [X]  
                            $
  Modal content here        
                            
  [Cancel] [Confirm]        
                            
```

---

## <¯ Requirements

**Must Have:**
-  Show/hide modal
-  Overlay background
-  Close on X button
-  Close on overlay click
-  Close on Escape key

**Nice to Have:**
- P Focus trap (keyboard navigation stays in modal)
- P Prevent body scroll when open
- P Animations

---

## =¡ Complete Solution

```jsx
import { useState, useEffect } from 'react';

function ModalDemo() {
  // State: Is modal open?
  const [isOpen, setIsOpen] = useState(false);

  const openModal = () => setIsOpen(true);
  const closeModal = () => setIsOpen(false);

  return (
    <div style={{ padding: '50px' }}>
      <h1>Modal Demo</h1>

      <button
        onClick={openModal}
        style={{
          padding: '12px 24px',
          fontSize: '16px',
          cursor: 'pointer'
        }}
      >
        Open Modal
      </button>

      {isOpen && <Modal onClose={closeModal} />}
    </div>
  );
}

// Modal Component
function Modal({ onClose }) {
  // Prevent body scroll when modal is open
  // Why useEffect? Side effect on body element
  useEffect(() => {
    // Save original body overflow
    const originalOverflow = document.body.style.overflow;

    // Disable scroll
    document.body.style.overflow = 'hidden';

    // Cleanup: Restore scroll on unmount
    return () => {
      document.body.style.overflow = originalOverflow;
    };
  }, []);

  // Close on Escape key
  // Why useEffect? Set up event listener
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };

    document.addEventListener('keydown', handleEscape);

    // Cleanup
    return () => document.removeEventListener('keydown', handleEscape);
  }, [onClose]);

  // Close on overlay click (not on modal content)
  const handleOverlayClick = (e) => {
    // Only close if clicking overlay, not modal content
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    // Overlay
    <div
      onClick={handleOverlayClick}
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: 'rgba(0, 0, 0, 0.5)', // Semi-transparent black
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000 // Above everything else
      }}
    >
      {/* Modal Content */}
      <div
        onClick={(e) => e.stopPropagation()}
        // Why stopPropagation? Prevent overlay click when clicking modal
        style={{
          background: 'white',
          borderRadius: '8px',
          boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
          maxWidth: '500px',
          width: '90%',
          maxHeight: '80vh',
          overflow: 'auto'
        }}
      >
        {/* Header */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: '20px',
            borderBottom: '1px solid #eee'
          }}
        >
          <h2 style={{ margin: 0 }}>Modal Title</h2>

          {/* Close Button */}
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '24px',
              cursor: 'pointer',
              padding: '0',
              width: '30px',
              height: '30px'
            }}
          >
            ×
          </button>
        </div>

        {/* Body */}
        <div style={{ padding: '20px' }}>
          <p>This is the modal content. You can put any content here!</p>
          <p>Try:</p>
          <ul>
            <li>Click the X button to close</li>
            <li>Click outside the modal (on the dark overlay)</li>
            <li>Press the Escape key</li>
          </ul>
        </div>

        {/* Footer */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'flex-end',
            gap: '10px',
            padding: '20px',
            borderTop: '1px solid #eee'
          }}
        >
          <button
            onClick={onClose}
            style={{
              padding: '10px 20px',
              background: '#f5f5f5',
              border: '1px solid #ddd',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Cancel
          </button>
          <button
            onClick={() => {
              alert('Confirmed!');
              onClose();
            }}
            style={{
              padding: '10px 20px',
              background: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Confirm
          </button>
        </div>
      </div>
    </div>
  );
}

export default ModalDemo;
```

---

## >à Key Concepts

### Prevent Body Scroll

```jsx
useEffect(() => {
  document.body.style.overflow = 'hidden';
  return () => {
    document.body.style.overflow = 'auto';
  };
}, []);
```

**Why?** When modal is open, page behind shouldn't scroll.

### Stop Propagation

```jsx
<div onClick={handleOverlayClick}> {/* Overlay */}
  <div onClick={(e) => e.stopPropagation()}> {/* Modal */}
    {/* Clicking here doesn't trigger overlay click */}
  </div>
</div>
```

### Position Fixed

```css
position: fixed;
top: 0; left: 0; right: 0; bottom: 0;
```

**Why fixed?** Modal stays in viewport even when scrolling.

---

## =€ Advanced: Using React Portal

```jsx
import { createPortal } from 'react-dom';

function Modal({ onClose }) {
  // Render modal outside main React tree
  return createPortal(
    <div className="modal-overlay">
      <div className="modal-content">
        {/* Modal content */}
      </div>
    </div>,
    document.body // Render as direct child of body
  );
}
```

**Why Portal?**
- Avoids z-index conflicts
- Better for accessibility
- Cleaner DOM structure

---

## = Common Mistakes

### Mistake 1: Not Preventing Background Scroll

```jsx
// L WRONG: Can scroll page behind modal
function Modal() {
  return <div className="modal">...</div>;
}

//  CORRECT: Disable body scroll
useEffect(() => {
  document.body.style.overflow = 'hidden';
  return () => { document.body.style.overflow = 'auto'; };
}, []);
```

### Mistake 2: Closing on Modal Click

```jsx
// L WRONG: Clicking anywhere closes modal
<div onClick={onClose}>
  <div>Modal content</div>
</div>

//  CORRECT: Only close on overlay
<div onClick={handleOverlayClick}>
  <div onClick={(e) => e.stopPropagation()}>
    Modal content
  </div>
</div>
```

---

## >à Medium Level Summary

Congratulations! You've learned:

1. **Pagination** - Managing page state, calculating items
2. **Infinite Scroll** - Scroll detection, useRef, loading states
3. **Auto-complete** - Filtering, keyboard navigation, click outside
4. **Debounce** - Performance optimization, timer cleanup
5. **Modal** - Overlays, focus management, event handling

**Key Skills:**
- useEffect for side effects
- useRef for persistent values
- Event listeners and cleanup
- Performance optimization
- Complex state management

**Next: [Hard Level](../hard/readme.md)** for advanced patterns!
