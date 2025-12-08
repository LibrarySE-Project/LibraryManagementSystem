package librarySE.app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import librarySE.managers.Admin;
import librarySE.managers.BorrowManager;
import librarySE.managers.LoginManager;
import librarySE.managers.UserManager;
import librarySE.managers.reports.ReportManager;

import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Main Swing window for the Library Management System.
 * 
 * Tabs:
 *  - Admin: login/logout, add item, search items
 *  - Borrow & Fines: borrow/return items, pay fines
 *  - Reports & Reminders: overdue reminders, reports, export CSV
 */
public class LibraryMainFrame extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final LoginManager loginManager;
    private final Admin admin;
    private final ItemManager itemManager;
    private final BorrowManager borrowManager;
    private final UserManager userManager;
    private final ReportManager reportManager;

    // Shared components
    private JLabel adminStatusLabel;

    // Admin tab components
    private JTextField adminUserField;
    private JPasswordField adminPassField;
    private JButton loginButton;
    private JButton logoutButton;

    // Add item
    private JComboBox<MaterialType> materialTypeCombo;
    private JTextField isbnField;    // for book
    private JTextField titleField;
    private JTextField authorArtistEditorField;
    private JTextField issueNumberField; // for journal only
    private JTextField priceField;
    private JButton addItemButton;

    // Search items
    private JTextField searchField;
    private JButton searchButton;
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;

    // Borrow tab components
    private JComboBox<User> userCombo;
    private JButton refreshUsersButton;
    private JLabel fineBalanceLabel;

    private JTable allItemsTable;
    private DefaultTableModel allItemsTableModel;
    private JButton borrowButton;

    private JTable userBorrowTable;
    private DefaultTableModel userBorrowTableModel;
    private JButton returnButton;

    private JTextField payAmountField;
    private JButton payFineButton;

    // Reports tab components
    private JTextArea reportArea;
    private JButton generateReportButton;
    private JButton exportCsvButton;
    private JButton sendRemindersButton;

    public LibraryMainFrame(LoginManager loginManager,
                            Admin admin,
                            ItemManager itemManager,
                            BorrowManager borrowManager,
                            UserManager userManager,
                            ReportManager reportManager) {
        super("Library Management System");

        this.loginManager = loginManager;
        this.admin = admin;
        this.itemManager = itemManager;
        this.borrowManager = borrowManager;
        this.userManager = userManager;
        this.reportManager = reportManager;

        initUi();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    private void initUi() {
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Admin", createAdminPanel());
        tabs.addTab("Borrow & Fines", createBorrowPanel());
        tabs.addTab("Reports & Reminders", createReportsPanel());

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }

    // ===================== Admin Panel =====================

    private JPanel createAdminPanel() {
        JPanel root = new JPanel(new BorderLayout());

        // --- Top: login panel ---
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        adminUserField = new JTextField("admin", 10);
        adminPassField = new JPasswordField("admin123", 10);
        loginButton = new JButton("Login");
        logoutButton = new JButton("Logout");
        adminStatusLabel = new JLabel("Not logged in");

        loginPanel.setBorder(BorderFactory.createTitledBorder("Admin Login"));
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(adminUserField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(adminPassField);
        loginPanel.add(loginButton);
        loginPanel.add(logoutButton);
        loginPanel.add(adminStatusLabel);

        loginButton.addActionListener(e -> handleAdminLogin());
        logoutButton.addActionListener(e -> handleAdminLogout());

        // --- Center: item management & search ---
        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                createAddItemPanel(),
                createSearchPanel()
        );
        split.setResizeWeight(0.4);

        root.add(loginPanel, BorderLayout.NORTH);
        root.add(split, BorderLayout.CENTER);

        return root;
    }

    private JPanel createAddItemPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add Library Item"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        materialTypeCombo = new JComboBox<>(MaterialType.values());
        isbnField = new JTextField(15);
        titleField = new JTextField(20);
        authorArtistEditorField = new JTextField(20);
        issueNumberField = new JTextField(10);
        priceField = new JTextField(8);
        addItemButton = new JButton("Add Item");

        // Row 0: type + price
        c.gridx = 0; c.gridy = 0;
        panel.add(new JLabel("Type:"), c);
        c.gridx = 1;
        panel.add(materialTypeCombo, c);

        c.gridx = 2;
        panel.add(new JLabel("Price (optional):"), c);
        c.gridx = 3;
        panel.add(priceField, c);

        // Row 1: ISBN (book only)
        c.gridx = 0; c.gridy = 1;
        panel.add(new JLabel("ISBN (Book only):"), c);
        c.gridx = 1;
        panel.add(isbnField, c);

        // Row 2: Title
        c.gridx = 0; c.gridy = 2;
        panel.add(new JLabel("Title:"), c);
        c.gridx = 1; c.gridwidth = 3;
        panel.add(titleField, c);
        c.gridwidth = 1;

        // Row 3: Author / Artist / Editor
        c.gridx = 0; c.gridy = 3;
        panel.add(new JLabel("Author / Artist / Editor:"), c);
        c.gridx = 1; c.gridwidth = 3;
        panel.add(authorArtistEditorField, c);
        c.gridwidth = 1;

        // Row 4: Issue number (Journal)
        c.gridx = 0; c.gridy = 4;
        panel.add(new JLabel("Issue (Journal only):"), c);
        c.gridx = 1;
        panel.add(issueNumberField, c);

        // Row 5: Button
        c.gridx = 0; c.gridy = 5; c.gridwidth = 4;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(addItemButton, c);

        addItemButton.addActionListener(e -> handleAddItem());

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Search Items"));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(30);
        searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");

        top.add(new JLabel("Keyword:"));
        top.add(searchField);
        top.add(searchButton);
        top.add(showAllButton);

        itemsTableModel = new DefaultTableModel(
                new Object[]{"Type", "Title", "Details"}, 0);
        itemsTable = new JTable(itemsTableModel);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);

        searchButton.addActionListener(e -> refreshSearchResults());
        showAllButton.addActionListener(e -> loadAllItemsToSearchTable());

        loadAllItemsToSearchTable();
        return panel;
    }

    private void handleAdminLogin() {
        String user = adminUserField.getText().trim();
        String pass = new String(adminPassField.getPassword());
        try {
            boolean ok = loginManager.login(user, pass);
            if (ok) {
                adminStatusLabel.setText("Logged in as " + admin.getUsername());
                JOptionPane.showMessageDialog(this, "Login successful.");
            } else {
                adminStatusLabel.setText("Login failed");
                JOptionPane.showMessageDialog(this, "Invalid credentials.",
                        "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAdminLogout() {
        loginManager.logout();
        adminStatusLabel.setText("Not logged in");
        JOptionPane.showMessageDialog(this, "Admin logged out.");
    }

    private void handleAddItem() {
        if (!loginManager.isLoggedIn()) {
            JOptionPane.showMessageDialog(this,
                    "You must be logged in as admin to add items.",
                    "Permission Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        MaterialType type = (MaterialType) materialTypeCombo.getSelectedItem();
        String priceText = priceField.getText().trim();
        String title = titleField.getText().trim();
        String person = authorArtistEditorField.getText().trim();
        String isbn = isbnField.getText().trim();
        String issue = issueNumberField.getText().trim();

        try {
            LibraryItem item;
            switch (type) {
                case BOOK -> {
                    if (isbn.isEmpty())
                        throw new IllegalArgumentException("ISBN is required for books.");
                    if (priceText.isEmpty())
                        item = LibraryItemFactory.create(type, isbn, title, person);
                    else
                        item = LibraryItemFactory.create(type, isbn, title, person, priceText);
                }
                case CD -> {
                    if (priceText.isEmpty())
                        item = LibraryItemFactory.create(type, title, person);
                    else
                        item = LibraryItemFactory.create(type, title, person, priceText);
                }
                case JOURNAL -> {
                    if (issue.isEmpty())
                        throw new IllegalArgumentException("Issue number is required for journals.");
                    if (priceText.isEmpty())
                        item = LibraryItemFactory.create(type, title, person, issue);
                    else
                        item = LibraryItemFactory.create(type, title, person, issue, priceText);
                }
                default -> throw new IllegalStateException("Unexpected value: " + type);
            }

            itemManager.addItem(item, admin);
            JOptionPane.showMessageDialog(this,
                    "Item added successfully: " + item.getTitle());
            loadAllItemsToSearchTable();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Error adding item", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshSearchResults() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAllItemsToSearchTable();
            return;
        }
        List<LibraryItem> results = itemManager.searchItems(keyword);
        itemsTableModel.setRowCount(0);
        for (LibraryItem item : results) {
            itemsTableModel.addRow(new Object[]{
                    item.getMaterialType(),
                    item.getTitle(),
                    item.toString()
            });
        }
    }

    private void loadAllItemsToSearchTable() {
        List<LibraryItem> all = itemManager.getAllItems();
        itemsTableModel.setRowCount(0);
        for (LibraryItem item : all) {
            itemsTableModel.addRow(new Object[]{
                    item.getMaterialType(),
                    item.getTitle(),
                    item.toString()
            });
        }
    }

    // ===================== Borrow & Fines Panel =====================

    private JPanel createBorrowPanel() {
        JPanel root = new JPanel(new BorderLayout());

        // Top: user selection + fine balance
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userCombo = new JComboBox<>();
        refreshUsersButton = new JButton("Refresh Users");
        fineBalanceLabel = new JLabel("Fine Balance: 0");

        top.setBorder(BorderFactory.createTitledBorder("User Selection"));
        top.add(new JLabel("User:"));
        top.add(userCombo);
        top.add(refreshUsersButton);
        top.add(fineBalanceLabel);

        refreshUsersButton.addActionListener(e -> loadUsersIntoCombo());
        loadUsersIntoCombo();

        // Center: split between all items + user borrows
        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                createBorrowFromItemsPanel(),
                createUserBorrowPanel()
        );
        split.setResizeWeight(0.5);

        // Bottom: pay fine
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBorder(BorderFactory.createTitledBorder("Pay Fine"));
        payAmountField = new JTextField(8);
        payFineButton = new JButton("Pay");
        bottom.add(new JLabel("Amount:"));
        bottom.add(payAmountField);
        bottom.add(payFineButton);

        payFineButton.addActionListener(e -> handlePayFine());

        root.add(top, BorderLayout.NORTH);
        root.add(split, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        return root;
    }

    private JPanel createBorrowFromItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Available Items"));

        allItemsTableModel = new DefaultTableModel(
                new Object[]{"Type", "Title", "Available?"}, 0);
        allItemsTable = new JTable(allItemsTableModel);
        borrowButton = new JButton("Borrow Selected");

        panel.add(new JScrollPane(allItemsTable), BorderLayout.CENTER);
        panel.add(borrowButton, BorderLayout.SOUTH);

        borrowButton.addActionListener(e -> handleBorrowItem());
        loadAllItemsToBorrowTable();

        return panel;
    }

    private JPanel createUserBorrowPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("User Active Borrows"));

        userBorrowTableModel = new DefaultTableModel(
                new Object[]{"Item", "Borrow Date", "Due Date", "Status"}, 0);
        userBorrowTable = new JTable(userBorrowTableModel);
        returnButton = new JButton("Return Selected");

        panel.add(new JScrollPane(userBorrowTable), BorderLayout.CENTER);
        panel.add(returnButton, BorderLayout.SOUTH);

        returnButton.addActionListener(e -> handleReturnItem());

        return panel;
    }

    private void loadUsersIntoCombo() {
        userCombo.removeAllItems();
        List<User> users = userManager.getAllUsers();
        for (User u : users) {
            userCombo.addItem(u);
        }
        updateFineBalanceLabel();
        refreshUserBorrowTable();
    }

    private User getSelectedUser() {
        return (User) userCombo.getSelectedItem();
    }

    private void updateFineBalanceLabel() {
        User u = getSelectedUser();
        if (u == null) {
            fineBalanceLabel.setText("Fine Balance: -");
        } else {
            fineBalanceLabel.setText("Fine Balance: " + u.getFineBalance());
        }
    }

    private void loadAllItemsToBorrowTable() {
        List<LibraryItem> all = itemManager.getAllItems();
        allItemsTableModel.setRowCount(0);
        for (LibraryItem item : all) {
            allItemsTableModel.addRow(new Object[]{
                    item.getMaterialType(),
                    item.getTitle(),
                    item.isAvailable() ? "Available" : "Not available"
            });
        }
    }

    private void handleBorrowItem() {
        User user = getSelectedUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return;
        }

        int row = allItemsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to borrow.");
            return;
        }

        String title = (String) allItemsTableModel.getValueAt(row, 1);

        // Find the item by title (في نظام حقيقي الأفضل ID)
        LibraryItem item = itemManager.getAllItems().stream()
                .filter(i -> i.getTitle().equals(title))
                .findFirst()
                .orElse(null);

        if (item == null) {
            JOptionPane.showMessageDialog(this, "Item not found.");
            return;
        }

        try {
            boolean borrowed = borrowManager.borrowItem(user, item);
            if (borrowed) {
                JOptionPane.showMessageDialog(this,
                        "Item borrowed successfully.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Item is not available, user added to waitlist.");
            }
            loadAllItemsToBorrowTable();
            refreshUserBorrowTable();
            updateFineBalanceLabel();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Borrow Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshUserBorrowTable() {
        User user = getSelectedUser();
        userBorrowTableModel.setRowCount(0);
        if (user == null) return;

        List<BorrowRecord> records = borrowManager.getBorrowRecordsForUser(user);
        LocalDate today = LocalDate.now();

        for (BorrowRecord r : records) {
            userBorrowTableModel.addRow(new Object[]{
                    r.getItem().getTitle(),
                    r.getBorrowDate(),
                    r.getDueDate(),
                    r.getStatus() + (r.isOverdue(today) ? " (OVERDUE)" : "")
            });
        }
    }

    private void handleReturnItem() {
        User user = getSelectedUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please select a user.");
            return;
        }
        int row = userBorrowTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a borrow record.");
            return;
        }

        String title = (String) userBorrowTableModel.getValueAt(row, 0);

        LibraryItem item = itemManager.getAllItems().stream()
                .filter(i -> i.getTitle().equals(title))
                .findFirst()
                .orElse(null);

        if (item == null) {
            JOptionPane.showMessageDialog(this, "Item not found.");
            return;
        }

        try {
            borrowManager.returnItem(user, item);
            JOptionPane.showMessageDialog(this, "Item returned successfully.");
            loadAllItemsToBorrowTable();
            refreshUserBorrowTable();
            updateFineBalanceLabel();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Return Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePayFine() {
        User user = getSelectedUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please select a user.");
            return;
        }
        String amountText = payAmountField.getText().trim();
        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter amount.");
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(amountText);
            user.payFine(amount);
            userManager.saveAll();
            updateFineBalanceLabel();
            JOptionPane.showMessageDialog(this, "Fine paid successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Payment Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================== Reports & Reminders Panel =====================

    private JPanel createReportsPanel() {
        JPanel root = new JPanel(new BorderLayout());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        generateReportButton = new JButton("Generate Summary");
        exportCsvButton = new JButton("Export Fines CSV (today)");
        sendRemindersButton = new JButton("Send Overdue Reminders (Email)");

        buttons.add(generateReportButton);
        buttons.add(exportCsvButton);
        buttons.add(sendRemindersButton);

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scroll = new JScrollPane(reportArea);

        generateReportButton.addActionListener(e -> handleGenerateReport());
        exportCsvButton.addActionListener(e -> handleExportCsv());
        sendRemindersButton.addActionListener(e -> handleSendReminders());

        root.add(buttons, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);

        return root;
    }

    private void handleGenerateReport() {
        LocalDate today = LocalDate.now();
        StringBuilder sb = new StringBuilder();

        sb.append("=== Top Borrowers ===\n");
        Map<User, Long> topBorrowers =
                reportManager.activity().getTopBorrowers();
        topBorrowers.forEach((user, count) ->
                sb.append(user.getUsername()).append(" -> ").append(count).append(" items\n"));

        sb.append("\n=== Most Borrowed Items ===\n");
        Map<librarySE.core.LibraryItem, Long> mostItems =
                reportManager.activity().getMostBorrowedItems();
        mostItems.forEach((item, count) ->
                sb.append(item.getTitle()).append(" (")
                  .append(item.getMaterialType()).append(") -> ")
                  .append(count).append(" borrows\n"));

        sb.append("\n=== Total Fines per User (as of ").append(today).append(") ===\n");
        Map<User, java.math.BigDecimal> totalFines =
                reportManager.fines().getTotalFinesForAllUsers(today);
        totalFines.forEach((user, fine) ->
                sb.append(user.getUsername()).append(" -> ").append(fine).append("\n"));

        reportArea.setText(sb.toString());
    }

    private void handleExportCsv() {
        LocalDate today = LocalDate.now();
        try {
            reportManager.exportFinesCsv(today);
            JOptionPane.showMessageDialog(this,
                    "Fines CSV exported for date: " + today);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSendReminders() {
        LocalDate today = LocalDate.now();
        try {
            Notifier notifier = new EmailNotifier();
            NotificationManager nm = new NotificationManager(borrowManager);
            nm.sendReminders(notifier, today);
            JOptionPane.showMessageDialog(this,
                    "Overdue reminders sent (check your mocked/real email service).");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Reminders Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // ================== 1) تهيئة الـ Repositories (بسيطة مؤقتًا) ==================
                ItemRepository itemRepo = new ItemRepository() {
                    private final java.util.concurrent.CopyOnWriteArrayList<LibraryItem> items =
                            new java.util.concurrent.CopyOnWriteArrayList<>();

                    @Override
                    public java.util.List<LibraryItem> loadAll() {
                        return java.util.List.copyOf(items);
                    }

                    @Override
                    public void saveAll(java.util.List<LibraryItem> newItems) {
                        items.clear();
                        items.addAll(newItems);
                    }
                };

                UserRepository userRepo = new UserRepository() {
                    private final java.util.concurrent.CopyOnWriteArrayList<User> users =
                            new java.util.concurrent.CopyOnWriteArrayList<>();

                    @Override
                    public java.util.List<User> loadAll() {
                        return java.util.List.copyOf(users);
                    }

                    @Override
                    public void saveAll(java.util.List<User> newUsers) {
                        users.clear();
                        users.addAll(newUsers);
                    }
                };

                BorrowRecordRepository borrowRepo = new BorrowRecordRepository() {
                    private final java.util.concurrent.CopyOnWriteArrayList<BorrowRecord> records =
                            new java.util.concurrent.CopyOnWriteArrayList<>();

                    @Override
                    public java.util.List<BorrowRecord> loadAll() {
                        return java.util.List.copyOf(records);
                    }

                    @Override
                    public void saveAll(java.util.List<BorrowRecord> newRecords) {
                        records.clear();
                        records.addAll(newRecords);
                    }
                };

                WaitlistRepository waitlistRepo = new WaitlistRepository() {
                    private final java.util.concurrent.CopyOnWriteArrayList<WaitlistEntry> waitlist =
                            new java.util.concurrent.CopyOnWriteArrayList<>();

                    @Override
                    public java.util.List<WaitlistEntry> loadAll() {
                        return java.util.List.copyOf(waitlist);
                    }

                    @Override
                    public void saveAll(java.util.List<WaitlistEntry> newEntries) {
                        waitlist.clear();
                        waitlist.addAll(newEntries);
                    }
                };

                // ================== 2) تهيئة الـ Managers (Singletons) ==================
                UserManager userManager     = UserManager.init(userRepo);
                ItemManager itemManager     = ItemManager.init(itemRepo, new KeywordSearchStrategy());
                BorrowManager borrowManager = BorrowManager.init(borrowRepo, waitlistRepo);

                // Admin singleton
                Admin.initialize("admin", "admin123", "admin@library.ps");
                Admin admin = Admin.getInstance();

                LoginManager loginManager   = new LoginManager(admin);

                // ReportManager بياخذ كل الـ borrow records الحاليين
                ReportManager reportManager = new ReportManager(borrowManager.getAllBorrowRecords());

                // ================== 3) إنشاء وإظهار النافذة ==================
                LibraryMainFrame frame = new LibraryMainFrame(
                        loginManager,
                        admin,
                        itemManager,
                        borrowManager,
                        userManager,
                        reportManager
                );
                frame.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Startup error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

}
