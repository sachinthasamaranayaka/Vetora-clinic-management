/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ASUS
 */
public class Consultation {
    private final long appointmentId;
    private final LocalDate appointmentDate;
    private final LocalTime appointmentTime;
    private final String petName;
    private final String ownerName;
    private final String species;
    private final LocalDate birthDate;
    private final String veterinarianName;
    private final String symptoms;
    private final String diagnosis;
    private final String treatment;
    private final String clinicalNotes;
    private final String status;
    private final List<PrescriptionItem> prescriptionItems;

    public Consultation(long appointmentId, LocalDate appointmentDate, LocalTime appointmentTime, String petName, String ownerName, String species, LocalDate birthDate, String veterinarianName, String symptoms, String diagnosis, String treatment, String clinicalNotes, String status, List<PrescriptionItem> prescriptionItems) {
        this.appointmentId = appointmentId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.petName = petName;
        this.ownerName = ownerName;
        this.species = species;
        this.birthDate = birthDate;
        this.veterinarianName = veterinarianName;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.clinicalNotes = clinicalNotes;
        this.status = status;
        this.prescriptionItems = new ArrayList<>(prescriptionItems);
    }

    public long getAppointmentId() {
        return appointmentId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public String getPetName() {
        return petName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getSpecies() {
        return species;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getVeterinarianName() {
        return veterinarianName;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public String getStatus() {
        return status;
    }
    
    
    public String getAgeText(){
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Colombo"));
        
        if (birthDate == null || birthDate.isAfter(today)){
            return "Unknown";
        }
        
        Period age = Period.between(birthDate, today);
        
        if (age.getYears() > 0){
            return age.getYears() + " year(s)";
        }
        
        if(age.getMonths() > 0){
            return age.getMonths() + " month(s)";
        }
        
        return "Less than 1 month";
    }
    
    
    public List<PrescriptionItem> getPrescriptionItems() {
        return new ArrayList<>(prescriptionItems);
    }

    public Consultation withPrescriptionItems(List<PrescriptionItem> items) {
        return new Consultation(appointmentId, appointmentDate, appointmentTime,
                petName, ownerName, species, birthDate, veterinarianName,
                symptoms, diagnosis, treatment, clinicalNotes, status, items);
    }

    public Consultation withSavedDetails(String symptoms, String diagnosis,
            String treatment, List<PrescriptionItem> items, boolean finalized) {
        return new Consultation(appointmentId, appointmentDate, appointmentTime,
                petName, ownerName, species, birthDate, veterinarianName,
                symptoms, diagnosis, treatment, clinicalNotes,
                finalized ? "Finalized" : "Draft", items);
    }
}
