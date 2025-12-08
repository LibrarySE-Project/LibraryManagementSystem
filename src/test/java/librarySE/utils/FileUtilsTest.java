package librarySE.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class FileUtilsTest {

    /** Temporary directory used only for the non-library_data tests. */
    private Path tempDir;

    /** JSON file inside the temporary directory. */
    private Path jsonFile;

    @BeforeEach
    void setup() throws Exception {
        tempDir = Files.createTempDirectory("fileutils_test");
        jsonFile = tempDir.resolve("test.json");
    }

    @Test
    void writeJson_createsFileInCustomDirectory() {
        List<String> data = List.of("a", "b");

        FileUtils.writeJson(jsonFile, data);

        assertTrue(Files.exists(jsonFile), "JSON file should be created");
    }

    @Test
    void writeJson_createsBackupWhenFileAlreadyExists() throws Exception {
        Path jsonFile = FileUtils.dataFile("test_backup_file.json");

        if (Files.exists(jsonFile)) {
            Files.delete(jsonFile);
        }

        List<String> original = List.of("old");
        List<String> updated  = List.of("new");

        FileUtils.writeJson(jsonFile, original);
        assertTrue(Files.exists(jsonFile));

        FileUtils.writeJson(jsonFile, updated);

        Path backupDir = FileUtils.dataFile("backups");
        assertTrue(Files.exists(backupDir));

        long count = Files.list(backupDir)
                .filter(p -> p.getFileName().toString()
                        .startsWith("test_backup_file_backup"))
                .count();

        assertTrue(count >= 1);
    }

    @Test
    void writeJson_createsBackupWithCleanBackupDirectory() throws Exception {
        Path jsonFile = FileUtils.dataFile("test_backup.json");
        Path backupDir = FileUtils.dataFile("backups");

        if (Files.exists(jsonFile)) {
            Files.delete(jsonFile);
        }

        if (Files.exists(backupDir)) {
            Files.list(backupDir).forEach(p -> {
                try { Files.delete(p); } catch (Exception ignored) {}
            });
        }

        FileUtils.writeJson(jsonFile, List.of("old"));
        FileUtils.writeJson(jsonFile, List.of("new"));

        long count = Files.list(backupDir)
                .filter(p -> p.getFileName().toString()
                        .startsWith("test_backup_backup_"))
                .count();

        assertTrue(count >= 1);
    }

    @Test
    void writeJson_throwsRuntimeExceptionOnIOFailure_customTempPath() {
        Path impossible = tempDir.resolve("file.txt").resolve("child.json");

        assertThrows(RuntimeException.class,
                () -> FileUtils.writeJson(impossible, List.of("x")));
    }

    @Test
    void writeJson_throwsRuntimeExceptionOnIOFailure_usingDataFile() {
        Path impossible = FileUtils.dataFile("abc.txt").resolve("child.json");

        assertThrows(RuntimeException.class,
                () -> FileUtils.writeJson(impossible, List.of("x")));
    }

    @Test
    void readJson_readsDataCorrectly() {
        List<String> data = List.of("hello", "world");
        FileUtils.writeJson(jsonFile, data);

        List<String> result = FileUtils.readJson(
                jsonFile,
                FileUtils.listTypeOf(String.class),
                List.of()
        );

        assertEquals(2, result.size());
        assertEquals("hello", result.get(0));
        assertEquals("world", result.get(1));
    }

    @Test
    void readJson_returnsDefaultIfFileMissing() {
        Path missing = tempDir.resolve("missing.json");

        List<String> result = FileUtils.readJson(
                missing,
                FileUtils.listTypeOf(String.class),
                List.of("x")
        );

        assertEquals(1, result.size());
        assertEquals("x", result.get(0));
    }

    @Test
    void readJson_throwsRuntimeExceptionOnCorruptedFile() throws Exception {
        Files.createDirectory(jsonFile);

        assertThrows(RuntimeException.class,
                () -> FileUtils.readJson(
                        jsonFile,
                        FileUtils.listTypeOf(String.class),
                        List.of()
                ));
    }

    @Test
    void listTypeOf_returnsNonNullType() {
        assertNotNull(FileUtils.listTypeOf(String.class));
    }

    @Test
    void dataFile_returnsPathInsideLibraryData() {
        Path p = FileUtils.dataFile("users.json");

        assertTrue(p.toString().contains("library_data"));
        assertTrue(p.toString().endsWith("users.json"));
    }

    @Test
    void writeJson_createsBackupDirectoryIfMissing() throws Exception {
        Path backupDir = FileUtils.dataFile("backups");

        if (Files.exists(backupDir)) {
            Files.walk(backupDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.delete(p); } catch (Exception ignored) {}
                    });
        }

        Path jsonFile = FileUtils.dataFile("test_create_dir.json");

        FileUtils.writeJson(jsonFile, List.of("a"));

        assertTrue(Files.exists(backupDir));
    }
    @Test
    void testStaticBlock_initializesDataDirectory() throws Exception {

        // Delete directory first
        Path dir = Paths.get("library_data");
        if (Files.exists(dir)) {
            Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (Exception ignored) {}
                });
        }

        assertFalse(Files.exists(dir));

        // Reload FileUtils using a NEW ClassLoader
        URL classUrl = FileUtils.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation();

        try (URLClassLoader cl = new URLClassLoader(new URL[]{classUrl}, null)) {

            Class<?> reloaded = cl.loadClass("librarySE.utils.FileUtils");

            // force static block execution
            reloaded.getDeclaredConstructors();
        }

        // Verify directory created
        assertTrue(Files.exists(dir));
    }
    @Test
    void testStaticBlock_handlesIOException() throws Exception {

        try (MockedStatic<Files> mocked = mockStatic(Files.class)) {

            // Simulate: Files.exists(DATA_DIR) returns false
            mocked.when(() -> Files.exists(any(Path.class))).thenReturn(false);

            // Simulate: createDirectories throws IOException
            mocked.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("boom"));

            // Reload FileUtils using new class loader to force static block execution
            URL classUrl = FileUtils.class.getProtectionDomain().getCodeSource().getLocation();
            URLClassLoader cl = new URLClassLoader(new URL[]{classUrl}, null);

            Class<?> reloaded = cl.loadClass("librarySE.utils.FileUtils");

            assertThrows(RuntimeException.class, () -> {
                reloaded.getDeclaredConstructors(); // forces static block
            });
        }
    }


}






