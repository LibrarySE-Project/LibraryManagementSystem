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
    User u1, u2;
    LibraryItem book, cd;
    BorrowRecord r1, r2, r3;

    @BeforeEach
    void setup() {
        u1 = new User("A", Role.USER, "pass123", "a@ps.com");
        u2 = new User("B", Role.USER, "pass123", "b@ps.com");

        book = new Book("ISBN", "Title", "Author", BigDecimal.valueOf(20));
        cd = new CD("CDTitle", "Artist", BigDecimal.valueOf(15));

        r1 = new BorrowRecord(u1, book, book.getMaterialType().createFineStrategy(), LocalDate.now().minusDays(40));
        r2 = new BorrowRecord(u1, cd, cd.getMaterialType().createFineStrategy(), LocalDate.now().minusDays(50));
        r3 = new BorrowRecord(u2, book, book.getMaterialType().createFineStrategy(), LocalDate.now().minusDays(5));

        service = new FineReportService(List.of(r1, r2, r3));
    }

    @Test
    void getTotalFinesForUser() {
        BigDecimal total = service.getTotalFinesForUser(u1, LocalDate.now());
        assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void getFinesByMediaType() {
        Map<MaterialType, BigDecimal> map =
                service.getFinesByMediaType(u1, LocalDate.now());

        assertTrue(map.get(MaterialType.BOOK).compareTo(BigDecimal.ZERO) > 0);
        assertTrue(map.get(MaterialType.CD).compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void getTotalFinesForAllUsers() {
        Map<User, BigDecimal> m = service.getTotalFinesForAllUsers(LocalDate.now());
        assertEquals(2, m.size());
    }
}

