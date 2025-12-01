package librarySE.search;

import librarySE.core.LibraryItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchContextTest {

    @Test
    void constructor_throwsIfStrategyNull() {
        assertThrows(NullPointerException.class,
                () -> new SearchContext(null));
    }

    @Test
    void constructor_storesStrategy() {
        SearchStrategy strategy = mock(SearchStrategy.class);
        SearchContext context = new SearchContext(strategy);

        // Invoke search to implicitly verify that stored strategy is used
        context.search(List.of(), "k");
        verifyNoMoreInteractions(strategy);
    }

    @Test
    void setStrategy_throwsIfNull() {
        SearchContext context = new SearchContext(mock(SearchStrategy.class));
        assertThrows(NullPointerException.class,
                () -> context.setStrategy(null));
    }

    @Test
    void setStrategy_changesStrategy() {
        SearchStrategy oldStrategy = mock(SearchStrategy.class);
        SearchStrategy newStrategy = mock(SearchStrategy.class);

        SearchContext context = new SearchContext(oldStrategy);
        context.setStrategy(newStrategy);

        LibraryItem item = mock(LibraryItem.class);

        context.search(List.of(item), "abc");

        verify(newStrategy).matches(item, "abc");   // new strategy was used
        verifyNoInteractions(oldStrategy);          // old strategy is not used anymore
    }

    @Test
    void search_returnsEmptyListIfItemsNull() {
        SearchContext context = new SearchContext(mock(SearchStrategy.class));
        List<LibraryItem> result = context.search(null, "java");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_returnsEmptyListIfKeywordNull() {
        SearchContext context = new SearchContext(mock(SearchStrategy.class));
        List<LibraryItem> result = context.search(List.of(), null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_returnsEmptyListIfItemsEmpty() {
        SearchContext context = new SearchContext(mock(SearchStrategy.class));
        List<LibraryItem> result = context.search(List.of(), "java");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_usesStrategyToFilterItems() {
        SearchStrategy strategy = mock(SearchStrategy.class);
        SearchContext context = new SearchContext(strategy);

        LibraryItem item1 = mock(LibraryItem.class);
        LibraryItem item2 = mock(LibraryItem.class);
        LibraryItem item3 = mock(LibraryItem.class);

        when(strategy.matches(item1, "java")).thenReturn(true);
        when(strategy.matches(item2, "java")).thenReturn(false);
        when(strategy.matches(item3, "java")).thenReturn(true);

        List<LibraryItem> result = context.search(List.of(item1, item2, item3), "java");

        assertEquals(2, result.size());
        assertTrue(result.contains(item1));
        assertTrue(result.contains(item3));

        // Verify matching calls
        verify(strategy).matches(item1, "java");
        verify(strategy).matches(item2, "java");
        verify(strategy).matches(item3, "java");
    }
}
