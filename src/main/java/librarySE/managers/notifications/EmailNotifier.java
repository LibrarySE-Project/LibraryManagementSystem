package librarySE.managers.notifications;

import librarySE.managers.User;
import librarySE.core.EmailService;

/**
 * {@code EmailNotifier} is an implementation of {@link Notifier}
 * that delivers notifications to users via email using {@link EmailService}.
 * <p>
 * It retrieves user email addresses from the {@link User} object and sends
 * messages through Gmail SMTP configured in the .env file.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * Notifier notifier = new EmailNotifier();
 * notifier.notify(user, "📚 A new book has been added to the library!");
 * }</pre>
 *
 * <p>Make sure your <b>.env</b> file contains:</p>
 * <pre>
 * EMAIL_USERNAME=your_email@gmail.com
 * EMAIL_PASSWORD=your_app_password
 * </pre>
 *
 * @author 
 */
public class EmailNotifier implements Notifier {

    /** Email service used for sending notifications. */
    private final EmailService emailService = new EmailService();
    /**
     * Sends an email notification to the specified user with a given subject.
     *
     * <p>This method validates the input parameters and sends the email through
     * the configured {@code emailService}. The message body is automatically
     * prefixed with a greeting and signed by the library system.</p>
     *
     * @param user     the recipient {@link User}; must not be {@code null}
     * @param subject  the subject line of the email; must not be {@code null} or blank
     * @param message  the message body to send; must not be {@code null} or blank
     * @throws IllegalArgumentException if any parameter is {@code null} or blank
     * @throws RuntimeException if sending the email fails
     */
    @Override
    public void notify(User user, String subject, String message) {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null.");
        if (subject == null || subject.isBlank())
            throw new IllegalArgumentException("Subject cannot be null or empty.");
        if (message == null || message.isBlank())
            throw new IllegalArgumentException("Message cannot be null or empty.");

        String to = user.getEmail();
        String body = "Dear " + user.getUsername() + ",\n\n" + message + "\n\n— Library System";

        emailService.sendEmail(to, subject, body);
    }

}
