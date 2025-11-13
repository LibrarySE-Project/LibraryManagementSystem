package librarySE;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.*;

import librarySE.core.strategy.BookFineStrategy;

/**
 * Unit tests for {@link BookFineStrategy}.
 * Verifies fine calculation and borrow period behavior.
 */
class BookFineStrategyTest {

    private BookFineStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BookFineStrategy();
    }

    /**
     * Test that the borrow period is always 28 days.
     */
    @Test
    void testBorrowPeriodDays() {
        assertEquals(28, strategy.getBorrowPeriodDays(),
                "Borrow period for books should be 28 days");
    }

    /**
     * Test fine calculation for zero overdue days.
     */
    @Test
    void testCalculateFineZeroDays() {
        BigDecimal fine = strategy.calculateFine(0);
        assertEquals(BigDecimal.ZERO, fine,
                "Fine for 0 overdue days should be 0");
    }

    /**
     * Test fine calculation for negative overdue days (edge case).
     */
    @Test
    void testCalculateFineNegativeDays() {
        BigDecimal fine = strategy.calculateFine(-5);
        assertEquals(BigDecimal.ZERO, fine,
                "Fine for negative overdue days should be 0");
    }

    /**
     * Test fine calculation for positive overdue days.
     */
    @Test
    void testCalculateFinePositiveDays() {
        long overdueDays = 5;
        BigDecimal expected = BigDecimal.valueOf(50); // 10 * 5
        BigDecimal fine = strategy.calculateFine(overdueDays);
        assertEquals(0, expected.compareTo(fine),
                "Fine should be 10 NIS per overdue day");
    }

    /**
     * Test fine calculation for large number of overdue days.
     */
    @Test
    void testCalculateFineLargeDays() {
        long overdueDays = 100;
        BigDecimal expected = BigDecimal.valueOf(1000); // 10 * 100
        BigDecimal fine = strategy.calculateFine(overdueDays);
        assertEquals(0, expected.compareTo(fine),
                "Fine calculation should scale linearly with overdue days");
    }
}

