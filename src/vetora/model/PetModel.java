/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;

import java.util.List;
import vetora.dao.OwnerDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import vetora.dao.PetDAO;
import vetora.exception.ValidationException;

/**
 *
 * @author ASUS
 */
public class PetModel {
   private final OwnerDAO ownerDao = new OwnerDAO();
   private final PetDAO petDao = new PetDAO();
   
   public List<Owner> getOwnerOptions() throws SQLException {
       return ownerDao.getAllOwners();
   }
   
   public void savePet(Long petId, Long ownerId, String petName, String species, String gender, String breed, LocalDate dob) throws  ValidationException, SQLException {
       if(petId != null && petId <=0){
           throw new ValidationException("Select a valid pet to update.");
       }
       
       petName = petName == null ? "" : petName.trim();
       breed = breed == null ? "" : breed.trim();
       
       if (ownerId == null || ownerId <= 0){
           throw new ValidationException("Select the pet's owner.");
       }
       if (petName.isEmpty()|| petName.length() > 100){
           throw new ValidationException("Enter a pet name of 1-100 characters.");
       }
       if (!Arrays.asList("Dog","Cat","Bird","Other").contains(species)){
           throw new ValidationException("Select a species.");
       }
       if (!Arrays.asList("Male","Female","Unknown").contains(gender)){
           throw  new ValidationException("Select a gender.");
       }
       if (breed.length() > 100){
           throw new ValidationException("Breed must not exceed 100 characters.");
       }
       
       LocalDate today = LocalDate.now(ZoneId.of("Asia/Colombo"));
       if (dob != null && dob.isAfter(today)){
           throw new ValidationException("Date of birth cannot be in the future.");
       }
       
       String savedBreed = breed.isEmpty() ? null : breed;
       
       if(petId == null){
           petDao.createPet(ownerId, petName, species, gender, breed, dob);
       }
       else{
           petDao.updatePet(petId, ownerId, petName, species, gender, savedBreed, dob);
       }
   }
   
   public List<Pet> getAllPets() throws SQLException {
       return petDao.getAllPets();
   }
}
