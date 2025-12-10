package librarySE.app;

import librarySE.core.LibraryItem;
import librarySE.managers.Admin;
import librarySE.managers.BorrowManager;
import librarySE.managers.BorrowRecord;
import librarySE.managers.ItemManager;
import librarySE.managers.LoginManager;
import librarySE.managers.User;
import librarySE.managers.UserManager;
import librarySE.managers.reports.ReportManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class LibraryUserFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final LoginManager loginManager;
    private final Admin admin;

    private final ItemManager itemManager;
    private final BorrowManager borrowManager;
    private final UserManager userManager;
    private final ReportManager reportManager; 
    private final User loggedInUser;

    // Header
    private JLabel userStatusLabel;
    private JButton logoutButton;

    // Browse & Borrow components
    private JTextField searchField;
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;

    // My Loans & Fines components
    private JLabel fineBalanceLabel;
    private JTable borrowTable;
    private DefaultTableModel borrowTableModel;
    private JButton returnButton;
    private JTextField payAmountField;
    private JButton payFineButton;

    public LibraryUserFrame(LoginManager loginManager,
                            Admin admin,
                            ItemManager itemManager,
                            BorrowManager borrowManager,
                            UserManager userManager,
                            ReportManager reportManager,
                            User loggedInUser) {
        super("Library – User");

        this.loginManager = loginManager;
        this.admin = admin;

        this.itemManager = itemManager;
        this.borrowManager = borrowManager;
        this.userManager = userManager;
        this.reportManager = reportManager;
        this.loggedInUser = loggedInUser;

        initUi();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 650);
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

        JLabel title = new JLabel("Library Management System – User Area");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(60, 40, 70));

        userStatusLabel = new JLabel(
                "Logged in as: " + loggedInUser.getUsername() +
                        "  (" + loggedInUser.getRole() + ")"
        );
        userStatusLabel.setForeground(new Color(60, 40, 70));

        JPanel leftHeader = new JPanel(new GridLayout(2, 1));
        leftHeader.setOpaque(false);
        leftHeader.add(title);
        leftHeader.add(userStatusLabel);

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

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Browse & Borrow", createBrowsePanel());
        tabs.addTab("My Loans & Fines", createMyLoansPanel());

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


    private JPanel createBrowsePanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search:"));
        searchField = new JTextField(25);
        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show all");
        top.add(searchField);
        top.add(searchButton);
        top.add(showAllButton);

        itemsTableModel = new DefaultTableModel(
                new Object[]{"Type", "Title", "Details", "Available?"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemsTable = new JTable(itemsTableModel);
        itemsTable.setRowHeight(24);
        itemsTable.setFillsViewportHeight(true);
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton borrowButton = new JButton("Borrow selected");

        searchButton.addActionListener(e -> refreshBrowseTable(true));
        showAllButton.addActionListener(e -> refreshBrowseTable(false));
        borrowButton.addActionListener(e -> handleBorrow());

        root.add(top, BorderLayout.NORTH);
        root.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        root.add(borrowButton, BorderLayout.SOUTH);

        refreshBrowseTable(false);
        return root;
    }

    private void refreshBrowseTable(boolean useKeyword) {
        List<LibraryItem> list;
        if (useKeyword) {
            String kw = searchField.getText().trim();
            if (kw.isEmpty()) {
                list = itemManager.getAllItems();
            } else {
                list = itemManager.searchItems(kw);
            }
        } else {
            list = itemManager.getAllItems();
        }

        itemsTableModel.setRowCount(0);
        for (LibraryItem item : list) {
            itemsTableModel.addRow(new Object[]{
                    item.getMaterialType(),
                    item.getTitle(),
                    item.toString(),
                    item.isAvailable() ? "Available" : "Not available"
            });
        }
    }

    private void handleBorrow() {
        int row = itemsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item to borrow.");
            return;
        }

        String title = (String) itemsTableModel.getValueAt(row, 1);
        LibraryItem item = itemManager.getAllItems().stream()
                .filter(i -> i.getTitle().equals(title))
                .findFirst()
                .orElse(null);

        if (item == null) {
            JOptionPane.showMessageDialog(this, "Item not found.");
            return;
        }

        try {
            boolean ok = borrowManager.borrowItem(loggedInUser, item);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Borrowed successfully.\nDue date depends on media type.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Item is not available; you have been added to the waitlist.");
            }
            refreshBrowseTable(false);
            refreshLoansTable();
            updateFineLabel();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Borrow error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    private JPanel createMyLoansPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new BorderLayout());
        fineBalanceLabel = new JLabel();
        fineBalanceLabel.setFont(fineBalanceLabel.getFont().deriveFont(Font.BOLD));
        fineBalanceLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        top.add(fineBalanceLabel, BorderLayout.WEST);

        borrowTableModel = new DefaultTableModel(
                new Object[]{"Item", "Borrow date", "Due date", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        borrowTable = new JTable(borrowTableModel);
        borrowTable.setRowHeight(24);
        borrowTable.setFillsViewportHeight(true);
        borrowTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh");
        returnButton = new JButton("Return selected");
        payAmountField = new JTextField(8);
        payFineButton = new JButton("Pay fine");

        bottom.add(refreshButton);
        bottom.add(returnButton);
        bottom.add(new JLabel("Amount:"));
        bottom.add(payAmountField);
        bottom.add(payFineButton);

        refreshButton.addActionListener(e -> {
            refreshLoansTable();
            updateFineLabel();
        });
        returnButton.addActionListener(e -> handleReturn());
        payFineButton.addActionListener(e -> handlePayFine());

        root.add(top, BorderLayout.NORTH);
        root.add(new JScrollPane(borrowTable), BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        refreshLoansTable();
        updateFineLabel();

        return root;
    }

    private void refreshLoansTable() {
        borrowTableModel.setRowCount(0);
        List<BorrowRecord> records =
                borrowManager.getBorrowRecordsForUser(loggedInUser);
        LocalDate today = LocalDate.now();

        for (BorrowRecord r : records) {
            borrowTableModel.addRow(new Object[]{
                    r.getItem().getTitle(),
                    r.getBorrowDate(),
                    r.getDueDate(),
                    r.getStatus() + (r.isOverdue(today) ? " (OVERDUE)" : "")
            });
        }
    }

    private void updateFineLabel() {
        BigDecimal balance = loggedInUser.getFineBalance();
        fineBalanceLabel.setText("Current fine balance: " + balance + " NIS");
    }

    private void handleReturn() {
        int row = borrowTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a borrow record to return.");
            return;
        }

        String title = (String) borrowTableModel.getValueAt(row, 0);
        LibraryItem item = itemManager.getAllItems().stream()
                .filter(i -> i.getTitle().equals(title))
                .findFirst()
                .orElse(null);

        if (item == null) {
            JOptionPane.showMessageDialog(this, "Item not found.");
            return;
        }

        try {
            borrowManager.returnItem(loggedInUser, item);
            JOptionPane.showMessageDialog(this, "Item returned successfully.");
            refreshBrowseTable(false);
            refreshLoansTable();
            updateFineLabel();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Return error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePayFine() {
        String amountText = payAmountField.getText().trim();
        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter an amount to pay.");
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountText);
            loggedInUser.payFine(amount);
            userManager.saveAll();
            updateFineLabel();
            JOptionPane.showMessageDialog(this,
                    "Fine paid successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Payment error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

