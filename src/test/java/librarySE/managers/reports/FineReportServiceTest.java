package librarySE.managers.reports;

import librarySE.core.*;
import librarySE.managers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FineReportServiceTest {

    FineReportService service;
    User u1, u2, u3;
    LibraryItem book, cd;
    BorrowRecord r1, r2, r3;

    LocalDate today;

    @BeforeEach
    void setup() {
        today = LocalDate.now();

        u1 = new User("A", Role.USER, "pass123", "a@ps.com");
        u2 = new User("B", Role.USER, "pass123", "b@ps.com");
        u3 = new User("C", Role.USER, "pass123", "c@ps.com"); // no records → test empty behavior

        book = new Book("ISBN", "Title", "Author", BigDecimal.valueOf(20));
        cd   = new CD("CDTitle", "Artist", BigDecimal.valueOf(15));

        // Overdue values (positive fine)
        r1 = new BorrowRecord(u1, book, book.getMaterialType().createFineStrategy(), today.minusDays(40));
        r2 = new BorrowRecord(u1, cd, cd.getMaterialType().createFineStrategy(), today.minusDays(50));

        // user 2 small overdue
        r3 = new BorrowRecord(u2, book, book.getMaterialType().createFineStrategy(), today.minusDays(5));

        service = new FineReportService(List.of(r1, r2, r3));
    }

    // ==========================
    // Constructor tests
    // ==========================

    @Test
    void testConstructorRejectsNullList() {
        assertThrows(IllegalArgumentException.class, () -> new FineReportService(null));
    }

    @Test
    void testConstructorAcceptsEmptyList() {
        FineReportService s = new FineReportService(Collections.emptyList());
        assertNotNull(s);
    }

    // ==========================
    // getTotalFinesForUser tests
    // ==========================

    @Test
    void testGetTotalFinesForUserSuccess() {
        BigDecimal total = service.getTotalFinesForUser(u1, today);
        assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testGetTotalFinesForUser_NoRecords() {
        BigDecimal total = service.getTotalFinesForUser(u3, today);
        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void testGetTotalFinesForUser_NullUser() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getTotalFinesForUser(null, today));
    }

    @Test
    void testGetTotalFinesForUser_NullDate() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getTotalFinesForUser(u1, null));
    }

    // ==========================
    // getFinesByMediaType tests
    // ==========================

    @Test
    void testGetFinesByMediaTypeSuccess() {
        Map<MaterialType, BigDecimal> result = service.getFinesByMediaType(u1, today);

        assertTrue(result.get(MaterialType.BOOK).compareTo(BigDecimal.ZERO) > 0);
        assertTrue(result.get(MaterialType.CD).compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testGetFinesByMediaType_NoRecords() {
        Map<MaterialType, BigDecimal> result = service.getFinesByMediaType(u3, today);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetFinesByMediaType_NullUser() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getFinesByMediaType(null, today));
    }

    @Test
    void testGetFinesByMediaType_NullDate() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getFinesByMediaType(u1, null));
    }

    // ==========================
    // getTotalFinesForAllUsers tests
    // ==========================

    @Test
    void testGetTotalFinesForAllUsersSuccess() {
        Map<User, BigDecimal> map = service.getTotalFinesForAllUsers(today);

        assertTrue(map.containsKey(u1));
        assertTrue(map.containsKey(u2));
        assertEquals(2, map.keySet().size());

    }

    @Test
    void testGetTotalFinesForAllUsers_EmptyRecords() {
        FineReportService s = new FineReportService(Collections.emptyList());
        Map<User, BigDecimal> map = s.getTotalFinesForAllUsers(today);

        assertTrue(map.isEmpty());
    }

    @Test
    void testGetTotalFinesForAllUsers_NullDate() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getTotalFinesForAllUsers(null));
    }
}

