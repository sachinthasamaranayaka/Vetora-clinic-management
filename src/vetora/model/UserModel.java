/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;
import java.util.Arrays;
import vetora.util.PasswordUtils;
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

    public List<User> searchUsers(String keyword) throws SQLException, ValidationException {
        keyword = keyword == null ? "" : keyword.trim();
        if (keyword.length() > 100) {
            throw new ValidationException("Search: maximum 100 characters.");
        }
        return userDAO.searchUsers(keyword);
    }

    public User saveUser(User original, String fullName, String username, String role,
            boolean active, char[] password, char[] confirmation, long currentUserId)
            throws SQLException, ValidationException, GeneralSecurityException {
        fullName = fullName == null ? "" : fullName.trim();
        username = username == null ? "" : username.trim();
        if (fullName.isEmpty() || fullName.length() > 100) {
            throw new ValidationException("Enter a full name of 1-100 characters.");
        }
        if (!username.matches("[A-Za-z0-9._-]{3,50}")) {
            throw new ValidationException("Username: use 3-50 letters, numbers, dots, underscores or hyphens.");
        }
        if (!Arrays.asList("Admin", "Staff", "Veterinarian").contains(role)) {
            throw new ValidationException("Select a role.");
        }
        if (original != null && original.getUserId() == currentUserId && !active) {
            throw new ValidationException("You cannot deactivate the account you are signed in with.");
        }
        int length = password == null ? 0 : Character.codePointCount(password, 0, password.length);
        int confirmLength = confirmation == null ? 0 : confirmation.length;
        boolean changePassword = original == null || length > 0 || confirmLength > 0;
        String passwordHash = null;
        if (changePassword) {
            if (length < 6 || length > 128) {
                throw new ValidationException("Use a password of 6-128 characters.");
            }
            if (!Arrays.equals(password, confirmation)) {
                throw new ValidationException("Passwords do not match.");
            }
            passwordHash = PasswordUtils.hashPassword(password);
        }
        return userDAO.saveUser(original, fullName, username, role,
                active ? "Active" : "Inactive", passwordHash, currentUserId);
    }
}
