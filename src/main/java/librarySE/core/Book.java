package librarySE.core;

import java.math.BigDecimal;
import java.util.Objects;

import librarySE.utils.Config;
import librarySE.utils.ValidationUtils;

/**
 * Represents a {@link LibraryItem} of type <b>Book</b> in the library system.
 * <p>
 * Each book has a unique ISBN identifier, a title, an author, and an optional price.
 * It inherits core functionality such as borrowing, returning, and price management
 * from {@link AbstractLibraryItem}, ensuring consistent thread-safe behavior across all
 * library materials.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Encapsulates essential metadata for books (ISBN, title, author, price).</li>
 *   <li>Supports keyword-based search across title, author, and ISBN fields.</li>
 *   <li>Ensures validation for non-empty fields and safe operations.</li>
 *   <li>Uses {@link ValidationUtils} for consistent data validation.</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * Book book = new Book("978-0134685991", "Effective Java", "Joshua Bloch", BigDecimal.valueOf(59.99));
 * System.out.println(book.getTitle());     // "Effective Java"
 * System.out.println(book.isAvailable());  // true
 * book.borrow();
 * System.out.println(book.isAvailable());  // false
 * }</pre>
 *
 * @author Eman
 * @see AbstractLibraryItem
 * @see MaterialType
 * @see ValidationUtils
 */
public class Book extends AbstractLibraryItem {

    /** Serialization identifier for version consistency. */
	private static final long serialVersionUID = 1L;

	/** Unique International Standard Book Number (ISBN). */
    private final String isbn;

    /** The title of the book. */
    private String title;

    /** The author of the book. */
    private String author;

    /**
     * Initializes a {@code Book} instance with validated metadata (ISBN, title, and author).
     * <p>
     * This constructor is used internally by other constructors to ensure
     * consistent validation and initialization of core fields.
     * The default price is initialized to {@code 0.00} and can later be set
     * through higher-level constructors.
     * </p>
     *
     * @param isbn   the unique ISBN of the book (non-null and non-empty)
     * @param title  the title of the book (non-null and non-empty)
     * @param author the author of the book (non-null and non-empty)
     * @throws IllegalArgumentException if any argument is invalid
     * @implNote This constructor is private and should not be called directly.
     */
    private Book(String isbn, String title, String author) {
        ValidationUtils.requireNonEmpty(isbn, "ISBN");
        ValidationUtils.requireNonEmpty(title, "Title");
        ValidationUtils.requireNonEmpty(author, "Author");
        this.isbn = isbn.trim();
        this.title = title.trim();
        this.author = author.trim();
    }

    /**
     * Constructs a new {@code Book} with validated metadata and a defined or automatically loaded price.
     * <p>
     * Implements the <b>Smart Price Logic</b>:
     * <ul>
     *   <li>If a positive {@code price} is provided → it will be used directly.</li>
     *   <li>If {@code price} is {@code null} or zero → the constructor automatically loads
     *       the default price from {@link Config} using key {@code "price.book.default"}.</li>
     *   <li>If no configuration value is found → the price defaults to {@code 0.00}.</li>
     * </ul>
     * </p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * Book b1 = new Book("9780134685991", "Effective Java", "Joshua Bloch", BigDecimal.valueOf(79.99));
     * Book b2 = new Book("9780134685991", "Effective Java", "Joshua Bloch", BigDecimal.ZERO);
     * }</pre>
     *
     * @param isbn   the unique ISBN of the book (non-null and non-empty)
     * @param title  the title of the book (non-null and non-empty)
     * @param author the author of the book (non-null and non-empty)
     * @param price  the price of the book; if {@code null} or zero, loads default from Config
     * @throws IllegalArgumentException if any string argument is invalid
     */
    public Book(String isbn, String title, String author, BigDecimal price) {
        this(isbn, title, author);
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            setPrice(price);
        } else {
            double defaultPrice = Config.getDouble("price.book.default", 0.0);
            setPrice(BigDecimal.valueOf(defaultPrice));
        }
    }

    /** Returns the unique ISBN of this book. */
    public String getIsbn() {
        return isbn;
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return title;
    }

    /** Returns the author of this book. */
    public String getAuthor() {
        return author;
    }

    /**
     * Updates the book's title after validation.
     *
     * @param t the new title (non-null and non-empty)
     * @throws IllegalArgumentException if title is invalid
     */
    public void setTitle(String t) {
        ValidationUtils.requireNonEmpty(t, "Title");
        this.title = t.trim();
    }

    /**
     * Updates the book's author after validation.
     *
     * @param a the new author (non-null and non-empty)
     * @throws IllegalArgumentException if author is invalid
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
     * Checks if the given keyword matches any of the book's metadata fields.
     * <p>
     * The search is case-insensitive and includes title, author, and ISBN.
     * </p>
     *
     * @param keyword the search term (non-null)
     * @return {@code true} if the keyword matches any field, otherwise {@code false}
     * @throws IllegalArgumentException if keyword is {@code null}
     */
    @Override
    public boolean matchesKeyword(String keyword) {
    	ValidationUtils.requireNonEmpty(keyword, "Keyword");
        String k = keyword.toLowerCase();
        return (title + " " + author + " " + isbn).toLowerCase().contains(k);
    }

    /**
     * Compares two books for equality based on their ISBN.
     *
     * @param o the object to compare with
     * @return {@code true} if both books have the same ISBN
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Book b)) return false;
        return Objects.equals(isbn, b.isbn);
    }


    /** Returns a hash code based on the book's ISBN. */
    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    /**
     * Returns a human-readable string representation of the book,
     * including its title, author, ISBN, price, and availability status.
     *
     * @return a formatted description of this book
     */
    @Override
    public String toString() {
        return "[BOOK] %s by %s (ISBN:%s, Price:%s) — %s".formatted(
                title, author, isbn,
                getPrice().toPlainString(),
                isAvailable() ? "Available" : "Borrowed"
        );
    }
}
