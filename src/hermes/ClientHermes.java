package hermes;


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
    private static final int PORT = 1234 ;
	private Package packet ;
    private Scanner scan ;

    // =====================================================================
    //                          Constructor
    // =====================================================================

    public ClientHermes() {
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
    } 

    

    
    // =====================================================================
    //                          Chatting method
    // =====================================================================

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
                    // Detele old writed line 
                    System.out.print("\r\033[K");
                    // System.out.print("\033[1A\033[2K"); // 1A: moving up, 2K: delete all line
                    System.out.println(time + "-" + partnerString + ":/$ " + message);
                    System.out.print("Hermes-Client:/$ ");
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
        while (!msg.equals("exit")) {
            System.out.print("\nHermes-Client:/$ ");
            msg = scan.nextLine();
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
        // Exiting the app
        this.scan.close();
        System.exit(0);
    }


    public static void main(String[] args){
        ClientHermes clienty = new ClientHermes();
        clienty.chat();

    }
    
}
