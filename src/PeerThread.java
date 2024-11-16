import java.io.*;
import java.net.Socket;
import java.util.Random;

public class PeerThread extends Thread {
    private final Socket socket;
    private final Peer peer;

    public PeerThread(Socket socket, Peer peer) {
        this.socket = socket;
        this.peer = peer;
    }

    public void run() {
        try {
            InputStream input = this.socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String textReceived = reader.readLine();
            this.peer.receiveConnection(socket, textReceived);
        } catch (IOException e) {
            System.out.println("Peer Exception: " + e.getMessage());
        }
    }
}
