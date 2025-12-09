package librarySE.app;

import librarySE.core.LibraryItem;
import librarySE.core.MaterialType;
import librarySE.managers.*;
import librarySE.managers.notifications.EmailNotifier;
import librarySE.managers.notifications.Notifier;
import librarySE.managers.reports.ReportManager;
import librarySE.managers.notifications.Notifier;
import librarySE.managers.notifications.EmailNotifier;
import librarySE.repo.BorrowRecordRepository;
import librarySE.repo.ItemRepository;
import librarySE.repo.UserRepository;
import librarySE.repo.WaitlistRepository;
import librarySE.search.KeywordSearchStrategy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Console-based entry point for the Library Management System.
 *
 * Covers the Phase 1 user stories:
 *  - Admin login / logout
 *  - Add book / CD / journal
 *  - Search items
 *  - Borrow items (books 28 days, CDs 7 days, journals 21 days via FineStrategyFactory)
 *  - Overdue detection & fines (via BorrowManager + strategies)
 *  - Pay fines
 *  - Send overdue reminders (EmailNotifier – real or mocked in tests)
 *  - Simple reports (top borrowers, most borrowed items, fines per user)
 *
 * This class plays the role of the Presentation Layer (CLI),
 * delegating all business logic to existing managers & services.
 */
public class LibraryConsoleApp {

    // Managers (service layer)
    private final Admin admin;
    private final LoginManager loginManager;
    private final ItemManager itemManager;
    private final UserManager userManager;
    private final BorrowManager borrowManager;
    private final ReportManager reportManager;

    // Scanner for console input
    private final Scanner scanner = new Scanner(System.in);

    private LibraryConsoleApp() {
        // ====== Bootstrap / Initialization ======

        // 1) Repositories (simple in-memory implementations for the console run)
        ItemRepository itemRepo = new InMemoryItemRepository();
        BorrowRecordRepository borrowRepo = new InMemoryBorrowRecordRepository();
        WaitlistRepository waitlistRepo = new InMemoryWaitlistRepository();
        UserRepository userRepo = new InMemoryUserRepository();

        // 2) Managers singletons
        ItemManager.init(itemRepo, new KeywordSearchStrategy());
        BorrowManager.init(borrowRepo, waitlistRepo);
        UserManager.init(userRepo);

        this.itemManager = ItemManager.getInstance();
        this.borrowManager = BorrowManager.getInstance();
        this.userManager = UserManager.getInstance();

        // 3) Admin singleton + login manager
        Admin.initialize("admin", "admin123", "admin@library.ps");
        this.admin = Admin.getInstance();
        this.loginManager = new LoginManager(admin);

        // 4) Report manager (facade over reporting services)
        this.reportManager = new ReportManager(borrowManager.getAllBorrowRecords());

        // 5) Seed a couple of demo users (for quick testing)
        seedSampleUsers();
    }

    private void seedSampleUsers() {
        if (userManager.getAllUsers().isEmpty()) {
            User u1 = new User("eman", Role.USER, "pass123", "eman@example.com");
            User u2 = new User("malak", Role.USER, "pass123", "malak@example.com");
            userManager.addUser(u1);
            userManager.addUser(u2);
        }
    }

    private void run() {
        System.out.println("=== Library Management System (Console) ===");
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> adminLogin();
                case "2" -> adminLogout();
                case "3" -> registerUser();
                case "4" -> listUsers();
                case "5" -> addItem();
                case "6" -> searchItems();
                case "7" -> borrowItem();
                case "8" -> returnItem();
                case "9" -> payFine();
                case "10" -> showReports();
                case "11" -> sendReminders();
                case "0" -> {
                    running = false;
                    System.out.println("Exiting... Bye!");
                }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("[Admin]  1) Admin login");
        System.out.println("[Admin]  2) Admin logout");
        System.out.println("[User]   3) Register new user");
        System.out.println("[User]   4) List users");
        System.out.println("[Admin]  5) Add library item (Book/CD/Journal)");
        System.out.println("[All]    6) Search items");
        System.out.println("[User]   7) Borrow item");
        System.out.println("[User]   8) Return item");
        System.out.println("[User]   9) Pay fine");
        System.out.println("[Admin] 10) Show reports");
        System.out.println("[Admin] 11) Send overdue reminders");
        System.out.println("        0) Exit");
        System.out.print("Choose: ");
    }

    // =========== Admin features (US1.1, US1.2, US1.3, US1.4 + others) ==========

    private void adminLogin() {
        if (loginManager.isLoggedIn()) {
            System.out.println("Already logged in as " + admin.getUsername());
            return;
        }
        System.out.print("Admin username: ");
        String u = scanner.nextLine().trim();
        System.out.print("Admin password: ");
        String p = scanner.nextLine().trim();

        try {
            boolean ok = loginManager.login(u, p);
            if (ok) {
                System.out.println("✅ Login successful.");
            } else {
                System.out.println("❌ Invalid credentials.");
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void adminLogout() {
        if (!loginManager.isLoggedIn()) {
            System.out.println("Admin is not logged in.");
            return;
        }
        loginManager.logout();
        System.out.println("✅ Admin logged out.");
    }

    private void ensureAdminLoggedIn() {
        if (!loginManager.isLoggedIn()) {
            throw new IllegalStateException("Admin must be logged in for this operation.");
        }
    }

    private void addItem() {
        try {
            ensureAdminLoggedIn();
        } catch (IllegalStateException ex) {
            System.out.println("❌ " + ex.getMessage());
            return;
        }

        System.out.println("\n--- Add Item ---");
        System.out.println("1) Book");
        System.out.println("2) CD");
        System.out.println("3) Journal");
        System.out.print("Type: ");
        String typeChoice = scanner.nextLine().trim();

        MaterialType type;
        switch (typeChoice) {
            case "1" -> type = MaterialType.BOOK;
            case "2" -> type = MaterialType.CD;
            case "3" -> type = MaterialType.JOURNAL;
            default -> {
                System.out.println("Invalid type.");
                return;
            }
        }

        try {
            LibraryItem item;
            System.out.print("Title: ");
            String title = scanner.nextLine().trim();
            System.out.print("Price (optional, press Enter to skip): ");
            String price = scanner.nextLine().trim();

            switch (type) {
                case BOOK -> {
                    System.out.print("Author: ");
                    String author = scanner.nextLine().trim();
                    System.out.print("ISBN: ");
                    String isbn = scanner.nextLine().trim();
                    if (isbn.isEmpty()) {
                        System.out.println("ISBN is required for books.");
                        return;
                    }
                    if (price.isEmpty())
                        item = librarySE.core.LibraryItemFactory.create(type, isbn, title, author);
                    else
                        item = librarySE.core.LibraryItemFactory.create(type, isbn, title, author, price);
                }
                case CD -> {
                    System.out.print("Artist: ");
                    String artist = scanner.nextLine().trim();
                    if (price.isEmpty())
                        item = librarySE.core.LibraryItemFactory.create(type, title, artist);
                    else
                        item = librarySE.core.LibraryItemFactory.create(type, title, artist, price);
                }
                case JOURNAL -> {
                    System.out.print("Editor: ");
                    String editor = scanner.nextLine().trim();
                    System.out.print("Issue: ");
                    String issue = scanner.nextLine().trim();
                    if (issue.isEmpty()) {
                        System.out.println("Issue is required for journals.");
                        return;
                    }
                    if (price.isEmpty())
                        item = librarySE.core.LibraryItemFactory.create(type, title, editor, issue);
                    else
                        item = librarySE.core.LibraryItemFactory.create(type, title, editor, issue, price);
                }
                default -> throw new IllegalStateException("Unexpected value: " + type);
            }

            itemManager.addItem(item, admin);
            System.out.println("✅ Item added: " + item.getTitle());
        } catch (Exception ex) {
            System.out.println("Error adding item: " + ex.getMessage());
        }
    }

    private void searchItems() {
        System.out.print("Enter keyword (title/author/ISBN/etc.): ");
        String keyword = scanner.nextLine().trim();
        if (keyword.isEmpty()) {
            System.out.println("Keyword cannot be empty.");
            return;
        }

        List<LibraryItem> results = itemManager.searchItems(keyword);
        if (results.isEmpty()) {
            System.out.println("No items found.");
        } else {
            System.out.println("Found " + results.size() + " item(s):");
            for (LibraryItem item : results) {
                System.out.println("- " + item.getTitle() + " [" + item.getMaterialType() + "]");
            }
        }
    }

    // =========== User management (register, list) ==========

    private void registerUser() {
        System.out.println("\n--- Register New User ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        try {
            User u = new User(username, Role.USER, password, email);
            userManager.addUser(u);
            System.out.println("✅ User registered: " + u.getUsername());
        } catch (Exception ex) {
            System.out.println("Error registering user: " + ex.getMessage());
        }
    }

    private void listUsers() {
        List<User> users = userManager.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users registered.");
            return;
        }
        System.out.println("\n--- Users ---");
        for (User u : users) {
            System.out.println("- " + u);
        }
    }

    private User chooseUserByEmail() {
        System.out.print("Enter user email: ");
        String email = scanner.nextLine().trim();
        Optional<User> opt = userManager.findUserByEmail(email);
        if (opt.isEmpty()) {
            System.out.println("User not found with email: " + email);
            return null;
        }
        return opt.get();
    }

    // =========== Borrowing & Fines (US2.x, US4.1, US5.x) ==========

    private void borrowItem() {
        System.out.println("\n--- Borrow Item ---");
        User user = chooseUserByEmail();
        if (user == null) return;

        // Show all items
        List<LibraryItem> all = itemManager.getAllItems();
        if (all.isEmpty()) {
            System.out.println("No items in library yet.");
            return;
        }

        for (int i = 0; i < all.size(); i++) {
            LibraryItem item = all.get(i);
            System.out.printf("%d) %s [%s] - %s%n",
                    i + 1, item.getTitle(), item.getMaterialType(),
                    item.isAvailable() ? "Available" : "Not available");
        }
        System.out.print("Choose item number: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx < 0 || idx >= all.size()) {
                System.out.println("Invalid item choice.");
                return;
            }
            LibraryItem chosen = all.get(idx);
            boolean borrowed = borrowManager.borrowItem(user, chosen);
            if (borrowed) {
                System.out.println("✅ Borrowed successfully. Due date depends on media type.");
            } else {
                System.out.println("ℹ️ Item unavailable. User added to waitlist.");
            }
        } catch (Exception ex) {
            System.out.println("Borrow error: " + ex.getMessage());
        }
    }

    private void returnItem() {
        System.out.println("\n--- Return Item ---");
        User user = chooseUserByEmail();
        if (user == null) return;

        List<BorrowRecord> records = borrowManager.getBorrowRecordsForUser(user);
        if (records.isEmpty()) {
            System.out.println("User has no borrow records.");
            return;
        }

        LocalDate today = LocalDate.now();
        List<BorrowRecord> active = new ArrayList<>();
        for (BorrowRecord r : records) {
            if (!r.isReturned()) active.add(r);
        }
        if (active.isEmpty()) {
            System.out.println("User has no active borrows.");
            return;
        }

        for (int i = 0; i < active.size(); i++) {
            BorrowRecord r = active.get(i);
            System.out.printf("%d) %s (due %s) %s%n",
                    i + 1,
                    r.getItem().getTitle(),
                    r.getDueDate(),
                    r.isOverdue(today) ? "[OVERDUE]" : "");
        }

        System.out.print("Choose record number: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx < 0 || idx >= active.size()) {
                System.out.println("Invalid choice.");
                return;
            }
            BorrowRecord chosen = active.get(idx);
            borrowManager.returnItem(user, chosen.getItem());
            System.out.println("✅ Item returned.");
        } catch (Exception ex) {
            System.out.println("Return error: " + ex.getMessage());
        }
    }

    private void payFine() {
        System.out.println("\n--- Pay Fine ---");
        User user = chooseUserByEmail();
        if (user == null) return;

        LocalDate today = LocalDate.now();
        // update fines first
        borrowManager.applyOverdueFines(today);
        BigDecimal balance = user.getFineBalance();
        System.out.println("Current fine balance: " + balance);
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("No fines to pay.");
            return;
        }
        System.out.print("Amount to pay: ");
        String amountStr = scanner.nextLine().trim();
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            user.payFine(amount);
            userManager.saveAll();
            System.out.println("✅ Payment successful. New balance: " + user.getFineBalance());
        } catch (Exception ex) {
            System.out.println("Payment error: " + ex.getMessage());
        }
    }

    // =========== Reports & Reminders (US3.1, US5.3) ==========

    private void showReports() {
        try {
            ensureAdminLoggedIn();
        } catch (IllegalStateException ex) {
            System.out.println("❌ " + ex.getMessage());
            return;
        }

        LocalDate today = LocalDate.now();
        StringBuilder sb = new StringBuilder();
        sb.append("=== Top Borrowers ===\n");
        Map<User, Long> topBorrowers = reportManager.activity().getTopBorrowers();
        topBorrowers.forEach((user, count) ->
                sb.append(user.getUsername()).append(" -> ").append(count).append(" items\n"));

        sb.append("\n=== Most Borrowed Items ===\n");
        Map<LibraryItem, Long> mostItems = reportManager.activity().getMostBorrowedItems();
        mostItems.forEach((item, count) ->
                sb.append(item.getTitle()).append(" (")
                  .append(item.getMaterialType()).append(") -> ")
                  .append(count).append(" borrows\n"));

        sb.append("\n=== Total Fines per User (as of ").append(today).append(") ===\n");
        Map<User, BigDecimal> totals = reportManager.fines().getTotalFinesForAllUsers(today);
        totals.forEach((user, fine) ->
                sb.append(user.getUsername()).append(" -> ").append(fine).append("\n"));

        System.out.println("\n" + sb);
    }

    private void sendReminders() {
        try {
            ensureAdminLoggedIn();
        } catch (IllegalStateException ex) {
            System.out.println("❌ " + ex.getMessage());
            return;
        }

        LocalDate today = LocalDate.now();
        Notifier notifier = new EmailNotifier(); // في الاختبارات ممكن تستخدم Mock
        NotificationManager nm = new NotificationManager(borrowManager);
        try {
            nm.sendReminders(notifier, today);
            System.out.println("✅ Overdue reminders sent (or recorded by mock email server).");
        } catch (Exception ex) {
            System.out.println("Reminder error: " + ex.getMessage());
        }
    }

    // =========== In-memory repository implementations ==========
    // هذه فقط لتشغيل الكونسول. مشروعك الحقيقي يستخدم Repo من librarySE.repo مع ملفات/JSON.

    /** Simple in-memory implementation of ItemRepository. */
    private static class InMemoryItemRepository implements ItemRepository {
        private final List<LibraryItem> items = new CopyOnWriteArrayList<>();

        @Override
        public List<LibraryItem> loadAll() {
            return new ArrayList<>(items);
        }

        @Override
        public void saveAll(List<LibraryItem> list) {
            items.clear();
            items.addAll(list);
        }
    }

    /** Simple in-memory implementation of BorrowRecordRepository. */
    private static class InMemoryBorrowRecordRepository implements BorrowRecordRepository {
        private final List<BorrowRecord> records = new CopyOnWriteArrayList<>();

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

    /** Simple in-memory implementation of WaitlistRepository. */
    private static class InMemoryWaitlistRepository implements WaitlistRepository {
        private final List<librarySE.core.WaitlistEntry> entries = new CopyOnWriteArrayList<>();

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

    /** Simple in-memory implementation of UserRepository. */
    private static class InMemoryUserRepository implements UserRepository {
        private final List<User> users = new CopyOnWriteArrayList<>();

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

    // =========== main() ==========
    public static void main(String[] args) {
        new LibraryConsoleApp().run();
    }
}
