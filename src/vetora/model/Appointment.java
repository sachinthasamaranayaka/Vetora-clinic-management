/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author ASUS
 */
public class Appointment {
    private final long appointmentId;
    private final long petId;
    private final long veterinarianId;
    private final LocalDate appointmentDate;
    private final LocalTime appointmentTime;
    private final String reason;
    private final String status;
    private final String petName;
    private final String ownerName;
    private final String veterinarianName;

    public Appointment(long appointmentId, long petId, long veterinarianId, LocalDate appointmentDate, LocalTime appointmentTime, String reason, String status, String petName, String ownerName, String veterinarianName) {
        this.appointmentId = appointmentId;
        this.petId = petId;
        this.veterinarianId = veterinarianId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.status = status;
        this.petName = petName;
        this.ownerName = ownerName;
        this.veterinarianName = veterinarianName;
    }

    

    public long getAppointmentId() {
        return appointmentId;
    }

    public long getPetId() {
        return petId;
    }

    public long getVeterinarianId() {
        return veterinarianId;
    }
    
    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    public String getPetName() {
        return petName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getVeterinarianName() {
        return veterinarianName;
    }

}
