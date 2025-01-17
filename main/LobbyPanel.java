package main;

import multiplayer.GameClient;
import multiplayer.GameClientDiscovery;
import multiplayer.GameServer;
import multiplayer.GameServerBroadcaster;

import javax.swing.*;
import java.awt.*;

public class LobbyPanel extends JPanel {
    private final Main parentWindow;

    private JTextArea serverList;
    private JButton hostButton, refreshButton, joinButton;

    private GameServer server;
    private GameClient client;
    private Thread discoveryThread;

    public LobbyPanel(Main parentWindow) {
        this.parentWindow = parentWindow;

        setLayout(new BorderLayout());

        // Server List
        serverList = new JTextArea();
        serverList.setEditable(false);
        add(new JScrollPane(serverList), BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(200, 100, 0, 750));

        hostButton = new JButton("Host Game");
        refreshButton = new JButton("Refresh");
        joinButton = new JButton("Join Game");


        buttonPanel.add(hostButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(joinButton);

        add(buttonPanel, BorderLayout.NORTH);

        // Button Actions
        hostButton.addActionListener(e -> hostGame());
        refreshButton.addActionListener(e -> discoverGames());
        joinButton.addActionListener(e -> joinGame());

        Utils.createMenuButton(parentWindow, this);
    }

    private void hostGame() {
        try {
            server = new GameServer();
            server.startServer(); // Start the server logic


            startGame(GameType.MULTIPLAYER_AS_HOST_WHITE, server);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to host the game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void discoverGames() {
        System.out.println("Discovering games...");
        GameClientDiscovery discovery = new GameClientDiscovery();
        discovery.setListener((serverAddress, serverPort) -> SwingUtilities.invokeLater(() -> serverList.append(serverAddress + ":" + serverPort + "\n")));

        discoveryThread = new Thread(discovery);
        discoveryThread.start();
    }

    private void joinGame() {
        try {
            String serverAddress = JOptionPane.showInputDialog(this, "Enter Server Address (e.g., localhost:8008):");
            if (serverAddress == null || serverAddress.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Server address cannot be empty!", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] parts = serverAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            client = new GameClient();
            client.connectToServer(host, port);

            JOptionPane.showMessageDialog(this, "Connected to server: " + serverAddress);
            System.out.println("Joined game at " + serverAddress);
            startGame(GameType.MULTIPLAYER_AS_CLIENT, client);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to join the game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void startGame(GameType gameType, Object connection) {
        GamePanel gamePanel = new GamePanel(parentWindow, gameType, connection);
        parentWindow.switchToPanel(gamePanel);
        gamePanel.launchGame();
    }
}
