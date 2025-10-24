package librarySE;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class LibraryTest {

    private Library library;
    private Admin admin;
    private User regularUser;
    private Book book1;
    private Book book2;

    /**
     * Runs before each test.
     * Initializes the library, admin user, regular user, and sample books.
     */
    @BeforeEach
    void setUp() {
        library = new Library();
        admin = Admin.getInstance("admin", "pass123");
        regularUser = new User("user1", Role.USER, "userpass");
        book1 = new Book("123", "Java Basics", "John Doe");
        book2 = new Book("456", "Python Intro", "Alice");
    }

    /**
     * Runs after each test.
     * Clears all references to avoid interference between tests.
     */
    @AfterEach
    void tearDown() {
        library = null;
        admin = null;
        regularUser = null;
        book1 = null;
        book2 = null;
    }

    /**
     * Test adding a book as an admin user succeeds.
     */
    @Test
    void testAddBookAsAdmin() {
        library.addBook(book1, admin);
        List<Book> books = library.getAllBooks();
        assertEquals(1, books.size(), "Library should contain 1 book");
        assertTrue(books.contains(book1), "Book1 should be in the library");
    }

    /**
     * Test adding a book as a regular (non-admin) user throws exception.
     */
    @Test
    void testAddBookAsRegularUser() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            library.addBook(book1, regularUser);
        });
        assertEquals("Only admins can add books!", exception.getMessage());
    }

    /**
     * Test adding a null book throws IllegalArgumentException.
     */
    @Test
    void testAddNullBook() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            library.addBook(null, admin);
        });
        assertEquals("Book cannot be null", exception.getMessage());
    }

    /**
     * Test adding a book with null user throws IllegalArgumentException.
     */
    @Test
    void testAddBookWithNullUser() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            library.addBook(book1, null);
        });
        assertEquals("User cannot be null", exception.getMessage());
    }

    /**
     * Test adding a book with duplicate ISBN throws exception.
     */
    @Test
    void testAddBookWithDuplicateISBN() {
        Book duplicateBook = new Book("123", "Advanced Java", "Jane Doe");
        library.addBook(book1, admin);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            library.addBook(duplicateBook, admin);
        });

        assertEquals("Book with ISBN 123 already exists!", exception.getMessage());
    }

    /**
     * Test searching books by title returns correct matches.
     */
    @Test
    void testSearchBookByTitle() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> result = library.searchBook("java");
        assertEquals(1, result.size());
        assertTrue(result.contains(book1));
    }

    /**
     * Test searching books by author returns correct matches.
     */
    @Test
    void testSearchBookByAuthor() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> result = library.searchBook("alice");
        assertEquals(1, result.size());
        assertTrue(result.contains(book2));
    }

    /**
     * Test searching books by ISBN returns correct matches.
     */
    @Test
    void testSearchBookByISBN() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> result = library.searchBook("456");
        assertEquals(1, result.size());
        assertTrue(result.contains(book2));
    }

    /**
     * Test searching books with a partial keyword.
     */
    @Test
    void testSearchBookPartialKeyword() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> result = library.searchBook("Intro");
        assertEquals(1, result.size());
        assertTrue(result.contains(book2));
    }

    /**
     * Test searching books returns multiple matches when applicable.
     */
    @Test
    void testSearchBookMultipleMatches() {
        Book anotherBook = new Book("789", "Python Advanced", "Bob");
        library.addBook(book2, admin);
        library.addBook(anotherBook, admin);

        List<Book> result = library.searchBook("Python");
        assertEquals(2, result.size());
        assertTrue(result.contains(book2));
        assertTrue(result.contains(anotherBook));
    }

    /**
     * Test searching books is case-insensitive.
     */
    @Test
    void testSearchBookCaseInsensitive() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> result = library.searchBook("PYTHON");
        assertEquals(1, result.size());
        assertTrue(result.contains(book2));
    }

    /**
     * Test searching with a keyword that matches no book returns empty list.
     */
    @Test
    void testSearchBookNotFound() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> result = library.searchBook("C++");
        assertTrue(result.isEmpty(), "Search should return empty list when no match");
    }

    /**
     * Test that all books are returned correctly by getAllBooks().
     */
    @Test
    void testGetAllBooks() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> allBooks = library.getAllBooks();
        assertEquals(2, allBooks.size());
        assertTrue(allBooks.contains(book1));
        assertTrue(allBooks.contains(book2));
    }

    /**
     * Test that a book is available by default after being added.
     */
    @Test
    void testBookIsAvailableAfterAdd() {
        library.addBook(book1, admin);
        List<Book> books = library.getAllBooks();
        assertTrue(books.get(0).isAvailable(), "Book should be available by default after being added");
    }

    /**
     * Test searching with null keyword throws IllegalArgumentException.
     */
    @Test
    void testSearchBookWithNullKeyword() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            library.searchBook(null);
        });
        assertEquals("Search keyword cannot be null", exception.getMessage());
    }
}
