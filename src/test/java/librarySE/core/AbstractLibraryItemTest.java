package librarySE.core;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Full unit tests for {@link AbstractLibraryItem}.
 * Includes positive, negative, edge-cases, and thread-safety.
 */
class AbstractLibraryItemTest {

    /**
     * Dummy subclass to test the abstract behavior of {@link AbstractLibraryItem}.
     *
     * <p>
     * This implementation simulates a single-copy item:
     * initially available, one successful borrow, and one successful return.
     * Any invalid operation (borrowing when already borrowed, or returning when
     * not borrowed) throws {@link IllegalStateException}, matching the contract
     * of {@link AbstractLibraryItem#borrow()} and {@link AbstractLibraryItem#returnItem()}.
     * </p>
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
            return false;
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
    // BASIC FUNCTIONALITY TESTS
    // ------------------------------------------------------------

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
        assertTrue(item.borrow());
        assertFalse(item.isAvailable());
    }

    @Test
    void testBorrow_FailsWhenAlreadyBorrowed() {
        item.borrow();
        assertThrows(IllegalStateException.class, () -> item.borrow());
    }

    @Test
    void testReturn_Success() {
        item.borrow();
        assertTrue(item.returnItem());
        assertTrue(item.isAvailable());
    }

    @Test
    void testReturn_FailsWhenNotBorrowed() {
        assertThrows(IllegalStateException.class, () -> item.returnItem());
    }

    // ------------------------------------------------------------
    // PRICE TESTS
    // ------------------------------------------------------------

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
    void testSetPrice_Null_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> item.setPrice(null));
    }

    @Test
    void testSetPrice_Negative_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> item.setPrice(BigDecimal.valueOf(-10)));
    }

    @Test
    void testSetPrice_ZeroValue_IsAllowed() {
        assertDoesNotThrow(() -> item.setPrice(BigDecimal.ZERO));
        assertEquals(BigDecimal.ZERO, item.getPrice());
    }

    @Test
    void testSetPrice_LargeValue_Success() {
        BigDecimal large = new BigDecimal("999999999999.99");
        item.setPrice(large);
        assertEquals(large, item.getPrice());
    }

    // ------------------------------------------------------------
    // THREAD SAFETY TESTS
    // ------------------------------------------------------------

    @Test
    void testBorrow_ThreadSafety() throws InterruptedException {
        int threads = 50;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    if (item.borrow()) {
                        successCount.incrementAndGet();
                    }
                } catch (IllegalStateException ignored) {
                    // expected for losing threads
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        // Only ONE thread should succeed
        assertEquals(1, successCount.get());
        assertFalse(item.isAvailable());
    }

    @Test
    void testReturn_ThreadSafety() throws InterruptedException {
        item.borrow(); // make it borrowed first

        int threads = 50;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    if (item.returnItem()) {
                        successCount.incrementAndGet();
                    }
                } catch (IllegalStateException ignored) {
                    // expected for losing threads
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        // Only one thread should manage to return it
        assertEquals(1, successCount.get());
        assertTrue(item.isAvailable());
    }

    // ------------------------------------------------------------
    // EXTRA EDGE CASE TESTS
    // ------------------------------------------------------------

    @Test
    void testToString_NotNull() {
        assertNotNull(item.toString());
    }

    @Test
    void testPriceDoesNotChangeWhenExceptionThrown() {
        assertEquals(BigDecimal.ZERO, item.getPrice());
        try {
            item.setPrice(BigDecimal.valueOf(-5)); // throws
        } catch (Exception ignored) {}

        // Price must remain unchanged
        assertEquals(BigDecimal.ZERO, item.getPrice());
    }

    @Test
    void testMultipleBorrowReturnCycles() {
        for (int i = 0; i < 5; i++) {
            assertTrue(item.borrow());
            assertFalse(item.isAvailable());
            assertTrue(item.returnItem());
            assertTrue(item.isAvailable());
        }
    }
}
