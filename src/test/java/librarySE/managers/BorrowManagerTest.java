package librarySE.managers;

import librarySE.core.*;
import librarySE.managers.notifications.EmailNotifier;
import librarySE.managers.notifications.Notifier;
import librarySE.repo.BorrowRecordRepository;
import librarySE.repo.UserRepository;
import librarySE.repo.WaitlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class BorrowManagerTest {

    // Fake repositories ----------------------------------------------------

    static class FakeBorrowRepo implements BorrowRecordRepository {
        List<BorrowRecord> store = new CopyOnWriteArrayList<>();
        @Override public List<BorrowRecord> loadAll() { return List.copyOf(store); }
        @Override public void saveAll(List<BorrowRecord> records) {
            store.clear();
            store.addAll(records);
        }
    }

    static class FakeWaitlistRepo implements WaitlistRepository {
        List<WaitlistEntry> store = new CopyOnWriteArrayList<>();
        @Override public List<WaitlistEntry> loadAll() { return List.copyOf(store); }
        @Override public void saveAll(List<WaitlistEntry> entries) {
            store.clear();
            store.addAll(entries);
        }
    }

    static class FakeUserRepo implements UserRepository {
        List<User> users = new CopyOnWriteArrayList<>();
        @Override public List<User> loadAll() { return List.copyOf(users); }
        @Override public void saveAll(List<User> list) {
            users.clear();
            users.addAll(list);
        }
    }

    // Simple concrete item -------------------------------------------------

    static class FakeItem extends Book {
        boolean available = true;
        boolean borrowResult = true;

        FakeItem() {
            super("ISBN", "Title", "Author", BigDecimal.TEN);
        }

        @Override public boolean isAvailable() { return available; }
        @Override public boolean borrow() { return borrowResult; }
    }

    // Helper notifier used only for capturing calls ------------------------

    static class CaptureNotifier implements Notifier {
        boolean notified = false;
        User target;
        String subject;
        String body;

        @Override
        public void notify(User u, String sub, String msg) {
            notified = true;
            target = u;
            subject = sub;
            body = msg;
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
        Field bf = BorrowManager.class.getDeclaredField("instance");
        bf.setAccessible(true);
        bf.set(null, null);

        // Reset UserManager singleton
        Field uf = UserManager.class.getDeclaredField("instance");
        uf.setAccessible(true);
        uf.set(null, null);

        borrowRepo = new FakeBorrowRepo();
        waitRepo = new FakeWaitlistRepo();
        userRepo = new FakeUserRepo();

        UserManager.init(userRepo);

        user = new User("M", Role.USER, "pass123", "m@ps.com");
        userRepo.users.add(user);

        item = new FakeItem();

        BorrowManager.init(borrowRepo, waitRepo);
    }

    // Borrow tests --------------------------------------------------------

    @Test
    void borrowItem_successfulBorrow() {
        BorrowManager bm = BorrowManager.getInstance();

        boolean ok = bm.borrowItem(user, item);

        assertTrue(ok);
        assertEquals(1, borrowRepo.store.size());
    }

    @Test
    void borrowItem_failsWhenUserNull() {
        BorrowManager bm = BorrowManager.getInstance();
        assertThrows(IllegalArgumentException.class,
                () -> bm.borrowItem(null, item));
    }

    @Test
    void borrowItem_failsWhenItemNull() {
        BorrowManager bm = BorrowManager.getInstance();
        assertThrows(IllegalArgumentException.class,
                () -> bm.borrowItem(user, null));
    }

    @Test
    void borrowItem_failsWhenUserHasOutstandingFine() {
        user.addFine(BigDecimal.TEN);
        BorrowManager bm = BorrowManager.getInstance();

        assertThrows(IllegalStateException.class,
                () -> bm.borrowItem(user, item));
    }

    @Test
    void borrowItem_failsWhenUserHasOverdueRecord() throws Exception {
        BorrowManager bm = BorrowManager.getInstance();

        // Replace internal borrowRecords with a mocked overdue record for this user
        Field f = BorrowManager.class.getDeclaredField("borrowRecords");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        CopyOnWriteArrayList<BorrowRecord> list =
                (CopyOnWriteArrayList<BorrowRecord>) f.get(bm);
        list.clear();

        BorrowRecord overdueRecord = Mockito.mock(BorrowRecord.class);
        when(overdueRecord.getUser()).thenReturn(user);
        when(overdueRecord.isOverdue(any(LocalDate.class))).thenReturn(true);

        list.add(overdueRecord);

        assertThrows(IllegalStateException.class,
                () -> bm.borrowItem(user, item));
    }

    @Test
    void borrowItem_addsToWaitlistWhenItemUnavailable() {
        item.available = false;
        BorrowManager bm = BorrowManager.getInstance();

        boolean ok = bm.borrowItem(user, item);

        assertFalse(ok);
        assertEquals(1, waitRepo.store.size());
        WaitlistEntry entry = waitRepo.store.get(0);
        assertEquals(item.getId(), entry.getItemId());
        assertEquals(user.getEmail(), entry.getUserEmail());
    }

    @Test
    void borrowItem_failsWhenBorrowReturnsFalse() {
        item.borrowResult = false;
        BorrowManager bm = BorrowManager.getInstance();

        assertThrows(IllegalStateException.class,
                () -> bm.borrowItem(user, item));
    }

    // Return tests --------------------------------------------------------

    @Test
    void returnItem_marksRecordReturned() {
        BorrowManager bm = BorrowManager.getInstance();
        bm.borrowItem(user, item);

        try (MockedConstruction<EmailNotifier> mock =
                     Mockito.mockConstruction(EmailNotifier.class)) {
            bm.returnItem(user, item);
        }

        assertTrue(borrowRepo.store.get(0).isReturned());
    }

    @Test
    void returnItem_failsWhenUserNull() {
        BorrowManager bm = BorrowManager.getInstance();
        assertThrows(IllegalArgumentException.class,
                () -> bm.returnItem(null, item));
    }

    @Test
    void returnItem_failsWhenItemNull() {
        BorrowManager bm = BorrowManager.getInstance();
        assertThrows(IllegalArgumentException.class,
                () -> bm.returnItem(user, null));
    }

    @Test
    void returnItem_failsWhenNoBorrowRecordFound() {
        BorrowManager bm = BorrowManager.getInstance();
        assertThrows(IllegalArgumentException.class,
                () -> bm.returnItem(user, item));
    }

    @Test
    void returnItem_throwsWhenRecordDoesNotMatchUserOrItem() {
        BorrowManager bm = BorrowManager.getInstance();

        // Borrow with a different user and item so the predicate is evaluated to false
        User otherUser = new User("X", Role.USER, "p", "x@mail.com");
        userRepo.users.add(otherUser);
        FakeItem otherItem = new FakeItem();
        bm.borrowItem(otherUser, otherItem);

        assertThrows(IllegalArgumentException.class,
                () -> bm.returnItem(user, item));
    }


    @Test
    void returnItem_handlesWaitlistEntryWithMissingUser() throws Exception {
        BorrowManager bm = BorrowManager.getInstance();
        bm.borrowItem(user, item);

        // Waitlist entry with email that does not exist in UserManager
        waitRepo.store.add(new WaitlistEntry(item.getId(), "unknown@mail.com", LocalDate.now()));

        Field wf = BorrowManager.class.getDeclaredField("waitlist");
        wf.setAccessible(true);
        @SuppressWarnings("unchecked")
        CopyOnWriteArrayList<WaitlistEntry> internalWaitlist =
                (CopyOnWriteArrayList<WaitlistEntry>) wf.get(bm);
        internalWaitlist.clear();
        internalWaitlist.addAll(waitRepo.store);

        CaptureNotifier capture = new CaptureNotifier();

        try (MockedConstruction<EmailNotifier> mocked =
                     mockConstruction(EmailNotifier.class,
                             (mock, ctx) -> doAnswer(inv -> {
                                 capture.notify(
                                         inv.getArgument(0),
                                         inv.getArgument(1),
                                         inv.getArgument(2));
                                 return null;
                             }).when(mock).notify(any(), anyString(), anyString()))) {

            bm.returnItem(user, item);
        }

        // Optional.ifPresent should be false → no notification
        assertFalse(capture.notified);
        assertEquals(0, waitRepo.store.size());
    }

    // Utility / query tests -----------------------------------------------

    @Test
    void calculateTotalFines_returnsPositiveWhenOverdue() {
        BorrowManager bm = BorrowManager.getInstance();
        bm.borrowItem(user, item);

        BigDecimal total = bm.calculateTotalFines(user, LocalDate.now().plusDays(80));

        assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateTotalFines_ignoresReturnedAndOtherUsers() {
        BorrowManager bm = BorrowManager.getInstance();

        // Active record for main user
        bm.borrowItem(user, item);

        // Borrow another item for a different user
        User other = new User("X", Role.USER, "p", "x@mail.com");
        userRepo.users.add(other);
        FakeItem otherItem = new FakeItem();
        bm.borrowItem(other, otherItem);

        // Now return the main user's item so his record becomes returned
        try (MockedConstruction<EmailNotifier> mock =
                     Mockito.mockConstruction(EmailNotifier.class)) {
            bm.returnItem(user, item);
        }

        BigDecimal total = bm.calculateTotalFines(user, LocalDate.now().plusDays(80));

        // Returned records and records of other users must be ignored
        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void getOverdueItems_returnsListOfOverdueRecords() {
        BorrowManager bm = BorrowManager.getInstance();
        bm.borrowItem(user, item);

        List<BorrowRecord> list = bm.getOverdueItems(LocalDate.now().plusDays(100));

        assertEquals(1, list.size());
    }

    @Test
    void getBorrowRecordsForUser_returnsUserRecordsOnly() {
        BorrowManager bm = BorrowManager.getInstance();
        bm.borrowItem(user, item);

        List<BorrowRecord> list = bm.getBorrowRecordsForUser(user);

        assertEquals(1, list.size());
        assertEquals(user, list.get(0).getUser());
    }

    @Test
    void applyOverdueFines_onEmptyListDoesNotThrow() {
        BorrowManager bm = BorrowManager.getInstance();
        assertDoesNotThrow(() -> bm.applyOverdueFines(LocalDate.now()));
    }

    // Singleton guards ----------------------------------------------------

    @Test
    void getInstance_failsWhenNotInitialized() throws Exception {
        Field f = BorrowManager.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);

        assertThrows(IllegalStateException.class, BorrowManager::getInstance);
    }

    @Test
    void init_doesNotReinitializeWhenInstanceExists() throws Exception {
        // Instance already created in @BeforeEach via init()
        BorrowManager first = BorrowManager.getInstance();

        FakeBorrowRepo newBorrowRepo = new FakeBorrowRepo();
        FakeWaitlistRepo newWaitlistRepo = new FakeWaitlistRepo();

        // Call init again with different repositories
        BorrowManager second = BorrowManager.init(newBorrowRepo, newWaitlistRepo);

        // Same singleton instance should be returned
        assertSame(first, second);

        // Internal repositories must still be the original ones
        Field borrowField = BorrowManager.class.getDeclaredField("borrowRepo");
        borrowField.setAccessible(true);
        Object internalBorrow = borrowField.get(second);
        assertSame(borrowRepo, internalBorrow);

        Field waitlistField = BorrowManager.class.getDeclaredField("waitlistRepo");
        waitlistField.setAccessible(true);
        Object internalWait = waitlistField.get(second);
        assertSame(waitRepo, internalWait);
    }

    // Copy views tests ----------------------------------------------------

    @Test
    void getAllBorrowRecords_returnsUnmodifiableCopy() {
        BorrowManager bm = BorrowManager.getInstance();
        bm.borrowItem(user, item);  // create one record

        List<BorrowRecord> all = bm.getAllBorrowRecords();

        assertEquals(1, all.size());
        assertThrows(UnsupportedOperationException.class, () -> all.add(null));
    }

    @Test
    void getWaitlist_returnsUnmodifiableCopy() {
        BorrowManager bm = BorrowManager.getInstance();

        // Make item unavailable so the user is added to waitlist
        item.available = false;
        boolean borrowed = bm.borrowItem(user, item);
        assertFalse(borrowed);

        List<WaitlistEntry> list = bm.getWaitlist();

        assertEquals(1, list.size());
        assertThrows(UnsupportedOperationException.class, list::clear);
    }
    @Test
    void returnItem_notifiesWaitlistUserAndClearsEntry() throws Exception {
        Field f0 = BorrowManager.class.getDeclaredField("instance");
        f0.setAccessible(true);
        f0.set(null, null);
        BorrowManager bm = BorrowManager.init(borrowRepo, waitRepo);

        userRepo.users.add(user);
        User u2 = new User("B", Role.USER, "p", "b@ps.com");
        userRepo.users.add(u2);
        UserManager.init(userRepo);

        bm.borrowItem(user, item);

        waitRepo.store.add(new WaitlistEntry(item.getId(), "b@ps.com", LocalDate.now()));

        Field wf = BorrowManager.class.getDeclaredField("waitlist");
        wf.setAccessible(true);
        @SuppressWarnings("unchecked")
        CopyOnWriteArrayList<WaitlistEntry> internalWaitlist =
                (CopyOnWriteArrayList<WaitlistEntry>) wf.get(bm);
        internalWaitlist.clear();
        internalWaitlist.addAll(waitRepo.store);

        CaptureNotifier capture = new CaptureNotifier();

        try (MockedConstruction<EmailNotifier> mocked =
                     mockConstruction(EmailNotifier.class,
                             (mock, ctx) -> doAnswer(inv -> {
                                 capture.notify(
                                         inv.getArgument(0),
                                         inv.getArgument(1),
                                         inv.getArgument(2));
                                 return null;
                             }).when(mock).notify(any(), anyString(), anyString()))) {

            bm.returnItem(user, item);
        }

        assertTrue(capture.notified);
        assertEquals("b@ps.com", capture.target.getEmail());

        assertEquals("The item \"" + item.getTitle() + "\" is now available!", capture.subject);
        assertEquals(
                "Good news! The item \"" + item.getTitle() + "\" you requested is now available for borrowing.",
                capture.body
        );

        assertEquals(0, waitRepo.store.size());
    }

}
