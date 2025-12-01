package librarySE.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BaseFineStrategyTest {

    private BaseFineStrategy strategy;

    // Dummy valid strategy
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

    // ============================
    // calculateFine() tests
    // ============================

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

    // ============================
    // getBorrowPeriodDays()
    // ============================

    @Test
    void testBorrowPeriod() {
        assertEquals(5, strategy.getBorrowPeriodDays());
    }

    // ============================
    // Constructor validation tests
    // ============================

    @Test
    void testConstructorRejectsNegativeRate() {
        assertThrows(IllegalArgumentException.class,
                () -> new BaseFineStrategy(BigDecimal.valueOf(-1), 5) {});
    }

    @Test
    void testConstructorRejectsInvalidPeriod_Zero() {
        assertThrows(IllegalArgumentException.class,
                () -> new BaseFineStrategy(BigDecimal.valueOf(10), 0) {});
    }

    @Test
    void testConstructorRejectsInvalidPeriod_Negative() {
        assertThrows(IllegalArgumentException.class,
                () -> new BaseFineStrategy(BigDecimal.valueOf(10), -3) {});
    }

    @Test
    void testConstructorRejectsNullRate() {
        assertThrows(IllegalArgumentException.class,
                () -> new BaseFineStrategy(null, 5) {});
    }
}
