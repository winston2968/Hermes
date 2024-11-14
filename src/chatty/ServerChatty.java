package chatty;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class ServerChatty {
    
    private ServerSocket server ;
    private Socket client ;
    private DataInputStream in ;
    private PrintStream out ;
    private String ipAddress ;
    private static final int PORT = 1234 ;
    private Scanner scan ;


    public ServerChatty() {
        this.scan = new Scanner(System.in);
        // Initializing server
        try {
            this.server = new ServerSocket(PORT);
            System.out.println("Server succesfully started !");
            // Waiting for client connection
            System.out.println("Waiting for clients connections...");
            this.client = this.server.accept();
            System.out.println("Client accepted !");
            // Setting in/out 
            this.in = new DataInputStream(new BufferedInputStream(this.client.getInputStream()));
            this.out = new PrintStream(this.client.getOutputStream());
        } catch (Exception e) {
            System.out.println("Error while setting server or accepting client");
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

        // Initializing Listening Thread
        Thread listeningThread = new Thread(() -> {
			String receveidMessage ;
			try {
				while ((receveidMessage = this.in.readUTF()) != null) {
					System.out.println("\n Client# " + receveidMessage);
				}
			} catch (Exception e) {
				System.out.println("Erreur lors de la réception du message");
				e.printStackTrace();
			}
		});
        listeningThread.start();

        // Loop for chatting
        String msg = "" ;
        while (!msg.equals("exit")) {
            System.out.print("Message à envoyer : ");
            msg = scan.nextLine();
            this.out.println(msg);
            
        }
    }





    public static void main(String[] args) {
        ServerChatty server = new ServerChatty();
        server.chat();
    }

}
