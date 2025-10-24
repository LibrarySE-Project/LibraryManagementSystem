package librarySE;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class UserTest {

    private User normalUser;
    private User adminUser;

    /**
     * Runs before each test.
     * Creates a normal user and an admin user for testing.
     */
    @BeforeEach
    void setUp() {
        normalUser = new User("adm", Role.USER, "alicePass");
        adminUser = new User("boss", Role.ADMIN, "adminPass");
    }

    /**
     * Runs after each test.
     * Clears user objects.
     */
    @AfterEach
    void tearDown() {
        normalUser = null;
        adminUser = null;
    }

    /**
     * Test getters: username and role are set correctly.
     */
    @Test
    void testGetters() {
        assertEquals("adm", normalUser.getUsername(), "Username should match");
        assertEquals("boss", adminUser.getUsername(), "Username should match");
        assertEquals(Role.USER, normalUser.getRole(), "Role should be USER");
        assertEquals(Role.ADMIN, adminUser.getRole(), "Role should be ADMIN");
    }

    /**
     * Test isAdmin(): returns true only for admin users.
     */
    @Test
    void testIsAdmin() {
        assertFalse(normalUser.isAdmin(), "Normal user should not be admin");
        assertTrue(adminUser.isAdmin(), "Admin user should be admin");
    }

    /**
     * Test toString(): returns formatted string "username (ROLE)".
     */
    @Test
    void testToString() {
        assertEquals("adm (USER)", normalUser.toString(), "toString format mismatch");
        assertEquals("boss (ADMIN)", adminUser.toString(), "toString format mismatch");
    }

    /**
     * Test checkPassword(): verifies correct and incorrect passwords.
     */
    @Test
    void testCheckPassword() {
        // Correct passwords
        assertTrue(normalUser.checkPassword("alicePass"), "Correct password should pass");
        assertTrue(adminUser.checkPassword("adminPass"), "Correct password should pass");

        // Incorrect passwords
        assertFalse(normalUser.checkPassword("wrongPass"), "Wrong password should fail");
        assertFalse(adminUser.checkPassword("12345"), "Wrong password should fail");
    }

    /**
     * Test changePassword(): ensures password update works correctly.
     */
    @Test
    void testChangePassword() {
        // Change password with correct old password
        assertTrue(normalUser.changePassword("alicePass", "newPass123"), "Should change password if old one is correct");
        // Verify the new password works
        assertTrue(normalUser.checkPassword("newPass123"), "New password should match");
        // Old password should no longer work
        assertFalse(normalUser.checkPassword("alicePass"), "Old password should no longer be valid");

        // Try changing password with incorrect old password
        assertFalse(adminUser.changePassword("wrongOld", "newAdminPass"), "Should not change password if old one is incorrect");
        // Password should remain unchanged
        assertTrue(adminUser.checkPassword("adminPass"), "Old password should still be valid");
    }

    /**
     * Check that two users with same username but different roles are distinct.
     */
    @Test
    void testUsersAreDistinct() {
        User duplicateUser = new User("adm", Role.ADMIN, "otherPass");
        assertNotSame(normalUser, duplicateUser, "Should not refer to same object");
        assertNotEquals(normalUser.getRole(), duplicateUser.getRole(), "Roles should differ");
        assertFalse(normalUser.checkPassword("otherPass"), "Password hashes should differ");
    }

    /**
     * Creating a user with null username should throw an exception or behave predictably.
     */
    @Test
    void testNullUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            new User(null, Role.USER, "somePass");
        }, "Null username should throw exception");
    }

    /**
     * Creating a user with null password should throw an exception or fail gracefully.
     */
    @Test
    void testNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            new User("john", Role.USER, null);
        }, "Null password should throw exception");
    }
    /**
     * Empty password should still hash successfully (but not recommended).
     */
    @Test
    void testEmptyPassword() {
        User u = new User("emptyGuy", Role.USER, "");
        assertTrue(u.checkPassword(""), "Empty string password should match itself");
        assertFalse(u.checkPassword(" "), "Space should not match empty password");
    }

    /**
     * Edge test: Verify that hashing the same password twice gives same hash (deterministic).
     */
    @Test
    void testHashConsistency() {
        User u1 = new User("x1", Role.USER, "repeatPass");
        User u2 = new User("x2", Role.USER, "repeatPass");
        // Hashes should be same because same password & algorithm
        assertTrue(u1.checkPassword("repeatPass"));
        assertTrue(u2.checkPassword("repeatPass"));
    }

    /**
     * Verify different passwords produce different hashes.
     */
    @Test
    void testDifferentPasswordsProduceDifferentHashes() {
        User u1 = new User("a1", Role.USER, "1234");
        User u2 = new User("a2", Role.USER, "12345");
        assertFalse(u1.checkPassword("12345"), "Hash mismatch expected");
        assertFalse(u2.checkPassword("1234"), "Hash mismatch expected");
    }

    /**
     * Attempting to change password with null values.
     */
    @Test
    void testChangePasswordWithNulls() {
        assertFalse(normalUser.changePassword(null, "newOne"), "Null old password should fail");
        assertFalse(normalUser.changePassword("alicePass", null), "Null new password should fail");
    }
}

