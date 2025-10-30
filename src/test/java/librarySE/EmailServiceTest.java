package librarySE;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the {@link EmailService} interface using a mock implementation.
 * <p>
 * Covers normal, edge, and negative cases for sending emails.
 * Tests ensure that valid messages are recorded and invalid parameters
 * (null or empty) are handled properly.
 * </p>
 */
class EmailServiceTest {

    private MockEmailService mockService;

    /**
     * A mock implementation of {@link EmailService} for testing.
     * Instead of sending real emails, it records them in a list.
     */
    private static class MockEmailService implements EmailService {
        private final List<String> sentEmails = new ArrayList<>();

        @Override
        public void sendEmail(String to, String subject, String body) {
            if (to == null || to.trim().isEmpty())
                throw new IllegalArgumentException("Recipient address cannot be null or empty.");
            if (subject == null)
                throw new IllegalArgumentException("Subject cannot be null.");
            if (body == null)
                throw new IllegalArgumentException("Body cannot be null.");

            sentEmails.add("To: " + to.trim() + " | Subject: " + subject + " | Body: " + body);
        }

        public List<String> getSentEmails() {
            return sentEmails;
        }

        public void clear() {
            sentEmails.clear();
        }
    }

    @BeforeEach
    void setUp() {
        mockService = new MockEmailService();
    }

    @AfterEach
    void tearDown() {
        mockService.clear();
        mockService = null;
    }

    // sendEmail() - normal cases

    /** sendEmail() records valid email details */
    @Test
    void testSendEmailValid() {
        mockService.sendEmail("user@example.com", "Reminder", "Your book is overdue.");
        assertEquals(1, mockService.getSentEmails().size());
        String record = mockService.getSentEmails().get(0);
        assertTrue(record.contains("user@example.com"));
        assertTrue(record.contains("Reminder"));
        assertTrue(record.contains("Your book is overdue."));
    }

    /** sendEmail() trims whitespace from recipient address */
    @Test
    void testSendEmailTrimsWhitespace() {
        mockService.sendEmail("  user@example.com  ", "Hello", "Welcome!");
        String record = mockService.getSentEmails().get(0);
        assertTrue(record.contains("user@example.com"));
    }

    /** sendEmail() allows empty subject or body */
    @Test
    void testSendEmailAllowsEmptySubjectOrBody() {
        assertDoesNotThrow(() -> mockService.sendEmail("test@najah.edu", "", ""));
        assertEquals(1, mockService.getSentEmails().size());
    }

    // sendEmail() - negative cases

    /** sendEmail() throws for null recipient */
    @Test
    void testSendEmailNullRecipient() {
        assertThrows(IllegalArgumentException.class,
                () -> mockService.sendEmail(null, "Subject", "Body"));
    }

    /** sendEmail() throws for empty recipient */
    @Test
    void testSendEmailEmptyRecipient() {
        assertThrows(IllegalArgumentException.class,
                () -> mockService.sendEmail("   ", "Subject", "Body"));
    }

    /** sendEmail() throws for null subject */
    @Test
    void testSendEmailNullSubject() {
        assertThrows(IllegalArgumentException.class,
                () -> mockService.sendEmail("user@najah.edu", null, "Body"));
    }

    /** sendEmail() throws for null body */
    @Test
    void testSendEmailNullBody() {
        assertThrows(IllegalArgumentException.class,
                () -> mockService.sendEmail("user@najah.edu", "Subject", null));
    }

    // Behavior tests

    /** Multiple sendEmail() calls record messages in order */
    @Test
    void testSendEmailMultipleCalls() {
        mockService.sendEmail("a@x.com", "1", "A");
        mockService.sendEmail("b@x.com", "2", "B");
        mockService.sendEmail("c@x.com", "3", "C");

        List<String> emails = mockService.getSentEmails();
        assertEquals(3, emails.size());
        assertTrue(emails.get(0).contains("a@x.com"));
        assertTrue(emails.get(1).contains("b@x.com"));
        assertTrue(emails.get(2).contains("c@x.com"));
    }

    /** clear() removes all recorded emails */
    @Test
    void testClearEmails() {
        mockService.sendEmail("user1@x.com", "S1", "B1");
        mockService.sendEmail("user2@x.com", "S2", "B2");
        assertEquals(2, mockService.getSentEmails().size());

        mockService.clear();
        assertTrue(mockService.getSentEmails().isEmpty());
    }
}

