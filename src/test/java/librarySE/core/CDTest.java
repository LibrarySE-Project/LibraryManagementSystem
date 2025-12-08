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

    // ----------------------------------------------------------
    //  Constructor Tests
    // ----------------------------------------------------------
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

    // ----------------------------------------------------------
    //  Setter Tests
    // ----------------------------------------------------------
    @Test
    void testSetTitle_Valid() {
        cd.setTitle("New Album");
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
    void testSetArtist_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class, () -> cd.setArtist(""));
        assertThrows(IllegalArgumentException.class, () -> cd.setArtist("  "));
        assertThrows(IllegalArgumentException.class, () -> cd.setArtist(null));
    }

    // ----------------------------------------------------------
    //  Copy Management
    // ----------------------------------------------------------
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
        CD c = new CD("Album", "Artist", BigDecimal.TEN, 3);
        // 3/3 copies available
        c.borrow(); // 2/3
        c.setTotalCopies(2); // delta = -1 → available = 1
        assertEquals(2, c.getTotalCopies());
        assertEquals(1, c.getAvailableCopies());

        c.borrow(); // avail = 0
        c.setTotalCopies(1);
        assertEquals(1, c.getTotalCopies());
        assertEquals(0, c.getAvailableCopies());
    }

    // ----------------------------------------------------------
    //  Keyword Matching
    // ----------------------------------------------------------
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
    void testMatchesKeyword_Empty_Throws() {
        assertThrows(IllegalArgumentException.class, () -> cd.matchesKeyword(""));
        assertThrows(IllegalArgumentException.class, () -> cd.matchesKeyword("   "));
    }

    // ----------------------------------------------------------
    //  Borrow / Return (Inherited with exceptions)
    // ----------------------------------------------------------
    @Test
    void testBorrow_Success() {
        assertTrue(cd.borrow());
        assertFalse(cd.isAvailable());
        assertEquals(0, cd.getAvailableCopies());
    }

    @Test
    void testBorrow_FailsWhenAlreadyBorrowed_Throws() {
        cd.borrow();
        assertThrows(IllegalStateException.class, () -> cd.borrow());
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

    @Test
    void testGetMaterialType() {
        assertEquals(MaterialType.CD, cd.getMaterialType());
    }

    @Test
    void testEquals_SameData_False() {
        CD c2 = new CD("Song Collection", "Famous Singer", BigDecimal.valueOf(40));
        assertFalse(cd.equals(c2));  // reference comparison only
    }

    @Test
    void testEquals_DifferentTitle_False() {
        CD c2 = new CD("Different", "Famous Singer", BigDecimal.valueOf(40));
        assertFalse(cd.equals(c2));
    }

    @Test
    void testEquals_DifferentArtist_False() {
        CD c2 = new CD("Song Collection", "Other Artist", BigDecimal.valueOf(40));
        assertFalse(cd.equals(c2));
    }

    @Test
    void testEquals_DifferentType_False() {
        assertFalse(cd.equals("String"));
    }

    @Test
    void testEquals_Null_False() {
        assertFalse(cd.equals(null));
    }

    // ----------------------------------------------------------
    //  toString()
    // ----------------------------------------------------------
    @Test
    void testToString_Available() {
        String output = cd.toString();
        assertTrue(output.contains("Available"));
        assertTrue(output.contains("CD"));
        assertTrue(output.contains(cd.getTitle()));
    }

    @Test
    void testToString_FullyBorrowed() {
        cd.borrow();
        String output = cd.toString();
        assertTrue(output.contains("Fully borrowed"));
    }
    @Test
    void testInitCopies_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CD("A","B", BigDecimal.TEN, 0));

        assertThrows(IllegalArgumentException.class,
                () -> new CD("A","B", BigDecimal.TEN, -3));
    }
    @Test
    void testSetTotalCopies_ClampsAvailableWithinBounds() {

        CD c = new CD("Album","Singer",BigDecimal.TEN,3);
        assertEquals(3, c.getAvailableCopies()); // 3/3

        c.borrow(); //2
        c.borrow(); //1

        c.setTotalCopies(1);  
        assertEquals(1, c.getTotalCopies());
        assertEquals(0, c.getAvailableCopies());  

        c.setTotalCopies(5);
        assertEquals(5, c.getTotalCopies());
        assertEquals(4, c.getAvailableCopies());

        c.borrow();  
        c.setTotalCopies(10); 
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
        availableField.setInt(c, 10);

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
        availableField.setInt(c, -5);

        c.setTotalCopies(2);

        assertEquals(2, c.getTotalCopies());
        assertEquals(0, c.getAvailableCopies());
    }



}
