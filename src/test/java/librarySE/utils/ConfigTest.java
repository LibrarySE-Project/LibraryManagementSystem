package librarySE.utils;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    /** Keep original CONFIG_FILE so we can restore it after each error test. */
    private static Path originalConfigFile;

    @BeforeAll
    static void resetConfigDirAndCaptureOriginalField() throws Exception {
        // Delete data/config so that static block in Config executes fully
        //    (creates directory + default config file) during this test run.
        Path configDir = Paths.get("data", "config");
        deleteRecursively(configDir);

        // Touch Config class now (after deletion) so static block runs
        //    with a clean filesystem.
        Class<?> cfgClass = Config.class; // force class loading

        // Capture original CONFIG_FILE value for later restoration
        Field f = Config.class.getDeclaredField("CONFIG_FILE");
        f.setAccessible(true);
        originalConfigFile = (Path) f.get(null);
    }

    @AfterEach
    void restoreOriginalConfigFile() throws Exception {
        // Restore CONFIG_FILE after tests that modify it via reflection.
        if (originalConfigFile != null) {
            Field f = Config.class.getDeclaredField("CONFIG_FILE");
            f.setAccessible(true);
            f.set(null, originalConfigFile);
        }
    }

    // Helper to delete directory recursively (used before class is loaded).
    private static void deleteRecursively(Path root) throws IOException {
        if (root == null || !Files.exists(root)) return;
        Files.walk(root)
                .sorted((a, b) -> b.compareTo(a)) // delete children first
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
    }

    // Happy-path tests (normal usage)
    @Test
    void testDefaultsFileCreatedAndLoaded() {
        Path cfgFile = Config.getConfigFile();
        assertTrue(Files.exists(cfgFile), "Config file should exist after static init");

        // Default values from the string in the static block
        assertEquals(10, Config.getInt("fine.book.rate", 0));
        assertEquals(28, Config.getInt("fine.book.period", 0));
        assertEquals(20, Config.getInt("fine.cd.rate", 0));
        assertTrue(Config.getBoolean("notifications.enabled", false));
    }

    @Test
    void testGetStringFallsBackToDefault() {
        String val = Config.get("non.existing.key", "fallback");
        assertEquals("fallback", val);
    }

    @Test
    void testGetIntFallsBackToDefaultOnInvalid() {
        Config.set("test.int.bad", "notANumber");
        int v = Config.getInt("test.int.bad", 42);
        assertEquals(42, v);
    }

    @Test
    void testGetDoubleFallsBackToDefaultOnInvalid() {
        Config.set("test.double.bad", "oops");
        double v = Config.getDouble("test.double.bad", 3.14);
        assertEquals(3.14, v, 0.0001);
    }

    @Test
    void testGetBooleanParsesCommonTrueValues() {
        Config.set("b.true1", "true");
        Config.set("b.true2", "1");
        Config.set("b.true3", "yes");

        assertTrue(Config.getBoolean("b.true1", false));
        assertTrue(Config.getBoolean("b.true2", false));
        assertTrue(Config.getBoolean("b.true3", false));
    }

    @Test
    void testSetPersistsAndReloadReads() throws IOException {
        Config.set("custom.key", "12345");

        assertEquals("12345", Config.get("custom.key", "0"));

        // Reload again from disk and check the value is still there
        Config.reload();
        assertEquals("12345", Config.get("custom.key", "0"));
    }

    // Error-path tests: cover catch blocks in reload() and set()
    @Test
    void testReloadHandlesIOExceptionGracefully() throws Exception {
        // Point CONFIG_FILE to a non-existing file in a non-existing directory
        Path tempRoot = Files.createTempDirectory("cfgReloadErr");
        Path bogusDir = tempRoot.resolve("no_such_dir");
        Path bogusFile = bogusDir.resolve("fine-config.properties");

        Field f = Config.class.getDeclaredField("CONFIG_FILE");
        f.setAccessible(true);
        f.set(null, bogusFile);

        // This should NOT throw, but it will go into the catch block
        assertDoesNotThrow(Config::reload);
    }

    @Test
    void testSetHandlesIOExceptionGracefully() throws Exception {
        // Parent directory does not exist -> Files.newOutputStream will fail
        Path tempRoot = Files.createTempDirectory("cfgSetErr");
        Path bogusDir = tempRoot.resolve("no_such_dir");
        Path bogusFile = bogusDir.resolve("fine-config.properties");

        Field f = Config.class.getDeclaredField("CONFIG_FILE");
        f.setAccessible(true);
        f.set(null, bogusFile);

        // This should NOT throw, but will execute the catch block in set()
        assertDoesNotThrow(() -> Config.set("k", "v"));
    }
    @Test
    void testStaticBlockCreatesDefaultConfigWhenFileMissing() throws Exception {
        // 1) Delete current config file (if it exists)
        Path cfg = Config.getConfigFile();
        Files.deleteIfExists(cfg);

        // 2) Reload Config in a fresh ClassLoader so its static block runs again
        URL classUrl = Config.class.getProtectionDomain()
                .getCodeSource().getLocation();

        try (URLClassLoader cl = new URLClassLoader(new URL[]{classUrl}, null)) {
            Class<?> reloaded = cl.loadClass("librarySE.utils.Config");

            // Force class initialization (triggers the static block)
            reloaded.getDeclaredMethods();

            // 3) Verify that the file was created with default content
            Path newCfg = (Path) reloaded.getMethod("getConfigFile").invoke(null);
            assertTrue(Files.exists(newCfg), "Config file should be created by static block");

            String content = Files.readString(newCfg);
            assertTrue(content.contains("fine.book.rate=10"));
            assertTrue(content.contains("notifications.enabled=true"));
        }
    }
    @Test
    void testGetBooleanRecognizesAllTrueValues() {
        // First branch: "true"
        Config.set("flag.true", "true");
        assertTrue(Config.getBoolean("flag.true", false));

        // Second branch: "1"
        Config.set("flag.one", "1");
        assertTrue(Config.getBoolean("flag.one", false));

        // Third branch: "yes"
        Config.set("flag.yes", "yes");
        assertTrue(Config.getBoolean("flag.yes", false));
    }

    @Test
    void testGetBooleanReturnsFalseForOtherValues() {
        Config.set("flag.other", "nope");
        assertFalse(Config.getBoolean("flag.other", false));
    }

    
}

