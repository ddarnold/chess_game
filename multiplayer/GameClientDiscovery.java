package multiplayer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class GameClientDiscovery implements Runnable {
    private final int port = 45678; // Multicast group port
    private boolean running = true;

    @Override
    public void run() {
        try (MulticastSocket socket = new MulticastSocket(port)) {
            InetAddress group = InetAddress.getByName("230.0.0.0"); // Multicast group address
            socket.joinGroup(group);

            while (running) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Discovered: " + message);
                // Parse and display available servers
            }

            socket.leaveGroup(group);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }
}
