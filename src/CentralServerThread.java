import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class CentralServerThread extends Thread {

    private final Socket socket;
    private final CentralServer centralServer;

    public CentralServerThread(Socket socket, CentralServer centralServer) {
        this.socket = socket;
        this.centralServer = centralServer;
    }

    public void run() {
        try {
            InputStream input = this.socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String textReceived = reader.readLine();
            this.centralServer.receiveConnection(socket, textReceived);
        } catch (IOException e) {
            System.out.println("Central Server Exception: " + e.getMessage());
        }
    }
}
