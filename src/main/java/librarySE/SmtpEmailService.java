package librarySE;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * A production-ready implementation of {@link EmailService} that sends emails using SMTP.
 * <p>
 * Features:
 * <ul>
 *   <li>Supports authentication and TLS/SSL</li>
 *   <li>Sends HTML emails with UTF-8 encoding</li>
 *   <li>Configurable connection and read timeouts</li>
 * </ul>
 * </p>
 *  
 * @author Eman
 */
public class SmtpEmailService implements EmailService {

    private static final Logger LOGGER = Logger.getLogger(SmtpEmailService.class.getName());
    private final String senderEmail;
    private final Session session;

    /**
     * Constructs an SMTP email service using Gmail's SMTP server by default.
     *
     * @param senderEmail    the email used to send messages
     * @param senderPassword the app password for authentication
     */
    public SmtpEmailService(String senderEmail, String senderPassword) {
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
     *
     * @param to      recipient's email address
     * @param subject subject line
     * @param body    message body (supports HTML)
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
