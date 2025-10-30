package librarySE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a single borrowing record in the library system.
 * <p>
 * Each record links a {@link User} with a {@link LibraryItem}, tracking the borrow date,
 * due date, and applicable fines for overdue items. This class implements the
 * Strategy Pattern for fine calculation using a {@link FineStrategy}.
 * </p>
 * 
 * <p>
 * The availability of the item is controlled via the {@link LibraryItem} interface.
 * An item is considered returned if {@link LibraryItem#isAvailable()} returns {@code true}.
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
    
    /** Indicates if the overdue fine has already been applied to the user to avoid double charging. */
    private boolean fineApplied = false;


    /**
     * Constructs a borrowing record for a given user, item, and fine strategy.
     * <p>
     * Upon construction, the item is marked as unavailable (borrowed).
     * </p>
     *
     * @param user the user borrowing the item; must not be {@code null}
     * @param item the borrowed item; must not be {@code null}
     * @param fineStrategy the fine strategy to calculate overdue fines; must not be {@code null}
     * @throws IllegalArgumentException if any argument is {@code null}
     */
    public BorrowRecord(User user, LibraryItem item, FineStrategy fineStrategy) {
        if (user == null || item == null || fineStrategy == null) {
            throw new IllegalArgumentException("User, item, and fineStrategy cannot be null.");
        }
        this.user = user;
        this.item = item;
        this.fineStrategy = fineStrategy;
        this.borrowPeriodDays = this.fineStrategy.getBorrowPeriodDays();
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(borrowPeriodDays);
        this.fine = BigDecimal.ZERO;
        item.borrow();
    }

    /** Returns the user who borrowed the item. */
    public User getUser() { return user; }

    /** Returns the borrowed item. */
    public LibraryItem getItem() { return item; }

    /** Returns the date when the item was borrowed. */
    public LocalDate getBorrowDate() { return borrowDate; }

    /** Returns the due date for returning the item. */
    public LocalDate getDueDate() { return dueDate; }

    /** Checks if the item has been returned. */
    public boolean isReturned() { return item.isAvailable(); }

    /** Returns the current fine for this borrowing record. */
    public BigDecimal getFine(LocalDate currentDate) {
        calculateFine(currentDate);
        return fine;
    }

    /** Calculates and updates the fine based on overdue days. */
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

    /** Applies the current fine to the user's account. */
    public void applyFineToUser(LocalDate currentDate) {
        calculateFine(currentDate);
        if (!fineApplied && fine.compareTo(BigDecimal.ZERO) > 0) {
            user.addFine(fine);
            fineApplied = true;
        }
    }

    /** Marks the item as returned and applies any overdue fines to the user. */
    public void markReturned() {
        applyFineToUser(LocalDate.now());
        item.returnItem();
    }

    /** Checks if the item is currently overdue. */
    public boolean isOverdue(LocalDate currentDate) {
        if (currentDate == null)
            throw new IllegalArgumentException("currentDate cannot be null.");
        return !item.isAvailable() && currentDate.isAfter(dueDate);
    }

    /** Returns a string representation of the borrowing record. */
    @Override
    public String toString() {
        return String.format("%s borrowed \"%s\" on %s (due: %s) | Returned: %b | Fine: %s",
                user.getUsername(), item.getTitle(), borrowDate, dueDate, isReturned(), fine);
    }
}
