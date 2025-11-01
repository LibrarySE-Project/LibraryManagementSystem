package librarySE;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Implementation of {@link EmailService} using SMTP (Gmail by default).
 * <p>
 * Responsible for sending HTML emails from a specified sender account.
 * Uses JavaMail API for SMTP communication.
 * </p>
 * <p>
 * Features:
 * <ul>
 *     <li>Validates sender email and password on construction.</li>
 *     <li>Supports TLS encryption and authentication.</li>
 *     <li>Logs success and failure messages using {@link Logger}.</li>
 * </ul>
 * </p>
 * 
 * 
 * @author Malak
 * @see EmailService
 */
public class SmtpEmailService implements EmailService {

    /** Logger for email sending events */
    private static final Logger LOGGER = Logger.getLogger(SmtpEmailService.class.getName());

    /** Sender's email address */
    private final String senderEmail;

    /** JavaMail session */
    private final Session session;

    /**
     * Constructs an SMTP email service using Gmail's SMTP server.
     *
     * @param senderEmail    the email used to send messages; must not be null or empty
     * @param senderPassword the app password for authentication; must not be null or empty
     * @throws IllegalArgumentException if {@code senderEmail} or {@code senderPassword} is null/empty
     */
    public SmtpEmailService(String senderEmail, String senderPassword) {
        if (senderEmail == null || senderEmail.isBlank()) {
            throw new IllegalArgumentException("Sender email cannot be null or empty.");
        }
        if (senderPassword == null || senderPassword.isBlank()) {
            throw new IllegalArgumentException("Sender password cannot be null or empty.");
        }

        this.senderEmail = senderEmail;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
    }

    /**
     * Sends an email to the specified recipient.
     * <p>
     * The email supports HTML content.
     * </p>
     *
     * @param to      recipient's email address; must be a valid format
     * @param subject subject line; can be empty
     * @param body    message body (supports HTML); can be empty
     * @throws RuntimeException if sending the email fails
     */
    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=UTF-8");

            Transport.send(message);
            LOGGER.info("Email sent successfully to " + to);
        } catch (MessagingException e) {
            LOGGER.severe("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
