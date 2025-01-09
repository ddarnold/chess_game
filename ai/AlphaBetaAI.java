package ai;

import main.GamePanel;
import piece.Piece;
import main.Type;

import java.util.ArrayList;

public class AlphaBetaAI {
    private static final int MAX_DEPTH = 2;
    private static final int CENTER_BONUS = 3;

    public void makeMove() {
        Move bestMove = findBestMove(GamePanel.BLACK, MAX_DEPTH, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        if (bestMove != null) {
            Piece piece = bestMove.piece;
            piece.col = bestMove.targetCol;
            piece.row = bestMove.targetRow;
            piece.updatePosition();

            // If a piece is captured, remove it
            if (bestMove.capturedPiece != null) {
                GamePanel.pieces.remove(bestMove.capturedPiece);
            }
        }
    }

    private Move findBestMove(int color, int depth, double alpha, double beta) {
        ArrayList<Move> possibleMoves = getAllPossibleMoves(color);
        Move bestMove = null;
        double bestScore = (color == GamePanel.BLACK) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (Move move : possibleMoves) {
            // Simulate the move
            simulateMove(move);
            double score = alphaBeta(color == GamePanel.BLACK ? GamePanel.WHITE : GamePanel.BLACK, depth - 1, alpha, beta);

            // Undo the move
            undoMove(move);

            if ((color == GamePanel.BLACK && score > bestScore) || (color == GamePanel.WHITE && score < bestScore)) {
                bestScore = score;
                bestMove = move;

                if (color == GamePanel.BLACK) {
                    alpha = Math.max(alpha, score);
                } else {
                    beta = Math.min(beta, score);
                }

                if (beta <= alpha) {
                    break;
                }
            }
        }

        return bestMove;
    }

    private double alphaBeta(int color, int depth, double alpha, double beta) {
        if (depth == 0 || isGameOver()) {
            return evaluateBoard();
        }

        ArrayList<Move> possibleMoves = getAllPossibleMoves(color);

        for (Move move : possibleMoves) {
            // Simulate the move
            simulateMove(move);
            double score = alphaBeta(color == GamePanel.BLACK ? GamePanel.WHITE : GamePanel.BLACK, depth - 1, alpha, beta);

            // Undo the move
            undoMove(move);

            if (color == GamePanel.BLACK) {
                alpha = Math.max(alpha, score);
            } else {
                beta = Math.min(beta, score);
            }

            if (beta <= alpha) {
                return (color == GamePanel.BLACK) ? alpha : beta;
            }
        }

        return (color == GamePanel.BLACK) ? alpha : beta;
    }

    private ArrayList<Move> getAllPossibleMoves(int color) {
        ArrayList<Move> moves = new ArrayList<>();
        for (Piece piece : GamePanel.pieces) {
            if (piece.color == color) {
                ArrayList<int[]> possibleMoves = piece.getPossibleMoves();
                for (int[] move : possibleMoves) {
                    int targetCol = move[0];
                    int targetRow = move[1];
                    Piece capturedPiece = getHittingPiece(targetCol, targetRow);
                    moves.add(new Move(piece, targetCol, targetRow, capturedPiece));
                }
            }
        }
        return moves;
    }

    private double evaluateBoard() {
        double score = 0;

        for (Piece piece : GamePanel.pieces) {
            int value = getPieceValue(piece.type);
            int col = piece.col;
            int row = piece.row;

            // Add positional bonuses for controlling the center
            if (col >= 2 && col <= 5 && row >= 2 && row <= 5) {
                value += CENTER_BONUS;
            }

            // Calculate score based on color
            score += (piece.color == GamePanel.WHITE ? -value : value);
        }

        return score;
    }

    private int getPieceValue(Type type) {
        switch (type) {
            case PAWN:
                return 1;
            case KNIGHT:
                return 3;
            case BISHOP:
                return 3;
            case ROOK:
                return 5;
            case QUEEN:
                return 9;
            case KING:
                return 1000; // Arbitrarily high value for the king
            default:
                return 0;
        }
    }

    private boolean isGameOver() {
        return GamePanel.gameOver || GamePanel.stalemate;
    }

    private void simulateMove(Move move) {
        Piece piece = move.piece;
        piece.preCol = piece.col;
        piece.preRow = piece.row;
        piece.col = move.targetCol;
        piece.row = move.targetRow;

        if (move.capturedPiece != null) {
            GamePanel.pieces.remove(move.capturedPiece);
        }
    }

    private void undoMove(Move move) {
        Piece piece = move.piece;
        piece.col = piece.preCol;
        piece.row = piece.preRow;

        if (move.capturedPiece != null) {
            GamePanel.pieces.add(move.capturedPiece);
        }
    }

    private Piece getHittingPiece(int col, int row) {
        for (Piece piece : GamePanel.pieces) {
            if (piece.col == col && piece.row == row) {
                return piece;
            }
        }
        return null;
    }

    private static class Move {
        Piece piece;
        int targetCol, targetRow;
        Piece capturedPiece;

        Move(Piece piece, int targetCol, int targetRow, Piece capturedPiece) {
            this.piece = piece;
            this.targetCol = targetCol;
            this.targetRow = targetRow;
            this.capturedPiece = capturedPiece;
        }
    }
}
