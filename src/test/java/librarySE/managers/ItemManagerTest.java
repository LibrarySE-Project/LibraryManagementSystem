package librarySE.managers;

import librarySE.core.*;
import librarySE.repo.ItemRepository;
import librarySE.search.SearchStrategy;
import librarySE.managers.notifications.EmailNotifier;

import org.junit.jupiter.api.*;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemManagerTest {

    // -----------------------------------------------------
    // Fake Repositories
    // -----------------------------------------------------

    static class FakeItemRepo implements ItemRepository {
        List<LibraryItem> store = new CopyOnWriteArrayList<>();

        @Override public List<LibraryItem> loadAll() { return new ArrayList<>(store); }

        @Override public void saveAll(List<LibraryItem> items) {
            store.clear();
            store.addAll(items);
        }
    }

    static class FakeSearch implements SearchStrategy {
        boolean matchResult = false;

        @Override
        public boolean matches(LibraryItem item, String keyword) {
            return matchResult;
        }
    }

    static class FakeUserRepo implements librarySE.repo.UserRepository {
        List<User> users = new CopyOnWriteArrayList<>();
        @Override public List<User> loadAll() { return new ArrayList<>(users); }
        @Override public void saveAll(List<User> u) { users.clear(); users.addAll(u); }
    }

    // -----------------------------------------------------
    FakeItemRepo repo;
    FakeSearch search;

    @BeforeEach
    void setup() throws Exception {

        // Reset ItemManager singleton
        var f = ItemManager.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);

        // Reset UserManager Singleton
        var uf = UserManager.class.getDeclaredField("instance");
        uf.setAccessible(true);
        uf.set(null, null);

        // Reset Admin Singleton
        var af = Admin.class.getDeclaredField("instance");
        af.setAccessible(true);
        af.set(null, null);

        repo = new FakeItemRepo();
        search = new FakeSearch();
    }

    // -----------------------------------------------------
    // Singleton Tests
    // -----------------------------------------------------

    @Test
    void testInitCreatesInstance() {
        ItemManager m = ItemManager.init(repo, search);
        assertSame(m, ItemManager.getInstance());
    }

    @Test
    void testGetInstanceFailsIfNotInitialized() throws Exception {
        var f = ItemManager.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);

        assertThrows(IllegalStateException.class, ItemManager::getInstance);
    }

    // -----------------------------------------------------
    // addItem() Tests
    // -----------------------------------------------------

    @Test
    void testAddItemFailsWhenItemNull() {
        ItemManager m = ItemManager.init(repo, search);

        Admin.initialize("Admin", "Strong1!", "admin@mail.com");
        Admin admin = Admin.getInstance();

        assertThrows(IllegalArgumentException.class,
                () -> m.addItem(null, admin));
    }

    @Test
    void testAddItemFailsWhenAdminNull() {
        ItemManager m = ItemManager.init(repo, search);
        LibraryItem book = new Book("1","T","A", BigDecimal.TEN);

        assertThrows(IllegalArgumentException.class,
                () -> m.addItem(book, null));
    }

    @Test
    void testAddItemFailsWhenAdminNotAdmin() {
        ItemManager m = ItemManager.init(repo, search);

        Admin fakeAdmin = mock(Admin.class);
        when(fakeAdmin.isAdmin()).thenReturn(false);

        LibraryItem book = new Book("1", "T", "A", BigDecimal.TEN);

        assertThrows(IllegalArgumentException.class,
                () -> m.addItem(book, fakeAdmin));
    }

    @Test
    void testAddItemSuccessAndNotificationsSent() throws Exception {

        // Reset ItemManager
        var im = ItemManager.class.getDeclaredField("instance");
        im.setAccessible(true);
        im.set(null, null);

        // Reset UserManager
        var um = UserManager.class.getDeclaredField("instance");
        um.setAccessible(true);
        um.set(null, null);

        repo = new FakeItemRepo();
        search = new FakeSearch();

        ItemManager m = ItemManager.init(repo, search);

        Admin.initialize("Admin", "Strong1!", "admin@mail.com");
        Admin admin = Admin.getInstance();

        FakeUserRepo fakeUserRepo = new FakeUserRepo();
        fakeUserRepo.users.add(new User("U", Role.USER, "Strong1!", "u@mail.com"));
        UserManager.init(fakeUserRepo);

        LibraryItem book = new Book("1", "Title", "Auth", BigDecimal.TEN);

        class Cap {
            boolean notified = false;
            User u;
        }
        Cap cap = new Cap();

        try (MockedConstruction<EmailNotifier> mocked =
                mockConstruction(EmailNotifier.class,
                        (mock, ctx) -> doAnswer(inv -> {
                            cap.notified = true;
                            cap.u = inv.getArgument(0);   
                            return null;
                        }).when(mock).notify(any(), anyString(), anyString()))) {

            m.addItem(book, admin);
        }

        assertEquals(1, repo.store.size());
        assertTrue(cap.notified);                     
        assertEquals("u@mail.com", cap.u.getEmail());
    }


    @Test
    void testAddItemNotificationFailsButStillPasses() {

        ItemManager m = ItemManager.init(repo, search);

        // Admin
        Admin.initialize("Admin", "Strong1!", "admin@mail.com");
        Admin admin = Admin.getInstance();

        LibraryItem book = new Book("1","B","A", BigDecimal.ONE);

        FakeUserRepo fakeUserRepo = new FakeUserRepo();
        UserManager.init(fakeUserRepo);
        fakeUserRepo.users.add(new User("U", Role.USER, "Strong1!", "u@mail.com"));

        try (MockedConstruction<EmailNotifier> mocked =
                mockConstruction(EmailNotifier.class,
                        (mock,ctx)-> doThrow(new RuntimeException("fail"))
                                .when(mock).notify(any(), anyString(), anyString()))) {

            assertDoesNotThrow(() -> m.addItem(book, admin));
        }
    }

    // -----------------------------------------------------
    // deleteItem() Tests
    // -----------------------------------------------------

    @Test
    void testDeleteItemFailsWhenItemNull() {
        ItemManager m = ItemManager.init(repo, search);

        Admin.initialize("A", "Strong1!", "a@mail.com");
        Admin admin = Admin.getInstance();

        assertThrows(IllegalArgumentException.class,
                () -> m.deleteItem(null, admin));
    }

    @Test
    void testDeleteItemFailsWhenAdminNull() {
        ItemManager m = ItemManager.init(repo, search);
        LibraryItem book = new Book("1","T","A", BigDecimal.TEN);

        assertThrows(IllegalArgumentException.class,
                () -> m.deleteItem(book, null));
    }

    @Test
    void testDeleteItemFailsWhenNotAdmin() {
        ItemManager m = ItemManager.init(repo, search);

        Admin fakeAdmin = mock(Admin.class);
        when(fakeAdmin.isAdmin()).thenReturn(false);

        LibraryItem book = new Book("1", "T", "A", BigDecimal.TEN);

        assertThrows(IllegalArgumentException.class,
                () -> m.deleteItem(book, fakeAdmin));
    }


    @Test
    void testDeleteItemSuccess() {
        ItemManager m = ItemManager.init(repo, search);

        Admin.initialize("A","Strong1!","a@mail.com");
        Admin admin = Admin.getInstance();

        LibraryItem book = new Book("1","T","A", BigDecimal.TEN);
        repo.store.add(book);

        m.deleteItem(book, admin);

        assertEquals(0, repo.store.size());
    }

    // -----------------------------------------------------
    // searchItems() Tests
    // -----------------------------------------------------

    @Test
    void testSearchItemsFailsWhenKeywordNull() {
        ItemManager m = ItemManager.init(repo, search);
        assertThrows(IllegalArgumentException.class,
                () -> m.searchItems(null));
    }

    @Test
    void testSearchItemsMatchFound() {
        // Add item BEFORE init so ItemManager loads it
        repo.store.add(new Book("1", "T", "A", BigDecimal.TEN));

        ItemManager m = ItemManager.init(repo, search);

        search.matchResult = true;

        var result = m.searchItems("abc");

        assertEquals(1, result.size());
    }


    @Test
    void testSearchItemsNoMatch() {
        ItemManager m = ItemManager.init(repo, search);

        repo.store.add(new Book("1","T","A", BigDecimal.TEN));

        search.matchResult = false;

        var result = m.searchItems("abc");
        assertEquals(0, result.size());
    }

    // -----------------------------------------------------
    // getAllItems()
    // -----------------------------------------------------

    @Test
    void testGetAllItemsReturnsUnmodifiableCopy() {
        ItemManager m = ItemManager.init(repo, search);
        repo.store.add(new Book("1","T","A", BigDecimal.TEN));

        var list = m.getAllItems();

        assertThrows(UnsupportedOperationException.class,
                () -> list.add(new Book("2","X","Y", BigDecimal.ONE)));
    }
    @Test
    void testInitDoesNotReinitializeWhenInstanceExists() {
        ItemManager first = ItemManager.init(repo, search);

        FakeSearch newSearch = new FakeSearch();
        FakeItemRepo newRepo = new FakeItemRepo();

        ItemManager second = ItemManager.init(newRepo, newSearch);

        assertSame(first, second);

        assertSame(repo, getField(second, "repo"));
        assertSame(search, getField(second, "searchStrategy"));
    }

	private Object getField(ItemManager second, String string) {
	   
	        try {
	            var f = ItemManager.class.getDeclaredField(string);
	            ((Field) f).setAccessible(true);
	            return f.get(second);
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	    
	}
	@Test
	void testSetSearchStrategyChangesStrategy() throws Exception {
	    ItemManager m = ItemManager.init(repo, search);

	    FakeSearch newStrategy = new FakeSearch();
	    m.setSearchStrategy(newStrategy);

	    var f = ItemManager.class.getDeclaredField("searchStrategy");
	    f.setAccessible(true);
	    Object internal = f.get(m);

	    assertSame(newStrategy, internal);
	}
}
