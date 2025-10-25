package librarySE;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BorrowRecordTest {

    private User user;
    private Book book;

    /** Initialize a new user and book before each test. */
    @BeforeEach
    void setUp() {
        user = new User("Alice", Role.USER, "pass123");
        book = new Book("111", "Java Programming", "Author A");
    }

    /** Tests that the constructor initializes fields correctly. */
    @Test
    void testConstructorValid() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertEquals(user, record.getUser());
        assertEquals(book, record.getBook());
        assertEquals(LocalDate.now(), record.getBorrowDate());
        assertEquals(LocalDate.now().plusDays(28), record.getDueDate());
        assertFalse(record.isReturned());
        assertEquals(BigDecimal.ZERO, record.getFine(LocalDate.now()));
    }

    /** Tests constructor throws exception when user or book is null. */
    @Test
    void testConstructorWithNulls() {
        assertThrows(IllegalArgumentException.class, () -> new BorrowRecord(null, book));
        assertThrows(IllegalArgumentException.class, () -> new BorrowRecord(user, null));
    }

    /** Checks that fine is zero when book is not overdue. */
    @Test
    void testCalculateFine_NotOverdue() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.calculateFine(LocalDate.now());
        assertEquals(BigDecimal.ZERO, record.getFine(LocalDate.now()));
    }

    /** Checks fine calculation for 1 day overdue. */
    @Test
    void testCalculateFine_1DayOverdue() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.calculateFine(record.getDueDate().plusDays(1));
        assertEquals(BigDecimal.valueOf(5), record.getFine(record.getDueDate().plusDays(1)));
    }

    /** Checks fine calculation for multiple overdue days. */
    @Test
    void testCalculateFine_MultipleDaysOverdue() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.calculateFine(record.getDueDate().plusDays(3));
        assertEquals(BigDecimal.valueOf(15), record.getFine(record.getDueDate().plusDays(3)));
    }

    /** Verifies fine remains zero after book is returned. */
    @Test
    void testCalculateFine_BookReturned() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.markReturned();
        record.calculateFine(record.getDueDate().plusDays(4));
        assertEquals(BigDecimal.ZERO, record.getFine(record.getDueDate().plusDays(4)));
    }

    /** Tests that calculateFine throws exception if date is null. */
    @Test
    void testCalculateFine_NullDate() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertThrows(IllegalArgumentException.class, () -> record.calculateFine(null));
    }

    /** Tests that markReturned updates returned status and resets fine. */
    @Test
    void testMarkReturned() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertFalse(record.isReturned());
        record.markReturned();
        assertTrue(record.isReturned());
        assertEquals(BigDecimal.ZERO, record.getFine(record.getDueDate().plusDays(5)));
    }

    /** Checks isOverdue returns true when book is past due date. */
    @Test
    void testIsOverdue_TrueCase() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertTrue(record.isOverdue(record.getDueDate().plusDays(1)));
    }

    /** Checks isOverdue returns false before due date. */
    @Test
    void testIsOverdue_FalseBeforeDue() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertFalse(record.isOverdue(record.getDueDate().minusDays(1)));
    }

    /** Checks isOverdue returns false on the due date. */
    @Test
    void testIsOverdue_OnDueDate() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertFalse(record.isOverdue(record.getDueDate()));
    }

    /** Checks isOverdue returns false if book has been returned. */
    @Test
    void testIsOverdue_BookReturned() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.markReturned();
        assertFalse(record.isOverdue(record.getDueDate().plusDays(1)));
    }

    /** Tests that isOverdue throws exception when date is null. */
    @Test
    void testIsOverdue_NullDate() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertThrows(IllegalArgumentException.class, () -> record.isOverdue(null));
    }

    /** Checks that toString contains all details before book is returned. */
    @Test
    void testToString_ContainsDetailsBeforeReturn() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.calculateFine(record.getDueDate().plusDays(2));
        String s = record.toString();
        assertTrue(s.contains(user.getUsername()));
        assertTrue(s.contains(book.getTitle()));
        assertTrue(s.contains(record.getBorrowDate().toString()));
        assertTrue(s.contains(record.getDueDate().toString()));
        assertTrue(s.contains("Returned: false"));
        assertTrue(s.contains("Fine: 10"));
    }

    /** Checks that toString reflects returned status and fine reset. */
    @Test
    void testToString_ReturnedWithFineReset() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.calculateFine(record.getDueDate().plusDays(2));
        record.markReturned();
        String s = record.toString();
        assertTrue(s.contains("Returned: true"));
        assertTrue(s.contains("Fine: 0"));
    }
}
