package librarySE.search;

import librarySE.core.LibraryItem;
import librarySE.utils.ValidationUtils;

/**
 * Default fallback implementation of {@link SearchStrategy}.
 * <p>
 * Performs a simple keyword search by converting the {@link LibraryItem}
 * to its string representation (via {@link LibraryItem#toString()})
 * and checking if the keyword appears anywhere in it (case-insensitive).
 * </p>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>As a fallback when no specific strategy (title, author, keyword) is provided.</li>
 *   <li>Useful for generic searches across all item types.</li>
 * </ul>
 *
 * <h3>Validation:</h3>
 * Uses {@link ValidationUtils} for consistent keyword validation.
 * </h3>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * SearchStrategy strategy = new DefaultSearchStrategy();
 * boolean found = strategy.matches(book, "Bloch");
 * }</pre>
 *
 * @see ValidationUtils
 * @see SearchStrategy
 * @see librarySE.core.LibraryItem
 *
 * @author Malak
 */
public class DefaultSearchStrategy implements SearchStrategy {

    /** {@inheritDoc} */
    @Override
    public boolean matches(LibraryItem item, String keyword) {
        if (item == null)
            throw new IllegalArgumentException("LibraryItem cannot be null");

        ValidationUtils.requireNonEmpty(keyword, "Keyword");

        return item.toString().toLowerCase().contains(keyword.toLowerCase());
    }
}
