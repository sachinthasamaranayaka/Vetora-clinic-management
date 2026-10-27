/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package vetora.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JSpinner;
import javax.swing.table.DefaultTableModel;
import vetora.model.Pet;
import vetora.model.Owner;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.JOptionPane;
import vetora.controller.PetController;

/**
 *
 * @author ASUS
 */
public class PanelPet extends javax.swing.JPanel {
    
    private List<Owner> ownerOptions = new ArrayList<>();
    private PetController controller;
    private boolean ownerOptionsLoading;
    private boolean savingPet;
    private Long editingPetId;
    private boolean petListBusy;
    private boolean restoringPetSelection;
    private List<Object> savedPetForm;
    private List<Pet> displayedPets = new ArrayList<>();
    private boolean splitReady;

    /**
     * Creates new form PanelPet
     */
    public PanelPet() {
        initComponents();
        setupPetUI();
        clearPetForm();
        tblPets.getSelectionModel().addListSelectionListener(e -> {if (!e.getValueIsAdjusting() && controller != null){controller.selectPet();}});
    }
    
    public String getPetName(){
        return txtPetName.getText();
    }
    public String getSpecies(){
        return (String)cmbSpecies.getSelectedItem();
    }
    public String getGender(){
        return (String)cmbGender.getSelectedItem();
    }
    public String getBreed(){
        return txtBreed.getText();
    }
    public boolean getBirthdayUnknown(){
        return chkDobUnknown.isSelected();
    }
    public LocalDate getBirthDay() throws ParseException{
        if(getBirthdayUnknown()){
            return null;
        }
        
        spnPetDob.commitEdit();
        Date value = (Date)spnPetDob.getValue();
        
        return value.toInstant().atZone(ZoneId.of("Asia/Colombo")).toLocalDate();
    }
    public Long getSelectedOwnerId() {
        int index = cmbPetOwner.getSelectedIndex();
        
        if (index <= 0 || index > ownerOptions.size()){
            return null;
        }
        
        return ownerOptions.get( index -1).getOwnerId();
    }
    public Long getEditingPetId(){
        return editingPetId;
    }
    public Pet getSelectedPet(){
        int row = tblPets.getSelectedRow();
        
        if (row <0){
            return null;
        }
        
        int modelRow = tblPets.convertRowIndexToModel(row);
        return modelRow < displayedPets.size() ? displayedPets.get(modelRow) : null;
    }
    
    public void setController(PetController controller){
        this.controller = controller;
    }
    
    public void displayOwnerOptions(List<Owner> owners){
        Long previousOwnerId = getSelectedOwnerId();
        
        ownerOptions = new ArrayList<>(owners);
        
        cmbPetOwner.removeAllItems();
        cmbPetOwner.addItem("Select owner");
        
        int selectedIndex = 0;
        
        for (int i = 0; i < ownerOptions.size(); i++){
            Owner owner = ownerOptions.get(i);
            
            cmbPetOwner.addItem(owner.getOwnerId() + " - " + owner.getFullName() + "(" + owner.getPhone() + ")");
            
            if (previousOwnerId != null && owner.getOwnerId() == previousOwnerId){
                selectedIndex = i +1;
            }
        }
        
        cmbPetOwner.setSelectedIndex(selectedIndex);
    }
    
    public void setOwnerOptionsLoading(boolean loading){
        ownerOptionsLoading = loading;
        updatePetFormState();
    }
    public void setSavingPet(boolean saving){
        savingPet = saving;
        updatePetFormState();
    }
    public void updatePetFormState(){
        boolean editable = !savingPet;
        boolean ownerReady = !ownerOptionsLoading && !ownerOptions.isEmpty();

        cmbPetOwner.setEnabled(editable && ownerReady);
        btnSavePet.setEnabled(editable && ownerReady);
        btnSavePet.setText(savingPet ? "Saving..." : editingPetId == null ? "Save" : "Update");

        txtPetName.setEditable(editable);
        txtBreed.setEditable(editable);
        cmbSpecies.setEnabled(editable);
        cmbGender.setEnabled(editable);

        chkDobUnknown.setEnabled(editable);
        spnPetDob.setEnabled(editable && !chkDobUnknown.isSelected());

        btnNewPet.setEnabled(editable);
        btnCancelPet.setEnabled(editable);
        tblPets.setEnabled(editable && ownerReady && !petListBusy);
        btnDeletePet.setEnabled(editable && !ownerOptionsLoading && editingPetId != null);
        btnSearchPet.setEnabled(editable && !petListBusy);
        txtPetSearch.setEnabled(editable && !petListBusy);
        cmbSpeciesFilter.setEnabled(editable && !petListBusy);
    }
    
    public void showPetError(String message){
        lblPetError.setText("<html>" + message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</html>");
        lblPetError.setToolTipText(message.trim().isEmpty() ? null : message);
    }
    
    private void setupPetUI(){
        tblPets.getTableHeader().setReorderingAllowed(false);
        tblPets.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        tblPets.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrPetTable.getViewport().setBackground(java.awt.Color.WHITE);
        scrPetTable.setColumnHeaderView(tblPets.getTableHeader());
        scrPetTable.getVerticalScrollBar().setUnitIncrement(21);

        tblPets.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,13));
        scrPetForm.getViewport().setBackground(Color.WHITE);
        scrPetForm.getVerticalScrollBar().setPreferredSize(new Dimension(10,0));
        scrPetTable.getVerticalScrollBar().setPreferredSize(new Dimension(8,0));
        
        scrPetForm.getVerticalScrollBar().setUnitIncrement(21);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spnPetDob,"dd/MM/yyyy");
        dateEditor.getFormat().setLenient(false);
        dateEditor.getFormat().setTimeZone(java.util.TimeZone.getTimeZone("Asia/Colombo"));
        spnPetDob.setEditor(dateEditor);
    }
    
    
    public void displayPets(List<Pet> pets){
        tblPets.clearSelection();
        tblPets.getSelectionModel().setAnchorSelectionIndex(-1);
        tblPets.getSelectionModel().setLeadSelectionIndex(-1);
        displayedPets = new ArrayList<>(pets);
        DefaultTableModel tableModel = (DefaultTableModel)tblPets.getModel();
        
        tableModel.setRowCount(0);
        
        for(Pet pet : pets){
            Integer age = pet.getAge();
            String ageText;
            
            if (age == null){
                ageText = "Unknown";
            }
            else if (age == 0){
                ageText = "Under 1 year";
            }
            else {
                ageText = age + (age == 1 ? " year" : " years");
            }
            
            tableModel.addRow(new Object[]{
                pet.getPetID(),
                pet.getPetName(),
                pet.getOwnerName(),
                pet.getSpecies(),
                ageText
            });
        }
        
        lblPetCount.setText(pets.isEmpty() ? "No pets match the search and species filter." : "Pets: " + pets.size());
    }
    
    public void showPetListMessage(String message){
        lblPetCount.setText(message);
    }
    
   public void showPetSaved() {
        JOptionPane.showMessageDialog(this, "Pet saved successfully.", "Pet registration", JOptionPane.INFORMATION_MESSAGE);
    }

    
    public void clearPetForm() {
        editingPetId = null;
        cmbPetOwner.setSelectedIndex(0);
        txtPetName.setText("");
        cmbSpecies.setSelectedIndex(0);
        cmbGender.setSelectedIndex(0);
        txtBreed.setText("");

        chkDobUnknown.setSelected(true);
        spnPetDob.setValue(new Date());

        lblPetAge.setText("Age: Unknown");
        lblPetId.setText("Pet ID: Auto-generated");
        lblPetFormTitle.setText("Add pet");
        lblPetError.setText(" ");

        tblPets.clearSelection();
        tblPets.getSelectionModel().setAnchorSelectionIndex(-1);
        tblPets.getSelectionModel().setLeadSelectionIndex(-1);
        btnDeletePet.setEnabled(false);

        updatePetFormState();
        savedPetForm = getPetFormValues();
        txtPetName.requestFocusInWindow();
    }
    
    public void showPetDetails(Pet pet){
        editingPetId = pet.getPetID();
        txtPetName.setText(pet.getPetName());
        cmbSpecies.setSelectedItem(pet.getSpecies());
        cmbGender.setSelectedItem(pet.getGender());
        txtBreed.setText(pet.getBreed() == null ? "" : pet.getBreed());
        cmbPetOwner.setSelectedIndex(0);

        for (int i = 0; i < ownerOptions.size(); i++) {
            if (ownerOptions.get(i).getOwnerId() == pet.getOwnerID()) {
                cmbPetOwner.setSelectedIndex(i + 1);
                break;
            }
        }

        Date dob = pet.getDob();
        chkDobUnknown.setSelected(dob == null);
        spnPetDob.setValue(dob == null ? new Date() : Date.from(pet.getDob().toLocalDate()
                .atStartOfDay(ZoneId.of("Asia/Colombo")).toInstant()));
        
        Integer age = pet.getAge();
        String ageText;

        if (age == null) {
            ageText = "Unknown";
        } 
        else if (age == 0) {
            ageText = "Under 1 year";
        }
        else {
            ageText = age + (age == 1 ? " year" : " years");
        }

        lblPetAge.setText("Age: " + ageText);
        lblPetId.setText("Pet ID: " + pet.getPetID());
        lblPetFormTitle.setText("Edit pet");
        lblPetError.setText(" ");

        updatePetFormState();
        savedPetForm = getPetFormValues();
    }
    public String getPetSearch(){
        return txtPetSearch.getText();
    }

    public String getSpeciesFilter(){
        return cmbSpeciesFilter.getSelectedIndex() <= 0 ? null : (String)cmbSpeciesFilter.getSelectedItem();
    }

    public boolean isOwnerOptionsLoading(){
        return ownerOptionsLoading;
    }

    public void setPetListBusy(boolean busy){
        petListBusy = busy;
        updatePetFormState();
    }

    public boolean isRestoringPetSelection(){
        return restoringPetSelection;
    }

    public void restorePetSelection(){
        restoringPetSelection = true;
        try {
            tblPets.clearSelection();
        tblPets.getSelectionModel().setAnchorSelectionIndex(-1);
        tblPets.getSelectionModel().setLeadSelectionIndex(-1);
            for (int i = 0; i < displayedPets.size(); i++) {
                if (Objects.equals(editingPetId, displayedPets.get(i).getPetID())) {
                    int row = tblPets.convertRowIndexToView(i);
                    if (row >= 0) {
                        tblPets.setRowSelectionInterval(row, row);
                    }
                    break;
                }
            }
        }
        finally {
            restoringPetSelection = false;
        }
    }

    private List<Object> getPetFormValues(){
        String dateText = ((JSpinner.DefaultEditor)spnPetDob.getEditor()).getTextField().getText();
        return Arrays.asList(getSelectedOwnerId(), txtPetName.getText(), getSpecies(), getGender(),
                txtBreed.getText(), chkDobUnknown.isSelected(), chkDobUnknown.isSelected() ? "" : dateText);
    }

    public boolean confirmDiscardPetChanges(){
        return savedPetForm == null || savedPetForm.equals(getPetFormValues())
                || JOptionPane.showConfirmDialog(this, "Discard the unsaved pet details?", "Unsaved changes",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public boolean confirmDeletePet(){
        return JOptionPane.showConfirmDialog(this,
                "Delete pet #" + editingPetId + "?\nPets with appointment history cannot be deleted.",
                "Delete pet", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    @Override
    public void doLayout(){
        super.doLayout();
        if (!splitReady && splitPets.getWidth() > 0) {
            splitReady = true;
            int availableWidth = splitPets.getWidth() - splitPets.getDividerSize();
            int formWidth = Math.max(360, (int)Math.round(availableWidth * 0.382));
            splitPets.setDividerLocation(Math.max(300, availableWidth - formWidth));
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
        pnlPetFormGlue = new javax.swing.JPanel();
        pnlPetBirthRow = new javax.swing.JPanel();
        pnlPetAgeGroup = new javax.swing.JPanel();
        pnlPetDobGroup = new javax.swing.JPanel();
        pnlPetIdentityRow = new javax.swing.JPanel();
        pnlPetBreedGroup = new javax.swing.JPanel();
        pnlPetNameGroup = new javax.swing.JPanel();
        pnlPetOwnerGroup = new javax.swing.JPanel();
        pnlPetFormHeader = new javax.swing.JPanel();

        pnlPetToolbar = new javax.swing.JPanel();
        pnlPetSearchGroup = new javax.swing.JPanel();
        lblPetSearch = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        txtPetSearch = new javax.swing.JTextField();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0));
        pnlPetFilterGroup = new javax.swing.JPanel();
        lblSpeciesFilter = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5));
        cmbSpeciesFilter = new javax.swing.JComboBox<>();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0));
        btnSearchPet = new javax.swing.JButton();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0));
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        btnNewPet = new javax.swing.JButton();
        splitPets = new javax.swing.JSplitPane();
        pnlPetList = new javax.swing.JPanel();
        lblPetListTitle = new javax.swing.JLabel();
        scrPetTable = new javax.swing.JScrollPane();
        tblPets = new javax.swing.JTable();
        lblPetCount = new javax.swing.JLabel();
        pnlPetEditor = new javax.swing.JPanel();
        scrPetForm = new javax.swing.JScrollPane();
        pnlPetForm = new javax.swing.JPanel();
        lblPetFormTitle = new javax.swing.JLabel();
        lblPetId = new javax.swing.JLabel();
        lblPetOwner = new javax.swing.JLabel();
        cmbPetOwner = new javax.swing.JComboBox<>();
        filler10 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        lblPetName = new javax.swing.JLabel();
        txtPetName = new javax.swing.JTextField();
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        pnlPetSpeciesGender = new javax.swing.JPanel();
        pnlPetSpeciesGroup = new javax.swing.JPanel();
        lblSpecies = new javax.swing.JLabel();
        cmbSpecies = new javax.swing.JComboBox<>();
        pnlPetGenderGroup = new javax.swing.JPanel();
        lblGender = new javax.swing.JLabel();
        cmbGender = new javax.swing.JComboBox<>();
        filler13 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        lblBreed = new javax.swing.JLabel();
        txtBreed = new javax.swing.JTextField();
        lblPetDob = new javax.swing.JLabel();
        spnPetDob = new javax.swing.JSpinner();
        chkDobUnknown = new javax.swing.JCheckBox();
        lblPetAge = new javax.swing.JLabel();
        pnlPetActions = new javax.swing.JPanel();
        lblPetError = new javax.swing.JLabel();
        pnlPetButtons = new javax.swing.JPanel();
        btnSavePet = new javax.swing.JButton();
        btnCancelPet = new javax.swing.JButton();
        btnDeletePet = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 247, 250));
        setLayout(new java.awt.BorderLayout(0, 13));

        pnlPetToolbar.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetToolbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 13, 8, 13));
        pnlPetToolbar.setPreferredSize(new java.awt.Dimension(0, 84));
        pnlPetToolbar.setLayout(new javax.swing.BoxLayout(pnlPetToolbar, javax.swing.BoxLayout.X_AXIS));

        pnlPetSearchGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetSearchGroup.setAlignmentY(1.0F);
        pnlPetSearchGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPetSearchGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPetSearchGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPetSearchGroup.setLayout(new javax.swing.BoxLayout(pnlPetSearchGroup, javax.swing.BoxLayout.Y_AXIS));
        pnlPetSearchGroup.setAlignmentX(0.0F);

        lblPetSearch.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPetSearch.setText("Search by ID, pet or owner");
        lblPetSearch.setMaximumSize(new java.awt.Dimension(32767, 21));
        lblPetSearch.setMinimumSize(new java.awt.Dimension(0, 21));
        lblPetSearch.setPreferredSize(new java.awt.Dimension(233, 21));
        lblPetSearch.setForeground(new java.awt.Color(71, 85, 105));

        txtPetSearch.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtPetSearch.addActionListener(this::txtPetSearchActionPerformed);
        txtPetSearch.setAlignmentX(0.0F);
        txtPetSearch.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtPetSearch.setMinimumSize(new java.awt.Dimension(0, 42));
        txtPetSearch.setPreferredSize(new java.awt.Dimension(233, 42));
        txtPetSearch.setToolTipText("Search by pet ID, pet name or owner name");


        pnlPetFilterGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetFilterGroup.setAlignmentY(1.0F);
        pnlPetFilterGroup.setMaximumSize(new java.awt.Dimension(160, 68));
        pnlPetFilterGroup.setMinimumSize(new java.awt.Dimension(160, 68));
        pnlPetFilterGroup.setPreferredSize(new java.awt.Dimension(160, 68));
        pnlPetFilterGroup.setLayout(new javax.swing.BoxLayout(pnlPetFilterGroup, javax.swing.BoxLayout.Y_AXIS));

        lblSpeciesFilter.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblSpeciesFilter.setText("Species");
        lblSpeciesFilter.setMaximumSize(new java.awt.Dimension(32767, 21));
        lblSpeciesFilter.setMinimumSize(new java.awt.Dimension(0, 21));
        lblSpeciesFilter.setPreferredSize(new java.awt.Dimension(144, 21));
        lblSpeciesFilter.setForeground(new java.awt.Color(71, 85, 105));

        cmbSpeciesFilter.setFont(new java.awt.Font("Segoe UI", 0, 16));
        cmbSpeciesFilter.addActionListener(this::cmbSpeciesFilterActionPerformed);
        cmbSpeciesFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All species", "Dog", "Cat", "Bird", "Other" }));
        cmbSpeciesFilter.setAlignmentX(0.0F);
        cmbSpeciesFilter.setMaximumSize(new java.awt.Dimension(32767, 42));
        cmbSpeciesFilter.setMinimumSize(new java.awt.Dimension(0, 42));
        cmbSpeciesFilter.setPreferredSize(new java.awt.Dimension(160, 42));


        btnSearchPet.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnSearchPet.addActionListener(this::btnSearchPetActionPerformed);
        btnSearchPet.setText("Search");
        btnSearchPet.setAlignmentY(1.0F);
        btnSearchPet.setMaximumSize(new java.awt.Dimension(89, 42));
        btnSearchPet.setMinimumSize(new java.awt.Dimension(89, 42));
        btnSearchPet.setPreferredSize(new java.awt.Dimension(89, 42));
        btnSearchPet.setBackground(new java.awt.Color(255, 255, 255));
        btnSearchPet.setForeground(new java.awt.Color(15, 118, 110));

        btnNewPet.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnNewPet.setText("New pet");
        btnNewPet.setAlignmentY(1.0F);
        btnNewPet.setMaximumSize(new java.awt.Dimension(144, 42));
        btnNewPet.setMinimumSize(new java.awt.Dimension(144, 42));
        btnNewPet.setPreferredSize(new java.awt.Dimension(144, 42));
        btnNewPet.setBackground(new java.awt.Color(15, 118, 110));
        btnNewPet.setForeground(new java.awt.Color(255, 255, 255));
        btnNewPet.addActionListener(this::btnNewPetActionPerformed);


        splitPets.setDividerSize(5);
        splitPets.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        splitPets.setResizeWeight(0.618);

        pnlPetList.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetList.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlPetList.setMinimumSize(new java.awt.Dimension(300, 100));
        pnlPetList.setLayout(new java.awt.BorderLayout(0, 13));

        lblPetListTitle.setFont(new java.awt.Font("Segoe UI", 1, 21));
        lblPetListTitle.setText("Registered pets");
        lblPetListTitle.setAutoscrolls(true);
        lblPetListTitle.setForeground(new java.awt.Color(15, 23, 42));

        tblPets.setFont(new java.awt.Font("Segoe UI", 0, 16));
        tblPets.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Pet ID", "Pet Name", "Owner", "Species", "Age"
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
        tblPets.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblPets.setFillsViewportHeight(true);
        tblPets.setGridColor(new java.awt.Color(226, 232, 240));
        tblPets.setMaximumSize(new java.awt.Dimension(32767, 42));
        tblPets.setMinimumSize(new java.awt.Dimension(0, 42));
        tblPets.setPreferredSize(new java.awt.Dimension(233, 42));
        tblPets.setRowHeight(34);
        tblPets.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblPets.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblPets.setShowHorizontalLines(true);
        tblPets.setShowVerticalLines(false);
        tblPets.setColumnSelectionAllowed(false);
        tblPets.setRowSelectionAllowed(true);
        tblPets.setAutoCreateRowSorter(true);
        tblPets.setBackground(new java.awt.Color(255, 255, 255));
        tblPets.setForeground(new java.awt.Color(15, 23, 42));
        tblPets.setSelectionBackground(new java.awt.Color(204, 251, 241));
        tblPets.setSelectionForeground(new java.awt.Color(15, 23, 42));
        if (tblPets.getColumnModel().getColumnCount() > 0) {
            tblPets.getColumnModel().getColumn(0).setPreferredWidth(55);
            tblPets.getColumnModel().getColumn(1).setPreferredWidth(144);
            tblPets.getColumnModel().getColumn(2).setPreferredWidth(233);
            tblPets.getColumnModel().getColumn(3).setPreferredWidth(89);
            tblPets.getColumnModel().getColumn(4).setPreferredWidth(89);
        }


        lblPetCount.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPetCount.setText("Pets not loaded.");
        lblPetCount.setForeground(new java.awt.Color(71, 85, 105));


        pnlPetEditor.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetEditor.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlPetEditor.setMinimumSize(new java.awt.Dimension(360, 0));
        pnlPetEditor.setLayout(new java.awt.BorderLayout(0, 8));

        scrPetForm.setBorder(null);
        scrPetForm.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pnlPetForm.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetForm.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlPetForm.setLayout(new javax.swing.BoxLayout(pnlPetForm, javax.swing.BoxLayout.Y_AXIS));

        lblPetFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 21));
        lblPetFormTitle.setText("Add pet");
        lblPetFormTitle.setForeground(new java.awt.Color(15, 23, 42));

        lblPetId.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPetId.setForeground(new java.awt.Color(71, 85, 105));
        lblPetId.setText("Pet ID: Auto-generated");
        lblPetId.setMinimumSize(new java.awt.Dimension(0, 21));
        lblPetId.setPreferredSize(new java.awt.Dimension(233, 21));
        lblPetId.setMaximumSize(new java.awt.Dimension(32767, 21));
        lblPetId.setAlignmentX(0.0F);

        lblPetOwner.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPetOwner.setText("Owner *");
        lblPetOwner.setPreferredSize(new java.awt.Dimension(0, 21));
        lblPetOwner.setForeground(new java.awt.Color(71, 85, 105));

        cmbPetOwner.setFont(new java.awt.Font("Segoe UI", 0, 16));
        cmbPetOwner.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select owner" }));
        cmbPetOwner.setAlignmentX(0.0F);
        cmbPetOwner.setMaximumSize(new java.awt.Dimension(32767, 42));
        cmbPetOwner.setMinimumSize(new java.awt.Dimension(0, 42));
        cmbPetOwner.setPreferredSize(new java.awt.Dimension(233, 42));

        lblPetName.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPetName.setText("Pet name *");
        lblPetName.setPreferredSize(new java.awt.Dimension(0, 21));
        lblPetName.setForeground(new java.awt.Color(71, 85, 105));

        txtPetName.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtPetName.setAlignmentX(0.0F);
        txtPetName.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtPetName.setMinimumSize(new java.awt.Dimension(0, 42));
        txtPetName.setPreferredSize(new java.awt.Dimension(233, 42));

        pnlPetSpeciesGender.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetSpeciesGender.setAlignmentX(0.0F);
        pnlPetSpeciesGender.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPetSpeciesGender.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPetSpeciesGender.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPetSpeciesGender.setLayout(new java.awt.GridLayout(1, 2, 13, 0));

        pnlPetSpeciesGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetSpeciesGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlPetSpeciesGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPetSpeciesGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPetSpeciesGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPetSpeciesGroup.setAlignmentX(0.0F);

        lblSpecies.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblSpecies.setText("Species *");
        lblSpecies.setPreferredSize(new java.awt.Dimension(0, 21));
        lblSpecies.setForeground(new java.awt.Color(71, 85, 105));

        cmbSpecies.setFont(new java.awt.Font("Segoe UI", 0, 16));
        cmbSpecies.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select", "Dog", "Cat", "Bird", "Other" }));
        cmbSpecies.setAlignmentX(0.0F);
        cmbSpecies.setMaximumSize(new java.awt.Dimension(32767, 42));
        cmbSpecies.setMinimumSize(new java.awt.Dimension(0, 42));
        cmbSpecies.setPreferredSize(new java.awt.Dimension(233, 42));


        pnlPetGenderGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetGenderGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlPetGenderGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPetGenderGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPetGenderGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPetGenderGroup.setAlignmentX(0.0F);

        lblGender.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblGender.setText("Gender *");
        lblGender.setPreferredSize(new java.awt.Dimension(0, 21));
        lblGender.setForeground(new java.awt.Color(71, 85, 105));

        cmbGender.setFont(new java.awt.Font("Segoe UI", 0, 16));
        cmbGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select", "Male", "Female", "Unknown" }));
        cmbGender.setAlignmentX(0.0F);
        cmbGender.setMaximumSize(new java.awt.Dimension(32767, 42));
        cmbGender.setMinimumSize(new java.awt.Dimension(0, 42));
        cmbGender.setPreferredSize(new java.awt.Dimension(233, 42));



        lblBreed.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblBreed.setText("Breed (optional)");
        lblBreed.setPreferredSize(new java.awt.Dimension(0, 21));
        lblBreed.setForeground(new java.awt.Color(71, 85, 105));

        txtBreed.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtBreed.setAlignmentX(0.0F);
        txtBreed.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtBreed.setMinimumSize(new java.awt.Dimension(0, 42));
        txtBreed.setPreferredSize(new java.awt.Dimension(233, 42));

        lblPetDob.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPetDob.setText("Date of Birth");
        lblPetDob.setPreferredSize(new java.awt.Dimension(0, 21));
        lblPetDob.setForeground(new java.awt.Color(71, 85, 105));

        spnPetDob.setFont(new java.awt.Font("Segoe UI", 0, 16));
        spnPetDob.setModel(new javax.swing.SpinnerDateModel());
        spnPetDob.setAlignmentX(0.0F);
        spnPetDob.setEnabled(false);
        spnPetDob.setMaximumSize(new java.awt.Dimension(32767, 42));
        spnPetDob.setMinimumSize(new java.awt.Dimension(0, 42));
        spnPetDob.setPreferredSize(new java.awt.Dimension(233, 42));

        chkDobUnknown.setFont(new java.awt.Font("Segoe UI", 0, 13));
        chkDobUnknown.setSelected(true);
        chkDobUnknown.setText("Date unknown");
        chkDobUnknown.addActionListener(this::chkDobUnknownActionPerformed);

        lblPetAge.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPetAge.setText("Age: Unknown");
        lblPetAge.setMinimumSize(new java.awt.Dimension(0, 21));
        lblPetAge.setPreferredSize(new java.awt.Dimension(233, 21));
        lblPetAge.setMaximumSize(new java.awt.Dimension(32767, 21));
        lblPetAge.setAlignmentX(0.0F);
        lblPetAge.setForeground(new java.awt.Color(71, 85, 105));
        lblPetAge.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        scrPetForm.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        pnlPetActions.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetActions.setAlignmentX(0.0F);
        pnlPetActions.setLayout(new java.awt.BorderLayout(0, 5));

        lblPetError.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPetError.setForeground(new java.awt.Color(185, 28, 28));
        lblPetError.setText(" ");
        lblPetError.setPreferredSize(new java.awt.Dimension(0, 34));
        lblPetError.setMinimumSize(new java.awt.Dimension(0, 34));
        lblPetError.setMaximumSize(new java.awt.Dimension(32767, 34));
        lblPetError.setAlignmentX(0.0F);

        pnlPetButtons.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetButtons.setPreferredSize(new java.awt.Dimension(0, 42));
        pnlPetButtons.setLayout(new java.awt.GridLayout(1, 3, 8, 0));
        pnlPetButtons.setMinimumSize(new java.awt.Dimension(0, 42));
        pnlPetButtons.setMaximumSize(new java.awt.Dimension(32767, 42));
        pnlPetButtons.setAlignmentX(0.0F);

        btnSavePet.setBackground(new java.awt.Color(15, 118, 110));
        btnSavePet.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnSavePet.setForeground(new java.awt.Color(255, 255, 255));
        btnSavePet.setText("Save");
        btnSavePet.setMargin(new java.awt.Insets(8, 5, 8, 5));
        btnSavePet.addActionListener(this::btnSavePetActionPerformed);

        btnCancelPet.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCancelPet.setForeground(new java.awt.Color(15, 118, 110));
        btnCancelPet.setText("Clear form");
        btnCancelPet.setMargin(new java.awt.Insets(8, 5, 8, 5));
        btnCancelPet.setBackground(new java.awt.Color(255, 255, 255));
        btnCancelPet.addActionListener(this::btnCancelPetActionPerformed);

        btnDeletePet.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnDeletePet.addActionListener(this::btnDeletePetActionPerformed);
        btnDeletePet.setForeground(new java.awt.Color(185, 28, 28));
        btnDeletePet.setText("Delete");
        btnDeletePet.setEnabled(false);
        btnDeletePet.setMargin(new java.awt.Insets(8, 5, 8, 5));
        btnDeletePet.setBackground(new java.awt.Color(255, 255, 255));




        pnlPetFormHeader.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetFormHeader.setLayout(new java.awt.BorderLayout(0, 5));
        pnlPetOwnerGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetOwnerGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlPetOwnerGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPetOwnerGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPetOwnerGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPetOwnerGroup.setAlignmentX(0.0F);
        pnlPetNameGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetNameGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlPetNameGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPetNameGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPetNameGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPetNameGroup.setAlignmentX(0.0F);
        pnlPetBreedGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetBreedGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlPetBreedGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPetBreedGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPetBreedGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPetBreedGroup.setAlignmentX(0.0F);
        pnlPetIdentityRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetIdentityRow.setLayout(new java.awt.GridLayout(1, 2, 13, 0));
        pnlPetIdentityRow.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPetIdentityRow.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPetIdentityRow.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPetIdentityRow.setAlignmentX(0.0F);
        pnlPetDobGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetDobGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlPetDobGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPetDobGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPetDobGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPetDobGroup.setAlignmentX(0.0F);
        pnlPetAgeGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetAgeGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlPetAgeGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPetAgeGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPetAgeGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPetAgeGroup.setAlignmentX(0.0F);
        pnlPetBirthRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetBirthRow.setLayout(new java.awt.GridLayout(1, 2, 13, 0));
        pnlPetBirthRow.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPetBirthRow.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPetBirthRow.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPetBirthRow.setAlignmentX(0.0F);
        pnlPetFormGlue.setBackground(new java.awt.Color(255, 255, 255));
        pnlPetFormGlue.setLayout(new javax.swing.BoxLayout(pnlPetFormGlue, javax.swing.BoxLayout.Y_AXIS));
        pnlPetFormGlue.setMinimumSize(new java.awt.Dimension(0, 0));
        pnlPetFormGlue.setPreferredSize(new java.awt.Dimension(0, 0));
        pnlPetFormGlue.setMaximumSize(new java.awt.Dimension(32767, 32767));
        pnlPetFormGlue.setAlignmentX(0.0F);

        pnlPetSearchGroup.add(lblPetSearch);
        pnlPetSearchGroup.add(filler1);
        pnlPetSearchGroup.add(txtPetSearch);
        pnlPetFilterGroup.add(lblSpeciesFilter);
        pnlPetFilterGroup.add(filler2);
        pnlPetFilterGroup.add(cmbSpeciesFilter);
        pnlPetToolbar.add(pnlPetSearchGroup);
        pnlPetToolbar.add(filler3);
        pnlPetToolbar.add(pnlPetFilterGroup);
        pnlPetToolbar.add(filler4);
        pnlPetToolbar.add(btnSearchPet);
        pnlPetToolbar.add(filler5);
        pnlPetToolbar.add(filler6);
        pnlPetToolbar.add(btnNewPet);
        scrPetTable.setViewportView(tblPets);
        pnlPetList.add(lblPetListTitle, java.awt.BorderLayout.NORTH);
        pnlPetList.add(scrPetTable, java.awt.BorderLayout.CENTER);
        pnlPetList.add(lblPetCount, java.awt.BorderLayout.SOUTH);
        pnlPetFormHeader.add(lblPetFormTitle, java.awt.BorderLayout.NORTH);
        pnlPetFormHeader.add(lblPetId, java.awt.BorderLayout.CENTER);
        pnlPetOwnerGroup.add(lblPetOwner, java.awt.BorderLayout.NORTH);
        pnlPetOwnerGroup.add(cmbPetOwner, java.awt.BorderLayout.CENTER);
        pnlPetNameGroup.add(lblPetName, java.awt.BorderLayout.NORTH);
        pnlPetNameGroup.add(txtPetName, java.awt.BorderLayout.CENTER);
        pnlPetBreedGroup.add(lblBreed, java.awt.BorderLayout.NORTH);
        pnlPetBreedGroup.add(txtBreed, java.awt.BorderLayout.CENTER);
        pnlPetIdentityRow.add(pnlPetNameGroup);
        pnlPetIdentityRow.add(pnlPetBreedGroup);
        pnlPetSpeciesGroup.add(lblSpecies, java.awt.BorderLayout.NORTH);
        pnlPetSpeciesGroup.add(cmbSpecies, java.awt.BorderLayout.CENTER);
        pnlPetGenderGroup.add(lblGender, java.awt.BorderLayout.NORTH);
        pnlPetGenderGroup.add(cmbGender, java.awt.BorderLayout.CENTER);
        pnlPetSpeciesGender.add(pnlPetSpeciesGroup);
        pnlPetSpeciesGender.add(pnlPetGenderGroup);
        pnlPetDobGroup.add(lblPetDob, java.awt.BorderLayout.NORTH);
        pnlPetDobGroup.add(spnPetDob, java.awt.BorderLayout.CENTER);
        pnlPetAgeGroup.add(chkDobUnknown, java.awt.BorderLayout.NORTH);
        pnlPetAgeGroup.add(lblPetAge, java.awt.BorderLayout.CENTER);
        pnlPetBirthRow.add(pnlPetDobGroup);
        pnlPetBirthRow.add(pnlPetAgeGroup);
        pnlPetForm.add(pnlPetOwnerGroup);
        pnlPetForm.add(filler10);
        pnlPetForm.add(pnlPetIdentityRow);
        pnlPetForm.add(filler12);
        pnlPetForm.add(pnlPetSpeciesGender);
        pnlPetForm.add(filler13);
        pnlPetForm.add(pnlPetBirthRow);
        pnlPetForm.add(pnlPetFormGlue);
        scrPetForm.setViewportView(pnlPetForm);
        pnlPetButtons.add(btnSavePet);
        pnlPetButtons.add(btnCancelPet);
        pnlPetButtons.add(btnDeletePet);
        pnlPetActions.add(lblPetError, java.awt.BorderLayout.NORTH);
        pnlPetActions.add(pnlPetButtons, java.awt.BorderLayout.CENTER);
        pnlPetEditor.add(pnlPetFormHeader, java.awt.BorderLayout.NORTH);
        pnlPetEditor.add(scrPetForm, java.awt.BorderLayout.CENTER);
        pnlPetEditor.add(pnlPetActions, java.awt.BorderLayout.SOUTH);
        splitPets.setLeftComponent(pnlPetList);
        splitPets.setRightComponent(pnlPetEditor);
        add(pnlPetToolbar, java.awt.BorderLayout.NORTH);
        add(splitPets, java.awt.BorderLayout.CENTER);
        tblPets.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        tblPets.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        tblPets.getTableHeader().setReorderingAllowed(false);
        scrPetTable.setColumnHeaderView(tblPets.getTableHeader());
        scrPetTable.getViewport().setBackground(java.awt.Color.WHITE);
    }// </editor-fold>//GEN-END:initComponents

    private void chkDobUnknownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDobUnknownActionPerformed
      updatePetFormState();
      lblPetAge.setText(chkDobUnknown.isSelected() ? "Age: Unknown" : "Age: —");
    }//GEN-LAST:event_chkDobUnknownActionPerformed

    private void btnSavePetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSavePetActionPerformed
        controller.savePet();
    }//GEN-LAST:event_btnSavePetActionPerformed

    private void btnCancelPetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelPetActionPerformed
        controller.newPet();
    }//GEN-LAST:event_btnCancelPetActionPerformed

    private void btnNewPetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewPetActionPerformed
        controller.newPet();
    }//GEN-LAST:event_btnNewPetActionPerformed


    private void btnSearchPetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchPetActionPerformed
        if (controller != null) {
            controller.loadPets();
        }
    }//GEN-LAST:event_btnSearchPetActionPerformed

    private void txtPetSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPetSearchActionPerformed
        if (controller != null) {
            controller.loadPets();
        }
    }//GEN-LAST:event_txtPetSearchActionPerformed

    private void cmbSpeciesFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSpeciesFilterActionPerformed
        if (controller != null) {
            controller.loadPets();
        }
    }//GEN-LAST:event_cmbSpeciesFilterActionPerformed

    private void btnDeletePetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeletePetActionPerformed
        if (controller != null) {
            controller.deletePet();
        }
    }//GEN-LAST:event_btnDeletePetActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnlPetFormGlue;
    private javax.swing.JPanel pnlPetBirthRow;
    private javax.swing.JPanel pnlPetAgeGroup;
    private javax.swing.JPanel pnlPetDobGroup;
    private javax.swing.JPanel pnlPetIdentityRow;
    private javax.swing.JPanel pnlPetBreedGroup;
    private javax.swing.JPanel pnlPetNameGroup;
    private javax.swing.JPanel pnlPetOwnerGroup;
    private javax.swing.JPanel pnlPetFormHeader;
    private javax.swing.JButton btnCancelPet;
    private javax.swing.JButton btnDeletePet;
    private javax.swing.JButton btnNewPet;
    private javax.swing.JButton btnSavePet;
    private javax.swing.JButton btnSearchPet;
    private javax.swing.JCheckBox chkDobUnknown;
    private javax.swing.JComboBox<String> cmbGender;
    private javax.swing.JComboBox<String> cmbPetOwner;
    private javax.swing.JComboBox<String> cmbSpecies;
    private javax.swing.JComboBox<String> cmbSpeciesFilter;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler10;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler13;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblBreed;
    private javax.swing.JLabel lblGender;
    private javax.swing.JLabel lblPetAge;
    private javax.swing.JLabel lblPetCount;
    private javax.swing.JLabel lblPetDob;
    private javax.swing.JLabel lblPetError;
    private javax.swing.JLabel lblPetFormTitle;
    private javax.swing.JLabel lblPetId;
    private javax.swing.JLabel lblPetListTitle;
    private javax.swing.JLabel lblPetName;
    private javax.swing.JLabel lblPetOwner;
    private javax.swing.JLabel lblPetSearch;
    private javax.swing.JLabel lblSpecies;
    private javax.swing.JLabel lblSpeciesFilter;
    private javax.swing.JPanel pnlPetActions;
    private javax.swing.JPanel pnlPetButtons;
    private javax.swing.JPanel pnlPetEditor;
    private javax.swing.JPanel pnlPetFilterGroup;
    private javax.swing.JPanel pnlPetForm;
    private javax.swing.JPanel pnlPetGenderGroup;
    private javax.swing.JPanel pnlPetList;
    private javax.swing.JPanel pnlPetSearchGroup;
    private javax.swing.JPanel pnlPetSpeciesGender;
    private javax.swing.JPanel pnlPetSpeciesGroup;
    private javax.swing.JPanel pnlPetToolbar;
    private javax.swing.JScrollPane scrPetForm;
    private javax.swing.JScrollPane scrPetTable;
    private javax.swing.JSplitPane splitPets;
    private javax.swing.JSpinner spnPetDob;
    private javax.swing.JTable tblPets;
    private javax.swing.JTextField txtBreed;
    private javax.swing.JTextField txtPetName;
    private javax.swing.JTextField txtPetSearch;
    // End of variables declaration//GEN-END:variables
}
