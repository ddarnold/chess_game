package multiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;

public class GameServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;

    public void startServer(int port){
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port));
            System.out.println("Server started. Waiting for a client...");
            clientSocket = serverSocket.accept();
            System.out.println("Client connected.");

            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            stopServer(); // Ensure partial resources are released.
        }
    }

    public void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(Constants.PORT));
            System.out.println("Server started. Waiting for a client...");
            clientSocket = serverSocket.accept();
            System.out.println("Client connected.");

            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);
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

    public String receiveMessage(){
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
    }
}

