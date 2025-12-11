package librarySE.strategy;

import librarySE.utils.Config;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FineStrategyFactoryTest {

    // =====================================================
    // DEFAULT STRATEGIES (BOOK / CD / JOURNAL)
    // =====================================================

    @Test
    void testBookDefaults() {
        Config.reload();

        FineStrategy s = FineStrategyFactory.book();

        BigDecimal day1 = s.calculateFine(1);
        BigDecimal day2 = s.calculateFine(2);

        assertTrue(day1.compareTo(BigDecimal.ZERO) >= 0);
        assertEquals(day1.multiply(BigDecimal.valueOf(2)), day2);
        assertTrue(s.getBorrowPeriodDays() > 0);
    }

    @Test
    void testCDDefaults() {
        Config.reload();

        FineStrategy s = FineStrategyFactory.cd();

        BigDecimal day1 = s.calculateFine(1);
        BigDecimal day2 = s.calculateFine(2);

        assertTrue(day1.compareTo(BigDecimal.ZERO) >= 0);
        assertEquals(day1.multiply(BigDecimal.valueOf(2)), day2);
        assertTrue(s.getBorrowPeriodDays() > 0);
    }

    @Test
    void testJournalDefaults() {
        Config.reload();

        FineStrategy s = FineStrategyFactory.journal();

        BigDecimal day1 = s.calculateFine(1);
        BigDecimal day2 = s.calculateFine(2);

        assertTrue(day1.compareTo(BigDecimal.ZERO) >= 0);
        assertEquals(day1.multiply(BigDecimal.valueOf(2)), day2);
        assertTrue(s.getBorrowPeriodDays() > 0);
    }

    // =====================================================
    // OVERRIDDEN CONFIG VALUES
    // =====================================================

    @Test
    void testBookConfigOverrides() {
        Config.reload();

        Config.set("fine.book.rate", "5");
        Config.set("fine.book.period", "10");

        double expectedRate = Config.getDouble("fine.book.rate", 0.0);
        int expectedPeriod = Config.getInt("fine.book.period", 0);

        FineStrategy s = FineStrategyFactory.book();

        assertEquals(BigDecimal.valueOf(expectedRate), s.calculateFine(1));
        assertEquals(expectedPeriod, s.getBorrowPeriodDays());
    }

    @Test
    void testCDConfigOverrides() {
        Config.reload();

        Config.set("fine.cd.rate", "2.5");
        Config.set("fine.cd.period", "3");

        double expectedRate = Config.getDouble("fine.cd.rate", 0.0);
        int expectedPeriod = Config.getInt("fine.cd.period", 0);

        FineStrategy s = FineStrategyFactory.cd();

        assertEquals(BigDecimal.valueOf(expectedRate), s.calculateFine(1));
        assertEquals(expectedPeriod, s.getBorrowPeriodDays());
    }

    @Test
    void testJournalConfigOverrides() {
        Config.reload();

        Config.set("fine.journal.rate", "7");
        Config.set("fine.journal.period", "5");

        double expectedRate = Config.getDouble("fine.journal.rate", 0.0);
        int expectedPeriod = Config.getInt("fine.journal.period", 0);

        FineStrategy s = FineStrategyFactory.journal();

        assertEquals(BigDecimal.valueOf(expectedRate), s.calculateFine(1));
        assertEquals(expectedPeriod, s.getBorrowPeriodDays());
    }

    // =====================================================
    // INVALID / MISSING CONFIG
    // =====================================================

    @Test
    void testInvalidRateFallback() {
        Config.reload();

        Config.set("fine.book.rate", "abc");

        FineStrategy s = FineStrategyFactory.book();
        assertNotNull(s);

        BigDecimal fine = s.calculateFine(1);
        assertNotNull(fine);
        assertTrue(fine.compareTo(BigDecimal.ZERO) >= 0);

        assertTrue(s.getBorrowPeriodDays() > 0);
    }

    @Test
    void testMissingValuesFallback() {
        Config.reload();

        assertDoesNotThrow(FineStrategyFactory::cd);

        FineStrategy s = FineStrategyFactory.cd();
        assertTrue(s.getBorrowPeriodDays() > 0);
        assertTrue(s.calculateFine(1).compareTo(BigDecimal.ZERO) >= 0);
    }

    // =====================================================
    // BaseFineStrategy behavior
    // =====================================================

    @Test
    void testCalculateFineZeroDays() {
        Config.reload();
        FineStrategy s = FineStrategyFactory.book();
        assertEquals(BigDecimal.ZERO, s.calculateFine(0));
    }

    @Test
    void testCalculateFineNegativeDays() {
        Config.reload();
        FineStrategy s = FineStrategyFactory.book();
        assertEquals(BigDecimal.ZERO, s.calculateFine(-5));
    }

    @Test
    void testCalculateFinePositiveDays() {
        Config.reload();

        FineStrategy s = FineStrategyFactory.book();
        BigDecimal fine1 = s.calculateFine(1);
        BigDecimal fine3 = s.calculateFine(3);

        assertEquals(fine1.multiply(BigDecimal.valueOf(3)), fine3);
    }

    // =====================================================
    // TYPE CHECK
    // =====================================================

    @Test
    void testSimpleFineStrategyInternal() {
        Config.reload();
        FineStrategy s = FineStrategyFactory.book();
        assertNotNull(s);
        assertTrue(s instanceof BaseFineStrategy);
    }
}
