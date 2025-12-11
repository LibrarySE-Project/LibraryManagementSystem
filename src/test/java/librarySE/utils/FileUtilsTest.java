package librarySE.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import librarySE.core.Book;
import librarySE.core.CD;
import librarySE.core.Journal;
import librarySE.core.LibraryItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    private Path tempDir;
    private Path jsonFile;

    static class DateHolder {
        LocalDate date;
        LocalDateTime dateTime;
    }

    @BeforeEach
    void setup() throws Exception {
        tempDir = Files.createTempDirectory("fileutils_test");
        jsonFile = tempDir.resolve("test.json");
    }

    @Test
    void writeJson_createsFileInCustomDirectory() {
        List<String> data = List.of("a", "b");
        FileUtils.writeJson(jsonFile, data);
        assertTrue(Files.exists(jsonFile));
    }

    @Test
    void writeJson_createsBackupWhenFileAlreadyExists() throws Exception {
        Path file = FileUtils.dataFile("test_backup_file.json");
        if (Files.exists(file)) Files.delete(file);

        List<String> d1 = List.of("old");
        List<String> d2 = List.of("new");

        FileUtils.writeJson(file, d1);
        assertTrue(Files.exists(file));

        FileUtils.writeJson(file, d2);
        Path backup = FileUtils.dataFile("backups");

        long count = Files.list(backup)
                .filter(p -> p.getFileName().toString().startsWith("test_backup_file_backup"))
                .count();

        assertTrue(count >= 1);
    }

    @Test
    void writeJson_createsBackupWithCleanBackupDirectory() throws Exception {
        Path file = FileUtils.dataFile("test_backup.json");
        Path backupDir = FileUtils.dataFile("backups");

        if (Files.exists(backupDir)) {
            Files.list(backupDir).forEach(p -> {
                try { Files.delete(p); } catch (Exception ignored) {}
            });
        }

        FileUtils.writeJson(file, List.of("old"));
        FileUtils.writeJson(file, List.of("new"));

        long count = Files.list(backupDir)
                .filter(p -> p.getFileName().toString().startsWith("test_backup_backup_"))
                .count();

        assertTrue(count >= 1);
    }

    @Test
    void writeJson_throwsRuntimeExceptionOnIOFailure_customTempPath() {
        Path impossible = tempDir.resolve("file.txt").resolve("child.json");
        assertThrows(RuntimeException.class, () -> FileUtils.writeJson(impossible, List.of("x")));
    }

    @Test
    void writeJson_throwsRuntimeExceptionOnIOFailure_usingDataFile() {
        Path impossible = FileUtils.dataFile("abc.txt").resolve("child.json");
        assertThrows(RuntimeException.class, () -> FileUtils.writeJson(impossible, List.of("x")));
    }

    @Test
    void readJson_readsDataCorrectly() {
        List<String> data = List.of("hello", "world");
        FileUtils.writeJson(jsonFile, data);

        List<String> result = FileUtils.readJson(
                jsonFile,
                FileUtils.listTypeOf(String.class),
                List.of()
        );

        assertEquals(2, result.size());
        assertEquals("hello", result.get(0));
    }

    @Test
    void readJson_returnsDefaultIfFileMissing() {
        Path missing = tempDir.resolve("missing.json");
        List<String> fallback = List.of("x");

        List<String> result = FileUtils.readJson(
                missing,
                FileUtils.listTypeOf(String.class),
                fallback
        );

        assertEquals(fallback, result);
    }

    @Test
    void readJson_returnsDefaultOnJsonSyntaxError() throws Exception {
        Files.writeString(jsonFile, "not valid json");
        List<String> fallback = List.of("fallback");

        List<String> result = FileUtils.readJson(
                jsonFile,
                FileUtils.listTypeOf(String.class),
                fallback
        );

        assertEquals(fallback, result);
    }

    @Test
    void listTypeOf_returnsNonNull() {
        assertNotNull(FileUtils.listTypeOf(String.class));
    }

    @Test
    void dataFile_returnsCorrectPath() {
        Path p = FileUtils.dataFile("users.json");
        assertTrue(p.toString().contains("library_data"));
        assertTrue(p.toString().endsWith("users.json"));
    }

    @Test
    void writeJson_createsBackupDirectoryIfMissing() throws Exception {
        Path backupDir = FileUtils.dataFile("backups");

        if (Files.exists(backupDir)) {
            Files.walk(backupDir).sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.delete(p); } catch (Exception ignored) {}
                    });
        }

        Path file = FileUtils.dataFile("test_create_dir.json");
        FileUtils.writeJson(file, List.of("a"));
        assertTrue(Files.exists(backupDir));
    }

    @Test
    void gsonAdapters_roundTripLocalDateAndLocalDateTime() {
        Path file = tempDir.resolve("dates.json");

        DateHolder h = new DateHolder();
        h.date = LocalDate.of(2025, 1, 1);
        h.dateTime = LocalDateTime.of(2025, 1, 1, 10, 30);

        FileUtils.writeJson(file, h);

        DateHolder read = FileUtils.readJson(file, DateHolder.class, null);

        assertNotNull(read);
        assertEquals(h.date, read.date);
        assertEquals(h.dateTime, read.dateTime);
    }

    @Test
    void runtimeTypeAdapterFactory_ofRejectsNullBaseType() {
        assertThrows(NullPointerException.class,
                () -> FileUtils.RuntimeTypeAdapterFactory.of(null, "type"));
    }

    @Test
    void runtimeTypeAdapterFactory_ofRejectsBlankField() {
        assertThrows(IllegalArgumentException.class,
                () -> FileUtils.RuntimeTypeAdapterFactory.of(LibraryItem.class, " "));
    }

    @Test
    void runtimeTypeAdapterFactory_registerSubtypeValidation() {
        FileUtils.RuntimeTypeAdapterFactory<LibraryItem> f =
                FileUtils.RuntimeTypeAdapterFactory.of(LibraryItem.class, "type");

        assertThrows(NullPointerException.class, () -> f.registerSubtype(null, "X"));
        assertThrows(IllegalArgumentException.class, () -> f.registerSubtype(Book.class, " "));

        f.registerSubtype(Book.class, "BOOK");
        assertThrows(IllegalArgumentException.class, () -> f.registerSubtype(Book.class, "BOOK"));
    }

    @Test
    void runtimeTypeAdapterFactory_createReturnsNullForUnrelatedType() {
        FileUtils.RuntimeTypeAdapterFactory<LibraryItem> f =
                FileUtils.RuntimeTypeAdapterFactory.of(LibraryItem.class, "type");

        TypeAdapter<String> adapter = f.create(new Gson(), TypeToken.get(String.class));
        assertNull(adapter);
    }

    @Test
    void runtimeTypeAdapter_polymorphicRoundTrip() {
        Path file = tempDir.resolve("items.json");

        List<LibraryItem> items = List.of(
                new Book("ISBN1", "T1", "A1", BigDecimal.ONE),
                new CD("CD Title", "Artist", BigDecimal.TEN),
                new Journal("Journal Title", "Editor Name", "Issue 1", BigDecimal.ONE)
        );

        FileUtils.writeJson(file, items);

        List<LibraryItem> result = FileUtils.readJson(
                file,
                FileUtils.listTypeOf(LibraryItem.class),
                List.of()
        );

        assertEquals(3, result.size());
        assertTrue(result.get(0) instanceof Book);
        assertTrue(result.get(1) instanceof CD);
        assertTrue(result.get(2) instanceof Journal);
    }

    @Test
    void runtimeTypeAdapter_handlesNullsAndErrors() throws Exception {
        FileUtils.RuntimeTypeAdapterFactory<LibraryItem> f =
                FileUtils.RuntimeTypeAdapterFactory.of(LibraryItem.class, "type");

        Gson g = new GsonBuilder().registerTypeAdapterFactory(f).create();
        TypeAdapter<LibraryItem> adapter = g.getAdapter(TypeToken.get(LibraryItem.class));

        StringWriter sw = new StringWriter();
        JsonWriter w = new JsonWriter(sw);
        adapter.write(w, null);
        w.flush();
        assertEquals("null", sw.toString());

        LibraryItem b = new Book("X", "T", "A", BigDecimal.ONE);
        assertThrows(JsonParseException.class, () -> adapter.toJson(b));

        assertNull(adapter.fromJson("null"));

        String missingType = "{\"isbn\":\"1\",\"title\":\"T\"}";
        assertThrows(JsonParseException.class, () -> adapter.fromJson(missingType));
    }

    @Test
    void runtimeTypeAdapter_unknownLabelAndNonObject() {
        FileUtils.RuntimeTypeAdapterFactory<LibraryItem> f1 =
                FileUtils.RuntimeTypeAdapterFactory.of(LibraryItem.class, "type")
                        .registerSubtype(Book.class, "BOOK");

        Gson g1 = new GsonBuilder().registerTypeAdapterFactory(f1).create();
        TypeAdapter<LibraryItem> a1 = g1.getAdapter(TypeToken.get(LibraryItem.class));

        String badLabel =
                "{\"type\":\"UNKNOWN\",\"isbn\":\"1\",\"title\":\"T\",\"author\":\"A\",\"price\":1}";
        assertThrows(JsonParseException.class, () -> a1.fromJson(badLabel));

        FileUtils.RuntimeTypeAdapterFactory<Object> f2 =
                FileUtils.RuntimeTypeAdapterFactory.of(Object.class, "type")
                        .registerSubtype(String.class, "STR");

        Gson g2 = new GsonBuilder().registerTypeAdapterFactory(f2).create();
        TypeAdapter<Object> a2 = g2.getAdapter(TypeToken.get(Object.class));

        assertThrows(JsonParseException.class, () -> a2.toJson("hello"));
    }

    @Test
    void staticBlock_createsDirectory() {
        FileUtils.dataFile("dummy.json");
        assertTrue(Files.exists(Paths.get("library_data")));
    }

    @Test
    void staticBlock_normalUseDoesNotThrow() {
        assertDoesNotThrow(() -> FileUtils.dataFile("dummy2.json"));
    }
}
