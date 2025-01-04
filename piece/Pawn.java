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
}
