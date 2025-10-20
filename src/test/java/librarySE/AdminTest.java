package librarySE;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminTest {
	private Admin admin;
	
	@BeforeEach
	void setUp() throws Exception {
		admin = Admin.getInstance("adminUser", "adminPass");
        admin.logout();
	}

	@AfterEach
	void tearDown() throws Exception {
		admin.logout();
	}

	@Test
	void testSingleton() {
		Admin anotherAdmin = admin.getInstance("anotherUser", "anotherPass");
		assertSame(admin,anotherAdmin,"getInstance should always return the same Admin instance");	
	}
	
	@Test
	void testLoginSuccess() {
		boolean result = admin.login("adminUser","adminPass");
	    assertTrue(result, "Login should succeed with correct username and password");
	    assertTrue(admin.isLoggedIn(), "Admin should be marked as logged in");
	}
	
	@Test
	void testLoginFailure() {
		boolean result = admin.login("wrongUser","wrongPass");
		assertFalse(result, "Login should fail with incorrect username and password");
	    assertFalse(admin.isLoggedIn(), "Admin should not be marked as logged in");
	}
	
	@Test
	void testLogout() {
		admin.login("adminUser","adminPass");
		admin.logout();
	    assertFalse(admin.isLoggedIn(), "Admin should be logged out after logout operation");
	}
	
	@Test
	void testGetUsername() {
		String username = admin.getUsername();
		assertEquals("adminUser", username, "getUsername should return the correct username");
	}

}
