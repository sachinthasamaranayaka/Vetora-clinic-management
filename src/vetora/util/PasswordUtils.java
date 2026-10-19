/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
/**
 *
 * @author ASUS
 */
public class PasswordUtils {
    private static final int ITERATIONS = 600_000;
    
    private PasswordUtils(){
    }
    
    public static String hashPassword(char[] password) throws GeneralSecurityException{
        if (password == null || password.length == 0){
            throw new IllegalArgumentException("Password is required.");
        }
        
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        
        PBEKeySpec spec = new PBEKeySpec(password,salt,ITERATIONS,256);
        
        try{
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            
            return "pbkdf2_sha256$"+ITERATIONS+"$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(hash);
        }
        finally{
            spec.clearPassword();
        }
    }
    
    public static boolean verifyPassword(char[] password, String storedHash) throws GeneralSecurityException {
        if(password == null || password.length == 0 || storedHash == null || storedHash.length()>255){
            return false;
        }
        try {
            String[] parts = storedHash.split("\\$",-1);
            if (parts.length != 4 ||!"pbkdf2_sha256".equals(parts[0])){
                return false;
            }
            
            int interactions = Integer.parseInt(parts[1]);
            if (interactions < 600_000 || interactions > 2_000_000){
                return false;
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            
            if (salt.length != 16 || expectedHash.length != 32){
                return false;
            }
            
            PBEKeySpec spec = new PBEKeySpec(password,salt,interactions,256);
            
            try{
                byte[] actualHash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
                return MessageDigest.isEqual(expectedHash,actualHash);
            }
            finally{
                spec.clearPassword();
            }
        }
        
        catch (IllegalArgumentException ex){
            return false;
        }
    }
}
