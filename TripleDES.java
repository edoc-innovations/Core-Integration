package eDOCPoster;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Hex;

public class TripleDES {
    //encryption
	public static String encrypt(String keyStr, String unencryptedText) throws Throwable {
		return encrypt(keyStr, "RANDOM", unencryptedText, "DESede/CBC/PKCS5Padding");
	}
	public static String encrypt(String keyStr, String Iv, String unencryptedText, String Algorithm) throws Throwable {
		// key should be passed as HEX string
    	byte[] KeyStrBytes = Hex.decodeHex(keyStr.toCharArray());
    	// get the IV
		byte[] IvBytes;
		if (Iv.toUpperCase() == "RANDOM") {
			SecureRandom random = new SecureRandom();
			IvBytes = random.generateSeed(8);
		}
		else {
			// assume init vector passed in HEX
			IvBytes = Hex.decodeHex(Iv.toCharArray());
		}
		String IvStr = Hex.encodeHexString(IvBytes);
		//System.out.println("Hex IV:" + IvStr);
		return  IvStr.toUpperCase() + encrypt(KeyStrBytes, IvBytes, unencryptedText, Algorithm).toUpperCase();
	}
    public static String encrypt(byte[] KeyBytes, byte[] IvBytes, String unencryptedText, String Algorithm) throws Throwable {
        try {
        	if(Algorithm.isEmpty()) {
        		Algorithm = "DESede/CBC/PKCS5Padding";
        	}
        	// get the encryption method
        	int x = Algorithm.indexOf("/");
        	String Method = Algorithm.substring(0, x);
        	// create the encryption key object
        	KeySpec ks = new DESedeKeySpec(KeyBytes);
        	//SecretKeyFactory skf = SecretKeyFactory.getInstance("DESede");
        	SecretKeyFactory skf = SecretKeyFactory.getInstance(Method);
        	SecretKey kgen = skf.generateSecret(ks);
        	// create the init vector object
        	IvParameterSpec iv = new IvParameterSpec(IvBytes);
        	// get the unencrypted text
        	byte[] unencryptedTextBytes = unencryptedText.getBytes();
        	// create the cipher object
        	Cipher cipher = Cipher.getInstance(Algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, kgen, iv);
            // encrypt the text
            byte[] CipherTextBytes = cipher.doFinal(unencryptedTextBytes);
            // return encrypted text as HEX string
            String CipherText = Hex.encodeHexString(CipherTextBytes);
            //System.out.println("Hex Encoded:" + Hex.encodeHexString(CipherTextBytes));
            return CipherText.toUpperCase();
        } catch (Exception e) {
            System.err.println("Error encrypting string.");
            e.printStackTrace();
        }
        return null;
    }
    
    //decryption
    public static String decrypt(String KeyStr, String encryptedText) throws Throwable {
    	return decrypt(KeyStr, "RANDOM", encryptedText, "DESede/CBC/PKCS5Padding");
    }
    public static String decrypt(String KeyStr, String Iv, String encryptedText, String Algorithm) throws Throwable {
		// key should be passed as HEX string
    	byte[] KeyStrBytes = Hex.decodeHex(KeyStr.toCharArray());
        // get IV    	
    	byte[] IvBytes;
    	String IvStr;
    	String NewEncryptedText;
		if (Iv.toUpperCase() == "RANDOM") {
			// first 8 bytes (16 characters of encrypted text are the random init vector
			IvStr = encryptedText.substring(0, 16);
			IvBytes = Hex.decodeHex(IvStr.toCharArray());
		}
		else {
			// assume init vector passed in HEX
			IvStr = Iv.substring(0, 16);
			IvBytes = Hex.decodeHex(IvStr.toCharArray());
		}
		NewEncryptedText = encryptedText.substring(16);
		//System.out.println("Hex IV:" + IvStr);
		//System.out.println("encrypted text minus IV:" + NewEncryptedText);
    	return decrypt(KeyStrBytes, IvBytes, NewEncryptedText, Algorithm);
    }
    public static String decrypt(byte[] KeyBytes, byte[] IvBytes, String encryptedText, String Algorithm) throws Throwable {
        try {
        	if(Algorithm.isEmpty()) {
        		Algorithm = "DESede/CBC/PKCS5Padding";
        	}        	
        	// get the encryption method
        	int x = Algorithm.indexOf("/");
        	String Method = Algorithm.substring(0, x);
        	// create the encryption key object
        	KeySpec ks = new DESedeKeySpec(KeyBytes);
        	//SecretKeyFactory skf = SecretKeyFactory.getInstance("DESede");
        	SecretKeyFactory skf = SecretKeyFactory.getInstance(Method);
        	SecretKey kgen = skf.generateSecret(ks);
        	// create the init vector object
        	IvParameterSpec iv = new IvParameterSpec(IvBytes);
        	// get the encrypted text (should be HEX string)
            byte[] encryptedTextBytes = Hex.decodeHex(encryptedText.toCharArray());
            // create the cipher object
            Cipher cipher = Cipher.getInstance(Algorithm); 
            cipher.init(Cipher.DECRYPT_MODE, kgen, iv);
            // decrypt the text
            byte[] plainTextBytes = cipher.doFinal(encryptedTextBytes);
            String plainText = new String(plainTextBytes);
            //System.out.println(plainText);
            return plainText;
        } catch (Exception e) {
            System.err.println("Error decrypting string.");
            e.printStackTrace();
        }
        return null;
    }

}
