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

public class LibraryGuiApp {

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

  
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            ItemRepository itemRepo = new FileItemRepository();
            BorrowRecordRepository borrowRepo = new FileBorrowRecordRepository();
            WaitlistRepository waitlistRepo = new FileWaitlistRepository();
            UserRepository userRepo = new FileUserRepository();

            ItemManager.init(itemRepo, new KeywordSearchStrategy());
            UserManager.init(userRepo);

            ItemManager itemManager = ItemManager.getInstance();


            BorrowManager.init(borrowRepo, waitlistRepo, itemManager);
            BorrowManager borrowMgr = BorrowManager.getInstance();

            UserManager userManager = UserManager.getInstance();

            Admin admin = initAdminFromEnv();
            LoginManager loginManager = new LoginManager(admin);

            ReportManager reportManager =
                    new ReportManager(borrowMgr.getAllBorrowRecords());

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
