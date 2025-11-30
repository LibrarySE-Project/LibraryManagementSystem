package librarySE.search;

import librarySE.core.LibraryItem;
import librarySE.utils.ValidationUtils;

/**
 * A {@link SearchStrategy} implementation that performs case-insensitive
 * search based on the title of a {@link LibraryItem}.
 * <p>
 * Ensures robust validation using {@link ValidationUtils} and provides
 * predictable results for all library materials that expose a title.
 * </p>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * SearchStrategy strategy = new TitleSearchStrategy();
 * boolean found = strategy.matches(book, "networking");
 * }</pre>
 * 
 * @author Eman
 * @see librarySE.core.LibraryItem#getTitle()
 * @see ValidationUtils
 * @see SearchStrategy
 */
public class TitleSearchStrategy implements SearchStrategy {

    /** {@inheritDoc} */
    @Override
    public boolean matches(LibraryItem item, String keyword) {
        if (item == null)
            throw new IllegalArgumentException("LibraryItem cannot be null");

        ValidationUtils.requireNonEmpty(keyword, "Keyword");

        String title = item.getTitle();
        return title != null && title.toLowerCase().contains(keyword.toLowerCase());
    }
}
