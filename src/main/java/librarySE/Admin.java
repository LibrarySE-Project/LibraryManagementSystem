package librarySE;

/**
 * Represents the administrator of the library system.
 * <p>
 * The Admin class follows the Singleton pattern — meaning only one admin 
 * instance can exist in the system. The admin has special privileges, 
 * such as adding books to the library.
 * </p>
 * 
 * @author Eman
 */
public class Admin extends User {

    /** The single instance of the Admin (Singleton pattern). */
    private static Admin instance;

    /** Indicates whether the admin is currently logged in. */
    private boolean loggedIn;

    /**
     * Private constructor to prevent direct instantiation.
     * 
     * @param username the admin's username
     * @param password the admin's password
     */
    private Admin(String username, String password) {
        super(username, Role.ADMIN, password);
        this.loggedIn = false;
    }

    /**
     * Attempts to log in the admin using the provided credentials.
     * 
     * @param enteredUser the entered username
     * @param enteredPass the entered password
     * @return {@code true} if the credentials are correct and the admin is logged in, {@code false} otherwise
     */
    public boolean login(String enteredUser, String enteredPass) {
        if (getUsername().equals(enteredUser) && checkPassword(enteredPass)) {
            loggedIn = true;
        } else {
            loggedIn = false;
        }
        return loggedIn;
    }

    /**
     * Returns the single instance of the Admin.
     * <p>
     * If no instance exists yet, it creates one using the given username and password.
     * </p>
     * 
     * @param username the admin's username
     * @param password the admin's password
     * @return the single {@link Admin} instance
     */
    public static Admin getInstance(String username, String password) {
        if (instance == null) {
            instance = new Admin(username, password);
        }
        return instance;
    }

    /**
     * Logs out the admin by setting the login status to false.
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
