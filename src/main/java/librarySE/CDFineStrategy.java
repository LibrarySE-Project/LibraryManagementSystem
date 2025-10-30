package librarySE;

import java.math.BigDecimal;

/**
 * Concrete implementation of {@link FineStrategy} for CDs.
 * <p>
 * Fine is fixed: 20 NIS per overdue day.
 * Borrow period is fixed: 7 days.
 * </p>
 * 
 * @see FineStrategy
 * @see CD
 * @see BorrowRecord
 * @see User
 * @author  Malak
 */
public class CDFineStrategy implements FineStrategy {

    /** Fine per overdue day in NIS */
    private final BigDecimal ratePerDay = BigDecimal.valueOf(20);

    /** Allowed borrow period in days */
    private final int borrowPeriodDays = 7;

    /**
     * Calculates the fine for a given number of overdue days.
     * 
     * @param overdueDays the number of days the CD is overdue
     * @return the calculated fine as a {@link BigDecimal}
     */
    @Override
    public BigDecimal calculateFine(long overdueDays) {
        if (overdueDays <= 0) return BigDecimal.ZERO;
        return ratePerDay.multiply(BigDecimal.valueOf(overdueDays));
    }

    /**
     * Returns the fixed allowed borrow period for CDs.
     * 
     * @return number of days a CD can be borrowed
     */
    public int getBorrowPeriodDays() {
        return borrowPeriodDays;
    }
}
