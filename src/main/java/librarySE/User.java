package librarySE;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** * Represents a system user within the library system. 
* A user can either be a regular user or an administrator, 
* distinguished by their role attribute.
*
* <p>This class is used as a base for all users of the system, 
* including the Admin class which extends it to gain additional privileges.</p> 
* 
* @author Malak 
*/

public class User {
    
    /** The username of the user. */
    private String username;

    /** The role of the user — can be USER or ADMIN. */
    private Role role; 
    
    /** The hashed password of the user. */
    private String passwordHash;
    private double fineBalance;

    /**
     * Constructs a new {@code User} with the specified username, role, and password.
     * <p>
     * The provided password is automatically hashed using SHA-256 for secure storage.
     * </p>
     *
     * @param username the name identifying the user (must not be {@code null})
     * @param role the role of the user ({@link Role#USER} or {@link Role#ADMIN}), must not be {@code null}
     * @param password the plain-text password (must not be {@code null})
     *
     * @throws IllegalArgumentException if {@code username}, {@code role}, or {@code password} is {@code null}
     */
    public User(String username, Role role, String password) {
        if (username == null || role == null || password == null) {
            throw new IllegalArgumentException("Username, role, and password cannot be null.");
        }
        this.username = username;
        this.role = role;
        this.passwordHash = hashPassword(password);
    }


    /**
     * Returns the username of this user.
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the user's role.
     * 
     * @return the user's role (USER or ADMIN)
     */
    public Role getRole() {
        return role;
    }

    /**
     * Checks whether this user is an administrator.
     * 
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * Verifies whether the provided password matches the stored (hashed) password.
     * 
     * @param enteredPassword the plain-text password to verify
     * @return true if the passwords match, false otherwise
     */
    public boolean checkPassword(String enteredPassword) {
        return passwordHash.equals(hashPassword(enteredPassword));
    }

    /**
     * Changes the user's password if the old password is correct.
     * 
     * @param oldPassword the user's current password
     * @param newPassword the new password to set
     * @return true if the password was successfully changed, false otherwise
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (oldPassword == null || newPassword == null) {
            return false;
        }
        if (checkPassword(oldPassword)) {
            this.passwordHash = hashPassword(newPassword);
            return true;
        }
        return false;
    }

    /**
     * Converts a plain-text password into a SHA-256 hash.
     * 
     * @param password the plain-text password
     * @return the hashed password in hexadecimal format
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
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
     * Returns the current fine balance of the user.
     * 
     * @return the fine balance in monetary units
     */
    public double getFineBalance() {
        return fineBalance;
    }
    /**
     * Adds a fine amount to the user's balance.
     * 
     * @param amount the fine amount to add; must be positive
     * @throws IllegalArgumentException if amount is negative
     */
    public void addFine(double amount) {
        if (amount < 0) throw new IllegalArgumentException("Fine amount cannot be negative");
        fineBalance += amount;
    }
    /**
     * Checks whether the user is allowed to borrow new books.
     * 
     * @return true if fine balance is 0, false otherwise
     */
    public boolean canBorrow() {
        return fineBalance == 0;
    }

/**
     * Pays (reduces) a portion of the user's fine balance.
     * 
     * @param amount the amount to pay; must be positive and not exceed current balance
     * @throws IllegalArgumentException if amount is negative or greater than balance
     */
    public void payFine(double amount) {
        if (amount < 0) throw new IllegalArgumentException("Payment amount cannot be negative");
        if (amount > fineBalance) throw new IllegalArgumentException("Payment exceeds current fine balance");
        fineBalance -= amount;
    }

    /**
     * Returns a string representation of this user 
     * in the format "username (role)".
     * 
     * @return a formatted string describing the user
     */
    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
