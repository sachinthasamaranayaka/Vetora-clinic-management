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
import java.util.Objects;
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

                String clashSql = "SELECT appointment_id FROM appointments WHERE veterinarian_id = ? AND appointment_date = ? AND status NOT IN ('Cancelled', 'No-Show') AND appointment_time > ? AND appointment_time < ? LIMIT 1 FOR UPDATE";

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
    
    public List<Appointment> getAppointments(LocalDate date, String status) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return getAppointments(con, date, status);
        }
    }

    public List<Appointment> getAppointments(Connection con, LocalDate date, String status) throws SQLException {

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

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
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
    
    public void updateStatus(long appointmentId, String newStatus) throws SQLException, ValidationException {
        boolean checkIn = "Checked-In".equals(newStatus);
        boolean noShow = "No-Show".equals(newStatus);
        if (!checkIn && !noShow && !"Cancelled".equals(newStatus)) {
            throw new ValidationException("Invalid appointment action.");
        }
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Colombo"));
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ? AND status = 'Scheduled'";
        if (checkIn) {
            sql += " AND appointment_date = ?";
        }
        else if (noShow) {
            sql += " AND (appointment_date < ? OR (appointment_date = ? AND appointment_time <= ?))";
        }
        try (Connection con = DBConnection.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setLong(2, appointmentId);
            if (checkIn) {
                stmt.setDate(3, Date.valueOf(now.toLocalDate()));
            }
            else if (noShow) {
                stmt.setDate(3, Date.valueOf(now.toLocalDate()));
                stmt.setDate(4, Date.valueOf(now.toLocalDate()));
                stmt.setTime(5, Time.valueOf(now.toLocalTime()));
            }
            if (stmt.executeUpdate() != 1) {
                throw new ValidationException(checkIn ? "Only today's scheduled appointments can be checked in."
                        : noShow ? "Only scheduled appointments whose start time has passed can be marked no-show."
                        : "Only scheduled appointments can be cancelled.");
            }
        }
    }

    public void rescheduleAppointment(Appointment original, LocalDate date, LocalTime time, String reason)
            throws SQLException, ValidationException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Use the same veterinarian -> pet -> appointment lock order as booking.
                String vetSql = "SELECT status, availability FROM veterinarians WHERE veterinarian_id = ? FOR UPDATE";
                try (PreparedStatement stmt = con.prepareStatement(vetSql)) {
                    stmt.setLong(1, original.getVeterinarianId());
                    try (ResultSet result = stmt.executeQuery()) {
                        if (!result.next() || !"Active".equals(result.getString("status"))
                                || !"Available".equals(result.getString("availability"))) {
                            throw new ValidationException("The clinic veterinarian is unavailable.");
                        }
                    }
                }
                try (PreparedStatement stmt = con.prepareStatement("SELECT pet_id FROM pets WHERE pet_id = ? FOR UPDATE")) {
                    stmt.setLong(1, original.getPetId());
                    try (ResultSet result = stmt.executeQuery()) {
                        if (!result.next()) {
                            throw new ValidationException("This pet no longer exists. Refresh the schedule.");
                        }
                    }
                }
                String currentSql = "SELECT pet_id, veterinarian_id, appointment_date, appointment_time, reason, status "
                        + "FROM appointments WHERE appointment_id = ? FOR UPDATE";
                try (PreparedStatement stmt = con.prepareStatement(currentSql)) {
                    stmt.setLong(1, original.getAppointmentId());
                    try (ResultSet result = stmt.executeQuery()) {
                        if (!result.next() || !"Scheduled".equals(result.getString("status"))
                                || result.getLong("pet_id") != original.getPetId()
                                || result.getLong("veterinarian_id") != original.getVeterinarianId()
                                || !result.getDate("appointment_date").toLocalDate().equals(original.getAppointmentDate())
                                || !result.getTime("appointment_time").toLocalTime().equals(original.getAppointmentTime())
                                || !Objects.equals(result.getString("reason"), original.getReason())) {
                            throw new ValidationException("This appointment changed. Clear the form and refresh the schedule.");
                        }
                    }
                }
                String clashSql = "SELECT appointment_id FROM appointments WHERE veterinarian_id = ? "
                        + "AND appointment_date = ? AND status NOT IN ('Cancelled', 'No-Show') "
                        + "AND appointment_time > ? AND appointment_time < ? AND appointment_id <> ? LIMIT 1 FOR UPDATE";
                try (PreparedStatement stmt = con.prepareStatement(clashSql)) {
                    stmt.setLong(1, original.getVeterinarianId());
                    stmt.setDate(2, Date.valueOf(date));
                    stmt.setTime(3, Time.valueOf(time.minusMinutes(30)));
                    stmt.setTime(4, Time.valueOf(time.plusMinutes(30)));
                    stmt.setLong(5, original.getAppointmentId());
                    try (ResultSet result = stmt.executeQuery()) {
                        if (result.next()) {
                            throw new ValidationException("That time slot is already booked. Choose another time.");
                        }
                    }
                }
                if (!date.atTime(time).isAfter(LocalDateTime.now(ZoneId.of("Asia/Colombo")))) {
                    throw new ValidationException("Choose a future appointment time.");
                }
                String sql = "UPDATE appointments SET appointment_date = ?, appointment_time = ?, reason = ? "
                        + "WHERE appointment_id = ? AND status = 'Scheduled'";
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setDate(1, Date.valueOf(date));
                    stmt.setTime(2, Time.valueOf(time));
                    stmt.setString(3, reason);
                    stmt.setLong(4, original.getAppointmentId());
                    stmt.executeUpdate();
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
}
