package librarySE.app;

import librarySE.core.LibraryItem;
import librarySE.managers.BorrowManager;
import librarySE.managers.BorrowRecord;
import librarySE.managers.ItemManager;
import  librarySE.managers.reports.*;
import librarySE.managers.User;
import librarySE.managers.UserManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * GUI for normal library users (no admin actions).
 * <p>
 * Features for the logged-in user:
 *  - Browse & search items.
 *  - Borrow available items.
 *  - View own active/previous borrows.
 *  - Return borrowed items.
 *  - Pay fines.
 */
public class LibraryUserFrame  extends JFrame {

    private final ItemManager itemManager;
    private final BorrowManager borrowManager;
    private final UserManager userManager;
    private final ReportManager reportManager; // (ممكن تستغليه لاحقاً لتقارير شخصية)
    private final User loggedInUser;

    // Browse & Borrow components
    private JTextField searchField;
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;

    // My Loans & Fines components
    private JLabel userInfoLabel;
    private JLabel fineBalanceLabel;
    private JTable borrowTable;
    private DefaultTableModel borrowTableModel;
    private JButton returnButton;
    private JTextField payAmountField;
    private JButton payFineButton;

    public LibraryUserFrame(ItemManager itemManager,
                            BorrowManager borrowManager,
                            UserManager userManager,
                            ReportManager reportManager,
                            User loggedInUser) {
        super("Library - User: " + loggedInUser.getUsername());

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
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Browse & Borrow", createBrowsePanel());
        tabs.addTab("My Loans & Fines", createMyLoansPanel());

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }

    // ==================== Browse & Borrow ====================

    private JPanel createBrowsePanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

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
        itemsTable.setRowHeight(22);

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

    // ==================== My Loans & Fines ====================

    private JPanel createMyLoansPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new BorderLayout());
        userInfoLabel = new JLabel(
                "Logged in as: " + loggedInUser.getUsername() + "  (" + loggedInUser.getRole() + ")");
        userInfoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        top.add(userInfoLabel, BorderLayout.NORTH);

        fineBalanceLabel = new JLabel();
        fineBalanceLabel.setFont(fineBalanceLabel.getFont().deriveFont(Font.BOLD));
        top.add(fineBalanceLabel, BorderLayout.SOUTH);

        borrowTableModel = new DefaultTableModel(
                new Object[]{"Item", "Borrow date", "Due date", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        borrowTable = new JTable(borrowTableModel);
        borrowTable.setRowHeight(22);

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
