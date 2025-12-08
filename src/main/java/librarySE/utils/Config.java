package librarySE.utils;


import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Provides centralized configuration management for the library system.
 * <p>
 * The {@code Config} class loads, reads, and writes key-value settings from a
 * single configuration file located at <b>data/config/fine-config.properties</b>.
 * It automatically creates the configuration file with default values if it does not exist.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *     <li>Automatic creation of configuration directories and default file.</li>
 *     <li>Support for retrieving {@code String}, {@code int}, {@code double}, and {@code boolean} values.</li>
 *     <li>Ability to update configuration values and reload them at runtime.</li>
 *     <li>Thread-safe operations for reading and writing.</li>
 * </ul>
 * <p>
 * This class is declared {@code final} and cannot be instantiated.
 * All methods and properties are static for global access.
 * </p>
 *
 * @author Malak
 */
public final class Config {

    /** Stores all configuration key-value pairs loaded from file. */
    private static final Properties PROPS = new Properties();

    /** Directory that contains the configuration files. */
    private static final Path CONFIG_DIR = Paths.get("data", "config");

    /** Main configuration file that holds fine and system settings. */
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("fine-config.properties");
    

    // --- Static initialization block ---
    static {
        try {
            
            if (!Files.exists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);

            
            if (!Files.exists(getConfigFile())) {
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
                Files.writeString(getConfigFile(), defaults);
            }
           
            reload();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    /** Private constructor to prevent instantiation. */
    private Config() {}

    

    /**
     * Reloads all configuration values from disk.
     * <p>
     * Useful when the configuration file is modified manually or by another process.
     * </p>
     */
    public static synchronized void reload() {
        try (FileInputStream in = new FileInputStream(getConfigFile().toFile())) {
            PROPS.clear();
            PROPS.load(in);
        } catch (IOException e) {
            System.err.println("⚠️ Failed to reload configuration: " + e.getMessage());
        }
    }

    /**
     * Retrieves a configuration value as a raw string.
     *
     * @param key the configuration key (e.g. "fine.book.rate")
     * @param def the default value if key is missing
     * @return the configuration value or the default if not found
     */
    public static String get(String key, String def) {
        return PROPS.getProperty(key, def);
    }

    /**
     * Retrieves a configuration value as an integer.
     *
     * @param key the configuration key
     * @param def default value if key not found or invalid
     * @return parsed integer value, or default if parsing fails
     */
    public static int getInt(String key, int def) {
        try { return Integer.parseInt(get(key, String.valueOf(def))); }
        catch (Exception ignored) { return def; }
    }

    /**
     * Retrieves a configuration value as a double.
     *
     * @param key the configuration key
     * @param def default value if key not found or invalid
     * @return parsed double value, or default if parsing fails
     */
    public static double getDouble(String key, double def) {
        try { return Double.parseDouble(get(key, String.valueOf(def))); }
        catch (Exception ignored) { return def; }
    }

    /**
     * Retrieves a configuration value as a boolean.
     * <p>
     * Accepts "true", "1", or "yes" (case-insensitive) as true values.
     * </p>
     *
     * @param key the configuration key
     * @param def default value if key not found
     * @return boolean interpretation of the configuration value
     */
    public static boolean getBoolean(String key, boolean def) {
        String val = get(key, String.valueOf(def));
        return val.equalsIgnoreCase("true") || val.equals("1") || val.equalsIgnoreCase("yes");
    }

    /**
     * Saves or updates a configuration key-value pair on disk.
     * <p>
     * If the key already exists, it will be overwritten.
     * Automatically appends a timestamp comment to the properties file.
     * </p>
     *
     * @param key   the property name
     * @param value the new value to set
     */
    public static synchronized void set(String key, String value) {
        PROPS.setProperty(key, value);
        try (OutputStream out = Files.newOutputStream(getConfigFile())) {
            PROPS.store(out, "Updated " + key + " at " + java.time.LocalDateTime.now());
        } catch (IOException e) {
            System.err.println("⚠️ Failed to save config: " + e.getMessage());
        }
    }

	/**
	 * @return the configFile
	 */
	public static Path getConfigFile() {
		return CONFIG_FILE;
	}
}
