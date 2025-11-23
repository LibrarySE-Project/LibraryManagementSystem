package librarySE.managers.notifications;

import librarySE.core.EmailService;
import librarySE.managers.Role;
import librarySE.managers.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link EmailNotifier}.
 */
class EmailNotifierTest {

    static class FakeEmailService extends EmailService {
        String to, subject, body;
        boolean throwError = false;

        @Override
        public void sendEmail(String to, String subject, String body) {
            if (throwError) {
                throw new RuntimeException("SMTP failure");
            }
            this.to = to;
            this.subject = subject;
            this.body = body;
        }
    }

    FakeEmailService fake;
    EmailNotifier notifier;
    User user;

    @BeforeEach
    void setup() {
        fake = new FakeEmailService();
        notifier = new EmailNotifier(fake);  
        user = new User("Malak", Role.USER, "pass123", "malak@test.com");
    }

    @Test
    void testNotifySuccess() {
        notifier.notify(user, "Hello", "Message");

        assertEquals("malak@test.com", fake.to);
        assertEquals("Hello", fake.subject);
        assertTrue(fake.body.contains("Message"));
        assertTrue(fake.body.contains("Dear Malak"));
    }

    @Test
    void testNullUserThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> notifier.notify(null, "Sub", "Msg"));
    }

    @Test
    void testEmptySubjectThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> notifier.notify(user, "  ", "Msg"));
    }

    @Test
    void testEmptyMessageThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> notifier.notify(user, "Sub", "  "));
    }

    @Test
    void testEmailServiceThrows() {
        fake.throwError = true;
        assertThrows(RuntimeException.class,
                () -> notifier.notify(user, "Sub", "Msg"));
    }
}

