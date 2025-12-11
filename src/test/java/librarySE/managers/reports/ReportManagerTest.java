package librarySE.managers.reports;

import librarySE.core.Book;
import librarySE.core.LibraryItem;
import librarySE.managers.BorrowRecord;
import librarySE.managers.Role;
import librarySE.managers.User;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportManagerTest {

    private ReportManager manager;
    private User u;
    private LibraryItem book;
    private BorrowRecord r;

    // Mocked exporter injected via reflection so we avoid real I/O
    private ReportExporter mockExporter;

    @BeforeEach
    void setup() throws Exception {
        // User from librarySE.managers with Role enum in the same package
        u = new User("A", Role.USER, "pass123", "a@ps.com");

        book = new Book("ISBN", "T", "A", BigDecimal.TEN);

        r = new BorrowRecord(
                u,
                book,
                book.getMaterialType().createFineStrategy(),
                LocalDate.now().minusDays(60)
        );

        manager = new ReportManager(List.of(r));

        // Replace the real exporter with a Mockito mock to control behavior
        mockExporter = mock(ReportExporter.class);
        Field exporterField = ReportManager.class.getDeclaredField("exporter");
        exporterField.setAccessible(true);
        exporterField.set(manager, mockExporter);
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
    void testExportCsv_DelegatesToExporter() {
        LocalDate today = LocalDate.now();

        manager.exportFinesCsv(today);

        verify(mockExporter).exportFinesReportToCsv(today);
    }

    @Test
    void testExportCsv_NullDatePropagatesIllegalArgumentException() {
        doThrow(new IllegalArgumentException("date must not be null"))
                .when(mockExporter).exportFinesReportToCsv(null);

        assertThrows(IllegalArgumentException.class,
                () -> manager.exportFinesCsv(null));
    }
}
