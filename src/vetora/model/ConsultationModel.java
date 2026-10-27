/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.model;

import java.sql.SQLException;
import java.util.List;
import vetora.dao.ConsultationDAO;
import vetora.exception.ValidationException;

/**
 * @author ASUS
 */
public class ConsultationModel {
    private final ConsultationDAO dao = new ConsultationDAO();

    public List<Consultation> getVisits(Long appointmentId)
            throws SQLException, ValidationException {
        if (appointmentId != null && appointmentId <= 0) {
            throw new ValidationException("Select an appointment.");
        }
        List<Consultation> visits = dao.getVisits(appointmentId);
        if (appointmentId != null && visits.isEmpty()) {
            throw new ValidationException("This visit is unavailable. Reopen Consultations to refresh.");
        }
        return visits;
    }

    public PrescriptionItem createPrescriptionItem(String medicineName, int quantity, String doseText, String frequency, String duration, String instructions) throws ValidationException {
        PrescriptionItem item = new PrescriptionItem(medicineName, "External pharmacy", quantity, doseText, frequency, duration, instructions);
        validatePrescriptionItem(item);
        return item;
    }

    private void validatePrescriptionItem(PrescriptionItem item)
            throws ValidationException {
        if (item == null || item.getMedicineName().isEmpty()
                || item.getMedicineName().length() > 150) {
            throw new ValidationException("Enter a medicine name of 1-150 characters.");
        }
        if (!"External pharmacy".equals(item.getFulfilment())) {
            throw new ValidationException("Only external pharmacy prescriptions are supported.");
        }
        if (item.getQuantity() < 1 || item.getQuantity() > 9999) {
            throw new ValidationException("Quantity must be a whole number from 1 to 9999.");
        }
        if (item.getDoseText().length() > 100
                || item.getFrequency().length() > 100
                || item.getDuration().length() > 100) {
            throw new ValidationException("Use at most 100 characters for dose, frequency and duration.");
        }
        if (item.getInstructions().length() > 2000) {
            throw new ValidationException("Use at most 2,000 characters for prescription instructions.");
        }
    }

    public void validateConsultation(Long appointmentId, String symptoms,
            String diagnosis, String treatment, List<PrescriptionItem> items,
            boolean finalized) throws ValidationException {
        if (appointmentId == null || appointmentId <= 0) {
            throw new ValidationException("Load a visit before saving.");
        }
        symptoms = cleanText(symptoms);
        diagnosis = cleanText(diagnosis);
        treatment = cleanText(treatment);
        if (items == null) {
            throw new ValidationException("Reload the visit before saving prescription items.");
        }
        if (symptoms.isEmpty() && diagnosis.isEmpty() && treatment.isEmpty() && items.isEmpty()) {
            throw new ValidationException("Enter clinical notes or add a prescription item.");
        }
        if (symptoms.length() > 10000 || diagnosis.length() > 10000 || treatment.length() > 10000) {
            throw new ValidationException("Use at most 10,000 characters per clinical field.");
        }
        if (finalized && (symptoms.isEmpty() || diagnosis.isEmpty())) {
            throw new ValidationException("Symptoms and diagnosis are required when finalizing.");
        }
        if (items.size() > 100) {
            throw new ValidationException("Use at most 100 prescription items per consultation.");
        }
        for (int i = 0; i < items.size(); i++) {
            PrescriptionItem item = items.get(i);
            validatePrescriptionItem(item);
            if (finalized && (item.getDoseText().isEmpty()
                    || item.getFrequency().isEmpty() || item.getDuration().isEmpty())) {
                throw new ValidationException("Complete dose, frequency and duration for every prescribed item before finalizing.");
            }
            for (int j = 0; j < i; j++) {
                if (item.sameDetails(items.get(j))) {
                    throw new ValidationException("Remove duplicate prescription items before saving.");
                }
            }
        }
    }

    public void saveDraft(Long appointmentId, String symptoms, String diagnosis, String treatment, List<PrescriptionItem> items) throws SQLException, ValidationException {
        save(appointmentId, symptoms, diagnosis, treatment, items, false);
    }

    public void finalizeConsultation(Long appointmentId, String symptoms,String diagnosis, String treatment, List<PrescriptionItem> items) throws SQLException, ValidationException {
        save(appointmentId, symptoms, diagnosis, treatment, items, true);
    }

    private void save(Long appointmentId, String symptoms, String diagnosis, String treatment, List<PrescriptionItem> items, boolean finalized) throws SQLException, ValidationException {
        validateConsultation(appointmentId, symptoms, diagnosis, treatment, items, finalized);
        dao.saveConsultation(appointmentId, cleanText(symptoms), cleanText(diagnosis), cleanText(treatment), items, finalized);
    }

    private String cleanText(String text) {
        return text == null ? "" : text.trim();
    }
    
    public String createPriscription(Consultation visit) throws ValidationException {
        if (visit == null){
            throw new ValidationException("Load a consultation first.");
        }
        if (!"Finalized".equals(visit.getStatus())){
            throw new ValidationException("Finalize the consultation befor printing.");
        }
        List<PrescriptionItem> items = visit.getPrescriptionItems();
        
        if (items.isEmpty()){
            throw new ValidationException("No priscription items to print.");
        }
        
        StringBuilder txt = new StringBuilder();
        txt.append("VETORA\n");
        txt.append("Veterinary Clinic Management System\n");
        txt.append("PRESCRIPTION\n\n");
        txt.append("Appointment: #").append(visit.getAppointmentId()).append("\n");
        txt.append("Visit date: ").append(visit.getAppointmentDate()).append("\n");
        txt.append("Owner: ").append(visit.getOwnerName()).append("\n");
        txt.append("Pet: ").append(visit.getPetName()).append("\n");
        txt.append("Species: ").append(visit.getSpecies()).append("\n");
        txt.append("Veterinarian: ").append(visit.getVeterinarianName()).append("\n\n");
        
        for (int i = 0; i < items.size(); i++) {
            PrescriptionItem item = items.get(i);

            txt.append(i + 1).append(". ").append(item.getMedicineName()).append("\n");
            txt.append("Quantity: ").append(item.getQuantity()).append("\n");
            txt.append("Dose: ").append(item.getDoseText()).append("\n");
            txt.append("Frequency: ").append(item.getFrequency()).append("\n");
            txt.append("Duration: ").append(item.getDuration()).append("\n");

            if (!item.getInstructions().isEmpty()) {
                txt.append("Instructions: ").append(item.getInstructions()).append("\n");
            }

            txt.append("\n");
        }
        txt.append("Veterinarian signature: ____________________\n");
        
        return txt.toString();
    }
}
