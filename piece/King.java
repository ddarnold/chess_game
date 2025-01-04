package piece;

import main.GamePanel;

public class King extends Piece {
    public King(int color, int col, int row) {
        super(color, col, row);

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/UI_Project_Chess_W_King");
        } else {
            image = getImage("/piece/UI_Project_Chess_B_King");
        }
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if (isWithinBoard(targetCol, targetRow)) {
            if (Math.abs(targetCol - preCol) + Math.abs(targetRow - preRow) == 1 || // up, down, left, right
                    (Math.abs(targetCol - preCol) * Math.abs(targetRow - preRow) == 1)) // diagonals
            {
                if (isValidSquare(targetCol, targetRow)) {
                    return true;
                }
            }
        }

        return false;
    }
}
