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
        assertEquals("alicePass", normalUser.getPassword());
        assertEquals("adminPass", adminUser.getPassword());
    }

    @Test
    void testIsAdmin() {
        assertFalse(normalUser.isAdmin());
        assertTrue(adminUser.isAdmin());
    }

    @Test
    void testToString() {
        assertEquals("adm (USER) [Fine: 0.0]", normalUser.toString());
        assertEquals("boss (ADMIN) [Fine: 0.0]", adminUser.toString());
    }


    @Test
    void testCheckPassword() {
        assertTrue(normalUser.checkPassword("alicePass"));
        assertTrue(adminUser.checkPassword("adminPass"));
        assertFalse(normalUser.checkPassword("wrongPass"));
        assertFalse(adminUser.checkPassword("12345"));
    }

    @Test
    void testInitialFineBalance() {
        assertEquals(0.0, normalUser.getFineBalance());
    }

    @Test
    void testAddFine() {
        normalUser.addFine(5.0);
        assertEquals(5.0, normalUser.getFineBalance());

        normalUser.addFine(2.5);
        assertEquals(7.5, normalUser.getFineBalance());
    }

    @Test
    void testAddFineNegative() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            normalUser.addFine(-5.0);
        });
        assertEquals("Fine amount cannot be negative", exception.getMessage());
    }

    @Test
    void testPayFine() {
        normalUser.addFine(10.0);
        normalUser.payFine(4.0);
        assertEquals(6.0, normalUser.getFineBalance());

        normalUser.payFine(6.0);
        assertEquals(0.0, normalUser.getFineBalance());
    }

    @Test
    void testPayFineNegative() {
        normalUser.addFine(10.0);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            normalUser.payFine(-3.0);
        });
        assertEquals("Payment amount cannot be negative", exception.getMessage());
    }

    @Test
    void testPayFineExceedBalance() {
        normalUser.addFine(5.0);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            normalUser.payFine(10.0);
        });
        assertEquals("Payment exceeds current fine balance", exception.getMessage());
    }

    @Test
    void testCanBorrow() {
        // fine 0
        assertTrue(normalUser.canBorrow());

        // fine > 0
        normalUser.addFine(5.0);
        assertFalse(normalUser.canBorrow());

        // pay fine to 0
        normalUser.payFine(5.0);
        assertTrue(normalUser.canBorrow());
    }

    @Test
    void testCheckPasswordWithNull() {
        User userWithNullPass = new User("nullUser", Role.USER, null);
        assertThrows(NullPointerException.class, () -> userWithNullPass.checkPassword("any"));
        assertTrue(userWithNullPass.checkPassword(null)); // null == null
    }
}


