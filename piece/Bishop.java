package piece;

import main.GamePanel;
import main.Type;

public class Bishop extends Piece {

    public Bishop(int color, int col, int row) {
        super(color, col, row);

        type = Type.BISHOP;

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/UI_Project_Chess_W_Bishop");
        } else {
            image = getImage("/piece/UI_Project_Chess_B_Bishop");
        }
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if (isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {
            if (Math.abs(targetCol - preCol) == Math.abs(targetRow - preRow)) { // it can move diagonally
                if (isValidSquare(targetCol, targetRow) && !pieceIsOnDiagonalLine(targetCol, targetRow)) {
                    return true;
                }
            }
        }

        return false;
    }
}
