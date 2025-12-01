package librarySE.managers.reports;

import librarySE.core.*;
import librarySE.managers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ActivityReportServiceTest {

    ActivityReportService service;
    User u1, u2;
    LibraryItem book1, book2;
    BorrowRecord r1, r2, r3;

    @BeforeEach
    void setup() {
        u1 = new User("A", Role.USER, "pass123", "a@ps.com");
        u2 = new User("B", Role.USER, "pass123", "b@ps.com");

        book1 = new Book("ISBN1", "T1", "A1", BigDecimal.TEN);
        book2 = new Book("ISBN2", "T2", "A2", BigDecimal.ONE);

        // borrow dates (all old → guaranteed overdue when checking with a future date)
        r1 = new BorrowRecord(u1, book1, book1.getMaterialType().createFineStrategy(), LocalDate.now().minusDays(10));
        r2 = new BorrowRecord(u1, book2, book2.getMaterialType().createFineStrategy(), LocalDate.now().minusDays(5));
        r3 = new BorrowRecord(u2, book1, book1.getMaterialType().createFineStrategy(), LocalDate.now().minusDays(20));

        service = new ActivityReportService(List.of(r1, r2, r3));
    }

    // ===========================
    // Constructor Tests
    // ===========================
    @Test
    void testConstructorRejectsNullList() {
        assertThrows(IllegalArgumentException.class, () -> new ActivityReportService(null));
    }

    @Test
    void testConstructorAcceptsEmptyList() {
        ActivityReportService s = new ActivityReportService(Collections.emptyList());
        assertNotNull(s);
    }


    // ===========================
    // getTopBorrowers Tests
    // ===========================
    @Test
    void testGetTopBorrowers() {
        Map<User, Long> map = service.getTopBorrowers();
        assertEquals(2L, map.get(u1));
        assertEquals(1L, map.get(u2));
    }

    @Test
    void testGetTopBorrowersEmpty() {
        ActivityReportService s = new ActivityReportService(Collections.emptyList());
        Map<User, Long> map = s.getTopBorrowers();
        assertTrue(map.isEmpty());
    }


    // ===========================
    // getMostBorrowedItems Tests
    // ===========================
    @Test
    void testGetMostBorrowedItems() {
        Map<LibraryItem, Long> map = service.getMostBorrowedItems();
        assertEquals(2L, map.get(book1));
        assertEquals(1L, map.get(book2));
    }

    @Test
    void testGetMostBorrowedItemsEmpty() {
        ActivityReportService s = new ActivityReportService(Collections.emptyList());
        assertTrue(s.getMostBorrowedItems().isEmpty());
    }


    // ===========================
    // getOverdueItemsForUser Tests
    // ===========================
    @Test
    void testGetOverdueItemsForUser() {
        LocalDate today = LocalDate.now().plusDays(50);
        List<BorrowRecord> list = service.getOverdueItemsForUser(u1, today);

        assertEquals(2, list.size());  // both r1 and r2 should be overdue for u1
        assertTrue(list.contains(r1));
        assertTrue(list.contains(r2));
    }

    @Test
    void testGetOverdueItemsForUserNoneOverdue() {
        LocalDate dateBeforeDue = LocalDate.now();  // same date → not overdue

        List<BorrowRecord> list = service.getOverdueItemsForUser(u1, dateBeforeDue);

        assertTrue(list.isEmpty());
    }

    @Test
    void testGetOverdueItemsForUserInvalidUser() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getOverdueItemsForUser(null, LocalDate.now()));
    }

    @Test
    void testGetOverdueItemsForUserInvalidDate() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getOverdueItemsForUser(u1, null));
    }

    @Test
    void testGetOverdueItemsForUser_EmptyRecords() {
        ActivityReportService s = new ActivityReportService(Collections.emptyList());
        List<BorrowRecord> list = s.getOverdueItemsForUser(u1, LocalDate.now().plusDays(10));
        assertTrue(list.isEmpty());
    }
}

