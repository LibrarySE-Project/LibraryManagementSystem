package librarySE.core;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import librarySE.utils.Config;

/**
 * Unit tests for {@link LibraryItemFactory}.
 */
class LibraryItemFactoryTest {

    @BeforeEach
    void setUp() {
        // no shared state needed yet
    }

    @AfterEach
    void tearDown() {
        // nothing to clean for now
    }

    // ----------------------------------------------------------
    //  Book Creation Tests (legacy create(...))
    // ----------------------------------------------------------

    @Test
    void testCreateBook_WithExplicitPrice() {
        LibraryItem item = LibraryItemFactory.create(
                MaterialType.BOOK,
                "ISBN123",
                "Title",
                "Author",
                "50.5"
        );

        assertTrue(item instanceof Book);
        assertEquals("ISBN123", ((Book) item).getIsbn());
        assertEquals("Title", item.getTitle());
        assertEquals(BigDecimal.valueOf(50.5), item.getPrice());
    }

    @Test
    void testCreateBook_UsesDefaultPrice() {
        double defaultPrice = Config.getDouble("price.book.default", 0.0);

        LibraryItem item = LibraryItemFactory.create(
                MaterialType.BOOK,
                "ISBN123",
                "Title",
                "Author"
        );

        assertTrue(item instanceof Book);
        assertEquals(BigDecimal.valueOf(defaultPrice), item.getPrice());
    }

    // ----------------------------------------------------------
    //  CD Creation Tests (legacy create(...))
    // ----------------------------------------------------------

    @Test
    void testCreateCD_WithExplicitPrice() {
        LibraryItem item = LibraryItemFactory.create(
                MaterialType.CD,
                "Thriller",
                "MJ",
                "25.75"
        );

        assertTrue(item instanceof CD);
        assertEquals("Thriller", item.getTitle());
        assertEquals(BigDecimal.valueOf(25.75), item.getPrice());
    }

    @Test
    void testCreateCD_UsesDefaultPrice() {
        double defaultPrice = Config.getDouble("price.cd.default", 0.0);

        LibraryItem item = LibraryItemFactory.create(
                MaterialType.CD,
                "Thriller",
                "MJ"
        );

        assertTrue(item instanceof CD);
        assertEquals(BigDecimal.valueOf(defaultPrice), item.getPrice());
    }

    // ----------------------------------------------------------
    //  Journal Creation Tests (legacy create(...))
    // ----------------------------------------------------------

    @Test
    void testCreateJournal_WithExplicitPrice() {
        LibraryItem item = LibraryItemFactory.create(
                MaterialType.JOURNAL,
                "AI Monthly",
                "Dr. Smith",
                "Vol 10",
                "40"
        );

        assertTrue(item instanceof Journal);
        assertEquals("AI Monthly", item.getTitle());
        assertEquals(BigDecimal.valueOf(40), item.getPrice());
    }

    @Test
    void testCreateJournal_UsesDefaultPrice() {
        double defaultPrice = Config.getDouble("price.journal.default", 0.0);

        LibraryItem item = LibraryItemFactory.create(
                MaterialType.JOURNAL,
                "AI Monthly",
                "Dr. Smith",
                "Vol 10"
        );

        assertTrue(item instanceof Journal);
        assertEquals(BigDecimal.valueOf(defaultPrice), item.getPrice());
    }

    // ----------------------------------------------------------
    //  Missing-Args Negative Tests (legacy create(...))
    // ----------------------------------------------------------

    @Test
    void testCreateBook_MissingArgs_Throws() {
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> LibraryItemFactory.create(MaterialType.BOOK, "ISBN123"));
    }

    @Test
    void testCreateCD_MissingArgs_Throws() {
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> LibraryItemFactory.create(MaterialType.CD, "OnlyTitle"));
    }

    @Test
    void testCreateJournal_MissingArgs_Throws() {
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> LibraryItemFactory.create(MaterialType.JOURNAL, "Title"));
    }

    // ----------------------------------------------------------
    //  Invalid Price Negative Tests (non-numeric)
    // ----------------------------------------------------------

    @Test
    void testCreateBook_InvalidPrice_Throws() {
        assertThrows(NumberFormatException.class,
                () -> LibraryItemFactory.create(
                        MaterialType.BOOK,
                        "ISBN",
                        "Title",
                        "Author",
                        "NOT_A_NUMBER"
                ));
    }

    @Test
    void testCreateCD_InvalidPrice_Throws() {
        assertThrows(NumberFormatException.class,
                () -> LibraryItemFactory.create(
                        MaterialType.CD,
                        "Album",
                        "Singer",
                        "abc"
                ));
    }

    @Test
    void testCreateJournal_InvalidPrice_Throws() {
        assertThrows(NumberFormatException.class,
                () -> LibraryItemFactory.create(
                        MaterialType.JOURNAL,
                        "Science",
                        "Editor",
                        "Vol 1",
                        "NaN"
                ));
    }

    // ----------------------------------------------------------
    //  Empty price → null in parsePrice → default price in item
    // ----------------------------------------------------------

    @Test
    void testCreateBook_EmptyPrice_UsesDefaultPrice() {
        double defaultPrice = Config.getDouble("price.book.default", 0.0);

        LibraryItem item = LibraryItemFactory.create(
                MaterialType.BOOK,
                "ISBN",
                "Title",
                "Author",
                ""   // empty price → parsePrice() returns null
        );

        assertTrue(item instanceof Book);
        assertEquals(BigDecimal.valueOf(defaultPrice), item.getPrice());
    }

    // ----------------------------------------------------------
    //  Negative price → IllegalArgumentException from parsePrice
    // ----------------------------------------------------------

    @Test
    void testCreateBook_NegativePrice_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> LibraryItemFactory.createBook(
                        "NEGISBN",
                        "Negative",
                        "Author",
                        "-5.0",
                        1
                ));
    }

    // ----------------------------------------------------------
    //  Direct overloads coverage: createBook(...)
    // ----------------------------------------------------------

    @Test
    void testCreateBook_AllOverloads() {
        // default price, 1 copy
        LibraryItem b1 = LibraryItemFactory.createBook("ISBN1", "T1", "A1");
        assertTrue(b1 instanceof Book);

        // explicit price, 1 copy
        LibraryItem b2 = LibraryItemFactory.createBook("ISBN2", "T2", "A2", "12.5");
        assertTrue(b2 instanceof Book);
        assertEquals(BigDecimal.valueOf(12.5), b2.getPrice());

        // default price, multiple copies
        LibraryItem b3 = LibraryItemFactory.createBook("ISBN3", "T3", "A3", 3);
        assertTrue(b3 instanceof Book);

        // explicit price, multiple copies
        LibraryItem b4 = LibraryItemFactory.createBook("ISBN4", "T4", "A4", "30.0", 4);
        assertTrue(b4 instanceof Book);
        assertEquals(BigDecimal.valueOf(30.0), b4.getPrice());
    }

    // ----------------------------------------------------------
    //  Direct overloads coverage: createCd(...)
    // ----------------------------------------------------------

    @Test
    void testCreateCd_AllOverloads() {
        // default price, 1 copy
        LibraryItem c1 = LibraryItemFactory.createCd("Album1", "Singer1");
        assertTrue(c1 instanceof CD);

        // explicit price, 1 copy
        LibraryItem c2 = LibraryItemFactory.createCd("Album2", "Singer2", "15.0");
        assertTrue(c2 instanceof CD);
        assertEquals(BigDecimal.valueOf(15.0), c2.getPrice());

        // default price, multiple copies
        LibraryItem c3 = LibraryItemFactory.createCd("Album3", "Singer3", 2);
        assertTrue(c3 instanceof CD);

        // explicit price, multiple copies
        LibraryItem c4 = LibraryItemFactory.createCd("Album4", "Singer4", "22.0", 5);
        assertTrue(c4 instanceof CD);
        assertEquals(BigDecimal.valueOf(22.0), c4.getPrice());
    }

    // ----------------------------------------------------------
    //  Direct overloads coverage: createJournal(...)
    // ----------------------------------------------------------

    @Test
    void testCreateJournal_AllOverloads() {
        // default price, 1 copy
        LibraryItem j1 = LibraryItemFactory.createJournal(
                "J1", "Ed1", "Issue1");
        assertTrue(j1 instanceof Journal);

        // explicit price, 1 copy
        LibraryItem j2 = LibraryItemFactory.createJournal(
                "J2", "Ed2", "Issue2", "18.0");
        assertTrue(j2 instanceof Journal);
        assertEquals(BigDecimal.valueOf(18.0), j2.getPrice());

        // default price, multiple copies
        LibraryItem j3 = LibraryItemFactory.createJournal(
                "J3", "Ed3", "Issue3", 3);
        assertTrue(j3 instanceof Journal);

        // explicit price, multiple copies
        LibraryItem j4 = LibraryItemFactory.createJournal(
                "J4", "Ed4", "Issue4", "27.5", 4);
        assertTrue(j4 instanceof Journal);
        assertEquals(BigDecimal.valueOf(27.5), j4.getPrice());
    }
}
