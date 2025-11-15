package librarySE;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import librarySE.managers.Role;
import librarySE.managers.User;

import java.util.List;

/**
 * Unit tests for the {@link EmailNotifier} class.
 * <p>
 * Covers normal and edge cases for notification behavior, message recording,
 * retrieval, and clearing functionality. Ensures thread safety and immutability
 * of returned message lists.
 * </p>
 */
class EmailNotifierTest {

    private EmailNotifier notifier;
    private User user;

    @BeforeEach
    void setUp() {
        notifier = new EmailNotifier();
        user = new User("Alice", Role.USER, "password123", "alice@example.com");
    }

    @AfterEach
    void tearDown() {
        notifier.clearMessages();
        notifier = null;
        user = null;
    }

    // notify() method

    /** notify() records message correctly for valid user and message */
    @Test
    void testNotifyRecordsMessage() {
        notifier.notify(user, "Your book is overdue!");
        List<String> messages = notifier.getSentMessages();

        assertEquals(1, messages.size());
        assertTrue(messages.get(0).contains("alice@example.com"));
        assertTrue(messages.get(0).contains("Your book is overdue!"));
    }

    /** notify() can handle multiple notifications for same or different users */
    @Test
    void testNotifyMultipleMessages() {
        User user2 = new User("Bob", Role.USER, "pass456", "bob@example.com");

        notifier.notify(user, "Reminder 1");
        notifier.notify(user2, "Reminder 2");

        List<String> messages = notifier.getSentMessages();
        assertEquals(2, messages.size());
        assertTrue(messages.get(0).contains("alice@example.com"));
        assertTrue(messages.get(1).contains("bob@example.com"));
    }

    /** notify() should not crash even if message is empty string */
    @Test
    void testNotifyWithEmptyMessage() {
        assertDoesNotThrow(() -> notifier.notify(user, ""));
        List<String> messages = notifier.getSentMessages();
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).contains("To: alice@example.com"));
    }

    /** notify() throws NullPointerException if user is null */
    @Test
    void testNotifyWithNullUser() {
        assertThrows(NullPointerException.class, () -> notifier.notify(null, "Message"));
    }

    /** notify() throws NullPointerException if message is null */
    @Test
    void testNotifyWithNullMessage() {
        assertThrows(NullPointerException.class, () -> notifier.notify(user, null));
    }

    // getSentMessages()

    /** getSentMessages() returns unmodifiable list */
    @Test
    void testGetSentMessagesIsUnmodifiable() {
        notifier.notify(user, "Hello!");
        List<String> messages = notifier.getSentMessages();
        assertThrows(UnsupportedOperationException.class, () -> messages.add("Fake Message"));
    }

    /** getSentMessages() reflects real-time updates after notify() */
    @Test
    void testGetSentMessagesReflectsUpdates() {
        assertTrue(notifier.getSentMessages().isEmpty());

        notifier.notify(user, "First");
        assertEquals(1, notifier.getSentMessages().size());

        notifier.notify(user, "Second");
        assertEquals(2, notifier.getSentMessages().size());
    }

    // clearMessages()

    /** clearMessages() removes all recorded messages */
    @Test
    void testClearMessages() {
        notifier.notify(user, "Message 1");
        notifier.notify(user, "Message 2");
        assertEquals(2, notifier.getSentMessages().size());

        notifier.clearMessages();
        assertTrue(notifier.getSentMessages().isEmpty());
    }

    /** clearMessages() on empty list has no effect or error */
    @Test
    void testClearMessagesWhenEmpty() {
        assertDoesNotThrow(() -> notifier.clearMessages());
        assertTrue(notifier.getSentMessages().isEmpty());
    }
}


