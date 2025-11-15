package librarySE.managers;

import java.math.BigDecimal;

import librarySE.strategy.BaseFineStrategy;
import librarySE.strategy.FineStrategy;
import librarySE.strategy.FineStrategyFactory;

/**
 * Context class that aggregates a {@link FineStrategy} to calculate fines.
 * <p>
 * Implements the Strategy Pattern — this class delegates fine calculation
 * and borrow period logic to the assigned strategy.
 * </p>
 *
 * <p>
 * The context does not depend on any specific material type, allowing
 * complete flexibility when switching strategies at runtime.
 * </p>
 *
 * @see FineStrategy
 * @see FineStrategyFactory
 * @see BaseFineStrategy
 * @see BorrowRecord
 * @author Malak
 */
public class FineContext {

    /** The fine calculation strategy (aggregated object). */
    private FineStrategy strategy;

    /**
     * Constructs a {@code FineContext} with the given fine strategy.
     *
     * @param strategy the fine strategy to use; must not be null
     * @throws IllegalArgumentException if {@code strategy} is null
     */
    public FineContext(FineStrategy strategy) {
        if (strategy == null)
            throw new IllegalArgumentException("Fine strategy cannot be null.");
        this.strategy = strategy;
    }

    /**
     * Sets or replaces the current fine strategy at runtime.
     *
     * @param strategy the new fine strategy to assign
     * @throws IllegalArgumentException if {@code strategy} is null
     */
    public void setStrategy(FineStrategy strategy) {
        if (strategy == null)
            throw new IllegalArgumentException("Fine strategy cannot be null.");
        this.strategy = strategy;
    }

    /**
     * Calculates the fine for the given number of overdue days
     * using the current strategy.
     *
     * @param overdueDays number of days the item is overdue
     * @return calculated fine as a {@link BigDecimal}
     */
    public BigDecimal calculateFine(long overdueDays) {
        return strategy.calculateFine(overdueDays);
    }

    /**
     * Returns the allowed borrow period in days for the current strategy.
     *
     * @return allowed borrowing period in days
     */
    public int getBorrowPeriodDays() {
        return strategy.getBorrowPeriodDays();
    }
}
