package chatty ;


import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import javax.crypto.Cipher;

public class Datagram {

    private KeyPairGenerator rsaGenerator ;
    private KeyPair keyPair ;
    private Cipher cypher ; 
    private PublicKey hisPublicKey ;
    private PrivateKey myPrivateKey ;


    /* I use a special pattern for datagrams. I want to save the date 
     * when I send the datagram and who sent it.
     * Datagram Format : Date;Hour;Username;Message
     */

     // =====================================================================
    //                          Constructor
    // =====================================================================

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
    public PublicKey getPublicKey() {
        return this.keyPair.getPublic();
    }

    public void setHisPublicKey(PublicKey key) {
        this.hisPublicKey = key ;
    }

    // =====================================================================
    //                          Messages Conversions
    // =====================================================================

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
    
    public String[] byteToString(byte[] datas) throws Exception {
        byte[] decryptedDatas = this.decypher(datas);
        String dataString = new String(decryptedDatas, StandardCharsets.UTF_8);
        return dataString.split(";");
    } 

    // =====================================================================
    //                          Cipher options
    // =====================================================================

    private byte[] cypher(byte[] datas) throws Exception{
        this.cypher.init(Cipher.ENCRYPT_MODE,this.hisPublicKey);
        return cypher.doFinal(datas);
    }

    private byte[] decypher(byte[] datas) throws Exception {
        this.cypher.init(Cipher.DECRYPT_MODE,this.myPrivateKey);
        return this.cypher.doFinal(datas);
    }


    // =====================================================================
    //                              Tests
    // =====================================================================

    public static void main(String[] args) {
        
    }



    

    

}