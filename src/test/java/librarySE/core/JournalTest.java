package librarySE.core;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import librarySE.utils.Config;

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

    // ----------------------------------------------------------
    // Constructor Tests
    // ----------------------------------------------------------
    @Test
    void testConstructor_ValidData() {
        assertEquals("AI Monthly", journal.getTitle());
        assertEquals("Dr. Smith", journal.getEditor());
        assertEquals("Vol. 10", journal.getIssueNumber());
        assertEquals(BigDecimal.valueOf(30), journal.getPrice());
        assertEquals(1, journal.getTotalCopies());
        assertEquals(1, journal.getAvailableCopies());
    }

    @Test
    void testConstructor_WithTotalCopies_Valid() {
        Journal j = new Journal("AI Research", "Editor", "Vol. 5",
                BigDecimal.TEN, 4);
        assertEquals("AI Research", j.getTitle());
        assertEquals("Editor", j.getEditor());
        assertEquals("Vol. 5", j.getIssueNumber());
        assertEquals(BigDecimal.TEN, j.getPrice());
        assertEquals(4, j.getTotalCopies());
        assertEquals(4, j.getAvailableCopies());
    }

    @Test
    void testConstructor_WithTotalCopies_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Journal("Title", "Editor", "Issue",
                        BigDecimal.TEN, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new Journal("Title", "Editor", "Issue",
                        BigDecimal.TEN, -3));
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
    void testConstructor_NegativePrice_UsesDefault() {
        double defaultPrice = Config.getDouble("price.journal.default", 0.0);
        Journal j = new Journal("Negative", "Editor", "Issue",
                BigDecimal.valueOf(-10));
        assertEquals(BigDecimal.valueOf(defaultPrice), j.getPrice());
    }

    @Test
    void testConstructor_InvalidTitle_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Journal("", "Editor", "Issue", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new Journal("   ", "Editor", "Issue", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new Journal(null, "Editor", "Issue", BigDecimal.TEN));
    }

    @Test
    void testConstructor_InvalidEditor_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Journal("Title", "  ", "Issue", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new Journal("Title", "", "Issue", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new Journal("Title", null, "Issue", BigDecimal.TEN));
    }

    @Test
    void testConstructor_InvalidIssue_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Journal("Title", "Editor", "", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new Journal("Title", "Editor", "   ", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new Journal("Title", "Editor", null, BigDecimal.TEN));
    }

    // ----------------------------------------------------------
    // Setter Tests
    // ----------------------------------------------------------
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

    // ----------------------------------------------------------
    // Copy Management / setTotalCopies branches
    // ----------------------------------------------------------
    @Test
    void testInitialCopies_DefaultSingleCopy() {
        assertEquals(1, journal.getTotalCopies());
        assertEquals(1, journal.getAvailableCopies());
    }

    @Test
    void testSetTotalCopies_ValidIncreaseAndDecrease_ClampsToZero() {
        journal.setTotalCopies(3);
        assertEquals(3, journal.getTotalCopies());
        assertEquals(3, journal.getAvailableCopies());

        journal.borrow();
        journal.borrow();
        assertEquals(1, journal.getAvailableCopies());

        journal.setTotalCopies(1);
        assertEquals(1, journal.getTotalCopies());
        assertEquals(0, journal.getAvailableCopies());
    }

    @Test
    void testSetTotalCopies_InvalidNewTotal_Throws() {
        assertThrows(IllegalArgumentException.class, () -> journal.setTotalCopies(0));
        assertThrows(IllegalArgumentException.class, () -> journal.setTotalCopies(-4));
    }

    @Test
    void testSetTotalCopies_ClampsWhenAvailableGreaterThanTotal() throws Exception {
        Field totalField = Journal.class.getDeclaredField("totalCopies");
        Field availableField = Journal.class.getDeclaredField("availableCopies");
        totalField.setAccessible(true);
        availableField.setAccessible(true);

        totalField.setInt(journal, 3);
        availableField.setInt(journal, 10); // availableCopies > totalCopies

        journal.setTotalCopies(3);

        assertEquals(3, journal.getTotalCopies());
        assertEquals(3, journal.getAvailableCopies());
    }

    @Test
    void testSetTotalCopies_ClampsNegativeAvailableToZero() throws Exception {
        Field totalField = Journal.class.getDeclaredField("totalCopies");
        Field availableField = Journal.class.getDeclaredField("availableCopies");
        totalField.setAccessible(true);
        availableField.setAccessible(true);

        totalField.setInt(journal, 5);
        availableField.setInt(journal, -2); // availableCopies < 0

        journal.setTotalCopies(5);

        assertEquals(5, journal.getTotalCopies());
        assertEquals(0, journal.getAvailableCopies());
    }

    // ----------------------------------------------------------
    // Keyword Matching
    // ----------------------------------------------------------
    @Test
    void testMatchesKeyword_TitleMatch() {
        assertTrue(journal.matchesKeyword("AI"));
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
    void testMatchesKeyword_EmptyOrSpaces_Throws() {
        assertThrows(IllegalArgumentException.class, () -> journal.matchesKeyword(""));
        assertThrows(IllegalArgumentException.class, () -> journal.matchesKeyword("   "));
    }

    // ----------------------------------------------------------
    // Borrow / Return Tests (with exceptions)
    // ----------------------------------------------------------
    @Test
    void testBorrow_Success() {
        assertTrue(journal.borrow());
        assertFalse(journal.isAvailable());
        assertEquals(0, journal.getAvailableCopies());
    }

    @Test
    void testBorrow_AlreadyBorrowed_ReturnsFalse() {
        // first borrow succeeds
        assertTrue(journal.borrow());
        assertEquals(0, journal.getAvailableCopies());

        // second borrow should fail and not change state
        boolean secondResult = journal.borrow();
        assertFalse(secondResult, "Second borrow on single-copy journal must fail");
        assertEquals(0, journal.getAvailableCopies(), "Available copies must stay at 0");
    }

    @Test
    void testDoBorrow_ThrowsWhenNoCopies() throws Exception {
        // force internal state to 0 available copies
        Field availableField = Journal.class.getDeclaredField("availableCopies");
        availableField.setAccessible(true);
        availableField.setInt(journal, 0);

        // direct call to protected doBorrow must throw
        assertThrows(IllegalStateException.class, () -> journal.doBorrow());
    }

    @Test
    void testReturn_Success() {
        journal.borrow();
        assertTrue(journal.returnItem());
        assertTrue(journal.isAvailable());
        assertEquals(1, journal.getAvailableCopies());
    }

    @Test
    void testReturn_NotBorrowed_Throws() {
        assertThrows(IllegalStateException.class, () -> journal.returnItem());
    }

    // ----------------------------------------------------------
    // getMaterialType
    // ----------------------------------------------------------
    @Test
    void testGetMaterialType() {
        assertEquals(MaterialType.JOURNAL, journal.getMaterialType());
    }

    // ----------------------------------------------------------
    // equals / hashCode (no override)
    // ----------------------------------------------------------
    @Test
    void testEquals_SameReference_True() {
        Journal j2 = journal;
        assertTrue(journal.equals(j2));
    }

    @Test
    void testEquals_DifferentObject_False() {
        Journal j2 = new Journal("AI Monthly", "Dr. Smith", "Vol. 10", BigDecimal.valueOf(30));
        assertFalse(journal.equals(j2));
    }

    @Test
    void testEquals_DifferentFields_False() {
        Journal j2 = new Journal("Other", "X", "Vol. 99", BigDecimal.valueOf(30));
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

    // ----------------------------------------------------------
    // toString Tests (Cover both states)
    // ----------------------------------------------------------
    @Test
    void testToString_Available() {
        String output = journal.toString();
        assertTrue(output.contains("JOURNAL"));
        assertTrue(output.contains("Available"));
        assertTrue(output.contains(journal.getTitle()));
        assertTrue(output.contains(journal.getIssueNumber()));
        assertTrue(output.contains(journal.getEditor()));
    }

    @Test
    void testToString_FullyBorrowed() {
        journal.borrow();
        String output = journal.toString();
        assertTrue(output.contains("Fully borrowed"));
        assertTrue(output.contains(journal.getTitle()));
    }
}
