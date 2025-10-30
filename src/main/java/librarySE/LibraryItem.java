package librarySE;

/**
 * Represents a generic item in the library system.
 * <p>
 * Any item that can be managed by the library (such as books, magazines, or other media)
 * should implement this interface. It defines the basic behavior required for all
 * library items in terms of borrowing, availability, and identification.
 * </p>
 * 
 * <p>
 * Availability indicates whether the item can currently be borrowed or not.
 * </p>
 * 
 * @see Book
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
     * <p>
     * An item is available if it is not currently borrowed or otherwise unavailable.
     * </p>
     *
     * @return {@code true} if the item is available, {@code false} otherwise
     */
    boolean isAvailable();

    /**
     * Marks the item as returned and available for borrowing.
     * <p>
     * Implementing classes should update the internal availability status accordingly.
     * This method can also perform additional actions when an item is returned
     * (e.g., logging, notifying observers, etc.).
     * </p>
     *
     * @return {@code true} if the item was successfully marked as returned,
     *         {@code false} if the item was already available
     */
    boolean returnItem();

    /**
     * Attempts to borrow the item.
     * <p>
     * If the item is already borrowed, this should return {@code false}.
     * Otherwise, it marks the item as borrowed and returns {@code true}.
     * </p>
     *
     * @return {@code true} if the item was successfully borrowed,
     *         {@code false} if it was already borrowed
     */
    boolean borrow();
}

