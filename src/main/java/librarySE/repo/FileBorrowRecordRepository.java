package librarySE.repo;
import com.google.gson.reflect.TypeToken;
import librarySE.managers.BorrowRecord;
import librarySE.utils.FileUtils;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

/**
 * File-based implementation of {@link BorrowRecordRepository}.
 * <p>
 * This class manages the persistent storage of borrowing records
 * using a JSON file located in the library's data directory.
 * It leverages the {@link FileUtils} utility class to handle
 * reading and writing operations safely.
 * </p>
 *
 * <p><strong>Storage format:</strong> JSON array of {@link BorrowRecord} objects</p>
 *
 * <p><strong>Example file path:</strong> {@code library_data/borrow_records.json}</p>
 *
 * <p>This implementation ensures that all borrowing data is
 * automatically persisted between system restarts.</p>
 *
 * @author Eman
 * 
 */
public class FileBorrowRecordRepository implements BorrowRecordRepository {

    /** JSON file path used for storing borrow record data. */
    private static final Path FILE = FileUtils.dataFile("borrow_records.json");

    /**
     * Loads all borrowing records from the JSON file.
     * <p>If the file does not exist or is empty, an empty list is returned.</p>
     *
     * @return list of all {@link BorrowRecord} instances stored in the file
     */
    @Override
    public List<BorrowRecord> loadAll() {
        Type type = new TypeToken<List<BorrowRecord>>() {}.getType();
        List<BorrowRecord> list = FileUtils.readJson(FILE, type, new ArrayList<>());
        return (list == null) ? new ArrayList<>() : list;
    }

    /**
     * Saves all borrowing records to the JSON file.
     * <p>Existing data will be overwritten to ensure consistency with the current state.</p>
     *
     * @param records list of {@link BorrowRecord} objects to be saved
     * @throws RuntimeException if writing to the file fails
     */
    @Override
    public void saveAll(List<BorrowRecord> records) {
        FileUtils.writeJson(FILE, records);
    }
}
