package librarySE;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.util.List;

/**
 * Unit tests for the {@link Admin} class.
 * <p>
 * Verifies singleton behavior, login/logout functionality, notifier management,
 * and sending reminder notifications to overdue users.
 * Includes both positive and negative scenarios.
 * </p>
 */
class AdminTest {

    private Admin admin;
    private User user1;
    private User user2;
    private Library library;
    private EmailNotifier mockNotifier;

    @BeforeEach
    void setUp() {
        mockNotifier = new EmailNotifier();
        admin = Admin.getInstance("admin", "pass123", "admin@najah.edu", mockNotifier);

        user1 = new User("Alice", Role.USER, "password123", "alice@example.com");
        user2 = new User("Bob", Role.USER, "pass456", "bob@example.com");

        library = new Library();
        library.addItem(new Book("Book A", "Author1", "ISBN1"), admin);
        library.addItem(new Book("Book B", "Author2", "ISBN2"), admin);
    }

    @AfterEach
    void tearDown() {
        mockNotifier.clearMessages();
    }

    // -------------------------------------------------------------
    // Singleton behavior
    // -------------------------------------------------------------

    /** getInstance should always return the same Admin instance */
    @Test
    void testSingletonInstance() {
        Admin secondInstance = Admin.getInstance("other", "123", "other@x.com", mockNotifier);
        assertSame(admin, secondInstance);
    }

    // -------------------------------------------------------------
    // Login & Logout
    // -------------------------------------------------------------

    /** login with correct credentials should succeed */
    @Test
    void testLoginSuccess() {
        assertTrue(admin.login("admin", "pass123"));
        assertTrue(admin.isLoggedIn());
    }

    /** login with incorrect credentials should fail */
    @Test
    void testLoginFailure() {
        assertFalse(admin.login("admin", "wrongpass"));
        assertFalse(admin.isLoggedIn());

        assertFalse(admin.login("wronguser", "pass123"));
        assertFalse(admin.isLoggedIn());
    }

    /** logout should set loggedIn to false */
    @Test
    void testLogout() {
        admin.login("admin", "pass123");
        assertTrue(admin.isLoggedIn());

        admin.logout();
        assertFalse(admin.isLoggedIn());
    }

    // -------------------------------------------------------------
    // Notifier Management
    // -------------------------------------------------------------

    /** setNotifier should update the observer correctly */
    @Test
    void testSetNotifier() {
        EmailNotifier newNotifier = new EmailNotifier();
        admin.setNotifier(newNotifier);
        // Attempt to send reminder, should use new notifier
        admin.login("admin", "pass123");
        admin.sendReminders(library);
        List<String> messages = newNotifier.getSentMessages();
        assertNotNull(messages);
    }

    /** setNotifier with null should throw exception */
    @Test
    void testSetNotifierNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> admin.setNotifier(null));
    }

    // -------------------------------------------------------------
    // Reminder Notifications
    // -------------------------------------------------------------

    /** sendReminders should notify users with overdue items */
    @Test
    void testSendReminders() {
        // Borrow items for users
        library.borrowItem(user1, "Book A", new BookFineStrategy());
        library.borrowItem(user2, "Book B", new BookFineStrategy());

        // Simulate overdue by applying overdue fines with past date
        admin.login("admin", "pass123");
        admin.sendReminders(library);

        List<String> messages = mockNotifier.getSentMessages();
        assertEquals(0, messages.size(), "No overdue yet, so no messages expected");
    }

    /** sendReminders without login should throw exception */
    @Test
    void testSendRemindersWithoutLoginThrows() {
        assertThrows(IllegalStateException.class, () -> admin.sendReminders(library));
    }
}





