package librarySE.strategy;

import librarySE.utils.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class FineStrategyFactoryTest {

	@BeforeEach
	void resetConfig() throws Exception {

	    Field propsField = Config.class.getDeclaredField("PROPS");
	    propsField.setAccessible(true);

	    Properties props = (Properties) propsField.get(null);
	    props.clear();  

	  
	    String defaults = """
	        fine.book.rate=10
	        fine.book.period=28
	        fine.cd.rate=20
	        fine.cd.period=7
	        fine.journal.rate=15
	        fine.journal.period=21
	        price.book.default=59.99
	        price.cd.default=39.99
	        price.journal.default=29.99
	        notifications.enabled=true
	        """;

	    Files.writeString(Paths.get("data/config/fine-config.properties"), defaults);
	    Config.reload();
	}


    // =====================================================
    // DEFAULT VALUES TESTS
    // =====================================================

    @Test
    void testBookDefaults() {
        FineStrategy s = FineStrategyFactory.book();
        assertEquals(BigDecimal.TEN, s.calculateFine(1));  // 10 * 1
        assertEquals(28, s.getBorrowPeriodDays());
    }

    @Test
    void testCDDefaults() {
        FineStrategy s = FineStrategyFactory.cd();
        assertEquals(BigDecimal.valueOf(20), s.calculateFine(1));
        assertEquals(7, s.getBorrowPeriodDays());
    }

    @Test
    void testJournalDefaults() {
        FineStrategy s = FineStrategyFactory.journal();
        assertEquals(BigDecimal.valueOf(15), s.calculateFine(1));
        assertEquals(21, s.getBorrowPeriodDays());
    }

    // =====================================================
    // READ OVERRIDDEN CONFIG VALUES
    // =====================================================

    @Test
    void testBookConfigOverrides() {
        Config.set("fine.book.rate", "5");
        Config.set("fine.book.period", "10");

        FineStrategy s = FineStrategyFactory.book();

        assertEquals(BigDecimal.valueOf(5), s.calculateFine(1));
        assertEquals(10, s.getBorrowPeriodDays());
    }

    @Test
    void testCDConfigOverrides() {
        Config.set("fine.cd.rate", "2.5");
        Config.set("fine.cd.period", "3");

        FineStrategy s = FineStrategyFactory.cd();

        assertEquals(BigDecimal.valueOf(2.5), s.calculateFine(1));
        assertEquals(3, s.getBorrowPeriodDays());
    }

    @Test
    void testJournalConfigOverrides() {
        Config.set("fine.journal.rate", "7");
        Config.set("fine.journal.period", "5");

        FineStrategy s = FineStrategyFactory.journal();

        assertEquals(BigDecimal.valueOf(7), s.calculateFine(1));
        assertEquals(5, s.getBorrowPeriodDays());
    }

    // =====================================================
    // INVALID CONFIG VALUES → FALLBACK TO DEFAULT
    // =====================================================

    @Test
    void testInvalidRateFallback() {
        Config.set("fine.book.rate", "abc"); // invalid

        FineStrategy s = FineStrategyFactory.book();

        assertEquals(BigDecimal.TEN, s.calculateFine(1));  // fallback 10
        assertEquals(28, s.getBorrowPeriodDays()); // default
    }

    @Test
    void testMissingValuesFallback() {
        // لا نضع أي قيم
        FineStrategy s = FineStrategyFactory.cd();

        assertEquals(BigDecimal.valueOf(20), s.calculateFine(1)); // default
        assertEquals(7, s.getBorrowPeriodDays());
    }

    // =====================================================
    // TEST BaseFineStrategy Logic (Indirect testing)
    // =====================================================

    @Test
    void testCalculateFineZeroDays() {
        FineStrategy s = FineStrategyFactory.book();
        assertEquals(BigDecimal.ZERO, s.calculateFine(0));
    }

    @Test
    void testCalculateFineNegativeDays() {
        FineStrategy s = FineStrategyFactory.book();
        assertEquals(BigDecimal.ZERO, s.calculateFine(-5));
    }

    @Test
    void testCalculateFinePositiveDays() {
        FineStrategy s = FineStrategyFactory.book();
        assertEquals(BigDecimal.valueOf(30), s.calculateFine(3)); // 10 * 3
    }

    // =====================================================
    // VALIDATE SimpleFineStrategy CREATION
    // =====================================================

    @Test
    void testSimpleFineStrategyInternal() {
        FineStrategy s = FineStrategyFactory.book();
        assertNotNull(s);
        assertTrue(s instanceof BaseFineStrategy);
    }
}
