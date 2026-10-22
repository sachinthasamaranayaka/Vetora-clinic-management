/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.controller;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import vetora.exception.ValidationException;
import vetora.model.User;
import vetora.model.UserModel;
import vetora.view.FormLogin;
import vetora.view.FormMain;

/**
 *
 * @author ASUS
 */
public class LoginController {
    private final UserModel model;
    private final FormLogin view;
    private boolean signingIn;
    
    public LoginController(UserModel model,FormLogin view){
        this.model = model;
        this.view = view;
    }
    
    public void signIn(){
        if (signingIn){
            return;
        }
        
        final String username = view.getUsername();
        final char[] password = view.getPassword();
        
        view.showError(" ");
        view.setSigningIn(true);
        
        new SwingWorker<vetora.model.User , Void > () {
        
            @Override
            protected User doInBackground() throws Exception {
                try {
                    return  model.login(username, password);
                }
                finally{
                    Arrays.fill(password, '\0');
                }
                
            }
            
            @Override
            protected void done(){
                try {
                    User user = get();
                    
                    FormMain mainForm = new FormMain(user);
                    mainForm.setVisible(true);
                    view.dispose();
                }
                catch (InterruptedException ex){
                    Thread.currentThread().interrupt();
                    view.showError("Sign in interrupted.");
                }
                catch (ExecutionException ex){
                    Throwable cause = ex.getCause();
                    
                    if(cause instanceof ValidationException){
                        view.showError(cause.getMessage());
                    }
                    else {
                        cause.printStackTrace();
                        view.showError("Sign in unavailable. Please try again.");
                    }
                }
                
                finally {
                    view.clearPassword();
                    view.setSigningIn(false);
                }
            }
        }.execute();
    }
}
