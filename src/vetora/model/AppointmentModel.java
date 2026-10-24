/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;

import java.sql.SQLException;
import java.util.List;
import vetora.dao.PetDAO;
import vetora.dao.VeterinarianDAO;
import vetora.exception.ValidationException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import vetora.dao.AppointmentDAO;
/**
 *
 * @author ASUS
 */
public class AppointmentModel {
    
    private final PetDAO petDao = new PetDAO();
    private final VeterinarianDAO vetDao = new VeterinarianDAO();
    private final AppointmentDAO appointmentDao = new AppointmentDAO();

    public List<Pet> getPetOptions() throws SQLException {
        return petDao.getAllPets();
    }

    public Veterinarian getClinicVeterinarian()throws SQLException, ValidationException {

        Veterinarian vet = vetDao.getProfile();

        if (vet == null) {
            throw new ValidationException("Add the clinic veterinarian before booking appointments.");
        }

        return vet;
    }
    
    public long bookAppointment(Long petId,LocalDate date,LocalTime time,String reason) throws SQLException, ValidationException {

        if (petId == null || petId <= 0) {
            throw new ValidationException("Select a pet.");
        }

        if (date == null || time == null) {
        throw new ValidationException("Select an appointment date and time.");
        }

        if (time.isBefore(LocalTime.of(9, 0))|| time.isAfter(LocalTime.of(16, 30))|| time.getMinute() % 30 != 0|| time.getSecond() != 0|| time.getNano() != 0) {
            throw new ValidationException("Choose a 30-minute slot between 09:00 and 16:30.");
        }

        if (!date.atTime(time).isAfter( LocalDateTime.now (ZoneId.of("Asia/Colombo")))) {
            throw new ValidationException("Choose a future appointment date and time.");
        }

        reason = reason == null ? "" : reason.trim();

        if (reason.length() > 255) {
            throw new ValidationException("Reason for visit: maximum 255 characters.");
        }

        Veterinarian vet = getClinicVeterinarian();

        return appointmentDao.createAppointment(  petId, vet.getVeterinarianId(), date, time, reason.isEmpty() ? null : reason);
    }
    
    public List<Appointment> getAppointments(LocalDate date,String status) throws SQLException, ValidationException {

        if (date == null) {
            throw new ValidationException("Select a schedule date.");
        }

        if (status != null && !List.of("Scheduled","Checked-In","Completed","Cancelled","No-Show").contains(status)) {
        throw new ValidationException("Select a valid appointment status.");
        }

        return appointmentDao.getAppointments(date, status);
    }
}
