package librarySE.core;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import librarySE.utils.Config;

/**
 * Full test suite for {@link Book}.
 * Aims to cover all branches and edge cases.
 */
class BookTest {

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book("123-ABC", "My Book", "Author Name", BigDecimal.valueOf(50));
    }

    @AfterEach
    void tearDown() {
        book = null;
    }

    // ----------------------------------------------------------
    // Constructor / initPrice / initCopies
    // ----------------------------------------------------------

    @Test
    void testConstructor_ValidData() {
        assertEquals("123-ABC", book.getIsbn());
        assertEquals("My Book", book.getTitle());
        assertEquals("Author Name", book.getAuthor());
        assertEquals(BigDecimal.valueOf(50), book.getPrice());
        assertEquals(1, book.getTotalCopies());
        assertEquals(1, book.getAvailableCopies());
    }

    @Test
    void testConstructor_PriceNull_UsesDefaultConfig() {
        double def = Config.getDouble("price.book.default", 0.0);
        Book b = new Book("111", "Title", "Author", null);
        assertEquals(BigDecimal.valueOf(def), b.getPrice());
    }

    @Test
    void testConstructor_PriceZero_UsesDefaultConfig() {
        double def = Config.getDouble("price.book.default", 0.0);
        Book b = new Book("222", "Title", "Author", BigDecimal.ZERO);
        assertEquals(BigDecimal.valueOf(def), b.getPrice());
    }

    @Test
    void testConstructor_NegativePrice_UsesDefaultConfig() {
        double def = Config.getDouble("price.book.default", 0.0);
        Book b = new Book("333", "Title", "Author", BigDecimal.valueOf(-5));
        assertEquals(BigDecimal.valueOf(def), b.getPrice());
    }

    @Test
    void testConstructor_WithTotalCopies_Valid() {
        Book b = new Book("999", "Title", "Author", BigDecimal.TEN, 5);
        assertEquals(5, b.getTotalCopies());
        assertEquals(5, b.getAvailableCopies());
        assertEquals(BigDecimal.TEN, b.getPrice());
    }

    @Test
    void testConstructor_WithTotalCopies_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Book("ID", "T", "A", BigDecimal.ONE, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new Book("ID", "T", "A", BigDecimal.ONE, -5));
    }

    @Test
    void testConstructor_InvalidISBN_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Book("", "Title", "Author", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new Book("   ", "Title", "Author", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new Book(null, "Title", "Author", BigDecimal.TEN));
    }

    @Test
    void testConstructor_InvalidTitle_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Book("123", "", "Author", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new Book("123", " ", "Author", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new Book("123", null, "Author", BigDecimal.TEN));
    }

    @Test
    void testConstructor_InvalidAuthor_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Book("123", "Title", "", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new Book("123", "Title", "   ", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new Book("123", "Title", null, BigDecimal.TEN));
    }

    @Test
    void testConstructor_TitleAndAuthorAreTrimmed() {
        Book b = new Book("789", "   A Title  ", "  An Author ", BigDecimal.TEN);
        assertEquals("A Title", b.getTitle());
        assertEquals("An Author", b.getAuthor());
    }

    // ----------------------------------------------------------
    // Setter Tests
    // ----------------------------------------------------------

    @Test
    void testSetTitle_Valid() {
        book.setTitle("New");
        assertEquals("New", book.getTitle());
    }

    @Test
    void testSetTitle_Trimmed() {
        book.setTitle("   Clean Title   ");
        assertEquals("Clean Title", book.getTitle());
    }

    @Test
    void testSetTitle_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class, () -> book.setTitle(""));
        assertThrows(IllegalArgumentException.class, () -> book.setTitle("  "));
        assertThrows(IllegalArgumentException.class, () -> book.setTitle(null));
    }

    @Test
    void testSetAuthor_Valid() {
        book.setAuthor("Someone New");
        assertEquals("Someone New", book.getAuthor());
    }

    @Test
    void testSetAuthor_Trimmed() {
        book.setAuthor("  Alice  ");
        assertEquals("Alice", book.getAuthor());
    }

    @Test
    void testSetAuthor_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class, () -> book.setAuthor(""));
        assertThrows(IllegalArgumentException.class, () -> book.setAuthor("   "));
        assertThrows(IllegalArgumentException.class, () -> book.setAuthor(null));
    }

    // ----------------------------------------------------------
    // Copy Management / setTotalCopies branches
    // ----------------------------------------------------------

    @Test
    void testInitialCopies_DefaultSingleCopy() {
        assertEquals(1, book.getTotalCopies());
        assertEquals(1, book.getAvailableCopies());
    }

    @Test
    void testSetTotalCopies_Increase() {
        book.setTotalCopies(5);
        assertEquals(5, book.getTotalCopies());
        assertEquals(5, book.getAvailableCopies());
    }

    @Test
    void testSetTotalCopies_Decrease_AvailableClampedToTotal() {
        Book b = new Book("ID", "T", "A", BigDecimal.ONE, 5);
        b.setTotalCopies(2);
        assertEquals(2, b.getTotalCopies());
        assertEquals(2, b.getAvailableCopies());
    }

    @Test
    void testSetTotalCopies_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class, () -> book.setTotalCopies(0));
        assertThrows(IllegalArgumentException.class, () -> book.setTotalCopies(-3));
    }

    @Test
    void testSetTotalCopies_ClampAvailableGreaterThanTotal_Reflection() throws Exception {
        Book b = new Book("X", "Y", "Z", BigDecimal.TEN, 2);

        Field totalField = Book.class.getDeclaredField("totalCopies");
        Field availableField = Book.class.getDeclaredField("availableCopies");
        totalField.setAccessible(true);
        availableField.setAccessible(true);

        totalField.setInt(b, 2);
        availableField.setInt(b, 10);

        b.setTotalCopies(2);

        assertEquals(2, b.getTotalCopies());
        assertEquals(2, b.getAvailableCopies());
    }

    @Test
    void testSetTotalCopies_ClampAvailableLessThanZero_Reflection() throws Exception {
        Book b = new Book("X", "Y", "Z", BigDecimal.TEN, 2);

        Field totalField = Book.class.getDeclaredField("totalCopies");
        Field availableField = Book.class.getDeclaredField("availableCopies");
        totalField.setAccessible(true);
        availableField.setAccessible(true);

        totalField.setInt(b, 2);
        availableField.setInt(b, -4);

        b.setTotalCopies(2);

        assertEquals(2, b.getTotalCopies());
        assertEquals(0, b.getAvailableCopies());
    }

    // ----------------------------------------------------------
    // Keyword Matching Tests
    // ----------------------------------------------------------

    @Test
    void testMatchesKeyword_Title() {
        assertTrue(book.matchesKeyword("My"));
    }

    @Test
    void testMatchesKeyword_Author() {
        assertTrue(book.matchesKeyword("Author"));
    }

    @Test
    void testMatchesKeyword_Isbn() {
        assertTrue(book.matchesKeyword("123"));
    }

    @Test
    void testMatchesKeyword_FullMatch() {
        assertTrue(book.matchesKeyword("My Book"));
    }

    @Test
    void testMatchesKeyword_CaseInsensitive() {
        assertTrue(book.matchesKeyword("author name".toLowerCase()));
    }

    @Test
    void testMatchesKeyword_NoMatch() {
        assertFalse(book.matchesKeyword("XYZ"));
    }

    @Test
    void testMatchesKeyword_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class, () -> book.matchesKeyword(null));
        assertThrows(IllegalArgumentException.class, () -> book.matchesKeyword(""));
        assertThrows(IllegalArgumentException.class, () -> book.matchesKeyword("   "));
    }

    // ----------------------------------------------------------
    // Borrow / Return Tests
    // ----------------------------------------------------------

    @Test
    void testBorrow_Success() {
        assertTrue(book.borrow());
        assertFalse(book.isAvailable());
        assertEquals(0, book.getAvailableCopies());
    }

    @Test
    void testBorrow_WhenAlreadyBorrowed_Throws() {
        // first borrow succeeds
        assertTrue(book.borrow());
        assertEquals(0, book.getAvailableCopies());

        // second borrow should fail and return false (not throw)
        boolean secondResult = book.borrow();
        assertFalse(secondResult, "Second borrow on a single-copy book must fail");
        assertEquals(0, book.getAvailableCopies(), "Available copies must remain 0");
    }


    @Test
    void testReturn_Success() {
        book.borrow();
        assertTrue(book.returnItem());
        assertTrue(book.isAvailable());
        assertEquals(1, book.getAvailableCopies());
    }

    @Test
    void testReturn_WhenNotBorrowed_Throws() {
        assertThrows(IllegalStateException.class, () -> book.returnItem());
    }

    @Test
    void testMultipleBorrowReturnCycles() {
        for (int i = 0; i < 3; i++) {
            assertTrue(book.borrow());
            assertFalse(book.isAvailable());
            assertTrue(book.returnItem());
            assertTrue(book.isAvailable());
        }
    }

    @Test
    void testGetMaterialType_ReturnsBook() {
        assertEquals(MaterialType.BOOK, book.getMaterialType());
    }

    // ----------------------------------------------------------
    // equals() and hashCode()
    // ----------------------------------------------------------

    @Test
    void testEquals_SameISBN_True() {
        Book b2 = new Book("123-ABC", "X", "Y", BigDecimal.ONE);
        assertTrue(book.equals(b2));
        assertEquals(book.hashCode(), b2.hashCode());
    }

    @Test
    void testEquals_DifferentISBN_False() {
        Book b2 = new Book("XXX", "X", "Y", BigDecimal.ONE);
        assertFalse(book.equals(b2));
    }

    @Test
    void testEquals_SameInstance_True() {
        assertTrue(book.equals(book));
    }

    @Test
    void testEquals_Null_False() {
        assertFalse(book.equals(null));
    }

    @Test
    void testEquals_DifferentType_False() {
        assertFalse(book.equals("Hello"));
    }

    // ----------------------------------------------------------
    // toString() Tests
    // ----------------------------------------------------------

    @Test
    void testToString_NotNull() {
        assertNotNull(book.toString());
    }

    @Test
    void testToString_ContainsFields() {
        String s = book.toString();
        assertTrue(s.contains("BOOK"));
        assertTrue(s.contains(book.getTitle()));
        assertTrue(s.contains(book.getAuthor()));
        assertTrue(s.contains(book.getIsbn()));
        assertTrue(s.contains(book.getPrice().toPlainString()));
    }

    @Test
    void testToString_AvailableState() {
        String text = book.toString();
        assertTrue(text.contains("Available"));
        assertTrue(text.contains("[BOOK]"));
        assertTrue(text.contains(book.getIsbn()));
    }

    @Test
    void testToString_FullyBorrowedState() {
        book.borrow();
        String text = book.toString();
        assertTrue(text.contains("Fully borrowed"));
    }
}
