package librarySE;

/**
 * Enumerates the different types of materials available in the library system.
 * <p>
 * Each {@code MaterialType} represents a distinct category of library items that 
 * can be borrowed, returned, and managed within the system.
 * </p>
 *
 * <p>
 * The supported material types are:
 * <ul>
 *   <li>{@link #BOOK} — Represents a standard book item.</li>
 *   <li>{@link #CD} — Represents a compact disc item (e.g., audio or software CD).</li>
 *   <li>{@link #JOURNAL} — Represents an academic or scientific journal.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This enumeration is primarily used by {@link LibraryItem} and its subclasses 
 * to define the specific type of material being represented.
 * </p>
 *
 * @see LibraryItem
 * @author Malak
 */
public enum MaterialType {
    /** Represents a book item in the library. */
    BOOK,

    /** Represents a CD item (e.g., audio or multimedia disc). */
    CD,

    /** Represents a journal or periodical publication. */
    JOURNAL
}

