package librarySE.core;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Transport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;


class EmailServiceTest {

    @Test
    @DisplayName("sendEmail: should call Transport.send and not throw when SMTP succeeds")
    void sendEmail_shouldCallTransportSend_withoutThrowingException() {
        EmailService emailService = new EmailService();

        String to = "student@stu.najah.edu";
        String subject = "Test Subject";
        String body = "Test Body";

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {
            // success path: do nothing

            assertDoesNotThrow(() -> emailService.sendEmail(to, subject, body));

            transportMock.verify(() -> Transport.send(any(Message.class)));
        }
    }

    @Test
    @DisplayName("sendEmail: should NOT throw even when Transport.send fails")
    void sendEmail_shouldNotThrow_whenTransportSendFails() {
        EmailService emailService = new EmailService();

        String to = "student@stu.najah.edu";
        String subject = "Test Failure";
        String body = "Test Body";

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {

            // Force Transport.send() to throw MessagingException
            transportMock
                    .when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("Simulated SMTP failure"));

            // New behaviour: method should handle the exception internally and NOT rethrow
            assertDoesNotThrow(() -> emailService.sendEmail(to, subject, body));

            // Ensure send() was still called
            transportMock.verify(() -> Transport.send(any(Message.class)));
        }
    }

    @Test
    @DisplayName("createAuthenticator: should return a PasswordAuthentication object")
    void createAuthenticator_shouldReturnPasswordAuthentication() {
        EmailService emailService = new EmailService();

        Authenticator authenticator = emailService.createAuthenticator();
        assertNotNull(authenticator, "Authenticator should not be null");

        PasswordAuthentication pass = null;
        try {
            // Invoke the protected getPasswordAuthentication() via reflection
            Method checkAuth = authenticator.getClass()
                    .getDeclaredMethod("getPasswordAuthentication");
            checkAuth.setAccessible(true);
            pass = (PasswordAuthentication) checkAuth.invoke(authenticator);
        } catch (Exception exception) {
            fail("Reflection call to getPasswordAuthentication() failed: " + exception.getMessage());
        }

        // We only guarantee that a PasswordAuthentication object is returned;
        // username/password values may be null if .env is not configured.
        assertNotNull(pass, "PasswordAuthentication should not be null");
    }
}
