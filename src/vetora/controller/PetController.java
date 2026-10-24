/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.controller;

import java.util.List;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import java.text.ParseException;
import java.time.LocalDate;
import vetora.exception.ValidationException;
import vetora.model.Owner;
import vetora.model.PetModel;
import vetora.view.PanelPet;
import vetora.model.Pet;
/**
 *
 * @author ASUS
 */
public class PetController {
    private final PetModel model;
    private final PanelPet view;
    private int ownerLoadVersion;
    private boolean savingPet;
    private int petLoadVersion;
    
    public PetController(PetModel model, PanelPet view){
        this.model = model;
        this.view = view;
    }
    
    public void loadOwnerOptions(){
        int requestVersion = ++ownerLoadVersion;
        
        view.setOwnerOptionsLoading(true);
        view.showPetError(" ");
        
        new SwingWorker<List<Owner>, Void>(){
            @Override
            protected List<Owner> doInBackground() throws Exception {
                return model.getOwnerOptions();
            }
            
            @Override
            protected void done(){
                if (requestVersion != ownerLoadVersion){
                    return;
                }
                
                try {
                    List<Owner> owners = get();
                    view.displayOwnerOptions(owners);
                    
                    if (owners.isEmpty()){
                        view.showPetError("Register an owner befor adding apet.");
                    }
                }
                catch (InterruptedException ex){
                    Thread.currentThread().interrupt();
                    view.displayOwnerOptions(Collections.emptyList());
                    
                    view.showPetError("Loading owners interrupted.");
                }
                catch (ExecutionException ex){
                    ex.getCause().printStackTrace();
                    view.displayOwnerOptions(Collections.emptyList());
                    view.showPetError("Could not load owners.");
                }
                finally {
                    view.setOwnerOptionsLoading(false);
                }
            }
        }.execute();
    }
    
    public void savePet() {
        if (savingPet) {
            return;
        }

        Long petId = view.getEditingPetId();
        Long ownerId = view.getSelectedOwnerId();
        String petName = view.getPetName();
        String species = view.getSpecies();
        String gender = view.getGender();
        String breed = view.getBreed();

        final LocalDate dob;

        try {
            dob = view.getBirthDay();
        } 
        catch (ParseException ex) {
            view.showPetError("Enter a valid date as DD/MM/YYYY, or mark DOB as unknown.");
            return;
        }

        savingPet = true;
        view.setSavingPet(true);
        view.showPetError(" ");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                model.savePet(petId,ownerId, petName, species, gender, breed, dob);
                return null;
            }

            @Override
            protected void done() {
                boolean saved = false;

                try {
                    get();
                    saved = true;
                } 
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showPetError("Saving interrupted.");
                } 
                catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();

                    if (cause instanceof ValidationException) {
                        view.showPetError(cause.getMessage());
                    } 
                    else {
                        cause.printStackTrace();
                        view.showPetError("Could not save pet. Please try again.");
                    }
                } 
                finally {
                    savingPet = false;
                    view.setSavingPet(false);
                }

                if (saved) {
                    view.clearPetForm();
                    loadPets();
                    view.showPetSaved();
                }
            }
        }.execute();
    }
    
    public void newPet(){
        if (!savingPet){
            view.clearPetForm();
        }
    }
    
    public void loadPets() {
        int requestVersion = ++petLoadVersion;
        view.showPetListMessage("Loading pets...");

        new SwingWorker<List<Pet>, Void>() {
            @Override
            protected List<Pet> doInBackground() throws Exception {
                return model.getAllPets();
            }

            @Override
            protected void done() {
                if (requestVersion != petLoadVersion) {
                    return;
                }

                try {
                    List<Pet> pets = get();
                    view.displayPets(pets);
                } 
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showPetListMessage("Loading interrupted.");
                } 
                catch (ExecutionException ex) {
                    ex.getCause().printStackTrace();
                    view.showPetListMessage("Could not load pets.");
                }
            }
        }.execute();
    }

    public void selectPet() {
        if (savingPet){
            return;
        }
        Pet selectedPet = view.getSelectedPet();
        
        if (selectedPet != null){
            view.showPetDetails(selectedPet);
        }
    }
}
