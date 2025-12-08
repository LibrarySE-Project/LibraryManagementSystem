package librarySE.core;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/**
 * {@code EmailService} is responsible for sending email notifications through Gmail's SMTP server.
 * It loads Gmail credentials from a local .env file using the java-dotenv library.
 *
 * <h3>Features:</h3>
 * <ul>
 *     <li>Loads email credentials securely from a .env file</li>
 *     <li>Uses TLS and authenticated SMTP connection</li>
 *     <li>Sends plain-text messages</li>
 * </ul>
 *
 * <p><b>Required .env file:</b></p>
 * <pre>
 * EMAIL_USERNAME=your_email@gmail.com
 * EMAIL_PASSWORD=your_app_password
 * </pre>
 *
 * @author Eman
 * @version 1.1
 */
public class EmailService {

    /** Sender Gmail username loaded from .env file */
    private final String username;

    /** Sender Gmail password (App Password) loaded from .env file */
    private final String password;

    /**
     * Constructs an {@code EmailService} and loads Gmail credentials from .env.
     */
    public EmailService() {
        io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.load();
        this.username = dotenv.get("EMAIL_USERNAME");
        this.password = dotenv.get("EMAIL_PASSWORD");
    }

    /**
     * Creates the SMTP authenticator used during email sending.
     *
     * <p>
     * This method is extracted for testability: unit tests can directly
     * call and verify the returned password authentication object.
     * </p>
     *
     * @return an {@link Authenticator} instance that supplies the Gmail credentials
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
     * Sends an email to the specified recipient using Gmail SMTP.
     *
     * @param to      recipient address
     * @param subject email subject
     * @param body    email body text
     * @throws RuntimeException if email fails to send
     */
    public void sendEmail(String to, String subject, String body) {

        // SMTP configuration
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Create session
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
                    .info("Email sent successfully to: " + to);

        } catch (MessagingException e) {
            java.util.logging.Logger.getLogger(EmailService.class.getName())
                    .severe("Failed to send email to: " + to + " → " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
