package librarySE.repo;

import librarySE.core.WaitlistEntry;
import java.util.List;

/**
 * Defines persistence operations for waitlist entries.
 * <p>This interface allows saving and loading the list of users waiting for specific library items.</p>
 *
 * @author Malak
 */
public interface WaitlistRepository {

    /**
     * Loads all waitlist entries from persistent storage.
     *
     * @return list of waitlist entries
     */
    List<WaitlistEntry> loadAll();

    /**
     * Saves all waitlist entries to persistent storage.
     *
     * @param entries the list of entries to save
     */
    void saveAll(List<WaitlistEntry> entries);
}
