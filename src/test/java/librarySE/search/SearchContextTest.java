package librarySE.search;

import librarySE.core.LibraryItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchContextTest {

    @Test
    void constructor_throwsIfStrategyNull() {
        assertThrows(NullPointerException.class, () ->
                new SearchContext(null)
        );
    }

    @Test
    void setStrategy_throwsIfNull() {
        SearchContext context = new SearchContext(mock(SearchStrategy.class));
        assertThrows(NullPointerException.class, () ->
                context.setStrategy(null)
        );
    }

    @Test
    void search_returnsEmptyListIfItemsNull() {
        SearchContext context = new SearchContext(mock(SearchStrategy.class));
        List<LibraryItem> result = context.search(null, "java");
        assertTrue(result.isEmpty());
    }

    @Test
    void search_returnsEmptyListIfKeywordNull() {
        SearchContext context = new SearchContext(mock(SearchStrategy.class));
        List<LibraryItem> result = context.search(List.of(), null);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_usesStrategyToFilterItems() {
        SearchStrategy strategy = mock(SearchStrategy.class);
        SearchContext context = new SearchContext(strategy);

        LibraryItem item1 = mock(LibraryItem.class);
        LibraryItem item2 = mock(LibraryItem.class);

        when(strategy.matches(item1, "java")).thenReturn(true);
        when(strategy.matches(item2, "java")).thenReturn(false);

        List<LibraryItem> result = context.search(List.of(item1, item2), "java");

        assertEquals(1, result.size());
        assertEquals(item1, result.get(0));
    }
}

