package librarySE;

import java.util.Objects;

/**
 * Represents a {@link LibraryItem} of type book in the library system.
 * <p>
 * Each {@code Book} has a unique ISBN identifier, a title, and an author.
 * It inherits core functionality such as borrowing, returning, and availability
 * management from {@link AbstractLibraryItem}, ensuring consistent behavior
 * across all library materials.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *     <li>Encapsulates basic metadata for books (ISBN, title, author).</li>
 *     <li>Supports borrowing and returning through inherited thread-safe methods.</li>
 *     <li>Provides keyword-based search capability across all attributes.</li>
 * </ul>
 *
 * <p>
 * This class follows the <b>Single Responsibility Principle</b> — it focuses solely
 * on representing a book and its identifying information, while borrowing logic
 * resides in {@link AbstractLibraryItem} and {@link BorrowManager}.
 * </p>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * Book book = new Book("978-0134685991", "Effective Java", "Joshua Bloch");
 * System.out.println(book.getTitle());        // "Effective Java"
 * System.out.println(book.isAvailable());    // true
 * book.borrow();
 * System.out.println(book.isAvailable());    // false
 * }</pre>
 *
 * @see LibraryItem
 * @see MaterialType
 * @see AbstractLibraryItem
 * @see BorrowManager
 * @see FineContext
 * @see BookFineStrategy
 * 
 * @author Malak
 */
public class Book extends AbstractLibraryItem {

    /** Unique International Standard Book Number (ISBN). */
    private final String isbn;

    /** The title of the book. */
    private String title;

    /** The author of the book. */
    private String author;

    /**
     * Constructs a new {@code Book} instance with the specified details.
     *
     * @param isbn   the unique ISBN of the book; must not be {@code null} or empty
     * @param title  the title of the book; must not be {@code null} or empty
     * @param author the author of the book; must not be {@code null} or empty
     * @throws IllegalArgumentException if any parameter is {@code null} or empty
     */
    public Book(String isbn, String title, String author) {
        validateNonEmpty(isbn, "ISBN");
        validateNonEmpty(title, "Title");
        validateNonEmpty(author, "Author");

        this.isbn = isbn.trim();
        this.title = title.trim();
        this.author = author.trim();
    }

    /**
     * Returns the unique ISBN of this book.
     *
     * @return the ISBN as a {@link String}
     */
    public String getIsbn() { return isbn; }

    /**
     * Returns the title of the book.
     *
     * @return the book title
     */
    @Override
    public String getTitle() { return title; }

    /**
     * Returns the author of the book.
     *
     * @return the author's name
     */
    public String getAuthor() { return author; }

    /**
     * Sets a new title for the book.
     *
     * @param title the new title; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code title} is {@code null} or empty
     */
    public void setTitle(String title) {
        validateNonEmpty(title, "Title");
        this.title = title.trim();
    }

    /**
     * Sets a new author name for the book.
     *
     * @param author the new author's name; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code author} is {@code null} or empty
     */
    public void setAuthor(String author) {
        validateNonEmpty(author, "Author");
        this.author = author.trim();
    }

    /**
     * Returns the material type of this item.
     *
     * @return {@link MaterialType#BOOK}
     */
    @Override
    public MaterialType getMaterialType() { return MaterialType.BOOK; }

    /**
     * Checks if this book matches the given keyword.
     * <p>
     * A match occurs if the keyword (case-insensitive) appears in
     * the title, author, or ISBN.
     * </p>
     *
     * @param keyword the keyword to search for; must not be {@code null}
     * @return {@code true} if this book matches the keyword; {@code false} otherwise
     * @throws IllegalArgumentException if {@code keyword} is {@code null}
     */
    @Override
    public boolean matchesKeyword(String keyword) {
        if (keyword == null)
            throw new IllegalArgumentException("Keyword cannot be null.");
        String lower = keyword.toLowerCase();
        return String.join(" ", title, author, isbn).toLowerCase().contains(lower);
    }

    /**
     * Compares this book to another object for equality.
     * <p>
     * Two books are considered equal if they share the same ISBN.
     * </p>
     *
     * @param obj the object to compare with
     * @return {@code true} if the other object is a {@code Book} with the same ISBN
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Book book && isbn.equals(book.isbn);
    }

    /**
     * Returns a hash code value for this book based on its ISBN.
     *
     * @return the hash code for this book
     */
    @Override
    public int hashCode() { return Objects.hash(isbn); }

    /**
     * Returns a formatted string representation of the book,
     * including its title, author, ISBN, and availability status.
     *
     * @return a human-readable string describing this book
     */
    @Override
    public String toString() {
        return String.format("[BOOK] %s by %s (ISBN: %s) — %s",
                title, author, isbn, isAvailable() ? "Available" : "Borrowed");
    }
}

