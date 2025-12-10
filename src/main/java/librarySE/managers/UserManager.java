package librarySE.managers;

import librarySE.repo.UserRepository;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manager responsible for all user-related operations within the library system.
 * <p>
 * This class follows the <b>Singleton</b> design pattern to ensure that only one
 * instance manages user data throughout the system. It provides thread-safe access
 * to user collections, lookup utilities, persistence control, and user removal logic.
 * </p>
 *
 * <h2>Main Responsibilities:</h2>
 * <ul>
 *     <li>Loading and saving users via {@link UserRepository}</li>
 *     <li>Registering new users</li>
 *     <li>Searching users by email or username</li>
 *     <li>Ensuring business constraints when unregistering a user</li>
 * </ul>
 *
 * <h2>Thread Safety:</h2>
 * Uses {@link CopyOnWriteArrayList} to ensure thread-safe iteration and modification.
 *
 * <h2>Related Use Cases:</h2>
 * <ul>
 *     <li>US1.x — Admin user management</li>
 *     <li>US4.2 — Unregister user constraints</li>
 *     <li>Notification / Borrowing features requiring email lookup</li>
 * </ul>
 *
 * @author Malak
 */
public class UserManager {

    /** Singleton instance of UserManager. */
    private static UserManager instance;

    /** Thread-safe list of all users currently known to the system. */
    private final CopyOnWriteArrayList<User> users;

    /** Repository used for persistence operations. */
    private final UserRepository repo;

    /**
     * Private constructor to prevent external instantiation. Loads all users
     * from the provided repository.
     *
     * @param repo the repository responsible for loading and saving user data
     * @throws NullPointerException if {@code repo} is null
     */
    private UserManager(UserRepository repo) {
        this.repo = Objects.requireNonNull(repo, "UserRepository cannot be null");
        this.users = new CopyOnWriteArrayList<>(repo.loadAll());
    }

    /**
     * Initializes the singleton {@code UserManager} instance.
     * Must be called exactly once during system setup.
     *
     * @param repo the repository used for persistence
     * @return the created singleton instance
     */
    public static synchronized UserManager init(UserRepository repo) {
        if (instance == null) instance = new UserManager(repo);
        return instance;
    }

    /**
     * Returns the active {@code UserManager} instance.
     *
     * @return the singleton UserManager
     * @throws IllegalStateException if {@link #init(UserRepository)} was not called beforehand
     */
    public static synchronized UserManager getInstance() {
        if (instance == null)
            throw new IllegalStateException("UserManager not initialized.");
        return instance;
    }

    /**
     * Looks up a user using their email address. Matching is case-insensitive.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the user if found, otherwise empty
     */
    public Optional<User> findUserByEmail(String email) {
        if (email == null || email.isBlank())
            return Optional.empty();

        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    /**
     * Finds a user by username. Matching is case-sensitive.
     *
     * @param username the username to locate
     * @return an {@link Optional} user if found; empty otherwise
     */
    public Optional<User> findUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return users.stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst();
    }

    /**
     * Registers a new user and persists the updated user list.
     *
     * @param user the user to add
     * @throws IllegalArgumentException if the user object is null
     */
    public void addUser(User user) {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null.");
        users.add(user);
        repo.saveAll(users);
    }

    /**
     * Returns an immutable snapshot of all users currently stored.
     *
     * @return an unmodifiable list of users
     */
    public List<User> getAllUsers() {
        return List.copyOf(users);
    }

    /**
     * Persists the current user list to the repository.
     */
    public void saveAll() {
        repo.saveAll(users);
    }

    // ============================================================
    // User Removal (US4.2)
    // ============================================================

    /**
     * Removes a user from the system, enforcing all business constraints:
     * <ul>
     *     <li>User must not have the ADMIN role.</li>
     *     <li>User must have no unpaid fines.</li>
     *     <li>User must have no active (unreturned) borrow records.</li>
     * </ul>
     *
     * @param user the user to remove
     * @throws IllegalArgumentException if the user reference is null
     * @throws IllegalStateException if any rule is violated
     */
    public void unregisterUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        // Prevent deleting admin accounts
        if (user.isAdmin()) {
            throw new IllegalStateException("Cannot unregister admin account.");
        }

        // User must not have unpaid fines
        if (user.hasOutstandingFine()) {
            throw new IllegalStateException("User has unpaid fines and cannot be unregistered.");
        }

        // User must not have active borrow records
        BorrowManager bm = BorrowManager.getInstance();
        boolean hasActiveLoans = bm.getBorrowRecordsForUser(user).stream()
                .anyMatch(record -> !record.isReturned());

        if (hasActiveLoans) {
            throw new IllegalStateException("User has active loans and cannot be unregistered.");
        }

        // Remove and persist
        users.removeIf(u -> u.equals(user));
        repo.saveAll(users);
    }
}
