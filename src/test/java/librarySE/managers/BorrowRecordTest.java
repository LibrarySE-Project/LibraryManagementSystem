package librarySE.managers;

import librarySE.core.*;
import librarySE.strategy.FineStrategy;
import librarySE.strategy.FineStrategyFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BorrowRecordTest {

    User user;
    LibraryItem item;
    FineStrategy strategy;
    LocalDate borrowDate;
    BorrowRecord record;

    @BeforeEach
    void setup() {
        user = new User("M", Role.USER, "pass123", "m@ps.com");
        item = new Book("ISBN", "Title", "Author", BigDecimal.TEN);
        strategy = FineStrategyFactory.book();
        borrowDate = LocalDate.of(2025, 1, 1);

        record = new BorrowRecord(user, item, strategy, borrowDate);
    }

    // Constructor Tests
    @Test
    void constructor_validInputs_success() {
        assertEquals(user, record.getUser());
        assertEquals(item, record.getItem());
        assertEquals(borrowDate, record.getBorrowDate());
        assertEquals(borrowDate.plusDays(strategy.getBorrowPeriodDays()), record.getDueDate());
        assertEquals(BorrowRecord.Status.BORROWED, record.getStatus());
    }

    @Test
    void constructor_nullUser_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new BorrowRecord(null, item, strategy, borrowDate));
    }

    @Test
    void constructor_nullItem_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new BorrowRecord(user, null, strategy, borrowDate));
    }

    @Test
    void constructor_nullStrategy_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new BorrowRecord(user, item, null, borrowDate));
    }

    @Test
    void constructor_nullDate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new BorrowRecord(user, item, strategy, null));
    }

    // Fine Calculation Tests
    @Test
    void calculateFine_notOverdue_zeroFine() {
        record.calculateFine(borrowDate.plusDays(1));
        assertEquals(BigDecimal.ZERO, record.getFine(borrowDate.plusDays(1)));
    }

    @Test
    void calculateFine_overdue_positiveFine() {
        LocalDate late = borrowDate.plusDays(strategy.getBorrowPeriodDays() + 5);
        record.calculateFine(late);
        assertTrue(record.getFine(late).compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateFine_nullDate_throws() {
        assertThrows(IllegalArgumentException.class, () -> record.calculateFine(null));
    }

    // Fine Payment Tests
    @Test
    void setFinePaid_validAmount_success() {
        LocalDate late = borrowDate.plusDays(50);
        record.calculateFine(late);
        BigDecimal fine = record.getFine(late);

        record.setFinePaid(fine);

        assertEquals(BigDecimal.ZERO, record.getRemainingFine());
    }

    @Test
    void setFinePaid_negative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> record.setFinePaid(BigDecimal.valueOf(-5)));
    }

    @Test
    void setFinePaid_exceedsFine_throws() {
        LocalDate late = borrowDate.plusDays(50);
        record.calculateFine(late);

        assertThrows(IllegalArgumentException.class,
                () -> record.setFinePaid(BigDecimal.valueOf(9999)));
    }

    // applyFineToUser Tests
    @Test
    void applyFineToUser_appliesFineOnce() {
        LocalDate late = borrowDate.plusDays(100);

        record.applyFineToUser(late);
        BigDecimal first = user.getFineBalance();

        record.applyFineToUser(late);
        BigDecimal second = user.getFineBalance();

        assertEquals(first, second);
    }

    // markReturned Tests
    @Test
    void markReturned_updatesStatusAndReturnsItem() {
        LocalDate late = borrowDate.plusDays(40);

        record.markReturned(late);

        assertTrue(record.isReturned());
        assertTrue(item.isAvailable());
    }

    // isOverdue Tests
    @Test
    void isOverdue_returnsTrueWhenLate() {
        LocalDate late = borrowDate.plusDays(100);
        assertTrue(record.isOverdue(late));
    }

    @Test
    void isOverdue_returnsFalseWhenNotLate() {
        assertFalse(record.isOverdue(borrowDate.plusDays(1)));
    }

    @Test
    void isOverdue_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> record.isOverdue(null));
    }

    // toString Test
    @Test
    void toString_notNull() {
        assertNotNull(record.toString());
    }
}

