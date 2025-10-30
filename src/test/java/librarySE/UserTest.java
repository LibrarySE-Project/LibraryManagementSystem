package librarySE;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.*;

/**
 * Unit tests for {@link User} class.
 * <p>
 * Covers constructor, email validation, role checks, password management,
 * fine operations, equality, and toString behavior.
 * </p>
 */
class UserTest {

    private User normalUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        normalUser = new User("user1", Role.USER, "userPass", "user1@gmail.com");
        adminUser = new User("admin1", Role.ADMIN, "adminPass", "admin1@najah.edu");
    }

    @AfterEach
    void tearDown() {
        normalUser = null;
        adminUser = null;
    }

    /** Basic constructor and getters */
    @Test
    void testConstructorAndGetters() {
        assertEquals("user1", normalUser.getUsername());
        assertEquals(Role.USER, normalUser.getRole());
        assertEquals("user1@gmail.com", normalUser.getEmail());
        assertEquals(0, normalUser.getFineBalance().compareTo(BigDecimal.ZERO));
    }

    /** Constructor throws on null or invalid parameters */
    @Test
    void testConstructorWithInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> new User(null, Role.USER, "pass", "mail@x.com"));
        assertThrows(IllegalArgumentException.class, () -> new User("user", null, "pass", "mail@x.com"));
        assertThrows(IllegalArgumentException.class, () -> new User("user", Role.USER, null, "mail@x.com"));
        assertThrows(IllegalArgumentException.class, () -> new User("user", Role.USER, "pass", null));
    }

    /** Email validation formats */
    @Test
    void testEmailValidationFormats() {
        assertDoesNotThrow(() -> new User("a", Role.USER, "pass", "user@najah.edu"));
        assertDoesNotThrow(() -> new User("a", Role.USER, "pass", "abc123@gmail.com"));
        assertThrows(IllegalArgumentException.class, () -> new User("a", Role.USER, "pass", "wrongemail"));
        assertThrows(IllegalArgumentException.class, () -> new User("a", Role.USER, "pass", "user@invalid.xyz"));
    }

    /** setEmail trims and lowercases properly */
    @Test
    void testSetEmailTrimmingAndLowercase() {
        normalUser.setEmail("  USER2@GMAIL.COM ");
        assertEquals("user2@gmail.com", normalUser.getEmail());
    }

    /** setEmail invalid formats throw exception */
    @Test
    void testSetEmailInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> normalUser.setEmail("invalid"));
        assertThrows(IllegalArgumentException.class, () -> normalUser.setEmail(null));
    }

    /** setUsername validation */
    @Test
    void testSetUsernameValidation() {
        normalUser.setUsername("  newName ");
        assertEquals("newName", normalUser.getUsername());
        assertThrows(IllegalArgumentException.class, () -> normalUser.setUsername("   "));
        assertThrows(IllegalArgumentException.class, () -> normalUser.setUsername(null));
    }

    /** Role check */
    @Test
    void testIsAdminCheck() {
        assertTrue(adminUser.isAdmin());
        assertFalse(normalUser.isAdmin());
    }

    /** Password verification */
    @Test
    void testPasswordVerification() {
        assertTrue(adminUser.checkPassword("adminPass"));
        assertFalse(adminUser.checkPassword("wrong"));
        assertFalse(adminUser.checkPassword(null));
    }

    /** Change password success and failure */
    @Test
    void testChangePassword() {
        assertTrue(normalUser.changePassword("userPass", "newPass123"));
        assertTrue(normalUser.checkPassword("newPass123"));
        assertFalse(normalUser.checkPassword("userPass"));

        // Wrong old password → no change
        assertFalse(adminUser.changePassword("wrongOld", "newAdmin"));
        assertTrue(adminUser.checkPassword("adminPass"));

        // Too short new password
        assertThrows(IllegalArgumentException.class, () -> normalUser.changePassword("newPass123", "123"));
    }

    /** Change password with null inputs (edge cases) */
    @Test
    void testChangePasswordNullInputs() {
        assertFalse(normalUser.changePassword(null, "newPass"));
        assertFalse(normalUser.changePassword("userPass", null));
        assertFalse(normalUser.changePassword(null, null));
    }

    /** Fine management (add, pay, check outstanding) */
    @Test
    void testFineOperations() {
        normalUser.addFine(new BigDecimal("5"));
        assertEquals(0, normalUser.getFineBalance().compareTo(new BigDecimal("5")));
        assertTrue(normalUser.hasOutstandingFine());

        normalUser.payFine(new BigDecimal("3"));
        assertEquals(0, normalUser.getFineBalance().compareTo(new BigDecimal("2")));

        normalUser.payFine(new BigDecimal("2"));
        assertEquals(0, normalUser.getFineBalance().compareTo(BigDecimal.ZERO));
        assertFalse(normalUser.hasOutstandingFine());
    }

    /** Add fine with negative amount */
    @Test
    void testAddFineNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> normalUser.addFine(new BigDecimal("-1")));
    }

    /** Add fine with null → NPE */
    @Test
    void testAddFineNullThrows() {
        assertThrows(NullPointerException.class, () -> normalUser.addFine(null));
    }

    /** Pay fine invalid values */
    @Test
    void testPayFineInvalidValues() {
        normalUser.addFine(new BigDecimal("10"));
        assertThrows(IllegalArgumentException.class, () -> normalUser.payFine(new BigDecimal("-5")));
        assertThrows(IllegalArgumentException.class, () -> normalUser.payFine(new BigDecimal("15")));
    }

    /** Pay zero fine does nothing */
    @Test
    void testPayFineZero() {
        normalUser.addFine(new BigDecimal("5"));
        normalUser.payFine(BigDecimal.ZERO);
        assertEquals(0, normalUser.getFineBalance().compareTo(new BigDecimal("5")));
    }

    /** Equality & hashCode based on ID only */
    @Test
    void testEqualityAndHashCode() {
        User u1 = new User("x", Role.USER, "p", "x@gmail.com");
        User u2 = new User("x", Role.USER, "p", "x@gmail.com");
        assertNotEquals(u1, u2);
        assertNotEquals(u1.hashCode(), u2.hashCode());
        assertEquals(u1, u1);
    }

    /** toString includes username, role, and email */
    @Test
    void testToStringIncludesData() {
        String s = adminUser.toString();
        assertTrue(s.contains("admin1"));
        assertTrue(s.contains("ADMIN"));
        assertTrue(s.contains("najah.edu"));
    }

    /** Password hashing with invalid algorithm triggers RuntimeException */
    @Test
    void testInvalidHashAlgorithmThrows() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            User.hashPassword("password", "INVALID");
        });
        assertTrue(ex.getMessage().contains("Error hashing password"));
        assertTrue(ex.getCause() instanceof NoSuchAlgorithmException);
    }
}




