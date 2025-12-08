package librarySE.search;


import librarySE.core.LibraryItem;

/**
 * Defines a flexible contract for implementing different search algorithms
 * on {@link LibraryItem} objects within the library system.
 * <p>
 * This interface is part of the <b>Strategy Pattern</b>, allowing the system
 * to dynamically choose how library items are searched based on different criteria
 * (e.g., by title, author, editor, or keyword).
 * </p>
 *
 * <h3>Purpose:</h3>
 * <ul>
 *   <li>Decouples the search logic from the {@link LibraryItem} domain model.</li>
 *   <li>Enables dynamic selection or switching of search behavior at runtime.</li>
 *   <li>Supports implementation of multiple, independent search strategies.</li>
 * </ul>
 *
 * <h3>Example Implementations:</h3>
 * <pre>{@code
 * public class TitleSearchStrategy implements SearchStrategy {
 *     @Override
 *     public boolean matches(LibraryItem item, String keyword) {
 *         return item.getTitle().toLowerCase().contains(keyword.toLowerCase());
 *     }
 * }
 *
 * public class AuthorSearchStrategy implements SearchStrategy {
 *     @Override
 *     public boolean matches(LibraryItem item, String keyword) {
 *         if (item instanceof Book b)
 *             return b.getAuthor().toLowerCase().contains(keyword.toLowerCase());
 *         return false;
 *     }
 * }
 * }</pre>
 *
 * <h3>Typical Usage:</h3>
 * <pre>{@code
 * SearchStrategy strategy = new TitleSearchStrategy();
 * boolean found = strategy.matches(book, "java");
 * }</pre>
 *
 * @author Eman
 */
public interface SearchStrategy {

    /**
     * Determines whether the specified {@link LibraryItem} matches
     * the provided search keyword.
     * <p>
     * Implementations should typically perform case-insensitive comparisons
     * and may use one or more fields (such as title, author, or editor)
     * depending on the search logic.
     * </p>
     *
     * @param item    the {@link LibraryItem} to test (must not be {@code null})
     * @param keyword the search term to compare against (must not be {@code null} or empty)
     * @return {@code true} if the item satisfies the search condition, otherwise {@code false}
     * @throws IllegalArgumentException if {@code item} or {@code keyword} is {@code null}
     */
    boolean matches(LibraryItem item, String keyword);
}
