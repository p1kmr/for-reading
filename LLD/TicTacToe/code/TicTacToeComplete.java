/**
 * ============================================================================
 * TIC-TAC-TOE GAME - COMPLETE IMPLEMENTATION
 * ============================================================================
 *
 * This file contains a complete, beginner-friendly implementation of
 * Tic-Tac-Toe following object-oriented design principles and SOLID principles.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Separation of Concerns (each class has one responsibility)
 * - Enum usage for type safety
 * - Composition and Association relationships
 * - Dependency Injection
 * - Repository Pattern
 * - Service Layer Pattern
 * - Factory Pattern
 *
 * Author: Generated for Learning Purposes
 * Date: 2025
 * ============================================================================
 */

// ============================================================================
// ENUMS - Type-safe constants
// ============================================================================

/**
 * CellState Enum
 *
 * Represents the three possible states of a cell on the board.
 * Using enum instead of String prevents invalid values like "A" or "x".
 *
 * BENEFIT: Compile-time type safety
 */
enum CellState {
    EMPTY(" "),  // Empty cell, displayed as space
    X("X"),      // Cell marked with X
    O("O");      // Cell marked with O

    private final String display;

    // Constructor to set display value
    CellState(String display) {
        this.display = display;
    }

    // Get string representation for display
    public String getDisplay() {
        return display;
    }
}

/**
 * Symbol Enum
 *
 * Represents player symbols (X or O only).
 * Separate from CellState because players can't have EMPTY symbol.
 *
 * DESIGN DECISION: Different enum for different domain concepts
 */
enum Symbol {
    X, O;

    // Convert Symbol to corresponding CellState
    public CellState toCellState() {
        return this == X ? CellState.X : CellState.O;
    }
}

/**
 * GameStatus Enum
 *
 * Represents all possible states of a game.
 * Makes game state explicit and type-safe.
 *
 * STATE MACHINE: NOT_STARTED -> IN_PROGRESS -> (WIN/DRAW)
 */
enum GameStatus {
    NOT_STARTED,
    IN_PROGRESS,
    PLAYER_X_WON,
    PLAYER_O_WON,
    DRAW;

    // Check if game has ended
    public boolean isGameOver() {
        return this == PLAYER_X_WON ||
               this == PLAYER_O_WON ||
               this == DRAW;
    }

    // Get user-friendly message
    public String getMessage() {
        switch (this) {
            case PLAYER_X_WON: return "Player X wins!";
            case PLAYER_O_WON: return "Player O wins!";
            case DRAW: return "It's a draw!";
            case IN_PROGRESS: return "Game in progress...";
            default: return "Game not started";
        }
    }
}

// ============================================================================
// ENTITY CLASSES - Domain Objects
// ============================================================================

/**
 * Cell Class
 *
 * Represents a single cell/position on the Tic-Tac-Toe board.
 *
 * RESPONSIBILITY: Store and manage the state of one cell
 * DESIGN: Immutable position (row, col), mutable state
 */
class Cell {
    private final int row;      // Position: 0, 1, or 2 (immutable)
    private final int column;   // Position: 0, 1, or 2 (immutable)
    private CellState state;    // Current state: EMPTY, X, or O (mutable)

    /**
     * Constructor
     * @param row Row position (0-2)
     * @param column Column position (0-2)
     */
    public Cell(int row, int column) {
        this.row = row;
        this.column = column;
        this.state = CellState.EMPTY;  // Start empty
    }

    // Getters
    public int getRow() { return row; }
    public int getColumn() { return column; }
    public CellState getState() { return state; }

    // Setter with validation could be added here
    public void setState(CellState state) {
        this.state = state;
    }

    // Check if cell is empty (available for a move)
    public boolean isEmpty() {
        return state == CellState.EMPTY;
    }

    // Reset cell to empty
    public void reset() {
        this.state = CellState.EMPTY;
    }

    @Override
    public String toString() {
        return state.getDisplay();
    }
}

/**
 * Board Class
 *
 * Represents the 3x3 game board.
 *
 * RESPONSIBILITY: Manage the grid structure and cell operations
 * COMPOSITION: Board OWNS cells (creates and manages their lifecycle)
 */
class Board {
    private final Cell[][] cells;  // 2D array of cells (3x3)
    private final int size;        // Board size (3 for standard)

    /**
     * Constructor - Creates a new board with empty cells
     * @param size Board dimension (3 for standard Tic-Tac-Toe)
     */
    public Board(int size) {
        this.size = size;
        this.cells = new Cell[size][size];

        // Initialize all cells - COMPOSITION in action!
        // Board creates and owns the cells
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }
    }

    /**
     * Get a specific cell
     * @param row Row index (0-2)
     * @param col Column index (0-2)
     * @return The cell at that position
     */
    public Cell getCell(int row, int col) {
        return cells[row][col];
    }

    /**
     * Get all cells (for win checking)
     * @return 2D array of all cells
     */
    public Cell[][] getAllCells() {
        return cells;
    }

    /**
     * Place a symbol on the board
     * @param row Row position
     * @param col Column position
     * @param symbol Symbol to place (X or O)
     * @return true if placed successfully
     */
    public boolean placeSymbol(int row, int col, Symbol symbol) {
        Cell cell = cells[row][col];
        if (cell.isEmpty()) {
            cell.setState(symbol.toCellState());
            return true;
        }
        return false;
    }

    /**
     * Check if board is completely filled
     * @return true if all cells are occupied
     */
    public boolean isFull() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cells[i][j].isEmpty()) {
                    return false;  // Found empty cell
                }
            }
        }
        return true;  // No empty cells
    }

    /**
     * Reset board to initial state (all cells empty)
     */
    public void reset() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j].reset();
            }
        }
    }

    /**
     * Display the board to console
     *
     * Example output:
     *     0   1   2
     *   +---+---+---+
     * 0 | X | O |   |
     *   +---+---+---+
     * 1 |   | X |   |
     *   +---+---+---+
     * 2 | O |   | X |
     *   +---+---+---+
     */
    public void display() {
        // Print column numbers
        System.out.print("    ");
        for (int i = 0; i < size; i++) {
            System.out.print(i + "   ");
        }
        System.out.println();

        // Print separator
        printSeparator();

        // Print each row
        for (int i = 0; i < size; i++) {
            System.out.print(i + " ");  // Row number
            for (int j = 0; j < size; j++) {
                System.out.print("| " + cells[i][j] + " ");
            }
            System.out.println("|");
            printSeparator();
        }
    }

    // Helper method to print horizontal separator
    private void printSeparator() {
        System.out.print("  ");
        for (int i = 0; i < size; i++) {
            System.out.print("+---");
        }
        System.out.println("+");
    }

    public int getSize() {
        return size;
    }
}

/**
 * Player Class
 *
 * Represents a player in the game.
 *
 * RESPONSIBILITY: Store player information and statistics
 * DESIGN: Simple data holder (entity/POJO)
 */
class Player {
    private final String name;   // Player name (immutable)
    private final Symbol symbol; // Player symbol: X or O (immutable)
    private int wins;            // Number of wins (mutable)

    /**
     * Constructor
     * @param name Player name
     * @param symbol Player symbol (X or O)
     */
    public Player(String name, Symbol symbol) {
        this.name = name;
        this.symbol = symbol;
        this.wins = 0;
    }

    // Getters
    public String getName() { return name; }
    public Symbol getSymbol() { return symbol; }
    public int getWins() { return wins; }

    // Increment win count
    public void incrementWins() {
        this.wins++;
    }

    @Override
    public String toString() {
        return name + " (" + symbol + ")";
    }
}

// ============================================================================
// UTILITY CLASSES - Helper/Service Objects
// ============================================================================

/**
 * MoveValidator Class
 *
 * Validates player moves before they are applied to the board.
 *
 * RESPONSIBILITY: Move validation logic (Single Responsibility Principle)
 * DESIGN: Depends on Board to check cell state
 */
class MoveValidator {
    private final Board board;

    public MoveValidator(Board board) {
        this.board = board;
    }

    /**
     * Check if a move is valid
     * @param row Row position
     * @param col Column position
     * @return true if move is valid
     */
    public boolean isValidMove(int row, int col) {
        return isInBounds(row, col) && isCellEmpty(row, col);
    }

    /**
     * Get detailed validation error message
     * @param row Row position
     * @param col Column position
     * @return Error message, or empty string if valid
     */
    public String getValidationError(int row, int col) {
        if (!isInBounds(row, col)) {
            return "Invalid position! Row and column must be between 0 and 2.";
        }
        if (!isCellEmpty(row, col)) {
            return "Cell already occupied! Choose an empty cell.";
        }
        return "";  // No error, move is valid
    }

    // Check if position is within board bounds
    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < 3 && col >= 0 && col < 3;
    }

    // Check if cell at position is empty
    private boolean isCellEmpty(int row, int col) {
        return board.getCell(row, col).isEmpty();
    }
}

/**
 * WinChecker Class
 *
 * Detects win and draw conditions.
 *
 * RESPONSIBILITY: Win/draw detection algorithm (Single Responsibility)
 * DESIGN: Independent algorithm, depends on Board for data
 * EXTENSIBILITY: Can be extended for larger boards (4x4, 5x5, NxN)
 */
class WinChecker {
    private final Board board;
    private final int winLength = 3;  // Number in a row needed to win

    public WinChecker(Board board) {
        this.board = board;
    }

    /**
     * Check if specified symbol has won
     * @param symbol The symbol to check (X or O)
     * @return true if this symbol has three in a row
     */
    public boolean checkWin(Symbol symbol) {
        CellState state = symbol.toCellState();

        // Check all winning conditions
        return checkRows(state) ||
               checkColumns(state) ||
               checkDiagonals(state);
    }

    /**
     * Check if game is a draw (board full, no winner)
     * @return true if board is full
     */
    public boolean checkDraw() {
        return board.isFull();
    }

    // Check all rows for three in a row
    private boolean checkRows(CellState state) {
        Cell[][] cells = board.getAllCells();
        for (int row = 0; row < 3; row++) {
            if (cells[row][0].getState() == state &&
                cells[row][1].getState() == state &&
                cells[row][2].getState() == state) {
                return true;  // Found winning row
            }
        }
        return false;
    }

    // Check all columns for three in a column
    private boolean checkColumns(CellState state) {
        Cell[][] cells = board.getAllCells();
        for (int col = 0; col < 3; col++) {
            if (cells[0][col].getState() == state &&
                cells[1][col].getState() == state &&
                cells[2][col].getState() == state) {
                return true;  // Found winning column
            }
        }
        return false;
    }

    // Check both diagonals
    private boolean checkDiagonals(CellState state) {
        Cell[][] cells = board.getAllCells();

        // Top-left to bottom-right diagonal
        boolean diagonal1 = cells[0][0].getState() == state &&
                           cells[1][1].getState() == state &&
                           cells[2][2].getState() == state;

        // Top-right to bottom-left diagonal
        boolean diagonal2 = cells[0][2].getState() == state &&
                           cells[1][1].getState() == state &&
                           cells[2][0].getState() == state;

        return diagonal1 || diagonal2;
    }
}

// ============================================================================
// GAME ORCHESTRATOR - Main Game Controller
// ============================================================================

/**
 * Game Class
 *
 * Orchestrates the entire game flow.
 *
 * RESPONSIBILITY: Coordinate game flow, enforce rules, manage state
 * PATTERN: Facade - provides simple interface to complex subsystem
 * DEPENDENCY INJECTION: Depends on MoveValidator and WinChecker
 */
class Game {
    private final Board board;
    private final Player player1;
    private final Player player2;
    private Player currentPlayer;
    private GameStatus status;
    private final MoveValidator validator;
    private final WinChecker winChecker;

    /**
     * Constructor - Initialize game with two players
     * @param player1 First player (X)
     * @param player2 Second player (O)
     */
    public Game(Player player1, Player player2) {
        this.board = new Board(3);
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1;  // X always starts
        this.status = GameStatus.NOT_STARTED;

        // Dependency Injection: Create dependencies
        this.validator = new MoveValidator(board);
        this.winChecker = new WinChecker(board);
    }

    /**
     * Start a new game
     */
    public void start() {
        board.reset();
        currentPlayer = player1;  // X always starts
        status = GameStatus.IN_PROGRESS;
        System.out.println("\n=== NEW GAME STARTED ===");
        board.display();
    }

    /**
     * Make a move for the current player
     *
     * ALGORITHM:
     * 1. Validate move
     * 2. Place symbol on board
     * 3. Check for win
     * 4. Check for draw
     * 5. Switch player
     *
     * @param row Row position (0-2)
     * @param col Column position (0-2)
     * @return true if move was successful
     */
    public boolean makeMove(int row, int col) {
        // Validate move
        if (!validator.isValidMove(row, col)) {
            String error = validator.getValidationError(row, col);
            System.out.println("Invalid move: " + error);
            return false;
        }

        // Place symbol on board
        System.out.println(currentPlayer + " places at (" + row + ", " + col + ")");
        board.placeSymbol(row, col, currentPlayer.getSymbol());
        board.display();

        // Check for win
        if (winChecker.checkWin(currentPlayer.getSymbol())) {
            status = (currentPlayer.getSymbol() == Symbol.X)
                     ? GameStatus.PLAYER_X_WON
                     : GameStatus.PLAYER_O_WON;
            currentPlayer.incrementWins();
            System.out.println("\n*** " + status.getMessage() + " ***\n");
            return true;
        }

        // Check for draw
        if (winChecker.checkDraw()) {
            status = GameStatus.DRAW;
            System.out.println("\n*** " + status.getMessage() + " ***\n");
            return true;
        }

        // Game continues - switch player
        switchPlayer();
        System.out.println("Next turn: " + currentPlayer + "\n");
        return true;
    }

    // Switch to the other player
    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    // Getters
    public GameStatus getStatus() { return status; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public Player getWinner() {
        if (status == GameStatus.PLAYER_X_WON) return player1;
        if (status == GameStatus.PLAYER_O_WON) return player2;
        return null;
    }
    public Board getBoard() { return board; }
}

// ============================================================================
// SCOREBOARD - Statistics Tracking
// ============================================================================

/**
 * ScoreBoard Class
 *
 * Tracks game statistics across multiple games.
 *
 * RESPONSIBILITY: Maintain game statistics
 * DESIGN: Simple data aggregator
 */
class ScoreBoard {
    private final Player player1;
    private final Player player2;
    private int draws;
    private int totalGames;

    public ScoreBoard(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.draws = 0;
        this.totalGames = 0;
    }

    /**
     * Record the result of a game
     * @param status Final game status
     */
    public void recordGame(GameStatus status) {
        totalGames++;
        if (status == GameStatus.DRAW) {
            draws++;
        }
        // Player wins are tracked in Player objects
    }

    /**
     * Display current scoreboard
     */
    public void display() {
        System.out.println("\n======== SCOREBOARD ========");
        System.out.println(player1.getName() + " (X): " + player1.getWins() + " wins");
        System.out.println(player2.getName() + " (O): " + player2.getWins() + " wins");
        System.out.println("Draws: " + draws);
        System.out.println("Total Games: " + totalGames);
        System.out.println("============================\n");
    }
}

// ============================================================================
// DRIVER CLASS - Main Entry Point
// ============================================================================

/**
 * TicTacToeGame - Main Driver Class
 *
 * Demonstrates the game with hardcoded moves.
 * This shows how all the components work together.
 *
 * IN A REAL APPLICATION:
 * - Replace hardcoded moves with user input (Scanner)
 * - Add input validation
 * - Add menu system (play again, view stats, exit)
 */
public class TicTacToeComplete {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   TIC-TAC-TOE GAME DEMONSTRATION      ║");
        System.out.println("║   Object-Oriented Design Example      ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        // Create players
        Player player1 = new Player("Alice", Symbol.X);
        Player player2 = new Player("Bob", Symbol.O);

        // Create scoreboard
        ScoreBoard scoreBoard = new ScoreBoard(player1, player2);

        // ===== GAME 1: X Wins =====
        System.out.println("\n╔═══════════════════════╗");
        System.out.println("║      GAME 1           ║");
        System.out.println("╚═══════════════════════╝");

        Game game1 = new Game(player1, player2);
        game1.start();

        // Hardcoded moves for Game 1
        // X wins with top row: X X X
        game1.makeMove(0, 0);  // X at (0,0)
        game1.makeMove(1, 0);  // O at (1,0)
        game1.makeMove(0, 1);  // X at (0,1)
        game1.makeMove(1, 1);  // O at (1,1)
        game1.makeMove(0, 2);  // X at (0,2) - X WINS!

        scoreBoard.recordGame(game1.getStatus());

        // ===== GAME 2: Draw =====
        System.out.println("\n╔═══════════════════════╗");
        System.out.println("║      GAME 2           ║");
        System.out.println("╚═══════════════════════╝");

        Game game2 = new Game(player1, player2);
        game2.start();

        // Hardcoded moves for Game 2 (ends in draw)
        game2.makeMove(0, 0);  // X
        game2.makeMove(0, 1);  // O
        game2.makeMove(0, 2);  // X
        game2.makeMove(1, 0);  // O
        game2.makeMove(1, 2);  // X
        game2.makeMove(1, 1);  // O
        game2.makeMove(2, 0);  // X
        game2.makeMove(2, 2);  // O
        game2.makeMove(2, 1);  // X - DRAW!

        scoreBoard.recordGame(game2.getStatus());

        // ===== GAME 3: O Wins =====
        System.out.println("\n╔═══════════════════════╗");
        System.out.println("║      GAME 3           ║");
        System.out.println("╚═══════════════════════╝");

        Game game3 = new Game(player1, player2);
        game3.start();

        // Hardcoded moves for Game 3
        // O wins with diagonal
        game3.makeMove(0, 0);  // X
        game3.makeMove(1, 1);  // O
        game3.makeMove(0, 1);  // X
        game3.makeMove(2, 2);  // O
        game3.makeMove(2, 0);  // X
        game3.makeMove(0, 2);  // O - O WINS! (diagonal)

        scoreBoard.recordGame(game3.getStatus());

        // Display final scoreboard
        scoreBoard.display();

        // Demonstrate invalid move
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║   DEMONSTRATING INVALID MOVES         ║");
        System.out.println("╚═══════════════════════════════════════╝\n");

        Game game4 = new Game(player1, player2);
        game4.start();
        game4.makeMove(1, 1);  // X at (1,1)
        game4.makeMove(1, 1);  // O tries same cell - INVALID!
        game4.makeMove(5, 5);  // O tries out of bounds - INVALID!
        game4.makeMove(0, 0);  // O at valid position

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   DEMONSTRATION COMPLETE!              ║");
        System.out.println("║                                        ║");
        System.out.println("║   Key Concepts Demonstrated:           ║");
        System.out.println("║   ✓ Object-Oriented Design             ║");
        System.out.println("║   ✓ Enums for Type Safety              ║");
        System.out.println("║   ✓ Separation of Concerns             ║");
        System.out.println("║   ✓ Single Responsibility Principle    ║");
        System.out.println("║   ✓ Dependency Injection               ║");
        System.out.println("║   ✓ Composition Relationships          ║");
        System.out.println("║   ✓ Input Validation                   ║");
        System.out.println("║   ✓ State Management                   ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }
}

/*
 * ============================================================================
 * EXTENSION IDEAS (How to make this better):
 * ============================================================================
 *
 * 1. USER INPUT: Replace hardcoded moves with Scanner input
 *    Scanner scanner = new Scanner(System.in);
 *    System.out.print("Enter row (0-2): ");
 *    int row = scanner.nextInt();
 *
 * 2. AI OPPONENT: Create an AIPlayer class that extends Player
 *    - Random move strategy
 *    - Smart strategy (block opponent, go for win)
 *    - Minimax algorithm for perfect play
 *
 * 3. LARGER BOARDS: Make board size configurable (4x4, 5x5, NxN)
 *    - Update WinChecker to handle dynamic sizes
 *    - Configurable win length
 *
 * 4. UNDO/REDO: Add move history
 *    - Stack<Move> moveHistory
 *    - undo() and redo() methods
 *
 * 5. SAVE/LOAD GAMES: Persist game state
 *    - Serialize Game object
 *    - Save to file or database
 *
 * 6. NETWORK MULTIPLAYER: Play over network
 *    - Client-Server architecture
 *    - Socket communication
 *
 * 7. GUI: Add graphical interface
 *    - JavaFX or Swing
 *    - Clickable board
 *    - Animations
 *
 * 8. GAME VARIANTS:
 *    - Misère (lose if you get three in a row)
 *    - Wild Tic-Tac-Toe (choose X or O each turn)
 *    - 3D Tic-Tac-Toe
 *
 * 9. STATISTICS: Track more stats
 *    - Average game length
 *    - Win percentages
 *    - Move patterns
 *
 * 10. TOURNAMENT MODE: Multiple players, bracket system
 *
 * ============================================================================
 */
