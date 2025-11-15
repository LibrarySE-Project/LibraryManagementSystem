package librarySE;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import librarySE.managers.Admin;
import librarySE.managers.LoginManager;

class LoginManagerTest {

    private Admin admin;
    private LoginManager loginManager;

    /**
     * Runs before each test.
     * Initializes the singleton Admin instance, logs out,
     * and creates a new LoginManager instance for testing.
     */
    @BeforeEach
    void setUp() throws Exception {
        admin = Admin.getInstance("adminUser", "adminPass");
        admin.logout();
        loginManager = new LoginManager(admin);
    }

    /**
     * Runs after each test.
     * Ensures the admin is logged out to reset state.
     */
    @AfterEach
    void tearDown() throws Exception {
        admin.logout();
    }

    /**
     * Test successful login via LoginManager.
     * Verifies that login with correct credentials returns true
     * and admin is marked as logged in.
     */
    @Test
    void testLoginSuccess() {
        boolean result = loginManager.login("adminUser", "adminPass");
        assertTrue(result, "Login should succeed with correct username and password");
        assertTrue(admin.isLoggedIn(), "Admin should be marked as logged in");
    }

    /**
     * Test failed login via LoginManager.
     * Verifies that login with incorrect credentials returns false
     * and admin remains logged out.
     */
    @Test
    void testLoginFailure() {
        boolean result = loginManager.login("wrongUser", "wrongPass");
        assertFalse(result, "Login should fail with incorrect username and password");
        assertFalse(admin.isLoggedIn(), "Admin should not be marked as logged in");
    }

    /**
     * Test logout via LoginManager.
     * Ensures that logout sets the admin state to logged out.
     */
    @Test
    void testLogout() {
        loginManager.login("adminUser", "adminPass");
        loginManager.logout();
        assertFalse(admin.isLoggedIn(), "Admin should be logged out after logout operation");
    }

    /**
     * Test isLoggedIn() status.
     * Checks that isLoggedIn reflects the current login state correctly.
     */
    @Test
    void testIsLoggedInStatus() {
        assertFalse(loginManager.isLoggedIn(), "Initially, admin should not be logged in");
        loginManager.login("adminUser", "adminPass");
        assertTrue(loginManager.isLoggedIn(), "After login, admin should be logged in");
        loginManager.logout();
        assertFalse(loginManager.isLoggedIn(), "After logout, admin should not be logged in");
    }
}

