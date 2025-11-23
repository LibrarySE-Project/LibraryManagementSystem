package librarySE.managers.reports;

import librarySE.core.*;
import librarySE.managers.*;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ReportExporterTest {

    FineReportService fineService;
    ReportExporter exporter;
    User u;
    LibraryItem book;
    BorrowRecord r;

    @BeforeEach
    void setup() {
        u = new User("A", Role.USER, "pass123", "a@ps.com");
        book = new Book("ISBN", "Title", "Auth", BigDecimal.TEN);

        r = new BorrowRecord(u, book, book.getMaterialType().createFineStrategy(),
                LocalDate.now().minusDays(50));

        fineService = new FineReportService(List.of(r));
        exporter = new ReportExporter(fineService);
    }

    @Test
    void generateFinesReport_NotEmpty() {
        String csv = exporter.generateFinesReport(LocalDate.now());
        assertTrue(csv.contains("User"));
        assertTrue(csv.contains("A"));
    }

    @Test
    void exportFinesReportToCsv_CreatesFile() {
        LocalDate d = LocalDate.of(2025,1,1);

        exporter.exportFinesReportToCsv(d);

        Path path = Paths.get("library_data", "reports", "fines_" + d + ".csv");
        assertTrue(Files.exists(path));
    }
}

