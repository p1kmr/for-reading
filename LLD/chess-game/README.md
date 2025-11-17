# ♟️ Chess Game - Low Level Design (LLD)

A comprehensive, beginner-friendly Low-Level Design (LLD) of a Chess Game system with detailed documentation, diagrams, and complete Java implementation.

---

## 📚 Table of Contents

1. [Requirements & Use Cases](#requirements)
2. [Incremental Class Diagrams](#class-diagrams)
3. [Sequence Diagrams](#sequence-diagrams)
4. [Design Patterns & SOLID Principles](#design-patterns)
5. [Interview Q&A](#interview-qa)
6. [Complete Implementation](#implementation)
7. [Learning Path](#learning-path)

---

## 📋 Requirements

- **Functional Requirements**: Two-player chess, move validation, check/checkmate/stalemate detection, special moves
- **Non-Functional Requirements**: Performance, maintainability, extensibility, testability

**📄 Files:**
- `01-requirements.md` - Complete requirements breakdown
- `02-use-case-diagram.md` - Use case diagram with actors and flows

---

## 🎨 Class Diagrams (Incremental Evolution)

The design is presented in **6 incremental steps** showing how the design evolves:

### Step 1: Basic Entities
- `Position` class (board coordinates)
- `Board` class (8x8 grid)
- `Color` enum
- **📄 File:** `step1-basic-entities-class-diagram.md`

### Step 2: Piece Hierarchy
- Abstract `Piece` class
- Concrete pieces: King, Queen, Rook, Bishop, Knight, Pawn
- `PieceType` enum
- **📄 File:** `step2-piece-hierarchy-class-diagram.md`

### Step 3: Move Validation & Rules Engine
- `Move` class (Command Pattern)
- `MoveValidator` interface (Strategy Pattern)
- `PathValidator` for sliding pieces
- Custom exceptions
- **📄 File:** `step3-move-validation-class-diagram.md`

### Step 4: Game State Management
- `ChessGame` orchestrator (Facade Pattern)
- `GameState` enum
- `MoveHistory` class
- `Player` class
- **📄 File:** `step4-game-state-management-class-diagram.md`

### Step 5: Special Moves
- `CastlingValidator`
- `EnPassantValidator`
- `PawnPromotionHandler`
- `SpecialMoveDetector`
- **📄 File:** `step5-special-moves-class-diagram.md`

### Step 6: Check, Checkmate & Stalemate Detection
- `CheckDetector`
- `CheckmateDetector`
- `StalemateDetector`
- `GameStatusEvaluator`
- **📄 File:** `step6-final-complete-class-diagram.md`

---

## 🔄 Sequence Diagrams

Visual representation of key flows:

1. **Make a Normal Move** - Standard move execution flow
2. **Castling** - Special two-piece move
3. **Check Detection** - How system detects check
4. **Checkmate Detection** - Game-ending condition
5. **En Passant** - Special pawn capture

**📄 File:** `sequence-diagrams.md`

---

## 🏗️ Design Patterns & SOLID Principles

### Design Patterns Used:
1. **Strategy Pattern** - MoveValidator (pluggable validation strategies)
2. **Command Pattern** - Move class (encapsulates move, supports undo)
3. **Facade Pattern** - ChessGame (simplifies complex subsystem)
4. **Template Method** - Piece hierarchy (common logic in parent)
5. **Factory Pattern** - Piece creation (optional)

### SOLID Principles Applied:
1. **S**ingle Responsibility - Each class has one job
2. **O**pen/Closed - Easy to extend (add new pieces/validators)
3. **L**iskov Substitution - All pieces are interchangeable
4. **I**nterface Segregation - Focused interfaces
5. **D**ependency Inversion - Depend on abstractions

**📄 File:** `design-patterns-and-solid.md`

---

## 💼 Interview Q&A

15+ common interview questions with detailed answers:

- Core design questions (architecture, class choices)
- Implementation questions (undo/redo, concurrency, persistence)
- Design trade-off questions (array vs HashMap, enum vs constants)
- Extension questions (Chess960, AI opponent)

**📄 File:** `interview-qa.md`

---

## 💻 Complete Implementation

Full Java implementation with:
- All classes (29 classes/interfaces)
- Detailed inline comments
- Beginner-friendly explanations
- Working demo

**📄 File:** `ChessGame-Complete-Implementation.java`

**How to Run:**
```bash
javac ChessGame-Complete-Implementation.java
java ChessGameDemo
```

---

## 📊 System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    ChessGame (Facade)                    │
│                   Main Orchestrator                      │
└───────────┬─────────────────────────────────────────────┘
            │
            ├───► Board (8x8 grid, piece management)
            │
            ├───► MoveValidator (Strategy Pattern)
            │         ├─► BasicMoveValidator
            │         └─► CheckAwareMoveValidator
            │
            ├───► GameStatusEvaluator
            │         ├─► CheckDetector
            │         ├─► CheckmateDetector
            │         └─► StalemateDetector
            │
            ├───► SpecialMoveDetector
            │         ├─► CastlingValidator
            │         ├─► EnPassantValidator
            │         └─► PawnPromotionHandler
            │
            ├───► MoveHistory (tracks all moves)
            │
            └───► Players (White & Black)

            ┌────────────────────────────────┐
            │      Piece Hierarchy           │
            │  (Abstract Piece + 6 concrete) │
            │  King, Queen, Rook, Bishop,    │
            │  Knight, Pawn                  │
            └────────────────────────────────┘
```

---

## 🎓 Learning Path (For Beginners)

### Step 1: Understand the Problem
1. Read `01-requirements.md`
2. Study `02-use-case-diagram.md`
3. Understand what chess is and the rules

### Step 2: Follow Incremental Design
1. **Step 1**: Position & Board (foundation)
2. **Step 2**: Pieces (core entities)
3. **Step 3**: Validation (rules enforcement)
4. **Step 4**: Game flow (orchestration)
5. **Step 5**: Special moves (advanced features)
6. **Step 6**: Game-ending conditions (completion)

### Step 3: Understand Interactions
1. Study `sequence-diagrams.md`
2. See how objects collaborate
3. Understand the flow of control

### Step 4: Learn Design Principles
1. Read `design-patterns-and-solid.md`
2. Understand WHY each pattern is used
3. See how SOLID principles are applied

### Step 5: Study Implementation
1. Read `ChessGame-Complete-Implementation.java`
2. Run the demo
3. Modify and experiment

### Step 6: Prepare for Interviews
1. Study `interview-qa.md`
2. Practice explaining design decisions
3. Be ready to discuss trade-offs

---

## 🎯 Key Highlights

### For Beginners:
✅ Incremental design evolution (not overwhelming)
✅ Detailed explanations for every decision
✅ Beginner mistakes and solutions
✅ Plain language (no jargon)
✅ Multiple Mermaid diagrams
✅ Complete working code

### For Interviews:
✅ Multiple design patterns demonstrated
✅ SOLID principles applied throughout
✅ Trade-off discussions
✅ Extension scenarios (AI, Chess960, multiplayer)
✅ 15+ Q&A with detailed answers

### For Learning LLD:
✅ Shows "what to draw first" approach
✅ Explains relationships (composition, inheritance, etc.)
✅ Documents design decisions
✅ Shows evolution of design
✅ Real-world implementation

---

## 📦 File Structure

```
chess-game/
├── README.md (this file)
├── 01-requirements.md
├── 02-use-case-diagram.md
├── step1-basic-entities-class-diagram.md
├── step2-piece-hierarchy-class-diagram.md
├── step3-move-validation-class-diagram.md
├── step4-game-state-management-class-diagram.md
├── step5-special-moves-class-diagram.md
├── step6-final-complete-class-diagram.md
├── sequence-diagrams.md
├── design-patterns-and-solid.md
├── interview-qa.md
└── ChessGame-Complete-Implementation.java
```

---

## 🚀 Next Steps

### Extend the Design:
1. **Add Undo/Redo** - Implement Command Pattern fully
2. **Add AI Opponent** - Implement Minimax algorithm
3. **Add Chess960** - Randomized starting positions
4. **Add Persistence** - Save/load games to database
5. **Add Multiplayer** - Online chess with sockets
6. **Add Time Controls** - Chess clock implementation

### Improve the Design:
1. Add more unit tests
2. Implement threefold repetition rule
3. Add 50-move rule for draw
4. Implement Zobrist hashing for fast position lookup
5. Add opening book
6. Add endgame tablebase

---

## 🏆 What You'll Learn

1. **LLD Process**: How to approach low-level design systematically
2. **Design Patterns**: When and why to use them
3. **SOLID Principles**: How to write maintainable code
4. **Class Relationships**: Composition, inheritance, aggregation
5. **Trade-offs**: Array vs HashMap, Enum vs Constants, etc.
6. **Interview Skills**: How to explain your design

---

## 🙏 Credits

Created as a comprehensive learning resource for:
- Software Engineering students
- LLD interview preparation
- Object-Oriented Design practice
- Design patterns study

---

## 📝 License

Free to use for learning purposes.

---

**Happy Learning! ♟️**
