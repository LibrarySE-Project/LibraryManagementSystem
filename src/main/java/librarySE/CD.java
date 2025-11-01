package librarySE;

import java.util.Objects;

/**
 * Represents a CD item in the library.
 * <p>
 * A CD has a title and an artist, and can be borrowed or returned.
 * Availability is tracked internally. Implements the {@link LibraryItem} interface.
 * </p>
 * 
 * @author Malak
 */
public class CD implements LibraryItem {

    /** The title of the CD */
    private String title;

    /** The artist of the CD */
    private  String artist;

    /** Indicates if the CD is currently available for borrowing */
    private boolean available = true;

    /**
     * Constructs a CD with the given title and artist.
     *
     * @param title the title of the CD; must not be null or empty
     * @param artist the artist of the CD; must not be null or empty
     * @throws IllegalArgumentException if title or artist is null/empty
     */
    public CD(String title, String artist) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Title cannot be empty");
        if (artist == null || artist.trim().isEmpty()) throw new IllegalArgumentException("Artist cannot be empty");
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
        if (title == null || title.trim().isEmpty())
            throw new IllegalArgumentException("Title cannot be null or empty");
        this.title = title.trim();
    }

    /**
     * Sets a new artist for the CD.
     *
     * @param artist the new artist; must not be null or empty
     * @throws IllegalArgumentException if artist is null or empty
     */
    public void setArtist(String artist) {
        if (artist == null || artist.trim().isEmpty())
            throw new IllegalArgumentException("Artist cannot be null or empty");
        this.artist = artist.trim();
    }


    /** Returns the title of the CD */
    public String getTitle() { return title; }

    /** Returns the artist of the CD */
    public String getArtist() { return artist; }

    /**
     * Checks if the CD is available for borrowing.
     *
     * @return true if available, false otherwise
     */
    @Override
    public boolean isAvailable() {
        synchronized(this) { return available; }
    }

    /**
     * Attempts to borrow the CD in a thread-safe manner.
     *
     * @return {@code true} if the CD was successfully borrowed,
     *         {@code false} if the CD was already borrowed (not available)
     */
    @Override
    public boolean borrow() {
        synchronized(this) {
            if (!available) return false;
            available = false;
            return true;
        }
    }

    /**
     * Returns the CD in a thread-safe manner.
     *
     * @return {@code true} if the CD was successfully returned,
     *         {@code false} if the CD was already available
     */
    @Override
    public boolean returnItem() {
        synchronized(this) {
            if (available) return false;
            available = true;
            return true;
        }
    }

    /** Returns the material type of this item */
    @Override
    public MaterialType getMaterialType() {
        return MaterialType.CD;
    }

    /**
     * Compares two CDs for equality.
     * <p>
     * Two CDs are equal if they have the same title and artist (case-insensitive).
     * </p>
     *
     * @param o the object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CD)) return false;
        CD cd = (CD) o;
        return title.equalsIgnoreCase(cd.title) && artist.equalsIgnoreCase(cd.artist);
    }

    /** Returns the hash code for the CD (based on title and artist, case-insensitive) */
    @Override
    public int hashCode() {
        return Objects.hash(title.toLowerCase(), artist.toLowerCase());
    }

    /** Returns a string representation of the CD with availability status */
    @Override
    public String toString() {
        return title + " by " + artist + (available ? " [AVAILABLE]" : " [BORROWED]");
    }
}
