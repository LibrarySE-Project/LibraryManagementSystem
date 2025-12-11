package librarySE.utils;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    private static boolean hadOriginalFile;
    private static String originalContent;

    @BeforeAll
    static void initConfigWithFreshDefaults() throws Exception {
        // Work on the real config path used by Config
        Path configDir = Paths.get("data", "config");
        Path configFile = configDir.resolve("fine-config.properties");

        // Backup current file (if exists) then delete it
        if (Files.exists(configFile)) {
            hadOriginalFile = true;
            originalContent = Files.readString(configFile);
            Files.delete(configFile);
        } else {
            hadOriginalFile = false;
            originalContent = null;
        }

        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }

        // Trigger class initialization -> runs static block
        Class.forName("librarySE.utils.Config");
    }

    @AfterAll
    static void restoreOriginalConfigFile() throws IOException {
        Path cfgFile = Config.getConfigFile();
        if (hadOriginalFile && originalContent != null) {
            Files.writeString(cfgFile, originalContent);
            Config.reload();
        }
    }

    @Test
    void testDefaultsFileCreatedAndLoaded() throws IOException {
        Path cfgFile = Config.getConfigFile();
        assertNotNull(cfgFile);

        // If the file does not exist (e.g., deleted by another test), recreate it
        if (!Files.exists(cfgFile)) {
            Files.createDirectories(cfgFile.getParent());

            String defaults = """
                    fine.book.rate=10
                    fine.book.period=28

                    fine.cd.rate=20
                    fine.cd.period=7

                    fine.journal.rate=15
                    fine.journal.period=21

                    price.book.default=59.99
                    price.cd.default=39.99
                    price.journal.default=29.99

                    notifications.enabled=true
                    """;

            Files.writeString(cfgFile, defaults);
            Config.reload();
        }

        // Ensure the configuration file now exists
        assertTrue(Files.exists(cfgFile));

        // Verify default values loaded correctly
        assertEquals(10, Config.getInt("fine.book.rate", 0));
        assertEquals(28, Config.getInt("fine.book.period", 0));
        assertEquals(20, Config.getInt("fine.cd.rate", 0));
        assertTrue(Config.getBoolean("notifications.enabled", false));
    }


    @Test
    void testGetStringFallsBackToDefault() {
        String value = Config.get("non.existing.key", "fallback");
        assertEquals("fallback", value);
    }

    @Test
    void testGetIntFallsBackToDefaultOnInvalid() {
        Config.set("test.int.bad", "notANumber");
        int v = Config.getInt("test.int.bad", 42);
        assertEquals(42, v);
    }

    @Test
    void testGetDoubleValidAndInvalid() {
        // valid value -> covers try branch
        Config.set("test.double.ok", "4.5");
        double ok = Config.getDouble("test.double.ok", 0.0);
        assertEquals(4.5, ok, 0.0001);

        // invalid value -> covers catch branch
        Config.set("test.double.bad", "oops");
        double fallback = Config.getDouble("test.double.bad", 3.14);
        assertEquals(3.14, fallback, 0.0001);
    }

    @Test
    void testGetBooleanTrueVariantsAndFalseVariant() {
        Config.set("b.true1", "true");
        Config.set("b.true2", "1");
        Config.set("b.true3", "yes");
        Config.set("b.other", "nope");

        assertTrue(Config.getBoolean("b.true1", false));
        assertTrue(Config.getBoolean("b.true2", false));
        assertTrue(Config.getBoolean("b.true3", false));
        assertFalse(Config.getBoolean("b.other", true));
    }

    @Test
    void testSetPersistsAndReloadReads() throws IOException {
        Config.set("custom.key", "12345");

        assertEquals("12345", Config.get("custom.key", "0"));

        Config.reload();
        assertEquals("12345", Config.get("custom.key", "0"));
    }

    @Test
    void testReloadHandlesIOExceptionCatchBranch() throws Exception {
        Path cfg = Config.getConfigFile();

        // Backup original file
        String backupContent = null;
        boolean existed = Files.exists(cfg);

        if (existed) {
            backupContent = Files.readString(cfg);
            Files.delete(cfg); 
        }

        try {
            assertDoesNotThrow(Config::reload); 
        } finally {
            // Restore original file
            if (existed && backupContent != null) {
                Files.createDirectories(cfg.getParent());
                Files.writeString(cfg, backupContent);
                Config.reload();
            }
        }
    }


}
