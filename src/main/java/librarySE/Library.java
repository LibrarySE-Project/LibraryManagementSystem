package librarySE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a library that manages books, users, and borrowing operations.
 * <p>
 * This class handles adding books, borrowing and returning them,
 * tracking overdue records, and applying fines automatically.
 * </p>
 * 
 * @author Malak
 */
public class Library {

    /** The list of all books stored in the library. */
    private List<Book> books;

    /** The list of all borrow records in the library. */
    private List<BorrowRecord> borrowRecords;

    /**
     * Constructs an empty Library instance with initialized book and record lists.
     */
    public Library() {
        books = new ArrayList<>();
        borrowRecords = new ArrayList<>();
    }

    /**
     * Adds a new book to the library (only admins are allowed).
     *
     * @param book the book to be added (must not be null)
     * @param user the user attempting to add the book (must be admin)
     * @throws IllegalArgumentException if book or user is null, 
     *                                  or if a book with the same ISBN already exists,
     *                                  or if the user is not an admin
     */
    public void addBook(Book book, User user) {
        if (book == null)
            throw new IllegalArgumentException("Book cannot be null");
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");
        if (!user.isAdmin())
            throw new IllegalArgumentException("Only admins can add books!");

        boolean exists = books.stream()
                              .anyMatch(b -> b.getIsbn().equals(book.getIsbn()));
        if (exists)
            throw new IllegalArgumentException(
                "Book with ISBN " + book.getIsbn() + " already exists!"
            );

        books.add(book);
    }

    /**
     * Allows a user to borrow a book if it is available and the user has no unpaid fines.
     *
     * @param user the user borrowing the book
     * @param isbn the ISBN of the book to borrow
     * @return true if borrowing succeeded, false if the book is already borrowed
     * @throws IllegalArgumentException if user or ISBN is null, or book is not found
     * @throws IllegalStateException if the user has unpaid fines
     */
    public boolean borrowBook(User user, String isbn) {
        if (user == null || isbn == null)
            throw new IllegalArgumentException("User and ISBN cannot be null.");

        applyOverdueFines(LocalDate.now());

        if (!user.canBorrow())
            throw new IllegalStateException("User has unpaid fines and cannot borrow books.");

        Book book = books.stream()
                .filter(b -> b.getIsbn().equals(isbn))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));

        if (!book.borrow())
            return false;

        BorrowRecord record = new BorrowRecord(user, book);
        borrowRecords.add(record);
        return true;
    }

    /**
     * Marks a borrowed book as returned.
     *
     * @param user the user returning the book
     * @param isbn the ISBN of the returned book
     * @throws IllegalArgumentException if no active borrow record is found
     */
    public void returnBook(User user, String isbn) {
        applyOverdueFines(LocalDate.now());

        BorrowRecord record = borrowRecords.stream()
                .filter(r -> r.getBook().getIsbn().equals(isbn) && r.getUser().equals(user) && !r.isReturned())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No active borrowing found."));

        record.markReturned();
        record.getBook().returnBook();
    }

    /**
     * Applies overdue fines for all users with overdue borrow records.
     *
     * @param today the date used to check for overdue status
     */
    public void applyOverdueFines(LocalDate today) {
        for (BorrowRecord record : borrowRecords) {
            if (record.isOverdue(today)) {
                BigDecimal fineAmount = record.getFine(today);
                if (fineAmount.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal currentFine = record.getUser().getFineBalance();
                    BigDecimal newFine = fineAmount.max(currentFine);
                    record.getUser().addFine(newFine.subtract(currentFine));
                }
            }
        }
    }

    /**
     * Returns a list of all overdue borrow records as of the given date.
     *
     * @param today the date to check for overdue records
     * @return a list of overdue borrow records
     */
    public List<BorrowRecord> getOverdueBooks(LocalDate today) {
        applyOverdueFines(today);
        return borrowRecords.stream()
                .filter(r -> r.isOverdue(today))
                .collect(Collectors.toList());
    }

    /**
     * Searches for books in the library by title, author, or ISBN (case-insensitive).
     *
     * @param keyword the search keyword
     * @return a list of books that match the search keyword
     * @throws IllegalArgumentException if the keyword is null
     */
    public List<Book> searchBook(String keyword) {
        if (keyword == null)
            throw new IllegalArgumentException("Search keyword cannot be null");
        String lowerKeyword = keyword.toLowerCase();
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerKeyword)
                        || b.getAuthor().toLowerCase().contains(lowerKeyword)
                        || b.getIsbn().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    /**
     * Returns the total number of books stored in the library.
     *
     * @return the number of books in the library
     */
    public int getBookCount() {
        return books.size();
    }

    /**
     * Returns a copy of all books currently stored in the library.
     *
     * @return a list of all books
     */
    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    /**
     * Returns a copy of all borrow records currently stored in the library.
     * Fines are updated before returning the list.
     *
     * @return a list of all borrow records
     */
    public List<BorrowRecord> getAllBorrowRecords() {
        applyOverdueFines(LocalDate.now());
        return new ArrayList<>(borrowRecords);
    }
}
