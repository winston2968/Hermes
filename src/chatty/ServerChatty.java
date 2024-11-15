package chatty;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

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

    public void chat() throws IOException {

        // Creating the main window
        Screen screen = new DefaultTerminalFactory().createScreen();
        screen.startScreen();

        WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
        BasicWindow window = new BasicWindow("Chatty");

        // Define custom colors themes for the window
        SimpleTheme customTheme = new SimpleTheme(
            TextColor.ANSI.BLACK,   // Texte par défaut
            TextColor.ANSI.CYAN    // Fond par défaut
        );

        // Main panel with vertical separation
        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));

        // History messages section
        TextBox messageBox = new TextBox(new TerminalSize(60,20), TextBox.Style.MULTI_LINE);
        messageBox.setReadOnly(true); // the user can't modify history messages
        mainPanel.addComponent(messageBox);

        // User input section
        TextBox inputBox = new TextBox(new TerminalSize(60, 1));
        mainPanel.addComponent(inputBox);

        // Apply the theme to the window
        textGUI.setTheme(customTheme);

        // Thread to listen user entry
        Thread userListenerThread = new Thread(() -> {
            try {
                while(true) {
                    // Read the stroken key 
                    KeyStroke keyStroke = screen.pollInput();
                    if (keyStroke != null) {
                        if (keyStroke.getKeyType() == KeyType.Enter) {
                            // We send the message 
                            String message = inputBox.getText();
                            inputBox.setText("");
                            this.out.println(message);
                            if(!message.trim().isEmpty()) {
                                messageBox.addLine("You# " + message);
                            }
                        }
                    } else {
                        // We add the hitten character to the inputBox
                        inputBox.handleKeyStroke(keyStroke);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Initializing Listening Thread
        Thread listeningThread = new Thread(() -> {
			String receveidMessage ;
			try {
				while ((receveidMessage = this.in.readUTF()) != null) {
                    // Display the receveid message in the history box 
					messageBox.addLine(receveidMessage);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

        // Launching threads 
        userListenerThread.start();
        listeningThread.start();

        // Display main window
        window.setComponent(mainPanel);
        textGUI.addWindowAndWait(window);

        // Stop display when window closing
        screen.stopScreen();
    }





    public static void main(String[] args) throws IOException {
        ServerChatty server = new ServerChatty();
        server.chat();
    }

}
