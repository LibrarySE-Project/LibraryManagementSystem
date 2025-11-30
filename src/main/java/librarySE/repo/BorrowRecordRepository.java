package librarySE.repo;


import librarySE.managers.BorrowRecord;
import java.util.List;

/**
 * Defines the contract for persistence operations related to {@link BorrowRecord}.
 * <p>
 * This interface abstracts how borrowing records are stored and retrieved from
 * persistent storage (such as JSON files or databases).
 * </p>
 *
 * <p>Typical implementations include:</p>
 * <ul>
 *     <li>{@code FileBorrowRecordRepository} – stores borrow records in a JSON file.</li>
 *     <li>{@code DatabaseBorrowRecordRepository} – stores borrow records in a database (future expansion).</li>
 * </ul>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *     <li>Load all existing borrow records from storage.</li>
 *     <li>Persist updates after borrowing, returning, or fine application.</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * BorrowRecordRepository repo = new FileBorrowRecordRepository();
 * List<BorrowRecord> records = repo.loadAll();
 *
 * // Add a new borrowing record
 * records.add(new BorrowRecord(user, item, fineStrategy, LocalDate.now()));
 * repo.saveAll(records);
 * }</pre>
 * 
 * @author Eman
 * @see librarySEv2.managers.BorrowRecord
 * @see librarySEv2.repo.FileBorrowRecordRepository
 */
public interface BorrowRecordRepository {

    /**
     * Loads all {@link BorrowRecord} entries from persistent storage.
     *
     * @return a list of all borrow records; never {@code null}, but may be empty
     */
    List<BorrowRecord> loadAll();

    /**
     * Saves the given list of {@link BorrowRecord} objects to persistent storage.
     * <p>
     * This method is typically called after any borrowing or returning action.
     * </p>
     *
     * @param records the list of borrow records to save; must not be {@code null}
     */
    void saveAll(List<BorrowRecord> records);
}
