package librarySE.managers.notifications;

import librarySE.managers.User;
import librarySE.core.EmailService;

/**
 * {@code EmailNotifier} is a concrete implementation of {@link Notifier}
 * that sends notifications to users via email using {@link EmailService}.
 *
 * <p>This class uses dependency injection to allow substituting
 * a fake or mock email service during unit testing.</p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * Notifier notifier = new EmailNotifier();
 * notifier.notify(user, "📚 New Book!", "A new item was added to the library.");
 * }</pre>
 *
 * <p>Make sure your <b>.env</b> file contains valid SMTP credentials:</p>
 * <pre>
 * EMAIL_USERNAME=your_email@gmail.com
 * EMAIL_PASSWORD=your_app_password
 * </pre>
 *
 * @author Eman
 * 
 */
public class EmailNotifier implements Notifier {

    /** The email service used to send messages. */
    private final EmailService emailService;

    /**
     * Default constructor that uses the real {@link EmailService}.
     */
    public EmailNotifier() {
        this.emailService = new EmailService();
    }

    /**
     * Constructor used for dependency injection during testing.
     *
     * @param emailService a custom email service (fake or mock)
     */
    public EmailNotifier(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Sends an email notification to the specified user.
     *
     * @param user    recipient user (must not be {@code null})
     * @param subject subject line (must not be blank)
     * @param message message body (must not be blank)
     * @throws IllegalArgumentException if any argument is invalid
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
        String body = "Dear " + user.getUsername() + ",\n\n" +
                message + "\n\n— Library System";

        emailService.sendEmail(to, subject, body);
    }
}

