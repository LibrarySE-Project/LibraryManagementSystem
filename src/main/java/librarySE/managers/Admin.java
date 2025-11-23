package librarySE.managers;
/**
 * Represents the single administrative account within the library system.
 * <p>
 * This class extends {@link User} and implements the <b>Singleton pattern</b>,
 * ensuring that only <b>one Admin instance</b> exists in the entire system.
 * <br>
 * The administrator has elevated privileges compared to regular users,
 * such as adding, deleting, and managing users or library items.
 * </p>
 *
 * <h3>Design Pattern:</h3>
 * <ul>
 *   <li><b>Singleton:</b> Only one Admin object can ever exist.</li>
 *   <li>Thread-safe initialization using the {@code volatile} keyword.</li>
 * </ul>
 *
 * <h3>Authentication:</h3>
 * <p>
 * Provides login and logout functionality, maintaining a {@code loggedIn} flag
 * to indicate whether the admin is currently authenticated.
 * </p>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * // Initialize once (e.g., at system startup)
 * Admin.initialize("admin", "securePass123", "admin@library.ps");
 *
 * // Retrieve and log in
 * Admin admin = Admin.getInstance();
 * boolean success = admin.login("admin", "securePass123");
 *
 * if (success) {
 *     System.out.println("Welcome, " + admin.getUsername());
 * }
 *
 * admin.logout();
 * }</pre>
 *
 * @author Eman
 * @see User
 * @see Role
 */
public class Admin extends User {

    /** The single, globally accessible Admin instance (Singleton). */
    private static volatile Admin instance;

    /** Tracks whether the admin is currently logged in. */
    private boolean loggedIn;


    /**
     * Private constructor to enforce the Singleton pattern.
     * <p>
     * Initializes the admin with a username, password, and email.
     * The role is automatically set to {@link Role#ADMIN}.
     * </p>
     *
     * @param username the admin’s username
     * @param password the admin’s password (validated and hashed)
     * @param email    the admin’s valid email address
     */
    private Admin(String username, String password, String email) {
        super(username, Role.ADMIN, password, email);
        this.loggedIn = false;
    }

    /**
     * Initializes the singleton {@code Admin} instance.
     * <p>
     * Must be called exactly once before calling {@link #getInstance()}.
     * Subsequent calls will throw an {@link IllegalStateException}.
     * </p>
     *
     * @param username admin’s username
     * @param password admin’s password
     * @param email    admin’s email address
     * @throws IllegalStateException if an admin instance already exists
     */
    public static void initialize(String username, String password, String email) {
        if (instance != null)
            throw new IllegalStateException("Admin already initialized");
        instance = new Admin(username, password, email);
    }

    /**
     * Retrieves the global {@code Admin} instance.
     *
     * @return the initialized admin
     * @throws IllegalStateException if {@link #initialize(String, String, String)} was not called first
     */
    public static Admin getInstance() {
        if (instance == null)
            throw new IllegalStateException("Admin not initialized yet");
        return instance;
    }


    /**
     * Attempts to log the admin in using the provided credentials.
     * <p>
     * Compares the entered username and password with the stored values.
     * If successful, the {@code loggedIn} flag is set to {@code true}.
     * </p>
     *
     * @param enteredUser the username entered by the user
     * @param enteredPass the password entered by the user
     * @return {@code true} if login succeeds, {@code false} otherwise
     */
    public boolean login(String enteredUser, String enteredPass) {
        loggedIn = getUsername().equals(enteredUser) && checkPassword(enteredPass);
        return loggedIn;
    }

    /**
     * Logs the admin out of the system.
     * <p>
     * Simply resets the {@code loggedIn} flag to {@code false}.
     * </p>
     */
    public void logout() {
        loggedIn = false;
    }

    /**
     * Checks whether the admin is currently logged in.
     *
     * @return {@code true} if logged in, otherwise {@code false}
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }
}
