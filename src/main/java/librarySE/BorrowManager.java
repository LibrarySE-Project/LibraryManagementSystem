package librarySE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Singleton class responsible for managing the borrowing and returning of library items.
 * <p>
 * This class acts as the central point for borrowing operations in the library system.
 * It coordinates users, items, and fines through {@link BorrowRecord} and {@link FineContext}.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *     <li>Borrowing items for users while enforcing borrowing rules (no overdue items or unpaid fines).</li>
 *     <li>Returning items and marking them as available again.</li>
 *     <li>Tracking overdue items and applying fines using {@link FineContext} with different {@link FineStrategy} implementations.</li>
 *     <li>Calculating total fines per user.</li>
 * </ul>
 *
 * <h3>Thread-safety:</h3>
 * <ul>
 *     <li>The {@link #borrowRecords} list is a {@link CopyOnWriteArrayList} for safe concurrent access.</li>
 *     <li>Borrowing and returning operations are synchronized per {@link LibraryItem} to prevent race conditions.</li>
 * </ul>
 *
 * <h3>Design Patterns:</h3>
 * <ul>
 *     <li><b>Singleton</b> – ensures a single instance of {@code BorrowManager} exists system-wide.</li>
 *     <li><b>Strategy</b> – delegates fine calculation via {@link FineContext} and {@link FineStrategy} implementations.</li>
 * </ul>
 *
 * @see BorrowRecord
 * @see LibraryItem
 * @see FineContext
 * @see FineStrategy
 * @see User
 * @see BookFineStrategy
 * @see CDFineStrategy
 * @see JournalFineStrategy
 * 
 * @author Malak
 */
public class BorrowManager {

    /** Singleton instance of the BorrowManager */
    private static BorrowManager instance;

    /** Thread-safe list of all borrow records in the system */
    private final CopyOnWriteArrayList<BorrowRecord> borrowRecords;

    /** Private constructor to enforce the Singleton pattern. */
    private BorrowManager() {
        this.borrowRecords = new CopyOnWriteArrayList<>();
    }

    /**
     * Returns the singleton instance of {@code BorrowManager}.
     * <p>
     * If no instance exists yet, it creates one lazily.
     * </p>
     *
     * @return the single global {@link BorrowManager} instance
     */
    public static synchronized BorrowManager getInstance() {
        if (instance == null) {
            instance = new BorrowManager();
        }
        return instance;
    }

    /**
     * Borrows a library item for a user.
     * <p>
     * The following rules are enforced:
     * <ul>
     *     <li>The user must not have any unpaid fines.</li>
     *     <li>The user must not have any overdue items.</li>
     *     <li>The item must currently be available for borrowing.</li>
     * </ul>
     *
     * If all checks pass, a new {@link BorrowRecord} is created with a {@link FineContext}
     * corresponding to the item's {@link MaterialType}, and added to the list of borrow records.
     * </p>
     *
     * <p><b>Thread-safety:</b> Borrowing is synchronized on the {@link LibraryItem} instance.</p>
     *
     * @param user the user borrowing the item; must not be {@code null}
     * @param item the library item to borrow; must not be {@code null}
     * @return {@code true} if the borrowing operation succeeds
     * @throws IllegalArgumentException if user or item is {@code null}
     * @throws IllegalStateException if the user has unpaid fines, overdue items, or the item is unavailable
     */
    public boolean borrowItem(User user, LibraryItem item) {
        if (user == null || item == null)
            throw new IllegalArgumentException("User and item cannot be null.");

        LocalDate today = LocalDate.now();
        applyOverdueFines(today);

        if (user.hasOutstandingFine())
            throw new IllegalStateException("Cannot borrow: unpaid fines exist.");

        boolean hasOverdue = getBorrowRecordsForUser(user).stream()
                .anyMatch(r -> r.isOverdue(today));
        if (hasOverdue)
            throw new IllegalStateException("Cannot borrow: overdue items exist.");

        synchronized (item) {
            if (!item.isAvailable())
                throw new IllegalStateException("Item is already borrowed.");
            item.borrow();
        }

        FineContext fineContext = new FineContext(item.getMaterialType().createFineStrategy());
        BorrowRecord record = new BorrowRecord(user, item, fineContext, today);
        borrowRecords.add(record);
        return true;
    }

    /**
     * Returns a borrowed item for a user.
     * <p>
     * Marks the corresponding {@link BorrowRecord} as returned and applies any overdue fines
     * up to the return date.
     * </p>
     *
     * <p><b>Thread-safety:</b> Return is synchronized on the {@link LibraryItem} instance.</p>
     *
     * @param user the user returning the item; must not be {@code null}
     * @param item the library item being returned; must not be {@code null}
     * @throws IllegalArgumentException if no active borrow record is found for the user-item pair
     */
    public void returnItem(User user, LibraryItem item) {
        if (user == null || item == null)
            throw new IllegalArgumentException("User and item cannot be null.");

        LocalDate today = LocalDate.now();
        applyOverdueFines(today);

        BorrowRecord record = borrowRecords.stream()
                .filter(r -> r.getItem().equals(item) && r.getUser().equals(user) && !r.isReturned())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No active borrowing found."));

        synchronized (item) {
            record.markReturned(today);
        }
    }

    /**
     * Applies overdue fines for all borrow records as of the given date.
     * <p>
     * Each record is charged only once to prevent double fine application.
     * </p>
     *
     * @param date the date used to check for overdue items; must not be {@code null}
     */
    public void applyOverdueFines(LocalDate date) {
        for (BorrowRecord record : borrowRecords) {
            if (record.isOverdue(date))
                record.applyFineToUser(date);
        }
    }

    /**
     * Returns all borrow records for a specific user.
     *
     * @param user the user whose records are retrieved; must not be {@code null}
     * @return a list of {@link BorrowRecord} associated with the user
     */
    public List<BorrowRecord> getBorrowRecordsForUser(User user) {
        return borrowRecords.stream()
                .filter(r -> r.getUser().equals(user))
                .collect(Collectors.toList());
    }

    /**
     * Calculates the total fines for a user as of a specific date.
     *
     * @param user the user whose fines are calculated; must not be {@code null}
     * @param date the date up to which fines are calculated; must not be {@code null}
     * @return total fines as a {@link BigDecimal}
     */
    public BigDecimal calculateTotalFines(User user, LocalDate date) {
        return borrowRecords.stream()
                .filter(r -> r.getUser().equals(user))
                .map(r -> r.getFine(date))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns a list of all overdue borrow records as of the given date.
     *
     * @param date the date to check for overdue items; must not be {@code null}
     * @return list of overdue {@link BorrowRecord} objects
     */
    public List<BorrowRecord> getOverdueItems(LocalDate date) {
        applyOverdueFines(date);
        return borrowRecords.stream()
                .filter(r -> r.isOverdue(date))
                .collect(Collectors.toList());
    }

    /**
     * Returns all borrow records in the system.
     * <p>
     * Returns a defensive copy to prevent external modification of the internal list.
     * </p>
     *
     * @return an immutable list of all {@link BorrowRecord} objects
     */
    public List<BorrowRecord> getAllBorrowRecords() {
        return List.copyOf(borrowRecords);
    }
}
