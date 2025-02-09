package piece;

import main.GamePanel;
import main.Type;

public class Rook extends Piece {
    public Rook(int color, int col, int row) {
        super(color, col, row);

        type = Type.ROOK;

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/UI_Project_Chess_W_Rook");
        } else {
            image = getImage("/piece/UI_Project_Chess_B_Rook");
        }
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if (isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {
            // Rook can move as long as either its col or row is the same
            if (targetCol == preCol || targetRow == preRow) {
                if (isValidSquare(targetCol, targetRow) && !pieceIsOnStraightLine(targetCol, targetRow)) {
                    return true;
                }
            }
        }

        return false;
    }
}
