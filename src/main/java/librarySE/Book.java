package librarySE;

import java.util.Objects;

/**
 * Represents a book in the library system.
 * <p>
 * Each book has a unique ISBN, a title, an author, and an availability status.
 * This class implements {@link LibraryItem} and supports borrowing and returning.
 * </p>
 *
 * <p>
 * Thread-safety is ensured for operations that modify or read the availability status.
 * </p>
 *
 * @author Malak
 * @see LibraryItem
 * @see MaterialType
 */
public class Book implements LibraryItem {

    /** Unique International Standard Book Number (ISBN) for the book. */
    private final String isbn;

    /** The title of the book. */
    private String title;

    /** The author of the book. */
    private String author;

    /** Availability status of the book; true if available for borrowing. */
    private boolean available = true;

    /**
     * Constructs a new {@code Book} with the given ISBN, title, and author.
     *
     * @param isbn the unique ISBN of the book; must not be null or empty
     * @param title the title of the book; must not be null or empty
     * @param author the author of the book; must not be null or empty
     * @throws IllegalArgumentException if any parameter is null or empty
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
     * Returns the ISBN of the book.
     *
     * @return the ISBN as a {@link String}
     */
    public String getIsbn() { return isbn; }

    /**
     * Returns the title of the book.
     *
     * @return the title
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
     * @param title the new title; must not be null or empty
     * @throws IllegalArgumentException if title is null or empty
     */
    public void setTitle(String title) {
        validateNonEmpty(title, "Title");
        this.title = title.trim();
    }

    /**
     * Sets a new author for the book.
     *
     * @param author the new author; must not be null or empty
     * @throws IllegalArgumentException if author is null or empty
     */
    public void setAuthor(String author) {
        validateNonEmpty(author, "Author");
        this.author = author.trim();
    }

    /**
     * Checks if the book is available for borrowing.
     *
     * @return {@code true} if available, {@code false} if currently borrowed
     */
    @Override
    public boolean isAvailable() { synchronized(this) { return available; } }

    /**
     * Marks the book as borrowed if it is available.
     *
     * @return {@code true} if the book was successfully borrowed,
     *         {@code false} if it was already borrowed
     */
    @Override
    public boolean borrow() { synchronized(this) { if(!available) return false; available=false; return true; } }

    /**
     * Marks the book as returned if it was borrowed.
     *
     * @return {@code true} if the book was successfully returned,
     *         {@code false} if it was already available
     */
    @Override
    public boolean returnItem() { synchronized(this) { if(available) return false; available=true; return true; } }

    /**
     * Returns the material type of this item.
     *
     * @return {@link MaterialType#BOOK}
     */
    @Override
    public MaterialType getMaterialType() { return MaterialType.BOOK; }

    /**
     * Compares this book to another object for equality.
     * <p>
     * Two books are considered equal if they have the same ISBN.
     * </p>
     *
     * @param obj the object to compare
     * @return {@code true} if the other object is a {@code Book} with the same ISBN
     */
    @Override
    public boolean equals(Object obj) {
        if(this==obj) return true;
        if(!(obj instanceof Book)) return false;
        return isbn.equals(((Book)obj).isbn);
    }

    /**
     * Returns the hash code for this book based on its ISBN.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() { return Objects.hash(isbn); }

    /**
     * Returns a string representation of the book, including title, author, ISBN, and availability.
     *
     * @return formatted string representing the book
     */
    @Override
    public String toString() {
        synchronized(this) {
            return String.format("%s — %s (ISBN: %s) %s",
                    title, author, isbn, available ? "[AVAILABLE]" : "[BORROWED]");
        }
    }

    /**
     * Validates that a string is not null or empty.
     *
     * @param value the string to check
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if value is null or empty
     */
    private void validateNonEmpty(String value, String fieldName) {
        if(value==null || value.trim().isEmpty())
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
    }
    /**
     * Checks if the book matches the given keyword.
     * <p>
     * A book matches if the keyword is found (case-insensitive) in the title,
     * author name, or ISBN.
     * </p>
     *
     * @param keyword the keyword to search for; must not be null
     * @return {@code true} if the book matches the keyword, {@code false} otherwise
     * @throws IllegalArgumentException if {@code keyword} is null
     */
    @Override
    public boolean matchesKeyword(String keyword) {
        if (keyword == null) {
            throw new IllegalArgumentException("Keyword cannot be null");
        }
        String lower = keyword.toLowerCase();
        return title.toLowerCase().contains(lower)
            || author.toLowerCase().contains(lower)
            || isbn.toLowerCase().contains(lower);
    }

}


