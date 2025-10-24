package librarySE;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

class BorrowRecordTest {

    private User user;
    private Book book;

    /**
     * Runs before each test.
     * Initializes a user and a book for testing BorrowRecord.
     */
    @BeforeEach
    void setUp() throws Exception {
        user = new User("Alice", Role.USER, "pass123");
        book = new Book("111", "Java Programming", "Author A");
    }

    /**
     * Runs after each test.
     * Resets user and book objects.
     */
    @AfterEach
    void tearDown() throws Exception {
        user = null;
        book = null;
    }

    /**
     * Test creation of BorrowRecord.
     * Verifies that all fields are initialized correctly.
     */
    @Test
    void testCreation() {
        BorrowRecord record = new BorrowRecord(user, book);
        assertEquals(user, record.getUser());
        assertEquals(book, record.getBook());
        assertFalse(record.isReturned(), "New record should not be marked as returned");
        assertEquals(BigDecimal.ZERO, record.getFine(), "Initial fine should be zero");
        assertEquals(LocalDate.now(), record.getBorrowDate(), "Borrow date should be today");
        assertEquals(LocalDate.now().plusDays(28), record.getDueDate(), "Due date should be 28 days from borrow date");
    }

    /**
     * Test calculating fine when book is not overdue.
     * Fine should remain zero.
     */
    @Test
    void testCalculateFineNotOverdue() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.calculateFine(BigDecimal.valueOf(2), LocalDate.now());
        assertEquals(0, record.getFine().compareTo(BigDecimal.ZERO), "Fine should be zero for not overdue book");
    }

    /**
     * Test calculating fine when book is overdue.
     * Fine should be correctly computed as (days overdue * fine per day).
     */
    @Test
    void testCalculateFineOverdue() {
        BorrowRecord record = new BorrowRecord(user, book);
        LocalDate overdueDate = LocalDate.now().plusDays(30); // 2 days overdue
        record.calculateFine(BigDecimal.valueOf(2), overdueDate);
        assertEquals(0, record.getFine().compareTo(BigDecimal.valueOf(4)), "Fine should be 4 for 2 days overdue at 2 per day");
    }

    /**
     * Test marking the book as returned.
     */
    @Test
    void testMarkReturned() {
        BorrowRecord record = new BorrowRecord(user, book);
        record.markReturned();
        assertTrue(record.isReturned(), "Record should be marked as returned");
    }

    /**
     * Test overdue status check.
     * Should return true if overdue and not returned, false if returned.
     */
    @Test
    void testIsOverdue() {
        BorrowRecord record = new BorrowRecord(user, book);
        LocalDate overdueDate = LocalDate.now().plusDays(30);
        assertTrue(record.isOverdue(overdueDate), "Record should be overdue");
        record.markReturned();
        assertFalse(record.isOverdue(overdueDate), "Record should not be overdue after returning");
    }

    /**
     * Test toString method.
     * Verifies that the string contains username and book title.
     */
    @Test
    void testToString() {
        BorrowRecord record = new BorrowRecord(user, book);
        String str = record.toString();
        assertTrue(str.contains(user.getUsername()), "toString should contain username");
        assertTrue(str.contains(book.getTitle()), "toString should contain book title");
    }
}


