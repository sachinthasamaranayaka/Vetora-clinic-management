/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.controller;

import java.security.GeneralSecurityException;
import vetora.dao.UserDAO;
import vetora.exception.ValidationException;
import java.sql.SQLException;
import java.util.Arrays;
import vetora.util.PasswordUtils;
import vetora.model.User;

/**
 *
 * @author ASUS
 */
public class UserController {
    
    private final UserDAO userDAO = new UserDAO();
    
    public void createFirstAdmin(String fullName , String username , char[] password , char[] confirmation)throws ValidationException,SQLException,GeneralSecurityException{
        fullName = fullName == null ? "" : fullName.trim();
        username = username == null ? "" : username.trim();
        
        if (fullName.isEmpty()|| fullName.length() > 100){
            throw new ValidationException("Enter a full name of 1-100 characters.");
        }
        
        if (!username.matches("[A-Za-z0-9._-]{3,50}")){
            throw new ValidationException("Username: 3-50 letters, numbers, dots, underscores or hyphens.");
        }
        
        int length = password == null ? 0 : Character.codePointCount(password, 0, password.length);
        
        if (length < 15 || length > 128){
            throw new ValidationException("Use a password of 15-128 characters.");
        }
        
        if (!Arrays.equals(password, confirmation)){
            throw new ValidationException("Passwords do not match.");
        }
        
        String hash = PasswordUtils.hashPassword(password);
        
        boolean created = userDAO.createFirstAdmin(fullName, username, hash);
        
        if (!created){
            throw new ValidationException("An account already exists. Sign in as an administrator.");
        }
    }
    
    public User login(String username, char[] password) throws ValidationException,SQLException,GeneralSecurityException{
        username = username == null ? "" : username.trim();
        
        if (username.isEmpty()){
            throw new ValidationException("Enter your username.");
        }
        
        if (password == null || password.length == 0){
            throw new ValidationException("Enter your password.");
        }
        
        int length = Character.codePointCount(password, 0, password.length);
        
        if (!username.matches("[A-Za-z0-9._-]{3,50}")|| length > 128){
            throw new ValidationException("Invalid username or password.");
        }
        
        User user = userDAO.authenticate(username, password);
        
        if (user == null){
            throw new ValidationException("Invalid username or password.");
        }
        
        return user;
    }
}
