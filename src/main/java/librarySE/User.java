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

    /**
     * Constructs a new User with the specified username, role, and password.
     *
     * @param username the name identifying the user
     * @param role the role of the user ("user" or "admin")
     * @param password the password of the user
     */

    public User(String username, Role role,String password) {
        this.username = username;
        this.password = password;
        this.role = role;
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
