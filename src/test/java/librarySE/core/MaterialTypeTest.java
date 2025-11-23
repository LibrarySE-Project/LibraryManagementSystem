package librarySE.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import librarySE.strategy.FineStrategy;
import librarySE.strategy.FineStrategyFactory;

/**
 * Unit tests for {@link MaterialType}.
 */
class MaterialTypeTest {

    // Strategy Creation Tests
    @Test
    void testBookStrategy_NotNull() {
        FineStrategy strategy = MaterialType.BOOK.createFineStrategy();
        assertNotNull(strategy);
        assertEquals(FineStrategyFactory.book().getClass(), strategy.getClass());
    }

    @Test
    void testCDStrategy_NotNull() {
        FineStrategy strategy = MaterialType.CD.createFineStrategy();
        assertNotNull(strategy);
        assertEquals(FineStrategyFactory.cd().getClass(), strategy.getClass());
    }

    @Test
    void testJournalStrategy_NotNull() {
        FineStrategy strategy = MaterialType.JOURNAL.createFineStrategy();
        assertNotNull(strategy);
        assertEquals(FineStrategyFactory.journal().getClass(), strategy.getClass());
    }

    // Enum Basic Behavior Tests
    @Test
    void testValuesLength() {
        MaterialType[] values = MaterialType.values();
        assertEquals(3, values.length);
    }

    @Test
    void testValueOf_Book() {
        assertEquals(MaterialType.BOOK, MaterialType.valueOf("BOOK"));
    }

    @Test
    void testValueOf_CD() {
        assertEquals(MaterialType.CD, MaterialType.valueOf("CD"));
    }

    @Test
    void testValueOf_Journal() {
        assertEquals(MaterialType.JOURNAL, MaterialType.valueOf("JOURNAL"));
    }

    @Test
    void testValueOf_Invalid_Throws() {
        assertThrows(IllegalArgumentException.class, () -> MaterialType.valueOf("INVALID"));
    }
}

