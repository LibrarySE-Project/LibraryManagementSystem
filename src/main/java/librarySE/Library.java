package librarySE;

import java.math.BigDecimal;
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
     * Returns all borrow records for a specific user.
     *
     * @param user the user whose borrow records are requested
     * @return list of borrow records belonging to the given user
     * @throws IllegalArgumentException if user is null
     */
    public List<BorrowRecord> getBorrowRecordsForUser(User user) {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null.");

        return borrowRecords.stream()
                .filter(record -> record.getUser().equals(user))
                .collect(Collectors.toList());
    }

    /**
     * Borrows a specific library item for a user.
     * <p>
     * Checks for outstanding fines or overdue items before borrowing.
     * Creates a {@link BorrowRecord} for the transaction and adds it to both
     * the library's and user's records. Uses {@link LocalDate#now()} as the borrow date.
     * </p>
     *
     * @param user the user borrowing the item (non-null)
     * @param item the library item to borrow (non-null)
     * @return {@code true} if borrowing succeeds, {@code false} otherwise
     * @throws IllegalArgumentException if arguments are null
     * @throws IllegalStateException if the user has unpaid fines, overdue items, 
     *         or the item is already borrowed
     */
    public boolean borrowItem(User user, LibraryItem item) {
        if (user == null || item == null)
            throw new IllegalArgumentException("User and item cannot be null.");

        LocalDate today = LocalDate.now();
        applyOverdueFines(today);

        if (user.hasOutstandingFine())
            throw new IllegalStateException("Cannot borrow: you have unpaid fines.");

        boolean hasOverdue = getBorrowRecordsForUser(user).stream()
                .anyMatch(record -> record.isOverdue(today));
        if (hasOverdue)
            throw new IllegalStateException("Cannot borrow: you have overdue items.");

        synchronized (item) {
            if (!item.isAvailable())
                throw new IllegalStateException("Item is already borrowed.");

            if (!item.borrow())
                return false;   
        }

        MaterialFineStrategy fineStrategy = new MaterialFineStrategy(item.getMaterialType());
        BorrowRecord record = new BorrowRecord(user, item, fineStrategy, today);
        borrowRecords.add(record);

        return true;
    }

    /**
     * Calculates the total fines owed by a specific user as of a given date.
     * <p>
     * Iterates over all borrow records for the user and sums the fines calculated
     * for the specified date. Useful for displaying total outstanding fines.
     * </p>
     *
     * @param user the user whose total fines are being calculated (non-null)
     * @param date the date used to calculate fines (non-null)
     * @return the total fines as a {@link BigDecimal}, never null
     * @throws IllegalArgumentException if user or date is null
     */
    public BigDecimal calculateTotalFines(User user, LocalDate date) {
        if (user == null || date == null) {
            throw new IllegalArgumentException("User and date cannot be null.");
        }

        return borrowRecords.stream()
            .filter(r -> r.getUser().equals(user))
            .map(r -> r.getFine(date))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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
     * Searches for library items (books, CDs, journals) that match the given keyword.
     * <p>
     * The search is case-insensitive and checks relevant fields for each type:
     * <ul>
     *     <li>Book: title, author, ISBN</li>
     *     <li>CD: title, artist</li>
     *     <li>Journal: title, issue number</li>
     * </ul>
     * </p>
     *
     * @param keyword the keyword to search for; must not be null
     * @return a list of {@link LibraryItem} objects that match the keyword
     * @throws IllegalArgumentException if the keyword is null
     */
    public List<LibraryItem> searchItems(String keyword) {
        if (keyword == null) throw new IllegalArgumentException("Keyword cannot be null.");
        String search = keyword.trim().toLowerCase();

        return items.stream()
                .filter(item -> {
                    if (item instanceof Book book) {
                        return book.getTitle().toLowerCase().contains(search)
                            || book.getAuthor().toLowerCase().contains(search)
                            || book.getIsbn().toLowerCase().contains(search);
                    } else if (item instanceof CD cd) {
                        return cd.getTitle().toLowerCase().contains(search)
                            || cd.getArtist().toLowerCase().contains(search);
                    } else if (item instanceof Journal journal) {
                        return journal.getTitle().toLowerCase().contains(search)
                            || journal.getIssueNumber().toLowerCase().contains(search);
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    /** Returns all library items as a new list (protects internal list) */
    public List<LibraryItem> getAllItems() {
        return new ArrayList<>(items);
    }

    /**
     * Returns all borrow records as a new list.
     * <p>
     * Before returning, it applies overdue fines for each record using the provided {@code date}.
     * This allows checking fines for a specific date instead of always using the current date.
     * </p>
     *
     * @param date the date to use for calculating overdue fines; must not be {@code null}
     * @return a new list containing all borrow records
     * @throws IllegalArgumentException if {@code date} is null
     */
    public List<BorrowRecord> getAllBorrowRecords(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Date cannot be null.");
        applyOverdueFines(date);
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
            String message = "You have " + count + " overdue item(s).";
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


