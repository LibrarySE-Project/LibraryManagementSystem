package librarySE.managers;

import librarySE.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserManagerTest {

    // Fake repository for testing
    static class FakeUserRepo implements UserRepository {
        final List<User> store = new CopyOnWriteArrayList<>();

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
    void testInit_LoadsExistingUsersFromRepo() {
        // pre-populate repo
        repo.store.add(new User("Pre", Role.USER, "pass123", "pre@ps.com"));

        UserManager um = UserManager.init(repo);
        assertEquals(1, um.getAllUsers().size());
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
    void testFindUserByEmail_CaseInsensitive() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        User u = new User("B", Role.USER, "pass123", "b@ps.com");
        um.addUser(u);

        Optional<User> result = um.findUserByEmail("B@PS.COM");
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
        assertTrue(um.findUserByEmail("   ").isEmpty());
    }

    // Find User By Username Tests
    @Test
    void testFindUserByUsername_Found() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        User u = new User("user1", Role.USER, "pass123", "u1@ps.com");
        um.addUser(u);

        Optional<User> result = um.findUserByUsername("user1");
        assertTrue(result.isPresent());
        assertEquals(u, result.get());
    }

    @Test
    void testFindUserByUsername_NotFound() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        User u = new User("someone", Role.USER, "pass123", "s@ps.com");
        um.addUser(u);

        Optional<User> result = um.findUserByUsername("other");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindUserByUsername_NullOrBlank_ReturnsEmpty() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        assertTrue(um.findUserByUsername(null).isEmpty());
        assertTrue(um.findUserByUsername("").isEmpty());
        assertTrue(um.findUserByUsername("   ").isEmpty());
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

    // unregisterUser Tests

    @Test
    void testUnregisterUser_Null_Throws() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        assertThrows(IllegalArgumentException.class, () -> um.unregisterUser(null));
    }

    @Test
    void testUnregisterUser_Admin_Throws() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        User admin = mock(User.class);
        when(admin.isAdmin()).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> um.unregisterUser(admin));
    }

    @Test
    void testUnregisterUser_HasOutstandingFine_Throws() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        User user = mock(User.class);
        when(user.isAdmin()).thenReturn(false);
        when(user.hasOutstandingFine()).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> um.unregisterUser(user));
    }

    @Test
    void testUnregisterUser_HasActiveLoans_Throws() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        User user = mock(User.class);
        when(user.isAdmin()).thenReturn(false);
        when(user.hasOutstandingFine()).thenReturn(false);

        BorrowRecord activeRecord = mock(BorrowRecord.class);
        when(activeRecord.isReturned()).thenReturn(false);

        try (MockedStatic<BorrowManager> mocked = Mockito.mockStatic(BorrowManager.class)) {
            BorrowManager bm = mock(BorrowManager.class);
            mocked.when(BorrowManager::getInstance).thenReturn(bm);
            when(bm.getBorrowRecordsForUser(user)).thenReturn(List.of(activeRecord));

            assertThrows(IllegalStateException.class, () -> um.unregisterUser(user));

            // ensure removal did not happen
            verify(bm).getBorrowRecordsForUser(user);
        }
    }

    @Test
    void testUnregisterUser_Success_RemovesUserAndSaves() {
        UserManager.init(repo);
        UserManager um = UserManager.getInstance();

        User user = new User("ToRemove", Role.USER, "pass123", "rem@ps.com");
        um.addUser(user);

        // After addUser, repo.store contains the user; ensure that
        assertEquals(1, repo.store.size());

        BorrowRecord returnedRecord = mock(BorrowRecord.class);
        when(returnedRecord.isReturned()).thenReturn(true);

        try (MockedStatic<BorrowManager> mocked = Mockito.mockStatic(BorrowManager.class)) {
            BorrowManager bm = mock(BorrowManager.class);
            mocked.when(BorrowManager::getInstance).thenReturn(bm);
            // Either no records or all returned; both should allow unregister
            when(bm.getBorrowRecordsForUser(user)).thenReturn(List.of(returnedRecord));

            um.unregisterUser(user);

            // user should be removed from manager
            assertTrue(um.getAllUsers().isEmpty());
            // repo should be updated accordingly
            assertEquals(0, repo.store.size());
        }
    }
}
