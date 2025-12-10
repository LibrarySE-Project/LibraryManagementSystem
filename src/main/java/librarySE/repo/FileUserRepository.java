package librarySE.repo;

import com.google.gson.reflect.TypeToken;
import librarySE.managers.User;
import librarySE.utils.FileUtils;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * File-based implementation of {@link UserRepository} that persists user data in JSON format.
 * <p>
 * This repository saves and loads users from a JSON file named
 * <code>users.json</code> inside the {@code library_data} directory. The directory is created
 * automatically if it does not exist.
 * </p>
 *
 * <h2>Storage Details</h2>
 * <ul>
 *     <li>JSON serialization/deserialization using Gson</li>
 *     <li>File location: {@code library_data/users.json}</li>
 *     <li>A backup copy of the old file is automatically created before saving new data</li>
 * </ul>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Load all stored users during system initialization</li>
 *     <li>Persist new or modified users when saving</li>
 * </ul>
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>If the file does not exist, {@link #loadAll()} returns an empty list</li>
 *   <li>{@link #saveAll(List)} creates the file automatically if missing</li>
 *   <li>All write operations overwrite existing data and create a timestamped backup first</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * UserRepository repo = new FileUserRepository();
 *
 * // Load all users
 * List<User> users = repo.loadAll();
 *
 * // Add a user
 * users.add(new User("eman", Role.ADMIN, "123456", "eman@najah.edu"));
 *
 * // Save back to file
 * repo.saveAll(users);
 * }</pre>
 *
 * @author Malak
 */
public class FileUserRepository implements UserRepository {

    /** Path of the JSON file where users are saved. */
    private static final Path FILE = FileUtils.dataFile("users.json");

    /**
     * Loads all previously stored users from the JSON file.
     * <p>
     * If the file is missing, this method returns an empty list instead
     * of {@code null}. Errors during reading trigger a runtime exception.
     * </p>
     *
     * @return a list of stored users; never {@code null}, may be empty
     */
    @Override
    public List<User> loadAll() {
        Type type = new TypeToken<List<User>>() {}.getType();
        List<User> list = FileUtils.readJson(FILE, type, new ArrayList<>());
        return (list == null) ? new ArrayList<>() : list;
    }

    /**
     * Saves the given list of users into the JSON file.
     * <p>
     * The existing data is replaced, and a timestamped backup copy of the
     * previous file (if it existed) is stored under {@code library_data/backups}.
     * </p>
     *
     * @param users the list of users to persist (must not be {@code null})
     */
    @Override
    public void saveAll(List<User> users) {
        List<User> snapshot = new ArrayList<>(users);
        FileUtils.writeJson(FILE, snapshot);
    }
}
