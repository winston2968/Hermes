package chatty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.io.InputStreamReader;

public class ServerChatty {
    
    private String ipAddress ;
    private ServerSocket server ;
    private Socket client ;
    private BufferedReader in ;
    private PrintWriter out ;


    public ServerChatty() {
        // Get all connection interfaces
        Enumeration<NetworkInterface> e;
        try {
            e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = e.nextElement();
                Enumeration<InetAddress> ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    // Get each interface ipAdress
                    InetAddress i = ee.nextElement();
                    String current = i.getHostAddress();
                    // We choose an ipAddress
                    String[] cut = current.split("\\.");
                    if (cut.length == 4 && ! current.equals("127.0.0.1")) {
                        this.ipAddress = current ;
                    }
                }
            }

        } catch (SocketException error) {
            System.out.println("Hermes/:$ --- /!\\ Error while getting ipAddress /!\\");
            error.printStackTrace();
        }
        
    }

    public void connectToClient() {
        System.out.println("Hermes/:$ trying to connect to client...");
        try {
            this.server = new ServerSocket(4444);
			this.client = this.server.accept();
			this.in = new BufferedReader(new InputStreamReader (client.getInputStream()));
			this.out = new PrintWriter(client.getOutputStream(), true);
			System.out.println("Hermes/:$ Connection established !");
        } catch (Exception e) {
            System.out.println("Hermes/:$ Error : Connection failed !");
            System.exit(0);
        }
    }

    public void sendToClient(String text) {
        this.out.println(text);
    }

    public String readFromClient() {
        try {
            return this.in.readLine();
        } catch (IOException e) {
            return "";
        }
    }

    public String getIpAddress() {
        return this.ipAddress;
    }








    public static void main(String[] args) {
        ServerChatty server = new ServerChatty();
        // System.out.println(server.ipAddress);
    }


}
