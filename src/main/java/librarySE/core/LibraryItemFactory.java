package librarySE.core;


import java.math.BigDecimal;
import librarySE.utils.Config;

/**
 * Factory class for creating {@link LibraryItem} objects dynamically
 * based on their {@link MaterialType}.
 *
 * <p>
 * This factory supports smart price initialization:
 * <ul>
 *   <li>If a price argument is provided → it will be used directly.</li>
 *   <li>If no price is provided → a default value will be loaded from {@link Config}.</li>
 * </ul>
 * </p>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * // Create a Book with explicit price
 * LibraryItem book1 = LibraryItemFactory.create(
 *     MaterialType.BOOK, "9780134685991", "Effective Java", "Joshua Bloch", "79.99"
 * );
 *
 * // Create a Book with default price from Config
 * LibraryItem book2 = LibraryItemFactory.create(
 *     MaterialType.BOOK, "9780134685991", "Effective Java", "Joshua Bloch"
 * );
 * }</pre>
 *
 * @author Malak
 */
public final class LibraryItemFactory {

    /** Private constructor to prevent instantiation. */
    private LibraryItemFactory() {}

    /**
     * Creates a new {@link LibraryItem} instance according to its material type.
     * <p>
     * The arguments expected per type are:
     * <ul>
     *   <li><b>BOOK:</b> ISBN, Title, Author, [Optional Price]</li>
     *   <li><b>CD:</b> Title, Artist, [Optional Price]</li>
     *   <li><b>JOURNAL:</b> Title, Editor, Issue, [Optional Price]</li>
     * </ul>
     * </p>
     *
     * @param type the {@link MaterialType} (BOOK, CD, JOURNAL)
     * @param args parameters required for that type
     * @return a properly initialized {@link LibraryItem} instance
     * @throws IllegalArgumentException if arguments are missing or invalid
     */
    public static LibraryItem create(MaterialType type, String... args) {
        return switch (type) {
            case BOOK -> {
                BigDecimal price = (args.length > 3)
                    ? new BigDecimal(args[3])
                    : BigDecimal.valueOf(Config.getDouble("price.book.default", 0.0));
                yield new Book(args[0], args[1], args[2], price);
            }

            case CD -> {
                BigDecimal price = (args.length > 2)
                    ? new BigDecimal(args[2])
                    : BigDecimal.valueOf(Config.getDouble("price.cd.default", 0.0));
                yield new CD(args[0], args[1], price);
            }

            case JOURNAL -> {
                BigDecimal price = (args.length > 3)
                    ? new BigDecimal(args[3])
                    : BigDecimal.valueOf(Config.getDouble("price.journal.default", 0.0));
                yield new Journal(args[0], args[1], args[2], price);
            }
        };
    }
}
