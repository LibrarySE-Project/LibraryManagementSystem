package librarySE.managers;


/**
 * Represents the role assigned to a {@code User} within the library system.
 * <p>
 * The {@code Role} enumeration defines access permissions and responsibilities
 * for different types of users. It helps enforce authorization rules
 * throughout the application (e.g., who can add or remove books, who can borrow).
 * </p>
 *
 * <h3>Defined Roles:</h3>
 * <ul>
 *   <li>{@link #USER} — Regular library user; can browse, search, and borrow materials.</li>
 *   <li>{@link #ADMIN} — System administrator; can manage users, inventory, and configurations.</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * Role userRole = Role.USER;
 * System.out.println(userRole.getDescription()); // "Can borrow & search"
 *
 * Role adminRole = Role.ADMIN;
 * System.out.println(adminRole.getDescription()); // "Can add/delete & manage users & items"
 * }</pre>
 *
 * @author Malak
 * @see librarySEv2.core.User
 */
public enum Role {

    /** Standard user with borrowing and search privileges. */
    USER("Can borrow & search"),

    /** Administrator with full management permissions. */
    ADMIN("Can add/delete & manage users & items");

    /** A short human-readable description of the role’s permissions. */
    private final String desc;

    /**
     * Constructs a new {@code Role} with a textual description.
     *
     * @param d the description of this role's capabilities
     */
    Role(String d) {
        this.desc = d;
    }

    /**
     * Returns a short description of this role’s privileges.
     *
     * @return a string describing what actions this role can perform
     */
    public String getDescription() {
        return desc;
    }
}
