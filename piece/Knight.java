package piece;

import main.GamePanel;

public class Knight extends Piece {
    public Knight(int color, int col, int row) {
        super(color, col, row);

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/UI_Project_Chess_W_Knight");
        } else {
            image = getImage("/piece/UI_Project_Chess_B_Knight");
        }
    }
}
