package librarySE.managers;

import librarySE.core.LibraryItem;
import librarySE.managers.notifications.EmailNotifier;
import librarySE.repo.ItemRepository;
import librarySE.search.SearchStrategy;
import librarySE.utils.ValidationUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * The {@code ItemManager} class manages all {@link LibraryItem} objects in the system,
 * such as books, CDs, and journals.
 *
 * <p>This class is implemented as a <b>Singleton</b> to ensure that only one instance
 * exists throughout the application lifecycle.</p>
 *
 * <p>It provides administrative operations (e.g., add and delete items) and
 * general operations (e.g., search and retrieve all items).</p>
 *
 * <p>When a new item is added, all registered users are automatically notified
 * via email using the {@link EmailNotifier} utility.</p>
 *
 * <p>Data persistence is handled by the injected {@link ItemRepository} implementation,
 * while searching behavior is delegated to the injected {@link SearchStrategy} instance.
 * Thread safety is achieved using a {@link CopyOnWriteArrayList} for concurrent access.</p>
 *
 * <p><b>Design Patterns used:</b> Singleton, Strategy, and Observer (via Email notifications).</p>
 *
 * @author  
 */
public class ItemManager {

    /** The single instance of {@code ItemManager} (Singleton). */
    private static ItemManager instance;

    /** Thread-safe list that holds all library items. */
    private final CopyOnWriteArrayList<LibraryItem> items;

    /** Repository responsible for persisting and loading library items. */
    private final ItemRepository repo;

    /** Strategy used for performing search operations. */
    private SearchStrategy searchStrategy;

    /**
     * Private constructor used internally by {@link #init(ItemRepository, SearchStrategy)}.
     *
     * @param repo the {@link ItemRepository} implementation responsible for persistence
     * @param searchStrategy the {@link SearchStrategy} used for keyword-based searches
     */
    private ItemManager(ItemRepository repo, SearchStrategy searchStrategy) {
        this.items = new CopyOnWriteArrayList<>();
        this.repo = repo;
        this.searchStrategy = searchStrategy;
        this.items.addAll(repo.loadAll());
    }

    /**
     * Initializes the {@code ItemManager} singleton instance.
     * <p>
     * Must be called once at system startup, typically in the main initialization phase.
     * </p>
     *
     * @param repo the {@link ItemRepository} instance used to persist data
     * @param search the {@link SearchStrategy} instance used for search logic
     * @return the initialized singleton instance
     */
    public static synchronized ItemManager init(ItemRepository repo, SearchStrategy search) {
        if (instance == null)
            instance = new ItemManager(repo, search);
        return instance;
    }

    /**
     * Retrieves the current {@code ItemManager} singleton instance.
     *
     * @return the singleton instance
     * @throws IllegalStateException if {@link #init(ItemRepository, SearchStrategy)} has not been called first
     */
    public static synchronized ItemManager getInstance() {
        if (instance == null)
            throw new IllegalStateException("ItemManager not initialized");
        return instance;
    }

    /**
     * Updates the current {@link SearchStrategy} used for item searches.
     *
     * @param s the new {@link SearchStrategy} to be used
     */
    public void setSearchStrategy(SearchStrategy s) {
        this.searchStrategy = s;
    }

    /**
     * Adds a new {@link LibraryItem} to the system.
     * <p>
     * Only administrators are authorized to perform this operation.
     * Once the item is added, all registered users are notified via email.
     * </p>
     *
     * @param item  the {@link LibraryItem} to add; must not be {@code null}
     * @param admin the {@link Admin} performing the operation; must not be {@code null}
     * @throws IllegalArgumentException if {@code item} or {@code admin} is {@code null},
     *                                  or if the admin does not have sufficient privileges
     */
    public void addItem(LibraryItem item, Admin admin) {
    	ValidationUtils.requireNonEmpty(admin, "admin");
    	ValidationUtils.requireNonEmpty(item, "item");
        if (!admin.isAdmin())
            throw new IllegalArgumentException("Only admins can add items.");

        items.add(item);
        repo.saveAll(items);

        EmailNotifier emailNotifier = new EmailNotifier();
        try {
            UserManager userManager = UserManager.getInstance();
            for (User user : userManager.getAllUsers()) {
                String subject = "📚 New Library Item Added: " + item.getTitle();
                String message = String.format(
                        "Hello %s,\n\nA new %s titled \"%s\" has been added to the library!\n" +
                        "Visit your account to borrow it now.\n\nBest regards,\nLibrary Team",
                        user.getUsername(),
                        item.getClass().getSimpleName(),
                        item.getTitle()
                );
				emailNotifier.notify(user, subject, message);
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to notify users → " + e.getMessage());
        }
    }

    /**
     * Deletes an existing {@link LibraryItem} from the system.
     * <p>
     * Only administrators are authorized to perform this operation.
     * </p>
     *
     * @param item  the {@link LibraryItem} to remove; must not be {@code null}
     * @param admin the {@link Admin} performing the operation; must not be {@code null}
     * @throws IllegalArgumentException if {@code item} or {@code admin} is {@code null},
     *                                  or if the admin does not have sufficient privileges
     */
    public void deleteItem(LibraryItem item, Admin admin) {
    	ValidationUtils.requireNonEmpty(admin, "admin");
    	ValidationUtils.requireNonEmpty(item, "item");
        if (!admin.isAdmin())
            throw new IllegalArgumentException("Only admins can delete items.");

        items.remove(item);
        repo.saveAll(items);
    }

    /**
     * Searches for items that match the provided keyword.
     * <p>
     * The search is case-insensitive and uses the configured {@link SearchStrategy}.
     * </p>
     *
     * @param keyword the search keyword; must not be {@code null}
     * @return a list of {@link LibraryItem} objects matching the keyword;
     *         returns an empty list if no results are found
     * @throws IllegalArgumentException if {@code keyword} is {@code null}
     */
    public List<LibraryItem> searchItems(String keyword) {
        if (keyword == null)
            throw new IllegalArgumentException("Keyword cannot be null.");
        String k = keyword.trim().toLowerCase();

        return items.stream()
                .filter(i -> searchStrategy.matches(i, k))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all {@link LibraryItem} objects currently available in the system.
     * <p>
     * Returns an unmodifiable copy to prevent external modification of the internal list.
     * </p>
     *
     * @return an immutable list containing all library items
     */
    public List<LibraryItem> getAllItems() {
        return List.copyOf(items);
    }
}
