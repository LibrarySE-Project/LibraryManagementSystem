package librarySE.utils;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import java.net.URL;
import java.net.URLClassLoader;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoggerUtilsTest {

    private static Path logDir;

    @BeforeAll
    static void setup() throws IOException {
        logDir = Paths.get("library_data", "logs");
        Files.createDirectories(logDir);
    }

    @AfterEach
    void cleanupFiles() throws IOException {
        if (Files.exists(logDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(logDir, "*.txt")) {
                for (Path p : stream) {
                    Files.deleteIfExists(p);
                }
            }
        }
    }

    @Test
    @Order(1)
    void testLog_createsNewFileAndWritesContent() throws Exception {
        String fileName = "test_create.txt";
        Path logFile = logDir.resolve(fileName);
        Files.deleteIfExists(logFile);

        LoggerUtils.log(fileName, "Hello");

        assertTrue(Files.exists(logFile));
        List<String> lines = Files.readAllLines(logFile);

        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("Hello"));
    }

    @Test
    @Order(2)
    void testLog_appendsToExistingFile() throws Exception {
        String fileName = "append_test.txt";
        Path logFile = logDir.resolve(fileName);
        Files.deleteIfExists(logFile);

        LoggerUtils.log(fileName, "First");
        LoggerUtils.log(fileName, "Second");

        assertTrue(Files.exists(logFile));

        List<String> lines = Files.readAllLines(logFile);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("First"));
        assertTrue(lines.get(1).contains("Second"));
    }

    @Test
    @Order(3)
    void testLog_includesCallerMethodName() throws Exception {
        String fileName = "caller.txt";
        Path logFile = logDir.resolve(fileName);
        Files.deleteIfExists(logFile);

        LoggerUtils.log(fileName, "Check");

        List<String> lines = Files.readAllLines(logFile);
        assertEquals(1, lines.size());

        String line = lines.get(0);
        assertTrue(line.contains("LoggerUtilsTest.testLog_includesCallerMethodName"));
        assertTrue(line.contains("Check"));
    }

    @Test
    @Order(4)
    void testLog_throwsRuntimeExceptionOnFailure() throws Exception {
        String dirName = "dir_as_file";
        Path dir = logDir.resolve(dirName);
        Files.createDirectories(dir);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> LoggerUtils.log(dirName, "Err"));

        assertTrue(ex.getMessage().contains("Failed to write log"));
    }

    @Test
    @Order(5)
    void testStaticBlock_successWhenDirectoryAlreadyExists() throws Exception {
        try (MockedStatic<Files> mocked = mockStatic(Files.class)) {

            mocked.when(() -> Files.exists(any(Path.class))).thenReturn(true);

            URL classUrl = LoggerUtils.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation();

            try (URLClassLoader cl = new URLClassLoader(new URL[]{classUrl}, null)) {
                Class<?> clazz = Class.forName("librarySE.utils.LoggerUtils", true, cl);
                assertNotNull(clazz);
            }

            mocked.verify(() -> Files.createDirectories(any(Path.class)), never());
        }
    }

    @Test
    @Order(6)
    void testStaticBlock_handlesIOExceptionOnInit() throws Exception {
        try (MockedStatic<Files> mocked = mockStatic(Files.class)) {

            mocked.when(() -> Files.exists(any(Path.class))).thenReturn(false);
            mocked.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("boom"));

            URL classUrl = LoggerUtils.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation();

            try (URLClassLoader cl = new URLClassLoader(new URL[]{classUrl}, null)) {

                ExceptionInInitializerError error =
                        assertThrows(ExceptionInInitializerError.class,
                                () -> Class.forName("librarySE.utils.LoggerUtils", true, cl));

                assertNotNull(error.getCause());
                assertTrue(error.getCause() instanceof RuntimeException);
                assertTrue(error.getCause().getCause() instanceof IOException);
            }
        }
    }
    @Test
    void testStaticBlock_directoryExists_noCreation() throws Exception {
        try (MockedStatic<Files> mocked = mockStatic(Files.class)) {

            mocked.when(() -> Files.exists(any(Path.class))).thenReturn(true);

            URL url = LoggerUtils.class.getProtectionDomain().getCodeSource().getLocation();
            try (URLClassLoader cl = new URLClassLoader(new URL[]{url}, null)) {
                Class<?> cls = Class.forName("librarySE.utils.LoggerUtils", true, cl);
                assertNotNull(cls);
            }

            mocked.verify(() -> Files.createDirectories(any(Path.class)), never());
        }
    }
    @Test
    void testStaticBlock_directoryCreationFails_throwsRuntimeException() throws Exception {
        try (MockedStatic<Files> mocked = mockStatic(Files.class)) {

            mocked.when(() -> Files.exists(any(Path.class))).thenReturn(false);
            mocked.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("boom"));

            URL url = LoggerUtils.class.getProtectionDomain().getCodeSource().getLocation();

            try (URLClassLoader cl = new URLClassLoader(new URL[]{url}, null)) {

                ExceptionInInitializerError err =
                        assertThrows(ExceptionInInitializerError.class,
                                () -> Class.forName("librarySE.utils.LoggerUtils", true, cl));

                assertNotNull(err.getCause());
                assertTrue(err.getCause() instanceof RuntimeException);
                assertTrue(err.getCause().getCause() instanceof IOException);
            }
        }
    }


}
