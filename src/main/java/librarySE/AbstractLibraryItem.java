package librarySE;

/**
 * Abstract base class providing common functionality for all {@link LibraryItem} types.
 * <p>
 * Implements standard behaviors such as availability handling, thread safety, and input validation.
 * Concrete subclasses like {@link Book}, {@link CD}, and {@link Journal} only need to
 * provide specific fields (title, author, etc.).
 * </p>
 *
 * @see LibraryItem
 * @see Book
 * @see CD
 * @see Journal
 * @author Malak
 */
public abstract class AbstractLibraryItem implements LibraryItem {

    /** Availability status of the item. */
    private boolean available = true;

    /**
     * Checks if the item is currently available for borrowing.
     *
     * @return {@code true} if available, {@code false} otherwise
     */
    @Override
    public synchronized boolean isAvailable() {
        return available;
    }

    /**
     * Marks the item as borrowed if available.
     *
     * @return {@code true} if successfully borrowed, {@code false} otherwise
     */
    @Override
    public synchronized boolean borrow() {
        if (!available) return false;
        available = false;
        return true;
    }

    /**
     * Marks the item as returned and available again.
     *
     * @return {@code true} if successfully returned, {@code false} otherwise
     */
    @Override
    public synchronized boolean returnItem() {
        if (available) return false;
        available = true;
        return true;
    }

    /**
     * Validates that a string is not null or empty.
     *
     * @param value the string to validate
     * @param fieldName the field name for error messages
     * @throws IllegalArgumentException if the string is null or empty
     */
    protected void validateNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(fieldName + " must not be null or empty.");
    }
}
