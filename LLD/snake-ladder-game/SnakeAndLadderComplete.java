/**
 * ============================================================================
 * SNAKE AND LADDER GAME - COMPLETE JAVA IMPLEMENTATION
 * ============================================================================
 *
 * This file contains the complete, production-ready implementation of
 * Snake and Ladder game following SOLID principles and design patterns.
 *
 * DESIGN PATTERNS USED:
 * - Strategy Pattern: DiceStrategy for different dice behaviors
 * - Factory Pattern: GameFactory, BoardFactory, DiceFactory for object creation
 * - Builder Pattern: GameBuilder for flexible game construction
 * - Template Method: BoardElement for common structure
 * - Facade: Game class as simple interface
 *
 * SOLID PRINCIPLES:
 * - Single Responsibility: Each class has one clear purpose
 * - Open/Closed: Easy to extend without modification
 * - Liskov Substitution: Subclasses work interchangeably
 * - Interface Segregation: Small, focused interfaces
 * - Dependency Inversion: Depend on abstractions
 *
 * AUTHOR: LLD Tutorial
 * DATE: 2025
 * ============================================================================
 */

import java.util.*;
import java.util.stream.Collectors;

// ============================================================================
// DOMAIN ENTITIES - Core Business Objects
// ============================================================================

/**
 * Represents a player in the game.
 *
 * RESPONSIBILITY: Manage player identity and position
 * SOLID: Single Responsibility - Only manages player state
 */
class Player {
    private final String playerId;      // Unique identifier
    private final String playerName;    // Display name
    private int currentPosition;        // Current position (0-100)

    /**
     * Creates a new player starting at position 0.
     *
     * @param id Unique player identifier (e.g., "P1", "P2")
     * @param name Display name (e.g., "Alice", "Bob")
     */
    public Player(String id, String name) {
        this.playerId = id;
        this.playerName = name;
        this.currentPosition = 0;  // All players start at position 0 (off board)
    }

    // Getters
    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public int getCurrentPosition() { return currentPosition; }

    /**
     * Sets player position to a specific value.
     * Used when landing on snake/ladder.
     *
     * @param position New position (0-100)
     */
    public void setPosition(int position) {
        this.currentPosition = position;
    }

    /**
     * Moves player forward by specified steps.
     *
     * @param steps Number of steps to move
     */
    public void moveBy(int steps) {
        this.currentPosition += steps;
    }

    @Override
    public String toString() {
        return String.format("%s (Position: %d)", playerName, currentPosition);
    }
}

/**
 * Represents a single cell on the board.
 *
 * RESPONSIBILITY: Represent a board position
 * SOLID: Single Responsibility - Just holds position data
 */
class Cell {
    private final int position;

    public Cell(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Cell[" + position + "]";
    }
}

// ============================================================================
// BOARD ELEMENTS - Polymorphic Hierarchy
// ============================================================================

/**
 * Abstract base class for all board elements (Snake, Ladder, etc.).
 *
 * RESPONSIBILITY: Define common structure for board elements
 * DESIGN PATTERN: Template Method
 * SOLID: Open/Closed - Easy to add new element types
 */
abstract class BoardElement {
    protected final int startPosition;   // Where element begins
    protected final int endPosition;     // Where element leads to

    /**
     * Creates a board element.
     *
     * @param start Starting position
     * @param end Ending position
     */
    public BoardElement(int start, int end) {
        this.startPosition = start;
        this.endPosition = end;
    }

    public int getStartPosition() { return startPosition; }
    public int getEndPosition() { return endPosition; }

    /**
     * Returns the type of this element.
     * Must be implemented by subclasses.
     *
     * @return Element type (e.g., "SNAKE", "LADDER")
     */
    public abstract String getElementType();

    /**
     * Returns a display message for this element.
     * Must be implemented by subclasses.
     *
     * @return User-friendly message
     */
    public abstract String getMessage();

    @Override
    public String toString() {
        return String.format("%s[%d → %d]", getElementType(), startPosition, endPosition);
    }
}

/**
 * Represents a snake that sends players backward.
 *
 * RESPONSIBILITY: Encapsulate snake behavior
 * SOLID: Liskov Substitution - Can be used wherever BoardElement is expected
 */
class Snake extends BoardElement {

    /**
     * Creates a snake from head to tail.
     *
     * @param head Snake head position (higher value)
     * @param tail Snake tail position (lower value)
     * @throws IllegalArgumentException if head <= tail
     */
    public Snake(int head, int tail) {
        super(head, tail);

        // VALIDATION: Snake must go downward
        if (head <= tail) {
            throw new IllegalArgumentException(
                String.format("Invalid snake: head (%d) must be > tail (%d)", head, tail)
            );
        }
    }

    @Override
    public String getElementType() {
        return "SNAKE";
    }

    @Override
    public String getMessage() {
        return String.format("🐍 Oops! Snake bite! Slide from %d to %d", startPosition, endPosition);
    }

    // Convenience methods with better semantics
    public int getHead() { return startPosition; }
    public int getTail() { return endPosition; }
}

/**
 * Represents a ladder that sends players forward.
 *
 * RESPONSIBILITY: Encapsulate ladder behavior
 * SOLID: Liskov Substitution - Can be used wherever BoardElement is expected
 */
class Ladder extends BoardElement {

    /**
     * Creates a ladder from bottom to top.
     *
     * @param bottom Ladder bottom position (lower value)
     * @param top Ladder top position (higher value)
     * @throws IllegalArgumentException if top <= bottom
     */
    public Ladder(int bottom, int top) {
        super(bottom, top);

        // VALIDATION: Ladder must go upward
        if (top <= bottom) {
            throw new IllegalArgumentException(
                String.format("Invalid ladder: top (%d) must be > bottom (%d)", top, bottom)
            );
        }
    }

    @Override
    public String getElementType() {
        return "LADDER";
    }

    @Override
    public String getMessage() {
        return String.format("🪜 Yay! Climb the ladder from %d to %d", startPosition, endPosition);
    }

    // Convenience methods with better semantics
    public int getBottom() { return startPosition; }
    public int getTop() { return endPosition; }
}

// ============================================================================
// BOARD - Manages Game Board
// ============================================================================

/**
 * Represents the game board with cells and board elements.
 *
 * RESPONSIBILITY: Manage board structure and configuration
 * SOLID:
 *   - Single Responsibility: Only manages board
 *   - Open/Closed: Can add new board element types
 */
class Board {
    private final int boardSize;                                    // Total cells (typically 100)
    private final Map<Integer, Cell> cells;                         // All board cells
    private final Map<Integer, BoardElement> boardElements;         // Snakes and ladders

    /**
     * Creates a board with specified size.
     *
     * @param size Total number of cells (e.g., 100)
     */
    public Board(int size) {
        this.boardSize = size;
        this.cells = new HashMap<>();
        this.boardElements = new HashMap<>();
        initializeBoard();
    }

    /**
     * Initializes all cells on the board.
     * PRIVATE: Internal implementation detail
     */
    private void initializeBoard() {
        for (int i = 1; i <= boardSize; i++) {
            cells.put(i, new Cell(i));
        }
    }

    public int getBoardSize() { return boardSize; }

    /**
     * Gets the cell at specified position.
     *
     * @param position Cell position (1-boardSize)
     * @return Cell object or null if invalid position
     */
    public Cell getCell(int position) {
        return cells.get(position);
    }

    /**
     * Adds a board element (snake or ladder) to the board.
     *
     * @param element BoardElement to add
     * @throws IllegalArgumentException if position already has an element
     */
    public void addBoardElement(BoardElement element) {
        int startPos = element.getStartPosition();

        // VALIDATION: Check if position already occupied
        if (boardElements.containsKey(startPos)) {
            throw new IllegalArgumentException(
                String.format("Position %d already has a %s",
                             startPos,
                             boardElements.get(startPos).getElementType())
            );
        }

        // VALIDATION: Element must be within board boundaries
        if (startPos < 1 || startPos > boardSize || element.getEndPosition() < 1 || element.getEndPosition() > boardSize) {
            throw new IllegalArgumentException("Board element positions must be within 1-" + boardSize);
        }

        boardElements.put(startPos, element);
    }

    /**
     * Gets the board element at specified position.
     *
     * @param position Position to check
     * @return BoardElement if exists, null otherwise
     */
    public BoardElement getBoardElement(int position) {
        return boardElements.get(position);
    }

    /**
     * Returns all board elements for display or configuration.
     *
     * @return Unmodifiable collection of board elements
     */
    public Collection<BoardElement> getAllBoardElements() {
        return Collections.unmodifiableCollection(boardElements.values());
    }
}

// ============================================================================
// DICE STRATEGY - Strategy Pattern
// ============================================================================

/**
 * Strategy interface for different dice rolling behaviors.
 *
 * DESIGN PATTERN: Strategy
 * SOLID:
 *   - Interface Segregation: Small, focused interface
 *   - Open/Closed: Easy to add new dice types
 *   - Dependency Inversion: Game depends on this interface
 */
interface DiceStrategy {
    /**
     * Rolls the dice and returns a value.
     *
     * @return Dice roll result
     */
    int roll();
}

/**
 * Standard 6-sided dice (1-6).
 *
 * RESPONSIBILITY: Generate random values 1-6
 */
class StandardDice implements DiceStrategy {
    private final Random random;

    public StandardDice() {
        this.random = new Random();
    }

    @Override
    public int roll() {
        return 1 + random.nextInt(6);  // Returns 1-6
    }
}

/**
 * Loaded dice that always returns a fixed value.
 * USEFUL FOR TESTING!
 *
 * RESPONSIBILITY: Return predictable value for testing
 */
class LoadedDice implements DiceStrategy {
    private final int fixedValue;

    /**
     * Creates a loaded dice.
     *
     * @param value Fixed value to return (typically 1-6)
     */
    public LoadedDice(int value) {
        this.fixedValue = value;
    }

    @Override
    public int roll() {
        return fixedValue;
    }
}

/**
 * Custom dice with configurable range.
 *
 * RESPONSIBILITY: Generate random values in custom range
 */
class CustomRangeDice implements DiceStrategy {
    private final int min;
    private final int max;
    private final Random random;

    /**
     * Creates custom dice with specified range.
     *
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     */
    public CustomRangeDice(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("Min must be < max");
        }
        this.min = min;
        this.max = max;
        this.random = new Random();
    }

    @Override
    public int roll() {
        return min + random.nextInt(max - min + 1);
    }
}

// ============================================================================
// GAME - Main Controller (Facade)
// ============================================================================

/**
 * Main game controller that orchestrates the Snake and Ladder game.
 *
 * RESPONSIBILITY: Manage game flow and coordinate components
 * DESIGN PATTERN: Facade (provides simple interface to complex subsystem)
 * SOLID:
 *   - Single Responsibility: Only orchestrates game flow
 *   - Dependency Inversion: Depends on DiceStrategy interface
 */
class Game {
    // Dependencies (injected via Builder)
    private final Board board;
    private final List<Player> players;
    private final DiceStrategy diceStrategy;

    // Game state
    private int currentPlayerIndex;
    private boolean isGameOver;
    private Player winner;

    /**
     * Private constructor - only GameBuilder can create instances.
     * This enforces use of Builder pattern.
     *
     * @param builder GameBuilder instance
     */
    Game(GameBuilder builder) {
        this.board = builder.getBoard();
        this.players = new ArrayList<>(builder.getPlayers());
        this.diceStrategy = builder.getDiceStrategy();
        this.currentPlayerIndex = 0;
        this.isGameOver = false;
        this.winner = null;
    }

    /**
     * Starts the game loop.
     * Continues until a player wins.
     */
    public void start() {
        System.out.println("\n========================================");
        System.out.println("🎮 SNAKE AND LADDER GAME STARTED! 🎮");
        System.out.println("========================================\n");

        displayGameState();

        // Main game loop
        while (!isGameOver) {
            playTurn();
        }

        announceWinner();
    }

    /**
     * Executes one complete turn for the current player.
     */
    public void playTurn() {
        Player currentPlayer = players.get(currentPlayerIndex);

        System.out.println("\n========================================");
        System.out.println("👤 " + currentPlayer.getPlayerName() + "'s turn");
        System.out.println("========================================");

        // Roll dice
        int diceValue = diceStrategy.roll();
        System.out.println("🎲 Rolled: " + diceValue);

        // Execute move
        executePlayerMove(currentPlayer, diceValue);

        // Check win condition
        if (checkWinCondition(currentPlayer)) {
            this.isGameOver = true;
            this.winner = currentPlayer;
            return;
        }

        // Handle extra turn on rolling 6
        if (diceValue == 6) {
            System.out.println("🎉 You rolled a 6! Go again!");
            // Don't switch player
        } else {
            switchToNextPlayer();
        }

        displayGameState();
    }

    /**
     * Moves player and handles board elements.
     * PRIVATE: Internal game logic
     *
     * @param player Player to move
     * @param diceValue Dice roll value
     */
    private void executePlayerMove(Player player, int diceValue) {
        int currentPos = player.getCurrentPosition();
        int newPos = currentPos + diceValue;

        // RULE: Cannot exceed board size
        if (newPos > board.getBoardSize()) {
            System.out.println("❌ Need exact number to win! Staying at position " + currentPos);
            return;
        }

        // Move player
        player.setPosition(newPos);
        System.out.println("📍 Moved from " + currentPos + " → " + newPos);

        // Check for snake or ladder (POLYMORPHISM in action!)
        handleBoardElement(player);
    }

    /**
     * Handles snake or ladder at player's position.
     * DEMONSTRATES: Polymorphism - works for Snake, Ladder, or any BoardElement
     *
     * @param player Player to check
     */
    private void handleBoardElement(Player player) {
        int position = player.getCurrentPosition();
        BoardElement element = board.getBoardElement(position);

        if (element != null) {
            // POLYMORPHIC CALL: Works for any BoardElement type!
            System.out.println(element.getMessage());
            player.setPosition(element.getEndPosition());
            System.out.println("📍 New position: " + player.getCurrentPosition());
        }
    }

    /**
     * Checks if player has won the game.
     *
     * @param player Player to check
     * @return true if player reached position equal to board size
     */
    private boolean checkWinCondition(Player player) {
        return player.getCurrentPosition() == board.getBoardSize();
    }

    /**
     * Switches to the next player's turn.
     * Uses modulo for round-robin rotation.
     */
    private void switchToNextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    /**
     * Displays current game state.
     */
    private void displayGameState() {
        System.out.println("\n📊 Current Positions:");
        System.out.println("========================================");

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            String marker = (i == currentPlayerIndex) ? " ← CURRENT" : "";
            System.out.printf("  %s: Position %d%s\n",
                            player.getPlayerName(),
                            player.getCurrentPosition(),
                            marker);
        }

        System.out.println("========================================");
    }

    /**
     * Announces the winner and displays final standings.
     */
    private void announceWinner() {
        System.out.println("\n\n🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉");
        System.out.println("  CONGRATULATIONS " + winner.getPlayerName().toUpperCase() + "!");
        System.out.println("  You won the game!");
        System.out.println("🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉");

        displayFinalStandings();
    }

    /**
     * Displays final standings sorted by position.
     */
    private void displayFinalStandings() {
        System.out.println("\n🏆 Final Standings:");
        System.out.println("========================================");

        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((p1, p2) -> Integer.compare(p2.getCurrentPosition(), p1.getCurrentPosition()));

        int rank = 1;
        for (Player player : sortedPlayers) {
            String medal = (player == winner) ? " 🏆 WINNER" : "";
            System.out.printf("  %d. %s: %d%s\n",
                            rank++,
                            player.getPlayerName(),
                            player.getCurrentPosition(),
                            medal);
        }

        System.out.println("========================================");
    }

    // Getters for testing and external access
    public boolean isGameOver() { return isGameOver; }
    public Player getWinner() { return winner; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public Board getBoard() { return board; }
}

// ============================================================================
// BUILDER PATTERN - Flexible Game Construction
// ============================================================================

/**
 * Builder for constructing Game instances.
 *
 * DESIGN PATTERN: Builder
 * BENEFITS:
 *   - Flexible construction
 *   - Readable API (fluent interface)
 *   - Validation before creation
 */
class GameBuilder {
    private List<Player> players;
    private Board board;
    private DiceStrategy diceStrategy;

    public GameBuilder() {
        this.players = new ArrayList<>();
        this.board = null;
        this.diceStrategy = new StandardDice();  // Default dice
    }

    public GameBuilder setPlayers(List<Player> players) {
        this.players = new ArrayList<>(players);
        return this;
    }

    public GameBuilder addPlayer(Player player) {
        this.players.add(player);
        return this;
    }

    public GameBuilder setBoard(Board board) {
        this.board = board;
        return this;
    }

    public GameBuilder setDiceStrategy(DiceStrategy diceStrategy) {
        this.diceStrategy = diceStrategy;
        return this;
    }

    /**
     * Builds and returns the Game instance.
     * VALIDATION: Ensures all required fields are set
     *
     * @return Configured Game instance
     * @throws IllegalStateException if validation fails
     */
    public Game build() {
        // VALIDATION
        if (players.size() < 2) {
            throw new IllegalStateException("Need at least 2 players to start the game");
        }

        if (board == null) {
            throw new IllegalStateException("Board is required");
        }

        if (diceStrategy == null) {
            throw new IllegalStateException("DiceStrategy is required");
        }

        return new Game(this);
    }

    // Package-private getters for Game constructor
    List<Player> getPlayers() { return players; }
    Board getBoard() { return board; }
    DiceStrategy getDiceStrategy() { return diceStrategy; }
}

// ============================================================================
// FACTORY PATTERN - Centralized Object Creation
// ============================================================================

/**
 * Factory for creating Board instances with different configurations.
 *
 * DESIGN PATTERN: Factory Method
 * BENEFITS:
 *   - Centralized creation logic
 *   - Hide configuration complexity
 *   - Provide pre-configured boards
 */
class BoardFactory {

    /**
     * Creates standard 10x10 board with classic snake/ladder configuration.
     *
     * @return Configured board with 10 snakes and 10 ladders
     */
    public static Board createStandardBoard() {
        Board board = new Board(100);

        // Add standard snakes (10 snakes)
        board.addBoardElement(new Snake(99, 54));
        board.addBoardElement(new Snake(95, 75));
        board.addBoardElement(new Snake(92, 88));
        board.addBoardElement(new Snake(89, 68));
        board.addBoardElement(new Snake(74, 53));
        board.addBoardElement(new Snake(64, 60));
        board.addBoardElement(new Snake(62, 19));
        board.addBoardElement(new Snake(49, 11));
        board.addBoardElement(new Snake(46, 25));
        board.addBoardElement(new Snake(16, 6));

        // Add standard ladders (10 ladders)
        board.addBoardElement(new Ladder(2, 38));
        board.addBoardElement(new Ladder(7, 14));
        board.addBoardElement(new Ladder(8, 31));
        board.addBoardElement(new Ladder(15, 26));
        board.addBoardElement(new Ladder(21, 42));
        board.addBoardElement(new Ladder(28, 84));
        board.addBoardElement(new Ladder(36, 44));
        board.addBoardElement(new Ladder(51, 67));
        board.addBoardElement(new Ladder(71, 91));
        board.addBoardElement(new Ladder(78, 98));

        return board;
    }

    /**
     * Creates board with custom size.
     *
     * @param size Number of cells
     * @return Empty board with specified size
     */
    public static Board createCustomBoard(int size) {
        return new Board(size);
    }
}

/**
 * Factory for creating DiceStrategy instances.
 *
 * DESIGN PATTERN: Factory Method
 */
class DiceFactory {

    public static DiceStrategy createStandardDice() {
        return new StandardDice();
    }

    public static DiceStrategy createLoadedDice(int value) {
        return new LoadedDice(value);
    }

    public static DiceStrategy createCustomDice(int min, int max) {
        return new CustomRangeDice(min, max);
    }
}

/**
 * Factory for creating Game instances.
 *
 * DESIGN PATTERN: Factory Method
 * BENEFITS:
 *   - One-line game creation
 *   - Hides Builder complexity
 *   - Provides common configurations
 */
class GameFactory {

    /**
     * Creates a standard game with default board and dice.
     *
     * @param players List of players (minimum 2)
     * @return Configured game ready to play
     */
    public static Game createStandardGame(List<Player> players) {
        Board board = BoardFactory.createStandardBoard();
        DiceStrategy dice = DiceFactory.createStandardDice();

        return new GameBuilder()
                .setPlayers(players)
                .setBoard(board)
                .setDiceStrategy(dice)
                .build();
    }

    /**
     * Creates a test game with loaded dice for deterministic testing.
     *
     * @param players List of players
     * @param fixedDiceValue Fixed dice value for testing
     * @return Test game with loaded dice
     */
    public static Game createTestGame(List<Player> players, int fixedDiceValue) {
        Board board = BoardFactory.createStandardBoard();
        DiceStrategy dice = new LoadedDice(fixedDiceValue);

        return new GameBuilder()
                .setPlayers(players)
                .setBoard(board)
                .setDiceStrategy(dice)
                .build();
    }
}

// ============================================================================
// MAIN - Entry Point
// ============================================================================

/**
 * Main class to run the Snake and Ladder game.
 */
public class SnakeAndLadderComplete {

    public static void main(String[] args) {
        // Create players
        List<Player> players = Arrays.asList(
            new Player("P1", "Alice"),
            new Player("P2", "Bob"),
            new Player("P3", "Charlie")
        );

        // Create and start game using Factory
        Game game = GameFactory.createStandardGame(players);

        // Start the game!
        game.start();

        // Game over - winner announced automatically
    }

    /**
     * Example: Custom game with custom board and dice
     */
    public static void exampleCustomGame() {
        // Create custom board
        Board customBoard = BoardFactory.createCustomBoard(50);
        customBoard.addBoardElement(new Snake(49, 10));
        customBoard.addBoardElement(new Ladder(5, 25));

        // Create custom dice (1-8)
        DiceStrategy customDice = DiceFactory.createCustomDice(1, 8);

        // Create players
        List<Player> players = Arrays.asList(
            new Player("P1", "Alice"),
            new Player("P2", "Bob")
        );

        // Build custom game
        Game customGame = new GameBuilder()
                             .setPlayers(players)
                             .setBoard(customBoard)
                             .setDiceStrategy(customDice)
                             .build();

        customGame.start();
    }
}

/**
 * ============================================================================
 * END OF IMPLEMENTATION
 * ============================================================================
 *
 * USAGE EXAMPLES:
 *
 * 1. Standard Game:
 *    Game game = GameFactory.createStandardGame(players);
 *    game.start();
 *
 * 2. Custom Game:
 *    Game game = new GameBuilder()
 *                   .setPlayers(players)
 *                   .setBoard(customBoard)
 *                   .setDiceStrategy(customDice)
 *                   .build();
 *
 * 3. Test Game:
 *    Game game = GameFactory.createTestGame(players, 4);  // Always rolls 4
 *
 * EXTENDING THE DESIGN:
 *
 * 1. Add new board element type:
 *    class Portal extends BoardElement { ... }
 *
 * 2. Add new dice type:
 *    class WeightedDice implements DiceStrategy { ... }
 *
 * 3. Add new board size:
 *    Board board = BoardFactory.createCustomBoard(200);
 *
 * ALL WITHOUT MODIFYING EXISTING CODE! (Open/Closed Principle)
 * ============================================================================
 */
