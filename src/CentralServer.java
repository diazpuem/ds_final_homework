import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class CentralServer {

    private final List<String> peerList = new ArrayList<>();
    public static final int PORT = 8080;

    public static void main(String[] args) {
        CentralServer centralServer = new CentralServer();
        System.out.println("Server is Running");
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(PORT);
            new CentralServerInteractiveThread(serverSocket, centralServer).start();
            while(serverSocket.isBound()) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection from " + socket.getRemoteSocketAddress());
                new CentralServerThread(socket, centralServer).start();
            }
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Server exception " + e.getMessage());
        }
    }

    public void receiveConnection(Socket socket, String peer) throws IOException {
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        if (peer.charAt(0) == 'r') {
            String peerHost = peer.substring(1);
            System.out.println("Received a request to removal from " + peerHost);
            this.removePeerFromMyList(peerHost);
            System.out.println("Peer " + peerHost + " removed from Central Server");
        } else {
            System.out.println("Received a request to add a new peer");
            writer.println(this.registerPeer(peer));
        }
    }

    private String registerPeer(String peer) {
        if (peerList.isEmpty()) {
            peerList.add(peer);
            return "First Registration. Peer Successfully Registered " + peer;
        } else {
            Random rand = new Random();
            int randomPeer = rand.nextInt(peerList.size() - 1);
            String peerToReturn = peerList.get(randomPeer);
            peerList.add(peer);
            System.out.println("Peer Sent in Return " + peerToReturn + " To Registered " + peer);
            return peerToReturn;
        }
    }

    private void removePeerFromMyList(String peer) {
        if (peerList.isEmpty()) {
            System.out.println("This node does not exist in central server");
        }
        this.peerList.remove(peer);
        System.out.println("Removed Peer: " + peer );
    }

    public void printPeerList() {
        if (peerList.isEmpty()) {
            System.out.println("Peer list is empty");
        }
        StringBuilder sb = new StringBuilder();
        for (String peer : peerList) {
            sb.append(peer).append("\n");
        }
        System.out.println(sb);
    }



}
