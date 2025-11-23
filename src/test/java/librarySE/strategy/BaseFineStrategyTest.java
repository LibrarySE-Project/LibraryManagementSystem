package librarySE.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BaseFineStrategyTest {

    private BaseFineStrategy strategy;

    // Dummy strategy to test abstract class
    static class DummyStrategy extends BaseFineStrategy {
        DummyStrategy() {
            super(BigDecimal.valueOf(10), 5);
        }
    }

    @BeforeEach
    void setUp() {
        strategy = new DummyStrategy();
    }

    @AfterEach
    void tearDown() {
        strategy = null;
    }

    @Test
    void testCalculateFineZeroDays() {
        assertEquals(BigDecimal.ZERO, strategy.calculateFine(0));
    }

    @Test
    void testCalculateFinePositiveDays() {
        assertEquals(BigDecimal.valueOf(50), strategy.calculateFine(5));
    }

    @Test
    void testCalculateFineNegativeReturnsZero() {
        assertEquals(BigDecimal.ZERO, strategy.calculateFine(-10));
    }

    @Test
    void testBorrowPeriod() {
        assertEquals(5, strategy.getBorrowPeriodDays());
    }

    @Test
    void testConstructorRejectsNegativeRate() {
        assertThrows(IllegalArgumentException.class,
                () -> new DummyInvalidRate());
    }

    static class DummyInvalidRate extends BaseFineStrategy {
        DummyInvalidRate() {
            super(BigDecimal.valueOf(-1), 5);
        }
    }

    @Test
    void testConstructorRejectsInvalidPeriod() {
        assertThrows(IllegalArgumentException.class,
                () -> new DummyInvalidPeriod());
    }

    static class DummyInvalidPeriod extends BaseFineStrategy {
        DummyInvalidPeriod() {
            super(BigDecimal.valueOf(10), 0);
        }
    }
}

