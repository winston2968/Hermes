package chatty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientChatty extends Thread {

    private String serverAdress ;
    private Socket client ;
    private BufferedReader in ;
    private PrintWriter out ;

    public ClientChatty() {
        
    }

    public void connectToServer() {
        System.out.println("Hermes/:$ Trying to connect to server...");
		try {
			this.client = new Socket(this.serverAdress, 4444);
			this.in = new BufferedReader(new InputStreamReader (client.getInputStream()));
			this.out = new PrintWriter(client.getOutputStream(), true);
			System.out.println("Hermes/:$ Connection Established ! !");
		} catch (Exception e) {
			System.out.println(e.toString());
			System.out.println("Hermes/:$ Error : Connection Failed !");
			System.exit(0);
		}
    }

    public void sendToServer(String text) {
		this.out.println(text);
	}
	
	public String readFromServer() {
		try {
			return this.in.readLine();
		} catch (IOException e) {
			return "";
		}
	}


    public static void main(String[] args) {
        ClientChatty client = new ClientChatty();

    }

}
