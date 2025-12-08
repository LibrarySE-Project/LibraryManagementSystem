package librarySE.repo;

import com.google.gson.reflect.TypeToken;
import librarySE.core.WaitlistEntry;
import librarySE.utils.FileUtils;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * File-based implementation of {@link WaitlistRepository} that persists
 * {@link WaitlistEntry} objects in JSON format.
 * <p>
 * This repository stores the waitlist of users waiting for specific library items.
 * Data is saved in a file named <b>waitlist.json</b> under the application's data directory.
 * </p>
 *
 * <h2>Storage Details:</h2>
 * <ul>
 *     <li>File Path: {@code library_data/waitlist.json}</li>
 *     <li>Format: JSON (serialized using Gson)</li>
 *     <li>Utility: {@link FileUtils} handles reading/writing safely</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * WaitlistRepository repo = new FileWaitlistRepository();
 *
 * // Load current waitlist
 * List<WaitlistEntry> waitlist = repo.loadAll();
 *
 * // Add a new entry
 * waitlist.add(new WaitlistEntry("BOOK-123", "student@najah.edu", LocalDate.now()));
 *
 * // Save updates
 * repo.saveAll(waitlist);
 * }</pre>
 *
 * @author Eman
 */
public class FileWaitlistRepository implements WaitlistRepository {

    /** Path to the JSON file storing waitlist data. */
    private static final Path FILE = FileUtils.dataFile("waitlist.json");

    /**
     * Loads all {@link WaitlistEntry} objects from persistent storage.
     * <p>
     * If the file does not exist or is empty, returns an empty list instead of {@code null}.
     * </p>
     *
     * @return list of waitlist entries; never {@code null}, but may be empty
     */
    @Override
    public List<WaitlistEntry> loadAll() {
        Type type = new TypeToken<List<WaitlistEntry>>() {}.getType();
        List<WaitlistEntry> list = FileUtils.readJson(FILE, type, new ArrayList<>());
        return (list == null) ? new ArrayList<>() : list;
    }

    /**
     * Saves the provided list of {@link WaitlistEntry} objects to persistent storage.
     * <p>
     * Overwrites existing data with the new list.
     * </p>
     *
     * @param entries the list of waitlist entries to save; must not be {@code null}
     */
    @Override
    public void saveAll(List<WaitlistEntry> entries) {
        FileUtils.writeJson(FILE, entries);
    }
}
