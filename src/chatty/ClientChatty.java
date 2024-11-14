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
        Scanner scan = new Scanner(System.in);
        String msg = "" ;
        while (!msg.equals("exit")) {
            System.out.print("Message à envoyer : ");
            msg = scan.nextLine();
			try {
				this.out.writeUTF(msg);
			} catch (Exception e) {
			}
			try {
                String receveid = this.in.readLine();
                System.out.println("Server# " + receveid);
            } catch (Exception e) {
            }
            
            
        }
    }



    public static void main(String[] args) {
        ClientChatty client = new ClientChatty("127.0.0.1",1234);
		client.chat();
    }

}
