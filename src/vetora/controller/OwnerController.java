/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.controller;


import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import vetora.model.OwnerModel;
import vetora.model.Owner;
import vetora.view.PanelOwner;
import vetora.exception.ValidationException;
import vetora.util.AccessControl;
/**
 *
 * @author ASUS
 */
public class OwnerController {
    private final OwnerModel model;
    private final PanelOwner view;
    private final AccessControl accessControl;
    
    private boolean savingOwner;
    private int ownerLoadVersion;

    public OwnerController(OwnerModel model, PanelOwner view, AccessControl accessControl) {
        if (accessControl == null){
            throw new IllegalArgumentException("Access control is required.");
        }
        this.model = model;
        this.view = view;
        this.accessControl = accessControl;
        view.setOwnerPermissions(accessControl.canEditOwnersAndPets(), accessControl.canDeleteOwnersAndPets());
    }
    
    
    
    public void loadOwners(){
        if (!accessControl.canViewOwnwrsAndPets()) {
            view.showOwnerError("You do not have permission to view owners.");
            return;
        }
        
        String keyword = view.getOwnerSearch();
        int requestVersion = ++ownerLoadVersion;
        
        view.showOwnerListMessage("Loading users...");
        
        new SwingWorker<List<Owner>,Void>(){
            @Override
            protected List<Owner> doInBackground() throws Exception {
                return model.searchOwners(keyword);
            }
            @Override
            protected void done(){
                if (requestVersion != ownerLoadVersion){
                    return;
                }
                
                try{
                    List<Owner> owners = get();
                    view.displayOwners(owners);
                    
                    if (owners.isEmpty()){
                        view.showOwnerListMessage("No owners found.");
                    }
                }
                catch (InterruptedException ex){
                    Thread.currentThread().interrupt();
                    view.showOwnerListMessage("Loading interrupted.");
                }
                catch (ExecutionException ex){
                    ex.getCause().printStackTrace();
                    view.showOwnerListMessage("Could not load owners.");
                }
            }
        }.execute();
    }
    
    public void saveOwner(){
        if (!accessControl.canEditOwnersAndPets()){
            view.showOwnerError("You do not have permission to add or update owners.");
            return;
        }
        
        if (savingOwner){
            return ;
        }
        
        Long ownerId = view.getEditingOwnerId();
        String fullName = view.getOwnerName();
        String phone = view.getOwnerPhone();
        String email = view.getOwnerEmail();
        String address = view.getOwnerAddress();
        
        savingOwner = true;
        view.setSavingOwner(true);
        view.showOwnerError(" ");
        
        new SwingWorker<Void, Void>(){
            @Override
            protected Void doInBackground()throws Exception {
                model.saveOwner(ownerId, fullName, phone, email, address);
                return null;
            }
            
            @Override
            protected void done(){
                boolean saved = false;
                
                try{
                    get();
                    saved = true;
                }
                catch (InterruptedException ex){
                    Thread.currentThread().interrupt();
                    view.showOwnerError("Saving interrupted.");
                }
                catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    
                    if (cause instanceof ValidationException){
                        view.showOwnerError(cause.getMessage());
                    }
                    else{
                        cause.printStackTrace();
                        view.showOwnerError("Could not save owner. Check the database connection.");
                    }
                }
                finally{
                    savingOwner = false;
                    view.setSavingOwner(false);
                }
                
                if (saved){
                    view.clearOwnerForm();
                    loadOwners();
                    view.showOwnerSaved();
                }
            }
        }.execute();
    }
    
    public void newOwner(){
        if (!accessControl.canEditOwnersAndPets()) {
            view.showOwnerError("You do not have permission to add owners.");
            return;
        }
        
        if (!savingOwner){
            view.clearOwnerForm();
        }
    }
    
    public void selectOwner(){
        if (!accessControl.canViewOwnwrsAndPets()) {
            view.showOwnerError("You do not have permission to view owners.");
            return;
        }
        
        if (savingOwner){
            return;
        }
        
        Owner owner = view.getSelectedOwner();
        
        if (owner != null){
            view.showOwnerDetails(owner);
        }
    }
    
    public void deleteOwner(){
        if (!accessControl.canDeleteOwnersAndPets()){
            view.showOwnerError("Only an addministrator can delete owners.");
            return;
        }
        
        if (savingOwner){
            return;
        }
        
        Long ownerId = view.getEditingOwnerId();
        
        if (ownerId == null){
            view.showOwnerError("Select an owner to delete.");
            return;
        }
        
        if (!view.confirmOwnerDelete(ownerId)){
            return;
        }
        
        savingOwner = true;
        view.setDeletingOwner(true);
        view.showOwnerError(" ");
        
        new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                model.deleteOwner(ownerId);
                return null;
            }
            
            @Override
            protected void done(){
                boolean deleted = false;
                
                try {
                    get();
                    deleted = true;
                }
                catch (InterruptedException ex){
                    Thread.currentThread().interrupt();
                    view.showOwnerError("Deleting interrupted.");
                }
                catch (ExecutionException ex){
                    Throwable cause = ex.getCause();
                    
                    if (cause instanceof ValidationException){
                        view.showOwnerError(cause.getMessage());
                    }
                    else {
                        cause.printStackTrace();
                        view.showOwnerError("Could not delete owner. Please try again.");
                    }
                }
                finally {
                    savingOwner =false;
                    view.setDeletingOwner(false);
                }
                
                if (deleted){
                    view.clearOwnerForm();
                    loadOwners();
                    view.ShowOwnerDeleted();
                }
            }
        }.execute();
    }
}
