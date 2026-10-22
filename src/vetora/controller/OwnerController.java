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
/**
 *
 * @author ASUS
 */
public class OwnerController {
    private final OwnerModel model;
    private final PanelOwner view;
    
    private boolean savingOwner;
    private int ownerLoadVersion;
    
    public OwnerController(OwnerModel model, PanelOwner view){
        this.model = model;
        this.view = view;
    }
    
    public void loadOwners(){
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
        if (savingOwner){
            return ;
        }
        
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
                model.createOwner(fullName, phone, email, address);
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
        if (!savingOwner){
            view.clearOwnerForm();
        }
    }
}
