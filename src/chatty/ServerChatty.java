package chatty;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.PublicKey;
import java.util.Enumeration;
import java.util.Scanner;


public class ServerChatty {
    
    private ServerSocket server ;
    private Socket client ;
    private ObjectInputStream in ;
    private ObjectOutputStream out ;
    private String ipAddress ;
    private static final int PORT = 1234 ;
    private Scanner scan ;
    private String username ;
    private Datagram datagram ;


    public ServerChatty() {
        this.scan = new Scanner(System.in);
		// Setting usename 
		System.out.println("Enter username for chatting...");
		System.out.print("Chatty:/$ ");
		this.username = this.scan.nextLine();
        // Initializing server
        try {
            this.server = new ServerSocket(PORT);
            System.out.println("Chatty:/$ Server succesfully started !");
            // Waiting for client connection
            System.out.println("Chatty:/$ Waiting for clients connections...");
            this.client = this.server.accept();
            System.out.println("Chatty:/$ Client connected !");
            // Setting in/out 
            this.out = new ObjectOutputStream(client.getOutputStream());
            this.in = new ObjectInputStream(client.getInputStream());
        } catch (Exception e) {
            System.out.println("Chatty:/$ Error while setting server or accepting client");
            System.out.println("Chatty:/$ Exiting...");
            System.exit(0);
        }


        // Setting datagrams and security features 
        this.datagram = new Datagram() ;

        try {
            // Sending actual public key to client 
            this.out.writeObject(this.datagram.getPublicKey());
            // Get client public key 
            this.datagram.setHisPublicKey( (PublicKey) this.in.readObject());
        } catch (Exception e) {
            System.out.println("Chatty:/$ Error while sendding/getting public key...");
            System.err.println(e);
        }

    }

    public void setAddress() {
        // Get current ip address
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
            System.out.println("Error while getting ip adress");
            error.printStackTrace();
        }
    }

    public void chat() {
        System.out.println("Launched !");

        // Initializing Listening Thread
        Thread listeningThread = new Thread(() -> {
			byte[] receveidMessage ;
			try {
				while ((receveidMessage = ((byte[]) this.in.readObject())) != null) {
                    // Extract datagram informations 
                    String[] infos  = this.datagram.byteToString(receveidMessage);
                    // String date = infos[0];
                    String time = infos[1];
                    String name = infos[2];
                    String msg = infos[4];
                    if (msg.equals("exit")) {
                        System.out.print("\r\033[K");
                        System.out.println("Chatty:/$ Partner exiting, exit...");
                        try {
                            Thread.sleep(1000); /// wainting client to exiting
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.exit(0);
                    } else {
                        // Detele old writed line 
                        System.out.print("\r\033[K"); // Delete actuel terminal line
                        // System.out.print("\033[1A\033[2K"); // 1A: moving up, 2K: delete all line
                        System.out.println(time + "-" + name + ":/$ " + msg);
                        System.out.print("\nChatty:/$ ");
                    }
				}
			} catch (Exception e) {
				System.out.println("Chatty:/$ Error while receiving message");
				e.printStackTrace();
			}
		});

        listeningThread.start();

		// Loop for chatting 
        String msg = "" ;
        while (!msg.equals("exit")) {
            System.out.print("\nChatty:/$ ");
            msg = this.scan.nextLine();
            try {
                // Create datagram to send message 
			    byte[] datas = this.datagram.stringToByte(msg, this.username,"partner");
                this.out.writeObject(datas);
                // Delete entry line and write history
                System.out.print("\033[1A\033[2K");
                System.out.println(this.username + ":/$" + msg);
            } catch (Exception e) {
               System.out.println("Chatty:/$ Error while encrypting datagram or sending message.");
            }
        }
    }

    public static void main(String[] args) {
        ServerChatty server = new ServerChatty();
        server.chat();
    }

}
