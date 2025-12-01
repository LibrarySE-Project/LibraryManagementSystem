package librarySE.core;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Complete unit tests for {@link WaitlistEntry}.
 * Covers: constructor validation, getters, equals/hashCode,
 * toString(), and all negative scenarios.
 */
class WaitlistEntryTest {

    private UUID itemId;
    private String email;
    private LocalDate date;
    private WaitlistEntry entry;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        email = "student@najah.edu";
        date = LocalDate.now();
        entry = new WaitlistEntry(itemId, email, date);
    }

    @AfterEach
    void tearDown() {
        entry = null;
    }

    // ----------------------------------------------------------
    //  Constructor Tests
    // ----------------------------------------------------------

    @Test
    void testConstructor_ValidData() {
        assertEquals(itemId, entry.getItemId());
        assertEquals(email, entry.getUserEmail());
        assertEquals(date, entry.getRequestDate());
    }

    @Test
    void testConstructor_NullUUID_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new WaitlistEntry(null, email, date));
    }

    @Test
    void testConstructor_EmptyEmail_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new WaitlistEntry(itemId, "", date));
    }

    @Test
    void testConstructor_BlankEmail_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new WaitlistEntry(itemId, "   ", date));
    }

    @Test
    void testConstructor_NullEmail_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new WaitlistEntry(itemId, null, date));
    }

    @Test
    void testConstructor_NullDate_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new WaitlistEntry(itemId, email, null));
    }

    // ----------------------------------------------------------
    //  Getter Tests
    // ----------------------------------------------------------

    @Test
    void testGetItemId() {
        assertEquals(itemId, entry.getItemId());
    }

    @Test
    void testGetUserEmail() {
        assertEquals(email, entry.getUserEmail());
    }

    @Test
    void testGetRequestDate() {
        assertEquals(date, entry.getRequestDate());
    }

    // ----------------------------------------------------------
    //  equals + hashCode Tests
    // ----------------------------------------------------------

    @Test
    void testEquals_SameReference_True() {
        assertTrue(entry.equals(entry)); // self comparison
    }

    @Test
    void testEquals_SameFields_True() {
        WaitlistEntry e2 = new WaitlistEntry(itemId, email, date);
        assertTrue(entry.equals(e2));
        assertEquals(entry.hashCode(), e2.hashCode());
    }

    @Test
    void testEquals_DifferentUUID_False() {
        WaitlistEntry e2 = new WaitlistEntry(UUID.randomUUID(), email, date);
        assertFalse(entry.equals(e2));
    }

    @Test
    void testEquals_DifferentEmail_False() {
        WaitlistEntry e2 = new WaitlistEntry(itemId, "other@najah.edu", date);
        assertFalse(entry.equals(e2));
    }

    @Test
    void testEquals_DifferentDate_False() {
        WaitlistEntry e2 = new WaitlistEntry(itemId, email, date.minusDays(1));
        assertFalse(entry.equals(e2));
    }

    @Test
    void testEquals_Null_False() {
        assertFalse(entry.equals(null));
    }

    @Test
    void testEquals_DifferentType_False() {
        assertFalse(entry.equals("STRING"));
    }

    // ----------------------------------------------------------
    //  toString Test
    // ----------------------------------------------------------

    @Test
    void testToString_ContainsAllFields() {
        String output = entry.toString();

        assertNotNull(output);
        assertTrue(output.contains("WaitlistEntry"));
        assertTrue(output.contains(itemId.toString()));
        assertTrue(output.contains(email));
        assertTrue(output.contains(date.toString()));
    }
}
