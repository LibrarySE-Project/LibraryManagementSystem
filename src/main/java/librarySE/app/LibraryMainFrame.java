package librarySE.app;

import librarySE.core.Book;
import librarySE.core.CD;
import librarySE.core.Journal;
import librarySE.core.LibraryItem;
import librarySE.core.LibraryItemFactory;
import librarySE.core.MaterialType;
import librarySE.managers.*;
import librarySE.managers.notifications.EmailNotifier;
import librarySE.managers.notifications.Notifier;
import librarySE.managers.reports.ReportManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public class LibraryMainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final LoginManager loginManager;
    private final Admin admin;
    private final ItemManager itemManager;
    private final BorrowManager borrowManager;
    private final UserManager userManager;
    private final ReportManager reportManager;

    // top header
    private JLabel adminStatusLabel;
    private JButton logoutButton;

    // Admin / items tab components
    private JComboBox<MaterialType> materialTypeCombo;
    private JTextField isbnField;    // for book
    private JTextField titleField;
    private JTextField authorArtistEditorField;
    private JTextField issueNumberField; // for journal only
    private JTextField priceField;
    private JSpinner copiesSpinner;
    private JButton addItemButton;

    private JTextField searchField;
    private JButton searchButton;
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;

    // User management tab components
    private JTextField newUserNameField;
    private JPasswordField newUserPassField;
    private JTextField newUserEmailField;
    private JButton addUserButton;
    private JButton unregisterUserButton;
    private JTable usersTable;
    private DefaultTableModel usersTableModel;

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
        super("Library Management System – Admin");

        this.loginManager = loginManager;
        this.admin = admin;
        this.itemManager = itemManager;
        this.borrowManager = borrowManager;
        this.userManager = userManager;
        this.reportManager = reportManager;

        initUi();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
    }

    private void initUi() {
        setLayout(new BorderLayout());

        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();
                Color c1 = new Color(255, 182, 193);
                Color c2 = new Color(135, 206, 235);
                GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                g2.dispose();
            }
        };
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Library Management System – Admin Area");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(60, 40, 70));

        adminStatusLabel = new JLabel("Logged in as: " + admin.getUsername());
        adminStatusLabel.setForeground(new Color(60, 40, 70));

        JPanel leftHeader = new JPanel(new GridLayout(2, 1));
        leftHeader.setOpaque(false);
        leftHeader.add(title);
        leftHeader.add(adminStatusLabel);

        logoutButton = new JButton("Logout");
        logoutButton.setFont(logoutButton.getFont().deriveFont(Font.BOLD));
        logoutButton.setFocusPainted(false);
        logoutButton.setBackground(new Color(255, 250, 250));
        logoutButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 130, 180)),
                BorderFactory.createEmptyBorder(5, 20, 5, 20)
        ));
        logoutButton.addActionListener(e -> handleLogout());

        header.add(leftHeader, BorderLayout.WEST);
        header.add(logoutButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ====== Center: tabbed content ======
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Books & Media", createAdminItemsPanel());
        tabs.addTab("Users", createUserManagementPanel());
        tabs.addTab("Borrow & Fines", createBorrowPanel());
        tabs.addTab("Reports & Reminders", createReportsPanel());

        add(tabs, BorderLayout.CENTER);
    }


    private void handleLogout() {
        loginManager.logout();
        JOptionPane.showMessageDialog(this, "You have been logged out.");
        dispose();
        LibraryLoginFrame loginFrame = new LibraryLoginFrame(
                loginManager,
                admin,
                itemManager,
                borrowManager,
                userManager,
                reportManager
        );
        loginFrame.setVisible(true);
    }


    private JPanel createAdminItemsPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createAddItemPanel(),
                createSearchPanel()
        );
        split.setResizeWeight(0.45);
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
        copiesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        addItemButton = new JButton("Add Item");

        // Row 0: type
        c.gridx = 0; c.gridy = 0;
        panel.add(new JLabel("Type:"), c);
        c.gridx = 1;
        panel.add(materialTypeCombo, c);

        // Row 1: price
        c.gridx = 0; c.gridy = 1;
        panel.add(new JLabel("Price (optional):"), c);
        c.gridx = 1;
        panel.add(priceField, c);

        // Row 2: copies
        c.gridx = 0; c.gridy = 2;
        panel.add(new JLabel("Total copies:"), c);
        c.gridx = 1;
        panel.add(copiesSpinner, c);

        // Row 3: ISBN (book only)
        c.gridx = 0; c.gridy = 3;
        panel.add(new JLabel("ISBN (Book only):"), c);
        c.gridx = 1;
        panel.add(isbnField, c);

        // Row 4: Title
        c.gridx = 0; c.gridy = 4;
        panel.add(new JLabel("Title:"), c);
        c.gridx = 1; c.gridwidth = 2;
        panel.add(titleField, c);
        c.gridwidth = 1;

        // Row 5: Author / Artist / Editor
        c.gridx = 0; c.gridy = 5;
        panel.add(new JLabel("Author / Artist / Editor:"), c);
        c.gridx = 1; c.gridwidth = 2;
        panel.add(authorArtistEditorField, c);
        c.gridwidth = 1;

        // Row 6: Issue number (Journal)
        c.gridx = 0; c.gridy = 6;
        panel.add(new JLabel("Issue (Journal only):"), c);
        c.gridx = 1;
        panel.add(issueNumberField, c);

        // Row 7: Button
        c.gridx = 0; c.gridy = 7; c.gridwidth = 3;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(addItemButton, c);

        addItemButton.addActionListener(e -> handleAddItem());

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Search Items"));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(25);
        searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");
        JButton deleteButton = new JButton("Delete Selected");

        top.add(new JLabel("Keyword:"));
        top.add(searchField);
        top.add(searchButton);
        top.add(showAllButton);
        top.add(deleteButton);

        itemsTableModel = new DefaultTableModel(
                new Object[]{"Type", "Title", "Details"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemsTable = new JTable(itemsTableModel);
        itemsTable.setRowHeight(24);
        itemsTable.setFillsViewportHeight(true);
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // double-click to edit item
        itemsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = itemsTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        String title = (String) itemsTableModel.getValueAt(row, 1);
                        LibraryItem item = itemManager.getAllItems().stream()
                                .filter(i -> i.getTitle().equals(title))
                                .findFirst()
                                .orElse(null);
                        if (item != null) {
                            openEditItemDialog(item);
                        }
                    }
                }
            }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);

        searchButton.addActionListener(e -> refreshSearchResults());
        showAllButton.addActionListener(e -> loadAllItemsToSearchTable());
        deleteButton.addActionListener(e -> handleDeleteSelectedItem());

        loadAllItemsToSearchTable();
        return panel;
    }


    private LibraryItem findExistingItem(MaterialType type,
                                         String title,
                                         String person,
                                         String isbn,
                                         String issue) {

        String normTitle = title == null ? "" : title.trim().toLowerCase();
        String normPerson = person == null ? "" : person.trim().toLowerCase();
        String normIsbn = isbn == null ? "" : isbn.trim();
        String normIssue = issue == null ? "" : issue.trim().toLowerCase();

        for (LibraryItem item : itemManager.getAllItems()) {

            switch (type) {

                case BOOK -> {
                    if (item instanceof Book b) {
                        if (!normIsbn.isEmpty()
                                && normIsbn.equalsIgnoreCase(b.getIsbn())) {
                            return item;
                        }
                    }
                }

                case CD -> {
                    if (item instanceof CD cd) {
                        String t = cd.getTitle().trim().toLowerCase();
                        String a = cd.getArtist().trim().toLowerCase();
                        if (normTitle.equals(t) && normPerson.equals(a)) {
                            return item;
                        }
                    }
                }

                case JOURNAL -> {
                    if (item instanceof Journal j) {
                        String t = j.getTitle().trim().toLowerCase();
                        String e = j.getEditor().trim().toLowerCase();
                        String iss = j.getIssueNumber().trim().toLowerCase();
                        if (normTitle.equals(t)
                                && normPerson.equals(e)
                                && normIssue.equals(iss)) {
                            return item;
                        }
                    }
                }
            }
        }
        return null;
    }


    private boolean hasActiveLoansForItem(LibraryItem item) {
        if (item == null) return false;

        return borrowManager.getAllBorrowRecords().stream()
                .anyMatch(r ->
                        r.getItem() != null
                                && r.getItem().getId().equals(item.getId())
                                && !r.isReturned());
    }


    private void handleDeleteSelectedItem() {
        int row = itemsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item to delete.",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = (String) itemsTableModel.getValueAt(row, 1);

        LibraryItem item = itemManager.getAllItems().stream()
                .filter(i -> i.getTitle().equals(title))
                .findFirst()
                .orElse(null);

        if (item == null) {
            JOptionPane.showMessageDialog(this,
                    "Item not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (hasActiveLoansForItem(item)) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete this item because there are active borrow records.",
                    "Delete not allowed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete item '" + item.getTitle() + "'?",
                "Confirm deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            itemManager.deleteItem(item, admin);
            itemManager.saveAll();

            loadAllItemsToSearchTable();
            loadAllItemsToBorrowTable();

            JOptionPane.showMessageDialog(this,
                    "Item deleted successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Delete Error",
                    JOptionPane.ERROR_MESSAGE);
        }
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
        int copies = (Integer) copiesSpinner.getValue();

        // check for existing item (by key per type)
        LibraryItem existing = findExistingItem(type, title, person, isbn, issue);
        if (existing != null) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "An item with the same key already exists.\n" +
                            "Do you want to open it for editing instead?",
                    "Duplicate item",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                openEditItemDialog(existing);
            }
            return;
        }

        try {
            LibraryItem item;

            switch (type) {

                case BOOK -> {
                    if (isbn.isEmpty())
                        throw new IllegalArgumentException("ISBN is required for books.");

                    if (priceText.isEmpty())
                        item = LibraryItemFactory.createBook(isbn, title, person, copies);
                    else
                        item = LibraryItemFactory.createBook(isbn, title, person, priceText, copies);
                }

                case CD -> {
                    if (priceText.isEmpty())
                        item = LibraryItemFactory.createCd(title, person, copies);
                    else
                        item = LibraryItemFactory.createCd(title, person, priceText, copies);
                }

                case JOURNAL -> {
                    if (issue.isEmpty())
                        throw new IllegalArgumentException("Issue number is required for journals.");

                    if (priceText.isEmpty())
                        item = LibraryItemFactory.createJournal(title, person, issue, copies);
                    else
                        item = LibraryItemFactory.createJournal(title, person, issue, priceText, copies);
                }

                default -> throw new IllegalStateException("Unexpected value: " + type);
            }

            itemManager.addItem(item, admin);
            itemManager.saveAll();

            JOptionPane.showMessageDialog(this,
                    "Item added successfully: " + item.getTitle());
            loadAllItemsToSearchTable();
            loadAllItemsToBorrowTable();

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


    private JPanel createUserManagementPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // top: add user form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Register New User"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        newUserNameField = new JTextField(15);
        newUserPassField = new JPasswordField(15);
        newUserEmailField = new JTextField(20);
        addUserButton = new JButton("Add User");

        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("Username:"), c);
        c.gridx = 1;
        form.add(newUserNameField, c);

        c.gridx = 0; c.gridy = 1;
        form.add(new JLabel("Password:"), c);
        c.gridx = 1;
        form.add(newUserPassField, c);

        c.gridx = 0; c.gridy = 2;
        form.add(new JLabel("Email:"), c);
        c.gridx = 1;
        form.add(newUserEmailField, c);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        form.add(addUserButton, c);

        addUserButton.addActionListener(e -> handleAddUser());

        // center: users table
        usersTableModel = new DefaultTableModel(
                new Object[]{"Username", "Email", "Role", "Fine Balance"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usersTable = new JTable(usersTableModel);
        usersTable.setRowHeight(24);
        usersTable.setFillsViewportHeight(true);

        JScrollPane tableScroll = new JScrollPane(usersTable);

        unregisterUserButton = new JButton("Unregister Selected User");
        unregisterUserButton.addActionListener(e -> handleUnregisterUser());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(unregisterUserButton);

        root.add(form, BorderLayout.NORTH);
        root.add(tableScroll, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        refreshUsersTable();

        return root;
    }

    private void refreshUsersTable() {
        usersTableModel.setRowCount(0);
        for (User u : userManager.getAllUsers()) {
            usersTableModel.addRow(new Object[]{
                    u.getUsername(),
                    u.getEmail(),
                    u.getRole(),
                    u.getFineBalance()
            });
        }
    }

    private void handleAddUser() {
        String username = newUserNameField.getText().trim();
        String password = new String(newUserPassField.getPassword());
        String email = newUserEmailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill username, password and email.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            User user = new User(username, Role.USER, password, email);
            userManager.addUser(user);
            userManager.saveAll();
            JOptionPane.showMessageDialog(this, "User registered successfully.");
            newUserNameField.setText("");
            newUserPassField.setText("");
            newUserEmailField.setText("");
            refreshUsersTable();
            // update borrow tab combo as well
            loadUsersIntoCombo();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Add User Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUnregisterUser() {
        int row = usersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to unregister.");
            return;
        }

        String username = (String) usersTableModel.getValueAt(row, 0);
        User user = userManager.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);

        if (user == null) {
            JOptionPane.showMessageDialog(this,
                    "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Unregister user '" + user.getUsername() + "'?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            userManager.unregisterUser(user);
            userManager.saveAll();
            JOptionPane.showMessageDialog(this, "User unregistered.");
            refreshUsersTable();
            loadUsersIntoCombo();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Unregister Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private JPanel createBorrowPanel() {
        JPanel root = new JPanel(new BorderLayout());

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

        userCombo.addActionListener(e -> {
            updateFineBalanceLabel();
            refreshUserBorrowTable();
        });

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

        loadUsersIntoCombo();

        return root;
    }

    private JPanel createBorrowFromItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Available Items"));

        allItemsTableModel = new DefaultTableModel(
                new Object[]{"Type", "Title", "Available / Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        allItemsTable = new JTable(allItemsTableModel);
        allItemsTable.setRowHeight(24);
        allItemsTable.setFillsViewportHeight(true);
        allItemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        borrowButton = new JButton("Borrow Selected");

        // double-click: open edit dialog
        allItemsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = allItemsTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        String title = (String) allItemsTableModel.getValueAt(row, 1);
                        LibraryItem item = itemManager.getAllItems().stream()
                                .filter(i -> i.getTitle().equals(title))
                                .findFirst()
                                .orElse(null);
                        if (item != null) {
                            openEditItemDialog(item);
                        }
                    }
                }
            }
        });

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
                new Object[]{"Item", "Borrow Date", "Due Date", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userBorrowTable = new JTable(userBorrowTableModel);
        userBorrowTable.setRowHeight(24);
        userBorrowTable.setFillsViewportHeight(true);

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

            int available = 0;
            int total = 1;

            if (item instanceof Book b) {
                available = b.getAvailableCopies();
                total = b.getTotalCopies();
            } else if (item instanceof CD cd) {
                available = cd.getAvailableCopies();
                total = cd.getTotalCopies();
            } else if (item instanceof Journal j) {
                available = j.getAvailableCopies();
                total = j.getTotalCopies();
            } else {
                // fallback: old boolean availability
                available = item.isAvailable() ? 1 : 0;
                total = 1;
            }

            String availabilityText = available + " / " + total;

            allItemsTableModel.addRow(new Object[]{
                    item.getMaterialType(),
                    item.getTitle(),
                    availabilityText
            });
        }
    }

    private void handleBorrowItem() {
        User user = getSelectedUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return;
        }

        if (user.getFineBalance() != null
                && user.getFineBalance().compareTo(BigDecimal.ZERO) > 0) {
            JOptionPane.showMessageDialog(this,
                    "This user has unpaid fines (" + user.getFineBalance() + ").\n" +
                            "They must pay the fine before borrowing new items.",
                    "Borrow Not Allowed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = allItemsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to borrow.");
            return;
        }

        String title = (String) allItemsTableModel.getValueAt(row, 1);

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
        if (userBorrowTableModel == null) {
            return;
        }

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
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be positive.");
            }

            BigDecimal currentFine = user.getFineBalance();
            if (amount.compareTo(currentFine) > 0) {
                throw new IllegalArgumentException(
                        "Amount exceeds outstanding fine (" + currentFine + ").");
            }

            borrowManager.payFineForUser(user, amount, LocalDate.now());

            userManager.saveAll();
            updateFineBalanceLabel();
            JOptionPane.showMessageDialog(this, "Fine paid successfully.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Payment Error", JOptionPane.ERROR_MESSAGE);
        }
    }


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

        ReportManager reportManager =
                new ReportManager(borrowManager.getAllBorrowRecords());

        sb.append("=== Top Borrowers ===\n");
        Map<User, Long> topBorrowers =
                reportManager.activity().getTopBorrowers();
        topBorrowers.forEach((user, count) ->
                sb.append(user.getUsername())
                  .append(" -> ")
                  .append(count)
                  .append(" items\n"));

        sb.append("\n=== Most Borrowed Items ===\n");
        Map<String, Long> mostItems =
                reportManager.activity().getMostBorrowedItems();
        mostItems.forEach((label, count) ->
                sb.append(label)
                  .append(" -> ")
                  .append(count)
                  .append(" borrows\n"));

        sb.append("\n=== Total Fines per User (as of ").append(today).append(") ===\n");
        Map<User, BigDecimal> totalFines =
                reportManager.fines().getTotalFinesForAllUsers(today);
        totalFines.forEach((user, fine) ->
                sb.append(user.getUsername())
                  .append(" -> ")
                  .append(fine)
                  .append("\n"));

        sb.append("\n=== Fines by Media Type per User (as of ").append(today).append(") ===\n");
        for (User u : userManager.getAllUsers()) {
            BigDecimal total = totalFines.getOrDefault(u, BigDecimal.ZERO);
            if (total.compareTo(BigDecimal.ZERO) <= 0) {
                continue; 
            }
            sb.append(u.getUsername()).append(":\n");
            Map<MaterialType, BigDecimal> byType =
                    reportManager.fines().getFinesByMediaType(u, today);
            for (MaterialType type : MaterialType.values()) {
                BigDecimal v = byType.getOrDefault(type, BigDecimal.ZERO);
                if (v.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append("  - ")
                      .append(type)
                      .append(": ")
                      .append(v)
                      .append("\n");
                }
            }
        }

        sb.append("\n=== Overdue Items per User (as of ").append(today).append(") ===\n");
        for (User u : userManager.getAllUsers()) {
            List<BorrowRecord> overdue =
                    reportManager.activity().getOverdueItemsForUser(u, today);
            if (overdue.isEmpty()) {
                continue;
            }
            sb.append(u.getUsername()).append(":\n");
            for (BorrowRecord r : overdue) {
                sb.append("  - ")
                  .append(r.getItem().getTitle())
                  .append(" (due ")
                  .append(r.getDueDate())
                  .append(")\n");
            }
        }

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
            List<BorrowRecord> overdue = borrowManager.getOverdueItems(today);

            if (overdue.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "There are no overdue items as of " + today + ". No reminders were sent.");
                return;
            }

            Notifier notifier = new EmailNotifier();
            NotificationManager nm = new NotificationManager(borrowManager);
            nm.sendReminders(notifier, today);

            JOptionPane.showMessageDialog(this,
                    "Overdue reminders have been sent for " + overdue.size() + " overdue borrow records.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Reminders Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void openEditItemDialog(LibraryItem item) {
        JDialog dlg = new JDialog(this, "Edit Item", true);
        dlg.setSize(420, 260);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // current values
        String currentTitle = item.getTitle();
        BigDecimal currentPrice = item.getPrice();
        int currentCopies = 1;
        if (item instanceof Book b) {
            currentCopies = b.getTotalCopies();
        } else if (item instanceof CD cd) {
            currentCopies = cd.getTotalCopies();
        } else if (item instanceof Journal j) {
            currentCopies = j.getTotalCopies();
        }

        JTextField titleField = new JTextField(currentTitle, 20);
        JTextField priceField = new JTextField(currentPrice.toPlainString(), 10);
        JSpinner copiesSpinner = new JSpinner(
                new SpinnerNumberModel(currentCopies, 1, 999, 1));

        c.gridx = 0; c.gridy = 0;
        panel.add(new JLabel("Title:"), c);
        c.gridx = 1;
        panel.add(titleField, c);

        c.gridx = 0; c.gridy = 1;
        panel.add(new JLabel("Price:"), c);
        c.gridx = 1;
        panel.add(priceField, c);

        c.gridx = 0; c.gridy = 2;
        panel.add(new JLabel("Total copies:"), c);
        c.gridx = 1;
        panel.add(copiesSpinner, c);

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        panel.add(buttons, c);

        dlg.setContentPane(panel);

        cancelBtn.addActionListener(e -> dlg.dispose());

        saveBtn.addActionListener(e -> {
            String newTitle = titleField.getText().trim();
            String priceText = priceField.getText().trim();
            int newCopies = (Integer) copiesSpinner.getValue();

            if (newTitle.isEmpty() || priceText.isEmpty()) {
                JOptionPane.showMessageDialog(dlg,
                        "Title and price are required.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                BigDecimal newPrice = new BigDecimal(priceText);
                if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Price must be non-negative.");
                }

                if (item instanceof Book b) {
                    b.setTitle(newTitle);
                    b.setTotalCopies(newCopies);
                    b.setPrice(newPrice);
                } else if (item instanceof CD cd) {
                    cd.setTitle(newTitle);
                    cd.setTotalCopies(newCopies);
                    cd.setPrice(newPrice);
                } else if (item instanceof Journal j) {
                    j.setTitle(newTitle);
                    j.setTotalCopies(newCopies);
                    j.setPrice(newPrice);
                }

                itemManager.saveAll();
                loadAllItemsToSearchTable();
                loadAllItemsToBorrowTable();

                dlg.dispose();
                JOptionPane.showMessageDialog(this,
                        "Item updated successfully.");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg,
                        ex.getMessage(),
                        "Update Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setVisible(true);
    }
}
