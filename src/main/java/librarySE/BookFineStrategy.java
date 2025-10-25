package librarySE;

import java.math.BigDecimal;

/**
 * Concrete implementation of {@link FineStrategy} for books.
 * <p>
 * Fine is fixed: 10 NIS per overdue day.
 * Borrow period is fixed: 28 days.
 * </p>
 * 
 * @see FineStrategy
 * @see Book
 * @see BorrowRecord
 * @see User
 * @author Malak
 */
public class BookFineStrategy implements FineStrategy {

    /** Fine per overdue day in NIS */
    private final BigDecimal ratePerDay = BigDecimal.valueOf(10);

    /** Allowed borrow period in days */
    private final int borrowPeriodDays = 28;

    @Override
    public BigDecimal calculateFine(long overdueDays) {
        if (overdueDays <= 0) return BigDecimal.ZERO;
        return ratePerDay.multiply(BigDecimal.valueOf(overdueDays));
    }

    /** Returns the fixed allowed borrow period for books */
    public int getBorrowPeriodDays() {
        return borrowPeriodDays;
    }
}
