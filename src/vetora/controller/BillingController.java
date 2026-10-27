package vetora.controller;

import vetora.util.AccessControl;

import java.awt.print.PrinterException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import vetora.exception.ValidationException;
import vetora.model.BillingModel;
import vetora.model.Invoice;
import vetora.model.InvoiceItem;
import vetora.view.PanelBilling;

/**
 * @author ASUS
 */
public class BillingController {
    private final AccessControl accessControl;
    private final BillingModel model;
    private final PanelBilling view;
    private boolean loading;
    private boolean saving;
    private boolean printing;
    private boolean restoringSelection;

    public BillingController(BillingModel model, PanelBilling view, AccessControl accessControl) {
        if (accessControl == null) {
            throw new IllegalArgumentException("Access control is required.");
        }
        this.accessControl = accessControl;
        this.model = model;
        this.view = view;
    }

    private boolean isBusy() { return loading || saving || printing; }

    public void loadConsultations() {
        if (!accessControl.canManageBilling()) {
            view.showBillingMessage("You do not have permission to perform this action.");
            return;
        } load(null); }

    public void loadInvoice() {
        if (!accessControl.canManageBilling()) {
            view.showBillingMessage("You do not have permission to perform this action.");
            return;
        }
        if (isBusy()) { return; }
        Long id = view.getSelectedBillingAppointmentId();
        if (id == null) {
            view.showBillingMessage("Select a finalized consultation.");
            return;
        }
        load(id);
    }

    private void load(Long appointmentId) {
        if (!accessControl.canManageBilling()) {
            view.showBillingMessage("You do not have permission to perform this action.");
            return;
        }
        if (isBusy()) {
            return; 
        }
        
        if (view.hasUnsavedBillingChanges() && !view.confirmDiscardBillingChanges()) {
            return; 
        }
        
        loading = true;
        view.setBillingBusy(true);
        view.clearLoadedInvoice();
        
        if (appointmentId == null) { 
            view.displayBillingOptions(new ArrayList<>()); 
        }
        
        view.showBillingMessage("Loading...");
        
        new SwingWorker<List<Invoice>, Void>() {
            @Override
            protected List<Invoice> doInBackground() throws Exception {
                return model.getVisits(appointmentId);
            }
            @Override
            protected void done() {
                try {
                    List<Invoice> visits = get();
                    if (appointmentId == null) { view.displayBillingOptions(visits); }
                    else { view.displayInvoice(visits.get(0)); }
                    view.showBillingMessage(visits.isEmpty() ? "No finalized consultations found." : " ");
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showBillingMessage("Loading interrupted.");
                }
                catch (ExecutionException ex) {
                    showFailure(ex, "Could not load billing. Check the connection and billing tables.");
                }
                finally {
                    loading = false;
                    view.setBillingBusy(false);
                }
            }
        }.execute();
    }

    public void addInvoiceItem() {
        if (!accessControl.canManageBilling()) {
            view.showBillingMessage("You do not have permission to perform this action.");
            return;
        }
        if (isBusy() || !view.isInvoiceEditable()) { 
            return; 
        }
        try {
            InvoiceItem item = model.createInvoiceItem(view.getBillingService(),
                    view.getBillingQuantity(), view.getBillingUnitPrice());
            List<InvoiceItem> items = view.getInvoiceItems();
            items.add(item);
            model.validateItems(items);
            view.addInvoiceItem(item);
            view.showBillingMessage(" ");
        }
        catch (ParseException ex) {
            view.showBillingMessage("Quantity must be a whole number from 1 to 9999.");
        }
        catch (ValidationException ex) { view.showBillingMessage(ex.getMessage()); }
    }

    public void removeInvoiceItem() {
        if (!accessControl.canManageBilling()) {
            view.showBillingMessage("You do not have permission to perform this action.");
            return;
        }
        if (!isBusy() && view.isInvoiceEditable()) {
            view.removeSelectedInvoiceItem();
            view.showBillingMessage(" ");
        }
    }

    public void clearInvoiceItem() {
        if (!accessControl.canManageBilling()) {
            view.showBillingMessage("You do not have permission to perform this action.");
            return;
        }
        if (!isBusy() && view.isInvoiceEditable()) {
            view.clearInvoiceEntry();
            view.showBillingMessage(" ");
        }
    }

    public void saveDraft() {
        if (!accessControl.canManageBilling()) {
            view.showBillingMessage("You do not have permission to perform this action.");
            return;
        } save(false); }
    public void recordPayment() {
        if (!accessControl.canManageBilling()) {
            view.showBillingMessage("You do not have permission to perform this action.");
            return;
        } save(true); }

    private void save(boolean paid) {
        if (!accessControl.canManageBilling()) {
            view.showBillingMessage("You do not have permission to perform this action.");
            return;
        }
        if (isBusy()) { return; }
        if (!view.isInvoiceEditable()) {
            view.showBillingMessage("Load an unpaid consultation bill first.");
            return;
        }
        if (view.hasPendingInvoiceInput()) {
            view.showBillingMessage("Click Add item or Clear fields before saving.");
            return;
        }
        Invoice loaded = view.getLoadedInvoice();
        List<InvoiceItem> items = view.getInvoiceItems();
        String method = view.getPaymentMethod();
        try {
            model.validateInvoice(loaded, items, paid, method);
            if (paid && !view.confirmRecordPayment(model.validateItems(items), method)) { 
                return;
            }
        }
        catch (ValidationException ex) {
            view.showBillingMessage(ex.getMessage());
            return;
        }
        saving = true;
        view.setBillingBusy(true);
        view.showBillingMessage(paid ? "Recording payment..." : "Saving invoice draft...");
        new SwingWorker<Invoice, Void>() {
            @Override
            protected Invoice doInBackground() throws Exception {
                return paid ? model.recordPayment(loaded, items, method) : model.saveDraft(loaded, items);
            }
            @Override
            protected void done() {
                try {
                    view.markInvoiceSaved(get());
                    view.showBillingSuccess(paid ? "Payment recorded. Receipt is ready." : "Invoice draft saved.");
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showBillingMessage("Saving interrupted. Reload the bill to check its status.");
                }
                catch (ExecutionException ex) {
                    showFailure(ex, "Could not confirm the save. Reload the bill to check its status.");
                }
                finally {
                    saving = false;
                    view.setBillingBusy(false);
                }
            }
        }.execute();
    }

    public void consultationChanged() {
        if (!accessControl.canManageBilling()) {
            view.showBillingMessage("You do not have permission to perform this action.");
            return;
        }
        if (isBusy() || restoringSelection) {
            return; 
        }
        if (view.hasUnsavedBillingChanges() && !view.confirmDiscardBillingChanges()) {
            restoringSelection = true;
            try { view.restoreLoadedBillingSelection(); }
            finally { restoringSelection = false; }
            return;
        }
        view.clearLoadedInvoice();
        view.showBillingMessage(" ");
        view.setBillingBusy(false);
    }

    public void printReceipt() {
        if (!accessControl.canManageBilling()) {
            view.showBillingMessage("You do not have permission to perform this action.");
            return;
        }
        if (isBusy()) { 
            return;
        }
        try {
            String receipt = model.createReceipt(view.getLoadedInvoice());
            printing = true;
            view.setBillingBusy(true);
            boolean printed = view.printBillingReceipt(receipt);
            view.showBillingSuccess(printed ? "Receipt sent to the printer." : "Printing cancelled. Payment is unchanged.");
        }
        catch (ValidationException ex) { view.showBillingMessage(ex.getMessage()); }
        catch (PrinterException | SecurityException ex) {
            ex.printStackTrace();
            view.showBillingMessage("Could not print. Payment is saved; you can print the receipt again.");
        }
        finally {
            printing = false;
            view.setBillingBusy(false);
        }
    }

    private void showFailure(ExecutionException ex, String message) {
        Throwable cause = ex.getCause();
        if (cause instanceof ValidationException) { view.showBillingMessage(cause.getMessage()); }
        else {
            cause.printStackTrace();
            view.showBillingMessage(message);
        }
    }
}
