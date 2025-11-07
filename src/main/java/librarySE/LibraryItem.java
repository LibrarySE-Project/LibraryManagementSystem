package librarySE;

/**
 * Represents a generic item in the library system.
 * <p>
 * Any item that can be managed by the library (such as books, CDs, or journals)
 * should implement this interface. It defines the required behavior for
 * borrowing, returning, and searching within the library.
 * </p>
 *
 * @see Book
 * @see CD
 * @see Journal
 * @see AbstractLibraryItem
 * @author Malak
 */
public interface LibraryItem {

    /**
     * Returns the title or name of the library item.
     *
     * @return the item's title as a {@code String}
     */
    String getTitle();

    /**
     * Checks if the item is currently available for borrowing.
     *
     * @return {@code true} if available, {@code false} otherwise
     */
    boolean isAvailable();

    /**
     * Marks the item as borrowed if available.
     *
     * @return {@code true} if successfully borrowed, {@code false} otherwise
     */
    boolean borrow();

    /**
     * Marks the item as returned and available again.
     *
     * @return {@code true} if successfully returned, {@code false} otherwise
     */
    boolean returnItem();

    /**
     * Returns the type of this material (e.g., BOOK, CD, JOURNAL).
     *
     * @return the {@link MaterialType} of this item
     */
    MaterialType getMaterialType();

    /**
     * Checks if the item matches the given keyword (case-insensitive).
     *
     * @param keyword the keyword to match; must not be null
     * @return {@code true} if the item matches the keyword, {@code false} otherwise
     * @throws IllegalArgumentException if {@code keyword} is null
     */
    boolean matchesKeyword(String keyword);
}

