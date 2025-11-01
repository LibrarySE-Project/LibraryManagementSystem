package librarySE;

import java.util.Objects;

/** 
 * Represents a book in the library system.
 * Each book has an ISBN, title, author, and availability status.
 * This class is used to manage and track books in the library system.
 * 
 * <p>The {@code borrow()} and {@code returnItem()} methods are thread-safe,
 * so this class can be safely used in a multi-threaded environment.</p>
 * 
 * <p>Setters are provided for title and author only, to allow correction of errors.
 * ISBN is immutable.</p>
 * 
 * @see LibraryItem
 * @see BorrowRecord
 * @see User 
 * @author Malak
 */
public class Book implements LibraryItem {

    /** The International Standard Book Number (ISBN) of the book, used to uniquely identify it. */
    private final String isbn;

    /** The title of the book. */
    private String title;

    /** The author of the book. */
    private String author;

    /** Indicates whether the book is currently available for borrowing. */
    private boolean available = true;

    /**
     * Constructs a new {@code Book} object with the specified details.
     * <p>
     * All parameters must be non-null and non-empty (cannot be empty string or only spaces). 
     * Leading and trailing spaces are trimmed automatically.
     * </p>
     *
     * @param isbn   the ISBN number of the book; must not be null or empty
     * @param title  the title of the book; must not be null or empty
     * @param author the author of the book; must not be null or empty
     * @throws IllegalArgumentException if {@code isbn}, {@code title}, or {@code author} is null or empty
     */
    public Book(String isbn, String title, String author) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be null or empty.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be null or empty.");
        }

        this.isbn = isbn.trim();
        this.title = title.trim();
        this.author = author.trim();
    }

    /** Returns the ISBN of the book. Immutable after creation. */
    public String getIsbn() {
        return isbn;
    }

    /** Returns the title of the book. */
    @Override
    public String getTitle() {
        return title;
    }

    /** Returns the author of the book. */
    public String getAuthor() {
        return author;
    }

    /** Sets a new title for the book. Cannot be null or empty. */
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty.");
        }
        this.title = title.trim();
    }

    /** Sets a new author for the book. Cannot be null or empty. */
    public void setAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be null or empty.");
        }
        this.author = author.trim();
    }

    /** Returns true if the book is available, false if it is borrowed. */
    @Override
    public boolean isAvailable() {
        synchronized (this) {
            return available;
        }
    }

    /** 
     * Attempts to borrow the book in a thread-safe manner.
     * 
     * @return {@code true} if the book was successfully borrowed,
     *         {@code false} if the book was already borrowed (not available)
     */
    @Override
    public boolean borrow() {
        synchronized (this) {
            if (!available) return false;
            available = false;
            return true;
        }
    }

    /** 
     * Returns the book in a thread-safe manner.
     * 
     * @return {@code true} if the book was successfully returned,
     *         {@code false} if the book was already available
     */
    @Override
    public boolean returnItem() {
        synchronized (this) {
            if (available) return false;
            available = true;
            return true;
        }
    }

    /** Two books are equal if they have the same ISBN. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Book b = (Book) obj;
        return isbn.equals(b.isbn);
    }

    /** Hash code based on ISBN. */
    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    /** String representation showing title, author, ISBN, and availability. */
    @Override
    public String toString() {
        synchronized (this) {
            return String.format("%s — %s (ISBN: %s) %s",
                    title, author, isbn, available ? "[AVAILABLE]" : "[BORROWED]");
        }
    }
    
    /**
     * Returns the material type of this item.
     * <p>
     * This implementation always returns {@link MaterialType#BOOK} because
     * this class represents a book.
     * </p>
     *
     * @return {@link MaterialType#BOOK}
     */
	@Override
	public MaterialType getMaterialType() {
		return MaterialType.BOOK;
	}
}

