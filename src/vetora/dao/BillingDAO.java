package vetora.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import vetora.config.DBConnection;
import vetora.exception.ValidationException;
import vetora.model.Invoice;
import vetora.model.InvoiceItem;

/**
 * @author ASUS
 */
public class BillingDAO {
    public List<Invoice> getVisits(Long appointmentId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            con.setAutoCommit(false);
            try {
                List<Invoice> visits = readVisits(con, appointmentId);
                con.commit();
                return visits;
            }
            catch (SQLException ex) {
                rollback(con, ex);
                throw ex;
            }
        }
    }

    private List<Invoice> readVisits(Connection con, Long appointmentId) throws SQLException {
        String sql = "SELECT a.appointment_id, a.appointment_date, a.appointment_time, "
                + "COALESCE(i.owner_name, o.full_name) AS owner_name, "
                + "COALESCE(i.pet_name, p.pet_name) AS pet_name, "
                + "i.invoice_id, i.version_no, i.status AS invoice_status, i.total_amount, i.created_at, "
                + "pay.payment_id, pay.amount AS paid_amount, pay.payment_method, pay.paid_at "
                + "FROM appointments a JOIN consultations c ON c.appointment_id = a.appointment_id "
                + "JOIN pets p ON p.pet_id = a.pet_id JOIN owners o ON o.owner_id = p.owner_id "
                + "LEFT JOIN invoices i ON i.appointment_id = a.appointment_id "
                + "LEFT JOIN payments pay ON pay.invoice_id = i.invoice_id "
                + "WHERE c.status = 'Finalized' AND a.status = 'Completed'";
        if (appointmentId != null) {
            sql += " AND a.appointment_id = ?";
        }
        sql += " ORDER BY a.appointment_date DESC, a.appointment_time, a.appointment_id";
        List<Invoice> visits = new ArrayList<>();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            if (appointmentId != null) {
                stmt.setLong(1, appointmentId);
            }
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    long id = result.getLong("invoice_id");
                    Long invoiceId = result.wasNull() ? null : id;
                    String status = invoiceId == null ? "Unbilled" : result.getString("invoice_status");
                    BigDecimal total = invoiceId == null ? new BigDecimal("0.00") : result.getBigDecimal("total_amount");
                    Timestamp createdAt = result.getTimestamp("created_at");
                    Timestamp paidAt = result.getTimestamp("paid_at");
                    long paymentId = result.getLong("payment_id");
                    BigDecimal paidAmount = result.getBigDecimal("paid_amount");
                    if (("Paid".equals(status) && (paymentId <= 0 || paidAt == null
                            || paidAmount == null || total == null || paidAmount.compareTo(total) != 0))
                            || (!"Paid".equals(status) && paymentId > 0)) {
                        throw new SQLException("Invoice and payment records are inconsistent.");
                    }
                    visits.add(new Invoice(result.getLong("appointment_id"),
                            result.getDate("appointment_date").toLocalDate(),
                            result.getTime("appointment_time").toLocalTime(),
                            result.getString("owner_name"), result.getString("pet_name"),
                            invoiceId, result.getInt("version_no"), status, total,
                            createdAt == null ? null : createdAt.toLocalDateTime(),
                            result.getString("payment_method"),
                            paidAt == null ? null : paidAt.toLocalDateTime(), new ArrayList<>()));
                }
            }
        }
        //  the dropdown needs summaries.
        if (appointmentId != null && !visits.isEmpty()) {
            Invoice invoice = visits.get(0);
            List<InvoiceItem> items = readItems(con, invoice.getInvoiceId());
            BigDecimal total = new BigDecimal("0.00");
            for (InvoiceItem item : items) {
                total = total.add(item.getAmount());
            }
            if (invoice.getTotal() == null || total.compareTo(invoice.getTotal()) != 0) {
                throw new SQLException("Invoice total does not match its items.");
            }
            visits.set(0, new Invoice(invoice.getAppointmentId(), invoice.getAppointmentDate(),
                    invoice.getAppointmentTime(), invoice.getOwnerName(), invoice.getPetName(),
                    invoice.getInvoiceId(), invoice.getVersion(), invoice.getStatus(), total,
                    invoice.getCreatedAt(), invoice.getPaymentMethod(), invoice.getPaidAt(), items));
        }
        return visits;
    }

    private List<InvoiceItem> readItems(Connection con, Long invoiceId) throws SQLException {
        List<InvoiceItem> items = new ArrayList<>();
        if (invoiceId == null) {
            return items;
        }
        String sql = "SELECT service_name, quantity, unit_price FROM invoice_items "
                + "WHERE invoice_id = ? ORDER BY invoice_item_id";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setLong(1, invoiceId);
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    items.add(new InvoiceItem(result.getString("service_name"),
                            result.getInt("quantity"), result.getBigDecimal("unit_price")));
                }
            }
        }
        return items;
    }

    public Invoice saveInvoice(Invoice loaded, List<InvoiceItem> items,
            BigDecimal total, boolean paid, String paymentMethod)
            throws SQLException, ValidationException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Same lock order as Consultations: appointment, consultation, then invoice.
                checkStatus(con, "SELECT status FROM appointments WHERE appointment_id = ? FOR UPDATE",
                        loaded.getAppointmentId(), "Completed");
                checkStatus(con, "SELECT status FROM consultations WHERE appointment_id = ? FOR UPDATE",
                        loaded.getAppointmentId(), "Finalized");
                Long invoiceId = null;
                int version = 0;
                try (PreparedStatement stmt = con.prepareStatement(
                        "SELECT invoice_id, status, version_no FROM invoices WHERE appointment_id = ? FOR UPDATE")) {
                    stmt.setLong(1, loaded.getAppointmentId());
                    try (ResultSet result = stmt.executeQuery()) {
                        if (result.next()) {
                            invoiceId = result.getLong("invoice_id");
                            version = result.getInt("version_no");
                            if (!"Draft".equals(result.getString("status"))) {
                                throw new ValidationException("Payment is already recorded. Reload the bill to view its receipt.");
                            }
                        }
                    }
                }
                if (invoiceId == null && loaded.getInvoiceId() != null
                        || invoiceId != null && (!invoiceId.equals(loaded.getInvoiceId())
                        || version != loaded.getVersion())) {
                    throw new ValidationException("This invoice was changed elsewhere. Reload the bill before saving.");
                }
                if (invoiceId == null) {
                    String sql = "INSERT INTO invoices (appointment_id, owner_name, pet_name, total_amount) VALUES (?,?,?,?)";
                    try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setLong(1, loaded.getAppointmentId());
                        stmt.setString(2, loaded.getOwnerName());
                        stmt.setString(3, loaded.getPetName());
                        stmt.setBigDecimal(4, total);
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("Invoice was not inserted.");
                        }
                        try (ResultSet keys = stmt.getGeneratedKeys()) {
                            if (!keys.next()) {
                                throw new SQLException("Invoice number was not returned.");
                            }
                            invoiceId = keys.getLong(1);
                        }
                    }
                }
                else {
                    try (PreparedStatement stmt = con.prepareStatement("SELECT payment_id FROM payments WHERE invoice_id = ?")) {
                        stmt.setLong(1, invoiceId);
                        try (ResultSet result = stmt.executeQuery()) {
                            if (result.next()) {
                                throw new ValidationException("A payment already exists. Reload the bill.");
                            }
                        }
                    }
                    String sql = "UPDATE invoices SET total_amount = ?, version_no = version_no + 1 "
                            + "WHERE invoice_id = ? AND status = 'Draft' AND version_no = ?";
                    try (PreparedStatement stmt = con.prepareStatement(sql)) {
                        stmt.setBigDecimal(1, total);
                        stmt.setLong(2, invoiceId);
                        stmt.setInt(3, version);
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("Invoice was not updated.");
                        }
                    }
                }
                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM invoice_items WHERE invoice_id = ?")) {
                    stmt.setLong(1, invoiceId);
                    stmt.executeUpdate();
                }
                String itemSql = "INSERT INTO invoice_items (invoice_id, service_name, quantity, unit_price) VALUES (?,?,?,?)";
                try (PreparedStatement stmt = con.prepareStatement(itemSql)) {
                    for (InvoiceItem item : items) {
                        stmt.setLong(1, invoiceId);
                        stmt.setString(2, item.getServiceName());
                        stmt.setInt(3, item.getQuantity());
                        stmt.setBigDecimal(4, item.getUnitPrice());
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("Invoice item was not inserted.");
                        }
                    }
                }
                if (paid) {
                    try (PreparedStatement stmt = con.prepareStatement(
                            "INSERT INTO payments (invoice_id, amount, payment_method) VALUES (?,?,?)")) {
                        stmt.setLong(1, invoiceId);
                        stmt.setBigDecimal(2, total);
                        stmt.setString(3, paymentMethod);
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("Payment was not inserted.");
                        }
                    }
                    try (PreparedStatement stmt = con.prepareStatement(
                            "UPDATE invoices SET status = 'Paid' WHERE invoice_id = ? AND status = 'Draft'")) {
                        stmt.setLong(1, invoiceId);
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("Invoice was not marked paid.");
                        }
                    }
                }
                List<Invoice> saved = readVisits(con, loaded.getAppointmentId());
                if (saved.size() != 1) {
                    throw new SQLException("Saved invoice could not be read.");
                }
                con.commit();
                return saved.get(0);
            }
            catch (SQLException | ValidationException ex) {
                rollback(con, ex);
                throw ex;
            }
        }
    }

    private void checkStatus(Connection con, String sql, long appointmentId, String status)
            throws SQLException, ValidationException {
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setLong(1, appointmentId);
            try (ResultSet result = stmt.executeQuery()) {
                if (!result.next() || !status.equals(result.getString("status"))) {
                    throw new ValidationException("Only a finalized, completed consultation can be billed. Reload Billing.");
                }
            }
        }
    }

    private void rollback(Connection con, Exception error) {
        try {
            con.rollback();
        }
        catch (SQLException rollbackError) {
            error.addSuppressed(rollbackError);
        }
    }
}
