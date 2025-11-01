package librarySE;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages notifications and reminders for users in the library system.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Sending reminders to users who have overdue items.</li>
 *     <li>Grouping overdue items by user for notification purposes.</li>
 *     <li>Supports sending notifications for a specific date, enabling testing or scheduled notifications.</li>
 * </ul>
 * <p>
 * Uses the {@link BorrowManager} to query overdue items and an {@link Observer} to send notifications.
 * </p>
 * 
 * @author Malak
 * @see BorrowManager
 * @see Observer
 * @see BorrowRecord
 * @see User
 */
public class NotificationManager {

    /** Reference to the borrow manager for querying overdue items. */
    private final BorrowManager borrowManager;

    /**
     * Constructs a {@code NotificationManager} with the given {@link BorrowManager}.
     * 
     * @param borrowManager the borrow manager to use; must not be null
     * @throws IllegalArgumentException if {@code borrowManager} is null
     */
    public NotificationManager(BorrowManager borrowManager) {
        if (borrowManager == null)
            throw new IllegalArgumentException("BorrowManager cannot be null.");
        this.borrowManager = borrowManager;
    }

    /**
     * Sends reminders to all users with overdue items as of the specified date.
     * <p>
     * Overdue items are grouped by user. Each user receives a notification indicating
     * the number of overdue items.
     * 
     * @param notifier the observer responsible for sending notifications; must not be null
     * @param date the date to check for overdue items; must not be null
     * @throws IllegalArgumentException if {@code notifier} or {@code date} is null
     */
    public void sendReminders(Observer notifier, LocalDate date) {
        if (notifier == null)
            throw new IllegalArgumentException("Notifier cannot be null.");
        if (date == null)
            throw new IllegalArgumentException("Date cannot be null.");

        // Get all overdue borrow records as of the given date
        List<BorrowRecord> overdueRecords = borrowManager.getOverdueItems(date);

        // Group overdue records by user
        Map<User, List<BorrowRecord>> byUser = overdueRecords.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getUser));

        // Send notification to each user about their overdue items
        for (Map.Entry<User, List<BorrowRecord>> entry : byUser.entrySet()) {
            User user = entry.getKey();
            int count = entry.getValue().size();
            String message = "You have " + count + " overdue item(s) as of " + date + ".";
            notifier.notify(user, message);
        }
    }
}
