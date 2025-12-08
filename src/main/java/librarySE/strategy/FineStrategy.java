package librarySE.strategy;

import java.math.BigDecimal;

/**
 * Strategy interface for calculating fines for overdue library items.
 * <p>
 * Implementing classes provide the fine calculation logic based on the number
 * of days an item (book, CD, journal, etc.) is overdue.
 * </p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * FineStrategy bookStrategy = new BookFineStrategy();
 * BigDecimal fine = bookStrategy.calculateFine(5); // Fine for 5 overdue days
 * </pre>
 * 
 * <p>
 * This follows the Strategy Pattern to allow different fine rules
 * for different item types without modifying the borrowing logic.
 * </p>
 * 
 * 
 * @author Eman
 * 
 */
public interface FineStrategy {

    /**
     * Calculates the fine for a given number of overdue days.
     *
     * @param overdueDays the number of days the item is overdue; must be >= 0
     * @return the calculated fine as a {@link BigDecimal}; returns 0 if overdueDays <= 0
     */
    BigDecimal calculateFine(long overdueDays);

    /**
     * Returns the allowed borrowing period (in days) for the item type.
     * <p>
     * This is useful for checking overdue status before calculating fines.
     * </p>
     *
     * @return allowed borrow period in days
     */
     int getBorrowPeriodDays() ;
}
