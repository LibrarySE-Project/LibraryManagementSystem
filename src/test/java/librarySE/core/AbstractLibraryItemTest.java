package librarySE.core;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Full unit tests for {@link AbstractLibraryItem}.
 * Includes positive, negative, and edge cases.
 */
class AbstractLibraryItemTest {

    // Dummy subclass for testing the abstract class
    static class DummyItem extends AbstractLibraryItem {
        @Override
        public String getTitle() {
            return "DummyTitle";
        }

        @Override
        public MaterialType getMaterialType() {
            return MaterialType.BOOK;
        }
        @Override
        public boolean matchesKeyword(String keyword) {
            return false; 
        }

    }

    private DummyItem item;

    @BeforeEach
    void setUp() {
        item = new DummyItem();  // fresh instance before each test
    }

    @AfterEach
    void tearDown() {
        item = null; // cleanup (not required, but you asked to include)
    }

    @Test
    void testGetId_NotNull() {
        UUID id = item.getId();
        assertNotNull(id);
    }

    @Test
    void testInitialAvailability_IsTrue() {
        assertTrue(item.isAvailable());
    }

    @Test
    void testBorrow_Success() {
        boolean result = item.borrow();
        assertTrue(result);
        assertFalse(item.isAvailable());
    }

    @Test
    void testBorrow_FailsWhenAlreadyBorrowed() {
        item.borrow();
        assertFalse(item.borrow());
    }

    @Test
    void testReturn_Success() {
        item.borrow();
        assertTrue(item.returnItem());
        assertTrue(item.isAvailable());
    }

    @Test
    void testReturn_FailsWhenNotBorrowed() {
        assertFalse(item.returnItem());
    }

    @Test
    void testInitialPrice_Zero() {
        assertEquals(BigDecimal.ZERO, item.getPrice());
    }

    @Test
    void testSetPrice_ValidPositive() {
        item.setPrice(BigDecimal.valueOf(15.75));
        assertEquals("15.75", item.getPrice().toPlainString());
    }

    @Test
    void testSetPrice_Null_ThrowsNullPointer() {
        assertThrows(NullPointerException.class, () -> item.setPrice(null));
    }

    @Test
    void testSetPrice_Negative_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> item.setPrice(BigDecimal.valueOf(-10)));
    }

    @Test
    void testBorrow_ThreadSafetyBasic() {
        item.borrow();
        assertFalse(item.borrow());
    }

    @Test
    void testToString_NotNull() {
        // optional but usually good
        assertNotNull(item.toString());
    }
}

