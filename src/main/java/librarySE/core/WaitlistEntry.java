package librarySE.core;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import librarySE.utils.ValidationUtils;

/**
 * Represents a single user's request to borrow a library item that is currently unavailable.
 * <p>
 * Each {@code WaitlistEntry} object stores:
 * <ul>
 *     <li>The unique ID of the requested item</li>
 *     <li>The email of the user who is waiting</li>
 *     <li>The date when the user joined the waitlist</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class is immutable and implements {@link Serializable} to support persistence
 * through JSON or other storage formats (e.g., via {@code FileWaitlistRepository}).
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // When a user tries to borrow an unavailable book:
 * WaitlistEntry entry = new WaitlistEntry(
 *     "BOOK-123",
 *     "student@najah.edu",
 *     LocalDate.now()
 * );
 *
 * // Add the entry to the waitlist
 * waitlist.add(entry);
 * waitlistRepo.saveAll(waitlist);
 * }</pre>
 *
 * @author 
 * @see librarySEv2.managers.BorrowManager
 * @see librarySEv2.repo.WaitlistRepository
 */
public class WaitlistEntry implements Serializable {

    /** Ensures version consistency during serialization. */
    private static final long serialVersionUID = 1L;

    /** The unique identifier of the library item being waited for. */
    private final UUID itemId;

    /** The email address of the user waiting for the item. */
    private final String userEmail;

    /** The date when the user was added to the waitlist. */
    private final LocalDate requestDate;

    /**
     * Constructs a new {@code WaitlistEntry} for a given item and user.
     *
     * @param uuid       the unique ID of the library item
     * @param userEmail    the email of the user waiting for the item
     * @param requestDate  the date the user joined the waitlist
     * @throws IllegalArgumentException if any argument is {@code null} or empty
     */
    public WaitlistEntry(UUID uuid, String userEmail, LocalDate requestDate) {
    	ValidationUtils.requireNonEmpty(uuid, "UUID");
    	ValidationUtils.requireNonEmpty(userEmail, "userEmail");
    	ValidationUtils.requireNonEmpty(requestDate, "requestDate");
        this.itemId = uuid;
        this.userEmail = userEmail;
        this.requestDate = requestDate;
    }

    /**
     * Returns the unique ID of the requested item.
     *
     * @return item ID as a {@link String}
     */
    public UUID getItemId() {
        return itemId;
    }

    /**
     * Returns the email address of the user waiting for the item.
     *
     * @return user email as a {@link String}
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Returns the date when the user was added to the waitlist.
     *
     * @return the request date as a {@link LocalDate}
     */
    public LocalDate getRequestDate() {
        return requestDate;
    }

    /**
     * Returns a human-readable string representation of this waitlist entry.
     * Useful for debugging, logging, or report generation.
     *
     * @return a string describing this entry
     */
    @Override
    public String toString() {
        return "WaitlistEntry{" +
                "itemId='" + itemId + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", requestDate=" + requestDate +
                '}';
    }

    /**
     * Checks equality based on {@code itemId}, {@code userEmail}, and {@code requestDate}.
     *
     * @param o the object to compare
     * @return {@code true} if both entries are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WaitlistEntry that)) return false;
        return itemId.equals(that.itemId)
                && userEmail.equals(that.userEmail)
                && requestDate.equals(that.requestDate);
    }

    /**
     * Returns a hash code based on item ID, user email, and request date.
     *
     * @return a consistent hash code for this entry
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(itemId, userEmail, requestDate);
    }
}
