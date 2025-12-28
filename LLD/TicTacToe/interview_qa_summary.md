# Tic-Tac-Toe LLD - Interview Q&A Quick Reference

**Purpose**: Quick reference guide for Tic-Tac-Toe LLD interview preparation.

---

## Quick Interview Checklist

### Phase 1: Requirements (2-3 minutes)
- [ ] Clarify board size (3x3 or NxN?)
- [ ] Number of players (2 human or AI?)
- [ ] Win conditions (standard 3-in-row?)
- [ ] Persistence needed?
- [ ] Multiplayer (local or network)?

### Phase 2: Core Classes (3-4 minutes)
Draw these first:
- [ ] `Board` - The grid
- [ ] `Cell` - Individual positions
- [ ] `Player` - Participants
- [ ] `Game` - Coordinator

### Phase 3: Enums (1-2 minutes)
- [ ] `CellState`: EMPTY, X, O
- [ ] `Symbol`: X, O
- [ ] `GameStatus`: NOT_STARTED, IN_PROGRESS, PLAYER_X_WON, PLAYER_O_WON, DRAW

### Phase 4: Relationships (2-3 minutes)
- [ ] `Board` *-- `Cell` (composition)
- [ ] `Game` *-- `Board` (composition)
- [ ] `Game` o-- `Player` (aggregation)

### Phase 5: Validation (2-3 minutes)
- [ ] `MoveValidator` - Validates moves
- [ ] `WinChecker` - Checks wins
- [ ] Explain SRP

### Phase 6: Multi-Game (if asked) (2-3 minutes)
- [ ] `GameSession` - Manages games
- [ ] `ScoreBoard` - Tracks stats
- [ ] `GameResult` - Immutable outcome

### Phase 7: Service & Repository (if asked) (3-4 minutes)
- [ ] `GameService` interface
- [ ] `ScoreBoardRepository` interface
- [ ] Explain Dependency Inversion

---

## Top 10 Interview Questions

### Q1: How did you ensure Single Responsibility Principle?
**Answer**: Each class has one reason to change:
- `MoveValidator` only validates moves
- `WinChecker` only checks win conditions
- `ScoreBoard` only tracks scores
- `Game` only coordinates game flow

---

### Q2: Why use Repository pattern?
**Answer**:
- **Abstraction**: Service doesn't know storage details
- **Flexibility**: Easy to swap storage (memory → file → DB)
- **Testability**: Can inject mock repositories
- **Centralization**: All data access in one place
- **Dependency Inversion**: Depend on interface, not concrete storage

---

### Q3: How would you add AI opponent?
**Answer**: Use Strategy Pattern:
```java
public interface MoveStrategy {
    Move chooseMove(Board board);
}

public class HumanMoveStrategy implements MoveStrategy { ... }
public class RandomAIMoveStrategy implements MoveStrategy { ... }
public class SmartAIMoveStrategy implements MoveStrategy { ... }

public class Player {
    private MoveStrategy strategy;
    public Move getMove(Board board) {
        return strategy.chooseMove(board);
    }
}
```

---

### Q4: How do you handle invalid moves?
**Answer**: Using `MoveValidator`:
```java
public class MoveValidator {
    public boolean isValidMove(int row, int col) {
        return isInBounds(row, col) && isCellEmpty(row, col);
    }

    public String getValidationError(int row, int col) {
        if (!isInBounds(row, col)) return "Out of bounds!";
        if (!isCellEmpty(row, col)) return "Cell occupied!";
        return "";
    }
}
```

---

### Q5: How would you extend to larger boards (NxN)?
**Answer**: Design already supports it!
```java
public class Board {
    public Board(int size) {  // Not hardcoded!
        this.size = size;
        this.cells = new Cell[size][size];
    }
}

// Usage
Board board4x4 = new Board(4);
Board board5x5 = new Board(5);
```

---

### Q6: What design patterns did you use?
**Answer**:
| Pattern | Where | Why |
|---------|-------|-----|
| Strategy | MoveStrategy | Interchangeable AI |
| Repository | ScoreBoardRepository | Abstract persistence |
| Facade | GameSession | Simplify subsystem |
| Value Object | GameResult | Immutable domain values |
| Factory | GameResult creation | Encapsulate creation |
| Dependency Injection | Service layer | Loose coupling |

---

### Q7: How do you test this design?
**Answer**: Each layer tests independently:
```java
// Unit test
@Test
public void testInvalidMove_OutOfBounds() {
    Board board = new Board(3);
    MoveValidator validator = new MoveValidator(board);
    assertFalse(validator.isValidMove(-1, 0));
}

// Integration test with mock
@Test
public void testGameService_SaveScoreBoard() {
    ScoreBoardRepository mockRepo = mock(ScoreBoardRepository.class);
    GameService service = new GameServiceImpl(mockRepo);
    service.saveScoreBoard(session);
    verify(mockRepo).save(any(), anyString());
}
```

---

### Q8: What are the trade-offs?
**Answer**:
| Decision | Pros | Cons |
|----------|------|------|
| Separate Validator & Checker | SRP, testable | More classes |
| Repository Pattern | Flexible storage | Extra layer |
| Immutable GameResult | Thread-safe | Can't modify |
| Enums vs Strings | Type-safe | Less flexible |

**Justification**: Prioritized maintainability and extensibility over simplicity.

---

### Q9: How do you handle concurrency?
**Answer**: Current design is single-threaded, but extensible:
```java
public class ScoreBoard {
    private final Map<Player, AtomicInteger> wins;
    private final AtomicInteger draws;

    public synchronized void recordResult(GameResult result) {
        totalGames.incrementAndGet();
        // ...
    }
}
```

---

### Q10: What SOLID principles does your design violate?
**Answer**: Honest assessment:
1. **ISP**: `GameService` could split into `GameManagementService` and `PersistenceService`
2. **SRP**: `Game` does coordination AND state management
3. **OCP**: Adding new game types might require modifying `WinChecker`

**Interview Tip**: Acknowledging imperfections shows critical thinking!

---

## UML Relationships Quick Reference

| Relationship | Symbol | Meaning | Example |
|--------------|--------|---------|---------|
| Composition | `*--` | Owns, strong lifecycle | Board owns Cells |
| Aggregation | `o--` | Has, weak lifecycle | Game has Players |
| Dependency | `-->` | Uses temporarily | Game uses Validator |
| Realization | `<\|..` | Implements interface | ServiceImpl implements Service |

---

## SOLID Principles Applied

### S - Single Responsibility
- `MoveValidator` only validates
- `WinChecker` only checks wins
- `ScoreBoard` only tracks scores

### O - Open/Closed
- Open for extension: Add new repository implementations
- Closed for modification: Interface doesn't change

### L - Liskov Substitution
- Any `ScoreBoardRepository` implementation works
- `InMemoryRepository` and `FileRepository` are interchangeable

### I - Interface Segregation
- Small, focused interfaces
- `ScoreBoardRepository` only has 4 methods
- No fat interfaces

### D - Dependency Inversion
- High-level `GameService` depends on `ScoreBoardRepository` interface
- Not on concrete `InMemoryRepository` or `FileRepository`

---

## Extension Points

### Easy Extensions
1. Undo/Redo (Stack of moves)
2. Move history (List with timestamps)
3. Larger boards (Already parameterized)
4. Timed games (Add timer)
5. N-in-a-row (Configurable win length)

### Medium Extensions
1. AI opponent (Strategy pattern)
2. GUI (Swing/JavaFX)
3. Network play (Sockets)
4. Replay system (Save/load)
5. Tournament mode (Best-of-N)

### Advanced Extensions
1. Minimax AI (Optimal play)
2. Database persistence (PostgreSQL/MongoDB)
3. REST API (Spring Boot)
4. Real-time updates (WebSocket)
5. ELO rating system (Player rankings)

---

## Whiteboard Time Allocation (45-min interview)

| Phase | Time | What to Draw |
|-------|------|-------------|
| Requirements | 2-3 min | Clarify scope |
| Core Entities | 3-4 min | Board, Cell, Player, Game |
| Enums | 1-2 min | CellState, Symbol, GameStatus |
| Relationships | 2-3 min | Composition, Aggregation |
| Validation | 2-3 min | MoveValidator, WinChecker |
| Session/Score | 2-3 min | If multi-game asked |
| Service/Repo | 3-4 min | If persistence asked |
| Methods | 2-3 min | Key methods only |
| Discussion | 20-25 min | Explain decisions, trade-offs |

---

## Key Talking Points

1. **Start Simple**: "I'll begin with core entities: Board, Cell, Player, Game"
2. **Show Thinking**: "I'm separating validation for SRP"
3. **Discuss Trade-offs**: "Composition for Board-Cell because cells can't exist independently"
4. **Mention Patterns**: "Repository pattern abstracts storage"
5. **Plan Extensions**: "Design supports AI through Strategy pattern"
6. **Be Honest**: "Keeping it 3x3 for now, but easy to extend to NxN"
7. **Show Expertise**: "Applying Dependency Inversion - service depends on interface"

---

## Common Mistakes to Avoid

### ❌ Don't Do This
1. **God Class**: Putting everything in one class
2. **Public Fields**: Violates encapsulation
3. **String for State**: No type safety
4. **Tight Coupling**: Creating dependencies inside
5. **Skipping Validation**: Not checking invalid moves
6. **Forgetting to Switch**: Player doesn't change
7. **Check Before Place**: Checking win before placing symbol

### ✅ Do This Instead
1. **Separate Concerns**: Each class has one job
2. **Private Fields**: Encapsulation with getters/setters
3. **Enums for State**: Type-safe CellState, Symbol
4. **Dependency Injection**: Pass dependencies
5. **Always Validate**: MoveValidator checks all moves
6. **Always Switch**: Change player after valid move
7. **Place Then Check**: Place symbol, then check win

---

## Code Skeleton Reference

```java
// Enum
public enum CellState { EMPTY, X, O; }

// Entity
public class Cell {
    private int row, column;
    private CellState state;
    // Constructor, getters, setters
}

// Container
public class Board {
    private Cell[][] cells;
    public Board(int size) {
        cells = new Cell[size][size];
        // Initialize cells
    }
}

// Validator
public class MoveValidator {
    private Board board;
    public boolean isValidMove(int row, int col) {
        return isInBounds(row, col) && isCellEmpty(row, col);
    }
}

// Checker
public class WinChecker {
    private Board board;
    public boolean checkWin(Symbol symbol) {
        return checkRows(symbol) ||
               checkColumns(symbol) ||
               checkDiagonals(symbol);
    }
}

// Coordinator
public class Game {
    private Board board;
    private Player player1, player2, currentPlayer;
    private MoveValidator validator;
    private WinChecker winChecker;

    public boolean makeMove(int row, int col) {
        if (!validator.isValidMove(row, col)) return false;
        board.placeSymbol(row, col, currentPlayer.getSymbol());
        if (winChecker.checkWin(currentPlayer.getSymbol())) {
            // Handle win
        }
        switchPlayer();
        return true;
    }
}
```

---

## Final Checklist Before Interview

- [ ] Understand all SOLID principles
- [ ] Know all design patterns used
- [ ] Can explain each class's responsibility
- [ ] Can draw class diagram in 10 minutes
- [ ] Can explain relationships (composition vs aggregation)
- [ ] Know how to extend for AI
- [ ] Know how to extend for larger boards
- [ ] Can discuss trade-offs
- [ ] Prepared for testing questions
- [ ] Ready to acknowledge design limitations

---

**Good luck with your interview!** 🚀

**Remember**:
- Clarify requirements first
- Start simple, add complexity gradually
- Explain your thought process
- Discuss trade-offs
- Be honest about limitations
