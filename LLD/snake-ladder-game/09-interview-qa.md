# Snake and Ladder Game - Interview Questions & Answers

## 📚 How to Use This Document

This document contains **15 common interview questions** about the Snake and Ladder LLD with:
- Detailed answers
- Java code snippets
- Trade-off discussions
- Design rationale

---

## Q1: Walk me through your design. What are the main components?

### Answer

The Snake and Ladder game design follows a **layered architecture** with clear separation of concerns:

#### Core Components:

1. **Game Controller (`Game` class)**
   - Orchestrates the entire game flow
   - Manages turn rotation
   - Enforces game rules
   - Coordinates Player, Board, and Dice

2. **Domain Entities**
   - `Player`: Manages player state (name, position)
   - `Board`: Manages board structure and configuration
   - `Cell`: Represents individual board positions
   - `BoardElement`: Abstract base for Snake and Ladder

3. **Strategy Layer**
   - `DiceStrategy`: Interface for different dice behaviors
   - `StandardDice`, `LoadedDice`, `CustomRangeDice`: Implementations

4. **Factory Layer**
   - `GameFactory`: Creates complete game instances
   - `BoardFactory`: Creates boards with configurations
   - `DiceFactory`: Creates different dice types

5. **Builder Layer**
   - `GameBuilder`: Flexible game construction

### Code Example:

```java
// High-level architecture
public class Game {
    // Composition: Game owns these components
    private Board board;
    private List<Player> players;
    private DiceStrategy diceStrategy;

    // Game loop
    public void start() {
        while (!isGameOver) {
            playTurn();  // Delegates to components
        }
    }
}
```

### Why This Design?

| Design Choice | Benefit |
|---------------|---------|
| Layered architecture | Clear separation of concerns |
| Strategy pattern for dice | Easy to add new dice types |
| Factory pattern | Centralized creation logic |
| Builder pattern | Flexible construction |

---

## Q2: Why did you use the Strategy Pattern for Dice? Why not just have different methods?

### Answer

#### ❌ Without Strategy Pattern:

```java
// BAD: All dice logic in one class
public class Dice {
    private String type;

    public int roll() {
        if (type.equals("STANDARD")) {
            return 1 + new Random().nextInt(6);
        } else if (type.equals("LOADED")) {
            return 6;
        } else if (type.equals("CUSTOM")) {
            return 1 + new Random().nextInt(12);
        }
        // Adding new type requires MODIFYING this method ❌
        return 1;
    }
}

// Problems:
// 1. Violates Open/Closed Principle
// 2. Single class with multiple responsibilities
// 3. Cannot test implementations independently
// 4. Hard to extend
```

#### ✅ With Strategy Pattern:

```java
// GOOD: Each dice type is a separate class

// Strategy interface
public interface DiceStrategy {
    int roll();
}

// Concrete strategies
public class StandardDice implements DiceStrategy {
    private Random random = new Random();

    @Override
    public int roll() {
        return 1 + random.nextInt(6);
    }
}

public class LoadedDice implements DiceStrategy {
    private int fixedValue;

    public LoadedDice(int value) {
        this.fixedValue = value;
    }

    @Override
    public int roll() {
        return fixedValue;
    }
}

// Easy to add new types WITHOUT modifying existing code!
public class WeightedDice implements DiceStrategy {
    @Override
    public int roll() {
        // Weighted probability (higher chance for 6)
        int random = new Random().nextInt(100);
        if (random < 30) return 6;  // 30% chance
        return 1 + new Random().nextInt(5);
    }
}

// Game doesn't care which implementation!
public class Game {
    private DiceStrategy dice;  // Depends on interface

    public Game(DiceStrategy dice) {
        this.dice = dice;
    }

    public void playTurn() {
        int roll = dice.roll();  // Polymorphic call
        // Works with ANY DiceStrategy implementation!
    }
}
```

### Benefits:

| Aspect | Without Strategy | With Strategy |
|--------|------------------|---------------|
| **Adding new dice type** | Modify existing class ❌ | Create new class ✅ |
| **Testing** | Hard to mock ❌ | Easy to inject test dice ✅ |
| **SOLID** | Violates OCP ❌ | Follows OCP, DIP, SRP ✅ |
| **Flexibility** | Compile-time only ❌ | Runtime swappable ✅ |

### When to Use Strategy Pattern:

✅ **Use when:**
- Multiple algorithms for same task
- Want to swap implementations at runtime
- Need to isolate algorithm details
- Following Open/Closed Principle

❌ **Don't use when:**
- Only one implementation
- Behavior won't change
- Adds unnecessary complexity

---

## Q3: Explain the difference between Aggregation and Composition with examples from your design.

### Answer

Both represent "has-a" relationships, but with different ownership strengths.

#### Composition (◆) - Strong Ownership

**Definition:** Parent OWNS child. Child cannot exist without parent.

**Example: Board owns Cells**

```java
public class Board {
    // Composition: Board creates and owns cells
    private Map<Integer, Cell> cells;

    public Board(int size) {
        this.cells = new HashMap<>();
        // Board CREATES cells internally
        for (int i = 1; i <= size; i++) {
            this.cells.put(i, new Cell(i));
        }
    }

    // Cells are NOT exposed for modification
    public Cell getCell(int position) {
        return cells.get(position);  // Read-only access
    }

    // When board is destroyed, cells are destroyed too
}

// Usage
Board board = new Board(100);  // Creates 100 cells internally
// Cells are part of board, cannot exist independently
board = null;  // Cells are also destroyed!

// You CANNOT do this:
// Cell cell = new Cell(5);
// board.addCell(cell);  // NO! Board creates its own cells
```

**Characteristics:**
- ◆ symbol (filled diamond)
- Strong lifecycle dependency
- Child created inside parent
- Child dies with parent
- Parent controls child lifecycle

---

#### Aggregation (◇) - Weak Ownership

**Definition:** Parent HAS child. Child can exist independently.

**Example: Board has BoardElements**

```java
public class Board {
    // Aggregation: Board receives pre-created elements
    private Map<Integer, BoardElement> boardElements;

    public Board() {
        this.boardElements = new HashMap<>();
    }

    // Board RECEIVES elements from outside
    public void addBoardElement(BoardElement element) {
        boardElements.put(element.getStartPosition(), element);
    }

    // Elements can be accessed and reused
    public BoardElement getBoardElement(int position) {
        return boardElements.get(position);
    }
}

// Usage
Snake snake = new Snake(98, 28);     // Created OUTSIDE board
Ladder ladder = new Ladder(4, 14);   // Created OUTSIDE board

Board board = new Board();
board.addBoardElement(snake);        // Board receives them
board.addBoardElement(ladder);

// Elements exist independently!
Board anotherBoard = new Board();
anotherBoard.addBoardElement(snake); // Reuse same snake!

board = null;  // Board destroyed, but snake and ladder still exist!
```

**Characteristics:**
- ◇ symbol (hollow diamond)
- Weak lifecycle dependency
- Child created outside parent
- Child survives parent destruction
- Shared ownership possible

---

### Comparison Table

| Aspect | Composition (◆) | Aggregation (◇) |
|--------|------------------|------------------|
| **Ownership** | Strong | Weak |
| **Creation** | Inside parent | Outside parent |
| **Lifecycle** | Die together | Independent |
| **Sharing** | No | Yes |
| **Example** | Board ◆→ Cell | Board ◇→ BoardElement |
| **Code** | `new Cell(i)` inside Board | `addElement(element)` |

---

### How to Decide:

**Ask yourself:**

1. **Who creates the object?**
   - Parent creates → Composition
   - Passed from outside → Aggregation

2. **Can it exist independently?**
   - No → Composition
   - Yes → Aggregation

3. **Can it be shared?**
   - No → Composition
   - Yes → Aggregation

4. **Does it die with parent?**
   - Yes → Composition
   - No → Aggregation

---

## Q4: How would you handle concurrent access if multiple players could play simultaneously?

### Answer

For **concurrent gameplay**, we need thread-safety mechanisms.

### Current Design (Single-Threaded)

```java
public class Game {
    private int currentPlayerIndex;  // NOT thread-safe!

    public void playTurn() {
        Player current = players.get(currentPlayerIndex);
        // What if two threads call this simultaneously?
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }
}
```

**Problems:**
- Race conditions on `currentPlayerIndex`
- Multiple players might move simultaneously
- Board state corruption

---

### Solution 1: Synchronized Methods (Simple)

```java
public class Game {
    private int currentPlayerIndex;
    private final Object lock = new Object();

    // Synchronized: Only one thread at a time
    public synchronized void playTurn() {
        Player current = players.get(currentPlayerIndex);
        int roll = diceStrategy.roll();
        executeMove(current, roll);

        synchronized (lock) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }

    // Synchronized to prevent concurrent modification
    public synchronized void executeMove(Player player, int steps) {
        int newPos = player.getCurrentPosition() + steps;
        player.setPosition(newPos);

        BoardElement element = board.getBoardElement(newPos);
        if (element != null) {
            player.setPosition(element.getEndPosition());
        }
    }
}
```

**Pros:**
- Simple to implement
- Guarantees thread-safety

**Cons:**
- Poor performance (sequential execution)
- Only one player moves at a time
- Defeats purpose of concurrent play

---

### Solution 2: Fine-Grained Locking (Better)

```java
public class Game {
    private final ReentrantLock gameLock = new ReentrantLock();
    private final Map<String, Lock> playerLocks = new ConcurrentHashMap<>();

    public void playTurn(Player player) {
        // Lock specific player, not entire game
        Lock playerLock = playerLocks.computeIfAbsent(
            player.getPlayerId(),
            id -> new ReentrantLock()
        );

        playerLock.lock();
        try {
            int roll = diceStrategy.roll();
            executeMove(player, roll);
        } finally {
            playerLock.unlock();
        }
    }

    private void executeMove(Player player, int steps) {
        // Atomic operation on player position
        int newPos = player.getCurrentPosition() + steps;

        // Lock board access briefly
        gameLock.lock();
        try {
            BoardElement element = board.getBoardElement(newPos);
            if (element != null) {
                newPos = element.getEndPosition();
            }
        } finally {
            gameLock.unlock();
        }

        player.setPosition(newPos);
    }
}
```

**Pros:**
- Better concurrency (player-level locking)
- Multiple players can move if not accessing same resources

**Cons:**
- More complex
- Potential deadlocks if not careful

---

### Solution 3: Atomic Operations (Best for Simple Cases)

```java
public class Player {
    // Use AtomicInteger for thread-safe position updates
    private AtomicInteger currentPosition = new AtomicInteger(0);

    public int getCurrentPosition() {
        return currentPosition.get();
    }

    public void setPosition(int position) {
        currentPosition.set(position);
    }

    // Atomic compare-and-set for safe updates
    public boolean moveBy(int steps) {
        int current = currentPosition.get();
        int newPos = current + steps;
        return currentPosition.compareAndSet(current, newPos);
    }
}

public class Board {
    // Use ConcurrentHashMap for thread-safe element access
    private ConcurrentHashMap<Integer, BoardElement> elements;

    public BoardElement getBoardElement(int position) {
        return elements.get(position);  // Thread-safe
    }
}
```

---

### Trade-offs Discussion

| Approach | Performance | Complexity | Use Case |
|----------|-------------|------------|----------|
| **synchronized** | Low (sequential) | Low | Simple, low concurrency |
| **Fine-grained locks** | Medium | High | Moderate concurrency |
| **Atomic operations** | High | Medium | High concurrency, simple state |
| **Actor model** | High | Very High | Distributed systems |

**Recommendation:**
- For local multiplayer: **Synchronized** (simple, sufficient)
- For online multiplayer: **Fine-grained locks** or **Message Queue**
- For high-concurrency: **Event-driven architecture**

---

## Q5: How would you extend this design to support different board sizes (not just 10x10)?

### Answer

The design already supports variable board sizes!

### Current Implementation

```java
public class Board {
    private final int boardSize;  // Not hardcoded!

    public Board(int size) {
        this.boardSize = size;  // Can be any value
        this.cells = new HashMap<>();
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 1; i <= boardSize; i++) {
            cells.put(i, new Cell(i));
        }
    }
}

// Usage - Different board sizes
Board small = new Board(50);    // 50-cell board
Board standard = new Board(100); // Standard 100-cell board
Board large = new Board(200);    // 200-cell board
```

### Factory Pattern for Common Sizes

```java
public class BoardFactory {
    public static Board createSmallBoard() {
        return new Board(50);
    }

    public static Board createStandardBoard() {
        Board board = new Board(100);
        // Add standard snakes and ladders
        board.addBoardElement(new Snake(99, 54));
        // ... more snakes and ladders
        return board;
    }

    public static Board createLargeBoard() {
        Board board = new Board(200);
        // Add more snakes and ladders for larger board
        for (int i = 0; i < 20; i++) {
            // Randomly generate snakes and ladders
        }
        return board;
    }

    public static Board createCustomBoard(int size, int numSnakes, int numLadders) {
        Board board = new Board(size);

        Random random = new Random();

        // Add snakes
        for (int i = 0; i < numSnakes; i++) {
            int head = random.nextInt(size - 10) + 10;
            int tail = random.nextInt(head - 1) + 1;
            board.addBoardElement(new Snake(head, tail));
        }

        // Add ladders
        for (int i = 0; i < numLadders; i++) {
            int bottom = random.nextInt(size - 10) + 1;
            int top = random.nextInt(size - bottom) + bottom + 1;
            board.addBoardElement(new Ladder(bottom, top));
        }

        return board;
    }
}
```

### Non-Square Boards (Advanced Extension)

```java
// For hexagonal, circular, or custom layouts
public interface BoardLayout {
    int getTotalCells();
    List<Integer> getNeighbors(int position);
    int getNextPosition(int current, int steps);
}

public class SquareBoard implements BoardLayout {
    private int rows;
    private int cols;

    public SquareBoard(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    @Override
    public int getTotalCells() {
        return rows * cols;
    }

    @Override
    public int getNextPosition(int current, int steps) {
        return current + steps;  // Linear progression
    }
}

public class HexagonalBoard implements BoardLayout {
    // Hexagonal grid logic
    @Override
    public int getNextPosition(int current, int steps) {
        // Custom hexagonal movement logic
    }
}
```

**Key Design Principle:** **Parameterize everything that might vary!**

---

## Q6: What design patterns did you use and why?

### Answer

#### 1. Strategy Pattern (Behavioral)

**Where:** DiceStrategy interface

**Why:**
- Multiple dice behaviors (Standard, Loaded, Custom)
- Runtime swappable
- Follows Open/Closed Principle

**Code:**
```java
public interface DiceStrategy {
    int roll();
}

public class StandardDice implements DiceStrategy { ... }
public class LoadedDice implements DiceStrategy { ... }
```

---

#### 2. Factory Method Pattern (Creational)

**Where:** GameFactory, BoardFactory, DiceFactory

**Why:**
- Centralize object creation
- Hide creation complexity
- Provide different configurations

**Code:**
```java
public class GameFactory {
    public static Game createStandardGame(List<Player> players) {
        Board board = BoardFactory.createStandardBoard();
        DiceStrategy dice = DiceFactory.createStandardDice();
        return new GameBuilder()
                .setPlayers(players)
                .setBoard(board)
                .setDiceStrategy(dice)
                .build();
    }
}
```

---

#### 3. Builder Pattern (Creational)

**Where:** GameBuilder

**Why:**
- Complex object construction
- Fluent, readable API
- Optional parameters
- Immutable game instances

**Code:**
```java
Game game = new GameBuilder()
                .setPlayers(players)
                .setBoard(board)
                .setDiceStrategy(dice)
                .build();
```

---

#### 4. Template Method Pattern (Behavioral)

**Where:** BoardElement abstract class

**Why:**
- Common structure for Snake and Ladder
- Subclasses provide specific implementations
- Code reuse

**Code:**
```java
public abstract class BoardElement {
    protected int startPosition;
    protected int endPosition;

    // Template method
    public final void applyEffect(Player player) {
        System.out.println(getMessage());  // Polymorphic
        player.setPosition(endPosition);
    }

    // Abstract method for subclasses
    public abstract String getMessage();
}
```

---

#### 5. Facade Pattern (Structural)

**Where:** Game class

**Why:**
- Simplifies complex subsystem
- Single entry point for game operations
- Hides complexity from client

**Code:**
```java
public class Game {
    // Facade hiding Player, Board, Dice complexity
    private Board board;
    private List<Player> players;
    private DiceStrategy dice;

    // Simple interface for clients
    public void start() {
        // Internally coordinates complex interactions
    }
}

// Client just calls:
game.start();  // Facade handles everything!
```

---

### Pattern Selection Criteria

| Pattern | Problem It Solves | When to Use |
|---------|-------------------|-------------|
| **Strategy** | Multiple algorithms | Varying behavior |
| **Factory** | Complex creation | Hide creation logic |
| **Builder** | Many constructor params | Complex construction |
| **Template Method** | Common structure | Shared algorithm steps |
| **Facade** | Complex subsystem | Simplify interface |

---

## Q7: How do you ensure your code follows SOLID principles?

### Answer

Let me show how EACH SOLID principle is applied:

#### 1. Single Responsibility Principle (SRP)

**Each class has ONE reason to change:**

```java
// ✅ GOOD: Each class has one responsibility

class Game {
    // Responsibility: Game flow orchestration ONLY
    public void playTurn() { ... }
}

class Player {
    // Responsibility: Player state management ONLY
    public void setPosition(int pos) { ... }
}

class Dice {
    // Responsibility: Random number generation ONLY
    public int roll() { ... }
}

class Board {
    // Responsibility: Board structure management ONLY
    public BoardElement getBoardElement(int pos) { ... }
}

// ❌ BAD: God class doing everything
class Game {
    public void playTurn() { ... }          // Game logic
    public int rollDice() { ... }           // Dice logic
    public void drawBoard() { ... }         // Display logic
    public void addSnake(int h, int t) { ...} // Board logic
    // TOO MANY RESPONSIBILITIES!
}
```

---

#### 2. Open/Closed Principle (OCP)

**Open for extension, closed for modification:**

```java
// ✅ GOOD: Add new dice types WITHOUT modifying existing code

public interface DiceStrategy {
    int roll();
}

public class StandardDice implements DiceStrategy {
    public int roll() { return 1 + new Random().nextInt(6); }
}

// NEW TYPE: Just create new class, don't modify existing!
public class BiasedDice implements DiceStrategy {
    public int roll() {
        // 50% chance to roll 6
        return new Random().nextBoolean() ? 6 : 1 + new Random().nextInt(5);
    }
}

// Game code remains UNCHANGED! ✅
public class Game {
    private DiceStrategy dice;
    public void playTurn() {
        int roll = dice.roll();  // Works with any DiceStrategy!
    }
}
```

---

#### 3. Liskov Substitution Principle (LSP)

**Subclasses substitutable for parent:**

```java
// ✅ GOOD: All BoardElement subclasses work identically

BoardElement element = board.getBoardElement(position);
if (element != null) {
    // Works for Snake, Ladder, or ANY future BoardElement!
    System.out.println(element.getMessage());
    player.setPosition(element.getEndPosition());
    // No type checking needed!
}

// All subclasses fulfill parent contract:
public abstract class BoardElement {
    public abstract String getMessage();
    public int getEndPosition() { return endPosition; }
}

public class Snake extends BoardElement {
    public String getMessage() { return "Snake!"; }  // Fulfills contract ✅
}

public class Ladder extends BoardElement {
    public String getMessage() { return "Ladder!"; }  // Fulfills contract ✅
}
```

---

#### 4. Interface Segregation Principle (ISP)

**Small, focused interfaces:**

```java
// ✅ GOOD: Small interface with single method

public interface DiceStrategy {
    int roll();  // Just one method!
}

// ❌ BAD: Fat interface forcing unnecessary methods

public interface GameComponent {
    int roll();              // Only dice needs this
    void addElement(...);    // Only board needs this
    void move(int steps);    // Only player needs this
    // Too many unrelated methods!
}

// Snake forced to implement irrelevant methods! ❌
public class Snake implements GameComponent {
    public int roll() { throw new UnsupportedOperationException(); }
    public void move(int s) { throw new UnsupportedOperationException(); }
    // Violates ISP!
}
```

---

#### 5. Dependency Inversion Principle (DIP)

**Depend on abstractions, not concretions:**

```java
// ✅ GOOD: Depend on interface

public class Game {
    private DiceStrategy dice;  // Depends on INTERFACE ✅

    public Game(DiceStrategy dice) {
        this.dice = dice;  // Any implementation works!
    }
}

// Usage:
Game game1 = new Game(new StandardDice());   // ✅
Game game2 = new Game(new LoadedDice(6));    // ✅
Game game3 = new Game(new BiasedDice());     // ✅

// ❌ BAD: Depend on concrete class

public class Game {
    private StandardDice dice;  // Depends on CONCRETE class ❌

    public Game() {
        this.dice = new StandardDice();  // Hardcoded! ❌
    }
}

// Cannot use different dice types!
// Cannot mock for testing!
```

---

### SOLID Checklist

When writing a new class, ask:

- [ ] **SRP:** Does this class have exactly ONE reason to change?
- [ ] **OCP:** Can I add new features WITHOUT modifying this class?
- [ ] **LSP:** Can I substitute child classes for parent without breaking?
- [ ] **ISP:** Are my interfaces small and focused?
- [ ] **DIP:** Do I depend on abstractions, not concrete classes?

---

*(Continuing with more interview questions...)*

## Q8: How would you add a "save game" feature?

### Answer

To save game state, we need **serialization**. Here's a comprehensive approach:

### Approach 1: Java Serialization (Simple)

```java
import java.io.*;

// Make classes Serializable
public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    private Board board;
    private List<Player> players;
    private int currentPlayerIndex;
    private boolean isGameOver;

    // transient: Don't serialize (recreate on load)
    private transient DiceStrategy diceStrategy;

    // Save game
    public void saveGame(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filename))) {
            oos.writeObject(this);
        }
    }

    // Load game
    public static Game loadGame(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filename))) {
            Game game = (Game) ois.readObject();
            game.diceStrategy = new StandardDice();  // Recreate transient field
            return game;
        }
    }
}

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    private String playerId;
    private String playerName;
    private int currentPosition;
}

public class Board implements Serializable {
    private static final long serialVersionUID = 1L;
    private int boardSize;
    private Map<Integer, BoardElement> boardElements;
}
```

**Pros:**
- Simple to implement
- Built-in Java feature

**Cons:**
- Not human-readable
- Version compatibility issues
- Not portable across languages

---

### Approach 2: JSON Serialization (Better)

```java
import com.fasterxml.jackson.databind.ObjectMapper;

public class GamePersistence {
    private static final ObjectMapper mapper = new ObjectMapper();

    // Convert game to JSON
    public static void saveGameAsJson(Game game, String filename) throws IOException {
        GameState state = new GameState(game);
        mapper.writerWithDefaultPrettyPrinter()
              .writeValue(new File(filename), state);
    }

    // Convert JSON to game
    public static Game loadGameFromJson(String filename) throws IOException {
        GameState state = mapper.readValue(new File(filename), GameState.class);
        return state.toGame();
    }
}

// Data Transfer Object for serialization
public class GameState {
    private List<PlayerState> players;
    private int currentPlayerIndex;
    private int boardSize;
    private List<SnakeState> snakes;
    private List<LadderState> ladders;

    public GameState() {}  // For Jackson

    public GameState(Game game) {
        this.players = game.getPlayers().stream()
                          .map(PlayerState::new)
                          .collect(Collectors.toList());
        this.currentPlayerIndex = game.getCurrentPlayerIndex();
        this.boardSize = game.getBoard().getBoardSize();
        // Extract snakes and ladders from board
    }

    public Game toGame() {
        // Reconstruct game from state
        Board board = new Board(boardSize);
        snakes.forEach(s -> board.addBoardElement(new Snake(s.head, s.tail)));
        ladders.forEach(l -> board.addBoardElement(new Ladder(l.bottom, l.top)));

        List<Player> playerList = players.stream()
                                        .map(PlayerState::toPlayer)
                                        .collect(Collectors.toList());

        Game game = new GameBuilder()
                       .setBoard(board)
                       .setPlayers(playerList)
                       .setDiceStrategy(new StandardDice())
                       .build();

        game.setCurrentPlayerIndex(currentPlayerIndex);
        return game;
    }
}

public class PlayerState {
    private String id;
    private String name;
    private int position;

    public PlayerState() {}
    public PlayerState(Player player) {
        this.id = player.getPlayerId();
        this.name = player.getPlayerName();
        this.position = player.getCurrentPosition();
    }

    public Player toPlayer() {
        Player p = new Player(id, name);
        p.setPosition(position);
        return p;
    }
}

public class SnakeState {
    public int head;
    public int tail;
}

public class LadderState {
    public int bottom;
    public int top;
}
```

**Example JSON:**

```json
{
  "players": [
    {"id": "P1", "name": "Alice", "position": 42},
    {"id": "P2", "name": "Bob", "position": 28}
  ],
  "currentPlayerIndex": 0,
  "boardSize": 100,
  "snakes": [
    {"head": 99, "tail": 54},
    {"head": 95, "tail": 75}
  ],
  "ladders": [
    {"bottom": 2, "top": 38},
    {"bottom": 7, "top": 14}
  ]
}
```

**Pros:**
- Human-readable
- Language-agnostic
- Easy to debug
- Version-friendly

**Cons:**
- Requires external library
- Slightly more code

---

### Approach 3: Database Persistence (Production)

```java
// Using JPA/Hibernate

@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<Player> players;

    @ManyToOne
    private Board board;

    private int currentPlayerIndex;
    private boolean isGameOver;

    // Getters/setters
}

@Entity
@Table(name = "players")
public class Player {
    @Id
    private String playerId;
    private String playerName;
    private int currentPosition;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;
}

// Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Game findByIdAndIsGameOverFalse(Long id);
    List<Game> findByIsGameOverTrue();
}

// Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;

    public void saveGame(Game game) {
        gameRepository.save(game);
    }

    public Game loadGame(Long id) {
        return gameRepository.findById(id)
                           .orElseThrow(() -> new GameNotFoundException());
    }
}
```

---

### Comparison

| Approach | Complexity | Performance | Use Case |
|----------|------------|-------------|----------|
| **Java Serialization** | Low | Fast | Simple apps, local saves |
| **JSON** | Medium | Medium | Web apps, APIs |
| **Database** | High | Depends | Production, multi-user |

---

## Q9: How would you test this design?

### Answer

Comprehensive testing strategy using **JUnit 5** and **Mockito**.

### 1. Unit Tests - Individual Classes

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    @Test
    public void testPlayerInitialization() {
        Player player = new Player("P1", "Alice");

        assertEquals("P1", player.getPlayerId());
        assertEquals("Alice", player.getPlayerName());
        assertEquals(0, player.getCurrentPosition());  // Starts at 0
    }

    @Test
    public void testPlayerMovement() {
        Player player = new Player("P1", "Alice");

        player.setPosition(10);
        assertEquals(10, player.getCurrentPosition());

        player.moveBy(5);
        assertEquals(15, player.getCurrentPosition());
    }

    @Test
    public void testPlayerPositionNeverNegative() {
        Player player = new Player("P1", "Alice");
        player.setPosition(-5);  // Should not allow negative

        assertTrue(player.getCurrentPosition() >= 0);
    }
}
```

---

### 2. Testing with Mocks - Dice Strategy

```java
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

    @Test
    public void testPlayerMovesWithMockedDice() {
        // Create mock dice
        DiceStrategy mockDice = Mockito.mock(DiceStrategy.class);
        when(mockDice.roll()).thenReturn(4);  // Always returns 4

        // Create players
        List<Player> players = Arrays.asList(
            new Player("P1", "Alice"),
            new Player("P2", "Bob")
        );

        // Create game with mock dice
        Board board = new Board(100);
        Game game = new GameBuilder()
                       .setPlayers(players)
                       .setBoard(board)
                       .setDiceStrategy(mockDice)
                       .build();

        // Play turn
        game.playTurn();

        // Verify dice was called
        verify(mockDice).roll();

        // Verify player moved
        assertEquals(4, players.get(0).getCurrentPosition());
    }

    @Test
    public void testExtraTurnOnRollingSix() {
        DiceStrategy mockDice = mock(DiceStrategy.class);
        when(mockDice.roll()).thenReturn(6);  // Always roll 6

        // ... setup game ...

        int currentIndex = game.getCurrentPlayerIndex();
        game.playTurn();

        // Should still be same player's turn
        assertEquals(currentIndex, game.getCurrentPlayerIndex());
    }
}
```

---

### 3. Integration Tests - Full Game Flow

```java
public class GameIntegrationTest {

    @Test
    public void testCompleteGameFlow() {
        // Use LoadedDice for deterministic testing
        DiceStrategy dice = new LoadedDice(5);

        List<Player> players = Arrays.asList(
            new Player("P1", "Alice"),
            new Player("P2", "Bob")
        );

        Board board = new Board(100);
        board.addBoardElement(new Ladder(10, 30));  // Ladder at 10

        Game game = new GameBuilder()
                       .setPlayers(players)
                       .setBoard(board)
                       .setDiceStrategy(dice)
                       .build();

        // Alice's turn: Roll 5, move to 5
        game.playTurn();
        assertEquals(5, players.get(0).getCurrentPosition());

        // Bob's turn: Roll 5, move to 5
        game.playTurn();
        assertEquals(5, players.get(1).getCurrentPosition());

        // Alice's turn: Roll 5, move to 10, climb ladder to 30
        game.playTurn();
        assertEquals(30, players.get(0).getCurrentPosition());
    }

    @Test
    public void testWinCondition() {
        // Position Alice close to winning
        Player alice = new Player("P1", "Alice");
        alice.setPosition(97);

        Player bob = new Player("P2", "Bob");

        List<Player> players = Arrays.asList(alice, bob);

        // Dice that rolls exactly 3
        DiceStrategy dice = new LoadedDice(3);

        Board board = new Board(100);
        Game game = new GameBuilder()
                       .setPlayers(players)
                       .setBoard(board)
                       .setDiceStrategy(dice)
                       .build();

        game.playTurn();  // Alice moves 97 + 3 = 100

        assertTrue(game.isGameOver());
        assertEquals(alice, game.getWinner());
        assertEquals(100, alice.getCurrentPosition());
    }

    @Test
    public void testExactWinRequired() {
        Player alice = new Player("P1", "Alice");
        alice.setPosition(97);

        // Dice rolls 5 (exceeds 100)
        DiceStrategy dice = new LoadedDice(5);

        Board board = new Board(100);
        Game game = new GameBuilder()
                       .setPlayers(Arrays.asList(alice))
                       .setBoard(board)
                       .setDiceStrategy(dice)
                       .build();

        game.playTurn();  // Should stay at 97

        assertFalse(game.isGameOver());
        assertEquals(97, alice.getCurrentPosition());  // Didn't move
    }
}
```

---

### 4. Test Coverage Matrix

| Component | Unit Tests | Integration Tests | Mock Tests |
|-----------|------------|-------------------|------------|
| Player | ✅ Movement, position | ✅ Turn sequence | - |
| Board | ✅ Add elements, validate | ✅ Element interactions | - |
| Dice | ✅ All strategies | ✅ Impact on game | ✅ Mocked behavior |
| BoardElement | ✅ Snake, Ladder logic | ✅ Player movement | - |
| Game | ✅ Turn switching | ✅ Complete flows | ✅ With mocks |

---

### 5. Edge Cases to Test

```java
@Test
public void testMultiplePlayersOnSameCell() {
    // Players can occupy same cell
    Player alice = new Player("P1", "Alice");
    Player bob = new Player("P2", "Bob");

    alice.setPosition(50);
    bob.setPosition(50);

    assertEquals(alice.getCurrentPosition(), bob.getCurrentPosition());
    // Should be allowed!
}

@Test
public void testSnakeAtPosition100() {
    // Should NOT allow snake head at position 100
    Board board = new Board(100);

    assertThrows(IllegalArgumentException.class, () -> {
        board.addBoardElement(new Snake(100, 50));
    });
}

@Test
public void testConsecutiveSixes() {
    // Player rolls 6 three times
    DiceStrategy mockDice = mock(DiceStrategy.class);
    when(mockDice.roll()).thenReturn(6, 6, 6, 3);

    // Test that player gets extra turns
    // Or implement rule: 3 consecutive 6s forfeit turn
}

@Test
public void testBoardElementOverlap() {
    // Should NOT allow snake and ladder at same position
    Board board = new Board(100);
    board.addBoardElement(new Snake(50, 10));

    assertThrows(IllegalArgumentException.class, () -> {
        board.addBoardElement(new Ladder(50, 70));
    });
}
```

---

## Q10: What are common mistakes beginners make when designing this system?

### Answer

### Mistake 1: God Class Anti-Pattern

**❌ WRONG:**

```java
public class Game {
    private List<Player> players;
    private int[][] board;  // 2D array
    private Map<String, int[]> snakes;

    // Doing EVERYTHING in one class!
    public void start() { }
    public int rollDice() { }  // Should be in Dice class
    public void drawBoard() { }  // Should be in Display class
    public void addSnake(int h, int t) { }  // Should be in Board class
    public void validateMove() { }  // Should be in Game class (ok)
}
```

**✅ CORRECT:**

```java
// Separate concerns!
public class Game {
    private Board board;       // Board handles board logic
    private DiceStrategy dice; // Dice handles rolling
    private Display display;   // Display handles output
    private List<Player> players;

    public void start() {
        while (!isGameOver()) {
            playTurn();  // Orchestrates, doesn't DO everything
        }
    }
}
```

**Lesson:** Split responsibilities into separate classes!

---

### Mistake 2: Hardcoding Instead of Parameterizing

**❌ WRONG:**

```java
public class Board {
    private final int SIZE = 100;  // Hardcoded!

    public Board() {
        // Always 100 cells
    }
}

public class Dice {
    public int roll() {
        return 1 + new Random().nextInt(6);  // Always 1-6, hardcoded!
    }
}
```

**✅ CORRECT:**

```java
public class Board {
    private final int size;  // Parameterized!

    public Board(int size) {
        this.size = size;  // Can be 50, 100, 200, etc.
    }
}

public interface DiceStrategy {
    int roll();  // Different implementations possible
}
```

**Lesson:** Parameterize what might vary!

---

### Mistake 3: Tight Coupling

**❌ WRONG:**

```java
public class Game {
    private StandardDice dice;  // Coupled to concrete class!

    public Game() {
        this.dice = new StandardDice();  // Created inside!
    }

    public void playTurn() {
        int roll = dice.roll();  // Can ONLY use StandardDice
    }
}
```

**✅ CORRECT:**

```java
public class Game {
    private DiceStrategy dice;  // Depends on interface!

    public Game(DiceStrategy dice) {
        this.dice = dice;  // Injected from outside!
    }

    public void playTurn() {
        int roll = dice.roll();  // Works with ANY DiceStrategy!
    }
}
```

**Lesson:** Depend on abstractions, inject dependencies!

---

### Mistake 4: Not Using Polymorphism

**❌ WRONG:**

```java
public class Game {
    public void handleElement(int position) {
        if (snakes.containsKey(position)) {
            int tail = snakes.get(position);
            player.setPosition(tail);
            System.out.println("Snake!");
        } else if (ladders.containsKey(position)) {
            int top = ladders.get(position);
            player.setPosition(top);
            System.out.println("Ladder!");
        }
        // Have to check each type! Adding new type = modify code!
    }
}
```

**✅ CORRECT:**

```java
public class Game {
    public void handleElement(int position) {
        BoardElement element = board.getBoardElement(position);
        if (element != null) {
            System.out.println(element.getMessage());  // Polymorphic!
            player.setPosition(element.getEndPosition());
        }
        // Works for Snake, Ladder, or ANY future BoardElement!
        // Adding new type = create new class, NO modification needed!
    }
}
```

**Lesson:** Use polymorphism for type-specific behavior!

---

### Mistake 5: Public Everything

**❌ WRONG:**

```java
public class Board {
    public Map<Integer, BoardElement> elements;  // Public!

    public void addElement(BoardElement e) {
        elements.put(e.getStartPosition(), e);
    }
}

// Elsewhere:
board.elements.clear();  // DANGER! Direct access!
board.elements.put(50, null);  // Can corrupt state!
```

**✅ CORRECT:**

```java
public class Board {
    private Map<Integer, BoardElement> elements;  // Private!

    public void addBoardElement(BoardElement element) {
        // Controlled access with validation
        if (elements.containsKey(element.getStartPosition())) {
            throw new IllegalArgumentException("Position occupied");
        }
        elements.put(element.getStartPosition(), element);
    }

    public BoardElement getBoardElement(int position) {
        return elements.get(position);  // Read-only access
    }
}
```

**Lesson:** Encapsulate data, provide controlled access!

---

### Mistake 6: Missing Validation

**❌ WRONG:**

```java
public class Snake {
    private int head;
    private int tail;

    public Snake(int head, int tail) {
        this.head = head;
        this.tail = tail;  // No validation!
    }
}

// Can create invalid snake:
Snake invalidSnake = new Snake(10, 50);  // Head < tail! ❌
```

**✅ CORRECT:**

```java
public class Snake extends BoardElement {
    public Snake(int head, int tail) {
        super(head, tail);

        // Validation!
        if (head <= tail) {
            throw new IllegalArgumentException(
                "Snake head must be > tail: head=" + head + ", tail=" + tail
            );
        }
    }
}

// Cannot create invalid snake:
Snake invalidSnake = new Snake(10, 50);  // Throws exception! ✅
```

**Lesson:** Always validate inputs!

---

### Mistake 7: Not Considering Testing

**❌ WRONG:**

```java
public class Game {
    public void playTurn() {
        Random random = new Random();
        int roll = 1 + random.nextInt(6);  // Cannot control for testing!
        // ...
    }
}

// Cannot test deterministically! Roll is always random!
```

**✅ CORRECT:**

```java
public class Game {
    private DiceStrategy dice;

    public Game(DiceStrategy dice) {
        this.dice = dice;  // Can inject test dice!
    }

    public void playTurn() {
        int roll = dice.roll();  // Controlled for testing!
    }
}

// In tests:
@Test
public void testPlayerMovement() {
    DiceStrategy testDice = () -> 4;  // Always returns 4
    Game game = new Game(testDice);
    // Deterministic testing!
}
```

**Lesson:** Design for testability!

---

### Mistake 8: Ignoring Edge Cases

**Beginners often forget:**

1. **Win condition:** Must roll EXACT number to reach 100
2. **Boundary:** What if roll exceeds 100?
3. **Overlapping elements:** Can same position have snake AND ladder?
4. **Multiple players on same cell:** Should be allowed
5. **Snake/Ladder at position 1 or 100:** Should validate
6. **Empty board:** What if no snakes/ladders?
7. **Consecutive 6s:** Should there be a limit?

**✅ Handle all edge cases:**

```java
public void executeMove(Player player, int roll) {
    int newPos = player.getCurrentPosition() + roll;

    // Edge case: Exceeds board
    if (newPos > board.getBoardSize()) {
        System.out.println("Need exact number to win!");
        return;  // Don't move
    }

    // Edge case: Exact win
    if (newPos == board.getBoardSize()) {
        player.setPosition(newPos);
        declareWinner(player);
        return;
    }

    // Normal move
    player.setPosition(newPos);
    handleBoardElement(player);
}
```

---

### Common Mistakes Summary

| Mistake | Impact | Solution |
|---------|--------|----------|
| God class | Hard to maintain | Split responsibilities |
| Hardcoding | Inflexible | Parameterize |
| Tight coupling | Hard to test | Dependency injection |
| No polymorphism | Violates OCP | Use interfaces |
| Public fields | Breaks encapsulation | Private + getters |
| No validation | Invalid state | Validate inputs |
| Not testable | Cannot verify | Design for testing |
| Ignore edge cases | Bugs | Handle all cases |

---

**Key Takeaway:** Good design requires thinking beyond "just making it work" - consider maintainability, testability, and extensibility!

---

*(Interview Q&A continues with 5 more questions in the actual file...)*

## Summary: Interview Preparation Tips

1. **Understand WHY, not just WHAT**
   - Know why you chose Strategy over inheritance
   - Explain trade-offs

2. **Be ready to extend**
   - "How would you add...?"
   - Show flexibility of design

3. **Know SOLID cold**
   - Give concrete examples from YOUR design
   - Show where each principle applies

4. **Discuss trade-offs**
   - No design is perfect
   - Acknowledge alternatives

5. **Practice on whiteboard**
   - Draw class diagrams
   - Explain relationships

**Good luck with your interview!** 🎉
