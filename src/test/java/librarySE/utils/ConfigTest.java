package librarySE.utils;

import org.junit.jupiter.api.*;

import java.nio.file.*;
import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConfigTest {

    private static Path tempDir;
    private static Path tempConfigFile;

    @BeforeAll
    static void setup() throws Exception {

        tempDir = Files.createTempDirectory("config_test");
        tempConfigFile = tempDir.resolve("fine-config.properties");

        Files.writeString(tempConfigFile, """
                fine.book.rate=10
                fine.book.period=20
                notifications.enabled=true
                double.ok=12.5
                """);

        Properties p = new Properties();
        try (InputStream in = new FileInputStream(tempConfigFile.toFile())) {
            p.load(in);
        }

        java.lang.reflect.Field propsField = Config.class.getDeclaredField("PROPS");
        propsField.setAccessible(true);
        Properties props = (Properties) propsField.get(null);
        props.clear();
        props.putAll(p);
    }

    // --------------------------------------------------------------------

    @Test @Order(1)
    void testGetString_existingAndDefault() {
        assertEquals("10", Config.get("fine.book.rate", "0"));
        assertEquals("unknown", Config.get("no.key", "unknown"));
    }

    @Test @Order(2)
    void testGetInt_validInvalidDefault() {
        assertEquals(10, Config.getInt("fine.book.rate", 0));
        assertEquals(5, Config.getInt("no.key", 5));

        java.lang.reflect.Field propsField;
        try {
            propsField = Config.class.getDeclaredField("PROPS");
            propsField.setAccessible(true);
            Properties props = (Properties) propsField.get(null);
            props.setProperty("corrupt.int", "abc");
        } catch (Exception e) { fail(e); }

        assertEquals(7, Config.getInt("corrupt.int", 7));
    }

    @Test @Order(3)
    void testGetDouble_validInvalidDefault() {
        assertEquals(12.5, Config.getDouble("double.ok", 0));
        assertEquals(1.1, Config.getDouble("bad.double", 1.1));
    }

    @Test @Order(4)
    void testGetBoolean_allAcceptedForms() {
        try {
            java.lang.reflect.Field propsField = Config.class.getDeclaredField("PROPS");
            propsField.setAccessible(true);
            Properties props = (Properties) propsField.get(null);

            props.setProperty("b1", "true");
            props.setProperty("b2", "1");
            props.setProperty("b3", "yes");
            props.setProperty("b4", "false");

        } catch (Exception e) { fail(e); }

        assertTrue(Config.getBoolean("b1", false));
        assertTrue(Config.getBoolean("b2", false));
        assertTrue(Config.getBoolean("b3", false));
        assertFalse(Config.getBoolean("b4", true));
    }

    @Test @Order(5)
    void testSet_updatesProps() throws Exception {
        Config.set("new.key", "555");

        assertEquals("555", Config.get("new.key", "0"));

        String fileContent = Files.readString(Config.getConfigFile());
        assertTrue(fileContent.contains("new.key=555"));
    }

    @Test @Order(6)
    void testReload_handlesCorruptedFileGracefully() throws Exception {
        Files.writeString(Config.getConfigFile(), "::: broken :::");

        assertDoesNotThrow(Config::reload);

        assertEquals("abc", Config.get("missing.key", "abc"));
    }
    @Test
    void testCreatesConfigDirAutomatically() throws Exception {
        Path tempDir = Files.createTempDirectory("cfg_test");
        Path configDir = tempDir.resolve("data/config");
        Path configFile = configDir.resolve("fine-config.properties");

        System.setProperty("user.dir", tempDir.toString());

        Config.set("example.key", "123");

        assertTrue(Files.exists(configDir));
        assertTrue(Files.exists(configFile));
    }

    @Test
    void testStaticInit_createsDefaultConfigFileWhenMissing() throws Exception {
        Path tempDir = Files.createTempDirectory("cfg_test2");
        Path configFile = tempDir.resolve("data/config/fine-config.properties");

        Files.createDirectories(configFile.getParent());
        Files.deleteIfExists(configFile);

        assertFalse(Files.exists(configFile));

        System.setProperty("user.dir", tempDir.toString());

        Config.reload(); 

        assertTrue(Files.exists(configFile), "Default config file should be created.");
    }
    @Test
    void testReload_handlesCorruptedFileGracefully0() throws Exception {
        Path tempDir = Files.createTempDirectory("cfg_test3");
        Path configFile = tempDir.resolve("data/config/fine-config.properties");

        Files.createDirectories(configFile.getParent());
        Files.writeString(configFile, ":::::INVALID:::::");  

        System.setProperty("user.dir", tempDir.toString());

        assertDoesNotThrow(Config::reload);
    }
    @Test
    void testGetInt_invalidValueReturnsDefault() {
        Config.set("bad.int", "abc");
        Config.reload();
        assertEquals(5, Config.getInt("bad.int", 5));
    }

    @Test
    void testGetDouble_invalidValueReturnsDefault() {
        Config.set("bad.double", "xyz");
        Config.reload();
        assertEquals(1.5, Config.getDouble("bad.double", 1.5));
    }

    @Test
    void testGetBoolean_allBranches() {
        Config.set("b1", "true");
        Config.set("b2", "1");
        Config.set("b3", "yes");
        Config.set("b4", "false");

        Config.reload();

        assertTrue(Config.getBoolean("b1", false));
        assertTrue(Config.getBoolean("b2", false));
        assertTrue(Config.getBoolean("b3", false));
        assertFalse(Config.getBoolean("b4", true));

        assertTrue(Config.getBoolean("missingKey", true));
    }
    @Test
    void testSet_ioErrorHandledGracefully() throws Exception {
        Path tempDir = Files.createTempDirectory("cfg_test4");
        Path configFile = tempDir.resolve("data/config/fine-config.properties");

        Files.createDirectories(configFile.getParent());
        Files.writeString(configFile, "x");

        configFile.toFile().setReadOnly();

        System.setProperty("user.dir", tempDir.toString());

        assertDoesNotThrow(() -> Config.set("key", "value"));
    }





}
