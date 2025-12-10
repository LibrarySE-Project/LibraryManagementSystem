package librarySE.managers;

import librarySE.core.LibraryItem;
import librarySE.core.MaterialType;
import librarySE.core.WaitlistEntry;
import librarySE.managers.notifications.EmailNotifier;
import librarySE.repo.BorrowRecordRepository;
import librarySE.repo.WaitlistRepository;
import librarySE.strategy.FineStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Exhaustive test suite for {@link BorrowManager}.
 *
 * Covers:
 * - Singleton init/getInstance
 * - borrowItem (success + all negative/edge cases)
 * - returnItem (success + negative cases)
 * - applyOverdueFines
 * - payFineForUser (success + invalid args)
 * - calculateTotalFines
 * - getOverdueItems
 * - getBorrowRecordsForUser
 * - getAllBorrowRecords
 * - getWaitlist
 */
class BorrowManagerTest {

    // --------------------------------------------------------------------
    // Fake in-memory repositories
    // --------------------------------------------------------------------

    static class FakeBorrowRepo implements BorrowRecordRepository {
        CopyOnWriteArrayList<BorrowRecord> store = new CopyOnWriteArrayList<>();

        @Override
        public List<BorrowRecord> loadAll() {
            return List.copyOf(store);
        }

        @Override
        public void saveAll(List<BorrowRecord> records) {
            store.clear();
            store.addAll(records);
        }
    }

    static class FakeWaitlistRepo implements WaitlistRepository {
        CopyOnWriteArrayList<WaitlistEntry> store = new CopyOnWriteArrayList<>();

        @Override
        public List<WaitlistEntry> loadAll() {
            return List.copyOf(store);
        }

        @Override
        public void saveAll(List<WaitlistEntry> entries) {
            store.clear();
            store.addAll(entries);
        }
    }

    private FakeBorrowRepo borrowRepo;
    private FakeWaitlistRepo waitlistRepo;
    private ItemManager itemManager; // mocked
    private BorrowManager borrowManager;

    // --------------------------------------------------------------------
    // Helper to reset the Singleton between tests
    // --------------------------------------------------------------------

    private static void resetBorrowManagerSingleton() {
        try {
            Field f = BorrowManager.class.getDeclaredField("instance");
            f.setAccessible(true);
            f.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset BorrowManager.instance", e);
        }
    }

    @BeforeEach
    void setUp() {
        resetBorrowManagerSingleton();
        borrowRepo = new FakeBorrowRepo();
        waitlistRepo = new FakeWaitlistRepo();
        itemManager = mock(ItemManager.class);
        // We do not init here; each test decides when to init depending on pre-populated data
    }

    // --------------------------------------------------------------------
    // init / getInstance
    // --------------------------------------------------------------------

    @Test
    void getInstance_beforeInit_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, BorrowManager::getInstance);
    }

    @Test
    void init_returnsSingleton_sameInstanceEveryTime() {
        BorrowManager first = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);
        BorrowManager second = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        assertSame(first, second);
        assertSame(first, BorrowManager.getInstance());
    }

    // --------------------------------------------------------------------
    // borrowItem – argument validation
    // --------------------------------------------------------------------

    @Test
    void borrowItem_nullUser_throwsIllegalArgument() {
        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        LibraryItem item = mock(LibraryItem.class);
        assertThrows(IllegalArgumentException.class,
                () -> borrowManager.borrowItem(null, item));
    }

    @Test
    void borrowItem_nullItem_throwsIllegalArgument() {
        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        User user = mock(User.class);
        assertThrows(IllegalArgumentException.class,
                () -> borrowManager.borrowItem(user, null));
    }

    // --------------------------------------------------------------------
    // borrowItem – user has unpaid fines
    // --------------------------------------------------------------------

    @Test
    void borrowItem_userHasOutstandingFine_throwsIllegalState() {
        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        User user = mock(User.class);
        LibraryItem item = mock(LibraryItem.class);

        when(user.hasOutstandingFine()).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> borrowManager.borrowItem(user, item));

        verify(item, never()).isAvailable();
    }

    // --------------------------------------------------------------------
    // borrowItem – user has overdue items
    // --------------------------------------------------------------------

    @Test
    void borrowItem_userHasOverdueItem_throwsIllegalState() {
        // Prepare an overdue record for this user before init
        User user = mock(User.class);
        LibraryItem dummyItem = mock(LibraryItem.class);

        BorrowRecord overdue = mock(BorrowRecord.class);
        when(overdue.getUser()).thenReturn(user);
        when(overdue.isOverdue(any(LocalDate.class))).thenReturn(true);
        borrowRepo.store.add(overdue);

        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        when(user.hasOutstandingFine()).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> borrowManager.borrowItem(user, dummyItem));

        verify(dummyItem, never()).isAvailable();
    }

    // --------------------------------------------------------------------
    // borrowItem – item unavailable → waitlist
    // --------------------------------------------------------------------

    @Test
    void borrowItem_itemUnavailable_addsToWaitlistAndReturnsFalse() {
        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        User user = mock(User.class);
        when(user.hasOutstandingFine()).thenReturn(false);
        when(user.getEmail()).thenReturn("user@test.com");

        LibraryItem item = mock(LibraryItem.class);
        UUID itemId = UUID.randomUUID();
        when(item.getId()).thenReturn(itemId);
        when(item.isAvailable()).thenReturn(false);

        boolean result = borrowManager.borrowItem(user, item);

        assertFalse(result, "Expected borrowItem to return false when item is unavailable.");
        assertEquals(1, waitlistRepo.store.size(), "User should be added to waitlist.");

        WaitlistEntry entry = waitlistRepo.store.get(0);
        assertEquals(itemId, entry.getItemId());
        assertEquals("user@test.com", entry.getUserEmail());
    }

    // --------------------------------------------------------------------
    // borrowItem – success path
    // --------------------------------------------------------------------

    @Test
    void borrowItem_success_createsRecord_savesAndPersistsItems() {
        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        User user = mock(User.class);
        when(user.hasOutstandingFine()).thenReturn(false);
        when(user.getUsername()).thenReturn("lian");
        when(user.getId()).thenReturn(UUID.randomUUID());

        LibraryItem item = mock(LibraryItem.class);
        UUID itemId = UUID.randomUUID();
        when(item.getId()).thenReturn(itemId);
        when(item.getTitle()).thenReturn("Some Book");
        when(item.isAvailable()).thenReturn(true);
        when(item.borrow()).thenReturn(true);

        MaterialType materialType = mock(MaterialType.class);
        FineStrategy fineStrategy = mock(FineStrategy.class);
        when(materialType.createFineStrategy()).thenReturn(fineStrategy);
        when(fineStrategy.getBorrowPeriodDays()).thenReturn(14);
        when(item.getMaterialType()).thenReturn(materialType);

        boolean result = borrowManager.borrowItem(user, item);

        assertTrue(result, "Expected successful borrow to return true.");
        assertEquals(1, borrowRepo.store.size(), "Borrow repo should contain one record.");

        BorrowRecord record = borrowRepo.store.get(0);
        assertEquals(user, record.getUser());
        assertEquals(item, record.getItem());

        verify(item).borrow();
        verify(itemManager).saveAll();
    }

    // --------------------------------------------------------------------
    // returnItem – argument validation
    // --------------------------------------------------------------------

    @Test
    void returnItem_nullUser_throwsIllegalArgument() {
        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        LibraryItem item = mock(LibraryItem.class);
        assertThrows(IllegalArgumentException.class,
                () -> borrowManager.returnItem(null, item));
    }

    @Test
    void returnItem_nullItem_throwsIllegalArgument() {
        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        User user = mock(User.class);
        assertThrows(IllegalArgumentException.class,
                () -> borrowManager.returnItem(user, null));
    }

    // --------------------------------------------------------------------
    // returnItem – no active record found
    // --------------------------------------------------------------------

    @Test
    void returnItem_noActiveBorrow_throwsIllegalArgument() {
        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());

        LibraryItem item = mock(LibraryItem.class);
        when(item.getId()).thenReturn(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class,
                () -> borrowManager.returnItem(user, item));
    }

    // --------------------------------------------------------------------
    // returnItem – success path (with waitlist + email notifications)
    // --------------------------------------------------------------------

    @Test
    void returnItem_success_updatesRecordItemWaitlistAndSendsEmails() {
        // Prepare data before init
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        User borrower = mock(User.class);
        when(borrower.getId()).thenReturn(userId);

        LibraryItem item = mock(LibraryItem.class);
        when(item.getId()).thenReturn(itemId);
        when(item.getTitle()).thenReturn("Test Item");

        BorrowRecord record = mock(BorrowRecord.class);
        when(record.getUser()).thenReturn(borrower);
        when(record.getItem()).thenReturn(item);
        when(record.isReturned()).thenReturn(false);

        borrowRepo.store.add(record);

        WaitlistEntry w1 = new WaitlistEntry(itemId, "a@example.com",
                LocalDate.now().minusDays(2));
        WaitlistEntry w2 = new WaitlistEntry(itemId, "b@example.com",
                LocalDate.now().minusDays(1));
        waitlistRepo.store.add(w1);
        waitlistRepo.store.add(w2);

        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        User userA = mock(User.class);
        when(userA.getEmail()).thenReturn("a@example.com");
        when(userA.getUsername()).thenReturn("User A");

        User userB = mock(User.class);
        when(userB.getEmail()).thenReturn("b@example.com");
        when(userB.getUsername()).thenReturn("User B");

        try (MockedStatic<UserManager> userManagerStatic = Mockito.mockStatic(UserManager.class);
             MockedConstruction<EmailNotifier> emailNotifierConstruction =
                     Mockito.mockConstruction(EmailNotifier.class, (mock, context) -> {})) {

            UserManager um = mock(UserManager.class);
            userManagerStatic.when(UserManager::getInstance).thenReturn(um);
            when(um.findUserByEmail("a@example.com")).thenReturn(Optional.of(userA));
            when(um.findUserByEmail("b@example.com")).thenReturn(Optional.of(userB));

            borrowManager.returnItem(borrower, item);

            // Record was marked as returned
            verify(record).markReturned(any(LocalDate.class));

            // Item was returned and repositories saved
            verify(item).returnItem();
            verify(itemManager).saveAll();
            assertEquals(1, borrowRepo.store.size(), "Record list size unchanged (only status updated).");

            // Waitlist for this item should be cleared
            assertTrue(waitlistRepo.store.isEmpty(), "Waitlist should be cleared after item is returned.");

            // Verify notification emails
            EmailNotifier constructed = emailNotifierConstruction.constructed().get(0);
            verify(constructed, times(2))
                    .notify(any(User.class),
                            contains("Test Item"),
                            contains("Test Item"));
        }
    }

    // --------------------------------------------------------------------
    // applyOverdueFines
    // --------------------------------------------------------------------

    @Test
    void applyOverdueFines_callsApplyFineToUserForOverdueRecords() {
        BorrowRecord overdue = mock(BorrowRecord.class);
        BorrowRecord notOverdue = mock(BorrowRecord.class);

        borrowRepo.store.add(overdue);
        borrowRepo.store.add(notOverdue);

        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        LocalDate today = LocalDate.now();

        when(overdue.isOverdue(today)).thenReturn(true);
        when(notOverdue.isOverdue(today)).thenReturn(false);

        borrowManager.applyOverdueFines(today);

        verify(overdue).applyFineToUser(today);
        verify(notOverdue, never()).applyFineToUser(any(LocalDate.class));
        assertEquals(2, borrowRepo.store.size());
    }

    // --------------------------------------------------------------------
    // payFineForUser – argument validation
    // --------------------------------------------------------------------

    @Test
    void payFineForUser_nullUser_throwsIllegalArgument() {
        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        assertThrows(IllegalArgumentException.class,
                () -> borrowManager.payFineForUser(null, BigDecimal.TEN, LocalDate.now()));
    }

    @Test
    void payFineForUser_nonPositiveAmount_throwsIllegalArgument() {
        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        User user = mock(User.class);
        assertThrows(IllegalArgumentException.class,
                () -> borrowManager.payFineForUser(user, BigDecimal.ZERO, LocalDate.now()));
        assertThrows(IllegalArgumentException.class,
                () -> borrowManager.payFineForUser(user, BigDecimal.valueOf(-5), LocalDate.now()));
    }

    @Test
    void payFineForUser_nullDate_throwsIllegalArgument() {
        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        User user = mock(User.class);
        assertThrows(IllegalArgumentException.class,
                () -> borrowManager.payFineForUser(user, BigDecimal.TEN, null));
    }

    // --------------------------------------------------------------------
    // payFineForUser – distribute payment over records
    // --------------------------------------------------------------------

    @Test
    void payFineForUser_distributesPaymentAndCallsUserPayFine() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());

        // Prepare records before init
        BorrowRecord r1 = mock(BorrowRecord.class);
        BorrowRecord r2 = mock(BorrowRecord.class);
        BorrowRecord rOther = mock(BorrowRecord.class);

        when(r1.getUser()).thenReturn(user);
        when(r2.getUser()).thenReturn(user);
        when(rOther.getUser()).thenReturn(mock(User.class));

        when(r1.getRemainingFine()).thenReturn(BigDecimal.valueOf(5));
        when(r2.getRemainingFine()).thenReturn(BigDecimal.valueOf(10));
        when(r1.getFinePaid()).thenReturn(BigDecimal.ZERO);
        when(r2.getFinePaid()).thenReturn(BigDecimal.ZERO);

        borrowRepo.store.add(r1);
        borrowRepo.store.add(r2);
        borrowRepo.store.add(rOther);

        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        LocalDate date = LocalDate.now();

        borrowManager.payFineForUser(user, BigDecimal.valueOf(12), date);

        verify(r1).calculateFine(date);
        verify(r2).calculateFine(date);

        // r1 is fully paid (5), r2 partially (7)
        verify(r1).setFinePaid(BigDecimal.valueOf(5));
        verify(r2).setFinePaid(BigDecimal.valueOf(7));

        verify(user).payFine(BigDecimal.valueOf(12));
        assertEquals(3, borrowRepo.store.size());
    }

    // --------------------------------------------------------------------
    // getBorrowRecordsForUser
    // --------------------------------------------------------------------

    @Test
    void getBorrowRecordsForUser_filtersCorrectly() {
        User u1 = mock(User.class);
        User u2 = mock(User.class);

        BorrowRecord r1 = mock(BorrowRecord.class);
        BorrowRecord r2 = mock(BorrowRecord.class);
        BorrowRecord r3 = mock(BorrowRecord.class);

        borrowRepo.store.add(r1);
        borrowRepo.store.add(r2);
        borrowRepo.store.add(r3);

        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        when(r1.getUser()).thenReturn(u1);
        when(r2.getUser()).thenReturn(u2);
        when(r3.getUser()).thenReturn(u1);

        List<BorrowRecord> list = borrowManager.getBorrowRecordsForUser(u1);

        assertEquals(2, list.size());
        assertTrue(list.contains(r1));
        assertTrue(list.contains(r3));
    }

    // --------------------------------------------------------------------
    // calculateTotalFines
    // --------------------------------------------------------------------

    @Test
    void calculateTotalFines_sumsOnlyNotReturnedRecordsForUser() {
        User user = mock(User.class);

        BorrowRecord active1 = mock(BorrowRecord.class);
        BorrowRecord active2 = mock(BorrowRecord.class);
        BorrowRecord returned = mock(BorrowRecord.class);
        BorrowRecord otherUser = mock(BorrowRecord.class);

        borrowRepo.store.add(active1);
        borrowRepo.store.add(active2);
        borrowRepo.store.add(returned);
        borrowRepo.store.add(otherUser);

        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        LocalDate date = LocalDate.now();

        when(active1.getUser()).thenReturn(user);
        when(active2.getUser()).thenReturn(user);
        when(returned.getUser()).thenReturn(user);
        when(otherUser.getUser()).thenReturn(mock(User.class));

        when(active1.isReturned()).thenReturn(false);
        when(active2.isReturned()).thenReturn(false);
        when(returned.isReturned()).thenReturn(true);

        when(active1.getFine(date)).thenReturn(BigDecimal.valueOf(3));
        when(active2.getFine(date)).thenReturn(BigDecimal.valueOf(7));
        when(returned.getFine(date)).thenReturn(BigDecimal.valueOf(100)); // ignored

        BigDecimal total = borrowManager.calculateTotalFines(user, date);
        assertEquals(BigDecimal.valueOf(10), total);
    }

    // --------------------------------------------------------------------
    // getOverdueItems
    // --------------------------------------------------------------------

    @Test
    void getOverdueItems_returnsOnlyOverdueRecords() {
        BorrowRecord overdue1 = mock(BorrowRecord.class);
        BorrowRecord overdue2 = mock(BorrowRecord.class);
        BorrowRecord notOverdue = mock(BorrowRecord.class);

        borrowRepo.store.add(overdue1);
        borrowRepo.store.add(overdue2);
        borrowRepo.store.add(notOverdue);

        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        LocalDate date = LocalDate.now();

        when(overdue1.isOverdue(date)).thenReturn(true);
        when(overdue2.isOverdue(date)).thenReturn(true);
        when(notOverdue.isOverdue(date)).thenReturn(false);

        List<BorrowRecord> result = borrowManager.getOverdueItems(date);

        assertEquals(2, result.size());
        assertTrue(result.contains(overdue1));
        assertTrue(result.contains(overdue2));
    }

    // --------------------------------------------------------------------
    // getAllBorrowRecords & getWaitlist
    // --------------------------------------------------------------------

    @Test
    void getAllBorrowRecords_returnsUnmodifiableCopy() {
        BorrowRecord r1 = mock(BorrowRecord.class);
        BorrowRecord r2 = mock(BorrowRecord.class);

        borrowRepo.store.add(r1);
        borrowRepo.store.add(r2);

        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        List<BorrowRecord> all = borrowManager.getAllBorrowRecords();

        assertEquals(2, all.size());
        assertThrows(UnsupportedOperationException.class,
                () -> all.add(mock(BorrowRecord.class)));
    }

    @Test
    void getWaitlist_returnsUnmodifiableCopy() {
        UUID itemId1 = UUID.randomUUID();
        UUID itemId2 = UUID.randomUUID();

        WaitlistEntry e1 = new WaitlistEntry(itemId1, "a@example.com", LocalDate.now());
        WaitlistEntry e2 = new WaitlistEntry(itemId2, "b@example.com", LocalDate.now());

        waitlistRepo.store.add(e1);
        waitlistRepo.store.add(e2);

        borrowManager = BorrowManager.init(borrowRepo, waitlistRepo, itemManager);

        List<WaitlistEntry> result = borrowManager.getWaitlist();

        assertEquals(2, result.size());
        assertThrows(UnsupportedOperationException.class,
                () -> result.add(new WaitlistEntry(itemId1, "c@example.com", LocalDate.now())));
    }
}


