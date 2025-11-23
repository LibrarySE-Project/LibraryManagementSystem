package librarySE.managers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class AdminTest {

    @BeforeEach
    void resetSingleton() throws Exception {
        // Reset Admin.instance using reflection for clean tests
        var field = Admin.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
    }

    // Initialization Tests
    @Test
    void testInitialize_CreatesInstance() {
        Admin.initialize("admin", "pass123", "a@a.com");
        assertNotNull(Admin.getInstance());
    }

    @Test
    void testInitialize_Twice_Throws() {
        Admin.initialize("admin", "pass123", "a@a.com");
        assertThrows(IllegalStateException.class,
                () -> Admin.initialize("x", "y", "z"));
    }

    // GetInstance Tests
    @Test
    void testGetInstance_WithoutInit_Throws() {
        assertThrows(IllegalStateException.class, Admin::getInstance);
    }

    @Test
    void testGetInstance_AfterInit_ReturnsSameInstance() {
        Admin.initialize("admin", "pass123", "a@a.com");
        Admin a1 = Admin.getInstance();
        Admin a2 = Admin.getInstance();
        assertSame(a1, a2);
    }

    // Login Tests
    @Test
    void testLogin_Success() {
        Admin.initialize("admin", "pass123", "a@a.com");
        Admin a = Admin.getInstance();
        assertTrue(a.login("admin", "pass123"));
        assertTrue(a.isLoggedIn());
    }

    @Test
    void testLogin_WrongPassword_Fails() {
        Admin.initialize("admin", "pass123", "a@a.com");
        Admin a = Admin.getInstance();
        assertFalse(a.login("admin", "WRONG"));
        assertFalse(a.isLoggedIn());
    }

    @Test
    void testLogin_WrongUsername_Fails() {
        Admin.initialize("admin", "pass123", "a@a.com");
        Admin a = Admin.getInstance();
        assertFalse(a.login("not_admin", "pass123"));
    }

    // Logout Tests
    @Test
    void testLogout_ClearsLoginState() {
        Admin.initialize("admin", "pass123", "a@a.com");
        Admin a = Admin.getInstance();
        a.login("admin", "pass123");
        a.logout();
        assertFalse(a.isLoggedIn());
    }
}

