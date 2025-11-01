package librarySE;

/**
 * Represents the administrator of the library system.
 * <p>
 * The {@code Admin} class follows the Singleton pattern — only one admin instance
 * can exist at a time. It extends {@link User} with the {@link Role#ADMIN} role.
 * </p>
 * <p>
 * Responsibilities of the admin include:
 * <ul>
 *   <li>Managing users (via {@link UserManager})</li>
 *   <li>Sending notifications or reminders to users</li>
 *   <li>Logging in and out of the system</li>
 * </ul>
 * </p>
 * <p>
 * Note: The admin must be explicitly initialized using {@link #initialize(String, String, String)}
 * before calling {@link #getInstance()}.
 * </p>
 * 
 * @author Eman
 * @see User
 * @see Role
 */
public class Admin extends User {

    /** The single instance of Admin (Singleton). */
    private static Admin instance;

    /** Tracks whether the admin is currently logged in. */
    private boolean loggedIn;

    /**
     * Private constructor to enforce the Singleton pattern.
     * <p>
     * Initializes an admin account with the provided username, password, and email.
     * </p>
     *
     * @param username the admin's username (non-null)
     * @param password the admin's password (non-null)
     * @param email    the admin's email (must be a valid format)
     */
    private Admin(String username, String password, String email) {
        super(username, Role.ADMIN, password, email);
        this.loggedIn = false;
    }

    /**
     * Initializes the singleton Admin instance.
     * <p>
     * This method must be called exactly once before calling {@link #getInstance()}.
     * Attempting to initialize more than once will throw an exception.
     * </p>
     *
     * @param username the admin's username
     * @param password the admin's password
     * @param email    the admin's email
     * @throws IllegalStateException if the admin has already been initialized
     */
    public static void initialize(String username, String password, String email) {
        if (instance != null) {
            throw new IllegalStateException("Admin already initialized");
        }
        instance = new Admin(username, password, email);
    }

    /**
     * Returns the singleton Admin instance.
     *
     * @return the single Admin instance
     * @throws IllegalStateException if the admin has not yet been initialized
     */
    public static Admin getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Admin not initialized yet");
        }
        return instance;
    }

    /**
     * Attempts to log in the admin using the provided credentials.
     *
     * @param enteredUser the entered username
     * @param enteredPass the entered password
     * @return {@code true} if login succeeds, {@code false} otherwise
     */
    public boolean login(String enteredUser, String enteredPass) {
        loggedIn = getUsername().equals(enteredUser) && checkPassword(enteredPass);
        return loggedIn;
    }

    /**
     * Logs out the admin, ending the current session.
     */
    public void logout() {
        loggedIn = false;
    }

    /**
     * Checks whether the admin is currently logged in.
     *
     * @return {@code true} if the admin is logged in, {@code false} otherwise
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }
}