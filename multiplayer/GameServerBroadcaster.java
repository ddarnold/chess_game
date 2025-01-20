package multiplayer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class GameServerBroadcaster implements Runnable {
    private final int broadcastPort = Constants.MULTICAST_GROUP_PORT; // Fixed port for broadcasting
    private boolean running = true;
    private final int serverPort;

    public GameServerBroadcaster(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            String message = "GameServer:" + InetAddress.getLocalHost().getHostAddress() + ":" + serverPort;
            byte[] buffer = message.getBytes();
            InetAddress group = InetAddress.getByName(Constants.MULTICAST_GROUP_ADDRESS); // Multicast group address

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, broadcastPort);
                socket.send(packet);
                System.out.println("Broadcasting server availability on port: " + serverPort);
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
