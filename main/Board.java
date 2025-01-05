package main;

import java.awt.*;

public class Board {
    public static final int SQUARE_SIZE = 100;
    public static final int HALF_SQUARE_SIZE = SQUARE_SIZE / 2;
    final int MAX_COL = 8;
    final int MAX_ROW = 8;

    // COLORS
    final Color DARK_SQUARE_COLOR_GREEN = new Color(118, 150, 86);
    final Color LIGHT_SQUARE_COLOR_GREEN = new Color(238, 238, 210);

    // ALT COLORS
    final Color LIGHT_SQUARE_COLOR_BROWN = new Color(210, 165, 125);
    final Color DARK_SQUARE_COLOR_BROWN = new Color(175, 115, 70);

    public void draw(Graphics2D g2) {
        int c = 0;

        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                if (c == 0) {
                    g2.setColor(LIGHT_SQUARE_COLOR_GREEN);
                    c = 1;
                } else {
                    g2.setColor(DARK_SQUARE_COLOR_GREEN);
                    c = 0;
                }
                g2.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
            c = (c == 0) ? 1 : 0;
        }
    }
}
