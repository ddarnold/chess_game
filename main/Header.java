package main;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class Header extends JPanel {
    public Header(JFrame parentWindow) {
        setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Right-align buttons
        setBackground(Color.DARK_GRAY);

        // Minimize Button
        JButton minimizeButton = createCircularButton("minimize.png");
        minimizeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        minimizeButton.addActionListener(e -> parentWindow.setState(Frame.ICONIFIED));

        // Close Button
        JButton closeButton = createCircularButton("close.png");
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> System.exit(0));

        // Add buttons to header
        add(minimizeButton);
        add(closeButton);
    }

    private JButton createCircularButton(String iconPath) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(30, 30)); // Circular size
        button.setBackground(Color.GRAY); // Button background
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        // Add icon (assuming icons are in the 'res/icons' directory)
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(Utils.class.getResource("/res/icon/" + iconPath)));
        Image scaledIcon = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        button.setIcon(new ImageIcon(scaledIcon));

        return button;
    }
}
