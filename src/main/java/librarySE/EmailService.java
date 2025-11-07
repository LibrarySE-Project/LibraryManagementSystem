package librarySE;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/**
 * {@code EmailService} is responsible for sending email notifications through Gmail's SMTP server.
 * <p>
 * It automatically loads credentials (username and password) from the environment variables
 * using the {@code .env} file via {@code java-dotenv} library.
 * </p>
 *
 * <h3>Features:</h3>
 * <ul>
 *     <li>Automatically loads Gmail credentials securely from a .env file.</li>
 *     <li>Supports TLS encryption and SMTP authentication.</li>
 *     <li>Provides simple API to send plain-text messages.</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * EmailService emailService = new EmailService();
 * emailService.sendEmail(
 *     "student@stu.najah.edu",
 *     "Book Due Reminder",
 *     "Dear user, your borrowed book is due soon."
 * );
 * }</pre>
 *
 * <p>
 * Make sure your <b>.env</b> file includes:
 * <pre>
 * EMAIL_USERNAME=your_email@gmail.com
 * EMAIL_PASSWORD=your_app_password
 * </pre>
 * </p>
 *
 * @author Malak
 * @version 1.0
 */
public class EmailService {

    /** Sender's Gmail address loaded from environment variables */
    private final String username;

    /** Sender's Gmail password or App Password loaded from environment variables */
    private final String password;

    /**
     * Constructs an {@code EmailService} instance.
     * <p>Automatically loads {@code EMAIL_USERNAME} and {@code EMAIL_PASSWORD} from a .env file.</p>
     */
    public EmailService() {
        io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.load();
        this.username = dotenv.get("EMAIL_USERNAME");
        this.password = dotenv.get("EMAIL_PASSWORD");
    }

    /**
     * Sends an email to the specified recipient with the given subject and message body.
     *
     * @param to      recipient's email address
     * @param subject email subject line
     * @param body    content of the email
     * @throws RuntimeException if the email fails to send
     */
    public void sendEmail(String to, String subject, String body) {
        // SMTP configuration
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Create session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
           
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            
            Transport.send(message);

            java.util.logging.Logger.getLogger(EmailService.class.getName())
                    .info("Email sent successfully to: " + to);

        } catch (MessagingException e) {
            java.util.logging.Logger.getLogger(EmailService.class.getName())
                    .severe("Failed to send email to: " + to + " → " + e.getMessage());
            throw new RuntimeException("❌ Failed to send email", e);
        }
    }
    
}
