package librarySE.app;

import librarySE.managers.Admin;
import librarySE.managers.BorrowManager;
import librarySE.managers.ItemManager;
import librarySE.managers.LoginManager;
import  librarySE.managers.reports.*;
import librarySE.managers.Role;
import librarySE.managers.User;
import librarySE.managers.UserManager;

import javax.swing.*;
import java.awt.*;


public class LibraryLoginFrame extends JFrame {

    private final LoginManager loginManager;
    private final Admin admin;
    private final ItemManager itemManager;
    private final BorrowManager borrowManager;
    private final UserManager userManager;
    private final ReportManager reportManager;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JToggleButton showPasswordButton;
    private JButton loginButton;

    private char defaultEchoChar;

    public LibraryLoginFrame(LoginManager loginManager,
                             Admin admin,
                             ItemManager itemManager,
                             BorrowManager borrowManager,
                             UserManager userManager,
                             ReportManager reportManager) {
        super("Library System – Login");

        this.loginManager = loginManager;
        this.admin = admin;
        this.itemManager = itemManager;
        this.borrowManager = borrowManager;
        this.userManager = userManager;
        this.reportManager = reportManager;

        initUi();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(520, 330);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initUi() {
        JPanel gradientPanel = new JPanel() {
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
        gradientPanel.setLayout(new GridBagLayout());
        gradientPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;

        JLabel title = new JLabel("Library Management System", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(new Color(60, 40, 70));

        c.gridy = 0;
        c.weightx = 1.0;
        gradientPanel.add(title, c);

        JLabel subtitle = new JLabel("Sign in to your library account", SwingConstants.CENTER);
        subtitle.setForeground(new Color(80, 60, 90));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 13f));

        c.gridy = 1;
        gradientPanel.add(subtitle, c);

        // ====== Form panel ======
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false); 
        GridBagConstraints f = new GridBagConstraints();
        f.insets = new Insets(6, 6, 6, 6);
        f.fill = GridBagConstraints.HORIZONTAL;
        f.gridx = 0;
        f.gridy = 0;

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(new Color(40, 30, 60));
        formPanel.add(userLabel, f);

        f.gridx = 1;
        usernameField = new JTextField(18);
        formPanel.add(usernameField, f);

        // Password row
        f.gridx = 0;
        f.gridy = 1;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(new Color(40, 30, 60));
        formPanel.add(passLabel, f);

        f.gridx = 1;
        passwordField = new JPasswordField(18);
        formPanel.add(passwordField, f);

        defaultEchoChar = passwordField.getEchoChar();

        f.gridx = 0;
        f.gridy = 2;
        f.gridwidth = 2;
        f.anchor = GridBagConstraints.CENTER;

        showPasswordButton = new JToggleButton("Show password");
        showPasswordButton.setFocusPainted(false);
        showPasswordButton.setBorderPainted(false);
        showPasswordButton.setContentAreaFilled(false);
        showPasswordButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showPasswordButton.setForeground(new Color(60, 20, 120));
        showPasswordButton.setFont(showPasswordButton.getFont().deriveFont(Font.BOLD, 12f));

        showPasswordButton.addActionListener(e -> togglePasswordVisibility());

        formPanel.add(showPasswordButton, f);

        f.gridy = 3;
        loginButton = new JButton("Login");
        loginButton.setFont(loginButton.getFont().deriveFont(Font.BOLD, 16f));
        loginButton.setForeground(new Color(50, 40, 70));
        loginButton.setBackground(new Color(255, 250, 250));
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 130, 180)),
                BorderFactory.createEmptyBorder(5, 25, 5, 25)
        ));

        formPanel.add(loginButton, f);

        c.gridy = 2;
        gradientPanel.add(formPanel, c);


        loginButton.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin()); 

        setContentPane(gradientPanel);
    }

    private void togglePasswordVisibility() {
        if (showPasswordButton.isSelected()) {
            passwordField.setEchoChar((char) 0);
            showPasswordButton.setText("Hide password");
        } else {
            passwordField.setEchoChar(defaultEchoChar);
            showPasswordButton.setText("Show password");
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username.");
            return;
        }

        try {
            boolean adminOk = loginManager.login(username, password);
            if (adminOk) {
                JOptionPane.showMessageDialog(this,
                        "Welcome Admin " + admin.getUsername() + "!");
                openAdminGui();
                return;
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = userManager.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(username)
                        && u.getRole() == Role.USER)
                .findFirst()
                .orElse(null);

        if (user == null) {
            JOptionPane.showMessageDialog(this,
                    "Invalid credentials.\nNo admin or user found with this username.",
                    "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Welcome " + user.getUsername() + "!");

        openUserGui(user);
    }

    private void openAdminGui() {
        LibraryMainFrame frame = new LibraryMainFrame(
                loginManager,
                admin,
                itemManager,
                borrowManager,
                userManager,
                reportManager
        );
        frame.setVisible(true);
        dispose();
    }


	private void openUserGui(User user) {
	    LibraryUserFrame userFrame = new LibraryUserFrame(
	            loginManager,   
	            admin,          
	            itemManager,
	            borrowManager,
	            userManager,
	            reportManager,
	            user
	    );
	    userFrame.setVisible(true);
	    dispose();
	}
}
