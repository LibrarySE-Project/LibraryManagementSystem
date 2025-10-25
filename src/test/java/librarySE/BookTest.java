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
     * <p>
     * Creates multiple {@link Book} objects for testing.
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
     * Clears references to avoid data interference between test executions.
     * </p>
     */
    @AfterEach
    void tearDown() {
        b1 = b2 = b3 = null;
    }

    /**
     * Tests all getter methods of the {@link Book} class.
     * <p>
     * Ensures that ISBN, title, author, and default availability
     * are correctly initialized and retrievable.
     * </p>
     */
    @Test
    void testGetters() {
        assertEquals("193", b1.getIsbn(), "ISBN should match");
        assertEquals("Java Basics", b1.getTitle(), "Title should match");
        assertEquals("John Doe", b1.getAuthor(), "Author should match");
        assertTrue(b1.isAvailable(), "New book should be available");
    }

    /**
     * Tests the {@link Book#borrow()} method.
     * <p>
     * Verifies that borrowing succeeds for an available book
     * and fails when attempting to borrow a book that is already borrowed.
     * </p>
     */
    @Test
    void testBorrow() {
        assertTrue(b3.borrow(), "Borrowing available book should succeed");
        assertFalse(b3.isAvailable(), "Book should now be unavailable");
        assertFalse(b3.borrow(), "Borrowing already borrowed book should fail");
    }

    /**
     * Tests the {@link Book#toString()} method.
     * <p>
     * Ensures correct string representation for both available and borrowed states.
     * </p>
     */
    @Test
    void testToString() {
        assertEquals("Python Intro — Alice (ISBN: 457) [AVAILABLE]", b3.toString(),
                "toString should show AVAILABLE");

        b3.borrow();
        assertEquals("Python Intro — Alice (ISBN: 457) [BORROWED]", b3.toString(),
                "toString should show BORROWED");
    }

    /**
     * Tests {@link Book} constructor validation.
     * <p>
     * Ensures that invalid arguments (null or empty values)
     * cause {@link IllegalArgumentException} to be thrown.
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
     * Tests the {@link Book#equals(Object)} and {@link Book#hashCode()} methods.
     * <p>
     * Ensures equality is based solely on ISBN and that hash codes are consistent.
     * </p>
     */
    @Test
    void testEqualsAndHashCode() {
        assertTrue(b1.equals(b1), "Book equals itself");
        assertTrue(b1.equals(b2), "Books with same ISBN are equal");
        assertFalse(b1.equals(b3), "Books with different ISBN are not equal");
        assertFalse(b1.equals(null), "Book does not equal null");
        assertFalse(b1.equals("string"), "Book does not equal object of different type");
        assertEquals(b1.hashCode(), b2.hashCode(), "Hash codes are same for same ISBN");
        assertNotEquals(b1.hashCode(), b3.hashCode(), "Hash codes differ for different ISBNs");
    }

    /**
     * Tests thread-safety of {@link Book#borrow()} method.
     * <p>
     * Ensures that only one thread can successfully borrow a book at a time.
     * </p>
     */
    @Test
    void testBorrowThreadSafety() throws InterruptedException {
        Book book = new Book("999", "Concurrency", "Thread Guru");
        Runnable borrowTask = () -> book.borrow();

        Thread t1 = new Thread(borrowTask);
        Thread t2 = new Thread(borrowTask);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertFalse(book.isAvailable(), "Only one thread should succeed borrowing");
    }

    /**
     * Tests thread-safety of {@link Book#returnItem()} method.
     * <p>
     * Ensures that concurrent returns are safely handled without
     * corrupting the book’s availability state.
     * </p>
     */
    @Test
    void testReturnThreadSafety() throws InterruptedException {
        Book book = new Book("1000", "Threads", "Java Master");
        book.borrow();

        Runnable returnTask = () -> {
            try {
                book.returnItem();
            } catch (IllegalStateException ignored) {
                // expected if already returned
            }
        };

        Thread t1 = new Thread(returnTask);
        Thread t2 = new Thread(returnTask);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertTrue(book.isAvailable(), "Book should be available after concurrent returns");
    }
    /**
     * Comprehensive test for {@link Book#borrow()} and {@link Book#returnItem()}.
     * <p>
     * Verifies:
     * <ul>
     *   <li>Successful borrowing and returning cycles</li>
     *   <li>Availability state transitions</li>
     *   <li>Returning an already available book returns {@code false}</li>
     * </ul>
     * </p>
     */
    @Test
    void testBorrowAndReturnBookComprehensive() {
        assertTrue(b3.borrow(), "First borrow should succeed");
        assertFalse(b3.isAvailable(), "Book should be unavailable after borrow");

        assertFalse(b3.borrow(), "Second borrow should fail when already borrowed");

        assertTrue(b3.returnItem(), "Return after borrow should succeed");
        assertTrue(b3.isAvailable(), "Book should be available after return");

        assertTrue(b3.borrow(), "Borrow after return should succeed");
        assertFalse(b3.isAvailable(), "Book unavailable again after borrow");

        assertTrue(b3.returnItem(), "Book available after final return");

       
        assertFalse(b3.returnItem(), "Returning an already available book should return false");
    }
}

