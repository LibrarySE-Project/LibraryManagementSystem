package librarySE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a single borrowing record in the library system.
 * <p>
 * Uses the Strategy Pattern through {@link FineContext} to calculate overdue fines.
 * The context delegates fine calculation to the assigned {@link FineStrategy}.
 * </p>
 *
 * <p>
 * Each record links a {@link User} with a {@link LibraryItem}, tracking:
 * <ul>
 *     <li>Borrow and due dates</li>
 *     <li>Fine calculation through {@link FineContext}</li>
 *     <li>Fine payment and application logic</li>
 * </ul>
 * </p>
 *
 * @see User
 * @see LibraryItem
 * @see FineStrategy
 * @see FineContext
 * @author Eman
 */
public class BorrowRecord {

    /** The user who borrowed the item */
    private final User user;

    /** The borrowed item (Book, CD, Journal, etc.) */
    private final LibraryItem item;

    /** Fine calculation context (aggregates a FineStrategy) */
    private final FineContext fineContext;

    /** Number of days allowed for borrowing (from the strategy) */
    private final int borrowPeriodDays;

    /** Date when the item was borrowed */
    private final LocalDate borrowDate;

    /** Date when the item is due for return */
    private final LocalDate dueDate;

    /** Current fine for this borrowing record */
    private BigDecimal fine;

    /** Indicates if the overdue fine has already been applied to the user */
    private boolean fineApplied = false;

    /** Amount of fine already paid for this record */
    private BigDecimal finePaid = BigDecimal.ZERO;

    /**
     * Constructs a borrowing record for a given user, item, fine context, and borrow date.
     *
     * @param user the user borrowing the item; must not be {@code null}
     * @param item the borrowed item; must not be {@code null}
     * @param fineContext the fine calculation context; must not be {@code null}
     * @param borrowDate the date when the item is borrowed; must not be {@code null}
     */
    public BorrowRecord(User user, LibraryItem item, FineContext fineContext, LocalDate borrowDate) {
        if (user == null || item == null || fineContext == null || borrowDate == null) {
            throw new IllegalArgumentException("User, item, fineContext, and borrowDate cannot be null.");
        }
        this.user = user;
        this.item = item;
        this.fineContext = fineContext;
        this.borrowPeriodDays = fineContext.getBorrowPeriodDays();
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(borrowPeriodDays);
        this.fine = BigDecimal.ZERO;
    }

    /** Returns the user who borrowed the item */
    public User getUser() { return user; }

    /** Returns the borrowed item */
    public LibraryItem getItem() { return item; }

    /** Returns the borrow date */
    public LocalDate getBorrowDate() { return borrowDate; }

    /** Returns the due date */
    public LocalDate getDueDate() { return dueDate; }

    /** Returns true if the fine has been applied */
    public boolean isFineApplied() { return fineApplied; }

    /** Sets fine applied flag */
    public void setFineApplied(boolean fineApplied) { this.fineApplied = fineApplied; }

    /** Checks if the item is returned */
    public boolean isReturned() { return item.isAvailable(); }

    /** Returns the fine as of currentDate */
    public BigDecimal getFine(LocalDate currentDate) {
        calculateFine(currentDate);
        return fine;
    }

    /** Returns fine already paid */
    public BigDecimal getFinePaid() { return finePaid; }

    /** Sets fine paid (validated) */
    public void setFinePaid(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Paid amount cannot be null or negative");
        if (amount.compareTo(fine) > 0)
            throw new IllegalArgumentException("Paid amount cannot exceed fine");
        this.finePaid = amount;
    }

    /** Returns remaining unpaid fine */
    public BigDecimal getRemainingFine() {
        return fine.subtract(finePaid);
    }

    /** Calculates fine based on overdue days using the FineContext */
    public void calculateFine(LocalDate currentDate) {
        if (currentDate == null)
            throw new IllegalArgumentException("currentDate cannot be null.");
        if (!item.isAvailable() && currentDate.isAfter(dueDate)) {
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, currentDate);
            fine = fineContext.calculateFine(daysOverdue);
        } else {
            fine = BigDecimal.ZERO;
        }
    }

    /** Applies fine to user once */
    public void applyFineToUser(LocalDate currentDate) {
        calculateFine(currentDate);
        if (!fineApplied && fine.compareTo(BigDecimal.ZERO) > 0) {
            user.addFine(fine);
            fineApplied = true;
        }
    }

    /** Marks item as returned and applies any overdue fine */
    public void markReturned(LocalDate returnDate) {
        applyFineToUser(returnDate);
        item.returnItem();
    }

    /** Checks if item is overdue */
    public boolean isOverdue(LocalDate currentDate) {
        if (currentDate == null)
            throw new IllegalArgumentException("currentDate cannot be null.");
        return !item.isAvailable() && currentDate.isAfter(dueDate);
    }

    /** String representation */
    @Override
    public String toString() {
        return String.format("%s borrowed \"%s\" on %s (due: %s) | Returned: %b | Fine: %s",
                user.getUsername(), item.getTitle(), borrowDate, dueDate, isReturned(), fine);
    }
}


