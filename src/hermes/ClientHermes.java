package hermes;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import chatty.Datagram;

public class ClientHermes {


    private Socket socket ;
    private ObjectInputStream in ;
    private ObjectOutputStream out ;
	private String username ; 
    private String serverIpAddress = "127.0.0.1";
    private final static int PORT = 1234 ;
	// private Datagram datagram ;

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
            System.exit(0);
        }
        System.out.println("Hermes-Client:/$ Connected to the server !");
    } 


    public static void main(String[] args){
        ClientHermes clienty = new ClientHermes();

    }
    
}
