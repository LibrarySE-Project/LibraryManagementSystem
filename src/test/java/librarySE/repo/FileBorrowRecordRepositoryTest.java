package librarySE.repo;

import librarySE.managers.BorrowRecord;
import librarySE.utils.FileUtils;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileBorrowRecordRepositoryTest {

    private FileBorrowRecordRepository repo;

    @BeforeEach
    void setup() {
        repo = new FileBorrowRecordRepository();
    }

    @Test
    void loadAll_returnsEmptyList_whenFileIsEmpty() {
        try (MockedStatic<FileUtils> mocked = mockStatic(FileUtils.class)) {

            mocked.when(() ->
                    FileUtils.readJson(any(Path.class), any(Type.class), any())
            ).thenReturn(null);

            List<BorrowRecord> result = repo.loadAll();
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void loadAll_returnsLoadedList() {
        List<BorrowRecord> fakeList = List.of(mock(BorrowRecord.class));

        try (MockedStatic<FileUtils> mocked = mockStatic(FileUtils.class)) {

            mocked.when(() ->
                    FileUtils.readJson(any(Path.class), any(Type.class), any())
            ).thenReturn(fakeList);

            List<BorrowRecord> result = repo.loadAll();
            assertEquals(1, result.size());
        }
    }

    @Test
    void saveAll_callsWriteJson() {
        List<BorrowRecord> list = new ArrayList<>();

        try (MockedStatic<FileUtils> mocked = mockStatic(FileUtils.class)) {

            repo.saveAll(list);

            mocked.verify(() -> FileUtils.writeJson(any(Path.class), eq(list)));
        }
    }
}

