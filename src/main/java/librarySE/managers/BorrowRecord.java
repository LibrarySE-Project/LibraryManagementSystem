package librarySE.managers;

import librarySE.core.LibraryItem;
import librarySE.strategy.FineStrategy;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;

/**
 * Represents a single borrowing transaction between a {@link User} and a {@link LibraryItem}.
 * <p>
 * Each {@code BorrowRecord} tracks key details such as:
 * <ul>
 *   <li>The user who borrowed the item</li>
 *   <li>The borrowed item and its associated fine strategy</li>
 *   <li>The borrow date, due date, and current status</li>
 *   <li>Any overdue fines and payments applied</li>
 * </ul>
 * </p>
 *
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *   <li>Calculates fines dynamically based on overdue days using {@link FineStrategy}.</li>
 *   <li>Applies fines to users and supports partial fine payments.</li>
 *   <li>Tracks borrow state transitions (BORROWED → RETURNED).</li>
 *   <li>Supports overdue checks and fine recalculations at any date.</li>
 * </ul>
 *
 * <h3>Design Notes:</h3>
 * <p>
 * The class implements {@link Serializable} to support persistent storage
 * in JSON or binary format for future auditing and reporting.
 * </p>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * User user = new User("Eman", Role.USER, "pass123", "eman@najah.edu");
 * LibraryItem book = new Book("978-0134685991", "Effective Java", "Joshua Bloch");
 * FineStrategy fineStrategy = FineStrategyFactory.book();
 *
 * BorrowRecord record = new BorrowRecord(user, book, fineStrategy, LocalDate.of(2025, 11, 1));
 *
 * // Simulate checking after 35 days (7 days late)
 * record.calculateFine(LocalDate.of(2025, 12, 6));
 * System.out.println(record.getFine(LocalDate.now())); // Prints overdue fine
 *
 * // Return item
 * record.markReturned(LocalDate.of(2025, 12, 6));
 * System.out.println(record);
 * }</pre>
 *
 * @author Eman
 */
public class BorrowRecord implements Serializable {

    /** Enumeration of borrowing states. */
    public enum Status { BORROWED, RETURNED }

    /** The user who borrowed the item. */
    private final User user;

    /** The borrowed item (e.g., Book, CD, Journal). */
    private final LibraryItem item;

    /** Fine strategy defining rate and allowed borrow period. */
    private final FineStrategy fineStrategy;

    /** Borrow period in days (defined by the fine strategy). */
    private final int borrowPeriodDays;

    /** Timestamp when the item was borrowed. */
    private final LocalDateTime borrowDateTime;

    /** Calculated due date/time based on borrow period. */
    private final LocalDateTime dueDateTime;

    /** Fine accumulated (may be 0 if not overdue). */
    private BigDecimal fine = BigDecimal.ZERO;

    /** Indicates whether fine has been applied to user’s balance. */
    private boolean fineApplied = false;

    /** Amount paid by the user toward the fine. */
    private BigDecimal finePaid = BigDecimal.ZERO;

    /** Current borrowing status (BORROWED or RETURNED). */
    private Status status = Status.BORROWED;


    /**
     * Creates a new borrow record linking a {@link User}, {@link LibraryItem}, and {@link FineStrategy}.
     * <p>
     * The due date is automatically computed based on the borrow date and the strategy’s allowed period.
     * </p>
     *
     * @param user          the borrowing user (must not be null)
     * @param item          the borrowed item (must not be null)
     * @param fineStrategy  the fine strategy to apply (must not be null)
     * @param borrowDate    the borrow date (must not be null)
     * @throws IllegalArgumentException if any argument is null
     */
    public BorrowRecord(User user, LibraryItem item, FineStrategy fineStrategy, LocalDate borrowDate) {
        if (user == null || item == null || fineStrategy == null || borrowDate == null)
            throw new IllegalArgumentException("BorrowRecord: arguments cannot be null.");

        this.user = user;
        this.item = item;
        this.fineStrategy = fineStrategy;
        this.borrowPeriodDays = fineStrategy.getBorrowPeriodDays();
        this.borrowDateTime = borrowDate.atStartOfDay();
        this.dueDateTime = borrowDateTime.plusDays(borrowPeriodDays);
    }

    /** @return the user who borrowed the item */
    public User getUser() { return user; }

    /** @return the borrowed library item */
    public LibraryItem getItem() { return item; }

    /** @return the borrow date */
    public LocalDate getBorrowDate() { return borrowDateTime.toLocalDate(); }

    /** @return the due date for returning the item */
    public LocalDate getDueDate() { return dueDateTime.toLocalDate(); }

    /** @return the current borrowing status */
    public Status getStatus() { return status; }

    /** @return {@code true} if the item has been returned */
    public boolean isReturned() { return status == Status.RETURNED; }

    /** @return {@code true} if a fine was applied to the user */
    public boolean isFineApplied() { return fineApplied; }

    /** @return the total fine amount the user has paid */
    public BigDecimal getFinePaid() { return finePaid; }

    /** @return the total fine (calculated based on overdue days) */
    public BigDecimal getFine(LocalDate date) {
        calculateFine(date);
        return fine;
    }

    /** @return the remaining unpaid portion of the fine */
    public BigDecimal getRemainingFine() {
        return fine.subtract(finePaid);
    }


    /**
     * Updates the paid fine amount.
     *
     * @param amount the amount paid (non-null, non-negative, ≤ total fine)
     * @throws IllegalArgumentException if invalid payment amount
     */
    public void setFinePaid(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Paid amount cannot be null or negative.");
        if (amount.compareTo(fine) > 0)
            throw new IllegalArgumentException("Paid amount cannot exceed total fine.");
        this.finePaid = amount;
    }

    /**
     * Calculates the overdue fine dynamically for a given date.
     * <p>
     * If the item is overdue (not returned and date is past due date),
     * the fine is computed using the assigned {@link FineStrategy}.
     * </p>
     *
     * @param currentDate the date for which to calculate the fine
     * @throws IllegalArgumentException if {@code currentDate} is null
     */
    public void calculateFine(LocalDate currentDate) {
        if (currentDate == null)
            throw new IllegalArgumentException("Current date cannot be null.");

        if (!isReturned() && currentDate.atStartOfDay().isAfter(dueDateTime)) {
            long daysOverdue = ChronoUnit.DAYS.between(dueDateTime.toLocalDate(), currentDate);
            fine = fineStrategy.calculateFine(daysOverdue);
        } else {
            fine = BigDecimal.ZERO;
        }
    }

    /**
     * Applies the calculated fine to the user’s account balance (if not already applied).
     *
     * @param currentDate the date of fine application
     */
    public void applyFineToUser(LocalDate currentDate) {
        calculateFine(currentDate);
        if (!fineApplied && fine.compareTo(BigDecimal.ZERO) > 0) {
            user.addFine(fine);
            fineApplied = true;
        }
    }


    /**
     * Marks the borrowed item as returned and updates user fines accordingly.
     *
     * @param returnDate the date the item was returned
     */
    public void markReturned(LocalDate returnDate) {
        applyFineToUser(returnDate);
        item.returnItem();
        status = Status.RETURNED;
    }

    /**
     * Checks whether the borrowed item is overdue based on a given date.
     *
     * @param currentDate the date to check
     * @return {@code true} if the item is overdue and not yet returned
     */
    public boolean isOverdue(LocalDate currentDate) {
        if (currentDate == null)
            throw new IllegalArgumentException("Current date cannot be null.");
        return !isReturned() && currentDate.atStartOfDay().isAfter(dueDateTime);
    }


    /**
     * Returns a string representation of this borrow record, including:
     * <ul>
     *   <li>Username</li>
     *   <li>Item title</li>
     *   <li>Borrow and due dates</li>
     *   <li>Status and current fine</li>
     * </ul>
     *
     * @return formatted string summary of the record
     */
    @Override
    public String toString() {
        return "%s borrowed \"%s\" on %s (due: %s) | Status: %s | Fine: %s"
                .formatted(user.getUsername(), item.getTitle(), getBorrowDate(),
                        getDueDate(), status, fine);
    }
}
