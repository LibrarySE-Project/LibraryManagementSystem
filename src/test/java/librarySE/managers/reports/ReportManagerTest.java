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

        r = new BorrowRecord(
                u,
                book,
                book.getMaterialType().createFineStrategy(),
                LocalDate.now().minusDays(60)
        );

        manager = new ReportManager(List.of(r));
    }

    // ================
    // Constructor tests
    // ================

    @Test
    void testConstructorRejectsNullList() {
        assertThrows(IllegalArgumentException.class,
                () -> new ReportManager(null));
    }

    @Test
    void testConstructorCreatesAllServices() {
        assertNotNull(manager.fines());
        assertNotNull(manager.activity());
        assertNotNull(manager.exporter());
    }

    // ===================
    // Service getter tests
    // ===================

    @Test
    void testFinesServiceNotNull() {
        assertNotNull(manager.fines());
    }

    @Test
    void testActivityServiceNotNull() {
        assertNotNull(manager.activity());
    }

    @Test
    void testExporterNotNull() {
        assertNotNull(manager.exporter());
    }

    // ==========================
    // exportFinesCsv() tests
    // ==========================

    @Test
    void testExportCsv_NoException() {
        assertDoesNotThrow(() -> manager.exportFinesCsv(LocalDate.now()));
    }

    @Test
    void testExportCsv_NullDateThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.exportFinesCsv(null));
    }
}
