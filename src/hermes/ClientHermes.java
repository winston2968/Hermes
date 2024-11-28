package hermes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Scanner;

public class ClientHermes {


    private Socket socket ;
    private ObjectInputStream in ;
    private ObjectOutputStream out ;
	private String username ; 
    private String serverIpAddress = "127.0.0.1";
    private final static int PORT = 1234 ;
	private Package packet ;

    public ClientHermes() {
        // Setting username 
        Scanner scan = new Scanner(System.in);
        System.out.print("Hermes-Client:/$ Enter username for chatting : ");
        this.username = scan.nextLine();

        /* 
        // Get server ip address
        System.out.print("Hermes-Client:/$ Enter server ip address : ");
        this.serverIpAddress = scan.nextLine();
        scan.close();
        */

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
        System.out.println("Hermes-Client:/$ Connected to the server !");

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
            System.exit(0);
        }
        System.out.println("Hermes-Client:/$ Secure connexion established !");

    } 


    public static void main(String[] args){
        ClientHermes clienty = new ClientHermes();

    }
    
}
