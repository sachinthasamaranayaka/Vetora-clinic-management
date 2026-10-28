/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package vetora.view;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.JFormattedTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import vetora.model.Pet;
import vetora.model.Veterinarian;
import vetora.controller.AppointmentController;
import vetora.model.Appointment;

/**
 *
 * @author ASUS
 */
public class PanelAppointment extends javax.swing.JPanel {
    private boolean canManageAppointments;

    private AppointmentController controller;
    private final List<Pet> appointmentPets = new ArrayList<>();
    private boolean clinicVetReady;
    private final List<Appointment> displayedAppointments = new ArrayList<>();
    private boolean appointmentBusy;
    private Appointment reschedulingAppointment;
    private List<Object> savedAppointmentForm;
    private boolean splitReady;

    /**
     * Creates new form PanelAppointment
     */
    public PanelAppointment() {
        initComponents();
        setupDateSpinner(spnAppointmentFilterDate, true);
        setupDateSpinner(spnAppointmentDate, true);
        
        setupAppointmentUI();
        pnlAppointmentList.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                updateAppointmentActionLayout();
            }
        });
        
        cmbTimeSlot.removeAllItems();
        cmbTimeSlot.addItem("Select time");

        for (int hour = 9; hour < 17; hour++) {
            cmbTimeSlot.addItem(String.format("%02d:00", hour) );
            cmbTimeSlot.addItem(String.format("%02d:30", hour));
        }
        
        clearAppointmentForm();
        setAppointmentBusy(true);
        
        tblAppointments.getSelectionModel().addListSelectionListener(e -> {if(!e.getValueIsAdjusting()){showSelectedAppointment();}});
    }
    
    public Long getAppointmentPetId() {
        Pet pet = getSelectedAppointmentPet();
        return pet == null ? null : pet.getPetID();
    }

    public LocalDate getAppointmentDate() throws ParseException {
        spnAppointmentDate.commitEdit();
        Date date = (Date) spnAppointmentDate.getValue();
        return date.toInstant().atZone(ZoneId.of("Asia/Colombo")).toLocalDate();    
    }

    public LocalTime getAppointmentTime() {
        return cmbTimeSlot.getSelectedIndex() <= 0 ? null : LocalTime.parse(cmbTimeSlot.getSelectedItem().toString());
    }

    public String getAppointmentReason() {
        return txtVisitReason.getText();
    }

    public void setController(AppointmentController controller) {
        this.controller = controller;
    }
    
    public void setAppointmentBusy(boolean busy) {
        appointmentBusy = busy;
        cmbAppointmentPet.setEnabled(canManageAppointments && !busy && reschedulingAppointment == null && !appointmentPets.isEmpty());
        spnAppointmentDate.setEnabled(canManageAppointments && !busy);
        cmbTimeSlot.setEnabled(canManageAppointments && !busy);
        txtVisitReason.setEnabled(canManageAppointments && !busy);
        btnNewAppointment.setEnabled(canManageAppointments && !busy);
        btnClearAppointment.setEnabled(canManageAppointments && !busy);
        btnBook.setEnabled(canManageAppointments && !busy && clinicVetReady && !appointmentPets.isEmpty());
        btnBook.setText(busy ? "Please wait..." : reschedulingAppointment == null ? "Book visit" : "Save changes");
        tblAppointments.setEnabled(!busy && reschedulingAppointment == null);
        btnRefreshAppointments.setEnabled(!busy && reschedulingAppointment == null);
        spnAppointmentFilterDate.setEnabled(!busy && reschedulingAppointment == null);
        cmbStatusFilter.setEnabled(!busy && reschedulingAppointment == null);

        updateAppointmentActions();
    }

    public void showAppointmentError(String message) {
        lblAppointmentError.setForeground(new Color(185, 28, 28));
        lblAppointmentError.setText("<html>" + message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</html>");
        lblAppointmentError.setToolTipText(message.trim().isEmpty() ? null : message);
    }

    private Pet getSelectedAppointmentPet() {
        int index = cmbAppointmentPet.getSelectedIndex() - 1;

        return index >= 0 && index < appointmentPets.size() ? appointmentPets.get(index) : null;
    }
    
    public LocalDate getAppointmentFilterDate() throws ParseException {
        spnAppointmentFilterDate.commitEdit();
        Date date =(Date) spnAppointmentFilterDate.getValue();
        return date.toInstant().atZone(ZoneId.of("Asia/Colombo")).toLocalDate();
    }

    public String getAppointmentStatusFilter() {
        return cmbStatusFilter.getSelectedIndex() <= 0 ? null : cmbStatusFilter.getSelectedItem().toString();
    }

    public void showAppointmentListMessage(String message) {
        lblAppointmentCount.setText(message);
    }

    public void clearAppointmentList() {
        tblAppointments.clearSelection();
        tblAppointments.getSelectionModel().setAnchorSelectionIndex(-1);
        tblAppointments.getSelectionModel().setLeadSelectionIndex(-1);
        displayedAppointments.clear();

        DefaultTableModel tableModel = (DefaultTableModel) tblAppointments.getModel();

        tableModel.setRowCount(0);

        lblSelectedAppoinment.setText("Selected appointment: None");
        
        updateAppointmentActions();
    }
    
    public void displayPetOptions(List<Pet> pets) {
        Pet previous = getSelectedAppointmentPet();

        appointmentPets.clear();
        appointmentPets.addAll(pets);

        cmbAppointmentPet.removeAllItems();
        cmbAppointmentPet.addItem("Select pet");

        int selectedIndex = 0;

        for (int i = 0; i < appointmentPets.size(); i++) {
            Pet pet = appointmentPets.get(i);

            cmbAppointmentPet.addItem(pet.getPetID() + " - " + pet.getPetName() + " (" + pet.getOwnerName() + ")" );

            if (previous != null && previous.getPetID() == pet.getPetID()) {
                selectedIndex = i + 1;
            }
        }

        cmbAppointmentPet.setSelectedIndex(selectedIndex);
        showSelectedPetOwner();
    }
    
    public void showSelectedPetOwner(){
        Pet pet = getSelectedAppointmentPet();
        
        lblAppointmentOwner.setText(pet == null ? "Owner: —" : "Owner: " + pet.getOwnerName());
    }
    public void displayClinicVet(Veterinarian vet){
        String state = !vet.isActive() ? " (Inactive) " : !vet.isAvailable() ? " (Unavailable) " : "";
        lblAppointmentVet.setText("Veterinarian: " + vet.getFullName() + state);
        clinicVetReady = vet.isActive() && vet.isAvailable();
    }
    
    public void clearAppointmentOptions(){
        clinicVetReady = false;
        displayPetOptions(Collections.emptyList());
        lblAppointmentVet.setText("Veterinarian: —");
    }
    
    public void clearAppointmentForm(){
        reschedulingAppointment = null;
        cmbAppointmentPet.setSelectedIndex(0);
        cmbTimeSlot.setSelectedIndex(0);
        txtVisitReason.setText("");
        ZoneId zone = ZoneId.of("Asia/Colombo");
        spnAppointmentDate.setValue(Date.from(LocalDate.now(zone).atStartOfDay(zone).toInstant()));
        lblAppointmentId.setText("Appointment ID: Auto-generated");
        lblAppointmentFormTitle.setText("New appointment");
        lblAppointmentStatus.setText("Status: New");
        showSelectedPetOwner();
        showAppointmentError(" ");
        savedAppointmentForm = getAppointmentFormValues();
        setAppointmentBusy(appointmentBusy);
    }
    
    public void showAppointmentBooked(long id){
        JOptionPane.showMessageDialog(this, "Appointment  " + id + "  booked successfully.", "Appointment saved", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void setupDateSpinner(
        javax.swing.JSpinner spinner,boolean allowPastDates) {
        java.time.ZoneId clinicZone =java.time.ZoneId.of("Asia/Colombo");
        java.util.Date today = java.util.Date.from(java.time.LocalDate.now(clinicZone).atStartOfDay(clinicZone).toInstant());

        spinner.setModel(new javax.swing.SpinnerDateModel(today,allowPastDates ? null : today,null, java.util.Calendar.DAY_OF_MONTH));

        javax.swing.JSpinner.DateEditor editor = new javax.swing.JSpinner.DateEditor(spinner, "dd MMM yyyy");
        editor.getFormat().setLenient(false);
        editor.getFormat().setTimeZone(java.util.TimeZone.getTimeZone("Asia/Colombo"));

        spinner.setEditor(editor);
        editor.getTextField().setFocusLostBehavior(JFormattedTextField.PERSIST);
        editor.getTextField().setValue(today);
    }
    
    public void displayAppointments( List<Appointment> appointments, LocalDate date,String status) {
        clearAppointmentList();
        displayedAppointments.addAll(appointments);

        DefaultTableModel tableModel = (DefaultTableModel) tblAppointments.getModel();

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        for (Appointment appointment : appointments) {
            tableModel.addRow(new Object[]{
                appointment.getAppointmentId(),
                appointment.getAppointmentTime().format(timeFormat),
                appointment.getPetName(),
                appointment.getOwnerName(),
                appointment.getStatus()
            });
        }
        lblAppointmentListTitle.setText("Daily schedule: " + date);
        lblAppointmentCount.setText(appointments.size() + " appointment(s) | "+ (status == null ? "All statuses" : status));
    }
    
    public void showBookedDateInFilters(LocalDate date) {
        ZoneId zone = ZoneId.of("Asia/Colombo");
        spnAppointmentFilterDate.setValue(Date.from(date.atStartOfDay(zone).toInstant()));
        cmbStatusFilter.setSelectedIndex(0);
    }
    
    public Appointment getSelectedAppointment() {
        int row = tblAppointments.getSelectedRow();

        if (row < 0) {
            return null;
        }

        int modelRow = tblAppointments.convertRowIndexToModel(row);

        return modelRow < displayedAppointments.size() ? displayedAppointments.get(modelRow) : null;
    }

    private void showSelectedAppointment() {
        Appointment appointment = getSelectedAppointment();
        lblSelectedAppoinment.setText(appointment == null ? "Selected appointment: None" : "Selected appointment: " + appointment.getAppointmentId());
        
        updateAppointmentActions();
    }
    
    private void setupAppointmentUI(){
        tblAppointments.getTableHeader().setReorderingAllowed(false);
        tblAppointments.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        tblAppointments.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrAppointmentTable.getViewport().setBackground(java.awt.Color.WHITE);
        scrAppointmentTable.setColumnHeaderView(tblAppointments.getTableHeader());
        scrAppointmentTable.getVerticalScrollBar().setUnitIncrement(21);

        tblAppointments.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,13));
        scrAppointmentForm.getViewport().setBackground(Color.WHITE);
        scrAppointmentForm.getVerticalScrollBar().setUnitIncrement(21);
    }
    
    private void updateAppointmentActions() {
        Appointment selected = getSelectedAppointment();

        boolean scheduled = canManageAppointments && !appointmentBusy && reschedulingAppointment == null && selected != null && "Scheduled".equals(selected.getStatus());

        btnCheckIn.setEnabled(scheduled && LocalDate.now(ZoneId.of("Asia/Colombo")).equals(selected.getAppointmentDate()));
        btnCancelAppointment.setEnabled(scheduled);
        btnReschedule.setEnabled(scheduled && clinicVetReady);
        btnNoShow.setEnabled(scheduled && !selected.getAppointmentDate().atTime(selected.getAppointmentTime())
                .isAfter(LocalDateTime.now(ZoneId.of("Asia/Colombo"))));
    }
    
    public boolean confirmCancelAppointment(long id) {
        return JOptionPane.showConfirmDialog(this,"Cancel appointment " + id + "?","Cancel appointment",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
    public void showAppointmentActionError(String message) {
        JOptionPane.showMessageDialog(this,message,"Appointment",JOptionPane.ERROR_MESSAGE);
    }        

    public Appointment getReschedulingAppointment(){
        return reschedulingAppointment;
    }

    public void showRescheduleAppointment(Appointment appointment){
        reschedulingAppointment = appointment;
        for (int i = 0; i < appointmentPets.size(); i++) {
            if (appointmentPets.get(i).getPetID() == appointment.getPetId()) {
                cmbAppointmentPet.setSelectedIndex(i + 1);
                break;
            }
        }
        spnAppointmentDate.setValue(Date.from(appointment.getAppointmentDate()
                .atStartOfDay(ZoneId.of("Asia/Colombo")).toInstant()));
        cmbTimeSlot.setSelectedItem(appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        txtVisitReason.setText(appointment.getReason() == null ? "" : appointment.getReason());
        lblAppointmentFormTitle.setText("Reschedule appointment");
        lblAppointmentId.setText("Appointment ID: " + appointment.getAppointmentId());
        lblAppointmentStatus.setText("Status: Scheduled");
        showSelectedPetOwner();
        showAppointmentError("Choose a future date and time, then Save changes.");
        lblAppointmentError.setForeground(new Color(15, 118, 110));
        savedAppointmentForm = getAppointmentFormValues();
        setAppointmentBusy(false);
        spnAppointmentDate.requestFocusInWindow();
    }

    private List<Object> getAppointmentFormValues(){
        String dateText = ((javax.swing.JSpinner.DefaultEditor)spnAppointmentDate.getEditor()).getTextField().getText();
        return Arrays.asList(getAppointmentPetId(), dateText, cmbTimeSlot.getSelectedItem(), txtVisitReason.getText());
    }

    public boolean confirmDiscardAppointmentChanges(){
        return savedAppointmentForm == null || savedAppointmentForm.equals(getAppointmentFormValues())
                || JOptionPane.showConfirmDialog(this, "Discard the unsaved appointment details?", "Unsaved changes",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public boolean confirmNoShowAppointment(long id){
        return JOptionPane.showConfirmDialog(this, "Mark appointment " + id + " as no-show?",
                "Mark no-show", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void showAppointmentSaved(long id, boolean rescheduled){
        JOptionPane.showMessageDialog(this, "Appointment " + id + (rescheduled ? " rescheduled." : " booked."),
                "Appointment saved", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void doLayout(){
        super.doLayout();
        if (!splitReady && splitAppointments.getWidth() > 0) {
            splitReady = true;
            int availableWidth = splitAppointments.getWidth() - splitAppointments.getDividerSize();
            int formWidth = Math.max(360, (int)Math.round(availableWidth * 0.382));
            splitAppointments.setDividerLocation(Math.max(300, availableWidth - formWidth));
        }
        splitAppointments.doLayout();
        updateAppointmentActionLayout();
    }

    private void updateAppointmentActionLayout(){
        boolean compact = pnlAppointmentList.getWidth() < 560;
        java.awt.GridLayout layout = (java.awt.GridLayout)pnlAppointmentActions.getLayout();
        int rows = compact ? 2 : 1;
        if (layout.getRows() != rows) {
            layout.setRows(rows);
            layout.setColumns(compact ? 2 : 4);
            layout.setVgap(compact ? 8 : 0);
            pnlAppointmentActions.setPreferredSize(new java.awt.Dimension(0, compact ? 92 : 42));
            pnlAppointmentList.revalidate();
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

        pnlAppointmentToolbar = new javax.swing.JPanel();
        pnlAppointmentDateFilterGroup = new javax.swing.JPanel();
        lblAppointmentFillterDate = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        spnAppointmentFilterDate = new javax.swing.JSpinner();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0));
        pnlAppointmentStatusFilterGroup = new javax.swing.JPanel();
        lblStatusFilter = new javax.swing.JLabel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        cmbStatusFilter = new javax.swing.JComboBox<>();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0));
        btnRefreshAppointments = new javax.swing.JButton();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0));
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        btnNewAppointment = new javax.swing.JButton();
        splitAppointments = new javax.swing.JSplitPane();
        pnlAppointmentList = new javax.swing.JPanel();
        pnlAppointmentListHeader = new javax.swing.JPanel();
        lblAppointmentListTitle = new javax.swing.JLabel();
        lblAppointmentVet = new javax.swing.JLabel();
        scrAppointmentTable = new javax.swing.JScrollPane();
        tblAppointments = new javax.swing.JTable();
        pnlAppointmentFooter = new javax.swing.JPanel();
        lblSelectedAppoinment = new javax.swing.JLabel();
        pnlAppointmentActions = new javax.swing.JPanel();
        btnCheckIn = new javax.swing.JButton();
        btnReschedule = new javax.swing.JButton();
        btnCancelAppointment = new javax.swing.JButton();
        btnNoShow = new javax.swing.JButton();
        lblAppointmentCount = new javax.swing.JLabel();
        pnlAppointmentEdditor = new javax.swing.JPanel();
        pnlAppointmentFormHeader = new javax.swing.JPanel();
        lblAppointmentFormTitle = new javax.swing.JLabel();
        pnlAppointmentMetaRow = new javax.swing.JPanel();
        lblAppointmentId = new javax.swing.JLabel();
        lblAppointmentStatus = new javax.swing.JLabel();
        pnlAppointmentEntry = new javax.swing.JPanel();
        scrAppointmentForm = new javax.swing.JScrollPane();
        pnlAppointmentForm = new javax.swing.JPanel();
        pnlAppointmentPetGroup = new javax.swing.JPanel();
        pnlAppointmentPetInput = new javax.swing.JPanel();
        lblAppointmentPet = new javax.swing.JLabel();
        cmbAppointmentPet = new javax.swing.JComboBox<>();
        lblAppointmentOwner = new javax.swing.JLabel();
        filler15 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        pnlAppointmentScheduleGroup = new javax.swing.JPanel();
        pnlAppointmentDateTime = new javax.swing.JPanel();
        pnlAppointmentDateGroup = new javax.swing.JPanel();
        lblAppointmentDate = new javax.swing.JLabel();
        spnAppointmentDate = new javax.swing.JSpinner();
        pnlAppointmentTimeGroup = new javax.swing.JPanel();
        lblTimeSlot = new javax.swing.JLabel();
        cmbTimeSlot = new javax.swing.JComboBox<>();
        lblAppointmentDuration = new javax.swing.JLabel();
        filler16 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        pnlAppointmentFormGlue = new javax.swing.JPanel();
        filler17 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        pnlAppointmentReasonGroup = new javax.swing.JPanel();
        lblVisitReason = new javax.swing.JLabel();
        scrVisitReasonText = new javax.swing.JScrollPane();
        txtVisitReason = new javax.swing.JTextArea();
        pnlAppointmentFormActions = new javax.swing.JPanel();
        lblAppointmentError = new javax.swing.JLabel();
        pnlAppointmentFormButtons = new javax.swing.JPanel();
        btnBook = new javax.swing.JButton();
        btnClearAppointment = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 247, 250));
        setLayout(new java.awt.BorderLayout(0, 13));

        pnlAppointmentToolbar.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentToolbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 13, 8, 13));
        pnlAppointmentToolbar.setPreferredSize(new java.awt.Dimension(0, 84));
        pnlAppointmentToolbar.setLayout(new javax.swing.BoxLayout(pnlAppointmentToolbar, javax.swing.BoxLayout.X_AXIS));

        pnlAppointmentDateFilterGroup.setAlignmentY(1.0F);
        pnlAppointmentDateFilterGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentDateFilterGroup.setMinimumSize(new java.awt.Dimension(160, 68));
        pnlAppointmentDateFilterGroup.setPreferredSize(new java.awt.Dimension(160, 68));
        pnlAppointmentDateFilterGroup.setMaximumSize(new java.awt.Dimension(160, 68));
        pnlAppointmentDateFilterGroup.setLayout(new javax.swing.BoxLayout(pnlAppointmentDateFilterGroup, javax.swing.BoxLayout.Y_AXIS));

        lblAppointmentFillterDate.setText("Date");
        lblAppointmentFillterDate.setMaximumSize(new java.awt.Dimension(32767, 21));
        lblAppointmentFillterDate.setMinimumSize(new java.awt.Dimension(0, 21));
        lblAppointmentFillterDate.setPreferredSize(new java.awt.Dimension(144, 21));
        lblAppointmentFillterDate.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentFillterDate.setForeground(new java.awt.Color(71, 85, 105));
        pnlAppointmentDateFilterGroup.add(lblAppointmentFillterDate);
        pnlAppointmentDateFilterGroup.add(filler1);

        spnAppointmentFilterDate.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1789591740000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        spnAppointmentFilterDate.setMinimumSize(new java.awt.Dimension(0, 42));
        spnAppointmentFilterDate.setPreferredSize(new java.awt.Dimension(160, 42));
        spnAppointmentFilterDate.setMaximumSize(new java.awt.Dimension(32767, 42));
        spnAppointmentFilterDate.setAlignmentX(0.0F);
        spnAppointmentFilterDate.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        pnlAppointmentDateFilterGroup.add(spnAppointmentFilterDate);

        pnlAppointmentToolbar.add(pnlAppointmentDateFilterGroup);
        pnlAppointmentToolbar.add(filler4);

        pnlAppointmentStatusFilterGroup.setAlignmentY(1.0F);
        pnlAppointmentStatusFilterGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentStatusFilterGroup.setMinimumSize(new java.awt.Dimension(160, 68));
        pnlAppointmentStatusFilterGroup.setPreferredSize(new java.awt.Dimension(160, 68));
        pnlAppointmentStatusFilterGroup.setMaximumSize(new java.awt.Dimension(160, 68));
        pnlAppointmentStatusFilterGroup.setLayout(new javax.swing.BoxLayout(pnlAppointmentStatusFilterGroup, javax.swing.BoxLayout.Y_AXIS));

        lblStatusFilter.setText("Status");
        lblStatusFilter.setMaximumSize(new java.awt.Dimension(32767, 21));
        lblStatusFilter.setMinimumSize(new java.awt.Dimension(0, 21));
        lblStatusFilter.setPreferredSize(new java.awt.Dimension(144, 21));
        lblStatusFilter.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblStatusFilter.setForeground(new java.awt.Color(71, 85, 105));
        pnlAppointmentStatusFilterGroup.add(lblStatusFilter);
        pnlAppointmentStatusFilterGroup.add(filler3);

        cmbStatusFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All statuses", "Scheduled", "Checked-In", "Completed", "Cancelled", "No-Show" }));
        cmbStatusFilter.setMinimumSize(new java.awt.Dimension(0, 42));
        cmbStatusFilter.setPreferredSize(new java.awt.Dimension(160, 42));
        cmbStatusFilter.setMaximumSize(new java.awt.Dimension(32767, 42));
        cmbStatusFilter.setAlignmentX(0.0F);
        cmbStatusFilter.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        pnlAppointmentStatusFilterGroup.add(cmbStatusFilter);

        pnlAppointmentToolbar.add(pnlAppointmentStatusFilterGroup);
        pnlAppointmentToolbar.add(filler6);

        btnRefreshAppointments.setText("Refresh");
        btnRefreshAppointments.setAlignmentY(1.0F);
        btnRefreshAppointments.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnRefreshAppointments.setForeground(new java.awt.Color(15, 118, 110));
        btnRefreshAppointments.setMinimumSize(new java.awt.Dimension(89, 42));
        btnRefreshAppointments.setPreferredSize(new java.awt.Dimension(89, 42));
        btnRefreshAppointments.setMaximumSize(new java.awt.Dimension(89, 42));
        btnRefreshAppointments.addActionListener(this::btnRefreshAppointmentsActionPerformed);
        pnlAppointmentToolbar.add(btnRefreshAppointments);
        pnlAppointmentToolbar.add(filler7);
        pnlAppointmentToolbar.add(filler8);

        btnNewAppointment.setText("New appointment");
        btnNewAppointment.setAlignmentY(1.0F);
        btnNewAppointment.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnNewAppointment.setBackground(new java.awt.Color(15, 118, 110));
        btnNewAppointment.setForeground(new java.awt.Color(255, 255, 255));
        btnNewAppointment.setMinimumSize(new java.awt.Dimension(200, 42));
        btnNewAppointment.setPreferredSize(new java.awt.Dimension(200, 42));
        btnNewAppointment.setMaximumSize(new java.awt.Dimension(200, 42));
        btnNewAppointment.addActionListener(this::btnNewAppointmentActionPerformed);
        pnlAppointmentToolbar.add(btnNewAppointment);

        add(pnlAppointmentToolbar, java.awt.BorderLayout.NORTH);

        splitAppointments.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        splitAppointments.setResizeWeight(0.618);

        pnlAppointmentList.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentList.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlAppointmentList.setMinimumSize(new java.awt.Dimension(300, 100));
        pnlAppointmentList.setLayout(new java.awt.BorderLayout(0, 13));

        pnlAppointmentListHeader.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentListHeader.setLayout(new java.awt.BorderLayout(0, 5));

        lblAppointmentListTitle.setText("Daily schedule");
        lblAppointmentListTitle.setFont(new java.awt.Font("Segoe UI", 1, 21)); // NOI18N
        lblAppointmentListTitle.setForeground(new java.awt.Color(15, 23, 42));
        pnlAppointmentListHeader.add(lblAppointmentListTitle, java.awt.BorderLayout.NORTH);

        lblAppointmentVet.setText("Veterinarian: —");
        lblAppointmentVet.setMinimumSize(new java.awt.Dimension(0, 21));
        lblAppointmentVet.setPreferredSize(new java.awt.Dimension(233, 21));
        lblAppointmentVet.setMaximumSize(new java.awt.Dimension(32767, 21));
        lblAppointmentVet.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentVet.setForeground(new java.awt.Color(71, 85, 105));
        pnlAppointmentListHeader.add(lblAppointmentVet, java.awt.BorderLayout.CENTER);

        pnlAppointmentList.add(pnlAppointmentListHeader, java.awt.BorderLayout.NORTH);

        tblAppointments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Time", "Pet", "Owner", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblAppointments.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblAppointments.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblAppointments.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblAppointments.getTableHeader().setReorderingAllowed(false);
        tblAppointments.setFillsViewportHeight(true);
        tblAppointments.setAutoCreateRowSorter(true);
        tblAppointments.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tblAppointments.setRowHeight(34);
        tblAppointments.setForeground(new java.awt.Color(15, 23, 42));
        tblAppointments.setGridColor(new java.awt.Color(226, 232, 240));
        tblAppointments.setSelectionBackground(new java.awt.Color(204, 251, 241));
        tblAppointments.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tblAppointments.setShowHorizontalLines(true);
        scrAppointmentTable.setViewportView(tblAppointments);
        if (tblAppointments.getColumnModel().getColumnCount() > 0) {
            tblAppointments.getColumnModel().getColumn(0).setPreferredWidth(55);
            tblAppointments.getColumnModel().getColumn(1).setPreferredWidth(89);
            tblAppointments.getColumnModel().getColumn(2).setPreferredWidth(144);
            tblAppointments.getColumnModel().getColumn(3).setPreferredWidth(144);
            tblAppointments.getColumnModel().getColumn(4).setPreferredWidth(144);
        }
        tblAppointments.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        tblAppointments.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        tblAppointments.getTableHeader().setReorderingAllowed(false);
        scrAppointmentTable.setColumnHeaderView(tblAppointments.getTableHeader());
        scrAppointmentTable.getViewport().setBackground(java.awt.Color.WHITE);

        pnlAppointmentList.add(scrAppointmentTable, java.awt.BorderLayout.CENTER);

        pnlAppointmentFooter.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentFooter.setLayout(new java.awt.BorderLayout(0, 13));

        lblSelectedAppoinment.setText("Selected appointment: None");
        lblSelectedAppoinment.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSelectedAppoinment.setForeground(new java.awt.Color(71, 85, 105));
        pnlAppointmentFooter.add(lblSelectedAppoinment, java.awt.BorderLayout.NORTH);

        pnlAppointmentActions.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentActions.setMinimumSize(new java.awt.Dimension(0, 42));
        pnlAppointmentActions.setPreferredSize(new java.awt.Dimension(0, 42));
        pnlAppointmentActions.setAlignmentX(0.0F);
        pnlAppointmentActions.setLayout(new java.awt.GridLayout(1, 4, 8, 0));

        btnCheckIn.setText("Check in");
        btnCheckIn.setEnabled(false);
        btnCheckIn.setToolTipText("Check in the selected appointment");
        btnCheckIn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCheckIn.setBackground(new java.awt.Color(15, 118, 110));
        btnCheckIn.setForeground(new java.awt.Color(255, 255, 255));
        btnCheckIn.addActionListener(this::btnCheckInActionPerformed);
        pnlAppointmentActions.add(btnCheckIn);

        btnReschedule.setText("Reschedule");
        btnReschedule.setEnabled(false);
        btnReschedule.setToolTipText("Change the selected appointment date or time");
        btnReschedule.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnReschedule.setForeground(new java.awt.Color(15, 118, 110));
        btnReschedule.addActionListener(this::btnRescheduleActionPerformed);
        pnlAppointmentActions.add(btnReschedule);

        btnCancelAppointment.setEnabled(false);
        btnCancelAppointment.setText("Cancel visit");
        btnCancelAppointment.setToolTipText("Cancel the selected appointment");
        btnCancelAppointment.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnCancelAppointment.setForeground(new java.awt.Color(185, 28, 28));
        btnCancelAppointment.addActionListener(this::btnCancelAppointmentActionPerformed);
        pnlAppointmentActions.add(btnCancelAppointment);

        btnNoShow.setEnabled(false);
        btnNoShow.setText("No-show");
        btnNoShow.setToolTipText("Mark a past scheduled appointment as not attended");
        btnNoShow.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnNoShow.setForeground(new java.awt.Color(185, 28, 28));
        btnNoShow.addActionListener(this::btnNoShowActionPerformed);
        pnlAppointmentActions.add(btnNoShow);

        pnlAppointmentFooter.add(pnlAppointmentActions, java.awt.BorderLayout.CENTER);

        lblAppointmentCount.setText("Appointments not loaded.");
        lblAppointmentCount.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentCount.setForeground(new java.awt.Color(71, 85, 105));
        pnlAppointmentFooter.add(lblAppointmentCount, java.awt.BorderLayout.SOUTH);

        pnlAppointmentList.add(pnlAppointmentFooter, java.awt.BorderLayout.SOUTH);

        splitAppointments.setLeftComponent(pnlAppointmentList);

        pnlAppointmentEdditor.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentEdditor.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlAppointmentEdditor.setMinimumSize(new java.awt.Dimension(360, 0));
        pnlAppointmentEdditor.setLayout(new java.awt.BorderLayout(0, 8));

        pnlAppointmentFormHeader.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentFormHeader.setLayout(new java.awt.BorderLayout(0, 5));

        lblAppointmentFormTitle.setText("New appointment");
        lblAppointmentFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 21)); // NOI18N
        lblAppointmentFormTitle.setForeground(new java.awt.Color(15, 23, 42));
        pnlAppointmentFormHeader.add(lblAppointmentFormTitle, java.awt.BorderLayout.NORTH);

        pnlAppointmentMetaRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentMetaRow.setLayout(new java.awt.BorderLayout(8, 0));

        lblAppointmentId.setText("Appointment ID: Auto-generated");
        lblAppointmentId.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentId.setForeground(new java.awt.Color(71, 85, 105));
        pnlAppointmentMetaRow.add(lblAppointmentId, java.awt.BorderLayout.CENTER);

        lblAppointmentStatus.setText("Status: New");
        lblAppointmentStatus.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentStatus.setForeground(new java.awt.Color(71, 85, 105));
        pnlAppointmentMetaRow.add(lblAppointmentStatus, java.awt.BorderLayout.EAST);

        pnlAppointmentFormHeader.add(pnlAppointmentMetaRow, java.awt.BorderLayout.CENTER);

        pnlAppointmentEdditor.add(pnlAppointmentFormHeader, java.awt.BorderLayout.NORTH);

        pnlAppointmentEntry.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentEntry.setLayout(new java.awt.BorderLayout(0, 8));

        scrAppointmentForm.setBorder(null);
        scrAppointmentForm.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pnlAppointmentForm.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentForm.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlAppointmentForm.setLayout(new javax.swing.BoxLayout(pnlAppointmentForm, javax.swing.BoxLayout.Y_AXIS));

        pnlAppointmentPetGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentPetGroup.setMinimumSize(new java.awt.Dimension(0, 90));
        pnlAppointmentPetGroup.setPreferredSize(new java.awt.Dimension(233, 90));
        pnlAppointmentPetGroup.setMaximumSize(new java.awt.Dimension(32767, 90));
        pnlAppointmentPetGroup.setAlignmentX(0.0F);
        pnlAppointmentPetGroup.setLayout(new java.awt.BorderLayout(0, 5));

        pnlAppointmentPetInput.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentPetInput.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlAppointmentPetInput.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlAppointmentPetInput.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlAppointmentPetInput.setAlignmentX(0.0F);
        pnlAppointmentPetInput.setLayout(new java.awt.BorderLayout(0, 5));

        lblAppointmentPet.setText("Pet *");
        lblAppointmentPet.setPreferredSize(new java.awt.Dimension(0, 21));
        lblAppointmentPet.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentPet.setForeground(new java.awt.Color(71, 85, 105));
        pnlAppointmentPetInput.add(lblAppointmentPet, java.awt.BorderLayout.NORTH);

        cmbAppointmentPet.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Pet" }));
        cmbAppointmentPet.setMinimumSize(new java.awt.Dimension(0, 42));
        cmbAppointmentPet.setPreferredSize(new java.awt.Dimension(233, 42));
        cmbAppointmentPet.setMaximumSize(new java.awt.Dimension(32767, 42));
        cmbAppointmentPet.setAlignmentX(0.0F);
        cmbAppointmentPet.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        cmbAppointmentPet.addActionListener(this::cmbAppointmentPetActionPerformed);
        pnlAppointmentPetInput.add(cmbAppointmentPet, java.awt.BorderLayout.CENTER);

        pnlAppointmentPetGroup.add(pnlAppointmentPetInput, java.awt.BorderLayout.CENTER);

        lblAppointmentOwner.setText("Owner: —");
        lblAppointmentOwner.setMinimumSize(new java.awt.Dimension(0, 17));
        lblAppointmentOwner.setPreferredSize(new java.awt.Dimension(233, 17));
        lblAppointmentOwner.setMaximumSize(new java.awt.Dimension(32767, 17));
        lblAppointmentOwner.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentOwner.setForeground(new java.awt.Color(71, 85, 105));
        pnlAppointmentPetGroup.add(lblAppointmentOwner, java.awt.BorderLayout.SOUTH);

        pnlAppointmentForm.add(pnlAppointmentPetGroup);
        pnlAppointmentForm.add(filler15);

        pnlAppointmentScheduleGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentScheduleGroup.setAlignmentX(0.0F);
        pnlAppointmentScheduleGroup.setMaximumSize(new java.awt.Dimension(32767, 90));
        pnlAppointmentScheduleGroup.setMinimumSize(new java.awt.Dimension(0, 90));
        pnlAppointmentScheduleGroup.setPreferredSize(new java.awt.Dimension(233, 90));
        pnlAppointmentScheduleGroup.setLayout(new java.awt.BorderLayout(0, 5));

        pnlAppointmentDateTime.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentDateTime.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlAppointmentDateTime.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlAppointmentDateTime.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlAppointmentDateTime.setAlignmentX(0.0F);
        pnlAppointmentDateTime.setLayout(new java.awt.GridLayout(1, 2, 13, 0));

        pnlAppointmentDateGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentDateGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlAppointmentDateGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlAppointmentDateGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlAppointmentDateGroup.setAlignmentX(0.0F);
        pnlAppointmentDateGroup.setLayout(new java.awt.BorderLayout(0, 5));

        lblAppointmentDate.setText("Date *");
        lblAppointmentDate.setPreferredSize(new java.awt.Dimension(0, 21));
        lblAppointmentDate.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentDate.setForeground(new java.awt.Color(71, 85, 105));
        pnlAppointmentDateGroup.add(lblAppointmentDate, java.awt.BorderLayout.NORTH);

        spnAppointmentDate.setMinimumSize(new java.awt.Dimension(0, 42));
        spnAppointmentDate.setPreferredSize(new java.awt.Dimension(233, 42));
        spnAppointmentDate.setMaximumSize(new java.awt.Dimension(32767, 42));
        spnAppointmentDate.setAlignmentX(0.0F);
        spnAppointmentDate.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        pnlAppointmentDateGroup.add(spnAppointmentDate, java.awt.BorderLayout.CENTER);

        pnlAppointmentDateTime.add(pnlAppointmentDateGroup);

        pnlAppointmentTimeGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentTimeGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlAppointmentTimeGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlAppointmentTimeGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlAppointmentTimeGroup.setAlignmentX(0.0F);
        pnlAppointmentTimeGroup.setLayout(new java.awt.BorderLayout(0, 5));

        lblTimeSlot.setText("Time slot *");
        lblTimeSlot.setPreferredSize(new java.awt.Dimension(0, 21));
        lblTimeSlot.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTimeSlot.setForeground(new java.awt.Color(71, 85, 105));
        pnlAppointmentTimeGroup.add(lblTimeSlot, java.awt.BorderLayout.NORTH);

        cmbTimeSlot.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Time" }));
        cmbTimeSlot.setMinimumSize(new java.awt.Dimension(0, 42));
        cmbTimeSlot.setPreferredSize(new java.awt.Dimension(233, 42));
        cmbTimeSlot.setMaximumSize(new java.awt.Dimension(32767, 42));
        cmbTimeSlot.setAlignmentX(0.0F);
        cmbTimeSlot.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        pnlAppointmentTimeGroup.add(cmbTimeSlot, java.awt.BorderLayout.CENTER);

        pnlAppointmentDateTime.add(pnlAppointmentTimeGroup);

        pnlAppointmentScheduleGroup.add(pnlAppointmentDateTime, java.awt.BorderLayout.CENTER);

        lblAppointmentDuration.setText("Duration: 30 minutes");
        lblAppointmentDuration.setMinimumSize(new java.awt.Dimension(0, 17));
        lblAppointmentDuration.setPreferredSize(new java.awt.Dimension(233, 17));
        lblAppointmentDuration.setMaximumSize(new java.awt.Dimension(32767, 17));
        lblAppointmentDuration.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentDuration.setForeground(new java.awt.Color(71, 85, 105));
        pnlAppointmentScheduleGroup.add(lblAppointmentDuration, java.awt.BorderLayout.SOUTH);

        pnlAppointmentForm.add(pnlAppointmentScheduleGroup);
        pnlAppointmentForm.add(filler16);

        pnlAppointmentFormGlue.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentFormGlue.setAlignmentX(0.0F);
        pnlAppointmentFormGlue.setMinimumSize(new java.awt.Dimension(0, 0));
        pnlAppointmentFormGlue.setPreferredSize(new java.awt.Dimension(0, 0));
        pnlAppointmentFormGlue.setLayout(new javax.swing.BoxLayout(pnlAppointmentFormGlue, javax.swing.BoxLayout.Y_AXIS));
        pnlAppointmentForm.add(pnlAppointmentFormGlue);
        pnlAppointmentForm.add(filler17);

        pnlAppointmentReasonGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentReasonGroup.setAlignmentX(0.0F);
        pnlAppointmentReasonGroup.setMaximumSize(new java.awt.Dimension(32767, 94));
        pnlAppointmentReasonGroup.setMinimumSize(new java.awt.Dimension(0, 94));
        pnlAppointmentReasonGroup.setPreferredSize(new java.awt.Dimension(233, 94));
        pnlAppointmentReasonGroup.setLayout(new java.awt.BorderLayout(0, 5));

        lblVisitReason.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblVisitReason.setForeground(new java.awt.Color(71, 85, 105));
        lblVisitReason.setText("Reason for visit (optional)");
        lblVisitReason.setPreferredSize(new java.awt.Dimension(0, 21));
        pnlAppointmentReasonGroup.add(lblVisitReason, java.awt.BorderLayout.NORTH);

        scrVisitReasonText.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scrVisitReasonText.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrVisitReasonText.setAlignmentX(0.0F);
        scrVisitReasonText.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        scrVisitReasonText.setMaximumSize(new java.awt.Dimension(32767, 68));
        scrVisitReasonText.setMinimumSize(new java.awt.Dimension(0, 68));
        scrVisitReasonText.setPreferredSize(new java.awt.Dimension(233, 68));

        txtVisitReason.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtVisitReason.setLineWrap(true);
        txtVisitReason.setRows(2);
        txtVisitReason.setToolTipText("Briefly describe the reason for this visit.");
        txtVisitReason.setWrapStyleWord(true);
        txtVisitReason.setMargin(new java.awt.Insets(4, 8, 4, 8));
        scrVisitReasonText.setViewportView(txtVisitReason);

        pnlAppointmentReasonGroup.add(scrVisitReasonText, java.awt.BorderLayout.CENTER);

        pnlAppointmentForm.add(pnlAppointmentReasonGroup);

        scrAppointmentForm.setViewportView(pnlAppointmentForm);

        pnlAppointmentEntry.add(scrAppointmentForm, java.awt.BorderLayout.CENTER);

        pnlAppointmentEdditor.add(pnlAppointmentEntry, java.awt.BorderLayout.CENTER);

        pnlAppointmentFormActions.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentFormActions.setAlignmentX(0.0F);
        pnlAppointmentFormActions.setLayout(new java.awt.BorderLayout(0, 5));

        lblAppointmentError.setText(" ");
        lblAppointmentError.setMinimumSize(new java.awt.Dimension(0, 34));
        lblAppointmentError.setPreferredSize(new java.awt.Dimension(0, 34));
        lblAppointmentError.setMaximumSize(new java.awt.Dimension(32767, 34));
        lblAppointmentError.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentError.setForeground(new java.awt.Color(185, 28, 28));
        pnlAppointmentFormActions.add(lblAppointmentError, java.awt.BorderLayout.NORTH);

        pnlAppointmentFormButtons.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentFormButtons.setMinimumSize(new java.awt.Dimension(0, 42));
        pnlAppointmentFormButtons.setPreferredSize(new java.awt.Dimension(0, 42));
        pnlAppointmentFormButtons.setMaximumSize(new java.awt.Dimension(32767, 42));
        pnlAppointmentFormButtons.setAlignmentX(0.0F);
        pnlAppointmentFormButtons.setLayout(new java.awt.GridLayout(1, 2, 8, 0));

        btnBook.setText("Book");
        btnBook.setEnabled(false);
        btnBook.setMargin(new java.awt.Insets(8, 5, 8, 5));
        btnBook.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnBook.setBackground(new java.awt.Color(15, 118, 110));
        btnBook.setForeground(new java.awt.Color(255, 255, 255));
        btnBook.addActionListener(this::btnBookActionPerformed);
        pnlAppointmentFormButtons.add(btnBook);

        btnClearAppointment.setMargin(new java.awt.Insets(8, 5, 8, 5));
        btnClearAppointment.setText("Clear form");
        btnClearAppointment.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnClearAppointment.setForeground(new java.awt.Color(15, 118, 110));
        btnClearAppointment.addActionListener(this::btnClearAppointmentActionPerformed);
        pnlAppointmentFormButtons.add(btnClearAppointment);

        pnlAppointmentFormActions.add(pnlAppointmentFormButtons, java.awt.BorderLayout.CENTER);

        pnlAppointmentEdditor.add(pnlAppointmentFormActions, java.awt.BorderLayout.SOUTH);

        splitAppointments.setRightComponent(pnlAppointmentEdditor);

        add(splitAppointments, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void cmbAppointmentPetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAppointmentPetActionPerformed
        if(controller != null){
            controller.petSelected();
        }
    }//GEN-LAST:event_cmbAppointmentPetActionPerformed

    private void btnBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookActionPerformed
        if(controller != null){
            controller.bookAppointment();
        }
    }//GEN-LAST:event_btnBookActionPerformed

    private void btnClearAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearAppointmentActionPerformed
        if (controller != null){
            controller.newAppointment();
        }
    }//GEN-LAST:event_btnClearAppointmentActionPerformed

    private void btnNewAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewAppointmentActionPerformed
        if (controller != null){
            controller.newAppointment();
        }
    }//GEN-LAST:event_btnNewAppointmentActionPerformed

    private void btnRefreshAppointmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshAppointmentsActionPerformed
        if (controller != null){
            controller.loadAppointments();
        }
    }//GEN-LAST:event_btnRefreshAppointmentsActionPerformed

    private void btnCheckInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheckInActionPerformed
        if(controller != null){
            controller.checkInAppointment();
        }
    }//GEN-LAST:event_btnCheckInActionPerformed

    private void btnCancelAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelAppointmentActionPerformed
        if(controller != null){
            controller.cancelAppointment();
        }
    }//GEN-LAST:event_btnCancelAppointmentActionPerformed


    private void btnNoShowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNoShowActionPerformed
        if (controller != null) {
            controller.noShowAppointment();
        }
    }//GEN-LAST:event_btnNoShowActionPerformed

    private void btnRescheduleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRescheduleActionPerformed
        if (controller != null) {
            controller.rescheduleAppointment();
        }
    }//GEN-LAST:event_btnRescheduleActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBook;
    private javax.swing.JButton btnCancelAppointment;
    private javax.swing.JButton btnCheckIn;
    private javax.swing.JButton btnClearAppointment;
    private javax.swing.JButton btnNewAppointment;
    private javax.swing.JButton btnNoShow;
    private javax.swing.JButton btnRefreshAppointments;
    private javax.swing.JButton btnReschedule;
    private javax.swing.JComboBox<String> cmbAppointmentPet;
    private javax.swing.JComboBox<String> cmbStatusFilter;
    private javax.swing.JComboBox<String> cmbTimeSlot;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler15;
    private javax.swing.Box.Filler filler16;
    private javax.swing.Box.Filler filler17;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.JLabel lblAppointmentCount;
    private javax.swing.JLabel lblAppointmentDate;
    private javax.swing.JLabel lblAppointmentDuration;
    private javax.swing.JLabel lblAppointmentError;
    private javax.swing.JLabel lblAppointmentFillterDate;
    private javax.swing.JLabel lblAppointmentFormTitle;
    private javax.swing.JLabel lblAppointmentId;
    private javax.swing.JLabel lblAppointmentListTitle;
    private javax.swing.JLabel lblAppointmentOwner;
    private javax.swing.JLabel lblAppointmentPet;
    private javax.swing.JLabel lblAppointmentStatus;
    private javax.swing.JLabel lblAppointmentVet;
    private javax.swing.JLabel lblSelectedAppoinment;
    private javax.swing.JLabel lblStatusFilter;
    private javax.swing.JLabel lblTimeSlot;
    private javax.swing.JLabel lblVisitReason;
    private javax.swing.JPanel pnlAppointmentActions;
    private javax.swing.JPanel pnlAppointmentDateFilterGroup;
    private javax.swing.JPanel pnlAppointmentDateGroup;
    private javax.swing.JPanel pnlAppointmentDateTime;
    private javax.swing.JPanel pnlAppointmentEdditor;
    private javax.swing.JPanel pnlAppointmentEntry;
    private javax.swing.JPanel pnlAppointmentFooter;
    private javax.swing.JPanel pnlAppointmentForm;
    private javax.swing.JPanel pnlAppointmentFormActions;
    private javax.swing.JPanel pnlAppointmentFormButtons;
    private javax.swing.JPanel pnlAppointmentFormGlue;
    private javax.swing.JPanel pnlAppointmentFormHeader;
    private javax.swing.JPanel pnlAppointmentList;
    private javax.swing.JPanel pnlAppointmentListHeader;
    private javax.swing.JPanel pnlAppointmentMetaRow;
    private javax.swing.JPanel pnlAppointmentPetGroup;
    private javax.swing.JPanel pnlAppointmentPetInput;
    private javax.swing.JPanel pnlAppointmentReasonGroup;
    private javax.swing.JPanel pnlAppointmentScheduleGroup;
    private javax.swing.JPanel pnlAppointmentStatusFilterGroup;
    private javax.swing.JPanel pnlAppointmentTimeGroup;
    private javax.swing.JPanel pnlAppointmentToolbar;
    private javax.swing.JScrollPane scrAppointmentForm;
    private javax.swing.JScrollPane scrAppointmentTable;
    private javax.swing.JScrollPane scrVisitReasonText;
    private javax.swing.JSplitPane splitAppointments;
    private javax.swing.JSpinner spnAppointmentDate;
    private javax.swing.JSpinner spnAppointmentFilterDate;
    private javax.swing.JTable tblAppointments;
    private javax.swing.JTextArea txtVisitReason;
    // End of variables declaration//GEN-END:variables
    public void setAppointmentPermissions(boolean canManage){
        canManageAppointments = canManage;
        setAppointmentBusy(appointmentBusy);
    }

}
