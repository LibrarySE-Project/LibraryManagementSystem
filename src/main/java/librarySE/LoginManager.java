package librarySE;

/**
 * Manages the login and logout operations for the library system's administrator.
 * <p>
 * This class acts as an interface to perform login-related actions on the
 * {@link Admin} instance, including logging in, logging out, and checking
 * the current login status.
 * </p>
 * 
 * @author Eman
 */
public class LoginManager {

    /** The administrator instance that this manager controls. */
    private Admin admin;

    /**
     * Constructs a LoginManager with the specified admin instance.
     * 
     * @param admin the {@link Admin} instance to manage
     */
    public LoginManager(Admin admin) {
        this.admin = admin;
    }

    /**
     * Attempts to log in the administrator using the provided credentials.
     * <p>
     * Performs validation to ensure that the username and password are not null or empty.
     * If either is invalid, an {@link IllegalArgumentException} is thrown.
     * </p>
     * 
     * @param user the username to log in; must not be null or empty
     * @param userPass the password to log in; must not be null or empty
     * @return {@code true} if the login is successful, {@code false} otherwise
     * @throws IllegalArgumentException if {@code user} or {@code userPass} is null or empty
     */
    public boolean login(String user, String userPass) {
    	  if (user == null || user.trim().isEmpty()) {
    	        throw new IllegalArgumentException("Username cannot be null or empty");
    	    }
    	    if (userPass == null || userPass.trim().isEmpty()) {
    	        throw new IllegalArgumentException("Password cannot be null or empty");
    	    }
        return admin.login(user, userPass);
    }

    /**
     * Logs out the administrator by updating their login status.
     */
    public void logout() {
        admin.logout();
    }

    /**
     * Checks whether the administrator is currently logged in.
     * 
     * @return {@code true} if the admin is logged in, {@code false} otherwise
     */
    public boolean isLoggedIn() {
        return admin.isLoggedIn();
    }
}