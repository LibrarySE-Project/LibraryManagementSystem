package librarySE;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Unit tests for the {@link Book} class.
 * <p>
 * Verifies constructor validation, getters/setters, borrow/return functionality,
 * equality/hashCode behavior, string representation, and thread safety.
 * </p>
 */
class BookTest {

    private Book b1;
    private Book b2;
    private Book b3;

    /**
     * Runs before each test.
     * <p>
     * Initializes multiple Book instances with different ISBNs and details
     * to cover equality and borrowing scenarios.
     * </p>
     */
    @BeforeEach
    void setUp() {
        b1 = new Book("193", "Java Basics", "John Doe");
        b2 = new Book("193", "Java Basics", "John Doe");
        b3 = new Book("457", "Python Intro", "Alice");
    }

    /**
     * Runs after each test.
     * <p>
     * Clears references to allow garbage collection.
     * </p>
     */
    @AfterEach
    void tearDown() {
        b1 = b2 = b3 = null;
    }

    /**
     * Test constructor and getters.
     * <p>
     * Ensures that the Book instance is created correctly and all getters
     * return expected values. Checks availability default state.
     * </p>
     */
    @Test
    void testConstructorAndGetters() {
        assertEquals("193", b1.getIsbn());
        assertEquals("Java Basics", b1.getTitle());
        assertEquals("John Doe", b1.getAuthor());
        assertTrue(b1.isAvailable(), "Newly created book should be available");
    }

    /**
     * Test constructor with invalid arguments.
     * <p>
     * Verifies that creating a Book with null or empty ISBN, title, or author
     * throws IllegalArgumentException.
     * </p>
     */
    @Test
    void testConstructorInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new Book(null, "Title", "Author"));
        assertThrows(IllegalArgumentException.class, () -> new Book("123", null, "Author"));
        assertThrows(IllegalArgumentException.class, () -> new Book("123", "Title", null));
        assertThrows(IllegalArgumentException.class, () -> new Book("", "Title", "Author"));
        assertThrows(IllegalArgumentException.class, () -> new Book("123", "  ", "Author"));
        assertThrows(IllegalArgumentException.class, () -> new Book("123", "Title", ""));
    }

    /**
     * Test setters for title and author.
     * <p>
     * Ensures setters correctly update values and throw exceptions for invalid input.
     * </p>
     */
    @Test
    void testSetters() {
        b1.setTitle("Advanced Java");
        b1.setAuthor("Jane Doe");
        assertEquals("Advanced Java", b1.getTitle());
        assertEquals("Jane Doe", b1.getAuthor());

        assertThrows(IllegalArgumentException.class, () -> b1.setTitle(""));
        assertThrows(IllegalArgumentException.class, () -> b1.setAuthor(null));
    }

    /**
     * Test borrow and return behavior.
     * <p>
     * Ensures borrow() marks the book unavailable, returns false if already borrowed,
     * and returnItem() marks it available and returns false if already available.
     * </p>
     */
    @Test
    void testBorrowAndReturnFlow() {
        assertTrue(b3.borrow());
        assertFalse(b3.isAvailable());
        assertFalse(b3.borrow());

        assertTrue(b3.returnItem());
        assertTrue(b3.isAvailable());
        assertFalse(b3.returnItem());
    }

    /**
     * Test equals and hashCode.
     * <p>
     * Verifies that books with the same ISBN are considered equal
     * and produce the same hash code, while books with different ISBNs are not.
     * </p>
     */
    @Test
    void testEqualsAndHashCode() {
        assertEquals(b1, b2, "Books with same ISBN should be equal");
        assertNotEquals(b1, b3, "Books with different ISBN should not be equal");
        assertNotEquals(b1, null);
        assertNotEquals(b1, "string");

        assertEquals(b1.hashCode(), b2.hashCode());
        assertNotEquals(b1.hashCode(), b3.hashCode());
    }

    /**
     * Test string representation.
     * <p>
     * Ensures toString shows correct title, author, ISBN, and availability status.
     * </p>
     */
    @Test
    void testToString() {
        assertEquals("Python Intro — Alice (ISBN: 457) [AVAILABLE]", b3.toString());
        b3.borrow();
        assertEquals("Python Intro — Alice (ISBN: 457) [BORROWED]", b3.toString());
    }

    /**
     * Test thread safety of borrow().
     * <p>
     * Runs concurrent borrow attempts and verifies only one succeeds,
     * ensuring proper synchronization.
     * </p>
     */
    @Test
    void testBorrowThreadSafety() throws InterruptedException {
        Book book = new Book("999", "Concurrency", "Thread Guru");
        Runnable borrowTask = book::borrow;

        Thread t1 = new Thread(borrowTask);
        Thread t2 = new Thread(borrowTask);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertFalse(book.isAvailable(), "Only one thread should succeed borrowing");
    }

    /**
     * Test thread safety of returnItem().
     * <p>
     * Runs concurrent return attempts and verifies book becomes available
     * without exceptions, ensuring proper synchronization.
     * </p>
     */
    @Test
    void testReturnThreadSafety() throws InterruptedException {
        Book book = new Book("1000", "Threads", "Java Master");
        book.borrow();

        Runnable returnTask = book::returnItem;

        Thread t1 = new Thread(returnTask);
        Thread t2 = new Thread(returnTask);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertTrue(book.isAvailable(), "Book should be available after concurrent returns");
    }

    /**
     * Test compliance with LibraryItem interface.
     * <p>
     * Verifies that borrow() and returnItem() behave correctly
     * when accessed via a LibraryItem reference.
     * </p>
     */
    @Test
    void testLibraryItemInterfaceCompliance() {
        LibraryItem libItem = b1;
        assertTrue(libItem.isAvailable());
        assertTrue(libItem.borrow());
        assertFalse(libItem.isAvailable());
        assertTrue(libItem.returnItem());
        assertTrue(libItem.isAvailable());
    }
}



