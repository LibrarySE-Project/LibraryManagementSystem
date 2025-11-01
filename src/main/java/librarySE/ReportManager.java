package librarySE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Thread-safe and optimized class that manages generation of reports for the library system.
 * <p>
 * Provides reporting functionalities based on borrow records, including:
 * <ul>
 *     <li>Total fines per user across all media types (Books, CDs, Journals)</li>
 *     <li>Fines broken down by media type per user</li>
 *     <li>Top borrowers by number of items borrowed</li>
 *     <li>Most borrowed items in the library</li>
 *     <li>Overdue items per user</li>
 * </ul>
 * </p>
 * <p>
 * Thread-safety:
 * <ul>
 *     <li>Uses {@link CopyOnWriteArrayList} to store borrow records.</li>
 *     <li>All read-only operations are safe for concurrent access.</li>
 * </ul>
 * </p>
 * 
 * @author Malak
 * @see BorrowRecord
 * @see User
 * @see LibraryItem
 * @see MaterialType
 */
public class ReportManager {

    /** Thread-safe list of borrow records used as the source for generating reports. */
    private final CopyOnWriteArrayList<BorrowRecord> borrowRecords;

    /**
     * Constructs a {@code ReportManager} with a given list of borrow records.
     * Thread-safe copy is created.
     *
     * @param borrowRecords the borrow records to be used for generating reports; must not be null
     * @throws IllegalArgumentException if {@code borrowRecords} is null
     */
    public ReportManager(List<BorrowRecord> borrowRecords) {
        if (borrowRecords == null)
            throw new IllegalArgumentException("Borrow records cannot be null.");
        this.borrowRecords = new CopyOnWriteArrayList<>(borrowRecords);
    }

    /**
     * Calculates the total fines owed by a specific user across all media types.
     *
     * @param user the user for whom fines are calculated; must not be null
     * @param date the date used for calculating fines; must not be null
     * @return the total fines as a {@link BigDecimal}; never null
     * @throws IllegalArgumentException if {@code user} or {@code date} is null
     */
    public BigDecimal getTotalFinesForUser(User user, LocalDate date) {
        if (user == null || date == null)
            throw new IllegalArgumentException("User and date cannot be null.");

        return borrowRecords.stream()
                .filter(r -> r.getUser().equals(user))
                .map(r -> r.getFine(date))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns fines per media type for a specific user.
     * <p>
     * Uses an {@link EnumMap} for performance and type safety.
     * </p>
     *
     * @param user the user for whom fines are calculated; must not be null
     * @param date the date used for calculating fines; must not be null
     * @return a map of {@link MaterialType} to {@link BigDecimal} representing fines per type
     * @throws IllegalArgumentException if {@code user} or {@code date} is null
     */
    public Map<MaterialType, BigDecimal> getFinesByMediaType(User user, LocalDate date) {
        if (user == null || date == null)
            throw new IllegalArgumentException("User and date cannot be null.");

        EnumMap<MaterialType, BigDecimal> finesMap = new EnumMap<>(MaterialType.class);
        for (BorrowRecord record : borrowRecords) {
            if (record.getUser().equals(user)) {
                MaterialType type = record.getItem().getMaterialType();
                finesMap.put(type, finesMap.getOrDefault(type, BigDecimal.ZERO).add(record.getFine(date)));
            }
        }
        return finesMap;
    }

    /**
     * Returns a mapping of users to the number of items they have borrowed.
     *
     * @return a map of {@link User} to {@link Long} representing the count of borrowed items
     */
    public Map<User, Long> getTopBorrowers() {
        return borrowRecords.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getUser, Collectors.counting()));
    }

    /**
     * Returns a mapping of library items to the number of times each has been borrowed.
     *
     * @return a map of {@link LibraryItem} to {@link Long} representing borrow counts
     */
    public Map<LibraryItem, Long> getMostBorrowedItems() {
        return borrowRecords.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getItem, Collectors.counting()));
    }

    /**
     * Returns a list of overdue borrow records for a specific user.
     *
     * @param user the user for whom overdue items are retrieved; must not be null
     * @param date the date used to check for overdue status; must not be null
     * @return a list of overdue {@link BorrowRecord} instances; empty list if none
     * @throws IllegalArgumentException if {@code user} or {@code date} is null
     */
    public List<BorrowRecord> getOverdueItemsForUser(User user, LocalDate date) {
        if (user == null || date == null)
            throw new IllegalArgumentException("User and date cannot be null.");

        return borrowRecords.stream()
                .filter(r -> r.getUser().equals(user) && r.isOverdue(date))
                .collect(Collectors.toList());
    }

    /**
     * Returns total fines for all users in the system.
     *
     * @param date the date used for calculating fines; must not be null
     * @return a map of {@link User} to {@link BigDecimal} representing total fines
     * @throws IllegalArgumentException if {@code date} is null
     */
    public Map<User, BigDecimal> getTotalFinesForAllUsers(LocalDate date) {
        if (date == null)
            throw new IllegalArgumentException("Date cannot be null.");

        return borrowRecords.stream()
                .collect(Collectors.groupingBy(
                        BorrowRecord::getUser,
                        Collectors.mapping(r -> r.getFine(date),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }

    /**
     * Exports a CSV-style text report of fines per user.
     * <p>
     * Columns include: Username, Total Fines, Book Fines, CD Fines, Journal Fines.
     * Uses cached totals and fines by type to avoid recalculating.
     * </p>
     *
     * @param date the date used for calculating fines; must not be null
     * @return a string representing the CSV report
     * @throws IllegalArgumentException if {@code date} is null
     */
    public String exportFinesReport(LocalDate date) {
        if (date == null)
            throw new IllegalArgumentException("Date cannot be null.");

        StringBuilder sb = new StringBuilder();
        sb.append("User,Total Fines,Book,CD,Journal\n");

        Map<User, BigDecimal> totalFinesMap = getTotalFinesForAllUsers(date);

        for (User user : totalFinesMap.keySet()) {
            BigDecimal total = totalFinesMap.get(user);
            Map<MaterialType, BigDecimal> finesByType = getFinesByMediaType(user, date);

            sb.append(user.getUsername()).append(",")
              .append(total).append(",")
              .append(finesByType.getOrDefault(MaterialType.BOOK, BigDecimal.ZERO)).append(",")
              .append(finesByType.getOrDefault(MaterialType.CD, BigDecimal.ZERO)).append(",")
              .append(finesByType.getOrDefault(MaterialType.JOURNAL, BigDecimal.ZERO))
              .append("\n");
        }
        return sb.toString();
    }

    /** Returns a defensive copy of all borrow records. */
    public List<BorrowRecord> getAllBorrowRecords() {
        return new ArrayList<>(borrowRecords);
    }
}
