package librarySE.core;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import librarySE.utils.Config;

/**
 * Unit tests for {@link Journal}.
 */
class JournalTest {

    private Journal journal;

    @BeforeEach
    void setUp() {
        journal = new Journal("AI Monthly", "Dr. Smith", "Vol. 10", BigDecimal.valueOf(30));
    }

    @AfterEach
    void tearDown() {
        journal = null;
    }

    // Constructor Tests
    @Test
    void testConstructor_ValidData() {
        assertEquals("AI Monthly", journal.getTitle());
        assertEquals("Dr. Smith", journal.getEditor());
        assertEquals("Vol. 10", journal.getIssueNumber());
        assertEquals(BigDecimal.valueOf(30), journal.getPrice());
    }

    @Test
    void testConstructor_PriceNull_UsesDefault() {
        double defaultPrice = Config.getDouble("price.journal.default", 0.0);
        Journal j = new Journal("Nature", "Editor", "Vol 5", null);
        assertEquals(BigDecimal.valueOf(defaultPrice), j.getPrice());
    }

    @Test
    void testConstructor_PriceZero_UsesDefault() {
        double defaultPrice = Config.getDouble("price.journal.default", 0.0);
        Journal j = new Journal("Nature", "Editor", "Vol 5", BigDecimal.ZERO);
        assertEquals(BigDecimal.valueOf(defaultPrice), j.getPrice());
    }

    @Test
    void testConstructor_InvalidTitle_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Journal("", "Editor", "Issue", BigDecimal.TEN));
    }

    @Test
    void testConstructor_InvalidEditor_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Journal("Title", "  ", "Issue", BigDecimal.TEN));
    }

    @Test
    void testConstructor_InvalidIssueNumber_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Journal("Title", "Editor", "", BigDecimal.TEN));
    }

    // Setter Tests
    @Test
    void testSetTitle_Valid() {
        journal.setTitle("New Title");
        assertEquals("New Title", journal.getTitle());
    }

    @Test
    void testSetTitle_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class, () -> journal.setTitle(""));
        assertThrows(IllegalArgumentException.class, () -> journal.setTitle(" "));
        assertThrows(IllegalArgumentException.class, () -> journal.setTitle(null));
    }

    @Test
    void testSetEditor_Valid() {
        journal.setEditor("New Editor");
        assertEquals("New Editor", journal.getEditor());
    }

    @Test
    void testSetEditor_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class, () -> journal.setEditor(""));
        assertThrows(IllegalArgumentException.class, () -> journal.setEditor("  "));
        assertThrows(IllegalArgumentException.class, () -> journal.setEditor(null));
    }

    @Test
    void testSetIssueNumber_Valid() {
        journal.setIssueNumber("Vol. 20");
        assertEquals("Vol. 20", journal.getIssueNumber());
    }

    @Test
    void testSetIssueNumber_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class, () -> journal.setIssueNumber(""));
        assertThrows(IllegalArgumentException.class, () -> journal.setIssueNumber("  "));
        assertThrows(IllegalArgumentException.class, () -> journal.setIssueNumber(null));
    }

    // matchesKeyword Tests
    @Test
    void testMatchesKeyword_TitleMatch() {
        assertTrue(journal.matchesKeyword("Monthly"));
    }

    @Test
    void testMatchesKeyword_EditorMatch() {
        assertTrue(journal.matchesKeyword("Smith"));
    }

    @Test
    void testMatchesKeyword_IssueMatch() {
        assertTrue(journal.matchesKeyword("Vol"));
    }

    @Test
    void testMatchesKeyword_NoMatch() {
        assertFalse(journal.matchesKeyword("XYZ"));
    }

    @Test
    void testMatchesKeyword_Null_Throws() {
        assertThrows(IllegalArgumentException.class, () -> journal.matchesKeyword(null));
    }

    @Test
    void testMatchesKeyword_Empty_Throws() {
        assertThrows(IllegalArgumentException.class, () -> journal.matchesKeyword(""));
    }

    // Borrow / Return Tests
    @Test
    void testBorrow_Success() {
        assertTrue(journal.borrow());
        assertFalse(journal.isAvailable());
    }

    @Test
    void testBorrow_FailsWhenAlreadyBorrowed() {
        journal.borrow();
        assertFalse(journal.borrow());
    }

    @Test
    void testReturn_Success() {
        journal.borrow();
        assertTrue(journal.returnItem());
        assertTrue(journal.isAvailable());
    }

    @Test
    void testReturn_FailsWhenNotBorrowed() {
        assertFalse(journal.returnItem());
    }

    // equals + hashCode Tests
    @Test
    void testEquals_SameFields_True() {
        Journal j2 = new Journal("AI Monthly", "Dr. Smith", "Vol. 10", BigDecimal.valueOf(30));
        assertTrue(journal.equals(j2));
        assertEquals(journal.hashCode(), j2.hashCode());
    }

    @Test
    void testEquals_DifferentTitle_False() {
        Journal j2 = new Journal("Other", "Dr. Smith", "Vol. 10", BigDecimal.valueOf(30));
        assertFalse(journal.equals(j2));
    }

    @Test
    void testEquals_DifferentEditor_False() {
        Journal j2 = new Journal("AI Monthly", "XXX", "Vol. 10", BigDecimal.valueOf(30));
        assertFalse(journal.equals(j2));
    }

    @Test
    void testEquals_DifferentIssue_False() {
        Journal j2 = new Journal("AI Monthly", "Dr. Smith", "Vol. 99", BigDecimal.valueOf(30));
        assertFalse(journal.equals(j2));
    }

    @Test
    void testEquals_Null_False() {
        assertFalse(journal.equals(null));
    }

    @Test
    void testEquals_DifferentType_False() {
        assertFalse(journal.equals("HELLO"));
    }

    // toString Test
    @Test
    void testToString_NotNull() {
        assertNotNull(journal.toString());
        assertTrue(journal.toString().contains("JOURNAL"));
    }
}

