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
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Date;
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

    private AppointmentController controller;
    private final List<Pet> appointmentPets = new ArrayList<>();
    private boolean clinicVetReady;
    private final List<Appointment> displayedAppointments = new ArrayList<>();
    /**
     * Creates new form PanelAppointment
     */
    public PanelAppointment() {
        initComponents();
        setupDateSpinner(spnAppointmentFilterDate, true);
        setupDateSpinner(spnAppointmentDate, false);
        
        setupAppointmentUI();
        
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
        cmbAppointmentPet.setEnabled(!busy && !appointmentPets.isEmpty());

        spnAppointmentDate.setEnabled(!busy);
        cmbTimeSlot.setEnabled(!busy);
        txtVisitReason.setEnabled(!busy);

        btnNewAppointment.setEnabled(!busy);
        btnClearAppointment.setEnabled(!busy);

        btnBook.setEnabled(!busy && clinicVetReady && !appointmentPets.isEmpty());

        btnBook.setText(busy ? "Please wait..." : "Book");
    }

    public void showAppointmentError(String message) {
        lblAppointmentError.setText(message);
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
        displayedAppointments.clear();

        DefaultTableModel tableModel = (DefaultTableModel) tblAppointments.getModel();

        tableModel.setRowCount(0);

        lblSelectedAppoinment.setText("Selected appointment: None");
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
                appointment.getVeterinarianName(),
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
    }
    
    private void setupAppointmentUI(){
        tblAppointments.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,13));
        scrAppointmentForm.getViewport().setBackground(Color.WHITE);
        scrAppointmentForm.getVerticalScrollBar().setUnitIncrement(21);
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
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        spnAppointmentFilterDate = new javax.swing.JSpinner();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0));
        pnlAppointmentStatusFilterGroup = new javax.swing.JPanel();
        lblStatusFilter = new javax.swing.JLabel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        cmbStatusFilter = new javax.swing.JComboBox<>();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0));
        btnRefreshAppointments = new javax.swing.JButton();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(21, 0), new java.awt.Dimension(21, 0), new java.awt.Dimension(21, 0));
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        btnNewAppointment = new javax.swing.JButton();
        splitAppointments = new javax.swing.JSplitPane();
        pnlAppointmentList = new javax.swing.JPanel();
        lblAppointmentListTitle = new javax.swing.JLabel();
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
        scrAppointmentForm = new javax.swing.JScrollPane();
        pnlAppointmentForm = new javax.swing.JPanel();
        lblAppointmentFormTitle = new javax.swing.JLabel();
        filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13));
        lblAppointmentId = new javax.swing.JLabel();
        filler10 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21));
        lblAppointmentPet = new javax.swing.JLabel();
        filler11 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        cmbAppointmentPet = new javax.swing.JComboBox<>();
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        lblAppointmentOwner = new javax.swing.JLabel();
        filler15 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21));
        lblAppointmentVet = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21));
        lblAppointmentDate = new javax.swing.JLabel();
        filler16 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        spnAppointmentDate = new javax.swing.JSpinner();
        filler17 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21));
        lblTimeSlot = new javax.swing.JLabel();
        filler18 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        cmbTimeSlot = new javax.swing.JComboBox<>();
        filler19 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        lblAppointmentDuration = new javax.swing.JLabel();
        filler20 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21), new java.awt.Dimension(0, 21));
        lblVisitReason = new javax.swing.JLabel();
        filler21 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        scrVisitReasonText = new javax.swing.JScrollPane();
        txtVisitReason = new javax.swing.JTextArea();
        filler22 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13));
        lblAppointmentStatus = new javax.swing.JLabel();
        pnlAppointmentFormActions = new javax.swing.JPanel();
        lblAppointmentError = new javax.swing.JLabel();
        pnlAppointmentFormButtons = new javax.swing.JPanel();
        btnBook = new javax.swing.JButton();
        btnClearAppointment = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 247, 250));
        setLayout(new java.awt.BorderLayout(0, 21));

        pnlAppointmentToolbar.setBackground(new java.awt.Color(245, 247, 250));
        pnlAppointmentToolbar.setPreferredSize(new java.awt.Dimension(0, 89));
        pnlAppointmentToolbar.setLayout(new javax.swing.BoxLayout(pnlAppointmentToolbar, javax.swing.BoxLayout.X_AXIS));

        pnlAppointmentDateFilterGroup.setBackground(new java.awt.Color(245, 247, 250));
        pnlAppointmentDateFilterGroup.setAlignmentY(1.0F);
        pnlAppointmentDateFilterGroup.setMaximumSize(new java.awt.Dimension(144, 84));
        pnlAppointmentDateFilterGroup.setMinimumSize(new java.awt.Dimension(144, 84));
        pnlAppointmentDateFilterGroup.setLayout(new javax.swing.BoxLayout(pnlAppointmentDateFilterGroup, javax.swing.BoxLayout.Y_AXIS));

        lblAppointmentFillterDate.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentFillterDate.setText("Date");
        lblAppointmentFillterDate.setMaximumSize(new java.awt.Dimension(32767, 21));
        lblAppointmentFillterDate.setMinimumSize(new java.awt.Dimension(0, 21));
        lblAppointmentFillterDate.setPreferredSize(new java.awt.Dimension(144, 21));
        pnlAppointmentDateFilterGroup.add(lblAppointmentFillterDate);
        pnlAppointmentDateFilterGroup.add(filler1);

        spnAppointmentFilterDate.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        spnAppointmentFilterDate.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1789591740000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        spnAppointmentFilterDate.setAlignmentX(0.0F);
        spnAppointmentFilterDate.setMaximumSize(new java.awt.Dimension(32767, 55));
        spnAppointmentFilterDate.setMinimumSize(new java.awt.Dimension(0, 55));
        spnAppointmentFilterDate.setPreferredSize(new java.awt.Dimension(144, 55));
        pnlAppointmentDateFilterGroup.add(spnAppointmentFilterDate);

        pnlAppointmentToolbar.add(pnlAppointmentDateFilterGroup);
        pnlAppointmentToolbar.add(filler4);

        pnlAppointmentStatusFilterGroup.setBackground(new java.awt.Color(245, 247, 250));
        pnlAppointmentStatusFilterGroup.setAlignmentY(1.0F);
        pnlAppointmentStatusFilterGroup.setMaximumSize(new java.awt.Dimension(144, 84));
        pnlAppointmentStatusFilterGroup.setMinimumSize(new java.awt.Dimension(144, 84));
        pnlAppointmentStatusFilterGroup.setPreferredSize(new java.awt.Dimension(144, 84));
        pnlAppointmentStatusFilterGroup.setLayout(new javax.swing.BoxLayout(pnlAppointmentStatusFilterGroup, javax.swing.BoxLayout.Y_AXIS));

        lblStatusFilter.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblStatusFilter.setText("Status");
        lblStatusFilter.setMaximumSize(new java.awt.Dimension(32767, 21));
        lblStatusFilter.setMinimumSize(new java.awt.Dimension(0, 21));
        lblStatusFilter.setPreferredSize(new java.awt.Dimension(144, 21));
        pnlAppointmentStatusFilterGroup.add(lblStatusFilter);
        pnlAppointmentStatusFilterGroup.add(filler3);

        cmbStatusFilter.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        cmbStatusFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All statuses", "Scheduled", "Checked-In", "Completed", "Cancelled", "No-Show" }));
        cmbStatusFilter.setAlignmentX(0.0F);
        cmbStatusFilter.setMaximumSize(new java.awt.Dimension(32767, 55));
        cmbStatusFilter.setMinimumSize(new java.awt.Dimension(0, 55));
        cmbStatusFilter.setPreferredSize(new java.awt.Dimension(144, 55));
        pnlAppointmentStatusFilterGroup.add(cmbStatusFilter);

        pnlAppointmentToolbar.add(pnlAppointmentStatusFilterGroup);
        pnlAppointmentToolbar.add(filler6);

        btnRefreshAppointments.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnRefreshAppointments.setText("Refresh");
        btnRefreshAppointments.setAlignmentY(1.0F);
        btnRefreshAppointments.setMaximumSize(new java.awt.Dimension(89, 55));
        btnRefreshAppointments.setMinimumSize(new java.awt.Dimension(89, 55));
        btnRefreshAppointments.setPreferredSize(new java.awt.Dimension(89, 55));
        btnRefreshAppointments.addActionListener(this::btnRefreshAppointmentsActionPerformed);
        pnlAppointmentToolbar.add(btnRefreshAppointments);
        pnlAppointmentToolbar.add(filler7);
        pnlAppointmentToolbar.add(filler8);

        btnNewAppointment.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnNewAppointment.setText("New appointment");
        btnNewAppointment.setAlignmentY(1.0F);
        btnNewAppointment.setMaximumSize(new java.awt.Dimension(233, 55));
        btnNewAppointment.setMinimumSize(new java.awt.Dimension(233, 55));
        btnNewAppointment.setPreferredSize(new java.awt.Dimension(233, 55));
        btnNewAppointment.addActionListener(this::btnNewAppointmentActionPerformed);
        pnlAppointmentToolbar.add(btnNewAppointment);

        add(pnlAppointmentToolbar, java.awt.BorderLayout.NORTH);

        splitAppointments.setDividerSize(7);
        splitAppointments.setResizeWeight(0.619);

        pnlAppointmentList.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentList.setBorder(javax.swing.BorderFactory.createEmptyBorder(21, 21, 21, 21));
        pnlAppointmentList.setMinimumSize(new java.awt.Dimension(300, 100));
        pnlAppointmentList.setLayout(new java.awt.BorderLayout(0, 13));

        lblAppointmentListTitle.setFont(new java.awt.Font("Segoe UI", 1, 21)); // NOI18N
        lblAppointmentListTitle.setText("Daily schedule");
        pnlAppointmentList.add(lblAppointmentListTitle, java.awt.BorderLayout.NORTH);

        tblAppointments.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tblAppointments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Time", "Pet", "Veterinarian", "Status"
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
        tblAppointments.setFillsViewportHeight(true);
        tblAppointments.setGridColor(new java.awt.Color(226, 232, 240));
        tblAppointments.setRowHeight(55);
        tblAppointments.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblAppointments.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblAppointments.setShowHorizontalLines(true);
        scrAppointmentTable.setViewportView(tblAppointments);
        if (tblAppointments.getColumnModel().getColumnCount() > 0) {
            tblAppointments.getColumnModel().getColumn(0).setPreferredWidth(55);
            tblAppointments.getColumnModel().getColumn(1).setPreferredWidth(89);
            tblAppointments.getColumnModel().getColumn(2).setPreferredWidth(144);
            tblAppointments.getColumnModel().getColumn(3).setPreferredWidth(144);
            tblAppointments.getColumnModel().getColumn(4).setPreferredWidth(144);
        }

        pnlAppointmentList.add(scrAppointmentTable, java.awt.BorderLayout.CENTER);

        pnlAppointmentFooter.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentFooter.setLayout(new java.awt.BorderLayout(0, 13));

        lblSelectedAppoinment.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSelectedAppoinment.setText("Selected appointment: None");
        pnlAppointmentFooter.add(lblSelectedAppoinment, java.awt.BorderLayout.NORTH);

        pnlAppointmentActions.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentActions.setPreferredSize(new java.awt.Dimension(0, 81));
        pnlAppointmentActions.setLayout(new java.awt.GridLayout(2, 2, 13, 13));

        btnCheckIn.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnCheckIn.setText("Check in");
        btnCheckIn.setEnabled(false);
        pnlAppointmentActions.add(btnCheckIn);

        btnReschedule.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnReschedule.setText("Reschedule");
        btnReschedule.setEnabled(false);
        pnlAppointmentActions.add(btnReschedule);

        btnCancelAppointment.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnCancelAppointment.setText("Cancel appointment");
        btnCancelAppointment.setEnabled(false);
        pnlAppointmentActions.add(btnCancelAppointment);

        btnNoShow.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnNoShow.setText("Mark no-show");
        btnNoShow.setEnabled(false);
        pnlAppointmentActions.add(btnNoShow);

        pnlAppointmentFooter.add(pnlAppointmentActions, java.awt.BorderLayout.CENTER);

        lblAppointmentCount.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentCount.setText("Appointments not loaded.");
        pnlAppointmentFooter.add(lblAppointmentCount, java.awt.BorderLayout.SOUTH);

        pnlAppointmentList.add(pnlAppointmentFooter, java.awt.BorderLayout.SOUTH);

        splitAppointments.setLeftComponent(pnlAppointmentList);

        pnlAppointmentEdditor.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentEdditor.setBorder(javax.swing.BorderFactory.createEmptyBorder(21, 21, 21, 21));
        pnlAppointmentEdditor.setMinimumSize(new java.awt.Dimension(320, 0));
        pnlAppointmentEdditor.setLayout(new java.awt.BorderLayout(0, 13));

        scrAppointmentForm.setBorder(null);
        scrAppointmentForm.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pnlAppointmentForm.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentForm.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlAppointmentForm.setLayout(new javax.swing.BoxLayout(pnlAppointmentForm, javax.swing.BoxLayout.Y_AXIS));

        lblAppointmentFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 21)); // NOI18N
        lblAppointmentFormTitle.setText("New appointment");
        pnlAppointmentForm.add(lblAppointmentFormTitle);
        pnlAppointmentForm.add(filler9);

        lblAppointmentId.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentId.setText("Appointment ID: Auto-generated");
        pnlAppointmentForm.add(lblAppointmentId);
        pnlAppointmentForm.add(filler10);

        lblAppointmentPet.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblAppointmentPet.setText("Pet *");
        pnlAppointmentForm.add(lblAppointmentPet);
        pnlAppointmentForm.add(filler11);

        cmbAppointmentPet.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        cmbAppointmentPet.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Pet" }));
        cmbAppointmentPet.setAlignmentX(0.0F);
        cmbAppointmentPet.setMaximumSize(new java.awt.Dimension(32767, 55));
        cmbAppointmentPet.setMinimumSize(new java.awt.Dimension(0, 55));
        cmbAppointmentPet.setPreferredSize(new java.awt.Dimension(233, 55));
        cmbAppointmentPet.addActionListener(this::cmbAppointmentPetActionPerformed);
        pnlAppointmentForm.add(cmbAppointmentPet);
        pnlAppointmentForm.add(filler12);

        lblAppointmentOwner.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentOwner.setText("Owner: —");
        pnlAppointmentForm.add(lblAppointmentOwner);
        pnlAppointmentForm.add(filler15);

        lblAppointmentVet.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblAppointmentVet.setText("Veterinarian: —");
        pnlAppointmentForm.add(lblAppointmentVet);
        pnlAppointmentForm.add(filler2);

        lblAppointmentDate.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblAppointmentDate.setText("Appointment date *");
        pnlAppointmentForm.add(lblAppointmentDate);
        pnlAppointmentForm.add(filler16);

        spnAppointmentDate.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        spnAppointmentDate.setAlignmentX(0.0F);
        spnAppointmentDate.setMaximumSize(new java.awt.Dimension(32767, 55));
        spnAppointmentDate.setMinimumSize(new java.awt.Dimension(0, 55));
        spnAppointmentDate.setPreferredSize(new java.awt.Dimension(233, 55));
        pnlAppointmentForm.add(spnAppointmentDate);
        pnlAppointmentForm.add(filler17);

        lblTimeSlot.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblTimeSlot.setText("Time slot *");
        pnlAppointmentForm.add(lblTimeSlot);
        pnlAppointmentForm.add(filler18);

        cmbTimeSlot.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        cmbTimeSlot.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Time" }));
        cmbTimeSlot.setAlignmentX(0.0F);
        cmbTimeSlot.setMaximumSize(new java.awt.Dimension(32767, 55));
        cmbTimeSlot.setMinimumSize(new java.awt.Dimension(0, 55));
        cmbTimeSlot.setPreferredSize(new java.awt.Dimension(233, 55));
        pnlAppointmentForm.add(cmbTimeSlot);
        pnlAppointmentForm.add(filler19);

        lblAppointmentDuration.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentDuration.setText("Duration: 30 minutes");
        pnlAppointmentForm.add(lblAppointmentDuration);
        pnlAppointmentForm.add(filler20);

        lblVisitReason.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblVisitReason.setText("Reason for visit (optional)");
        pnlAppointmentForm.add(lblVisitReason);
        pnlAppointmentForm.add(filler21);

        scrVisitReasonText.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scrVisitReasonText.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrVisitReasonText.setAlignmentX(0.0F);
        scrVisitReasonText.setMaximumSize(new java.awt.Dimension(32767, 89));
        scrVisitReasonText.setMinimumSize(new java.awt.Dimension(0, 89));
        scrVisitReasonText.setPreferredSize(new java.awt.Dimension(233, 89));

        txtVisitReason.setColumns(20);
        txtVisitReason.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtVisitReason.setLineWrap(true);
        txtVisitReason.setRows(3);
        txtVisitReason.setWrapStyleWord(true);
        scrVisitReasonText.setViewportView(txtVisitReason);

        pnlAppointmentForm.add(scrVisitReasonText);
        pnlAppointmentForm.add(filler22);

        lblAppointmentStatus.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentStatus.setText("Status: New");
        pnlAppointmentForm.add(lblAppointmentStatus);

        scrAppointmentForm.setViewportView(pnlAppointmentForm);

        pnlAppointmentEdditor.add(scrAppointmentForm, java.awt.BorderLayout.CENTER);

        pnlAppointmentFormActions.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentFormActions.setAlignmentX(0.0F);
        pnlAppointmentFormActions.setLayout(new java.awt.BorderLayout(0, 8));

        lblAppointmentError.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAppointmentError.setForeground(new java.awt.Color(185, 28, 28));
        lblAppointmentError.setText(" ");
        lblAppointmentError.setPreferredSize(new java.awt.Dimension(0, 34));
        pnlAppointmentFormActions.add(lblAppointmentError, java.awt.BorderLayout.NORTH);

        pnlAppointmentFormButtons.setBackground(new java.awt.Color(255, 255, 255));
        pnlAppointmentFormButtons.setPreferredSize(new java.awt.Dimension(0, 55));
        pnlAppointmentFormButtons.setLayout(new java.awt.GridLayout(1, 2, 8, 0));

        btnBook.setBackground(new java.awt.Color(15, 118, 110));
        btnBook.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBook.setForeground(new java.awt.Color(255, 255, 255));
        btnBook.setText("Book");
        btnBook.setEnabled(false);
        btnBook.setMargin(new java.awt.Insets(8, 5, 8, 5));
        btnBook.addActionListener(this::btnBookActionPerformed);
        pnlAppointmentFormButtons.add(btnBook);

        btnClearAppointment.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnClearAppointment.setForeground(new java.awt.Color(71, 85, 105));
        btnClearAppointment.setText("Clear");
        btnClearAppointment.setMargin(new java.awt.Insets(8, 5, 8, 5));
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
    private javax.swing.Box.Filler filler10;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler15;
    private javax.swing.Box.Filler filler16;
    private javax.swing.Box.Filler filler17;
    private javax.swing.Box.Filler filler18;
    private javax.swing.Box.Filler filler19;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler20;
    private javax.swing.Box.Filler filler21;
    private javax.swing.Box.Filler filler22;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.Box.Filler filler9;
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
    private javax.swing.JPanel pnlAppointmentEdditor;
    private javax.swing.JPanel pnlAppointmentFooter;
    private javax.swing.JPanel pnlAppointmentForm;
    private javax.swing.JPanel pnlAppointmentFormActions;
    private javax.swing.JPanel pnlAppointmentFormButtons;
    private javax.swing.JPanel pnlAppointmentList;
    private javax.swing.JPanel pnlAppointmentStatusFilterGroup;
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
}
