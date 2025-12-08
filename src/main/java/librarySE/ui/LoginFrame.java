package librarySE.ui;

import librarySE.managers.LoginManager;

import javax.swing.*;
import java.awt.*;

/**
 * Fancy Swing login window with show/hide password toggle.
 */
public class LoginFrame extends JFrame {

    private final LoginManager loginManager;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JLabel lblStatus;

    private char defaultEchoChar;

    public LoginFrame(LoginManager loginManager) {
        this.loginManager = loginManager;
        initUi();
    }

    private void initUi() {
        setTitle("Library System – Admin Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 320);
        setLocationRelativeTo(null); // center on screen
        setResizable(false);

        // ====== main content: gradient background + centered card ======
        setContentPane(new GradientPanel());

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel lblTitle = new JLabel("Library Admin Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        card.add(lblTitle, gbc);

        // Username label + field
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setForeground(Color.WHITE);
        card.add(lblUser, gbc);

        gbc.gridx = 1;
        txtUsername = new JTextField(18);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(txtUsername, gbc);

        // Password label + field
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setForeground(Color.WHITE);
        card.add(lblPass, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(18);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        defaultEchoChar = txtPassword.getEchoChar();
        card.add(txtPassword, gbc);

        // Show password checkbox
        gbc.gridx = 1;
        gbc.gridy++;
        chkShowPassword = new JCheckBox("Show password");
        chkShowPassword.setOpaque(false);
        chkShowPassword.setForeground(Color.WHITE);
        chkShowPassword.addActionListener(e -> togglePasswordVisibility());
        card.add(chkShowPassword, gbc);

        // Status label
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setForeground(new Color(255, 230, 230));
        card.add(lblStatus, gbc);

        // Buttons row
        gbc.gridy++;
        gbc.gridwidth = 2;

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttons.setOpaque(false);

        JButton btnLogin = new JButton("Login");
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.addActionListener(e -> doLogin());

        JButton btnExit = new JButton("Exit");
        btnExit.setFocusPainted(false);
        btnExit.addActionListener(e -> System.exit(0));

        buttons.add(btnLogin);
        buttons.add(btnExit);

        card.add(buttons, gbc);

        // Add card to center of gradient background
        setLayout(new GridBagLayout());
        add(card);

        // Enter key = login
        getRootPane().setDefaultButton(btnLogin);
    }

    private void togglePasswordVisibility() {
        if (chkShowPassword.isSelected()) {
            txtPassword.setEchoChar((char) 0); // show characters
        } else {
            txtPassword.setEchoChar(defaultEchoChar); // hide again
        }
    }

    private void doLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        try {
            boolean ok = loginManager.login(user, pass);
            if (ok) {
                lblStatus.setForeground(new Color(210, 255, 210));
                lblStatus.setText("✅ Login successful. Welcome, " + user + "!");
                // TODO: افتحي الـ Dashboard هنا
                // new MainDashboardFrame(...).setVisible(true);
                // dispose();
            } else {
                lblStatus.setForeground(new Color(255, 200, 200));
                lblStatus.setText("❌ Invalid username or password.");
            }
        } catch (IllegalArgumentException ex) {
            lblStatus.setForeground(new Color(255, 200, 200));
            lblStatus.setText("⚠ " + ex.getMessage());
        }
    }

    /**
     * Simple panel with a vertical blue-purple gradient background.
     */
    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth();
            int h = getHeight();
            Color c1 = new Color(54, 84, 134);  // top
            Color c2 = new Color(96, 67, 132);  // bottom
            GradientPaint gp = new GradientPaint(0, 0, c1, 0, h, c2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }
}
