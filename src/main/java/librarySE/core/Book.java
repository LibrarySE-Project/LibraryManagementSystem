package librarySE.core;

import java.math.BigDecimal;
import java.util.Objects;

import librarySE.utils.Config;
import librarySE.utils.ValidationUtils;

/**
 * Represents a Book item in the library system.
 * <p>
 * A {@code Book} is uniquely identified by its ISBN and contains metadata such
 * as title, author, and price. The class extends {@link AbstractLibraryItem},
 * inheriting full copy-tracking functionality including borrowing and returning.
 * </p>
 *
 * <h2>Main Characteristics</h2>
 * <ul>
 *     <li>Each book has a unique ISBN.</li>
 *     <li>Supports multiple physical copies of the same title.</li>
 *     <li>Provides synchronized borrow/return operations for thread safety.</li>
 *     <li>Price can be supplied or loaded from configuration defaults.</li>
 * </ul>
 *
 * <h2>Copy Management</h2>
 * <ul>
 *     <li>Total copies = number of physical units owned by the library.</li>
 *     <li>Available copies = number of units currently not borrowed.</li>
 *     <li>A book is available when availableCopies ≥ 1.</li>
 * </ul>
 *
 * <h2>Concurrency Notes</h2>
 * <p>
 * All mutations to copy-related state are synchronized in the parent class
 * to guarantee safe concurrent access in multi-threaded contexts.
 * </p>
 *
 * @author eman
 */
public class Book extends AbstractLibraryItem {

    private static final long serialVersionUID = 1L;

    private final String isbn;
    private String title;
    private String author;

    /**
     * Private helper constructor that initializes core identity fields and copy count.
     *
     * @param isbn        the ISBN of the book, must not be blank
     * @param title       the title of the book, must not be blank
     * @param author      the author name, must not be blank
     * @param totalCopies the number of physical copies; must be &ge; 1
     * @throws IllegalArgumentException if any string is blank or if totalCopies is invalid
     */
    private Book(String isbn, String title, String author, int totalCopies) {
        super(totalCopies);
        ValidationUtils.requireNonEmpty(isbn, "ISBN");
        ValidationUtils.requireNonEmpty(title, "Title");
        ValidationUtils.requireNonEmpty(author, "Author");
        this.isbn = isbn.trim();
        this.title = title.trim();
        this.author = author.trim();
    }

    /**
     * Creates a {@code Book} with exactly one copy.
     *
     * @param isbn   unique ISBN identifier, must not be blank
     * @param title  the book title, must not be blank
     * @param author the author name, must not be blank
     * @param price  the price; if null or invalid, a configured default is loaded
     */
    public Book(String isbn, String title, String author, BigDecimal price) {
        this(isbn, title, author, 1);
        initPrice(price);
    }

    /**
     * Creates a {@code Book} with a specified number of total copies.
     *
     * @param isbn        unique ISBN identifier, must not be blank
     * @param title       the book title, must not be blank
     * @param author      the author name, must not be blank
     * @param price       the price; if null or invalid, a configured default is loaded
     * @param totalCopies number of physical copies; must be &ge; 1
     */
    public Book(String isbn, String title, String author, BigDecimal price, int totalCopies) {
        this(isbn, title, author, totalCopies);
        initPrice(price);
    }

    /**
     * Initializes the price of the book.
     * <p>
     * If a positive price is provided, it is used directly.
     * Otherwise, the value of:
     * <pre>price.book.default</pre>
     * is loaded from configuration.
     * </p>
     *
     * @param price the provided price value, or {@code null} to trigger default loading
     */
    private void initPrice(BigDecimal price) {
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            setPrice(price);
        } else {
            double defaultPrice = Config.getDouble("price.book.default", 0.0);
            setPrice(BigDecimal.valueOf(defaultPrice));
        }
    }

    /**
     * Returns the ISBN of this book.
     *
     * @return the ISBN string (never null)
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Returns the book title.
     *
     * @return non-null title string
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Returns the author name.
     *
     * @return non-null author name string
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Updates the title of the book.
     *
     * @param t the new title; must not be blank
     * @throws IllegalArgumentException if {@code t} is empty
     */
    public void setTitle(String t) {
        ValidationUtils.requireNonEmpty(t, "Title");
        this.title = t.trim();
    }

    /**
     * Updates the author name.
     *
     * @param a the new author; must not be blank
     * @throws IllegalArgumentException if {@code a} is empty
     */
    public void setAuthor(String a) {
        ValidationUtils.requireNonEmpty(a, "Author");
        this.author = a.trim();
    }

    /**
     * Returns the material type of this item.
     *
     * @return {@link MaterialType#BOOK}
     */
    @Override
    public MaterialType getMaterialType() {
        return MaterialType.BOOK;
    }

    /**
     * Determines whether this book matches a search keyword.
     * <p>
     * Matching fields include:
     * <ul>
     *     <li>title</li>
     *     <li>author</li>
     *     <li>ISBN</li>
     * </ul>
     * </p>
     *
     * @param keyword search keyword; must not be blank
     * @return {@code true} if the keyword appears in any searchable field
     * @throws IllegalArgumentException if keyword is empty
     */
    @Override
    public boolean matchesKeyword(String keyword) {
        ValidationUtils.requireNonEmpty(keyword, "Keyword");
        String k = keyword.toLowerCase();
        return (title + " " + author + " " + isbn).toLowerCase().contains(k);
    }

    /**
     * Returns the display name used in notifications/messages.
     *
     * @return the book title
     */
    @Override
    protected String getDisplayNameForMessages() {
        return title;
    }

    /**
     * Two books are equal if they share the same ISBN.
     *
     * @param obj the object to compare
     * @return {@code true} if both objects represent the same ISBN
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Book b)) return false;
        return Objects.equals(isbn, b.isbn);
    }

    /**
     * Computes hash based on the unique ISBN.
     *
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    /**
     * Returns a formatted string containing book metadata and availability status.
     *
     * @return human-readable description of this book
     */
    @Override
    public String toString() {
        return "[BOOK] %s by %s (ISBN:%s, Price:%s, Available:%d/%d) — %s".formatted(
                title, author, isbn,
                getPrice().toPlainString(),
                getAvailableCopies(),
                getTotalCopies(),
                isAvailable() ? "Available" : "Fully borrowed"
        );
    }
}
