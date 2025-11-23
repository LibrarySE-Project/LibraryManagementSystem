package librarySE.repo;

import com.google.gson.reflect.TypeToken;
import librarySE.managers.User;
import librarySE.utils.FileUtils;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

/**
 * File-based implementation of {@link UserRepository} that persists user data in JSON format.
 * <p>
 * This class handles reading and writing {@link User} objects to a file named
 * <code>users.json</code> located in the application's data directory.
 * </p>
 *
 * <h2>Storage Format:</h2>
 * <ul>
 *     <li>Uses Gson for JSON serialization and deserialization.</li>
 *     <li>File path: {@code library_data/users.json}</li>
 *     <li>Ensures safe read/write operations using {@link FileUtils} helper methods.</li>
 * </ul>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *     <li>Load all users when the system starts.</li>
 *     <li>Persist new or updated users when changes occur.</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * UserRepository repo = new FileUserRepository();
 *
 * // Load all users
 * List<User> users = repo.loadAll();
 *
 * // Add a new user
 * users.add(new User("malak@najah.edu", "Malak"));
 *
 * // Save back to file
 * repo.saveAll(users);
 * }</pre>
 *
 * @author Malak
 * @see librarySEv2.repo.UserRepository
 * @see librarySEv2.managers.User
 * @see librarySEv2.utils.FileUtils
 */
public class FileUserRepository implements UserRepository {

    /** Path to the JSON file where users are stored. */
    private static final Path FILE = FileUtils.dataFile("users.json");

    /**
     * Loads all {@link User} objects from the JSON file.
     * <p>
     * If the file is missing or empty, returns an empty list instead of {@code null}.
     * </p>
     *
     * @return list of users; never {@code null}, but may be empty
     */
    @Override
    public List<User> loadAll() {
        Type type = new TypeToken<List<User>>() {}.getType();
        List<User> list = FileUtils.readJson(FILE, type, new ArrayList<>());
        return (list == null) ? new ArrayList<>() : list;
    }

    /**
     * Saves the provided list of {@link User} objects into the JSON file.
     * <p>
     * Overwrites existing data with the new list.
     * </p>
     *
     * @param users the list of users to save; must not be {@code null}
     */
    @Override
    public void saveAll(List<User> users) {
        FileUtils.writeJson(FILE, users);
    }
}
