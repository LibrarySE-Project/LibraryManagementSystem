package librarySE.strategy;

import librarySE.utils.Config;

import java.math.BigDecimal;

/**
 * Central factory for creating {@link FineStrategy} instances
 * for all supported library material types (Book, CD, Journal).
 *
 * <p>
 * The factory hides all details about:
 * </p>
 * <ul>
 *     <li>Which configuration keys are used</li>
 *     <li>What business defaults are applied if keys are missing</li>
 *     <li>How fine-per-day and borrow-period are combined into one strategy</li>
 * </ul>
 *
 * <h3>Business rules (defaults)</h3>
 * These defaults are taken from the assignment user stories:
 * <ul>
 *     <li><b>Book</b>:
 *         <ul>
 *             <li>Borrow period = <b>28 days</b></li>
 *             <li>Overdue fine   = <b>10 NIS / day</b></li>
 *         </ul>
 *     </li>
 *     <li><b>CD</b>:
 *         <ul>
 *             <li>Borrow period = <b>7 days</b></li>
 *             <li>Overdue fine   = <b>20 NIS / day</b></li>
 *         </ul>
 *     </li>
 *     <li><b>Journal</b>:
 *         <ul>
 *             <li>Borrow period = <b>21 days</b></li>
 *             <li>Overdue fine   = <b>15 NIS / day</b></li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <h3>Configuration integration</h3>
 * <p>
 * All values are read from the central {@link Config} class, which
 * loads {@code data/config/fine-config.properties}. The following keys
 * are used (with the defaults above):
 * </p>
 * <ul>
 *     <li>{@code fine.book.rate}, {@code fine.book.period}</li>
 *     <li>{@code fine.cd.rate}, {@code fine.cd.period}</li>
 *     <li>{@code fine.journal.rate}, {@code fine.journal.period}</li>
 * </ul>
 * <p>
 * If a key is missing or invalid in the properties file, the factory
 * automatically falls back to the documented default value so that
 * the system remains robust.
 * </p>
 *
 * <h3>Typical usage</h3>
 * <pre>{@code
 * // Book: 28 days, 10 NIS per overdue day
 * FineStrategy bookStrategy = FineStrategyFactory.book();
 *
 * // CD: 7 days, 20 NIS per overdue day
 * FineStrategy cdStrategy   = FineStrategyFactory.cd();
 *
 * // Journal: 21 days, 15 NIS per overdue day
 * FineStrategy journalStrategy = FineStrategyFactory.journal();
 * }</pre>
 *
 * @author Malak
 */
public final class FineStrategyFactory {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private FineStrategyFactory() {
        // not meant to be instantiated
    }

    // ---------------------------------------------------------------------
    // Book strategy
    // ---------------------------------------------------------------------

    /**
     * Creates a {@link FineStrategy} configured for <b>books</b>.
     *
     * <p>Configuration keys:</p>
     * <ul>
     *   <li>{@code fine.book.rate}   – fine per overdue day (default: 10.0)</li>
     *   <li>{@code fine.book.period} – allowed borrow period in days (default: 28)</li>
     * </ul>
     *
     * @return a {@link FineStrategy} whose rate and period match the
     *         business rules for books
     */
    public static FineStrategy book() {
        double rate = Config.getDouble("fine.book.rate", 10.0);
        int period  = Config.getInt("fine.book.period", 28);
        return new SimpleFineStrategy(BigDecimal.valueOf(rate), period);
    }

    // ---------------------------------------------------------------------
    // CD strategy
    // ---------------------------------------------------------------------

    /**
     * Creates a {@link FineStrategy} configured for <b>CDs</b>.
     *
     * <p>Configuration keys:</p>
     * <ul>
     *   <li>{@code fine.cd.rate}   – fine per overdue day (default: 20.0)</li>
     *   <li>{@code fine.cd.period} – allowed borrow period in days (default: 7)</li>
     * </ul>
     *
     * @return a {@link FineStrategy} whose rate and period match the
     *         business rules for CDs
     */
    public static FineStrategy cd() {
        double rate = Config.getDouble("fine.cd.rate", 20.0);
        int period  = Config.getInt("fine.cd.period", 7);
        return new SimpleFineStrategy(BigDecimal.valueOf(rate), period);
    }

    // ---------------------------------------------------------------------
    // Journal strategy
    // ---------------------------------------------------------------------

    /**
     * Creates a {@link FineStrategy} configured for <b>journals</b>.
     *
     * <p>Configuration keys:</p>
     * <ul>
     *   <li>{@code fine.journal.rate}   – fine per overdue day (default: 15.0)</li>
     *   <li>{@code fine.journal.period} – allowed borrow period in days (default: 21)</li>
     * </ul>
     *
     * @return a {@link FineStrategy} whose rate and period match the
     *         business rules for journals
     */
    public static FineStrategy journal() {
        double rate = Config.getDouble("fine.journal.rate", 15.0);
        int period  = Config.getInt("fine.journal.period", 21);
        return new SimpleFineStrategy(BigDecimal.valueOf(rate), period);
    }

    // ---------------------------------------------------------------------
    // Internal implementation
    // ---------------------------------------------------------------------

    /**
     * Simple concrete implementation of {@link BaseFineStrategy} used by
     * the factory methods above.
     *
     * <p>
     * It does not add any behavior beyond {@link BaseFineStrategy}; it only
     * exposes a public constructor inside this file so that the factory
     * can create properly configured strategy instances.
     * </p>
     */
    private static final class SimpleFineStrategy extends BaseFineStrategy {

        /**
         * Creates a new fine strategy with a fixed daily rate and a fixed
         * borrow period in days.
         *
         * @param ratePerDay       fine amount per overdue day
         * @param borrowPeriodDays allowed number of days before an item is
         *                         considered overdue
         */
        private SimpleFineStrategy(BigDecimal ratePerDay, int borrowPeriodDays) {
            super(ratePerDay, borrowPeriodDays);
        }
    }
}
