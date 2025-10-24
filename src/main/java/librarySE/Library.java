package librarySE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a library that stores books and allows operations like
 * adding, searching, and retrieving books.
 * <p>
 * This class manages the collection of books and enforces admin permissions
 * for adding books.
 * </p>
 * 
 * @author Malak
 */
public class Library {

    /** Internal list storing all books in the library */
    private List<Book> books;

    /**
     * Constructs an empty library.
     */
    public Library() {
        books = new ArrayList<>();
    }

    /**
     * Adds a new book to the library.
     * <p>
     * Only users with administrator privileges can add books.
     * </p>
     *
     * @param book the book to add; must not be {@code null}
     * @param user the user attempting to add the book; must not be {@code null} and must be an admin
     * @throws IllegalArgumentException if {@code book} is {@code null}
     * @throws IllegalArgumentException if {@code user} is {@code null}
     * @throws IllegalArgumentException if {@code user} is not an admin
     * @throws IllegalArgumentException if a book with the same ISBN already exists
     */
    public void addBook(Book book, User user) {
        if (book == null) 
            throw new IllegalArgumentException("Book cannot be null");
        if (user == null) 
            throw new IllegalArgumentException("User cannot be null");
        if (!user.isAdmin()) 
            throw new IllegalArgumentException("Only admins can add books!");
        boolean exists = books.stream().anyMatch(b -> b.getIsbn().equals(book.getIsbn()));
        if (exists) 
            throw new IllegalArgumentException("Book with ISBN " + book.getIsbn() + " already exists!");
        books.add(book);
    }

    /**
     * Searches for books by title, author, or ISBN.
     * <p>
     * The search is case-insensitive. Returns all books where the keyword appears
     * in the title, author's name, or ISBN.
     * </p>
     *
     * @param keyword the search keyword; must not be {@code null}
     * @return a list of books matching the keyword; empty list if no matches
     * @throws IllegalArgumentException if {@code keyword} is {@code null}
     */
    public List<Book> searchBook(String keyword) {
        if (keyword == null) 
            throw new IllegalArgumentException("Search keyword cannot be null");
        String lowerKeyword = keyword.toLowerCase();
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerKeyword) 
                          || b.getAuthor().toLowerCase().contains(lowerKeyword)
                          || b.getIsbn().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    /**
     * Returns all books in the library.
     * <p>
     * Returns a new list to prevent external modification of the internal books list.
     * </p>
     *
     * @return a list of all books in the library; empty list if no books
     */
    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }
}
