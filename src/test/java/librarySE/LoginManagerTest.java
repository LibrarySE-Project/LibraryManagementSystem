package librarySE;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
class LoginManagerTest {
	private Admin admin;
	private LoginManager loginManager;

	@BeforeEach
	void setUp() throws Exception {
		admin = Admin.getInstance("adminUser", "adminPass");
		admin.logout();
		loginManager = new LoginManager(admin);
	}

	@AfterEach
	void tearDown() throws Exception {
		admin.logout();
	}

	@Test
	void testLoginSuccess() {
		boolean result = loginManager.login("adminUser","adminPass");
	    assertTrue(result, "Login should succeed with correct username and password");
	    assertTrue(admin.isLoggedIn(), "Admin should be marked as logged in");
	}
	
	@Test
	void testLoginFailure() {
		boolean result = loginManager.login("wrongUser","wrongPass");
		assertFalse(result, "Login should fail with incorrect username and password");
	    assertFalse(admin.isLoggedIn(), "Admin should not be marked as logged in");
	}
	
	@Test
	void testLogout() {
		loginManager.login("adminUser","adminPass");
		loginManager.logout();
	    assertFalse(admin.isLoggedIn(), "Admin should be logged out after logout operation");
		
	}
	@Test
	void testIsLoggedInStatus() {
	    assertFalse(loginManager.isLoggedIn());
	    loginManager.login("adminUser", "adminPass");
	    assertTrue(loginManager.isLoggedIn());
	    loginManager.logout();
	    assertFalse(loginManager.isLoggedIn());
	}

}
