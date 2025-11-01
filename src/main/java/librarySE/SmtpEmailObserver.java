package librarySE;

/**
 * An adapter that allows a real {@link EmailService} to function as an {@link Observer}.
 * <p>
 * This class follows the Adapter design pattern, bridging the {@link Observer} interface
 * used for notifications in the library system with an actual SMTP-based email service.
 * </p>
 * 
 * <p>
 * When a user-related event occurs (e.g., overdue books), the {@link #notify(User, String)}
 * method sends an email to the user using the underlying {@link EmailService}.
 * </p>
 * 
 * 
 * <p>
 * This class ensures that email notifications are sent consistently whenever an observer
 * is notified, without the library system needing to know the implementation details
 * of the email service.
 * </p>
 * 
 * @author Eman
 */
public class SmtpEmailObserver implements Observer {

    /** The underlying email service used to send messages. */
    private final EmailService emailService;

    /**
     * Constructs a new SMTP email observer using the provided {@link EmailService}.
     * 
     * @param emailService the email service to be used for sending notifications; must not be {@code null}
     * @throws IllegalArgumentException if {@code emailService} is null
     */
    public SmtpEmailObserver(EmailService emailService) {
        if (emailService == null) {
            throw new IllegalArgumentException("EmailService cannot be null");
        }
        this.emailService = emailService;
    }

    /**
     * Sends a notification email to the specified user.
     * <p>
     * The email subject is set to "Library Reminder" and the body contains the provided message.
     * </p>
     *
     * @param user the user to notify; must not be {@code null}
     * @param message the message content to send; must not be {@code null}
     * @throws IllegalArgumentException if {@code user} or {@code message} is null
     */
    @Override
    public void notify(User user, String message) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        String subject = "Library Reminder";
        emailService.sendEmail(user.getEmail(), subject, message);
    }
}

