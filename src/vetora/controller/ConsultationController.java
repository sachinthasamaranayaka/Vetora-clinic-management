/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vetora.controller;

import java.awt.print.PrinterException;
import vetora.util.AccessControl;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import vetora.exception.ValidationException;
import vetora.model.Consultation;
import vetora.model.ConsultationModel;
import vetora.model.PrescriptionItem;
import vetora.view.PanelConsultation;

/**
 * @author ASUS
 */
public class ConsultationController {
    private final AccessControl accessControl;
    private final ConsultationModel model;
    private final PanelConsultation view;
    private boolean loading;
    private boolean savingConsultation;
    private boolean restoringSelection;
    private boolean printing;

    public ConsultationController(ConsultationModel model, PanelConsultation view, AccessControl accessControl) {
        if (accessControl == null) {
            throw new IllegalArgumentException("Access control is required.");
        }
        this.accessControl = accessControl;
        this.model = model;
        this.view = view;
    }

    public void loadAppointments() {
        if (!accessControl.canEditConsultation()) {
            view.showConsultationMessage("You do not have permission to perform this action.");
            return;
        }
        load(null);
    }

    public void loadVisit() {
        if (!accessControl.canEditConsultation()) {
            view.showConsultationMessage("You do not have permission to perform this action.");
            return;
        }
        if (loading || savingConsultation || printing) {
            return;
        }
        Long id = view.getSelectedConsultationAppointmentId();
        if (id == null) {
            view.showConsultationMessage("Select an appointment.");
            return;
        }
        load(id);
    }

    private void load(Long appointmentId) {
        if (!accessControl.canEditConsultation()) {
            view.showConsultationMessage("You do not have permission to perform this action.");
            return;
        }
        if (loading || savingConsultation || printing) {
            return;
        }
        if (view.hasUnsavedConsultationChanges() && !view.confirmDiscardConsultationChanges()) {
            return;
        }
        loading = true;
        view.setConsultationBusy(true);
        view.clearLoadedVisit();
        if (appointmentId == null) {
            view.displayVisitOptions(List.of());
        }
        view.showConsultationMessage("Loading...");
        new SwingWorker<List<Consultation>, Void>() {
            @Override
            protected List<Consultation> doInBackground() throws Exception {
                return model.getVisits(appointmentId);
            }
            @Override
            protected void done() {
                try {
                    List<Consultation> visits = get();
                    if (appointmentId == null) {
                        view.displayVisitOptions(visits);
                    }
                    else {
                        view.displayVisit(visits.get(0));
                    }
                    view.showConsultationMessage(visits.isEmpty() ? "No consultation visits found." : " ");
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showConsultationMessage("Loading interrupted.");
                }
                catch (ExecutionException ex) {
                    showFailure(ex, "Could not load consultation details.");
                }
                finally {
                    loading = false;
                    view.setConsultationBusy(false);
                }
            }
        }.execute();
    }

    public void addPrescriptionItem() {
        if (!accessControl.canEditConsultation()) {
            view.showConsultationMessage("You do not have permission to perform this action.");
            return;
        }
        if (loading || savingConsultation || !view.isConsultationEditable() || printing) {
            return;
        }
        try {
            PrescriptionItem item = model.createPrescriptionItem(
                    view.getPrescriptionMedicine(), view.getPrescriptionQuantity(),
                    view.getPrescriptionDose(), view.getPrescriptionFrequency(),
                    view.getPrescriptionDuration(), view.getPrescriptionInstructions());
            List<PrescriptionItem> items = view.getPrescriptionItems();
            if (items.size() >= 100) {
                throw new ValidationException("Use at most 100 prescription items.");
            }
            for (PrescriptionItem existing : items) {
                if (item.sameDetails(existing)) {
                    throw new ValidationException("This prescription item is already in the list.");
                }
            }
            view.addPrescriptionItem(item);
            view.showConsultationMessage(" ");
        }
        catch (ParseException ex) {
            view.showConsultationMessage("Quantity must be a whole number from 1 to 9999.");
        }
        catch (ValidationException ex) {
            view.showConsultationMessage(ex.getMessage());
        }
    }

    public void removePrescriptionItem() {
        if (!accessControl.canEditConsultation()) {
            view.showConsultationMessage("You do not have permission to perform this action.");
            return;
        }
        if (loading || savingConsultation || printing || restoringSelection) {
            return;
        }
        if (!view.isConsultationEditable()){
            return;
        }
        view.removeSelectedPrescriptionItem();
        view.showConsultationMessage(" ");
    }

    public void clearPrescriptionItem() {
        if (!accessControl.canEditConsultation()) {
            view.showConsultationMessage("You do not have permission to perform this action.");
            return;
        }
        if (loading || savingConsultation || printing || restoringSelection) {
            return;
        }
        if (!view.isConsultationEditable()) {
            return;
        }
        view.clearPrescriptionEntry();
        view.showConsultationMessage(" ");
    }

    public void saveDraft() {
        if (!accessControl.canEditConsultation()) {
            view.showConsultationMessage("You do not have permission to perform this action.");
            return;
        }
        save(false);
    }

    public void finalizeConsultation() {
        if (!accessControl.canEditConsultation()) {
            view.showConsultationMessage("You do not have permission to perform this action.");
            return;
        }
        save(true);
    }

    private void save(boolean finalized) {
        if (!accessControl.canEditConsultation()) {
            view.showConsultationMessage("You do not have permission to perform this action.");
            return;
        }
        if (loading || savingConsultation) {
            return;
        }
        if (!view.isConsultationEditable()) {
            view.showConsultationMessage("Load a checked-in visit before saving.");
            return;
        }
        if (view.hasPendingPrescriptionInput()) {
            view.showConsultationMessage("Click Add item or Clear fields before saving.");
            return;
        }
        Long appointmentId = view.getLoadedConsultationAppointmentId();
        String symptoms = view.getConsultationSymptoms();
        String diagnosis = view.getConsultationDiagnosis();
        String treatment = view.getConsultationTreatment();
        List<PrescriptionItem> items = view.getPrescriptionItems();
        try {
            model.validateConsultation(appointmentId, symptoms, diagnosis, treatment, items, finalized);
        }
        catch (ValidationException ex) {
            view.showConsultationMessage(ex.getMessage());
            return;
        }
        if (finalized && !view.confirmFinalizeConsultation()) {
            return;
        }
        savingConsultation = true;
        view.setConsultationBusy(true);
        view.showConsultationMessage(finalized ? "Finalizing consultation..." : "Saving draft...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (finalized) {
                    model.finalizeConsultation(appointmentId, symptoms, diagnosis, treatment, items);
                }
                else {
                    model.saveDraft(appointmentId, symptoms, diagnosis, treatment, items);
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    view.markConsultationSaved(appointmentId, symptoms, diagnosis, treatment, items, finalized);
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showConsultationMessage("Saving interrupted. Your notes and items are still shown.");
                }
                catch (ExecutionException ex) {
                    showFailure(ex, "Could not confirm the save. Your notes and items are still shown.");
                }
                finally {
                    savingConsultation = false;
                    view.setConsultationBusy(false);
                }
            }
        }.execute();
    }

    public void appointmentChanged() {
        if (!accessControl.canEditConsultation()) {
            view.showConsultationMessage("You do not have permission to perform this action.");
            return;
        }
        if (loading || savingConsultation || restoringSelection) {
            return;
        }
        if (view.hasUnsavedConsultationChanges() && !view.confirmDiscardConsultationChanges()) {
            restoringSelection = true;
            try {
                view.restoreLoadedAppointmentSelection();
            }
            finally {
                restoringSelection = false;
            }
            return;
        }
        view.clearLoadedVisit();
        view.showConsultationMessage(" ");
        view.setConsultationBusy(false);
    }

    private void showFailure(ExecutionException ex, String message) {
        Throwable cause = ex.getCause();
        if (cause instanceof ValidationException) {
            view.showConsultationMessage(cause.getMessage());
        }
        else {
            cause.printStackTrace();
            view.showConsultationMessage(message);
        }
    }
    
    public boolean canPrintPrescription() {
        return accessControl.canEditConsultation();
    }
    
    public void printPrescription() {
        if (!canPrintPrescription()) {
            view.showConsultationMessage("You do not have permission to print prescriptions.");
            return;
        }
        if (loading || savingConsultation || printing) {
            return;
        }
        
        try {
            String txt = model.createPriscription(view.getLoadedConsultation());
            printing = true;
            view.setConsultationBusy(true);
            boolean printed = view.printConsultationPrescription(txt);
            view.showPrescriptionPrinted(printed ? "Prescription sent to the printer." : "Printing cancelled.");
        }
        catch (ValidationException ex) {
            view.showConsultationMessage(ex.getMessage());
        }
        catch (PrinterException | SecurityException ex) {
            ex.printStackTrace();
            view.showConsultationMessage("Could not print. You can try again.");
        }
        finally {
            printing = false;
            view.setConsultationBusy(false);
        }
    }
}
