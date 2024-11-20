package chatty;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class ClientChatty {

	private Socket socket ;
	private Scanner scan ;
	private DataOutputStream out ;
	private BufferedReader in ;

	public ClientChatty(String serverAddress, int port) {
		// Trying to connect to the server
		this.scan = new Scanner(System.in);
		try {
			this.socket = new Socket(serverAddress,port);
			System.out.println("Connected to the server !");

			// Setting in/out 
			this.scan = new Scanner(System.in);
			this.out = new DataOutputStream(socket.getOutputStream());
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		} catch (Exception e) {
			System.out.println("Unable to connect to the server");
		}
	}


    public void chat() {

		// Initializing Listening Thread
        Thread listeningThread = new Thread(() -> {
			String receveidMessage ;
			try {
				while ((receveidMessage = this.in.readLine()) != null) {
					if (receveidMessage.equals("exit")) {
                        System.out.print("\r\033[K");
                        System.out.println("Partner exiting, exit...");
                        System.exit(0);
                    }
					// Detele old writed line 
					System.out.print("\r\033[K");
					// System.out.print("\033[1A\033[2K"); // 1A: moving up, 2K: delete all line
					System.out.println("Server# " + receveidMessage);
					System.out.print("\n--- Enter Message # ");
				}
			} catch (Exception e) {
				System.out.println("Error while receiving message");
				e.printStackTrace();
			}
		});

        listeningThread.start();

		// Loop for chatting 
        String msg = "" ;
        while (!msg.equals("exit")) {
            System.out.print("\n---- Enter Message # ");
            msg = this.scan.nextLine();
			// Delete entry line and write history
			System.out.print("\033[1A\033[2K");
			System.out.println("You# " + msg);
			System.exit(0);
			try {
				this.out.writeUTF(msg);
			} catch (Exception e) {
			}
        }
    }



    public static void main(String[] args) {
        ClientChatty client = new ClientChatty("127.0.0.1",1234);
		client.chat();
    }

}
