package librarySE.managers;

import librarySE.core.LibraryItem;
import librarySE.core.WaitlistEntry;
import librarySE.managers.notifications.EmailNotifier;
import librarySE.managers.notifications.Notifier;
import librarySE.repo.BorrowRecordRepository;
import librarySE.repo.WaitlistRepository;
import librarySE.strategy.FineStrategy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Central manager responsible for handling all borrowing and returning operations in the library system.
 * <p>
 * It manages:
 * <ul>
 *     <li>Borrowing and returning of library items</li>
 *     <li>Automatic fine calculation for overdue items</li>
 *     <li>Waitlist management for unavailable items</li>
 *     <li>Email notifications to users when a waited item becomes available</li>
 * </ul>
 * </p>
 *
 * <p>
 * Implements the <b>Singleton Pattern</b> to ensure only one global instance manages borrow operations.
 * </p>
 *
 * <p><b>Note:</b> Email notifications require a configured {@link librarySE.core.EmailService}
 * with valid credentials in the <b>.env</b> file.</p>
 *
 * @author Malak
 */
public class BorrowManager {

    /** Singleton instance. */
    private static BorrowManager instance;

    /** Thread-safe list of borrowing records. */
    private final CopyOnWriteArrayList<BorrowRecord> borrowRecords;

    /** Thread-safe list of waitlist entries. */
    private final CopyOnWriteArrayList<WaitlistEntry> waitlist;

    /** Repository for saving and loading borrow records. */
    private final BorrowRecordRepository borrowRepo;

    /** Repository for saving and loading waitlist entries. */
    private final WaitlistRepository waitlistRepo;

    /**
     * Manager for library items. Used so that borrow / return operations
     * can persist updated copy counts to {@code items.json}.
     */
    private final ItemManager itemManager;

    /**
     * Private constructor used for initialization.
     *
     * @param borrowRepo   repository for borrow records (JSON-based)
     * @param waitlistRepo repository for waitlist entries (JSON-based)
     * @param itemManager  manager used to persist changes to {@link LibraryItem} objects
     */
    private BorrowManager(BorrowRecordRepository borrowRepo,
                          WaitlistRepository waitlistRepo,
                          ItemManager itemManager) {
        this.borrowRepo = Objects.requireNonNull(borrowRepo, "BorrowRecordRepository cannot be null.");
        this.waitlistRepo = Objects.requireNonNull(waitlistRepo, "WaitlistRepository cannot be null.");
        this.itemManager = Objects.requireNonNull(itemManager, "ItemManager cannot be null.");

        this.borrowRecords = new CopyOnWriteArrayList<>(borrowRepo.loadAll());
        this.waitlist = new CopyOnWriteArrayList<>(waitlistRepo.loadAll());
    }

    /**
     * Initializes the singleton instance.
     *
     * @param borrowRepo   repository for borrow records
     * @param waitlistRepo repository for waitlist entries
     * @param itemManager  manager for items (for persisting copy counts)
     * @return the initialized {@link BorrowManager} instance
     */
    public static synchronized BorrowManager init(BorrowRecordRepository borrowRepo,
                                                  WaitlistRepository waitlistRepo,
                                                  ItemManager itemManager) {
        if (instance == null) {
            instance = new BorrowManager(borrowRepo, waitlistRepo, itemManager);
        }
        return instance;
    }

    /**
     * Gets the active {@link BorrowManager} instance.
     *
     * @return the BorrowManager instance
     * @throws IllegalStateException if not initialized yet
     */
    public static synchronized BorrowManager getInstance() {
        if (instance == null) throw new IllegalStateException("BorrowManager not initialized.");
        return instance;
    }

    // =====================================================================
    // Borrow / Return
    // =====================================================================

    /**
     * Attempts to borrow an item for a user.
     * <p>If the item is unavailable, the user is automatically added to the waitlist.</p>
     *
     * @param user the user borrowing the item
     * @param item the item to borrow
     * @return {@code true} if borrowed successfully; {@code false} if added to waitlist
     * @throws IllegalArgumentException if user or item is null
     * @throws IllegalStateException    if user has unpaid fines or overdue items
     */
    public boolean borrowItem(User user, LibraryItem item) {
        if (user == null || item == null)
            throw new IllegalArgumentException("User and item cannot be null.");

        LocalDate today = LocalDate.now();

        // Make sure all overdue records have their fines applied to the user
        applyOverdueFines(today);

        // Block borrowing if user has unpaid fines
        if (user.hasOutstandingFine())
            throw new IllegalStateException("Cannot borrow: unpaid fines exist.");

        // Block borrowing if user still has overdue items
        boolean hasOverdue = getBorrowRecordsForUser(user)
                .stream().anyMatch(r -> r.isOverdue(today));
        if (hasOverdue)
            throw new IllegalStateException("Cannot borrow: overdue items exist.");

        // If no copies available -> add user to waitlist
        if (!item.isAvailable()) {
            WaitlistEntry entry = new WaitlistEntry(item.getId(), user.getEmail(), LocalDate.now());
            waitlist.add(entry);
            waitlistRepo.saveAll(waitlist);
            System.out.println("ℹ️ Item unavailable. User added to waitlist: " + user.getEmail());
            return false;
        }

        // Try to decrement available copy
        if (!item.borrow())
            throw new IllegalStateException("Failed to borrow item.");

        // Create a new BorrowRecord with appropriate fine strategy
        FineStrategy strategy = item.getMaterialType().createFineStrategy();
        BorrowRecord record = new BorrowRecord(user, item, strategy, today);
        borrowRecords.add(record);
        borrowRepo.saveAll(borrowRecords);

        // ✅ Persist new availableCopies / totalCopies to items.json
        itemManager.saveAll();

        System.out.println("✅ Item borrowed successfully: " + item.getTitle() + " by " + user.getUsername());
        return true;
    }

    /**
     * Marks an item as returned and notifies users waiting for it via email.
     *
     * @param user the user returning the item
     * @param item the item being returned
     * @throws IllegalArgumentException if user or item is null
     */
    public void returnItem(User user, LibraryItem item) {
        if (user == null || item == null)
            throw new IllegalArgumentException("User and item cannot be null.");

        LocalDate today = LocalDate.now();
        applyOverdueFines(today);

        BorrowRecord record = borrowRecords.stream()
                .filter(r -> 
                        r.getUser().getId().equals(user.getId()) &&   
                        r.getItem().getId().equals(item.getId()) &&  
                        !r.isReturned())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No active borrowing found."));

        record.markReturned(today);

        item.returnItem();

        borrowRepo.saveAll(borrowRecords);
        itemManager.saveAll();

        List<WaitlistEntry> waitingUsers = waitlist.stream()
                .filter(w -> w.getItemId().equals(item.getId()))
                .collect(Collectors.toList());

        Notifier notifier = new EmailNotifier();
        for (WaitlistEntry entry : waitingUsers) {
            Optional<User> target = UserManager.getInstance()
                    .findUserByEmail(entry.getUserEmail());
            target.ifPresent(value ->
                    notifier.notify(value,
                            "The item \"" + item.getTitle() + "\" is now available!",
                            "Good news! The item \"" + item.getTitle()
                                    + "\" you requested is now available for borrowing."));
        }

        waitlist.removeIf(w -> w.getItemId().equals(item.getId()));
        waitlistRepo.saveAll(waitlist);

        System.out.println("✅ Item returned successfully: " + item.getTitle());
    }


    // =====================================================================
    // Fines
    // =====================================================================

    /**
     * Applies overdue fines to all records that are past their due date.
     *
     * @param date the date to check against
     */
    public void applyOverdueFines(LocalDate date) {
        for (BorrowRecord r : borrowRecords) {
            if (r.isOverdue(date)) {
                r.applyFineToUser(date);
            }
        }
        borrowRepo.saveAll(borrowRecords);
    }

    /**
     * Handles paying fines for a given user.
     * <p>
     * This method:
     * <ul>
     *     <li>Ensures fines are up-to-date as of {@code date}</li>
     *     <li>Distributes the payment across that user's borrow records (finePaid)</li>
     *     <li>Subtracts the paid amount from the user's fine balance</li>
     * </ul>
     *
     * @param user   the user who is paying
     * @param amount the amount they are paying
     * @param date   the date of payment (used to recalculate fines)
     */
    public void payFineForUser(User user, BigDecimal amount, LocalDate date) {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null.");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive.");
        if (date == null)
            throw new IllegalArgumentException("Date cannot be null.");

        // Make sure all fines are calculated/applied first
        applyOverdueFines(date);

        BigDecimal remaining = amount;

        // Distribute payment over the user's borrow records
        for (BorrowRecord r : borrowRecords) {
            if (!r.getUser().equals(user)) continue;

            r.calculateFine(date); // ensure fine is up-to-date

            BigDecimal recordRemaining = r.getRemainingFine();
            if (recordRemaining.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal payPart = remaining.min(recordRemaining);
            r.setFinePaid(r.getFinePaid().add(payPart));
            remaining = remaining.subtract(payPart);
        }

        // Deduct the amount from the user's fine balance
        user.payFine(amount);

        borrowRepo.saveAll(borrowRecords);
    }

    /**
     * Retrieves all borrow records for a specific user.
     *
     * @param user the user whose records to retrieve
     * @return list of borrow records for that user
     */
    public List<BorrowRecord> getBorrowRecordsForUser(User user) {
        return borrowRecords.stream()
                .filter(r -> r.getUser().equals(user))
                .collect(Collectors.toList());
    }

    /**
     * Calculates the total fines owed by a user at a given date.
     *
     * @param user the user whose fines to calculate
     * @param date the date for evaluation
     * @return total fine amount
     */
    public BigDecimal calculateTotalFines(User user, LocalDate date) {
        return borrowRecords.stream()
                .filter(r -> r.getUser().equals(user) && !r.isReturned())
                .map(r -> r.getFine(date))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns all items that are overdue as of the given date.
     *
     * @param date the current date
     * @return list of overdue borrow records
     */
    public List<BorrowRecord> getOverdueItems(LocalDate date) {
        applyOverdueFines(date);
        return borrowRecords.stream()
                .filter(r -> r.isOverdue(date))
                .collect(Collectors.toList());
    }

    /**
     * Returns all borrow records in the system.
     *
     * @return list of all borrow records
     */
    public List<BorrowRecord> getAllBorrowRecords() {
        return List.copyOf(borrowRecords);
    }

    /**
     * Returns the current waitlist for inspection or debugging.
     *
     * @return unmodifiable list of waitlist entries
     */
    public List<WaitlistEntry> getWaitlist() {
        return List.copyOf(waitlist);
    }
}
