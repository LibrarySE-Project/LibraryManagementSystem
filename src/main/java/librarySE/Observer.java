package librarySE;

/**
 * Represents an observer in the library system.
 * <p>
 * Classes that implement this interface can receive notifications
 * about events related to users, such as borrowing or returning books,
 * or other system messages.
 * </p>
 * 
 * <p>
 * This follows the Observer design pattern, where observers can be
 * notified of changes in the system without tightly coupling them
 * to the source of events.
 * </p>
 * 
 * @author Malak
 */
public interface Observer {

    /**
     * Sends a notification to the observer regarding a user event or system message.
     * 
     * @param user the {@link User} associated with the event or action
     * @param message the message or information to be communicated
     */
    void notify(User user, String message);
}
