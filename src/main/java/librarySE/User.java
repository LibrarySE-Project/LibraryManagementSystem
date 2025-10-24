package librarySE;

/**
 * Represents a system user within the library system.
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

    /** The role of the user — can be "user" or "admin". */
    private Role role; 
    
    /** The password of the user (protected for subclass access). */
    protected String password;
    
    /** The current fine balance of the user. Users cannot borrow new books until it is 0. */
    private double fineBalance;

    /**
     * Constructs a new User with the specified username, role, and password.
     *
     * @param username the name identifying the user
     * @param role the role of the user ("user" or "admin")
     * @param password the password of the user
     */
    public User(String username, Role role, String password) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fineBalance = 0; // default fine balance is 0
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
     * Returns the password of this user.
     * <p>
     * Note: In production systems, it's generally better to use 
     * {@link #checkPassword(String)} instead of exposing the password directly
     * for security reasons.
     * </p>
     * 
     * @return the user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Verifies whether the provided password matches the user's password.
     * 
     * @param enteredPassword the password to verify
     * @return true if the passwords match, false otherwise
     */
    public boolean checkPassword(String enteredPassword) {
        return this.password.equals(enteredPassword);
    }

    /**
     * Returns the role of this user.
     * 
     * @return the user's role ("user" or "admin")
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
     * Checks whether the user is allowed to borrow new books.
     * 
     * @return true if fine balance is 0, false otherwise
     */
    public boolean canBorrow() {
        return fineBalance == 0;
    }

    /**
     * Returns a string representation of this user 
     * in the format "username (role) [Fine: x]".
     * 
     * @return a formatted string describing the user
     */
    @Override
    public String toString() {
        return username + " (" + role + ") [Fine: " + fineBalance + "]";
    }
}

