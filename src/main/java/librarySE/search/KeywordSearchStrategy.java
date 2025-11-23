package librarySE.search;

import librarySE.core.LibraryItem;
import librarySE.utils.ValidationUtils;

/**
 * A broad {@link SearchStrategy} that checks all searchable fields
 * using the {@link LibraryItem#matchesKeyword(String)} method.
 * <p>
 * This is the most flexible and semantic strategy, as it leverages
 * the domain-specific matching logic implemented inside each item type.
 * </p>
 *
 * <h3>Validation:</h3>
 * Uses {@link ValidationUtils} to ensure the keyword is non-empty and non-null,
 * making the search consistent across all modules.
 * </h3>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * SearchStrategy strategy = new KeywordSearchStrategy();
 * boolean result = strategy.matches(book, "Artificial Intelligence");
 * }</pre>
 * @author Eman
 */
public class KeywordSearchStrategy implements SearchStrategy {

    /** {@inheritDoc} */
    @Override
    public boolean matches(LibraryItem item, String keyword) {
        if (item == null)
            throw new IllegalArgumentException("LibraryItem cannot be null");

        ValidationUtils.requireNonEmpty(keyword, "Keyword");

        return item.matchesKeyword(keyword);
    }
}
