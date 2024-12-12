package hermes;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import chatty.Datagram;

    /**
     * Package management for Hermes classes. 
     * @author winston2968
     * @version 1.0
     */

public class Package extends Datagram {
    
    private KeyPairGenerator rsaGenerator ;
    private KeyPair keyPair ;
    private Cipher cipher ; 
    private PublicKey hisPublicKey ;
    private PrivateKey myPrivateKey ;
    public SecretKey aesKey ;

    /**
     * Package constructor
     */
    public Package() {
        try {
            // Generating RSA public/private key
            this.rsaGenerator = KeyPairGenerator.getInstance("RSA");
            this.rsaGenerator.initialize(2048); // Taille des clés
            this.keyPair = this.rsaGenerator.generateKeyPair();
            this.myPrivateKey = this.keyPair.getPrivate();
            // Generating AES key 
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            this.aesKey = keyGen.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================================================================
    //                          Getter and Setter
    // =====================================================================

    @Override
    public PublicKey getPublicKey() {
        return this.keyPair.getPublic();
    }

    @Override
    public void setHisPublicKey(PublicKey hisKey) {
        this.hisPublicKey = hisKey ;
    }

    // =====================================================================
    //                     Sendding/Getting AES key
    // =====================================================================

    /**
     * Method to send AES key
     * @return AES secret key ciphered with RSA parter public key to send it throught socket
     * @throws Exception
     */
    public byte[] getAESCiphered() throws Exception {
        // Cipher AES key with the RSA public key
        this.cipher = Cipher.getInstance("RSA");
        this.cipher.init(Cipher.ENCRYPT_MODE,this.hisPublicKey);
        byte[] aesKeyEncoded = this.aesKey.getEncoded();
        return this.cipher.doFinal(aesKeyEncoded);
    }

    /**
     * Method to get AES key from the socket
     * @param aesKey
     * @throws Exception
     */
    public void setAESCiphered(byte[] aesKey) throws Exception {
        // Decipher received AES key with RSA
        this.cipher = Cipher.getInstance("RSA");
        this.cipher.init(Cipher.DECRYPT_MODE, this.myPrivateKey);
        byte[] aesKeyDecipher = this.cipher.doFinal(aesKey);
        this.aesKey = new SecretKeySpec(aesKeyDecipher, "AES");
    }

    // =====================================================================
    //                     Messages conversion
    // =====================================================================

    /**
     * Method which cipher a text with AES
     * @param text
     * @return message ciphered with AES
     * @throws Exception
     */
    public byte[] cipherStringAES(String text) throws Exception {
        // Initializing cipher tool 
        this.cipher = Cipher.getInstance("AES");
        this.cipher.init(Cipher.ENCRYPT_MODE,this.aesKey);
        return this.cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Method which decipher a byte[] with AES
     * @param datas
     * @return String of deciphered message
     * @throws Exception
     */
    public String decipherToStringAES(byte[] datas) throws Exception {
        // Initializing cipher tool 
        this.cipher = Cipher.getInstance("AES");
        this.cipher.init(Cipher.DECRYPT_MODE,this.aesKey);
        return new String(this.cipher.doFinal(datas), StandardCharsets.UTF_8);
    }

    /**
     * Method which create a datagram to send it on socket
     * @param username
     * @param destinator
     * @param message
     * @return new datagram ciphered with AES
     * @throws Exception
     */
    public byte[][] cipherMessageAES(String username, String destinator, String message) throws Exception {
        // Initializing cipher tool 
        this.cipher = Cipher.getInstance("AES");
        this.cipher.init(Cipher.ENCRYPT_MODE,this.aesKey);
        // Get current date and time 
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd;HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        // Convert and return ciphered message 
        byte[] messageToCipher = (formattedDateTime + ";" + message.replace(';', ' ')).getBytes(StandardCharsets.UTF_8);
        byte[] usernameCiphered = this.cipher.doFinal(username.getBytes(StandardCharsets.UTF_8));
        byte[] destinatorCiphered = this.cipher.doFinal(destinator.getBytes(StandardCharsets.UTF_8));
        byte[] messageCiphered = this.cipher.doFinal(messageToCipher);
        return new byte[][] {destinatorCiphered,usernameCiphered,messageCiphered};
    }

    /**
     * Method which decipher a datagram received on the socket
     * @param packet
     * @return the deciphered datagram
     * @throws Exception
     */
    public String[] decipherMessageAES(byte[][] packet) throws Exception {
        // Initializing decipher tool
        this.cipher = Cipher.getInstance("AES");
        this.cipher.init(Cipher.DECRYPT_MODE,this.aesKey);
        // Extract content
        byte[] destinatorByte = this.cipher.doFinal(packet[0]);
        byte[] usernameByte = this.cipher.doFinal(packet[1]);
        byte[] message = this.cipher.doFinal(packet[2]);
        // Converting
        return new String[] {
            new String(destinatorByte,StandardCharsets.UTF_8), 
            new String(usernameByte,StandardCharsets.UTF_8),
            new String(message, StandardCharsets.UTF_8)
        };
    }


}
