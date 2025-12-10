package librarySE.repo;

import com.google.gson.reflect.TypeToken;
import librarySE.core.LibraryItem;
import librarySE.utils.FileUtils;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * File-based implementation of {@link ItemRepository} that persists {@link LibraryItem}
 * objects in JSON format.
 * <p>
 * This repository stores library items in a file named <code>items.json</code> under the
 * {@code library_data} directory, which is automatically created on first use.
 * </p>
 *
 * <h2>Storage Details</h2>
 * <ul>
 *     <li>JSON serialization using Gson</li>
 *     <li>File location: {@code library_data/items.json}</li>
 *     <li>Creates a timestamped backup copy before overwriting the existing file</li>
 * </ul>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Load all stored library items during system initialization</li>
 *     <li>Persist new or modified items when saving</li>
 * </ul>
 *
 * <h2>Behavior</h2>
 * <ul>
 *     <li>If the file does not exist, {@link #loadAll()} returns an empty list</li>
 *     <li>{@link #saveAll(List)} creates the file automatically if missing</li>
 *     <li>All write operations overwrite existing data and create a timestamped backup first</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * ItemRepository repo = new FileItemRepository();
 *
 * // Load data
 * List<LibraryItem> items = repo.loadAll();
 *
 * // Modify
 * items.add(new Book("123", "Clean Code", "Robert C. Martin"));
 *
 * // Persist
 * repo.saveAll(items);
 * }</pre>
 *
 * @author Malak
 */
public class FileItemRepository implements ItemRepository {

    /** Path of the JSON file where items are stored. */
    private static final Path FILE = FileUtils.dataFile("items.json");

    /**
     * Loads all stored {@link LibraryItem} objects from the JSON file.
     * <p>
     * If the file does not exist, this method returns an empty list instead of {@code null}.
     * </p>
     *
     * @return non-null list of library items; may be empty
     */
    @Override
    public List<LibraryItem> loadAll() {
        Type type = new TypeToken<List<LibraryItem>>() {}.getType();
        List<LibraryItem> list = FileUtils.readJson(FILE, type, new ArrayList<>());
        return (list == null) ? new ArrayList<>() : list;
    }

    /**
     * Saves the provided list of {@link LibraryItem} objects to the JSON file.
     * <p>
     * The existing data is overwritten and a timestamped backup is saved in
     * {@code library_data/backups}.
     * </p>
     *
     * @param items list of items to persist (must not be {@code null})
     */
    @Override
    public void saveAll(List<LibraryItem> items) {
        List<LibraryItem> snapshot = new ArrayList<>(items);
        FileUtils.writeJson(FILE, snapshot);
    }
}
