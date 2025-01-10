package main;

import java.awt.*;

import static main.Constants.MARGIN_X;
import static main.Constants.MARGIN_Y;
import static main.Utils.getDarkColor;
import static main.Utils.getLightColor;

public class Board {
    public static final int SQUARE_SIZE = 100;
    public static final int HALF_SQUARE_SIZE = SQUARE_SIZE / 2;
    final int MAX_COL = 8;
    final int MAX_ROW = 8;

    public void draw(Graphics2D g2) {
        int c = 0;

        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                if (c == 0) {
                    g2.setColor(getLightColor());
                    c = 1;
                } else {
                    g2.setColor(getDarkColor());
                    c = 0;
                }
                g2.fillRect(MARGIN_X + col * SQUARE_SIZE, MARGIN_Y + row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
            c = (c == 0) ? 1 : 0;
        }
    }
}
