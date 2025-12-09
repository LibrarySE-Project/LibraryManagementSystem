package librarySE.app;

import librarySE.managers.*;
import librarySE.managers.reports.ReportManager;
import librarySE.repo.BorrowRecordRepository;
import librarySE.repo.ItemRepository;
import librarySE.repo.UserRepository;
import librarySE.repo.WaitlistRepository;
import librarySE.search.KeywordSearchStrategy;

import io.github.cdimascio.dotenv.Dotenv;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Entry point for the GUI-based Library Management System.
 *
 * Responsibilities:
 *  - Load admin credentials from .env file.
 *  - Initialize repositories and managers (Item/Borrow/User/Waitlist).
 *  - Create the ReportManager.
 *  - Open the initial login window (LibraryLoginFrame).
 */
public class LibraryGuiApp {

    /**
     * Reads admin credentials from .env and initializes the Admin singleton.
     *
     * Expected keys in .env:
     *  - ADMIN_USERNAME
     *  - ADMIN_PASSWORD
     *  - ADMIN_EMAIL
     *
     * If any key is missing/blank, a default value is used.
     */
    private static Admin initAdminFromEnv() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String username = valueOrDefault(dotenv, "ADMIN_USERNAME", "admin");
        String password = valueOrDefault(dotenv, "ADMIN_PASSWORD", "admin123");
        String email    = valueOrDefault(dotenv, "ADMIN_EMAIL", "librarysystem408@gmail.com");

        if (isBlank(username) || isBlank(password) || isBlank(email)) {
            throw new IllegalStateException(
                    "Admin credentials are missing in .env (ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL).");
        }

        Admin.initialize(username, password, email);
        return Admin.getInstance();
    }

    private static String valueOrDefault(Dotenv dotenv, String key, String def) {
        String v = dotenv.get(key);
        if (v == null || v.isBlank()) {
            return def;
        }
        return v.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // ===== main =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // 1) Repositories (in-memory implementations for GUI run)
            ItemRepository itemRepo = new InMemoryItemRepository();
            BorrowRecordRepository borrowRepo = new InMemoryBorrowRecordRepository();
            WaitlistRepository waitlistRepo = new InMemoryWaitlistRepository();
            UserRepository userRepo = new InMemoryUserRepository();

            // 2) Managers
            ItemManager.init(itemRepo, new KeywordSearchStrategy());
            BorrowManager.init(borrowRepo, waitlistRepo);
            UserManager.init(userRepo);

            ItemManager itemManager = ItemManager.getInstance();
            BorrowManager borrowMgr = BorrowManager.getInstance();
            UserManager userManager = UserManager.getInstance();

            // 3) Admin from .env + LoginManager
            Admin admin = initAdminFromEnv();
            LoginManager loginManager = new LoginManager(admin);

            // 4) ReportManager
            ReportManager reportManager =
                    new ReportManager(borrowMgr.getAllBorrowRecords());

            // 5) Open login window
            LibraryLoginFrame loginFrame = new LibraryLoginFrame(
                    loginManager,
                    admin,
                    itemManager,
                    borrowMgr,
                    userManager,
                    reportManager
            );
            loginFrame.setVisible(true);
        });
    }

    // --------- In-memory Repositories (for GUI run; replace with real repos if needed) ---------

    private static class InMemoryItemRepository implements ItemRepository {
        private final List<librarySE.core.LibraryItem> items =
                new CopyOnWriteArrayList<>();

        @Override
        public List<librarySE.core.LibraryItem> loadAll() {
            return new ArrayList<>(items);
        }

        @Override
        public void saveAll(List<librarySE.core.LibraryItem> list) {
            items.clear();
            items.addAll(list);
        }
    }

    private static class InMemoryBorrowRecordRepository implements BorrowRecordRepository {
        private final List<BorrowRecord> records =
                new CopyOnWriteArrayList<>();

        @Override
        public List<BorrowRecord> loadAll() {
            return new ArrayList<>(records);
        }

        @Override
        public void saveAll(List<BorrowRecord> list) {
            records.clear();
            records.addAll(list);
        }
    }

    private static class InMemoryWaitlistRepository implements WaitlistRepository {
        private final List<librarySE.core.WaitlistEntry> entries =
                new CopyOnWriteArrayList<>();

        @Override
        public List<librarySE.core.WaitlistEntry> loadAll() {
            return new ArrayList<>(entries);
        }

        @Override
        public void saveAll(List<librarySE.core.WaitlistEntry> list) {
            entries.clear();
            entries.addAll(list);
        }
    }

    private static class InMemoryUserRepository implements UserRepository {
        private final List<User> users =
                new CopyOnWriteArrayList<>();

        @Override
        public List<User> loadAll() {
            return new ArrayList<>(users);
        }

        @Override
        public void saveAll(List<User> list) {
            users.clear();
            users.addAll(list);
        }
    }
}
