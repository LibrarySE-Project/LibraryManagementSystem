package librarySE.core;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import librarySE.utils.ValidationUtils;

/**
 * An abstract base class that provides common functionality for all library items.
 * <p>
 * Implements {@link LibraryItem} and provides thread-safe operations for borrowing
 * and returning items using a {@link ReentrantLock}. Each item has a unique ID,
 * availability state, and configurable price.
 * </p>
 *
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *   <li>Maintain item identity and availability status.</li>
 *   <li>Provide thread-safe borrow and return operations.</li>
 *   <li>Support price management with validation.</li>
 * </ul>
 *
 * <h3>Used By:</h3>
 * <ul>
 *   <li>{@link Book}</li>
 *   <li>{@link CD}</li>
 *   <li>{@link Journal}</li>
 * </ul>
 *
 * @see LibraryItem
 * @see Book
 * @see CD
 * @see Journal
 * @author Malak
 */
public abstract class AbstractLibraryItem implements LibraryItem, Serializable {


    /** Serialization identifier for version consistency. */
    private static final long serialVersionUID = 1L;

    /** Unique system-generated identifier for this item. */
    private final UUID id = UUID.randomUUID();

    /** Lock ensuring thread safety during borrow/return operations. */
    private final ReentrantLock lock = new ReentrantLock();

    /** Indicates whether the item is currently available for borrowing. */
    private boolean available = true;

    /** The current price of this item (default = 0). */
    private BigDecimal price = BigDecimal.ZERO;


    /**
     * Returns the unique identifier of this library item.
     *
     * @return a {@link UUID} representing this item's unique ID
     */
    @Override
    public UUID getId() {
        return id;
    }

 

    /**
     * Checks if the item is available for borrowing.
     * <p>
     * This method is thread-safe and ensures consistent results
     * even if multiple users access the same item concurrently.
     * </p>
     *
     * @return {@code true} if the item is available, {@code false} if currently borrowed
     */
    @Override
    public boolean isAvailable() {
        lock.lock();
        try {
            return available;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Attempts to borrow the item.
     * <p>
     * If the item is available, it will be marked as borrowed and
     * become unavailable for other users. This method is synchronized
     * using {@link ReentrantLock} for thread safety.
     * </p>
     *
     * @return {@code true} if the item was successfully borrowed;
     *         {@code false} if it was already borrowed by another user
     */
    @Override
    public boolean borrow() {
        lock.lock();
        try {
            if (!available) return false;
            available = false;
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the item to the library and makes it available again.
     * <p>
     * Thread-safe operation that updates the availability flag only
     * if the item was previously borrowed.
     * </p>
     *
     * @return {@code true} if successfully returned;
     *         {@code false} if the item was not borrowed
     */
    @Override
    public boolean returnItem() {
        lock.lock();
        try {
            if (available) return false;
            available = true;
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves the current price of the item.
     *
     * @return the item's {@link BigDecimal} price (never {@code null})
     */
    @Override
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Updates the price of this item.
     * <p>
     * Ensures that the new price value is valid and non-negative.
     * Used by administrators or system operations to assign item prices.
     * </p>
     *
     * @param price the new price to assign (must be non-negative and non-null)
     * @throws IllegalArgumentException if the price is negative
     * @throws NullPointerException if {@code price} is {@code null}
     */
    @Override
    public void setPrice(BigDecimal price) {
    	ValidationUtils.requireNonEmpty(price, "price");
        if (price.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Price cannot be negative.");
        this.price = price;
    }
}
