package librarySE.managers.notifications;

import librarySE.core.EmailService;
import librarySE.managers.Role;
import librarySE.managers.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailNotifierTest {

    EmailService mockEmail;
    EmailNotifier notifier;
    User user;

    @BeforeEach
    void setup() {
        mockEmail = mock(EmailService.class);
        notifier = new EmailNotifier(mockEmail);
        user = new User("Malak", Role.USER, "pass123", "malak@test.com");
    }

    // -------------------------------------------------------
    // SUCCESS CASE WITH VERIFY
    // -------------------------------------------------------
    @Test
    void testNotifySuccess() {
        notifier.notify(user, "Hello", "Message");

        // Capture arguments sent to sendEmail
        ArgumentCaptor<String> toCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCap = ArgumentCaptor.forClass(String.class);

        verify(mockEmail, times(1))
                .sendEmail(toCap.capture(), subjectCap.capture(), bodyCap.capture());

        assertEquals("malak@test.com", toCap.getValue());
        assertEquals("Hello", subjectCap.getValue());
        assertTrue(bodyCap.getValue().contains("Message"));
        assertTrue(bodyCap.getValue().contains("Dear Malak"));
    }

    // -------------------------------------------------------
    // VALIDATION BRANCHES
    // -------------------------------------------------------
    @Test
    void testNullUserThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> notifier.notify(null, "Sub", "Msg"));
    }

    @Test
    void testNullSubjectThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> notifier.notify(user, null, "Msg"));
    }

    @Test
    void testBlankSubjectThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> notifier.notify(user, "   ", "Msg"));
    }

    @Test
    void testNullMessageThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> notifier.notify(user, "Sub", null));
    }

    @Test
    void testBlankMessageThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> notifier.notify(user, "Sub", "   "));
    }

    // -------------------------------------------------------
    // EMAIL SERVICE FAILURE BRANCH
    // -------------------------------------------------------
    @Test
    void testEmailServiceThrows() {
        // make mock throw when sendEmail() is called
        doThrow(new RuntimeException("SMTP failure"))
                .when(mockEmail)
                .sendEmail(anyString(), anyString(), anyString());

        assertThrows(RuntimeException.class,
                () -> notifier.notify(user, "Sub", "Msg"));
    }

    // -------------------------------------------------------
    // COVER DEFAULT CONSTRUCTOR
    // -------------------------------------------------------
    @Test
    void testDefaultConstructor() {
        try (MockedConstruction<EmailService> mocked = mockConstruction(EmailService.class)) {
            EmailNotifier notifier2 = new EmailNotifier();
            assertNotNull(notifier2);
        }
    }

}
