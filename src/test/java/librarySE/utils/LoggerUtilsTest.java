package librarySE.utils;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoggerUtilsTest {

    private static Path tempRoot;      // e.g.  /tmp/log_test_12345
    private static Path tempLogDir;    // e.g.  /tmp/log_test_12345/library_data/logs

    @BeforeAll
    static void setup() throws IOException {
        // Create isolated folder
        tempRoot = Files.createTempDirectory("log_test_");

        // Build same directory structure used by LoggerUtils
        tempLogDir = tempRoot.resolve("library_data/logs");
        Files.createDirectories(tempLogDir);

        // Change current working directory so LoggerUtils writes inside tempRoot
        System.setProperty("user.dir", tempRoot.toAbsolutePath().toString());
    }

    @AfterAll
    static void cleanup() throws IOException {
        // Delete all files created
        if (Files.exists(tempRoot)) {
            Files.walk(tempRoot)
                    .sorted((a, b) -> b.compareTo(a)) // delete children first
                    .forEach(path -> {
                        try { Files.deleteIfExists(path); } catch (Exception ignored) {}
                    });
        }
    }

    // --------------------------------------------------------------------
    @Test @Order(1)
    void testLog_createsNewFileAndWritesContent() throws Exception {
        String fileName = "test_create.txt";

        LoggerUtils.log(fileName, "Hello");

        Path logFile = tempLogDir.resolve(fileName);
        assertTrue(Files.exists(logFile));

        List<String> lines = Files.readAllLines(logFile);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("Hello"));
    }

    // --------------------------------------------------------------------
    @Test @Order(2)
    void testLog_appendsToExistingFile() throws Exception {
        String fileName = "append_test.txt";

        LoggerUtils.log(fileName, "First");
        LoggerUtils.log(fileName, "Second");

        Path logFile = tempLogDir.resolve(fileName);
        List<String> lines = Files.readAllLines(logFile);

        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("First"));
        assertTrue(lines.get(1).contains("Second"));
    }

    // --------------------------------------------------------------------
    @Test @Order(3)
    void testLog_includesCallerMethodName() throws Exception {
        String fileName = "caller.txt";

        LoggerUtils.log(fileName, "Check");

        Path logFile = tempLogDir.resolve(fileName);
        List<String> lines = Files.readAllLines(logFile);

        assertTrue(lines.get(0).contains("testLog_includesCallerMethodName"));
        assertTrue(lines.get(0).contains("Check"));
    }

    // --------------------------------------------------------------------
    @Test @Order(4)
    void testLog_throwsRuntimeExceptionOnFailure() throws Exception {
        // Create a file where a directory should be
        Path fakeDir = tempLogDir.resolve("not_a_dir");
        Files.writeString(fakeDir, "X"); // becomes a file, not directory

        // Now using it as a folder will cause IOException
        assertThrows(RuntimeException.class, () ->
                LoggerUtils.log("not_a_dir/file.txt", "Err")
        );
    }
}
