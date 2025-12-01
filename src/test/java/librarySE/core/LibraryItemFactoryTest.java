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
    //  Book Creation Tests
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

        assertEquals(BigDecimal.valueOf(defaultPrice), item.getPrice());
    }

    // ----------------------------------------------------------
    //  CD Creation Tests
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

        assertEquals(BigDecimal.valueOf(defaultPrice), item.getPrice());
    }

    // ----------------------------------------------------------
    //  Journal Creation Tests
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

        assertEquals(BigDecimal.valueOf(defaultPrice), item.getPrice());
    }

    // ----------------------------------------------------------
    //  Missing-Args Negative Tests
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
    //  Invalid Price Negative Tests
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
    void testCreateBook_EmptyPrice_Throws() {
        assertThrows(NumberFormatException.class,
                () -> LibraryItemFactory.create(
                        MaterialType.BOOK,
                        "ISBN",
                        "Title",
                        "Author",
                        ""
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
}
