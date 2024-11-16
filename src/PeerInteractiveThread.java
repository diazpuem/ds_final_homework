import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class PeerInteractiveThread extends Thread {
    private final ServerSocket serverSocket;
    private final Peer peer;

    public PeerInteractiveThread(ServerSocket serverSocket, Peer peer) {
        this.serverSocket = serverSocket;
        this.peer = peer;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        String textInput;
        while (true) {
            textInput = scanner.nextLine();
            if (textInput.equals("quit")) {
                // remove connections with the other guys
                System.out.println("Removing Peer from network. Good bye");
                peer.removeMyself();
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            } else if (textInput.equals("neighbors")) {
                peer.printNeighbors();
            }
        }
    }
}
