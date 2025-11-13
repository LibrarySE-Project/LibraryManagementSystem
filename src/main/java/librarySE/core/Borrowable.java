package librarySE.core;


/**
 * Represents the ability of a library item to be borrowed and returned.
 * <p>
 * Classes implementing this interface (e.g., {@code Book}, {@code CD}, {@code Journal})
 * define how items can be checked out and returned by users.
 * </p>
 *
 * @author Malak
 */
public interface Borrowable {

    /**
     * Checks whether this item is currently available for borrowing.
     *
     * @return {@code true} if the item is available to borrow, {@code false} otherwise
     */
    boolean isAvailable();

    /**
     * Attempts to borrow this item.
     * <p>
     * Implementations should update the item's internal state to mark it as borrowed.
     * </p>
     *
     * @return {@code true} if the borrowing operation succeeded,
     *         {@code false} if the item is already borrowed or unavailable
     */
    boolean borrow();

    /**
     * Returns this item to the library.
     * <p>
     * Implementations should update the item's internal state to mark it as available again.
     * </p>
     *
     * @return {@code true} if the return operation succeeded,
     *         {@code false} if the item was not borrowed or cannot be returned
     */
    boolean returnItem();
}
