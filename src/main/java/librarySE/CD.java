package librarySE;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a CD (music disc) in the library system.
 * <p>
 * Each CD has a unique identifier, a title, an artist, and an availability status.
 * Implements {@link LibraryItem} to support borrowing and returning functionality.
 * </p>
 * 
 * <p>
 * Thread-safe operations are provided for checking availability, borrowing, and returning the CD.
 * </p>
 * 
 * @author Malak
 * @see LibraryItem
 * @see MaterialType
 */
public class CD implements LibraryItem {

    /** Unique identifier for the CD (UUID). */
    private final UUID id = UUID.randomUUID();

    /** The title of the CD. */
    private String title;

    /** The artist of the CD. */
    private String artist;

    /** Availability status of the CD; true if available for borrowing. */
    private boolean available = true;

    /**
     * Constructs a new {@code CD} with the given title and artist.
     *
     * @param title the title of the CD; must not be null or empty
     * @param artist the artist of the CD; must not be null or empty
     * @throws IllegalArgumentException if title or artist is null or empty
     */
    public CD(String title, String artist) {
        validateNonEmpty(title, "Title");
        validateNonEmpty(artist, "Artist");
        this.title = title.trim();
        this.artist = artist.trim();
    }

    /**
     * Sets a new title for the CD.
     *
     * @param title the new title; must not be null or empty
     * @throws IllegalArgumentException if title is null or empty
     */
    public void setTitle(String title) { 
        validateNonEmpty(title, "Title"); 
        this.title = title.trim(); 
    }

    /**
     * Sets a new artist for the CD.
     *
     * @param artist the new artist; must not be null or empty
     * @throws IllegalArgumentException if artist is null or empty
     */
    public void setArtist(String artist) { 
        validateNonEmpty(artist, "Artist"); 
        this.artist = artist.trim(); 
    }

    /**
     * Returns the title of the CD.
     *
     * @return the CD title
     */
    public String getTitle() { return title; }

    /**
     * Returns the artist of the CD.
     *
     * @return the CD artist
     */
    public String getArtist() { return artist; }

    /**
     * Checks if the CD is available for borrowing.
     *
     * @return {@code true} if available, {@code false} if currently borrowed
     */
    @Override
    public boolean isAvailable() { synchronized(this) { return available; } }

    /**
     * Marks the CD as borrowed if it is available.
     *
     * @return {@code true} if the CD was successfully borrowed,
     *         {@code false} if it was already borrowed
     */
    @Override
    public boolean borrow() { synchronized(this) { if(!available) return false; available=false; return true; } }

    /**
     * Marks the CD as returned if it was borrowed.
     *
     * @return {@code true} if the CD was successfully returned,
     *         {@code false} if it was already available
     */
    @Override
    public boolean returnItem() { synchronized(this) { if(available) return false; available=true; return true; } }

    /**
     * Returns the material type of this item.
     *
     * @return {@link MaterialType#CD}
     */
    @Override
    public MaterialType getMaterialType() { return MaterialType.CD; }

    /**
     * Compares this CD to another object for equality.
     * <p>
     * Two CDs are equal if they have the same unique {@code id}.
     * </p>
     *
     * @param o the object to compare
     * @return {@code true} if the other object is a {@code CD} with the same ID
     */
    @Override
    public boolean equals(Object o) { 
        return this==o || (o instanceof CD cd && id.equals(cd.id)); 
    }

    /**
     * Returns the hash code for this CD based on its unique ID.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() { return Objects.hash(id); }

    /**
     * Returns a string representation of the CD, including title, artist, and availability.
     *
     * @return formatted string representing the CD
     */
    @Override
    public String toString() {
        return title + " by " + artist + (available ? " [AVAILABLE]" : " [BORROWED]");
    }

    /**
     * Validates that a string is not null or empty.
     *
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if value is null or empty
     */
    private void validateNonEmpty(String value, String fieldName) {
        if(value==null || value.trim().isEmpty())
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
    }
    /**
     * Checks if the CD matches the given keyword.
     * <p>
     * A CD matches if the keyword is found (case-insensitive) in the title,
     * artist, or genre.
     * </p>
     *
     * @param keyword the keyword to search for; must not be null
     * @return {@code true} if the CD matches the keyword, {@code false} otherwise
     * @throws IllegalArgumentException if {@code keyword} is null
     */
    @Override
    public boolean matchesKeyword(String keyword) {
        if (keyword == null) {
            throw new IllegalArgumentException("Keyword cannot be null");
        }
        String lower = keyword.toLowerCase();
        return title.toLowerCase().contains(lower)
            || artist.toLowerCase().contains(lower);
    }

}
