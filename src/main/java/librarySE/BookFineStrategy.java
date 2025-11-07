package librarySE;

import java.math.BigDecimal;

/**
 * Fine strategy for books.
 *
 * @see BaseFineStrategy
 * @see FineStrategy
 * @author Malak
 */
public class BookFineStrategy extends BaseFineStrategy {

    public BookFineStrategy() {
        super(BigDecimal.valueOf(10), 28); // 10 NIS/day, 28 days period
    }
}
