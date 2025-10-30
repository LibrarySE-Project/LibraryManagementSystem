package librarySE;

import java.math.BigDecimal;

/**
 * A {@link FineStrategy} implementation that determines fine rates and borrowing periods
 * based on the {@link MaterialType} of the library item.
 * <p>
 * Each material type has its own fixed fine rate per overdue day and a specific borrowing period:
 * <ul>
 *   <li>{@link MaterialType#BOOK} → 10 NIS per day, 28-day borrow period</li>
 *   <li>{@link MaterialType#CD} → 20 NIS per day, 7-day borrow period</li>
 *   <li>{@link MaterialType#JOURNAL} → 15 NIS per day, 21-day borrow period</li>
 * </ul>
 * </p>
 *
 * @see FineStrategy
 * @see MaterialType
 * @see BorrowRecord
 * @author Malak
 */
public class MaterialFineStrategy implements FineStrategy {

    /** Fine rate applied per overdue day. */
    private final BigDecimal ratePerDay;

    /** Maximum number of days the item can be borrowed before it becomes overdue. */
    private final int borrowPeriodDays;

    /**
     * Constructs a fine strategy based on the given material type.
     *
     * @param type the type of library material (e.g., BOOK, CD, JOURNAL)
     * @throws IllegalArgumentException if the type is unknown
     */
    public MaterialFineStrategy(MaterialType type) {
    	 switch (type) {
          case BOOK:
              ratePerDay = BigDecimal.valueOf(10);
              borrowPeriodDays = 28;
              break;
          case CD:
              ratePerDay = BigDecimal.valueOf(20);
              borrowPeriodDays = 7;
              break;
          case JOURNAL:
              ratePerDay = BigDecimal.valueOf(15);
              borrowPeriodDays = 21;
              break;
          default:
              throw new IllegalArgumentException("Unknown material type: " + type);
          }
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal calculateFine(long overdueDays) {
        if (overdueDays <= 0) return BigDecimal.ZERO;
        return ratePerDay.multiply(BigDecimal.valueOf(overdueDays));
    }

    /**
     * Returns the borrowing period in days for the associated material type.
     *
     * @return number of days allowed for borrowing
     */
    @Override
    public int getBorrowPeriodDays() {
        return borrowPeriodDays;
    }
}
