package librarySE.core;

/**
 * Represents the ability of a library item to be searched by keyword.
 * <p>
 * Classes implementing this interface (e.g., {@code Book}, {@code Journal}, {@code CD})
 * define how keyword matching is performed, typically by comparing the keyword
 * against one or more fields such as title, author, or editor.
 * </p>
 *
 * @author malak
 */
public interface Searchable {

    /**
     * Determines whether this item matches the specified search keyword.
     * <p>
     * Implementations may perform case-insensitive comparisons
     * and may check multiple fields for matches (e.g., title, author, ISBN).
     * </p>
     *
     * @param keyword the search keyword; must not be {@code null} or empty
     * @return {@code true} if this item matches the keyword, {@code false} otherwise
     */
    boolean matchesKeyword(String keyword);
}
