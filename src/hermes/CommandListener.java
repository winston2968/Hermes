package hermes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

    /**
     * CLI Hermes Server Management
     * @author winston2968
     * @version 1.0
     */

public class CommandListener implements Runnable {
    
    private List<ClientHandler> clientsList ;
    private Map<String, Runnable> commands ;
    private ServerHermes server ;
    private Scanner scan ;
    
    // =====================================================================
    //                          Constructor
    // =====================================================================

    /**
     * CommandListener constructor
     * @param clients
     * @param serv
     */
    public CommandListener(List<ClientHandler> clients, ServerHermes serv) {
        this.scan = new Scanner(System.in);
        // Link to general server
        this.server = serv ;
        // Link to connected clients
        this.clientsList = clients ; 
        // Initializing commands 
        this.commands = new HashMap<>();
        this.commands.put("/stop", this::stopServer);
        this.commands.put("/list", this::listClients);
        this.commands.put("/killOne", this::killClient);
        this.commands.put("/killAll", this::killAll);
        this.commands.put("/help", this::help);
        this.commands.put("/broadcast", this::broadcast);
    }
    
    // =====================================================================
    //                     Thread always running
    // =====================================================================

    /**
     * Implemented method for listening admin command entries. 
     */
    @Override
    public void run() {
        System.out.println("""
    * Command line tool for Hermes-Server managment. 
    * You can manage connected clients and server
    * by launching just simples commands here !
    * You can start by typing /help...
                """);
        System.out.println("");

        // Infinite loop
        String entry = "" ;
        while (true) {
            // Reading user entry 
            System.out.print("Hermes-Server:/$ ");
            entry = this.scan.nextLine();
            // Executing corresponding process 
            Runnable action = commands.get(entry);
            if (action != null) {
                action.run();
            } else {
                System.out.println("Hermes-Server:/$ Type valid command please...rtfm");
            }
        }
    }

    // =====================================================================
    //                       Commands Methods
    // =====================================================================

    /**
     * Method to stop the server properly. 
     */
    private void stopServer() {
        // Starting by disconnect all clients 
        this.killAll();
        // Stoping server
        // this.server.stopServer();
        System.out.println("Hermes-Server:/$ Server Stopped, exiting...");
        System.exit(0);
    }

    /**
     * Method to list current connected clients. 
     */
    private void listClients() {
        // List all connected clients 
        System.out.println("Hermes-Clients:/$ Connected clients :");
        for (int i = 0 ; i < this.clientsList.size(); i++) {
            System.out.println("+----- " + this.clientsList.get(i).getUsername());
        }
    }

    /** 
     * Method to disconnect a client. 
     */
    private void killClient() {
        // List users
        this.listClients();

        // Get username to disconnect
        System.out.println("Hermes-Server:/$ Which client do you want to disconnect ?");
        System.out.print("Hermes-Server:/$ ");
        String user = this.scan.nextLine();

        // Disconnect the user
        synchronized (this.clientsList) {
            for (int i = 0; i < this.clientsList.size(); i++) {
                if (this.clientsList.get(i).getUsername().equals(user)) {
                    this.clientsList.get(i).closeConnection();
                }
            }
        }
        System.out.println("Hermes-Client:/$ Client " + user + " succesfully disconnected !");
    }

    /**
     * Method to disconnect all connected clients. 
     */
    private void killAll() {
        synchronized (this.clientsList) {
            while (!this.clientsList.isEmpty()) {
                ClientHandler ch = clientsList.get(0);
                System.out.println("Hermes-Client:/$ Client " + ch.getUsername() + " succesfully disconnected !");
                ch.closeConnection();
            }
        }
    }   

    /**
     * Method to list available commands. 
     */
    private void help() {
        // Printing all command and description
        System.out.println("Hermes-Server:/$ Available commands :");
        System.out.println(this.commands.keySet().toString());
    }

    /**
     * Method to send a message to all clients. 
     */
    private void broadcast() {
        // Get the message to broadcast
        System.out.println("Hermes-Server:/$ Enter message to broadcast...");
        System.out.print("Hermes-Server:/$ ");
        String message = this.scan.nextLine();

        // Creating datagram to send 
        byte[][] data = null ;
        try {
            data = this.server.createDatagram(message, "Server", "all");

            // Send message to all connected clients 
            synchronized(clientsList) {
                for (int i = 0; i < this.clientsList.size(); i++) {
                    this.clientsList.get(i).sendDatagramm(data);
                }
            }
            System.out.println("Hermes-Server:/$ Message succesfully broadcasted to all clients !");
        } catch (Exception e) {
            System.out.println("Hermes-Server:/$ Unnable to create datagram. Operation avorted.");
            System.err.println(e);
            e.printStackTrace();
        }
    }




}
