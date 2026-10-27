/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package vetora.view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.print.PrintException;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
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
        tabConsultation.addChangeListener(e -> EventQueue.invokeLater(this::setupPrescriptionSplit));
        splitConsultation.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                setupPrescriptionSplit();
            }
        });
        clearLoadedVisit();
        setConsultationBusy(false);
    }

    private void setupConsultationUI() {
        tblPrescriptionItems.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblPrescriptionItems.getTableHeader().setReorderingAllowed(false);
        tblPrescriptionItems.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        scrPrescriptionItems.setColumnHeaderView(tblPrescriptionItems.getTableHeader());
        tblPrescriptionItems.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                int row = tblPrescriptionItems.rowAtPoint(evt.getPoint());
                int column = tblPrescriptionItems.columnAtPoint(evt.getPoint());
                Object value = row < 0 || column < 0 ? null : tblPrescriptionItems.getValueAt(row, column);
                tblPrescriptionItems.setToolTipText(value == null ? null : value.toString());
            }
        });
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
        updatePrescriptionButtons();
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
        lblConsultationError.setText("<html>" + message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</html>");
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
        btnPrintPrescription.setEnabled(!consultationBusy && controller != null && controller.canPrintPrescription() && loadedVisit != null && "Finalized".equals(loadedVisit.getStatus()) && !loadedVisit.getPrescriptionItems().isEmpty());
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
        tabConsultation.setSelectedIndex(0);
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
        if (loadedVisit == null || loadedVisit.getAppointmentId() != visit.getAppointmentId()) {
            tabConsultation.setSelectedIndex(0);
        }
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
                "Finalize this consultation and complete the appointment?\nClinical notes and prescription items will become read-only.",
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
        lblConsultationError.setText("<html>" + message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</html>");
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
        tblPrescriptionItems.getSelectionModel().setAnchorSelectionIndex(-1);
        tblPrescriptionItems.getSelectionModel().setLeadSelectionIndex(-1);
        DefaultTableModel tableModel = (DefaultTableModel) tblPrescriptionItems.getModel();
        tableModel.setRowCount(0);
        for (PrescriptionItem item : prescriptionItems) {
            tableModel.addRow(new Object[]{item.getMedicineName(),
                item.getQuantity(), item.getDoseText(), item.getFrequency(),
                item.getDuration(), item.getInstructions()});
        }
        lblPrescriptionCount.setText(prescriptionItems.isEmpty()
                ? "No items added. A prescription is optional."
                : prescriptionItems.size() + " prescription item(s)");
        tabConsultation.setTitleAt(1, "Prescription (" + prescriptionItems.size() + ")");
        updatePrescriptionButtons();
    }

    private void setupPrescriptionSplit() {
        if (!consultationSplitReady && tabConsultation.getSelectedIndex() == 1
                && splitConsultation.getWidth() > 0) {
            int width = splitConsultation.getWidth() - splitConsultation.getDividerSize();
            int editorWidth = Math.max(360, (int)Math.round(width * 0.382));
            splitConsultation.setDividerLocation(Math.min(editorWidth, Math.max(360, width - 400)));
            consultationSplitReady = true;
        }
    }
    
    public Consultation getLoadedConsultation() {
        return loadedVisit;
    }
    
    public boolean printConsultationPrescription(String txt) throws PrinterException {
        JTextArea prescription = new JTextArea(txt);
        prescription.setFont(new Font("Monospaced", Font.PLAIN, 11));
        prescription.setEditable(false);
        prescription.setLineWrap(true);
        prescription.setWrapStyleWord(true);
        
        return prescription.print(new MessageFormat("Vetora - Prescription"), new MessageFormat("Page {0}"));
    }
    
    public void showPrescriptionPrinted(String message) {
        showConsultationMessage(message);
        lblConsultationError.setForeground(new Color(15,118,110));
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
        scrClinicalNotes = new javax.swing.JScrollPane();
        pnlClinicalNotes = new javax.swing.JPanel();
        pnlClinicalNotesHeading = new javax.swing.JPanel();
        lblClinicalNotesTitle = new javax.swing.JLabel();
        lblClinicalNotesHint = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13));
        pnlAssessmentRow = new javax.swing.JPanel();
        pnlSymptomsGroup = new javax.swing.JPanel();
        lblSymptoms = new javax.swing.JLabel();
        scrSymptomsText = new javax.swing.JScrollPane();
        txtSymptoms = new javax.swing.JTextArea();
        pnlDiagnosisGroup = new javax.swing.JPanel();
        lblDiagnosis = new javax.swing.JLabel();
        scrDiagnosisText = new javax.swing.JScrollPane();
        txtDiagnosis = new javax.swing.JTextArea();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13));
        pnlTreatmentGroup = new javax.swing.JPanel();
        lblTreatmentNotes = new javax.swing.JLabel();
        scrTreatmentNotesText = new javax.swing.JScrollPane();
        txtTreatmentNotes = new javax.swing.JTextArea();
        pnlClinicalNotesGlue = new javax.swing.JPanel();
        pnlConsultationPrescription = new javax.swing.JPanel();
        splitConsultation = new javax.swing.JSplitPane();
        pnlPrescriptionEditor = new javax.swing.JPanel();
        pnlPrescriptionHeader = new javax.swing.JPanel();
        lblPrescriptionTitle = new javax.swing.JLabel();
        lblFulfilment = new javax.swing.JLabel();
        scrPrescription = new javax.swing.JScrollPane();
        pnlPrescription = new javax.swing.JPanel();
        pnlPrescriptionMedicineRow = new javax.swing.JPanel();
        pnlExternalMedicineGroup = new javax.swing.JPanel();
        lblExtterrnalMedicine = new javax.swing.JLabel();
        txtExternalMedicine = new javax.swing.JTextField();
        pnlPrescriptionQuantityGroup = new javax.swing.JPanel();
        lblPrescribedQuantity = new javax.swing.JLabel();
        spnPrescribedQuantity = new javax.swing.JSpinner();
        filler11 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
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
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        pnlPrescriptionInstructionsGroup = new javax.swing.JPanel();
        lblPrescriptionInsructions = new javax.swing.JLabel();
        scrPrescriptionInstructions = new javax.swing.JScrollPane();
        txtPrescriptionInstructions = new javax.swing.JTextArea();
        pnlPrescriptionEntryGlue = new javax.swing.JPanel();
        pnlPrescriptionItemButtons = new javax.swing.JPanel();
        btnAddPrescriptionItem = new javax.swing.JButton();
        btnClearPrescriptionItem = new javax.swing.JButton();
        pnlPrescriptionList = new javax.swing.JPanel();
        pnlPrescriptionListHeader = new javax.swing.JPanel();
        lblPrescriptionListTitle = new javax.swing.JLabel();
        lblPrescriptionCount = new javax.swing.JLabel();
        scrPrescriptionItems = new javax.swing.JScrollPane();
        tblPrescriptionItems = new javax.swing.JTable();
        pnlPrescriptionListFooter = new javax.swing.JPanel();
        lblPrescriptionListHint = new javax.swing.JLabel();
        btnRemovePrescriptionItem = new javax.swing.JButton();
        pnlConsultationActions = new javax.swing.JPanel();
        lblConsultationError = new javax.swing.JLabel();
        pnlConsultationButtons = new javax.swing.JPanel();
        btnSaveDraft = new javax.swing.JButton();
        filler17 = new javax.swing.Box.Filler(new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0));
        btnFinalizeConsultation = new javax.swing.JButton();
        filler18 = new javax.swing.Box.Filler(new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0));
        btnPrintPrescription = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 247, 250));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        setLayout(new java.awt.BorderLayout(0, 13));

        pnlConsultationHeader.setBackground(new java.awt.Color(255, 255, 255));
        pnlConsultationHeader.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 13, 8, 13));
        pnlConsultationHeader.setLayout(new java.awt.BorderLayout(0, 5));

        pnlConsultationHeaderLables.setBackground(new java.awt.Color(255, 255, 255));
        pnlConsultationHeaderLables.setPreferredSize(new java.awt.Dimension(0, 21));
        pnlConsultationHeaderLables.setLayout(new java.awt.BorderLayout(13, 0));

        lblSelectedConsultationAppointment.setText("Consultation appointment");
        lblSelectedConsultationAppointment.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSelectedConsultationAppointment.setForeground(new java.awt.Color(71, 85, 105));
        pnlConsultationHeaderLables.add(lblSelectedConsultationAppointment, java.awt.BorderLayout.CENTER);

        lblConsultationStatus.setText("Consultation: Not loaded");
        lblConsultationStatus.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblConsultationStatus.setForeground(new java.awt.Color(71, 85, 105));
        pnlConsultationHeaderLables.add(lblConsultationStatus, java.awt.BorderLayout.EAST);

        pnlConsultationHeader.add(pnlConsultationHeaderLables, java.awt.BorderLayout.NORTH);

        pnlConsultationSeclector.setBackground(new java.awt.Color(255, 255, 255));
        pnlConsultationSeclector.setMinimumSize(new java.awt.Dimension(0, 42));
        pnlConsultationSeclector.setPreferredSize(new java.awt.Dimension(0, 42));
        pnlConsultationSeclector.setMaximumSize(new java.awt.Dimension(32767, 42));
        pnlConsultationSeclector.setAlignmentX(0.0F);
        pnlConsultationSeclector.setLayout(new java.awt.BorderLayout(13, 0));

        cmbConsultationAppointment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select appointment" }));
        cmbConsultationAppointment.setMinimumSize(new java.awt.Dimension(0, 42));
        cmbConsultationAppointment.setPreferredSize(new java.awt.Dimension(233, 42));
        cmbConsultationAppointment.setMaximumSize(new java.awt.Dimension(32767, 42));
        cmbConsultationAppointment.setAlignmentX(0.0F);
        cmbConsultationAppointment.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        cmbConsultationAppointment.addActionListener(this::cmbConsultationAppointmentActionPerformed);
        pnlConsultationSeclector.add(cmbConsultationAppointment, java.awt.BorderLayout.CENTER);

        btnLoadConsultation.setText("Load visit");
        btnLoadConsultation.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnLoadConsultation.setForeground(new java.awt.Color(15, 118, 110));
        btnLoadConsultation.setMinimumSize(new java.awt.Dimension(144, 42));
        btnLoadConsultation.setPreferredSize(new java.awt.Dimension(144, 42));
        btnLoadConsultation.setMaximumSize(new java.awt.Dimension(144, 42));
        btnLoadConsultation.addActionListener(this::btnLoadConsultationActionPerformed);
        pnlConsultationSeclector.add(btnLoadConsultation, java.awt.BorderLayout.EAST);

        pnlConsultationHeader.add(pnlConsultationSeclector, java.awt.BorderLayout.CENTER);

        pnlConsultationPatientSummary.setBackground(new java.awt.Color(255, 255, 255));
        pnlConsultationPatientSummary.setPreferredSize(new java.awt.Dimension(0, 39));
        pnlConsultationPatientSummary.setLayout(new java.awt.GridLayout(2, 3, 13, 5));

        lblPatientPet.setText("Pet: —");
        lblPatientPet.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPatientPet.setForeground(new java.awt.Color(71, 85, 105));
        pnlConsultationPatientSummary.add(lblPatientPet);

        lblPatientOwner.setText("Owner: —");
        lblPatientOwner.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPatientOwner.setForeground(new java.awt.Color(71, 85, 105));
        pnlConsultationPatientSummary.add(lblPatientOwner);

        lblPatientSpecies.setText("Species: —");
        lblPatientSpecies.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPatientSpecies.setForeground(new java.awt.Color(71, 85, 105));
        pnlConsultationPatientSummary.add(lblPatientSpecies);

        lblPatientAge.setText("Age: —");
        lblPatientAge.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPatientAge.setForeground(new java.awt.Color(71, 85, 105));
        pnlConsultationPatientSummary.add(lblPatientAge);

        lblConsultationVet.setText("Veterinarian: —");
        lblConsultationVet.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblConsultationVet.setForeground(new java.awt.Color(71, 85, 105));
        pnlConsultationPatientSummary.add(lblConsultationVet);

        lblConsultationVisit.setText("Visit: —");
        lblConsultationVisit.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblConsultationVisit.setForeground(new java.awt.Color(71, 85, 105));
        pnlConsultationPatientSummary.add(lblConsultationVisit);

        pnlConsultationHeader.add(pnlConsultationPatientSummary, java.awt.BorderLayout.SOUTH);

        add(pnlConsultationHeader, java.awt.BorderLayout.NORTH);

        tabConsultation.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        pnlCurrentConsultation.setBackground(new java.awt.Color(245, 247, 250));
        pnlCurrentConsultation.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlCurrentConsultation.setLayout(new java.awt.BorderLayout(0, 13));

        scrClinicalNotes.setMinimumSize(new java.awt.Dimension(300, 0));
        scrClinicalNotes.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrClinicalNotes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        pnlClinicalNotes.setBackground(new java.awt.Color(255, 255, 255));
        pnlClinicalNotes.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlClinicalNotes.setLayout(new javax.swing.BoxLayout(pnlClinicalNotes, javax.swing.BoxLayout.Y_AXIS));

        pnlClinicalNotesHeading.setBackground(new java.awt.Color(255, 255, 255));
        pnlClinicalNotesHeading.setMinimumSize(new java.awt.Dimension(0, 26));
        pnlClinicalNotesHeading.setPreferredSize(new java.awt.Dimension(233, 26));
        pnlClinicalNotesHeading.setMaximumSize(new java.awt.Dimension(32767, 26));
        pnlClinicalNotesHeading.setAlignmentX(0.0F);
        pnlClinicalNotesHeading.setLayout(new java.awt.BorderLayout(13, 0));

        lblClinicalNotesTitle.setText("Clinical notes");
        lblClinicalNotesTitle.setFont(new java.awt.Font("Segoe UI", 1, 21)); // NOI18N
        lblClinicalNotesTitle.setForeground(new java.awt.Color(15, 23, 42));
        pnlClinicalNotesHeading.add(lblClinicalNotesTitle, java.awt.BorderLayout.CENTER);

        lblClinicalNotesHint.setText("* Required when finalizing");
        lblClinicalNotesHint.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblClinicalNotesHint.setForeground(new java.awt.Color(71, 85, 105));
        pnlClinicalNotesHeading.add(lblClinicalNotesHint, java.awt.BorderLayout.EAST);

        pnlClinicalNotes.add(pnlClinicalNotesHeading);
        pnlClinicalNotes.add(filler1);

        pnlAssessmentRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlAssessmentRow.setMinimumSize(new java.awt.Dimension(0, 115));
        pnlAssessmentRow.setPreferredSize(new java.awt.Dimension(233, 115));
        pnlAssessmentRow.setMaximumSize(new java.awt.Dimension(32767, 115));
        pnlAssessmentRow.setAlignmentX(0.0F);
        pnlAssessmentRow.setLayout(new java.awt.GridLayout(1, 2, 13, 0));

        pnlSymptomsGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlSymptomsGroup.setMinimumSize(new java.awt.Dimension(0, 115));
        pnlSymptomsGroup.setPreferredSize(new java.awt.Dimension(233, 115));
        pnlSymptomsGroup.setMaximumSize(new java.awt.Dimension(32767, 115));
        pnlSymptomsGroup.setAlignmentX(0.0F);
        pnlSymptomsGroup.setLayout(new java.awt.BorderLayout(0, 5));

        lblSymptoms.setText("Symptoms *");
        lblSymptoms.setPreferredSize(new java.awt.Dimension(0, 21));
        lblSymptoms.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSymptoms.setForeground(new java.awt.Color(71, 85, 105));
        pnlSymptomsGroup.add(lblSymptoms, java.awt.BorderLayout.NORTH);

        scrSymptomsText.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scrSymptomsText.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        scrSymptomsText.setMinimumSize(new java.awt.Dimension(0, 89));
        scrSymptomsText.setPreferredSize(new java.awt.Dimension(233, 89));
        scrSymptomsText.setMaximumSize(new java.awt.Dimension(32767, 89));
        scrSymptomsText.setAlignmentX(0.0F);
        scrSymptomsText.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txtSymptoms.setEditable(false);
        txtSymptoms.setRows(3);
        txtSymptoms.setLineWrap(true);
        txtSymptoms.setWrapStyleWord(true);
        txtSymptoms.setMargin(new java.awt.Insets(4, 8, 4, 8));
        txtSymptoms.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        scrSymptomsText.setViewportView(txtSymptoms);

        pnlSymptomsGroup.add(scrSymptomsText, java.awt.BorderLayout.CENTER);

        pnlAssessmentRow.add(pnlSymptomsGroup);

        pnlDiagnosisGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlDiagnosisGroup.setMinimumSize(new java.awt.Dimension(0, 115));
        pnlDiagnosisGroup.setPreferredSize(new java.awt.Dimension(233, 115));
        pnlDiagnosisGroup.setMaximumSize(new java.awt.Dimension(32767, 115));
        pnlDiagnosisGroup.setAlignmentX(0.0F);
        pnlDiagnosisGroup.setLayout(new java.awt.BorderLayout(0, 5));

        lblDiagnosis.setText("Diagnosis *");
        lblDiagnosis.setPreferredSize(new java.awt.Dimension(0, 21));
        lblDiagnosis.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDiagnosis.setForeground(new java.awt.Color(71, 85, 105));
        pnlDiagnosisGroup.add(lblDiagnosis, java.awt.BorderLayout.NORTH);

        scrDiagnosisText.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scrDiagnosisText.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        scrDiagnosisText.setMinimumSize(new java.awt.Dimension(0, 89));
        scrDiagnosisText.setPreferredSize(new java.awt.Dimension(233, 89));
        scrDiagnosisText.setMaximumSize(new java.awt.Dimension(32767, 89));
        scrDiagnosisText.setAlignmentX(0.0F);
        scrDiagnosisText.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txtDiagnosis.setEditable(false);
        txtDiagnosis.setRows(3);
        txtDiagnosis.setLineWrap(true);
        txtDiagnosis.setWrapStyleWord(true);
        txtDiagnosis.setMargin(new java.awt.Insets(4, 8, 4, 8));
        txtDiagnosis.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        scrDiagnosisText.setViewportView(txtDiagnosis);

        pnlDiagnosisGroup.add(scrDiagnosisText, java.awt.BorderLayout.CENTER);

        pnlAssessmentRow.add(pnlDiagnosisGroup);

        pnlClinicalNotes.add(pnlAssessmentRow);
        pnlClinicalNotes.add(filler3);

        pnlTreatmentGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlTreatmentGroup.setMinimumSize(new java.awt.Dimension(0, 115));
        pnlTreatmentGroup.setPreferredSize(new java.awt.Dimension(233, 115));
        pnlTreatmentGroup.setMaximumSize(new java.awt.Dimension(32767, 115));
        pnlTreatmentGroup.setAlignmentX(0.0F);
        pnlTreatmentGroup.setLayout(new java.awt.BorderLayout(0, 5));

        lblTreatmentNotes.setPreferredSize(new java.awt.Dimension(0, 21));
        lblTreatmentNotes.setText("Treatment notes (optional)");
        lblTreatmentNotes.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTreatmentNotes.setForeground(new java.awt.Color(71, 85, 105));
        pnlTreatmentGroup.add(lblTreatmentNotes, java.awt.BorderLayout.NORTH);

        scrTreatmentNotesText.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scrTreatmentNotesText.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        scrTreatmentNotesText.setMinimumSize(new java.awt.Dimension(0, 89));
        scrTreatmentNotesText.setPreferredSize(new java.awt.Dimension(233, 89));
        scrTreatmentNotesText.setMaximumSize(new java.awt.Dimension(32767, 89));
        scrTreatmentNotesText.setAlignmentX(0.0F);
        scrTreatmentNotesText.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txtTreatmentNotes.setEditable(false);
        txtTreatmentNotes.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtTreatmentNotes.setLineWrap(true);
        txtTreatmentNotes.setRows(3);
        txtTreatmentNotes.setWrapStyleWord(true);
        txtTreatmentNotes.setMargin(new java.awt.Insets(4, 8, 4, 8));
        scrTreatmentNotesText.setViewportView(txtTreatmentNotes);

        pnlTreatmentGroup.add(scrTreatmentNotesText, java.awt.BorderLayout.CENTER);

        pnlClinicalNotes.add(pnlTreatmentGroup);

        pnlClinicalNotesGlue.setBackground(new java.awt.Color(255, 255, 255));
        pnlClinicalNotesGlue.setPreferredSize(new java.awt.Dimension(0, 0));
        pnlClinicalNotesGlue.setMinimumSize(new java.awt.Dimension(0, 0));
        pnlClinicalNotesGlue.setAlignmentX(0.0F);
        pnlClinicalNotesGlue.setLayout(new javax.swing.BoxLayout(pnlClinicalNotesGlue, javax.swing.BoxLayout.Y_AXIS));
        pnlClinicalNotes.add(pnlClinicalNotesGlue);

        scrClinicalNotes.setViewportView(pnlClinicalNotes);

        pnlCurrentConsultation.add(scrClinicalNotes, java.awt.BorderLayout.CENTER);

        tabConsultation.addTab("Clinical notes", pnlCurrentConsultation);

        pnlConsultationPrescription.setBackground(new java.awt.Color(255, 255, 255));
        pnlConsultationPrescription.setLayout(new java.awt.BorderLayout(0, 0));

        splitConsultation.setResizeWeight(0.382);
        splitConsultation.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        pnlPrescriptionEditor.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionEditor.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlPrescriptionEditor.setMinimumSize(new java.awt.Dimension(360, 0));
        pnlPrescriptionEditor.setLayout(new java.awt.BorderLayout(0, 8));

        pnlPrescriptionHeader.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionHeader.setLayout(new java.awt.BorderLayout(13, 0));

        lblPrescriptionTitle.setText("Prescription");
        lblPrescriptionTitle.setFont(new java.awt.Font("Segoe UI", 1, 21)); // NOI18N
        lblPrescriptionTitle.setForeground(new java.awt.Color(15, 23, 42));
        pnlPrescriptionHeader.add(lblPrescriptionTitle, java.awt.BorderLayout.CENTER);

        lblFulfilment.setText("External pharmacy");
        lblFulfilment.setToolTipText("Supply: External pharmacy");
        lblFulfilment.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblFulfilment.setForeground(new java.awt.Color(71, 85, 105));
        pnlPrescriptionHeader.add(lblFulfilment, java.awt.BorderLayout.EAST);

        pnlPrescriptionEditor.add(pnlPrescriptionHeader, java.awt.BorderLayout.NORTH);

        scrPrescription.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrPrescription.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrPrescription.setMinimumSize(new java.awt.Dimension(0, 0));

        pnlPrescription.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescription.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlPrescription.setLayout(new javax.swing.BoxLayout(pnlPrescription, javax.swing.BoxLayout.Y_AXIS));

        pnlPrescriptionMedicineRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionMedicineRow.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPrescriptionMedicineRow.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPrescriptionMedicineRow.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPrescriptionMedicineRow.setAlignmentX(0.0F);
        pnlPrescriptionMedicineRow.setLayout(new java.awt.BorderLayout(13, 0));

        pnlExternalMedicineGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlExternalMedicineGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlExternalMedicineGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlExternalMedicineGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlExternalMedicineGroup.setAlignmentX(0.0F);
        pnlExternalMedicineGroup.setLayout(new java.awt.BorderLayout(0, 5));

        lblExtterrnalMedicine.setText("Medicine *");
        lblExtterrnalMedicine.setPreferredSize(new java.awt.Dimension(0, 21));
        lblExtterrnalMedicine.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblExtterrnalMedicine.setForeground(new java.awt.Color(71, 85, 105));
        pnlExternalMedicineGroup.add(lblExtterrnalMedicine, java.awt.BorderLayout.NORTH);

        txtExternalMedicine.setEnabled(false);
        txtExternalMedicine.setMinimumSize(new java.awt.Dimension(0, 42));
        txtExternalMedicine.setPreferredSize(new java.awt.Dimension(233, 42));
        txtExternalMedicine.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtExternalMedicine.setAlignmentX(0.0F);
        txtExternalMedicine.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        pnlExternalMedicineGroup.add(txtExternalMedicine, java.awt.BorderLayout.CENTER);

        pnlPrescriptionMedicineRow.add(pnlExternalMedicineGroup, java.awt.BorderLayout.CENTER);

        pnlPrescriptionQuantityGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionQuantityGroup.setAlignmentX(0.0F);
        pnlPrescriptionQuantityGroup.setMinimumSize(new java.awt.Dimension(89, 68));
        pnlPrescriptionQuantityGroup.setPreferredSize(new java.awt.Dimension(89, 68));
        pnlPrescriptionQuantityGroup.setMaximumSize(new java.awt.Dimension(89, 68));
        pnlPrescriptionQuantityGroup.setLayout(new java.awt.BorderLayout(0, 5));

        lblPrescribedQuantity.setText("Quantity *");
        lblPrescribedQuantity.setPreferredSize(new java.awt.Dimension(0, 21));
        lblPrescribedQuantity.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPrescribedQuantity.setForeground(new java.awt.Color(71, 85, 105));
        pnlPrescriptionQuantityGroup.add(lblPrescribedQuantity, java.awt.BorderLayout.NORTH);

        spnPrescribedQuantity.setModel(new javax.swing.SpinnerNumberModel(1, 1, 9999, 1));
        spnPrescribedQuantity.setEnabled(false);
        spnPrescribedQuantity.setMinimumSize(new java.awt.Dimension(0, 42));
        spnPrescribedQuantity.setPreferredSize(new java.awt.Dimension(233, 42));
        spnPrescribedQuantity.setMaximumSize(new java.awt.Dimension(32767, 42));
        spnPrescribedQuantity.setAlignmentX(0.0F);
        spnPrescribedQuantity.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        pnlPrescriptionQuantityGroup.add(spnPrescribedQuantity, java.awt.BorderLayout.CENTER);

        pnlPrescriptionMedicineRow.add(pnlPrescriptionQuantityGroup, java.awt.BorderLayout.EAST);

        pnlPrescription.add(pnlPrescriptionMedicineRow);
        pnlPrescription.add(filler11);

        pnlPrescriptionDoseRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionDoseRow.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPrescriptionDoseRow.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPrescriptionDoseRow.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPrescriptionDoseRow.setAlignmentX(0.0F);
        pnlPrescriptionDoseRow.setLayout(new java.awt.GridLayout(1, 3, 8, 0));

        pnlPrescriptionDoseGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionDoseGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPrescriptionDoseGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPrescriptionDoseGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPrescriptionDoseGroup.setAlignmentX(0.0F);
        pnlPrescriptionDoseGroup.setLayout(new java.awt.BorderLayout(0, 5));

        lblDoseText.setText("Dose *");
        lblDoseText.setToolTipText("Required when finalizing a prescription item.");
        lblDoseText.setPreferredSize(new java.awt.Dimension(0, 21));
        lblDoseText.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDoseText.setForeground(new java.awt.Color(71, 85, 105));
        pnlPrescriptionDoseGroup.add(lblDoseText, java.awt.BorderLayout.NORTH);

        txtDoseText.setEnabled(false);
        txtDoseText.setMinimumSize(new java.awt.Dimension(0, 42));
        txtDoseText.setPreferredSize(new java.awt.Dimension(233, 42));
        txtDoseText.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtDoseText.setAlignmentX(0.0F);
        txtDoseText.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        pnlPrescriptionDoseGroup.add(txtDoseText, java.awt.BorderLayout.CENTER);

        pnlPrescriptionDoseRow.add(pnlPrescriptionDoseGroup);

        pnlPrescriptionFrequencyGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionFrequencyGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPrescriptionFrequencyGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPrescriptionFrequencyGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPrescriptionFrequencyGroup.setAlignmentX(0.0F);
        pnlPrescriptionFrequencyGroup.setLayout(new java.awt.BorderLayout(0, 5));

        lblFrequency.setText("Frequency *");
        lblFrequency.setToolTipText("Required when finalizing a prescription item.");
        lblFrequency.setPreferredSize(new java.awt.Dimension(0, 21));
        lblFrequency.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblFrequency.setForeground(new java.awt.Color(71, 85, 105));
        pnlPrescriptionFrequencyGroup.add(lblFrequency, java.awt.BorderLayout.NORTH);

        txtFrequency.setEnabled(false);
        txtFrequency.setMinimumSize(new java.awt.Dimension(0, 42));
        txtFrequency.setPreferredSize(new java.awt.Dimension(233, 42));
        txtFrequency.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtFrequency.setAlignmentX(0.0F);
        txtFrequency.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        pnlPrescriptionFrequencyGroup.add(txtFrequency, java.awt.BorderLayout.CENTER);

        pnlPrescriptionDoseRow.add(pnlPrescriptionFrequencyGroup);

        pnlPrescriptionDurationGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionDurationGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPrescriptionDurationGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPrescriptionDurationGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPrescriptionDurationGroup.setAlignmentX(0.0F);
        pnlPrescriptionDurationGroup.setLayout(new java.awt.BorderLayout(0, 5));

        lblDuration.setText("Duration *");
        lblDuration.setToolTipText("Required when finalizing a prescription item.");
        lblDuration.setPreferredSize(new java.awt.Dimension(0, 21));
        lblDuration.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDuration.setForeground(new java.awt.Color(71, 85, 105));
        pnlPrescriptionDurationGroup.add(lblDuration, java.awt.BorderLayout.NORTH);

        txtDuration.setEnabled(false);
        txtDuration.setMinimumSize(new java.awt.Dimension(0, 42));
        txtDuration.setPreferredSize(new java.awt.Dimension(233, 42));
        txtDuration.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtDuration.setAlignmentX(0.0F);
        txtDuration.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        pnlPrescriptionDurationGroup.add(txtDuration, java.awt.BorderLayout.CENTER);

        pnlPrescriptionDoseRow.add(pnlPrescriptionDurationGroup);

        pnlPrescription.add(pnlPrescriptionDoseRow);
        pnlPrescription.add(filler12);

        pnlPrescriptionInstructionsGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionInstructionsGroup.setMinimumSize(new java.awt.Dimension(0, 94));
        pnlPrescriptionInstructionsGroup.setPreferredSize(new java.awt.Dimension(233, 94));
        pnlPrescriptionInstructionsGroup.setMaximumSize(new java.awt.Dimension(32767, 94));
        pnlPrescriptionInstructionsGroup.setAlignmentX(0.0F);
        pnlPrescriptionInstructionsGroup.setLayout(new java.awt.BorderLayout(0, 5));

        lblPrescriptionInsructions.setText("Instructions (optional)");
        lblPrescriptionInsructions.setPreferredSize(new java.awt.Dimension(0, 21));
        lblPrescriptionInsructions.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPrescriptionInsructions.setForeground(new java.awt.Color(71, 85, 105));
        pnlPrescriptionInstructionsGroup.add(lblPrescriptionInsructions, java.awt.BorderLayout.NORTH);

        scrPrescriptionInstructions.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scrPrescriptionInstructions.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        scrPrescriptionInstructions.setMinimumSize(new java.awt.Dimension(0, 68));
        scrPrescriptionInstructions.setPreferredSize(new java.awt.Dimension(233, 68));
        scrPrescriptionInstructions.setMaximumSize(new java.awt.Dimension(32767, 68));
        scrPrescriptionInstructions.setAlignmentX(0.0F);
        scrPrescriptionInstructions.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txtPrescriptionInstructions.setEnabled(false);
        txtPrescriptionInstructions.setRows(2);
        txtPrescriptionInstructions.setLineWrap(true);
        txtPrescriptionInstructions.setWrapStyleWord(true);
        txtPrescriptionInstructions.setMargin(new java.awt.Insets(4, 8, 4, 8));
        txtPrescriptionInstructions.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        scrPrescriptionInstructions.setViewportView(txtPrescriptionInstructions);

        pnlPrescriptionInstructionsGroup.add(scrPrescriptionInstructions, java.awt.BorderLayout.CENTER);

        pnlPrescription.add(pnlPrescriptionInstructionsGroup);

        pnlPrescriptionEntryGlue.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionEntryGlue.setPreferredSize(new java.awt.Dimension(0, 0));
        pnlPrescriptionEntryGlue.setMinimumSize(new java.awt.Dimension(0, 0));
        pnlPrescriptionEntryGlue.setAlignmentX(0.0F);
        pnlPrescriptionEntryGlue.setLayout(new javax.swing.BoxLayout(pnlPrescriptionEntryGlue, javax.swing.BoxLayout.Y_AXIS));
        pnlPrescription.add(pnlPrescriptionEntryGlue);

        scrPrescription.setViewportView(pnlPrescription);

        pnlPrescriptionEditor.add(scrPrescription, java.awt.BorderLayout.CENTER);

        pnlPrescriptionItemButtons.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionItemButtons.setMinimumSize(new java.awt.Dimension(0, 42));
        pnlPrescriptionItemButtons.setPreferredSize(new java.awt.Dimension(233, 42));
        pnlPrescriptionItemButtons.setMaximumSize(new java.awt.Dimension(32767, 42));
        pnlPrescriptionItemButtons.setAlignmentX(0.0F);
        pnlPrescriptionItemButtons.setLayout(new java.awt.GridLayout(1, 2, 8, 0));

        btnAddPrescriptionItem.setText("Add item");
        btnAddPrescriptionItem.setEnabled(false);
        btnAddPrescriptionItem.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddPrescriptionItem.setBackground(new java.awt.Color(15, 118, 110));
        btnAddPrescriptionItem.setForeground(new java.awt.Color(255, 255, 255));
        btnAddPrescriptionItem.setPreferredSize(new java.awt.Dimension(0, 42));
        btnAddPrescriptionItem.addActionListener(this::btnAddPrescriptionItemActionPerformed);
        pnlPrescriptionItemButtons.add(btnAddPrescriptionItem);

        btnClearPrescriptionItem.setText("Clear fields");
        btnClearPrescriptionItem.setEnabled(false);
        btnClearPrescriptionItem.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnClearPrescriptionItem.setForeground(new java.awt.Color(15, 118, 110));
        btnClearPrescriptionItem.setPreferredSize(new java.awt.Dimension(0, 42));
        btnClearPrescriptionItem.addActionListener(this::btnClearPrescriptionItemActionPerformed);
        pnlPrescriptionItemButtons.add(btnClearPrescriptionItem);

        pnlPrescriptionEditor.add(pnlPrescriptionItemButtons, java.awt.BorderLayout.SOUTH);

        splitConsultation.setLeftComponent(pnlPrescriptionEditor);

        pnlPrescriptionList.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionList.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlPrescriptionList.setMinimumSize(new java.awt.Dimension(400, 0));
        pnlPrescriptionList.setLayout(new java.awt.BorderLayout(0, 8));

        pnlPrescriptionListHeader.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionListHeader.setLayout(new java.awt.BorderLayout(0, 5));

        lblPrescriptionListTitle.setText("Prescribed items");
        lblPrescriptionListTitle.setFont(new java.awt.Font("Segoe UI", 1, 21)); // NOI18N
        lblPrescriptionListTitle.setForeground(new java.awt.Color(15, 23, 42));
        pnlPrescriptionListHeader.add(lblPrescriptionListTitle, java.awt.BorderLayout.NORTH);

        lblPrescriptionCount.setText("0 prescription items");
        lblPrescriptionCount.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPrescriptionCount.setForeground(new java.awt.Color(71, 85, 105));
        pnlPrescriptionListHeader.add(lblPrescriptionCount, java.awt.BorderLayout.CENTER);

        pnlPrescriptionList.add(pnlPrescriptionListHeader, java.awt.BorderLayout.NORTH);

        scrPrescriptionItems.setAlignmentX(0.0F);
        scrPrescriptionItems.setMinimumSize(new java.awt.Dimension(0, 89));
        scrPrescriptionItems.setPreferredSize(new java.awt.Dimension(233, 144));

        tblPrescriptionItems.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Medicine", "Qty", "Dose", "Frequency", "Duration", "Instructions"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPrescriptionItems.setFillsViewportHeight(true);
        tblPrescriptionItems.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblPrescriptionItems.getTableHeader().setReorderingAllowed(false);
        tblPrescriptionItems.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblPrescriptionItems.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tblPrescriptionItems.setRowHeight(34);
        tblPrescriptionItems.setForeground(new java.awt.Color(15, 23, 42));
        tblPrescriptionItems.setGridColor(new java.awt.Color(226, 232, 240));
        tblPrescriptionItems.setSelectionBackground(new java.awt.Color(204, 251, 241));
        tblPrescriptionItems.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tblPrescriptionItems.setShowHorizontalLines(true);
        scrPrescriptionItems.setViewportView(tblPrescriptionItems);
        tblPrescriptionItems.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tblPrescriptionItems.getColumnModel().getColumnCount() > 0) {
            tblPrescriptionItems.getColumnModel().getColumn(0).setPreferredWidth(144);
            tblPrescriptionItems.getColumnModel().getColumn(1).setPreferredWidth(55);
            tblPrescriptionItems.getColumnModel().getColumn(2).setPreferredWidth(89);
            tblPrescriptionItems.getColumnModel().getColumn(3).setPreferredWidth(89);
            tblPrescriptionItems.getColumnModel().getColumn(4).setPreferredWidth(89);
            tblPrescriptionItems.getColumnModel().getColumn(5).setPreferredWidth(144);
        }
        tblPrescriptionItems.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        tblPrescriptionItems.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        tblPrescriptionItems.getTableHeader().setReorderingAllowed(false);
        scrPrescriptionItems.setColumnHeaderView(tblPrescriptionItems.getTableHeader());
        scrPrescriptionItems.getViewport().setBackground(java.awt.Color.WHITE);

        pnlPrescriptionList.add(scrPrescriptionItems, java.awt.BorderLayout.CENTER);

        pnlPrescriptionListFooter.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionListFooter.setLayout(new java.awt.BorderLayout(13, 0));

        lblPrescriptionListHint.setText("<html>Select an item to remove it.<br>Hover a cell for its full text.</html>");
        lblPrescriptionListHint.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPrescriptionListHint.setForeground(new java.awt.Color(71, 85, 105));
        pnlPrescriptionListFooter.add(lblPrescriptionListHint, java.awt.BorderLayout.CENTER);

        btnRemovePrescriptionItem.setText("Remove");
        btnRemovePrescriptionItem.setEnabled(false);
        btnRemovePrescriptionItem.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnRemovePrescriptionItem.setForeground(new java.awt.Color(185, 28, 28));
        btnRemovePrescriptionItem.setMinimumSize(new java.awt.Dimension(89, 42));
        btnRemovePrescriptionItem.setPreferredSize(new java.awt.Dimension(89, 42));
        btnRemovePrescriptionItem.setMaximumSize(new java.awt.Dimension(89, 42));
        btnRemovePrescriptionItem.addActionListener(this::btnRemovePrescriptionItemActionPerformed);
        pnlPrescriptionListFooter.add(btnRemovePrescriptionItem, java.awt.BorderLayout.EAST);

        pnlPrescriptionList.add(pnlPrescriptionListFooter, java.awt.BorderLayout.SOUTH);

        splitConsultation.setRightComponent(pnlPrescriptionList);

        pnlConsultationPrescription.add(splitConsultation, java.awt.BorderLayout.CENTER);

        tabConsultation.addTab("Prescription (0)", pnlConsultationPrescription);

        add(tabConsultation, java.awt.BorderLayout.CENTER);

        pnlConsultationActions.setBackground(new java.awt.Color(245, 247, 250));
        pnlConsultationActions.setPreferredSize(new java.awt.Dimension(0, 42));
        pnlConsultationActions.setLayout(new java.awt.BorderLayout(13, 0));

        lblConsultationError.setText(" ");
        lblConsultationError.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblConsultationError.setForeground(new java.awt.Color(185, 28, 28));
        pnlConsultationActions.add(lblConsultationError, java.awt.BorderLayout.CENTER);

        pnlConsultationButtons.setBackground(new java.awt.Color(245, 247, 250));
        pnlConsultationButtons.setLayout(new javax.swing.BoxLayout(pnlConsultationButtons, javax.swing.BoxLayout.X_AXIS));

        btnSaveDraft.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnSaveDraft.setForeground(new java.awt.Color(15, 118, 110));
        btnSaveDraft.setText("Save draft");
        btnSaveDraft.setEnabled(false);
        btnSaveDraft.setMaximumSize(new java.awt.Dimension(144, 42));
        btnSaveDraft.setMinimumSize(new java.awt.Dimension(144, 42));
        btnSaveDraft.setPreferredSize(new java.awt.Dimension(144, 42));
        btnSaveDraft.addActionListener(this::btnSaveDraftActionPerformed);
        pnlConsultationButtons.add(btnSaveDraft);
        pnlConsultationButtons.add(filler17);

        btnFinalizeConsultation.setBackground(new java.awt.Color(15, 118, 110));
        btnFinalizeConsultation.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnFinalizeConsultation.setForeground(new java.awt.Color(255, 255, 255));
        btnFinalizeConsultation.setText("Finalize consultation");
        btnFinalizeConsultation.setEnabled(false);
        btnFinalizeConsultation.setMaximumSize(new java.awt.Dimension(200, 42));
        btnFinalizeConsultation.setMinimumSize(new java.awt.Dimension(200, 42));
        btnFinalizeConsultation.setPreferredSize(new java.awt.Dimension(200, 42));
        btnFinalizeConsultation.addActionListener(this::btnFinalizeConsultationActionPerformed);
        pnlConsultationButtons.add(btnFinalizeConsultation);
        pnlConsultationButtons.add(filler18);

        btnPrintPrescription.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnPrintPrescription.setForeground(new java.awt.Color(15, 118, 110));
        btnPrintPrescription.setText("Print prescription");
        btnPrintPrescription.setToolTipText("Print the finalized prescription");
        btnPrintPrescription.setEnabled(false);
        btnPrintPrescription.setMaximumSize(new java.awt.Dimension(144, 42));
        btnPrintPrescription.setMinimumSize(new java.awt.Dimension(144, 42));
        btnPrintPrescription.setPreferredSize(new java.awt.Dimension(144, 42));
        btnPrintPrescription.addActionListener(this::btnPrintPrescriptionActionPerformed);
        pnlConsultationButtons.add(btnPrintPrescription);

        pnlConsultationActions.add(pnlConsultationButtons, java.awt.BorderLayout.EAST);

        add(pnlConsultationActions, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        EventQueue.invokeLater(this::setupPrescriptionSplit);
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

    private void btnPrintPrescriptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintPrescriptionActionPerformed
        if (controller != null) {
            controller.printPrescription();
        }
    }//GEN-LAST:event_btnPrintPrescriptionActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddPrescriptionItem;
    private javax.swing.JButton btnClearPrescriptionItem;
    private javax.swing.JButton btnFinalizeConsultation;
    private javax.swing.JButton btnLoadConsultation;
    private javax.swing.JButton btnPrintPrescription;
    private javax.swing.JButton btnRemovePrescriptionItem;
    private javax.swing.JButton btnSaveDraft;
    private javax.swing.JComboBox<String> cmbConsultationAppointment;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler17;
    private javax.swing.Box.Filler filler18;
    private javax.swing.Box.Filler filler3;
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
    private javax.swing.JLabel lblPrescriptionListHint;
    private javax.swing.JLabel lblPrescriptionListTitle;
    private javax.swing.JLabel lblPrescriptionTitle;
    private javax.swing.JLabel lblSelectedConsultationAppointment;
    private javax.swing.JLabel lblSymptoms;
    private javax.swing.JLabel lblTreatmentNotes;
    private javax.swing.JPanel pnlAssessmentRow;
    private javax.swing.JPanel pnlClinicalNotes;
    private javax.swing.JPanel pnlClinicalNotesGlue;
    private javax.swing.JPanel pnlClinicalNotesHeading;
    private javax.swing.JPanel pnlConsultationActions;
    private javax.swing.JPanel pnlConsultationButtons;
    private javax.swing.JPanel pnlConsultationHeader;
    private javax.swing.JPanel pnlConsultationHeaderLables;
    private javax.swing.JPanel pnlConsultationPatientSummary;
    private javax.swing.JPanel pnlConsultationPrescription;
    private javax.swing.JPanel pnlConsultationSeclector;
    private javax.swing.JPanel pnlCurrentConsultation;
    private javax.swing.JPanel pnlDiagnosisGroup;
    private javax.swing.JPanel pnlExternalMedicineGroup;
    private javax.swing.JPanel pnlPrescription;
    private javax.swing.JPanel pnlPrescriptionDoseGroup;
    private javax.swing.JPanel pnlPrescriptionDoseRow;
    private javax.swing.JPanel pnlPrescriptionDurationGroup;
    private javax.swing.JPanel pnlPrescriptionEditor;
    private javax.swing.JPanel pnlPrescriptionEntryGlue;
    private javax.swing.JPanel pnlPrescriptionFrequencyGroup;
    private javax.swing.JPanel pnlPrescriptionHeader;
    private javax.swing.JPanel pnlPrescriptionInstructionsGroup;
    private javax.swing.JPanel pnlPrescriptionItemButtons;
    private javax.swing.JPanel pnlPrescriptionList;
    private javax.swing.JPanel pnlPrescriptionListFooter;
    private javax.swing.JPanel pnlPrescriptionListHeader;
    private javax.swing.JPanel pnlPrescriptionMedicineRow;
    private javax.swing.JPanel pnlPrescriptionQuantityGroup;
    private javax.swing.JPanel pnlSymptomsGroup;
    private javax.swing.JPanel pnlTreatmentGroup;
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
