package librarySE.managers;

import librarySE.core.*;
import librarySE.repo.ItemRepository;
import librarySE.search.SearchStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ItemManagerTest {

    // Fake repo for storing items
    static class FakeItemRepo implements ItemRepository {
        List<LibraryItem> store = new CopyOnWriteArrayList<>();

        @Override
        public List<LibraryItem> loadAll() {
            return new ArrayList<>(store);
        }

        @Override
        public void saveAll(List<LibraryItem> items) {
            store.clear();
            store.addAll(items);
        }
    }

    // Fake search strategy (matches if title contains keyword)
    static class FakeSearch implements SearchStrategy {
        @Override
        public boolean matches(LibraryItem item, String keyword) {
            return item.getTitle().toLowerCase().contains(keyword.toLowerCase());
        }
    }

    FakeItemRepo repo;
    SearchStrategy search;

    ItemManager manager;
    Admin admin;
    LibraryItem book;

    @BeforeEach
    void setup() throws Exception {

        // Reset singleton
        var f = ItemManager.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);

        repo = new FakeItemRepo();
        search = new FakeSearch();

        // initialize ItemManager
        manager = ItemManager.init(repo, search);

        // admin needed for add/delete operations
        Admin.initialize("admin", "123456", "a@a.com");
        admin = Admin.getInstance();

        // test item
        book = new Book("ISBN", "TitleABC", "Author", BigDecimal.TEN);
    }

    // init should load items from repo
    @Test
    void testInitLoadsFromRepo() {
        repo.store.add(book);
        ItemManager.init(repo, search);
        assertEquals(1, ItemManager.getInstance().getAllItems().size());
    }

    // getInstance should fail if not initialized
    @Test
    void testGetInstanceFailsIfNotInitialized() throws Exception {
        var f = ItemManager.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);

        assertThrows(IllegalStateException.class,
                ItemManager::getInstance);
    }

    // addItem should insert into repo
    @Test
    void testAddItemSuccess() {
        manager.addItem(book, admin);
        assertEquals(1, repo.store.size());
    }

    // addItem should fail if called by non-admin
    @Test
    void testAddItemFailsForNonAdmin() {
        User u = new User("x", Role.USER, "123456", "x@x.com");
        assertThrows(IllegalArgumentException.class,
                () -> manager.addItem(book, (Admin) u));
    }

    // deleteItem removes item
    @Test
    void testDeleteItemSuccess() {
        manager.addItem(book, admin);
        manager.deleteItem(book, admin);
        assertEquals(0, repo.store.size());
    }

    // deleteItem fails if non-admin
    @Test
    void testDeleteItemFailsForNonAdmin() {
        manager.addItem(book, admin);
        User u = new User("x", Role.USER, "123456", "x@x.com");
        assertThrows(IllegalArgumentException.class,
                () -> manager.deleteItem(book, (Admin) u));
    }

    // searchItems should use the strategy
    @Test
    void testSearchItems() {
        manager.addItem(book, admin);

        List<LibraryItem> result = manager.searchItems("abc");
        assertEquals(1, result.size());
    }

    // searchItems should reject null keyword
    @Test
    void testSearchNullKeyword() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.searchItems(null));
    }

    // getAllItems returns copy (immutable)
    @Test
    void testGetAllItems() {
        manager.addItem(book, admin);
        List<LibraryItem> list = manager.getAllItems();

        assertEquals(1, list.size());
        assertThrows(UnsupportedOperationException.class,
                () -> list.add(book));
    }
}

