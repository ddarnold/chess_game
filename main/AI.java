package main;

import piece.Piece;

import java.util.ArrayList;
import java.util.List;

public class AI {
    private final ArrayList<Piece> pieces;
    private static final int MAX_DEPTH = 4;

    public AI(ArrayList<Piece> pieces) {
        this.pieces = pieces;
    }

    public int[] getNextMove(int aiColor) {
        Move bestMove = minimax(MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true, aiColor);
        return bestMove != null ? new int[]{bestMove.startCol, bestMove.startRow, bestMove.targetCol, bestMove.targetRow} : null;
    }

    private Move minimax(int depth, int alpha, int beta, boolean isMaximizingPlayer, int aiColor) {
        if (depth == 0 || isGameOver()) {
            return new Move(evaluateBoard(aiColor));
        }

        List<Move> possibleMoves = generateAllMoves(isMaximizingPlayer ? aiColor : 1 - aiColor);
        Move bestMove = null;

        for (Move move : possibleMoves) {
            executeMove(move);
            System.out.println("Executing move: t2" + move);

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
        List<Piece> piecesCopy = new ArrayList<>(pieces);

        for (Piece piece : piecesCopy) {
            if (piece.color == color) {
                for (int col = 0; col < 8; col++) {
                    for (int row = 0; row < 8; row++) {
                        if (piece.canMove(col, row)) {
                            Piece targetPiece = getPieceAt(col, row);
                            if (targetPiece == null || targetPiece.color != color) {
                                Move move = new Move(piece.col, piece.row, col, row, targetPiece);

                                // Apply the move temporarily
                                executeMove(move);

                                // Check for checkmate
                                if (isCheckmate(1 - color)) {
                                    move.score += 10000; // Reward checkmate moves
                                } else if (isKingInCheck(1 - color)) {
                                    move.score += 50; // Reward moves that put the opponent in check
                                }

                                // Undo the move
                                undoMove(move);

                                // Add the move to the list
                                moves.add(move);
                            }
                        }
                    }
                }
            }
        }

        // Sort moves by score in descending order to improve pruning
        moves.sort((m1, m2) -> Integer.compare(m2.score, m1.score));
        return moves;
    }



    private void executeMove(Move move) {
        Piece piece = getPieceAt(move.startCol, move.startRow);
        if (piece != null) {
            move.capturedPiece = getPieceAt(move.targetCol, move.targetRow);
            piece.col = move.targetCol;
            piece.row = move.targetRow;
            pieces.remove(move.capturedPiece);
        }
//        System.out.println("Executing move: " + move);

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

        // Check for checkmate or losing conditions
        if (isCheckmate(1 - aiColor)) return Integer.MAX_VALUE; // Bot wins
        if (isCheckmate(aiColor)) return Integer.MIN_VALUE; // Bot loses
        if (isKingInCheck(aiColor)) score -= 200; // Heavily penalize if the bot's king is in check

        for (Piece piece : pieces) {
            int value = getPieceValue(piece);
            score += (piece.color == aiColor ? value : -value);

            if (isCenterPosition(piece)) {
                score += (piece.color == aiColor ? 5 : -5); // Bonus for central control
            }

            // Bonus for attacking opponent's king
            if (piece.color == aiColor && canAttackKing(piece, 1 - aiColor)) {
                score += 100; // Reward moves that attack the king
            }

            // Penalty for leaving the bot's king exposed
            if (piece.type == Type.KING && piece.color == aiColor && !isKingSafe(piece)) {
                score -= 50;
            }
        }

        return score;
    }

    private boolean canAttackKing(Piece piece, int opponentColor) {
        Piece king = pieces.stream().filter(p -> p.type == Type.KING && p.color == opponentColor).findFirst().orElse(null);
        return king != null && piece.canMove(king.col, king.row);
    }

    private boolean isKingSafe(Piece king) {
        return pieces.stream().noneMatch(p -> p.color != king.color && p.canMove(king.col, king.row));
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
                return 90;
            case KING:
                return 900;
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

    private Piece getPieceAt(int col, int row) {
        return pieces.stream().filter(p -> p.col == col && p.row == row).findFirst().orElse(null);
    }

    private static class Move {
        int startCol, startRow, targetCol, targetRow, score;
        Piece capturedPiece;

        Move(int startCol, int startRow, int targetCol, int targetRow, Piece capturedPiece) {
            this.startCol = startCol;
            this.startRow = startRow;
            this.targetCol = targetCol;
            this.targetRow = targetRow;
            this.capturedPiece = capturedPiece;
        }

        Move(int score) {
            this.score = score;
        }
    }

    private boolean isCheckmate(int color) {
        if (!isKingInCheck(color)) return false; // Not in check, so no checkmate
        List<Move> possibleMoves = generateAllMoves(color);
        for (Move move : possibleMoves) {
            executeMove(move);
            boolean kingStillInCheck = isKingInCheck(color);
            undoMove(move);
            if (!kingStillInCheck) return false; // Escape route exists
        }
        return true; // No escape, checkmate
    }

}
