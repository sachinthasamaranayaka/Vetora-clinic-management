package vetora.model;

import java.math.BigDecimal;

/**
 * @author ASUS
 */
public class InvoiceItem {
    private final String serviceName;
    private final int quantity;
    private final BigDecimal unitPrice;

    public InvoiceItem(String serviceName, int quantity, BigDecimal unitPrice) {
        this.serviceName = serviceName == null ? "" : serviceName.trim();
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getAmount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean sameDetails(InvoiceItem other) {
        return other != null && serviceName.equals(other.serviceName) && quantity == other.quantity && unitPrice.compareTo(other.unitPrice) == 0;
    }
}
