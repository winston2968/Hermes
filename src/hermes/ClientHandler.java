package hermes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.List;
import java.util.concurrent.BlockingQueue;


public class ClientHandler implements Runnable {

    private Socket client ;
    private ObjectInputStream in ;
    private ObjectOutputStream out ;
    // private String username ;
    // private String ipAddress
    private Package packet ;
    private ServerHermes server ;
    private List<ClientHandler> clientsList ; 

    // =====================================================================
    //                          Constructor
    // =====================================================================

    public ClientHandler (Socket client, ServerHermes server, List<ClientHandler> clients) throws IOException {
        // Add link to clients list for routing messages
        this.clientsList = clients ;

        // Create new client instance with in/out objects
        this.client = client ;
        this.server = server ;
        this.out = new ObjectOutputStream(this.client.getOutputStream()) ; 
        this.in = new ObjectInputStream(this.client.getInputStream()) ;

         // Setting security features 
        this.packet = new Package();
        // Exchange RSA keys
        try {
			// Sending actual RSA public key to server 
            this.out.writeObject(this.packet.getPublicKey());
            // Get server RSA public key 
            this.packet.setHisPublicKey( (PublicKey) this.in.readObject());
            // Send to client actual AES ciphered key
            this.out.writeObject(this.packet.getAESCiphered());
        } catch (Exception e) {
            System.out.println("Hermes-Client:/$ Error while securing connexion...exiting.");
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("Hermes-Client:/$ Secure connexion established !");

        
    }


    // =====================================================================
    //                          Thread Always running
    // =====================================================================

    @Override
    public void run() {
        // Loop for listening client input 
        String receveidMessage ;
        try {
            while ((receveidMessage = ((String) this.in.readObject())) != null) {
                // There is a new messages
                // We add it to the BlockingQueue
                System.out.println("Client-Handler:/$ Message received : " + receveidMessage);
                // this.messagesQueue.add(receveidMessage);
                this.broadcast(receveidMessage);
            }
        } catch (Exception e) {
            System.out.println("Hermes:/$ Error while reading client entry");
            System.err.println();
            this.closeConnection();
        }
    }

    public void broadcast(String message) {
        synchronized(this.clientsList) {
            for (int i = 0 ; i < this.clientsList.size(); i++) {
                try {
                    this.clientsList.get(i).sendDatagramm(message);
                } catch (IOException e) {
                    System.out.println("Hermes-Handler:/$ Error while broadcasting message...");
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


    public void sendDatagramm(String message) throws IOException {
        this.out.writeObject(message);
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
            System.out.println("Hermes:/$ Client still not answer..removing...");
        }
    }
    
}
