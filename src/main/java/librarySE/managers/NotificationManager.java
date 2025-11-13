package librarySE.managers;

import librarySE.managers.notifications.Notifier;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles automated user notifications related to borrowed library items.
 * <p>
 * The {@code NotificationManager} collaborates with the {@link BorrowManager}
 * to identify overdue borrow records and send reminder messages to users
 * using a specified {@link Notifier} implementation.
 * </p>
 *
 * <p>This design allows flexible notification delivery methods
 * (e.g., file-based logging, email, SMS, etc.) without changing the core logic.</p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * BorrowManager borrowManager = BorrowManager.getInstance();
 * Notifier notifier = new FileNotifier();
 * NotificationManager manager = new NotificationManager(borrowManager);
 * manager.sendReminders(notifier, LocalDate.now());
 * }</pre>
 *
 * <p>This will send a notification to each user who has overdue items
 * as of the given date.</p>
 *
 * @author 
 */
public class NotificationManager {

    /** Reference to the central borrow manager for retrieving overdue records. */
    private final BorrowManager borrowManager;

    /**
     * Constructs a new {@code NotificationManager} with a reference to the
     * {@link BorrowManager} for accessing borrowing data.
     *
     * @param borrowManager the borrow manager instance to use; must not be {@code null}
     * @throws IllegalArgumentException if {@code borrowManager} is {@code null}
     */
    public NotificationManager(BorrowManager borrowManager) {
        if (borrowManager == null)
            throw new IllegalArgumentException("BorrowManager cannot be null.");
        this.borrowManager = borrowManager;
    }

    /**
     * Sends reminder notifications to all users with overdue items as of the specified date.
     * <p>
     * Groups overdue borrow records by user, counts how many overdue items each user has,
     * and uses the provided {@link Notifier} to deliver a personalized message.
     * </p>
     *
     * @param notifier the notifier implementation to use for sending messages
     * @param date     the date used to check for overdue items
     * @throws IllegalArgumentException if {@code notifier} or {@code date} is {@code null}
     */
    public void sendReminders(Notifier notifier, LocalDate date) {
        if (notifier == null)
            throw new IllegalArgumentException("Notifier cannot be null.");
        if (date == null)
            throw new IllegalArgumentException("Date cannot be null.");

        List<BorrowRecord> overdue = borrowManager.getOverdueItems(date);

        Map<User, List<BorrowRecord>> byUser = overdue.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getUser));

        for (Map.Entry<User, List<BorrowRecord>> e : byUser.entrySet()) {
            User u = e.getKey();
            int count = e.getValue().size();
            String msg = "You have " + count + " overdue item(s) as of " + date + ".";
            String subject = "📚 Reminder: You have overdue library items!";
            notifier.notify(u, subject, msg);
        }
    }
}
