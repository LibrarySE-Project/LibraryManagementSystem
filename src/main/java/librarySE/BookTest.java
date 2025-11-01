package librarySE;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

/**
 * Unit tests for the {@link Book} class.
 * <p>
 * Covers constructor validation, getters and setters, borrowing and returning
 * functionality, equality, hash code, and string representation.
 * Includes both positive and negative test cases.
 * </p>
 */
class BookTest {

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book("ISBN123", "Effective Java", "Joshua Bloch");
    }

    @AfterEach
    void tearDown() {
        book = null;
    }

    // Constructor and Getters

    /** Constructor sets fields correctly and trims input */
    @Test
    void testConstructorAndGetters() {
        Book b = new Book(" 123-XYZ ", " Title ", " Author ");
        assertEquals("123-XYZ", b.getIsbn());
        assertEquals("Title", b.getTitle());
        assertEquals("Author", b.getAuthor());
        assertTrue(b.isAvailable());
    }

    /** Constructor throws for null or empty parameters */
    @Test
    void testConstructorInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> new Book(null, "Title", "Author"));
        assertThrows(IllegalArgumentException.class, () -> new Book("ISBN", null, "Author"));
        assertThrows(IllegalArgumentException.class, () -> new Book("ISBN", "Title", null));
        assertThrows(IllegalArgumentException.class, () -> new Book(" ", "Title", "Author"));
        assertThrows(IllegalArgumentException.class, () -> new Book("ISBN", " ", "Author"));
        assertThrows(IllegalArgumentException.class, () -> new Book("ISBN", "Title", " "));
    }

    // Setters

    /** setTitle trims and updates the title */
    @Test
    void testSetTitleValid() {
        book.setTitle(" New Title ");
        assertEquals("New Title", book.getTitle());
    }

    /** setTitle invalid values throw exception */
    @Test
    void testSetTitleInvalid() {
        assertThrows(IllegalArgumentException.class, () -> book.setTitle(null));
        assertThrows(IllegalArgumentException.class, () -> book.setTitle("  "));
    }

    /** setAuthor trims and updates the author */
    @Test
    void testSetAuthorValid() {
        book.setAuthor(" New Author ");
        assertEquals("New Author", book.getAuthor());
    }

    /** setAuthor invalid values throw exception */
    @Test
    void testSetAuthorInvalid() {
        assertThrows(IllegalArgumentException.class, () -> book.setAuthor(null));
        assertThrows(IllegalArgumentException.class, () -> book.setAuthor("  "));
    }

    // Borrow and Return

    /** borrow() sets availability to false if available */
    @Test
    void testBorrowSuccess() {
        assertTrue(book.isAvailable());
        assertTrue(book.borrow());
        assertFalse(book.isAvailable());
    }

    /** borrow() returns false if already borrowed */
    @Test
    void testBorrowAlreadyBorrowed() {
        book.borrow();
        assertFalse(book.borrow());
    }

    /** returnItem() sets availability to true if borrowed */
    @Test
    void testReturnItemSuccess() {
        book.borrow();
        assertFalse(book.isAvailable());
        assertTrue(book.returnItem());
        assertTrue(book.isAvailable());
    }

    /** returnItem() returns false if already available */
    @Test
    void testReturnItemAlreadyAvailable() {
        assertTrue(book.isAvailable());
        assertFalse(book.returnItem());
    }

    // Equality and HashCode

    /** Two books with same ISBN are equal */
    @Test
    void testEqualsSameIsbn() {
        Book b2 = new Book("ISBN123", "Different Title", "Different Author");
        assertEquals(book, b2);
        assertEquals(book.hashCode(), b2.hashCode());
    }

    /** Two books with different ISBN are not equal */
    @Test
    void testEqualsDifferentIsbn() {
        Book b2 = new Book("ISBN456", "Effective Java", "Joshua Bloch");
        assertNotEquals(book, b2);
        assertNotEquals(book.hashCode(), b2.hashCode());
    }

    /** Equals against null and other types returns false */
    @Test
    void testEqualsWithNullAndOther() {
        assertNotEquals(book, null);
        assertNotEquals(book, "some string");
    }

    // toString

    /** toString contains title, author, ISBN, and availability */
    @Test
    void testToStringContainsData() {
        String s = book.toString();
        assertTrue(s.contains("Effective Java"));
        assertTrue(s.contains("Joshua Bloch"));
        assertTrue(s.contains("ISBN123"));
        assertTrue(s.contains("AVAILABLE"));

        book.borrow();
        s = book.toString();
        assertTrue(s.contains("BORROWED"));
    }
}




