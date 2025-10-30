package librarySE;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Represents a system user within the library system.
 * <p>
 * A user can either be a regular user or an administrator, distinguished by their role.
 * Each user has a unique ID, username, role, email, hashed password, and fine balance.
 * </p>
 * 
 * <p>
 * This class provides functionality for:
 * <ul>
 *     <li>Password verification and change (with minimum length check).</li>
 *     <li>Fine management (adding and paying fines).</li>
 *     <li>Email validation.</li>
 *     <li>Logical equality based on unique user ID.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Users are considered equal if they have the same unique ID, regardless of username, email, or role.
 * </p>
 * 
 * @see Role 
 * @author Eman
 */
public class User {

    /** Unique identifier for the user (UUID). */
    private final String userId;

    /** The username of the user (trimmed, non-null). */
    private String username;

    /** The role of the user — can be USER or ADMIN (non-null). */
    private Role role;

    /** The hashed password of the user (non-null). */
    private String passwordHash;

    /** The current fine balance of the user. */
    private BigDecimal fineBalance;

    /** The user's email address (lowercase, trimmed, must be valid). */
    private String email;

    /** Regular expression pattern for validating email addresses. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.(com|net|edu|org|ps)$"
    );

    /** Minimum password length when changing passwords. */
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * Constructs a new {@code User} with a unique ID.
     *
     * @param username the name identifying the user (must not be {@code null})
     * @param role the user's role ({@link Role#USER} or {@link Role#ADMIN}), must not be {@code null}
     * @param password the plain-text password (must not be {@code null})
     * @param email the user's email address (must follow a valid format)
     * @throws IllegalArgumentException if any parameter is {@code null} or invalid
     */
    public User(String username, Role role, String password, String email) {
        if (username == null || role == null || password == null || email == null) {
            throw new IllegalArgumentException("Username, role, password, and email cannot be null.");
        }

        this.userId = UUID.randomUUID().toString();
        this.username = username.trim();
        this.role = role;
        this.passwordHash = hashPassword(password);
        this.fineBalance = BigDecimal.ZERO;
        setEmail(email); // validation and lowercase handled in setEmail
    }

    /** Returns the unique ID of the user. */
    public String getUserId() {
        return userId;
    }

    /** Returns the username of the user. */
    public String getUsername() {
        return username;
    }

    /** Returns the role of the user. */
    public Role getRole() {
        return role;
    }

    /** Returns the user's email address. */
    public String getEmail() {
        return email;
    }
    /**
     * Sets a new username for the user.
     * <p>
     * The username is trimmed of leading and trailing spaces and must not be {@code null} or empty.
     * </p>
     *
     * @param newUsername the new username to set
     * @throws IllegalArgumentException if {@code newUsername} is {@code null} or empty after trimming
     */
    public void setUsername(String newUsername) {
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        this.username = newUsername.trim();
    }


    /**
     * Sets the user's email after validating format.
     *
     * @param newEmail the new email
     * @throws IllegalArgumentException if null or invalid
     */
    public void setEmail(String newEmail) {
        if (newEmail == null) {
            throw new IllegalArgumentException("Email cannot be null.");
        }
        String trimmedEmail = newEmail.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Invalid email format. Example: name@gmail.com or user@najah.edu");
        }
        this.email = trimmedEmail;
    }

    /** Checks if the user is an administrator. */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * Verifies the password.
     *
     * @param enteredPassword plain-text password
     * @return true if matches
     */
    public boolean checkPassword(String enteredPassword) {
        if (enteredPassword == null || passwordHash == null) return false;
        return passwordHash.equals(hashPassword(enteredPassword));
    }

    /**
     * Changes the password if old password matches.
     *
     * @param oldPassword current password
     * @param newPassword new password (must meet MIN_PASSWORD_LENGTH)
     * @return true if changed successfully
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (oldPassword == null || newPassword == null) return false;
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("New password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }
        if (checkPassword(oldPassword)) {
            this.passwordHash = hashPassword(newPassword);
            return true;
        }
        return false;
    }

    /** Returns the user's current fine balance. */
    public BigDecimal getFineBalance() {
        return fineBalance;
    }

    /**
     * Adds a fine to the user's balance.
     *
     * @param amount positive fine amount
     */
    public void addFine(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Fine amount cannot be negative");
        fineBalance = fineBalance.add(amount);
    }

    /** Returns true if the user has unpaid fines. */
    public boolean hasOutstandingFine() {
        return fineBalance.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Pays a portion of the fine balance.
     *
     * @param amount positive amount not exceeding balance
     */
    public void payFine(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Payment amount cannot be negative");
        if (amount.compareTo(fineBalance) > 0)
            throw new IllegalArgumentException("Payment exceeds current fine balance");
        fineBalance = fineBalance.subtract(amount);
    }

    /** Hashes a password using SHA-256. */
    private static String hashPassword(String password) {
        return hashPassword(password, "SHA-256");
    }

    /** Hashes password using a specified algorithm. */
    protected static String hashPassword(String password, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Logical equality based on unique userId.
     * <p>Even if username/email/role differs, two users are equal only if userId matches.</p>
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        User other = (User) obj;
        return userId.equals(other.userId);
    }

    /** Hash code based on unique userId for correct collection behavior. */
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    /** Returns formatted string: username (role) - email */
    @Override
    public String toString() {
        return username + " (" + role + ") - " + email;
    }
}

