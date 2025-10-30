package librarySE;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Unit tests for the {@link BorrowRecord} class.
 * <p>
 * Covers constructor validation, fine calculation, overdue detection,
 * applying fines, returning items, and edge cases (including invalid inputs).
 * Includes both positive and negative test cases.
 * </p>
 */
class BorrowRecordTest {

    private User user;
    private LibraryItem mockItem;
    private FineStrategy fineStrategy;
    private BorrowRecord record;

    @BeforeEach
    void setUp() {
        // Mock fine strategy that gives 14 days period and 1 per day fine
        fineStrategy = new FineStrategy() {
            public BigDecimal calculateFine(long overdueDays) {
                return BigDecimal.valueOf(overdueDays);
            }
            public int getBorrowPeriodDays() {
                return 14;
            }
        };

        // Mock item that tracks availability
        mockItem = new LibraryItem() {
            private boolean available = true;
            public String getTitle() { return "Mock Book"; }
            public boolean isAvailable() { return available; }
            public boolean returnItem() {
                if (available) return false;
                available = true;
                return true;
            }
            public boolean borrow() {
                if (!available) return false;
                available = false;
                return true;
            }
            public MaterialType getMaterialType() { return MaterialType.BOOK; }
        };

        user = new User("Alice", Role.USER, "password123", "alice@example.com");
        record = new BorrowRecord(user, mockItem, fineStrategy);
    }

    @AfterEach
    void tearDown() {
        user = null;
        mockItem = null;
        fineStrategy = null;
        record = null;
    }

    // Constructor and Initialization

    /** Constructor sets all fields correctly and marks item as borrowed */
    @Test
    void testConstructorSetsFieldsCorrectly() {
        assertEquals(user, record.getUser());
        assertEquals(mockItem, record.getItem());
        assertFalse(mockItem.isAvailable());
        assertNotNull(record.getBorrowDate());
        assertEquals(record.getBorrowDate().plusDays(14), record.getDueDate());
    }

    /** Constructor throws if any argument is null */
    @Test
    void testConstructorWithNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> new BorrowRecord(null, mockItem, fineStrategy));
        assertThrows(IllegalArgumentException.class, () -> new BorrowRecord(user, null, fineStrategy));
        assertThrows(IllegalArgumentException.class, () -> new BorrowRecord(user, mockItem, null));
    }

    // Fine Calculation

    /** getFine returns 0 when not overdue */
    @Test
    void testGetFineWhenNotOverdue() {
        LocalDate beforeDue = record.getDueDate();
        assertEquals(BigDecimal.ZERO, record.getFine(beforeDue));
    }

    /** getFine calculates positive fine when overdue */
    @Test
    void testGetFineWhenOverdue() {
        LocalDate afterDue = record.getDueDate().plusDays(5);
        BigDecimal fine = record.getFine(afterDue);
        assertEquals(BigDecimal.valueOf(5), fine);
    }

    /** calculateFine sets fine to 0 if item returned */
    @Test
    void testCalculateFineWhenItemReturned() {
        mockItem.returnItem();
        record.calculateFine(record.getDueDate().plusDays(10));
        assertEquals(BigDecimal.ZERO, record.getFine(record.getDueDate().plusDays(10)));
    }

    /** calculateFine throws for null date */
    @Test
    void testCalculateFineWithNullDate() {
        assertThrows(IllegalArgumentException.class, () -> record.calculateFine(null));
    }

    // Fine Application

    /** applyFineToUser adds fine once when overdue */
    @Test
    void testApplyFineToUserOnce() {
        LocalDate afterDue = record.getDueDate().plusDays(3);
        record.applyFineToUser(afterDue);
        BigDecimal fineBefore = user.getFineBalance();

        // Second call should not double-charge
        record.applyFineToUser(afterDue);
        assertEquals(fineBefore, user.getFineBalance());
        assertEquals(BigDecimal.valueOf(3), fineBefore);
    }

    /** applyFineToUser should not add fine if not overdue */
    @Test
    void testApplyFineToUserWhenNotOverdue() {
        record.applyFineToUser(record.getBorrowDate());
        assertEquals(BigDecimal.ZERO, user.getFineBalance());
    }

    // Return Item

    /** markReturned should apply fine and mark item available */
    @Test
    void testMarkReturned() {
        LocalDate afterDue = record.getDueDate().plusDays(2);
        record.applyFineToUser(afterDue);
        record.markReturned();
        assertTrue(mockItem.isAvailable());
    }

    // Overdue Check

    /** isOverdue returns true only if after due and not returned */
    @Test
    void testIsOverdueTrue() {
        LocalDate afterDue = record.getDueDate().plusDays(1);
        assertTrue(record.isOverdue(afterDue));
    }

    /** isOverdue returns false if item is returned */
    @Test
    void testIsOverdueFalseWhenReturned() {
        mockItem.returnItem();
        assertFalse(record.isOverdue(record.getDueDate().plusDays(5)));
    }

    /** isOverdue throws if currentDate is null */
    @Test
    void testIsOverdueWithNullDate() {
        assertThrows(IllegalArgumentException.class, () -> record.isOverdue(null));
    }

    // toString

    /** toString contains username, title, due date, and fine */
    @Test
    void testToStringContainsImportantData() {
        String s = record.toString();
        assertTrue(s.contains(user.getUsername()));
        assertTrue(s.contains(mockItem.getTitle()));
        assertTrue(s.contains("due"));
        assertTrue(s.contains("Fine"));
    }
}


