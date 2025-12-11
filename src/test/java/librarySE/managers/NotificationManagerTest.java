package librarySE.managers;

import librarySE.managers.notifications.Notifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;
import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationManagerTest {

    private BorrowManager borrowManager;
    private NotificationManager notificationManager;
    private Notifier notifier;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        borrowManager = mock(BorrowManager.class);
        notificationManager = new NotificationManager(borrowManager);
        notifier = mock(Notifier.class);
        date = LocalDate.of(2025, 1, 1);
    }

    // Constructor tests

    @Test
    void constructor_nullBorrowManager_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new NotificationManager(null));
    }

    // sendReminders argument validation

    @Test
    void sendReminders_nullNotifier_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> notificationManager.sendReminders(null, date));
    }

    @Test
    void sendReminders_nullDate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> notificationManager.sendReminders(notifier, null));
    }

    // Behavior when there are no overdue items

    @Test
    void sendReminders_noOverdueItems_doesNotSendAnyNotification() {
        when(borrowManager.getOverdueItems(date)).thenReturn(Collections.emptyList());

        notificationManager.sendReminders(notifier, date);

        verify(borrowManager).getOverdueItems(date);
        verifyNoInteractions(notifier);
    }

    // Single user, multiple overdue items -> one notification with correct count

    @Test
    void sendReminders_singleUserMultipleOverdue_sendsOneNotificationWithCorrectCount() {
        User user = mock(User.class);

        BorrowRecord r1 = mock(BorrowRecord.class);
        BorrowRecord r2 = mock(BorrowRecord.class);

        when(r1.getUser()).thenReturn(user);
        when(r2.getUser()).thenReturn(user);

        when(borrowManager.getOverdueItems(date)).thenReturn(List.of(r1, r2));

        notificationManager.sendReminders(notifier, date);

        String expectedSubject = "📚 Reminder: You have overdue library items!";
        String expectedMessage = "You have 2 overdue item(s) as of " + date + ".";

        verify(borrowManager).getOverdueItems(date);
        verify(notifier).notify(user, expectedSubject, expectedMessage);
        verifyNoMoreInteractions(notifier);
    }

    // Multiple users with different overdue counts

    @Test
    void sendReminders_multipleUsers_sendsOneNotificationPerUserWithProperCounts() {
        User user1 = mock(User.class);
        User user2 = mock(User.class);

        BorrowRecord r1 = mock(BorrowRecord.class);
        BorrowRecord r2 = mock(BorrowRecord.class);
        BorrowRecord r3 = mock(BorrowRecord.class);

        when(r1.getUser()).thenReturn(user1);
        when(r2.getUser()).thenReturn(user1);
        when(r3.getUser()).thenReturn(user2);

        when(borrowManager.getOverdueItems(date)).thenReturn(List.of(r1, r2, r3));

        notificationManager.sendReminders(notifier, date);

        String expectedSubject = "📚 Reminder: You have overdue library items!";
        String expectedMsgUser1 = "You have 2 overdue item(s) as of " + date + ".";
        String expectedMsgUser2 = "You have 1 overdue item(s) as of " + date + ".";

        verify(borrowManager).getOverdueItems(date);

        verify(notifier).notify(user1, expectedSubject, expectedMsgUser1);
        verify(notifier).notify(user2, expectedSubject, expectedMsgUser2);
        verifyNoMoreInteractions(notifier);
    }
}
