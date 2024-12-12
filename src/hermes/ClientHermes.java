package hermes;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

    /**
     * Class to connect a client to Hermes server in command-line
     * @author winston2968
     * @version 1.0
     */

public class ClientHermes {


    private Socket socket ;
    private ObjectInputStream in ;
    private ObjectOutputStream out ;
	private String username ; 
    private String serverIpAddress = "127.0.0.1";
    private static final int PORT = 1234 ;
	private Package packet ;
    private Scanner scan ;
    private String[] serverCommands = {"/disconnect","/update-clients"};
    private String[] connectedClients ;
    private Boolean inputLoop = true ;
    private Map<String, Runnable> clientCommands ;

    // =====================================================================
    //                          Constructor
    // =====================================================================

    /**
     * ClientHermes constructor. 
     * It starts the clients and try to connect to HermesServer. 
     * When it's done, it instanciate security features.
     */
    public ClientHermes() {

        System.out.println(
    "  _   _                                       ____ _     ___ \n" +
    " | | | |  ___   _ __   _ __ ___    ___   ___ / ___| |   |_ _|\n" +
    " | |_| | / _ \\ | '__| | '_ ` _ \\  / _ \\ / __| |   | |    | | \n" +
    " |  _  ||  __/ | |    | | | | | ||  __/ \\__ \\ |___| |___ | | \n" +
    " |_| |_| \\___| |_|    |_| |_| |_| \\___| |___/\\____|_____|___|\n"
);
        // Setting username 
        this.scan = new Scanner(System.in);
        System.out.print("Hermes-Client:/$ Enter username for chatting : ");
        this.username = scan.nextLine();

        /* 
        // Get server ip address
        System.out.print("Hermes-Client:/$ Enter server ip address : ");
        this.serverIpAddress = scan.nextLine();
        scan.close();
        */
        // this.scan.close();

        // Initializing socket and trying to connect to server
        try {
            this.socket = new Socket(serverIpAddress, PORT);
            // Setting in/out process
            this.out = new ObjectOutputStream(this.socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(this.socket.getInputStream());
        } catch (Exception e) {
            System.out.println("Hermes-Client:/$ Error while connecting to server...");
            e.printStackTrace();
        }

        // Setting security features 
        this.packet = new Package();
        // Exchange RSA keys
        try {
            // Get server RSA public key 
            this.packet.setHisPublicKey( (PublicKey) this.in.readObject());
			// Sending actual RSA public key to server 
            this.out.writeObject(this.packet.getPublicKey());
            // Get server AES key 
            this.packet.setAESCiphered((byte[]) this.in.readObject());
        } catch (Exception e) {
            System.out.println("Hermes-Client:/$ Error while securing connexion...exiting.");
            e.printStackTrace();
            this.scan.close();
            System.exit(0);
        }
        System.out.println("Hermes-Client:/$ Secure connexion established !");

        // Sending username to server for packages identification
        try {
            this.out.writeObject(this.packet.cipherStringAES(this.username));
        } catch (Exception e) {
            System.out.println("Hermes-Client:/$ Unable to send username...exiting");
            System.err.println(e);
            e.printStackTrace();
            System.exit(0);
        }

        /* 
        * If you want to see the AES key at the client connexion, uncomment 
        * those lines ...

        byte[] secretKeyEncoed = this.packet.aesKey.getEncoded();
        StringBuilder hexString = new StringBuilder();
        for (byte b : secretKeyEncoed) {
            hexString.append(String.format("%02X", b));
        }
        System.out.println("Clé AES : " + hexString);
        */

        // Initializing clientCommands 
        this.clientCommands = new HashMap<>();
        this.clientCommands.put("/disconnect",this::disconnect);
        this.clientCommands.put("/listConnected",this::listConnected);
        

    } 

    

    
    // =====================================================================
    //                          Chatting method
    // =====================================================================

    /**
     * Method for chatting on Hermes. 
     * It contains a Thread to listen Socket input 
     * and a loop for client message input. 
     */
    public void chat() {
        // Initializing Listening Thread
        Thread listeningThread = new Thread(() -> {
			byte[][] receivedDatagram ;
			try {
				while ((receivedDatagram = ((byte[][]) this.in.readObject())) != null) {
                    // Extracting datagram content 
                    String[] decipheredDatagram = this.packet.decipherMessageAES(receivedDatagram);
                    String partnerString = decipheredDatagram[1];
                    String message = (decipheredDatagram[2].split(";"))[2];
                    String time = (decipheredDatagram[2].split(";"))[1];
                    // Getting command if it's a command message
                    String[] messageCut = message.split("-separator-");
                    if (messageCut[0].equals(this.serverCommands[0]) && partnerString.equals("Server")) {
                        this.disconnect();
                    } else if (messageCut[0].equals(this.serverCommands[1]) && partnerString.equals("Server")) {
                        String[] clients = ((messageCut[1].replace("[","")).replace("]","")).split(",");
                        this.updateConnectedClients(clients);
                    } else {
                         // Detele old writed line 
                        System.out.print("\r\033[K");
                        // System.out.print("\033[1A\033[2K"); // 1A: moving up, 2K: delete all line
                        System.out.println(time + "-" + partnerString + ":/$ " + message);
                        System.out.print("Hermes-Client:/$ ");       
                    }
				}
			} catch (Exception e) {
				System.out.println("Hermes-Client:/$ Error while receiving message");
                System.out.println("Hermes-Client:/$ Exiting...");
                System.exit(0);
				// e.printStackTrace();
			}
		});

        listeningThread.start();

        // Loop for chatting 
        String msg = "" ;
        while (this.inputLoop) {
            System.out.print("\nHermes-Client:/$ ");
            msg = scan.nextLine();
            // Checking if we don't type a command
            String msgClean = msg.replace(" ", "");
            if (this.clientCommands.containsKey(msgClean)) {
                Runnable action = this.clientCommands.get(msgClean);
                action.run();
            } else {
                // Create datagram to send message 
                try {
                    byte[][] datagram = this.packet.cipherMessageAES(this.username, "all", msg);
                    this.out.writeObject(datagram);
                    // Delete entry line and write history
                    System.out.print("\033[1A\033[2K");
                    System.out.println(this.username + ":/$" + msg);
                } catch (Exception e) {
                    System.out.println("Hermes-Client:/$ Error while encrypt datagram or sendding datas...");
                }
            }
        }
        // Exiting the app
        this.scan.close();
        System.exit(0);
    }

    /**
     * Method to update current connected clients list
     * @param clients
     */
    public void updateConnectedClients(String[] clients) {
        this.connectedClients = new String[clients.length];
        for (int i = 0 ; i < clients.length; i++) {
            this.connectedClients[i] = (clients[i].replace(" ", ""));
        }
    }

    /**
     * Method to disconnect properly from the server. 
     */
    public void disconnect() {
        // Disconnect from server
        try {
            this.in.close();
            this.out.close();
            this.socket.close();
            System.out.println("Hermes-Client:/$ Disconnected from server. Exiting...");
        } catch (Exception e) {
            System.out.println("Failed to close connexion properly. Exiting...");
        }
    }

    /**
     * List the contain of connected clients list. 
     */
    public void listConnected() {
        if (this.connectedClients.length > 1) {
            System.out.println("Hermes-Client:/$ Connected Clients :");
            for (String client : this.connectedClients) {
                if (!client.equals(this.username)) {
                    System.err.println("+----- " + client);
                }
            }
        }
    }


    public static void main(String[] args){
        ClientHermes clienty = new ClientHermes();
        clienty.chat();

    }
    
}
