package librarySE;

import java.math.BigDecimal;

/**
 * Fine strategy for journals.
 *
 * @see BaseFineStrategy
 * @see FineStrategy
 * @author Malak
 */
public class JournalFineStrategy extends BaseFineStrategy {

    public JournalFineStrategy() {
        super(BigDecimal.valueOf(15), 21); // 15 NIS/day, 21 days period
    }
}
