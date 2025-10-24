package librarySE;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminTest {
    
    private Admin admin;
    
    /**
     * Runs before each test.
     * Initializes the singleton Admin instance and ensures it is logged out.
     */
    @BeforeEach
    void setUp() throws Exception {
        admin = Admin.getInstance("adminUser", "adminPass");
        admin.logout();
    }

    /**
     * Runs after each test.
     * Ensures the admin is logged out after each test to reset state.
     */
    @AfterEach
    void tearDown() throws Exception {
        admin.logout();
    }

    /**
     * Test singleton behavior.
     * Verifies that getInstance always returns the same Admin object.
     */
    @Test
    void testSingleton() {
        Admin anotherAdmin = admin.getInstance("anotherUser", "anotherPass");
        assertSame(admin, anotherAdmin, "getInstance should always return the same Admin instance");
    }

    /**
     * Test successful login.
     * Checks that logging in with correct credentials returns true
     * and marks the admin as logged in.
     */
    @Test
    void testLoginSuccess() {
        boolean result = admin.login("adminUser", "adminPass");
        assertTrue(result, "Login should succeed with correct username and password");
        assertTrue(admin.isLoggedIn(), "Admin should be marked as logged in");
    }

    /**
     * Test failed login.
     * Checks that logging in with incorrect credentials returns false
     * and admin remains logged out.
     */
    @Test
    void testLoginFailure() {
        boolean result = admin.login("wrongUser", "wrongPass");
        assertFalse(result, "Login should fail with incorrect username and password");
        assertFalse(admin.isLoggedIn(), "Admin should not be marked as logged in");
    }

    /**
     * Test logout functionality.
     * Ensures that logout sets the admin state to logged out.
     */
    @Test
    void testLogout() {
        admin.login("adminUser", "adminPass");
        admin.logout();
        assertFalse(admin.isLoggedIn(), "Admin should be logged out after logout operation");
    }

    /**
     * Test getUsername() method.
     * Ensures that the correct username is returned.
     */
    @Test
    void testGetUsername() {
        String username = admin.getUsername();
        assertEquals("adminUser", username, "getUsername should return the correct username");
    }
}

