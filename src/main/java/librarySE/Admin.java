package librarySE;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents the administrator of the library system.
 * <p>
 * The {@code Admin} class follows the Singleton pattern — only one admin instance
 * can exist at a time. It supports user management, overdue notifications, and
 * interaction with the {@link Library} instance for administrative tasks.
 * </p>
 *
 * <p>
 * The admin can send reminders to users, unregister users with no active loans or
 * unpaid fines, and manage notification strategies (mock or real).
 * </p>
 *
 * @see User
 * @see Observer
 * @see Library
 * @see EmailNotifier
 * @see SmtpEmailObserver
 * @see SmtpEmailService
 * @author Eman
 */
public class Admin extends User {

    /** The single instance of Admin (Singleton). */
    private static Admin instance;

    /** Tracks whether the admin is currently logged in. */
    private boolean loggedIn;

    /** The observer responsible for sending notifications (mock or real). */
    private Observer notifier;
    
    /** Reference to the library system managed by this admin. */
    private Library library;
    
    /** Default constructor (used for testing or initialization). */
    public Admin() {
        super(); 
    }

    /**
     * Private constructor to enforce the Singleton pattern.
     * <p>
     * Initializes an admin account with the provided credentials, a notification
     * handler, and a reference to the library being managed.
     * </p>
     *
     * @param username the admin's username (non-null)
     * @param password the admin's password (non-null)
     * @param email    the admin's email (must follow valid format)
     * @param notifier the notification handler used for sending messages
     * @param library  the library instance managed by this admin
     */
    private Admin(String username, String password, String email, Observer notifier, Library library) {
        super(username, Role.ADMIN, password, email);
        this.loggedIn = false;
        this.notifier = notifier;
        this.library = library;
    }

    /**
     * Returns the single {@code Admin} instance, creating it if necessary.
     * <p>
     * The same admin instance is reused across the system. The first call
     * initializes it with the provided parameters.
     * </p>
     *
     * @param username the admin's username
     * @param password the admin's password
     * @param email    the admin's email
     * @param notifier the observer for notifications
     * @param library  the library managed by the admin
     * @return the singleton {@code Admin} instance
     */
    public static Admin getInstance(String username, String password, String email, Observer notifier, Library library) {
        if (instance == null) {
            instance = new Admin(username, password, email, notifier, library);
        }
        return instance;
    }

    /**
     * Updates the notifier used for sending user notifications.
     *
     * @param newNotifier the new {@link Observer} implementation to use
     * @throws IllegalArgumentException if {@code newNotifier} is {@code null}
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
     * @param enteredUser the entered username
     * @param enteredPass the entered password
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

    /** Logs out the admin, ending the current session. */
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
     * Sends reminder notifications to all users with overdue borrow records.
     * <p>
     * This method uses the currently assigned {@link Observer} implementation
     * (either mock or real SMTP) to deliver reminder messages.
     * </p>
     *
     * @param library the library instance containing user and borrowing data
     * @throws IllegalStateException if the admin is not logged in
     */
    public void sendReminders(Library library) {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Admin must be logged in to send reminders.");
        }
        library.sendReminders(notifier);
    }

    /**
     * Attempts to unregister (remove) a user from the library system.
     * <p>
     * The admin can only remove a user if:
     * <ul>
     *   <li>The user exists in the library's user list.</li>
     *   <li>The user has no active borrowed items.</li>
     *   <li>The user has no unpaid fines.</li>
     * </ul>
     * Any associated borrow records are also removed.
     * </p>
     *
     * @param targetUser the user to be unregistered
     * @return a message describing the result of the operation
     */
    public String unregisterUser(User targetUser) {
        if (targetUser == null) {
            return "Cannot unregister: user is null.";
        }

        List<User> users = library.getUsers();
        if (!users.contains(targetUser)) {
            return "User not found in the system.";
        }

        List<BorrowRecord> borrowRecords = library.getBorrowRecords();

        boolean hasActiveLoans = borrowRecords.stream()
                .anyMatch(r -> r.getUser().equals(targetUser) && !r.isReturned());

        boolean hasUnpaidFines = targetUser.getFineBalance().compareTo(BigDecimal.ZERO) > 0;

        if (hasActiveLoans || hasUnpaidFines) {
            return "Cannot unregister user: active loans or unpaid fines exist.";
        }

        borrowRecords.removeIf(r -> r.getUser().equals(targetUser));
        users.remove(targetUser);

        return "User unregistered successfully.";
    }

}

