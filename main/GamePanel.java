package main;

import multiplayer.GameClient;
import multiplayer.GameServer;
import piece.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static main.Constants.MARGIN_X;
import static main.Constants.MARGIN_Y;

public class GamePanel extends JPanel implements Runnable {
    // CONSTANTS
    private static final int FPS = 60;
    public static final int WHITE = 1;
    public static final int BLACK = 0;

    // GAME STATE VARIABLES
    private Thread gameThread;
    private boolean gameOver, stalemate, draw, promotion, isAi, isMultiplayer;
    private static int fiftyMoveCounter = 0;
    public static int currentColor = WHITE;
    public static int opponentColor;

    // GAME OBJECTS
    private final Board board = new Board();
    private final Mouse mouse;
    private AI ai;
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    private final ArrayList<Piece> promoPieces = new ArrayList<>();
    private final HashMap<String, Integer> boardStates = new HashMap<>();
    private Piece activePiece, checkingPiece;
    public static Piece castlingPiece;
    private Object connection;

    // BOOLEANS
    boolean canMove;
    boolean validSquare;

    // CONSTRUCTOR
    public GamePanel(Main parentWindow, GameType selectedGameType, Object connection) {
        this.connection = connection;

        this.mouse = new Mouse(parentWindow, this);

        // Graphics and game state
        initializeUI(parentWindow);
        initializeGameState();

        // Different Multiplayer Game Modes
        if (selectedGameType == GameType.MULTIPLAYER_AS_CLIENT) {
            opponentColor = WHITE;
            isMultiplayer = true;
            this.isAi = false;
        } else if (selectedGameType == GameType.MULTIPLAYER_AS_HOST_WHITE) {
            opponentColor = BLACK;
            isMultiplayer = true;
            this.isAi = false;
        } else throw new IllegalArgumentException();
    }

    public GamePanel(Main parentWindow, GameType selectedGameType) {
        this.mouse = new Mouse(parentWindow, this);

        // Graphics and game state
        initializeUI(parentWindow);
        initializeGameState();

        // Different Game Modes
        if (selectedGameType == GameType.AGAINST_AI_AS_WHITE) {
            this.isAi = true;
            this.ai = new AI(pieces);
            opponentColor = BLACK;
        } else if (selectedGameType == GameType.AGAINST_AI_AS_BLACK) {
            this.isAi = true;
            this.ai = new AI(pieces);
            opponentColor = BLACK;
        } else {
            this.isAi = false;
            opponentColor = BLACK;
        }
    }


    // INITIALIZATION
    private void initializeUI(Main parentWindow) {
        setLayout(new BorderLayout());
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        Utils.createMenuButton(parentWindow, this);
    }

    private void initializeGameState() {
        setPieces();
        copyPieces(pieces, simPieces);
        currentColor = WHITE;
        fiftyMoveCounter = 0;
    }

    public void launchGame() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    private void setPieces() {
        pieces.clear();
        // Add white pieces
        addPieces(WHITE, 6, 7);
        // Add black pieces
        addPieces(BLACK, 1, 0);
    }

    private void addPieces(int color, int pawnRow, int backRow) {
        for (int i = 0; i < 8; i++) {
            pieces.add(new Pawn(color, i, pawnRow));
        }
        pieces.add(new Rook(color, 0, backRow));
        pieces.add(new Rook(color, 7, backRow));
        pieces.add(new Knight(color, 1, backRow));
        pieces.add(new Knight(color, 6, backRow));
        pieces.add(new Bishop(color, 2, backRow));
        pieces.add(new Bishop(color, 5, backRow));
        pieces.add(new Queen(color, 3, backRow));
        pieces.add(new King(color, 4, backRow));
    }

    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
        target.clear();
        target.addAll(source);
    }

    // GAME LOOP
    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        long lastTime = System.nanoTime();
        double delta = 0;

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }

    }

    private void update() {
        if (promotion) {
            promoting();
        } else if (!gameOver && !stalemate) {
            if (currentColor == opponentColor && isAi) {
                executeAIMove();
            } else if (currentColor == opponentColor && isMultiplayer) {
                executeMultiplayerMove();
            } else {
                handlePlayerMove();
            }
        }
    }

    // EVENT HANDLING
    private void executeAIMove() {
        int[] move = ai.getNextMove(opponentColor);
        executeMultiplayerOpponentMove(move);
    }

    private void executeMultiplayerOpponentMove(int[] move) {
        if (move != null) {
            Piece piece = getPieceAt(move[0], move[1]);
            if (piece != null) {
                Piece targetPiece = getPieceAt(move[2], move[3]);

                // Remove the captured piece
                if (targetPiece != null && targetPiece.color != opponentColor) {
                    pieces.remove(targetPiece);
                }

                // Move the AI's piece
                piece.col = move[2];
                piece.row = move[3];
                piece.updatePosition();

                // Check for promotion
                if (piece.type == Type.PAWN && (piece.row == 0 || piece.row == 7)) {
                    promoteAIPawn(piece);
                } else {
                    piece.updatePosition();
                }

                // Switch the turn to the player
                changePlayer();
            }
        } else {
            gameOver = true; // Stalemate or checkmate
        }
    }

    private void executeMultiplayerMove() {
        // Wait for the opponent's move
        String opponentMove;
        if (connection instanceof GameServer) {
            opponentMove = ((GameServer) connection).receiveMessage();
        } else {
            opponentMove = ((GameClient) connection).receiveMessage();
        }

        executeMultiplayerOpponentMove(parseCoordinates(opponentMove));
    }

    private void handlePlayerMove() {
        if (mouse.pressed) {
            if (activePiece == null) {
                // If the activePiece is null, check if you can pick up a piece
                pickPiece();
            } else {
                // If the player is holding a piece, simulate the move
                simulateMove();
            }
        } else if (activePiece != null) { // Mouse button released
            processMove();
        }
    }

    private void pickPiece() {
        int col = (mouse.x - MARGIN_X) / Board.SQUARE_SIZE;
        int row = (mouse.y - MARGIN_Y) / Board.SQUARE_SIZE;
        for (Piece piece : pieces) {
            if (piece.color == currentColor && piece.col == col && piece.row == row) {
                activePiece = piece;
                return;
            }
        }
    }

    private void simulateMove() {
        canMove = false;
        validSquare = false;

        // Reset the piece list in every loop
        // This is basically for restoring the removed piece during the simulation
        copyPieces(pieces, simPieces);

        // Reset the castling piece's position
        if (castlingPiece != null) {
            castlingPiece.col = castlingPiece.preCol;
            castlingPiece.x = MARGIN_X + castlingPiece.getX(castlingPiece.col);
            castlingPiece = null;
        }

        // If a piece is being held, update its position
        activePiece.x = mouse.x - Board.HALF_SQUARE_SIZE - MARGIN_X;
        activePiece.y = mouse.y - Board.HALF_SQUARE_SIZE - MARGIN_Y;
        activePiece.col = activePiece.getCol(activePiece.x);
        activePiece.row = activePiece.getRow(activePiece.y);

        // Check if the piece is hovering over a reachable square
        if (activePiece.canMove(activePiece.col, activePiece.row)) {
            canMove = true;

            // If hitting a piece, remove it from the list
            if (activePiece.hittingPiece != null) {
                simPieces.remove(activePiece.hittingPiece.getIndex());
            }

            checkCastling();

            if (!isIllegal(activePiece) && !opponentCanCaptureKing()) {
                validSquare = true;
            }
        }
    }

    private void processMove() {
        if (validSquare) {
            // MOVE CONFIRMED

            finalizeMove();
        } else {
            // The move is not valid so reset everything
            resetMove();

        }
    }

    private void sendMoveInformation() {
        String move = activePiece.getMoveString(); // Get input from the UI
        if (connection instanceof GameServer) {
            ((GameServer) connection).sendMessage(move);
        } else {
            ((GameClient) connection).sendMessage(move);
        }
    }

    private void finalizeMove() {
        // TODO: probably wrong position
        if (isMultiplayer) {
            sendMoveInformation();
        }

        // Update the piece list in case a piece has been captured and removed during the simulation
        copyPieces(simPieces, pieces);
        activePiece.updatePosition();

        if (castlingPiece != null) {
            castlingPiece.updatePosition();
        }

        if (!isEndOfGame()) {
            if (canPromote()) {
                promotion = true;
            } else {
                changePlayer();
            }
        }
    }

    private void resetMove() {
        copyPieces(pieces, simPieces);
        activePiece.resetPosition();
        activePiece = null;
    }

    private void changePlayer() {
        fiftyMoveCounter++;

        if (currentColor == WHITE) {
            currentColor = BLACK;
            for (Piece piece : pieces) {
                if (piece.color == BLACK) {
                    piece.twoStepped = false; // Reset two-stepped pawns
                }
            }
        } else {
            currentColor = WHITE;
            for (Piece piece : pieces) {
                if (piece.color == WHITE) {
                    piece.twoStepped = false; // Reset two-stepped pawns
                }
            }
        }
        activePiece = null;
    }

    private void promoting() {
        if (mouse.pressed) {
            for (Piece piece : promoPieces) {
                if (piece.col == mouse.x / Board.SQUARE_SIZE && piece.row == mouse.y / Board.SQUARE_SIZE) {
                    switch (piece.type) {
                        case ROOK:
                            simPieces.add(new Rook(currentColor, activePiece.col, activePiece.row));
                            break;
                        case KNIGHT:
                            simPieces.add(new Knight(currentColor, activePiece.col, activePiece.row));
                            break;
                        case BISHOP:
                            simPieces.add(new Bishop(currentColor, activePiece.col, activePiece.row));
                            break;
                        case QUEEN:
                            simPieces.add(new Queen(currentColor, activePiece.col, activePiece.row));
                            break;
                        default:
                            break;
                    }
                    simPieces.remove(activePiece.getIndex());
                    copyPieces(simPieces, pieces);
                    activePiece = null;
                    promotion = false;
                    isKingInCheck();
                    changePlayer();
                }
            }
        }
    }

    // RENDERING
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        drawBoard(g2d);
        drawPieces(g2d);
        drawStatus(g2d);
    }

    private void drawBoard(Graphics2D g2d) {
        board.draw(g2d);
    }

    private void drawPieces(Graphics2D g2d) {
        for (Piece piece : pieces) {
            piece.draw(g2d);
        }
    }

    private void drawStatus(Graphics2D g2d) {
        g2d.setFont(Utils.deriveFont(40, Font.PLAIN));
        g2d.setColor(Color.white);
        //Print the vertical numbers
        for (int i = 8; i >= 1; i--) {
            g2d.drawString(String.valueOf(i), MARGIN_X / 2 - 10, 15 + Board.SQUARE_SIZE / 2 + MARGIN_Y + Board.SQUARE_SIZE * (8 - i));
        }
        //Print the horizontal letters
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H"};
        for (int i = 0; i < 8; i++) {
            g2d.drawString(letters[i], Board.SQUARE_SIZE / 2 - 15 + MARGIN_X + Board.SQUARE_SIZE * i, MARGIN_Y - 15);
        }

        if (activePiece != null) {
            int x = MARGIN_X + activePiece.col * Board.SQUARE_SIZE;
            int y = MARGIN_Y + activePiece.row * Board.SQUARE_SIZE;
            if (canMove) {
                if (isIllegal(activePiece) || opponentCanCaptureKing()) {
                    g2d.setColor(Color.red);
                } else {
                    g2d.setColor(Color.black);
                }
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2d.fillRect(x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }

            // Draw the active piece in the end so it won't be hidden by the board or the colored square
            activePiece.draw(g2d);
        }

        if (gameOver) {
            String s;
            if (currentColor == WHITE) {
                s = "White wins";
            } else {
                s = "Black wins";
            }
            g2d.setFont(Utils.deriveFont(90, Font.PLAIN));
            g2d.fillRoundRect(180, 330, currentColor == WHITE ? 475 : 450, 100, 20, 20);
            g2d.setColor(Color.RED);
            g2d.drawString(s, 200, 410);
        } else {

            // Status messages
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(Utils.deriveFont(40, Font.PLAIN));
            g2d.setColor(Color.WHITE);

            if (promotion) {
                g2d.drawString("Promote to:", 900, 150);
                for (Piece piece : promoPieces) {
                    g2d.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
                }
            } else {
                if (currentColor == WHITE) {
                    g2d.drawString("White's turn", 900, 550);
                    if (checkingPiece != null && checkingPiece.color == BLACK) {
                        g2d.setColor(Color.red);
                        g2d.drawString("The King", 900, 650);
                        g2d.drawString("is in check!", 900, 700);
                    }
                } else {
                    g2d.drawString("Black's turn", 900, 250);
                    if (checkingPiece != null && checkingPiece.color == WHITE) {
                        g2d.setColor(Color.red);
                        g2d.drawString("The King", 900, 100);
                        g2d.drawString("is in check!", 900, 150);
                    }
                }
            }
        }

        if (stalemate) {
            g2d.setFont(Utils.deriveFont(90, Font.PLAIN));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("Stalemate", 200, 420);
        }
        if (draw) {
            g2d.setFont(Utils.deriveFont(90, Font.PLAIN));
            g2d.setColor(Color.LIGHT_GRAY);
            if (fiftyMoveCounter == 50) {
                g2d.drawString(" 50 Move Draw", 80, 420);

            } else {
                g2d.drawString("Draw", 250, 420);

            }
        }
    }

    // HELPER METHODS
    // State Management
    public boolean isEndOfGame() {
        if (isCheckmate()) {
            gameOver = true;
            return true;
        } else if (isStalemate() && !isKingInCheck()) {
            stalemate = true;
            return true;
        } else if (isFiftyMovesDraw(activePiece) || isRepetitionDraw() || isDeadPosition()) {
            draw = true;
            return true;
        }
        return false;
    }

    private boolean isCheckmate() {
        Piece king = getKing(true);

        return isKingInCheck() && kingCanNotMove(king) && !canBlockCheck(Objects.requireNonNull(king));
    }

    private boolean isStalemate() {
        return kingCanNotMove(getKing(true)) && !anyPieceCanMove();
    }

    private boolean isFiftyMovesDraw(Piece activePiece) {
        // TODO: ai?
        // reset the 50 moves counter when pawn moves or other piece is taken
        if (activePiece.type == main.Type.PAWN || activePiece.hittingPiece != null) {
            fiftyMoveCounter = -1;
        }

        // a draw if no capture has been made and no pawn has been moved in the last fifty moves
        return fiftyMoveCounter == 50;
    }

    private boolean isRepetitionDraw() {
        String state = getBoardState();
        boardStates.put(state, boardStates.getOrDefault(state, 0) + 1);
        return boardStates.get(state) >= 3;
    }

    private boolean isDeadPosition() {
        int whiteMaterial = 0;
        int blackMaterial = 0;

        boolean whiteHasBishop = false;
        boolean blackHasBishop = false;

        int whiteKnights = 0;
        int blackKnights = 0;

        for (Piece piece : pieces) {
            if (piece.type == main.Type.PAWN) {
                return false; // Pawns mean there is still potential for checkmate
            }

            if (piece.type == main.Type.QUEEN || piece.type == main.Type.ROOK) {
                return false; // Queens and rooks can checkmate
            }

            if (piece.type == main.Type.BISHOP) {
                if (piece.color == WHITE) {
                    whiteMaterial++;
                    whiteHasBishop = true;
                } else {
                    blackMaterial++;
                    blackHasBishop = true;
                }
            }

            if (piece.type == main.Type.KNIGHT) {
                if (piece.color == WHITE) {
                    whiteMaterial++;
                    whiteKnights++;
                } else {
                    blackMaterial++;
                    blackKnights++;
                }
            }
        }

        // King vs King
        if (pieces.size() == 2) {
            return true;
        }

        // King vs King and Bishop
        if (pieces.size() == 3 && (whiteHasBishop || blackHasBishop)) {
            return true;
        }

        // King vs King and Knight
        if (pieces.size() == 3 && (whiteMaterial == 1 || blackMaterial == 1)) {
            return true;
        }

        // King and Bishop vs King and Bishop (both bishops on the same color)
        if (pieces.size() == 4 && whiteHasBishop && blackHasBishop) {
            return bishopsOnSameColor();
        }

        // King and 2 Knights vs King (or King vs King and 2 Knights)
        return pieces.size() == 4 && (whiteKnights == 2 && blackMaterial == 0 || blackKnights == 2 && whiteMaterial == 0);
    }

    private boolean bishopsOnSameColor() {
        boolean whiteBishopOnWhiteSquare = false;
        boolean blackBishopOnWhiteSquare = false;

        for (Piece piece : pieces) {
            if (piece.type == main.Type.BISHOP) {
                int squareColor = (piece.col + piece.row) % 2;
                if (piece.color == WHITE) {
                    whiteBishopOnWhiteSquare = (squareColor == 0);
                } else {
                    blackBishopOnWhiteSquare = (squareColor == 0);
                }
            }
        }

        return whiteBishopOnWhiteSquare == blackBishopOnWhiteSquare;
    }

    private boolean isKingInCheck() {
        Piece king = getKing(true);

        for (Piece piece : simPieces) {
            if (piece.color != Objects.requireNonNull(king).color && piece.canMove(king.col, king.row)) {
                checkingPiece = piece;
                return true;
            }
        }
        checkingPiece = null;
        return false;
    }

    private boolean kingCanNotMove(Piece king) {
        return isInvalidKingMove(king, -1, -1) && isInvalidKingMove(king, 0, -1) &&
                isInvalidKingMove(king, 1, -1) && isInvalidKingMove(king, -1, 0) &&
                isInvalidKingMove(king, 1, 0) && isInvalidKingMove(king, -1, 1) &&
                isInvalidKingMove(king, 0, 1) && isInvalidKingMove(king, 1, 1);
    }

    private boolean isInvalidKingMove(Piece king, int colPlus, int rowPlus) {
        boolean isValid = false;

        // Temporarily move the king
        king.col += colPlus;
        king.row += rowPlus;

        if (king.canMove(king.col, king.row)) {
            if (king.hittingPiece != null) {
                simPieces.remove(king.hittingPiece.getIndex());
            }
            if (!isIllegal(king)) {
                isValid = true;
            }
        }

        // Undo the move
        king.resetPosition();
        copyPieces(pieces, simPieces);

        return !isValid;
    }

    private boolean anyPieceCanMove() {
        // todo: ChatGPT generated

        for (Piece piece : simPieces) {
            if (piece.color == currentColor) {
                // Iterate through all possible squares on the board
                for (int col = 0; col < Board.SQUARE_SIZE; col++) {
                    for (int row = 0; row < Board.SQUARE_SIZE; row++) {
                        // Check if the piece can legally move to the target square
                        if (piece.canMove(col, row)) {
                            // Simulate the move and check if it doesn't result in an illegal state
                            int originalCol = piece.col;
                            int originalRow = piece.row;
                            Piece capturedPiece = getPieceAt(col, row);

                            // Simulate the move
                            piece.col = col;
                            piece.row = row;
                            if (capturedPiece != null) {
                                simPieces.remove(capturedPiece);
                            }

                            boolean isIllegalMove = isIllegal(Objects.requireNonNull(getKing(false)));

                            // Undo the move
                            piece.col = originalCol;
                            piece.row = originalRow;
                            if (capturedPiece != null) {
                                simPieces.add(capturedPiece);
                            }

                            if (!isIllegalMove) {
                                return true; // Found a valid move
                            }
                        }
                    }
                }
            }
        }
        return false; // No valid moves found
    }

    private boolean isIllegal(Piece king) {
        if (king.type == main.Type.KING) {
            for (Piece piece : simPieces) {
                if (piece != king && piece.color != king.color && piece.canMove(king.col, king.row)) {
                    return true; // King is in check
                }
            }
        }
        return false;
    }

    private boolean opponentCanCaptureKing() {
        Piece king = getKing(false);
        for (Piece piece : simPieces) {
            assert king != null;
            if (piece.color != king.color && piece.canMove(king.col, king.row)) {
                return true;
            }
        }
        return false;
    }

    private boolean canBlockCheck(Piece king) {
        // Check the position of the checking piece and the king in check
        int colDiff = Math.abs(checkingPiece.col - king.col);
        int rowDiff = Math.abs(checkingPiece.row - king.row);

        if (colDiff == 0) {
            // The checking piece is attacking vertically
            if (checkingPiece.row < king.row) {
                // The checking piece is above the king
                for (int row = checkingPiece.row; row < king.row + rowDiff; row++) {
                    for (Piece piece : simPieces) {
                        if (piece != king && piece.color != currentColor && piece.canMove(checkingPiece.col, row)) {
                            return true;
                        }
                    }
                }
            }
            if (checkingPiece.col < king.col) {
                // The checking piece is below the king
                for (int row = checkingPiece.row; row > king.row + rowDiff; row--) {
                    for (Piece piece : simPieces) {
                        if (piece != king && piece.color != currentColor && piece.canMove(checkingPiece.col, row)) {
                            return true;
                        }
                    }
                }
            }
        } else if (rowDiff == 0) {
            // The checking piece is attacking horizontally
            if (checkingPiece.col < king.col) {
                // The checking piece is to the left
                for (int col = checkingPiece.col; col < king.col + rowDiff; col++) {
                    for (Piece piece : simPieces) {
                        if (piece != king && piece.color != currentColor && piece.canMove(col, checkingPiece.row)) {
                            return true;
                        }
                    }
                }
            }
            if (checkingPiece.col > king.col) {
                // The checking piece is to the right
                for (int col = checkingPiece.col; col > king.col + rowDiff; col--) {
                    for (Piece piece : simPieces) {
                        if (piece != king && piece.color != currentColor && piece.canMove(col, checkingPiece.row)) {
                            return true;
                        }
                    }
                }
            }
        } else if (colDiff == rowDiff) {
            // The checking piece is attacking diagonally
            if (checkingPiece.row < king.row) {
                // The checking piece is above the King
                if (checkingPiece.col < king.col) {
                    // The checking piece is in the upper left
                    for (int col = checkingPiece.col, row = checkingPiece.row; col < king.col; col++, row++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                return true;
                            }
                        }
                    }
                }
                if (checkingPiece.col > king.col) {
                    // The checking piece is in the upper right
                    for (int col = checkingPiece.col, row = checkingPiece.row; col > king.col; col--, row++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                return true;
                            }
                        }
                    }
                }
            }
            if (checkingPiece.row > king.row) {
                // The checking piece is below the King
                if (checkingPiece.col < king.col) {
                    // The checking piece is in the lower left
                    for (int col = checkingPiece.col, row = checkingPiece.row; col < king.col; col++, row--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                return true;
                            }
                        }
                    }
                }
                if (checkingPiece.col > king.col) {
                    // The checking piece is in the lower right
                    for (int col = checkingPiece.col, row = checkingPiece.row; col > king.col; col--, row--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } else {
            // Knight attacking
            for (Piece piece : simPieces) {
                if (piece != king && piece.color != currentColor) {
                    if (piece.canMove(checkingPiece.col, checkingPiece.row)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Special Moves
    private void promoteAIPawn(Piece pawn) {
        // TODO: for now promote to Queen only
        pieces.remove(pawn);
        pieces.add(new Queen(opponentColor, pawn.col, pawn.row));
    }

    private boolean canPromote() {
        if (activePiece.type == main.Type.PAWN) {
            if (currentColor == WHITE && activePiece.row == 0 || currentColor == BLACK && activePiece.row == 7) {
                setPromoPieces();
                return true;
            }
        }

        return false;
    }

    private void setPromoPieces() {
        promoPieces.clear();
        promoPieces.add(new Rook(currentColor, 9, 2));
        promoPieces.add(new Knight(currentColor, 9, 3));
        promoPieces.add(new Bishop(currentColor, 9, 4));
        promoPieces.add(new Queen(currentColor, 9, 5));
    }

    private void checkCastling() {
        if (castlingPiece != null) {
            if (castlingPiece.col == 0) {
                castlingPiece.col += 3;
            } else if (castlingPiece.col == 7) {
                castlingPiece.col -= 2;
            }
            castlingPiece.x = castlingPiece.getX(castlingPiece.col);
        }
    }

    //Utility
    private Piece getPieceAt(int col, int row) {
        for (Piece piece : pieces) {
            if (piece.col == col && piece.row == row) {
                return piece;
            }
        }
        return null;
    }

    private Piece getKing(boolean opponent) {
        for (Piece piece : simPieces) {
            if (piece.type == Type.KING && piece.color == (opponent ? 1 - currentColor : currentColor)) {
                return piece;
            }
        }
        return null;
    }

    private String getBoardState() {
        StringBuilder state = new StringBuilder();

        for (Piece piece : pieces) {
            state.append(piece.type).append(piece.color)
                    .append(piece.col).append(piece.row).append(";");
        }

        state.append(currentColor).append(";");

        return state.toString();
    }

    // string → int[startCol, startRow, targetCol, targetRow]
    public static int[] parseCoordinates(String input) {
        // Remove the brackets and whitespace
        String cleanedInput = input.replaceAll("[\\[\\]\\s]", "");

        // Split the string by commas
        String[] parts = cleanedInput.split(",");

        // Convert to integers
        int[] coordinates = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            coordinates[i] = Integer.parseInt(parts[i]);
        }

        return coordinates;
    }
}