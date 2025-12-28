/**
 * CHESS GAME - COMPLETE JAVA IMPLEMENTATION
 * ===========================================================================
 * This file contains the complete, beginner-friendly implementation of a Chess Game
 * following Low-Level Design (LLD) best practices.
 *
 * KEY DESIGN PATTERNS USED:
 * - Strategy Pattern (MoveValidator)
 * - Command Pattern (Move class)
 * - Facade Pattern (ChessGame class)
 * - Template Method (Piece hierarchy)
 *
 * SOLID PRINCIPLES APPLIED:
 * - Single Responsibility: Each class has one job
 * - Open/Closed: Easy to extend (add new pieces, validators)
 * - Liskov Substitution: All pieces are interchangeable
 * - Interface Segregation: Focused interfaces
 * - Dependency Inversion: Depend on abstractions
 *
 * ===========================================================================
 */

// ============================================================================
// ENUMERATIONS
// ============================================================================

/**
 * Enum representing piece colors
 */
enum Color {
    WHITE,
    BLACK;

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}

/**
 * Enum representing piece types
 */
enum PieceType {
    KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
}

/**
 * Enum representing game states
 */
enum GameState {
    NOT_STARTED,    // Game created but not started
    IN_PROGRESS,    // Game is active
    CHECK,          // King is under attack
    CHECKMATE,      // King in check with no escape (game over)
    STALEMATE,      // No legal moves but not in check (draw)
    DRAW,           // Draw by agreement or rules
    RESIGNED        // Player resigned
}

/**
 * Enum representing move types
 */
enum MoveType {
    NORMAL,              // Regular move
    CAPTURE,             // Capturing opponent's piece
    CASTLING_KINGSIDE,   // King-side castling (0-0)
    CASTLING_QUEENSIDE,  // Queen-side castling (0-0-0)
    EN_PASSANT,          // En passant capture
    PAWN_PROMOTION       // Pawn reaches last rank
}

// ============================================================================
// EXCEPTION CLASSES
// ============================================================================

/**
 * Exception thrown for invalid moves
 */
class InvalidMoveException extends Exception {
    public InvalidMoveException(String message) {
        super(message);
    }
}

/**
 * Exception thrown for invalid positions
 */
class InvalidPositionException extends InvalidMoveException {
    public InvalidPositionException(String message) {
        super(message);
    }
}

// ============================================================================
// POSITION CLASS
// ============================================================================

/**
 * Represents a position (square) on the chess board
 * Uses 0-based indexing internally: row [0-7], column [0-7]
 * Maps to chess notation: a-h (columns), 1-8 (rows)
 */
class Position {
    private final int row;     // 0-7 (immutable)
    private final int column;  // 0-7 (immutable)

    public Position(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    /**
     * Check if position is within board boundaries
     */
    public boolean isValid() {
        return row >= 0 && row < 8 && column >= 0 && column < 8;
    }

    /**
     * Convert to chess notation (e.g., "e2", "a8")
     */
    @Override
    public String toString() {
        char file = (char) ('a' + column);  // 0→a, 1→b, ..., 7→h
        int rank = row + 1;                 // 0→1, 1→2, ..., 7→8
        return "" + file + rank;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return this.row == other.row && this.column == other.column;
    }

    @Override
    public int hashCode() {
        return row * 8 + column;
    }
}

// ============================================================================
// PIECE HIERARCHY
// ============================================================================

/**
 * Abstract base class for all chess pieces
 * DESIGN PATTERN: Template Method Pattern
 * SOLID: Single Responsibility - manages piece state only
 */
abstract class Piece {
    private final Color color;        // WHITE or BLACK
    private final PieceType type;     // KING, QUEEN, etc.
    private boolean hasMoved;         // Track if piece has moved (for castling)

    protected Piece(Color color, PieceType type) {
        this.color = color;
        this.type = type;
        this.hasMoved = false;
    }

    // Getters
    public Color getColor() {
        return color;
    }

    public PieceType getType() {
        return type;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setMoved(boolean moved) {
        this.hasMoved = moved;
    }

    /**
     * Check if this piece can move from 'from' to 'to'
     * Subclasses MUST implement this with piece-specific rules
     */
    public abstract boolean canMove(Position from, Position to, Board board);

    /**
     * Get all possible moves for this piece from a given position
     */
    public abstract List<Position> getPossibleMoves(Position from, Board board);

    /**
     * Get symbol for display (Unicode chess symbols)
     */
    public abstract String getSymbol();

    @Override
    public String toString() {
        return color + " " + type;
    }
}

/**
 * KING: Moves 1 square in any direction
 */
class King extends Piece {

    public King(Color color) {
        super(color, PieceType.KING);
    }

    @Override
    public boolean canMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getColumn() - from.getColumn());

        // King moves exactly 1 square in any direction
        // OR 2 squares horizontally (castling - validated separately)
        if (colDiff == 2 && rowDiff == 0) {
            return true;  // Castling (validated by CastlingValidator)
        }

        return (rowDiff <= 1 && colDiff <= 1) && (rowDiff + colDiff > 0);
    }

    @Override
    public List<Position> getPossibleMoves(Position from, Board board) {
        List<Position> moves = new ArrayList<>();

        // All 8 directions around the king
        int[] dRow = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dCol = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            Position newPos = new Position(
                from.getRow() + dRow[i],
                from.getColumn() + dCol[i]
            );

            if (newPos.isValid()) {
                Piece targetPiece = board.getPiece(newPos);
                // Can move to empty square or capture opponent
                if (targetPiece == null || targetPiece.getColor() != getColor()) {
                    moves.add(newPos);
                }
            }
        }

        return moves;
    }

    @Override
    public String getSymbol() {
        return getColor() == Color.WHITE ? "♔" : "♚";
    }
}

/**
 * QUEEN: Moves any distance horizontally, vertically, or diagonally
 */
class Queen extends Piece {

    public Queen(Color color) {
        super(color, PieceType.QUEEN);
    }

    @Override
    public boolean canMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getColumn() - from.getColumn());

        // Horizontal or Vertical (like Rook)
        boolean straightMove = (rowDiff == 0 || colDiff == 0);

        // Diagonal (like Bishop)
        boolean diagonalMove = (rowDiff == colDiff);

        return straightMove || diagonalMove;
    }

    @Override
    public List<Position> getPossibleMoves(Position from, Board board) {
        // Queen combines Rook + Bishop movements
        List<Position> moves = new ArrayList<>();

        // 8 directions: 4 straight + 4 diagonal
        int[] dRow = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dCol = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int dir = 0; dir < 8; dir++) {
            // Keep going in this direction until blocked
            for (int step = 1; step < 8; step++) {
                Position newPos = new Position(
                    from.getRow() + dRow[dir] * step,
                    from.getColumn() + dCol[dir] * step
                );

                if (!newPos.isValid()) break;

                Piece targetPiece = board.getPiece(newPos);
                if (targetPiece == null) {
                    moves.add(newPos);  // Empty square
                } else {
                    if (targetPiece.getColor() != getColor()) {
                        moves.add(newPos);  // Can capture
                    }
                    break;  // Blocked by piece
                }
            }
        }

        return moves;
    }

    @Override
    public String getSymbol() {
        return getColor() == Color.WHITE ? "♕" : "♛";
    }
}

/**
 * ROOK: Moves any distance horizontally or vertically
 */
class Rook extends Piece {

    public Rook(Color color) {
        super(color, PieceType.ROOK);
    }

    @Override
    public boolean canMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getColumn() - from.getColumn());

        // Either row changes OR column changes (not both)
        return (rowDiff == 0 && colDiff > 0) || (colDiff == 0 && rowDiff > 0);
    }

    @Override
    public List<Position> getPossibleMoves(Position from, Board board) {
        List<Position> moves = new ArrayList<>();

        // 4 directions: up, down, left, right
        int[] dRow = {-1, 1, 0, 0};
        int[] dCol = {0, 0, -1, 1};

        for (int dir = 0; dir < 4; dir++) {
            for (int step = 1; step < 8; step++) {
                Position newPos = new Position(
                    from.getRow() + dRow[dir] * step,
                    from.getColumn() + dCol[dir] * step
                );

                if (!newPos.isValid()) break;

                Piece targetPiece = board.getPiece(newPos);
                if (targetPiece == null) {
                    moves.add(newPos);
                } else {
                    if (targetPiece.getColor() != getColor()) {
                        moves.add(newPos);
                    }
                    break;
                }
            }
        }

        return moves;
    }

    @Override
    public String getSymbol() {
        return getColor() == Color.WHITE ? "♖" : "♜";
    }
}

/**
 * BISHOP: Moves any distance diagonally
 */
class Bishop extends Piece {

    public Bishop(Color color) {
        super(color, PieceType.BISHOP);
    }

    @Override
    public boolean canMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getColumn() - from.getColumn());

        // Row change must equal column change (diagonal)
        return rowDiff == colDiff && rowDiff > 0;
    }

    @Override
    public List<Position> getPossibleMoves(Position from, Board board) {
        List<Position> moves = new ArrayList<>();

        // 4 diagonal directions
        int[] dRow = {-1, -1, 1, 1};
        int[] dCol = {-1, 1, -1, 1};

        for (int dir = 0; dir < 4; dir++) {
            for (int step = 1; step < 8; step++) {
                Position newPos = new Position(
                    from.getRow() + dRow[dir] * step,
                    from.getColumn() + dCol[dir] * step
                );

                if (!newPos.isValid()) break;

                Piece targetPiece = board.getPiece(newPos);
                if (targetPiece == null) {
                    moves.add(newPos);
                } else {
                    if (targetPiece.getColor() != getColor()) {
                        moves.add(newPos);
                    }
                    break;
                }
            }
        }

        return moves;
    }

    @Override
    public String getSymbol() {
        return getColor() == Color.WHITE ? "♗" : "♝";
    }
}

/**
 * KNIGHT: Moves in L-shape, can jump over pieces
 */
class Knight extends Piece {

    public Knight(Color color) {
        super(color, PieceType.KNIGHT);
    }

    @Override
    public boolean canMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getColumn() - from.getColumn());

        // L-shape: (2,1) or (1,2)
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    @Override
    public List<Position> getPossibleMoves(Position from, Board board) {
        List<Position> moves = new ArrayList<>();

        // All 8 possible L-shaped moves
        int[] dRow = {-2, -2, -1, -1, 1, 1, 2, 2};
        int[] dCol = {-1, 1, -2, 2, -2, 2, -1, 1};

        for (int i = 0; i < 8; i++) {
            Position newPos = new Position(
                from.getRow() + dRow[i],
                from.getColumn() + dCol[i]
            );

            if (newPos.isValid()) {
                Piece targetPiece = board.getPiece(newPos);
                if (targetPiece == null || targetPiece.getColor() != getColor()) {
                    moves.add(newPos);
                }
            }
        }

        return moves;
    }

    @Override
    public String getSymbol() {
        return getColor() == Color.WHITE ? "♘" : "♞";
    }
}

/**
 * PAWN: Moves forward, captures diagonally
 * Most complex piece due to special rules:
 * - Two square advance on first move
 * - Diagonal capture
 * - En passant
 * - Promotion
 */
class Pawn extends Piece {

    public Pawn(Color color) {
        super(color, PieceType.PAWN);
    }

    @Override
    public boolean canMove(Position from, Position to, Board board) {
        // Pawns move differently based on color
        int direction = getColor() == Color.WHITE ? 1 : -1;  // White up, Black down
        int rowDiff = to.getRow() - from.getRow();
        int colDiff = Math.abs(to.getColumn() - from.getColumn());

        // Move forward 1 square
        if (rowDiff == direction && colDiff == 0) {
            return board.getPiece(to) == null;  // Must be empty
        }

        // Move forward 2 squares (first move only)
        if (rowDiff == 2 * direction && colDiff == 0 && !hasMoved()) {
            Position intermediate = new Position(
                from.getRow() + direction,
                from.getColumn()
            );
            return board.getPiece(to) == null &&
                   board.getPiece(intermediate) == null;  // Path must be clear
        }

        // Capture diagonally
        if (rowDiff == direction && colDiff == 1) {
            Piece targetPiece = board.getPiece(to);
            // Normal capture OR en passant (validated separately)
            return targetPiece != null && targetPiece.getColor() != getColor();
        }

        return false;
    }

    @Override
    public List<Position> getPossibleMoves(Position from, Board board) {
        List<Position> moves = new ArrayList<>();
        int direction = getColor() == Color.WHITE ? 1 : -1;

        // Forward 1 square
        Position forward1 = new Position(from.getRow() + direction, from.getColumn());
        if (forward1.isValid() && board.getPiece(forward1) == null) {
            moves.add(forward1);

            // Forward 2 squares (first move only)
            if (!hasMoved()) {
                Position forward2 = new Position(from.getRow() + 2 * direction, from.getColumn());
                if (forward2.isValid() && board.getPiece(forward2) == null) {
                    moves.add(forward2);
                }
            }
        }

        // Diagonal captures
        int[] captureCols = {from.getColumn() - 1, from.getColumn() + 1};
        for (int col : captureCols) {
            Position capturePos = new Position(from.getRow() + direction, col);
            if (capturePos.isValid()) {
                Piece targetPiece = board.getPiece(capturePos);
                if (targetPiece != null && targetPiece.getColor() != getColor()) {
                    moves.add(capturePos);
                }
            }
        }

        return moves;
    }

    @Override
    public String getSymbol() {
        return getColor() == Color.WHITE ? "♙" : "♟";
    }
}

// ============================================================================
// BOARD CLASS
// ============================================================================

/**
 * Represents the 8x8 chess board
 * SOLID: Single Responsibility - manages board state only
 */
class Board {
    private Piece[][] cells;           // 8x8 grid
    private static final int SIZE = 8;

    public Board() {
        cells = new Piece[SIZE][SIZE];
    }

    /**
     * Initialize board with starting position
     */
    public void initializeBoard() {
        // Place white pieces (row 0-1)
        cells[0][0] = new Rook(Color.WHITE);
        cells[0][1] = new Knight(Color.WHITE);
        cells[0][2] = new Bishop(Color.WHITE);
        cells[0][3] = new Queen(Color.WHITE);
        cells[0][4] = new King(Color.WHITE);
        cells[0][5] = new Bishop(Color.WHITE);
        cells[0][6] = new Knight(Color.WHITE);
        cells[0][7] = new Rook(Color.WHITE);

        for (int col = 0; col < SIZE; col++) {
            cells[1][col] = new Pawn(Color.WHITE);
        }

        // Place black pieces (row 6-7)
        for (int col = 0; col < SIZE; col++) {
            cells[6][col] = new Pawn(Color.BLACK);
        }

        cells[7][0] = new Rook(Color.BLACK);
        cells[7][1] = new Knight(Color.BLACK);
        cells[7][2] = new Bishop(Color.BLACK);
        cells[7][3] = new Queen(Color.BLACK);
        cells[7][4] = new King(Color.BLACK);
        cells[7][5] = new Bishop(Color.BLACK);
        cells[7][6] = new Knight(Color.BLACK);
        cells[7][7] = new Rook(Color.BLACK);
    }

    /**
     * Get piece at position
     */
    public Piece getPiece(Position position) {
        if (!isPositionValid(position)) {
            return null;
        }
        return cells[position.getRow()][position.getColumn()];
    }

    /**
     * Set piece at position
     */
    public void setPiece(Position position, Piece piece) {
        if (!isPositionValid(position)) {
            throw new IllegalArgumentException("Invalid position: " + position);
        }
        cells[position.getRow()][position.getColumn()] = piece;
    }

    /**
     * Remove piece from position
     */
    public void removePiece(Position position) {
        setPiece(position, null);
    }

    /**
     * Move piece from one position to another
     * Returns captured piece (if any)
     */
    public Piece movePiece(Position from, Position to) {
        Piece piece = getPiece(from);
        Piece capturedPiece = getPiece(to);

        setPiece(to, piece);
        removePiece(from);

        return capturedPiece;
    }

    /**
     * Check if position is valid
     */
    public boolean isPositionValid(Position position) {
        return position != null && position.isValid();
    }

    /**
     * Find king position for a color
     */
    public Position findKing(Color color) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece piece = cells[row][col];
                if (piece != null &&
                    piece.getType() == PieceType.KING &&
                    piece.getColor() == color) {
                    return new Position(row, col);
                }
            }
        }
        return null;
    }

    /**
     * Display board (simple text representation)
     */
    public void display() {
        System.out.println("  a b c d e f g h");
        for (int row = 7; row >= 0; row--) {
            System.out.print((row + 1) + " ");
            for (int col = 0; col < SIZE; col++) {
                Piece piece = cells[row][col];
                System.out.print(piece == null ? "." : piece.getSymbol());
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}

// ============================================================================
// MOVE CLASS
// ============================================================================

/**
 * Represents a chess move
 * DESIGN PATTERN: Command Pattern
 * Encapsulates move information for history, undo/redo
 */
class Move {
    private final Position from;
    private final Position to;
    private final Piece piece;
    private Piece capturedPiece;
    private MoveType moveType;

    public Move(Position from, Position to, Piece piece) {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.moveType = MoveType.NORMAL;
    }

    // Getters
    public Position getFrom() { return from; }
    public Position getTo() { return to; }
    public Piece getPiece() { return piece; }
    public Piece getCapturedPiece() { return capturedPiece; }
    public MoveType getMoveType() { return moveType; }

    // Setters
    public void setCapturedPiece(Piece piece) {
        this.capturedPiece = piece;
        if (piece != null) {
            this.moveType = MoveType.CAPTURE;
        }
    }

    public void setMoveType(MoveType type) {
        this.moveType = type;
    }

    @Override
    public String toString() {
        String notation = from.toString() + " → " + to.toString();
        if (capturedPiece != null) {
            notation += " (captures " + capturedPiece.getType() + ")";
        }
        return notation;
    }
}

// ============================================================================
// VALIDATOR CLASSES
// ============================================================================

/**
 * DESIGN PATTERN: Strategy Pattern
 * Interface for move validation strategies
 * SOLID: Dependency Inversion - depend on abstraction
 */
interface MoveValidator {
    boolean validate(Move move, Board board) throws InvalidMoveException;
}

/**
 * Basic move validator
 * Validates fundamental chess rules
 */
class BasicMoveValidator implements MoveValidator {

    @Override
    public boolean validate(Move move, Board board) throws InvalidMoveException {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece piece = move.getPiece();

        // 1. Check positions are valid
        if (!board.isPositionValid(from) || !board.isPositionValid(to)) {
            throw new InvalidPositionException("Position out of bounds");
        }

        // 2. Check source has a piece
        Piece sourcePiece = board.getPiece(from);
        if (sourcePiece == null) {
            throw new InvalidMoveException("No piece at " + from);
        }

        // 3. Check destination is different
        if (from.equals(to)) {
            throw new InvalidMoveException("Source and destination are the same");
        }

        // 4. Check not capturing own piece
        Piece destinationPiece = board.getPiece(to);
        if (destinationPiece != null &&
            destinationPiece.getColor() == piece.getColor()) {
            throw new InvalidMoveException("Cannot capture your own piece");
        }

        // 5. Check piece-specific movement rules
        if (!piece.canMove(from, to, board)) {
            throw new InvalidMoveException(
                piece.getType() + " cannot move from " + from + " to " + to
            );
        }

        // 6. Check path is clear (except Knight)
        if (piece.getType() != PieceType.KNIGHT) {
            if (!isPathClear(from, to, board)) {
                throw new InvalidMoveException("Path is blocked");
            }
        }

        return true;
    }

    /**
     * Check if path between positions is clear
     */
    private boolean isPathClear(Position from, Position to, Board board) {
        int rowStep = Integer.compare(to.getRow() - from.getRow(), 0);
        int colStep = Integer.compare(to.getColumn() - from.getColumn(), 0);

        int currentRow = from.getRow() + rowStep;
        int currentCol = from.getColumn() + colStep;

        // Check all intermediate squares
        while (currentRow != to.getRow() || currentCol != to.getColumn()) {
            if (board.getPiece(new Position(currentRow, currentCol)) != null) {
                return false;  // Path blocked
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return true;  // Path clear
    }
}

// ============================================================================
// PLAYER AND HISTORY CLASSES
// ============================================================================

/**
 * Represents a chess player
 */
class Player {
    private final String name;
    private final Color color;
    private List<Piece> capturedPieces;

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.capturedPieces = new ArrayList<>();
    }

    public String getName() { return name; }
    public Color getColor() { return color; }

    public void addCapturedPiece(Piece piece) {
        capturedPieces.add(piece);
    }

    @Override
    public String toString() {
        return name + " (" + color + ")";
    }
}

/**
 * Tracks move history
 */
class MoveHistory {
    private List<Move> moves = new ArrayList<>();

    public void addMove(Move move) {
        moves.add(move);
    }

    public Move getLastMove() {
        return moves.isEmpty() ? null : moves.get(moves.size() - 1);
    }

    public List<Move> getAllMoves() {
        return new ArrayList<>(moves);
    }

    public int getMoveCount() {
        return moves.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Move History:\n");
        for (int i = 0; i < moves.size(); i++) {
            sb.append((i + 1)).append(". ").append(moves.get(i)).append("\n");
        }
        return sb.toString();
    }
}

// ============================================================================
// MAIN CHESS GAME CLASS
// ============================================================================

/**
 * Main Chess Game orchestrator
 * DESIGN PATTERN: Facade Pattern
 * SOLID: Single Responsibility - coordinates game flow
 */
class ChessGame {
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private Player currentPlayer;
    private GameState gameState;
    private MoveHistory moveHistory;
    private MoveValidator moveValidator;

    /**
     * Constructor
     */
    public ChessGame() {
        this.board = new Board();
        this.whitePlayer = new Player("White", Color.WHITE);
        this.blackPlayer = new Player("Black", Color.BLACK);
        this.currentPlayer = whitePlayer;  // White starts
        this.gameState = GameState.NOT_STARTED;
        this.moveHistory = new MoveHistory();
        this.moveValidator = new BasicMoveValidator();
    }

    /**
     * Initialize the game
     */
    public void initializeGame() {
        board.initializeBoard();
        gameState = GameState.IN_PROGRESS;
        System.out.println("♟️ Chess game started!");
        System.out.println(currentPlayer.getName() + "'s turn\n");
        board.display();
    }

    /**
     * Make a move
     */
    public Move makeMove(Position from, Position to) throws InvalidMoveException {
        // Check if game is active
        if (isGameOver()) {
            throw new InvalidMoveException("Game is over! State: " + gameState);
        }

        // Get piece at source
        Piece piece = board.getPiece(from);
        if (piece == null || piece.getColor() != currentPlayer.getColor()) {
            throw new InvalidMoveException(
                "No piece or wrong color at " + from
            );
        }

        // Create move object
        Move move = new Move(from, to, piece);

        // Validate move
        moveValidator.validate(move, board);

        // Execute move
        executeMove(move);

        // Add to history
        moveHistory.addMove(move);

        // Switch turn
        switchTurn();

        // Display board
        System.out.println("\nMove: " + move);
        board.display();
        System.out.println();

        return move;
    }

    /**
     * Execute a validated move
     */
    private void executeMove(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();

        // Check for capture
        Piece capturedPiece = board.getPiece(to);
        if (capturedPiece != null) {
            move.setCapturedPiece(capturedPiece);
            currentPlayer.addCapturedPiece(capturedPiece);
            System.out.println("Captured: " + capturedPiece);
        }

        // Move piece
        board.movePiece(from, to);

        // Mark as moved
        move.getPiece().setMoved(true);
    }

    /**
     * Switch turn to other player
     */
    private void switchTurn() {
        currentPlayer = (currentPlayer == whitePlayer) ? blackPlayer : whitePlayer;
        System.out.println(currentPlayer.getName() + "'s turn");
    }

    /**
     * Check if game is over
     */
    public boolean isGameOver() {
        return gameState == GameState.CHECKMATE ||
               gameState == GameState.STALEMATE ||
               gameState == GameState.DRAW ||
               gameState == GameState.RESIGNED;
    }

    // Getters
    public Board getBoard() { return board; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public GameState getGameState() { return gameState; }
    public MoveHistory getMoveHistory() { return moveHistory; }
}

// ============================================================================
// MAIN METHOD - DEMO
// ============================================================================

public class ChessGameDemo {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("♟️  CHESS GAME - LOW LEVEL DESIGN DEMO  ♟️");
        System.out.println("=".repeat(60));
        System.out.println();

        try {
            // Create and initialize game
            ChessGame game = new ChessGame();
            game.initializeGame();

            System.out.println("\n--- Playing some moves ---\n");

            // White's turn: e2 → e4
            game.makeMove(new Position(1, 4), new Position(3, 4));

            // Black's turn: e7 → e5
            game.makeMove(new Position(6, 4), new Position(4, 4));

            // White's turn: Nf3
            game.makeMove(new Position(0, 6), new Position(2, 5));

            // Black's turn: Nc6
            game.makeMove(new Position(7, 1), new Position(5, 2));

            // Display move history
            System.out.println("\n" + game.getMoveHistory());

        } catch (InvalidMoveException e) {
            System.err.println("❌ Invalid move: " + e.getMessage());
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ Demo completed successfully!");
        System.out.println("=".repeat(60));
    }
}
