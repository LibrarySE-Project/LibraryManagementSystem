package librarySE;

/**
 * Represents the administrator of the library system.
 * <p>
 * The {@code Admin} class follows the Singleton pattern — only one admin instance
 * can exist at a time. It supports sending notifications to users via either
 * a mock notifier ({@link EmailNotifier}) for testing or a real SMTP service
 * wrapped as an {@link Observer} for production.
 * </p>
 *
 * <p>
 * This version allows switching the notifier at runtime without creating a new Admin instance.
 * </p>
 *
 * @see User
 * @see Observer
 * @see EmailNotifier
 * @see SmtpEmailObserver
 * @see SmtpEmailService
 * @see Library
 * @author Eman
 */
public class Admin extends User {

    /** The single instance of Admin (Singleton). */
    private static Admin instance;

    /** Tracks whether the admin is currently logged in. */
    private boolean loggedIn;

    /** The observer responsible for sending notifications (mock or real). */
    private Observer notifier;

    /**
     * Private constructor to enforce the Singleton pattern.
     *
     * @param username the admin's username
     * @param password the admin's password
     * @param email    the admin's email
     * @param notifier the notification handler (mock or real)
     */
    private Admin(String username, String password, String email, Observer notifier) {
        super(username, Role.ADMIN, password, email);
        this.loggedIn = false;
        this.notifier = notifier;
    }

    /**
     * Returns the single {@code Admin} instance, creating it if necessary.
     *
     * @param username the admin's username
     * @param password the admin's password
     * @param email    the admin's email
     * @param notifier the notification handler (mock or real)
     * @return the singleton {@code Admin} instance
     */
    public static Admin getInstance(String username, String password, String email, Observer notifier) {
        if (instance == null) {
            instance = new Admin(username, password, email, notifier);
        }
        return instance;
    }

    /**
     * Allows switching the notifier (mock or real) at runtime.
     *
     * @param newNotifier the new Observer to use for notifications
     */
    public void setNotifier(Observer newNotifier) {
        if (newNotifier == null) {
            throw new IllegalArgumentException("Notifier cannot be null");
        }
        this.notifier = newNotifier;
    }

    /**
     * Attempts to log in the admin using the provided credentials.
     *
     * @param enteredUser the username entered
     * @param enteredPass the password entered
     * @return {@code true} if login succeeds, {@code false} otherwise
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
     * Logs out the admin, invalidating their session.
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

    /**
     * Sends reminder notifications to users with overdue items.
     * <p>
     * The notification mechanism depends on the currently set {@link Observer} implementation:
     * either a mock notifier ({@link EmailNotifier}) for testing or a real SMTP email
     * service wrapped in {@link SmtpEmailObserver}.
     * </p>
     *
     * @param library the library instance used to access users and overdue items
     * @throws IllegalStateException if the admin is not logged in
     */
    public void sendReminders(Library library) {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Admin must be logged in to send reminders.");
        }
        library.sendReminders(notifier);
    }
}
