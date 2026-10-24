/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;

/**
 *
 * @author ASUS
 */
public class Veterinarian {
    private final Long veterinarianId;
    private final String fullName;
    private final String registrationNumber;
    private final String specialization;
    private final String phone;
    private final String email;
    private final boolean available;
    private final boolean active;

    public Veterinarian(Long veterinarianId, String fullName, String registrationNumber, String specialization, String phone, String email, boolean available, boolean active) {
        this.veterinarianId = veterinarianId;
        this.fullName = fullName;
        this.registrationNumber = registrationNumber;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
        this.available = available;
        this.active = active;
    }

    public Long getVeterinarianId() {
        return veterinarianId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isActive() {
        return active;
    }
    
    
}
