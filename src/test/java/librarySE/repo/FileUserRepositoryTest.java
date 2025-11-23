package librarySE.repo;

import librarySE.managers.User;
import librarySE.utils.FileUtils;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileUserRepositoryTest {

    private FileUserRepository repo;

    @BeforeEach
    void setup() {
        repo = new FileUserRepository();
    }

    @Test
    void loadAll_returnsEmptyList_ifNullReturned() {
        try (MockedStatic<FileUtils> mocked = mockStatic(FileUtils.class)) {

            mocked.when(() ->
                    FileUtils.readJson(any(Path.class), any(Type.class), any())
            ).thenReturn(null);

            List<User> result = repo.loadAll();
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void loadAll_returnsUsers_whenJsonHasData() {
    	List<User> users = List.of(mock(User.class));
    	
        try (MockedStatic<FileUtils> mocked = mockStatic(FileUtils.class)) {

            mocked.when(() ->
                    FileUtils.readJson(any(Path.class), any(Type.class), any())
            ).thenReturn(users);

            List<User> result = repo.loadAll();
            assertEquals(1, result.size());
        }
    }

    @Test
    void saveAll_callsWriteJson() {
        List<User> users = new ArrayList<>();

        try (MockedStatic<FileUtils> mocked = mockStatic(FileUtils.class)) {

            repo.saveAll(users);

            mocked.verify(() -> FileUtils.writeJson(any(Path.class), eq(users)));
        }
    }
}

