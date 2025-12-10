package librarySE.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import librarySE.core.Book;
import librarySE.core.CD;
import librarySE.core.Journal;
import librarySE.core.LibraryItem;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Utility class for managing file storage using JSON format.
 *
 * <p>This class provides methods for reading and writing data objects as JSON
 * files inside the {@code library_data} directory. It also automatically
 * creates a timestamped backup copy of the old file before overwriting it.</p>
 *
 * <h3>Main Features</h3>
 * <ul>
 *     <li>Automatic directory creation under {@code library_data/}</li>
 *     <li>Safe read/write using Google Gson</li>
 *     <li>Automatic backup before writing new data</li>
 *     <li>Generic support for any data type (List, Map, Object...)</li>
 *     <li>Polymorphic support for {@link LibraryItem} (Book/CD/Journal)
 *         via a discriminator field</li>
 *     <li>Custom adapters for {@link LocalDate} and {@link LocalDateTime}
 *         to avoid reflection-access issues</li>
 * </ul>
 *
 * <p>Example JSON for a {@link LibraryItem}:</p>
 * <pre>{@code
 * {
 *   "type": "BOOK",
 *   "isbn": "123",
 *   "title": "Clean Code",
 *   ...
 * }
 * }</pre>
 *
 * @author Eman
 */
public final class FileUtils {

    /**
     * Formatter used for storing {@link LocalDateTime} as a String.
     * Uses ISO_LOCAL_DATE_TIME (e.g. 2025-12-09T22:15:30).
     */
    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Formatter used for storing {@link LocalDate} as a String.
     * Uses ISO_LOCAL_DATE (e.g. 2025-12-09).
     */
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Shared Gson instance with:
     * <ul>
     *     <li>Pretty printing</li>
     *     <li>Polymorphic handling for {@link LibraryItem}</li>
     *     <li>Custom adapters for {@link LocalDate} and {@link LocalDateTime}</li>
     * </ul>
     */
    private static final Gson GSON;

    /** Main directory where all data files are stored. */
    private static final Path DATA_DIR = Paths.get("library_data");

    // =====================================================================
    // Static initialization
    // =====================================================================
    static {
        try {
            // Ensure main data directory exists
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
            }

            // 1) Polymorphic type adapter for LibraryItem (Book / CD / Journal)
            RuntimeTypeAdapterFactory<LibraryItem> itemFactory =
                    RuntimeTypeAdapterFactory
                            .of(LibraryItem.class, "type")
                            .registerSubtype(Book.class, "BOOK")
                            .registerSubtype(CD.class, "CD")
                            .registerSubtype(Journal.class, "JOURNAL");

            // 2) Time adapters (LocalDate / LocalDateTime)

            // Adapter for LocalDateTime → stored as ISO string
            TypeAdapter<LocalDateTime> localDateTimeAdapter =
                    new TypeAdapter<>() {
                        @Override
                        public void write(JsonWriter out, LocalDateTime value)
                                throws IOException {
                            if (value == null) {
                                out.nullValue();
                            } else {
                                out.value(value.format(DATE_TIME_FMT));
                            }
                        }

                        @Override
                        public LocalDateTime read(JsonReader in)
                                throws IOException {
                            if (in.peek() == JsonToken.NULL) {
                                in.nextNull();
                                return null;
                            }
                            String text = in.nextString();
                            return LocalDateTime.parse(text, DATE_TIME_FMT);
                        }
                    };

            // Adapter for LocalDate → stored as ISO string
            TypeAdapter<LocalDate> localDateAdapter =
                    new TypeAdapter<>() {
                        @Override
                        public void write(JsonWriter out, LocalDate value)
                                throws IOException {
                            if (value == null) {
                                out.nullValue();
                            } else {
                                out.value(value.format(DATE_FMT));
                            }
                        }

                        @Override
                        public LocalDate read(JsonReader in)
                                throws IOException {
                            if (in.peek() == JsonToken.NULL) {
                                in.nextNull();
                                return null;
                            }
                            String text = in.nextString();
                            return LocalDate.parse(text, DATE_FMT);
                        }
                    };

            // 3) Build the configured Gson instance
            GSON = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapterFactory(itemFactory)
                    .registerTypeAdapter(LocalDateTime.class, localDateTimeAdapter)
                    .registerTypeAdapter(LocalDate.class, localDateAdapter)
                    .create();

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize data directory", e);
        }
    }

    /** Private constructor to prevent instantiation. */
    private FileUtils() {}

    // =====================================================================
    // Core JSON Read/Write with Backup
    // =====================================================================

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
            // Ensure backup folder exists
            Path backupDir = DATA_DIR.resolve("backups");
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            // If old file exists → create a backup copy
            if (Files.exists(file)) {
                String timestamp = LocalDateTime.now()
                        .toString().replace(":", "-");
                String backupName = file.getFileName().toString()
                        .replace(".json", "_backup_" + timestamp + ".json");
                Path backupFile = backupDir.resolve(backupName);
                Files.copy(file, backupFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Write the new data to file (overwrite)
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(obj, writer);
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to write JSON with backup: " + file, e);
        }
    }

    /**
     * Reads and deserializes a JSON file into an object or collection.
     * If the file is missing, returns the provided default value.
     *
     * <p>For polymorphic types such as {@link LibraryItem}, this method
     * relies on the registered {@link RuntimeTypeAdapterFactory} and the
     * {@code "type"} discriminator field embedded in the JSON to reconstruct
     * the correct subclass.</p>
     *
     * @param file         the JSON file path
     * @param type         the target type to deserialize into
     * @param defaultValue value returned if the file does not exist or
     *                     deserialization fails
     * @param <T>          the return type
     * @return the deserialized object, or {@code defaultValue} if not found
     *         or if parsing failed
     * @throws RuntimeException if file I/O fails
     */
    public static <T> T readJson(Path file, Type type, T defaultValue) {
        if (!Files.exists(file)) {
            return defaultValue;
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            T result = GSON.fromJson(reader, type);
            return (result != null) ? result : defaultValue;

        } catch (JsonIOException | JsonSyntaxException e) {
            // For invalid JSON / polymorphic issues → log and return default
            System.err.println(
                    "⚠️ Failed to parse JSON from " + file
                            + " → using default value. Reason: " + e.getMessage());
            return defaultValue;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON: " + file, e);
        }
    }

    /**
     * Returns a path inside {@code library_data/} for a given file name.
     *
     * <p>Example: {@code FileUtils.dataFile("users.json")}
     * → {@code library_data/users.json}</p>
     *
     * @param name the file name
     * @return the resolved {@link Path}
     */
    public static Path dataFile(String name) {
        return DATA_DIR.resolve(name);
    }

    /**
     * Creates a {@link Type} token for a list of a given class.
     *
     * <p>Example:</p>
     * <pre>{@code
     * Type userListType = FileUtils.listTypeOf(User.class);
     * }</pre>
     *
     * @param clazz the element type class
     * @return a parameterized list type (e.g., {@code List<User>})
     */
    public static Type listTypeOf(Class<?> clazz) {
        return TypeToken.getParameterized(List.class, clazz).getType();
    }

    // =====================================================================
    // RuntimeTypeAdapterFactory (polymorphic support)
    // =====================================================================

    /**
     * A generic factory that allows Gson to (de)serialize a base type and its
     * registered subtypes using a type-discriminating field.
     *
     * <p>JSON produced by this factory will include a field (for example
     * {@code "type"}) whose value indicates which concrete subtype should
     * be used on read.</p>
     *
     * @param <T> the base type
     */
    public static final class RuntimeTypeAdapterFactory<T>
            implements TypeAdapterFactory {

        private final Class<?> baseType;
        private final String typeFieldName;
        private final Map<String, Class<?>> labelToSubtype =
                new LinkedHashMap<>();
        private final Map<Class<?>, String> subtypeToLabel =
                new LinkedHashMap<>();

        private RuntimeTypeAdapterFactory(Class<?> baseType,
                                          String typeFieldName) {
            if (baseType == null) {
                throw new NullPointerException("Base type must not be null");
            }
            if (typeFieldName == null || typeFieldName.isBlank()) {
                throw new IllegalArgumentException(
                        "Type field name must not be null or blank");
            }
            this.baseType = baseType;
            this.typeFieldName = typeFieldName;
        }

        /**
         * Creates a new factory for the given base type, using the provided
         * JSON field name to store the subtype label.
         *
         * @param baseType      the base class or interface
         * @param typeFieldName the JSON field name used to store the type label
         * @param <T>           the base type
         * @return a new {@code RuntimeTypeAdapterFactory}
         */
        public static <T> RuntimeTypeAdapterFactory<T> of(
                Class<T> baseType, String typeFieldName) {
            return new RuntimeTypeAdapterFactory<>(baseType, typeFieldName);
        }

        /**
         * Registers a concrete subtype with a specific string label.
         *
         * @param subtype the concrete subclass
         * @param label   the discriminator label written into JSON
         * @return this factory for chaining
         */
        public RuntimeTypeAdapterFactory<T> registerSubtype(
                Class<? extends T> subtype, String label) {

            if (subtype == null) {
                throw new NullPointerException("Subtype must not be null");
            }
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException(
                        "Label must not be null or blank");
            }
            if (subtypeToLabel.containsKey(subtype)
                    || labelToSubtype.containsKey(label)) {
                throw new IllegalArgumentException(
                        "Subtype or label already registered: "
                                + subtype + " / " + label);
            }
            labelToSubtype.put(label, subtype);
            subtypeToLabel.put(subtype, label);
            return this;
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
            Class<?> rawType = type.getRawType();
            if (!baseType.isAssignableFrom(rawType)) {
                // This factory does not handle this type
                return null;
            }

            // Prepare delegate adapters for each registered subtype
            final Map<String, TypeAdapter<?>> labelToDelegate =
                    new LinkedHashMap<>();
            final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate =
                    new LinkedHashMap<>();

            for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
                TypeToken<?> subtypeToken = TypeToken.get(entry.getValue());
                TypeAdapter<?> delegate =
                        gson.getDelegateAdapter(this, subtypeToken);
                labelToDelegate.put(entry.getKey(), delegate);
                subtypeToDelegate.put(entry.getValue(), delegate);
            }

            final Gson context = gson;

            return new TypeAdapter<>() {

                @Override
                public void write(JsonWriter out, R value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                        return;
                    }

                    Class<?> srcClass = value.getClass();
                    String label = subtypeToLabel.get(srcClass);
                    TypeAdapter delegate = subtypeToDelegate.get(srcClass);

                    if (delegate == null || label == null) {
                        throw new JsonParseException(
                                "Unregistered subtype: "
                                        + srcClass.getName());
                    }

                    JsonElement element = delegate.toJsonTree(value);
                    if (!element.isJsonObject()) {
                        throw new JsonParseException(
                                "Expected JsonObject for subtype: "
                                        + srcClass.getName());
                    }

                    JsonObject original = element.getAsJsonObject();
                    JsonObject withType = new JsonObject();
                    withType.addProperty(typeFieldName, label);

                    for (Map.Entry<String, JsonElement> e
                            : original.entrySet()) {
                        withType.add(e.getKey(), e.getValue());
                    }

                    context.toJson(withType, out);
                }

                @Override
                public R read(JsonReader in) throws IOException {
                    JsonElement element = JsonParser.parseReader(in);
                    if (element.isJsonNull()) {
                        return null;
                    }

                    JsonObject obj = element.getAsJsonObject();
                    JsonElement typeElem = obj.remove(typeFieldName);
                    if (typeElem == null) {
                        throw new JsonParseException(
                                "Missing type field \"" + typeFieldName
                                        + "\" in JSON: " + obj);
                    }

                    String label = typeElem.getAsString();
                    TypeAdapter<?> delegate = labelToDelegate.get(label);
                    if (delegate == null) {
                        throw new JsonParseException(
                                "Unknown type label: " + label);
                    }

                    // Safe because we control the registrations
                    return (R) delegate.fromJsonTree(obj);
                }
            };
        }
    }
}
