package chatty;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientChatty {

	private Socket socket ;
	private Scanner scan ;
    private ObjectInputStream in ;
    private ObjectOutputStream out ;
	private String username ; 
	private Datagram datagram ;

	public ClientChatty(String serverAddress, int port) {
		this.scan = new Scanner(System.in);
		// Setting usename 
		System.out.println("Enter username for chatting...");
		System.out.print("Chatty:/$ ");
		this.username = this.scan.nextLine();
		// Trying to connect to the server
		try {
			this.socket = new Socket(serverAddress,port);
			System.out.println("Chatty:/$ Connected to the server !");
			// Setting in/out 
			this.in = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());

		} catch (Exception e) {
			System.out.println("Chatty:/$ Unable to connect to the server");
		}

		// Setting security features 
		this.datagram = new Datagram();
	}


    public void chat() {

		System.out.println("Launched !");

		// Initializing Listening Thread
        Thread listeningThread = new Thread(() -> {
			byte[] receveidMessage ;
			try {
				while ((receveidMessage = ((byte[]) this.in.readObject())) != null) {
					// Extract datagram infos 
					String[] infos  = this.datagram.byteToString(receveidMessage);
                    // String date = infos[0];
                    String time = infos[1];
                    String name = infos[2];
                    String msg = infos[4];
					if (msg.equals("exit")) {
                        System.out.print("\r\033[K");
                        System.out.println("Chatty:/$ Partner exiting, exit...");
                        System.exit(0);
                    } else {
						// Detele old writed line 
						System.out.print("\r\033[K");
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
			// Create datagram to send message 
			byte[] datas = this.datagram.stringToByte(msg, this.username, "partner");
			try {
				this.out.writeObject(datas);
			} catch (Exception e) {
			}
			// Delete entry line and write history
			System.out.print("\033[1A\033[2K");
			System.out.println(this.username + ":/$" + msg);
        }
    }



    public static void main(String[] args) {
        ClientChatty client = new ClientChatty("127.0.0.1",1234);
		client.chat();
    }

}
