package librarySE.repo;

import librarySE.core.Book;
import librarySE.core.LibraryItem;
import librarySE.utils.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class FileItemRepositoryTest {

    @Test
    @DisplayName("loadAll: returns list from FileUtils.readJson when not null")
    void loadAll_returnsListFromFile() {
        try (MockedStatic<FileUtils> filesMock = Mockito.mockStatic(FileUtils.class)) {

            LibraryItem book = new Book("ISBN1", "Title1", "Author1", BigDecimal.TEN);
            List<LibraryItem> stored = List.of(book);

            // Important: use any() for the third argument so it matches the new ArrayList<>() call
            filesMock.when(() -> FileUtils.readJson(
                    any(Path.class),
                    any(java.lang.reflect.Type.class),
                    any()
            )).thenReturn(stored);

            FileItemRepository repo = new FileItemRepository();
            List<LibraryItem> result = repo.loadAll();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(book, result.get(0));
        }
    }

    @Test
    @DisplayName("loadAll: returns empty list when FileUtils.readJson returns null")
    void loadAll_nullFromFile_returnsEmptyList() {
        try (MockedStatic<FileUtils> filesMock = Mockito.mockStatic(FileUtils.class)) {

            filesMock.when(() -> FileUtils.readJson(
                    any(Path.class),
                    any(java.lang.reflect.Type.class),
                    any()
            )).thenReturn(null);

            FileItemRepository repo = new FileItemRepository();
            List<LibraryItem> result = repo.loadAll();

            assertNotNull(result);
            assertTrue(result.isEmpty(), "Result list should be empty when underlying readJson returns null");
        }
    }

    @Test
    @DisplayName("saveAll: writes a defensive snapshot of the given list")
    void saveAll_writesSnapshot() {
        try (MockedStatic<FileUtils> filesMock = Mockito.mockStatic(FileUtils.class)) {

            FileItemRepository repo = new FileItemRepository();

            LibraryItem book = new Book("ISBN2", "Title2", "Author2", BigDecimal.ONE);
            List<LibraryItem> items = new ArrayList<>();
            items.add(book);

            AtomicReference<Object> capturedData = new AtomicReference<>();

            filesMock.when(() -> FileUtils.writeJson(
                    any(Path.class),
                    any()
            )).thenAnswer(invocation -> {
                capturedData.set(invocation.getArgument(1));
                return null;
            });

            repo.saveAll(items);

            Object written = capturedData.get();
            assertNotNull(written);
            assertTrue(written instanceof List);

            @SuppressWarnings("unchecked")
            List<LibraryItem> writtenList = (List<LibraryItem>) written;

            assertEquals(items, writtenList);
            assertNotSame(items, writtenList); // snapshot, not the same instance
        }
    }
}
