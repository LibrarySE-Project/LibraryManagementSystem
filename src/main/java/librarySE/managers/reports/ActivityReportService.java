package librarySE.managers.reports;

import librarySE.core.LibraryItem;
import librarySE.managers.BorrowRecord;
import librarySE.managers.User;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Provides analytical reporting features for library activity.
 *
 * <p>This service processes historical {@link BorrowRecord} data to generate
 * insights such as:
 * <ul>
 *     <li>Top users who borrow the most items</li>
 *     <li>Most frequently borrowed books, CDs, or journals</li>
 *     <li>Overdue items for a given user at a given date</li>
 * </ul>
 *
 * <p>The class is thread-safe because it uses {@link CopyOnWriteArrayList}
 * to store borrow records, ensuring safe iteration during concurrent updates.</p>
 *
 * <h3>Main Responsibilities:</h3>
 * <ul>
 *     <li>Aggregate borrowing trends and usage statistics</li>
 *     <li>Support admin dashboards and reporting modules</li>
 *     <li>Detect overdue items per user</li>
 * </ul>
 *
 * <p>This service is read-heavy, so immutability of the underlying list is desirable.
 * New borrow records should be provided by rebuilding the service or exposing append methods externally.</p>
 *
 * @author Malak
 */
public class ActivityReportService {

    /** Thread-safe list of all borrow records stored for reporting. */
    private final CopyOnWriteArrayList<BorrowRecord> borrowRecords;

    /**
     * Creates a new ActivityReportService with a given list of borrow records.
     *
     * @param borrowRecords list of borrow records to analyze (must not be null)
     * @throws IllegalArgumentException if {@code borrowRecords} is null
     */
    public ActivityReportService(List<BorrowRecord> borrowRecords) {
        if (borrowRecords == null)
            throw new IllegalArgumentException("Borrow records cannot be null.");
        this.borrowRecords = new CopyOnWriteArrayList<>(borrowRecords);
    }

    /**
     * Computes how many items each user has borrowed in total.
     *
     * <p>The result groups borrow records by {@link User}, and counts
     * how many borrowing events belong to each user.</p>
     *
     * @return a map where keys are users and values are count of items borrowed
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
     * <p>The key of the returned map is a readable label in the format:<br>
     * <b>"Title (TYPE)"</b><br>
     * For example: <i>"Clean Code (BOOK)"</i></p>
     *
     * <p>This method aggregates logically identical items (same title and type)
     * even if they exist as multiple copies in the system.</p>
     *
     * @return a map from item label to number of times borrowed
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
     * @param item the library item
     * @return label in the format "Title (TYPE)" or "Unknown item" if null
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
     * <p>The record is considered overdue if:
     * <ul>
     *     <li>The user matches</li>
     *     <li>{@link BorrowRecord#isOverdue(LocalDate)} returns true for the given date</li>
     * </ul>
     *
     * <p>This is commonly used for:
     * <ul>
     *     <li>Admin overdue reports</li>
     *     <li>Sending reminders or fines</li>
     * </ul></p>
     *
     * @param user the user whose overdue items should be checked
     * @param date the reference date to compare against due dates
     * @return list of overdue borrow records for that user
     * @throws IllegalArgumentException if {@code user} or {@code date} is null
     */
    public List<BorrowRecord> getOverdueItemsForUser(User user, LocalDate date) {
        if (user == null || date == null)
            throw new IllegalArgumentException("User/date cannot be null.");
        return borrowRecords.stream()
                .filter(r -> r.getUser().equals(user) && r.isOverdue(date))
                .collect(Collectors.toList());
    }
}
