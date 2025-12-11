package librarySE.core;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import librarySE.utils.Config;

class CDTest {

    private CD cd;

    @BeforeEach
    void setUp() {
        cd = new CD("Song Collection", "Famous Singer", BigDecimal.valueOf(40));
    }

    @AfterEach
    void tearDown() {
        cd = null;
    }

    // ---------------------------------------------------------------------
    // Constructor tests
    // ---------------------------------------------------------------------

    @Test
    void testConstructor_ValidData() {
        assertEquals("Song Collection", cd.getTitle());
        assertEquals("Famous Singer", cd.getArtist());
        assertEquals(BigDecimal.valueOf(40), cd.getPrice());
        assertEquals(1, cd.getTotalCopies());
        assertEquals(1, cd.getAvailableCopies());
    }

    @Test
    void testConstructor_WithTotalCopies() {
        CD c = new CD("Album", "Artist", BigDecimal.TEN, 5);
        assertEquals("Album", c.getTitle());
        assertEquals("Artist", c.getArtist());
        assertEquals(BigDecimal.TEN, c.getPrice());
        assertEquals(5, c.getTotalCopies());
        assertEquals(5, c.getAvailableCopies());
    }

    @Test
    void testConstructor_PriceNull_UsesDefault() {
        double defaultPrice = Config.getDouble("price.cd.default", 0.0);
        CD c = new CD("Hits", "Artist", null);
        assertEquals(BigDecimal.valueOf(defaultPrice), c.getPrice());
    }

    @Test
    void testConstructor_PriceZero_UsesDefault() {
        double defaultPrice = Config.getDouble("price.cd.default", 0.0);
        CD c = new CD("Hits", "Artist", BigDecimal.ZERO);
        assertEquals(BigDecimal.valueOf(defaultPrice), c.getPrice());
    }

    @Test
    void testConstructor_InvalidTitle_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CD("", "Artist", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new CD("   ", "Artist", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new CD(null, "Artist", BigDecimal.TEN));
    }

    @Test
    void testConstructor_InvalidArtist_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CD("Album", "  ", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new CD("Album", "", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new CD("Album", null, BigDecimal.TEN));
    }

    @Test
    void testConstructor_TitleAndArtistAreTrimmed() {
        CD c = new CD("  Album  ", "  Singer  ", BigDecimal.TEN);
        assertEquals("Album", c.getTitle());
        assertEquals("Singer", c.getArtist());
    }

    @Test
    void testInitCopies_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CD("A", "B", BigDecimal.TEN, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new CD("A", "B", BigDecimal.TEN, -3));
    }

    // ---------------------------------------------------------------------
    // Setter tests
    // ---------------------------------------------------------------------

    @Test
    void testSetTitle_Valid() {
        cd.setTitle("New Album");
        assertEquals("New Album", cd.getTitle());
    }

    @Test
    void testSetTitle_Trimmed() {
        cd.setTitle("   New Album   ");
        assertEquals("New Album", cd.getTitle());
    }

    @Test
    void testSetTitle_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class, () -> cd.setTitle(""));
        assertThrows(IllegalArgumentException.class, () -> cd.setTitle("  "));
        assertThrows(IllegalArgumentException.class, () -> cd.setTitle(null));
    }

    @Test
    void testSetArtist_Valid() {
        cd.setArtist("New Artist");
        assertEquals("New Artist", cd.getArtist());
    }

    @Test
    void testSetArtist_Trimmed() {
        cd.setArtist("   New Artist   ");
        assertEquals("New Artist", cd.getArtist());
    }

    @Test
    void testSetArtist_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class, () -> cd.setArtist(""));
        assertThrows(IllegalArgumentException.class, () -> cd.setArtist("  "));
        assertThrows(IllegalArgumentException.class, () -> cd.setArtist(null));
    }

    // ---------------------------------------------------------------------
    // Copy management tests
    // ---------------------------------------------------------------------

    @Test
    void testInitialCopies_DefaultSingleCopy() {
        assertEquals(1, cd.getTotalCopies());
        assertEquals(1, cd.getAvailableCopies());
    }

    @Test
    void testSetTotalCopies_ValidIncrease() {
        cd.setTotalCopies(3);
        assertEquals(3, cd.getTotalCopies());
        assertEquals(3, cd.getAvailableCopies());
    }

    @Test
    void testSetTotalCopies_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class, () -> cd.setTotalCopies(0));
        assertThrows(IllegalArgumentException.class, () -> cd.setTotalCopies(-5));
    }

    @Test
    void testSetTotalCopies_AdjustsAvailableWithinRange() {
        CD c = new CD("Album", "Artist", BigDecimal.TEN, 3); // 3/3 available
        c.borrow(); // 2/3
        c.borrow(); // 1/3

        c.setTotalCopies(1); // delta = -2 → available = -1 → clamped to 0
        assertEquals(1, c.getTotalCopies());
        assertEquals(0, c.getAvailableCopies());

        c.setTotalCopies(5); // delta = +4 → available = 4
        assertEquals(5, c.getTotalCopies());
        assertEquals(4, c.getAvailableCopies());

        c.borrow();          // available = 3
        c.setTotalCopies(10); // delta = +5 → available = 8 (<= total)
        assertTrue(c.getAvailableCopies() <= c.getTotalCopies());
    }

    @Test
    void testSetTotalCopies_ClampWhenAvailableGreaterThanTotal_Reflection() throws Exception {
        CD c = new CD("A", "B", BigDecimal.TEN, 2);

        Field totalField = CD.class.getDeclaredField("totalCopies");
        Field availableField = CD.class.getDeclaredField("availableCopies");
        totalField.setAccessible(true);
        availableField.setAccessible(true);

        totalField.setInt(c, 2);
        availableField.setInt(c, 10); // force available > total

        c.setTotalCopies(2);

        assertEquals(2, c.getTotalCopies());
        assertEquals(2, c.getAvailableCopies());
    }

    @Test
    void testSetTotalCopies_ClampWhenAvailableNegative_Reflection() throws Exception {
        CD c = new CD("A", "B", BigDecimal.TEN, 2);

        Field totalField = CD.class.getDeclaredField("totalCopies");
        Field availableField = CD.class.getDeclaredField("availableCopies");
        totalField.setAccessible(true);
        availableField.setAccessible(true);

        totalField.setInt(c, 2);
        availableField.setInt(c, -5); // force available < 0

        c.setTotalCopies(2);

        assertEquals(2, c.getTotalCopies());
        assertEquals(0, c.getAvailableCopies());
    }

    // ---------------------------------------------------------------------
    // Keyword matching
    // ---------------------------------------------------------------------

    @Test
    void testMatchesKeyword_TitleMatch() {
        assertTrue(cd.matchesKeyword("Song"));
    }

    @Test
    void testMatchesKeyword_ArtistMatch() {
        assertTrue(cd.matchesKeyword("Famous"));
    }

    @Test
    void testMatchesKeyword_NoMatch() {
        assertFalse(cd.matchesKeyword("XYZ"));
    }

    @Test
    void testMatchesKeyword_Null_Throws() {
        assertThrows(IllegalArgumentException.class, () -> cd.matchesKeyword(null));
    }

    @Test
    void testMatchesKeyword_EmptyOrSpaces_Throws() {
        assertThrows(IllegalArgumentException.class, () -> cd.matchesKeyword(""));
        assertThrows(IllegalArgumentException.class, () -> cd.matchesKeyword("   "));
    }

    // ---------------------------------------------------------------------
    // Borrow / Return behaviour
    // ---------------------------------------------------------------------

    @Test
    void testBorrow_Success() {
        assertTrue(cd.borrow());
        assertFalse(cd.isAvailable());
        assertEquals(0, cd.getAvailableCopies());
    }

    @Test
    void testBorrow_FailsWhenAlreadyBorrowed_Throws() {
        // first borrow succeeds
        assertTrue(cd.borrow());
        assertEquals(0, cd.getAvailableCopies());

        // second borrow should fail safely (no exception)
        boolean secondResult = cd.borrow();
        assertFalse(secondResult, "Second borrow on single-copy CD must fail");
        assertEquals(0, cd.getAvailableCopies(), "Available copies must remain 0");
    }


    @Test
    void testReturn_Success() {
        cd.borrow();
        assertTrue(cd.returnItem());
        assertTrue(cd.isAvailable());
        assertEquals(1, cd.getAvailableCopies());
    }

    @Test
    void testReturn_FailsWhenNotBorrowed_Throws() {
        assertThrows(IllegalStateException.class, () -> cd.returnItem());
    }

    @Test
    void testMultipleBorrowReturnCycles_SingleCopy() {
        for (int i = 0; i < 3; i++) {
            assertTrue(cd.borrow());
            assertFalse(cd.isAvailable());
            assertTrue(cd.returnItem());
            assertTrue(cd.isAvailable());
        }
    }

    // ---------------------------------------------------------------------
    // Type and toString
    // ---------------------------------------------------------------------

    @Test
    void testGetMaterialType() {
        assertEquals(MaterialType.CD, cd.getMaterialType());
    }

    @Test
    void testToString_Available() {
        String output = cd.toString();
        assertTrue(output.contains("[CD]"));
        assertTrue(output.contains("Available"));
        assertTrue(output.contains(cd.getTitle()));
        assertTrue(output.contains(cd.getArtist()));
    }

    @Test
    void testToString_FullyBorrowed() {
        cd.borrow();
        String output = cd.toString();
        assertTrue(output.contains("Fully borrowed"));
    }

    // ---------------------------------------------------------------------
    // equals basic behaviour (inherited from Object)
    // ---------------------------------------------------------------------

    @Test
    void testEquals_SameInstance_True() {
        assertTrue(cd.equals(cd));
    }

    @Test
    void testEquals_DifferentInstance_False() {
        CD other = new CD("Song Collection", "Famous Singer", BigDecimal.valueOf(40));
        assertFalse(cd.equals(other));
    }

    @Test
    void testEquals_NullAndDifferentType_False() {
        assertFalse(cd.equals(null));
        assertFalse(cd.equals("not a cd"));
    }
}
