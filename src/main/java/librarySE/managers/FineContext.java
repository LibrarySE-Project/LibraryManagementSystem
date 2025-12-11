package librarySE.managers;

import java.math.BigDecimal;

import librarySE.strategy.FineStrategy;
import librarySE.strategy.FineStrategyFactory;

/**
 * A flexible context class that delegates fine calculation and borrow-period
 * rules to a pluggable {@link FineStrategy}.
 *
 * <p>
 * This class embodies the <b>Strategy Pattern</b>: instead of embedding any
 * fine-related logic, it outsources all computations to the strategy currently
 * assigned. This allows seamless switching between different fine policies at
 * runtime (e.g., books, journals, CDs, premium materials, or custom rules).
 * </p>
 *
 * <h3>Key Characteristics</h3>
 * <ul>
 *   <li>Decouples fine logic from the rest of the system</li>
 *   <li>Supports runtime strategy replacement through {@link #setStrategy(FineStrategy)}</li>
 *   <li>Promotes high configurability and extensibility</li>
 *   <li>Works with any class implementing {@link FineStrategy}</li>
 * </ul>
 *
 * <p>
 * The context itself contains no material-specific assumptions, making it fully
 * reusable across the entire library domain.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * FineStrategy strategy = FineStrategyFactory.forBooks();
 * FineContext context = new FineContext(strategy);
 *
 * BigDecimal fine = context.calculateFine(3);  // Fine for 3 overdue days
 * int period = context.getBorrowPeriodDays(); // Borrow duration set by strategy
 *
 * // Switch strategy dynamically:
 * context.setStrategy(FineStrategyFactory.forJournals());
 * }</pre>
 *
 * <p>
 * This design ensures that policy changes require no modification to the
 * context itself — only the strategies evolve.
 * </p>
 *
 * @author Eman
 */
public class FineContext {

    /** The active fine calculation strategy. */
    private FineStrategy strategy;

    /**
     * Creates a {@code FineContext} with the provided strategy.
     *
     * @param strategy the fine strategy to use; must not be null
     * @throws IllegalArgumentException if the provided strategy is null
     */
    public FineContext(FineStrategy strategy) {
        if (strategy == null)
            throw new IllegalArgumentException("Fine strategy cannot be null.");
        this.strategy = strategy;
    }

    /**
     * Replaces the active fine strategy at runtime.
     * <p>
     * Enables dynamic behavioral changes in borrowing and penalty policies.
     * </p>
     *
     * @param strategy the new fine strategy
     * @throws IllegalArgumentException if the provided strategy is null
     */
    public void setStrategy(FineStrategy strategy) {
        if (strategy == null)
            throw new IllegalArgumentException("Fine strategy cannot be null.");
        this.strategy = strategy;
    }

    /**
     * Computes the fine for the specified number of overdue days
     * using the currently assigned strategy.
     *
     * @param overdueDays number of days an item is overdue
     * @return the calculated fine as a {@link BigDecimal}
     */
    public BigDecimal calculateFine(long overdueDays) {
        return strategy.calculateFine(overdueDays);
    }

    /**
     * Retrieves the borrowing period (in days) defined by the current strategy.
     *
     * @return number of days an item may be borrowed before becoming overdue
     */
    public int getBorrowPeriodDays() {
        return strategy.getBorrowPeriodDays();
    }
}
