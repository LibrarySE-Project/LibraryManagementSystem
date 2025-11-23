package librarySE.managers.notifications;

import librarySE.managers.User;

/**
 * Defines a contract for sending notifications to users within the library system.
 * <p>
 * Implementations of this interface are responsible for delivering messages to users
 * through various channels (e.g., email, SMS, console, or in-app notifications).
 * </p>
 *
 * <p>This abstraction allows the system to easily switch between different notification
 * mechanisms without modifying business logic in managers such as {@code BorrowManager}.</p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * Notifier emailNotifier = new EmailNotifier();
 * emailNotifier.notify(user, "Your reserved item is now available.");
 * }</pre>
 *
 * @author Malak
 */
public interface Notifier {

    /**
     * Sends a notification message to the specified user.
     *
     * @param user    the user who should receive the notification; must not be {@code null}
     * @param message the message content to send; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code user} or {@code message} is {@code null} or empty
     */
    void notify(User user, String subject, String message);
}
