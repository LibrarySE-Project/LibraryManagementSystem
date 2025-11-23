package librarySE.utils;

import org.junit.jupiter.api.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class LoggerUtilsTest {

    private static Path tempDir;

    @BeforeAll
    static void setup() throws Exception {
        tempDir = Files.createTempDirectory("logtest");

        // Override LOG_DIR using reflection
        Field logDirField = LoggerUtils.class.getDeclaredField("LOG_DIR");
        logDirField.setAccessible(true);
        logDirField.set(null, tempDir); 
    }

    @Test
    void log_createsFileAndWritesContent() throws Exception {
        String file = "test_log.txt";
        LoggerUtils.log(file, "Hello World");

        Path logFile = tempDir.resolve(file);
        assertTrue(Files.exists(logFile));

        String content = Files.readString(logFile);
        assertTrue(content.contains("Hello World"));
    }
}

