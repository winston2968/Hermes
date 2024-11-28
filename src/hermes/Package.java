package hermes;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import chatty.Datagram;

public class Package extends Datagram {
    
    private KeyPairGenerator rsaGenerator ;
    private KeyPair keyPair ;
    private Cipher cipher ; 
    private PublicKey hisPublicKey ;
    private PrivateKey myPrivateKey ;
    private SecretKey aesKey ;

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

    // Return AES secret key ciphred with RSA parter public key to send it throught socket
    public byte[] getAESCiphered() throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        // Cipher AES key with the RSA public key
        this.cipher = Cipher.getInstance("RSA");
        this.cipher.init(Cipher.ENCRYPT_MODE,this.hisPublicKey);
        byte[] aesKeyEncoded = this.aesKey.getEncoded();
        return this.cipher.doFinal(aesKeyEncoded);
    }

    // Get the encrypted AES key from socket,
    // decioher it with own RSA private key and instaciate new AES private key
    // for messages exchange
    public void setAESCiphered(byte[] aesKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Decipher received AES key with RSA
        this.cipher = Cipher.getInstance("RSA");
        this.cipher.init(Cipher.DECRYPT_MODE, this.myPrivateKey);
        byte[] aesKeyDecipher = this.cipher.doFinal(aesKey);
        this.aesKey = new SecretKeySpec(aesKeyDecipher, "AES");
    }

    // =====================================================================
    //                     Messages conversion
    // =====================================================================



}
