package multiplayer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class GameServerBroadcaster implements Runnable {
    private final int port = 12345; // Fixed port for broadcasting
    private boolean running = true;

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            while (running) {
                String message = "GameServer:" + InetAddress.getLocalHost().getHostAddress() + ":" + port;
                byte[] buffer = message.getBytes();
                InetAddress group = InetAddress.getByName("230.0.0.0"); // Multicast group address
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 45678);
                socket.send(packet);
                Thread.sleep(2000); // Broadcast every 2 seconds
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }
}
