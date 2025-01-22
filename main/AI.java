package main;

import piece.Piece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AI {
    private final ArrayList<Piece> pieces;
    private static final int MAX_DEPTH = 3;

    // Evaluation table to prioritize control of the center
    private static final int[][] POSITIONAL_VALUES = {
            {1, 1, 1, 2, 2, 1, 1, 1},
            {1, 2, 2, 3, 3, 2, 2, 1},
            {1, 3, 4, 5, 5, 4, 3, 1},
            {2, 3, 5, 5, 5, 5, 3, 2},
            {2, 3, 5, 5, 5, 5, 3, 2},
            {1, 2, 3, 4, 4, 3, 2, 1},
            {1, 2, 2, 3, 3, 2, 2, 1},
            {1, 1, 1, 2, 2, 1, 1, 1}
    };

    public AI(ArrayList<Piece> pieces) {
        this.pieces = pieces;
    }

    private boolean isFirstMove = true; // Track if it's the first move

    public int[] getNextMove(int aiColor) {
        if (isFirstMove) {
            isFirstMove = false; // First move will now be over
            Move randomOpening = getRandomOpeningMove(aiColor);
            if (randomOpening != null) {
                return new int[]{randomOpening.startCol, randomOpening.startRow, randomOpening.targetCol, randomOpening.targetRow};
            }
        }

        // Use minimax for subsequent moves
        Move bestMove = minimax(MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true, aiColor);
        return bestMove != null ? new int[]{bestMove.startCol, bestMove.startRow, bestMove.targetCol, bestMove.targetRow} : null;
    }

    private Move getRandomOpeningMove(int aiColor) {
        List<Move> openingMoves = new ArrayList<>();
        for (Piece piece : pieces) {
            if (piece.color == aiColor) {
                for (int col = 0; col < 8; col++) {
                    for (int row = 0; row < 8; row++) {
                        if (piece.canMove(col, row)) {
                            Piece targetPiece = getPieceAt(col, row);
                            if (targetPiece == null || targetPiece.color != aiColor) {
                                int positionalValue = POSITIONAL_VALUES[row][col];
                                openingMoves.add(new Move(piece.col, piece.row, col, row, targetPiece, positionalValue));
                            }
                        }
                    }
                }
            }
        }

        // Shuffle the list to randomize order
        if (!openingMoves.isEmpty()) {
            Collections.shuffle(openingMoves); // Adds randomness

            // Sort only after shuffling to prioritize good moves while keeping variety
            openingMoves.sort((m1, m2) -> Integer.compare(m2.score, m1.score));

            // Return a random move among the top 3 best-scoring moves
            return openingMoves.get(Math.min(openingMoves.size() - 1, (int) (Math.random() * Math.min(3, openingMoves.size()))));
        }

        return null; // No valid opening moves found
    }

    private Move minimax(int depth, int alpha, int beta, boolean isMaximizingPlayer, int aiColor) {
        if (depth == 0 || isGameOver()) {
            return new Move(evaluateBoard(aiColor));
        }

        List<Move> possibleMoves = generateAllMoves(isMaximizingPlayer ? aiColor : 1 - aiColor);
        Move bestMove = null;

        for (Move move : possibleMoves) {
            executeMove(move);
            if (isKingInCheck(aiColor)) {
                undoMove(move);
                continue; // Skip moves that leave the king in check
            }

            Move evaluation = minimax(depth - 1, alpha, beta, !isMaximizingPlayer, aiColor);
            undoMove(move);

            if (isMaximizingPlayer) {
                if (evaluation.score > alpha) {
                    alpha = evaluation.score;
                    bestMove = move;
                    bestMove.score = alpha;
                }
            } else {
                if (evaluation.score < beta) {
                    beta = evaluation.score;
                    bestMove = move;
                    bestMove.score = beta;
                }
            }

            if (alpha >= beta) {
                break;
            }
        }

        return bestMove != null ? bestMove : new Move(isMaximizingPlayer ? alpha : beta);
    }

    private List<Move> generateAllMoves(int color) {
        List<Move> moves = new ArrayList<>();
        boolean allowKingMove = shouldMoveKing(color);

        for (Piece piece : pieces) {
            if (piece.color == color) {
                // Always allow king moves if the king is in check
                if (piece.type == Type.KING && (!allowKingMove || isKingInCheck(color))) {
                    allowKingMove = true;
                }

                // Skip king moves unless necessary
                if (piece.type == Type.KING && !allowKingMove) {
                    continue;
                }

                for (int col = 0; col < 8; col++) {
                    for (int row = 0; row < 8; row++) {
                        if (piece.canMove(col, row)) {
                            Piece targetPiece = getPieceAt(col, row);
                            if (targetPiece == null || targetPiece.color != color) {
                                if (piece.type == Type.QUEEN && targetPiece != null && getPieceValue(targetPiece) < getPieceValue(piece) && isProtected(targetPiece)) {
                                    continue;
                                }
                                moves.add(new Move(piece.col, piece.row, col, row, targetPiece, POSITIONAL_VALUES[row][col]));
                            }
                        }
                    }
                }
            }
        }

        return moves;
    }

    private boolean isProtected(Piece targetPiece) {
        if (targetPiece == null) {
            return false; // Null pieces can't be protected
        }
        int targetCol = targetPiece.col;
        int targetRow = targetPiece.row;
        int targetColor = targetPiece.color;

        // Check if any piece of the same color can move to the target piece's position
        for (Piece piece : pieces) {
            if (piece.color == targetColor && piece != targetPiece) {
                if (piece.canMove(targetCol, targetRow)) {
                    return true; // Found a protecting piece
                }
            }
        }

        return false; // No protecting pieces found
    }


    private void executeMove(Move move) {
        Piece piece = getPieceAt(move.startCol, move.startRow);
        if (piece != null) {
            move.capturedPiece = getPieceAt(move.targetCol, move.targetRow);
            piece.col = move.targetCol;
            piece.row = move.targetRow;
            pieces.remove(move.capturedPiece);
        }
    }

    private void undoMove(Move move) {
        Piece piece = getPieceAt(move.targetCol, move.targetRow);
        if (piece != null) {
            piece.col = move.startCol;
            piece.row = move.startRow;
            if (move.capturedPiece != null) {
                pieces.add(move.capturedPiece);
            }
        }
    }

    private int evaluateBoard(int aiColor) {
        int score = 0;
        for (Piece piece : pieces) {
            int value = getPieceValue(piece);
            score += (piece.color == aiColor ? value : -value);
            if (isCenterPosition(piece)) {
                score += (piece.color == aiColor ? 5 : -5); // Bonus for central control
            }
        }
        return score;
    }

    private int getPieceValue(Piece piece) {
        switch (piece.type) {
            case PAWN:
                return 10;
            case KNIGHT:
            case BISHOP:
                return 30;
            case ROOK:
                return 50;
            case QUEEN:
                return 900; // Significantly high value for the queen
            case KING:
                return 10000; // The king is invaluable
            default:
                return 0;
        }
    }

    private boolean isCenterPosition(Piece piece) {
        int col = piece.col;
        int row = piece.row;
        return (col >= 2 && col <= 5 && row >= 2 && row <= 5);
    }

    private boolean isGameOver() {
        return pieces.stream().noneMatch(p -> p.type == Type.KING);
    }

    private boolean isKingInCheck(int color) {
        Piece king = pieces.stream().filter(p -> p.type == Type.KING && p.color == color).findFirst().orElse(null);
        if (king == null) return true; // No king means the player is in checkmate

        for (Piece piece : pieces) {
            if (piece.color != color && piece.canMove(king.col, king.row)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldMoveKing(int color) {
        return pieces.stream().noneMatch(p -> p.color == color && p.type != Type.KING);
    }


    private Piece getPieceAt(int col, int row) {
        return pieces.stream().filter(p -> p.col == col && p.row == row).findFirst().orElse(null);
    }

    private static class Move {
        int startCol, startRow, targetCol, targetRow, score;
        Piece capturedPiece;

        Move(int startCol, int startRow, int targetCol, int targetRow, Piece capturedPiece, int positionalValue) {
            this.startCol = startCol;
            this.startRow = startRow;
            this.targetCol = targetCol;
            this.targetRow = targetRow;
            this.capturedPiece = capturedPiece;
            this.score = positionalValue;
        }

        Move(int score) {
            this.score = score;
        }
    }
}
