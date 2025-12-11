package librarySE.core;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Provides a lightweight email-sending utility backed by Gmail’s SMTP service.
 *
 * <p>
 * {@code EmailService} loads Gmail credentials from a local <b>.env</b> file using
 * the <i>java-dotenv</i> library and sends plain-text email notifications using a
 * secure TLS-enabled SMTP connection.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Load Gmail username and app-password from environment file</li>
 *     <li>Configure and authenticate an SMTP session</li>
 *     <li>Send text-based email messages</li>
 * </ul>
 *
 * <h2>Required .env File</h2>
 * <p>
 * The service expects a valid Gmail app-password (not a regular password).
 * </p>
 *
 * <pre>{@code
 * EMAIL_USERNAME=your_email@gmail.com
 * EMAIL_PASSWORD=your_app_password
 * }</pre>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * EmailService mail = new EmailService();
 * mail.sendEmail("student@najah.edu", "Reminder", "Your book is overdue.");
 * }</pre>
 *
 * <p>
 * Errors during SMTP communication are surfaced as {@link RuntimeException}s
 * and also written to the system logger for debugging.
 * </p>
 *
 *
 * @author Eman
 */
public class EmailService {

    /** Gmail username loaded from .env. */
    private final String username;

    /** Gmail app password loaded from .env. */
    private final String password;

    /**
     * Constructs an {@code EmailService} and loads Gmail credentials
     * from the project's .env file.
     *
     * <p>
     * Uses {@code java-dotenv} to read environment variables without exposing
     * sensitive credentials directly in the source code.
     * </p>
     */
    public EmailService() {
        io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.load();
        this.username = dotenv.get("EMAIL_USERNAME");
        this.password = dotenv.get("EMAIL_PASSWORD");
    }

    /**
     * Creates the SMTP authenticator used when establishing
     * the Gmail SMTP session.
     *
     * <p>
     * Extracted as a protected method to allow mocking in unit tests.
     * </p>
     *
     * @return an {@link Authenticator} that supplies Gmail credentials
     */
    protected Authenticator createAuthenticator() {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

    /**
     * Sends a plain-text email message through Gmail’s SMTP server.
     *
     * <p><b>SMTP Details:</b></p>
     * <ul>
     *     <li>Host: {@code smtp.gmail.com}</li>
     *     <li>Port: {@code 587} (TLS)</li>
     *     <li>Authentication: Required</li>
     *     <li>STARTTLS: Enabled</li>
     * </ul>
     *
     * @param to      recipient email address
     * @param subject message subject line
     * @param body    plain-text body content
     *
     * @throws RuntimeException if the email cannot be sent
     */
    public void sendEmail(String to, String subject, String body) {

        // SMTP configuration
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Create authenticated session
        Session session = Session.getInstance(props, createAuthenticator());

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            // Send email via SMTP
            Transport.send(message);

            java.util.logging.Logger.getLogger(EmailService.class.getName())
            .log(Level.INFO, "Email sent successfully to: {0}", to);

        } catch (MessagingException e) {
        	java.util.logging.Logger.getLogger(EmailService.class.getName())
            .log(Level.SEVERE, "Failed to send email to: {0} → {1}",
                    new Object[]{to, e.getMessage()});
        }
    }
}