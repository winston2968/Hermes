package chatty ;


import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import javax.crypto.Cipher;

public class Datagram {

    private KeyPairGenerator rsaGenerator ;
    private KeyPair keyPair ;
    private Cipher cypher ; 
    public PublicKey clientkey ;

    /* I use a special pattern for datagrams. I want to save the date 
     * when I send the datagram and who sent it.
     * Datagram Format : Date;Hour;Username;Message
     */


    public Datagram() {
        try {
            // Generating and setting private and public keys
            this.rsaGenerator = KeyPairGenerator.getInstance("RSA");
            this.rsaGenerator.initialize(2048); // Taille des clés
            this.keyPair = this.rsaGenerator.generateKeyPair();
            // Setting cypher tool
            this.cypher = Cipher.getInstance("RSA");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PublicKey getPublicKey() {
        return this.keyPair.getPublic();
    }

    public byte[] stringToByte(String msg, String username) {
        // Replace all ; in msg
        String msgClean = msg.replace(';', ' ');
        String usernameClean = username.replace(';', ' ');
        // Get current date and time 
        LocalDateTime now = LocalDateTime.now();
        // Formatting current date and time 
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd;HH:mm:ss");
        // Export current date and hour to String 
        String formattedDateTime = now.format(formatter);
        String finalMsg = formattedDateTime + ";" + usernameClean + ";" + msgClean;
        // Convert and return byte datagram 
        return finalMsg.getBytes(StandardCharsets.UTF_8);
    }
    
    public String[] byteToString(byte[] datas) {
        String dataString = new String(datas, StandardCharsets.UTF_8);
        return dataString.split(";");
    } 

    private byte[] cypher(byte[] datas) throws Exception{
        this.cypher.init(Cipher.ENCRYPT_MODE,this.keyPair.getPublic());
        return cypher.doFinal(datas);
    }

    private byte[] decypher(byte[] datas) throws Exception {
        this.cypher.init(Cipher.DECRYPT_MODE,this.keyPair.getPrivate());
        return this.cypher.doFinal(datas);
    }


    public static void main(String[] args) {
        String msg = "Coucou les am;is !!!" ;

        Datagram algo = new Datagram();

        byte[] datas = algo.stringToByte(msg, "Darm;anin");
        try {
            byte[] datasCypher = algo.cypher(datas);
            byte[] datasDecipher = algo.decypher(datasCypher);
            String[] finalMessage = algo.byteToString(datasDecipher);
            System.out.println(Arrays.toString(finalMessage));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }



    

    

}