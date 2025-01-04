package piece;

import main.GamePanel;

public class Rook extends Piece {
    public Rook(int color, int col, int row) {
        super(color, col, row);

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/UI_Project_Chess_W_Rook");
        } else {
            image = getImage("/piece/UI_Project_Chess_B_Rook");
        }
    }
}
