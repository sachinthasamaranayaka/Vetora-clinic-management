/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package vetora.view;

import vetora.util.AccessControl;

import java.awt.BorderLayout;
import java.io.File;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;
import vetora.controller.ReportController;
import vetora.service.ReportService;

/**
 *
 * @author ASUS
 */
public class PanelReports extends javax.swing.JPanel {

    /**
     * Creates new form PanelReports
     */
    public PanelReports() {
        this(new AccessControl(null));
    }

    public PanelReports(AccessControl accessControl) {
        initComponents();
        
        ReportController controller = new ReportController(this, new ReportService(), accessControl);
        
        if (!accessControl.canViewPaymentReports()) {
            cmbReportType.removeItem("Payment Summary");
        }
        cmbReportType.addActionListener(e -> controller.reportSelectionChanged());
        spnReportFrom.addChangeListener(e -> controller.reportSelectionChanged());
        spnReportTo.addChangeListener(e -> controller.reportSelectionChanged());
        
        btnGenerateReport.addActionListener(e -> controller.generate());
        btnExportPdf.addActionListener(e -> controller.exportPdf());
        btnGenerateReport.setEnabled(accessControl.canViewClinicalReports());
        btnExportPdf.setEnabled(false);
        lblReportStatus.setText("Ready to generate report.");
        
        JSpinner.DateEditor fromEditor = new JSpinner.DateEditor(spnReportFrom, "yyyy-MM-dd");
        fromEditor.getFormat().setLenient(false);
        spnReportFrom.setEditor(fromEditor);
        
        JSpinner.DateEditor toEditor = new JSpinner.DateEditor(spnReportTo, "yyyy-MM-dd");
        toEditor.getFormat().setLenient(false);
        spnReportTo.setEditor(toEditor);
        
        fromEditor.getTextField().setFocusLostBehavior(JFormattedTextField.PERSIST);
        toEditor.getTextField().setFocusLostBehavior(JFormattedTextField.PERSIST);
        
        setReportBusy(false, false);
    }
    
    public void setReportBusy(boolean busy, boolean hasReport) {
        boolean paymentReport = "Payment Summary".equals(getSelectedReportType());
        boolean validReport = "Consultation History".equals(getSelectedReportType()) || paymentReport;
        cmbReportType.setEnabled(!busy);
        pnlReportFilters.setVisible(paymentReport);
        spnReportFrom.setEnabled(!busy && paymentReport);
        spnReportTo.setEnabled(!busy && paymentReport);
        btnGenerateReport.setEnabled(!busy && validReport);
        btnExportPdf.setEnabled(!busy && hasReport);
        pnlReportToolbar.revalidate();
        pnlReportToolbar.repaint();
    }

    public void setReportStatus(String message) {
        lblReportStatus.setText(message);
        lblReportStatus.setToolTipText(message);
    }
    
    public String getSelectedReportType() {
        return (String) cmbReportType.getSelectedItem();
    }
    
    public LocalDate getReportFromDate() throws ParseException {
        spnReportFrom.commitEdit();
        Date date =(Date) spnReportFrom.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    
    public LocalDate getReportToDate() throws ParseException {
        spnReportTo.commitEdit();
        Date date = (Date) spnReportTo.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void showReport(JasperPrint report) {
        pnlReportPreview.removeAll();
        pnlReportPreview.add(new JRViewer(report), BorderLayout.CENTER);
        pnlReportPreview.revalidate();
        pnlReportPreview.repaint();
    }

    public void showReportError(String message, Throwable error) {
        String details = error == null ? "Unknown error" : error.getMessage();
        setReportStatus(message);
        JOptionPane.showMessageDialog(this, message + "\n" + details, "Vetora Reports", JOptionPane.ERROR_MESSAGE);
    }

    public File choosePdfDestination() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Report PDF");
        chooser.setSelectedFile(new File("Payment Summary".equals(getSelectedReportType()) ? "Vetora_Payment_Summary.pdf" : "Vetora_Consultation_History.pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File selected = chooser.getSelectedFile();
        if (!selected.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
            selected = new File(selected.getParentFile(), selected.getName() + ".pdf");
        }
        if (selected.exists() && JOptionPane.showConfirmDialog(this,"Replace existing file?", "Confirm overwrite", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return null;
        }
        return selected;
    }
    
    public void clearReportPreview() {
        pnlReportPreview.removeAll();
        pnlReportPreview.revalidate();
        pnlReportPreview.repaint();
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlReportToolbar = new javax.swing.JPanel();
        lblReportType = new javax.swing.JLabel();
        pnlReportSelectorRow = new javax.swing.JPanel();
        pnlReportButtons = new javax.swing.JPanel();
        btnExportPdf = new javax.swing.JButton();
        btnGenerateReport = new javax.swing.JButton();
        cmbReportType = new javax.swing.JComboBox<>();
        pnlReportFilters = new javax.swing.JPanel();
        lblReportFrom = new javax.swing.JLabel();
        spnReportFrom = new javax.swing.JSpinner();
        lblReportTo = new javax.swing.JLabel();
        spnReportTo = new javax.swing.JSpinner();
        pnlReportCard = new javax.swing.JPanel();
        pnlReportHeading = new javax.swing.JPanel();
        lblReportPreviewTitle = new javax.swing.JLabel();
        lblReportHint = new javax.swing.JLabel();
        pnlReportPreview = new javax.swing.JPanel();
        lblReportEmpty = new javax.swing.JLabel();
        lblReportStatus = new javax.swing.JLabel();

        setBackground(new java.awt.Color(245, 247, 250));
        setLayout(new java.awt.BorderLayout(0, 13));

        pnlReportToolbar.setBackground(new java.awt.Color(255, 255, 255));
        pnlReportToolbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 13, 8, 13));
        pnlReportToolbar.setLayout(new java.awt.BorderLayout(13, 5));

        lblReportType.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblReportType.setForeground(new java.awt.Color(71, 85, 105));
        lblReportType.setText("Report");
        lblReportType.setMaximumSize(new java.awt.Dimension(32767, 21));
        lblReportType.setMinimumSize(new java.awt.Dimension(0, 21));
        lblReportType.setPreferredSize(new java.awt.Dimension(233, 21));
        pnlReportToolbar.add(lblReportType, java.awt.BorderLayout.NORTH);

        pnlReportSelectorRow.setBackground(new java.awt.Color(255, 255, 255));
        pnlReportSelectorRow.setLayout(new java.awt.BorderLayout(13, 0));

        pnlReportButtons.setBackground(new java.awt.Color(255, 255, 255));
        pnlReportButtons.setPreferredSize(new java.awt.Dimension(373, 42));
        pnlReportButtons.setLayout(new java.awt.GridLayout(1, 2, 13, 0));

        btnExportPdf.setText("Export PDF");
        btnExportPdf.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnExportPdf.setForeground(new java.awt.Color(15, 118, 110));
        btnExportPdf.setMinimumSize(new java.awt.Dimension(180, 42));
        btnExportPdf.setPreferredSize(new java.awt.Dimension(180, 42));
        btnExportPdf.setMaximumSize(new java.awt.Dimension(180, 42));
        pnlReportButtons.add(btnExportPdf);

        btnGenerateReport.setText("Generate report");
        btnGenerateReport.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnGenerateReport.setBackground(new java.awt.Color(15, 118, 110));
        btnGenerateReport.setForeground(new java.awt.Color(255, 255, 255));
        btnGenerateReport.setMinimumSize(new java.awt.Dimension(180, 42));
        btnGenerateReport.setPreferredSize(new java.awt.Dimension(180, 42));
        btnGenerateReport.setMaximumSize(new java.awt.Dimension(180, 42));
        pnlReportButtons.add(btnGenerateReport);

        pnlReportSelectorRow.add(pnlReportButtons, java.awt.BorderLayout.EAST);

        cmbReportType.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Report Type", "Consultation History", "Payment Summary" }));
        cmbReportType.setAlignmentX(0.0F);
        cmbReportType.setMaximumSize(new java.awt.Dimension(32767, 42));
        cmbReportType.setMinimumSize(new java.awt.Dimension(0, 42));
        cmbReportType.setPreferredSize(new java.awt.Dimension(233, 42));
        cmbReportType.addActionListener(this::cmbReportTypeActionPerformed);
        pnlReportSelectorRow.add(cmbReportType, java.awt.BorderLayout.CENTER);

        pnlReportToolbar.add(pnlReportSelectorRow, java.awt.BorderLayout.CENTER);

        pnlReportFilters.setBackground(new java.awt.Color(255, 255, 255));
        pnlReportFilters.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 13, 8));

        lblReportFrom.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblReportFrom.setText("From date");
        pnlReportFilters.add(lblReportFrom);

        spnReportFrom.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        spnReportFrom.setModel(new javax.swing.SpinnerDateModel());
        spnReportFrom.setPreferredSize(new java.awt.Dimension(144, 42));
        pnlReportFilters.add(spnReportFrom);

        lblReportTo.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblReportTo.setText("To date");
        pnlReportFilters.add(lblReportTo);

        spnReportTo.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        spnReportTo.setModel(new javax.swing.SpinnerDateModel());
        spnReportTo.setPreferredSize(new java.awt.Dimension(144, 42));
        pnlReportFilters.add(spnReportTo);

        pnlReportToolbar.add(pnlReportFilters, java.awt.BorderLayout.SOUTH);

        add(pnlReportToolbar, java.awt.BorderLayout.NORTH);

        pnlReportCard.setBackground(new java.awt.Color(255, 255, 255));
        pnlReportCard.setBorder(javax.swing.BorderFactory.createEmptyBorder(13, 13, 13, 13));
        pnlReportCard.setLayout(new java.awt.BorderLayout(0, 13));

        pnlReportHeading.setBackground(new java.awt.Color(255, 255, 255));
        pnlReportHeading.setLayout(new java.awt.BorderLayout(0, 5));

        lblReportPreviewTitle.setText("Report preview");
        lblReportPreviewTitle.setFont(new java.awt.Font("Segoe UI", 1, 21)); // NOI18N
        lblReportPreviewTitle.setForeground(new java.awt.Color(15, 23, 42));
        pnlReportHeading.add(lblReportPreviewTitle, java.awt.BorderLayout.NORTH);

        lblReportHint.setText("Generate consultation history to preview or export a PDF.");
        lblReportHint.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblReportHint.setForeground(new java.awt.Color(71, 85, 105));
        pnlReportHeading.add(lblReportHint, java.awt.BorderLayout.CENTER);

        pnlReportCard.add(pnlReportHeading, java.awt.BorderLayout.NORTH);

        pnlReportPreview.setBackground(new java.awt.Color(255, 255, 255));
        pnlReportPreview.setLayout(new java.awt.BorderLayout());

        lblReportEmpty.setText("Generate a report to see the preview here.");
        lblReportEmpty.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblReportEmpty.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblReportEmpty.setForeground(new java.awt.Color(71, 85, 105));
        pnlReportPreview.add(lblReportEmpty, java.awt.BorderLayout.CENTER);

        pnlReportCard.add(pnlReportPreview, java.awt.BorderLayout.CENTER);

        add(pnlReportCard, java.awt.BorderLayout.CENTER);

        lblReportStatus.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblReportStatus.setForeground(new java.awt.Color(71, 85, 105));
        lblReportStatus.setText("Ready to generate report.");
        lblReportStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 13, 0, 13));
        lblReportStatus.setMaximumSize(new java.awt.Dimension(32767, 42));
        lblReportStatus.setMinimumSize(new java.awt.Dimension(0, 42));
        lblReportStatus.setPreferredSize(new java.awt.Dimension(0, 42));
        add(lblReportStatus, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void cmbReportTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbReportTypeActionPerformed
        
    }//GEN-LAST:event_cmbReportTypeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExportPdf;
    private javax.swing.JButton btnGenerateReport;
    private javax.swing.JComboBox<String> cmbReportType;
    private javax.swing.JLabel lblReportEmpty;
    private javax.swing.JLabel lblReportFrom;
    private javax.swing.JLabel lblReportHint;
    private javax.swing.JLabel lblReportPreviewTitle;
    private javax.swing.JLabel lblReportStatus;
    private javax.swing.JLabel lblReportTo;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JPanel pnlReportButtons;
    private javax.swing.JPanel pnlReportCard;
    private javax.swing.JPanel pnlReportFilters;
    private javax.swing.JPanel pnlReportHeading;
    private javax.swing.JPanel pnlReportPreview;
    private javax.swing.JPanel pnlReportSelectorRow;
    private javax.swing.JPanel pnlReportToolbar;
    private javax.swing.JSpinner spnReportFrom;
    private javax.swing.JSpinner spnReportTo;
    // End of variables declaration//GEN-END:variables
}
