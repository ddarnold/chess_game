package main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static main.Utils.createRoundedButton;

public class Menu extends JPanel {
    private final Main parentWindow;
    private BufferedImage backgroundImage;

    public Menu(Main parentWindow) {
        this.parentWindow = parentWindow;
        parentWindow.setCursor(Cursor.getDefaultCursor());
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
        title.setBorder(BorderFactory.createEmptyBorder(200, 100, 0, 750)); // Top, left, bottom, right
        add(title, BorderLayout.NORTH);

        // Buttons Panel with Margins
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 1, 10, 10)); // 3 buttons, no gaps
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 100, 200, 750)); // Add margins around the buttons

        // Create buttons
        RoundedButton playWithAiButton = createRoundedButton("Play vs AI");
        RoundedButton playOfflineButton = createRoundedButton("Play Offline");
        RoundedButton playOnlineButton = createRoundedButton("Play Online");
        JButton settingsButton = createRoundedButton("Settings");
        JButton exitButton = createRoundedButton("Exit");
        playWithAiButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    null,
                    "Would you like to play as white?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                startGame(GameType.AGAINST_AI_AS_WHITE);
            } else {
                //startGame(GameType.AGAINST_AI_AS_BLACK);
            }
        });
        playOfflineButton.addActionListener(e -> startGame(GameType.LOCAL_2_PLAYER));
        playOnlineButton.addActionListener(e -> showLobby());
        settingsButton.addActionListener(e -> showSettings());
        exitButton.addActionListener(e -> System.exit(0));
    

        // Add buttons to panel
        buttonPanel.add(playWithAiButton);
        buttonPanel.add(playOfflineButton);
        buttonPanel.add(playOnlineButton);
        buttonPanel.add(settingsButton);
        buttonPanel.add(exitButton);

        // Add button panel to bottom
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void startGame(GameType gameType) {
        GamePanel gamePanel = new GamePanel(parentWindow, gameType);
        parentWindow.switchToPanel(gamePanel);
        gamePanel.launchGame();
    }

    private void showSettings() {
        parentWindow.switchToPanel(new Settings(parentWindow));
    }

    private void showLobby() {
        parentWindow.switchToPanel(new LobbyPanel(parentWindow));
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
