package librarySE;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a {@link LibraryItem} of type CD (Compact Disc) in the library system.
 * <p>
 * Each {@code CD} instance stores identifying information such as a unique ID, title, and artist.
 * It inherits core functionality including borrowing, returning, and availability
 * management from {@link AbstractLibraryItem}, ensuring consistent thread-safe behavior
 * across all library materials.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *     <li>Encapsulates metadata specific to CDs (title and artist).</li>
 *     <li>Automatically generates a unique {@link UUID} for each CD instance.</li>
 *     <li>Supports keyword-based search across title and artist fields.</li>
 *     <li>Integrates seamlessly with {@link BorrowManager} and fine calculation logic via {@link FineContext}.</li>
 * </ul>
 *
 * <p>
 * This class follows the <b>Single Responsibility Principle</b> — it focuses solely
 * on representing a CD entity, while borrowing logic is handled by
 * {@link AbstractLibraryItem} and {@link BorrowManager}.
 * </p>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * CD cd = new CD("Classical Symphony", "Beethoven");
 * System.out.println(cd.getTitle());      // "Classical Symphony"
 * System.out.println(cd.getArtist());     // "Beethoven"
 * System.out.println(cd.isAvailable());   // true
 * cd.borrow();
 * System.out.println(cd.isAvailable());   // false
 * }</pre>
 *
 * @see LibraryItem
 * @see AbstractLibraryItem
 * @see MaterialType
 * @see BorrowManager
 * @see FineContext
 * @see CDFineStrategy
 *
 * author Malak
 */
public class CD extends AbstractLibraryItem {

    /** Unique identifier for this CD (automatically generated). */
    private final UUID id = UUID.randomUUID();

    /** The title of the CD (e.g., album or compilation name). */
    private String title;

    /** The artist or performer of the CD. */
    private String artist;

    /**
     * Constructs a new {@code CD} instance with the specified title and artist.
     *
     * @param title  the title of the CD; must not be {@code null} or empty
     * @param artist the artist or performer; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code title} or {@code artist} is {@code null} or empty
     */
    public CD(String title, String artist) {
        validateNonEmpty(title, "Title");
        validateNonEmpty(artist, "Artist");
        this.title = title.trim();
        this.artist = artist.trim();
    }

    /**
     * Returns the title of this CD.
     *
     * @return the CD title as a {@link String}
     */
    @Override
    public String getTitle() { return title; }

    /**
     * Returns the artist of this CD.
     *
     * @return the artist name as a {@link String}
     */
    public String getArtist() { return artist; }

    /**
     * Updates the title of this CD.
     *
     * @param title the new title; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code title} is {@code null} or empty
     */
    public void setTitle(String title) {
        validateNonEmpty(title, "Title");
        this.title = title.trim();
    }

    /**
     * Updates the artist name of this CD.
     *
     * @param artist the new artist; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code artist} is {@code null} or empty
     */
    public void setArtist(String artist) {
        validateNonEmpty(artist, "Artist");
        this.artist = artist.trim();
    }

    /**
     * Returns the material type of this item.
     *
     * @return {@link MaterialType#CD}
     */
    @Override
    public MaterialType getMaterialType() { return MaterialType.CD; }

    /**
     * Checks whether this CD matches a given keyword.
     * <p>
     * A match occurs if the keyword (case-insensitive) appears in
     * the title or artist name.
     * </p>
     *
     * @param keyword the keyword to search for; must not be {@code null}
     * @return {@code true} if this CD matches the keyword; {@code false} otherwise
     * @throws IllegalArgumentException if {@code keyword} is {@code null}
     */
    @Override
    public boolean matchesKeyword(String keyword) {
        if (keyword == null)
            throw new IllegalArgumentException("Keyword cannot be null.");
        String lower = keyword.toLowerCase();
        return String.join(" ", title, artist).toLowerCase().contains(lower);
    }

    /**
     * Compares this CD to another object for equality.
     * <p>
     * Two CDs are considered equal if they share the same unique identifier.
     * </p>
     *
     * @param obj the object to compare with
     * @return {@code true} if the other object is a {@code CD} with the same unique ID
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof CD cd && id.equals(cd.id);
    }

    /**
     * Returns a hash code value for this CD based on its unique identifier.
     *
     * @return the hash code for this CD
     */
    @Override
    public int hashCode() { return Objects.hash(id); }

    /**
     * Returns a formatted string representation of the CD,
     * including its title, artist, and availability status.
     *
     * @return a human-readable string describing this CD
     */
    @Override
    public String toString() {
        return String.format("[CD] %s by %s — %s",
                title, artist, isAvailable() ? "Available" : "Borrowed");
    }
}
