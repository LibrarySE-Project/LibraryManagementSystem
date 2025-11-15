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
 * Each {@code User} has a unique identifier, username, email address,
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
 * @see Role
 * @see ValidationUtils
 * @author Malak
 * @version 2.1
 * @since 2025-11
 */
public class User implements Serializable {

    /** Unique identifier for this user (immutable). */
    private final UUID id;

    /** The username of the user (non-null, trimmed). */
    private String username;

    /** The user’s role — either USER or ADMIN. */
    private Role role;

    /** Hashed version of the user's password (using SHA-256). */
    private String passwordHash;

    /** The user's total fine balance (never negative). */
    private BigDecimal fineBalance;

    /** The user's validated email address (lowercased). */
    private String email;

    /** Minimum allowed password length for security. */
    private static final int MIN_PASSWORD_LENGTH = 6;


    /** Default constructor (used for deserialization or testing). */
    public User() {
        this.id = UUID.randomUUID();
        this.username = "";
        this.email = "";
        this.role = Role.USER;
        this.passwordHash = "";
        this.fineBalance = BigDecimal.ZERO;
    }

    /**
     * Creates a new {@code User} with the provided attributes.
     *
     * @param username user's name (non-null and non-empty)
     * @param role user's role (USER or ADMIN)
     * @param password plain-text password (will be hashed)
     * @param email valid email address
     * @throws IllegalArgumentException if validation fails
     */
    public User(String username, Role role, String password, String email) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(role);
        Objects.requireNonNull(password);
        Objects.requireNonNull(email);
        ValidationUtils.validatePassword(password);
        ValidationUtils.validateEmail(email);
        this.id = UUID.randomUUID();
        this.username = username.trim();
        this.role = role;
        this.passwordHash = hashPassword(password);
        this.fineBalance = BigDecimal.ZERO;
        this.email=email;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public String getEmail() { return email; }
    public BigDecimal getFineBalance() { return fineBalance; }

    /** Updates the username after validation. */
    public void setUsername(String newUsername) {
        ValidationUtils.requireNonEmpty(newUsername, "Username");
        this.username = newUsername.trim();
    }

    /** Updates the email after validation. */
    public void setEmail(String newEmail) {
        ValidationUtils.validateEmail(newEmail);
        this.email = newEmail.trim().toLowerCase();
    }

    /** Checks if this user is an administrator. */
    public boolean isAdmin() { return role == Role.ADMIN; }


    /** Verifies that a given password matches the stored hash. */
    public boolean checkPassword(String entered) {
        if (entered == null || passwordHash == null) return false;
        return passwordHash.equals(hashPassword(entered));
    }

    /**
     * Changes the user’s password if the old password is correct.
     *
     * @param oldPassword the current password
     * @param newPassword the new password (must meet length requirements)
     * @return true if successfully changed; false otherwise
     */
    public boolean changePassword(String oldPassword, String newPassword) {
    	  if (!checkPassword(oldPassword)) return false;
    	    ValidationUtils.validatePassword(newPassword);
    	    this.passwordHash = hashPassword(newPassword);
    	    return true;
    }


    /** Adds a new fine to the user’s balance. */
    public void addFine(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Fine amount cannot be negative");
        fineBalance = fineBalance.add(amount);
    }

    /** Returns whether the user has unpaid fines. */
    public boolean hasOutstandingFine() {
        return fineBalance.compareTo(BigDecimal.ZERO) > 0;
    }

    /** Pays part or all of the user’s current fine balance. */
    public void payFine(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Payment amount cannot be negative");
        if (amount.compareTo(fineBalance) > 0)
            throw new IllegalArgumentException("Payment exceeds current fine balance");
        fineBalance = fineBalance.subtract(amount);
    }

    /**
     * Generates a SHA-256 hash for the given password.
     *
     * @param password the plain-text password
     * @return a hexadecimal string representing the hash
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


    /** Two users are equal if they share the same unique ID. */
    @Override
    public boolean equals(Object o) {
        return o instanceof User u && id.equals(u.id);
    }

    /** Returns hash code based on unique ID. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** Returns readable summary: username (role) - email. */
    @Override
    public String toString() {
        return username + " (" + role + ") - " + email;
    }
}
