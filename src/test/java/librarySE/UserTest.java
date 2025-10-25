package librarySE;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.*;

/**
 * Unit tests for {@link User} class.
 * Covers constructor, role checks, password management, fines, unique ID, and string representation.
 */
class UserTest {

    private User normalUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        normalUser = new User("adm", Role.USER, "alicePass", "alice@domain.com");
        adminUser = new User("boss", Role.ADMIN, "adminPass", "boss@domain.com");
    }

    @AfterEach
    void tearDown() {
        normalUser = null;
        adminUser = null;
    }

    /** Tests constructor and getter methods. */
    @Test
    void testConstructorAndGetters() {
        assertEquals("adm", normalUser.getUsername());
        assertEquals(Role.USER, normalUser.getRole());
        assertEquals(0, normalUser.getFineBalance().compareTo(BigDecimal.ZERO));
        assertNotNull(normalUser.getUserId()); // UUID should not be null
    }

    /** Tests constructor throws exception when arguments are null. */
    @Test
    void testConstructorWithNullsThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new User(null, Role.USER, "pass", "email@domain.com"));
        assertThrows(IllegalArgumentException.class, () -> new User("user", null, "pass", "email@domain.com"));
        assertThrows(IllegalArgumentException.class, () -> new User("user", Role.ADMIN, null, "email@domain.com"));
        assertThrows(IllegalArgumentException.class, () -> new User("user", Role.USER, "pass", null));
    }

    /** Tests isAdmin method for USER and ADMIN roles. */
    @Test
    void testIsAdmin() {
        assertFalse(normalUser.isAdmin());
        assertTrue(adminUser.isAdmin());
    }

    /** Tests password verification for correct, wrong, and null passwords. */
    @Test
    void testPasswordVerification() {
        assertTrue(normalUser.checkPassword("alicePass"));
        assertFalse(normalUser.checkPassword("wrongPass"));
        assertFalse(normalUser.checkPassword(null));
    }

    /** Tests changing password successfully and failing with wrong or null inputs. */
    @Test
    void testChangePasswordSuccessAndFailure() {
        assertTrue(normalUser.changePassword("alicePass", "newPass123"));
        assertTrue(normalUser.checkPassword("newPass123"));
        assertFalse(normalUser.checkPassword("alicePass"));

        assertFalse(adminUser.changePassword("wrong", "newAdminPass"));
        assertTrue(adminUser.checkPassword("adminPass"));

        assertThrows(IllegalArgumentException.class,
                () -> normalUser.changePassword("newPass123", "short")); // too short
    }

    /** Tests that addFine increments fine balance correctly. */
    @Test
    void testAddFineIncrementsBalance() {
        normalUser.addFine(new BigDecimal("5.0"));
        assertEquals(0, normalUser.getFineBalance().compareTo(new BigDecimal("5.0")));
        normalUser.addFine(new BigDecimal("2.5"));
        assertEquals(0, normalUser.getFineBalance().compareTo(new BigDecimal("7.5")));
    }

    /** Tests that adding negative fine throws exception. */
    @Test
    void testAddFineNegativeThrowsException() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> normalUser.addFine(new BigDecimal("-1")));
        assertEquals("Fine amount cannot be negative", ex.getMessage());
    }

    /** Tests paying fine partially and fully reduces fine balance. */
    @Test
    void testPayFineNormalAndFull() {
        normalUser.addFine(new BigDecimal("10.0"));
        normalUser.payFine(new BigDecimal("4.0"));
        assertEquals(0, normalUser.getFineBalance().compareTo(new BigDecimal("6.0")));
        normalUser.payFine(new BigDecimal("6.0"));
        assertEquals(0, normalUser.getFineBalance().compareTo(BigDecimal.ZERO));
    }

    /** Tests canBorrow behavior depending on fine balance. */
    @Test
    void testCanBorrowBehavior() {
        assertTrue(normalUser.canBorrow());
        normalUser.addFine(new BigDecimal("3"));
        assertFalse(normalUser.canBorrow());
        normalUser.payFine(new BigDecimal("3"));
        assertTrue(normalUser.canBorrow());
    }

    /** Tests equality and hash code based on userId. */
    @Test
    void testEqualityAndHashCodeWithUserId() {
        User u1 = new User("sameName", Role.USER, "pass1", "u1@domain.com");
        User u2 = new User("sameName", Role.USER, "pass2", "u2@domain.com");

        assertNotEquals(u1, u2); // even with same username, different UUID
        assertNotEquals(u1.hashCode(), u2.hashCode());

        // equality to itself
        assertEquals(u1, u1);
        assertEquals(u1.hashCode(), u1.hashCode());
    }

    /** Tests that toString contains username and role. */
    @Test
    void testToStringContainsRoleAndUsername() {
        String s = normalUser.toString();
        assertTrue(s.contains("adm"));
        assertTrue(s.contains("USER"));
    }

    /** Tests that hashPassword throws RuntimeException on invalid algorithm. */
    @Test
    void testHashPasswordCatchBlockTriggered() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            User.hashPassword("password123", "INVALID_ALGO");
        });

        assertTrue(ex.getMessage().contains("Error hashing password"));
        assertTrue(ex.getCause() instanceof NoSuchAlgorithmException);
    }
}

