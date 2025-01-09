package main;

import piece.Piece;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

import static main.Constants.*;
import static main.GamePanel.pieces;

public class Mouse extends MouseAdapter {
    public int x, y;
    public boolean pressed;
    private final JFrame parentWindow;
    private final GamePanel gamePanel;

    public Mouse(JFrame parentWindow, GamePanel gamePanel) {
        this.parentWindow = parentWindow;
        this.gamePanel = gamePanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int buttonX = LAYOUT_WIDTH - 200 - 10;
        int buttonY = LAYOUT_HEIGHT - 50 - 40;
        if (e.getX() >= buttonX && e.getX() <= buttonX + 200 &&
                e.getY() >= buttonY && e.getY() <= buttonY + 50) {
            Utils.returnToHome(parentWindow, gamePanel);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        pressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        pressed = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        x = e.getX();
        y = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        x = e.getX();
        y = e.getY();
        boolean isHovering = false;

        // Check if the mouse is over any piece
        for (Piece piece : pieces) {
            int pieceX = piece.x + MARGIN_X + SCALED_DOWN_VALUE;
            int pieceY = piece.y + MARGIN_Y + SCALED_DOWN_VALUE;
            int pieceWidth = Board.SQUARE_SIZE - SCALED_DOWN_VALUE * 2;
            int pieceHeight = Board.SQUARE_SIZE - SCALED_DOWN_VALUE * 2;

            if (e.getX() >= pieceX && e.getX() <= pieceX + pieceWidth &&
                    e.getY() >= pieceY && e.getY() <= pieceY + pieceHeight) {
                isHovering = true;
                break;
            }
        }

        // Update cursor
        if (isHovering) {
            parentWindow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            parentWindow.setCursor(Cursor.getDefaultCursor());
        }

        int buttonX = LAYOUT_WIDTH - 200 - 10;
        int buttonY = LAYOUT_HEIGHT - 50 - 40;
        if (e.getX() >= buttonX && e.getX() <= buttonX + 200 &&
                e.getY() >= buttonY && e.getY() <= buttonY + 50) {
            parentWindow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }
}
