/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.controller;

import vetora.util.AccessControl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import vetora.exception.ValidationException;
import vetora.model.User;
import vetora.model.UserModel;
import vetora.view.PanelUser;
import vetora.view.DialogUser;

/**
 *
 * @author ASUS
 */
public class UserController {
    private final AccessControl accessControl;
    private final UserModel model;
    private final PanelUser view;
    private final long currentUserId;
    private int userLoadVersion;
    private boolean dialogOpen;

    public UserController(UserModel model, PanelUser view, long currentUserId, AccessControl accessControl){
        if (accessControl == null) {
            throw new IllegalArgumentException("Access control is required.");
        }
        this.accessControl = accessControl;
        this.model = model;
        this.view = view;
        this.currentUserId = currentUserId;
    }

    public void loadUsers(){
        if (!accessControl.canManageUsers()) {
            view.showUserListMessage("You do not have permission to perform this action.");
            return;
        }
        if (dialogOpen) {
            return;
        }
        int requestVersion = ++userLoadVersion;
        String keyword = view.getUserSearch();
        view.setUserListBusy(true);
        view.showUserListMessage("Loading users...");
        new SwingWorker<List<User>, Void>(){
            @Override
            protected List<User> doInBackground() throws Exception {
                return model.searchUsers(keyword);
            }
            @Override
            protected void done(){
                if (requestVersion != userLoadVersion) {
                    return;
                }
                try {
                    view.displayUser(get());
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showUserListMessage("Loading interrupted.");
                }
                catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (!(cause instanceof ValidationException)) {
                        cause.printStackTrace();
                    }
                    view.showUserListMessage(cause instanceof ValidationException ? cause.getMessage()
                            : "Could not load users. Click Search to retry.");
                }
                finally {
                    view.setUserListBusy(false);
                }
            }
        }.execute();
    }

    public void newUser(){
        if (!accessControl.canManageUsers()) {
            view.showUserListMessage("You do not have permission to perform this action.");
            return;
        }
        openDialog(null);
    }

    public void editUser(){
        if (!accessControl.canManageUsers()) {
            view.showUserListMessage("You do not have permission to perform this action.");
            return;
        }
        User selected = view.getSelectedUser();
        if (selected == null) {
            view.showUserListMessage("Select a user to edit.");
            return;
        }
        openDialog(selected);
    }

    private void openDialog(User selected){
        if (!accessControl.canManageUsers()) {
            view.showUserListMessage("You do not have permission to perform this action.");
            return;
        }
        if (dialogOpen || view.isUserListBusy()) {
            return;
        }
        dialogOpen = true;
        try {
            DialogUser dialog = view.createUserDialog(selected);
            UserDialogController dialogController = new UserDialogController(model, dialog, currentUserId, accessControl);
            dialog.setController(dialogController);
            dialog.setVisible(true);
            if (dialog.getSavedUser() != null) {
                view.userSaved(dialog.getSavedUser());
            }
        }
        finally {
            dialogOpen = false;
        }
        loadUsers();
    }
}
