package librarySE;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

class BorrowManagerTest {

    private BorrowManager manager;
    private User user;
    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        user = new User("Alice", Role.USER, "pass123");
        book1 = new Book("111", "Java Programming", "Author A");
        book2 = new Book("222", "Python Basics", "Author B");
        manager = new BorrowManager(2.0);
    }

    @AfterEach
    void tearDown() {
        user = null;
        book1 = null;
        book2 = null;
        manager = null;
    }

    @Test
    void testBorrowBookSuccess() {
        manager.borrowBook(user, book1);
        List<BorrowRecord> records = manager.getAllRecords();

        assertEquals(1, records.size());
        assertFalse(book1.isAvailable());
        assertEquals(user, records.get(0).getUser());
    }

    @Test
    void testBorrowBookUserHasFine() {
        user.addFine(5.0);
        Exception e = assertThrows(IllegalArgumentException.class, () -> manager.borrowBook(user, book1));
        assertEquals("User has unpaid fines and cannot borrow new books.", e.getMessage());
    }

    @Test
    void testBorrowBookNotAvailable() {
        book1.borrow();
        Exception e = assertThrows(IllegalArgumentException.class, () -> manager.borrowBook(user, book1));
        assertEquals("Book is currently not available.", e.getMessage());
    }

    @Test
    void testReturnBookNoFine() {
        manager.borrowBook(user, book1);
        BorrowRecord record = manager.getAllRecords().get(0);

        manager.returnBook(record);

        assertTrue(record.isReturned());
        assertTrue(book1.isAvailable());
       // assertEquals(0.0, user.getFineBalance());
    }

    @Test
    void testReturnBookWithFine() {
        manager.borrowBook(user, book1);
        BorrowRecord record = manager.getAllRecords().get(0);

        record.calculateFine(manager.getFinePerDay(), record.getDueDate().plusDays(3));
        manager.returnBook(record);

        assertTrue(record.isReturned());
        assertTrue(book1.isAvailable());
        assertTrue(user.getFineBalance() >= 0);
    }

    @Test
    void testPayFineSuccessfullyWhenNoUnreturnedBooks() {
        user.addFine(20.0); 
        manager.payFine(user, 5.0); 
        assertEquals(15.0, user.getFineBalance());
    }

    @Test
    void testPayFineWithUnreturnedBook() {
        manager.borrowBook(user, book1);

        user.addFine(10.0);

        Exception e = assertThrows(IllegalStateException.class, () -> manager.payFine(user, 5.0));
        assertEquals("User must return all borrowed books before paying fines.", e.getMessage());
    }

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

    @Test
    void testSetAndGetFinePerDay() {
        assertEquals(2.0, manager.getFinePerDay());
        manager.setFinePerDay(5.0);
        assertEquals(5.0, manager.getFinePerDay());
    }
    
    @Test
    void testPayFineThrowsExceptionWhenUserHasUnreturnedBook() {
        manager.borrowBook(user, book1);    
        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> manager.payFine(user, 5.0)
        );
        assertEquals("User must return all borrowed books before paying fines.", e.getMessage());
    }

}

