package librarySE.core;

import java.math.BigDecimal;
import java.util.Objects;

import librarySE.utils.Config;
import librarySE.utils.ValidationUtils;

/**
 * Represents a Book in the library system, supporting multiple copies and
 * thread-safe borrowing/returning operations inherited from
 * {@link AbstractLibraryItem}.
 *
 * <p>Each book is uniquely identified by its ISBN and contains title, author,
 * and price metadata. The price may be explicitly provided or loaded from
 * configuration defaults. The class supports multiple physical copies of the
 * same book, ensuring accurate tracking of total and available copies.</p>
 *
 * <h3>Copy Management</h3>
 * <ul>
 *     <li>Total copies represent the physical quantity owned by the library.</li>
 *     <li>Available copies represent the currently borrowable count.</li>
 *     <li>Borrowing decreases available copies; returning increases them.</li>
 *     <li>A book is available if at least one copy is currently unborrowed.</li>
 * </ul>
 *
 * <h3>Concurrency</h3>
 * <p>
 * All public state-changing operations are synchronized to ensure safe
 * concurrent access consistent with the system architecture.
 * </p>
 *
 * @author Eman
 * 
 */
public class Book extends AbstractLibraryItem {

    private static final long serialVersionUID = 1L;

    /**
     * Unique International Standard Book Number identifying this book.
     */
    private final String isbn;

    /**
     * Human-readable title of this book.
     */
    private String title;

    /**
     * Name of the author of this book.
     */
    private String author;

    /**
     * Total number of physical copies of this book owned by the library.
     */
    private int totalCopies;

    /**
     * Number of copies currently available for borrowing.
     */
    private int availableCopies;

    /**
     * Creates a Book with validated ISBN, title, and author.
     *
     * @param isbn   non-empty ISBN string
     * @param title  non-empty book title
     * @param author non-empty author name
     * @throws IllegalArgumentException if any parameter is null or empty
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
     * Creates a Book with a single copy and smart price initialization.
     *
     * @param isbn   book ISBN
     * @param title  book title
     * @param author book author
     * @param price  explicit price, or configuration/default if null/zero
     */
    public Book(String isbn, String title, String author, BigDecimal price) {
        this(isbn, title, author);
        initCopies(1);
        initPrice(price);
    }

    /**
     * Creates a Book with a specified number of total copies and smart price initialization.
     *
     * @param isbn        book ISBN
     * @param title       book title
     * @param author      book author
     * @param price       explicit price, or configuration/default if null/zero
     * @param totalCopies total physical copies (> 0)
     * @throws IllegalArgumentException if totalCopies is not positive
     */
    public Book(String isbn, String title, String author, BigDecimal price, int totalCopies) {
        this(isbn, title, author);
        initCopies(totalCopies);
        initPrice(price);
    }

    /**
     * Initializes the price using smart price logic:
     * uses the provided positive price, or loads a default from configuration,
     * or falls back to zero.
     *
     * @param price optional explicit price
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
     * Initializes total and available copies to the same positive value.
     *
     * @param copies initial total copies (> 0)
     * @throws IllegalArgumentException if copies is not positive
     */
    private void initCopies(int copies) {
        if (copies <= 0) throw new IllegalArgumentException("Total copies must be > 0");
        this.totalCopies = copies;
        this.availableCopies = copies;
    }

    /**
     * Returns the ISBN of this book.
     *
     * @return ISBN string
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Returns the title of this book.
     *
     * @return book title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Returns the author of this book.
     *
     * @return author name
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Updates the book title after validation.
     *
     * @param t new title string
     * @throws IllegalArgumentException if the title is null or empty
     */
    public void setTitle(String t) {
        ValidationUtils.requireNonEmpty(t, "Title");
        this.title = t.trim();
    }

    /**
     * Updates the book author after validation.
     *
     * @param a new author name
     * @throws IllegalArgumentException if the author is null or empty
     */
    public void setAuthor(String a) {
        ValidationUtils.requireNonEmpty(a, "Author");
        this.author = a.trim();
    }

    /**
     * Returns the total number of physical copies of this book.
     *
     * @return total copies
     */
    public synchronized int getTotalCopies() {
        return totalCopies;
    }

    /**
     * Returns the number of currently available copies of this book.
     *
     * @return available copies
     */
    public synchronized int getAvailableCopies() {
        return availableCopies;
    }

    /**
     * Changes the total number of physical copies and adjusts the available
     * copies accordingly, ensuring the available count stays within [0, total].
     *
     * @param newTotal new total copies (> 0)
     * @throws IllegalArgumentException if newTotal is not positive
     */
    public synchronized void setTotalCopies(int newTotal) {
        if (newTotal <= 0) throw new IllegalArgumentException("Total copies must be > 0");
        int delta = newTotal - this.totalCopies;
        this.totalCopies = newTotal;
        this.availableCopies += delta;
        if (this.availableCopies > this.totalCopies) this.availableCopies = this.totalCopies;
        if (this.availableCopies < 0) this.availableCopies = 0;
    }

    /**
     * Returns the material type for this item.
     *
     * @return {@link MaterialType#BOOK}
     */
    @Override
    public MaterialType getMaterialType() {
        return MaterialType.BOOK;
    }

    /**
     * Checks whether the given keyword matches title, author, or ISBN
     * in a case-insensitive manner.
     *
     * @param keyword non-null search keyword
     * @return true if any field contains the keyword
     * @throws IllegalArgumentException if keyword is null or empty
     */
    @Override
    public boolean matchesKeyword(String keyword) {
        ValidationUtils.requireNonEmpty(keyword, "Keyword");
        String k = keyword.toLowerCase();
        return (title + " " + author + " " + isbn).toLowerCase().contains(k);
    }

    /**
     * Determines whether at least one copy of this book is available to borrow.
     */
    @Override
    protected boolean isAvailableInternal() {
        return availableCopies > 0;
    }

    /**
     * Borrows one copy of this book by decrementing the available count.
     *
     * @return {@code true} if the copy was successfully borrowed
     * @throws IllegalStateException if no copies are currently available
     */
    @Override
    protected boolean doBorrow() {
        if (availableCopies <= 0) {
            throw new IllegalStateException(
                    "No available copies of \"" + title + "\" to borrow."
            );
        }
        availableCopies--;
        return true;
    }

    /**
     * Returns one copy of this book to the library by incrementing
     * the available count, up to the totalCopies limit.
     *
     * @return {@code true} if the copy was successfully returned
     * @throws IllegalStateException if all copies are already in the library
     */
    @Override
    protected boolean doReturn() {
        if (availableCopies >= totalCopies) {
            throw new IllegalStateException(
                    "All copies of \"" + title + "\" are already in the library."
            );
        }
        availableCopies++;
        return true;
    }

    /**
     * Compares this book with another object for equality based on ISBN.
     *
     * @param obj object to compare
     * @return true if obj is a Book with the same ISBN
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Book b)) return false;
        return Objects.equals(isbn, b.isbn);
    }

    /**
     * Returns a hash code for this book derived from its ISBN.
     *
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    /**
     * Returns a formatted string representation of this book including
     * title, author, ISBN, price, copy counts, and availability.
     *
     * @return human-readable description string
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
