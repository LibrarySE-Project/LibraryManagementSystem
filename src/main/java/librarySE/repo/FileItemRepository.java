package librarySE.repo;

import com.google.gson.reflect.TypeToken;
import librarySE.core.LibraryItem;
import librarySE.utils.FileUtils;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * File-based implementation of {@link ItemRepository} that persists
 * library items (books, CDs, journals, etc.) in JSON format.
 *
 * Data is stored inside:  library_data/items.json
 *
 * Uses {@link FileUtils} for safe reading/writing with automatic backups.
 *
 * @author Eman
 */
public class FileItemRepository implements ItemRepository {

    /** Path to the items.json file. */
    private static final Path FILE = FileUtils.dataFile("items.json");

    @Override
    public List<LibraryItem> loadAll() {
        Type type = new TypeToken<List<LibraryItem>>() {}.getType();
        List<LibraryItem> list = FileUtils.readJson(FILE, type, new ArrayList<>());
        return (list == null) ? new ArrayList<>() : list;
    }

    @Override
    public void saveAll(List<LibraryItem> items) {
        FileUtils.writeJson(FILE, items);
    }
}

