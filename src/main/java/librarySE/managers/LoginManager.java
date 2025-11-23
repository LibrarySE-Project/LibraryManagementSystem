package librarySE.managers;

import librarySE.utils.LoggerUtils;
import librarySE.utils.ValidationUtils;

/**
 * Handles authentication operations (login and logout) for the system administrator.
 * <p>
 * The {@code LoginManager} serves as a controller between the user interface and
 * the {@link Admin} singleton, providing safe and validated login functionality.
 * It also logs all login-related events for traceability and auditing.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Performs validation for username and password inputs via {@link ValidationUtils}.</li>
 *   <li>Records login and logout events using {@link LoggerUtils}.</li>
 *   <li>Supports checking the current admin session state.</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * Admin.initialize("admin", "secure123", "admin@library.ps");
 * LoginManager manager = new LoginManager(Admin.getInstance());
 *
 * if (manager.login("admin", "secure123")) {
 *     System.out.println("Login successful!");
 * }
 *
 * manager.logout();
 * }</pre>
 *
 * @author Malak
 * @see Admin
 * @see LoggerUtils
 * @see ValidationUtils
 */
public class LoginManager {

    /** The administrator instance controlled by this manager. */
    private final Admin admin;

    /**
     * Constructs a {@code LoginManager} for the given administrator.
     *
     * @param admin the {@link Admin} instance to manage
     */
    public LoginManager(Admin admin) {
        this.admin = admin;
    }

    /**
     * Attempts to log in the administrator using the given credentials.
     * <p>
     * All inputs are validated using {@link ValidationUtils}, and the attempt
     * is recorded in the log file {@code login_log.txt}.
     * </p>
     *
     * @param user the username provided
     * @param pass the password provided
     * @return {@code true} if login succeeds; {@code false} otherwise
     * @throws IllegalArgumentException if username or password is null or empty
     */
    public boolean login(String user, String pass) {
        ValidationUtils.requireNonEmpty(user, "Username");
        ValidationUtils.requireNonEmpty(pass, "Password");

        boolean ok = admin.login(user, pass);
        LoggerUtils.log("login_log.txt",
                (ok ? "SUCCESS " : "FAIL ") + "login attempt for admin=" + user);
        return ok;
    }

    /**
     * Logs out the administrator and records the event in {@code login_log.txt}.
     */
    public void logout() {
        admin.logout();
        LoggerUtils.log("login_log.txt", "Admin logged out: " + admin.getUsername());
    }

    /**
     * Returns whether the administrator is currently logged in.
     *
     * @return {@code true} if admin is logged in, otherwise {@code false}
     */
    public boolean isLoggedIn() {
        return admin.isLoggedIn();
    }
}
