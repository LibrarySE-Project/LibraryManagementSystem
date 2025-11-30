package librarySE.repo;

import librarySE.core.WaitlistEntry;
import librarySE.utils.FileUtils;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileWaitlistRepositoryTest {

    private FileWaitlistRepository repo;

    @BeforeEach
    void setup() {
        repo = new FileWaitlistRepository();
    }

    @Test
    void loadAll_returnsEmptyList_ifFileEmpty() {
        try (MockedStatic<FileUtils> mocked = mockStatic(FileUtils.class)) {

            mocked.when(() ->
                    FileUtils.readJson(any(Path.class), any(Type.class), any())
            ).thenReturn(null);

            List<WaitlistEntry> result = repo.loadAll();
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void loadAll_returnsList_ifExists() {
        List<WaitlistEntry> fake = List.of(mock(WaitlistEntry.class));

        try (MockedStatic<FileUtils> mocked = mockStatic(FileUtils.class)) {

            mocked.when(() ->
                    FileUtils.readJson(any(Path.class), any(Type.class), any())
            ).thenReturn(fake);

            List<WaitlistEntry> result = repo.loadAll();
            assertEquals(1, result.size());
        }
    }

    @Test
    void saveAll_callsWriteJson() {
        List<WaitlistEntry> entries = new ArrayList<>();

        try (MockedStatic<FileUtils> mocked = mockStatic(FileUtils.class)) {

            repo.saveAll(entries);

            mocked.verify(() -> FileUtils.writeJson(any(Path.class), eq(entries)));
        }
    }
}

