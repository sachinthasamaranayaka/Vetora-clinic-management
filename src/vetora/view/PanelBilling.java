/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package vetora.view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import vetora.controller.BillingController;
import vetora.model.Invoice;
import vetora.model.InvoiceItem;

/**
 *
 * @author ASUS
 */
public class PanelBilling extends javax.swing.JPanel {

    /**
     * Creates new form PanelBilling
     */
    
    private boolean billingSplitReady = false;
    private BillingController controller;
    private final List<Invoice> billingVisits = new ArrayList<>();
    private final List<InvoiceItem> invoiceItems = new ArrayList<>();
    private Invoice loadedInvoice;
    private boolean billingBusy;
    
    public PanelBilling() {
        initComponents();
        setupBillingUI();
    }
    
    private void setupBillingUI(){
        tblInvoiceItems.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,13));
        scrInvoiceItems.getViewport().setBackground(Color.WHITE);
        scrBillingForm.getViewport().setBackground(Color.WHITE);
        scrBillingForm.getVerticalScrollBar().setUnitIncrement(21);
        spnBillingQuantity.setEditor(new JSpinner.NumberEditor(spnBillingQuantity, "0"));
        tblInvoiceItems.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { updateBillingControls(); }
        });
        javax.swing.table.DefaultTableCellRenderer amountRenderer = new javax.swing.table.DefaultTableCellRenderer();
        amountRenderer.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tblInvoiceItems.getColumnModel().getColumn(2).setCellRenderer(amountRenderer);
        tblInvoiceItems.getColumnModel().getColumn(3).setCellRenderer(amountRenderer);
        setBillingBusy(false);
    }

    public void setController(BillingController controller) {
        this.controller = controller;
    }

    public Long getSelectedBillingAppointmentId() {
        int index = cmbBillingConsultation.getSelectedIndex() - 1;
        return index >= 0 && index < billingVisits.size()
                ? billingVisits.get(index).getAppointmentId() : null;
    }

    public Invoice getLoadedInvoice() { return loadedInvoice; }
    public List<InvoiceItem> getInvoiceItems() { return new ArrayList<>(invoiceItems); }

    public String getBillingService() {
        Object value = cmbBillingService.getEditor().getItem();
        return value == null ? "" : value.toString().trim();
    }

    public String getBillingUnitPrice() { return txtBillingUnitPrice.getText().trim(); }

    public int getBillingQuantity() throws ParseException {
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spnBillingQuantity.getEditor();
        String text = editor.getTextField().getText().trim();
        if (!text.matches("[0-9]{1,4}")) {
            throw new ParseException("Invalid quantity", 0);
        }
        spnBillingQuantity.commitEdit();
        return Integer.parseInt(text);
    }

    public String getPaymentMethod() {
        return cmbPaymentMethod.getSelectedIndex() > 0
                ? (String) cmbPaymentMethod.getSelectedItem() : null;
    }

    public void displayBillingOptions(List<Invoice> visits) {
        billingVisits.clear();
        billingVisits.addAll(visits);
        cmbBillingConsultation.removeAllItems();
        cmbBillingConsultation.addItem("Select consultation");
        for (Invoice invoice : visits) { cmbBillingConsultation.addItem(optionText(invoice)); }
        cmbBillingConsultation.setSelectedIndex(0);
        updateBillingControls();
    }

    private String optionText(Invoice invoice) {
        return "[" + invoice.getStatus() + "] #" + invoice.getAppointmentId()
                + " | " + invoice.getAppointmentDate()
                + " " + invoice.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                + " | " + invoice.getPetName() + " | " + invoice.getOwnerName();
    }

    public void displayInvoice(Invoice invoice) {
        loadedInvoice = invoice;
        invoiceItems.clear();
        invoiceItems.addAll(invoice.getItems());
        clearInvoiceEntry();
        lblBillingOwner.setText("Owner: " + invoice.getOwnerName());
        lblBillingOwner.setToolTipText(lblBillingOwner.getText());
        lblBillingPet.setText("Pet: " + invoice.getPetName());
        lblBillingPet.setToolTipText(lblBillingPet.getText());
        lblInvoiceNumber.setText("Invoice no: " + invoice.getInvoiceNumber());
        lblPaymentStatus.setText("Status: " + invoice.getStatus());
        if (invoice.isPaid()) { cmbPaymentMethod.setSelectedItem(invoice.getPaymentMethod()); }
        else { cmbPaymentMethod.setSelectedIndex(0); }
        updateInvoiceTable();
        updateBillingControls();
    }

    public void clearLoadedInvoice() {
        loadedInvoice = null;
        invoiceItems.clear();
        clearInvoiceEntry();
        cmbPaymentMethod.setSelectedIndex(0);
        lblBillingOwner.setText("Owner: —");
        lblBillingOwner.setToolTipText(null);
        lblBillingPet.setText("Pet: —");
        lblBillingPet.setToolTipText(null);
        lblInvoiceNumber.setText("Invoice no: —");
        lblPaymentStatus.setText("Status: No consultation loaded");
        updateInvoiceTable();
        updateBillingControls();
    }

    public void addInvoiceItem(InvoiceItem item) {
        invoiceItems.add(item);
        clearInvoiceEntry();
        updateInvoiceTable();
        updateBillingControls();
        cmbBillingService.requestFocusInWindow();
    }

    public void removeSelectedInvoiceItem() {
        int row = tblInvoiceItems.getSelectedRow();
        if (row >= 0) {
            invoiceItems.remove(tblInvoiceItems.convertRowIndexToModel(row));
            updateInvoiceTable();
            updateBillingControls();
        }
    }

    public void clearInvoiceEntry() {
        cmbBillingService.setSelectedIndex(0);
        cmbBillingService.getEditor().setItem("Select service");
        spnBillingQuantity.setValue(1);
        ((JSpinner.DefaultEditor) spnBillingQuantity.getEditor()).getTextField().setValue(1);
        txtBillingUnitPrice.setText("");
    }

    private void updateInvoiceTable() {
        tblInvoiceItems.clearSelection();
        tblInvoiceItems.getSelectionModel().setAnchorSelectionIndex(-1);
        tblInvoiceItems.getSelectionModel().setLeadSelectionIndex(-1);
        DefaultTableModel table = (DefaultTableModel) tblInvoiceItems.getModel();
        table.setRowCount(0);
        BigDecimal total = new BigDecimal("0.00");
        for (InvoiceItem item : invoiceItems) {
            table.addRow(new Object[] {item.getServiceName(), item.getQuantity(),
                item.getUnitPrice().setScale(2).toPlainString(), item.getAmount().setScale(2).toPlainString()});
            total = total.add(item.getAmount());
        }
        lblInvoiceItemCount.setText(invoiceItems.size() + (invoiceItems.size() == 1 ? " item" : " items"));
        lblInvoiceTotal.setText("Total: LKR " + total.setScale(2).toPlainString());
    }

    public boolean isInvoiceEditable() {
        return !billingBusy && loadedInvoice != null
                && ("Unbilled".equals(loadedInvoice.getStatus()) || "Draft".equals(loadedInvoice.getStatus()));
    }

    public void setBillingBusy(boolean busy) {
        billingBusy = busy;
        updateBillingControls();
    }

    private void updateBillingControls() {
        boolean editable = isInvoiceEditable();
        cmbBillingConsultation.setEnabled(!billingBusy && !billingVisits.isEmpty());
        btnLoadBillingConsultation.setEnabled(!billingBusy && getSelectedBillingAppointmentId() != null);
        cmbBillingService.setEnabled(editable);
        spnBillingQuantity.setEnabled(editable);
        txtBillingUnitPrice.setEditable(editable);
        txtBillingUnitPrice.setEnabled(editable);
        txtBillingUnitPrice.setBackground(editable ? Color.WHITE : new Color(245, 247, 250));
        btnAddInvoiceItem.setEnabled(editable);
        btnClearInvoiceItem.setEnabled(editable);
        btnRemoveInvoiceItem.setEnabled(editable && tblInvoiceItems.getSelectedRow() >= 0);
        cmbPaymentMethod.setEnabled(editable);
        btnSaveInvoiceDraft.setEnabled(editable && !invoiceItems.isEmpty());
        btnRecordPayment.setEnabled(editable && !invoiceItems.isEmpty());
        btnPrintReceipt.setEnabled(!billingBusy && loadedInvoice != null && loadedInvoice.isPaid());
        tblInvoiceItems.setEnabled(!billingBusy && loadedInvoice != null);
    }

    public boolean hasPendingInvoiceInput() {
        String service = getBillingService();
        String quantity = ((JSpinner.DefaultEditor) spnBillingQuantity.getEditor()).getTextField().getText().trim();
        return (!service.isEmpty() && !"Select service".equals(service))
                || !getBillingUnitPrice().isEmpty() || !"1".equals(quantity);
    }

    public boolean hasUnsavedBillingChanges() {
        if (loadedInvoice == null || loadedInvoice.isPaid()) { return false; }
        if (hasPendingInvoiceInput() || getPaymentMethod() != null) { return true; }
        List<InvoiceItem> saved = loadedInvoice.getItems();
        if (saved.size() != invoiceItems.size()) { return true; }
        for (int i = 0; i < saved.size(); i++) {
            if (!saved.get(i).sameDetails(invoiceItems.get(i))) { return true; }
        }
        return false;
    }

    public boolean confirmDiscardBillingChanges() {
        return JOptionPane.showConfirmDialog(this,
                "Discard unsaved invoice items and payment selection?",
                "Unsaved invoice", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public boolean confirmRecordPayment(BigDecimal total, String method) {
        return JOptionPane.showConfirmDialog(this,
                "Confirm that the full payment of LKR " + total.toPlainString() + " via " + method
                + " has been received.\nThis will save the invoice and lock it for editing.",
                "Record full payment", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void restoreLoadedBillingSelection() {
        if (loadedInvoice == null) { cmbBillingConsultation.setSelectedIndex(0); return; }
        for (int i = 0; i < billingVisits.size(); i++) {
            if (billingVisits.get(i).getAppointmentId() == loadedInvoice.getAppointmentId()) {
                cmbBillingConsultation.setSelectedIndex(i + 1);
                return;
            }
        }
    }

    public void markInvoiceSaved(Invoice invoice) {
        displayInvoice(invoice);
        for (int i = 0; i < billingVisits.size(); i++) {
            if (billingVisits.get(i).getAppointmentId() == invoice.getAppointmentId()) {
                billingVisits.set(i, invoice);
                cmbBillingConsultation.removeItemAt(i + 1);
                cmbBillingConsultation.insertItemAt(optionText(invoice), i + 1);
                cmbBillingConsultation.setSelectedIndex(i + 1);
                break;
            }
        }
    }

    public void showBillingMessage(String text) {
        lblBillingError.setForeground(new Color(185, 28, 28));
        String message = text == null ? " " : text;
        lblBillingError.setText("<html>" + message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</html>");
        lblBillingError.setToolTipText(text == null || text.trim().isEmpty() ? null : text);
    }

    public void showBillingSuccess(String text) {
        showBillingMessage(text);
        lblBillingError.setForeground(new Color(15, 118, 110));
    }

    public boolean printBillingReceipt(String text) throws PrinterException {
        JTextArea receipt = new JTextArea(text);
        receipt.setFont(new Font("Monospaced", Font.PLAIN, 11));
        receipt.setEditable(false);
        receipt.setLineWrap(true);
        receipt.setWrapStyleWord(true);
        return receipt.print(new MessageFormat("Vetora - Payment receipt"), new MessageFormat("Page {0}"));
    }

    @Override
    public void doLayout(){
        super.doLayout();
        if (!billingSplitReady && splitBilling.getWidth() > 0) {
            int width = splitBilling.getWidth() - splitBilling.getDividerSize();
            int editorWidth = Math.max(360, (int)Math.round(width * 0.382));
            splitBilling.setDividerLocation(Math.max(300, width - editorWidth));
            billingSplitReady = true;
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
        pnlBillingFormGlue = new javax.swing.JPanel();
        pnlPaymentMethodGroup = new javax.swing.JPanel();
        pnlBillingServiceGroup = new javax.swing.JPanel();
        pnlInvoiceMeta = new javax.swing.JPanel();
        pnlBillingEditor = new javax.swing.JPanel();

        pnlBillingToolbar = new javax.swing.JPanel();
        lblBillingSelectConsultation = new javax.swing.JLabel();
        pnlBillingSelectorRow = new javax.swing.JPanel();
        cmbBillingConsultation = new javax.swing.JComboBox<>();
        btnLoadBillingConsultation = new javax.swing.JButton();
        splitBilling = new javax.swing.JSplitPane();
        pnlInvoiceItems = new javax.swing.JPanel();
        pnlInvoiceHolder = new javax.swing.JPanel();
        lblInvoiceItemsTitle = new javax.swing.JLabel();
        pnlBillingPatientSummary = new javax.swing.JPanel();
        lblBillingOwner = new javax.swing.JLabel();
        lblBillingPet = new javax.swing.JLabel();
        scrInvoiceItems = new javax.swing.JScrollPane();
        tblInvoiceItems = new javax.swing.JTable();
        pnlInvoiceSummary = new javax.swing.JPanel();
        pnlInvoiceSummaryRow = new javax.swing.JPanel();
        lblInvoiceItemCount = new javax.swing.JLabel();
        btnRemoveInvoiceItem = new javax.swing.JButton();
        lblInvoiceTotal = new javax.swing.JLabel();
        scrBillingForm = new javax.swing.JScrollPane();
        pnlBillingForm = new javax.swing.JPanel();
        lblBillingFormTitle = new javax.swing.JLabel();
        lblBillingService = new javax.swing.JLabel();
        cmbBillingService = new javax.swing.JComboBox<>();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        pnlBillingAmountRow = new javax.swing.JPanel();
        pnlBillingQuantityGroup = new javax.swing.JPanel();
        lblBillingQuantity = new javax.swing.JLabel();
        spnBillingQuantity = new javax.swing.JSpinner();
        pnlBillingPriceGroup = new javax.swing.JPanel();
        lblBillingUnitPrice = new javax.swing.JLabel();
        txtBillingUnitPrice = new javax.swing.JTextField();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        pnlBillingItemButtons = new javax.swing.JPanel();
        btnAddInvoiceItem = new javax.swing.JButton();
        btnClearInvoiceItem = new javax.swing.JButton();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13));
        jSeparator1 = new javax.swing.JSeparator();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13), new java.awt.Dimension(0, 13));
        lblPaymentTitle = new javax.swing.JLabel();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        lblInvoiceNumber = new javax.swing.JLabel();
        lblPaymentStatus = new javax.swing.JLabel();
        lblPaymentMethod = new javax.swing.JLabel();
        cmbPaymentMethod = new javax.swing.JComboBox<>();
        filler11 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8));
        lblPaymentHint = new javax.swing.JLabel();
        pnlBillingActions = new javax.swing.JPanel();
        lblBillingError = new javax.swing.JLabel();
        pnlBillingButtons = new javax.swing.JPanel();
        btnSaveInvoiceDraft = new javax.swing.JButton();
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0));
        btnPrintReceipt = new javax.swing.JButton();
        filler13 = new javax.swing.Box.Filler(new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0), new java.awt.Dimension(13, 0));
        btnRecordPayment = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 247, 250));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        setLayout(new java.awt.BorderLayout(0, 13));

        pnlBillingToolbar.setBackground(new java.awt.Color(255, 255, 255));
        pnlBillingToolbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 13, 8, 13));
        pnlBillingToolbar.setLayout(new java.awt.BorderLayout(0, 5));
        pnlBillingToolbar.setPreferredSize(new java.awt.Dimension(0, 84));

        lblBillingSelectConsultation.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblBillingSelectConsultation.setText("Finalized consultation");
        lblBillingSelectConsultation.setPreferredSize(new java.awt.Dimension(0, 21));
        lblBillingSelectConsultation.setForeground(new java.awt.Color(71, 85, 105));

        pnlBillingSelectorRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlBillingSelectorRow.setPreferredSize(new java.awt.Dimension(0, 42));
        pnlBillingSelectorRow.setLayout(new java.awt.BorderLayout(13, 0));
        pnlBillingSelectorRow.setMinimumSize(new java.awt.Dimension(0, 42));
        pnlBillingSelectorRow.setMaximumSize(new java.awt.Dimension(32767, 42));
        pnlBillingSelectorRow.setAlignmentX(0.0F);

        cmbBillingConsultation.setFont(new java.awt.Font("Segoe UI", 0, 16));
        cmbBillingConsultation.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select consultation" }));
        cmbBillingConsultation.setPreferredSize(new java.awt.Dimension(233, 42));
        cmbBillingConsultation.setMinimumSize(new java.awt.Dimension(0, 42));
        cmbBillingConsultation.setMaximumSize(new java.awt.Dimension(32767, 42));
        cmbBillingConsultation.setAlignmentX(0.0F);

        btnLoadBillingConsultation.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnLoadBillingConsultation.setText("Load bill");
        btnLoadBillingConsultation.setPreferredSize(new java.awt.Dimension(144, 42));
        btnLoadBillingConsultation.setMinimumSize(new java.awt.Dimension(144, 42));
        btnLoadBillingConsultation.setMaximumSize(new java.awt.Dimension(144, 42));
        btnLoadBillingConsultation.setBackground(new java.awt.Color(255, 255, 255));
        btnLoadBillingConsultation.setForeground(new java.awt.Color(15, 118, 110));



        splitBilling.setDividerSize(5);
        splitBilling.setResizeWeight(0.618);

        pnlInvoiceItems.setBackground(new java.awt.Color(255, 255, 255));
        pnlInvoiceItems.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlInvoiceItems.setMinimumSize(new java.awt.Dimension(300, 0));
        pnlInvoiceItems.setLayout(new java.awt.BorderLayout(0, 13));

        pnlInvoiceHolder.setBackground(new java.awt.Color(255, 255, 255));
        pnlInvoiceHolder.setLayout(new java.awt.BorderLayout(0, 5));

        lblInvoiceItemsTitle.setFont(new java.awt.Font("Segoe UI", 1, 21));
        lblInvoiceItemsTitle.setText("Invoice items");
        lblInvoiceItemsTitle.setPreferredSize(new java.awt.Dimension(0, 26));
        lblInvoiceItemsTitle.setForeground(new java.awt.Color(15, 23, 42));

        pnlBillingPatientSummary.setBackground(new java.awt.Color(255, 255, 255));
        pnlBillingPatientSummary.setPreferredSize(new java.awt.Dimension(0, 21));
        pnlBillingPatientSummary.setLayout(new java.awt.GridLayout(1, 2, 13, 0));

        lblBillingOwner.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblBillingOwner.setText("Owner: —");
        lblBillingOwner.setForeground(new java.awt.Color(71, 85, 105));

        lblBillingPet.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblBillingPet.setText("Pet: —");
        lblBillingPet.setForeground(new java.awt.Color(71, 85, 105));



        tblInvoiceItems.setFont(new java.awt.Font("Segoe UI", 0, 16));
        tblInvoiceItems.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Service", "Qty", "Unit price (LKR)", "Amount (LKR)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
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
        tblInvoiceItems.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblInvoiceItems.setColumnSelectionAllowed(false);
        tblInvoiceItems.setRowSelectionAllowed(true);
        tblInvoiceItems.setFillsViewportHeight(true);
        tblInvoiceItems.setGridColor(new java.awt.Color(226, 232, 240));
        tblInvoiceItems.setRowHeight(34);
        tblInvoiceItems.setSelectionBackground(new java.awt.Color(204, 251, 241));
        tblInvoiceItems.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tblInvoiceItems.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblInvoiceItems.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblInvoiceItems.setShowHorizontalLines(true);
        tblInvoiceItems.setBackground(new java.awt.Color(255, 255, 255));
        tblInvoiceItems.setForeground(new java.awt.Color(15, 23, 42));
        tblInvoiceItems.setShowVerticalLines(false);
        tblInvoiceItems.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tblInvoiceItems.getColumnModel().getColumnCount() > 0) {
            tblInvoiceItems.getColumnModel().getColumn(0).setPreferredWidth(233);
            tblInvoiceItems.getColumnModel().getColumn(1).setPreferredWidth(55);
            tblInvoiceItems.getColumnModel().getColumn(2).setPreferredWidth(144);
            tblInvoiceItems.getColumnModel().getColumn(3).setPreferredWidth(144);
        }


        pnlInvoiceSummary.setBackground(new java.awt.Color(255, 255, 255));
        pnlInvoiceSummary.setLayout(new java.awt.BorderLayout(0, 8));

        pnlInvoiceSummaryRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlInvoiceSummaryRow.setPreferredSize(new java.awt.Dimension(0, 42));
        pnlInvoiceSummaryRow.setLayout(new java.awt.BorderLayout(13, 0));

        lblInvoiceItemCount.setText("0 items");
        lblInvoiceItemCount.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblInvoiceItemCount.setForeground(new java.awt.Color(71, 85, 105));

        btnRemoveInvoiceItem.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnRemoveInvoiceItem.setForeground(new java.awt.Color(185, 28, 28));
        btnRemoveInvoiceItem.setText("Remove item");
        btnRemoveInvoiceItem.setEnabled(false);
        btnRemoveInvoiceItem.setPreferredSize(new java.awt.Dimension(144, 42));
        btnRemoveInvoiceItem.setMinimumSize(new java.awt.Dimension(144, 42));
        btnRemoveInvoiceItem.setMaximumSize(new java.awt.Dimension(144, 42));
        btnRemoveInvoiceItem.setBackground(new java.awt.Color(255, 255, 255));


        lblInvoiceTotal.setFont(new java.awt.Font("Segoe UI", 1, 21));
        lblInvoiceTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblInvoiceTotal.setText("Total: LKR 0.00");
        lblInvoiceTotal.setPreferredSize(new java.awt.Dimension(0, 34));



        scrBillingForm.setBorder(null);
        scrBillingForm.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrBillingForm.setMinimumSize(new java.awt.Dimension(0, 0));
        scrBillingForm.setPreferredSize(new java.awt.Dimension(320, 0));

        pnlBillingForm.setBackground(new java.awt.Color(255, 255, 255));
        pnlBillingForm.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlBillingForm.setLayout(new javax.swing.BoxLayout(pnlBillingForm, javax.swing.BoxLayout.Y_AXIS));

        lblBillingFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 21));
        lblBillingFormTitle.setText("Charges & payment");
        lblBillingFormTitle.setForeground(new java.awt.Color(15, 23, 42));

        lblBillingService.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblBillingService.setText("Service *");
        lblBillingService.setPreferredSize(new java.awt.Dimension(0, 21));
        lblBillingService.setForeground(new java.awt.Color(71, 85, 105));

        cmbBillingService.setEditable(true);
        cmbBillingService.setFont(new java.awt.Font("Segoe UI", 0, 16));
        cmbBillingService.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select service", "Consultation", "Treatment", "Vaccination", "Laboratory test", "Other clinic service" }));
        cmbBillingService.setAlignmentX(0.0F);
        cmbBillingService.setMaximumSize(new java.awt.Dimension(32767, 42));
        cmbBillingService.setMinimumSize(new java.awt.Dimension(0, 42));
        cmbBillingService.setPreferredSize(new java.awt.Dimension(233, 42));

        pnlBillingAmountRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlBillingAmountRow.setAlignmentX(0.0F);
        pnlBillingAmountRow.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlBillingAmountRow.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlBillingAmountRow.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlBillingAmountRow.setLayout(new java.awt.BorderLayout(13, 0));

        pnlBillingQuantityGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlBillingQuantityGroup.setPreferredSize(new java.awt.Dimension(89, 68));
        pnlBillingQuantityGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlBillingQuantityGroup.setMinimumSize(new java.awt.Dimension(89, 68));
        pnlBillingQuantityGroup.setMaximumSize(new java.awt.Dimension(89, 68));
        pnlBillingQuantityGroup.setAlignmentX(0.0F);

        lblBillingQuantity.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblBillingQuantity.setText("Quantity *");
        lblBillingQuantity.setPreferredSize(new java.awt.Dimension(0, 21));
        lblBillingQuantity.setForeground(new java.awt.Color(71, 85, 105));

        spnBillingQuantity.setFont(new java.awt.Font("Segoe UI", 0, 16));
        spnBillingQuantity.setModel(new javax.swing.SpinnerNumberModel(1, 1, 9999, 1));
        spnBillingQuantity.setMinimumSize(new java.awt.Dimension(0, 42));
        spnBillingQuantity.setPreferredSize(new java.awt.Dimension(233, 42));
        spnBillingQuantity.setMaximumSize(new java.awt.Dimension(32767, 42));
        spnBillingQuantity.setAlignmentX(0.0F);


        pnlBillingPriceGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlBillingPriceGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlBillingPriceGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlBillingPriceGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlBillingPriceGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlBillingPriceGroup.setAlignmentX(0.0F);

        lblBillingUnitPrice.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblBillingUnitPrice.setText("Unit price (LKR) *");
        lblBillingUnitPrice.setPreferredSize(new java.awt.Dimension(0, 21));
        lblBillingUnitPrice.setForeground(new java.awt.Color(71, 85, 105));

        txtBillingUnitPrice.setEditable(true);
        txtBillingUnitPrice.setBackground(new java.awt.Color(255, 255, 255));
        txtBillingUnitPrice.setFont(new java.awt.Font("Segoe UI", 0, 16));
        txtBillingUnitPrice.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtBillingUnitPrice.setText("");
        txtBillingUnitPrice.setToolTipText("Enter the clinic price, for example 1500.00");
        txtBillingUnitPrice.setMinimumSize(new java.awt.Dimension(0, 42));
        txtBillingUnitPrice.setPreferredSize(new java.awt.Dimension(233, 42));
        txtBillingUnitPrice.setMaximumSize(new java.awt.Dimension(32767, 42));
        txtBillingUnitPrice.setAlignmentX(0.0F);



        btnAddInvoiceItem.setBackground(new java.awt.Color(15, 118, 110));
        btnAddInvoiceItem.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnAddInvoiceItem.setForeground(new java.awt.Color(255, 255, 255));
        btnAddInvoiceItem.setText("Add item");
        btnAddInvoiceItem.setEnabled(false);
        btnAddInvoiceItem.setMaximumSize(new java.awt.Dimension(32767, 42));
        btnAddInvoiceItem.setMinimumSize(new java.awt.Dimension(0, 42));
        btnAddInvoiceItem.setPreferredSize(new java.awt.Dimension(144, 42));
        pnlBillingItemButtons.setBackground(java.awt.Color.WHITE);
        pnlBillingItemButtons.setAlignmentX(0.0F);
        pnlBillingItemButtons.setMaximumSize(new java.awt.Dimension(32767, 42));
        pnlBillingItemButtons.setMinimumSize(new java.awt.Dimension(0, 42));
        pnlBillingItemButtons.setPreferredSize(new java.awt.Dimension(233, 42));
        pnlBillingItemButtons.setLayout(new java.awt.GridLayout(1, 2, 13, 0));
        btnClearInvoiceItem.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnClearInvoiceItem.setForeground(new java.awt.Color(15, 118, 110));
        btnClearInvoiceItem.setText("Clear fields");
        btnClearInvoiceItem.setEnabled(false);
        btnClearInvoiceItem.setPreferredSize(new java.awt.Dimension(144, 42));
        btnClearInvoiceItem.setBackground(new java.awt.Color(255, 255, 255));

        jSeparator1.setAlignmentX(0.0F);
        jSeparator1.setMaximumSize(new java.awt.Dimension(32767, 1));
        jSeparator1.setMinimumSize(new java.awt.Dimension(0, 1));
        jSeparator1.setPreferredSize(new java.awt.Dimension(233, 1));

        lblPaymentTitle.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblPaymentTitle.setText("Payment details");
        lblPaymentTitle.setMinimumSize(new java.awt.Dimension(0, 21));
        lblPaymentTitle.setPreferredSize(new java.awt.Dimension(233, 21));
        lblPaymentTitle.setMaximumSize(new java.awt.Dimension(32767, 21));
        lblPaymentTitle.setAlignmentX(0.0F);
        lblPaymentTitle.setForeground(new java.awt.Color(71, 85, 105));

        lblInvoiceNumber.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblInvoiceNumber.setText("Invoice no: —");
        lblInvoiceNumber.setForeground(new java.awt.Color(71, 85, 105));

        lblPaymentStatus.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPaymentStatus.setText("Status: No consultation loaded");
        lblPaymentStatus.setForeground(new java.awt.Color(71, 85, 105));

        lblPaymentMethod.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPaymentMethod.setText("Payment method *");
        lblPaymentMethod.setPreferredSize(new java.awt.Dimension(0, 21));
        lblPaymentMethod.setForeground(new java.awt.Color(71, 85, 105));

        cmbPaymentMethod.setFont(new java.awt.Font("Segoe UI", 0, 16));
        cmbPaymentMethod.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select payment method", "Cash", "Card" }));
        cmbPaymentMethod.setAlignmentX(0.0F);
        cmbPaymentMethod.setMaximumSize(new java.awt.Dimension(32767, 42));
        cmbPaymentMethod.setMinimumSize(new java.awt.Dimension(0, 42));
        cmbPaymentMethod.setPreferredSize(new java.awt.Dimension(233, 42));

        lblPaymentHint.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblPaymentHint.setForeground(new java.awt.Color(71, 85, 105));
        lblPaymentHint.setText("<html>Record payment only after the full<br>invoice total has been received.</html>");
        lblPaymentHint.setMinimumSize(new java.awt.Dimension(0, 34));
        lblPaymentHint.setPreferredSize(new java.awt.Dimension(233, 34));
        lblPaymentHint.setMaximumSize(new java.awt.Dimension(32767, 34));
        lblPaymentHint.setAlignmentX(0.0F);

        scrBillingForm.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);



        pnlBillingActions.setBackground(new java.awt.Color(245, 247, 250));
        pnlBillingActions.setPreferredSize(new java.awt.Dimension(0, 42));
        pnlBillingActions.setLayout(new java.awt.BorderLayout(13, 0));

        lblBillingError.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblBillingError.setForeground(new java.awt.Color(185, 28, 28));
        lblBillingError.setText(" ");

        pnlBillingButtons.setBackground(new java.awt.Color(245, 247, 250));
        pnlBillingButtons.setLayout(new javax.swing.BoxLayout(pnlBillingButtons, javax.swing.BoxLayout.X_AXIS));

        btnSaveInvoiceDraft.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnSaveInvoiceDraft.setForeground(new java.awt.Color(15, 118, 110));
        btnSaveInvoiceDraft.setText("Save draft");
        btnSaveInvoiceDraft.setEnabled(false);
        btnSaveInvoiceDraft.setMaximumSize(new java.awt.Dimension(144, 42));
        btnSaveInvoiceDraft.setMinimumSize(new java.awt.Dimension(144, 42));
        btnSaveInvoiceDraft.setPreferredSize(new java.awt.Dimension(144, 42));
        btnSaveInvoiceDraft.setBackground(new java.awt.Color(255, 255, 255));

        btnPrintReceipt.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnPrintReceipt.setForeground(new java.awt.Color(15, 118, 110));
        btnPrintReceipt.setText("Print receipt");
        btnPrintReceipt.setEnabled(false);
        btnPrintReceipt.setMaximumSize(new java.awt.Dimension(144, 42));
        btnPrintReceipt.setMinimumSize(new java.awt.Dimension(144, 42));
        btnPrintReceipt.setPreferredSize(new java.awt.Dimension(144, 42));
        btnPrintReceipt.setBackground(new java.awt.Color(255, 255, 255));

        btnRecordPayment.setBackground(new java.awt.Color(15, 118, 110));
        btnRecordPayment.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnRecordPayment.setForeground(new java.awt.Color(255, 255, 255));
        btnRecordPayment.setText("Record payment");
        btnRecordPayment.setEnabled(false);
        btnRecordPayment.setMaximumSize(new java.awt.Dimension(180, 42));
        btnRecordPayment.setMinimumSize(new java.awt.Dimension(180, 42));
        btnRecordPayment.setPreferredSize(new java.awt.Dimension(180, 42));



        cmbBillingConsultation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbBillingConsultationActionPerformed(evt);
            }
        });
        btnLoadBillingConsultation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadBillingConsultationActionPerformed(evt);
            }
        });
        btnAddInvoiceItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddInvoiceItemActionPerformed(evt);
            }
        });
        btnClearInvoiceItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearInvoiceItemActionPerformed(evt);
            }
        });
        btnRemoveInvoiceItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveInvoiceItemActionPerformed(evt);
            }
        });
        btnSaveInvoiceDraft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveInvoiceDraftActionPerformed(evt);
            }
        });
        btnRecordPayment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRecordPaymentActionPerformed(evt);
            }
        });
        btnPrintReceipt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintReceiptActionPerformed(evt);
            }
        });

        splitBilling.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlBillingEditor.setBackground(new java.awt.Color(255, 255, 255));
        pnlBillingEditor.setLayout(new java.awt.BorderLayout(0, 8));
        pnlBillingEditor.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlBillingEditor.setMinimumSize(new java.awt.Dimension(360, 0));
        pnlInvoiceMeta.setBackground(new java.awt.Color(255, 255, 255));
        pnlInvoiceMeta.setLayout(new java.awt.BorderLayout(13, 0));
        pnlInvoiceMeta.setMinimumSize(new java.awt.Dimension(0, 21));
        pnlInvoiceMeta.setPreferredSize(new java.awt.Dimension(0, 21));
        pnlInvoiceMeta.setMaximumSize(new java.awt.Dimension(32767, 21));
        pnlInvoiceMeta.setAlignmentX(0.0F);
        pnlBillingServiceGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlBillingServiceGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlBillingServiceGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlBillingServiceGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlBillingServiceGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlBillingServiceGroup.setAlignmentX(0.0F);
        pnlPaymentMethodGroup.setBackground(new java.awt.Color(255, 255, 255));
        pnlPaymentMethodGroup.setLayout(new java.awt.BorderLayout(0, 5));
        pnlPaymentMethodGroup.setMinimumSize(new java.awt.Dimension(0, 68));
        pnlPaymentMethodGroup.setPreferredSize(new java.awt.Dimension(233, 68));
        pnlPaymentMethodGroup.setMaximumSize(new java.awt.Dimension(32767, 68));
        pnlPaymentMethodGroup.setAlignmentX(0.0F);
        pnlBillingFormGlue.setBackground(new java.awt.Color(255, 255, 255));
        pnlBillingFormGlue.setLayout(new javax.swing.BoxLayout(pnlBillingFormGlue, javax.swing.BoxLayout.Y_AXIS));
        pnlBillingFormGlue.setMinimumSize(new java.awt.Dimension(0, 0));
        pnlBillingFormGlue.setPreferredSize(new java.awt.Dimension(0, 0));
        pnlBillingFormGlue.setMaximumSize(new java.awt.Dimension(32767, 32767));
        pnlBillingFormGlue.setOpaque(false);
        pnlBillingFormGlue.setAlignmentX(0.0F);

        pnlBillingSelectorRow.add(cmbBillingConsultation, java.awt.BorderLayout.CENTER);
        pnlBillingSelectorRow.add(btnLoadBillingConsultation, java.awt.BorderLayout.EAST);
        pnlBillingToolbar.add(lblBillingSelectConsultation, java.awt.BorderLayout.NORTH);
        pnlBillingToolbar.add(pnlBillingSelectorRow, java.awt.BorderLayout.CENTER);
        pnlBillingPatientSummary.add(lblBillingOwner);
        pnlBillingPatientSummary.add(lblBillingPet);
        pnlInvoiceMeta.add(lblInvoiceNumber, java.awt.BorderLayout.CENTER);
        pnlInvoiceMeta.add(lblPaymentStatus, java.awt.BorderLayout.EAST);
        pnlInvoiceHolder.add(lblInvoiceItemsTitle, java.awt.BorderLayout.NORTH);
        pnlInvoiceHolder.add(pnlBillingPatientSummary, java.awt.BorderLayout.CENTER);
        pnlInvoiceHolder.add(pnlInvoiceMeta, java.awt.BorderLayout.SOUTH);
        scrInvoiceItems.setViewportView(tblInvoiceItems);
        pnlInvoiceSummaryRow.add(lblInvoiceItemCount, java.awt.BorderLayout.CENTER);
        pnlInvoiceSummaryRow.add(btnRemoveInvoiceItem, java.awt.BorderLayout.EAST);
        pnlInvoiceSummary.add(pnlInvoiceSummaryRow, java.awt.BorderLayout.NORTH);
        pnlInvoiceSummary.add(lblInvoiceTotal, java.awt.BorderLayout.CENTER);
        pnlInvoiceItems.add(pnlInvoiceHolder, java.awt.BorderLayout.NORTH);
        pnlInvoiceItems.add(scrInvoiceItems, java.awt.BorderLayout.CENTER);
        pnlInvoiceItems.add(pnlInvoiceSummary, java.awt.BorderLayout.SOUTH);
        pnlBillingServiceGroup.add(lblBillingService, java.awt.BorderLayout.NORTH);
        pnlBillingServiceGroup.add(cmbBillingService, java.awt.BorderLayout.CENTER);
        pnlBillingQuantityGroup.add(lblBillingQuantity, java.awt.BorderLayout.NORTH);
        pnlBillingQuantityGroup.add(spnBillingQuantity, java.awt.BorderLayout.CENTER);
        pnlBillingPriceGroup.add(lblBillingUnitPrice, java.awt.BorderLayout.NORTH);
        pnlBillingPriceGroup.add(txtBillingUnitPrice, java.awt.BorderLayout.CENTER);
        pnlBillingAmountRow.add(pnlBillingQuantityGroup, java.awt.BorderLayout.WEST);
        pnlBillingAmountRow.add(pnlBillingPriceGroup, java.awt.BorderLayout.CENTER);
        pnlBillingItemButtons.add(btnAddInvoiceItem);
        pnlBillingItemButtons.add(btnClearInvoiceItem);
        pnlPaymentMethodGroup.add(lblPaymentMethod, java.awt.BorderLayout.NORTH);
        pnlPaymentMethodGroup.add(cmbPaymentMethod, java.awt.BorderLayout.CENTER);
        pnlBillingForm.add(pnlBillingServiceGroup);
        pnlBillingForm.add(filler3);
        pnlBillingForm.add(pnlBillingAmountRow);
        pnlBillingForm.add(filler4);
        pnlBillingForm.add(pnlBillingItemButtons);
        pnlBillingForm.add(filler5);
        pnlBillingForm.add(jSeparator1);
        pnlBillingForm.add(filler6);
        pnlBillingForm.add(lblPaymentTitle);
        pnlBillingForm.add(filler7);
        pnlBillingForm.add(pnlPaymentMethodGroup);
        pnlBillingForm.add(filler11);
        pnlBillingForm.add(lblPaymentHint);
        pnlBillingForm.add(pnlBillingFormGlue);
        scrBillingForm.setViewportView(pnlBillingForm);
        pnlBillingEditor.add(lblBillingFormTitle, java.awt.BorderLayout.NORTH);
        pnlBillingEditor.add(scrBillingForm, java.awt.BorderLayout.CENTER);
        splitBilling.setLeftComponent(pnlInvoiceItems);
        splitBilling.setRightComponent(pnlBillingEditor);
        pnlBillingButtons.add(btnSaveInvoiceDraft);
        pnlBillingButtons.add(filler12);
        pnlBillingButtons.add(btnPrintReceipt);
        pnlBillingButtons.add(filler13);
        pnlBillingButtons.add(btnRecordPayment);
        pnlBillingActions.add(lblBillingError, java.awt.BorderLayout.CENTER);
        pnlBillingActions.add(pnlBillingButtons, java.awt.BorderLayout.EAST);
        add(pnlBillingToolbar, java.awt.BorderLayout.NORTH);
        add(splitBilling, java.awt.BorderLayout.CENTER);
        add(pnlBillingActions, java.awt.BorderLayout.SOUTH);
        tblInvoiceItems.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        tblInvoiceItems.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        tblInvoiceItems.getTableHeader().setReorderingAllowed(false);
        scrInvoiceItems.setColumnHeaderView(tblInvoiceItems.getTableHeader());
        scrInvoiceItems.getViewport().setBackground(java.awt.Color.WHITE);
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        revalidate();
    }//GEN-LAST:event_formComponentShown



    private void cmbBillingConsultationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbBillingConsultationActionPerformed
        if (controller != null) { controller.consultationChanged(); }
    }//GEN-LAST:event_cmbBillingConsultationActionPerformed

    private void btnLoadBillingConsultationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadBillingConsultationActionPerformed
        if (controller != null) { controller.loadInvoice(); }
    }//GEN-LAST:event_btnLoadBillingConsultationActionPerformed

    private void btnAddInvoiceItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddInvoiceItemActionPerformed
        if (controller != null) { controller.addInvoiceItem(); }
    }//GEN-LAST:event_btnAddInvoiceItemActionPerformed

    private void btnClearInvoiceItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearInvoiceItemActionPerformed
        if (controller != null) { controller.clearInvoiceItem(); }
    }//GEN-LAST:event_btnClearInvoiceItemActionPerformed

    private void btnRemoveInvoiceItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveInvoiceItemActionPerformed
        if (controller != null) { controller.removeInvoiceItem(); }
    }//GEN-LAST:event_btnRemoveInvoiceItemActionPerformed

    private void btnSaveInvoiceDraftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveInvoiceDraftActionPerformed
        if (controller != null) { controller.saveDraft(); }
    }//GEN-LAST:event_btnSaveInvoiceDraftActionPerformed

    private void btnRecordPaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecordPaymentActionPerformed
        if (controller != null) { controller.recordPayment(); }
    }//GEN-LAST:event_btnRecordPaymentActionPerformed

    private void btnPrintReceiptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintReceiptActionPerformed
        if (controller != null) { controller.printReceipt(); }
    }//GEN-LAST:event_btnPrintReceiptActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnlBillingFormGlue;
    private javax.swing.JPanel pnlPaymentMethodGroup;
    private javax.swing.JPanel pnlBillingServiceGroup;
    private javax.swing.JPanel pnlInvoiceMeta;
    private javax.swing.JPanel pnlBillingEditor;
    private javax.swing.JPanel pnlBillingItemButtons;
    private javax.swing.JButton btnClearInvoiceItem;
    private javax.swing.JButton btnAddInvoiceItem;
    private javax.swing.JButton btnLoadBillingConsultation;
    private javax.swing.JButton btnPrintReceipt;
    private javax.swing.JButton btnRecordPayment;
    private javax.swing.JButton btnRemoveInvoiceItem;
    private javax.swing.JButton btnSaveInvoiceDraft;
    private javax.swing.JComboBox<String> cmbBillingConsultation;
    private javax.swing.JComboBox<String> cmbBillingService;
    private javax.swing.JComboBox<String> cmbPaymentMethod;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler13;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblBillingError;
    private javax.swing.JLabel lblBillingFormTitle;
    private javax.swing.JLabel lblBillingOwner;
    private javax.swing.JLabel lblBillingPet;
    private javax.swing.JLabel lblBillingQuantity;
    private javax.swing.JLabel lblBillingSelectConsultation;
    private javax.swing.JLabel lblBillingService;
    private javax.swing.JLabel lblBillingUnitPrice;
    private javax.swing.JLabel lblInvoiceItemCount;
    private javax.swing.JLabel lblInvoiceItemsTitle;
    private javax.swing.JLabel lblInvoiceNumber;
    private javax.swing.JLabel lblInvoiceTotal;
    private javax.swing.JLabel lblPaymentHint;
    private javax.swing.JLabel lblPaymentMethod;
    private javax.swing.JLabel lblPaymentStatus;
    private javax.swing.JLabel lblPaymentTitle;
    private javax.swing.JPanel pnlBillingActions;
    private javax.swing.JPanel pnlBillingAmountRow;
    private javax.swing.JPanel pnlBillingButtons;
    private javax.swing.JPanel pnlBillingForm;
    private javax.swing.JPanel pnlBillingPatientSummary;
    private javax.swing.JPanel pnlBillingPriceGroup;
    private javax.swing.JPanel pnlBillingQuantityGroup;
    private javax.swing.JPanel pnlBillingSelectorRow;
    private javax.swing.JPanel pnlBillingToolbar;
    private javax.swing.JPanel pnlInvoiceHolder;
    private javax.swing.JPanel pnlInvoiceItems;
    private javax.swing.JPanel pnlInvoiceSummary;
    private javax.swing.JPanel pnlInvoiceSummaryRow;
    private javax.swing.JScrollPane scrBillingForm;
    private javax.swing.JScrollPane scrInvoiceItems;
    private javax.swing.JSplitPane splitBilling;
    private javax.swing.JSpinner spnBillingQuantity;
    private javax.swing.JTable tblInvoiceItems;
    private javax.swing.JTextField txtBillingUnitPrice;
    // End of variables declaration//GEN-END:variables
}
