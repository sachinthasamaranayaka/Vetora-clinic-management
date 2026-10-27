package vetora.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author ASUS
 */
public class Invoice {
    private final long appointmentId;
    private final LocalDate appointmentDate;
    private final LocalTime appointmentTime;
    private final String ownerName;
    private final String petName;
    private final Long invoiceId;
    private final int version;
    private final String status;
    private final BigDecimal total;
    private final LocalDateTime createdAt;
    private final String paymentMethod;
    private final LocalDateTime paidAt;
    private final List<InvoiceItem> items;

    public Invoice(long appointmentId, LocalDate appointmentDate, LocalTime appointmentTime, String ownerName, String petName, Long invoiceId, int version, String status, BigDecimal total, LocalDateTime createdAt, String paymentMethod, LocalDateTime paidAt, List<InvoiceItem> items) {
        this.appointmentId = appointmentId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.ownerName = ownerName;
        this.petName = petName;
        this.invoiceId = invoiceId;
        this.version = version;
        this.status = status;
        this.total = total;
        this.createdAt = createdAt;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
        this.items = items;
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

    public String getOwnerName() {
        return ownerName;
    }

    public String getPetName() {
        return petName;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public int getVersion() {
        return version;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public boolean isPaid() {
        return "Paid".equals(status); 
    }

    public String getInvoiceNumber() {
        return invoiceId == null ? "Not saved" : String.format(Locale.ENGLISH, "INV-%06d", invoiceId);
    }
}
