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

    // ---------------- Identity & price ----------------

    private final UUID id;
    private BigDecimal price = BigDecimal.ZERO;

    /**
     * Lock used to protect borrow/return/availability operations.
     * Marked as {@code transient} so it is not serialized.
     */
    private transient ReentrantLock lock;

    // ---------------- Copy tracking ----------------

    private int totalCopies;
    private int availableCopies;

    /**
     * Constructs a new {@code AbstractLibraryItem} with the given total copies.
     *
     * @param totalCopies initial total copies (> 0)
     */
    protected AbstractLibraryItem(int totalCopies) {
        if (totalCopies <= 0) {
            throw new IllegalArgumentException("Total copies must be > 0");
        }
        this.id = UUID.randomUUID();
        this.lock = new ReentrantLock();
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    /**
     * Lazily initializes and returns the internal {@link ReentrantLock}.
     */
    protected ReentrantLock getLock() {
        if (lock == null) {
            lock = new ReentrantLock();
        }
        return lock;
    }

    // --------------- Identity & price API ----------------

    @Override
    public final UUID getId() {
        return id;
    }

    @Override
    public final BigDecimal getPrice() {
        return price;
    }

    @Override
    public final void setPrice(BigDecimal price) {
        Objects.requireNonNull(price, "Price must not be null");
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be >= 0");
        }
        this.price = price;
    }

    // --------------- Copy counters API ----------------

    /** Total physical copies of this item. */
    public final synchronized int getTotalCopies() {
        return totalCopies;
    }

    /** Number of currently available copies. */
    public final synchronized int getAvailableCopies() {
        return availableCopies;
    }

    /**
     * Changes the total number of physical copies and adjusts the available
     * count accordingly, keeping it in the range [0, total].
     */
    public final synchronized void setTotalCopies(int newTotal) {
        if (newTotal <= 0) {
            throw new IllegalArgumentException("Total copies must be > 0");
        }
        int delta = newTotal - this.totalCopies;
        this.totalCopies = newTotal;
        this.availableCopies += delta;

        if (this.availableCopies > this.totalCopies) {
            this.availableCopies = this.totalCopies;
        }
        if (this.availableCopies < 0) {
            this.availableCopies = 0;
        }
    }

    // --------------- Thread-safe template methods ----------------

    @Override
    public final boolean isAvailable() {
        ReentrantLock l = getLock();
        l.lock();
        try {
            return availableCopies > 0;
        } finally {
            l.unlock();
        }
    }

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

    // --------------- Internal copy logic ----------------

    /** Used internally by {@link #isAvailable()} and {@link #borrow()}. */
    protected boolean isAvailableInternal() {
        return availableCopies > 0;
    }

    /** Common borrow implementation for all copy-tracked items. */
    protected boolean doBorrow() {
        if (availableCopies <= 0) {
            throw new IllegalStateException(
                    "No available copies of \"" + getDisplayNameForMessages() + "\" to borrow."
            );
        }
        availableCopies--;
        return true;
    }

    /** Common return implementation for all copy-tracked items. */
    protected boolean doReturn() {
        if (availableCopies >= totalCopies) {
            throw new IllegalStateException(
                    "All copies of \"" + getDisplayNameForMessages() + "\" are already in the library."
            );
        }
        availableCopies++;
        return true;
    }

    /**
     * Human-friendly label used in error messages
     * (e.g. "Clean Code", or "AI Journal [Vol. 15]").
     */
    protected abstract String getDisplayNameForMessages();
}