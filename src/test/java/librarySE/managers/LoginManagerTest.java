package librarySE.managers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginManagerTest {

    @BeforeEach
    void resetAdminSingleton() throws Exception {
        var field = Admin.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);

        Admin.initialize("admin", "pass123", "admin@ps.com");
    }

    // Constructor Tests
    @Test
    void testConstructor_StoresAdminReference() {
        LoginManager lm = new LoginManager(Admin.getInstance());
        assertNotNull(lm);
    }

    // Login Tests
    @Test
    void testLogin_Success() {
        LoginManager lm = new LoginManager(Admin.getInstance());
        assertTrue(lm.login("admin", "pass123"));
        assertTrue(lm.isLoggedIn());
    }

    @Test
    void testLogin_WrongPassword() {
        LoginManager lm = new LoginManager(Admin.getInstance());
        assertFalse(lm.login("admin", "wrong"));
        assertFalse(lm.isLoggedIn());
    }

    @Test
    void testLogin_WrongUsername() {
        LoginManager lm = new LoginManager(Admin.getInstance());
        assertFalse(lm.login("nope", "pass123"));
    }

    @Test
    void testLogin_NullInputs_Throws() {
        LoginManager lm = new LoginManager(Admin.getInstance());

        assertThrows(IllegalArgumentException.class, () -> lm.login(null, "x"));
        assertThrows(IllegalArgumentException.class, () -> lm.login("admin", null));
        assertThrows(IllegalArgumentException.class, () -> lm.login("", "x"));
        assertThrows(IllegalArgumentException.class, () -> lm.login("admin", ""));
    }

    // Logout Tests
    @Test
    void testLogout() {
        LoginManager lm = new LoginManager(Admin.getInstance());
        lm.login("admin", "pass123");
        lm.logout();
        assertFalse(lm.isLoggedIn());
    }

    // isLoggedIn Tests
    @Test
    void testIsLoggedIn_InitialFalse() {
        LoginManager lm = new LoginManager(Admin.getInstance());
        assertFalse(lm.isLoggedIn());
    }
}

