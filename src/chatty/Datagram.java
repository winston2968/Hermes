package chatty;



import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.crypto.Cipher;

    /**
     * Class which represent Datagram gestion for chatty
     * @author winston2968
     * @version 1.0
     */

public class Datagram {

    private KeyPairGenerator rsaGenerator ;
    private KeyPair keyPair ;
    private Cipher cypher ; 
    private PublicKey hisPublicKey ;
    private PrivateKey myPrivateKey ;

    // =====================================================================
    //                          Constructor
    // =====================================================================

    /**
     * Constructor for Datagram class
     */

    public Datagram() {
        try {
            // Generating and setting private and public keys
            this.rsaGenerator = KeyPairGenerator.getInstance("RSA");
            this.rsaGenerator.initialize(2048); // Taille des clés
            this.keyPair = this.rsaGenerator.generateKeyPair();
            this.myPrivateKey = this.keyPair.getPrivate();
            // Setting cypher tool
            this.cypher = Cipher.getInstance("RSA");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================================================================
    //                          Getter and Setter
    // =====================================================================

    /**
     * Method to get Datagram RSA public key
     * @return PublicKey une clé publique RSA
     */
    public PublicKey getPublicKey() {
        return this.keyPair.getPublic();
    }

    /**
     * Method to modify current RSA public key
     * @param key
     */
    public void setHisPublicKey(PublicKey key) {
        this.hisPublicKey = key ;
    }

    // =====================================================================
    //                          Messages Conversions
    // =====================================================================

    /**
     * Method which convert a message, username and destinator to datagram format
     * @param msg
     * @param username
     * @param destinator
     * @return byte[] new datagram 
     * @throws Exception
     */

    public byte[] stringToByte(String msg, String username, String destinator) throws Exception {
        // Replace all ; in msg
        String msgClean = msg.replace(';', ' ');
        String usernameClean = username.replace(';', ' ');
        String destinatorClean = destinator.replace(';',' ');
        // Get current date and time 
        LocalDateTime now = LocalDateTime.now();
        // Formatting current date and time 
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd;HH:mm:ss");
        // Export current date and hour to String 
        String formattedDateTime = now.format(formatter);
        String finalMsg = formattedDateTime + ";" + usernameClean + ";" + destinatorClean + ";" + msgClean;
        // Convert and return byte datagram 
        byte[] messagePlain = finalMsg.getBytes(StandardCharsets.UTF_8);
        // Cipher message to send with partner public key 
        return this.cypher(messagePlain);
    }

    /**
     * Method which convert deciphered datagram to array of string 
     * for message display 
     * @param datas
     * @return String[] date, sender username, destinator username and message  
     * @throws Exception
     */
    
    public String[] byteToString(byte[] datas) throws Exception {
        byte[] decryptedDatas = this.decypher(datas);
        String dataString = new String(decryptedDatas, StandardCharsets.UTF_8);
        return dataString.split(";");
    } 

    // =====================================================================
    //                          Cipher options
    // =====================================================================

    /**
     * Method which cipher byte array with RSA algorithm
     * @param datas
     * @return byte[] RSA cyphered 
     * @throws Exception
     */
    private byte[] cypher(byte[] datas) throws Exception{
        this.cypher.init(Cipher.ENCRYPT_MODE,this.hisPublicKey);
        return cypher.doFinal(datas);
    }

    /**
     * Method which decipher byte array with RSA algorithm
     * @param datas
     * @return byte[] RSA deciphered
     * @throws Exception
     */

    private byte[] decypher(byte[] datas) throws Exception {
        this.cypher.init(Cipher.DECRYPT_MODE,this.myPrivateKey);
        return this.cypher.doFinal(datas);
    }


    // =====================================================================
    //                              Tests
    // =====================================================================

    public static void main(String[] args) {
        System.out.println("Hello there !");
    }



    

    

}