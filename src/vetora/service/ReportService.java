package vetora.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import vetora.config.DBConnection;


public class ReportService {

    public JasperPrint consultationHistory() throws IOException, SQLException, JRException {
        String resource = "/vetora/reports/ConsultationHistory.jasper";
        try (InputStream jasper = ReportService.class.getResourceAsStream(resource)) {
            if (jasper == null) {
                throw new IOException("Compiled report not found: " + resource);
            }
            try (Connection connection = DBConnection.getConnection()) {
                return JasperFillManager.fillReport(jasper, new HashMap<>(), connection);
            }
        }
    }

    public void exportPdf(JasperPrint report, File file) throws JRException {
        if (report == null) {
            throw new IllegalArgumentException("Generate a report first.");
        }
        JasperExportManager.exportReportToPdfFile(report, file.getAbsolutePath());
    }
    
    public JasperPrint paymentSummary(LocalDate fromDate, LocalDate toDate) throws IOException, SQLException, JRException {

        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("Select both report dates.");
        }

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date cannot be after To date.");
        }

        String resource = "/vetora/reports/PaymentSummary.jasper";

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("FROM_DATE", java.sql.Date.valueOf(fromDate));
        parameters.put("TO_DATE", java.sql.Date.valueOf(toDate));

        try (InputStream jasper = ReportService.class.getResourceAsStream(resource)) {

            if (jasper == null) {
                throw new IOException("Compiled report not found: " + resource);
            }

            try (Connection connection = DBConnection.getConnection()) {
                return JasperFillManager.fillReport(jasper, parameters, connection);
            }
        }
    }
}
