package librarySE.search;


import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import librarySE.core.LibraryItem;

/**
 * Context class for executing searches using different {@link SearchStrategy} implementations.
 * <p>
 * This class acts as the runtime switcher between multiple strategies,
 * following the <b>Strategy Design Pattern</b>. You can set or replace
 * the search algorithm dynamically while the application is running.
 * </p>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * SearchContext context = new SearchContext(new TitleSearchStrategy());
 * List<LibraryItem> results = context.search(items, "java");
 *
 * // Switch strategy at runtime
 * context.setStrategy(new AuthorSearchStrategy());
 * results = context.search(items, "bloch");
 * }</pre>
 *
 * @see SearchStrategy
 * @see TitleSearchStrategy
 * @see KeywordSearchStrategy
 * @see DefaultSearchStrategy
 */
public class SearchContext {

    /** The current search algorithm used by this context. */
    private SearchStrategy strategy;

    /**
     * Constructs a new {@code SearchContext} with the specified initial strategy.
     *
     * @param strategy the initial {@link SearchStrategy} to use (non-null)
     * @throws NullPointerException if strategy is {@code null}
     */
    public SearchContext(SearchStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "SearchStrategy cannot be null");
    }

    /**
     * Replaces the current search strategy with a new one.
     *
     * @param strategy the new {@link SearchStrategy} to use (non-null)
     * @throws NullPointerException if strategy is {@code null}
     */
    public void setStrategy(SearchStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "SearchStrategy cannot be null");
    }

    /**
     * Executes a search across a collection of {@link LibraryItem} objects
     * using the currently active strategy.
     *
     * @param items   the list of items to search
     * @param keyword the search term
     * @return a list of matching items, possibly empty but never {@code null}
     */
    public List<LibraryItem> search(List<LibraryItem> items, String keyword) {
        if (items == null || keyword == null) return List.of();
        return items.stream()
                .filter(item -> strategy.matches(item, keyword))
                .collect(Collectors.toList());
    }
}
