package librarySE.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setup() {
        user = new User("Malak", Role.USER, "pass123", "m@ps.com");
    }

    // constructor 

    @Test
    void constructorStoresFields() {
        assertEquals("Malak", user.getUsername());
        assertEquals(Role.USER, user.getRole());
        assertEquals("m@ps.com", user.getEmail());
        assertEquals(BigDecimal.ZERO, user.getFineBalance());
        assertNotNull(user.getId());
    }

    @Test
    void constructorRejectsNullUsername() {
        assertThrows(NullPointerException.class,
                () -> new User(null, Role.USER, "pass123", "a@a.com"));
    }

    @Test
    void constructorRejectsNullPassword() {
        assertThrows(NullPointerException.class,
                () -> new User("X", Role.USER, null, "a@a.com"));
    }

    @Test
    void constructorRejectsInvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("X", Role.USER, "pass123", "wrongEmail"));
    }

    // checkPassword 

    @Test
    void checkPasswordSuccess() {
        assertTrue(user.checkPassword("pass123"));
    }

    @Test
    void checkPasswordWrong() {
        assertFalse(user.checkPassword("wrong"));
    }

    @Test
    void checkPasswordNullEntered() {
        assertFalse(user.checkPassword(null));
    }

    @Test
    void checkPasswordHashNull() throws Exception {
        User u = new User("X", Role.USER, "pass123", "a@a.com");
        Field f = User.class.getDeclaredField("passwordHash");
        f.setAccessible(true);
        f.set(u, null);
        assertFalse(u.checkPassword("pass123"));
    }

    // changePassword

    @Test
    void changePasswordSuccess() {
        boolean ok = user.changePassword("pass123", "newpass1");
        assertTrue(ok);
        assertTrue(user.checkPassword("newpass1"));
    }

    @Test
    void changePasswordWrongOld() {
        boolean ok = user.changePassword("wrong", "newpass1");
        assertFalse(ok);
    }

    // fines 

    @Test
    void addFineIncreasesBalance() {
        user.addFine(BigDecimal.valueOf(20));
        assertEquals(BigDecimal.valueOf(20), user.getFineBalance());
    }

    @Test
    void addFineRejectsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> user.addFine(BigDecimal.valueOf(-5)));
    }

    @Test
    void payFineReducesBalance() {
        user.addFine(BigDecimal.valueOf(20));
        user.payFine(BigDecimal.valueOf(5));
        assertEquals(BigDecimal.valueOf(15), user.getFineBalance());
    }

    @Test
    void payFineRejectsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> user.payFine(BigDecimal.valueOf(-1)));
    }

    @Test
    void payFineRejectsTooMuch() {
        user.addFine(BigDecimal.valueOf(10));
        assertThrows(IllegalArgumentException.class,
                () -> user.payFine(BigDecimal.valueOf(20)));
    }

    @Test
    void hasOutstandingFine() {
        assertFalse(user.hasOutstandingFine());
        user.addFine(BigDecimal.TEN);
        assertTrue(user.hasOutstandingFine());
    }

    // setters

    @Test
    void setUsernameWorks() {
        user.setUsername("New");
        assertEquals("New", user.getUsername());
    }

    @Test
    void setUsernameRejectsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> user.setUsername(""));
    }

    @Test
    void setEmailWorks() {
        user.setEmail("test@najah.edu");
        assertEquals("test@najah.edu", user.getEmail());
    }

    @Test
    void setEmailRejectsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> user.setEmail("wrongEmail"));
    }

    // equals / hashCode / toString / default ctor 

    @Test
    void equalsSameObject() {
        assertTrue(user.equals(user));
    }

    @Test
    void equalsDifferentUsers() {
        User other = new User("Other", Role.USER, "pass123", "o@o.com");
        assertNotEquals(user, other);
    }

    @Test
    void equalsNull() {
        assertFalse(user.equals(null));
    }

    @Test
    void equalsDifferentType() {
        assertFalse(user.equals("not a user"));
    }

    @Test
    void hashCodeUsesId() {
        int expected = Objects.hash(user.getId());
        assertEquals(expected, user.hashCode());
    }

    @Test
    void toStringContainsFields() {
        String s = user.toString();
        assertTrue(s.contains("Malak"));
        assertTrue(s.contains("USER"));
        assertTrue(s.contains("m@ps.com"));
    }

    @Test
    void defaultConstructorInitializesDefaults() {
        User u = new User();
        assertNotNull(u.getId());
        assertEquals("", u.getUsername());
        assertEquals("", u.getEmail());
        assertEquals(Role.USER, u.getRole());
        assertEquals(BigDecimal.ZERO, u.getFineBalance());
    }

    @Test
    void isAdminBranches() {
        User admin = new User("A", Role.ADMIN, "pass123", "a@a.com");
        User normal = new User("B", Role.USER, "pass123", "b@b.com");

        assertTrue(admin.isAdmin());
        assertFalse(normal.isAdmin());
    }

    // hashPassword catch branch 

    @Test
    void hashPassword_failureBranchWrappedInRuntimeException() throws Exception {
        Method m = User.class.getDeclaredMethod("hashPassword", String.class);
        m.setAccessible(true);

        try (MockedStatic<MessageDigest> mdMock =
                     Mockito.mockStatic(MessageDigest.class)) {

            mdMock.when(() -> MessageDigest.getInstance("SHA-256"))
                  .thenThrow(new NoSuchAlgorithmException("boom"));

            InvocationTargetException ex =
                    assertThrows(InvocationTargetException.class,
                            () -> m.invoke(null, "abc"));

            Throwable cause = ex.getCause();
            assertTrue(cause instanceof RuntimeException);
            assertEquals("Error hashing password", cause.getMessage());
            assertTrue(cause.getCause() instanceof NoSuchAlgorithmException);
        }
    }
}


