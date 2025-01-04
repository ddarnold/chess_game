package piece;

import main.GamePanel;

public class Queen extends Piece {

    public Queen(int color, int col, int row) {
        super(color, col, row);

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/UI_Project_Chess_W_Queen");
        } else {
            image = getImage("/piece/UI_Project_Chess_B_Queen");
        }
    }
}
