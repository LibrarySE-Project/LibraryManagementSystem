package librarySE;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LibraryTest {

    private Library library;
    private Admin admin;
    private User regularUser;
    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        library = new Library();
        admin = Admin.getInstance("admin", "pass123");
        regularUser = new User("user1", Role.USER, "userpass");
        book1 = new Book("123", "Java Basics", "John Doe");
        book2 = new Book("456", "Python Intro", "Alice");
    }

    @AfterEach
    void tearDown() {
        library = null;
        admin = null;
        regularUser = null;
        book1 = null;
        book2 = null;
    }

    /** Tests adding a book as an admin user. */
    @Test
    void testAddBookAsAdmin() {
        library.addBook(book1, admin);
        assertEquals(1, library.getBookCount());
        assertTrue(library.getAllBooks().contains(book1));
    }

    /** Tests that a non-admin cannot add a book. */
    @Test
    void testAddBookAsNonAdmin() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> library.addBook(book1, regularUser));
        assertEquals("Only admins can add books!", e.getMessage());
        assertEquals(0, library.getBookCount());
    }

    /** Tests adding a null book throws an exception. */
    @Test
    void testAddNullBook() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> library.addBook(null, admin));
        assertEquals("Book cannot be null", e.getMessage());
    }

    /** Tests that adding a book with a null user throws an exception. */
    @Test
    void testAddBookWithNullUser() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> library.addBook(book1, null));
        assertEquals("User cannot be null", e.getMessage());
    }

    /** Tests that adding a book with a duplicate ISBN throws an exception. */
    @Test
    void testAddBookWithDuplicateISBN() {
        library.addBook(book1, admin);
        Book duplicate = new Book("123", "Advanced Java", "Jane Doe");
        Exception e = assertThrows(IllegalArgumentException.class, () -> library.addBook(duplicate, admin));
        assertEquals("Book with ISBN 123 already exists!", e.getMessage());
        assertEquals(1, library.getBookCount());
    }

    /** Tests searching books by title, author, or ISBN. */
    @Test
    void testSearchBookByTitleAuthorISBN() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> byTitle = library.searchBook("java");
        assertTrue(byTitle.contains(book1));

        List<Book> byAuthor = library.searchBook("alice");
        assertTrue(byAuthor.contains(book2));

        List<Book> byISBN = library.searchBook("456");
        assertTrue(byISBN.contains(book2));
    }

    /** Tests partial and case-insensitive search functionality. */
    @Test
    void testSearchBookPartialAndCaseInsensitive() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> partial = library.searchBook("Intro");
        assertTrue(partial.contains(book2));

        List<Book> caseInsensitive = library.searchBook("PYTHON");
        assertTrue(caseInsensitive.contains(book2));
    }

    /** Tests search returns empty list if book not found. */
    @Test
    void testSearchBookNotFound() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        List<Book> result = library.searchBook("C++");
        assertTrue(result.isEmpty());
    }

    /** Tests searching with a null keyword throws exception. */
    @Test
    void testSearchBookNullKeyword() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> library.searchBook(null));
        assertEquals("Search keyword cannot be null", e.getMessage());
    }

    /** Tests searching in an empty library returns empty list. */
    @Test
    void testSearchBookInEmptyLibrary() {
        List<Book> result = library.searchBook("Java");
        assertTrue(result.isEmpty());
    }


    /** Tests getAllBooks returns a new list each time. */
    @Test
    void testGetAllBooksReturnsNewList() {
        library.addBook(book1, admin);
        List<Book> list1 = library.getAllBooks();
        List<Book> list2 = library.getAllBooks();
        assertNotSame(list1, list2);
        list1.clear();
        assertEquals(1, library.getBookCount());
    }

    /** Tests getAllBooks content and count correctness. */
    @Test
    void testGetAllBooksContentAndCount() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);
        List<Book> allBooks = library.getAllBooks();
        assertEquals(2, allBooks.size());
        assertTrue(allBooks.contains(book1));
        assertTrue(allBooks.contains(book2));
        assertEquals(2, library.getBookCount());
    }

    /** Tests newly added book is available. */
    @Test
    void testBookIsAvailableAfterAdd() {
        library.addBook(book1, admin);
        assertTrue(library.getAllBooks().get(0).isAvailable());
    }


    /** Tests successful borrowing of a book. */
    @Test
    void testBorrowBookSuccessfully() {
        library.addBook(book1, admin);
        boolean success = library.borrowBook(regularUser, "123");
        assertTrue(success);
        assertFalse(book1.isAvailable());
    }

    /** Tests borrowing a book that is already borrowed fails. */
    @Test
    void testBorrowBookAlreadyBorrowed() {
        library.addBook(book1, admin);
        library.borrowBook(regularUser, "123");
        User anotherUser = new User("user2", Role.USER, "pass");
        boolean success = library.borrowBook(anotherUser, "123");
        assertFalse(success);
    }

    /** Tests borrowing with null user or ISBN throws exception. */
    @Test
    void testBorrowBookWithNulls() {
        Exception e1 = assertThrows(IllegalArgumentException.class, () -> library.borrowBook(null, "123"));
        Exception e2 = assertThrows(IllegalArgumentException.class, () -> library.borrowBook(regularUser, null));
        assertEquals("User and ISBN cannot be null.", e1.getMessage());
        assertEquals("User and ISBN cannot be null.", e2.getMessage());
    }

    /** Tests borrowing a non-existent book throws exception. */
    @Test
    void testBorrowBookNotFound() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> library.borrowBook(regularUser, "999"));
        assertEquals("Book not found.", e.getMessage());
    }

    /** Tests borrowing fails if user has unpaid fines. */
    @Test
    void testBorrowBookWithUnpaidFines() {
        library.addBook(book1, admin);
        regularUser.addFine(BigDecimal.valueOf(50));
        Exception e = assertThrows(IllegalStateException.class, () -> library.borrowBook(regularUser, "123"));
        assertEquals("User has unpaid fines and cannot borrow books.", e.getMessage());
    }

    /** Tests successful returning of a borrowed book. */
    @Test
    void testReturnBookSuccessfully() {
        library.addBook(book1, admin);
        library.borrowBook(regularUser, "123");
        library.returnBook(regularUser, "123");
        assertTrue(book1.isAvailable());
    }

    /** Tests returning a book that was not borrowed throws exception. */
    @Test
    void testReturnBookNotBorrowed() {
        library.addBook(book1, admin);
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> library.returnBook(regularUser, "123"));
        assertEquals("No active borrowing found.", e.getMessage());
    }


    /** Tests overdue fines are applied correctly. */
    @Test
    void testApplyOverdueFines() {
        library.addBook(book1, admin);
        library.borrowBook(regularUser, "123");
        LocalDate overdueDate = LocalDate.now().plusDays(30);
        library.applyOverdueFines(overdueDate);
        assertTrue(regularUser.getFineBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    /** Tests retrieving overdue books. */
    @Test
    void testGetOverdueBooks() {
        library.addBook(book1, admin);
        library.borrowBook(regularUser, "123");
        LocalDate overdueDate = LocalDate.now().plusDays(29);
        List<BorrowRecord> overdue = library.getOverdueBooks(overdueDate);
        assertEquals(1, overdue.size());
        assertEquals(book1, overdue.get(0).getBook());
    }


    /** Tests full library workflow from adding books to borrowing and returning. */
    @Test
    void testFullLibraryFlow() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);
        assertEquals(2, library.getBookCount());

        library.borrowBook(regularUser, "123");
        assertFalse(book1.isAvailable());

        User user2 = new User("user2", Role.USER, "pass");
        library.borrowBook(user2, "456");
        assertFalse(book2.isAvailable());

        boolean success = library.borrowBook(user2, "123");
        assertFalse(success);

        library.returnBook(regularUser, "123");
        assertTrue(book1.isAvailable());

        List<Book> javaBooks = library.searchBook("java");
        assertEquals(1, javaBooks.size());
        assertEquals(book1, javaBooks.get(0));
    }


    /** Tests getAllBorrowRecords on an empty library. */
    @Test
    void testGetAllBorrowRecordsEmpty() {
        List<BorrowRecord> records = library.getAllBorrowRecords();
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    /** Tests getAllBorrowRecords with active and returned borrow records. */
    @Test
    void testGetAllBorrowRecordsWithActiveAndReturned() {
        library.addBook(book1, admin);
        library.addBook(book2, admin);

        library.borrowBook(regularUser, "123");
        User user2 = new User("user2", Role.USER, "pass");
        library.borrowBook(user2, "456");
        library.returnBook(user2, "456");

        List<BorrowRecord> records = library.getAllBorrowRecords();
        assertEquals(2, records.size());

        BorrowRecord activeRecord = records.stream()
                .filter(r -> r.getBook().getIsbn().equals("123"))
                .findFirst()
                .orElseThrow();
        BorrowRecord returnedRecord = records.stream()
                .filter(r -> r.getBook().getIsbn().equals("456"))
                .findFirst()
                .orElseThrow();

        assertFalse(activeRecord.isReturned());
        assertTrue(returnedRecord.isReturned());
    }

    /** Tests that fines are applied and user's fine balance updated. */
    @Test
    void testGetAllBorrowRecordsUpdatesFines() {
        library.addBook(book1, admin);
        library.borrowBook(regularUser, "123");
        LocalDate overdueDate = LocalDate.now().plusDays(30);
        library.applyOverdueFines(overdueDate);

        List<BorrowRecord> records = library.getAllBorrowRecords();
        BorrowRecord record = records.get(0);
        BigDecimal fine = record.getFine(overdueDate);
        assertTrue(fine.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(regularUser.getFineBalance().compareTo(BigDecimal.ZERO) > 0);
    }
}
