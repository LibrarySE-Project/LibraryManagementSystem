package librarySE.managers;

import librarySE.core.*;
import librarySE.repo.BorrowRecordRepository;
import librarySE.repo.UserRepository;
import librarySE.repo.WaitlistRepository;

import librarySE.managers.notifications.EmailNotifier;
import librarySE.managers.notifications.Notifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockConstruction;

class BorrowManagerTest {

    // ---------- Fake Repositories ----------
    static class FakeBorrowRepo implements BorrowRecordRepository {
        List<BorrowRecord> store = new CopyOnWriteArrayList<>();
        @Override public List<BorrowRecord> loadAll() { return new ArrayList<>(store); }
        @Override public void saveAll(List<BorrowRecord> records) { store.clear(); store.addAll(records); }
    }

    static class FakeWaitlistRepo implements WaitlistRepository {
        List<WaitlistEntry> store = new CopyOnWriteArrayList<>();
        @Override public List<WaitlistEntry> loadAll() { return new ArrayList<>(store); }
        @Override public void saveAll(List<WaitlistEntry> entries) { store.clear(); store.addAll(entries); }
    }

    static class FakeUserRepo implements UserRepository {
        List<User> users = new CopyOnWriteArrayList<>();
        @Override public List<User> loadAll() { return new ArrayList<>(users); }
        @Override public void saveAll(List<User> list) { users.clear(); users.addAll(list); }
    }

    // Fake Item
    static class FakeItem extends Book {
        boolean available = true;
        boolean borrowResult = true;

        public FakeItem() { super("ISBN", "Title", "Author", BigDecimal.TEN); }

        @Override public boolean isAvailable() { return available; }
        @Override public boolean borrow() { return borrowResult; }
    }

    // Fake Notifier
    static class FakeNotifier implements Notifier {
        boolean notified = false;
        User target;
        @Override public void notify(User u, String sub, String msg) {
            notified = true;
            target = u;
        }
    }

    FakeBorrowRepo borrowRepo;
    FakeWaitlistRepo waitRepo;
    FakeUserRepo userRepo;

    User user;
    FakeItem item;

    @BeforeEach
    void setup() throws Exception {

        // Reset BorrowManager singleton
        var b = BorrowManager.class.getDeclaredField("instance");
        b.setAccessible(true);
        b.set(null, null);

        // Reset UserManager singleton
        var u = UserManager.class.getDeclaredField("instance");
        u.setAccessible(true);
        u.set(null, null);

        borrowRepo = new FakeBorrowRepo();
        waitRepo = new FakeWaitlistRepo();
        userRepo = new FakeUserRepo();

        UserManager.init(userRepo);

        user = new User("M", Role.USER, "pass123", "m@ps.com");
        userRepo.users.add(user);

        item = new FakeItem();

        BorrowManager.init(borrowRepo, waitRepo);
    }

    // ---------- Borrow Tests ----------

    @Test
    void testBorrowSuccess() {
        BorrowManager bm = BorrowManager.getInstance();

        boolean ok = bm.borrowItem(user, item);

        assertTrue(ok);
        assertEquals(1, borrowRepo.store.size());
    }

    @Test
    void testBorrowFailsWhenUserNull() {
        BorrowManager bm = BorrowManager.getInstance();
        assertThrows(IllegalArgumentException.class,
                () -> bm.borrowItem(null, item));
    }

    @Test
    void testBorrowFailsWhenItemNull() {
        BorrowManager bm = BorrowManager.getInstance();
        assertThrows(IllegalArgumentException.class,
                () -> bm.borrowItem(user, null));
    }

    @Test
    void testBorrowFailsWhenUserHasFine() {
        user.addFine(BigDecimal.TEN);
        BorrowManager bm = BorrowManager.getInstance();
        assertThrows(IllegalStateException.class,
                () -> bm.borrowItem(user, item));
    }

    @Test
    void testBorrowFailsWhenUserHasOverdue() throws Exception {

        BorrowManager bm = BorrowManager.getInstance();

        bm.borrowItem(user, item);

        BorrowRecord record = borrowRepo.store.get(0);

        var f = BorrowRecord.class.getDeclaredField("borrowDate");
        f.setAccessible(true);
        f.set(record, LocalDate.now().minusDays(50));

        borrowRepo.saveAll(borrowRepo.store);

        assertThrows(IllegalStateException.class,
                () -> bm.borrowItem(user, item));
    }


    @Test
    void testBorrowAddsToWaitlistWhenUnavailable() {
        item.available = false;
        BorrowManager bm = BorrowManager.getInstance();

        boolean ok = bm.borrowItem(user, item);

        assertFalse(ok);
        assertEquals(1, waitRepo.store.size());
    }

    @Test
    void testBorrowFailsWhenBorrowReturnsFalse() {
        item.borrowResult = false;
        BorrowManager bm = BorrowManager.getInstance();

        assertThrows(IllegalStateException.class,
                () -> bm.borrowItem(user, item));
    }

    // ---------- Return Tests ----------

    @Test
    void testReturnSuccess() {

        BorrowManager bm = BorrowManager.getInstance();
        bm.borrowItem(user, item);

        try (MockedConstruction<EmailNotifier> mock =
                     Mockito.mockConstruction(EmailNotifier.class))
        {
            bm.returnItem(user, item);
        }

        assertTrue(borrowRepo.store.get(0).isReturned());
    }

    @Test
    void testReturnFailsWhenUserNull() {
        BorrowManager bm = BorrowManager.getInstance();
        assertThrows(IllegalArgumentException.class,
                () -> bm.returnItem(null, item));
    }

    @Test
    void testReturnFailsWhenItemNull() {
        BorrowManager bm = BorrowManager.getInstance();
        assertThrows(IllegalArgumentException.class,
                () -> bm.returnItem(user, null));
    }

    @Test
    void testReturnFailsWhenNoRecord() {
        BorrowManager bm = BorrowManager.getInstance();
        assertThrows(IllegalArgumentException.class,
                () -> bm.returnItem(user, item));
    }

    @Test
    void testReturnClearsWaitlistAndSendsEmail() throws Exception {

        // ---- Reset Singleton ----
        Field f0 = BorrowManager.class.getDeclaredField("instance");
        f0.setAccessible(true);
        f0.set(null, null);

        BorrowManager bm = BorrowManager.init(borrowRepo, waitRepo);

        // Add both users to UserManager
        userRepo.users.add(user);
        User u2 = new User("B", Role.USER, "p", "b@ps.com");
        userRepo.users.add(u2);
        UserManager.init(userRepo);

        // ---- Borrow item first ----
        bm.borrowItem(user, item);

        // ---- Add u2 to waitlist ----
        waitRepo.store.add(new WaitlistEntry(item.getId(), "b@ps.com", LocalDate.now()));

        FakeNotifier capture = new FakeNotifier();

        // ---- Mock EmailNotifier ----
        try (MockedConstruction<EmailNotifier> mocked =
                mockConstruction(EmailNotifier.class,
                        (mock, ctx) -> doAnswer(inv -> {
                            capture.notify(
                                    inv.getArgument(0),
                                    inv.getArgument(1),
                                    inv.getArgument(2));
                            return null;
                        }).when(mock).notify(any(), anyString(), anyString())))
        {
            bm.returnItem(user, item);
        }

        // ---- Assertions ----
        assertTrue(capture.notified, "Notifier should be called");
        assertEquals("b@ps.com", capture.target.getEmail(), "Correct user must be notified");
        assertEquals(0, waitRepo.store.size(), "Waitlist must be cleared");
    }


    // ---------- Utility tests ----------

    @Test
    void testCalculateTotalFines() {
        BorrowManager bm = BorrowManager.getInstance();
        bm.borrowItem(user, item);

        BigDecimal v =
                bm.calculateTotalFines(user, LocalDate.now().plusDays(80));

        assertTrue(v.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalculateTotalFinesWhenReturned() {

        BorrowManager bm = BorrowManager.getInstance();
        bm.borrowItem(user, item);

        try (MockedConstruction<EmailNotifier> mock =
                     Mockito.mockConstruction(EmailNotifier.class))
        {
            bm.returnItem(user, item);
        }

        BigDecimal v =
                bm.calculateTotalFines(user, LocalDate.now().plusDays(80));

        assertEquals(BigDecimal.ZERO, v);
    }

    @Test
    void testGetOverdueItems() {
        BorrowManager bm = BorrowManager.getInstance();
        bm.borrowItem(user, item);

        var list = bm.getOverdueItems(LocalDate.now().plusDays(100));

        assertEquals(1, list.size());
    }

    @Test
    void testGetBorrowRecordsForUser() {
        BorrowManager bm = BorrowManager.getInstance();
        bm.borrowItem(user, item);

        var list = bm.getBorrowRecordsForUser(user);

        assertEquals(1, list.size());
    }

    @Test
    void testApplyOverdueFinesOnEmptyList() {
        BorrowManager bm = BorrowManager.getInstance();
        assertDoesNotThrow(() -> bm.applyOverdueFines(LocalDate.now()));
    }

    @Test
    void testGetInstanceFailsWhenNotInitialized() throws Exception {
        var f = BorrowManager.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);

        assertThrows(IllegalStateException.class, BorrowManager::getInstance);
    }
}
