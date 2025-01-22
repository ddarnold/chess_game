package main;

import piece.Piece;

import java.util.ArrayList;
import java.util.Random;

public class AI {
    private final Random random = new Random();
    private final ArrayList<Piece> pieces;

    public AI(ArrayList<Piece> pieces) {
        this.pieces = pieces;
    }

    // Returns a move in the format [startCol, startRow, targetCol, targetRow]
    public int[] getNextMove(int aiColor) {
        for (Piece piece : pieces) {
            if (piece.color == aiColor) {
                for (int col = 0; col < 8; col++) {
                    for (int row = 0; row < 8; row++) {
                        if (piece.canMove(col, row)) {
                            return new int[]{piece.col, piece.row, col, row};
                        }
                    }
                }
            }
        }
        return null; // No move found (stalemate or checkmate)
    }
}

