package librarySE.managers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import librarySE.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserManagerTest {

    // Fake repository for testing
    static class FakeUserRepo implements UserRepository {
        private final List<User> store = new CopyOnWriteArrayList<>();

        @Override
        public List<User> loadAll() {
            return new ArrayList<>(store);
        }

        @Override
        public void saveAll(List<User> users) {
            store.clear();
            store.addAll(users);
        }
    }

    private FakeUserRepo repo;

    @BeforeEach
    void resetSingleton() throws Exception {
        // reset UserManager.instance
        var field = UserManager.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);

        repo = new FakeUserRepo();
    }

    // Initialization Tests
    @Test
    void testInit_CreatesInstance() {
        UserManager um = UserManager.init(repo);
        assertNotNull(um);
    }

    @Test
    void testInit_Twice_ReturnsSameInstance() {
        UserManager um1 = UserManager.init(repo);
        UserManager um2 = UserManager.init(repo);
        assertSame(um1, um2);
    }

    @Test
    void testGetInstance_WithoutInit_Throws() {
        assertThrows(IllegalStateException.class, UserManager::getInstance);
    }

    // Add User Tests
    @Test
    void testAddUser_Success() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        User u = new User("M", Role.USER, "pass123", "m@ps.com");
        um.addUser(u);

        assertEquals(1, um.getAllUsers().size());
    }

    @Test
    void testAddUser_Null_Throws() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        assertThrows(IllegalArgumentException.class, () -> um.addUser(null));
    }

    // Find User By Email Tests
    @Test
    void testFindUserByEmail_Found() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        User u = new User("A", Role.USER, "pass123", "a@ps.com");
        um.addUser(u);

        Optional<User> result = um.findUserByEmail("a@ps.com");
        assertTrue(result.isPresent());
        assertEquals(u, result.get());
    }

    @Test
    void testFindUserByEmail_NotFound() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        Optional<User> result = um.findUserByEmail("no@ps.com");
        assertFalse(result.isPresent());
    }

    @Test
    void testFindUserByEmail_NullOrBlank_ReturnsEmpty() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        assertTrue(um.findUserByEmail(null).isEmpty());
        assertTrue(um.findUserByEmail("").isEmpty());
    }

    // SaveAll Tests
    @Test
    void testSaveAll_WritesToRepo() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        User u = new User("A", Role.USER, "pass123", "a@ps.com");
        um.addUser(u);

        um.saveAll(); // should push to repo

        assertEquals(1, repo.store.size());
    }

    // Get All Users Tests
    @Test
    void testGetAllUsers_ReturnsImmutableList() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        User u = new User("X", Role.USER, "pass123", "x@ps.com");
        um.addUser(u);

        var list = um.getAllUsers();
        assertThrows(UnsupportedOperationException.class, () -> list.add(u));
    }
}

