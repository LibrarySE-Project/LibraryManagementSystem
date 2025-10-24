package librarySE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
/**
 * Represents a single borrowing record in the library system.
 * <p>
 * Each record links a {@link User} with a {@link Book}, tracking the borrow date,
 * due date, return status, and any fine applied when the book is overdue.
 * </p>
 * <p>
 * The fine is not calculated automatically; it is handled by {@link BorrowManager}
 * when the user returns the book.
 * </p>
 * 
 * @author Eman
 */
public class BorrowRecord {

    /** Number of days a book can be borrowed before it becomes overdue. */
    private static final int BORROW_PERIOD_DAYS = 28;

    /** The user who borrowed the book. */
    private final User user;

    /** The book that has been borrowed. */
    private final Book book;

    /** The date when the book was borrowed. */
    private final LocalDate borrowDate;

    /** The due date for returning the book. */
    private final LocalDate dueDate;

    /** Indicates whether the book has been returned. */
    private boolean returned;

    /** The fine amount assigned to this record (if overdue). */
    private BigDecimal fine;

    /**
     * Constructs a new BorrowRecord with the specified user and book.
     * <p>
     * The borrow date is set to today, and the due date is automatically set
     * to {@link #BORROW_PERIOD_DAYS} after the borrow date.
     * </p>
     *
     * @param user the user borrowing the book
     * @param book the book being borrowed
     */
    public BorrowRecord(User user, Book book) {
        this.user = user;
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(BORROW_PERIOD_DAYS);
        this.returned = false;
        this.fine = BigDecimal.ZERO;
    }

    public User getUser() { return user; }
    public Book getBook() { return book; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isReturned() { return returned; }
    public BigDecimal getFine() { return fine; }

    /**
     * Calculates the fine based on the number of overdue days and the fine rate per day.
     * <p>
     * The fine is only applied if the book is overdue and not yet returned.
     * </p>
     *
     * @param finePerDay  the daily fine amount
     * @param currentDate the current date used to check overdue status
     */
    public void calculateFine(BigDecimal finePerDay, LocalDate currentDate) {
        if (!returned && currentDate.isAfter(dueDate)) {
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, currentDate);
            fine = finePerDay.multiply(BigDecimal.valueOf(daysOverdue));
        } else {
            fine = BigDecimal.ZERO;
        }
    }

    /**
     * Marks the book as returned.
     */
    public void markReturned() {
        returned = true;
    }

    /**
     * Checks whether this borrowing record is overdue as of the given date.
     *
     * @param currentDate the date to check against the due date
     * @return true if the book is overdue and not returned, false otherwise
     */
    public boolean isOverdue(LocalDate currentDate) {
        return !returned && currentDate.isAfter(dueDate);
    }

    @Override
    public String toString() {
        return String.format(
                "%s borrowed \"%s\" on %s, due on %s, returned: %b, fine: %s",
                user.getUsername(), book.getTitle(), borrowDate, dueDate, returned, fine
        );
    }
}



