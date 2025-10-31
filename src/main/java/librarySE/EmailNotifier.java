package librarySE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A mock implementation of the {@link Observer} interface that "sends" email notifications
 * to users by recording messages instead of actually sending emails.
 * <p>
 * This class follows the Observer design pattern and is useful for testing
 * or simulating email notifications in the library system.
 * </p>
 * 
 * <p>
 * Thread-safe: multiple threads can safely notify users simultaneously.
 * </p>
 * 
 * @see Observer 
 * @see User
 * @author Eman
 */
public class EmailNotifier implements Observer {

    /** Thread-safe list of sent messages */
    private final List<String> sentMessages = Collections.synchronizedList(new ArrayList<>());

    /**
     * Notifies the observer of an event related to a user.
     * <p>
     * Instead of sending a real email, the message is recorded in {@link #sentMessages}.
     * </p>
     * 
     * @param user the {@link User} associated with the event
     * @param message the message or information to notify the user about
     */
    @Override
    public void notify(User user, String message) {
        String record = "To: " + user.getEmail() + " | Message: " + message;
        sentMessages.add(record);
        System.out.println(record); // for debugging / testing
    }

    /**
     * Returns an unmodifiable list of all messages that have been "sent".
     * <p>
     * Useful for testing or auditing notifications.
     * </p>
     * 
     * @return a list of recorded messages
     */
    public List<String> getSentMessages() {
        return Collections.unmodifiableList(sentMessages);
    }

    /**
     * Clears all recorded messages.
     * <p>
     * Useful for resetting state between tests or events.
     * </p>
     */
    public void clearMessages() {
        sentMessages.clear();
    }
}
