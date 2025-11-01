package librarySE;

package librarySE;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Singleton class that manages users in the library system.
 * <p>
 * Responsibilities include:
 * <ul>
 *     <li>Adding new users (admin only)</li>
 *     <li>Deleting or unregistering users (admin only)</li>
 *     <li>Searching users by username or email (case-insensitive)</li>
 *     <li>Maintaining the internal user list</li>
 *     <li>Interacting with {@link BorrowRecord} to enforce rules for unregistering users</li>
 * </ul>
 * <p>
 * Thread-safety:
 * <ul>
 *     <li>The internal user list is a {@link CopyOnWriteArrayList}, allowing concurrent reads and safe iteration.</li>
 *     <li>Methods modifying the list perform atomic checks to prevent inconsistent state.</li>
 * </ul>
 * <p>
 * This class follows the Singleton design pattern to ensure that only one instance exists
 * in the application.
 * </p>
 * 
 * @author Malak
 * @see User
 * @see BorrowRecord
 * @see Admin
 */
public class UserManager {

    /** The singleton instance of {@code UserManager}. */
    private static UserManager instance;

    /** Thread-safe internal list containing all registered users. */
    private final CopyOnWriteArrayList<User> users;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the internal users list as a {@link CopyOnWriteArrayList}.
     */
    private UserManager() {
        this.users = new CopyOnWriteArrayList<>();
    }

    /**
     * Returns the singleton instance of {@code UserManager}.
     * <p>
     * Thread-safe creation using synchronized method ensures only one instance exists.
     * </p>
     *
     * @return the single instance of {@code UserManager}
     */
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /**
     * Registers a new user in the system (admin only).
     * <p>
     * Performs null checks, verifies that the invoking admin has privileges, and
     * ensures that the user does not already exist.
     * </p>
     *
     * @param newUser the {@link User} to register; must not be null
     * @param admin the {@link Admin} performing the operation; must not be null
     * @throws IllegalArgumentException if {@code newUser} or {@code admin} is null,
     *                                  if {@code admin} does not have admin privileges,
     *                                  or if the user already exists
     */
    public void addUser(User newUser, Admin admin) {
        if (newUser == null || admin == null)
            throw new IllegalArgumentException("User and admin cannot be null.");
        if (!admin.isAdmin())
            throw new IllegalArgumentException("Only admin can add users.");
        if (users.contains(newUser))
            throw new IllegalArgumentException("User already exists.");
        users.add(newUser);
    }

    /**
     * Unregisters a user from the system (admin only).
     * <p>
     * Before removal, the method checks:
     * <ul>
     *     <li>Whether the admin performing the action has privileges</li>
     *     <li>Whether the target user exists in the system</li>
     *     <li>Whether the user has active borrowings or unpaid fines</li>
     * </ul>
     * If the user has active loans or unpaid fines, the user cannot be unregistered.
     * Any borrow records associated with the user are removed upon successful unregistration.
     * </p>
     *
     * @param targetUser the {@link User} to remove; must not be null
     * @param admin the {@link Admin} performing the operation; must not be null
     * @param borrowRecords the list of {@link BorrowRecord} objects to check for active loans; must not be null
     * @return a status message describing the outcome of the operation
     */
    public String unregisterUser(User targetUser, Admin admin, List<BorrowRecord> borrowRecords) {
        if (targetUser == null || admin == null)
            return "Cannot unregister: user or admin is null.";
        if (!admin.isAdmin())
            return "Operation denied: only admin can unregister users.";
        if (!users.contains(targetUser))
            return "User not found in the system.";

        boolean hasActiveLoans = borrowRecords.stream()
                .anyMatch(r -> r.getUser().equals(targetUser) && !r.isReturned());
        boolean hasUnpaidFines = targetUser.getFineBalance().compareTo(BigDecimal.ZERO) > 0;

        if (hasActiveLoans || hasUnpaidFines)
            return "Cannot unregister user: active loans or unpaid fines exist.";

        borrowRecords.removeIf(r -> r.getUser().equals(targetUser));
        users.remove(targetUser);

        return "User unregistered successfully.";
    }

    /**
     * Searches for users by keyword (case-insensitive) in username or email.
     * <p>
     * Returns all users whose username or email contains the trimmed, lower-cased keyword.
     * Uses {@link java.util.stream.Stream} to filter results.
     * </p>
     *
     * @param keyword the keyword to search for; must not be null
     * @return a list of matching {@link User} objects; empty list if none found
     * @throws IllegalArgumentException if {@code keyword} is null
     */
    public List<User> searchUsers(String keyword) {
        if (keyword == null) throw new IllegalArgumentException("Keyword cannot be null.");
        String search = keyword.trim().toLowerCase();
        return users.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(search)
                          || u.getEmail().toLowerCase().contains(search))
                .collect(Collectors.toList());
    }

    /**
     * Returns a copy of all registered users.
     * <p>
     * Returns a defensive copy to prevent external modification of the internal list.
     * </p>
     *
     * @return a new {@link List} containing all {@link User} objects
     */
    public List<User> getAllUsers() {
        return List.copyOf(users);
    }
}
