package librarySE;

import java.util.Objects;

/** 
 * Represents a book in the library system.
 * Each book has an ISBN, title, author, and availability status.
 * This class is used to manage and track books in the library system.
 * 
 * <p>The {@code borrow()} and {@code returnBook()} methods are thread-safe,
 * so this class can be safely used in a multi-threaded environment.</p>
 * 
 * @author Malak
 */
public class Book {
    /** The International Standard Book Number (ISBN) of the book, used to uniquely identify it. */
    private final String isbn;

    /** The title of the book. */
    private final String title;

    /** The author of the book. */
    private final String author;

    /** Indicates whether the book is currently available for borrowing. 
     *  True if available, false if it is borrowed.
     */
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
     *
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

    /**
     * Returns the book's ISBN.
     * 
     * @return the book's ISBN
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Returns the book's title.
     * 
     * @return the book's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the author's name.
     * 
     * @return the author's name
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns whether the book is currently available for borrowing.
     * <p>
     * Note: The availability may change immediately after this call
     * if another thread borrows or returns the book.
     * </p>
     * 
     * @return true if available, false if borrowed
     */
    public boolean isAvailable() {
        synchronized (this) {
            return available;
        }
    }

    /**
     * Attempts to borrow the book in a thread-safe manner.
     * <p>
     * If the book is already borrowed, returns false.
     * Otherwise, marks the book as borrowed and returns true.
     * </p>
     *
     * @return {@code true} if the book was successfully borrowed, {@code false} otherwise
     */
    public boolean borrow() {
        synchronized (this) {
            if (!available) return false;
            available = false;
            return true;
        }
    }

    /**
     * Returns the book, making it available again for borrowing in a thread-safe manner.
     * <p>
     * If the book is already available, an IllegalStateException is thrown.
     * </p>
     *
     * @throws IllegalStateException if the book was not borrowed
     */
    
    public void returnBook() {
        synchronized (this) {
            if (available) {
                throw new IllegalStateException("Cannot return a book that is already available.");
            }
            available = true;
        }
    }


    /**
     * Compares this book with another object.
     * Two books are considered equal if they share the same ISBN.
     * 
     * @param obj the object to compare with
     * @return true if the ISBN is the same, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Book b = (Book) obj;
        return isbn.equals(b.isbn);
    }

    /**
     * Returns the hash code value for this book, based on its ISBN.
     * 
     * @return the hash code of the book based on isbn
     */
    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    /**
     * Returns a string representation of the book in the format:
     * "Title — Author (ISBN: xxx) [AVAILABLE/BORROWED]".
     *
     * @return a formatted string describing the book
     */
    @Override
    public String toString() {
        synchronized (this) {
            return String.format("%s — %s (ISBN: %s) %s",
                    title, author, isbn, available ? "[AVAILABLE]" : "[BORROWED]");
        }
    }
}

