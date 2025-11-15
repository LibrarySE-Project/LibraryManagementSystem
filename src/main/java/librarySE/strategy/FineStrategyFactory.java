package librarySE.strategy;


import librarySE.utils.Config;
import java.math.BigDecimal;

/**
 * Factory class responsible for creating {@link FineStrategy} instances
 * for different library material types (Book, CD, Journal).
 * <p>
 * This class reads fine rates and borrowing periods from the centralized
 * configuration file via {@link Config} and constructs the corresponding
 * fine calculation strategy. It encapsulates the logic for initializing
 * rate and period values, ensuring consistent and maintainable behavior.
 * </p>
 *
 * <h3>Design Pattern:</h3>
 * <ul>
 *   <li><b>Factory Pattern</b> — to produce {@link FineStrategy} objects dynamically.</li>
 *   <li><b>Strategy Pattern</b> — to encapsulate fine calculation logic per material type.</li>
 * </ul>
 *
 * <h3>Configuration Keys:</h3>
 * <ul>
 *   <li>{@code fine.book.rate}, {@code fine.book.period}</li>
 *   <li>{@code fine.cd.rate}, {@code fine.cd.period}</li>
 *   <li>{@code fine.journal.rate}, {@code fine.journal.period}</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * FineStrategy bookFine = FineStrategyFactory.book();
 * BigDecimal fine = bookFine.calculateFine(3); // 3 overdue days × rate
 * System.out.println("Book fine: " + fine);
 *
 * FineStrategy cdFine = FineStrategyFactory.cd();
 * System.out.println("CD allowed period: " + cdFine.getBorrowPeriodDays());
 * }</pre>
 *
 * @see FineStrategy
 * @see BaseFineStrategy
 * @see Config
 * @author Malak
 */
public final class FineStrategyFactory {

    /** Private constructor to prevent instantiation. */
    private FineStrategyFactory() {}


    /**
     * Creates a {@link FineStrategy} for books using configuration values.
     * <p>
     * Reads {@code fine.book.rate} and {@code fine.book.period} from {@link Config}.
     * Defaults to rate = 10.0 and period = 28 days if not found.
     * </p>
     *
     * @return a {@link FineStrategy} configured for books
     */
    public static FineStrategy book() {
        double rate = Config.getDouble("fine.book.rate", 10.0);
        int period  = Config.getInt("fine.book.period", 28);
        return new SimpleFineStrategy(BigDecimal.valueOf(rate), period);
    }

    /**
     * Creates a {@link FineStrategy} for CDs using configuration values.
     * <p>
     * Reads {@code fine.cd.rate} and {@code fine.cd.period} from {@link Config}.
     * Defaults to rate = 20.0 and period = 7 days if not found.
     * </p>
     *
     * @return a {@link FineStrategy} configured for CDs
     */
    public static FineStrategy cd() {
        double rate = Config.getDouble("fine.cd.rate", 20.0);
        int period  = Config.getInt("fine.cd.period", 7);
        return new SimpleFineStrategy(BigDecimal.valueOf(rate), period);
    }

    /**
     * Creates a {@link FineStrategy} for journals using configuration values.
     * <p>
     * Reads {@code fine.journal.rate} and {@code fine.journal.period} from {@link Config}.
     * Defaults to rate = 15.0 and period = 21 days if not found.
     * </p>
     *
     * @return a {@link FineStrategy} configured for journals
     */
    public static FineStrategy journal() {
        double rate = Config.getDouble("fine.journal.rate", 15.0);
        int period  = Config.getInt("fine.journal.period", 21);
        return new SimpleFineStrategy(BigDecimal.valueOf(rate), period);
    }

    /**
     * A simple implementation of {@link BaseFineStrategy} used internally
     * by the factory to provide fine calculation behavior.
     * <p>
     * This class is private and should not be used directly outside
     * the {@link FineStrategyFactory}.
     * </p>
     */
    private static final class SimpleFineStrategy extends BaseFineStrategy {
        private SimpleFineStrategy(BigDecimal ratePerDay, int borrowPeriodDays) {
            super(ratePerDay, borrowPeriodDays);
        }
    }
}
