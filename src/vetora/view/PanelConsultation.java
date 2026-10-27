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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblPrescriptionListHint = new javax.swing.JLabel();
        pnlPrescriptionListFooter = new javax.swing.JPanel();
        lblPrescriptionListTitle = new javax.swing.JLabel();
        pnlPrescriptionListHeader = new javax.swing.JPanel();
        pnlPrescriptionList = new javax.swing.JPanel();
        pnlPrescriptionEntryGlue = new javax.swing.JPanel();
        pnlPrescriptionInstructionsGroup = new javax.swing.JPanel();
        pnlPrescriptionHeader = new javax.swing.JPanel();
        pnlPrescriptionEditor = new javax.swing.JPanel();
        pnlConsultationPrescription = new javax.swing.JPanel();
        pnlClinicalNotesGlue = new javax.swing.JPanel();
        pnlAssessmentRow = new javax.swing.JPanel();
        pnlTreatmentGroup = new javax.swing.JPanel();
        pnlDiagnosisGroup = new javax.swing.JPanel();
        pnlSymptomsGroup = new javax.swing.JPanel();
        pnlClinicalNotesHeading = new javax.swing.JPanel();

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
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13));
        lblSymptoms = new javax.swing.JLabel();
        scrSymptomsText = new javax.swing.JScrollPane();
        txtSymptoms = new javax.swing.JTextArea();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13));
        lblDiagnosis = new javax.swing.JLabel();
        scrDiagnosisText = new javax.swing.JScrollPane();
        txtDiagnosis = new javax.swing.JTextArea();
        lblTreatmentNotes = new javax.swing.JLabel();
        scrTreatmentNotesText = new javax.swing.JScrollPane();
        txtTreatmentNotes = new javax.swing.JTextArea();
        lblClinicalNotesHint = new javax.swing.JLabel();
        scrPrescription = new javax.swing.JScrollPane();
        pnlPrescription = new javax.swing.JPanel();
        lblPrescriptionTitle = new javax.swing.JLabel();
        lblFulfilment = new javax.swing.JLabel();
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
        lblPrescriptionInsructions = new javax.swing.JLabel();
        scrPrescriptionInstructions = new javax.swing.JScrollPane();
        txtPrescriptionInstructions = new javax.swing.JTextArea();
        pnlPrescriptionItemButtons = new javax.swing.JPanel();
        btnAddPrescriptionItem = new javax.swing.JButton();
        btnRemovePrescriptionItem = new javax.swing.JButton();
        btnClearPrescriptionItem = new javax.swing.JButton();
        scrPrescriptionItems = new javax.swing.JScrollPane();
        tblPrescriptionItems = new javax.swing.JTable();
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
        setLayout(new java.awt.BorderLayout(0, 13));

        pnlConsultationHeader.setBackground(new java.awt.Color(255, 255, 255));
        pnlConsultationHeader.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 13, 8, 13));
        pnlConsultationHeader.setLayout(new java.awt.BorderLayout(0, 5));

        pnlConsultationHeaderLables.setBackground(new java.awt.Color(255, 255, 255));
        pnlConsultationHeaderLables.setPreferredSize(new java.awt.Dimension(0, 21));
        pnlConsultationHeaderLables.setLayout(new java.awt.BorderLayout(13, 0));

        lblSelectedConsultationAppointment.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblSelectedConsultationAppointment.setText("Consultation appointment");
        lblSelectedConsultationAppointment.setForeground(new java.awt.Color(71, 85, 105));

        lblConsultationStatus.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblConsultationStatus.setText("Consultation: Not loaded");
        lblConsultationStatus.setForeground(new java.awt.Color(71, 85, 105));


        pnlConsultationSeclector.setBackground(new java.awt.Color(255, 255, 255));
        pnlConsultationSeclector.setPreferredSize(new java.awt.Dimension(0, 42));
        pnlConsultationSeclector.setLayout(new java.awt.BorderLayout(13, 0));
        pnlConsultationSeclector.setMinimumSize(new java.awt.Dimension(0, 42));
        pnlConsultationSeclector.setMaximumSize(new java.awt.Dimension(32767, 42));
        pnlConsultationSeclector.setAlignmentX(0.0F);

        cmbConsultationAppointment.setFont(new java.awt.Font("Segoe UI", 0, 16));
        cmbConsultationAppointment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select appointment" }));
        cmbConsultationAppointment.setPreferredSize(new java.awt.Dimension(233, 42));
        cmbConsultationAppointment.setMinimumSize(new java.awt.Dimension(0, 42));
        cmbConsultationAppointment.setMaximumSize(new java.awt.Dimension(32767, 42));
        cmbConsultationAppointment.setAlignmentX(0.0F);
        cmbConsultationAppointment.addActionListener(this::cmbConsultationAppointmentActionPerformed);

        btnLoadConsultation.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnLoadConsultation.setText("Load visit");
        btnLoadConsultation.setPreferredSize(new java.awt.Dimension(144, 42));
        btnLoadConsultation.setMinimumSize(new java.awt.Dimension(144, 42));
        btnLoadConsultation.setMaximumSize(new java.awt.Dimension(144, 42));
        btnLoadConsultation.setBackground(new java.awt.Color(255, 255, 255));
        btnLoadConsultation.setForeground(new java.awt.Color(15, 118, 110));
        btnLoadConsultation.addActionListener(this::btnLoadConsultationActionPerformed);


        pnlConsultationPatientSummary.setBackground(new java.awt.Color(255, 255, 255));
        pnlConsultationPatientSummary.setPreferredSize(new java.awt.Dimension(0, 39));
        pnlConsultationPatientSummary.setLayout(new java.awt.GridLayout(2, 3, 13, 5));

        lblPatientPet.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPatientPet.setText("Pet: —");
        lblPatientPet.setForeground(new java.awt.Color(71, 85, 105));

        lblPatientOwner.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPatientOwner.setText("Owner: —");
        lblPatientOwner.setForeground(new java.awt.Color(71, 85, 105));

        lblPatientSpecies.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPatientSpecies.setText("Species: —");
        lblPatientSpecies.setForeground(new java.awt.Color(71, 85, 105));

        lblPatientAge.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPatientAge.setText("Age: —");
        lblPatientAge.setForeground(new java.awt.Color(71, 85, 105));

        lblConsultationVet.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblConsultationVet.setText("Veterinarian: —");
        lblConsultationVet.setForeground(new java.awt.Color(71, 85, 105));

        lblConsultationVisit.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblConsultationVisit.setText("Visit: —");
        lblConsultationVisit.setForeground(new java.awt.Color(71, 85, 105));



        tabConsultation.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        pnlCurrentConsultation.setBackground(new java.awt.Color(245, 247, 250));
        pnlCurrentConsultation.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlCurrentConsultation.setLayout(new java.awt.BorderLayout(0, 13));

        splitConsultation.setDividerSize(5);
        splitConsultation.setResizeWeight(0.382);

        scrClinicalNotes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrClinicalNotes.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrClinicalNotes.setMinimumSize(new java.awt.Dimension(300, 0));

        pnlClinicalNotes.setBackground(new java.awt.Color(255, 255, 255));
        pnlClinicalNotes.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlClinicalNotes.setLayout(new javax.swing.BoxLayout(pnlClinicalNotes, javax.swing.BoxLayout.Y_AXIS));

        lblClinicalNotesTitle.setFont(new java.awt.Font("Segoe UI", 1, 21));
        lblClinicalNotesTitle.setText("Clinical notes");
        lblClinicalNotesTitle.setForeground(new java.awt.Color(15, 23, 42));

        lblSymptoms.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblSymptoms.setText("Symptoms *");
        lblSymptoms.setPreferredSize(new java.awt.Dimension(0, 21));
        lblSymptoms.setForeground(new java.awt.Color(71, 85, 105));

        scrSymptomsText.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scrSymptomsText.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrSymptomsText.setAlignmentX(0.0F);
        scrSymptomsText.setMaximumSize(new java.awt.Dimension(32767, 89));
        scrSymptomsText.setMinimumSize(new java.awt.Dimension(0, 89));
        scrSymptomsText.setPreferredSize(new java.awt.Dimension(233, 89));

        txtSymptoms.setEditable(false);
        txtSymptoms.setColumns(0);
        txtSymptoms.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtSymptoms.setLineWrap(true);
        txtSymptoms.setWrapStyleWord(true);
        txtSymptoms.setMargin(new java.awt.Insets(4, 8, 4, 8));
        txtSymptoms.setRows(3);
        scrSymptomsText.setFont(new java.awt.Font("Segoe UI", 0, 16));
        scrSymptomsText.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        lblDiagnosis.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblDiagnosis.setText("Diagnosis *");
        lblDiagnosis.setPreferredSize(new java.awt.Dimension(0, 21));
        lblDiagnosis.setForeground(new java.awt.Color(71, 85, 105));

        scrDiagnosisText.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scrDiagnosisText.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrDiagnosisText.setAlignmentX(0.0F);
        scrDiagnosisText.setMaximumSize(new java.awt.Dimension(32767, 89));
        scrDiagnosisText.setMinimumSize(new java.awt.Dimension(0, 89));
        scrDiagnosisText.setPreferredSize(new java.awt.Dimension(233, 89));

        txtDiagnosis.setEditable(false);
        txtDiagnosis.setColumns(0);
        txtDiagnosis.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtDiagnosis.setLineWrap(true);
        txtDiagnosis.setWrapStyleWord(true);
        txtDiagnosis.setMargin(new java.awt.Insets(4, 8, 4, 8));
        txtDiagnosis.setRows(3);
        scrDiagnosisText.setFont(new java.awt.Font("Segoe UI", 0, 16));
        scrDiagnosisText.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        lblTreatmentNotes.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblTreatmentNotes.setText("Treatment notes (optional)");
        lblTreatmentNotes.setPreferredSize(new java.awt.Dimension(0, 21));
        lblTreatmentNotes.setForeground(new java.awt.Color(71, 85, 105));

        scrTreatmentNotesText.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scrTreatmentNotesText.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrTreatmentNotesText.setAlignmentX(0.0F);
        scrTreatmentNotesText.setMaximumSize(new java.awt.Dimension(32767, 89));
        scrTreatmentNotesText.setMinimumSize(new java.awt.Dimension(0, 89));
        scrTreatmentNotesText.setPreferredSize(new java.awt.Dimension(233, 89));

        txtTreatmentNotes.setEditable(false);
        txtTreatmentNotes.setColumns(0);
        txtTreatmentNotes.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtTreatmentNotes.setLineWrap(true);
        txtTreatmentNotes.setWrapStyleWord(true);
        txtTreatmentNotes.setMargin(new java.awt.Insets(4, 8, 4, 8));
        txtTreatmentNotes.setRows(3);
        scrTreatmentNotesText.setFont(new java.awt.Font("Segoe UI", 0, 16));
        scrTreatmentNotesText.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        lblClinicalNotesHint.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblClinicalNotesHint.setForeground(new java.awt.Color(71, 85, 105));
        lblClinicalNotesHint.setText("* Required when finalizing");

        scrClinicalNotes.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        scrPrescription.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrPrescription.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrPrescription.setMinimumSize(new java.awt.Dimension(0, 0));

        pnlPrescription.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescription.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlPrescription.setLayout(new javax.swing.BoxLayout(pnlPrescription, javax.swing.BoxLayout.Y_AXIS));

        lblPrescriptionTitle.setFont(new java.awt.Font("Segoe UI", 1, 21));
        lblPrescriptionTitle.setText("Prescription");
        lblPrescriptionTitle.setForeground(new java.awt.Color(15, 23, 42));

        lblFulfilment.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblFulfilment.setText("External pharmacy");
        lblFulfilment.setForeground(new java.awt.Color(71, 85, 105));
        lblFulfilment.setToolTipText("Supply: External pharmacy");


        pnlPrescriptionMedicineRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionMedicineRow.setAlignmentX(0.0F);
        pnlPrescriptionMedicineRow.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPrescriptionMedicineRow.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPrescriptionMedicineRow.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPrescriptionMedicineRow.setLayout(new java.awt.BorderLayout(13, 0));

        pnlExternalMedicineGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlExternalMedicineGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlExternalMedicineGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlExternalMedicineGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlExternalMedicineGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlExternalMedicineGroup.setAlignmentX(0.0F);

        lblExtterrnalMedicine.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblExtterrnalMedicine.setText("Medicine *");
        lblExtterrnalMedicine.setPreferredSize(new java.awt.Dimension(0, 21));
        lblExtterrnalMedicine.setForeground(new java.awt.Color(71, 85, 105));

        txtExternalMedicine.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtExternalMedicine.setEnabled(false);
        txtExternalMedicine.setMinimumSize(new java.awt.Dimension(0, 42));
        txtExternalMedicine.setPreferredSize(new java.awt.Dimension(233, 42));
        txtExternalMedicine.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtExternalMedicine.setAlignmentX(0.0F);


        pnlPrescriptionQuantityGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionQuantityGroup.setPreferredSize(new java.awt.Dimension(89, 68));
        pnlPrescriptionQuantityGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlPrescriptionQuantityGroup.setMinimumSize(new java.awt.Dimension(89, 68));
        pnlPrescriptionQuantityGroup.setMaximumSize(new java.awt.Dimension(89, 68));
        pnlPrescriptionQuantityGroup.setAlignmentX(0.0F);

        lblPrescribedQuantity.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPrescribedQuantity.setText("Quantity *");
        lblPrescribedQuantity.setPreferredSize(new java.awt.Dimension(0, 21));
        lblPrescribedQuantity.setForeground(new java.awt.Color(71, 85, 105));

        spnPrescribedQuantity.setFont(new java.awt.Font("Segoe UI", 0, 16));
        spnPrescribedQuantity.setModel(new javax.swing.SpinnerNumberModel(1, 1, 9999, 1));
        spnPrescribedQuantity.setEnabled(false);
        spnPrescribedQuantity.setMinimumSize(new java.awt.Dimension(0, 42));
        spnPrescribedQuantity.setPreferredSize(new java.awt.Dimension(233, 42));
        spnPrescribedQuantity.setMaximumSize(new java.awt.Dimension(32767, 42));
        spnPrescribedQuantity.setAlignmentX(0.0F);



        pnlPrescriptionDoseRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionDoseRow.setAlignmentX(0.0F);
        pnlPrescriptionDoseRow.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPrescriptionDoseRow.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPrescriptionDoseRow.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPrescriptionDoseRow.setLayout(new java.awt.GridLayout(1, 3, 8, 0));

        pnlPrescriptionDoseGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionDoseGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlPrescriptionDoseGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPrescriptionDoseGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPrescriptionDoseGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPrescriptionDoseGroup.setAlignmentX(0.0F);

        lblDoseText.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblDoseText.setText("Dose *");
        lblDoseText.setToolTipText("Required when finalizing a prescription item.");
        lblDoseText.setPreferredSize(new java.awt.Dimension(0, 21));
        lblDoseText.setForeground(new java.awt.Color(71, 85, 105));

        txtDoseText.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtDoseText.setEnabled(false);
        txtDoseText.setMinimumSize(new java.awt.Dimension(0, 42));
        txtDoseText.setPreferredSize(new java.awt.Dimension(233, 42));
        txtDoseText.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtDoseText.setAlignmentX(0.0F);


        pnlPrescriptionFrequencyGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionFrequencyGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlPrescriptionFrequencyGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPrescriptionFrequencyGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPrescriptionFrequencyGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPrescriptionFrequencyGroup.setAlignmentX(0.0F);

        lblFrequency.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblFrequency.setText("Frequency *");
        lblFrequency.setToolTipText("Required when finalizing a prescription item.");
        lblFrequency.setPreferredSize(new java.awt.Dimension(0, 21));
        lblFrequency.setForeground(new java.awt.Color(71, 85, 105));

        txtFrequency.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtFrequency.setEnabled(false);
        txtFrequency.setMinimumSize(new java.awt.Dimension(0, 42));
        txtFrequency.setPreferredSize(new java.awt.Dimension(233, 42));
        txtFrequency.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtFrequency.setAlignmentX(0.0F);


        pnlPrescriptionDurationGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionDurationGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlPrescriptionDurationGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPrescriptionDurationGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPrescriptionDurationGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPrescriptionDurationGroup.setAlignmentX(0.0F);

        lblDuration.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblDuration.setText("Duration *");
        lblDuration.setToolTipText("Required when finalizing a prescription item.");
        lblDuration.setPreferredSize(new java.awt.Dimension(0, 21));
        lblDuration.setForeground(new java.awt.Color(71, 85, 105));

        txtDuration.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtDuration.setEnabled(false);
        txtDuration.setMinimumSize(new java.awt.Dimension(0, 42));
        txtDuration.setPreferredSize(new java.awt.Dimension(233, 42));
        txtDuration.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtDuration.setAlignmentX(0.0F);



        lblPrescriptionInsructions.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPrescriptionInsructions.setText("Instructions (optional)");
        lblPrescriptionInsructions.setPreferredSize(new java.awt.Dimension(0, 21));
        lblPrescriptionInsructions.setForeground(new java.awt.Color(71, 85, 105));

        scrPrescriptionInstructions.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scrPrescriptionInstructions.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrPrescriptionInstructions.setAlignmentX(0.0F);
        scrPrescriptionInstructions.setMaximumSize(new java.awt.Dimension(32767, 68));
        scrPrescriptionInstructions.setMinimumSize(new java.awt.Dimension(0, 68));
        scrPrescriptionInstructions.setPreferredSize(new java.awt.Dimension(233, 68));

        txtPrescriptionInstructions.setColumns(0);
        txtPrescriptionInstructions.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtPrescriptionInstructions.setLineWrap(true);
        txtPrescriptionInstructions.setWrapStyleWord(true);
        txtPrescriptionInstructions.setEnabled(false);
        txtPrescriptionInstructions.setMargin(new java.awt.Insets(4, 8, 4, 8));
        txtPrescriptionInstructions.setRows(2);
        scrPrescriptionInstructions.setFont(new java.awt.Font("Segoe UI", 0, 16));
        scrPrescriptionInstructions.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        pnlPrescriptionItemButtons.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionItemButtons.setAlignmentX(0.0F);
        pnlPrescriptionItemButtons.setMaximumSize(new java.awt.Dimension(32767, 42));
        pnlPrescriptionItemButtons.setMinimumSize(new java.awt.Dimension(0, 42));
        pnlPrescriptionItemButtons.setPreferredSize(new java.awt.Dimension(233, 42));
        pnlPrescriptionItemButtons.setLayout(new java.awt.GridLayout(1, 2, 8, 0));

        btnAddPrescriptionItem.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnAddPrescriptionItem.setForeground(new java.awt.Color(255, 255, 255));
        btnAddPrescriptionItem.setText("Add item");
        btnAddPrescriptionItem.setEnabled(false);
        btnAddPrescriptionItem.setPreferredSize(new java.awt.Dimension(0, 42));
        btnAddPrescriptionItem.setBackground(new java.awt.Color(15, 118, 110));
        btnAddPrescriptionItem.addActionListener(this::btnAddPrescriptionItemActionPerformed);

        btnRemovePrescriptionItem.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnRemovePrescriptionItem.setForeground(new java.awt.Color(185, 28, 28));
        btnRemovePrescriptionItem.setText("Remove");
        btnRemovePrescriptionItem.setEnabled(false);
        btnRemovePrescriptionItem.setPreferredSize(new java.awt.Dimension(89, 42));
        btnRemovePrescriptionItem.setMinimumSize(new java.awt.Dimension(89, 42));
        btnRemovePrescriptionItem.setMaximumSize(new java.awt.Dimension(89, 42));
        btnRemovePrescriptionItem.setBackground(new java.awt.Color(255, 255, 255));
        btnRemovePrescriptionItem.addActionListener(this::btnRemovePrescriptionItemActionPerformed);

        btnClearPrescriptionItem.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnClearPrescriptionItem.setForeground(new java.awt.Color(15, 118, 110));
        btnClearPrescriptionItem.setText("Clear fields");
        btnClearPrescriptionItem.setEnabled(false);
        btnClearPrescriptionItem.setPreferredSize(new java.awt.Dimension(0, 42));
        btnClearPrescriptionItem.setBackground(new java.awt.Color(255, 255, 255));
        btnClearPrescriptionItem.addActionListener(this::btnClearPrescriptionItemActionPerformed);


        scrPrescriptionItems.setAlignmentX(0.0F);
        scrPrescriptionItems.setMaximumSize(new java.awt.Dimension(32767, 32767));
        scrPrescriptionItems.setMinimumSize(new java.awt.Dimension(0, 89));
        scrPrescriptionItems.setPreferredSize(new java.awt.Dimension(233, 144));

        tblPrescriptionItems.setFont(new java.awt.Font("Segoe UI", 0, 16));
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
        tblPrescriptionItems.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblPrescriptionItems.setColumnSelectionAllowed(false);
        tblPrescriptionItems.setRowSelectionAllowed(true);
        tblPrescriptionItems.setFillsViewportHeight(true);
        tblPrescriptionItems.setGridColor(new java.awt.Color(226, 232, 240));
        tblPrescriptionItems.setRowHeight(34);
        tblPrescriptionItems.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblPrescriptionItems.setShowHorizontalLines(true);
        tblPrescriptionItems.setSelectionBackground(new java.awt.Color(204, 251, 241));
        tblPrescriptionItems.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tblPrescriptionItems.setBackground(new java.awt.Color(255, 255, 255));
        tblPrescriptionItems.setForeground(new java.awt.Color(15, 23, 42));
        tblPrescriptionItems.setShowVerticalLines(false);
        tblPrescriptionItems.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tblPrescriptionItems.getColumnModel().getColumnCount() > 0) {
            tblPrescriptionItems.getColumnModel().getColumn(0).setPreferredWidth(144);
            tblPrescriptionItems.getColumnModel().getColumn(1).setPreferredWidth(55);
            tblPrescriptionItems.getColumnModel().getColumn(2).setPreferredWidth(89);
            tblPrescriptionItems.getColumnModel().getColumn(3).setPreferredWidth(89);
            tblPrescriptionItems.getColumnModel().getColumn(4).setPreferredWidth(89);
            tblPrescriptionItems.getColumnModel().getColumn(5).setPreferredWidth(144);
        }


        lblPrescriptionCount.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPrescriptionCount.setForeground(new java.awt.Color(71, 85, 105));
        lblPrescriptionCount.setText("0 prescription items");

        scrPrescription.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);



        pnlConsultationActions.setBackground(new java.awt.Color(245, 247, 250));
        pnlConsultationActions.setPreferredSize(new java.awt.Dimension(0, 42));
        pnlConsultationActions.setLayout(new java.awt.BorderLayout(13, 0));

        lblConsultationError.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblConsultationError.setForeground(new java.awt.Color(185, 28, 28));
        lblConsultationError.setText(" ");

        pnlConsultationButtons.setBackground(new java.awt.Color(245, 247, 250));
        pnlConsultationButtons.setLayout(new javax.swing.BoxLayout(pnlConsultationButtons, javax.swing.BoxLayout.X_AXIS));

        btnSaveDraft.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnSaveDraft.setForeground(new java.awt.Color(15, 118, 110));
        btnSaveDraft.setText("Save draft");
        btnSaveDraft.setEnabled(false);
        btnSaveDraft.setMaximumSize(new java.awt.Dimension(144, 42));
        btnSaveDraft.setMinimumSize(new java.awt.Dimension(144, 42));
        btnSaveDraft.setPreferredSize(new java.awt.Dimension(144, 42));
        btnSaveDraft.setBackground(new java.awt.Color(255, 255, 255));
        btnSaveDraft.addActionListener(this::btnSaveDraftActionPerformed);

        btnFinalizeConsultation.setBackground(new java.awt.Color(15, 118, 110));
        btnFinalizeConsultation.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnFinalizeConsultation.setForeground(new java.awt.Color(255, 255, 255));
        btnFinalizeConsultation.setText("Finalize consultation");
        btnFinalizeConsultation.setEnabled(false);
        btnFinalizeConsultation.setMaximumSize(new java.awt.Dimension(200, 42));
        btnFinalizeConsultation.setMinimumSize(new java.awt.Dimension(200, 42));
        btnFinalizeConsultation.setPreferredSize(new java.awt.Dimension(200, 42));
        btnFinalizeConsultation.addActionListener(this::btnFinalizeConsultationActionPerformed);




        pnlClinicalNotesHeading.setBackground(new java.awt.Color(255, 255, 255));
        pnlClinicalNotesHeading.setLayout(new java.awt.BorderLayout(13, 0));
        pnlClinicalNotesHeading.setMinimumSize(new java.awt.Dimension(0, 26));
        pnlClinicalNotesHeading.setPreferredSize(new java.awt.Dimension(233, 26));
        pnlClinicalNotesHeading.setMaximumSize(new java.awt.Dimension(32767, 26));
        pnlClinicalNotesHeading.setAlignmentX(0.0F);
        pnlSymptomsGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlSymptomsGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlSymptomsGroup.setMinimumSize(new java.awt.Dimension(0, 115));
        pnlSymptomsGroup.setPreferredSize(new java.awt.Dimension(233, 115));
        pnlSymptomsGroup.setMaximumSize(new java.awt.Dimension(32767, 115));
        pnlSymptomsGroup.setAlignmentX(0.0F);
        pnlDiagnosisGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlDiagnosisGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlDiagnosisGroup.setMinimumSize(new java.awt.Dimension(0, 115));
        pnlDiagnosisGroup.setPreferredSize(new java.awt.Dimension(233, 115));
        pnlDiagnosisGroup.setMaximumSize(new java.awt.Dimension(32767, 115));
        pnlDiagnosisGroup.setAlignmentX(0.0F);
        pnlTreatmentGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlTreatmentGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlTreatmentGroup.setMinimumSize(new java.awt.Dimension(0, 115));
        pnlTreatmentGroup.setPreferredSize(new java.awt.Dimension(233, 115));
        pnlTreatmentGroup.setMaximumSize(new java.awt.Dimension(32767, 115));
        pnlTreatmentGroup.setAlignmentX(0.0F);
        pnlAssessmentRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlAssessmentRow.setLayout(new java.awt.GridLayout(1, 2, 13, 0));
        pnlAssessmentRow.setMinimumSize(new java.awt.Dimension(0, 115));
        pnlAssessmentRow.setPreferredSize(new java.awt.Dimension(233, 115));
        pnlAssessmentRow.setMaximumSize(new java.awt.Dimension(32767, 115));
        pnlAssessmentRow.setAlignmentX(0.0F);
        pnlClinicalNotesGlue.setBackground(new java.awt.Color(255, 255, 255));
        pnlClinicalNotesGlue.setLayout(new javax.swing.BoxLayout(pnlClinicalNotesGlue, javax.swing.BoxLayout.Y_AXIS));
        pnlClinicalNotesGlue.setPreferredSize(new java.awt.Dimension(0, 0));
        pnlClinicalNotesGlue.setMinimumSize(new java.awt.Dimension(0, 0));
        pnlClinicalNotesGlue.setAlignmentX(0.0F);
        pnlConsultationPrescription.setBackground(new java.awt.Color(255, 255, 255));
        pnlConsultationPrescription.setLayout(new java.awt.BorderLayout(0, 0));
        pnlPrescriptionEditor.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionEditor.setLayout(new java.awt.BorderLayout(0, 8));
        pnlPrescriptionEditor.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlPrescriptionEditor.setMinimumSize(new java.awt.Dimension(360, 0));
        pnlPrescriptionHeader.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionHeader.setLayout(new java.awt.BorderLayout(13, 0));
        pnlPrescriptionInstructionsGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionInstructionsGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlPrescriptionInstructionsGroup.setMinimumSize(new java.awt.Dimension(0, 94));
        pnlPrescriptionInstructionsGroup.setPreferredSize(new java.awt.Dimension(233, 94));
        pnlPrescriptionInstructionsGroup.setMaximumSize(new java.awt.Dimension(32767, 94));
        pnlPrescriptionInstructionsGroup.setAlignmentX(0.0F);
        pnlPrescriptionEntryGlue.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionEntryGlue.setLayout(new javax.swing.BoxLayout(pnlPrescriptionEntryGlue, javax.swing.BoxLayout.Y_AXIS));
        pnlPrescriptionEntryGlue.setPreferredSize(new java.awt.Dimension(0, 0));
        pnlPrescriptionEntryGlue.setMinimumSize(new java.awt.Dimension(0, 0));
        pnlPrescriptionEntryGlue.setAlignmentX(0.0F);
        pnlPrescriptionList.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionList.setLayout(new java.awt.BorderLayout(0, 8));
        pnlPrescriptionList.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlPrescriptionList.setMinimumSize(new java.awt.Dimension(400, 0));
        pnlPrescriptionListHeader.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionListHeader.setLayout(new java.awt.BorderLayout(0, 5));
        lblPrescriptionListTitle.setText("Prescribed items");
        lblPrescriptionListTitle.setFont(new java.awt.Font("Segoe UI", 1, 21));
        lblPrescriptionListTitle.setForeground(new java.awt.Color(15, 23, 42));
        pnlPrescriptionListFooter.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrescriptionListFooter.setLayout(new java.awt.BorderLayout(13, 0));
        lblPrescriptionListHint.setText("<html>Select an item to remove it.<br>Hover a cell for its full text.</html>");
        lblPrescriptionListHint.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPrescriptionListHint.setForeground(new java.awt.Color(71, 85, 105));
        splitConsultation.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        pnlConsultationHeaderLables.add(lblSelectedConsultationAppointment, java.awt.BorderLayout.CENTER);
        pnlConsultationHeaderLables.add(lblConsultationStatus, java.awt.BorderLayout.EAST);
        pnlConsultationSeclector.add(cmbConsultationAppointment, java.awt.BorderLayout.CENTER);
        pnlConsultationSeclector.add(btnLoadConsultation, java.awt.BorderLayout.EAST);
        pnlConsultationPatientSummary.add(lblPatientPet);
        pnlConsultationPatientSummary.add(lblPatientOwner);
        pnlConsultationPatientSummary.add(lblPatientSpecies);
        pnlConsultationPatientSummary.add(lblPatientAge);
        pnlConsultationPatientSummary.add(lblConsultationVet);
        pnlConsultationPatientSummary.add(lblConsultationVisit);
        pnlConsultationHeader.add(pnlConsultationHeaderLables, java.awt.BorderLayout.NORTH);
        pnlConsultationHeader.add(pnlConsultationSeclector, java.awt.BorderLayout.CENTER);
        pnlConsultationHeader.add(pnlConsultationPatientSummary, java.awt.BorderLayout.SOUTH);
        pnlClinicalNotesHeading.add(lblClinicalNotesTitle, java.awt.BorderLayout.CENTER);
        pnlClinicalNotesHeading.add(lblClinicalNotesHint, java.awt.BorderLayout.EAST);
        scrSymptomsText.setViewportView(txtSymptoms);
        pnlSymptomsGroup.add(lblSymptoms, java.awt.BorderLayout.NORTH);
        pnlSymptomsGroup.add(scrSymptomsText, java.awt.BorderLayout.CENTER);
        scrDiagnosisText.setViewportView(txtDiagnosis);
        pnlDiagnosisGroup.add(lblDiagnosis, java.awt.BorderLayout.NORTH);
        pnlDiagnosisGroup.add(scrDiagnosisText, java.awt.BorderLayout.CENTER);
        pnlAssessmentRow.add(pnlSymptomsGroup);
        pnlAssessmentRow.add(pnlDiagnosisGroup);
        scrTreatmentNotesText.setViewportView(txtTreatmentNotes);
        pnlTreatmentGroup.add(lblTreatmentNotes, java.awt.BorderLayout.NORTH);
        pnlTreatmentGroup.add(scrTreatmentNotesText, java.awt.BorderLayout.CENTER);
        pnlClinicalNotes.add(pnlClinicalNotesHeading);
        pnlClinicalNotes.add(filler1);
        pnlClinicalNotes.add(pnlAssessmentRow);
        pnlClinicalNotes.add(filler3);
        pnlClinicalNotes.add(pnlTreatmentGroup);
        pnlClinicalNotes.add(pnlClinicalNotesGlue);
        scrClinicalNotes.setViewportView(pnlClinicalNotes);
        pnlCurrentConsultation.add(scrClinicalNotes, java.awt.BorderLayout.CENTER);
        pnlPrescriptionHeader.add(lblPrescriptionTitle, java.awt.BorderLayout.CENTER);
        pnlPrescriptionHeader.add(lblFulfilment, java.awt.BorderLayout.EAST);
        pnlExternalMedicineGroup.add(lblExtterrnalMedicine, java.awt.BorderLayout.NORTH);
        pnlExternalMedicineGroup.add(txtExternalMedicine, java.awt.BorderLayout.CENTER);
        pnlPrescriptionQuantityGroup.add(lblPrescribedQuantity, java.awt.BorderLayout.NORTH);
        pnlPrescriptionQuantityGroup.add(spnPrescribedQuantity, java.awt.BorderLayout.CENTER);
        pnlPrescriptionMedicineRow.add(pnlExternalMedicineGroup, java.awt.BorderLayout.CENTER);
        pnlPrescriptionMedicineRow.add(pnlPrescriptionQuantityGroup, java.awt.BorderLayout.EAST);
        pnlPrescriptionDoseGroup.add(lblDoseText, java.awt.BorderLayout.NORTH);
        pnlPrescriptionDoseGroup.add(txtDoseText, java.awt.BorderLayout.CENTER);
        pnlPrescriptionFrequencyGroup.add(lblFrequency, java.awt.BorderLayout.NORTH);
        pnlPrescriptionFrequencyGroup.add(txtFrequency, java.awt.BorderLayout.CENTER);
        pnlPrescriptionDurationGroup.add(lblDuration, java.awt.BorderLayout.NORTH);
        pnlPrescriptionDurationGroup.add(txtDuration, java.awt.BorderLayout.CENTER);
        pnlPrescriptionDoseRow.add(pnlPrescriptionDoseGroup);
        pnlPrescriptionDoseRow.add(pnlPrescriptionFrequencyGroup);
        pnlPrescriptionDoseRow.add(pnlPrescriptionDurationGroup);
        scrPrescriptionInstructions.setViewportView(txtPrescriptionInstructions);
        pnlPrescriptionInstructionsGroup.add(lblPrescriptionInsructions, java.awt.BorderLayout.NORTH);
        pnlPrescriptionInstructionsGroup.add(scrPrescriptionInstructions, java.awt.BorderLayout.CENTER);
        pnlPrescription.add(pnlPrescriptionMedicineRow);
        pnlPrescription.add(filler11);
        pnlPrescription.add(pnlPrescriptionDoseRow);
        pnlPrescription.add(filler12);
        pnlPrescription.add(pnlPrescriptionInstructionsGroup);
        pnlPrescription.add(pnlPrescriptionEntryGlue);
        scrPrescription.setViewportView(pnlPrescription);
        pnlPrescriptionItemButtons.add(btnAddPrescriptionItem);
        pnlPrescriptionItemButtons.add(btnClearPrescriptionItem);
        pnlPrescriptionEditor.add(pnlPrescriptionHeader, java.awt.BorderLayout.NORTH);
        pnlPrescriptionEditor.add(scrPrescription, java.awt.BorderLayout.CENTER);
        pnlPrescriptionEditor.add(pnlPrescriptionItemButtons, java.awt.BorderLayout.SOUTH);
        pnlPrescriptionListHeader.add(lblPrescriptionListTitle, java.awt.BorderLayout.NORTH);
        pnlPrescriptionListHeader.add(lblPrescriptionCount, java.awt.BorderLayout.CENTER);
        scrPrescriptionItems.setViewportView(tblPrescriptionItems);
        pnlPrescriptionListFooter.add(lblPrescriptionListHint, java.awt.BorderLayout.CENTER);
        pnlPrescriptionListFooter.add(btnRemovePrescriptionItem, java.awt.BorderLayout.EAST);
        pnlPrescriptionList.add(pnlPrescriptionListHeader, java.awt.BorderLayout.NORTH);
        pnlPrescriptionList.add(scrPrescriptionItems, java.awt.BorderLayout.CENTER);
        pnlPrescriptionList.add(pnlPrescriptionListFooter, java.awt.BorderLayout.SOUTH);
        splitConsultation.setLeftComponent(pnlPrescriptionEditor);
        splitConsultation.setRightComponent(pnlPrescriptionList);
        pnlConsultationPrescription.add(splitConsultation, java.awt.BorderLayout.CENTER);
        tabConsultation.addTab("Clinical notes", pnlCurrentConsultation);
        tabConsultation.addTab("Prescription (0)", pnlConsultationPrescription);
        pnlConsultationButtons.add(btnSaveDraft);
        pnlConsultationButtons.add(filler17);
        pnlConsultationButtons.add(btnFinalizeConsultation);
        pnlConsultationActions.add(lblConsultationError, java.awt.BorderLayout.CENTER);
        pnlConsultationActions.add(pnlConsultationButtons, java.awt.BorderLayout.EAST);
        add(pnlConsultationHeader, java.awt.BorderLayout.NORTH);
        add(tabConsultation, java.awt.BorderLayout.CENTER);
        add(pnlConsultationActions, java.awt.BorderLayout.SOUTH);
        tblPrescriptionItems.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        tblPrescriptionItems.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        tblPrescriptionItems.getTableHeader().setReorderingAllowed(false);
        scrPrescriptionItems.setColumnHeaderView(tblPrescriptionItems.getTableHeader());
        scrPrescriptionItems.getViewport().setBackground(java.awt.Color.WHITE);
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblPrescriptionListHint;
    private javax.swing.JPanel pnlPrescriptionListFooter;
    private javax.swing.JLabel lblPrescriptionListTitle;
    private javax.swing.JPanel pnlPrescriptionListHeader;
    private javax.swing.JPanel pnlPrescriptionList;
    private javax.swing.JPanel pnlPrescriptionEntryGlue;
    private javax.swing.JPanel pnlPrescriptionInstructionsGroup;
    private javax.swing.JPanel pnlPrescriptionHeader;
    private javax.swing.JPanel pnlPrescriptionEditor;
    private javax.swing.JPanel pnlConsultationPrescription;
    private javax.swing.JPanel pnlClinicalNotesGlue;
    private javax.swing.JPanel pnlAssessmentRow;
    private javax.swing.JPanel pnlTreatmentGroup;
    private javax.swing.JPanel pnlDiagnosisGroup;
    private javax.swing.JPanel pnlSymptomsGroup;
    private javax.swing.JPanel pnlClinicalNotesHeading;
    private javax.swing.JButton btnAddPrescriptionItem;
    private javax.swing.JButton btnClearPrescriptionItem;
    private javax.swing.JButton btnFinalizeConsultation;
    private javax.swing.JButton btnLoadConsultation;
    private javax.swing.JButton btnRemovePrescriptionItem;
    private javax.swing.JButton btnSaveDraft;
    private javax.swing.JComboBox<String> cmbConsultationAppointment;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler17;
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
