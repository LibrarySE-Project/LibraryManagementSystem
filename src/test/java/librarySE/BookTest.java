package librarySE;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookTest {
    private Book b1;
    private Book b2;
    private Book b3;

    /**
     * Runs before each test.
     * Creates three Book objects for testing.
     */
    @BeforeEach
    void setUp() {
        b1 = new Book("193", "Java Basics", "John Doe");
        b2 = new Book("193", "Java Basics", "John Doe");
        b3 = new Book("457", "Python Intro", "Alice");
    }

    /**
     * Runs after each test.
     * Clears book objects.
     */
    @AfterEach
    void tearDown() {
        b1 = b2 = b3 = null;
    }

    /**
     * Test getters: isbn, title, author, and availability.
     */
    @Test
    void testGetters() {
        assertEquals("457", b3.getIsbn(), "ISBN should match");
        assertEquals("Python Intro", b3.getTitle(), "Title should match");
        assertEquals("Alice", b3.getAuthor(), "Author should match");
        assertTrue(b3.isAvailable(), "New book should be available");
    }

    /**
     * Test equals method:
     * - comparing to itself
     * - comparing to another Book with same ISBN
     * - comparing to Book with different ISBN
     * - comparing to null
     * - comparing to object of different type
     */
    @Test
    void testEquals() {
        Integer obj = 21;

        assertTrue(b1.equals(b1), "Book should equal itself");
        assertTrue(b1.equals(b2), "Books with same ISBN should be equal");
        assertFalse(b1.equals(b3), "Books with different ISBN should not be equal");
        assertFalse(b1.equals(obj), "Book should not equal an object of different type");
        assertFalse(b1.equals(null), "Book should not equal null");
    }

    /**
     * Test hashCode consistency with equals:
     * Books with same ISBN should have same hashCode.
     */
    @Test
    void testHashCode() {
        assertEquals(b1.hashCode(), b2.hashCode(), "Books with same ISBN must have same hashCode");
        assertNotEquals(b1.hashCode(), b3.hashCode(), "Books with different ISBN must have different hashCode");
    }

    /**
     * Test borrow() behavior:
     * - can borrow if available
     * - cannot borrow if already borrowed
     */
    @Test
    void testBorrow() {
        assertTrue(b3.isAvailable(), "Book should initially be available");
        assertTrue(b3.borrow(), "Borrowing available book should succeed");
        assertFalse(b3.isAvailable(), "Book should now be unavailable");
        assertFalse(b3.borrow(), "Borrowing already borrowed book should fail");
    }

    /**
     * Test returnBook() behavior:
     * - sets book as available
     * - calling returnBook when already available does nothing
     */
    @Test
    void testReturnBook() {
        b3.borrow();
        assertFalse(b3.isAvailable(), "Book should be unavailable after borrow");

        b3.returnBook();
        assertTrue(b3.isAvailable(), "Book should be available after return");

        b3.returnBook();
        assertTrue(b3.isAvailable(), "Returning an already available book should not change availability");
    }

    /**
     * Test toString() format:
     * - shows [AVAILABLE] if book is available
     * - shows [BORROWED] if book is borrowed
     */
    @Test
    void testToString() {
        String expectedAvailable = "Python Intro — Alice (ISBN: 457) [AVAILABLE]";
        assertEquals(expectedAvailable, b3.toString(), "toString should display availability correctly");

        b3.borrow();
        String expectedBorrowed = "Python Intro — Alice (ISBN: 457) [BORROWED]";
        assertEquals(expectedBorrowed, b3.toString(), "toString should display borrowed status correctly");
    }

    /**
     * Test constructor null arguments:
     * - passing null ISBN, title, or author should throw IllegalArgumentException
     */
    @Test
    void testConstructorNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> new Book(null, "Title", "Author"), "Null ISBN should throw exception");
        assertThrows(IllegalArgumentException.class, () -> new Book("123", null, "Author"), "Null title should throw exception");
        assertThrows(IllegalArgumentException.class, () -> new Book("123", "Title", null), "Null author should throw exception");
    }

    /**
     * Edge test: multiple borrow and return cycles
     */
    @Test
    void testMultipleBorrowReturn() {
        assertTrue(b3.borrow(), "First borrow should succeed");
        assertFalse(b3.borrow(), "Second borrow should fail");
        b3.returnBook();
        assertTrue(b3.borrow(), "Borrow after return should succeed again");
    }
}
