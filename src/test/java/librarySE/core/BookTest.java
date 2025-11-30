package librarySE.core;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import librarySE.utils.Config;

/**
 * Unit tests for {@link Book}.
 * 
 * <p>This test suite covers:</p>
 * <ul>
 *   <li>Constructor validation</li>
 *   <li>Smart price loading logic</li>
 *   <li>Field setters</li>
 *   <li>Keyword matching</li>
 *   <li>Borrow/return behavior (inherited)</li>
 *   <li>equals / hashCode</li>
 *   <li>toString()</li>
 * </ul>
 */
class BookTest {

    private Book book;

    //  Test Lifecycle
    @BeforeEach
    void setUp() {
        book = new Book("123-ABC", "My Book", "Author Name", BigDecimal.valueOf(50));
    }

    @AfterEach
    void tearDown() {
        book = null;
    }

    //  Constructor Test
    @Test
    void testConstructor_ValidData() {
        assertEquals("123-ABC", book.getIsbn());
        assertEquals("My Book", book.getTitle());
        assertEquals("Author Name", book.getAuthor());
        assertEquals(BigDecimal.valueOf(50), book.getPrice());
    }

    @Test
    void testConstructor_PriceNull_UsesDefault() {
        double defaultPrice = Config.getDouble("price.book.default", 0.0);
        Book b = new Book("111", "Title", "Author", null);
        assertEquals(BigDecimal.valueOf(defaultPrice), b.getPrice());
    }

    @Test
    void testConstructor_PriceZero_UsesDefault() {
        double defaultPrice = Config.getDouble("price.book.default", 0.0);
        Book b = new Book("222", "Title", "Author", BigDecimal.ZERO);
        assertEquals(BigDecimal.valueOf(defaultPrice), b.getPrice());
    }

    @Test
    void testConstructor_InvalidISBN_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Book("", "Title", "Author", BigDecimal.TEN));
    }

    @Test
    void testConstructor_InvalidTitle_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Book("123", " ", "Author", BigDecimal.TEN));
    }

    @Test
    void testConstructor_InvalidAuthor_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Book("123", "Title", "", BigDecimal.TEN));
    }

    // ----------------------------------------------------------
    //  Setter Tests
    // ----------------------------------------------------------

    @Test
    void testSetTitle_Valid() {
        book.setTitle("New Title");
        assertEquals("New Title", book.getTitle());
    }

    @Test
    void testSetTitle_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class, () -> book.setTitle(""));
        assertThrows(IllegalArgumentException.class, () -> book.setTitle("  "));
        assertThrows(IllegalArgumentException.class, () -> book.setTitle(null));
    }

    @Test
    void testSetAuthor_Valid() {
        book.setAuthor("New Author");
        assertEquals("New Author", book.getAuthor());
    }

    @Test
    void testSetAuthor_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class, () -> book.setAuthor(""));
        assertThrows(IllegalArgumentException.class, () -> book.setAuthor("  "));
        assertThrows(IllegalArgumentException.class, () -> book.setAuthor(null));
    }

    // ----------------------------------------------------------
    //  Keyword Matching
    // ----------------------------------------------------------

    @Test
    void testMatchesKeyword_TitleMatch() {
        assertTrue(book.matchesKeyword("My"));
    }

    @Test
    void testMatchesKeyword_AuthorMatch() {
        assertTrue(book.matchesKeyword("Author"));
    }

    @Test
    void testMatchesKeyword_IsbnMatch() {
        assertTrue(book.matchesKeyword("123"));
    }

    @Test
    void testMatchesKeyword_NoMatch() {
        assertFalse(book.matchesKeyword("XYZ"));
    }

    @Test
    void testMatchesKeyword_Null_Throws() {
        assertThrows(IllegalArgumentException.class, () -> book.matchesKeyword(null));
    }

    @Test
    void testMatchesKeyword_Empty_Throws() {
        assertThrows(IllegalArgumentException.class, () -> book.matchesKeyword(""));
    }

    // ----------------------------------------------------------
    //  Borrow / Return Tests (Inherited)
    // ----------------------------------------------------------

    @Test
    void testBorrow_Success() {
        assertTrue(book.borrow());
        assertFalse(book.isAvailable());
    }

    @Test
    void testBorrow_FailsWhenAlreadyBorrowed() {
        book.borrow();
        assertFalse(book.borrow());
    }

    @Test
    void testReturn_Success() {
        book.borrow();
        assertTrue(book.returnItem());
        assertTrue(book.isAvailable());
    }

    @Test
    void testReturn_FailsWhenNotBorrowed() {
        assertFalse(book.returnItem());
    }

    // ----------------------------------------------------------
    //  equals / hashCode
    // ----------------------------------------------------------

    @Test
    void testEquals_SameISBN_True() {
        Book b2 = new Book("123-ABC", "Another", "A", BigDecimal.TEN);
        assertTrue(book.equals(b2));
        assertEquals(book.hashCode(), b2.hashCode());
    }

    @Test
    void testEquals_DifferentISBN_False() {
        Book b2 = new Book("ZZZ", "Other", "A", BigDecimal.TEN);
        assertFalse(book.equals(b2));
    }

    @Test
    void testEquals_Null_False() {
        assertFalse(book.equals(null));
    }

    @Test
    void testEquals_DifferentType_False() {
        assertFalse(book.equals("Hello"));
    }

    //  toString()
    @Test
    void testToString_NotNull() {
        assertNotNull(book.toString());
        assertTrue(book.toString().contains("BOOK"));
    }
}


