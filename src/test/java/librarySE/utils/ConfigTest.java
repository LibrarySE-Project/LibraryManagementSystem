package librarySE.utils;

import org.junit.jupiter.api.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    private static Path tempDir;
    private static Path tempFile;

    @BeforeAll
    static void setup() throws Exception {
        tempDir = Files.createTempDirectory("configtest");
        tempFile = tempDir.resolve("fine-config.properties");

        // Override CONFIG_DIR
        Field configDirField = Config.class.getDeclaredField("CONFIG_DIR");
        configDirField.setAccessible(true);
        configDirField.set(null, tempDir);

        // Override CONFIG_FILE
        Field configFileField = Config.class.getDeclaredField("CONFIG_FILE");
        configFileField.setAccessible(true);
        configFileField.set(null, tempFile);

        // Reload using new directory
        Config.reload();
    }

    @Test
    void setAndGetStringValue() {
        Config.set("test.key", "hello");
        String value = Config.get("test.key", "default");
        assertEquals("hello", value);
    }

    @Test
    void getInt_returnsCorrectValue() {
        Config.set("num", "42");
        assertEquals(42, Config.getInt("num", 0));
    }

    @Test
    void getDouble_returnsCorrectValue() {
        Config.set("price", "19.5");
        assertEquals(19.5, Config.getDouble("price", 0));
    }

    @Test
    void getBoolean_recognizesTrue() {
        Config.set("flag", "true");
        assertTrue(Config.getBoolean("flag", false));
    }
}

