package chatty;

import java.util.Scanner;


public class MainChatty {

    private ClientChatty client ;
    private ServerChatty server ;
    private String name ;
    private String partnerName ;
    private Boolean condServer ;

    public MainChatty() {
        this.server = new ServerChatty();
        this.client = new ClientChatty();
    }

    public void initConnection() {
        Scanner scan = new Scanner(System.in);

        // Choose connection mode
        System.out.print("""
				Hermes/:$ --- Choose connection mode (Client/Server):
								1 -> Client
								2 -> Server
				Hermes/:$ """);
        int number = scan.nextInt();
       this.condServer = !(number == 1);

        // Ask to name
        System.out.println("Hermes/:$ --- Set Name ---");
        System.out.println("Hermes/:$ Enter your name : ");
        this.name = scan.nextLine();


        // Display current ipAdress to give it to partner
        System.out.println("Hermes/:$ --- Initialize connection ---");
        System.out.println("Hermes/:$ Give this address to your partner : " + this.server.getIpAddress());

        // Enter partner ipAdress to start connection
        System.out.println("Hermes/:$ Enter partner ipAdress...");
        String entry = scan.nextLine();
        System.out.println("Hermes/:$ Address : " + entry);
        this.client.sendToServer(entry);
        this.client.connectToServer();

        // Get partnerName
        this.client.sendToServer(this.name);
        this.partnerName = this.server.readFromClient();
    }

    public void startChat() {
        Scanner scan = new Scanner(System.in);
        String messageToPrint = "" ;
        String messageToSend = "" ;

        // Init connection
        this.initConnection();

        // Loop to chat
        while(!messageToSend.equals("bye")) {
            System.out.println(this.partnerName + "/:$ ");
        }
    }



}
