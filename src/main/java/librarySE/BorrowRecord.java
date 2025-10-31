package librarySE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a single borrowing record in the library system.
 * <p>
 * Each record links a {@link User} with a {@link LibraryItem}, tracking:
 * <ul>
 *     <li>The borrow date</li>
 *     <li>The due date (automatically calculated based on the {@link FineStrategy} of the item)</li>
 *     <li>Applicable fines for overdue items</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class implements the Strategy Pattern for fine calculation using {@link FineStrategy}.
 * Each material type (Book, CD, Journal, etc.) has its own borrow period defined in its strategy.
 * The {@code dueDate} for a borrow record is automatically set to:
 * <pre>
 * borrowDate + fineStrategy.getBorrowPeriodDays()
 * </pre>
 * ensuring that different material types have their own return deadlines.
 * </p>
 *
 * <p>
 * The availability of the item is controlled via the {@link LibraryItem} interface.
 * An item is considered returned if {@link LibraryItem#isAvailable()} returns {@code true}.
 * </p>
 *
 * <p>
 * All date calculations (borrow date, due date, return date) use {@link LocalDate} parameters.
 * This allows specifying a custom date for testing or simulation instead of relying on the current date.
 * </p>
 *
 * @see User
 * @see LibraryItem
 * @see FineStrategy
 * @author Eman
 */

public class BorrowRecord {

    /** The user who borrowed the item */
    private final User user;

    /** The borrowed item (Book, CD, Journal, etc.) */
    private final LibraryItem item;

    /** Strategy used to calculate fines for overdue items */
    private final FineStrategy fineStrategy;

    /** Number of days allowed for borrowing (from the strategy) */
    private final int borrowPeriodDays;

    /** Date when the item was borrowed */
    private final LocalDate borrowDate;

    /** Date when the item is due for return */
    private final LocalDate dueDate;

    /** Current fine for this borrowing record */
    private BigDecimal fine;
    
    /** Indicates if the overdue fine has already been applied to the user to avoid double charging */
    private boolean fineApplied = false;
    
    /** Amount of fine already paid for this record */ 
    private BigDecimal finePaid = BigDecimal.ZERO;

    /**
     * Constructs a borrowing record for a given user, item, fine strategy, and borrow date.
     * <p>
     * Upon construction, the item is marked as unavailable (borrowed), and the due date
     * is automatically calculated based on the borrow period from the {@link FineStrategy}.
     * </p>
     *
     * @param user the user borrowing the item; must not be {@code null}
     * @param item the borrowed item; must not be {@code null}
     * @param fineStrategy the fine strategy to calculate overdue fines; must not be {@code null}
     * @param borrowDate the date when the item is borrowed; must not be {@code null}
     * @throws IllegalArgumentException if any argument is {@code null}
     */
    public BorrowRecord(User user, LibraryItem item, FineStrategy fineStrategy, LocalDate borrowDate) {
        if (user == null || item == null || fineStrategy == null || borrowDate == null) {
            throw new IllegalArgumentException("User, item, fineStrategy, and borrowDate cannot be null.");
        }
        this.user = user;
        this.item = item;
        this.fineStrategy = fineStrategy;
        this.borrowPeriodDays = this.fineStrategy.getBorrowPeriodDays();
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(borrowPeriodDays);
        this.fine = BigDecimal.ZERO;
    }

    /** Returns the user who borrowed the item */
    public User getUser() { return user; }

    /** Returns the borrowed item */
    public LibraryItem getItem() { return item; }

    /** Returns the date when the item was borrowed */
    public LocalDate getBorrowDate() { return borrowDate; }

    /** Returns the due date for returning the item */
    public LocalDate getDueDate() { return dueDate; }
    
    /** Returns true if the fine has already been applied to the user */
    public boolean isFineApplied() {
        return fineApplied;
    }

    /** Sets whether the fine has been applied to the user */
    public void setFineApplied(boolean fineApplied) {
        this.fineApplied = fineApplied;
    }

    /** Checks if the item has been returned */
    public boolean isReturned() { return item.isAvailable(); }

    /**
     * Returns the current fine for this borrowing record.
     * <p>
     * Fine is calculated based on the provided {@code currentDate}.
     * </p>
     *
     * @param currentDate the date for calculating overdue fines; must not be {@code null}
     * @return the calculated fine
     * @throws IllegalArgumentException if {@code currentDate} is null
     */
    public BigDecimal getFine(LocalDate currentDate) {
        calculateFine(currentDate);
        return fine;
    }
    /**
     * Returns the amount of fine already paid for this borrowing record.
     *
     * @return the paid fine as a {@link BigDecimal}, never {@code null}
     */
    public BigDecimal getFinePaid() {
        return finePaid;
    }

    /**
     * Sets the amount of fine paid for this borrowing record.
     * <p>
     * This method validates that the amount is non-null, non-negative, and does not exceed
     * the total fine for this record.
     * </p>
     *
     * @param amount the amount to mark as paid; must be non-null, ≥ 0, and ≤ {@link #fine}
     * @throws IllegalArgumentException if {@code amount} is null, negative, or exceeds the total fine
     */
    public void setFinePaid(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Paid amount cannot be null or negative");
        }
        if (amount.compareTo(fine) > 0) {
            throw new IllegalArgumentException("Paid amount cannot exceed fine");
        }
        this.finePaid = amount;
    }

    /**
     * Returns the remaining unpaid fine for this borrowing record.
     * <p>
     * Computed as the total fine minus the amount already paid. This can be used
     * to display outstanding fines for the user.
     * </p>
     *
     * @return the remaining fine as a {@link BigDecimal}, never {@code null}
     */
    public BigDecimal getRemainingFine() {
        return fine.subtract(finePaid);
    }

    /**
     * Calculates and updates the fine based on overdue days.
     *
     * @param currentDate the date for calculating overdue fines; must not be {@code null}
     */
    public void calculateFine(LocalDate currentDate) {
        if (currentDate == null)
            throw new IllegalArgumentException("currentDate cannot be null.");
        if (!item.isAvailable() && currentDate.isAfter(dueDate)) {
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, currentDate);
            fine = fineStrategy.calculateFine(daysOverdue);
        } else {
            fine = BigDecimal.ZERO;
        }
    }

    /**
     * Applies the current fine to the user's account.
     * <p>
     * Ensures that the fine is only applied once per overdue period.
     * </p>
     *
     * @param currentDate the date used to calculate and apply fines; must not be {@code null}
     */
    public void applyFineToUser(LocalDate currentDate) {
        calculateFine(currentDate);
        if (!fineApplied && fine.compareTo(BigDecimal.ZERO) > 0) {
            user.addFine(fine);
            fineApplied = true;
        }
    }

    /**
     * Marks the item as returned and applies any overdue fines to the user.
     * <p>
     * The return date is provided to calculate fines correctly.
     * </p>
     *
     * @param returnDate the date when the item is returned; must not be {@code null}
     */
    public void markReturned(LocalDate returnDate) {
        applyFineToUser(returnDate);
        item.returnItem();
    }

    /**
     * Checks if the item is currently overdue as of the given date.
     *
     * @param currentDate the date for checking overdue status; must not be {@code null}
     * @return {@code true} if the item is overdue, {@code false} otherwise
     */
    public boolean isOverdue(LocalDate currentDate) {
        if (currentDate == null)
            throw new IllegalArgumentException("currentDate cannot be null.");
        return !item.isAvailable() && currentDate.isAfter(dueDate);
    }

    /** Returns a string representation of the borrowing record */
    @Override
    public String toString() {
        return String.format("%s borrowed \"%s\" on %s (due: %s) | Returned: %b | Fine: %s",
                user.getUsername(), item.getTitle(), borrowDate, dueDate, isReturned(), fine);
    }
}

