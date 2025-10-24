package librarySE;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.*;

/**
 * Unit tests for {@link User} class.
 * Covers constructor, role checks, password management, fines, and string representation.
 */
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

    /** Tests constructor and getter methods. */
    @Test
    void testConstructorAndGetters() {
        assertEquals("adm", normalUser.getUsername());
        assertEquals(Role.USER, normalUser.getRole());
        assertEquals(0, normalUser.getFineBalance().compareTo(BigDecimal.ZERO));
    }

    /** Tests constructor throws exception when arguments are null. */
    @Test
    void testConstructorWithNullsThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new User(null, Role.USER, "pass"));
        assertThrows(IllegalArgumentException.class, () -> new User("user", null, "pass"));
        assertThrows(IllegalArgumentException.class, () -> new User("user", Role.ADMIN, null));
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
        assertTrue(normalUser.changePassword("alicePass", "newPass"));
        assertTrue(normalUser.checkPassword("newPass"));
        assertFalse(normalUser.checkPassword("alicePass"));

        assertFalse(adminUser.changePassword("wrong", "newAdmin"));
        assertTrue(adminUser.checkPassword("adminPass"));

        assertFalse(normalUser.changePassword(null, "something"));
        assertFalse(normalUser.changePassword("newPass", null));
    }

    /** Tests behavior with empty passwords. */
    @Test
    void testEmptyPasswordBehavesConsistently() {
        User u = new User("empty", Role.USER, "");
        assertTrue(u.checkPassword(""));
        assertFalse(u.checkPassword(" "));
    }

    /** Tests hash consistency for same and different passwords. */
    @Test
    void testHashConsistencyAndDifference() {
        User u1 = new User("u1", Role.USER, "samePass");
        User u2 = new User("u2", Role.USER, "samePass");
        User u3 = new User("u3", Role.USER, "diffPass");

        assertTrue(u1.checkPassword("samePass"));
        assertTrue(u2.checkPassword("samePass"));
        assertFalse(u1.checkPassword("diffPass"));
        assertFalse(u3.checkPassword("samePass"));
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

    /** Tests that adding null fine throws NullPointerException. */
    @Test
    void testAddFineNullThrowsNPE() {
        assertThrows(NullPointerException.class, () -> normalUser.addFine(null));
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

    /** Tests that paying negative fine throws exception. */
    @Test
    void testPayFineNegativeThrowsException() {
        normalUser.addFine(new BigDecimal("5"));
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> normalUser.payFine(new BigDecimal("-2")));
        assertEquals("Payment amount cannot be negative", ex.getMessage());
    }

    /** Tests paying more than current balance throws exception. */
    @Test
    void testPayFineExceedBalanceThrowsException() {
        normalUser.addFine(new BigDecimal("5"));
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> normalUser.payFine(new BigDecimal("10")));
        assertEquals("Payment exceeds current fine balance", ex.getMessage());
    }

    /** Tests paying zero fine does not change balance. */
    @Test
    void testPayFineZeroAmountNoChange() {
        normalUser.addFine(new BigDecimal("5"));
        normalUser.payFine(BigDecimal.ZERO);
        assertEquals(0, normalUser.getFineBalance().compareTo(new BigDecimal("5")));
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

    /** Tests users with same username but different roles are distinct. */
    @Test
    void testUsersWithSameUsernameDifferentRole() {
        User u2 = new User("adm", Role.ADMIN, "otherPass");
        assertNotSame(normalUser, u2);
        assertNotEquals(normalUser.getRole(), u2.getRole());
        assertFalse(normalUser.checkPassword("otherPass"));
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
