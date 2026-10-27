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
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import vetora.model.Owner;
import vetora.controller.OwnerController;

/**
 *
 * @author ASUS
 */
public class PanelOwner extends javax.swing.JPanel {

    private OwnerController controller;
    
    private List<Owner> displayedOwners = new ArrayList<>();
    private Long editingOwnerId;
    private boolean ownerSplitReady;
    /**
     * Creates new form PanelOwner
     */
    public PanelOwner() {
        initComponents();
        setupOwnerUI();
        
        tblOwners.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblOwners.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting() && controller != null){
                controller.selectOwner();
            }
        });
    }
    
    public String getOwnerName(){
        return txtOwnerName.getText();
    }
    public String getOwnerPhone(){
        return txtOwnerPhone.getText();
    }
    public String getOwnerEmail(){
        return txtOwnerEmail.getText();
    }
    public String getOwnerAddress(){
        return txtOwnerAddress.getText();
    }
    public String getOwnerSearch() {
        return txtOwnerSearch.getText();
    }
    public void showOwnerError(String message){
        lblOwnerError.setText("<html>" + message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</html>");
    }

    public Long getEditingOwnerId(){
        return editingOwnerId;
    }
    
    
    private void setupOwnerUI(){
        tblOwners.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblOwners.getTableHeader().setReorderingAllowed(false);
        
        scrOwnerForm.getViewport().setBackground(Color.WHITE);
        scrOwnerForm.getVerticalScrollBar().setUnitIncrement(16);
        scrOwnerForm.getVerticalScrollBar().setPreferredSize(new Dimension(10,0));
        scrOwnerAddress.getVerticalScrollBar().setPreferredSize(new Dimension(5,0));
        scrOwnersTable.getVerticalScrollBar().setPreferredSize(new Dimension(8,0));
        

    }
    
    public void displayOwners(List<Owner> owners){
        
        displayedOwners = new ArrayList<>(owners);
        
        DefaultTableModel tableModel = (DefaultTableModel)tblOwners.getModel();
        
        tableModel.setRowCount(0);
        
        for(Owner owner : owners){
            tableModel.addRow(new Object[]{
                owner.getOwnerId(),
                owner.getFullName(),
                owner.getPhone(),
                null
            });
        }
        
        lblOwnerCount.setText("Owners: "+owners.size());
    }
    
    public void showOwnerListMessage(String message){
        lblOwnerCount.setText(message);
    }
    
    public void setController(OwnerController controller){
        this.controller = controller;
    }
    
    public void clearOwnerForm(){
        editingOwnerId = null;
        btnSaveOwner.setText("Save");
        
        txtOwnerName.setText("");
        txtOwnerPhone.setText("");
        txtOwnerEmail.setText("");
        txtOwnerAddress.setText("");

        lblOwnerID.setText("Owner ID: Auto-generated");
        lblOwnerFormTitle.setText("Add owner");
        showOwnerError(" ");

        tblOwners.clearSelection();
        btnDeleteOwner.setEnabled(false);
        txtOwnerName.requestFocusInWindow();
    }
    
    public void setSavingOwner(boolean saving){
        btnSaveOwner.setEnabled(!saving);
        btnClearOwner.setEnabled(!saving);
        btnNewOwner.setEnabled(!saving);

        txtOwnerName.setEditable(!saving);
        txtOwnerPhone.setEditable(!saving);
        txtOwnerEmail.setEditable(!saving);
        txtOwnerAddress.setEditable(!saving);
        tblOwners.setEnabled(!saving);

        String normalText = editingOwnerId == null ? "Save" : "Update";
        btnSaveOwner.setText(saving ? "Saving..." : normalText);
        
        btnDeleteOwner.setEnabled(!saving && editingOwnerId != null);
    }
    
    public void showOwnerSaved(){
        JOptionPane.showMessageDialog(this,"Owner saved successfully.","Owners",JOptionPane.INFORMATION_MESSAGE);
    }
    
    public Owner getSelectedOwner(){
        int selectedRow = tblOwners.getSelectedRow();
        
        if (selectedRow < 0){
            return null;
        }
        
        int modelRow = tblOwners.convertRowIndexToModel(selectedRow);
        
        return displayedOwners.get(modelRow);
    }
    
    public void showOwnerDetails(Owner owner){
        editingOwnerId = owner.getOwnerId();
        
        txtOwnerName.setText(owner.getFullName());
        txtOwnerPhone.setText(owner.getPhone());
        txtOwnerEmail.setText(owner.getEmail()== null ? "" : owner.getEmail());
        txtOwnerAddress.setText(owner.getAdress() == null ? "" : owner.getAdress());
        
        lblOwnerID.setText("Owner ID: " + editingOwnerId);
        lblOwnerFormTitle.setText("Edit owner");
        btnSaveOwner.setText("Update");
        
        showOwnerError(" ");
        
        btnDeleteOwner.setEnabled(true);
    }
    
    public boolean confirmOwnerDelete(long ownerId){
        String[] options = {"Delete","Cancel"};
        
        int choice = JOptionPane.showOptionDialog( this, "Delete owner ID" + ownerId + "?\nThis action cannot be undone.", "Delete owner", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options,options[1]);
        
        return choice == 0;
    }
    
    public void setDeletingOwner(boolean deleting){
        setSavingOwner(deleting);
        btnSaveOwner.setText(editingOwnerId == null ? "Save" : "Update");
        btnDeleteOwner.setText(deleting ? "Deleting..." : "Delete");
    } 
    
    public void ShowOwnerDeleted(){
        JOptionPane.showMessageDialog( this, "Owner deleted successfully.", "Owners", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void doLayout(){
        super.doLayout();
        if (!ownerSplitReady && splitOwners.getWidth() > 0) {
            int width = splitOwners.getWidth() - splitOwners.getDividerSize();
            int editorWidth = Math.max(360, (int)Math.round(width * 0.382));
            splitOwners.setDividerLocation(Math.max(300, width - editorWidth));
            ownerSplitReady = true;
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
        pnlOwnerFormGlue = new javax.swing.JPanel();
        pnlOwnerAddressGroup = new javax.swing.JPanel();
        pnlOwnerContactRow = new javax.swing.JPanel();
        pnlOwnerEmailGroup = new javax.swing.JPanel();
        pnlOwnerPhoneGroup = new javax.swing.JPanel();
        pnlOwnerNameGroup = new javax.swing.JPanel();
        pnlOwnerFormHeader = new javax.swing.JPanel();
        pnlOwnerToolbarButtons = new javax.swing.JPanel();
        pnlOwnerSearchRow = new javax.swing.JPanel();

        filler10 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0));
        pnlOwnerToolBar = new javax.swing.JPanel();
        lblOwnerSearch = new javax.swing.JLabel();
        txtOwnerSearch = new javax.swing.JTextField();
        btnSearchOwner = new javax.swing.JButton();
        btnNewOwner = new javax.swing.JButton();
        splitOwners = new javax.swing.JSplitPane();
        pnlOwnerList = new javax.swing.JPanel();
        lblOwnerListTitle = new javax.swing.JLabel();
        lblOwnerCount = new javax.swing.JLabel();
        scrOwnersTable = new javax.swing.JScrollPane();
        tblOwners = new javax.swing.JTable();
        pnlOwnerEditer = new javax.swing.JPanel();
        scrOwnerForm = new javax.swing.JScrollPane();
        pnlOwnerForm = new javax.swing.JPanel();
        lblOwnerFormTitle = new javax.swing.JLabel();
        lblOwnerID = new javax.swing.JLabel();
        lblOwnerName = new javax.swing.JLabel();
        txtOwnerName = new javax.swing.JTextField();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        lblOwnerPhone = new javax.swing.JLabel();
        txtOwnerPhone = new javax.swing.JTextField();
        lblOwnerEmail = new javax.swing.JLabel();
        txtOwnerEmail = new javax.swing.JTextField();
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        lblOwnerAddress = new javax.swing.JLabel();
        scrOwnerAddress = new javax.swing.JScrollPane();
        txtOwnerAddress = new javax.swing.JTextArea();
        pnlOwnerActions = new javax.swing.JPanel();
        lblOwnerError = new javax.swing.JLabel();
        pnlOwnerButtons = new javax.swing.JPanel();
        btnSaveOwner = new javax.swing.JButton();
        btnClearOwner = new javax.swing.JButton();
        btnDeleteOwner = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 247, 250));
        setLayout(new java.awt.BorderLayout(0, 13));

        pnlOwnerToolBar.setBackground(new java.awt.Color(245, 247, 250));
        pnlOwnerToolBar.setName(""); // NOI18N
        pnlOwnerToolBar.setPreferredSize(new java.awt.Dimension(0, 84));

        lblOwnerSearch.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblOwnerSearch.setText("Search by name or phone");
        lblOwnerSearch.setMinimumSize(new java.awt.Dimension(0, 21));
        lblOwnerSearch.setPreferredSize(new java.awt.Dimension(233, 21));
        lblOwnerSearch.setMaximumSize(new java.awt.Dimension(32767, 21));
        lblOwnerSearch.setAlignmentX(0.0F);
        lblOwnerSearch.setForeground(new java.awt.Color(71, 85, 105));

        txtOwnerSearch.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtOwnerSearch.setPreferredSize(new java.awt.Dimension(233, 42));
        txtOwnerSearch.setMinimumSize(new java.awt.Dimension(0, 42));
        txtOwnerSearch.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtOwnerSearch.setAlignmentX(0.0F);
        txtOwnerSearch.addActionListener(this::txtOwnerSearchActionPerformed);

        btnSearchOwner.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnSearchOwner.setForeground(new java.awt.Color(15, 118, 110));
        btnSearchOwner.setText("Search");
        btnSearchOwner.setPreferredSize(new java.awt.Dimension(89, 42));
        btnSearchOwner.setMinimumSize(new java.awt.Dimension(89, 42));
        btnSearchOwner.setMaximumSize(new java.awt.Dimension(89, 42));
        btnSearchOwner.setBackground(new java.awt.Color(255, 255, 255));
        btnSearchOwner.addActionListener(this::btnSearchOwnerActionPerformed);

        btnNewOwner.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnNewOwner.setForeground(new java.awt.Color(255, 255, 255));
        btnNewOwner.setText("New owner");
        btnNewOwner.setPreferredSize(new java.awt.Dimension(144, 42));
        btnNewOwner.setMinimumSize(new java.awt.Dimension(144, 42));
        btnNewOwner.setMaximumSize(new java.awt.Dimension(144, 42));
        btnNewOwner.setBackground(new java.awt.Color(15, 118, 110));
        btnNewOwner.addActionListener(this::btnNewOwnerActionPerformed);

        pnlOwnerToolBar.setLayout(new java.awt.BorderLayout(13, 5));


        splitOwners.setDividerSize(5);
        splitOwners.setResizeWeight(0.618);

        pnlOwnerList.setBackground(new java.awt.Color(255, 255, 255));
        pnlOwnerList.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlOwnerList.setMinimumSize(new java.awt.Dimension(300, 100));
        pnlOwnerList.setLayout(new java.awt.BorderLayout(0, 13));

        lblOwnerListTitle.setFont(new java.awt.Font("Segoe UI", 1, 21));
        lblOwnerListTitle.setText("Registered owners");
        lblOwnerListTitle.setForeground(new java.awt.Color(15, 23, 42));

        lblOwnerCount.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblOwnerCount.setForeground(new java.awt.Color(71, 85, 105));
        lblOwnerCount.setText("Owners not loaded.");

        tblOwners.setFont(new java.awt.Font("Segoe UI", 0, 16));
        tblOwners.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Owner ID", "Full Name", "Phone", "Pets"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblOwners.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblOwners.setColumnSelectionAllowed(true);
        tblOwners.setFillsViewportHeight(true);
        tblOwners.setGridColor(new java.awt.Color(226, 232, 240));
        tblOwners.setRowHeight(34);
        tblOwners.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblOwners.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblOwners.setShowHorizontalLines(true);
        tblOwners.setBackground(new java.awt.Color(255, 255, 255));
        tblOwners.setForeground(new java.awt.Color(15, 23, 42));
        tblOwners.setSelectionBackground(new java.awt.Color(204, 251, 241));
        tblOwners.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tblOwners.setShowVerticalLines(false);
        tblOwners.getTableHeader().setReorderingAllowed(false);
        tblOwners.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tblOwners.getColumnModel().getColumnCount() > 0) {
            tblOwners.getColumnModel().getColumn(0).setPreferredWidth(89);
            tblOwners.getColumnModel().getColumn(1).setPreferredWidth(233);
            tblOwners.getColumnModel().getColumn(2).setPreferredWidth(144);
            tblOwners.getColumnModel().getColumn(3).setPreferredWidth(55);
        }



        pnlOwnerEditer.setBackground(new java.awt.Color(255, 255, 255));
        pnlOwnerEditer.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlOwnerEditer.setMinimumSize(new java.awt.Dimension(360, 0));
        pnlOwnerEditer.setLayout(new java.awt.BorderLayout(0, 8));

        scrOwnerForm.setBorder(null);
        scrOwnerForm.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrOwnerForm.setMinimumSize(new java.awt.Dimension(300, 0));

        pnlOwnerForm.setBackground(new java.awt.Color(255, 255, 255));
        pnlOwnerForm.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlOwnerForm.setLayout(new javax.swing.BoxLayout(pnlOwnerForm, javax.swing.BoxLayout.Y_AXIS));

        lblOwnerFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 21));
        lblOwnerFormTitle.setText("Add owner");
        lblOwnerFormTitle.setForeground(new java.awt.Color(15, 23, 42));

        lblOwnerID.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblOwnerID.setText("Owner ID:  Auto-generated");
        lblOwnerID.setForeground(new java.awt.Color(71, 85, 105));

        lblOwnerName.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblOwnerName.setText("Full name *");
        lblOwnerName.setPreferredSize(new java.awt.Dimension(0, 21));
        lblOwnerName.setForeground(new java.awt.Color(71, 85, 105));

        txtOwnerName.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtOwnerName.setAlignmentX(0.0F);
        txtOwnerName.setFocusTraversalPolicyProvider(true);
        txtOwnerName.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtOwnerName.setMinimumSize(new java.awt.Dimension(0, 42));
        txtOwnerName.setPreferredSize(new java.awt.Dimension(233, 42));

        lblOwnerPhone.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblOwnerPhone.setText("Phone number *");
        lblOwnerPhone.setPreferredSize(new java.awt.Dimension(0, 21));
        lblOwnerPhone.setForeground(new java.awt.Color(71, 85, 105));

        txtOwnerPhone.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtOwnerPhone.setAlignmentX(0.0F);
        txtOwnerPhone.setFocusTraversalPolicyProvider(true);
        txtOwnerPhone.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtOwnerPhone.setMinimumSize(new java.awt.Dimension(0, 42));
        txtOwnerPhone.setPreferredSize(new java.awt.Dimension(233, 42));

        lblOwnerEmail.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblOwnerEmail.setText("Email (optional)");
        lblOwnerEmail.setPreferredSize(new java.awt.Dimension(0, 21));
        lblOwnerEmail.setForeground(new java.awt.Color(71, 85, 105));

        txtOwnerEmail.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtOwnerEmail.setAlignmentX(0.0F);
        txtOwnerEmail.setFocusTraversalPolicyProvider(true);
        txtOwnerEmail.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtOwnerEmail.setMinimumSize(new java.awt.Dimension(0, 42));
        txtOwnerEmail.setPreferredSize(new java.awt.Dimension(233, 42));

        lblOwnerAddress.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblOwnerAddress.setText("Address (optional)");
        lblOwnerAddress.setPreferredSize(new java.awt.Dimension(0, 21));
        lblOwnerAddress.setForeground(new java.awt.Color(71, 85, 105));

        scrOwnerAddress.setAlignmentX(0.0F);
        scrOwnerAddress.setMaximumSize(new java.awt.Dimension(32767, 68));
        scrOwnerAddress.setMinimumSize(new java.awt.Dimension(0, 68));
        scrOwnerAddress.setPreferredSize(new java.awt.Dimension(233, 68));

        txtOwnerAddress.setColumns(0);
        txtOwnerAddress.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtOwnerAddress.setLineWrap(true);
        txtOwnerAddress.setWrapStyleWord(true);
        txtOwnerAddress.setRows(2);
        txtOwnerAddress.setMargin(new java.awt.Insets(4, 8, 4, 8));
        scrOwnerAddress.setFont(new java.awt.Font("Segoe UI", 0, 16));
        scrOwnerAddress.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrOwnerAddress.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        scrOwnerForm.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        pnlOwnerActions.setBackground(new java.awt.Color(255, 255, 255));
        pnlOwnerActions.setLayout(new java.awt.BorderLayout(0, 5));

        lblOwnerError.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblOwnerError.setForeground(new java.awt.Color(185, 28, 28));
        lblOwnerError.setText(" ");
        lblOwnerError.setPreferredSize(new java.awt.Dimension(0, 34));

        pnlOwnerButtons.setBackground(new java.awt.Color(255, 255, 255));
        pnlOwnerButtons.setPreferredSize(new java.awt.Dimension(0, 42));
        pnlOwnerButtons.setLayout(new java.awt.GridLayout(1, 3, 8, 0));
        pnlOwnerButtons.setMinimumSize(new java.awt.Dimension(0, 42));
        pnlOwnerButtons.setMaximumSize(new java.awt.Dimension(32767, 42));
        pnlOwnerButtons.setAlignmentX(0.0F);

        btnSaveOwner.setBackground(new java.awt.Color(15, 118, 110));
        btnSaveOwner.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnSaveOwner.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveOwner.setText("Save");
        btnSaveOwner.setMargin(new java.awt.Insets(8, 5, 8, 5));
        btnSaveOwner.setPreferredSize(new java.awt.Dimension(90, 42));
        btnSaveOwner.addActionListener(this::btnSaveOwnerActionPerformed);

        btnClearOwner.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnClearOwner.setForeground(new java.awt.Color(15, 118, 110));
        btnClearOwner.setText("Clear form");
        btnClearOwner.setMargin(new java.awt.Insets(8, 5, 8, 5));
        btnClearOwner.setPreferredSize(new java.awt.Dimension(90, 42));
        btnClearOwner.setBackground(new java.awt.Color(255, 255, 255));
        btnClearOwner.addActionListener(this::btnClearOwnerActionPerformed);

        btnDeleteOwner.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnDeleteOwner.setForeground(new java.awt.Color(185, 28, 28));
        btnDeleteOwner.setText("Delete");
        btnDeleteOwner.setEnabled(false);
        btnDeleteOwner.setMargin(new java.awt.Insets(8, 5, 8, 5));
        btnDeleteOwner.setPreferredSize(new java.awt.Dimension(90, 42));
        btnDeleteOwner.setBackground(new java.awt.Color(255, 255, 255));
        btnDeleteOwner.addActionListener(this::btnDeleteOwnerActionPerformed);




        pnlOwnerToolBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 13, 8, 13));
        pnlOwnerSearchRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlOwnerSearchRow.setLayout(new java.awt.BorderLayout(13, 0));
        pnlOwnerToolbarButtons.setBackground(new java.awt.Color(255, 255, 255));
        pnlOwnerToolbarButtons.setLayout(new java.awt.BorderLayout(13, 0));
        pnlOwnerToolbarButtons.setPreferredSize(new java.awt.Dimension(246, 42));
        splitOwners.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlOwnerFormHeader.setBackground(new java.awt.Color(255, 255, 255));
        pnlOwnerFormHeader.setLayout(new java.awt.BorderLayout(0, 5));
        pnlOwnerNameGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlOwnerNameGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlOwnerNameGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlOwnerNameGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlOwnerNameGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlOwnerNameGroup.setAlignmentX(0.0F);
        pnlOwnerPhoneGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlOwnerPhoneGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlOwnerPhoneGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlOwnerPhoneGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlOwnerPhoneGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlOwnerPhoneGroup.setAlignmentX(0.0F);
        pnlOwnerEmailGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlOwnerEmailGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlOwnerEmailGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlOwnerEmailGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlOwnerEmailGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlOwnerEmailGroup.setAlignmentX(0.0F);
        pnlOwnerContactRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlOwnerContactRow.setLayout(new java.awt.GridLayout(1, 2, 13, 0));
        pnlOwnerContactRow.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlOwnerContactRow.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlOwnerContactRow.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlOwnerContactRow.setAlignmentX(0.0F);
        pnlOwnerAddressGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlOwnerAddressGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlOwnerAddressGroup.setMinimumSize(new java.awt.Dimension(0, 94));
        pnlOwnerAddressGroup.setPreferredSize(new java.awt.Dimension(233, 94));
        pnlOwnerAddressGroup.setMaximumSize(new java.awt.Dimension(32767, 94));
        pnlOwnerAddressGroup.setAlignmentX(0.0F);
        pnlOwnerFormGlue.setBackground(new java.awt.Color(255, 255, 255));
        pnlOwnerFormGlue.setLayout(new javax.swing.BoxLayout(pnlOwnerFormGlue, javax.swing.BoxLayout.Y_AXIS));
        pnlOwnerFormGlue.setMinimumSize(new java.awt.Dimension(0, 0));
        pnlOwnerFormGlue.setPreferredSize(new java.awt.Dimension(0, 0));
        pnlOwnerFormGlue.setMaximumSize(new java.awt.Dimension(32767, 32767));
        pnlOwnerFormGlue.setOpaque(false);
        pnlOwnerFormGlue.setAlignmentX(0.0F);

        pnlOwnerToolbarButtons.add(btnSearchOwner, java.awt.BorderLayout.WEST);
        pnlOwnerToolbarButtons.add(btnNewOwner, java.awt.BorderLayout.EAST);
        pnlOwnerSearchRow.add(txtOwnerSearch, java.awt.BorderLayout.CENTER);
        pnlOwnerSearchRow.add(pnlOwnerToolbarButtons, java.awt.BorderLayout.EAST);
        pnlOwnerToolBar.add(lblOwnerSearch, java.awt.BorderLayout.NORTH);
        pnlOwnerToolBar.add(pnlOwnerSearchRow, java.awt.BorderLayout.CENTER);
        scrOwnersTable.setViewportView(tblOwners);
        pnlOwnerList.add(lblOwnerListTitle, java.awt.BorderLayout.NORTH);
        pnlOwnerList.add(lblOwnerCount, java.awt.BorderLayout.SOUTH);
        pnlOwnerList.add(scrOwnersTable, java.awt.BorderLayout.CENTER);
        pnlOwnerFormHeader.add(lblOwnerFormTitle, java.awt.BorderLayout.NORTH);
        pnlOwnerFormHeader.add(lblOwnerID, java.awt.BorderLayout.CENTER);
        pnlOwnerNameGroup.add(lblOwnerName, java.awt.BorderLayout.NORTH);
        pnlOwnerNameGroup.add(txtOwnerName, java.awt.BorderLayout.CENTER);
        pnlOwnerPhoneGroup.add(lblOwnerPhone, java.awt.BorderLayout.NORTH);
        pnlOwnerPhoneGroup.add(txtOwnerPhone, java.awt.BorderLayout.CENTER);
        pnlOwnerEmailGroup.add(lblOwnerEmail, java.awt.BorderLayout.NORTH);
        pnlOwnerEmailGroup.add(txtOwnerEmail, java.awt.BorderLayout.CENTER);
        pnlOwnerContactRow.add(pnlOwnerPhoneGroup);
        pnlOwnerContactRow.add(pnlOwnerEmailGroup);
        scrOwnerAddress.setViewportView(txtOwnerAddress);
        pnlOwnerAddressGroup.add(lblOwnerAddress, java.awt.BorderLayout.NORTH);
        pnlOwnerAddressGroup.add(scrOwnerAddress, java.awt.BorderLayout.CENTER);
        pnlOwnerForm.add(pnlOwnerNameGroup);
        pnlOwnerForm.add(filler4);
        pnlOwnerForm.add(pnlOwnerContactRow);
        pnlOwnerForm.add(filler8);
        pnlOwnerForm.add(pnlOwnerAddressGroup);
        pnlOwnerForm.add(pnlOwnerFormGlue);
        scrOwnerForm.setViewportView(pnlOwnerForm);
        pnlOwnerButtons.add(btnSaveOwner);
        pnlOwnerButtons.add(btnClearOwner);
        pnlOwnerButtons.add(btnDeleteOwner);
        pnlOwnerActions.add(lblOwnerError, java.awt.BorderLayout.NORTH);
        pnlOwnerActions.add(pnlOwnerButtons, java.awt.BorderLayout.CENTER);
        pnlOwnerEditer.add(pnlOwnerFormHeader, java.awt.BorderLayout.NORTH);
        pnlOwnerEditer.add(scrOwnerForm, java.awt.BorderLayout.CENTER);
        pnlOwnerEditer.add(pnlOwnerActions, java.awt.BorderLayout.SOUTH);
        splitOwners.setLeftComponent(pnlOwnerList);
        splitOwners.setRightComponent(pnlOwnerEditer);
        add(pnlOwnerToolBar, java.awt.BorderLayout.NORTH);
        add(splitOwners, java.awt.BorderLayout.CENTER);
        tblOwners.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        tblOwners.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        tblOwners.getTableHeader().setReorderingAllowed(false);
        scrOwnersTable.setColumnHeaderView(tblOwners.getTableHeader());
        scrOwnersTable.getViewport().setBackground(java.awt.Color.WHITE);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveOwnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveOwnerActionPerformed
        controller.saveOwner();
    }//GEN-LAST:event_btnSaveOwnerActionPerformed

    private void btnClearOwnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearOwnerActionPerformed
        controller.newOwner();
    }//GEN-LAST:event_btnClearOwnerActionPerformed

    private void btnNewOwnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewOwnerActionPerformed
        controller.newOwner();
    }//GEN-LAST:event_btnNewOwnerActionPerformed

    private void btnSearchOwnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchOwnerActionPerformed
        controller.loadOwners();
    }//GEN-LAST:event_btnSearchOwnerActionPerformed

    private void txtOwnerSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtOwnerSearchActionPerformed
        controller.loadOwners();
    }//GEN-LAST:event_txtOwnerSearchActionPerformed

    private void btnDeleteOwnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteOwnerActionPerformed
        controller.deleteOwner();
    }//GEN-LAST:event_btnDeleteOwnerActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnlOwnerFormGlue;
    private javax.swing.JPanel pnlOwnerAddressGroup;
    private javax.swing.JPanel pnlOwnerContactRow;
    private javax.swing.JPanel pnlOwnerEmailGroup;
    private javax.swing.JPanel pnlOwnerPhoneGroup;
    private javax.swing.JPanel pnlOwnerNameGroup;
    private javax.swing.JPanel pnlOwnerFormHeader;
    private javax.swing.JPanel pnlOwnerToolbarButtons;
    private javax.swing.JPanel pnlOwnerSearchRow;
    private javax.swing.JButton btnClearOwner;
    private javax.swing.JButton btnDeleteOwner;
    private javax.swing.JButton btnNewOwner;
    private javax.swing.JButton btnSaveOwner;
    private javax.swing.JButton btnSearchOwner;
    private javax.swing.Box.Filler filler10;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler8;
    private javax.swing.JLabel lblOwnerAddress;
    private javax.swing.JLabel lblOwnerCount;
    private javax.swing.JLabel lblOwnerEmail;
    private javax.swing.JLabel lblOwnerError;
    private javax.swing.JLabel lblOwnerFormTitle;
    private javax.swing.JLabel lblOwnerID;
    private javax.swing.JLabel lblOwnerListTitle;
    private javax.swing.JLabel lblOwnerName;
    private javax.swing.JLabel lblOwnerPhone;
    private javax.swing.JLabel lblOwnerSearch;
    private javax.swing.JPanel pnlOwnerActions;
    private javax.swing.JPanel pnlOwnerButtons;
    private javax.swing.JPanel pnlOwnerEditer;
    private javax.swing.JPanel pnlOwnerForm;
    private javax.swing.JPanel pnlOwnerList;
    private javax.swing.JPanel pnlOwnerToolBar;
    private javax.swing.JScrollPane scrOwnerAddress;
    private javax.swing.JScrollPane scrOwnerForm;
    private javax.swing.JScrollPane scrOwnersTable;
    private javax.swing.JSplitPane splitOwners;
    private javax.swing.JTable tblOwners;
    private javax.swing.JTextArea txtOwnerAddress;
    private javax.swing.JTextField txtOwnerEmail;
    private javax.swing.JTextField txtOwnerName;
    private javax.swing.JTextField txtOwnerPhone;
    private javax.swing.JTextField txtOwnerSearch;
    // End of variables declaration//GEN-END:variables
}
