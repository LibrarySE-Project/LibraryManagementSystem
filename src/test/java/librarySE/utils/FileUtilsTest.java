package librarySE.utils;

import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    private Path tempDir;
    private Path jsonFile;

    @BeforeEach
    void setup() throws Exception {
        tempDir = Files.createTempDirectory("fileutils");
        jsonFile = tempDir.resolve("test.json");
    }

    @Test
    void writeJson_createsFile() {
        List<String> data = List.of("a", "b");

        FileUtils.writeJson(jsonFile, data);

        assertTrue(Files.exists(jsonFile));
    }

    @Test
    void readJson_readsDataCorrectly() {
        List<String> data = List.of("hello", "world");

        FileUtils.writeJson(jsonFile, data);

        List<String> result = FileUtils.readJson(jsonFile, FileUtils.listTypeOf(String.class), List.of());

        assertEquals(2, result.size());
        assertEquals("hello", result.get(0));
    }

    @Test
    void readJson_returnsDefaultIfNotExists() {
        Path missing = tempDir.resolve("missing.json");

        List<String> result = FileUtils.readJson(missing, FileUtils.listTypeOf(String.class), List.of("x"));

        assertEquals(1, result.size());
        assertEquals("x", result.get(0));
    }
}

