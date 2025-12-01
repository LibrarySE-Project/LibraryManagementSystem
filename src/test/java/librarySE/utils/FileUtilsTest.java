package librarySE.utils;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    private Path tempDir;
    private Path jsonFile;

    @BeforeEach
    void setup() throws Exception {
        tempDir = Files.createTempDirectory("fileutils_test");
        jsonFile = tempDir.resolve("test.json");
    }

    // ---------------------------------------------------------
    // WRITE TESTS
    // ---------------------------------------------------------
    @Test
    void writeJson_createsFile() {
        List<String> data = List.of("a", "b");

        FileUtils.writeJson(jsonFile, data);

        assertTrue(Files.exists(jsonFile));
    }
    @Test
    void writeJson_createsBackupWhenFileExists() throws Exception {

        Path jsonFile = FileUtils.dataFile("test_backup_file.json");

        // Ensure clean start
        if (Files.exists(jsonFile)) Files.delete(jsonFile);

        List<String> original = List.of("old");
        List<String> updated  = List.of("new");

        // Create initial file
        FileUtils.writeJson(jsonFile, original);
        assertTrue(Files.exists(jsonFile));

        // Write again → should create backup
        FileUtils.writeJson(jsonFile, updated);

        Path backupDir = FileUtils.dataFile("backups");
        assertTrue(Files.exists(backupDir), "Backup directory must exist");

        long count = Files.list(backupDir)
                .filter(p -> p.getFileName().toString().startsWith("test_backup_file_backup"))
                .count();

        assertTrue(count >= 1, "Backup file should be created");
    }




    @Test
    void writeJson_throwsRuntimeExceptionOnIOFailure() {
        // Impossible path: cannot create child.json inside a normal file (file.txt)
        Path invalidFile = tempDir.resolve("file.txt").resolve("child.json");

        assertThrows(RuntimeException.class,
                () -> FileUtils.writeJson(invalidFile, List.of("x")));
    }

    // ---------------------------------------------------------
    // READ TESTS
    // ---------------------------------------------------------

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
    }

    @Test
    void readJson_returnsDefaultIfMissing() {
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
        // Make "jsonFile" a directory → causes IOException when opening reader
        Files.createDirectory(jsonFile);

        assertThrows(RuntimeException.class,
                () -> FileUtils.readJson(
                        jsonFile,
                        FileUtils.listTypeOf(String.class),
                        List.of()
                ));
    }

    // ---------------------------------------------------------
    // TYPE TOKEN
    // ---------------------------------------------------------
    @Test
    void testListTypeOf_returnsValidType() {
        assertNotNull(FileUtils.listTypeOf(String.class));
    }

    // ---------------------------------------------------------
    // dataFile() TEST
    // ---------------------------------------------------------
    @Test
    void testDataFile_returnsCorrectPath() {
        Path p = FileUtils.dataFile("users.json");
        assertTrue(p.toString().contains("library_data"));
        assertTrue(p.toString().endsWith("users.json"));
    }
    @Test
    void writeJson_createsBackupDirectoryIfMissing() throws Exception {
        Path backupDir = FileUtils.dataFile("backups");

        if (Files.exists(backupDir))
            Files.walk(backupDir).sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.delete(p); } catch (Exception ignored) {}
            });

        Path jsonFile = FileUtils.dataFile("test_create_dir.json");

        FileUtils.writeJson(jsonFile, List.of("a"));

        assertTrue(Files.exists(backupDir), "Backup directory must be created!");
    }
    @Test
    void writeJson_createsBackupWhenFileExists0() throws Exception {

        Path jsonFile = FileUtils.dataFile("test_backup.json");

        if (Files.exists(jsonFile)) Files.delete(jsonFile);

        Path backupDir = FileUtils.dataFile("backups");

        if (Files.exists(backupDir)) {
            Files.list(backupDir).forEach(p -> {
                try { Files.delete(p); } catch (Exception ignored) {}
            });
        }

        FileUtils.writeJson(jsonFile, List.of("old"));

        FileUtils.writeJson(jsonFile, List.of("new"));

        long count = Files.list(backupDir)
                .filter(p -> p.getFileName().toString().startsWith("test_backup_backup_"))
                .count();

        assertTrue(count >= 1, "Backup file should be created!");
    }

    @Test
    void writeJson_throwsRuntimeExceptionOnIOFailure0() {
        Path impossible = FileUtils.dataFile("abc.txt").resolve("child.json");

        assertThrows(RuntimeException.class,
                () -> FileUtils.writeJson(impossible, List.of("x"))
        );
    }




}
