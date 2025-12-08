package librarySE.managers.reports;

import librarySE.core.Book;
import librarySE.core.LibraryItem;
import librarySE.managers.BorrowRecord;
import librarySE.managers.Role;
import librarySE.managers.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

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

        r = new BorrowRecord(
                u,
                book,
                book.getMaterialType().createFineStrategy(),
                LocalDate.now().minusDays(50)
        );

        fineService = new FineReportService(List.of(r));
        exporter = new ReportExporter(fineService);
    }

    @Test
    @DisplayName("constructor: null FineReportService should throw IllegalArgumentException")
    void constructor_nullService_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new ReportExporter(null)
        );
        assertEquals("FineReportService cannot be null.", ex.getMessage());
    }

    @Test
    @DisplayName("generateFinesReport: CSV should contain header and user row")
    void generateFinesReport_notEmpty() {
        LocalDate today = LocalDate.now();

        String csv = exporter.generateFinesReport(today);

        assertNotNull(csv);
        assertTrue(csv.startsWith("User,Total Fines,Book,CD,Journal"));
        assertTrue(csv.contains("A"));
    }

    @Test
    @DisplayName("generateFinesReport: null date should throw IllegalArgumentException")
    void generateFinesReport_nullDate_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> exporter.generateFinesReport(null)
        );
        assertEquals("Date cannot be null.", ex.getMessage());
    }

    @Test
    @DisplayName("exportFinesReportToCsv: creates directory and report file")
    void exportFinesReportToCsv_createsFile() throws IOException {
        LocalDate d = LocalDate.of(2025, 1, 1);

        Path reportsDir = Paths.get("library_data", "reports");
        Path file = reportsDir.resolve("fines_" + d + ".csv");

        if (Files.exists(file)) {
            Files.delete(file);
        }
        if (Files.exists(reportsDir)) {
            try {
                Files.delete(reportsDir);
            } catch (DirectoryNotEmptyException ignored) {
            }
        }

        exporter.exportFinesReportToCsv(d);

        assertTrue(Files.exists(reportsDir));
        assertTrue(Files.exists(file));

        String fileContent = Files.readString(file);
        String expected = exporter.generateFinesReport(d);
        assertEquals(expected.trim(), fileContent.trim());
    }

    @Test
    @DisplayName("exportFinesReportToCsv: null date should throw IllegalArgumentException")
    void exportFinesReportToCsv_nullDate_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> exporter.exportFinesReportToCsv(null)
        );
        assertEquals("Date cannot be null.", ex.getMessage());
    }

    @Test
    @DisplayName("exportFinesReportToCsv: should throw RuntimeException when directory creation fails")
    void exportFinesReportToCsv_directoryCreationFails() throws IOException {
        LocalDate d = LocalDate.of(2025, 2, 1);
        Path reportsDir = Paths.get("library_data", "reports");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {

            filesMock.when(() -> Files.exists(reportsDir)).thenReturn(false);
            filesMock.when(() -> Files.createDirectories(reportsDir))
                     .thenThrow(new IOException("Simulated directory failure"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> exporter.exportFinesReportToCsv(d));

            assertTrue(ex.getMessage().contains("Failed to create reports directory."));
        }
    }

    @Test
    @DisplayName("exportFinesReportToCsv: should throw RuntimeException when file writing fails")
    void exportFinesReportToCsv_writeFails() throws IOException {
        LocalDate d = LocalDate.of(2025, 3, 1);

        Path reportsDir = Paths.get("library_data", "reports");
        Path file = reportsDir.resolve("fines_" + d + ".csv");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {

            filesMock.when(() -> Files.exists(reportsDir)).thenReturn(true);

            filesMock.when(() ->
                    Files.writeString(
                            eq(file),
                            anyString(),
                            any(StandardOpenOption.class),
                            any(StandardOpenOption.class))
            ).thenThrow(new IOException("Simulated write failure"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> exporter.exportFinesReportToCsv(d));

            assertTrue(ex.getMessage().contains("Failed to write report"));
        }
    }
    @Test
    @DisplayName("exportFinesReportToCsv: should create directories when they do not exist")
    void exportFinesReportToCsv_directoryCreatedSuccessfully() {
        LocalDate d = LocalDate.of(2025, 4, 1);

        Path reportsDir = Paths.get("library_data", "reports");
        Path file = reportsDir.resolve("fines_" + d + ".csv");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {

            // Directory does not exist → createDirectories() should be called
            filesMock.when(() -> Files.exists(reportsDir)).thenReturn(false);

            // Simulate successful directory creation (no exception)
            filesMock.when(() -> Files.createDirectories(reportsDir))
                     .thenReturn(reportsDir);

            // Simulate successful write (no exception)
            filesMock.when(() ->
                    Files.writeString(
                            eq(file),
                            anyString(),
                            any(StandardOpenOption.class),
                            any(StandardOpenOption.class))
            ).thenReturn(file);

            // No exception should be thrown from exportFinesReportToCsv
            assertDoesNotThrow(() -> exporter.exportFinesReportToCsv(d));
        }
    }
}


