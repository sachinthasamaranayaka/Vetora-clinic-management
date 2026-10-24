/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import vetora.config.DBConnection;
import vetora.exception.ValidationException;
import vetora.model.Appointment;
/**
 *
 * @author ASUS
 */
public class AppointmentDAO {
    
    public long createAppointment(long petId, long vetId, LocalDate date, LocalTime time, String reason) throws SQLException, ValidationException {

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                String vetSql = "SELECT status, availability FROM veterinarians WHERE veterinarian_id = ? FOR UPDATE";

                try (PreparedStatement stmt = con.prepareStatement(vetSql)) {
                    stmt.setLong(1, vetId);

                    try (ResultSet result = stmt.executeQuery()) {
                        if (!result.next() || !"Active".equals(result.getString("status")) || !"Available".equals(result.getString("availability"))) {
                            throw new ValidationException("The clinic veterinarian is unavailable.");
                        }
                    }
                }

                String petSql = "SELECT pet_id FROM pets WHERE pet_id = ? FOR UPDATE";

                try (PreparedStatement stmt = con.prepareStatement(petSql)) {
                    stmt.setLong(1, petId);

                    try (ResultSet result = stmt.executeQuery()) {
                        if (!result.next()) {
                            throw new ValidationException("This pet no longer exists. Reload the Appointments page.");
                        }
                    }
                }

                String clashSql = "SELECT appointment_id FROM appointments WHERE veterinarian_id = ? AND appointment_date = ? AND status <> 'Cancelled' AND appointment_time > ? AND appointment_time < ? LIMIT 1 FOR UPDATE";

                try (PreparedStatement stmt = con.prepareStatement(clashSql)) {
                    stmt.setLong(1, vetId);
                    stmt.setDate(2, Date.valueOf(date));
                    stmt.setTime(3, Time.valueOf(time.minusMinutes(30)));
                    stmt.setTime(4, Time.valueOf(time.plusMinutes(30)));

                    try (ResultSet result = stmt.executeQuery()) {
                        if (result.next()) {
                            throw new ValidationException("That time slot is already booked. Choose another time.");
                        }
                    }
                }

                if (!date.atTime(time).isAfter(LocalDateTime.now(ZoneId.of("Asia/Colombo")))) {
                    throw new ValidationException("Choose a future appointment time.");
                }

                String sql = "INSERT INTO appointments (pet_id, veterinarian_id, appointment_date, appointment_time, reason, status) VALUES (?, ?, ?, ?, ?, 'Scheduled')";

                long appointmentId;

                try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setLong(1, petId);
                    stmt.setLong(2, vetId);
                    stmt.setDate(3, Date.valueOf(date));
                    stmt.setTime(4, Time.valueOf(time));
                    stmt.setString(5, reason);

                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Appointment was not inserted.");
                    }

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Appointment ID was not returned.");
                        }

                        appointmentId = keys.getLong(1);
                    }
                }

                con.commit();
                return appointmentId;

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
    
    public List<Appointment> getAppointments(LocalDate date,String status) throws SQLException {

        List<Appointment> appointments = new ArrayList<>();

        String sql =
                "SELECT a.appointment_id, a.pet_id, a.veterinarian_id, "
                + "a.appointment_date, a.appointment_time, a.reason, a.status, "
                + "p.pet_name, o.full_name AS owner_name, "
                + "v.full_name AS veterinarian_name "
                + "FROM appointments a "
                + "JOIN pets p ON p.pet_id = a.pet_id "
                + "JOIN owners o ON o.owner_id = p.owner_id "
                + "JOIN veterinarians v ON v.veterinarian_id = a.veterinarian_id "
                + "WHERE a.appointment_date = ?";

        if (status != null) {
            sql += " AND a.status = ?";
        }

        sql += " ORDER BY a.appointment_time, a.appointment_id";

        try (Connection con = DBConnection.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));

            if (status != null) {
                stmt.setString(2, status);
            }

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    appointments.add(new Appointment(
                            result.getLong("appointment_id"),
                            result.getLong("pet_id"),
                            result.getLong("veterinarian_id"),
                            result.getDate("appointment_date").toLocalDate(),
                            result.getTime("appointment_time").toLocalTime(),
                            result.getString("reason"),
                            result.getString("status"),
                            result.getString("pet_name"),
                            result.getString("owner_name"),
                            result.getString("veterinarian_name")
                    ));
                }
            }
        }

        return appointments;
    }
}
