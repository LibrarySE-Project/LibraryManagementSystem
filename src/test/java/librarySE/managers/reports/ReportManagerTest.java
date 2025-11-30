package librarySE.managers.reports;

import librarySE.core.*;
import librarySE.managers.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ReportManagerTest {

    ReportManager manager;
    User u;
    LibraryItem book;
    BorrowRecord r;

    @BeforeEach
    void setup() {
        u = new User("A", Role.USER, "pass123", "a@ps.com");
        book = new Book("ISBN", "T", "A", BigDecimal.TEN);

        r = new BorrowRecord(u, book, book.getMaterialType().createFineStrategy(),
                LocalDate.now().minusDays(60));

        manager = new ReportManager(List.of(r));
    }

    @Test
    void finesServiceNotNull() {
        assertNotNull(manager.fines());
    }

    @Test
    void activityServiceNotNull() {
        assertNotNull(manager.activity());
    }

    @Test
    void exporterNotNull() {
        assertNotNull(manager.exporter());
    }

    @Test
    void exportCsv_NoException() {
        assertDoesNotThrow(() -> manager.exportFinesCsv(LocalDate.now()));
    }
}

