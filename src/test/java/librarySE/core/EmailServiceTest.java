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

/**
 * Unit tests for {@link EmailService} using Mockito static mocking.
 *
 * This test suite validates:
 *  1) Successful email sending (Transport.send is called and no exception is thrown).
 *  2) Failure scenario where Transport.send throws MessagingException and EmailService
 *     wraps it in a RuntimeException.
 *  3) The Authenticator returned by createAuthenticator() correctly produces
 *     a PasswordAuthentication object.
 */
class EmailServiceTest {

    @Test
    @DisplayName("sendEmail: should call Transport.send and not throw when SMTP succeeds")
    void sendEmail_shouldCallTransportSend_withoutThrowingException() {
        EmailService emailService = new EmailService();

        String to = "student@stu.najah.edu";
        String subject = "Test Subject";
        String body = "Test Body";

        // Mock static Transport.send()
        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {

            // Default behavior: do nothing (success path)

            // Execute and ensure no exception is thrown
            assertDoesNotThrow(() -> emailService.sendEmail(to, subject, body));

            // Verify Transport.send() invocation
            transportMock.verify(() -> Transport.send(any(Message.class)));
        }
    }

    @Test
    @DisplayName("sendEmail: should wrap MessagingException into RuntimeException when sending fails")
    void sendEmail_shouldThrowRuntimeException_whenTransportSendFails() {
        EmailService emailService = new EmailService();

        String to = "student@stu.najah.edu";
        String subject = "Test Failure";
        String body = "Test Body";

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {

            // Force Transport.send() to throw MessagingException
            transportMock
                    .when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("Simulated SMTP failure"));

            // Verify that EmailService throws RuntimeException
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    emailService.sendEmail(to, subject, body)
            );

            // Validate exception message and cause
            assertEquals("Failed to send email", ex.getMessage());
            assertNotNull(ex.getCause());
            assertTrue(ex.getCause() instanceof MessagingException);

            // Ensure send() was called
            transportMock.verify(() -> Transport.send(any(Message.class)));
        }
    }

    @Test
    @DisplayName("createAuthenticator: should return a valid PasswordAuthentication object")
    void createAuthenticator_shouldReturnPasswordAuthentication() {
        EmailService emailService = new EmailService();

        Authenticator authenticator = emailService.createAuthenticator();

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

        // Validate returned authentication object
        assertNotNull(pass, "PasswordAuthentication should not be null");
        assertNotNull(pass.getUserName(), "Username should not be null");
        assertNotNull(pass.getPassword(), "Password should not be null");
    }
}


