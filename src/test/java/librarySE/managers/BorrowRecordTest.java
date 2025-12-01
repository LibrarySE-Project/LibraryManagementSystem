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

    // ============================
    // Constructor Tests
    // ============================

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


    // ============================
    // Fine Calculation Tests
    // ============================

    @Test
    void calculateFine_notOverdue_zeroFine() {
        LocalDate early = borrowDate.plusDays(1);
        record.calculateFine(early);
        assertEquals(BigDecimal.ZERO, record.getFine(early));
    }

    @Test
    void calculateFine_atDueDate_zeroFine() {
        LocalDate due = record.getDueDate();
        record.calculateFine(due);
        assertEquals(BigDecimal.ZERO, record.getFine(due));
    }

    @Test
    void calculateFine_afterDue_positiveFine() {
        LocalDate late = borrowDate.plusDays(strategy.getBorrowPeriodDays() + 7);
        record.calculateFine(late);
        assertTrue(record.getFine(late).compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateFine_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> record.calculateFine(null));
    }

    @Test
    void calculateFine_returnedItem_fineAlwaysZero() {
        LocalDate late = borrowDate.plusDays(80);
        record.markReturned(late);
        assertEquals(BigDecimal.ZERO, record.getFine(late.plusDays(20)));
    }

    // ============================
    // setFinePaid Tests
    // ============================

    @Test
    void setFinePaid_exactFine_success() {
        LocalDate late = borrowDate.plusDays(50);

        // 1) Calculate fine once
        record.calculateFine(late);
        BigDecimal fine = record.getFine(late);

        // 2) Pay full fine
        record.setFinePaid(fine);

        // 3) Assert remainingFine == 0 using compareTo
        assertEquals(0, record.getRemainingFine().compareTo(BigDecimal.ZERO));
    }


    @Test
    void setFinePaid_zero_success() {
        LocalDate late = borrowDate.plusDays(50);
        record.calculateFine(late);

        record.setFinePaid(BigDecimal.ZERO);

        assertEquals(record.getFine(late), record.getRemainingFine());
    }

    @Test
    void setFinePaid_negative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> record.setFinePaid(BigDecimal.valueOf(-10)));
    }

    @Test
    void setFinePaid_exceedsFine_throws() {
        LocalDate late = borrowDate.plusDays(80);
        record.calculateFine(late);

        assertThrows(IllegalArgumentException.class,
                () -> record.setFinePaid(BigDecimal.valueOf(99999)));
    }

    @Test
    void setFinePaid_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> record.setFinePaid(null));
    }


    // ============================
    // applyFineToUser Tests
    // ============================

    @Test
    void applyFineToUser_appliesOnce() {
        LocalDate late = borrowDate.plusDays(100);

        record.applyFineToUser(late);
        BigDecimal first = user.getFineBalance();

        record.applyFineToUser(late);
        BigDecimal second = user.getFineBalance();

        assertEquals(first, second);
    }

    @Test
    void applyFineToUser_zeroFine_notApplied() {
        LocalDate early = borrowDate.plusDays(1);
        record.applyFineToUser(early);
        assertEquals(BigDecimal.ZERO, user.getFineBalance());
    }

    @Test
    void applyFineToUser_returnedItem_notApplied() {
        LocalDate late = borrowDate.plusDays(100);
        record.markReturned(late);
        BigDecimal amount = user.getFineBalance();

        record.applyFineToUser(late.plusDays(10));
        assertEquals(amount, user.getFineBalance());
    }


    // ============================
    // markReturned Tests
    // ============================

    @Test
    void markReturned_updatesStatusAndAvailability() {
        LocalDate late = borrowDate.plusDays(40);
        record.markReturned(late);

        assertTrue(record.isReturned());
        assertTrue(item.isAvailable());
    }

    @Test
    void markReturned_appliesFineOnce() {
        LocalDate late = borrowDate.plusDays(90);

        record.markReturned(late);
        BigDecimal amt1 = user.getFineBalance();

        record.markReturned(late.plusDays(4));
        BigDecimal amt2 = user.getFineBalance();

        assertEquals(amt1, amt2);
    }


    // ============================
    // isOverdue Tests
    // ============================

    @Test
    void isOverdue_trueWhenLate() {
        LocalDate late = borrowDate.plusDays(200);
        assertTrue(record.isOverdue(late));
    }

    @Test
    void isOverdue_falseWhenNotLate() {
        LocalDate early = borrowDate.plusDays(1);
        assertFalse(record.isOverdue(early));
    }

    @Test
    void isOverdue_falseWhenReturned() {
        LocalDate late = borrowDate.plusDays(100);
        record.markReturned(late);
        assertFalse(record.isOverdue(late.plusDays(10)));
    }

    @Test
    void isOverdue_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> record.isOverdue(null));
    }


    // ============================
    // toString Test
    // ============================

    @Test
    void toString_containsFields() {
        String s = record.toString();
        assertTrue(s.contains("borrowed"));
        assertTrue(s.contains("Status"));
        assertTrue(s.contains(item.getTitle()));
        assertTrue(s.contains(user.getUsername()));
    }
 // ============================
 // isFineApplied() Tests
 // ============================

 @Test
 void testIsFineApplied_initiallyFalse() {
     assertFalse(record.isFineApplied());
 }

 @Test
 void testIsFineApplied_afterApplyFine_true() {
     LocalDate late = borrowDate.plusDays(100);

     record.applyFineToUser(late);

     assertTrue(record.isFineApplied());
 }


 // ============================
 // getFinePaid() Tests
 // ============================

 @Test
 void testGetFinePaid_initiallyZero() {
     assertEquals(BigDecimal.ZERO, record.getFinePaid());
 }

 @Test
 void testGetFinePaid_afterPayingFine() {
     LocalDate late = borrowDate.plusDays(80);
     record.calculateFine(late);

     BigDecimal fine = record.getFine(late);
     record.setFinePaid(fine);

     assertEquals(fine, record.getFinePaid());
 }

}
