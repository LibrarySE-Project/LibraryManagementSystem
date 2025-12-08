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
 * @author Eman
 * @see LibraryItem
 * @see Book
 * @see CD
 * @see Journal
 */
public abstract class AbstractLibraryItem implements LibraryItem, Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique system-generated identifier for this item. */
    private final UUID id = UUID.randomUUID();

    /** Lock ensuring thread safety during borrow/return operations. */
    protected final ReentrantLock lock = new ReentrantLock();

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
     * Template method: thread-safe availability check.
     *
     * @return {@code true} if the item is available, {@code false} otherwise
     */
    @Override
    public boolean isAvailable() {
        lock.lock();
        try {
            return isAvailableInternal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Template method: thread-safe borrow operation.
     *
     * @return {@code true} if borrowing succeeds
     * @throws IllegalStateException if the item (or copies) are not available
     */
    @Override
    public boolean borrow() {
        lock.lock();
        try {
            return doBorrow();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Template method: thread-safe return operation.
     *
     * @return {@code true} if returning succeeds
     * @throws IllegalStateException if the item cannot be returned
     */
    @Override
    public final boolean returnItem() {
        lock.lock();
        try {
            return doReturn();
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
     *
     * @param price the new price to assign (must be non-negative and non-null)
     * @throws IllegalArgumentException if the price is negative
     * @throws NullPointerException     if {@code price} is {@code null}
     */
    @Override
    public void setPrice(BigDecimal price) {
        ValidationUtils.requireNonEmpty(price, "price");
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.price = price;
    }

    /* ---------- Hook methods for subclasses ---------- */

    /**
     * Implemented by subclasses to check if at least one copy is available.
     */
    protected abstract boolean isAvailableInternal();

    /**
     * Implemented by subclasses to perform the actual borrow logic on copies.
     */
    protected abstract boolean doBorrow();

    /**
     * Implemented by subclasses to perform the actual return logic on copies.
     */
    protected abstract boolean doReturn();
}
