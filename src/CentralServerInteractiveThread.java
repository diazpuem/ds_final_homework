import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class CentralServerInteractiveThread extends Thread {

    private final ServerSocket serverSocket;
    private final CentralServer centralServer;

    public CentralServerInteractiveThread(ServerSocket serverSocket, CentralServer centralServer) {
        this.serverSocket = serverSocket;
        this.centralServer = centralServer;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        String textInput;
        while(true) {
            textInput = scanner.nextLine();
            if (textInput.equals("members")) {
                centralServer.printPeerList();
            } else if (textInput.equals("quit")) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
    }
}
