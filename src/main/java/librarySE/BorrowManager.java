package librarySE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the borrowing and returning process of books in the library system.
 * <p>
 * This class handles all borrowing operations, fine calculations, overdue checking,
 * and fine payments for users. It ensures that users cannot borrow books if they
 * have unpaid fines and cannot pay fines before returning all borrowed books.
 * </p>
 * 
 * @author Eman
 */
public class BorrowManager {

    /** A list that stores all borrowing records in the system. */
    private List<BorrowRecord> records;

    /** The fine amount charged per day for overdue books. */
    private double finePerDay;

    /**
     * Constructs a new BorrowManager with the specified daily fine rate.
     *
     * @param finePerDay the fine charged per day for overdue books
     */
    public BorrowManager(double finePerDay) {
        this.records = new ArrayList<>();
        this.finePerDay = finePerDay;
    }

    /**
     * Allows a user to borrow a book if they have no unpaid fines and the book is available.
     * <p>
     * A new {@link BorrowRecord} is created when the borrowing is successful.
     * </p>
     *
     * @param user the user borrowing the book
     * @param book the book to be borrowed
     * @throws IllegalArgumentException if the user has unpaid fines or if the book is unavailable
     */
    public void borrowBook(User user, Book book) {
        if (!user.canBorrow()) {
            throw new IllegalArgumentException("User has unpaid fines and cannot borrow new books.");
        }
        if (!book.isAvailable()) {
            throw new IllegalArgumentException("Book is currently not available.");
        }

        BorrowRecord record = new BorrowRecord(user, book);
        book.borrow();
        records.add(record);
    }

    /**
     * Handles the return process for a borrowed book and calculates any overdue fines.
     * <p>
     * When a book is returned, this method calculates the fine (if overdue),
     * adds it to the user's total fines, marks the record as returned,
     * and updates the book’s availability.
     * </p>
     *
     * @param record the borrowing record representing the borrowed book
     */
    public void returnBook(BorrowRecord record) {
        record.calculateFine(finePerDay, LocalDate.now());
        record.getUser().addFine(record.getFine());
        record.markReturned();
        record.getBook().returnBook();
    }

    /**
     * Allows a user to pay their fine only if all their borrowed books have been returned.
     * <p>
     * This method prevents users from paying fines while still having borrowed (unreturned) books.
     * </p>
     *
     * @param user   the user attempting to pay the fine
     * @param amount the amount of money the user wishes to pay
     * @throws IllegalStateException if the user still has unreturned books
     */
    public void payFine(User user, double amount) {
        boolean hasUnreturnedBooks = records.stream()
                .anyMatch(r -> r.getUser().equals(user) && !r.isReturned());

        if (hasUnreturnedBooks) {
            throw new IllegalStateException("User must return all borrowed books before paying fines.");
        }

        user.payFine(amount);
    }

    /**
     * Returns a list of all overdue borrowing records as of today.
     * <p>
     * This method checks each record’s due date and collects all that are overdue.
     * </p>
     *
     * @return a list of {@link BorrowRecord} objects representing overdue borrowings
     */
    public List<BorrowRecord> getOverdueRecords() { 
    	LocalDate today = LocalDate.now(); 
    	List<BorrowRecord> overdue = new ArrayList<>(); 
    	for (BorrowRecord r : records) { 
    		if (r.isOverdue(today)) { 
    			overdue.add(r); 
    			} } 
    	return overdue; 
    	}


    /**
     * Returns a list of all borrowing records in the system.
     *
     * @return a list containing all {@link BorrowRecord} objects
     */
    public List<BorrowRecord> getAllRecords() {
        return new ArrayList<>(records);
    }

    /**
     * Returns the current fine rate per day.
     *
     * @return the daily fine amount
     */
    public double getFinePerDay() {
        return finePerDay;
    }

    /**
     * Updates the fine amount charged per overdue day.
     *
     * @param finePerDay the new fine rate per day
     */
    public void setFinePerDay(double finePerDay) {
        this.finePerDay = finePerDay;
    }
}



