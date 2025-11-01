package librarySE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Singleton class responsible for managing the borrowing and returning of library items.
 * <p>
 * Responsibilities include:
 * <ul>
 *     <li>Borrowing items for users while enforcing borrowing rules (no overdue items, no unpaid fines).</li>
 *     <li>Returning items and marking them as returned.</li>
 *     <li>Tracking overdue items and applying fines according to each item's {@link FineStrategy}.</li>
 *     <li>Calculating total fines for a specific user.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Thread-safety:
 * <ul>
 *     <li>The {@link #borrowRecords} list is a {@link CopyOnWriteArrayList} to allow safe concurrent reads and modifications.</li>
 *     <li>Borrowing and returning of items are synchronized on each {@link LibraryItem} to prevent race conditions.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class uses the Singleton pattern. Use {@link #getInstance()} to obtain
 * the single global instance of {@code BorrowManager}.
 * </p>
 *
 * @author Malak
 * @see BorrowRecord
 * @see LibraryItem
 * @see FineStrategy
 * @see User
 */
public class BorrowManager {

    /** Singleton instance */
    private static BorrowManager instance;

    /** Thread-safe list of all borrow records in the system */
    private final CopyOnWriteArrayList<BorrowRecord> borrowRecords;

    /** Private constructor to enforce Singleton pattern. */
    private BorrowManager() {
        this.borrowRecords = new CopyOnWriteArrayList<>();
    }

    /**
     * Returns the singleton instance of {@code BorrowManager}.
     * <p>
     * If no instance exists yet, it creates one.
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
     * Rules enforced:
     * <ul>
     *     <li>User must not have any unpaid fines.</li>
     *     <li>User must not have any overdue items.</li>
     *     <li>Item must be currently available.</li>
     * </ul>
     * If all checks pass, a new {@link BorrowRecord} is created and added to the internal list.
     * <p>
     * Thread-safety: Synchronized on the {@link LibraryItem} during the borrow operation.
     * </p>
     *
     * @param user the user borrowing the item; must not be null
     * @param item the library item to borrow; must not be null
     * @return {@code true} if borrowing succeeds
     * @throws IllegalArgumentException if user or item is null
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

        BorrowRecord record = new BorrowRecord(user, item, item.getFineStrategy(), today);
        borrowRecords.add(record);
        return true;
    }

    /**
     * Returns a borrowed item for a user.
     * <p>
     * Marks the corresponding {@link BorrowRecord} as returned and applies any overdue fines
     * up to the return date.
     * <p>
     * Thread-safety: Synchronized on the {@link LibraryItem} during the return operation.
     * </p>
     *
     * @param user the user returning the item; must not be null
     * @param item the library item being returned; must not be null
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
     * Each record is charged only once to prevent double fines.
     * </p>
     *
     * @param date the date to check for overdue items
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
     * @param user the user whose records are retrieved
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
     * @param user the user whose fines are calculated
     * @param date the date to calculate fines up to
     * @return total fines as {@link BigDecimal}
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
     * @param date the date to check for overdue items
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
     * @return list of all {@link BorrowRecord} objects
     */
    public List<BorrowRecord> getAllBorrowRecords() {
        return List.copyOf(borrowRecords);
    }
}


