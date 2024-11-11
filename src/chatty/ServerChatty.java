package chatty;

import java.io.BufferedReader;
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

public class ServerChatty {
    
    private String ipAddress ;
    private ServerSocket server ;
    private Socket client ;
    private BufferedReader in ;
    private PrintWriter out ;
    private String partnerName ; 


    public ServerChatty() {
        // Get all connection interfaces
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
            System.out.println("Hermes/:$ --- /!\\ Error while getting ipAddress /!\\");
            error.printStackTrace();
        }
        
    }

    public void connectToClient() {
        System.out.println("Hermes/:$ trying to connect to client...");
        try {
            this.server = new ServerSocket(4444);
			this.client = this.server.accept();
			this.in = new BufferedReader(new InputStreamReader (client.getInputStream()));
			this.out = new PrintWriter(client.getOutputStream(), true);
			System.out.println("Hermes/:$ Connection established !");
        } catch (Exception e) {
            System.out.println("Hermes/:$ Error : Connection failed !");
            System.exit(0);
        }
    }

    public void initConnection() {
        // Exchanges addresses
        System.out.println("Hermes/:$ Give this address to your partner : " + this.ipAddress);
        this.connectToClient();

        /* 
        // Choose partner name 
        Scanner scan = new Scanner(System.in);
		System.out.print("Hermes/:$ Choose name : ");
		String name = scan.next();
		// Send name to partner 
		this.out.println(name);
		// Get partner name 
		try {
			this.partnerName = this.in.readLine();
		} catch (Exception e) {
			System.err.println("""
			Hermes/:$ Impossible de récupérer le nom du partenaire...
			-> Remplacement par 'toto26' """);
			this.partnerName = "toto26";
		}
        scan.close();
        */
    }


	public void chat() {

		// Queue to memorize receveid messages 
		BlockingQueue<String> messagesQueue = new LinkedBlockingQueue<>();

		// Thread to listen receveid messages
		Thread readingThread = new Thread(() -> {
			String receveidMessage ;
			try {
				while ((receveidMessage = this.in.readLine()) != null) {
					messagesQueue.put(receveidMessage);  // Stocker les messages entrants dans la file
				}
			} catch (Exception e) {
				System.out.println("Hermes/:$ Erreur lors de la réception du message");
                e.printStackTrace();
			}
		});

		// Thread to display new messages without erase user entry
		Thread displayThread = new Thread(() -> {
			while (true) {
				try {
					// Get and display messages from the queue
					String message = messagesQueue.take();
					System.out.println(this.partnerName + "/:$ " + message);
					System.out.println("Hermes/:$"); // Re-display user entry
				} catch (Exception e) {
					Thread.currentThread().interrupt();
				}
			}
		});

		readingThread.start();
		displayThread.start();

		// Loop to get messages
        Scanner scan = new Scanner(System.in);
		while (true) {
            System.out.print("Entrez votre message (ou 'exit' pour quitter) : ");
            if (scan.hasNextLine()) {
                String message = scan.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Fermeture du chat.");
                    break;  // Quitter la boucle si l'utilisateur tape "exit"
                }
                this.out.println(message);  // Envoyer le message au correspondant
            } else {
                System.out.println("Pas d'entrée disponible.");
                break;  // Sortir si le flux est fermé
            }
        }
	}







    public static void main(String[] args) {
        ServerChatty server = new ServerChatty();
        server.initConnection();
        server.chat();
    }


}
