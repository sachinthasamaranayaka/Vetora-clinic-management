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
        if (savingPet) {
            return;
        }
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
                        view.showPetError("Register an owner before adding a pet.");
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
        if (savingPet || view.isOwnerOptionsLoading()) {
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

        petLoadVersion++;
        view.setPetListBusy(false);
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
        if (!savingPet && view.confirmDiscardPetChanges()){
            view.clearPetForm();
        }
    }
    
    public void loadPets() {
        if (savingPet) {
            return;
        }
        String keyword = view.getPetSearch();
        String species = view.getSpeciesFilter();
        view.setPetListBusy(true);
        int requestVersion = ++petLoadVersion;
        view.showPetListMessage("Loading pets...");

        new SwingWorker<List<Pet>, Void>() {
            @Override
            protected List<Pet> doInBackground() throws Exception {
                return model.searchPets(keyword, species);
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
                    Throwable cause = ex.getCause();
                    view.showPetListMessage(cause instanceof ValidationException ? cause.getMessage() : "Could not load pets. Click Search to retry.");
                }
                finally {
                    view.setPetListBusy(false);
                }
            }
        }.execute();
    }

    public void selectPet() {
        if (savingPet || view.isRestoringPetSelection()) {
            return;
        }
        Pet selectedPet = view.getSelectedPet();
        if (selectedPet != null) {
            if (view.confirmDiscardPetChanges()) {
                view.showPetDetails(selectedPet);
            }
            else {
                view.restorePetSelection();
            }
        }
    }

    public void deletePet(){
        if (savingPet || view.isOwnerOptionsLoading()) {
            return;
        }
        Long id = view.getEditingPetId();
        if (id == null || !view.confirmDeletePet()) {
            return;
        }
        savingPet = true;
        petLoadVersion++;
        view.setPetListBusy(false);
        view.setSavingPet(true);
        view.showPetError("Deleting pet...");
        new SwingWorker<Void, Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                model.deletePet(id);
                return null;
            }
            @Override
            protected void done(){
                boolean deleted = false;
                try {
                    get();
                    deleted = true;
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showPetError("Deletion interrupted. Refresh before retrying.");
                }
                catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (!(cause instanceof ValidationException)) {
                        cause.printStackTrace();
                    }
                    view.showPetError(cause instanceof ValidationException ? cause.getMessage() : "Could not delete pet. Refresh before retrying.");
                }
                finally {
                    savingPet = false;
                    view.setSavingPet(false);
                }
                if (deleted) {
                    view.clearPetForm();
                    loadPets();
                    view.showPetError("Pet deleted.");
                }
            }
        }.execute();
    }
}
