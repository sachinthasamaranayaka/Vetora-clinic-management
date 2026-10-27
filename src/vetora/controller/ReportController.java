package vetora.controller;

import vetora.util.AccessControl;

import java.io.File;
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

    public ReportController(PanelReports view, ReportService service, AccessControl accessControl) {
        if (accessControl == null) {
            throw new IllegalArgumentException("Access control is required.");
        }
        this.accessControl = accessControl;
        this.view = view;
        this.service = service;
    }

    public void generate() {
        if (!accessControl.canViewClinicalReports()) {
            view.setReportStatus("You do not have permission to perform this action.");
            return;
        }
        if (busy){
            return; 
        }
        generatedReport = null;
        busy = true;
        view.setReportBusy(true, false);
        view.setReportStatus("Generating consultation history...");

        new SwingWorker<JasperPrint, Void>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return service.consultationHistory();
            }

            @Override
            protected void done() {
                try {
                    generatedReport = get();
                    view.showReport(generatedReport);
                    view.setReportStatus("Report generated successfully.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showReportError("Report generation was interrupted.", ex);
                } catch (ExecutionException ex) {
                    view.showReportError("Could not generate the report.", ex.getCause());
                } finally {
                    busy = false;
                    view.setReportBusy(false, generatedReport != null);
                }
            }
        }.execute();
    }

    public void exportPdf() {
        if (!accessControl.canViewClinicalReports()) {
            view.setReportStatus("You do not have permission to perform this action.");
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
}
