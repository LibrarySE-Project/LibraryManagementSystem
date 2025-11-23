package librarySE.search;

import librarySE.core.LibraryItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TitleSearchStrategyTest {

    private final TitleSearchStrategy strategy = new TitleSearchStrategy();

    @Test
    void matches_throwsIfItemNull() {
        assertThrows(IllegalArgumentException.class, () ->
                strategy.matches(null, "java")
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
    void matches_returnsTrueIfTitleContainsKeyword() {
        LibraryItem item = mock(LibraryItem.class);
        when(item.getTitle()).thenReturn("Java Programming");

        boolean result = strategy.matches(item, "java");
        assertTrue(result);
    }

    @Test
    void matches_returnsFalseIfTitleDoesNotContainKeyword() {
        LibraryItem item = mock(LibraryItem.class);
        when(item.getTitle()).thenReturn("Data Structures");

        boolean result = strategy.matches(item, "java");
        assertFalse(result);
    }

    @Test
    void matches_returnsFalseIfTitleIsNull() {
        LibraryItem item = mock(LibraryItem.class);
        when(item.getTitle()).thenReturn(null);

        boolean result = strategy.matches(item, "java");
        assertFalse(result);
    }
}

