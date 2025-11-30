package librarySE.managers;

import librarySE.strategy.FineStrategy;
import librarySE.strategy.FineStrategyFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FineContextTest {

    FineStrategy strategy;
    FineContext context;

    @BeforeEach
    void setup() {
        strategy = FineStrategyFactory.book();
        context = new FineContext(strategy);
    }

    // Constructor Tests
    @Test
    void constructor_validStrategy_success() {
        assertEquals(strategy.getBorrowPeriodDays(), context.getBorrowPeriodDays());
    }

    @Test
    void constructor_nullStrategy_throws() {
        assertThrows(IllegalArgumentException.class, () -> new FineContext(null));
    }

    // setStrategy Tests
    @Test
    void setStrategy_valid_success() {
        FineStrategy newStrategy = FineStrategyFactory.cd();
        context.setStrategy(newStrategy);
        assertEquals(newStrategy.getBorrowPeriodDays(), context.getBorrowPeriodDays());
    }

    @Test
    void setStrategy_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> context.setStrategy(null));
    }

    // calculateFine Tests
    @Test
    void calculateFine_zeroDays_zeroFine() {
        BigDecimal fine = context.calculateFine(0);
        assertEquals(BigDecimal.ZERO, fine);
    }

    @Test
    void calculateFine_positiveDays_positiveFine() {
        BigDecimal fine = context.calculateFine(5);
        assertTrue(fine.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateFine_negativeDays_allowedIfStrategyAllows() {
        // Strategy may or may not allow negative days; book returns ZERO.
        BigDecimal fine = context.calculateFine(-10);
        assertNotNull(fine); // just ensure no crash
    }
}

