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

	@BeforeEach
	void setUp() throws Exception {
	     library = new Library();
	     admin = Admin.getInstance("admin", "pass123");
	     regularUser = new User("user1", Role.USER, "userpass");
	     book1 = new Book("123", "Java Basics", "John Doe");
	     book2 = new Book("456", "Python Intro", "Alice");

	}

	@AfterEach
	void tearDown() throws Exception {
		library=null;
		admin =null;
		regularUser=null;
		book1=book2=null;
	}
	@Test
    void testAddBookAsAdmin() {
        library.addBook(book1, admin);
        List<Book> books = library.getAllBooks();
        assertEquals(1, books.size());
        assertTrue(books.contains(book1));
    }

    @Test
    void testAddBookAsRegularUser() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            library.addBook(book1, regularUser);
        });
        assertEquals("Only admins can add books!", exception.getMessage());
    }

    @Test
    void testSearchBookByTitle() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> result = library.searchBook("java");
        assertEquals(1, result.size());
        assertTrue(result.contains(book1));
    }

    @Test
    void testSearchBookByAuthor() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> result = library.searchBook("alice");
        assertEquals(1, result.size());
        assertTrue(result.contains(book2));
    }

    @Test
    void testSearchBookByISBN() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> result = library.searchBook("456");
        assertEquals(1, result.size());
        assertTrue(result.contains(book2));
    }

    @Test
    void testGetAllBooks() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> allBooks = library.getAllBooks();
        assertEquals(2, allBooks.size());
        assertTrue(allBooks.contains(book1));
        assertTrue(allBooks.contains(book2));
    }
    @Test
    void testSearchBookNotFound() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> result = library.searchBook("C++"); 
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchBookCaseInsensitive() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> result = library.searchBook("PYTHON"); 
        assertEquals(1, result.size());
        assertTrue(result.contains(book2));
    }

    @Test
    void testAddBookWithDuplicateISBN() {
        Book duplicateBook = new Book("123", "Advanced Java", "Jane Doe");
        library.addBook(book1, admin);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            library.addBook(duplicateBook, admin);
        });

        assertEquals("Book with ISBN 123 already exists!", exception.getMessage());
    }
    @Test
    void testSearchBookPartialKeyword() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> result = library.searchBook("Intro"); 
        assertEquals(1, result.size());
        assertTrue(result.contains(book2));
    }
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
    
    @Test
    void testBookIsAvailableAfterAdd() {
        library.addBook(book1, admin);
        List<Book> books = library.getAllBooks();
        assertEquals(1, books.size());
        assertTrue(books.get(0).isAvailable(), "Book should be available by default after being added");
    }


}
