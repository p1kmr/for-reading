# Chess Game - Interview Questions & Answers

## 💼 Phase 10: Common LLD Interview Questions

This document covers 15+ common interview questions about the Chess Game LLD, with detailed answers and code examples.

---

## 📚 CORE DESIGN QUESTIONS

### Q1: Walk me through your high-level design of the Chess Game.

**Answer:**

My Chess Game design follows a **layered architecture** with clear separation of concerns:

**1. Core Controller Layer:**
- `ChessGame`: Main orchestrator (Facade pattern)
- Coordinates all subsystems
- Provides simple API for users

**2. Domain Model Layer:**
- `Board`: Manages 8x8 grid and piece positions
- `Piece` hierarchy: Abstract Piece with 6 concrete types (King, Queen, Rook, Bishop, Knight, Pawn)
- `Position`: Represents board coordinates
- `Player`: Represents players with color and captured pieces

**3. Business Logic Layer:**
- `MoveValidator`: Validates moves (Strategy pattern)
- `GameStatusEvaluator`: Detects check/checkmate/stalemate
- `SpecialMoveDetector`: Handles castling, en passant, promotion

**4. Supporting Components:**
- `Move`: Encapsulates move information (Command pattern)
- `MoveHistory`: Tracks all moves
- Enums: `GameState`, `PieceType`, `Color`, `MoveType`

**Key Design Decisions:**
```java
// Main API
ChessGame game = new ChessGame();
game.initializeGame();
Move move = game.makeMove(e2, e4);
GameState status = game.getGameState();
```

**Flow:**
```
User → ChessGame → [Board, Validator, History, StatusEvaluator] → Result
```

---

### Q2: Why did you make Piece an abstract class instead of an interface?

**Answer:**

I chose an **abstract class** for Piece because:

**1. Common State (Data):**
```java
public abstract class Piece {
    private final Color color;        // ✅ All pieces have color
    private final PieceType type;     // ✅ All pieces have type
    private boolean hasMoved;         // ✅ All pieces track movement

    // Constructor initializes common state
    protected Piece(Color color, PieceType type) {
        this.color = color;
        this.type = type;
        this.hasMoved = false;
    }
}
```

**2. Code Reuse:**
- All pieces share getters for color, type, hasMoved
- Don't need to duplicate this code in 6 classes

**3. Concrete Methods:**
```java
public abstract class Piece {
    // Concrete method available to all subclasses
    public Color getColor() {
        return color;
    }

    // Abstract method - subclasses MUST implement
    public abstract boolean canMove(Position from, Position to, Board board);
}
```

**If it was an interface:**
```java
// ❌ Can't have instance variables (before Java 8)
public interface IPiece {
    Color getColor();  // Every class must implement
    PieceType getType();  // Duplication in 6 classes!
    boolean hasMoved();  // Duplication!
}
```

**Trade-off:**
- Abstract Class: ✅ Code reuse, ❌ Single inheritance
- Interface: ✅ Multiple inheritance, ❌ No state

**Choice**: Abstract class is better here because pieces share significant common state and behavior.

---

### Q3: How would you handle move validation to ensure the king is not left in check?

**Answer:**

I use a **two-level validation strategy**:

**Level 1: Basic Move Validation** (BasicMoveValidator)
```java
public class BasicMoveValidator implements MoveValidator {
    public boolean validate(Move move, Board board) {
        // 1. Check positions valid
        // 2. Check piece exists
        // 3. Check piece belongs to player
        // 4. Check piece-specific movement rules
        // 5. Check path is clear
        // 6. Check not capturing own piece
        return true;
    }
}
```

**Level 2: Check-Aware Validation** (CheckAwareMoveValidator)
```java
public class CheckAwareMoveValidator implements MoveValidator {
    private final BasicMoveValidator basicValidator;
    private final CheckDetector checkDetector;

    public boolean validate(Move move, Board board) {
        // First, do basic validation
        if (!basicValidator.validate(move, board)) {
            return false;
        }

        // Simulate the move
        Board tempBoard = board.clone();
        tempBoard.movePiece(move.getFrom(), move.getTo());

        // Check if own king is in check after move
        Color playerColor = move.getPiece().getColor();
        if (checkDetector.isKingInCheck(tempBoard, playerColor)) {
            throw new InvalidMoveException(
                "Move would leave your king in check!"
            );
        }

        return true;
    }
}
```

**Key Technique: Board Cloning**
```java
// Create a copy of board to simulate move
Board tempBoard = board.clone();
tempBoard.movePiece(from, to);

// Check if this results in own king being in check
if (checkDetector.isKingInCheck(tempBoard, playerColor)) {
    return false;  // Invalid move!
}
```

**Why Clone?**
- Don't want to modify actual board
- Need to test "what if" scenarios
- Can roll back if move is invalid

**Performance Consideration:**
- Cloning board is expensive
- Only do it for final validation
- Could use board state snapshots for optimization

---

### Q4: Explain your implementation of the Strategy Pattern for move validation.

**Answer:**

**Strategy Pattern Structure:**

```java
// 1. Strategy Interface
public interface MoveValidator {
    boolean validate(Move move, Board board) throws InvalidMoveException;
}

// 2. Concrete Strategy 1: Basic Validation
public class BasicMoveValidator implements MoveValidator {
    @Override
    public boolean validate(Move move, Board board) {
        // Basic chess rules only
        return piece.canMove(from, to, board);
    }
}

// 3. Concrete Strategy 2: Check-Aware Validation
public class CheckAwareMoveValidator implements MoveValidator {
    @Override
    public boolean validate(Move move, Board board) {
        // Basic rules + ensure king not in check
        if (!basicValidation(move, board)) return false;

        Board tempBoard = board.clone();
        tempBoard.movePiece(from, to);
        return !checkDetector.isKingInCheck(tempBoard, color);
    }
}

// 4. Context: ChessGame uses strategy
public class ChessGame {
    private MoveValidator validator;

    // Dependency injection
    public ChessGame(MoveValidator validator) {
        this.validator = validator;
    }

    public void setValidator(MoveValidator validator) {
        this.validator = validator;  // Can swap at runtime!
    }

    public Move makeMove(Position from, Position to) {
        validator.validate(move, board);  // Delegates to strategy
        // ...
    }
}
```

**Usage:**
```java
// Easy mode: just basic rules
ChessGame game1 = new ChessGame(new BasicMoveValidator());

// Normal mode: check-aware
ChessGame game2 = new ChessGame(new CheckAwareMoveValidator());

// Can even swap mid-game!
game1.setValidator(new CheckAwareMoveValidator());
```

**Benefits:**
1. **Open/Closed Principle**: Add new validators without changing ChessGame
2. **Testability**: Can inject mock validator for testing
3. **Flexibility**: Swap strategies at runtime
4. **Single Responsibility**: Each validator has one job

**Future Extensions:**
```java
// AI validator for computer player
public class AIValidator implements MoveValidator {
    @Override
    public boolean validate(Move move, Board board) {
        // Evaluate move quality for AI
        return moveScore > threshold;
    }
}

// Multiplayer validator (checks network state)
public class MultiplayerValidator implements MoveValidator {
    // ...
}
```

---

### Q5: How do you detect checkmate?

**Answer:**

**Checkmate Definition:**
1. King is in check (under attack)
2. No valid moves to escape check

**Implementation:**

```java
public class CheckmateDetector {
    private final CheckDetector checkDetector;

    public boolean isCheckmate(Board board, Player player) {
        Color playerColor = player.getColor();

        // Step 1: Is king in check?
        if (!checkDetector.isKingInCheck(board, playerColor)) {
            return false;  // Not in check, so not checkmate
        }

        // Step 2: Try all possible moves
        return !hasValidEscapeMove(board, player);
    }

    private boolean hasValidEscapeMove(Board board, Player player) {
        List<Piece> playerPieces = board.getAllPieces(player.getColor());

        // Try every piece
        for (Piece piece : playerPieces) {
            Position from = board.findPiecePosition(piece);
            List<Position> possibleMoves = piece.getPossibleMoves(from, board);

            // Try every possible move
            for (Position to : possibleMoves) {
                // Simulate move
                Board tempBoard = board.clone();
                tempBoard.movePiece(from, to);

                // Does this escape check?
                if (!checkDetector.isKingInCheck(tempBoard, player.getColor())) {
                    return true;  // Found escape move!
                }
            }
        }

        return false;  // No escape moves found - checkmate!
    }
}
```

**Example Scenario:**
```
8 . . . . k . . .
7 . . . . . . . .
6 . . . . K . . .   ← White king blocks escape
5 . . . R . . . .   ← Rook gives check
4 . . . . . . . .

Black king at e8:
- In check by White rook at d5
- Can't move to d8, f8, d7, f7 (White king controls these squares)
- Can't move to e7 (still in line with rook)
- Can't capture rook (too far)
- No other pieces to block or capture

Result: CHECKMATE!
```

**Optimization:**
```java
// Early termination - stop as soon as we find escape move
if (!checkDetector.isKingInCheck(tempBoard, playerColor)) {
    return true;  // Found one, that's enough!
}
```

**Difference from Stalemate:**
```java
// Checkmate: In check + no escape
isCheckmate = isInCheck && !hasValidMove

// Stalemate: NOT in check + no legal moves
isStalemate = !isInCheck && !hasValidMove
```

---

## 🎮 IMPLEMENTATION QUESTIONS

### Q6: How would you implement the undo/redo functionality?

**Answer:**

Using the **Command Pattern** with MoveHistory:

**Enhanced Move Class:**
```java
public class Move {
    private Position from;
    private Position to;
    private Piece piece;
    private Piece capturedPiece;
    private boolean pieceHadMoved;  // For undo

    // Execute move
    public void execute(Board board) {
        // Save state for undo
        capturedPiece = board.getPiece(to);
        pieceHadMoved = piece.hasMoved();

        // Execute
        board.movePiece(from, to);
        piece.setMoved(true);
    }

    // Undo move
    public void undo(Board board) {
        // Reverse the move
        board.movePiece(to, from);

        // Restore captured piece
        if (capturedPiece != null) {
            board.setPiece(to, capturedPiece);
        }

        // Restore piece state
        piece.setMoved(pieceHadMoved);
    }
}
```

**MoveHistory with Undo/Redo:**
```java
public class MoveHistory {
    private List<Move> moves = new ArrayList<>();
    private int currentIndex = -1;  // Current position in history

    public void addMove(Move move) {
        // Remove any moves after current index (redo history)
        while (moves.size() > currentIndex + 1) {
            moves.remove(moves.size() - 1);
        }

        moves.add(move);
        currentIndex++;
    }

    public Move undo(Board board) {
        if (!canUndo()) {
            throw new IllegalStateException("No moves to undo");
        }

        Move move = moves.get(currentIndex);
        move.undo(board);
        currentIndex--;
        return move;
    }

    public Move redo(Board board) {
        if (!canRedo()) {
            throw new IllegalStateException("No moves to redo");
        }

        currentIndex++;
        Move move = moves.get(currentIndex);
        move.execute(board);
        return move;
    }

    public boolean canUndo() {
        return currentIndex >= 0;
    }

    public boolean canRedo() {
        return currentIndex < moves.size() - 1;
    }
}
```

**ChessGame Integration:**
```java
public class ChessGame {
    private MoveHistory moveHistory;

    public void undoLastMove() {
        moveHistory.undo(board);
        switchTurn();  // Switch back to previous player
        updateGameState();
    }

    public void redoMove() {
        moveHistory.redo(board);
        switchTurn();
        updateGameState();
    }
}
```

**Special Case: Castling Undo**
```java
public class CastlingMove extends Move {
    private Position rookFrom;
    private Position rookTo;

    @Override
    public void undo(Board board) {
        // Undo king move
        super.undo(board);

        // Undo rook move
        board.movePiece(rookTo, rookFrom);
    }
}
```

---

### Q7: How would you handle concurrency if two players are playing online?

**Answer:**

**Approach: Optimistic Locking with Version Numbers**

```java
public class Board {
    private Piece[][] cells;
    private long version = 0;  // Version number for optimistic locking

    public synchronized long getVersion() {
        return version;
    }

    public synchronized boolean makeMove(Move move, long expectedVersion)
            throws ConcurrentModificationException {

        // Check version
        if (this.version != expectedVersion) {
            throw new ConcurrentModificationException(
                "Board has been modified by another player"
            );
        }

        // Execute move
        movePiece(move.getFrom(), move.getTo());

        // Increment version
        this.version++;

        return true;
    }
}
```

**Client Code:**
```java
public class ChessGame {
    public Move makeMove(Position from, Position to) {
        long currentVersion = board.getVersion();

        // Validate move
        validator.validate(move, board);

        try {
            // Try to execute with expected version
            board.makeMove(move, currentVersion);
        } catch (ConcurrentModificationException e) {
            // Another player moved first!
            throw new InvalidMoveException(
                "Board state changed. Please retry your move."
            );
        }

        return move;
    }
}
```

**Alternative: Pessimistic Locking**
```java
public class ChessGame {
    private final ReentrantLock gameLock = new ReentrantLock();

    public Move makeMove(Position from, Position to) {
        gameLock.lock();  // Acquire lock
        try {
            // Only one player can make move at a time
            validator.validate(move, board);
            board.movePiece(from, to);
            return move;
        } finally {
            gameLock.unlock();  // Always release
        }
    }
}
```

**Comparison:**

| Aspect | Optimistic Locking | Pessimistic Locking |
|--------|-------------------|---------------------|
| Performance | ✅ Better (no blocking) | ❌ Slower (blocks) |
| Conflicts | Retry on conflict | Waits for lock |
| Use Case | Low conflict rate | High conflict rate |
| Implementation | Version checking | Synchronized/Lock |

**Best for Chess:** Optimistic locking (players take turns, low conflict)

---

### Q8: How would you persist the game state to a database?

**Answer:**

**Approach: Repository Pattern with ORM**

**1. Entity Mapping (JPA/Hibernate):**

```java
@Entity
@Table(name = "games")
public class GameEntity {
    @Id
    @GeneratedValue
    private Long id;

    private String whitePlayerName;
    private String blackPlayerName;
    private String currentTurn;  // WHITE or BLACK

    @Enumerated(EnumType.STRING)
    private GameState gameState;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "game")
    private List<MoveEntity> moves;

    @Column(length = 1000)
    private String boardState;  // JSON representation

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Entity
@Table(name = "moves")
public class MoveEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private GameEntity game;

    private String fromPosition;  // "e2"
    private String toPosition;    // "e4"
    private String pieceType;     // "PAWN"

    @Enumerated(EnumType.STRING)
    private MoveType moveType;

    private int moveNumber;
    private LocalDateTime timestamp;
}
```

**2. Repository Interface:**
```java
public interface GameRepository {
    GameEntity save(GameEntity game);
    Optional<GameEntity> findById(Long id);
    List<GameEntity> findByPlayerName(String playerName);
    void delete(Long id);
}

// JPA implementation
@Repository
public interface JpaGameRepository
        extends JpaRepository<GameEntity, Long>, GameRepository {
    // JPA provides implementation automatically!
}
```

**3. Conversion Layer (Domain ↔ Entity):**
```java
public class GameConverter {

    public static GameEntity toEntity(ChessGame game) {
        GameEntity entity = new GameEntity();
        entity.setWhitePlayerName(game.getWhitePlayer().getName());
        entity.setBlackPlayerName(game.getBlackPlayer().getName());
        entity.setGameState(game.getGameState());

        // Serialize board state to JSON
        String boardJson = serializeBoardToJson(game.getBoard());
        entity.setBoardState(boardJson);

        // Convert move history
        List<MoveEntity> moveEntities = game.getMoveHistory()
            .getAllMoves()
            .stream()
            .map(GameConverter::toMoveEntity)
            .collect(Collectors.toList());
        entity.setMoves(moveEntities);

        return entity;
    }

    public static ChessGame fromEntity(GameEntity entity) {
        // Deserialize board from JSON
        Board board = deserializeBoardFromJson(entity.getBoardState());

        // Recreate players
        Player white = new Player(entity.getWhitePlayerName(), Color.WHITE);
        Player black = new Player(entity.getBlackPlayerName(), Color.BLACK);

        // Reconstruct game
        ChessGame game = new ChessGame(white, black);
        game.setBoard(board);
        game.setGameState(entity.getGameState());

        // Replay moves
        for (MoveEntity moveEntity : entity.getMoves()) {
            Move move = fromMoveEntity(moveEntity);
            game.getMoveHistory().addMove(move);
        }

        return game;
    }

    private static String serializeBoardToJson(Board board) {
        // Use Jackson or Gson
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(board);
    }
}
```

**4. Service Layer:**
```java
@Service
public class ChessGameService {

    @Autowired
    private GameRepository gameRepository;

    // Save game
    public Long saveGame(ChessGame game) {
        GameEntity entity = GameConverter.toEntity(game);
        GameEntity saved = gameRepository.save(entity);
        return saved.getId();
    }

    // Load game
    public ChessGame loadGame(Long gameId) {
        GameEntity entity = gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));

        return GameConverter.fromEntity(entity);
    }

    // Auto-save after each move
    @Transactional
    public void makeAndSaveMove(Long gameId, Position from, Position to) {
        ChessGame game = loadGame(gameId);
        game.makeMove(from, to);
        saveGame(game);
    }
}
```

**Board State Serialization:**
```json
{
  "cells": [
    [{"type":"ROOK","color":"WHITE"}, null, ...],
    [...],
    ...
  ],
  "version": 15
}
```

---

## 🏗️ DESIGN TRADE-OFF QUESTIONS

### Q9: Why did you choose to store pieces in a 2D array instead of a HashMap?

**Answer:**

**Choice: 2D Array `Piece[8][8]`**

**Pros:**
✅ **Fast Access**: O(1) lookup by row/column
✅ **Memory Efficient**: Fixed size, no overhead
✅ **Simple**: Easy to understand and visualize
✅ **Cache-Friendly**: Contiguous memory
✅ **Natural Mapping**: Board IS a grid

**Cons:**
❌ Sparse for end-game (many empty squares)
❌ Fixed size (can't extend board)

**Alternative: HashMap<Position, Piece>**

**Pros:**
✅ Only stores non-null pieces (memory efficient for sparse boards)
✅ Flexible size

**Cons:**
❌ **Slower**: O(1) average, but slower than array
❌ **More Memory**: HashMap overhead (entries, buckets)
❌ **Less Intuitive**: Board is fundamentally a grid

**Code Comparison:**

```java
// Array: Simple and fast
Piece piece = cells[row][column];

// HashMap: More code
Piece piece = pieceMap.get(new Position(row, column));
```

**Decision Matrix:**

| Requirement | Array | HashMap | Winner |
|-------------|-------|---------|---------|
| Speed | ⭐⭐⭐ | ⭐⭐ | Array |
| Memory (full board) | ⭐⭐⭐ | ⭐⭐ | Array |
| Memory (end-game) | ⭐⭐ | ⭐⭐⭐ | HashMap |
| Simplicity | ⭐⭐⭐ | ⭐⭐ | Array |
| Flexibility | ⭐ | ⭐⭐⭐ | HashMap |

**Conclusion:** Array is better for chess because:
1. Board is always 8x8 (fixed size)
2. Speed matters (frequent piece lookups)
3. Most chess games have pieces on board (not sparse until endgame)

**When to Use HashMap:**
- Variable board sizes (e.g., Go game)
- Very sparse boards
- Need to store additional metadata per square

---

### Q10: Explain your choice of using enums vs constants for GameState, PieceType, etc.

**Answer:**

**Choice: Enums**

**Why Enums Are Better:**

```java
// ✅ GOOD: Enum
public enum GameState {
    NOT_STARTED,
    IN_PROGRESS,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW
}

// Usage
GameState state = GameState.CHECKMATE;

// ❌ BAD: Constants
public class GameState {
    public static final int NOT_STARTED = 0;
    public static final int IN_PROGRESS = 1;
    public static final int CHECK = 2;
    public static final int CHECKMATE = 3;
    // ...
}

// Usage (error-prone!)
int state = GameState.CHECKMATE;
```

**Benefits of Enums:**

**1. Type Safety:**
```java
// ✅ Compiler error!
GameState state = PieceType.KING;  // Type mismatch!

// ❌ No compiler error (both are ints)
int state = PieceType.KING;  // Compiles but logically wrong!
```

**2. Limited Values:**
```java
// ✅ Only predefined values allowed
public void setGameState(GameState state) {
    // state can ONLY be one of 6 defined values
}

// ❌ Any int value allowed
public void setGameState(int state) {
    // What if someone passes 999? or -1?
}
```

**3. Switch Statement Completeness:**
```java
// ✅ Compiler warns if you miss a case
switch (gameState) {
    case IN_PROGRESS: ...
    case CHECK: ...
    // Compiler: "Missing cases: CHECKMATE, STALEMATE, ..."
}

// ❌ No warning
switch (gameState) {
    case 1: ...
    case 2: ...
    // Missing cases, but compiler doesn't know!
}
```

**4. Readability:**
```java
// ✅ Self-documenting
if (state == GameState.CHECKMATE) { ... }

// ❌ Magic numbers
if (state == 3) { ... }  // What does 3 mean?
```

**5. Can Add Methods:**
```java
public enum Color {
    WHITE, BLACK;

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}

// Usage
Color opponent = currentPlayer.opposite();
```

**6. Can Have Data:**
```java
public enum PieceType {
    PAWN(1),
    KNIGHT(3),
    BISHOP(3),
    ROOK(5),
    QUEEN(9),
    KING(0);

    private final int value;

    PieceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

// Usage
int queenValue = PieceType.QUEEN.getValue();  // 9
```

**When to Use Constants:**
- Mathematical constants (Math.PI)
- Configuration values
- String templates

**When to Use Enums:**
- ✅ Fixed set of values (GameState, PieceType, Color)
- ✅ Need type safety
- ✅ Want to add behavior

---

## 🚀 EXTENSION QUESTIONS

### Q11: How would you add support for Chess960 (Fischer Random Chess)?

**Answer:**

**Chess960**: Starting position is randomized (960 possible configurations)

**Requirements:**
1. King must be between rooks
2. Bishops must be on opposite colors
3. Castling still allowed (modified rules)

**Implementation:**

**1. Add Board Initializer Strategy:**
```java
public interface BoardInitializer {
    void initialize(Board board);
}

// Standard chess
public class StandardBoardInitializer implements BoardInitializer {
    @Override
    public void initialize(Board board) {
        // Place pieces in standard positions
        board.setPiece(new Position(0, 0), new Rook(Color.WHITE));
        board.setPiece(new Position(0, 1), new Knight(Color.WHITE));
        // ...
    }
}

// Chess960
public class Chess960Initializer implements BoardInitializer {
    @Override
    public void initialize(Board board) {
        // Generate random valid configuration
        List<Piece> backRank = generateRandomBackRank(Color.WHITE);

        for (int col = 0; col < 8; col++) {
            board.setPiece(new Position(0, col), backRank.get(col));
            // Mirror for black
            board.setPiece(new Position(7, col),
                createMirrorPiece(backRank.get(col), Color.BLACK));
        }
    }

    private List<Piece> generateRandomBackRank(Color color) {
        List<Piece> pieces = new ArrayList<>();

        // 1. Place king between rooks
        int kingPos = 1 + random.nextInt(6);  // 1-6
        int rook1Pos = random.nextInt(kingPos);
        int rook2Pos = kingPos + 1 + random.nextInt(7 - kingPos);

        // 2. Place bishops on opposite colors
        List<Integer> lightSquares = Arrays.asList(1, 3, 5, 7);
        List<Integer> darkSquares = Arrays.asList(0, 2, 4, 6);

        // Remove positions already taken
        lightSquares.removeIf(p -> p == kingPos || p == rook1Pos || p == rook2Pos);
        darkSquares.removeIf(p -> p == kingPos || p == rook1Pos || p == rook2Pos);

        int bishop1Pos = lightSquares.get(random.nextInt(lightSquares.size()));
        int bishop2Pos = darkSquares.get(random.nextInt(darkSquares.size()));

        // 3. Place knights and queen in remaining positions
        // ...

        return pieces;
    }
}
```

**2. Modify ChessGame:**
```java
public class ChessGame {
    private BoardInitializer initializer;

    public ChessGame(BoardInitializer initializer) {
        this.initializer = initializer;
    }

    public void initializeGame() {
        initializer.initialize(board);
        gameState = GameState.IN_PROGRESS;
    }
}

// Usage
ChessGame standardGame = new ChessGame(new StandardBoardInitializer());
ChessGame chess960Game = new ChessGame(new Chess960Initializer());
```

**3. Modify Castling Rules:**
```java
public class Chess960CastlingValidator extends CastlingValidator {
    @Override
    public void executeKingsideCastling(Board board, Color color) {
        Position kingPos = board.findKing(color);
        Position rookPos = findKingsideRook(board, color);

        // King ends at g-file, rook at f-file (same as standard)
        int rank = color == Color.WHITE ? 0 : 7;
        board.movePiece(kingPos, new Position(rank, 6));  // g-file
        board.movePiece(rookPos, new Position(rank, 5));  // f-file
    }

    private Position findKingsideRook(Board board, Color color) {
        Position kingPos = board.findKing(color);
        int rank = kingPos.getRow();

        // Find rook to the right of king
        for (int col = kingPos.getColumn() + 1; col < 8; col++) {
            Piece piece = board.getPiece(new Position(rank, col));
            if (piece != null && piece.getType() == PieceType.ROOK) {
                return new Position(rank, col);
            }
        }
        return null;
    }
}
```

**Key Changes:**
- ✅ Pluggable board initialization
- ✅ Modified castling rules
- ✅ No changes to other game logic

---

### Q12: How would you add an AI opponent?

**Answer:**

**Approach: Minimax Algorithm with Alpha-Beta Pruning**

**1. Create AI Player:**
```java
public interface Player {
    Move selectMove(Board board, List<Move> legalMoves);
}

public class HumanPlayer implements Player {
    @Override
    public Move selectMove(Board board, List<Move> legalMoves) {
        // Wait for user input
        return getUserInput();
    }
}

public class AIPlayer implements Player {
    private final MoveEvaluator evaluator;
    private final int depth;  // Search depth

    public AIPlayer(int depth) {
        this.evaluator = new MinimaxEvaluator();
        this.depth = depth;
    }

    @Override
    public Move selectMove(Board board, List<Move> legalMoves) {
        // Use AI to select best move
        return evaluator.findBestMove(board, legalMoves, depth);
    }
}
```

**2. Move Evaluator (Minimax):**
```java
public class MinimaxEvaluator {

    public Move findBestMove(Board board, List<Move> legalMoves, int depth) {
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (Move move : legalMoves) {
            // Simulate move
            Board tempBoard = board.clone();
            tempBoard.movePiece(move.getFrom(), move.getTo());

            // Evaluate position
            int score = minimax(tempBoard, depth - 1, false,
                Integer.MIN_VALUE, Integer.MAX_VALUE);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private int minimax(Board board, int depth, boolean isMaximizing,
                        int alpha, int beta) {
        // Base case: reached max depth or game over
        if (depth == 0 || isGameOver(board)) {
            return evaluateBoard(board);
        }

        List<Move> moves = getAllLegalMoves(board);

        if (isMaximizing) {
            int maxScore = Integer.MIN_VALUE;
            for (Move move : moves) {
                Board tempBoard = board.clone();
                tempBoard.movePiece(move.getFrom(), move.getTo());

                int score = minimax(tempBoard, depth - 1, false, alpha, beta);
                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score);

                if (beta <= alpha) break;  // Alpha-beta pruning
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (Move move : moves) {
                Board tempBoard = board.clone();
                tempBoard.movePiece(move.getFrom(), move.getTo());

                int score = minimax(tempBoard, depth - 1, true, alpha, beta);
                minScore = Math.min(minScore, score);
                beta = Math.min(beta, score);

                if (beta <= alpha) break;  // Alpha-beta pruning
            }
            return minScore;
        }
    }

    private int evaluateBoard(Board board) {
        int score = 0;

        // Material value
        for (Piece piece : board.getAllPieces(Color.WHITE)) {
            score += getPieceValue(piece);
        }
        for (Piece piece : board.getAllPieces(Color.BLACK)) {
            score -= getPieceValue(piece);
        }

        // Position value (e.g., controlling center)
        score += evaluatePosition(board);

        return score;
    }

    private int getPieceValue(Piece piece) {
        switch (piece.getType()) {
            case PAWN:   return 100;
            case KNIGHT: return 320;
            case BISHOP: return 330;
            case ROOK:   return 500;
            case QUEEN:  return 900;
            case KING:   return 20000;
            default:     return 0;
        }
    }
}
```

**3. Integration with ChessGame:**
```java
public class ChessGame {
    private Player whitePlayer;
    private Player blackPlayer;

    public void playTurn() {
        Player currentPlayer = getCurrentPlayer();

        if (currentPlayer instanceof AIPlayer) {
            // AI selects move automatically
            List<Move> legalMoves = getAllLegalMoves();
            Move aiMove = currentPlayer.selectMove(board, legalMoves);
            makeMove(aiMove);
        } else {
            // Wait for human input
            // UI will call makeMove() when human selects move
        }
    }
}

// Usage
Player human = new HumanPlayer();
Player ai = new AIPlayer(4);  // Depth 4 search

ChessGame game = new ChessGame(human, ai);
```

**Optimizations:**
- ✅ Alpha-beta pruning (reduces search space)
- ✅ Move ordering (check captures first)
- ✅ Transposition tables (cache evaluations)
- ✅ Iterative deepening (better time management)

---

## ✅ SUMMARY

**Key Interview Points:**
1. ✅ Clear separation of concerns
2. ✅ Multiple design patterns (Strategy, Command, Facade)
3. ✅ SOLID principles throughout
4. ✅ Extensible design (easy to add features)
5. ✅ Proper error handling
6. ✅ Consideration of concurrency
7. ✅ Database persistence strategy
8. ✅ Performance optimization (board cloning, minimax)

**Be Ready to Discuss:**
- Trade-offs in design decisions
- Alternative approaches
- Scalability considerations
- Testing strategy
- Extension scenarios

---
