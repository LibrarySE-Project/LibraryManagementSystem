package librarySE.core;

import java.math.BigDecimal;

/**
 * Factory for constructing {@link LibraryItem} objects:
 * {@link Book}, {@link CD}, and {@link Journal}.
 *
 * <p>The factory supports:</p>
 * <ul>
 *     <li>Creating items with default or explicit price.</li>
 *     <li>Creating items with single or multiple copies.</li>
 *     <li>A legacy varargs creator for backward compatibility.</li>
 * </ul>
 *
 * <p>Price parsing returns {@code null} for empty text, allowing
 * item classes to apply their configured default price.</p>
 *
 * @author Malak
 */
public final class LibraryItemFactory {

    /** Prevents instantiation. */
    private LibraryItemFactory() {}

    private static BigDecimal parsePrice(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            return null;
        }
        BigDecimal price = new BigDecimal(priceText.trim());
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be >= 0.");
        }
        return price;
    }

    /** Creates a Book with default price and a single copy. */
    public static LibraryItem createBook(String isbn, String title, String author) {
        return createBook(isbn, title, author, null, 1);
    }

    /** Creates a Book with explicit price and a single copy. */
    public static LibraryItem createBook(String isbn,
                                         String title,
                                         String author,
                                         String priceText) {
        return createBook(isbn, title, author, priceText, 1);
    }

    /** Creates a Book with default price and multiple copies. */
    public static LibraryItem createBook(String isbn,
                                         String title,
                                         String author,
                                         int totalCopies) {
        return createBook(isbn, title, author, null, totalCopies);
    }

    /** Creates a Book with explicit price and multiple copies. */
    public static LibraryItem createBook(String isbn,
                                         String title,
                                         String author,
                                         String priceText,
                                         int totalCopies) {
        BigDecimal price = parsePrice(priceText);
        return new Book(isbn, title, author, price, totalCopies);
    }

    /** Creates a CD with default price and a single copy. */
    public static LibraryItem createCd(String title, String artist) {
        return createCd(title, artist, null, 1);
    }

    /** Creates a CD with explicit price and a single copy. */
    public static LibraryItem createCd(String title,
                                       String artist,
                                       String priceText) {
        return createCd(title, artist, priceText, 1);
    }

    /** Creates a CD with default price and multiple copies. */
    public static LibraryItem createCd(String title,
                                       String artist,
                                       int totalCopies) {
        return createCd(title, artist, null, totalCopies);
    }

    /** Creates a CD with explicit price and multiple copies. */
    public static LibraryItem createCd(String title,
                                       String artist,
                                       String priceText,
                                       int totalCopies) {
        BigDecimal price = parsePrice(priceText);
        return new CD(title, artist, price, totalCopies);
    }

    /** Creates a Journal with default price and a single copy. */
    public static LibraryItem createJournal(String title,
                                            String editor,
                                            String issue) {
        return createJournal(title, editor, issue, null, 1);
    }

    /** Creates a Journal with explicit price and a single copy. */
    public static LibraryItem createJournal(String title,
                                            String editor,
                                            String issue,
                                            String priceText) {
        return createJournal(title, editor, issue, priceText, 1);
    }

    /** Creates a Journal with default price and multiple copies. */
    public static LibraryItem createJournal(String title,
                                            String editor,
                                            String issue,
                                            int totalCopies) {
        return createJournal(title, editor, issue, null, totalCopies);
    }

    /** Creates a Journal with explicit price and multiple copies. */
    public static LibraryItem createJournal(String title,
                                            String editor,
                                            String issue,
                                            String priceText,
                                            int totalCopies) {
        BigDecimal price = parsePrice(priceText);
        return new Journal(title, editor, issue, price, totalCopies);
    }

    /**
     * Legacy varargs creator for backward compatibility.
     * Always creates exactly one copy.
     *
     * @param type item type
     * @param args parameters required by the specific type
     * @return created {@link LibraryItem}
     * @deprecated This method is kept only for backward compatibility and will be removed in a future version.
     */
    @Deprecated
    public static LibraryItem create(MaterialType type, String... args) {
        return switch (type) {
            case BOOK -> {
                String isbn = args[0];
                String title = args[1];
                String author = args[2];
                String priceText = (args.length > 3) ? args[3] : null;
                yield createBook(isbn, title, author, priceText, 1);
            }
            case CD -> {
                String title = args[0];
                String artist = args[1];
                String priceText = (args.length > 2) ? args[2] : null;
                yield createCd(title, artist, priceText, 1);
            }
            case JOURNAL -> {
                String title = args[0];
                String editor = args[1];
                String issue = args[2];
                String priceText = (args.length > 3) ? args[3] : null;
                yield createJournal(title, editor, issue, priceText, 1);
            }
        };
    }
}
