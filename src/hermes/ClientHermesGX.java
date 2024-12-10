package hermes;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ClientHermesGX extends JFrame implements ActionListener {

    // =====================================================================
    //                          Attributes
    // =====================================================================

    // Connexions attributes 
    private Socket socket ;
    private ObjectInputStream in ;
    private ObjectOutputStream out ;
    private String username ;
    private String serverIpAddress = "127.0.0.1";
    private static final int PORT = 1234 ;
    private Package packet ;
    private String[] commands = {"/disconnect","/update-clients"};
    private DefaultListModel<String> model ;
    private JList<String> clientsJList  ;
    
    
    private JButton connect ;
    private JTextField entryAddress ;
    private JTextField entryUsername ;
    private JTextField messageEntry ;
    private JButton sendMessage ;
    private JTextArea history ;


    // =====================================================================
    //                          Constructor
    // =====================================================================


    public ClientHermesGX() {
        super("Connexion Client - Hermes");
        // Launching the connexion window
        this.connexionWindow();
    }

    // =====================================================================
    //                       Connexion Stater
    // =====================================================================


    // Starting connexion, connect to the server and get username
    public void connexionWindow() {
        // Initializing window structure
        Border grayBorder = BorderFactory.createLineBorder(Color.lightGray);
        JPanel general = new JPanel(new GridLayout(3,1));
        JPanel entries = new JPanel(new GridLayout(2,2));
        general.setBackground(new Color(0x000000));
        entries.setBackground(new Color(0x000000));
        //this.setBackground(new Color(0x000000));

        // Initializing entries/labels
        JLabel mainTitle = new JLabel("Connexion to Server");
        JLabel ipAddressLabel = new JLabel("Server IP Address");
        JLabel usernameLabel = new JLabel("Username");
        mainTitle.setHorizontalAlignment(SwingConstants.CENTER);
        mainTitle.setVerticalAlignment(SwingConstants.CENTER);
        mainTitle.setForeground(Color.lightGray);
        mainTitle.setFont(new Font("Arial", Font.BOLD, 20));
        ipAddressLabel.setForeground(Color.lightGray);
        ipAddressLabel.setBorder(grayBorder);
        usernameLabel.setForeground(Color.lightGray);
        usernameLabel.setBorder(grayBorder);

        this.entryAddress = new JTextField("127.0.0.1");
        this.entryUsername = new JTextField();
        entryAddress.setBackground(new Color(0x000000));
        entryAddress.setForeground(Color.lightGray);
        entryAddress.setBorder(grayBorder);
        entryUsername.setBackground(new Color(0x000000));
        entryUsername.setForeground(Color.LIGHT_GRAY);
        entryUsername.setBorder(grayBorder);

        this.connect = new JButton("Connect");
        connect.setBackground(new Color(0x000000));
        connect.setBorder(grayBorder);
        connect.setForeground(Color.lightGray);
        connect.setSize(new Dimension(60, 20));
        connect.addActionListener(this);

        // Adding Labels to structure
        entries.add(ipAddressLabel); entries.add(entryAddress);
        entries.add(usernameLabel); entries.add(entryUsername);

        // Adding panels to main structure 
        general.add(mainTitle);
        general.add(entries);
        general.add(connect);

        this.add(general);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(600,400);
        this.setVisible(true);
    }


    private void startConnexion() {
        // Try to connect to the server
        this.username = this.entryUsername.getText();
        this.serverIpAddress = this.entryAddress.getText();

        try {
            this.socket = new Socket(this.serverIpAddress, PORT);
            // Setting in/out
            this.out = new ObjectOutputStream(this.socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(this.socket.getInputStream());
        } catch (Exception e) {
            new InfoWindow("Config-Error", "Failed to get server.\nExiting...");
        }

        // Setting security features 
        this.packet = new Package();
        // Exchange RSA keys
        try {
            // Get server RSA public key 
            this.packet.setHisPublicKey( (PublicKey) this.in.readObject());
			// Sending actual RSA public key to server 
            this.out.writeObject(this.packet.getPublicKey());
            // Get server AES key 
            this.packet.setAESCiphered((byte[]) this.in.readObject());
        } catch (Exception e) {
            new InfoWindow("Config-Error", "Failed to set secure connexion.\nExiting...");
            System.exit(0);
        }

        // Sending username to server for packages identification
        try {
            this.out.writeObject(this.packet.cipherStringAES(this.username));
        } catch (Exception e) {
            new InfoWindow("Config-Error", "Failed to send username.\nExiting...");
            System.exit(0);
        }

        // Starting main chat window
        this.chat();
    }


    // =====================================================================
    //                          Chatting window
    // =====================================================================

    public void chat() {

        // Chatting window
        this.setTitle("Hermes - ClientGX");
        this.getContentPane().removeAll();

        // Setting new components 
        JPanel messagePanel = new JPanel(new GridLayout(1,2));
        GridBagConstraints gridBag = new GridBagConstraints();
        Border grayBorder = BorderFactory.createLineBorder(Color.lightGray);
        messagePanel.setBackground(new Color(0x000000));

        // Config Message Entry display
        this.messageEntry = new JTextField("Type a message...");
        this.messageEntry.setBackground(new Color(0x000000));
        messagePanel.setBackground(new Color(0x000000));
        this.sendMessage = new JButton("Send");
        this.sendMessage.addActionListener(this);
        sendMessage.setBackground(new Color(0x000000));
        gridBag.gridx = 0 ;
        gridBag.gridy = 0 ;
        gridBag.weightx = 0.75 ; 
        gridBag.fill = GridBagConstraints.HORIZONTAL ;
        messageEntry.setForeground(Color.lightGray);
        messageEntry.setBorder(grayBorder);
        messagePanel.add(messageEntry, gridBag);

        // Config Send button display
        gridBag.gridx = 1;
        gridBag.weightx = 0.25;
        sendMessage.setForeground(Color.lightGray);
        sendMessage.setBorder(grayBorder);
        messagePanel.add(sendMessage, gridBag);

        // Config message history display
        this.history = new JTextArea();
        history.setEditable(false);
        history.setLineWrap(true);
        history.setWrapStyleWord(true);
        history.setForeground(Color.lightGray);
        history.setBorder(grayBorder);
        history.setBackground(new Color(0x000000));
        JScrollPane capsuleHistory = new JScrollPane(history);
        capsuleHistory.setBackground(new Color(0x000000));

        // Config clients list display
        this.model = new DefaultListModel<>();
        this.clientsJList = new JList<>(model);
        this.clientsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.clientsJList.setBorder(BorderFactory.createTitledBorder("Connected People"));
        clientsJList.setBackground(new Color(0x000000));
        clientsJList.setForeground(Color.lightGray);

        // Adding chat history and message input in left panel
        messagePanel.add(messageEntry,gridBag); messagePanel.add(sendMessage,gridBag);
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(capsuleHistory,BorderLayout.CENTER);
        rightPanel.add(messagePanel,BorderLayout.SOUTH);

        // Setting up window separation for messages and clients list
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(clientsJList),rightPanel);
        splitPane.setDividerLocation(200);


        // Adding the splitPane to main window
        this.add(splitPane);

        // Update window
        this.revalidate();
        this.repaint();

        // Listener for input messages 
        Thread listeningThread = new Thread(() -> {
            byte[][] receivedDatagram ;
            try {
                while ((receivedDatagram = ((byte[][]) this.in.readObject())) != null) {
                    // Extracting datas
                    String[] decipheredDatagram = this.packet.decipherMessageAES(receivedDatagram);
                    String partnerString = decipheredDatagram[1];
                    String message = (decipheredDatagram[2].split(";"))[2];
                    
                    // Getting command if it's a command message
                    String[] messageCut = message.split("-separator-");
                    if (messageCut[0].equals(this.commands[0]) && partnerString.equals("Server")) {
                        this.disconnect();
                    } else if (messageCut[0].equals(this.commands[1]) && partnerString.equals("Server")) {
                        String[] clients = ((messageCut[1].replace("[","")).replace("]","")).split(",");
                        this.updateConnectedClients(clients);
                    } else {
                        String time = (decipheredDatagram[2].split(";"))[1];
                        // Write new message on message history panel
                        this.history.setText(this.history.getText() + "\n" + time + ":" + partnerString + " : " + message);
                            
                    }
                }
            } catch (Exception e) {
                System.err.println(e);
                e.printStackTrace();
            }
        });
        listeningThread.start();

    }

    // =====================================================================
    //                          Connexion Management
    // =====================================================================

    public void disconnect() {
        // Disconnect from server
        try {
            this.in.close();
            this.out.close();
            this.socket.close();
            new InfoWindow("Info", "Disconnected from server.\nExiting...");
        } catch (Exception e) {
            new InfoWindow("Error", "Failed to close connexion properly.\nExiting...");
        }
        
    }

    
    public void updateConnectedClients(String[] clients) {
        // Update graphic clients list
        SwingUtilities.invokeLater(() -> {
            this.model.clear();
            for (String client : clients) {
                if (!(client.replace(" ", "")).equals(this.username)) {
                    model.addElement((client.replace(" ", "")));
                }
            }
        });
    }   
    



    // =====================================================================
    //                     Window's action manager
    // =====================================================================

    @Override
    public void actionPerformed(ActionEvent e) {
        // Execute methods depending on the input
        if (e.getSource() == connect) {
            this.startConnexion();
        } else if (e.getSource() == sendMessage) {
            try {
                // Cipher and send datagram to the server
                byte[][] datagram = this.packet.cipherMessageAES(this.username, "all", this.messageEntry.getText());
                this.out.writeObject(datagram);
                // Display new message in messages history
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String formattedDateTime = now.format(formatter);
                this.history.setText(this.history.getText() + "\n" + formattedDateTime + ":" + this.username + ":/$ " + this.messageEntry.getText());
                this.messageEntry.setText("");
            } catch (Exception error) {
                error.printStackTrace();
                System.err.println(error);
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientHermesGX::new);
    }
}


