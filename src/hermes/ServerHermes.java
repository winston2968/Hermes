package hermes;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

    /**
     * Server management/package routing
     * @author winston2968
     * @version 1.0
     */

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

    /**
     * Constructor for ServerHermes. 
     * It instanciate the socket and set up security features. 
     */
    public ServerHermes() {
        System.out.println(
            "  _   _                                    \n" +
            " | | | |  ___   _ __   _ __ ___    ___   ___ \n" +
            " | |_| | / _ \\ | '__| | '_ ` _ \\  / _ \\ / __|\n" +
            " |  _  ||  __/ | |    | | | | | ||  __/ \\__ \\\n" +
            " |_| |_| \\___| |_|    |_| |_| |_| \\___| |___/\n"
        );
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

    /**
     * Create a dephered datagram to send it on the socket. 
     * @param message
     * @param username
     * @param destinator
     * @return ciphered datagram
     * @throws Exception
     */
    public byte[][] createDatagram(String message, String username, String destinator) throws Exception {
        return this.packet.cipherMessageAES(username, destinator, message);
    }

    // =====================================================================
    //                          Clients management
    // =====================================================================


    /**
     * Method to start the server. 
     * It listen clients connexion requests and, 
     * when a new client arrives, starts a Thread to listen
     * its entries. 
     */
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
                    this.updateConnectedClients();
                    System.out.println("Hermes-Server:/$ ConnectedClients list sent to all clients !");
                    System.out.print("Hermes-Server:/$ ");
                } catch (Exception e) {
                    System.out.println("Hermes-Server:/$ Error while accepting new client");
                    // System.err.println(e);
                    // e.printStackTrace();
                }
            }
        });

        // Launching accepting clients thread
        listenningNewClients.start();

        // Launching cli tool 
        Thread commandThread = new Thread(this.cliTool);
        commandThread.start();
    }

    /* 
    // Properly stop the server by closing main socket
    public void stopServer() {
        try {
            this.server.close();
        } catch (Exception e) {
            System.out.println("Hermes-Server:/$ Failed to stop server properly...");
        }
    } */

    /**
     * Method to send the current connected 
     * clients list to all clients. 
     */
    public void updateConnectedClients() {
        synchronized (this.clientsList) {
            // Convert client list to String[] of client's username
            String[] usernameArray = new String[this.clientsList.size()];
            for (int i = 0; i < this.clientsList.size(); i++) {
                usernameArray[i] = this.clientsList.get(i).getUsername();
            }

            // Send clients array to all clients 
            String messageString = "/update-clients-separator-" + Arrays.toString(usernameArray);
            System.out.println(messageString);
            try {
                byte[][] datagram = this.packet.cipherMessageAES("Server", "all", messageString);
                for (int i = 0 ; i < this.clientsList.size(); i++) {
                    this.clientsList.get(i).sendDatagramm(datagram);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e);
            }
            
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
