package librarySE.core;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

/**
 * Unit tests for {@link AbstractLibraryItem}.
 */
class AbstractLibraryItemTest {

    /**
     * Simple concrete item with one copy that can be borrowed and returned.
     */
    static class DummyItem extends AbstractLibraryItem {

        private boolean available = true;

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
            return "match".equalsIgnoreCase(keyword);
        }

        @Override
        protected boolean isAvailableInternal() {
            return available;
        }

        @Override
        protected boolean doBorrow() {
            if (!available) {
                throw new IllegalStateException("Item already borrowed");
            }
            available = false;
            return true;
        }

        @Override
        protected boolean doReturn() {
            if (available) {
                throw new IllegalStateException("Item is not currently borrowed");
            }
            available = true;
            return true;
        }
    }

    /**
     * Item that is never available; used to cover the branch where
     * borrow() returns false without invoking doBorrow().
     */
    static class AlwaysUnavailableItem extends AbstractLibraryItem {

        boolean borrowCalled = false;

        @Override
        public String getTitle() {
            return "Unavailable";
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
        protected boolean isAvailableInternal() {
            return false;
        }

        @Override
        protected boolean doBorrow() {
            borrowCalled = true;
            return true;
        }

        @Override
        protected boolean doReturn() {
            return false;
        }
    }

    private DummyItem item;

    @BeforeEach
    void setUp() {
        item = new DummyItem();
    }

    @AfterEach
    void tearDown() {
        item = null;
    }

    // ------------------------------------------------------------
    // Basic functionality
    // ------------------------------------------------------------

    @Test
    void testGetId_NotNull() {
        UUID id = item.getId();
        assertNotNull(id);
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
    void testSetPrice_ZeroAllowed() {
        assertDoesNotThrow(() -> item.setPrice(BigDecimal.ZERO));
        assertEquals(BigDecimal.ZERO, item.getPrice());
    }

    @Test
    void testSetPrice_Null_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> item.setPrice(null));
    }

    @Test
    void testSetPrice_Negative_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> item.setPrice(BigDecimal.valueOf(-1)));
    }

    @Test
    void testPriceUnchangedWhenExceptionThrown() {
        assertEquals(BigDecimal.ZERO, item.getPrice());
        try {
            item.setPrice(BigDecimal.valueOf(-5));
        } catch (Exception ignored) {
        }
        assertEquals(BigDecimal.ZERO, item.getPrice());
    }

    // ------------------------------------------------------------
    // Availability and borrow/return
    // ------------------------------------------------------------

    @Test
    void testInitialAvailability_IsTrue() {
        assertTrue(item.isAvailable());
    }

    @Test
    void testBorrow_Success() {
        assertTrue(item.borrow());
        assertFalse(item.isAvailable());
    }

    @Test
    void testBorrow_SecondTimeFailsWithoutException() {
        // first borrow succeeds
        assertTrue(item.borrow());
        assertFalse(item.isAvailable());

        // second borrow should fail gracefully (no exception) and return false
        boolean secondResult = item.borrow();
        assertFalse(secondResult, "Second borrow on single-copy item must fail");
        assertFalse(item.isAvailable(), "Item must remain unavailable after failed second borrow");
    }

    @Test
    void testReturn_Success() {
        item.borrow();
        assertTrue(item.returnItem());
        assertTrue(item.isAvailable());
    }

    @Test
    void testReturn_WhenNotBorrowed_Throws() {
        assertThrows(IllegalStateException.class, () -> item.returnItem());
    }

    @Test
    void testMultipleBorrowReturnCycles() {
        for (int i = 0; i < 3; i++) {
            assertTrue(item.borrow());
            assertFalse(item.isAvailable());
            assertTrue(item.returnItem());
            assertTrue(item.isAvailable());
        }
    }

    @Test
    void testBorrowReturnsFalseWhenNotAvailableAndDoesNotCallDoBorrow() {
        AlwaysUnavailableItem it = new AlwaysUnavailableItem();

        boolean result = it.borrow();

        assertFalse(result);
        assertFalse(it.borrowCalled, "doBorrow() should not be called when not available");
    }

    // ------------------------------------------------------------
    // getLock lazy initialization branch
    // ------------------------------------------------------------

    @Test
    void testGetLock_LazyInitializationWhenNull() throws Exception {
        // set lock field to null via reflection to simulate deserialized object
        Field lockField = AbstractLibraryItem.class.getDeclaredField("lock");
        lockField.setAccessible(true);
        lockField.set(item, null);

        assertDoesNotThrow(() -> {
            // triggers getLock() and should reinitialize the lock
            boolean available = item.isAvailable();
            assertTrue(available);
        });
    }

    // ------------------------------------------------------------
    // Thread-safety behaviour
    // ------------------------------------------------------------

    @Test
    void testBorrow_ThreadSafety_AllowsOnlyOneSuccess() throws InterruptedException {
        int threads = 30;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    if (item.borrow()) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        assertEquals(1, successCount.get());
        assertFalse(item.isAvailable());
    }

    @Test
    void testReturn_ThreadSafety_AllowsOnlyOneSuccess() throws InterruptedException {
        item.borrow(); // make it borrowed first

        int threads = 30;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    if (item.returnItem()) {
                        successCount.incrementAndGet();
                    }
                } catch (IllegalStateException ignored) {
                    // losers: trying to return when already returned
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        assertEquals(1, successCount.get());
        assertTrue(item.isAvailable());
    }

    // ------------------------------------------------------------
    // Misc
    // ------------------------------------------------------------

    @Test
    void testToString_NotNull() {
        assertNotNull(item.toString());
    }
}
