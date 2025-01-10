package main;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {

    private int cornerRadius = 15; // Adjust the corner radius as needed

    public RoundedButton(String text) {
        super(text);
        setFocusPainted(false); // Remove focus border
        setContentAreaFilled(false); // Disable default button fill
        setBorderPainted(false); // Disable default button border
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
    }

    @Override
    public Insets getInsets() {
        return new Insets(15, 15, 15, 15); // Top, left, bottom, right padding
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        // Antialiasing for smooth corners
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background color
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Draw button text
        g2d.setColor(getForeground());
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(getText(), x, y);

        g2d.dispose();
        super.paintComponent(g);
    }
}
