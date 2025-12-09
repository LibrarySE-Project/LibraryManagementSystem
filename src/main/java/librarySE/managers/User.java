package librarySE.managers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import librarySE.utils.ValidationUtils;

/**
 * Represents a system user within the library management system.
 * <p>
 * Each {@code User} has a unique identifier, username,   email address,
 * role (either {@link Role#USER} or {@link Role#ADMIN}), password hash,
 * and a fine balance for overdue materials.
 * </p>
 *
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *   <li>Encapsulates user-related data and behavior (login, fines, roles).</li>
 *   <li>Handles password hashing and validation securely via SHA-256.</li>
 *   <li>Provides fine management operations (add, pay, check outstanding fines).</li>
 *   <li>Uses {@link ValidationUtils} for consistent field validation.</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * User u = new User("Eman", Role.USER, "mypassword", "eman@najah.edu");
 * System.out.println(u.isAdmin()); // false
 * u.addFine(BigDecimal.valueOf(15));
 * System.out.println(u.getFineBalance()); // 15
 * }</pre>
 *
 * @author Eman
 * 
 */
public class User implements Serializable {

    /**
     * Unique identifier for the user.
     * <p>This ID is automatically generated and never changes.</p>
     */
    private final UUID id;

    /**
     * The name used to identify the user in the system.
     * <p>Must be non-null and non-empty.</p>
     */
    private String username;

    /**
     * The assigned role of this user.
     * <p>Determines system permissions such as admin actions.</p>
     */
    private Role role;

    /**
     * A SHA-256 hashed representation of the user's password.
     * <p>Raw passwords are never stored directly.</p>
     */
    private String passwordHash;

    /**
     * The user's outstanding fines.
     * <p>Always non-negative. Defaults to zero.</p>
     */
    private BigDecimal fineBalance;

    /**
     * User's validated and normalized (lowercased) email address.
     */
    private String email;

    /**
     * Minimum password length required by the system.
     * <p>Currently six characters as defined in requirements.</p>
     */
    private static final int MIN_PASSWORD_LENGTH = 6;



    /**
     * Default constructor used primarily for deserialization.
     * <p>Initializes safe default values.</p>
     */
    public User() {
        this.id = UUID.randomUUID();
        this.username = "";
        this.email = "";
        this.role = Role.USER;
        this.passwordHash = "";
        this.fineBalance = BigDecimal.ZERO;
    }

    /**
     * Creates a fully initialized user with validation.
     *
     * @param username user's chosen name (cannot be null or blank)
     * @param role USER or ADMIN
     * @param password plain-text password (validated & hashed)
     * @param email email address (validated and normalized)
     *
     * @throws IllegalArgumentException if validation fails
     */
    public User(String username, Role role, String password, String email) {
        Objects.requireNonNull(username, "Username is required.");
        Objects.requireNonNull(role, "Role is required.");
        Objects.requireNonNull(password, "Password is required.");
        Objects.requireNonNull(email, "Email is required.");

        String trimmedPassword = password.trim();
        ValidationUtils.validatePassword(trimmedPassword);
        ValidationUtils.validateEmail(email);

        this.id = UUID.randomUUID();
        this.username = username.trim();
        this.role = role;
        this.passwordHash = hashPassword(trimmedPassword);
        this.fineBalance = BigDecimal.ZERO;
        this.email = email.trim().toLowerCase();
    }


    // ========================================================================
    // Getters
    // ========================================================================

    /**
     * @return immutable UUID of the user
     */
    public UUID getId() { return id; }

    /**
     * @return the username of this user
     */
    public String getUsername() { return username; }

    /**
     * @return the user's assigned role
     */
    public Role getRole() { return role; }

    /**
     * @return user's stored email address
     */
    public String getEmail() { return email; }

    /**
     * @return current fine balance (never negative)
     */
    public BigDecimal getFineBalance() { return fineBalance; }


    // ========================================================================
    // Mutators
    // ========================================================================

    /**
     * Updates the user's username after validation.
     *
     * @param newUsername non-empty string
     * @throws IllegalArgumentException if validation fails
     */
    public void setUsername(String newUsername) {
        ValidationUtils.requireNonEmpty(newUsername, "Username");
        this.username = newUsername.trim();
    }

    /**
     * Updates the user's email address after validation.
     *
     * @param newEmail must match email pattern
     * @throws IllegalArgumentException if the email is invalid
     */
    public void setEmail(String newEmail) {
        ValidationUtils.validateEmail(newEmail);
        this.email = newEmail.trim().toLowerCase();
    }


    // ========================================================================
    // Role & Authentication
    // ========================================================================

    /**
     * @return true if the user has ADMIN privileges
     */
    public boolean isAdmin() { return role == Role.ADMIN; }

    /**
     * Verifies that the provided password matches the stored hash.
     *
     * @param entered plain-text password
     * @return true if correct; false otherwise
     */
    public boolean checkPassword(String entered) {
        if (entered == null || passwordHash == null) return false;
        return passwordHash.equals(hashPassword(entered.trim()));
    }

    /**
     * Attempts to update the user's password after confirming the old password.
     *
     * @param oldPassword current password
     * @param newPassword new password (validated and hashed)
     * @return true if successfully updated
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (!checkPassword(oldPassword)) return false;

        String trimmed = newPassword.trim();
        ValidationUtils.validatePassword(trimmed);
        this.passwordHash = hashPassword(trimmed);
        return true;
    }


    // ========================================================================
    // Fine System
    // ========================================================================

    /**
     * Adds a new fine amount to the user's total.
     *
     * @param amount amount of fine to add (must be ≥ 0)
     */
    public void addFine(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Fine amount cannot be negative");
        fineBalance = fineBalance.add(amount);
    }

    /**
     * @return true if the user owes any fines
     */
    public boolean hasOutstandingFine() {
        return fineBalance.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Reduces the user's fine balance by a payment amount.
     *
     * @param amount amount to pay (must be ≥ 0 and ≤ current balance)
     */
    public void payFine(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Payment amount cannot be negative");
        if (amount.compareTo(fineBalance) > 0)
            throw new IllegalArgumentException("Payment exceeds current fine balance");
        fineBalance = fineBalance.subtract(amount);
    }


    // ========================================================================
    // Password Hashing
    // ========================================================================

    /**
     * Converts a plain-text password into a SHA-256 secure hash.
     *
     * @param password plain text password
     * @return hashed hex string
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }


    // ========================================================================
    // Overrides
    // ========================================================================

    /**
     * Users are equal if they share the same UUID.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof User u && id.equals(u.id);
    }

    /**
     * @return hash code derived from UUID
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * @return formatted string: "username (ROLE) - email"
     */
    @Override
    public String toString() {
        return username + " (" + role + ") - " + email;
    }
}
