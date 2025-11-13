package librarySE.utils;


import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Utility class for managing file storage using JSON format.
 * <p>
 * This class provides methods for reading and writing data objects as JSON files
 * inside the <b>library_data</b> directory. It also automatically creates
 * a timestamped backup copy of the old file before overwriting it.
 * </p>
 *
 * <h3>Main Features:</h3>
 * <ul>
 *     <li>Automatic directory creation under {@code library_data/}</li>
 *     <li>Safe read/write using Google Gson</li>
 *     <li>Automatic backup before writing new data</li>
 *     <li>Generic support for any data type (List, Map, Object...)</li>
 * </ul>
 *
 *
 * @author Malak
 */
public final class FileUtils {

    /** Shared Gson instance with pretty printing enabled. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Main directory where all data files are stored. */
    private static final Path DATA_DIR = Paths.get("library_data");

    static {
        try {
            if (!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize data directory", e);
        }
    }

    /** Private constructor to prevent instantiation. */
    private FileUtils() {}

    // ------------------------------------------------------------------------
    // Core JSON Read/Write with Backup
    // ------------------------------------------------------------------------

    /**
     * Writes the given object to a JSON file. If the file already exists,
     * a timestamped backup copy is saved before overwriting.
     *
     * @param file the target JSON file path
     * @param obj  the object or collection to serialize
     * @param <T>  the type of the object being written
     * @throws RuntimeException if writing or backup creation fails
     */
    public static <T> void writeJson(Path file, T obj) {
        try {
            // 1️⃣ Ensure backup folder exists
            Path backupDir = DATA_DIR.resolve("backups");
            if (!Files.exists(backupDir)) Files.createDirectories(backupDir);

            // 2️⃣ If old file exists → create a backup copy
            if (Files.exists(file)) {
                String timestamp = LocalDateTime.now().toString().replace(":", "-");
                String backupName = file.getFileName().toString()
                        .replace(".json", "_backup_" + timestamp + ".json");
                Path backupFile = backupDir.resolve(backupName);
                Files.copy(file, backupFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // 3️⃣ Write the new data to file (overwrite)
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(obj, writer);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON with backup: " + file, e);
        }
    }

    /**
     * Reads and deserializes a JSON file into an object or collection.
     * If the file is missing, returns the provided default value.
     *
     * @param file         the JSON file path
     * @param type         the type of object to deserialize into (use {@link #listTypeOf(Class)})
     * @param defaultValue the value returned if the file does not exist
     * @param <T>          the return type
     * @return the deserialized object, or {@code defaultValue} if not found
     * @throws RuntimeException if file reading fails
     */
    public static <T> T readJson(Path file, Type type, T defaultValue) {
        if (!Files.exists(file)) return defaultValue;
        try (Reader reader = Files.newBufferedReader(file)) {
            return GSON.fromJson(reader, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON: " + file, e);
        }
    }

    /**
     * Returns a path inside {@code library_data/} for a given file name.
     * <p>
     * Example: {@code FileUtils.dataFile("users.json")} → library_data/users.json
     * </p>
     *
     * @param name the file name
     * @return the resolved {@link Path}
     */
    public static Path dataFile(String name) {
        return DATA_DIR.resolve(name);
    }

    /**
     * Creates a {@link Type} token for a list of a given class.
     * <p>
     * Example usage:
     * <pre>{@code
     * Type userListType = FileUtils.listTypeOf(User.class);
     * }</pre>
     * </p>
     *
     * @param clazz the element type class
     * @return a parameterized list type (e.g., List&lt;User&gt;)
     */
    public static Type listTypeOf(Class<?> clazz) {
        return TypeToken.getParameterized(List.class, clazz).getType();
    }
}
