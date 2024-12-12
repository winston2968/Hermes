package hermes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.List;

    /**
     * Client management, server-side
     * @author winston2968
     * @version 1.0
     */


public class ClientHandler implements Runnable {

    private Socket client ;
    private ObjectInputStream in ;
    private ObjectOutputStream out ;
    private String username = null ;
    // private String ipAddress
    private Package packet ;
    private List<ClientHandler> clientsList ; 
    private Boolean running ;

    // =====================================================================
    //                          Constructor
    // =====================================================================

    /**
     * Client Handler constructor 
     * @param client
     * @param clients
     * @param serverPaquet
     * @throws IOException
     */

    public ClientHandler (Socket client, List<ClientHandler> clients, Package serverPaquet) throws IOException {
        // Add link to clients list for routing messages
        this.clientsList = clients ;

        // Create new client instance with in/out objects
        this.client = client ;
        this.out = new ObjectOutputStream(this.client.getOutputStream()) ; 
        this.in = new ObjectInputStream(this.client.getInputStream()) ;

         // Setting security features 
        this.packet = serverPaquet ;
        // Exchange RSA keys
        try {
			// Sending actual RSA public key to server 
            this.out.writeObject(this.packet.getPublicKey());
            // Get server RSA public key 
            this.packet.setHisPublicKey( (PublicKey) this.in.readObject());
            // Send to client actual AES ciphered key
            this.out.writeObject(this.packet.getAESCiphered());
        } catch (Exception e) {
            System.out.println("Hermes-Server:/$ Error while securing connexion...exiting.");
            e.printStackTrace();
            System.exit(0);
        }

        // Getting username 
        try {
            this.username = this.packet.decipherToStringAES((byte[]) this.in.readObject());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Allows thread to launch
        this.running = true ;
    }

    // =====================================================================
    //                          Getter and Setter
    // =====================================================================

    /**
     * Getter for client username 
     * @return username of corresponding connected client 
     */
    public String getUsername() {
        return this.username ;
    }

    // =====================================================================
    //                          Thread Always running
    // =====================================================================

    /**
     * Implemented method which listen socket from client. 
     * 
     */
    @Override
    public void run() {
        // Loop for listening client input 
        byte[][] receveidMessage ;
        try {
            while ((receveidMessage = ((byte[][]) this.in.readObject())) != null && this.running) {
                // Getting destinator/sender username 
                String sender = this.packet.decipherToStringAES(receveidMessage[1]);
                String destinator = this.packet.decipherToStringAES(receveidMessage[0]);
                if (destinator.equals("all")) {
                    this.broadcast(receveidMessage, sender);
                } else {
                    // Find the destinator and sending him the message 
                    synchronized(this.clientsList) {
                        for (int i = 0 ; i < this.clientsList.size(); i++) {
                            if (this.clientsList.get(i).username.equals(destinator)) {
                                this.clientsList.get(i).out.writeObject(receveidMessage);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            this.closeConnection();
        }
    }

    /**
     * Method to broadcast messages to all other connected clients on the server
     * @param message
     * @param exclude
     */
    public void broadcast(byte[][] message, String exclude) {
        synchronized(this.clientsList) {
            for (int i = 0 ; i < this.clientsList.size(); i++) {
                try {
                    // Sendding broadcast message to clients differents from sender
                    if (!this.clientsList.get(i).getUsername().equals(exclude)) {
                        this.clientsList.get(i).sendDatagramm(message);
                    }
                } catch (IOException e) {
                    System.out.println("Hermes-Server:/$ Error while broadcasting message...");
                    e.printStackTrace();
                    this.clientsList.get(i).closeConnection();
                }
            }

        }
    }

    // =====================================================================
    //                       Socket Interactions 
    // =====================================================================


    /**
     * Method to simply send a datagram to a connected client
     * @param datagram
     * @throws IOException
     */
    public void sendDatagramm(byte[][] datagram) throws IOException {
        this.out.writeObject(datagram);
    }

    /**
     * Method to close the current client connexion.  
     */
    public void closeConnection() {
        try {
            // Stop listening thread
            this.running = false ;
            // Remove client from general server clients list 
            this.clientsList.remove(this);
            // Properly remove client and close in/out
            if (this.in != null) this.in.close();
            if (this.out != null) this.out.close();
            if (this.client != null) this.client.close();
        } catch (Exception e) {
            System.out.println("Hermes-Server:/$ Client " + this.username + " still don't responding, removed");
        }
    }
    
}
