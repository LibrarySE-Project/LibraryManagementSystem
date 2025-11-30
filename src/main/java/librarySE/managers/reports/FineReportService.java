package librarySE.managers.reports;

import librarySE.managers.BorrowRecord;
import librarySE.managers.User;
import librarySE.core.MaterialType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Provides analytical reports related to fines within the library system.
 * <p>
 * This service class focuses on aggregating fine information from all borrowing records.
 * It supports:
 * <ul>
 *     <li>Calculating total fines per user</li>
 *     <li>Summarizing fines by media type (e.g., BOOK, CD, JOURNAL)</li>
 *     <li>Calculating total fines for all users combined</li>
 * </ul>
 * </p>
 *
 * <p>All calculations are performed based on the provided date,
 * which ensures that overdue fines are evaluated consistently.</p>
 *
 * <p><strong>Thread-safety:</strong> Uses {@link CopyOnWriteArrayList} to allow safe concurrent reads.</p>
 *
 * @author Malak
 */
public class FineReportService {

    /** Thread-safe list of all borrow records used for reporting. */
    private final CopyOnWriteArrayList<BorrowRecord> borrowRecords;

    /**
     * Constructs a new FineReportService instance.
     *
     * @param borrowRecords list of all borrowing records in the system
     * @throws IllegalArgumentException if the provided list is {@code null}
     */
    public FineReportService(List<BorrowRecord> borrowRecords) {
        if (borrowRecords == null)
            throw new IllegalArgumentException("Borrow records cannot be null.");
        this.borrowRecords = new CopyOnWriteArrayList<>(borrowRecords);
    }

    /**
     * Calculates the total fines owed by a specific user as of the given date.
     *
     * @param user the user whose total fine is to be calculated
     * @param date the current date used to check overdue items
     * @return total fine amount for the user, or {@link BigDecimal#ZERO} if none
     * @throws IllegalArgumentException if {@code user} or {@code date} is {@code null}
     */
    public BigDecimal getTotalFinesForUser(User user, LocalDate date) {
        if (user == null || date == null)
            throw new IllegalArgumentException("User/date cannot be null.");
        return borrowRecords.stream()
                .filter(r -> r.getUser().equals(user))
                .map(r -> r.getFine(date))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Generates a breakdown of fines per {@link MaterialType} (e.g. BOOK, CD, JOURNAL)
     * for a specific user.
     *
     * @param user the user whose fines are to be analyzed
     * @param date the date used for overdue fine calculation
     * @return a map where each key is a material type and each value is the total fine
     * @throws IllegalArgumentException if {@code user} or {@code date} is {@code null}
     */
    public Map<MaterialType, BigDecimal> getFinesByMediaType(User user, LocalDate date) {
        if (user == null || date == null)
            throw new IllegalArgumentException("User/date cannot be null.");

        EnumMap<MaterialType, BigDecimal> map = new EnumMap<>(MaterialType.class);
        for (BorrowRecord record : borrowRecords) {
            if (record.getUser().equals(user)) {
                MaterialType type = record.getItem().getMaterialType();
                map.put(type, map.getOrDefault(type, BigDecimal.ZERO).add(record.getFine(date)));
            }
        }
        return map;
    }

    /**
     * Calculates the total fines for <strong>all users</strong> in the system.
     * <p>Useful for generating administrative or financial reports.</p>
     *
     * @param date the date used to calculate overdue fines
     * @return a map where the key is a {@link User} and the value is their total fine
     * @throws IllegalArgumentException if {@code date} is {@code null}
     */
    public Map<User, BigDecimal> getTotalFinesForAllUsers(LocalDate date) {
        if (date == null)
            throw new IllegalArgumentException("Date cannot be null.");
        return borrowRecords.stream().collect(Collectors.groupingBy(
                BorrowRecord::getUser,
                Collectors.mapping(r -> r.getFine(date),
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
        ));
    }
}
