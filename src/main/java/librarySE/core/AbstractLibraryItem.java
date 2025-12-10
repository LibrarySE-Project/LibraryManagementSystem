package librarySE.core;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract base implementation of {@link LibraryItem} that provides:
 * <ul>
 *   <li>A generated unique {@link UUID} identifier for each item instance.</li>
 *   <li>Common price handling with validation.</li>
 *   <li>Thread-safe implementations of borrowing and returning operations.</li>
 *   <li>A template-method based design that delegates copy-specific logic
 *       to subclasses via {@link #isAvailableInternal()}, {@link #doBorrow()},
 *       and {@link #doReturn()}.</li>
 * </ul>
 *
 * <p>
 * Concrete materials such as {@link Book}, {@link CD}, and {@link Journal}
 * extend this class and only implement their own metadata and copy-tracking
 * logic. All concurrency concerns and core behaviors are centralized here.
 * </p>
 *
 * <h3>Concurrency Model</h3>
 * <p>
 * This class uses an internal {@link ReentrantLock} to guarantee that
 * calls to {@link #borrow()}, {@link #returnItem()}, and {@link #isAvailable()}
 * are mutually exclusive per instance. This is important when multiple threads
 * may attempt to borrow or return the same item concurrently.
 * </p>
 *
 * <p>
 * The lock field is marked as {@code transient} so that it is <b>not</b>
 * included in JSON persistence when using Gson. Because Gson does not invoke
 * constructors or field initializers during deserialization, the lock is
 * lazily initialized via {@link #getLock()} to avoid {@code NullPointerException}
 * after loading items from disk.
 * </p>
 *
 * <h3>Template Methods</h3>
 * <ul>
 *   <li>{@link #isAvailable()} acquires the lock and then delegates to
 *       {@link #isAvailableInternal()}.</li>
 *   <li>{@link #borrow()} acquires the lock, checks availability via
 *       {@link #isAvailableInternal()}, and if allowed, calls {@link #doBorrow()}.</li>
 *   <li>{@link #returnItem()} acquires the lock and calls {@link #doReturn()}.</li>
 * </ul>
 *
 * <p>
 * Subclasses are responsible only for implementing the internal methods
 * that adjust their copy counters; they do not need to worry about
 * synchronization or template flow.
 * </p>
 *
 * @author eman
 */
public abstract class AbstractLibraryItem implements LibraryItem, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for this library item instance.
     */
    private final UUID id;

    /**
     * Monetary value or replacement cost of this item.
     */
    private BigDecimal price = BigDecimal.ZERO;

    /**
     * Lock used to protect borrow/return/availability operations.
     * <p>
     * Marked as {@code transient} so that JSON serializers such as Gson
     * do not attempt to serialize the internal fields of {@link ReentrantLock},
     * which would otherwise cause reflection-access errors.
     * </p>
     *
     * <p>
     * Because constructors and field initializers are not invoked during
     * Gson deserialization, this field may end up {@code null} after
     * loading from JSON. To handle that safely, all access goes through
     * {@link #getLock()}, which lazily reinitializes the lock if needed.
     * </p>
     */
    private transient ReentrantLock lock;

    /**
     * Constructs a new {@code AbstractLibraryItem} with a generated UUID
     * and default price of {@code 0.00}.
     */
    protected AbstractLibraryItem() {
        this.id = UUID.randomUUID();
        this.lock = new ReentrantLock();
    }

    /**
     * Lazily initializes and returns the internal {@link ReentrantLock}.
     * <p>
     * This method is necessary because JSON deserialization (e.g. via Gson)
     * does not invoke constructors or field initializers, so transient fields
     * like {@code lock} may be {@code null} when an object is loaded from disk.
     * </p>
     *
     * @return a non-null {@link ReentrantLock} instance
     */
    protected ReentrantLock getLock() {
        if (lock == null) {
            lock = new ReentrantLock();
        }
        return lock;
    }

    /**
     * Returns the unique identifier of this item.
     *
     * @return non-null {@link UUID}
     */
    @Override
    public final UUID getId() {
        return id;
    }

    /**
     * Returns the current price of this item.
     *
     * @return non-null {@link BigDecimal} representing the price
     */
    @Override
    public final BigDecimal getPrice() {
        return price;
    }

    /**
     * Updates the price of this item after basic validation.
     * <p>
     * Subclasses typically call this method from their own "smart price"
     * initializers (e.g., reading defaults from configuration).
     * </p>
     *
     * @param price new price; must not be {@code null} and must be &gt;= 0
     * @throws IllegalArgumentException if {@code price} is null or negative
     */
    @Override
    public final void setPrice(BigDecimal price) {
        Objects.requireNonNull(price, "Price must not be null");
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be >= 0");
        }
        this.price = price;
    }

    /**
     * Checks whether this item is currently available for borrowing.
     * <p>
     * The operation is performed under the internal lock to ensure a
     * consistent view when other threads may be borrowing or returning
     * copies at the same time.
     * </p>
     *
     * @return {@code true} if available; {@code false} otherwise
     */
    @Override
    public final boolean isAvailable() {
        ReentrantLock l = getLock();
        l.lock();
        try {
            return isAvailableInternal();
        } finally {
            l.unlock();
        }
    }

    /**
     * Attempts to borrow this item in a thread-safe manner.
     * <p>
     * This method:
     * <ol>
     *   <li>Acquires the internal lock.</li>
     *   <li>Checks availability via {@link #isAvailableInternal()}.</li>
     *   <li>If unavailable, returns {@code false} without calling {@link #doBorrow()}.</li>
     *   <li>If available, calls {@link #doBorrow()} to let the subclass
     *       update its internal copy counters.</li>
     * </ol>
     * </p>
     *
     * @return {@code true} if a copy was successfully borrowed;
     *         {@code false} if no copies were available
     * @throws IllegalStateException if subclasses choose to signal
     *         invalid states via exceptions
     */
    @Override
    public final boolean borrow() {
        ReentrantLock l = getLock();
        l.lock();
        try {
            if (!isAvailableInternal()) {
                return false;
            }
            return doBorrow();
        } finally {
            l.unlock();
        }
    }

    /**
     * Returns a previously borrowed copy of this item in a thread-safe manner.
     * <p>
     * Delegates to {@link #doReturn()} to perform subclass-specific logic
     * (e.g., incrementing available copy counts).
     * </p>
     *
     * @return {@code true} if the return was successful
     * @throws IllegalStateException if the item cannot be returned in its
     *         current state (e.g., all copies already present)
     */
    @Override
    public final boolean returnItem() {
        ReentrantLock l = getLock();
        l.lock();
        try {
            return doReturn();
        } finally {
            l.unlock();
        }
    }

    /**
     * Internal availability check used by {@link #isAvailable()} and {@link #borrow()}.
     * <p>
     * Subclasses should implement this method based on their internal
     * copy-tracking model (for example, {@code availableCopies > 0}).
     * </p>
     *
     * @return {@code true} if at least one copy is available
     */
    protected abstract boolean isAvailableInternal();

    /**
     * Performs the actual "borrow" mutation at the subclass level.
     * <p>
     * This method is called only when {@link #isAvailableInternal()} has
     * already returned {@code true} under the internal lock.
     * Implementations typically decrement an {@code availableCopies} field
     * and may throw {@link IllegalStateException} if invariants are violated.
     * </p>
     *
     * @return {@code true} if the borrow mutation succeeded
     */
    protected abstract boolean doBorrow();

    /**
     * Performs the actual "return" mutation at the subclass level.
     * <p>
     * This method is invoked under the internal lock by {@link #returnItem()}.
     * Implementations typically increment an {@code availableCopies} field
     * while enforcing that it does not exceed {@code totalCopies}.
     * </p>
     *
     * @return {@code true} if the return mutation succeeded
     */
    protected abstract boolean doReturn();
}
