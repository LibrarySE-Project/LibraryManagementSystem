package librarySE.core;

import static org.junit.jupiter.api.Assertions.*;

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
    }

    @Test
    void testConstructor_InvalidArtist_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CD("Album", "  ", BigDecimal.TEN));
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
    }

    // ----------------------------------------------------------
    //  Borrow / Return (Inherited)
    // ----------------------------------------------------------
    @Test
    void testBorrow_Success() {
        assertTrue(cd.borrow());
        assertFalse(cd.isAvailable());
    }

    @Test
    void testBorrow_FailsWhenAlreadyBorrowed() {
        cd.borrow();
        assertFalse(cd.borrow());
    }

    @Test
    void testReturn_Success() {
        cd.borrow();
        assertTrue(cd.returnItem());
        assertTrue(cd.isAvailable());
    }

    @Test
    void testReturn_FailsWhenNotBorrowed() {
        assertFalse(cd.returnItem());
    }
    @Test
    void testGetMaterialType() {
        assertEquals(MaterialType.CD, cd.getMaterialType());
    }


	 // ----------------------------------------------------------
	//  equals / hashCode
	// ----------------------------------------------------------
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
    void testToString_Borrowed() {
        cd.borrow();
        String output = cd.toString();
        assertTrue(output.contains("Borrowed"));
    }
}
