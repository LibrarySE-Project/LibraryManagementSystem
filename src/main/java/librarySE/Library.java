package librarySE;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a library that manages library items, users, and borrowing operations.
 * Supports borrowing, returning, fine application, overdue notifications, and search.
 * 
 * 
 * @author Malak
 */
public class Library {

    /** All library items stored (books, CDs, journals, etc.) */
    private List<LibraryItem> items;

    /** All borrow records */
    private List<BorrowRecord> borrowRecords;

    public Library() {
        items = new ArrayList<>();
        borrowRecords = new ArrayList<>();
    }

    /** Add a new library item (only admins allowed) */
    public void addItem(LibraryItem item, User user) {
        if (item == null || user == null)
            throw new IllegalArgumentException("Item and User cannot be null.");
        if (!user.isAdmin())
            throw new IllegalArgumentException("Only admins can add items.");

        items.add(item);
    }

    /** Borrow a library item */
    public boolean borrowItem(User user, String title, FineStrategy fineStrategy) {
        if (user == null || title == null || fineStrategy == null)
            throw new IllegalArgumentException("Arguments cannot be null.");

        applyOverdueFines(LocalDate.now());//Apply late fines before borrowing
        
        //Ensure the user does not have fines or overdue items
        if (user.hasOutstandingFine())
            throw new IllegalStateException("User has unpaid fines or overdue items, cannot borrow.");
        //Search for available item
        LibraryItem item = items.stream()
                .filter(i -> i.getTitle().equalsIgnoreCase(title) && i.isAvailable())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found or unavailable."));
        //Update loan status
        if (!item.borrow())
            return false;
        //Create a new loan record
        BorrowRecord record = new BorrowRecord(user, item, fineStrategy);
        borrowRecords.add(record);
        return true;
    }

    /** Return a borrowed library item */
    public void returnItem(User user, LibraryItem item) {
        if (user == null || item == null)
            throw new IllegalArgumentException("User and Item cannot be null.");

        BorrowRecord record = borrowRecords.stream()
                .filter(r -> r.getItem().equals(item) && r.getUser().equals(user) && !r.isReturned())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No active borrowing found."));

        record.markReturned();
    }

    /** Apply overdue fines to all borrow records */
    public void applyOverdueFines(LocalDate date) {
    	/*Passes through each borrow record.
         If the item is overdue (isOverdue) → applyFineToUser.*/
        for (BorrowRecord record : borrowRecords) {
            if (record.isOverdue(date)) {
                record.applyFineToUser(date);
            }
        }
    }

    /** Get all overdue borrow records */
    public List<BorrowRecord> getOverdueItems(LocalDate date) {
        applyOverdueFines(date);
        return borrowRecords.stream()
                .filter(r -> r.isOverdue(date))
                .collect(Collectors.toList());
    }

    /** Search books by title, author, or ISBN */
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

    /** Get all library items */
    public List<LibraryItem> getAllItems() {
        return new ArrayList<>(items);
    }

    /** Get all borrow records */
    public List<BorrowRecord> getAllBorrowRecords() {
        applyOverdueFines(LocalDate.now());
        return new ArrayList<>(borrowRecords);
    }

    /**
     * Sends reminder notifications to users with overdue items.
     * Works with any Observer implementation (mock or real email).
     *
     * @param notifier the observer responsible for sending notifications
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
}
