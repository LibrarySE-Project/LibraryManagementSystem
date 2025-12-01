package librarySE.managers;

import librarySE.core.*;
import librarySE.managers.notifications.Notifier;
import librarySE.repo.BorrowRecordRepository;
import librarySE.repo.WaitlistRepository;
import librarySE.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class NotificationManagerTest {

    // Fake borrow repo
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

    // Fake waitlist repo
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

    // Fake user repo
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

    // Fake notifier (captures calls)
    static class FakeNotifier implements Notifier {
        List<String> sent = new ArrayList<>();

        @Override
        public void notify(User user, String subject, String message) {
            sent.add(user.getUsername() + "|" + message);
        }
    }

    FakeBorrowRepo borrowRepo;
    FakeWaitlistRepo waitRepo;
    FakeUserRepo userRepo;

    BorrowManager bm;
    NotificationManager nm;

    User user;
    LibraryItem book;

    @BeforeEach
    void setup() throws Exception {

        // Reset BorrowManager singleton
        var f1 = BorrowManager.class.getDeclaredField("instance");
        f1.setAccessible(true);
        f1.set(null, null);

        // Reset UserManager singleton
        var f2 = UserManager.class.getDeclaredField("instance");
        f2.setAccessible(true);
        f2.set(null, null);

        borrowRepo = new FakeBorrowRepo();
        waitRepo = new FakeWaitlistRepo();
        userRepo = new FakeUserRepo();

        // init user manager
        UserManager.init(userRepo);
        user = new User("Malak", Role.USER, "pass123", "m@ps.com");
        userRepo.users.add(user);

        // item
        book = new Book("ISBN", "Title", "Author", BigDecimal.TEN);

        // init borrow manager
        BorrowManager.init(borrowRepo, waitRepo);
        bm = BorrowManager.getInstance();

        nm = new NotificationManager(bm);
    }

    // sendReminders sends notification if overdue
    @Test
    void testSendReminderForOverdueItem() {
        bm.borrowItem(user, book);

        LocalDate overdueDate = LocalDate.now().plusDays(50);

        FakeNotifier notifier = new FakeNotifier();

        nm.sendReminders(notifier, overdueDate);

        assertEquals(1, notifier.sent.size());
    }

    // sendReminders sends nothing if no overdue items
    @Test
    void testNoRemindersWhenNothingOverdue() {
        FakeNotifier notifier = new FakeNotifier();

        nm.sendReminders(notifier, LocalDate.now());

        assertEquals(0, notifier.sent.size());
    }

    // sendReminders rejects null notifier
    @Test
    void testNotifierNullThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> nm.sendReminders(null, LocalDate.now()));
    }

    // sendReminders rejects null date
    @Test
    void testDateNullThrows() {
        FakeNotifier notifier = new FakeNotifier();

        assertThrows(IllegalArgumentException.class,
                () -> nm.sendReminders(notifier, null));
    }
    @Test
    void testConstructorThrowsWhenBorrowManagerNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new NotificationManager(null));
    }

}

