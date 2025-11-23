package librarySE.repo;


import librarySE.managers.User;
import java.util.List;

/**
 * Defines the contract for user persistence operations within the library system.
 * <p>
 * This interface abstracts how {@link User} objects are loaded from
 * and saved to persistent storage (e.g., JSON files, databases, etc.).
 * </p>
 *
 * <p>Typical implementations include:</p>
 * <ul>
 *     <li>{@code FileUserRepository} – stores users in a JSON file.</li>
 *     <li>{@code DatabaseUserRepository} – stores users in a database (future expansion).</li>
 * </ul>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *     <li>Provide a unified way to retrieve all registered users.</li>
 *     <li>Save user data after additions, deletions, or updates.</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * UserRepository repo = new FileUserRepository();
 * List<User> users = repo.loadAll();
 * users.add(new User("malak@najah.edu", "Malak"));
 * repo.saveAll(users);
 * }</pre>
 *
 * @author Eman
 * @see librarySEv2.repo.FileUserRepository
 * @see librarySEv2.managers.User
 */
public interface UserRepository {

    /**
     * Loads all {@link User} objects from persistent storage.
     *
     * @return a list of all users; never {@code null}, but may be empty
     */
    List<User> loadAll();

    /**
     * Saves the given list of {@link User} objects to persistent storage.
     * <p>
     * This operation typically overwrites existing data.
     * </p>
     *
     * @param users the list of users to save; must not be {@code null}
     */
    void saveAll(List<User> users);
}
