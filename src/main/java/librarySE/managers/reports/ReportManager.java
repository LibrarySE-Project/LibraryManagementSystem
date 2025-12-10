package librarySE.managers.reports;


import librarySE.managers.BorrowRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * Acts as a unified facade that centralizes access to all library reporting services.
 * <p>
 * The {@code ReportManager} provides a single entry point for:
 * <ul>
 *     <li>Financial reporting through {@link FineReportService}</li>
 *     <li>Activity-based reporting through {@link ActivityReportService}</li>
 *     <li>Export operations through {@link ReportExporter}</li>
 * </ul>
 * </p>
 *
 * <p>This design follows the <strong>Facade Pattern</strong>,
 * simplifying how higher-level classes interact with different reporting modules.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * ReportManager manager = new ReportManager(borrowRecords);
 * manager.fines().getTotalFinesForAllUsers(LocalDate.now());
 * manager.activity().getTopBorrowers();
 * manager.exportFinesCsv(LocalDate.now());
 * </pre>
 *
 * @author Malak
 * 
 */
public class ReportManager {

    /** Handles fine-related calculations and summaries. */
    private final FineReportService fineService;

    /** Provides activity-based reports such as top borrowers and most borrowed items. */
    private final ActivityReportService activityService;

    /** Responsible for exporting reports to CSV or other file formats. */
    private final ReportExporter exporter;

    /**
     * Constructs a unified report manager for a given list of borrowing records.
     *
     * @param borrowRecords list of all borrowing records in the system
     * @throws IllegalArgumentException if {@code borrowRecords} is {@code null}
     */
    public ReportManager(List<BorrowRecord> borrowRecords) {
        if (borrowRecords == null)
            throw new IllegalArgumentException("Borrow records cannot be null.");

        this.fineService = new FineReportService(borrowRecords);
        this.activityService = new ActivityReportService(borrowRecords);
        this.exporter = new ReportExporter(fineService);
    }

    /**
     * Provides access to fine-related reports.
     *
     * @return the {@link FineReportService} instance
     */
    public FineReportService fines() {
        return fineService;
    }

    /**
     * Provides access to activity-based reports.
     *
     * @return the {@link ActivityReportService} instance
     */
    public ActivityReportService activity() {
        return activityService;
    }

    /**
     * Provides access to report exporting operations.
     *
     * @return the {@link ReportExporter} instance
     */
    public ReportExporter exporter() {
        return exporter;
    }

    /**
     * High-level unified call that directly exports a fines report to a CSV file.
     * <p>Internally delegates to {@link ReportExporter#exportFinesReportToCsv(LocalDate)}.</p>
     *
     * @param date the date for which the fines report should be generated
     * @throws IllegalArgumentException if {@code date} is {@code null}
     */
    public void exportFinesCsv(LocalDate date) {
        exporter.exportFinesReportToCsv(date);
    }
}
