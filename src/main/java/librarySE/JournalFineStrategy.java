package librarySE;

import java.math.BigDecimal;

/**
 * Concrete implementation of {@link FineStrategy} for journals.
 * <p>
 * Fine is fixed: 15 NIS per overdue day.
 * Borrow period is fixed: 21 days.
 * </p>
 * 
 * @see FineStrategy
 * @author  Malak
 */
public class JournalFineStrategy implements FineStrategy {

    /** Fine per overdue day in NIS */
    private final BigDecimal ratePerDay = BigDecimal.valueOf(15);

    /** Allowed borrow period in days */
    private final int borrowPeriodDays = 21;

    /**
     * Calculates the fine for a given number of overdue days.
     * 
     * @param overdueDays the number of days the journal is overdue
     * @return the calculated fine as a {@link BigDecimal}
     */
    @Override
    public BigDecimal calculateFine(long overdueDays) {
        if (overdueDays <= 0) return BigDecimal.ZERO;
        return ratePerDay.multiply(BigDecimal.valueOf(overdueDays));
    }

    /**
     * Returns the fixed allowed borrow period for journals.
     * 
     * @return number of days a journal can be borrowed
     */
    public int getBorrowPeriodDays() {
        return borrowPeriodDays;
    }
}
