package librarySE;


import java.util.Objects;

/** 
 * Represents a book in the library system.
 * Each book has an ISBN, title, author, and availability status.
 * This class is used to manage and track books in the library system.
 * 
 * @author Malak
 * 
 */

public class Book {
	/** The International Standard Book Number (ISBN) of the book, used to uniquely identify it. */
	private final String isbn;

	/** The title of the book. */
	private final String title;

	/** The author of the book. */
	private final String author;

	/** Indicates whether the book is currently available for borrowing. 
	 *  True if available, false if it is borrowed or reserved. 
	 */
	private boolean available = true;

    /**
     * Constructs a new Book object with the specified details.
     *
     * @param isbn   the ISBN number of the book
     * @param title  the title of the book
     * @param author the author of the book
     */

    public Book(String isbn, String title, String author) {
      if (isbn == null || title == null || author == null) {
    	       throw new IllegalArgumentException("ISBN, title, and author cannot be null.");
    	}
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    /**
     * Returns the book's ISBN.
     * 
     * @return the book's Isbn
    */
    public String getIsbn() { return isbn; }
    /** 
     * Returns the book's Title.
     * 
     * @return the book's Title
    */
    public String getTitle() { return title; }
    /** 
     * Returns the author's name.
     * 
     * @return the author's name
    */
    public String getAuthor() { return author; }
    /** 
     * Returns whether the book is currently available for borrowing.
     * 
     * @return true if available, false if borrowed
    */
    public boolean isAvailable () { return available; }
    
    /**
     * Attempts to borrow the book.
     * 
     * @return {@code true} if the book was successfully borrowed (was available),
     *         {@code false} if the book is already borrowed (not available).
     */
    public boolean borrow() {
    	  if (!available) return false;
    	    available = false;
    	    return true;
    }

    /**
     * Returns the book, making it available again for borrowing.
     * This method sets the book's availability status to {@code true}.
     */
    public void returnBook() {
        available = true;
    }


    /** 
     * Compares this book with another object.
     * Two books are considered equal if they share the same ISBN.
     * 
     *  @param obj the object to compare with
     *  @return true if the ISBN is the same, false otherwise
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
    public int hashCode() { return Objects.hash(isbn); }
    /**
     * Returns a string representation of the book in the format:
     * "Title — Author (ISBN: xxx) [AVAILABLE/BORROWED]".
     * 
    @return a formatted string describing the book
    */
    @Override
    public String toString() {
    	return String.format("%s — %s (ISBN: %s) %s", title, author, isbn, available ? "[AVAILABLE]" : "[BORROWED]");
    }
}
