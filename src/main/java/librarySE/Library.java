package librarySE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a library that stores books and allows operations like
 * adding and searching books.
 * 
 * @author Malak
 */
public class Library {

	/** Internal list storing all books in the library */
    private List<Book> books;

    /** Constructs an empty library */
    public Library() {
        books = new ArrayList<>();
    }

    /**
     * Adds a new book to the library.
     * <p>
     * Only users with administrator privileges can add books. 
     * If a non-admin user attempts to add a book, an exception is thrown.
     * </p>
     *
     * @param book the book to add
     * @param user the user attempting to add the book; must be an admin
     * @throws IllegalArgumentException if the user is not an admin
     */
    public void addBook(Book book,User user) {
    	 if (user.isAdmin()) {
    		 boolean exists = books.stream().anyMatch(b -> b.getIsbn().equals(book.getIsbn()));
    		    if (exists) {
    		        throw new IllegalArgumentException("Book with ISBN " + book.getIsbn() + " already exists!");
    		    }

    	        books.add(book);
    	    } else {
    	        throw new IllegalArgumentException("Only admins can add books!");
    	    }
    }

    /**
     * Searches for books by title, author, or ISBN.
     * 
     * @param keyword the search keyword
     * @return a list of books matching the keyword
     */
    public List<Book> searchBook(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerKeyword) 
                          || b.getAuthor().toLowerCase().contains(lowerKeyword)
                          || b.getIsbn().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    /**
     * Returns all books in the library.
     * 
     * @return list of books
     */
    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }
}
