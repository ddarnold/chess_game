package multiplayer;

public interface GameDiscoveryListener {
    void onServerDiscovered(String serverAddress, String serverPort);
}
