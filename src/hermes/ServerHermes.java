package hermes;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class ServerHermes {
    
    // Server features 
    private ServerSocket server ;
    private String ipAddress ;
    private static final int PORT = 1234 ;
    private Package packet ;
    private CommandListener cliTool ;
    private boolean isRunning ;

    // Client management
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

        // Allow server to start
        this.isRunning = true ;

        // Setting local ipAddress for testing 
        this.ipAddress = "127.0.0.1";

        // Initilizing package object to send AES keys and RSA keys
        this.packet = new Package();

        // Starting cli tool 
        this.cliTool = new CommandListener(this.clientsList, this);
    }

    // =====================================================================
    //                          Getters
    // =====================================================================

    public byte[][] createDatagram(String message, String username, String destinator) throws Exception {
        return this.packet.cipherMessageAES(username, destinator, message);
    }

    // =====================================================================
    //                          Clients management
    // =====================================================================



    public void runServer() {

        // Initializing accepting client thread
        Thread listenningNewClients = new Thread(() -> {
            System.out.println("Hermes-Server:/$ Server succesfully started !");
            while(this.isRunning) {
                // Accepting client and create new client instance
                // stored in clientsThreads list 
                try {
                    Socket client = this.server.accept();
                    // Creating new client handler for this client 
                    ClientHandler clientInstance = new ClientHandler(client, this.clientsList, this.packet);
                    // Adding the new connected client to thee client list
                    this.clientsList.add(clientInstance);
                    // Starting client handler listening thread
                    new Thread(clientInstance).start();
                    System.out.println("Hermes-Server-/$ New client added : " + clientInstance.getUsername());
                    System.out.print("Hermes-Server:/$ ");
                } catch (Exception e) {
                    System.out.println("Hermes-Server:/$ Error while accepting new client");
                    System.err.println(e);
                    e.printStackTrace();
                }
            }
        });

        // Launching accepting clients thread
        listenningNewClients.start();

        // Launching cli tool 
        Thread commandThread = new Thread(this.cliTool);
        commandThread.start();
    }

    // Properly stop the server by closing main socket
    public void stopServer() {
        try {
            this.server.close();
        } catch (Exception e) {
            System.out.println("Hermes-Server:/$ Failed to stop server properly...");
        }
    }

    // Send clients list to all clients connected
    public void updateConnectedClients() {
        // TODO : complete this method
    }



    // =====================================================================
    //                          Tests
    // =====================================================================

    public static void main(String[] args) {
        ServerHermes hermy = new ServerHermes();
        hermy.runServer();
    }
}
