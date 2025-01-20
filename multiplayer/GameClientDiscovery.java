package multiplayer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class GameClientDiscovery implements Runnable {
    private final int listenPort = Constants.MULTICAST_GROUP_PORT;
    private boolean running = true;
    private GameDiscoveryListener listener; // Listener for server discovery

    public void setListener(GameDiscoveryListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try (MulticastSocket socket = new MulticastSocket(listenPort)) {
            InetAddress group = InetAddress.getByName(Constants.MULTICAST_GROUP_ADDRESS);
            socket.joinGroup(group);

            while (running) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Discovered: " + message);

                // Extract and display server details
                String[] details = message.split(":");
                if (details.length == 3 && "GameServer".equals(details[0])) {
                    String serverAddress = details[1];
                    String serverPort = details[2];
                    System.out.println("Server found at " + serverAddress + ":" + serverPort);

                    // Notify listener
                    if (listener != null) {
                        listener.onServerDiscovered(serverAddress, serverPort);
                    }
                }
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
