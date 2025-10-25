package librarySE;

/**
 * Defines a contract for sending email messages within the library system.
 * <p>
 * This interface abstracts the process of sending emails so that different implementations
 * (e.g., real SMTP-based services or mock/testing ones) can be used interchangeably.
 * </p>
 * 
 * <p>
 * Typical use cases include sending overdue notifications, registration confirmations,
 * or administrative messages to users.
 * </p>
 * 
 * @see EmailNotifier
 * @see User
 * @author Malak
 */
public interface EmailService {

    /**
     * Sends an email message to the specified recipient.
     * <p>
     * Implementations of this method should handle the actual delivery of the message,
     * including connection setup, error handling, and encoding if applicable.
     * </p>
     *
     * @param to the recipient's email address (must not be {@code null} or empty)
     * @param subject the subject line of the email
     * @param body the main content or body of the email (plain text or HTML)
     */
    void sendEmail(String to, String subject, String body);
}
