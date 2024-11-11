package chatty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientChatty {

	private String serverAddress ;
	private Socket client ;
	private BufferedReader in ;
	private PrintWriter out ;
	private String partnerName ;

	public ClientChatty() {
		
	}

	public void connectToServer() {
		System.out.println("Hermes/:$ Trying to connect to server...");
		try {
			this.client = new Socket(this.serverAddress, 4444);
			this.out = new PrintWriter(client.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader (client.getInputStream()));
			System.out.println("Hermes/:$ Connection Established ! !");
		} catch (Exception e) {
			System.out.println(e.toString());
			System.out.println("Hermes/:$ Error : Connection Failed !");
			System.exit(0);
		}
	}

	public void initConnection() {
		// Init connection 
		Scanner scan = new Scanner(System.in);
		System.out.print("Hermes/:$ Enter server ip address : ");
		String address = scan.next();
		this.serverAddress = address ;
		scan.close();
		this.connectToServer();

		/* 
		// Choose partner name 
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
			*/
	}

	public void chat() {

		// Queue to memorize receveid messages 
		BlockingQueue<String> messagesQueue = new LinkedBlockingQueue<>();

		// Thread to listen receveid messages
		Thread readingThread = new Thread(() -> {
			String receveidMessage ;
			try {
				Thread.sleep(10);
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
			while (!Thread.currentThread().isInterrupted()) {
				try {
					String message = messagesQueue.take();
					System.out.println(this.partnerName + "/:$ " + message);
					System.out.print("Hermes/:$ ");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();  // Rétablir l'interruption
				}
			}
		}); 

		readingThread.start();
		displayThread.start();

		// Loop to get messages
		try {
			while (true) {
				Scanner scan = new Scanner(System.in);
				System.out.print("Entrez votre message (ou 'exit' pour quitter) : ");
				String message = scan.nextLine();  // Utilisation de nextLine()
				if (message.equalsIgnoreCase("exit")) {
					System.out.println("Fermeture du chat.");
					break;
				}
				this.out.println(message);  // Envoi du message
			}
		} catch (NoSuchElementException | IllegalStateException e) {
			System.out.println("Le flux d'entrée utilisateur semble être fermé.");
		}

	}


    public static void main(String[] args) {
        ClientChatty client = new ClientChatty();
		client.initConnection();
		client.chat();
    }

}
