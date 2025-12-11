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
 * Central JSON persistence utility for the Library Management System.
 *
 * <p>This class provides:</p>
 * <ul>
 *     <li>Reading & writing JSON files safely using Gson</li>
 *     <li>Automatic backup of files before overwriting</li>
 *     <li>Polymorphic serialization/deserialization of {@link LibraryItem}</li>
 *     <li>Custom serializers for {@link LocalDate} and {@link LocalDateTime}</li>
 *     <li>Helpers for obtaining data paths and typed list definitions</li>
 * </ul>
 *
 * <p>All methods are static — this class cannot be instantiated.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * Path file = FileUtils.dataFile("items.json");
 * List<LibraryItem> items = FileUtils.readJson(file, typeToken, new ArrayList<>());
 * FileUtils.writeJson(file, items);
 * }</pre>
 *
 * @author Eman
 */
public final class FileUtils {

    /** Formatter for ISO-8601 LocalDateTime representation. */
    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** Formatter for ISO-8601 LocalDate representation. */
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Shared Gson instance configured with:
     * <ul>
     *     <li>Pretty printing</li>
     *     <li>Polymorphic adapter for LibraryItem</li>
     *     <li>Custom date/time adapters</li>
     * </ul>
     */
    private static final Gson GSON;

    /** Root directory for all stored JSON files. */
    private static final Path DATA_DIR = Paths.get("library_data");

    // =====================================================================
    // Static Initialization
    // =====================================================================

    /**
     * Static initializer that sets up Gson and ensures that
     * the data directory exists before use.
     */
    static {
        GSON = buildGson();
    }

    /**
     * Builds the primary configured Gson instance.
     *
     * @return configured Gson instance
     * @throws RuntimeException if data directory cannot be initialized
     */
    private static Gson buildGson() {
        try {
            ensureDataDirExists();

            // Polymorphic adapter for LibraryItem
            RuntimeTypeAdapterFactory<LibraryItem> itemFactory =
                    RuntimeTypeAdapterFactory
                            .of(LibraryItem.class, "type")
                            .registerSubtype(Book.class, "BOOK")
                            .registerSubtype(CD.class, "CD")
                            .registerSubtype(Journal.class, "JOURNAL");

            TypeAdapter<LocalDateTime> localDateTimeAdapter = createLocalDateTimeAdapter();
            TypeAdapter<LocalDate> localDateAdapter = createLocalDateAdapter();

            return new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapterFactory(itemFactory)
                    .registerTypeAdapter(LocalDateTime.class, localDateTimeAdapter)
                    .registerTypeAdapter(LocalDate.class, localDateAdapter)
                    .create();

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize data directory", e);
        }
    }

    /**
     * Ensures the root data directory exists.
     *
     * @throws IOException if creation fails
     */
    private static void ensureDataDirExists() throws IOException {
        if (!Files.exists(DATA_DIR)) {
            Files.createDirectories(DATA_DIR);
        }
    }

    /**
     * Gson adapter for {@link LocalDateTime}.
     *
     * @return a custom type adapter
     */
    private static TypeAdapter<LocalDateTime> createLocalDateTimeAdapter() {
        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, LocalDateTime value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                out.value(value.format(DATE_TIME_FMT));
            }

            @Override
            public LocalDateTime read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                return LocalDateTime.parse(in.nextString(), DATE_TIME_FMT);
            }
        };
    }

    /**
     * Gson adapter for {@link LocalDate}.
     *
     * @return a custom type adapter
     */
    private static TypeAdapter<LocalDate> createLocalDateAdapter() {
        return new TypeAdapter<>() {

            @Override
            public void write(JsonWriter out, LocalDate value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                out.value(value.format(DATE_FMT));
            }

            @Override
            public LocalDate read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                return LocalDate.parse(in.nextString(), DATE_FMT);
            }
        };
    }

    /** Private constructor — FileUtils cannot be instantiated. */
    private FileUtils() {}

    // =====================================================================
    // JSON Write with Backup
    // =====================================================================

    /**
     * Writes an object to a JSON file.
     * If the file exists, a timestamped backup is created before overwriting.
     *
     * @param file the destination path
     * @param obj  any serializable object
     * @param <T>  object type
     * @throws RuntimeException if writing or backup fails
     */
    public static <T> void writeJson(Path file, T obj) {
        try {
            Path backupDir = ensureBackupDirExists();
            backupExistingFileIfNeeded(file, backupDir);
            writeJsonToFile(file, obj);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON with backup: " + file, e);
        }
    }

    /**
     * Ensures backup directory exists.
     *
     * @return backup directory path
     * @throws IOException if creation fails
     */
    private static Path ensureBackupDirExists() throws IOException {
        Path backupDir = DATA_DIR.resolve("backups");
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
        }
        return backupDir;
    }

    /**
     * Creates a timestamped backup if the target file already exists.
     *
     * @param file      existing file
     * @param backupDir destination backup directory
     * @throws IOException if backup fails
     */
    private static void backupExistingFileIfNeeded(Path file, Path backupDir) throws IOException {
        if (!Files.exists(file)) return;

        String timestamp = LocalDateTime.now().toString().replace(":", "-");
        String backupName = file.getFileName().toString()
                .replace(".json", "_backup_" + timestamp + ".json");

        Files.copy(file, backupDir.resolve(backupName), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Writes JSON to the specified file using the shared Gson instance.
     *
     * @param file output file
     * @param obj  data to write
     * @param <T>  type of data
     * @throws IOException if writing fails
     */
    private static <T> void writeJsonToFile(Path file, T obj) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(obj, writer);
        }
    }

    // =====================================================================
    // JSON Read
    // =====================================================================

    /**
     * Reads and deserializes JSON from a file.
     *
     * <p>If the file does not exist or JSON is invalid, returns the provided default value.</p>
     *
     * @param file         JSON path
     * @param type         expected type token
     * @param defaultValue return value if missing/invalid
     * @param <T>          return type
     * @return parsed object or default value
     */
    public static <T> T readJson(Path file, Type type, T defaultValue) {
        if (!Files.exists(file)) {
            return defaultValue;
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            T result = GSON.fromJson(reader, type);
            return (result != null) ? result : defaultValue;

        } catch (JsonIOException | JsonSyntaxException e) {
            return defaultValue;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON: " + file, e);
        }
    }

    // =====================================================================
    // Path & Type Utilities
    // =====================================================================

    /**
     * Resolves a file inside the data directory.
     *
     * @param name file name
     * @return resolved path
     */
    public static Path dataFile(String name) {
        return DATA_DIR.resolve(name);
    }

    /**
     * Creates a generic List<T> type token.
     *
     * @param clazz element class
     * @return List<T> type token
     */
    public static Type listTypeOf(Class<?> clazz) {
        return TypeToken.getParameterized(List.class, clazz).getType();
    }

    // =====================================================================
    // RuntimeTypeAdapterFactory — polymorphic Gson support
    // =====================================================================

    /**
     * Polymorphic adapter factory enabling Gson to serialize and deserialize
     * abstract types (like {@link LibraryItem}).
     *
     * <p>It inserts a discriminator field (e.g., <code>"type"</code>) into JSON
     * to identify the actual subtype.</p>
     *
     * @param <T> base type
     */
    public static final class RuntimeTypeAdapterFactory<T>
            implements TypeAdapterFactory {

        private final Class<?> baseType;
        private final String typeFieldName;
        private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<>();
        private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<>();

        private RuntimeTypeAdapterFactory(Class<?> baseType, String typeFieldName) {
            if (baseType == null)
                throw new NullPointerException("Base type must not be null");
            if (typeFieldName == null || typeFieldName.isBlank())
                throw new IllegalArgumentException("Type field name must not be null or blank");

            this.baseType = baseType;
            this.typeFieldName = typeFieldName;
        }

        /**
         * Factory builder.
         *
         * @param baseType      abstract base class/interface
         * @param typeFieldName name of JSON discriminator field
         * @param <T>           base type
         * @return new adapter factory
         */
        public static <T> RuntimeTypeAdapterFactory<T> of(
                Class<T> baseType, String typeFieldName) {
            return new RuntimeTypeAdapterFactory<>(baseType, typeFieldName);
        }

        /**
         * Registers a subtype + label pair.
         *
         * @param subtype concrete subclass
         * @param label   discriminator value
         * @return this factory
         */
        public RuntimeTypeAdapterFactory<T> registerSubtype(
                Class<? extends T> subtype, String label) {

            if (subtype == null)
                throw new NullPointerException("Subtype must not be null");
            if (label == null || label.isBlank())
                throw new IllegalArgumentException("Label must not be null or blank");

            if (subtypeToLabel.containsKey(subtype) ||
                labelToSubtype.containsKey(label)) {
                throw new IllegalArgumentException("Subtype or label already registered");
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
                return null; // Unsupported type
            }

            Map<String, TypeAdapter<?>> labelToDelegate = new LinkedHashMap<>();
            Map<Class<?>, TypeAdapter<?>> subtypeToDelegate = new LinkedHashMap<>();

            for (var entry : labelToSubtype.entrySet()) {
                TypeAdapter<?> delegate =
                        gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
                labelToDelegate.put(entry.getKey(), delegate);
                subtypeToDelegate.put(entry.getValue(), delegate);
            }

            return new PolymorphicTypeAdapter<>(
                    gson,
                    labelToDelegate,
                    subtypeToDelegate,
                    subtypeToLabel,
                    typeFieldName
            );
        }

        /**
         * TypeAdapter that performs actual polymorphic read/write logic.
         *
         * @param <R> resolved runtime type
         */
        private static final class PolymorphicTypeAdapter<R> extends TypeAdapter<R> {

            private final Gson context;
            private final Map<String, TypeAdapter<?>> labelToDelegate;
            private final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate;
            private final Map<Class<?>, String> subtypeToLabel;
            private final String typeFieldName;

            PolymorphicTypeAdapter(
                    Gson context,
                    Map<String, TypeAdapter<?>> labelToDelegate,
                    Map<Class<?>, TypeAdapter<?>> subtypeToDelegate,
                    Map<Class<?>, String> subtypeToLabel,
                    String typeFieldName) {

                this.context = context;
                this.labelToDelegate = labelToDelegate;
                this.subtypeToDelegate = subtypeToDelegate;
                this.subtypeToLabel = subtypeToLabel;
                this.typeFieldName = typeFieldName;
            }

            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public void write(JsonWriter out, R value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }

                Class<?> clazz = value.getClass();
                String label = subtypeToLabel.get(clazz);
                TypeAdapter delegate = subtypeToDelegate.get(clazz);

                if (delegate == null || label == null) {
                    throw new JsonParseException("Unregistered subtype: " + clazz.getName());
                }

                JsonObject tree = delegate.toJsonTree(value).getAsJsonObject();
                JsonObject finalObj = new JsonObject();
                finalObj.addProperty(typeFieldName, label);

                for (var entry : tree.entrySet()) {
                    finalObj.add(entry.getKey(), entry.getValue());
                }

                context.toJson(finalObj, out);
            }

            @Override
            @SuppressWarnings("unchecked")
            public R read(JsonReader in) throws IOException {
                JsonElement element = JsonParser.parseReader(in);
                if (element.isJsonNull()) return null;

                JsonObject obj = element.getAsJsonObject();
                JsonElement typeElem = obj.remove(typeFieldName);

                if (typeElem == null) {
                    throw new JsonParseException(
                            "Missing type field \"" + typeFieldName + "\"");
                }

                String label = typeElem.getAsString();
                TypeAdapter<?> delegate = labelToDelegate.get(label);

                if (delegate == null) {
                    throw new JsonParseException("Unknown subtype: " + label);
                }

                return (R) delegate.fromJsonTree(obj);
            }
        }
    }
}
