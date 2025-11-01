package librarySE;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Singleton class that manages all library items in the system.
 * <p>
 * Responsibilities include:
 * <ul>
 *     <li>Adding new items (admin only)</li>
 *     <li>Deleting existing items (admin only)</li>
 *     <li>Searching items by keyword (delegates matching to each item)</li>
 *     <li>Retrieving all items</li>
 * </ul>
 * <p>
 * The internal list of items is thread-safe using {@link Collections#synchronizedList(List)}.
 * All access to the list that involves iteration is synchronized to prevent
 * {@link ConcurrentModificationException}.
 * </p>
 * 
 * <p>
 * The class follows the Singleton design pattern to ensure that only one instance
 * of {@code ItemManager} exists in the application.
 * </p>
 * 
 * @author Malak
 */
public class ItemManager {

    /** The singleton instance of {@code ItemManager}. */
    private static ItemManager instance;

    /** Thread-safe list containing all library items in the system. */
    private final List<LibraryItem> items;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the internal items list as a synchronized list.
     */
    private ItemManager() {
        this.items = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Returns the singleton instance of {@code ItemManager}.
     * <p>
     * If no instance exists, a new one is created in a thread-safe manner.
     * </p>
     *
     * @return the single instance of {@code ItemManager}
     */
    public static synchronized ItemManager getInstance() {
        if (instance == null) {
            instance = new ItemManager();
        }
        return instance;
    }

    /**
     * Adds a library item to the system.
     * <p>
     * Only users with administrative privileges can add items.
     * Null checks are performed for both {@code item} and {@code user}.
     * </p>
     *
     * @param item the {@link LibraryItem} to add; must not be null
     * @param user the {@link Admin} performing the operation; must not be null
     * @throws IllegalArgumentException if {@code item} or {@code user} is null,
     *                                  or if {@code user} does not have admin privileges
     */
    public void addItem(LibraryItem item, Admin user) {
        if (item == null || user == null)
            throw new IllegalArgumentException("Item and user cannot be null.");
        if (!user.isAdmin())
            throw new IllegalArgumentException("Only admins can add items.");
        items.add(item);
    }

    /**
     * Deletes a library item from the system.
     * <p>
     * Only users with administrative privileges can delete items.
     * Null checks are performed for both {@code item} and {@code user}.
     * </p>
     *
     * @param item the {@link LibraryItem} to delete; must not be null
     * @param user the {@link Admin} performing the operation; must not be null
     * @throws IllegalArgumentException if {@code item} or {@code user} is null,
     *                                  or if {@code user} does not have admin privileges
     */
    public void deleteItem(LibraryItem item, Admin user) {
        if (item == null || user == null)
            throw new IllegalArgumentException("Item and user cannot be null.");
        if (!user.isAdmin())
            throw new IllegalArgumentException("Only admins can delete items.");
        items.remove(item);
    }

    /**
     * Searches for library items containing the specified keyword.
     * <p>
     * Matching is case-insensitive. The keyword is trimmed before matching.
     * The actual matching logic is delegated to each {@link LibraryItem}'s
     * {@link LibraryItem#matchesKeyword(String)} method.
     * </p>
     *
     * <p>
     * Iteration over the internal list is synchronized to ensure thread-safety.
     * </p>
     *
     * @param keyword the keyword to search for; must not be null
     * @return a list of {@link LibraryItem} objects whose fields match the keyword;
     *         returns an empty list if no matches are found
     * @throws IllegalArgumentException if {@code keyword} is null
     */
    public List<LibraryItem> searchItems(String keyword) {
        if (keyword == null)
            throw new IllegalArgumentException("Keyword cannot be null.");
        String search = keyword.trim().toLowerCase();

        synchronized(items) {
            return items.stream()
                        .filter(item -> item.matchesKeyword(search))
                        .collect(Collectors.toList());
        }
    }

    /**
     * Returns a list of all library items in the system.
     * <p>
     * Returns a defensive copy to prevent external modification of the internal list.
     * Iteration is synchronized for thread safety.
     * </p>
     *
     * @return a new list containing all {@link LibraryItem} objects in the system;
     *         returns an empty list if no items exist
     */
    public List<LibraryItem> getAllItems() {
        synchronized(items) {
            return List.copyOf(items);
        }
    }
}
