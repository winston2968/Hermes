package hermes;

import chatty.Datagram;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;


public class ClientHandler extends Thread {

    private Socket client ;
    private ObjectInputStream in ;
    private ObjectOutputStream out ;
    // private String username ;
    // private String ipAddress
    private Package packet ;
    public BlockingQueue<String> messagesQueue ;
    private ServerHermes server ;

    // =====================================================================
    //                          Constructor
    // =====================================================================

    public ClientHandler (Socket client, ServerHermes server, BlockingQueue<String> serverReceveidMessages) throws IOException {
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

        // Connect new client to general receveid messages queue
        this.messagesQueue = serverReceveidMessages ;

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
                this.messagesQueue.add(receveidMessage);
            }
        } catch (Exception e) {
            System.out.println("Hermes:/$ Error while reading client entry");
            System.err.println();
        } finally {
            this.closeConnection();
        }

        
    }

    // =====================================================================
    //                          Thread Always running
    // =====================================================================


    public void sendDatagramm(Datagram data) throws IOException {
        this.out.writeObject(data);
    }

    public void closeConnection() {
        // Properly remove client and close in/out
        try {
            if (this.in != null) this.in.close();
            if (this.out != null) this.out.close();
            if (this.client != null) this.client.close();
        } catch (Exception e) {
            System.out.println("Hermes:/$ Client still not answer..removing...");
        }
        // Remove client from general server clients list 
        this.server.removeClient(this);
    }
    
}
