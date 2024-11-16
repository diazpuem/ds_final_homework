import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class Peer {
    private final List<String> neighbors;
    private final AtomicInteger counter;
    private final String myIp;
    private final String serverHost;
    public static final int LIMIT_TRIES = 4;
    public static final int PORT = 8080;

    public Peer(String serverHost) throws UnknownHostException {
        this.myIp = InetAddress.getLocalHost().getHostAddress();
        this.counter = new AtomicInteger(0);
        this.neighbors =  new ArrayList<>();
        this.serverHost = serverHost;
    }

    public static void main(String[] args) throws UnknownHostException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter Server IP address: ");
        String host = scanner.nextLine();
        System.out.println("Connecting to server " + host);
        Peer peerObj = new Peer(host);

        // CONNECTING TO CENTRAL SERVER
        try (Socket socket = new Socket(host, PORT)) {
            peerObj.registerToCentralServer(socket);
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + PORT);
        }
        System.out.println("Waiting on new connections for joining");
        try  {
            ServerSocket serverSocket = new ServerSocket(PORT);
            new PeerInteractiveThread(serverSocket, peerObj).start();
            while (serverSocket.isBound()) {
                Socket socket = serverSocket.accept();
                new PeerThread(socket, peerObj).start();
            }
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Server exception " + e.getMessage());
        }
    }

    private void registerToCentralServer(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        writer.println("add");
        String receivedMessage = reader.readLine();
        System.out.println("Received Message from Server " + receivedMessage);
        socket.close();
        if (!receivedMessage.equals("first")) {
            connectToPeer(receivedMessage, 0);
        } else {
            System.out.println("Invalid message received");
        }
    }

     public void receiveConnection(Socket socket, String message) throws IOException {
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        String ipReceived = Peer.getIpAddress(socket);
        if (message.equals("remove")) {
            System.out.println("Received a request to removal from " + ipReceived);
            this.removePeerFromMyNeighbors(ipReceived);
        } else if (message.equals("add")) {
            if (this.counter.get() < 3) {
                System.out.println("Received a request to connect from " + ipReceived + " and accepting it");
                writer.println("ok");
                this.neighbors.add(ipReceived);
                this.counter.set(this.counter.get() + 1);
            } else {
                System.out.println("Received a request but my neighbors are full to connect peer " + ipReceived);
                Random rand = new Random();
                int randomPeer = rand.nextInt(neighbors.size() - 1);
                String peerToReturn = neighbors.get(randomPeer);
                System.out.println("Returning one of my random neighbors " + peerToReturn +" to " + ipReceived);
                writer.println(peerToReturn);
            }
        } else {
            System.out.println("Invalid message received");
        }
    }

    private void connectToPeer(String peer, int tries) {
        if (tries == LIMIT_TRIES) {
            System.out.println("Limit Exceeded for connections to peers from " + myIp);
        } else {
            String newPeer = "";
            try (Socket socket = new Socket(peer, PORT)) {
                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                System.out.println("Connecting to: " + peer);
                writer.println("add");
                String receivedMessage = reader.readLine();
                if (receivedMessage.equals("ok")) {
                    neighbors.add(peer);
                    counter.set(counter.get() + 1);
                    System.out.println("Connected to Peer " + peer);
                } else {
                    newPeer = receivedMessage;
                }
            } catch (UnknownHostException e) {
                System.out.println("Unknown host: " + peer + " error: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("I/O Exception: " + e.getMessage());
            }
            if (!newPeer.isEmpty()) {
                connectToPeer(newPeer, ++tries);
            }
        }
    }

    public void printNeighbors() {
        if (neighbors.isEmpty()) {
            System.out.println("Peer list is empty");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String peer : neighbors) {
                sb.append(peer).append("\n");
            }
            System.out.println(sb);
        }
    }

    public void removePeerFromMyNeighbors(String peer) {
        this.neighbors.remove(peer);
        System.out.println("Removed Peer: " + peer );
    }

    public void removeMyself() {
        // Remove myself from central server
        System.out.println("Removing myself from central server");
        sendRemovePeer(serverHost);
        // Remove myself from my neighbors
        System.out.println("Removing myself from my neighbors");
        for (String address : this.neighbors) {
            sendRemovePeer(address);
        }
    }

    private void sendRemovePeer(String destination) {
        try (Socket socket = new Socket(destination, PORT)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println("remove");
            System.out.println("Sending request to remove myself to " + destination);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + destination + " error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O Exception: " + e.getMessage());
        }
    }

    private static String getIpAddress(Socket socket) {
        String socketName = socket.getRemoteSocketAddress().toString();
        return socketName.substring(1, socketName.indexOf(":"));
    }
}
