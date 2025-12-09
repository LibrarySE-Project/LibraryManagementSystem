package librarySE.app;

import librarySE.managers.*;
import librarySE.managers.reports.ReportManager;
import librarySE.repo.BorrowRecordRepository;
import librarySE.repo.FileBorrowRecordRepository;
import librarySE.repo.FileItemRepository;
import librarySE.repo.FileUserRepository;
import librarySE.repo.FileWaitlistRepository;
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

        	// 1) Repositories (file-based implementations)
        	ItemRepository itemRepo = new FileItemRepository();
        	BorrowRecordRepository borrowRepo = new FileBorrowRecordRepository();
        	WaitlistRepository waitlistRepo = new FileWaitlistRepository();
        	UserRepository userRepo = new FileUserRepository();


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

}
