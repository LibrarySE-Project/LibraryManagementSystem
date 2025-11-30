package librarySE.repo;

import librarySE.core.LibraryItem;
import java.util.List;

/**
 * Defines the contract for managing persistence operations of {@link LibraryItem} objects.
 * <p>
 * This interface abstracts how library items (such as books, CDs, and journals)
 * are stored and retrieved from persistent storage (e.g., JSON files, databases, etc.).
 * </p>
 *
 * <p>Typical implementations include:</p>
 * <ul>
 *     <li>{@code FileItemRepository} – stores items in a JSON file.</li>
 *     <li>{@code DatabaseItemRepository} – stores items in a database (future expansion).</li>
 * </ul>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *     <li>Load all library items currently stored in the system.</li>
 *     <li>Persist updates to items such as new additions or modifications.</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * ItemRepository repo = new FileItemRepository();
 * List<LibraryItem> items = repo.loadAll();
 * items.add(new Book("123", "Clean Code", "Robert C. Martin"));
 * repo.saveAll(items);
 * }</pre>
 * 
 * @author Malak
 * @see librarySEv2.core.LibraryItem
 * @see librarySEv2.repo.FileItemRepository
 */
public interface ItemRepository {

    /**
     * Loads all {@link LibraryItem} objects from persistent storage.
     *
     * @return a list of all stored library items; never {@code null}, but may be empty
     */
    List<LibraryItem> loadAll();

    /**
     * Saves the given list of {@link LibraryItem} objects to persistent storage.
     * <p>
     * This operation typically overwrites the existing data with the provided list.
     * </p>
     *
     * @param items the list of library items to save; must not be {@code null}
     */
    void saveAll(List<LibraryItem> items);
}
