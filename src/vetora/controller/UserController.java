/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.controller;

import java.security.GeneralSecurityException;
import vetora.exception.ValidationException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import vetora.model.User;
import vetora.model.UserModel;
import vetora.view.PanelUser;

/**
 *
 * @author ASUS
 */
public class UserController {
    
    private final UserModel model;
    private final PanelUser view;
    
    public UserController(UserModel model, PanelUser view){
        this.model = model;
        this.view = view;
    }
    
    public void loadUsers(){
        view.showUserListMessage("Loading users...");
        
        new SwingWorker<List<User>,Void>(){
            @Override
            protected List<User> doInBackground() throws Exception {
                return model.getAllUser();
            }
            @Override
            protected void done(){
                try{
                    List<User> users = get();
                    view.displayUser(users);
                }
                catch (InterruptedException ex){
                    Thread.currentThread().interrupt();
                    view.showUserListMessage("Loading interrupted.");
                }
                catch (ExecutionException ex){
                    ex.getCause().printStackTrace();
                    view.showUserListMessage("Could not load users.");
                }
            }
        }.execute();
    }
    
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
        
        if (length < 6 || length > 128){
            throw new ValidationException("Use a password of 6-128 characters.");
        }
        
        if (!Arrays.equals(password, confirmation)){
            throw new ValidationException("Passwords do not match.");
        }
    }
}
