package librarySE;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserTest {

    private User normalUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        normalUser = new User("adm", Role.USER, "alicePass");
        adminUser = new User("boss", Role.ADMIN, "adminPass");
    }

    @AfterEach
    void tearDown() {
        normalUser = null;
        adminUser = null;
    }

    @Test
    void testGetters() {
        assertEquals("adm", normalUser.getUsername());
        assertEquals("boss", adminUser.getUsername());
        assertEquals(Role.USER, normalUser.getRole());
        assertEquals(Role.ADMIN, adminUser.getRole());
    }

    @Test
    void testIsAdmin() {
        assertFalse(normalUser.isAdmin());
        assertTrue(adminUser.isAdmin());
    }

    @Test
    void testToString() {
        assertEquals("adm (USER)", normalUser.toString());
        assertEquals("boss (ADMIN)", adminUser.toString());
    }
}
