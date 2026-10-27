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
import vetora.view.DialogUser;

/**
 *
 * @author ASUS
 */
public class UserDialogController {
    private final UserModel model;
    private final DialogUser view;
    private final long currentUserId;
    private boolean savingUser;

    public UserDialogController(UserModel model, DialogUser view, long currentUserId){
        this.model = model;
        this.view = view;
        this.currentUserId = currentUserId;
    }

    public void saveUser(){
        if (savingUser) {
            return;
        }
        User original = view.getOriginalUser();
        String fullName = view.getUserFullName();
        String username = view.getUserUsername();
        String role = view.getUserRole();
        boolean active = view.isUserActive();
        char[] password = view.getUserPassword();
        char[] confirmation = view.getUserConfirmation();
        savingUser = true;
        view.setSavingUser(true);
        view.showUserError(" ");
        new SwingWorker<User, Void>(){
            @Override
            protected User doInBackground() throws Exception {
                try {
                    return model.saveUser(original, fullName, username, role, active,
                            password, confirmation, currentUserId);
                }
                finally {
                    Arrays.fill(password, '\0');
                    Arrays.fill(confirmation, '\0');
                }
            }
            @Override
            protected void done(){
                User saved = null;
                try {
                    saved = get();
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showUserError("Saving interrupted. Refresh the user list before retrying.");
                }
                catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof ValidationException) {
                        view.showUserError(cause.getMessage());
                    }
                    else {
                        cause.printStackTrace();
                        view.showUserError("Could not confirm the save. Refresh the user list before retrying.");
                    }
                }
                finally {
                    savingUser = false;
                    view.setSavingUser(false);
                }
                if (saved != null) {
                    view.completeUserSave(saved);
                }
            }
        }.execute();
    }
}
