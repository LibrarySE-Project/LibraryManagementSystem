package librarySE.core;

import librarySE.strategy.FineStrategy;
import librarySE.strategy.FineStrategyFactory;


/**
 * Enumeration representing the various material types available in the library.
 * <p>
 * Each {@code MaterialType} corresponds to a specific category of library item
 * (e.g., book, CD, journal) and defines how its fines are calculated by delegating
 * to a matching {@link FineStrategy} implementation.
 * </p>
 *
 * <h3>Design Pattern Integration:</h3>
 * <ul>
 *   <li><b>Strategy Pattern:</b> Each type returns its own fine calculation strategy.</li>
 *   <li><b>Factory Method:</b> Uses {@link FineStrategyFactory} to instantiate
 *       the correct {@link FineStrategy} for the material type.</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * MaterialType type = MaterialType.CD;
 * FineStrategy fine = type.createFineStrategy();
 * int fineAmount = fine.calculateFine(3); // e.g. 3 days overdue × 20 NIS/day = 60 NIS
 * }</pre>
 *
 * @author Eman
 * 
 */
public enum MaterialType {

    /**
     * Represents a standard printed book material.
     * <p>
     * Default fine rate: 10 NIS/day, Borrow period: 28 days.
     * </p>
     */
    BOOK {
        @Override
        public FineStrategy createFineStrategy() {
            return FineStrategyFactory.book();
        }
    },

    /**
     * Represents an audio CD material.
     * <p>
     * Default fine rate: 20 NIS/day, Borrow period: 7 days.
     * </p>
     */
    CD {
        @Override
        public FineStrategy createFineStrategy() {
            return FineStrategyFactory.cd();
        }
    },

    /**
     * Represents a journal or periodical material.
     * <p>
     * Default fine rate: 15 NIS/day, Borrow period: 21 days.
     * </p>
     */
    JOURNAL {
        @Override
        public FineStrategy createFineStrategy() {
            return FineStrategyFactory.journal();
        }
    };

    /**
     * Factory method to create the appropriate {@link FineStrategy}
     * instance for this material type.
     *
     * @return a {@link FineStrategy} defining fine calculation rules for the type
     */
    public abstract FineStrategy createFineStrategy();
}

