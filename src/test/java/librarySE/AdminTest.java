package librarySE;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import java.util.*;

/**
 * Unit tests for the {@link Admin} class.
 * <p>
 * Tests Singleton behavior, login/logout logic, notifier switching, 
 * and sending overdue reminders to users.
 * </p>
 */
class AdminTest {

    private Admin admin;
    private Observer mockNotifier;
    private Library library;
    private User user1;
    private User user2;

    /** 
     * Setup a fresh environment before each test.
     * Since Admin is a Singleton, we reset the instance via reflection.
     */
    @BeforeEach
    void setUp() throws Exception {
        // Reset the Singleton instance using reflection
        var field = Admin.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);

        mockNotifier = new EmailNotifier();
        admin = Admin.getInstance("admin", "123456", "admin@najah.edu", mockNotifier);
        library = new Library();

        user1 = new User("Alice", Role.USER, "pass123", "alice@example.com");
        user2 = new User("Bob", Role.USER, "pass456", "bob@example.com");
    }

    /** Verifies that only one Admin instance exists (Singleton). */
    @Test
    void testSingletonInstance() {
        Admin admin2 = Admin.getInstance("another", "xyz", "other@najah.edu", mockNotifier);
        assertSame(admin, admin2, "Only one Admin instance should exist.");
    }

    /** Tests successful login with correct credentials. */
    @Test
    void testLoginSuccess() {
        assertTrue(admin.login("admin", "123456"));
        assertTrue(admin.isLoggedIn(), "Admin should be logged in after successful login.");
    }

    /** Tests login failure with wrong username or password. */
    @Test
    void testLoginFailure() {
        assertFalse(admin.login("wrongUser", "123456"));
        assertFalse(admin.isLoggedIn(), "Admin should remain logged out with invalid username.");

        assertFalse(admin.login("admin", "wrongPass"));
        assertFalse(admin.isLoggedIn(), "Admin should remain logged out with invalid password.");
    }

    /** Tests logout behavior after login. */
    @Test
    void testLogout() {
        admin.login("admin", "123456");
        admin.logout();
        assertFalse(admin.isLoggedIn(), "Admin should not be logged in after logout.");
    }

    /** Tests that notifier can be changed at runtime. */
    @Test
    void testSetNotifierSuccess() {
        Observer newNotifier = new EmailNotifier();
        admin.setNotifier(newNotifier);
        assertNotNull(newNotifier);
    }

    /** Tests that setting a null notifier throws exception. */
    @Test
    void testSetNotifierNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> admin.setNotifier(null));
    }

    /** 
     * Tests that Admin cannot send reminders if not logged in. 
     * Expect IllegalStateException.
     */
    @Test
    void testSendRemindersWithoutLogin() {
        assertThrows(IllegalStateException.class, () -> admin.sendReminders(library));
    }

    /**
     * Tests sending reminders when users have overdue items.
     * Uses {@link EmailNotifier} to simulate message sending.
     */
    @Test
    void testSendRemindersWhenLoggedIn() {
        // Create mock data
        Book book = new Book("Java Basics", "1234", "John Doe", 2020);
        admin.login("admin", "123456");
        library.addItem(book, admin);

        // Borrow book and mark it overdue
        library.borrowItem(user1, "Java Basics", new BookFineStrategy());
        var record = library.getAllBorrowRecords().get(0);
        record.setBorrowDate(record.getBorrowDate().minusDays(20)); // make overdue

        // Send reminders
        admin.sendReminders(library);

        EmailNotifier emailNotifier = (EmailNotifier) mockNotifier;
        assertFalse(emailNotifier.getSentMessages().isEmpty(), "Notifier should record sent messages.");
        assertTrue(emailNotifier.getSentMessages().get(0).contains("overdue"), "Message should mention overdue items.");
    }

    /** Tests that Admin remains the same even after multiple getInstance() calls. */
    @Test
    void testMultipleGetInstanceReturnsSameObject() {
        Admin admin2 = Admin.getInstance("admin2", "pass2", "other@najah.edu", mockNotifier);
        assertSame(admin, admin2);
    }

    /** Tests login and reminder workflow end-to-end. */
    @Test
    void testFullWorkflow() {
        admin.login("admin", "123456");
        assertTrue(admin.isLoggedIn());

        Book book = new Book("AI", "5678", "Andrew Ng", 2021);
        library.addItem(book, admin);
        library.borrowItem(user2, "AI", new BookFineStrategy());
        var record = library.getAllBorrowRecords().get(0);
        record.setBorrowDate(record.getBorrowDate().minusDays(15)); // overdue

        admin.sendReminders(library);

        EmailNotifier notifier = (EmailNotifier) mockNotifier;
        List<String> messages = notifier.getSentMessages();
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).contains("bob@example.com"));
    }
}




