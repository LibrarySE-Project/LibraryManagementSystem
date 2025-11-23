package librarySE.managers;


import librarySE.utils.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    User user;

    @BeforeEach
    void setup() {
        user = new User("Malak", Role.USER, "pass123", "m@ps.com");
    }

    // constructor must set fields correctly
    @Test
    void testConstructorStoresFields() {
        assertEquals("Malak", user.getUsername());
        assertEquals(Role.USER, user.getRole());
        assertEquals("m@ps.com", user.getEmail());
        assertEquals(BigDecimal.ZERO, user.getFineBalance());
    }

    // constructor rejects null username
    @Test
    void testConstructorRejectsNullUsername() {
        assertThrows(NullPointerException.class,
                () -> new User(null, Role.USER, "pass123", "a@a.com"));
    }

    // constructor rejects null password
    @Test
    void testConstructorRejectsNullPassword() {
        assertThrows(NullPointerException.class,
                () -> new User("X", Role.USER, null, "a@a.com"));
    }

    // constructor rejects invalid email
    @Test
    void testConstructorRejectsInvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("X", Role.USER, "pass123", "wrongEmail"));
    }

    // checkPassword success
    @Test
    void testCheckPasswordSuccess() {
        assertTrue(user.checkPassword("pass123"));
    }

    // checkPassword fails wrong input
    @Test
    void testCheckPasswordWrong() {
        assertFalse(user.checkPassword("wrong"));
    }

    // changePassword success
    @Test
    void testChangePasswordSuccess() {
        boolean ok = user.changePassword("pass123", "newpass1");
        assertTrue(ok);
        assertTrue(user.checkPassword("newpass1"));
    }

    // changePassword fails if old password wrong
    @Test
    void testChangePasswordWrongOldPassword() {
        boolean ok = user.changePassword("wrong", "newpass1");
        assertFalse(ok);
    }

    // addFine increases balance
    @Test
    void testAddFine() {
        user.addFine(BigDecimal.valueOf(20));
        assertEquals(BigDecimal.valueOf(20), user.getFineBalance());
    }

    // addFine rejects negative
    @Test
    void testAddFineRejectsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> user.addFine(BigDecimal.valueOf(-5)));
    }

    // payFine reduces balance
    @Test
    void testPayFine() {
        user.addFine(BigDecimal.valueOf(20));
        user.payFine(BigDecimal.valueOf(5));
        assertEquals(BigDecimal.valueOf(15), user.getFineBalance());
    }

    // payFine rejects negative
    @Test
    void testPayFineRejectsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> user.payFine(BigDecimal.valueOf(-1)));
    }

    // payFine rejects more than balance
    @Test
    void testPayFineRejectsTooMuch() {
        user.addFine(BigDecimal.valueOf(10));
        assertThrows(IllegalArgumentException.class,
                () -> user.payFine(BigDecimal.valueOf(20)));
    }

    // hasOutstandingFine works
    @Test
    void testHasOutstandingFine() {
        assertFalse(user.hasOutstandingFine());
        user.addFine(BigDecimal.TEN);
        assertTrue(user.hasOutstandingFine());
    }

    // setUsername works
    @Test
    void testSetUsername() {
        user.setUsername("New");
        assertEquals("New", user.getUsername());
    }

    // setUsername rejects empty
    @Test
    void testSetUsernameRejectsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> user.setUsername(""));
    }

    // setEmail works
    @Test
    void testSetEmail() {
        user.setEmail("test@najah.edu");
        assertEquals("test@najah.edu", user.getEmail());
    }

    // setEmail rejects invalid
    @Test
    void testSetEmailRejectsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> user.setEmail("wrongEmail"));
    }

    // equals uses ID, not username
    @Test
    void testEquals() {
        User other = new User("Other", Role.USER, "pass123", "o@o.com");
        assertNotEquals(user, other);
    }

    // isAdmin works
    @Test
    void testIsAdmin() {
        User admin = new User("A", Role.ADMIN, "pass123", "a@a.com");
        assertTrue(admin.isAdmin());
        assertFalse(user.isAdmin());
    }
}

