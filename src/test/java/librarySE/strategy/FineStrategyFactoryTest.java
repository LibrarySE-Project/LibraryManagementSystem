package librarySE.strategy;

import librarySE.utils.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FineStrategyFactoryTest {
	
    @Test
    void testBookStrategyDefaults() {
        FineStrategy strategy = FineStrategyFactory.book();
        assertEquals(BigDecimal.TEN, strategy.calculateFine(1));
        assertEquals(28, strategy.getBorrowPeriodDays());
    }

    @Test
    void testCDStrategyDefaults() {
        FineStrategy strategy = FineStrategyFactory.cd();
        assertEquals(BigDecimal.valueOf(20), strategy.calculateFine(1));
        assertEquals(7, strategy.getBorrowPeriodDays());
    }

    @Test
    void testJournalStrategyDefaults() {
        FineStrategy strategy = FineStrategyFactory.journal();
        assertEquals(BigDecimal.valueOf(15), strategy.calculateFine(1));
        assertEquals(21, strategy.getBorrowPeriodDays());
    }

    @Test
    void testBookReadsConfig() {
        Config.set("fine.book.rate", "5");
        Config.set("fine.book.period", "10");

        FineStrategy strategy = FineStrategyFactory.book();

        assertEquals(BigDecimal.valueOf(5), strategy.calculateFine(1));
        assertEquals(10, strategy.getBorrowPeriodDays());
    }
}
