package hermes;

import chatty.Datagram;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerHermes {
    
    // Server features 
    private ServerSocket server ;
    private String ipAddress ;
    private static final int PORT = 1234 ;
    private Package packet ;

    // Client management
    private BlockingQueue<String> receveidMessages ;
    private List<ClientHandler> clientsList = Collections.synchronizedList(new ArrayList<>());
 

    
    // =====================================================================
    //                          Constructor
    // =====================================================================

    public ServerHermes() {
        // Starting server
        try {
            this.server = new ServerSocket(PORT);
        } catch (Exception e) {
            System.out.println("Hermes-Server:/$ Error while starting server.");
            System.exit(0);
        }

        // Getting current ip address
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
            System.out.println("Hermes-Server:/$ Error while getting ip adress");
            error.printStackTrace();
            System.exit(0);
        }

        // Initializing client management tools
        this.receveidMessages = new LinkedBlockingQueue<>();

        // Setting local ipAddress for testing 
        this.ipAddress = "127.0.0.1";
    }



    // =====================================================================
    //                          Clients management
    // =====================================================================


    public void runServer() {

        // Initializing accepting client thread
        Thread listenningNewClients = new Thread(() -> {
            while (true) {
                // Accepting client and create new client instance
                // stored in clientsThreads list 
                System.out.println("Hermes-Server:/$ Waiting for client connexions...");
                try {
                    Socket client = this.server.accept();
                    // Creating new client handler for this client 
                    ClientHandler clientInstance = new ClientHandler(client, this, this.receveidMessages);
                    // Adding the new connected client to thee client list
                    this.clientsList.add(clientInstance);
                    System.out.println("Hermes-Server:/$ Adding new client !");
                } catch (Exception e) {
                    System.out.println("Hermes-Server:/$ Error while accepting new client");
                    e.printStackTrace();
                }
            }
        });






        // Launching accepting clients thread
        listenningNewClients.start();
    }


    public void removeClient(ClientHandler client) {
        synchronized (this.clientsList) {
            this.clientsList.remove(client);
            System.out.println("Hermes-Server:/$ Client removed.");
        }
    }



    // =====================================================================
    //                          Tests
    // =====================================================================

    public static void main(String[] args) {
        ServerHermes hermy = new ServerHermes();
        hermy.runServer();
    }
}
