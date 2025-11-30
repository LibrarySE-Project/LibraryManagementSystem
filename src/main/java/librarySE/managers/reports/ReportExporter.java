package librarySE.managers.reports;

import librarySE.managers.User;
import librarySE.core.MaterialType;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Map;

/**
 * Responsible for exporting library fine-related reports into text or CSV formats.
 * <p>
 * This class works in collaboration with {@link FineReportService} to retrieve
 * fine data per user and media type, and then formats it into human-readable reports.
 * </p>
 *
 * <p>Supported output types:</p>
 * <ul>
 *     <li>CSV string format (in-memory) via {@link #generateFinesReport(LocalDate)}</li>
 *     <li>Physical CSV file export via {@link #exportFinesReportToCsv(LocalDate)}</li>
 * </ul>
 *
 * <p>Reports are automatically saved under the directory:
 * <code>library_data/reports/</code></p>
 *
 * <p><strong>Example Output:</strong></p>
 * <pre>
 * User,Total Fines,Book,CD,Journal
 * Alice,12.50,10.00,2.50,0.00
 * Bob,0.00,0.00,0.00,0.00
 * </pre>
 *
 * @author Eman
 */
public class ReportExporter {

    /** The fine reporting service that provides aggregated fine data. */
    private final FineReportService fineReportService;

    /**
     * Constructs a new {@link ReportExporter} linked to the given {@link FineReportService}.
     *
     * @param fineReportService the reporting service that provides fine summaries
     * @throws IllegalArgumentException if {@code fineReportService} is {@code null}
     */
    public ReportExporter(FineReportService fineReportService) {
        if (fineReportService == null)
            throw new IllegalArgumentException("FineReportService cannot be null.");
        this.fineReportService = fineReportService;
    }

    /**
     * Generates a fines report as a CSV-formatted string.
     * <p>
     * The report includes:
     * <ul>
     *     <li>Username</li>
     *     <li>Total fines</li>
     *     <li>Breakdown by material type (Book, CD, Journal)</li>
     * </ul>
     * </p>
     *
     * @param date the reference date for calculating fines
     * @return a CSV text representation of the fines report
     * @throws IllegalArgumentException if {@code date} is {@code null}
     */
    public String generateFinesReport(LocalDate date) {
        if (date == null)
            throw new IllegalArgumentException("Date cannot be null.");

        StringBuilder sb = new StringBuilder("User,Total Fines,Book,CD,Journal\n");
        Map<User, BigDecimal> totals = fineReportService.getTotalFinesForAllUsers(date);

        for (User user : totals.keySet()) {
            BigDecimal total = totals.get(user);
            Map<MaterialType, BigDecimal> byType = fineReportService.getFinesByMediaType(user, date);

            sb.append(user.getUsername()).append(",")
              .append(total).append(",")
              .append(byType.getOrDefault(MaterialType.BOOK, BigDecimal.ZERO)).append(",")
              .append(byType.getOrDefault(MaterialType.CD, BigDecimal.ZERO)).append(",")
              .append(byType.getOrDefault(MaterialType.JOURNAL, BigDecimal.ZERO)).append("\n");
        }

        return sb.toString();
    }

    /**
     * Exports the fines report to a physical CSV file inside the reports directory.
     * <p>
     * The file name format is: <code>fines_YYYY-MM-DD.csv</code>
     * and it is automatically created inside <code>library_data/reports/</code>.
     * </p>
     *
     * <p>If the directory does not exist, it will be created automatically.</p>
     *
     * @param date the reference date for the report
     * @throws IllegalArgumentException if {@code date} is {@code null}
     * @throws RuntimeException if file creation or writing fails
     */
    public void exportFinesReportToCsv(LocalDate date) {
        if (date == null)
            throw new IllegalArgumentException("Date cannot be null.");

        Path reportsDir = Paths.get("library_data", "reports");
        try {
            if (!Files.exists(reportsDir))
                Files.createDirectories(reportsDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create reports directory.", e);
        }

        Path file = reportsDir.resolve("fines_" + date + ".csv");
        String report = generateFinesReport(date);

        try {
            Files.writeString(file, report,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report: " + file, e);
        }
    }
}
