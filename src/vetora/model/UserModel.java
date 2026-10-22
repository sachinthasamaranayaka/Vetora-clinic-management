/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;
import vetora.dao.UserDAO;
import vetora.exception.ValidationException;

/**
 *
 * @author ASUS
 */
public class UserModel {
    private final UserDAO userDAO = new UserDAO();
    
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
    
    public List<User> getAllUser()throws SQLException{
        return userDAO.getAllUsers();
    }
}
