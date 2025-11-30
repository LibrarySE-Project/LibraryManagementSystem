package librarySE.search;

import librarySE.core.LibraryItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KeywordSearchStrategyTest {

    private final KeywordSearchStrategy strategy = new KeywordSearchStrategy();

    @Test
    void matches_throwsIfItemNull() {
        assertThrows(IllegalArgumentException.class, () ->
                strategy.matches(null, "AI")
        );
    }

    @Test
    void matches_throwsIfKeywordEmpty() {
        LibraryItem item = mock(LibraryItem.class);
        assertThrows(IllegalArgumentException.class, () ->
                strategy.matches(item, "")
        );
    }

    @Test
    void matches_delegatesToItemMatchesKeyword() {
        LibraryItem item = mock(LibraryItem.class);
        when(item.matchesKeyword("java")).thenReturn(true);

        boolean result = strategy.matches(item, "java");
        assertTrue(result);
    }

    @Test
    void matches_returnsFalseWhenItemReturnsFalse() {
        LibraryItem item = mock(LibraryItem.class);
        when(item.matchesKeyword("oop")).thenReturn(false);

        boolean result = strategy.matches(item, "oop");
        assertFalse(result);
    }
}

