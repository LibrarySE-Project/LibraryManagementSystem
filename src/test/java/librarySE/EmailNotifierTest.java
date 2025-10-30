package librarySE;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.*;

/**
 * Unit tests for {@link EmailNotifier}.
 * <p>
 * Covers all public methods including message recording, clearing,
 * and immutability of the returned list.
 * </p>
 */
class EmailNotifierTest {

    private EmailNotifier notifier;
    private User user1;
    private User user2;

    /**
     * Initializes fresh notifier and users before each test.
     */
    @BeforeEach
    void setUp() {
        notifier = new EmailNotifier();
        user1 = new User("Alice", "alice@example.com", "password123", "user");
        user2 = new User("Bob", "bob@example.com", "pass456", "user");
    }

    /**
     * Tests that notify() correctly records messages.
     */
    @Test
    void testNotifyRecordsMessage() {
        notifier.notify(user1, "Your book is overdue!");
        List<String> messages = notifier.getSentMessages();

        assertEquals(1, messages.size());
        assertTrue(messages.get(0).contains("alice@example.com"));
        assertTrue(messages.get(0).contains("Your book is overdue!"));
    }

    /**
     * Tests multiple notifications are recorded in order.
     */
    @Test
    void testMultipleNotifications() {
        notifier.notify(user1, "First message");
        notifier.notify(user2, "Second message");

        List<String> messages = notifier.getSentMessages();

        assertEquals(2, messages.size());
        assertTrue(messages.get(0).contains("alice@example.com"));
        assertTrue(messages.get(1).contains("bob@example.com"));
    }

    /**
     * Tests that getSentMessages() returns an unmodifiable list.
     */
    @Test
    void testGetSentMessagesIsUnmodifiable() {
        notifier.notify(user1, "Cannot modify this list");
        List<String> messages = notifier.getSentMessages();

        assertThrows(UnsupportedOperationException.class, () -> {
            messages.add("This should fail");
        });
    }

    /**
     * Tests that clearMessages() removes all recorded messages.
     */
    @Test
    void testClearMessages() {
        notifier.notify(user1, "Some message");
        notifier.clearMessages();
        assertTrue(notifier.getSentMessages().isEmpty());
    }

    /**
     * Tests that notifying with an empty message still records correctly.
     */
    @Test
    void testNotifyWithEmptyMessage() {
        notifier.notify(user1, "");
        List<String> messages = notifier.getSentMessages();

        assertEquals(1, messages.size());
        assertTrue(messages.get(0).contains("alice@example.com"));
        assertTrue(messages.get(0).contains("Message: "));
    }

    /**
     * Tests that notifying multiple threads simultaneously does not cause errors.
     * This ensures thread safety of the synchronized list.
     */
    @Test
    void testThreadSafety() throws InterruptedException {
        Runnable task = () -> notifier.notify(user1, "Concurrent message");

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        Thread t3 = new Thread(task);

        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();

        assertEquals(3, notifier.getSentMessages().size());
    }
}

