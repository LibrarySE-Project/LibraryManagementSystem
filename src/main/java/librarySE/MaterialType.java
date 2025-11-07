package librarySE;

/**
 * Enum representing types of materials in the library.
 * <p>
 * Each material type is directly associated with its own {@link FineStrategy}.
 * This eliminates the need for a switch or factory class.
 * </p>
 *
 * @see FineStrategy
 * @see BookFineStrategy
 * @see CDFineStrategy
 * @see JournalFineStrategy
 */
public enum MaterialType {

    BOOK {
        @Override
        public FineStrategy createFineStrategy() {
            return new BookFineStrategy();
        }
    },
    CD {
        @Override
        public FineStrategy createFineStrategy() {
            return new CDFineStrategy();
        }
    },
    JOURNAL {
        @Override
        public FineStrategy createFineStrategy() {
            return new JournalFineStrategy();
        }
    };

    /**
     * Creates the appropriate {@link FineStrategy} for this material type.
     *
     * @return a new instance of {@link FineStrategy} for the type
     */
    public abstract FineStrategy createFineStrategy();
}

