package piece;

import main.GamePanel;

public class Pawn extends Piece {
    public Pawn(int color, int col, int row) {
        super(color, col, row);

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/UI_Project_Chess_W_Pawn");
        } else {
            image = getImage("/piece/UI_Project_Chess_B_Pawn");
        }
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if (isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {
            // Define the move value based on its color
            int moveValue;
            if (color == GamePanel.WHITE) {
                moveValue = -1;
            } else {
                moveValue = 1;
            }

            // Check the hitting piece
            hittingPiece = getHittingPiece(targetCol, targetRow);

            // 1 square movement
            if (targetCol == preCol && targetRow == preRow + moveValue && hittingPiece == null) {
                return true;
            }

            // 2 sqaure movement
            if (targetCol == preCol && targetRow == preRow + 2 * moveValue && hittingPiece == null && !moved && !pieceIsOnStraightLine(targetCol, targetRow)) {
                return true;
            }

            // Diagonal Capture (if a piece is on a square diagonally in front of it)
            if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue && hittingPiece != null && hittingPiece.color != color) {
                return true;
            }
        }

        return false;
    }
}
