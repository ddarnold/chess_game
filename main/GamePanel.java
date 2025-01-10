package main;

import piece.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import static main.Constants.*;

public class GamePanel extends JPanel implements Runnable {
    Thread gameThread;
    Board board = new Board();
    Mouse mouse;

    private final HashMap<String, Integer> boardStates = new HashMap<>();


    final int FPS = 60;

    // COLOR
    public static final int WHITE = 1;
    public static final int BLACK = 0;
    public static int currentColor = WHITE;


    // PIECES
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promoPieces = new ArrayList<>();
    Piece activePiece, checkingPiece;
    public static Piece castlingPiece;

    // BOOLEANS
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver;
    boolean stalemate;
    boolean draw;
    boolean isAi;

    // COUNTERS
    static int fiftyMoveCounter = 0;

    public GamePanel(Main parentWindow, boolean isAi) {
        mouse = new Mouse(parentWindow, this);
        this.isAi = isAi;
        setLayout(new BorderLayout());
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        setPieces();
        copyPieces(pieces, simPieces);
        currentColor = WHITE;
        fiftyMoveCounter = 0;
        Utils.createMenuButton(parentWindow, this);
    }

    public void launchGame() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    public void setPieces() {
        pieces.clear();
        // Whites
        pieces.add(new Pawn(WHITE, 0, 6));
        pieces.add(new Pawn(WHITE, 1, 6));
        pieces.add(new Pawn(WHITE, 2, 6));
        pieces.add(new Pawn(WHITE, 3, 6));
        pieces.add(new Pawn(WHITE, 4, 6));
        pieces.add(new Pawn(WHITE, 5, 6));
        pieces.add(new Pawn(WHITE, 6, 6));
        pieces.add(new Pawn(WHITE, 7, 6));
        pieces.add(new Rook(WHITE, 0, 7));
        pieces.add(new Rook(WHITE, 7, 7));
        pieces.add(new Knight(WHITE, 1, 7));
        pieces.add(new Knight(WHITE, 6, 7));
        pieces.add(new Bishop(WHITE, 2, 7));
        pieces.add(new Bishop(WHITE, 5, 7));
        pieces.add(new King(WHITE, 4, 7));
        pieces.add(new Queen(WHITE, 3, 7));
        // Blacks
        pieces.add(new Pawn(BLACK, 0, 1));
        pieces.add(new Pawn(BLACK, 1, 1));
        pieces.add(new Pawn(BLACK, 2, 1));
        pieces.add(new Pawn(BLACK, 3, 1));
        pieces.add(new Pawn(BLACK, 4, 1));
        pieces.add(new Pawn(BLACK, 5, 1));
        pieces.add(new Pawn(BLACK, 6, 1));
        pieces.add(new Pawn(BLACK, 7, 1));
        pieces.add(new Rook(BLACK, 0, 0));
        pieces.add(new Rook(BLACK, 7, 0));
        pieces.add(new Knight(BLACK, 1, 0));
        pieces.add(new Knight(BLACK, 6, 0));
        pieces.add(new Bishop(BLACK, 2, 0));
        pieces.add(new Bishop(BLACK, 5, 0));
        pieces.add(new King(BLACK, 4, 0));
        pieces.add(new Queen(BLACK, 3, 0));
    }

    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
        target.clear();
        target.addAll(source);
    }

    @Override
    public void run() {
        // Game Loop

        double drawInterval = (double) 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();

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
            if (mouse != null) {
                if (mouse.pressed) {
                    if (activePiece == null) {
                        // If the activePiece is null, check if you can pick up a piece
                        for (Piece piece : pieces) {
                            // If the mouse is on an ally piece, pick it up as the activePiece
                            int adjustedMouseCol = (mouse.x - MARGIN_X) / Board.SQUARE_SIZE;
                            int adjustedMouseRow = (mouse.y - MARGIN_Y) / Board.SQUARE_SIZE;
                            if (piece.color == currentColor && piece.col == adjustedMouseCol && piece.row == adjustedMouseRow) {
                                activePiece = piece;
                            }
                        }
                    } else {
                        // If the player is holding a piece, simulate the move
                        simulate();
                    }
                }

                // Mouse button released
                if (!mouse.pressed) {
                    if (activePiece != null) {
                        if (validSquare) {
                            // MOVE CONFIRMED

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
                        } else {
                            // The move is not valid so reset everything
                            copyPieces(pieces, simPieces);

                            activePiece.resetPosition();
                            activePiece = null;

                        }
                    }
                }
            }
        }

    }

    private void simulate() {
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

    private boolean isIllegal(Piece king) {
        if (king.type == main.Type.KING) {
            for (Piece piece : simPieces) {
                if (piece != king && piece.color != king.color && piece.canMove(king.col, king.row)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean opponentCanCaptureKing() {
        Piece king = getKing(false);
        for (Piece piece : simPieces) {
            if (piece.color != king.color && piece.canMove(king.col, king.row)) {
                return true;
            }
        }

        return false;
    }

    private boolean isKingInCheck() {
        Piece king = getKing(true);

        for (Piece piece : simPieces) {
            if( piece.color != king.color && piece.canMove(king.col, king.row)) {
                checkingPiece = piece;
                return true;
            }
        }
        checkingPiece = null;
        return false;
    }

    private Piece getKing(boolean opponent) {
        Piece king = null;

        for (Piece piece : simPieces) {
            if (opponent) {
                if (piece.type == main.Type.KING && piece.color != currentColor) {
                    king = piece;
                }
            } else {
                if (piece.type == main.Type.KING && piece.color == currentColor) {
                    king = piece;
                }
            }
        }

        return king;
    }

    private boolean isEndOfGame() {
        if (isKingInCheck() && isCheckmate()) {
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

        if (kingCanMove(king)) {
            return false;
        } else {
            // Check if you can block the attack with your piece

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
                                return false;
                            }
                        }
                    }
                }
                if (checkingPiece.col < king.col) {
                    // The checking piece is below the king
                    for (int row = checkingPiece.row; row > king.row + rowDiff; row--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingPiece.col, row)) {
                                return false;
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
                                return false;
                            }
                        }
                    }
                }
                if (checkingPiece.col > king.col) {
                    // The checking piece is to the right
                    for (int col = checkingPiece.col; col > king.col + rowDiff; col--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, checkingPiece.row)) {
                                return false;
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
                                    return false;
                                }
                            }
                        }
                    }
                    if (checkingPiece.col > king.col) {
                        // The checking piece is in the upper right
                        for (int col = checkingPiece.col, row = checkingPiece.row; col > king.col; col--, row++) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                    return false;
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
                                    return false;
                                }
                            }
                        }
                    }
                    if (checkingPiece.col > king.col) {
                        // The checking piece is in the lower right
                        for (int col = checkingPiece.col, row = checkingPiece.row; col > king.col; col--, row--) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            } else {
                // Knight attacking
                for (Piece piece : simPieces) {
                    if (piece != king && piece.color != currentColor) {
                        if(piece.canMove(checkingPiece.col, checkingPiece.row)) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean kingCanMove(Piece king) {
        // Simulate if there is any square where the king can move to
        if (isValidMove(king, -1, -1)) {
            return true;
        }
        if (isValidMove(king, 0, -1)) {
            return true;
        }
        if (isValidMove(king, 1, -1)) {
            return true;
        }
        if (isValidMove(king, -1, 0)) {
            return true;
        }
        if (isValidMove(king, 1, 0)) {
            return true;
        }
        if (isValidMove(king, -1, 1)) {
            return true;
        }
        if (isValidMove(king, 0, 1)) {
            return true;
        }
        return isValidMove(king, 1, 1);
    }

    private boolean isValidMove(Piece king, int colPlus, int rowPlus) {
        boolean isValidMove = false;

        // Update the king's position for a second
        king.col += colPlus;
        king.row += rowPlus;

        if (king.canMove(king.col, king.row)) {
            if (king.hittingPiece != null) {
                simPieces.remove(king.hittingPiece.getIndex());
            }
            if (!isIllegal(king)) {
                isValidMove = true;
            }
        }

        // Reset the king's position and restore the removed piece
        king.resetPosition();
        copyPieces(pieces, simPieces);

        return isValidMove;
    }

    private boolean isStalemate() {
        int count = 0;
        // Count the number of pieces
        for (Piece piece : simPieces) {
            if (piece.color != currentColor) {
                count++;
            }
        }

        // If only one piece (the king) is left
        if (count == 1) {
            return !kingCanMove(getKing(true));
        }

        return false;
    }

    private boolean isFiftyMovesDraw(Piece activePiece) {
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
        if (pieces.size() == 4 && (whiteKnights == 2 && blackMaterial == 0 || blackKnights == 2 && whiteMaterial == 0)) {
            return true;
        }

        return false;
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

    private String getBoardState() {
        StringBuilder state = new StringBuilder();

        for (Piece piece : pieces) {
            state.append(piece.type).append(piece.color)
                    .append(piece.col).append(piece.row).append(";");
        }

        state.append(currentColor).append(";");

        return state.toString();
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

    private void changePlayer() {
        fiftyMoveCounter++;

        if (currentColor == WHITE) {
            currentColor = BLACK;
            // Reset black's two stepped status
            for (Piece piece : pieces) {
                if (piece.color == BLACK) {
                    piece.twoStepped = false;
                }
            }
        } else {
            currentColor = WHITE;
            // Reset white's two stepped status
            for (Piece piece : pieces) {
                if (piece.color == WHITE) {
                    piece.twoStepped = false;
                }
            }
        }
        activePiece = null;
    }

    private boolean canPromote() {
        if (activePiece.type == main.Type.PAWN) {
            if (currentColor == WHITE && activePiece.row == 0 || currentColor == BLACK && activePiece.row == 7) {
                promoPieces.clear();
                promoPieces.add(new Rook(currentColor, 9, 2));
                promoPieces.add(new Knight(currentColor, 9, 3));
                promoPieces.add(new Bishop(currentColor, 9, 4));
                promoPieces.add(new Queen(currentColor, 9, 5));
                return true;
            }
        }

        return false;
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

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // Board
        board.draw(g2d);

        // Pieces
        for (Piece piece : pieces) {
            piece.draw(g2d);
        }

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

}
