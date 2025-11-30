package librarySE.managers;

import librarySE.core.*;
import librarySE.repo.BorrowRecordRepository;
import librarySE.repo.UserRepository;
import librarySE.repo.WaitlistRepository;
import librarySE.strategy.FineStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BorrowManagerTest {

    // Fake BorrowRecordRepository
    static class FakeBorrowRepo implements BorrowRecordRepository {
        List<BorrowRecord> store = new CopyOnWriteArrayList<>();

        @Override
        public List<BorrowRecord> loadAll() {
            return new ArrayList<>(store);
        }

        @Override
        public void saveAll(List<BorrowRecord> records) {
            store.clear();
            store.addAll(records);
        }
    }

    // Fake WaitlistRepository
    static class FakeWaitlistRepo implements WaitlistRepository {
        List<WaitlistEntry> store = new CopyOnWriteArrayList<>();

        @Override
        public List<WaitlistEntry> loadAll() {
            return new ArrayList<>(store);
        }

        @Override
        public void saveAll(List<WaitlistEntry> entries) {
            store.clear();
            store.addAll(entries);
        }
    }

    // Fake UserRepository
    static class FakeUserRepo implements UserRepository {
        List<User> users = new CopyOnWriteArrayList<>();

        @Override
        public List<User> loadAll() {
            return new ArrayList<>(users);
        }

        @Override
        public void saveAll(List<User> list) {
            users.clear();
            users.addAll(list);
        }
    }

    // Test Data
    FakeBorrowRepo borrowRepo;
    FakeWaitlistRepo waitRepo;
    FakeUserRepo userRepo;

    User user;
    LibraryItem book;
    FineStrategy strategy;

    @BeforeEach
    void reset() throws Exception {

        // Reset BorrowManager singleton
        var f1 = BorrowManager.class.getDeclaredField("instance");
        f1.setAccessible(true);
        f1.set(null, null);

        // Reset UserManager singleton
        var f2 = UserManager.class.getDeclaredField("instance");
        f2.setAccessible(true);
        f2.set(null, null);

        // Initialize fake repositories
        borrowRepo = new FakeBorrowRepo();
        waitRepo = new FakeWaitlistRepo();
        userRepo = new FakeUserRepo();

        // Initialize UserManager with repository
        UserManager.init(userRepo);
        UserManager.getInstance().addUser(new User("M", Role.USER, "pass123", "m@ps.com"));

        user = UserManager.getInstance()
                .findUserByEmail("m@ps.com")
                .orElseThrow();

        // Book constructor requires 4 arguments
        book = new Book("ISBN", "Title", "Author", BigDecimal.TEN);

        strategy = book.getMaterialType().createFineStrategy();

        // Initialize BorrowManager
        BorrowManager.init(borrowRepo, waitRepo);
    }

    // Borrow success
    @Test
    void testBorrowSuccess() {
        BorrowManager bm = BorrowManager.getInstance();
        boolean ok = bm.borrowItem(user, book);

        assertTrue(ok);
        assertFalse(book.isAvailable());
        assertEquals(1, borrowRepo.store.size());
    }

    // Adds to waitlist when unavailable
    @Test
    void testBorrowAddsToWaitlistWhenUnavailable() {
        BorrowManager bm = BorrowManager.getInstance();

        bm.borrowItem(user, book);

        User user2 = new User("X", Role.USER, "p12345", "x@ps.com");
        userRepo.users.add(user2);

        boolean ok = bm.borrowItem(user2, book);

        assertFalse(ok);
        assertEquals(1, waitRepo.store.size());
    }

    // Invalid arguments
    @Test
    void testBorrowFailsForNullUserOrItem() {
        BorrowManager bm = BorrowManager.getInstance();

        assertThrows(IllegalArgumentException.class,
                () -> bm.borrowItem(null, book));

        assertThrows(IllegalArgumentException.class,
                () -> bm.borrowItem(user, null));
    }

    // Return success
    @Test
    void testReturnItemSuccess() {
        BorrowManager bm = BorrowManager.getInstance();

        bm.borrowItem(user, book);
        bm.returnItem(user, book);

        assertTrue(book.isAvailable());
        assertTrue(borrowRepo.store.get(0).isReturned());
    }

    // Return fails when no borrow record exists
    @Test
    void testReturnItemFailsWhenNotBorrowed() {
        BorrowManager bm = BorrowManager.getInstance();

        assertThrows(IllegalArgumentException.class,
                () -> bm.returnItem(user, book));
    }

    // Total fines calculation
    @Test
    void testCalculateTotalFines() {
        BorrowManager bm = BorrowManager.getInstance();
        bm.borrowItem(user, book);

        LocalDate overdueDate = LocalDate.now().plusDays(50);
        BigDecimal fine = bm.calculateTotalFines(user, overdueDate);

        assertTrue(fine.compareTo(BigDecimal.ZERO) > 0);
    }

    // Overdue items
    @Test
    void testGetOverdueItems() {
        BorrowManager bm = BorrowManager.getInstance();
        bm.borrowItem(user, book);

        List<BorrowRecord> list =
                bm.getOverdueItems(LocalDate.now().plusDays(100));

        assertEquals(1, list.size());
    }
}



