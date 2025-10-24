package librarySE;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;


class BorrowRecordTest {

    private User user;
    private Book book;

	@BeforeEach
	void setUp() throws Exception {
		user = new User("Alice", Role.USER, "pass123");
        book = new Book("111", "Java Programming", "Author A");
	}

	@AfterEach
	void tearDown() throws Exception {
		user = null;
		book = null;
	}

	 @Test
	    void testCreation() {
	        BorrowRecord record = new BorrowRecord(user, book);
	        assertEquals(user, record.getUser());
	        assertEquals(book, record.getBook());
	        assertFalse(record.isReturned());
	        assertEquals(0.0, record.getFine());
	        assertEquals(LocalDate.now(), record.getBorrowDate());
	        assertEquals(LocalDate.now().plusDays(28), record.getDueDate());
	    }

	    @Test
	    void testCalculateFineNotOverdue() {
	        BorrowRecord record = new BorrowRecord(user, book);
	        record.calculateFine(2.0, LocalDate.now());
	        assertEquals(0.0, record.getFine());
	    }

	    @Test
	    void testCalculateFineOverdue() {
	        BorrowRecord record = new BorrowRecord(user, book);
	        LocalDate overdueDate = LocalDate.now().plusDays(30); // 2 days overdue
	        record.calculateFine(2.0, overdueDate);
	        assertEquals(4.0, record.getFine());
	    }

	    @Test
	    void testMarkReturned() {
	        BorrowRecord record = new BorrowRecord(user, book);
	        record.markReturned();
	        assertTrue(record.isReturned());
	    }

	    @Test
	    void testIsOverdue() {
	        BorrowRecord record = new BorrowRecord(user, book);
	        LocalDate overdueDate = LocalDate.now().plusDays(30);
	        assertTrue(record.isOverdue(overdueDate));
	        record.markReturned();
	        assertFalse(record.isOverdue(overdueDate));
	    }

	    @Test
	    void testToString() {
	        BorrowRecord record = new BorrowRecord(user, book);
	        String str = record.toString();
	        assertTrue(str.contains(user.getUsername()));
	        assertTrue(str.contains(book.getTitle()));
	    }

}
