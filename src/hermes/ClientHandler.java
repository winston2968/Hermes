package hermes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.List;


public class ClientHandler implements Runnable {

    private Socket client ;
    private ObjectInputStream in ;
    private ObjectOutputStream out ;
    private String username = null ;
    // private String ipAddress
    private Package packet ;
    private ServerHermes server ;
    private List<ClientHandler> clientsList ; 

    // =====================================================================
    //                          Constructor
    // =====================================================================

    public ClientHandler (Socket client, ServerHermes server, List<ClientHandler> clients, Package serverPaquet) throws IOException {
        // Add link to clients list for routing messages
        this.clientsList = clients ;

        // Create new client instance with in/out objects
        this.client = client ;
        this.server = server ;
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
        System.out.println("Hermes-Server:/$ Secure connexion established !");

        // Getting username 
        try {
            this.username = this.packet.decipherToStringAES((byte[]) this.in.readObject());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // =====================================================================
    //                          Thread Always running
    // =====================================================================

    @Override
    public void run() {
        // Loop for listening client input 
        byte[][] receveidMessage ;
        try {
            while ((receveidMessage = ((byte[][]) this.in.readObject())) != null) {
                // Getting destinator/sender username 
                String sender = this.packet.decipherToStringAES(receveidMessage[1]);
                String destinator = this.packet.decipherToStringAES(receveidMessage[0]);
                if (destinator.equals("all")) {
                    this.broadcast(receveidMessage);
                } else {
                    // Find the destinator and sending him the message 
                    synchronized(this.clientsList) {
                        System.out.println("Message received from : " + sender);
                        for (int i = 0 ; i < this.clientsList.size(); i++) {
                            if (this.clientsList.get(i).username.equals(destinator)) {
                                this.clientsList.get(i).out.writeObject(receveidMessage);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Hermes-Server:/$ Error while reading client entry");
            System.err.println();
            this.closeConnection();
        }
    }

    public void broadcast(byte[][] message) {
        synchronized(this.clientsList) {
            for (int i = 0 ; i < this.clientsList.size(); i++) {
                try {
                    this.clientsList.get(i).sendDatagramm(message);
                } catch (IOException e) {
                    System.out.println("Hermes-Server:/$ Error while broadcasting message...");
                    e.printStackTrace();
                    // Removing client 
                    this.server.removeClient(this.clientsList.get(i));
                }
            }

        }
    }

    // =====================================================================
    //                          ----
    // =====================================================================


    public void sendDatagramm(byte[][] datagram) throws IOException {
        this.out.writeObject(datagram);
    }

    public void closeConnection() {
        // Remove client from general server clients list 
        this.server.removeClient(this);
        // Properly remove client and close in/out
        try {
            if (this.in != null) this.in.close();
            if (this.out != null) this.out.close();
            if (this.client != null) this.client.close();
        } catch (Exception e) {
            System.out.println("Hermes-Server:/$ Client still not answer..removing...");
        }
    }
    
}
