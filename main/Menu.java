package main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static main.Constants.LAYOUT_HEIGHT;
import static main.Constants.LAYOUT_WIDTH;

public class Menu extends JPanel {
    private final JFrame parentWindow;
    private BufferedImage backgroundImage;

    public Menu(JFrame parentWindow) {
        this.parentWindow = parentWindow;
        setPreferredSize(new Dimension(LAYOUT_WIDTH, LAYOUT_HEIGHT));
        try {
            backgroundImage = javax.imageio.ImageIO.read(Objects.requireNonNull(
                    getClass().getResource("/res/image/chess_bg.jpg")));
        } catch (IOException ignored) {
        }

        setLayout(new BorderLayout());

        // Title with Margin
        JLabel title = new JLabel("Chess");
        title.setFont(Utils.deriveFont(100, Font.BOLD));
        title.setForeground(Color.white);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(200, 100, 0, 650)); // Top, left, bottom, right
        add(title, BorderLayout.NORTH);

        // Buttons Panel with Margins
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 10)); // 3 buttons, no gaps
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 100, 200, 650)); // Add margins around the buttons

        // Create buttons
        RoundedButton playButton = createRoundedButton("Play");
        JButton settingsButton = createRoundedButton("Settings");
        JButton exitButton = createRoundedButton("Exit");
        playButton.addActionListener(e -> startGame());
        settingsButton.addActionListener(e -> showSettings());
        exitButton.addActionListener(e -> System.exit(0));

        // Add buttons to panel
        buttonPanel.add(playButton);
        buttonPanel.add(settingsButton);
        buttonPanel.add(exitButton);

        // Add button panel to bottom
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private RoundedButton createRoundedButton(String text) {
        RoundedButton button = new RoundedButton(text);
        button.setFont(Utils.deriveFont(25, Font.PLAIN));
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);
        button.setCornerRadius(50);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void startGame() {
        // Switch to game panel
        parentWindow.getContentPane().removeAll();
        GamePanel gamePanel = new GamePanel(parentWindow);
        Header header = new Header(parentWindow);
        parentWindow.add(header, BorderLayout.NORTH);
        parentWindow.add(gamePanel);
        parentWindow.revalidate();
        parentWindow.repaint();
        gamePanel.launchGame();
    }

    private void showSettings() {
        JOptionPane.showMessageDialog(this, "Settings will be implemented later!", "Settings", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();

            // Calculate aspect ratio
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imageWidth = backgroundImage.getWidth();
            int imageHeight = backgroundImage.getHeight();

            double panelAspect = (double) panelWidth / panelHeight;
            double imageAspect = (double) imageWidth / imageHeight;

            int drawWidth, drawHeight;
            if (panelAspect > imageAspect) {
                // Panel is wider than the image
                drawWidth = panelWidth;
                drawHeight = (int) (panelWidth / imageAspect);
            } else {
                // Panel is taller than the image
                drawHeight = panelHeight;
                drawWidth = (int) (panelHeight * imageAspect);
            }

            // Center the image
            int x = (panelWidth - drawWidth) / 2;
            int y = (panelHeight - drawHeight) / 2;

            // Draw the image
            g2d.drawImage(backgroundImage, x, y, drawWidth, drawHeight, this);
            g2d.dispose();
        }
    }
}
