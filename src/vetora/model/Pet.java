/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;


import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Period;
import java.sql.Date;
/**
 *
 * @author ASUS
 */
public class Pet {
    private final long petID;
    private final long ownerID;
    private final String petName;
    private final String species;
    private final String gender;
    private final String breed;
    private final Date dob;
    private final String ownerName;

    public Pet(long petID, long ownerID, String petName, String species, String gender, String breed, Date dob, String ownerName) {
        this.petID = petID;
        this.ownerID = ownerID;
        this.petName = petName;
        this.species = species;
        this.gender = gender;
        this.breed = breed;
        this.dob = dob;
        this.ownerName = ownerName;
    }

    public long getPetID() {
        return petID;
    }

    public long getOwnerID() {
        return ownerID;
    }

    public String getPetName() {
        return petName;
    }

    public String getSpecies() {
        return species;
    }

    public String getGender() {
        return gender;
    }

    public String getBreed() {
        return breed;
    }

    public Date getDob() {
        return dob;
    }
    
    public String getOwnerName(){
        return ownerName;
    }
    
    public Integer getAge(){
        if (dob == null){
            return null;
        }
        
        ZoneId zone = ZoneId.of("Asia/Colombo");
        LocalDate today = LocalDate.now(zone);
        
        LocalDate birthDate;
        
        if (dob instanceof Date){
            birthDate = ((Date)dob).toLocalDate();
        }
        else {
            birthDate = dob.toInstant().atZone(zone).toLocalDate();
        }
        
        return Period.between(birthDate, today).getYears();
    }

    
}
