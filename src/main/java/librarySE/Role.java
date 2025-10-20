package librarySE;

/**
 * Represents the roles that a user can have in the library system.
 * 
 * <p>
 * There are two roles:
 * <ul>
 *   <li>{@link #USER} - Regular user with standard permissions.</li>
 *   <li>{@link #ADMIN} - Administrator with elevated permissions.</li>
 * </ul>
 * </p>
 * 
 * @author Malak
 */


public enum Role {
    /** Regular user with standard permissions. */
    USER,

    /** Administrator with elevated permissions. */
    ADMIN
}

