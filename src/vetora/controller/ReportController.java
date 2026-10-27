package vetora.controller;

import vetora.util.AccessControl;

import java.io.File;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import net.sf.jasperreports.engine.JasperPrint;
import vetora.service.ReportService;
import vetora.view.PanelReports;

/** Runs report generation/export without freezing the Swing event thread. */
public class ReportController {
    private final AccessControl accessControl;

    private final PanelReports view;
    private final ReportService service;
    private JasperPrint generatedReport;
    private boolean busy;
    private boolean generatedPaymentReport;

    public ReportController(PanelReports view, ReportService service, AccessControl accessControl) {
        if (accessControl == null) {
            throw new IllegalArgumentException("Access control is required.");
        }
        this.accessControl = accessControl;
        this.view = view;
        this.service = service;
    }

    public void generate() {
        if (busy) {
            return;
        }

        String reportType = view.getSelectedReportType();
        boolean paymentReport = "Payment Summary".equals(reportType);

        if (!paymentReport && !"Consultation History".equals(reportType)) {
            view.setReportStatus("Select a report type.");
            return;
        }

        boolean allowed = paymentReport ? accessControl.canViewPaymentReports() : accessControl.canViewClinicalReports();

        if (!allowed) {
            view.setReportStatus("You do not have permission to generate this report.");
            return;
        }

        LocalDate fromDate = null;
        LocalDate toDate = null;

        if (paymentReport) {
            try {
                fromDate = view.getReportFromDate();
                toDate = view.getReportToDate();
            }
            catch (java.text.ParseException ex) {
                view.setReportStatus("Enter valid report dates.");
                return;
            }

            if (fromDate.isAfter(toDate)) {
                view.setReportStatus("From date cannot be after To date.");
                return;
            }
        }

        final LocalDate selectedFromDate = fromDate;
        final LocalDate selectedToDate = toDate;

        generatedReport = null;
        busy = true;
        view.setReportBusy(true, false);
        view.clearReportPreview();
        view.setReportStatus("Generating " + reportType + "...");

        new SwingWorker<JasperPrint, Void>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                if (paymentReport) {
                    return service.paymentSummary(selectedFromDate, selectedToDate);
                }

                return service.consultationHistory();
            }

            @Override
            protected void done() {
                try {
                    generatedReport = get();
                    generatedPaymentReport = paymentReport;
                    view.showReport(generatedReport);
                    view.setReportStatus(reportType + " generated successfully.");
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showReportError("Report generation was interrupted.", ex);
                }
                catch (ExecutionException ex) {
                    view.showReportError("Could not generate the report.", ex.getCause());
                }
                finally {
                    busy = false;
                    view.setReportBusy(false, generatedReport != null);
                }
            }
        }.execute();
    }

    public void exportPdf() {
        
        boolean allowed = generatedPaymentReport ? accessControl.canViewPaymentReports() : accessControl.canViewClinicalReports();

        if (!allowed) {
            view.setReportStatus("You do not have permission to export this report.");
            return;
        }
        
        if (busy || generatedReport == null) return;
        File output = view.choosePdfDestination();
        if (output == null) return;

        busy = true;
        view.setReportBusy(true, true);
        view.setReportStatus("Exporting PDF...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.exportPdf(generatedReport, output);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    view.setReportStatus("PDF saved: " + output.getAbsolutePath());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showReportError("PDF export was interrupted.", ex);
                } catch (ExecutionException ex) {
                    view.showReportError("Could not export PDF.", ex.getCause());
                } finally {
                    busy = false;
                    view.setReportBusy(false, true);
                }
            }
        }.execute();
    }
    
    public void reportSelectionChanged() {
        if (busy) {
            return;
        }
        
        generatedReport = null;
        view.clearReportPreview();
        view.setReportBusy(false, false);
        view.setReportStatus("Click Genarate to view the selected report.");
    }
}
