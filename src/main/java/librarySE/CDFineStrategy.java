package librarySE;

import java.math.BigDecimal;

/**
 * Fine strategy for CDs.
 *
 * @see BaseFineStrategy
 * @see FineStrategy
 *  @author Malak
 */
public class CDFineStrategy extends BaseFineStrategy {

    public CDFineStrategy() {
        super(BigDecimal.valueOf(20), 7); // 20 NIS/day, 7 days period
    }
}
