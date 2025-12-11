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

    private ActivityReportService service;
    private User u1, u2;
    private LibraryItem book1, book2;
    private BorrowRecord r1, r2, r3;

    @BeforeEach
    void setup() {
        u1 = new User("A", Role.USER, "pass123", "a@ps.com");
        u2 = new User("B", Role.USER, "pass123", "b@ps.com");

        book1 = new Book("ISBN1", "T1", "A1", BigDecimal.TEN);
        book2 = new Book("ISBN2", "T2", "A2", BigDecimal.ONE);

        // Borrow dates in the past so they can become overdue for a future reference date
        r1 = new BorrowRecord(
                u1,
                book1,
                book1.getMaterialType().createFineStrategy(),
                LocalDate.now().minusDays(10)
        );
        r2 = new BorrowRecord(
                u1,
                book2,
                book2.getMaterialType().createFineStrategy(),
                LocalDate.now().minusDays(5)
        );
        r3 = new BorrowRecord(
                u2,
                book1,
                book1.getMaterialType().createFineStrategy(),
                LocalDate.now().minusDays(20)
        );

        service = new ActivityReportService(List.of(r1, r2, r3));
    }

    // ===========================
    // Constructor tests
    // ===========================

    @Test
    void testConstructorRejectsNullList() {
        assertThrows(IllegalArgumentException.class,
                () -> new ActivityReportService(null));
    }

    @Test
    void testConstructorAcceptsEmptyList() {
        ActivityReportService s = new ActivityReportService(Collections.emptyList());
        assertNotNull(s);
    }

    // ===========================
    // getTopBorrowers tests
    // ===========================

    @Test
    void testGetTopBorrowers() {
        Map<User, Long> map = service.getTopBorrowers();

        // u1 has r1 and r2, u2 has r3
        assertEquals(2L, map.get(u1));
        assertEquals(1L, map.get(u2));
        assertEquals(2, map.size());
    }

    @Test
    void testGetTopBorrowersEmpty() {
        ActivityReportService s = new ActivityReportService(Collections.emptyList());
        Map<User, Long> map = s.getTopBorrowers();
        assertTrue(map.isEmpty());
    }

    // ===========================
    // getMostBorrowedItems tests
    // ===========================

    @Test
    void testGetMostBorrowedItems() {
        Map<String, Long> map = service.getMostBorrowedItems();

        String labelBook1 = book1.getTitle() + " (" + book1.getMaterialType() + ")";
        String labelBook2 = book2.getTitle() + " (" + book2.getMaterialType() + ")";

        assertEquals(2L, map.get(labelBook1)); // r1 and r3
        assertEquals(1L, map.get(labelBook2)); // r2
        assertEquals(2, map.size());
    }

    @Test
    void testGetMostBorrowedItemsEmpty() {
        ActivityReportService s = new ActivityReportService(Collections.emptyList());
        Map<String, Long> map = s.getMostBorrowedItems();
        assertTrue(map.isEmpty());
    }

    // ===========================
    // getOverdueItemsForUser tests
    // ===========================

    @Test
    void testGetOverdueItemsForUser() {
        LocalDate farFuture = LocalDate.now().plusDays(50);
        List<BorrowRecord> list = service.getOverdueItemsForUser(u1, farFuture);

        assertEquals(2, list.size());
        assertTrue(list.contains(r1));
        assertTrue(list.contains(r2));
    }

    @Test
    void testGetOverdueItemsForUserNoneOverdue() {
        LocalDate today = LocalDate.now();
        List<BorrowRecord> list = service.getOverdueItemsForUser(u1, today);

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
