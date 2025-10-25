package librarySE;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BorrowManagerTest {

    private BorrowManager manager;
    private User user;
    private Book book1;
    private Book book2;

    /**
     * Runs before each test.
     * Initializes a user, two books, and a BorrowManager instance.
     */
    @BeforeEach
    void setUp() {
        user = new User("Alice", Role.USER, "pass123");
        book1 = new Book("111", "Java Programming", "Author A");
        book2 = new Book("222", "Python Basics", "Author B");
        manager = new BorrowManager(BigDecimal.valueOf(2.0)); // finePerDay as BigDecimal
    }

    /**
     * Runs after each test.
     * Resets objects to null.
     */
    @AfterEach
    void tearDown() {
        user = null;
        book1 = null;
        book2 = null;
        manager = null;
    }

    /**
     * Test borrowing a book successfully.
     * Verifies that the book is marked as borrowed and the record is created.
     */
    @Test
    void testBorrowBookSuccess() {
        manager.borrowBook(user, book1);
        List<BorrowRecord> records = manager.getAllRecords();

        assertEquals(1, records.size());
        assertFalse(book1.isAvailable());
        assertEquals(user, records.get(0).getUser());
    }

    /**
     * Test borrowing when user has unpaid fines.
     * Should throw an exception preventing borrowing.
     */
    @Test
    void testBorrowBookUserHasFine() {
        user.addFine(BigDecimal.valueOf(5.0));
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> manager.borrowBook(user, book1));
        assertEquals("User has unpaid fines and cannot borrow new books.", e.getMessage());
    }

    /**
     * Test borrowing a book that is not available.
     * Should throw an exception.
     */
    @Test
    void testBorrowBookNotAvailable() {
        book1.borrow();
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> manager.borrowBook(user, book1));
        assertEquals("Book is currently not available.", e.getMessage());
    }

    /**
     * Test returning a book that is not overdue.
     * Fine should remain zero and book becomes available.
     */
    @Test
    void testReturnBookNoFine() {
        manager.borrowBook(user, book1);
        BorrowRecord record = manager.getAllRecords().get(0);

        manager.returnBook(record);

        assertTrue(record.isReturned());
        assertTrue(book1.isAvailable());
        assertEquals(BigDecimal.ZERO, user.getFineBalance());
    }

    /**
     * Test returning a book that is overdue.
     * Fine should be applied to user balance and book becomes available.
     */
    @Test
    void testReturnBookWithFine() {
        manager.borrowBook(user, book1);
        BorrowRecord record = manager.getAllRecords().get(0);

        record.calculateFine(manager.getFinePerDay(), record.getDueDate().plusDays(3));
        manager.returnBook(record);

        assertTrue(record.isReturned());
        assertTrue(book1.isAvailable());
        assertTrue(user.getFineBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * Test paying fine successfully when all books are returned.
     */
    @Test
    void testPayFineSuccessfullyWhenNoUnreturnedBooks() {
        user.addFine(BigDecimal.valueOf(20.0));
        manager.payFine(user, BigDecimal.valueOf(5.0));
        assertEquals(BigDecimal.valueOf(15.0), user.getFineBalance());
    }

    /**
     * Test paying fine when user has an unreturned book.
     * Should throw IllegalStateException.
     */
    @Test
    void testPayFineWithUnreturnedBook() {
        manager.borrowBook(user, book1);
        user.addFine(BigDecimal.valueOf(10.0));

        Exception e = assertThrows(IllegalStateException.class,
                () -> manager.payFine(user, BigDecimal.valueOf(5.0)));
        assertEquals("User must return all borrowed books before paying fines.", e.getMessage());
    }

    /**
     * Test retrieval of overdue records.
     * Simulates one overdue and one not overdue book.
     */
    @Test
    void testGetOverdueRecords() {
        manager.borrowBook(user, book1);
        manager.borrowBook(user, book2);
        List<BorrowRecord> all = manager.getAllRecords();
        BorrowRecord r1 = all.get(0);
        BorrowRecord r2 = all.get(1);

        r1.calculateFine(manager.getFinePerDay(), r1.getDueDate().plusDays(10));
        r2.calculateFine(manager.getFinePerDay(), r2.getDueDate().minusDays(1));

        List<BorrowRecord> overdue = manager.getOverdueRecords();

        assertTrue(overdue.contains(r1));
        assertFalse(overdue.contains(r2));
    }

    /**
     * Test setting and getting fine per day.
     */
    @Test
    void testSetAndGetFinePerDay() {
        assertEquals(BigDecimal.valueOf(2.0), manager.getFinePerDay());
        manager.setFinePerDay(BigDecimal.valueOf(5.0));
        assertEquals(BigDecimal.valueOf(5.0), manager.getFinePerDay());
    }

    /**
     * Test paying fine throws exception when user has unreturned books.
     * Redundant with testPayFineWithUnreturnedBook but kept for clarity.
     */
    @Test
    void testPayFineThrowsExceptionWhenUserHasUnreturnedBook() {
        manager.borrowBook(user, book1);
        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> manager.payFine(user, BigDecimal.valueOf(5.0))
        );
        assertEquals("User must return all borrowed books before paying fines.", e.getMessage());
    }

}



