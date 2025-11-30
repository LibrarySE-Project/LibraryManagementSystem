package librarySE.managers;

import librarySE.managers.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    // USER should return correct description
    @Test
    void testUserDescription() {
        assertEquals("Can borrow & search", Role.USER.getDescription());
    }

    // ADMIN should return correct description
    @Test
    void testAdminDescription() {
        assertEquals("Can add/delete & manage users & items", Role.ADMIN.getDescription());
    }

    // enum values() should contain both roles
    @Test
    void testEnumValues() {
        Role[] roles = Role.values();
        assertEquals(2, roles.length);
    }

    // valueOf works correctly
    @Test
    void testEnumValueOf() {
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(Role.USER, Role.valueOf("USER"));
    }
}

