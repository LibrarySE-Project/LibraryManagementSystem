package librarySE.strategy;

import java.math.BigDecimal;

/**
 * Abstract base class for fine strategies that share common logic.
 * <p>
 * Provides default implementations for {@link #calculateFine(long)} and
 * {@link #getBorrowPeriodDays()}, while allowing subclasses to define their
 * own fine rate and borrow period.
 * </p>
 * 
 * @author Eman
 * @see FineStrategy
 */
public abstract class BaseFineStrategy implements FineStrategy {

    /** Fine rate per overdue day (in NIS). */
    private final BigDecimal ratePerDay;

    /** Allowed borrow period in days. */
    private final int borrowPeriodDays;

    /**
     * Constructs a base fine strategy with a fixed rate and borrow period.
     *
     * @param ratePerDay fine rate per overdue day; must not be null or negative
     * @param borrowPeriodDays allowed borrowing period in days; must be positive
     * @throws IllegalArgumentException if rate or period are invalid
     */
    protected BaseFineStrategy(BigDecimal ratePerDay, int borrowPeriodDays) {
        if (ratePerDay == null || ratePerDay.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Rate per day must be non-negative.");
        if (borrowPeriodDays <= 0)
            throw new IllegalArgumentException("Borrow period must be positive.");

        this.ratePerDay = ratePerDay;
        this.borrowPeriodDays = borrowPeriodDays;
    }

    /**
     * Calculates the fine for a given number of overdue days.
     *
     * @param overdueDays number of days overdue; must be >= 0
     * @return the calculated fine; {@link BigDecimal#ZERO} if no overdue
     */
    @Override
    public BigDecimal calculateFine(long overdueDays) {
        if (overdueDays <= 0) return BigDecimal.ZERO;
        return ratePerDay.multiply(BigDecimal.valueOf(overdueDays));
    }

    /**
     * Returns the allowed borrowing period for this strategy.
     *
     * @return number of days the item can be borrowed without fines
     */
    @Override
    public int getBorrowPeriodDays() {
        return borrowPeriodDays;
    }
}
