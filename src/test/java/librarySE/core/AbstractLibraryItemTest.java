package librarySE.core;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Tests validation and core behaviour inside {@link AbstractLibraryItem}.
 */
class AbstractLibraryItemTest {

    /**
     * Minimal concrete item to test normal behaviour of AbstractLibraryItem.
     */
    static class TestItem extends AbstractLibraryItem {

        TestItem(int total) {
            super(total);
        }

        @Override
        public String getTitle() {
            return "Test";
        }

        @Override
        public MaterialType getMaterialType() {
            return MaterialType.BOOK;
        }

        @Override
        public boolean matchesKeyword(String keyword) {
            return false;
        }

        @Override
        protected String getDisplayNameForMessages() {
            return "TestItem";
        }
    }

    /**
     * Concrete item exposing doBorrow() for exception testing.
     */
    static class BorrowProbeItem extends AbstractLibraryItem {

        BorrowProbeItem(int total) {
            super(total);
        }

        @Override
        public String getTitle() {
            return "BorrowProbe";
        }

        @Override
        public MaterialType getMaterialType() {
            return MaterialType.BOOK;
        }

        @Override
        public boolean matchesKeyword(String keyword) {
            return false;
        }

        @Override
        protected String getDisplayNameForMessages() {
            return "BorrowProbeItem";
        }

        public boolean callDoBorrowDirect() {
            return super.doBorrow();
        }
    }

    private static void setIntField(AbstractLibraryItem target, String fieldName, int value) throws Exception {
        Field f = AbstractLibraryItem.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.setInt(target, value);
    }

    private static int getIntField(AbstractLibraryItem target, String fieldName) throws Exception {
        Field f = AbstractLibraryItem.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.getInt(target);
    }

    // ---------------------------------------------------------
    // Constructor & identity
    // ---------------------------------------------------------

    @Test
    void testConstructor_InvalidTotalCopies_Throws() {
        assertThrows(IllegalArgumentException.class, () -> new TestItem(0));
        assertThrows(IllegalArgumentException.class, () -> new TestItem(-5));
    }

    @Test
    void testConstructor_ValidInitialCopiesAndIdNotNull() {
        TestItem item = new TestItem(3);
        assertEquals(3, item.getTotalCopies());
        assertEquals(3, item.getAvailableCopies());

        UUID id = item.getId();
        assertNotNull(id);
    }

    // ---------------------------------------------------------
    // Price validation
    // ---------------------------------------------------------

    @Test
    void testInitialPrice_IsZero() {
        TestItem item = new TestItem(1);
        assertEquals(BigDecimal.ZERO, item.getPrice());
    }

    @Test
    void testSetPrice_Positive_Succeeds() {
        TestItem item = new TestItem(1);
        item.setPrice(BigDecimal.valueOf(12.5));
        assertEquals(BigDecimal.valueOf(12.5), item.getPrice());
    }

    @Test
    void testSetPrice_Null_ThrowsNullPointerException() {
        TestItem item = new TestItem(1);
        assertThrows(NullPointerException.class, () -> item.setPrice(null));
    }

    @Test
    void testSetPrice_Negative_ThrowsIllegalArgumentException() {
        TestItem item = new TestItem(1);
        assertThrows(IllegalArgumentException.class, () -> item.setPrice(BigDecimal.valueOf(-1)));
    }

    // ---------------------------------------------------------
    // Lazy lock initialization
    // ---------------------------------------------------------

    @Test
    void testGetLock_LazyInitializationWhenNull() throws Exception {
        TestItem item = new TestItem(1);

        Field lockField = AbstractLibraryItem.class.getDeclaredField("lock");
        lockField.setAccessible(true);
        lockField.set(item, null);

        assertDoesNotThrow(item::isAvailable);
    }

    // ---------------------------------------------------------
    // setTotalCopies() coverage
    // ---------------------------------------------------------

    @Test
    void testSetTotalCopies_Invalid_Throws() {
        TestItem item = new TestItem(2);
        assertThrows(IllegalArgumentException.class, () -> item.setTotalCopies(0));
        assertThrows(IllegalArgumentException.class, () -> item.setTotalCopies(-3));
    }

    @Test
    void testSetTotalCopies_IncreaseTotalCopies() {
        TestItem item = new TestItem(2);
        item.setTotalCopies(5);
        assertEquals(5, item.getTotalCopies());
        assertEquals(5, item.getAvailableCopies());
    }

    @Test
    void testSetTotalCopies_DecreaseTotal_ClampsAvailableToZero() {
        TestItem item = new TestItem(5);
        item.borrow();
        item.borrow();

        item.setTotalCopies(2);

        assertEquals(2, item.getTotalCopies());
        assertEquals(0, item.getAvailableCopies());
    }

    @Test
    void testSetTotalCopies_ClampWhenAvailableExceedsTotal() throws Exception {
        TestItem item = new TestItem(3);
        setIntField(item, "totalCopies", 3);
        setIntField(item, "availableCopies", 10);

        item.setTotalCopies(3);

        assertEquals(3, item.getTotalCopies());
        assertEquals(3, item.getAvailableCopies());
    }

    @Test
    void testSetTotalCopies_ClampWhenAvailableNegative() throws Exception {
        TestItem item = new TestItem(3);
        setIntField(item, "totalCopies", 3);
        setIntField(item, "availableCopies", -5);

        item.setTotalCopies(3);

        assertEquals(3, item.getTotalCopies());
        assertEquals(0, item.getAvailableCopies());
    }

    // ---------------------------------------------------------
    // doBorrow() and doReturn() branches
    // ---------------------------------------------------------

    @Test
    void testBorrow_NormalFlow() {
        TestItem item = new TestItem(1);

        assertTrue(item.borrow());
        assertEquals(0, item.getAvailableCopies());

        assertFalse(item.borrow());
        assertEquals(0, item.getAvailableCopies());
    }

    @Test
    void testDoBorrow_IllegalStateExceptionBranchWhenAvailableZero() throws Exception {
        BorrowProbeItem item = new BorrowProbeItem(1);
        setIntField(item, "availableCopies", 0);

        assertThrows(IllegalStateException.class, item::callDoBorrowDirect);
    }

    @Test
    void testDoReturn_WhenValid_Succeeds() {
        TestItem item = new TestItem(1);
        item.borrow();
        assertTrue(item.returnItem());
        assertEquals(1, item.getAvailableCopies());
    }

    @Test
    void testDoReturn_WhenAllCopiesAlreadyReturned_Throws() {
        TestItem item = new TestItem(1);
        assertThrows(IllegalStateException.class, item::returnItem);
    }

    // ---------------------------------------------------------
    // isAvailable()
    // ---------------------------------------------------------

    @Test
    void testIsAvailable_TrueThenFalse() {
        TestItem item = new TestItem(1);
        assertTrue(item.isAvailable());
        item.borrow();
        assertFalse(item.isAvailable());
    }
}
