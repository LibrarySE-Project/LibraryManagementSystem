package librarySE.managers;

import librarySE.repo.UserRepository;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages all user-related operations in the library system.
 * <p>
 * This class is implemented as a <b>Singleton</b> to ensure there is only
 * one instance managing users across the system. It provides methods for
 * retrieving users, searching by email, and saving updates to persistent storage.
 * </p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *     <li>Load and store user data using {@link UserRepository}</li>
 *     <li>Find users by email (used by {@link BorrowManager} for notifications)</li>
 *     <li>Provide thread-safe access to the user list</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * UserManager userManager = UserManager.init(userRepo);
 *
 * // Find a user by email
 * Optional<User> user = userManager.findUserByEmail("student@najah.edu");
 * user.ifPresent(u -> System.out.println("Found user: " + u.getUsername()));
 * }</pre>
 * 
 * @author Malak
 * @see BorrowManager
 * @see UserRepository
 */
public class UserManager {

    /** Singleton instance of UserManager */
    private static UserManager instance;

    /** Thread-safe list of all users in the system */
    private final CopyOnWriteArrayList<User> users;

    /** Repository for persistent storage of users */
    private final UserRepository repo;

    /**
     * Private constructor to enforce Singleton pattern.
     *
     * @param repo repository responsible for loading and saving users
     */
    private UserManager(UserRepository repo) {
        this.repo = Objects.requireNonNull(repo, "UserRepository cannot be null");
        this.users = new CopyOnWriteArrayList<>(repo.loadAll());
    }

    /**
     * Initializes the singleton instance of {@code UserManager}.
     *
     * @param repo the repository to use for persistence
     * @return the initialized {@link UserManager} instance
     */
    public static synchronized UserManager init(UserRepository repo) {
        if (instance == null) instance = new UserManager(repo);
        return instance;
    }

    /**
     * Returns the active singleton instance.
     *
     * @return the {@link UserManager} instance
     * @throws IllegalStateException if not initialized yet
     */
    public static synchronized UserManager getInstance() {
        if (instance == null)
            throw new IllegalStateException("UserManager not initialized.");
        return instance;
    }
    /**
     * Finds a user by their email address (case-insensitive).
     * <p>
     * Used mainly by {@link BorrowManager} when sending email notifications
     * to users waiting for specific items.
     * </p>
     *
     * @param email the email address to search for; must not be {@code null} or blank
     * @return an {@link Optional} containing the user if found; otherwise empty
     */
    public Optional<User> findUserByEmail(String email) {
        if (email == null || email.isBlank())
            return Optional.empty();

        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    /**
     * Adds a new user to the system.
     *
     * @param user the user to add
     * @throws IllegalArgumentException if user is null
     */
    public void addUser(User user) {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null.");
        users.add(user);
        repo.saveAll(users);
    }

    /**
     * Returns all users currently registered in the system.
     *
     * @return unmodifiable list of users
     */
    public List<User> getAllUsers() {
        return List.copyOf(users);
    }

    /**
     * Saves all users to persistent storage.
     */
    public void saveAll() {
        repo.saveAll(users);
    }
}

