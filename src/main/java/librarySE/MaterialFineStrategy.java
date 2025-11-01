package librarySE;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Implements the {@link FineStrategy} interface based on the type of library material.
 * <p>
 * Each {@link MaterialType} (BOOK, CD, JOURNAL) has a predefined fine rate per overdue day
 * and a standard borrowing period.
 * </p>
 * 
 * <p>
 * This class calculates fines for overdue items and provides the borrowing period
 * for each material type.
 * </p>
 * 
 * @see FineStrategy
 * @see MaterialType
 * @author Malak
 */
public class MaterialFineStrategy implements FineStrategy {

    /** Mapping of material type to daily fine rate. */
    private static final Map<MaterialType, BigDecimal> RATE_PER_DAY = Map.of(
        MaterialType.BOOK, BigDecimal.valueOf(10),
        MaterialType.CD, BigDecimal.valueOf(20),
        MaterialType.JOURNAL, BigDecimal.valueOf(15)
    );

    /** Mapping of material type to standard borrow period in days. */
    private static final Map<MaterialType, Integer> BORROW_PERIOD = Map.of(
        MaterialType.BOOK, 28,
        MaterialType.CD, 7,
        MaterialType.JOURNAL, 21
    );

    /** The fine rate per day for this material. */
    private final BigDecimal ratePerDay;

    /** The borrowing period in days for this material. */
    private final int borrowPeriodDays;

    /**
     * Constructs a {@code MaterialFineStrategy} for the given material type.
     *
     * @param type the type of library material; must exist in RATE_PER_DAY and BORROW_PERIOD
     * @throws IllegalArgumentException if the material type is unknown
     */
    public MaterialFineStrategy(MaterialType type) {
        if (!RATE_PER_DAY.containsKey(type))
            throw new IllegalArgumentException("Unknown material type: " + type);
        this.ratePerDay = RATE_PER_DAY.get(type);
        this.borrowPeriodDays = BORROW_PERIOD.get(type);
    }

    /**
     * Calculates the fine for a given number of overdue days.
     *
     * @param overdueDays the number of days the item is overdue; must be >= 0
     * @return the total fine as a {@link BigDecimal}; returns zero if overdueDays <= 0
     */
    @Override
    public BigDecimal calculateFine(long overdueDays) {
        if (overdueDays <= 0) return BigDecimal.ZERO;
        return ratePerDay.multiply(BigDecimal.valueOf(overdueDays));
    }

    /**
     * Returns the standard borrowing period for this material type.
     *
     * @return number of days the item can be borrowed without fines
     */
    @Override
    public int getBorrowPeriodDays() { return borrowPeriodDays; }
}
