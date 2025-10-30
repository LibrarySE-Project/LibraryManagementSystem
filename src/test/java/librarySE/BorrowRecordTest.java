package librarySE;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BorrowRecordTest {

    private User user;
    private Book book;

    /** 
     * Runs before each test.
     * Creates a new user and book to ensure a clean state for every test case.
     */
    @BeforeEach
    void setUp() {
        user = new User("Alice", Role.USER, "pass123");
        book = new Book("111", "Java Programming", "Author A");
    }

    // ---------------- Constructor Tests ----------------

    /** 
     * Verifies that the constructor initializes all fields correctly.
     */
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

    /** 
     * Ensures that the constructor throws an exception if user or book is null.
     */
    @Test
    void testConstructorWithNulls() {
        assertThrows(IllegalArgumentException.class, () -> new BorrowRecord(null, book));
        assertThrows(IllegalArgumentException.class, () -> new BorrowRecord(user, null));
    }

    /** 
     * Checks that the default fine per day value is initialized correctly.
     */
    @Test
    void testConstructor_DefaultFinePerDay() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertEquals(BigDecimal.valueOf(5), record.getFinePerDay());
    }

    // ---------------- calculateFine Tests ----------------

    /** 
     * Fine should be zero when the book is not overdue.
     */
    @Test
    void testCalculateFine_NotOverdue() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.calculateFine(LocalDate.now());
        assertEquals(BigDecimal.ZERO, record.getFine(LocalDate.now()));
    }

    /** 
     * Fine should be correct for one day overdue.
     */
    @Test
    void testCalculateFine_1DayOverdue() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.calculateFine(record.getDueDate().plusDays(1));
        assertEquals(BigDecimal.valueOf(5), record.getFine(record.getDueDate().plusDays(1)));
    }

    /** 
     * Fine should accumulate for multiple overdue days.
     */
    @Test
    void testCalculateFine_MultipleDaysOverdue() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.calculateFine(record.getDueDate().plusDays(3));
        assertEquals(BigDecimal.valueOf(15), record.getFine(record.getDueDate().plusDays(3)));
    }

    /** 
     * Fine remains zero if the book has already been returned.
     */
    @Test
    void testCalculateFine_BookReturned() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.markReturned();
        record.calculateFine(record.getDueDate().plusDays(4));
        assertEquals(BigDecimal.ZERO, record.getFine(record.getDueDate().plusDays(4)));
    }

    /** 
     * Ensures calculateFine throws an exception if date is null.
     */
    @Test
    void testCalculateFine_NullDate() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertThrows(IllegalArgumentException.class, () -> record.calculateFine(null));
    }

    /** 
     * Fine should be zero if the given date is before the borrow date (edge case).
     */
    @Test
    void testCalculateFine_BeforeBorrowDate() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.calculateFine(record.getBorrowDate().minusDays(1));
        assertEquals(BigDecimal.ZERO, record.getFine(record.getBorrowDate().minusDays(1)));
    }

    // ---------------- markReturned Tests ----------------

    /** 
     * Checks that markReturned updates the status and resets fine to zero.
     */
    @Test
    void testMarkReturned() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertFalse(record.isReturned());
        record.markReturned();
        assertTrue(record.isReturned());
        assertEquals(BigDecimal.ZERO, record.getFine(record.getDueDate().plusDays(5)));
    }

    /** 
     * If markReturned is called twice, it should not affect the record’s state.
     */
    @Test
    void testMarkReturned_CalledTwice() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.markReturned();
        record.markReturned(); // Calling again shouldn't change anything
        assertTrue(record.isReturned());
        assertEquals(BigDecimal.ZERO, record.getFine(record.getDueDate().plusDays(5)));
    }

    // ---------------- isOverdue Tests ----------------

    /** 
     * isOverdue should return true when current date is past due date.
     */
    @Test
    void testIsOverdue_TrueCase() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertTrue(record.isOverdue(record.getDueDate().plusDays(1)));
    }

    /** 
     * isOverdue should return false before the due date.
     */
    @Test
    void testIsOverdue_FalseBeforeDue() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertFalse(record.isOverdue(record.getDueDate().minusDays(1)));
    }

    /** 
     * isOverdue should return false exactly on the due date.
     */
    @Test
    void testIsOverdue_OnDueDate() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertFalse(record.isOverdue(record.getDueDate()));
    }

    /** 
     * isOverdue should return false if the book was already returned.
     */
    @Test
    void testIsOverdue_BookReturned() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.markReturned();
        assertFalse(record.isOverdue(record.getDueDate().plusDays(1)));
    }

    /** 
     * Ensures isOverdue throws an exception when a null date is passed.
     */
    @Test
    void testIsOverdue_NullDate() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertThrows(IllegalArgumentException.class, () -> record.isOverdue(null));
    }

    // ---------------- getFine Tests ----------------

    /** 
     * getFine should not modify internal state when called multiple times.
     */
    @Test
    void testGetFine_DoesNotModifyState() {
        BorrowRecord record = new BorrowRecord(user, book);
        BigDecimal fineBefore = record.getFine(record.getDueDate().plusDays(2));
        BigDecimal fineAfter = record.getFine(record.getDueDate().plusDays(2));
        assertEquals(fineBefore, fineAfter);
    }

    // ---------------- toString Tests ----------------

    /** 
     * Ensures toString contains all record details before the book is returned.
     */
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

    /** 
     * Ensures toString correctly reflects returned status and fine reset.
     */
    @Test
    void testToString_ReturnedWithFineReset() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.calculateFine(record.getDueDate().plusDays(2));
        record.markReturned();
        String s = record.toString();
        assertTrue(s.contains("Returned: true"));
        assertTrue(s.contains("Fine: 0"));
    }

    /** 
     * Ensures toString works correctly when the book is immediately returned.
     */
    @Test
    void testToString_ImmediatelyReturned() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.markReturned();
        String s = record.toString();
        assertTrue(s.contains("Returned: true"));
        assertTrue(s.contains("Fine: 0"));
    }
}

