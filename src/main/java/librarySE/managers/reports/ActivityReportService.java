package librarySE.managers.reports;

import librarySE.core.LibraryItem;
import librarySE.managers.BorrowRecord;
import librarySE.managers.User;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Service class that provides analytical reporting features for library activity.
 *
 * <p>
 * {@code ActivityReportService} processes historical {@link BorrowRecord} data to
 * compute high-level insights about how the library is being used, such as:
 * </p>
 * <ul>
 *     <li>Which users borrow the most items (top borrowers).</li>
 *     <li>Which materials (books, CDs, journals) are borrowed most frequently.</li>
 *     <li>Which items are overdue for a specific user at a given reference date.</li>
 * </ul>
 *
 * <h3>Thread-Safety and Data Model</h3>
 * <p>
 * Internally, the service stores a snapshot of borrow records in a
 * {@link CopyOnWriteArrayList}, which provides:
 * </p>
 * <ul>
 *     <li>Safe iteration while other threads may read concurrently.</li>
 *     <li>Stable, snapshot-like behavior for reporting scenarios.</li>
 * </ul>
 *
 * <p>
 * The service is designed to be primarily read-heavy. For most use cases, a new
 * instance can be constructed whenever a fresh view of the data is needed.
 * Alternatively, the underlying list can be extended externally before passing
 * it into this service.
 * </p>
 *
 * <h3>Typical Usage</h3>
 * <pre>{@code
 * List<BorrowRecord> history = borrowManager.getAllBorrowRecords();
 * ActivityReportService reports = new ActivityReportService(history);
 *
 * Map<User, Long> topBorrowers = reports.getTopBorrowers();
 * Map<String, Long> popularItems = reports.getMostBorrowedItems();
 * List<BorrowRecord> overdueForUser =
 *         reports.getOverdueItemsForUser(someUser, LocalDate.now());
 * }</pre>
 *
 * <p>
 * This class focuses on aggregation and analytics only; it does not modify the
 * underlying {@link BorrowRecord} instances.
 * </p>
 *
 * @author Malak
 */
public class ActivityReportService {

    /** Thread-safe list of all borrow records stored for reporting. */
    private final CopyOnWriteArrayList<BorrowRecord> borrowRecords;

    /**
     * Creates a new {@code ActivityReportService} with a given list of borrow records.
     * <p>
     * The provided collection is copied into an internal {@link CopyOnWriteArrayList}
     * to allow safe concurrent reads and iteration during aggregation.
     * </p>
     *
     * @param borrowRecords list of borrow records to analyze (must not be {@code null})
     * @throws IllegalArgumentException if {@code borrowRecords} is {@code null}
     */
    public ActivityReportService(List<BorrowRecord> borrowRecords) {
        if (borrowRecords == null)
            throw new IllegalArgumentException("Borrow records cannot be null.");
        this.borrowRecords = new CopyOnWriteArrayList<>(borrowRecords);
    }

    /**
     * Computes how many items each user has borrowed in total.
     *
     * <p>
     * The result groups all borrow records by {@link User} and counts how many
     * records belong to each. This effectively answers the question:
     * "How many borrowing events has each user performed?"
     * </p>
     *
     * @return a map where:
     *         <ul>
     *             <li>keys are users</li>
     *             <li>values are the total number of items they have borrowed</li>
     *         </ul>
     */
    public Map<User, Long> getTopBorrowers() {
        return borrowRecords.stream()
                .collect(Collectors.groupingBy(
                        BorrowRecord::getUser,
                        Collectors.counting()
                ));
    }

    /**
     * Determines which library items were borrowed the most.
     *
     * <p>
     * Items are aggregated by a human-readable label in the format:
     * </p>
     * <pre>
     * "Title (TYPE)"
     * </pre>
     * <p>
     * Example: {@code "Clean Code (BOOK)"}.
     * This allows multiple copies of the same logical item to be counted together,
     * regardless of internal identifiers.
     * </p>
     *
     * @return a map where:
     *         <ul>
     *             <li>keys are item labels such as {@code "Title (TYPE)"}</li>
     *             <li>values are the number of times those items were borrowed</li>
     *         </ul>
     */
    public Map<String, Long> getMostBorrowedItems() {
        return borrowRecords.stream()
                .collect(Collectors.groupingBy(
                        r -> buildItemLabel(r.getItem()),
                        Collectors.counting()
                ));
    }

    /**
     * Builds a human-friendly label of a library item to unify its representation
     * during reporting.
     *
     * @param item the library item, may be {@code null}
     * @return label in the format {@code "Title (TYPE)"}, or {@code "Unknown item"} if {@code item} is {@code null}
     */
    private String buildItemLabel(LibraryItem item) {
        if (item == null) {
            return "Unknown item";
        }
        return item.getTitle() + " (" + item.getMaterialType() + ")";
    }

    /**
     * Returns all overdue borrow records for a given user at a specific date.
     *
     * <p>
     * A record is considered overdue if:
     * </p>
     * <ul>
     *     <li>its associated {@link User} equals the given {@code user}, and</li>
     *     <li>{@link BorrowRecord#isOverdue(LocalDate)} returns {@code true}
     *         for the given {@code date}</li>
     * </ul>
     *
     * <p>
     * Common usages include:
     * </p>
     * <ul>
     *     <li>Admin overdue and fine reports.</li>
     *     <li>Sending reminder emails or notifications for late returns.</li>
     * </ul>
     *
     * @param user the user whose overdue items should be checked (must not be {@code null})
     * @param date the reference date to compare against due dates (must not be {@code null})
     * @return list of overdue borrow records for that user at the given date
     * @throws IllegalArgumentException if {@code user} or {@code date} is {@code null}
     */
    public List<BorrowRecord> getOverdueItemsForUser(User user, LocalDate date) {
        if (user == null || date == null)
            throw new IllegalArgumentException("User/date cannot be null.");
        return borrowRecords.stream()
        	    .filter(r -> r.getUser().equals(user) && r.isOverdue(date))
        	    .toList();
    }
}