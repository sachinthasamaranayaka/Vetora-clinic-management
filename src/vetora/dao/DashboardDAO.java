/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import vetora.config.DBConnection;
import vetora.model.Appointment;
import vetora.model.Dashboard;

/**
 *
 * @author ASUS
 */
public class DashboardDAO {
    public Dashboard getDashboard(LocalDate date) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            con.setAutoCommit(false);
            try {
                List<Appointment> appointments = new AppointmentDAO().getAppointments(con, date, null);
                BigDecimal income = BigDecimal.ZERO;
                String sql = "SELECT COALESCE(SUM(amount), 0) AS income FROM payments WHERE paid_at >= ? AND paid_at < ?";
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
                    stmt.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
                    try (ResultSet result = stmt.executeQuery()) {
                        if (result.next()) {
                            income = result.getBigDecimal("income");
                        }
                    }
                }
                con.commit();
                return new Dashboard(date, appointments, income);
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
    }
}
