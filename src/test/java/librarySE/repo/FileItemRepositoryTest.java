package librarySE.repo;

import librarySE.core.Book;
import librarySE.core.LibraryItem;
import librarySE.utils.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.gson.reflect.TypeToken;
import com.sun.glass.ui.CommonDialogs.Type;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

class FileItemRepositoryTest {

@Test
@DisplayName("loadAll: returns list from FileUtils.readJson when not null")
void loadAll_returnsListFromFile() throws Exception {
    // Access the private static FILE field via reflection
    var fileField = FileItemRepository.class.getDeclaredField("FILE");
    fileField.setAccessible(true);
    Path file = (Path) fileField.get(null);

    java.lang.reflect.Type type = new TypeToken<List<LibraryItem>>() {}.getType();
    List<LibraryItem> defaultValue = new ArrayList<>();

    LibraryItem book = new Book("ISBN1", "Title1", "Author1", BigDecimal.TEN);
    List<LibraryItem> stored = List.of(book);

    try (MockedStatic<FileUtils> filesMock = Mockito.mockStatic(FileUtils.class)) {

        // Stub readJson with the exact same arguments used inside loadAll()
        filesMock.when(() -> FileUtils.readJson(file, type, defaultValue))
                 .thenReturn(stored);

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

   

}
