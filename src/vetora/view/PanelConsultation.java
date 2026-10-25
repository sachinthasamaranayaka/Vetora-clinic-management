/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package vetora.view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.table.DefaultTableModel;
import vetora.controller.ConsultationController;
import vetora.model.Consultation;
import vetora.model.PrescriptionItem;

/**
 *
 * @author ASUS
 */
public class PanelConsultation extends javax.swing.JPanel {

    private ConsultationController controller;
    private final List<Consultation> consultationVisits = new ArrayList<>();
    private final List<PrescriptionItem> prescriptionItems = new ArrayList<>();
    private Consultation loadedVisit;
    private boolean consultationBusy;
    private boolean consultationSplitReady = false;

    public PanelConsultation() {
        initComponents();
        setupConsultationUI();
        clearLoadedVisit();
        setConsultationBusy(false);
    }

    private void setupConsultationUI() {
        tblPrescriptionItems.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        scrClinicalNotes.getViewport().setBackground(Color.WHITE);
        scrClinicalNotes.getVerticalScrollBar().setUnitIncrement(21);
        scrPrescription.getViewport().setBackground(Color.WHITE);
        scrPrescription.getVerticalScrollBar().setUnitIncrement(21);
        scrPrescriptionItems.getViewport().setBackground(Color.WHITE);
        scrPrescriptionItems.getVerticalScrollBar().setUnitIncrement(21);
        spnPrescribedQuantity.setEditor(new JSpinner.NumberEditor(spnPrescribedQuantity, "0"));
        tblPrescriptionItems.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updatePrescriptionButtons();
            }
        });
    }

    public void setController(ConsultationController controller) {
        this.controller = controller;
    }

    public Long getLoadedConsultationAppointmentId() {
        return loadedVisit == null ? null : loadedVisit.getAppointmentId();
    }

    public String getConsultationSymptoms() {
        return txtSymptoms.getText();
    }

    public String getConsultationDiagnosis() {
        return txtDiagnosis.getText();
    }

    public String getConsultationTreatment() {
        return txtTreatmentNotes.getText();
    }

    public void showConsultationMessage(String message) {
        lblConsultationError.setForeground(new Color(185, 28, 28));
        lblConsultationError.setText(message);
        lblConsultationError.setToolTipText(message.trim().isEmpty() ? null : message);
    }

    public Long getSelectedConsultationAppointmentId() {
        int index = cmbConsultationAppointment.getSelectedIndex() - 1;
        return index >= 0 && index < consultationVisits.size()
                ? consultationVisits.get(index).getAppointmentId() : null;
    }

    public boolean isConsultationEditable() {
        return !consultationBusy && loadedVisit != null
                && (loadedVisit.getStatus() == null || "Draft".equals(loadedVisit.getStatus()));
    }

    public void setConsultationBusy(boolean busy) {
        consultationBusy = busy;
        cmbConsultationAppointment.setEnabled(!busy && !consultationVisits.isEmpty());
        btnLoadConsultation.setEnabled(!busy && getSelectedConsultationAppointmentId() != null);
        btnLoadConsultation.setText(busy ? "Please wait..." : "Load visit");
        boolean editable = isConsultationEditable();
        txtSymptoms.setEditable(editable);
        txtDiagnosis.setEditable(editable);
        txtTreatmentNotes.setEditable(editable);
        txtExternalMedicine.setEnabled(editable);
        spnPrescribedQuantity.setEnabled(editable);
        txtDoseText.setEnabled(editable);
        txtFrequency.setEnabled(editable);
        txtDuration.setEnabled(editable);
        txtPrescriptionInstructions.setEnabled(editable);
        tblPrescriptionItems.setEnabled(!busy && loadedVisit != null);
        btnSaveDraft.setEnabled(editable);
        btnFinalizeConsultation.setEnabled(editable);
        updatePrescriptionButtons();
    }

    private void updatePrescriptionButtons() {
        boolean editable = isConsultationEditable();
        btnAddPrescriptionItem.setEnabled(editable);
        btnClearPrescriptionItem.setEnabled(editable);
        btnRemovePrescriptionItem.setEnabled(editable && tblPrescriptionItems.getSelectedRow() >= 0);
    }

    private String formatVisit(Consultation visit) {
        String status = visit.getStatus() == null ? "New" : visit.getStatus();
        return "[" + status + "] " + visit.getAppointmentDate() + " " + visit.getAppointmentTime()
                + " | #" + visit.getAppointmentId() + " | " + visit.getPetName()
                + " (" + visit.getOwnerName() + ")";
    }

    public void displayVisitOptions(List<Consultation> visits) {
        consultationVisits.clear();
        consultationVisits.addAll(visits);
        cmbConsultationAppointment.removeAllItems();
        cmbConsultationAppointment.addItem("Select appointment");
        for (Consultation visit : visits) {
            cmbConsultationAppointment.addItem(formatVisit(visit));
        }
        cmbConsultationAppointment.setSelectedIndex(0);
    }

    public void clearLoadedVisit() {
        loadedVisit = null;
        lblPatientPet.setText("Pet: —");
        lblPatientOwner.setText("Owner: —");
        lblPatientSpecies.setText("Species: —");
        lblPatientAge.setText("Age: —");
        lblConsultationVet.setText("Veterinarian: —");
        lblConsultationVisit.setText("Visit: —");
        lblConsultationStatus.setText("Consultation: Not loaded");
        txtSymptoms.setText("");
        txtDiagnosis.setText("");
        txtTreatmentNotes.setText("");
        prescriptionItems.clear();
        displayPrescriptionItems();
        clearPrescriptionEntry();
    }

    public void displayVisit(Consultation visit) {
        loadedVisit = visit;
        lblPatientPet.setText("Pet: " + visit.getPetName());
        lblPatientOwner.setText("Owner: " + visit.getOwnerName());
        lblPatientSpecies.setText("Species: " + visit.getSpecies());
        lblPatientAge.setText("Age: " + visit.getAgeText());
        lblConsultationVet.setText("Veterinarian: " + visit.getVeterinarianName());
        lblConsultationVisit.setText("Visit: " + visit.getAppointmentDate() + " " + visit.getAppointmentTime());
        lblConsultationStatus.setText("Consultation: " + (visit.getStatus() == null ? "New" : visit.getStatus()));
        txtSymptoms.setText(visit.getSymptoms());
        txtDiagnosis.setText(visit.getDiagnosis());
        txtTreatmentNotes.setText(visit.getTreatment());
        prescriptionItems.clear();
        prescriptionItems.addAll(visit.getPrescriptionItems());
        displayPrescriptionItems();
        clearPrescriptionEntry();
        txtSymptoms.setCaretPosition(0);
        txtDiagnosis.setCaretPosition(0);
        txtTreatmentNotes.setCaretPosition(0);
    }

    private String cleanConsultationText(String text) {
        return text == null ? "" : text.trim();
    }

    public boolean hasUnsavedConsultationChanges() {
        return loadedVisit != null && (
                !cleanConsultationText(txtSymptoms.getText()).equals(cleanConsultationText(loadedVisit.getSymptoms()))
                || !cleanConsultationText(txtDiagnosis.getText()).equals(cleanConsultationText(loadedVisit.getDiagnosis()))
                || !cleanConsultationText(txtTreatmentNotes.getText()).equals(cleanConsultationText(loadedVisit.getTreatment()))
                || !samePrescriptionItems(loadedVisit.getPrescriptionItems())
                || hasPendingPrescriptionInput());
    }

    private boolean samePrescriptionItems(List<PrescriptionItem> savedItems) {
        if (savedItems.size() != prescriptionItems.size()) {
            return false;
        }
        for (int i = 0; i < savedItems.size(); i++) {
            if (!savedItems.get(i).sameDetails(prescriptionItems.get(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean confirmDiscardConsultationChanges() {
        return JOptionPane.showConfirmDialog(this,
                "Discard unsaved clinical notes and prescription changes?", "Unsaved changes",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public boolean confirmFinalizeConsultation() {
        return JOptionPane.showConfirmDialog(this,
                "Finalize this consultation and complete the appointment?\n"
                + "Clinical notes and prescription items will become read-only.",
                "Finalize consultation", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void restoreLoadedAppointmentSelection() {
        if (loadedVisit == null) {
            return;
        }
        for (int i = 0; i < consultationVisits.size(); i++) {
            if (consultationVisits.get(i).getAppointmentId() == loadedVisit.getAppointmentId()) {
                cmbConsultationAppointment.setSelectedIndex(i + 1);
                return;
            }
        }
    }

    public void markConsultationSaved(long appointmentId, String symptoms, String diagnosis,
            String treatment, List<PrescriptionItem> items, boolean finalized) {
        if (loadedVisit == null || loadedVisit.getAppointmentId() != appointmentId) {
            return;
        }
        Consultation saved = loadedVisit.withSavedDetails(cleanConsultationText(symptoms),
                cleanConsultationText(diagnosis), cleanConsultationText(treatment), items, finalized);
        displayVisit(saved);
        for (int i = 0; i < consultationVisits.size(); i++) {
            if (consultationVisits.get(i).getAppointmentId() == appointmentId) {
                consultationVisits.set(i, saved);
                cmbConsultationAppointment.removeItemAt(i + 1);
                cmbConsultationAppointment.insertItemAt(formatVisit(saved), i + 1);
                cmbConsultationAppointment.setSelectedIndex(i + 1);
                break;
            }
        }
        lblConsultationError.setForeground(new Color(15, 118, 110));
        String message = finalized ? "Consultation finalized. Appointment completed." : "Draft and prescription saved.";
        lblConsultationError.setText(message);
        lblConsultationError.setToolTipText(message);
    }

    public String getPrescriptionMedicine() {
        return txtExternalMedicine.getText();
    }

    public int getPrescriptionQuantity() throws ParseException {
        String text = getQuantityEditorText();
        if (!text.matches("[0-9]{1,4}")) {
            throw new ParseException("Invalid quantity.", 0);
        }
        spnPrescribedQuantity.commitEdit();
        return Integer.parseInt(text);
    }

    private String getQuantityEditorText() {
        return ((JSpinner.DefaultEditor) spnPrescribedQuantity.getEditor())
                .getTextField().getText().trim();
    }

    public String getPrescriptionDose() {
        return txtDoseText.getText();
    }

    public String getPrescriptionFrequency() {
        return txtFrequency.getText();
    }

    public String getPrescriptionDuration() {
        return txtDuration.getText();
    }

    public String getPrescriptionInstructions() {
        return txtPrescriptionInstructions.getText();
    }

    public List<PrescriptionItem> getPrescriptionItems() {
        return new ArrayList<>(prescriptionItems);
    }

    public boolean hasPendingPrescriptionInput() {
        return !txtExternalMedicine.getText().trim().isEmpty()
                || !txtDoseText.getText().trim().isEmpty()
                || !txtFrequency.getText().trim().isEmpty()
                || !txtDuration.getText().trim().isEmpty()
                || !txtPrescriptionInstructions.getText().trim().isEmpty()
                || !"1".equals(getQuantityEditorText());
    }

    public void clearPrescriptionEntry() {
        txtExternalMedicine.setText("");
        spnPrescribedQuantity.setValue(1);
        ((JSpinner.DefaultEditor) spnPrescribedQuantity.getEditor()).getTextField().setValue(1);
        txtDoseText.setText("");
        txtFrequency.setText("");
        txtDuration.setText("");
        txtPrescriptionInstructions.setText("");
    }

    public void addPrescriptionItem(PrescriptionItem item) {
        prescriptionItems.add(item);
        displayPrescriptionItems();
        clearPrescriptionEntry();
        txtExternalMedicine.requestFocusInWindow();
    }

    public void removeSelectedPrescriptionItem() {
        int selected = tblPrescriptionItems.getSelectedRow();
        if (selected >= 0) {
            int index = tblPrescriptionItems.convertRowIndexToModel(selected);
            prescriptionItems.remove(index);
            displayPrescriptionItems();
        }
    }

    private void displayPrescriptionItems() {
        tblPrescriptionItems.clearSelection();
        DefaultTableModel tableModel = (DefaultTableModel) tblPrescriptionItems.getModel();
        tableModel.setRowCount(0);
        for (PrescriptionItem item : prescriptionItems) {
            tableModel.addRow(new Object[]{item.getMedicineName(), item.getFulfilment(),
                item.getQuantity(), item.getDoseText(), item.getFrequency(),
                item.getDuration(), item.getInstructions()});
        }
        lblPrescriptionCount.setText(prescriptionItems.size() + " prescription item(s)");
        updatePrescriptionButtons();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlConsultationHeader = new javax.swing.JPanel();
        pnlConsultationHeaderLables = new javax.swing.JPanel();
        lblSelectedConsultationAppointment = new javax.swing.JLabel();
        lblConsultationStatus = new javax.swing.JLabel();
        pnlConsultationSeclector = new javax.swing.JPanel();
        cmbConsultationAppointment = new javax.swing.JComboBox<>();
        btnLoadConsultation = new javax.swing.JButton();
        pnlConsultationPatientSummary = new javax.swing.JPanel();
        lblPatientPet = new javax.swing.JLabel();
        lblPatientOwner = new javax.swing.JLabel();
        lblPatientSpecies = new javax.swing.JLabel();
        lblPatientAge = new javax.swing.JLabel();
        lblConsultationVet = new javax.swing.JLabel();
        lblConsultationVisit = new javax.swing.JLabel();
        tabConsultation = new javax.swing.JTabbedPane();
        pnlCurrentConsultation = new javax.swing.JPanel();
        splitConsultation = new javax.swing.JSplitPane();
        scrClinicalNotes = new javax.swing.JScrollPane();
        pnlClinicalNotes = new javax.swing.JPanel();
        lblClinicalNotesTitle = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21));
        lblSymptoms = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        scrSymptomsText = new javax.swing.JScrollPane();
        txtSymptoms = new javax.swing.JTextArea();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21));
        lblDiagnosis = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        scrDiagnosisText = new javax.swing.JScrollPane();
        txtDiagnosis = new javax.swing.JTextArea();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21));
        lblTreatmentNotes = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        scrTreatmentNotesText = new javax.swing.JScrollPane();
        txtTreatmentNotes = new javax.swing.JTextArea();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13));
        lblClinicalNotesHint = new javax.swing.JLabel();
        scrPrescription = new javax.swing.JScrollPane();
        pnlPrescription = new javax.swing.JPanel();
        lblPrescriptionTitle = new javax.swing.JLabel();
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21));
        lblFulfilment = new javax.swing.JLabel();
        filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        cmbFulfilment = new javax.swing.JComboBox<>();
        filler10 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21));
        pnlPrescriptionMedicineRow = new javax.swing.JPanel();
        pnlExternalMedicineGroup = new javax.swing.JPanel();
        lblExtterrnalMedicine = new javax.swing.JLabel();
        txtExternalMedicine = new javax.swing.JTextField();
        pnlPrescriptionQuantityGroup = new javax.swing.JPanel();
        lblPrescribedQuantity = new javax.swing.JLabel();
        spnPrescribedQuantity = new javax.swing.JSpinner();
        filler11 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21));
        pnlPrescriptionDoseRow = new javax.swing.JPanel();
        pnlPrescriptionDoseGroup = new javax.swing.JPanel();
        lblDoseText = new javax.swing.JLabel();
        txtDoseText = new javax.swing.JTextField();
        pnlPrescriptionFrequencyGroup = new javax.swing.JPanel();
        lblFrequency = new javax.swing.JLabel();
        txtFrequency = new javax.swing.JTextField();
        pnlPrescriptionDurationGroup = new javax.swing.JPanel();
        lblDuration = new javax.swing.JLabel();
        txtDuration = new javax.swing.JTextField();
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21));
        lblPrescriptionInsructions = new javax.swing.JLabel();
        filler13 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        scrPrescriptionInstructions = new javax.swing.JScrollPane();
        txtPrescriptionInstructions = new javax.swing.JTextArea();
        filler14 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21));
        pnlPrescriptionItemButtons = new javax.swing.JPanel();
        btnAddPrescriptionItem = new javax.swing.JButton();
        btnRemovePrescriptionItem = new javax.swing.JButton();
        btnClearPrescriptionItem = new javax.swing.JButton();
        filler15 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13));
        scrPrescriptionItems = new javax.swing.JScrollPane();
        tblPrescriptionItems = new javax.swing.JTable();
        filler16 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        lblPrescriptionCount = new javax.swing.JLabel();
        pnlConsultationActions = new javax.swing.JPanel();
        lblConsultationError = new javax.swing.JLabel();
        pnlConsultationButtons = new javax.swing.JPanel();
        btnSaveDraft = new javax.swing.JButton();
        filler17 = new javax.swing.Box.Filler(new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0));
        btnFinalizeConsultation = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 247, 250));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        setLayout(new java.awt.BorderLayout(0, 21));

        pnlConsultationHeader.setBackground(new java.awt.Color(255, 255, 255));
        pnlConsultationHeader.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlConsultationHeader.setLayout(new java.awt.BorderLayout(0, 8));

        pnlConsultationHeaderLables.setBackground(new java.awt.Color(255, 255, 255));
        pnlConsultationHeaderLables.setPreferredSize(new java.awt.Dimension(0, 21));
        pnlConsultationHeaderLables.setLayout(new java.awt.BorderLayout(13, 0));

        lblSelectedConsultationAppointment.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSelectedConsultationAppointment.setText("Consultation appointment");
        pnlConsultationHeaderLables.add(lblSelectedConsultationAppointment, java.awt.BorderLayout.CENTER);

        lblConsultationStatus.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblConsultationStatus.setText("Consultation: Not loaded");
        pnlConsultationHeaderLables.add(lblConsultationStatus, java.awt.BorderLayout.EAST);

        pnlConsultationHeader.add(pnlConsultationHeaderLables, java.awt.BorderLayout.NORTH);

        pnlConsultationSeclector.setBackground(new java.awt.Color(255, 255, 255));
        pnlConsultationSeclector.setPreferredSize(new java.awt.Dimension(0, 55));
        pnlConsultationSeclector.setLayout(new java.awt.BorderLayout(13, 0));

        cmbConsultationAppointment.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        cmbConsultationAppointment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select appointment" }));
        cmbConsultationAppointment.setPreferredSize(new java.awt.Dimension(233, 55));
        cmbConsultationAppointment.addActionListener(this::cmbConsultationAppointmentActionPerformed);
        pnlConsultationSeclector.add(cmbConsultationAppointment, java.awt.BorderLayout.CENTER);

        btnLoadConsultation.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnLoadConsultation.setText("Load visit");
        btnLoadConsultation.setPreferredSize(new java.awt.Dimension(144, 55));
        btnLoadConsultation.addActionListener(this::btnLoadConsultationActionPerformed);
        pnlConsultationSeclector.add(btnLoadConsultation, java.awt.BorderLayout.EAST);

        pnlConsultationHeader.add(pnlConsultationSeclector, java.awt.BorderLayout.CENTER);

        pnlConsultationPatientSummary.setBackground(new java.awt.Color(255, 255, 255));
        pnlConsultationPatientSummary.setPreferredSize(new java.awt.Dimension(0, 50));
        pnlConsultationPatientSummary.setLayout(new java.awt.GridLayout(2, 3, 21, 8));

        lblPatientPet.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPatientPet.setText("Pet: —");
        pnlConsultationPatientSummary.add(lblPatientPet);

        lblPatientOwner.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPatientOwner.setText("Owner: —");
        pnlConsultationPatientSummary.add(lblPatientOwner);

        lblPatientSpecies.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPatientSpecies.setText("Species: —");
        pnlConsultationPatientSummary.add(lblPatientSpecies);

        lblPatientAge.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPatientAge.setText("Age: —");
        pnlConsultationPatientSummary.add(lblPatientAge);

        lblConsultationVet.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblConsultationVet.setText("Veterinarian: —");
        pnlConsultationPatientSummary.add(lblConsultationVet);

        lblConsultationVisit.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblConsultationVisit.setText("Visit: —");
        pnlConsultationPatientSummary.add(lblConsultationVisit);

        pnlConsultationHeader.add(pnlConsultationPatientSummary, java.awt.BorderLayout.SOUTH);

        add(pnlConsultationHeader, java.awt.BorderLayout.NORTH);

        tabConsultation.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        pnlCurrentConsultation.setBackground(new java.awt.Color(245, 247, 250));
        pnlCurrentConsultation.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlCurrentConsultation.setLayout(new java.awt.BorderLayout(0, 13));

        splitConsultation.setDividerSize(7);
        splitConsultation.setResizeWeight(0.381);

        scrClinicalNotes.setBorder(null);
        scrClinicalNotes.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrClinicalNotes.setMinimumSize(new java.awt.Dimension(300, 0));

        pnlClinicalNotes.setBackground(new java.awt.Color(255, 255, 255));
        pnlClinicalNotes.setBorder(javax.swing.BorderFactory.createEmptyBorder(21, 21, 21, 21));
        pnlClinicalNotes.setLayout(new javax.swing.BoxLayout(pnlClinicalNotes, javax.swing.BoxLayout.Y_AXIS));

        lblClinicalNotesTitle.setFont(new java.awt.Font("Segoe UI", 1, 21)); // NOI18N
        lblClinicalNotesTitle.setText("Clinical notes");
        pnlClinicalNotes.add(lblClinicalNotesTitle);
        pnlClinicalNotes.add(filler1);

        lblSymptoms.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblSymptoms.setText("Symptoms *");
        pnlClinicalNotes.add(lblSymptoms);
        pnlClinicalNotes.add(filler2);

        scrSymptomsText.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scrSymptomsText.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrSymptomsText.setAlignmentX(0.0F);
        scrSymptomsText.setMaximumSize(new java.awt.Dimension(32767, 89));
        scrSymptomsText.setMinimumSize(new java.awt.Dimension(0, 89));
        scrSymptomsText.setPreferredSize(new java.awt.Dimension(233, 89));

        txtSymptoms.setEditable(false);
        txtSymptoms.setColumns(20);
        txtSymptoms.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtSymptoms.setLineWrap(true);
        txtSymptoms.setWrapStyleWord(true);
        txtSymptoms.setMargin(new java.awt.Insets(8, 8, 8, 8));
        scrSymptomsText.setViewportView(txtSymptoms);

        pnlClinicalNotes.add(scrSymptomsText);
        pnlClinicalNotes.add(filler3);

        lblDiagnosis.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblDiagnosis.setText("Diagnosis *");
        pnlClinicalNotes.add(lblDiagnosis);
        pnlClinicalNotes.add(filler4);

        scrDiagnosisText.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scrDiagnosisText.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrDiagnosisText.setAlignmentX(0.0F);
        scrDiagnosisText.setMaximumSize(new java.awt.Dimension(32767, 89));
        scrDiagnosisText.setMinimumSize(new java.awt.Dimension(0, 89));
        scrDiagnosisText.setPreferredSize(new java.awt.Dimension(233, 89));

        txtDiagnosis.setEditable(false);
        txtDiagnosis.setColumns(20);
        txtDiagnosis.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtDiagnosis.setLineWrap(true);
        txtDiagnosis.setWrapStyleWord(true);
        txtDiagnosis.setMargin(new java.awt.Insets(8, 8, 8, 8));
        scrDiagnosisText.setViewportView(txtDiagnosis);

        pnlClinicalNotes.add(scrDiagnosisText);
        pnlClinicalNotes.add(filler5);

        lblTreatmentNotes.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblTreatmentNotes.setText("Treatment notes");
        pnlClinicalNotes.add(lblTreatmentNotes);
        pnlClinicalNotes.add(filler6);

        scrTreatmentNotesText.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scrTreatmentNotesText.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrTreatmentNotesText.setAlignmentX(0.0F);
        scrTreatmentNotesText.setMaximumSize(new java.awt.Dimension(32767, 89));
        scrTreatmentNotesText.setMinimumSize(new java.awt.Dimension(0, 89));
        scrTreatmentNotesText.setPreferredSize(new java.awt.Dimension(233, 89));

        txtTreatmentNotes.setEditable(false);
        txtTreatmentNotes.setColumns(20);
        txtTreatmentNotes.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtTreatmentNotes.setLineWrap(true);
        txtTreatmentNotes.setWrapStyleWord(true);
        txtTreatmentNotes.setMargin(new java.awt.Insets(8, 8, 8, 8));
        scrTreatmentNotesText.setViewportView(txtTreatmentNotes);

        pnlClinicalNotes.add(scrTreatmentNotesText);
        pnlClinicalNotes.add(filler7);

        lblClinicalNotesHint.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblClinicalNotesHint.setForeground(new java.awt.Color(71, 85, 105));
        lblClinicalNotesHint.setText("* Required when finalizing");
        pnlClinicalNotes.add(lblClinicalNotesHint);

        scrClinicalNotes.setViewportView(pnlClinicalNotes);

        splitConsultation.setLeftComponent(scrClinicalNotes);

        scrPrescription.setBorder(null);
        scrPrescription.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrPrescription.setMinimumSize(new java.awt.Dimension(430, 0));

        pnlPrescription.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescription.setBorder(javax.swing.BorderFactory.createEmptyBorder(21, 21, 21, 21));
        pnlPrescription.setLayout(new javax.swing.BoxLayout(pnlPrescription, javax.swing.BoxLayout.Y_AXIS));

        lblPrescriptionTitle.setFont(new java.awt.Font("Segoe UI", 1, 21)); // NOI18N
        lblPrescriptionTitle.setText("Prescription");
        pnlPrescription.add(lblPrescriptionTitle);
        pnlPrescription.add(filler8);

        lblFulfilment.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblFulfilment.setText("Supply from");
        pnlPrescription.add(lblFulfilment);
        pnlPrescription.add(filler9);

        cmbFulfilment.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        cmbFulfilment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "External pharmacy" }));
        cmbFulfilment.setAlignmentX(0.0F);
        cmbFulfilment.setEnabled(false);
        cmbFulfilment.setMaximumSize(new java.awt.Dimension(32767, 55));
        cmbFulfilment.setMinimumSize(new java.awt.Dimension(0, 55));
        cmbFulfilment.setPreferredSize(new java.awt.Dimension(233, 55));
        pnlPrescription.add(cmbFulfilment);
        pnlPrescription.add(filler10);

        pnlPrescriptionMedicineRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionMedicineRow.setAlignmentX(0.0F);
        pnlPrescriptionMedicineRow.setMaximumSize(new java.awt.Dimension(32767, 84));
        pnlPrescriptionMedicineRow.setMinimumSize(new java.awt.Dimension(0, 84));
        pnlPrescriptionMedicineRow.setPreferredSize(new java.awt.Dimension(233, 84));
        pnlPrescriptionMedicineRow.setLayout(new java.awt.BorderLayout(13, 0));

        pnlExternalMedicineGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlExternalMedicineGroup.setLayout(new java.awt.BorderLayout(0, 8));

        lblExtterrnalMedicine.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblExtterrnalMedicine.setText("Medicine *");
        lblExtterrnalMedicine.setPreferredSize(new java.awt.Dimension(0, 21));
        pnlExternalMedicineGroup.add(lblExtterrnalMedicine, java.awt.BorderLayout.NORTH);

        txtExternalMedicine.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtExternalMedicine.setEnabled(false);
        pnlExternalMedicineGroup.add(txtExternalMedicine, java.awt.BorderLayout.CENTER);

        pnlPrescriptionMedicineRow.add(pnlExternalMedicineGroup, java.awt.BorderLayout.CENTER);

        pnlPrescriptionQuantityGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionQuantityGroup.setPreferredSize(new java.awt.Dimension(89, 84));
        pnlPrescriptionQuantityGroup.setLayout(new java.awt.BorderLayout(0, 8));

        lblPrescribedQuantity.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblPrescribedQuantity.setText("Quantity *");
        lblPrescribedQuantity.setPreferredSize(new java.awt.Dimension(0, 21));
        pnlPrescriptionQuantityGroup.add(lblPrescribedQuantity, java.awt.BorderLayout.NORTH);

        spnPrescribedQuantity.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        spnPrescribedQuantity.setModel(new javax.swing.SpinnerNumberModel(1, 1, 9999, 1));
        spnPrescribedQuantity.setEnabled(false);
        pnlPrescriptionQuantityGroup.add(spnPrescribedQuantity, java.awt.BorderLayout.CENTER);

        pnlPrescriptionMedicineRow.add(pnlPrescriptionQuantityGroup, java.awt.BorderLayout.EAST);

        pnlPrescription.add(pnlPrescriptionMedicineRow);
        pnlPrescription.add(filler11);

        pnlPrescriptionDoseRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionDoseRow.setAlignmentX(0.0F);
        pnlPrescriptionDoseRow.setMaximumSize(new java.awt.Dimension(32767, 84));
        pnlPrescriptionDoseRow.setMinimumSize(new java.awt.Dimension(0, 84));
        pnlPrescriptionDoseRow.setPreferredSize(new java.awt.Dimension(233, 84));
        pnlPrescriptionDoseRow.setLayout(new java.awt.GridLayout(1, 3, 13, 0));

        pnlPrescriptionDoseGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionDoseGroup.setLayout(new java.awt.BorderLayout(0, 8));

        lblDoseText.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblDoseText.setText("Dose *");
        lblDoseText.setToolTipText("Required when finalizing a prescription item.");
        lblDoseText.setPreferredSize(new java.awt.Dimension(0, 21));
        pnlPrescriptionDoseGroup.add(lblDoseText, java.awt.BorderLayout.NORTH);

        txtDoseText.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtDoseText.setEnabled(false);
        pnlPrescriptionDoseGroup.add(txtDoseText, java.awt.BorderLayout.CENTER);

        pnlPrescriptionDoseRow.add(pnlPrescriptionDoseGroup);

        pnlPrescriptionFrequencyGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionFrequencyGroup.setLayout(new java.awt.BorderLayout(0, 8));

        lblFrequency.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblFrequency.setText("Frequency *");
        lblFrequency.setToolTipText("Required when finalizing a prescription item.");
        lblFrequency.setPreferredSize(new java.awt.Dimension(0, 21));
        pnlPrescriptionFrequencyGroup.add(lblFrequency, java.awt.BorderLayout.NORTH);

        txtFrequency.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtFrequency.setEnabled(false);
        pnlPrescriptionFrequencyGroup.add(txtFrequency, java.awt.BorderLayout.CENTER);

        pnlPrescriptionDoseRow.add(pnlPrescriptionFrequencyGroup);

        pnlPrescriptionDurationGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionDurationGroup.setLayout(new java.awt.BorderLayout(0, 8));

        lblDuration.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblDuration.setText("Duration *");
        lblDuration.setToolTipText("Required when finalizing a prescription item.");
        lblDuration.setPreferredSize(new java.awt.Dimension(0, 21));
        pnlPrescriptionDurationGroup.add(lblDuration, java.awt.BorderLayout.NORTH);

        txtDuration.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtDuration.setEnabled(false);
        pnlPrescriptionDurationGroup.add(txtDuration, java.awt.BorderLayout.CENTER);

        pnlPrescriptionDoseRow.add(pnlPrescriptionDurationGroup);

        pnlPrescription.add(pnlPrescriptionDoseRow);
        pnlPrescription.add(filler12);

        lblPrescriptionInsructions.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblPrescriptionInsructions.setText("Instructions (optional)");
        pnlPrescription.add(lblPrescriptionInsructions);
        pnlPrescription.add(filler13);

        scrPrescriptionInstructions.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scrPrescriptionInstructions.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrPrescriptionInstructions.setAlignmentX(0.0F);
        scrPrescriptionInstructions.setMaximumSize(new java.awt.Dimension(32767, 89));
        scrPrescriptionInstructions.setMinimumSize(new java.awt.Dimension(0, 89));
        scrPrescriptionInstructions.setPreferredSize(new java.awt.Dimension(233, 89));

        txtPrescriptionInstructions.setColumns(20);
        txtPrescriptionInstructions.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtPrescriptionInstructions.setLineWrap(true);
        txtPrescriptionInstructions.setWrapStyleWord(true);
        txtPrescriptionInstructions.setEnabled(false);
        txtPrescriptionInstructions.setMargin(new java.awt.Insets(8, 8, 8, 8));
        scrPrescriptionInstructions.setViewportView(txtPrescriptionInstructions);

        pnlPrescription.add(scrPrescriptionInstructions);
        pnlPrescription.add(filler14);

        pnlPrescriptionItemButtons.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionItemButtons.setAlignmentX(0.0F);
        pnlPrescriptionItemButtons.setMaximumSize(new java.awt.Dimension(32767, 55));
        pnlPrescriptionItemButtons.setMinimumSize(new java.awt.Dimension(0, 55));
        pnlPrescriptionItemButtons.setPreferredSize(new java.awt.Dimension(233, 55));
        pnlPrescriptionItemButtons.setLayout(new java.awt.GridLayout(1, 3, 13, 0));

        btnAddPrescriptionItem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnAddPrescriptionItem.setForeground(new java.awt.Color(15, 118, 110));
        btnAddPrescriptionItem.setText("Add item");
        btnAddPrescriptionItem.setEnabled(false);
        btnAddPrescriptionItem.setPreferredSize(new java.awt.Dimension(0, 55));
        btnAddPrescriptionItem.addActionListener(this::btnAddPrescriptionItemActionPerformed);
        pnlPrescriptionItemButtons.add(btnAddPrescriptionItem);

        btnRemovePrescriptionItem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnRemovePrescriptionItem.setForeground(new java.awt.Color(185, 28, 28));
        btnRemovePrescriptionItem.setText("Remove");
        btnRemovePrescriptionItem.setEnabled(false);
        btnRemovePrescriptionItem.setPreferredSize(new java.awt.Dimension(0, 55));
        btnRemovePrescriptionItem.addActionListener(this::btnRemovePrescriptionItemActionPerformed);
        pnlPrescriptionItemButtons.add(btnRemovePrescriptionItem);

        btnClearPrescriptionItem.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnClearPrescriptionItem.setForeground(new java.awt.Color(71, 85, 105));
        btnClearPrescriptionItem.setText("Clear fields");
        btnClearPrescriptionItem.setEnabled(false);
        btnClearPrescriptionItem.setPreferredSize(new java.awt.Dimension(0, 55));
        btnClearPrescriptionItem.addActionListener(this::btnClearPrescriptionItemActionPerformed);
        pnlPrescriptionItemButtons.add(btnClearPrescriptionItem);

        pnlPrescription.add(pnlPrescriptionItemButtons);
        pnlPrescription.add(filler15);

        scrPrescriptionItems.setAlignmentX(0.0F);
        scrPrescriptionItems.setMaximumSize(new java.awt.Dimension(32767, 233));
        scrPrescriptionItems.setMinimumSize(new java.awt.Dimension(0, 233));
        scrPrescriptionItems.setPreferredSize(new java.awt.Dimension(233, 233));

        tblPrescriptionItems.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tblPrescriptionItems.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Medicine", "Supply from", "Quantity", "Dose", "Frequency", "Duration", "Instructions"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPrescriptionItems.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tblPrescriptionItems.setColumnSelectionAllowed(false);
        tblPrescriptionItems.setRowSelectionAllowed(true);
        tblPrescriptionItems.setFillsViewportHeight(true);
        tblPrescriptionItems.setGridColor(new java.awt.Color(226, 232, 240));
        tblPrescriptionItems.setRowHeight(55);
        tblPrescriptionItems.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblPrescriptionItems.setShowHorizontalLines(true);
        scrPrescriptionItems.setViewportView(tblPrescriptionItems);
        tblPrescriptionItems.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tblPrescriptionItems.getColumnModel().getColumnCount() > 0) {
            tblPrescriptionItems.getColumnModel().getColumn(0).setPreferredWidth(233);
            tblPrescriptionItems.getColumnModel().getColumn(1).setPreferredWidth(144);
            tblPrescriptionItems.getColumnModel().getColumn(2).setPreferredWidth(89);
            tblPrescriptionItems.getColumnModel().getColumn(3).setPreferredWidth(144);
            tblPrescriptionItems.getColumnModel().getColumn(4).setPreferredWidth(144);
            tblPrescriptionItems.getColumnModel().getColumn(5).setPreferredWidth(89);
            tblPrescriptionItems.getColumnModel().getColumn(6).setPreferredWidth(233);
        }

        pnlPrescription.add(scrPrescriptionItems);
        pnlPrescription.add(filler16);

        lblPrescriptionCount.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPrescriptionCount.setForeground(new java.awt.Color(71, 85, 105));
        lblPrescriptionCount.setText("0 prescription items");
        pnlPrescription.add(lblPrescriptionCount);

        scrPrescription.setViewportView(pnlPrescription);

        splitConsultation.setRightComponent(scrPrescription);

        pnlCurrentConsultation.add(splitConsultation, java.awt.BorderLayout.CENTER);

        pnlConsultationActions.setBackground(new java.awt.Color(245, 247, 250));
        pnlConsultationActions.setPreferredSize(new java.awt.Dimension(0, 55));
        pnlConsultationActions.setLayout(new java.awt.BorderLayout(13, 0));

        lblConsultationError.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblConsultationError.setForeground(new java.awt.Color(185, 28, 28));
        lblConsultationError.setText(" ");
        pnlConsultationActions.add(lblConsultationError, java.awt.BorderLayout.CENTER);

        pnlConsultationButtons.setBackground(new java.awt.Color(245, 247, 250));
        pnlConsultationButtons.setLayout(new javax.swing.BoxLayout(pnlConsultationButtons, javax.swing.BoxLayout.X_AXIS));

        btnSaveDraft.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnSaveDraft.setForeground(new java.awt.Color(71, 85, 105));
        btnSaveDraft.setText("Save draft");
        btnSaveDraft.setEnabled(false);
        btnSaveDraft.setMaximumSize(new java.awt.Dimension(144, 55));
        btnSaveDraft.setMinimumSize(new java.awt.Dimension(144, 55));
        btnSaveDraft.setPreferredSize(new java.awt.Dimension(144, 55));
        btnSaveDraft.addActionListener(this::btnSaveDraftActionPerformed);
        pnlConsultationButtons.add(btnSaveDraft);
        pnlConsultationButtons.add(filler17);

        btnFinalizeConsultation.setBackground(new java.awt.Color(15, 118, 110));
        btnFinalizeConsultation.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnFinalizeConsultation.setForeground(new java.awt.Color(255, 255, 255));
        btnFinalizeConsultation.setText("Finalize consultation");
        btnFinalizeConsultation.setEnabled(false);
        btnFinalizeConsultation.setMaximumSize(new java.awt.Dimension(233, 55));
        btnFinalizeConsultation.setMinimumSize(new java.awt.Dimension(233, 55));
        btnFinalizeConsultation.setPreferredSize(new java.awt.Dimension(233, 55));
        btnFinalizeConsultation.addActionListener(this::btnFinalizeConsultationActionPerformed);
        pnlConsultationButtons.add(btnFinalizeConsultation);

        pnlConsultationActions.add(pnlConsultationButtons, java.awt.BorderLayout.EAST);

        pnlCurrentConsultation.add(pnlConsultationActions, java.awt.BorderLayout.SOUTH);

        tabConsultation.addTab("Current consultation", pnlCurrentConsultation);

        add(tabConsultation, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        if(!consultationSplitReady){
            EventQueue.invokeLater(() -> {
                if (splitConsultation.isShowing() && splitConsultation.getWidth() > 0){
                    splitConsultation.setDividerLocation(0.381);
                    consultationSplitReady = true;
                }
            });
        }
    }//GEN-LAST:event_formComponentShown

    private void cmbConsultationAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbConsultationAppointmentActionPerformed
        if (controller != null){
            controller.appointmentChanged();
        }
    }//GEN-LAST:event_cmbConsultationAppointmentActionPerformed

    private void btnLoadConsultationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadConsultationActionPerformed
        if(controller != null){
            controller.loadVisit();
        }
    }//GEN-LAST:event_btnLoadConsultationActionPerformed

    private void btnSaveDraftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveDraftActionPerformed
        if(controller != null){
            controller.saveDraft();
        }
    }//GEN-LAST:event_btnSaveDraftActionPerformed


    private void btnAddPrescriptionItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPrescriptionItemActionPerformed
        if (controller != null) {
            controller.addPrescriptionItem();
        }
    }//GEN-LAST:event_btnAddPrescriptionItemActionPerformed

    private void btnRemovePrescriptionItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemovePrescriptionItemActionPerformed
        if (controller != null) {
            controller.removePrescriptionItem();
        }
    }//GEN-LAST:event_btnRemovePrescriptionItemActionPerformed

    private void btnClearPrescriptionItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearPrescriptionItemActionPerformed
        if (controller != null) {
            controller.clearPrescriptionItem();
        }
    }//GEN-LAST:event_btnClearPrescriptionItemActionPerformed

    private void btnFinalizeConsultationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinalizeConsultationActionPerformed
        if (controller != null) {
            controller.finalizeConsultation();
        }
    }//GEN-LAST:event_btnFinalizeConsultationActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddPrescriptionItem;
    private javax.swing.JButton btnClearPrescriptionItem;
    private javax.swing.JButton btnFinalizeConsultation;
    private javax.swing.JButton btnLoadConsultation;
    private javax.swing.JButton btnRemovePrescriptionItem;
    private javax.swing.JButton btnSaveDraft;
    private javax.swing.JComboBox<String> cmbConsultationAppointment;
    private javax.swing.JComboBox<String> cmbFulfilment;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler10;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler13;
    private javax.swing.Box.Filler filler14;
    private javax.swing.Box.Filler filler15;
    private javax.swing.Box.Filler filler16;
    private javax.swing.Box.Filler filler17;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.Box.Filler filler9;
    private javax.swing.JLabel lblClinicalNotesHint;
    private javax.swing.JLabel lblClinicalNotesTitle;
    private javax.swing.JLabel lblConsultationError;
    private javax.swing.JLabel lblConsultationStatus;
    private javax.swing.JLabel lblConsultationVet;
    private javax.swing.JLabel lblConsultationVisit;
    private javax.swing.JLabel lblDiagnosis;
    private javax.swing.JLabel lblDoseText;
    private javax.swing.JLabel lblDuration;
    private javax.swing.JLabel lblExtterrnalMedicine;
    private javax.swing.JLabel lblFrequency;
    private javax.swing.JLabel lblFulfilment;
    private javax.swing.JLabel lblPatientAge;
    private javax.swing.JLabel lblPatientOwner;
    private javax.swing.JLabel lblPatientPet;
    private javax.swing.JLabel lblPatientSpecies;
    private javax.swing.JLabel lblPrescribedQuantity;
    private javax.swing.JLabel lblPrescriptionCount;
    private javax.swing.JLabel lblPrescriptionInsructions;
    private javax.swing.JLabel lblPrescriptionTitle;
    private javax.swing.JLabel lblSelectedConsultationAppointment;
    private javax.swing.JLabel lblSymptoms;
    private javax.swing.JLabel lblTreatmentNotes;
    private javax.swing.JPanel pnlClinicalNotes;
    private javax.swing.JPanel pnlConsultationActions;
    private javax.swing.JPanel pnlConsultationButtons;
    private javax.swing.JPanel pnlConsultationHeader;
    private javax.swing.JPanel pnlConsultationHeaderLables;
    private javax.swing.JPanel pnlConsultationPatientSummary;
    private javax.swing.JPanel pnlConsultationSeclector;
    private javax.swing.JPanel pnlCurrentConsultation;
    private javax.swing.JPanel pnlExternalMedicineGroup;
    private javax.swing.JPanel pnlPrescription;
    private javax.swing.JPanel pnlPrescriptionDoseGroup;
    private javax.swing.JPanel pnlPrescriptionDoseRow;
    private javax.swing.JPanel pnlPrescriptionDurationGroup;
    private javax.swing.JPanel pnlPrescriptionFrequencyGroup;
    private javax.swing.JPanel pnlPrescriptionItemButtons;
    private javax.swing.JPanel pnlPrescriptionMedicineRow;
    private javax.swing.JPanel pnlPrescriptionQuantityGroup;
    private javax.swing.JScrollPane scrClinicalNotes;
    private javax.swing.JScrollPane scrDiagnosisText;
    private javax.swing.JScrollPane scrPrescription;
    private javax.swing.JScrollPane scrPrescriptionInstructions;
    private javax.swing.JScrollPane scrPrescriptionItems;
    private javax.swing.JScrollPane scrSymptomsText;
    private javax.swing.JScrollPane scrTreatmentNotesText;
    private javax.swing.JSplitPane splitConsultation;
    private javax.swing.JSpinner spnPrescribedQuantity;
    private javax.swing.JTabbedPane tabConsultation;
    private javax.swing.JTable tblPrescriptionItems;
    private javax.swing.JTextArea txtDiagnosis;
    private javax.swing.JTextField txtDoseText;
    private javax.swing.JTextField txtDuration;
    private javax.swing.JTextField txtExternalMedicine;
    private javax.swing.JTextField txtFrequency;
    private javax.swing.JTextArea txtPrescriptionInstructions;
    private javax.swing.JTextArea txtSymptoms;
    private javax.swing.JTextArea txtTreatmentNotes;
    // End of variables declaration//GEN-END:variables
}
