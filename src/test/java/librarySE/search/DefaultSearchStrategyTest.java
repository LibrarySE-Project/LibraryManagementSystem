package librarySE.search;

import librarySE.core.LibraryItem;
import librarySE.utils.ValidationUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultSearchStrategyTest {

    private final DefaultSearchStrategy strategy = new DefaultSearchStrategy();

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
    void matches_trueWhenKeywordFoundInToString() {
        LibraryItem item = mock(LibraryItem.class);
        when(item.toString()).thenReturn("Clean Code by Robert Martin");

        boolean result = strategy.matches(item, "clean");
        assertTrue(result);
    }

    @Test
    void matches_falseWhenKeywordNotFound() {
        LibraryItem item = mock(LibraryItem.class);
        when(item.toString()).thenReturn("Algorithms");

        boolean result = strategy.matches(item, "java");
        assertFalse(result);
    }
}
