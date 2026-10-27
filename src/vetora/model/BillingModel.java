package vetora.model;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import vetora.dao.BillingDAO;
import vetora.exception.ValidationException;

/**
 * @author ASUS
 */
public class BillingModel {
    private final BillingDAO dao = new BillingDAO();
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999.99");

    public List<Invoice> getVisits(Long appointmentId) throws SQLException, ValidationException {
        if (appointmentId != null && appointmentId <= 0) {
            throw new ValidationException("Select a finalized consultation.");
        }
        List<Invoice> visits = dao.getVisits(appointmentId);
        if (appointmentId != null && visits.isEmpty()) {
            throw new ValidationException("This consultation is unavailable. Reopen Billing to refresh.");
        }
        return visits;
    }

    public InvoiceItem createInvoiceItem(String serviceName, int quantity, String priceText)
            throws ValidationException {
        String price = priceText == null ? "" : priceText.trim();
        if (!price.matches("[0-9]{1,10}(\\.[0-9]{1,2})?")) {
            throw new ValidationException("Enter a unit price such as 1500.00, using at most two decimal places.");
        }
        InvoiceItem item = new InvoiceItem(serviceName, quantity, new BigDecimal(price).setScale(2));
        validateItem(item);
        return item;
    }

    private void validateItem(InvoiceItem item) throws ValidationException {
        if (item == null || item.getServiceName().isEmpty()
                || "Select service".equals(item.getServiceName())
                || item.getServiceName().length() > 150) {
            throw new ValidationException("Select or enter a service name of 1-150 characters.");
        }
        for (int i = 0; i < item.getServiceName().length(); i++) {
            if (Character.isISOControl(item.getServiceName().charAt(i))) {
                throw new ValidationException("Enter the service name on one line.");
            }
        }
        if (item.getQuantity() < 1 || item.getQuantity() > 9999) {
            throw new ValidationException("Quantity must be a whole number from 1 to 9999.");
        }
        if (item.getUnitPrice() == null || item.getUnitPrice().signum() <= 0
                || item.getUnitPrice().scale() > 2
                || item.getUnitPrice().compareTo(MAX_AMOUNT) > 0
                || item.getAmount().compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException("Use a positive price with at most two decimals; amount exceeds the limit.");
        }
    }

    public BigDecimal validateItems(List<InvoiceItem> items) throws ValidationException {
        if (items == null || items.isEmpty() || items.size() > 100) {
            throw new ValidationException("Add between 1 and 100 invoice items.");
        }
        BigDecimal total = new BigDecimal("0.00");
        for (int i = 0; i < items.size(); i++) {
            InvoiceItem item = items.get(i);
            validateItem(item);
            for (int j = 0; j < i; j++) {
                if (item.getServiceName().equalsIgnoreCase(items.get(j).getServiceName())) {
                    throw new ValidationException("This service is already added. Remove it and add the required quantity.");
                }
            }
            total = total.add(item.getAmount());
        }
        if (total.compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException("Invoice total exceeds LKR 9999999999.99.");
        }
        return total.setScale(2);
    }

    public void validateInvoice(Invoice invoice, List<InvoiceItem> items,
            boolean paid, String paymentMethod) throws ValidationException {
        if (invoice == null || invoice.getAppointmentId() <= 0) {
            throw new ValidationException("Load a finalized consultation first.");
        }
        if (!"Unbilled".equals(invoice.getStatus()) && !"Draft".equals(invoice.getStatus())) {
            throw new ValidationException("This invoice is paid and cannot be changed.");
        }
        validateItems(items);
        if (paid && !"Cash".equals(paymentMethod) && !"Card".equals(paymentMethod)) {
            throw new ValidationException("Select Cash or Card after the full payment has been received.");
        }
    }

    public Invoice saveDraft(Invoice invoice, List<InvoiceItem> items)
            throws SQLException, ValidationException {
        validateInvoice(invoice, items, false, null);
        return dao.saveInvoice(invoice, items, validateItems(items), false, null);
    }

    public Invoice recordPayment(Invoice invoice, List<InvoiceItem> items, String paymentMethod)
            throws SQLException, ValidationException {
        validateInvoice(invoice, items, true, paymentMethod);
        return dao.saveInvoice(invoice, items, validateItems(items), true, paymentMethod);
    }

    public String createReceipt(Invoice invoice) throws ValidationException {
        if (invoice == null || !invoice.isPaid() || invoice.getInvoiceId() == null
                || invoice.getPaidAt() == null) {
            throw new ValidationException("Record payment before printing a receipt.");
        }
        BigDecimal total = validateItems(invoice.getItems());
        if (total.compareTo(invoice.getTotal()) != 0) {
            throw new ValidationException("Invoice amounts do not match. Reload the bill before printing.");
        }
        DateTimeFormatter dates = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        StringBuilder text = new StringBuilder();
        text.append("VETORA\nVeterinary Clinic Management System\n\nPAYMENT RECEIPT\n\n");
        text.append("Invoice: ").append(invoice.getInvoiceNumber()).append('\n');
        text.append("Appointment: ").append(invoice.getAppointmentId()).append('\n');
        text.append("Visit: ").append(invoice.getAppointmentDate()).append(' ')
                .append(invoice.getAppointmentTime()).append('\n');
        text.append("Owner: ").append(invoice.getOwnerName()).append('\n');
        text.append("Pet: ").append(invoice.getPetName()).append("\n\n");
        int number = 1;
        for (InvoiceItem item : invoice.getItems()) {
            text.append(number++).append(". ").append(item.getServiceName()).append('\n');
            text.append("   ").append(item.getQuantity()).append(" x LKR ")
                    .append(item.getUnitPrice().setScale(2).toPlainString())
                    .append(" = LKR ").append(item.getAmount().setScale(2).toPlainString()).append('\n');
        }
        text.append("\nTOTAL PAID: LKR ").append(total.toPlainString()).append('\n');
        text.append("Payment method: ").append(invoice.getPaymentMethod()).append('\n');
        text.append("Paid at: ").append(invoice.getPaidAt().format(dates)).append('\n');
        text.append("Status: Paid\n\nThank you.\n");
        return text.toString();
    }
}
