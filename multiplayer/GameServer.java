package multiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    private ServerSocket serverSocket;
    private boolean running = true;

    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;

    public void startServer() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(Constants.PORT));
            System.out.println("Server started on port: " + Constants.PORT + ",  Waiting for a client...");

            // Start broadcasting availability
            GameServerBroadcaster broadcaster = new GameServerBroadcaster(Constants.PORT);
            Thread broadcasterThread = new Thread(broadcaster);
            broadcasterThread.start();

            // Accept connections
            new Thread(() -> {
                while (running) {
                    try {
                        System.out.println("Waiting for a client...");
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Client connected: " + clientSocket.getInetAddress());

                        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        output = new PrintWriter(clientSocket.getOutputStream(), true);

                        // Stop broadcasting once a client connects
                        broadcaster.stop();
                    } catch (IOException e) {
                        if (running) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            stopServer(); // Ensure partial resources are released.
        }

    }

    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }

    public String receiveMessage() {
        try {
            return input.readLine();
        } catch (IOException e) {
            System.err.println("Error reading message: " + e.getMessage());
        }
        return null;
    }

    public void stopServer() {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing input: " + e.getMessage());
        }

        try {
            if (output != null) {
                output.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing output: " + e.getMessage());
        }

        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }

        running = false;
    }
}

