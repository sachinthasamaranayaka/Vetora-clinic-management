/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import vetora.config.DBConnection;
import vetora.exception.ValidationException;
import vetora.model.Consultation;
import vetora.model.PrescriptionItem;

/**
 * @author ASUS
 */
public class ConsultationDAO {
    public List<Consultation> getVisits(Long appointmentId) throws SQLException {
        String sql = "SELECT a.appointment_id, a.appointment_date, a.appointment_time, "
                + "p.pet_name, p.species, p.date_of_birth, "
                + "o.full_name AS owner_name, v.full_name AS vet_name, "
                + "c.symptoms, c.diagnosis, c.treatment, c.clinical_notes, "
                + "c.status AS consultation_status "
                + "FROM appointments a "
                + "JOIN pets p ON p.pet_id = a.pet_id "
                + "JOIN owners o ON o.owner_id = p.owner_id "
                + "JOIN veterinarians v ON v.veterinarian_id = a.veterinarian_id "
                + "LEFT JOIN consultations c ON c.appointment_id = a.appointment_id "
                + "WHERE ((a.status = 'Checked-In' "
                + "AND (c.appointment_id IS NULL OR c.status = 'Draft')) "
                + "OR c.status = 'Finalized') ";
        if (appointmentId != null) {
            sql += "AND a.appointment_id = ? ";
        }
        sql += "ORDER BY a.appointment_date DESC, a.appointment_time, a.appointment_id";
        List<Consultation> visits = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            // Read the notes and medicine list from the same database snapshot.
            con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            con.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    if (appointmentId != null) {
                        stmt.setLong(1, appointmentId);
                    }
                    try (ResultSet result = stmt.executeQuery()) {
                        while (result.next()) {
                            Date dob = result.getDate("date_of_birth");
                            visits.add(new Consultation(
                                    result.getLong("appointment_id"),
                                    result.getDate("appointment_date").toLocalDate(),
                                    result.getTime("appointment_time").toLocalTime(),
                                    result.getString("pet_name"), result.getString("owner_name"),
                                    result.getString("species"), dob == null ? null : dob.toLocalDate(),
                                    result.getString("vet_name"), result.getString("symptoms"),
                                    result.getString("diagnosis"), result.getString("treatment"),
                                    result.getString("clinical_notes"), result.getString("consultation_status"),
                                    new ArrayList<PrescriptionItem>()));
                        }
                    }
                }
                if (appointmentId != null && !visits.isEmpty()) {
                    visits.set(0, visits.get(0).withPrescriptionItems(
                            getPrescriptionItems(con, appointmentId)));
                }
                con.commit();
            }
            catch (SQLException ex) {
                try {
                    con.rollback();
                }
                catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            }
        }
        return visits;
    }

    private List<PrescriptionItem> getPrescriptionItems(Connection con, long appointmentId)
            throws SQLException {
        List<PrescriptionItem> items = new ArrayList<>();
        String sql = "SELECT medicine_name, fulfilment, quantity, dose_text, "
                + "frequency, duration, instructions FROM prescription_items "
                + "WHERE appointment_id = ? ORDER BY prescription_item_id";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setLong(1, appointmentId);
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    items.add(new PrescriptionItem(result.getString("medicine_name"),
                            result.getString("fulfilment"), result.getInt("quantity"),
                            result.getString("dose_text"), result.getString("frequency"),
                            result.getString("duration"), result.getString("instructions")));
                }
            }
        }
        return items;
    }

    public void saveConsultation(long appointmentId, String symptoms,
            String diagnosis, String treatment, List<PrescriptionItem> items,
            boolean finalized) throws SQLException, ValidationException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Always lock the appointment before its consultation.
                try (PreparedStatement stmt = con.prepareStatement(
                        "SELECT status FROM appointments WHERE appointment_id = ? FOR UPDATE")) {
                    stmt.setLong(1, appointmentId);
                    try (ResultSet result = stmt.executeQuery()) {
                        if (!result.next() || !"Checked-In".equals(result.getString("status"))) {
                            throw new ValidationException("Only checked-in appointments can be saved or finalized. Reload the visit.");
                        }
                    }
                }
                boolean exists;
                try (PreparedStatement stmt = con.prepareStatement(
                        "SELECT status FROM consultations WHERE appointment_id = ? FOR UPDATE")) {
                    stmt.setLong(1, appointmentId);
                    try (ResultSet result = stmt.executeQuery()) {
                        exists = result.next();
                        if (exists && !"Draft".equals(result.getString("status"))) {
                            throw new ValidationException("Finalized consultations cannot be edited. Reload the visit.");
                        }
                    }
                }
                String sql;
                if (exists) {
                    sql = "UPDATE consultations SET symptoms = ?, diagnosis = ?, treatment = ?, "
                            + "started_at = COALESCE(started_at, CURRENT_TIMESTAMP) "
                            + "WHERE appointment_id = ? AND status = 'Draft'";
                }
                else {
                    sql = "INSERT INTO consultations "
                            + "(symptoms, diagnosis, treatment, appointment_id, status, started_at) "
                            + "VALUES (?, ?, ?, ?, 'Draft', CURRENT_TIMESTAMP)";
                }
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, symptoms);
                    stmt.setString(2, diagnosis);
                    stmt.setString(3, treatment);
                    stmt.setLong(4, appointmentId);
                    int affected = stmt.executeUpdate();
                    if (!exists && affected != 1) {
                        throw new SQLException("Consultation was not inserted.");
                    }
                }
                savePrescriptionItems(con, appointmentId, items);
                if (finalized) {
                    try (PreparedStatement stmt = con.prepareStatement(
                            "UPDATE consultations SET status = 'Finalized', finalized_at = CURRENT_TIMESTAMP "
                            + "WHERE appointment_id = ? AND status = 'Draft'")) {
                        stmt.setLong(1, appointmentId);
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("Consultation was not finalized.");
                        }
                    }
                    try (PreparedStatement stmt = con.prepareStatement(
                            "UPDATE appointments SET status = 'Completed' "
                            + "WHERE appointment_id = ? AND status = 'Checked-In'")) {
                        stmt.setLong(1, appointmentId);
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("Appointment was not completed.");
                        }
                    }
                }
                con.commit();
            }
            catch (SQLException | ValidationException ex) {
                try {
                    con.rollback();
                }
                catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            }
        }
    }

    private void savePrescriptionItems(Connection con, long appointmentId,
            List<PrescriptionItem> items) throws SQLException {
        // Replacing a draft's items is part of the same save transaction.
        try (PreparedStatement stmt = con.prepareStatement(
                "DELETE FROM prescription_items WHERE appointment_id = ?")) {
            stmt.setLong(1, appointmentId);
            stmt.executeUpdate();
        }
        String sql = "INSERT INTO prescription_items "
                + "(appointment_id, medicine_name, fulfilment, quantity, dose_text, "
                + "frequency, duration, instructions) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (PrescriptionItem item : items) {
                stmt.setLong(1, appointmentId);
                stmt.setString(2, item.getMedicineName());
                stmt.setString(3, item.getFulfilment());
                stmt.setInt(4, item.getQuantity());
                stmt.setString(5, item.getDoseText());
                stmt.setString(6, item.getFrequency());
                stmt.setString(7, item.getDuration());
                stmt.setString(8, item.getInstructions());
                if (stmt.executeUpdate() != 1) {
                    throw new SQLException("Prescription item was not inserted.");
                }
            }
        }
    }
}
