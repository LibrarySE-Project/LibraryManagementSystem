package librarySE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a single borrowing record in the library system.
 * <p>
 * Each record connects a {@link User} with a {@link Book}, tracking the borrow date,
 * due date, return status, and applicable fines for overdue items.
 * </p>
 * 
 * @author Eman
 */
public class BorrowRecord {

    private static final int BORROW_PERIOD_DAYS = 28;
    private static final BigDecimal FINE_PER_DAY = BigDecimal.valueOf(5);

    private final User user;
    private final Book book;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private boolean returned;
    private BigDecimal fine;

    /**
     * Constructs a borrowing record for a given user and book.
     * 
     * @param user the user borrowing the book
     * @param book the borrowed book
     * @throws IllegalArgumentException if either argument is null
     */
    public BorrowRecord(User user, Book book) {
        if (user == null || book == null) {
            throw new IllegalArgumentException("User and book cannot be null.");
        }
        this.user = user;
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(BORROW_PERIOD_DAYS);
        this.returned = false;
        this.fine = BigDecimal.ZERO;
    }

    /** @return the user who borrowed the book */
    public User getUser() { return user; }

    /** @return the borrowed book */
    public Book getBook() { return book; }

    /** @return the date when the book was borrowed */
    public LocalDate getBorrowDate() { return borrowDate; }

    /** @return the due date when the book should be returned */
    public LocalDate getDueDate() { return dueDate; }

    /** @return true if the book has been returned, false otherwise */
    public boolean isReturned() { return returned; }

    /**
     * Returns the current fine for this borrowing record.
     * <p>
     * The fine is automatically recalculated based on the provided date.
     * </p>
     * 
     * @param currentDate the date used to check if the item is overdue
     * @return the fine amount
     * @throws IllegalArgumentException if {@code currentDate} is null
     */
    public BigDecimal getFine(LocalDate currentDate) {
        calculateFine(currentDate);
        return fine;
    }

    /**
     * Calculates and updates the fine based on the number of overdue days.
     * <p>
     * The fine is applied only if the book is overdue and not yet returned.
     * </p>
     * 
     * @param currentDate the date used to determine overdue status
     * @throws IllegalArgumentException if {@code currentDate} is null
     */
    public void calculateFine(LocalDate currentDate) {
        if (currentDate == null)
            throw new IllegalArgumentException("currentDate cannot be null.");
        if (!returned && currentDate.isAfter(dueDate)) {
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, currentDate);
            fine = FINE_PER_DAY.multiply(BigDecimal.valueOf(daysOverdue));
        } else {
            fine = BigDecimal.ZERO;
        }
    }

    /**
     * Marks the book as returned.
     * <p>
     * Once marked as returned, the fine value is reset to zero.
     * </p>
     */
    public void markReturned() {
        this.returned = true;
        this.fine = BigDecimal.ZERO;
    }

    /**
     * Checks if the book is currently overdue.
     * 
     * @param currentDate the date used to evaluate overdue status
     * @return true if the book is overdue and not returned; false otherwise
     * @throws IllegalArgumentException if {@code currentDate} is null
     */
    public boolean isOverdue(LocalDate currentDate) {
        if (currentDate == null)
            throw new IllegalArgumentException("currentDate cannot be null.");
        return !returned && currentDate.isAfter(dueDate);
    }

    /**
     * Returns a formatted string representation of the borrowing record.
     * 
     * @return a string describing the user, book, borrow date, due date, and fine
     */
    @Override
    public String toString() {
        return String.format("%s borrowed \"%s\" on %s (due: %s) | Returned: %b | Fine: %s",
                user.getUsername(), book.getTitle(), borrowDate, dueDate, returned, fine);
    }
}
