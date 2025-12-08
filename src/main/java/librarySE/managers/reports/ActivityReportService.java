package librarySE.managers.reports;


import librarySE.core.LibraryItem;
import librarySE.managers.BorrowRecord;
import librarySE.managers.User;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Provides analytical reports about borrowing activities within the library system.
 * <p>
 * This service focuses on user and item statistics derived from borrowing records.
 * It supports:
 * <ul>
 *     <li>Finding top borrowers (users with the highest number of borrowed items)</li>
 *     <li>Identifying most borrowed library items</li>
 *     <li>Listing overdue items per user</li>
 * </ul>
 * </p>
 *
 * <p><strong>Thread-safety:</strong> Uses {@link CopyOnWriteArrayList} to safely access
 * borrowing records concurrently without risking modification conflicts.</p>
 *
 * @author Eman
 * 
 */
public class ActivityReportService {

    /** Thread-safe list of borrowing records used for generating reports. */
    private final CopyOnWriteArrayList<BorrowRecord> borrowRecords;

    /**
     * Constructs an ActivityReportService that analyzes user and item activity
     * based on the provided borrowing records.
     *
     * @param borrowRecords list of all borrow records in the system
     * @throws IllegalArgumentException if {@code borrowRecords} is {@code null}
     */
    public ActivityReportService(List<BorrowRecord> borrowRecords) {
        if (borrowRecords == null)
            throw new IllegalArgumentException("Borrow records cannot be null.");
        this.borrowRecords = new CopyOnWriteArrayList<>(borrowRecords);
    }

    /**
     * Returns a map of users and the number of items they have borrowed.
     * <p>Can be used to determine the most active borrowers in the library.</p>
     *
     * @return a map where each key is a {@link User} and each value is the count of borrowed items
     */
    public Map<User, Long> getTopBorrowers() {
        return borrowRecords.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getUser, Collectors.counting()));
    }

    /**
     * Returns a map of library items and how many times each was borrowed.
     * <p>Helps identify the most popular or frequently borrowed items.</p>
     *
     * @return a map where each key is a {@link LibraryItem} and each value is the borrow count
     */
    public Map<LibraryItem, Long> getMostBorrowedItems() {
        return borrowRecords.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getItem, Collectors.counting()));
    }

    /**
     * Retrieves a list of overdue borrow records for a specific user as of a given date.
     *
     * @param user the user whose overdue records are requested
     * @param date the reference date used to check overdue status
     * @return a list of {@link BorrowRecord}s that are overdue for the given user
     * @throws IllegalArgumentException if {@code user} or {@code date} is {@code null}
     */
    public List<BorrowRecord> getOverdueItemsForUser(User user, LocalDate date) {
        if (user == null || date == null)
            throw new IllegalArgumentException("User/date cannot be null.");
        return borrowRecords.stream()
                .filter(r -> r.getUser().equals(user) && r.isOverdue(date))
                .collect(Collectors.toList());
    }
}
