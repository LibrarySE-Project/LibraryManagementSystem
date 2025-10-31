package librarySE;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a library that manages library items, users, and borrowing operations.
 * <p>
 * The {@code Library} class supports adding items, borrowing and returning items,
 * applying overdue fines, sending reminders to users, and searching books.
 * Each library maintains lists of {@link LibraryItem}, {@link User}, and {@link BorrowRecord}.
 * </p>
 * 
 * @author Malak
 */
public class Library {

    /** All library items stored (books, CDs, journals, etc.) */
    private List<LibraryItem> items;

    /** All borrow records for tracking loans and fines */
    private List<BorrowRecord> borrowRecords;
    
    /** All registered users in the library system */
    private List<User> users;

    /** Constructs a new, empty library */
    public Library() {
        users = new ArrayList<>();
        items = new ArrayList<>();
        borrowRecords = new ArrayList<>();
    }

    /**
     * Adds a new library item to the collection.
     * <p>
     * Only users with admin privileges can add items.
     * </p>
     *
     * @param item the library item to add (non-null)
     * @param user the user attempting to add the item (must be admin)
     * @throws IllegalArgumentException if item or user is null, or user is not an admin
     */
    public void addItem(LibraryItem item, User user) {
        if (item == null || user == null)
            throw new IllegalArgumentException("Item and User cannot be null.");
        if (!user.isAdmin())
            throw new IllegalArgumentException("Only admins can add items.");

        items.add(item);
    }

    /**
     * Borrows a library item for a user.
     * <p>
     * Checks for outstanding fines or overdue items before borrowing.
     * Creates a {@link BorrowRecord} for the transaction and adds it to both
     * the library's and user's records. Uses {@link LocalDate#now()} as the borrow date.
     * For testing, a specific date can be used via the {@link BorrowRecord} constructor.
     * </p>
     *
     * @param user the user borrowing the item (non-null)
     * @param title the title of the item to borrow (non-null)
     * @return {@code true} if borrowing succeeds, {@code false} otherwise
     * @throws IllegalArgumentException if arguments are null or item unavailable
     * @throws IllegalStateException if the user has unpaid fines or overdue items
     */
    public boolean borrowItem(User user, String title) {
        if (user == null || title == null)
            throw new IllegalArgumentException("Arguments cannot be null.");

        applyOverdueFines(LocalDate.now());

        if (user.hasOutstandingFine())
            throw new IllegalStateException("User has unpaid fines or overdue items, cannot borrow.");

        LibraryItem item = items.stream()
                .filter(i -> i.getTitle().equalsIgnoreCase(title) && i.isAvailable())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found or unavailable."));

        MaterialFineStrategy fineStrategy = new MaterialFineStrategy(item.getMaterialType());

        if (!item.borrow()) return false;

        BorrowRecord record = new BorrowRecord(user, item, fineStrategy, LocalDate.now());
        borrowRecords.add(record);
        user.addBorrowRecord(record);

        return true;
    }

    /**
     * Returns a borrowed library item for a user.
     * <p>
     * Updates both the library's and the user's borrow records.
     * Marks the item as returned and applies any overdue fines.
     * Uses {@link LocalDate#now()} as the return date.
     * For testing, a specific date can be passed to {@link BorrowRecord#markReturned(LocalDate)}.
     * </p>
     *
     * @param user the user returning the item (non-null)
     * @param item the item being returned (non-null)
     * @throws IllegalArgumentException if user or item is null or no active borrow record exists
     */
    public void returnItem(User user, LibraryItem item) {
        if (user == null || item == null)
            throw new IllegalArgumentException("User and Item cannot be null.");

        BorrowRecord record = borrowRecords.stream()
                .filter(r -> r.getItem().equals(item) && r.getUser().equals(user) && !r.isReturned())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No active borrowing found."));

        record.markReturned(LocalDate.now());
        user.removeBorrowRecord(record);
    }

    /**
     * Applies overdue fines to all borrow records as of the given date.
     * <p>
     * The provided {@code date} is used to calculate overdue fines, 
     * allowing for testing with arbitrary dates instead of the current date.
     * </p>
     *
     * @param date the current date used to calculate overdue fines (non-null)
     */
    public void applyOverdueFines(LocalDate date) {
        for (BorrowRecord record : borrowRecords) {
            if (record.isOverdue(date)) {
                record.applyFineToUser(date);
            }
        }
    }

    /**
     * Returns all borrow records that are overdue as of the given date.
     * <p>
     * Applies overdue fines before returning the list.
     * The {@code date} parameter allows checking overdue status for any date.
     * </p>
     *
     * @param date the current date for overdue checking (non-null)
     * @return list of overdue borrow records
     */
    public List<BorrowRecord> getOverdueItems(LocalDate date) {
        applyOverdueFines(date);
        return borrowRecords.stream()
                .filter(r -> r.isOverdue(date))
                .collect(Collectors.toList());
    }

    /**
     * Searches for books matching a keyword in title, author, or ISBN.
     *
     * @param keyword the search term (non-null)
     * @return list of books matching the keyword
     * @throws IllegalArgumentException if keyword is null
     */
    public List<Book> searchBooks(String keyword) {
        if (keyword == null)
            throw new IllegalArgumentException("Keyword cannot be null.");
        String lower = keyword.toLowerCase();

        return items.stream()
                .filter(i -> i instanceof Book)
                .map(i -> (Book) i)
                .filter(b -> b.getTitle().toLowerCase().contains(lower)
                        || b.getAuthor().toLowerCase().contains(lower)
                        || b.getIsbn().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    /** Returns all library items as a new list (protects internal list) */
    public List<LibraryItem> getAllItems() {
        return new ArrayList<>(items);
    }

    /** Returns all borrow records as a new list (applies overdue fines first using {@link LocalDate#now()}) */
    public List<BorrowRecord> getAllBorrowRecords() {
        applyOverdueFines(LocalDate.now());
        return new ArrayList<>(borrowRecords);
    }

    /**
     * Sends reminder notifications to users with overdue items.
     * <p>
     * Uses the current date ({@link LocalDate#now()}) to check for overdue items.
     * Uses the given {@link Observer} for sending notifications (mock or real).
     * </p>
     *
     * @param notifier the observer responsible for sending notifications (non-null)
     * @throws IllegalArgumentException if notifier is null
     */
    public void sendReminders(Observer notifier) {
        if (notifier == null) throw new IllegalArgumentException("Notifier cannot be null");

        List<BorrowRecord> overdueRecords = getOverdueItems(LocalDate.now());

        Map<User, List<BorrowRecord>> byUser = overdueRecords.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getUser));

        for (User user : byUser.keySet()) {
            int count = byUser.get(user).size();
            String message = "You have " + count + " overdue book(s).";
            notifier.notify(user, message);
        }
    }

    /** Returns the list of registered users in the library */
    public List<User> getUsers() {
        return users;
    }

    /** Returns the list of all borrow records in the library */
    public List<BorrowRecord> getBorrowRecords() {
        return borrowRecords;
    }
}


